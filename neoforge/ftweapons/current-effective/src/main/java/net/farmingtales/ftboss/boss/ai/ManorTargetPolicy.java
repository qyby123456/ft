package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class ManorTargetPolicy {
    private ManorTargetPolicy() {
    }

    public static boolean preferRanged(boolean var0, boolean var1, EasterBossEntity var2) {
        if (var0 && !var1 && var2 != null && ManorTargetRules.hybrid(var2.getVariant()) && !var2.isMini() && !var2.isGiant()) {
            LivingEntity var3 = var2.getTarget();
            if (var3 == null) {
                return var0;
            } else {
                double var4 = var2.allowsParallelMelee() ? 12.25 : 9.0;
                return ManorTargetRules.preferRanged(
                    var0, var1, var2.getVariant(), var2.isMini(), var2.isGiant(), var2.distanceToSqr(var3), var3.getY() - var2.getY(), var4
                );
            }
        } else {
            return var0;
        }
    }

    private static boolean scopedThreat(LivingEntity var0) {
        if (var0 instanceof EasterBossEntity var1 && !var1.isMini() && !var1.isGiant() && var1.getVariant() != 9) {
            return ManorMultiplayerBalance.isManagedManorBoss(var0);
        }

        return false;
    }

    public static boolean filterThreat(boolean var0, LivingEntity var1, LivingEntity var2) {
        return var0 && scopedThreat(var1) ? ManorMultiplayerBalance.validManorTarget(var1, var2) : var0;
    }

    public static boolean shouldClearTracked(boolean var0, Mob var1, LivingEntity var2) {
        return var0 && scopedThreat(var1) ? !ManorMultiplayerBalance.validManorTarget(var1, var2) : var0;
    }
}
