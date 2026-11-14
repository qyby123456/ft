import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class PatchManorDamageLimits implements Opcodes {
    private static final String LIMITER = "net/farmingtales/ftboss/boss/ai/ManorDamageLimit";
    private static final String LIVING = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final String SOURCE = "Lnet/minecraft/world/damagesource/DamageSource;";
    private static final String ENTRY = "net/farmingtales/ftboss/boss/ai/ManorFinalDamage.class";

    public static void main(String[] arguments) throws Exception {
        ClassNode node = new ClassNode();
        try (JarFile baseline = new JarFile(arguments[0])) {
            new ClassReader(baseline.getInputStream(baseline.getJarEntry(ENTRY)).readAllBytes())
                    .accept(node, ClassReader.EXPAND_FRAMES);
        }
        int preliminary = 0;
        int settled = 0;
        for (var method : node.methods) {
            if (!method.name.equals("settle")) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode call)) continue;
                if (call.owner.equals("net/farmingtales/ftboss/util/ManorSpellRules") && call.name.equals("cap")) {
                    if (!(call.getPrevious() instanceof VarInsnNode load) || load.getOpcode() != FLOAD
                            || !(load.getPrevious() instanceof VarInsnNode source) || source.getOpcode() != ALOAD || source.var != 1
                            || !(source.getPrevious() instanceof VarInsnNode boss) || boss.getOpcode() != ALOAD || boss.var != 0) {
                        throw new IllegalStateException("Unexpected preliminary spell cap inputs");
                    }
                    LabelNode original = new LabelNode();
                    LabelNode after = new LabelNode();
                    InsnList bypass = new InsnList();
                    bypass.add(new VarInsnNode(ALOAD, 0));
                    bypass.add(new MethodInsnNode(INVOKESTATIC, LIMITER, "applies", "(" + LIVING + ")Z", false));
                    bypass.add(new JumpInsnNode(IFEQ, original));
                    bypass.add(new VarInsnNode(FLOAD, load.var));
                    bypass.add(new JumpInsnNode(GOTO, after));
                    bypass.add(original);
                    method.instructions.insertBefore(boss, bypass);
                    method.instructions.insert(call, after);
                    preliminary++;
                }
                if (call.owner.equals("net/minecraftforge/event/entity/living/LivingDamageEvent") && call.name.equals("getAmount")) {
                    if (!(call.getNext() instanceof VarInsnNode store) || store.getOpcode() != FSTORE) {
                        throw new IllegalStateException("Unexpected final event damage store");
                    }
                    InsnList limit = new InsnList();
                    limit.add(new VarInsnNode(ALOAD, 0));
                    limit.add(new VarInsnNode(ALOAD, 1));
                    limit.add(new VarInsnNode(FLOAD, store.var));
                    limit.add(new MethodInsnNode(INVOKESTATIC, LIMITER, "limit", "(" + LIVING + SOURCE + "F)F", false));
                    limit.add(new VarInsnNode(FSTORE, store.var));
                    method.instructions.insert(store, limit);
                    settled++;
                }
            }
        }
        if (preliminary != 1 || settled != 1) throw new IllegalStateException("Settlement hook count mismatch");
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Path output = Path.of(arguments[1]).resolve(ENTRY);
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
        System.out.println("PASS manor settlement patch: conditional legacy spell cap and one final damage limiter");
        patchSharing(Path.of(arguments[0]), Path.of(arguments[1]));
    }

    private static void patchSharing(Path baselinePath, Path payload) throws Exception {
        String entry = "net/farmingtales/ftboss/entity/EasterBossEntity.class";
        ClassNode node = new ClassNode();
        try (JarFile baseline = new JarFile(baselinePath.toFile())) {
            new ClassReader(baseline.getInputStream(baseline.getJarEntry(entry)).readAllBytes()).accept(node, 0);
        }
        int patched = 0;
        for (var method : node.methods) {
            if (!method.name.equals("sparrowShare")) continue;
            for (var instruction : method.instructions.toArray()) {
                if (instruction.getOpcode() != FSUB || !(instruction.getPrevious() instanceof VarInsnNode share)
                        || share.getOpcode() != FLOAD || !(share.getPrevious() instanceof MethodInsnNode health)
                        || !health.name.equals("m_21223_") || !(health.getPrevious() instanceof VarInsnNode bird)
                        || bird.getOpcode() != ALOAD) continue;
                InsnList limit = new InsnList();
                limit.add(new VarInsnNode(ALOAD, bird.var));
                limit.add(new VarInsnNode(ALOAD, 2));
                limit.add(new MethodInsnNode(INVOKESTATIC, LIMITER, "sharedHealth", "(FF" + LIVING + SOURCE + ")F", false));
                method.instructions.insertBefore(instruction, limit);
                method.instructions.remove(instruction);
                patched++;
            }
        }
        if (patched != 1) throw new IllegalStateException("Sparrow shared health hook count mismatch: " + patched);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Path output = payload.resolve(entry);
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
        System.out.println("PASS sparrow sharing patch: direct shared health damage uses the same boss budget");
    }
}
