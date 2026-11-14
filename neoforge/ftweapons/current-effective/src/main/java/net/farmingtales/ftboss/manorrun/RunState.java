package net.farmingtales.ftboss.manorrun;

public final class RunState {
    private double lastClock = Double.NaN;
    private double lastX;
    private double lastZ;
    private double speed;
    private double blend;
    private double phase;
    private int lastTick;
    private boolean wasAllowed;

    public RunState.Frame advance(int var1, int var2, double var3, double var5, double var7, boolean var9, boolean var10) {
        if (Double.isFinite(var3) && Double.isFinite(var5) && Double.isFinite(var7)) {
            if (Double.isFinite(this.lastClock) && !(var3 < this.lastClock) && !(var3 - this.lastClock > 5.0) && var2 >= this.lastTick) {
                boolean var11 = false;
                if (var2 != this.lastTick) {
                    double var12 = (double)(var2 - this.lastTick);
                    double var14 = this.wasAllowed ? RunMotion.target(this.speed) : 0.0;
                    this.blend = approach(this.blend, var14, var12);
                    if (this.speed > 0.025) {
                        this.phase = (
                                this.phase + var12 * RunMotion.cadence(RunMotion.PROFILES[Math.max(0, Math.min(11, var1))], this.speed) * Math.PI * 2.0 / 20.0
                            )
                            % (Math.PI * 2);
                    }

                    double var16 = Math.hypot(var5 - this.lastX, var7 - this.lastZ) / Math.max(1.0, var12);
                    if (var16 > 0.85) {
                        this.speed = 0.0;
                        this.blend = 0.0;
                        var11 = true;
                    } else {
                        this.speed = var16;
                    }

                    this.lastX = var5;
                    this.lastZ = var7;
                    this.lastTick = var2;
                }

                double var18 = var9 && !var10 ? RunMotion.target(this.speed) : 0.0;
                if (var10 || !var9) {
                    this.blend = 0.0;
                }

                double var19 = RunMotion.clamp(var3 - (double)var2, 0.0, 1.0);
                double var20 = this.phase;
                if (this.speed > 0.025 && !var11) {
                    var20 = (this.phase + var19 * RunMotion.cadence(RunMotion.PROFILES[Math.max(0, Math.min(11, var1))], this.speed) * Math.PI * 2.0 / 20.0)
                        % (Math.PI * 2);
                }

                this.wasAllowed = var9 && !var10;
                this.lastClock = var3;
                return new RunState.Frame(var20, RunMotion.clamp(approach(this.blend, var18, var19), 0.0, 1.0), this.speed, var11);
            } else {
                this.reset(var2, var3, var5, var7);
                return new RunState.Frame(this.phase, 0.0, 0.0, true);
            }
        } else {
            return new RunState.Frame(this.phase, 0.0, 0.0, true);
        }
    }

    private static double approach(double var0, double var2, double var4) {
        return var0 + (var2 - var0) * (1.0 - Math.exp(-var4 / (var2 > var0 ? 2.8 : 2.0)));
    }

    private void reset(int var1, double var2, double var4, double var6) {
        this.lastTick = var1;
        this.lastClock = var2;
        this.lastX = var4;
        this.lastZ = var6;
        this.speed = 0.0;
        this.blend = 0.0;
        this.phase = 0.0;
        this.wasAllowed = false;
    }

    public static record Frame(double phase, double blend, double speed, boolean discontinuity) {
    }
}
