/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class BossKnockback {
    public static final double XUANLAN_MULTIPLIER = 1.5;
    public static final double BOSS_RESISTANCE = 0.85;
    public static final double TO_PLAYER_MULT = 0.1;
    public static final boolean SCALE_VERTICAL = true;
    public static final double VERTICAL_MULT = 0.25;
    private static final ThreadLocal<LivingEntity> EXPLICIT_TARGET = new ThreadLocal();

    private BossKnockback() {
    }

    public static double sourceMultiplier(Entity source) {
        for (int depth = 0; source != null && depth < 8; ++depth) {
            if (source instanceof Player) {
                return 1.0;
            }
            if (source instanceof EasterBossEntity) {
                EasterBossEntity boss = (EasterBossEntity)source;
                return boss.getVariant() == 6 ? 1.5 : 1.0;
            }
            Entity owner = ManorFightMembers.owner(source);
            if (owner == source) break;
            source = owner;
        }
        return 1.0;
    }

    public static void push(Entity source, Entity target, double dx, double dy, double dz) {
        double multiplier = BossKnockback.sourceMultiplier(source);
        BossKnockback.push(target, dx * multiplier, dy * multiplier, dz * multiplier);
    }

    public static void knockback(Entity source, LivingEntity target, double strength, double dx, double dz) {
        BossKnockback.knockback(target, strength * BossKnockback.sourceMultiplier(source), dx, dz);
    }

    public static double vertical(Entity source, Entity target, double strength) {
        return BossKnockback.vertical(target, strength * BossKnockback.sourceMultiplier(source));
    }

    public static void push(Entity target, double dx, double dy, double dz) {
        if (target instanceof Player) {
            target.m_5997_(dx * 0.1, BossKnockback.vertical(target, dy), dz * 0.1);
        } else {
            target.m_5997_(dx, dy, dz);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void knockback(LivingEntity target, double strength, double dx, double dz) {
        LivingEntity previous = EXPLICIT_TARGET.get();
        EXPLICIT_TARGET.set(target);
        try {
            target.m_147240_(target instanceof Player ? strength * 0.1 : strength, dx, dz);
        }
        finally {
            if (previous == null) {
                EXPLICIT_TARGET.remove();
            } else {
                EXPLICIT_TARGET.set(previous);
            }
        }
    }

    public static boolean isExplicit(LivingEntity target) {
        return EXPLICIT_TARGET.get() == target;
    }

    public static double vertical(Entity target, double strength) {
        return target instanceof Player ? strength * 0.25 : strength;
    }
}

