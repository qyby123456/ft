package net.farmingtales.ftboss.util;

public final class YouziMiniRules {
    public static final double HEALTH = 250.0;
    public static final int ARMOR = 55;
    public static final double SPEED_MULTIPLIER = 0.9;

    private YouziMiniRules() {}

    public static double health(double original, int variant) {
        return variant == 0 ? HEALTH : original;
    }

    public static double speed(double original, int variant, boolean mini) {
        return variant == 0 && mini ? original * SPEED_MULTIPLIER : original;
    }
}
