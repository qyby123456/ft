/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render.dandan;

import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;

public final class DandanMotion {
    private static final double[] ROTATION_TIMES = new double[]{0.0, 0.75, 1.375, 1.9583};
    private static final double[][] ROTATIONS = new double[][]{{0.0, 0.0, 0.0}, {-115.9351, 27.62288, -65.81454}, {134.06, 27.62, -65.81}, {0.0, 0.0, 0.0}};
    private static final int[] ROTATION_EASING = new int[]{0, 0, 1, 2};
    private static final double[] POSITION_TIMES = new double[]{0.0, 0.375, 0.75, 0.8333, 0.875, 0.9167, 0.9583, 1.0417, 1.4167, 1.9583};
    private static final double[][] POSITIONS = new double[][]{{0.0, 0.0, 0.0}, {-5.5, 0.0, 0.5}, {-10.0, 0.0, 6.0}, {-5.5, 0.0, -4.0}, {-3.25, 0.0, -8.0}, {-1.0, 0.0, -10.0}, {0.5, 0.0, -9.0}, {3.5, 0.0, -4.0}, {8.0, 0.0, 6.0}, {0.0, 0.0, 0.0}};
    private static final int[] POSITION_EASING = new int[]{0, 3, 4, 0, 0, 0, 0, 0, 4, 2};

    private DandanMotion() {
    }

