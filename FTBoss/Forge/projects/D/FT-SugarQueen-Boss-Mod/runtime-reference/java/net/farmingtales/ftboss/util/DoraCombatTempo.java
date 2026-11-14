/*
 * Decompiled with CFR 0.152.
 */
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
        return this.stacks;
    }

    public void tick(long now) {
        if (this.stacks > 0 ? now >= this.expiresAt : this.hits > 0 && now - this.lastHit >= 60L) {
            this.reset();
        }
    }

    public int hit(UUID victim, long now) {
        this.tick(now);
        if (!victim.equals(this.target)) {
            this.reset();
        }
        this.target = victim;
        this.lastHit = now;
        if (this.stacks > 0) {
            this.stacks = Math.min(6, this.stacks + 1);
        } else if (++this.hits >= 3) {
            this.stacks = 1;
        }
        if (this.stacks > 0) {
            this.expiresAt = now + 120L;
        }
        return this.stacks;
    }

    public void miss() {
        this.hits = 0;
    }

    public void reset() {
        this.target = null;
        this.hits = 0;
        this.stacks = 0;
        this.lastHit = 0L;
        this.expiresAt = 0L;
    }

    public double attackSpeedMultiplier() {
        return 1.0 + (double)this.stacks * 0.125;
    }

    public double cooldownMultiplier() {
        return 1.0 - (double)this.stacks * 0.05;
    }

    public int burstShots(int base) {
        return base + this.stacks * 4;
    }

    public int spearInterval() {
        return Math.max(3, (int)Math.ceil(5.0 / this.attackSpeedMultiplier()));
    }
}

