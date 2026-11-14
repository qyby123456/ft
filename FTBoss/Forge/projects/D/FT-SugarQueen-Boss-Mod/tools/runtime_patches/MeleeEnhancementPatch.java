import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public final class MeleeEnhancementPatch implements Opcodes {
    private static final String ITEM = "net/farmingtales/ftboss/item/";
    private static final String RULES = "net/farmingtales/ftboss/util/MeleeEnhancementRules";
    private static final String BUILDER = ITEM + "FoodWeaponHitPlan$Builder";
    private static final List<String> CHANGES = new ArrayList<>();

    private static MethodNode method(ClassNode owner, String name) {
        List<MethodNode> matches = owner.methods.stream().filter(value -> value.name.equals(name)).toList();
        if (matches.size() != 1) throw new IllegalStateException(owner.name + "." + name + " count=" + matches.size());
        return matches.get(0);
    }

    private static void preserveFlagFrames(MethodNode method, int slot) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (!(instruction instanceof FrameNode frame)) continue;
            int width = 0;
            for (Object local : frame.local) width += local == LONG || local == DOUBLE ? 2 : 1;
            while (width++ < slot) frame.local.add(TOP);
            frame.local.add(INTEGER);
        }
    }

    private static int builderFlag(MethodNode method) {
        int flag = method.maxLocals++;
        InsnList start = new InsnList();
        start.add(new VarInsnNode(ALOAD, 0));
        start.add(new FieldInsnNode(GETFIELD, BUILDER, "ftboss$meleeEnhancement", "Z"));
        start.add(new VarInsnNode(ISTORE, flag));
        method.instructions.insert(start);
        preserveFlagFrames(method, flag);
        return flag;
    }

    private static void markBuilder(MethodNode method, int flag) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode call) || !call.owner.equals(BUILDER) || !call.name.equals("<init>")) continue;
            AbstractInsnNode following = instruction.getNext();
            while (following != null && following.getOpcode() < 0) following = following.getNext();
            if (!(following instanceof VarInsnNode store) || store.getOpcode() != ASTORE) throw new IllegalStateException("Builder constructor store changed");
            InsnList mark = new InsnList();
            mark.add(new VarInsnNode(ALOAD, store.var));
            mark.add(new VarInsnNode(ILOAD, flag));
            mark.add(new FieldInsnNode(PUTFIELD, BUILDER, "ftboss$meleeEnhancement", "Z"));
            method.instructions.insert(store, mark);
            return;
        }
        throw new IllegalStateException("Hit builder constructor missing");
    }

    private static int eventFlag(MethodNode method) {
        int flag = method.maxLocals++;
        InsnList start = new InsnList();
        start.add(new VarInsnNode(ALOAD, 0));
        start.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/event/entity/living/LivingHurtEvent", "getSource",
                "()Lnet/minecraft/world/damagesource/DamageSource;", false));
        start.add(new FieldInsnNode(GETSTATIC, "net/minecraft/world/damagesource/DamageTypes", "f_268464_", "Lnet/minecraft/resources/ResourceKey;"));
        start.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/damagesource/DamageSource", "m_276093_",
                "(Lnet/minecraft/resources/ResourceKey;)Z", false));
        start.add(new VarInsnNode(ISTORE, flag));
        method.instructions.insert(start);
        preserveFlagFrames(method, flag);
        return flag;
    }

    private static void transform(MethodNode method, MethodInsnNode call, int argument, String rule, int flag) {
        Type[] arguments = Type.getArgumentTypes(call.desc);
        int[] slots = new int[arguments.length];
        InsnList code = new InsnList();
        for (int index = arguments.length - 1; index >= 0; index--) {
            slots[index] = method.maxLocals;
            method.maxLocals += arguments[index].getSize();
            code.add(new VarInsnNode(arguments[index].getOpcode(ISTORE), slots[index]));
        }
        for (int index = 0; index < arguments.length; index++) {
            code.add(new VarInsnNode(arguments[index].getOpcode(ILOAD), slots[index]));
            if (index == argument) {
                code.add(new VarInsnNode(ILOAD, flag));
                String type = arguments[index].getDescriptor();
                code.add(new MethodInsnNode(INVOKESTATIC, RULES, rule, "(" + type + "Z)" + type, false));
            }
        }
        method.instructions.insertBefore(call, code);
        CHANGES.add(method.name + ":" + call.owner + "." + call.name + ":" + rule);
    }

    private static void bonuses(MethodNode method, int flag, boolean sockets) {
        boolean active = !sockets;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode call)) continue;
            if (sockets && call.owner.equals(ITEM + "FoodWeaponEvolution") && call.name.equals("quoteFoodBlessingHits")) {
                active = true;
                continue;
            }
            if (sockets && call.owner.equals(ITEM + "FoodWeaponEvolution") && call.name.equals("isGourmetEvolutionWeapon")) active = false;
            if (!active) continue;
            if (call.owner.equals(BUILDER)) {
                switch (call.name) {
                    case "add", "multiply" -> transform(method, call, 0, call.name.equals("multiply") ? "multiplier" : "amount", flag);
                    case "heal" -> transform(method, call, 2, "amount", flag);
                    case "roll" -> transform(method, call, 1, "chance", flag);
                    default -> { }
                }
            } else if (call.owner.equals(ITEM + "FoodWeaponEvolution") && (call.name.equals("quoteChainArc") || call.name.equals("quoteOverloadBurst"))) {
                transform(method, call, 3, "amount", flag);
            } else if (call.owner.equals(ITEM + "StatueBlessingCombat") && call.name.equals("heal")) transform(method, call, 2, "amount", flag);
        }
    }

    private static void attributes(ClassNode owner, MethodNode method, int weapon, boolean event, String target) {
        int flag = method.maxLocals++;
        InsnList start = new InsnList();
        start.add(new VarInsnNode(ALOAD, weapon));
        if (event) start.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraftforge/event/ItemAttributeModifierEvent", "getItemStack", "()Lnet/minecraft/world/item/ItemStack;", false));
        start.add(new MethodInsnNode(INVOKESTATIC, owner.name, "ftboss$meleeEnhancementWeapon", "(Lnet/minecraft/world/item/ItemStack;)Z", false));
        start.add(new VarInsnNode(ISTORE, flag));
        method.instructions.insert(start);
        preserveFlagFrames(method, flag);
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode call) || !call.name.equals(target)) continue;
            if (target.equals("effectPower") && call.owner.equals(ITEM + "FoodWeaponEvolution$SocketStats")) {
                InsnList code = new InsnList();
                code.add(new VarInsnNode(ILOAD, flag));
                code.add(new MethodInsnNode(INVOKESTATIC, RULES, "amount", "(DZ)D", false));
                method.instructions.insert(call, code);
                CHANGES.add(method.name + ":socket_attribute:amount");
            } else if (target.equals("add") && call.owner.equals(owner.name)) transform(method, call, 3, "amount", flag);
        }
    }

    private static void weaponPredicate(ClassNode owner) {
        MethodNode predicate = new MethodNode(ACC_PRIVATE | ACC_STATIC, "ftboss$meleeEnhancementWeapon", "(Lnet/minecraft/world/item/ItemStack;)Z", null, null);
        LabelNode no = new LabelNode();
        predicate.instructions.add(new VarInsnNode(ALOAD, 0));
        predicate.instructions.add(new JumpInsnNode(IFNULL, no));
        predicate.instructions.add(new VarInsnNode(ALOAD, 0));
        predicate.instructions.add(new MethodInsnNode(INVOKESTATIC, ITEM + "FoodWeaponFamilyBuild", "kind", "(Lnet/minecraft/world/item/ItemStack;)Lnet/farmingtales/ftboss/util/FoodWeaponFamilyRules$Kind;", false));
        predicate.instructions.add(new FieldInsnNode(GETSTATIC, "net/farmingtales/ftboss/util/FoodWeaponFamilyRules$Kind", "MELEE", "Lnet/farmingtales/ftboss/util/FoodWeaponFamilyRules$Kind;"));
        predicate.instructions.add(new JumpInsnNode(IF_ACMPNE, no));
        predicate.instructions.add(new InsnNode(ICONST_1));
        predicate.instructions.add(new InsnNode(IRETURN));
        predicate.instructions.add(no);
        predicate.instructions.add(new FrameNode(F_SAME, 0, null, 0, null));
        predicate.instructions.add(new InsnNode(ICONST_0));
        predicate.instructions.add(new InsnNode(IRETURN));
        predicate.maxLocals = 1;
        owner.methods.add(predicate);
    }

    public static void main(String[] arguments) throws Exception {
        Path payload = Path.of(arguments[1]);
        try (ZipFile baseline = new ZipFile(arguments[0]); ZipFile original = arguments.length > 2 ? new ZipFile(arguments[2]) : new ZipFile(arguments[0])) {
            for (String name : List.of("FoodWeaponEvolution", "StatueBlessingCombat", "FoodBlessingCombat", "FoodWeaponHitPlan$Builder")) {
                String entry = ITEM + name + ".class";
                ClassNode owner = new ClassNode();
                ZipFile input = name.equals("FoodWeaponHitPlan$Builder") ? baseline : original;
                new ClassReader(input.getInputStream(input.getEntry(entry))).accept(owner, ClassReader.EXPAND_FRAMES);
                if (name.equals("FoodWeaponEvolution")) {
                    MethodNode blessing = method(owner, "quoteFoodBlessingHits");
                    int flag = builderFlag(blessing);
                    bonuses(blessing, flag, false);
                    MethodNode hit = method(owner, "quoteFinalWeaponHit");
                    int hitFlag = eventFlag(hit);
                    markBuilder(hit, hitFlag);
                    bonuses(hit, hitFlag, true);
                    attributes(owner, method(owner, "growthAttributes"), 0, true, "effectPower");
                } else if (name.equals("StatueBlessingCombat")) {
                    MethodNode quote = method(owner, "quote");
                    bonuses(quote, builderFlag(quote), false);
                    attributes(owner, method(owner, "attributes"), 1, false, "add");
                } else if (name.equals("FoodBlessingCombat")) attributes(owner, method(owner, "blessingAttributes"), 1, false, "add");
                else {
                    if (owner.fields.stream().anyMatch(field -> field.name.equals("ftboss$meleeEnhancement"))) throw new IllegalStateException("Melee field already installed");
                    owner.fields.add(new FieldNode(ACC_PUBLIC, "ftboss$meleeEnhancement", "Z", null, null));
                }
                if (!name.equals("FoodWeaponHitPlan$Builder")) weaponPredicate(owner);
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                owner.accept(writer);
                Path output = payload.resolve(entry);
                Files.createDirectories(output.getParent());
                Files.write(output, writer.toByteArray());
            }
        }
        Files.write(payload.getParent().resolve("transformations.txt"), CHANGES);
        System.out.println("PASS: melee bonus transforms=" + CHANGES.size());
    }
}
