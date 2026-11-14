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

    public static boolean activeTag(long var0, long var2, int var4, int var5) {
        return var5 == 1 && var4 > 0 && var4 <= 200 && var0 >= var2 && var0 < var2 + (long)var4;
    }

    public static boolean validDistance(double var0, long var2) {
        return Double.isFinite(var0) && var2 == 1L && var0 <= 1.25;
    }

    public void update(long var1, double var3, double var5, boolean var7) {
        if (var1 != this.tick) {
            long var8 = this.tick == Long.MIN_VALUE ? 0L : var1 - this.tick;
            double var10 = Math.hypot(var3 - this.horizontal, var5 - this.depth);
            this.horizontal = var3;
            this.depth = var5;
            this.tick = var1;
            this.previousPhase = this.phase;
            this.previousWeight = this.weight;
            if (var7 && validDistance(var10, var8)) {
                this.speed = this.speed + (var10 - this.speed) * 0.45;
                this.running = this.running ? this.speed > 0.15 : this.speed >= 0.2;
                double var12 = this.running ? 1.0 : 0.0;
                this.weight = this.weight + Math.max(-0.22, Math.min(0.22, var12 - this.weight));
                this.phaseStep = this.weight > 0.0 ? Math.min(0.18, var10 / 2.4) : 0.0;
                this.phase = this.phase + this.phaseStep;
                if (this.phase > 4096.0) {
                    this.phase -= 4096.0;
                    this.previousPhase -= 4096.0;
                }
            } else {
                this.reset();
            }
        }
    }

    public void reset() {
        this.speed = 0.0;
        this.weight = 0.0;
        this.previousWeight = 0.0;
        this.running = false;
        this.phaseStep = 0.0;
    }

    public double phaseAt(double var1) {
        return this.previousPhase + this.phaseStep * Math.max(0.0, Math.min(1.0, var1));
    }

    public double weightAt(double var1) {
        return this.previousWeight + (this.weight - this.previousWeight) * Math.max(0.0, Math.min(1.0, var1));
    }
}
