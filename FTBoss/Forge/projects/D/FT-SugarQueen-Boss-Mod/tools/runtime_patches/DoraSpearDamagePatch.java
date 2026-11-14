import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class DoraSpearDamagePatch {
    public static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    public static final String RULES = "net/farmingtales/ftboss/util/DoraSpearDamageRules";
    private static final String ATTRIBUTES = "net/minecraft/world/entity/ai/attributes/Attributes";
    private static final String ATTRIBUTE_DESCRIPTOR = "Lnet/minecraft/world/entity/ai/attributes/Attribute;";

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static ClassNode read(Path archive) throws Exception {
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            ClassNode type = new ClassNode();
            new ClassReader(zip.getInputStream(zip.getEntry(BOSS + ".class")).readAllBytes()).accept(type, 0);
            return type;
        }
    }

    private static MethodNode spear(ClassNode type) {
        List<MethodNode> matches = type.methods.stream().filter(method -> method.name.equals("spearChargeHit")
                && method.desc.equals("(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;)V")).toList();
        require(matches.size() == 1, "Expected exactly one spear attack");
        return matches.get(0);
    }

    private static List<MethodInsnNode> calls(MethodNode method, String owner, String name) {
        return Arrays.stream(method.instructions.toArray()).filter(instruction -> instruction instanceof MethodInsnNode call
                && call.owner.equals(owner) && call.name.equals(name)).map(MethodInsnNode.class::cast).toList();
    }

    private static byte[] bytes(ClassNode type) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        return writer.toByteArray();
    }

    private static InsnList scaling() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, ATTRIBUTES, "f_22279_", ATTRIBUTE_DESCRIPTOR));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "m_21133_", "(" + ATTRIBUTE_DESCRIPTOR + ")D", false));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RULES, "damage", "(FD)F", false));
        return instructions;
    }

    private static void patch(ClassNode type) {
        MethodNode method = spear(type);
        List<MethodInsnNode> hooks = calls(method, RULES, "damage");
        if (!hooks.isEmpty()) {
            require(hooks.size() == 2, "Partial or duplicate spear patch");
            return;
        }
        List<MethodInsnNode> hurts = calls(method, "net/minecraft/world/entity/LivingEntity", "m_6469_");
        require(hurts.size() == 1, "Expected one spear damage application");
        AbstractInsnNode literal = hurts.get(0).getPrevious();
        require(literal instanceof LdcInsnNode number && number.cst.equals(8.333333F), "Unknown spear base damage");
        List<MethodInsnNode> boxes = calls(method, "java/lang/Integer", "valueOf");
        require(boxes.size() == 3, "Unknown spear damage log");
        MethodInsnNode logDamage = boxes.get(boxes.size() - 1);
        AbstractInsnNode logLiteral = logDamage.getPrevious();
        require(logLiteral.getOpcode() == Opcodes.BIPUSH, "Unknown spear log literal");
        method.instructions.set(logLiteral, new LdcInsnNode(8.333333F));
        method.instructions.insertBefore(logDamage, scaling());
        logDamage.owner = "java/lang/Float";
        logDamage.desc = "(F)Ljava/lang/Float;";
        method.instructions.insert(literal, scaling());
    }

    private static void verify(Path baseline, Path candidate) throws Exception {
        ClassNode original = read(baseline);
        ClassNode staged = read(candidate);
        MethodNode method = spear(staged);
        List<MethodInsnNode> hooks = calls(method, RULES, "damage");
        require(hooks.size() == 2, "Damage and log must both use speed scaling");
        for (MethodInsnNode hook : hooks) {
            require(hook.desc.equals("(FD)F"), "Wrong scaling signature");
            require(hook.getPrevious() instanceof MethodInsnNode speed && speed.owner.equals(BOSS)
                    && speed.name.equals("m_21133_") && speed.desc.equals("(" + ATTRIBUTE_DESCRIPTOR + ")D"), "Speed must read final attribute");
            require(hook.getPrevious().getPrevious() instanceof FieldInsnNode attribute
                    && attribute.owner.equals(ATTRIBUTES) && attribute.name.equals("f_22279_"), "Wrong speed attribute");
            require(hook.getPrevious().getPrevious().getPrevious() instanceof VarInsnNode receiver
                    && receiver.getOpcode() == Opcodes.ALOAD && receiver.var == 0, "Speed must belong to Dora");
        }
        new Analyzer<>(new BasicVerifier()).analyze(BOSS, method);
        byte[] once = bytes(staged);
        patch(staged);
        require(Arrays.equals(once, bytes(staged)), "Patch is not idempotent");
        for (MethodInsnNode hook : hooks) {
            for (int index = 0; index < 3; index++) method.instructions.remove(hook.getPrevious());
            method.instructions.remove(hook);
        }
        List<MethodInsnNode> originalBoxes = calls(spear(original), "java/lang/Integer", "valueOf");
        MethodInsnNode logDamage = calls(method, "java/lang/Float", "valueOf").get(0);
        method.instructions.set(logDamage.getPrevious(), originalBoxes.get(originalBoxes.size() - 1).getPrevious().clone(null));
        logDamage.owner = "java/lang/Integer";
        logDamage.desc = "(I)Ljava/lang/Integer;";
        require(Arrays.equals(bytes(original), bytes(staged)), "Non-spear behavior changed");
        System.out.println("PASS spear bytecode types, live attribute wiring, idempotence and exact reverse-patch preservation");
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments[0].equals("check")) {
            verify(Path.of(arguments[1]), Path.of(arguments[2]));
            return;
        }
        require(arguments[0].equals("apply"), "Expected apply or check");
        ClassNode type = read(Path.of(arguments[1]));
        patch(type);
        Path output = Path.of(arguments[2]).resolve(BOSS + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, bytes(type));
        System.out.println("PASS scoped Dora spear speed patch");
    }
}
