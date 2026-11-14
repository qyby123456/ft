import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class YouziSupportBalancePatch {
    public static final String ENTRY = "net/farmingtales/ftboss/entity/EasterBossEntity.class";

    public static ClassNode read(ZipFile jar) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getEntry(ENTRY)).readAllBytes()).accept(type, 0);
        return type;
    }

    public static byte[] patch(ClassNode type) throws Exception {
        int fields = 0;
        int instructions = 0;
        for (FieldNode field : type.fields) {
            if (!field.name.equals("YOUZI_MINION_SHARE")) continue;
            if (!Float.valueOf(0.10F).equals(field.value)) throw new IllegalStateException("Unexpected original share field");
            field.value = 0.05F;
            fields++;
        }
        for (MethodNode method : type.methods) {
            if (!method.name.equals("youziMinionShare")) continue;
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof LdcInsnNode literal && Float.valueOf(0.10F).equals(literal.cst)) {
                    literal.cst = 0.05F;
                    instructions++;
                }
            }
        }
        if (fields != 1 || instructions != 1) throw new IllegalStateException("Share patch must change exactly one field and one instruction");
        for (MethodNode method : type.methods) {
            if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0)
                new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
        }
        ClassWriter writer = new ClassWriter(0);
        type.accept(writer);
        return writer.toByteArray();
    }

    public static void main(String[] arguments) throws Exception {
        try (ZipFile baseline = new ZipFile(arguments[0])) {
            byte[] patched = patch(read(baseline));
            Path output = Path.of(arguments[1]).resolve(ENTRY);
            Files.createDirectories(output.getParent());
            Files.write(output, patched);
        }
        System.out.println("PASS: Youzi total clone absorption 10% -> 5%; every other runtime method retained");
    }
}
