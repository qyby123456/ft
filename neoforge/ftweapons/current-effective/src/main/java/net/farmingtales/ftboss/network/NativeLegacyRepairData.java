package net.farmingtales.ftboss.network;

import java.util.Objects;
import net.farmingtales.ftboss.item.NativeFoundationItemData;
import net.farmingtales.ftboss.item.NativeRepairStackImage;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

final class NativeLegacyRepairData {
    private final ItemStack owner;
    private final HolderLookup.Provider registries;
    private final CompoundTag image;

    private NativeLegacyRepairData(ItemStack stack, HolderLookup.Provider registries) {
        this.owner = Objects.requireNonNull(stack);
        this.registries = Objects.requireNonNull(registries);
        this.image = NativeFoundationItemData.image(stack, registries);
    }

    static NativeLegacyRepairData capture(ItemStack stack, HolderLookup.Provider registries) {
        return new NativeLegacyRepairData(stack, registries);
    }

    boolean changed(ItemStack stack) {
        this.requireOwner(stack);
        CompoundTag current = NativeFoundationItemData.image(stack, this.registries);
        current.putInt("count", NativeRepairStackImage.count(this.image));
        return !this.image.equals(current);
    }

    void restore(ItemStack stack) {
        this.requireOwner(stack);
        ItemStack restored = NativeRepairStackImage.decode(this.registries, this.image.copy());
        if (restored.isEmpty() || restored.getItem() != stack.getItem() || !(stack.getComponents() instanceof PatchedDataComponentMap components)) {
            throw new IllegalStateException("Original legacy repair native item identity changed during compensation");
        }
        components.restorePatch(restored.getComponentsPatch());
    }

    private void requireOwner(ItemStack stack) {
        if (stack != this.owner) {
            throw new IllegalArgumentException("Original legacy repair compensation belongs to another stack");
        }
    }
}
