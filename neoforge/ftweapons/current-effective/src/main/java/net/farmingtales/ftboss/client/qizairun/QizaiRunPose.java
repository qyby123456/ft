package net.farmingtales.ftboss.client.qizairun;

public final class QizaiRunPose {
    private QizaiRunPose() {
    }

    public static double clamp(double var0, double var2, double var4) {
        return Math.max(var2, Math.min(var4, var0));
    }

    public static double smooth(double var0) {
        double var2 = clamp(var0, 0.0, 1.0);
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static double[] neutral() {
        return new double[]{
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            -5.0,
            2.5,
            0.0,
            -0.18,
            0.0,
            0.08,
            5.0,
            2.5,
            0.0,
            0.0,
            0.0,
            -0.08,
            -1.9,
            12.0,
            0.0,
            0.0,
            0.0,
            0.0,
            1.9,
            12.0,
            0.0,
            0.0,
            0.0,
            0.0
        };
    }

    private static double[] rotate(double var0, double var2, double var4, double var6, double var8, double var10) {
        double var12 = var2 * Math.cos(var6) - var4 * Math.sin(var6);
        double var14 = var2 * Math.sin(var6) + var4 * Math.cos(var6);
        double var16 = var0 * Math.cos(var8) + var14 * Math.sin(var8);
        double var18 = -var0 * Math.sin(var8) + var14 * Math.cos(var8);
        return new double[]{var16 * Math.cos(var10) - var12 * Math.sin(var10), var16 * Math.sin(var10) + var12 * Math.cos(var10), var18};
    }

    public static double footBottom(double[] var0, int var1) {
        int var2 = var1 * 6;
        double var3 = Double.NEGATIVE_INFINITY;

        for (double var8 : new double[]{-2.0, 2.0}) {
            for (double var13 : new double[]{-2.0, 2.0}) {
                var3 = Math.max(var3, var0[var2 + 1] + rotate(var8, 12.0, var13, var0[var2 + 3], var0[var2 + 4], var0[var2 + 5])[1]);
            }
        }

        return var3;
    }

    public static void apply(
        double[] var0, double var1, double var3, double var5, boolean var7, boolean var8, boolean var9, double var10, double var12, double var14
    ) {
        if (var0.length != 36) {
            throw new IllegalArgumentException("Expected six model parts");
        } else if (Double.isFinite(var1)
            && Double.isFinite(var3)
            && Double.isFinite(var5)
            && Double.isFinite(var10)
            && Double.isFinite(var12)
            && Double.isFinite(var14)
            && !(var5 <= 0.001)) {
            for (double var19 : var0) {
                if (!Double.isFinite(var19)) {
                    return;
                }
            }

            var5 = clamp(var5, 0.0, 1.0);
            var10 = clamp(var10, 0.0, 1.0);
            boolean var55 = var10 <= 0.001;
            double[] var56 = (double[])var0.clone();
            double var57 = smooth((var3 - 0.24) / 0.4);
            double var20 = var1 + (var7 ? Math.PI : 0.0);
            double var22 = var7 ? -1.0 : 1.0;
            double var24 = 1.0 - 2.0 * smooth(-var12);
            double var26 = clamp(Math.atan2(var14, Math.abs(var12)) * 0.42, -0.52, 0.52);
            double var28 = (0.11 + 0.07 * var57) * var24;
            double var30 = 0.035 * Math.sin(var20) * var22;
            double var32 = 0.018 * Math.sin(var20) * var22 - 0.045 * var14;
            if (!var55) {
                var56[9] = var28;
                var56[10] = var30;
                var56[11] = var32;
                var56[3] = var0[3] - 0.32 * var28;
                var56[4] = var0[4];
                var56[5] = -0.35 * var32;

                for (int var34 = 2; var34 < 4; var34++) {
                    int var35 = var34 * 6;
                    boolean var36 = var34 == (var7 ? 3 : 2);
                    boolean var37 = var36 ? var8 : var9;
                    double var38 = var34 == 2 ? -1.0 : 1.0;
                    double var40 = var20 + (var34 == 2 ? Math.PI : 0.0);
                    var56[var35 + 3] = var28 + (var37 ? -0.78 : -0.18) + (var37 ? 0.055 : 0.42) * Math.sin(var40) * var24;
                    var56[var35 + 4] = var30 + var38 * (var37 ? 0.1 : 0.05);
                    var56[var35 + 5] = var32 - var38 * (var37 ? 0.16 : 0.12);
                }
            }

            for (int var58 = 0; var58 < 24; var58++) {
                var56[var58] = var0[var58] + (var56[var58] - var0[var58]) * var10;
            }

            double var59 = var56[9];
            double var60 = var56[10];
            double var61 = var56[11];

            for (int var62 = 4; var62 < 6; var62++) {
                int var41 = var62 * 6;
                double var42 = (var20 + (var62 == 4 ? 0.0 : Math.PI)) / (Math.PI * 2) % 1.0;
                if (var42 < 0.0) {
                    var42++;
                }

                double var44 = 0.54;
                double var46 = var42 < var44 ? var42 / var44 : (var42 - var44) / (1.0 - var44);
                double var48 = (var42 < var44 ? 0.68 - 1.44 * smooth(var46) : -0.76 + 1.44 * smooth(var46)) * (0.93 + 0.07 * var57) * var24;
                double var50 = var42 < var44 ? 0.0 : (1.15 + 0.4 * var57) * Math.pow(Math.sin(var46 * Math.PI), 2.0);
                double[] var52 = rotate(var62 == 4 ? -1.9 : 1.9, 12.0, 0.0, var59, var60, var61);
                var56[var41] = var56[6] + var52[0];
                var56[var41 + 1] = var56[7] + var52[1] - var50;
                var56[var41 + 2] = var56[8] + var52[2];
                var56[var41 + 3] = var48;
                var56[var41 + 4] = var26;
                var56[var41 + 5] = (double)(var62 == 4 ? 1 : -1) * 0.025;
            }

            if (!var55) {
                double[] var63 = rotate(0.0, 12.0, 0.0, var59, var60, var61);
                var56[6] = -var63[0];
                var56[7] = 12.0 - var63[1];
                var56[8] = -var63[2];

                for (int var66 = 4; var66 < 6; var66++) {
                    var56[var66 * 6] = var56[var66 * 6] - var63[0];
                    var56[var66 * 6 + 1] = var56[var66 * 6 + 1] + (12.0 - var63[1]);
                    var56[var66 * 6 + 2] = var56[var66 * 6 + 2] - var63[2];
                }

                var56[0] = var56[6];
                var56[1] = var56[7];
                var56[2] = var56[8];

                for (int var67 = 2; var67 < 4; var67++) {
                    double[] var68 = rotate(var67 == 2 ? -5.0 : 5.0, 2.5, 0.0, var59, var60, var61);

                    for (int var43 = 0; var43 < 3; var43++) {
                        var56[var67 * 6 + var43] = var56[6 + var43] + var68[var43];
                    }
                }
            }

            for (int var64 = 0; var64 < 36; var64++) {
                if (!var55 || var64 >= 24) {
                    var0[var64] += (var56[var64] - var0[var64]) * var5;
                }
            }

            double var65 = Math.max(footBottom(var0, 4), footBottom(var0, 5));
            double var69 = (24.0 - var65) * smooth(var5);

            for (int var70 = 0; var70 < 6; var70++) {
                var0[var70 * 6 + 1] = var0[var70 * 6 + 1] + var69;
            }
        }
    }
}
