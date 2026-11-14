package net.farmingtales.ftboss.client.zicaiarcane;

import java.util.List;
import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;

public final class ArcaneRig {
    public static double ease(double var0) {
        return 0.5 - 0.5 * Math.cos(Math.PI * Math.max(0.0, Math.min(1.0, var0)));
    }

    public static double spatialScale(double var0) {
        return Double.isFinite(var0) && var0 > 0.0 ? Math.max(0.5, Math.min(4.0, var0)) : 1.0;
    }

    public static double animationAge(String var0, boolean var1, double var2, int var4) {
        return !var1 && !var0.equals("halo_sanctuary")
            ? Math.max(0.0, var2) * Math.max(1.0, (var0.equals("seraph_descent") ? 44.0 : 36.0) / (double)Math.max(1, var4))
            : Math.max(0.0, var2);
    }

    public static double[] floorAnchor(String var0, String var1, double[] var2, double[] var3, double[] var4) {
        if (var0.equals("CASTER") && var4 != null) {
            return (double[])var4.clone();
        } else {
            return var0.equals("DESCENT") && var1.equals("BEAM") ? new double[]{var3[0], var2[1] - 3.0, var3[2]} : (double[])var2.clone();
        }
    }

    public static double persistence(double var0, int var2) {
        double var3 = Math.min(6.0, Math.max(1.0, (double)var2 / 3.0));
        return 1.0 - ease((var0 - (double)var2 + var3) / var3);
    }

    public static double sample(double[][] var0, double var1) {
        if (var1 <= var0[0][0]) {
            return var0[0][1];
        } else {
            for (int var3 = 1; var3 < var0.length; var3++) {
                if (var1 <= var0[var3][0]) {
                    double[] var4 = var0[var3 - 1];
                    double[] var5 = var0[var3];
                    return var4[1] + (var5[1] - var4[1]) * ease((var1 - var4[0]) / (var5[0] - var4[0]));
                }
            }

            return var0[var0.length - 1][1];
        }
    }

