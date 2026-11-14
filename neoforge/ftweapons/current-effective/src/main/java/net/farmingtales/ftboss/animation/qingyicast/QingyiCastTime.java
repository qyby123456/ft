package net.farmingtales.ftboss.animation.qingyicast;

public final class QingyiCastTime {
    private static final int[] RELEASE = new int[]{18, 22, 24, 12, 14, 14, 20, 12};

    private QingyiCastTime() {
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, Double.isFinite(var0) ? var0 : 0.0));
    }

    private static double smooth(double var0) {
        double var2 = clamp(var0);
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static QingyiCastTime.Sample sample(int var0, double var1, int var3, int var4, double var5, boolean var7, double var8, double var10) {
        if (var0 < 0
            || var0 >= RELEASE.length
            || !Double.isFinite(var1)
            || !Double.isFinite(var5)
            || !Double.isFinite(var8)
            || !Double.isFinite(var10)
            || var3 < 1
            || var3 > 200
            || var1 < 0.0) {
            return new QingyiCastTime.Sample(0.0, 0.0);
        } else if (var4 == 0) {
            return new QingyiCastTime.Sample(0.42 * clamp(var1 / (double)var3), smooth(var1 / (double)Math.min(3, var3)));
        } else if (var4 == 2) {
            return new QingyiCastTime.Sample(clamp(var8), (1.0 - smooth(var1 / 6.0)) * smooth(var5 / (double)Math.min(3, var3)));
        } else if (var4 != 1) {
            return new QingyiCastTime.Sample(0.0, 0.0);
        } else if (var0 == 7 && !var7) {
            return new QingyiCastTime.Sample(0.6 + Math.sin((var1 + var5) * 0.18) * 0.025, smooth((var1 + var5) / 4.0));
        } else if (var0 == 7) {
            return new QingyiCastTime.Sample(
                var8 + (1.0 - var8) * smooth((var1 - var10) / (double)RELEASE[var0]), 1.0 - smooth((var1 - var10 - (double)(RELEASE[var0] - 6)) / 6.0)
            );
        } else {
            double var12 = (double)(RELEASE[var0] - 6);
            double var14 = var1 < 3.0
                ? var8 + (0.55 - var8) * smooth(var1 / 3.0)
                : (var1 < 5.0 ? 0.55 + 0.09 * smooth((var1 - 3.0) / 2.0) : 0.64 + 0.16 * smooth((var1 - 5.0) / (var12 - 5.0)));
            if (var7 && var1 > var12) {
                var14 = 0.8 + 0.2 * smooth((var1 - var12) / 6.0);
            }

            return new QingyiCastTime.Sample(clamp(var14), var7 ? 1.0 - smooth((var1 - var12) / 6.0) : 1.0);
        }
    }

    public static record Sample(double progress, double weight) {
    }
}
