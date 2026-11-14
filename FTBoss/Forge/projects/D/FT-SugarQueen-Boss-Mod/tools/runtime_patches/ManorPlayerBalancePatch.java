import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class ManorPlayerBalancePatch implements Opcodes {
    private static final String ROOT = "net/farmingtales/ftboss/";
    private static final String HOOK = ROOT + "integ/spells/ManorRewardBalance";
    private static Path classes;
    private static Path output;
    private static ZicaiVoidPatch.Names names;

    private static ClassNode read(ZipFile jar, String name) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getEntry(ROOT + name + ".class")).readAllBytes()).accept(node, 0);
        return node;
    }

    private static ClassNode compiled(String name) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(classes.resolve(ROOT + name + ".class")))
                .accept(new ClassRemapper(node, names), 0);
        return node;
    }

    private static MethodNode method(ClassNode node, String name) {
        return node.methods.stream().filter(value -> value.name.equals(name)).findFirst().orElseThrow();
    }

    private static void save(ClassNode node) throws Exception {
        for (MethodNode method : node.methods) {
            if ((method.access & (ACC_ABSTRACT | ACC_NATIVE)) == 0)
                new Analyzer<>(new BasicVerifier()).analyze(node.name, method);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Path path = output.resolve(node.name + ".class");
        Files.createDirectories(path.getParent());
        Files.write(path, writer.toByteArray());
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    public static void main(String[] arguments) throws Exception {
        classes = Path.of(arguments[1]);
        names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        output = Path.of(arguments[3]);
        try (ZipFile baseline = new ZipFile(arguments[0])) {
            require(baseline.getEntry(HOOK + ".class") == null, "Already patched baseline");
            for (String name : new String[]{"util/ManorRewardBalanceRules", "integ/spells/ManorRewardBalance",
                    "util/ManorDamageLimitRules", "util/ManorDamageLimitRules$Window"}) save(compiled(name));
            ClassNode weapon = read(baseline, "util/ManorWeaponBalanceRules");
            ClassNode weaponDonor = compiled("util/ManorWeaponBalanceRules");
            for (String name : new String[]{"attackBonus", "attributeTarget"}) {
                MethodNode previous = method(weapon, name);
                weapon.methods.set(weapon.methods.indexOf(previous), method(weaponDonor, name));
            }
            for (FieldNode field : weaponDonor.fields) {
                if (field.name.equals("BLACK_CLEAVER_BASE_DAMAGE") || field.name.equals("SANGUINE_BASE_DAMAGE")) {
                    require(weapon.fields.stream().noneMatch(value -> value.name.equals(field.name)), "Existing weapon field");
                    weapon.fields.add(field);
                }
            }
            save(weapon);
            ClassNode armor = read(baseline, "item/EquipmentWeaponBuild");
            FieldNode strength = armor.fields.stream().filter(field -> field.name.equals("STRENGTH")).findFirst().orElseThrow();
            double previous = (Double) strength.value;
            require(previous == 1.5, "Preserve the newer explicit 150-percent armor adjustment: " + previous);
            ClassNode fullPower = read(baseline, "integ/spells/FullPowerBossSpell");
            require(fullPower.methods.stream().noneMatch(value -> value.name.equals("getManaCost")), "Mana override exists");
            fullPower.methods.add(method(compiled("integ/spells/FullPowerBossSpell"), "getManaCost"));
            save(fullPower);
            ClassNode registry = read(baseline, "integ/spells/ManorIronSpells");
            MethodNode register = method(registry, "register");
            int registrations = 0;
            for (AbstractInsnNode instruction : register.instructions.toArray()) {
                if (instruction.getOpcode() != RETURN) continue;
                InsnList hook = new InsnList();
                hook.add(new FieldInsnNode(GETSTATIC, "net/minecraftforge/common/MinecraftForge", "EVENT_BUS",
                        "Lnet/minecraftforge/eventbus/api/IEventBus;"));
                hook.add(new LdcInsnNode(Type.getObjectType(HOOK)));
                hook.add(new MethodInsnNode(INVOKEINTERFACE, "net/minecraftforge/eventbus/api/IEventBus", "register",
                        "(Ljava/lang/Object;)V", true));
                register.instructions.insertBefore(instruction, hook);
                registrations++;
            }
            require(registrations == 1, "Expected one damage listener registration");
            save(registry);
            for (String name : new String[]{"ManorSignatureSpell", "QizaiSpell", "QizaiBarrageSpell"}) {
                ClassNode spell = read(baseline, "integ/spells/" + name);
                int displays = 0;
                for (AbstractInsnNode instruction : method(spell, "getUniqueInfo").instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode call) || !call.name.equals("getSpellPower")) continue;
                    InsnList preview = new InsnList();
                    preview.add(new VarInsnNode(ALOAD, 2));
                    preview.add(new MethodInsnNode(INVOKESTATIC, HOOK, "displayDamage",
                            "(FLnet/minecraft/world/entity/LivingEntity;)F", false));
                    method(spell, "getUniqueInfo").instructions.insert(instruction, preview);
                    displays++;
                }
                require(displays == 1, "Expected one damage preview: " + name);
                save(spell);
            }
        }
        System.out.println("PASS: method-level balance patch; two new classes; newer 150-percent armor preserved when present");
    }
}
