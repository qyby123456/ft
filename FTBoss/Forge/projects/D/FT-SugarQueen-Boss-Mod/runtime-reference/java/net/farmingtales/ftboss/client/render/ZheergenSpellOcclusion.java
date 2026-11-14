/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;

public final class ZheergenSpellOcclusion {
    private ZheergenSpellOcclusion() {
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    private static double edgeDistance(double[] dArray, double[] dArray2, double[] dArray3) {
        double d = dArray3[0] - dArray2[0];
        double d2 = dArray3[1] - dArray2[1];
        double d3 = dArray3[2] - dArray2[2];
        double d4 = d * d + d2 * d2 + d3 * d3;
        double d5 = d4 < 1.0E-16 ? 0.0 : ZheergenSpellOcclusion.clamp(((dArray[0] - dArray2[0]) * d + (dArray[1] - dArray2[1]) * d2 + (dArray[2] - dArray2[2]) * d3) / d4);
        return Math.pow(dArray[0] - dArray2[0] - d5 * d, 2.0) + Math.pow(dArray[1] - dArray2[1] - d5 * d2, 2.0) + Math.pow(dArray[2] - dArray2[2] - d5 * d3, 2.0);
    }

    private static double triangleDistance(double[] dArray, double[] dArray2, double[] dArray3, double[] dArray4) {
        double d = dArray3[0] - dArray2[0];
        double d2 = dArray3[1] - dArray2[1];
        double d3 = dArray3[2] - dArray2[2];
        double d4 = dArray4[0] - dArray2[0];
        double d5 = dArray4[1] - dArray2[1];
        double d6 = dArray4[2] - dArray2[2];
        double d7 = d * d + d2 * d2 + d3 * d3;
        double d8 = d * d4 + d2 * d5 + d3 * d6;
        double d9 = d4 * d4 + d5 * d5 + d6 * d6;
        double d10 = dArray[0] - dArray2[0];
        double d11 = dArray[1] - dArray2[1];
        double d12 = dArray[2] - dArray2[2];
        double d13 = d10 * d + d11 * d2 + d12 * d3;
        double d14 = d10 * d4 + d11 * d5 + d12 * d6;
        double d15 = d7 * d9 - d8 * d8;
        if (d15 > 1.0E-18) {
            double d16 = (d9 * d13 - d8 * d14) / d15;
            double d17 = (d7 * d14 - d8 * d13) / d15;
            if (d16 >= 0.0 && d17 >= 0.0 && d16 + d17 <= 1.0) {
                return Math.pow(d10 - d16 * d - d17 * d4, 2.0) + Math.pow(d11 - d16 * d2 - d17 * d5, 2.0) + Math.pow(d12 - d16 * d3 - d17 * d6, 2.0);
            }
        }
        return Math.min(ZheergenSpellOcclusion.edgeDistance(dArray, dArray2, dArray3), Math.min(ZheergenSpellOcclusion.edgeDistance(dArray, dArray3, dArray4), ZheergenSpellOcclusion.edgeDistance(dArray, dArray4, dArray2)));
    }

    public static float camera(double[][] dArray, double[] dArray2) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return 1.0f;
        }
        double d = Math.sqrt(Math.min(ZheergenSpellOcclusion.triangleDistance(dArray2, dArray[0], dArray[1], dArray[2]), ZheergenSpellOcclusion.triangleDistance(dArray2, dArray[0], dArray[2], dArray[3])));
        return (float)ZheergenSpellOcclusion.clamp((d - 0.2) / 0.45);
    }

    public static float actor(double[][] dArray, double[] dArray2) {
        double d = 0.0;
        for (int i = 0; i < 3; ++i) {
            double d2 = Double.POSITIVE_INFINITY;
            double d3 = Double.NEGATIVE_INFINITY;
            for (double[] dArray3 : dArray) {
                d2 = Math.min(d2, dArray3[i]);
                d3 = Math.max(d3, dArray3[i]);
            }
            double d4 = Math.max(0.0, Math.max(dArray2[i] - d3, d2 - dArray2[i + 3]));
            d += d4 * d4;
        }
        return (float)(0.08 + 0.92 * ZheergenSpellOcclusion.clamp(Math.sqrt(d) / 0.3));
    }

    public static float sight(double[][] dArray, double[] dArray2, double[] dArray3) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return 1.0f;
        }
        double d = 1.0;
        for (int i = 0; i < 5; ++i) {
            double d2 = 0.0;
            double d3 = 0.0;
            double d4 = 0.0;
            if (i < 4) {
                d2 = dArray[i][0] - dArray2[0];
                d3 = dArray[i][1] - dArray2[1];
                d4 = dArray[i][2] - dArray2[2];
            } else {
                for (double[] dArray4 : dArray) {
                    d2 += (dArray4[0] - dArray2[0]) * 0.25;
                    d3 += (dArray4[1] - dArray2[1]) * 0.25;
                    d4 += (dArray4[2] - dArray2[2]) * 0.25;
                }
            }
            double d5 = d2 * dArray3[0] + d3 * dArray3[1] + d4 * dArray3[2];
            if (d5 <= 0.2 || d5 > 3.0) continue;
            double d6 = Math.sqrt(Math.pow(d2 - d5 * dArray3[0], 2.0) + Math.pow(d3 - d5 * dArray3[1], 2.0) + Math.pow(d4 - d5 * dArray3[2], 2.0));
            double d7 = 0.06 + d5 * 0.035;
            d = Math.min(d, 0.22 + 0.78 * ZheergenSpellOcclusion.clamp((d6 - d7 * 0.5) / (d7 * 0.8)));
        }
        return (float)d;
    }
}

