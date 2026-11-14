import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class PatchManorImpossibleHealth implements Opcodes {
    private static final String RAID = "net/farmingtales/ftboss/boss/ai/ManorRaidCombat";

    public static void main(String[] arguments) throws Exception {
        try (JarFile baseline = new JarFile(arguments[0])) {
            ClassNode multiplayer = read(baseline, "net/farmingtales/ftboss/boss/ai/ManorMultiplayerBalance");
            int patched = 0;
            for (var method : multiplayer.methods) {
                if (!method.name.equals("applyHealthAndRestoreDamage")) continue;
                for (var instruction : method.instructions) {
                    if (instruction instanceof MethodInsnNode call && call.owner.equals(RAID)
                            && call.name.equals("scaleMaximumHealth")) {
                        throw new IllegalStateException("Impossible health multiplier is already integrated");
                    }
                }
                for (var instruction : method.instructions.toArray()) {
                    if (instruction.getOpcode() != DMUL
                            || !(instruction.getPrevious() instanceof VarInsnNode multiplier)
                            || multiplier.getOpcode() != DLOAD || multiplier.var != 1
                            || !(multiplier.getPrevious() instanceof VarInsnNode base) || base.getOpcode() != DLOAD
                            || !(instruction.getNext() instanceof VarInsnNode target) || target.getOpcode() != DSTORE) continue;
                    InsnList difficulty = new InsnList();
                    difficulty.add(new VarInsnNode(ALOAD, 0));
                    difficulty.add(new MethodInsnNode(INVOKESTATIC, RAID, "scaleMaximumHealth",
                            "(DLnet/minecraft/world/entity/LivingEntity;)D", false));
                    method.instructions.insert(instruction, difficulty);
                    patched++;
                }
            }
            if (patched != 1) throw new IllegalStateException("Expected one multiplayer health calculation: " + patched);
            write(Path.of(arguments[1]), multiplayer);
            multiplayer.methods.removeIf(method -> !method.name.equals("applyHealthAndRestoreDamage"));
            multiplayer.methods.get(0).access = ACC_PUBLIC | ACC_STATIC;
            write(Path.of(arguments[2]), multiplayer);
            ClassNode raid = read(baseline, RAID);
            raid.methods.removeIf(method -> !Set.of("mainFight", "scaleMaximumHealth").contains(method.name));
            write(Path.of(arguments[2]), raid);
        }
        System.out.println("PASS impossible health x4 integrated after multiplayer scaling; existing raid exclusions retained");
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
