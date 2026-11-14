/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.qizaiarcane;

public final class ArcaneSafety {
    private ArcaneSafety() {
    }

    public static float scale(double d, double d2) {
        if (!Double.isFinite(d) || d <= 0.0 || !Double.isFinite(d2) || d2 <= 0.0) {
            return 1.0f;
        }
        return (float)Math.max(0.55, Math.min(8.0, d / d2));
    }

    public static float fade(double[][] dArray, double[] dArray2, double[] dArray3, boolean bl) {
        double d = Double.POSITIVE_INFINITY;
        double d2 = Double.POSITIVE_INFINITY;
        double d3 = Double.POSITIVE_INFINITY;
        double d4 = Double.NEGATIVE_INFINITY;
        double d5 = Double.NEGATIVE_INFINITY;
        double d6 = Double.NEGATIVE_INFINITY;
        for (double[] dArray4 : dArray) {
            d = Math.min(d, dArray4[0]);
            d2 = Math.min(d2, dArray4[1]);
            d3 = Math.min(d3, dArray4[2]);
            d4 = Math.max(d4, dArray4[0]);
            d5 = Math.max(d5, dArray4[1]);
            d6 = Math.max(d6, dArray4[2]);
        }
        if (!bl && dArray3 != null && d <= dArray3[3] && d4 >= dArray3[0] && d2 <= dArray3[4] && d5 >= dArray3[1] && d3 <= dArray3[5] && d6 >= dArray3[2]) {
            return 0.0f;
        }
        double d7 = Math.max(0.0, Math.max(d - dArray2[0], dArray2[0] - d4));
        double d8 = Math.max(0.0, Math.max(d2 - dArray2[1], dArray2[1] - d5));
        double d9 = Math.max(0.0, Math.max(d3 - dArray2[2], dArray2[2] - d6));
        double d10 = d7 * d7 + d8 * d8 + d9 * d9;
        double d11 = Math.sqrt(d10);
        return (float)Math.max(0.0, Math.min(1.0, (d11 - (bl ? 0.08 : 0.28)) / (bl ? 0.2 : 0.65)));
    }
}

