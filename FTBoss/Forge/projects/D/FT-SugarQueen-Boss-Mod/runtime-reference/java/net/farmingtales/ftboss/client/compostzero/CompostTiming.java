/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.compostzero;

public final class CompostTiming {
    private CompostTiming() {
    }

    public static int duration(String string, String string2, int n) {
        if (string2.equals("CHARGE")) {
            return 10;
        }
        if (string2.equals("BEAM")) {
            return 12;
        }
        if (string.equals("spore_eruption") && string2.equals("RELEASE")) {
            return 16;
        }
        return n;
    }

    public static boolean expired(long l, long l2, int n) {
        return l < l2 || l - l2 >= (long)n;
    }

    public static double radiusScale(double d) {
        return Double.isFinite(d) ? Math.max(0.25, Math.min(4.0, d)) : 1.0;
    }
}

