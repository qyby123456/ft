/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 */
package net.farmingtales.ftboss.entity;

import net.farmingtales.ftboss.boss.ai.LemonImmuneBuffRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public final class BossBuffImmunity {
    private BossBuffImmunity() {
    }

    public static boolean blocked(MobEffectInstance mobEffectInstance) {
        if (mobEffectInstance == null) {
            return false;
        }
        if (ManorSpellRules.stupor(mobEffectInstance)) {
            return true;
        }
        MobEffect mobEffect = mobEffectInstance.m_19544_();
        return mobEffect != null && mobEffect.m_19486_() && LemonImmuneBuffRules.isForbidden(mobEffect);
    }
}

