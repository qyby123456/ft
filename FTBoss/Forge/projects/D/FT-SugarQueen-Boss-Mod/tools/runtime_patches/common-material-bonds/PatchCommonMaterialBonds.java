import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/** 材料羁绊增量补丁；2026-10-25 仅替换配方判定方法和追加枚举，保留运行时其他构筑契约。 */
public final class PatchCommonMaterialBonds {
    private static final String ROOT = "net/farmingtales/ftboss/util/";
    private static final Set<String> METHODS = Set.of("links", "linkRequirements");

    private static ClassNode read(byte[] bytes) {
        ClassNode result = new ClassNode();
        new ClassReader(bytes).accept(result, 0);
        return result;
    }

    private static String trace(MethodNode method) {
        Textifier printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        StringWriter text = new StringWriter();
        printer.print(new PrintWriter(text));
        return text.toString();
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments[0].equals("--verify")) {
            verify(Path.of(arguments[1]), Path.of(arguments[2]));
            return;
        }
        Path compiled = Path.of(arguments[1]);
        Path output = Path.of(arguments[2]);
        try (JarFile baseline = new JarFile(arguments[0])) {
            byte[] bytes = baseline.getInputStream(baseline.getJarEntry(ROOT + "SwordCavityRules.class")).readAllBytes();
            ClassNode original = read(bytes);
            ClassNode replacement = read(Files.readAllBytes(compiled.resolve(ROOT + "SwordCavityRules.class")));
            for (String name : METHODS) {
                MethodNode old = original.methods.stream().filter(method -> method.name.equals(name)).findFirst().orElseThrow();
                MethodNode updated = replacement.methods.stream().filter(method -> method.name.equals(name)).findFirst().orElseThrow();
                if (!old.desc.equals(updated.desc)) throw new AssertionError("Changed method descriptor: " + name);
                original.methods.set(original.methods.indexOf(old), updated);
            }
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            original.accept(writer);
            byte[] patched = writer.toByteArray();
            ClassNode before = read(bytes);
            ClassNode after = read(patched);
            for (MethodNode method : before.methods) {
                if (METHODS.contains(method.name)) continue;
                MethodNode preserved = after.methods.stream().filter(candidate -> candidate.name.equals(method.name)
                        && candidate.desc.equals(method.desc)).findFirst().orElseThrow();
                if (!trace(method).equals(trace(preserved))) throw new AssertionError("Unrelated method changed: " + method.name);
            }
            Path target = output.resolve(ROOT + "SwordCavityRules.class");
            Files.createDirectories(target.getParent());
            Files.write(target, patched);
        }
        System.out.println("PASS surgical patch: links/linkRequirements only; unrelated rule methods preserved");
    }

    private static void verify(Path before, Path after) throws Exception {
        Set<String> allowed = Set.of(ROOT + "SwordCavityRules.class", ROOT + "SwordCavityRules$Link.class",
                ROOT + "CommonMaterialBondRules.class", ROOT + "CommonMaterialBondRules$Family.class",
                "assets/ftboss/lang/zh_cn.json", "assets/ftboss/lang/en_us.json", "assets/ftboss/weapon_bonds/guide.json");
        Set<String> changed = new HashSet<>();
        int preserved = 0;
        try (JarFile baseline = new JarFile(before.toFile()); JarFile candidate = new JarFile(after.toFile())) {
            for (var entry : baseline.stream().toList()) {
                var current = candidate.getJarEntry(entry.getName());
                if (current == null) throw new AssertionError("Removed entry: " + entry.getName());
                if (!Arrays.equals(baseline.getInputStream(entry).readAllBytes(), candidate.getInputStream(current).readAllBytes())) {
                    if (!allowed.contains(entry.getName())) throw new AssertionError("Unrelated entry changed: " + entry.getName());
                    changed.add(entry.getName());
                } else preserved++;
            }
            for (var entry : candidate.stream().toList()) if (baseline.getJarEntry(entry.getName()) == null) {
                if (!allowed.contains(entry.getName())) throw new AssertionError("Unexpected new entry: " + entry.getName());
                changed.add(entry.getName());
            }
            if (!changed.equals(allowed)) throw new AssertionError("Missing expected patch entries: " + changed);
            for (String entry : allowed) if (entry.endsWith(".class")) {
                ClassNode type = read(candidate.getInputStream(candidate.getJarEntry(entry)).readAllBytes());
                for (MethodNode method : type.methods) if (method.instructions.size() > 0) {
                    new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
                }
            }
        }
        System.out.println("PASS final archive: exactly 7 scoped entries; " + preserved + " unrelated entries byte-identical; bytecode verified");
    }
}
