/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class PlayerBuffRules {
    private PlayerBuffRules() {
    }

    public static int clearBuffs(LivingEntity target) {
        if (target == null) {
            return 0;
        }
        int cleared = 0;
        for (MobEffectInstance instance : new ArrayList(target.m_21220_())) {
            if (!instance.m_19544_().m_19486_() || !target.m_21195_(instance.m_19544_())) continue;
            ++cleared;
        }
        return cleared;
    }
}

