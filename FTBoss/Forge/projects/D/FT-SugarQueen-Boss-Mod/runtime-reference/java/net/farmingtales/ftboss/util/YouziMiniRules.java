/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class YouziMiniRules {
    public static final double HEALTH = 75.0;
    public static final double SPEED_MULTIPLIER = 0.9;

    private YouziMiniRules() {
    }

    public static double health(double d, int n) {
        return n == 0 ? 75.0 : d;
    }

    public static double speed(double d, int n, boolean bl) {
        return n == 0 && bl ? d * 0.9 : d;
    }
}

