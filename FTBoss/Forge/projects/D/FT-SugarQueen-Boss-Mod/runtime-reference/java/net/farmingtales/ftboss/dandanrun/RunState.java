/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.dandanrun;

public final class RunState {
    public double phase;
    public double previousPhase;
    public double phaseStep;
    public double weight;
    public double previousWeight;
    public double speed;
    public boolean running;
    public long tick = Long.MIN_VALUE;
    private double horizontal;
    private double depth;

    public static boolean activeTag(long l, long l2, int n, int n2) {
        return n2 == 1 && n > 0 && n <= 200 && l >= l2 && l < l2 + (long)n;
    }

    public static boolean validDistance(double d, long l) {
        return Double.isFinite(d) && l == 1L && d <= 1.25;
    }

    public void update(long l, double d, double d2, boolean bl) {
        if (l == this.tick) {
            return;
        }
        long l2 = this.tick == Long.MIN_VALUE ? 0L : l - this.tick;
        double d3 = Math.hypot(d - this.horizontal, d2 - this.depth);
        this.horizontal = d;
        this.depth = d2;
        this.tick = l;
        this.previousPhase = this.phase;
        this.previousWeight = this.weight;
        if (!bl || !RunState.validDistance(d3, l2)) {
            this.reset();
            return;
        }
        this.speed += (d3 - this.speed) * 0.45;
        this.running = this.running ? this.speed > 0.15 : this.speed >= 0.2;
        double d4 = this.running ? 1.0 : 0.0;
        this.weight += Math.max(-0.22, Math.min(0.22, d4 - this.weight));
        this.phaseStep = this.weight > 0.0 ? Math.min(0.18, d3 / 2.4) : 0.0;
        this.phase += this.phaseStep;
        if (this.phase > 4096.0) {
            this.phase -= 4096.0;
            this.previousPhase -= 4096.0;
        }
    }

    public void reset() {
        this.speed = 0.0;
        this.weight = 0.0;
        this.previousWeight = 0.0;
        this.running = false;
        this.phaseStep = 0.0;
    }

    public double phaseAt(double d) {
        return this.previousPhase + this.phaseStep * Math.max(0.0, Math.min(1.0, d));
    }

    public double weightAt(double d) {
        return this.previousWeight + (this.weight - this.previousWeight) * Math.max(0.0, Math.min(1.0, d));
    }
}

