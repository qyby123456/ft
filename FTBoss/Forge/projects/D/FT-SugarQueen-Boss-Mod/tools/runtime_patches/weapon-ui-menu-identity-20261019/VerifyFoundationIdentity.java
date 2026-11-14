import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.jar.JarFile;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class VerifyFoundationIdentity {
    private static String body(MethodNode method) {
        Textifier text = new Textifier();
        method.accept(new TraceMethodVisitor(text));
        StringWriter output = new StringWriter();
        text.print(new PrintWriter(output));
        return output.toString();
    }
    private static ArrayList<String> calls(MethodNode method) {
        var result = new ArrayList<String>();
        for (var instruction : method.instructions) if (instruction instanceof MethodInsnNode call) result.add(call.owner + "/" + call.name + call.desc);
        return result;
    }
    public static void main(String[] arguments) throws Exception {
        String target = "net/farmingtales/ftboss/transaction/FoundationEntryPoints.class";
        String network = "net/farmingtales/ftboss/network/StatueMenuNetwork.class";
        try (JarFile original = new JarFile(arguments[0]); JarFile candidate = new JarFile(arguments[1])) {
            if (original.size() != candidate.size()) throw new AssertionError("Entry count changed");
            var names = new HashSet<String>();
            int changed = 0;
            var entries = original.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (!names.add(entry.getName())) throw new AssertionError("Duplicate baseline entry");
                var after = candidate.getJarEntry(entry.getName());
                if (after == null) throw new AssertionError("Removed entry " + entry.getName());
                if (!Arrays.equals(original.getInputStream(entry).readAllBytes(), candidate.getInputStream(after).readAllBytes())) {
                    if (!entry.getName().equals(target) && !entry.getName().equals(network)) throw new AssertionError("Unrelated entry changed " + entry.getName());
                    changed++;
                }
            }
            if (changed != 2) throw new AssertionError("Expected exactly two changed entries");
            int totalMethods = 0;
            int preservedMethods = 0;
            for (String changedClass : new String[] {target, network}) {
                ClassNode before = new ClassNode();
                ClassNode after = new ClassNode();
                new ClassReader(original.getInputStream(original.getJarEntry(changedClass)).readAllBytes()).accept(before, 0);
                new ClassReader(candidate.getInputStream(candidate.getJarEntry(changedClass)).readAllBytes()).accept(after, 0);
                if (before.methods.size() != after.methods.size() || before.fields.size() != after.fields.size()) throw new AssertionError("Class shape changed");
                int methods = 0;
                for (MethodNode method : before.methods) {
                    MethodNode patched = after.methods.stream().filter(value -> value.name.equals(method.name) && value.desc.equals(method.desc)).findFirst().orElseThrow();
                    if (body(method).equals(body(patched))) continue;
                    if (!(changedClass.equals(target) && method.name.equals("seedFromStatue") || changedClass.equals(network) && method.name.equals("open"))) throw new AssertionError("Unrelated method changed " + method.name);
                    var priorCalls = calls(method);
                    var nextCalls = calls(patched);
                    int cursor = 0;
                    for (String call : nextCalls) if (cursor < priorCalls.size() && call.equals(priorCalls.get(cursor))) cursor++;
                    if (cursor != priorCalls.size()) throw new AssertionError("Existing seed call removed or reordered");
                    if (changedClass.equals(target) && nextCalls.stream().filter(value -> value.equals("java/util/UUID/randomUUID()Ljava/util/UUID;")).count() != 1) throw new AssertionError("Expected one identity creation");
                    if (changedClass.equals(network) && nextCalls.stream().filter(value -> value.contains("FoundationEntryPoints/seedFromStatue")).count() != 2) throw new AssertionError("Missing both-hand seeding");
                    methods++;
                }
                if (methods != 1) throw new AssertionError("Expected one changed method");
                totalMethods += methods;
                preservedMethods += before.methods.size() - methods;
            }
            if (totalMethods != 2) throw new AssertionError("Expected two changed methods");
            System.out.println("PASS exact guarded seed and statue-open delta; " + (original.size() - 2) + " other entries and " + preservedMethods + " other methods unchanged");
        }
    }
}
