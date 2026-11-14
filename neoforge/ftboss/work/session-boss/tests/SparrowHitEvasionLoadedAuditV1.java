import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.util.*;
import net.farmingtales.ftboss.boss.ai.NativeSparrowHitEvasionInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorDefenseInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorFinalDamageInstaller;
public final class SparrowHitEvasionLoadedAuditV1 {
    private static int checks;
    private static void require(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    private static ClassNode read(Path path)throws Exception{ClassNode type=new ClassNode();new ClassReader(Files.readAllBytes(path)).accept(type,ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);return type;}
    private static String graph(MethodNode method){Textifier text=new Textifier();method.accept(new TraceMethodVisitor(text));return text.getText().toString();}
    private static MethodNode method(ClassNode type,String name){List<MethodNode> found=type.methods.stream().filter(method->method.name.equals(name)).toList();require(found.size()==1,"Unique actual loaded method "+name);return found.getFirst();}
    public static void main(String[] arguments)throws Exception {
        ClassNode baseline=read(Path.of(arguments[0])),candidate=read(Path.of(arguments[1]));
        require(baseline.name.equals(candidate.name),"Actual defined classes have identical identity");
        NativeSparrowHitEvasionInstaller.verifyInstalled(candidate);NativeManorDefenseInstaller.verifyInstalled(candidate);NativeManorFinalDamageInstaller.verifyInstalled(candidate);
        require(NativeSparrowHitEvasionInstaller.install(candidate)==0,"Actual final defined hook installation idempotent");
        Map<String,MethodNode> originals=new HashMap<>();for(MethodNode method:baseline.methods)originals.put(method.name+method.desc,method);
        List<String> changed=new ArrayList<>();int preserved=0;
        for(MethodNode method:candidate.methods){MethodNode before=originals.remove(method.name+method.desc);require(before!=null,"No unintended actual defined method added");if(!graph(before).equals(graph(method))){require(Set.of("hitFeedback","sparrowTick").contains(method.name),"Only original evasion hook methods differ");changed.add(method.name);new Analyzer<BasicValue>(new BasicVerifier()).analyze(candidate.name,method);}else preserved++;}
        require(originals.isEmpty()&&changed.size()==2,"All original methods retained; exactly two original hook owners changed");
        require(graph(method(baseline,"ftboss$reduceManorDamage")).equals(graph(method(candidate,"ftboss$reduceManorDamage"))),"Accepted Stage29 owned reduction context unchanged");
        MethodNode feedback=method(candidate,"hitFeedback");
        for(AbstractInsnNode instruction:feedback.instructions)if(instruction instanceof MethodInsnNode call&&call.owner.endsWith("/SparrowHitEvasion")&&call.name.equals("confirmedHit")){call.name="prepare";break;}
        boolean rejected=false;try{NativeManorDefenseInstaller.verifyInstalled(candidate);}catch(IllegalStateException expected){rejected=true;}
        require(rejected,"Corrupt evasion prefix still rejected by original defense verifier");
        System.out.println("failures=0;checks="+checks+";actualDefinedBossMethodsPreserved="+preserved+";changedMethods="+String.join(",",changed)+";originalDefenseVerifierPreserved=true;stage29DamageContextUnchanged=true;actualInstallationIdempotent=true;negativeControls=1;");
    }
}
