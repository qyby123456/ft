import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.*;

public final class DoraXueliStallPatch {
    public static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    public static final Set<String> EDITED = Set.of("m_8119_", "launchXueliSlam", "skillXueliSlam", "xueliSlamTick");
    public static final List<String> HELPERS = List.of("cancelXueliSlam", "repairXueliSlam", "expireCombatPoseTimers", "canScheduleSharedSkill");

    public static ClassNode read(byte[] bytes) {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, 0);
        return type;
    }

    public static MethodNode method(ClassNode type, String name) {
        List<MethodNode> matches = type.methods.stream().filter(body -> body.name.equals(name)).toList();
        if (matches.size() != 1) throw new IllegalStateException("Missing or ambiguous method: " + name);
        return matches.get(0);
    }

    private static AbstractInsnNode next(AbstractInsnNode node) {
        do { node = node.getNext(); } while (node != null && node.getOpcode() < 0);
        if (node == null) throw new IllegalStateException("Unexpected method ending");
        return node;
    }

    private static InsnList call(String name) {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, name,
                name.equals("canScheduleSharedSkill") ? "()Z" : "()V", false));
        return code;
    }

    private static void affected(InsnList code, LabelNode original) {
        LabelNode selected = new LabelNode();
        code.add(callVariant());
        code.add(new InsnNode(Opcodes.ICONST_2));
        code.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, selected));
        code.add(callVariant());
        code.add(new IntInsnNode(Opcodes.BIPUSH, 10));
        code.add(new JumpInsnNode(Opcodes.IF_ICMPNE, original));
        code.add(selected);
    }

    private static InsnList callVariant() {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "getVariant", "()I", false));
        return code;
    }

    public static void transform(ClassNode type) {
        MethodNode tick = method(type, "m_8119_");
        List<MethodInsnNode> gates = Arrays.stream(tick.instructions.toArray())
                .filter(instruction -> instruction instanceof MethodInsnNode invoke && invoke.name.equals("spellReady")
                        && invoke.owner.equals("net/farmingtales/ftboss/boss/ai/ManorCombatLanes"))
                .map(instruction -> (MethodInsnNode) instruction).toList();
        if (gates.size() != 1) throw new IllegalStateException("Unsupported shared scheduler; rebase explicitly");
        MethodInsnNode gate = gates.get(0);
        AbstractInsnNode first = gate.getPrevious();
        if (first.getOpcode() != Opcodes.ALOAD) throw new IllegalStateException("Unexpected scheduler receiver");
        JumpInsnNode skipVisuals = (JumpInsnNode) next(gate);
        AbstractInsnNode cursor = next(skipVisuals);
        JumpInsnNode deny = null;
        int counters = 0;
        for (int scanned = 0; scanned < 10; scanned++) {
            if (cursor instanceof FieldInsnNode field && field.owner.equals(BOSS)
                    && (field.name.equals("actionTicks") || field.name.equals("castTicks"))) {
                counters++;
                if (field.name.equals("castTicks")) deny = (JumpInsnNode) next(cursor);
            }
            if (deny != null) break;
            cursor = next(cursor);
        }
        if (counters != 2 || deny == null || deny.getOpcode() != Opcodes.IFGT) {
            throw new IllegalStateException("Unsupported animation gate");
        }
        LabelNode original = new LabelNode();
        InsnList schedule = new InsnList();
        affected(schedule, original);
        schedule.add(call("canScheduleSharedSkill"));
        schedule.add(new JumpInsnNode(Opcodes.IFEQ, deny.label));
        schedule.add(new JumpInsnNode(Opcodes.GOTO, skipVisuals.label));
        schedule.add(original);
        tick.instructions.insertBefore(first, schedule);

        FieldInsnNode client = Arrays.stream(tick.instructions.toArray())
                .filter(instruction -> instruction instanceof FieldInsnNode field
                        && field.owner.equals("net/minecraft/world/level/Level") && field.name.equals("f_46443_"))
                .map(instruction -> (FieldInsnNode) instruction).findFirst().orElseThrow();
        if (!(next(client) instanceof JumpInsnNode server) || server.getOpcode() != Opcodes.IFEQ) {
            throw new IllegalStateException("Unexpected server branch");
        }
        LabelNode keep = new LabelNode();
        InsnList expiry = new InsnList();
        affected(expiry, keep);
        expiry.add(call("expireCombatPoseTimers"));
        expiry.add(keep);
        tick.instructions.insert(server.label, expiry);

        MethodNode launch = method(type, "launchXueliSlam");
        AbstractInsnNode rejected = Arrays.stream(launch.instructions.toArray())
                .filter(instruction -> instruction.getOpcode() == Opcodes.RETURN).findFirst().orElseThrow();
        MethodInsnNode validation = Arrays.stream(launch.instructions.toArray())
                .filter(instruction -> instruction instanceof MethodInsnNode invoke && invoke.name.equals("canTeleportWith"))
                .map(instruction -> (MethodInsnNode) instruction).findFirst().orElseThrow();
        if (!(next(validation) instanceof JumpInsnNode jump) || next(jump) != rejected) {
            throw new IllegalStateException("Unexpected slam rejection branch");
        }
        launch.instructions.insertBefore(rejected, call("cancelXueliSlam"));
        method(type, "xueliSlamTick").instructions.insert(call("repairXueliSlam"));

        MethodNode start = method(type, "skillXueliSlam");
        MethodInsnNode pick = Arrays.stream(start.instructions.toArray())
                .filter(instruction -> instruction instanceof MethodInsnNode invoke && invoke.name.equals("pickSlamVictim"))
                .map(instruction -> (MethodInsnNode) instruction).findFirst().orElseThrow();
        AbstractInsnNode store = next(pick);
        AbstractInsnNode load = next(store);
        AbstractInsnNode nullCheck = next(load);
        if (!(load instanceof VarInsnNode victim) || !(nullCheck instanceof JumpInsnNode missing)
                || missing.getOpcode() != Opcodes.IFNONNULL) throw new IllegalStateException("Unexpected slam victim branch");
        LabelNode rejectedStart = new LabelNode();
        start.instructions.insertBefore(next(nullCheck), rejectedStart);
        InsnList valid = new InsnList();
        valid.add(new VarInsnNode(Opcodes.ALOAD, 0));
        valid.add(new VarInsnNode(Opcodes.ALOAD, victim.var));
        valid.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "canTeleportWith", validation.desc, false));
        valid.add(new JumpInsnNode(Opcodes.IFEQ, rejectedStart));
        start.instructions.insert(missing.label, valid);
    }

    public static byte[] normalized(MethodNode method) {
        MethodNode copy = new MethodNode(method.access, method.name, method.desc, method.signature, method.exceptions.toArray(String[]::new));
        method.accept(copy);
        for (AbstractInsnNode instruction : copy.instructions.toArray()) if (instruction instanceof FrameNode) copy.instructions.remove(instruction);
        copy.maxStack = 0;
        copy.maxLocals = 0;
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "StallMethod", null, "java/lang/Object", null);
        copy.accept(writer.visitMethod(copy.access, copy.name, copy.desc, copy.signature, copy.exceptions.toArray(String[]::new)));
        writer.visitEnd();
        return writer.toByteArray();
    }

    public static void main(String[] arguments) throws Exception {
        Path payload = Path.of(arguments[3]);
        ClassNode boss;
        try (ZipFile archive = new ZipFile(arguments[0])) {
            boss = read(archive.getInputStream(archive.getEntry(BOSS + ".class")).readAllBytes());
        }
        if (boss.methods.stream().anyMatch(method -> method.name.equals("repairXueliSlam"))) throw new IllegalStateException("Already patched");
        transform(boss);
        ClassWriter donorWriter = new ClassWriter(0);
        new ClassReader(Files.readAllBytes(Path.of(arguments[1]).resolve(BOSS + ".class")))
                .accept(new ClassRemapper(donorWriter, new ZicaiVoidPatch.Names(Path.of(arguments[2]))), 0);
        ClassNode donor = read(donorWriter.toByteArray());
        for (String name : HELPERS) {
            if (boss.methods.stream().anyMatch(method -> method.name.equals(name))) throw new IllegalStateException("Conflicting helper: " + name);
            boss.methods.add(method(donor, name));
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        boss.accept(writer);
        Path output = payload.resolve(BOSS + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
        Path clock = payload.resolve("net/farmingtales/ftboss/util/ManorCombatClock.class");
        Files.createDirectories(clock.getParent());
        Files.copy(Path.of(arguments[1]).resolve("net/farmingtales/ftboss/util/ManorCombatClock.class"), clock,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        System.out.println("PASS targeted Dora/Xueli scheduler and slam cleanup patch");
    }
}
