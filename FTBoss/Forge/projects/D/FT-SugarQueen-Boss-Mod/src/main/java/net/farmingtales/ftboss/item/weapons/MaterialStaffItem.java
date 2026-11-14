package net.farmingtales.ftboss.item.weapons;

import io.redspace.ironsspellbooks.item.CastingItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

public final class MaterialStaffItem extends CastingItem {
    private final Tier tier;

    public MaterialStaffItem(Tier tier, Properties properties) {
        super(properties.stacksTo(1).durability(tier.getUses()));
        this.tier = tier;
    }

    @Override
    public int getEnchantmentValue() {
        return tier.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack weapon, ItemStack ingredient) {
        return tier.getRepairIngredient().test(ingredient) || super.isValidRepairItem(weapon, ingredient);
    }
}
