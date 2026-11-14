package net.farmingtales.ftboss.util;

import java.util.UUID;

public final class DoraCombatTempo {
    public static final int TRIGGER_HITS = 3;
    public static final int COMBO_WINDOW = 60;
    public static final int DURATION = 120;
    public static final int MAX_STACKS = 6;
    private UUID target;
    private int hits;
    private int stacks;
    private long lastHit;
    private long expiresAt;

    public int stacks() {
        return stacks;
    }

    public void tick(long now) {
        if (stacks > 0 ? now >= expiresAt : hits > 0 && now - lastHit >= COMBO_WINDOW) reset();
    }

    public int hit(UUID victim, long now) {
        tick(now);
        if (!victim.equals(target)) reset();
        target = victim;
        lastHit = now;
        if (stacks > 0) stacks = Math.min(MAX_STACKS, stacks + 1);
        else if (++hits >= TRIGGER_HITS) stacks = 1;
        if (stacks > 0) expiresAt = now + DURATION;
        return stacks;
    }

    public void miss() {
        hits = 0;
    }

    public void reset() {
        target = null;
        hits = 0;
        stacks = 0;
        lastHit = 0;
        expiresAt = 0;
    }

    public double attackSpeedMultiplier() {
        return 1.0 + stacks * 0.125;
    }

    public double cooldownMultiplier() {
        return 1.0 - stacks * 0.05;
    }

    public int burstShots(int base) {
        return base + stacks * 4;
    }

    public int spearInterval() {
        return Math.max(3, (int) Math.ceil(5.0 / attackSpeedMultiplier()));
    }
}
