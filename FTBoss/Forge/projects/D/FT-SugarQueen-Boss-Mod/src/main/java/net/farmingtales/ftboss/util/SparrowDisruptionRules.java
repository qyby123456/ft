package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

public final class SparrowDisruptionRules {
    public static final int INTERVAL_TICKS = 200;
    public static final int SILENCE_TICKS = 100;
    public static final int HOTBAR_SIZE = 9;
    public static final double RANGE = 48.0;

    private SparrowDisruptionRules() {}

    public static boolean due(long now, long next) {
        return next >= 0L && now >= next;
    }

    public static boolean active(long now, long until) {
        return until > now && until - now <= SILENCE_TICKS;
    }

    public static boolean removable(boolean beneficial, boolean ambient) {
        return beneficial && !ambient;
    }

    public static <Value> Value choose(List<Value> candidates, IntUnaryOperator randomIndex) {
        return candidates.isEmpty() ? null : candidates.get(randomIndex.applyAsInt(candidates.size()));
    }

    public static <Value> Value chooseHotbar(List<Value> inventory, Predicate<Value> eligible, IntUnaryOperator randomIndex) {
        var candidates = new ArrayList<Value>();
        for (int slot = 0; slot < Math.min(HOTBAR_SIZE, inventory.size()); slot++) {
            Value value = inventory.get(slot);
            if (eligible.test(value)) candidates.add(value);
        }
        return choose(candidates, randomIndex);
    }
}
