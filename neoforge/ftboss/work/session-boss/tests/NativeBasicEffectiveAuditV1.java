import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class NativeBasicEffectiveAuditV1 {
    private static void show(ClassNode node,String phase){for(String name:List.of("syncCombatAnimation","finishManorFiveCast")){MethodNode method=node.methods.stream().filter(value->value.name.equals(name)).findFirst().orElseThrow();List<String> calls=Arrays.stream(method.instructions.toArray()).filter(value->value instanceof MethodInsnNode call&&call.owner.contains("dandanmotion")).map(value->{MethodInsnNode call=(MethodInsnNode)value;return call.owner+"."+call.name+call.desc;}).toList();System.out.println(phase+":"+name+":"+calls);}}
    public static void main(String[] arguments)throws Exception{try(ZipFile jar=new ZipFile(arguments[0])){ClassNode node=new ClassNode();new ClassReader(jar.getInputStream(jar.getEntry("net/farmingtales/ftboss/entity/EasterBossEntity.class")).readAllBytes()).accept(node,0);show(node,"raw");int basic=net.farmingtales.ftboss.boss.ai.NativeManorBasicInstaller.install(node);net.farmingtales.ftboss.boss.ai.NativeManorBasicInstaller.verifyInstalled(node);show(node,"after-original-existing-basic-installer");int cadence=net.farmingtales.ftboss.boss.ai.NativeManorCadenceInstaller.install(node);net.farmingtales.ftboss.boss.ai.NativeManorCadenceInstaller.verifyInstalled(node);show(node,"after-original-existing-cadence-installer");ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);Path output=Path.of(arguments[1]);Files.write(output,writer.toByteArray());System.out.println("failures=0;existingBasicMethodsInstalled="+basic+";existingCadenceMethodsInstalled="+cadence+";productChanged=false");}}
}
