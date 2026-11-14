import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** 保留当前运行包的其他改动，仅注入雪黎阶段调度入口（2026-10-26）。 */
public final class XueliPhasePatch implements Opcodes {
    private static final String ROOT = "net/farmingtales/ftboss/";
    private static final String ENTITY = ROOT + "entity/EasterBossEntity";
    private static final String PHASES = ROOT + "boss/ai/XueliPhases";
    private static final String INTEGER_HOOK = "(IL" + ENTITY + ";)I";

    private static ClassNode read(ZipFile archive, String name) throws Exception {
        ClassNode result = new ClassNode();
        new ClassReader(archive.getInputStream(archive.getEntry(name + ".class")).readAllBytes()).accept(result, 0);
        return result;
    }

    private static MethodNode method(ClassNode type, String name) {
        MethodNode result = type.methods.stream().filter(candidate -> candidate.name.equals(name)).findFirst().orElseThrow();
        for (AbstractInsnNode instruction : result.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(PHASES)) throw new IllegalStateException("Already patched: " + name);
        }
        return result;
    }

    private static void save(ClassNode type, Path payload) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path file = payload.resolve(type.name + ".class");
        Files.createDirectories(file.getParent());
        Files.write(file, writer.toByteArray());
    }

    private static void expect(int count, int expected, String label) {
        if (count != expected) throw new IllegalStateException(label + ": " + count + " != " + expected);
    }

    private static InsnList integerHook(String name) {
        InsnList instructions = new InsnList();
        instructions.add(new VarInsnNode(ALOAD, 0));
        instructions.add(new MethodInsnNode(INVOKESTATIC, PHASES, name, INTEGER_HOOK, false));
        return instructions;
    }

    public static void main(String[] arguments) throws Exception {
        Path compiled = Path.of(arguments[1]);
        Path payload = Path.of(arguments[3]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        try (ZipFile baseline = new ZipFile(arguments[0])) {
            if (baseline.getEntry(PHASES + ".class") == null) {
            ClassNode entity = read(baseline, ENTITY);
            MethodNode attack = method(entity, "getMeleeAttackInterval");
            int count = 0;
            for (AbstractInsnNode instruction : attack.instructions.toArray()) {
                if (instruction.getOpcode() != IRETURN) continue;
                attack.instructions.insertBefore(instruction, integerHook("attackInterval"));
                count++;
            }
            expect(count, 1, "attack interval");
            MethodNode tick = method(entity, names.mapMethodName(ENTITY, "tick", "()V"));
            MethodNode clock = entity.methods.stream().filter(candidate -> candidate.name.equals("tickSharedSkillCooldown")).findFirst().orElse(tick);
            count = 0;
            for (AbstractInsnNode instruction : clock.instructions.toArray()) {
                if (!(instruction instanceof FieldInsnNode field) || field.getOpcode() != PUTFIELD || !field.name.equals("skillCd")) continue;
                if (clock == tick && instruction.getPrevious().getOpcode() != ISUB) continue;
                clock.instructions.insertBefore(instruction, integerHook("remainingSkillCooldown"));
                count++;
            }
            expect(count, 1, "remaining skill cooldown");
            count = 0;
            for (AbstractInsnNode instruction : tick.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode call) || !call.owner.equals(ROOT + "util/ManorBossCombatRules")
                        || !call.name.equals("skillCooldown")) continue;
                tick.instructions.insert(instruction, integerHook("skillInterval"));
                count++;
            }
            expect(count, 1, "skill scheduler");
            MethodNode cast = method(entity, "castSkill");
            InsnList gate = new InsnList();
            gate.add(new VarInsnNode(ALOAD, 0));
            gate.add(new MethodInsnNode(INVOKESTATIC, PHASES, "shouldCast", "(L" + ENTITY + ";)Z", false));
            LabelNode original = new LabelNode();
            gate.add(new JumpInsnNode(IFNE, original));
            gate.add(new InsnNode(RETURN));
            gate.add(original);
            gate.add(new FrameNode(F_SAME, 0, null, 0, null));
            cast.instructions.insert(gate);
            MethodNode clones = method(entity, "xueliCloneTick");
            count = 0;
            for (AbstractInsnNode instruction : clones.instructions.toArray()) {
                if (!(instruction instanceof IntInsnNode constant) || constant.operand != 600) continue;
                clones.instructions.insert(instruction, integerHook("cooldown"));
                count++;
            }
            expect(count, 1, "clone reset cooldown");
            InsnList clamp = new InsnList();
            clamp.add(new VarInsnNode(ALOAD, 0));
            clamp.add(new VarInsnNode(ALOAD, 0));
            clamp.add(new FieldInsnNode(GETFIELD, ENTITY, "xueliCloneCd", "I"));
            clamp.add(new IntInsnNode(SIPUSH, 600));
            clamp.add(integerHook("cooldown"));
            clamp.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Math", "min", "(II)I", false));
            clamp.add(new FieldInsnNode(PUTFIELD, ENTITY, "xueliCloneCd", "I"));
            clones.instructions.insert(clamp);
            save(entity, payload);

            ClassNode goal = read(baseline, ENTITY + "$PlayerReachMeleeGoal");
            boolean sharedClock = goal.methods.stream().anyMatch(candidate -> candidate.name.equals("refreshMeleeCooldown"));
            MethodNode refresh = method(goal, sharedClock ? "refreshMeleeCooldown" : names.mapMethodName(goal.name, "tick", "()V"));
            count = 0;
            for (AbstractInsnNode instruction : refresh.instructions.toArray()) {
                if (!(instruction instanceof FieldInsnNode field) || field.getOpcode() != PUTFIELD || !field.name.equals("qingyiMeleeCooldown")) continue;
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(ALOAD, 0));
                hook.add(new FieldInsnNode(GETFIELD, goal.name, "boss", "L" + ENTITY + ";"));
                hook.add(new MethodInsnNode(INVOKESTATIC, PHASES, "remainingMeleeCooldown", INTEGER_HOOK, false));
                refresh.instructions.insertBefore(instruction, hook);
                count++;
            }
            expect(count, 1, "remaining melee cooldown");
            save(goal, payload);

            ClassNode maids = read(baseline, ROOT + "boss/ai/XueliMaids");
            MethodNode ready = method(maids, "canTrigger");
            count = 0;
            for (AbstractInsnNode instruction : ready.instructions.toArray()) {
                if (!(instruction instanceof LdcInsnNode constant) || !Long.valueOf(2400L).equals(constant.cst)) continue;
                InsnList hook = new InsnList();
                hook.add(new IntInsnNode(SIPUSH, 2400));
                hook.add(new VarInsnNode(ALOAD, 0));
                hook.add(new FieldInsnNode(GETFIELD, maids.name, "boss", "L" + ENTITY + ";"));
                hook.add(new MethodInsnNode(INVOKESTATIC, PHASES, "cooldown", INTEGER_HOOK, false));
                hook.add(new InsnNode(I2L));
                ready.instructions.insertBefore(instruction, hook);
                ready.instructions.remove(instruction);
                count++;
            }
            expect(count, 1, "maid cooldown");
            save(maids, payload);
            }
        }
        for (String name : new String[] {PHASES, ROOT + "util/XueliPhaseRules"}) {
            ClassNode type = new ClassNode();
            new ClassReader(Files.readAllBytes(compiled.resolve(name + ".class"))).accept(new ClassRemapper(type, names), 0);
            save(type, payload);
        }
        System.out.println("PASS: Xueli phase hooks added without recompiling unrelated runtime behaviour");
    }
}
