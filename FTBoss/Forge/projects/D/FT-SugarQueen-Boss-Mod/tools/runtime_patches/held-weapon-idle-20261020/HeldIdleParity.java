/** 手持武器空路径性能补丁的移植或回归工具，2026-10-20。 */
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.ZipFile;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class HeldIdleParity {
    static int fish;
    static int wine;
    static int countCalls;
    static boolean binding;
    static boolean origin;
    static boolean eligible;
    static boolean server;
    static int eligibleCalls;
    static int originalCalls;
    static final Map<Object, Object> bindings = new IdentityHashMap<>();

    public static int count(Object stack, Object type) { countCalls++; return type.toString().equals("FISH") ? fish : wine; }
    public static boolean noBinding(Object stack) { return !binding; }
    public static Object wearOrigin(Object stack) { return origin ? new Object() : null; }
    public static boolean eligible(Object stack) { eligibleCalls++; return eligible; }
    public static boolean originalCaller(int kind, String caller) { originalCalls++; return false; }
    public static Object server() { return server ? new Object() : null; }

    public static void main(String[] arguments) throws Exception {
        Method beforeFlight = method(load(arguments[0], "FoodSocketPowers", "flight"), "flight");
        Method afterFlight = method(load(arguments[1], "FoodSocketPowers", "flight"), "flight");
        int flightCases = 0;
        for (int fishCount = 0; fishCount <= 8; fishCount++) for (int wineCount = 0; wineCount <= 8; wineCount++) {
            fish = fishCount; wine = wineCount;
            Object expected = beforeFlight.invoke(null, new Object[]{null});
            countCalls = 0;
            Object actual = afterFlight.invoke(null, new Object[]{null});
            check(expected.equals(actual), "Flight result changed");
            check(countCalls == (fish < 2 ? 1 : 2), "Unneeded second count");
            flightCases++;
        }
        Method beforeEntry = method(load(arguments[0], "OwnerStatueWearAdmission", "entered"), "entered");
        Method afterEntry = method(load(arguments[1], "OwnerStatueWearAdmission", "entered"), "entered");
        int admissionCases = 0;
        for (boolean hasBinding : new boolean[]{false, true}) for (boolean hasOrigin : new boolean[]{false, true})
                for (boolean nativeEligible : new boolean[]{false, true}) {
            binding = hasBinding; origin = hasOrigin; eligible = nativeEligible;
            Object expected = beforeEntry.invoke(null, null, null, 9, 0);
            eligibleCalls = 0; originalCalls = 0;
            Object actual = afterEntry.invoke(null, null, null, 9, 0);
            check(expected == null && actual == null, "Absence filter granted admission");
            if (!binding && !origin) check(eligibleCalls == 0 && originalCalls == 0, "Absent context still validated");
            else check(eligibleCalls == 1, "Existing context skipped original eligibility");
            admissionCases++;
        }
        Method settlement = method(load(arguments[1], "NativeFoodWeaponWear", "settleDamage"), "settleDamage");
        server = false; eligible = false; eligibleCalls = 0;
        settlement.invoke(null, new Object[]{null});
        check(eligibleCalls == 0, "Client queried server-only eligibility");
        server = true; eligible = false; eligibleCalls = 0;
        settlement.invoke(null, new Object[]{null});
        check(eligibleCalls == 1, "Server skipped eligibility");
        Class<?> continuation = load(arguments[1], "OwnerRepairContinuation", "noBinding");
        Field table = continuation.getDeclaredField("BINDINGS"); table.setAccessible(true); table.set(null, bindings);
        Method noBinding = method(continuation, "noBinding");
        check((boolean) noBinding.invoke(null, new Object[]{null}), "Empty map not absent");
        bindings.put(null, new Object());
        check(!(boolean) noBinding.invoke(null, new Object[]{null}), "Existing binding marked absent");
        System.out.println("PASS flightCases=" + flightCases + " admissionDenialCases=" + admissionCases
                + " clientSettlementNoop=true serverEligibilityPreserved=true bindingAbsenceProbe=true jvmVerify=all");
    }

    static Class<?> load(String path, String name, String retained) throws Exception {
        byte[] bytes;
        try (ZipFile archive = new ZipFile(path)) { bytes = archive.getInputStream(archive.getEntry(PatchHeldIdle.ITEM + name + ".class")).readAllBytes(); }
        ClassNode type = new ClassNode(); new ClassReader(bytes).accept(type, 0);
        type.methods.removeIf(method -> !method.name.equals(retained));
        type.fields.removeIf(field -> !field.name.equals("BINDINGS"));
        type.fields.forEach(field -> field.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
        type.visibleAnnotations = null; type.invisibleAnnotations = null;
        for (MethodNode method : type.methods) {
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode call)) continue;
                String signature = null;
                if (name.equals("FoodSocketPowers") && call.owner.equals(type.name) && call.name.equals("count")) signature = "(Ljava/lang/Object;Ljava/lang/Object;)I";
                if (name.equals("OwnerStatueWearAdmission")) {
                    if (call.name.equals("noBinding")) signature = "(Ljava/lang/Object;)Z";
                    if (call.name.equals("wearOrigin")) signature = "(Ljava/lang/Object;)Ljava/lang/Object;";
                    if (call.owner.equals(PatchHeldIdle.ITEM + "NativeFoodWeaponWear") && call.name.equals("eligible")) signature = "(Ljava/lang/Object;)Z";
                    if (call.owner.equals(type.name) && call.name.equals("originalCaller")) signature = "(ILjava/lang/String;)Z";
                }
                if (name.equals("NativeFoodWeaponWear")) {
                    if (call.owner.equals(type.name) && call.name.equals("server")) signature = "()Ljava/lang/Object;";
                    if (call.owner.equals(type.name) && call.name.equals("eligible")) signature = "(Ljava/lang/Object;)Z";
                }
                if (signature != null) {
                    if (call.name.equals("wearOrigin") && call.getNext().getOpcode() == Opcodes.ASTORE) {
                        method.instructions.insert(call, new TypeInsnNode(Opcodes.CHECKCAST, PatchHeldIdle.ITEM + "OwnerStatueWearAdmission$Origin"));
                    }
                    call.owner = "HeldIdleParity"; call.desc = signature;
                }
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); type.accept(writer);
        byte[] modified = writer.toByteArray();
        return new ClassLoader(HeldIdleParity.class.getClassLoader()) {
            Class<?> define() { return defineClass(type.name.replace('/', '.'), modified, 0, modified.length); }
        }.define();
    }
    static Method method(Class<?> type, String name) {
        Method result = Arrays.stream(type.getDeclaredMethods()).filter(method -> method.getName().equals(name)).findFirst().orElseThrow();
        result.setAccessible(true); return result;
    }
    static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
