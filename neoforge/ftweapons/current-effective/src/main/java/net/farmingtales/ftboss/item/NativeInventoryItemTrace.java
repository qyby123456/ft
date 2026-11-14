package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public final class NativeInventoryItemTrace {
    private NativeInventoryItemTrace() {
    }

    public static Value quote(CompoundTag inventoryEntry) {
        return project(NativeExpectedRawFields.observe(inventoryEntry));
    }

    static Value project(Value inventoryEntry) {
        if (inventoryEntry == null || inventoryEntry.kind != 10
            || FoodWeaponExpectedTrace.field(inventoryEntry, "Count") != null
            || FoodWeaponExpectedTrace.field(inventoryEntry, "tag") != null
            || FoodWeaponExpectedTrace.field(inventoryEntry, "ForgeCaps") != null) {
            return null;
        }
        Value id = FoodWeaponExpectedTrace.field(inventoryEntry, "id");
        Value count = FoodWeaponExpectedTrace.field(inventoryEntry, "count");
        Value components = FoodWeaponExpectedTrace.field(inventoryEntry, "components");
        if (id == null || id.kind != 8 || ResourceLocation.tryParse(id.string) == null
            || count != null && (count.kind != 3 || count.bits != 1L)
            || components != null && components.kind != 10) {
            return null;
        }
        Value actualCount = count == null ? FoodWeaponExpectedTrace.number(3, 1L) : count;
        return components == null
            ? FoodWeaponExpectedTrace.compound(new String[]{"count", "id"}, new Value[]{actualCount, id})
            : FoodWeaponExpectedTrace.compound(new String[]{"components", "count", "id"}, new Value[]{components, actualCount, id});
    }

    static Value customDataRoot(Value inventoryEntry) {
        Value projected = project(inventoryEntry);
        Value components = FoodWeaponExpectedTrace.field(projected, "components");
        Value custom = FoodWeaponExpectedTrace.field(components, "minecraft:custom_data");
        return custom != null && custom.kind == 10 ? custom : null;
    }

    static String[] customDataPath() {
        return new String[]{"components", "minecraft:custom_data"};
    }

    static Value wearProjection(Value inventoryEntry) {
        Value projected = project(inventoryEntry);
        Value components = FoodWeaponExpectedTrace.field(projected, "components");
        Value custom = FoodWeaponExpectedTrace.field(components, "minecraft:custom_data");
        return custom == null || custom.kind != 10 ? null : projected;
    }

    public static CompoundTag image(CompoundTag inventoryEntry) {
        Value projected = quote(inventoryEntry);
        return projected == null ? null : (CompoundTag)FoodWeaponExpectedTrace.materialize(projected);
    }
}
