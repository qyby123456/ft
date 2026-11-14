import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class WeaponHoverTooltipPatch {
    private static final String ROOT = "net/farmingtales/ftboss/";

    private static ClassNode read(byte[] bytes) {
        ClassNode result = new ClassNode();
        new ClassReader(bytes).accept(result, 0);
        return result;
    }

    public static MethodNode method(ClassNode type, String name) {
        return type.methods.stream().filter(value -> value.name.equals(name)).findFirst().orElseThrow();
    }

    public static void moveMonocleCheck(MethodNode tooltip) {
        for (var instruction : tooltip.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode call) || !call.owner.equals(ROOT + "item/FoodAppraisalMonocleItem")
                    || !call.name.equals("isEquipped")) continue;
            var start = call.getPrevious().getPrevious();
            var branch = call.getNext();
            var exit = branch.getNext();
            var label = exit.getNext();
            var frame = label.getNext();
            while (frame instanceof org.objectweb.asm.tree.LineNumberNode) frame = frame.getNext();
            if (start.getOpcode() != Opcodes.ALOAD || branch.getOpcode() != Opcodes.IFNE
                    || exit.getOpcode() != Opcodes.RETURN || !(label instanceof org.objectweb.asm.tree.LabelNode)
                    || !(frame instanceof org.objectweb.asm.tree.FrameNode)) throw new IllegalStateException("Unexpected monocle guard");
            org.objectweb.asm.tree.InsnList prefix = new org.objectweb.asm.tree.InsnList();
            var cursor = start;
            while (cursor != label) {
                var next = cursor.getNext();
                tooltip.instructions.remove(cursor);
                prefix.add(cursor);
                cursor = next;
            }
            var early = new org.objectweb.asm.tree.LabelNode();
            ((org.objectweb.asm.tree.JumpInsnNode) branch).label = early;
            prefix.add(early);
            prefix.add(new org.objectweb.asm.tree.FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            tooltip.instructions.insert(prefix);
            return;
        }
        throw new IllegalStateException("Missing monocle guard");
    }

    public static void main(String[] arguments) throws Exception {
        Path compiled = Path.of(arguments[1]);
        Path output = Path.of(arguments[2]);
        Map<String, Set<String>> additions = Map.of(ROOT + "item/FoodWeaponEvolution", Set.of("selectableSkillCount"),
                ROOT + "item/FoodWeaponSkillLoadout", Set.of("carriedSkillCount"), ROOT + "client/FoodWeaponSkillWheel", Set.of());
        try (JarFile baseline = new JarFile(arguments[0])) {
            for (var entry : additions.entrySet()) {
                String name = entry.getKey();
                ClassNode original = read(baseline.getInputStream(baseline.getJarEntry(name + ".class")).readAllBytes());
                ClassNode replacement = read(Files.readAllBytes(compiled.resolve(name + ".class")));
                for (String added : entry.getValue()) {
                    if (original.methods.stream().anyMatch(value -> value.name.equals(added))) throw new IllegalStateException("Already patched: " + added);
                    original.methods.add(method(replacement, added));
                }
                if (name.endsWith("FoodWeaponSkillWheel")) {
                    MethodNode previous = method(original, "tooltip");
                    original.methods.set(original.methods.indexOf(previous), method(replacement, "tooltip"));
                }
                if (name.endsWith("FoodWeaponEvolution")) moveMonocleCheck(method(original, "tooltip"));
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                original.accept(writer);
                Path file = output.resolve(name + ".class");
                Files.createDirectories(file.getParent());
                Files.write(file, writer.toByteArray());
            }
        }
        System.out.println("PASS hover patch: two read-only count methods, one tooltip call, one early equipment gate");
    }
}
