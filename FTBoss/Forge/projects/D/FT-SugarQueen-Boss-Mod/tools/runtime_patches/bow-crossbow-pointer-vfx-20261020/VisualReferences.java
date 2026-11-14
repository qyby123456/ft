import java.nio.charset.StandardCharsets;
import java.util.jar.JarFile;

/** 刀光注销边界检查；2026-10-20 排除仍有显式注册或外部渲染调用。 */
public final class VisualReferences {
    public static void main(String[] arguments) throws Exception {
        String name = "net/farmingtales/ftboss/client/render/GourmetBladeStreamVisual";
        try (var jar = new JarFile(arguments[0])) {
            for (var entry : jar.stream().filter(entry -> entry.getName().endsWith(".class")).toList()) {
                String bytes = new String(jar.getInputStream(entry).readAllBytes(), StandardCharsets.ISO_8859_1);
                if (!bytes.contains(name)) continue;
                System.out.println(entry.getName());
                if (!entry.getName().startsWith(name)) throw new AssertionError("External visual reference survives");
            }
        }
        System.out.println("PASS no external visual registration/callers");
    }
}
