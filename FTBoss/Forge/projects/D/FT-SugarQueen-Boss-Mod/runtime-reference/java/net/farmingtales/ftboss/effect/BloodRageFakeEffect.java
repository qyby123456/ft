/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeMap
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Remove
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.effect;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.FTEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BloodRageFakeEffect
extends MobEffect {
    public static final int MAX_STACKS = 5;
    public static final int DECAY_TICKS = 200;
    public static final int DURATION = 300;
    private static final UUID ATK_UUID = UUID.fromString("7c31f0aa-1d52-4b6e-9a10-00000000b001");
    private static final UUID SPD_UUID = UUID.fromString("7c31f0aa-1d52-4b6e-9a10-00000000b002");

    @SubscribeEvent
    public static void onRemoved(MobEffectEvent.Remove e) {
        try {
            MobEffect eff = (MobEffect)FTEffects.BLOOD_RAGE_FAKE.get();
            if (eff == null || e.getEffectInstance() == null) {
                return;
            }
            if (e.getEffectInstance().m_19544_() != eff) {
                return;
            }
            FTBossMod.LOGGER.info("[FTBoss][bloodrage-fake] removed: target={} left={} stacks={}", new Object[]{e.getEntity().m_7755_().getString(), e.getEffectInstance().m_19557_(), e.getEffectInstance().m_19564_() + 1});
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public BloodRageFakeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B0000);
    }

    public boolean m_6584_(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    public static void onHit(LivingEntity attacker) {
        if (!(attacker instanceof Player)) {
            return;
        }
        Player p = (Player)attacker;
        MobEffect eff = (MobEffect)FTEffects.BLOOD_RAGE_FAKE.get();
        if (eff == null) {
            return;
        }
        MobEffectInstance cur = p.m_21124_(eff);
        int stacks = cur == null ? 1 : Math.min(5, cur.m_19564_() + 2);
        p.m_7292_(new MobEffectInstance(eff, 300, stacks - 1, false, true, true));
    }

    public static float trueDamageRatio(int amplifier) {
        return 0.002f * (float)(amplifier + 1);
    }

    public void m_6742_(LivingEntity entity, int amplifier) {
        if (entity.m_9236_().f_46443_) {
            return;
        }
        int elapsed = 300 - (entity.m_21124_((MobEffect)this) == null ? 0 : entity.m_21124_((MobEffect)this).m_19557_());
        if (elapsed > 0 && elapsed % 200 == 0) {
            if (amplifier <= 0) {
                entity.m_21195_((MobEffect)this);
                FTBossMod.LOGGER.info("[FTBoss][\u8840\u6012\u00b7\u4f2a] {} \u5c42\u6570\u6389\u5149, \u6548\u679c\u7ed3\u675f", (Object)entity.m_7755_().getString());
                return;
            }
            int left = Math.max(1, entity.m_21124_((MobEffect)this) == null ? 300 : entity.m_21124_((MobEffect)this).m_19557_());
            entity.m_7292_(new MobEffectInstance((MobEffect)this, left, amplifier - 1, false, true, true));
            FTBossMod.LOGGER.info("[FTBoss][\u8840\u6012\u00b7\u4f2a] {} \u8870\u51cf\u5230 {} \u5c42", (Object)entity.m_7755_().getString(), (Object)amplifier);
        }
        BloodRageFakeEffect.applyAttributes(entity, amplifier + 1);
    }

    public void m_6385_(LivingEntity entity, AttributeMap map, int amplifier) {
        BloodRageFakeEffect.applyAttributes(entity, amplifier + 1);
    }

    public void m_6386_(LivingEntity entity, AttributeMap map, int amplifier) {
        AttributeInstance spd;
        AttributeInstance atk = entity.m_21051_(Attributes.f_22281_);
        if (atk != null) {
            atk.m_22120_(ATK_UUID);
        }
        if ((spd = entity.m_21051_(Attributes.f_22279_)) != null) {
            spd.m_22120_(SPD_UUID);
        }
    }

    private static void applyAttributes(LivingEntity e, int stacks) {
        AttributeInstance spd;
        AttributeInstance atk = e.m_21051_(Attributes.f_22281_);
        if (atk != null) {
            atk.m_22120_(ATK_UUID);
            atk.m_22118_(new AttributeModifier(ATK_UUID, "ftboss.blood_rage_fake_atk", 0.2 * (double)stacks, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        if ((spd = e.m_21051_(Attributes.f_22279_)) != null) {
            spd.m_22120_(SPD_UUID);
            spd.m_22118_(new AttributeModifier(SPD_UUID, "ftboss.blood_rage_fake_spd", 0.02 * (double)stacks, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }
}

