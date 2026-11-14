package net.farmingtales.ftboss.boss.ai;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeManorMultiplayerInstaller {
    private static final String TARGET = "net/farmingtales/ftboss/boss/ai/BossScaling";
    private static final String HEALTH = "net/farmingtales/ftboss/boss/ai/ManorHealthBalance";
    private static final String BRIDGE = "net/farmingtales/ftboss/boss/ai/ManorMultiplayerBalance";
    private static final String LIVING = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final String EXPECTED = "8d1a8951e74108424b5460d46b61cb36ebd7ebd61f411ee9802f5d4b9ee10b97";

    private NativeManorMultiplayerInstaller() {
    }

    public static int install(ClassNode target) {
        MethodNode method = method(target);
        List<AbstractInsnNode> original = code(method);
        if (original.stream().anyMatch(instruction -> instruction instanceof MethodInsnNode call && (call.owner.equals(HEALTH) || call.owner.equals(BRIDGE)))) {
            verifyInstalled(target);
            return 0;
        }
        NativeManorRaidInstaller.verifyInstalled(target);
        if (!fingerprint(original).equals(EXPECTED)) throw new IllegalStateException("Changed complete native scaling preimage");
        LabelNode fallback = new LabelNode();
        InsnList added = new InsnList();
        added.add(new VarInsnNode(Opcodes.ALOAD, 0));
        added.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HEALTH, "normalize", "(" + LIVING + ")V", false));
        added.add(new VarInsnNode(Opcodes.ALOAD, 0));
        added.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "tick", "(" + LIVING + ")Z", false));
        added.add(new JumpInsnNode(Opcodes.IFEQ, fallback));
        added.add(new InsnNode(Opcodes.RETURN));
        added.add(fallback);
        method.instructions.insertBefore(method.instructions.getFirst(), added);
        for (AbstractInsnNode instruction : method.instructions.toArray()) if (instruction instanceof FrameNode) method.instructions.remove(instruction);
        method.maxStack = Math.max(1, method.maxStack);
        verifyInstalled(target);
        return 1;
    }

    public static void verifyInstalled(ClassNode target) {
        List<AbstractInsnNode> instructions = code(method(target));
        if (instructions.size() < 7 || !variable(instructions.get(0), 0) || !call(instructions.get(1), HEALTH, "normalize", "(" + LIVING + ")V")
            || !variable(instructions.get(2), 0) || !call(instructions.get(3), BRIDGE, "tick", "(" + LIVING + ")Z")
            || !(instructions.get(4) instanceof JumpInsnNode jump) || jump.getOpcode() != Opcodes.IFEQ
            || next(jump.label) != instructions.get(6) || instructions.get(5).getOpcode() != Opcodes.RETURN
            || instructions.stream().filter(instruction -> instruction instanceof MethodInsnNode invocation && (invocation.owner.equals(HEALTH) || invocation.owner.equals(BRIDGE))).count() != 2
            || !fingerprint(instructions.subList(6, instructions.size())).equals(EXPECTED)) throw new IllegalStateException("Changed exact multiplayer early-return/fallback contract");
        NativeManorRaidInstaller.verifyInstalled(target);
    }

    private static String fingerprint(List<AbstractInsnNode> instructions) {
        StringBuilder result = new StringBuilder();
        for (AbstractInsnNode instruction : instructions) {
            result.append(instruction.getOpcode());
            if (instruction instanceof VarInsnNode variable) result.append(':').append(variable.var);
            else if (instruction instanceof FieldInsnNode field) result.append(':').append(field.owner).append(':').append(field.name).append(':').append(field.desc);
            else if (instruction instanceof MethodInsnNode invocation) result.append(':').append(invocation.owner).append(':').append(invocation.name).append(':').append(invocation.desc).append(':').append(invocation.itf);
            else if (instruction instanceof TypeInsnNode type) result.append(':').append(type.desc);
            else if (instruction instanceof LdcInsnNode constant) result.append(':').append(constant.cst.getClass().getName()).append(':').append(constant.cst);
            else if (instruction instanceof IntInsnNode integer) result.append(':').append(integer.operand);
            else if (instruction instanceof IincInsnNode increment) result.append(':').append(increment.var).append(':').append(increment.incr);
            else if (instruction instanceof JumpInsnNode jump) {
                int index = instructions.indexOf(next(jump.label));
                if (index < 0) throw new IllegalStateException("Native scaling jump escaped complete method");
                result.append(':').append(index);
            } else if (!(instruction instanceof InsnNode)) throw new IllegalStateException("Unreviewed native scaling instruction shape");
            result.append('\n');
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(result.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException failure) {
            throw new IllegalStateException(failure);
        }
    }

    private static MethodNode method(ClassNode target) {
        if (target == null || !target.name.equals(TARGET)) throw new IllegalStateException("Unsupported multiplayer target");
        List<MethodNode> methods = target.methods.stream().filter(method -> method.name.equals("tick") && method.desc.equals("(" + LIVING + ")V")).toList();
        if (methods.size() != 1 || (methods.getFirst().access & (Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != Opcodes.ACC_STATIC || !methods.getFirst().tryCatchBlocks.isEmpty()) throw new IllegalStateException("Changed native static scaling method contract");
        return methods.getFirst();
    }

    private static boolean variable(AbstractInsnNode instruction, int slot) {
        return instruction instanceof VarInsnNode variable && variable.getOpcode() == Opcodes.ALOAD && variable.var == slot;
    }

    private static boolean call(AbstractInsnNode instruction, String owner, String name, String descriptor) {
        return instruction instanceof MethodInsnNode invocation && invocation.getOpcode() == Opcodes.INVOKESTATIC && invocation.owner.equals(owner) && invocation.name.equals(name) && invocation.desc.equals(descriptor) && !invocation.itf;
    }

    private static AbstractInsnNode next(AbstractInsnNode instruction) {
        while (instruction != null && instruction.getOpcode() < 0) instruction = instruction.getNext();
        return instruction;
    }

    private static List<AbstractInsnNode> code(MethodNode method) {
        List<AbstractInsnNode> result = new ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions) if (instruction.getOpcode() >= 0) result.add(instruction);
        return result;
    }
}
