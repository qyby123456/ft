package net.farmingtales.ftboss.client.xueli_combat;

import java.util.ArrayList;
import java.util.List;

public final class CombatSafety {
    private CombatSafety() {
    }

    public static double distance(double[] var0, double[] var1) {
        double var2 = 0.0;

        for (int var4 = 0; var4 < 3; var4++) {
            var2 += Math.pow(var0[var4] - var1[var4], 2.0);
        }

        return Math.sqrt(var2);
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    public static double rayTrailLag(double var0, double var2, double var4) {
        return Math.max(0.0, var0 - var2) / 0.94 * Math.max(0.0, var4 - 0.7);
    }

    public static double visibility(double[] var0, double[] var1, double[] var2, boolean var3, boolean var4) {
        if (!var3) {
            return 1.0;
        } else {
            double var5 = distance(var0, var1);
            if (var5 < 1.0E-9) {
                return 0.0;
            } else {
                double var7 = clamp((var5 - 0.55) / 0.35);
                if (!var4) {
                    double var9 = 0.0;

                    for (int var11 = 0; var11 < 3; var11++) {
                        var9 += (var0[var11] - var1[var11]) * var2[var11] / var5;
                    }

                    double var15 = clamp((var9 - 0.88) / 0.09);
                    var15 = var15 * var15 * (3.0 - 2.0 * var15);
                    double var13 = 0.12 + 0.88 * clamp((var5 - 3.0) / 1.3);
                    var7 *= 1.0 - var15 * (1.0 - var13);
                }

                return var7;
            }
        }
    }

    private static double plane(double[] var0, double[] var1, double[] var2, double var3) {
        double var5 = -var3;

        for (int var7 = 0; var7 < 3; var7++) {
            var5 += (var0[var7] - var1[var7]) * var2[var7];
        }

        return var5;
    }

    public static List<CombatSafety.Corner> cut(List<CombatSafety.Corner> var0, double[] var1, double[] var2, double var3) {
        List<CombatSafety.Corner> var5 = new ArrayList<>();

        for (int var6 = 0; var6 < var0.size(); var6++) {
            CombatSafety.Corner var7 = (CombatSafety.Corner)var0.get(var6);
            CombatSafety.Corner var8 = (CombatSafety.Corner)var0.get((var6 + var0.size() - 1) % var0.size());
            double var9 = plane(var7.position(), var1, var2, var3);
            double var11 = plane(var8.position(), var1, var2, var3);
            if (var9 >= 0.0 != var11 >= 0.0) {
                double var13 = var11 / (var11 - var9);
                double[] var15 = new double[3];
                double[] var16 = new double[2];

                for (int var17 = 0; var17 < 3; var17++) {
                    var15[var17] = var8.position()[var17] + (var7.position()[var17] - var8.position()[var17]) * var13;
                }

                for (int var18 = 0; var18 < 2; var18++) {
                    var16[var18] = var8.uv()[var18] + (var7.uv()[var18] - var8.uv()[var18]) * var13;
                }

                var5.add(new CombatSafety.Corner(var15, var16));
            }

            if (var9 >= 0.0) {
                var5.add(var7);
            }
        }

        return var5;
    }

    public static boolean bodyOverlap(List<CombatSafety.Corner> var0, double[] var1) {
        for (int var2 = 0; var2 < 3; var2++) {
            double var3 = Double.POSITIVE_INFINITY;
            double var5 = Double.NEGATIVE_INFINITY;

            for (CombatSafety.Corner var8 : var0) {
                var3 = Math.min(var3, var8.position()[var2]);
                var5 = Math.max(var5, var8.position()[var2]);
            }

            if (var5 < var1[var2] || var3 > var1[var2 + 3]) {
                return false;
            }
        }

        return true;
    }

    public static record Corner(double[] position, double[] uv) {
    }
}
