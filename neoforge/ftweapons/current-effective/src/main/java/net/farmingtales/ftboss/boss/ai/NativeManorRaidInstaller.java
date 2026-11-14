package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeManorRaidInstaller {
    private static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String SCALING = "net/farmingtales/ftboss/boss/ai/BossScaling";
    private static final String BRIDGE = "net/farmingtales/ftboss/boss/ai/ManorRaidCombat";
    private static final String LIVING = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final String SOURCE = "Lnet/minecraft/world/damagesource/DamageSource;";

    private NativeManorRaidInstaller() {
    }

    public static int install(ClassNode target) {
        MethodNode method = method(target);
        List<AbstractInsnNode> code = instructions(method);
        if (code.stream().anyMatch(instruction -> instruction instanceof MethodInsnNode call && call.owner.equals(BRIDGE))) {
            verifyInstalled(target);
            return 0;
        }
        InsnList added = new InsnList();
        if (target.name.equals(BOSS)) {
            MethodInsnNode write = healthWrite(code);
            added.add(new InsnNode(Opcodes.DUP2));
            added.add(new VarInsnNode(Opcodes.ALOAD, 1));
            added.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "beforeHealthWrite", "(" + LIVING + "F" + SOURCE + ")V", false));
            method.instructions.insertBefore(write, added);
            method.maxStack += 3;
        } else {
            VarInsnNode store = scalingStore(code);
            added.add(new VarInsnNode(Opcodes.ALOAD, 0));
            added.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "scaleMaximumHealth", "(D" + LIVING + ")D", false));
            method.instructions.insertBefore(store, added);
            method.maxStack += 1;
        }
        verifyInstalled(target);
        return 1;
    }

    public static void verifyInstalled(ClassNode target) {
        MethodNode method = method(target);
        List<AbstractInsnNode> code = instructions(method);
        List<MethodInsnNode> hooks = code.stream().filter(instruction -> instruction instanceof MethodInsnNode call && call.owner.equals(BRIDGE)).map(instruction -> (MethodInsnNode)instruction).toList();
        if (hooks.size() != 1) throw new IllegalStateException("Missing unique native raid bridge");
        MethodInsnNode hook = hooks.getFirst();
        int position = code.indexOf(hook);
        List<AbstractInsnNode> original = new ArrayList<>(code);
        if (target.name.equals(BOSS)) {
            if (!hook.name.equals("beforeHealthWrite") || !hook.desc.equals("(" + LIVING + "F" + SOURCE + ")V") || hook.getOpcode() != Opcodes.INVOKESTATIC
                || position < 2 || code.get(position - 2).getOpcode() != Opcodes.DUP2 || !variable(code.get(position - 1), Opcodes.ALOAD, 1)) {
                throw new IllegalStateException("Changed actual raid health bridge operands");
            }
            original.subList(position - 2, position + 1).clear();
            if (healthWrite(original) != code.get(position + 1)) throw new IllegalStateException("Raid contribution bridge moved away from actual remaining-health write");
        } else {
            if (!hook.name.equals("scaleMaximumHealth") || !hook.desc.equals("(D" + LIVING + ")D") || hook.getOpcode() != Opcodes.INVOKESTATIC
                || position < 1 || !variable(code.get(position - 1), Opcodes.ALOAD, 0)) {
                throw new IllegalStateException("Changed actual raid scaling bridge operands");
            }
            original.subList(position - 1, position + 1).clear();
            if (scalingStore(original) != code.get(position + 1)) throw new IllegalStateException("Raid health multiplier moved away from original desired-health store");
        }
    }

    private static MethodNode method(ClassNode target) {
        if (target == null || !List.of(BOSS, SCALING).contains(target.name)) throw new IllegalStateException("Unsupported native raid target");
        String name = target.name.equals(BOSS) ? "manorHurt" : "tick";
        String descriptor = target.name.equals(BOSS) ? "(" + SOURCE + "F)Z" : "(" + LIVING + ")V";
        List<MethodNode> methods = target.methods.stream().filter(method -> method.name.equals(name) && method.desc.equals(descriptor)).toList();
        if (methods.size() != 1 || (methods.getFirst().access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) != 0
            || ((methods.getFirst().access & Opcodes.ACC_STATIC) != 0) != target.name.equals(SCALING)) throw new IllegalStateException("Missing actual unique native raid method");
        return methods.getFirst();
    }

    private static MethodInsnNode healthWrite(List<AbstractInsnNode> code) {
        List<MethodInsnNode> writes = code.stream().filter(instruction -> call(instruction, BOSS, "setHealth", "(F)V")).map(instruction -> (MethodInsnNode)instruction).toList();
        if (writes.size() != 3 || code.get(code.indexOf(writes.getFirst()) - 1).getOpcode() != Opcodes.FCONST_0 || code.get(code.indexOf(writes.getLast()) - 1).getOpcode() != Opcodes.FCONST_0) throw new IllegalStateException("Changed native kill/ordinary health write scope");
        MethodInsnNode write = writes.get(1);
        int position = code.indexOf(write);
        if (position < 10 || code.get(position - 1).getOpcode() != Opcodes.FSUB || !variable(code.get(position - 2), Opcodes.FLOAD, 3)
            || !call(code.get(position - 3), BOSS, "getHealth", "()F") || !variable(code.get(position - 4), Opcodes.ALOAD, 0) || !variable(code.get(position - 5), Opcodes.ALOAD, 0)
            || !(code.get(position - 6) instanceof JumpInsnNode jump) || jump.getOpcode() != Opcodes.IFLE || code.get(position - 7).getOpcode() != Opcodes.FCMPL
            || code.get(position - 8).getOpcode() != Opcodes.FCONST_0 || !variable(code.get(position - 9), Opcodes.FLOAD, 3)) throw new IllegalStateException("Changed native positive post-absorption actual health debit");
        return write;
    }

    private static VarInsnNode scalingStore(List<AbstractInsnNode> code) {
        List<MethodInsnNode> writes = code.stream().filter(instruction -> call(instruction, "net/minecraft/world/entity/ai/attributes/AttributeInstance", "setBaseValue", "(D)V")).map(instruction -> (MethodInsnNode)instruction).toList();
        if (writes.size() != 2) throw new IllegalStateException("Changed native health/attack scaling write scope");
        int position = code.indexOf(writes.getFirst());
        if (!variable(code.get(position - 1), Opcodes.DLOAD, 12) || !variable(code.get(position - 2), Opcodes.ALOAD, 9)) throw new IllegalStateException("Changed native health attribute write operands");
        List<VarInsnNode> stores = code.stream().filter(instruction -> variable(instruction, Opcodes.DSTORE, 12)).map(instruction -> (VarInsnNode)instruction).toList();
        if (stores.size() != 1) throw new IllegalStateException("Changed native desired-health local");
        VarInsnNode store = stores.getFirst();
        int index = code.indexOf(store);
        if (index < 3 || code.get(index - 1).getOpcode() != Opcodes.DMUL || !variable(code.get(index - 2), Opcodes.DLOAD, 5) || !variable(code.get(index - 3), Opcodes.DLOAD, 10)) throw new IllegalStateException("Changed native original base-health/player-scaling product");
        return store;
    }

    private static boolean variable(AbstractInsnNode instruction, int opcode, int slot) {
        return instruction instanceof VarInsnNode variable && variable.getOpcode() == opcode && variable.var == slot;
    }

    private static boolean call(AbstractInsnNode instruction, String owner, String name, String descriptor) {
        return instruction instanceof MethodInsnNode method && method.getOpcode() == Opcodes.INVOKEVIRTUAL && method.owner.equals(owner) && method.name.equals(name) && method.desc.equals(descriptor);
    }

    private static List<AbstractInsnNode> instructions(MethodNode method) {
        List<AbstractInsnNode> result = new ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions) if (instruction.getOpcode() >= 0) result.add(instruction);
        return result;
    }
}
