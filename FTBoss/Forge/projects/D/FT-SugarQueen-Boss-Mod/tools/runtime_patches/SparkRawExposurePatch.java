import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class SparkRawExposurePatch implements Opcodes {
    public static final String OWNER = "net/farmingtales/ftboss/item/OwnerFieldExpectedAuthority";
    public static final String HELPER = "net/farmingtales/ftboss/item/OwnerRawExposureFrames";
    private static final String FRAME = "java/lang/StackWalker$StackFrame";

    public static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    public static ClassNode parse(byte[] bytes) {
        ClassNode result = new ClassNode();
        new ClassReader(bytes).accept(result, 0);
        return result;
    }

    public static byte[] read(ZipFile archive, String name) throws Exception {
        var entry = archive.getEntry(name + ".class");
        require(entry != null, "Missing " + name);
        try (var stream = archive.getInputStream(entry)) { return stream.readAllBytes(); }
    }

    public static MethodNode method(ClassNode owner, String name) {
        var matches = owner.methods.stream().filter(method -> method.name.equals(name)).toList();
        require(matches.size() == 1, "Ambiguous method " + name);
        return matches.get(0);
    }

    public static MethodNode selector(ClassNode owner) {
        var matches = owner.methods.stream().filter(method -> method.name.startsWith("lambda$rawExposed$")
                && method.desc.equals("(Ljava/util/stream/Stream;)Ljava/util/List;")).toList();
        require(matches.size() == 1, "Raw observer selector drift");
        return matches.get(0);
    }

    private static AbstractInsnNode previous(AbstractInsnNode instruction) {
        do { instruction = instruction.getPrevious(); } while (instruction != null && instruction.getOpcode() < 0);
        require(instruction != null, "Missing receiver");
        return instruction;
    }

    public static byte[] patch(byte[] original) throws Exception {
        ClassNode owner = parse(original);
        MethodNode body = method(owner, "rawExposed");
        MethodNode select = selector(owner);
        require(Arrays.stream(select.instructions.toArray()).noneMatch(instruction ->
                instruction instanceof MethodInsnNode invoke && invoke.owner.equals(HELPER)), "Already patched; use its original baseline");
        int replaced = 0;
        for (var instruction : body.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode invoke) || !invoke.owner.equals("java/util/List")
                    || !invoke.name.equals("stream")) continue;
            AbstractInsnNode receiver = previous(instruction);
            require(receiver instanceof VarInsnNode variable && variable.getOpcode() == ALOAD && variable.var == 2,
                    "Unknown raw selector local");
            AbstractInsnNode cast = instruction;
            while (cast != null && !(cast instanceof TypeInsnNode type && type.getOpcode() == CHECKCAST && type.desc.equals(FRAME))) {
                require(cast == instruction || cast.getOpcode() >= 0, "Unexpected control flow in selector");
                cast = cast.getNext();
            }
            require(cast != null, "Missing selected frame cast");
            AbstractInsnNode cursor = instruction;
            while (cursor != cast) {
                AbstractInsnNode next = cursor.getNext();
                body.instructions.remove(cursor);
                cursor = next;
            }
            InsnList lookup = new InsnList();
            lookup.add(new InsnNode(replaced == 0 ? ICONST_0 : ICONST_1));
            lookup.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true));
            body.instructions.insertBefore(cast, lookup);
            replaced++;
        }
        require(replaced == 2, "Expected exactly two stream-based frame lookups");
        select.instructions.clear();
        select.tryCatchBlocks.clear();
        select.localVariables = null;
        select.instructions.add(new VarInsnNode(ALOAD, 0));
        select.instructions.add(new MethodInsnNode(INVOKESTATIC, HELPER, "select", select.desc, false));
        select.instructions.add(new InsnNode(ARETURN));
        select.maxStack = 1;
        select.maxLocals = 1;
        for (MethodNode target : List.of(body, select)) new Analyzer<>(new BasicVerifier()).analyze(owner.name, target);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        owner.accept(writer);
        return writer.toByteArray();
    }

    private static byte[] fingerprint(MethodNode method) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(V17, ACC_PUBLIC, "RawExposureMethodCheck", null, "java/lang/Object", null);
        method.accept(writer.visitMethod(method.access, method.name, method.desc, method.signature, method.exceptions.toArray(String[]::new)));
        writer.visitEnd();
        return writer.toByteArray();
    }

    public static void main(String[] arguments) throws Exception {
        if (arguments[0].equals("patch")) {
            try (ZipFile archive = new ZipFile(arguments[1])) {
                byte[] original = read(archive, OWNER);
                String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(original));
                require(hash.equals(arguments[3]), "Original authority class changed; explicit review required");
                Path output = Path.of(arguments[2], OWNER + ".class");
                Files.createDirectories(output.getParent());
                Files.write(output, patch(original));
            }
        } else if (arguments[0].equals("verify")) {
            try (ZipFile before = new ZipFile(arguments[1]); ZipFile after = new ZipFile(arguments[2])) {
                byte[] original = read(before, OWNER);
                byte[] updated = read(after, OWNER);
                require(Arrays.equals(patch(original), updated), "Not the exact approved method transformation");
                ClassNode first = parse(original), second = parse(updated);
                require(first.fields.size() == second.fields.size() && first.methods.size() == second.methods.size(), "Class inventory changed");
                int changed = 0, unchanged = 0;
                for (MethodNode method : first.methods) {
                    MethodNode actual = method(second, method.name);
                    if (Arrays.equals(fingerprint(method), fingerprint(actual))) unchanged++;
                    else {
                        require(method.name.equals("rawExposed") || method == selector(first), "Unrelated method changed: " + method.name);
                        changed++;
                    }
                }
                require(changed == 2, "Expected two changed methods");
                int preserved = 0;
                for (var entry : before.stream().toList()) {
                    var replacement = after.getEntry(entry.getName());
                    require(replacement != null, "Missing entry " + entry.getName());
                    if (entry.getName().equals(OWNER + ".class")) continue;
                    require(Arrays.equals(before.getInputStream(entry).readAllBytes(), after.getInputStream(replacement).readAllBytes()),
                            "Unrelated entry changed: " + entry.getName());
                    preserved++;
                }
                require(after.size() == before.size() + 1 && before.getEntry(HELPER + ".class") == null
                        && after.getEntry(HELPER + ".class") != null, "Unexpected helper inventory");
                require(after.stream().map(java.util.zip.ZipEntry::getName).distinct().count() == after.size(), "Duplicate entries");
                System.out.println("PASS exact transformation; changedMethods=" + changed + " preservedMethods=" + unchanged + " preservedEntries=" + preserved);
            }
        } else throw new IllegalArgumentException("Expected patch or verify");
    }
}
