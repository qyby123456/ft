/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class AppleBlastLightRules {
    public static final int LIFE_TICKS = 10;
    public static final int CORE_TICKS = 2;
    public static final int SEGMENTS = 32;
    public static final int MAX_ACTIVE = 24;
    public static final double VIEW_DISTANCE = 64.0;

    private AppleBlastLightRules() {
    }

    public static boolean valid(double d, double d2, double d3, double d4) {
        return Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3) && Double.isFinite(d4) && Math.abs(d) <= 3.0E7 && Math.abs(d3) <= 3.0E7 && Math.abs(d2) <= 4096.0 && d4 >= 3.5 && d4 <= 7.0;
    }

    public static float fade(float f) {
        if (!Float.isFinite(f) || f < 0.0f || f >= 10.0f) {
            return 0.0f;
        }
        float f2 = 1.0f - f / 10.0f;
        return f2 * f2;
    }

    public static double ringRadius(double d, float f) {
        double d2 = Math.max(0.0f, Math.min(1.0f, f / 10.0f));
        return 0.35 + (d - 0.35) * (1.0 - Math.pow(1.0 - d2, 2.0));
    }
}

