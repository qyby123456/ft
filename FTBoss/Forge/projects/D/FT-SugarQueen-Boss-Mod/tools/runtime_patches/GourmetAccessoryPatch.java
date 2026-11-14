import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.*;

public final class GourmetAccessoryPatch implements Opcodes {
    private static final String ITEM = "net/farmingtales/ftboss/item/";
    private static final String EFFECTS = ITEM + "GourmetAccessoryEffects";
    private static final String RULES = "net/farmingtales/ftboss/util/GourmetAccessoryRules";
    private static final String STACK = "Lnet/minecraft/world/item/ItemStack;";
    private static final String KEY = "tooltip.ftboss.gourmet.accessory_strength";
    private static final Set<String> CHANGED = Set.of(ITEM + "FoodWeaponEvolution", ITEM + "EquipmentWeaponBuild",
            ITEM + "weapons/GourmetBlankBladeItem");

    private static ClassNode read(byte[] bytes) {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, ClassReader.EXPAND_FRAMES);
        return type;
    }

    private static byte[] bytes(ClassNode type, int flags) {
        ClassWriter writer = new ClassWriter(flags);
        type.accept(writer);
        return writer.toByteArray();
    }

    private static void write(Path output, String name, byte[] bytes) throws Exception {
        Path file = output.resolve(name);
        Files.createDirectories(file.getParent());
        Files.write(file, bytes);
    }

    private static void patch(ClassNode type) {
        if (type.methods.stream().flatMap(method -> Arrays.stream(method.instructions.toArray()))
                .anyMatch(instruction -> instruction instanceof MethodInsnNode call && call.owner.equals(EFFECTS))) {
            throw new IllegalStateException("Already patched: " + type.name);
        }
        int hooks = 0;
        for (MethodNode method : type.methods) {
            boolean food = type.name.endsWith("/FoodWeaponEvolution") && method.name.equals("socketStats");
            boolean scale = type.name.endsWith("/EquipmentWeaponBuild") && method.name.equals("scale");
            boolean tooltip = type.name.endsWith("/GourmetBlankBladeItem") && method.desc.equals("(" + STACK
                    + "Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V");
            if (!food && !scale && !tooltip) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction.getOpcode() != (tooltip ? RETURN : ARETURN)) continue;
                InsnList code = new InsnList();
                if (food) {
                    code.add(new InsnNode(DUP));
                    code.add(new FieldInsnNode(GETFIELD, ITEM + "FoodWeaponEvolution$SocketStats", "powers", "[D"));
                    code.add(new VarInsnNode(ALOAD, 0));
                    code.add(new MethodInsnNode(INVOKESTATIC, EFFECTS, "scalePowers", "([D" + STACK + ")V", false));
                } else if (scale) {
                    String result = method.desc.substring(method.desc.indexOf(')') + 1);
                    code.add(new VarInsnNode(ALOAD, 0));
                    code.add(new MethodInsnNode(INVOKESTATIC, EFFECTS, "scale", "(" + result + STACK + ")" + result, false));
                } else {
                    code.add(new VarInsnNode(ALOAD, 3));
                    code.add(new MethodInsnNode(INVOKESTATIC, EFFECTS, "tooltip", "(Ljava/util/List;)V", false));
                }
                method.instructions.insertBefore(instruction, code);
                hooks++;
            }
        }
        int expected = type.name.endsWith("/EquipmentWeaponBuild") ? 3 : 1;
        if (hooks != expected) throw new IllegalStateException("Unexpected settlement exits: " + type.name + " " + hooks);
    }

    private static String languageLine(Path project, String locale) throws Exception {
        return Files.readAllLines(project.resolve("src/main/resources/assets/ftboss/lang/" + locale + ".json"))
                .stream().filter(line -> line.contains("\"" + KEY + "\"")).findFirst().orElseThrow();
    }

    private static void check(Path original, Path candidate, Path project) throws Exception {
        Set<String> additions = Set.of(EFFECTS + ".class", RULES + ".class");
        int changed = 0;
        int preserved = 0;
        try (ZipFile before = new ZipFile(original.toFile()); ZipFile after = new ZipFile(candidate.toFile())) {
            var entries = after.entries();
            Set<String> seen = new java.util.HashSet<>();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (!seen.add(entry.getName())) throw new AssertionError("Duplicate entry: " + entry.getName());
                var old = before.getEntry(entry.getName());
                if (old == null) {
                    if (!additions.contains(entry.getName())) throw new AssertionError("Unexpected addition: " + entry.getName());
                    continue;
                }
                byte[] expected = before.getInputStream(old).readAllBytes();
                byte[] actual = after.getInputStream(entry).readAllBytes();
                if (Arrays.equals(expected, actual)) { preserved++; continue; }
                changed++;
                String name = entry.getName().replaceFirst("\\.class$", "");
                if (CHANGED.contains(name)) {
                    ClassNode patched = read(expected);
                    patch(patched);
                    if (!Arrays.equals(bytes(read(bytes(patched, ClassWriter.COMPUTE_MAXS)), 0), bytes(read(actual), 0))) {
                        throw new AssertionError("Unexpected bytecode changes: " + name);
                    }
                } else if (entry.getName().startsWith("assets/ftboss/lang/")) {
                    String locale = entry.getName().contains("zh_cn") ? "zh_cn" : "en_us";
                    String text = new String(expected, StandardCharsets.UTF_8);
                    int opening = text.indexOf('{') + 1;
                    byte[] patched = (text.substring(0, opening) + "\n" + languageLine(project, locale)
                            + text.substring(opening)).getBytes(StandardCharsets.UTF_8);
                    if (!Arrays.equals(patched, actual)) throw new AssertionError("Unrelated language changes");
                } else throw new AssertionError("Unrelated changed entry: " + entry.getName());
            }
            if (changed != 5 || after.size() != before.size() + 2
                    || !seen.containsAll(additions)) throw new AssertionError("Unexpected JAR delta");
        }
        System.out.println("PASS exact JAR delta: 3 classes + 2 language keys, 2 helpers; " + preserved + " entries preserved byte-for-byte");
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments[0].equals("check")) { check(Path.of(arguments[1]), Path.of(arguments[2]), Path.of(arguments[3])); return; }
        Path classes = Path.of(arguments[2]);
        Path output = Path.of(arguments[4]);
        Path project = Path.of(arguments[5]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[3]));
        try (ZipFile baseline = new ZipFile(arguments[1])) {
            for (String name : CHANGED) {
                ClassNode type = read(baseline.getInputStream(baseline.getEntry(name + ".class")).readAllBytes());
                patch(type);
                write(output, name + ".class", bytes(type, ClassWriter.COMPUTE_MAXS));
            }
            for (String name : List.of(EFFECTS, RULES)) {
                ClassNode type = new ClassNode();
                new ClassReader(Files.readAllBytes(classes.resolve(name + ".class"))).accept(new ClassRemapper(type, names), 0);
                write(output, name + ".class", bytes(type, 0));
            }
            for (String locale : List.of("zh_cn", "en_us")) {
                String entry = "assets/ftboss/lang/" + locale + ".json";
                String text = new String(baseline.getInputStream(baseline.getEntry(entry)).readAllBytes(), StandardCharsets.UTF_8);
                if (text.contains("\"" + KEY + "\"")) throw new IllegalStateException("Language already patched");
                int opening = text.indexOf('{') + 1;
                write(output, entry, (text.substring(0, opening) + "\n" + languageLine(project, locale)
                        + text.substring(opening)).getBytes(StandardCharsets.UTF_8));
            }
        }
        System.out.println("PASS: gourmet power, build, rarity and tooltip hooks; no counters or evolution values rewritten");
    }
}
