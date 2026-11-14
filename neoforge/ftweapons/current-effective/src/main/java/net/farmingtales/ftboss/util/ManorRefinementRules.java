package net.farmingtales.ftboss.util;

public final class ManorRefinementRules {
    public static final int TELEPORT_SEAL_TICKS = 200;
    public static final int PREDICTION_TICKS = 10;
    public static final double MAX_LEAD = 6.0;
    public static final float DORA_DAMAGE_MULTIPLIER = 1.25F;
    public static final double GONGDI_RANGE_MULTIPLIER = 1.5;

    private ManorRefinementRules() {
    }

    public static double leadScale(double var0, double var2) {
        double var4 = Math.hypot(var0, var2);
        return Double.isFinite(var4) && !(var4 < 0.001) && !(var4 > 1.5) ? Math.min(10.0, 6.0 / var4) : 0.0;
    }

    public static int armorSteps(int var0, int var1) {
        return Math.min(Math.max(0, var0) / 5 * 2, Math.max(0, var1));
    }

    public static boolean sealed(long var0, long var2) {
        return var2 > var0 && var2 - var0 <= 200L;
    }

    public static boolean minorSpell(int var0) {
        return var0 >= 1 && var0 <= 6 && var0 != 4;
    }
}
