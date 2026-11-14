/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.boss.ai;

public final class RatSwarmRules {
    public static final double MASTER_HEALTH = 20000.0;
    public static final double DEPUTY_HEALTH = 400.0;
    public static final double DEPUTY_DAMAGE = 2.0;
    public static final int SWARM_SIZE = 12;
    public static final int WOUNDED_SWARM_SIZE = 16;
    public static final float SWARM_DAMAGE_TAKEN = 0.6f;
    public static final int DEVICE_COUNT = 3;
    public static final int DEVICE_TICKS = 400;
    public static final int DEVICE_FULL = 100;
    public static final int FOOD_HEAL = 20;
    public static final int FEED_COOLDOWN = 4;
    public static final float DEVICE_DAMAGE_FRACTION = 0.125f;
    public static final float EXPLOSION_DAMAGE = 8.0f;
    public static final double EXPLOSION_RADIUS = 4.0;
    public static final int GUARD_PHASE = 0;
    public static final int DEVICE_PHASE = 1;

    private RatSwarmRules() {
    }

    public static int swarmSize(float f, float f2) {
        return f < f2 * 0.5f ? 16 : 12;
    }

    public static float damageSwarm(float f, float f2) {
        if (!Float.isFinite(f2) || f2 <= 0.0f) {
            return f;
        }
        return Math.max(0.0f, f - f2 * 0.6f);
    }

    public static float sharedHitDamage(float f, float f2, long l) {
        if (!Float.isFinite(f) || f <= 0.0f) {
            return 0.0f;
        }
        return l >= 0L && l < 10L ? Math.max(0.0f, f - f2) : f;
    }

    public static float scaleSwarm(float f, float f2, float f3) {
        return f2 > 0.0f ? Math.max(0.0f, Math.min(f3, f / f2 * f3)) : f3;
    }

    public static int healDevice(int n) {
        return Math.min(100, Math.max(0, n) + 20);
    }

    public static boolean lethalBurst(float f, float f2, boolean bl) {
        return bl && Float.isFinite(f) && f2 > 0.0f && f >= f2;
    }

    public static boolean deviceComplete(int n) {
        return n >= 100;
    }

    public static boolean deviceExpired(int n) {
        return n <= 0;
    }
}

