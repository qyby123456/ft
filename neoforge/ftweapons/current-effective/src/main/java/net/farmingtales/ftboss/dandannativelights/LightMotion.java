package net.farmingtales.ftboss.dandannativelights;

public final class LightMotion {
    private LightMotion() {
    }

    public static double partRadius(double var0, double var2) {
        return var0 > 0.0 ? var0 : var2;
    }

    public static double[] sample(double[][] var0, double var1) {
        double var3 = Math.max(0.0, Math.min(1.0, var1));
        int var5 = 1;

        while (var5 < var0.length - 1 && var3 > var0[var5][0]) {
            var5++;
        }

        double[] var6 = var0[var5 - 1];
        double[] var7 = var0[var5];
        double[] var8 = new double[8];
        double var9 = Math.max(0.0, Math.min(1.0, (var3 - var6[0]) / (var7[0] - var6[0])));

        for (int var11 = 0; var11 < var8.length; var11++) {
            var8[var11] = var6[var11 + 1] * (1.0 - var9) + var7[var11 + 1] * var9;
        }

        return var8;
    }

    public static double[] transform(double[] var0, double[] var1, double var2) {
        double var4 = var0[0] * var1[3];
        double var6 = var0[2] * var1[5];
        return new double[]{
            (var4 * Math.cos(var1[6]) + var6 * Math.sin(var1[6]) + var1[0]) * var2,
            var0[1] * var1[4] + var1[1],
            (-var4 * Math.sin(var1[6]) + var6 * Math.cos(var1[6]) + var1[2]) * var2
        };
    }

    public static double[] worldTransform(double[] var0, double var1, double var3, double[] var5) {
        double var6 = Math.cos(var0[6]);
        double var8 = Math.sin(var0[6]);
        double var10 = Math.cos(var3);
        double var12 = Math.sin(var3);
        double var14 = var6 * var10 - var8 * var12;
        double var16 = var8 * var10 + var6 * var12;
        return new double[]{
            var1 * var0[3] * var14,
            var1 * var0[5] * var16,
            var5[0] + var1 * (var0[0] * var10 + var0[2] * var12),
            var0[4],
            var5[1] + var0[1],
            -var1 * var0[3] * var16,
            var1 * var0[5] * var14,
            var5[2] + var1 * (-var0[0] * var12 + var0[2] * var10)
        };
    }

    public static void transformInto(double[] var0, double[] var1, double[] var2) {
        var2[0] = var0[0] * var1[0] + var0[2] * var1[1] + var1[2];
        var2[1] = var0[1] * var1[3] + var1[4];
        var2[2] = var0[0] * var1[5] + var0[2] * var1[6] + var1[7];
    }

    public static double sightFade(double[] var0, double[] var1, double[] var2) {
        double var3 = 0.0;

        for (int var5 = 0; var5 < 3; var5++) {
            double var6 = Math.max(0.0, Math.max(var0[var5] - var1[var5], var1[var5] - var0[var5 + 3]));
            var3 += var6 * var6;
        }

        if (var3 < 0.16) {
            return 0.08;
        } else {
            double var14 = 0.3;
            double var7 = 3.0;

            for (int var9 = 0; var9 < 3; var9++) {
                if (Math.abs(var2[var9]) < 1.0E-8) {
                    if (var1[var9] < var0[var9] - 0.06 || var1[var9] > var0[var9 + 3] + 0.06) {
                        return 1.0;
                    }
                } else {
                    double var10 = (var0[var9] - 0.06 - var1[var9]) / var2[var9];
                    double var12 = (var0[var9 + 3] + 0.06 - var1[var9]) / var2[var9];
                    var14 = Math.max(var14, Math.min(var10, var12));
                    var7 = Math.min(var7, Math.max(var10, var12));
                    if (var14 > var7) {
                        return 1.0;
                    }
                }
            }

            return 0.12;
        }
    }
}
