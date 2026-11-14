/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.qizairun;

import net.farmingtales.ftboss.client.qizairun.QizaiRunPose;

public final class QizaiRunClock {
    public static final double START_SPEED = 0.24;
    public static final double STOP_SPEED = 0.17;
    public static final double FULL_SPEED = 0.4;
    public static final double CYCLE_DISTANCE = 2.4;
    private double lastTime = Double.NaN;
    private double lastX;
    private double lastY;
    private double lastZ;
    private double speed;
    private double blend;
    private double phase;
    private double forward = 1.0;
    private double side;
    private boolean running;

    private State reset(double d, double d2, double d3, double d4) {
        this.lastTime = d;
        this.lastX = d2;
        this.lastY = d3;
        this.lastZ = d4;
        this.phase = 0.0;
        this.blend = 0.0;
        this.speed = 0.0;
        this.running = false;
        this.forward = 1.0;
        this.side = 0.0;
        return new State(0.0, 0.0, 0.0, 1.0, 0.0, true);
    }

    public State update(double d, double d2, double d3, double d4, boolean bl, double d5) {
        double d6;
        double d7;
        if (!(Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3) && Double.isFinite(d4) && Double.isFinite(d5))) {
            this.lastTime = Double.NaN;
            this.phase = 0.0;
            this.blend = 0.0;
            this.speed = 0.0;
            this.running = false;
            return new State(0.0, 0.0, 0.0, 1.0, 0.0, true);
        }
        if (!Double.isFinite(this.lastTime) || d < this.lastTime || d - this.lastTime > 6.0 || !bl) {
            return this.reset(d, d2, d3, d4);
        }
        double d8 = d - this.lastTime;
        if (d8 <= 1.0E-8) {
            return new State(this.phase, this.speed, this.blend, this.forward, this.side, false);
        }
        double d9 = d2 - this.lastX;
        double d10 = d4 - this.lastZ;
        double d11 = Math.hypot(d9, d10) / d8;
        if (d11 > 1.6 || Math.abs(d3 - this.lastY) / d8 > 1.5) {
            return this.reset(d, d2, d3, d4);
        }
        double d12 = this.speed;
        double d13 = 1.0 - Math.exp(-d8 / 2.5);
        this.speed += (d11 - this.speed) * d13;
        double d14 = d11 * d8 + (d12 - d11) * 2.5 * d13;
        double d15 = 0.33599999999999997;
        if (Math.min(d12, this.speed) >= d15) {
            d14 = d15 * d8;
        } else if (Math.max(d12, this.speed) > d15) {
            d7 = QizaiRunPose.clamp(-2.5 * Math.log((d15 - d11) / (d12 - d11)), 0.0, d8);
            d6 = d11 * d7 + (d12 - d11) * 2.5 * (1.0 - Math.exp(-d7 / 2.5));
            double d16 = d14 = d12 < d15 ? d6 + d15 * (d8 - d7) : d15 * d7 + d14 - d6;
        }
        if (d11 > 0.005) {
            d7 = Math.toRadians(d5);
            d6 = Math.hypot(d9, d10);
            this.forward += ((-d9 * Math.sin(d7) + d10 * Math.cos(d7)) / d6 - this.forward) * d13;
            this.side += ((d9 * Math.cos(d7) + d10 * Math.sin(d7)) / d6 - this.side) * d13;
        }
        if (this.running && this.speed < 0.17) {
            this.running = false;
        } else if (!this.running && this.speed >= 0.24) {
            this.running = true;
        }
        d7 = this.running ? QizaiRunPose.smooth((this.speed - 0.17) / 0.23) : 0.0;
        this.blend += (d7 - this.blend) * (1.0 - Math.exp(-d8 / (d7 > this.blend ? 3.2 : 2.2)));
        this.phase = (this.phase + Math.max(0.0, d14) * Math.PI * 2.0 / 2.4) % (Math.PI * 2);
        this.lastTime = d;
        this.lastX = d2;
        this.lastY = d3;
        this.lastZ = d4;
        return new State(this.phase, this.speed, this.blend, this.forward, this.side, false);
    }

    public record State(double phase, double speed, double blend, double forward, double side, boolean reset) {
    }
}

