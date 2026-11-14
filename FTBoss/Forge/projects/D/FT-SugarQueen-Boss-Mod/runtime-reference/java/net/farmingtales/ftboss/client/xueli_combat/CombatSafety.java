/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.xueli_combat;

import java.util.ArrayList;
import java.util.List;

public final class CombatSafety {
    private CombatSafety() {
    }

    public static double distance(double[] dArray, double[] dArray2) {
        double d = 0.0;
        for (int i = 0; i < 3; ++i) {
            d += Math.pow(dArray[i] - dArray2[i], 2.0);
        }
        return Math.sqrt(d);
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    public static double rayTrailLag(double d, double d2, double d3) {
        return Math.max(0.0, d - d2) / 0.94 * Math.max(0.0, d3 - 0.7);
    }

    public static double visibility(double[] dArray, double[] dArray2, double[] dArray3, boolean bl, boolean bl2) {
        if (!bl) {
            return 1.0;
        }
        double d = CombatSafety.distance(dArray, dArray2);
        if (d < 1.0E-9) {
            return 0.0;
        }
        double d2 = CombatSafety.clamp((d - 0.55) / 0.35);
        if (!bl2) {
            double d3 = 0.0;
            for (int i = 0; i < 3; ++i) {
                d3 += (dArray[i] - dArray2[i]) * dArray3[i] / d;
            }
            double d4 = CombatSafety.clamp((d3 - 0.88) / 0.09);
            d4 = d4 * d4 * (3.0 - 2.0 * d4);
            double d5 = 0.12 + 0.88 * CombatSafety.clamp((d - 3.0) / 1.3);
            d2 *= 1.0 - d4 * (1.0 - d5);
        }
        return d2;
    }

    private static double plane(double[] dArray, double[] dArray2, double[] dArray3, double d) {
        double d2 = -d;
        for (int i = 0; i < 3; ++i) {
            d2 += (dArray[i] - dArray2[i]) * dArray3[i];
        }
        return d2;
    }

    public static List<Corner> cut(List<Corner> list, double[] dArray, double[] dArray2, double d) {
        ArrayList<Corner> arrayList = new ArrayList<Corner>();
        for (int i = 0; i < list.size(); ++i) {
            double d2;
            Corner corner = list.get(i);
            Corner corner2 = list.get((i + list.size() - 1) % list.size());
            double d3 = CombatSafety.plane(corner.position(), dArray, dArray2, d);
            if (d3 >= 0.0 != (d2 = CombatSafety.plane(corner2.position(), dArray, dArray2, d)) >= 0.0) {
                int n;
                double d4 = d2 / (d2 - d3);
                double[] dArray3 = new double[3];
                double[] dArray4 = new double[2];
                for (n = 0; n < 3; ++n) {
                    dArray3[n] = corner2.position()[n] + (corner.position()[n] - corner2.position()[n]) * d4;
                }
                for (n = 0; n < 2; ++n) {
                    dArray4[n] = corner2.uv()[n] + (corner.uv()[n] - corner2.uv()[n]) * d4;
                }
                arrayList.add(new Corner(dArray3, dArray4));
            }
            if (!(d3 >= 0.0)) continue;
            arrayList.add(corner);
        }
        return arrayList;
    }

    public static boolean bodyOverlap(List<Corner> list, double[] dArray) {
        for (int i = 0; i < 3; ++i) {
            double d = Double.POSITIVE_INFINITY;
            double d2 = Double.NEGATIVE_INFINITY;
            for (Corner corner : list) {
                d = Math.min(d, corner.position()[i]);
                d2 = Math.max(d2, corner.position()[i]);
            }
            if (!(d2 < dArray[i]) && !(d > dArray[i + 3])) continue;
            return false;
        }
        return true;
    }

    public record Corner(double[] position, double[] uv) {
    }
}

