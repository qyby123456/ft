/** 手持武器空路径性能补丁的移植或回归工具，2026-10-20。 */
import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.*;

public final class VerifyHeldIdle {
    public static void main(String[] arguments) throws Exception {
        int identical = 0;
        int changed = 0;
        int identicalMethods = 0;
        try (ZipFile before = new ZipFile(arguments[0]); ZipFile after = new ZipFile(arguments[1])) {
            check(before.size() == after.size(), "Entry inventory changed");
            for (ZipEntry entry : Collections.list(before.entries())) {
                ZipEntry next = after.getEntry(entry.getName());
                check(next != null, "Missing " + entry.getName());
                byte[] oldBytes = before.getInputStream(entry).readAllBytes();
                byte[] newBytes = after.getInputStream(next).readAllBytes();
                if (Arrays.equals(oldBytes, newBytes)) { identical++; continue; }
                String name = entry.getName().replace(PatchHeldIdle.ITEM, "").replace(".class", "");
                check(PatchHeldIdle.CHANGED.containsKey(name), "Unexpected payload " + entry.getName());
                changed++;
                ClassNode oldType = new ClassNode(); ClassNode newType = new ClassNode();
                new ClassReader(oldBytes).accept(oldType, 0); new ClassReader(newBytes).accept(newType, 0);
                check(fields(oldType).equals(fields(newType)), "Fields changed " + name);
                Map<String, String> oldMethods = methods(oldType); Map<String, String> newMethods = methods(newType);
                Set<String> extra = new HashSet<>(newMethods.keySet()); extra.removeAll(oldMethods.keySet());
                if (name.equals("OwnerRepairContinuation")) {
                    check(extra.equals(Set.of("noBinding(" + PatchHeldIdle.STACK + ")Z")), "Unexpected helper delta");
                    check(PatchHeldIdle.find(newType, "noBinding").access == Opcodes.ACC_STATIC, "Absence probe must stay package static");
                } else check(extra.isEmpty(), "Unexpected methods " + name);
                Set<String> changes = new HashSet<>();
                for (String key : oldMethods.keySet()) {
                    if (Objects.equals(oldMethods.get(key), newMethods.get(key))) identicalMethods++;
                    else changes.add(key.substring(0, key.indexOf('(')));
                }
                check(changes.equals(PatchHeldIdle.CHANGED.get(name)), "Unexpected method delta " + name + ": " + changes);
                newMethods.keySet().removeAll(extra);
                check(oldMethods.keySet().equals(newMethods.keySet()), "Missing methods " + name);
            }
        }
        check(changed == 6, "Expected 6 changed classes");
        System.out.println("PASS changedClasses=" + changed + " byteIdenticalOtherEntries=" + identical + " unchangedOtherMethods=" + identicalMethods + " addedAbsenceProbe=1");
    }
    static List<String> fields(ClassNode type) { return type.fields.stream().map(field -> field.access + ":" + field.name + ":" + field.desc + ":" + field.value).toList(); }
    static Map<String, String> methods(ClassNode type) {
        Map<String, String> result = new TreeMap<>();
        for (MethodNode method : type.methods) {
            Textifier trace = new Textifier(); method.accept(new TraceMethodVisitor(trace));
            StringWriter text = new StringWriter(); trace.print(new PrintWriter(text)); result.put(method.name + method.desc, text.toString());
        }
        return result;
    }
    static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
