import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

/** 对当前运行包做定点指令修改，拒绝回灌整份旧Boss源码。 */
public final class ManorSixPatch {
    private static final String ROOT = "net/farmingtales/ftboss/";
    private static final String BOSS = ROOT + "entity/EasterBossEntity";
    private static final String SKY = ROOT + "boss/ai/XuanlanSkyStrike";
    private static final String TEMPLATE = ROOT + "boss/ai/ManorSkyTemplate";
    private static final String LEVEL = "Lnet/minecraft/server/level/ServerLevel;";
    private static final String LIVING = "Lnet/minecraft/world/entity/LivingEntity;";

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode node = new ClassNode();
        try (var input = jar.getInputStream(jar.getJarEntry(name + ".class"))) {
            new ClassReader(input.readAllBytes()).accept(node, 0);
        }
        return node;
    }

    private static MethodNode method(ClassNode type, String name) {
        List<MethodNode> found = type.methods.stream().filter(method -> method.name.equals(name)).toList();
        require(found.size() == 1, "Expected unique method " + type.name + "." + name);
        return found.get(0);
    }

    private static void save(ClassNode type, Path output) throws Exception {
        for (MethodNode method : type.methods) {
            if ((method.access & (Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) == 0)
                new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path target = output.resolve(type.name + ".class");
        Files.createDirectories(target.getParent());
        Files.write(target, writer.toByteArray());
    }

    private static int constants(MethodNode method, Map<Double, Double> values) {
        int changed = 0;
        for (var instruction : method.instructions) {
            if (instruction instanceof LdcInsnNode load && load.cst instanceof Double value && values.containsKey(value)) {
                load.cst = values.get(value);
                changed++;
            }
        }
        return changed;
    }

    private static void damage(MethodNode method, int expected) {
        int count = 0;
        for (var instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode call && call.name.equals("m_6469_")
                    && call.desc.equals("(Lnet/minecraft/world/damagesource/DamageSource;F)Z")) {
                InsnList multiplier = new InsnList();
                multiplier.add(new LdcInsnNode(1.25F));
                multiplier.add(new InsnNode(Opcodes.FMUL));
                method.instructions.insertBefore(call, multiplier);
                count++;
            }
        }
        require(count == expected, method.name + " damage site mismatch: " + count);
    }

    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[2]);
        try (JarFile baseline = new JarFile(arguments[0]); JarFile compiled = new JarFile(arguments[1])) {
            ClassNode boss = read(baseline, BOSS);
            damage(method(boss, "pumpBurst"), 2);
            damage(method(boss, "spearChargeHit"), 1);
            for (FieldNode field : boss.fields) {
                if (List.of("BURST_DAMAGE_PHYS", "BURST_DAMAGE_MAGIC", "BURST_DAMAGE", "SPEAR_CHARGE_DAMAGE").contains(field.name))
                    field.value = ((Number) field.value).floatValue() * 1.25F;
                if (field.name.equals("CLEAVE_RADIUS")) field.value = 16.875;
            }
            require(constants(method(boss, "m_8119_"), Map.of(11.25, 16.875)) == 1, "Gongdi scheduler range");
            require(constants(method(boss, "skillCleave"), Map.of(11.25, 16.875, 4.5, 6.75)) == 2, "Cleave telegraph range");
            require(constants(method(boss, "executeCleave"), Map.of(11.25, 16.875, 20.25, 45.5625)) == 2, "Cleave hit range");
            int armor = 0;
            for (var instruction : method(boss, "syncQingyiTridentBuffs").instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals("java/lang/Math")
                        && call.name.equals("min") && call.desc.equals("(II)I")) {
                    if (armor++ == 0) {
                        call.owner = ROOT + "util/ManorRefinementRules";
                        call.name = "armorSteps";
                    }
                }
            }
            require(armor == 2, "Qingyi armor/health cap sites changed");
            int summonCaps = 0;
            for (var instruction : method(boss, "onQingyiTridentSummoned").instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals("java/lang/Math")
                        && call.name.equals("min") && call.desc.equals("(II)I")) {
                    if (summonCaps++ == 2) {
                        call.owner = ROOT + "util/ManorRefinementRules";
                        call.name = "armorSteps";
                    }
                }
            }
            require(summonCaps == 4, "Qingyi summon log cap sites changed");
            save(boss, output);

            ClassNode gongdi = read(baseline, ROOT + "boss/ai/GongdiSkills");
            Map<Double, Double> ranges = Map.of(11.25, 16.875, 6.75, 10.125, 5.1, 7.65, 10.5, 15.75);
            int total = 0;
            for (String name : List.of("tryStart", "pull", "strike", "execute")) {
                int count = constants(method(gongdi, name), ranges);
                require(count > 0, "Missing Gongdi range in " + name);
                total += count;
            }
            require(total == 12, "Expected twelve Gongdi range sites, got " + total);
            save(gongdi, output);
            ClassNode rules = read(baseline, ROOT + "util/GongdiCombatRules");
            Map<String, Double> ruleFields = Map.of("INNER_RADIUS", 6.75, "STRIKE_RANGE", 7.65,
                    "PULL_RANGE", 15.75, "EXECUTE_RANGE", 10.125);
            int fields = 0;
            for (FieldNode field : rules.fields) if (ruleFields.containsKey(field.name)) {
                field.value = ruleFields.get(field.name);
                fields++;
            }
            require(fields == 4, "Gongdi rule fields changed");
            save(rules, output);

            ClassNode sky = read(baseline, SKY);
            require(sky.methods.stream().noneMatch(method -> method.name.equals("queuePrediction")), "Already patched sky");
            ClassNode template = read(compiled, TEMPLATE);
            ClassNode moved = new ClassNode();
            template.accept(new ClassRemapper(moved, new SimpleRemapper(Map.of(TEMPLATE, SKY,
                    TEMPLATE + "$PendingStrike", SKY + "$PendingStrike"))));
            sky.methods.add(method(moved, "queuePrediction"));
            int queue = 0;
            for (var instruction : method(sky, "tick").instructions.toArray()) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals("java/util/ArrayDeque") && call.name.equals("addLast")) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 7));
                    hook.add(new VarInsnNode(Opcodes.ILOAD, 2));
                    hook.add(new IntInsnNode(Opcodes.BIPUSH, 10));
                    hook.add(new InsnNode(Opcodes.IADD));
                    hook.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, SKY, "queuePrediction", "(" + LEVEL + LIVING + "I)V", false));
                    method(sky, "tick").instructions.insert(call, hook);
                    queue++;
                }
            }
            require(queue == 1, "Sky queue site changed");
            save(sky, output);

            ClassNode qizai = read(baseline, ROOT + "boss/ai/QizaiMagic");
            int returns = 0;
            for (var instruction : method(qizai, "begin").instructions.toArray()) {
                if (instruction.getOpcode() == Opcodes.IRETURN && instruction.getPrevious().getOpcode() == Opcodes.ICONST_1) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new FieldInsnNode(Opcodes.GETFIELD, qizai.name, "boss", "L" + BOSS + ";"));
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    hook.add(new VarInsnNode(Opcodes.ILOAD, 3));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ROOT + "boss/ai/ManorPredictiveAttacks", "extraSpell",
                            "(" + LEVEL + "L" + BOSS + ";" + LIVING + "I)V", false));
                    method(qizai, "begin").instructions.insertBefore(instruction, hook);
                    returns++;
                }
            }
            require(returns == 5, "Qizai successful begin sites changed: " + returns);
            save(qizai, output);
        }
        System.out.println("PASS six combat runtime patch: three damage, seventeen range, one armor, prediction hooks");
    }
}
