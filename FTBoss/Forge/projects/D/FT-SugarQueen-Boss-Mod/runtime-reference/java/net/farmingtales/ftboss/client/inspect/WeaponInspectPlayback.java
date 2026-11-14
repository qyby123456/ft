/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.inspect;

import net.farmingtales.ftboss.client.inspect.WeaponInspectMotion;

public final class WeaponInspectPlayback {
    public static final int TICKS_PER_SECOND = 20;
    public static final int RETURN_TICKS = 6;
    private WeaponInspectMotion.Clip clip;
    private WeaponInspectMotion.Pose returnPose;
    private int ticks;
    private int previousTicks;

    public void start(WeaponInspectMotion.Clip selected) {
        this.clear();
        this.clip = selected;
    }

    public boolean active() {
        return this.clip != null;
    }

    public boolean returning() {
        return this.returnPose != null;
    }

    public void tick() {
        double durationTicks;
        if (!this.active()) {
            return;
        }
        this.previousTicks = this.ticks++;
        double d = durationTicks = this.returning() ? 6.0 : this.clip.duration() * 20.0;
        if ((double)this.previousTicks >= durationTicks) {
            this.clear();
        }
    }

    public WeaponInspectMotion.Pose sample(double partialTick) {
        if (!this.active()) {
            return WeaponInspectMotion.REST;
        }
        double time = (double)this.previousTicks + (double)(this.ticks - this.previousTicks) * Math.max(0.0, Math.min(1.0, partialTick));
        return this.returning() ? this.returnPose.blend(WeaponInspectMotion.REST, time / 6.0) : this.clip.sample(time / 20.0);
    }

    public void finish(double partialTick) {
        if (!this.active() || this.returning()) {
            return;
        }
        this.returnPose = this.sample(partialTick);
        this.ticks = 0;
        this.previousTicks = 0;
    }

    public void clear() {
        this.clip = null;
        this.returnPose = null;
        this.ticks = 0;
        this.previousTicks = 0;
    }
}

