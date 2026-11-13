package net.farmingtales.ftdungeonmobs;

public final class BloodPoolRitual {
    public static final int REQUIRED = 16;
    public static final int ACTIVE_TICKS = 200;
    public static final int COOLDOWN_TICKS = 600;
    private int charge;
    private long activeUntil;
    private long cooldownUntil;

    public int charge() {
        return charge;
    }

    public long activeUntil() {
        return activeUntil;
    }

    public long cooldownUntil() {
        return cooldownUntil;
    }

    public int phase(long now) {
        return now >= 0 && now < activeUntil ? 2 : now >= 0 && now < cooldownUntil ? 3 : charge > 0 ? 1 : 0;
    }

    public boolean offer(int points, long now) {
        if (points <= 0 || points > REQUIRED || now < 0 || phase(now) >= 2 || charge >= REQUIRED) {
            return false;
        }
        activeUntil = 0;
        cooldownUntil = 0;
        charge = Math.min(REQUIRED, charge + points);
        return true;
    }

    public boolean activate(long now) {
        if (charge != REQUIRED || phase(now) >= 2 || now < 0 || now > Long.MAX_VALUE - ACTIVE_TICKS - COOLDOWN_TICKS) {
            return false;
        }
        charge = 0;
        activeUntil = now + ACTIVE_TICKS;
        cooldownUntil = activeUntil + COOLDOWN_TICKS;
        return true;
    }

    public void interrupt(long now) {
        if (phase(now) == 2) {
            activeUntil = now;
            cooldownUntil = now > Long.MAX_VALUE - COOLDOWN_TICKS ? Long.MAX_VALUE : now + COOLDOWN_TICKS;
        }
    }

    public void restore(int savedCharge, long savedActive, long savedCooldown) {
        charge = Math.max(0, Math.min(REQUIRED, savedCharge));
        activeUntil = Math.max(0, savedActive);
        cooldownUntil = Math.max(activeUntil, savedCooldown);
        if (cooldownUntil - activeUntil > COOLDOWN_TICKS) {
            activeUntil = 0;
            cooldownUntil = 0;
        }
        if (activeUntil > 0 || cooldownUntil > 0) {
            charge = 0;
        }
    }
}
