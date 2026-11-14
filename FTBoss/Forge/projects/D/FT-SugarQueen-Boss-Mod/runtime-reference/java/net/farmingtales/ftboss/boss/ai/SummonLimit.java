/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public final class SummonLimit {
    public static final int MAX_PER_TYPE = 10;
    public static final double RADIUS = 64.0;
    public static final int BUFF_TICKS = 400;

    private SummonLimit() {
    }

    public static int count(LivingEntity boss, Class<? extends Entity> type) {
        List list = boss.m_9236_().m_6443_(type, boss.m_20191_().m_82400_(64.0), Entity::m_6084_);
        return list.size();
    }

    public static int room(LivingEntity boss, Class<? extends Entity> type) {
        return Math.max(0, 10 - SummonLimit.count(boss, type));
    }

    public static boolean atCap(LivingEntity boss, Class<? extends Mob> type) {
        if (SummonLimit.room(boss, type) > 0) {
            return false;
        }
        SummonLimit.empower(boss, type);
        return true;
    }

    public static void empower(LivingEntity boss, Class<? extends Mob> type) {
        List list = boss.m_9236_().m_6443_(type, boss.m_20191_().m_82400_(64.0), Entity::m_6084_);
        for (Mob m : list) {
            m.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 400, 1, false, false, true));
            m.m_7292_(new MobEffectInstance(MobEffects.f_19600_, 400, 0, false, false, true));
            m.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 400, 0, false, false, true));
        }
        Level level = boss.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            Fx.particles((Level)sl, boss.m_20185_(), boss.m_20186_() + 1.2, boss.m_20189_(), 36, (ParticleOptions)ParticleTypes.f_123748_, 2.2, 0.12);
            sl.m_6263_(null, boss.m_20185_(), boss.m_20186_(), boss.m_20189_(), SoundEvents.f_11736_, SoundSource.HOSTILE, 0.9f, 1.4f);
            for (ServerPlayer p : sl.m_6907_()) {
                if (!(p.m_20280_((Entity)boss) <= 4096.0)) continue;
                p.m_5661_((Component)Component.m_237113_((String)"\u00a76\u53ec\u5524\u5df2\u8fbe\u4e0a\u9650(10) \u00a77\u2192 \u573a\u4e0a\u540c\u7c7b\u00a7c\u5168\u4f53\u5f3a\u5316\u00a77(\u901f\u5ea6/\u529b\u91cf/\u6297\u6027 20s)"), true);
            }
        }
        FTBossMod.LOGGER.info("[FTBoss] {} \u7684 {} \u5df2\u5230\u4e0a\u9650 {}, \u672c\u6b21\u6539\u4e3a\u5168\u4f53\u5f3a\u5316 {} \u53ea", new Object[]{boss.m_7755_().getString(), type.getSimpleName(), 10, list.size()});
    }
}

