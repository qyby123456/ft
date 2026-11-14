/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.manorrun;

import net.farmingtales.ftboss.manorrun.RunMotion;

public final class RunState {
    private double lastClock = Double.NaN;
    private double lastX;
    private double lastZ;
    private double speed;
    private double blend;
    private double phase;
    private int lastTick;
    private boolean wasAllowed;

    public Frame advance(int n, int n2, double d, double d2, double d3, boolean bl, boolean bl2) {
        double d4;
        double d5;
        double d6;
        if (!(Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3))) {
            return new Frame(this.phase, 0.0, 0.0, true);
        }
        if (!Double.isFinite(this.lastClock) || d < this.lastClock || d - this.lastClock > 5.0 || n2 < this.lastTick) {
            this.reset(n2, d, d2, d3);
            return new Frame(this.phase, 0.0, 0.0, true);
        }
        boolean bl3 = false;
        if (n2 != this.lastTick) {
            d6 = n2 - this.lastTick;
            d5 = this.wasAllowed ? RunMotion.target(this.speed) : 0.0;
            this.blend = RunState.approach(this.blend, d5, d6);
            if (this.speed > 0.025) {
                this.phase = (this.phase + d6 * RunMotion.cadence(RunMotion.PROFILES[Math.max(0, Math.min(11, n))], this.speed) * Math.PI * 2.0 / 20.0) % (Math.PI * 2);
            }
            if ((d4 = Math.hypot(d2 - this.lastX, d3 - this.lastZ) / Math.max(1.0, d6)) > 0.85) {
                this.speed = 0.0;
                this.blend = 0.0;
                bl3 = true;
            } else {
                this.speed = d4;
            }
            this.lastX = d2;
            this.lastZ = d3;
            this.lastTick = n2;
        }
        double d7 = d6 = bl && !bl2 ? RunMotion.target(this.speed) : 0.0;
        if (bl2 || !bl) {
            this.blend = 0.0;
        }
        d5 = RunMotion.clamp(d - (double)n2, 0.0, 1.0);
        d4 = this.phase;
        if (this.speed > 0.025 && !bl3) {
            d4 = (this.phase + d5 * RunMotion.cadence(RunMotion.PROFILES[Math.max(0, Math.min(11, n))], this.speed) * Math.PI * 2.0 / 20.0) % (Math.PI * 2);
        }
        this.wasAllowed = bl && !bl2;
        this.lastClock = d;
        return new Frame(d4, RunMotion.clamp(RunState.approach(this.blend, d6, d5), 0.0, 1.0), this.speed, bl3);
    }

    private static double approach(double d, double d2, double d3) {
        return d + (d2 - d) * (1.0 - Math.exp(-d3 / (d2 > d ? 2.8 : 2.0)));
    }

    private void reset(int n, double d, double d2, double d3) {
        this.lastTick = n;
        this.lastClock = d;
        this.lastX = d2;
        this.lastZ = d3;
        this.speed = 0.0;
        this.blend = 0.0;
        this.phase = 0.0;
        this.wasAllowed = false;
    }

    public record Frame(double phase, double blend, double speed, boolean discontinuity) {
    }
}

