import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public final class XueliCrushTurretPatch implements Opcodes {
    static final String ROOT = "net/farmingtales/ftboss/";
    static final String ENTITY = ROOT + "entity/EasterBossEntity";
    static final String CRUSH = ROOT + "boss/ai/XueliCrushDamage";
    static final String OUTGOING = ROOT + "boss/ai/BossOutgoingDamageNerfs";
    static final String RULES = ROOT + "balance/ManorExpansionRules";
    static final String LIVING = "net/minecraft/world/entity/LivingEntity";
    static final String PLAYER = "net/minecraft/world/entity/player/Player";
    static final String EVENT = "net/minecraftforge/event/entity/living/LivingHurtEvent";

    static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    static ClassNode read(byte[] bytes) {
        ClassNode result = new ClassNode();
        new ClassReader(bytes).accept(result, 0);
        return result;
    }

    static ClassNode read(ZipFile archive, String name) throws Exception {
        return read(archive.getInputStream(archive.getEntry(name + ".class")).readAllBytes());
    }

    static MethodNode method(ClassNode type, String name) {
        return type.methods.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
    }

    static void save(ClassNode type, Path payload) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path file = payload.resolve(type.name + ".class");
        Files.createDirectories(file.getParent());
        Files.write(file, writer.toByteArray());
    }

    public static void main(String[] arguments) throws Exception {
        Path compiled = Path.of(arguments[1]);
        Path payload = Path.of(arguments[2]);
        try (ZipFile archive = new ZipFile(arguments[0])) {
            ClassNode entity = read(archive, ENTITY);
            MethodNode tick = method(entity, "xueliCrushTick");
            int expiry = 0;
            int pulses = 0;
            for (AbstractInsnNode instruction : tick.instructions.toArray()) {
                if (instruction instanceof JumpInsnNode jump && jump.getOpcode() == IFGT
                        && jump.getPrevious() instanceof VarInsnNode variable && variable.var == 5) {
                    jump.setOpcode(IFGE);
                    expiry++;
                }
                if (!(instruction instanceof MethodInsnNode call) || !call.owner.equals(CRUSH) || !call.name.equals("hurt")) continue;
                AbstractInsnNode start = call;
                while (!(start instanceof FieldInsnNode field && field.getOpcode() == PUTFIELD
                        && field.owner.equals(PLAYER) && field.desc.equals("I"))) start = start.getPrevious();
                start = start.getPrevious().getPrevious();
                require(start instanceof VarInsnNode variable && variable.getOpcode() == ALOAD && variable.var == 4,
                        "Unexpected crush target local");
                require(call.getNext().getOpcode() == POP, "Unexpected crush return handling");
                LabelNode visuals = new LabelNode();
                InsnList gate = new InsnList();
                gate.add(new VarInsnNode(ILOAD, 5));
                gate.add(new IntInsnNode(BIPUSH, 20));
                gate.add(new InsnNode(IREM));
                gate.add(new JumpInsnNode(IFNE, visuals));
                tick.instructions.insertBefore(start, gate);
                InsnList continuation = new InsnList();
                continuation.add(visuals);
                continuation.add(new FrameNode(F_FULL, 6, new Object[] {ENTITY, "net/minecraft/server/level/ServerLevel",
                        "java/util/Iterator", "java/util/Map$Entry", PLAYER, INTEGER}, 0, null));
                tick.instructions.insert(call.getNext(), continuation);
                pulses++;
            }
            require(expiry == 1 && pulses == 1, "Crush cadence patch must match exactly once");
            int messages = 0;
            for (AbstractInsnNode instruction : method(entity, "startCrush").instructions) {
                if (!(instruction instanceof LdcInsnNode constant) || !(constant.cst instanceof String text)) continue;
                String updated = text.replace("60 tick × 5 点", "60 tick, 每秒 5 点")
                        .replace("吃到粉碎轮 {} tick x {} 点", "吃到粉碎轮 {} tick, 每秒 {} 点");
                if (!updated.equals(text)) { constant.cst = updated; messages++; }
            }
            require(messages == 2, "Expected crush message and log update");
            save(entity, payload);

            ClassNode crush = read(archive, CRUSH);
            require(crush.methods.stream().noneMatch(candidate -> candidate.name.equals("isActive")), "Already patched");
            ClassNode donor = read(Files.readAllBytes(compiled.resolve(CRUSH + ".class")));
            crush.methods.add(method(donor, "isActive"));
            save(crush, payload);

            ClassNode outgoing = read(archive, OUTGOING);
            MethodNode hurt = method(outgoing, "onHurt");
            InsnList guard = new InsnList();
            LabelNode original = new LabelNode();
            guard.add(new VarInsnNode(ALOAD, 0));
            guard.add(new MethodInsnNode(INVOKEVIRTUAL, EVENT, "getEntity", "()L" + LIVING + ";", false));
            guard.add(new MethodInsnNode(INVOKESTATIC, CRUSH, "isActive", "(L" + LIVING + ";)Z", false));
            guard.add(new JumpInsnNode(IFEQ, original));
            guard.add(new InsnNode(RETURN));
            guard.add(original);
            guard.add(new FrameNode(F_SAME, 0, null, 0, null));
            hurt.instructions.insert(guard);
            save(outgoing, payload);

            ClassNode rules = read(archive, RULES);
            FieldNode interval = rules.fields.stream().filter(field -> field.name.equals("TURRET_INTERVAL")).findFirst().orElseThrow();
            require(Integer.valueOf(40).equals(interval.value), "Unexpected existing turret interval");
            interval.value = 80;
            int intervals = 0;
            for (AbstractInsnNode instruction : method(rules, "turretFires").instructions) {
                if (instruction instanceof IntInsnNode constant && constant.operand == 40) {
                    constant.operand = 80;
                    intervals++;
                }
            }
            require(intervals == 1, "Expected one turret cadence constant");
            save(rules, payload);
        }
        System.out.println("PASS: patched crush cadence, fixed-damage exception, messages and turret interval only");
    }
}
