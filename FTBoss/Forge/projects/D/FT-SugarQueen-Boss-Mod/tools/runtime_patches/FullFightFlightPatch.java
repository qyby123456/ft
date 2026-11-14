import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class FullFightFlightPatch {
    public static final String BOSS = ManorBaseMovementPatch.BOSS;
    public static final String FIGHT = "net/farmingtales/ftboss/boss/ManorFight";
    public static final String MEMBER = FIGHT + "$Fight";
    public static final String BAN = "net/farmingtales/ftboss/boss/ai/ManorFlyBan";
    public static final String RULES = "net/farmingtales/ftboss/util/FullFightFlightRules";
    public static final String HANDLER = "net/farmingtales/ftboss/boss/ai/FullFightFlightBan";
    public static final String PLAYER = "net/minecraft/world/entity/player/Player";
    public static final String SERVER_PLAYER = "net/minecraft/server/level/ServerPlayer";
    public static final String SERVER_LEVEL = "net/minecraft/server/level/ServerLevel";
    public static final String VECTOR = "net/minecraft/world/phys/Vec3";
    public static final String CONTROL = "net/minecraft/world/entity/ai/control/MoveControl";
    public static final String EVENT = "net/minecraftforge/event/TickEvent$PlayerTickEvent";
    public static final Map<String, Set<String>> CHANGED = Map.of(BOSS,
            Set.of("manorFlyBanTick", "haleyFlightTick", "hasHaleyGlide"), BAN,
            Set.of("trigger", "active", "anyActive", "notifyPlayer"), FIGHT, Set.of());

    public static MethodNode method(ClassNode type, String name, String descriptor) {
        return type.methods.stream().filter(candidate -> candidate.name.equals(name) && candidate.desc.equals(descriptor))
                .findFirst().orElseThrow();
    }

    private static MethodNode replace(ClassNode type, String name, String descriptor) {
        MethodNode previous = method(type, name, descriptor);
        MethodNode replacement = new MethodNode(Opcodes.ASM9, previous.access, name, descriptor,
                previous.signature, previous.exceptions.toArray(String[]::new));
        replacement.visibleAnnotations = previous.visibleAnnotations;
        replacement.invisibleAnnotations = previous.invisibleAnnotations;
        type.methods.set(type.methods.indexOf(previous), replacement);
        return replacement;
    }

    private static void finish(MethodNode method, LabelNode end) {
        method.instructions.add(end);
        method.instructions.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.add(new InsnNode(Opcodes.RETURN));
        method.maxLocals = (method.access & Opcodes.ACC_STATIC) != 0 ? 1 : 2;
        method.maxStack = 9;
    }

    private static void call(InsnList body, int opcode, String owner, String name, String descriptor) {
        body.add(new MethodInsnNode(opcode, owner, name, descriptor, opcode == Opcodes.INVOKEINTERFACE));
    }

    private static void addScopeQuery(ClassNode fight) {
        if (fight.methods.stream().anyMatch(method -> method.name.equals("isFlightBanned"))) return;
        MethodNode source = method(fight, "isPlayerFighting", "(L" + PLAYER + ";)Z");
        MethodNode query = new MethodNode(Opcodes.ASM9, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "isFlightBanned", source.desc, null, null);
        source.accept(query);
        MethodInsnNode distance = Arrays.stream(query.instructions.toArray()).filter(instruction ->
                instruction instanceof MethodInsnNode invocation && invocation.name.equals("m_20275_")
                && invocation.desc.equals("(DDD)D")).map(instruction -> (MethodInsnNode) instruction).findFirst().orElseThrow();
        AbstractInsnNode first = distance;
        for (int step = 0; step < 10; step++) first = first.getPrevious();
        if (!(first instanceof VarInsnNode receiver) || receiver.var != 0) throw new IllegalStateException("Unknown scope method layout");
        AbstractInsnNode comparison = distance.getNext().getNext().getNext();
        if (!(comparison instanceof JumpInsnNode jump) || jump.getOpcode() != Opcodes.IFGT) throw new IllegalStateException("Unknown arena boundary branch");
        InsnList scope = new InsnList();
        scope.add(new VarInsnNode(Opcodes.ALOAD, 0));
        call(scope, Opcodes.INVOKEVIRTUAL, PLAYER, "m_20185_", "()D");
        scope.add(new VarInsnNode(Opcodes.ALOAD, 0));
        call(scope, Opcodes.INVOKEVIRTUAL, PLAYER, "m_20189_", "()D");
        for (String coordinate : new String[]{"f_82479_", "f_82481_"}) {
            scope.add(new VarInsnNode(Opcodes.ALOAD, 2));
            scope.add(new FieldInsnNode(Opcodes.GETFIELD, MEMBER, "anchor", "L" + VECTOR + ";"));
            scope.add(new FieldInsnNode(Opcodes.GETFIELD, VECTOR, coordinate, "D"));
        }
        scope.add(new VarInsnNode(Opcodes.ALOAD, 2));
        scope.add(new FieldInsnNode(Opcodes.GETFIELD, MEMBER, "participants", "Ljava/util/Set;"));
        scope.add(new VarInsnNode(Opcodes.ALOAD, 0));
        call(scope, Opcodes.INVOKEVIRTUAL, PLAYER, "m_20148_", "()Ljava/util/UUID;");
        call(scope, Opcodes.INVOKEINTERFACE, "java/util/Set", "contains", "(Ljava/lang/Object;)Z");
        call(scope, Opcodes.INVOKESTATIC, RULES, "contains", "(DDDDZ)Z");
        scope.add(new JumpInsnNode(Opcodes.IFEQ, jump.label));
        query.instructions.insertBefore(first, scope);
        AbstractInsnNode after = comparison.getNext();
        while (first != after) {
            AbstractInsnNode next = first.getNext();
            query.instructions.remove(first);
            first = next;
        }
        query.localVariables = null;
        query.maxStack = 10;
        fight.methods.add(query);
    }

    public static void patch(ClassNode boss, ClassNode fight, ClassNode ban) {
        addScopeQuery(fight);
        replace(boss, "manorFlyBanTick", "()V").instructions.add(new InsnNode(Opcodes.RETURN));
        MethodNode glide = replace(boss, "hasHaleyGlide", "()Z");
        glide.instructions.add(new InsnNode(Opcodes.ICONST_0));
        glide.instructions.add(new InsnNode(Opcodes.IRETURN));
        MethodNode ground = replace(boss, "haleyFlightTick", "()V");
        InsnList body = ground.instructions;
        LabelNode end = new LabelNode();
        body.add(new VarInsnNode(Opcodes.ALOAD, 0));
        body.add(new FieldInsnNode(Opcodes.GETFIELD, BOSS, "haleyFlying", "Z"));
        body.add(new JumpInsnNode(Opcodes.IFEQ, end));
        body.add(new VarInsnNode(Opcodes.ALOAD, 0));
        body.add(new InsnNode(Opcodes.ICONST_0));
        body.add(new FieldInsnNode(Opcodes.PUTFIELD, BOSS, "haleyFlying", "Z"));
        body.add(new VarInsnNode(Opcodes.ALOAD, 0));
        body.add(new InsnNode(Opcodes.ICONST_0));
        call(body, Opcodes.INVOKEVIRTUAL, BOSS, "m_20242_", "(Z)V");
        body.add(new VarInsnNode(Opcodes.ALOAD, 0));
        body.add(new TypeInsnNode(Opcodes.NEW, CONTROL));
        body.add(new InsnNode(Opcodes.DUP));
        body.add(new VarInsnNode(Opcodes.ALOAD, 0));
        call(body, Opcodes.INVOKESPECIAL, CONTROL, "<init>", "(Lnet/minecraft/world/entity/Mob;)V");
        body.add(new FieldInsnNode(Opcodes.PUTFIELD, BOSS, "f_21342_", "L" + CONTROL + ";"));
        body.add(new VarInsnNode(Opcodes.ALOAD, 0));
        call(body, Opcodes.INVOKEVIRTUAL, BOSS, "m_20184_", "()L" + VECTOR + ";");
        body.add(new VarInsnNode(Opcodes.ASTORE, 1));
        body.add(new VarInsnNode(Opcodes.ALOAD, 0));
        body.add(new VarInsnNode(Opcodes.ALOAD, 1));
        body.add(new FieldInsnNode(Opcodes.GETFIELD, VECTOR, "f_82479_", "D"));
        body.add(new InsnNode(Opcodes.DCONST_0));
        body.add(new VarInsnNode(Opcodes.ALOAD, 1));
        body.add(new FieldInsnNode(Opcodes.GETFIELD, VECTOR, "f_82480_", "D"));
        call(body, Opcodes.INVOKESTATIC, "java/lang/Math", "min", "(DD)D");
        body.add(new VarInsnNode(Opcodes.ALOAD, 1));
        body.add(new FieldInsnNode(Opcodes.GETFIELD, VECTOR, "f_82481_", "D"));
        call(body, Opcodes.INVOKEVIRTUAL, BOSS, "m_20334_", "(DDD)V");
        body.add(new VarInsnNode(Opcodes.ALOAD, 0));
        body.add(new InsnNode(Opcodes.ICONST_1));
        body.add(new FieldInsnNode(Opcodes.PUTFIELD, BOSS, "f_19864_", "Z"));
        finish(ground, end);
        replace(ban, "trigger", "(L" + SERVER_LEVEL + ";DDD)V").instructions.add(new InsnNode(Opcodes.RETURN));
        MethodNode active = replace(ban, "active", "(L" + SERVER_PLAYER + ";)Z");
        LabelNode inactive = new LabelNode();
        active.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        active.instructions.add(new JumpInsnNode(Opcodes.IFNULL, inactive));
        for (String condition : new String[]{"m_6084_", "m_7500_", "m_5833_"}) {
            active.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            call(active.instructions, Opcodes.INVOKEVIRTUAL, SERVER_PLAYER, condition, "()Z");
            active.instructions.add(new JumpInsnNode(condition.equals("m_6084_") ? Opcodes.IFEQ : Opcodes.IFNE, inactive));
        }
        active.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        call(active.instructions, Opcodes.INVOKESTATIC, FIGHT, "isFlightBanned", "(L" + PLAYER + ";)Z");
        active.instructions.add(new InsnNode(Opcodes.IRETURN));
        active.instructions.add(inactive);
        active.instructions.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        active.instructions.add(new InsnNode(Opcodes.ICONST_0));
        active.instructions.add(new InsnNode(Opcodes.IRETURN));
        active.maxLocals = 1;
        MethodNode any = replace(ban, "anyActive", "(L" + SERVER_LEVEL + ";)Z");
        LabelNode absent = new LabelNode();
        any.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        any.instructions.add(new JumpInsnNode(Opcodes.IFNULL, absent));
        any.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        call(any.instructions, Opcodes.INVOKESTATIC, FIGHT, "hasActive", "(L" + SERVER_LEVEL + ";)Z");
        any.instructions.add(new InsnNode(Opcodes.IRETURN));
        any.instructions.add(absent);
        any.instructions.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        any.instructions.add(new InsnNode(Opcodes.ICONST_0));
        any.instructions.add(new InsnNode(Opcodes.IRETURN));
        any.maxLocals = 1;
        MethodNode notify = method(ban, "notifyPlayer", "(L" + SERVER_PLAYER + ";)V");
        int notifications = 0;
        for (AbstractInsnNode instruction : notify.instructions) {
            if (instruction instanceof LdcInsnNode literal && literal.cst instanceof String text && text.contains("[甜苹果庄园]")) {
                literal.cst = "§c[甜苹果庄园] §7本场战斗全程禁飞 —— 请在地面接战";
                notifications++;
            }
        }
        if (notifications != 1) throw new IllegalStateException("Unexpected notification literal count");
    }

    public static byte[] handler() {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, HANDLER, null, "java/lang/Object", null);
        var registration = writer.visitAnnotation("Lnet/minecraftforge/fml/common/Mod$EventBusSubscriber;", true);
        registration.visit("modid", "ftboss");
        registration.visitEnd();
        MethodNode constructor = new MethodNode(Opcodes.ACC_PRIVATE, "<init>", "()V", null, null);
        constructor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        call(constructor.instructions, Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        constructor.instructions.add(new InsnNode(Opcodes.RETURN));
        constructor.accept(writer);
        MethodNode tick = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "onPlayerTick", "(L" + EVENT + ";)V", null, null);
        AnnotationNode subscription = new AnnotationNode("Lnet/minecraftforge/eventbus/api/SubscribeEvent;");
        subscription.visitEnum("priority", "Lnet/minecraftforge/eventbus/api/EventPriority;", "LOWEST");
        tick.visibleAnnotations = java.util.List.of(subscription);
        LabelNode end = new LabelNode();
        for (int load = 0; load < 2; load++) {
            tick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            tick.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, EVENT, "player", "L" + PLAYER + ";"));
            if (load == 0) {
                tick.instructions.add(new TypeInsnNode(Opcodes.INSTANCEOF, SERVER_PLAYER));
                tick.instructions.add(new JumpInsnNode(Opcodes.IFEQ, end));
            } else {
                tick.instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, SERVER_PLAYER));
                tick.instructions.add(new VarInsnNode(Opcodes.ASTORE, 1));
            }
        }
        for (String name : new String[]{"active", "stripFlight"}) {
            tick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            call(tick.instructions, Opcodes.INVOKESTATIC, BAN, name, "(L" + SERVER_PLAYER + ";)Z");
            tick.instructions.add(new JumpInsnNode(Opcodes.IFEQ, end));
        }
        tick.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        call(tick.instructions, Opcodes.INVOKESTATIC, BAN, "notifyPlayer", "(L" + SERVER_PLAYER + ";)V");
        finish(tick, end);
        tick.maxLocals = 2;
        tick.accept(writer);
        writer.visitEnd();
        return writer.toByteArray();
    }

    public static void main(String[] arguments) throws Exception {
        Path baseline = Path.of(arguments[0]), payload = Path.of(arguments[1]);
        ClassNode boss = ManorBaseMovementPatch.read(baseline, BOSS), fight = ManorBaseMovementPatch.read(baseline, FIGHT),
                ban = ManorBaseMovementPatch.read(baseline, BAN);
        patch(boss, fight, ban);
        for (ClassNode type : new ClassNode[]{boss, fight, ban}) {
            Path destination = payload.resolve(type.name + ".class");
            Files.createDirectories(destination.getParent());
            Files.write(destination, ManorBaseMovementPatch.bytes(type));
        }
        Path listener = payload.resolve(HANDLER + ".class");
        Files.createDirectories(listener.getParent());
        Files.write(listener, handler());
        System.out.println("PASS full-fight flight patch; 3 scoped classes and automatic per-player tick listener");
    }
}
