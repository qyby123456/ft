package net.farmingtales.ftboss.item;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.CustomData;

public final class NativeSourceLedgerItemData {
    private static final String COMPONENTS = "components";
    private static final String CUSTOM_DATA = "minecraft:custom_data";
    private static final String CHARGED = "minecraft:charged_projectiles";
    private static final String EVOLUTION = "FTBossFoodEvolution";

    private NativeSourceLedgerItemData() {
    }

    public static CompoundTag rawCustomDataForObservation(ItemStack stack) {
        CustomData data = Objects.requireNonNull(stack, "stack").get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.getUnsafe();
    }

    public static CompoundTag imageData(CompoundTag image) {
        return NativeRepairStackImage.data(image);
    }

    private static CompoundTag editableData(CompoundTag image) {
        NativeRepairStackImage.data(image);
        CompoundTag components = image.getCompound(COMPONENTS);
        if (components.contains(CUSTOM_DATA) && !components.contains(CUSTOM_DATA, 10)) {
            throw new IllegalArgumentException("Invalid native ledger custom-data image");
        }
        CompoundTag data = components.getCompound(CUSTOM_DATA);
        components.remove("!" + CUSTOM_DATA);
        components.put(CUSTOM_DATA, data);
        image.put(COMPONENTS, components);
        return data;
    }

    private static CompoundTag evolutionData(CompoundTag image, String profile) {
        CompoundTag data = editableData(image);
        CompoundTag evolution = data.getCompound(EVOLUTION);
        if (profile != null && !evolution.contains("Profile", 8)) {
            evolution.putString("Profile", profile);
        }
        data.put(EVOLUTION, evolution);
        return evolution;
    }

    public static CompoundTag projectTimerEntry(CompoundTag image, String profile) {
        CompoundTag projected = Objects.requireNonNull(image, "image").copy();
        evolutionData(projected, profile);
        return projected;
    }

    public static CompoundTag projectTimer(CompoundTag image, String profile, String key, Tag value) {
        CompoundTag projected = Objects.requireNonNull(image, "image").copy();
        evolutionData(projected, profile).put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value").copy());
        return projected;
    }

    public static CompoundTag projectWolfEntry(CompoundTag image) {
        CompoundTag projected = Objects.requireNonNull(image, "image").copy();
        editableData(projected);
        return projected;
    }

    public static CompoundTag projectWolfToken(CompoundTag image, UUID token) {
        CompoundTag projected = Objects.requireNonNull(image, "image").copy();
        editableData(projected).putUUID("FTBossFoodSocketWeaponToken", Objects.requireNonNull(token, "token"));
        return projected;
    }

    public static void removeDamageFromComparison(CompoundTag image) {
        NativeRepairStackImage.data(image);
        image.getCompound(COMPONENTS).remove("minecraft:damage");
    }

    public static boolean uncharged(HolderLookup.Provider registries, CompoundTag image) {
        ItemStack stack = NativeRepairStackImage.decode(Objects.requireNonNull(registries, "registries"), image);
        return !stack.isEmpty() && stack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).isEmpty();
    }

    public static void removeChargedFromComparison(CompoundTag image) {
        NativeRepairStackImage.data(image);
        CompoundTag components = image.getCompound(COMPONENTS);
        components.remove(CHARGED);
        components.remove("!" + CHARGED);
    }
}
