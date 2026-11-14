package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class NativeDamagePhaseInstaller {
    private static final String CONTAINER = "net/neoforged/neoforge/common/damagesource/DamageContainer";
    private static final String BRIDGE = "net/farmingtales/ftboss/event/NativeWeaponDamagePhases";
    private static final String ENTITY = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final String DAMAGE = "(Lnet/minecraft/world/damagesource/DamageSource;F)V";
    private static final String HURT = "(" + ENTITY + "L" + CONTAINER + ";)Z";
    private static final String HEALTH = "(L" + CONTAINER + ";" + ENTITY + ")F";

    private NativeDamagePhaseInstaller() {
    }

    public static int install(ClassNode target) {
        MethodNode method = actualMethod(target);
        List<AbstractInsnNode> code = instructions(method);
        long bridgeCalls = code.stream().filter(instruction -> instruction instanceof MethodInsnNode call && call.owner.equals(BRIDGE)).count();
        if (bridgeCalls != 0) {
            verifyInstalled(target);
            return 0;
        }
        Sites sites = sites(target, method, code);
        InsnList before = new InsnList();
        LabelNode proceed = new LabelNode();
        before.add(new VarInsnNode(Opcodes.ALOAD, 0));
        before.add(new VarInsnNode(Opcodes.ALOAD, 0));
        before.add(new FieldInsnNode(Opcodes.GETFIELD, sites.stack().owner, sites.stack().name, sites.stack().desc));
        before.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/util/Stack", "peek", "()Ljava/lang/Object;", false));
        before.add(new TypeInsnNode(Opcodes.CHECKCAST, CONTAINER));
        before.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "beforeMitigation", HURT, false));
        before.add(new JumpInsnNode(Opcodes.IFEQ, proceed));
        before.add(new InsnNode(Opcodes.RETURN));
        before.add(proceed);
        boolean expandedFrames = false;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof FrameNode frame && frame.type == Opcodes.F_NEW) expandedFrames = true;
        }
        before.add(expandedFrames
            ? new FrameNode(Opcodes.F_NEW, 3, new Object[]{target.name, "net/minecraft/world/damagesource/DamageSource", Opcodes.FLOAT}, 0, new Object[0])
            : new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insertBefore(sites.start(), before);
        method.instructions.insertBefore(sites.healthRead(), new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.set(sites.healthRead(), new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "beforeHealth", HEALTH, false));
        method.maxStack = Math.max(method.maxStack, 4);
        verifyInstalled(target);
        return 2;
    }

    public static void verifyInstalled(ClassNode target) {
        MethodNode method = actualMethod(target);
        List<AbstractInsnNode> code = instructions(method);
        MethodInsnNode hurt = unique(code, BRIDGE, "beforeMitigation", HURT);
        MethodInsnNode health = unique(code, BRIDGE, "beforeHealth", HEALTH);
        MethodInsnNode armor = unique(code, null, "getDamageAfterArmorAbsorb", "(Lnet/minecraft/world/damagesource/DamageSource;F)F");
        MethodInsnNode magic = unique(code, null, "getDamageAfterMagicAbsorb", "(Lnet/minecraft/world/damagesource/DamageSource;F)F");
        MethodInsnNode pre = unique(code, "net/neoforged/neoforge/common/CommonHooks", "onLivingDamagePre", "(" + ENTITY + "L" + CONTAINER + ";)F");
        MethodInsnNode absorption = unique(code, null, "setAbsorptionAmount", "(F)V");
        MethodInsnNode healthWrite = unique(code, null, "setHealth", "(F)V");
        MethodInsnNode post = unique(code, "net/neoforged/neoforge/common/CommonHooks", "onLivingDamagePost", "(" + ENTITY + "L" + CONTAINER + ";)V");
        requireOrder(code, hurt, armor, magic, pre, absorption, health, healthWrite, post);
        int index = code.indexOf(hurt);
        if (index < 5 || !(code.get(index - 5) instanceof VarInsnNode actor) || actor.getOpcode() != Opcodes.ALOAD || actor.var != 0
            || !(code.get(index - 4) instanceof VarInsnNode owner) || owner.getOpcode() != Opcodes.ALOAD || owner.var != 0
            || !(code.get(index - 3) instanceof FieldInsnNode stack) || !stack.name.equals("damageContainers") || !stack.desc.equals("Ljava/util/Stack;")
            || !(code.get(index - 2) instanceof MethodInsnNode peek) || !peek.owner.equals("java/util/Stack") || !peek.name.equals("peek")
            || !(code.get(index - 1) instanceof TypeInsnNode cast) || !cast.desc.equals(CONTAINER)
            || !(code.get(index + 1) instanceof JumpInsnNode exit) || exit.getOpcode() != Opcodes.IFEQ
            || code.get(index + 2).getOpcode() != Opcodes.RETURN) {
            throw new IllegalStateException("Missing original pre-mitigation container/return boundary");
        }
        AbstractInsnNode continuation = exit.label;
        while (continuation != null && continuation.getOpcode() < 0) continuation = continuation.getNext();
        if (continuation != code.get(index + 3)) throw new IllegalStateException("Changed pre-mitigation continuation");
        int healthIndex = code.indexOf(health);
        if (!(code.get(healthIndex - 1) instanceof VarInsnNode receiver) || receiver.getOpcode() != Opcodes.ALOAD || receiver.var != 0
            || !finalContainer(target, code, healthIndex - 1)
            || !(code.get(healthIndex + 1) instanceof VarInsnNode result) || result.getOpcode() != Opcodes.FSTORE) {
            throw new IllegalStateException("Missing actual post-absorption damage local boundary");
        }
        long calls = code.stream().filter(instruction -> instruction instanceof MethodInsnNode call && call.owner.equals(BRIDGE)).count();
        if (calls != 2) throw new IllegalStateException("Unexpected additional phase bridge calls");
    }

    private static Sites sites(ClassNode target, MethodNode method, List<AbstractInsnNode> code) {
        MethodInsnNode armor = unique(code, null, "getDamageAfterArmorAbsorb", "(Lnet/minecraft/world/damagesource/DamageSource;F)F");
        MethodInsnNode magic = unique(code, null, "getDamageAfterMagicAbsorb", "(Lnet/minecraft/world/damagesource/DamageSource;F)F");
        MethodInsnNode pre = unique(code, "net/neoforged/neoforge/common/CommonHooks", "onLivingDamagePre", "(" + ENTITY + "L" + CONTAINER + ";)F");
        MethodInsnNode absorption = unique(code, null, "setAbsorptionAmount", "(F)V");
        MethodInsnNode healthWrite = unique(code, null, "setHealth", "(F)V");
        MethodInsnNode post = unique(code, "net/neoforged/neoforge/common/CommonHooks", "onLivingDamagePost", "(" + ENTITY + "L" + CONTAINER + ";)V");
        requireOrder(code, armor, magic, pre, absorption, healthWrite, post);
        if (code.size() < 18 || !(code.get(0) instanceof VarInsnNode first) || first.getOpcode() != Opcodes.ALOAD || first.var != 0
            || !(code.get(1) instanceof VarInsnNode source) || source.getOpcode() != Opcodes.ALOAD || source.var != 1
            || !(code.get(2) instanceof MethodInsnNode invulnerable) || !invulnerable.name.equals("isInvulnerableTo") || !invulnerable.desc.equals("(Lnet/minecraft/world/damagesource/DamageSource;)Z")
            || !(code.get(3) instanceof JumpInsnNode branch) || branch.getOpcode() != Opcodes.IFNE
            || !(code.get(4) instanceof VarInsnNode start) || start.getOpcode() != Opcodes.ALOAD || start.var != 0
            || !(code.get(5) instanceof FieldInsnNode stack) || stack.getOpcode() != Opcodes.GETFIELD || !stack.name.equals("damageContainers") || !stack.desc.equals("Ljava/util/Stack;")
            || !(code.get(6) instanceof MethodInsnNode peek) || !peek.owner.equals("java/util/Stack") || !peek.name.equals("peek")) {
            throw new IllegalStateException("Unsupported actual invulnerability-to-armor entry: " + target.name);
        }
        if (!stack.owner.equals(target.name) && !stack.owner.equals("net/minecraft/world/entity/LivingEntity")) {
            throw new IllegalStateException("Unexpected native damage container field host");
        }
        for (int index = 4; index < code.indexOf(armor); index++) {
            if (code.get(index) instanceof VarInsnNode local && local.getOpcode() >= Opcodes.ISTORE && local.getOpcode() <= Opcodes.ASTORE) {
                throw new IllegalStateException("Changed initial damage locals");
            }
        }
        List<MethodInsnNode> healthReads = code.stream().filter(instruction -> instruction instanceof MethodInsnNode call
            && call.owner.equals(CONTAINER) && call.name.equals("getNewDamage") && call.desc.equals("()F")
            && code.indexOf(call) > code.indexOf(absorption) && code.indexOf(call) < code.indexOf(healthWrite))
            .map(instruction -> (MethodInsnNode)instruction).toList();
        if (healthReads.size() != 1 || !method.tryCatchBlocks.isEmpty()) throw new IllegalStateException("Ambiguous actual pre-health damage site");
        MethodInsnNode healthRead = healthReads.getFirst();
        int index = code.indexOf(healthRead);
        if (!finalContainer(target, code, index)
            || !(code.get(index + 1) instanceof VarInsnNode store) || store.getOpcode() != Opcodes.FSTORE) {
            throw new IllegalStateException("Unsupported native final damage load");
        }
        return new Sites(start, stack, healthRead);
    }

    private static boolean finalContainer(ClassNode target, List<AbstractInsnNode> code, int index) {
        int containerIndex = index - 1;
        if (code.get(containerIndex) instanceof MethodInsnNode guard) {
            String descriptor = "(Lnet/minecraft/world/damagesource/DamageSource;FLorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V";
            if (guard.getOpcode() != Opcodes.INVOKESPECIAL || !guard.owner.equals(target.name)
                || !guard.name.startsWith("handler$") || !guard.name.endsWith("$ftboss$finalDamageGuard")
                || !guard.desc.equals(descriptor) || index < 6
                || !(code.get(index - 5) instanceof VarInsnNode actor) || actor.getOpcode() != Opcodes.ALOAD || actor.var != 0
                || !(code.get(index - 4) instanceof VarInsnNode source) || source.getOpcode() != Opcodes.ALOAD || source.var != 1
                || !(code.get(index - 3) instanceof VarInsnNode damage) || damage.getOpcode() != Opcodes.FLOAD || damage.var != 2
                || code.get(index - 2).getOpcode() != Opcodes.ACONST_NULL) return false;
            List<MethodNode> handlers = new ArrayList<>();
            for (MethodNode candidate : target.methods) if (candidate.name.equals(guard.name) && candidate.desc.equals(descriptor)) handlers.add(candidate);
            if (handlers.size() != 1) return false;
            MethodNode handler = handlers.getFirst();
            boolean provenance = false;
            if (handler.visibleAnnotations != null) for (org.objectweb.asm.tree.AnnotationNode annotation : handler.visibleAnnotations) {
                if (annotation.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;") && annotation.values != null) {
                    for (int ordinal = 0; ordinal + 1 < annotation.values.size(); ordinal += 2) {
                        if (annotation.values.get(ordinal).equals("mixin") && annotation.values.get(ordinal + 1).equals("net.farmingtales.ftboss.mixin.FinalBossDamageMixin")) provenance = true;
                    }
                }
            }
            if (!provenance || (handler.access & Opcodes.ACC_PRIVATE) == 0 || (handler.access & Opcodes.ACC_STATIC) != 0) return false;
            List<AbstractInsnNode> body = instructions(handler);
            int[] opcodes = {25,180,182,192,58,25,182,56,25,25,23,184,56,23,184,23,184,159,25,23,182,177};
            if (body.size() != opcodes.length) return false;
            for (int ordinal = 0; ordinal < opcodes.length; ordinal++) if (body.get(ordinal).getOpcode() != opcodes[ordinal]) return false;
            List<String> calls = new ArrayList<>();
            for (AbstractInsnNode instruction : body) if (instruction instanceof MethodInsnNode call) calls.add(call.owner + "." + call.name + call.desc);
            if (!calls.equals(List.of("java/util/Stack.peek()Ljava/lang/Object;", CONTAINER + ".getNewDamage()F",
                "net/farmingtales/ftboss/boss/ai/FinalBossDamageGuard.settled(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)F",
                "java/lang/Float.floatToIntBits(F)I", "java/lang/Float.floatToIntBits(F)I", CONTAINER + ".setNewDamage(F)V"))) return false;
            int[] localIndexes = {0,4,4,5,0,1,5,6,6,5,4,6};
            List<VarInsnNode> locals = new ArrayList<>();
            for (AbstractInsnNode instruction : body) if (instruction instanceof VarInsnNode local) locals.add(local);
            if (locals.size() != localIndexes.length) return false;
            for (int ordinal = 0; ordinal < localIndexes.length; ordinal++) if (locals.get(ordinal).var != localIndexes[ordinal]) return false;
            if (!(body.get(1) instanceof FieldInsnNode field) || !field.owner.equals(target.name) || !field.name.equals("damageContainers") || !field.desc.equals("Ljava/util/Stack;")
                || !(body.get(3) instanceof TypeInsnNode cast) || !cast.desc.equals(CONTAINER)
                || !(body.get(17) instanceof JumpInsnNode branch)) return false;
            AbstractInsnNode destination = branch.label;
            while (destination != null && destination.getOpcode() < 0) destination = destination.getNext();
            if (destination != body.getLast() || !handler.tryCatchBlocks.isEmpty()) return false;
            containerIndex = index - 6;
        }
        return containerIndex >= 0 && code.get(containerIndex) instanceof TypeInsnNode container
            && container.getOpcode() == Opcodes.CHECKCAST && container.desc.equals(CONTAINER);
    }

    private static MethodNode actualMethod(ClassNode target) {
        if (target == null || !List.of("net/minecraft/world/entity/LivingEntity", "net/minecraft/world/entity/player/Player").contains(target.name)) {
            throw new IllegalStateException("Unsupported damage phase target");
        }
        List<MethodNode> methods = target.methods.stream().filter(method -> method.name.equals("actuallyHurt") && method.desc.equals(DAMAGE)
            && (method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0).toList();
        if (methods.size() != 1) throw new IllegalStateException("Missing unique actual damage method");
        return methods.getFirst();
    }

    private static MethodInsnNode unique(List<AbstractInsnNode> code, String owner, String name, String descriptor) {
        List<MethodInsnNode> matches = code.stream().filter(instruction -> instruction instanceof MethodInsnNode call
            && (owner == null || call.owner.equals(owner)) && call.name.equals(name) && call.desc.equals(descriptor))
            .map(instruction -> (MethodInsnNode)instruction).toList();
        if (matches.size() != 1) throw new IllegalStateException("Missing unique native phase member " + name);
        return matches.getFirst();
    }

    private static void requireOrder(List<AbstractInsnNode> code, AbstractInsnNode... expected) {
        int previous = -1;
        for (AbstractInsnNode instruction : expected) {
            int index = code.indexOf(instruction);
            if (index <= previous) throw new IllegalStateException("Changed native damage phase order");
            previous = index;
        }
    }

    private static List<AbstractInsnNode> instructions(MethodNode method) {
        List<AbstractInsnNode> code = new ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction.getOpcode() >= 0) code.add(instruction);
        }
        return code;
    }

    private record Sites(VarInsnNode start, FieldInsnNode stack, MethodInsnNode healthRead) {
    }
}
