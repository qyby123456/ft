import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class ManorCombatHalvingPatch implements Opcodes {
    private static final String ROOT = "net/farmingtales/ftboss/";
    private static final String HOOK = ROOT + "boss/ai/ManorCombatHalving";

    private static void save(ClassNode type, Path payload) throws Exception {
        for (MethodNode method : type.methods) {
            if ((method.access & (ACC_ABSTRACT | ACC_NATIVE)) == 0)
                new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path file = payload.resolve(type.name + ".class");
        Files.createDirectories(file.getParent());
        Files.write(file, writer.toByteArray());
    }

    public static void main(String[] arguments) throws Exception {
        Path payload = Path.of(arguments[3]);
        ClassNode hook = new ClassNode();
        new ClassReader(Files.readAllBytes(Path.of(arguments[1]).resolve(HOOK + ".class")))
                .accept(new ClassRemapper(hook, new ZicaiVoidPatch.Names(Path.of(arguments[2]))), 0);
        save(hook, payload);
        try (ZipFile baseline = new ZipFile(arguments[0])) {
            if (baseline.getEntry(HOOK + ".class") != null) throw new IllegalStateException("Already patched baseline");
            for (String name : new String[]{"BlueFireEffect", "ShadowErosionEffect"}) {
                ClassNode effect = new ClassNode();
                new ClassReader(baseline.getInputStream(baseline.getEntry(ROOT + "effect/" + name + ".class")).readAllBytes())
                        .accept(effect, 0);
                int count = 0;
                for (MethodNode method : effect.methods) {
                    if (!method.name.equals("m_6742_")) continue;
                    for (AbstractInsnNode instruction : method.instructions.toArray()) {
                        if (instruction instanceof MethodInsnNode call && call.name.equals("m_6469_")
                                && call.desc.equals("(Lnet/minecraft/world/damagesource/DamageSource;F)Z")) {
                            method.instructions.insertBefore(call, new MethodInsnNode(INVOKESTATIC, HOOK, "statusDamage", "(F)F", false));
                            count++;
                        }
                    }
                }
                if (count != 1) throw new IllegalStateException(name + " damage calls: " + count);
                save(effect, payload);
            }
        }
        System.out.println("PASS: new balance subscriber and two source-less damage paths");
    }
}
