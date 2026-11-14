/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.ManorWeaponBalanceRules;

public final class PufferBatRules {
    public static final double CLOUD_RADIUS = 4.5;
    public static final int CLOUD_TICKS = 100;
    public static final int TICK_INTERVAL = 20;
    public static final double CAST_RANGE = 6.0;
    public static final int COOLDOWN_TICKS = 180;
    public static final int DURABILITY_COST = 3;
    public static final int MAX_ACTIVE_CLOUDS = 4;
    public static final int CLOUD_VISUAL_REFRESH_TICKS = 20;
    public static final float TOTAL_DAMAGE_CAP = 256.0f;
    public static final double SHOCKWAVE_RADIUS = 5.5;
    public static final float SHOCKWAVE_BASE_DAMAGE = 18.0f;
    public static final float SHOCKWAVE_POISONED_MULTIPLIER = 1.5f;
    public static final int SHOCKWAVE_COOLDOWN_TICKS = 240;
    public static final int SHOCKWAVE_DURABILITY_COST = 4;
    public static final int SHOCKWAVE_POISON_TICKS = 120;
    public static final int SHOCKWAVE_POISON_AMPLIFIER = 1;
    public static final double SHOCKWAVE_KNOCKBACK = 1.15;
    public static final double SHOCKWAVE_LIFT = 0.25;
    public static final float TICK_BASE_DAMAGE = 14.0f;
    public static final int POISON_AMPLIFIER = 1;
    public static final int POISON_TICKS = 200;
    public static final int SLOW_AMPLIFIER = 0;
    public static final int SLOW_TICKS = 120;

    private PufferBatRules() {
    }

    public static float tickDamage() {
        return ManorWeaponBalanceRules.skillDamage(14.0f);
    }

    public static float shockwaveDamage(boolean poisoned) {
        float damage = ManorWeaponBalanceRules.skillDamage(18.0f);
        return poisoned ? damage * 1.5f : damage;
    }

    public static int damageTicks() {
        return Math.max(1, 5);
    }

    public static int tickOffset(int index) {
        return Math.max(0, index) * 20;
    }

    public static float totalDamage() {
        return Math.min(256.0f, PufferBatRules.tickDamage() * (float)PufferBatRules.damageTicks());
    }

    public static boolean inside(double offsetX, double offsetY, double offsetZ, double radius) {
        return Double.isFinite(offsetX) && Double.isFinite(offsetY) && Double.isFinite(offsetZ) && Double.isFinite(radius) && radius > 0.0 && Math.abs(offsetY) <= 3.0 && offsetX * offsetX + offsetZ * offsetZ <= radius * radius;
    }

    public static boolean validCast(double x, double y, double z) {
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
    }

    public static double[] castPoint(double originX, double originY, double originZ, double targetX, double targetY, double targetZ) {
        if (!PufferBatRules.validCast(originX, originY, originZ) || !PufferBatRules.validCast(targetX, targetY, targetZ)) {
            return null;
        }
        double dx = targetX - originX;
        double dy = targetY - originY;
        double dz = targetZ - originZ;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 6.0 || length <= 1.0E-6) {
            return new double[]{targetX, targetY, targetZ};
        }
        double scale = 6.0 / length;
        return new double[]{originX + dx * scale, originY + dy * scale, originZ + dz * scale};
    }
}

