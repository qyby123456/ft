import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

public final class LinkBossMaterialDropBoundary {
    public static void main(String[] arguments) throws Exception {
        Path input = Path.of(arguments[0]);
        Path output = Path.of(arguments[1]);
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(input)).accept(node, 0);
        int changed = 0;
        for (var method : node.methods) {
            for (var instruction : method.instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals("net/farmingtales/ftboss/item/WeaponBossMaterialItems") && call.name.equals("reward")) {
                    if (call.getOpcode() != Opcodes.INVOKESTATIC || !call.desc.equals("(Lnet/farmingtales/ftboss/util/BossRelicRules$Kind;Ljava/util/function/Consumer;)V")) throw new AssertionError("Unexpected original callback signature");
                    call.owner = "net/farmingtales/ftboss/item/BossMaterialDrops";
                    changed++;
                }
            }
        }
        if (changed != 1) throw new AssertionError("Expected one original Boss material edge");
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        Files.write(output, writer.toByteArray());
        System.out.println("rewrittenCallbackOwners=1;originalControlFlowAndExceptionHandlersRetained=true;class=" + node.name);
    }
}
