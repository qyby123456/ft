package net.farmingtales.ftboss.boss.ai;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
public final class NativeManorDamageBridgeInstaller {
    private static final String BOSS="net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String SOURCE="Lnet/minecraft/world/damagesource/DamageSource;";
    private static final String BRIDGE="net/farmingtales/ftboss/boss/ai/NativeManorDamageBridge";
    private static final String ACCESS="net/farmingtales/ftboss/boss/ai/NativeManorDamageScopeAccess";
    private static final String CONTAINER="net/neoforged/neoforge/common/damagesource/DamageContainer";
    private static final String FINAL="net/farmingtales/ftboss/boss/ai/ManorFinalDamage";
    private static final String OLD="net/farmingtales/ftboss/event/NativeManorDamageEvents";
    private static final String BARGS="(L"+BOSS+";"+SOURCE;
    private static final String BASE_MANOR="1e58e727c9ccdda71b2d6935c212d4b301ac5a5d6e1f53c11a997a729518d71f";
    private static final String BASE_REDUCE="79e6c56276f21546b42563525853085d0b56d49bdcb2c851b142e668f2ca76b6";
    private static final String BASE_SETTLE="cb54f52ff04c3ca85e1e8aecaf1c627d353c80f63b42e21728e6560fd3d453b1";
    private NativeManorDamageBridgeInstaller() {}
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
    private static List<AbstractInsnNode> code(MethodNode method){List<AbstractInsnNode> nodes=new ArrayList<>();for(AbstractInsnNode node:method.instructions)if(node.getOpcode()>=0)nodes.add(node);return nodes;}
    private static AbstractInsnNode next(AbstractInsnNode node){while(node!=null&&node.getOpcode()<0)node=node.getNext();return node;}
    private static int index(List<AbstractInsnNode> nodes,LabelNode label){AbstractInsnNode dest=next(label);int position=dest==null?nodes.size():nodes.indexOf(dest);require(position>=0,"Foreign native damage branch");return position;}
    public static String graph(MethodNode method) {
        List<AbstractInsnNode> nodes=code(method);StringBuilder text=new StringBuilder(method.access+":"+method.name+method.desc+"\n");
        for(AbstractInsnNode node:nodes){text.append(node.getOpcode());
            if(node instanceof VarInsnNode value)text.append(':').append(value.var);
            else if(node instanceof FieldInsnNode value)text.append(':').append(value.owner).append(':').append(value.name).append(':').append(value.desc);
            else if(node instanceof MethodInsnNode value)text.append(':').append(value.owner).append(':').append(value.name).append(':').append(value.desc).append(':').append(value.itf);
            else if(node instanceof TypeInsnNode value)text.append(':').append(value.desc);
            else if(node instanceof IntInsnNode value)text.append(':').append(value.operand);
            else if(node instanceof IincInsnNode value)text.append(':').append(value.var).append(':').append(value.incr);
            else if(node instanceof LdcInsnNode value)text.append(':').append(value.cst.getClass().getName()).append(':').append(value.cst);
            else if(node instanceof JumpInsnNode value)text.append(':').append(index(nodes,value.label));
            else if(node instanceof InvokeDynamicInsnNode value)text.append(':').append(value.name).append(':').append(value.desc).append(':').append(value.bsm).append(':').append(Arrays.deepToString(value.bsmArgs));
            else if(node instanceof MultiANewArrayInsnNode value)text.append(':').append(value.desc).append(':').append(value.dims);
            else if(node instanceof TableSwitchInsnNode value){text.append(':').append(value.min).append(':').append(value.max).append(':').append(index(nodes,value.dflt));for(LabelNode label:value.labels)text.append(':').append(index(nodes,label));}
            else if(node instanceof LookupSwitchInsnNode value){text.append(':').append(value.keys).append(':').append(index(nodes,value.dflt));for(LabelNode label:value.labels)text.append(':').append(index(nodes,label));}
            else require(node instanceof InsnNode,"Unsupported native damage graph node "+node.getClass().getName());
            text.append('\n');
        }
        for(TryCatchBlockNode handler:method.tryCatchBlocks)text.append("try:").append(index(nodes,handler.start)).append(':').append(index(nodes,handler.end)).append(':').append(index(nodes,handler.handler)).append(':').append(handler.type).append('\n');
        try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.toString().getBytes(StandardCharsets.UTF_8)));}catch(Exception failure){throw new IllegalStateException(failure);}
    }
    public static MethodNode method(ClassNode type,String name){List<MethodNode> found=type.methods.stream().filter(method->method.name.equals(name)).toList();require(found.size()==1,"Non-unique native damage method "+name);return found.getFirst();}
    private static ClassNode copy(ClassNode type){ClassNode result=new ClassNode();type.accept(result);return result;}
    private static MethodInsnNode call(MethodNode method,String owner,String name){List<MethodInsnNode> found=code(method).stream().filter(node->node instanceof MethodInsnNode value&&value.owner.equals(owner)&&value.name.equals(name)).map(node->(MethodInsnNode)node).toList();require(found.size()==1,"Non-unique native damage call "+name);return found.getFirst();}
    private static List<MethodInsnNode> calls(MethodNode method,String name){return code(method).stream().filter(node->node instanceof MethodInsnNode value&&value.owner.equals(BRIDGE)&&value.name.equals(name)).map(node->(MethodInsnNode)node).toList();}
    private static MethodInsnNode hook(String name,String descriptor){return new MethodInsnNode(Opcodes.INVOKESTATIC,BRIDGE,name,descriptor,false);}
    private static InsnList arguments(int amountSlot){InsnList nodes=new InsnList();nodes.add(new VarInsnNode(Opcodes.ALOAD,0));nodes.add(new VarInsnNode(Opcodes.ALOAD,1));if(amountSlot>=0)nodes.add(new VarInsnNode(Opcodes.FLOAD,amountSlot));return nodes;}
    private static InsnList notification(String name){InsnList nodes=arguments(-1);nodes.add(hook(name,BARGS+")V"));return nodes;}
    private static MethodNode getter(){MethodNode method=new MethodNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_FINAL,"ftboss$manorDamageContainers","()Ljava/util/Stack;",null,null);method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,BOSS,"damageContainers","Ljava/util/Stack;"));method.instructions.add(new InsnNode(Opcodes.ARETURN));method.maxStack=1;method.maxLocals=1;return method;}
    private static void remove(MethodNode method,AbstractInsnNode start,int count){for(int offset=0;offset<count;offset++){AbstractInsnNode after=start.getNext();method.instructions.remove(start);start=after;}}
    private static void resultHook(MethodNode method,AbstractInsnNode after,String name,int slot,String ownerDescriptor){InsnList nodes=arguments(slot);nodes.add(hook(name,ownerDescriptor+"F)F"));nodes.add(new VarInsnNode(Opcodes.FSTORE,slot));method.instructions.insert(after,nodes);method.maxStack=Math.max(method.maxStack,4);}
    private static void weave(ClassNode type) {
        MethodNode manor=method(type,"manorHurt"),reduce=method(type,"ftboss$reduceManorDamage");
        MethodInsnNode settle=call(manor,FINAL,"settle");AbstractInsnNode anchor=settle.getPrevious().getPrevious().getPrevious();
        InsnList begin=arguments(2);begin.add(hook("begin",BARGS+"F)V"));LabelNode start=new LabelNode();begin.add(start);manor.instructions.insertBefore(anchor,begin);
        boolean active=false;for(AbstractInsnNode node:manor.instructions.toArray()){if(node==start)active=true;if(active&&node.getOpcode()==Opcodes.IRETURN)manor.instructions.insertBefore(node,notification("finish"));}
        List<FieldInsnNode> timers=code(manor).stream().filter(node->node instanceof FieldInsnNode value&&value.getOpcode()==Opcodes.PUTFIELD&&value.name.equals("invulnerableTime")).map(node->(FieldInsnNode)node).toList();require(timers.size()==1,"Changed Manor invulnerability write");manor.instructions.insert(timers.getFirst(),notification("timing"));
        MethodInsnNode write=call(manor,"net/farmingtales/ftboss/boss/ai/ManorRaidCombat","beforeHealthWrite");AbstractInsnNode health=next(write.getNext());require(health instanceof MethodInsnNode value&&value.name.equals("setHealth"),"Changed original real Manor health write");manor.instructions.insert(health,notification("applied"));
        LabelNode end=new LabelNode(),handler=end;manor.instructions.add(handler);manor.instructions.add(new FrameNode(Opcodes.F_FULL,3,new Object[]{BOSS,"net/minecraft/world/damagesource/DamageSource",Opcodes.FLOAT},1,new Object[]{"java/lang/Throwable"}));
        manor.instructions.add(new InsnNode(Opcodes.DUP));manor.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));manor.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));manor.instructions.add(hook("abort","(Ljava/lang/Throwable;L"+BOSS+";"+SOURCE+")V"));manor.instructions.add(new InsnNode(Opcodes.ATHROW));manor.tryCatchBlocks.add(new TryCatchBlockNode(start,end,handler,null));manor.maxStack=Math.max(manor.maxStack,4);
        AbstractInsnNode constructor=code(reduce).getFirst();require(constructor.getOpcode()==Opcodes.NEW,"Changed owned Stage29 context constructor");InsnList factory=arguments(2);factory.add(hook("reductionContainer",BARGS+"F)L"+CONTAINER+";"));reduce.instructions.insertBefore(constructor,factory);remove(reduce,constructor,5);
        MethodInsnNode armor=call(reduce,BOSS,"getDamageAfterArmorAbsorb");resultHook(reduce,next(armor.getNext()),"armorResult",2,BARGS);
        MethodInsnNode magic=call(reduce,BOSS,"getDamageAfterMagicAbsorb");resultHook(reduce,next(magic.getNext()),"magicResult",4,BARGS);
        type.interfaces.add(ACCESS);type.methods.add(getter());
    }
    public static int install(ClassNode type) {
        require(type.name.equals(BOSS),"Foreign native damage target");if(type.interfaces.contains(ACCESS)){originalView(type);return 0;}
        require(graph(method(type,"manorHurt")).equals(BASE_MANOR)&&graph(method(type,"ftboss$reduceManorDamage")).equals(BASE_REDUCE),"Changed accepted Manor damage inputs");
        require(method(type,"manorHurt").tryCatchBlocks.isEmpty(),"Unexpected prior Manor handler");weave(type);originalView(type);return 2;
    }
    private static void removeArgumentsCall(MethodNode method,MethodInsnNode call,int before,int after){AbstractInsnNode start=call;for(int count=0;count<before;count++)start=start.getPrevious();remove(method,start,before+1+after);}
    private static void unweave(ClassNode type) {
        require(Collections.frequency(type.interfaces,ACCESS)==1,"Missing unique damage scope access");require(graph(method(type,"ftboss$manorDamageContainers")).equals(graph(getter())),"Changed actual damage stack accessor");type.interfaces.remove(ACCESS);type.methods.remove(method(type,"ftboss$manorDamageContainers"));
        MethodNode manor=method(type,"manorHurt"),reduce=method(type,"ftboss$reduceManorDamage");require(manor.tryCatchBlocks.size()==1,"Changed Manor cleanup handlers");TryCatchBlockNode handler=manor.tryCatchBlocks.getFirst();
        MethodInsnNode begin=call(manor,BRIDGE,"begin");require(begin.getNext()==handler.start&&handler.type==null&&handler.end==handler.handler,"Changed exact Manor cleanup boundaries");
        List<AbstractInsnNode> tail=new ArrayList<>();for(AbstractInsnNode node=handler.handler;node!=null;node=node.getNext())if(node.getOpcode()>=0)tail.add(node);
        require(tail.size()==5&&tail.get(0).getOpcode()==Opcodes.DUP&&tail.get(4).getOpcode()==Opcodes.ATHROW&&tail.get(3)==call(manor,BRIDGE,"abort"),"Changed original throwable cleanup");
        AbstractInsnNode tailStart=handler.end;while(tailStart!=null){AbstractInsnNode after=tailStart.getNext();manor.instructions.remove(tailStart);tailStart=after;}manor.tryCatchBlocks.clear();manor.instructions.remove(handler.start);removeArgumentsCall(manor,begin,3,0);
        require(!calls(manor,"finish").isEmpty(),"Missing normal cleanup");for(String name:List.of("finish","timing","applied"))for(MethodInsnNode point:calls(manor,name))removeArgumentsCall(manor,point,2,0);
        for(String name:List.of("armorResult","magicResult"))removeArgumentsCall(reduce,call(reduce,BRIDGE,name),3,1);
        MethodInsnNode factory=call(reduce,BRIDGE,"reductionContainer");AbstractInsnNode first=factory.getPrevious().getPrevious().getPrevious();InsnList constructor=new InsnList();constructor.add(new TypeInsnNode(Opcodes.NEW,CONTAINER));constructor.add(new InsnNode(Opcodes.DUP));constructor.add(new VarInsnNode(Opcodes.ALOAD,1));constructor.add(new VarInsnNode(Opcodes.FLOAD,2));constructor.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,CONTAINER,"<init>","("+SOURCE+"F)V",false));reduce.instructions.insertBefore(first,constructor);remove(reduce,first,4);
    }
    public static ClassNode originalView(ClassNode type) {
        ClassNode original=copy(type);unweave(original);require(graph(method(original,"manorHurt")).equals(BASE_MANOR)&&graph(method(original,"ftboss$reduceManorDamage")).equals(BASE_REDUCE),"Original Manor/Stage29 context not preserved");
        ClassNode expected=copy(original);weave(expected);for(String name:List.of("manorHurt","ftboss$reduceManorDamage","ftboss$manorDamageContainers"))require(graph(method(expected,name)).equals(graph(method(type,name))),"Changed exact native Manor instrumentation "+name);
        return original;
    }
    private static void weaveSettlement(ClassNode type) {
        MethodNode settle=method(type,"settle");for(String name:List.of("onLivingHurt","damageEvent","post")){MethodInsnNode point=call(settle,OLD,name);point.owner=BRIDGE;if(name.equals("onLivingHurt"))point.name="incoming";}
        MethodInsnNode defense=call(settle,"net/farmingtales/ftboss/boss/ai/ManorDefense","reduce");AbstractInsnNode defenseStore=next(defense.getNext());require(defenseStore instanceof VarInsnNode value&&value.getOpcode()==Opcodes.FSTORE&&value.var==8,"Changed original defense result");String entity="(Lnet/minecraft/world/entity/LivingEntity;"+SOURCE;resultHook(settle,defenseStore,"pre",8,entity);
        List<VarInsnNode> absorption=code(settle).stream().filter(node->node instanceof VarInsnNode value&&value.getOpcode()==Opcodes.FSTORE&&value.var==10).map(node->(VarInsnNode)node).toList();require(absorption.size()==1,"Changed original planned absorption");resultHook(settle,absorption.getFirst(),"absorption",10,entity);
    }
    public static void installSettlement(ClassNode type) {
        require(type.name.equals(FINAL)&&graph(method(type,"settle")).equals(BASE_SETTLE),"Changed accepted original settlement");weaveSettlement(type);verifySettlement(type);
    }
    public static void verifySettlement(ClassNode type) {
        ClassNode original=copy(type);MethodNode settle=method(original,"settle");for(String name:List.of("incoming","damageEvent","post")){MethodInsnNode point=call(settle,BRIDGE,name);point.owner=OLD;if(name.equals("incoming"))point.name="onLivingHurt";}
        for(String name:List.of("pre","absorption"))removeArgumentsCall(settle,call(settle,BRIDGE,name),3,1);
        require(graph(settle).equals(BASE_SETTLE),"Original settlement algorithm changed");ClassNode expected=copy(original);weaveSettlement(expected);require(graph(method(expected,"settle")).equals(graph(method(type,"settle"))),"Changed exact native settlement hooks");
    }
}
