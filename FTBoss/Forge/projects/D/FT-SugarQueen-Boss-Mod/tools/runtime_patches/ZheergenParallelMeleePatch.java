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
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class ZheergenParallelMeleePatch implements Opcodes {
    public static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    public static final String BALANCE = "net/farmingtales/ftboss/balance/ManorBalanceHooks";
    public static final String LANES = "net/farmingtales/ftboss/boss/ai/ManorCombatLanes";
    public static final String GONGDI = "net/farmingtales/ftboss/boss/ai/GongdiSkills";

    public static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    public static ClassNode read(ZipFile archive, String name) throws Exception {
        var entry = archive.getEntry(name + ".class");
        require(entry != null, "Missing class: " + name);
        ClassNode result = new ClassNode();
        new ClassReader(archive.getInputStream(entry).readAllBytes()).accept(result, 0);
        return result;
    }

    public static MethodNode method(ClassNode owner, String name) {
        var matches = owner.methods.stream().filter(candidate -> candidate.name.equals(name)).toList();
        require(matches.size() == 1, "Expected exactly one method: " + owner.name + "." + name);
        return matches.get(0);
    }

    private static AbstractInsnNode previous(AbstractInsnNode instruction) {
        do { instruction = instruction.getPrevious(); } while (instruction != null && instruction.getOpcode() < 0);
        require(instruction != null, "Missing previous instruction");
        return instruction;
    }

    public static void transformBoss(ClassNode owner) throws Exception {
        for (var field : owner.fields) {
            if (field.name.equals("SHADOW_PHANTOM_CHANCE")) {
                require(Double.valueOf(0.35).equals(field.value) || Double.valueOf(0.65).equals(field.value), "Unknown phantom chance");
                field.value = 0.65;
            }
            if (field.name.equals("SHADOW_PHANTOM_CHANCE_PERCENT")) {
                require(Integer.valueOf(35).equals(field.value) || Integer.valueOf(65).equals(field.value), "Unknown phantom percent");
                field.value = 65;
            }
        }
        for (String name : List.of("skillShadowStrike", "onManorFiveCastStart")) {
            MethodNode target = method(owner, name);
            int matches = 0;
            for (var instruction : target.instructions) {
                if (instruction instanceof LdcInsnNode constant && constant.cst instanceof Double chance
                        && (chance == 0.35 || chance == 0.65)) {
                    constant.cst = 0.65;
                    matches++;
                }
            }
            require(matches == 1, "Expected one phantom roll in " + name);
        }
        MethodNode target = method(owner, "blocksMelee");
        boolean installed = Arrays.stream(target.instructions.toArray()).anyMatch(instruction ->
                instruction instanceof MethodInsnNode invoke && invoke.owner.equals(BOSS) && invoke.name.equals("isMini"));
        if (!installed) {
            InsnList prefix = new InsnList();
            LabelNode original = new LabelNode();
            prefix.add(new VarInsnNode(ALOAD, 0));
            prefix.add(new MethodInsnNode(INVOKEVIRTUAL, BOSS, "getVariant", "()I", false));
            prefix.add(new IntInsnNode(BIPUSH, 7));
            prefix.add(new JumpInsnNode(IF_ICMPNE, original));
            for (String name : List.of("isMini", "isGiant")) {
                prefix.add(new VarInsnNode(ALOAD, 0));
                prefix.add(new MethodInsnNode(INVOKEVIRTUAL, BOSS, name, "()Z", false));
                prefix.add(new JumpInsnNode(IFNE, original));
            }
            prefix.add(new InsnNode(ICONST_0));
            prefix.add(new InsnNode(IRETURN));
            prefix.add(original);
            prefix.add(new FrameNode(F_SAME, 0, null, 0, null));
            target.instructions.insert(prefix);
            target.maxStack = Math.max(2, target.maxStack);
        }
        new Analyzer<>(new BasicVerifier()).analyze(owner.name, target);
    }

    public static void transformBalance(ClassNode owner) throws Exception {
        MethodNode target = method(owner, "tick");
        int matches = 0;
        for (var instruction : target.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode invoke) || !invoke.owner.equals(BOSS)
                    || !List.of("isPerformingSkill", "isCastingLike").contains(invoke.name)) continue;
            AbstractInsnNode receiver = previous(instruction);
            require(receiver instanceof VarInsnNode && receiver.getOpcode() == ALOAD, "Unknown pose gate receiver");
            target.instructions.remove(receiver);
            target.instructions.set(instruction, new InsnNode(ICONST_0));
            matches++;
        }
        require(matches == 2 || matches == 0, "Partial blink pose patch");
        new Analyzer<>(new BasicVerifier()).analyze(owner.name, target);
    }

    public static byte[] bytes(ClassNode owner) {
        ClassWriter writer = new ClassWriter(0);
        owner.accept(writer);
        return writer.toByteArray();
    }

    private static byte[] fingerprint(MethodNode original) {
        ClassNode owner = new ClassNode();
        owner.version = V17;
        owner.access = ACC_PUBLIC;
        owner.name = "ZheergenMethodCheck";
        owner.superName = "java/lang/Object";
        MethodNode copy = new MethodNode(original.access, original.name, original.desc, null, null);
        original.accept(copy);
        for (var instruction : copy.instructions.toArray()) {
            if (instruction instanceof FrameNode) copy.instructions.remove(instruction);
        }
        copy.localVariables = null;
        owner.methods.add(copy);
        return bytes(owner);
    }

    public static void main(String[] arguments) throws Exception {
        try (ZipFile baseline = new ZipFile(arguments[1])) {
            for (String name : List.of(BOSS, BALANCE)) {
                ClassNode expected = read(baseline, name);
                if (name.equals(BOSS)) transformBoss(expected); else transformBalance(expected);
                if (arguments[0].equals("patch")) {
                    Path output = Path.of(arguments[2], name + ".class");
                    Files.createDirectories(output.getParent());
                    Files.write(output, bytes(expected));
                } else if (arguments[0].equals("verify")) {
                    try (ZipFile candidate = new ZipFile(arguments[2])) {
                        ClassNode actual = read(candidate, name);
                        require(actual.methods.size() == expected.methods.size() && actual.fields.size() == expected.fields.size(),
                                "Unexpected class structure change: " + name);
                        for (MethodNode target : expected.methods) {
                            MethodNode replacement = actual.methods.stream().filter(value -> value.name.equals(target.name)
                                    && value.desc.equals(target.desc)).findFirst().orElseThrow();
                            require(Arrays.equals(fingerprint(target), fingerprint(replacement)), "Unexpected method change: " + name + "." + target.name);
                        }
                        for (int index = 0; index < expected.fields.size(); index++) {
                            var first = expected.fields.get(index);
                            var second = actual.fields.get(index);
                            require(first.name.equals(second.name) && first.desc.equals(second.desc)
                                    && first.access == second.access && java.util.Objects.equals(first.value, second.value), "Unexpected field change");
                        }
                        byte[] once = bytes(actual);
                        if (name.equals(BOSS)) transformBoss(actual); else transformBalance(actual);
                        require(Arrays.equals(once, bytes(actual)), "Non-idempotent patch: " + name);
                    }
                } else throw new IllegalArgumentException("Expected patch or verify");
            }
        }
        System.out.println("PASS " + arguments[0] + ": exact Zheergen method changes; all other methods preserved");
    }
}
