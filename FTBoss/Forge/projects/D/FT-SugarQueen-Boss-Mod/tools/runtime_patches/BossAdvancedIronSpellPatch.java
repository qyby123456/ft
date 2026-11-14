import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

/** 按现行 JAR 定点移植高级铁魔法入口，避免全量构建回退其它功能。 */
public final class BossAdvancedIronSpellPatch {
    private static final String ROOT = "net/farmingtales/ftboss/";
    private static final Map<String, Set<String>> METHODS = Map.of(
            ROOT + "integ/WizardSummon", Set.of("<clinit>", "castByName", "castDirect", "castAt", "spellPoolRare", "rarityOf", "resolve"),
            ROOT + "boss/IronSpellBridge", Set.of("cast"),
            ROOT + "integ/spells/LemonSpellBarrage", Set.of("rank"));
    private static final Set<String> ADDED = Set.of(ROOT + "integ/spells/BossIronSpellLevels", ROOT + "mixin/BossAdvancedIronSpellMixin");

    private static ClassNode read(byte[] bytes) {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, 0);
        return type;
    }

    private static byte[] entry(ZipFile archive, String name) throws Exception {
        try (var stream = archive.getInputStream(archive.getEntry(name))) { return stream.readAllBytes(); }
    }

    private static byte[] methodBytes(MethodNode method) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(61, 1, "MethodComparison", null, "java/lang/Object", null);
        method.accept(writer);
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static void save(ClassNode type, Path payload) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        byte[] bytes = writer.toByteArray();
        ClassNode verified = read(bytes);
        for (MethodNode method : verified.methods) {
            if ((method.access & 0x500) == 0) new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
        }
        Path destination = payload.resolve(type.name + ".class");
        Files.createDirectories(destination.getParent());
        Files.write(destination, bytes);
    }

    private static void patch(String[] arguments) throws Exception {
        Path compiled = Path.of(arguments[2]);
        Path payload = Path.of(arguments[4]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[3]));
        try (ZipFile baseline = new ZipFile(arguments[1])) {
            Set<String> production = new HashSet<>(METHODS.keySet());
            production.addAll(ADDED);
            for (String name : production) {
                ClassNode donor = new ClassNode();
                new ClassReader(Files.readAllBytes(compiled.resolve(name + ".class"))).accept(new ClassRemapper(donor, names), 0);
                if (ADDED.contains(name)) {
                    if (baseline.getEntry(name + ".class") != null) throw new IllegalStateException("Already installed: " + name);
                    save(donor, payload);
                    continue;
                }
                ClassNode original = read(entry(baseline, name + ".class"));
                Set<String> replaced = new HashSet<>();
                for (MethodNode method : donor.methods) {
                    if (!METHODS.get(name).contains(method.name)) continue;
                    int index = -1;
                    for (int position = 0; position < original.methods.size(); position++) {
                        MethodNode before = original.methods.get(position);
                        if (before.name.equals(method.name) && before.desc.equals(method.desc)) index = position;
                    }
                    if (index < 0) throw new IllegalStateException("Runtime method mismatch: " + name + "." + method.name);
                    original.methods.set(index, method);
                    replaced.add(method.name);
                }
                if (!replaced.equals(METHODS.get(name))) throw new IllegalStateException("Missing method transplants: " + name);
                save(original, payload);
            }
        }
        System.out.println("PASS targeted transplant: 9 existing methods and 2 new classes; runtime fields and other methods retained");
    }

    private static void verify(String[] arguments) throws Exception {
        try (ZipFile before = new ZipFile(arguments[1]); ZipFile after = new ZipFile(arguments[2])) {
            Set<String> expectedAdded = new HashSet<>();
            for (String name : ADDED) expectedAdded.add(name + ".class");
            Set<String> beforeNames = new HashSet<>();
            before.stream().forEach(item -> beforeNames.add(item.getName()));
            Set<String> afterNames = new HashSet<>();
            after.stream().forEach(item -> {
                if (!afterNames.add(item.getName())) throw new IllegalStateException("Duplicate ZIP entry: " + item.getName());
            });
            Set<String> expected = new HashSet<>(beforeNames);
            expected.addAll(expectedAdded);
            if (!expected.equals(afterNames)) throw new IllegalStateException("Unexpected archive entry changes");
            int preserved = 0;
            for (String name : beforeNames) {
                if (name.equals("ftboss.mixins.json")) continue;
                String owner = name.endsWith(".class") ? name.substring(0, name.length() - 6) : "";
                byte[] original = entry(before, name);
                byte[] updated = entry(after, name);
                if (!METHODS.containsKey(owner)) {
                    if (!Arrays.equals(original, updated)) throw new IllegalStateException("Unrelated entry changed: " + name);
                    preserved++;
                    continue;
                }
                ClassNode oldType = read(original);
                ClassNode newType = read(updated);
                if (oldType.methods.size() != newType.methods.size() || oldType.fields.size() != newType.fields.size())
                    throw new IllegalStateException("Runtime members changed: " + owner);
                for (MethodNode method : oldType.methods) {
                    if (METHODS.get(owner).contains(method.name)) continue;
                    MethodNode current = newType.methods.stream().filter(item -> item.name.equals(method.name) && item.desc.equals(method.desc))
                            .findFirst().orElseThrow();
                    if (!Arrays.equals(methodBytes(method), methodBytes(current)))
                        throw new IllegalStateException("Unrelated method changed: " + owner + "." + method.name);
                }
            }
            String levels = ROOT + "integ/spells/BossIronSpellLevels";
            for (var target : METHODS.entrySet()) {
                ClassNode type = read(entry(after, target.getKey() + ".class"));
                for (MethodNode method : type.methods) {
                    if (!target.getValue().contains(method.name) || Set.of("<clinit>", "resolve").contains(method.name)) continue;
                    boolean hooked = false;
                    for (var instruction : method.instructions) {
                        if (instruction instanceof MethodInsnNode call && call.owner.equals(levels)) hooked = true;
                    }
                    if (!hooked) throw new IllegalStateException("Level policy missing from " + method.name);
                }
            }
            System.out.println("PASS JAR scope: " + preserved + " existing entries byte-for-byte preserved; unrelated methods unchanged");
        }
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments[0].equals("patch")) patch(arguments);
        else if (arguments[0].equals("verify")) verify(arguments);
        else throw new IllegalArgumentException(arguments[0]);
    }
}
