import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class WeaponTypeVolleyPatch {
    private static final String ENTRY = "net/farmingtales/ftboss/skill/WeaponTypeSkills.class";

    private static ClassNode read(JarFile jar) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(ENTRY)).readAllBytes()).accept(type, 0);
        return type;
    }

    private static MethodNode shoot(ClassNode type) {
        return type.methods.stream().filter(method -> method.name.equals("shoot")).findFirst().orElseThrow();
    }

    public static void main(String[] arguments) throws Exception {
        try (JarFile baseline = new JarFile(arguments[0]); JarFile compiled = new JarFile(arguments[1])) {
            ClassNode target = read(baseline);
            MethodNode replacement = shoot(read(compiled));
            MethodNode previous = shoot(target);
            if (!replacement.desc.equals(previous.desc) || replacement.access != previous.access) {
                throw new IllegalStateException("Volley method contract changed");
            }
            target.methods.set(target.methods.indexOf(previous), replacement);
            ClassWriter writer = new ClassWriter(0);
            target.accept(writer);
            Path output = Path.of(arguments[2]).resolve(ENTRY);
            Files.createDirectories(output.getParent());
            Files.write(output, writer.toByteArray());
            System.out.println("PASS replaced only WeaponTypeSkills.shoot with the compiled production method");
        }
    }
}
