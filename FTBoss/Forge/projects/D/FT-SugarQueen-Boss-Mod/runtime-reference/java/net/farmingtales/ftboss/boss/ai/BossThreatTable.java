/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public final class BossThreatTable {
    public static final int HALF_LIFE_TICKS = 600;
    public static final double MIN_THREAT = 0.1;
    public static final double FOOD_THREAT = 5.0;
    public static final double POTION_THREAT = 8.0;
    private final Map<UUID, Double> threat = new LinkedHashMap<UUID, Double>();
    private long updatedAt;

    public void add(UUID target, double amount, long now) {
        if (target == null || !Double.isFinite(amount) || amount <= 0.0) {
            return;
        }
        this.decay(now);
        this.threat.merge(target, amount, (previous, added) -> Math.min(Double.MAX_VALUE, previous + added));
    }

    public UUID highest(long now, UUID current, Predicate<UUID> valid) {
        this.decay(now);
        this.threat.keySet().removeIf(target -> !valid.test((UUID)target));
        UUID best = this.threat.containsKey(current) ? current : null;
        double bestThreat = this.threat.getOrDefault(best, 0.0);
        for (Map.Entry<UUID, Double> entry : this.threat.entrySet()) {
            if (!(entry.getValue() > bestThreat)) continue;
            best = entry.getKey();
            bestThreat = entry.getValue();
        }
        return best;
    }

    public boolean contains(UUID target) {
        return this.threat.containsKey(target);
    }

    public void remove(UUID target) {
        this.threat.remove(target);
    }

    public boolean isEmpty() {
        return this.threat.isEmpty();
    }

    public double value(UUID target, long now) {
        this.decay(now);
        return this.threat.getOrDefault(target, 0.0);
    }

    private void decay(long now) {
        if (this.threat.isEmpty()) {
            this.updatedAt = now;
            return;
        }
        if (now > this.updatedAt) {
            double multiplier = Math.pow(0.5, (double)(now - this.updatedAt) / 600.0);
            this.threat.replaceAll((target, amount) -> amount * multiplier);
            this.updatedAt = now;
        }
        this.threat.values().removeIf(amount -> amount < 0.1);
    }
}

