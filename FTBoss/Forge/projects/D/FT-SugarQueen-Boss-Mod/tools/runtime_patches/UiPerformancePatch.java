import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/** 界面性能增量构建；2026-10-27 只替换光影探测方法并校验其余运行方法。 */
public final class UiPerformancePatch {
    private static final String BASE = "net/farmingtales/ftboss/client/render/bloom/";
    private static final String RENDERER = BASE + "SpellBloomRenderer";
    private static final String[] HELPERS = {BASE + "ShaderPackProbe", BASE + "ShaderPackProbe$DefaultProbe"};

    private static ClassNode read(JarFile archive, String name) throws Exception {
        var entry = archive.getJarEntry(name + ".class");
        if (entry == null) throw new IllegalStateException("Missing class: " + name);
        ClassNode type = new ClassNode();
        new ClassReader(archive.getInputStream(entry).readAllBytes()).accept(type, 0);
        return type;
    }

    private static MethodNode selected(ClassNode type) {
        return type.methods.stream().filter(method -> method.name.equals("shaderPackActive") && method.desc.equals("()Z"))
                .findFirst().orElseThrow();
    }

    private static String text(MethodNode method) {
        Textifier printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        StringWriter output = new StringWriter();
        printer.print(new PrintWriter(output));
        return method.access + "/" + method.name + method.desc + "/" + output.toString().replaceAll("MAXSTACK = [0-9]+", "MAXSTACK = computed");
    }

    private static void write(Path output, ClassNode type) throws Exception {
        for (MethodNode method : type.methods) new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path destination = output.resolve(type.name + ".class");
        Files.createDirectories(destination.getParent());
        Files.write(destination, writer.toByteArray());
    }

    public static void main(String[] arguments) throws Exception {
        boolean verify = arguments.length == 2;
        try (JarFile baseline = new JarFile(arguments[0]); JarFile candidate = new JarFile(arguments[1])) {
            ClassNode original = read(baseline, RENDERER);
            ClassNode updated = read(candidate, RENDERER);
            if (verify) {
                if (original.fields.size() != updated.fields.size() || original.methods.size() != updated.methods.size()) {
                    throw new AssertionError("Renderer class shape changed");
                }
                Map<String, MethodNode> methods = new HashMap<>();
                for (MethodNode method : updated.methods) methods.put(method.name + method.desc, method);
                for (MethodNode method : original.methods) {
                    MethodNode replacement = methods.get(method.name + method.desc);
                    if (replacement == null) throw new AssertionError("Runtime method removed");
                    if (method != selected(original) && !text(method).equals(text(replacement))) {
                        throw new AssertionError("Unrelated runtime method changed: " + method.name);
                    }
                }
                for (int index = 0; index < original.fields.size(); index++) {
                    var before = original.fields.get(index);
                    var after = updated.fields.get(index);
                    if (!before.name.equals(after.name) || !before.desc.equals(after.desc) || before.access != after.access) {
                        throw new AssertionError("Runtime field changed");
                    }
                }
                new Analyzer<>(new BasicVerifier()).analyze(RENDERER, selected(updated));
            } else {
                try (JarFile beforeSource = new JarFile(arguments[3])) {
                    if (!text(selected(original)).equals(text(selected(read(beforeSource, RENDERER))))) {
                        throw new AssertionError("Source/runtime shader probe drift; refusing patch");
                    }
                }
                original.methods.set(original.methods.indexOf(selected(original)), selected(updated));
                write(Path.of(arguments[2]), original);
            }
            for (String helper : HELPERS) {
                ClassNode type = read(candidate, helper);
                for (MethodNode method : type.methods) new Analyzer<>(new BasicVerifier()).analyze(helper, method);
                if (!verify) write(Path.of(arguments[2]), type);
            }
        }
        System.out.println("PASS: one renderer method; all other runtime methods preserved; two helper classes verified");
    }
}
