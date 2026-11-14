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
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class XuanlanPlayerFlywheelPatch {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static final String RULES = PREFIX + "util/XuanlanRules";
    private static final Map<String, List<String>> METHODS = Map.of(
            RULES, List.of("bladeDamage"),
            PREFIX + "skill/XuanlanBladeRoster", List.of("<clinit>", "createKeys", "count", "track"),
            PREFIX + "skill/XuanlanCombat", List.of("canLaunchBlade"),
            PREFIX + "item/XuanlanMoonWeaponItem", List.of("m_7203_"),
            PREFIX + "entity/PlayerMoonChakramEntity", List.of("m_8119_"));
    private static final Set<String> FIELDS = Set.of("MAX_ACTIVE_BLADES", "BLADES_PER_VOLLEY",
            "BLADE_COOLDOWN_TICKS", "BLADE_DAMAGE", "BLADE_SPREAD_RADIANS");

    private static ClassNode read(byte[] bytes) {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, ClassReader.SKIP_DEBUG);
        return type;
    }

    private static ClassNode compiled(Path classes, String name, ZicaiVoidPatch.Names names) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(Files.readAllBytes(classes.resolve(name + ".class")))
                .accept(new ClassRemapper(type, names), ClassReader.SKIP_DEBUG);
        return type;
    }

    private static MethodNode method(ClassNode type, String name) {
        var found = type.methods.stream().filter(candidate -> candidate.name.equals(name)).toList();
        if (found.size() != 1) throw new IllegalStateException("Expected one method: " + type.name + "." + name);
        return found.get(0);
    }

    private static byte[] fingerprint(MethodNode method) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(61, 1, "MethodFingerprint", null, "java/lang/Object", null);
        method.accept(writer);
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static void patch(Path baseline, Path classes, Path mappings, Path output) throws Exception {
        var names = new ZicaiVoidPatch.Names(mappings);
        try (ZipFile input = new ZipFile(baseline.toFile())) {
            for (var entry : METHODS.entrySet()) {
                ClassNode original = read(input.getInputStream(input.getEntry(entry.getKey() + ".class")).readAllBytes());
                ClassNode replacement = compiled(classes, entry.getKey(), names);
                for (String name : entry.getValue()) {
                    MethodNode incoming = method(replacement, name);
                    if (name.equals("createKeys")) {
                        if (original.methods.stream().anyMatch(existing -> existing.name.equals(name))) {
                            throw new IllegalStateException("Already patched roster");
                        }
                        original.methods.add(incoming);
                    } else {
                        MethodNode outgoing = method(original, name);
                        if (!outgoing.desc.equals(incoming.desc)) throw new IllegalStateException("Method contract changed");
                        original.methods.set(original.methods.indexOf(outgoing), incoming);
                    }
                }
                if (entry.getKey().equals(RULES)) {
                    original.fields.removeIf(field -> FIELDS.contains(field.name));
                    original.fields.addAll(replacement.fields.stream().filter(field -> FIELDS.contains(field.name)).toList());
                }
                ClassWriter writer = new ClassWriter(0);
                original.accept(writer);
                Path destination = output.resolve(entry.getKey() + ".class");
                Files.createDirectories(destination.getParent());
                Files.write(destination, writer.toByteArray());
            }
        }
        System.out.println("PASS: patched only five player-weapon classes, preserving the deployed baseline");
    }

    private static void verify(Path baseline, Path candidate, Path classes, Path mappings) throws Exception {
        var names = new ZicaiVoidPatch.Names(mappings);
        Set<String> expected = new TreeSet<>();
        for (String name : METHODS.keySet()) expected.add(name + ".class");
        expected.add("assets/ftboss/lang/zh_cn.json");
        expected.add("assets/ftboss/lang/en_us.json");
        Set<String> changed = new TreeSet<>();
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
                if (prior == null) throw new AssertionError("Unexpected new JAR entry: " + entry.getName());
                if (!Arrays.equals(prior, after.getInputStream(entry).readAllBytes())) changed.add(entry.getName());
                else preserved++;
            }
            if (!originals.isEmpty()) throw new AssertionError("Lost JAR entries: " + originals.keySet());
            if (!changed.equals(expected)) throw new AssertionError("Unexpected JAR delta: " + changed);
            for (var entry : METHODS.entrySet()) {
                String file = entry.getKey() + ".class";
                ClassNode original = read(before.getInputStream(before.getEntry(file)).readAllBytes());
                ClassNode updated = read(after.getInputStream(after.getEntry(file)).readAllBytes());
                ClassNode source = compiled(classes, entry.getKey(), names);
                int added = entry.getValue().contains("createKeys") ? 1 : 0;
                if (updated.methods.size() != original.methods.size() + added) throw new AssertionError("Method inventory changed: " + file);
                for (MethodNode prior : original.methods) {
                    if (entry.getValue().contains(prior.name)) continue;
                    if (!Arrays.equals(fingerprint(prior), fingerprint(method(updated, prior.name)))) {
                        throw new AssertionError("Unrelated method changed: " + entry.getKey() + "." + prior.name);
                    }
                }
                for (String name : entry.getValue()) {
                    if (!Arrays.equals(fingerprint(method(updated, name)), fingerprint(method(source, name)))) {
                        throw new AssertionError("Runtime differs from tested source: " + entry.getKey() + "." + name);
                    }
                }
                for (var field : original.fields) {
                    if (entry.getKey().equals(RULES) && FIELDS.contains(field.name)) continue;
                    var preservedField = updated.fields.stream().filter(value -> value.name.equals(field.name)
                            && value.desc.equals(field.desc) && value.access == field.access
                            && java.util.Objects.equals(value.value, field.value)).findFirst();
                    if (preservedField.isEmpty()) throw new AssertionError("Unrelated field changed: " + field.name);
                }
                if (entry.getKey().equals(RULES)) {
                    for (String name : FIELDS) {
                        var actual = updated.fields.stream().filter(field -> field.name.equals(name)).findFirst().orElseThrow();
                        var declared = source.fields.stream().filter(field -> field.name.equals(name)).findFirst().orElseThrow();
                        if (!java.util.Objects.equals(actual.value, declared.value)) throw new AssertionError("Wrong rule constant: " + name);
                    }
                }
            }
        }
        System.out.println("PASS: " + preserved + " JAR entries byte-identical; only seven expected entries changed; all target methods match tested source");
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments[0].equals("patch")) {
            patch(Path.of(arguments[1]), Path.of(arguments[2]), Path.of(arguments[3]), Path.of(arguments[4]));
        } else if (arguments[0].equals("verify")) {
            verify(Path.of(arguments[1]), Path.of(arguments[2]), Path.of(arguments[3]), Path.of(arguments[4]));
        } else throw new IllegalArgumentException("Expected patch or verify");
    }
}
