import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

public final class DomainVisualMethodMerge {
    public static final String VISUAL = "net/farmingtales/ftboss/client/render/ManorDomainVisual.class";
    public static final String GEOMETRY = "net/farmingtales/ftboss/util/ManorDomainGeometryV2.class";
    public static final String EXPANSION = "net/farmingtales/ftboss/util/ManorDomainExpansion.class";
    public static final String RESOURCE = "assets/ftboss/manor/domain_geometry_v2.json";
    public static final Map<String, Set<String>> METHODS = Map.of(VISUAL, Set.of("ftboss$renderWithoutSoftBloom"), GEOMETRY, Set.of("read", "emit"), EXPANSION, Set.of("emit"));

    public static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    public static MethodNode method(ClassNode node, String name) {
        List<MethodNode> matching = node.methods.stream().filter(method -> method.name.equals(name)).toList();
        if (matching.size() != 1) throw new IllegalStateException("Expected one method " + node.name + "." + name);
        return matching.get(0);
    }

    private static MethodNode cloneMethod(MethodNode source, String owner) {
        MethodNode copy = new MethodNode(Opcodes.ASM9, source.access, source.name, source.desc, source.signature, source.exceptions.toArray(String[]::new));
        source.accept(new MethodRemapper(copy, new Remapper() {
            @Override
            public String mapMethodName(String methodOwner, String methodName, String descriptor) {
                if (methodOwner.equals(owner) && methodName.startsWith("lambda$ftboss$renderWithoutSoftBloom$")) return "lambda$render$0";
                return methodName;
            }
        }));
        return copy;
    }

