package net.farmingtales.ftboss.client.dorabasic;

public final class DoraBasicPose {
    private static final double[][] BASES = new double[][]{
        {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {-5.0, 2.5, 0.0}, {5.0, 2.5, 0.0}, {-1.9, 12.0, 0.0}, {1.9, 12.0, 0.0}
    };

    private DoraBasicPose() {
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    private static double smooth(double var0) {
        double var2 = clamp(var0);
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static double runtimePhase(boolean var0, double var1) {
        return (var0 ? 0.28 : 0.34) + clamp(var1 / (double)(var0 ? 3 : 4)) * (var0 ? 0.72 : 0.66);
    }

    public static double weight(boolean var0, double var1) {
        return var1 < 0.0 ? 0.0 : 1.0 - smooth(var1 - (double)(var0 ? 2 : 3));
    }

    public static DoraBasicPose.Pose overlay(double[][] var0, int var1, int var2, double var3, double var5, boolean var7, double var8, double var10) {
        double var12 = weight(true, var3);
        double var14 = weight(false, var5);
        double[][] var16 = var12 > 0.0 ? sampleAngles(var1, runtimePhase(true, var3), var7) : null;
        double[][] var17 = var14 > 0.0 ? sampleAngles(var2, runtimePhase(false, var5), var7) : null;
        double[][] var18 = new double[6][3];
        int var19 = var7 ? 3 : 2;
        int var20 = var7 ? 2 : 3;

        for (int var21 = 0; var21 < 6; var21++) {
            for (int var22 = 0; var22 < 3; var22++) {
                double var23 = var0[var21][var22];
                double var25 = var22 == 0 ? var10 : (var22 == 1 ? Math.max(-45.0, Math.min(45.0, var8)) : 0.0);
                int var27 = var7 && var21 >= 2 ? (var21 % 2 == 0 ? var21 + 1 : var21 - 1) : var21;
                double var28 = DoraBasicData.ANGLES[0][0][var27][var22] * (double)(var7 && var22 > 0 ? -1 : 1);
                if (var21 == var19) {
                    var18[var21][var22] = var23 + (var16 == null ? 0.0 : (var16[var21][var22] + var25 - var23) * var12);
                } else if (var21 == var20) {
                    var18[var21][var22] = var23 + (var17 == null ? 0.0 : (var17[var21][var22] + var25 - var23) * var14);
                } else {
                    var18[var21][var22] = var23
                        + (var16 == null ? 0.0 : (var16[var21][var22] - var28) * var12)
                        + (var17 == null ? 0.0 : (var17[var21][var22] - var28) * var14);
                }
            }
        }

        var18[0] = new double[]{var10, var8, 0.0};
        var18[1][1] = var18[1][1] + Math.max(-45.0, Math.min(45.0, var8)) * 0.4 * var12;
        return new DoraBasicPose.Pose(var18, stance(var18));
    }

    public static DoraBasicPose.Pose sample(int var0, double var1, boolean var3) {
        double[][] var4 = sampleAngles(var0, var1, var3);
        return new DoraBasicPose.Pose(var4, stance(var4));
    }

    private static double[][] sampleAngles(int var0, double var1, boolean var3) {
        double[] var4 = DoraBasicData.PHASES[var0];
        int var5 = 0;

        while (var5 < var4.length - 1 && var4[var5] < clamp(var1)) {
            var5++;
        }

        int var6 = Math.max(0, var5 - 1);
        double var7 = smooth(var4[var5] == var4[var6] ? 0.0 : (clamp(var1) - var4[var6]) / (var4[var5] - var4[var6]));
        double[][] var9 = new double[6][3];

        for (int var10 = 0; var10 < 6; var10++) {
            int var11 = var3 && var10 >= 2 ? (var10 % 2 == 0 ? var10 + 1 : var10 - 1) : var10;

            for (int var12 = 0; var12 < 3; var12++) {
                double var13 = DoraBasicData.ANGLES[var0][var6][var11][var12];
                double var15 = DoraBasicData.ANGLES[var0][var5][var11][var12];
                var9[var10][var12] = (var13 + (var15 - var13) * var7) * (double)(var3 && var12 > 0 ? -1 : 1);
            }
        }

        return var9;
    }

    private static double[] rotate(double[] var0, double[] var1) {
        double var2 = var0[0];
        double var4 = var0[1];
        double var6 = var0[2];
        double var8 = Math.toRadians(var1[0]);
        double var10 = Math.toRadians(var1[1]);
        double var12 = Math.toRadians(var1[2]);
        double var14 = var4 * Math.cos(var8) - var6 * Math.sin(var8);
        var6 = var4 * Math.sin(var8) + var6 * Math.cos(var8);
        double var16 = var2 * Math.cos(var10) + var6 * Math.sin(var10);
        var6 = -var2 * Math.sin(var10) + var6 * Math.cos(var10);
        return new double[]{var16 * Math.cos(var12) - var14 * Math.sin(var12), var16 * Math.sin(var12) + var14 * Math.cos(var12), var6};
    }

    public static double[][] stance(double[][] var0) {
        double[][] var1 = new double[6][3];
        double[] var2 = rotate(new double[]{0.0, 12.0, 0.0}, var0[1]);
        var1[1] = new double[]{-var2[0], 12.0 - var2[1], -var2[2]};
        var1[0] = (double[])var1[1].clone();

        for (int var3 = 2; var3 < 4; var3++) {
            var1[var3] = rotate(BASES[var3], var0[1]);

            for (int var4 = 0; var4 < 3; var4++) {
                var1[var3][var4] = var1[var3][var4] + var1[1][var4];
            }
        }

        var1[4] = (double[])BASES[4].clone();
        var1[5] = (double[])BASES[5].clone();
        return var1;
    }

    public static record Pose(double[][] angles, double[][] positions) {
    }
}
