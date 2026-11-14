/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.effect;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TremorEffect
extends MobEffect {
    public static final int STUN_AT = 10;
    public static final int MAX_STACKS = 10;
    public static final int TICKS = 200;
    public static final int STUN_TICKS = 20;

    public TremorEffect() {
        super(MobEffectCategory.HARMFUL, 9071146);
    }

    public boolean m_19486_() {
        return false;
    }

    public boolean m_6584_(int duration, int amplifier) {
        return true;
    }

    public void m_6742_(LivingEntity entity, int amplifier) {
        Level level;
        if (entity.m_9236_().f_46443_) {
            return;
        }
        if (amplifier + 1 < 10) {
            return;
        }
        entity.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 20, 250, false, true, true));
        entity.m_7292_(new MobEffectInstance(MobEffects.f_19603_, 20, 128, false, false, false));
        entity.m_7292_(new MobEffectInstance(MobEffects.f_19604_, 40, 0, false, true, true));
        entity.m_20334_(0.0, entity.m_20184_().f_82480_, 0.0);
        if (entity instanceof Player) {
            Player p = (Player)entity;
            p.m_6858_(false);
            p.m_5661_((Component)Component.m_237113_((String)"\u00a76\u3010\u9707\u98a4\u3011\u00a77\u5341\u5c42\u79ef\u805a \u2014\u2014 \u00a7c\u4f60\u88ab\u9707\u5f97\u7ad9\u4e0d\u7a33\uff01"), true);
        }
        if ((level = entity.m_9236_()) instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.m_8767_((ParticleOptions)ParticleTypes.f_123797_, entity.m_20185_(), entity.m_20186_() + 1.6, entity.m_20189_(), 24, 0.4, 0.3, 0.4, 0.15);
            sl.m_6263_(null, entity.m_20185_(), entity.m_20186_(), entity.m_20189_(), SoundEvents.f_11668_, SoundSource.HOSTILE, 1.4f, 0.5f);
            FTBossMod.LOGGER.info("[FTBoss][\u9707\u98a4] {} \u53e0\u6ee1 {} \u5c42 \u2192 \u7729\u6655 {} \u79d2, \u5c42\u6570\u6e05\u96f6", new Object[]{entity.m_7755_().getString(), 10, 1});
        }
        entity.m_21195_((MobEffect)this);
    }
}

