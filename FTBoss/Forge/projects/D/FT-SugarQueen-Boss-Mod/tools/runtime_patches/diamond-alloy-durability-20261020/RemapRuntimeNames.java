import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.io.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

public final class RemapRuntimeNames {
    public static void main(String[] arguments) throws Exception {
        Map<String, String> methods = new HashMap<>();
        Map<String, String> fields = new HashMap<>();
        for (String line : Files.readAllLines(Path.of(arguments[0]))) {
            if (!line.startsWith("\t") || line.startsWith("\t\t")) continue;
            String[] parts = line.trim().split(" +");
            if (parts.length == 3 && parts[2].startsWith("m_")) methods.put(parts[2], parts[0]);
            if (parts.length == 2 && parts[1].startsWith("f_")) fields.put(parts[1], parts[0]);
        }
        Remapper names = new Remapper() {
            @Override public String mapMethodName(String owner, String name, String descriptor) { return methods.getOrDefault(name, name); }
            @Override public String mapFieldName(String owner, String name, String descriptor) { return fields.getOrDefault(name, name); }
        };
        try (JarFile input = new JarFile(arguments[1]); JarOutputStream output = new JarOutputStream(Files.newOutputStream(Path.of(arguments[2])))) {
            var entries = input.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                byte[] bytes = input.getInputStream(entry).readAllBytes();
                if (entry.getName().endsWith(".class")) {
                    ClassWriter writer = new ClassWriter(0);
                    new ClassReader(bytes).accept(new ClassRemapper(writer, names), 0);
                    bytes = writer.toByteArray();
                }
                output.putNextEntry(new JarEntry(entry.getName()));
                output.write(bytes);
                output.closeEntry();
            }
        }
        System.out.println("PASS offline SRG-name bridge: " + methods.size() + " methods, " + fields.size() + " fields");
    }
}
