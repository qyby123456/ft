package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class NativeManorSharedDamageInstaller {
    private static final String BOSS="net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String SUPPORT="net/farmingtales/ftboss/maque/support/MaqueSupportHooks";
    private static final String YOUZI="net/farmingtales/ftboss/youzi/YouziCombatEvents";
    private static final String SOURCE="Lnet/minecraft/world/damagesource/DamageSource;";
    private static final String INTERCEPT="(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;F)V";
    private static final String SHARED="(L"+BOSS+";L"+BOSS+";)V";

    private NativeManorSharedDamageInstaller() {
    }

    public static int install(ClassNode target) {
        requireTarget(target);MethodNode sparrow=method(target,"sparrowShare");MethodNode minions=method(target,"youziMinionShare");
        if(count(sparrow)+count(minions)!=0){verifyInstalled(target);return 0;}
        List<AbstractInsnNode> instructions=code(sparrow);List<VarInsnNode> stores=new ArrayList<>();
        for(int index=3;index<instructions.size();index++)if(instructions.get(index) instanceof VarInsnNode store&&store.getOpcode()==Opcodes.FSTORE&&instructions.get(index-1).getOpcode()==Opcodes.FMUL&&instructions.get(index-2) instanceof LdcInsnNode half&&Float.valueOf(0.5F).equals(half.cst)&&variable(instructions.get(index-3),Opcodes.FLOAD,1))stores.add(store);
        if(stores.size()!=1 || stores.getFirst().var!=5)throw new IllegalStateException("Changed unique original sparrow half-damage calculation");VarInsnNode store=stores.getFirst();int position=instructions.indexOf(store);
        if(!(instructions.get(position+1) instanceof VarInsnNode receiver)||receiver.getOpcode()!=Opcodes.ALOAD||receiver.var!=4||!invoke(instructions.get(position+2),Opcodes.INVOKEVIRTUAL,BOSS,"getHealth","()F")||!variable(instructions.get(position+3),Opcodes.FLOAD,store.var)||instructions.get(position+4).getOpcode()!=Opcodes.FSUB)throw new IllegalStateException("Changed original sparrow health-delta phase");
        List<MethodInsnNode> writes=writes(minions);List<AbstractInsnNode> managed=code(minions);int receiverSlot=receiverBefore(writes,managed);
        if(receiverSlot!=8)throw new IllegalStateException("Changed original native minion receiver local");
        InsnList intercept=new InsnList();intercept.add(new VarInsnNode(Opcodes.ALOAD,0));intercept.add(new VarInsnNode(Opcodes.ALOAD,receiver.var));intercept.add(new VarInsnNode(Opcodes.FLOAD,store.var));intercept.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SUPPORT,"intercept",INTERCEPT,false));sparrow.instructions.insert(store,intercept);
        for(MethodInsnNode write:writes){InsnList shared=new InsnList();shared.add(new VarInsnNode(Opcodes.ALOAD,0));shared.add(new VarInsnNode(Opcodes.ALOAD,receiverSlot));shared.add(new MethodInsnNode(Opcodes.INVOKESTATIC,YOUZI,"sharedDamage",SHARED,false));minions.instructions.insertBefore(write,shared);}
        sparrow.maxStack+=3;minions.maxStack+=2;verifyInstalled(target);return 2;
    }

    public static void verifyInstalled(ClassNode target) {
        requireTarget(target);MethodNode sparrow=method(target,"sparrowShare"),minions=method(target,"youziMinionShare");if(count(sparrow)!=1||count(minions)!=3)throw new IllegalStateException("Missing or duplicate native sharing callbacks");
        List<AbstractInsnNode> instructions=code(sparrow);int position=unique(instructions,SUPPORT,"intercept",INTERCEPT);
        if(position<7||!variable(instructions.get(position-3),Opcodes.ALOAD,0)||!(instructions.get(position-2) instanceof VarInsnNode receiver)||receiver.getOpcode()!=Opcodes.ALOAD||receiver.var!=4||!(instructions.get(position-1) instanceof VarInsnNode amount)||amount.getOpcode()!=Opcodes.FLOAD||amount.var!=5||!variable(instructions.get(position-4),Opcodes.FSTORE,amount.var)||instructions.get(position-5).getOpcode()!=Opcodes.FMUL||!(instructions.get(position-6) instanceof LdcInsnNode half)||!Float.valueOf(0.5F).equals(half.cst)||!variable(instructions.get(position-7),Opcodes.FLOAD,1)||!variable(instructions.get(position+1),Opcodes.ALOAD,receiver.var)||!invoke(instructions.get(position+2),Opcodes.INVOKEVIRTUAL,BOSS,"getHealth","()F")||!variable(instructions.get(position+3),Opcodes.FLOAD,amount.var)||instructions.get(position+4).getOpcode()!=Opcodes.FSUB)throw new IllegalStateException("Changed actual native sparrow intercept arguments/order");
        List<MethodInsnNode> writes=writes(minions);List<AbstractInsnNode> managed=code(minions);int receiverSlot=-1;
        for(int index=0;index<writes.size();index++){
            int write=managed.indexOf(writes.get(index));if(write<5||!invoke(managed.get(write-1),Opcodes.INVOKESTATIC,YOUZI,"sharedDamage",SHARED)||!variable(managed.get(write-3),Opcodes.ALOAD,0)||!(managed.get(write-2) instanceof VarInsnNode minion)||minion.getOpcode()!=Opcodes.ALOAD||minion.var!=8||!variable(managed.get(write-5),Opcodes.ALOAD,minion.var))throw new IllegalStateException("Changed actual native minion pre-health callback");
            if(receiverSlot<0)receiverSlot=minion.var;if(receiverSlot!=minion.var)throw new IllegalStateException("Changed minion receiver identity across death/ordinary branches");
            if(index==2&&!variable(managed.get(write-4),Opcodes.FLOAD,9))throw new IllegalStateException("Changed native ordinary minion health local");
            int opcode=managed.get(write-4).getOpcode();if(index==0&&opcode!=Opcodes.FCONST_1||index==1&&opcode!=Opcodes.FCONST_0||index==2&&opcode!=Opcodes.FLOAD)throw new IllegalStateException("Changed original minion protected/death/ordinary health values");
        }
    }

    private static int receiverBefore(List<MethodInsnNode> writes,List<AbstractInsnNode> instructions) {
        int slot=-1;
        for(int index=0;index<writes.size();index++){
            int position=instructions.indexOf(writes.get(index));if(position<2||!(instructions.get(position-2) instanceof VarInsnNode receiver)||receiver.getOpcode()!=Opcodes.ALOAD)throw new IllegalStateException("Changed original minion health-write receiver");
            if(slot<0)slot=receiver.var;if(slot!=receiver.var)throw new IllegalStateException("Changed original minion receiver identity");if(index==2&&!variable(instructions.get(position-1),Opcodes.FLOAD,9))throw new IllegalStateException("Changed original native ordinary minion health local");int opcode=instructions.get(position-1).getOpcode();if(index==0&&opcode!=Opcodes.FCONST_1||index==1&&opcode!=Opcodes.FCONST_0||index==2&&opcode!=Opcodes.FLOAD)throw new IllegalStateException("Changed original three minion health phases");
        }
        return slot;
    }

    private static List<MethodInsnNode> writes(MethodNode method) {List<MethodInsnNode> writes=code(method).stream().filter(instruction->invoke(instruction,Opcodes.INVOKEVIRTUAL,BOSS,"setHealth","(F)V")).map(instruction->(MethodInsnNode)instruction).toList();if(writes.size()!=3)throw new IllegalStateException("Missing exact three native minion health writes");return writes;}
    private static int unique(List<AbstractInsnNode> instructions,String owner,String name,String descriptor) {List<AbstractInsnNode> matches=instructions.stream().filter(instruction->invoke(instruction,Opcodes.INVOKESTATIC,owner,name,descriptor)).toList();if(matches.size()!=1)throw new IllegalStateException("Missing exact native sparrow intercept");return instructions.indexOf(matches.getFirst());}
    private static int count(MethodNode method) {int count=0;for(AbstractInsnNode instruction:method.instructions)if(instruction instanceof MethodInsnNode call&&(call.owner.equals(SUPPORT)||call.owner.equals(YOUZI)))count++;return count;}
    private static boolean invoke(AbstractInsnNode instruction,int opcode,String owner,String name,String descriptor) {return instruction instanceof MethodInsnNode call&&call.getOpcode()==opcode&&call.owner.equals(owner)&&call.name.equals(name)&&call.desc.equals(descriptor)&&!call.itf;}
    private static boolean variable(AbstractInsnNode instruction,int opcode,int slot) {return instruction instanceof VarInsnNode variable&&variable.getOpcode()==opcode&&variable.var==slot;}
    private static MethodNode method(ClassNode target,String name) {List<MethodNode> methods=target.methods.stream().filter(method->method.name.equals(name)&&method.desc.equals("(F"+SOURCE+")F")&&(method.access&(Opcodes.ACC_STATIC|Opcodes.ACC_NATIVE|Opcodes.ACC_ABSTRACT))==0).toList();if(methods.size()!=1)throw new IllegalStateException("Missing genuine native sharing method "+name);return methods.getFirst();}
    private static List<AbstractInsnNode> code(MethodNode method) {List<AbstractInsnNode> instructions=new ArrayList<>();for(AbstractInsnNode instruction:method.instructions)if(instruction.getOpcode()>=0)instructions.add(instruction);return instructions;}
    private static void requireTarget(ClassNode target) {if(target==null||!target.name.equals(BOSS))throw new IllegalStateException("Unsupported native sharing target");}
}
