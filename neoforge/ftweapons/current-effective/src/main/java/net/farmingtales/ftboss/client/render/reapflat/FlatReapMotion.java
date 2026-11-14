package net.farmingtales.ftboss.client.render.reapflat;

public final class FlatReapMotion {
    private static final double[] TIMES = new double[]{0.0, 15.0, 27.5, 39.166};
    private static final double[] SWEEP = new double[]{36.0, 64.0, -62.0, -18.0};
    private static final double[] CANT = new double[]{2.0, 7.0, -4.0, 1.0};

    private FlatReapMotion() {
    }

    public static double[] rotation(double var0, int var2) {
        if (var2 == 31) {
            return new double[]{1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0};
        } else {
            double var3 = Math.max(0.0, Math.min(TIMES[3], Double.isFinite(var0) ? var0 : 0.0));
            int var5 = 1;

            while (var5 < 3 && var3 > TIMES[var5]) {
                var5++;
            }

            double var6 = (var3 - TIMES[var5 - 1]) / (TIMES[var5] - TIMES[var5 - 1]);
            if (var5 == 2) {
                var6 = 1.0 - Math.pow(1.0 - var6, 2.0);
            }

            if (var5 == 3) {
                var6 = (1.0 - Math.cos(Math.PI * var6)) / 2.0;
            }

            double var8 = Math.toRadians(SWEEP[var5 - 1] + (SWEEP[var5] - SWEEP[var5 - 1]) * var6);
            double var10 = Math.toRadians(CANT[var5 - 1] + (CANT[var5] - CANT[var5 - 1]) * var6);
            double var12 = Math.cos(var8);
            double var14 = Math.sin(var8);
            double var16 = Math.cos(var10);
            double var18 = Math.sin(var10);
            return new double[]{var12, var14 * var18, var14 * var16, 0.0, var16, -var18, -var14, var12 * var18, var12 * var16};
        }
    }

    public static double[] transform(double[] var0, double[] var1, double[] var2) {
        double[] var3 = (double[])var1.clone();

        for (int var4 = 0; var4 < 3; var4++) {
            for (int var5 = 0; var5 < 3; var5++) {
                var3[var4] += var2[var4 * 3 + var5] * (var0[var5] - var1[var5]);
            }
        }

        return var3;
    }

    public static double smooth(double var0) {
        var0 = Math.max(0.0, Math.min(1.0, var0));
        return var0 * var0 * (3.0 - 2.0 * var0);
    }

    public static double placementLift(double var0, double var2, double var4) {
        return Math.max(0.18 - var0, var4 - (var0 + var2) / 2.0);
    }

    public static double phaseClock(double var0, double var2, boolean var4, double var5) {
        double var7 = Math.min(1.0, Math.max(0.0, var0) * var5 / Math.max(1.0, var2));
        return var4 ? var7 * 15.0 : 15.0 + var7 * 24.166;
    }

    public static double trailAlpha(double var0, double var2) {
        double var4 = var0 - var2 - 0.65;
        return !(var4 <= 0.0) && !(var4 >= 8.0) ? Math.min(1.0, var4 / 0.65) * Math.pow(1.0 - var4 / 8.0, 2.0) : 0.0;
    }

    public static double[] anchoredTrailPoint(double[] var0, double[] var1) {
        double var2 = var0[2] + var1[5] - 0.3;
        return new double[]{var1[0] + var0[0] * var1[4] + var2 * var1[3], var1[1] + var0[1], var1[2] - var0[0] * var1[3] + var2 * var1[4]};
    }

    public static double visibleAlpha(double[] var0, double[] var1, double[] var2, boolean var3) {
        if (var3) {
            return 1.0;
        } else {
            double var4 = 0.0;

            for (int var6 = 0; var6 < 3; var6++) {
                double var7 = Math.max(0.0, Math.max(var0[var6] - var1[var6], var1[var6] - var0[var6 + 3]));
                var4 += var7 * var7;
            }

            double var17 = smooth((Math.sqrt(var4) - 0.22) / 0.4);
            if (!var3) {
                return var17;
            } else {
                double var8 = 0.3;
                double var10 = 3.0;

                for (int var12 = 0; var12 < 3; var12++) {
                    if (Math.abs(var2[var12]) < 1.0E-8) {
                        if (var1[var12] < var0[var12] - 0.06 || var1[var12] > var0[var12 + 3] + 0.06) {
                            return var17;
                        }
                    } else {
                        double var13 = (var0[var12] - 0.06 - var1[var12]) / var2[var12];
                        double var15 = (var0[var12 + 3] + 0.06 - var1[var12]) / var2[var12];
                        var8 = Math.max(var8, Math.min(var13, var15));
                        var10 = Math.min(var10, Math.max(var13, var15));
                        if (var8 > var10) {
                            return var17;
                        }
                    }
                }

                return var17 * 0.16;
            }
        }
    }
}
