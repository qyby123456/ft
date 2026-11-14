/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.LivingEntity;

public final class ZicaiMeleePassive {
    private ZicaiMeleePassive() {
    }

    public static void apply(EasterBossEntity easterBossEntity, LivingEntity livingEntity) {
        ManorBalanceHooks.zicaiMelee(easterBossEntity, livingEntity);
    }
}

