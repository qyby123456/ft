package net.farmingtales.ftboss.lemoncasting.client;

import net.farmingtales.ftboss.lemoncasting.CastingClock;

public final class CastingPose {
    private CastingPose() {
    }

    public static CastingPose.Frame evaluate(CastingClock.Cue var0, double var1, int var3, double var4, double var6, double var8, double var10) {
        double var12 = CastingClock.age(var0, var1);
        int var14 = var0.phase() == 0 ? var0.duration() : CastingClock.recovery(var0.duration());
        String var15 = var0.spell() + (var0.phase() == 0 ? "_charge" : "_release");
        double[][] var16 = LemonCastingCurves.pose(var15, var12, (double)var14, var3, var4, var6, var8, var10);
        double var17 = LemonCastingCurves.weight(var12, (double)var14, var15);
        double var19 = 1.0;
        if (var0.phase() == 2) {
            double var21 = (double)(var0.changed() - var0.started());
            var16 = LemonCastingCurves.pose(var0.spell() + "_charge", var21, (double)var0.duration(), var3, var4, var6, var8, var10);
            var17 = LemonCastingCurves.weight(var21, (double)var0.duration(), var0.spell() + "_charge");
            var19 = CastingClock.fade(var12, var14);
        } else if (var0.phase() == 1 && var0.changed() - var0.started() < (long)var0.duration()) {
            double var32 = (double)(var0.changed() - var0.started());
            double var23 = CastingClock.fade(var12, Math.max(1, var14 / 2));
            double[][] var25 = LemonCastingCurves.pose(var0.spell() + "_charge", var32, (double)var0.duration(), var3, var4, var6, var8, var10);
            double[][] var26 = LemonCastingCurves.pose(var15, 0.0, (double)var14, var3, var4, var6, var8, var10);

            for (int var30 : new int[]{0, 1, 2, 3}) {
                for (int var31 = 0; var31 < 6; var31++) {
                    var16[var30][var31] = var16[var30][var31] + (var25[var30][var31] - var26[var30][var31]) * var23;
                }
            }

            var17 += (LemonCastingCurves.weight(var32, (double)var0.duration(), var0.spell() + "_charge") - 1.0) * var23;
        }

        double[][] var33 = new double[6][6];

        for (int var35 : new int[]{0, 1, 2, 3}) {
            for (int var36 = 0; var36 < 3; var36++) {
                var33[var35][var36] = (var16[var35][var36] - LemonCastingCurves.PIVOTS[var35][var36]) * var19;
            }

            double var37 = var35 == 0 ? var10 : (var35 >= 2 ? Math.cos(var4 * 0.6662 + (var35 == 2 ? Math.PI : 0.0)) * var6 * 180.0 / Math.PI : 0.0);
            double var38 = var35 == 0 ? var8 : 0.0;
            var33[var35][3] = (var16[var35][3] - var37 * (1.0 - var17)) * var19;
            var33[var35][4] = (var16[var35][4] - var38 * (1.0 - var17)) * var19;
            var33[var35][5] = var16[var35][5] * var19;
        }

        return new CastingPose.Frame(var33, var17 * var19);
    }

    public static record Frame(double[][] delta, double influence) {
    }
}
