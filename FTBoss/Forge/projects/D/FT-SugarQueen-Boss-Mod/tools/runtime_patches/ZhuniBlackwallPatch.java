import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class ZhuniBlackwallPatch {
    private static final String PREFIX = "net/farmingtales/ftboss/";

    private static ClassNode read(byte[] bytes) {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, 0);
        return type;
    }

    private static AbstractInsnNode previous(AbstractInsnNode node) {
        do { node = node.getPrevious(); } while (node != null && node.getOpcode() < 0);
        return node;
    }

    private static void save(ClassNode type, Path output) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        byte[] bytes = writer.toByteArray();
        ClassNode checked = read(bytes);
        for (MethodNode method : checked.methods) {
            if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0)
                new Analyzer<>(new BasicVerifier()).analyze(checked.name, method);
        }
        Path file = output.resolve(type.name + ".class");
        Files.createDirectories(file.getParent());
        Files.write(file, bytes);
    }

    public static void main(String[] arguments) throws Exception {
        Path payload = Path.of(arguments[1]);
        try (JarFile baseline = new JarFile(arguments[0])) {
            String sparrowName = PREFIX + "boss/ai/SparrowDisruption";
            ClassNode original = read(baseline.getInputStream(baseline.getJarEntry(sparrowName + ".class")).readAllBytes());
            ClassNode compiled = read(Files.readAllBytes(payload.resolve(sparrowName + ".class")));
            int added = 0;
            for (MethodNode method : compiled.methods) {
                if (!method.name.equals("silenceItems") && !method.name.startsWith("lambda$silenceItems$")) continue;
                if (original.methods.stream().anyMatch(existing -> existing.name.equals(method.name) && existing.desc.equals(method.desc)))
                    throw new IllegalStateException("Already patched: " + method.name);
                original.methods.add(method);
                added++;
            }
            if (added != 2) throw new IllegalStateException("Expected silenceItems and its map factory, got " + added);
            save(original, payload);
            String birdName = PREFIX + "turret/ZhuniEntity";
            ClassNode bird = read(baseline.getInputStream(baseline.getJarEntry(birdName + ".class")).readAllBytes());
            int hooks = 0;
            for (MethodNode method : bird.methods) {
                for (AbstractInsnNode node : method.instructions.toArray()) {
                    if (!(node instanceof MethodInsnNode call) || !call.owner.equals(PREFIX + "turret/ZhuniTurretDrops") || !call.name.equals("tick")) continue;
                    if (!(previous(call) instanceof VarInsnNode level) || level.getOpcode() != Opcodes.ALOAD
                            || !(previous(level) instanceof VarInsnNode boss) || boss.getOpcode() != Opcodes.ALOAD)
                        throw new IllegalStateException("Unexpected turret tick locals");
                    InsnList extra = new InsnList();
                    extra.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    extra.add(new VarInsnNode(Opcodes.ALOAD, boss.var));
                    extra.add(new VarInsnNode(Opcodes.ALOAD, level.var));
                    extra.add(new MethodInsnNode(Opcodes.INVOKESTATIC, PREFIX + "boss/ai/ZhuniBlackwallDisruption", "tick",
                            "(L" + birdName + ";L" + PREFIX + "entity/EasterBossEntity;Lnet/minecraft/server/level/ServerLevel;)V", false));
                    method.instructions.insert(call, extra);
                    hooks++;
                }
            }
            if (hooks != 1) throw new IllegalStateException("Expected one visible combat tick hook, got " + hooks);
            save(bird, payload);
        }
        System.out.println("PASS preserved existing runtime methods; added two silence methods and one combat hook");
    }
}
