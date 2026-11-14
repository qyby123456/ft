import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class ManorFarthestSigilPatch {
    public static final String PREFIX = "net/farmingtales/ftboss/";
    public static final String HOOK = PREFIX + "boss/ai/ManorFarthestSigil";
    public static final String DESCRIPTOR = "(Lnet/minecraft/server/level/ServerLevel;L" + PREFIX
            + "entity/EasterBossEntity;Ljava/util/List;Lnet/minecraft/world/phys/Vec3;)";

    public static void save(ClassNode type, Path destination) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path output = destination.resolve(type.name + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
    }

    public static ClassNode read(ZipFile jar, String name) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getEntry(name + ".class")).readAllBytes()).accept(type, 0);
        return type;
    }

    private static void hook(ClassNode type, String methodName) {
        var matches = type.methods.stream().filter(method -> method.name.equals(methodName) && method.desc.equals(DESCRIPTOR + "V")).toList();
        if (matches.size() != 1) throw new IllegalStateException("Unexpected attack entry: " + type.name + "/" + methodName);
        var method = matches.get(0);
        for (var instruction : method.instructions)
            if (instruction instanceof MethodInsnNode call && call.owner.equals(HOOK)) throw new IllegalStateException("Already patched");
        InsnList prefix = new InsnList();
        for (int slot = 0; slot < 4; slot++) prefix.add(new VarInsnNode(Opcodes.ALOAD, slot));
        prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "tick", DESCRIPTOR + "Z", false));
        LabelNode original = new LabelNode();
        prefix.add(new JumpInsnNode(Opcodes.IFEQ, original));
        prefix.add(new InsnNode(Opcodes.RETURN));
        prefix.add(original);
        prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(prefix);
    }

    public static void main(String[] arguments) throws Exception {
        Path classes = Path.of(arguments[1]);
        Path payload = Path.of(arguments[3]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        try (ZipFile jar = new ZipFile(arguments[0])) {
            ClassNode normal = read(jar, PREFIX + "boss/ai/ManorMultiplayerBalance");
            ClassNode finale = read(jar, PREFIX + "boss/ai/ManorFinaleCombat");
            hook(normal, "tickIndependentRangedAttack");
            hook(finale, "tickAssault");
            save(normal, payload);
            save(finale, payload);
        }
        try (var files = Files.walk(classes.resolve("net/farmingtales/ftboss"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".class")).toList()) {
                if (!file.getFileName().toString().startsWith("ManorFarthestSigil")) continue;
                ClassNode output = new ClassNode();
                new ClassReader(Files.readAllBytes(file)).accept(new ClassRemapper(output, names), 0);
                save(output, payload);
            }
        }
        System.out.println("PASS: normal and finale attack entry hooks; unrelated runtime classes preserved");
    }
}
