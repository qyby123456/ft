import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class YouziCloneWeaponsPatch {
    public static final String PREFIX = "net/farmingtales/ftboss/";
    public static final String ENTITY = PREFIX + "entity/EasterBossEntity";
    public static final String WEAPONS = PREFIX + "boss/ai/YouziCloneWeapons";

    private static void save(ClassNode type, Path destination) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path file = destination.resolve(type.name + ".class");
        Files.createDirectories(file.getParent());
        Files.write(file, writer.toByteArray());
    }

    public static void main(String[] arguments) throws Exception {
        Path classes = Path.of(arguments[1]);
        Path payload = Path.of(arguments[3]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        try (ZipFile archive = new ZipFile(arguments[0])) {
            ClassNode type = new ClassNode();
            new ClassReader(archive.getInputStream(archive.getEntry(ENTITY + ".class")).readAllBytes()).accept(type, 0);
            var method = type.methods.stream().filter(candidate -> candidate.name.equals("equipMiniWeapon")
                    && candidate.desc.equals("(I)V")).findFirst().orElseThrow();
            method.instructions.clear();
            method.tryCatchBlocks.clear();
            if (method.localVariables != null) method.localVariables.clear();
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, WEAPONS, "equipRandom", "(L" + ENTITY + ";)V", false));
            method.instructions.add(new InsnNode(Opcodes.RETURN));
            var initializer = type.methods.stream().filter(candidate -> candidate.name.equals("<clinit>")).findFirst().orElseThrow();
            FieldInsnNode field = null;
            for (var instruction : initializer.instructions) {
                if (instruction instanceof FieldInsnNode put && put.getOpcode() == Opcodes.PUTSTATIC
                        && put.name.equals("MINI_WEAPONS")) field = put;
            }
            if (field == null) throw new IllegalStateException("Missing clone weapon field");
            var first = field.getPrevious();
            while (first != null && first.getOpcode() != Opcodes.ANEWARRAY) first = first.getPrevious();
            if (first == null || first.getPrevious().getOpcode() != Opcodes.ICONST_3) {
                throw new IllegalStateException("Unexpected original clone weapon initializer");
            }
            first = first.getPrevious();
            while (first != field) {
                var next = first.getNext();
                initializer.instructions.remove(first);
                first = next;
            }
            initializer.instructions.insertBefore(field, new MethodInsnNode(Opcodes.INVOKESTATIC,
                    WEAPONS, "weaponIds", "()[Ljava/lang/String;", false));
            var miniature = type.methods.stream().filter(candidate -> candidate.name.equals("setMini")
                    && candidate.desc.equals("(Z)V")).findFirst().orElseThrow();
            for (var instruction : miniature.instructions.toArray()) {
                if (instruction.getOpcode() != Opcodes.RETURN) continue;
                var hook = new org.objectweb.asm.tree.InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, PREFIX + "youzi/YouziMiniBalance", "restore",
                        "(L" + ENTITY + ";)V", false));
                miniature.instructions.insertBefore(instruction, hook);
            }
            save(type, payload);
        }
        try (var files = Files.walk(classes.resolve(PREFIX))) {
            for (Path file : files.filter(candidate -> candidate.toString().endsWith(".class")).toList()) {
                ClassNode output = new ClassNode();
                new ClassReader(Files.readAllBytes(file)).accept(new ClassRemapper(output, names), 0);
                save(output, payload);
            }
        }
        System.out.println("PASS: clone equipment, initializer and setMini patched; all other runtime methods retained");
    }
}
