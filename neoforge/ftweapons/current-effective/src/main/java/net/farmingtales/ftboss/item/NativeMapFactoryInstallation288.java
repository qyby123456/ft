package net.farmingtales.ftboss.item;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

public final class NativeMapFactoryInstallation288 {
    private static volatile Map<String, Object> result = Map.of("finalContextObserved", false, "runtimeFactoryAuthorityGranted", false);

    private NativeMapFactoryInstallation288() { }

    private static byte[] resource(String name) throws Exception {
        try (InputStream input = NativeMapFactoryInstallation288.class.getClassLoader().getResourceAsStream(name)) {
            if (input == null) throw new IllegalArgumentException("Missing original source " + name);
            return input.readAllBytes();
        }
    }

    static synchronized void finished(ITargetClassContext context, boolean trustedFinalContext) {
        boolean caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1).findFirst().map(frame -> frame.getDeclaringClass() == NativeMapPutLateProofExtension.class && frame.getMethodName().equals("postApply") && frame.getDescriptor().equals("(Lorg/spongepowered/asm/mixin/transformer/ext/ITargetClassContext;)V")).orElse(false));
        if (!caller || !trustedFinalContext || context == null || NativeMapPutLateProofExtension.current(context.getClassNode()) != context || !"net/minecraft/nbt/CompoundTag".equals(context.getClassNode().name)) throw new IllegalStateException("Untrusted native factory final context");
        Map<String, Object> observed = new LinkedHashMap<>();
        observed.put("finalContextObserved", true);
        try {
            observed.put("actualFinalHostProof", NativeMapFactoryProof287.verify(resource("net/minecraft/nbt/CompoundTag.class"), resource("net/minecraft/nbt/CompoundTag$1.class"), resource("net/farmingtales/ftboss/mixin/NativeCompoundTagMutationVersionMixin.class"), context.getClassNode()));
            NativeFreshFactoryTrust298.installFactories(context, trustedFinalContext);
            observed.put("failures", 0);
        } catch (Exception failure) {
            observed.put("failures", 1); observed.put("failure", failure.toString());
        }
        observed.put("runtimeFactoryAuthorityGranted", Integer.valueOf(0).equals(observed.get("failures")));
        observed.put("allConstructorsRemainRawEscaped", !Integer.valueOf(0).equals(observed.get("failures")));
        observed.put("runtimeLoaderClassStillNotCertified", true);
        observed.put("nativeOwnerAuthorityGranted", false);
        result = Map.copyOf(observed);
    }

    public static Map<String, Object> snapshot() { return result; }
}
