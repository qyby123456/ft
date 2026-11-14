/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.lemoncat;

import java.util.List;
import net.farmingtales.ftboss.client.lemoncat.LemoncatArtMotion;
import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;

public final class LemoncatArtGuard {
    private LemoncatArtGuard() {
    }

    public static float alpha(double[][] dArray, double[] dArray2, double[] dArray3, boolean bl, List<double[][]> list) {
        double d = 0.0;
        double[] dArray4 = new double[3];
        double[] dArray5 = new double[3];
        for (int i = 0; i < 3; ++i) {
            dArray4[i] = Double.POSITIVE_INFINITY;
            dArray5[i] = Double.NEGATIVE_INFINITY;
            for (double[] dArray6 : dArray) {
                dArray4[i] = Math.min(dArray4[i], dArray6[i]);
                dArray5[i] = Math.max(dArray5[i], dArray6[i]);
            }
            double d2 = Math.max(0.0, Math.max(dArray4[i] - dArray2[i], dArray2[i] - dArray5[i]));
            d += d2 * d2;
        }
        double d3 = FirstPersonSpellVisibility.cameraFactor(LemoncatArtMotion.clamp((Math.sqrt(d) - 0.24) / 0.66), bl);
        if (false) {
            double[] dArray7 = new double[3];
            double[] dArray8 = new double[3];
            double d4 = 0.0;
            double d5 = 0.0;
            double d6 = 0.0;
            for (double[] dArray9 : dArray) {
                for (int i = 0; i < 3; ++i) {
                    int n = i;
                    dArray7[n] = dArray7[n] + dArray9[i] / 4.0;
                }
            }
            for (int i = 0; i < 3; ++i) {
                dArray8[i] = dArray7[i] - dArray2[i];
                d4 += dArray8[i] * dArray3[i];
                d5 += dArray8[i] * dArray8[i];
            }
            if (d4 > 0.05) {
                for (double[] dArray9 : dArray) {
                    double d7 = 0.0;
                    for (int i = 0; i < 3; ++i) {
                        d7 += (dArray9[i] - dArray7[i]) * (dArray9[i] - dArray7[i]);
                    }
                    d6 = Math.max(d6, Math.sqrt(d7));
                }
                double d8 = Math.sqrt(Math.max(0.0, d5 - d4 * d4));
                d3 *= LemoncatArtMotion.smooth(((d8 - d6) / d4 - 0.15) / 0.2);
            }
        }
        for (double[][] dArray10 : list) {
            double d9 = 0.0;
            for (int i = 0; i < 3; ++i) {
                double d10 = Math.max(0.0, Math.max(dArray10[0][i] - 0.075 - dArray5[i], dArray4[i] - dArray10[1][i] - 0.075));
                d9 += d10 * d10;
            }
            d3 *= LemoncatArtMotion.clamp(Math.sqrt(d9) / 0.2);
        }
        return (float)d3;
    }
}

