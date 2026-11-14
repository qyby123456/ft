/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;

public final class ManorBuffCopy {
    public static final int DURATION_TICKS = 600;
    public static final int COOLDOWN_TICKS = 300;

    private ManorBuffCopy() {
    }

    public static boolean isCopyTick(int tickCount) {
        return tickCount > 0 && tickCount % 300 == 0;
    }

    public static MobEffectInstance copyOf(MobEffectInstance source) {
        if (source == null) {
            return null;
        }
        MobEffect effect = source.m_19544_();
        if (effect == null || !effect.m_19486_()) {
            return null;
        }
        if (effect == MobEffects.f_19617_ || effect == MobEffects.f_19616_) {
            return null;
        }
        if (effect.m_19485_().containsKey(Attributes.f_22276_)) {
            return null;
        }
        ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey((Object)effect);
        if (key != null && key.m_135827_().equals("herbalbrews") && key.m_135815_().equals("renewal")) {
            return null;
        }
        return new MobEffectInstance(effect, 600, source.m_19564_(), source.m_19571_(), source.m_19572_(), source.m_19575_());
    }
}

