package net.farmingtales.ftboss.client.manorrun;

public final class RunCycle {
    public static final double WALK_ENTER = 0.018;
    public static final double IDLE_ENTER = 0.009;
    public static final double RUN_ENTER = 0.18;
    public static final double RUN_EXIT = 0.145;
    public static final double FAST = 0.32;
    public static final double SPRINT = 0.48;
    private static final double TWO_PI = Math.PI * 2;

    public static RunCycle.Pose pose(int var0, double var1, double var3) {
        double var5 = clamp((var1 - 0.145) / 0.33499999999999996, 0.0, 1.0);
        double var7 = Math.sin(var3) * (0.48 + 0.24 * var5);
        double var9 = Math.sin(var3 - 0.2);
        double var11 = 0.07 + 0.035 * var5;
        double var13 = -0.24 - 0.12 * var9;
        double var15 = -0.18 + 0.35 * var9;
        double var17 = 0.035;
        switch (var0) {
            case 0:
                var13 = -0.34 - 0.065 * var9;
                var15 = -0.18 + 0.24 * var9;
                break;
            case 1:
                var13 = -0.36 - 0.07 * var9;
                var15 = -0.15 + 0.32 * var9;
                break;
            case 2:
                var13 = -0.5 - 0.035 * var9;
                var15 = -0.46 + 0.035 * var9;
                break;
            case 3:
                var13 = -0.28 - 0.045 * var9;
                var15 = -0.18 + 0.28 * var9;
                var17 = 0.015;
                break;
            case 4:
                var13 = -0.32 - 0.04 * var9;
                var15 = -0.3 + 0.055 * var9;
                break;
            case 5:
                var13 = -0.3 - 0.055 * var9;
                var15 = -0.2 + 0.3 * var9;
                break;
            case 6:
                var13 = -0.65 - 0.03 * var9;
                var15 = -0.62 + 0.03 * var9;
                break;
            case 7:
                var13 = -0.24 - 0.07 * var9;
                var15 = -0.17 + 0.32 * var9;
                break;
            case 8:
                var13 = -0.29 - 0.045 * var9;
                var15 = -0.19 + 0.26 * var9;
            case 9:
            default:
                break;
            case 10:
                var13 = -0.32 - 0.04 * var9;
                var15 = -0.3 + 0.055 * var9;
                break;
            case 11:
                var13 = -0.27 - 0.055 * var9;
                var15 = -0.17 + 0.26 * var9;
                break;
            case 12:
                var13 = -0.26 - 0.05 * var9;
                var15 = -0.18 + 0.3 * var9;
        }

        return new RunCycle.Pose(var7, var11, var13, var15, var17);
    }

    public static RunCycle.Pose settledPose(int var0, double var1, double var3) {
        RunCycle.Pose var5 = pose(var0, var1, var3);
        if (var1 >= 0.18) {
            return var5;
        } else {
            RunCycle.Pose var6 = pose(var0, 0.0, 0.0);
            double var7 = var1 >= 0.018 ? clamp(var1 / 0.145, 0.0, 1.0) : 0.0;
            return new RunCycle.Pose(
                var7 * 0.4 * Math.sin(var3),
                0.0,
                var6.rightArm() + (var5.rightArm() - var6.rightArm()) * var7 * 0.65,
                var6.leftArm() + (var5.leftArm() - var6.leftArm()) * var7 * 0.65,
                var6.roll()
            );
        }
    }

    public static double clamp(double var0, double var2, double var4) {
        return Math.max(var2, Math.min(var4, var0));
    }

    private RunCycle() {
    }

    public static record Pose(double leg, double lean, double rightArm, double leftArm, double roll) {
    }

    public static final class State {
        public int tick = Integer.MIN_VALUE;
        public double lastX;
        public double lastZ;
        public double speed;
        public double phase;
        public double previousPhase;
        public double weight;
        public double previousWeight;
        public double travelSign = 1.0;
        public int mode;
        private int enterTicks;
        private int exitTicks;
        private int walkTicks;
        private int idleTicks;

        public void update(int var1, double var2, double var4, boolean var6, double var7) {
            if (var1 != this.tick) {
                int var9 = var1 - this.tick;
                if (this.tick != Integer.MIN_VALUE && var9 >= 1 && var9 <= 5 && Double.isFinite(var2) && Double.isFinite(var4)) {
                    double var10 = Math.hypot(var2 - this.lastX, var4 - this.lastZ);
                    this.previousPhase = this.phase;
                    this.previousWeight = this.weight;
                    if (var10 > 1.5 * (double)var9) {
                        this.reset(var1, var2, var4);
                    } else {
                        this.speed = this.speed + (var10 / (double)var9 - this.speed) * (1.0 - Math.pow(0.55, (double)var9));
                        if (var6) {
                            this.enterTicks = this.exitTicks = this.walkTicks = this.idleTicks = 0;
                            this.mode = 0;
                            this.weight = this.previousWeight = 0.0;
                        } else {
                            this.enterTicks = this.speed >= 0.18 ? this.enterTicks + var9 : 0;
                            this.exitTicks = this.speed <= 0.145 ? this.exitTicks + var9 : 0;
                            this.walkTicks = this.speed >= 0.018 ? this.walkTicks + var9 : 0;
                            this.idleTicks = this.speed <= 0.009 ? this.idleTicks + var9 : 0;
                            if (this.mode != 2 && this.enterTicks >= 3) {
                                this.mode = 2;
                            } else if (this.mode == 2 && this.exitTicks >= 4) {
                                this.mode = this.speed >= 0.018 ? 1 : 0;
                            } else if (this.mode == 0 && this.walkTicks >= 3) {
                                this.mode = 1;
                            } else if (this.mode == 1 && this.idleTicks >= 4) {
                                this.mode = 0;
                            }

                            double var12 = this.mode == 2 ? 1.0 : 0.0;
                            this.weight = this.weight + (var12 - this.weight) * (1.0 - Math.pow(var12 > this.weight ? 0.72 : 0.76, (double)var9));
                        }

                        if (var7 < -var10 * 0.35) {
                            this.travelSign = -1.0;
                        } else if (var7 > var10 * 0.35) {
                            this.travelSign = 1.0;
                        }

                        this.phase = this.phase + this.travelSign * Math.min(var10 * (Math.PI * 2) / 2.8, 0.68 * (double)var9);
                        if (Math.abs(this.phase) > 10000.0) {
                            double var14 = Math.floor(this.phase / (Math.PI * 2)) * (Math.PI * 2);
                            this.phase -= var14;
                            this.previousPhase -= var14;
                        }

                        this.tick = var1;
                        this.lastX = var2;
                        this.lastZ = var4;
                    }
                } else {
                    this.reset(var1, var2, var4);
                }
            }
        }

        private void reset(int var1, double var2, double var4) {
            this.tick = var1;
            this.lastX = Double.isFinite(var2) ? var2 : 0.0;
            this.lastZ = Double.isFinite(var4) ? var4 : 0.0;
            this.speed = this.weight = this.previousWeight = this.phase = this.previousPhase = 0.0;
            this.mode = this.enterTicks = this.exitTicks = this.walkTicks = this.idleTicks = 0;
            this.travelSign = 1.0;
        }
    }
}
