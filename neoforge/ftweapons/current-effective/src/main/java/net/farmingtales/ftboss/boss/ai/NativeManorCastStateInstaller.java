package net.farmingtales.ftboss.boss.ai;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public final class NativeManorCastStateInstaller {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static final String BOSS = PREFIX + "entity/EasterBossEntity";
    private static final String DATA = "net/minecraft/network/syncher/SynchedEntityData";
    private static final String ACCESSOR = "Lnet/minecraft/network/syncher/EntityDataAccessor;";
    private static final String TAG = "Lnet/minecraft/nbt/CompoundTag;";
    private static final String[] FIELDS = {"ftboss$qingyiCast", "ftboss$zicaiCast", "DATA_XUELI_CAST_ACTIONS"};
    private static final String[] INTERFACES = {"animation/qingyicast/QingyiCastAccess", "animation/zicaicast/ZicaiCastAccess", "body/BodyCueAccess", "xueli_cast_actions/CastAccess", "zheergennightfall/NightfallAccess"};
    private static final String[] READERS = {"qingyiCastState", "zicaiCastState", "ftboss$bodyRead", "ftboss$getXueliCastActions", "ftboss$nightfallRead"};
    private static final String[] WRITERS = {"qingyiCastState", "zicaiCastState", "ftboss$bodyWrite", "ftboss$setXueliCastActions", "ftboss$nightfallWrite"};
    private static final String[] ROOTS = {FIELDS[0], FIELDS[1], "DATA_COMBAT_ANIMATION", FIELDS[2], "DATA_COMBAT_ANIMATION"};
    private static final Map<String, String> EXPECTED = Map.ofEntries(
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity.<clinit>()V", "bd0ae4dc764b563848423adf8e38cf48bad4d6a4b7e73497bd87088deb298ae7"),
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity.defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V", "a939a160360873b843395abf3e921d69d53f2b64cfeeb90abbe42682e290e50f")
    );
    private NativeManorCastStateInstaller() {
    }
    public static int install(ClassNode target) {
        requireTarget(target);
        boolean present = target.fields.stream().anyMatch(member -> Arrays.asList(FIELDS).contains(member.name)) || target.interfaces.stream().anyMatch(name -> Arrays.stream(INTERFACES).anyMatch(value -> name.equals(PREFIX + value))) || target.methods.stream().anyMatch(member -> Arrays.asList(READERS).contains(member.name) || Arrays.asList(WRITERS).contains(member.name));
        if (present) { verifyInstalled(target); return 0; }
        for (MethodNode member : selected(target)) if (!fingerprint(member).equals(EXPECTED.get(BOSS + "." + member.name + member.desc))) throw new IllegalStateException("Changed complete native cast state preimage: " + member.name);
        field(target, "DATA_COMBAT_ANIMATION", ACCESSOR);
        for (String name : FIELDS) target.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, name, ACCESSOR, "Lnet/minecraft/network/syncher/EntityDataAccessor<" + TAG + ">;", null));
        for (int index = 0; index < INTERFACES.length; index++) { target.interfaces.add(PREFIX + INTERFACES[index]); target.methods.add(accessor(index, false)); target.methods.add(accessor(index, true)); }
        beforeReturns(method(target, "<clinit>", "()V"), ids()); beforeReturns(method(target, "defineSynchedData", "(L" + DATA + "$Builder;)V"), definitions());
        for (MethodNode member : selected(target)) { for (AbstractInsnNode instruction : member.instructions.toArray()) if (instruction instanceof FrameNode) member.instructions.remove(instruction); member.maxStack += 8; }
        verifyInstalled(target); return 2;
    }
    public static void verifyInstalled(ClassNode target) {
        requireTarget(target);
        for (String name : FIELDS) { FieldNode actual = field(target, name, ACCESSOR); if (actual.access != (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL) || !Objects.equals(actual.signature, "Lnet/minecraft/network/syncher/EntityDataAccessor<" + TAG + ">;")) throw new IllegalStateException("Changed appended cast synchronized state"); }
        for (int index = 0; index < INTERFACES.length; index++) { String expectedInterface = PREFIX + INTERFACES[index]; if (target.interfaces.stream().filter(expectedInterface::equals).count() != 1) throw new IllegalStateException("Missing/duplicate original cast interface"); for (boolean setter : new boolean[]{false, true}) { MethodNode expected = accessor(index, setter), actual = method(target, expected.name, expected.desc); if (!fingerprint(expected).equals(fingerprint(actual))) throw new IllegalStateException("Changed complete original cast accessor copy/reference/modifier semantics"); } }
        for (MethodNode member : selected(target)) { MethodNode result = copy(member); strip(result, member.name.equals("<clinit>") ? ids() : definitions()); if (!fingerprint(result).equals(EXPECTED.get(BOSS + "." + member.name + member.desc))) throw new IllegalStateException("Changed native prior synchronized fields/defaults/ID order"); }
    }
    private static InsnList ids() {
        InsnList result = new InsnList();
        for (String name : FIELDS) { result.add(new LdcInsnNode(Type.getObjectType(BOSS))); result.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/network/syncher/EntityDataSerializers", "COMPOUND_TAG", "Lnet/minecraft/network/syncher/EntityDataSerializer;")); result.add(new MethodInsnNode(Opcodes.INVOKESTATIC, DATA, "defineId", "(Ljava/lang/Class;Lnet/minecraft/network/syncher/EntityDataSerializer;)" + ACCESSOR, false)); result.add(new FieldInsnNode(Opcodes.PUTSTATIC, BOSS, name, ACCESSOR)); }
        return result;
    }
    private static InsnList definitions() {
        InsnList result = new InsnList();
        for (String name : FIELDS) { result.add(new VarInsnNode(Opcodes.ALOAD, 1)); result.add(new FieldInsnNode(Opcodes.GETSTATIC, BOSS, name, ACCESSOR)); result.add(new TypeInsnNode(Opcodes.NEW, "net/minecraft/nbt/CompoundTag")); result.add(new InsnNode(Opcodes.DUP)); result.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/nbt/CompoundTag", "<init>", "()V", false)); result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DATA + "$Builder", "define", "(" + ACCESSOR + "Ljava/lang/Object;)L" + DATA + "$Builder;", false)); result.add(new InsnNode(Opcodes.POP)); }
        return result;
    }
    private static MethodNode accessor(int index, boolean setter) {
        int access = Opcodes.ACC_PUBLIC | (index == 4 ? Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC : 0);
        MethodNode result = new MethodNode(access, setter ? WRITERS[index] : READERS[index], setter ? "(" + TAG + ")V" : "()" + TAG, null, null); InsnList body = result.instructions;
        body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new FieldInsnNode(Opcodes.GETFIELD, BOSS, "entityData", "L" + DATA + ";")); body.add(new FieldInsnNode(Opcodes.GETSTATIC, BOSS, ROOTS[index], ACCESSOR));
        if (setter) { body.add(new VarInsnNode(Opcodes.ALOAD, 1)); if (index < 2) body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/nbt/CompoundTag", "copy", "()" + TAG, false)); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DATA, "set", "(" + ACCESSOR + "Ljava/lang/Object;)V", false)); body.add(new InsnNode(Opcodes.RETURN)); }
        else { body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DATA, "get", "(" + ACCESSOR + ")Ljava/lang/Object;", false)); body.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/nbt/CompoundTag")); if (index < 2) body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/nbt/CompoundTag", "copy", "()" + TAG, false)); body.add(new InsnNode(Opcodes.ARETURN)); }
        result.maxLocals = setter ? 2 : 1; result.maxStack = 3; return result;
    }
    private static void beforeReturns(MethodNode member, InsnList fragment) { List<AbstractInsnNode> returns = code(member).stream().filter(value -> value.getOpcode() == Opcodes.RETURN).toList(); if (returns.size() != 1) throw new IllegalStateException("Changed unique cast state definition return"); member.instructions.insertBefore(returns.getFirst(), fragment); }
    private static void strip(MethodNode member, InsnList fragment) {
        List<AbstractInsnNode> actual = code(member), expected = Arrays.stream(fragment.toArray()).filter(value -> value.getOpcode() >= 0).toList(); int start = actual.size() - expected.size() - 1;
        if (start < 0 || actual.getLast().getOpcode() != Opcodes.RETURN) throw new IllegalStateException("Missing cast state tail");
        for (int offset = 0; offset < expected.size(); offset++) if (!operand(actual.get(start + offset)).equals(operand(expected.get(offset)))) throw new IllegalStateException("Moved or changed appended cast state IDs/defaults");
        for (int offset = 0; offset < expected.size(); offset++) member.instructions.remove(actual.get(start + offset));
    }
    private static List<MethodNode> selected(ClassNode target) { requireTarget(target); return List.of(method(target, "<clinit>", "()V"), method(target, "defineSynchedData", "(L" + DATA + "$Builder;)V")); }
    private static MethodNode copy(MethodNode source) { MethodNode result = new MethodNode(source.access, source.name, source.desc, source.signature, source.exceptions.toArray(String[]::new)); source.accept(result); return result; }
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
        throw new IllegalStateException("Unreviewed complete native basic instruction shape");
    }
    private static String fingerprint(MethodNode member) {
        List<AbstractInsnNode> instructions = code(member); StringBuilder result = new StringBuilder(member.name).append(member.desc).append(':').append(member.access).append('\n');
        for (AbstractInsnNode instruction : instructions) { result.append(operand(instruction)); if (instruction instanceof JumpInsnNode jump) { int destination = instructions.indexOf(next(jump.label)); if (destination < 0) throw new IllegalStateException("Native basic branch escapes full method"); result.append(':').append(destination); } result.append('\n'); }
        for (TryCatchBlockNode handler : member.tryCatchBlocks) result.append("handler:").append(instructions.indexOf(next(handler.start))).append(':').append(instructions.indexOf(next(handler.end))).append(':').append(instructions.indexOf(next(handler.handler))).append(':').append(handler.type).append('\n');
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(result.toString().getBytes(StandardCharsets.UTF_8))); } catch (java.security.NoSuchAlgorithmException failure) { throw new IllegalStateException(failure); }
    }
    private static List<AbstractInsnNode> code(MethodNode method) { return Arrays.stream(method.instructions.toArray()).filter(instruction -> instruction.getOpcode() >= 0).toList(); }
    private static AbstractInsnNode next(AbstractInsnNode instruction) { while (instruction != null && instruction.getOpcode() < 0) instruction = instruction.getNext(); return instruction; }
    private static MethodNode method(ClassNode target, String name, String descriptor) { List<MethodNode> methods = target.methods.stream().filter(member -> member.name.equals(name) && member.desc.equals(descriptor) && (member.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0).toList(); if (methods.size() != 1) throw new IllegalStateException("Missing actual native basic method: " + name); return methods.getFirst(); }
    private static FieldNode field(ClassNode target, String name, String descriptor) { List<FieldNode> fields = target.fields.stream().filter(member -> member.name.equals(name) && member.desc.equals(descriptor)).toList(); if (fields.size() != 1) throw new IllegalStateException("Missing actual native basic field: " + name); return fields.getFirst(); }
    private static void requireTarget(ClassNode target) { if (target == null || !target.name.equals(BOSS)) throw new IllegalStateException("Unsupported native cast state target"); }
}
