import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class SharedCombatCadencePatch {
    public static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    public static final String GOAL = BOSS + "$PlayerReachMeleeGoal";
    public static final String RULES = "net/farmingtales/ftboss/util/ManorCombatClock";
    public static final List<String> BOSS_METHODS = List.of("tickSharedSkillCooldown", "expireCombatPoseTimers", "canScheduleSharedSkill");

    public static void main(String[] arguments) throws Exception {
        Path compiled = Path.of(arguments[1]), payload = Path.of(arguments[2]);
        try (ZipFile archive = new ZipFile(arguments[0])) {
            ClassNode boss = read(archive, BOSS), goal = read(archive, GOAL);
            boolean bossInstalled = boss.methods.stream().anyMatch(method -> method.name.equals("tickSharedSkillCooldown"));
            boolean goalInstalled = goal.methods.stream().anyMatch(method -> method.name.equals("refreshMeleeCooldown"));
            if (bossInstalled != goalInstalled) throw new IllegalStateException("Partial shared cadence patch needs explicit merge");
            if (!bossInstalled) {
                transformBoss(boss);
                transformGoal(goal);
                mergeDonor(boss, donor(compiled, BOSS), BOSS_METHODS, List.of("sharedSkillClockTick", "sharedSkillClockInitialized"));
                mergeDonor(goal, donor(compiled, GOAL), List.of("refreshMeleeCooldown"), List.of("meleeClockTick", "meleeClockInitialized"));
            }
            requireHooks(boss, goal);
            save(boss, payload.resolve(BOSS + ".class"));
            save(goal, payload.resolve(GOAL + ".class"));
        }
        Path rules = payload.resolve(RULES + ".class");
        Files.createDirectories(rules.getParent());
        Files.write(rules, Files.readAllBytes(compiled.resolve(RULES + ".class")));
        System.out.println("PASS method-level shared cadence patch; no shooting-sequence or gameplay-lock replacement");
    }

    private static void requireHooks(ClassNode boss, ClassNode goal) {
        MethodNode tick = method(boss, "m_8119_", "()V");
        for (String name : BOSS_METHODS) {
            method(boss, name, name.equals("canScheduleSharedSkill") ? "()Z" : "()V");
            long calls = java.util.Arrays.stream(tick.instructions.toArray())
                    .filter(instruction -> instruction instanceof MethodInsnNode invoke && invoke.owner.equals(BOSS) && invoke.name.equals(name)).count();
            if (calls != (name.equals("tickSharedSkillCooldown") ? 2 : 1)) {
                throw new IllegalStateException("Shared tick hook was overwritten; explicit block-level remerge required: " + name);
            }
        }
        for (String name : List.of("m_8037_", "m_8056_", "m_6739_")) {
            MethodNode body = method(goal, name, name.equals("m_6739_") ? "(Lnet/minecraft/world/entity/LivingEntity;D)V" : "()V");
            long calls = java.util.Arrays.stream(body.instructions.toArray())
                    .filter(instruction -> instruction instanceof MethodInsnNode invoke && invoke.owner.equals(GOAL) && invoke.name.equals("refreshMeleeCooldown")).count();
            if (calls != 1) throw new IllegalStateException("Goal clock hook was overwritten: " + name);
        }
    }

    public static void transformBoss(ClassNode type) {
        MethodNode tick = method(type, "m_8119_", "()V");
        List<AbstractInsnNode> clientChecks = java.util.Arrays.stream(tick.instructions.toArray())
                .filter(instruction -> instruction instanceof FieldInsnNode field && field.owner.equals("net/minecraft/world/level/Level")
                        && field.name.equals("f_46443_")).toList();
        if (clientChecks.isEmpty() || !(next(clientChecks.get(0)) instanceof JumpInsnNode server)) {
            throw new IllegalStateException("Missing authoritative server branch");
        }
        AbstractInsnNode entry = server.label;
        while (entry.getNext() != null && entry.getNext().getOpcode() < 0) entry = entry.getNext();
        InsnList clocks = call(BOSS, "tickSharedSkillCooldown");
        clocks.add(call(BOSS, "expireCombatPoseTimers"));
        tick.instructions.insert(entry, clocks);

        AbstractInsnNode decrement = null, end = null;
        int matches = 0;
        for (AbstractInsnNode instruction : tick.instructions) {
            if (instruction instanceof FieldInsnNode field && field.owner.equals(BOSS) && field.name.equals("skillCd")
                    && instruction.getOpcode() == Opcodes.GETFIELD && next(instruction).getOpcode() == Opcodes.ICONST_1
                    && next(next(instruction)).getOpcode() == Opcodes.ISUB) {
                decrement = previous(previous(instruction));
                end = next(next(next(instruction)));
                matches++;
            }
        }
        if (matches != 1 || decrement.getOpcode() != Opcodes.ALOAD || !(end instanceof FieldInsnNode cooldownStore)
                || cooldownStore.getOpcode() != Opcodes.PUTFIELD || !cooldownStore.name.equals("skillCd")) {
            throw new IllegalStateException("Unexpected shared cooldown decrement; leave Dora accumulator unchanged");
        }
        replace(tick, decrement, end, call(BOSS, "tickSharedSkillCooldown"));

        List<MethodInsnNode> gates = java.util.Arrays.stream(tick.instructions.toArray())
                .filter(instruction -> instruction instanceof MethodInsnNode invoke && invoke.owner.equals(BOSS)
                        && invoke.name.equals("allowsParallelMelee"))
                .map(instruction -> (MethodInsnNode)instruction).toList();
        if (gates.size() != 1) throw new IllegalStateException("Ambiguous shared visual gate");
        MethodInsnNode gate = gates.get(0);
        AbstractInsnNode cursor = gate;
        JumpInsnNode deny = null;
        int fields = 0;
        for (int scanned = 0; scanned < 12; scanned++) {
            cursor = next(cursor);
            if (cursor instanceof MethodInsnNode invoke) {
                if (!invoke.owner.equals(BOSS) || !invoke.name.equals("doraCadenceActionGateTicks") || !invoke.desc.equals("()I")) {
                    throw new IllegalStateException("Unknown shared scheduler hook; explicit merge required: " + invoke.name);
                }
                fields++;
            }
            if (cursor instanceof FieldInsnNode field) {
                if (!List.of("actionTicks", "castTicks").contains(field.name)) throw new IllegalStateException("Unknown visual gate field");
                fields++;
                if (field.name.equals("castTicks")) {
                    if (next(cursor) instanceof JumpInsnNode jump && jump.getOpcode() == Opcodes.IFGT) deny = jump;
                    break;
                }
            }
        }
        if (fields != 2 || deny == null) throw new IllegalStateException("Missing bounded action/cast gate");
        InsnList replacement = call(BOSS, "canScheduleSharedSkill");
        replacement.add(new JumpInsnNode(Opcodes.IFEQ, deny.label));
        replace(tick, previous(gate), deny, replacement);
    }

    public static void transformGoal(ClassNode type) {
        MethodNode tick = method(type, "m_8037_", "()V");
        AbstractInsnNode beginning = null, end = null;
        int stores = 0;
        for (AbstractInsnNode instruction : tick.instructions) {
            if (instruction instanceof FieldInsnNode field && field.name.equals("qingyiMeleeCooldown")) {
                if (beginning == null) beginning = previous(instruction);
                if (instruction.getOpcode() == Opcodes.PUTFIELD) { end = instruction; stores++; }
            }
        }
        if (beginning == null || stores != 1 || beginning.getOpcode() != Opcodes.ALOAD) {
            throw new IllegalStateException("Unexpected Goal cooldown body");
        }
        replace(tick, beginning, end, call(GOAL, "refreshMeleeCooldown"));
        method(type, "m_6739_", "(Lnet/minecraft/world/entity/LivingEntity;D)V").instructions.insert(call(GOAL, "refreshMeleeCooldown"));
        MethodNode start = method(type, "m_8056_", "()V");
        start.instructions.insert(call(GOAL, "refreshMeleeCooldown"));
        start.maxStack = Math.max(1, start.maxStack);
    }

    private static InsnList call(String owner, String name) {
        InsnList result = new InsnList();
        result.add(new VarInsnNode(Opcodes.ALOAD, 0));
        result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, owner, name,
                name.equals("canScheduleSharedSkill") ? "()Z" : "()V", false));
        return result;
    }

    private static AbstractInsnNode next(AbstractInsnNode instruction) {
        do { instruction = instruction.getNext(); } while (instruction != null && instruction.getOpcode() < 0);
        if (instruction == null) throw new IllegalStateException("Unexpected bytecode end");
        return instruction;
    }

    private static AbstractInsnNode previous(AbstractInsnNode instruction) {
        do { instruction = instruction.getPrevious(); } while (instruction != null && instruction.getOpcode() < 0);
        if (instruction == null) throw new IllegalStateException("Unexpected bytecode beginning");
        return instruction;
    }

    private static void replace(MethodNode method, AbstractInsnNode first, AbstractInsnNode last, InsnList instructions) {
        for (AbstractInsnNode cursor = first; cursor != last; cursor = cursor.getNext()) {
            if (cursor == null) throw new IllegalStateException("Non-contiguous edit");
            if (cursor instanceof LabelNode label) {
                for (AbstractInsnNode candidate : method.instructions) {
                    if (candidate instanceof JumpInsnNode jump && jump.label == label) {
                        throw new IllegalStateException("Cannot remove externally reachable label");
                    }
                }
            }
        }
        method.instructions.insertBefore(first, instructions);
        AbstractInsnNode stop = last.getNext();
        while (first != stop) {
            AbstractInsnNode following = first.getNext();
            method.instructions.remove(first);
            first = following;
        }
    }

    private static void mergeDonor(ClassNode type, ClassNode donor, List<String> methods, List<String> fields) {
        for (String name : methods) {
            if (type.methods.stream().anyMatch(method -> method.name.equals(name))) throw new IllegalStateException("Conflicting method: " + name);
            type.methods.add(method(donor, name, name.equals("canScheduleSharedSkill") ? "()Z" : "()V"));
        }
        for (String name : fields) {
            if (type.fields.stream().anyMatch(field -> field.name.equals(name))) throw new IllegalStateException("Conflicting clock field: " + name);
            FieldNode field = donor.fields.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
            type.fields.add(field);
        }
    }

    private static ClassNode donor(Path root, String name) throws Exception {
        ClassWriter writer = new ClassWriter(0);
        new ClassReader(Files.readAllBytes(root.resolve(name + ".class"))).accept(new ClassRemapper(writer, new Remapper() {
            @Override
            public String mapFieldName(String owner, String name, String descriptor) {
                return name.equals("entityData") ? "f_19804_" : name.equals("isClientSide") ? "f_46443_" : name;
            }
            @Override
            public String mapMethodName(String owner, String name, String descriptor) {
                if (name.equals("level")) return "m_9236_";
                if (name.equals("isAlive")) return "m_6084_";
                if (name.equals("getGameTime")) return "m_46467_";
                if (owner.equals("net/minecraft/network/syncher/SynchedEntityData") && name.equals("get")) return "m_135370_";
                if (owner.equals("net/minecraft/nbt/CompoundTag")) return switch (name) {
                    case "contains" -> "m_128441_";
                    case "getLong" -> "m_128454_";
                    case "getInt" -> "m_128451_";
                    default -> name;
                };
                return name;
            }
        }), 0);
        ClassNode result = new ClassNode();
        new ClassReader(writer.toByteArray()).accept(result, 0);
        return result;
    }

    public static ClassNode read(ZipFile archive, String name) throws Exception {
        ClassNode result = new ClassNode();
        try (var stream = archive.getInputStream(archive.getEntry(name + ".class"))) { new ClassReader(stream.readAllBytes()).accept(result, 0); }
        return result;
    }

    public static MethodNode method(ClassNode type, String name, String descriptor) {
        var matches = type.methods.stream().filter(method -> method.name.equals(name) && method.desc.equals(descriptor)).toList();
        if (matches.size() != 1) throw new IllegalStateException("Missing or ambiguous method: " + type.name + "." + name);
        return matches.get(0);
    }

    private static void save(ClassNode type, Path destination) throws Exception {
        for (MethodNode method : type.methods) {
            if ((method.access & (Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) == 0) new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
        }
        ClassWriter writer = new ClassWriter(0);
        type.accept(writer);
        Files.createDirectories(destination.getParent());
        Files.write(destination, writer.toByteArray());
    }
}
