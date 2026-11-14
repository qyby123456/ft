package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class NativePreQueryCapabilityLifetime199 {
    private static final int LIMIT = 65536;
    private static final Map<Item,ArrayList<Watch>> WATCHES = new IdentityHashMap<>();
    private static final Map<Watch,Object> NOTIFIED = new IdentityHashMap<>();
    private static int count;

    private NativePreQueryCapabilityLifetime199() {
    }

    public static void watch(Object actual,OwnerFullRepairGuard.Cut cut) {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != OwnerFullRepairGuard.class || !caller.getMethodName().equals("enter")
            || !caller.getDescriptor().equals("(Ljava/lang/Object;Ljava/util/UUID;I)Lnet/farmingtales/ftboss/item/OwnerFullRepairGuard$Cut;")
            || !Thread.holdsLock(OwnerFullRepairGuard.class)) {
            throw new SecurityException("Actual original repair entry required");
        }
        if (!(actual instanceof ItemStack stack) || cut == null) return;
        boolean overflow = false;
        synchronized (NativePreQueryCapabilityLifetime199.class) {
            if (count >= LIMIT) compact();
            if (count >= LIMIT) overflow = true;
            else {
                WATCHES.computeIfAbsent(stack.getItem(),unused -> new ArrayList<>())
                    .add(new Watch(new WeakReference<>(stack),new WeakReference<>(cut)));
                count++;
            }
        }
        if (overflow) OwnerFullRepairGuard.changed(stack,40);
    }

    public static void curiosState(Item item, boolean before) {
        List<StackWalker.StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(2L).toList());
        String hook = before ? "$ftboss$nativeCuriosStateBefore" : "$ftboss$nativeCuriosStateAfter";
        if (frames.size() != 2 || !frames.get(0).getDeclaringClass().getName().equals("top.theillusivec4.curios.mixin.CuriosImplMixinHooks")
            || frames.get(0).getDeclaringClass().getClassLoader() != NativePreQueryCapabilityLifetime199.class.getClassLoader()
            || !frames.get(0).getMethodName().endsWith(hook)
            || !frames.get(0).getDescriptor().equals("(Lnet/minecraft/world/item/Item;Ljava/lang/Object;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V")
            || frames.get(1).getDeclaringClass() != frames.get(0).getDeclaringClass()
            || !frames.get(1).getMethodName().equals("registerCurio")
            || !frames.get(1).getDescriptor().equals("(Lnet/minecraft/world/item/Item;Ltop/theillusivec4/curios/api/type/capability/ICurioItem;)V")) {
            throw new SecurityException("Actual native Curios provider state transition required");
        }
        if (item == null) return;
        List<ItemStack> owners = new ArrayList<>();
        synchronized (NativePreQueryCapabilityLifetime199.class) {
            compact();
            ArrayList<Watch> watches = WATCHES.get(item);
            if (watches == null) return;
            IdentityHashMap<ItemStack,Boolean> selected = new IdentityHashMap<>();
            for (Watch watch : watches) {
                ItemStack stack = watch.stack.get();
                if (stack != null && stack.getItem() == item && selected.put(stack, Boolean.TRUE) == null) owners.add(stack);
            }
        }
        for (ItemStack owner : owners) {
            try { OwnerFullRepairGuard.changed(owner,20); }
            catch (Throwable observationFailure) { }
        }
    }

    private static void compact() {
        Iterator<ArrayList<Watch>> groups = WATCHES.values().iterator();
        while (groups.hasNext()) {
            ArrayList<Watch> watches = groups.next();
            Iterator<Watch> entries = watches.iterator();
            while (entries.hasNext()) {
                Watch watch = entries.next();
                ItemStack stack = watch.stack.get();
                OwnerFullRepairGuard.Cut cut = watch.cut.get();
                if (stack == null || cut == null || !OwnerFullRepairGuard.nativePreQueryWatchLive(stack,cut)) {
                    NOTIFIED.remove(watch);
                    entries.remove();
                    count--;
                }
            }
            if (watches.isEmpty()) groups.remove();
        }
    }

    public static void resolved(ItemCapability<?,?> capability,Item item) {
        List<StackWalker.StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(2L).toList());
        if (frames.size() != 2 || frames.get(0).getDeclaringClass() != RegisterCapabilitiesEvent.class
            || !frames.get(0).getMethodName().endsWith("$ftboss$nativeLifetimeItem")
            || !frames.get(0).getDescriptor().equals("(Lnet/minecraft/world/level/ItemLike;Lnet/neoforged/neoforge/capabilities/ItemCapability;Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;[Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/Item;")
            || frames.get(1).getDeclaringClass() != RegisterCapabilitiesEvent.class
            || !frames.get(1).getMethodName().equals("registerItem")
            || !frames.get(1).getDescriptor().equals("(Lnet/neoforged/neoforge/capabilities/ItemCapability;Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;[Lnet/minecraft/world/level/ItemLike;)V")) {
            throw new SecurityException("Actual original native registration resolution required");
        }
        if (capability == null || item == null) return;
        List<Watch> pending;
        synchronized (NativePreQueryCapabilityLifetime199.class) {
            ArrayList<Watch> watches = WATCHES.get(item);
            if (watches == null) return;
            pending = List.copyOf(watches);
        }
        IdentityHashMap<ItemStack,Boolean> seen = new IdentityHashMap<>();
        for (Watch watch : pending) {
            ItemStack stack = watch.stack.get();
            OwnerFullRepairGuard.Cut cut = watch.cut.get();
            if (stack == null || cut == null || seen.containsKey(stack)) continue;
            if (!OwnerFullRepairGuard.nativePreQueryWatchLive(stack,cut) || stack.getItem() != item
                || notified(watch,capability) && NativeCapabilityOwnerLinks194.hasOwner199(capability,stack)) continue;
            seen.put(stack,Boolean.TRUE);
            try {
                OwnerFullRepairGuard.changed(stack,20);
            } catch (Throwable observationFailure) {
            }
        }
    }

    private static void notificationCaller() {
        StackWalker.StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(frames -> frames.skip(2L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != NativeCapabilityOwnerLinks194.class || !caller.getMethodName().equals("changing")
            || !caller.getDescriptor().equals("(Ljava/lang/Object;)V")) {
            throw new SecurityException("Actual native registration owner notification required");
        }
    }

    public static Object notificationCut(Object capability,ItemStack stack) {
        notificationCaller();
        if (!(capability instanceof ItemCapability<?,?>) || stack == null) return null;
        synchronized (NativePreQueryCapabilityLifetime199.class) {
            ArrayList<Watch> watches = WATCHES.get(stack.getItem());
            if (watches == null) return null;
            ArrayList<Watch> selected = new ArrayList<>();
            for (Watch watch : watches) if (watch.stack.get() == stack) selected.add(watch);
            return selected.isEmpty() ? null : new Notification(capability,List.copyOf(selected));
        }
    }

    public static void registrationNotified(Object pending) {
        notificationCaller();
        if (!(pending instanceof Notification notification)) return;
        synchronized (NativePreQueryCapabilityLifetime199.class) {
            for (Watch watch : notification.watches) {
                ItemStack stack = watch.stack.get();
                ArrayList<Watch> current = stack == null ? null : WATCHES.get(stack.getItem());
                if (current != null && current.stream().anyMatch(actual -> actual == watch)) NOTIFIED.put(watch,notification.capability);
            }
        }
    }

    private static synchronized boolean notified(Watch watch,Object capability) {
        return NOTIFIED.get(watch) == capability;
    }

    private record Watch(WeakReference<ItemStack> stack,WeakReference<OwnerFullRepairGuard.Cut> cut) {
    }

    private record Notification(Object capability,List<Watch> watches) {
    }
}
