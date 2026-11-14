package net.farmingtales.ftboss.client.render.dandan;

import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;

public final class DandanMotion {
    private static final double[] ROTATION_TIMES = new double[]{0.0, 0.75, 1.375, 1.9583};
    private static final double[][] ROTATIONS = new double[][]{{0.0, 0.0, 0.0}, {-115.9351, 27.62288, -65.81454}, {134.06, 27.62, -65.81}, {0.0, 0.0, 0.0}};
    private static final int[] ROTATION_EASING = new int[]{0, 0, 1, 2};
    private static final double[] POSITION_TIMES = new double[]{0.0, 0.375, 0.75, 0.8333, 0.875, 0.9167, 0.9583, 1.0417, 1.4167, 1.9583};
    private static final double[][] POSITIONS = new double[][]{
        {0.0, 0.0, 0.0},
        {-5.5, 0.0, 0.5},
        {-10.0, 0.0, 6.0},
        {-5.5, 0.0, -4.0},
        {-3.25, 0.0, -8.0},
        {-1.0, 0.0, -10.0},
        {0.5, 0.0, -9.0},
        {3.5, 0.0, -4.0},
        {8.0, 0.0, 6.0},
        {0.0, 0.0, 0.0}
    };
    private static final int[] POSITION_EASING = new int[]{0, 3, 4, 0, 0, 0, 0, 0, 4, 2};

    private DandanMotion() {
    }

