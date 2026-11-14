/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 */
package net.farmingtales.ftboss.effect;

import java.util.UUID;
import net.farmingtales.ftboss.effect.FTEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ArmorShredEffect
extends MobEffect {
    public static final double SHRED_PER_STACK = 10.0;
    public static final int MAX_STACKS = 2;
    public static final int TICKS = 160;
    private static final UUID UUID_PER_STACK_1 = UUID.fromString("7c31f0aa-1d52-4b6e-9a10-00000000c001");
    private static final UUID UUID_PER_STACK_2 = UUID.fromString("7c31f0aa-1d52-4b6e-9a10-00000000c002");

    public ArmorShredEffect() {
        super(MobEffectCategory.HARMFUL, 7240587);
    }

    public static void onHit(LivingEntity target) {
        MobEffect eff = (MobEffect)FTEffects.ARMOR_SHRED.get();
        if (eff == null) {
            return;
        }
        MobEffectInstance cur = target.m_21124_(eff);
        int stacks = cur == null ? 1 : Math.min(2, cur.m_19564_() + 2);
        target.m_7292_(new MobEffectInstance(eff, 160, stacks - 1, false, true, true));
    }

    public boolean m_6584_(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    public void m_6742_(LivingEntity entity, int amplifier) {
        if (entity.m_9236_().f_46443_) {
            return;
        }
        AttributeInstance armor = entity.m_21051_(Attributes.f_22284_);
        if (armor == null) {
            return;
        }
        armor.m_22120_(UUID_PER_STACK_1);
        armor.m_22120_(UUID_PER_STACK_2);
        armor.m_22118_(new AttributeModifier(UUID_PER_STACK_1, "ftboss.armor_shred_1", -10.0, AttributeModifier.Operation.ADDITION));
        if (amplifier >= 1) {
            armor.m_22118_(new AttributeModifier(UUID_PER_STACK_2, "ftboss.armor_shred_2", -10.0, AttributeModifier.Operation.ADDITION));
        }
    }
}

