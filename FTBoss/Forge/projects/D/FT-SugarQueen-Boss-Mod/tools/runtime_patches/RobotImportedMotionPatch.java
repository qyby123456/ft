import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class RobotImportedMotionPatch {
    private static final String MODEL = "net/farmingtales/ftboss/client/defectzero/DefectSkillGeoModel";
    private static final String DESCRIPTOR = "(Lnet/farmingtales/ftboss/entity/DefectCoreEntity;JLsoftware/bernie/geckolib/core/animation/AnimationState;)V";

    private static ClassNode read(byte[] bytes) {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, 0);
        return type;
    }

    private static byte[] methodBytes(MethodNode method) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(61, 1, "Comparison", null, "java/lang/Object", null);
        method.accept(writer);
        writer.visitEnd();
        return writer.toByteArray();
    }

    public static void main(String[] arguments) throws Exception {
        try (ZipFile baseline = new ZipFile(arguments[0])) {
            ClassNode original = read(baseline.getInputStream(baseline.getEntry(MODEL + ".class")).readAllBytes());
            ClassNode type = read(baseline.getInputStream(baseline.getEntry(MODEL + ".class")).readAllBytes());
            MethodNode method = type.methods.stream().filter(candidate -> candidate.name.equals("setCustomAnimations") && candidate.desc.equals(DESCRIPTOR)).findFirst().orElseThrow();
            long writes = Arrays.stream(method.instructions.toArray()).filter(instruction -> instruction instanceof MethodInsnNode call && call.owner.endsWith("/GeoBone") && call.name.startsWith("set")).count();
            if (writes != 9) throw new AssertionError("Expected nine legacy bone transform writes, got " + writes);
            MethodInsnNode parent = Arrays.stream(method.instructions.toArray()).filter(instruction -> instruction instanceof MethodInsnNode call && call.getOpcode() == Opcodes.INVOKESPECIAL && call.name.equals("setCustomAnimations")).map(instruction -> (MethodInsnNode) instruction).findFirst().orElseThrow();
            method.instructions.clear();
            method.tryCatchBlocks.clear();
            if (method.localVariables != null) method.localVariables.clear();
            method.visibleLocalVariableAnnotations = null;
            method.invisibleLocalVariableAnnotations = null;
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            method.instructions.add(new VarInsnNode(Opcodes.LLOAD, 2));
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
            method.instructions.add(new MethodInsnNode(parent.getOpcode(), parent.owner, parent.name, parent.desc, parent.itf));
            method.instructions.add(new InsnNode(Opcodes.RETURN));
            method.maxLocals = 5;
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            type.accept(writer);
            byte[] patched = writer.toByteArray();
            ClassNode verified = read(patched);
            int preserved = 0;
            for (MethodNode before : original.methods) {
                MethodNode after = verified.methods.stream().filter(candidate -> candidate.name.equals(before.name) && candidate.desc.equals(before.desc)).findFirst().orElseThrow();
                new Analyzer<>(new BasicVerifier()).analyze(type.name, after);
                if (before.name.equals("setCustomAnimations") && before.desc.equals(DESCRIPTOR)) continue;
                if (!Arrays.equals(methodBytes(before), methodBytes(after))) throw new AssertionError("Unrelated method changed: " + before.name);
                preserved++;
            }
            if (verified.fields.size() != original.fields.size() || verified.methods.size() != original.methods.size()) throw new AssertionError("Class schema changed");
            ClassNode renderer = read(baseline.getInputStream(baseline.getEntry("net/farmingtales/ftboss/client/render/DefectCoreRenderer.class")).readAllBytes());
            boolean selected = renderer.methods.stream().flatMap(candidate -> Arrays.stream(candidate.instructions.toArray())).anyMatch(instruction -> instruction instanceof TypeInsnNode creation && creation.getOpcode() == Opcodes.NEW && creation.desc.equals(MODEL));
            if (!selected) throw new AssertionError("Renderer does not select patched model");
            Path output = Path.of(arguments[1]).resolve(MODEL + ".class");
            Files.createDirectories(output.getParent());
            Files.write(output, patched);
            System.out.println("PASS active renderer model: removed 9 legacy bone writes; parent animation hook retained; " + preserved + " other methods and class schema unchanged; ASM verified.");
        }
    }
}
