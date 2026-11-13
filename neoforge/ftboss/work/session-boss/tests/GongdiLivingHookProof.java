import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class GongdiLivingHookProof implements Opcodes {
    private static final String ENTITY="net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String PLAYER="net/minecraft/world/entity/player/Player";
    private static final String LIVING="net/minecraft/world/entity/LivingEntity";
    private static ClassNode read(byte[] bytes) {ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);return node;}
    private static void require(boolean value,String message) {if(!value)throw new AssertionError(message);}
    private static String body(MethodNode method) {Textifier printer=new Textifier();method.accept(new TraceMethodVisitor(printer));StringWriter text=new StringWriter();printer.print(new PrintWriter(text));return text.toString();}
    public static void main(String[] arguments) throws Exception {
        try(ZipFile base=new ZipFile(arguments[0]);ZipFile reference=new ZipFile(arguments[1])) {
            ClassNode original=read(base.getInputStream(base.getEntry(ENTITY+".class")).readAllBytes());
            ClassNode current=read(base.getInputStream(base.getEntry(ENTITY+".class")).readAllBytes());
            ClassNode forge=read(reference.getInputStream(reference.getEntry(ENTITY+".class")).readAllBytes());
            int added=0;
            for(String name:List.of("onGongdiAbilityHit","addBloodRage")) {
                MethodNode source=current.methods.stream().filter(method->method.name.equals(name)&&method.desc.contains("L"+PLAYER+";")).findFirst().orElseThrow();
                String descriptor=source.desc.replace(PLAYER,LIVING);
                require(forge.methods.stream().anyMatch(method->method.name.equals(name)&&method.desc.equals(descriptor)),"Original Living overload absent:"+name);
                MethodNode port=new MethodNode(source.access,source.name,descriptor,source.signature,source.exceptions.toArray(String[]::new));source.accept(port);
                if(port.localVariables!=null) for(LocalVariableNode local:port.localVariables) local.desc=local.desc.replace(PLAYER,LIVING);
                int hudGuards=0;
                for(AbstractInsnNode instruction:port.instructions.toArray()) {
                    if(instruction instanceof FrameNode frame) {
                        if(frame.local!=null) for(int index=0;index<frame.local.size();index++) if(PLAYER.equals(frame.local.get(index)))frame.local.set(index,LIVING);
                    }
                    if(instruction instanceof MethodInsnNode call) {
                        if(call.owner.equals(PLAYER)&&call.name.equals("isAlive"))call.owner=LIVING;
                        if(call.owner.equals(ENTITY)&&call.name.equals("addBloodRage"))call.desc=call.desc.replace(PLAYER,LIVING);
                    }
                    if(name.equals("addBloodRage")&&instruction instanceof JumpInsnNode branch&&branch.getOpcode()==IFNULL&&instruction.getPrevious() instanceof VarInsnNode load&&load.var==2) {
                        port.instructions.insertBefore(branch,new TypeInsnNode(INSTANCEOF,PLAYER));branch.setOpcode(IFEQ);
                        AbstractInsnNode receiver=branch.getNext();require(receiver instanceof VarInsnNode variable&&variable.var==2,"Expected original HUD receiver");
                        port.instructions.insert(receiver,new TypeInsnNode(CHECKCAST,PLAYER));hudGuards++;
                    }
                }
                require(hudGuards==(name.equals("addBloodRage")?1:0),"Unexpected HUD guard count");
                current.methods.add(port);added++;
            }
            for(int index=0;index<original.methods.size();index++)require(body(original.methods.get(index)).equals(body(current.methods.get(index))),"Previous method changed");
            ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);current.accept(writer);byte[] bytes=writer.toByteArray();ClassNode written=read(bytes);int verified=0;
            for(MethodNode method:written.methods)if((method.access&(ACC_ABSTRACT|ACC_NATIVE))==0){new Analyzer<>(new BasicVerifier()).analyze(ENTITY,method);verified++;}
            Path output=Path.of(arguments[2]).resolve(ENTITY+".class");Files.createDirectories(output.getParent());Files.write(output,bytes);
            System.out.println("failures=0;addedOriginalLivingOverloads="+added+";allExistingEntityMethodsPreserved="+original.methods.size()+";methodsBasicVerified="+verified+";playerHudGuarded=true");
        }
    }
}
