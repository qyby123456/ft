package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeShadowStrikeStartInstallerV3 implements Opcodes {
    private static final String ROOT="net/farmingtales/ftboss/";
    private static final String BOSS=ROOT+"entity/EasterBossEntity";
    private static final String LIVING="net/minecraft/world/entity/LivingEntity";
    private static final String LEVEL="net/minecraft/server/level/ServerLevel";
    private static final String DESC="(L"+LEVEL+";L"+LIVING+";)V";
    private static final String PREIMAGE="38e88321052628347b9dfa5a78297fab40a4390aa83a91ca5ec0321ea3ca1be8";

    private NativeShadowStrikeStartInstallerV3() {
    }

    public static void install(ClassNode owner) {
        MethodNode method=method(owner);
        if(!NativeBossLivingComposerSupportV2.methodFingerprint(method).equals(PREIMAGE)) {
            verifyMethod(method);
            return;
        }
        MethodInsnNode guard=one(method,INVOKEVIRTUAL,BOSS,"canTeleportWith","(L"+LIVING+";)Z");
        guard.name="canShadowStrike";
        List<MethodInsnNode> windups=calls(method,INVOKEVIRTUAL,ROOT+"boss/ManorDifficulty","acceleratedTicks","(I)I");
        require(windups.size()==2,"Exact two original native strike windup sites");
        for(MethodInsnNode windup:windups)method.instructions.insert(windup,readability());
        MethodInsnNode teleport=one(method,INVOKEVIRTUAL,BOSS,"randomTeleport","(DDDZ)Z");
        teleport.setOpcode(INVOKESTATIC);
        teleport.owner=ROOT+"zheergenfx/LightHooks";
        teleport.name="teleport";
        teleport.desc="(L"+BOSS+";DDDZ)Z";
        List<FieldInsnNode> targets=code(method).stream().filter(value->value instanceof FieldInsnNode field&&field.getOpcode()==PUTFIELD&&field.owner.equals(BOSS)&&field.name.equals("shadowStrikeTarget")&&field.desc.equals("Ljava/util/UUID;")).map(value->(FieldInsnNode)value).toList();
        require(targets.size()==1,"Exact original native target UUID assignment");
        AbstractInsnNode uuid=previous(targets.getFirst()),target=previous(uuid),self=previous(target);
        require(call(uuid,INVOKEVIRTUAL,LIVING,"getUUID","()Ljava/util/UUID;")&&load(target,ALOAD,2)&&load(self,ALOAD,0),"Entire original post-particle-loop target assignment anchor");
        method.instructions.insertBefore(self,phantom());
        MethodInsnNode cue=one(method,INVOKEVIRTUAL,BOSS,"beginSkillCue","(Lnet/minecraft/world/phys/Vec3;DIZ)V");
        method.instructions.insert(cue,startHooks());
        method.maxStack=Math.max(method.maxStack,18);
        verifyMethod(method);
    }

    public static void verifyInstalled(ClassNode owner) {
        verifyMethod(method(owner));
    }

    private static void verifyMethod(MethodNode source) {
        MethodNode restored=NativeBossLivingComposerSupportV2.copy(source);
        one(restored,INVOKEVIRTUAL,BOSS,"canShadowStrike","(L"+LIVING+";)Z").name="canTeleportWith";
        List<MethodInsnNode> windups=calls(restored,INVOKESTATIC,ROOT+"zheergenphantom/CastReadability","strikeWindup","(IL"+BOSS+";)I");
        require(windups.size()==2,"Installed original strike readability site inventory");
        for(MethodInsnNode windup:windups)removeThrough(restored,windup,readability());
        MethodInsnNode teleport=one(restored,INVOKESTATIC,ROOT+"zheergenfx/LightHooks","teleport","(L"+BOSS+";DDDZ)Z");
        teleport.setOpcode(INVOKEVIRTUAL);
        teleport.owner=BOSS;
        teleport.name="randomTeleport";
        teleport.desc="(DDDZ)Z";
        MethodInsnNode cue=one(restored,INVOKEVIRTUAL,BOSS,"beginSkillCue","(Lnet/minecraft/world/phys/Vec3;DIZ)V");
        AbstractInsnNode last=cue;
        for(int count=0;count<code(startHooks()).size();count++)last=next(last);
        removeThrough(restored,last,startHooks());
        MethodInsnNode spawn=one(restored,INVOKEVIRTUAL,BOSS,"spawnShadowPhantom","(L"+LEVEL+";DDDL"+LIVING+";Ljava/lang/String;)V");
        AbstractInsnNode boundary=spawn.getNext();
        require(boundary instanceof LabelNode&&boundary.getNext() instanceof FrameNode,"Exact installed phantom branch frame boundary");
        restored.instructions.remove(boundary.getNext());
        removeThrough(restored,spawn,phantom());
        require(NativeBossLivingComposerSupportV2.methodFingerprint(restored).equals(PREIMAGE),"Whole frozen native strike body differs outside exact original start policy additions");
    }

    private static InsnList readability() {
        InsnList result=new InsnList();
        result.add(new VarInsnNode(ALOAD,0));
        result.add(new MethodInsnNode(INVOKESTATIC,ROOT+"zheergenphantom/CastReadability","strikeWindup","(IL"+BOSS+";)I",false));
        return result;
    }

    private static InsnList startHooks() {
        InsnList result=new InsnList();
        result.add(new VarInsnNode(ALOAD,0));
        result.add(new MethodInsnNode(INVOKESTATIC,ROOT+"zheergenremainingbody/SkillBodyDispatch","strikeStart","(L"+BOSS+";)V",false));
        result.add(new VarInsnNode(ALOAD,0));
        result.add(new InsnNode(ICONST_0));
        result.add(new MethodInsnNode(INVOKESTATIC,ROOT+"zheergenfx/LightHooks","strike","(L"+BOSS+";Z)V",false));
        return result;
    }

    private static InsnList phantom() {
        InsnList result=new InsnList();
        LabelNode done=new LabelNode();
        result.add(new VarInsnNode(ALOAD,0));
        result.add(new FieldInsnNode(GETFIELD,BOSS,"random","Lnet/minecraft/util/RandomSource;"));
        result.add(new MethodInsnNode(INVOKEINTERFACE,"net/minecraft/util/RandomSource","nextDouble","()D",true));
        result.add(new LdcInsnNode(0.35D));
        result.add(new InsnNode(DCMPG));
        result.add(new JumpInsnNode(IFGE,done));
        result.add(new VarInsnNode(ALOAD,0));
        result.add(new VarInsnNode(ALOAD,1));
        result.add(new VarInsnNode(DLOAD,3));
        result.add(new VarInsnNode(DLOAD,5));
        result.add(new VarInsnNode(DLOAD,7));
        result.add(new VarInsnNode(ALOAD,2));
        result.add(new LdcInsnNode("shadow_strike"));
        result.add(new MethodInsnNode(INVOKEVIRTUAL,BOSS,"spawnShadowPhantom","(L"+LEVEL+";DDDL"+LIVING+";Ljava/lang/String;)V",false));
        result.add(done);
        result.add(new FrameNode(F_FULL,13,new Object[]{BOSS,LEVEL,LIVING,DOUBLE,DOUBLE,DOUBLE,"net/minecraft/world/phys/Vec3",DOUBLE,DOUBLE,INTEGER,"[[D",INTEGER,INTEGER},0,new Object[]{}));
        return result;
    }

    private static MethodNode method(ClassNode owner) {
        require(owner.name.equals(BOSS),"Original strike installer requires genuine Boss owner");
        List<MethodNode> found=owner.methods.stream().filter(value->value.name.equals("skillShadowStrike")&&value.desc.equals(DESC)).toList();
        require(found.size()==1,"Exactly one complete native strike start required");
        return found.getFirst();
    }

    private static List<MethodInsnNode> calls(MethodNode method,int opcode,String owner,String name,String descriptor) {
        return code(method).stream().filter(value->call(value,opcode,owner,name,descriptor)).map(value->(MethodInsnNode)value).toList();
    }

    private static MethodInsnNode one(MethodNode method,int opcode,String owner,String name,String descriptor) {
        List<MethodInsnNode> found=calls(method,opcode,owner,name,descriptor);
        require(found.size()==1,"Exact original strike member inventory: "+owner+"."+name+descriptor);
        return found.getFirst();
    }

    private static List<AbstractInsnNode> code(MethodNode method) {
        return code(method.instructions);
    }

    private static List<AbstractInsnNode> code(InsnList instructions) {
        List<AbstractInsnNode> result=new ArrayList<>();
        for(AbstractInsnNode instruction:instructions)if(instruction.getOpcode()>=0)result.add(instruction);
        return result;
    }

    private static AbstractInsnNode next(AbstractInsnNode source) {
        AbstractInsnNode result=source==null?null:source.getNext();
        while(result!=null&&result.getOpcode()<0)result=result.getNext();
        return result;
    }

    private static AbstractInsnNode previous(AbstractInsnNode source) {
        AbstractInsnNode result=source==null?null:source.getPrevious();
        while(result!=null&&result.getOpcode()<0)result=result.getPrevious();
        return result;
    }

    private static boolean load(AbstractInsnNode source,int opcode,int slot) {
        return source instanceof VarInsnNode variable&&variable.getOpcode()==opcode&&variable.var==slot;
    }

    private static boolean call(AbstractInsnNode source,int opcode,String owner,String name,String descriptor) {
        return source instanceof MethodInsnNode member&&member.getOpcode()==opcode&&member.owner.equals(owner)&&member.name.equals(name)&&member.desc.equals(descriptor);
    }

    private static void removeThrough(MethodNode method,AbstractInsnNode terminal,InsnList expected) {
        List<AbstractInsnNode> wanted=code(expected),remove=new ArrayList<>();
        AbstractInsnNode actual=terminal;
        for(int index=wanted.size()-1;index>=0;index--) {
            AbstractInsnNode value=wanted.get(index);
            require(actual!=null&&same(actual,value),"Exact original strike insertion operand mismatch at "+index);
            if(actual instanceof JumpInsnNode branch) {
                AbstractInsnNode target=branch.label;
                while(target!=null&&target.getOpcode()<0)target=target.getNext();
                require(target==next(terminal),"Original35percent phantom branch must skip only its complete spawn body");
            }
            remove.add(actual);
            actual=previous(actual);
        }
        remove.forEach(method.instructions::remove);
    }

    private static boolean same(AbstractInsnNode actual,AbstractInsnNode expected) {
        if(actual.getOpcode()!=expected.getOpcode())return false;
        if(expected instanceof VarInsnNode variable)return actual instanceof VarInsnNode other&&variable.var==other.var;
        if(expected instanceof FieldInsnNode field)return actual instanceof FieldInsnNode other&&field.owner.equals(other.owner)&&field.name.equals(other.name)&&field.desc.equals(other.desc);
        if(expected instanceof MethodInsnNode member)return actual instanceof MethodInsnNode other&&member.owner.equals(other.owner)&&member.name.equals(other.name)&&member.desc.equals(other.desc)&&member.itf==other.itf;
        if(expected instanceof LdcInsnNode literal)return actual instanceof LdcInsnNode other&&literal.cst.equals(other.cst);
        return expected instanceof InsnNode||expected instanceof JumpInsnNode;
    }

    private static void require(boolean value,String reason) {
        if(!value)throw new IllegalStateException(reason);
    }
}
