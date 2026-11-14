import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/** 基于现用成品增量替换连射和缩放入口，校验其余方法与资源不回退（2026-11-04）。 */
public final class XuanlanBladeBurstPatch {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static final Map<String, List<String>> METHODS = Map.of(
            PREFIX + "util/XuanlanRules", List.of(),
            PREFIX + "item/XuanlanMoonWeaponItem", List.of("m_7203_"),
            PREFIX + "client/render/PlayerMoonChakramRenderer", List.of("render"),
            PREFIX + "client/render/XuanlanTrailVisual", List.of("render"));
    private static final Map<String, Set<String>> FIELDS = Map.of(
            PREFIX + "util/XuanlanRules", Set.of("BLADE_COOLDOWN_TICKS", "BLADE_SHOT_INTERVAL_TICKS", "BLADE_SPREAD_RADIANS"),
            PREFIX + "client/render/PlayerMoonChakramRenderer", Set.of("PROJECTILE_SCALE"));
    private static final List<String> ADDED = List.of(PREFIX + "skill/XuanlanBladeBurst", PREFIX + "skill/XuanlanBladeBurst$Burst");

    private static ClassNode read(byte[] bytes) {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, ClassReader.SKIP_DEBUG);
        return type;
    }

    private static byte[] bytes(ClassNode type) {
        ClassWriter writer = new ClassWriter(0);
        type.accept(writer);
        return writer.toByteArray();
    }

    private static ClassNode compiled(Path classes, String name, ZicaiVoidPatch.Names names) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(Files.readAllBytes(classes.resolve(name + ".class")))
                .accept(new ClassRemapper(type, names), ClassReader.SKIP_DEBUG);
        return type;
    }

    private static MethodNode method(ClassNode type, String name) {
        var found = type.methods.stream().filter(candidate -> candidate.name.equals(name)
                && (candidate.access & Opcodes.ACC_BRIDGE) == 0).toList();
        if (found.size() != 1) throw new IllegalStateException("Expected one non-bridge method: " + type.name + "." + name);
        return found.get(0);
    }

    private static byte[] fingerprint(MethodNode method) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(61, 1, "MethodFingerprint", null, "java/lang/Object", null);
        method.accept(writer);
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static void save(Path output, String name, ClassNode type) throws Exception {
        Path destination = output.resolve(name + ".class");
        Files.createDirectories(destination.getParent());
        Files.write(destination, bytes(type));
    }

    private static void patch(Path baseline, Path classes, Path mappings, Path output) throws Exception {
        var names = new ZicaiVoidPatch.Names(mappings);
        try (ZipFile input = new ZipFile(baseline.toFile())) {
            for (var entry : METHODS.entrySet()) {
                ClassNode original = read(input.getInputStream(input.getEntry(entry.getKey() + ".class")).readAllBytes());
                ClassNode replacement = compiled(classes, entry.getKey(), names);
                for (String name : entry.getValue()) {
                    MethodNode incoming = method(replacement, name);
                    MethodNode outgoing = method(original, name);
                    if (!outgoing.desc.equals(incoming.desc)) throw new IllegalStateException("Method contract changed");
                    original.methods.set(original.methods.indexOf(outgoing), incoming);
                }
                Set<String> fields = FIELDS.getOrDefault(entry.getKey(), Set.of());
                original.fields.removeIf(field -> fields.contains(field.name));
                original.fields.addAll(replacement.fields.stream().filter(field -> fields.contains(field.name)).toList());
                save(output, entry.getKey(), original);
            }
            for (String name : ADDED) {
                if (input.getEntry(name + ".class") != null) throw new IllegalStateException("Burst already installed");
                save(output, name, compiled(classes, name, names));
            }
        }
        System.out.println("PASS: three target methods and constants patched; server burst subscriber added");
    }

    private static void verify(Path baseline, Path candidate, Path classes, Path mappings) throws Exception {
        var names = new ZicaiVoidPatch.Names(mappings);
        Set<String> expected = new TreeSet<>();
        for (String name : METHODS.keySet()) expected.add(name + ".class");
        expected.add("assets/ftboss/lang/zh_cn.json");
        expected.add("assets/ftboss/lang/en_us.json");
        Set<String> additions = new TreeSet<>();
        for (String name : ADDED) additions.add(name + ".class");
        Set<String> changed = new TreeSet<>();
        Set<String> added = new TreeSet<>();
        int preserved = 0;
        try (ZipFile before = new ZipFile(baseline.toFile()); ZipFile after = new ZipFile(candidate.toFile())) {
            Map<String, byte[]> originals = new LinkedHashMap<>();
            var entries = before.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                originals.put(entry.getName(), before.getInputStream(entry).readAllBytes());
            }
            entries = after.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                byte[] prior = originals.remove(entry.getName());
                if (prior == null) added.add(entry.getName());
                else if (!Arrays.equals(prior, after.getInputStream(entry).readAllBytes())) changed.add(entry.getName());
                else preserved++;
            }
            if (!originals.isEmpty() || !changed.equals(expected) || !added.equals(additions)) {
                throw new AssertionError("Unexpected JAR delta: changed=" + changed + ", added=" + added + ", lost=" + originals.keySet());
            }
            for (var entry : METHODS.entrySet()) {
                String file = entry.getKey() + ".class";
                ClassNode original = read(before.getInputStream(before.getEntry(file)).readAllBytes());
                ClassNode updated = read(after.getInputStream(after.getEntry(file)).readAllBytes());
                ClassNode source = compiled(classes, entry.getKey(), names);
                if (updated.methods.size() != original.methods.size()) throw new AssertionError("Method inventory changed: " + file);
                for (MethodNode prior : original.methods) {
                    if (entry.getValue().contains(prior.name) && (prior.access & Opcodes.ACC_BRIDGE) == 0) continue;
                    var matching = updated.methods.stream().filter(value -> value.name.equals(prior.name) && value.desc.equals(prior.desc)).findFirst().orElseThrow();
                    if (!Arrays.equals(fingerprint(prior), fingerprint(matching))) throw new AssertionError("Unrelated method changed: " + file + "." + prior.name);
                }
                for (String name : entry.getValue()) {
                    if (!Arrays.equals(fingerprint(method(updated, name)), fingerprint(method(source, name)))) throw new AssertionError("Runtime differs from source: " + file + "." + name);
                }
                Set<String> fields = FIELDS.getOrDefault(entry.getKey(), Set.of());
                for (var field : original.fields) {
                    if (fields.contains(field.name)) continue;
                    if (updated.fields.stream().noneMatch(value -> value.name.equals(field.name) && value.desc.equals(field.desc)
                            && value.access == field.access && java.util.Objects.equals(value.value, field.value))) throw new AssertionError("Unrelated field changed: " + field.name);
                }
                for (String name : fields) {
                    var actual = updated.fields.stream().filter(field -> field.name.equals(name)).findFirst().orElseThrow();
                    var declared = source.fields.stream().filter(field -> field.name.equals(name)).findFirst().orElseThrow();
                    if (!java.util.Objects.equals(actual.value, declared.value)) throw new AssertionError("Wrong constant: " + name);
                }
            }
            for (String name : ADDED) {
                if (!Arrays.equals(after.getInputStream(after.getEntry(name + ".class")).readAllBytes(), bytes(compiled(classes, name, names)))) {
                    throw new AssertionError("New class differs from compiled source: " + name);
                }
            }
            for (String language : List.of("zh_cn", "en_us")) {
                String file = "assets/ftboss/lang/" + language + ".json";
                if (after.getEntry(file) == null) throw new AssertionError("Missing language: " + file);
            }
        }
        System.out.println("PASS: " + preserved + " JAR entries byte-identical; exactly six changed and two added; target methods and subscriber match source");
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments[0].equals("patch")) patch(Path.of(arguments[1]), Path.of(arguments[2]), Path.of(arguments[3]), Path.of(arguments[4]));
        else if (arguments[0].equals("verify")) verify(Path.of(arguments[1]), Path.of(arguments[2]), Path.of(arguments[3]), Path.of(arguments[4]));
        else throw new IllegalArgumentException("Expected patch or verify");
    }
}
