package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class NativeItemCapabilityObservation {
    private static final IdentityHashMap<Object, Item> ORIGINAL_CURIO_PROVIDERS = new IdentityHashMap<>();
    private static Object originalCurioCapability;
    private Item repairItem;
    private Map<?, ?> repairRegistry;
    private long repairRegistryVersion;
    private boolean repairPolicy;
    private final IdentityHashMap<ItemCapability<?, ?>, Capability> capabilities = new IdentityHashMap<>();
    private final Thread thread = Thread.currentThread();
    private int budget;

    private NativeItemCapabilityObservation() {
        List<ItemCapability<?, ?>> registered = ItemCapability.getAll();
        if (registered.size() > 65536) throw new IllegalArgumentException("Native capability registry budget exceeded");
        for (ItemCapability<?, ?> capability : registered) {
            if (capability == null || this.capabilities.put(capability, read(capability)) != null) {
                throw new IllegalStateException("Invalid native capability identity registry");
            }
        }
        if (!unchanged()) throw new IllegalStateException("Native capability registry changed during observation");
    }

    public static NativeItemCapabilityObservation capture() {
        return new NativeItemCapabilityObservation();
    }

    public static void nativeRepairProviders(ItemCapability<?, ?> capability, Object provider, net.minecraft.world.level.ItemLike[] items, boolean before) {
        List<StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).limit(3L).toList());
        if (frames.size() != 3 || frames.get(0).getDeclaringClass() != RegisterCapabilitiesEvent.class
            || !frames.get(0).getMethodName().endsWith(before ? "$ftboss$originalProviderBefore" : "$ftboss$originalProviderAfter")
            || frames.get(1).getDeclaringClass() != RegisterCapabilitiesEvent.class || !frames.get(1).getMethodName().equals("registerItem")) {
            throw new SecurityException("Actual native repair provider registration required");
        }
        if (items == null || items.length > 65536) return;
        if (before) {
            for (var item : items) if (item != null) NativeCuriosProviderFields260.repairProviderChanging(item.asItem());
            return;
        }
        try {
            ClassLoader loader = NativeItemCapabilityObservation.class.getClassLoader();
            Class<?> curios = Class.forName("top.theillusivec4.curios.Curios", false, loader);
            if (frames.get(2).getDeclaringClass() != curios || !frames.get(2).getMethodName().equals("registerCaps")
                || items.length != 1 || items[0] == null || provider == null || !provider.getClass().isHidden()
                || provider.getClass().getNestHost() != curios) return;
            Class<?> capabilityClass = Class.forName("top.theillusivec4.curios.api.CuriosCapability", false, loader);
            if (capabilityClass.getField("ITEM").get(null) != capability) return;
            Item item = items[0].asItem();
            java.lang.reflect.Field[] fields = provider.getClass().getDeclaredFields();
            if (fields.length != 1 || fields[0].getType() != Item.class || !java.lang.reflect.Modifier.isFinal(fields[0].getModifiers())
                || java.lang.reflect.Modifier.isStatic(fields[0].getModifiers()) || !fields[0].trySetAccessible() || fields[0].get(provider) != item) return;
            synchronized (ORIGINAL_CURIO_PROVIDERS) {
                if (ORIGINAL_CURIO_PROVIDERS.size() >= 65536 || originalCurioCapability != null && originalCurioCapability != capability) return;
                originalCurioCapability = capability;
                ORIGINAL_CURIO_PROVIDERS.put(provider, item);
            }
        } catch (ReflectiveOperationException | LinkageError | RuntimeException unsupported) {
        }
    }

    static NativeItemCapabilityObservation nativeRepairCapture(ItemStack stack) {
        StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass().getNestHost() != NativeFoodWeaponWear.class) throw new SecurityException("Original native repair observation required");
        NativeItemCapabilityObservation observation = capture();
        observation.repairItem = stack.getItem();
        boolean curiosPresent = false;
        for (var entry : observation.capabilities.entrySet()) {
            Providers providers = entry.getValue().entries.get(observation.repairItem);
            if (providers == null) continue;
            if (providers.members.size() != 1) throw new IllegalArgumentException("Native repair requires absent or original inert Curios providers");
            synchronized (ORIGINAL_CURIO_PROVIDERS) {
                if (entry.getKey() != originalCurioCapability || ORIGINAL_CURIO_PROVIDERS.get(providers.members.get(0)) != observation.repairItem) {
                    throw new IllegalArgumentException("Native repair provider has no original registration provenance");
                }
            }
            curiosPresent = true;
        }
        if (curiosPresent) {
            try {
                ClassLoader loader = NativeItemCapabilityObservation.class.getClassLoader();
                Class<?> hooks = Class.forName("top.theillusivec4.curios.mixin.CuriosImplMixinHooks", false, loader);
                Class<?> curioItem = Class.forName("top.theillusivec4.curios.api.type.capability.ICurioItem", false, loader);
                java.lang.reflect.Field registry = hooks.getDeclaredField("REGISTRY");
                if (!registry.trySetAccessible() || curioItem.isInstance(observation.repairItem)) throw new IllegalArgumentException("Native repair custom Curios item unsupported");
                Object actual = registry.get(null);
                if (!(actual instanceof Map<?, ?> table) || actual.getClass() != NativeCuriosProviderFields260.class || table.containsKey(observation.repairItem)) {
                    throw new IllegalArgumentException("Native repair custom Curios registration unsupported");
                }
                observation.repairRegistry = table;
                observation.repairRegistryVersion = NativeCuriosProviderFields260.repairVersion(table);
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Native repair Curios registry observation unavailable", failure);
            }
        }
        observation.repairPolicy = true;
        if (!observation.live()) throw new IllegalStateException("Native repair provider observation changed during capture");
        return observation;
    }

    private boolean repairLive() {
        if (!this.repairPolicy || this.repairRegistry == null) return true;
        try {
            Class<?> hooks = Class.forName("top.theillusivec4.curios.mixin.CuriosImplMixinHooks", false, NativeItemCapabilityObservation.class.getClassLoader());
            java.lang.reflect.Field registry = hooks.getDeclaredField("REGISTRY");
            return registry.trySetAccessible() && registry.get(null) == this.repairRegistry && !this.repairRegistry.containsKey(this.repairItem)
                && NativeCuriosProviderFields260.repairVersion(this.repairRegistry) == this.repairRegistryVersion;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException unsupported) {
            return false;
        }
    }

    public static void requireFields(String method) {
        List<StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(2L).toList());
        if (frames.size() != 2 || frames.get(0).getDeclaringClass() != ItemCapability.class
            || !frames.get(0).getMethodName().equals(method) || frames.get(1).getDeclaringClass() != NativeItemCapabilityObservation.class
            || !frames.get(1).getMethodName().equals("read")) {
            throw new IllegalStateException("Native capability physical fields denied");
        }
    }

    public static void requireRegistration() {
        List<StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(3L).toList());
        if (frames.size() != 3 || frames.get(0).getDeclaringClass() != ItemCapability.class
            || !frames.get(0).getMethodName().equals("ftboss$nativeCapabilityRegistration") || !frames.get(0).getDescriptor().equals("()V")
            || frames.get(1).getDeclaringClass() != RegisterCapabilitiesEvent.class || !frames.get(1).getMethodName().contains("ftboss$originalProviderBefore")
            || frames.get(2).getDeclaringClass() != RegisterCapabilitiesEvent.class || !frames.get(2).getMethodName().equals("registerItem")
            || !frames.get(2).getDescriptor().equals("(Lnet/neoforged/neoforge/capabilities/ItemCapability;Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;[Lnet/minecraft/world/level/ItemLike;)V")) {
            throw new IllegalStateException("Native capability registration version caller denied");
        }
    }

    private Capability read(ItemCapability<?, ?> capability) {
        if (!((Object)capability instanceof NativeCapabilityRegistrationFields fields)) {
            throw new IllegalStateException("Native capability registration fields Mixin required");
        }
        long version = fields.ftboss$nativeCapabilityVersion();
        if (version == Long.MAX_VALUE) throw new IllegalStateException("Native capability registration version exhausted");
        Map<Item, ?> providers = fields.ftboss$rawNativeProviders();
        if (providers == null || providers.getClass() != IdentityHashMap.class || providers.size() > 65536 - this.budget) {
            throw new IllegalArgumentException("Unsupported native capability provider map");
        }
        IdentityHashMap<Item, Providers> entries = new IdentityHashMap<>();
        for (Map.Entry<Item, ?> entry : providers.entrySet()) {
            if (entry.getKey() == null || !(entry.getValue() instanceof List<?> list) || list.getClass() != ArrayList.class
                || ++this.budget > 65536 || list.size() > 65536 - this.budget) {
                throw new IllegalArgumentException("Unsupported native capability provider list");
            }
            ArrayList<Object> members = new ArrayList<>();
            for (Object provider : list) {
                if (!(provider instanceof ICapabilityProvider) || ++this.budget > 65536) {
                    throw new IllegalArgumentException("Invalid native capability provider entry");
                }
                members.add(provider);
            }
            entries.put(entry.getKey(), new Providers(list, List.copyOf(members)));
        }
        if (fields.ftboss$nativeCapabilityVersion() != version || fields.ftboss$rawNativeProviders() != providers) {
            throw new IllegalStateException("Native capability changed while reading physical fields");
        }
        return new Capability(providers, version, entries);
    }

    private boolean unchanged() {
        this.budget = 0;
        if (this.thread != Thread.currentThread()) return false;
        List<ItemCapability<?, ?>> current = ItemCapability.getAll();
        if (current.size() != this.capabilities.size()) return false;
        for (ItemCapability<?, ?> capability : current) {
            Capability before = this.capabilities.get(capability);
            if (before == null || !same(before, read(capability))) return false;
        }
        return true;
    }

    private static boolean same(Capability before, Capability current) {
        if (before.map != current.map || before.version != current.version || before.entries.size() != current.entries.size()) return false;
        for (Map.Entry<Item, Providers> entry : before.entries.entrySet()) {
            Providers after = current.entries.get(entry.getKey());
            Providers prior = entry.getValue();
            if (after == null || prior.list != after.list || prior.members.size() != after.members.size()) return false;
            for (int index = 0; index < prior.members.size(); index++) {
                if (prior.members.get(index) != after.members.get(index)) return false;
            }
        }
        return true;
    }

    public boolean live() {
        try {
            this.budget = 0;
            return unchanged() && repairLive();
        } catch (RuntimeException rejected) {
            return false;
        }
    }

    public boolean same(NativeItemCapabilityObservation current) {
        if (current == null || !live() || !current.live() || this.capabilities.size() != current.capabilities.size()) return false;
        for (Map.Entry<ItemCapability<?, ?>, Capability> entry : this.capabilities.entrySet()) {
            Capability after = current.capabilities.get(entry.getKey());
            if (after == null || !same(entry.getValue(), after)) return false;
        }
        return true;
    }

    private record Capability(Map<Item, ?> map, long version, IdentityHashMap<Item, Providers> entries) {
    }

    private record Providers(List<?> list, List<Object> members) {
    }
}