    public static double smooth(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, Double.isFinite(d) ? d : 0.0));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double[] point(double[] dArray, int n, double[] dArray2, double d) {
        double[] dArray3 = (double[])dArray.clone();
        d = Math.max(0.0, Double.isFinite(d) ? d : 0.0);
        if (n == 1) {
            return DandanMotion.weaponPoint(dArray, dArray2, DandanMotion.weaponPose(d));
        }
        if (n == 6 || n == 9 || n >= 10) {
            double d2 = 0.5 - 0.5 * Math.cos(d * Math.PI * 2.0 / 41.6666);
            dArray3[1] = dArray3[1] + (n == 9 ? 0.09 : 0.07) * d2;
        } else if (n != 0) {
            double d3 = switch (n) {
                case 1 -> -0.72 + 1.55 * DandanMotion.smooth(d / 24.0);
                case 5 -> d * 0.022;
                case 7 -> d * 0.08;
                default -> throw new IllegalArgumentException("Unsupported Dandan joint");
            };
            int n2 = n == 1 || n == 7 ? 2 : 1;
            double d4 = dArray3[0] - dArray2[0];
            double d5 = dArray3[n2] - dArray2[n2];
            dArray3[0] = dArray2[0] + d4 * Math.cos(d3) - d5 * Math.sin(d3);
            dArray3[n2] = dArray2[n2] + d4 * Math.sin(d3) + d5 * Math.cos(d3);
        }
        return dArray3;
    }

    public static double emergence(double d, boolean bl) {
        return bl ? 0.72 + 0.28 * DandanMotion.smooth(d / 12.0) : 1.0;
    }

    private static double[] sample(double[] dArray, double[][] dArray2, int[] nArray, double d) {
        if (d <= dArray[0]) {
            return (double[])dArray2[0].clone();
        }
        for (int i = 1; i < dArray.length; ++i) {
            if (!(d <= dArray[i])) continue;
            double d2 = (d - dArray[i - 1]) / (dArray[i] - dArray[i - 1]);
            d2 = switch (nArray[i]) {
                case 1 -> 1.0 - (1.0 - d2) * (1.0 - d2);
                case 2 -> 0.5 - 0.5 * Math.cos(Math.PI * d2);
                case 3 -> 1.0 - Math.cos(Math.PI * d2 / 2.0);
                case 4 -> Math.sin(Math.PI * d2 / 2.0);
                default -> d2;
            };
            double[] dArray3 = new double[3];
            for (int j = 0; j < 3; ++j) {
                dArray3[j] = dArray2[i - 1][j] * (1.0 - d2) + dArray2[i][j] * d2;
            }
            return dArray3;
        }
        return (double[])dArray2[dArray2.length - 1].clone();
    }

    public static WeaponPose weaponPose(double d) {
        double[] dArray = DandanMotion.sample(ROTATION_TIMES, ROTATIONS, ROTATION_EASING, d / 20.0);
        double d2 = Math.toRadians(dArray[0] * 0.18);
        double d3 = Math.toRadians(dArray[1] * 0.35) - (-0.72 + 1.55 * DandanMotion.smooth(d / 39.166));
        double d4 = Math.toRadians(dArray[2] * 0.24);
        double d5 = Math.cos(d2);
        double d6 = Math.sin(d2);
        double d7 = Math.cos(d3);
        double d8 = Math.sin(d3);
        double d9 = Math.cos(d4);
        double d10 = Math.sin(d4);
        double[] dArray2 = new double[]{d9 * d7, d9 * d8 * d6 - d10 * d5, d9 * d8 * d5 + d10 * d6, d10 * d7, d10 * d8 * d6 + d9 * d5, d10 * d8 * d5 - d9 * d6, -d8, d7 * d6, d7 * d5};
        double[] dArray3 = DandanMotion.sample(POSITION_TIMES, POSITIONS, POSITION_EASING, d / 20.0);
        int n = 0;
        while (n < 3) {
            int n2 = n++;
            dArray3[n2] = dArray3[n2] * 0.01875;
        }
        return new WeaponPose(dArray2, dArray3);
    }

    public static double[] weaponPoint(double[] dArray, double[] dArray2, WeaponPose weaponPose) {
        double[] dArray3 = new double[3];
        for (int i = 0; i < 3; ++i) {
            dArray3[i] = dArray2[i] + weaponPose.translation()[i];
            for (int j = 0; j < 3; ++j) {
                int n = i;
                dArray3[n] = dArray3[n] + weaponPose.matrix()[i * 3 + j] * (dArray[j] - dArray2[j]);
            }
        }
        return dArray3;
    }

    public static double fade(double d, int n) {
        return 1.0 - DandanMotion.smooth((d - (double)Math.max(1, n - 7)) / 7.0);
    }

    public static double cameraFade(double d) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return 1.0;
        }
        return DandanMotion.smooth((d - 0.25) / 0.55);
    }

    public static double[] bounds(double[][] dArray) {
        double[] dArray2 = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double[] dArray3 : dArray) {
            for (int i = 0; i < 3; ++i) {
                dArray2[i] = Math.min(dArray2[i], dArray3[i]);
                dArray2[i + 3] = Math.max(dArray2[i + 3], dArray3[i]);
            }
        }
        return dArray2;
    }

    public static boolean overlaps(double[] dArray, double[] dArray2) {
        for (int i = 0; i < 3; ++i) {
            if (!(dArray[i + 3] < dArray2[i]) && !(dArray[i] > dArray2[i + 3])) continue;
            return false;
        }
        return true;
    }

    public static double cameraDistance(double[] dArray, double[] dArray2) {
        double d = 0.0;
        for (int i = 0; i < 3; ++i) {
            double d2 = Math.max(0.0, Math.max(dArray[i] - dArray2[i], dArray2[i] - dArray[i + 3]));
            d += d2 * d2;
        }
        return Math.sqrt(d);
    }

    public static boolean blocksSight(double[] dArray, double[] dArray2, double[] dArray3) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return false;
        }
        double d = 0.3;
        double d2 = 1.8;
        for (int i = 0; i < 3; ++i) {
            if (Math.abs(dArray3[i]) < 1.0E-8) {
                if (!(dArray2[i] < dArray[i] - 0.03) && !(dArray2[i] > dArray[i + 3] + 0.03)) continue;
                return false;
            }
            double d3 = (dArray[i] - 0.03 - dArray2[i]) / dArray3[i];
            double d4 = (dArray[i + 3] + 0.03 - dArray2[i]) / dArray3[i];
            d = Math.max(d, Math.min(d3, d4));
            if (!((d2 = Math.min(d2, Math.max(d3, d4))) < d)) continue;
            return false;
        }
        return true;
    }

    public record WeaponPose(double[] matrix, double[] translation) {
    }
}

