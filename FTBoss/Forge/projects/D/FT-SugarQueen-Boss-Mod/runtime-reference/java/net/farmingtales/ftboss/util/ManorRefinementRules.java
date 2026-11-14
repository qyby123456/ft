/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class ManorRefinementRules {
    public static final int TELEPORT_SEAL_TICKS = 200;
    public static final int PREDICTION_TICKS = 10;
    public static final double MAX_LEAD = 6.0;
    public static final float DORA_DAMAGE_MULTIPLIER = 1.25f;
    public static final double GONGDI_RANGE_MULTIPLIER = 1.5;

    private ManorRefinementRules() {
    }

    public static double leadScale(double d, double d2) {
        double d3 = Math.hypot(d, d2);
        return !Double.isFinite(d3) || d3 < 0.001 || d3 > 1.5 ? 0.0 : Math.min(10.0, 6.0 / d3);
    }

    public static int armorSteps(int n, int n2) {
        return Math.min(Math.max(0, n) / 5 * 2, Math.max(0, n2));
    }

    public static boolean sealed(long l, long l2) {
        return l2 > l && l2 - l <= 200L;
    }

    public static boolean minorSpell(int n) {
        return n >= 1 && n <= 6 && n != 4;
    }
}

