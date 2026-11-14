package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class NativeItemHandlerLifetime232 {
    private static final int LIMIT = 65536;
    private static final List<Watch> WATCHES = new ArrayList<>();
    private static final List<ChildWatch> CHILDREN = new ArrayList<>();
    private static final List<List<WeakReference<Object>>> REFERENCES = new ArrayList<>();

    public static void providerReferences(Object provider, ItemStack owner) {
        List<StackWalker.StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).limit(2L).toList());
        if (frames.size() != 2 || frames.get(0).getDeclaringClass() != NativeOpaqueProviderReadFence201.class || !frames.get(0).getMethodName().equals("before") || !frames.get(0).getDescriptor().equals("(Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;)V") || frames.get(1).getDeclaringClass() != NativeExpectedFieldObservation.class || !frames.get(1).getMethodName().equals("nativeProviderQueryBefore") || !frames.get(1).getDescriptor().equals("(Ljava/lang/Object;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;)Ljava/lang/Object;")) throw new SecurityException("Actual native provider reference capture required");
        if (owner == null || !(provider instanceof net.neoforged.neoforge.capabilities.ICapabilityProvider) || !provider.getClass().isSynthetic()) return;
        java.lang.reflect.Field[] fields = provider.getClass().getDeclaredFields();
        if (fields.length > 64) { OwnerFullRepairGuard.changed(owner, 40); return; }
        List<Object> references = new ArrayList<>();
        for (java.lang.reflect.Field field : fields) {
            if (field.getType() != java.util.concurrent.atomic.AtomicReference.class || java.lang.reflect.Modifier.isStatic(field.getModifiers()) || !java.lang.reflect.Modifier.isFinal(field.getModifiers())) continue;
            try {
                if (!field.trySetAccessible()) { OwnerFullRepairGuard.changed(owner, 40); return; }
                Object reference = field.get(provider);
                if (reference != null && reference.getClass() == java.util.concurrent.atomic.AtomicReference.class) references.add(reference);
            } catch (IllegalAccessException | RuntimeException inaccessible) { OwnerFullRepairGuard.changed(owner, 40); return; }
        }
        boolean exhausted = false;
        synchronized (NativeItemHandlerLifetime232.class) {
            REFERENCES.removeIf(watch -> watch.get(0).get() == null || watch.get(1).get() == null);
            for (Object reference : references) {
                boolean present = false;
                for (List<WeakReference<Object>> watch : REFERENCES) if (watch.get(0).get() == owner && watch.get(1).get() == reference) { present = true; break; }
                if (!present) {
                    if (REFERENCES.size() >= LIMIT) exhausted = true;
                    else REFERENCES.add(List.of(new WeakReference<Object>(owner), new WeakReference<Object>(reference)));
                }
            }
        }
        if (exhausted) OwnerFullRepairGuard.changed(owner, 40);
    }

    private NativeItemHandlerLifetime232() {
    }

    public static void returned(Object owner,Object result) {
        List<StackWalker.StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(3L).toList());
        if (frames.size() != 3 || frames.get(0).getDeclaringClass() != ItemCapability.class
            || !frames.get(0).getMethodName().equals("ftboss$nativeProviderRepairAfter")
            || !frames.get(0).getDescriptor().equals("(Ljava/lang/Object;Ljava/lang/Object;)V")
            || frames.get(1).getDeclaringClass() != ItemCapability.class
            || !frames.get(1).getMethodName().endsWith("$ftboss$nativeProviderQuery")
            || !frames.get(1).getDescriptor().equals("(Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
            || frames.get(2).getDeclaringClass() != ItemCapability.class
            || !frames.get(2).getMethodName().equals("getCapability")
            || !frames.get(2).getDescriptor().equals("(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;)Ljava/lang/Object;")) {
            throw new SecurityException("Actual original native capability return required");
        }
        if (!(owner instanceof ItemStack stack) || !(result instanceof ItemStackHandler)) return;
        boolean exhausted;
        synchronized (NativeItemHandlerLifetime232.class) {
            compact();
            for (Watch watch : WATCHES) if (watch.owner.get() == stack && watch.handler.get() == result) return;
            exhausted = WATCHES.size() >= LIMIT;
            if (!exhausted) WATCHES.add(new Watch(new WeakReference<>(stack),new WeakReference<>((ItemStackHandler)result)));
        }
        if (exhausted) OwnerFullRepairGuard.changed(stack,40);
    }

    public static void changing(ItemStackHandler handler) {
        List<StackWalker.StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(3L).toList());
        if (frames.size() != 3 || frames.get(0).getDeclaringClass() != ItemStackHandler.class
            || !frames.get(0).getMethodName().equals("ftboss$nativeHandlerChanging")
            || !frames.get(0).getDescriptor().equals("()V")
            || frames.get(1).getDeclaringClass() != ItemStackHandler.class
            || !frames.get(1).getMethodName().contains("$ftboss$nativeHandler")
            || frames.get(2).getDeclaringClass() != ItemStackHandler.class
            || !originalWrite(frames.get(2).getMethodName()+frames.get(2).getDescriptor())) {
            throw new SecurityException("Actual original native handler write required");
        }
        if (handler == null) return;
        List<ItemStack> owners = new ArrayList<>();
        synchronized (NativeItemHandlerLifetime232.class) {
            compact();
            for (Watch watch : WATCHES) {
                ItemStack owner = watch.owner.get();
                if (owner != null && watch.handler.get() == handler) owners.add(owner);
            }
        }
        synchronized (NativeItemHandlerLifetime232.class) {
            REFERENCES.removeIf(watch -> watch.get(0).get() == null || watch.get(1).get() == null);
            for (List<WeakReference<Object>> watch : REFERENCES) {
                Object owner = watch.get(0).get();
                Object reference = watch.get(1).get();
                if (owner instanceof ItemStack stack && reference instanceof java.util.concurrent.atomic.AtomicReference<?> atomic && atomic.get() == handler && !owners.contains(stack)) owners.add(stack);
            }
        }
        for (ItemStack owner : owners) {
            try { OwnerFullRepairGuard.changed(owner,20); }
            catch (Throwable observationFailure) { }
        }
    }

    public static void childReturned(ItemStackHandler handler,ItemStack child) {
        List<StackWalker.StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(3L).toList());
        if (frames.size() != 3 || frames.get(0).getDeclaringClass() != ItemStackHandler.class
            || !frames.get(0).getMethodName().equals("ftboss$nativeHandlerChildReturned")
            || !frames.get(0).getDescriptor().equals("(Lnet/minecraft/world/item/ItemStack;)V")
            || frames.get(1).getDeclaringClass() != ItemStackHandler.class
            || !frames.get(1).getMethodName().contains("$ftboss$nativeHandlerChildReturn")
            || !frames.get(1).getDescriptor().equals("(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V")
            || frames.get(2).getDeclaringClass() != ItemStackHandler.class
            || !frames.get(2).getMethodName().equals("getStackInSlot")
            || !frames.get(2).getDescriptor().equals("(I)Lnet/minecraft/world/item/ItemStack;")) {
            throw new SecurityException("Actual original native handler child return required");
        }
        if (handler == null || child == null || child.isEmpty()) return;
        List<ItemStack> owners = new ArrayList<>();
        synchronized (NativeItemHandlerLifetime232.class) {
            compact();
            for (Watch watch : WATCHES) {
                ItemStack owner = watch.owner.get();
                if (owner != null && watch.handler.get() == handler) owners.add(owner);
            }
        }
        List<ItemStack> exhausted = new ArrayList<>();
        synchronized (NativeItemHandlerLifetime232.class) {
            CHILDREN.removeIf(watch -> watch.owner.get() == null || watch.child.get() == null);
            for (ItemStack owner : owners) {
                boolean present = false;
                for (ChildWatch watch : CHILDREN) if (watch.owner.get() == owner && watch.child.get() == child) { present = true; break; }
                if (!present) {
                    if (CHILDREN.size() >= LIMIT) exhausted.add(owner);
                    else CHILDREN.add(new ChildWatch(new WeakReference<>(owner),new WeakReference<>(child)));
                }
            }
        }
        for (ItemStack owner : exhausted) {
            try { OwnerFullRepairGuard.changed(owner,40); }
            catch (Throwable observationFailure) { }
        }
    }

    public static void childCountChanging(ItemStack child) {
        List<StackWalker.StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(3L).toList());
        if (frames.size() != 3 || frames.get(0).getDeclaringClass() != ItemStack.class
            || !frames.get(0).getMethodName().equals("ftboss$nativeChildCountChanging")
            || !frames.get(0).getDescriptor().equals("()V")
            || frames.get(1).getDeclaringClass() != ItemStack.class
            || !frames.get(1).getMethodName().contains("$ftboss$nativeChildCountWrite")
            || !frames.get(1).getDescriptor().equals("(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V")
            || frames.get(2).getDeclaringClass() != ItemStack.class
            || !frames.get(2).getMethodName().equals("setCount")
            || !frames.get(2).getDescriptor().equals("(I)V")) {
            throw new SecurityException("Actual original native child count write required");
        }
        List<ItemStack> owners = new ArrayList<>();
        synchronized (NativeItemHandlerLifetime232.class) {
            CHILDREN.removeIf(watch -> watch.owner.get() == null || watch.child.get() == null);
            for (ChildWatch watch : CHILDREN) {
                ItemStack owner = watch.owner.get();
                if (owner != null && watch.child.get() == child) owners.add(owner);
            }
        }
        for (ItemStack owner : owners) {
            try { OwnerFullRepairGuard.changed(owner,20); }
            catch (Throwable observationFailure) { }
        }
    }

    private static boolean originalWrite(String method) {
        return switch (method) {
            case "setSize(I)V", "setStackInSlot(ILnet/minecraft/world/item/ItemStack;)V",
                "insertItem(ILnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;",
                "extractItem(IIZ)Lnet/minecraft/world/item/ItemStack;",
                "lambda$deserializeNBT$0(ILnet/minecraft/world/item/ItemStack;)V" -> true;
            default -> false;
        };
    }

    private static void compact() {
        Iterator<Watch> entries = WATCHES.iterator();
        while (entries.hasNext()) {
            Watch watch = entries.next();
            if (watch.owner.get() == null || watch.handler.get() == null) entries.remove();
        }
    }

    private record ChildWatch(WeakReference<ItemStack> owner,WeakReference<ItemStack> child) {
    }

    private record Watch(WeakReference<ItemStack> owner,WeakReference<ItemStackHandler> handler) {
    }

    public static void serialized(ItemStackHandler handler, net.minecraft.nbt.CompoundTag result) {
        List<StackWalker.StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(3L).toList());
        if (frames.size() != 3 || frames.get(0).getDeclaringClass() != ItemStackHandler.class
            || !frames.get(0).getMethodName().equals("ftboss$nativeHandlerSerialized")
            || !frames.get(0).getDescriptor().equals("(Lnet/minecraft/nbt/CompoundTag;)V")
            || frames.get(1).getDeclaringClass() != ItemStackHandler.class
            || !frames.get(1).getMethodName().endsWith("$ftboss$nativeHandlerSerializeReturn")
            || !frames.get(1).getDescriptor().equals("(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V")
            || frames.get(2).getDeclaringClass() != ItemStackHandler.class
            || !frames.get(2).getMethodName().equals("serializeNBT")
            || !frames.get(2).getDescriptor().equals("(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;")) {
            throw new SecurityException("Actual original native handler serialization return required");
        }
        if (handler == null || result == null) return;
        List<ItemStack> owners = new ArrayList<>();
        synchronized (NativeItemHandlerLifetime232.class) {
            compact();
            for (Watch watch : WATCHES) {
                ItemStack owner = watch.owner.get();
                if (owner != null && watch.handler.get() == handler) owners.add(owner);
            }
        }
        for (ItemStack owner : owners) {
            try { OwnerFullRepairGuard.alias(owner, result, true); }
            catch (Throwable observationFailure) { }
        }
    }
}
