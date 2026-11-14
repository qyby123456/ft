package net.farmingtales.ftboss.util;

public final class DandanScytheRules {
    private static final double HALF_ARC_COSINE = Math.cos(Math.toRadians(70));

    private DandanScytheRules() {}

    public static boolean withinSweep(double horizontal, double depth, double yaw, double radius, boolean fullCircle) {
        if (!Double.isFinite(horizontal) || !Double.isFinite(depth) || !Double.isFinite(yaw)
                || !Double.isFinite(radius) || radius <= 0) return false;
        double squared = horizontal * horizontal + depth * depth;
        if (squared > radius * radius) return false;
        if (fullCircle || squared <= 0.01) return true;
        double angle = Math.toRadians(yaw);
        return (-Math.sin(angle) * horizontal + Math.cos(angle) * depth) / Math.sqrt(squared) >= HALF_ARC_COSINE - 1.0E-10;
    }

    public static boolean validBlinkDelta(double horizontal, double vertical, double depth) {
        if (!Double.isFinite(horizontal) || !Double.isFinite(vertical) || !Double.isFinite(depth)
                || Math.abs(vertical) > 0.01) return false;
        double squared = horizontal * horizontal + vertical * vertical + depth * depth;
        return squared >= 1.25 * 1.25 && squared <= 6.0 * 6.0;
    }

    public static boolean shouldDrop(int variant, boolean mini, boolean giant, boolean noAi, boolean finale,
                                     boolean playerKill, boolean mobLoot) {
        return variant == 1 && !mini && !giant && !noAi && !finale && playerKill && mobLoot;
    }
}
