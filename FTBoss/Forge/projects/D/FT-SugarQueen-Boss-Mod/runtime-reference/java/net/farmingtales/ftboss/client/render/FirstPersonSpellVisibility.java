/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package net.farmingtales.ftboss.client.render;

import net.minecraft.client.Minecraft;

public final class FirstPersonSpellVisibility {
    private FirstPersonSpellVisibility() {
    }

    public static boolean firstPerson() {
        return !Minecraft.m_91087_().f_91063_.m_109153_().m_90594_();
    }

    public static boolean preserveThirdPerson(boolean bl) {
        return !FirstPersonSpellVisibility.firstPerson() && bl;
    }

    public static double cameraFactor(double d, boolean bl) {
        return bl ? 1.0 : d;
    }

    public static double cameraDistance(double d, boolean bl) {
        return bl ? Double.POSITIVE_INFINITY : d;
    }

    public static float geometryOpacity(double[][] dArray) {
        double[][] dArray2 = dArray;
        int n = dArray2.length;
        for (int i = 0; i < n; ++i) {
            double[] dArray3;
            for (double d : dArray3 = dArray2[i]) {
                if (Double.isFinite(d)) continue;
                return 0.0f;
            }
        }
        return 1.0f;
    }

    public static double smooth(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double nearOpacity(double d) {
        return 1.0;
    }

    public static double featherOpacity(double[] dArray, double[] dArray2, double[] dArray3, boolean bl) {
        if (bl) {
            return 1.0;
        }
        double d = dArray[0] - dArray2[0];
        double d2 = dArray[1] - dArray2[1];
        double d3 = dArray[2] - dArray2[2];
        double d4 = Math.sqrt(d * d + d2 * d2 + d3 * d3);
        return FirstPersonSpellVisibility.smooth((d4 - 0.42) / 0.5);
    }

    public static double forgeOpacity(double[] dArray, double[] dArray2, double[] dArray3, boolean bl, boolean bl2) {
        if (bl) {
            return 1.0;
        }
        double d = dArray[0] - dArray2[0];
        double d2 = dArray[1] - dArray2[1];
        double d3 = dArray[2] - dArray2[2];
        double d4 = Math.sqrt(d * d + d2 * d2 + d3 * d3);
        return Math.max(0.0, Math.min(1.0, (d4 - 0.55) / 0.35));
    }

    public static boolean intersectsNearViewRay(double[][] dArray, double[] dArray2, double[] dArray3) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return false;
        }
        return FirstPersonSpellVisibility.intersectsTriangle(dArray[0], dArray[1], dArray[2], dArray2, dArray3) || FirstPersonSpellVisibility.intersectsTriangle(dArray[0], dArray[2], dArray[3], dArray2, dArray3);
    }

    private static boolean intersectsTriangle(double[] dArray, double[] dArray2, double[] dArray3, double[] dArray4, double[] dArray5) {
        double d = dArray2[0] - dArray[0];
        double d2 = dArray3[2] - dArray[2];
        double d3 = dArray3[1] - dArray[1];
        double d4 = dArray5[1] * d2 - dArray5[2] * d3;
        double d5 = dArray2[1] - dArray[1];
        double d6 = dArray3[0] - dArray[0];
        double d7 = dArray5[2] * d6 - dArray5[0] * d2;
        double d8 = dArray2[2] - dArray[2];
        double d9 = dArray5[0] * d3 - dArray5[1] * d6;
        double d10 = d * d4 + d5 * d7 + d8 * d9;
        if (Math.abs(d10) < 1.0E-8) {
            return false;
        }
        double d11 = dArray4[0] - dArray[0];
        double d12 = dArray4[1] - dArray[1];
        double d13 = dArray4[2] - dArray[2];
        double d14 = (d11 * d4 + d12 * d7 + d13 * d9) / d10;
        if (d14 < 0.0 || d14 > 1.0) {
            return false;
        }
        double d15 = d12 * d8 - d13 * d5;
        double d16 = d13 * d - d11 * d8;
        double d17 = d11 * d5 - d12 * d;
        double d18 = (dArray5[0] * d15 + dArray5[1] * d16 + dArray5[2] * d17) / d10;
        if (d18 < 0.0 || d14 + d18 > 1.0) {
            return false;
        }
        double d19 = (d6 * d15 + d3 * d16 + d2 * d17) / d10;
        return d19 > 0.0 && d19 <= 2.4;
    }
}

