import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.*;

public final class LemonSummonLimitPatch implements Opcodes {
    private static final String BASE = "net/farmingtales/ftboss/";
    private static final String ENTITY = "net/minecraft/world/entity/Entity";
    private static final String EASTER = BASE + "entity/EasterBossEntity";
    private static final String LIMIT = BASE + "boss/ai/LemonSummonLimit";
    private static final String MEMBERS = BASE + "boss/ManorFightMembers";
    private static final String PAIR = "(L" + ENTITY + ";L" + ENTITY + ";)V";

    private static ClassNode read(JarFile jar, String name) throws Exception {
        ClassNode type = new ClassNode();
        new ClassReader(jar.getInputStream(jar.getJarEntry(name + ".class")).readAllBytes()).accept(type, 0);
        return type;
    }

    private static void save(ClassNode type, Path output) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        Path destination = output.resolve(type.name + ".class");
        Files.createDirectories(destination.getParent());
        Files.write(destination, writer.toByteArray());
    }

    public static void main(String[] arguments) throws Exception {
        Path output = Path.of(arguments[3]);
        var names = new ZicaiVoidPatch.Names(Path.of(arguments[2]));
        try (JarFile baseline = new JarFile(arguments[0])) {
            ClassNode members = read(baseline, MEMBERS);
            MethodNode inherit = members.methods.stream().filter(method -> method.name.equals("inherit") && method.desc.equals(PAIR))
                    .findFirst().orElseThrow();
            for (var instruction : inherit.instructions) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals(LIMIT)) throw new AssertionError("Already patched");
            }
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(ALOAD, 0));
            hook.add(new VarInsnNode(ALOAD, 1));
            hook.add(new MethodInsnNode(INVOKESTATIC, LIMIT, "inherit", PAIR, false));
            inherit.instructions.insert(hook);
            save(members, output);
            ClassNode easter = read(baseline, EASTER);
            if (easter.methods.stream().anyMatch(method -> method.name.equals("isLemonDirectSummon"))) throw new AssertionError("Already patched");
            MethodNode accessor = new MethodNode(ACC_PUBLIC, "isLemonDirectSummon", "(L" + ENTITY + ";)Z", null, null);
            LabelNode rejected = new LabelNode();
            accessor.instructions.add(new VarInsnNode(ALOAD, 0));
            accessor.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, EASTER, "getVariant", "()I", false));
            accessor.instructions.add(new InsnNode(ICONST_4));
            accessor.instructions.add(new JumpInsnNode(IF_ICMPNE, rejected));
            accessor.instructions.add(new VarInsnNode(ALOAD, 0));
            accessor.instructions.add(new FieldInsnNode(GETFIELD, EASTER, "summons", "Ljava/util/List;"));
            accessor.instructions.add(new VarInsnNode(ALOAD, 1));
            accessor.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, ENTITY,
                    names.mapMethodName(ENTITY, "getUUID", "()Ljava/util/UUID;"), "()Ljava/util/UUID;", false));
            accessor.instructions.add(new MethodInsnNode(INVOKEINTERFACE, "java/util/List", "contains", "(Ljava/lang/Object;)Z", true));
            accessor.instructions.add(new InsnNode(IRETURN));
            accessor.instructions.add(rejected);
            accessor.instructions.add(new FrameNode(F_SAME, 0, null, 0, null));
            accessor.instructions.add(new InsnNode(ICONST_0));
            accessor.instructions.add(new InsnNode(IRETURN));
            easter.methods.add(accessor);
            save(easter, output);
        }
        if (!Files.exists(Path.of(arguments[1]).resolve(LIMIT + ".class"))) return;
        for (String name : new String[]{"util/LemonBalanceRules", "boss/ai/LemonSummonLimit"}) {
            ClassWriter writer = new ClassWriter(0);
            new ClassReader(Files.readAllBytes(Path.of(arguments[1]).resolve(BASE + name + ".class")))
                    .accept(new ClassRemapper(writer, names), 0);
            Path destination = output.resolve(BASE + name + ".class");
            Files.createDirectories(destination.getParent());
            Files.write(destination, writer.toByteArray());
        }
        System.out.println("PASS targeted Lemon provenance hook and direct-summon accessor; compiled rules remapped");
    }
}
