import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import net.farmingtales.ftboss.boss.ai.NativeManorDamageBridgeInstaller;
import net.farmingtales.ftboss.boss.ai.NativeAnimationEmitterInstaller;
public final class YouziOriginalSummonLoadedAuditV1 {
    private static int checks;
    private static void require(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static ClassNode read(byte[] payload){ClassNode node=new ClassNode();new ClassReader(payload).accept(node,0);return node;}
    private static byte[] bytes(ClassNode node){ClassWriter writer=new ClassWriter(0);node.accept(writer);return writer.toByteArray();}
    private static Map<String,String> graphs(ClassNode node){Map<String,String> result=new LinkedHashMap<>();for(MethodNode method:node.methods)require(result.put(method.name+method.desc,NativeManorDamageBridgeInstaller.graph(method))==null,"Unique full signature");return result;}
    public static void main(String[] args)throws Exception{
        ClassNode expected=read(Files.readAllBytes(Path.of(args[0]))),actual=read(Files.readAllBytes(Path.of(args[1])));require(graphs(expected).equals(graphs(actual)),"All 342 actual method graphs equal frozen proven composition");require(actual.methods.size()==342,"All effective original methods retained");for(String name:List.of("hurt","youziAddHit","summonMiniClones","supportSparrow"))new Analyzer<BasicValue>(new BasicVerifier()).analyze(actual.name,NativeManorDamageBridgeInstaller.method(actual,name));
        Map<String,String> before=graphs(actual);NativeAnimationEmitterInstaller.verifyInstalled(actual);NativeAnimationEmitterInstaller.install(actual);require(before.equals(graphs(actual)),"Exact original callback verification/install is idempotent");
        for(String fault:List.of("wrong-original-cast","wrong-player-cap")){ClassNode damaged=read(bytes(actual));MethodNode method=NativeManorDamageBridgeInstaller.method(damaged,"summonMiniClones");if(fault.equals("wrong-original-cast")){MethodInsnNode callback=Arrays.stream(method.instructions.toArray()).filter(value->value instanceof MethodInsnNode invoke&&invoke.name.equals("summoned")).map(value->(MethodInsnNode)value).findFirst().orElseThrow();AbstractInsnNode previous=callback.getPrevious();while(previous.getOpcode()<0)previous=previous.getPrevious();require(previous instanceof TypeInsnNode,"Exact original callback cast exists");((TypeInsnNode)previous).desc="net/minecraft/world/entity/Entity";}else{MethodInsnNode cap=Arrays.stream(method.instructions.toArray()).filter(value->value instanceof MethodInsnNode invoke&&invoke.name.equals("cap")).map(value->(MethodInsnNode)value).findFirst().orElseThrow();cap.name="count";}boolean rejected=false;try{NativeAnimationEmitterInstaller.verifyInstalled(damaged);}catch(IllegalStateException expectedFailure){rejected=true;}require(rejected,"Strict original emitter verification rejects "+fault);}
        System.out.println("checks="+checks+";actualDefinedMethods=342;allActualGraphsEqualComposition=true;exactEmitterIdempotent=true;negativeControls=2;originalHurtUnchanged=true;");
    }
}
