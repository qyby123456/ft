/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 */
package net.farmingtales.ftboss.effect;

import net.farmingtales.ftboss.boss.ai.BleedLink;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class BleedEffect
extends MobEffect {
    public static final float DAMAGE_PER_SECOND = 3.0f;
    public static final int MAX_STACKS = 10;
    public static final int TICKS = 160;

    public BleedEffect() {
        super(MobEffectCategory.HARMFUL, 9116186);
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
        float dmg = 3.0f * (float)Math.min(10, amplifier + 1);
        entity.f_19802_ = 0;
        entity.m_6469_(entity.m_269291_().m_269425_(), dmg);
        if (entity instanceof Player) {
            Player pl = (Player)entity;
            BleedLink.refund(pl, dmg);
        }
    }
}

