package net.farmingtales.ftboss.boss.ai;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;

public final class NativeBossLivingComposerSupportV2 {
    private NativeBossLivingComposerSupportV2() {
    }

    public static String fingerprint(ClassNode source) {
        ClassWriter writer = new ClassWriter(0);
        source.accept(writer);
        ClassNode canonical = new ClassNode();
        new ClassReader(writer.toByteArray()).accept(canonical, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        for (MethodNode method : canonical.methods) {
            method.maxStack = 0;
            method.maxLocals = 0;
        }
        StringWriter text = new StringWriter();
        canonical.accept(new TraceClassVisitor(new PrintWriter(text)));
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException failure) {
            throw new IllegalStateException(failure);
        }
    }

    public static String methodFingerprint(MethodNode source) {
        ClassNode owner = new ClassNode();
        owner.version = Opcodes.V21;
        owner.access = Opcodes.ACC_PUBLIC;
        owner.name = "ftboss/FullMethodFingerprint";
        owner.superName = "java/lang/Object";
        owner.methods.add(source);
        return fingerprint(owner);
    }

    public static MethodNode copy(MethodNode source) {
        MethodNode result = new MethodNode(source.access, source.name, source.desc, source.signature, source.exceptions.toArray(String[]::new));
        source.accept(result);
        for (AbstractInsnNode instruction : result.instructions) {
            if (instruction instanceof InvokeDynamicInsnNode dynamic) {
                dynamic.bsmArgs = dynamic.bsmArgs.clone();
            }
        }
        return result;
    }
}
