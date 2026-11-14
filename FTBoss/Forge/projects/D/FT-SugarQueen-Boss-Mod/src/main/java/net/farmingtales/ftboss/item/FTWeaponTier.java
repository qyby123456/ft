package net.farmingtales.ftboss.item;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 五把 Boss 主题武器共用的材质等级。
 * <p>数值是**占位**(对齐铁级略强)：耐久 1500 / 挖掘速度 6 / 附加伤害 3 / 等级 2 / 附魔 18。
 * 用户若要各自的特性(不同伤害、附带食物 buff、召唤物互动等)，改 {@link net.farmingtales.ftboss.item.FTBossItems}
 * 里各自的 {@code SwordItem} 参数即可，或换成自定义 Item 类。
 */
public enum FTWeaponTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return 1500;
    }

    @Override
    public float getSpeed() {
        return 6.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 3.0F;
    }

    @Override
    public int getLevel() {
        return Tiers.IRON.getLevel();
    }

    @Override
    public int getEnchantmentValue() {
        return 18;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Items.IRON_INGOT);
    }
}
