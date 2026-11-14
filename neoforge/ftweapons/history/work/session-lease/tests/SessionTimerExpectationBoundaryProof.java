import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class SessionTimerExpectationBoundaryProof {
    private static ClassNode read(Path directory, String name) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(directory.resolve(name + ".class"))).accept(node, 0);
        return node;
    }
    private static String body(MethodNode method) {
        Textifier text = new Textifier();
        method.accept(new TraceMethodVisitor(text));
        return method.access + "/" + method.desc + "/" + method.signature + "/" + method.exceptions + "/" + text.getText();
    }
    public static void main(String[] arguments) throws Exception {
        Path before = Path.of(arguments[0]);
        Path after = Path.of(arguments[1]);
        int preserved = 0;
        int added = 0;
        int linkedClose = 0;
        for (String name : new String[]{"FoodWeaponSourceLedger", "FoodWeaponOriginalSequence$Session"}) {
            String prefix = "net/farmingtales/ftboss/item/";
            ClassNode original = read(before, prefix + name);
            ClassNode current = read(after, prefix + name);
            Map<String, MethodNode> methods = new HashMap<>();
            for (MethodNode method : current.methods) methods.put(method.name + method.desc, method);
            for (MethodNode method : original.methods) {
                MethodNode actual = methods.remove(method.name + method.desc);
                if (actual == null) throw new AssertionError("Removed original method " + method.name);
                if (name.endsWith("$Session") && method.name.equals("close")) {
                    int originalAdvance = 0;
                    for (var instruction : actual.instructions) {
                        if (instruction instanceof MethodInsnNode call && call.name.equals("nativeTimerExpectationReturned")) linkedClose++;
                        if (instruction instanceof MethodInsnNode call && call.owner.endsWith("OwnerFieldExpectedAuthority") && call.name.equals("advance")) originalAdvance++;
                    }
                    if (originalAdvance != 1) throw new AssertionError("Original field advance removed or duplicated");
                } else {
                    if (!body(method).equals(body(actual))) throw new AssertionError("Unrelated body changed: " + name + "." + method.name + method.desc);
                    preserved++;
                }
            }
            for (MethodNode method : methods.values()) {
                if (!name.equals("FoodWeaponSourceLedger") || !(method.name.equals("nativeTimerExpectationReturned")
                    || method.name.startsWith("lambda$nativeTimerExpectationReturned$"))) throw new AssertionError("Unexpected new method " + method.name);
                added++;
            }
        }
        if (linkedClose != 1 || added != 3) throw new AssertionError("Native TIMER gate/helper cardinality");
        System.out.println("failures=0;unrelatedMethodsPreserved=" + preserved + ";addedHelperAndLambdas=" + added + ";originalCloseLinks=" + linkedClose);
    }
}
