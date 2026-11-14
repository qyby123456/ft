package net.farmingtales.ftboss.util;

public final class ManorMeleeTiming {
    public static final int DEFAULT_INTERVAL = 20;
    public static final int IMPACT_TICKS = 14;

    private ManorMeleeTiming() {}

    public static int recoveryTicks(int interval, boolean accelerated) {
        int recovery = accelerated ? IMPACT_TICKS / 2 : IMPACT_TICKS;
        return Math.max(1, Math.min(interval, recovery));
    }

    public static boolean canRestart(int actionTicks, int castTicks, boolean casting, boolean melee) {
        return castTicks <= 0 && !casting && (actionTicks <= 0 || melee);
    }
}
