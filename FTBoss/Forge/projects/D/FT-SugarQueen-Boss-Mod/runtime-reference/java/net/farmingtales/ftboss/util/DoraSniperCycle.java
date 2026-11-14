/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.UUID;
import net.farmingtales.ftboss.util.HiddenBossCastRules;

public final class DoraSniperCycle {
    public static final int CHARGE_TICKS = HiddenBossCastRules.quicker(40);
    public static final int TRACK_TICKS = HiddenBossCastRules.quicker(28);
    public static final int RECOVERY_TICKS = 12;
    public static final double RANGE = 36.0;
    public static final double RADIUS = 0.72;
    private UUID target;
    private int age;
    private int chargeTicks = CHARGE_TICKS;
    private int trackTicks = TRACK_TICKS;
    private int recoveryTicks = 12;

    public boolean begin(UUID victim) {
        return this.begin(victim, false);
    }

    public boolean begin(UUID victim, boolean accelerated) {
        if (victim == null || this.busy()) {
            return false;
        }
        this.chargeTicks = accelerated ? (CHARGE_TICKS + 1) / 2 : CHARGE_TICKS;
        this.trackTicks = accelerated ? (TRACK_TICKS + 1) / 2 : TRACK_TICKS;
        this.recoveryTicks = accelerated ? 6 : 12;
        this.target = victim;
        this.age = 0;
        return true;
    }

    public Step tick(UUID victim, boolean valid) {
        if (!this.busy()) {
            return Step.IDLE;
        }
        if (!valid || !this.target.equals(victim)) {
            this.reset();
            return Step.CANCELLED;
        }
        ++this.age;
        if (this.age < this.trackTicks) {
            return Step.TRACKING;
        }
        if (this.age == this.trackTicks) {
            return Step.LOCK;
        }
        if (this.age < this.chargeTicks) {
            return Step.LOCKED;
        }
        if (this.age == this.chargeTicks) {
            return Step.FIRE;
        }
        if (this.age < this.chargeTicks + this.recoveryTicks) {
            return Step.RECOVERY;
        }
        this.reset();
        return Step.FINISHED;
    }

    public boolean busy() {
        return this.target != null;
    }

    public boolean charging() {
        return this.busy() && this.age < this.chargeTicks;
    }

    public boolean locked() {
        return this.busy() && this.age >= this.trackTicks;
    }

    public static boolean inRange(double distanceSquared) {
        return Double.isFinite(distanceSquared) && distanceSquared >= 0.0 && distanceSquared <= 1296.0;
    }

    public void reset() {
        this.target = null;
        this.age = 0;
    }

    public static enum Step {
        IDLE,
        TRACKING,
        LOCK,
        LOCKED,
        FIRE,
        RECOVERY,
        FINISHED,
        CANCELLED;

    }
}

