package net.farmingtales.ftboss.xueli_melee;

public final class MeleeRules {
    public static final double MELEE_REACH_SQUARED = 9.0;
    public static final double LOW_AIRBORNE_HEIGHT = 2.0;

    private MeleeRules() {
    }

    public static boolean preferRanged(boolean var0, boolean var1, int var2, boolean var3, boolean var4, double var5, double var7) {
        return var0 && !var1 && var2 == 10 && !var3 && !var4 && Double.isFinite(var5) && !(var5 < 0.0) && Double.isFinite(var7)
            ? var5 > 9.0 && var7 > 2.0
            : var0;
    }
}
