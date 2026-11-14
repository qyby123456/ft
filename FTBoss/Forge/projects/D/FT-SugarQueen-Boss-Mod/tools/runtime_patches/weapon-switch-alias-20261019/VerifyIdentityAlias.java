import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class VerifyIdentityAlias {
    private static final String ENTRY = "net/farmingtales/ftboss/item/OwnerRepairContinuation.class";
    public static void main(String[] arguments) throws Exception {
        int unchanged = 0;
        int changed = 0;
        int unchangedMethods = 0;
        try (ZipFile before = new ZipFile(arguments[0]); ZipFile after = new ZipFile(arguments[1])) {
            if (before.size() != after.size()) throw new AssertionError("Entry count changed");
            Enumeration<? extends ZipEntry> entries = before.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                ZipEntry result = after.getEntry(entry.getName());
                if (result == null) throw new AssertionError("Missing entry " + entry.getName());
                byte[] original = before.getInputStream(entry).readAllBytes();
                byte[] modified = after.getInputStream(result).readAllBytes();
                if (Arrays.equals(original, modified)) { unchanged++; continue; }
                if (!entry.getName().equals(ENTRY)) throw new AssertionError("Unexpected change " + entry.getName());
                changed++;
                ClassNode originalType = new ClassNode();
                ClassNode modifiedType = new ClassNode();
                new ClassReader(original).accept(originalType, 0);
                new ClassReader(modified).accept(modifiedType, 0);
                Map<String, String> originalMethods = methods(originalType);
                Map<String, String> modifiedMethods = methods(modifiedType);
                if (!originalMethods.keySet().equals(modifiedMethods.keySet())) throw new AssertionError("Method inventory changed");
                for (String method : originalMethods.keySet()) {
                    if (method.equals("identityAlias(Ljava/lang/Object;Ljava/lang/Object;)Z")) {
                        if (originalMethods.get(method).equals(modifiedMethods.get(method))) throw new AssertionError("Target unchanged");
                    } else {
                        if (!originalMethods.get(method).equals(modifiedMethods.get(method))) throw new AssertionError("Other method changed " + method);
                        unchangedMethods++;
                    }
                }
            }
        }
        if (changed != 1) throw new AssertionError("Expected exactly one changed entry");
        System.out.println("{\"changedEntries\":" + changed + ",\"byteIdenticalOtherEntries\":" + unchanged
                + ",\"unchangedOtherMethods\":" + unchangedMethods + "}");
    }
    private static Map<String, String> methods(ClassNode type) {
        Map<String, String> methods = new TreeMap<>();
        for (MethodNode method : type.methods) {
            Textifier trace = new Textifier();
            method.accept(new TraceMethodVisitor(trace));
            StringWriter output = new StringWriter();
            trace.print(new PrintWriter(output));
            methods.put(method.name + method.desc, output.toString());
        }
        return methods;
    }
}
