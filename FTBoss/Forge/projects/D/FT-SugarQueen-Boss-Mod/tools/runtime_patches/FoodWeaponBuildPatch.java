import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/** 比较改动前后编译类，只向当次运行基线移植发生变化的方法和字段。 */
public final class FoodWeaponBuildPatch {
    private static ClassNode read(byte[] bytes, int flags) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, flags);
        return node;
    }

    private static Map<String, MethodNode> methods(ClassNode node) {
        Map<String, MethodNode> result = new LinkedHashMap<>();
        for (MethodNode method : node.methods) result.put(method.name + method.desc, method);
        return result;
    }

    private static String methodFingerprint(MethodNode method) {
        if (method == null) return "<absent>";
        Textifier printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        StringWriter text = new StringWriter();
        printer.print(new PrintWriter(text));
        return method.access + "/" + method.signature + "/" + method.exceptions + "/" + text;
    }

    private static String fieldFingerprint(FieldNode field) {
        return field == null ? "<absent>" : field.access + "/" + field.name + "/" + field.desc + "/" + field.signature + "/" + field.value;
    }

    private static Map<String, FieldNode> fields(ClassNode node) {
        Map<String, FieldNode> result = new HashMap<>();
        for (FieldNode field : node.fields) result.put(field.name + field.desc, field);
        return result;
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    private static byte[] entry(JarFile jar, String name) throws Exception {
        var item = jar.getJarEntry(name);
        return item == null ? null : jar.getInputStream(item).readAllBytes();
    }

    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[3]);
        Files.createDirectories(output);
        List<String> reports = new ArrayList<>();
        int preserved = 0;
        int changed = 0;
        try (JarFile baseline = new JarFile(arguments[0]); JarFile before = new JarFile(arguments[1]); JarFile after = new JarFile(arguments[2])) {
            var entries = after.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (!name.endsWith(".class")) continue;
                byte[] nextBytes = entry(after, name);
                byte[] priorBytes = entry(before, name);
                byte[] runtimeBytes = entry(baseline, name);
                if (priorBytes == null) {
                    if (runtimeBytes != null) throw new IllegalStateException("new class already present in runtime baseline: " + name);
                    Path file = output.resolve(name);
                    Files.createDirectories(file.getParent());
                    Files.write(file, nextBytes);
                    reports.add("{\"entry\":" + quote(name) + ",\"kind\":\"new-class\"}");
                    changed++;
                    continue;
                }
                if (runtimeBytes == null) throw new IllegalStateException("existing source class absent from runtime: " + name);
                ClassNode prior = read(priorBytes, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                ClassNode next = read(nextBytes, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                ClassNode target = read(runtimeBytes, 0);
                Map<String, MethodNode> baselineMethods = methods(read(runtimeBytes, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES));
                ClassNode nextFull = read(nextBytes, 0);
                Map<String, MethodNode> oldMethods = methods(prior);
                Map<String, MethodNode> newMethods = methods(next);
                Map<String, MethodNode> targetMethods = methods(target);
                Map<String, MethodNode> newFullMethods = methods(nextFull);
                List<String> methodChanges = new ArrayList<>();
                for (String key : oldMethods.keySet()) {
                    if (newMethods.containsKey(key)) continue;
                    MethodNode old = targetMethods.get(key);
                    if (old == null) throw new IllegalStateException("cannot remove absent runtime method: " + name + ":" + key);
                    if (!methodFingerprint(oldMethods.get(key)).equals(methodFingerprint(baselineMethods.get(key)))) {
                        throw new IllegalStateException("runtime-only changes in removed method; rebase required: " + name + ":" + key);
                    }
                    target.methods.remove(old);
                    methodChanges.add(quote("remove " + key));
                }
                for (String key : newMethods.keySet()) {
                    if (methodFingerprint(oldMethods.get(key)).equals(methodFingerprint(newMethods.get(key)))) continue;
                    MethodNode previous = targetMethods.get(key);
                    if (oldMethods.containsKey(key) && previous == null) {
                        throw new IllegalStateException("changed source method absent from runtime: " + name + ":" + key);
                    }
                    if (!oldMethods.containsKey(key) && previous != null) {
                        throw new IllegalStateException("new method conflicts with runtime-only method: " + name + ":" + key);
                    }
                    if (previous != null && !methodFingerprint(oldMethods.get(key)).equals(methodFingerprint(baselineMethods.get(key)))) {
                        throw new IllegalStateException("runtime-only changes in selected method; rebase required: " + name + ":" + key);
                    }
                    if (previous != null) target.methods.remove(previous);
                    target.methods.add(newFullMethods.get(key));
                    methodChanges.add(quote((previous == null ? "add " : "replace ") + key));
                }
                Map<String, FieldNode> oldFields = fields(prior);
                Map<String, FieldNode> newFields = fields(next);
                Map<String, FieldNode> targetFields = fields(target);
                List<String> fieldChanges = new ArrayList<>();
                for (String key : newFields.keySet()) {
                    if (fieldFingerprint(oldFields.get(key)).equals(fieldFingerprint(newFields.get(key)))) continue;
                    FieldNode previous = targetFields.get(key);
                    if (!oldFields.containsKey(key) && previous != null) throw new IllegalStateException("field conflict: " + name + ":" + key);
                    if (oldFields.containsKey(key) && !fieldFingerprint(oldFields.get(key)).equals(fieldFingerprint(previous))) {
                        throw new IllegalStateException("runtime-only changes in selected field; rebase required: " + name + ":" + key);
                    }
                    if (previous != null) target.fields.remove(previous);
                    target.fields.add(newFields.get(key));
                    fieldChanges.add(quote(key));
                }
                if (methodChanges.isEmpty() && fieldChanges.isEmpty()) continue;
                if (target.innerClasses != null && nextFull.innerClasses != null) {
                    for (var inner : nextFull.innerClasses) {
                        if (target.innerClasses.stream().noneMatch(existing -> existing.name.equals(inner.name))) target.innerClasses.add(inner);
                    }
                }
                if (nextFull.nestMembers != null) {
                    if (target.nestMembers == null) target.nestMembers = new ArrayList<>();
                    for (String member : nextFull.nestMembers) if (!target.nestMembers.contains(member)) target.nestMembers.add(member);
                }
                if (target.version != nextFull.version || target.access != nextFull.access || !target.superName.equals(nextFull.superName)) {
                    throw new IllegalStateException("class header changed unexpectedly: " + name);
                }
                ClassWriter writer = new ClassWriter(0);
                target.accept(writer);
                byte[] result = writer.toByteArray();
                ClassNode verify = read(result, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                ClassNode original = read(runtimeBytes, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                Map<String, MethodNode> verifiedMethods = methods(verify);
                for (MethodNode method : original.methods) {
                    String key = method.name + method.desc;
                    boolean selected = methodChanges.stream().anyMatch(change -> change.equals(quote("replace " + key)) || change.equals(quote("remove " + key)));
                    if (!selected) {
                        if (!methodFingerprint(method).equals(methodFingerprint(verifiedMethods.get(key)))) {
                            throw new IllegalStateException("unrelated method changed: " + name + ":" + key);
                        }
                        preserved++;
                    }
                }
                Map<String, FieldNode> verifiedFields = fields(verify);
                for (FieldNode field : original.fields) {
                    String key = field.name + field.desc;
                    if (!fieldChanges.contains(quote(key)) && !fieldFingerprint(field).equals(fieldFingerprint(verifiedFields.get(key)))) {
                        throw new IllegalStateException("unrelated field changed: " + name + ":" + key);
                    }
                }
                Path file = output.resolve(name);
                Files.createDirectories(file.getParent());
                Files.write(file, result);
                reports.add("{\"entry\":" + quote(name) + ",\"kind\":\"method-field-delta\",\"methods\":["
                        + String.join(",", methodChanges) + "],\"fields\":[" + String.join(",", fieldChanges) + "]}");
                changed++;
            }
        }
        String report = "{\n\"changedClassEntries\":" + changed + ",\n\"preservedUnrelatedMethods\":" + preserved
                + ",\n\"entries\":[\n" + String.join(",\n", reports) + "\n]}\n";
        Files.writeString(Path.of(arguments[4]), report, StandardCharsets.UTF_8);
        System.out.println("PASS method-level delta: " + changed + " class entries, " + preserved + " unrelated runtime methods preserved");
    }
}
