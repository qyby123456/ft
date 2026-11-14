import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class BossBarReflectionPatch {
    public static final String OWNER = "net/farmingtales/ftboss/client/BossBarOverlay";
    public static final String ENTRY = OWNER + ".class";
    public static final String HELPER = "net/minecraftforge/fml/util/ObfuscationReflectionHelper";
    public static final String FIND_DESC = "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;";

    public static ClassNode read(Path jar) throws Exception {
        try (ZipFile archive = new ZipFile(jar.toFile())) {
            ClassNode type = new ClassNode();
            new ClassReader(archive.getInputStream(archive.getEntry(ENTRY)).readAllBytes()).accept(type, 0);
            return type;
        }
    }

    public static MethodNode method(ClassNode type) {
        return type.methods.stream().filter(candidate -> candidate.name.equals("eventsRaw")
                && candidate.desc.equals("()Ljava/util/Map;")).findFirst().orElseThrow();
    }

    public static byte[] bytes(ClassNode type) {
        ClassWriter writer = new ClassWriter(0);
        type.accept(writer);
        return writer.toByteArray();
    }

    public static int change(ClassNode type, boolean revert) {
        int changed = 0;
        for (AbstractInsnNode instruction : method(type).instructions) {
            if (!(instruction instanceof MethodInsnNode call)) {
                continue;
            }
            boolean old = call.owner.equals("java/lang/Class") && call.name.equals("getDeclaredField")
                    && call.desc.equals("(Ljava/lang/String;)Ljava/lang/reflect/Field;")
                    && call.getOpcode() == Opcodes.INVOKEVIRTUAL && !call.itf;
            boolean fixed = call.owner.equals(HELPER) && call.name.equals("findField")
                    && call.desc.equals(FIND_DESC) && call.getOpcode() == Opcodes.INVOKESTATIC && !call.itf;
            if (!old && !fixed) {
                continue;
            }
            if (!(call.getPrevious() instanceof LdcInsnNode literal)
                    || !literal.cst.equals(old ? "events" : "f_93699_")) {
                throw new IllegalStateException("Unexpected reflection layout; re-audit baseline");
            }
            literal.cst = revert ? "events" : "f_93699_";
            call.owner = revert ? "java/lang/Class" : HELPER;
            call.name = revert ? "getDeclaredField" : "findField";
            call.desc = revert ? "(Ljava/lang/String;)Ljava/lang/reflect/Field;" : FIND_DESC;
            call.setOpcode(revert ? Opcodes.INVOKEVIRTUAL : Opcodes.INVOKESTATIC);
            changed++;
        }
        if (changed != 1) {
            throw new IllegalStateException("Expected exactly one reflection lookup, found " + changed);
        }
        return changed;
    }

    public static void main(String[] arguments) throws Exception {
        Path baseline = Path.of(arguments[0]);
        ClassNode original = read(baseline);
        byte[] canonicalOriginal = bytes(original);
        ClassNode patched = read(baseline);
        change(patched, false);
        byte[] result = bytes(patched);
        ClassNode repeated = new ClassNode();
        new ClassReader(result).accept(repeated, 0);
        change(repeated, false);
        if (!Arrays.equals(result, bytes(repeated))) {
            throw new AssertionError("Non-idempotent reflection patch");
        }
        ClassNode restored = new ClassNode();
        new ClassReader(result).accept(restored, 0);
        change(restored, true);
        ClassNode normalizedOriginal = read(baseline);
        change(normalizedOriginal, true);
        if (!Arrays.equals(bytes(normalizedOriginal), bytes(restored))) {
            throw new AssertionError("Unexpected method, field, annotation or frame changes");
        }
        Path output = Path.of(arguments[1]).resolve(ENTRY);
        Files.createDirectories(output.getParent());
        Files.write(output, result);
        System.out.println("PASS exact two-instruction patch; other " + (original.methods.size() - 1)
                + " methods and all fields/frames/metadata preserved; idempotent=true; baselineAlreadyCorrect="
                + Arrays.equals(canonicalOriginal, result));
    }
}
