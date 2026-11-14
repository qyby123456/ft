import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.util.*;

public final class ZicaiCurioSilenceComposerV1 implements Opcodes {
    private static final String ROOT="net/farmingtales/ftboss/",HELPER=ROOT+"boss/ai/ZicaiCurioSilence",RULES=ROOT+"util/ZicaiCurioSilenceRules",CHOICE=HELPER+"$Choice";
    private static final String PLAYER="net/minecraft/server/level/ServerPlayer",STACK="net/minecraft/world/item/ItemStack",COMPOUND="net/minecraft/nbt/CompoundTag",TAG="net/minecraft/nbt/Tag",PROVIDER="net/minecraft/core/HolderLookup$Provider";
    private static final String OLD_DAMAGE="net/minecraftforge/event/entity/living/LivingDamageEvent",OLD_TICK="net/minecraftforge/event/TickEvent$PlayerTickEvent",NEW_TICK="net/neoforged/neoforge/event/tick/PlayerTickEvent$Post";
    private static final String SUBSCRIBER="Lnet/neoforged/fml/common/EventBusSubscriber;",SUBSCRIBE="Lnet/neoforged/bus/api/SubscribeEvent;";
    private static final Map<String,String> PINNED=Map.of(HELPER,"9d7cf5e0060b4a9df4da819450ac5a9883737546fb622053f2b023f85a1b4a1c",RULES,"5b7058c1325e5780221914dc6fa2e97f80a3602681a2f95e24cef0a733a2f545",CHOICE,"c04f47802e814238edd96545e1dd13ad08dbb89774d5c0b8b28353c8bab60969");
    private static final Map<String,String> NAMES=Map.ofEntries(
        Map.entry("m_20194_","getServer"),Map.entry("m_129783_","overworld"),Map.entry("m_46467_","getGameTime"),Map.entry("m_128469_","getCompound"),Map.entry("m_128437_","getList"),Map.entry("m_128473_","remove"),Map.entry("m_128456_","isEmpty"),Map.entry("m_128365_","put"),Map.entry("m_128461_","getString"),Map.entry("m_128451_","getInt"),Map.entry("m_7639_","getEntity"),Map.entry("m_7640_","getDirectEntity"),Map.entry("m_19749_","getOwner"),Map.entry("m_7500_","isCreative"),Map.entry("m_5833_","isSpectator"),Map.entry("m_20148_","getUUID"),Map.entry("m_41619_","isEmpty"),Map.entry("m_128728_","getCompound"),Map.entry("m_128454_","getLong"),Map.entry("m_217043_","getRandom"),Map.entry("m_188503_","nextInt"),Map.entry("m_128359_","putString"),Map.entry("m_128405_","putInt"),Map.entry("m_128356_","putLong"),Map.entry("f_41583_","EMPTY"),Map.entry("m_5661_","displayClientMessage"),Map.entry("m_237113_","literal"),Map.entry("m_7220_","append"),Map.entry("m_41786_","getDisplayName"),Map.entry("m_130946_","append"),Map.entry("m_150109_","getInventory"),Map.entry("m_36054_","add")
    );
    private static final Remapper PORT=new Remapper(){
        public String map(String name){
            if(name.equals(OLD_DAMAGE))return "net/neoforged/neoforge/event/entity/living/LivingDamageEvent$Post";
            if(name.equals(OLD_TICK))return NEW_TICK;
            if(name.equals("top/theillusivec4/curios/api/event/CurioEquipEvent"))return "top/theillusivec4/curios/api/event/CurioCanEquipEvent";
            if(name.equals("net/minecraftforge/common/util/LazyOptional"))return "java/util/Optional";
            if(name.equals("net/minecraftforge/eventbus/api/Event$Result"))return "net/neoforged/neoforge/common/util/TriState";
            if(name.equals("net/minecraftforge/fml/common/Mod$EventBusSubscriber"))return "net/neoforged/fml/common/EventBusSubscriber";
            if(name.equals("net/minecraftforge/fml/common/Mod$EventBusSubscriber$Bus"))return "net/neoforged/fml/common/EventBusSubscriber$Bus";
            if(name.startsWith("net/minecraftforge/eventbus/api/"))return name.replace("net/minecraftforge/eventbus/api/","net/neoforged/bus/api/");
            if(name.startsWith("net/minecraftforge/event/"))return name.replace("net/minecraftforge/event/","net/neoforged/neoforge/event/");
            return name;
        }
        public String mapMethodName(String owner,String name,String descriptor){
            if(owner.equals(OLD_DAMAGE)&&name.equals("getAmount"))return "getNewDamage";
            if(owner.equals("top/theillusivec4/curios/api/event/CurioEquipEvent")&&name.equals("setResult"))return "setEquipResult";
            return NAMES.getOrDefault(name,name);
        }
        public String mapFieldName(String owner,String name,String descriptor){
            if(owner.equals("net/minecraftforge/eventbus/api/Event$Result")&&name.equals("DENY"))return "FALSE";
            return NAMES.getOrDefault(name,name);
        }
    };
    private static int cancelGuards,postGuards,playerGetters,optionalResolvers,saveAdapters,parseAdapters;
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);}
    private static String sha(byte[] bytes)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}
    private static byte[] read(ZipFile archive,String name)throws Exception{return archive.getInputStream(archive.getEntry(name+".class")).readAllBytes();}
    private static ClassNode parse(byte[] bytes){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);return node;}
    private static MethodNode method(ClassNode owner,String name){return owner.methods.stream().filter(value->value.name.equals(name)).reduce((left,right)->{throw new AssertionError("AmbiguousMethod:"+name);}).orElseThrow();}
    private static List<AbstractInsnNode> ops(MethodNode member){List<AbstractInsnNode> result=new ArrayList<>();for(AbstractInsnNode instruction:member.instructions)if(instruction.getOpcode()>=0)result.add(instruction);return result;}
    private static int label(MethodNode member,LabelNode target){int index=0;for(AbstractInsnNode instruction:member.instructions){if(instruction==target)return index;if(instruction.getOpcode()>=0)index++;}throw new AssertionError("UnboundLabel");}
    private static String graph(MethodNode member){
        StringBuilder result=new StringBuilder(member.name+member.desc+":"+member.access+";");
        for(AbstractInsnNode instruction:ops(member)){
            result.append(instruction.getOpcode()).append(':');
            if(instruction instanceof VarInsnNode value)result.append(value.var);
            else if(instruction instanceof IntInsnNode value)result.append(value.operand);
            else if(instruction instanceof LdcInsnNode value)result.append(value.cst.getClass().getName()).append('=').append(value.cst);
            else if(instruction instanceof TypeInsnNode value)result.append(value.desc);
            else if(instruction instanceof FieldInsnNode value)result.append(value.owner).append('.').append(value.name).append(value.desc);
            else if(instruction instanceof MethodInsnNode value)result.append(value.owner).append('.').append(value.name).append(value.desc).append(value.itf);
            else if(instruction instanceof JumpInsnNode value)result.append(label(member,value.label));
            else if(instruction instanceof IincInsnNode value)result.append(value.var).append(',').append(value.incr);
            else if(instruction instanceof InvokeDynamicInsnNode value)result.append(value.name).append(value.desc).append(value.bsm).append(Arrays.deepToString(value.bsmArgs));
            else require(instruction instanceof InsnNode,"UnexpectedInstruction:"+instruction.getClass().getName());
            result.append(';');
        }
        for(TryCatchBlockNode block:member.tryCatchBlocks)result.append("try:").append(label(member,block.start)).append(',').append(label(member,block.end)).append(',').append(label(member,block.handler)).append(',').append(block.type);
        return result.toString();
    }
    private static MethodNode copy(MethodNode member){MethodNode result=new MethodNode(member.access,member.name,member.desc,member.signature,member.exceptions.toArray(String[]::new));member.accept(result);return result;}
    private static void removePrefix(MethodNode member,int count){List<AbstractInsnNode> instructions=ops(member);for(int index=0;index<count;index++)member.instructions.remove(instructions.get(index));}
    private static ClassNode adapted(ClassNode original){
        ClassNode normalized=new ClassNode();original.accept(normalized);
        MethodNode damage=method(normalized,"onDamage");List<AbstractInsnNode> damagePrefix=ops(damage);
        require(damagePrefix.get(0).getOpcode()==ALOAD&&damagePrefix.get(1) instanceof MethodInsnNode call&&call.owner.equals(OLD_DAMAGE)&&call.name.equals("isCanceled")&&call.desc.equals("()Z")&&damagePrefix.get(2).getOpcode()==IFNE,"OriginalCancellationGuardShape");removePrefix(damage,3);cancelGuards++;
        MethodNode tick=method(normalized,"onTick");List<AbstractInsnNode> tickPrefix=ops(tick);
        require(tickPrefix.get(0).getOpcode()==ALOAD&&tickPrefix.get(1) instanceof FieldInsnNode phase&&phase.name.equals("phase")&&tickPrefix.get(2) instanceof FieldInsnNode end&&end.name.equals("END")&&tickPrefix.get(3).getOpcode()==IF_ACMPNE,"OriginalEndPhaseGuardShape");removePrefix(tick,4);postGuards++;
        for(MethodNode member:normalized.methods)for(AbstractInsnNode instruction:member.instructions.toArray()){
            if(instruction instanceof FieldInsnNode field&&field.owner.equals(OLD_TICK)&&field.name.equals("player")){
                require(field.getOpcode()==GETFIELD&&field.desc.equals("Lnet/minecraft/world/entity/player/Player;"),"OriginalPlayerFieldShape");member.instructions.set(field,new MethodInsnNode(INVOKEVIRTUAL,OLD_TICK,"getEntity","()Lnet/minecraft/world/entity/player/Player;",false));playerGetters++;
            }else if(instruction instanceof MethodInsnNode call&&call.owner.equals("net/minecraftforge/common/util/LazyOptional")&&call.name.equals("resolve")){
                require(call.desc.equals("()Ljava/util/Optional;"),"OriginalResolveShape");member.instructions.remove(call);optionalResolvers++;
            }else if(instruction instanceof MethodInsnNode call&&call.owner.equals(STACK)&&call.name.equals("m_41739_")){
                require(call.getOpcode()==INVOKEVIRTUAL&&call.desc.equals("(L"+COMPOUND+";)L"+COMPOUND+";")&&(member.name.equals("silence")||member.name.equals("restore")),"OriginalSaveShape");member.instructions.insertBefore(call,new VarInsnNode(ALOAD,0));member.instructions.set(call,new MethodInsnNode(INVOKESTATIC,HELPER,"saveStack","(L"+STACK+";L"+COMPOUND+";L"+PLAYER+";)L"+COMPOUND+";",false));saveAdapters++;
            }else if(instruction instanceof MethodInsnNode call&&call.owner.equals(STACK)&&call.name.equals("m_41712_")){
                require(call.getOpcode()==INVOKESTATIC&&call.desc.equals("(L"+COMPOUND+";)L"+STACK+";")&&(member.name.equals("silence")||member.name.equals("restore")),"OriginalParseShape");member.instructions.insertBefore(call,new VarInsnNode(ALOAD,0));member.instructions.set(call,new MethodInsnNode(INVOKESTATIC,HELPER,"parseStack","(L"+COMPOUND+";L"+PLAYER+";)L"+STACK+";",false));parseAdapters++;
            }
        }
        require(cancelGuards==1&&postGuards==1&&playerGetters==1&&optionalResolvers==1&&saveAdapters==2&&parseAdapters==2,"ExactPlatformAdapterCounts");
        require(normalized.visibleAnnotations!=null,"OriginalSubscriberAnnotation");
        for(AnnotationNode annotation:normalized.visibleAnnotations)if(annotation.desc.equals("Lnet/minecraftforge/fml/common/Mod$EventBusSubscriber;"))for(int index=0;index<annotation.values.size();index+=2)if(annotation.values.get(index).equals("bus")){
            String[] value=(String[])annotation.values.get(index+1);require(value[1].equals("FORGE"),"OriginalForgeBus");value[1]="GAME";
        }
        Set<String> retiredMetadata=Set.of("net/minecraftforge/event/TickEvent$Phase","net/minecraftforge/eventbus/api/Event$Result","net/minecraftforge/fml/common/Mod$EventBusSubscriber");
        int previousMetadata=normalized.innerClasses.size();normalized.innerClasses.removeIf(value->retiredMetadata.contains(value.name));require(previousMetadata-normalized.innerClasses.size()==3,"OnlyRetiredPlatformInnerClassMetadataRemoved");
        int repairedInnerClasses=0;for(InnerClassNode inner:normalized.innerClasses)if(inner.name.equals(OLD_TICK)){inner.outerName="net/neoforged/neoforge/event/tick/PlayerTickEvent";inner.innerName="Post";repairedInnerClasses++;}require(repairedInnerClasses==1,"ExactNewPostInnerClassParent");
        ClassNode result=new ClassNode();normalized.accept(new ClassRemapper(result,PORT));
        MethodNode save=new MethodNode(ACC_PRIVATE|ACC_STATIC|ACC_SYNTHETIC,"saveStack","(L"+STACK+";L"+COMPOUND+";L"+PLAYER+";)L"+COMPOUND+";",null,null);
        save.instructions.add(new VarInsnNode(ALOAD,0));save.instructions.add(new VarInsnNode(ALOAD,2));save.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,PLAYER,"registryAccess","()Lnet/minecraft/core/RegistryAccess;",false));save.instructions.add(new VarInsnNode(ALOAD,1));save.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,STACK,"save","(L"+PROVIDER+";L"+TAG+";)L"+TAG+";",false));save.instructions.add(new TypeInsnNode(CHECKCAST,COMPOUND));save.instructions.add(new InsnNode(ARETURN));result.methods.add(save);
        MethodNode parse=new MethodNode(ACC_PRIVATE|ACC_STATIC|ACC_SYNTHETIC,"parseStack","(L"+COMPOUND+";L"+PLAYER+";)L"+STACK+";",null,null);
        parse.instructions.add(new VarInsnNode(ALOAD,1));parse.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,PLAYER,"registryAccess","()Lnet/minecraft/core/RegistryAccess;",false));parse.instructions.add(new VarInsnNode(ALOAD,0));parse.instructions.add(new MethodInsnNode(INVOKESTATIC,STACK,"parseOptional","(L"+PROVIDER+";L"+COMPOUND+";)L"+STACK+";",false));parse.instructions.add(new InsnNode(ARETURN));result.methods.add(parse);
        return result;
    }
    private static byte[] output(ClassNode owner){ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);owner.accept(writer);return writer.toByteArray();}
    private static void exact(MethodNode expected,MethodNode actual){require(graph(expected).equals(graph(actual)),"OriginalMappedGraphChanged:"+expected.name);}
    private static void annotations(ClassNode owner){
        AnnotationNode subscriber=owner.visibleAnnotations.stream().filter(value->value.desc.equals(SUBSCRIBER)).findFirst().orElseThrow();
        boolean correctBus=false,correctMod=false;for(int index=0;index<subscriber.values.size();index+=2){Object key=subscriber.values.get(index),value=subscriber.values.get(index+1);if(key.equals("modid"))correctMod=value.equals("ftboss");if(key.equals("bus"))correctBus=Arrays.equals((String[])value,new String[]{"Lnet/neoforged/fml/common/EventBusSubscriber$Bus;","GAME"});}
        require(correctBus&&correctMod,"ExactNeoForgeGameSubscriber");
        int subscribers=0;for(MethodNode member:owner.methods)if(member.visibleAnnotations!=null)for(AnnotationNode annotation:member.visibleAnnotations)if(annotation.desc.equals(SUBSCRIBE)){
            subscribers++;if(Set.of("onDamage","onEquip","onDeath").contains(member.name)){String expected=member.name.equals("onDeath")?"HIGHEST":"LOWEST";require(annotation.values!=null&&annotation.values.size()==2&&annotation.values.get(0).equals("priority")&&Arrays.equals((String[])annotation.values.get(1),new String[]{"Lnet/neoforged/bus/api/EventPriority;",expected}),"OriginalEventPriority:"+member.name);}
        }require(subscribers==8,"EveryOriginalLifecycleSubscriberPreserved");
    }
    public static void main(String[] arguments)throws Exception{
        Path base=Path.of(arguments[0]),original=Path.of(arguments[1]),destination=Path.of(arguments[2]);
        try(ZipFile previous=new ZipFile(base.toFile());ZipFile source=new ZipFile(original.toFile())){
            Map<String,byte[]> originals=new LinkedHashMap<>();for(String name:List.of(HELPER,RULES,CHOICE)){require(previous.getEntry(name+".class")==null,"DoublePatchOrUnexpectedBase:"+name);byte[] bytes=read(source,name);require(sha(bytes).equals(PINNED.get(name)),"OriginalClassHash:"+name);originals.put(name,bytes);}
            ClassNode originalHelper=parse(originals.get(HELPER)),expected=adapted(originalHelper);byte[] helper=output(expected);ClassNode finished=parse(helper);annotations(finished);
            require(finished.methods.size()==originalHelper.methods.size()+2,"OnlyRequiredCodecBridgesAdded");
            int originalGraphs=0,verified=0;for(MethodNode member:expected.methods){exact(member,method(finished,member.name));if(!Set.of("saveStack","parseStack").contains(member.name))originalGraphs++;}
            Map<String,byte[]> payloads=new LinkedHashMap<>();payloads.put(HELPER,helper);payloads.put(RULES,originals.get(RULES));payloads.put(CHOICE,originals.get(CHOICE));
            for(var entry:payloads.entrySet()){
                String raw=new String(entry.getValue(),StandardCharsets.ISO_8859_1);require(!raw.contains("net/minecraftforge/")&&!java.util.regex.Pattern.compile("[mf]_[0-9]+_").matcher(raw).find(),"NoUnmappedLegacySymbols:"+entry.getKey());
                new ClassReader(entry.getValue()).accept(new CheckClassAdapter(new ClassWriter(0),false),0);ClassNode owner=parse(entry.getValue());
                for(MethodNode member:owner.methods)if((member.access&(ACC_ABSTRACT|ACC_NATIVE))==0){new Analyzer<BasicValue>(new BasicVerifier()).analyze(owner.name,member);verified++;}
            }
            int negatives=0;
            MethodNode badEquip=copy(method(finished,"onEquip"));for(AbstractInsnNode instruction:badEquip.instructions)if(instruction instanceof FieldInsnNode field&&field.owner.equals("net/neoforged/neoforge/common/util/TriState")&&field.name.equals("FALSE")){field.name="TRUE";break;}
            try{exact(method(expected,"onEquip"),badEquip);}catch(AssertionError rejected){negatives++;}
            MethodNode badOwner=copy(method(finished,"owner"));for(AbstractInsnNode instruction:badOwner.instructions)if(instruction instanceof IntInsnNode constant&&constant.operand==8){constant.operand=7;break;}
            try{exact(method(expected,"owner"),badOwner);}catch(AssertionError rejected){negatives++;}
            MethodNode badPending=copy(method(finished,"pending"));for(AbstractInsnNode instruction:badPending.instructions)if(instruction instanceof LdcInsnNode constant&&constant.cst.equals("PlayerPersisted")){constant.cst="InvalidPersisted";break;}
            try{exact(method(expected,"pending"),badPending);}catch(AssertionError rejected){negatives++;}
            ClassNode rules=parse(originals.get(RULES));MethodNode badExpiry=copy(method(rules,"expires"));for(AbstractInsnNode instruction:badExpiry.instructions)if(instruction instanceof LdcInsnNode constant&&constant.cst.equals(100L)){constant.cst=99L;break;}
            try{exact(method(rules,"expires"),badExpiry);}catch(AssertionError rejected){negatives++;}
            require(negatives==4,"AllMeaningfulNegativeControlsRejected");
            for(var entry:payloads.entrySet()){Path path=destination.resolve(entry.getKey()+".class");Files.createDirectories(path.getParent());Files.write(path,entry.getValue(),StandardOpenOption.CREATE_NEW);}
            System.out.println("failures=0;addedClasses=3;originalHelperMappedGraphs="+originalGraphs+";codecBridges=2;originalPureRulesAndChoiceByteIdentical=true;subscribers=8;adapterCounts=1,1,1,1,2,2;asmVerifiedMethods="+verified+";negativeControls="+negatives+";");
        }
    }
}
