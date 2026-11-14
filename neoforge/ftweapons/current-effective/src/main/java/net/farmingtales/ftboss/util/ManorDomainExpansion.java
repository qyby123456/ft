package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.ManorCeremonyModel.Sink;

public final class ManorDomainExpansion {
    public static final double DURATION = 64.0;

    private ManorDomainExpansion() {
    }

    public static double smooth(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, var0));
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static double scale(String var0, double var1) {
        return switch (var0) {
            case "sigil" -> smooth(var1 / 12.0);
            case "foundation" -> smooth((var1 - 8.0) / 24.0);
            case "boundary" -> smooth((var1 - 10.0) / 28.0);
            case "monuments" -> smooth((var1 - 26.0) / 22.0);
            default -> 0.0;
        };
    }

    public static double radius(double var0) {
        return 0.35 + 8.61 * smooth((var0 - 8.0) / 30.0);
    }

    public static double flare(double var0) {
        return smooth((var0 - 28.0) / 12.0) * (1.0 - smooth((var0 - 48.0) / 16.0));
    }

    public static void emit(int var0, int var1, int var2, double var3, boolean var5, float var6, Sink var7) {
        if (Double.isFinite(var3) && Float.isFinite(var6) && !(var3 < 0.0) && !(var6 <= 0.001F)) {
            var6 = Math.min(1.0F, var6);
            int var8 = var5 ? 64 : 128;
            double var9 = smooth(var3 / 6.0) * (1.0 - smooth((var3 - 42.0) / 18.0));
            if (var9 > 0.001) {
                annulus(var7, radius(var3), 0.045 + var9 * 0.065, 0.17, var8, var1, var6 * (float)(var9 * 0.8));
                annulus(var7, Math.max(0.25, radius(var3) - 0.22), 0.018, 0.19, var8, var2, var6 * (float)(var9 * 0.38));
            }

            double var11 = 0.7 + 0.15 * Math.sin(var3 * 0.035 + (double)var0);
            double var13 = smooth((var3 - 36.0) / 22.0);
            int var15 = var5 ? 8 : 16;

            for (int var16 = 0; var13 > 0.001 && var16 < var15; var16++) {
                double var17 = (double)var16 * Math.PI * 2.0 / (double)var15 + var3 * 0.003 * (var0 % 2 == 0 ? 1.0 : -1.0);
                arc(var7, 8.97, 0.044, var17, var17 + 0.045, 0.18, 2, var1, var6 * (float)(var13 * var11 * 0.6));
            }

            double var42 = flare(var3);
            if (!(var42 <= 0.001)) {
                int var18 = var5 ? 6 : 12;
                double var19 = (double)(var0 % 3 - 1) * 0.24;

                for (int var21 = 0; var21 < var18; var21++) {
                    double var22 = (double)var21 * Math.PI * 2.0 / (double)var18 + (double)var0 * 0.17;
                    double var24 = 1.5 + 0.5 * Math.sin((double)var21 * 1.7 + (double)var0);
                    int var26 = var5 ? 10 : 18;

                    for (int var27 = 0; var27 < var26; var27++) {
                        double var28 = (double)var27 / (double)var26;
                        double var30 = (double)(var27 + 1) / (double)var26;
                        ribbon(var7, var22, var19, var28, var30, var24 * var42, var1, var6 * (float)(var42 * Math.sin((var28 + var30) * Math.PI * 0.5) * 0.3));
                    }

                    double var43 = var42 * (0.6 + var24 * 0.55);
                    double var29 = 0.055;
                    double var31 = 8.7;
                    double var33 = Math.cos(var22) * var29;
                    double var35 = -Math.sin(var22) * var29;
                    double var37 = Math.sin(var22) * var31;
                    double var39 = Math.cos(var22) * var31;
                    quad(
                        var7,
                        var37 - var33,
                        0.22,
                        var39 - var35,
                        var37 + var33,
                        0.22,
                        var39 + var35,
                        var37 + var33 * 0.15,
                        var43,
                        var39 + var35 * 0.15,
                        var37 - var33 * 0.15,
                        var43,
                        var39 - var35 * 0.15,
                        var2,
                        var6 * (float)(var42 * 0.28)
                    );
                }
            }
        }
    }

    private static void ribbon(Sink var0, double var1, double var3, double var5, double var7, double var9, int var11, float var12) {
        double var13 = 1.4 + 7.3 * var5;
        double var15 = 1.4 + 7.3 * var7;
        double var17 = var1 + var3 * Math.sin(var5 * Math.PI);
        double var19 = var1 + var3 * Math.sin(var7 * Math.PI);
        double var21 = 0.22 + Math.sin(var5 * Math.PI) * var9;
        double var23 = 0.22 + Math.sin(var7 * Math.PI) * var9;
        double var25 = 0.025;
        quad(
            var0,
            Math.sin(var17 - var25 / var13) * var13,
            var21,
            Math.cos(var17 - var25 / var13) * var13,
            Math.sin(var17 + var25 / var13) * var13,
            var21,
            Math.cos(var17 + var25 / var13) * var13,
            Math.sin(var19 + var25 / var15) * var15,
            var23,
            Math.cos(var19 + var25 / var15) * var15,
            Math.sin(var19 - var25 / var15) * var15,
            var23,
            Math.cos(var19 - var25 / var15) * var15,
            var11,
            var12
        );
    }

    private static void annulus(Sink var0, double var1, double var3, double var5, int var7, int var8, float var9) {
        arc(var0, var1, var3, 0.0, Math.PI * 2, var5, var7, var8, var9);
    }

    private static void arc(Sink var0, double var1, double var3, double var5, double var7, double var9, int var11, int var12, float var13) {
        for (int var14 = 0; var14 < var11; var14++) {
            double var15 = var5 + (var7 - var5) * (double)var14 / (double)var11;
            double var17 = var5 + (var7 - var5) * (double)(var14 + 1) / (double)var11;
            double var19 = var1 - var3 * 0.5;
            double var21 = var1 + var3 * 0.5;
            quad(
                var0,
                Math.sin(var15) * var19,
                var9,
                Math.cos(var15) * var19,
                Math.sin(var15) * var21,
                var9,
                Math.cos(var15) * var21,
                Math.sin(var17) * var21,
                var9,
                Math.cos(var17) * var21,
                Math.sin(var17) * var19,
                var9,
                Math.cos(var17) * var19,
                var12,
                var13
            );
        }
    }

    private static void quad(
        Sink var0,
        double var1,
        double var3,
        double var5,
        double var7,
        double var9,
        double var11,
        double var13,
        double var15,
        double var17,
        double var19,
        double var21,
        double var23,
        int var25,
        float var26
    ) {
        if (!(var26 <= 0.001F)) {
            var0.vertex(var1, var3, var5, var25, var26);
            var0.vertex(var7, var9, var11, var25, var26);
            var0.vertex(var13, var15, var17, var25, var26);
            var0.vertex(var19, var21, var23, var25, var26);
        }
    }
}
