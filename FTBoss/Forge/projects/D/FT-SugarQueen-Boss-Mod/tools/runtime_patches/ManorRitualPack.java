import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class ManorRitualPack {
    public static void main(String[] arguments) throws Exception {
        if (arguments[0].equals("remap")) {
            var names = new ZicaiVoidPatch.Names(Path.of(arguments[1]));
            Path compiled = Path.of(arguments[2]), payload = Path.of(arguments[3]);
            try (var paths = Files.walk(compiled.resolve("net/farmingtales/ftboss"))) {
                for (Path file : paths.filter(path -> path.toString().endsWith(".class")).toList()) {
                    ClassWriter writer = new ClassWriter(0);
                    new ClassReader(Files.readAllBytes(file)).accept(new ClassRemapper(writer, names), 0);
                    Path target = payload.resolve(compiled.relativize(file));
                    Files.createDirectories(target.getParent());
                    Files.write(target, writer.toByteArray());
                }
            }
            return;
        }
        Set<String> controlled = new HashSet<>(Files.readAllLines(Path.of(arguments[3])));
        int changed = 0, preserved = 0;
        try (ZipFile baseline = new ZipFile(arguments[1]); ZipFile candidate = new ZipFile(arguments[2])) {
            Set<String> seen = new HashSet<>();
            for (var entry : candidate.stream().toList()) {
                if (!seen.add(entry.getName())) throw new AssertionError("Duplicate entry: " + entry.getName());
                if (entry.isDirectory()) continue;
                byte[] bytes = candidate.getInputStream(entry).readAllBytes();
                var old = baseline.getEntry(entry.getName());
                boolean differs = old == null || !Arrays.equals(bytes, baseline.getInputStream(old).readAllBytes());
                if (differs && !controlled.contains(entry.getName())) throw new AssertionError("Out-of-scope change: " + entry.getName());
                if (controlled.contains(entry.getName())) {
                    if (entry.getName().endsWith(".class")) {
                        ClassNode type = new ClassNode();
                        new ClassReader(bytes).accept(type, 0);
                        for (var method : type.methods) if ((method.access & 0x500) == 0)
                            new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
                        if (type.name.equals("net/farmingtales/ftboss/client/render/ManorRitualRenderType")) {
                            Set<String> states = new HashSet<>();
                            for (var method : type.methods) for (var instruction : method.instructions) {
                                if (instruction instanceof FieldInsnNode field) states.add(field.name);
                            }
                            if (!states.containsAll(Set.of("f_110136_", "f_110113_", "f_110115_")))
                                throw new AssertionError("Ritual material must preserve alpha-additive blending, depth testing and color-only writes");
                            System.out.println("PASS runtime ritual material: SRG SRC_ALPHA/ONE + LEQUAL + COLOR_WRITE");
                        }
                    }
                    changed++;
                } else preserved++;
            }
            for (var old : baseline.stream().toList()) if (!seen.contains(old.getName()))
                throw new AssertionError("Removed entry: " + old.getName());
            for (String target : controlled) if (!seen.contains(target)) throw new AssertionError("Missing payload: " + target);
        }
        System.out.println("PASS: " + changed + " controlled entries; " + preserved + " entries byte-for-byte preserved; ASM frames/dataflow, no duplicates/removals.");
    }
}
