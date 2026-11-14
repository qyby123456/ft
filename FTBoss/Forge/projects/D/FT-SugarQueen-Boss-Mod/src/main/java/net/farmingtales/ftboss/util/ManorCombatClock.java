package net.farmingtales.ftboss.util;

public final class ManorCombatClock {
    private ManorCombatClock() {}

    public static int remainingCooldown(int cooldown, long previous, long now, boolean initialized) {
        if (cooldown <= 0) return 0;
        if (!initialized) return cooldown;
        if (now < previous) return 0;
        long elapsed = now - previous;
        return elapsed < 0 || elapsed >= cooldown ? 0 : cooldown - (int) elapsed;
    }

    public static boolean poseExpired(boolean hasStart, long start, int duration, long now) {
        if (!hasStart || duration <= 0 || now < start) return true;
        long elapsed = now - start;
        return elapsed < 0 || elapsed >= duration;
    }
}
