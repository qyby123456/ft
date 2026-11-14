package net.farmingtales.ftboss.item;

import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class NativeWearComponentRollback {
    private NativeWearComponentRollback() {
    }

    public static CompoundTag snapshot(ItemStack stack) {
        return snapshot(stack, NativeFoundationItemData.registries());
    }

    public static CompoundTag snapshot(ItemStack stack, HolderLookup.Provider registries) {
        PatchedDataComponentMap storage = storage(stack);
        Tag encoded = DataComponentPatch.CODEC.encodeStart(Objects.requireNonNull(registries, "registries").createSerializationContext(NbtOps.INSTANCE), storage.asPatch())
            .getOrThrow(error -> new IllegalArgumentException("Invalid native wear rollback component snapshot: " + error));
        if (!(encoded instanceof CompoundTag compound)) {
            throw new IllegalArgumentException("Native wear rollback requires a real component patch compound");
        }
        return compound.copy();
    }

    public static void restore(ItemStack stack, CompoundTag snapshot) {
        restore(stack, snapshot, NativeFoundationItemData.registries());
    }

    public static void restore(ItemStack stack, CompoundTag snapshot, HolderLookup.Provider registries) {
        PatchedDataComponentMap storage = storage(stack);
        DataComponentPatch patch = DataComponentPatch.CODEC.parse(Objects.requireNonNull(registries, "registries").createSerializationContext(NbtOps.INSTANCE), Objects.requireNonNull(snapshot, "snapshot").copy())
            .getOrThrow(error -> new IllegalArgumentException("Invalid native wear rollback component snapshot: " + error));
        storage.restorePatch(patch);
    }

    private static PatchedDataComponentMap storage(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        if (stack.isEmpty() || !(stack.getComponents() instanceof PatchedDataComponentMap storage)) {
            throw new IllegalArgumentException("Native wear rollback requires actual nonempty component storage");
        }
        return storage;
    }
}
