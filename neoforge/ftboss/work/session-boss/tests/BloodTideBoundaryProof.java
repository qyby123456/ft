import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class BloodTideBoundaryProof {
    private static final String PREFIX="net/farmingtales/ftboss/butcherskills/";
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

    public static void main(String[] arguments) throws Exception {
        Path compiled=Path.of(arguments[2]);
        try (ZipFile base=new ZipFile(arguments[0]);ZipFile original=new ZipFile(arguments[1])) {
            for (String name:List.of("ButcherBloodBrand","ButcherBloodBrand$State")) {
                String key=PREFIX+name+".class";
                byte[] bytes=Files.readAllBytes(compiled.resolve(key));
                var changed=read(bytes);
                var before=read(base.getInputStream(base.getEntry(key)).readAllBytes());
                for (var method:changed.methods) {
                    new Analyzer<>(new BasicVerifier()).analyze(changed.name,method);
                    verified++;
                    boolean allowed=name.equals("ButcherBloodBrand") && List.of("tick","release","<clinit>").contains(method.name) || name.endsWith("$State") && method.name.equals("<init>");
                    if (!allowed) {
                        var prior=before.methods.stream().filter(value->value.name.equals(method.name) && value.desc.equals(method.desc)).findFirst().orElseThrow();
                        require(body(method).equals(body(prior)),"Unrelated method changed: "+method.name);
                        preserved++;
                    }
                    for (var instruction:method.instructions) {
                        if (instruction instanceof FieldInsnNode field && field.getOpcode()==Opcodes.GETSTATIC && field.owner.equals(PREFIX+"ButcherSkillRules$Skill")) require(List.of("BLOOD_BRAND","BLOOD_TIDE").contains(field.name),"Unrequested skill enabled");
                    }
                }
                require(!new String(bytes,java.nio.charset.StandardCharsets.ISO_8859_1).contains("net/minecraft/client/"),"Client-only API reference");
            }
            for (String name:List.of("ButcherSkillRules","ButcherSkillRules$Skill","ButcherSkillRules$ParticleBudget","ButcherFxPlan","ButcherFxPlan$Point")) {
                String key=PREFIX+name+".class";
                require(java.util.Arrays.equals(base.getInputStream(base.getEntry(key)).readAllBytes(),original.getInputStream(original.getEntry(key)).readAllBytes()),"Original pure rules changed");
            }
        }
        require(preserved==3,"Expected constructor, eligibility and facing-lock preservation");
        System.out.println("failures=0;methodsBasicVerified="+verified+";unrelatedMethodsPreserved="+preserved+";originalPureClassesByteIdentical=5;onlyBloodBrandAndBloodTideEnabled=true");
    }
}
