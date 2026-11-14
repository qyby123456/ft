import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class PatchManorWeapons {
    public static final String PREFIX = "net/farmingtales/ftboss/";
    public static final String ENHANCEMENTS = PREFIX + "item/ManorWeaponEnhancements";
    public static final String RULES = PREFIX + "util/ManorWeaponBalanceRules";
    public static final String ORIGINAL_RULES = PREFIX + "util/ManorWeaponRules";
    public static final String HOOKS = PREFIX + "item/ManorTridentDamageHooks";
    public static final String NESTED = "META-INF/jarjar/tridents_n_stuff-0.2.0-forge-bleed-nerf.jar";
    public static final Map<String, Set<String>> METHODS = new LinkedHashMap<>();

    static {
        METHODS.put("item/ManorWeaponEnhancements", Set.of("attributes", "tridentDamage"));
        METHODS.put("item/weapons/DandanScytheItem", Set.of("sweepDamage", "blinkDamage", "markBonus"));
        METHODS.put("item/weapons/LemonCatWeaponItem", Set.of("skilletDamage"));
        METHODS.put("item/weapons/ShadowSwordItem", Set.of("skyDamage"));
        METHODS.put("item/weapons/XueliWeaponRules", Set.of("sweepDamage", "comboDamage", "rollDamage"));
        METHODS.put("util/PufferBatRules", Set.of("tickDamage", "shockwaveDamage", "totalDamage"));
        METHODS.put("skill/QingyiConfluenceSkill", Set.of("burst"));
        METHODS.put("util/DoraWeaponRules", Set.of());
        METHODS.put("util/DoraWeaponRules$Mode", Set.of("damage"));
        METHODS.put("util/DoraSmgRules", Set.of("damage"));
        METHODS.put("util/XuanlanRules", Set.of("weaponDamage"));
    }

    public static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    public static byte[] entry(ZipFile archive, String name) throws Exception {
        check(archive.getEntry(name) != null, "Missing entry: " + name);
        return archive.getInputStream(archive.getEntry(name)).readAllBytes();
    }

    public static String hash(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static InsnList identifier() {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraftforge/event/ItemAttributeModifierEvent",
                "getItemStack", "()Lnet/minecraft/world/item/ItemStack;", false));
        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENHANCEMENTS, "id",
                "(Lnet/minecraft/world/item/ItemStack;)Ljava/lang/String;", false));
        return instructions;
    }

    private static void attributes(MethodNode method) {
        int targets = 0;
        int bonuses = 0;
        int empty = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(ENHANCEMENTS) && call.name.equals("isWeapon")) {
                InsnList test = identifier();
                test.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RULES, "attributeTarget", "(ZLjava/lang/String;)Z", false));
                method.instructions.insert(call, test);
                targets++;
            } else if (instruction instanceof MethodInsnNode call && call.owner.equals(ORIGINAL_RULES) && call.name.equals("attackBonus")) {
                method.instructions.insertBefore(call, identifier());
                call.owner = RULES;
                call.desc = "(DLjava/lang/String;)D";
                bonuses++;
            } else if (instruction.getOpcode() == Opcodes.DCONST_1) {
                InsnList bonus = identifier();
                bonus.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RULES, "emptyHandBonus", "(Ljava/lang/String;)D", false));
                method.instructions.insertBefore(instruction, bonus);
                method.instructions.remove(instruction);
                empty++;
            }
        }
        check(targets == 1 && bonuses == 1 && empty == 1, "Unexpected attribute implementation");
    }

    private static void literal(MethodNode method, Object before, Object after) {
        int changed = 0;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof LdcInsnNode constant && before.equals(constant.cst)) {
                constant.cst = after;
                changed++;
            }
        }
        check(changed == 1, "Expected one literal in " + method.name + ": " + before + ", found " + changed);
    }

    private static void field(ClassNode node, String name, float before, float after) {
        var target = node.fields.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
        check(Float.valueOf(before).equals(target.value), "Unexpected constant: " + name);
        target.value = after;
    }

    private static void doubleReturn(MethodNode method) {
        int returns = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction.getOpcode() == Opcodes.FRETURN) {
                InsnList scale = new InsnList();
                scale.add(new InsnNode(Opcodes.FCONST_2));
                scale.add(new InsnNode(Opcodes.FMUL));
                method.instructions.insertBefore(instruction, scale);
                returns++;
            }
        }
        check(returns > 0, "No damage result in " + method.name);
    }

    private static void redirect(MethodNode method) {
        int redirected = 0;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(ORIGINAL_RULES)
                    && (call.name.equals("skillDamage") || call.name.equals("skillBonusDamage"))) {
                call.owner = RULES;
                redirected++;
            }
        }
        check(redirected == 1, "Unexpected skill formula in " + method.name);
    }

    private static byte[] patch(byte[] bytes, String path, byte[] donor) throws Exception {
        ClassNode node = DomainVisualMethodMerge.read(bytes);
        if (path.equals("item/ManorWeaponEnhancements")) {
            check(node.methods.stream().noneMatch(method -> method.name.equals("staffSpellDamage")), "Weapon rebalance already installed");
            attributes(DomainVisualMethodMerge.method(node, "attributes"));
            int index = node.methods.indexOf(DomainVisualMethodMerge.method(node, "tridentDamage"));
            node.methods.set(index, DomainVisualMethodMerge.method(DomainVisualMethodMerge.read(donor), "tridentDamage"));
            node.methods.add(DomainVisualMethodMerge.method(DomainVisualMethodMerge.read(donor), "staffSpellDamage"));
        } else if (path.equals("util/DoraWeaponRules")) {
            field(node, "PULSE_GUN_DAMAGE_MULTIPLIER", 3.0F, 6.0F);
        } else if (path.equals("util/DoraWeaponRules$Mode")) {
            doubleReturn(DomainVisualMethodMerge.method(node, "damage"));
        } else if (path.equals("util/DoraSmgRules")) {
            field(node, "DAMAGE_MULTIPLIER", 2.0F, 4.0F);
            doubleReturn(DomainVisualMethodMerge.method(node, "damage"));
        } else if (path.equals("util/XuanlanRules")) {
            field(node, "WENXUANLAN_DAMAGE_MULTIPLIER", 2.0F, 4.0F);
            doubleReturn(DomainVisualMethodMerge.method(node, "weaponDamage"));
        } else {
            for (String name : METHODS.get(path)) {
                MethodNode method = DomainVisualMethodMerge.method(node, name);
                if (name.equals("totalDamage")) literal(method, 128.0F, 256.0F);
                else redirect(method);
            }
            if (path.equals("util/PufferBatRules")) field(node, "TOTAL_DAMAGE_CAP", 128.0F, 256.0F);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        byte[] result = writer.toByteArray();
        verifyStack(result);
        return result;
    }

    private static void verifyStack(byte[] bytes) throws Exception {
        ClassNode node = DomainVisualMethodMerge.read(bytes);
        for (MethodNode method : node.methods) {
            if ((method.access & (Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) == 0) new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
        }
    }

    public static void verifyUntouched(byte[] before, byte[] after, String path) {
        ClassNode original = DomainVisualMethodMerge.read(before);
        ClassNode result = DomainVisualMethodMerge.read(after);
        int added = path.equals("item/ManorWeaponEnhancements") ? 1 : 0;
        check(result.methods.size() == original.methods.size() + added, "Unexpected method count: " + path);
        for (MethodNode method : original.methods) {
            MethodNode target = DomainVisualMethodMerge.method(result, method.name);
            check(method.desc.equals(target.desc) && method.access == target.access, "Method ABI changed: " + path + "." + method.name);
            boolean equal = Arrays.equals(DomainVisualMethodMerge.fingerprint(method), DomainVisualMethodMerge.fingerprint(target));
            check(equal != METHODS.get(path).contains(method.name), "Unexpected method diff: " + path + "." + method.name);
        }
        for (var originalField : original.fields) {
            result.fields.stream().filter(candidate -> candidate.name.equals(originalField.name)).findFirst().orElseThrow().value = originalField.value;
        }
        check(Arrays.equals(DomainVisualMethodMerge.shell(original), DomainVisualMethodMerge.shell(result)), "Class metadata changed: " + path);
    }

    public static byte[] nested(byte[] bytes) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int hits = 0;
        int kills = 0;
        int channeling = 0;
        try (ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(bytes)); ZipOutputStream target = new ZipOutputStream(output)) {
            ZipEntry entry;
            while ((entry = input.getNextEntry()) != null) {
                byte[] contents = input.readAllBytes();
                Set<String> allowed = Set.of();
                if (entry.getName().equals("me/fzzyhmstrs/tridents_n_stuff/TridentModifier.class")) allowed = Set.of("onHit", "onKill");
                if (entry.getName().equals("me/fzzyhmstrs/tridents_n_stuff/entity/TnsTridentEntity.class")) allowed = Set.of("m_5790_");
                if (!allowed.isEmpty()) {
                    ClassNode node = DomainVisualMethodMerge.read(contents);
                    for (String name : allowed) {
                        MethodNode method = DomainVisualMethodMerge.method(node, name);
                        for (var instruction : method.instructions.toArray()) {
                            if (name.equals("onHit") && instruction instanceof LdcInsnNode literal
                                    && (Float.valueOf(8.0F).equals(literal.cst) || Float.valueOf(6.0F).equals(literal.cst))) {
                                InsnList scale = new InsnList();
                                scale.add(new VarInsnNode(Opcodes.ALOAD, 1));
                                scale.add(new VarInsnNode(Opcodes.ALOAD, 2));
                                scale.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "damage",
                                        "(FLnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;)F", false));
                                method.instructions.insert(instruction, scale);
                                hits++;
                            }
                            if (instruction instanceof MethodInsnNode call && call.owner.equals("net/minecraft/world/entity/LightningBolt") && call.name.equals("<init>")) {
                                InsnList scale = new InsnList();
                                if (name.equals("onKill")) {
                                    scale.add(new VarInsnNode(Opcodes.ALOAD, 1));
                                    scale.add(new VarInsnNode(Opcodes.ALOAD, 2));
                                    kills++;
                                } else {
                                    check(name.equals("m_5790_"), "Unexpected lightning constructor");
                                    scale.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    scale.add(new FieldInsnNode(Opcodes.GETFIELD, node.name, "tridentStack", "Lnet/minecraft/world/item/ItemStack;"));
                                    scale.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    scale.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, node.name, "m_19749_", "()Lnet/minecraft/world/entity/Entity;", false));
                                    channeling++;
                                }
                                scale.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOKS, "lightning",
                                        "(Lnet/minecraft/world/entity/LightningBolt;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/entity/LightningBolt;", false));
                                method.instructions.insert(instruction, scale);
                            }
                        }
                    }
                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    node.accept(writer);
                    byte[] after = writer.toByteArray();
                    verifyStack(after);
                    ClassNode original = DomainVisualMethodMerge.read(contents);
                    ClassNode patched = DomainVisualMethodMerge.read(after);
                    check(Arrays.equals(DomainVisualMethodMerge.shell(original), DomainVisualMethodMerge.shell(patched)), "External class metadata preserved");
                    check(original.methods.size() == patched.methods.size(), "External method count preserved");
                    for (MethodNode previous : original.methods) {
                        MethodNode result = patched.methods.stream().filter(candidate -> candidate.name.equals(previous.name) && candidate.desc.equals(previous.desc)).findFirst().orElseThrow();
                        boolean equal = Arrays.equals(DomainVisualMethodMerge.fingerprint(previous), DomainVisualMethodMerge.fingerprint(result));
                        check(equal != allowed.contains(previous.name), "External method diff: " + previous.name + previous.desc);
                    }
                    contents = after;
                }
                ZipEntry next = new ZipEntry(entry.getName());
                next.setTime(entry.getTime());
                target.putNextEntry(next);
                target.write(contents);
                target.closeEntry();
            }
        }
        check(hits == 2 && kills == 1 && channeling == 1, "Unexpected external trident skill paths");
        return output.toByteArray();
    }

    public static void main(String[] arguments) throws Exception {
        Path baseline = Path.of(arguments[0]);
        Path output = Path.of(arguments[1]);
        Path known = Path.of(arguments[2]);
        Path compiled = Path.of(arguments[3]);
        Map<String, byte[]> replacements = new LinkedHashMap<>();
        try (ZipFile current = new ZipFile(baseline.toFile()); ZipFile reference = new ZipFile(known.toFile())) {
            check(current.getEntry(RULES + ".class") == null, "Weapon rebalance already installed");
            for (String path : METHODS.keySet()) {
                String entryName = PREFIX + path + ".class";
                byte[] before = entry(current, entryName);
                ClassNode original = DomainVisualMethodMerge.read(before);
                ClassNode expected = DomainVisualMethodMerge.read(entry(reference, entryName));
                for (String name : METHODS.get(path)) {
                    check(Arrays.equals(DomainVisualMethodMerge.fingerprint(DomainVisualMethodMerge.method(original, name)),
                            DomainVisualMethodMerge.fingerprint(DomainVisualMethodMerge.method(expected, name))), "Concurrent scoped edit: " + path + "." + name);
                }
                byte[] after = patch(before, path, Files.readAllBytes(compiled.resolve("donor/" + ENHANCEMENTS + ".class")));
                verifyUntouched(before, after, path);
                replacements.put(entryName, after);
            }
            replacements.put(RULES + ".class", Files.readAllBytes(compiled.resolve(RULES + ".class")));
            replacements.put(HOOKS + ".class", Files.readAllBytes(compiled.resolve("donor/" + HOOKS + ".class")));
            byte[] originalNested = entry(current, NESTED);
            check(Arrays.equals(originalNested, entry(reference, NESTED)), "Concurrent external trident change; refuse rollback");
            replacements.put(NESTED, nested(originalNested));
            try (ZipOutputStream target = new ZipOutputStream(Files.newOutputStream(output))) {
                var entries = current.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry previous = entries.nextElement();
                    ZipEntry next = new ZipEntry(previous.getName());
                    next.setTime(previous.getTime());
                    target.putNextEntry(next);
                    target.write(replacements.containsKey(previous.getName()) ? replacements.get(previous.getName()) : entry(current, previous.getName()));
                    target.closeEntry();
                }
                target.putNextEntry(new ZipEntry(RULES + ".class"));
                target.write(replacements.get(RULES + ".class"));
                target.closeEntry();
                target.putNextEntry(new ZipEntry(HOOKS + ".class"));
                target.write(replacements.get(HOOKS + ".class"));
                target.closeEntry();
            }
        }
        int preserved = 0;
        try (ZipFile before = new ZipFile(baseline.toFile()); ZipFile after = new ZipFile(output.toFile())) {
            check(after.size() == before.size() + 2, "Unexpected JAR entry count");
            var entries = before.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (replacements.containsKey(name)) continue;
                check(Arrays.equals(entry(before, name), entry(after, name)), "Unrelated entry changed: " + name);
                preserved++;
            }
        }
        var audit = Map.of("mode", "CURRENT_BASELINE_METHOD_MERGE", "baselineSHA256", hash(Files.readAllBytes(baseline)),
                "candidateSHA256", hash(Files.readAllBytes(output)), "changed", replacements.keySet(), "unchangedEntries", preserved,
                "damageScale", 2, "excluded", Set.of("ftboss:black_cleaver", "ftboss:statue_youzi"), "inGameVerified", false);
        Files.writeString(Path.of(output + ".audit.json"), new GsonBuilder().setPrettyPrinting().create().toJson(audit));
        System.out.println("PASS scoped weapon merge; " + preserved + " original entries byte-identical; only damage/attack paths changed");
    }
}
