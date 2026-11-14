package net.farmingtales.ftboss.zheergenfx.client;

import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;

public final class LightSafety {
    private LightSafety() {
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    private static double dot(double[] var0, double[] var1) {
        return var0[0] * var1[0] + var0[1] * var1[1] + var0[2] * var1[2];
    }

    private static double[] subtract(double[] var0, double[] var1) {
        return new double[]{var0[0] - var1[0], var0[1] - var1[1], var0[2] - var1[2]};
    }

    private static double[] cross(double[] var0, double[] var1) {
        return new double[]{var0[1] * var1[2] - var0[2] * var1[1], var0[2] * var1[0] - var0[0] * var1[2], var0[0] * var1[1] - var0[1] * var1[0]};
    }

    private static double edge(double[] var0, double[] var1, double[] var2) {
        double[] var3 = subtract(var2, var1);
        double[] var4 = subtract(var0, var1);
        double var5 = dot(var3, var3);
        double var7 = var5 < 1.0E-20 ? 0.0 : clamp(dot(var4, var3) / var5);
        double var9 = 0.0;

        for (int var11 = 0; var11 < 3; var11++) {
            var9 += Math.pow(var4[var11] - var3[var11] * var7, 2.0);
        }

        return var9;
    }

    private static double triangle(double[] var0, double[] var1, double[] var2, double[] var3) {
        double[] var4 = cross(subtract(var2, var1), subtract(var3, var1));
        double var5 = dot(var4, var4);
        if (var5 > 1.0E-20) {
            double var7 = dot(subtract(var0, var1), var4) / var5;
            double[] var9 = new double[]{var0[0] - var4[0] * var7, var0[1] - var4[1] * var7, var0[2] - var4[2] * var7};
            double[][] var10 = new double[][]{var1, var2, var3};
            boolean var11 = true;

            for (int var12 = 0; var12 < 3; var12++) {
                if (dot(cross(subtract(var10[(var12 + 1) % 3], var10[var12]), subtract(var9, var10[var12])), var4) < -1.0E-12) {
                    var11 = false;
                }
            }

            if (var11) {
                return var7 * var7 * var5;
            }
        }

        return Math.min(edge(var0, var1, var2), Math.min(edge(var0, var2, var3), edge(var0, var3, var1)));
    }

    public static float camera(double[][] var0, double[] var1) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return 1.0F;
        } else {
            double var2 = Math.sqrt(Math.min(triangle(var1, var0[0], var0[1], var0[2]), triangle(var1, var0[0], var0[2], var0[3])));
            return (float)clamp((var2 - 0.18) / 0.44);
        }
    }

    public static float actor(double[][] var0, double[] var1) {
        double var2 = 0.0;

        for (int var4 = 0; var4 < 3; var4++) {
            double var5 = Double.POSITIVE_INFINITY;
            double var7 = Double.NEGATIVE_INFINITY;

            for (double[] var12 : var0) {
                var5 = Math.min(var5, var12[var4]);
                var7 = Math.max(var7, var12[var4]);
            }

            double var13 = Math.max(0.0, Math.max(var1[var4] - var7, var5 - var1[var4 + 3]));
            var2 += var13 * var13;
        }

        return (float)(0.075 + 0.925 * clamp(Math.sqrt(var2) / 0.32));
    }

    public static float sight(double[][] var0, double[] var1, double[] var2) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return 1.0F;
        } else {
            double var3 = 1.0;

            for (int var5 = 0; var5 < 5; var5++) {
                double[] var6 = new double[3];

                for (int var7 = 0; var7 < 3; var7++) {
                    double var8 = var5 < 4 ? var0[var5][var7] : (var0[0][var7] + var0[1][var7] + var0[2][var7] + var0[3][var7]) * 0.25;
                    var6[var7] = var8 - var1[var7];
                }

                double var13 = dot(var6, var2);
                if (!(var13 <= 0.18) && !(var13 > 6.0)) {
                    double var9 = 0.105 + var13 * 0.052;
                    double var11 = Math.sqrt(Math.max(0.0, dot(var6, var6) - var13 * var13));
                    var3 = Math.min(var3, 0.2 + 0.8 * clamp((var11 - var9 * 0.45) / (var9 * 0.85)));
                }
            }

            return (float)var3;
        }
    }

    public static float sightVertex(double[] var0, double[] var1, double[] var2) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return 1.0F;
        } else {
            double[] var3 = subtract(var0, var1);
            double var4 = dot(var3, var2);
            if (!(var4 <= 0.18) && !(var4 > 6.0)) {
                double var6 = 0.105 + var4 * 0.052;
                double var8 = Math.sqrt(Math.max(0.0, dot(var3, var3) - var4 * var4));
                return (float)(0.2 + 0.8 * clamp((var8 - var6 * 0.45) / (var6 * 0.85)));
            } else {
                return 1.0F;
            }
        }
    }
}
