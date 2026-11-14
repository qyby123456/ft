/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.saya;

public final class SayaMotion {
    private SayaMotion() {
    }

    public static double smooth(double d) {
        d = Math.max(0.0, Math.min(1.0, d));
        return d * d * (3.0 - 2.0 * d);
    }

    public static double[] pose(String string, double d, double d2, boolean bl) {
        double d3 = SayaMotion.smooth((d2 - d) / 0.36);
        double d4 = 1.0 - SayaMotion.smooth((d2 - 0.86) / 0.14);
        double d5 = 0.06 + 0.94 * d3;
        double d6 = 0.0;
        double d7 = 0.0;
        double d8 = 1.0;
        switch (string) {
            case "rise": {
                d7 = -0.42 * (1.0 - d3);
                break;
            }
            case "bloom": {
                d6 = 0.12 * (1.0 - d3);
                break;
            }
            case "pollen": {
                d8 = 0.45 + 0.65 * d3;
                d7 = 0.18 * Math.sin(d2 * Math.PI);
                d5 *= 0.7 + 0.3 * Math.sin(d2 * Math.PI);
                break;
            }
            case "wave": {
                d5 *= 0.8 + 0.2 * Math.sin(d2 * Math.PI);
                d8 = 0.78 + 0.22 * d3;
                break;
            }
            case "suture": {
                d6 = 0.07 * Math.sin(d2 * Math.PI * 2.0);
                d5 *= 0.9 + 0.1 * Math.sin(d2 * Math.PI);
                break;
            }
        }
        if (bl) {
            d5 *= 0.35;
            d8 *= 0.35;
            d7 += 0.18;
        }
        return new double[]{d5 * (0.06 + 0.94 * d4), d6, d7, d8};
    }

    public static double[] vertex(double[] dArray, double[] dArray2, double[] dArray3) {
        double[] dArray4 = new double[3];
        SayaMotion.vertex(dArray, dArray2, dArray3, dArray4);
        return dArray4;
    }

    public static void vertex(double[] dArray, double[] dArray2, double[] dArray3, double[] dArray4) {
        double d = (dArray[0] - dArray2[0]) * dArray3[0];
        double d2 = (dArray[1] - dArray2[1]) * dArray3[0];
        double d3 = (dArray[2] - dArray2[2]) * dArray3[0];
        double d4 = Math.cos(dArray3[1]);
        double d5 = Math.sin(dArray3[1]);
        dArray4[0] = dArray2[0] * dArray3[3] + d * d4 - d3 * d5;
        dArray4[1] = dArray2[1] + d2 + dArray3[2];
        dArray4[2] = dArray2[2] * dArray3[3] + d * d5 + d3 * d4;
    }

    public static float viewAlpha(double[] dArray, double[] dArray2, double[] dArray3, boolean bl) {
        return SayaMotion.viewAlpha(dArray[0] - dArray2[0], dArray[1] - dArray2[1], dArray[2] - dArray2[2], dArray3[0], dArray3[1], dArray3[2], bl);
    }

    public static float viewAlpha(double d, double d2, double d3, double d4, double d5, double d6, boolean bl) {
        return 1.0f;
    }
}

