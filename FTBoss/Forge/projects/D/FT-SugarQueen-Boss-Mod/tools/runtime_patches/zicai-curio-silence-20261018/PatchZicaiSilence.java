import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class PatchZicaiSilence {
    public static final String HANDLER = "net/farmingtales/ftboss/boss/ai/ZicaiCurioSilence";
    public static final String RULES = "net/farmingtales/ftboss/util/ZicaiCurioSilenceRules";

    public static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    public static byte[] entry(ZipFile archive, String name) throws Exception {
        check(archive.getEntry(name) != null, "Missing JAR entry: " + name);
        return archive.getInputStream(archive.getEntry(name)).readAllBytes();
    }

    public static String hash(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    public static void main(String[] arguments) throws Exception {
        Path baseline = Path.of(arguments[0]);
        Path output = Path.of(arguments[1]);
        Path classes = Path.of(arguments[2]);
        Map<String, byte[]> additions = new LinkedHashMap<>();
        for (String name : new String[]{HANDLER, HANDLER + "$Choice", RULES}) {
            byte[] bytes = Files.readAllBytes(classes.resolve(name + ".class"));
            ClassNode node = new ClassNode();
            new ClassReader(bytes).accept(node, 0);
            check(node.version == 61 && node.name.equals(name), "Java 17 class ABI: " + name);
            for (var method : node.methods) new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
            additions.put(name + ".class", bytes);
        }
        int preserved = 0;
        try (ZipFile current = new ZipFile(baseline.toFile()); ZipOutputStream target = new ZipOutputStream(Files.newOutputStream(output))) {
            for (String name : additions.keySet()) check(current.getEntry(name) == null, "Silence already installed: " + name);
            var entries = current.entries();
            while (entries.hasMoreElements()) {
                ZipEntry previous = entries.nextElement();
                ZipEntry next = new ZipEntry(previous.getName());
                next.setTime(previous.getTime());
                target.putNextEntry(next);
                target.write(entry(current, previous.getName()));
                target.closeEntry();
            }
            for (var addition : additions.entrySet()) {
                target.putNextEntry(new ZipEntry(addition.getKey()));
                target.write(addition.getValue());
                target.closeEntry();
            }
        }
        try (ZipFile before = new ZipFile(baseline.toFile()); ZipFile after = new ZipFile(output.toFile())) {
            check(after.size() == before.size() + additions.size(), "Only three new classes");
            var entries = before.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                check(Arrays.equals(entry(before, name), entry(after, name)), "Existing entry changed: " + name);
                preserved++;
            }
        }
        var audit = Map.of("mode", "ADDITIVE_CURRENT_RUNTIME_MERGE", "baselineSHA256", hash(Files.readAllBytes(baseline)),
                "candidateSHA256", hash(Files.readAllBytes(output)), "added", additions.keySet(), "originalEntriesPreserved", preserved,
                "silenceTicks", 100, "inGameVerified", false);
        Files.writeString(Path.of(output + ".audit.json"), new GsonBuilder().setPrettyPrinting().create().toJson(audit));
        System.out.println("PASS additive silence merge; all " + preserved + " original entries byte-identical");
    }
}