    public static byte[] fingerprint(MethodNode source) {
        MethodNode normalized = cloneMethod(source, "");
        normalized.name = "normalized";
        normalized.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
        normalized.signature = null;
        normalized.visibleAnnotations = null;
        normalized.invisibleAnnotations = null;
        normalized.visibleTypeAnnotations = null;
        normalized.invisibleTypeAnnotations = null;
        normalized.visibleParameterAnnotations = null;
        normalized.invisibleParameterAnnotations = null;
        normalized.annotationDefault = null;
        normalized.parameters = null;
        normalized.attrs = null;
        normalized.localVariables = null;
        normalized.visibleLocalVariableAnnotations = null;
        normalized.invisibleLocalVariableAnnotations = null;
        for (var instruction : normalized.instructions.toArray()) if (instruction instanceof FrameNode || instruction instanceof LineNumberNode) normalized.instructions.remove(instruction);
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "MethodFingerprint", null, "java/lang/Object", null);
        normalized.accept(writer);
        writer.visitEnd();
        return writer.toByteArray();
    }

    public static byte[] shell(ClassNode source) {
        ClassNode clone = read(bytes(source));
        clone.methods.clear();
        return bytes(clone);
    }

    public static byte[] bytes(ClassNode source) {
        ClassWriter writer = new ClassWriter(0);
        source.accept(writer);
        return writer.toByteArray();
    }

    private static String hash(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    public static void verifyMethods(byte[] before, byte[] after, Set<String> allowed) {
        ClassNode original = read(before);
        ClassNode patched = read(after);
        if (!Arrays.equals(shell(original), shell(patched))) throw new IllegalStateException("Class shell/fields/annotations changed: " + original.name);
        if (original.methods.size() != patched.methods.size()) throw new IllegalStateException("Method count changed");
        for (MethodNode source : original.methods) {
            MethodNode target = method(patched, source.name);
            if (!source.desc.equals(target.desc) || source.access != target.access) throw new IllegalStateException("Method ABI changed");
            boolean equal = Arrays.equals(fingerprint(source), fingerprint(target));
            if (equal == allowed.contains(source.name)) throw new IllegalStateException("Unexpected method delta " + original.name + "." + source.name);
        }
    }

    public static void main(String[] arguments) throws Exception {
        Path baseline = Path.of(arguments[0]);
        Path output = Path.of(arguments[1]);
        Path classes = Path.of(arguments[2]);
        Path resource = Path.of(arguments[3]).resolve(RESOURCE);
        Path previousJar = Path.of(arguments[4]);
        Path audit = Path.of(arguments[5]);
        Map<String, byte[]> replacements = new LinkedHashMap<>();
        List<Object> written = new ArrayList<>();
        try (ZipFile current = new ZipFile(baseline.toFile()); ZipFile previous = new ZipFile(previousJar.toFile())) {
            for (Map.Entry<String, Set<String>> plan : METHODS.entrySet()) {
                byte[] before = current.getInputStream(current.getEntry(plan.getKey())).readAllBytes();
                ClassNode target = read(before);
                ClassNode expected = read(previous.getInputStream(previous.getEntry(plan.getKey())).readAllBytes());
                ClassNode donor = read(Files.readAllBytes(classes.resolve(plan.getKey())));
                if (plan.getKey().equals(VISUAL)) {
                    MethodNode oldLambda = method(target, "lambda$render$0");
                    MethodNode compiledLambda = donor.methods.stream().filter(candidate -> candidate.name.startsWith("lambda$ftboss$renderWithoutSoftBloom$")).findFirst().orElseThrow();
                    if (!Arrays.equals(fingerprint(oldLambda), fingerprint(compiledLambda))) throw new IllegalStateException("Render lambda conflict");
                }
                for (String name : plan.getValue()) {
                    MethodNode existing = method(target, name);
                    MethodNode known = method(expected, name.equals("ftboss$renderWithoutSoftBloom") ? "render" : name);
                    if (!Arrays.equals(fingerprint(existing), fingerprint(known))) throw new IllegalStateException("Concurrent owned-method edit; refusing merge: " + plan.getKey() + "#" + name);
                    MethodNode replacement = cloneMethod(method(donor, name), target.name);
                    if (!replacement.desc.equals(existing.desc)) throw new IllegalStateException("Replacement descriptor mismatch");
                    String oldHash = hash(fingerprint(existing));
                    existing.instructions = replacement.instructions;
                    existing.tryCatchBlocks = replacement.tryCatchBlocks;
                    existing.localVariables = replacement.localVariables;
                    existing.visibleLocalVariableAnnotations = replacement.visibleLocalVariableAnnotations;
                    existing.invisibleLocalVariableAnnotations = replacement.invisibleLocalVariableAnnotations;
                    existing.maxStack = replacement.maxStack;
                    existing.maxLocals = replacement.maxLocals;
                    written.add(Map.of("entry", plan.getKey(), "method", name, "descriptor", existing.desc, "beforeCodeSHA256", oldHash, "afterCodeSHA256", hash(fingerprint(existing))));
                }
                byte[] merged = bytes(target);
                verifyMethods(before, merged, plan.getValue());
                replacements.put(plan.getKey(), merged);
            }
            byte[] beforeResource = current.getInputStream(current.getEntry(RESOURCE)).readAllBytes();
            byte[] expectedResource = previous.getInputStream(previous.getEntry(RESOURCE)).readAllBytes();
            if (!Arrays.equals(beforeResource, expectedResource)) throw new IllegalStateException("Concurrent domain model resource edit; refusing merge");
            byte[] newResource = Files.readAllBytes(resource);
            replacements.put(RESOURCE, newResource);
            written.add(Map.of("entry", RESOURCE, "beforeSHA256", hash(beforeResource), "afterSHA256", hash(newResource)));
            Set<String> seen = new java.util.HashSet<>();
            try (ZipOutputStream target = new ZipOutputStream(Files.newOutputStream(output))) {
                var entries = current.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (!seen.add(entry.getName())) throw new IllegalStateException("Duplicate baseline ZIP entry");
                    byte[] contents = replacements.get(entry.getName());
                    if (contents == null) contents = current.getInputStream(entry).readAllBytes();
                    ZipEntry copy = new ZipEntry(entry.getName());
                    copy.setTime(entry.getTime());
                    if (entry.getComment() != null) copy.setComment(entry.getComment());
                    if (entry.getExtra() != null) copy.setExtra(entry.getExtra());
                    target.putNextEntry(copy);
                    target.write(contents);
                    target.closeEntry();
                }
            }
        }
        var report = Map.of("mode", "METHOD_LEVEL_CURRENT_BASELINE_ONLY", "baseline", baseline.toString(), "baselineSHA256", hash(Files.readAllBytes(baseline)), "outputSHA256", hash(Files.readAllBytes(output)), "written", written, "preserved", List.of("all non-target JAR entries", "all non-target methods and class metadata", "SpellBloomRenderer wrapper and render lambda", "server mechanics and ceremony resources"));
        Files.writeString(audit, new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println("PASS method-level merge: four method bodies across three classes + one domain resource. Bloom wrapper, ABI and all unrelated methods preserved.");
    }
}
