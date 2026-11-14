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

public final class SummonFxBoundaryProofV2 {
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
        Path linked=Path.of(arguments[1]);Path output=Path.of(arguments[2]);
        try (ZipFile base=new ZipFile(arguments[0])) {
            for (String name:List.of("boss/MeatBoss","butcherskills/ButcherBloodBrand")) {
                String key=PREFIX+name+".class";byte[] original=base.getInputStream(base.getEntry(key)).readAllBytes();ClassNode before=read(original);ClassNode after=read(original);
                int changed=0;
                for (MethodNode method:after.methods) {
                    for (AbstractInsnNode instruction:method.instructions.toArray()) {
                        if (name.equals("boss/MeatBoss") && List.of("spawnClone","tryStart","summonWave").contains(method.name) && instruction instanceof MethodInsnNode call && call.getOpcode()==Opcodes.INVOKEVIRTUAL && call.owner.equals("net/minecraft/server/level/ServerLevel") && call.name.equals("addFreshEntity") && call.desc.equals("(Lnet/minecraft/world/entity/Entity;)Z")) {
                            call.setOpcode(Opcodes.INVOKESTATIC);call.owner=HELPER;call.name="addWithFx";call.desc="(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)Z";changed++;
                        }
                        if (name.equals("butcherskills/ButcherBloodBrand") && method.name.equals("tick") && instruction instanceof TypeInsnNode cast && cast.getOpcode()==Opcodes.CHECKCAST && cast.desc.equals("net/minecraft/server/level/ServerLevel")) {
                            AbstractInsnNode next=instruction.getNext();
                            while(next instanceof LabelNode || next instanceof LineNumberNode || next instanceof FrameNode) next=next.getNext();
                            require(next instanceof VarInsnNode store && store.getOpcode()==Opcodes.ASTORE,"Wrong actual level local");
                            int local=((VarInsnNode)next).var;InsnList callback=new InsnList();callback.add(new VarInsnNode(Opcodes.ALOAD,local));callback.add(new VarInsnNode(Opcodes.ALOAD,local));callback.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"net/minecraft/server/level/ServerLevel","getGameTime","()J",false));callback.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HELPER,"tickSummons","(Lnet/minecraft/server/level/ServerLevel;J)V",false));method.instructions.insert(next,callback);changed++;
                        }
                    }
                }
                require(changed==(name.equals("boss/MeatBoss")?3:1),"Unexpected actual instruction sites");
                for(MethodNode method:after.methods) {
                    new Analyzer<>(new BasicVerifier()).analyze(after.name,method);verified++;
                    MethodNode prior=before.methods.stream().filter(value->value.name.equals(method.name) && value.desc.equals(method.desc)).findFirst().orElseThrow();
                    require(body(prior,false).equals(body(method,true)),"Unrelated instructions changed: "+name+"."+method.name);preserved++;
                }
                org.objectweb.asm.ClassWriter writer=new org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);after.accept(writer);byte[] encoded=writer.toByteArray();ClassNode encodedNode=read(encoded);
                for(MethodNode method:encodedNode.methods)new Analyzer<>(new BasicVerifier()).analyze(encodedNode.name,method);
                Path target=output.resolve(key);Files.createDirectories(target.getParent());Files.write(target,encoded);
            }
            for(String suffix:List.of("","$Pulse","$Pulses")) {
                String key=HELPER+suffix+".class";byte[] bytes=Files.readAllBytes(linked.resolve(key));ClassNode node=read(bytes);
                require(!new String(bytes,java.nio.charset.StandardCharsets.ISO_8859_1).contains("net/minecraft/client/"),"Client dependency introduced");
                for(MethodNode method:node.methods){new Analyzer<>(new BasicVerifier()).analyze(node.name,method);verified++;}
                Path target=output.resolve(key);Files.createDirectories(target.getParent());Files.write(target,bytes);
            }
        }
        require(redirects==3 && ticks==1,"Incomplete original ingress coverage");
        System.out.println("failures=0;methodsVerified="+verified+";existingMethodsPreserved="+preserved+";exactOriginalSpawnRedirects="+redirects+";actualLevelTickCallback="+ticks+";allFourSkillInstructionsPreserved=true;unrelatedRecompiledMethodsNeverPackaged=true");
    }
}
