import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import net.farmingtales.ftboss.boss.ai.NativeManorDamageBridgeInstaller;
public final class ManorOriginalEntryLoadedAuditV1 {
    private static int checks;
    private static void require(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static ClassNode read(String path)throws Exception{ClassNode node=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(path))).accept(node,0);return node;}
    private static Map<String,String> methods(ClassNode node){Map<String,String> values=new LinkedHashMap<>();for(MethodNode method:node.methods)require(values.put(method.name+method.desc,NativeManorDamageBridgeInstaller.graph(method))==null,"Unique exact method signature");return values;}
    public static void main(String[] args)throws Exception{
        ClassNode simulatedBefore=read(args[0]),simulatedAfter=read(args[1]),actualBefore=read(args[2]),actualAfter=read(args[3]);
        require(methods(simulatedBefore).equals(methods(actualBefore)),"All baseline actual method graphs equal pinned simulation");require(methods(simulatedAfter).equals(methods(actualAfter)),"All corrected actual method graphs equal proven simulation");require(actualBefore.methods.size()==342&&actualAfter.methods.size()==342,"All 342 actual Boss methods retained");
        require(NativeManorDamageBridgeInstaller.graph(NativeManorDamageBridgeInstaller.method(actualAfter,"hurt")).equals("7b5cc70744f2dea2e8287d8f575fe592c20e3cc6446c75e5fe60da93b63fbcb6"),"Full actual defined hurt equals mapped original");
        for(ClassNode node:List.of(actualBefore,actualAfter))new Analyzer<BasicValue>(new BasicVerifier()).analyze(node.name,NativeManorDamageBridgeInstaller.method(node,"hurt"));
        System.out.println("failures=0;checks="+checks+";actualBaselineMethods=342;actualCorrectedMethods=342;fullActualHurtMatchesOriginal=true;actualHurtAsmAnalyses=2;");
    }
}
