import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class FirstPersonSpellPatch {
    private static final String RENDER = "net/farmingtales/ftboss/client/render/";
    private static final String HELPER = RENDER + "FirstPersonSpellVisibility";

    private FirstPersonSpellPatch() {}

    public static void main(String[] arguments) throws Exception {
        Path baseline = Path.of(arguments[0]), output = Path.of(arguments[1]);
        Map<String, byte[]> replacements = new LinkedHashMap<>();
        try (ZipFile archive = new ZipFile(baseline.toFile())) {
            ClassNode safety = read(archive, RENDER + "OptimizedSpellSafety");
            change(method(safety, "cameraOpacity"), 0.55, 0.30, 1);
            change(method(safety, "cameraOpacity"), 1.10, 0.70, 1);
            save(replacements, safety);
            ClassNode transform = read(archive, RENDER + "OptimizedSpellRenderer$Transform");
            change(method(transform, "hullOpacity"), 0.80, 0.50, 1);
            save(replacements, transform);
            ClassNode dandan = read(archive, RENDER + "dandan/DandanMotion");
            change(method(dandan, "blocksSight"), 3.0, 1.8, 1);
            change(method(dandan, "blocksSight"), 0.06, 0.03, 4);
            save(replacements, dandan);
            ClassNode zheergen = read(archive, RENDER + "ZheergenSpellOcclusion");
            change(method(zheergen, "sight"), 6.0, 3.0, 1);
            change(method(zheergen, "sight"), 0.10, 0.06, 1);
            change(method(zheergen, "sight"), 0.055, 0.035, 1);
            save(replacements, zheergen);
            ClassNode maque = read(archive, "net/farmingtales/ftboss/client/maque/MaqueMotion");
            delegate(method(maque, "cameraVisibility"), "featherOpacity", Opcodes.DRETURN, 4, 3);
            delegate(method(maque, "intersectsViewRay"), "intersectsNearViewRay", Opcodes.IRETURN, 3, 3);
            save(replacements, maque);
            ClassNode xueli = read(archive, "net/farmingtales/ftboss/client/xueli/XueliGuard");
            delegate(method(xueli, "opacity"), "forgeOpacity", Opcodes.DRETURN, 5, 3);
            save(replacements, xueli);
        }
        for (var replacement : replacements.entrySet()) {
            Path target = output.resolve(replacement.getKey());
            Files.createDirectories(target.getParent());
            Files.write(target, replacement.getValue());
            System.out.println("PATCH " + replacement.getKey());
        }
    }

    private static ClassNode read(ZipFile archive, String name) throws IOException {
        var entry = archive.getEntry(name + ".class");
        if (entry == null) throw new IOException("Missing runtime class: " + name);
        ClassNode result = new ClassNode();
        try (var stream = archive.getInputStream(entry)) { new ClassReader(stream.readAllBytes()).accept(result, 0); }
        return result;
    }

    private static MethodNode method(ClassNode type, String name) {
        var matches = type.methods.stream().filter(value -> value.name.equals(name)).toList();
        if (matches.size() != 1) throw new IllegalStateException("Ambiguous or missing method: " + type.name + "." + name);
        return matches.get(0);
    }

    private static void change(MethodNode method, double previous, double next, int expected) {
        int found = 0, already = 0;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof LdcInsnNode constant && constant.cst instanceof Double value) {
                if (value == previous) { constant.cst = next; found++; }
                else if (value == next) already++;
            }
        }
        if (found != expected && !(found == 0 && already >= expected)) {
            throw new IllegalStateException("Unexpected constants in " + method.name + ": " + previous + ", found " + found);
        }
    }

    private static void delegate(MethodNode method, String target, int returnOpcode, int arguments, int references) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        if (method.localVariables != null) method.localVariables.clear();
        method.visibleLocalVariableAnnotations = null;
        method.invisibleLocalVariableAnnotations = null;
        for (int index = 0; index < arguments; index++) {
            method.instructions.add(new VarInsnNode(index < references ? Opcodes.ALOAD : Opcodes.ILOAD, index));
        }
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, target, method.desc, false));
        method.instructions.add(new InsnNode(returnOpcode));
        method.maxStack = arguments;
        method.maxLocals = arguments;
    }

    private static void save(Map<String, byte[]> replacements, ClassNode type) {
        try {
            for (MethodNode method : type.methods) {
                if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0) {
                    new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Invalid runtime bytecode: " + type.name, exception);
        }
        ClassWriter writer = new ClassWriter(0);
        type.accept(writer);
        replacements.put(type.name + ".class", writer.toByteArray());
    }
}
