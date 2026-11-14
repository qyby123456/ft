import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/** 旧装配回退的双方法定向移植与逐项保真校验。 */
public final class LegacyAccessoryPatch {
    private static final String HUB = "net/farmingtales/ftboss/client/StatueHubScreen.class";
    private static final List<String> METHODS = List.of("openSwordCavity", "openBuildAdjustment");
    private static ClassNode read(byte[] bytes) {
        var result = new ClassNode();
        new ClassReader(bytes).accept(result, 0);
        return result;
    }
    private static String text(MethodNode method) {
        var printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        return printer.getText().toString();
    }
    public static void main(String[] arguments) throws Exception {
        var donor = read(Files.readAllBytes(Path.of(arguments[1]).resolve(HUB)));
        try (var before = new JarFile(arguments[0]); var output = new JarOutputStream(Files.newOutputStream(Path.of(arguments[2])))) {
            for (var entry : before.stream().toList()) {
                byte[] content = before.getInputStream(entry).readAllBytes();
                if (entry.getName().equals(HUB)) {
                    var target = read(content);
                    for (String name : METHODS) {
                        var replacement = donor.methods.stream().filter(method -> method.name.equals(name)).findFirst().orElseThrow();
                        int count = 0;
                        for (int index = 0; index < target.methods.size(); index++) {
                            var method = target.methods.get(index);
                            if (method.name.equals(name) && method.desc.equals(replacement.desc)) {
                                target.methods.set(index, replacement);
                                count++;
                            }
                        }
                        if (count != 1) throw new AssertionError("Expected one target: " + name);
                    }
                    var writer = new ClassWriter(0);
                    target.accept(writer);
                    content = writer.toByteArray();
                }
                output.putNextEntry(new JarEntry(entry.getName()));
                output.write(content);
                output.closeEntry();
            }
        }
        int unchanged = 0;
        int changedMethods = 0;
        try (var before = new JarFile(arguments[0]); var after = new JarFile(arguments[2])) {
            if (before.size() != after.size()) throw new AssertionError("Entry count changed");
            for (var entry : before.stream().toList()) {
                byte[] original = before.getInputStream(entry).readAllBytes();
                byte[] current = after.getInputStream(after.getJarEntry(entry.getName())).readAllBytes();
                if (Arrays.equals(original, current)) { unchanged++; continue; }
                if (!entry.getName().equals(HUB)) throw new AssertionError("Unrelated entry: " + entry.getName());
                var oldType = read(original);
                var newType = read(current);
                if (oldType.methods.size() != newType.methods.size() || oldType.fields.size() != newType.fields.size()) throw new AssertionError("Class shape changed");
                for (var method : oldType.methods) {
                    var other = newType.methods.stream().filter(candidate -> candidate.name.equals(method.name) && candidate.desc.equals(method.desc)).findFirst().orElseThrow();
                    if (!text(method).equals(text(other))) {
                        if (!METHODS.contains(method.name)) throw new AssertionError("Unrelated method: " + method.name);
                        changedMethods++;
                    }
                }
            }
        }
        if (changedMethods != 2) throw new AssertionError("Expected two changed methods: " + changedMethods);
        System.out.println("PASS two routing methods; unchanged entries=" + unchanged);
    }
}
