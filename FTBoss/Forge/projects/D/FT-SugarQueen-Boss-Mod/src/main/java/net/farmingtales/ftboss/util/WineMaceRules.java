package net.farmingtales.ftboss.util;

public final class WineMaceRules {
    public static final int MAX_PRESSURE = 3;
    public static final int PRESSURE_TICKS = 300;
    public static final int HIT_INTERVAL = 20;
    public static final int WINDUP_TICKS = 12;
    public static final double RANGE = 7.0;
    public static final double BASE_RADIUS = 3.0;
    public static final int MAX_TARGETS = 24;
    public static final String PRESSURE_TAG = "FTWineMacePressure";

    private WineMaceRules() {}

    public static int pressure(int stored, long expires, long now) {
        if (expires <= now || expires - now < 0 || expires - now > PRESSURE_TICKS) return 0;
        return Math.max(0, Math.min(MAX_PRESSURE, stored));
    }

    public static boolean hitReady(long readyAt, long now) {
        return readyAt <= now || readyAt - now < 0 || readyAt - now > HIT_INTERVAL;
    }

    public static double radius(int pressure) {
        return BASE_RADIUS + Math.max(0, Math.min(MAX_PRESSURE, pressure)) * 0.5;
    }

    public static float damage(double attack, int pressure) {
        if (!Double.isFinite(attack) || attack < 0) return 0;
        int stacks = Math.max(0, Math.min(MAX_PRESSURE, pressure));
        return (float) Math.min(128.0, 6.0 + attack * 1.5 + stacks * (4.0 + attack * 0.35));
    }

    public static boolean inBlast(double horizontalSquared, double height, int pressure) {
        double radius = radius(pressure);
        return Double.isFinite(horizontalSquared) && Double.isFinite(height)
                && horizontalSquared >= 0 && horizontalSquared <= radius * radius && Math.abs(height) <= 2.5;
    }
}
