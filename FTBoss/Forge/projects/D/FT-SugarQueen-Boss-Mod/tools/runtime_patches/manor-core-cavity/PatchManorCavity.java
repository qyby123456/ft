import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class PatchManorCavity implements Opcodes {
    private static final String BASE = "net/farmingtales/ftboss/";
    private static final String STACK = "(Lnet/minecraft/world/item/ItemStack;)Z";
    private static final Map<String, Set<String>> METHODS = Map.of(
            "item/FourWeaponFoundationAdapter", Set.of("applies"),
            "item/FoodWeaponEvolution", Set.of("weaponCandidate", "maxLevel"),
            "item/SwordCavityBuild", Set.of("applies", "editable"),
            "item/FoodWeaponLifecycle", Set.of("ensureTracked"));

    private static ClassNode read(JarFile archive, String name) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(archive.getInputStream(archive.getJarEntry(BASE + name + ".class")).readAllBytes()).accept(type, 0);
        return type;
    }

    private static MethodNode method(ClassNode type, String name) {
        return type.methods.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
    }

    private static void prefix(MethodNode method, String test, int result) {
        InsnList guard = new InsnList();
        LabelNode original = new LabelNode();
        guard.add(new VarInsnNode(ALOAD, 0));
        guard.add(new MethodInsnNode(INVOKESTATIC, BASE + "item/ManorWeaponCavity", test, STACK, false));
        guard.add(new JumpInsnNode(IFEQ, original));
        guard.add(new InsnNode(result));
        guard.add(new InsnNode(IRETURN));
        guard.add(original);
        guard.add(new FrameNode(F_SAME, 0, null, 0, null));
        method.instructions.insert(guard);
    }

    public static void main(String[] arguments) throws Exception {
        try (JarFile baseline = new JarFile(arguments[0])) {
            if (arguments.length == 2) {
                try (JarFile candidate = new JarFile(arguments[1])) {
                    for (var entry : METHODS.entrySet()) {
                        ClassNode before = read(baseline, entry.getKey());
                        ClassNode after = read(candidate, entry.getKey());
                        for (MethodNode original : before.methods) {
                            if (entry.getValue().contains(original.name)) continue;
                            MethodNode actual = after.methods.stream().filter(value -> value.name.equals(original.name)
                                    && value.desc.equals(original.desc)).findFirst().orElseThrow();
                            if (!text(original).equals(text(actual))) throw new AssertionError("Non-target method changed: " + entry.getKey() + "/" + original.name);
                        }
                    }
                }
                System.out.println("PASS non-target method preservation");
                return;
            }
            for (var entry : METHODS.entrySet()) {
                ClassNode type = read(baseline, entry.getKey());
                switch (entry.getKey()) {
                    case "item/FourWeaponFoundationAdapter" -> prefix(method(type, "applies"), "applies", ICONST_0);
                    case "item/FoodWeaponEvolution" -> {
                        prefix(method(type, "weaponCandidate"), "applies", ICONST_1);
                        prefix(method(type, "maxLevel"), "applies", ICONST_5);
                    }
                    case "item/SwordCavityBuild" -> {
                        prefix(method(type, "applies"), "applies", ICONST_1);
                        prefix(method(type, "editable"), "pendingMigration", ICONST_0);
                    }
                    case "item/FoodWeaponLifecycle" -> {
                        int changed = 0;
                        for (var instruction : method(type, "ensureTracked").instructions) {
                            if (instruction instanceof MethodInsnNode call && call.owner.equals(BASE + "item/GourmetBladeCavity") && call.name.equals("applies")) {
                                call.name = "identityOnly";
                                changed++;
                            }
                        }
                        if (changed != 1) throw new AssertionError("Lifecycle branch count " + changed);
                        InsnList identity = new InsnList();
                        identity.add(new VarInsnNode(ALOAD, 0));
                        identity.add(new MethodInsnNode(INVOKESTATIC, BASE + "item/GourmetBladeCavity", "ensureIdentity", "(Lnet/minecraft/world/item/ItemStack;)V", false));
                        method(type, "ensureTracked").instructions.insert(identity);
                    }
                }
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                type.accept(writer);
                Path output = Path.of(arguments[2]).resolve(type.name + ".class");
                Files.createDirectories(output.getParent());
                Files.write(output, writer.toByteArray());
            }
            System.out.println("PASS manor-only eligibility, five-stage cap and lifecycle routing");
        }
    }

    private static String text(MethodNode method) {
        var output = new java.io.StringWriter();
        var printer = new org.objectweb.asm.util.Textifier();
        method.accept(new org.objectweb.asm.util.TraceMethodVisitor(printer));
        printer.print(new java.io.PrintWriter(output));
        return output.toString();
    }
}
