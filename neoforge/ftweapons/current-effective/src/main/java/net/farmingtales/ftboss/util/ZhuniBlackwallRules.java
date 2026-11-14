package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

public final class ZhuniBlackwallRules {
    public static final int INTERVAL_TICKS = 200;
    public static final int DURATION_TICKS = 100;
    public static final int ITEM_COUNT = 3;

    private ZhuniBlackwallRules() {
    }

    public static <T> List<T> choose(List<T> var0, IntUnaryOperator var1) {
        ArrayList var2 = new ArrayList(var0);
        ArrayList var3 = new ArrayList();

        while (!var2.isEmpty() && var3.size() < 3) {
            var3.add(var2.remove(var1.applyAsInt(var2.size())));
        }

        return var3;
    }

    public static boolean due(long var0, long var2) {
        return var2 > 0L && var0 >= var2;
    }

    public static float strength(float var0) {
        if (!(var0 < 0.0F) && !(var0 >= 100.0F)) {
            float var1 = Math.min(1.0F, (100.0F - var0) / 16.0F);
            return (0.58F + 0.42F * (float)Math.exp((double)(-var0 / 9.0F))) * var1;
        } else {
            return 0.0F;
        }
    }
}
