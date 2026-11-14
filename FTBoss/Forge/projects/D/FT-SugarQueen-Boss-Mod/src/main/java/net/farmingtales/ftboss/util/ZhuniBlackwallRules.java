package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

public final class ZhuniBlackwallRules {
    public static final int INTERVAL_TICKS = 200;
    public static final int DURATION_TICKS = 100;
    public static final int ITEM_COUNT = 3;
    private ZhuniBlackwallRules() {}

    public static <T> List<T> choose(List<T> distinctItems, IntUnaryOperator random) {
        List<T> pool = new ArrayList<>(distinctItems);
        List<T> selected = new ArrayList<>();
        while (!pool.isEmpty() && selected.size() < ITEM_COUNT) selected.add(pool.remove(random.applyAsInt(pool.size())));
        return selected;
    }

    public static boolean due(long now, long next) { return next > 0 && now >= next; }

    public static float strength(float age) {
        if (age < 0 || age >= DURATION_TICKS) return 0;
        float fade = Math.min(1, (DURATION_TICKS - age) / 16f);
        return (0.58f + 0.42f * (float) Math.exp(-age / 9f)) * fade;
    }
}
