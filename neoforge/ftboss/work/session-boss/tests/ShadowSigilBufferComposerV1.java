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
import net.farmingtales.ftboss.boss.ai.NativeManorMixinPlugin;
public final class ShadowSigilBufferComposerV1 {
    private static final String MATERIAL="net/farmingtales/ftboss/item/BossMaterialDrops";
    private static final List<String> TARGETS=List.of("net/farmingtales/ftboss/item/FTBossItems","net/farmingtales/ftboss/item/FTCreativeTabs","net/farmingtales/ftboss/skill/BossRelicDrops");
    private static int checks,linkageChecks;
    private static final Map<String,ClassNode> hierarchy=new HashMap<>();
    private static void require(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    private static ClassNode read(byte[] payload){ClassNode node=new ClassNode();new ClassReader(payload).accept(node,ClassReader.EXPAND_FRAMES);return node;}
    private static byte[] bytes(ClassNode node){ClassWriter writer=new ClassWriter(0);node.accept(writer);return writer.toByteArray();}
    private static ClassNode read(ZipFile jar,String name)throws Exception{var entry=jar.getEntry(name+".class");require(entry!=null,"Original entry exists "+name);return read(jar.getInputStream(entry).readAllBytes());}
    private static String graph(MethodNode method){return NativeManorDamageBridgeInstaller.graph(method);}
    private static MethodNode method(ClassNode node,String name){List<MethodNode> found=node.methods.stream().filter(value->value.name.equals(name)).toList();require(found.size()==1,"Unique selected method "+name);return found.getFirst();}
    private static Map<String,String> graphs(ClassNode node){Map<String,String> result=new LinkedHashMap<>();for(MethodNode method:node.methods)require(result.put(method.name+method.desc,graph(method))==null,"Unique method signature");return result;}
    private static String header(ClassNode node){return node.version+"|"+node.access+"|"+node.name+"|"+node.signature+"|"+node.superName+"|"+node.interfaces+"|"+node.fields.stream().map(field->field.access+":"+field.name+":"+field.desc+":"+field.signature+":"+field.value).toList();}
    private static ClassNode lookup(String name)throws Exception{if(hierarchy.containsKey(name))return hierarchy.get(name);try(InputStream input=ClassLoader.getSystemResourceAsStream(name+".class")){require(input!=null,"Linkage owner exists "+name);ClassNode node=read(input.readAllBytes());hierarchy.put(name,node);return node;}}
    private static boolean member(String owner,String name,String desc,boolean field,Set<String> visited)throws Exception{if(!visited.add(owner))return false;ClassNode node=lookup(owner);if(field?node.fields.stream().anyMatch(value->value.name.equals(name)&&value.desc.equals(desc)):node.methods.stream().anyMatch(value->value.name.equals(name)&&value.desc.equals(desc)))return true;if(name.equals("<init>"))return false;if(node.superName!=null&&member(node.superName,name,desc,field,visited))return true;for(String parent:node.interfaces)if(member(parent,name,desc,field,visited))return true;return false;}
    private static void linkage(ClassNode node,MethodNode method)throws Exception{new Analyzer<BasicValue>(new BasicVerifier()).analyze(node.name,method);for(AbstractInsnNode instruction:method.instructions){if(instruction instanceof MethodInsnNode call){require(member(call.owner,call.name,call.desc,false,new HashSet<>()),"Method resolves "+call.owner+"."+call.name+call.desc);linkageChecks++;}if(instruction instanceof FieldInsnNode field){require(member(field.owner,field.name,field.desc,true,new HashSet<>()),"Field resolves "+field.owner+"."+field.name+field.desc);linkageChecks++;}}}
    private static MethodNode copy(MethodNode source){MethodNode result=new MethodNode(source.access,source.name,source.desc,source.signature,source.exceptions.toArray(String[]::new));source.accept(result);return result;}
    private static List<MethodInsnNode> calls(ClassNode node,String owner,String name){List<MethodInsnNode> result=new ArrayList<>();for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions)if(instruction instanceof MethodInsnNode call&&call.owner.equals(owner)&&call.name.equals(name))result.add(call);return result;}
    private static MethodNode containing(ClassNode node,AbstractInsnNode selected){return node.methods.stream().filter(method->Arrays.asList(method.instructions.toArray()).contains(selected)).findFirst().orElseThrow();}
    private static final String VISUAL="net/farmingtales/ftboss/client/render/ShadowSigilVisual";
    private static final String BUFFER="net/minecraft/client/renderer/MultiBufferSource$BufferSource";
    private record Removed(AbstractInsnNode instruction,AbstractInsnNode following){}
    private record Change(List<Removed> removed,List<AbstractInsnNode> inserted){}
    private static AbstractInsnNode previous(AbstractInsnNode node){AbstractInsnNode value=node.getPrevious();while(value!=null&&value.getOpcode()<0)value=value.getPrevious();return value;}
    private static List<AbstractInsnNode> executable(MethodNode method){return Arrays.stream(method.instructions.toArray()).filter(node->node.getOpcode()>=0).toList();}
    private static Change patch(ClassNode node){
        MethodNode render=method(node,"onRenderLevel");require(render.desc.equals("(Lnet/neoforged/neoforge/client/event/RenderLevelStageEvent;)V"),"Exact native render event descriptor");
        List<MethodInsnNode> getters=calls(node,BUFFER,"getBuffer");require(getters.size()==2&&getters.stream().allMatch(call->containing(node,call)==render),"Two exact original shared-buffer acquisitions");
        List<Removed> removed=new ArrayList<>();List<AbstractInsnNode> inserted=new ArrayList<>();
        for(int index=0;index<2;index++){
            MethodInsnNode getter=getters.get(index);AbstractInsnNode first=getter;int count=index==0?5:8;for(int step=0;step<count;step++)first=previous(first);
            require(first instanceof VarInsnNode variable&&variable.getOpcode()==Opcodes.ALOAD&&variable.var==(index==0?6:9),"Exact original cached acquisition block begins at expected local");
            AbstractInsnNode following=getter.getNext();require(following instanceof VarInsnNode stored&&stored.getOpcode()==Opcodes.ASTORE&&stored.var==6+index,"Exact original cached consumer storage");
            List<AbstractInsnNode> span=new ArrayList<>();for(AbstractInsnNode instruction=first;;instruction=instruction.getNext()){if(instruction.getOpcode()>=0)span.add(instruction);if(instruction==following)break;}
            require(span.size()==(index==0?7:10),"Exact cached acquisition instruction count");
            for(AbstractInsnNode instruction:span)removed.add(new Removed(instruction,instruction.getNext()));for(AbstractInsnNode instruction:span)render.instructions.remove(instruction);
        }
        for(int index=0;index<2;index++){
            String name=index==0?"renderSigil":"renderPillar";MethodInsnNode call=calls(node,VISUAL,name).getFirst();require(containing(node,call)==render,"Original geometry writer remains in actual event method");AbstractInsnNode consumer=call;
            for(int step=0;step<10;step++){consumer=previous(consumer);if(consumer instanceof VarInsnNode variable&&variable.getOpcode()==Opcodes.ALOAD&&variable.var==6+index)break;}
            require(consumer instanceof VarInsnNode variable&&variable.getOpcode()==Opcodes.ALOAD&&variable.var==6+index,"Unique cached consumer argument at original geometry writer");
            AbstractInsnNode following=consumer.getNext();removed.add(new Removed(consumer,following));InsnList acquisition=new InsnList();acquisition.add(new VarInsnNode(Opcodes.ALOAD,5));acquisition.add(new FieldInsnNode(Opcodes.GETSTATIC,VISUAL,index==0?"SIGIL":"PILLAR","Lnet/minecraft/resources/ResourceLocation;"));acquisition.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"net/minecraft/client/renderer/RenderType","eyes","(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;",false));acquisition.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,BUFFER,"getBuffer","(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;",false));acquisition.add(new InsnNode(Opcodes.DUP));acquisition.add(new VarInsnNode(Opcodes.ASTORE,6+index));inserted.addAll(Arrays.asList(acquisition.toArray()));render.instructions.insertBefore(consumer,acquisition);render.instructions.remove(consumer);
        }
        validate(node);return new Change(removed,inserted);
    }
    private static void validate(ClassNode node){
        MethodNode render=method(node,"onRenderLevel");List<MethodInsnNode> getters=calls(node,BUFFER,"getBuffer");require(getters.size()==2,"Exactly two just-in-time buffer acquisitions in original loop");
        for(int index=0;index<2;index++){
            MethodInsnNode getter=getters.get(index);AbstractInsnNode type=previous(getter);require(type instanceof MethodInsnNode call&&call.owner.equals("net/minecraft/client/renderer/RenderType")&&call.name.equals("eyes"),"Original eyes render type unchanged");AbstractInsnNode texture=previous(type);require(texture instanceof FieldInsnNode field&&field.owner.equals(VISUAL)&&field.name.equals(index==0?"SIGIL":"PILLAR"),"Correct original texture matched to geometry");
            AbstractInsnNode next=getter.getNext();require(next.getOpcode()==Opcodes.DUP&&next.getNext() instanceof VarInsnNode variable&&variable.getOpcode()==Opcodes.ASTORE&&variable.var==6+index,"Fresh consumer retained solely for existing final flush guards");
            AbstractInsnNode writer=next.getNext();int steps=0;while(!(writer instanceof MethodInsnNode call&&call.owner.equals(VISUAL)&&(call.name.equals("renderSigil")||call.name.equals("renderPillar")))){writer=writer.getNext();require(writer!=null&&++steps<12,"Writer immediately follows acquisition without intervening buffer switch");if(writer instanceof MethodInsnNode call)require(!call.name.equals("getBuffer")&&!call.name.equals("endBatch"),"No material switch before geometry writer");}
            require(((MethodInsnNode)writer).name.equals(index==0?"renderSigil":"renderPillar"),"Fresh matching consumer reaches original matching writer");
        }
        for(AbstractInsnNode instruction:executable(render))if(instruction instanceof JumpInsnNode jump&&jump.getOpcode()==Opcodes.IFNONNULL){AbstractInsnNode condition=previous(jump);require(!(condition instanceof VarInsnNode variable&&variable.getOpcode()==Opcodes.ALOAD&&(variable.var==6||variable.var==7)),"No stale cached-consumer prefetch guard remains");}
    }
    private static void reverse(ClassNode node,Change change){MethodNode render=method(node,"onRenderLevel");for(AbstractInsnNode instruction:change.inserted())render.instructions.remove(instruction);for(int index=change.removed().size()-1;index>=0;index--){Removed removed=change.removed().get(index);render.instructions.insertBefore(removed.following(),removed.instruction());}}
    public static void main(String[] args)throws Exception{
        Path output=Path.of(args[1]);try(ZipFile jar=new ZipFile(args[0])){
            ClassNode before=read(jar,VISUAL),after=read(bytes(before));Map<String,String> originalGraphs=graphs(before);Change change=patch(after);require(header(before).equals(header(after)),"Class fields, metadata and declaration unchanged");Map<String,String> changedGraphs=graphs(after);List<String> changed=originalGraphs.keySet().stream().filter(key->!originalGraphs.get(key).equals(changedGraphs.get(key))).toList();require(changed.size()==1&&changed.getFirst().startsWith("onRenderLevel("),"Only native render event graph changes");
            reverse(after,change);require(graphs(after).equals(originalGraphs),"Reverse exact acquisition changes fully recovers all old method graphs");patch(after);
            boolean duplicateRejected=false;try{patch(read(bytes(after)));}catch(AssertionError expected){duplicateRejected=true;}require(duplicateRejected,"Already-patched preimage negative control rejected");
            ClassNode wrong=read(bytes(after));MethodNode wrongRender=method(wrong,"onRenderLevel");for(AbstractInsnNode instruction:wrongRender.instructions)if(instruction instanceof FieldInsnNode field&&field.name.equals("SIGIL")){field.name="PILLAR";break;}boolean wrongRejected=false;try{validate(wrong);}catch(AssertionError expected){wrongRejected=true;}require(wrongRejected,"Mismatched texture negative control rejected");
            ClassNode extra=read(bytes(after));method(extra,"onRenderLevel").instructions.insert(new InsnNode(Opcodes.NOP));require(!graphs(extra).equals(changedGraphs),"Unexpected unrelated instruction negative control rejected");
            hierarchy.put(VISUAL,after);int verified=0;for(MethodNode method:after.methods){linkage(after,method);verified++;}ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);after.accept(writer);byte[] payload=writer.toByteArray();ClassNode serialized=read(payload);validate(serialized);require(graphs(serialized).equals(changedGraphs),"Serialized candidate graph equals exact proved composition");Path target=output.resolve(VISUAL+".class");Files.createDirectories(target.getParent());Files.write(target,payload);
            JsonObject receipt=new JsonObject();receipt.addProperty("checks",checks);receipt.addProperty("memberLinkageChecks",linkageChecks);receipt.addProperty("changedExistingMethods",1);receipt.addProperty("otherOriginalMethodGraphsRetained",before.methods.size()-1);receipt.addProperty("asmVerifiedMethods",verified);receipt.addProperty("negativeControls",3);receipt.addProperty("originalGeometryAlphaLifetimeCullingRenderTypesAndFlushesRetained",true);receipt.addProperty("originalTextureResourceBytesUnchanged",true);receipt.addProperty("sharedBufferConsumersReacquiredImmediatelyBeforeEachGeometryWriter",true);receipt.addProperty("beforeRenderGraph",originalGraphs.get(changed.getFirst()));receipt.addProperty("afterRenderGraph",changedGraphs.get(changed.getFirst()));Files.writeString(output.resolve("composition-proof.json"),new GsonBuilder().setPrettyPrinting().create().toJson(receipt));System.out.println("checks="+checks+";memberLinkageChecks="+linkageChecks+";changedExistingMethods=1;otherOriginalMethods="+(before.methods.size()-1)+";asmVerifiedMethods="+verified+";negativeControls=3;");
        }
    }
}
