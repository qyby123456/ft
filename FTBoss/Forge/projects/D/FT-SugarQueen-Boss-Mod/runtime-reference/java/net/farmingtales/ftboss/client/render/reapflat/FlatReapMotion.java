/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render.reapflat;

public final class FlatReapMotion {
    private static final double[] TIMES = new double[]{0.0, 15.0, 27.5, 39.166};
    private static final double[] SWEEP = new double[]{36.0, 64.0, -62.0, -18.0};
    private static final double[] CANT = new double[]{2.0, 7.0, -4.0, 1.0};

    private FlatReapMotion() {
    }

    public static double[] rotation(double d, int n) {
        int n2;
        if (n == 31) {
            return new double[]{1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0};
        }
        double d2 = Math.max(0.0, Math.min(TIMES[3], Double.isFinite(d) ? d : 0.0));
        for (n2 = 1; n2 < 3 && d2 > TIMES[n2]; ++n2) {
        }
        double d3 = (d2 - TIMES[n2 - 1]) / (TIMES[n2] - TIMES[n2 - 1]);
        if (n2 == 2) {
            d3 = 1.0 - Math.pow(1.0 - d3, 2.0);
        }
        if (n2 == 3) {
            d3 = (1.0 - Math.cos(Math.PI * d3)) / 2.0;
        }
        double d4 = Math.toRadians(SWEEP[n2 - 1] + (SWEEP[n2] - SWEEP[n2 - 1]) * d3);
        double d5 = Math.toRadians(CANT[n2 - 1] + (CANT[n2] - CANT[n2 - 1]) * d3);
        double d6 = Math.cos(d4);
        double d7 = Math.sin(d4);
        double d8 = Math.cos(d5);
        double d9 = Math.sin(d5);
        return new double[]{d6, d7 * d9, d7 * d8, 0.0, d8, -d9, -d7, d6 * d9, d6 * d8};
    }

    public static double[] transform(double[] dArray, double[] dArray2, double[] dArray3) {
        double[] dArray4 = (double[])dArray2.clone();
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int n = i;
                dArray4[n] = dArray4[n] + dArray3[i * 3 + j] * (dArray[j] - dArray2[j]);
            }
        }
        return dArray4;
    }

    public static double smooth(double d) {
        d = Math.max(0.0, Math.min(1.0, d));
        return d * d * (3.0 - 2.0 * d);
    }

    public static double placementLift(double d, double d2, double d3) {
        return Math.max(0.18 - d, d3 - (d + d2) / 2.0);
    }

    public static double phaseClock(double d, double d2, boolean bl, double d3) {
        double d4 = Math.min(1.0, Math.max(0.0, d) * d3 / Math.max(1.0, d2));
        return bl ? d4 * 15.0 : 15.0 + d4 * 24.166;
    }

    public static double trailAlpha(double d, double d2) {
        double d3 = d - d2 - 0.65;
        if (d3 <= 0.0 || d3 >= 8.0) {
            return 0.0;
        }
        return Math.min(1.0, d3 / 0.65) * Math.pow(1.0 - d3 / 8.0, 2.0);
    }

    public static double[] anchoredTrailPoint(double[] dArray, double[] dArray2) {
        double d = dArray[2] + dArray2[5] - 0.3;
        return new double[]{dArray2[0] + dArray[0] * dArray2[4] + d * dArray2[3], dArray2[1] + dArray[1], dArray2[2] - dArray[0] * dArray2[3] + d * dArray2[4]};
    }

    public static double visibleAlpha(double[] dArray, double[] dArray2, double[] dArray3, boolean bl) {
        if (bl) {
            return 1.0;
        }
        double d = 0.0;
        for (int i = 0; i < 3; ++i) {
            double d2 = Math.max(0.0, Math.max(dArray[i] - dArray2[i], dArray2[i] - dArray[i + 3]));
            d += d2 * d2;
        }
        double d3 = FlatReapMotion.smooth((Math.sqrt(d) - 0.22) / 0.4);
        if (!bl) {
            return d3;
        }
        double d4 = 0.3;
        double d5 = 3.0;
        for (int i = 0; i < 3; ++i) {
            double d6;
            double d7;
            if (!(Math.abs(dArray3[i]) < 1.0E-8 ? dArray2[i] < dArray[i] - 0.06 || dArray2[i] > dArray[i + 3] + 0.06 : (d4 = Math.max(d4, Math.min(d7 = (dArray[i] - 0.06 - dArray2[i]) / dArray3[i], d6 = (dArray[i + 3] + 0.06 - dArray2[i]) / dArray3[i]))) > (d5 = Math.min(d5, Math.max(d7, d6))))) continue;
            return d3;
        }
        return d3 * 0.16;
    }
}

