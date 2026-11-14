package net.farmingtales.ftboss.util;

public final class FullFightFlightRules {
    public static final double RADIUS = 96.0;

    private FullFightFlightRules() {}

    public static boolean contains(double playerX, double playerZ, double anchorX, double anchorZ, boolean participant) {
        if (participant) return true;
        double deltaX = playerX - anchorX;
        double deltaZ = playerZ - anchorZ;
        return deltaX * deltaX + deltaZ * deltaZ <= RADIUS * RADIUS;
    }
}
