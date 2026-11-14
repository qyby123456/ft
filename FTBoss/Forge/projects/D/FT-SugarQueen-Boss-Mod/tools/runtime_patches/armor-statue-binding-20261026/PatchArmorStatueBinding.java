import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class PatchArmorStatueBinding {
    private static final String ROOT = "net/farmingtales/ftboss/item/";
    private static final String ENTRY = ROOT + "FoodWeaponEvolution.class";
    private static final String DESCRIPTOR = "(Lnet/minecraft/world/item/ItemStack;)Z";

    private static String fingerprint(MethodNode method) {
        Textifier printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        StringWriter text = new StringWriter();
        printer.print(new PrintWriter(text));
        return method.access + "/" + method.signature + "/" + method.exceptions + "/" + text;
    }

    public static void main(String[] arguments) throws Exception {
        byte[] original;
        try (var archive = new ZipFile(arguments[0])) {
            original = archive.getInputStream(archive.getEntry(ENTRY)).readAllBytes();
        }
        ClassNode type = new ClassNode();
        new ClassReader(original).accept(type, 0);
        MethodNode candidate = type.methods.stream().filter(method -> method.name.equals("weaponCandidate")
                && method.desc.equals(DESCRIPTOR)).findFirst().orElseThrow();
        for (var instruction : candidate.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(ROOT + "EquipmentWeaponBuild")) {
                throw new AssertionError("Armor eligibility already present");
            }
        }
        LabelNode fallback = new LabelNode();
        InsnList prefix = new InsnList();
        prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
        prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ROOT + "EquipmentWeaponBuild", "applies", DESCRIPTOR, false));
        prefix.add(new JumpInsnNode(Opcodes.IFEQ, fallback));
        prefix.add(new InsnNode(Opcodes.ICONST_1));
        prefix.add(new InsnNode(Opcodes.IRETURN));
        prefix.add(fallback);
        prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        candidate.instructions.insert(prefix);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        byte[] patched = writer.toByteArray();
        ClassNode before = new ClassNode();
        ClassNode after = new ClassNode();
        new ClassReader(original).accept(before, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        new ClassReader(patched).accept(after, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        for (MethodNode prior : before.methods) {
            MethodNode next = after.methods.stream().filter(method -> method.name.equals(prior.name)
                    && method.desc.equals(prior.desc)).findFirst().orElseThrow();
            if (prior.name.equals("weaponCandidate") && prior.desc.equals(DESCRIPTOR)) {
                for (int index = 0; index < 6; index++) next.instructions.remove(next.instructions.getFirst());
                next.maxStack = prior.maxStack;
                next.maxLocals = prior.maxLocals;
            }
            if (!fingerprint(prior).equals(fingerprint(next))) throw new AssertionError("Original method changed: " + prior.name);
        }
        Path output = Path.of(arguments[1]).resolve(ENTRY);
        Files.createDirectories(output.getParent());
        Files.write(output, patched);
        System.out.println("PASS: armor eligibility added; original instructions preserved for all " + before.methods.size() + " methods");
    }
}
