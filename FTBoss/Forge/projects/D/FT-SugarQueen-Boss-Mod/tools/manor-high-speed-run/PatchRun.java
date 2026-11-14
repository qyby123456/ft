import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class PatchRun {
    public static void main(String[] arguments) throws Exception {
        String target = "net/farmingtales/ftboss/client/model/EasterBossModel.class";
        ClassNode model = new ClassNode();
        try (ZipFile zip = new ZipFile(arguments[0])) {
            new ClassReader(zip.getInputStream(zip.getEntry(target)).readAllBytes()).accept(model, 0);
        }
        int inserted = 0;
        for (MethodNode method : model.methods) {
            if (!method.name.equals("setupAnim") || !method.desc.equals("(Lnet/farmingtales/ftboss/entity/EasterBossEntity;FFFFF)V")) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals("net/farmingtales/ftboss/client/manorrun/ManorRunAnimation")) throw new IllegalStateException("Already patched");
                if (instruction.getOpcode() == Opcodes.RETURN) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    hook.add(new VarInsnNode(Opcodes.FLOAD, 4));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "net/farmingtales/ftboss/client/manorrun/ManorRunAnimation", "apply",
                            "(Lnet/farmingtales/ftboss/client/model/EasterBossModel;Lnet/farmingtales/ftboss/entity/EasterBossEntity;F)V", false));
                    method.instructions.insertBefore(instruction, hook);
                    inserted++;
                }
            }
            new Analyzer<>(new BasicVerifier()).analyze(model.name, method);
        }
        if (inserted != 1) throw new IllegalStateException("Expected one base hook, found " + inserted);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        model.accept(writer);
        Path output = Path.of(arguments[1], target);
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
        String rendererTarget = "net/farmingtales/ftboss/client/render/GongdiHumanoidRenderer.class";
        ClassNode renderer = new ClassNode();
        try (ZipFile zip = new ZipFile(arguments[0])) {
            new ClassReader(zip.getInputStream(zip.getEntry(rendererTarget)).readAllBytes()).accept(renderer, 0);
        }
        int customHooks = 0;
        for (MethodNode method : renderer.methods) {
            if (!method.name.equals("setup")) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode call && call.owner.equals("net/farmingtales/ftboss/client/manorrun/GongdiRunFrame")) throw new IllegalStateException("Already patched renderer");
                if (instruction instanceof MethodInsnNode call && call.owner.endsWith("GongdiHumanoidTimeline") && call.name.equals("select")) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD,1));
                    hook.add(new VarInsnNode(Opcodes.FLOAD,4));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"net/farmingtales/ftboss/client/manorrun/GongdiRunFrame","apply",
                        "(Lnet/farmingtales/ftboss/client/render/GongdiBasicAttackSampler$Frame;Lnet/farmingtales/ftboss/entity/EasterBossEntity;F)Lnet/farmingtales/ftboss/client/render/GongdiBasicAttackSampler$Frame;",false));
                    method.instructions.insert(instruction,hook);
                    customHooks++;
                }
            }
            new Analyzer<>(new BasicVerifier()).analyze(renderer.name,method);
        }
        if (customHooks != 1) throw new IllegalStateException("Expected custom rig hook, found " + customHooks);
        ClassWriter rendererWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        renderer.accept(rendererWriter);
        Path rendererOutput = Path.of(arguments[1],rendererTarget);
        Files.createDirectories(rendererOutput.getParent());
        Files.write(rendererOutput,rendererWriter.toByteArray());
        String legacyTarget = "net/farmingtales/ftboss/client/manorrun/RunAnimator.class";
        try (ZipFile zip = new ZipFile(arguments[0])) {
            var entry = zip.getEntry(legacyTarget);
            if (entry != null) {
                ClassNode legacy = new ClassNode();
                new ClassReader(zip.getInputStream(entry).readAllBytes()).accept(legacy, 0);
                int gates = 0;
                for (MethodNode method : legacy.methods) {
                    if (!method.name.equals("prepare") && !method.name.equals("apply")) continue;
                    InsnList gate = new InsnList();
                    LabelNode fallback = new LabelNode();
                    gate.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    gate.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/farmingtales/ftboss/entity/EasterBossEntity", "getVariant", "()I", false));
                    gate.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/farmingtales/ftboss/entity/EasterBossEntity", "isManorVariant", "(I)Z", false));
                    gate.add(new JumpInsnNode(Opcodes.IFEQ, fallback));
                    gate.add(new InsnNode(Opcodes.RETURN));
                    gate.add(fallback);
                    gate.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                    method.instructions.insert(gate);
                    new Analyzer<>(new BasicVerifier()).analyze(legacy.name, method);
                    gates++;
                }
                if (gates != 2) throw new IllegalStateException("Expected two legacy entry gates");
                ClassWriter legacyWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                legacy.accept(legacyWriter);
                Path legacyOutput = Path.of(arguments[1], legacyTarget);
                Files.createDirectories(legacyOutput.getParent());
                Files.write(legacyOutput, legacyWriter.toByteArray());
            }
        }
        System.out.println("Two method-level locomotion injections PASS; all downstream action hooks retained");
    }
}
