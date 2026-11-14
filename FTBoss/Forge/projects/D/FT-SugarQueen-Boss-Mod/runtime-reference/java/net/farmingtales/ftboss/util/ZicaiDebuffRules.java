/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 */
package net.farmingtales.ftboss.util;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class ZicaiDebuffRules {
    public static final float DAMAGE_PER_DEBUFF = 2.0f;

    private ZicaiDebuffRules() {
    }

    public static float damageBonus(LivingEntity target) {
        int count = 0;
        for (MobEffectInstance effect : target.m_21220_()) {
            if (effect.m_19544_().m_19483_() != MobEffectCategory.HARMFUL) continue;
            ++count;
        }
        return (float)count * 2.0f;
    }

    public static int safeAmplifier(MobEffect effect, int requestedAmplifier) {
        AttributeModifier movement = (AttributeModifier)effect.m_19485_().get(Attributes.f_22279_);
        if (movement == null || movement.m_22217_() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
            return requestedAmplifier;
        }
        for (int amplifier = requestedAmplifier; amplifier >= 0; --amplifier) {
            double amount = effect.m_7048_(amplifier, movement);
            if (!Double.isFinite(amount) || !(amount >= -1.0)) continue;
            return amplifier;
        }
        return -1;
    }
}

