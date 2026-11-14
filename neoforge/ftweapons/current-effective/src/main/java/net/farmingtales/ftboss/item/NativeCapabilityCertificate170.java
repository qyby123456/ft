package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

final class NativeCapabilityCertificate170 {
    private static final ThreadLocal<Map<ITargetClassContext, ClassNode>> PENDING = ThreadLocal.withInitial(IdentityHashMap::new);
    private static final ClassNode[] TARGETS = new ClassNode[3];
    private static final ClassNode[] PRISTINE = new ClassNode[3];
    private static final List<Map<String, IMixinInfo>> SOURCES = new ArrayList<>(java.util.Arrays.asList(null, null, null));
    private static final List<List<IExtension>> ORDERS = new ArrayList<>(java.util.Arrays.asList(null, null, null));
    private static final Thread[] THREADS = new Thread[3];
    private static final ClassLoader[] LOADERS = new ClassLoader[3];
    private static final IMixinConfig[] CONFIGS = new IMixinConfig[3];
    private static final IMixinConfigPlugin[] PLUGINS = new IMixinConfigPlugin[3];
    private static final String[] FAILURES = new String[3];
    private NativeCapabilityCertificate170() {
    }
    static void begin(ITargetClassContext context) {
        var caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().orElseThrow());
        if (context == null || caller.getDeclaringClass() != NativeMapPutLateProofExtension.class || !caller.getMethodName().equals("preApply") || NativeMapPutLateProofExtension.current(context.getClassNode()) != context || NativeCapabilityInstallationProof170.host(context.getClassNode()) < 0 || NativeMapPutLateProofExtension.order() == null || PENDING.get().containsKey(context)) throw new IllegalStateException("Untrusted native capability preimage context");
        PENDING.get().put(context, NativeCapabilityInstallationProof170.copy(context.getClassNode()));
    }
    private static boolean source(IMixinInfo info, String family, ClassNode target, IMixinConfigPlugin plugin, IMixinConfig config) {
        try {
            ClassLoader loader = IMixinInfo.class.getClassLoader();
            if (info == null || info.getClass() != Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo", false, loader) || !info.getClassName().equals("net.farmingtales.ftboss.mixin." + family) || info.getPriority() != 1000 || info.getConfig() == null || info.getConfig().getClass() != Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig", false, loader)) return false;
            IMixinConfig actual = info.getConfig();String name = target.name.replace('/', '.');
            return (config == null || actual == config) && actual.isRequired() && actual.getName().equals("ftboss.owner-wear.mixins.json") && actual.getMixinPackage().equals("net.farmingtales.ftboss.mixin.") && actual.getPlugin() == plugin && plugin != null && plugin.getClass().getName().equals("net.farmingtales.ftboss.mixin.NativeOptionalMixinPlugin") && NativeMapPutLateProofExtension.registeredPlugin(plugin.getClass()) && info.getTargetClasses().stream().anyMatch(value -> value.replace('/', '.').equals(name)) && actual.getTargets().stream().anyMatch(value -> value.replace('/', '.').equals(name));
        } catch (ClassNotFoundException failure) { return false; }
    }
    static synchronized void finish(ITargetClassContext context, IMixinConfigPlugin plugin) {
        var caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().orElseThrow());
        if (context == null || caller.getDeclaringClass() != OwnerFieldCoverageRegistry.class || !caller.getMethodName().equals("nativeCapabilityFinished170") || !caller.getDescriptor().equals("(Lorg/spongepowered/asm/mixin/transformer/ext/ITargetClassContext;)V") || NativeMapPutLateProofExtension.current(context.getClassNode()) != context || NativeMapPutLateProofExtension.order() == null) throw new IllegalStateException("Untrusted native capability final-stage delegation");
        ClassNode target = context.getClassNode();int slot = NativeCapabilityInstallationProof170.host(target);
        if (slot < 0) throw new IllegalStateException("Unsupported native capability proof target");
        TARGETS[slot] = null;PRISTINE[slot] = null;SOURCES.set(slot, null);ORDERS.set(slot, null);THREADS[slot] = null;LOADERS[slot] = null;CONFIGS[slot] = null;PLUGINS[slot] = null;FAILURES[slot] = null;
        Map<ITargetClassContext, ClassNode> pending = PENDING.get();ClassNode pristine = pending.remove(context);if (pending.isEmpty()) PENDING.remove();
        try {
            if (pristine == null || NativeMapPutLateProofExtension.order() == null) throw new IllegalStateException("Missing same real native preimage and final order");
            Map<String, IMixinInfo> observed = new LinkedHashMap<>();
            for (var pair : com.llamalad7.mixinextras.utils.MixinInternals.getMixinsFor(context)) for (String family : NativeCapabilityInstallationProof170.required(target)) if (pair.getLeft().getClassName().equals("net.farmingtales.ftboss.mixin." + family)) {
                if (observed.put(family, pair.getLeft()) != null) throw new IllegalStateException("Duplicate genuine native capability source");
            }
            if (!observed.keySet().equals(Set.copyOf(NativeCapabilityInstallationProof170.required(target)))) throw new IllegalStateException("Incomplete real native capability source set");
            IMixinConfig config = observed.values().iterator().next().getConfig();Map<String, ClassNode> nodes = new LinkedHashMap<>();
            for (var entry : observed.entrySet()) {
                if (!source(entry.getValue(), entry.getKey(), target, plugin, config)) throw new IllegalStateException("Untrusted native capability source/config/plugin identity: " + entry.getKey());nodes.put(entry.getKey(), entry.getValue().getClassNode(0));
            }
            NativeCapabilityInstallationProof170.verify(target, nodes, pristine);
            TARGETS[slot] = target;PRISTINE[slot] = pristine;SOURCES.set(slot, Map.copyOf(observed));ORDERS.set(slot, NativeMapPutLateProofExtension.order());THREADS[slot] = Thread.currentThread();LOADERS[slot] = plugin.getClass().getClassLoader();CONFIGS[slot] = config;PLUGINS[slot] = plugin;
        } catch (RuntimeException failure) { FAILURES[slot] = failure.toString(); }
    }
    static synchronized boolean complete(IMixinConfigPlugin plugin) {
        if (plugin == null || !NativeMapPutLateProofExtension.registeredPlugin(plugin.getClass())) return false;
        for (int slot = 0; slot < 3; slot++) {
            if (TARGETS[slot] == null || PRISTINE[slot] == null || SOURCES.get(slot) == null || ORDERS.get(slot) == null || ORDERS.get(slot) != NativeMapPutLateProofExtension.order() || THREADS[slot] == null || LOADERS[slot] != plugin.getClass().getClassLoader() || CONFIGS[slot] == null || PLUGINS[slot] != plugin) return false;
            Map<String, ClassNode> nodes = new LinkedHashMap<>();
            for (var entry : SOURCES.get(slot).entrySet()) {
                if (!source(entry.getValue(), entry.getKey(), TARGETS[slot], plugin, CONFIGS[slot])) return false;nodes.put(entry.getKey(), entry.getValue().getClassNode(0));
            }
            try { NativeCapabilityInstallationProof170.verify(TARGETS[slot], nodes, PRISTINE[slot]); } catch (RuntimeException failure) { return false; }
        }
        return true;
    }
}
