import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.util.*;
public final class ShadowRetirementComposerV3 implements Opcodes {
    private static final String ROOT="net/farmingtales/ftboss/",BLIND=ROOT+"effect/ShadowBlindEffect",SPELL=ROOT+"integ/spells/ManorSpellEffects",FILTER=ROOT+"zheergenphantom/ErosionFilter",MOB="net/minecraft/world/effect/MobEffect",HOLDER="net/minecraft/core/Holder",BUFF=ROOT+"boss/ai/PlayerBuffGuard";
    private static final Map<String,String> PINNED=Map.of(BUFF,"2a0635ab21fbaab075e3cceeaefc2bc20057ab0a660b806310a90d409dc6e683",BLIND,"cd3f71a95553675ff545f091554ad934e9defa59451566cca6456a219c51bf15",SPELL,"39d5c311d628f0f7a4a445acd4d28b784fc8f172a6ac12fab5b21757bc21da85");
    private static final Map<String,String> NAMES=Map.ofEntries(Map.entry("m_21124_","getEffect"),Map.entry("m_21195_","removeEffect"),Map.entry("m_9236_","level"),Map.entry("m_19564_","getAmplifier"),Map.entry("m_19572_","isVisible"),Map.entry("m_19575_","showIcon"),Map.entry("m_267577_","isInfiniteDuration"),Map.entry("m_19557_","getDuration"),Map.entry("m_147207_","addEffect"),Map.entry("f_46443_","isClientSide"),Map.entry("f_19610_","BLINDNESS"),Map.entry("f_216964_","DARKNESS"));
    private static final Remapper PORT=new Remapper(){
        public String map(String name){return switch(name){case MOB -> HOLDER;case "net/minecraftforge/registries/RegistryObject" -> "net/neoforged/neoforge/registries/DeferredHolder";case "net/minecraftforge/event/entity/living/MobEffectEvent$Remove" -> "net/neoforged/neoforge/event/entity/living/MobEffectEvent$Remove";case "net/minecraftforge/event/entity/living/MobEffectEvent$Applicable" -> "net/neoforged/neoforge/event/entity/living/MobEffectEvent$Applicable";case "net/minecraftforge/event/TickEvent$PlayerTickEvent" -> "net/neoforged/neoforge/event/tick/PlayerTickEvent$Post";default -> name;};}
        public String mapMethodName(String owner,String name,String descriptor){return NAMES.getOrDefault(name,name);}
        public String mapFieldName(String owner,String name,String descriptor){return NAMES.getOrDefault(name,name);}
    };
    private static int getters,casts;
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);}
    private static String sha(byte[] bytes)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}
    private static byte[] bytes(ZipFile archive,String name)throws Exception{return archive.getInputStream(archive.getEntry(name+".class")).readAllBytes();}
    private static ClassNode read(byte[] bytes){ClassNode result=new ClassNode();new ClassReader(bytes).accept(result,0);return result;}
    private static MethodNode method(ClassNode owner,String name){return owner.methods.stream().filter(value->value.name.equals(name)).reduce((left,right)->{throw new AssertionError("AmbiguousMethod:"+name);}).orElseThrow();}
    private static MethodNode copy(MethodNode source){MethodNode result=new MethodNode(source.access,source.name,source.desc,source.signature,source.exceptions.toArray(String[]::new));source.accept(result);return result;}
    private static AbstractInsnNode next(AbstractInsnNode source){AbstractInsnNode result=source.getNext();while(result!=null&&result.getOpcode()<0)result=result.getNext();return result;}
    private static ClassNode portClass(ClassNode original){
        ClassNode normalized=new ClassNode();original.accept(normalized);
        for(MethodNode member:normalized.methods)for(AbstractInsnNode instruction:member.instructions.toArray())if(instruction instanceof FieldInsnNode field&&field.owner.equals(ROOT+"effect/FTEffects")&&field.name.equals("SHADOW_BLIND")){
            AbstractInsnNode following=next(field);require(following instanceof MethodInsnNode call&&call.owner.equals("net/minecraftforge/registries/RegistryObject")&&call.name.equals("get")&&call.desc.equals("()Ljava/lang/Object;"),"LegacyRegistryAccessorShapeChanged");AbstractInsnNode cast=next(following);member.instructions.remove(following);getters++;
            if(cast instanceof TypeInsnNode typed&&typed.getOpcode()==CHECKCAST&&typed.desc.equals(MOB)){member.instructions.remove(cast);casts++;}
        }
        ClassNode result=new ClassNode();normalized.accept(new ClassRemapper(result,PORT));return result;
    }
    private static MethodNode portMethod(MethodNode original){MethodNode result=new MethodNode(original.access,original.name,PORT.mapMethodDesc(original.desc),null,original.exceptions.toArray(String[]::new));original.accept(new MethodRemapper(result,PORT));return result;}
    private static String body(MethodNode member){MethodNode cloned=copy(member);for(AbstractInsnNode instruction:cloned.instructions.toArray())if(instruction instanceof FrameNode||instruction instanceof LineNumberNode)cloned.instructions.remove(instruction);Textifier text=new Textifier();cloned.instructions.accept(new TraceMethodVisitor(text));return text.getText().toString();}
    private static String flat(MethodNode member){MethodNode cloned=copy(member);for(AbstractInsnNode instruction:cloned.instructions.toArray())if(instruction.getOpcode()<0)cloned.instructions.remove(instruction);Textifier text=new Textifier();cloned.instructions.accept(new TraceMethodVisitor(text));return text.getText().toString();}
    private static void replace(MethodNode target,MethodNode original){require(target.desc.equals(original.desc),"PortedDescriptorMismatch:"+target.name);target.instructions=original.instructions;target.tryCatchBlocks=original.tryCatchBlocks;target.maxStack=original.maxStack;target.maxLocals=original.maxLocals;target.localVariables=original.localVariables;target.visibleLocalVariableAnnotations=original.visibleLocalVariableAnnotations;target.invisibleLocalVariableAnnotations=original.invisibleLocalVariableAnnotations;}
    private static byte[] output(ClassNode source){ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);source.accept(writer);return writer.toByteArray();}
    private static void exact(MethodNode original,MethodNode current){require(body(original).equals(body(current)),"MappedOriginalInstructionsDiffer:"+original.name);}
    public static void main(String[] arguments)throws Exception{
        try(ZipFile base=new ZipFile(arguments[0]);ZipFile original=new ZipFile(arguments[1])){
            for(var pinned:PINNED.entrySet())require(sha(bytes(base,pinned.getKey())).equals(pinned.getValue()),"PinnedWholeClassPreimageChanged");
            byte[] filterOriginal=bytes(original,FILTER);require(sha(filterOriginal).equals("fcde7792ef98bd06079a7d5e7c03fa0b165dbff5ee72531c1acf4fd3ec7c00dd")&&base.getEntry(FILTER+".class")==null,"OriginalFilterOrBaseInventoryChanged");
            ClassNode originalFilter=read(filterOriginal),filter=portClass(originalFilter),filterExpected=portClass(originalFilter);require(getters==6&&casts==4,"ExactHolderRegistryAdaptationsDiffer");
            ClassNode blind=read(bytes(base,BLIND)),beforeBlind=read(bytes(base,BLIND)),authorityBlind=read(bytes(original,BLIND));
            for(String name:List.of("onRemove","applyTo","synchronize"))replace(method(blind,name),portMethod(method(authorityBlind,name)));
            ClassNode spell=read(bytes(base,SPELL)),beforeSpell=read(bytes(base,SPELL)),authoritySpell=read(bytes(original,SPELL));replace(method(spell,"effect"),portMethod(method(authoritySpell,"effect")));
            ClassNode buff=read(bytes(base,BUFF)),beforeBuff=read(bytes(base,BUFF)),authorityBuff=read(bytes(original,BUFF));
            List<String> retired=List.of("isInvulnerability","isBuff","isOverlong","shouldPurge","onApplicable","onPlayerTick","sweep");
            for(String name:retired){MethodNode source=portMethod(method(authorityBuff,name));List<Integer> opcodes=Arrays.stream(source.instructions.toArray()).filter(value->value.getOpcode()>=0).map(AbstractInsnNode::getOpcode).toList();require(opcodes.equals(List.of(RETURN))||opcodes.equals(List.of(ICONST_0,IRETURN)),"OriginalBuffMethodNotRetired:"+name);replace(method(buff,name),source);}
            int constants=0,constantChanges=0;for(String name:List.of("ENABLED","SCAN_PERIOD_TICKS","MAX_BUFF_TICKS","INVULNERABLE_RESISTANCE_AMPLIFIER")){FieldNode current=buff.fields.stream().filter(value->value.name.equals(name)).findFirst().orElseThrow(),source=authorityBuff.fields.stream().filter(value->value.name.equals(name)).findFirst().orElseThrow();require(current.desc.equals(source.desc)&&source.value!=null&&(source.access&(ACC_STATIC|ACC_FINAL))==(ACC_STATIC|ACC_FINAL),"OriginalBuffConstantShapeChanged");if(!Objects.equals(current.value,source.value))constantChanges++;current.value=source.value;constants++;}
            require(constants==4&&constantChanges==1&&buff.fields.stream().anyMatch(value->value.name.equals("ENABLED")&&Objects.equals(value.value,0)),"OriginalRetiredBuffConstantsDiffer");
            int exactMethods=0;for(MethodNode target:filter.methods){exact(method(filterExpected,target.name),target);exactMethods++;}
            for(String name:List.of("onRemove","applyTo","synchronize")){exact(portMethod(method(authorityBlind,name)),method(blind,name));exactMethods++;}exact(portMethod(method(authoritySpell,"effect")),method(spell,"effect"));exactMethods++;
            for(String name:retired){exact(portMethod(method(authorityBuff,name)),method(buff,name));exactMethods++;}
            MethodNode stripped=copy(method(spell,"effect"));List<AbstractInsnNode> instructions=Arrays.stream(stripped.instructions.toArray()).filter(value->value.getOpcode()>=0).toList();
            require(instructions.get(0) instanceof VarInsnNode owner&&owner.getOpcode()==ALOAD&&owner.var==0&&instructions.get(1) instanceof VarInsnNode effect&&effect.getOpcode()==ALOAD&&effect.var==2&&instructions.get(2) instanceof MethodInsnNode call&&call.getOpcode()==INVOKESTATIC&&call.owner.equals(FILTER)&&call.name.equals("suppress")&&instructions.get(3) instanceof JumpInsnNode branch&&branch.getOpcode()==IFEQ&&instructions.get(4).getOpcode()==RETURN&&next(branch.label)==instructions.get(5),"OriginalEffectGuardArgumentsOrBranchDiffer");
            for(int index=0;index<5;index++)stripped.instructions.remove(instructions.get(index));require(flat(stripped).equals(flat(method(beforeSpell,"effect"))),"PreviousEffectApplicationInstructionsChanged");
            int preserved=0;for(var pair:List.of(new ClassNode[]{beforeBlind,blind},new ClassNode[]{beforeSpell,spell},new ClassNode[]{beforeBuff,buff}))for(MethodNode member:pair[0].methods){if(pair[0]==beforeBlind&&List.of("onRemove","applyTo","synchronize").contains(member.name)||pair[0]==beforeSpell&&member.name.equals("effect")||pair[0]==beforeBuff&&retired.contains(member.name))continue;exact(member,method(pair[1],member.name));preserved++;}
            require(blind.fields.size()==beforeBlind.fields.size()&&blind.methods.size()==beforeBlind.methods.size()&&spell.fields.size()==beforeSpell.fields.size()&&spell.methods.size()==beforeSpell.methods.size(),"PreviousClassStructureChanged");require(buff.fields.size()==beforeBuff.fields.size()&&buff.methods.size()==beforeBuff.methods.size(),"PreviousBuffStructureChanged");
            int negatives=0;for(int mutation=0;mutation<3;mutation++){
                String name=mutation==0?"suppress":"removeCompanion";MethodNode wrong=copy(method(filter,name));boolean changed=false;
                for(AbstractInsnNode instruction:wrong.instructions.toArray()){
                    if(mutation==0&&instruction instanceof IntInsnNode constant&&constant.operand==7){constant.operand=6;changed=true;break;}
                    if(mutation==1&&instruction.getOpcode()==ICONST_2){wrong.instructions.set(instruction,new InsnNode(ICONST_3));changed=true;break;}
                    if(mutation==2&&instruction instanceof MethodInsnNode call&&call.name.equals("showIcon")){call.name="isAmbient";changed=true;break;}
                }
                require(changed,"NegativeMutationNotApplied");try{exact(method(filterExpected,name),wrong);}catch(AssertionError rejected){negatives++;}
            }
            MethodNode wrongEffect=copy(method(spell,"effect"));for(AbstractInsnNode instruction:wrongEffect.instructions.toArray())if(instruction instanceof VarInsnNode variable&&variable.getOpcode()==ALOAD){variable.var=1;break;}try{exact(portMethod(method(authoritySpell,"effect")),wrongEffect);}catch(AssertionError rejected){negatives++;}require(negatives==4,"WrongVariantDurationIconReceiverControlsAccepted");
            int verified=0;Path destination=Path.of(arguments[2]);for(ClassNode target:List.of(filter,blind,spell,buff)){
                byte[] payload=output(target);ClassNode finished=read(payload);new ClassReader(payload).accept(new CheckClassAdapter(new ClassWriter(0),false),0);
                for(MethodNode member:finished.methods)if((member.access&(ACC_ABSTRACT|ACC_NATIVE))==0){new Analyzer<BasicValue>(new BasicVerifier()).analyze(finished.name,member);verified++;}
                Path file=destination.resolve(target.name+".class");Files.createDirectories(file.getParent());Files.write(file,payload,StandardOpenOption.CREATE_NEW);
            }
            System.out.println("failures=0;originalMappedMethodsExact="+exactMethods+";originalRegistryGettersAdapted=3;originalEffectCastsAdapted=2;priorOtherMethodsPreserved="+preserved+";priorEffectBodyPreserved=true;originalEffectGuardInstructions=5;negativeControls="+negatives+";methodsAsmVerified="+verified+";originalBuffPublicMethodsRetired=7;originalBuffConstantsPinned=4;changedConstantValues=1;noMixinOrVisibilityOrEventMetadataChanges=true");
        }
    }
}
