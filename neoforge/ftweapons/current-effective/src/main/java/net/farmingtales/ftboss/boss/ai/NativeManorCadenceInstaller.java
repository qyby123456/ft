package net.farmingtales.ftboss.boss.ai;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeManorCadenceInstaller {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static final String BOSS = PREFIX + "entity/EasterBossEntity";
    private static final String GOAL = BOSS + "$PlayerReachMeleeGoal";
    private static final String CADENCE = PREFIX + "xueli_multiplayer/CadenceServer";
    private static final String ZICAI = PREFIX + "util/ZicaiCombatRules";
    private static final String TIMING = PREFIX + "util/ManorMeleeTiming";
    private static final String DIFFICULTY = PREFIX + "boss/ManorDifficulty";
    private static final String DATA = "net/minecraft/network/syncher/SynchedEntityData";
    private static final String ACCESSOR = "Lnet/minecraft/network/syncher/EntityDataAccessor;";
    private static final String TAG = "net/minecraft/nbt/CompoundTag";
    private static final Map<String, String> EXPECTED = Map.ofEntries(
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity$PlayerReachMeleeGoal.getAttackInterval()I", "9f9f874af942f74039ff4c16249f6d902f9a7e21ea633d06d65ef9ef1d64b881"),
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity.doHurtTarget(Lnet/minecraft/world/entity/Entity;)Z", "428ee5607332680e3b06aa6b5bd0e7fdc0d120bd5c0acc1d5ffa2a136ee4a7ac"),
        Map.entry("net/farmingtales/ftboss/entity/EasterBossEntity.tick()V", "d5bb41f4a11cc7d246b973b65ad15f56554c47cdab2dfc347c2e94d30c532b34")
    );

    private NativeManorCadenceInstaller() {
    }

    public static int install(ClassNode target) {
        requireTarget(target);
        boolean present = target.methods.stream().anyMatch(member -> member.name.equals("getMeleeAttackInterval") || code(member).stream().anyMatch(instruction -> instruction instanceof MethodInsnNode invocation && invocation.owner.equals(CADENCE)));
        if (present) { verifyInstalled(target); return 0; }
        for (MethodNode member : selected(target)) if (!fingerprint(member).equals(EXPECTED.get(target.name + "." + member.name + member.desc))) throw new IllegalStateException("Changed complete native cadence preimage: " + member.name);
        if (target.name.equals(BOSS)) {
            for (String name : List.of("armMeleeAnimationDisabled", "playMeleeImpactAction", "getMeleeAttackInterval")) if (target.methods.stream().anyMatch(member -> member.name.equals(name))) throw new IllegalStateException("Partial native cadence methods");
            target.methods.add(disabled()); target.methods.add(interval()); target.methods.add(impact());
            MethodNode hurt = method(target, "doHurtTarget", "(Lnet/minecraft/world/entity/Entity;)Z");
            List<AbstractInsnNode> before = code(hurt); InsnList legacy = legacyImpact(new LabelNode()); int start = find(hurt, legacy);
            if (start != 163) throw new IllegalStateException("Changed native post-hit impact location");
            LabelNode exit = ((JumpInsnNode)before.get(start + 1)).label;
            hurt.instructions.insertBefore(before.get(start), impactCall(exit));
            for (int index = start; index < start + code(legacy).size(); index++) hurt.instructions.remove(before.get(index));
            MethodNode tick = method(target, "tick", "()V");
            List<AbstractInsnNode> sites = code(tick).stream().filter(instruction -> instruction instanceof MethodInsnNode invocation && invocation.owner.equals(PREFIX + "util/ManorBossCombatRules") && invocation.name.equals("skillCooldown") && invocation.desc.equals("(II)I")).toList();
            if (sites.size() != 1) throw new IllegalStateException("Missing exact native skill cooldown computation");
            tick.instructions.insert(sites.getFirst(), skill());
        } else {
            MethodNode member = method(target, "getAttackInterval", "()I");
            List<AbstractInsnNode> returns = code(member).stream().filter(instruction -> instruction.getOpcode() == Opcodes.IRETURN).toList();
            if (returns.size() != 1) throw new IllegalStateException("Changed native accelerated interval return");
            member.instructions.insertBefore(returns.getFirst(), cadence());
        }
        for (MethodNode member : selected(target)) { for (AbstractInsnNode instruction : member.instructions.toArray()) if (instruction instanceof FrameNode) member.instructions.remove(instruction); member.maxStack += 8; }
        verifyInstalled(target);
        return target.name.equals(BOSS) ? 2 : 1;
    }

    public static void verifyInstalled(ClassNode target) {
        requireTarget(target);
        if (target.name.equals(BOSS)) for (MethodNode expected : List.of(disabled(), interval(), impact())) {
            MethodNode actual = method(target, expected.name, expected.desc);
            if (actual.access != expected.access || !fingerprint(actual).equals(fingerprint(expected))) throw new IllegalStateException("Changed complete original cadence/impact method: " + actual.name);
        }
        for (MethodNode member : selected(target)) if (!fingerprint(restored(target.name, member)).equals(EXPECTED.get(target.name + "." + member.name + member.desc))) throw new IllegalStateException("Changed original native method outside exact cadence/impact sites: " + member.name);
    }

    private static MethodNode restored(String owner, MethodNode member) {
        MethodNode result = copy(member);
        if (member.name.equals("doHurtTarget")) {
            InsnList pattern = impactCall(new LabelNode()); List<AbstractInsnNode> actual = code(result); int start = find(result, pattern);
            if (start != 163) throw new IllegalStateException("Moved complete post-hit impact hook");
            LabelNode exit = ((JumpInsnNode)actual.get(start + 1)).label; result.instructions.insertBefore(actual.get(start), legacyImpact(exit));
            for (int index = start; index < start + code(pattern).size(); index++) result.instructions.remove(actual.get(index));
        } else {
            InsnList pattern = member.name.equals("tick") ? skill() : cadence(); List<AbstractInsnNode> actual = code(result); int start = find(result, pattern); int end = start + code(pattern).size();
            if (member.name.equals("tick")) {
                if (start <= 0 || !(actual.get(start - 1) instanceof MethodInsnNode invocation) || !invocation.owner.equals(PREFIX + "util/ManorBossCombatRules") || !invocation.name.equals("skillCooldown") || !invocation.desc.equals("(II)I") || end >= actual.size() || !(actual.get(end) instanceof FieldInsnNode field) || field.getOpcode() != Opcodes.PUTFIELD || !field.owner.equals(BOSS) || !field.name.equals("skillCd") || !field.desc.equals("I")) throw new IllegalStateException("Moved original skill cooldown wrapper");
            } else if (end != actual.size() - 1 || actual.get(end).getOpcode() != Opcodes.IRETURN) throw new IllegalStateException("Moved native accelerated melee cadence return");
            for (int index = start; index < end; index++) result.instructions.remove(actual.get(index));
        }
        return result;
    }

    private static InsnList skill() { InsnList result = new InsnList(); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CADENCE, "skill", "(IL" + BOSS + ";)I", false)); return result; }
    private static InsnList cadence() {
        InsnList result = new InsnList(); result.add(new InsnNode(Opcodes.ICONST_1)); result.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Math", "max", "(II)I", false)); goalBoss(result); result.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CADENCE, "melee", "(IL" + BOSS + ";)I", false)); goalBoss(result); result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "getVariant", "()I", false)); result.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ZICAI, "interval", "(II)I", false)); return result;
    }
    private static void goalBoss(InsnList result) { result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new FieldInsnNode(Opcodes.GETFIELD, GOAL, "boss", "L" + BOSS + ";")); }
    private static InsnList impactCall(LabelNode exit) { InsnList result = new InsnList(); result.add(new VarInsnNode(Opcodes.ILOAD, 3)); result.add(new JumpInsnNode(Opcodes.IFEQ, exit)); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "playMeleeImpactAction", "()V", false)); return result; }
    private static InsnList legacyImpact(LabelNode exit) {
        LabelNode hit = new LabelNode(); InsnList result = new InsnList(); result.add(new VarInsnNode(Opcodes.ILOAD, 3)); result.add(new JumpInsnNode(Opcodes.IFEQ, exit)); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new FieldInsnNode(Opcodes.GETFIELD, BOSS, "actionTicks", "I")); result.add(new JumpInsnNode(Opcodes.IFGT, exit)); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new FieldInsnNode(Opcodes.GETFIELD, BOSS, "castTicks", "I")); result.add(new JumpInsnNode(Opcodes.IFGT, exit)); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "allowsParallelMelee", "()Z", false)); result.add(new JumpInsnNode(Opcodes.IFEQ, hit)); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "isCastingLike", "()Z", false)); result.add(new JumpInsnNode(Opcodes.IFNE, exit)); result.add(hit); result.add(new VarInsnNode(Opcodes.ALOAD, 0)); result.add(new IntInsnNode(Opcodes.BIPUSH, 14)); result.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "playImpactAction", "(I)V", false)); return result;
    }

    private static MethodNode disabled() { MethodNode result = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, "armMeleeAnimationDisabled", "()Z", null, null); result.instructions.add(new InsnNode(Opcodes.ICONST_1)); result.instructions.add(new InsnNode(Opcodes.IRETURN)); result.maxStack = 1; return result; }
    private static MethodNode interval() {
        MethodNode result = new MethodNode(Opcodes.ACC_PUBLIC, "getMeleeAttackInterval", "()I", null, null); InsnList body = result.instructions; LabelNode normal = new LabelNode(), fallback = new LabelNode(), store = new LabelNode();
        body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "getVariant", "()I", false)); body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, PREFIX + "util/ManorBossCombatRules", "meleeInterval", "(I)I", false)); body.add(new VarInsnNode(Opcodes.ISTORE, 1)); body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "allowsParallelMelee", "()Z", false)); body.add(new JumpInsnNode(Opcodes.IFEQ, normal)); body.add(new IntInsnNode(Opcodes.BIPUSH, 10)); body.add(new JumpInsnNode(Opcodes.GOTO, store)); body.add(normal); body.add(new VarInsnNode(Opcodes.ILOAD, 1)); body.add(new JumpInsnNode(Opcodes.IFLE, fallback)); body.add(new VarInsnNode(Opcodes.ILOAD, 1)); body.add(new JumpInsnNode(Opcodes.GOTO, store)); body.add(fallback); body.add(new IntInsnNode(Opcodes.BIPUSH, 20)); body.add(store); body.add(new VarInsnNode(Opcodes.ISTORE, 2)); body.add(new InsnNode(Opcodes.ICONST_1)); body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "getChallengeDifficulty", "()L" + DIFFICULTY + ";", false)); body.add(new VarInsnNode(Opcodes.ILOAD, 2)); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DIFFICULTY, "acceleratedTicks", "(I)I", false)); body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Math", "max", "(II)I", false)); body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CADENCE, "melee", "(IL" + BOSS + ";)I", false)); body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "getVariant", "()I", false)); body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ZICAI, "interval", "(II)I", false)); body.add(new InsnNode(Opcodes.IRETURN)); result.maxStack = 4; result.maxLocals = 3; return result;
    }
    private static MethodNode impact() {
        MethodNode result = new MethodNode(Opcodes.ACC_PRIVATE, "playMeleeImpactAction", "()V", null, null); InsnList body = result.instructions; LabelNode exit = new LabelNode();
        body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BOSS, "armMeleeAnimationDisabled", "()Z", false)); body.add(new JumpInsnNode(Opcodes.IFNE, exit)); state(body); body.add(new VarInsnNode(Opcodes.ASTORE, 1)); body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new FieldInsnNode(Opcodes.GETFIELD, BOSS, "actionTicks", "I")); body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new FieldInsnNode(Opcodes.GETFIELD, BOSS, "castTicks", "I")); body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "isCastingLike", "()Z", false)); body.add(new VarInsnNode(Opcodes.ALOAD, 1)); body.add(new LdcInsnNode("melee")); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, TAG, "getBoolean", "(Ljava/lang/String;)Z", false)); body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, TIMING, "canRestart", "(IIZZ)Z", false)); body.add(new JumpInsnNode(Opcodes.IFEQ, exit)); body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "getMeleeAttackInterval", "()I", false)); body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "isImpossibleChallenge", "()Z", false)); body.add(new MethodInsnNode(Opcodes.INVOKESTATIC, TIMING, "recoveryTicks", "(IZ)I", false)); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "playImpactAction", "(I)V", false)); state(body); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, TAG, "copy", "()L" + TAG + ";", false)); body.add(new VarInsnNode(Opcodes.ASTORE, 1)); body.add(new VarInsnNode(Opcodes.ALOAD, 1)); body.add(new LdcInsnNode("melee")); body.add(new InsnNode(Opcodes.ICONST_1)); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, TAG, "putBoolean", "(Ljava/lang/String;Z)V", false)); body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new FieldInsnNode(Opcodes.GETFIELD, BOSS, "entityData", "L" + DATA + ";")); body.add(new FieldInsnNode(Opcodes.GETSTATIC, BOSS, "DATA_COMBAT_ANIMATION", ACCESSOR)); body.add(new VarInsnNode(Opcodes.ALOAD, 1)); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DATA, "set", "(" + ACCESSOR + "Ljava/lang/Object;)V", false)); body.add(exit); body.add(new InsnNode(Opcodes.RETURN)); result.maxStack = 5; result.maxLocals = 2; return result;
    }
    private static void state(InsnList body) { body.add(new VarInsnNode(Opcodes.ALOAD, 0)); body.add(new FieldInsnNode(Opcodes.GETFIELD, BOSS, "entityData", "L" + DATA + ";")); body.add(new FieldInsnNode(Opcodes.GETSTATIC, BOSS, "DATA_COMBAT_ANIMATION", ACCESSOR)); body.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, DATA, "get", "(" + ACCESSOR + ")Ljava/lang/Object;", false)); body.add(new TypeInsnNode(Opcodes.CHECKCAST, TAG)); }

    private static int find(MethodNode member, InsnList fragment) {
        List<AbstractInsnNode> actual = code(member), expected = code(fragment); List<Integer> matches = new ArrayList<>();
        for (int start = 0; start + expected.size() <= actual.size(); start++) {
            boolean valid = true;
            for (int offset = 0; offset < expected.size(); offset++) {
                AbstractInsnNode left = actual.get(start + offset), right = expected.get(offset);
                if (!operand(left).equals(operand(right))) { valid = false; break; }
                if (right instanceof JumpInsnNode jump) { int destination = expected.indexOf(next(jump.label)); if (destination < 0) destination = expected.size(); if (actual.indexOf(next(((JumpInsnNode)left).label)) != start + destination) { valid = false; break; } }
            }
            if (valid) matches.add(start);
        }
        if (matches.size() != 1) throw new IllegalStateException("Changed complete native cadence fragment: " + member.name);
        return matches.getFirst();
    }
    private static MethodNode copy(MethodNode source) { MethodNode result = new MethodNode(source.access, source.name, source.desc, source.signature, source.exceptions.toArray(String[]::new)); source.accept(result); return result; }
    private static List<MethodNode> selected(ClassNode target) { requireTarget(target); return target.name.equals(BOSS) ? List.of(method(target, "doHurtTarget", "(Lnet/minecraft/world/entity/Entity;)Z"), method(target, "tick", "()V")) : List.of(method(target, "getAttackInterval", "()I")); }
    private static List<AbstractInsnNode> code(InsnList body) { return Arrays.stream(body.toArray()).filter(instruction -> instruction.getOpcode() >= 0).toList(); }
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
    private static void requireTarget(ClassNode target) { if (target == null || !Set.of(BOSS, GOAL).contains(target.name)) throw new IllegalStateException("Unsupported native cadence target"); }
}
