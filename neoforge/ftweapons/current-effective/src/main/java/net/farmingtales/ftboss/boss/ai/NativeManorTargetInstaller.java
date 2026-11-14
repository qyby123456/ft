package net.farmingtales.ftboss.boss.ai;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeManorTargetInstaller {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static final String BOSS = PREFIX + "entity/EasterBossEntity";
    private static final String CONTINUITY = PREFIX + "boss/ai/ManorCombatContinuity";
    private static final String FINALE = PREFIX + "boss/ai/ManorFinaleCombat";
    private static final String LIVING = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final String LEVEL = "Lnet/minecraft/server/level/ServerLevel;";
    private static final String PLAYER = "Lnet/minecraft/server/level/ServerPlayer;";
    private static final Map<String, String> EXPECTED = Map.of(
        "canTeleportWith", "568d5eb35b2387f7147d107373e8b65541d9bf3756cd465a1de0d12cc27ad822",
        "grabLaggard", "a0f2e4bd5ae7626ec6fc0ca81cb9ff547ab8fab7edc207d2ffad50a5fbdd9f24",
        "huntStrayPlayer", "cbe52929f87aff31b39ba141f96e7ca248cecc9b401b2bab10633eef2027ef06",
        "tick", "25edc882b973f2aa59ca667e5cdd9add256ecc10613586ae2e345c4df30af440"
    );

    private NativeManorTargetInstaller() {
    }

    public static int install(ClassNode target) {
        requireTarget(target);
        if (selected(target).stream().flatMap(member -> code(member).stream()).anyMatch(instruction -> instruction instanceof MethodInsnNode invocation && (invocation.owner.equals(CONTINUITY) || invocation.owner.equals(FINALE)))) {
            verifyInstalled(target);
            return 0;
        }
        for (MethodNode member : selected(target)) if (!fingerprint(member).equals(EXPECTED.get(member.name))) throw new IllegalStateException("Changed complete native target preimage: " + member.name);
        MethodNode oldTeleport = method(target, "canTeleportWith", "(" + LIVING + ")Z");
        target.methods.set(target.methods.indexOf(oldTeleport), teleport());
        for (String name : List.of("grabLaggard", "huntStrayPlayer")) {
            MethodNode member = method(target, name, "(" + LEVEL + ")V");
            List<MethodInsnNode> calls = code(member).stream().filter(instruction -> instruction instanceof MethodInsnNode invocation && invocation.owner.equals(BOSS) && invocation.name.equals("manorSummoner") && invocation.desc.equals("()" + PLAYER) && invocation.getOpcode() == Opcodes.INVOKEVIRTUAL).map(instruction -> (MethodInsnNode)instruction).toList();
            if (calls.size() != 1) throw new IllegalStateException("Missing unique full native summoner fallback: " + name);
            MethodInsnNode invocation = calls.getFirst(); invocation.setOpcode(Opcodes.INVOKESTATIC); invocation.owner = CONTINUITY; invocation.name = "fightPlayer"; invocation.desc = "(L" + BOSS + ";)" + PLAYER;
            if (name.equals("huntStrayPlayer")) member.instructions.insertBefore(member.instructions.getFirst(), huntGuard());
            else {
                List<MethodInsnNode> broadcasts = code(member).stream().filter(instruction -> instruction instanceof MethodInsnNode call && call.owner.equals("net/minecraft/world/entity/player/Player") && call.name.equals("sendSystemMessage") && call.desc.equals("(Lnet/minecraft/network/chat/Component;)V")).map(instruction -> (MethodInsnNode)instruction).toList();
                if (broadcasts.size() != 1) throw new IllegalStateException("Missing exact legacy nearby-player taunt broadcast");
                MethodInsnNode broadcast = broadcasts.getFirst(); member.instructions.insertBefore(broadcast, new InsnNode(Opcodes.POP)); member.instructions.set(broadcast, new InsnNode(Opcodes.POP));
            }
        }
        MethodNode tick = method(target, "tick", "()V");
        List<AbstractInsnNode> instructions = code(tick);
        List<MethodInsnNode> supers = instructions.stream().filter(instruction -> instruction instanceof MethodInsnNode invocation && invocation.getOpcode() == Opcodes.INVOKESPECIAL && invocation.owner.equals("net/minecraft/world/entity/monster/Monster") && invocation.name.equals("tick") && invocation.desc.equals("()V")).map(instruction -> (MethodInsnNode)instruction).toList();
        if (supers.size() != 1 || !(instructions.get(instructions.indexOf(supers.getFirst()) - 1) instanceof VarInsnNode receiver) || receiver.getOpcode() != Opcodes.ALOAD || receiver.var != 0) throw new IllegalStateException("Missing exact original native super tick entry");
        InsnList repair = new InsnList(); repair.add(new VarInsnNode(Opcodes.ALOAD, 0)); repair.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CONTINUITY, "repair", "(L" + BOSS + ";)V", false)); tick.instructions.insertBefore(receiver, repair);
        List<AbstractInsnNode> returns = code(tick).stream().filter(instruction -> instruction.getOpcode() == Opcodes.RETURN).toList();
        if (returns.size() != 9) throw new IllegalStateException("Changed complete native tick exits");
        for (AbstractInsnNode exit : returns) { InsnList callback = new InsnList(); callback.add(new VarInsnNode(Opcodes.ALOAD, 0)); callback.add(new MethodInsnNode(Opcodes.INVOKESTATIC, FINALE, "tick", "(L" + BOSS + ";)V", false)); tick.instructions.insertBefore(exit, callback); }
        for (MethodNode member : selected(target)) { for (AbstractInsnNode instruction : member.instructions.toArray()) if (instruction instanceof FrameNode) member.instructions.remove(instruction); member.maxStack += 4; }
        verifyInstalled(target);
        return 4;
    }

    public static void verifyInstalled(ClassNode target) {
        requireTarget(target);
        MethodNode teleport = method(target, "canTeleportWith", "(" + LIVING + ")Z");
        if (!fingerprint(teleport).equals(fingerprint(teleport()))) throw new IllegalStateException("Changed complete original teleport eligibility method");
        for (MethodNode member : selected(target)) if (!member.name.equals("canTeleportWith") && !fingerprint(restored(member)).equals(EXPECTED.get(member.name))) throw new IllegalStateException("Changed native target fallback or exact phase hook location: " + member.name);
    }

    private static MethodNode teleport() {
        MethodNode result = new MethodNode(Opcodes.ACC_PUBLIC, "canTeleportWith", "(" + LIVING + ")Z", null, null);
        result.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1)); result.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CONTINUITY, "canTeleport", "(L" + BOSS + ";" + LIVING + ")Z", false)); result.instructions.add(new InsnNode(Opcodes.IRETURN)); result.maxLocals = 2; result.maxStack = 2; return result;
    }

    private static InsnList huntGuard() {
        LabelNode ordinary = new LabelNode(); InsnList result = new InsnList(); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new MethodInsnNode(Opcodes.INVOKESTATIC, FINALE, "manages", "(" + LIVING + ")Z", false)); result.add(new JumpInsnNode(Opcodes.IFEQ, ordinary)); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new VarInsnNode(Opcodes.ALOAD, 1)); result.add(new MethodInsnNode(Opcodes.INVOKESTATIC, FINALE, "huntTarget", "(L" + BOSS + ";" + LEVEL + ")V", false)); result.add(new InsnNode(Opcodes.RETURN)); result.add(ordinary); return result;
    }

    private static MethodNode restored(MethodNode original) {
        MethodNode result = copy(original);
        if (original.name.equals("huntStrayPlayer")) {
            List<AbstractInsnNode> instructions = code(result), expected = Arrays.stream(huntGuard().toArray()).filter(instruction -> instruction.getOpcode() >= 0).toList();
            for (int index = 0; index < expected.size(); index++) if (!operand(instructions.get(index)).equals(operand(expected.get(index)))) throw new IllegalStateException("Missing exact original finale hunt prefix");
            if (next(((JumpInsnNode)instructions.get(2)).label) != instructions.get(7)) throw new IllegalStateException("Original finale hunt bypass moved");
            for (int index = 0; index < expected.size(); index++) result.instructions.remove(instructions.get(index));
        }
        if (original.name.equals("grabLaggard")) {
            List<AbstractInsnNode> instructions = code(result); int restored = 0;
            for (int index = 2; index < instructions.size(); index++) if (instructions.get(index).getOpcode() == Opcodes.POP && instructions.get(index - 1).getOpcode() == Opcodes.POP && instructions.get(index - 2) instanceof MethodInsnNode literal && literal.getOpcode() == Opcodes.INVOKESTATIC && literal.owner.equals("net/minecraft/network/chat/Component") && literal.name.equals("literal") && literal.desc.equals("(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")) {
                result.instructions.set(instructions.get(index - 1), new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/world/entity/player/Player", "sendSystemMessage", "(Lnet/minecraft/network/chat/Component;)V", false)); result.instructions.remove(instructions.get(index)); restored++;
            }
            if (restored != 1) throw new IllegalStateException("Changed complete original nearby-player quiet broadcast block");
        }
        int fallbacks = 0, callbacks = 0, repairs = 0;
        for (AbstractInsnNode instruction : code(result)) if (instruction instanceof MethodInsnNode invocation) {
            List<AbstractInsnNode> instructions = code(result); int position = instructions.indexOf(invocation);
            if (invocation.owner.equals(CONTINUITY) && invocation.name.equals("fightPlayer") && invocation.desc.equals("(L" + BOSS + ";)" + PLAYER) && invocation.getOpcode() == Opcodes.INVOKESTATIC && !invocation.itf) {
                invocation.setOpcode(Opcodes.INVOKEVIRTUAL); invocation.owner = BOSS; invocation.name = "manorSummoner"; invocation.desc = "()" + PLAYER; fallbacks++;
            } else if (original.name.equals("tick") && invocation.owner.equals(FINALE) && invocation.name.equals("tick") && invocation.desc.equals("(L" + BOSS + ";)V") && invocation.getOpcode() == Opcodes.INVOKESTATIC && !invocation.itf) {
                if (!(instructions.get(position - 1) instanceof VarInsnNode receiver) || receiver.getOpcode() != Opcodes.ALOAD || receiver.var != 0 || instructions.get(position + 1).getOpcode() != Opcodes.RETURN) throw new IllegalStateException("Original finale callback moved from native exit"); result.instructions.remove(instructions.get(position - 1)); result.instructions.remove(invocation); callbacks++;
            } else if (original.name.equals("tick") && invocation.owner.equals(CONTINUITY) && invocation.name.equals("repair") && invocation.desc.equals("(L" + BOSS + ";)V") && invocation.getOpcode() == Opcodes.INVOKESTATIC && !invocation.itf) {
                if (!(instructions.get(position - 1) instanceof VarInsnNode receiver) || receiver.getOpcode() != Opcodes.ALOAD || receiver.var != 0 || !(instructions.get(position + 1) instanceof VarInsnNode nextReceiver) || nextReceiver.getOpcode() != Opcodes.ALOAD || nextReceiver.var != 0 || !(instructions.get(position + 2) instanceof MethodInsnNode superCall) || superCall.getOpcode() != Opcodes.INVOKESPECIAL || !superCall.owner.equals("net/minecraft/world/entity/monster/Monster") || !superCall.name.equals("tick")) throw new IllegalStateException("Original combat repair moved from exact pre-super entry"); result.instructions.remove(instructions.get(position - 1)); result.instructions.remove(invocation); repairs++;
            }
        }
        if (original.name.equals("tick") ? fallbacks != 0 || callbacks != 9 || repairs != 1 : fallbacks != 1 || callbacks != 0 || repairs != 0) throw new IllegalStateException("Partial complete native target entries");
        return result;
    }

    private static MethodNode copy(MethodNode source) { MethodNode result = new MethodNode(source.access, source.name, source.desc, source.signature, source.exceptions.toArray(String[]::new)); source.accept(result); return result; }
    private static List<MethodNode> selected(ClassNode target) { return List.of(method(target, "canTeleportWith", "(" + LIVING + ")Z"), method(target, "grabLaggard", "(" + LEVEL + ")V"), method(target, "huntStrayPlayer", "(" + LEVEL + ")V"), method(target, "tick", "()V")); }
    private static String operand(AbstractInsnNode instruction) {
        String result = Integer.toString(instruction.getOpcode());
        if (instruction instanceof VarInsnNode variable) return result + ':' + variable.var;
        if (instruction instanceof FieldInsnNode field) return result + ':' + field.owner + ':' + field.name + ':' + field.desc;
        if (instruction instanceof MethodInsnNode invocation) return result + ':' + invocation.owner + ':' + invocation.name + ':' + invocation.desc + ':' + invocation.itf;
        if (instruction instanceof TypeInsnNode type) return result + ':' + type.desc;
        if (instruction instanceof LdcInsnNode constant) return result + ':' + constant.cst.getClass().getName() + ':' + constant.cst;
        if (instruction instanceof IntInsnNode integer) return result + ':' + integer.operand;
        if (instruction instanceof IincInsnNode increment) return result + ':' + increment.var + ':' + increment.incr;
        if (instruction instanceof InsnNode || instruction instanceof JumpInsnNode) return result;
        if (instruction instanceof InvokeDynamicInsnNode dynamic) return result + ':' + dynamic.name + ':' + dynamic.desc + ':' + dynamic.bsm + ':' + Arrays.toString(dynamic.bsmArgs);
        throw new IllegalStateException("Unreviewed complete native target instruction shape");
    }
    private static String fingerprint(MethodNode member) {
        List<AbstractInsnNode> instructions = code(member); StringBuilder result = new StringBuilder(member.name).append(member.desc).append(':').append(member.access).append('\n');
        for (AbstractInsnNode instruction : instructions) { result.append(operand(instruction)); if (instruction instanceof JumpInsnNode jump) { int destination = instructions.indexOf(next(jump.label)); if (destination < 0) throw new IllegalStateException("Native target branch escapes full method"); result.append(':').append(destination); } result.append('\n'); }
        for (TryCatchBlockNode handler : member.tryCatchBlocks) result.append("handler:").append(instructions.indexOf(next(handler.start))).append(':').append(instructions.indexOf(next(handler.end))).append(':').append(instructions.indexOf(next(handler.handler))).append(':').append(handler.type).append('\n');
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(result.toString().getBytes(StandardCharsets.UTF_8))); } catch (java.security.NoSuchAlgorithmException failure) { throw new IllegalStateException(failure); }
    }
    private static List<AbstractInsnNode> code(MethodNode method) { return Arrays.stream(method.instructions.toArray()).filter(instruction -> instruction.getOpcode() >= 0).toList(); }
    private static AbstractInsnNode next(AbstractInsnNode instruction) { while (instruction != null && instruction.getOpcode() < 0) instruction = instruction.getNext(); return instruction; }
    private static MethodNode method(ClassNode target, String name, String descriptor) { List<MethodNode> methods = target.methods.stream().filter(member -> member.name.equals(name) && member.desc.equals(descriptor) && (member.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0).toList(); if (methods.size() != 1) throw new IllegalStateException("Missing actual native basic method: " + name); return methods.getFirst(); }
    private static void requireTarget(ClassNode target) { if (target == null || !target.name.equals(BOSS)) throw new IllegalStateException("Unsupported native target continuity owner"); }
}
