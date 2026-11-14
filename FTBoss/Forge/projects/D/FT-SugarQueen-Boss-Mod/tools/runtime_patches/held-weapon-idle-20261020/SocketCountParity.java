/** 手持武器空路径性能补丁的移植或回归工具，2026-10-20。 */
import java.lang.reflect.Method;
import java.util.*;
import java.util.zip.ZipFile;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class SocketCountParity {
    public enum Kind { FISH, WINE, OTHER }
    public enum Category { FOOD, OTHER }
    public record Stack(int count, Kind kind, boolean empty, boolean food) {
        public boolean m_41619_() { return empty; }
        public int m_41613_() { return count; }
        public Tag m_41737_(String key) { return null; }
    }
    public record Tag(boolean empty) { public boolean m_128456_() { return empty; } }
    public record Profile(Set<Integer> unlocked) {}
    public record Status(boolean ready) {}
    public record Entry(boolean paid, Stack storedItem) {}
    public record View(boolean valid, boolean persisted, int level, Status status, Profile profile, Map<Integer, Entry> entries) {}
    public record Definition(Set<Category> categories) {}
    public record Stats(int value) { public int count(Kind kind) { return value; } }
    static View view;
    static boolean applies = true;
    static boolean blocks;
    static boolean broken;
    static boolean emptyFingerprint;
    static int validationCalls;
    public static boolean applies(Stack stack) { return applies; }
    public static View read(Stack stack) { return view; }
    public static boolean blocksUse(Stack stack) { validationCalls++; return blocks; }
    public static boolean isBroken(Stack stack) { validationCalls++; return broken; }
    public static Tag foundationSnapshot(Stack stack) { validationCalls++; return new Tag(emptyFingerprint); }
    public static boolean unlocked(Profile profile, int index, int level) { return profile.unlocked.contains(index); }
    public static Optional<Definition> resolve(Stack stack) { return Optional.of(new Definition(stack.food ? Set.of(Category.FOOD) : Set.of(Category.OTHER))); }
    public static Kind foodType(Stack stack) { return stack.kind; }
    public static Stats socketStats(Stack stack) { return new Stats(3); }

    public static void main(String[] arguments) throws Exception {
        Method before = load(arguments[0]); Method after = load(arguments[1]);
        int cases = 0; int zeroFastPaths = 0; int positivePaths = 0;
        Random random = new Random(20261020);
        for (int iteration = 0; iteration < 600; iteration++) {
            Map<Integer, Entry> entries = new LinkedHashMap<>(); Set<Integer> unlocked = new HashSet<>();
            for (int index = 0; index < 8; index++) {
                entries.put(index, new Entry(random.nextBoolean(), new Stack(random.nextInt(3), Kind.values()[random.nextInt(3)], random.nextInt(8) == 0, random.nextBoolean())));
                if (random.nextBoolean()) unlocked.add(index);
            }
            for (int invalid = 0; invalid <= 9; invalid++) {
                applies = true; blocks = invalid == 1; broken = invalid == 2; emptyFingerprint = invalid == 3;
                view = new View(invalid != 4, invalid != 5, invalid == 6 ? -1 : invalid == 7 ? 5 : 1,
                        new Status(invalid != 8), new Profile(Set.copyOf(unlocked)), Map.copyOf(entries));
                Stack weapon = new Stack(invalid == 9 ? 2 : 1, Kind.OTHER, false, false);
                for (Kind kind : Kind.values()) {
                    int expected = (int) before.invoke(null, weapon, kind);
                    validationCalls = 0;
                    int actual = (int) after.invoke(null, weapon, kind);
                    check(actual == expected, "Count mismatch at " + iteration + "/" + invalid + "/" + kind + " expected=" + expected + " actual=" + actual + " view=" + view);
                    if (actual > 0) { check(validationCalls == 3, "Positive count skipped validation"); positivePaths++; }
                    else if (validationCalls == 0) zeroFastPaths++;
                    cases++;
                }
            }
        }
        for (boolean damage : new boolean[]{false, true}) {
            applies = false; broken = damage;
            Stack weapon = new Stack(1, Kind.OTHER, false, false);
            check(before.invoke(null, weapon, Kind.FISH).equals(after.invoke(null, weapon, Kind.FISH)), "Legacy count changed");
        }
        check((int) after.invoke(null, null, Kind.FISH) == 0, "Null weapon accepted");
        check((int) after.invoke(null, new Stack(1, Kind.OTHER, false, false), null) == 0, "Null type accepted");
        System.out.println("PASS differentialCases=" + cases + " deniedOrZeroNoValidation=" + zeroFastPaths + " positiveFullValidation=" + positivePaths
                + " legacyParity=true nullInputs=true productionBytecode=true jvmVerify=all");
    }

    static Method load(String path) throws Exception {
        byte[] bytes;
        try (ZipFile archive = new ZipFile(path)) { bytes = archive.getInputStream(archive.getEntry(PatchHeldIdle.ITEM + "FoodWeaponEvolution.class")).readAllBytes(); }
        ClassNode type = new ClassNode(); new ClassReader(bytes).accept(type, 0);
        type.methods.removeIf(method -> !method.name.equals("socketTypeCount")); type.fields.clear(); type.visibleAnnotations = null; type.invisibleAnnotations = null;
        type.name = "SocketCountIsolated";
        MethodNode method = type.methods.get(0); method.desc = remap(method.desc); method.localVariables = null;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call) {
                String owner = call.owner;
                call.owner = remap(owner); call.desc = remap(call.desc);
                if (owner.startsWith(PatchHeldIdle.ITEM) && !call.owner.startsWith("SocketCountParity$")
                        || owner.equals("net/farmingtales/ftboss/util/foundations/FourWeaponFoundationRules")
                        || owner.equals("net/farmingtales/ftboss/api/SwordMaterialExtensions")) call.owner = "SocketCountParity";
            } else if (instruction instanceof FieldInsnNode field) { field.owner = remap(field.owner); field.desc = remap(field.desc); }
            else if (instruction instanceof TypeInsnNode cast) cast.desc = remap(cast.desc);
            else if (instruction instanceof FrameNode frame) {
                if (frame.local != null) frame.local.replaceAll(value -> value instanceof String text ? remap(text) : value);
                if (frame.stack != null) frame.stack.replaceAll(value -> value instanceof String text ? remap(text) : value);
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); type.accept(writer); byte[] changed = writer.toByteArray();
        Class<?> isolated = new ClassLoader(SocketCountParity.class.getClassLoader()) {
            Class<?> define() { return defineClass("SocketCountIsolated", changed, 0, changed.length); }
        }.define();
        return isolated.getMethod("socketTypeCount", Stack.class, Kind.class);
    }
    static String remap(String value) {
        Map<String, String> mappings = Map.ofEntries(
                Map.entry("net/minecraft/world/item/ItemStack", "Stack"), Map.entry("net/minecraft/nbt/CompoundTag", "Tag"),
                Map.entry(PatchHeldIdle.ITEM + "FoodWeaponEvolution$FoodType", "Kind"), Map.entry(PatchHeldIdle.ITEM + "FoodWeaponEvolution$SocketStats", "Stats"),
                Map.entry(PatchHeldIdle.ITEM + "FourWeaponFoundationAdapter$View", "View"), Map.entry(PatchHeldIdle.ITEM + "FourWeaponFoundationAdapter$Entry", "Entry"),
                Map.entry("net/farmingtales/ftboss/util/foundations/FourWeaponFoundationRules$Status", "Status"),
                Map.entry("net/farmingtales/ftboss/util/foundations/FourWeaponFoundationRules$Profile", "Profile"),
                Map.entry("net/farmingtales/ftboss/api/SwordMaterialDefinition$Category", "Category"),
                Map.entry("net/farmingtales/ftboss/api/SwordMaterialDefinition", "Definition"));
        for (Map.Entry<String, String> entry : mappings.entrySet().stream().sorted(Comparator.comparingInt((Map.Entry<String, String> entry) -> entry.getKey().length()).reversed()).toList()) {
            value = value.replace(entry.getKey(), "SocketCountParity$" + entry.getValue());
        }
        return value;
    }
    static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
