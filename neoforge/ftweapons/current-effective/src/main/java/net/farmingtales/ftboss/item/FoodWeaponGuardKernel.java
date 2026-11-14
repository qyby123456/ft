package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.boss.ai.FinalBossDamageGuard;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

final class FoodWeaponGuardKernel {
    private FoodWeaponGuardKernel() {
    }

    static float finish(LivingEntity var0, DamageSource var1, float var2) {
        return FinalBossDamageGuard.settled(var0, var1, var2);
    }
}
