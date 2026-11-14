package net.farmingtales.ftboss.util;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class ZicaiDebuffRules {
    public static final float DAMAGE_PER_DEBUFF = 2.0F;

    private ZicaiDebuffRules() {}

    public static float damageBonus(LivingEntity target) {
        int count = 0;
        for (MobEffectInstance effect : target.getActiveEffects()) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) count++;
        }
        return count * DAMAGE_PER_DEBUFF;
    }

    public static int safeAmplifier(MobEffect effect, int requestedAmplifier) {
        AttributeModifier movement = effect.getAttributeModifiers().get(Attributes.MOVEMENT_SPEED);
        if (movement == null || movement.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
            return requestedAmplifier;
        }
        for (int amplifier = requestedAmplifier; amplifier >= 0; amplifier--) {
            double amount = effect.getAttributeModifierValue(amplifier, movement);
            if (Double.isFinite(amount) && amount >= -1.0) return amplifier;
        }
        return -1;
    }
}
