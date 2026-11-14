package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.IdentityHashMap;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;

public final class NativeItemCapabilityState {
    private NativeItemCapabilityState() {
    }

    public static void requireObserver() {
        List<StackFrame> frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).limit(2L).toList());
        if (frames.size() != 2 || frames.get(0).getDeclaringClass() != ItemCapability.class
            || !frames.get(0).getMethodName().equals("ftboss$nativeHasItemProviders")
            || !frames.get(0).getDescriptor().equals("(Lnet/minecraft/world/item/Item;)Z")
            || frames.get(1).getDeclaringClass() != NativeItemCapabilityState.class
            || !frames.get(1).getMethodName().equals("hasNoProviders")) {
            throw new IllegalStateException("Native item capability field observation denied");
        }
    }

    public static boolean hasNoProviders(ItemStack stack) {
        StackFrame caller = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(stream -> stream.skip(1L).findFirst().orElseThrow());
        if (caller.getDeclaringClass() != ItemStack.class || !caller.getMethodName().equals("ftboss$ownerRepairInitializedNoProviders")
            || !caller.getDescriptor().equals("()Z")) {
            throw new IllegalStateException("Native item capability guard caller denied");
        }
        if (stack == null) return false;
        Item item = stack.getItem();
        if (item == null) return false;
        List<ItemCapability<?, ?>> capabilities = ItemCapability.getAll();
        if (capabilities.size() > 65536) return false;
        IdentityHashMap<ItemCapability<?, ?>, Boolean> seen = new IdentityHashMap<>();
        for (ItemCapability<?, ?> capability : capabilities) {
            if (seen.put(capability, Boolean.TRUE) != null || !((Object)capability instanceof NativeItemCapabilityFields fields)
                || fields.ftboss$nativeHasItemProviders(item)) return false;
        }
        List<ItemCapability<?, ?>> current = ItemCapability.getAll();
        if (current.size() != capabilities.size() || stack.getItem() != item) return false;
        for (ItemCapability<?, ?> capability : current) {
            if (seen.remove(capability) == null || !((Object)capability instanceof NativeItemCapabilityFields fields)
                || fields.ftboss$nativeHasItemProviders(item)) return false;
        }
        return seen.isEmpty();
    }
}
