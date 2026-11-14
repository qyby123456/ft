/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class WineMaceRules {
    public static final int MAX_PRESSURE = 3;
    public static final int PRESSURE_TICKS = 300;
    public static final int HIT_INTERVAL = 20;
    public static final int WINDUP_TICKS = 12;
    public static final double RANGE = 7.0;
    public static final double BASE_RADIUS = 3.0;
    public static final int MAX_TARGETS = 24;
    public static final String PRESSURE_TAG = "FTWineMacePressure";

    private WineMaceRules() {
    }

    public static int pressure(int stored, long expires, long now) {
        if (expires <= now || expires - now < 0L || expires - now > 300L) {
            return 0;
        }
        return Math.max(0, Math.min(3, stored));
    }

    public static boolean hitReady(long readyAt, long now) {
        return readyAt <= now || readyAt - now < 0L || readyAt - now > 20L;
    }

    public static double radius(int pressure) {
        return 3.0 + (double)Math.max(0, Math.min(3, pressure)) * 0.5;
    }

    public static float damage(double attack, int pressure) {
        if (!Double.isFinite(attack) || attack < 0.0) {
            return 0.0f;
        }
        int stacks = Math.max(0, Math.min(3, pressure));
        return (float)Math.min(128.0, 6.0 + attack * 1.5 + (double)stacks * (4.0 + attack * 0.35));
    }

    public static boolean inBlast(double horizontalSquared, double height, int pressure) {
        double radius = WineMaceRules.radius(pressure);
        return Double.isFinite(horizontalSquared) && Double.isFinite(height) && horizontalSquared >= 0.0 && horizontalSquared <= radius * radius && Math.abs(height) <= 2.5;
    }
}

