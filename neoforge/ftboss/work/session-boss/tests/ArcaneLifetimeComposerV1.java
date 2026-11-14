import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.util.*;
public final class ArcaneLifetimeComposerV1 implements Opcodes {
    private static final String SELF="net/farmingtales/ftboss/util/SpellVisualRules$Spell",PHASE="Lnet/farmingtales/ftboss/util/SpellVisualRules$Phase;",HELPER="net/farmingtales/ftboss/zheergenphantom/ArcaneReadability";
    private static final String BASE_SHA="54a98d19e15be44886eb436162325c1f05461c6f19d003517fbeeff9f5c87904",HELPER_SHA="6ce1aad1a8cb73002a29e1b84125ff2ec74e279a5ab3e237a7817a82767d5f47";
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);}
    private static String sha(byte[] bytes)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}
    private static byte[] bytes(ZipFile archive,String name)throws Exception{return archive.getInputStream(archive.getEntry(name+".class")).readAllBytes();}
    private static ClassNode node(byte[] bytes){ClassNode result=new ClassNode();new ClassReader(bytes).accept(result,0);return result;}
    private static MethodNode method(ClassNode owner,String name,String descriptor){return owner.methods.stream().filter(value->value.name.equals(name)&&value.desc.equals(descriptor)).reduce((left,right)->{throw new AssertionError("AmbiguousMethod");}).orElseThrow();}
    private static MethodNode copy(MethodNode source){MethodNode result=new MethodNode(source.access,source.name,source.desc,source.signature,source.exceptions.toArray(String[]::new));source.accept(result);return result;}
    private static List<AbstractInsnNode> instructions(MethodNode method){return Arrays.stream(method.instructions.toArray()).filter(value->value.getOpcode()>=0).toList();}
    private static String body(MethodNode method){MethodNode cloned=copy(method);for(AbstractInsnNode instruction:cloned.instructions.toArray())if(instruction instanceof FrameNode||instruction instanceof LineNumberNode)cloned.instructions.remove(instruction);Textifier text=new Textifier();cloned.instructions.accept(new TraceMethodVisitor(text));return text.getText().toString();}
    private static void verifyWrapper(MethodNode after,MethodNode before){
        MethodNode stripped=copy(after);List<AbstractInsnNode> exact=instructions(stripped);int calls=0;
        for(int index=0;index<exact.size();index++)if(exact.get(index) instanceof MethodInsnNode call&&call.owner.equals(HELPER)){
            require(call.getOpcode()==INVOKESTATIC&&call.name.equals("lifetime")&&call.desc.equals("(IL"+SELF+";"+PHASE+")I")&&!call.itf,"WrongHelperBinding");
            require(index>=2&&exact.get(index-2) instanceof VarInsnNode receiver&&receiver.getOpcode()==ALOAD&&receiver.var==0&&exact.get(index-1) instanceof VarInsnNode phase&&phase.getOpcode()==ALOAD&&phase.var==1,"WrongReceiverOrPhaseBinding");
            require(index+1<exact.size()&&exact.get(index+1).getOpcode()==IRETURN,"OriginalReturnBoundaryChanged");
            stripped.instructions.remove(exact.get(index-2));stripped.instructions.remove(exact.get(index-1));stripped.instructions.remove(call);calls++;
        }
        require(calls==1&&body(stripped).equals(body(before)),"PreviousLifetimeInstructionsChanged");
    }
    public static void main(String[] arguments)throws Exception{
        try(ZipFile base=new ZipFile(arguments[0]);ZipFile original=new ZipFile(arguments[1])){
            byte[] beforeBytes=bytes(base,SELF),helper=bytes(original,HELPER);require(sha(beforeBytes).equals(BASE_SHA)&&sha(helper).equals(HELPER_SHA),"PinnedWholeClassPreimagesDiffer");require(base.getEntry(HELPER+".class")==null,"HelperAlreadyPresent");
            ClassNode target=node(beforeBytes),before=node(beforeBytes),authority=node(bytes(original,SELF));MethodNode old=method(before,"lifetime","("+PHASE+")I"),changed=method(target,"lifetime",old.desc),forge=method(authority,"lifetime",old.desc);
            List<AbstractInsnNode> forgeInstructions=instructions(forge);List<MethodInsnNode> originalCalls=forgeInstructions.stream().filter(value->value instanceof MethodInsnNode call&&call.owner.equals(HELPER)).map(value->(MethodInsnNode)value).toList();require(originalCalls.size()==1,"OriginalWrapperNotUnique");
            int position=forgeInstructions.indexOf(originalCalls.getFirst());require(forgeInstructions.get(position-2) instanceof VarInsnNode receiver&&receiver.getOpcode()==ALOAD&&receiver.var==0&&forgeInstructions.get(position-1) instanceof VarInsnNode phase&&phase.getOpcode()==ALOAD&&phase.var==1&&forgeInstructions.get(position+1).getOpcode()==IRETURN,"OriginalWrapperArgumentsOrBoundaryDiffer");
            require(instructions(old).stream().filter(value->value.getOpcode()==IRETURN).count()==1,"CurrentReturnNotUnique");
            for(AbstractInsnNode instruction:changed.instructions.toArray())if(instruction.getOpcode()==IRETURN){InsnList insertion=new InsnList();insertion.add(new VarInsnNode(ALOAD,0));insertion.add(new VarInsnNode(ALOAD,1));insertion.add(originalCalls.getFirst().clone(new HashMap<>()));changed.instructions.insertBefore(instruction,insertion);}
            verifyWrapper(changed,old);
            int negatives=0;for(int mutation=0;mutation<2;mutation++){
                MethodNode wrong=copy(changed);List<AbstractInsnNode> exact=instructions(wrong);
                for(int index=0;index<exact.size();index++)if(exact.get(index) instanceof MethodInsnNode call&&call.owner.equals(HELPER)){if(mutation==0)((VarInsnNode)exact.get(index-2)).var=1;else call.name="wrongLifetime";break;}
                try{verifyWrapper(wrong,old);}catch(AssertionError rejected){negatives++;}
            }
            byte[] wrongPreimage=beforeBytes.clone();wrongPreimage[wrongPreimage.length-1]^=1;try{require(sha(wrongPreimage).equals(BASE_SHA),"WrongPreimageRejected");}catch(AssertionError rejected){negatives++;}require(negatives==3,"NegativeControlsNotRejected");
            int preserved=0;for(int index=0;index<before.methods.size();index++)if(before.methods.get(index)!=old){require(body(before.methods.get(index)).equals(body(target.methods.get(index))),"OtherEnumMethodChanged");preserved++;}
            ClassNode helperNode=node(helper);MethodNode helperMethod=method(helperNode,"lifetime","(IL"+SELF+";"+PHASE+")I");require((helperMethod.access&(ACC_PUBLIC|ACC_STATIC))==(ACC_PUBLIC|ACC_STATIC),"OriginalHelperNotPublicStatic");
            require(target.fields.size()==before.fields.size()&&target.interfaces.equals(before.interfaces)&&target.methods.size()==before.methods.size(),"EnumStructureChanged");
            ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);target.accept(writer);byte[] output=writer.toByteArray();ClassNode finalNode=node(output);int verified=0;
            for(ClassNode owner:List.of(finalNode,helperNode))for(MethodNode member:owner.methods)if((member.access&(ACC_ABSTRACT|ACC_NATIVE))==0){new Analyzer<BasicValue>(new BasicVerifier()).analyze(owner.name,member);verified++;}
            ClassReader reader=new ClassReader(output);reader.accept(new CheckClassAdapter(new ClassWriter(0),false),0);new ClassReader(helper).accept(new CheckClassAdapter(new ClassWriter(0),false),0);
            Path destination=Path.of(arguments[2]);for(var item:Map.of(SELF,output,HELPER,helper).entrySet()){Path file=destination.resolve(item.getKey()+".class");Files.createDirectories(file.getParent());Files.write(file,item.getValue(),StandardOpenOption.CREATE_NEW);}
            System.out.println("failures=0;originalHelperByteIdentical=true;insertedInstructions=3;otherEnumMethodsPreserved="+preserved+";methodsVerified="+verified+";negativeControls="+negatives+";originalReceiverPhaseAndReturnBoundary=true;enumStructureUnchanged=true");
        }
    }
}
