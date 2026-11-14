/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.maque;

public final class MaqueRicochetPose {
    private MaqueRicochetPose() {
    }

    public static double[] transform(double[] dArray, double[] dArray2, double[] dArray3, double[] dArray4) {
        double[] dArray5;
        double[] dArray6;
        double d = Math.sqrt(dArray4[0] * dArray4[0] + dArray4[1] * dArray4[1] + dArray4[2] * dArray4[2]);
        if (d > 1.0E-8) {
            double[] dArray7 = new double[3];
            dArray7[0] = dArray4[0] / d;
            dArray7[1] = dArray4[1] / d;
            dArray6 = dArray7;
            dArray7[2] = dArray4[2] / d;
        } else {
            double[] dArray8 = new double[3];
            dArray8[0] = 0.0;
            dArray8[1] = 0.0;
            dArray6 = dArray8;
            dArray8[2] = 1.0;
        }
        double[] dArray9 = dArray6;
        double d2 = Math.hypot(dArray9[0], dArray9[2]);
        if (d2 > 1.0E-8) {
            double[] dArray10 = new double[3];
            dArray10[0] = dArray9[2] / d2;
            dArray10[1] = 0.0;
            dArray5 = dArray10;
            dArray10[2] = -dArray9[0] / d2;
        } else {
            double[] dArray11 = new double[3];
            dArray11[0] = 1.0;
            dArray11[1] = 0.0;
            dArray5 = dArray11;
            dArray11[2] = 0.0;
        }
        double[] dArray12 = dArray5;
        double[] dArray13 = new double[]{dArray9[1] * dArray12[2] - dArray9[2] * dArray12[1], dArray9[2] * dArray12[0] - dArray9[0] * dArray12[2], dArray9[0] * dArray12[1] - dArray9[1] * dArray12[0]};
        double[] dArray14 = new double[3];
        for (int i = 0; i < 3; ++i) {
            dArray14[i] = dArray3[i] + (dArray[0] - dArray2[0]) * dArray12[i] + (dArray[1] - dArray2[1]) * dArray13[i] + (dArray[2] - dArray2[2]) * dArray9[i];
        }
        return dArray14;
    }

    public static double trailAlpha(int n, int n2) {
        return n2 <= 0 ? 0.0 : 0.5 * Math.pow(1.0 - (double)n / (double)n2, 2.0);
    }

    public static double[][] trailQuad(double[] dArray, double[] dArray2, int n, int n2, int n3) {
        double d = 0.07 * Math.max(0.0, 1.0 - (double)n / (double)n2);
        double d2 = 0.07 * Math.max(0.0, 1.0 - (double)(n + 1) / (double)n2);
        double[][] dArrayArray = new double[][]{(double[])dArray.clone(), (double[])dArray.clone(), (double[])dArray2.clone(), (double[])dArray2.clone()};
        double[] dArray3 = dArrayArray[0];
        int n4 = n3;
        dArray3[n4] = dArray3[n4] + d;
        double[] dArray4 = dArrayArray[1];
        int n5 = n3;
        dArray4[n5] = dArray4[n5] - d;
        double[] dArray5 = dArrayArray[2];
        int n6 = n3;
        dArray5[n6] = dArray5[n6] - d2;
        double[] dArray6 = dArrayArray[3];
        int n7 = n3;
        dArray6[n7] = dArray6[n7] + d2;
        return dArrayArray;
    }
}

