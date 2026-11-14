import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class RetireExcessDamagePatch {
    public static void main(String[] arguments) throws Exception {
        Path donor = Path.of(arguments[1]);
        Path payload = Path.of(arguments[2]);
        try (JarFile baseline = new JarFile(arguments[0])) {
            patch(baseline, donor, payload, "boss/ai/FinalBossDamageGuard", "blocked", "MAX_FINAL_DAMAGE");
            patch(baseline, donor, payload, "util/ManorDamageRules", "ignored", "MAX_FINAL_HIT");
        }
    }

    private static void patch(JarFile baseline, Path donor, Path payload, String type, String name, String constant) throws Exception {
        String entry = "net/farmingtales/ftboss/" + type + ".class";
        ClassNode original = new ClassNode();
        new ClassReader(baseline.getInputStream(baseline.getJarEntry(entry)).readAllBytes()).accept(original, 0);
        ClassNode compiled = new ClassNode();
        new ClassReader(Files.readAllBytes(donor.resolve(entry))).accept(compiled, 0);
        MethodNode replacement = compiled.methods.stream()
                .filter(method -> method.name.equals(name) && method.desc.equals("(F)Z")).findFirst().orElseThrow();
        int changed = 0;
        for (int index = 0; index < original.methods.size(); index++) {
            MethodNode method = original.methods.get(index);
            if (method.name.equals(name) && method.desc.equals("(F)Z")) {
                original.methods.set(index, replacement);
                changed++;
            }
        }
        var field = original.fields.stream().filter(candidate -> candidate.name.equals(constant)).findFirst().orElseThrow();
        if (!Float.valueOf(300.0F).equals(field.value) || changed != 1) {
            throw new IllegalStateException("Unexpected excess-damage baseline: " + entry);
        }
        field.value = Float.MAX_VALUE;
        ClassWriter writer = new ClassWriter(0);
        original.accept(writer);
        Path output = payload.resolve(entry);
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
        System.out.println("PASS patched only " + type + "." + name + " and legacy threshold constant");
    }
}
