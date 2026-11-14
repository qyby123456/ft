package net.farmingtales.ftboss.util;

public final class ManorBaseMovementSpeed {
    public static final double MULTIPLIER = 1.30;

    private ManorBaseMovementSpeed() {}

    public static double scale(double original, boolean mini) {
        return mini ? original : original * MULTIPLIER;
    }
}
