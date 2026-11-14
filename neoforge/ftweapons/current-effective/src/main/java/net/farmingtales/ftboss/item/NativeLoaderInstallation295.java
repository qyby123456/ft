package net.farmingtales.ftboss.item;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

public final class NativeLoaderInstallation295 {
    private static volatile Map<String, Object> result = Map.of("finalContextObserved", false, "runtimeFactoryAuthorityGranted", false);

    private NativeLoaderInstallation295() { }

    static synchronized void finished(ITargetClassContext context, boolean trustedFinalContext) {
        boolean caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1).findFirst().map(frame -> frame.getDeclaringClass() == NativeMapPutLateProofExtension.class && frame.getMethodName().equals("postApply") && frame.getDescriptor().equals("(Lorg/spongepowered/asm/mixin/transformer/ext/ITargetClassContext;)V")).orElse(false));
        if (!caller || !trustedFinalContext || context == null || NativeMapPutLateProofExtension.current(context.getClassNode()) != context || !"net/minecraft/nbt/CompoundTag$1".equals(context.getClassNode().name)) throw new IllegalStateException("Untrusted native loader final context");
        Map<String, Object> observed = new LinkedHashMap<>();
        observed.put("finalContextObserved", true);
        try (InputStream input = NativeLoaderInstallation295.class.getClassLoader().getResourceAsStream("net/minecraft/nbt/CompoundTag$1.class")) {
            if (input == null) throw new IllegalArgumentException("Missing pinned loader source");
            observed.put("actualFinalLoaderProof", NativeLoaderProof292.verify(input.readAllBytes(), context.getClassNode()));
            observed.put("actualLoaderFinalContextVerified", true);
            NativeFreshFactoryTrust298.installLoader(context, trustedFinalContext);
            observed.put("failures", 0);
        } catch (Exception failure) {
            observed.put("actualLoaderFinalContextVerified", false);
            observed.put("failures", 1);
            observed.put("failure", failure.toString());
        }
        observed.put("runtimeFactoryAuthorityGranted", Integer.valueOf(0).equals(observed.get("failures")));
        observed.put("allConstructorsRemainRawEscaped", !Integer.valueOf(0).equals(observed.get("failures")));
        observed.put("nativeOwnerAuthorityGranted", false);
        result = Map.copyOf(observed);
    }

    public static Map<String, Object> snapshot() { return result; }
}
