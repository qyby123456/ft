package net.farmingtales.ftboss.boss.ai;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public final class BossThreatTable {
    public static final int HALF_LIFE_TICKS = 30 * 20;
    public static final double MIN_THREAT = 0.1;
    public static final double FOOD_THREAT = 5.0;
    public static final double POTION_THREAT = 8.0;

    private final Map<UUID, Double> threat = new LinkedHashMap<>();
    private long updatedAt;

    public void add(UUID target, double amount, long now) {
        if (target == null || !Double.isFinite(amount) || amount <= 0.0) return;
        decay(now);
        threat.merge(target, amount, (previous, added) -> Math.min(Double.MAX_VALUE, previous + added));
    }

    public UUID highest(long now, UUID current, Predicate<UUID> valid) {
        decay(now);
        threat.keySet().removeIf(target -> !valid.test(target));
        UUID best = threat.containsKey(current) ? current : null;
        double bestThreat = threat.getOrDefault(best, 0.0);
        for (Map.Entry<UUID, Double> entry : threat.entrySet()) {
            if (entry.getValue() > bestThreat) {
                best = entry.getKey();
                bestThreat = entry.getValue();
            }
        }
        return best;
    }

    public boolean contains(UUID target) {
        return threat.containsKey(target);
    }

    public void remove(UUID target) {
        threat.remove(target);
    }

    public boolean isEmpty() {
        return threat.isEmpty();
    }

    public double value(UUID target, long now) {
        decay(now);
        return threat.getOrDefault(target, 0.0);
    }

    private void decay(long now) {
        if (threat.isEmpty()) {
            updatedAt = now;
            return;
        }
        if (now > updatedAt) {
            double multiplier = Math.pow(0.5, (now - updatedAt) / (double) HALF_LIFE_TICKS);
            threat.replaceAll((target, amount) -> amount * multiplier);
            updatedAt = now;
        }
        threat.values().removeIf(amount -> amount < MIN_THREAT);
    }
}
