package net.farmingtales.ftboss.util;

public final class ZicaiCombatRules {
    private ZicaiCombatRules() {
    }

    public static int interval(int var0, int var1) {
        return var1 == 8 ? Math.max(1, (int)Math.round((double)var0 / 1.5)) : var0;
    }

    public static double reachSquared(double var0, int var2) {
        if (var2 != 8) {
            return var0;
        } else {
            double var3 = Math.sqrt(var0) + 1.0;
            return var3 * var3;
        }
    }

    public static String stripFlavor(int var0) {
        return var0 == 8 ? "河豚侵蚀" : "末影扰动";
    }
}
