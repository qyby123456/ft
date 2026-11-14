/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ManorMercy;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.DoraSniperDamage;
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
    private ManorAdaptiveDamage() {
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void onHurt(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (DoraSniperDamage.isIndependent(event.getSource())) {
            return;
        }
        Entity owner = event.getSource().m_7639_();
        if (owner == null) {
            owner = event.getSource().m_7640_();
        }
        event.setAmount(ManorAdaptiveDamage.adjustDamage(owner, event.getEntity(), event.getAmount()));
    }

    public static boolean hurtSkill(LivingEntity target, Entity owner, DamageSource source, float amount) {
        if (source.m_7639_() == null && source.m_7640_() == null && !DoraSniperDamage.isIndependent(source)) {
            amount = ManorAdaptiveDamage.adjustDamage(owner, target, amount);
        }
        float damage = amount;
        return ManorMercy.withOwner(owner, () -> target.m_6469_(source, damage));
    }

    private static float adjustDamage(Entity owner, LivingEntity target, float damage) {
        EasterBossEntity boss;
        Player player;
        block10: {
            block9: {
                if (!Float.isFinite(damage) || damage <= 0.0f || !(target instanceof Player)) break block9;
                player = (Player)target;
                if (!player.m_9236_().f_46443_ && player.m_6084_() && !player.m_7500_() && !player.m_5833_()) break block10;
            }
            return damage;
        }
        int depth = 0;
        while (owner instanceof Projectile) {
            Projectile projectile = (Projectile)owner;
            if (depth >= 8) break;
            Entity next = projectile.m_19749_();
            if (next == null || next == owner) {
                return damage;
            }
            owner = next;
            ++depth;
        }
        if (!(owner instanceof EasterBossEntity) || (boss = (EasterBossEntity)owner).isMini()) {
            return damage;
        }
        if (boss.getVariant() == 8 && !boss.isGiant()) {
            return (float)Math.min(3.4028234663852886E38, (double)damage + (double)ZicaiDebuffRules.damageBonus((LivingEntity)player));
        }
        if (!ManorFiveCombatRules.supports(boss.getVariant())) {
            return damage;
        }
        int buffs = 0;
        for (MobEffectInstance effect : player.m_21220_()) {
            if (!effect.m_19544_().m_19486_()) continue;
            ++buffs;
        }
        return ManorAdaptiveDamage.scale(damage, 1.0 + ManorFiveCombatRules.adaptiveDamageBonus(player.m_21233_(), buffs));
    }

    private static float scale(float damage, double multiplier) {
        if (!Float.isFinite(damage) || damage <= 0.0f || multiplier == 1.0) {
            return damage;
        }
        return (float)Math.min(3.4028234663852886E38, (double)damage * multiplier);
    }
}

