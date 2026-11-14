import java.nio.file.Files;
import java.nio.file.Path;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;

public final class ZicaiThemeAfterimagePatch {
    public static void main(String[] arguments) throws Exception {
        Path compiled = Path.of(arguments[0]);
        Path output = Path.of(arguments[1]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        try (var files = Files.walk(compiled)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".class")).toList()) {
                String relative = compiled.relativize(file).toString().replace('\\', '/');
                boolean runtime = relative.startsWith("net/farmingtales/ftboss/") && (relative.contains("ZheergenAfterimage")
                        || relative.contains("ZicaiVoidSpells") || relative.contains("ZicaiVoidVisual")
                        || relative.endsWith("/ManorSpellRules$Kind.class"));
                boolean preview = relative.equals("ZheergenAfterimagePreview.class");
                if (!runtime && !preview) continue;
                ClassNode type = new ClassNode();
                new ClassReader(Files.readAllBytes(file)).accept(new ClassRemapper(type, names), 0);
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                type.accept(writer);
                Path target = runtime ? output.resolve(relative) : output.getParent().resolve("preview-classes").resolve(relative);
                Files.createDirectories(target.getParent());
                Files.write(target, writer.toByteArray());
            }
        }
        System.out.println("PASS remapped holy theme and independently implemented client afterimages");
    }
}
