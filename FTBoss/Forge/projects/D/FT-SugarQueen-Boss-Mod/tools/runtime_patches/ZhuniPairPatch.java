import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class ZhuniPairPatch {
    private static final String PREFIX = "net/farmingtales/ftboss/";
    private static final String BOSS = PREFIX + "entity/EasterBossEntity";
    private static final String HELPER = PREFIX + "boss/ai/ZhuniPairCombat";

    private static ClassNode read(byte[] bytes) {
        ClassNode type = new ClassNode();
        new ClassReader(bytes).accept(type, 0);
        return type;
    }

    private static void save(ClassNode type, Path payload) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        byte[] bytes = writer.toByteArray();
        ClassNode checked = read(bytes);
        for (MethodNode method : checked.methods) {
            if ((method.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) == 0)
                new Analyzer<>(new BasicVerifier()).analyze(checked.name, method);
        }
        Path file = payload.resolve(type.name + ".class");
        Files.createDirectories(file.getParent());
        Files.write(file, bytes);
    }

    private static void expect(int actual, int expected, String label) {
        if (actual != expected) throw new IllegalStateException(label + ": expected " + expected + ", found " + actual);
    }

    private static MethodNode method(ClassNode type, String name) {
        return type.methods.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
    }

    private static void guard(MethodNode method, String hook, String descriptor, int arguments) {
        InsnList prefix = new InsnList();
        for (int slot = 0; slot < arguments; slot++) prefix.add(new VarInsnNode(Opcodes.ALOAD, slot));
        prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, hook, descriptor, false));
        LabelNode original = new LabelNode();
        prefix.add(new JumpInsnNode(Opcodes.IFEQ, original));
        prefix.add(new InsnNode(Opcodes.RETURN));
        prefix.add(original);
        prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(prefix);
    }

    public static void main(String[] arguments) throws Exception {
        Path compiled = Path.of(arguments[1]);
        Path payload = Path.of(arguments[3]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        for (String suffix : new String[]{"boss/ai/ZhuniPairRules", "boss/ai/ZhuniPairCombat", "boss/ai/XuanlanEncounterLimits",
                "turret/ZhuniEntity", "turret/ZhuniStage", "turret/ZhuniTurretDrops"}) {
            ClassNode type = new ClassNode();
            new ClassReader(Files.readAllBytes(compiled.resolve(PREFIX + suffix + ".class")))
                    .accept(new ClassRemapper(type, names), 0);
            save(type, payload);
        }
        try (ZipFile baseline = new ZipFile(arguments[0])) {
            for (String suffix : new String[]{"entity/EasterBossEntity", "boss/ai/ManorHealthBalance", "boss/ai/BossScaling",
                    "turret/XuanlanTurretEntity"}) {
                String owner = PREFIX + suffix;
                ClassNode type = read(baseline.getInputStream(baseline.getEntry(owner + ".class")).readAllBytes());
                for (MethodNode method : type.methods) for (AbstractInsnNode instruction : method.instructions)
                    if (instruction instanceof MethodInsnNode call && call.owner.equals(HELPER))
                        throw new IllegalStateException("Already patched: " + owner);
                if (owner.equals(BOSS)) {
                    String death = names.mapMethodName("net/minecraft/world/entity/LivingEntity", "die",
                            "(Lnet/minecraft/world/damagesource/DamageSource;)V");
                    guard(method(type, death), "deferDeath", "(L" + BOSS + ";Lnet/minecraft/world/damagesource/DamageSource;)Z", 2);
                    MethodNode throwing = method(type, "throwFlywheel");
                    int changes = 0;
                    for (AbstractInsnNode instruction : throwing.instructions.toArray()) {
                        if (instruction instanceof IntInsnNode integer && integer.operand == 15) {
                            InsnList replacement = new InsnList();
                            replacement.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "flywheelLimit", "(L" + BOSS + ";)I", false));
                            throwing.instructions.insertBefore(instruction, replacement);
                            throwing.instructions.remove(instruction);
                            changes++;
                        }
                    }
                    expect(changes, 2, "throw limit and diagnostic count");
                } else if (suffix.endsWith("ManorHealthBalance")) {
                    guard(method(type, "normalize"), "normalize", "(Lnet/minecraft/world/entity/LivingEntity;)Z", 1);
                } else if (suffix.endsWith("BossScaling")) {
                    int changes = 0;
                    for (AbstractInsnNode instruction : method(type, "tick").instructions) {
                        if (instruction instanceof MethodInsnNode call && call.owner.equals(PREFIX + "boss/ai/ManorRaidCombat")
                                && call.name.equals("scaleMaximumHealth")) {
                            call.owner = HELPER;
                            call.name = "maximumHealth";
                            changes++;
                        }
                    }
                    expect(changes, 1, "fixed pair health scaling");
                } else {
                    Map<String, String> gates = Map.of("isNoAi", "turretNoAi", "isInvulnerable", "turretInvulnerable",
                            "isInvisible", "turretInvisible", "isManorIntroHidden", "turretIntroHidden");
                    String tick = names.mapMethodName("net/minecraft/world/entity/Entity", "tick", "()V");
                    int changes = 0;
                    for (AbstractInsnNode instruction : method(type, tick).instructions) {
                        if (!(instruction instanceof MethodInsnNode call) || !call.owner.equals(BOSS)) continue;
                        for (var gate : gates.entrySet()) {
                            if (!call.name.equals(names.mapMethodName(BOSS, gate.getKey(), "()Z"))) continue;
                            call.owner = HELPER;
                            call.name = gate.getValue();
                            call.desc = "(L" + BOSS + ";)Z";
                            call.setOpcode(Opcodes.INVOKESTATIC);
                            changes++;
                            break;
                        }
                    }
                    expect(changes, 4, "turrets survive defeated Xuanlan anchor");
                }
                save(type, payload);
            }
        }
        System.out.println("PASS: pair payload and four surgical runtime hooks; JVM stacks verified");
    }
}
