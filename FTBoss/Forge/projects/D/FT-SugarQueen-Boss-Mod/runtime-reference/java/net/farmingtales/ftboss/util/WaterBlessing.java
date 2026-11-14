/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.util;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class WaterBlessing {
    public static final int INTERVAL_TICKS = 40;
    private static final int DURATION_TICKS = 60;

    private WaterBlessing() {
    }

    public static void tick(LivingEntity entity) {
        if (entity == null || entity.m_9236_().m_5776_() || !entity.m_6084_()) {
            return;
        }
        if (entity.f_19797_ % 40 != 0) {
            return;
        }
        WaterBlessing.apply(entity);
    }

    public static void apply(LivingEntity entity) {
        if (entity.m_9236_().m_5776_() || !entity.m_6084_()) {
            return;
        }
        WaterBlessing.hidden(entity, MobEffects.f_19596_);
        WaterBlessing.hidden(entity, MobEffects.f_19606_);
        WaterBlessing.hidden(entity, MobEffects.f_19600_);
        if (WaterBlessing.isRainy(entity.m_9236_())) {
            WaterBlessing.hidden(entity, MobEffects.f_19605_);
            WaterBlessing.hidden(entity, MobEffects.f_19608_);
        }
    }

    public static boolean isRainy(Level level) {
        return level != null && (level.m_46471_() || level.m_46470_());
    }

    private static void hidden(LivingEntity entity, MobEffect effect) {
        entity.m_7292_(new MobEffectInstance(effect, 60, 0, true, false));
    }
}

