package net.farmingtales.ftboss.util;

public final class MaqueFlightRules {
    public static final int COOLDOWN_TICKS = 160;
    public static final int INITIAL_DELAY = 40;
    public static final int CANCEL_RETRY = 40;
    public static final int HEAL_BAN_TICKS = 100;
    public static final int MAX_ACTIVE = 64;
    public static final double OWNER_RANGE = 32;
    public static final double TARGET_RANGE = 24;

    private MaqueFlightRules() {}

    public static boolean canStart(long now, long ready, int active, boolean busy) {
        return now >= ready && active < MAX_ACTIVE && !busy;
    }

    public static int castState(long age, int duration, boolean valid, boolean enabled) {
        if (!valid || !enabled || duration < 1 || duration > 200 || age < 0 || age > duration + 20L) return -1;
        return age < duration ? 0 : 1;
    }

    public static boolean refreshBan(int remaining, boolean infinite) {
        return !infinite && remaining < HEAL_BAN_TICKS;
    }
}
