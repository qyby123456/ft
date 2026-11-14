package net.farmingtales.ftboss.dandaniron;

import net.farmingtales.ftboss.client.render.dandan.DandanMotion;
import net.farmingtales.ftboss.client.render.reapflat.FlatReapMotion;

public final class IronGeometry {
    private IronGeometry() {
    }

    public static double[] point(
        double[] var0, double[] var1, int var2, boolean var3, boolean var4, double[] var5, DandanMotion.WeaponPose var6, double var7, double var9, double var11
    ) {
        double[] var13 = var3
            ? (var4 ? (double[])var0.clone() : FlatReapMotion.transform(var0, var1, var5))
            : (var2 == 1 ? DandanMotion.weaponPoint(var0, var1, var6) : DandanMotion.point(var0, var2, var1, var7));
        var13[0] *= var9 * var11;
        var13[1] *= var9 * (var3 ? 1.0 : var11);
        var13[2] *= var9 * var11;
        return var13;
    }

    public static double[] world(double[] var0, double[] var1, double var2, double var4, double var6, double var8) {
        return new double[]{var1[0] + var0[0] * var8 + (var0[2] + var4) * var6, var1[1] + var0[1] + var2, var1[2] - var0[0] * var6 + (var0[2] + var4) * var8};
    }

    public static void worldInto(double[] var0, double[] var1, double var2, double var4, double var6, double var8) {
        double var10 = var0[0];
        double var12 = var0[2] + var4;
        var0[0] = var1[0] + var10 * var8 + var12 * var6;
        var0[1] += var1[1] + var2;
        var0[2] = var1[2] - var10 * var6 + var12 * var8;
    }
}
