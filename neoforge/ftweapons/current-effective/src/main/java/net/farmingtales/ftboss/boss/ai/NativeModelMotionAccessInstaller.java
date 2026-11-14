package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeModelMotionAccessInstaller {
    private static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String ACCESS = "net/farmingtales/ftboss/lemonmotion/LemonMotionAccess";
    private static final String TAG = "net/minecraft/nbt/CompoundTag";
    private static final String DATA = "net/minecraft/network/syncher/SynchedEntityData";
    private static final String GETTER = "()L" + TAG + ";";

    private NativeModelMotionAccessInstaller() {
    }

    private static MethodNode getter(String name) {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, name, GETTER, null, null);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, BOSS, "entityData", "L" + DATA + ";"));
        method.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, BOSS, "DATA_COMBAT_ANIMATION", "Lnet/minecraft/network/syncher/EntityDataAccessor;"));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DATA, "get", "(Lnet/minecraft/network/syncher/EntityDataAccessor;)Ljava/lang/Object;", false));
        method.instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, TAG));
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
        method.maxStack = 2;
        method.maxLocals = 1;
        return method;
    }

    private static List<String> instructions(MethodNode method) {
        if (method.access != Opcodes.ACC_PUBLIC || method.signature != null || !method.exceptions.isEmpty() || !method.tryCatchBlocks.isEmpty()) throw new IllegalStateException("Unexpected motion getter contract");
        List<String> result = new ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction.getOpcode() < 0) continue;
            String token = Integer.toString(instruction.getOpcode());
            if (instruction instanceof VarInsnNode variable) token += ":" + variable.var;
            else if (instruction instanceof FieldInsnNode field) token += ":" + field.owner + ":" + field.name + ":" + field.desc;
            else if (instruction instanceof MethodInsnNode call) token += ":" + call.owner + ":" + call.name + ":" + call.desc + ":" + call.itf;
            else if (instruction instanceof TypeInsnNode type) token += ":" + type.desc;
            else if (!(instruction instanceof InsnNode)) throw new IllegalStateException("Unreviewed motion getter instruction");
            result.add(token);
        }
        return result;
    }

    private static MethodNode find(ClassNode owner, String name) {
        List<MethodNode> methods = owner.methods.stream().filter(method -> method.name.equals(name)).toList();
        if (methods.size() != 1 || !methods.getFirst().desc.equals(GETTER)) throw new IllegalStateException("Missing or ambiguous native motion getter: " + name);
        return methods.getFirst();
    }

    private static void preflight(ClassNode owner) {
        if (!owner.name.equals(BOSS) || !owner.interfaces.contains("net/farmingtales/ftboss/dandanmotion/MotionAccess")) throw new IllegalStateException("Native motion base is not installed");
        if (owner.fields.stream().filter(field -> field.name.equals("DATA_COMBAT_ANIMATION") && field.desc.equals("Lnet/minecraft/network/syncher/EntityDataAccessor;") && (field.access & Opcodes.ACC_STATIC) != 0).count() != 1) throw new IllegalStateException("Shared combat animation accessor changed");
        List<MethodNode> readers = owner.methods.stream().filter(method -> method.name.equals("dandanMotionData") && method.desc.equals(GETTER)).toList();
        if (readers.size() != 1 || !instructions(readers.getFirst()).equals(instructions(getter("dandanMotionData")))) throw new IllegalStateException("Shared combat animation reader changed");
    }

    public static void install(ClassNode owner) {
        preflight(owner);
        if (owner.interfaces.contains(ACCESS) || owner.methods.stream().anyMatch(method -> method.name.equals("lemonMotionData"))) {
            verifyInstalled(owner);
            return;
        }
        owner.interfaces.add(ACCESS);
        owner.methods.add(getter("lemonMotionData"));
        verifyInstalled(owner);
    }

    public static void verifyInstalled(ClassNode owner) {
        preflight(owner);
        if (owner.interfaces.stream().filter(ACCESS::equals).count() != 1 || !instructions(find(owner, "lemonMotionData")).equals(instructions(getter("lemonMotionData")))) throw new IllegalStateException("Original Lemon shared-state bridge differs");
    }
}
