/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.ThrownEnderpearl
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.HitResult
 */
package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class EasterPearlEntity
extends ThrownEnderpearl {
    public static final float LAND_DAMAGE = 30.0f;
    public static final double LAND_RADIUS = 4.0;

    public EasterPearlEntity(EntityType entityType, Level level) {
        super(entityType, level);
    }

    public EasterPearlEntity(Level level, LivingEntity livingEntity) {
        super(level, livingEntity);
    }

    protected void m_6532_(HitResult hitResult) {
        if (!this.m_9236_().f_46443_) {
            LivingEntity livingEntity;
            int n = 0;
            Object object = this.m_19749_();
            LivingEntity livingEntity2 = object instanceof LivingEntity ? (livingEntity = (LivingEntity)object) : null;
            livingEntity = this.m_269291_().m_269104_((Entity)this, (Entity)livingEntity2);
            for (LivingEntity level2 : ManorLivingTargets.nearby(this.m_9236_(), (Entity)this)) {
                float f;
                if (!level2.m_6084_() || level2.m_5833_() || level2.m_20280_((Entity)this) > 16.0) continue;
                level2.f_19802_ = 0;
                if (livingEntity2 instanceof EasterBossEntity) {
                    EasterBossEntity easterBossEntity = (EasterBossEntity)livingEntity2;
                    f = easterBossEntity.manorSkillDamage(level2, 30.0f, 1.5);
                } else {
                    f = 30.0f;
                }
                float f2 = f;
                level2.m_6469_((DamageSource)livingEntity, f2);
                ++n;
            }
            Level level = this.m_9236_();
            if (level instanceof ServerLevel) {
                object = (ServerLevel)level;
                object.m_8767_((ParticleOptions)ParticleTypes.f_123799_, this.m_20185_(), this.m_20186_() + 0.2, this.m_20189_(), 45, 1.3, 0.4, 1.3, 0.09);
                object.m_8767_((ParticleOptions)ParticleTypes.f_123760_, this.m_20185_(), this.m_20186_() + 0.2, this.m_20189_(), 30, 1.0, 0.4, 1.0, 0.25);
                object.m_6263_(null, this.m_20185_(), this.m_20186_(), this.m_20189_(), SoundEvents.f_11895_, SoundSource.HOSTILE, 1.2f, 1.4f);
            }
            FTBossMod.LOGGER.info("[FTBoss][Easter] \u672b\u5f71\u73cd\u73e0\u843d\u5730: \u547d\u4e2d {} \u540d\u73a9\u5bb6 ({} \u70b9\u9b54\u6cd5\u4f24\u5bb3, \u534a\u5f84 {} \u683c)", new Object[]{n, 30, 4});
        }
        super.m_6532_(hitResult);
    }
}

