package net.farmingtales.ftboss.item;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public final class NativeProgressionItemData {
    private static final String ROOT = "FTBossFoodEvolution";

    private NativeProgressionItemData() {
    }

    public static CompoundTag snapshot(CompoundTag var0, List<String> progressionKeys) {
        CompoundTag var1 = new CompoundTag();

        for (String var3 : progressionKeys) {
            if (var0.contains(var3)) {
                var1.put(var3, var0.get(var3).copy());
            }
        }

        return var1;
    }

    public static void normalizeExperience(ItemStack stack, int experience) {
        FoodWeaponData.edit(stack, ROOT, evolution -> {
            evolution.putInt("Experience", experience);
            evolution.putInt("ProgressionVersion", 2);
        });
    }

    public static void appendStage(ItemStack stack, ListTag history, CompoundTag stage, int selectedStage) {
        history.add(stage);
        FoodWeaponData.edit(stack, ROOT, evolution -> {
            evolution.put("StageHistory", history.copy());
            evolution.putInt("SelectedStage", selectedStage);
        });
    }

    public static void finishExperience(ItemStack stack) {
        FoodWeaponData.edit(stack, ROOT, evolution -> evolution.putInt("Experience", 0));
    }
}
