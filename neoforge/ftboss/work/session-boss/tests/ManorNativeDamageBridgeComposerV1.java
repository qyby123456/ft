import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import net.farmingtales.ftboss.boss.ai.NativeManorDamageBridgeInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorFinalDamageInstaller;
public final class ManorNativeDamageBridgeComposerV1 {
    private static int checks;
    private static void require(boolean test,String message){checks++;if(!test)throw new AssertionError(message);}
    private static ClassNode read(byte[] bytes){ClassNode type=new ClassNode();new ClassReader(bytes).accept(type,ClassReader.SKIP_DEBUG);return type;}
    private static ClassNode copy(ClassNode type){ClassNode result=new ClassNode();type.accept(result);return result;}
    private static String graph(MethodNode method){return NativeManorDamageBridgeInstaller.graph(method);}
    public static void main(String[] args)throws Exception {
        ClassNode boss=read(Files.readAllBytes(Path.of(args[1])));ClassNode settlement;
        try(JarFile jar=new JarFile(args[2])){settlement=read(jar.getInputStream(jar.getJarEntry("net/farmingtales/ftboss/boss/ai/ManorFinalDamage.class")).readAllBytes());}
        if(args[0].equals("pins")){for(String name:List.of("manorHurt","ftboss$reduceManorDamage"))System.out.println(name+"="+graph(NativeManorDamageBridgeInstaller.method(boss,name)));System.out.println("settle="+graph(NativeManorDamageBridgeInstaller.method(settlement,"settle")));return;}
        ClassNode before=copy(boss);require(NativeManorFinalDamageInstaller.install(boss)==2,"Upgrade exact Stage30 actual Boss");NativeManorFinalDamageInstaller.verifyInstalled(boss);require(NativeManorFinalDamageInstaller.install(boss)==0,"Native context integration idempotent");
        ClassNode original=NativeManorDamageBridgeInstaller.originalView(boss);require(before.methods.size()==original.methods.size(),"No original method lost");
        for(MethodNode method:before.methods)require(graph(method).equals(graph(original.methods.stream().filter(candidate->candidate.name.equals(method.name)&&candidate.desc.equals(method.desc)).findFirst().orElseThrow())),"Preserved original method "+method.name);
        int changed=0;for(MethodNode method:before.methods)if(!graph(method).equals(graph(boss.methods.stream().filter(candidate->candidate.name.equals(method.name)&&candidate.desc.equals(method.desc)).findFirst().orElseThrow()))){require(Set.of("manorHurt","ftboss$reduceManorDamage").contains(method.name),"Only damage owners changed");changed++;}
        require(changed==2&&boss.methods.size()==before.methods.size()+1,"Two method changes and one stack accessor");
        NativeManorDamageBridgeInstaller.installSettlement(settlement);NativeManorDamageBridgeInstaller.verifySettlement(settlement);
        for(String name:List.of("manorHurt","ftboss$reduceManorDamage","ftboss$manorDamageContainers"))new Analyzer<BasicValue>(new BasicVerifier()).analyze(boss.name,NativeManorDamageBridgeInstaller.method(boss,name));
        new Analyzer<BasicValue>(new BasicVerifier()).analyze(settlement.name,NativeManorDamageBridgeInstaller.method(settlement,"settle"));
        ClassNode damaged=copy(boss);for(AbstractInsnNode node:NativeManorDamageBridgeInstaller.method(damaged,"manorHurt").instructions)if(node instanceof MethodInsnNode call&&call.owner.endsWith("/NativeManorDamageBridge")&&call.name.equals("finish")){call.name="applied";break;}
        boolean rejected=false;try{NativeManorFinalDamageInstaller.verifyInstalled(damaged);}catch(IllegalStateException expected){rejected=true;}require(rejected,"Missing cleanup rejected by strict combined verifier");
        damaged=copy(settlement);for(AbstractInsnNode node:NativeManorDamageBridgeInstaller.method(damaged,"settle").instructions)if(node instanceof MethodInsnNode call&&call.owner.endsWith("/NativeManorDamageBridge")&&call.name.equals("pre")){call.name="absorption";break;}
        rejected=false;try{NativeManorDamageBridgeInstaller.verifySettlement(damaged);}catch(IllegalStateException expected){rejected=true;}require(rejected,"Missing real native pre rejected");
        Path output=Path.of(args[3]);ClassWriter writer=new ClassWriter(0);settlement.accept(writer);Files.write(output.resolve("ManorFinalDamage.class"),writer.toByteArray());
        writer=new ClassWriter(0);boss.accept(writer);Files.write(output.resolve("expected-defined-EasterBossEntity.class"),writer.toByteArray());
        ClassNode roundTrip=read(writer.toByteArray());NativeManorFinalDamageInstaller.verifyInstalled(roundTrip);
        System.out.println("failures=0;checks="+checks+";originalMethodsPreserved="+before.methods.size()+";changedMethods=2;newAccessor=1;asmMethods=4;negativeControls=2;roundTripVerified=true;");
    }
}
