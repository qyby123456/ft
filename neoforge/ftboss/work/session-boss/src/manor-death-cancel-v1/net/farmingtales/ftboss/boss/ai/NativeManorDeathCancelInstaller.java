package net.farmingtales.ftboss.boss.ai;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class NativeManorDeathCancelInstaller {
    private static final String BOSS="net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String LIVING="net/minecraft/world/entity/LivingEntity";
    private static final String SOURCE="(Lnet/minecraft/world/damagesource/DamageSource;)V";
    private static final String BASE_MANOR="0bc967a13b178d293426e09e6d66183d32fb5c1447c25b0dddcd5d729ec31510";
    private static final String BASE_DIE="ff98c5c8e2a059a0338e3dda5efb1772720910a498c0954361ce9861443f4f33";
    private NativeManorDeathCancelInstaller(){}
    private static void require(boolean value,String label){if(!value)throw new IllegalStateException(label);}
    private static MethodNode method(ClassNode type,String name){return NativeManorDamageBridgeInstaller.method(type,name);}
    private static String graph(MethodNode method){return NativeManorDamageBridgeInstaller.graph(method);}
    private static AbstractInsnNode next(AbstractInsnNode value){while(value!=null&&value.getOpcode()<0)value=value.getNext();return value;}
    private static boolean receiver(AbstractInsnNode value){return value instanceof VarInsnNode load&&load.getOpcode()==Opcodes.ALOAD&&load.var==0;}
    private static boolean finalFlag(AbstractInsnNode value){return value instanceof FieldInsnNode field&&field.getOpcode()==Opcodes.GETFIELD&&field.owner.equals(LIVING)&&field.name.equals("dead")&&field.desc.equals("Z");}
    private static List<MethodInsnNode> deaths(MethodNode method){return Arrays.stream(method.instructions.toArray()).filter(value->value instanceof MethodInsnNode call&&call.getOpcode()==Opcodes.INVOKEVIRTUAL&&call.owner.equals(BOSS)&&call.name.equals("die")&&call.desc.equals(SOURCE)).map(value->(MethodInsnNode)value).toList();}
    private static MethodInsnNode superDeath(MethodNode method){List<MethodInsnNode> found=Arrays.stream(method.instructions.toArray()).filter(value->value instanceof MethodInsnNode call&&call.getOpcode()==Opcodes.INVOKESPECIAL&&call.name.equals("die")&&call.desc.equals(SOURCE)).map(value->(MethodInsnNode)value).toList();require(found.size()==1,"Exact unique original superclass death invocation");return found.getFirst();}
    private static ClassNode copy(ClassNode type){ClassNode result=new ClassNode();type.accept(result);return result;}
    private static void preimage(ClassNode type){require(type.name.equals(BOSS)&&type.methods.size()==342,"Exact current complete original Boss target");require(graph(method(type,"manorHurt")).equals(BASE_MANOR)&&graph(method(type,"die")).equals(BASE_DIE),"Changed complete native original death-removal preimage");}
    private static void weave(ClassNode type){
        MethodNode manor=method(type,"manorHurt");List<MethodInsnNode> calls=deaths(manor);require(calls.size()==2,"Both original scripted death/discard paths");
        for(MethodInsnNode call:calls){AbstractInsnNode load=next(call.getNext()),removal=next(load.getNext());require(receiver(load)&&removal instanceof MethodInsnNode invoke&&invoke.getOpcode()==Opcodes.INVOKEVIRTUAL&&invoke.owner.equals(BOSS)&&invoke.name.equals("discard")&&invoke.desc.equals("()V"),"Original scripted removal remains adjacent to native death");LabelNode keep=new LabelNode();InsnList guard=new InsnList();guard.add(new VarInsnNode(Opcodes.ALOAD,0));guard.add(new FieldInsnNode(Opcodes.GETFIELD,LIVING,"dead","Z"));guard.add(new JumpInsnNode(Opcodes.IFEQ,keep));manor.instructions.insert(call,guard);manor.instructions.insert(removal,keep);}
        MethodNode die=method(type,"die");MethodInsnNode call=superDeath(die);LabelNode committed=new LabelNode();InsnList guard=new InsnList();guard.add(new VarInsnNode(Opcodes.ALOAD,0));guard.add(new FieldInsnNode(Opcodes.GETFIELD,LIVING,"dead","Z"));guard.add(new JumpInsnNode(Opcodes.IFNE,committed));guard.add(new InsnNode(Opcodes.RETURN));guard.add(committed);die.instructions.insert(call,guard);
    }
    private static void strip(ClassNode type){
        MethodNode manor=method(type,"manorHurt");List<MethodInsnNode> calls=deaths(manor);require(calls.size()==2,"Both exact native guarded script paths retained");
        for(MethodInsnNode call:calls){AbstractInsnNode load=next(call.getNext()),flag=next(load.getNext()),jump=next(flag.getNext());require(receiver(load)&&finalFlag(flag)&&jump instanceof JumpInsnNode branch&&branch.getOpcode()==Opcodes.IFEQ,"Exact native final-death discard gate");AbstractInsnNode receiver=next(jump.getNext()),removal=next(receiver.getNext());require(receiver(receiver)&&removal instanceof MethodInsnNode invoke&&invoke.owner.equals(BOSS)&&invoke.name.equals("discard")&&invoke.desc.equals("()V"),"Original discard receiver and invocation preserved");require(next(((JumpInsnNode)jump).label)==next(removal.getNext()),"Native cancellation skips only original discard");for(AbstractInsnNode value:List.of(load,flag,jump))manor.instructions.remove(value);}
        MethodNode die=method(type,"die");MethodInsnNode call=superDeath(die);AbstractInsnNode load=next(call.getNext()),flag=next(load.getNext()),jump=next(flag.getNext()),ret=next(jump.getNext());require(receiver(load)&&finalFlag(flag)&&jump instanceof JumpInsnNode branch&&branch.getOpcode()==Opcodes.IFNE&&ret.getOpcode()==Opcodes.RETURN,"Exact native final-death post-super cleanup gate");require(next(((JumpInsnNode)jump).label)==next(ret.getNext()),"Uncanceled death retains exact original cleanup continuation");for(AbstractInsnNode value:List.of(load,flag,jump,ret))die.instructions.remove(value);
    }
    public static ClassNode originalView(ClassNode type){require(type.name.equals(BOSS),"Foreign native death lifecycle target");ClassNode result=copy(type);strip(result);preimage(result);return result;}
    public static void verifyInstalled(ClassNode type){ClassNode expected=originalView(type);weave(expected);for(String name:List.of("manorHurt","die"))require(graph(method(expected,name)).equals(graph(method(type,name))),"Changed exact native final-death lifecycle contract "+name);}
    public static int install(ClassNode type){require(type.name.equals(BOSS),"Foreign native death lifecycle target");boolean present=Arrays.stream(method(type,"die").instructions.toArray()).anyMatch(NativeManorDeathCancelInstaller::finalFlag);if(present){verifyInstalled(type);return 0;}preimage(type);weave(type);verifyInstalled(type);return 3;}
}
