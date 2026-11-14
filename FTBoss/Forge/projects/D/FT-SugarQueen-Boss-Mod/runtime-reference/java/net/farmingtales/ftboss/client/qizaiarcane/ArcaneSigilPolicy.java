/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.qizaiarcane;

public final class ArcaneSigilPolicy {
    public static final int MIN_RELEASE_TICKS = 18;
    public static final int FADE_TICKS = 12;

    private ArcaneSigilPolicy() {
    }

    public static boolean suppressLegacy(int n, boolean bl) {
        return bl && n != 8 && n != 10;
    }

    public static boolean valid(double d, double d2, int n) {
        return Double.isFinite(d) && Double.isFinite(d2) && n > 0 && n <= 1200 && d >= d2;
    }

    public static float opacity(double d, double d2, int n) {
        if (!ArcaneSigilPolicy.valid(d, d2, n)) {
            return 0.0f;
        }
        double d3 = d - d2;
        double d4 = Math.max(18, n);
        if (d3 >= d4) {
            return 0.0f;
        }
        double d5 = Math.max(0.0, Math.min(1.0, (d3 - d4 + 12.0) / 12.0));
        return (float)(0.5 * (1.0 - d5 * d5 * (3.0 - 2.0 * d5)));
    }

    public static boolean retain(double d, double d2, int n, boolean bl) {
        return ArcaneSigilPolicy.valid(d, d2, n) && ArcaneSigilPolicy.opacity(d, d2, n) > 0.0f && (bl || d >= d2 + (double)n - 1.0);
    }
}

