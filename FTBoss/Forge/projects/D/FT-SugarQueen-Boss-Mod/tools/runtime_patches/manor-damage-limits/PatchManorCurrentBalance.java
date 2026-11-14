import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public final class PatchManorCurrentBalance implements Opcodes {
    public static void main(String[] arguments) throws Exception {
        Path payload = Path.of(arguments[1]);
        Path fixture = Path.of(arguments[2]);
        try (JarFile baseline = new JarFile(arguments[0])) {
            ClassNode boss = read(baseline, "net/farmingtales/ftboss/entity/EasterBossEntity");
            int changed = 0;
            for (var method : boss.methods) {
                if (!method.name.equals("createAttributes")) continue;
                for (var instruction : method.instructions) {
                    if (instruction instanceof LdcInsnNode constant && constant.cst.equals(3000.0)
                            && constant.getPrevious() instanceof FieldInsnNode attribute
                            && attribute.name.equals("f_22276_")) {
                        constant.cst = 8000.0;
                        changed++;
                    }
                }
            }
            if (changed != 1) throw new IllegalStateException("Expected exactly one current 3000 health attribute: " + changed);
            write(payload, boss);
            ClassNode multiplayer = read(baseline, "net/farmingtales/ftboss/boss/ai/ManorMultiplayerBalance");
            multiplayer.methods.removeIf(method -> !method.name.equals("applyHealthAndRestoreDamage"));
            if (multiplayer.methods.size() != 1) throw new IllegalStateException("Missing multiplayer health method");
            multiplayer.methods.get(0).access = ACC_PUBLIC | ACC_STATIC;
            write(fixture, multiplayer);
            ClassNode raid = read(baseline, "net/farmingtales/ftboss/boss/ai/ManorRaidCombat");
            raid.methods.removeIf(method -> !Set.of("mainFight", "scaleMaximumHealth").contains(method.name));
            write(fixture, raid);
        }
        System.out.println("PASS manor spawn health 8000; existing multiplayer and difficulty bytecode retained");
    }

    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(name + ".class")).readAllBytes()).accept(node, 0);
        return node;
    }

    private static void write(Path root, ClassNode node) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Path output = root.resolve(node.name + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
    }
}
