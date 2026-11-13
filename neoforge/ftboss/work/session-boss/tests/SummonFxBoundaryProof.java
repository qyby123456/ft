import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class SummonFxBoundaryProof {
    private static final String PREFIX="net/farmingtales/ftboss/";
    private static final String HELPER=PREFIX+"butcherskills/ButcherSummonFx";
    private static int verified;
    private static int preserved;
    private static int redirects;
    private static int ticks;

    private static void require(boolean value,String message) {
        if (!value) throw new AssertionError(message);
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);return node;
    }

    private static String body(MethodNode source,boolean normalize) {
        MethodNode copy=new MethodNode(source.access,source.name,source.desc,source.signature,source.exceptions.toArray(String[]::new));source.accept(copy);
        if (normalize) {
            for (AbstractInsnNode instruction:copy.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode call) || !call.owner.equals(HELPER)) continue;
                if (call.name.equals("addWithFx")) {
                    require(List.of("spawnClone","tryStart","summonWave").contains(copy.name),"Unexpected summon redirect");
                    require(call.desc.equals("(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)Z"),"Wrong spawn descriptor");
                    call.setOpcode(Opcodes.INVOKEVIRTUAL);call.owner="net/minecraft/server/level/ServerLevel";call.name="addFreshEntity";call.desc="(Lnet/minecraft/world/entity/Entity;)Z";redirects++;
                } else {
                    require(copy.name.equals("tick") && call.name.equals("tickSummons") && call.desc.equals("(Lnet/minecraft/server/level/ServerLevel;J)V"),"Unexpected tick callback");
                    AbstractInsnNode time=call.getPrevious();
                    require(time instanceof MethodInsnNode value && value.name.equals("getGameTime") && value.desc.equals("()J"),"Wrong time source");
                    AbstractInsnNode second=time.getPrevious();AbstractInsnNode first=second.getPrevious();
                    require(first instanceof VarInsnNode initial && second instanceof VarInsnNode repeated && initial.getOpcode()==Opcodes.ALOAD && repeated.getOpcode()==Opcodes.ALOAD && initial.var==repeated.var,"Wrong actual level identity");
                    copy.instructions.remove(first);copy.instructions.remove(second);copy.instructions.remove(time);copy.instructions.remove(call);ticks++;
                }
            }
        }
        for (AbstractInsnNode instruction:copy.instructions.toArray()) if (instruction instanceof FrameNode || instruction instanceof LineNumberNode) copy.instructions.remove(instruction);
        Textifier text=new Textifier();copy.instructions.accept(new TraceMethodVisitor(text));return text.getText().toString();
    }

    public static void main(String[] arguments) throws Exception {
        Path compiled=Path.of(arguments[1]);
        try (ZipFile base=new ZipFile(arguments[0])) {
            for (String name:List.of("boss/MeatBoss","butcherskills/ButcherBloodBrand","butcherskills/ButcherBloodBrand$State")) {
                String key=PREFIX+name+".class";ClassNode before=read(base.getInputStream(base.getEntry(key)).readAllBytes());ClassNode after=read(Files.readAllBytes(compiled.resolve(key)));
                require(before.fields.stream().map(value->value.name+value.desc+value.access).toList().equals(after.fields.stream().map(value->value.name+value.desc+value.access).toList()),"Unrelated fields changed");
                require(before.methods.size()==after.methods.size(),"Unrelated methods added");
                for (MethodNode method:after.methods) {
                    new Analyzer<>(new BasicVerifier()).analyze(after.name,method);verified++;
                    MethodNode prior=before.methods.stream().filter(value->value.name.equals(method.name) && value.desc.equals(method.desc)).findFirst().orElseThrow();
                    require(body(prior,false).equals(body(method,true)),"Unrelated instructions changed: "+name+"."+method.name);preserved++;
                }
            }
            for (String name:List.of("","$Pulse","$Pulses")) {
                byte[] bytes=Files.readAllBytes(compiled.resolve(HELPER+name+".class"));ClassNode node=read(bytes);
                require(!new String(bytes,java.nio.charset.StandardCharsets.ISO_8859_1).contains("net/minecraft/client/"),"Client dependency introduced");
                for (MethodNode method:node.methods) {new Analyzer<>(new BasicVerifier()).analyze(node.name,method);verified++;}
            }
        }
        require(redirects==3 && ticks==1,"Incomplete original ingress coverage");
        System.out.println("failures=0;methodsVerified="+verified+";existingMethodsPreserved="+preserved+";exactOriginalSpawnRedirects="+redirects+";actualLevelTickCallback="+ticks+";allFourSkillInstructionsPreserved=true");
    }
}
