package net.farmingtales.ftboss.client.render;

import java.util.Arrays;

public final class OptimizedSpellSafety {
    private OptimizedSpellSafety() {
    }

    public static double[] direction(double var0, double var2, double var4) {
        double var6 = Math.sqrt(var0 * var0 + var2 * var2 + var4 * var4);
        return Double.isFinite(var6) && !(var6 < 1.0E-5) ? new double[]{var0 / var6, var2 / var6, var4 / var6} : new double[]{0.0, 0.0, 1.0};
    }

    public static double distanceSquared(double[][] var0, double[] var1) {
        double var2 = 0.0;

        for (int var4 = 0; var4 < 3; var4++) {
            double var5 = Double.POSITIVE_INFINITY;
            double var7 = Double.NEGATIVE_INFINITY;

            for (double[] var12 : var0) {
                if (!Double.isFinite(var12[var4])) {
                    return -1.0;
                }

                var5 = Math.min(var5, var12[var4]);
                var7 = Math.max(var7, var12[var4]);
            }

            double var13 = Math.max(0.0, Math.max(var5 - var1[var4], var1[var4] - var7));
            var2 += var13 * var13;
        }

        return var2;
    }

    public static float cameraOpacity(double[][] var0, double[] var1, boolean var2) {
        if (var2) {
            return FirstPersonSpellVisibility.geometryOpacity(var0);
        } else {
            double var3 = distanceSquared(var0, var1);
            if (!(var3 < 0.0) && Double.isFinite(var3)) {
                double var5 = var2 ? 0.3 : 0.12;
                double var7 = var2 ? 0.7 : 0.35;
                double var9 = Math.max(0.0, Math.min(1.0, (Math.sqrt(var3) - var5) / (var7 - var5)));
                return (float)(var9 * var9 * (3.0 - 2.0 * var9));
            } else {
                return 0.0F;
            }
        }
    }

    public static long signature(double[][] var0) {
        long[] var1 = new long[4];

        for (int var2 = 0; var2 < 4; var2++) {
            long var3 = -3750763034362895579L;

            for (int var5 = 0; var5 < 3; var5++) {
                var3 = (var3 ^ Math.round(var0[var2][var5] * 100000.0)) * 1099511628211L;
            }

            var1[var2] = var3;
        }

        Arrays.sort(var1);
        long var9 = -3750763034362895579L;

        for (long var7 : var1) {
            var9 = (var9 ^ var7) * 1099511628211L;
        }

        return var9;
    }

    public static double area(double[][] var0) {
        return triangle(var0[0], var0[1], var0[2]) + triangle(var0[0], var0[2], var0[3]);
    }

    private static double triangle(double[] var0, double[] var1, double[] var2) {
        double var3 = var1[0] - var0[0];
        double var5 = var1[1] - var0[1];
        double var7 = var1[2] - var0[2];
        double var9 = var2[0] - var0[0];
        double var11 = var2[1] - var0[1];
        double var13 = var2[2] - var0[2];
        double var15 = var5 * var13 - var7 * var11;
        double var17 = var7 * var9 - var3 * var13;
        double var19 = var3 * var11 - var5 * var9;
        return Math.sqrt(var15 * var15 + var17 * var17 + var19 * var19) * 0.5;
    }
}
