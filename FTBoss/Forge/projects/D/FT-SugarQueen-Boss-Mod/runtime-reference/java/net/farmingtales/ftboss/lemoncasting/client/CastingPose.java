/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.lemoncasting.client;

import net.farmingtales.ftboss.lemoncasting.CastingClock;
import net.farmingtales.ftboss.lemoncasting.client.LemonCastingCurves;

public final class CastingPose {
    private CastingPose() {
    }

    public static Frame evaluate(CastingClock.Cue cue, double d, int n, double d2, double d3, double d4, double d5) {
        double d6 = CastingClock.age(cue, d);
        int n2 = cue.phase() == 0 ? cue.duration() : CastingClock.recovery(cue.duration());
        String string = cue.spell() + (cue.phase() == 0 ? "_charge" : "_release");
        double[][] dArray = LemonCastingCurves.pose(string, d6, n2, n, d2, d3, d4, d5);
        double d7 = LemonCastingCurves.weight(d6, n2, string);
        double d8 = 1.0;
        if (cue.phase() == 2) {
            var21_13 = cue.changed() - cue.started();
            dArray = LemonCastingCurves.pose(cue.spell() + "_charge", var21_13, cue.duration(), n, d2, d3, d4, d5);
            d7 = LemonCastingCurves.weight(var21_13, cue.duration(), cue.spell() + "_charge");
            d8 = CastingClock.fade(d6, n2);
        } else if (cue.phase() == 1 && cue.changed() - cue.started() < (long)cue.duration()) {
            var21_13 = cue.changed() - cue.started();
            double d9 = CastingClock.fade(d6, Math.max(1, n2 / 2));
            double[][] dArray2 = LemonCastingCurves.pose(cue.spell() + "_charge", var21_13, cue.duration(), n, d2, d3, d4, d5);
            double[][] dArray3 = LemonCastingCurves.pose(string, 0.0, n2, n, d2, d3, d4, d5);
            for (int n3 : new int[]{0, 1, 2, 3}) {
                for (int i = 0; i < 6; ++i) {
                    double[] dArray4 = dArray[n3];
                    int n4 = i;
                    dArray4[n4] = dArray4[n4] + (dArray2[n3][i] - dArray3[n3][i]) * d9;
                }
            }
            d7 += (LemonCastingCurves.weight(var21_13, cue.duration(), cue.spell() + "_charge") - 1.0) * d9;
        }
        double[][] dArray5 = new double[6][6];
        for (int n5 : new int[]{0, 1, 2, 3}) {
            for (int i = 0; i < 3; ++i) {
                dArray5[n5][i] = (dArray[n5][i] - LemonCastingCurves.PIVOTS[n5][i]) * d8;
            }
            double d10 = n5 == 0 ? d5 : (n5 >= 2 ? Math.cos(d2 * 0.6662 + (n5 == 2 ? Math.PI : 0.0)) * d3 * 180.0 / Math.PI : 0.0);
            double d11 = n5 == 0 ? d4 : 0.0;
            dArray5[n5][3] = (dArray[n5][3] - d10 * (1.0 - d7)) * d8;
            dArray5[n5][4] = (dArray[n5][4] - d11 * (1.0 - d7)) * d8;
            dArray5[n5][5] = dArray[n5][5] * d8;
        }
        return new Frame(dArray5, d7 * d8);
    }

    public record Frame(double[][] delta, double influence) {
    }
}

