package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;

public final class ArrowPassiveDamage {
    private static final String APPLIED = "FTBossPassiveArrowDamage";
    private static final double BOW_SPEED = 3.0;

    private ArrowPassiveDamage() {
    }

    public static AbstractArrow apply(AbstractArrow var0, ItemStack var1) {
        boolean var2 = var1.getItem() instanceof CrossbowItem;
        if (var0.getOwner() instanceof ServerPlayer
            && (var2 || var1.getItem() instanceof BowItem)
            && !FoodWeaponLifecycle.isBroken(var1)
            && !var0.getPersistentData().getBoolean("FTBossPassiveArrowDamage")) {
            double var3 = var0.getBaseDamage();
            if (var1.getItem() instanceof MaterialCrossbowItem var5) {
                var3 += NativeEquipmentAttributes.crossbowProfile(var5).arrowDamageAdjustment();
            }

            if (FoodBlessingCombat.eligible(var1)) {
                double var11 = (var2 ? RangedHybridCombat.bonuses(var1) : BowGrowthCombat.bonuses(var1)).damage()
                    + WeaponMaterialBonuses.bonuses(var1).attackDamage()
                    + (var2 ? 0.75 * (double)FoodBlessingCombat.level(var1, "cleaver_might") : 0.0);
                var3 += Math.max(0.0, var11) / (var2 ? 3.15F : 3.0);
            }

            double var12 = FoodWeaponFamilyBuild.damageMultiplier(var1);
            double var7 = FoodWeaponEvolution.damageEvolutionMultiplier(var1);
            if (!Double.isFinite(var7) || var7 <= 0.0) {
                var7 = 1.0;
            }

            double var9 = var3 * var12 * var7;
            if (Double.isFinite(var9) && var9 >= 0.0) {
                var0.setBaseDamage(var9);
                var0.getPersistentData().putBoolean("FTBossPassiveArrowDamage", true);
            }

            return var0;
        } else {
            return var0;
        }
    }

    public static double remainingMaterial(double var0, BeforeMitigation var2) {
        if (var2.getSource().getDirectEntity() instanceof AbstractArrow var3 && var3.getPersistentData().getBoolean("FTBossPassiveArrowDamage")) {
            return 1.0;
        }

        return var0;
    }
}
