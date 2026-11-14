/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ai.ManorMultiplayerBalance;
import net.farmingtales.ftboss.boss.ai.ManorTargetRules;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class ManorTargetPolicy {
    private ManorTargetPolicy() {
    }

    public static boolean preferRanged(boolean bl, boolean bl2, EasterBossEntity easterBossEntity) {
        if (!bl || bl2 || easterBossEntity == null || !ManorTargetRules.hybrid(easterBossEntity.getVariant()) || easterBossEntity.isMini() || easterBossEntity.isGiant()) {
            return bl;
        }
        LivingEntity livingEntity = easterBossEntity.m_5448_();
        if (livingEntity == null) {
            return bl;
        }
        double d = easterBossEntity.allowsParallelMelee() ? 12.25 : 9.0;
        return ManorTargetRules.preferRanged(bl, bl2, easterBossEntity.getVariant(), easterBossEntity.isMini(), easterBossEntity.isGiant(), easterBossEntity.m_20280_((Entity)livingEntity), livingEntity.m_20186_() - easterBossEntity.m_20186_(), d);
    }

    private static boolean scopedThreat(LivingEntity livingEntity) {
        EasterBossEntity easterBossEntity;
        if (!(livingEntity instanceof EasterBossEntity) || (easterBossEntity = (EasterBossEntity)livingEntity).isMini() || easterBossEntity.isGiant() || easterBossEntity.getVariant() == 9) {
            return false;
        }
        return ManorMultiplayerBalance.isManagedManorBoss(livingEntity);
    }

    public static boolean filterThreat(boolean bl, LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (!bl || !ManorTargetPolicy.scopedThreat(livingEntity)) {
            return bl;
        }
        return ManorMultiplayerBalance.validManorTarget(livingEntity, livingEntity2);
    }

    public static boolean shouldClearTracked(boolean bl, Mob mob, LivingEntity livingEntity) {
        if (!bl || !ManorTargetPolicy.scopedThreat((LivingEntity)mob)) {
            return bl;
        }
        return !ManorMultiplayerBalance.validManorTarget((LivingEntity)mob, livingEntity);
    }
}

