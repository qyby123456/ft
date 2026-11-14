import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class PatchBlessings implements Opcodes {
    private static final String BASE = "net/farmingtales/ftboss/item/";
    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode result = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(BASE + name + ".class")).readAllBytes()).accept(result, 0);
        return result;
    }
    private static MethodNode method(ClassNode node, String name) {
        return node.methods.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
    }
    private static int integer(AbstractInsnNode node) {
        if (node instanceof IntInsnNode value) return value.operand;
        return node.getOpcode() - ICONST_0;
    }
    private static Map<String, Integer> switches(JarFile jar) throws Exception {
        Map<String, Integer> result = new HashMap<>();
        for (AbstractInsnNode instruction : method(read(jar, "FoodWeaponEvolution$1"), "<clinit>").instructions) {
            if (instruction instanceof FieldInsnNode field && field.owner.equals(BASE + "FoodWeaponEvolution$FoodBlessingKind") && field.getOpcode() == GETSTATIC) {
                result.put(field.name, integer(instruction.getNext().getNext()));
            }
        }
        return result;
    }
    private static String trace(MethodNode method) {
        Textifier text = new Textifier();
        method.accept(new TraceMethodVisitor(text));
        StringWriter result = new StringWriter();
        text.print(new PrintWriter(result));
        return result.toString();
    }
    private static void dispatch(ClassNode node, Map<String, Integer> values) {
        MethodNode method = method(node, "quoteFoodBlessingHits");
        List<TableSwitchInsnNode> tables = new ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions) if (instruction instanceof TableSwitchInsnNode table) tables.add(table);
        if (tables.size() != 2) throw new AssertionError("Unexpected blessing dispatch count");
        TableSwitchInsnNode random = tables.get(0);
        Set<String> chance = Set.of("CRIT", "ABSORPTION", "CHAIN", "OVERLOAD", "BURN", "MARK", "POISON", "WITHER", "ROOT", "REGEN", "CORK_RUSH", "PRISMATIC", "BRINE");
        int[] randomKeys = chance.stream().mapToInt(values::get).sorted().toArray();
        LabelNode[] randomLabels = new LabelNode[randomKeys.length];
        Arrays.fill(randomLabels, random.labels.get(0));
        method.instructions.set(random, new LookupSwitchInsnNode(random.dflt, randomKeys, randomLabels));
        TableSwitchInsnNode effects = tables.get(1);
        Map<String, String> destinations = Map.ofEntries(
                Map.entry("CRIT", "CRIT"), Map.entry("EXECUTE", "FRENZY"), Map.entry("ABSORPTION", "EXECUTE"),
                Map.entry("CHAIN", "ABSORPTION"), Map.entry("OVERLOAD", "CHAIN"), Map.entry("BURN", "OVERLOAD"),
                Map.entry("MARK", "BURN"), Map.entry("LIFESTEAL", "PRISMATIC"), Map.entry("POISON", "MARK"),
                Map.entry("WITHER", "LIFESTEAL"), Map.entry("ROOT", "POISON"), Map.entry("REGEN", "WITHER"),
                Map.entry("CORK_RUSH", "ROOT"), Map.entry("FRENZY", "REPAIR"), Map.entry("PRISMATIC", "REGEN"),
                Map.entry("REPAIR", "EMERGENCY_HEART"), Map.entry("EMERGENCY_HEART", "UNDERTOW"),
                Map.entry("UNDERTOW", "BRINE"), Map.entry("BRINE", "CORK_RUSH"),
                Map.entry("ISOLATION", "ISOLATION"), Map.entry("HUNT", "HUNT"));
        List<LabelNode> original = List.copyOf(effects.labels);
        destinations.forEach((kind, destination) -> effects.labels.set(values.get(kind) - effects.min, original.get(values.get(destination) - effects.min)));
        System.out.println("PASS 21 effect routes and 13 matching probability domains");
    }
    private static void earlyRegistration(MethodNode method, boolean use) {
        InsnList prefix = new InsnList();
        LabelNode prior = new LabelNode();
        prefix.add(new VarInsnNode(ALOAD, 0));
        prefix.add(new VarInsnNode(ALOAD, 1));
        prefix.add(new MethodInsnNode(INVOKESTATIC, BASE + "NativeMaterialCrossbowSupport", "registered", "(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)Z", false));
        prefix.add(new JumpInsnNode(IFEQ, prior));
        if (use) {
            for (int slot = 0; slot < 5; slot++) prefix.add(new VarInsnNode(ALOAD, slot));
            prefix.add(new MethodInsnNode(INVOKESTATIC, BASE + "NativeMaterialCrossbowSupport", "use", method.desc, false));
        } else {
            prefix.add(new FieldInsnNode(GETSTATIC, BASE + "FoodWeaponSourceLedger$NativeKind", "CROSSBOW", "L" + BASE + "FoodWeaponSourceLedger$NativeKind;"));
        }
        prefix.add(new InsnNode(ARETURN));
        prefix.add(prior);
        prefix.add(new FrameNode(F_SAME, 0, null, 0, null));
        method.instructions.insert(prefix);
    }
    public static void main(String[] args) throws Exception {
        try (JarFile baseline = new JarFile(args[0]); JarFile donors = new JarFile(args[1])) {
            Map<String, Integer> values = switches(baseline);
            for (String name : List.of("FoodWeaponEvolution", "FoodWeaponSourceLedger", "FoodWeaponUseBridge", "WeaponMaterialBuild")) {
                ClassNode node = read(baseline, name);
                ClassNode original = read(baseline, name);
                Set<String> changed = new HashSet<>();
                if (name.equals("FoodWeaponEvolution")) {
                    ClassNode donor = read(donors, name);
                    for (String wrapper : List.of("attributes", "addFoodBlessingAttributes", "foodBlessingDefense", "foodBlessingKillFeast")) {
                        MethodNode target = method(node, wrapper);
                        MethodNode replacement = method(donor, wrapper);
                        replacement.visibleAnnotations = target.visibleAnnotations;
                        replacement.invisibleAnnotations = target.invisibleAnnotations;
                        if (wrapper.equals("foodBlessingDefense") && replacement.visibleAnnotations == null) replacement.visibleAnnotations = new ArrayList<>(List.of(new AnnotationNode("Lnet/minecraftforge/eventbus/api/SubscribeEvent;")));
                        node.methods.set(node.methods.indexOf(target), replacement);
                        changed.add(wrapper);
                    }
                    dispatch(node, values);
                    changed.add("quoteFoodBlessingHits");
                    MethodNode quote = method(node, "quoteFinalWeaponHit");
                    for (AbstractInsnNode instruction : quote.instructions.toArray()) {
                        if (instruction instanceof MethodInsnNode call && call.name.equals("quoteFoodBlessingHits")) {
                            InsnList addition = new InsnList();
                            addition.add(new VarInsnNode(ALOAD, 4));
                            addition.add(new VarInsnNode(ALOAD, 0));
                            addition.add(new VarInsnNode(ALOAD, 6));
                            addition.add(new MethodInsnNode(INVOKESTATIC, BASE + "FoodBlessingCombat", "quoteDamage", "(L" + BASE + "FoodWeaponHitPlan$Builder;Lnet/minecraftforge/event/entity/living/LivingHurtEvent;Lnet/minecraft/world/item/ItemStack;)V", false));
                            quote.instructions.insertBefore(instruction.getPrevious().getPrevious().getPrevious().getPrevious().getPrevious().getPrevious().getPrevious(), addition);
                            changed.add(quote.name);
                        }
                    }
                } else if (name.equals("WeaponMaterialBuild")) {
                    MethodNode quote = method(node, "quoteFinalHit");
                    int repaired = 0;
                    for (AbstractInsnNode instruction : quote.instructions) {
                        if (instruction instanceof MethodInsnNode call && call.owner.equals(BASE + "WeaponMaterialPowers")
                                && (call.name.equals("quoteHit") || call.name.equals("quoteFinalHitEffects"))) {
                            call.owner = BASE + "MaterialHitPlanCompatibility";
                            repaired++;
                        }
                    }
                    if (repaired != 2) throw new AssertionError("Expected two missing material links");
                    changed.add(quote.name);
                } else {
                    String methodName = name.equals("FoodWeaponSourceLedger") ? "nativeKind" : "use";
                    earlyRegistration(method(node, methodName), methodName.equals("use"));
                    changed.add(methodName);
                }
                for (MethodNode prior : original.methods) {
                    MethodNode current = node.methods.stream().filter(candidate -> candidate.name.equals(prior.name) && candidate.desc.equals(prior.desc)).findFirst().orElseThrow();
                    if (!changed.contains(prior.name) && !trace(prior).equals(trace(current))) throw new AssertionError("Sibling changed: " + name + "#" + prior.name);
                }
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                node.accept(writer);
                Path output = Path.of(args[2]).resolve(BASE + name + ".class");
                Files.createDirectories(output.getParent());
                Files.write(output, writer.toByteArray());
                System.out.println("PASS surgical patch and sibling preservation: " + name);
            }
        }
    }
}
