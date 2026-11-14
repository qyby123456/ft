package net.farmingtales.ftboss.boss.ai;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeManorTurretInstaller {
    private static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String STAGE = "net/farmingtales/ftboss/turret/ZhuniStage";
    private static final String ACCESS = "net/farmingtales/ftboss/entity/ManorBalanceAccess";
    private static final String BALANCE = "net/farmingtales/ftboss/balance/ManorBalanceHooks";
    private static final String PUFFER = "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V";
    private static final Map<String,String> EXPECTED = Map.of("tick","d70e59db0146f9ad0afe7ebc6ae7df6c5fa3871d53361cf13e41375c479893d5","applyPufferDebuffs","b8f0f2a795af701659682a24358ba3e23a983d0d107ea1a3d8103c21f8bd9b43");
    private NativeManorTurretInstaller() {
    }
    public static int install(ClassNode target) {
        requireTarget(target);
        if (target.interfaces.contains(ACCESS)) { verifyInstalled(target); return 0; }
        for (MethodNode member : List.of(method(target,"tick","()V"),method(target,"applyPufferDebuffs",PUFFER))) if (!fingerprint(member).equals(EXPECTED.get(member.name))) throw new IllegalStateException("Changed complete native turret preimage: "+member.name);
        if (target.methods.stream().anyMatch(member -> member.name.equals("ftboss$applyPufferSingle"))) throw new IllegalStateException("Unexpected native balance accessor");
        MethodNode puffer = method(target,"applyPufferDebuffs",PUFFER); puffer.name = "ftboss$applyPufferSingle"; puffer.access = Opcodes.ACC_PUBLIC;
        target.methods.add(pufferBridge()); target.interfaces.add(ACCESS);
        MethodNode tick = method(target,"tick","()V"); LabelNode continued = new LabelNode(); InsnList guard = new InsnList(); guard.add(new VarInsnNode(Opcodes.ALOAD,0)); guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC,STAGE,"hold","(L"+BOSS+";)Z",false)); guard.add(new JumpInsnNode(Opcodes.IFEQ,continued)); guard.add(new InsnNode(Opcodes.RETURN)); guard.add(continued); tick.instructions.insertBefore(tick.instructions.getFirst(),guard);
        verifyInstalled(target); return 3;
    }
    public static void verifyInstalled(ClassNode target) {
        requireTarget(target);
        if (Collections.frequency(target.interfaces,ACCESS) != 1 || !fingerprint(method(target,"applyPufferDebuffs",PUFFER)).equals(fingerprint(pufferBridge()))) throw new IllegalStateException("Changed full original balance interface or dispatch wrapper");
        MethodNode puffer = copy(method(target,"ftboss$applyPufferSingle",PUFFER)); if (puffer.access != Opcodes.ACC_PUBLIC) throw new IllegalStateException("Changed public balance method access"); puffer.name = "applyPufferDebuffs"; puffer.access = Opcodes.ACC_PRIVATE;
        if (!fingerprint(puffer).equals(EXPECTED.get(puffer.name))) throw new IllegalStateException("Original full native puffer body was changed");
        MethodNode tick = copy(method(target,"tick","()V")); List<AbstractInsnNode> instructions = code(tick);
        if (!(instructions.get(0) instanceof VarInsnNode receiver) || receiver.getOpcode() != Opcodes.ALOAD || receiver.var != 0 || !(instructions.get(1) instanceof MethodInsnNode hold) || hold.getOpcode() != Opcodes.INVOKESTATIC || !hold.owner.equals(STAGE) || !hold.name.equals("hold") || !hold.desc.equals("(L"+BOSS+";)Z") || hold.itf || !(instructions.get(2) instanceof JumpInsnNode jump) || jump.getOpcode() != Opcodes.IFEQ || next(jump.label) != instructions.get(4) || instructions.get(3).getOpcode() != Opcodes.RETURN) throw new IllegalStateException("Moved or changed exact original outer Zhuni hold gate");
        for (int index=0;index<4;index++) tick.instructions.remove(instructions.get(index));
        if (!fingerprint(tick).equals(EXPECTED.get("tick"))) throw new IllegalStateException("Changed complete native tick beyond original outer stage gate");
    }
    private static MethodNode pufferBridge() { MethodNode result = new MethodNode(Opcodes.ACC_PRIVATE,"applyPufferDebuffs",PUFFER,null,null); result.instructions.add(new VarInsnNode(Opcodes.ALOAD,0)); result.instructions.add(new VarInsnNode(Opcodes.ALOAD,1)); result.instructions.add(new VarInsnNode(Opcodes.ALOAD,2)); result.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,BALANCE,"zicai","(L"+BOSS+";Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V",false)); result.instructions.add(new InsnNode(Opcodes.RETURN)); result.maxLocals=3; result.maxStack=3; return result; }
    private static MethodNode copy(MethodNode source) { MethodNode result = new MethodNode(source.access,source.name,source.desc,source.signature,source.exceptions.toArray(String[]::new)); source.accept(result); return result; }
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
    private static void requireTarget(ClassNode target) { if (target == null || !target.name.equals(BOSS)) throw new IllegalStateException("Unsupported native turret owner"); }
}
