package net.farmingtales.ftboss.boss.ai;

import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeManorLifecycleInstaller {
    private static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String ACCESS = "net/farmingtales/ftboss/boss/ai/ManorEntryBuffAccess";

    private NativeManorLifecycleInstaller() {
    }

    public static int install(ClassNode target) {
        requireTarget(target);
        if (target.interfaces.contains(ACCESS) || target.methods.stream().anyMatch(member -> member.name.equals("prepareManorEntryBuffs"))) {
            verifyInstalled(target);
            return 0;
        }
        sync(target);
        target.interfaces.add(ACCESS);
        MethodNode wrapper = new MethodNode(Opcodes.ACC_PUBLIC, "prepareManorEntryBuffs", "()V", null, null);
        wrapper.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        wrapper.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, BOSS, "syncPlayerBuffs", "()V", false));
        wrapper.instructions.add(new InsnNode(Opcodes.RETURN));
        wrapper.maxLocals = 1;
        wrapper.maxStack = 1;
        target.methods.add(wrapper);
        verifyInstalled(target);
        return 1;
    }

    public static void verifyInstalled(ClassNode target) {
        requireTarget(target);
        sync(target);
        if (target.interfaces.stream().filter(ACCESS::equals).count() != 1) throw new IllegalStateException("Missing unique original manor entry access");
        List<MethodNode> wrappers = target.methods.stream().filter(member -> member.name.equals("prepareManorEntryBuffs")).toList();
        if (wrappers.size() != 1) throw new IllegalStateException("Missing unique full original manor entry method");
        MethodNode wrapper = wrappers.getFirst();
        List<AbstractInsnNode> instructions = Arrays.stream(wrapper.instructions.toArray()).filter(instruction -> instruction.getOpcode() >= 0).toList();
        if (wrapper.access != Opcodes.ACC_PUBLIC || !wrapper.desc.equals("()V") || !wrapper.tryCatchBlocks.isEmpty() || instructions.size() != 3
            || !(instructions.get(0) instanceof VarInsnNode receiver) || receiver.getOpcode() != Opcodes.ALOAD || receiver.var != 0
            || !(instructions.get(1) instanceof MethodInsnNode invocation) || invocation.getOpcode() != Opcodes.INVOKESPECIAL || invocation.itf
            || !invocation.owner.equals(BOSS) || !invocation.name.equals("syncPlayerBuffs") || !invocation.desc.equals("()V")
            || instructions.get(2).getOpcode() != Opcodes.RETURN) throw new IllegalStateException("Changed complete original manor entry delegation");
    }

    private static void sync(ClassNode target) {
        List<MethodNode> methods = target.methods.stream().filter(member -> member.name.equals("syncPlayerBuffs") && member.desc.equals("()V")).toList();
        if (methods.size() != 1 || methods.getFirst().access != Opcodes.ACC_PRIVATE || methods.getFirst().instructions.size() == 0) throw new IllegalStateException("Missing genuine native player buff synchronization");
    }

    private static void requireTarget(ClassNode target) {
        if (target == null || !target.name.equals(BOSS)) throw new IllegalStateException("Unsupported manor lifecycle target");
    }
}
