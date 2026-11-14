/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class FullFightFlightRules {
    public static final double RADIUS = 96.0;

    private FullFightFlightRules() {
    }

    public static boolean contains(double d, double d2, double d3, double d4, boolean bl) {
        if (bl) {
            return true;
        }
        double d5 = d - d3;
        double d6 = d2 - d4;
        return d5 * d5 + d6 * d6 <= 9216.0;
    }
}

