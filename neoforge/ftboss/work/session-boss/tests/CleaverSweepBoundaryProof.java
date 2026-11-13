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

public final class CleaverSweepBoundaryProof {
    private static final String PREFIX="net/farmingtales/ftboss/";
    private static final String SKILLS=PREFIX+"butcherskills/";
    private static int verified;
    private static int preserved;

    private static void require(boolean value,String message) {
        if (!value) throw new AssertionError(message);
    }

    private static ClassNode read(byte[] bytes) {
        var node=new ClassNode();
        new ClassReader(bytes).accept(node,0);
        return node;
    }

    private static String body(MethodNode method) {
        var copy=new MethodNode(method.access,method.name,method.desc,method.signature,method.exceptions.toArray(String[]::new));
        method.accept(copy);
        for (var instruction:copy.instructions.toArray()) if (instruction instanceof FrameNode || instruction instanceof LineNumberNode) copy.instructions.remove(instruction);
        var text=new Textifier();
        copy.instructions.accept(new TraceMethodVisitor(text));
        return text.getText().toString();
    }

    private static List<String> selectedSkills(ClassNode node) {
        var names=new ArrayList<String>();
        var method=node.methods.stream().filter(value->value.name.equals("<clinit>")).findFirst().orElseThrow();
        for (var instruction:method.instructions) if (instruction instanceof FieldInsnNode field && field.getOpcode()==Opcodes.GETSTATIC && field.owner.equals(SKILLS+"ButcherSkillRules$Skill")) names.add(field.name);
        return names;
    }

    public static void main(String[] arguments) throws Exception {
        Path compiled=Path.of(arguments[2]);
        try (ZipFile base=new ZipFile(arguments[0]);ZipFile original=new ZipFile(arguments[1])) {
            var forge=read(original.getInputStream(original.getEntry(PREFIX+"boss/ai/ButcherCombat.class")).readAllBytes());
            var update=forge.methods.stream().filter(value->value.name.equals("update") && value.desc.equals("()V")).findFirst().orElseThrow();
            int originalCalls=0;
            for (var instruction:update.instructions) if (instruction instanceof MethodInsnNode call && call.owner.equals(SKILLS+"ButcherSkills") && call.name.equals("tick")) originalCalls++;
            require(originalCalls==1,"Frozen Forge real skill callsite missing");
            for (String name:List.of("ButcherBloodBrand","ButcherBloodBrand$State")) {
                String key=SKILLS+name+".class";
                byte[] bytes=Files.readAllBytes(compiled.resolve(key));
                var changed=read(bytes);
                var before=read(base.getInputStream(base.getEntry(key)).readAllBytes());
                require(before.fields.stream().map(value->value.name+value.desc+value.access).toList().equals(changed.fields.stream().map(value->value.name+value.desc+value.access).toList()) || name.equals("ButcherBloodBrand"),"Unrelated state fields changed");
                if (name.equals("ButcherBloodBrand")) {
                    require(selectedSkills(before).equals(List.of("BLOOD_BRAND","BLOOD_TIDE")),"Baseline selected skill set drifted");
                    require(selectedSkills(changed).equals(List.of("BLOOD_BRAND","BLOOD_TIDE","CLEAVER_SWEEP")),"Only Cleaver Sweep may be enabled");
                }
                require(before.methods.size()==changed.methods.size(),"New helper methods introduced");
                for (var method:changed.methods) {
                    new Analyzer<>(new BasicVerifier()).analyze(changed.name,method);
                    verified++;
                    boolean allowed=name.equals("ButcherBloodBrand") && List.of("release","<clinit>").contains(method.name);
                    if (!allowed) {
                        var prior=before.methods.stream().filter(value->value.name.equals(method.name) && value.desc.equals(method.desc)).findFirst().orElseThrow();
                        require(body(method).equals(body(prior)),"Unrelated method changed: "+method.name);
                        preserved++;
                    }
                    for (var instruction:method.instructions) if (instruction instanceof FieldInsnNode field && field.getOpcode()==Opcodes.GETSTATIC && field.owner.equals(SKILLS+"ButcherSkillRules$Skill")) require(!field.name.equals("BONE_CRUSH"),"Unrequested Bone Crush enabled");
                }
                require(!new String(bytes,java.nio.charset.StandardCharsets.ISO_8859_1).contains("net/minecraft/client/"),"Client-only dependency introduced");
            }
            for (String name:List.of("ButcherSkillRules","ButcherSkillRules$Skill","ButcherSkillRules$ParticleBudget","ButcherFxPlan","ButcherFxPlan$Point")) {
                String key=SKILLS+name+".class";
                require(java.util.Arrays.equals(base.getInputStream(base.getEntry(key)).readAllBytes(),original.getInputStream(original.getEntry(key)).readAllBytes()),"Frozen rule class drifted");
            }
        }
        require(verified==8 && preserved==6,"Unexpected related method count");
        System.out.println("failures=0;methodsBasicVerified=8;unrelatedMethodsPreserved=6;originalPureClassesByteIdentical=5;onlyCleaverSweepAdded=true;originalSchedulerInstructionsPreserved=true");
    }
}
