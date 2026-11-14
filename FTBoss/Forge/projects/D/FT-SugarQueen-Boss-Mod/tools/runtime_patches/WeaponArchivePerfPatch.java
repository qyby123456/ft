import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.TraceClassVisitor;

public final class WeaponArchivePerfPatch {
    private static final String BASE = "net/farmingtales/ftboss/client/";
    private static final String CACHE = BASE + "StatueWeaponDetailsCache";
    private static final String HUB = BASE + "StatueHubScreen.class";
    private static final String PANEL = BASE + "StatueWeaponDetailsPanel.class";

    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    private static byte[] bytes(JarFile archive, String name) throws Exception {
        return archive.getInputStream(archive.getJarEntry(name)).readAllBytes();
    }

    private static String trace(ClassNode node) {
        StringWriter output = new StringWriter();
        node.accept(new TraceClassVisitor(new PrintWriter(output)));
        return output.toString();
    }

    private static MethodNode method(ClassNode node, String name) {
        var matches = node.methods.stream().filter(member -> member.name.equals(name)).toList();
        if (matches.size() != 1) throw new AssertionError("Expected one " + name);
        return matches.get(0);
    }

    private static void output(ClassNode expected, String name, Path overlay, JarFile candidate) throws Exception {
        if (candidate != null) {
            if (!trace(expected).equals(trace(read(bytes(candidate, name))))) {
                throw new AssertionError("Unrelated bytecode changed in " + name);
            }
        } else {
            ClassWriter writer = new ClassWriter(0);
            expected.accept(writer);
            Path target = overlay.resolve(name);
            Files.createDirectories(target.getParent());
            Files.write(target, writer.toByteArray());
        }
    }

    public static void main(String[] arguments) throws Exception {
        try (JarFile baseline = new JarFile(arguments[0]); JarFile donor = new JarFile(arguments[1]);
             JarFile candidate = arguments.length == 4 ? new JarFile(arguments[3]) : null) {
            Path overlay = Path.of(arguments[2]);
            ClassNode hub = read(bytes(baseline, HUB));
            MethodNode details = method(hub, "weaponDetails");
            if (!details.desc.equals("(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;")) {
                throw new AssertionError("Unexpected weaponDetails signature");
            }
            details.instructions.clear();
            details.tryCatchBlocks.clear();
            details.localVariables = null;
            details.visibleLocalVariableAnnotations = null;
            details.invisibleLocalVariableAnnotations = null;
            details.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            details.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            details.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CACHE, "frame",
                    "(Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;)L" + CACHE + "$Frame;", false));
            details.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, CACHE + "$Frame", "details", "()Ljava/util/List;", false));
            details.instructions.add(new InsnNode(Opcodes.ARETURN));
            details.maxStack = 2;
            details.maxLocals = 2;
            output(hub, HUB, overlay, candidate);

            ClassNode panel = read(bytes(baseline, PANEL));
            MethodNode oldRender = method(panel, "render");
            MethodNode render = method(read(bytes(donor, PANEL)), "render");
            if (!oldRender.desc.equals(render.desc)) throw new AssertionError("Render signature changed");
            panel.methods.set(panel.methods.indexOf(oldRender), render);
            output(panel, PANEL, overlay, candidate);
            for (String suffix : new String[] {"", "$Card", "$Frame"}) {
                String name = CACHE + suffix + ".class";
                byte[] content = bytes(donor, name);
                if (candidate != null) {
                    if (!java.util.Arrays.equals(content, bytes(candidate, name))) throw new AssertionError(name);
                } else {
                    Path target = overlay.resolve(name);
                    Files.createDirectories(target.getParent());
                    Files.write(target, content);
                }
            }
            System.out.println("PASS archive cache patch: only weaponDetails/render replaced; all other methods preserved");
        }
    }
}
