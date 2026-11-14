import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class GongdiAnimationPriorityPatch implements Opcodes {
    private static final String RENDER = "net/farmingtales/ftboss/client/render/";
    private static final String ENTITY = "net/farmingtales/ftboss/entity/EasterBossEntity";
    private static final String PRIORITY = RENDER + "GongdiAnimationPriority";

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static ClassNode read(ZipFile archive, String name) throws Exception {
        ClassNode result = new ClassNode();
        new ClassReader(archive.getInputStream(archive.getEntry(name + ".class")).readAllBytes()).accept(result, 0);
        return result;
    }

    private static AbstractInsnNode next(AbstractInsnNode instruction) {
        AbstractInsnNode result = instruction.getNext();
        while (result != null && result.getOpcode() < 0) result = result.getNext();
        return result;
    }

    private static void save(ClassNode node, Path payload) throws Exception {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        Path output = payload.resolve(node.name + ".class");
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
    }

    private static MethodNode method(ClassNode owner, String name) {
        List<MethodNode> matches = owner.methods.stream().filter(method -> method.name.equals(name)).toList();
        require(matches.size() == 1, "Expected exactly one " + owner.name + "." + name);
        return matches.get(0);
    }

    private static InsnList actionState() {
        InsnList result = new InsnList();
        result.add(new VarInsnNode(ALOAD, 1));
        result.add(new MethodInsnNode(INVOKEVIRTUAL, ENTITY, "isCastingLike", "()Z", false));
        result.add(new VarInsnNode(ALOAD, 1));
        result.add(new VarInsnNode(FLOAD, 7));
        result.add(new MethodInsnNode(INVOKEVIRTUAL, ENTITY, "getCombatAnimationWeight", "(F)F", false));
        return result;
    }

    private static void patchRenderer(ClassNode renderer) {
        MethodNode setup = method(renderer, "setup");
        int nativeChanges = 0;
        int spellChanges = 0;
        for (AbstractInsnNode instruction : setup.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(PRIORITY)) {
                throw new IllegalStateException("Priority patch already installed");
            }
            if (instruction instanceof VarInsnNode store && store.getOpcode() == ISTORE && store.var == 11) {
                InsnList filter = new InsnList();
                filter.add(new VarInsnNode(ALOAD, 8));
                filter.add(new LdcInsnNode("released"));
                filter.add(new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/nbt/CompoundTag", "m_128471_",
                        "(Ljava/lang/String;)Z", false));
                filter.add(actionState());
                filter.add(new MethodInsnNode(INVOKESTATIC, PRIORITY, "nativeCue", "(ZZZF)Z", false));
                setup.instructions.insertBefore(store, filter);
                nativeChanges++;
            }
            if (instruction instanceof MethodInsnNode call && call.owner.equals(RENDER + "GongdiSpellCues")
                    && call.name.equals("current")) {
                AbstractInsnNode store = next(call);
                require(store instanceof VarInsnNode variable && store.getOpcode() == ASTORE && variable.var == 12,
                        "Unexpected spell pose local");
                InsnList filter = actionState();
                filter.add(new MethodInsnNode(INVOKESTATIC, PRIORITY, "spellPose",
                        "(Ljava/lang/Object;ZF)Ljava/lang/Object;", false));
                filter.add(new TypeInsnNode(CHECKCAST, RENDER + "GongdiSpellCues$Pose"));
                setup.instructions.insert(call, filter);
                spellChanges++;
            }
        }
        require(nativeChanges == 1 && spellChanges == 1, "Unexpected renderer layout");
    }

    private static void arbitrationFixture(ClassNode renderer, Path fixtures) throws Exception {
        MethodNode setup = method(renderer, "setup");
        ClassNode fixture = new ClassNode();
        fixture.version = V17;
        fixture.access = ACC_PUBLIC;
        fixture.name = "GongdiAnimationArbitrationFixture";
        fixture.superName = "java/lang/Object";
        MethodNode test = new MethodNode(ACC_PUBLIC | ACC_STATIC, "arbitrate",
                setup.desc.substring(0, setup.desc.length() - 1) + "L" + RENDER + "GongdiHumanoidTimeline$Cue;", null, null);
        boolean copying = false;
        boolean finished = false;
        java.util.Map<org.objectweb.asm.tree.LabelNode, org.objectweb.asm.tree.LabelNode> labels = new java.util.HashMap<>();
        for (AbstractInsnNode instruction : setup.instructions) {
            if (instruction instanceof org.objectweb.asm.tree.LabelNode label) labels.put(label, new org.objectweb.asm.tree.LabelNode());
        }
        for (AbstractInsnNode instruction : setup.instructions) {
            if (!copying && instruction.getOpcode() == FCONST_0) copying = true;
            if (!copying || instruction instanceof org.objectweb.asm.tree.FrameNode
                    || instruction instanceof org.objectweb.asm.tree.LineNumberNode) continue;
            if (instruction.getOpcode() == RETURN) {
                test.instructions.add(new InsnNode(ACONST_NULL));
                test.instructions.add(new InsnNode(ARETURN));
            } else {
                test.instructions.add(instruction.clone(labels));
            }
            if (instruction instanceof MethodInsnNode call && call.owner.equals(RENDER + "GongdiHumanoidTimeline$Cue")
                    && call.name.equals("<init>")) {
                test.instructions.add(new InsnNode(ARETURN));
                finished = true;
                break;
            }
        }
        require(finished, "Renderer arbitration fixture incomplete");
        fixture.methods.add(test);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            @Override
            protected String getCommonSuperClass(String first, String second) {
                if (first.equals(second)) return first;
                throw new IllegalStateException("Unexpected fixture type merge: " + first + " / " + second);
            }
        };
        fixture.accept(writer);
        Files.createDirectories(fixtures);
        Files.write(fixtures.resolve(fixture.name + ".class"), writer.toByteArray());
    }

    public static void main(String[] arguments) throws Exception {
        Path jar = Path.of(arguments[1]);
        if (arguments[0].equals("patch")) {
            try (ZipFile archive = new ZipFile(jar.toFile())) {
                ClassNode timeline = read(archive, RENDER + "GongdiHumanoidTimeline");
                ClassNode renderer = read(archive, RENDER + "GongdiHumanoidRenderer");
                patchTimeline(timeline);
                patchRenderer(renderer);
                save(timeline, Path.of(arguments[2]));
                save(renderer, Path.of(arguments[2]));
            }
            System.out.println("PASS: restored ordinary attack weight and filtered residual skill poses");
        } else if (arguments[0].equals("fixture")) {
            try (ZipFile archive = new ZipFile(jar.toFile())) {
                arbitrationFixture(read(archive, RENDER + "GongdiHumanoidRenderer"), Path.of(arguments[2]));
            }
        } else if (arguments[0].equals("verify")) {
            try (ZipFile archive = new ZipFile(jar.toFile()); ZipFile baseline = new ZipFile(Path.of(arguments[2]).toFile())) {
                for (String name : List.of("GongdiHumanoidTimeline", "GongdiHumanoidRenderer")) {
                    ClassNode candidate = read(archive, RENDER + name);
                    ClassNode original = read(baseline, RENDER + name);
                    String changedMethod = name.equals("GongdiHumanoidTimeline") ? "select" : "setup";
                    require(candidate.methods.size() == original.methods.size(), "Method set changed");
                    for (MethodNode method : candidate.methods) {
                        new Analyzer<>(new BasicVerifier()).analyze(candidate.name, method);
                        MethodNode previous = original.methods.stream().filter(other -> other.name.equals(method.name)
                                && other.desc.equals(method.desc)).findFirst().orElseThrow();
                        if (!method.name.equals(changedMethod)) {
                            require(java.util.Arrays.equals(methodBytes(method), methodBytes(previous)),
                                    "Unrelated method changed: " + candidate.name + "." + method.name);
                        }
                    }
                }
            }
            System.out.println("PASS: both renderer classes pass bytecode analysis; only setup/select methods changed");
        } else {
            throw new IllegalArgumentException("Use patch, fixture or verify");
        }
    }

    private static byte[] methodBytes(MethodNode method) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(V17, ACC_PUBLIC, "MethodComparison", null, "java/lang/Object", null);
        method.accept(writer);
        writer.visitEnd();
        return writer.toByteArray();
    }

    private static void patchTimeline(ClassNode timeline) {
        MethodNode select = method(timeline, "select");
        int repaired = 0;
        for (AbstractInsnNode instruction : select.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode call) || !call.owner.equals(RENDER + "GongdiHumanoidTimeline$Cue")
                    || !call.name.equals("actionWeight") || !call.desc.equals("()F")) continue;
            AbstractInsnNode discarded = next(call);
            AbstractInsnNode zero = next(discarded);
            require(discarded.getOpcode() == POP && zero.getOpcode() == FCONST_0,
                    "Unknown actionWeight implementation; refusing another baseline");
            select.instructions.remove(discarded);
            select.instructions.remove(zero);
            repaired++;
        }
        require(repaired == 1, "Expected one disabled ordinary-attack weight");
    }
}
