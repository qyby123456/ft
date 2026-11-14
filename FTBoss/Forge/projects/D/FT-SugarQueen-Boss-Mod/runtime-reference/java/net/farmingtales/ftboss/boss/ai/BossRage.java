/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public final class BossRage {
    private static final String NBT_TIER = "ftboss_rage_tier";

    private BossRage() {
    }

    public static void tickBoss(LivingEntity boss) {
        int want;
        if (boss.m_9236_().m_5776_() || boss.m_21224_()) {
            return;
        }
        int tier = boss.getPersistentData().m_128451_(NBT_TIER);
        float ratio = boss.m_21223_() / Math.max(1.0f, boss.m_21233_());
        int n = ratio <= 0.25f ? 2 : (want = ratio <= 0.5f ? 1 : 0);
        if (want > tier) {
            boss.getPersistentData().m_128405_(NBT_TIER, want);
            tier = want;
            BossRage.enterRage(boss, tier);
        }
        if (tier <= 0) {
            return;
        }
        if (boss.f_19797_ % 20 == 0) {
            BossRage.refreshRage(boss, tier);
        }
        if (boss.f_19797_ % 5 == 0) {
            BossRage.rageParticles(boss, tier);
        }
    }

    public static void reset(LivingEntity boss) {
        boss.getPersistentData().m_128405_(NBT_TIER, 0);
    }

    public static int tierOf(LivingEntity boss) {
        return boss.getPersistentData().m_128451_(NBT_TIER);
    }

    private static void enterRage(LivingEntity boss, int tier) {
        Level level = boss.m_9236_();
        double x = boss.m_20185_();
        double y = boss.m_20186_() + 1.0;
        double z = boss.m_20189_();
        Fx.sound(level, x, y, z, SoundEvents.f_12363_, 1.7f, tier >= 2 ? 0.7f : 0.9f);
        Fx.sound(level, x, y, z, SoundEvents.f_12563_, 0.5f, 1.7f);
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.m_8767_((ParticleOptions)new DustParticleOptions(new Vector3f(0.6f, 0.03f, 0.03f), 1.3f), x, y, z, tier >= 2 ? 60 : 35, 1.2, 1.0, 1.2, 0.18);
            Fx.ring(level, x, boss.m_20186_() + 0.2, z, tier >= 2 ? 3.6 : 2.6, tier >= 2 ? 40 : 26, (ParticleOptions)ParticleTypes.f_123792_, 0.02);
        }
    }

    private static void refreshRage(LivingEntity boss, int tier) {
        boolean offenseOnly = boss instanceof DefectCoreEntity;
        boss.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 80, offenseOnly && tier >= 2 ? 1 : 0, false, false, true));
        if (!offenseOnly) {
            boss.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 80, 0, false, false, true));
        }
        if (tier >= 2) {
            boss.m_7292_(new MobEffectInstance(MobEffects.f_19600_, 80, offenseOnly ? 1 : 0, false, false, true));
            if (!offenseOnly) {
                boss.m_7292_(new MobEffectInstance(MobEffects.f_19605_, 80, 0, false, false, true));
            }
        }
    }

    private static void rageParticles(LivingEntity boss, int tier) {
        Fx.dust(boss.m_9236_(), boss.m_20185_(), boss.m_20186_() + 1.0, boss.m_20189_(), tier >= 2 ? 6 : 3, 0.55f, 0.04f, 0.04f, 0.7, 0.02);
    }
}

