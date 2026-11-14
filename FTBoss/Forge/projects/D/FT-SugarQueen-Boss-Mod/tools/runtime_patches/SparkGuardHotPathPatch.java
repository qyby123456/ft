import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.util.*;

public final class SparkGuardHotPathPatch {
    public static final String ITEM = "net/farmingtales/ftboss/item/";
    public static final String GUARD = ITEM + "OwnerFullRepairGuard";
    public static final String CONTINUATION = ITEM + "OwnerRepairContinuation";
    public static final String STACK = "net/minecraft/world/item/ItemStack";
    public static final Map<String, Set<String>> CHANGED = Map.of(
            GUARD, Set.of("inventoryTickEntered", "inventoryTickReturned"), CONTINUATION, Set.of("deferEntry"));
    private static final Map<String, String> EXPECTED = Map.of(
            GUARD, "1a870a6ab015d759d2d0c4a46d36c6b304ca67b580a83f31c27038ed967abb73",
            CONTINUATION, "6cfb4dbceceb1a5c50f3a938bc3eace645da162cce0bd79e2bc6d6b5f2f39e6e");

    private SparkGuardHotPathPatch() {}

    public static void main(String[] arguments) throws Exception {
        if (arguments.length != 3) throw new IllegalArgumentException("patch baseline.jar overlay | verify baseline.jar candidate.jar");
        if (arguments[0].equals("patch")) {
            try (ZipFile archive = new ZipFile(arguments[1])) {
                for (String name : CHANGED.keySet()) {
                    Path output = Path.of(arguments[2], name + ".class");
                    Files.createDirectories(output.getParent());
                    Files.write(output, patch(read(archive, name + ".class")));
                }
            }
        } else if (arguments[0].equals("verify")) verify(arguments[1], arguments[2]);
        else throw new IllegalArgumentException(arguments[0]);
    }

