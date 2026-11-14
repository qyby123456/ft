import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class PatchFamilyCavityBuild {
    private static final String ENTRY = "net/farmingtales/ftboss/item/FoodWeaponFamilyBuild.class";
    private static final Set<String> METHODS = Set.of("build", "materialPreview");

    private static ClassNode read(JarFile archive) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(archive.getInputStream(archive.getJarEntry(ENTRY)).readAllBytes()).accept(type, 0);
        return type;
    }

    private static String text(MethodNode method) {
        var output = new java.io.StringWriter();
        var printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        printer.print(new java.io.PrintWriter(output));
        return output.toString();
    }

    public static void main(String[] arguments) throws Exception {
        try (JarFile baseline = new JarFile(arguments[0]); JarFile replacement = new JarFile(arguments[1])) {
            ClassNode original = read(baseline);
            ClassNode updated = read(replacement);
            if (arguments.length == 2) {
                Set<String> changed = new HashSet<>();
                if (original.methods.size() != updated.methods.size() || original.fields.size() != updated.fields.size()) throw new AssertionError("Class shape changed");
                for (MethodNode before : original.methods) {
                    MethodNode after = updated.methods.stream().filter(value -> value.name.equals(before.name) && value.desc.equals(before.desc)).findFirst().orElseThrow();
                    if (!text(before).equals(text(after))) changed.add(before.name);
                }
                if (!changed.equals(METHODS)) throw new AssertionError("Unexpected methods " + changed);
                System.out.println("PASS only build/materialPreview changed; all other methods preserved");
                return;
            }
            for (String name : METHODS) {
                MethodNode before = original.methods.stream().filter(value -> value.name.equals(name)).findFirst().orElseThrow();
                MethodNode after = updated.methods.stream().filter(value -> value.name.equals(name) && value.desc.equals(before.desc)).findFirst().orElseThrow();
                original.methods.set(original.methods.indexOf(before), after);
            }
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            original.accept(writer);
            Path target = Path.of(arguments[2]).resolve(ENTRY);
            Files.createDirectories(target.getParent());
            Files.write(target, writer.toByteArray());
            System.out.println("PASS material family construction and preview use cavity builds");
        }
    }
}
