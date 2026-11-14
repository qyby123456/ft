import com.google.gson.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import net.farmingtales.ftboss.boss.ai.NativeManorDamageBridgeInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorMixinPlugin;
public final class ManorOriginalEntryGuardComposerV1 implements Opcodes {
    private static final String BOSS="net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String ORIGINAL="7b5cc70744f2dea2e8287d8f575fe592c20e3cc6446c75e5fe60da93b63fbcb6";
    private static int checks;
    private static void require(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    private static ClassNode read(byte[] bytes){ClassNode value=new ClassNode();new ClassReader(bytes).accept(value,0);return value;}
    private static byte[] bytes(ClassNode node){ClassWriter writer=new ClassWriter(0);node.accept(writer);return writer.toByteArray();}
    private static String hash(byte[] value)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));}
    private static String graph(MethodNode method){return NativeManorDamageBridgeInstaller.graph(method);}
    private static MethodNode hurt(ClassNode node){return NativeManorDamageBridgeInstaller.method(node,"hurt");}
    private static List<AbstractInsnNode> real(MethodNode method){return Arrays.stream(method.instructions.toArray()).filter(node->node.getOpcode()>=0).toList();}
    private static boolean load(AbstractInsnNode node,int opcode,int variable){return node instanceof VarInsnNode value&&value.getOpcode()==opcode&&value.var==variable;}
    private static int patch(MethodNode method){
        List<AbstractInsnNode> code=real(method);List<Integer> matches=new ArrayList<>();
        for(int index=9;index+2<code.size();index++)if(code.get(index) instanceof LdcInsnNode value&&Double.valueOf(500.0D).equals(value.cst))matches.add(index);
        require(matches.size()==1,"Unique original extra raw 500 guard");int index=matches.getFirst();
        require(load(code.get(index-9),FLOAD,2)&&code.get(index-8) instanceof MethodInsnNode call&&call.getOpcode()==INVOKESTATIC&&call.owner.equals("java/lang/Float")&&call.name.equals("isFinite")&&call.desc.equals("(F)Z"),"Exact finite-positive input guard retained");
        require(code.get(index-7) instanceof JumpInsnNode finite&&finite.getOpcode()==IFEQ,"Exact nonfinite reject branch");
        JumpInsnNode finite=(JumpInsnNode)code.get(index-7),lower=(JumpInsnNode)code.get(index-3),upper=(JumpInsnNode)code.get(index+2);
        require(load(code.get(index-6),FLOAD,2)&&code.get(index-5).getOpcode()==FCONST_0&&code.get(index-4).getOpcode()==FCMPG&&lower.getOpcode()==IFLE&&lower.label==finite.label,"Exact nonpositive reject branch");
        require(load(code.get(index-2),FLOAD,2)&&code.get(index-1).getOpcode()==F2D&&code.get(index+1).getOpcode()==DCMPL&&upper.getOpcode()==IFLE&&upper.label!=lower.label,"Exact extra upper bound branch");
        Set<AbstractInsnNode> removed=new HashSet<>(code.subList(index-2,index+3));
        for(AbstractInsnNode node:method.instructions.toArray())if(node.getOpcode()<0&&node.getPrevious()!=null&&removed.contains(node.getPrevious())&&node.getNext()!=null&&removed.contains(node.getNext()))require(false,"No internal label/frame/debug target in removed guard");
        lower.setOpcode(IFGT);lower.label=upper.label;for(AbstractInsnNode node:removed)method.instructions.remove(node);return removed.size();
    }
    private static void reverse(MethodNode method){
        List<AbstractInsnNode> code=real(method);List<Integer> matches=new ArrayList<>();
        for(int index=3;index<code.size();index++)if(code.get(index) instanceof JumpInsnNode jump&&jump.getOpcode()==IFGT&&code.get(index-1).getOpcode()==FCMPG&&code.get(index-2).getOpcode()==FCONST_0&&load(code.get(index-3),FLOAD,2))matches.add(index);
        require(matches.size()==1,"Unique restored original positive entry branch");int index=matches.getFirst();JumpInsnNode lower=(JumpInsnNode)code.get(index),finite=(JumpInsnNode)code.get(index-4);LabelNode accept=lower.label;
        lower.setOpcode(IFLE);lower.label=finite.label;InsnList extra=new InsnList();extra.add(new VarInsnNode(FLOAD,2));extra.add(new InsnNode(F2D));extra.add(new LdcInsnNode(500.0D));extra.add(new InsnNode(DCMPL));extra.add(new JumpInsnNode(IFLE,accept));method.instructions.insert(lower,extra);
    }
    private static Map<String,String> graphs(ClassNode node){Map<String,String> values=new LinkedHashMap<>();for(MethodNode method:node.methods)require(values.put(method.name+method.desc,graph(method))==null,"Unique existing method "+method.name);return values;}
    private static String header(ClassNode node){return node.version+"|"+node.access+"|"+node.name+"|"+node.signature+"|"+node.superName+"|"+node.interfaces+"|"+node.fields.stream().map(field->field.access+":"+field.name+":"+field.desc+":"+field.signature+":"+field.value).toList();}
    private static void compare(ClassNode before,ClassNode after,String changed){
        require(header(before).equals(header(after)),"Existing class/interface/field structure unchanged");Map<String,String> original=graphs(before),current=graphs(after);require(original.keySet().equals(current.keySet()),"Existing method set unchanged");
        for(String key:original.keySet())require(key.equals(changed)?!original.get(key).equals(current.get(key)):original.get(key).equals(current.get(key)),"Only selected complete method graph changed: "+key);
    }
    private static void applied(ClassNode node){new NativeManorMixinPlugin().postApply(BOSS.replace('/','.'),node,"net.farmingtales.ftboss.mixin.NativeManorRaidMixin",null);}
    public static void main(String[] args)throws Exception{
        Path out=Path.of(args[1]);byte[] raw;
        try(ZipFile jar=new ZipFile(args[0])){raw=jar.getInputStream(jar.getEntry(BOSS+".class")).readAllBytes();}
        ClassNode before=read(raw),after=read(raw);String signature=hurt(before).name+hurt(before).desc;int removed=patch(hurt(after));require(removed==5,"Exactly five extra opcodes removed");compare(before,after,signature);
        ClassNode reversed=read(bytes(after));reverse(hurt(reversed));require(graph(hurt(reversed)).equals(graph(hurt(before))),"Reverse edit restores entire previous entry graph");
        new Analyzer<BasicValue>(new BasicVerifier()).analyze(BOSS,hurt(after));byte[] patched=bytes(after);ClassNode roundtrip=read(patched);require(graph(hurt(roundtrip)).equals(graph(hurt(after))),"Patched class roundtrip exact graph");
        ClassNode effectiveBefore=read(raw),effectiveAfter=read(patched);applied(effectiveBefore);applied(effectiveAfter);require(effectiveAfter.methods.size()==342,"All 342 effective Boss methods retained");compare(effectiveBefore,effectiveAfter,signature);
        require(graph(hurt(effectiveBefore)).equals("0a1baad080587b81bd4366d68b1e37e3bd0beb5a07a3fb2f2d9263ac0579cedf"),"Baseline full actual hurt graph pin");require(graph(hurt(effectiveAfter)).equals(ORIGINAL),"Complete corrected effective hurt equals mapped original Forge");new Analyzer<BasicValue>(new BasicVerifier()).analyze(BOSS,hurt(effectiveAfter));
        ClassNode captured=read(Files.readAllBytes(Path.of(args[2])));require(graphs(captured).equals(graphs(effectiveBefore)),"All simulated baseline methods equal previous actual captured definition");
        ClassNode bad=read(raw);List<AbstractInsnNode> badCode=real(hurt(bad));LdcInsnNode limit=(LdcInsnNode)badCode.stream().filter(node->node instanceof LdcInsnNode constant&&Double.valueOf(500.0D).equals(constant.cst)).findFirst().orElseThrow();limit.cst=499.0D;boolean rejected=false;try{patch(hurt(bad));}catch(AssertionError expected){rejected=true;}require(rejected,"Negative control rejects changed upper-bound anchor");
        ClassNode badFinite=read(bytes(effectiveAfter));MethodInsnNode isFinite=(MethodInsnNode)real(hurt(badFinite)).stream().filter(node->node instanceof MethodInsnNode call&&call.owner.equals("java/lang/Float")&&call.name.equals("isFinite")).findFirst().orElseThrow();isFinite.name="isNaN";require(!graph(hurt(badFinite)).equals(ORIGINAL),"Negative control detects weakened finite guard");
        Files.write(out.resolve("EasterBossEntity.class"),patched);Files.write(out.resolve("simulated-before.class"),bytes(effectiveBefore));Files.write(out.resolve("simulated-after.class"),bytes(effectiveAfter));
        JsonObject proof=new JsonObject();proof.addProperty("checks",checks);proof.addProperty("failures",0);proof.addProperty("rawBeforeSha256",hash(raw));proof.addProperty("rawAfterSha256",hash(patched));proof.addProperty("fullEffectiveHurtBefore",graph(hurt(effectiveBefore)));proof.addProperty("fullEffectiveHurtAfter",graph(hurt(effectiveAfter)));proof.addProperty("effectiveMethodCount",effectiveAfter.methods.size());proof.addProperty("unchangedOtherEffectiveMethods",effectiveAfter.methods.size()-1);proof.addProperty("negativeControls",2);proof.addProperty("removedRealOpcodes",removed);proof.addProperty("finitePositiveSourceInvulnerabilityGuardsPreservedByFullOriginalGraph",true);Files.writeString(out.resolve("composition-proof.json"),new GsonBuilder().setPrettyPrinting().create().toJson(proof));System.out.println("failures=0;checks="+checks+";effectiveMethods=342;unchangedOtherMethods=341;removedExtraOpcodes=5;negativeControls=2;originalHurtGraphExact=true;");
    }
}
