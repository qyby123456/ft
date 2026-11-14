package net.farmingtales.ftboss.zheergenremainingbody.client;

public final class CrossPose {
    private static final double[] CHARGE_TIMES = new double[]{0.0, 0.24, 0.63, 1.0};
    private static final double[] RELEASE_TIMES = new double[]{0.0, 0.12, 0.28, 0.42, 0.56, 0.76, 1.0};
    private static final double[][] CHARGE_VALUES = new double[][]{
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -18.0, 0.0, 4.0, 0.0, 0.0, -4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {0.0, 0.0, 0.0, 2.0, -5.0, 1.0, -30.0, 16.0, 20.0, -15.0, -8.0, -13.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {0.1, 0.1, 0.05, 3.0, -13.0, 3.0, -48.0, 28.0, 36.0, -25.0, -15.0, -23.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {0.12, 0.12, 0.06, 2.0, -15.0, 3.0, -52.0, 32.0, 40.0, -28.0, -16.0, -25.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    };
    private static final double[][] RELEASE_VALUES = new double[][]{
        {0.12, 0.12, 0.06, 2.0, -15.0, 3.0, -52.0, 32.0, 40.0, -28.0, -16.0, -25.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {-0.16, 0.22, -0.06, 7.0, 13.0, -2.0, -25.0, 42.0, 36.0, -36.0, -20.0, -29.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {-0.12, 0.16, -0.04, 6.0, 8.0, -1.0, -22.0, 34.0, 28.0, -30.0, -15.0, -25.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {0.05, 0.1, 0.0, -3.0, 12.0, -2.0, -48.0, 44.0, 62.0, -32.0, -20.0, -28.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {0.16, 0.26, -0.08, 9.0, -12.0, 2.0, -28.0, 40.0, 32.0, -40.0, -25.0, -30.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {0.04, 0.08, 0.0, 3.0, -4.0, 1.0, -24.0, 23.0, 18.0, -22.0, -10.0, -14.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
        {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -18.0, 0.0, 4.0, 0.0, 0.0, -4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}
    };
    private static final double[][] CHARGE_SLOPES = slopes(CHARGE_TIMES, CHARGE_VALUES);
    private static final double[][] RELEASE_SLOPES = slopes(RELEASE_TIMES, RELEASE_VALUES);
    private static final double[][] BIND = new double[][]{
        {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {-5.0, 2.5, 0.0}, {5.0, 2.5, 0.0}, {-1.9, 12.0, 0.0}, {1.9, 12.0, 0.0}
    };
    public final double[][] position;
    public final double[][] rotation;

    private static double[][] slopes(double[] var0, double[][] var1) {
        double[][] var2 = new double[var0.length][18];

        for (int var3 = 0; var3 < var0.length; var3++) {
            for (int var4 = 0; var4 < 18; var4++) {
                var2[var3][var4] = slope(var0, var1, var3, var4);
            }
        }

        return var2;
    }

    private CrossPose(double[][] var1, double[][] var2) {
        this.position = var1;
        this.rotation = var2;
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    private static double ease(double var0) {
        double var2 = clamp(var0);
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    private static double slope(double[] var0, double[][] var1, int var2, int var3) {
        if (var2 != 0 && var2 != var0.length - 1) {
            double var4 = var0[var2] - var0[var2 - 1];
            double var6 = var0[var2 + 1] - var0[var2];
            double var8 = (var1[var2][var3] - var1[var2 - 1][var3]) / var4;
            double var10 = (var1[var2 + 1][var3] - var1[var2][var3]) / var6;
            if (var8 * var10 <= 0.0) {
                return 0.0;
            } else {
                double var12 = 2.0 * var6 + var4;
                double var14 = var6 + 2.0 * var4;
                return (var12 + var14) / (var12 / var8 + var14 / var10);
            }
        } else {
            return 0.0;
        }
    }

    public static double[] curve(boolean var0, double var1) {
        double[] var3 = var0 ? RELEASE_TIMES : CHARGE_TIMES;
        double[][] var4 = var0 ? RELEASE_VALUES : CHARGE_VALUES;
        double var5 = clamp(var1);
        int var7 = 1;

        while (var7 < var3.length - 1 && var3[var7] < var5) {
            var7++;
        }

        int var8 = var7 - 1;
        double var9 = var3[var7] - var3[var8];
        double var11 = (var5 - var3[var8]) / var9;
        double var13 = var11 * var11;
        double var15 = var13 * var11;
        double[] var17 = new double[18];

        for (int var18 = 0; var18 < 18; var18++) {
            var17[var18] = (2.0 * var15 - 3.0 * var13 + 1.0) * var4[var8][var18]
                + (var15 - 2.0 * var13 + var11) * var9 * (var0 ? RELEASE_SLOPES : CHARGE_SLOPES)[var8][var18]
                + (-2.0 * var15 + 3.0 * var13) * var4[var7][var18]
                + (var15 - var13) * var9 * (var0 ? RELEASE_SLOPES : CHARGE_SLOPES)[var7][var18];
        }

        return var17;
    }

    public static double[] rotate(double[] var0, double[] var1) {
        double var2 = var1[0];
        double var4 = var1[1];
        double var6 = var1[2];
        double var8 = var0[1] * Math.cos(var2) - var0[2] * Math.sin(var2);
        double var10 = var0[1] * Math.sin(var2) + var0[2] * Math.cos(var2);
        double var12 = var0[0] * Math.cos(var4) + var10 * Math.sin(var4);
        var10 = -var0[0] * Math.sin(var4) + var10 * Math.cos(var4);
        return new double[]{var12 * Math.cos(var6) - var8 * Math.sin(var6), var12 * Math.sin(var6) + var8 * Math.cos(var6), var10};
    }

    private static double[] compose(double[] var0, double[] var1) {
        double[][] var2 = new double[][]{
            rotate(rotate(new double[]{1.0, 0.0, 0.0}, var1), var0),
            rotate(rotate(new double[]{0.0, 1.0, 0.0}, var1), var0),
            rotate(rotate(new double[]{0.0, 0.0, 1.0}, var1), var0)
        };
        return new double[]{Math.atan2(var2[1][2], var2[2][2]), Math.asin(Math.max(-1.0, Math.min(1.0, -var2[0][2]))), Math.atan2(var2[0][1], var2[0][0])};
    }

    public static CrossPose sample(boolean var0, double var1, double var3, boolean var5, double var6, double var8, double var10, double var12) {
        double[] var14 = curve(var0, var1);
        double var15 = var5 ? -1.0 : 1.0;
        double var17 = clamp(var8);
        double var19 = Math.cos(var6 * 0.6662) * 1.4 * var17;
        var3 = clamp(var3);
        double[][] var21 = new double[][]{
            {var12, var10, 0.0}, {0.0, 0.0, 0.0}, {-var19 * 0.5, 0.0, 0.02}, {var19 * 0.5, 0.0, -0.02}, {var19, 0.0, 0.0}, {-var19, 0.0, 0.0}
        };
        var21[var5 ? 3 : 2][0] = (-Math.PI / 10) - var19 * 0.22;
        var21[var5 ? 3 : 2][2] = 0.06981317007977318 * var15;
        var21[var5 ? 2 : 3][2] = -0.06981317007977318 * var15;
        double[][] var22 = new double[6][3];
        double[][] var23 = new double[6][3];

        for (int var24 = 0; var24 < 6; var24++) {
            var22[var24] = (double[])BIND[var24].clone();
            var23[var24] = (double[])var21[var24].clone();
        }

        double[] var53 = new double[]{var14[0] * var15, var14[1], var14[2]};
        double[] var25 = new double[]{Math.toRadians(var14[3]), Math.toRadians(var14[4]) * var15, Math.toRadians(var14[5]) * var15};

        for (int var26 = 0; var26 < 3; var26++) {
            var22[1][var26] = var53[var26] * var3;
            var23[1][var26] = var25[var26] * var3;
        }

        for (int var29 : new int[]{0, 2, 3}) {
            double[] var30;
            if (var29 == 0) {
                var30 = new double[]{var12 - var25[0] * 0.6, var10 - var25[1] * 0.7, -var25[2] * 0.65};
            } else {
                int var31 = var29 == (var5 ? 3 : 2) ? 6 : 9;
                var30 = new double[]{Math.toRadians(var14[var31]), Math.toRadians(var14[var31 + 1]) * var15, Math.toRadians(var14[var31 + 2]) * var15};
                var30[0] += var21[var29][0] * var17 * 0.2;
            }

            double[] var60 = compose(var25, var30);
            double[] var32 = rotate(BIND[var29], var25);

            for (int var33 = 0; var33 < 3; var33++) {
                var22[var29][var33] = BIND[var29][var33] + (var32[var33] + var53[var33] - BIND[var29][var33]) * var3;
                var23[var29][var33] = var21[var29][var33] + (var60[var33] - var21[var29][var33]) * var3;
            }
        }

        for (int var58 : new int[]{4, 5}) {
            int var59 = var5 ? 9 - var58 : var58;
            int var61 = 12 + (var59 - 4) * 3;

            for (int var62 = 0; var62 < 3; var62++) {
                var23[var58][var62] = var21[var58][var62] + Math.toRadians(var14[var61 + var62]) * (var62 == 0 ? 1.0 : var15) * var3 * (1.0 - var17);
            }

            double var63 = 1.0 - ease(var17 * 2.0);
            double[] var34 = rotate(new double[]{0.0, 12.0, 0.0}, var23[var58]);
            var22[var58][0] = var22[var58][0] - var34[0] * var63;
            var22[var58][2] = var22[var58][2] - var34[2] * var63;
            double var35 = -Double.MAX_VALUE;

            for (double var40 : new double[]{-2.0, 2.0}) {
                for (double var45 : new double[]{0.0, 12.0}) {
                    for (double var50 : new double[]{-2.0, 2.0}) {
                        var35 = Math.max(var35, rotate(new double[]{var40, var45, var50}, var23[var58])[1]);
                    }
                }
            }

            var22[var58][1] = Math.min(12.0, 24.0 - var35);
        }

        return new CrossPose(var22, var23);
    }
}
