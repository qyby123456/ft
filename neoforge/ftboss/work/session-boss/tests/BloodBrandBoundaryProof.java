import java.nio.file.Files;
import java.nio.file.Path;
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

public final class BloodBrandBoundaryProof {
    private static final String PREFIX="net/farmingtales/ftboss/";
    private static final String COMBAT=PREFIX+"boss/ai/ButcherCombat";
    private static final String ENTITY=PREFIX+"entity/MeatMountainEntity";
    private static final String ACTION=PREFIX+"boss/ai/BossActionState";
    private static final String SKILLS=PREFIX+"butcherskills/";
    private static int verified;

    private static void require(boolean condition,String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static ClassNode read(byte[] bytes,int flags) {
        var node=new ClassNode();
        new ClassReader(bytes).accept(node,flags);
        return node;
    }

    private static MethodNode update(ClassNode node) {
        return node.methods.stream().filter(method->method.name.equals("update") && method.desc.equals("()V")).findFirst().orElseThrow();
    }

    private static long calls(MethodNode method,String owner,String name) {
        long count=0;
        for (var instruction:method.instructions) if (instruction instanceof MethodInsnNode call && call.owner.equals(owner) && call.name.equals(name)) count++;
        return count;
    }

    private static String instructions(MethodNode method) {
        var copy=new MethodNode(method.access,method.name,method.desc,method.signature,method.exceptions.toArray(String[]::new));
        method.accept(copy);
        for (var instruction:copy.instructions.toArray()) if (instruction instanceof FrameNode || instruction instanceof LineNumberNode) copy.instructions.remove(instruction);
        var text=new Textifier();
        copy.instructions.accept(new TraceMethodVisitor(text));
        return text.getText().toString();
    }

    private static void verify(ClassNode node) throws Exception {
        for (var method:node.methods) {
            new Analyzer<>(new BasicVerifier()).analyze(node.name,method);
            verified++;
        }
    }

    private static void write(Path output,String name,byte[] payload) throws Exception {
        Path target=output.resolve(name+".class");
        Files.createDirectories(target.getParent());
        Files.write(target,payload);
        verify(read(payload,0));
    }

    public static void main(String[] args) throws Exception {
        Path compiled=Path.of(args[2]);
        Path output=Path.of(args[3]);
        byte[] originalBytes;
        try (ZipFile base=new ZipFile(args[0]);ZipFile original=new ZipFile(args[1])) {
            originalBytes=base.getInputStream(base.getEntry(COMBAT+".class")).readAllBytes();
            var forge=read(original.getInputStream(original.getEntry(COMBAT+".class")).readAllBytes(),0);
            require(calls(update(forge),SKILLS+"ButcherSkills","tick")==1,"Frozen Forge has the real pre-melee skill callsite");
            for (String name:List.of("ButcherSkillRules","ButcherSkillRules$Skill","ButcherSkillRules$ParticleBudget","ButcherFxPlan","ButcherFxPlan$Point")) {
                String fullName=SKILLS+name;
                byte[] payload=original.getInputStream(original.getEntry(fullName+".class")).readAllBytes();
                write(output,fullName,payload);
            }
        }
        var before=read(originalBytes,ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        var patched=read(originalBytes,0);
        var api=read(Files.readAllBytes(compiled.resolve(COMBAT+".class")),0);
        require(calls(update(api),SKILLS+"ButcherBloodBrand","tick")==1,"API source must wire precisely the one selected skill");
        var continuation=new LabelNode();
        var gate=new InsnList();
        gate.add(new VarInsnNode(Opcodes.ALOAD,0));
        gate.add(new FieldInsnNode(Opcodes.GETFIELD,COMBAT,"owner","L"+ENTITY+";"));
        gate.add(new VarInsnNode(Opcodes.ALOAD,0));
        gate.add(new FieldInsnNode(Opcodes.GETFIELD,COMBAT,"action","L"+ACTION+";"));
        gate.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SKILLS+"ButcherBloodBrand","tick","(L"+ENTITY+";L"+ACTION+";)Z",false));
        gate.add(new JumpInsnNode(Opcodes.IFEQ,continuation));
        gate.add(new InsnNode(Opcodes.RETURN));
        gate.add(continuation);
        gate.add(new FrameNode(Opcodes.F_SAME,0,null,0,null));
        var method=update(patched);
        method.instructions.insertBefore(method.instructions.getFirst(),gate);
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);
        patched.accept(writer);
        byte[] result=writer.toByteArray();
        var after=read(result,ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
        require(after.methods.size()==before.methods.size(),"Method count drift");
        int preserved=0;
        for (var actual:after.methods) {
            var previous=before.methods.stream().filter(prior->prior.name.equals(actual.name) && prior.desc.equals(actual.desc)).findFirst().orElseThrow();
            if (!actual.name.equals("update")) { require(instructions(actual).equals(instructions(previous)),"Unrelated method changed "+actual.name);preserved++; }
        }
        var normalized=update(read(result,ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES));
        var prefix=normalized.instructions.toArray();
        require(prefix[4] instanceof MethodInsnNode && ((MethodInsnNode)prefix[4]).owner.equals(SKILLS+"ButcherBloodBrand"),"Selected skill gate must run first");
        require(prefix[5].getOpcode()==Opcodes.IFEQ && prefix[6].getOpcode()==Opcodes.RETURN && prefix[7] instanceof LabelNode,"Pre-melee consumed-action gate drift");
        for (int index=0;index<8;index++) normalized.instructions.remove(prefix[index]);
        require(instructions(normalized).equals(instructions(update(before))),"Existing update instructions changed outside added gate");
        require(calls(update(after),PREFIX+"boss/ai/ButcherPhaseRules","interval")==1,"Stage08 phase scheduler lost");
        write(output,COMBAT,result);
        for (String name:List.of("ButcherBloodBrand","ButcherBloodBrand$State","ButcherSkillFx")) {
            String fullName=SKILLS+name;
            byte[] payload=Files.readAllBytes(compiled.resolve(fullName+".class"));
            require(!new String(payload,java.nio.charset.StandardCharsets.ISO_8859_1).contains("net/minecraft/client/"),"Server implementation references client-only API");
            if (name.equals("ButcherBloodBrand")) {
                var helper=read(payload,0);
                for (var helperMethod:helper.methods) for (var instruction:helperMethod.instructions) {
                    if (instruction instanceof FieldInsnNode field && field.getOpcode()==Opcodes.GETSTATIC && field.owner.equals(SKILLS+"ButcherSkillRules$Skill")) require(field.name.equals("BLOOD_BRAND"),"Another active skill was enabled");
                }
            }
            write(output,fullName,payload);
        }
        System.out.println("failures=0;methodsBasicVerified="+verified+";unrelatedCombatMethodsPreserved="+preserved+";originalPureClassesByteIdentical=5;onlyBloodBrandEnabled=true;normalizedUpdateInstructionsIdentical=true;stage08PhaseSchedulerPreserved=true");
    }
}
