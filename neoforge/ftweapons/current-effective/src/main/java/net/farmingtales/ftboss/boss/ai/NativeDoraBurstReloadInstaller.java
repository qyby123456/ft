package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeDoraBurstReloadInstaller implements Opcodes {
    private static final String ROOT = "net/farmingtales/ftboss/";
    private static final String BOSS = ROOT + "entity/EasterBossEntity";
    private static final String PLAYER = "net/minecraft/world/entity/player/Player";
    private static final String LIVING = "net/minecraft/world/entity/LivingEntity";
    private static final String LEVEL = "net/minecraft/server/level/ServerLevel";
    private static final String MOTION = ROOT + "doramotion/DoraMotionSync";
    private static final String CYCLE = ROOT + "util/DoraSniperCycle";
    private static final Map<String,String> PREIMAGES = Map.of(
        "skillSuppress", "56738cf9ffc1011f99e10307cd25ce18e4b1649aba7fbf4fe8aa357fc8ed58c7",
        "pumpBurst", "ab1aff7d3e6cc8cf3d2280b40b5d2ec7b6adba4d0c227abe4eea33b417dfd559",
        "startReload", "ac5a91dc0b2b2f5462aa2ce2552de83998a966123e22ecb07283f08bf73ab8fa",
        "reloadTick", "f66d356e700e2b035d5daf08d04637f11a71e7d4e02c48db477339e0f73404e2",
        "finishReload", "f99d5c1beea4d7ffb1f0041f7839611063633243f1c540146b9e677b97269065"
    );
    private static final Map<String,Integer> RETURNS = Map.of("skillSuppress",2,"pumpBurst",4,"startReload",1,"reloadTick",3,"finishReload",1);

    private NativeDoraBurstReloadInstaller() {
    }

    public static void install(ClassNode owner) {
        require(owner.name.equals(BOSS), "Dora burst installer requires genuine Boss owner");
        for (String name : PREIMAGES.keySet()) {
            MethodNode method = method(owner,name);
            if (!NativeBossLivingComposerSupportV2.methodFingerprint(method).equals(PREIMAGES.get(name))) {
                verifyMethod(method);
                continue;
            }
            if (name.equals("pumpBurst")) {
                List<AbstractInsnNode> instructions = code(method);
                List<AbstractInsnNode> tracers = new ArrayList<>();
                for (int index = 0; index < instructions.size(); index++) {
                    AbstractInsnNode instruction = instructions.get(index);
                    if (instruction instanceof FieldInsnNode field && field.getOpcode() == GETSTATIC && field.owner.equals(ROOT + "util/WeaponFxRules$Kind") && field.name.equals("DORA_TRACER")) {
                        List<AbstractInsnNode> sends = instructions.subList(index + 1,Math.min(index + 12,instructions.size())).stream().filter(value -> value instanceof MethodInsnNode member && member.owner.equals(ROOT + "skill/WeaponFx") && member.name.equals("send")).toList();
                        require(sends.size() == 1,"Exact original native tracer send anchor");
                        tracers.add(sends.getFirst());
                    }
                }
                require(tracers.size() == 1,"One native Dora tracer site");
                method.instructions.insert(tracers.getFirst(),burstHooks());
                broaden(method,false);
            }
            List<AbstractInsnNode> returns = code(method).stream().filter(value -> value.getOpcode() == RETURN).toList();
            require(returns.size() == RETURNS.get(name),"Exact native Dora terminal branch inventory: " + name);
            for (AbstractInsnNode terminal : returns) method.instructions.insertBefore(terminal,capture());
            method.maxStack = Math.max(method.maxStack,12);
            verifyMethod(method);
        }
    }

    public static void verifyInstalled(ClassNode owner) {
        require(owner.name.equals(BOSS),"Unexpected Dora burst installer verification owner");
        for (String name : PREIMAGES.keySet()) verifyMethod(method(owner,name));
    }

    private static void verifyMethod(MethodNode source) {
        MethodNode restored = NativeBossLivingComposerSupportV2.copy(source);
        List<AbstractInsnNode> returns = code(restored).stream().filter(value -> value.getOpcode() == RETURN).toList();
        require(returns.size() == RETURNS.get(source.name),"Installed Dora terminal branch count: " + source.name);
        for (AbstractInsnNode terminal : returns) removeBefore(restored,terminal,capture());
        if (source.name.equals("pumpBurst")) {
            List<AbstractInsnNode> shots = code(restored).stream().filter(value -> value instanceof MethodInsnNode member && member.owner.equals(MOTION) && member.name.equals("shot")).toList();
            require(shots.size() == 1,"Exact installed original Dora shot site");
            removeThrough(restored,shots.getFirst(),burstHooks());
            broaden(restored,true);
        }
        require(code(restored).stream().noneMatch(value -> value instanceof MethodInsnNode member && (member.owner.equals(MOTION) || member.owner.equals(ROOT + "balance/ManorExpansionHooks") && member.name.equals("bullet") || member.owner.equals(ROOT + "combat/dorabasic/DoraBasicCue") && member.name.equals("stamp"))),"Unaccounted original Dora hook site");
        require(NativeBossLivingComposerSupportV2.methodFingerprint(restored).equals(PREIMAGES.get(source.name)),"Whole frozen native Dora body drift beyond exact original hooks/living feedback: " + source.name);
    }

    private static void broaden(MethodNode method,boolean reverse) {
        String from = reverse ? LIVING : PLAYER;
        String to = reverse ? PLAYER : LIVING;
        int types = 0;
        int calls = 0;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof TypeInsnNode type && type.desc.equals(from)) {
                require(type.getOpcode() == INSTANCEOF || type.getOpcode() == CHECKCAST,"Exact Dora victim type operation");
                type.desc = to;
                types++;
            }
            if (instruction instanceof MethodInsnNode member && member.owner.equals(BOSS) && (member.name.equals("doraKnockback") || member.name.equals("doraHit")) && member.desc.contains("L" + from + ";")) {
                member.desc = member.desc.replace("L" + from + ";","L" + to + ";");
                calls++;
            }
        }
        require(types == 2 && calls == 2,"Exact two Dora victim type and two feedback dispatch adaptations");
    }

    private static InsnList capture() {
        InsnList result = new InsnList();
        result.add(new VarInsnNode(ALOAD,0));
        for (String name : List.of("burstShots","reloadTicks","doraReloadProgress")) {
            result.add(new VarInsnNode(ALOAD,0));
            result.add(new FieldInsnNode(GETFIELD,BOSS,name,name.equals("doraReloadProgress") ? "D" : "I"));
        }
        result.add(new VarInsnNode(ALOAD,0));
        result.add(new FieldInsnNode(GETFIELD,BOSS,"doraSniper","L" + CYCLE + ";"));
        result.add(new MethodInsnNode(INVOKEVIRTUAL,CYCLE,"busy","()Z",false));
        result.add(new MethodInsnNode(INVOKESTATIC,MOTION,"capture","(L" + BOSS + ";IIDZ)V",false));
        return result;
    }

    private static InsnList burstHooks() {
        InsnList result = new InsnList();
        result.add(new VarInsnNode(ALOAD,0));
        result.add(new VarInsnNode(ALOAD,1));
        result.add(new VarInsnNode(ALOAD,2));
        result.add(new MethodInsnNode(INVOKESTATIC,ROOT + "balance/ManorExpansionHooks","bullet","(L" + BOSS + ";L" + LEVEL + ";L" + LIVING + ";)V",false));
        result.add(new VarInsnNode(ALOAD,0));
        result.add(new FieldInsnNode(GETFIELD,BOSS,"entityData","Lnet/minecraft/network/syncher/SynchedEntityData;"));
        result.add(new FieldInsnNode(GETSTATIC,BOSS,"DATA_SKILL_CUE","Lnet/minecraft/network/syncher/EntityDataAccessor;"));
        result.add(new VarInsnNode(ALOAD,0));
        result.add(new MethodInsnNode(INVOKEVIRTUAL,BOSS,"getSkillCue","()Lnet/minecraft/nbt/CompoundTag;",false));
        result.add(new InsnNode(ICONST_0));
        result.add(new VarInsnNode(ALOAD,0));
        result.add(new MethodInsnNode(INVOKEVIRTUAL,BOSS,"level","()Lnet/minecraft/world/level/Level;",false));
        result.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/world/level/Level","getGameTime","()J",false));
        result.add(new VarInsnNode(ALOAD,0));
        result.add(new MethodInsnNode(INVOKEVIRTUAL,BOSS,"getVariant","()I",false));
        result.add(new MethodInsnNode(INVOKESTATIC,ROOT + "combat/dorabasic/DoraBasicCue","stamp","(Lnet/minecraft/nbt/CompoundTag;IJI)Lnet/minecraft/nbt/CompoundTag;",false));
        result.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/network/syncher/SynchedEntityData","set","(Lnet/minecraft/network/syncher/EntityDataAccessor;Ljava/lang/Object;)V",false));
        result.add(new VarInsnNode(ALOAD,0));
        result.add(new MethodInsnNode(INVOKESTATIC,MOTION,"shot","(L" + BOSS + ";)V",false));
        return result;
    }

    private static MethodNode method(ClassNode owner,String name) {
        String descriptor = switch (name) {
            case "skillSuppress" -> "(L" + LEVEL + ";L" + LIVING + ";)V";
            case "startReload", "finishReload" -> "(L" + LEVEL + ";)V";
            default -> "()V";
        };
        List<MethodNode> methods = owner.methods.stream().filter(value -> value.name.equals(name) && value.desc.equals(descriptor)).toList();
        require(methods.size() == 1,"Missing or duplicate native Dora method: " + name);
        return methods.getFirst();
    }

    private static List<AbstractInsnNode> code(MethodNode method) {
        List<AbstractInsnNode> result = new ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions) if (instruction.getOpcode() >= 0) result.add(instruction);
        return result;
    }

    private static void removeBefore(MethodNode method,AbstractInsnNode anchor,InsnList expected) {
        AbstractInsnNode last = anchor.getPrevious();
        while (last != null && last.getOpcode() < 0) last = last.getPrevious();
        removeThrough(method,last,expected);
    }

    private static void removeThrough(MethodNode method,AbstractInsnNode last,InsnList expected) {
        List<AbstractInsnNode> wanted = new ArrayList<>();
        for (AbstractInsnNode instruction : expected) wanted.add(instruction);
        List<AbstractInsnNode> remove = new ArrayList<>();
        AbstractInsnNode actual = last;
        for (int index = wanted.size() - 1; index >= 0; index--) {
            while (actual != null && actual.getOpcode() < 0) actual = actual.getPrevious();
            require(actual != null && same(actual,wanted.get(index)),"Missing exact installed Dora hook operand: " + method.name + ":" + index);
            remove.add(actual);
            actual = actual.getPrevious();
        }
        remove.forEach(method.instructions::remove);
    }

    private static boolean same(AbstractInsnNode actual,AbstractInsnNode expected) {
        if (actual.getOpcode() != expected.getOpcode()) return false;
        if (expected instanceof VarInsnNode variable) return actual instanceof VarInsnNode other && other.var == variable.var;
        if (expected instanceof FieldInsnNode field) return actual instanceof FieldInsnNode other && other.owner.equals(field.owner) && other.name.equals(field.name) && other.desc.equals(field.desc);
        if (expected instanceof MethodInsnNode member) return actual instanceof MethodInsnNode other && other.owner.equals(member.owner) && other.name.equals(member.name) && other.desc.equals(member.desc) && other.itf == member.itf;
        return expected instanceof InsnNode;
    }

    private static void require(boolean condition,String reason) {
        if (!condition) throw new IllegalStateException(reason);
    }
}
