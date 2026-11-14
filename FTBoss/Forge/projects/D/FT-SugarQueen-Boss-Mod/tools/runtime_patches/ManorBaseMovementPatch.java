import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
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

public final class ManorBaseMovementPatch {
    public static final String BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity";
    public static final String WALKING = "net/farmingtales/ftboss/boss/ai/BossWalkingSpeed";
    public static final String RULES = "net/farmingtales/ftboss/util/ManorBaseMovementSpeed";
    public static final String EVENT = "net/minecraftforge/event/entity/EntityJoinLevelEvent";
    public static final String LEVEL = "net/minecraft/world/level/Level";
    public static final String ENTITY = "net/minecraft/world/entity/Entity";

    public static ClassNode read(Path jar, String owner) throws Exception {
        try (ZipFile archive = new ZipFile(jar.toFile())) {
            ClassNode type = new ClassNode();
            new ClassReader(archive.getInputStream(archive.getEntry(owner + ".class")).readAllBytes()).accept(type, 0);
            return type;
        }
    }

    public static MethodNode method(ClassNode type, String name) {
        var matches = type.methods.stream().filter(candidate -> candidate.name.equals(name)).toList();
        if (matches.size() != 1) throw new IllegalStateException("Unexpected method count: " + name);
        return matches.get(0);
    }

