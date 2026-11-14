/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

final class ZicaiTargetIsolation {
    private ZicaiTargetIsolation() {
    }

    public static void apply(EasterBossEntity easterBossEntity, ServerLevel serverLevel, LivingEntity livingEntity) {
        ManorBalanceHooks.zicaiAttack(easterBossEntity, serverLevel, livingEntity);
    }
}

