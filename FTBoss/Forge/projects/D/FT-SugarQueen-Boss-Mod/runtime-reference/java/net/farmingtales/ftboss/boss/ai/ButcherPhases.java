/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.MeatBoss;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.ButcherPhaseRules;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public final class ButcherPhases {
    private static final String PHASE = "ftboss_butcher_phase";
    private static final String ELAPSED = "ftboss_butcher_phase_ticks";
    private static final String DODGE_CD = "ftboss_butcher_dodge_cd";
    private static final String BYPASS = "ftboss_butcher_phase_bypass";
    private static final UUID HEALTH_MODIFIER = UUID.fromString("fe0a6bc2-4ab2-40b2-971c-28c105e27533");

    private ButcherPhases() {
    }

    public static int phase(MeatMountainEntity meatMountainEntity) {
        return ButcherPhaseRules.normalize(meatMountainEntity.getPersistentData().m_128451_(PHASE));
    }

    public static int remainingSeconds(MeatMountainEntity meatMountainEntity) {
        int n = Math.max(0, meatMountainEntity.getPersistentData().m_128451_(ELAPSED));
        return Math.max(0, (1200 - n + 19) / 20);
    }

    public static void tick(MeatMountainEntity meatMountainEntity) {
        if (meatMountainEntity.m_9236_().f_46443_ || !meatMountainEntity.m_6084_()) {
            return;
        }
        CompoundTag compoundTag = meatMountainEntity.getPersistentData();
        if (!compoundTag.m_128441_(PHASE)) {
            compoundTag.m_128405_(PHASE, 1);
            BossRage.reset((LivingEntity)meatMountainEntity);
            meatMountainEntity.m_21219_();
        }
        ButcherPhases.applyHealth(meatMountainEntity);
        int n = compoundTag.m_128451_(DODGE_CD);
        if (n > 0) {
            compoundTag.m_128405_(DODGE_CD, n - 1);
        }
        if (ButcherPhases.phase(meatMountainEntity) != 3) {
            return;
        }
        int n2 = Math.min(1200, Math.max(0, compoundTag.m_128451_(ELAPSED))) + 1;
        compoundTag.m_128405_(ELAPSED, n2);
        if (ButcherPhaseRules.expired(ButcherPhases.phase(meatMountainEntity), n2)) {
            ButcherPhases.enter(meatMountainEntity, 1);
            return;
        }
        float f = ButcherPhaseRules.regeneration(ButcherPhases.phase(meatMountainEntity), n2, meatMountainEntity.m_21233_(), meatMountainEntity.m_21233_() - meatMountainEntity.m_21223_());
        if (f > 0.0f) {
            meatMountainEntity.m_5634_(f);
            Level level = meatMountainEntity.m_9236_();
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123750_, meatMountainEntity.m_20185_(), meatMountainEntity.m_20186_() + 1.5, meatMountainEntity.m_20189_(), 3, 0.4, 0.4, 0.4, 0.0);
            }
        }
    }

    public static boolean advance(MeatMountainEntity meatMountainEntity) {
        if (meatMountainEntity.getPersistentData().m_128471_(BYPASS)) {
            return false;
        }
        int n = ButcherPhaseRules.afterDefeat(ButcherPhases.phase(meatMountainEntity));
        if (n == 0) {
            return false;
        }
        ButcherPhases.enter(meatMountainEntity, n);
        return true;
    }

    private static void enter(MeatMountainEntity meatMountainEntity, int n) {
        CompoundTag compoundTag = meatMountainEntity.getPersistentData();
        compoundTag.m_128405_(PHASE, n);
        compoundTag.m_128405_(ELAPSED, 0);
        compoundTag.m_128405_(DODGE_CD, 0);
        BossRage.reset((LivingEntity)meatMountainEntity);
        meatMountainEntity.m_21219_();
        ButcherPhases.applyHealth(meatMountainEntity);
        meatMountainEntity.m_21153_(meatMountainEntity.m_21233_());
        meatMountainEntity.f_19802_ = 0;
        meatMountainEntity.resetButcherCombat();
        MeatBoss.onPhaseChanged(meatMountainEntity, n);
        Level level = meatMountainEntity.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.m_5594_(null, meatMountainEntity.m_20183_(), SoundEvents.f_11957_, SoundSource.HOSTILE, 1.0f, 0.8f);
        }
        FTBossMod.LOGGER.info("[FTBoss] \u5c60\u592b\u9636\u6bb5={} \u8840\u91cf={} \u4e09\u9636\u6bb5\u5269\u4f59={}\u79d2 \u5b9e\u4f53={}", new Object[]{n, Float.valueOf(meatMountainEntity.m_21233_()), n == 3 ? 60 : 0, meatMountainEntity.m_20148_()});
    }

    private static void applyHealth(MeatMountainEntity meatMountainEntity) {
        AttributeInstance attributeInstance = meatMountainEntity.m_21051_(Attributes.f_22276_);
        if (attributeInstance == null) {
            return;
        }
        double d = ButcherPhaseRules.healthMultiplier(ButcherPhases.phase(meatMountainEntity)) - 1.0;
        AttributeModifier attributeModifier = attributeInstance.m_22111_(HEALTH_MODIFIER);
        if (attributeModifier != null && attributeModifier.m_22218_() == d) {
            return;
        }
        attributeInstance.m_22120_(HEALTH_MODIFIER);
        if (d != 0.0) {
            attributeInstance.m_22118_(new AttributeModifier(HEALTH_MODIFIER, "ftboss_butcher_phase_health", d, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        if (meatMountainEntity.m_21223_() > meatMountainEntity.m_21233_()) {
            meatMountainEntity.m_21153_(meatMountainEntity.m_21233_());
        }
    }

    public static void beginDamage(MeatMountainEntity meatMountainEntity, DamageSource damageSource) {
        meatMountainEntity.getPersistentData().m_128379_(BYPASS, damageSource.m_269533_(DamageTypeTags.f_268738_));
    }

    public static void endDamage(MeatMountainEntity meatMountainEntity) {
        meatMountainEntity.getPersistentData().m_128473_(BYPASS);
    }

    public static boolean dodge(MeatMountainEntity meatMountainEntity, DamageSource damageSource) {
        if (!meatMountainEntity.m_6084_() || meatMountainEntity.m_6673_(damageSource) || damageSource.m_7639_() == null || damageSource.m_7639_() == meatMountainEntity || damageSource.m_269533_(DamageTypeTags.f_268738_)) {
            return false;
        }
        CompoundTag compoundTag = meatMountainEntity.getPersistentData();
        if (ButcherPhases.phase(meatMountainEntity) != 3 || compoundTag.m_128451_(DODGE_CD) > 0) {
            return false;
        }
        if (!ButcherPhaseRules.dodge(ButcherPhases.phase(meatMountainEntity), compoundTag.m_128451_(DODGE_CD), meatMountainEntity.m_217043_().m_188501_())) {
            return false;
        }
        compoundTag.m_128405_(DODGE_CD, 20);
        Level level = meatMountainEntity.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.m_8767_((ParticleOptions)ParticleTypes.f_123796_, meatMountainEntity.m_20185_(), meatMountainEntity.m_20186_() + 1.0, meatMountainEntity.m_20189_(), 8, 0.5, 0.6, 0.5, 0.04);
            serverLevel.m_5594_(null, meatMountainEntity.m_20183_(), SoundEvents.f_12317_, SoundSource.HOSTILE, 0.6f, 1.4f);
        }
        return true;
    }

    public static float adjustScaledHealth(LivingEntity livingEntity, float f) {
        if (!(livingEntity instanceof MeatMountainEntity)) {
            return f;
        }
        AttributeInstance attributeInstance = livingEntity.m_21051_(Attributes.f_22276_);
        if (attributeInstance == null || attributeInstance.m_22115_() <= 0.0) {
            return f;
        }
        return ButcherPhaseRules.scaledHealth(f / (float)attributeInstance.m_22115_(), livingEntity.m_21233_());
    }
}

