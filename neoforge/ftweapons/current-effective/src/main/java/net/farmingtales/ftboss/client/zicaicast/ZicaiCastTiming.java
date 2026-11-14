package net.farmingtales.ftboss.client.zicaicast;

public final class ZicaiCastTiming {
    private static final int[] RELEASE = new int[]{16, 22, 20};

    private ZicaiCastTiming() {
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, Double.isFinite(var0) ? var0 : 0.0));
    }

    private static double ease(double var0) {
        double var2 = clamp(var0);
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static ZicaiCastTiming.Sample sample(int var0, double var1, int var3, int var4, double var5) {
        if (var0 < 0 || var0 >= 3 || !Double.isFinite(var1) || !Double.isFinite(var5) || var3 < 1 || var3 > 200 || var1 < 0.0) {
            return new ZicaiCastTiming.Sample(0.0, 0.0);
        } else if (var4 == 0) {
            return new ZicaiCastTiming.Sample(
                0.42 * clamp(var1 / (double)var3), ease(var1 / (double)Math.min(4, var3)) * (1.0 - ease((var1 - (double)var3 - 20.0) / 6.0))
            );
        } else if (var4 == 2) {
            return new ZicaiCastTiming.Sample(0.42 * clamp(var5 / (double)var3), (1.0 - ease(var1 / 6.0)) * ease(var5 / (double)Math.min(4, var3)));
        } else if (var4 != 1) {
            return new ZicaiCastTiming.Sample(0.0, 0.0);
        } else {
            double var7 = 0.42 * clamp(var5 / (double)var3);
            double var9 = clamp(var1 / (double)RELEASE[var0]);
            return new ZicaiCastTiming.Sample(var7 + (1.0 - var7) * var9, 1.0 - ease((var1 - (double)(RELEASE[var0] - 6)) / 6.0));
        }
    }

    public static record Sample(double progress, double weight) {
    }
}
