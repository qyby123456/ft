package net.farmingtales.ftboss.youzi.motion;

public final class YouziBodyClock {
    public static YouziBodyClock.Sample sample(String var0, double var1, double var3, double var5, double var7, double var9) {
        if (Double.isFinite(var1)
            && Double.isFinite(var3)
            && Double.isFinite(var5)
            && Double.isFinite(var7)
            && Double.isFinite(var9)
            && !(var5 <= 0.0)
            && !(var3 < 0.0)
            && (!"CHARGE".equals(var0) || var3 != 0.0)
            && !(var7 <= 0.0)
            && !(var7 >= 1.0)
            && ("CHARGE".equals(var0) || "RELEASE".equals(var0) || "SUMMON".equals(var0))) {
            if (var0.equals("CHARGE")) {
                double var17 = Math.max(0.0, Math.min(1.0, var1 / Math.max(1.0, var3)));
                return new YouziBodyClock.Sample(var17 * var7, smooth(Math.max(0.0, var1) / 2.0), var9 > 8.0);
            } else {
                double var11 = var0.equals("SUMMON") ? var5 * 20.0 : var5 * (1.0 - var7) * 20.0;
                if (Double.isFinite(var11) && !(var11 <= 0.0)) {
                    double var13 = Math.max(0.0, Math.min(1.0, var1 / Math.max(1.0, var11)));
                    double var15 = var0.equals("SUMMON") ? var13 : var7 + (1.0 - var7) * var13;
                    return new YouziBodyClock.Sample(var15, smooth((var11 - var1) / Math.min(5.0, var11 * 0.3)), var1 >= var11 || var1 < -2.0);
                } else {
                    return new YouziBodyClock.Sample(0.0, 0.0, true);
                }
            }
        } else {
            return new YouziBodyClock.Sample(0.0, 0.0, true);
        }
    }

    private static double smooth(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, var0));
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static record Sample(double phase, double weight, boolean expired) {
    }
}
