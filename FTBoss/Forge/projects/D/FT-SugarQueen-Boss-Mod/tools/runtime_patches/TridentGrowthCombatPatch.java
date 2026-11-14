import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/** 三叉戟收益路由的定点合包；2026-10-25 仅替换旧投掷报价和提示，保留并发修复。 */
public final class TridentGrowthCombatPatch {
    private static final String OWNER = "net/farmingtales/ftboss/item/RangedHybridCombat";

    private static ClassNode read(JarFile jar) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(OWNER + ".class")).readAllBytes()).accept(type, 0);
        return type;
    }

    public static void main(String[] arguments) throws Exception {
        try (JarFile baseline = new JarFile(arguments[0]); JarFile donor = new JarFile(arguments[1])) {
            ClassNode target = read(baseline);
            ClassNode source = read(donor);
            for (String name : Set.of("quoteDamage", "tooltip")) {
                var replacement = source.methods.stream().filter(method -> method.name.equals(name)).findFirst().orElseThrow();
                var previous = target.methods.stream().filter(method -> method.name.equals(name)
                        && method.desc.equals(replacement.desc)).findFirst().orElseThrow();
                target.methods.set(target.methods.indexOf(previous), replacement);
            }
            ClassWriter writer = new ClassWriter(0);
            target.accept(writer);
            Path output = Path.of(arguments[2]).resolve(OWNER + ".class");
            Files.createDirectories(output.getParent());
            Files.write(output, writer.toByteArray());
        }
        System.out.println("PASS trident routing patch: methods=2");
    }
}
