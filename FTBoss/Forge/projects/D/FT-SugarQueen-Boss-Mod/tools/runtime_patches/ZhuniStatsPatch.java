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
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class ZhuniStatsPatch {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static final Set<String> TYPES = Set.of("boss/ai/ManorCombatHalving", "boss/ai/ZhuniPairRules", "turret/ZhuniEntity");
    private static int assertions;

    private static void check(boolean valid, String message) {
        assertions++;
        if (!valid) throw new AssertionError(message);
    }

    private static ClassNode read(ZipFile archive, String suffix) throws Exception {
        ClassNode type = new ClassNode();
        try (var stream = archive.getInputStream(archive.getEntry(PREFIX + suffix + ".class"))) {
            new ClassReader(stream.readAllBytes()).accept(type, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        return type;
    }

    private static byte[] fingerprint(ClassNode type, MethodNode method) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(type.version, type.access, type.name, null, type.superName, null);
        method.accept(writer);
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static MethodNode method(ClassNode type, String name) {
        return type.methods.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
    }

    private static long calls(MethodNode method, String name) {
        long count = 0;
        for (var instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.name.equals(name)) count++;
        }
        return count;
    }

    private static boolean constant(MethodNode method, Number value) {
        for (var instruction : method.instructions) {
            if (instruction instanceof LdcInsnNode literal && literal.cst.equals(value)) return true;
        }
        return false;
    }

    private static boolean attribute(MethodNode method, String name) {
        for (var instruction : method.instructions) {
            if (instruction instanceof FieldInsnNode field && field.name.equals(name)
                    && field.owner.equals("net/minecraft/world/entity/ai/attributes/Attributes")) return true;
        }
        return false;
    }

    public static void main(String[] arguments) throws Exception {
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[3]));
        if (arguments[0].equals("payload")) {
            for (String suffix : TYPES) {
                String relative = PREFIX + suffix + ".class";
                ClassWriter writer = new ClassWriter(0);
                new ClassReader(Files.readAllBytes(Path.of(arguments[1]).resolve(relative)))
                        .accept(new ClassRemapper(writer, names), 0);
                Path destination = Path.of(arguments[2]).resolve(relative);
                Files.createDirectories(destination.getParent());
                Files.write(destination, writer.toByteArray());
            }
            System.out.println("PASS: remapped exactly three attribute-fix classes");
            return;
        }
        try (ZipFile before = new ZipFile(arguments[1]); ZipFile after = new ZipFile(arguments[2])) {
            Set<String> changed = new HashSet<>();
            for (var entry : after.stream().toList()) {
                var original = before.getEntry(entry.getName());
                if (original == null || !Arrays.equals(after.getInputStream(entry).readAllBytes(),
                        before.getInputStream(original).readAllBytes())) changed.add(entry.getName());
            }
            for (var entry : before.stream().toList()) check(after.getEntry(entry.getName()) != null, "Baseline entry retained");
            Set<String> intended = new HashSet<>();
            for (String suffix : TYPES) intended.add(PREFIX + suffix + ".class");
            check(changed.equals(intended), "Exactly three changed classes: " + changed);
            String load = names.mapMethodName(PREFIX + "turret/ZhuniEntity", "readAdditionalSaveData", "(Lnet/minecraft/nbt/CompoundTag;)V");
            Map<String, Set<String>> permitted = Map.of("boss/ai/ManorCombatHalving", Set.of("normalize"),
                    "turret/ZhuniEntity", Set.of("attributes", load));
            for (var scope : permitted.entrySet()) {
                ClassNode original = read(before, scope.getKey());
                ClassNode candidate = read(after, scope.getKey());
                check(original.methods.size() == candidate.methods.size(), "Method count retained");
                for (MethodNode previous : original.methods) {
                    if (scope.getValue().contains(previous.name)) continue;
                    MethodNode current = candidate.methods.stream().filter(item -> item.name.equals(previous.name)
                            && item.desc.equals(previous.desc)).findFirst().orElseThrow();
                    check(Arrays.equals(fingerprint(original, previous), fingerprint(candidate, current)),
                            "Unrelated method retained: " + scope.getKey() + "." + previous.name);
                }
            }
            ClassNode rules = read(after, "boss/ai/ZhuniPairRules");
            check(rules.fields.stream().anyMatch(field -> field.name.equals("HEALTH") && Float.valueOf(2000).equals(field.value)), "2000 health rule");
            check(rules.fields.stream().anyMatch(field -> field.name.equals("BIRD_ARMOR") && Double.valueOf(55).equals(field.value)), "55 armor rule");
            ClassNode bird = read(after, "turret/ZhuniEntity");
            String armor = names.mapFieldName("net/minecraft/world/entity/ai/attributes/Attributes", "ARMOR", "Lnet/minecraft/world/entity/ai/attributes/Attribute;");
            MethodNode attributes = method(bird, "attributes");
            check(attribute(attributes, armor) && constant(attributes, 55.0D) && constant(attributes, 2000.0D), "Spawn attributes: 2000 HP and 55 armor");
            MethodNode reload = method(bird, load);
            check(attribute(reload, armor) && constant(reload, 55.0D) && constant(reload, 2000.0D), "Reload repairs old health and armor");
            check(calls(reload, names.mapMethodName("net/minecraft/nbt/CompoundTag", "getBoolean", "(Ljava/lang/String;)Z")) == 0, "Legacy marker cannot skip repair");
            MethodNode normalize = method(read(after, "boss/ai/ManorCombatHalving"), "normalize");
            check(calls(normalize, "paired") == 1 && calls(normalize, "xuanlanDefeated") == 1, "Encounter scope and defeated anchor preserved");
            check(calls(normalize, names.mapMethodName("net/minecraft/world/entity/ai/attributes/AttributeInstance", "removeModifier", "(Ljava/util/UUID;)V")) == 1,
                    "Old health-halving modifier removed");
            for (String suffix : TYPES) {
                ClassNode type = read(after, suffix);
                for (MethodNode member : type.methods) new Analyzer<>(new BasicVerifier()).analyze(type.name, member);
            }
        }
        System.out.println("PASS: " + assertions + " runtime attribute, migration, preservation and JVM stack checks");
    }
}
