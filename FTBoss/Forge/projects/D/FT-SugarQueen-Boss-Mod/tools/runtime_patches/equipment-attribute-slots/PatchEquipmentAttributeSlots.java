import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class PatchEquipmentAttributeSlots implements Opcodes {
    private static final String PREFIX = "net/farmingtales/ftboss/item/";
    private static final String HELPER = PREFIX + "EquipmentAttributeSlots";
    private static final String EVENT = "net/minecraftforge/event/ItemAttributeModifierEvent";
    private static final String STACK = "net/minecraft/world/item/ItemStack";
    private static final String SLOT = "net/minecraft/world/entity/EquipmentSlot";

    private static MethodNode method(ClassNode node, String name) {
        return node.methods.stream().filter(entry -> entry.name.equals(name)).findFirst().orElseThrow();
    }

    private static InsnList stack() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new MethodInsnNode(INVOKEVIRTUAL, EVENT, "getItemStack", "()L" + STACK + ";", false));
        return instructions;
    }

    private static void slotGate(MethodNode method, int stackIndex) {
        int gates = 0;
        for (var instruction : method.instructions.toArray()) {
            if (!(instruction instanceof FieldInsnNode field) || !field.owner.equals(SLOT) || !field.name.equals("MAINHAND")) continue;
            AbstractInsnNode next = instruction.getNext();
            while (next != null && next.getOpcode() < 0) next = next.getNext();
            if (!(next instanceof JumpInsnNode branch) || branch.getOpcode() != IF_ACMPEQ && branch.getOpcode() != IF_ACMPNE) continue;
            InsnList replacement = stackIndex < 0 ? stack() : new InsnList();
            if (stackIndex >= 0) replacement.add(new VarInsnNode(ALOAD, stackIndex));
            replacement.add(new MethodInsnNode(INVOKESTATIC, HELPER, "growthSlot", "(L" + SLOT + ";L" + STACK + ";)Z", false));
            method.instructions.insertBefore(instruction, replacement);
            method.instructions.remove(instruction);
            branch.setOpcode(branch.getOpcode() == IF_ACMPEQ ? IFNE : IFEQ);
            gates++;
        }
        if (gates != 1) throw new IllegalStateException("Expected one slot gate in " + method.name + ": " + gates);
    }

    private static void heldGuard(MethodNode method, boolean directStack, boolean returnsBoolean) {
        InsnList guard = directStack ? new InsnList() : stack();
        if (directStack) guard.add(new VarInsnNode(ALOAD, 0));
        guard.add(new MethodInsnNode(INVOKESTATIC, HELPER, "heldItem", "(L" + STACK + ";)Z", false));
        LabelNode accepted = new LabelNode();
        guard.add(new JumpInsnNode(IFNE, accepted));
        if (returnsBoolean) guard.add(new InsnNode(ICONST_0));
        guard.add(new InsnNode(returnsBoolean ? IRETURN : RETURN));
        guard.add(accepted);
        guard.add(new FrameNode(F_SAME, 0, null, 0, null));
        method.instructions.insert(guard);
    }

    private static void blessingGuard(MethodNode method) {
        InsnList guard = new InsnList();
        guard.add(new VarInsnNode(ALOAD, 0));
        guard.add(new VarInsnNode(ALOAD, 1));
        guard.add(new MethodInsnNode(INVOKESTATIC, HELPER, "blessingSlot", "(L" + EVENT + ";L" + STACK + ";)Z", false));
        LabelNode accepted = new LabelNode();
        guard.add(new JumpInsnNode(IFNE, accepted));
        guard.add(new InsnNode(RETURN));
        guard.add(accepted);
        guard.add(new FrameNode(F_SAME, 0, null, 0, null));
        method.instructions.insert(guard);
    }

    private static void modifierIds(MethodNode method, boolean fields) {
        int replacements = 0;
        for (var instruction : method.instructions.toArray()) {
            boolean selected = fields
                    ? instruction instanceof FieldInsnNode field && field.getOpcode() == GETSTATIC && field.desc.equals("Ljava/util/UUID;")
                    : instruction instanceof MethodInsnNode call && call.owner.equals("java/util/UUID") && call.name.equals("nameUUIDFromBytes");
            if (!selected) continue;
            InsnList namespace = stack();
            namespace.add(new MethodInsnNode(INVOKESTATIC, HELPER, "modifierId", "(Ljava/util/UUID;L" + STACK + ";)Ljava/util/UUID;", false));
            method.instructions.insert(instruction, namespace);
            replacements++;
        }
        if (replacements != (fields ? 7 : 1)) throw new IllegalStateException("Unexpected modifier count in " + method.name + ": " + replacements);
    }

    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[2]);
        try (JarFile baseline = new JarFile(arguments[0]); JarFile helper = new JarFile(arguments[1])) {
            for (String name : List.of("FoodWeaponEvolution", "FoodBlessingCombat", "StatueBlessingCombat", "RangedHybridCombat",
                    "FoodWeaponFamilyBuild", "WeaponMaterialBuild", "WeaponMaterialBonuses")) {
                ClassNode node = read(baseline, name);
                if (name.equals("FoodWeaponEvolution")) {
                    slotGate(method(node, "growthAttributes"), -1);
                    modifierIds(method(node, "growthAttributes"), true);
                } else if (name.equals("FoodBlessingCombat")) {
                    MethodNode dispatch = method(node, "attributes");
                    dispatch.instructions.clear();
                    dispatch.tryCatchBlocks.clear();
                    if (dispatch.localVariables != null) dispatch.localVariables.clear();
                    dispatch.instructions.add(new VarInsnNode(ALOAD, 0));
                    dispatch.instructions.add(new MethodInsnNode(INVOKESTATIC, HELPER, "attributes", "(L" + EVENT + ";)V", false));
                    dispatch.instructions.add(new InsnNode(RETURN));
                    slotGate(method(node, "blessingAttributes"), 1);
                    blessingGuard(method(node, "blessingAttributes"));
                    modifierIds(method(node, "add"), false);
                } else if (name.equals("StatueBlessingCombat")) {
                    slotGate(method(node, "attributes"), 1);
                    modifierIds(method(node, "add"), false);
                } else if (name.equals("RangedHybridCombat")) {
                    heldGuard(method(node, "redirects"), true, true);
                } else if (name.equals("FoodWeaponFamilyBuild")) {
                    heldGuard(method(node, "staffAttributes"), false, false);
                } else if (name.equals("WeaponMaterialBuild")) {
                    heldGuard(method(node, "attributes"), false, false);
                } else {
                    int guards = 0;
                    for (var instruction : method(node, "attributes").instructions.toArray()) {
                        if (!(instruction instanceof MethodInsnNode call) || !call.owner.equals(PREFIX + "EquipmentWeaponBuild")
                                || !call.name.equals("applies")) continue;
                        call.owner = HELPER;
                        call.name = "heldItem";
                        var branch = (JumpInsnNode) call.getNext();
                        if (branch.getOpcode() != IFEQ) throw new IllegalStateException("Unexpected armor routing branch");
                        branch.setOpcode(IFNE);
                        guards++;
                    }
                    if (guards != 1) throw new IllegalStateException("Expected one armor material guard");
                }
                write(output, node);
            }
            write(output, read(helper, "EquipmentAttributeSlots"));
        }
        System.out.println("PASS surgical equipment slot patch: seven existing classes and one helper; native item/NBT/boss logic retained");
    }

    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(PREFIX + name + ".class")).readAllBytes()).accept(node, 0);
        return node;
    }

    private static void write(Path root, ClassNode node) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Path target = root.resolve(node.name + ".class");
        Files.createDirectories(target.getParent());
        Files.write(target, writer.toByteArray());
    }
}
