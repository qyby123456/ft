import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.util.TraceClassVisitor;

public final class WeaponEvolutionChancePatch {
    private static final String RULES = "net/farmingtales/ftboss/util/FoodWeaponBuildRules.class";
    private static final String PROGRESSION = "net/farmingtales/ftboss/item/FoodWeaponProgression";

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static String trace(ClassNode node) {
        StringWriter output = new StringWriter();
        node.accept(new TraceClassVisitor(new PrintWriter(output)));
        return output.toString();
    }

    private static void calls(JarFile archive, String owner, String method, String targetOwner, String targetName) throws Exception {
        ClassNode node = read(archive.getInputStream(archive.getJarEntry(owner + ".class")).readAllBytes());
        for (var member : node.methods) if (member.name.equals(method)) {
            for (var instruction : member.instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals(targetOwner)
                        && call.name.equals(targetName)) return;
            }
        }
        throw new AssertionError(owner + "." + method + " no longer uses " + targetOwner + "." + targetName);
    }

    public static void main(String[] arguments) throws Exception {
        try (JarFile baseline = new JarFile(arguments[0])) {
            ClassNode target = read(baseline.getInputStream(baseline.getJarEntry(RULES)).readAllBytes());
            ClassNode donor = read(Files.readAllBytes(Path.of(arguments[1]).resolve(RULES)));
            var replacement = donor.methods.stream().filter(method -> method.name.equals("successPercent")
                    && method.desc.equals("(IIZ)I")).findFirst().orElseThrow();
            int replaced = 0;
            for (int index = 0; index < target.methods.size(); index++) {
                var method = target.methods.get(index);
                if (method.name.equals(replacement.name) && method.desc.equals(replacement.desc)) {
                    target.methods.set(index, replacement);
                    replaced++;
                }
            }
            if (replaced != 1) throw new AssertionError("Expected exactly one successPercent method");
            if (arguments.length == 4) {
                try (JarFile candidate = new JarFile(arguments[3])) {
                    ClassNode actual = read(candidate.getInputStream(candidate.getJarEntry(RULES)).readAllBytes());
                    if (!trace(target).equals(trace(actual))) throw new AssertionError("Unrelated rule bytecode changed");
                    calls(candidate, PROGRESSION, "chance", RULES.substring(0, RULES.length() - 6), "successPercent");
                    calls(candidate, PROGRESSION, "evolve", PROGRESSION, "chance");
                    calls(candidate, PROGRESSION, "evolve", RULES.substring(0, RULES.length() - 6), "succeeds");
                    calls(candidate, "net/farmingtales/ftboss/client/StatueHubScreen", "renderEvolution", PROGRESSION, "chance");
                    calls(candidate, "net/farmingtales/ftboss/client/StatueHubScreen", "ingredientTooltip", PROGRESSION, "chance");
                }
                System.out.println("PASS exact method audit: only successPercent changed; UI and server use the same probability");
            } else {
                ClassWriter writer = new ClassWriter(0);
                target.accept(writer);
                Path destination = Path.of(arguments[2]).resolve(RULES);
                Files.createDirectories(destination.getParent());
                Files.write(destination, writer.toByteArray());
                System.out.println("PASS replaced only FoodWeaponBuildRules.successPercent(IIZ)I");
            }
        }
    }
}
