package net.farmingtales.ftboss.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

/** Silver Key aura: enemies inside 16 blocks continuously receive Infection (the parasitic effect). */
public final class BloomEffect extends MobEffect {
    public static final double RADIUS = 16.0;
    public static final int INFECTION_TICKS = 50;

    public BloomEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC94C78);
    }

    @Override
    public boolean isBeneficial() {
        return true;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity wearer, int amplifier) {
        if (!(wearer.level() instanceof ServerLevel level)) return;
        double radiusSqr = RADIUS * RADIUS;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                wearer.getBoundingBox().inflate(RADIUS), target -> isEnemy(wearer, target))) {
            if (wearer.distanceToSqr(target) > radiusSqr) continue;
            target.addEffect(new MobEffectInstance(FTEffects.INFECTION.get(), INFECTION_TICKS,
                    Math.max(0, amplifier), false, true, true), wearer);
        }
    }

    static boolean isEnemy(LivingEntity wearer, LivingEntity target) {
        if (target == wearer || !target.isAlive() || target.isSpectator() || wearer.isAlliedTo(target)) return false;
        if (target instanceof Player other) {
            return wearer instanceof Player player && player.canHarmPlayer(other);
        }
        if (target instanceof Enemy) return true;
        if (target instanceof Mob mob && mob.getTarget() == wearer) return true;
        return wearer.getLastHurtMob() == target || target.getLastHurtByMob() == wearer;
    }
}
