/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

public final class ZheergenArcanePlayback {
    private ZheergenArcanePlayback() {
    }

    public static double visualScale(double d) {
        if (!Double.isFinite(d) || d <= 0.0) {
            d = 1.0;
        }
        return Math.max(0.85, Math.min(1.35, Math.sqrt(d)));
    }

    public static double elevation(double d, double d2, double d3, double d4) {
        return Math.max(0.1 - d2 * d4, d - (d2 + d3) * 0.5 * d4);
    }

    public static boolean alive(double d, int n) {
        return Double.isFinite(d) && d >= 0.0 && n > 0 && d < (double)n;
    }

    public static float envelope(double d, int n) {
        if (!ZheergenArcanePlayback.alive(d, n)) {
            return 0.0f;
        }
        return (float)(1.0 - Math.max(0.0, Math.min(1.0, (d - (double)n * 0.55) / ((double)n * 0.45))));
    }
}

