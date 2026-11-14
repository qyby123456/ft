package net.farmingtales.ftboss.boss.ai;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public final class NativeManorBasicInstaller {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static final String BOSS = PREFIX + "entity/EasterBossEntity";
    private static final String GOAL = BOSS + "$PlayerReachMeleeGoal";
    private static final String QINGYI = PREFIX + "qingyi_basic/BasicServer";
    private static final String XUELI = PREFIX + "xueli_basic/BasicServer";
    private static final String LANES = PREFIX + "boss/ai/ManorCombatLanes";
    private static final String CONTINUITY = PREFIX + "boss/ai/ManorCombatContinuity";
    private static final String ZICAI = PREFIX + "util/ZicaiCombatRules";
    private static final String DATA = "net/minecraft/network/syncher/SynchedEntityData";
    private static final String ACCESSOR = "Lnet/minecraft/network/syncher/EntityDataAccessor;";
    private static final String TAG = "Lnet/minecraft/nbt/CompoundTag;";
    private static final String LIVING = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final String ENTITY = "Lnet/minecraft/world/entity/Entity;";
    private static final String[] FIELDS = {"DATA_QINGYI_BASIC_ACTION", "DATA_XUELI_BASIC"};
    private static final String[] ACCESS = {PREFIX + "qingyi_basic/BasicAccess", PREFIX + "xueli_basic/BasicAccess"};
    private static final String[] STATES = {"Qingyi", "Xueli"};
    private static final Map<String, String> EXPECTED = Map.ofEntries(
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity$PlayerReachMeleeGoal.canUse()Z", "331bbbe755e4a9e1757a90419fa12b6b2af551a65022f1efb0c211b4cfbee313"),
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity$PlayerReachMeleeGoal.checkAndPerformAttack(Lnet/minecraft/world/entity/LivingEntity;)V", "b1ec828df9908ea32125590a09277331212d4ba0197297f625e493b3a2d65877"),
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity$PlayerReachMeleeGoal.reachSqr()D", "a1d8f634acdd683ad0e8bd84f8806d6698dcaa659f23fd489aa2943080795e7e"),
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity$PlayerReachMeleeGoal.stop()V", "533f62e884d1117fad307347d2a09c1078adb9c855eda186926318da3e7c12b8"),
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity$PlayerReachMeleeGoal.tick()V", "b5305ca5b4a5d4f7bfac569596df754c1b3d350d5c4003578794a4f898086882"),
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity.<clinit>()V", "54e5f1e39a4523d921c2990e6cfc3706f58068db56281f53b1a64107ba670c58"),
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity.blocksMelee()Z", "cb33f54c8c276c497b8f1f4bb836815ecf05e180c052431abb5768e0752b28a1"),
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity.defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V", "0404476a0a108952ef2b35698718df232d3210f67b924c0a6b1f549550cde3fd"),
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity.doHurtTarget(Lnet/minecraft/world/entity/Entity;)Z", "097c00823e6c013914b326ed950cf67f73a904cb86fe2ef5086c488af74d630d"),
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity.syncCombatAnimation(IZIZ)V", "0fef4a4524c410a58680e42d5d4f208a780d02dfa068a3296ab863659a3eb2f6")
    );

    private NativeManorBasicInstaller() {
    }

    public static int install(ClassNode target) {
        requireTarget(target);
        boolean present = target.fields.stream().anyMatch(field -> Arrays.asList(FIELDS).contains(field.name)) || target.interfaces.stream().anyMatch(Arrays.asList(ACCESS)::contains)
            || target.methods.stream().flatMap(method -> code(method).stream()).anyMatch(instruction -> instruction instanceof MethodInsnNode invocation && Set.of(QINGYI, XUELI).contains(invocation.owner));
        if (present) { verifyInstalled(target); return 0; }
        for (Map.Entry<String, String> entry : preimages(target).entrySet()) if (!entry.getValue().equals(EXPECTED.get(entry.getKey()))) throw new IllegalStateException("Changed complete native basic entry preimage: " + entry.getKey());
        if (target.name.equals(BOSS)) {
            for (int index = 0; index < FIELDS.length; index++) {
                target.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, FIELDS[index], ACCESSOR, "Lnet/minecraft/network/syncher/EntityDataAccessor<" + TAG + ">;", null));
                target.interfaces.add(ACCESS[index]);
                target.methods.add(accessor(index, false)); target.methods.add(accessor(index, true));
            }
            beforeReturns(method(target, "<clinit>", "()V"), Opcodes.RETURN, ids());
            beforeReturns(method(target, "defineSynchedData", "(L" + DATA + "$Builder;)V"), Opcodes.RETURN, definitions());
            MethodNode hurt = method(target, "doHurtTarget", "(" + ENTITY + ")Z");
            hurt.instructions.insertBefore(hurt.instructions.getFirst(), attackGuard());
            List<AbstractInsnNode> superCalls = code(hurt).stream().filter(instruction -> instruction instanceof MethodInsnNode invocation && invocation.getOpcode() == Opcodes.INVOKESPECIAL && invocation.name.equals("doHurtTarget") && invocation.desc.equals("(" + ENTITY + ")Z")).toList();
            if (superCalls.size() != 1) throw new IllegalStateException("Missing unique native super melee debit");
            hurt.instructions.insertBefore(superCalls.getFirst(), strikes());
            MethodNode animation = method(target, "syncCombatAnimation", "(IZIZ)V");
            animation.instructions.insertBefore(animation.instructions.getFirst(), animationCancel());
            beforeReturns(method(target, "blocksMelee", "()Z"), Opcodes.IRETURN, laneBlock());
        } else {
            field(target, "boss", "L" + BOSS + ";"); field(target, "qingyiMeleeCooldown", "I");
            beforeReturns(method(target, "tick", "()V"), Opcodes.RETURN, validations());
            MethodNode attack = method(target, "checkAndPerformAttack", "(" + LIVING + ")V");
            attack.instructions.insertBefore(attack.instructions.getFirst(), preparations()); attack.maxLocals = Math.max(4, attack.maxLocals);
            beforeReturns(method(target, "stop", "()V"), Opcodes.RETURN, cancellations());
            MethodNode usable = method(target, "canUse", "()Z");
            usable.instructions.insertBefore(usable.instructions.getFirst(), repair());
            List<AbstractInsnNode> ranged = code(usable).stream().filter(instruction -> instruction.getOpcode() == Opcodes.BALOAD).toList();
            if (ranged.size() != 1) throw new IllegalStateException("Missing unique native melee ranged gate");
            usable.instructions.insert(ranged.getFirst(), laneRanged());
            beforeReturns(method(target, "reachSqr", "()D"), Opcodes.DRETURN, reach());
        }
        for (MethodNode member : selected(target)) { for (AbstractInsnNode instruction : member.instructions.toArray()) if (instruction instanceof FrameNode) member.instructions.remove(instruction); member.maxStack += 16; }
        verifyInstalled(target);
        return target.name.equals(BOSS) ? 5 : 5;
    }

    public static void verifyInstalled(ClassNode target) {
        requireTarget(target);
        if (target.name.equals(BOSS)) for (int index = 0; index < FIELDS.length; index++) {
            FieldNode actual = field(target, FIELDS[index], ACCESSOR);
            if (actual.access != (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL) || !Objects.equals(actual.signature, "Lnet/minecraft/network/syncher/EntityDataAccessor<" + TAG + ">;") || target.interfaces.stream().filter(ACCESS[index]::equals).count() != 1) throw new IllegalStateException("Changed genuine basic synchronized field/interface contract");
            for (boolean setter : new boolean[]{false, true}) { MethodNode expected = accessor(index, setter); MethodNode observed = method(target, expected.name, expected.desc); if (observed.access != Opcodes.ACC_PUBLIC || !fingerprint(expected).equals(fingerprint(observed))) throw new IllegalStateException("Changed exact original basic accessor semantics"); }
        }
        for (MethodNode member : selected(target)) {
            MethodNode restored = copy(member);
            String key = target.name + "." + member.name + member.desc;
            List<InsnList> fragments = fragments(target.name, member.name);
            for (InsnList fragment : fragments) strip(restored, fragment);
            if (!fingerprint(restored).equals(EXPECTED.get(key))) throw new IllegalStateException("Changed original native basic fallback or hook position: " + key);
        }
    }

    private static List<InsnList> fragments(String owner, String name) {
        if (owner.equals(BOSS)) return switch (name) { case "<clinit>" -> List.of(ids()); case "defineSynchedData" -> List.of(definitions()); case "doHurtTarget" -> List.of(attackGuard(), strikes()); case "syncCombatAnimation" -> List.of(animationCancel()); case "blocksMelee" -> List.of(laneBlock()); default -> throw new IllegalStateException("Unreviewed Boss basic method"); };
        return switch (name) { case "tick" -> List.of(validations()); case "checkAndPerformAttack" -> List.of(preparations()); case "stop" -> List.of(cancellations()); case "canUse" -> List.of(repair(), laneRanged()); case "reachSqr" -> List.of(reach()); default -> throw new IllegalStateException("Unreviewed native melee goal method"); };
    }

    private static InsnList ids() {
        InsnList result = new InsnList();
        for (String field : FIELDS) { result.add(new LdcInsnNode(Type.getObjectType(BOSS))); result.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraft/network/syncher/EntityDataSerializers", "COMPOUND_TAG", "Lnet/minecraft/network/syncher/EntityDataSerializer;")); result.add(new MethodInsnNode(Opcodes.INVOKESTATIC, DATA, "defineId", "(Ljava/lang/Class;Lnet/minecraft/network/syncher/EntityDataSerializer;)" + ACCESSOR, false)); result.add(new FieldInsnNode(Opcodes.PUTSTATIC, BOSS, field, ACCESSOR)); }
        return result;
    }

    private static InsnList definitions() {
        InsnList result = new InsnList();
        for (String field : FIELDS) { result.add(new VarInsnNode(Opcodes.ALOAD, 1)); result.add(new FieldInsnNode(Opcodes.GETSTATIC, BOSS, field, ACCESSOR)); result.add(new TypeInsnNode(Opcodes.NEW, "net/minecraft/nbt/CompoundTag")); result.add(new InsnNode(Opcodes.DUP)); result.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/nbt/CompoundTag", "<init>", "()V", false)); result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DATA + "$Builder", "define", "(" + ACCESSOR + "Ljava/lang/Object;)L" + DATA + "$Builder;", false)); result.add(new InsnNode(Opcodes.POP)); }
        return result;
    }

    private static MethodNode accessor(int index, boolean setter) {
        MethodNode result = new MethodNode(Opcodes.ACC_PUBLIC, "ftboss$" + (setter ? "set" : "get") + STATES[index] + "Basic", setter ? "(" + TAG + ")V" : "()" + TAG, null, null);
        result.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, BOSS, "entityData", "L" + DATA + ";")); result.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, BOSS, FIELDS[index], ACCESSOR));
        if (setter) { result.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1)); result.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DATA, "set", "(" + ACCESSOR + "Ljava/lang/Object;)V", false)); result.instructions.add(new InsnNode(Opcodes.RETURN)); }
        else { result.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DATA, "get", "(" + ACCESSOR + ")Ljava/lang/Object;", false)); result.instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/nbt/CompoundTag")); result.instructions.add(new InsnNode(Opcodes.ARETURN)); }
        result.maxLocals = setter ? 2 : 1; result.maxStack = 3; return result;
    }

    private static InsnList attackGuard() { InsnList result = new InsnList(); LabelNode allowed = new LabelNode(); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new VarInsnNode(Opcodes.ALOAD, 1)); result.add(invoke(CONTINUITY, "canAttack", "(L" + BOSS + ";" + ENTITY + ")Z")); result.add(new JumpInsnNode(Opcodes.IFNE, allowed)); result.add(new InsnNode(Opcodes.ICONST_0)); result.add(new InsnNode(Opcodes.IRETURN)); result.add(allowed); return result; }
    private static InsnList strikes() { InsnList result = new InsnList(); for (String server : List.of(QINGYI, XUELI)) { result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new VarInsnNode(Opcodes.ALOAD, 1)); result.add(invoke(server, "strike", "(L" + BOSS + ";" + ENTITY + ")V")); } return result; }
    private static InsnList animationCancel() { InsnList result = new InsnList(); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new VarInsnNode(Opcodes.ILOAD, 2)); result.add(invoke(XUELI, "cancel", "(L" + BOSS + ";Z)V")); return result; }
    private static InsnList laneBlock() { InsnList result = new InsnList(); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "movementLocked", "()Z", false)); result.add(invoke(LANES, "blocksMelee", "(ZL" + BOSS + ";Z)Z")); return result; }
    private static void boss(InsnList result) { result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new FieldInsnNode(Opcodes.GETFIELD, GOAL, "boss", "L" + BOSS + ";")); }
    private static InsnList validations() { InsnList result = new InsnList(); for (String server : List.of(QINGYI, XUELI)) { boss(result); boss(result); result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "blocksMelee", "()Z", false)); result.add(invoke(server, "validate", "(L" + BOSS + ";Z)V")); } return result; }
    private static InsnList preparations() {
        InsnList result = new InsnList(); LabelNode skip = new LabelNode(); result.add(new VarInsnNode(Opcodes.ALOAD, 1)); result.add(new JumpInsnNode(Opcodes.IFNULL, skip)); boss(result); result.add(new VarInsnNode(Opcodes.ALOAD, 1)); result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "distanceToSqr", "(" + ENTITY + ")D", false)); result.add(new VarInsnNode(Opcodes.DSTORE, 2));
        for (String server : List.of(XUELI, QINGYI)) { boss(result); result.add(new VarInsnNode(Opcodes.ALOAD, 1)); result.add(new VarInsnNode(Opcodes.DLOAD, 2)); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new FieldInsnNode(Opcodes.GETFIELD, GOAL, "qingyiMeleeCooldown", "I")); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, GOAL, "getAttackInterval", "()I", false)); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, GOAL, "reachSqr", "()D", false)); if (server.equals(QINGYI)) { boss(result); result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "blocksMelee", "()Z", false)); } result.add(invoke(server, "prepare", "(L" + BOSS + ";" + LIVING + "DIID" + (server.equals(QINGYI) ? "Z" : "") + ")V")); }
        result.add(skip); return result;
    }
    private static InsnList cancellations() { InsnList result = new InsnList(); boss(result); result.add(invoke(QINGYI, "cancel", "(L" + BOSS + ";)V")); boss(result); result.add(new InsnNode(Opcodes.ICONST_1)); result.add(invoke(XUELI, "cancel", "(L" + BOSS + ";Z)V")); return result; }
    private static InsnList repair() { InsnList result = new InsnList(); boss(result); result.add(invoke(CONTINUITY, "repair", "(L" + BOSS + ";)V")); return result; }
    private static InsnList laneRanged() { InsnList result = new InsnList(); boss(result); result.add(invoke(LANES, "rangedBlocksMelee", "(ZL" + BOSS + ";)Z")); return result; }
    private static InsnList reach() { InsnList result = new InsnList(); boss(result); result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "getVariant", "()I", false)); result.add(invoke(ZICAI, "reachSquared", "(DI)D")); return result; }
    private static MethodInsnNode invoke(String owner, String name, String descriptor) { return new MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, descriptor, false); }

    private static void beforeReturns(MethodNode method, int opcode, InsnList fragment) { List<AbstractInsnNode> returns = code(method).stream().filter(instruction -> instruction.getOpcode() == opcode).toList(); if (returns.size() != 1) throw new IllegalStateException("Changed unique native basic return site: " + method.name); method.instructions.insertBefore(returns.getFirst(), fragment); }
    private static MethodNode copy(MethodNode source) { MethodNode result = new MethodNode(source.access, source.name, source.desc, source.signature, source.exceptions.toArray(String[]::new)); source.accept(result); return result; }
    private static void strip(MethodNode method, InsnList fragment) {
        List<AbstractInsnNode> expected = Arrays.stream(fragment.toArray()).filter(instruction -> instruction.getOpcode() >= 0).toList(); List<AbstractInsnNode> actual = code(method); List<Integer> matches = new ArrayList<>();
        for (int start = 0; start + expected.size() <= actual.size(); start++) { boolean same = true; for (int index = 0; index < expected.size(); index++) { AbstractInsnNode left = expected.get(index), right = actual.get(start + index); if (!operand(left).equals(operand(right))) { same = false; break; } if (left instanceof JumpInsnNode jump) { int destination = expected.indexOf(next(jump.label)); if (destination < 0) destination = expected.size(); if (actual.indexOf(next(((JumpInsnNode)right).label)) != start + destination) { same = false; break; } } } if (same) matches.add(start); }
        if (matches.size() != 1) throw new IllegalStateException("Changed precise basic hook instruction/receiver/phase: " + method.name);
        int start = matches.getFirst(); phase(method.name, actual, expected, start); for (int index = start; index < start + expected.size(); index++) method.instructions.remove(actual.get(index));
    }
    private static void phase(String name, List<AbstractInsnNode> actual, List<AbstractInsnNode> expected, int start) {
        int end = start + expected.size();
        boolean valid;
        if (Set.of("<clinit>", "defineSynchedData", "blocksMelee", "tick", "stop", "reachSqr").contains(name)) {
            int opcode = name.equals("blocksMelee") ? Opcodes.IRETURN : name.equals("reachSqr") ? Opcodes.DRETURN : Opcodes.RETURN;
            valid = end == actual.size() - 1 && actual.get(end).getOpcode() == opcode;
        } else if (name.equals("syncCombatAnimation") || name.equals("checkAndPerformAttack")) valid = start == 0;
        else if (name.equals("doHurtTarget")) {
            boolean guard = expected.stream().anyMatch(instruction -> instruction instanceof MethodInsnNode invocation && invocation.owner.equals(CONTINUITY));
            valid = guard ? start == 0 : end < actual.size() && actual.get(end) instanceof MethodInsnNode invocation && invocation.getOpcode() == Opcodes.INVOKESPECIAL && invocation.owner.equals("net/minecraft/world/entity/monster/Monster") && invocation.name.equals("doHurtTarget") && invocation.desc.equals("(" + ENTITY + ")Z");
        } else if (name.equals("canUse")) {
            boolean repair = expected.stream().anyMatch(instruction -> instruction instanceof MethodInsnNode invocation && invocation.owner.equals(CONTINUITY));
            valid = repair ? start == 0 : start > 0 && actual.get(start - 1).getOpcode() == Opcodes.BALOAD;
        } else throw new IllegalStateException("Unreviewed native basic phase method");
        if (!valid) throw new IllegalStateException("Native basic hook moved from exact original phase/site: " + name);
    }

    private static Map<String, String> preimages(ClassNode target) { Map<String, String> result = new TreeMap<>(); for (MethodNode member : selected(target)) result.put(target.name + "." + member.name + member.desc, fingerprint(member)); return result; }
    private static List<MethodNode> selected(ClassNode target) {
        requireTarget(target);
        return target.name.equals(BOSS) ? List.of(method(target, "<clinit>", "()V"), method(target, "defineSynchedData", "(L" + DATA + "$Builder;)V"), method(target, "doHurtTarget", "(" + ENTITY + ")Z"), method(target, "syncCombatAnimation", "(IZIZ)V"), method(target, "blocksMelee", "()Z"))
            : List.of(method(target, "tick", "()V"), method(target, "checkAndPerformAttack", "(" + LIVING + ")V"), method(target, "stop", "()V"), method(target, "canUse", "()Z"), method(target, "reachSqr", "()D"));
    }
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
    private static void requireTarget(ClassNode target) { if (target == null || !Set.of(BOSS, GOAL).contains(target.name)) throw new IllegalStateException("Unsupported native basic target"); }
}
