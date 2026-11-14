/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.List;
import java.util.function.IntUnaryOperator;

public final class SparrowDisruptionRules {
    public static final int INTERVAL_TICKS = 200;
    public static final int SILENCE_TICKS = 20;
    public static final double RANGE = 48.0;

    private SparrowDisruptionRules() {
    }

    public static boolean due(long l, long l2) {
        return l2 >= 0L && l >= l2;
    }

    public static boolean active(long l, long l2) {
        return l2 > l && l2 - l <= 20L;
    }

    public static boolean removable(boolean bl, boolean bl2) {
        return bl && !bl2;
    }

    public static <Value> Value choose(List<Value> list, IntUnaryOperator intUnaryOperator) {
        return list.isEmpty() ? null : (Value)list.get(intUnaryOperator.applyAsInt(list.size()));
    }
}

