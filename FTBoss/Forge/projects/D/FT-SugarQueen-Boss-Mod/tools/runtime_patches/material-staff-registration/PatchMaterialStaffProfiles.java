import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;

public final class PatchMaterialStaffProfiles implements Opcodes {
    private static final String ENTRY = "net/farmingtales/ftboss/util/foundations/FourWeaponFoundationProfiles.class";
    private static final String STAFF = "net/farmingtales/ftboss/util/foundations/MaterialStaffProfiles";

    private static ClassNode read(JarFile archive) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(archive.getInputStream(archive.getJarEntry(ENTRY)).readAllBytes()).accept(type, 0);
        return type;
    }

    private static MethodNode examples(ClassNode type) {
        return type.methods.stream().filter(method -> method.name.equals("examples") && method.desc.equals("()Ljava/util/Map;"))
                .findFirst().orElseThrow();
    }

    private static String text(ClassNode type) {
        StringWriter output = new StringWriter();
        type.accept(new TraceClassVisitor(new PrintWriter(output)));
        return output.toString();
    }

    public static void main(String[] arguments) throws Exception {
        try (JarFile baseline = new JarFile(arguments[0])) {
            ClassNode type = read(baseline);
            MethodNode method = examples(type);
            if (arguments.length == 3 && arguments[1].equals("--verify")) {
                try (JarFile candidate = new JarFile(arguments[2])) {
                    ClassNode patched = read(candidate);
                    MethodNode changed = examples(patched);
                    int removed = 0;
                    for (AbstractInsnNode instruction : changed.instructions.toArray()) {
                        if (instruction instanceof MethodInsnNode call && call.owner.equals(STAFF)) {
                            if (!call.name.equals("addTo") || call.getPrevious().getOpcode() != DUP) throw new AssertionError("Invalid profile hook");
                            changed.instructions.remove(call.getPrevious());
                            changed.instructions.remove(call);
                            removed++;
                        }
                    }
                    if (removed != 1 || !text(type).equals(text(patched))) throw new AssertionError("Unrelated profile bytecode changed");
                    System.out.println("PASS: exactly one staff catalogue hook; all original fields and methods preserved");
                    return;
                }
            }
            int inserted = 0;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals(STAFF)) throw new AssertionError("Staff profiles already installed");
                if (instruction instanceof MethodInsnNode call && call.owner.equals("java/util/Collections") && call.name.equals("unmodifiableMap")) {
                    InsnList addition = new InsnList();
                    addition.add(new InsnNode(DUP));
                    addition.add(new MethodInsnNode(INVOKESTATIC, STAFF, "addTo", "(Ljava/util/Map;)V", false));
                    method.instructions.insertBefore(call, addition);
                    inserted++;
                }
            }
            if (inserted != 1) throw new AssertionError("Expected one catalogue finalization");
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            type.accept(writer);
            Path target = Path.of(arguments[1]).resolve(ENTRY);
            Files.createDirectories(target.getParent());
            Files.write(target, writer.toByteArray());
        }
    }
}
