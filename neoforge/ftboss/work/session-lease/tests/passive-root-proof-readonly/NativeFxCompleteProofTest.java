import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class NativeFxCompleteProofTest {
    private static java.lang.reflect.Method proof;
    private static MethodNode quoted;
    private static MethodNode mapQuoted;
    private static ClassNode mapSource;
    private static java.lang.reflect.Method mapProof;
    private static MethodNode method(ClassNode host, String pattern) {
        return host.methods.stream().filter(value -> value.name.matches(pattern)).findFirst().orElseThrow();
    }
    private static boolean accepted(ClassNode host) throws Exception {
        MethodNode guard = method(host, ".*\\$ftboss\\$fullRepairVoidMutation");
        MethodNode mapActual = method(host, ".*\\$ftboss\\$originalFieldMapPut");
        return Boolean.TRUE.equals(proof.invoke(null, host, quoted, guard))
            && Boolean.TRUE.equals(mapProof.invoke(null, host, mapSource, mapQuoted, mapActual));
    }
    private static void reject(ClassNode original, String name, Consumer<ClassNode> mutate) throws Exception {
        ClassNode changed = new ClassNode(); original.accept(changed);
        mutate.accept(changed);
        if (accepted(changed)) throw new AssertionError("Malformed FX proof accepted: " + name);
        System.out.println(name + "=rejected");
    }
    public static void main(String[] arguments) throws Exception {
        ClassNode host = new ClassNode();
        new org.objectweb.asm.ClassReader(Files.readAllBytes(Path.of(arguments[0]))).accept(host, 0);
        ClassNode source = new ClassNode();
        new org.objectweb.asm.ClassReader(Files.readAllBytes(Path.of(arguments[1]))).accept(source, 0);
        quoted = method(source, "ftboss\\$fullRepairVoidMutation");
        proof = Class.forName("net.farmingtales.ftboss.item.OwnerFieldInstallationProof")
            .getDeclaredMethod("siteAndOrder", ClassNode.class, MethodNode.class, MethodNode.class);
        proof.setAccessible(true);
        mapSource = new ClassNode();
        new org.objectweb.asm.ClassReader(Files.readAllBytes(Path.of(arguments[2]))).accept(mapSource, 0);
        mapQuoted = method(mapSource, "ftboss\\$originalFieldMapPut");
        mapProof = Class.forName("net.farmingtales.ftboss.item.OwnerFieldInstallationProof")
            .getDeclaredMethod("mapWrapper", ClassNode.class, ClassNode.class, MethodNode.class, MethodNode.class);
        mapProof.setAccessible(true);
        if (!accepted(host)) throw new AssertionError("Actual original FX guarded chain rejected");
        System.out.println("actualOriginalFxChain=accepted");
        reject(host, "wrongHost", changed -> changed.name += "Other");
        reject(host, "extraPublicInstruction", changed -> method(changed, "putLong").instructions.insert(new InsnNode(Opcodes.NOP)));
        reject(host, "wrongCallback", changed -> {
            for (var instruction : method(changed, ".*\\$ftboss\\$nativeFxCooldown").instructions)
                if (instruction instanceof MethodInsnNode call) call.name = "foreignCallback";
        });
        reject(host, "wrongBridgePrimitive", changed -> {
            for (var instruction : method(changed, "mixinextras\\$bridge\\$putLong.*").instructions)
                if (instruction instanceof MethodInsnNode call && call.name.equals("longValue")) call.name = "intValue";
        });
        reject(host, "wrongBootstrap", changed -> {
            for (var instruction : method(changed, "putLong").instructions)
                if (instruction instanceof InvokeDynamicInsnNode dynamic) dynamic.name = "foreignFactory";
        });
        reject(host, "missingOriginalGuard", changed -> {
            MethodNode original = method(changed, "putLong\\$mixinextras\\$wrapped\\$[0-9]+");
            for (var instruction : original.instructions.toArray())
                if (instruction instanceof MethodInsnNode call && call.name.endsWith("$ftboss$fullRepairVoidMutation")) original.instructions.remove(instruction);
        });
        reject(host, "branchBeforeOriginalGuard", changed -> {
            MethodNode original = method(changed, "putLong\\$mixinextras\\$wrapped\\$[0-9]+");
            LabelNode target = new LabelNode(); original.instructions.add(target);
            original.instructions.insert(new JumpInsnNode(Opcodes.GOTO, target));
        });
        reject(host, "duplicateMovedOriginal", changed -> {
            MethodNode original = method(changed, "putLong\\$mixinextras\\$wrapped\\$[0-9]+");
            MethodNode duplicate = new MethodNode(original.access, "putLong$mixinextras$wrapped$999999", original.desc, null, null);
            original.accept(duplicate); duplicate.name = "putLong$mixinextras$wrapped$999999"; changed.methods.add(duplicate);
        });
        reject(host, "escapingMovedOriginal", changed -> {
            MethodNode original = method(changed, "putLong\\$mixinextras\\$wrapped\\$[0-9]+");
            MethodNode escape = new MethodNode(2, "foreignEscape", "()V", null, null);
            escape.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, changed.name, original.name, original.desc, false));
            changed.methods.add(escape);
        });
        reject(host, "wrongOriginalMapPut", changed -> {
            for (var instruction : method(changed, "putLong\\$mixinextras\\$wrapped\\$[0-9]+").instructions)
                if (instruction instanceof MethodInsnNode call && call.name.endsWith("$ftboss$originalFieldMapPut")) call.name = "foreignMapPut";
        });
        reject(host, "wrongMapFieldType", changed -> changed.fields.stream().filter(field -> field.name.equals("tags"))
            .forEach(field -> field.desc = "Ljava/lang/Object;"));
        System.out.println("failures=0;negativeCases=11;originalGuardAndAllThreeMapWrites=true");
    }
}
