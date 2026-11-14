package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class NativeCapabilityOwnerLinks194 {
    private static final int LIMIT = 65536;
    private static final Map<Object, ArrayList<WeakReference<ItemStack>>> OWNERS = new IdentityHashMap<>();
    private static int associations;

    private NativeCapabilityOwnerLinks194() {
    }

    private static void requireCaller(Class<?> expectedClass, String expectedMethod, String expectedDescriptor) {
        List<StackWalker.StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.filter(frame -> frame.getDeclaringClass() != NativeCapabilityOwnerLinks194.class).limit(3L).toList());
        if (frames.size() != 3) throw new SecurityException("Native capability ownership caller denied");
        StackWalker.StackFrame helper = frames.get(0), operation = frames.get(1), entry = frames.get(2);
        if (helper.getDeclaringClass() != expectedClass || !helper.getMethodName().equals(expectedMethod)
            || !helper.getDescriptor().equals(expectedDescriptor)) {
            throw new SecurityException("Native capability ownership caller denied");
        }
        boolean query = expectedClass == ItemCapability.class
            && operation.getDeclaringClass() == ItemCapability.class
            && operation.getMethodName().endsWith("$ftboss$nativeProviderQuery")
            && operation.getDescriptor().equals("(Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
            && entry.getDeclaringClass() == ItemCapability.class
            && entry.getMethodName().equals("getCapability")
            && entry.getDescriptor().equals("(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;)Ljava/lang/Object;");
        boolean registration = expectedClass == RegisterCapabilitiesEvent.class
            && operation.getDeclaringClass() == RegisterCapabilitiesEvent.class
            && operation.getMethodName().endsWith("$ftboss$originalProviderBefore")
            && operation.getDescriptor().equals("(Lnet/neoforged/neoforge/capabilities/ItemCapability;Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;[Lnet/minecraft/world/level/ItemLike;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V")
            && entry.getDeclaringClass() == RegisterCapabilitiesEvent.class
            && entry.getMethodName().equals("registerItem")
            && entry.getDescriptor().equals("(Lnet/neoforged/neoforge/capabilities/ItemCapability;Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;[Lnet/minecraft/world/level/ItemLike;)V");
        if (!query && !registration) throw new SecurityException("Native capability ownership operation provenance denied");
    }

    private static void compact() {
        Iterator<ArrayList<WeakReference<ItemStack>>> lists = OWNERS.values().iterator();
        while (lists.hasNext()) {
            ArrayList<WeakReference<ItemStack>> owners = lists.next();
            Iterator<WeakReference<ItemStack>> entries = owners.iterator();
            while (entries.hasNext()) {
                if (entries.next().get() == null) {
                    entries.remove();
                    associations--;
                }
            }
            if (owners.isEmpty()) lists.remove();
        }
    }

    public static void bind(Object actualStack, Object capability) {
        requireCaller(ItemCapability.class, "ftboss$nativeProviderOwnerBefore", "(Ljava/lang/Object;Ljava/lang/Object;)V");
        if (!(actualStack instanceof ItemStack stack) || !(capability instanceof ItemCapability<?, ?>)) {
            throw new IllegalArgumentException("Actual native stack and capability required");
        }
        synchronized (NativeCapabilityOwnerLinks194.class) {
            ArrayList<WeakReference<ItemStack>> owners = OWNERS.get(capability);
            if (owners != null) {
                Iterator<WeakReference<ItemStack>> entries = owners.iterator();
                while (entries.hasNext()) {
                    ItemStack existing = entries.next().get();
                    if (existing == stack) return;
                    if (existing == null) {
                        entries.remove();
                        associations--;
                    }
                }
            }
            if (associations >= LIMIT) compact();
            if (associations < LIMIT) {
                OWNERS.computeIfAbsent(capability, unused -> new ArrayList<>()).add(new WeakReference<>(stack));
                associations++;
                return;
            }
        }
        OwnerFullRepairGuard.alias(stack, new Object(), true);
    }

    public static synchronized boolean hasOwner199(ItemCapability<?,?> capability,ItemStack stack) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != NativePreQueryCapabilityLifetime199.class || !caller.getMethodName().equals("resolved")) {
            throw new SecurityException("Actual native lifetime observer required");
        }
        ArrayList<WeakReference<ItemStack>> owners = OWNERS.get(capability);
        if (owners == null) return false;
        for (WeakReference<ItemStack> owner : owners) if (owner.get() == stack) return true;
        return false;
    }

    public static void changing(Object capability) throws Throwable {
        requireCaller(RegisterCapabilitiesEvent.class, "ftboss$nativeProviderRevocationBefore", "(Ljava/lang/Object;)V");
        List<ItemStack> live = new ArrayList<>();
        synchronized (NativeCapabilityOwnerLinks194.class) {
            ArrayList<WeakReference<ItemStack>> owners = OWNERS.get(capability);
            if (owners == null) return;
            Iterator<WeakReference<ItemStack>> entries = owners.iterator();
            while (entries.hasNext()) {
                ItemStack stack = entries.next().get();
                if (stack == null) {
                    entries.remove();
                    associations--;
                } else {
                    live.add(stack);
                }
            }
            if (owners.isEmpty()) OWNERS.remove(capability);
        }
        Throwable firstFailure = null;
        for (ItemStack stack : live) {
            Object pending = null;
            try {
                pending = NativePreQueryCapabilityLifetime199.notificationCut(capability,stack);
            } catch (Throwable observationFailure) {
            }
            try {
                OwnerFullRepairGuard.changed(stack, 20);
                try {
                    NativePreQueryCapabilityLifetime199.registrationNotified(pending);
                } catch (Throwable observationFailure) {
                }
            } catch (Throwable failure) {
                if (firstFailure == null) firstFailure = failure;
            }
        }
        if (firstFailure != null) throw firstFailure;
    }
}
