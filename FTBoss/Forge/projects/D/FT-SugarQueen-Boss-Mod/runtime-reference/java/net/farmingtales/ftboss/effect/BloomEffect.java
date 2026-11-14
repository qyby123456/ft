/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.monster.Enemy
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.effect;

import net.farmingtales.ftboss.effect.FTEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class BloomEffect
extends MobEffect {
    public static final double RADIUS = 16.0;
    public static final int INFECTION_TICKS = 50;

    public BloomEffect() {
        super(MobEffectCategory.BENEFICIAL, 13192312);
    }

    public boolean m_19486_() {
        return true;
    }

    public boolean m_6584_(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    public void m_6742_(LivingEntity wearer, int amplifier) {
        Level level = wearer.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        double radiusSqr = 256.0;
        for (LivingEntity target2 : level2.m_6443_(LivingEntity.class, wearer.m_20191_().m_82400_(16.0), target -> BloomEffect.isEnemy(wearer, target))) {
            if (wearer.m_20280_((Entity)target2) > radiusSqr) continue;
            target2.m_147207_(new MobEffectInstance((MobEffect)FTEffects.INFECTION.get(), 50, Math.max(0, amplifier), false, true, true), (Entity)wearer);
        }
    }

    static boolean isEnemy(LivingEntity wearer, LivingEntity target) {
        Mob mob;
        if (target == wearer || !target.m_6084_() || target.m_5833_() || wearer.m_7307_((Entity)target)) {
            return false;
        }
        if (target instanceof Player) {
            Player player;
            Player other = (Player)target;
            return wearer instanceof Player && (player = (Player)wearer).m_7099_(other);
        }
        if (target instanceof Enemy) {
            return true;
        }
        if (target instanceof Mob && (mob = (Mob)target).m_5448_() == wearer) {
            return true;
        }
        return wearer.m_21214_() == target || target.m_21188_() == wearer;
    }
}

