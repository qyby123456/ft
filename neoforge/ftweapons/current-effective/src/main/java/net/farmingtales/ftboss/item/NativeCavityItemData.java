package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class NativeCavityItemData {
    private static final String STRUCTURE = "FTBossSwordStructure";
    private static final String MATERIALS = "FTBossWeaponMaterials";

    private NativeCavityItemData() {
    }

    public static void initialize(ItemStack stack, CompoundTag cells, int revision) {
        FoodWeaponData.edit(stack, STRUCTURE, structure -> {
            structure.putInt("Version", 2);
            structure.putInt("Revision", revision);
            if (structure.getString("NativeItem").isEmpty()) {
                structure.putString("NativeItem", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            }
            structure.put("Cells", cells.copy());
        });
    }

    public static void writeCell(ItemStack stack, Slot slot, Cell cell, CompoundTag image) {
        CompoundTag data = NativeFoundationItemData.customData(stack);
        if (data == null) {
            data = new CompoundTag();
        }
        CompoundTag structure = data.getCompound(STRUCTURE);
        CompoundTag cells = structure.getCompound("Cells");
        CompoundTag entry = new CompoundTag();
        entry.putString("State", cell.state().name().toLowerCase(java.util.Locale.ROOT));
        entry.putString("Material", cell.material().id());
        entry.putBoolean("EverPresent", cell.present() || cells.getCompound(slot.name()).getBoolean("EverPresent"));
        if (cell.installed() && image != null) {
            entry.put("Item", image.copy());
        }
        cells.put(slot.name(), entry);
        structure.put("Cells", cells);
        data.put(STRUCTURE, structure);
        CompoundTag materials = data.getCompound(MATERIALS);
        if (slot.legacyPart != null) {
            materials.putString(slot.legacyPart.name(), cell.installed() ? cell.material().id() : Material.ORIGINAL.id());
        }
        data.put(MATERIALS, materials);
        NativeFoundationItemData.setCustomData(stack, data);
    }

    public static void enableGrid(ItemStack stack) {
        FoodWeaponData.edit(stack, STRUCTURE, structure -> {
            structure.putBoolean("GridValues", true);
            structure.putBoolean("LinksEnabled", true);
        });
    }

    public static void copyBuildData(ItemStack target, ItemStack source) {
        CompoundTag data = NativeFoundationItemData.customData(target);
        if (data == null) {
            data = new CompoundTag();
        }
        data.put(STRUCTURE, NativeFoundationItemData.element(source, STRUCTURE).copy());
        data.put(MATERIALS, NativeFoundationItemData.element(source, MATERIALS).copy());
        NativeFoundationItemData.setCustomData(target, data);
    }

    public static ItemStack decode(HolderLookup.Provider registries, CompoundTag image) {
        try {
            return NativeRepairStackImage.decode(registries, image);
        } catch (IllegalArgumentException invalidImage) {
            return ItemStack.EMPTY;
        }
    }
}
