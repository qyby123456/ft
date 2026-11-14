package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem;
import net.farmingtales.ftboss.util.MaterialCrossbowRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/** 弓弩被动箭伤；2026-10-25 每箭独立冻结成长与材料倍率，不把被动收益绑定到可撤销技能快照。 */
public final class ArrowPassiveDamage {
    private static final String APPLIED = "FTBossPassiveArrowDamage";
    private static final double BOW_SPEED = 3.0D;

    private ArrowPassiveDamage() {}

    public static AbstractArrow apply(AbstractArrow arrow, ItemStack weapon) {
        boolean crossbow = weapon.getItem() instanceof CrossbowItem;
        if (!(arrow.getOwner() instanceof ServerPlayer) || !crossbow && !(weapon.getItem() instanceof BowItem)
                || FoodWeaponLifecycle.isBroken(weapon) || arrow.getPersistentData().getBoolean(APPLIED)) return arrow;
        double damage = arrow.getBaseDamage();
        if (weapon.getItem() instanceof MaterialCrossbowItem material) damage += material.profile().arrowDamageAdjustment();
        if (FoodBlessingCombat.eligible(weapon)) {
            double extra = (crossbow ? RangedHybridCombat.bonuses(weapon) : BowGrowthCombat.bonuses(weapon)).damage()
                    + WeaponMaterialBonuses.bonuses(weapon).attackDamage()
                    + (crossbow ? 0.75D * FoodBlessingCombat.level(weapon, "cleaver_might") : 0.0D);
            damage += Math.max(0.0D, extra) / (crossbow ? MaterialCrossbowRules.ARROW_SPEED : BOW_SPEED);
        }
        double material = FoodWeaponFamilyBuild.damageMultiplier(weapon);
        double evolution = FoodWeaponEvolution.damageEvolutionMultiplier(weapon);
        if (!Double.isFinite(evolution) || evolution <= 0.0D) evolution = 1.0D;
        double result = damage * material * evolution;
        if (Double.isFinite(result) && result >= 0.0D) {
            arrow.setBaseDamage(result);
            arrow.getPersistentData().putBoolean(APPLIED, true);
        }
        return arrow;
    }

    public static double remainingMaterial(double multiplier, LivingHurtEvent event) {
        return event.getSource().getDirectEntity() instanceof AbstractArrow arrow && arrow.getPersistentData().getBoolean(APPLIED)
                ? 1.0D : multiplier;
    }
}
