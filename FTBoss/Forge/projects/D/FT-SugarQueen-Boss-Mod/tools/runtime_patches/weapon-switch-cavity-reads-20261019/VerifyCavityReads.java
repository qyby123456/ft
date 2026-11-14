import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.*;

public final class VerifyCavityReads {
    public static void main(String[] arguments) throws Exception {
        int unchangedEntries = 0;
        int changedEntries = 0;
        int unchangedMethods = 0;
        try (ZipFile before = new ZipFile(arguments[0]); ZipFile after = new ZipFile(arguments[1])) {
            check(before.size() == after.size(), "Entry count changed");
            for (ZipEntry entry : Collections.list(before.entries())) {
                ZipEntry next = after.getEntry(entry.getName());
                check(next != null, "Missing entry " + entry.getName());
                byte[] oldBytes = before.getInputStream(entry).readAllBytes();
                byte[] newBytes = after.getInputStream(next).readAllBytes();
                if (Arrays.equals(oldBytes, newBytes)) { unchangedEntries++; continue; }
                check(entry.getName().equals(PatchCavityReads.TYPE + ".class"), "Unexpected entry change " + entry.getName());
                changedEntries++;
                ClassNode oldType = new ClassNode();
                ClassNode newType = new ClassNode();
                new ClassReader(oldBytes).accept(oldType, 0);
                new ClassReader(newBytes).accept(newType, 0);
                Map<String, String> oldMethods = methods(oldType);
                Map<String, String> newMethods = methods(newType);
                Set<String> extra = new HashSet<>(newMethods.keySet());
                extra.removeAll(oldMethods.keySet());
                check(extra.equals(Set.of("legacyMaterialValues(Ljava/util/List;)Ljava/util/List;")), "Unexpected new methods");
                MethodNode helper = PatchCavityReads.find(newType, "legacyMaterialValues", "(Ljava/util/List;)Ljava/util/List;");
                check(helper.access == (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC), "Helper must remain private static");
                Set<String> changed = new HashSet<>();
                for (String method : oldMethods.keySet()) {
                    if (!Objects.equals(oldMethods.get(method), newMethods.get(method))) changed.add(method);
                    else unchangedMethods++;
                }
                check(changed.equals(Set.of("effectiveCells(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
                        "legacyBuild(Lnet/minecraft/world/item/ItemStack;)Lnet/farmingtales/ftboss/util/FoodWeaponBuildRules$Build;")), "Unexpected method delta " + changed);
                newMethods.keySet().removeAll(extra);
                check(newMethods.keySet().equals(oldMethods.keySet()), "Missing methods");
                check(fields(oldType).equals(fields(newType)), "Field contract changed");
            }
        }
        check(changedEntries == 1, "Expected exactly one changed class");
        System.out.println("PASS changedEntries=" + changedEntries + " identicalOtherEntries=" + unchangedEntries
                + " identicalOtherMethods=" + unchangedMethods + " addedPrivateHelpers=1");
    }
    static Map<String, String> methods(ClassNode type) {
        Map<String, String> result = new TreeMap<>();
        for (MethodNode method : type.methods) {
            Textifier text = new Textifier();
            method.accept(new TraceMethodVisitor(text));
            StringWriter output = new StringWriter();
            text.print(new PrintWriter(output));
            result.put(method.name + method.desc, output.toString());
        }
        return result;
    }
    static List<String> fields(ClassNode type) {
        return type.fields.stream().map(field -> field.access + ":" + field.name + ":" + field.desc + ":" + field.value).toList();
    }
    static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
