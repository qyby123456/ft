package net.farmingtales.ftboss.boss.ai;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeManorGroundDomainInstaller {
    private static final String BOSS="net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String GROUND="net/farmingtales/ftboss/boss/ai/ManorGroundSupport";
    private static final String EXPECTED="bc30aef70fe0afefbc974b062400fe88cf53f2e53ee51abcdf5ab2409b1042c8";
    private NativeManorGroundDomainInstaller() {
    }
    public static int install(ClassNode target) {
        requireTarget(target);MethodNode tick=method(target,"tick","()V");
        if(code(tick).stream().anyMatch(instruction->instruction instanceof MethodInsnNode call&&call.owner.equals(GROUND))){verifyInstalled(target);return 0;}
        if(!fingerprint(tick).equals(EXPECTED))throw new IllegalStateException("Changed complete native domain tick preimage");
        List<AbstractInsnNode> calls=code(tick).stream().filter(instruction->instruction instanceof MethodInsnNode call&&call.getOpcode()==Opcodes.INVOKESPECIAL&&call.owner.equals("net/minecraft/world/entity/monster/Monster")&&call.name.equals("tick")&&call.desc.equals("()V")&&!call.itf).toList();
        if(calls.size()!=1)throw new IllegalStateException("Missing exact original super tick anchor");
        InsnList body=new InsnList();body.add(new VarInsnNode(Opcodes.ALOAD,0));body.add(new MethodInsnNode(Opcodes.INVOKESTATIC,GROUND,"tick","(L"+BOSS+";)V",false));tick.instructions.insert(calls.getFirst(),body);verifyInstalled(target);return 1;
    }
    public static void verifyInstalled(ClassNode target) {
        requireTarget(target);MethodNode tick=copy(method(target,"tick","()V"));List<AbstractInsnNode> instructions=code(tick);List<MethodInsnNode> calls=instructions.stream().filter(instruction->instruction instanceof MethodInsnNode call&&call.owner.equals(GROUND)).map(instruction->(MethodInsnNode)instruction).toList();
        if(calls.size()!=1)throw new IllegalStateException("Missing or duplicate complete original domain tick call");
        MethodInsnNode call=calls.getFirst();int index=instructions.indexOf(call);
        if(index!=11||call.getOpcode()!=Opcodes.INVOKESTATIC||!call.name.equals("tick")||!call.desc.equals("(L"+BOSS+";)V")||call.itf||!(instructions.get(index-1) instanceof VarInsnNode receiver)||receiver.getOpcode()!=Opcodes.ALOAD||receiver.var!=0||!(instructions.get(index-2) instanceof MethodInsnNode parent)||parent.getOpcode()!=Opcodes.INVOKESPECIAL||!parent.owner.equals("net/minecraft/world/entity/monster/Monster")||!parent.name.equals("tick")||!parent.desc.equals("()V")||parent.itf)throw new IllegalStateException("Moved or changed whole original post-super domain tick receiver/anchor");
        tick.instructions.remove(instructions.get(index-1));tick.instructions.remove(call);
        if(!fingerprint(tick).equals(EXPECTED))throw new IllegalStateException("Changed existing full native tick beyond original domain call");
    }
    private static MethodNode copy(MethodNode source){MethodNode result=new MethodNode(source.access,source.name,source.desc,source.signature,source.exceptions.toArray(String[]::new));source.accept(result);return result;}
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
    private static void requireTarget(ClassNode target){if(target==null||!target.name.equals(BOSS))throw new IllegalStateException("Unsupported native ground-domain target");}
}
