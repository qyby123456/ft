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
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class RetiredFoodClosure {
    private static int checks;

    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static ClassNode read(Path classes, String name) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(classes.resolve("net/farmingtales/ftboss/" + name + ".class"))).accept(node, 0);
        return node;
    }

    private static List<AbstractInsnNode> instructions(MethodNode method) {
        List<AbstractInsnNode> result = new ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions) if (instruction.getOpcode() >= 0) result.add(instruction);
        return result;
    }

    private static void exactReturn(ClassNode node, String name, int... expected) {
        List<MethodNode> methods = node.methods.stream().filter(method -> method.name.equals(name)).toList();
        require(!methods.isEmpty(), node.name + ": missing " + name);
        for (MethodNode method : methods) {
            List<AbstractInsnNode> actual = instructions(method);
            require(actual.size() == expected.length, node.name + ": unexpected body size " + name);
            require(method.tryCatchBlocks.isEmpty(), node.name + ": catch drift " + name);
            for (int index = 0; index < expected.length; index++) require(actual.get(index).getOpcode() == expected[index], node.name + ": opcode drift " + name);
        }
    }

    private static void earlySeven(ClassNode node, String name, String descriptor) {
        MethodNode method = node.methods.stream().filter(candidate -> candidate.name.equals(name) && candidate.desc.equals(descriptor)).findFirst().orElseThrow();
        List<AbstractInsnNode> actual = instructions(method);
        require(actual.size() >= 5, node.name + ": missing early reject");
        int returned = -1;
        boolean seven = false;
        for (int index = 0; index < actual.size(); index++) {
            AbstractInsnNode instruction = actual.get(index);
            if (instruction instanceof IntInsnNode operand && operand.operand == 7) seven = true;
            require(!(instruction instanceof MethodInsnNode), node.name + ": packet/context invoked before retired reject");
            if (instruction.getOpcode() == Opcodes.RETURN) { returned = index; break; }
        }
        require(seven && returned >= 3 && returned <= 5, node.name + ": no exact action-seven early return");
    }

    public static void main(String[] arguments) throws Exception {
        Path classes = Path.of(arguments[0]);
        ClassNode combat = read(classes, "item/FoodBlessingCombat");
        exactReturn(combat, "eligible", Opcodes.ICONST_0, Opcodes.IRETURN);
        exactReturn(combat, "level", Opcodes.ICONST_0, Opcodes.IRETURN);
        exactReturn(combat, "crossbowChargeTicks", Opcodes.ILOAD, Opcodes.IRETURN);
        for (String name : List.of("blessingAttributes", "quoteDamage", "defense", "killFeast")) exactReturn(combat, name, Opcodes.RETURN);
        MethodNode dispatch = combat.methods.stream().filter(method -> method.name.equals("attributes")).findFirst().orElseThrow();
        List<AbstractInsnNode> body = instructions(dispatch);
        require(body.size() == 3 && body.get(1) instanceof MethodInsnNode call && call.owner.equals("net/farmingtales/ftboss/item/EquipmentAttributeSlots")
            && call.name.equals("attributes") && call.desc.equals("(Lnet/neoforged/neoforge/event/ItemAttributeModifierEvent;)V"), "General equipment attribute dispatcher changed");
        require(combat.methods.stream().noneMatch(method -> method.name.equals("heldLevel") || method.name.equals("add")), "Food effect helpers remain");
        ClassNode statue = read(classes, "item/StatueBlessingCombat");
        for (String name : List.of("quote", "defense", "kill", "attributes")) exactReturn(statue, name, Opcodes.RETURN);
        require(statue.methods.size() == 6, "Statue combat effect helpers remain");
        ClassNode catalogue = read(classes, "item/StatueBlessingCatalog");
        require(catalogue.fields.isEmpty() && catalogue.methods.stream().noneMatch(method -> method.name.equals("<clinit>")), "Statue catalogue still registers data");
        exactReturn(catalogue, "byId", Opcodes.ACONST_NULL, Opcodes.ARETURN);
        exactReturn(catalogue, "isAdded", Opcodes.ICONST_0, Opcodes.IRETURN);
        ClassNode armor = read(classes, "item/ArmorBlessingCatalog");
        require(armor.methods.stream().noneMatch(method -> method.name.equals("rule") || method.name.equals("add")), "Armor catalogue registration helpers remain");
        for (MethodNode method : armor.methods) for (AbstractInsnNode instruction : instructions(method)) {
            require(!(instruction instanceof MethodInsnNode call && call.owner.equals(armor.name + "$Definition") && call.name.equals("<init>")), "Armor catalogue still creates definitions");
        }
        for (String name : List.of("client/screen/StatueHubScreen", "client/screen/OrdinaryStatueHubScreen")) {
            Path source = classes.resolve("net/farmingtales/ftboss/" + name + ".class");
            if (!Files.exists(source)) {
                try (var paths = Files.walk(classes)) { source = paths.filter(path -> path.getFileName().toString().equals(name.substring(name.lastIndexOf('/') + 1) + ".class")).findFirst().orElseThrow(); }
            }
            ClassNode screen = new ClassNode(); new ClassReader(Files.readAllBytes(source)).accept(screen, 0);
            require(screen.methods.stream().noneMatch(method -> method.name.equals("blessingFoodTooltip")), "Dead retired screen helper remains");
            for (MethodNode method : screen.methods) for (AbstractInsnNode instruction : instructions(method))
                require(!(instruction instanceof LdcInsnNode constant && constant.cst instanceof String key && key.startsWith("screen.ftboss.statue_hub.enchant.")), "Screen still advertises retired food enchantment");
        }
        ClassNode network = read(classes, "network/StatueMenuNetwork");
        for (ClassNode host : List.of(network, read(classes, "network/StatueMenuNetwork$Action"), read(classes, "network/StatueMenuNetwork$OrdinaryAction"))) {
            for (MethodNode method : host.methods) for (AbstractInsnNode instruction : instructions(method))
                require(!(instruction instanceof MethodInsnNode call && call.name.contains("applyFoodBlessingFromUi")), "Network still dispatches retired food enchantment");
        }
        earlySeven(read(classes, "network/StatueMenuNetwork$Action"), "send", "(Lnet/minecraft/core/BlockPos;II)V");
        earlySeven(read(classes, "network/StatueMenuNetwork$Action"), "handle", "(Lnet/farmingtales/ftboss/network/StatueMenuNetwork$Action;Lnet/neoforged/neoforge/network/handling/IPayloadContext;)V");
        earlySeven(read(classes, "network/StatueMenuNetwork$OrdinaryAction"), "send", "(Lnet/minecraft/core/BlockPos;II)V");
        Files.writeString(Path.of(arguments[1]), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("failures", 0, "checks", checks,
            "noRetiredEffectOrRegistrationImplementations", true, "generalEquipmentAttributeDispatcherRetained", true,
            "retiredActionSevenRejectedBeforeNetworkOrContext", true, "clientScreensDoNotAdvertiseRetiredFeature", true)));
    }
}
