import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.farmingtales.ftboss.boss.ai.NativeManorFinalDamageInstaller;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class ManorDamageLoadedClassAuditV1 {
    private static int checks;
    private static void require(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    private static ClassNode read(Path path)throws Exception {ClassNode type=new ClassNode();new ClassReader(Files.readAllBytes(path)).accept(type,ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);return type;}
    private static MethodNode method(ClassNode type,String name) {List<MethodNode> found=type.methods.stream().filter(method->method.name.equals(name)).toList();require(found.size()==1,"Unique actual loaded method "+name);return found.getFirst();}
    private static String graph(MethodNode method) {Textifier text=new Textifier();method.accept(new TraceMethodVisitor(text));return text.getText().toString();}
    private static long calls(MethodNode method,String owner,String name){long count=0;for(AbstractInsnNode instruction:method.instructions)if(instruction instanceof MethodInsnNode call&&call.owner.equals(owner)&&call.name.equals(name))count++;return count;}
    public static void main(String[] arguments)throws Exception {
        ClassNode before=read(Path.of(arguments[0])),after=read(Path.of(arguments[1])),living=read(Path.of(arguments[2]));
        require(before.name.equals(after.name)&&after.name.equals("net/farmingtales/ftboss/entity/EasterBossEntity"),"Actual defined Easter types bound");
        NativeManorFinalDamageInstaller.verifyInstalled(after);
        MethodNode oldReduction=method(before,"ftboss$reduceManorDamage"),newReduction=method(after,"ftboss$reduceManorDamage");
        require(oldReduction.tryCatchBlocks.isEmpty()&&calls(oldReduction,"java/util/Stack","push")==0,"Failed runtime actually lacked platform reduction context");
        require(newReduction.tryCatchBlocks.size()==1&&calls(newReduction,"java/util/Stack","push")==1&&calls(newReduction,"java/util/Stack","pop")==2,"Successful runtime has owned context and both cleanup paths");
        new Analyzer<BasicValue>(new BasicVerifier()).analyze(after.name,newReduction);
        List<String> preserved=new ArrayList<>();
        for(String name:new String[]{"hurt","manorHurt","hitFeedback","sparrowTick"}) {
            require(graph(method(before,name)).equals(graph(method(after,name))),"Existing actual loaded method unchanged: "+name);preserved.add(name);
        }
        require(living.fields.stream().anyMatch(field->field.name.equals("damageContainers")&&field.desc.equals("Ljava/util/Stack;")&&(field.access&Opcodes.ACC_PROTECTED)!=0),"Actual loaded LivingEntity owns protected NeoForge context stack");
        require(calls(method(living,"getDamageAfterMagicAbsorb"),"java/util/Stack","peek")>0,"Actual loaded magic reduction requires platform stack");
        MethodNode damaged=method(after,"ftboss$reduceManorDamage");damaged.tryCatchBlocks.clear();boolean rejected=false;
        try{NativeManorFinalDamageInstaller.verifyInstalled(after);}catch(IllegalStateException expected){rejected=true;}
        require(rejected,"Loaded-class cleanup deletion negative control rejected");
        System.out.println("failures=0;checks="+checks+";actualDefinedClassesAudited=3;oldEmptyContextConfirmed=true;newBalancedContextConfirmed=true;preservedLoadedMethods="+String.join(",",preserved)+";loadedMagicRequiresContext=true;negativeControls=1;");
    }
}
