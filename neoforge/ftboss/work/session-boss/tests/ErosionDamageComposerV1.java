import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.jar.JarFile;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.CheckClassAdapter;
public final class ErosionDamageComposerV1 implements Opcodes {
    private static final String ENTRY="net/farmingtales/ftboss/effect/ShadowErosionEffect.class";
    private static final String RULES="net/farmingtales/ftboss/zheergenphantom/ErosionRules";
    private static final String NERFS="net/farmingtales/ftboss/boss/ai/BossOutgoingDamageNerfs";
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);}
    private static byte[] read(Path jar,String entry)throws Exception{try(JarFile packed=new JarFile(jar.toFile())){return packed.getInputStream(packed.getJarEntry(entry)).readAllBytes();}}
    private static String sha(byte[] bytes)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));}
    private static ClassNode parse(byte[] bytes){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);return node;}
    private static ClassNode copy(ClassNode node){ClassNode result=new ClassNode();node.accept(result);return result;}
    private static MethodNode method(ClassNode node,String name){List<MethodNode> matches=node.methods.stream().filter(value->value.name.equals(name)).toList();require(matches.size()==1,"UniqueMethod:"+name);return matches.getFirst();}
    private static List<AbstractInsnNode> ops(MethodNode method){List<AbstractInsnNode> result=new ArrayList<>();for(AbstractInsnNode node:method.instructions)if(node.getOpcode()>=0)result.add(node);return result;}
    private static int label(MethodNode method,LabelNode target){int index=0;for(AbstractInsnNode node:method.instructions){if(node==target)return index;if(node.getOpcode()>=0)index++;}throw new AssertionError("UnboundLabel");}
    private static String code(MethodNode method){StringBuilder result=new StringBuilder();for(AbstractInsnNode node:ops(method)){result.append(node.getOpcode()).append(':');
        if(node instanceof VarInsnNode value)result.append(value.var);
        else if(node instanceof IntInsnNode value)result.append(value.operand);
        else if(node instanceof LdcInsnNode value)result.append(value.cst.getClass().getName()).append('=').append(value.cst);
        else if(node instanceof TypeInsnNode value)result.append(value.desc);
        else if(node instanceof FieldInsnNode value)result.append(value.owner).append('.').append(value.name).append(value.desc);
        else if(node instanceof MethodInsnNode value)result.append(value.owner).append('.').append(value.name).append(value.desc).append(value.itf);
        else if(node instanceof JumpInsnNode value)result.append(label(method,value.label));
        else require(node instanceof InsnNode,"UnexpectedInstruction");result.append(';');}
        for(TryCatchBlockNode block:method.tryCatchBlocks)result.append("try:").append(label(method,block.start)).append(',').append(label(method,block.end)).append(',').append(label(method,block.handler)).append(',').append(block.type);
        return result.toString();}
    private static MethodNode adapted(MethodNode original,boolean returnsBoolean){MethodNode result=new MethodNode(ASM9,original.access,original.name,original.desc,original.signature,original.exceptions.toArray(String[]::new));original.accept(result);
        Map<String,String> methods=Map.of("m_9236_","level","m_269291_","damageSources","m_269341_","fellOutOfWorld","m_6469_","hurt");
        Map<String,String> fields=Map.of("f_46443_","isClientSide","f_19802_","invulnerableTime");
        for(AbstractInsnNode node:new ArrayList<>(ops(result))){if(node instanceof MethodInsnNode value)value.name=methods.getOrDefault(value.name,value.name);if(node instanceof FieldInsnNode value)value.name=fields.getOrDefault(value.name,value.name);
            if(returnsBoolean&&node.getOpcode()==RETURN){result.instructions.insertBefore(node,new InsnNode(ICONST_1));result.instructions.set(node,new InsnNode(IRETURN));}}
        return result;}
    private static boolean originalExact(ClassNode original,ClassNode candidate){return code(adapted(method(original,"<init>"),false)).equals(code(method(candidate,"<init>")))&&code(adapted(method(original,"m_6584_"),false)).equals(code(method(candidate,"shouldApplyEffectTickThisTick")))&&code(adapted(method(original,"m_6742_"),true)).equals(code(method(candidate,"applyEffectTick")));}
    private static void patch(ClassNode node){int categories=0,helpers=0;for(AbstractInsnNode instruction:ops(method(node,"<init>")))if(instruction instanceof FieldInsnNode field&&field.owner.equals("net/minecraft/world/effect/MobEffectCategory")&&field.name.equals("HARMFUL")){field.name="BENEFICIAL";categories++;}
        for(AbstractInsnNode instruction:ops(method(node,"applyEffectTick")))if(instruction instanceof MethodInsnNode call&&call.owner.equals(NERFS)&&call.name.equals("zheergenDamage")&&call.desc.equals("(F)F")){call.owner=RULES;call.name="unscaledDamage";helpers++;}
        require(categories==1&&helpers==1,"ExactTwoReferencePreimage");}
    public static void main(String[] arguments)throws Exception{Path base=Path.of(arguments[0]),originalJar=Path.of(arguments[1]),output=Path.of(arguments[2]);byte[] before=read(base,ENTRY),source=read(originalJar,ENTRY);
        require(sha(before).equals("d1017d5e86d636992024ce41971c74092f7bf3f4494ac49ed683ce3c27a90f50"),"PinnedCurrentClass");require(sha(source).equals("167f6fa59f15d9e1c912bfe75c73e30d7bed13bbaeb366b9772f6887dfe6f0b3"),"PinnedOriginalClass");
        ClassNode originalRules=parse(read(originalJar,RULES+".class")),currentRules=parse(read(base,RULES+".class"));for(String name:new String[]{"damage","unscaledDamage"})require(code(method(originalRules,name)).equals(code(method(currentRules,name))),"ExactOriginalPureDamageHelper:"+name);
        ClassNode prior=parse(before),original=parse(source),candidate=copy(prior);require(!originalExact(original,prior),"RealOriginalPreimageDifference");patch(candidate);require(originalExact(original,candidate),"ThreeWholeOriginalMethodGraphsExact");
        List<AbstractInsnNode> beneficial=ops(method(original,"m_19486_"));require(beneficial.size()==2&&beneficial.get(0).getOpcode()==ICONST_1&&beneficial.get(1).getOpcode()==IRETURN,"OriginalBeneficialOverrideTrue");
        int preserved=0;for(MethodNode current:prior.methods)if(!current.name.equals("<init>")&&!current.name.equals("applyEffectTick")){require(code(current).equals(code(method(candidate,current.name))),"OtherNativeMethodPreserved:"+current.name);preserved++;}require(preserved==2,"TwoOtherMethodsPreserved");
        ClassNode reverse=copy(candidate);for(AbstractInsnNode node:ops(method(reverse,"<init>")))if(node instanceof FieldInsnNode field&&field.name.equals("BENEFICIAL"))field.name="HARMFUL";
        for(AbstractInsnNode node:ops(method(reverse,"applyEffectTick")))if(node instanceof MethodInsnNode call&&call.owner.equals(RULES)&&call.name.equals("unscaledDamage")){call.owner=NERFS;call.name="zheergenDamage";}
        for(MethodNode current:prior.methods)require(code(current).equals(code(method(reverse,current.name))),"OnlyTwoReferenceChanges:"+current.name);
        int negatives=0;for(int variant=0;variant<4;variant++){ClassNode wrong=copy(candidate);
            if(variant==0)for(AbstractInsnNode node:ops(method(wrong,"<init>")))if(node instanceof FieldInsnNode field&&field.name.equals("BENEFICIAL"))field.name="HARMFUL";
            if(variant==1)for(AbstractInsnNode node:ops(method(wrong,"applyEffectTick")))if(node instanceof MethodInsnNode call&&call.owner.equals(RULES)){call.owner=NERFS;call.name="zheergenDamage";}
            if(variant==2)for(AbstractInsnNode node:ops(method(wrong,"shouldApplyEffectTickThisTick")))if(node instanceof IntInsnNode value&&value.operand==20)value.operand=10;
            if(variant==3)((VarInsnNode)ops(method(wrong,"applyEffectTick")).getFirst()).var=0;
            require(!originalExact(original,wrong),"NegativeControl:"+variant);negatives++;}
        boolean idempotenceGuard=false;try{patch(copy(candidate));}catch(AssertionError expected){idempotenceGuard=true;}require(idempotenceGuard,"NoDoublePatch");
        ClassWriter writer=new ClassWriter(new ClassReader(before),ClassWriter.COMPUTE_MAXS);candidate.accept(writer);byte[] after=writer.toByteArray();ClassNode emitted=parse(after);require(originalExact(original,emitted),"EmittedOriginalGraphs");
        StringWriter verification=new StringWriter();CheckClassAdapter.verify(new ClassReader(after),false,new PrintWriter(verification));require(verification.toString().isBlank(),"ASM:"+verification);
        Path target=output.resolve(ENTRY);Files.createDirectories(target.getParent());Files.write(target,after,StandardOpenOption.CREATE_NEW);
        System.out.println("failures=0;changedReferences=2;wholeOriginalMappedMethodsExact=3;otherNativeMethodsPreserved=2;methodsAsmVerified=4;negativeControls="+negatives+";doublePatchRejected=true;originalBeneficialOverrideTrue=true;nativeBooleanReturnsPreserved=true;pureOriginalErosionDamageMethodsExact=2");}
}