    public static byte[] patch(byte[] bytes) throws Exception {
        ClassNode type = parse(bytes);
        require(HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes)).equals(EXPECTED.get(type.name)),
                "Runtime class changed; review required: " + type.name);
        if (type.name.equals(GUARD)) {
            entered(find(type, "inventoryTickEntered"));
            returned(find(type, "inventoryTickReturned"));
        } else if (type.name.equals(CONTINUATION)) {
            require(find(type, "noBinding").desc.equals("(L" + STACK + ";)Z"), "Absence probe descriptor drift");
            defer(find(type, "deferEntry"));
        } else throw new IllegalArgumentException(type.name);
        for (MethodNode method : type.methods) {
            if (CHANGED.get(type.name).contains(method.name)) new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        return writer.toByteArray();
    }

    private static void defer(MethodNode method) {
        require(method.desc.equals("(Ljava/lang/Object;I)Z"), "Continuation descriptor drift");
        uniqueCall(method, CONTINUATION, "caller");
        uniqueCall(method, CONTINUATION, "binding");
        require(Arrays.stream(method.instructions.toArray()).noneMatch(instruction ->
                instruction instanceof MethodInsnNode call && call.name.equals("noBinding")), "Already patched continuation");
        LabelNode original = new LabelNode();
        InsnList prefix = new InsnList();
        prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
        prefix.add(new TypeInsnNode(Opcodes.INSTANCEOF, STACK));
        prefix.add(new JumpInsnNode(Opcodes.IFEQ, original));
        prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
        prefix.add(new TypeInsnNode(Opcodes.CHECKCAST, STACK));
        prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CONTINUATION, "noBinding", "(L" + STACK + ";)Z", false));
        prefix.add(new JumpInsnNode(Opcodes.IFEQ, original));
        prefix.add(new InsnNode(Opcodes.ICONST_0));
        prefix.add(new InsnNode(Opcodes.IRETURN));
        prefix.add(original);
        prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(prefix);
    }

    private static void entered(MethodNode method) {
        require(method.desc.equals("(Ljava/lang/Object;Ljava/lang/Object;)L" + GUARD + "$TickCut;"), "Tick entry descriptor drift");
        MethodInsnNode caller = uniqueCall(method, GUARD, "tickCaller");
        require(nextOperation(caller) instanceof JumpInsnNode branch && branch.getOpcode() == Opcodes.IFNE, "Tick caller branch drift");
        JumpInsnNode branch = (JumpInsnNode) nextOperation(caller);
        AbstractInsnNode failure = nextOperation(branch);
        require(failure.getOpcode() == Opcodes.ACONST_NULL, "Tick failure cleanup drift");
        MethodInsnNode node = uniqueCall(method, GUARD, "node");
        require(nextOperation(node) instanceof VarInsnNode variable && variable.var == 5, "Node local drift");
        AbstractInsnNode storage = Arrays.stream(method.instructions.toArray()).filter(instruction ->
                instruction instanceof TypeInsnNode allocation && allocation.getOpcode() == Opcodes.NEW
                        && allocation.desc.equals(GUARD + "$StorageView")).findFirst().orElseThrow();
        require(previousOperation(storage) instanceof VarInsnNode variable
                && variable.getOpcode() == Opcodes.ASTORE && variable.var == 6, "Record local drift");
        LabelNode rejected = new LabelNode();
        InsnList rejection = new InsnList();
        rejection.add(rejected);
        rejection.add(new FrameNode(Opcodes.F_FULL, 5,
                new Object[]{"java/lang/Object", "java/lang/Object", GUARD + "$Mutation", "java/lang/Throwable", "java/lang/Object"},
                0, new Object[]{}));
        method.instructions.insertBefore(failure, rejection);
        method.instructions.set(caller, new InsnNode(Opcodes.ICONST_1));
        InsnList authorization = new InsnList();
        authorization.add(new MethodInsnNode(Opcodes.INVOKESTATIC, GUARD, "tickCaller", "()Z", false));
        authorization.add(new JumpInsnNode(Opcodes.IFEQ, rejected));
        method.instructions.insert(previousOperation(storage), authorization);
    }

    private static void returned(MethodNode method) {
        require(method.desc.equals("(L" + GUARD + "$TickCut;Ljava/lang/Throwable;)V"), "Tick return descriptor drift");
        require(firstOperation(method).getOpcode() == Opcodes.NEW, "Already patched tick return");
        uniqueCall(method, GUARD, "tickCaller");
        LabelNode original = new LabelNode();
        InsnList prefix = new InsnList();
        prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
        prefix.add(new JumpInsnNode(Opcodes.IFNONNULL, original));
        prefix.add(new InsnNode(Opcodes.RETURN));
        prefix.add(original);
        prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(prefix);
    }

    private static void verify(String baseline, String candidate) throws Exception {
        int unchangedEntries = 0;
        int unchangedMethods = 0;
        int changedClasses = 0;
        try (ZipFile before = new ZipFile(baseline); ZipFile after = new ZipFile(candidate)) {
            require(before.size() == after.size(), "JAR entry count changed");
            require(Collections.list(after.entries()).stream().map(entry -> entry.getName()).distinct().count() == after.size(),
                    "Duplicate JAR entry");
            for (var entry : Collections.list(before.entries())) {
                byte[] oldBytes = read(before, entry.getName());
                byte[] newBytes = read(after, entry.getName());
                if (Arrays.equals(oldBytes, newBytes)) { unchangedEntries++; continue; }
                String name = entry.getName().replaceFirst("\\.class$", "");
                require(CHANGED.containsKey(name), "Unexpected changed entry " + entry.getName());
                require(Arrays.equals(patch(oldBytes), newBytes), "Payload differs from exact approved transformation " + name);
                Map<String, String> oldMethods = methods(parse(oldBytes));
                Map<String, String> newMethods = methods(parse(newBytes));
                require(oldMethods.keySet().equals(newMethods.keySet()), "Method inventory changed");
                Set<String> changes = new HashSet<>();
                for (String signature : oldMethods.keySet()) {
                    if (oldMethods.get(signature).equals(newMethods.get(signature))) unchangedMethods++;
                    else changes.add(signature.substring(0, signature.indexOf('(')));
                }
                require(changes.equals(CHANGED.get(name)), "Unexpected method changes " + changes);
                changedClasses++;
            }
        }
        require(changedClasses == CHANGED.size(), "Missing changed classes");
        System.out.println("PASS changedClasses=" + changedClasses + " changedMethods=3 unchangedEntries=" + unchangedEntries
                + " unchangedMethodsInPatchedClasses=" + unchangedMethods + " exactApprovedPayload=true");
    }

    public static ClassNode parse(byte[] bytes) {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, 0);
        return type;
    }

    public static MethodNode find(ClassNode type, String name) {
        var matches = type.methods.stream().filter(method -> method.name.equals(name)).toList();
        require(matches.size() == 1, "Ambiguous method " + name);
        return matches.get(0);
    }

    public static byte[] read(ZipFile archive, String name) throws Exception {
        var entry = archive.getEntry(name);
        require(entry != null, "Missing entry " + name);
        try (var input = archive.getInputStream(entry)) { return input.readAllBytes(); }
    }

    private static MethodInsnNode uniqueCall(MethodNode method, String owner, String name) {
        var matches = Arrays.stream(method.instructions.toArray()).filter(instruction ->
                instruction instanceof MethodInsnNode call && call.owner.equals(owner) && call.name.equals(name)).toList();
        require(matches.size() == 1, "Ambiguous call " + method.name + ":" + name);
        return (MethodInsnNode) matches.get(0);
    }

    public static AbstractInsnNode firstOperation(MethodNode method) {
        AbstractInsnNode instruction = method.instructions.getFirst();
        while (instruction != null && instruction.getOpcode() < 0) instruction = instruction.getNext();
        return instruction;
    }

    private static AbstractInsnNode nextOperation(AbstractInsnNode instruction) {
        do { instruction = instruction.getNext(); } while (instruction != null && instruction.getOpcode() < 0);
        return instruction;
    }

    private static AbstractInsnNode previousOperation(AbstractInsnNode instruction) {
        do { instruction = instruction.getPrevious(); } while (instruction != null && instruction.getOpcode() < 0);
        return instruction;
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

    public static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
