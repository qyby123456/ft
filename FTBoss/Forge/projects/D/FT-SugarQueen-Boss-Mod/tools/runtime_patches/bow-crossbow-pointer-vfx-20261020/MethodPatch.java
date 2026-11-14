import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.*;

/** 弓弩及指针热修；2026-10-20 只替换准入方法、新客户端订阅器，删除旧刀光订阅注解。 */
public final class MethodPatch {
    private static final String GUARD = "net/farmingtales/ftboss/item/FourWeaponUseGuard.class";
    private static final String POINTER = "net/farmingtales/ftboss/client/SwordCavityPointerEvents.class";
    private static final Set<String> VISUALS = Set.of("net/farmingtales/ftboss/client/render/GourmetBladeStreamVisual.class", "net/farmingtales/ftboss/client/render/GourmetBladeStreamVisual$Registration.class");
    private static ClassNode read(byte[] bytes) { var node = new ClassNode(); new ClassReader(bytes).accept(node, 0); return node; }
    private static byte[] bytes(ClassNode node) { var writer = new ClassWriter(0); node.accept(writer); return writer.toByteArray(); }
    private static void strip(ClassNode node) {
        if (node.visibleAnnotations != null) node.visibleAnnotations.removeIf(annotation -> annotation.desc.equals("Lnet/minecraftforge/fml/common/Mod$EventBusSubscriber;"));
        for (MethodNode method : node.methods) if (method.visibleAnnotations != null) method.visibleAnnotations.removeIf(annotation -> annotation.desc.equals("Lnet/minecraftforge/eventbus/api/SubscribeEvent;"));
    }
    private static String text(ClassNode node) {
        var output = new StringWriter(); node.accept(new TraceClassVisitor(new PrintWriter(output))); return output.toString();
    }
    private static String text(MethodNode node) {
        var output = new StringWriter(); var tracer = new Textifier(); node.accept(new TraceMethodVisitor(tracer)); tracer.print(new PrintWriter(output)); return output.toString();
    }
    public static void main(String[] arguments) throws Exception {
        if (arguments[0].equals("check")) { verify(arguments[1], arguments[2]); return; }
        boolean overlay = arguments.length == 4;
        try (var input = new JarFile(arguments[0]); var output = new JarOutputStream(Files.newOutputStream(Path.of(arguments[2])))) {
            for (var entry : input.stream().toList()) {
                byte[] content = input.getInputStream(entry).readAllBytes();
                if (entry.getName().equals(GUARD)) {
                    ClassNode target = read(content);
                    ClassNode source = read(Files.readAllBytes(Path.of(arguments[1]).resolve(GUARD)));
                    MethodNode replacement = source.methods.stream().filter(method -> method.name.equals("blocksNativeSourceAction")).findFirst().orElseThrow();
                    for (int index = 0; index < target.methods.size(); index++) if (target.methods.get(index).name.equals(replacement.name)) target.methods.set(index, replacement);
                    content = overlay ? Files.readAllBytes(Path.of(arguments[1]).resolve(GUARD)) : bytes(target);
                } else if (VISUALS.contains(entry.getName())) {
                    ClassNode target = read(content); strip(target); content = bytes(target);
                }
                output.putNextEntry(new JarEntry(entry.getName())); output.write(content); output.closeEntry();
            }
            output.putNextEntry(new JarEntry(POINTER)); output.write(Files.readAllBytes(Path.of(arguments[1]).resolve(POINTER))); output.closeEntry();
        }
    }
    private static void verify(String beforePath, String afterPath) throws Exception {
        int unchanged = 0;
        Set<String> changed = new TreeSet<>();
        try (var before = new JarFile(beforePath); var after = new JarFile(afterPath)) {
            if (after.size() != before.size() + 1 || after.getEntry(POINTER) == null) throw new AssertionError("Unexpected entry set");
            for (var entry : before.stream().toList()) {
                byte[] previous = before.getInputStream(entry).readAllBytes();
                var next = after.getJarEntry(entry.getName());
                if (next == null) throw new AssertionError("Entry removed");
                byte[] current = after.getInputStream(next).readAllBytes();
                if (Arrays.equals(previous, current)) { unchanged++; continue; }
                changed.add(entry.getName());
                ClassNode oldClass = read(previous), newClass = read(current);
                if (VISUALS.contains(entry.getName())) {
                    strip(oldClass);
                    if (!text(oldClass).equals(text(newClass))) throw new AssertionError("Non-annotation visual change");
                } else if (entry.getName().equals(GUARD)) {
                    if (oldClass.methods.size() != newClass.methods.size() || oldClass.fields.size() != newClass.fields.size()) throw new AssertionError("Guard class shape changed");
                    for (MethodNode method : oldClass.methods) {
                        MethodNode match = newClass.methods.stream().filter(other -> other.name.equals(method.name) && other.desc.equals(method.desc)).findFirst().orElseThrow();
                        if (!method.name.equals("blocksNativeSourceAction") && !text(method).equals(text(match))) throw new AssertionError("Other guard method changed: " + method.name);
                    }
                } else throw new AssertionError("Unexpected change: " + entry.getName());
            }
        }
        var expected = new TreeSet<>(VISUALS); expected.add(GUARD);
        if (!changed.equals(expected)) throw new AssertionError("Incomplete patch " + changed);
        System.out.println("PASS exact delta: one guard method, two annotation-only classes, one added client subscriber; unchanged entries=" + unchanged);
    }
}
