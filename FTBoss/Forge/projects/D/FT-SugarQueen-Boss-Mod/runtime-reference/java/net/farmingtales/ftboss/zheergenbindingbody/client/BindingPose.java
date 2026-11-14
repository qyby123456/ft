/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.zheergenbindingbody.client;

public final class BindingPose {
    private static final double[] CHARGE_TIMES = new double[]{0.0, 0.18, 0.48, 0.78, 1.0};
    private static final double[] RELEASE_TIMES = new double[]{0.0, 0.14, 0.28, 0.46, 0.72, 1.0};
    private static final double[][] CHARGE_VALUES = new double[][]{{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -18.0, 0.0, 4.0, 0.0, 0.0, -4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, {0.06, 0.05, 0.0, -1.0, -2.0, -0.5, -21.0, 8.0, 7.0, -28.0, -5.0, -16.0, 0.5, 0.0, -0.3, -0.5, 0.0, 0.3}, {0.18, 0.16, 0.01, -2.0, -6.0, -1.5, -26.0, 19.0, 13.0, -62.0, -9.0, -13.0, 1.5, 0.0, -0.7, -1.5, 0.0, 0.7}, {0.22, 0.18, 0.02, -1.0, -7.0, -2.0, -27.0, 21.0, 14.0, -79.0, -9.0, -8.0, 2.0, 0.0, -0.8, -2.0, 0.0, 0.8}, {0.2, 0.16, 0.02, 0.0, -6.0, -2.0, -27.0, 20.0, 14.0, -79.0, -9.0, -8.0, 2.0, 0.0, -0.8, -2.0, 0.0, 0.8}};
    private static final double[][] RELEASE_VALUES = new double[][]{{0.2, 0.16, 0.02, 0.0, -6.0, -2.0, -27.0, 20.0, 14.0, -79.0, -9.0, -8.0, 2.0, 0.0, -0.8, -2.0, 0.0, 0.8}, {0.12, 0.29, -0.12, 7.0, -3.0, -1.5, -29.0, 22.0, 16.0, -57.0, -13.0, -12.0, 2.5, 0.0, -1.0, -2.5, 0.0, 1.0}, {0.08, 0.27, -0.1, 8.0, -2.0, -1.0, -29.0, 22.0, 16.0, -54.0, -12.0, -12.0, 2.5, 0.0, -1.0, -2.5, 0.0, 1.0}, {0.1, 0.17, -0.06, 5.0, -3.0, -1.0, -27.0, 18.0, 13.0, -53.0, -10.0, -12.0, 1.5, 0.0, -0.7, -1.5, 0.0, 0.7}, {0.03, 0.04, -0.01, 1.0, -1.0, -0.3, -22.0, 6.0, 7.0, -26.0, -4.0, -8.0, 0.5, 0.0, -0.2, -0.5, 0.0, 0.2}, {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -18.0, 0.0, 4.0, 0.0, 0.0, -4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}};
    private static final double[][] BIND = new double[][]{{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {-5.0, 2.5, 0.0}, {5.0, 2.5, 0.0}, {-1.9, 12.0, 0.0}, {1.9, 12.0, 0.0}};
    public final double[][] position;
    public final double[][] rotation;

    private BindingPose(double[][] dArray, double[][] dArray2) {
        this.position = dArray;
        this.rotation = dArray2;
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    private static double ease(double d) {
        double d2 = BindingPose.clamp(d);
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    private static double slope(double[] dArray, double[][] dArray2, int n, int n2) {
        if (n == 0 || n == dArray.length - 1) {
            return 0.0;
        }
        double d = dArray[n] - dArray[n - 1];
        double d2 = (dArray2[n][n2] - dArray2[n - 1][n2]) / d;
        double d3 = dArray[n + 1] - dArray[n];
        double d4 = (dArray2[n + 1][n2] - dArray2[n][n2]) / d3;
        if (d2 * d4 <= 0.0) {
            return 0.0;
        }
        double d5 = 2.0 * d3 + d;
        double d6 = d3 + 2.0 * d;
        return (d5 + d6) / (d5 / d2 + d6 / d4);
    }

    public static double[] curve(boolean bl, double d) {
        int n;
        double[] dArray = bl ? RELEASE_TIMES : CHARGE_TIMES;
        double[][] dArray2 = bl ? RELEASE_VALUES : CHARGE_VALUES;
        double d2 = BindingPose.clamp(d);
        for (n = 1; n < dArray.length - 1 && dArray[n] < d2; ++n) {
        }
        int n2 = n - 1;
        double d3 = dArray[n] - dArray[n2];
        double d4 = (d2 - dArray[n2]) / d3;
        double d5 = d4 * d4;
        double d6 = d5 * d4;
        double[] dArray3 = new double[18];
        for (int i = 0; i < 18; ++i) {
            dArray3[i] = (2.0 * d6 - 3.0 * d5 + 1.0) * dArray2[n2][i] + (d6 - 2.0 * d5 + d4) * d3 * BindingPose.slope(dArray, dArray2, n2, i) + (-2.0 * d6 + 3.0 * d5) * dArray2[n][i] + (d6 - d5) * d3 * BindingPose.slope(dArray, dArray2, n, i);
        }
        return dArray3;
    }

    public static double[] rotate(double[] dArray, double[] dArray2) {
        double d = dArray2[0];
        double d2 = dArray2[1];
        double d3 = dArray2[2];
        double d4 = dArray[1] * Math.cos(d) - dArray[2] * Math.sin(d);
        double d5 = dArray[1] * Math.sin(d) + dArray[2] * Math.cos(d);
        double d6 = dArray[0] * Math.cos(d2) + d5 * Math.sin(d2);
        d5 = -dArray[0] * Math.sin(d2) + d5 * Math.cos(d2);
        return new double[]{d6 * Math.cos(d3) - d4 * Math.sin(d3), d6 * Math.sin(d3) + d4 * Math.cos(d3), d5};
    }

    private static double[] compose(double[] dArray, double[] dArray2) {
        double[][] dArrayArray = new double[][]{BindingPose.rotate(BindingPose.rotate(new double[]{1.0, 0.0, 0.0}, dArray2), dArray), BindingPose.rotate(BindingPose.rotate(new double[]{0.0, 1.0, 0.0}, dArray2), dArray), BindingPose.rotate(BindingPose.rotate(new double[]{0.0, 0.0, 1.0}, dArray2), dArray)};
        return new double[]{Math.atan2(dArrayArray[1][2], dArrayArray[2][2]), Math.asin(Math.max(-1.0, Math.min(1.0, -dArrayArray[0][2]))), Math.atan2(dArrayArray[0][1], dArrayArray[0][0])};
    }

    public static BindingPose sample(boolean bl, double d, double d2, boolean bl2, double d3, double d4, double d5, double d6) {
        double[] dArray = BindingPose.curve(bl, d);
        double d7 = bl2 ? -1.0 : 1.0;
        double d8 = BindingPose.clamp(d4);
        double d9 = Math.cos(d3 * 0.6662) * 1.4 * d8;
        d2 = BindingPose.clamp(d2);
        double[][] dArrayArray = new double[][]{{d6, d5, 0.0}, {0.0, 0.0, 0.0}, {-d9 * 0.5, 0.0, 0.02}, {d9 * 0.5, 0.0, -0.02}, {d9, 0.0, 0.0}, {-d9, 0.0, 0.0}};
        dArrayArray[bl2 ? 3 : 2][0] = -0.3141592653589793 - d9 * 0.22;
        dArrayArray[bl2 ? 3 : 2][2] = 0.06981317007977318 * d7;
        dArrayArray[bl2 ? 2 : 3][2] = -0.06981317007977318 * d7;
        double[][] dArray2 = new double[6][3];
        double[][] dArray3 = new double[6][3];
        for (int i = 0; i < 6; ++i) {
            dArray2[i] = (double[])BIND[i].clone();
            dArray3[i] = (double[])dArrayArray[i].clone();
        }
        double[] dArray4 = new double[]{dArray[0] * d7, dArray[1], dArray[2]};
        double[] dArray5 = new double[]{Math.toRadians(dArray[3]), Math.toRadians(dArray[4]) * d7, Math.toRadians(dArray[5]) * d7};
        for (int i = 0; i < 3; ++i) {
            dArray2[1][i] = dArray4[i] * d2;
            dArray3[1][i] = dArray5[i] * d2;
        }
        for (int n : new int[]{0, 2, 3}) {
            double[] dArray6;
            if (n == 0) {
                dArray6 = new double[]{d6 - dArray5[0] * 0.6, d5 - dArray5[1] * 0.7, -dArray5[2] * 0.65};
            } else {
                int n2 = n == (bl2 ? 3 : 2) ? 6 : 9;
                dArray6 = new double[]{Math.toRadians(dArray[n2]), Math.toRadians(dArray[n2 + 1]) * d7, Math.toRadians(dArray[n2 + 2]) * d7};
                dArray6[0] = dArray6[0] + dArrayArray[n][0] * d8 * 0.2;
            }
            double[] dArray7 = BindingPose.compose(dArray5, dArray6);
            double[] dArray8 = BindingPose.rotate(BIND[n], dArray5);
            for (int i = 0; i < 3; ++i) {
                dArray2[n][i] = BIND[n][i] + (dArray8[i] + dArray4[i] - BIND[n][i]) * d2;
                dArray3[n][i] = dArrayArray[n][i] + (dArray7[i] - dArrayArray[n][i]) * d2;
            }
        }
        for (int n : new int[]{4, 5}) {
            int n3 = bl2 ? 9 - n : n;
            int n4 = 12 + (n3 - 4) * 3;
            for (int i = 0; i < 3; ++i) {
                dArray3[n][i] = dArrayArray[n][i] + Math.toRadians(dArray[n4 + i]) * (i == 0 ? 1.0 : d7) * d2 * (1.0 - d8);
            }
            double d10 = 1.0 - BindingPose.ease(d8 * 2.0);
            double[] dArray9 = BindingPose.rotate(new double[]{0.0, 12.0, 0.0}, dArray3[n]);
            double[] dArray10 = dArray2[n];
            dArray10[0] = dArray10[0] - dArray9[0] * d10;
            double[] dArray11 = dArray2[n];
            dArray11[2] = dArray11[2] - dArray9[2] * d10;
            double d11 = -1.7976931348623157E308;
            for (double d12 : new double[]{-2.0, 2.0}) {
                for (double d13 : new double[]{0.0, 12.0}) {
                    for (double d14 : new double[]{-2.0, 2.0}) {
                        d11 = Math.max(d11, BindingPose.rotate(new double[]{d12, d13, d14}, dArray3[n])[1]);
                    }
                }
            }
            dArray2[n][1] = Math.min(12.0, 24.0 - d11);
        }
        return new BindingPose(dArray2, dArray3);
    }
}