    public static byte[] bytes(ClassNode type) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        type.accept(writer);
        return writer.toByteArray();
    }

    public static void patch(ClassNode boss, ClassNode walking) {
        MethodNode refresh = method(boss, "refreshWalkingSpeed");
        long hooks = Arrays.stream(refresh.instructions.toArray()).filter(instruction ->
                instruction instanceof MethodInsnNode call && call.owner.equals(RULES)).count();
        if (hooks == 0) {
            var setters = Arrays.stream(refresh.instructions.toArray()).filter(instruction ->
                    instruction instanceof MethodInsnNode call
                    && call.owner.equals("net/minecraft/world/entity/ai/attributes/AttributeInstance")
                    && call.name.equals("m_22100_") && call.desc.equals("(D)V")).toList();
            if (setters.size() != 1) throw new IllegalStateException("Expected one base speed setter");
            InsnList scale = new InsnList();
            scale.add(new VarInsnNode(Opcodes.ALOAD, 0));
            scale.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "isMini", "()Z", false));
            scale.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RULES, "scale", "(DZ)D", false));
            refresh.instructions.insertBefore(setters.get(0), scale);
        } else if (hooks != 1) {
            throw new IllegalStateException("Duplicate base movement hooks");
        }
        refresh.access = (refresh.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
        MethodNode attributes = method(boss, "createAttributes");
        int attributesChanged = 0;
        for (AbstractInsnNode instruction : attributes.instructions) {
            if (instruction instanceof FieldInsnNode field && field.owner.equals("net/minecraft/world/entity/ai/attributes/Attributes")
                    && field.name.equals("f_22279_")) {
                if (!(field.getNext() instanceof LdcInsnNode literal) || !(literal.cst instanceof Double value)
                        || (value != 0.1 && value != 0.13)) {
                    throw new IllegalStateException("Unknown original default movement value");
                }
                literal.cst = 0.13;
                attributesChanged++;
            }
        }
        if (attributesChanged != 1) throw new IllegalStateException("Expected one default movement attribute");
        MethodNode join = method(walking, "onJoin");
        long joins = Arrays.stream(join.instructions.toArray()).filter(instruction ->
                instruction instanceof MethodInsnNode call && call.owner.equals(BOSS)
                && call.name.equals("refreshWalkingSpeed")).count();
        if (joins == 0) {
            LabelNode original = new LabelNode();
            InsnList prefix = new InsnList();
            prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
            prefix.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, EVENT, "getEntity", "()L" + ENTITY + ";", false));
            prefix.add(new TypeInsnNode(Opcodes.INSTANCEOF, BOSS));
            prefix.add(new JumpInsnNode(Opcodes.IFEQ, original));
            prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
            prefix.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, EVENT, "getLevel", "()L" + LEVEL + ";", false));
            prefix.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, LEVEL, "m_5776_", "()Z", false));
            prefix.add(new JumpInsnNode(Opcodes.IFNE, original));
            prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
            prefix.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, EVENT, "getEntity", "()L" + ENTITY + ";", false));
            prefix.add(new TypeInsnNode(Opcodes.CHECKCAST, BOSS));
            prefix.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "isMini", "()Z", false));
            prefix.add(new JumpInsnNode(Opcodes.IFNE, original));
            prefix.add(new VarInsnNode(Opcodes.ALOAD, 0));
            prefix.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, EVENT, "getEntity", "()L" + ENTITY + ";", false));
            prefix.add(new TypeInsnNode(Opcodes.CHECKCAST, BOSS));
            prefix.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, BOSS, "refreshWalkingSpeed", "()V", false));
            prefix.add(new InsnNode(Opcodes.RETURN));
            prefix.add(original);
            prefix.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            join.instructions.insert(prefix);
        } else if (joins != 1) {
            throw new IllegalStateException("Duplicate manor join refresh hooks");
        }
    }

    public static void revert(ClassNode boss, ClassNode walking, ClassNode originalBoss) {
        MethodNode refresh = method(boss, "refreshWalkingSpeed");
        MethodInsnNode scale = Arrays.stream(refresh.instructions.toArray()).filter(instruction ->
                instruction instanceof MethodInsnNode call && call.owner.equals(RULES))
                .map(instruction -> (MethodInsnNode) instruction).findFirst().orElseThrow();
        AbstractInsnNode mini = scale.getPrevious();
        AbstractInsnNode receiver = mini.getPrevious();
        refresh.instructions.remove(receiver);
        refresh.instructions.remove(mini);
        refresh.instructions.remove(scale);
        refresh.access = method(originalBoss, "refreshWalkingSpeed").access;
        for (AbstractInsnNode instruction : method(boss, "createAttributes").instructions) {
            if (instruction instanceof FieldInsnNode field && field.owner.equals("net/minecraft/world/entity/ai/attributes/Attributes")
                    && field.name.equals("f_22279_")) ((LdcInsnNode) field.getNext()).cst = 0.1;
        }
        MethodNode join = method(walking, "onJoin");
        MethodInsnNode call = Arrays.stream(join.instructions.toArray()).filter(instruction ->
                instruction instanceof MethodInsnNode invocation && invocation.owner.equals(BOSS)
                && invocation.name.equals("refreshWalkingSpeed"))
                .map(instruction -> (MethodInsnNode) instruction).findFirst().orElseThrow();
        AbstractInsnNode originalLabel = call.getNext().getNext();
        AbstractInsnNode introducedFrame = originalLabel.getNext();
        while (introducedFrame != null && !(introducedFrame instanceof FrameNode)) introducedFrame = introducedFrame.getNext();
        if (introducedFrame == null) throw new IllegalStateException("Missing join prefix stack frame");
        while (join.instructions.getFirst() != originalLabel) join.instructions.remove(join.instructions.getFirst());
        join.instructions.remove(introducedFrame);
    }

    public static void main(String[] arguments) throws Exception {
        Path baseline = Path.of(arguments[0]);
        Path payload = Path.of(arguments[1]);
        ClassNode boss = read(baseline, BOSS), walking = read(baseline, WALKING);
        patch(boss, walking);
        for (ClassNode type : new ClassNode[]{boss, walking}) {
            Path destination = payload.resolve(type.name + ".class");
            Files.createDirectories(destination.getParent());
            Files.write(destination, bytes(type));
        }
        System.out.println("PASS scoped manor base movement patch; runtime refresh and default plus server join only");
    }
}
