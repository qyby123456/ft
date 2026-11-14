import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import net.farmingtales.ftboss.boss.ai.NativeManorDamageBridgeInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorDeathCancelInstaller;
public final class ManorDeathCancelActualLoadedAuditV1 {
    private static int checks;
    private static void require(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static ClassNode read(Path path)throws Exception{ClassNode node=new ClassNode();new ClassReader(Files.readAllBytes(path)).accept(node,ClassReader.SKIP_FRAMES);return node;}
    private static Map<String,String> graphs(ClassNode node){Map<String,String> result=new LinkedHashMap<>();for(MethodNode method:node.methods)require(result.put(method.name+method.desc,NativeManorDamageBridgeInstaller.graph(method))==null,"Unique full method signature");return result;}
    public static void main(String[] args)throws Exception{
        ClassNode before=read(Path.of(args[0])),actual=read(Path.of(args[1]));Map<String,String> old=graphs(before),now=graphs(actual);require(before.methods.size()==342&&actual.methods.size()==342,"All original 342 methods retained");NativeManorDeathCancelInstaller.verifyInstalled(actual);ClassNode restored=NativeManorDeathCancelInstaller.originalView(actual);require(graphs(restored).equals(old),"All actual 342 graphs reverse exactly to frozen Stage40 runtime");long changed=old.entrySet().stream().filter(entry->!entry.getValue().equals(now.get(entry.getKey()))).count();require(changed==2,"Only actual manorHurt and die graphs change");for(MethodNode method:actual.methods)new Analyzer<BasicValue>(new BasicVerifier()).analyze(actual.name,method);require(NativeManorDeathCancelInstaller.install(actual)==0&&graphs(actual).equals(now),"Actual loaded adapter remains idempotent");ClassWriter writer=new ClassWriter(0);restored.accept(writer);Files.write(Path.of(args[2]),writer.toByteArray());int compared=0;
        Path previous=Path.of(args[0]).getParent(),directory=Path.of(args[1]).getParent();try(var files=Files.list(previous)){for(Path prior:files.filter(path->path.getFileName().toString().endsWith(".class")).toList()){String name=prior.getFileName().toString();if(name.equals("net_farmingtales_ftboss_entity_EasterBossEntity.class"))continue;Path current=directory.resolve(name);if(!Files.exists(current))continue;require(graphs(read(prior)).equals(graphs(read(current))),"Unrelated captured actual method graphs unchanged: "+name);compared++;}}require(compared>=15,"Substantial actual helper and engine graph preservation checked");System.out.println("checks="+checks+";actualDefinedMethods=342;changedActualMethods=2;unchangedBossMethods=340;allOriginalGraphsRecoveredExactly=true;actualLoadedInstallerIdempotent=true;otherCapturedClassesUnchanged="+compared+";asmVerifiedMethods=342;");
    }
}
