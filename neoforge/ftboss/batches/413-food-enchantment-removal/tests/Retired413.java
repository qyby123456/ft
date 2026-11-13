import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class Retired413 {
    private static ClassNode read(Path root, String name) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(root.resolve("net/farmingtales/ftboss/" + name + ".class"))).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return node;
    }

    private static int check(ClassNode owner, List<String> methods, boolean rejected) {
        int checked = 0;
        for (String name : methods) {
            List<MethodNode> candidates = owner.methods.stream().filter(method -> method.name.equals(name)).toList();
            if (candidates.size() != 1) throw new AssertionError("Missing or ambiguous removed operation " + name);
            List<Integer> instructions = new ArrayList<>();
            for (AbstractInsnNode instruction : candidates.getFirst().instructions) if (instruction.getOpcode() >= 0) instructions.add(instruction.getOpcode());
            List<Integer> expected = rejected ? List.of(Opcodes.ICONST_0, Opcodes.IRETURN) : List.of(Opcodes.RETURN);
            if (!instructions.equals(expected)) throw new AssertionError("Retired operation retains effects: " + name + instructions);
            checked++;
        }
        return checked;
    }

    public static void main(String[] arguments) throws Exception {
        Path root = Path.of(arguments[0]);
        ClassNode weapon = read(root, "item/FoodWeaponEvolution");
        ClassNode armor = read(root, "item/ArmorFoodBlessings");
        int checked = check(weapon, List.of("applyFoodBlessing", "applyFoodBlessingFromUi", "isBlessingCandidate", "ftboss$legacyOrdinary$applyFoodBlessing", "ftboss$legacyOrdinary$applyFoodBlessingFromUi", "ftboss$legacyOrdinary$isBlessingCandidate", "hasWashableBlessing"), true);
        checked += check(weapon, List.of("applyFoodBlessingHits", "foodBlessingDefense", "foodBlessingKillFeast", "grindstoneBlessing", "addFoodBlessingAttributes", "ftboss$legacyOrdinary$addFoodBlessingAttributes", "addFoodBlessingTooltip"), false);
        checked += check(armor, List.of("eligible", "hasRoom", "apply"), true);
        checked += check(armor, List.of("attributes", "defend", "poison", "tickStart", "tickEnd", "damaged"), false);
        if (armor.visibleAnnotations != null && armor.visibleAnnotations.stream().anyMatch(annotation -> annotation.desc.contains("EventBusSubscriber"))) throw new AssertionError("Retired armor module remains subscribed");
        List<Path> sections;
        try (var paths = Files.walk(root)) { sections = paths.filter(path -> path.getFileName().toString().matches("(?:Ordinary)?StatueHubScreen\\$Section.class")).toList(); }
        if (sections.size() != 2) throw new AssertionError("Missing statue section enums");
        for (Path section : sections) {
            ClassNode node = new ClassNode();
            new ClassReader(Files.readAllBytes(section)).accept(node, ClassReader.SKIP_DEBUG);
            if (node.fields.stream().anyMatch(field -> field.name.equals("ENCHANT"))) throw new AssertionError("Enchantment section remains");
        }
        Files.writeString(Path.of(arguments[1]), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("failures", 0, "checkedMethods", checked, "statueSectionEnumsWithoutEnchant", sections.size(), "armorSubscriberRemoved", true, "actualRuntimeAccepted", false)));
    }
}
