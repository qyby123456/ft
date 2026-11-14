package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.minecraft.world.item.*;

/** 只移植原生材料方法；2026-10-20 与主源码已有的 TieredItem 安全兜底对齐。 */
public final class SwordCavityBuild {
    private static Material materialFor(ItemStack stack) { throw new UnsupportedOperationException(); }

    private static Material nativeMaterial(ItemStack stack, Slot slot) {
        if (slot == Slot.GRIP || slot == Slot.BINDING) return Material.WOOD;
        if (slot.ordinal() >= 4) return Material.ORIGINAL;
        if (!(stack.getItem() instanceof TieredItem tiered)) return Material.ORIGINAL;
        var tier = tiered.getTier();
        if (tier == Tiers.WOOD) return Material.WOOD;
        if (tier == Tiers.STONE) return Material.STONE;
        if (tier == Tiers.IRON) return Material.IRON;
        if (tier == Tiers.GOLD) return Material.GOLD;
        if (tier == Tiers.DIAMOND) return Material.DIAMOND;
        if (tier == Tiers.NETHERITE) return Material.NETHERITE;
        for (ItemStack ingredient : tier.getRepairIngredient().getItems()) {
            Material material = materialFor(ingredient);
            if (material != Material.ORIGINAL) return material;
        }
        return Material.ORIGINAL;
    }

}
