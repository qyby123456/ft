import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.*;

public final class ManorDomainLifetimePatch implements Opcodes {
    private static final String BASE = "net/farmingtales/ftboss/";
    private static final String LIFETIME = BASE + "boss/ai/ManorDomainLifetime";
    private static final String VISUAL = BASE + "client/render/ManorDomainVisual";
    private static final String SHOW = VISUAL + "$Show";
    private static final String PACKET = "L" + BASE + "skill/ManorDomainPacket;";
    private static final String FADE = "(Ljava/util/Map;Ljava/lang/Object;)Ljava/lang/Object;";

    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(name + ".class")).readAllBytes()).accept(type, 0);
        return type;
    }

    private static void save(ClassNode type, Path output) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path file = output.resolve(type.name + ".class");
        Files.createDirectories(file.getParent());
        Files.write(file, writer.toByteArray());
    }

    private static MethodNode fade() {
        MethodNode method = new MethodNode(ACC_PRIVATE | ACC_STATIC, "ftboss$fadeOut", FADE, null, null);
        InsnList code = method.instructions;
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true));
        code.add(new TypeInsnNode(CHECKCAST, SHOW));
        code.add(new VarInsnNode(ASTORE, 2));
        code.add(new VarInsnNode(ALOAD, 2));
        LabelNode done = new LabelNode();
        code.add(new JumpInsnNode(IFNULL, done));
        code.add(new VarInsnNode(ALOAD, 0));
        code.add(new VarInsnNode(ALOAD, 1));
        code.add(new TypeInsnNode(NEW, SHOW));
        code.add(new InsnNode(DUP));
        code.add(new VarInsnNode(ALOAD, 2));
        code.add(new FieldInsnNode(GETFIELD, SHOW, "packet", PACKET));
        code.add(new VarInsnNode(ALOAD, 2));
        code.add(new FieldInsnNode(GETFIELD, SHOW, "started", "J"));
        code.add(new VarInsnNode(ALOAD, 2));
        code.add(new FieldInsnNode(GETFIELD, SHOW, "updated", "J"));
        code.add(new FieldInsnNode(GETSTATIC, VISUAL, "world", "Lnet/minecraft/client/multiplayer/ClientLevel;"));
        code.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/client/multiplayer/ClientLevel", "m_46467_", "()J", false));
        code.add(new LdcInsnNode(40L));
        code.add(new InsnNode(LSUB));
        code.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Math", "min", "(JJ)J", false));
        code.add(new MethodInsnNode(INVOKESPECIAL, SHOW, "<init>", "(" + PACKET + "JJ)V", false));
        code.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true));
        code.add(new InsnNode(POP));
        code.add(done);
        code.add(new FrameNode(F_APPEND, 1, new Object[]{SHOW}, 0, null));
        code.add(new VarInsnNode(ALOAD, 2));
        code.add(new InsnNode(ARETURN));
        return method;
    }

    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[3]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        try (JarFile jar = new JarFile(arguments[0])) {
            ClassNode support = read(jar, BASE + "boss/ai/ManorGroundSupport");
            MethodNode eligible = support.methods.stream().filter(method -> method.name.equals("eligible")).findFirst().orElseThrow();
            int returns = 0;
            for (var instruction : eligible.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals(LIFETIME)) throw new AssertionError("Already patched");
                if (instruction.getOpcode() != IRETURN) continue;
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(ALOAD, 0));
                hook.add(new VarInsnNode(ALOAD, 1));
                hook.add(new MethodInsnNode(INVOKESTATIC, LIFETIME, "eligible", "(ZL" + BASE + "entity/EasterBossEntity;Ljava/util/UUID;)Z", false));
                eligible.instructions.insertBefore(instruction, hook);
                returns++;
            }
            if (returns != 1) throw new AssertionError("Unexpected eligibility returns: " + returns);
            save(support, output);
            ClassNode visual = read(jar, VISUAL);
            if (visual.methods.stream().anyMatch(method -> method.name.equals("ftboss$fadeOut"))) throw new AssertionError("Already patched");
            MethodNode accept = visual.methods.stream().filter(method -> method.name.equals("accept")).findFirst().orElseThrow();
            int removals = 0;
            for (var instruction : accept.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode call) || !call.owner.equals("java/util/Map") || !call.name.equals("remove")) continue;
                accept.instructions.set(call, new MethodInsnNode(INVOKESTATIC, VISUAL, "ftboss$fadeOut", FADE, false));
                removals++;
            }
            if (removals != 1) throw new AssertionError("Unexpected visual removal sites: " + removals);
            visual.methods.add(fade());
            save(visual, output);
        }
        for (String name : new String[]{"util/ManorDomainLifetimeRules", "boss/ai/ManorDomainLifetime"}) {
            ClassWriter writer = new ClassWriter(0);
            new ClassReader(Files.readAllBytes(Path.of(arguments[1]).resolve(BASE + name + ".class")))
                    .accept(new ClassRemapper(writer, names), 0);
            Path file = output.resolve(BASE + name + ".class");
            Files.createDirectories(file.getParent());
            Files.write(file, writer.toByteArray());
        }
        System.out.println("PASS domain eligibility timeout hook and 25-tick ground fade");
    }
}
