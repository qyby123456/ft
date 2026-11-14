package net.farmingtales.ftboss.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

public final class NativeFreshFactoryTrust298 {
    private static volatile Set<String> factoryMethods = Set.of();
    private static volatile String constructorHandler = "";
    private static volatile boolean loaderVerified;

    private NativeFreshFactoryTrust298() { }

    private static boolean caller(Class<?> expected) {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(2).findFirst().map(frame -> frame.getDeclaringClass() == expected && frame.getMethodName().equals("finished") && frame.getDescriptor().equals("(Lorg/spongepowered/asm/mixin/transformer/ext/ITargetClassContext;Z)V")).orElse(false));
    }

    static synchronized void installFactories(ITargetClassContext context, boolean verified) {
        if (!caller(NativeMapFactoryInstallation288.class) || !verified || context == null || NativeMapPutLateProofExtension.current(context.getClassNode()) != context || !"net/minecraft/nbt/CompoundTag".equals(context.getClassNode().name) || !factoryMethods.isEmpty()) throw new IllegalStateException("Untrusted fresh factory installation");
        List<String> handlers = context.getClassNode().methods.stream().filter(method -> method.name.endsWith("$ftboss$observeConstructorMap280") && method.desc.equals("(Ljava/util/Map;)Ljava/util/Map;")).map(method -> method.name).toList();
        Set<String> methods = context.getClassNode().methods.stream().filter(method -> method.name.equals("<init>") && (method.desc.equals("()V") || method.desc.equals("(I)V")) || method.name.equals("shallowCopy") && method.desc.equals("()Lnet/minecraft/nbt/CompoundTag;") || method.name.startsWith("copy$mixinextras$wrapped$") && method.desc.equals("()Lnet/minecraft/nbt/CompoundTag;")).map(method -> method.name + method.desc).collect(Collectors.toUnmodifiableSet());
        if (handlers.size() != 1 || methods.size() != 4) throw new IllegalArgumentException("Fresh factory method set differs");
        constructorHandler = handlers.getFirst();
        factoryMethods = methods;
    }

    static synchronized void installLoader(ITargetClassContext context, boolean verified) {
        if (!caller(NativeLoaderInstallation295.class) || !verified || context == null || NativeMapPutLateProofExtension.current(context.getClassNode()) != context || !"net/minecraft/nbt/CompoundTag$1".equals(context.getClassNode().name) || loaderVerified) throw new IllegalStateException("Untrusted fresh loader installation");
        loaderVerified = true;
    }

    public static boolean freshMap(Object owner, Map<?, ?> backing) {
        if (owner == null || owner.getClass() != CompoundTag.class || backing == null || backing.getClass() != HashMap.class || factoryMethods.isEmpty()) return false;
        try {
            List<StackWalker.StackFrame> frames = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(frame -> frame.getDeclaringClass() != NativeFreshFactoryTrust298.class).limit(3).toList());
            if (frames.size() != 3) return false;
            StackWalker.StackFrame handler = frames.get(0), constructor = frames.get(1), factory = frames.get(2);
            if (handler.getDeclaringClass() != CompoundTag.class || !handler.getMethodName().equals(constructorHandler) || !handler.getDescriptor().equals("(Ljava/util/Map;)Ljava/util/Map;")) return false;
            if (constructor.getDeclaringClass() != CompoundTag.class || !constructor.getMethodName().equals("<init>") || !constructor.getDescriptor().equals("(Ljava/util/Map;)V")) return false;
            if (factory.getDeclaringClass() == CompoundTag.class) {
                boolean trusted = factoryMethods.contains(factory.getMethodName() + factory.getDescriptor());
                if (trusted && factory.getMethodName().equals("<init>") && factory.getDescriptor().equals("()V")) {
                    NativeExpectedFieldObservation.rangedFreshAllocation431(owner, backing);
                }
                return trusted;
            }
            return loaderVerified && factory.getDeclaringClass().getName().equals("net.minecraft.nbt.CompoundTag$1") && factory.getDeclaringClass().getClassLoader() == CompoundTag.class.getClassLoader() && factory.getMethodName().equals("loadCompound") && factory.getDescriptor().equals("(Ljava/io/DataInput;Lnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;");
        } catch (RuntimeException failure) {
            return false;
        }
    }

    static boolean readableMap(Object owner, Map<?, ?> actual) {
        if (owner == null || owner.getClass() != CompoundTag.class || actual == null || actual.getClass() != NativeMutationMap276.class || factoryMethods.isEmpty()) return false;
        if (!(owner instanceof NativeNbtMutationFields fields) || fields.ftboss$nativeNbtRawEscaped() || !((NativeMutationMap276<?, ?>)actual).belongsTo(owner)) return false;
        try {
            java.lang.reflect.Field physical = CompoundTag.class.getField("tags");
            if (physical.getDeclaringClass() != CompoundTag.class || physical.getModifiers() != (java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL) || physical.getType() != Map.class || physical.get(owner) != actual) return false;
            return true;
        } catch (ReflectiveOperationException failure) { return false; }
    }

    public static Map<String, Object> snapshot() { return Map.of("factoryMethods", factoryMethods, "constructorHandler", constructorHandler, "loaderVerified", loaderVerified, "nativeOwnerAuthorityGranted", false); }
}
