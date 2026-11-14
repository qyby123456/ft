package net.farmingtales.ftboss.boss.ai;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;

public final class ManorBuffCopy {
    public static final int DURATION_TICKS = 30 * 20;
    public static final int COOLDOWN_TICKS = 15 * 20;

    private ManorBuffCopy() {}

    public static boolean isCopyTick(int tickCount) {
        return tickCount > 0 && tickCount % COOLDOWN_TICKS == 0;
    }

    public static MobEffectInstance copyOf(MobEffectInstance source) {
        if (source == null) return null;
        MobEffect effect = source.getEffect();
        if (effect == null || !effect.isBeneficial()) return null;
        if (effect == MobEffects.ABSORPTION || effect == MobEffects.HEALTH_BOOST) return null;
        if (effect.getAttributeModifiers().containsKey(Attributes.MAX_HEALTH)) return null;
        ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        if (key != null && key.getNamespace().equals("herbalbrews") && key.getPath().equals("renewal")) return null;
        return new MobEffectInstance(effect, DURATION_TICKS, source.getAmplifier(),
                source.isAmbient(), source.isVisible(), source.showIcon());
    }
}
