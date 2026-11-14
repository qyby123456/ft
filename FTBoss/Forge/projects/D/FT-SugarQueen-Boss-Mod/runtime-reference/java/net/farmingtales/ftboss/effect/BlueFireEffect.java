/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BlueFireEffect
extends MobEffect {
    public static final float DAMAGE_PER_SECOND = 10.0f;
    public static final int TICKS = 360;

    public BlueFireEffect() {
        super(MobEffectCategory.HARMFUL, 3111910);
    }

    public boolean m_19486_() {
        return false;
    }

    public boolean m_6584_(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    public void m_6742_(LivingEntity entity, int amplifier) {
        if (entity.m_9236_().f_46443_) {
            return;
        }
        entity.f_19802_ = 0;
        entity.m_6469_(entity.m_269291_().m_269341_(), 10.0f);
    }
}

