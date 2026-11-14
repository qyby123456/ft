package net.farmingtales.ftboss.util;

import java.util.function.IntUnaryOperator;

public final class ZicaiCurioSilenceRules {
    public static final int SILENCE_TICKS = 100;

    private ZicaiCurioSilenceRules() {}

    public static boolean eligible(int variant, boolean mini, boolean giant) {
        return variant == 8 && !mini && !giant;
    }

    public static long expires(long now) {
        return now > Long.MAX_VALUE - SILENCE_TICKS ? Long.MAX_VALUE : now + SILENCE_TICKS;
    }

    public static boolean active(long now, long until) {
        return until > now && until - now <= SILENCE_TICKS;
    }

    public static int choose(int size, IntUnaryOperator random) {
        return size <= 0 ? -1 : random.applyAsInt(size);
    }
}
