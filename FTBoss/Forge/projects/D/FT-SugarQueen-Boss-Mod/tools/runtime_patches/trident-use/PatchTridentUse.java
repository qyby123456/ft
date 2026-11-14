import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/** 三叉戟可用判定定点构建；2026-10-24 仅替换缺件路由，其余认证和耐久方法逐项保持。 */
public final class PatchTridentUse {
    private static final String ENTRY = "net/farmingtales/ftboss/item/SwordCavityBuild.class";

    private static ClassNode read(JarFile archive) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(archive.getInputStream(archive.getJarEntry(ENTRY)).readAllBytes()).accept(node, 0);
        return node;
    }

    private static String trace(MethodNode method) {
        Textifier text = new Textifier();
        method.accept(new TraceMethodVisitor(text));
        StringWriter output = new StringWriter();
        text.print(new PrintWriter(output));
        return output.toString();
    }

    public static void main(String[] args) throws Exception {
        try (JarFile baseline = new JarFile(args[0]); JarFile donors = new JarFile(args[1])) {
            ClassNode original = read(baseline);
            ClassNode node = read(baseline);
            ClassNode donor = read(donors);
            MethodNode prior = node.methods.stream().filter(method -> method.name.equals("missingRequired")).findFirst().orElseThrow();
            MethodNode replacement = donor.methods.stream().filter(method -> method.name.equals(prior.name) && method.desc.equals(prior.desc))
                    .findFirst().orElseThrow();
            replacement.visibleAnnotations = prior.visibleAnnotations;
            replacement.invisibleAnnotations = prior.invisibleAnnotations;
            node.methods.set(node.methods.indexOf(prior), replacement);
            for (MethodNode method : original.methods) {
                MethodNode current = node.methods.stream().filter(candidate -> candidate.name.equals(method.name) && candidate.desc.equals(method.desc))
                        .findFirst().orElseThrow();
                if (!method.name.equals("missingRequired") && !trace(method).equals(trace(current))) {
                    throw new AssertionError("Unrelated method changed: " + method.name);
                }
            }
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            Path output = Path.of(args[2]).resolve(ENTRY);
            Files.createDirectories(output.getParent());
            Files.write(output, writer.toByteArray());
            System.out.println("PASS missingRequired replaced; " + (node.methods.size() - 1) + " sibling methods preserved");
        }
    }
}
