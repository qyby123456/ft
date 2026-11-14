package net.farmingtales.ftboss.boss.ai;

public final class ManorTargetRules {
    public static final double LOW_AIRBORNE_HEIGHT = 2.0;

    private ManorTargetRules() {
    }

    public static boolean hybrid(int var0) {
        return var0 == 0 || var0 == 1 || var0 == 4 || var0 == 5 || var0 == 7;
    }

    public static boolean preferRanged(boolean var0, boolean var1, int var2, boolean var3, boolean var4, double var5, double var7, double var9) {
        return var0
                && !var1
                && hybrid(var2)
                && !var3
                && !var4
                && Double.isFinite(var5)
                && !(var5 < 0.0)
                && Double.isFinite(var7)
                && Double.isFinite(var9)
                && !(var9 < 0.0)
            ? var5 > var9 && Math.abs(var7) > 2.0
            : var0;
    }
}
