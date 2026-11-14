/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class QingyiConfluenceRules {
    public static final int CHARGE_TICKS = 32;
    public static final int IMPACT_TICKS = 4;
    public static final int RELEASE_TICKS = 16;
    public static final int ATTACK_TICKS = 10;
    public static final int THROW_CHARGE_TICKS = 10;
    public static final int THROW_USE_TICKS = 72000;
    public static final int COOLDOWN_TICKS = 160;
    public static final int DEBUFF_TICKS = 80;
    public static final double RANGE = 24.0;
    public static final double RADIUS = 3.5;
    public static final double HALF_HEIGHT = 3.0;
    public static final float PLAYER_DAMAGE = 48.0f;
    public static final float BOSS_DAMAGE = 12.0f;

    private QingyiConfluenceRules() {
    }

    public static boolean canThrow(int remaining) {
        return remaining >= 0 && remaining <= 71990;
    }

    public static long skillCooldownRemaining(long now, long readyAt) {
        return Math.max(0L, Math.min(160L, readyAt - now));
    }

    public static int useTicks() {
        return 36;
    }

    public static boolean impactAt(int elapsed) {
        return elapsed == QingyiConfluenceRules.useTicks();
    }

    public static boolean finished(int elapsed) {
        return elapsed >= 48;
    }

    public static boolean inside(double offsetX, double offsetY, double offsetZ) {
        return Double.isFinite(offsetX) && Double.isFinite(offsetY) && Double.isFinite(offsetZ) && Math.abs(offsetY) <= 3.0 && offsetX * offsetX + offsetZ * offsetZ <= 12.25;
    }
}

