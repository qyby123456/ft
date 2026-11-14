import java.io.PrintWriter;
import java.io.StringWriter;
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
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class PatchWeaponBondGuide {
    private static final String OWNER = "net/farmingtales/ftboss/client/BossPreviewScreen";
    private static final String ENTRY = "net/farmingtales/ftboss/client/StatueWeaponBondGuideEntry";

    private static String trace(MethodNode method) {
        Textifier printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        StringWriter output = new StringWriter();
        printer.print(new PrintWriter(output));
        return output.toString();
    }

    public static void main(String[] arguments) throws Exception {
        ClassNode original = new ClassNode();
        try (ZipFile archive = new ZipFile(arguments[0])) {
            new ClassReader(archive.getInputStream(archive.getEntry(OWNER + ".class"))).accept(original, 0);
        }
        MethodNode init = original.methods.stream().filter(method -> method.name.equals("m_7856_") && method.desc.equals("()V")).findFirst().orElseThrow();
        String before = trace(init);
        MethodInsnNode widget = null;
        for (var instruction : init.instructions) if (instruction instanceof MethodInsnNode call) {
            if (call.owner.equals(ENTRY)) throw new IllegalStateException("Guide already installed");
            if (call.name.equals("m_142416_")) widget = call;
        }
        if (widget == null) throw new AssertionError("No widget registration anchor");
        List<AbstractInsnNode> injected = new ArrayList<>();
        int returns = 0;
        for (var instruction : init.instructions.toArray()) if (instruction.getOpcode() == Opcodes.RETURN) {
            InsnList addition = new InsnList();
            addition.add(new VarInsnNode(Opcodes.ALOAD, 0));
            addition.add(new FieldInsnNode(Opcodes.GETFIELD, OWNER, "modelButtons", "Ljava/util/List;"));
            addition.add(new VarInsnNode(Opcodes.ALOAD, 0));
            addition.add(new VarInsnNode(Opcodes.ALOAD, 0));
            addition.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENTRY, "create", "(Lnet/minecraft/client/gui/screens/Screen;)Lnet/minecraft/client/gui/components/Button;", false));
            addition.add(new MethodInsnNode(widget.getOpcode(), widget.owner, widget.name, widget.desc, widget.itf));
            addition.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/client/gui/components/Button"));
            addition.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true));
            addition.add(new InsnNode(Opcodes.POP));
            for (var added : addition.toArray()) injected.add(added);
            init.instructions.insertBefore(instruction, addition);
            returns++;
        }
        if (returns != 1) throw new AssertionError("Expected a single init return");
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        original.accept(writer);
        byte[] updated = writer.toByteArray();
        for (var instruction : injected) init.instructions.remove(instruction);
        if (!trace(init).equals(before)) throw new AssertionError("Unrelated init changes");
        Path destination = Path.of(arguments[1]).resolve(OWNER + ".class");
        Files.createDirectories(destination.getParent());
        Files.write(destination, updated);
        System.out.println("PASS one read-only guide button injected; every original field, method and init instruction preserved");
    }
}
