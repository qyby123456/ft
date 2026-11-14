/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.zheergenfx.client;

import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;

public final class LightSafety {
    private LightSafety() {
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    private static double dot(double[] dArray, double[] dArray2) {
        return dArray[0] * dArray2[0] + dArray[1] * dArray2[1] + dArray[2] * dArray2[2];
    }

    private static double[] subtract(double[] dArray, double[] dArray2) {
        return new double[]{dArray[0] - dArray2[0], dArray[1] - dArray2[1], dArray[2] - dArray2[2]};
    }

    private static double[] cross(double[] dArray, double[] dArray2) {
        return new double[]{dArray[1] * dArray2[2] - dArray[2] * dArray2[1], dArray[2] * dArray2[0] - dArray[0] * dArray2[2], dArray[0] * dArray2[1] - dArray[1] * dArray2[0]};
    }

    private static double edge(double[] dArray, double[] dArray2, double[] dArray3) {
        double[] dArray4 = LightSafety.subtract(dArray3, dArray2);
        double[] dArray5 = LightSafety.subtract(dArray, dArray2);
        double d = LightSafety.dot(dArray4, dArray4);
        double d2 = d < 1.0E-20 ? 0.0 : LightSafety.clamp(LightSafety.dot(dArray5, dArray4) / d);
        double d3 = 0.0;
        for (int i = 0; i < 3; ++i) {
            d3 += Math.pow(dArray5[i] - dArray4[i] * d2, 2.0);
        }
        return d3;
    }

    private static double triangle(double[] dArray, double[] dArray2, double[] dArray3, double[] dArray4) {
        double[] dArray5 = LightSafety.cross(LightSafety.subtract(dArray3, dArray2), LightSafety.subtract(dArray4, dArray2));
        double d = LightSafety.dot(dArray5, dArray5);
        if (d > 1.0E-20) {
            double d2 = LightSafety.dot(LightSafety.subtract(dArray, dArray2), dArray5) / d;
            double[] dArray6 = new double[]{dArray[0] - dArray5[0] * d2, dArray[1] - dArray5[1] * d2, dArray[2] - dArray5[2] * d2};
            double[][] dArrayArray = new double[][]{dArray2, dArray3, dArray4};
            boolean bl = true;
            for (int i = 0; i < 3; ++i) {
                if (!(LightSafety.dot(LightSafety.cross(LightSafety.subtract(dArrayArray[(i + 1) % 3], dArrayArray[i]), LightSafety.subtract(dArray6, dArrayArray[i])), dArray5) < -1.0E-12)) continue;
                bl = false;
            }
            if (bl) {
                return d2 * d2 * d;
            }
        }
        return Math.min(LightSafety.edge(dArray, dArray2, dArray3), Math.min(LightSafety.edge(dArray, dArray3, dArray4), LightSafety.edge(dArray, dArray4, dArray2)));
    }

    public static float camera(double[][] dArray, double[] dArray2) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return 1.0f;
        }
        double d = Math.sqrt(Math.min(LightSafety.triangle(dArray2, dArray[0], dArray[1], dArray[2]), LightSafety.triangle(dArray2, dArray[0], dArray[2], dArray[3])));
        return (float)LightSafety.clamp((d - 0.18) / 0.44);
    }

    public static float actor(double[][] dArray, double[] dArray2) {
        double d = 0.0;
        for (int i = 0; i < 3; ++i) {
            double d2 = Double.POSITIVE_INFINITY;
            double d3 = Double.NEGATIVE_INFINITY;
            for (double[] dArray3 : dArray) {
                d2 = Math.min(d2, dArray3[i]);
                d3 = Math.max(d3, dArray3[i]);
            }
            double d4 = Math.max(0.0, Math.max(dArray2[i] - d3, d2 - dArray2[i + 3]));
            d += d4 * d4;
        }
        return (float)(0.075 + 0.925 * LightSafety.clamp(Math.sqrt(d) / 0.32));
    }

    public static float sight(double[][] dArray, double[] dArray2, double[] dArray3) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return 1.0f;
        }
        double d = 1.0;
        for (int i = 0; i < 5; ++i) {
            double[] dArray4 = new double[3];
            for (int j = 0; j < 3; ++j) {
                double d2 = i < 4 ? dArray[i][j] : (dArray[0][j] + dArray[1][j] + dArray[2][j] + dArray[3][j]) * 0.25;
                dArray4[j] = d2 - dArray2[j];
            }
            double d3 = LightSafety.dot(dArray4, dArray3);
            if (d3 <= 0.18 || d3 > 6.0) continue;
            double d4 = 0.105 + d3 * 0.052;
            double d5 = Math.sqrt(Math.max(0.0, LightSafety.dot(dArray4, dArray4) - d3 * d3));
            d = Math.min(d, 0.2 + 0.8 * LightSafety.clamp((d5 - d4 * 0.45) / (d4 * 0.85)));
        }
        return (float)d;
    }

    public static float sightVertex(double[] dArray, double[] dArray2, double[] dArray3) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return 1.0f;
        }
        double[] dArray4 = LightSafety.subtract(dArray, dArray2);
        double d = LightSafety.dot(dArray4, dArray3);
        if (d <= 0.18 || d > 6.0) {
            return 1.0f;
        }
        double d2 = 0.105 + d * 0.052;
        double d3 = Math.sqrt(Math.max(0.0, LightSafety.dot(dArray4, dArray4) - d * d));
        return (float)(0.2 + 0.8 * LightSafety.clamp((d3 - d2 * 0.45) / (d2 * 0.85)));
    }
}

