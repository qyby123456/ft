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

public final class TeaOriginalCastComposerV1 implements Opcodes {
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
    private static final String MODEL="net/farmingtales/ftboss/client/model/SayaPlayerModel",COMBAT="net/farmingtales/ftboss/integ/spells/BossSpellCombat",PRE="net/neoforged/neoforge/event/tick/EntityTickEvent$Pre";
    private static final List<String> ADDITIONS=List.of("saya/SayaCastData","saya/SayaCastMotion","saya/SayaCastSync","saya/SayaCastLifecycle","saya/SayaCastCancellation","client/saya/SayaCastingPose","client/saya/SayaCastingPose$Memory");
    private static final Set<String> PURE=Set.of("saya/SayaCastData","saya/SayaCastMotion","saya/SayaCastSync","saya/SayaCastLifecycle");
    private static byte[] entry(ZipFile archive,String name)throws Exception{return archive.getInputStream(Objects.requireNonNull(archive.getEntry(name+".class"),name)).readAllBytes();}
    private static MethodNode modelSetup(ClassNode node){List<MethodNode> found=node.methods.stream().filter(value->value.name.equals("setupAnim")&&value.desc.equals("(L"+TEA+";FFFFF)V")).toList();require(found.size()==1,"Unique typed Tea setupAnim");return found.getFirst();}
    private static Map<String,String> graphs(ClassNode node){Map<String,String> result=new TreeMap<>();for(MethodNode value:node.methods)require(result.put(value.name+value.desc,graph(value))==null,"Unique method signature");return result;}
    private static ClassNode mapped(ClassNode original,Map<String,String> names){
        Map<String,String> classes=Map.of("net/farmingtales/ftboss/boss/NonManorFinaleDifficulty","net/farmingtales/ftboss/boss/NonManorFinale","net/minecraftforge/event/entity/living/LivingEvent$LivingTickEvent",PRE,"net/minecraftforge/eventbus/api/SubscribeEvent","net/neoforged/bus/api/SubscribeEvent","net/minecraftforge/eventbus/api/EventPriority","net/neoforged/bus/api/EventPriority","net/minecraftforge/fml/common/Mod$EventBusSubscriber","net/neoforged/fml/common/EventBusSubscriber","net/minecraftforge/fml/common/Mod$EventBusSubscriber$Bus","net/neoforged/fml/common/EventBusSubscriber$Bus");
        ClassNode result=new ClassNode();original.accept(new ClassRemapper(result,new Remapper(){
            @Override public String map(String name){return classes.getOrDefault(name,name);}
            @Override public String mapMethodName(String owner,String name,String desc){if(name.matches("m_[0-9]+_")){require(names.containsKey(name),"Official method mapping "+name);return names.get(name);}return name;}
            @Override public String mapFieldName(String owner,String name,String desc){if(name.matches("f_[0-9]+_")){require(names.containsKey(name),"Official field mapping "+name);return names.get(name);}return name;}
        }));
        for(MethodNode value:result.methods)for(AbstractInsnNode instruction:value.instructions)if(instruction instanceof MethodInsnNode call&&call.owner.equals(PRE)&&call.name.equals("getEntity")&&call.desc.equals("()Lnet/minecraft/world/entity/LivingEntity;")){call.desc="()Lnet/minecraft/world/entity/Entity;";bridges.add(Map.of("method",result.name+"#"+value.name,"kind","NeoForge Pre event getEntity return Entity"));}
        for(List<AnnotationNode> annotations:Arrays.asList(result.visibleAnnotations,result.invisibleAnnotations))if(annotations!=null)for(AnnotationNode annotation:annotations)if(annotation.desc.equals("Lnet/neoforged/fml/common/EventBusSubscriber;")&&annotation.values!=null)for(int index=0;index<annotation.values.size();index+=2)if(annotation.values.get(index).equals("bus")){String[] value=(String[])annotation.values.get(index+1);require(value[1].equals("FORGE"),"Original event bus explicitly FORGE");value[1]="GAME";bridges.add(Map.of("class",result.name,"kind","Forge FORGE subscriber bus to NeoForge GAME"));}
        return result;
    }
    private static void replaceMethod(ClassNode actual,MethodNode before,MethodNode replacement){require(before.name.equals(replacement.name)&&before.desc.equals(replacement.desc),"Original method signature exact");actual.methods.set(actual.methods.indexOf(before),replacement);}
    private static void verifySingleChange(ClassNode before,ClassNode after,String selected){Map<String,String> old=graphs(before),now=graphs(after);require(old.size()==now.size()&&old.containsKey(selected),"Existing class method set retained");require(!old.remove(selected).equals(now.remove(selected)),"Requested original graph restored");require(old.equals(now),"Every other existing method graph unchanged");}
    private static void exact(ClassNode expected,ClassNode actual){require(graphs(expected).equals(graphs(actual)),"All selected original method graphs exact "+actual.name);}
    private static String digest(byte[] bytes)throws Exception{return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));}
    public static void main(String[] args)throws Exception{
        Path originalPath=Path.of(args[0]),candidatePath=Path.of(args[1]),mappingPath=Path.of(args[2]),output=Path.of(args[3]);JsonObject mapping=JsonParser.parseString(Files.readString(mappingPath)).getAsJsonObject();Map<String,String> names=new HashMap<>();mapping.getAsJsonObject("memberNames").entrySet().forEach(value->names.put(value.getKey(),value.getValue().getAsString()));
        Map<String,ClassNode> expected=new LinkedHashMap<>(),actual=new LinkedHashMap<>();Map<String,byte[]> bytes=new LinkedHashMap<>();Map<String,String> originalDigests=new LinkedHashMap<>();int unchangedTea,unchangedModel;ClassNode teaBefore,modelBefore,combatBefore;
        try(ZipFile original=new ZipFile(originalPath.toFile());ZipFile candidate=new ZipFile(candidatePath.toFile())){
            teaBefore=read(entry(candidate,TEA));modelBefore=read(entry(candidate,MODEL));combatBefore=read(entry(candidate,COMBAT));
            ClassNode originalTea=read(entry(original,TEA)),teaSelection=new ClassNode();teaSelection.name=TEA;teaSelection.superName=originalTea.superName;teaSelection.version=originalTea.version;teaSelection.methods.add(method(originalTea,"startSignatureCast"));ClassNode teaExpected=mapped(teaSelection,names),teaActual=copy(teaBefore);replaceMethod(teaActual,method(teaActual,"startSignatureCast"),method(teaExpected,"startSignatureCast"));require(graph(method(teaExpected,"startSignatureCast")).equals(graph(method(teaActual,"startSignatureCast"))),"Complete original Tea cast action and duration graph exact");verifySingleChange(teaBefore,teaActual,"startSignatureCast(Lnet/farmingtales/ftboss/util/BossSpellRules$Kind;I)V");unchangedTea=teaBefore.methods.size()-1;actual.put(TEA,teaActual);
            ClassNode originalModel=read(entry(original,MODEL)),modelSelection=new ClassNode();modelSelection.name=MODEL;modelSelection.superName=originalModel.superName;modelSelection.version=originalModel.version;modelSelection.methods.add(modelSetup(originalModel));ClassNode modelExpected=mapped(modelSelection,names),modelActual=copy(modelBefore);replaceMethod(modelActual,modelSetup(modelActual),modelSetup(modelExpected));require(graph(modelSetup(modelExpected)).equals(graph(modelSetup(modelActual))),"Complete original model setupAnim graph exact");verifySingleChange(modelBefore,modelActual,modelSetup(modelActual).name+modelSetup(modelActual).desc);unchangedModel=modelBefore.methods.size()-1;actual.put(MODEL,modelActual);
            ClassNode combat=mapped(read(entry(original,COMBAT)),names);expected.put(COMBAT,copy(combat));actual.put(COMBAT,combat);require(!graphs(combatBefore).equals(graphs(combat)),"Original scheduling differs from previous parallel implementation");
            for(String addition:ADDITIONS){String name="net/farmingtales/ftboss/"+addition;require(candidate.getEntry(name+".class")==null,"Original missing helper added only once");byte[] payload=entry(original,name);ClassNode value=PURE.contains(addition)?read(payload):mapped(read(payload),names);originalDigests.put(name,digest(payload));expected.put(name,copy(value));actual.put(name,value);if(PURE.contains(addition))bytes.put(name,payload);}
            expected.put(TEA,copy(teaActual));expected.put(MODEL,copy(modelActual));
        }
        actual.forEach(hierarchy::put);for(ClassNode value:actual.values()){verifyLinks(value);for(MethodNode method:value.methods)new Analyzer<BasicValue>(new BasicVerifier()).analyze(value.name,method);exact(expected.get(value.name),value);if(!bytes.containsKey(value.name)){ClassWriter writer=writer();value.accept(writer);bytes.put(value.name,writer.toByteArray());}exact(expected.get(value.name),read(bytes.get(value.name)));}
        long parallelCalls=actual.get(COMBAT).methods.stream().flatMap(value->Arrays.stream(value.instructions.toArray())).filter(value->value instanceof MethodInsnNode call&&call.name.equals("parallelSpells")).count();require(parallelCalls==0,"No invented parallel spell release in restored original");long acceleration=actual.get(COMBAT).methods.stream().flatMap(value->Arrays.stream(value.instructions.toArray())).filter(value->value instanceof MethodInsnNode call&&call.owner.equals("net/farmingtales/ftboss/boss/NonManorFinale")&&call.name.equals("acceleratedTicks")).count();require(acceleration==3,"All three original accelerated tick callsites retained");
        int negatives=0;ClassNode wrong=copy(actual.get(TEA));for(AbstractInsnNode instruction:method(wrong,"startSignatureCast").instructions)if(instruction instanceof MethodInsnNode call&&call.name.equals("duration")){call.name="incorrectDuration";break;}require(!graphs(expected.get(TEA)).equals(graphs(wrong)),"Negative wrong Tea duration rejected");negatives++;
        wrong=copy(actual.get(MODEL));boolean changed=false;for(AbstractInsnNode instruction:modelSetup(wrong).instructions)if(instruction instanceof MethodInsnNode call&&call.owner.endsWith("/SayaCastingPose")){call.name="incorrectPose";changed=true;break;}require(changed&&!graphs(expected.get(MODEL)).equals(graphs(wrong)),"Negative missing original pose rejected");negatives++;
        wrong=copy(actual.get(COMBAT));changed=false;for(AbstractInsnNode instruction:method(wrong,"tick").instructions)if(instruction instanceof LdcInsnNode constant&&Long.valueOf(60).equals(constant.cst)){constant.cst=61L;changed=true;break;}require(changed&&!graphs(expected.get(COMBAT)).equals(graphs(wrong)),"Negative changed original cast age boundary rejected");negatives++;
        wrong=copy(actual.get("net/farmingtales/ftboss/saya/SayaCastData"));changed=false;for(AbstractInsnNode instruction:method(wrong,"<clinit>").instructions)if(instruction instanceof LdcInsnNode constant&&constant.cst instanceof Double number){constant.cst=number+0.125D;changed=true;break;}require(changed&&!graphs(expected.get(wrong.name)).equals(graphs(wrong)),"Negative changed local-only original animation data rejected without exporting values");negatives++;
        Map<String,String> outputHashes=new TreeMap<>();for(Map.Entry<String,byte[]> value:bytes.entrySet()){Path target=output.resolve(value.getKey()+".class");Files.createDirectories(target.getParent());Files.write(target,value.getValue());outputHashes.put(value.getKey(),digest(value.getValue()));}
        Map<String,Object> receipt=new LinkedHashMap<>();receipt.put("checks",checks);receipt.put("sdkMethodAndFieldLinksResolved",links);receipt.put("negativeControls",negatives);receipt.put("unchangedOtherTeaMethods",unchangedTea);receipt.put("unchangedOtherModelMethods",unchangedModel);receipt.put("originalCompleteCombatMethodCount",actual.get(COMBAT).methods.size());receipt.put("originalThreeAcceleratedCallsites",acceleration);receipt.put("parallelSpellCalls",parallelCalls);receipt.put("addedOriginalHelpers",ADDITIONS);receipt.put("platformBridges",bridges);receipt.put("classSha256",outputHashes);receipt.put("originalHelperClassSha256",originalDigests);receipt.put("localOnlyOriginalAnimationDataByteIdentical",outputHashes.get("net/farmingtales/ftboss/saya/SayaCastData").equals(originalDigests.get("net/farmingtales/ftboss/saya/SayaCastData")));receipt.put("allSelectedOriginalGraphsExact",true);receipt.put("animationOrModelPayloadIncludedInReceipt",false);receipt.put("modelOrMusicUploaded",false);Files.writeString(output.resolve("composition-proof.json"),new GsonBuilder().setPrettyPrinting().create().toJson(receipt));System.out.println(new Gson().toJson(receipt));
    }
}
