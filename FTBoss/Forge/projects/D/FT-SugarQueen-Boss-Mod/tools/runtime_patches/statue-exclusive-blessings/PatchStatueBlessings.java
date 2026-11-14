import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class PatchStatueBlessings implements Opcodes {
    private static final String ROOT = "net/farmingtales/ftboss/item/";
    private static final Set<String> REPLACED = Set.of("blessingPool", "applyFoodBlessing", "foodBlessingViews", "drawGourmetSkill");

    private static ClassNode read(byte[] bytes) {
        ClassNode result = new ClassNode();
        new ClassReader(bytes).accept(result, 0);
        return result;
    }

    private static MethodNode method(ClassNode type, String name) {
        return type.methods.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
    }

    private static String trace(MethodNode method) {
        Textifier printer = new Textifier();
        method.accept(new TraceMethodVisitor(printer));
        StringWriter result = new StringWriter();
        printer.print(new PrintWriter(result));
        return result.toString();
    }

    private static void output(Path directory, String name, ClassNode node) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Path target = directory.resolve(ROOT + name + ".class");
        Files.createDirectories(target.getParent());
        Files.write(target, writer.toByteArray());
    }

    public static void main(String[] arguments) throws Exception {
        Path compiled = Path.of(arguments[1]);
        Path output = Path.of(arguments[2]);
        try (JarFile baseline = new JarFile(arguments[0])) {
            byte[] evolutionBytes = baseline.getInputStream(baseline.getJarEntry(ROOT + "FoodWeaponEvolution.class")).readAllBytes();
            ClassNode evolution = read(evolutionBytes);
            ClassNode replacement = read(Files.readAllBytes(compiled.resolve(ROOT + "FoodWeaponEvolution.class")));
            for (String name : REPLACED) {
                MethodNode old = method(evolution, name);
                MethodNode updated = method(replacement, name);
                if (!old.desc.equals(updated.desc)) throw new AssertionError("Changed descriptor: " + name);
                evolution.methods.set(evolution.methods.indexOf(old), updated);
            }
            MethodNode quote = method(evolution, "quoteFoodBlessingHits");
            if (quote.instructions.size() == 0) throw new AssertionError("Missing production hit quote");
            InsnList addition = new InsnList();
            for (int slot : new int[] {0, 1, 2, 3}) addition.add(new VarInsnNode(ALOAD, slot));
            addition.add(new VarInsnNode(LLOAD, 4));
            addition.add(new VarInsnNode(ALOAD, 6));
            addition.add(new VarInsnNode(ALOAD, 7));
            addition.add(new MethodInsnNode(INVOKESTATIC, ROOT + "StatueBlessingCombat", "quote", quote.desc, false));
            quote.instructions.insert(addition);
            ClassNode original = read(evolutionBytes);
            for (MethodNode old : original.methods) {
                if (!REPLACED.contains(old.name) && !old.name.equals(quote.name)
                        && !trace(old).equals(trace(evolution.methods.stream().filter(value -> value.name.equals(old.name)
                                && value.desc.equals(old.desc)).findFirst().orElseThrow()))) {
                    throw new AssertionError("Unrelated evolution method changed: " + old.name);
                }
            }
            output(output, "FoodWeaponEvolution", evolution);
            byte[] combatBytes = baseline.getInputStream(baseline.getJarEntry(ROOT + "FoodBlessingCombat.class")).readAllBytes();
            ClassNode combat = read(combatBytes);
            for (String name : List.of("blessingAttributes", "defense", "killFeast")) {
                MethodNode target = method(combat, name);
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(ALOAD, 0));
                if (name.equals("blessingAttributes")) hook.add(new VarInsnNode(ALOAD, 1));
                String called = name.equals("blessingAttributes") ? "attributes" : name.equals("killFeast") ? "kill" : name;
                hook.add(new MethodInsnNode(INVOKESTATIC, ROOT + "StatueBlessingCombat", called, target.desc, false));
                target.instructions.insert(hook);
            }
            for (MethodNode old : read(combatBytes).methods) {
                if (!Set.of("blessingAttributes", "defense", "killFeast").contains(old.name)
                        && !trace(old).equals(trace(combat.methods.stream().filter(value -> value.name.equals(old.name)
                                && value.desc.equals(old.desc)).findFirst().orElseThrow()))) {
                    throw new AssertionError("Unrelated blessing method changed: " + old.name);
                }
            }
            output(output, "FoodBlessingCombat", combat);
            String profile = ROOT + "FoodWeaponEvolution$Profile.class";
            Path target = output.resolve(profile);
            Files.createDirectories(target.getParent());
            Files.copy(compiled.resolve(profile), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("PASS surgical selection, 4 extended effect hooks, isolated profile pools and unchanged sibling methods");
        }
    }
}
