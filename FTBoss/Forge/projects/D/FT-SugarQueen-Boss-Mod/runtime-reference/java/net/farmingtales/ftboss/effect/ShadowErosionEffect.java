/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.effect;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.zheergenphantom.ErosionRules;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ShadowErosionEffect
extends MobEffect {
    public static final float DAMAGE_PER_STACK = 4.0f;
    public static final int MAX_STACKS = 5;
    public static final int TICKS = 200;

    public ShadowErosionEffect() {
        super(MobEffectCategory.BENEFICIAL, 8007358);
    }

    public boolean m_19486_() {
        return true;
    }

    public boolean m_6584_(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    public void m_6742_(LivingEntity entity, int amplifier) {
        if (entity.m_9236_().f_46443_) {
            return;
        }
        float dmg = ErosionRules.unscaledDamage(4.0f * (float)(amplifier + 1));
        entity.f_19802_ = 0;
        entity.m_6469_(entity.m_269291_().m_269341_(), dmg);
    }

    public List<ItemStack> getCurativeItems() {
        return new ArrayList<ItemStack>();
    }
}

