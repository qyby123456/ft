import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class RatSwarmCyclePatch {
    private static final String ROOT = "net/farmingtales/ftboss/";
    private static final String RAT = ROOT + "entity/RatBossEntity";
    private static final String HELPER = ROOT + "boss/ai/RatSwarmEncounter";
    private static final String ENTITY = "Lnet/minecraft/world/entity/Entity;";
    private static final String LIVING = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final String RAT_DESC = "L" + RAT + ";";
    private static final String HURT_EVENT = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;";
    private static final Map<String, String> changes = new LinkedHashMap<>();

    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[2]);
        try (JarFile baseline = new JarFile(arguments[0]); JarFile donor = new JarFile(arguments[1])) {
            ClassNode rat = read(baseline, RAT);
            MethodNode tick = method(rat, "m_8119_", "()V");
            AbstractInsnNode superTick = null;
            for (AbstractInsnNode instruction : tick.instructions) {
                if (instruction instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESPECIAL
                        && call.name.equals("m_8119_") && call.desc.equals("()V")) superTick = instruction;
            }
            if (superTick == null) throw new IllegalStateException("No vanilla tick entry");
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
            hook.add(call("tick", "(" + RAT_DESC + ")Z"));
            returnIfTrue(hook, Opcodes.RETURN, false);
            tick.instructions.insert(superTick, hook);
            MethodNode hurt = method(rat, "m_6469_", "(Lnet/minecraft/world/damagesource/DamageSource;F)Z");
            hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
            hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
            hook.add(new VarInsnNode(Opcodes.FLOAD, 2));
            hook.add(call("blocksDamage", "(" + RAT_DESC + "Lnet/minecraft/world/damagesource/DamageSource;F)Z"));
            returnIfTrue(hook, Opcodes.IRETURN, false);
            hurt.instructions.insert(hook);
            MethodNode melee = method(rat, "m_7327_", "(" + ENTITY + ")Z");
            hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
            hook.add(call("isMaster", "(" + RAT_DESC + ")Z"));
            returnIfTrue(hook, Opcodes.IRETURN, false);
            melee.instructions.insert(hook);
            for (String name : new String[]{"m_6667_", "m_142687_"}) {
                MethodNode lifecycle = unique(rat, name);
                hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                if (name.equals("m_142687_")) hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                hook.add(call(name.equals("m_142687_") ? "cleanupRemoved" : "cleanup", "(" + RAT_DESC
                        + (name.equals("m_142687_") ? "Lnet/minecraft/world/entity/Entity$RemovalReason;" : "") + ")V"));
                lifecycle.instructions.insert(hook);
                for (AbstractInsnNode instruction : lifecycle.instructions) {
                    if (instruction instanceof MethodInsnNode invocation && invocation.owner.equals(ROOT + "boss/ai/SummonSweep")
                            && invocation.name.equals("belongsTo")) {
                        invocation.owner = HELPER;
                        invocation.name = "legacyBelongsTo";
                    }
                }
            }
            ClassNode ratDonor = read(donor, RAT);
            if (rat.methods.stream().anyMatch(entry -> entry.name.equals("tickEncounterMelee"))) throw new IllegalStateException("Already patched");
            rat.methods.add(method(ratDonor, "tickEncounterMelee", "()V"));
            write(output, rat);
            ClassNode handler = read(baseline, ROOT + "boss/BossHandler");
            prefix(handler, "onRatSwarmHurt", 1, "skipSharedHealth", HURT_EVENT, Opcodes.RETURN);
            prefix(handler, "onBossHitPlayer", 1, "handlePlayerHit", HURT_EVENT, Opcodes.RETURN);
            write(output, handler);
            ClassNode guard = read(baseline, ROOT + "boss/ai/FinalBossDamageGuard");
            hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
            hook.add(new org.objectweb.asm.tree.TypeInsnNode(Opcodes.INSTANCEOF, RAT));
            returnIfTrue(hook, Opcodes.IRETURN, false);
            method(guard, "applies", "(" + LIVING + ")Z").instructions.insert(hook);
            write(output, guard);
            ClassNode empower = read(baseline, ROOT + "boss/ai/BossEmpower");
            prefix(empower, "tick", 0, "skipEmpower", LIVING, Opcodes.RETURN);
            write(output, empower);
            ClassNode advancements = read(baseline, ROOT + "boss/BossAdvancements");
            prefix(advancements, "bossId", 0, "skipVictory", ENTITY, Opcodes.ARETURN);
            write(output, advancements);
            ClassNode bait = read(baseline, ROOT + "item/RatSwarmBaitItem");
            MethodNode use = unique(bait, "m_7203_");
            int prepares = 0;
            for (AbstractInsnNode instruction : use.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode invoke && invoke.owner.equals(RAT) && invoke.name.equals("m_21530_")) {
                    AbstractInsnNode previous = instruction.getPrevious();
                    while (previous.getOpcode() < 0) previous = previous.getPrevious();
                    if (!(previous instanceof VarInsnNode variable) || variable.getOpcode() != Opcodes.ALOAD) {
                        throw new IllegalStateException("Unexpected summon persistence receiver");
                    }
                    hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, variable.var));
                    hook.add(call("prepare", "(" + RAT_DESC + ")V"));
                    use.instructions.insert(instruction, hook);
                    prepares++;
                }
            }
            if (prepares != 1) throw new IllegalStateException("Missing summon prepare insertion: " + prepares);
            MethodNode tooltip = unique(bait, "m_7373_");
            for (AbstractInsnNode instruction : tooltip.instructions.toArray()) {
                if (instruction.getOpcode() == Opcodes.RETURN) {
                    hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 3));
                    hook.add(call("appendTooltip", "(Ljava/util/List;)V"));
                    tooltip.instructions.insertBefore(instruction, hook);
                }
            }
            write(output, bait);
            for (String name : new String[]{HELPER, ROOT + "boss/ai/RatSwarmRules"}) {
                if (baseline.getJarEntry(name + ".class") != null) throw new IllegalStateException("New class conflict: " + name);
                write(output, read(donor, name));
            }
        }
        Files.writeString(Path.of(arguments[3]), changes.toString() + "\n");
        System.out.println("PASS: scoped prefix patch, runtime-only methods and assets preserved");
    }

    private static void prefix(ClassNode owner, String methodName, int local, String helper, String argument, int result) {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, local));
        instructions.add(call(helper, "(" + argument + ")Z"));
        returnIfTrue(instructions, result, false);
        unique(owner, methodName).instructions.insert(instructions);
    }

    private static void returnIfTrue(InsnList instructions, int result, boolean value) {
        LabelNode proceed = new LabelNode();
        instructions.add(new JumpInsnNode(Opcodes.IFEQ, proceed));
        if (result == Opcodes.IRETURN) instructions.add(new InsnNode(value ? Opcodes.ICONST_1 : Opcodes.ICONST_0));
        if (result == Opcodes.ARETURN) instructions.add(new InsnNode(Opcodes.ACONST_NULL));
        instructions.add(new InsnNode(result));
        instructions.add(proceed);
        instructions.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
    }

    private static MethodInsnNode call(String name, String descriptor) {
        return new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, name, descriptor, false);
    }

    private static MethodNode unique(ClassNode owner, String name) {
        var methods = owner.methods.stream().filter(entry -> entry.name.equals(name)).toList();
        if (methods.size() != 1) throw new IllegalStateException("Ambiguous method: " + owner.name + ":" + name);
        return methods.get(0);
    }

    private static MethodNode method(ClassNode owner, String name, String descriptor) {
        return owner.methods.stream().filter(entry -> entry.name.equals(name) && entry.desc.equals(descriptor))
                .findFirst().orElseThrow(() -> new IllegalStateException("Missing method: " + owner.name + ":" + name));
    }

    private static ClassNode read(JarFile jar, String name) throws Exception {
        var entry = jar.getJarEntry(name + ".class");
        if (entry == null) throw new IllegalStateException("Missing class: " + name);
        ClassNode owner = new ClassNode();
        new ClassReader(jar.getInputStream(entry).readAllBytes()).accept(owner, 0);
        return owner;
    }

    private static void write(Path output, ClassNode owner) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        owner.accept(writer);
        Path target = output.resolve(owner.name + ".class");
        Files.createDirectories(target.getParent());
        Files.write(target, writer.toByteArray());
        changes.put(owner.name, "prefix/new-method only");
    }
}
