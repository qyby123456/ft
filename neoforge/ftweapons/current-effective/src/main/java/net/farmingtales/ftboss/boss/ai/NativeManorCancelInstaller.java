package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class NativeManorCancelInstaller {
    private static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String TAG = "net/minecraft/nbt/CompoundTag";
    private static final String DATA = "net/minecraft/network/syncher/SynchedEntityData";
    private static final String ACCESS = "net/farmingtales/ftboss/dandanmotion/MotionAccess";
    private static final String[] OWNERS = {
        "net/farmingtales/ftboss/qingyilight/LightHooks",
        "net/farmingtales/ftboss/animation/qingyicast/QingyiCastSync",
        "net/farmingtales/ftboss/zheergenremainingbody/SkillBodyDispatch",
        "net/farmingtales/ftboss/zheergenfx/LightHooks",
        "net/farmingtales/ftboss/dandanmotion/MotionServer",
        "net/farmingtales/ftboss/dandannativelights/LightHooks"
    };
    private static final String[] NAMES = {"cancel", "cancelNative", "captureCancel", "cancel", "clear", "cancel"};

    private NativeManorCancelInstaller() {
    }

    private static InsnList prefix() {
        InsnList instructions = new InsnList();
        for (int index = 0; index < OWNERS.length; index++) {
            instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            if (index == 5) instructions.add(new InsnNode(Opcodes.ICONST_1));
            instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, OWNERS[index], NAMES[index], index == 5 ? "(L" + BOSS + ";I)V" : "(L" + BOSS + ";)V", false));
        }
        return instructions;
    }

    private static InsnList suffix() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/farmingtales/ftboss/zheergenremainingbody/SkillBodyDispatch", "finish", "(L" + BOSS + ";)V", false));
        return instructions;
    }

    private static MethodNode nativeCore() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "cancelManorFiveCast", "()V", null, null);
        for (String field : List.of("actionTicks", "castTicks")) {
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            method.instructions.add(new InsnNode(Opcodes.ICONST_0));
            method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, BOSS, field, "I"));
        }
        for (String field : List.of("DATA_SKILL_CUE", "DATA_COMBAT_ANIMATION")) {
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, BOSS, "entityData", "L" + DATA + ";"));
            method.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, BOSS, field, "Lnet/minecraft/network/syncher/EntityDataAccessor;"));
            method.instructions.add(new TypeInsnNode(Opcodes.NEW, TAG));
            method.instructions.add(new InsnNode(Opcodes.DUP));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, TAG, "<init>", "()V", false));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DATA, "set", "(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V", false));
        }
        method.instructions.add(new InsnNode(Opcodes.RETURN));
        return method;
    }

    private static MethodNode completed() {
        MethodNode method = nativeCore();
        method.instructions.insert(prefix());
        method.instructions.insertBefore(method.instructions.getLast(), suffix());
        return method;
    }

    private static MethodNode motion(boolean writer) {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "dandanMotionData", writer ? "(L" + TAG + ";)V" : "()L" + TAG + ";", null, null);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, BOSS, "entityData", "L" + DATA + ";"));
        method.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, BOSS, "DATA_COMBAT_ANIMATION", "Lnet/minecraft/network/syncher/EntityDataAccessor;"));
        if (writer) {
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DATA, "set", "(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V", false));
            method.instructions.add(new InsnNode(Opcodes.RETURN));
        } else {
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DATA, "get", "(Lnet/minecraft/network/syncher/EntityDataAccessor;)Ljava/lang/Object;", false));
            method.instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, TAG));
            method.instructions.add(new InsnNode(Opcodes.ARETURN));
        }
        method.maxStack = 3;
        method.maxLocals = writer ? 2 : 1;
        return method;
    }

    private static List<String> instructions(MethodNode method) {
        if (!method.tryCatchBlocks.isEmpty()) throw new IllegalStateException("Unreviewed native cancel/motion exception graph");
        List<String> instructions = new ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction.getOpcode() < 0) continue;
            String operand = Integer.toString(instruction.getOpcode());
            if (instruction instanceof VarInsnNode variable) operand += ":" + variable.var;
            else if (instruction instanceof FieldInsnNode field) operand += ":" + field.owner + ":" + field.name + ":" + field.desc;
            else if (instruction instanceof MethodInsnNode call) operand += ":" + call.owner + ":" + call.name + ":" + call.desc + ":" + call.itf;
            else if (instruction instanceof TypeInsnNode type) operand += ":" + type.desc;
            else if (instruction instanceof LdcInsnNode literal) operand += ":" + literal.cst;
            else if (!(instruction instanceof InsnNode)) throw new IllegalStateException("Unreviewed native cancel/motion instruction");
            instructions.add(operand);
        }
        return instructions;
    }

    private static MethodNode method(ClassNode target, String name, String descriptor) {
        List<MethodNode> methods = target.methods.stream().filter(method -> method.name.equals(name) && method.desc.equals(descriptor)).toList();
        if (methods.size() != 1) throw new IllegalStateException("Missing unique genuine native cancel/motion method: " + name + descriptor);
        return methods.getFirst();
    }

    private static void requireTarget(ClassNode target) {
        if (target == null || !target.name.equals(BOSS)) throw new IllegalStateException("Unsupported native original cancel target");
    }

    private static void verifyMethod(MethodNode actual, MethodNode expected) {
        if (actual.access != expected.access || !actual.exceptions.isEmpty() || actual.signature != null || !instructions(actual).equals(instructions(expected))) throw new IllegalStateException("Unreviewed complete native cancel/motion body: " + actual.name + actual.desc);
    }

    public static void install(ClassNode target) {
        requireTarget(target);
        MethodNode cancel = method(target, "cancelManorFiveCast", "()V");
        if (!instructions(cancel).equals(instructions(completed()))) {
            verifyMethod(cancel, nativeCore());
            cancel.instructions.insert(prefix());
            cancel.instructions.insertBefore(Arrays.stream(cancel.instructions.toArray()).filter(instruction -> instruction.getOpcode() == Opcodes.RETURN).findFirst().orElseThrow(), suffix());
            cancel.maxStack = Math.max(cancel.maxStack, 3);
        }
        if (!target.interfaces.contains(ACCESS)) target.interfaces.add(ACCESS);
        for (boolean writer : List.of(false, true)) {
            MethodNode expected = motion(writer);
            if (target.methods.stream().noneMatch(method -> method.name.equals(expected.name) && method.desc.equals(expected.desc))) target.methods.add(expected);
            verifyMethod(method(target, expected.name, expected.desc), expected);
        }
        verifyInstalled(target);
    }

    public static void verifyInstalled(ClassNode target) {
        requireTarget(target);
        if (!target.interfaces.contains(ACCESS)) throw new IllegalStateException("Missing original Dandan motion contract");
        verifyMethod(method(target, "cancelManorFiveCast", "()V"), completed());
        for (boolean writer : List.of(false, true)) {
            MethodNode expected = motion(writer);
            verifyMethod(method(target, expected.name, expected.desc), expected);
        }
    }
}
