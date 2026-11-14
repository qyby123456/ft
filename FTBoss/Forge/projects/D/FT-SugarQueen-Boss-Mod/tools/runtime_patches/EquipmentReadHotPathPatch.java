import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class EquipmentReadHotPathPatch {
    private static final String BASE = "net/farmingtales/ftboss/item/";
    private static final Map<String, Set<String>> CHANGES = Map.of(
            BASE + "EquipmentWeaponBuild", Set.of("profile", "cellUnlocked"),
            BASE + "SwordCavityBuild", Set.of("cellUnlocked"));

    private static ClassNode read(byte[] bytes) {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, ClassReader.SKIP_DEBUG);
        return type;
    }

    private static ClassNode read(JarFile jar, String name) throws Exception {
        return read(jar.getInputStream(jar.getJarEntry(name + ".class")).readAllBytes());
    }

    private static String text(MethodNode method) {
        Textifier printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        StringWriter output = new StringWriter();
        printer.print(new PrintWriter(output));
        return method.access + ":" + method.name + method.desc + ":" + output;
    }

    private static Map<String, MethodNode> methods(ClassNode type) {
        Map<String, MethodNode> methods = new HashMap<>();
        for (MethodNode method : type.methods) methods.put(method.name + method.desc, method);
        return methods;
    }

    private static void equivalent(ClassNode original, ClassNode updated, Set<String> allowed) {
        if (!original.superName.equals(updated.superName) || !original.interfaces.equals(updated.interfaces)
                || original.fields.size() != updated.fields.size()) throw new AssertionError("Class shape changed: " + original.name);
        for (int index = 0; index < original.fields.size(); index++) {
            var before = original.fields.get(index);
            var after = updated.fields.get(index);
            if (!before.name.equals(after.name) || !before.desc.equals(after.desc) || before.access != after.access
                    || !java.util.Objects.equals(before.value, after.value)) throw new AssertionError("Runtime field changed");
        }
        Map<String, MethodNode> methods = methods(updated);
        for (MethodNode method : original.methods) {
            MethodNode replacement = methods.remove(method.name + method.desc);
            if (replacement == null) throw new AssertionError("Removed runtime method: " + method.name);
            if (!allowed.contains(method.name) && !text(method).equals(text(replacement))) {
                throw new AssertionError("Unrelated runtime method changed: " + original.name + "." + method.name);
            }
        }
        for (MethodNode added : methods.values()) {
            if (!original.name.equals(BASE + "EquipmentWeaponBuild") || !added.name.equals("cellUnlocked")) {
                throw new AssertionError("Unexpected added method: " + added.name);
            }
        }
    }

    public static void main(String[] arguments) throws Exception {
        try (JarFile baseline = new JarFile(arguments[1])) {
            if (arguments[0].equals("verify")) {
                try (JarFile candidate = new JarFile(arguments[2])) {
                    for (var entry : CHANGES.entrySet()) equivalent(read(baseline, entry.getKey()), read(candidate, entry.getKey()), entry.getValue());
                }
            } else {
                Path before = Path.of(arguments[2]);
                Path after = Path.of(arguments[3]);
                Path output = Path.of(arguments[4]);
                for (var entry : CHANGES.entrySet()) {
                    ClassNode original = read(baseline, entry.getKey());
                    ClassNode oldSource = read(Files.readAllBytes(before.resolve(entry.getKey() + ".class")));
                    ClassNode newSource = read(Files.readAllBytes(after.resolve(entry.getKey() + ".class")));
                    Map<String, MethodNode> oldMethods = methods(oldSource);
                    Map<String, MethodNode> newMethods = methods(newSource);
                    for (MethodNode method : original.methods.toArray(MethodNode[]::new)) {
                        if (!entry.getValue().contains(method.name)) continue;
                        String key = method.name + method.desc;
                        if (!text(method).equals(text(oldMethods.get(key)))) {
                            throw new AssertionError("Source/runtime drift: " + entry.getKey() + "." + method.name);
                        }
                        original.methods.set(original.methods.indexOf(method), newMethods.get(key));
                    }
                    if (entry.getKey().equals(BASE + "EquipmentWeaponBuild")) {
                        MethodNode added = newSource.methods.stream().filter(method -> method.name.equals("cellUnlocked")).findFirst().orElseThrow();
                        if (methods(original).containsKey(added.name + added.desc)) throw new AssertionError("Fast path already exists");
                        original.methods.add(added);
                    }
                    for (MethodNode method : original.methods) new Analyzer<>(new BasicVerifier()).analyze(original.name, method);
                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    ClassNode withFrames = new ClassNode();
                    new ClassReader(baseline.getInputStream(baseline.getJarEntry(entry.getKey() + ".class")).readAllBytes()).accept(withFrames, 0);
                    ClassNode fullSource = new ClassNode();
                    new ClassReader(Files.readAllBytes(after.resolve(entry.getKey() + ".class"))).accept(fullSource, 0);
                    Map<String, MethodNode> changed = methods(fullSource);
                    for (MethodNode method : withFrames.methods.toArray(MethodNode[]::new)) {
                        if (entry.getValue().contains(method.name)) withFrames.methods.set(withFrames.methods.indexOf(method), changed.get(method.name + method.desc));
                    }
                    if (entry.getKey().equals(BASE + "EquipmentWeaponBuild")) {
                        withFrames.methods.add(fullSource.methods.stream().filter(method -> method.name.equals("cellUnlocked")).findFirst().orElseThrow());
                    }
                    withFrames.accept(writer);
                    Path destination = output.resolve(entry.getKey() + ".class");
                    Files.createDirectories(destination.getParent());
                    Files.write(destination, writer.toByteArray());
                }
            }
        }
        System.out.println("PASS: two runtime classes, two replaced methods, one added method; unrelated runtime methods preserved");
    }
}
