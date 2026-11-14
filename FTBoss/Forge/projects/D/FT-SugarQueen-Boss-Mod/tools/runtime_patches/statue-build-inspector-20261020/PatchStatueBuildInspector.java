import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class PatchStatueBuildInspector {
    static final String HUB = "net/farmingtales/ftboss/client/StatueHubScreen";
    static final List<String> ADDED = List.of("addBuildInspectorEntry", "canOpenBuildInspector", "openBuildAdjustment");

    static ClassNode read(ZipFile archive, String name) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(archive.getInputStream(archive.getEntry(name + ".class"))).accept(type, 0);
        return type;
    }

    public static void main(String[] arguments) throws Exception {
        try (ZipFile before = new ZipFile(arguments[0]); ZipFile donor = new ZipFile(arguments[1])) {
            ClassNode original = read(before, HUB);
            ClassNode compiled = read(donor, HUB);
            for (String name : ADDED) {
                if (original.methods.stream().anyMatch(method -> method.name.equals(name))) throw new IllegalStateException("Already installed: " + name);
                MethodNode method = compiled.methods.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
                original.methods.add(method);
            }
            MethodNode init = original.methods.stream().filter(method -> method.name.equals("m_7856_") && method.desc.equals("()V")).findFirst().orElseThrow();
            int returns = 0;
            for (var instruction : init.instructions.toArray()) {
                if (instruction.getOpcode() != Opcodes.RETURN) continue;
                InsnList injection = new InsnList();
                injection.add(new VarInsnNode(Opcodes.ALOAD, 0));
                injection.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, HUB, "addBuildInspectorEntry", "()V", false));
                init.instructions.insertBefore(instruction, injection);
                returns++;
            }
            if (returns != 1) throw new IllegalStateException("Unexpected init return count: " + returns);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            original.accept(writer);
            Path output = Path.of(arguments[2]).resolve(HUB + ".class");
            Files.createDirectories(output.getParent());
            Files.write(output, writer.toByteArray());
            System.out.println("PASS: preserved existing hub, injected one init call and three private helpers");
        }
    }
}
