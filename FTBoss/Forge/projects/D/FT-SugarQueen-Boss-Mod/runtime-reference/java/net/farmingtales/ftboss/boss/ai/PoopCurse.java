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
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.util.Fx;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;

public final class PoopCurse {
    public static final int DURATION = 160;
    public static final int AMPLIFIER = 0;

    private PoopCurse() {
    }

    public static void castOnce(ServerPlayer player, String source) {
        if (player == null || !player.m_6084_()) {
            return;
        }
        player.m_7292_(new MobEffectInstance((MobEffect)FTEffects.POOP_CONFUSION.get(), 160, 0, false, true, true));
        player.m_5661_((Component)Component.m_237113_((String)("\u00a76\u5c4e\uff1f\uff01\uff01 \u00a77\u2190 " + source + " \u00a78| \u00a7c\u65b9\u5411\u952e\u6df7\u4e71 \u00a77+ \u00a7b\u6cd5\u529b\u6d41\u5931")), true);
        Level level = player.m_9236_();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            Fx.particles((Level)sl, player.m_20185_(), player.m_20186_() + 1.0, player.m_20189_(), 18, (ParticleOptions)ParticleTypes.f_123753_, 0.6, 0.1);
            Fx.particles((Level)sl, player.m_20185_(), player.m_20186_() + 1.2, player.m_20189_(), 6, (ParticleOptions)ParticleTypes.f_123755_, 0.4, 0.04);
            sl.m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_12384_, SoundSource.HOSTILE, 1.0f, 0.55f);
        }
        FTBossMod.LOGGER.info("[FTBoss][\u7b54\u8fa9\u8d85\u4eba] \u5411 {} \u65bd\u653e\u300c\u5c4e\uff1f\uff01\uff01\u300d(\u5355\u6b21 {}t)", (Object)player.m_7755_().getString(), (Object)160);
    }

    public static int stacksOf(ServerPlayer player) {
        MobEffectInstance inst = player.m_21124_((MobEffect)FTEffects.POOP_CONFUSION.get());
        return inst == null ? 0 : inst.m_19564_() + 1;
    }
}

