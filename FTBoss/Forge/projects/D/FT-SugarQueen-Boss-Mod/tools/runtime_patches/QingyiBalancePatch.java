import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class QingyiBalancePatch implements Opcodes {
    private static final String ENTITY = "net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String RECOVERY = "net/farmingtales/ftboss/boss/ai/QingyiTridentRecovery";

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static AbstractInsnNode nextCode(AbstractInsnNode instruction) {
        do { instruction = instruction.getNext(); } while (instruction != null && instruction.getOpcode() < 0);
        return instruction;
    }

    private static void removeUntil(MethodNode method, AbstractInsnNode first, AbstractInsnNode limit) {
        while (first != limit) {
            require(first != null, "Missing patch boundary");
            AbstractInsnNode following = first.getNext();
            method.instructions.remove(first);
            first = following;
        }
    }

    private static MethodNode method(ClassNode type, String name) {
        return type.methods.stream().filter(candidate -> candidate.name.equals(name))
                .findFirst().orElseThrow();
    }

    private static void clearArmorCalculation(MethodNode method) {
        int replacements = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode call && call.name.equals("armorSteps")) {
                InsnList replacement = new InsnList();
                replacement.add(new InsnNode(POP2));
                replacement.add(new InsnNode(ICONST_0));
                method.instructions.insertBefore(call, replacement);
                method.instructions.remove(call);
                replacements++;
            }
        }
        require(replacements == 1, "Unexpected armor calculation in " + method.name);
    }

    private static void patch(ClassNode type) throws Exception {
        MethodNode sync = method(type, "syncQingyiTridentBuffs");
        clearArmorCalculation(sync);
        boolean removedArmor = false;
        for (AbstractInsnNode instruction : sync.instructions.toArray()) {
            if (!(instruction instanceof FieldInsnNode field) || !field.name.equals("QINGYI_ARMOR_MOD")) continue;
            AbstractInsnNode removeCall = nextCode(field);
            require(removeCall instanceof MethodInsnNode call && call.name.equals("m_22120_"), "Armor removal changed");
            AbstractInsnNode first = nextCode(removeCall);
            AbstractInsnNode branch = nextCode(first);
            require(branch instanceof JumpInsnNode && branch.getOpcode() == IFLE, "Armor grant changed");
            removeUntil(sync, first, ((JumpInsnNode) branch).label);
            removedArmor = true;
            break;
        }
        require(removedArmor, "Missing trident armor modifier");

        MethodNode summon = method(type, "onQingyiTridentSummoned");
        JumpInsnNode cap = null;
        MethodInsnNode synchronize = null;
        for (AbstractInsnNode instruction : summon.instructions) {
            if (instruction instanceof JumpInsnNode jump && jump.getOpcode() == IF_ICMPGE && cap == null) cap = jump;
            if (instruction instanceof MethodInsnNode call && call.name.equals("syncQingyiTridentBuffs")) {
                synchronize = call;
                break;
            }
        }
        require(cap != null && synchronize != null, "Summon growth prefix changed");
        removeUntil(summon, synchronize.getNext(), cap.label);
        InsnList recovery = new InsnList();
        recovery.add(new VarInsnNode(ALOAD, 0));
        recovery.add(new MethodInsnNode(INVOKESTATIC, RECOVERY, "onSummoned", "(L" + ENTITY + ";)V", false));
        summon.instructions.insertBefore(nextCode(cap.label), recovery);
        clearArmorCalculation(summon);
        int logs = 0;
        for (AbstractInsnNode instruction : summon.instructions) {
            if (instruction instanceof LdcInsnNode constant && constant.cst instanceof String text
                    && text.contains("血量上限 +{})")) {
                constant.cst = text.replace("血量上限 +{})", "血量上限 +{}; 每5把回复10点生命)");
                logs++;
            }
        }
        require(logs == 1, "Summon log changed");
        for (MethodNode changed : new MethodNode[] {sync, summon}) {
            new Analyzer<>(new BasicVerifier()).analyze(type.name, changed);
        }
    }

    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[1], ENTITY + ".class");
        try (ZipFile archive = new ZipFile(arguments[0])) {
            byte[] original = archive.getInputStream(archive.getEntry(ENTITY + ".class")).readAllBytes();
            ClassReader reader = new ClassReader(original);
            ClassNode type = new ClassNode();
            reader.accept(type, 0);
            patch(type);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            type.accept(writer);
            Files.createDirectories(output.getParent());
            Files.write(output, writer.toByteArray());
            ClassNode verified = new ClassNode();
            new ClassReader(writer.toByteArray()).accept(verified, 0);
            for (String name : new String[] {"syncQingyiTridentBuffs", "onQingyiTridentSummoned"}) {
                new Analyzer<>(new BasicVerifier()).analyze(verified.name, method(verified, name));
            }
        }
        System.out.println("PASS: trident armor removed; recurring recovery is outside the growth cap; bytecode verified");
    }
}
