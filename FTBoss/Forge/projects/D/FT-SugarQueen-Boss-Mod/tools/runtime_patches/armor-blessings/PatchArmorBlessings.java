import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class PatchArmorBlessings {
    private static ClassNode read(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }
    public static void main(String[] arguments) throws Exception {
        try (JarFile baseline = new JarFile(arguments[0])) {
            for (String name : List.of("item/FoodWeaponEvolution", "client/StatueHubScreen", "item/StatueBlessingCombat")) {
                String entry = "net/farmingtales/ftboss/" + name + ".class";
                ClassNode original = read(baseline.getInputStream(baseline.getJarEntry(entry)).readAllBytes());
                ClassNode replacement = read(Files.readAllBytes(Path.of(arguments[1]).resolve(entry)));
                List<String> methods = name.equals("item/FoodWeaponEvolution") ? List.of("applyFoodBlessing", "foodBlessingViews",
                        "isBlessingCandidate", "applyFoodBlessingFromUi") : name.startsWith("client/") ? List.of("evolutionWeapon") : List.of("add");
                for (String method : methods) {
                    MethodNode previous = original.methods.stream().filter(value -> value.name.equals(method)).findFirst().orElseThrow();
                    MethodNode updated = replacement.methods.stream().filter(value -> value.name.equals(method)
                            && value.desc.equals(previous.desc)).findFirst().orElseThrow();
                    original.methods.set(original.methods.indexOf(previous), updated);
                }
                if (name.equals("item/StatueBlessingCombat")) {
                    MethodNode attributes = original.methods.stream().filter(value -> value.name.equals("attributes")).findFirst().orElseThrow();
                    boolean rat = false;
                    int patched = 0;
                    for (var instruction : attributes.instructions.toArray()) {
                        if (instruction instanceof LdcInsnNode literal && literal.cst.equals("rat_whisker_luck")) {
                            var next = instruction.getNext();
                            while (next != null && next.getOpcode() < 0) next = next.getNext();
                            rat = next instanceof FieldInsnNode;
                        }
                        if (rat && instruction instanceof FieldInsnNode field && field.owner.equals("net/minecraftforge/common/ForgeMod")
                                && field.name.equals("ENTITY_REACH")) patched++;
                        if (rat && instruction instanceof FieldInsnNode field && field.owner.equals("net/minecraft/world/entity/ai/attributes/Attributes")) {
                            InsnList reach = new InsnList();
                            reach.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraftforge/common/ForgeMod", "ENTITY_REACH", "Lnet/minecraftforge/registries/RegistryObject;"));
                            reach.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraftforge/registries/RegistryObject", "get", "()Ljava/lang/Object;", false));
                            reach.add(new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/world/entity/ai/attributes/Attribute"));
                            attributes.instructions.insertBefore(field, reach);
                            attributes.instructions.remove(field);
                            patched++;
                        }
                        if (rat && instruction instanceof LdcInsnNode literal && literal.cst instanceof Double) {
                            literal.cst = .15D;
                            rat = false;
                        }
                    }
                    if (patched != 1) throw new IllegalStateException("Expected one Whisker Reach attribute: " + patched);
                }
                ClassWriter writer = new ClassWriter(0);
                original.accept(writer);
                Path target = Path.of(arguments[2]).resolve(entry);
                Files.createDirectories(target.getParent());
                Files.write(target, writer.toByteArray());
            }
            String entry = "net/farmingtales/ftboss/item/FoodBlessingCombat.class";
            ClassNode combat = read(baseline.getInputStream(baseline.getJarEntry(entry)).readAllBytes());
            MethodNode attributes = combat.methods.stream().filter(value -> value.name.equals("blessingAttributes")).findFirst().orElseThrow();
            boolean magnetic = false;
            int flags = 0;
            for (var instruction : attributes.instructions.toArray()) {
                if (instruction instanceof LdcInsnNode literal && literal.cst.equals("magnetic_plate")) magnetic = true;
                if (!magnetic || instruction.getOpcode() != Opcodes.DMUL) continue;
                var flag = instruction.getNext();
                while (flag != null && flag.getOpcode() < 0) flag = flag.getNext();
                if (flag == null || flag.getOpcode() != Opcodes.ICONST_1 && flag.getOpcode() != Opcodes.ICONST_0)
                    throw new IllegalStateException("Unexpected Magnetic Plate operation shape");
                attributes.instructions.set(flag, new InsnNode(Opcodes.ICONST_0));
                flags++;
                magnetic = false;
            }
            if (flags != 1) throw new IllegalStateException("Expected one Magnetic Plate operation flag, got " + flags);
            ClassWriter writer = new ClassWriter(0);
            combat.accept(writer);
            Path target = Path.of(arguments[2]).resolve(entry);
            Files.write(target, writer.toByteArray());
        }
        System.out.println("PASS surgical armor UI, percentage helper and one Magnetic Plate flag; retained runtime enhancements");
    }
}
