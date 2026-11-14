import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class PatchManorHealthBalance implements Opcodes {
    public static void main(String[] arguments) throws Exception {
        Path payload = Path.of(arguments[1]);
        try (JarFile baseline = new JarFile(arguments[0])) {
            String entity = "net/farmingtales/ftboss/entity/EasterBossEntity";
            ClassNode boss = read(baseline, entity);
            int health = 0;
            for (var method : boss.methods) {
                if (!method.name.equals("createAttributes")) continue;
                for (var instruction : method.instructions) {
                    if (instruction instanceof LdcInsnNode constant && constant.cst.equals(1500.0)
                            && constant.getPrevious() instanceof FieldInsnNode attribute
                            && attribute.name.equals("f_22276_")) {
                        constant.cst = 3000.0;
                        health++;
                    }
                }
            }
            if (health != 1) throw new IllegalStateException("Expected one 1500 manor base health attribute: " + health);
            write(payload, boss);
            ClassNode multiplayer = read(baseline, "net/farmingtales/ftboss/boss/ai/ManorMultiplayerBalance");
            int cap = 0;
            for (var method : multiplayer.methods) {
                if (!method.name.equals("applyHealthAndRestoreDamage")) continue;
                for (var instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof LdcInsnNode constant) || !constant.cst.equals(1024.0)
                            || !(constant.getPrevious() instanceof VarInsnNode base) || base.getOpcode() != DLOAD
                            || constant.getNext().getOpcode() != DCMPG
                            || !(constant.getNext().getNext() instanceof JumpInsnNode jump) || jump.getOpcode() != IFGT) continue;
                    InsnList support = new InsnList();
                    support.add(new VarInsnNode(ALOAD, 0));
                    support.add(new MethodInsnNode(INVOKESTATIC, "net/farmingtales/ftboss/boss/ai/ManorHealthBalance",
                            "support", "(Lnet/farmingtales/ftboss/entity/EasterBossEntity;)Z", false));
                    support.add(new JumpInsnNode(IFNE, jump.label));
                    method.instructions.insertBefore(base, support);
                    cap++;
                }
            }
            if (cap != 1) throw new IllegalStateException("Expected one legacy 1024 support scaling cap: " + cap);
            write(payload, multiplayer);
            multiplayer.methods.removeIf(method -> !method.name.equals("applyHealthAndRestoreDamage"));
            multiplayer.methods.get(0).access = ACC_PUBLIC | ACC_STATIC;
            write(Path.of(arguments[2]), multiplayer);
        }
        System.out.println("PASS manor base health 3000; support bypasses legacy 1024 clamp; player multipliers unchanged");
    }

    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(name + ".class")).readAllBytes()).accept(node, 0);
        return node;
    }

    private static void write(Path root, ClassNode node) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Path output = root.resolve(node.name + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
    }
}
