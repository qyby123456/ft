/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

public final class QingyiWaveTiming {
    private QingyiWaveTiming() {
    }

    public static double scale(double d) {
        return Double.isFinite(d) && d > 0.0 ? Math.min(32.0, d) : 1.0;
    }

    public static int slice(double d, double d2) {
        return Math.max(1, Math.min(4, (int)Math.round((d * 0.5 - 0.8) / (1.75 * d2))));
    }

    public static double delay(int n) {
        return 0.0;
    }

    public static float time(double d, int n) {
        return (float)Math.max(0.0, (d - QingyiWaveTiming.delay(n)) * 0.05);
    }

    public static float opacity(double d, int n) {
        double d2 = d - QingyiWaveTiming.delay(n);
        return (float)(QingyiWaveTiming.smooth(d2 / 1.2) * (1.0 - QingyiWaveTiming.smooth((d2 - 11.0) / 8.0)));
    }

    public static double travel(double d, int n) {
        return 0.0;
    }

    public static float cameraOpacity(double d, double d2, double d3, double d4, double d5, double d6, boolean bl) {
        double d7 = Math.max(0.0, Math.max(d, -d4));
        double d8 = Math.max(0.0, Math.max(d2, -d5));
        double d9 = Math.max(0.0, Math.max(d3, -d6));
        double d10 = Math.sqrt(d7 * d7 + d8 * d8 + d9 * d9);
        return (float)QingyiWaveTiming.smooth((d10 - (bl ? 0.3 : 0.12)) / (bl ? 0.85 : 0.45));
    }

    public static double smooth(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }
}

