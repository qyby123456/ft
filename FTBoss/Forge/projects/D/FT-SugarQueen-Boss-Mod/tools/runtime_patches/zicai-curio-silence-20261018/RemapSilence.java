import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;
import net.minecraftforge.srgutils.IMappingFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

public final class RemapSilence {
    public static void main(String[] arguments) throws Exception {
        IMappingFile mappings = IMappingFile.load(Path.of(arguments[0]).toFile());
        Path input = Path.of(arguments[1]);
        Path output = Path.of(arguments[2]);
        Path fallback = Path.of(arguments[4]);
        Map<String, ClassReader> hierarchy = new HashMap<>();
        try (ZipFile minecraft = new ZipFile(arguments[3])) {
            Remapper remapper = new Remapper() {
                private String member(String owner, String name, String descriptor, boolean method) {
                    var mapped = mappings.getClass(owner);
                    if (mapped != null) {
                        String result = method ? mapped.remapMethod(name, descriptor) : mapped.remapField(name);
                        if (!result.equals(name)) return result;
                    }
                    ClassReader type = hierarchy.get(owner);
                    if (type == null) {
                        try {
                            Path own = input.resolve(owner + ".class");
                            Path inherited = fallback.resolve(owner + ".class");
                            var entry = minecraft.getEntry(owner + ".class");
                            if (entry != null) type = new ClassReader(minecraft.getInputStream(entry));
                            else if (Files.exists(own)) type = new ClassReader(Files.readAllBytes(own));
                            else if (Files.exists(inherited)) type = new ClassReader(Files.readAllBytes(inherited));
                        } catch (Exception exception) { throw new IllegalStateException(exception); }
                        if (type != null) hierarchy.put(owner, type);
                    }
                    if (type == null) return name;
                    if (type.getSuperName() != null) {
                        String result = member(type.getSuperName(), name, descriptor, method);
                        if (!result.equals(name)) return result;
                    }
                    for (String parent : type.getInterfaces()) {
                        String result = member(parent, name, descriptor, method);
                        if (!result.equals(name)) return result;
                    }
                    return name;
                }

                @Override public String mapMethodName(String owner, String name, String descriptor) {
                    return name.startsWith("<") ? name : member(owner, name, descriptor, true);
                }

                @Override public String mapFieldName(String owner, String name, String descriptor) {
                    return member(owner, name, descriptor, false);
                }
            };
            try (var files = Files.walk(input)) {
                for (Path source : files.filter(path -> path.toString().endsWith(".class")).toList()) {
                    ClassWriter writer = new ClassWriter(0);
                    new ClassReader(Files.readAllBytes(source)).accept(new ClassRemapper(writer, remapper), 0);
                    Path destination = output.resolve(input.relativize(source));
                    Files.createDirectories(destination.getParent());
                    Files.write(destination, writer.toByteArray());
                }
            }
        }
    }
}
