import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class GongdiLifecycleBoundaryProof implements Opcodes {
    private static final String ENTITY="net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String CONTROLLER="net/farmingtales/ftboss/boss/ai/GongdiSkills";
    private static final String HELPER="net/farmingtales/ftboss/boss/ai/GongdiCastLifecycle";
    private static final String BOSS="L"+ENTITY+";";
    private static final String LEVEL="Lnet/minecraft/server/level/ServerLevel;";
    private static final String LIVING="Lnet/minecraft/world/entity/LivingEntity;";
    private static int changed;
    private static int preserved;
    private static int verified;
    private static void require(boolean value,String message) {if(!value) throw new AssertionError(message);}
    private static ClassNode read(byte[] bytes) {ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);return node;}
    private static String body(MethodNode source) {
        MethodNode copy=new MethodNode(source.access,source.name,source.desc,source.signature,source.exceptions.toArray(String[]::new));source.accept(copy);
        for(AbstractInsnNode instruction:copy.instructions.toArray()) if(instruction instanceof FrameNode || instruction instanceof LineNumberNode) copy.instructions.remove(instruction);
        Textifier text=new Textifier();copy.instructions.accept(new TraceMethodVisitor(text));return text.getText().toString();
    }
    private static void boss(InsnList prefix,boolean controller) {
        prefix.add(new VarInsnNode(ALOAD,0));if(controller) prefix.add(new FieldInsnNode(GETFIELD,CONTROLLER,"boss",BOSS));
    }
    private static void same(InsnList prefix,LabelNode label) {prefix.add(label);prefix.add(new FrameNode(F_SAME,0,null,0,null));}
    private static InsnList prefix(String owner,MethodNode method) {
        InsnList prefix=new InsnList();boolean controller=owner.equals(CONTROLLER);LabelNode original=new LabelNode();
        if(!controller && method.name.equals("beginGongdiCast")) {
            boss(prefix,false);prefix.add(new MethodInsnNode(INVOKESTATIC,HELPER,"canBegin","("+BOSS+")Z",false));prefix.add(new JumpInsnNode(IFNE,original));prefix.add(new InsnNode(RETURN));same(prefix,original);
        } else if(!controller && method.name.equals("finishGongdiCast")) {
            boss(prefix,false);prefix.add(new MethodInsnNode(INVOKESTATIC,HELPER,"canFinish","("+BOSS+")Z",false));prefix.add(new JumpInsnNode(IFNE,original));boss(prefix,false);prefix.add(new MethodInsnNode(INVOKESTATIC,HELPER,"cancel","("+BOSS+")V",false));prefix.add(new InsnNode(RETURN));same(prefix,original);
        } else if(controller && method.name.equals("reset")) {
            boss(prefix,true);prefix.add(new MethodInsnNode(INVOKESTATIC,HELPER,"cancel","("+BOSS+")V",false));
        } else if(controller && method.name.equals("tryStart")) {
            LabelNode rejected=new LabelNode();prefix.add(new VarInsnNode(ALOAD,0));prefix.add(new FieldInsnNode(GETFIELD,CONTROLLER,"windup","I"));prefix.add(new JumpInsnNode(IFGT,rejected));
            boss(prefix,true);prefix.add(new VarInsnNode(ALOAD,1));prefix.add(new VarInsnNode(ALOAD,2));prefix.add(new MethodInsnNode(INVOKESTATIC,HELPER,"canStart","("+BOSS+LEVEL+LIVING+")Z",false));prefix.add(new JumpInsnNode(IFNE,original));same(prefix,rejected);prefix.add(new InsnNode(ICONST_0));prefix.add(new InsnNode(IRETURN));same(prefix,original);
        } else if(controller && method.name.equals("tick")) {
            LabelNode sameLevel=new LabelNode();boss(prefix,true);prefix.add(new MethodInsnNode(INVOKEVIRTUAL,ENTITY,"level","()Lnet/minecraft/world/level/Level;",false));prefix.add(new VarInsnNode(ALOAD,1));prefix.add(new JumpInsnNode(IF_ACMPEQ,sameLevel));prefix.add(new InsnNode(RETURN));same(prefix,sameLevel);
            boss(prefix,true);prefix.add(new MethodInsnNode(INVOKESTATIC,HELPER,"canBegin","("+BOSS+")Z",false));prefix.add(new JumpInsnNode(IFNE,original));prefix.add(new VarInsnNode(ALOAD,0));prefix.add(new MethodInsnNode(INVOKEVIRTUAL,CONTROLLER,"reset","()V",false));prefix.add(new InsnNode(RETURN));same(prefix,original);
        }
        return prefix;
    }
    public static void main(String[] arguments) throws Exception {
        Path output=Path.of(arguments[2]);
        try(ZipFile base=new ZipFile(arguments[0]);ZipFile reference=new ZipFile(arguments[1])) {
            for(String name:List.of(ENTITY,CONTROLLER)) {
                ClassNode original=read(base.getInputStream(base.getEntry(name+".class")).readAllBytes());ClassNode current=read(base.getInputStream(base.getEntry(name+".class")).readAllBytes());
                ClassNode forge=read(reference.getInputStream(reference.getEntry(name+".class")).readAllBytes());
                for(int index=0;index<current.methods.size();index++) {
                    MethodNode method=current.methods.get(index);String before=body(original.methods.get(index));InsnList extra=prefix(name,method);
                    if(extra.size()>0) {
                        MethodNode authority=forge.methods.stream().filter(value->value.name.equals(method.name) && value.desc.equals(method.desc)).findFirst().orElseThrow();
                        long expectedCalls=java.util.Arrays.stream(authority.instructions.toArray()).filter(value->value instanceof MethodInsnNode call && call.owner.equals(HELPER)).count();require(expectedCalls>=1,"Original lifecycle call absent:"+method.name);
                        int count=extra.size();method.instructions.insert(extra);MethodNode tail=new MethodNode(method.access,method.name,method.desc,method.signature,method.exceptions.toArray(String[]::new));method.accept(tail);
                        for(int position=0;position<count;position++) tail.instructions.remove(tail.instructions.getFirst());
                        require(body(tail).equals(before),"Existing instructions changed:"+method.name);changed++;
                    } else {require(body(method).equals(before),"Unrelated method changed:"+method.name);preserved++;}
                }
                ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);current.accept(writer);byte[] bytes=writer.toByteArray();ClassNode written=read(bytes);
                for(MethodNode method:written.methods) if((method.access&(ACC_ABSTRACT|ACC_NATIVE))==0) {new Analyzer<>(new BasicVerifier()).analyze(name,method);verified++;}
                Path target=output.resolve(name+".class");Files.createDirectories(target.getParent());Files.write(target,bytes);
            }
        }
        require(changed==5,"Expected exactly five lifecycle entry ports");
        System.out.println("failures=0;changedMethods="+changed+";existingMethodInstructionsPreserved=true;unrelatedMethodsPreserved="+preserved+";methodsBasicVerified="+verified+";originalForgeHelperCallsitesConfirmed=true");
    }
}
