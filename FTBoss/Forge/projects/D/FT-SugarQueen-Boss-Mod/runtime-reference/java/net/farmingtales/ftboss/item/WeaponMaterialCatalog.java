/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class WeaponMaterialCatalog {
    private WeaponMaterialCatalog() {
    }

    public static FoodWeaponBuildRules.Material materialFor(ItemStack itemStack) {
        if (itemStack.m_41619_() || itemStack.m_41763_()) {
            return FoodWeaponBuildRules.Material.ORIGINAL;
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        if (resourceLocation == null) {
            return FoodWeaponBuildRules.Material.ORIGINAL;
        }
        return WeaponMaterialCatalogRules.resolve(resourceLocation.toString(), itemStack.m_204131_().map(tagKey -> tagKey.f_203868_().toString()).toList());
    }

    public static WeaponMaterialCatalogRules.Group group(FoodWeaponBuildRules.Material material) {
        return WeaponMaterialCatalogRules.group(material);
    }

    public static boolean boss(FoodWeaponBuildRules.Material material) {
        return WeaponMaterialCatalogRules.boss(material);
    }

    public static Component source(FoodWeaponBuildRules.Material material) {
        return Component.m_237115_((String)("source.ftboss.material." + material.id()));
    }
}

