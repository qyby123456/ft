/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

public final class FinalBossDamageGuard {
    public static final float MAX_FINAL_DAMAGE = 300.0f;

    private FinalBossDamageGuard() {
    }

    public static boolean applies(LivingEntity livingEntity) {
        if (livingEntity instanceof RatBossEntity) {
            return false;
        }
        if (livingEntity instanceof EasterBossEntity) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)livingEntity;
            return !easterBossEntity.isMini();
        }
        if (livingEntity instanceof RatBossEntity) {
            RatBossEntity ratBossEntity = (RatBossEntity)livingEntity;
            return !ratBossEntity.isMinion();
        }
        return livingEntity instanceof SugarQueenEntity || livingEntity instanceof MeatMountainEntity || livingEntity instanceof TeaTreeBossEntity || livingEntity instanceof WineBarrelBossEntity || livingEntity instanceof DefectCoreEntity || livingEntity instanceof CompostBossEntity || livingEntity instanceof AppleBossEntity || livingEntity instanceof BengerEntity || livingEntity instanceof TideFishBossEntity;
    }

    public static boolean blocked(float f) {
        return !Float.isFinite(f) || f > 300.0f;
    }

    public static float settled(LivingEntity livingEntity, DamageSource damageSource, float f) {
        if (livingEntity == null || !FinalBossDamageGuard.applies(livingEntity) || livingEntity.m_9236_().m_5776_() || damageSource.m_276093_(DamageTypes.f_286979_)) {
            return f;
        }
        return FinalBossDamageGuard.blocked(f) ? 0.0f : f;
    }
}

