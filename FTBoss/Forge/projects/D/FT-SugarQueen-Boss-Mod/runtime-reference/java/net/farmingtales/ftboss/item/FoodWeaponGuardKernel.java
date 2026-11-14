/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.boss.ai.FinalBossDamageGuard;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

final class FoodWeaponGuardKernel {
    private FoodWeaponGuardKernel() {
    }

    static float finish(LivingEntity livingEntity, DamageSource damageSource, float f) {
        return FinalBossDamageGuard.settled(livingEntity, damageSource, f);
    }
}