    public static ArcaneRig.Frame frame(String var0, double var1, boolean var3) {
        double var4 = var1 / 20.0;
        double var6 = ease((var1 - 2.0) / 19.0);
        double var8 = 1.0;
        double var10 = 1.0;
        double var12 = 0.0;
        double var14 = 0.0;
        double var16 = 1.0;
        if (var0.equals("compression")) {
            var8 = var3 ? 0.88 + 0.08 * Math.sin(var4 * 5.0) : 1.0 - 0.25 * ease(var1 / 8.0);
            var16 = var3 ? 0.85 : 1.0 - 0.98 * ease((var1 - 4.0) / 10.0);
        } else if (var0.equals("blast")) {
            var8 = var3 ? 0.23 + 0.018 * Math.sin(var4 * 6.0) : 0.2 + 0.8 * (1.0 - Math.pow(1.0 - Math.max(0.0, Math.min(1.0, var1 / 12.0)), 3.0));
            var10 = var3 ? 0.74 : 1.0 - 0.22 * ease(var1 / 18.0);
            var16 = var3 ? 0.25 : (0.28 + 0.72 * ease(var1 / 3.0)) * (1.0 - 0.96 * ease((var1 - 12.0) / 22.0));
        } else if (var0.equals("wake")) {
            var8 = var3 ? 0.25 : 0.18 + 0.82 * (1.0 - Math.pow(1.0 - Math.max(0.0, Math.min(1.0, (var1 - 4.0) / 18.0)), 3.0));
            var10 = 0.88;
            var16 = var3 ? 0.06 : ease((var1 - 3.0) / 5.0) * (1.0 - 0.98 * ease((var1 - 19.0) / 23.0));
        } else if (var0.equals("boundary")) {
            var16 = var3 ? 0.16 : (0.3 + 0.7 * ease((var1 - 9.0) / 7.0)) * (1.0 - 0.97 * ease((var1 - 25.0) / 22.0));
        } else if (var0.equals("cloud")) {
            var8 = var3 ? 0.88 + 0.08 * ease(var1 / 24.0) : 1.0 + 0.025 * Math.sin(var4 * 1.9);
            var10 = 1.0 + 0.035 * Math.sin(var4 * 2.1);
            var16 = var3 ? 0.5 + 0.35 * ease(var1 / 8.0) : 1.0 - 0.94 * ease((var1 - 33.0) / 25.0);
        } else if (var0.equals("judgment")) {
            var10 = var3 ? 0.68 : 0.88 + 0.12 * ease(var1 / 10.0);
            var14 = var3 ? 0.36 : 0.15 * (1.0 - ease(var1 / 12.0));
            var16 = var3 ? 0.06 : (0.14 + 0.86 * ease((var1 - 5.0) / 5.0)) * (1.0 - 0.96 * ease((var1 - 16.0) / 15.0));
        } else if (var0.equals("pressure")) {
            var8 = var3 ? 0.96 + 0.035 * Math.sin(var4 * 4.0) : 1.0 + 0.33 * var6;
            var10 = var3 ? 1.0 : 1.0 - 0.18 * var6;
            var16 = var3 ? 0.74 : 1.0 - 0.78 * ease((var1 - 9.0) / 19.0);
        } else if (var0.equals("recoil") || var0.equals("aftershock")) {
            boolean var23 = var0.equals("aftershock");
            var8 = var3 ? 0.74 : 0.82 + (var23 ? 0.54 : 0.7) * ease((var1 - (double)(var23 ? 6 : 1)) / 23.0);
            var16 = var3 ? 0.2 : (0.24 + 0.76 * ease(var1 / 6.0)) * (1.0 - 0.95 * ease((var1 - (double)(var23 ? 16 : 13)) / 30.0));
        } else if (var0.startsWith("concord")) {
            int var18 = Integer.parseInt(var0.substring(var0.length() - 1));
            var8 = 0.97 + 0.17 * (sample(ArcaneField.REFERENCE.breath(), (var4 + (double)var18 * 0.23) % ArcaneField.REFERENCE.breathLength()) - 1.0);
            var12 = Math.sin(var4 * 0.56 + (double)var18 * 0.8) * 3.5;
            var16 = 0.72 + 0.24 * ease((Math.sin(var4 * 2.1 - (double)var18) + 1.0) / 2.0);
        } else if (var0.equals("weave")) {
            var12 = var4 * 4.6;
            var14 = 0.035 * Math.sin(var4 * 1.9);
            var16 = 0.78 + 0.2 * Math.pow(Math.sin(var4 * 1.6), 2.0);
        } else if (var0.equals("motes")) {
            var14 = 0.24 * Math.sin(var4 * 1.6);
            var16 = 0.4 + 0.5 * Math.pow(Math.sin(var4 * 1.9), 2.0);
        } else if (var0.startsWith("aperture")) {
            int var21 = Integer.parseInt(var0.substring(var0.length() - 1));
            var12 = (double)(var21 % 2 == 1 ? -1 : 1) * var4 * (double)(9 + var21 * 3);
            var8 = var3 ? 0.87 + 0.07 * Math.sin(var4 * 2.0) : 0.89 + 0.19 * var6;
            var16 = var3 ? 0.83 : 1.0 - 0.7 * ease((var1 - 17.0) / 31.0);
        } else if (var0.equals("fall")) {
            double var22 = sample(ArcaneField.REFERENCE.descent(), Math.min(0.5833, var4 * 0.58));
            var14 = var3 ? 0.58 : 0.74 * (1.0 - var22 / 5.0);
            var10 = var3 ? 0.63 : 0.65 + 0.35 * ease(var1 / 17.0);
            var16 = var3 ? 0.28 : (0.3 + 0.7 * ease(var1 / 7.0)) * (1.0 - 0.88 * ease((var1 - 22.0) / 24.0));
        } else {
            if (!var0.equals("impact")) {
                throw new IllegalStateException("Unknown new bone behavior " + var0);
            }

            var8 = var3 ? 0.71 : 0.75 + 0.65 * ease((var1 - 10.0) / 21.0);
            var16 = var3 ? 0.1 : ease((var1 - 8.0) / 9.0) * (1.0 - 0.93 * ease((var1 - 24.0) / 23.0));
        }

        double var24 = Math.toRadians(var12);
        return new ArcaneRig.Frame(var8, var10, var12, var14, var16, Math.cos(var24), Math.sin(var24));
    }

