import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class BossOwnerBoundaryProof {
    private static ClassNode load(Path path) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(path)).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return node;
    }

    private static List<String> instructions(MethodNode method) {
        Textifier printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        return printer.getText().stream().map(Object::toString).toList();
    }

    public static void main(String[] arguments) throws Exception {
        ClassNode previous = load(Path.of(arguments[0]));
        ClassNode current = load(Path.of(arguments[1]));
        if (!previous.name.equals(current.name) || previous.methods.size() != current.methods.size())
            throw new AssertionError("Owner class identity or method set changed");
        List<String> changed = new ArrayList<>();
        for (MethodNode method : current.methods) {
            new Analyzer<>(new BasicVerifier()).analyze(current.name, method);
            MethodNode before = previous.methods.stream().filter(entry -> entry.name.equals(method.name)
                && entry.desc.equals(method.desc)).findFirst().orElseThrow();
            if (!instructions(before).equals(instructions(method))) changed.add(method.name + method.desc);
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.contains("FoodWeapon"))
                    throw new AssertionError("Boss owner propagation still depends on weapon subsystem");
            }
        }
        if (!changed.equals(List.of("inherit(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V")))
            throw new AssertionError("Unexpected method changes: " + changed);
        MethodNode inherit = current.methods.stream().filter(method -> method.name.equals("inherit")).findFirst().orElseThrow();
        long originalOwnerWrites = java.util.stream.StreamSupport.stream(inherit.instructions.spliterator(), false)
            .filter(instruction -> instruction instanceof MethodInsnNode call && call.owner.equals("net/minecraft/nbt/CompoundTag")
                && call.name.equals("putUUID") && call.desc.equals("(Ljava/lang/String;Ljava/util/UUID;)V")).count();
        if (originalOwnerWrites != 1) throw new AssertionError("Expected exactly one original persistent owner UUID write");
        System.out.println("failures=0;changedMethods=1;originalOwnerUuidWrites=1;weaponLedgerDependency=false;allMethodsBasicVerified=true");
    }
}
