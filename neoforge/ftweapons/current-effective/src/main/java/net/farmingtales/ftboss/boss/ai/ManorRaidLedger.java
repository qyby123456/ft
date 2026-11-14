package net.farmingtales.ftboss.boss.ai;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Predicate;

public final class ManorRaidLedger {
    private UUID stage;
    private final Map<UUID, Double> current = new LinkedHashMap<>();
    private final Map<UUID, Double> previous = new LinkedHashMap<>();
    private boolean bonusConsumed;
    private double currentPhysical;
    private double currentMagic;
    private double previousPhysical;
    private double previousMagic;

    public boolean enter(UUID boss) {
        if (boss != null && !boss.equals(this.stage)) {
            this.previous.clear();
            this.previous.putAll(this.current);
            this.current.clear();
            this.previousPhysical = this.currentPhysical;
            this.previousMagic = this.currentMagic;
            this.currentPhysical = 0.0;
            this.currentMagic = 0.0;
            this.stage = boss;
            this.bonusConsumed = false;
            return true;
        } else {
            return false;
        }
    }

    public void record(UUID boss, UUID player, double damage) {
        if (boss != null && player != null && Double.isFinite(damage) && !(damage <= 0.0)) {
            this.enter(boss);
            this.current.merge(player, damage, (before, added) -> Math.min(Double.MAX_VALUE, before + added));
        }
    }

    public UUID previousLeader() {
        UUID leader = null;
        double maximum = 0.0;

        for (Entry<UUID, Double> entry : this.previous.entrySet()) {
            if (entry.getValue() > maximum || entry.getValue() == maximum && (leader == null || entry.getKey().toString().compareTo(leader.toString()) < 0)) {
                leader = entry.getKey();
                maximum = entry.getValue();
            }
        }

        return leader;
    }

    public void recordKind(int kind, double damage) {
        if (Double.isFinite(damage) && !(damage <= 0.0)) {
            if (kind == 1) {
                this.currentPhysical = Math.min(Double.MAX_VALUE, this.currentPhysical + damage);
            }

            if (kind == 2) {
                this.currentMagic = Math.min(Double.MAX_VALUE, this.currentMagic + damage);
            }
        }
    }

    public int adaptiveKind() {
        return ManorRaidRules.dominantDamage(this.previousPhysical, this.previousMagic);
    }

    public double currentPhysical() {
        return this.currentPhysical;
    }

    public double currentMagic() {
        return this.currentMagic;
    }

    public double previousPhysical() {
        return this.previousPhysical;
    }

    public double previousMagic() {
        return this.previousMagic;
    }

    public void restoreDamageMix(double currentPhysical, double currentMagic, double previousPhysical, double previousMagic) {
        this.currentPhysical = positive(currentPhysical);
        this.currentMagic = positive(currentMagic);
        this.previousPhysical = positive(previousPhysical);
        this.previousMagic = positive(previousMagic);
    }

    private static double positive(double damage) {
        return Double.isFinite(damage) && damage > 0.0 ? damage : 0.0;
    }

    public UUID consumeBonus(boolean multiplayer, Predicate<UUID> valid) {
        if (this.bonusConsumed) {
            return null;
        } else {
            this.bonusConsumed = true;
            UUID leader = this.previousLeader();
            return multiplayer && leader != null && valid.test(leader) ? leader : null;
        }
    }

    public UUID stage() {
        return this.stage;
    }

    public boolean bonusConsumed() {
        return this.bonusConsumed;
    }

    public Map<UUID, Double> current() {
        return Map.copyOf(this.current);
    }

    public Map<UUID, Double> previous() {
        return Map.copyOf(this.previous);
    }

    public static ManorRaidLedger restore(UUID stage, Map<UUID, Double> current, Map<UUID, Double> previous, boolean consumed) {
        ManorRaidLedger ledger = new ManorRaidLedger();
        ledger.stage = stage;
        copyPositive(current, ledger.current);
        copyPositive(previous, ledger.previous);
        ledger.bonusConsumed = consumed;
        return ledger;
    }

    private static void copyPositive(Map<UUID, Double> from, Map<UUID, Double> to) {
        from.forEach((player, damage) -> {
            if (player != null && damage != null && Double.isFinite(damage) && damage > 0.0) {
                to.put(player, damage);
            }
        });
    }
}
