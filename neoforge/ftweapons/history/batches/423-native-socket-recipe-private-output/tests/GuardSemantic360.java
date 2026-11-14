import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class GuardSemantic360 {
    private static ClassNode read(Path path) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(path)).accept(node, ClassReader.SKIP_DEBUG);
        return node;
    }

    private static byte[] normalized(ClassNode node) {
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static String method(MethodNode node) {
        Textifier text = new Textifier();
        node.accept(new TraceMethodVisitor(text));
        StringWriter output = new StringWriter();
        text.print(new PrintWriter(output));
        return node.access + ":" + node.desc + ":" + node.signature + ":" + node.exceptions + ":" + node.maxLocals + ":" + output;
    }

    private static Map<String, String> methods(ClassNode node) {
        Map<String, String> result = new TreeMap<>();
        for (MethodNode method : node.methods) result.put(method.name + method.desc, method(method));
        return result;
    }

    private static List<String> fields(ClassNode node) {
        return node.fields.stream().map(field -> field.access + ":" + field.name + ":" + field.desc + ":" + field.signature + ":" + field.value).toList();
    }

    private static String hash(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    public static void main(String[] arguments) throws Exception {
        var manifest = JsonParser.parseString(Files.readString(Path.of(arguments[0]))).getAsJsonArray();
        Path details = Path.of(arguments[2]);
        Files.createDirectories(details);
        List<Map<String, Object>> results = new ArrayList<>();
        int identical = 0, semantic = 0;
        for (var value : manifest) {
            var entry = value.getAsJsonObject();
            String name = entry.get("name").getAsString();
            Path beforePath = Path.of(entry.get("beforePath").getAsString());
            Path afterPath = Path.of(entry.get("afterPath").getAsString());
            ClassNode before = read(beforePath), after = read(afterPath);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", name);
            row.put("beforeRawSha256", hash(Files.readAllBytes(beforePath)));
            row.put("afterRawSha256", hash(Files.readAllBytes(afterPath)));
            boolean same = Arrays.equals(normalized(before), normalized(after));
            row.put("skipDebugClassBytesEqual", same);
            row.put("fieldsEqual", fields(before).equals(fields(after)));
            row.put("interfacesEqual", before.interfaces.equals(after.interfaces));
            row.put("classAccessEqual", before.access == after.access);
            row.put("superClassEqual", java.util.Objects.equals(before.superName, after.superName));
            Map<String, String> oldMethods = methods(before), newMethods = methods(after);
            List<String> removed = new ArrayList<>(), added = new ArrayList<>(), changed = new ArrayList<>();
            for (var method : oldMethods.entrySet()) {
                if (!newMethods.containsKey(method.getKey())) removed.add(method.getKey());
                else if (!method.getValue().equals(newMethods.get(method.getKey()))) changed.add(method.getKey());
            }
            for (String key : newMethods.keySet()) if (!oldMethods.containsKey(key)) added.add(key);
            row.put("removedMethods", removed);
            row.put("addedMethods", added);
            row.put("changedMethods", changed);
            row.put("oldMethodCount", oldMethods.size());
            row.put("newMethodCount", newMethods.size());
            if (same) identical++;
            else {
                semantic++;
                Path output = details.resolve(name.replace('/', '_') + ".json");
                Map<String, Object> bodies = new LinkedHashMap<>();
                for (String key : changed) bodies.put(key, Map.of("before", oldMethods.get(key), "after", newMethods.get(key)));
                for (String key : removed) bodies.put(key, Map.of("before", oldMethods.get(key), "after", "ABSENT"));
                for (String key : added) bodies.put(key, Map.of("before", "ABSENT", "after", newMethods.get(key)));
                Files.writeString(output, new GsonBuilder().setPrettyPrinting().create().toJson(bodies));
                row.put("methodEvidence", output.toString());
            }
            results.add(row);
        }
        Files.writeString(Path.of(arguments[1]), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
            "failures", 0, "guardMismatchCount", results.size(), "skipDebugEqualCount", identical,
            "classSemanticOrMetadataDifferenceCount", semantic, "entries", results,
            "guardPinsUpdated", false, "installationAccepted", false, "installable", false)));
        System.out.println("mismatches=" + results.size() + " skipDebugEqual=" + identical + " remaining=" + semantic);
    }
}
