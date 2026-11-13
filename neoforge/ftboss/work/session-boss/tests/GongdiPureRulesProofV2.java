import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.*;
public final class GongdiPureRulesProofV2 {
    private static ClassNode read(ZipFile jar,String name)throws Exception{ClassNode node=new ClassNode();new ClassReader(jar.getInputStream(jar.getEntry(name+".class")).readAllBytes()).accept(node,0);return node;}
    private static String body(MethodNode source){MethodNode copy=new MethodNode(source.access,source.name,source.desc,source.signature,source.exceptions.toArray(String[]::new));source.accept(copy);for(AbstractInsnNode instruction:copy.instructions.toArray())if(instruction instanceof FrameNode||instruction instanceof LineNumberNode)copy.instructions.remove(instruction);Set<LabelNode> targets=new HashSet<>();for(AbstractInsnNode instruction:copy.instructions.toArray())if(instruction instanceof JumpInsnNode branch)targets.add(branch.label);for(AbstractInsnNode instruction:copy.instructions.toArray())if(instruction instanceof LabelNode label&&!targets.contains(label))copy.instructions.remove(label);Textifier text=new Textifier();copy.instructions.accept(new TraceMethodVisitor(text));return text.getText().toString();}
    public static void main(String[] arguments)throws Exception{int verified=0;try(ZipFile original=new ZipFile(arguments[0]);ZipFile current=new ZipFile(arguments[1])){for(String suffix:List.of("GongdiCombatRules","GongdiCombatRules$Bleed","HiddenBossCastRules")){String name="net/farmingtales/ftboss/util/"+suffix;ClassNode before=read(original,name),after=read(current,name);for(MethodNode method:before.methods){MethodNode other=after.methods.stream().filter(value->value.name.equals(method.name)&&value.desc.equals(method.desc)).findFirst().orElseThrow();if(!body(method).equals(body(other)))throw new AssertionError(suffix+":"+method.name);verified++;}}}System.out.println("failures=0;originalPureRuleMethodBodiesEquivalent="+verified+";compileTimeLegacyRangeConstantsStillPending=true");}
}
