import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class PatchSparrowEntitySnapshot {
    private static final String ENTRY = "net/farmingtales/ftboss/boss/SparrowSupport.class";
    private static final Map<String, String> METHODS = Map.of(
            "beginStage", "(Lnet/minecraft/server/level/ServerLevel;Lnet/farmingtales/ftboss/entity/EasterBossEntity;)V",
            "endStage", "(Lnet/minecraft/server/level/ServerLevel;Ljava/util/UUID;)V",
            "countAliveSparrows", "(Lnet/minecraft/server/MinecraftServer;)I");

    public static void main(String[] arguments) throws Exception {
        ClassReader reader;
        try (JarFile archive = new JarFile(arguments[0])) {
            reader = new ClassReader(archive.getInputStream(archive.getJarEntry(ENTRY)).readAllBytes());
        }
        ClassNode type = new ClassNode();
        reader.accept(type, 0);
        int patched = 0;
        for (MethodNode method : type.methods) {
            int traversals = 0;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode call)
                        || !call.owner.equals("net/minecraft/server/level/ServerLevel")
                        || !call.name.equals("m_8583_") || !call.desc.equals("()Ljava/lang/Iterable;")) continue;
                require(method.desc.equals(METHODS.get(method.name)), "Unexpected entity traversal: " + method.name);
                AbstractInsnNode next = call.getNext();
                while (next != null && next.getOpcode() < 0) next = next.getNext();
                require(next instanceof MethodInsnNode iterator && iterator.owner.equals("java/lang/Iterable")
                        && iterator.name.equals("iterator") && iterator.desc.equals("()Ljava/util/Iterator;"),
                        "Traversal already patched or changed: " + method.name);
                method.instructions.insert(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/google/common/collect/ImmutableList", "copyOf",
                        "(Ljava/lang/Iterable;)Lcom/google/common/collect/ImmutableList;", false));
                traversals++;
            }
            if (METHODS.containsKey(method.name)) {
                require(traversals == 1, "Expected one traversal in " + method.name);
                patched++;
            }
        }
        require(patched == METHODS.size(), "Missing expected traversal methods");
        ClassWriter writer = new ClassWriter(reader, 0);
        type.accept(writer);
        Path output = Path.of(arguments[1]).resolve(ENTRY);
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
        System.out.println("PATCHED: three SparrowSupport traversals snapshot entities before removal");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
