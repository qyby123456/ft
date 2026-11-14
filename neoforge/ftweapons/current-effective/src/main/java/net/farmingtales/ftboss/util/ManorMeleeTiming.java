package net.farmingtales.ftboss.util;

public final class ManorMeleeTiming {
    public static final int DEFAULT_INTERVAL = 20;
    public static final int IMPACT_TICKS = 14;

    private ManorMeleeTiming() {
    }

    public static int recoveryTicks(int var0, boolean var1) {
        int var2 = var1 ? 7 : 14;
        return Math.max(1, Math.min(var0, var2));
    }

    public static boolean canRestart(int var0, int var1, boolean var2, boolean var3) {
        return var1 <= 0 && !var2 && (var0 <= 0 || var3);
    }
}
