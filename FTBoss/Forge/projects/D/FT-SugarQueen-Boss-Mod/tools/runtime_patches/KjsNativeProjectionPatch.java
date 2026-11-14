import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class KjsNativeProjectionPatch {
    public static final String LEDGER = "net/farmingtales/ftboss/item/FoodWeaponSourceLedger";
    public static final String ROOT = LEDGER + "$InteractionRoot";
    public static final String ARRAY = "ACTIVE_ROOT_PROJECTIONS";
    public static final String LIMIT = "ftboss$nativeRootLimit";
    public static final String TRIM = "ftboss$trimNativeRootLimit";
    public static final Set<String> CHANGED = Set.of("installRootProjection", "removeRootProjection",
            "beginNativeObservation(Ljava/lang/Object;)", "invalidateWeaponReadProjection");
    private static final String EXPECTED = "5b989f4b9f30a38e8f0c319357eebc16c244657320afa6235f99212872b8c9c3";

    private KjsNativeProjectionPatch() {}

    public static void main(String[] arguments) throws Exception {
        require(arguments.length == 3, "patch baseline.jar overlay | verify baseline.jar candidate.jar");
        if (arguments[0].equals("patch")) {
            try (ZipFile archive = new ZipFile(arguments[1])) {
                Path output = Path.of(arguments[2], LEDGER + ".class");
                Files.createDirectories(output.getParent());
                Files.write(output, patch(read(archive, LEDGER + ".class")));
            }
        } else if (arguments[0].equals("verify")) {
            verify(Path.of(arguments[1]), Path.of(arguments[2]));
        } else throw new IllegalArgumentException(arguments[0]);
    }

    public static byte[] patch(byte[] bytes) throws Exception {
        require(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)).equals(EXPECTED),
                "Ledger baseline changed; review required before patching");
        ClassNode type = parse(bytes);
        require(type.fields.stream().noneMatch(field -> field.name.equals(LIMIT)), "Already patched");
        require(type.methods.stream().noneMatch(method -> method.name.equals(TRIM)), "Trim helper collision");
        verifyWriters(type);
        type.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, LIMIT, "I", null, null));
        MethodNode install = find(type, "installRootProjection", "(L" + ROOT + ";)Z");
        MethodNode remove = find(type, "removeRootProjection", "(L" + ROOT + ";)V");
        requireLocked(install);
        requireLocked(remove);
        AbstractInsnNode installStore = store(install);
        require(previousOperation(installStore) instanceof VarInsnNode load && load.getOpcode() == Opcodes.ALOAD
                && load.var == 0, "Install store shape changed");
        require(previousOperation(previousOperation(installStore)) instanceof VarInsnNode slot
                && slot.getOpcode() == Opcodes.ILOAD && slot.var == 1, "Install slot local changed");
        InsnList update = new InsnList();
        update.add(new FieldInsnNode(Opcodes.GETSTATIC, LEDGER, LIMIT, "I"));
        update.add(new VarInsnNode(Opcodes.ILOAD, 1));
        update.add(new InsnNode(Opcodes.ICONST_1));
        update.add(new InsnNode(Opcodes.IADD));
        update.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Math", "max", "(II)I", false));
        update.add(new FieldInsnNode(Opcodes.PUTSTATIC, LEDGER, LIMIT, "I"));
        install.instructions.insert(installStore, update);
        AbstractInsnNode removeStore = store(remove);
        require(previousOperation(removeStore).getOpcode() == Opcodes.ACONST_NULL, "Removal store shape changed");
        remove.instructions.insert(removeStore, new MethodInsnNode(Opcodes.INVOKESTATIC, LEDGER, TRIM, "()V", false));
        bound(find(type, "beginNativeObservation", "(Ljava/lang/Object;)L" + LEDGER + "$NativeObservationCut;"));
        bound(find(type, "invalidateWeaponReadProjection", "(Lnet/minecraft/world/item/ItemStack;)V"));
        type.methods.add(trim());
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        byte[] result = writer.toByteArray();
        ClassNode verified = parse(result);
        for (MethodNode method : verified.methods) {
            if (changed(method) || method.name.equals(TRIM)) new Analyzer<>(new BasicVerifier()).analyze(LEDGER, method);
        }
        return result;
    }

    private static void bound(MethodNode method) {
        requireLocked(method);
        var bounds = Arrays.stream(method.instructions.toArray()).filter(instruction ->
                instruction instanceof IntInsnNode constant && constant.operand == 4096).toList();
        require(bounds.size() == 1, "Unexpected scan bounds: " + method.name);
        require(nextOperation(bounds.get(0)).getOpcode() == Opcodes.IF_ICMPGE, "Unexpected loop branch");
        method.instructions.set(bounds.get(0), new FieldInsnNode(Opcodes.GETSTATIC, LEDGER, LIMIT, "I"));
    }

    private static MethodNode trim() {
        MethodNode method = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, TRIM, "()V", null, null);
        LabelNode repeat = new LabelNode();
        LabelNode done = new LabelNode();
        method.instructions.add(repeat);
        method.instructions.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, LEDGER, LIMIT, "I"));
        method.instructions.add(new JumpInsnNode(Opcodes.IFEQ, done));
        method.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, LEDGER, ARRAY, "[L" + ROOT + ";"));
        method.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, LEDGER, LIMIT, "I"));
        method.instructions.add(new InsnNode(Opcodes.ICONST_1));
        method.instructions.add(new InsnNode(Opcodes.ISUB));
        method.instructions.add(new InsnNode(Opcodes.AALOAD));
        method.instructions.add(new JumpInsnNode(Opcodes.IFNONNULL, done));
        method.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, LEDGER, LIMIT, "I"));
        method.instructions.add(new InsnNode(Opcodes.ICONST_1));
        method.instructions.add(new InsnNode(Opcodes.ISUB));
        method.instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, LEDGER, LIMIT, "I"));
        method.instructions.add(new JumpInsnNode(Opcodes.GOTO, repeat));
        method.instructions.add(done);
        method.instructions.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.add(new InsnNode(Opcodes.RETURN));
        return method;
    }

    private static void verifyWriters(ClassNode type) {
        Set<String> writers = new java.util.HashSet<>();
        for (MethodNode method : type.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction instanceof FieldInsnNode field && field.owner.equals(LEDGER)
                        && field.name.equals(ARRAY) && field.getOpcode() == Opcodes.PUTSTATIC) {
                    require(method.name.equals("<clinit>"), "Unexpected projection array replacement");
                }
                if (instruction.getOpcode() != Opcodes.AASTORE) continue;
                AbstractInsnNode cursor = instruction;
                for (int distance = 0; distance < 5 && cursor != null; distance++, cursor = previousOperation(cursor)) {
                    if (cursor instanceof FieldInsnNode field && field.owner.equals(LEDGER) && field.name.equals(ARRAY)) {
                        writers.add(method.name);
                    }
                }
            }
        }
        require(writers.equals(Set.of("installRootProjection", "removeRootProjection")), "Projection writers drift: " + writers);
    }

    private static AbstractInsnNode store(MethodNode method) {
        var stores = Arrays.stream(method.instructions.toArray()).filter(instruction -> instruction.getOpcode() == Opcodes.AASTORE).toList();
        require(stores.size() == 1, "Unexpected array writes: " + method.name);
        return stores.get(0);
    }

    private static void requireLocked(MethodNode method) {
        require((method.access & (Opcodes.ACC_STATIC | Opcodes.ACC_SYNCHRONIZED)) ==
                (Opcodes.ACC_STATIC | Opcodes.ACC_SYNCHRONIZED), "Projection access must hold ledger monitor: " + method.name);
    }

    private static boolean changed(MethodNode method) {
        return CHANGED.contains(method.name) || CHANGED.contains(method.name + method.desc.substring(0, method.desc.indexOf(')') + 1));
    }

    public static void verify(Path baseline, Path candidate) throws Exception {
        int unchangedEntries = 0;
        int changedEntries = 0;
        try (ZipFile before = new ZipFile(baseline.toFile()); ZipFile after = new ZipFile(candidate.toFile())) {
            require(before.size() == after.size(), "JAR entry inventory changed");
            require(Collections.list(after.entries()).stream().map(entry -> entry.getName()).distinct().count() == after.size(),
                    "Duplicate JAR entries");
            for (var entry : Collections.list(before.entries())) {
                byte[] original = read(before, entry.getName());
                byte[] current = read(after, entry.getName());
                if (Arrays.equals(original, current)) { unchangedEntries++; continue; }
                require(entry.getName().equals(LEDGER + ".class"), "Unrelated payload changed: " + entry.getName());
                require(Arrays.equals(patch(original), current), "Candidate differs from approved transformation");
                Map<String, String> oldMethods = methods(parse(original));
                Map<String, String> newMethods = methods(parse(current));
                require(newMethods.size() == oldMethods.size() + 1 && newMethods.containsKey(TRIM + "()V"), "Unexpected new methods");
                int changedMethods = 0;
                for (MethodNode method : parse(original).methods) {
                    String signature = method.name + method.desc;
                    boolean different = !oldMethods.get(signature).equals(newMethods.get(signature));
                    require(different == changed(method), "Unexpected method delta: " + signature);
                    if (different) changedMethods++;
                }
                require(changedMethods == 4, "Expected four changed methods");
                changedEntries++;
            }
        }
        require(changedEntries == 1, "Missing ledger patch");
        System.out.println("PASS changedClasses=1 changedMethods=4 addedPrivateHelpers=1 unchangedEntries=" + unchangedEntries);
    }

    public static MethodNode find(ClassNode type, String name, String descriptor) {
        return type.methods.stream().filter(method -> method.name.equals(name) && method.desc.equals(descriptor)).findFirst().orElseThrow();
    }

    public static ClassNode parse(byte[] bytes) {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, 0);
        return type;
    }

    public static byte[] read(ZipFile archive, String name) throws Exception {
        var entry = archive.getEntry(name);
        require(entry != null, "Missing JAR entry: " + name);
        try (var input = archive.getInputStream(entry)) { return input.readAllBytes(); }
    }

    private static Map<String, String> methods(ClassNode type) {
        Map<String, String> result = new LinkedHashMap<>();
        for (MethodNode method : type.methods) {
            Textifier trace = new Textifier();
            method.accept(new TraceMethodVisitor(trace));
            StringWriter text = new StringWriter();
            trace.print(new PrintWriter(text));
            result.put(method.name + method.desc, text.toString());
        }
        return result;
    }

    private static AbstractInsnNode previousOperation(AbstractInsnNode instruction) {
        do { instruction = instruction.getPrevious(); } while (instruction != null && instruction.getOpcode() < 0);
        return instruction;
    }

    private static AbstractInsnNode nextOperation(AbstractInsnNode instruction) {
        do { instruction = instruction.getNext(); } while (instruction != null && instruction.getOpcode() < 0);
        return instruction;
    }

    public static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
