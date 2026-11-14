package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.util.ZicaiDebuffRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ManorAdaptiveDamage {
    private ManorAdaptiveDamage() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onHurt(LivingHurtEvent event) {
        if (event.isCanceled()) return;
        if (net.farmingtales.ftboss.skill.DoraSniperDamage.isIndependent(event.getSource())) return;
        Entity owner = event.getSource().getEntity();
        if (owner == null) owner = event.getSource().getDirectEntity();
        event.setAmount(adjustDamage(owner, event.getEntity(), event.getAmount()));
    }

    public static boolean hurtSkill(LivingEntity target, Entity owner, DamageSource source, float amount) {
        if (source.getEntity() == null && source.getDirectEntity() == null
                && !net.farmingtales.ftboss.skill.DoraSniperDamage.isIndependent(source)) {
            amount = adjustDamage(owner, target, amount);
        }
        float damage = amount;
        return net.farmingtales.ftboss.boss.ManorMercy.withOwner(owner, () -> target.hurt(source, damage));
    }

    private static float adjustDamage(Entity owner, LivingEntity target, float damage) {
        if (!Float.isFinite(damage) || damage <= 0.0F || !(target instanceof Player player)
                || player.level().isClientSide || !player.isAlive()
                || player.isCreative() || player.isSpectator()) return damage;
        for (int depth = 0; owner instanceof Projectile projectile && depth < 8; depth++) {
            Entity next = projectile.getOwner();
            if (next == null || next == owner) return damage;
            owner = next;
        }
        if (!(owner instanceof EasterBossEntity boss) || boss.isMini()) return damage;
        if (boss.getVariant() == 8 && !boss.isGiant()) {
            return (float) Math.min(Float.MAX_VALUE, (double) damage + ZicaiDebuffRules.damageBonus(player));
        }
        if (!ManorFiveCombatRules.supports(boss.getVariant())) return damage;
        int buffs = 0;
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (effect.getEffect().isBeneficial()) buffs++;
        }
        return scale(damage, 1.0 + ManorFiveCombatRules.adaptiveDamageBonus(player.getMaxHealth(), buffs));
    }

    private static float scale(float damage, double multiplier) {
        if (!Float.isFinite(damage) || damage <= 0.0f || multiplier == 1.0) return damage;
        return (float) Math.min(Float.MAX_VALUE, damage * multiplier);
    }
}
