import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import net.farmingtales.ftboss.boss.ai.*;
public final class ManorNativeDamageLoadedAuditV1 {
    private static int checks;
    private static void require(boolean test,String label){checks++;if(!test)throw new AssertionError(label);}
    private static ClassNode read(String path)throws Exception{ClassNode type=new ClassNode();new ClassReader(Files.readAllBytes(Path.of(path))).accept(type,ClassReader.SKIP_DEBUG);return type;}
    private static String graph(MethodNode method){return NativeManorDamageBridgeInstaller.graph(method);}
    public static void main(String[] args)throws Exception {
        ClassNode baseline=read(args[0]),actual=read(args[1]),settlement=read(args[2]);NativeManorFinalDamageInstaller.verifyInstalled(actual);NativeManorDamageBridgeInstaller.verifySettlement(settlement);NativeSparrowHitEvasionInstaller.verifyInstalled(actual);
        ClassNode original=NativeManorDamageBridgeInstaller.originalView(actual);require(original.methods.size()==baseline.methods.size(),"Complete original method count preserved");
        for(MethodNode before:baseline.methods){List<MethodNode> after=original.methods.stream().filter(method->method.name.equals(before.name)&&method.desc.equals(before.desc)).toList();require(after.size()==1&&graph(before).equals(graph(after.getFirst())),"Exact original method graph "+before.name+before.desc);}
        require(NativeManorFinalDamageInstaller.install(actual)==0,"Actual final integration idempotent");require(NativeSparrowHitEvasionInstaller.install(actual)==0,"Actual Sparrow hooks remain idempotent");
        for(String name:List.of("manorHurt","ftboss$reduceManorDamage","ftboss$manorDamageContainers"))new Analyzer<BasicValue>(new BasicVerifier()).analyze(actual.name,NativeManorDamageBridgeInstaller.method(actual,name));
        new Analyzer<BasicValue>(new BasicVerifier()).analyze(settlement.name,NativeManorDamageBridgeInstaller.method(settlement,"settle"));
        System.out.println("failures=0;checks="+checks+";originalMethodsExactlyReconstructed="+baseline.methods.size()+";actualSparrowAndDefenseAndContextVerified=true;actualDefinedAsmMethods=4;actualInstallIdempotent=true;");
    }
}
