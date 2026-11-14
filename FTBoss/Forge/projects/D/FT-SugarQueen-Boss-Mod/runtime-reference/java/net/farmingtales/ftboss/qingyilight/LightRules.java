/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.qingyilight;

public final class LightRules {
    public static final int TIDE = 0;
    public static final int THUNDER = 1;
    public static final int STAR = 2;
    public static final int CONFLUENCE = 3;
    public static final int DASH = 4;
    public static final int BLINK = 5;
    public static final int BIRTH = 6;
    public static final int IMPACT = 7;
    public static final int POTION = 8;
    public static final int CHARGE = 9;
    public static final int ORB = 10;
    public static final int CANCEL = 11;
    public static final int MAX_EVENTS = 96;
    public static final int MAX_TRAILS = 48;
    public static final int MAX_HISTORY = 7;

    private LightRules() {
    }

    public static double smooth(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double opacity(double d, int n) {
        return Double.isFinite(d) && d >= 0.0 && d < (double)n ? LightRules.smooth((d + 0.25) / 2.0) * LightRules.smooth(((double)n - d) / Math.min(6.0, (double)n * 0.4)) : 0.0;
    }

    public static boolean valid(int n, int n2, int n3, double d, int n4) {
        return n >= 0 && n <= 11 && n2 >= 0 && n2 <= 2 && n3 >= 1 && n3 <= 640 && Double.isFinite(d) && d >= 0.0 && d <= 32.0 && n4 >= 0 && n4 <= 8;
    }

    public static boolean point(double d, double d2, double d3) {
        return Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3) && Math.abs(d) <= 3.0E7 && Math.abs(d3) <= 3.0E7 && Math.abs(d2) <= 4096.0;
    }

    public static boolean direction(double d, double d2, double d3) {
        return Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3) && d * d + d2 * d2 + d3 * d3 <= 1.00001;
    }

    public static int budget(int n) {
        return n == 2 ? 1800 : (n == 1 ? 3600 : 6400);
    }

    public static double cameraOpacity(double d, double d2, double d3, double d4, double d5, double d6) {
        double d7 = Math.max(0.0, Math.max(d, -d4));
        double d8 = Math.max(0.0, Math.max(d2, -d5));
        double d9 = Math.max(0.0, Math.max(d3, -d6));
        return LightRules.smooth((Math.sqrt(d7 * d7 + d8 * d8 + d9 * d9) - 0.3) / 0.9);
    }

    public static boolean trailSegment(double d) {
        return Double.isFinite(d) && d > 1.0E-6 && d <= 16.0;
    }
}

