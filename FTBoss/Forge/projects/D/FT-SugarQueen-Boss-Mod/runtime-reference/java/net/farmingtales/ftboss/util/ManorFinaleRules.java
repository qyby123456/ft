/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class ManorFinaleRules {
    public static final double ARENA_RADIUS = 96.0;
    public static final double ASSAULT_RADIUS = 3.0;
    public static final double ASSAULT_HEIGHT = 3.0;
    public static final int WINDUP_TICKS = 20;
    public static final int COOLDOWN_TICKS = 70;

    private ManorFinaleRules() {
    }

    public static boolean insideArena(double d, double d2, double d3) {
        return Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3) && d * d + d2 * d2 + d3 * d3 <= 9216.0;
    }

    public static boolean intersectsField(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9) {
        double d10;
        if (!Double.isFinite(d) || !Double.isFinite(d2) || !Double.isFinite(d3) || !Double.isFinite(d4) || !Double.isFinite(d5) || !Double.isFinite(d6) || !Double.isFinite(d7) || !Double.isFinite(d8) || !Double.isFinite(d9) || d4 > d7 || d5 > d8 || d6 > d9 || d8 < d2 - 0.25 || d5 > d2 + 3.0) {
            return false;
        }
        double d11 = Math.max(d4 - d, Math.max(0.0, d - d7));
        return d11 * d11 + (d10 = Math.max(d6 - d3, Math.max(0.0, d3 - d9))) * d10 <= 9.0;
    }

    public static int rotationStart(int n, long l, int n2) {
        return n2 <= 0 ? -1 : Math.floorMod((long)n + l, n2);
    }

    public static int cooldown(int n) {
        return 70 + Math.floorMod(n, 11);
    }
}

