import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public final class ShadowOverlayRegistrationPatch {
    public static final String TARGET = "net/farmingtales/ftboss/client/ShadowDarknessOverlay$Registration";
    public static final String METHOD = "registerShadowDarknessOverlay";
    public static final String DESCRIPTOR = "(Lnet/minecraftforge/client/event/RegisterGuiOverlaysEvent;)V";

    private ShadowOverlayRegistrationPatch() {}

    public static void main(String[] arguments) throws Exception {
        ClassNode type = new ClassNode();
        try (ZipFile archive = new ZipFile(arguments[0])) {
            var entry = archive.getEntry(TARGET + ".class");
            if (entry == null) throw new IllegalStateException("Missing darkness overlay registration");
            try (var input = archive.getInputStream(entry)) { new ClassReader(input.readAllBytes()).accept(type, 0); }
        }
        var matches = type.methods.stream().filter(method -> method.name.equals("register") && method.desc.equals(DESCRIPTOR)).toList();
        if (matches.size() != 1) throw new IllegalStateException("Unexpected registration method; refusing blind overwrite");
        if (type.methods.stream().anyMatch(method -> method.name.equals(METHOD))) throw new IllegalStateException("Patch already present");
        matches.get(0).name = METHOD;
        ClassWriter output = new ClassWriter(0);
        type.accept(output);
        Path destination = Path.of(arguments[1]).resolve(TARGET + ".class");
        Files.createDirectories(destination.getParent());
        Files.write(destination, output.toByteArray());
        System.out.println("PATCH single registration method: " + TARGET + ".register -> " + METHOD);
    }
}
