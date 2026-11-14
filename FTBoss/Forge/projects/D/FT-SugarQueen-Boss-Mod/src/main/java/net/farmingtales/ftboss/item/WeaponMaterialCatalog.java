package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules;
import net.farmingtales.ftboss.util.WeaponMaterialCatalogRules.Group;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/** 构筑来源解析与 UI 分类；精确遗物/锭白名单优先，2026-10-12 接入 Forge 材料标签且不直接依赖其它模组。 */
public final class WeaponMaterialCatalog {
    private WeaponMaterialCatalog() {}

    public static Material materialFor(ItemStack stack) {
        if (stack.isEmpty() || stack.isDamageableItem()) return Material.ORIGINAL;
        var itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) return Material.ORIGINAL;
        return WeaponMaterialCatalogRules.resolve(itemId.toString(),
                stack.getTags().map(tag -> tag.location().toString()).toList());
    }

    public static Group group(Material material) {
        return WeaponMaterialCatalogRules.group(material);
    }

    public static boolean boss(Material material) {
        return WeaponMaterialCatalogRules.boss(material);
    }

    public static Component source(Material material) {
        return Component.translatable("source.ftboss.material." + material.id());
    }
}
