package net.farmingtales.ftboss.item;

import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class NativeOwnerRepairItemData {
    private NativeOwnerRepairItemData() {
    }

    public static CompoundTag rawElementForObservation(ItemStack stack, String key) {
        CompoundTag root = NativeSourceLedgerItemData.rawCustomDataForObservation(Objects.requireNonNull(stack, "stack"));
        return root == null || !root.contains(key, 10) ? null : root.getCompound(key);
    }

    public static PaymentComponents paymentComponents(ItemStack stack) {
        return paymentComponents(stack, NativeFoundationItemData.registries());
    }

    public static PaymentComponents paymentComponents(ItemStack stack, HolderLookup.Provider registries) {
        return new PaymentComponents(stack, registries);
    }

    public static final class PaymentComponents {
        private final ItemStack actual;
        private final Item item;
        private final HolderLookup.Provider registries;
        private final PatchedDataComponentMap storage;
        private final DataComponentMap values;
        private final Tag encodedPatch;

        private PaymentComponents(ItemStack stack, HolderLookup.Provider registries) {
            this.actual = Objects.requireNonNull(stack, "stack");
            this.registries = Objects.requireNonNull(registries, "registries");
            if (stack.isEmpty() || !(stack.getComponents() instanceof PatchedDataComponentMap components)) {
                throw new IllegalArgumentException("Repair payment requires the actual nonempty native component storage");
            }
            this.item = stack.getItem();
            this.storage = components;
            this.values = DataComponentMap.builder().addAll(components).build();
            this.encodedPatch = encode().copy();
        }

        private Tag encode() {
            return DataComponentPatch.CODEC.encodeStart(this.registries.createSerializationContext(NbtOps.INSTANCE), this.storage.asPatch())
                .getOrThrow(error -> new IllegalArgumentException("Invalid native repair payment component patch: " + error));
        }

        public boolean matches(ItemStack stack) {
            return stack == this.actual
                && (stack.isEmpty() || stack.getItem() == this.item && stack.getComponents() == this.storage)
                && this.values.keySet().equals(this.storage.keySet())
                && this.values.stream().allMatch(component -> Objects.equals(component.value(), this.storage.get(component.type())))
                && this.encodedPatch.equals(encode());
        }
    }
}
