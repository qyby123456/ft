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

public final class ButcherCadenceBoundaryProof {
    private static final String PREFIX="net/farmingtales/ftboss/";
    private static final String COMBAT=PREFIX+"boss/ai/ButcherCombat";
    private static final String RULES=PREFIX+"boss/ai/ButcherCombatRules";
    private static final String PHASE_RULES=PREFIX+"boss/ai/ButcherPhaseRules";
    private static final String ENTITY=PREFIX+"entity/MeatMountainEntity";

    private static void require(boolean condition,String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode node=new ClassNode();
        new ClassReader(bytes).accept(node,0);
        return node;
    }

    private static MethodNode update(ClassNode node) {
        return node.methods.stream().filter(method->method.name.equals("update") && method.desc.equals("()V")).findFirst().orElseThrow();
    }

    private static List<MethodInsnNode> intervals(MethodNode method,String owner,String descriptor) {
        var calls=new java.util.ArrayList<MethodInsnNode>();
        for (AbstractInsnNode instruction:method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(owner) && call.name.equals("interval") && call.desc.equals(descriptor)) calls.add(call);
        }
        return calls;
    }

    private static String instructions(MethodNode method) {
        var copy=new MethodNode(method.access,method.name,method.desc,method.signature,method.exceptions.toArray(String[]::new));
        method.accept(copy);
        for (var instruction:copy.instructions.toArray()) {
            if (instruction instanceof FrameNode || instruction instanceof LineNumberNode) copy.instructions.remove(instruction);
        }
        var text=new Textifier();
        copy.instructions.accept(new TraceMethodVisitor(text));
        return text.getText().toString();
    }

    public static void main(String[] args) throws Exception {
        byte[] originalBytes;
        ClassNode forge;
        try (ZipFile nativeJar=new ZipFile(args[0]);ZipFile originalJar=new ZipFile(args[1])) {
            originalBytes=nativeJar.getInputStream(nativeJar.getEntry(COMBAT+".class")).readAllBytes();
            forge=read(originalJar.getInputStream(originalJar.getEntry(COMBAT+".class")).readAllBytes());
            String rulesName=PHASE_RULES+".class";
            require(java.util.Arrays.equals(nativeJar.getInputStream(nativeJar.getEntry(rulesName)).readAllBytes(),originalJar.getInputStream(originalJar.getEntry(rulesName)).readAllBytes()),"Frozen original phase-rule class must remain byte identical");
        }
        require(intervals(update(forge),PHASE_RULES,"(I)I").size()==1,"Frozen Forge must actually call phase interval");
        ClassNode api=read(Files.readAllBytes(Path.of(args[2]).resolve(COMBAT+".class")));
        require(intervals(update(api),PHASE_RULES,"(I)I").size()==1 && intervals(update(api),RULES,"(Z)I").isEmpty(),"API-linked source must match intended single scheduler change");
        ClassNode before=read(originalBytes);
        ClassNode patched=read(originalBytes);
        MethodNode method=update(patched);
        var legacy=intervals(method,RULES,"(Z)I");
        require(legacy.size()==1,"Exactly one old scheduler call expected");
        MethodInsnNode call=legacy.getFirst();
        require(call.getPrevious() instanceof VarInsnNode && call.getPrevious().getOpcode()==Opcodes.ILOAD,"Old scheduler boolean load must be adjacent");
        int booleanSlot=((VarInsnNode)call.getPrevious()).var;
        var phaseRead=new InsnList();
        phaseRead.add(new VarInsnNode(Opcodes.ALOAD,0));
        phaseRead.add(new FieldInsnNode(Opcodes.GETFIELD,COMBAT,"owner","L"+ENTITY+";"));
        phaseRead.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,ENTITY,"getButcherPhase","()I",false));
        method.instructions.insertBefore(call.getPrevious(),phaseRead);
        method.instructions.remove(call.getPrevious());
        method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,PHASE_RULES,"interval","(I)I",false));
        var writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);
        patched.accept(writer);
        byte[] result=writer.toByteArray();
        ClassNode after=read(result);
        require(before.methods.size()==after.methods.size(),"Method count changed");
        require(before.fields.stream().map(field->field.name+field.desc+field.access+field.value).toList().equals(after.fields.stream().map(field->field.name+field.desc+field.access+field.value).toList()),"Field structure changed");
        int preserved=0;
        int verified=0;
        for (MethodNode actual:after.methods) {
            new Analyzer<>(new BasicVerifier()).analyze(after.name,actual);
            verified++;
            MethodNode previous=before.methods.stream().filter(prior->prior.name.equals(actual.name) && prior.desc.equals(actual.desc)).findFirst().orElseThrow();
            if (!actual.name.equals("update")) {
                require(instructions(previous).equals(instructions(actual)),"Unrelated method changed: "+actual.name);
                preserved++;
            }
        }
        MethodNode normalized=update(read(result));
        var phaseCall=intervals(normalized,PHASE_RULES,"(I)I");
        require(phaseCall.size()==1,"Single phase scheduler required");
        var replacement=phaseCall.getFirst();
        AbstractInsnNode getter=replacement.getPrevious();
        AbstractInsnNode field=getter.getPrevious();
        AbstractInsnNode self=field.getPrevious();
        require(getter instanceof MethodInsnNode && ((MethodInsnNode)getter).owner.equals(ENTITY) && ((MethodInsnNode)getter).name.equals("getButcherPhase"),"Phase getter drift");
        require(field instanceof FieldInsnNode && ((FieldInsnNode)field).name.equals("owner") && self instanceof VarInsnNode && ((VarInsnNode)self).var==0,"Owner read drift");
        normalized.instructions.remove(getter);
        normalized.instructions.remove(field);
        normalized.instructions.remove(self);
        normalized.instructions.insertBefore(replacement,new VarInsnNode(Opcodes.ILOAD,booleanSlot));
        normalized.instructions.set(replacement,new MethodInsnNode(Opcodes.INVOKESTATIC,RULES,"interval","(Z)I",false));
        require(instructions(update(before)).equals(instructions(normalized)),"Instructions outside the exact scheduler replacement changed");
        Path output=Path.of(args[3]).resolve(COMBAT+".class");
        Files.createDirectories(output.getParent());
        Files.write(output,result);
        System.out.println("failures=0;methodsBasicVerified="+verified+";unrelatedMethodsPreserved="+preserved+";singleSchedulerReplacementOnly=true;normalizedUpdateInstructionsIdentical=true;originalPhaseRulesByteIdentical=true");
    }
}
