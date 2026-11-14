package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeManorDefenseInstaller {
    private static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String DEFENSE = "net/farmingtales/ftboss/boss/ai/ManorDefense";
    private static final String ADAPTIVE = "net/farmingtales/ftboss/boss/ai/ManorAdaptiveShield";
    private static final String ACCESS = "net/farmingtales/ftboss/boss/ai/NativeManorDefenseAccess";
    private static final String MAIDS = "net/farmingtales/ftboss/boss/ai/XueliMaids";
    private static final String SOURCE = "Lnet/minecraft/world/damagesource/DamageSource;";
    private static final String REDUCE = "(FL" + BOSS + ";" + SOURCE + ")F";

    private NativeManorDefenseInstaller() {
    }

    public static int install(ClassNode target) {
        requireTarget(target);
        int present = count(target);
        if (present != 0) {
            verifyInstalled(target);
            return 0;
        }
        MethodNode feedback = method(target,"hitFeedback","(" + SOURCE + ")V");
        if (target.interfaces.contains(ACCESS) || target.methods.stream().anyMatch(method -> method.name.equals("isManorMaidShielded")) || target.fields.stream().filter(field -> field.name.equals("xueliMaids") && field.desc.equals("L" + MAIDS + ";") && (field.access & Opcodes.ACC_STATIC) == 0).count() != 1) throw new IllegalStateException("Changed actual native maid-shield field/accessor scope");
        MethodNode hurt = method(target,"hurt","(" + SOURCE + "F)Z");
        MethodNode manor = method(target,"manorHurt","(" + SOURCE + "F)Z");
        List<AbstractInsnNode> normal = code(hurt);
        List<MethodInsnNode> fallbacks = normal.stream().filter(instruction -> instruction instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESPECIAL && call.owner.equals("net/minecraft/world/entity/monster/Monster") && call.name.equals("hurt") && call.desc.equals("(" + SOURCE + "F)Z")).map(instruction -> (MethodInsnNode)instruction).toList();
        if (fallbacks.size() != 1) throw new IllegalStateException("Missing unique original native superclass damage fallback");
        MethodInsnNode fallback = fallbacks.getFirst();
        int index = normal.indexOf(fallback);
        if (!(normal.get(index - 1) instanceof MethodInsnNode share) || !share.name.equals("sparrowShare") || !share.owner.equals(BOSS) || !share.desc.equals("(F" + SOURCE + ")F")) throw new IllegalStateException("Changed original native fallback sharing order");
        List<AbstractInsnNode> managed = code(manor);
        List<MethodInsnNode> absorption = managed.stream().filter(instruction -> instruction instanceof MethodInsnNode call && call.owner.equals(BOSS) && call.name.equals("getAbsorptionAmount") && call.desc.equals("()F")).map(instruction -> (MethodInsnNode)instruction).toList();
        if (absorption.size() != 1) throw new IllegalStateException("Missing original unique native post-sharing absorption read");
        int position = managed.indexOf(absorption.getFirst());
        if (!(managed.get(position - 1) instanceof VarInsnNode self) || self.var != 0 || self.getOpcode() != Opcodes.ALOAD
            || !(managed.get(position + 1) instanceof VarInsnNode store) || store.var != 4 || store.getOpcode() != Opcodes.FSTORE) throw new IllegalStateException("Changed native damage/absorption local layout");
        InsnList hit = new InsnList();
        hit.add(new VarInsnNode(Opcodes.ALOAD,0)); hit.add(new VarInsnNode(Opcodes.ALOAD,1)); hit.add(new MethodInsnNode(Opcodes.INVOKESTATIC,DEFENSE,"confirmedHit","(L" + BOSS + ";" + SOURCE + ")V",false));
        feedback.instructions.insert(hit);
        InsnList ordinary = new InsnList();
        ordinary.add(new VarInsnNode(Opcodes.ALOAD,0)); ordinary.add(new VarInsnNode(Opcodes.ALOAD,1)); ordinary.add(new MethodInsnNode(Opcodes.INVOKESTATIC,DEFENSE,"reduceAfterSharing",REDUCE,false));
        hurt.instructions.insertBefore(fallback,ordinary);
        InsnList direct = new InsnList();
        direct.add(new VarInsnNode(Opcodes.ALOAD,0)); direct.add(new VarInsnNode(Opcodes.ALOAD,1)); direct.add(new VarInsnNode(Opcodes.FLOAD,3));
        direct.add(new MethodInsnNode(Opcodes.INVOKESTATIC,ADAPTIVE,"reduce","(Lnet/minecraft/world/entity/LivingEntity;" + SOURCE + "F)F",false));
        direct.add(new VarInsnNode(Opcodes.ALOAD,0)); direct.add(new VarInsnNode(Opcodes.ALOAD,1)); direct.add(new MethodInsnNode(Opcodes.INVOKESTATIC,DEFENSE,"reduceAfterSharing",REDUCE,false)); direct.add(new VarInsnNode(Opcodes.FSTORE,3));
        manor.instructions.insertBefore(managed.get(position - 1),direct);
        feedback.maxStack += 2; hurt.maxStack += 2; manor.maxStack += 3;
        target.interfaces.add(ACCESS);
        MethodNode shield = new MethodNode(Opcodes.ACC_PUBLIC,"isManorMaidShielded","()Z",null,null);
        shield.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
        shield.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,BOSS,"xueliMaids","L" + MAIDS + ";"));
        shield.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,MAIDS,"shielded","()Z",false));
        shield.instructions.add(new InsnNode(Opcodes.IRETURN));
        shield.maxStack = 1; shield.maxLocals = 1; target.methods.add(shield);
        verifyInstalled(target);
        return 3;
    }

    public static void verifyInstalled(ClassNode target) {
        requireTarget(target);
        boolean finalDamage = NativeManorFinalDamageInstaller.isInstalled(target);
        if (count(target) != (finalDamage ? 2 : 4)) throw new IllegalStateException("Missing or duplicate native defense/adaptive hooks");
        if (target.interfaces.stream().filter(name -> name.equals(ACCESS)).count() != 1) throw new IllegalStateException("Missing actual native maid-shield access interface");
        List<AbstractInsnNode> shield = code(method(target,"isManorMaidShielded","()Z"));
        if (shield.size() != 4 || !variable(shield.get(0),Opcodes.ALOAD,0) || !(shield.get(1) instanceof FieldInsnNode field) || field.getOpcode() != Opcodes.GETFIELD || !field.owner.equals(BOSS) || !field.name.equals("xueliMaids") || !field.desc.equals("L" + MAIDS + ";") || !(shield.get(2) instanceof MethodInsnNode read) || read.getOpcode() != Opcodes.INVOKEVIRTUAL || !read.owner.equals(MAIDS) || !read.name.equals("shielded") || !read.desc.equals("()Z") || shield.get(3).getOpcode() != Opcodes.IRETURN) throw new IllegalStateException("Changed real native maid-shield accessor");
        MethodNode feedback = method(target,"hitFeedback","(" + SOURCE + ")V");
        MethodNode hurt = method(target,"hurt","(" + SOURCE + "F)Z");
        MethodNode manor = method(target,"manorHurt","(" + SOURCE + "F)Z");
        List<AbstractInsnNode> hit = code(feedback);
        int hitOffset=0;
        if(hit.size()>=3 && call(hit.get(2),"net/farmingtales/ftboss/boss/ai/SparrowHitEvasion","confirmedHit","(L"+BOSS+";"+SOURCE+")V")) {
            NativeSparrowHitEvasionInstaller.verifyInstalled(target);
            hitOffset=3;
        }
        if (hit.size()<hitOffset+3 || !variable(hit.get(hitOffset),Opcodes.ALOAD,0) || !variable(hit.get(hitOffset+1),Opcodes.ALOAD,1) || !call(hit.get(hitOffset+2),DEFENSE,"confirmedHit","(L" + BOSS + ";" + SOURCE + ")V")) throw new IllegalStateException("Changed native confirmed-hit bridge arguments");
        List<AbstractInsnNode> fallback = code(hurt);
        int normal = locate(fallback,DEFENSE,"reduceAfterSharing",REDUCE);
        if (!variable(fallback.get(normal - 2),Opcodes.ALOAD,0) || !variable(fallback.get(normal - 1),Opcodes.ALOAD,1) || !(fallback.get(normal - 3) instanceof MethodInsnNode share) || !share.name.equals("sparrowShare")
            || !(fallback.get(normal + 1) instanceof MethodInsnNode parent) || parent.getOpcode() != Opcodes.INVOKESPECIAL || !parent.name.equals("hurt")) throw new IllegalStateException("Changed native fallback defense phase");
        if (finalDamage) {
            NativeManorFinalDamageInstaller.verifyInstalled(target);
            return;
        }
        List<AbstractInsnNode> direct = code(manor);
        int adaptive = locate(direct,ADAPTIVE,"reduce","(Lnet/minecraft/world/entity/LivingEntity;" + SOURCE + "F)F");
        int defense = locate(direct,DEFENSE,"reduceAfterSharing",REDUCE);
        if (defense != adaptive + 3 || !variable(direct.get(adaptive - 3),Opcodes.ALOAD,0) || !variable(direct.get(adaptive - 2),Opcodes.ALOAD,1) || !variable(direct.get(adaptive - 1),Opcodes.FLOAD,3)
            || !variable(direct.get(adaptive + 1),Opcodes.ALOAD,0) || !variable(direct.get(adaptive + 2),Opcodes.ALOAD,1) || !variable(direct.get(defense + 1),Opcodes.FSTORE,3)
            || !variable(direct.get(defense + 2),Opcodes.ALOAD,0) || !(direct.get(defense + 3) instanceof MethodInsnNode absorptionRead) || !absorptionRead.name.equals("getAbsorptionAmount")) throw new IllegalStateException("Changed actual post-sharing pre-absorption adaptive/defense phase");
    }

    private static void requireTarget(ClassNode target) {
        if (target == null || !target.name.equals(BOSS)) throw new IllegalStateException("Unsupported native defense target");
    }

    private static MethodNode method(ClassNode target,String name,String descriptor) {
        List<MethodNode> methods = target.methods.stream().filter(method -> method.name.equals(name) && method.desc.equals(descriptor) && (method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0).toList();
        if (methods.size() != 1) throw new IllegalStateException("Missing actual native defense method " + name);
        return methods.getFirst();
    }

    private static int count(ClassNode target) {
        int result = 0;
        for (MethodNode method : target.methods) for (AbstractInsnNode instruction : method.instructions) if (instruction instanceof MethodInsnNode call && (call.owner.equals(DEFENSE) || call.owner.equals(ADAPTIVE))) result++;
        return result;
    }

    private static int locate(List<AbstractInsnNode> code,String owner,String name,String descriptor) {
        List<AbstractInsnNode> matches = code.stream().filter(instruction -> call(instruction,owner,name,descriptor)).toList();
        if (matches.size() != 1) throw new IllegalStateException("Missing unique exact native defense hook");
        return code.indexOf(matches.getFirst());
    }

    private static boolean call(AbstractInsnNode instruction,String owner,String name,String descriptor) {
        return instruction instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESTATIC && call.owner.equals(owner) && call.name.equals(name) && call.desc.equals(descriptor);
    }

    private static boolean variable(AbstractInsnNode instruction,int opcode,int slot) {
        return instruction instanceof VarInsnNode variable && variable.getOpcode() == opcode && variable.var == slot;
    }

    private static List<AbstractInsnNode> code(MethodNode method) {
        List<AbstractInsnNode> result = new ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions) if (instruction.getOpcode() >= 0) result.add(instruction);
        return result;
    }
}
