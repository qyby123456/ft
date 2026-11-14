import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class RangedBossTeleportPatch implements Opcodes {
    private static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String GOAL = BOSS + "$PursueGoal";
    private static final String HOOK = "net/farmingtales/ftboss/boss/ai/RangedBossTeleport";

    private static byte[] patch(byte[] bytes, boolean goal) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, 0);
        int changes = 0;
        for (MethodNode method : type.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals(HOOK))
                    throw new IllegalStateException("Already patched: " + type.name);
            }
            if (!goal && method.name.equals("hitFeedback")
                    && method.desc.equals("(Lnet/minecraft/world/damagesource/DamageSource;)V")) {
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(ALOAD, 0));
                hook.add(new VarInsnNode(ALOAD, 1));
                hook.add(new MethodInsnNode(INVOKESTATIC, HOOK, "confirmedHit",
                        "(L" + BOSS + ";Lnet/minecraft/world/damagesource/DamageSource;)V", false));
                method.instructions.insert(hook);
                changes++;
            }
            if (goal && method.name.equals("m_8037_") && method.desc.equals("()V")) {
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode call)
                            || !call.owner.equals("net/minecraft/world/entity/ai/control/LookControl")
                            || !call.desc.equals("(Lnet/minecraft/world/entity/Entity;FF)V")) continue;
                    String outer = type.fields.stream().filter(field -> field.desc.equals("L" + BOSS + ";"))
                            .findFirst().orElseThrow().name;
                    LabelNode resume = new LabelNode();
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(ALOAD, 0));
                    hook.add(new FieldInsnNode(GETFIELD, GOAL, outer, "L" + BOSS + ";"));
                    hook.add(new VarInsnNode(ALOAD, 1));
                    hook.add(new MethodInsnNode(INVOKESTATIC, HOOK, "tick", "(L" + BOSS
                            + ";Lnet/minecraft/world/entity/LivingEntity;)Z", false));
                    hook.add(new JumpInsnNode(IFEQ, resume));
                    hook.add(new InsnNode(RETURN));
                    hook.add(resume);
                    hook.add(new FrameNode(F_SAME, 0, null, 0, null));
                    method.instructions.insert(call, hook);
                    changes++;
                }
            }
        }
        if (changes != 1) throw new IllegalStateException("Expected one hook in " + type.name + ", got " + changes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        byte[] result = writer.toByteArray();
        ClassNode checked = new ClassNode();
        new ClassReader(result).accept(checked, 0);
        for (MethodNode method : checked.methods) {
            if ((method.access & (ACC_NATIVE | ACC_ABSTRACT)) == 0)
                new Analyzer<>(new BasicVerifier()).analyze(checked.name, method);
        }
        return result;
    }

    private static void save(Path root, String name, byte[] bytes) throws Exception {
        Path output = root.resolve(name + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, bytes);
    }

    private static void check(String baselinePath, String candidatePath) throws Exception {
        Set<String> expected = Set.of(BOSS + ".class", GOAL + ".class", HOOK + ".class", HOOK + "$State.class");
        Set<String> changed = new HashSet<>();
        try (ZipFile baseline = new ZipFile(baselinePath); ZipFile candidate = new ZipFile(candidatePath)) {
            var entries = baseline.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                var installed = candidate.getEntry(entry.getName());
                if (installed == null) throw new AssertionError("Removed entry: " + entry.getName());
                byte[] before = baseline.getInputStream(entry).readAllBytes();
                byte[] after = candidate.getInputStream(installed).readAllBytes();
                if (!Arrays.equals(before, after)) changed.add(entry.getName());
                if (entry.getName().equals(BOSS + ".class") || entry.getName().equals(GOAL + ".class")) {
                    if (!Arrays.equals(patch(before, entry.getName().equals(GOAL + ".class")), after))
                        throw new AssertionError("Unexpected patch: " + entry.getName());
                }
            }
            var updated = candidate.entries();
            Set<String> unique = new HashSet<>();
            while (updated.hasMoreElements()) {
                var entry = updated.nextElement();
                if (!unique.add(entry.getName())) throw new AssertionError("Duplicate ZIP entry");
                if (baseline.getEntry(entry.getName()) == null && !entry.isDirectory()) changed.add(entry.getName());
            }
            if (!changed.equals(expected)) throw new AssertionError("Out-of-scope changes: " + changed);
        }
        System.out.println("PASS: exactly two narrow AI hooks and two new classes; all other runtime entries preserved");
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments[0].equals("check")) {
            check(arguments[1], arguments[2]);
            return;
        }
        Path output = Path.of(arguments[3]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        for (String name : new String[]{HOOK, HOOK + "$State"}) {
            ClassWriter writer = new ClassWriter(0);
            new ClassReader(Files.readAllBytes(Path.of(arguments[1]).resolve(name + ".class")))
                    .accept(new ClassRemapper(writer, names), 0);
            save(output, name, writer.toByteArray());
        }
        try (ZipFile baseline = new ZipFile(arguments[0])) {
            if (baseline.getEntry(HOOK + ".class") != null) throw new IllegalStateException("Already patched baseline");
            for (String name : new String[]{BOSS, GOAL})
                save(output, name, patch(baseline.getInputStream(baseline.getEntry(name + ".class")).readAllBytes(), name.equals(GOAL)));
        }
        System.out.println("PASS: accepted-hit and pursuit hooks installed without rebuilding source-diverged boss classes");
    }
}
