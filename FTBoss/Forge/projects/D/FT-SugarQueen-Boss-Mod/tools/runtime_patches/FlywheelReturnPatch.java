import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
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
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class FlywheelReturnPatch {
    public static final String FLYWHEEL = "net/farmingtales/ftboss/entity/FlywheelProjectileEntity";
    public static final String RULES = "net/farmingtales/ftboss/util/FlywheelCombatRules";

    private static ClassNode compiled(Path classes, String name, ZicaiVoidPatch.Names names) throws Exception {
        ClassNode result = new ClassNode();
        new ClassReader(Files.readAllBytes(classes.resolve(name + ".class"))).accept(new ClassRemapper(result, names), 0);
        return result;
    }

    private static MethodNode method(ClassNode type, String name) {
        var matches = type.methods.stream().filter(method -> method.name.equals(name)).toList();
        if (matches.size() != 1) throw new IllegalStateException("Expected one method: " + name);
        return matches.get(0);
    }

    private static InsnList invoke(String name) {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, FLYWHEEL, name, "()V", false));
        return instructions;
    }

    private static void guard(MethodNode method, boolean booleanResult) {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, FLYWHEEL, "isAttackReady", "()Z", false));
        LabelNode ready = new LabelNode();
        instructions.add(new JumpInsnNode(Opcodes.IFNE, ready));
        if (booleanResult) instructions.add(new InsnNode(Opcodes.ICONST_0));
        instructions.add(new InsnNode(booleanResult ? Opcodes.IRETURN : Opcodes.RETURN));
        instructions.add(ready);
        instructions.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(instructions);
    }

    private static void save(ClassNode type, Path output) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path file = output.resolve(type.name + ".class");
        Files.createDirectories(file.getParent());
        Files.write(file, writer.toByteArray());
    }

    public static void main(String[] arguments) throws Exception {
        Path classes = Path.of(arguments[1]);
        Path output = Path.of(arguments[3]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        ClassNode replacement = compiled(classes, FLYWHEEL, names);
        try (ZipFile baseline = new ZipFile(arguments[0])) {
            ClassNode original = new ClassNode();
            new ClassReader(baseline.getInputStream(baseline.getEntry(FLYWHEEL + ".class")).readAllBytes()).accept(original, 0);
            for (String name : Set.of("isAttackReady", "startReturning")) {
                if (original.methods.stream().anyMatch(method -> method.name.equals(name))) throw new IllegalStateException("Already patched");
                original.methods.add(method(replacement, name));
            }
            original.methods.remove(method(original, "launchAgain"));
            original.methods.add(method(replacement, "launchAgain"));
            guard(method(original, "m_5603_"), true);
            MethodNode hit = method(original, "m_5790_");
            guard(hit, false);
            int inserted = 0;
            for (var instruction : hit.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode call)
                        || !call.owner.equals("net/farmingtales/ftboss/boss/ai/BossTargets") || !call.name.equals("valid")) continue;
                var next = instruction.getNext();
                while (next.getOpcode() < 0) next = next.getNext();
                if (!(next instanceof JumpInsnNode jump) || jump.getOpcode() != Opcodes.IFNE) throw new IllegalStateException("Unexpected target gate");
                next = jump.label.getNext();
                while (next.getOpcode() < 0) next = next.getNext();
                hit.instructions.insertBefore(next, invoke("startReturning"));
                inserted++;
            }
            if (inserted != 1) throw new IllegalStateException("Expected one contact return hook");
            save(original, output);
        }
        ClassNode rules = compiled(classes, RULES, names);
        try (ZipFile baseline = new ZipFile(arguments[0])) {
            ClassNode original = new ClassNode();
            new ClassReader(baseline.getInputStream(baseline.getEntry(RULES + ".class")).readAllBytes()).accept(original, 0);
            original.methods.add(method(rules, "canAttack"));
            save(original, output);
        }
        System.out.println("PASS: return-cycle gates added; runtime targeting, damage, lifetime and elite tuning preserved");
    }
}
