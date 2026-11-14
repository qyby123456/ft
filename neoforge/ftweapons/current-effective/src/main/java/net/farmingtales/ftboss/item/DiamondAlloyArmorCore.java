package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordFunctionRules.Profile;
import net.farmingtales.ftboss.util.SwordFunctionRules.Values;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Origin;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;

public final class DiamondAlloyArmorCore {
    private static final Set<String> ARMOR = Set.of("kubejs:ne_helmet", "kubejs:ne_chestplate", "kubejs:ne_leggings", "kubejs:ne_boots");

    private DiamondAlloyArmorCore() {
    }

    public static boolean applies(ItemStack var0) {
        ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getItem());
        return EquipmentWeaponBuild.applies(var0) && var1 != null && ARMOR.contains(var1.toString());
    }

    public static int index(ItemStack var0, List<Piece> var1) {
        if (!applies(var0)) {
            return -1;
        } else {
            String var2 = BuiltInRegistries.ITEM.getKey(var0.getItem()).toString();

            for (int var3 = 0; var3 < var1.size(); var3++) {
                Piece var4 = (Piece)var1.get(var3);
                if (var4.count() == 1 && var4.origin() == Origin.THEME && var4.itemId().equals(var2)) {
                    return var3;
                }
            }

            return -1;
        }
    }

    public static boolean fixed(ItemStack var0, int var1) {
        return applies(var0) && var1 >= 0 && var1 == index(var0, EquipmentWeaponBuild.parts(var0));
    }

    public static List<Piece> parts(ItemStack var0, List<Piece> var1) {
        if (applies(var0) && index(var0, var1) < 0) {
            CompoundTag var2 = FoodWeaponData.read(var0, "FTBossSwordStructure");
            int var3 = var1.size() - 1;

            while (true) {
                if (var3 < 0) {
                    return var1;
                }

                if (((Piece)var1.get(var3)).count() == 0) {
                    if (var2 == null) {
                        break;
                    }

                    CompoundTag var4 = var2.getCompound("Cells").getCompound(Slot.byIndex(var3).name());
                    if (!var4.getString("State").equals("installed") && !var4.contains("Item")) {
                        break;
                    }
                }

                var3--;
            }

            ArrayList var5 = new ArrayList(var1);
            var5.set(var3, new Piece(BuiltInRegistries.ITEM.getKey(var0.getItem()).toString(), 1, Origin.THEME));
            return List.copyOf(var5);
        } else {
            return var1;
        }
    }

    public static Profile profile(Profile var0, Values var1, int var2) {
        if (var2 < 0) {
            return var0;
        } else {
            ArrayList var3 = new ArrayList<>(Collections.nCopies(27, SwordFunctionRules.ZERO));
            var3.set(var2, var1);
            return new Profile(var0.minimum(), Collections.nCopies(27, SwordFunctionRules.ZERO), var0.unlockLevels(), var0.materials(), var3, var0.itemValues());
        }
    }

    public static ItemStack display(ItemStack var0) {
        ItemStack var1 = new ItemStack(var0.getItem());
        var1.set(DataComponents.CUSTOM_NAME, Component.translatable("item.ftboss.diamond_alloy_armor_core"));
        return var1;
    }
}
