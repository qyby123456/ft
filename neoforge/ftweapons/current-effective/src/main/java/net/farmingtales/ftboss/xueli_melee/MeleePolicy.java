package net.farmingtales.ftboss.xueli_melee;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.LivingEntity;

public final class MeleePolicy {
    private MeleePolicy() {
    }

    public static boolean preferRanged(boolean var0, boolean var1, EasterBossEntity var2) {
        if (var0 && !var1 && var2 != null && var2.getVariant() == 10 && !var2.isMini() && !var2.isGiant()) {
            LivingEntity var3 = var2.getTarget();
            return var3 == null
                ? var0
                : MeleeRules.preferRanged(var0, var1, var2.getVariant(), var2.isMini(), var2.isGiant(), var2.distanceToSqr(var3), var3.getY() - var2.getY());
        } else {
            return var0;
        }
    }
}
