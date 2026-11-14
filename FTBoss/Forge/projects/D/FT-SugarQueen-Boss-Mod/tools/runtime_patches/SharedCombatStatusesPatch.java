import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;
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

/** 当前运行基线的共享状态增量补丁；保留其余字节码。2026-10-26：新增。 */
public final class SharedCombatStatusesPatch {
    public static final String PREFIX = "net/farmingtales/ftboss/";
    public static final String HOOK = PREFIX + "boss/ai/CombatStatusHooks";
    private static final String LIVING = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final String EFFECT = "Lnet/minecraft/world/effect/MobEffect;";

    private static ClassNode read(ZipFile jar, String name) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getEntry(name + ".class")).readAllBytes()).accept(type, 0);
        return type;
    }

    private static void save(ClassNode type, Path payload) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path file = payload.resolve(type.name + ".class");
        Files.createDirectories(file.getParent());
        Files.write(file, writer.toByteArray());
    }

    private static MethodNode method(ClassNode type, String name) {
        var methods = type.methods.stream().filter(candidate -> candidate.name.equals(name)).toList();
        if (methods.size() != 1) throw new IllegalStateException("Ambiguous/missing method " + type.name + "/" + name);
        MethodNode method = methods.get(0);
        for (var instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(HOOK)) {
                throw new IllegalStateException("Already patched " + name);
            }
        }
        return method;
    }

    private static void hit(ClassNode type, String name, String attack) {
        MethodNode method = method(type, name);
        int found = 0;
        for (var instruction : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode call) || !call.name.equals("applyXueliFrost")) continue;
            if (!call.desc.equals("(" + LIVING + ")V")) throw new IllegalStateException("Frost descriptor changed");
            var load = call.getPrevious();
            if (!(load instanceof VarInsnNode variable) || variable.getOpcode() != Opcodes.ALOAD) {
                throw new IllegalStateException("Unexpected victim slot " + name);
            }
            var after = instruction;
            if (attack.equals("pan")) {
                while (after != null && !(after instanceof MethodInsnNode push && push.owner.equals(PREFIX + "util/BossKnockback")
                        && push.name.equals("push"))) after = after.getNext();
                if (after == null) throw new IllegalStateException("Pan knockback changed");
            }
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ALOAD, variable.var));
            hook.add(new LdcInsnNode(attack));
            hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "xueliHit",
                    "(" + LIVING + "Ljava/lang/String;)V", false));
            method.instructions.insert(after, hook);
            found++;
        }
        if (found != 1) throw new IllegalStateException("Expected one victim hook in " + name + ", found " + found);
        int removed = 0;
        for (var instruction : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode call)) continue;
            if (attack.equals("pan") && call.name.equals("m_20254_") && call.desc.equals("(I)V")) {
                InsnList discard = new InsnList();
                discard.add(new InsnNode(Opcodes.POP));
                discard.add(new InsnNode(Opcodes.POP));
                method.instructions.insertBefore(call, discard);
                method.instructions.remove(call);
                removed++;
            }
            if (attack.equals("slam") && call.name.equals("stunPlayer")) {
                InsnList discard = new InsnList();
                for (int argument = 0; argument < 3; argument++) discard.add(new InsnNode(Opcodes.POP));
                method.instructions.insertBefore(call, discard);
                method.instructions.remove(call);
                removed++;
            }
        }
        if ((attack.equals("pan") || attack.equals("slam")) && removed != 1) {
            throw new IllegalStateException("Expected one legacy status replacement " + name + ", found " + removed);
        }
    }

    private static void valor(ClassNode type, String name) {
        MethodNode method = method(type, name);
        InsnList hook = new InsnList();
        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "grantValor", "(" + LIVING + ")V", false));
        method.instructions.insert(hook);
    }

    private static void branch(ClassNode type, String name, int effectSlot, String predicate, String descriptor, boolean apply) {
        MethodNode method = method(type, name);
        InsnList hook = new InsnList();
        hook.add(new VarInsnNode(Opcodes.ALOAD, effectSlot));
        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, predicate, descriptor, false));
        LabelNode original = new LabelNode();
        hook.add(new JumpInsnNode(Opcodes.IFEQ, original));
        if (apply) {
            hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
            hook.add(new VarInsnNode(Opcodes.ALOAD, 2));
            hook.add(new IntInsnNode(Opcodes.SIPUSH, 120));
            hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "apply", "(" + LIVING + EFFECT + "I)Z", false));
        } else {
            hook.add(new InsnNode(Opcodes.ICONST_1));
        }
        hook.add(new InsnNode(Opcodes.IRETURN));
        hook.add(original);
        hook.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(hook);
    }

    public static void main(String[] arguments) throws Exception {
        Path classes = Path.of(arguments[1]);
        Path payload = Path.of(arguments[3]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        try (ZipFile jar = new ZipFile(arguments[0])) {
            ClassNode boss = read(jar, PREFIX + "entity/EasterBossEntity");
            hit(boss, "detonate", "bomb");
            hit(boss, "executeXueliPanSlap", "pan");
            hit(boss, "xueliRollTick", "roller");
            hit(boss, "xueliSlamDown", "slam");
            valor(boss, "skillXueliClones");
            valor(boss, "executePoisonBurst");
            branch(boss, "zicaiAllowed", 0, "sharedHarmful", "(" + EFFECT + ")Z", false);
            branch(boss, "safeAddDebuff", 2, "sharedHarmful", "(" + EFFECT + ")Z", true);
            save(boss, payload);
            ClassNode pot = read(jar, PREFIX + "entity/StockpotEntity");
            hit(pot, "splash", "pot");
            save(pot, payload);
            ClassNode knife = read(jar, PREFIX + "entity/XueliKnifeEntity");
            hit(knife, "cut", "knife");
            save(knife, payload);
            ClassNode heal = read(jar, PREFIX + "boss/ai/HealBan");
            branch(heal, "isBanned", 0, "grievous", "(" + LIVING + ")Z", false);
            save(heal, payload);
        }
        try (var files = Files.walk(classes.resolve(PREFIX))) {
            for (Path file : files.filter(candidate -> candidate.toString().endsWith(".class")).toList()) {
                if (!file.getFileName().toString().startsWith("CombatStatus")) continue;
                ClassWriter writer = new ClassWriter(0);
                new ClassReader(Files.readAllBytes(file)).accept(new ClassRemapper(writer, names), 0);
                Path output = payload.resolve(classes.relativize(file));
                Files.createDirectories(output.getParent());
                Files.write(output, writer.toByteArray());
            }
        }
        System.out.println("PASS surgical shared-status hooks; existing boss bytecode preserved");
    }
}
