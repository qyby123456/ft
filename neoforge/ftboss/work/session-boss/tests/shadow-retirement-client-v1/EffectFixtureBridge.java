package net.farmingtales.ftboss.integ.spells;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
public final class EffectFixtureBridge {
    public static void apply(LivingEntity caster,LivingEntity target,Holder<MobEffect> effect,int duration,int amplifier){ManorSpellEffects.effect(caster,target,effect,duration,amplifier);}
}
