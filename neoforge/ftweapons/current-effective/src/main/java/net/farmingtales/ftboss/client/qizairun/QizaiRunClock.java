package net.farmingtales.ftboss.client.qizairun;

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

    private QizaiRunClock.State reset(double var1, double var3, double var5, double var7) {
        this.lastTime = var1;
        this.lastX = var3;
        this.lastY = var5;
        this.lastZ = var7;
        this.speed = this.blend = this.phase = 0.0;
        this.running = false;
        this.forward = 1.0;
        this.side = 0.0;
        return new QizaiRunClock.State(0.0, 0.0, 0.0, 1.0, 0.0, true);
    }

    public QizaiRunClock.State update(double var1, double var3, double var5, double var7, boolean var9, double var10) {
        if (Double.isFinite(var1) && Double.isFinite(var3) && Double.isFinite(var5) && Double.isFinite(var7) && Double.isFinite(var10)) {
            if (Double.isFinite(this.lastTime) && !(var1 < this.lastTime) && !(var1 - this.lastTime > 6.0) && var9) {
                double var12 = var1 - this.lastTime;
                if (var12 <= 1.0E-8) {
                    return new QizaiRunClock.State(this.phase, this.speed, this.blend, this.forward, this.side, false);
                } else {
                    double var14 = var3 - this.lastX;
                    double var16 = var7 - this.lastZ;
                    double var18 = Math.hypot(var14, var16) / var12;
                    if (!(var18 > 1.6) && !(Math.abs(var5 - this.lastY) / var12 > 1.5)) {
                        double var20 = this.speed;
                        double var22 = 1.0 - Math.exp(-var12 / 2.5);
                        this.speed = this.speed + (var18 - this.speed) * var22;
                        double var24 = var18 * var12 + (var20 - var18) * 2.5 * var22;
                        double var26 = 0.33599999999999997;
                        if (Math.min(var20, this.speed) >= var26) {
                            var24 = var26 * var12;
                        } else if (Math.max(var20, this.speed) > var26) {
                            double var28 = QizaiRunPose.clamp(-2.5 * Math.log((var26 - var18) / (var20 - var18)), 0.0, var12);
                            double var30 = var18 * var28 + (var20 - var18) * 2.5 * (1.0 - Math.exp(-var28 / 2.5));
                            var24 = var20 < var26 ? var30 + var26 * (var12 - var28) : var26 * var28 + var24 - var30;
                        }

                        if (var18 > 0.005) {
                            double var32 = Math.toRadians(var10);
                            double var34 = Math.hypot(var14, var16);
                            this.forward = this.forward + ((-var14 * Math.sin(var32) + var16 * Math.cos(var32)) / var34 - this.forward) * var22;
                            this.side = this.side + ((var14 * Math.cos(var32) + var16 * Math.sin(var32)) / var34 - this.side) * var22;
                        }

                        if (this.running && this.speed < 0.17) {
                            this.running = false;
                        } else if (!this.running && this.speed >= 0.24) {
                            this.running = true;
                        }

                        double var33 = this.running ? QizaiRunPose.smooth((this.speed - 0.17) / 0.23) : 0.0;
                        this.blend = this.blend + (var33 - this.blend) * (1.0 - Math.exp(-var12 / (var33 > this.blend ? 3.2 : 2.2)));
                        this.phase = (this.phase + Math.max(0.0, var24) * Math.PI * 2.0 / 2.4) % (Math.PI * 2);
                        this.lastTime = var1;
                        this.lastX = var3;
                        this.lastY = var5;
                        this.lastZ = var7;
                        return new QizaiRunClock.State(this.phase, this.speed, this.blend, this.forward, this.side, false);
                    } else {
                        return this.reset(var1, var3, var5, var7);
                    }
                }
            } else {
                return this.reset(var1, var3, var5, var7);
            }
        } else {
            this.lastTime = Double.NaN;
            this.speed = this.blend = this.phase = 0.0;
            this.running = false;
            return new QizaiRunClock.State(0.0, 0.0, 0.0, 1.0, 0.0, true);
        }
    }

    public static record State(double phase, double speed, double blend, double forward, double side, boolean reset) {
    }
}
