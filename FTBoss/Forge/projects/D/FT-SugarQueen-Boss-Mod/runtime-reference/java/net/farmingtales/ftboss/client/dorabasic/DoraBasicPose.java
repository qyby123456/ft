/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.dorabasic;

import net.farmingtales.ftboss.client.dorabasic.DoraBasicData;

public final class DoraBasicPose {
    private static final double[][] BASES = new double[][]{{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {-5.0, 2.5, 0.0}, {5.0, 2.5, 0.0}, {-1.9, 12.0, 0.0}, {1.9, 12.0, 0.0}};

    private DoraBasicPose() {
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    private static double smooth(double d) {
        double d2 = DoraBasicPose.clamp(d);
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double runtimePhase(boolean bl, double d) {
        return (bl ? 0.28 : 0.34) + DoraBasicPose.clamp(d / (double)(bl ? 3 : 4)) * (bl ? 0.72 : 0.66);
    }

    public static double weight(boolean bl, double d) {
        return d < 0.0 ? 0.0 : 1.0 - DoraBasicPose.smooth(d - (double)(bl ? 2 : 3));
    }

    public static Pose overlay(double[][] dArray, int n, int n2, double d, double d2, boolean bl, double d3, double d4) {
        double d5 = DoraBasicPose.weight(true, d);
        double d6 = DoraBasicPose.weight(false, d2);
        double[][] dArray2 = d5 > 0.0 ? DoraBasicPose.sampleAngles(n, DoraBasicPose.runtimePhase(true, d), bl) : null;
        double[][] dArray3 = d6 > 0.0 ? DoraBasicPose.sampleAngles(n2, DoraBasicPose.runtimePhase(false, d2), bl) : null;
        double[][] dArray4 = new double[6][3];
        int n3 = bl ? 3 : 2;
        int n4 = bl ? 2 : 3;
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 3; ++j) {
                double d7;
                double d8 = dArray[i][j];
                double d9 = j == 0 ? d4 : (d7 = j == 1 ? Math.max(-45.0, Math.min(45.0, d3)) : 0.0);
                int n5 = bl && i >= 2 ? (i % 2 == 0 ? i + 1 : i - 1) : i;
                double d10 = DoraBasicData.ANGLES[0][0][n5][j] * (double)(bl && j > 0 ? -1 : 1);
                dArray4[i][j] = i == n3 ? d8 + (dArray2 == null ? 0.0 : (dArray2[i][j] + d7 - d8) * d5) : (i == n4 ? d8 + (dArray3 == null ? 0.0 : (dArray3[i][j] + d7 - d8) * d6) : d8 + (dArray2 == null ? 0.0 : (dArray2[i][j] - d10) * d5) + (dArray3 == null ? 0.0 : (dArray3[i][j] - d10) * d6));
            }
        }
        dArray4[0] = new double[]{d4, d3, 0.0};
        double[] dArray5 = dArray4[1];
        dArray5[1] = dArray5[1] + Math.max(-45.0, Math.min(45.0, d3)) * 0.4 * d5;
        return new Pose(dArray4, DoraBasicPose.stance(dArray4));
    }

    public static Pose sample(int n, double d, boolean bl) {
        double[][] dArray = DoraBasicPose.sampleAngles(n, d, bl);
        return new Pose(dArray, DoraBasicPose.stance(dArray));
    }

    private static double[][] sampleAngles(int n, double d, boolean bl) {
        int n2;
        double[] dArray = DoraBasicData.PHASES[n];
        for (n2 = 0; n2 < dArray.length - 1 && dArray[n2] < DoraBasicPose.clamp(d); ++n2) {
        }
        int n3 = Math.max(0, n2 - 1);
        double d2 = DoraBasicPose.smooth(dArray[n2] == dArray[n3] ? 0.0 : (DoraBasicPose.clamp(d) - dArray[n3]) / (dArray[n2] - dArray[n3]));
        double[][] dArray2 = new double[6][3];
        for (int i = 0; i < 6; ++i) {
            int n4 = bl && i >= 2 ? (i % 2 == 0 ? i + 1 : i - 1) : i;
            for (int j = 0; j < 3; ++j) {
                double d3 = DoraBasicData.ANGLES[n][n3][n4][j];
                double d4 = DoraBasicData.ANGLES[n][n2][n4][j];
                dArray2[i][j] = (d3 + (d4 - d3) * d2) * (double)(bl && j > 0 ? -1 : 1);
            }
        }
        return dArray2;
    }

    private static double[] rotate(double[] dArray, double[] dArray2) {
        double d = dArray[0];
        double d2 = dArray[1];
        double d3 = dArray[2];
        double d4 = Math.toRadians(dArray2[0]);
        double d5 = Math.toRadians(dArray2[1]);
        double d6 = Math.toRadians(dArray2[2]);
        double d7 = d2 * Math.cos(d4) - d3 * Math.sin(d4);
        d3 = d2 * Math.sin(d4) + d3 * Math.cos(d4);
        d2 = d7;
        double d8 = d * Math.cos(d5) + d3 * Math.sin(d5);
        d3 = -d * Math.sin(d5) + d3 * Math.cos(d5);
        d = d8;
        return new double[]{d * Math.cos(d6) - d2 * Math.sin(d6), d * Math.sin(d6) + d2 * Math.cos(d6), d3};
    }

    public static double[][] stance(double[][] dArray) {
        double[][] dArray2 = new double[6][3];
        double[] dArray3 = DoraBasicPose.rotate(new double[]{0.0, 12.0, 0.0}, dArray[1]);
        dArray2[1] = new double[]{-dArray3[0], 12.0 - dArray3[1], -dArray3[2]};
        dArray2[0] = (double[])dArray2[1].clone();
        for (int i = 2; i < 4; ++i) {
            dArray2[i] = DoraBasicPose.rotate(BASES[i], dArray[1]);
            for (int j = 0; j < 3; ++j) {
                double[] dArray4 = dArray2[i];
                int n = j;
                dArray4[n] = dArray4[n] + dArray2[1][j];
            }
        }
        dArray2[4] = (double[])BASES[4].clone();
        dArray2[5] = (double[])BASES[5].clone();
        return dArray2;
    }

    public record Pose(double[][] angles, double[][] positions) {
    }
}

