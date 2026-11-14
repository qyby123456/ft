import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class ManorSpellTimingPatch {
    public static void main(String[] arguments) throws Exception {
        String entry = "net/farmingtales/ftboss/integ/spells/ManorSignatureSpell.class";
        Set<String> selected = Set.of("onServerPreCast", "onServerCastTick", "onCast");
        try (JarFile baseline = new JarFile(arguments[0]); JarFile compiled = new JarFile(arguments[1])) {
            ClassNode original = read(baseline, entry);
            ClassNode donor = read(compiled, entry);
            for (String name : selected) {
                MethodNode replacement = donor.methods.stream().filter(method -> method.name.equals(name)).findFirst().orElseThrow();
                int index = -1;
                for (int position = 0; position < original.methods.size(); position++) {
                    MethodNode current = original.methods.get(position);
                    if (current.name.equals(name) && current.desc.equals(replacement.desc)) index = position;
                }
                if (index < 0) throw new IllegalStateException("Missing method: " + name);
                original.methods.set(index, replacement);
            }
            ClassWriter writer = new ClassWriter(0);
            original.accept(writer);
            Path output = Path.of(arguments[2]).resolve(entry);
            Files.createDirectories(output.getParent());
            Files.write(output, writer.toByteArray());
        }
        System.out.println("PASS: only three Manor spell lifecycle methods transplanted");
    }

    private static ClassNode read(JarFile jar, String entry) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(entry)).readAllBytes()).accept(node, 0);
        return node;
    }
}
