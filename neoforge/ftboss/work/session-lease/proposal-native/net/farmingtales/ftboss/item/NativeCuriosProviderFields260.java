package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.item.Item;

public final class NativeCuriosProviderFields260 extends ConcurrentHashMap<Object, Object> {
    private final java.util.concurrent.atomic.AtomicLong rawMutationVersion = new java.util.concurrent.atomic.AtomicLong();
    private volatile boolean rawWritableAlias;
    private volatile boolean rawObservationFailed;
    private static final int LIMIT = 65536;
    private static final Map<Object, State> STATES = new IdentityHashMap<>();
    private static final Map<Object, Long> REPAIR_REGISTRY_VERSIONS = new IdentityHashMap<>();
    private static final Map<Item, List<WeakReference<net.minecraft.world.item.ItemStack>>> REPAIR_OWNERS = new IdentityHashMap<>();
    private static long rejected;

    private NativeCuriosProviderFields260() {
    }

    public static ConcurrentHashMap<Object, Object> originalRegistry() {
        List<StackWalker.StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(2L).toList());
        if (frames.size() != 2 || !frames.get(0).getDeclaringClass().getName().equals("top.theillusivec4.curios.mixin.CuriosImplMixinHooks")
            || frames.get(0).getDeclaringClass().getClassLoader() != NativeCuriosProviderFields260.class.getClassLoader()
            || !frames.get(0).getMethodName().endsWith("$ftboss$trackedOriginalRegistry")
            || frames.get(1).getDeclaringClass() != frames.get(0).getDeclaringClass()
            || !frames.get(1).getMethodName().equals("<clinit>")) throw new SecurityException("Original Curios registry initialization required");
        return new NativeCuriosProviderFields260();
    }

    private void rawChanging(Object key, boolean all) {
        rawMutationVersion.updateAndGet(value -> value == Long.MAX_VALUE ? Long.MAX_VALUE : value + 1L);
        List<net.minecraft.world.item.ItemStack> owners = new ArrayList<>();
        synchronized (NativeCuriosProviderFields260.class) {
            for (var entry : REPAIR_OWNERS.entrySet()) {
                if (!all && entry.getKey() != key) continue;
                entry.getValue().removeIf(owner -> owner.get() == null);
                for (var reference : entry.getValue()) {
                    var owner = reference.get();
                    if (owner != null) owners.add(owner);
                }
            }
        }
        for (var owner : owners) {
            try {
                OwnerFullRepairGuard.changed(owner, 20);
            } catch (RuntimeException | LinkageError failure) {
                rawObservationFailed = true;
            }
        }
    }

    @Override
    public Object put(Object key, Object value) {
        rawChanging(key, false);
        return super.put(key, value);
    }

    @Override
    public Object putIfAbsent(Object key, Object value) {
        rawChanging(key, false);
        return super.putIfAbsent(key, value);
    }

    @Override
    public void putAll(Map<?, ?> values) {
        rawChanging(null, true);
        super.putAll(values);
    }

    @Override
    public Object remove(Object key) {
        rawChanging(key, false);
        return super.remove(key);
    }

    @Override
    public boolean remove(Object key, Object value) {
        rawChanging(key, false);
        return super.remove(key, value);
    }

    @Override
    public Object replace(Object key, Object value) {
        rawChanging(key, false);
        return super.replace(key, value);
    }

    @Override
    public boolean replace(Object key, Object before, Object after) {
        rawChanging(key, false);
        return super.replace(key, before, after);
    }

    @Override
    public Object computeIfAbsent(Object key, java.util.function.Function<? super Object, ? extends Object> function) {
        rawChanging(key, false);
        return super.computeIfAbsent(key, function);
    }

    @Override
    public Object computeIfPresent(Object key, java.util.function.BiFunction<? super Object, ? super Object, ? extends Object> function) {
        rawChanging(key, false);
        return super.computeIfPresent(key, function);
    }

    @Override
    public Object compute(Object key, java.util.function.BiFunction<? super Object, ? super Object, ? extends Object> function) {
        rawChanging(key, false);
        return super.compute(key, function);
    }

    @Override
    public Object merge(Object key, Object value, java.util.function.BiFunction<? super Object, ? super Object, ? extends Object> function) {
        rawChanging(key, false);
        return super.merge(key, value, function);
    }

    @Override
    public void replaceAll(java.util.function.BiFunction<? super Object, ? super Object, ? extends Object> function) {
        rawChanging(null, true);
        super.replaceAll(function);
    }

    @Override
    public void clear() {
        rawChanging(null, true);
        super.clear();
    }

    @Override
    public ConcurrentHashMap.KeySetView<Object, Object> keySet(Object mappedValue) {
        rawWritableAlias = true;
        rawChanging(null, true);
        return super.keySet(mappedValue);
    }

    @Override
    public java.util.Set<Map.Entry<Object, Object>> entrySet() {
        rawWritableAlias = true;
        rawChanging(null, true);
        return super.entrySet();
    }

    static synchronized long repairVersion(Map<?, ?> registry) {
        var caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != NativeItemCapabilityObservation.class) throw new SecurityException("Original repair provider observation required");
        if (registry instanceof NativeCuriosProviderFields260 tracked) {
            if (tracked.rawWritableAlias || tracked.rawObservationFailed || tracked.rawMutationVersion.get() == Long.MAX_VALUE) throw new IllegalStateException("Native registry mutable insertion alias, failed observation or version exhausted");
            return tracked.rawMutationVersion.get();
        }
        if (!REPAIR_REGISTRY_VERSIONS.containsKey(registry)) {
            if (REPAIR_REGISTRY_VERSIONS.size() >= LIMIT) throw new IllegalStateException("Native repair provider registry budget");
            REPAIR_REGISTRY_VERSIONS.put(registry, 0L);
        }
        long version = REPAIR_REGISTRY_VERSIONS.get(registry);
        if (version == Long.MAX_VALUE) throw new IllegalStateException("Native repair provider registry version exhausted");
        return version;
    }

    static synchronized void watchRepair(net.minecraft.world.item.ItemStack stack) {
        var caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerRepairGenerationAuthority.class
            || !(caller.getMethodName().equals("publishNativeRepair") || caller.getMethodName().equals("publishNativeReload"))) {
            throw new SecurityException("Original native repair generation required");
        }
        List<WeakReference<net.minecraft.world.item.ItemStack>> owners = REPAIR_OWNERS.computeIfAbsent(stack.getItem(), ignored -> new java.util.ArrayList<>());
        owners.removeIf(owner -> owner.get() == null);
        if (owners.stream().anyMatch(owner -> owner.get() == stack)) return;
        if (REPAIR_OWNERS.size() >= LIMIT || owners.size() >= LIMIT) throw new IllegalStateException("Native repair provider owner budget");
        owners.add(new WeakReference<>(stack));
    }

    static void retireReloadPredecessors(net.minecraft.world.item.ItemStack actual, OwnerLineageRevocationStore.Lease successor) {
        var caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerRepairGenerationAuthority.class || !caller.getMethodName().equals("publishNativeReload"))
            throw new SecurityException("Original native reload generation required");
        List<net.minecraft.world.item.ItemStack> predecessors;
        synchronized (NativeCuriosProviderFields260.class) {
            List<WeakReference<net.minecraft.world.item.ItemStack>> registered = REPAIR_OWNERS.get(actual.getItem());
            if (registered == null) return;
            predecessors = registered.stream().map(WeakReference::get).filter(java.util.Objects::nonNull).filter(stack -> stack != actual).toList();
        }
        for (var predecessor : predecessors) {
            OwnerLineageRevocationStore.Lease lease = OwnerFullRepairGuard.lease(predecessor);
            if (lease != null && lease.owner.equals(successor.owner) && lease.weapon.equals(successor.weapon))
                OwnerFullRepairGuard.changed(predecessor, 20);
        }
    }

    static void repairProviderChanging(Item item) {
        var caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (!(caller.getDeclaringClass() == NativeItemCapabilityObservation.class && caller.getMethodName().equals("nativeRepairProviders"))
            && !(caller.getDeclaringClass() == NativeCuriosProviderFields260.class && caller.getMethodName().equals("before"))) {
            throw new SecurityException("Original native provider mutation required");
        }
        List<net.minecraft.world.item.ItemStack> owners;
        synchronized (NativeCuriosProviderFields260.class) {
            List<WeakReference<net.minecraft.world.item.ItemStack>> registered = REPAIR_OWNERS.get(item);
            if (registered == null) return;
            registered.removeIf(owner -> owner.get() == null);
            owners = registered.stream().map(WeakReference::get).filter(java.util.Objects::nonNull).toList();
        }
        for (var owner : owners) OwnerFullRepairGuard.changed(owner, 20);
    }

    private static void caller(String method) {
        List<StackWalker.StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(3L).toList());
        if (frames.size() != 3 || frames.get(0).getDeclaringClass() != NativeCuriosProviderFields260.class
            || !frames.get(0).getMethodName().equals(method)
            || !frames.get(1).getDeclaringClass().getName().equals("top.theillusivec4.curios.mixin.CuriosImplMixinHooks")
            || frames.get(1).getDeclaringClass().getClassLoader() != NativeCuriosProviderFields260.class.getClassLoader()
            || !frames.get(1).getMethodName().endsWith("$ftboss$nativeCuriosStateWrite")
            || !frames.get(1).getDescriptor().equals("(Ljava/util/Map;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
            || frames.get(2).getDeclaringClass() != frames.get(1).getDeclaringClass()
            || !frames.get(2).getMethodName().equals("registerCurio")
            || !frames.get(2).getDescriptor().equals("(Lnet/minecraft/world/item/Item;Ltop/theillusivec4/curios/api/type/capability/ICurioItem;)V")) {
            throw new SecurityException("Actual native Curios provider field write required");
        }
    }

    private static Snapshot capture(Map<?, ?> registry, Object key) {
        if (registry == null || registry.getClass() != ConcurrentHashMap.class && registry.getClass() != NativeCuriosProviderFields260.class) return new Snapshot(false, false, null);
        if (key == null) return new Snapshot(true, false, null);
        if (!(key instanceof Item)) return new Snapshot(false, false, null);
        try {
            if (key.getClass().getMethod("hashCode").getDeclaringClass() != Object.class
                || key.getClass().getMethod("equals", Object.class).getDeclaringClass() != Object.class) return new Snapshot(false, false, null);
        } catch (ReflectiveOperationException | RuntimeException failure) {
            return new Snapshot(false, false, null);
        }
        Object value = registry.get(key);
        return new Snapshot(true, value != null, value);
    }

    public static Object before(Map<?, ?> registry, Object key, Object requested) {
        caller("before");
        if (key instanceof Item item) repairProviderChanging(item);
        Frame frame;
        synchronized (NativeCuriosProviderFields260.class) {
            Long repairVersion = REPAIR_REGISTRY_VERSIONS.get(registry);
            if (repairVersion != null) REPAIR_REGISTRY_VERSIONS.put(registry, repairVersion == Long.MAX_VALUE ? Long.MAX_VALUE : repairVersion + 1L);
            State state = STATES.get(key);
            if (state == null && STATES.size() < LIMIT) {
                state = new State(registry);
                STATES.put(key, state);
            }
            boolean overflow = state == null || state.version == Long.MAX_VALUE || state.active.size() >= 64;
            if (overflow) {
                if (state != null) for (Frame active : state.active) active.conflict = true;
                rejected++;
                state = new State(registry);
            }
            boolean conflict = overflow || !state.active.isEmpty() || state.registry.get() != registry;
            for (Frame active : state.active) active.conflict = true;
            Snapshot snapshot = capture(registry, key);
            frame = new Frame(state, registry, key, requested, snapshot, ++state.version, conflict);
            state.active.add(frame);
        }
        try {
            frame.factToken = NativeExpectedFieldObservation.curiosProviderFieldsBefore(registry, key, frame.before, frame);
        } catch (Throwable observationFailure) {
            frame.conflict = true;
        }
        return frame;
    }

    public static void after(Object token, Map<?, ?> registry, Object key, Object requested, Object returned, Throwable originalFailure) {
        caller("after");
        if (!(token instanceof Frame frame)) {
            synchronized (NativeCuriosProviderFields260.class) { rejected++; }
            return;
        }
        Snapshot snapshot = capture(registry, key);
        boolean matched;
        synchronized (NativeCuriosProviderFields260.class) {
            matched = !frame.completed && frame.thread.get() == Thread.currentThread() && frame.registry.get() == registry
                && frame.key.get() == key && frame.requested.get() == requested
                && frame.state.active.contains(frame) && frame.state.version == frame.entryVersion
                && !frame.conflict && frame.before.supported && snapshot.supported;
            if (originalFailure == null) matched &= frame.before.value.get() == returned
                && snapshot.value.get() == requested && snapshot.present && key != null && requested != null;
            else matched &= frame.before.present == snapshot.present && frame.before.value.get() == snapshot.value.get();
            if (frame.completed) { rejected++; return; }
            frame.completed = true;
            frame.after = snapshot;
            frame.returned = new WeakReference<>(returned);
            frame.failureClass = originalFailure == null ? null : originalFailure.getClass().getName();
            frame.matched = matched;
            frame.state.active.remove(frame);
            if (frame.state.version < Long.MAX_VALUE) frame.exitVersion = ++frame.state.version;
            else { frame.matched = false; rejected++; }
            frame.state.last = frame;
        }
        try {
            NativeExpectedFieldObservation.curiosProviderFieldsAfter(frame.factToken, registry, key, snapshot, frame, originalFailure, frame.matched);
        } finally {
            frame.factToken = null;
        }
    }

    private static final class State {
        private final WeakReference<Object> registry;
        private final List<Frame> active = new ArrayList<>();
        private long version;
        private Frame last;

        private State(Object registry) {
            this.registry = new WeakReference<>(registry);
        }
    }

    private static final class Snapshot {
        private final boolean supported;
        private final boolean present;
        private final WeakReference<Object> value;

        private Snapshot(boolean supported, boolean present, Object value) {
            this.supported = supported;
            this.present = present;
            this.value = new WeakReference<>(value);
        }
    }

    private static final class Frame {
        private final State state;
        private final WeakReference<Thread> thread = new WeakReference<>(Thread.currentThread());
        private final WeakReference<Object> registry;
        private final WeakReference<Object> key;
        private final WeakReference<Object> requested;
        private final Snapshot before;
        private final long entryVersion;
        private boolean conflict;
        private boolean completed;
        private Snapshot after;
        private WeakReference<Object> returned;
        private String failureClass;
        private boolean matched;
        private long exitVersion;
        private Object factToken;

        private Frame(State state, Object registry, Object key, Object requested, Snapshot before, long entryVersion, boolean conflict) {
            this.state = state;
            this.registry = new WeakReference<>(registry);
            this.key = new WeakReference<>(key);
            this.requested = new WeakReference<>(requested);
            this.before = before;
            this.entryVersion = entryVersion;
            this.conflict = conflict;
        }
    }
}
