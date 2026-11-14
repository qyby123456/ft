import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;

public final class PatchManorImpossibleAttributes {
    private static final String PREFIX = "net/farmingtales/ftboss/";

    public static void main(String[] arguments) throws Exception {
        Path payload = Path.of(arguments[2]);
        boolean movementOnly = arguments.length == 4 && arguments[3].equals("--movement-only");
        try (JarFile baseline = new JarFile(arguments[0]); JarFile compiled = new JarFile(arguments[1])) {
            for (String name : movementOnly ? List.of("boss/ManorDifficulty") : List.of("boss/ManorDifficulty", "boss/ai/ManorRaidRules")) {
                ClassNode original = read(baseline, name), updated = read(compiled, name);
                List<String> methods = movementOnly ? List.of("movementMultiplier") : name.endsWith("ManorDifficulty")
                        ? List.of("acceleratedTicks", "attackRate", "movementMultiplier") : List.of("maximumHealth");
                for (String method : methods) {
                    var replacement = updated.methods.stream().filter(entry -> entry.name.equals(method)).findFirst().orElseThrow();
                    int index = -1;
                    for (int position = 0; position < original.methods.size(); position++) {
                        if (original.methods.get(position).name.equals(method)) index = position;
                    }
                    if (index < 0) throw new IllegalStateException("Missing method " + method);
                    original.methods.set(index, replacement);
                }
                for (var field : updated.fields) {
                    if (!field.name.matches(movementOnly ? "IMPOSSIBLE_MOVEMENT_MULTIPLIER" : "IMPOSSIBLE_(ATTRIBUTE|MOVEMENT|ATTACK|HEALTH)_MULTIPLIER")) continue;
                    original.fields.removeIf(entry -> entry.name.equals(field.name));
                    original.fields.add(field);
                }
                write(payload, original);
            }
            if (movementOnly) {
                write(payload, read(compiled, "boss/ai/ManorImpossibleAttributes"));
                System.out.println("PASS movement-only patch; all other difficulty, attribute and damage-limit paths retained");
                return;
            }
            ClassNode hurt = read(baseline, "boss/ManorImpossibleRules");
            int changed = 0;
            for (var method : hurt.methods) {
                if (!method.name.equals("onHurt")) continue;
                for (var instruction : method.instructions) {
                    if (instruction instanceof LdcInsnNode constant && Float.valueOf(4.0F).equals(constant.cst)) {
                        constant.cst = (5.0F / 6.0F) * 10.0F;
                        changed++;
                    }
                }
            }
            if (changed != 1) throw new IllegalStateException("Expected one existing impossible damage multiplier: " + changed);
            write(payload, hurt);
            write(payload, read(compiled, "boss/ai/ManorImpossibleAttributes"));
        }
        System.out.println("PASS four-entry impossible attributes patch; all damage-limit classes retained");
    }

    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(PREFIX + name + ".class")).readAllBytes()).accept(node, 0);
        return node;
    }

    private static void write(Path root, ClassNode node) throws Exception {
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        Path output = root.resolve(node.name + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
    }
}
