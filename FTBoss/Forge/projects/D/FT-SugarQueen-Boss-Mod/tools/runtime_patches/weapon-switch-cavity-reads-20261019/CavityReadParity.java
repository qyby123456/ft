import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipFile;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordCavityRules.State;
import net.farmingtales.ftboss.util.SwordFunctionRules;

public final class CavityReadParity {
    public static List<Cell> source;
    public static SwordFunctionRules.Profile profile;
    public static boolean applicable = true;
    public static int level;
    public static int profiles;
    public static int levels;
    public static List<SwordCavityRules.Link> activeLinks = List.of();
    public static List<Cell> cells(Object stack) { return source; }
    public static boolean applies(Object stack) { return applicable; }
    public static SwordFunctionRules.Profile functionProfile(Object stack) { profiles++; return profile; }
    public static int evolutionLevel(Object stack) { levels++; return level; }
    public static List<SwordCavityRules.Link> links(Object stack) { return activeLinks; }
    public static void main(String[] arguments) throws Exception {
        Class<?> before = load(arguments[0]);
        Class<?> after = load(arguments[1]);
        Method oldRead = method(before, "effectiveCells");
        Method newRead = method(after, "effectiveCells");
        Method oldLegacy = method(before, "legacyBuild");
        Method newLegacy = method(after, "legacyBuild");
        List<SwordFunctionRules.Profile> allProfiles = new ArrayList<>();
        allProfiles.add(null);
        allProfiles.add(SwordFunctionRules.iron());
        Random random = new Random(19061019L);
        for (int sample = 0; sample < 40; sample++) {
            List<Integer> unlocks = new ArrayList<>();
            for (int index = 0; index < SwordCavityRules.CELL_COUNT; index++) unlocks.add(random.nextInt(6) - 1);
            SwordFunctionRules.Profile iron = SwordFunctionRules.iron();
            allProfiles.add(new SwordFunctionRules.Profile(iron.minimum(), iron.cellWeights(), unlocks, iron.materials(), iron.cellBaseValues(), iron.itemValues()));
        }
        int cases = 0;
        for (SwordFunctionRules.Profile candidate : allProfiles) for (boolean supported : new boolean[]{false, true})
                for (int evolution : new int[]{-100, -1, 0, 1, 2, 3, 4, 5, 100}) {
            profile = candidate;
            applicable = supported;
            level = evolution;
            List<Cell> input = new ArrayList<>();
            for (int index = 0; index < SwordCavityRules.CELL_COUNT; index++) {
                State state = cases % 3 == 0 ? State.NATIVE : cases % 3 == 1 ? State.INSTALLED : State.values()[random.nextInt(State.values().length)];
                input.add(new Cell(state, Material.values()[random.nextInt(Material.values().length)]));
            }
            source = List.copyOf(input);
            List<Cell> snapshot = List.copyOf(source);
            Object expected = oldRead.invoke(null, new Object[]{null});
            Object actual = newRead.invoke(null, new Object[]{null});
            check(expected.equals(actual), "Cell mismatch case " + cases);
            check(snapshot.equals(source), "Source list mutated");
            for (List<SwordCavityRules.Link> links : List.of(List.<SwordCavityRules.Link>of(), List.of(SwordCavityRules.Link.FROST_EDGE),
                    List.of(SwordCavityRules.Link.COPPER_CIRCUIT), List.of(SwordCavityRules.Link.FROST_EDGE, SwordCavityRules.Link.COPPER_CIRCUIT))) {
                activeLinks = links;
                check(oldLegacy.invoke(null, new Object[]{null}).equals(newLegacy.invoke(null, new Object[]{null})), "Legacy build mismatch " + cases);
            }
            cases++;
        }
        applicable = true;
        profile = SwordFunctionRules.iron();
        level = 1;
        source = Collections.nCopies(SwordCavityRules.CELL_COUNT, new Cell(State.NATIVE, Material.IRON));
        profiles = 0; levels = 0; oldRead.invoke(null, new Object[]{null});
        int oldProfiles = profiles; int oldLevels = levels;
        profiles = 0; levels = 0; newRead.invoke(null, new Object[]{null});
        check(oldProfiles == SwordCavityRules.CELL_COUNT && oldLevels == SwordCavityRules.CELL_COUNT, "Old count unexpected");
        check(profiles == 1 && levels == 1, "New operation did not deduplicate");
        int newProfiles = profiles; int newLevels = levels;
        profiles = 0; levels = 0; oldLegacy.invoke(null, new Object[]{null});
        int oldLegacyProfiles = profiles;
        profiles = 0; levels = 0; newLegacy.invoke(null, new Object[]{null});
        check(profiles == 1 && oldLegacyProfiles == 2 * SwordCavityRules.CELL_COUNT, "Legacy count unexpected");
        System.out.println("PASS cellCases=" + cases + " legacyBuildCases=" + (cases * 4) + " unchangedInput=true productionMethods=true jvmVerify=all effectiveProfiles="
                + oldProfiles + "->" + newProfiles + " effectiveLevels=" + oldLevels + "->" + newLevels + " legacyProfiles=" + oldLegacyProfiles + "->" + profiles);
    }
    static Method method(Class<?> type, String name) {
        Method result = Arrays.stream(type.getDeclaredMethods()).filter(candidate -> candidate.getName().equals(name)).findFirst().orElseThrow();
        result.setAccessible(true);
        return result;
    }
    static Class<?> load(String archivePath) throws Exception {
        byte[] original;
        try (ZipFile archive = new ZipFile(archivePath)) { original = archive.getInputStream(archive.getEntry(PatchCavityReads.TYPE + ".class")).readAllBytes(); }
        ClassNode type = new ClassNode();
        new ClassReader(original).accept(type, 0);
        Set<String> retained = Set.of("cells", "applies", "links", "effectiveCells", "legacyMaterials", "cellUnlocked", "legacyBuild", "legacyMaterialValues");
        type.methods.removeIf(method -> !retained.contains(method.name));
        type.fields.clear();
        type.visibleAnnotations = null;
        type.invisibleAnnotations = null;
        for (MethodNode method : type.methods) {
            if (method.name.equals("cells") || method.name.equals("applies") || method.name.equals("links")) {
                method.instructions.clear(); method.tryCatchBlocks.clear();
                method.localVariables = null;
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                String returned = Type.getReturnType(method.desc).getDescriptor();
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "CavityReadParity", method.name, "(Ljava/lang/Object;)" + returned, false));
                method.instructions.add(new InsnNode(returned.equals("Z") ? Opcodes.IRETURN : Opcodes.ARETURN));
            }
            for (AbstractInsnNode instruction : method.instructions) {
                if (!(instruction instanceof MethodInsnNode call)) continue;
                if (call.owner.equals("net/farmingtales/ftboss/api/SwordCavityExtensions") && call.name.equals("functionProfile")
                        || call.owner.equals("net/farmingtales/ftboss/item/FoodWeaponEvolution") && call.name.equals("evolutionLevel")) {
                    call.owner = "CavityReadParity";
                    call.desc = "(Ljava/lang/Object;)" + Type.getReturnType(call.desc).getDescriptor();
                }
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        byte[] bytes = writer.toByteArray();
        return new ClassLoader(CavityReadParity.class.getClassLoader()) {
            Class<?> define() { return defineClass(PatchCavityReads.TYPE.replace('/', '.'), bytes, 0, bytes.length); }
        }.define();
    }
    static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
