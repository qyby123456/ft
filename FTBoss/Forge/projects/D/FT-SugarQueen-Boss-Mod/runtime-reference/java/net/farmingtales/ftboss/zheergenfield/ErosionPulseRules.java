/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.zheergenfield;

public final class ErosionPulseRules {
    public static final int INTERVAL_TICKS = 100;
    public static final int DURATION_TICKS = 200;
    public static final double ARENA_RADIUS = 96.0;

    private ErosionPulseRules() {
    }

    public static boolean due(long l, long l2) {
        return l2 <= 9223372036854775707L && l >= l2 + 100L;
    }

    public static int nextAmplifier(int n) {
        return Math.min(4, Math.max(-1, Math.min(4, n)) + 1);
    }

    public static boolean inside(double d) {
        return Double.isFinite(d) && d >= 0.0 && d <= 9216.0;
    }
}

