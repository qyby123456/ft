package net.farmingtales.ftboss.util;

public final class FullFightFlightRules {
    public static final double RADIUS = 96.0;

    private FullFightFlightRules() {
    }

    public static boolean contains(double var0, double var2, double var4, double var6, boolean var8) {
        if (var8) {
            return true;
        } else {
            double var9 = var0 - var4;
            double var11 = var2 - var6;
            return var9 * var9 + var11 * var11 <= 9216.0;
        }
    }
}
