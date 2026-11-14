import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class DoraCombatPatch {
    public static final String BOSS = DoraSpearDamagePatch.BOSS;
    public static final String CYCLE = "net/farmingtales/ftboss/util/DoraSniperCycle";
    public static final String DAMAGE = "net/farmingtales/ftboss/util/DoraSniperDamageRules";
    private static final String VECTOR = "net/minecraft/world/phys/Vec3";
    private static final String LEVEL = "net/minecraft/server/level/ServerLevel";
    private static final String TARGET = "net/minecraft/world/entity/LivingEntity";
    private static final List<String> EDITED = List.of("tickDoraSpear", "tickDoraSniper", "reloadTick",
            "startDoraSniper", "syncDoraSniperCue", "fireDoraSniper");

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static ClassNode read(byte[] data) {
        ClassNode type = new ClassNode();
        new ClassReader(data).accept(type, ClassReader.EXPAND_FRAMES);
        return type;
    }

    private static byte[] entry(Path archive, String owner) throws Exception {
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            return zip.getInputStream(zip.getEntry(owner + ".class")).readAllBytes();
        }
    }

    private static byte[] bytes(ClassNode type) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        return writer.toByteArray();
    }

    private static MethodNode method(ClassNode type, String name) {
        var matches = type.methods.stream().filter(candidate -> candidate.name.equals(name)).toList();
        require(matches.size() == 1, "Ambiguous method: " + name);
        return matches.get(0);
    }

    private static MethodInsnNode call(MethodNode method, String name) {
        var matches = Arrays.stream(method.instructions.toArray()).filter(instruction ->
                instruction instanceof MethodInsnNode invocation && invocation.name.equals(name)).toList();
        require(matches.size() == 1, "Ambiguous call: " + method.name + "." + name);
        return (MethodInsnNode) matches.get(0);
    }

    private static AbstractInsnNode previous(AbstractInsnNode instruction, int count) {
        for (int index = 0; index < count; index++) instruction = instruction.getPrevious();
        return instruction;
    }

    private static void remove(MethodNode method, AbstractInsnNode start, AbstractInsnNode end) {
        AbstractInsnNode next;
        for (AbstractInsnNode instruction = start; instruction != end; instruction = next) {
            next = instruction.getNext();
            require(next != null, "Missing exclusive removal boundary");
            method.instructions.remove(instruction);
        }
    }

    private static void guardMovement(MethodNode method, AbstractInsnNode start, MethodInsnNode setter, Object[] locals) {
        LabelNode skip = new LabelNode();
        InsnList guard = new InsnList();
        guard.add(new VarInsnNode(Opcodes.ALOAD, 0));
        guard.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "getDoraSpearTicks", "()I", false));
        guard.add(new JumpInsnNode(Opcodes.IFGT, skip));
        method.instructions.insertBefore(start, guard);
        InsnList end = new InsnList();
        end.add(skip);
        end.add(new FrameNode(Opcodes.F_NEW, locals.length, locals, 0, new Object[0]));
        method.instructions.insert(setter, end);
    }

    private static void patchBoss(ClassNode boss) throws Exception {
        MethodNode spear = method(boss, "tickDoraSpear");
        MethodInsnNode multiply = call(spear, "m_82542_");
        AbstractInsnNode dashStart = previous(multiply, 4);
        require(dashStart instanceof VarInsnNode directionLoad && directionLoad.var == 3, "Unknown dash direction local");
        AbstractInsnNode dashEnd = Arrays.stream(spear.instructions.toArray()).filter(instruction ->
                instruction instanceof FieldInsnNode field && field.name.equals("f_19812_")
                        && field.getOpcode() == Opcodes.PUTFIELD).findFirst().orElseThrow();
        JumpInsnNode finalBranch = (JumpInsnNode) Arrays.stream(spear.instructions.toArray()).filter(instruction ->
                instruction instanceof JumpInsnNode jump && jump.getOpcode() == Opcodes.IFGT).reduce((first, second) -> second).orElseThrow();
        LabelNode oldElse = finalBranch.label;
        AbstractInsnNode afterCooldown = oldElse.getPrevious();
        require(afterCooldown instanceof JumpInsnNode jump && jump.getOpcode() == Opcodes.GOTO, "Missing combo exit");
        LabelNode finish = ((JumpInsnNode) afterCooldown).label;
        InsnList dash = new InsnList();
        AbstractInsnNode boundary = dashEnd.getNext();
        for (AbstractInsnNode instruction = dashStart; instruction != boundary;) {
            AbstractInsnNode next = instruction.getNext();
            spear.instructions.remove(instruction);
            dash.add(instruction);
            instruction = next;
        }
        remove(spear, oldElse, finish);
        finalBranch.label = finish;
        AbstractInsnNode direction = Arrays.stream(spear.instructions.toArray()).filter(instruction ->
                instruction instanceof VarInsnNode store && store.getOpcode() == Opcodes.ASTORE && store.var == 3)
                .findFirst().orElseThrow();
        spear.instructions.insert(direction, dash);

        MethodNode sniper = method(boss, "tickDoraSniper");
        MethodInsnNode stop = call(sniper, "m_20334_");
        guardMovement(sniper, previous(stop, 6), stop, new Object[]{BOSS, LEVEL, TARGET, CYCLE + "$Step"});
        MethodNode reload = method(boss, "reloadTick");
        MethodInsnNode reloadMove = call(reload, "m_20334_");
        AbstractInsnNode reloadStart = reloadMove.getPrevious();
        while (!(reloadStart instanceof VarInsnNode reloadReceiver && reloadReceiver.getOpcode() == Opcodes.ALOAD && reloadReceiver.var == 0)) {
            reloadStart = reloadStart.getPrevious();
        }
        guardMovement(reload, reloadStart, reloadMove, new Object[]{BOSS, LEVEL, Opcodes.INTEGER, TARGET, VECTOR, VECTOR});

        MethodNode start = method(boss, "startDoraSniper");
        int timing = 0;
        for (AbstractInsnNode instruction : start.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode invocation && invocation.name.equals("acceleratedTicks")
                    && invocation.getPrevious() instanceof FieldInsnNode field && field.owner.equals(CYCLE)) {
                require(field.name.equals("CHARGE_TICKS") || field.name.equals("TRACK_TICKS"), "Unexpected sniper clock");
                AbstractInsnNode difficulty = field.getPrevious();
                require(difficulty instanceof MethodInsnNode getter && getter.name.equals("getChallengeDifficulty"), "Missing difficulty getter");
                start.instructions.remove(difficulty.getPrevious());
                start.instructions.remove(difficulty);
                start.instructions.remove(invocation);
                timing++;
            }
        }
        require(timing == 4, "Expected four charge/track difficulty scales");
        MethodNode cue = method(boss, "syncDoraSniperCue");
        AbstractInsnNode durationStart = Arrays.stream(cue.instructions.toArray()).filter(instruction ->
                instruction instanceof VarInsnNode load && load.getOpcode() == Opcodes.ILOAD && load.var == 1)
                .findFirst().orElseThrow();
        AbstractInsnNode durationStore = Arrays.stream(cue.instructions.toArray()).filter(instruction ->
                instruction instanceof VarInsnNode store && store.getOpcode() == Opcodes.ISTORE && store.var == 4)
                .findFirst().orElseThrow();
        InsnList duration = new InsnList();
        duration.add(new VarInsnNode(Opcodes.ILOAD, 1));
        duration.add(new VarInsnNode(Opcodes.ALOAD, 0));
        duration.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "isImpossibleChallenge", "()Z", false));
        duration.add(new MethodInsnNode(Opcodes.INVOKESTATIC, CYCLE, "cueTicks", "(ZZ)I", false));
        cue.instructions.insertBefore(durationStart, duration);
        remove(cue, durationStart, durationStore);
        MethodInsnNode acceleration = call(cue, "acceleratedTicks");
        AbstractInsnNode argument = acceleration.getPrevious();
        AbstractInsnNode getter = argument.getPrevious();
        cue.instructions.remove(getter.getPrevious());
        cue.instructions.remove(getter);
        cue.instructions.remove(acceleration);
        int radii = 0;
        for (String name : new String[]{"syncDoraSniperCue", "fireDoraSniper"}) {
            MethodNode attack = method(boss, name);
            for (AbstractInsnNode instruction : attack.instructions.toArray()) {
                if (instruction instanceof LdcInsnNode literal && literal.cst.equals(0.72)) {
                    literal.cst = 1.44;
                    radii++;
                }
                if (name.equals("fireDoraSniper") && instruction instanceof MethodInsnNode invocation
                        && invocation.owner.equals("net/minecraft/world/phys/AABB") && invocation.name.equals("m_82400_")
                        && invocation.getPrevious().getOpcode() == Opcodes.DCONST_1) {
                    attack.instructions.set(invocation.getPrevious(), new LdcInsnNode(1.44));
                    radii++;
                }
            }
        }
        require(radii == 3, "Expected cue, broadphase and narrowphase radii");
        for (String name : EDITED) new Analyzer<>(new BasicVerifier()).analyze(BOSS, method(boss, name));
    }

    private static void patchDamage(ClassNode damage) {
        int amounts = 0;
        for (AbstractInsnNode instruction : method(damage, "<clinit>").instructions) {
            if (instruction instanceof LdcInsnNode literal && literal.cst instanceof Float value) {
                require(value == 80.0F, "Unexpected installed sniper damage; do not triple twice");
                literal.cst = value * 3;
                amounts++;
            }
        }
        require(amounts == 3, "Expected three damage channels");
    }

    public static void main(String[] arguments) throws Exception {
        Path baseline = Path.of(arguments[1]);
        Path payload = Path.of(arguments[2]);
        if (arguments[0].equals("apply")) {
            DoraSpearDamagePatch.main(new String[]{"apply", baseline.toString(), payload.toString()});
            ClassNode boss = read(Files.readAllBytes(payload.resolve(BOSS + ".class")));
            patchBoss(boss);
            Files.write(payload.resolve(BOSS + ".class"), bytes(boss));
            ClassNode damage = read(entry(baseline, DAMAGE));
            patchDamage(damage);
            Path output = payload.resolve(DAMAGE + ".class");
            Files.createDirectories(output.getParent());
            Files.write(output, bytes(damage));
            System.out.println("PASS three forward dashes, sniper/reload movement guards, radius and fixed charge cues");
        } else {
            require(arguments[0].equals("check"), "Expected apply/check");
            Path candidate = Path.of(arguments[3]);
            ClassNode original = read(entry(baseline, BOSS));
            ClassNode staged = read(entry(candidate, BOSS));
            for (String name : EDITED) {
                new Analyzer<>(new BasicVerifier()).analyze(BOSS, method(staged, name));
                int index = staged.methods.indexOf(method(staged, name));
                staged.methods.set(index, method(original, name));
            }
            int spearIndex = staged.methods.indexOf(method(staged, "spearChargeHit"));
            staged.methods.set(spearIndex, method(original, "spearChargeHit"));
            require(Arrays.equals(bytes(original), bytes(staged)), "Unrelated boss methods changed");
            for (String owner : new String[]{BOSS, DAMAGE, CYCLE, DoraSpearDamagePatch.RULES}) {
                require(Arrays.equals(entry(candidate, owner), Files.readAllBytes(payload.resolve(owner + ".class"))), "Payload mismatch " + owner);
            }
            ClassNode expected = read(entry(baseline, DAMAGE));
            patchDamage(expected);
            require(Arrays.equals(bytes(expected), entry(candidate, DAMAGE)), "Damage is not exactly baseline x3");
            System.out.println("PASS all edited method types, exact unaffected-method preservation, payload and baseline-relative damage x3");
        }
    }
}
