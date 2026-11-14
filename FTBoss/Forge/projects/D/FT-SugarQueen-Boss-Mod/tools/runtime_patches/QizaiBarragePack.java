import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

public final class QizaiBarragePack {
    public static void main(String[] arguments) throws Exception {
        Path classes = Path.of(arguments[0]);
        Path payload = Path.of(arguments[1]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        int count = 0;
        try (var files = Files.walk(classes.resolve("net/farmingtales/ftboss"))) {
            for (Path file : files.filter(path -> path.getFileName().toString().startsWith("QizaiBarrage")
                    && path.toString().endsWith(".class")).toList()) {
                ClassWriter writer = new ClassWriter(0);
                new ClassReader(Files.readAllBytes(file)).accept(new ClassRemapper(writer, names), 0);
                Path output = payload.resolve(classes.relativize(file));
                Files.createDirectories(output.getParent());
                Files.write(output, writer.toByteArray());
                count++;
            }
        }
        if (count != 9) throw new IllegalStateException("Unexpected runtime class count: " + count);
        System.out.println("PASS remapped " + count + " new classes; no existing class replaced");
    }
}
