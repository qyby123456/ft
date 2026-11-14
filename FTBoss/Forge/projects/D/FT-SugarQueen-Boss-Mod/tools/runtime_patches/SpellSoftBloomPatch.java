import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class SpellSoftBloomPatch {
    public static final List<String> TARGETS = List.of(
            "net/farmingtales/ftboss/client/render/SpellVisualRenderer",
            "net/farmingtales/ftboss/client/render/XuanlanSpellVisuals",
            "net/farmingtales/ftboss/client/maque/MaqueSpellRenderer",
            "net/farmingtales/ftboss/client/xueli/XueliForgeRenderer",
            "net/farmingtales/ftboss/client/render/ManorDomainVisual",
            "net/farmingtales/ftboss/client/render/ManorAssaultVisual");
    private static final String EVENT = "Lnet/minecraftforge/client/event/RenderLevelStageEvent;";
    private static final String BRIDGE = "net/farmingtales/ftboss/client/render/bloom/SpellBloomRenderer";
    private static final String BODY = "ftboss$renderWithoutSoftBloom";

    private SpellSoftBloomPatch() {}

    public static void main(String[] arguments) throws Exception {
        Path baseline = Path.of(arguments[0]), compiled = Path.of(arguments[1]), payload = Path.of(arguments[2]);
        try (ZipFile archive = new ZipFile(baseline.toFile())) {
            for (String name : TARGETS) {
                var entry = archive.getEntry(name + ".class");
                if (entry == null) throw new IllegalStateException("Missing bloom renderer: " + name);
                ClassNode type = new ClassNode();
                try (var stream = archive.getInputStream(entry)) { new ClassReader(stream.readAllBytes()).accept(type, 0); }
                if (type.methods.stream().noneMatch(method -> method.name.equals(BODY))) wrap(type);
                save(type, payload.resolve(name + ".class"));
                System.out.println("HOOK " + name);
            }
        }
        try (var files = Files.walk(compiled.resolve("net/farmingtales/ftboss/client/render/bloom"))) {
            for (Path source : files.filter(path -> path.toString().endsWith(".class")).toList()) {
                ClassWriter writer = new ClassWriter(0);
                new ClassReader(Files.readAllBytes(source)).accept(new ClassRemapper(writer, runtimeNames()), 0);
                Path output = payload.resolve(compiled.relativize(source));
                Files.createDirectories(output.getParent());
                Files.write(output, writer.toByteArray());
            }
        }
    }

    private static void wrap(ClassNode type) {
        var matches = type.methods.stream().filter(method -> method.name.equals("render") && method.desc.equals("(" + EVENT + ")V")).toList();
        if (matches.size() != 1) throw new IllegalStateException("Unexpected render entry: " + type.name);
        MethodNode wrapper = matches.get(0);
        MethodNode body = new MethodNode(Opcodes.ASM9, wrapper.access, BODY, wrapper.desc, wrapper.signature,
                wrapper.exceptions.toArray(String[]::new));
        wrapper.accept(body);
        body.name = BODY;
        body.access = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC;
        body.visibleAnnotations = body.invisibleAnnotations = null;
        var fields = type.fields.stream().filter(field -> (field.access & Opcodes.ACC_STATIC) != 0
                && (field.name.equals("ACTIVE") || field.name.equals("CASTS"))).toList();
        if (fields.size() != 1) throw new IllegalStateException("Unexpected active collection: " + type.name);
        var active = fields.get(0);
        String collection = active.desc.substring(1, active.desc.length() - 1);
        InsnList code = new InsnList();
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new FieldInsnNode(Opcodes.GETSTATIC, type.name, active.name, active.desc));
        code.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, collection, "isEmpty", "()Z", true));
        code.add(new InsnNode(Opcodes.ICONST_1));
        code.add(new InsnNode(Opcodes.IXOR));
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "begin", "(" + EVENT + "Z)Z", false));
        code.add(new VarInsnNode(Opcodes.ISTORE, 1));
        LabelNode start = new LabelNode(), finish = new LabelNode(), failure = new LabelNode();
        code.add(start);
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, type.name, BODY, wrapper.desc, false));
        code.add(finish);
        code.add(new VarInsnNode(Opcodes.ILOAD, 1));
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "end", "(Z)V", false));
        code.add(new InsnNode(Opcodes.RETURN));
        code.add(failure);
        code.add(new VarInsnNode(Opcodes.ASTORE, 2));
        code.add(new VarInsnNode(Opcodes.ILOAD, 1));
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "end", "(Z)V", false));
        code.add(new VarInsnNode(Opcodes.ALOAD, 2));
        code.add(new InsnNode(Opcodes.ATHROW));
        wrapper.instructions = code;
        wrapper.tryCatchBlocks.clear();
        wrapper.tryCatchBlocks.add(new TryCatchBlockNode(start, finish, failure, null));
        wrapper.localVariables = null;
        wrapper.visibleLocalVariableAnnotations = wrapper.invisibleLocalVariableAnnotations = null;
        wrapper.maxStack = 3;
        wrapper.maxLocals = 3;
        wrapper.instructions.insertBefore(failure, new org.objectweb.asm.tree.FrameNode(Opcodes.F_NEW, 2,
                new Object[]{"net/minecraftforge/client/event/RenderLevelStageEvent", Opcodes.INTEGER}, 1,
                new Object[]{"java/lang/Throwable"}));
        type.methods.add(body);
    }

    private static void save(ClassNode type, Path destination) throws Exception {
        for (MethodNode method : type.methods) {
            if ((method.access & (Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) == 0) new Analyzer<>(new BasicVerifier()).analyze(type.name, method);
        }
        ClassWriter writer = new ClassWriter(0);
        type.accept(writer);
        Files.createDirectories(destination.getParent());
        Files.write(destination, writer.toByteArray());
    }

    private static Remapper runtimeNames() {
        Map<String, String> methods = Map.of(
                "getInstance", "m_91087_", "getResourceManager", "m_91098_", "getMainRenderTarget", "m_91385_", "getResourceOrThrow", "m_215593_",
                "open", "m_215507_", "isDetached", "m_90594_");
        return new Remapper() {
            @Override
            public String mapMethodName(String owner, String name, String descriptor) {
                return owner.startsWith("net/minecraft/") ? methods.getOrDefault(name, name) : name;
            }

            @Override
            public String mapFieldName(String owner, String name, String descriptor) {
                if (owner.equals("com/mojang/blaze3d/pipeline/RenderTarget") && name.equals("frameBufferId")) return "f_83920_";
                return owner.equals("net/minecraft/client/Minecraft") && name.equals("level") ? "f_91073_" : name;
            }
        };
    }
}