    public static double smooth(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, Double.isFinite(var0) ? var0 : 0.0));
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static double[] point(double[] var0, int var1, double[] var2, double var3) {
        double[] var5 = (double[])var0.clone();
        var3 = Math.max(0.0, Double.isFinite(var3) ? var3 : 0.0);
        if (var1 == 1) {
            return weaponPoint(var0, var2, weaponPose(var3));
        } else {
            if (var1 == 6 || var1 == 9 || var1 >= 10) {
                double var14 = 0.5 - 0.5 * Math.cos(var3 * Math.PI * 2.0 / 41.6666);
                var5[1] += (var1 == 9 ? 0.09 : 0.07) * var14;
            } else if (var1 != 0) {
                double var6 = switch (var1) {
                    case 1 -> -0.72 + 1.55 * smooth(var3 / 24.0);
                    case 5 -> var3 * 0.022;
                    case 7 -> var3 * 0.08;
                    default -> throw new IllegalArgumentException("Unsupported Dandan joint");
                };
                int var8 = var1 != 1 && var1 != 7 ? 1 : 2;
                double var9 = var5[0] - var2[0];
                double var11 = var5[var8] - var2[var8];
                var5[0] = var2[0] + var9 * Math.cos(var6) - var11 * Math.sin(var6);
                var5[var8] = var2[var8] + var9 * Math.sin(var6) + var11 * Math.cos(var6);
            }

            return var5;
        }
    }

    public static double emergence(double var0, boolean var2) {
        return var2 ? 0.72 + 0.28 * smooth(var0 / 12.0) : 1.0;
    }

    private static double[] sample(double[] var0, double[][] var1, int[] var2, double var3) {
        if (var3 <= var0[0]) {
            return (double[])var1[0].clone();
        } else {
            for (int var5 = 1; var5 < var0.length; var5++) {
                if (var3 <= var0[var5]) {
                    double var6 = (var3 - var0[var5 - 1]) / (var0[var5] - var0[var5 - 1]);

                    var6 = switch (var2[var5]) {
                        case 1 -> 1.0 - (1.0 - var6) * (1.0 - var6);
                        case 2 -> 0.5 - 0.5 * Math.cos(Math.PI * var6);
                        case 3 -> 1.0 - Math.cos(Math.PI * var6 / 2.0);
                        case 4 -> Math.sin(Math.PI * var6 / 2.0);
                        default -> var6;
                    };
                    double[] var8 = new double[3];

                    for (int var9 = 0; var9 < 3; var9++) {
                        var8[var9] = var1[var5 - 1][var9] * (1.0 - var6) + var1[var5][var9] * var6;
                    }

                    return var8;
                }
            }

            return (double[])var1[var1.length - 1].clone();
        }
    }

    public static DandanMotion.WeaponPose weaponPose(double var0) {
        double[] var2 = sample(ROTATION_TIMES, ROTATIONS, ROTATION_EASING, var0 / 20.0);
        double var3 = Math.toRadians(var2[0] * 0.18);
        double var5 = Math.toRadians(var2[1] * 0.35) - (-0.72 + 1.55 * smooth(var0 / 39.166));
        double var7 = Math.toRadians(var2[2] * 0.24);
        double var9 = Math.cos(var3);
        double var11 = Math.sin(var3);
        double var13 = Math.cos(var5);
        double var15 = Math.sin(var5);
        double var17 = Math.cos(var7);
        double var19 = Math.sin(var7);
        double[] var21 = new double[]{
            var17 * var13,
            var17 * var15 * var11 - var19 * var9,
            var17 * var15 * var9 + var19 * var11,
            var19 * var13,
            var19 * var15 * var11 + var17 * var9,
            var19 * var15 * var9 - var17 * var11,
            -var15,
            var13 * var11,
            var13 * var9
        };
        double[] var22 = sample(POSITION_TIMES, POSITIONS, POSITION_EASING, var0 / 20.0);

        for (int var23 = 0; var23 < 3; var23++) {
            var22[var23] *= 0.01875;
        }

        return new DandanMotion.WeaponPose(var21, var22);
    }

    public static double[] weaponPoint(double[] var0, double[] var1, DandanMotion.WeaponPose var2) {
        double[] var3 = new double[3];

        for (int var4 = 0; var4 < 3; var4++) {
            var3[var4] = var1[var4] + var2.translation()[var4];

            for (int var5 = 0; var5 < 3; var5++) {
                var3[var4] += var2.matrix()[var4 * 3 + var5] * (var0[var5] - var1[var5]);
            }
        }

        return var3;
    }

    public static double fade(double var0, int var2) {
        return 1.0 - smooth((var0 - (double)Math.max(1, var2 - 7)) / 7.0);
    }

    public static double cameraFade(double var0) {
        return FirstPersonSpellVisibility.firstPerson() ? 1.0 : smooth((var0 - 0.25) / 0.55);
    }

    public static double[] bounds(double[][] var0) {
        double[] var1 = new double[]{
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY
        };

        for (double[] var5 : var0) {
            for (int var6 = 0; var6 < 3; var6++) {
                var1[var6] = Math.min(var1[var6], var5[var6]);
                var1[var6 + 3] = Math.max(var1[var6 + 3], var5[var6]);
            }
        }

        return var1;
    }

    public static boolean overlaps(double[] var0, double[] var1) {
        for (int var2 = 0; var2 < 3; var2++) {
            if (var0[var2 + 3] < var1[var2] || var0[var2] > var1[var2 + 3]) {
                return false;
            }
        }

        return true;
    }

    public static double cameraDistance(double[] var0, double[] var1) {
        double var2 = 0.0;

        for (int var4 = 0; var4 < 3; var4++) {
            double var5 = Math.max(0.0, Math.max(var0[var4] - var1[var4], var1[var4] - var0[var4 + 3]));
            var2 += var5 * var5;
        }

        return Math.sqrt(var2);
    }

    public static boolean blocksSight(double[] var0, double[] var1, double[] var2) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return false;
        } else {
            double var3 = 0.3;
            double var5 = 1.8;

            for (int var7 = 0; var7 < 3; var7++) {
                if (Math.abs(var2[var7]) < 1.0E-8) {
                    if (var1[var7] < var0[var7] - 0.03 || var1[var7] > var0[var7 + 3] + 0.03) {
                        return false;
                    }
                } else {
                    double var8 = (var0[var7] - 0.03 - var1[var7]) / var2[var7];
                    double var10 = (var0[var7 + 3] + 0.03 - var1[var7]) / var2[var7];
                    var3 = Math.max(var3, Math.min(var8, var10));
                    var5 = Math.min(var5, Math.max(var8, var10));
                    if (var5 < var3) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    public static record WeaponPose(double[] matrix, double[] translation) {
    }
}
