import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public final class PatchIdentityAlias {
    private static final String ENTRY = "net/farmingtales/ftboss/item/OwnerRepairContinuation.class";

    public static void main(String[] arguments) throws Exception {
        byte[] original;
        try (ZipFile archive = new ZipFile(arguments[0])) {
            original = archive.getInputStream(archive.getEntry(ENTRY)).readAllBytes();
        }
        ClassReader reader = new ClassReader(original);
        ClassNode type = new ClassNode();
        reader.accept(type, 0);
        MethodNode method = type.methods.stream().filter(candidate -> candidate.name.equals("identityAlias")
                && candidate.desc.equals("(Ljava/lang/Object;Ljava/lang/Object;)Z")).findFirst().orElseThrow();
        List<AbstractInsnNode> operations = new ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions) if (instruction.getOpcode() >= 0) operations.add(instruction);
        require(operations.get(0) instanceof LdcInsnNode guard && guard.cst.toString().equals("Lnet/farmingtales/ftboss/item/OwnerFullRepairGuard;"));
        require(operations.get(1) instanceof LdcInsnNode name && name.cst.equals("alias"));
        require(operations.get(2) instanceof MethodInsnNode call && call.name.equals("caller")
                && call.owner.equals(type.name) && call.desc.equals("(Ljava/lang/Class;Ljava/lang/String;)Z"));
        require(operations.get(3).getOpcode() == Opcodes.IFEQ);
        require(operations.get(4).getOpcode() == Opcodes.ALOAD);
        require(operations.get(5) instanceof TypeInsnNode array && array.getOpcode() == Opcodes.INSTANCEOF && array.desc.equals("[I"));
        require(operations.get(6).getOpcode() == Opcodes.IFEQ);
        require(((JumpInsnNode) operations.get(3)).label == ((JumpInsnNode) operations.get(6)).label);
        require(operations.get(9).getOpcode() == Opcodes.ASTORE && operations.get(10).getOpcode() == Opcodes.GOTO);
        InsnList caller = new InsnList();
        for (int index = 0; index < 4; index++) {
            AbstractInsnNode instruction = operations.get(index);
            method.instructions.remove(instruction);
            caller.add(instruction);
        }
        method.instructions.insertBefore(operations.get(10), caller);
        ClassWriter writer = new ClassWriter(reader, 0);
        type.accept(writer);
        Path output = Path.of(arguments[1]).resolve(ENTRY);
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
        System.out.println("PATCHED identityAlias: int[] eligibility precedes unchanged caller validation");
    }

    private static void require(boolean condition) {
        if (!condition) throw new IllegalStateException("Unexpected production method; refusing patch");
    }
}
