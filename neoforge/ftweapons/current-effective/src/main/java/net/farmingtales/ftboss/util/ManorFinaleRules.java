package net.farmingtales.ftboss.util;

public final class ManorFinaleRules {
    public static final double ARENA_RADIUS = 96.0;
    public static final double ASSAULT_RADIUS = 3.0;
    public static final double ASSAULT_HEIGHT = 3.0;
    public static final int WINDUP_TICKS = 20;
    public static final int COOLDOWN_TICKS = 70;

    private ManorFinaleRules() {
    }

    public static boolean insideArena(double var0, double var2, double var4) {
        return Double.isFinite(var0) && Double.isFinite(var2) && Double.isFinite(var4) && var0 * var0 + var2 * var2 + var4 * var4 <= 9216.0;
    }

    public static boolean intersectsField(
        double var0, double var2, double var4, double var6, double var8, double var10, double var12, double var14, double var16
    ) {
        if (Double.isFinite(var0)
            && Double.isFinite(var2)
            && Double.isFinite(var4)
            && Double.isFinite(var6)
            && Double.isFinite(var8)
            && Double.isFinite(var10)
            && Double.isFinite(var12)
            && Double.isFinite(var14)
            && Double.isFinite(var16)
            && !(var6 > var12)
            && !(var8 > var14)
            && !(var10 > var16)
            && !(var14 < var2 - 0.25)
            && !(var8 > var2 + 3.0)) {
            double var18 = Math.max(var6 - var0, Math.max(0.0, var0 - var12));
            double var20 = Math.max(var10 - var4, Math.max(0.0, var4 - var16));
            return var18 * var18 + var20 * var20 <= 9.0;
        } else {
            return false;
        }
    }

    public static int rotationStart(int var0, long var1, int var3) {
        return var3 <= 0 ? -1 : Math.floorMod((long)var0 + var1, var3);
    }

    public static int cooldown(int var0) {
        return 70 + Math.floorMod(var0, 11);
    }
}