    public static void position(double[] var0, ArcaneField.Bone var1, ArcaneRig.Frame var2, double[] var3) {
        double var4 = (var0[0] - var1.pivot()[0]) * var2.radius();
        double var6 = (var0[2] - var1.pivot()[2]) * var2.radius();
        var3[0] = var1.pivot()[0] + var4 * var2.cosine() + var6 * var2.sine();
        var3[1] = var1.pivot()[1] + (var0[1] - var1.pivot()[1]) * var2.height() + var2.altitude();
        var3[2] = var1.pivot()[2] - var4 * var2.sine() + var6 * var2.cosine();
    }

    public static double visibility(double[][] var0, int[] var1, double[] var2, double[] var3, List<ArcaneRig.Box> var4, boolean var5) {
        double var6 = 0.0;
        double var8 = 0.0;
        double var10 = 0.0;

        for (int var15 : var1) {
            var6 += var0[var15][0] / 3.0;
            var8 += var0[var15][1] / 3.0;
            var10 += var0[var15][2] / 3.0;
        }

        double var28 = 0.0;

        for (int var17 : var1) {
            double var18 = var0[var17][0] - var6;
            double var20 = var0[var17][1] - var8;
            double var22 = var0[var17][2] - var10;
            var28 = Math.max(var28, Math.sqrt(var18 * var18 + var20 * var20 + var22 * var22));
        }

        double var30 = 1.0;

        for (ArcaneRig.Box var35 : var4) {
            double var36 = Math.max(Math.max(var35.minX() - var6, 0.0), var6 - var35.maxX());
            double var38 = Math.max(Math.max(var35.minY() - var8, 0.0), var8 - var35.maxY());
            double var40 = Math.max(Math.max(var35.minZ() - var10, 0.0), var10 - var35.maxZ());
            double var24 = var36 * var36 + var38 * var38 + var40 * var40;
            double var26 = var28 + 0.22;
            if (!(var24 >= var26 * var26)) {
                var30 = Math.min(var30, ease((Math.sqrt(var24) - var28) / 0.22));
                if (var30 == 0.0) {
                    return 0.0;
                }
            }
        }

        double var34 = var6 - var2[0];
        double var37 = var8 - var2[1];
        double var39 = var10 - var2[2];
        double var41 = Math.sqrt(var34 * var34 + var37 * var37 + var39 * var39);
        var30 *= FirstPersonSpellVisibility.cameraFactor(ease((var41 - var28 - 0.22) / 0.7), var5);
        if (false) {
            double var42 = var34 * var3[0] + var37 * var3[1] + var39 * var3[2];
            if (var42 + var28 > 0.0) {
                double var43 = Math.sqrt(Math.max(0.0, var41 * var41 - var42 * var42)) - var28;
                var30 *= ease((var43 - Math.max(0.0, var42) * 0.13) / Math.max(0.12, Math.max(0.0, var42) * 0.12));
            }
        }

        return var30;
    }

    private ArcaneRig() {
    }

    public static double vertexVisibility(double[] var0, double[] var1, double[] var2, List<ArcaneRig.Box> var3, boolean var4) {
        return visibilityAt(var0[0], var0[1], var0[2], 0.0, var1, var2, var3, var4);
    }

    private static double visibilityAt(
        double var0, double var2, double var4, double var6, double[] var8, double[] var9, List<ArcaneRig.Box> var10, boolean var11
    ) {
        double var12 = 1.0;

        for (ArcaneRig.Box var15 : var10) {
            double var16 = Math.max(Math.max(var15.minX() - var0, 0.0), var0 - var15.maxX());
            double var18 = Math.max(Math.max(var15.minY() - var2, 0.0), var2 - var15.maxY());
            double var20 = Math.max(Math.max(var15.minZ() - var4, 0.0), var4 - var15.maxZ());
            double var22 = var16 * var16 + var18 * var18 + var20 * var20;
            double var24 = var6 + 0.22;
            if (!(var22 >= var24 * var24)) {
                var12 = Math.min(var12, ease((Math.sqrt(var22) - var6) / 0.22));
                if (var12 == 0.0) {
                    return 0.0;
                }
            }
        }

        double var26 = var0 - var8[0];
        double var27 = var2 - var8[1];
        double var28 = var4 - var8[2];
        double var29 = Math.sqrt(var26 * var26 + var27 * var27 + var28 * var28);
        return var12 * FirstPersonSpellVisibility.cameraFactor(ease((var29 - var6 - 0.22) / 0.7), var11);
    }

    public static record Box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    }

    public static record Frame(double radius, double height, double yaw, double altitude, double opacity, double cosine, double sine) {
    }
}
