/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

import java.util.Arrays;
import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;

public final class OptimizedSpellSafety {
    private OptimizedSpellSafety() {
    }

    public static double[] direction(double d, double d2, double d3) {
        double d4 = Math.sqrt(d * d + d2 * d2 + d3 * d3);
        if (!Double.isFinite(d4) || d4 < 1.0E-5) {
            return new double[]{0.0, 0.0, 1.0};
        }
        return new double[]{d / d4, d2 / d4, d3 / d4};
    }

    public static double distanceSquared(double[][] dArray, double[] dArray2) {
        double d = 0.0;
        for (int i = 0; i < 3; ++i) {
            double d2 = Double.POSITIVE_INFINITY;
            double d3 = Double.NEGATIVE_INFINITY;
            for (double[] dArray3 : dArray) {
                if (!Double.isFinite(dArray3[i])) {
                    return -1.0;
                }
                d2 = Math.min(d2, dArray3[i]);
                d3 = Math.max(d3, dArray3[i]);
            }
            double d4 = Math.max(0.0, Math.max(d2 - dArray2[i], dArray2[i] - d3));
            d += d4 * d4;
        }
        return d;
    }

    public static float cameraOpacity(double[][] dArray, double[] dArray2, boolean bl) {
        if (bl) {
            return FirstPersonSpellVisibility.geometryOpacity(dArray);
        }
        double d = OptimizedSpellSafety.distanceSquared(dArray, dArray2);
        if (d < 0.0 || !Double.isFinite(d)) {
            return 0.0f;
        }
        double d2 = bl ? 0.3 : 0.12;
        double d3 = bl ? 0.7 : 0.35;
        double d4 = Math.max(0.0, Math.min(1.0, (Math.sqrt(d) - d2) / (d3 - d2)));
        return (float)(d4 * d4 * (3.0 - 2.0 * d4));
    }

    public static long signature(double[][] dArray) {
        long[] lArray = new long[4];
        for (int i = 0; i < 4; ++i) {
            long l = -3750763034362895579L;
            for (int j = 0; j < 3; ++j) {
                l = (l ^ Math.round(dArray[i][j] * 100000.0)) * 1099511628211L;
            }
            lArray[i] = l;
        }
        Arrays.sort(lArray);
        long l = -3750763034362895579L;
        for (long l2 : lArray) {
            l = (l ^ l2) * 1099511628211L;
        }
        return l;
    }

    public static double area(double[][] dArray) {
        return OptimizedSpellSafety.triangle(dArray[0], dArray[1], dArray[2]) + OptimizedSpellSafety.triangle(dArray[0], dArray[2], dArray[3]);
    }

    private static double triangle(double[] dArray, double[] dArray2, double[] dArray3) {
        double d = dArray2[0] - dArray[0];
        double d2 = dArray2[1] - dArray[1];
        double d3 = dArray2[2] - dArray[2];
        double d4 = dArray3[0] - dArray[0];
        double d5 = dArray3[1] - dArray[1];
        double d6 = dArray3[2] - dArray[2];
        double d7 = d2 * d6 - d3 * d5;
        double d8 = d3 * d4 - d * d6;
        double d9 = d * d5 - d2 * d4;
        return Math.sqrt(d7 * d7 + d8 * d8 + d9 * d9) * 0.5;
    }
}

