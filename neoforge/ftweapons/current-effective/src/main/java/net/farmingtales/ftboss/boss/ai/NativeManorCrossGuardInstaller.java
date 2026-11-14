package net.farmingtales.ftboss.boss.ai;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeManorCrossGuardInstaller {
    private static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String GUARD = "net/farmingtales/ftboss/skill/qizaicrossguard/CrossGuard";
    private static final String CONTINUITY = "net/farmingtales/ftboss/boss/ai/ManorCombatContinuity";
    private static final String OWNER = "L" + BOSS + ";";
    private static final String MOVE = "(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V";
    private static final Map<String, String> EXPECTED = Map.of("tick", "090110aacfca6c176d6cb45d63ecbf36b46812e36a17e50e825d33e5bb6944b7", "setQizaiCue", "44953146316f155b219de559a90168e4134c9c19df491a8df07412f1f5b5779f");

    private NativeManorCrossGuardInstaller() {
    }

    public static int install(ClassNode target) {
        requireTarget(target);
        if (target.methods.stream().anyMatch(member -> member.name.equals("move") && member.desc.equals(MOVE))) {
            verifyInstalled(target);
            return 0;
        }
        for (MethodNode member : selected(target)) if (!fingerprint(member).equals(EXPECTED.get(member.name))) throw new IllegalStateException("Changed full native CrossGuard preimage: " + member.name);
        MethodNode tick = method(target, "tick", "()V");
        List<MethodInsnNode> repair = code(tick).stream().filter(instruction -> instruction instanceof MethodInsnNode invocation && invocation.owner.equals(CONTINUITY) && invocation.name.equals("repair") && invocation.desc.equals("(" + OWNER + ")V")).map(instruction -> (MethodInsnNode)instruction).toList();
        if (repair.size() != 1 || !(repair.getFirst().getPrevious() instanceof VarInsnNode receiver) || receiver.getOpcode() != Opcodes.ALOAD || receiver.var != 0) throw new IllegalStateException("Original exact pre-super continuity receiver absent");
        tick.instructions.insertBefore(repair.getFirst().getPrevious(), callback("stabilize"));
        MethodNode cue = method(target, "setQizaiCue", "(Lnet/minecraft/nbt/CompoundTag;)V");
        List<AbstractInsnNode> returns = code(cue).stream().filter(instruction -> instruction.getOpcode() == Opcodes.RETURN).toList();
        if (returns.size() != 1) throw new IllegalStateException("Changed original cue return count");
        cue.instructions.insertBefore(returns.getFirst(), callback("cue"));
        target.methods.add(movement());
        verifyInstalled(target);
        return 3;
    }

    public static void verifyInstalled(ClassNode target) {
        requireTarget(target);
        if (!fingerprint(method(target, "move", MOVE)).equals(fingerprint(movement()))) throw new IllegalStateException("Changed complete original movement override");
        for (MethodNode member : selected(target)) {
            MethodNode restored = copy(member);
            String name = member.name.equals("tick") ? "stabilize" : "cue";
            List<AbstractInsnNode> instructions = code(restored);
            List<MethodInsnNode> calls = instructions.stream().filter(instruction -> instruction instanceof MethodInsnNode invocation && invocation.owner.equals(GUARD)).map(instruction -> (MethodInsnNode)instruction).toList();
            if (calls.size() != 1) throw new IllegalStateException("Missing or extra original guard hook");
            MethodInsnNode invocation = calls.getFirst();
            int index = instructions.indexOf(invocation);
            AbstractInsnNode receiver = index == 0 ? null : instructions.get(index - 1);
            if (invocation.getOpcode() != Opcodes.INVOKESTATIC || !invocation.name.equals(name) || !invocation.desc.equals("(" + OWNER + ")V") || invocation.itf || !(receiver instanceof VarInsnNode load) || load.getOpcode() != Opcodes.ALOAD || load.var != 0) throw new IllegalStateException("Changed original guard receiver or signature");
            restored.instructions.remove(receiver);
            restored.instructions.remove(invocation);
            if (!fingerprint(restored).equals(EXPECTED.get(member.name))) throw new IllegalStateException("Changed full native method beyond original guard insertion");
            List<AbstractInsnNode> current = code(member);
            int currentIndex = current.indexOf(code(member).stream().filter(instruction -> instruction instanceof MethodInsnNode call && call.owner.equals(GUARD)).findFirst().orElseThrow());
            if (name.equals("cue") && current.get(currentIndex + 1).getOpcode() != Opcodes.RETURN) throw new IllegalStateException("Moved original cue hook");
            if (name.equals("stabilize") && (!(current.get(currentIndex + 1) instanceof VarInsnNode nextReceiver) || nextReceiver.getOpcode() != Opcodes.ALOAD || nextReceiver.var != 0 || !(current.get(currentIndex + 2) instanceof MethodInsnNode nextCall) || !nextCall.owner.equals(CONTINUITY) || !nextCall.name.equals("repair"))) throw new IllegalStateException("Moved original pre-continuity stabilization");
        }
    }

    private static InsnList callback(String name) { InsnList result = new InsnList(); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new MethodInsnNode(Opcodes.INVOKESTATIC, GUARD, name, "(" + OWNER + ")V", false)); return result; }
    private static MethodNode movement() { MethodNode result = new MethodNode(Opcodes.ACC_PUBLIC, "move", MOVE, null, null); result.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1)); result.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1)); result.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2)); result.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, GUARD, "movement", "(" + OWNER + "Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;", false)); result.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/world/entity/monster/Monster", "move", MOVE, false)); result.instructions.add(new InsnNode(Opcodes.RETURN)); result.maxStack = 5; result.maxLocals = 3; return result; }
    private static MethodNode copy(MethodNode source) { MethodNode result = new MethodNode(source.access, source.name, source.desc, source.signature, source.exceptions.toArray(String[]::new)); source.accept(result); return result; }
    private static List<MethodNode> selected(ClassNode target) { return List.of(method(target, "tick", "()V"), method(target, "setQizaiCue", "(Lnet/minecraft/nbt/CompoundTag;)V")); }
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
    private static void requireTarget(ClassNode target) { if (target == null || !target.name.equals(BOSS)) throw new IllegalStateException("Unsupported native guard owner"); }
}
