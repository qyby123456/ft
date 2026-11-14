import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class PatchUpgradeReturns {
    private static final String BASE = "net/farmingtales/ftboss/";
    private static final String RULES = BASE + "util/SwordCavityRules";

    private static ClassNode read(JarFile archive, String name) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(archive.getInputStream(archive.getJarEntry(name + ".class")).readAllBytes()).accept(node, 0);
        return node;
    }

    private static MethodNode method(ClassNode owner, String name) {
        return owner.methods.stream().filter(value -> value.name.equals(name)).findFirst().orElseThrow();
    }

    private static void replace(ClassNode owner, ClassNode compiled, String name) {
        owner.methods.remove(method(owner, name));
        owner.methods.add(method(compiled, name));
    }

    private static void write(Path output, ClassNode node) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Path destination = output.resolve(node.name + ".class");
        Files.createDirectories(destination.getParent());
        Files.write(destination, writer.toByteArray());
    }

    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[2]);
        try (JarFile baseline = new JarFile(arguments[0]); JarFile compiled = new JarFile(arguments[1])) {
            ClassNode rules = read(baseline, RULES);
            ClassNode newRules = read(compiled, RULES);
            replace(rules, newRules, "applyMaterials");
            rules.methods.add(method(newRules, "materialBonus"));
            rules.methods.add(method(newRules, "wearBonus"));
            write(output, rules);
            for (String name : new String[]{RULES + "$Link", BASE + "util/WeaponMaterialBonusRules", BASE + "util/WeaponMaterialBonusRules$Bonus"}) {
                ClassNode original = read(baseline, name);
                ClassNode updated = read(compiled, name);
                for (String target : name.endsWith("$Bonus") ? new String[]{"plus"}
                        : name.endsWith("$Link") ? new String[]{"<clinit>"} : new String[]{"material", "<clinit>"}) {
                    replace(original, updated, target);
                }
                write(output, original);
            }
            ClassNode foundation = read(baseline, BASE + "util/foundations/FourWeaponFoundationRules");
            MethodNode combat = method(foundation, "combat");
            int adjusted = 0;
            for (AbstractInsnNode instruction : combat.instructions.toArray()) {
                if (!(instruction instanceof FieldInsnNode field) || !field.owner.equals(BASE + "util/FoodWeaponBuildRules$Material")) continue;
                if (!Map.of("durability", true, "wear", true, "damage", true, "speed", true, "reach", true).containsKey(field.name)) continue;
                AbstractInsnNode delta = instruction;
                if (!field.name.equals("reach")) {
                    do { delta = delta.getNext(); } while (delta != null && delta.getOpcode() != Opcodes.DSUB);
                    if (delta == null) throw new IllegalStateException("Material delta not found");
                }
                combat.instructions.insert(delta, new MethodInsnNode(Opcodes.INVOKESTATIC, RULES,
                        field.name.equals("wear") ? "wearBonus" : "materialBonus", "(D)D", false));
                adjusted++;
            }
            if (adjusted != 5) throw new IllegalStateException("Expected five foundation material contributions, got " + adjusted);
            write(output, foundation);
            ClassNode tiers = read(baseline, BASE + "item/FoodWeaponEvolution$SocketTier");
            MethodNode initializer = method(tiers, "<clinit>");
            int tierChanges = 0;
            for (AbstractInsnNode instruction : initializer.instructions.toArray()) {
                if (instruction.getOpcode() == Opcodes.DCONST_1) {
                    initializer.instructions.set(instruction, new LdcInsnNode(1.5));
                    tierChanges++;
                } else if (instruction instanceof LdcInsnNode constant && constant.cst instanceof Double value) {
                    if (value != 1.25 && value != 1.5) throw new IllegalStateException("Unexpected socket multiplier " + value);
                    constant.cst = value * 1.5;
                    tierChanges++;
                }
            }
            if (tierChanges != 3) throw new IllegalStateException("Expected three socket multipliers");
            write(output, tiers);
            System.out.println("PASS surgical upgrade patch: six classes; existing members and unrelated methods preserved");
        }
    }
}
