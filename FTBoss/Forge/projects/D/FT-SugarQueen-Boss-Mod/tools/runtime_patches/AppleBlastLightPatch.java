import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class AppleBlastLightPatch {
    public static final String BOSS = "net/farmingtales/ftboss/entity/AppleBossEntity";
    public static final String CLONE = "net/farmingtales/ftboss/entity/AppleCloneEntity";
    public static final String NETWORK = "net/farmingtales/ftboss/network/AppleBlastNetwork";
    private static final String FX = "net/farmingtales/ftboss/util/Fx";
    private static final String PARTICLES = "net/minecraft/core/particles/ParticleTypes";

    public static ClassNode read(Path jar, String owner) throws Exception {
        try (ZipFile archive = new ZipFile(jar.toFile())) {
            return read(archive.getInputStream(archive.getEntry(owner + ".class")).readAllBytes());
        }
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, 0);
        return type;
    }

    public static MethodNode method(ClassNode type, String name) {
        List<MethodNode> matches = type.methods.stream().filter(candidate -> candidate.name.equals(name)).toList();
        if (matches.size() != 1) throw new IllegalStateException("Expected unique method " + name);
        return matches.get(0);
    }

    private static boolean particles(MethodInsnNode call) {
        return call.owner.equals(FX) && call.name.equals("particles")
                || call.owner.equals("net/minecraft/server/level/ServerLevel") && call.name.equals("m_8767_");
    }

    private static List<AbstractInsnNode> statement(MethodInsnNode call) {
        AbstractInsnNode start = call;
        while (!(start instanceof VarInsnNode variable && variable.getOpcode() == Opcodes.ALOAD && variable.var == 1)) {
            start = start.getPrevious();
            if (start == null || start instanceof MethodInsnNode) throw new IllegalStateException("Unexpected particle expression");
        }
        AbstractInsnNode end = call;
        if (call.desc.endsWith(")I")) {
            end = call.getNext();
            while (end.getOpcode() < 0) end = end.getNext();
            if (end.getOpcode() != Opcodes.POP) throw new IllegalStateException("Expected unused particle return");
        }
        List<AbstractInsnNode> nodes = new ArrayList<>();
        for (AbstractInsnNode node = start; ; node = node.getNext()) {
            if (node.getOpcode() >= 0) nodes.add(node);
            if (node == end) return nodes;
        }
    }

    public static void stripBlastVisuals(MethodNode method) {
        for (AbstractInsnNode node : method.instructions.toArray()) {
            if (node instanceof MethodInsnNode call && (particles(call) || call.owner.equals(NETWORK) && call.name.equals("show"))) {
                for (AbstractInsnNode part : statement(call)) method.instructions.remove(part);
            }
        }
    }

    private static void blast(MethodNode method) {
        List<MethodInsnNode> calls = Arrays.stream(method.instructions.toArray())
                .filter(node -> node instanceof MethodInsnNode call && particles(call))
                .map(node -> (MethodInsnNode) node).toList();
        if (calls.size() != 3) throw new IllegalStateException("Expected emitter and two particle batches in " + method.name);
        InsnList light = new InsnList();
        light.add(new VarInsnNode(Opcodes.ALOAD, 1));
        light.add(new VarInsnNode(Opcodes.DLOAD, 6));
        light.add(new VarInsnNode(Opcodes.DLOAD, 8));
        light.add(new VarInsnNode(Opcodes.DLOAD, 10));
        light.add(new VarInsnNode(Opcodes.DLOAD, 3));
        light.add(new MethodInsnNode(Opcodes.INVOKESTATIC, NETWORK, "show",
                "(Lnet/minecraft/server/level/ServerLevel;DDDD)V", false));
        method.instructions.insertBefore(statement(calls.get(0)).get(0), light);
        for (MethodInsnNode call : calls) {
            for (AbstractInsnNode part : statement(call)) method.instructions.remove(part);
        }
    }

    public static void trail(MethodNode method, int oldCount, int newCount, String oldType, String newType) {
        int changed = 0;
        for (AbstractInsnNode node : method.instructions.toArray()) {
            if (!(node instanceof FieldInsnNode field) || !field.owner.equals(PARTICLES) || !field.name.equals(oldType)) continue;
            AbstractInsnNode previous = field.getPrevious();
            while (previous.getOpcode() < 0) previous = previous.getPrevious();
            int count = previous instanceof IntInsnNode number ? number.operand
                    : previous instanceof LdcInsnNode constant && constant.cst instanceof Integer number ? number
                    : previous.getOpcode() - Opcodes.ICONST_0;
            if (count != oldCount) throw new IllegalStateException("Unexpected trail count in " + method.name);
            method.instructions.set(previous, newCount <= 5 ? new InsnNode(Opcodes.ICONST_0 + newCount)
                    : new IntInsnNode(Opcodes.BIPUSH, newCount));
            field.name = newType;
            changed++;
        }
        if (changed != 1) throw new IllegalStateException("Expected one trail batch in " + method.name);
    }

    public static byte[] fingerprint(MethodNode source) {
        ClassNode holder = new ClassNode();
        holder.version = Opcodes.V17;
        holder.access = Opcodes.ACC_PUBLIC;
        holder.name = "Fingerprint";
        holder.superName = "java/lang/Object";
        MethodNode copy = new MethodNode(source.access, source.name, source.desc, source.signature,
                source.exceptions.toArray(String[]::new));
        source.accept(copy);
        for (AbstractInsnNode node : copy.instructions.toArray()) {
            if (node.getType() == AbstractInsnNode.LINE || node.getType() == AbstractInsnNode.FRAME) copy.instructions.remove(node);
        }
        copy.localVariables = null;
        copy.maxStack = 0;
        copy.maxLocals = 0;
        holder.methods.add(copy);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        holder.accept(writer);
        return writer.toByteArray();
    }

    private static void save(ClassNode type, Path payload) throws Exception {
        for (MethodNode method : type.methods) {
            if ((method.access & (Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) == 0) new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path output = payload.resolve(type.name + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
    }

    private static final class RuntimeNames extends Remapper {
        private final Map<String, String> methods = new HashMap<>();
        private final Map<String, String> fields = new HashMap<>();
        private final Map<String, ClassNode> hierarchy = new HashMap<>();

        private RuntimeNames(Path mappings) throws Exception {
            String owner = null;
            for (String line : Files.readAllLines(mappings)) {
                if (line.isBlank() || line.startsWith("tsrg2")) continue;
                String[] parts = line.trim().split("\\s+");
                if (!line.startsWith("\t")) owner = parts[0];
                else if (!line.startsWith("\t\t")) {
                    if (parts.length == 3 && parts[1].startsWith("(")) methods.put(owner + "/" + parts[0] + parts[1], parts[2]);
                    else if (parts.length == 2) fields.put(owner + "/" + parts[0], parts[1]);
                }
            }
        }

        private ClassNode type(String owner) {
            if (hierarchy.containsKey(owner)) return hierarchy.get(owner);
            try (var input = AppleBlastLightPatch.class.getClassLoader().getResourceAsStream(owner + ".class")) {
                ClassNode type = null;
                if (input != null) {
                    type = new ClassNode();
                    new ClassReader(input.readAllBytes()).accept(type, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                }
                hierarchy.put(owner, type);
                return type;
            } catch (Exception error) {
                throw new IllegalStateException(owner, error);
            }
        }

        private String resolve(Map<String, String> names, String owner, String member) {
            String mapped = names.get(owner + "/" + member);
            if (mapped != null) return mapped;
            ClassNode type = type(owner);
            if (type == null || owner.startsWith("java/")) return null;
            if (type.superName != null) {
                mapped = resolve(names, type.superName, member);
                if (mapped != null) return mapped;
            }
            for (String parent : type.interfaces) {
                mapped = resolve(names, parent, member);
                if (mapped != null) return mapped;
            }
            return null;
        }

        @Override
        public String mapMethodName(String owner, String name, String descriptor) {
            String mapped = resolve(methods, owner, name + descriptor);
            return mapped == null ? name : mapped;
        }

        @Override
        public String mapFieldName(String owner, String name, String descriptor) {
            String mapped = resolve(fields, owner, name);
            return mapped == null ? name : mapped;
        }
    }

    public static void main(String[] arguments) throws Exception {
        Path baseline = Path.of(arguments[0]);
        Path compiled = Path.of(arguments[1]);
        Path payload = Path.of(arguments[3]);
        RuntimeNames names = new RuntimeNames(Path.of(arguments[2]));
        String crit = names.mapFieldName(PARTICLES, "CRIT", "");
        String cloud = names.mapFieldName(PARTICLES, "CLOUD", "");
        String endRod = names.mapFieldName(PARTICLES, "END_ROD", "");
        ClassNode boss = read(baseline, BOSS);
        blast(method(boss, "explosionStage"));
        trail(method(boss, "startCharge"), 20, 4, crit, crit);
        trail(method(boss, "tickCharge"), 8, 1, cloud, endRod);
        save(boss, payload);
        ClassNode clone = read(baseline, CLONE);
        blast(method(clone, "detonate"));
        trail(method(clone, "m_8119_"), 5, 1, cloud, endRod);
        save(clone, payload);
        try (var paths = Files.walk(compiled.resolve("net/farmingtales/ftboss"))) {
            for (Path file : paths.filter(path -> path.toString().endsWith(".class")).toList()) {
                ClassNode output = new ClassNode();
                new ClassReader(Files.readAllBytes(file)).accept(new ClassRemapper(output, names), 0);
                save(output, payload);
            }
        }
        System.out.println("PASS: five visual-only methods patched; independent light classes remapped to runtime names");
    }
}
