/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.dandaniron;

public final class IronClock {
    private IronClock() {
    }

    public static double progress(double d, long l, int n) {
        return Math.max(0.0, Math.min(1.0, (d - (double)l) / (double)Math.max(1, n)));
    }

    public static double clock(boolean bl, double d, long l, int n) {
        double d2 = IronClock.progress(d, l, n);
        return bl ? 15.0 * d2 : 15.0 + 24.166 * d2;
    }

    public static double opacity(boolean bl, double d, long l, int n, long l2) {
        if (d < (double)l || d - (double)l2 > 8.0 && bl || d >= (double)(l + (long)n)) {
            return 0.0;
        }
        if (bl) {
            return 0.72 + 0.28 * IronClock.smooth(IronClock.progress(d, l, n));
        }
        return 1.0 - IronClock.smooth((IronClock.progress(d, l, n) - 0.7) / 0.3);
    }

    public static double smooth(double d) {
        d = Math.max(0.0, Math.min(1.0, d));
        return d * d * (3.0 - 2.0 * d);
    }

    public static int displayTicks(String string, String string2, int n) {
        return Math.max(1, string2.equals("BEAM") ? Math.min(n, 8) : (string.equals("candle_reap") ? n / 2 : n));
    }

    public static double personOpacity(boolean bl, boolean bl2) {
        return !bl && bl2 ? 0.14 : 1.0;
    }
}

