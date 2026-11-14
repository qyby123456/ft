import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class ZicaiVoidPatch {
    private static final String SPELLS = "net/farmingtales/ftboss/integ/spells/";
    private static final String KIND = "net/farmingtales/ftboss/util/ManorSpellRules$Kind";

    private static void save(ClassNode type, Path payload) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path file = payload.resolve(type.name + ".class");
        Files.createDirectories(file.getParent());
        Files.write(file, writer.toByteArray());
    }

    private static ClassNode read(ZipFile archive, String name) throws Exception {
        ClassNode type = new ClassNode();
        try (var input = archive.getInputStream(archive.getEntry(name + ".class"))) {
            new ClassReader(input.readAllBytes()).accept(type, 0);
        }
        return type;
    }

    private static void hook(ClassNode type, String name, String descriptor, int count, int integer) {
        MethodNode method = type.methods.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
        if (!method.desc.equals(descriptor.replace(")Z", ")V"))) throw new IllegalStateException("Unexpected hook signature: " + name);
        for (var instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(SPELLS + "ZicaiVoidSpells"))
                throw new IllegalStateException("Already patched: " + name);
        }
        InsnList prefix = new InsnList();
        for (int slot = 0; slot < count; slot++) prefix.add(new VarInsnNode(slot == integer ? Opcodes.ILOAD : Opcodes.ALOAD, slot));
        prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SPELLS + "ZicaiVoidSpells", name, descriptor, false));
        LabelNode original = new LabelNode();
        prefix.add(new JumpInsnNode(Opcodes.IFEQ, original));
        prefix.add(new InsnNode(Opcodes.RETURN));
        prefix.add(original);
        prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(prefix);
    }

    public static final class Names extends Remapper {
        private final Map<String, String> methods = new HashMap<>();
        private final Map<String, String> fields = new HashMap<>();
        private final Map<String, ClassNode> hierarchy = new HashMap<>();

        public Names(Path mappings) throws Exception {
            String owner = "";
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
            try (var input = getClass().getClassLoader().getResourceAsStream(owner + ".class")) {
                ClassNode type = null;
                if (input != null) {
                    type = new ClassNode();
                    new ClassReader(input.readAllBytes()).accept(type, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                }
                hierarchy.put(owner, type);
                return type;
            } catch (Exception error) { throw new IllegalStateException(owner, error); }
        }

        private String resolve(Map<String, String> entries, String owner, String member) {
            String result = entries.get(owner + "/" + member);
            if (result != null) return result;
            ClassNode type = type(owner);
            if (type == null || owner.startsWith("java/")) return null;
            if (type.superName != null && (result = resolve(entries, type.superName, member)) != null) return result;
            for (String parent : type.interfaces) if ((result = resolve(entries, parent, member)) != null) return result;
            return null;
        }

        @Override public String mapMethodName(String owner, String name, String descriptor) {
            String result = resolve(methods, owner, name + descriptor);
            return result == null ? name : result;
        }

        @Override public String mapFieldName(String owner, String name, String descriptor) {
            String result = resolve(fields, owner, name);
            return result == null ? name : result;
        }
    }

    public static void main(String[] arguments) throws Exception {
        Path compiled = Path.of(arguments[1]);
        Path payload = Path.of(arguments[3]);
        Names names = new Names(Path.of(arguments[2]));
        try (ZipFile archive = new ZipFile(arguments[0])) {
            ClassNode effects = read(archive, SPELLS + "ManorSpellEffects");
            hook(effects, "warning", "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;L"
                    + KIND + ";Lnet/minecraft/world/phys/Vec3;)Z", 4, -1);
            hook(effects, "cast", "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;L"
                    + SPELLS + "ManorSignatureSpell;ILnet/minecraft/world/phys/Vec3;)Z", 5, 3);
            save(effects, payload);
            ClassNode profile = read(archive, SPELLS + "ManorBossSpellProfile");
            MethodNode power = profile.methods.stream().filter(method -> method.name.equals("power")).findFirst().orElseThrow();
            InsnList prefix = new InsnList();
            prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
            prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SPELLS + "ZicaiVoidSpells", "matches", "(L" + KIND + ";)Z", false));
            LabelNode original = new LabelNode();
            prefix.add(new JumpInsnNode(Opcodes.IFEQ, original));
            prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
            prefix.add(new FieldInsnNode(Opcodes.GETFIELD, KIND, "damage", "I"));
            prefix.add(new IntInsnNode(Opcodes.BIPUSH, 8));
            prefix.add(new InsnNode(Opcodes.IADD));
            prefix.add(new InsnNode(Opcodes.I2F));
            prefix.add(new InsnNode(Opcodes.FRETURN));
            prefix.add(original);
            prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            power.instructions.insert(prefix);
            save(profile, payload);
        }
        try (var files = Files.walk(compiled.resolve("net/farmingtales/ftboss"))) {
            for (Path file : files.filter(path -> path.toString().endsWith(".class")).toList()) {
                String relative = compiled.relativize(file).toString().replace('\\', '/');
                if (!relative.contains("ZicaiVoid") && !relative.equals(KIND + ".class")) continue;
                ClassNode output = new ClassNode();
                new ClassReader(Files.readAllBytes(file)).accept(new ClassRemapper(output, names), 0);
                save(output, payload);
            }
        }
        System.out.println("PASS: two effect entry hooks, power extension, appended enum and new runtime classes");
    }
}
