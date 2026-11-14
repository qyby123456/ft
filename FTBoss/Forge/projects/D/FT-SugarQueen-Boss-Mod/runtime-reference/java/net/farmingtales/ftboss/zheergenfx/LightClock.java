/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.zheergenfx;

public final class LightClock {
    private LightClock() {
    }

    public static boolean alive(double d, int n) {
        return Double.isFinite(d) && d >= 0.0 && n > 0 && d < (double)n;
    }

    public static double progress(double d, int n) {
        return Math.max(0.0, Math.min(1.0, d / (double)Math.max(1, n)));
    }

    public static int variant(int n, int n2) {
        return n == 3 ? Math.max(0, Math.min(2, n2)) : (n == 10 ? Math.max(0, Math.min(5, n2)) : (n == 1 ? n2 % 2 : 0));
    }

    public static boolean charge(int n) {
        return n == 0 || n == 2 || n == 4;
    }

    public static int family(int n) {
        return n <= 1 ? 0 : (n <= 3 ? 2 : (n <= 5 ? 4 : n));
    }
}

