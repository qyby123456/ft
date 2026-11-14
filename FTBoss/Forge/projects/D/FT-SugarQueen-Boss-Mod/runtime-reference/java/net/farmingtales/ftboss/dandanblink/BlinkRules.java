/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.dandanblink;

import java.util.List;
import java.util.function.IntUnaryOperator;

public final class BlinkRules {
    private BlinkRules() {
    }

    private static double distance(double d) {
        return Double.isFinite(d) && d >= 0.0 ? Math.min(192.0, d) : 192.0;
    }

    public static int swapCooldown(double d) {
        return 80 + (int)Math.ceil(Math.max(0.0, BlinkRules.distance(d) - 4.0) * 2.0);
    }

    public static int rushCooldown(double d) {
        return 5 + (int)Math.ceil(Math.max(0.0, BlinkRules.distance(d) - 4.0) / 4.0);
    }

    public static boolean ready(long l, long l2, long l3) {
        return l < l2 || l >= l3;
    }

    public static <Target> Target choose(List<Target> list, IntUnaryOperator intUnaryOperator) {
        if (list.isEmpty()) {
            return null;
        }
        return list.get(intUnaryOperator.applyAsInt(list.size()));
    }
}

