/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.zheergenrevision;

public final class SealRules {
    public static final double BASE_RADIUS = 3.5;
    public static final int BASE_TICKS = 60;

    private SealRules() {
    }

    public static boolean inside(double d, double d2) {
        return Double.isFinite(d) && Double.isFinite(d2) && d >= 0.0 && d2 > 0.0 && d <= d2 * d2;
    }

    public static boolean active(long l, long l2, boolean bl, boolean bl2) {
        return bl && bl2 && l >= 0L && l2 > l && l2 - l <= 12000L;
    }

    public static int duration(int n) {
        return Math.max(1, Math.min(1200, n));
    }
}

