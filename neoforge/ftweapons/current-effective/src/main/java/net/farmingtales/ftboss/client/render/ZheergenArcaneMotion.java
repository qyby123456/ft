package net.farmingtales.ftboss.client.render;

public final class ZheergenArcaneMotion {
    private ZheergenArcaneMotion() {
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    private static double ease(double var0) {
        return (1.0 - Math.cos(clamp(var0) * Math.PI)) * 0.5;
    }

    public static ZheergenArcaneMotion.Frame frame(int var0, double var1, boolean var3) {
        double var4 = 0.0;
        double var6 = 0.0;
        double var8 = 0.0;
        double var10 = 1.0;
        double var12 = 1.0;
        double var14 = 1.0;
        double var16 = 0.0;
        double var18 = 1.0;
        if (var0 >= 23 && var0 <= 25) {
            int var32 = var0 - 23;
            double var35 = ease((var1 - (double)var32 * 0.55) / (double)(var3 ? 10 : 5));
            var10 = var14 = var3 ? 0.9 + 0.1 * var35 : 1.0 - 0.025 * Math.sin(var1 * 0.25 + (double)var32);
            var12 = var3 ? 0.12 + 0.45 * var35 : 0.78 + 0.22 * ease(var1 / 5.0);
            var18 = var3 ? 0.18 + 0.32 * var35 : (0.84 + 0.1 * Math.sin(var1 * 0.21 + (double)var32)) * (1.0 - ease((var1 - 48.0) / 12.0));
        } else if (var0 >= 33 && var0 <= 37) {
            int var31 = var0 - 33;
            double var34 = Math.max(0.0, var1 - (double)var31 * 0.66);
            double var37 = ease(var34 / 1.65);
            double var38 = ease((var34 - 4.0) / 8.0);
            double var27 = var31 % 2 == 0 ? 1.0 : -1.0;
            var10 = var3 ? 0.72 + 0.14 * ease(var1 / 10.0) : 0.26 + 0.74 * var37;
            var12 = var3 ? 0.78 + 0.09 * ease(var1 / 10.0) : 0.68 + 0.32 * var37;
            var4 = var3 ? 0.0 : var27 * 0.44 * (1.0 - var37) + 0.08 * (double)(var31 - 2) * var38;
            var6 = var3 ? 0.0 : (double)(2 - var31) * 0.12 * (1.0 - var37);
            var8 = var3 ? 0.0 : var27 * 0.1 * Math.sin(var38 * Math.PI);
            var16 = var3 ? (double)(var31 - 2) * 0.012 * ease(var1 / 10.0) : var27 * (0.07 * (1.0 - var37) - 0.025 * var38);
            var18 = var3 ? 0.1 + 0.22 * ease(var1 / 10.0) : var37 * (1.0 - ease((var34 - 10.0) / 10.0));
        } else if (var0 >= 38 && var0 <= 40) {
            int var30 = var0 - 38;
            double var33 = Math.max(0.0, var1 - (double)var30 * 0.65);
            double var36 = ease(var33 / 8.0);
            double var25 = ease((var33 - 14.0) / 10.0);
            var10 = var3 ? 0.56 + 0.12 * ease(var1 / 10.0) : 0.82 + 0.23 * Math.sin(var36 * Math.PI) - 0.1 * var25;
            var12 = var3 ? 0.4 + 0.25 * ease(var1 / 10.0) : 1.32 - 0.64 * var36 - 0.15 * var25;
            var14 = var3 ? 0.47 + 0.16 * ease(var1 / 10.0) : 0.64 + 0.63 * var36 + 0.11 * var25;
            var4 = var3 ? 0.0 : (double)(var30 - 1) * 0.13 * Math.sin(var36 * Math.PI);
            var6 = var3 ? -0.32 * (1.0 - ease(var1 / 10.0)) : 0.09 * Math.sin(var36 * Math.PI) - 0.24 * var25;
            var8 = var3 ? -2.2 : -2.6 + 4.9 * ease(var33 / 19.0);
            var16 = var3 ? 0.0 : (double)(var30 - 1) * 0.07 * Math.sin(var33 * 0.31);
            var18 = var3 ? 0.35 + 0.35 * ease(var1 / 10.0) : ease((var33 + 0.8) / 2.0) * (1.0 - ease((var33 - 20.0) / 6.0));
        } else if (var0 >= 41 && var0 <= 43) {
            int var29 = var0 - 41;
            double var21 = Math.max(0.0, var1 - 3.8 - (double)var29 * 0.65);
            double var23 = ease(var21 / 16.0);
            var10 = var3 ? 0.65 : 1.0 + 0.16 * var23;
            var12 = var3 ? 0.42 : 0.52 - 0.27 * var23;
            var14 = var3 ? 0.45 : 0.62 + 0.52 * var23;
            var6 = -0.06;
            var8 = var3 ? -2.2 : -1.3 + 1.9 * var23;
            var18 = var3 ? 0.0 : ease(var21 / 3.0) * (1.0 - ease((var1 - 19.0) / 7.0));
        } else if (var0 == 44) {
            double var20 = ease((var1 - 7.0) / 9.0);
            double var22 = ease((var1 - 17.0) / 9.0);
            var10 = var14 = var3 ? 0.5 : 0.55 + 0.75 * var20;
            var12 = var3 ? 0.5 : 0.85 + 0.45 * Math.sin(var20 * Math.PI) - 0.45 * var22;
            var6 = var3 ? -0.5 : 0.52 * Math.sin(var20 * Math.PI) - 0.4 * var22;
            var8 = var3 ? -1.5 : -0.6 + 2.6 * var20;
            var18 = var3 ? 0.0 : ease((var1 - 6.0) / 4.0) * (1.0 - ease((var1 - 19.0) / 7.0));
        }

        return new ZheergenArcaneMotion.Frame(var4, var6, var8, var10, var12, var14, var16, clamp(var18));
    }

    public static void position(double[] var0, double[] var1, ZheergenArcaneMotion.Frame var2, double[] var3) {
        double var4 = (var0[0] - var1[0]) * var2.scale();
        double var6 = (var0[1] - var1[1]) * var2.verticalScale();
        double var8 = Math.cos(var2.angle());
        double var10 = Math.sin(var2.angle());
        var3[0] = var1[0] + var4 * var8 - var6 * var10 + var2.horizontal();
        var3[1] = var1[1] + var4 * var10 + var6 * var8 + var2.vertical();
        var3[2] = var1[2] + (var0[2] - var1[2]) * var2.depthScale() + var2.depth();
    }

    public static record Frame(
        double horizontal, double vertical, double depth, double scale, double verticalScale, double depthScale, double angle, double alpha
    ) {
    }
}
