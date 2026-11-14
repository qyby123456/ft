import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.*;

public final class DoraClonePatch {
    private static final String ROOT = "net/farmingtales/ftboss/";
    private static final String HOOK = ROOT + "boss/ai/DoraPursuitClone";
    private static final String ENTITY = "net/minecraft/world/entity/Entity";

    private static ClassNode read(ZipFile archive, String name) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(archive.getInputStream(archive.getEntry(ROOT + name + ".class")).readAllBytes()).accept(type, 0);
        for (MethodNode method : type.methods) for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(HOOK)) {
                throw new IllegalStateException("Already patched: " + type.name);
            }
        }
        return type;
    }

    private static MethodNode method(ClassNode type, String name) {
        return type.methods.stream().filter(method -> method.name.equals(name)).reduce((first, second) -> {
            throw new IllegalStateException("Ambiguous method " + name);
        }).orElseThrow();
    }

    private static void save(ClassNode type, Path payload) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path file = payload.resolve(type.name + ".class");
        Files.createDirectories(file.getParent());
        Files.write(file, writer.toByteArray());
    }

    private static void guard(MethodNode method, String hook, String descriptor, int local) {
        InsnList prefix = new InsnList();
        prefix.add(new VarInsnNode(Opcodes.ALOAD, local));
        prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, hook, descriptor, false));
        LabelNode original = new LabelNode();
        prefix.add(new JumpInsnNode(Opcodes.IFEQ, original));
        prefix.add(new InsnNode(Opcodes.RETURN));
        prefix.add(original);
        prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insert(prefix);
    }

    public static void main(String[] arguments) throws Exception {
        Path payload = Path.of(arguments[3]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        try (ZipFile archive = new ZipFile(arguments[0])) {
            ClassNode handler = read(archive, "boss/BossHandler");
            MethodNode constructor = method(handler, "<init>");
            int registrations = 0;
            for (AbstractInsnNode instruction : constructor.instructions.toArray()) {
                if (instruction.getOpcode() != Opcodes.RETURN) continue;
                InsnList registration = new InsnList();
                registration.add(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraftforge/common/MinecraftForge",
                        "EVENT_BUS", "Lnet/minecraftforge/eventbus/api/IEventBus;"));
                registration.add(new LdcInsnNode(org.objectweb.asm.Type.getObjectType(HOOK)));
                registration.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraftforge/eventbus/api/IEventBus",
                        "register", "(Ljava/lang/Object;)V", true));
                constructor.instructions.insertBefore(instruction, registration);
                registrations++;
            }
            if (registrations != 1) throw new AssertionError("Unexpected registration count " + registrations);
            guard(method(handler, "onBossChangeTarget"), "redirectTarget",
                    "(Lnet/minecraftforge/event/entity/living/LivingChangeTargetEvent;)Z", 1);
            save(handler, payload);
            for (String[] entry : new String[][]{{"boss/ai/BossScaling", "tick"},
                    {"boss/ai/ManorHealthBalance", "normalize"}, {"boss/ai/BossMercy", "onBossDefeated"}}) {
                ClassNode type = read(archive, entry[0]);
                guard(method(type, entry[1]), "isClone", "(L" + ENTITY + ";)Z", 0);
                save(type, payload);
            }
            ClassNode fight = read(archive, "boss/ManorFight");
            int recoveries = 0;
            for (AbstractInsnNode instruction : method(fight, "recoverNear").instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals(ROOT + "entity/EasterBossEntity")
                        && call.name.equals("isMini") && call.desc.equals("()Z")) {
                    call.setOpcode(Opcodes.INVOKESTATIC);
                    call.owner = HOOK;
                    call.name = "skipRecovery";
                    call.desc = "(L" + ROOT + "entity/EasterBossEntity;)Z";
                    call.itf = false;
                    recoveries++;
                }
            }
            if (recoveries != 1) throw new AssertionError("Unexpected recovery count " + recoveries);
            save(fight, payload);
        }
        try (var files = Files.walk(Path.of(arguments[1]).resolve(ROOT))) {
            for (Path file : files.filter(file -> file.toString().endsWith(".class")).toList()) {
                ClassNode type = new ClassNode();
                new ClassReader(Files.readAllBytes(file)).accept(new ClassRemapper(type, names), 0);
                save(type, payload);
            }
        }
        System.out.println("PASS Dora clone: explicit registration, target routing, health/reward/recovery guards, SRG remap");
    }
}
