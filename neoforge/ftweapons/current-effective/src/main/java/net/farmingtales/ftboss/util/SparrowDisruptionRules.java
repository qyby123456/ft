package net.farmingtales.ftboss.util;

import java.util.List;
import java.util.function.IntUnaryOperator;

public final class SparrowDisruptionRules {
    public static final int INTERVAL_TICKS = 200;
    public static final int SILENCE_TICKS = 20;
    public static final double RANGE = 48.0;

    private SparrowDisruptionRules() {
    }

    public static boolean due(long var0, long var2) {
        return var2 >= 0L && var0 >= var2;
    }

    public static boolean active(long var0, long var2) {
        return var2 > var0 && var2 - var0 <= 20L;
    }

    public static boolean removable(boolean var0, boolean var1) {
        return var0 && !var1;
    }

    public static <Value> Value choose(List<Value> var0, IntUnaryOperator var1) {
        return (Value)(var0.isEmpty() ? null : var0.get(var1.applyAsInt(var0.size())));
    }
}
