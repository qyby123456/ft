import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

public final class Asm417 {
    public static void main(String[] arguments) throws Exception {
        int count = 0;
        try (var paths = Files.walk(Path.of(arguments[0]))) {
            for (Path path : paths.filter(candidate -> candidate.toString().endsWith(".class")).toList()) {
                StringWriter errors = new StringWriter();
                CheckClassAdapter.verify(new ClassReader(Files.readAllBytes(path)), false, new PrintWriter(errors));
                if (!errors.toString().isBlank()) throw new AssertionError(path + "\n" + errors);
                count++;
            }
        }
        Files.writeString(Path.of(arguments[1]), "{\"failures\":0,\"prototypeClassesChecked\":" + count + ",\"productionChanged\":false,\"installable\":false}");
    }
}
