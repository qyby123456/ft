import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** 对现用字节码仅修补封印槽就绪规则和屠夫材料识别，保留其他运行时差异。 */
public final class StatueEvolutionBowPatch implements Opcodes {
    private static final String BASE = "net/farmingtales/ftboss/";
    private static final String STATUS = BASE + "util/foundations/FourWeaponFoundationRules$Status";
    private static final String EVOLUTION = BASE + "item/FoodWeaponEvolution";
    private static final String FOOD = EVOLUTION + "$FoodType";

    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(name + ".class")).readAllBytes()).accept(type, 0);
        return type;
    }

    private static MethodNode method(ClassNode type, String name) {
        return type.methods.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
    }

    private static void write(Path output, ClassNode type) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path path = output.resolve(type.name + ".class");
        Files.createDirectories(path.getParent());
        Files.write(path, writer.toByteArray());
    }

    public static void main(String[] arguments) throws Exception {
        try (JarFile baseline = new JarFile(arguments[0])) {
            Path output = Path.of(arguments[1]);
            ClassNode status = read(baseline, STATUS);
            MethodNode ready = method(status, "ready");
            int sealedReads = 0;
            for (var instruction : ready.instructions) {
                if (instruction instanceof FieldInsnNode field && field.name.equals("sealed")) sealedReads++;
            }
            if (sealedReads != 1) throw new IllegalStateException("Unexpected readiness baseline");
            ready.instructions.clear();
            ready.tryCatchBlocks.clear();
            ready.localVariables = null;
            ready.instructions.add(new VarInsnNode(ALOAD, 0));
            ready.instructions.add(new FieldInsnNode(GETFIELD, STATUS, "missing", "Ljava/util/List;"));
            ready.instructions.add(new VarInsnNode(ALOAD, 0));
            ready.instructions.add(new FieldInsnNode(GETFIELD, STATUS, "unsupported", "Ljava/util/List;"));
            ready.instructions.add(new MethodInsnNode(INVOKESTATIC, BASE + "util/foundations/FoundationReadinessRules",
                    "ready", "(Ljava/util/List;Ljava/util/List;)Z", false));
            ready.instructions.add(new InsnNode(IRETURN));
            write(output, status);

            ClassNode evolution = read(baseline, EVOLUTION);
            MethodNode offering = method(evolution, "explicitOfferingType");
            for (var instruction : offering.instructions) {
                if (instruction instanceof LdcInsnNode constant && "ftboss:butcher_material".equals(constant.cst)) {
                    throw new IllegalStateException("Butcher offering patch already present");
                }
            }
            LabelNode original = new LabelNode();
            InsnList prefix = new InsnList();
            prefix.add(new LdcInsnNode("ftboss:butcher_material"));
            prefix.add(new VarInsnNode(ALOAD, 0));
            prefix.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false));
            prefix.add(new JumpInsnNode(IFEQ, original));
            prefix.add(new FieldInsnNode(GETSTATIC, FOOD, "MEAT", "L" + FOOD + ";"));
            prefix.add(new InsnNode(ARETURN));
            prefix.add(original);
            prefix.add(new org.objectweb.asm.tree.FrameNode(F_SAME, 0, null, 0, null));
            offering.instructions.insert(prefix);
            write(output, evolution);
        }
        System.out.println("StatueEvolutionBowPatch PASS methods=2");
    }
}
