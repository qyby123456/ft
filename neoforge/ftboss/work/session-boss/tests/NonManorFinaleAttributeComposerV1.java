import com.google.gson.Gson;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public final class NonManorFinaleAttributeComposerV1 implements Opcodes {
    private static final String TARGET = "net/farmingtales/ftboss/boss/NonManorFinale";
    private static final String HELPER = TARGET + "Attributes";
    private static final String DESC = "(Lnet/minecraft/world/entity/LivingEntity;Z)V";
    private static int checks;
    private static void require(boolean condition, String label) {
        checks++;
        if (!condition) throw new IllegalStateException(label);
    }
    private static ClassNode read(byte[] bytes) {
        ClassNode result = new ClassNode();
        new ClassReader(bytes).accept(result, 0);
        return result;
    }
    private static byte[] write(ClassNode node) {
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }
    private static MethodNode target(ClassNode node) {
        List<MethodNode> found = node.methods.stream().filter(method -> method.name.equals("applyAttributes") && method.desc.equals(DESC)).toList();
        require(found.size() == 1, "Unique existing private attribute implementation");
        require(found.getFirst().access == (ACC_PRIVATE | ACC_STATIC), "Original method access preserved");
        return found.getFirst();
    }
    private static void verify(ClassNode before, ClassNode after) {
        MethodNode oldMethod = target(before), changed = target(after);
        AbstractInsnNode[] code = changed.instructions.toArray();
        require(code.length == 4 && code[0] instanceof VarInsnNode load && load.getOpcode() == ALOAD && load.var == 0, "Exact entity delegation");
        require(code[1] instanceof VarInsnNode load && load.getOpcode() == ILOAD && load.var == 1, "Exact full-heal delegation");
        require(code[2] instanceof MethodInsnNode call && call.getOpcode() == INVOKESTATIC && call.owner.equals(HELPER) && call.name.equals("apply") && call.desc.equals(DESC) && !call.itf, "Exact helper linkage");
        require(code[3].getOpcode() == RETURN, "Only helper delegation and return");
        require(before.methods.size() == after.methods.size(), "No ingress, tick, cast, or reward method added or removed");
        int index = after.methods.indexOf(changed);
        after.methods.set(index, oldMethod);
        require(Arrays.equals(write(before), write(after)), "Every other method, field, annotation, and class property unchanged");
        after.methods.set(index, changed);
    }
    public static void main(String[] args) throws Exception {
        Path input = Path.of(args[0]), classes = Path.of(args[1]), output = Path.of(args[2]);
        byte[] raw;
        try (ZipFile jar = new ZipFile(input.toFile())) { raw = jar.getInputStream(jar.getEntry(TARGET + ".class")).readAllBytes(); }
        ClassNode before = read(raw), after = read(raw);
        MethodNode original = target(after);
        MethodNode replacement = new MethodNode(original.access, original.name, original.desc, original.signature, original.exceptions.toArray(String[]::new));
        replacement.visibleAnnotations = original.visibleAnnotations;
        replacement.invisibleAnnotations = original.invisibleAnnotations;
        replacement.instructions.add(new VarInsnNode(ALOAD, 0));
        replacement.instructions.add(new VarInsnNode(ILOAD, 1));
        replacement.instructions.add(new MethodInsnNode(INVOKESTATIC, HELPER, "apply", DESC, false));
        replacement.instructions.add(new InsnNode(RETURN));
        replacement.maxLocals = 2;
        replacement.maxStack = 2;
        after.methods.set(after.methods.indexOf(original), replacement);
        verify(before, after);
        int negatives = 0;
        ClassNode bad = read(write(after));
        ((MethodInsnNode) target(bad).instructions.get(2)).name = "wrongApply";
        try { verify(before, bad); } catch (IllegalStateException expected) { negatives++; }
        bad = read(write(after));
        bad.methods.stream().filter(method -> method.name.equals("tick")).findFirst().orElseThrow().instructions.insert(new InsnNode(NOP));
        try { verify(before, bad); } catch (IllegalStateException expected) { negatives++; }
        require(negatives == 2, "Both wrong linkage and collateral tick mutation rejected");
        int verifiedMethods = 0;
        for (String name : List.of(HELPER, HELPER + "$AttributeBase")) {
            ClassNode helper = read(Files.readAllBytes(classes.resolve(name + ".class")));
            for (MethodNode method : helper.methods) {
                new Analyzer<BasicValue>(new BasicVerifier()).analyze(helper.name, method);
                verifiedMethods++;
                for (AbstractInsnNode instruction : method.instructions) {
                    if (instruction instanceof MethodInsnNode call) require(!call.name.equals("addPermanentModifier") && !call.name.equals("addTransientModifier"), "No invented finale multiplier modifier");
                }
            }
        }
        new Analyzer<BasicValue>(new BasicVerifier()).analyze(after.name, target(after));
        Files.write(output.resolve("NonManorFinale.class"), write(after));
        Map<String, Object> receipt = new LinkedHashMap<>();
        receipt.put("checks", checks);
        receipt.put("negativeControls", negatives);
        receipt.put("unchangedOtherMethods", before.methods.size() - 1);
        receipt.put("verifiedHelperAndDelegateMethods", verifiedMethods + 1);
        receipt.put("changedExistingMethods", List.of("applyAttributes" + DESC));
        receipt.put("allIngressTickCastingRewardAndOtherMethodsUnchanged", true);
        receipt.put("newFinaleTotalMultipliersAdded", false);
        System.out.println(new Gson().toJson(receipt));
    }
}
