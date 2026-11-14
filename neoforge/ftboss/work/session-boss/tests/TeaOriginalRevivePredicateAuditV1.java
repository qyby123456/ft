import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import net.farmingtales.ftboss.boss.ai.NativeManorDamageBridgeInstaller;

public final class TeaOriginalRevivePredicateAuditV1 implements Opcodes {
    private static final String TEA="net/farmingtales/ftboss/entity/TeaTreeBossEntity";
    private static final String ATTRIBUTE="net/minecraft/world/entity/ai/attributes/Attribute";
    private static final String EFFECT="net/minecraft/world/effect/MobEffect";
    private static final String HOLDER="net/minecraft/core/Holder";
    private static final List<String> ORIGINAL_METHODS=List.of("isFinaleChallenge","setFinaleChallenge","updateBossBarName","tickServer","tickIronMagic","spitTeaPoison");
    private static final Set<String> EXISTING_CHANGED=Set.of("tickServer","tickIronMagic","spitTeaPoison","tickBossBar","dropRewards");
    private static final Map<String,ClassNode> hierarchy=new HashMap<>();
    private static final List<Map<String,String>> bridges=new ArrayList<>();
    private static int checks,links;
    private static void require(boolean value,String label){checks++;if(!value)throw new IllegalStateException(label);}
    private static ClassNode read(byte[] bytes){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);return node;}
    private static ClassNode copy(ClassNode value){ClassNode result=new ClassNode();value.accept(result);return result;}
    private static MethodNode method(ClassNode node,String name){List<MethodNode> found=node.methods.stream().filter(value->value.name.equals(name)).toList();require(found.size()==1,"Unique method "+name);return found.getFirst();}
    private static String graph(MethodNode method){return NativeManorDamageBridgeInstaller.graph(method);}
    private static byte[] raw(ClassNode node){ClassWriter writer=new ClassWriter(0);node.accept(writer);return writer.toByteArray();}
    private static AbstractInsnNode previous(AbstractInsnNode value){while(value!=null&&value.getOpcode()<0)value=value.getPrevious();return value;}
    private static ClassNode lookup(String name)throws Exception{
        if(hierarchy.containsKey(name))return hierarchy.get(name);
        try(InputStream stream=ClassLoader.getSystemResourceAsStream(name+".class")){require(stream!=null,"Linkage owner exists "+name);ClassNode node=read(stream.readAllBytes());hierarchy.put(name,node);return node;}
    }
    private static boolean assignable(String parent,String child,Set<String> seen)throws Exception{
        if(parent.equals(child))return true;if(!seen.add(child))return false;ClassNode node=lookup(child);
        if(node.superName!=null&&assignable(parent,node.superName,seen))return true;
        for(String type:node.interfaces)if(assignable(parent,type,seen))return true;return false;
    }
    private static ClassWriter writer(){return new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS){
        @Override protected String getCommonSuperClass(String first,String second){try{
            if(first.equals(second))return first;if(first.startsWith("[")||second.startsWith("["))return "java/lang/Object";
            if(assignable(first,second,new HashSet<>()))return first;if(assignable(second,first,new HashSet<>()))return second;
            ClassNode node=lookup(first);if((node.access&ACC_INTERFACE)!=0||(lookup(second).access&ACC_INTERFACE)!=0)return "java/lang/Object";
            while(node.superName!=null){node=lookup(node.superName);if(assignable(node.name,second,new HashSet<>()))return node.name;}return "java/lang/Object";
        }catch(Exception failure){throw new IllegalStateException(failure);}}
    };}
    private static MethodNode linkedMethod(String owner,String name,String desc,Set<String> seen)throws Exception{
        if(!seen.add(owner))return null;ClassNode node=lookup(owner);
        for(MethodNode value:node.methods)if(value.name.equals(name)&&value.desc.equals(desc))return value;
        if(name.equals("<init>"))return null;
        if(node.superName!=null){MethodNode result=linkedMethod(node.superName,name,desc,seen);if(result!=null)return result;}
        for(String type:node.interfaces){MethodNode result=linkedMethod(type,name,desc,seen);if(result!=null)return result;}return null;
    }
    private static FieldNode linkedField(String owner,String name,String desc,Set<String> seen)throws Exception{
        if(!seen.add(owner))return null;ClassNode node=lookup(owner);
        for(FieldNode value:node.fields)if(value.name.equals(name)&&value.desc.equals(desc))return value;
        if(node.superName!=null){FieldNode result=linkedField(node.superName,name,desc,seen);if(result!=null)return result;}
        for(String type:node.interfaces){FieldNode result=linkedField(type,name,desc,seen);if(result!=null)return result;}return null;
    }
    private static void verifyLinks(ClassNode node)throws Exception{
        for(String name:ORIGINAL_METHODS)for(AbstractInsnNode instruction:method(node,name).instructions){
            if(instruction instanceof MethodInsnNode call){MethodNode found=linkedMethod(call.owner,call.name,call.desc,new HashSet<>());require(found!=null,"Resolved actual SDK method "+call.owner+"."+call.name+call.desc);require(((found.access&ACC_STATIC)!=0)==(call.getOpcode()==INVOKESTATIC),"Actual SDK method staticness "+call.name);links++;}
            if(instruction instanceof FieldInsnNode field){FieldNode found=linkedField(field.owner,field.name,field.desc,new HashSet<>());require(found!=null,"Resolved actual SDK field "+field.owner+"."+field.name+field.desc);require(((found.access&ACC_STATIC)!=0)==(field.getOpcode()==GETSTATIC||field.getOpcode()==PUTSTATIC),"Actual SDK field staticness "+field.name);links++;}
        }
    }
    private static void bridge(String method,String kind,String before,String after){bridges.add(Map.of("method",method,"kind",kind,"before",before,"after",after));}
    private static void platformBridges(ClassNode node){
        for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray()){
            if(instruction instanceof FieldInsnNode field&&((field.owner.equals("net/minecraft/world/entity/ai/attributes/Attributes")&&field.desc.equals("L"+ATTRIBUTE+";"))||(field.owner.equals("net/minecraft/world/effect/MobEffects")&&field.desc.equals("L"+EFFECT+";")))){
                String before=field.desc;field.desc="L"+HOLDER+";";bridge(method.name,"registry holder field",before,field.desc);
            }
            if(!(instruction instanceof MethodInsnNode call))continue;
            String before=call.desc;
            if(call.name.equals("getAttribute")&&call.desc.equals("(L"+ATTRIBUTE+";)Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;"))call.desc="(L"+HOLDER+";)Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;";
            if(call.owner.equals("net/minecraft/world/effect/MobEffectInstance")&&call.name.equals("getEffect")&&call.desc.equals("()L"+EFFECT+";"))call.desc="()L"+HOLDER+";";
            if(call.name.equals("removeEffect")&&call.desc.equals("(L"+EFFECT+";)Z"))call.desc="(L"+HOLDER+";)Z";
            if(call.owner.equals("net/minecraft/world/effect/MobEffectInstance")&&call.name.equals("<init>")&&call.desc.equals("(L"+EFFECT+";IIZZZ)V"))call.desc="(L"+HOLDER+";IIZZZ)V";
            if(!call.desc.equals(before))bridge(method.name,"platform holder descriptor",before,call.desc);
            if(call.owner.equals(EFFECT)&&call.name.equals("isBeneficial")){
                InsnList unwrap=new InsnList();unwrap.add(new MethodInsnNode(INVOKEINTERFACE,HOLDER,"value","()Ljava/lang/Object;",true));unwrap.add(new TypeInsnNode(CHECKCAST,EFFECT));method.instructions.insertBefore(call,unwrap);bridge(method.name,"unwrap effect for behavior","Holder<MobEffect>","Holder.value then CHECKCAST MobEffect");
            }
        }
    }
    private static void verifyOriginalMethods(ClassNode expected,ClassNode actual){
        for(String name:ORIGINAL_METHODS)require(graph(method(expected,name)).equals(graph(method(actual,name))),"Complete original mapped and narrowly bridged graph exact: "+name);
    }
    private static void bossBarName(ClassNode node){
        MethodNode method=method(node,"tickBossBar");List<FieldInsnNode> candidates=new ArrayList<>();
        for(AbstractInsnNode instruction:method.instructions)if(instruction instanceof FieldInsnNode field&&field.owner.equals(TEA)&&field.name.equals("bossBar")){
            AbstractInsnNode cursor=instruction;boolean progress=false;
            for(int count=0;cursor!=null&&count<12;cursor=cursor.getNext())if(cursor.getOpcode()>=0){count++;if(cursor instanceof MethodInsnNode call&&call.name.equals("setProgress")){progress=true;break;}}
            if(progress)candidates.add(field);
        }
        require(candidates.size()==1,"Unique live non-minion boss-bar update boundary");AbstractInsnNode load=previous(candidates.getFirst().getPrevious());require(load instanceof VarInsnNode value&&value.getOpcode()==ALOAD&&value.var==0,"Original boss-bar receiver");
        InsnList addition=new InsnList();addition.add(new VarInsnNode(ALOAD,0));addition.add(new MethodInsnNode(INVOKEVIRTUAL,TEA,"updateBossBarName","()V",false));method.instructions.insertBefore(load,addition);
    }
    private static int experiencePredicate(ClassNode node){
        MethodNode method=method(node,"dropRewards");TypeInsnNode allocation=null;
        for(AbstractInsnNode instruction:method.instructions)if(instruction instanceof TypeInsnNode type&&type.getOpcode()==NEW&&type.desc.equals("net/minecraft/world/entity/ExperienceOrb")){require(allocation==null,"Unique original experience allocation");allocation=type;}
        require(allocation!=null,"Existing original experience allocation retained");VarInsnNode predicate=null;
        for(AbstractInsnNode cursor=allocation.getNext();cursor!=null;cursor=cursor.getNext()){
            if(cursor instanceof MethodInsnNode call&&call.name.equals("<init>"))break;
            if(cursor instanceof VarInsnNode load&&load.getOpcode()==ILOAD){require(predicate==null,"Unique existing experience predicate");predicate=load;}
        }
        require(predicate!=null,"Existing silver-key predicate identified independently from experience");int slot=predicate.var;
        InsnList addition=new InsnList();addition.add(new VarInsnNode(ALOAD,0));addition.add(new MethodInsnNode(INVOKEVIRTUAL,TEA,"isFinaleChallenge","()Z",false));method.instructions.insertBefore(predicate,addition);method.instructions.remove(predicate);return slot;
    }
    private static void verifyCollateral(ClassNode before,ClassNode actual,int xpSlot){
        require(actual.methods.size()==before.methods.size()+3&&actual.fields.size()==before.fields.size()+3,"Only original three methods and constants added");
        for(MethodNode original:before.methods)if(!EXISTING_CHANGED.contains(original.name))require(graph(original).equals(graph(method(actual,original.name))),"Every unrelated actual Tea method graph unchanged: "+original.name);
        ClassNode reverted=copy(actual);MethodNode bar=method(reverted,"tickBossBar");List<MethodInsnNode> calls=Arrays.stream(bar.instructions.toArray()).filter(value->value instanceof MethodInsnNode call&&call.owner.equals(TEA)&&call.name.equals("updateBossBarName")).map(value->(MethodInsnNode)value).toList();require(calls.size()==1,"One new name refresh at original live-bar branch");AbstractInsnNode load=previous(calls.getFirst().getPrevious());require(load instanceof VarInsnNode value&&value.getOpcode()==ALOAD&&value.var==0,"Name refresh exact receiver");bar.instructions.remove(load);bar.instructions.remove(calls.getFirst());require(graph(bar).equals(graph(method(before,"tickBossBar"))),"Boss-bar name addition reverses to exact previous graph");
        MethodNode rewards=method(reverted,"dropRewards");calls=Arrays.stream(rewards.instructions.toArray()).filter(value->value instanceof MethodInsnNode call&&call.owner.equals(TEA)&&call.name.equals("isFinaleChallenge")).map(value->(MethodInsnNode)value).toList();require(calls.size()==1,"Only experience uses added original predicate");MethodInsnNode call=calls.getFirst();load=previous(call.getPrevious());require(load instanceof VarInsnNode value&&value.getOpcode()==ALOAD&&value.var==0,"Experience predicate exact receiver");rewards.instructions.insertBefore(load,new VarInsnNode(ILOAD,xpSlot));rewards.instructions.remove(load);rewards.instructions.remove(call);require(graph(rewards).equals(graph(method(before,"dropRewards"))),"Experience predicate reverses exactly; all silver-key, material and other reward behavior unchanged");
    }
    public static void main(String[] args)throws Exception{
        Path originalPath=Path.of(args[0]),candidatePath=Path.of(args[1]),mappingPath=Path.of(args[2]),actualPath=Path.of(args[3]);
        JsonObject mapping=JsonParser.parseString(Files.readString(mappingPath)).getAsJsonObject();Map<String,String> names=new HashMap<>();mapping.getAsJsonObject("memberNames").entrySet().forEach(entry->names.put(entry.getKey(),entry.getValue().getAsString()));
        ClassNode original,candidate,handler,originalHandler;
        try(ZipFile jar=new ZipFile(originalPath.toFile())){original=read(jar.getInputStream(jar.getEntry(TEA+".class")).readAllBytes());originalHandler=read(jar.getInputStream(jar.getEntry("net/farmingtales/ftboss/boss/BossHandler.class")).readAllBytes());}
        try(ZipFile jar=new ZipFile(candidatePath.toFile())){candidate=read(jar.getInputStream(jar.getEntry(TEA+".class")).readAllBytes());handler=read(jar.getInputStream(jar.getEntry("net/farmingtales/ftboss/boss/BossHandler.class")).readAllBytes());}
        ClassNode selected=new ClassNode();selected.name=original.name;selected.superName=original.superName;selected.version=original.version;selected.methods.add(method(original,"tryRevive"));
        ClassNode expected=new ClassNode();selected.accept(new ClassRemapper(expected,new Remapper(){
            @Override public String mapMethodName(String owner,String name,String desc){if(name.matches("m_[0-9]+_")){require(names.containsKey(name),"Official method mapping "+name);return names.get(name);}return name;}
            @Override public String mapFieldName(String owner,String name,String desc){if(name.matches("f_[0-9]+_")){require(names.containsKey(name),"Official field mapping "+name);return names.get(name);}return name;}
        }));platformBridges(expected);
        String originalGraph=graph(method(expected,"tryRevive"));require(originalGraph.equals(graph(method(candidate,"tryRevive"))),"Complete original tryRevive graph unchanged after official mapping");
        ClassNode actual=read(Files.readAllBytes(actualPath));require(graph(method(candidate,"tryRevive")).equals(graph(method(actual,"tryRevive"))),"Actual defined native tryRevive graph equals unchanged original");
        List<String> gate=new ArrayList<>();for(AbstractInsnNode instruction:method(expected,"tryRevive").instructions){if(instruction instanceof FieldInsnNode field&&field.name.equals("secondLifeUsed"))gate.add("secondLifeUsed:"+field.getOpcode());if(instruction instanceof MethodInsnNode call&&call.name.equals("isAlive"))gate.add("isAlive:"+call.desc);}
        require(gate.equals(List.of("secondLifeUsed:180","isAlive:()Z","secondLifeUsed:181")),"Original state guard precedes isAlive and state write");
        for(ClassNode value:List.of(originalHandler,handler)){List<String> calls=new ArrayList<>();for(AbstractInsnNode instruction:method(value,"onBossDeath").instructions)if(instruction instanceof MethodInsnNode call&&call.owner.equals(TEA))calls.add(call.name+call.desc);require(calls.equals(List.of("tryRevive()Z","dropRewards()V")),"Original and current actual death ingress preserve ordered revive and rewards");}
        ClassNode bad=copy(candidate);for(AbstractInsnNode instruction:method(bad,"tryRevive").instructions)if(instruction instanceof MethodInsnNode call&&call.name.equals("isAlive")){call.name="isRemoved";break;}require(!originalGraph.equals(graph(method(bad,"tryRevive"))),"Negative control changed alive predicate rejected");
        System.out.println(new Gson().toJson(Map.of("checks",checks,"negativeControls",1,"completeOriginalTryReviveGraphExact",true,"actualDefinedTryReviveOriginalGraphExact",true,"originalOnBossDeathCallsTryReviveBeforeRewards",true,"originalGuardRequiresAlive",true,"nativeZeroHealthDeathCannotSatisfyOriginalGuard",true,"revivalFeatureAccepted",false,"productChanged",false)));
    }
}
