/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class MaqueRicochetRules {
    public static final int FEATHERS = 5;
    public static final int HITS = 3;
    public static final int LIFETIME = 160;
    public static final int MAX_FLIGHTS = 320;
    public static final double AREA_RADIUS = 8.0;
    public static final double COLLISION_RADIUS = 0.28;
    public static final double SPEED = 1.38;

    private MaqueRicochetRules() {
    }

    public static boolean finished(int n, long l, boolean bl) {
        return !bl || n >= 3 || l < 0L || l >= 160L;
    }

    public static int choose(int n, int n2, double d) {
        if (n <= 0) {
            return -1;
        }
        boolean bl = n > 1 && n2 >= 0 && n2 < n;
        int n3 = n - (bl ? 1 : 0);
        double d2 = Double.isFinite(d) ? Math.max(0.0, Math.min(Math.nextDown(1.0), d)) : 0.0;
        int n4 = Math.min(n3 - 1, (int)(d2 * (double)n3));
        return bl && n4 >= n2 ? n4 + 1 : n4;
    }

    public static double[] steer(double[] dArray, double[] dArray2) {
        double d = Math.sqrt(dArray2[0] * dArray2[0] + dArray2[1] * dArray2[1] + dArray2[2] * dArray2[2]);
        if (!(d > 1.0E-8) || !Double.isFinite(d)) {
            return (double[])dArray.clone();
        }
        double[] dArray3 = new double[3];
        double d2 = 0.0;
        for (int i = 0; i < 3; ++i) {
            dArray3[i] = dArray[i] * 0.55 + dArray2[i] / d * 0.62;
            d2 += dArray3[i] * dArray3[i];
        }
        double d3 = d2 > 1.9043999999999996 ? 1.38 / Math.sqrt(d2) : 1.0;
        int n = 0;
        while (n < 3) {
            int n2 = n++;
            dArray3[n2] = dArray3[n2] * d3;
        }
        return dArray3;
    }
}

