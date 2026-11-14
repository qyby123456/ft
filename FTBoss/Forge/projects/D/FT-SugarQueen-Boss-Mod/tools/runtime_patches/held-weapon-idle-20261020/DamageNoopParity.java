/** 相同耐久空写入与真实修理分支的生产字节码差分，2026-10-20。 */
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.ZipFile;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class DamageNoopParity {
    public static final class Stack {
        final int damage; final int maximum;
        Stack(int damage, int maximum) { this.damage = damage; this.maximum = maximum; }
        public int damage() { return damage; }
        public int maximum() { return maximum; }
    }
    static boolean managedValue; static boolean repairValue; static int managedCalls;
    public static boolean managed(Stack stack) { managedCalls++; return managedValue; }
    public static boolean statueRepairWrite(Stack stack) { return repairValue; }

    public static void main(String[] arguments) throws Exception {
        Class<?> before = load(arguments[0]); Class<?> after = load(arguments[1]);
        Method original = before.getMethod("beforeDamageChange", Stack.class, int.class);
        Method patched = after.getMethod("beforeDamageChange", Stack.class, int.class);
        long cases = 0; long noops = 0;
        for (int current : new int[]{-3, -1, 0, 1, 5, 10, 50, Integer.MAX_VALUE})
            for (int maximum : new int[]{-3, 0, 1, 5, 10, 100, Integer.MAX_VALUE})
                for (int requested : new int[]{Integer.MIN_VALUE, -3, -1, 0, 1, 5, 10, 50, Integer.MAX_VALUE, current})
                    for (boolean internal : new boolean[]{false, true}) for (boolean managed : new boolean[]{false, true})
                        for (boolean repair : new boolean[]{false, true}) {
                            Stack stack = new Stack(current, maximum); managedValue = managed; repairValue = repair;
                            set(before, internal ? stack : new Stack(0, 0)); set(after, internal ? stack : new Stack(0, 0));
                            Object expected = outcome(original, stack, requested); managedCalls = 0;
                            Object actual = outcome(patched, stack, requested);
                            check(Objects.equals(expected, actual), "Changed damage result current=" + current + " requested=" + requested + " max=" + maximum + " repair=" + repair);
                            if (!internal && !repair && requested == current && current >= 0) {
                                check(managedCalls == 0, "Unchanged damage still validates eligibility"); noops++;
                            }
                            if (!internal && repair && current == requested && current >= 0) check(managedCalls == 1, "Repair context skipped original managed check");
                            cases++;
                        }
        for (boolean internal : new boolean[]{false, true}) for (boolean managed : new boolean[]{false, true}) for (boolean repair : new boolean[]{false, true}) {
            managedValue = managed; repairValue = repair;
            set(before, internal ? null : new Stack(0, 0)); set(after, internal ? null : new Stack(0, 0));
            check(Objects.equals(outcome(original, null, 1), outcome(patched, null, 1)), "Null stack behavior changed"); cases++;
        }
        System.out.println("PASS damageDifferentialCases=" + cases + " noopValidationSkipped=" + noops + " repairClampPreserved=true internalWritePreserved=true negativeAndExtremeDamage=true nullParity=true productionBytecode=true");
    }
    static Object outcome(Method method, Stack stack, int requested) throws Exception {
        try { return method.invoke(null, stack, requested); }
        catch (InvocationTargetException failure) { return failure.getCause().getClass().getName(); }
    }
    @SuppressWarnings("unchecked")
    static void set(Class<?> type, Object value) throws Exception { ((ThreadLocal<Object>) type.getField("INTERNAL_WRITE").get(null)).set(value); }
    static Class<?> load(String archive) throws Exception {
        ClassNode type = new ClassNode();
        try (ZipFile zip = new ZipFile(archive)) { new ClassReader(zip.getInputStream(zip.getEntry(PatchHeldIdle.ITEM + "FoodWeaponLifecycle.class")).readAllBytes()).accept(type, 0); }
        String originalName = type.name; type.name = "DamageNoopIsolated";
        type.methods.removeIf(method -> !method.name.equals("beforeDamageChange"));
        type.fields.removeIf(field -> !field.name.equals("INTERNAL_WRITE"));
        type.fields.forEach(field -> field.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
        type.visibleAnnotations = null; type.invisibleAnnotations = null;
        for (MethodNode method : type.methods) {
            method.desc = remap(method.desc);
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call) {
                    if (call.name.equals("managed") || call.name.equals("statueRepairWrite")) call.owner = "DamageNoopParity";
                    if (call.owner.equals("net/minecraft/world/item/ItemStack")) {
                        call.owner = "DamageNoopParity$Stack";
                        call.name = switch (call.name) { case "m_41773_" -> "damage"; case "m_41776_" -> "maximum"; default -> throw new AssertionError(call.name); };
                    }
                    call.desc = remap(call.desc);
                } else if (instruction instanceof FieldInsnNode field && field.owner.equals(originalName)) field.owner = type.name;
                else if (instruction instanceof FrameNode frame) {
                    if (frame.local != null) frame.local.replaceAll(value -> value instanceof String text ? remap(text) : value);
                    if (frame.stack != null) frame.stack.replaceAll(value -> value instanceof String text ? remap(text) : value);
                }
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS); type.accept(writer); byte[] bytes = writer.toByteArray();
        Class<?> isolated = new ClassLoader(DamageNoopParity.class.getClassLoader()) { Class<?> define() { return defineClass(type.name, bytes, 0, bytes.length); } }.define();
        isolated.getField("INTERNAL_WRITE").set(null, new ThreadLocal<>()); return isolated;
    }
    static String remap(String value) { return value.replace("net/minecraft/world/item/ItemStack", "DamageNoopParity$Stack"); }
    static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
