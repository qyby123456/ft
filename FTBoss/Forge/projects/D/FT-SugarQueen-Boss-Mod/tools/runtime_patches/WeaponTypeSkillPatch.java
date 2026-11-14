import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class WeaponTypeSkillPatch implements Opcodes {
    private static final String BASE = "net/farmingtales/ftboss/";
    private static final String EXECUTOR = BASE + "skill/WeaponEvolutionSkills";
    private static final String HELPER = BASE + "skill/WeaponTypeSkills";

    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(name + ".class")).readAllBytes()).accept(type, 0);
        return type;
    }

    private static MethodNode method(ClassNode type, String name) {
        return type.methods.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
    }

    private static void write(Path output, ClassNode type) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path path = output.resolve(type.name + ".class");
        Files.createDirectories(path.getParent());
        Files.write(path, writer.toByteArray());
    }

    private static MethodInsnNode description() {
        return new MethodInsnNode(INVOKESTATIC, HELPER, "description",
                "(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/network/chat/Component;", false);
    }

    private static void descriptions(MethodNode method, int baseCount, int fusionCount) {
        int bases = 0;
        int fusions = 0;
        for (var instruction : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode call)
                    || !call.owner.startsWith(BASE + "util/WeaponEvolutionSkillPool$")
                    || !List.of("descriptionKey", "styleDescriptionKey").contains(call.name)) continue;
            boolean fusion = call.name.equals("styleDescriptionKey");
            var receiver = call.getPrevious();
            var translate = call.getNext();
            while (receiver != null && receiver.getOpcode() < 0) receiver = receiver.getPrevious();
            while (translate != null && translate.getOpcode() < 0) translate = translate.getNext();
            if (!(receiver instanceof VarInsnNode) || !(translate instanceof MethodInsnNode next)
                    || !next.owner.equals("net/minecraft/network/chat/Component") || !next.name.equals("m_237115_")) {
                throw new IllegalStateException("Unexpected description expression in " + method.name);
            }
            InsnList replacement = new InsnList();
            replacement.add(new VarInsnNode(ALOAD, 0));
            replacement.add(new InsnNode(fusion && fusions > 0 ? ICONST_1 : ICONST_0));
            replacement.add(description());
            method.instructions.insertBefore(receiver, replacement);
            method.instructions.remove(receiver);
            method.instructions.remove(call);
            method.instructions.remove(translate);
            if (fusion) fusions++; else bases++;
        }
        if (bases != baseCount || fusions != fusionCount) throw new IllegalStateException("Unexpected description count: " + method.name + " " + bases + "/" + fusions);
    }

    public static void main(String[] arguments) throws Exception {
        try (JarFile baseline = new JarFile(arguments[0]); JarFile compiled = new JarFile(arguments[1])) {
            Path output = Path.of(arguments[2]);
            ClassNode target = read(baseline, EXECUTOR);
            ClassNode source = read(compiled, EXECUTOR);
            for (String name : List.of("executeBase", "executePrimary", "executeSecondary")) {
                MethodNode replacement = method(source, name);
                MethodNode previous = method(target, name);
                if (!replacement.desc.equals(previous.desc)) throw new IllegalStateException("Executor signature changed");
                target.methods.set(target.methods.indexOf(previous), replacement);
            }
            target.methods.add(method(source, "executeTyped"));
            for (String name : List.of("applyTraits", "applyBoons")) method(target, name).access &= ~ACC_PRIVATE;
            MethodNode cast = method(target, "cast");
            int messages = 0;
            for (var instruction : cast.instructions.toArray()) {
                if (!(instruction instanceof LdcInsnNode text) || !"message.ftboss.evolution.no_target".equals(text.cst)) continue;
                var translate = text.getNext();
                if (!(translate instanceof MethodInsnNode call) || !call.name.equals("m_237115_")) throw new IllegalStateException("Unexpected failure message");
                cast.instructions.insertBefore(text, new VarInsnNode(ALOAD, 1));
                cast.instructions.set(translate, new MethodInsnNode(INVOKESTATIC, HELPER, "failureMessage",
                        "(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/chat/Component;", false));
                cast.instructions.remove(text);
                messages++;
            }
            if (messages != 1) throw new IllegalStateException("Missing failure message");
            write(output, target);
            ClassNode evolution = read(baseline, BASE + "item/FoodWeaponEvolution");
            descriptions(method(evolution, "weaponDetailViews"), 2, 2);
            write(output, evolution);
            ClassNode progression = read(baseline, BASE + "item/FoodWeaponProgression");
            descriptions(method(progression, "details"), 1, 0);
            write(output, progression);
            ClassNode nativeSkills = read(baseline, BASE + "skill/NonManorWeaponSkills");
            method(nativeSkills, "execute").name = "executeLegacy";
            nativeSkills.methods.add(method(read(compiled, nativeSkills.name), "execute"));
            MethodNode nativeCast = method(nativeSkills, "cast");
            int nativeMessages = 0;
            for (var instruction : nativeCast.instructions.toArray()) {
                if (!(instruction instanceof LdcInsnNode text) || !"message.ftboss.non_manor_no_target".equals(text.cst)) continue;
                var translate = text.getNext();
                if (!(translate instanceof MethodInsnNode call) || !call.name.equals("m_237115_")) throw new IllegalStateException("Unexpected native failure message");
                nativeCast.instructions.insertBefore(text, new VarInsnNode(ALOAD, 2));
                nativeCast.instructions.set(translate, new MethodInsnNode(INVOKESTATIC, HELPER, "failureMessage",
                        "(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/chat/Component;", false));
                nativeCast.instructions.remove(text);
                nativeMessages++;
            }
            if (nativeMessages != 1) throw new IllegalStateException("Missing native failure message");
            write(output, nativeSkills);
            System.out.println("PASS preserved runtime casting/state, patched typed executors and five descriptions");
        }
    }
}
