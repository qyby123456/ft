/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.function.IntUnaryOperator;

public final class ZicaiCurioSilenceRules {
    public static final int SILENCE_TICKS = 100;

    private ZicaiCurioSilenceRules() {
    }

    public static boolean eligible(int n, boolean bl, boolean bl2) {
        return n == 8 && !bl && !bl2;
    }

    public static long expires(long l) {
        return l > 9223372036854775707L ? Long.MAX_VALUE : l + 100L;
    }

    public static boolean active(long l, long l2) {
        return l2 > l && l2 - l <= 100L;
    }

    public static int choose(int n, IntUnaryOperator intUnaryOperator) {
        return n <= 0 ? -1 : intUnaryOperator.applyAsInt(n);
    }
}

