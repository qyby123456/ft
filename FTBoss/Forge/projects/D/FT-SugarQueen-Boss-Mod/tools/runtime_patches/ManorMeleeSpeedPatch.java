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
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class ManorMeleeSpeedPatch {
    public static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    public static final String GOAL = BOSS + "$PlayerReachMeleeGoal";
    public static final String RULES = "net/farmingtales/ftboss/util/ManorMeleeTiming";

    public static void main(String[] arguments) throws Exception {
        Path compiled = Path.of(arguments[1]), payload = Path.of(arguments[2]);
        ClassWriter mapped = new ClassWriter(0);
        new ClassReader(Files.readAllBytes(compiled.resolve(BOSS + ".class")))
                .accept(new ClassRemapper(mapped, runtimeNames()), 0);
        ClassNode donor = new ClassNode();
        new ClassReader(mapped.toByteArray()).accept(donor, 0);
        try (ZipFile archive = new ZipFile(arguments[0])) {
            ClassNode boss = read(archive, BOSS), goal = read(archive, GOAL);
            boolean installed = boss.methods.stream().anyMatch(method -> method.name.equals("playMeleeImpactAction"));
            if (!installed) {
                patchHit(method(boss, "m_7327_", "(Lnet/minecraft/world/entity/Entity;)Z"));
                for (String name : List.of("getMeleeAttackInterval", "playMeleeImpactAction")) {
                    if (boss.methods.stream().anyMatch(method -> method.name.equals(name))) {
                        throw new IllegalStateException("Conflicting melee API: " + name);
                    }
                    boss.methods.add(method(donor, name, name.startsWith("get") ? "()I" : "()V"));
                }
                MethodNode interval = method(goal, "m_25566_", "()I");
                for (AbstractInsnNode instruction : interval.instructions) {
                    if (instruction instanceof MethodInsnNode call && !List.of("allowsParallelMelee", "m_183277_",
                            "getVariant", "meleeInterval", "m_25566_", "getChallengeDifficulty", "acceleratedTicks").contains(call.name)) {
                        throw new IllegalStateException("Unknown interval hook must be merged explicitly: " + call.name);
                    }
                }
                interval.instructions.clear();
                interval.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                interval.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, GOAL, "boss", "L" + BOSS + ";"));
                interval.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "getMeleeAttackInterval", "()I", false));
                interval.instructions.add(new InsnNode(Opcodes.IRETURN));
                interval.tryCatchBlocks.clear();
                interval.localVariables = null;
                interval.maxStack = 1;
                interval.maxLocals = 1;
            }
            save(boss, payload.resolve(BOSS + ".class"));
            save(goal, payload.resolve(GOAL + ".class"));
        }
        Path rules = payload.resolve(RULES + ".class");
        Files.createDirectories(rules.getParent());
        Files.write(rules, Files.readAllBytes(compiled.resolve(RULES + ".class")));
        System.out.println("PASS scoped melee speed patch: " + (Files.size(rules)) + " bytes of timing rules");
    }

    private static void patchHit(MethodNode hit) {
        AbstractInsnNode beginning = null, ending = null;
        int fields = 0, impacts = 0;
        for (AbstractInsnNode instruction : hit.instructions) {
            if (instruction instanceof FieldInsnNode field && field.owner.equals(BOSS) && field.name.equals("actionTicks")) {
                fields++;
                beginning = field.getPrevious();
            }
            if (instruction instanceof MethodInsnNode call && call.owner.equals(BOSS) && call.name.equals("playImpactAction")) {
                impacts++;
                ending = instruction;
            }
        }
        if (fields != 1 || impacts != 1 || beginning == null || beginning.getOpcode() != Opcodes.ALOAD) {
            throw new IllegalStateException("Unexpected melee animation guard");
        }
        boolean reached = false;
        for (AbstractInsnNode instruction = beginning; instruction != null; instruction = instruction.getNext()) {
            if (instruction instanceof MethodInsnNode call && !List.of("allowsParallelMelee", "isCastingLike", "playImpactAction").contains(call.name)) {
                throw new IllegalStateException("Animation guard contains another task's hook: " + call.name);
            }
            if (instruction == ending) { reached = true; break; }
        }
        if (!reached) throw new IllegalStateException("Animation guard is not contiguous");
        InsnList replacement = new InsnList();
        replacement.add(new VarInsnNode(Opcodes.ALOAD, 0));
        replacement.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "playMeleeImpactAction", "()V", false));
        hit.instructions.insertBefore(beginning, replacement);
        AbstractInsnNode after = ending.getNext();
        while (beginning != after) {
            AbstractInsnNode next = beginning.getNext();
            hit.instructions.remove(beginning);
            beginning = next;
        }
        hit.localVariables = null;
    }

    public static ClassNode read(ZipFile archive, String name) throws Exception {
        ClassNode type = new ClassNode();
        try (var stream = archive.getInputStream(archive.getEntry(name + ".class"))) {
            new ClassReader(stream.readAllBytes()).accept(type, 0);
        }
        return type;
    }

    public static MethodNode method(ClassNode type, String name, String descriptor) {
        var matches = type.methods.stream().filter(method -> method.name.equals(name) && method.desc.equals(descriptor)).toList();
        if (matches.size() != 1) throw new IllegalStateException("Missing or ambiguous method: " + type.name + "." + name);
        return matches.get(0);
    }

    private static void save(ClassNode type, Path destination) throws Exception {
        for (MethodNode method : type.methods) {
            if ((method.access & (Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) == 0) {
                new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
            }
        }
        ClassWriter writer = new ClassWriter(0);
        type.accept(writer);
        Files.createDirectories(destination.getParent());
        Files.write(destination, writer.toByteArray());
    }

    private static Remapper runtimeNames() {
        return new Remapper() {
            @Override
            public String mapFieldName(String owner, String name, String descriptor) {
                return name.equals("entityData") ? "f_19804_" : name;
            }

            @Override
            public String mapMethodName(String owner, String name, String descriptor) {
                if (owner.equals("net/minecraft/network/syncher/SynchedEntityData")) {
                    return name.equals("get") ? "m_135370_" : name.equals("set") ? "m_135381_" : name;
                }
                if (owner.equals("net/minecraft/nbt/CompoundTag")) {
                    return switch (name) {
                        case "getBoolean" -> "m_128471_";
                        case "putBoolean" -> "m_128379_";
                        case "copy" -> "m_6426_";
                        default -> name;
                    };
                }
                return name;
            }
        };
    }
}
