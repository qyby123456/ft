import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class VerifyJarStream {
    public static void main(String[] arguments) throws Exception {
        Set<String> names = new HashSet<>();
        try (ZipInputStream stream = new ZipInputStream(Files.newInputStream(Path.of(arguments[0])))) {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                if (!names.add(entry.getName())) throw new IllegalStateException("Duplicate ZIP entry: " + entry.getName());
                stream.transferTo(OutputStream.nullOutputStream());
                stream.closeEntry();
            }
        }
        if (!names.contains("cn/farmingtales/qysevent/wishingwell/GoldPotWishes.class")) {
            throw new IllegalStateException("Missing gold-pot wish implementation");
        }
        System.out.println("JAR stream verified: " + names.size() + " unique entries, no CRC or descriptor errors.");
    }
}
