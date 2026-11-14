import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import net.farmingtales.ftboss.boss.ai.NativeManorDamageBridgeInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorMultiplayerInstaller;
public final class BossScalingActualLoadedAuditV2 {
    private static int checks;
    private static void require(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    private static ClassNode read(byte[] bytes){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);return node;}
    private static Map<String,String> graphs(ClassNode node){Map<String,String> result=new LinkedHashMap<>();for(MethodNode method:node.methods)require(result.put(method.name+method.desc,NativeManorDamageBridgeInstaller.graph(method))==null,"Unique full method signature");return result;}
    private static String header(ClassNode node){return node.version+"|"+node.access+"|"+node.name+"|"+node.signature+"|"+node.superName+"|"+node.interfaces+"|"+node.fields.stream().map(field->field.access+":"+field.name+":"+field.desc+":"+field.signature+":"+field.value).toList();}
    public static void main(String[] args)throws Exception{
        byte[] raw;try(ZipFile jar=new ZipFile(args[0])){raw=jar.getInputStream(jar.getEntry("net/farmingtales/ftboss/boss/ai/BossScaling.class")).readAllBytes();}
        ClassNode expected=read(raw),actual=read(Files.readAllBytes(Path.of(args[1])));Map<String,String> original=graphs(expected);int raid=net.farmingtales.ftboss.boss.ai.NativeManorRaidInstaller.install(expected);require(raid>0,"Existing plugin first applies its original native raid bridge");net.farmingtales.ftboss.boss.ai.NativeManorRaidInstaller.verifyInstalled(expected);int installed=NativeManorMultiplayerInstaller.install(expected);require(installed>0,"Existing product installer actually applies its original health and multiplayer adaptation");NativeManorMultiplayerInstaller.verifyInstalled(expected);NativeManorMultiplayerInstaller.verifyInstalled(actual);require(header(expected).equals(header(actual)),"Actual class metadata and fields exactly equal candidate simulated installation");require(graphs(expected).equals(graphs(actual)),"Every actual scaling method graph exactly equals existing product installer simulation");long changed=original.entrySet().stream().filter(entry->!entry.getValue().equals(graphs(expected).get(entry.getKey()))).count();require(changed==1,"Only original tick graph receives existing product adapter");Map<String,String> before=graphs(actual);require(NativeManorMultiplayerInstaller.install(actual)==0&&before.equals(graphs(actual)),"Exact actual adapter is idempotent");
        for(MethodNode method:actual.methods)new Analyzer<BasicValue>(new BasicVerifier()).analyze(actual.name,method);
        for(String fault:List.of("normalize","tick")){ClassNode damaged=read(Files.readAllBytes(Path.of(args[1])));MethodInsnNode selected=null;for(MethodNode method:damaged.methods)for(AbstractInsnNode node:method.instructions)if(node instanceof MethodInsnNode call&&call.name.equals(fault)&&call.owner.equals("net/farmingtales/ftboss/boss/ai/"+(fault.equals("normalize")?"ManorHealthBalance":"ManorMultiplayerBalance")))selected=call;require(selected!=null,"Exact existing adapter call found");selected.name="unexpected";boolean rejected=false;try{NativeManorMultiplayerInstaller.verifyInstalled(damaged);}catch(IllegalStateException expectedFailure){rejected=true;}require(rejected,"Strict existing adapter verifier rejects changed "+fault);}
        System.out.println("checks="+checks+";actualDefinedMethods="+actual.methods.size()+";allActualGraphsEqualExistingInstallerSimulation=true;existingAdapterIdempotent=true;negativeControls=2;gameplayInvoked=false;");
    }
}
