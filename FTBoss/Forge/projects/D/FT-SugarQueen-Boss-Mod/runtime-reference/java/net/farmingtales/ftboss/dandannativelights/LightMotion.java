/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.dandannativelights;

public final class LightMotion {
    private LightMotion() {
    }

    public static double partRadius(double d, double d2) {
        return d > 0.0 ? d : d2;
    }

    public static double[] sample(double[][] dArray, double d) {
        int n;
        double d2 = Math.max(0.0, Math.min(1.0, d));
        for (n = 1; n < dArray.length - 1 && d2 > dArray[n][0]; ++n) {
        }
        double[] dArray2 = dArray[n - 1];
        double[] dArray3 = dArray[n];
        double[] dArray4 = new double[8];
        double d3 = Math.max(0.0, Math.min(1.0, (d2 - dArray2[0]) / (dArray3[0] - dArray2[0])));
        for (int i = 0; i < dArray4.length; ++i) {
            dArray4[i] = dArray2[i + 1] * (1.0 - d3) + dArray3[i + 1] * d3;
        }
        return dArray4;
    }

    public static double[] transform(double[] dArray, double[] dArray2, double d) {
        double d2 = dArray[0] * dArray2[3];
        double d3 = dArray[2] * dArray2[5];
        return new double[]{(d2 * Math.cos(dArray2[6]) + d3 * Math.sin(dArray2[6]) + dArray2[0]) * d, dArray[1] * dArray2[4] + dArray2[1], (-d2 * Math.sin(dArray2[6]) + d3 * Math.cos(dArray2[6]) + dArray2[2]) * d};
    }

    public static double[] worldTransform(double[] dArray, double d, double d2, double[] dArray2) {
        double d3 = Math.cos(dArray[6]);
        double d4 = Math.sin(dArray[6]);
        double d5 = Math.cos(d2);
        double d6 = Math.sin(d2);
        double d7 = d3 * d5 - d4 * d6;
        double d8 = d4 * d5 + d3 * d6;
        return new double[]{d * dArray[3] * d7, d * dArray[5] * d8, dArray2[0] + d * (dArray[0] * d5 + dArray[2] * d6), dArray[4], dArray2[1] + dArray[1], -d * dArray[3] * d8, d * dArray[5] * d7, dArray2[2] + d * (-dArray[0] * d6 + dArray[2] * d5)};
    }

    public static void transformInto(double[] dArray, double[] dArray2, double[] dArray3) {
        dArray3[0] = dArray[0] * dArray2[0] + dArray[2] * dArray2[1] + dArray2[2];
        dArray3[1] = dArray[1] * dArray2[3] + dArray2[4];
        dArray3[2] = dArray[0] * dArray2[5] + dArray[2] * dArray2[6] + dArray2[7];
    }

    public static double sightFade(double[] dArray, double[] dArray2, double[] dArray3) {
        double d = 0.0;
        for (int i = 0; i < 3; ++i) {
            double d2 = Math.max(0.0, Math.max(dArray[i] - dArray2[i], dArray2[i] - dArray[i + 3]));
            d += d2 * d2;
        }
        if (d < 0.16) {
            return 0.08;
        }
        double d3 = 0.3;
        double d4 = 3.0;
        for (int i = 0; i < 3; ++i) {
            double d5;
            double d6;
            if (!(Math.abs(dArray3[i]) < 1.0E-8 ? dArray2[i] < dArray[i] - 0.06 || dArray2[i] > dArray[i + 3] + 0.06 : (d3 = Math.max(d3, Math.min(d6 = (dArray[i] - 0.06 - dArray2[i]) / dArray3[i], d5 = (dArray[i + 3] + 0.06 - dArray2[i]) / dArray3[i]))) > (d4 = Math.min(d4, Math.max(d6, d5))))) continue;
            return 1.0;
        }
        return 0.12;
    }
}

