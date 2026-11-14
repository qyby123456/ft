/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.dandaniron;

import net.farmingtales.ftboss.client.render.dandan.DandanMotion;
import net.farmingtales.ftboss.client.render.reapflat.FlatReapMotion;

public final class IronGeometry {
    private IronGeometry() {
    }

    public static double[] point(double[] dArray, double[] dArray2, int n, boolean bl, boolean bl2, double[] dArray3, DandanMotion.WeaponPose weaponPose, double d, double d2, double d3) {
        double[] dArray4 = bl ? (bl2 ? (double[])dArray.clone() : FlatReapMotion.transform(dArray, dArray2, dArray3)) : (n == 1 ? DandanMotion.weaponPoint(dArray, dArray2, weaponPose) : DandanMotion.point(dArray, n, dArray2, d));
        dArray4[0] = dArray4[0] * (d2 * d3);
        dArray4[1] = dArray4[1] * (d2 * (bl ? 1.0 : d3));
        dArray4[2] = dArray4[2] * (d2 * d3);
        return dArray4;
    }

    public static double[] world(double[] dArray, double[] dArray2, double d, double d2, double d3, double d4) {
        return new double[]{dArray2[0] + dArray[0] * d4 + (dArray[2] + d2) * d3, dArray2[1] + dArray[1] + d, dArray2[2] - dArray[0] * d3 + (dArray[2] + d2) * d4};
    }

    public static void worldInto(double[] dArray, double[] dArray2, double d, double d2, double d3, double d4) {
        double d5 = dArray[0];
        double d6 = dArray[2] + d2;
        dArray[0] = dArray2[0] + d5 * d4 + d6 * d3;
        dArray[1] = dArray[1] + (dArray2[1] + d);
        dArray[2] = dArray2[2] - d5 * d3 + d6 * d4;
    }
}

