import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/** 只在既有构筑读取入口添加展示域复用；保留原始方法名、校验体及全部非展示调用。 */
public final class PatchTooltipReadScope {
    public static final String ADAPTER = "net/farmingtales/ftboss/item/FourWeaponFoundationAdapter";
    public static final String SCOPE = "net/farmingtales/ftboss/item/WeaponTooltipReadScope";
    public static final String SWORD = "net/farmingtales/ftboss/item/SwordCavityBuild";
    public static final String VIEW = ADAPTER + "$View";
    public static final String STACK = "Lnet/minecraft/world/item/ItemStack;";

    public static void main(String[] arguments) throws Exception {
        try (var archive = new JarFile(arguments[0])) {
            patch(archive, Path.of(arguments[1]), ADAPTER, "read", VIEW, "cached", "remember", 3);
            patch(archive, Path.of(arguments[1]), SWORD, "cells", "java/util/List", "cachedCells", "rememberCells", 1);
        }
    }

    private static void patch(JarFile archive, Path outputRoot, String owner, String name, String result,
                              String lookup, String remember, int expectedReturns) throws Exception {
        var type = new ClassNode();
        new ClassReader(archive.getInputStream(archive.getJarEntry(owner + ".class")).readAllBytes()).accept(type, 0);
        var read = type.methods.stream().filter(method -> method.name.equals(name)
                && method.desc.equals("(" + STACK + ")L" + result + ";")).findFirst().orElseThrow();
        for (var instruction : read.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(SCOPE)) {
                throw new IllegalStateException("Tooltip scope already installed");
            }
        }
        int returns = 0;
        for (var instruction : read.instructions.toArray()) {
            if (instruction.getOpcode() != Opcodes.ARETURN) continue;
            var store = new InsnList();
            store.add(new VarInsnNode(Opcodes.ALOAD, 0));
            store.add(new InsnNode(Opcodes.SWAP));
            store.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SCOPE, remember,
                    "(" + STACK + "L" + result + ";)L" + result + ";", false));
            read.instructions.insertBefore(instruction, store);
            returns++;
        }
        if (returns != expectedReturns) throw new IllegalStateException("Unexpected returns: " + owner + ": " + returns);
        var prefix = new InsnList();
        var miss = new LabelNode();
        prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
        prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SCOPE, lookup, "(" + STACK + ")L" + result + ";", false));
        prefix.add(new InsnNode(Opcodes.DUP));
        prefix.add(new JumpInsnNode(Opcodes.IFNULL, miss));
        prefix.add(new InsnNode(Opcodes.ARETURN));
        prefix.add(miss);
        prefix.add(new FrameNode(Opcodes.F_SAME1, 0, null, 1, new Object[] { result }));
        prefix.add(new InsnNode(Opcodes.POP));
        read.instructions.insert(prefix);
        var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        var output = outputRoot.resolve(owner + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
        System.out.println("PASS tooltip scope: " + owner + "." + name + ", " + returns + " original returns; original body retained");
    }
}
