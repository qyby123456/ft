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

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class PoopConfusionEffect
extends MobEffect {
    private static final float MANA_PER_STACK = 3.0f;

    public PoopConfusionEffect() {
        super(MobEffectCategory.HARMFUL, 9132587);
    }

    public boolean m_19486_() {
        return false;
    }

    public boolean m_6584_(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    public void m_6742_(LivingEntity entity, int amplifier) {
        if (entity.m_9236_().f_46443_ || !(entity instanceof Player)) {
            return;
        }
        Player player = (Player)entity;
        float drain = 3.0f + (float)amplifier * 2.0f;
        try {
            Class<?> mdClass = Class.forName("io.redspace.ironsspellbooks.api.magic.MagicData");
            Object md = mdClass.getMethod("getPlayerMagicData", LivingEntity.class).invoke(null, player);
            if (md == null) {
                return;
            }
            float mana = ((Float)mdClass.getMethod("getMana", new Class[0]).invoke(md, new Object[0])).floatValue();
            if (mana <= 0.0f) {
                return;
            }
            mdClass.getMethod("setMana", Float.TYPE).invoke(md, Float.valueOf(Math.max(0.0f, mana - drain)));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }
}

