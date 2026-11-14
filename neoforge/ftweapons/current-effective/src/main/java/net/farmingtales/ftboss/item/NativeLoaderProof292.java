package net.farmingtales.ftboss.item;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceClassVisitor;

public final class NativeLoaderProof292 implements Opcodes {
    private static final String HOST = "net/minecraft/nbt/CompoundTag$1";
    private static final String HASH = "f05fa08db790436311050d87471954ab47cf94fde75a936aa73d833c7955f4ea";

    private NativeLoaderProof292() { }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }

    private static String canonical(ClassNode node) {
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        StringWriter text = new StringWriter();
        new ClassReader(writer.toByteArray()).accept(new TraceClassVisitor(new PrintWriter(text)), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return text.toString();
    }

    public static Map<String, Object> verify(byte[] pinnedSource, ClassNode actual) throws Exception {
        String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(pinnedSource));
        require(HASH.equals(hash), "Loader source pin differs");
        ClassNode source = new ClassNode();
        new ClassReader(pinnedSource).accept(source, 0);
        require(actual != null && HOST.equals(actual.name), "Wrong loader host");
        require(canonical(source).equals(canonical(actual)), "Actual complete loader class differs from pinned source");
        List<MethodNode> factories = actual.methods.stream().filter(method -> method.name.equals("loadCompound") && method.desc.equals("(Ljava/io/DataInput;Lnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;")).toList();
        require(factories.size() == 1, "Loader factory count differs");
        MethodNode factory = factories.getFirst();
        require(factory.access == (ACC_PRIVATE | ACC_STATIC), "Loader factory access differs");
        int freshMaps = 0;
        int constructors = 0;
        int writes = 0;
        for (AbstractInsnNode instruction : factory.instructions) {
            if (!(instruction instanceof MethodInsnNode call)) continue;
            if (call.getOpcode() == INVOKESTATIC && call.owner.equals("com/google/common/collect/Maps") && call.name.equals("newHashMap") && call.desc.equals("()Ljava/util/HashMap;")) freshMaps++;
            if (call.getOpcode() == INVOKESPECIAL && call.owner.equals("net/minecraft/nbt/CompoundTag") && call.name.equals("<init>") && call.desc.equals("(Ljava/util/Map;)V")) constructors++;
            if (call.getOpcode() == INVOKEINTERFACE && call.owner.equals("java/util/Map") && call.name.equals("put") && call.desc.equals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")) writes++;
        }
        require(freshMaps == 1 && constructors == 1 && writes == 1, "Pinned loader allocation pattern differs");
        return Map.of("loaderHost", HOST, "sourceSha256", hash, "wholeClassExactIncludingExceptionRoutes", true, "methodCount", actual.methods.size(), "freshMapSites", freshMaps, "compoundConstructorSites", constructors, "mapWriteSites", writes, "runtimeInstallationCertified", false, "factoryAuthorityGranted", false);
    }
}
