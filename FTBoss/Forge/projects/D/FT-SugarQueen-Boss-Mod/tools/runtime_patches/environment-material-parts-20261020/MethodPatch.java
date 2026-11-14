import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.*;

/** 环境辅料最小补丁；两个纯规则方法、两处原生材料安全转换及显式分类表。 */
public final class MethodPatch {
    private static final String TARGET = "net/farmingtales/ftboss/util/SwordCompatibilityRules.class";
    private static final String NATIVE = "net/farmingtales/ftboss/item/SwordNativeMaterials.class";
    private static final String CAVITY = "net/farmingtales/ftboss/item/SwordCavityBuild.class";
    private static final String HELPER = "net/farmingtales/ftboss/util/WeaponEnvironmentMaterials.class";
    private static final Set<String> METHODS = Set.of("material", "itemFunctions");
    private static ClassNode read(byte[] content) { var node = new ClassNode(); new ClassReader(content).accept(node, ClassReader.EXPAND_FRAMES); return node; }
    private static String text(MethodNode method) {
        var output = new StringWriter(); var tracer = new Textifier(); method.accept(new TraceMethodVisitor(tracer)); tracer.print(new PrintWriter(output)); return output.toString();
    }
    public static void main(String[] arguments) throws Exception {
        if (arguments[0].equals("check")) { verify(arguments[1], arguments[2]); return; }
        boolean overlay = arguments.length == 4;
        int changed = 0;
        try (var input = new JarFile(arguments[0]); var output = new JarOutputStream(Files.newOutputStream(Path.of(arguments[2])))) {
            for (var entry : input.stream().toList()) {
                if (entry.getName().equals(HELPER)) throw new IllegalStateException("Baseline already contains helper");
                byte[] content = input.getInputStream(entry).readAllBytes();
                if (entry.getName().equals(TARGET)) {
                    var replacement = read(Files.readAllBytes(Path.of(arguments[1]).resolve(TARGET)));
                    if (overlay) content = Files.readAllBytes(Path.of(arguments[1]).resolve(TARGET));
                    else {
                        var target = read(content);
                        for (int index = 0; index < target.methods.size(); index++) {
                            var previous = target.methods.get(index);
                            if (!METHODS.contains(previous.name)) continue;
                            target.methods.set(index, replacement.methods.stream().filter(method -> method.name.equals(previous.name) && method.desc.equals(previous.desc)).findFirst().orElseThrow());
                            changed++;
                        }
                        if (changed != 2) throw new IllegalStateException("Expected two rule methods");
                        var writer = new ClassWriter(0); target.accept(writer); content = writer.toByteArray();
                    }
                }
                if (entry.getName().equals(NATIVE)) {
                    if (overlay) content = Files.readAllBytes(Path.of(arguments[1]).resolve(NATIVE));
                    else {
                        var target = read(content); guardNativeDefaults(target);
                        var writer = new ClassWriter(0); target.accept(writer); content = writer.toByteArray();
                    }
                }
                if (entry.getName().equals(CAVITY)) {
                    if (overlay) content = Files.readAllBytes(Path.of(arguments[1]).resolve(CAVITY));
                    else {
                        var target = read(content);
                        var source = read(Files.readAllBytes(Path.of(arguments[1]).resolve(CAVITY)));
                        var method = source.methods.stream().filter(value -> value.name.equals("nativeMaterial")).findFirst().orElseThrow();
                        int replacements = 0;
                        for (int index = 0; index < target.methods.size(); index++) if (target.methods.get(index).name.equals(method.name) && target.methods.get(index).desc.equals(method.desc)) { target.methods.set(index, method); replacements++; }
                        if (replacements != 1) throw new IllegalStateException("Expected native material method");
                        var writer = new ClassWriter(0); target.accept(writer); content = writer.toByteArray();
                    }
                }
                output.putNextEntry(new JarEntry(entry.getName())); output.write(content); output.closeEntry();
            }
            output.putNextEntry(new JarEntry(HELPER)); output.write(Files.readAllBytes(Path.of(arguments[1]).resolve(HELPER))); output.closeEntry();
        }
    }
    private static void guardNativeDefaults(ClassNode target) {
        var method = target.methods.stream().filter(value -> value.name.equals("defaults")).findFirst().orElseThrow();
        int changes = 0;
        for (var instruction : method.instructions.toArray()) {
            if (!(instruction instanceof TypeInsnNode cast) || cast.getOpcode() != Opcodes.CHECKCAST || !cast.desc.equals("net/minecraft/world/item/SwordItem")) continue;
            var getter = (MethodInsnNode) cast.getNext();
            if (!getter.name.equals("getTier")) throw new IllegalStateException("Unexpected native tier getter");
            var label = new LabelNode();
            var guard = new InsnList();
            guard.add(new InsnNode(Opcodes.DUP));
            guard.add(new TypeInsnNode(Opcodes.INSTANCEOF, "net/minecraft/world/item/TieredItem"));
            guard.add(new JumpInsnNode(Opcodes.IFNE, label));
            guard.add(new InsnNode(Opcodes.POP));
            guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/List", "of", "()Ljava/util/List;", true));
            guard.add(new InsnNode(Opcodes.ARETURN));
            guard.add(label);
            guard.add(new FrameNode(Opcodes.F_NEW, 3, new Object[]{"net/minecraft/world/item/ItemStack", "java/lang/String", "java/util/List"}, 1, new Object[]{"net/minecraft/world/item/Item"}));
            method.instructions.insertBefore(cast, guard);
            cast.desc = "net/minecraft/world/item/TieredItem"; getter.owner = cast.desc;
            method.maxStack = Math.max(method.maxStack, 2);
            changes++;
        }
        if (changes != 1) throw new IllegalStateException("Expected one unsafe native fallback cast, found " + changes);
    }
    private static void verify(String beforePath, String afterPath) throws Exception {
        int changed = 0, unchanged = 0;
        try (var before = new JarFile(beforePath); var after = new JarFile(afterPath)) {
            if (before.getEntry(HELPER) != null || after.getEntry(HELPER) == null || after.size() != before.size() + 1) throw new AssertionError("Unexpected entry set");
            for (var entry : before.stream().toList()) {
                byte[] previous = before.getInputStream(entry).readAllBytes();
                var next = after.getJarEntry(entry.getName());
                if (next == null) throw new AssertionError("Entry removed");
                byte[] current = after.getInputStream(next).readAllBytes();
                if (Arrays.equals(previous, current)) { unchanged++; continue; }
                if (!entry.getName().equals(TARGET) && !entry.getName().equals(NATIVE) && !entry.getName().equals(CAVITY)) throw new AssertionError("Unrelated change: " + entry.getName());
                var oldClass = read(previous); var newClass = read(current);
                if (oldClass.methods.size() != newClass.methods.size() || oldClass.fields.size() != newClass.fields.size()) throw new AssertionError("Class shape changed");
                for (var method : oldClass.methods) {
                    var match = newClass.methods.stream().filter(other -> other.name.equals(method.name) && other.desc.equals(method.desc)).findFirst().orElseThrow();
                    if (!text(method).equals(text(match))) {
                        if (!(entry.getName().equals(TARGET) && METHODS.contains(method.name)) && !(entry.getName().equals(NATIVE) && method.name.equals("defaults")) && !(entry.getName().equals(CAVITY) && method.name.equals("nativeMaterial"))) throw new AssertionError("Unrelated method changed: " + method.name);
                        changed++;
                    }
                }
            }
        }
        if (changed != 4) throw new AssertionError("Expected four changed methods: " + changed);
        System.out.println("PASS two material rule methods, two safe native fallbacks and one helper; unchanged entries=" + unchanged);
    }
}
