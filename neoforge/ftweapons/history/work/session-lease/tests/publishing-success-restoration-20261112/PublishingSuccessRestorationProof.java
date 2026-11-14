import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class PublishingSuccessRestorationProof {
    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return node;
    }

    private static List<String> instructions(MethodNode method) {
        Textifier printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        return printer.getText().stream().map(Object::toString).toList();
    }

    private static MethodNode publishing(ClassNode node) {
        return node.methods.stream().filter(method -> method.name.equals("publishListedProducer")).findFirst().orElseThrow();
    }

    private static int stores(MethodNode method) {
        int found = 0;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof VarInsnNode variable && variable.getOpcode() == Opcodes.ISTORE && variable.var == 4) found++;
        }
        return found;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] arguments) throws Exception {
        ClassNode baseline = read(Files.readAllBytes(Path.of(arguments[0])));
        ClassNode candidate = read(Files.readAllBytes(Path.of(arguments[1])));
        ClassNode original;
        try (ZipFile archive = new ZipFile(arguments[2])) {
            original = read(archive.getInputStream(archive.getEntry("net/farmingtales/ftboss/item/FoodWeaponFinalDamage.class")).readAllBytes());
        }
        MethodNode restored = publishing(candidate);
        require(stores(publishing(original)) == 2, "Frozen Forge original must assign false then actual publish result to success local");
        require(stores(publishing(baseline)) == 1, "Baseline decompile omission no longer matches actual gap");
        require(stores(restored) == 2, "Restoration lacks the actual success-result assignment");
        new Analyzer<>(new BasicVerifier()).analyze(candidate.name, restored);
        int stripped = 0;
        for (AbstractInsnNode instruction : restored.instructions.toArray()) {
            if (!(instruction instanceof VarInsnNode variable) || variable.getOpcode() != Opcodes.ISTORE || variable.var != 4) continue;
            AbstractInsnNode previous = instruction.getPrevious();
            if (previous == null || previous.getOpcode() != Opcodes.DUP) continue;
            require(instruction.getNext() instanceof VarInsnNode returned && returned.getOpcode() == Opcodes.ISTORE && returned.var == 7, "Restored success assignment is not the actual original return value");
            restored.instructions.remove(previous);
            restored.instructions.remove(instruction);
            stripped++;
        }
        require(stripped == 1, "Exactly one copied successful result must be restored");
        require(baseline.methods.size() == candidate.methods.size(), "Unexpected method count change");
        int preserved = 0;
        for (MethodNode before : baseline.methods) {
            MethodNode after = candidate.methods.stream().filter(method -> method.name.equals(before.name) && method.desc.equals(before.desc)).findFirst().orElseThrow();
            require(before.access == after.access && instructions(before).equals(instructions(after)), "Unrelated original instruction changed: " + before.name + before.desc);
            preserved++;
        }
        System.out.println("failures=0;originalForgeSuccessFlagStores=2;baselineSuccessFlagStores=1;restoredSuccessFlagStores=2;singleSuccessResultAssignmentRestored=true;allOriginalMethodInstructionsPreservedAfterStrippingAssignment=" + preserved + ";changedMethodBasicVerified=true;noGameplayNumbersOrOriginalCallsChanged=true");
    }
}
