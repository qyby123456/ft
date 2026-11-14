/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.manorrun;

public final class RunCycle {
    public static final double WALK_ENTER = 0.018;
    public static final double IDLE_ENTER = 0.009;
    public static final double RUN_ENTER = 0.18;
    public static final double RUN_EXIT = 0.145;
    public static final double FAST = 0.32;
    public static final double SPRINT = 0.48;
    private static final double TWO_PI = Math.PI * 2;

    public static Pose pose(int n, double d, double d2) {
        double d3 = RunCycle.clamp((d - 0.145) / 0.33499999999999996, 0.0, 1.0);
        double d4 = Math.sin(d2) * (0.48 + 0.24 * d3);
        double d5 = Math.sin(d2 - 0.2);
        double d6 = 0.07 + 0.035 * d3;
        double d7 = -0.24 - 0.12 * d5;
        double d8 = -0.18 + 0.35 * d5;
        double d9 = 0.035;
        switch (n) {
            case 0: {
                d7 = -0.34 - 0.065 * d5;
                d8 = -0.18 + 0.24 * d5;
                break;
            }
            case 1: {
                d7 = -0.36 - 0.07 * d5;
                d8 = -0.15 + 0.32 * d5;
                break;
            }
            case 2: {
                d7 = -0.5 - 0.035 * d5;
                d8 = -0.46 + 0.035 * d5;
                break;
            }
            case 3: {
                d7 = -0.28 - 0.045 * d5;
                d8 = -0.18 + 0.28 * d5;
                d9 = 0.015;
                break;
            }
            case 4: {
                d7 = -0.32 - 0.04 * d5;
                d8 = -0.3 + 0.055 * d5;
                break;
            }
            case 5: {
                d7 = -0.3 - 0.055 * d5;
                d8 = -0.2 + 0.3 * d5;
                break;
            }
            case 6: {
                d7 = -0.65 - 0.03 * d5;
                d8 = -0.62 + 0.03 * d5;
                break;
            }
            case 7: {
                d7 = -0.24 - 0.07 * d5;
                d8 = -0.17 + 0.32 * d5;
                break;
            }
            case 8: {
                d7 = -0.29 - 0.045 * d5;
                d8 = -0.19 + 0.26 * d5;
                break;
            }
            case 10: {
                d7 = -0.32 - 0.04 * d5;
                d8 = -0.3 + 0.055 * d5;
                break;
            }
            case 11: {
                d7 = -0.27 - 0.055 * d5;
                d8 = -0.17 + 0.26 * d5;
                break;
            }
            case 12: {
                d7 = -0.26 - 0.05 * d5;
                d8 = -0.18 + 0.3 * d5;
                break;
            }
        }
        return new Pose(d4, d6, d7, d8, d9);
    }

    public static Pose settledPose(int n, double d, double d2) {
        Pose pose = RunCycle.pose(n, d, d2);
        if (d >= 0.18) {
            return pose;
        }
        Pose pose2 = RunCycle.pose(n, 0.0, 0.0);
        double d3 = d >= 0.018 ? RunCycle.clamp(d / 0.145, 0.0, 1.0) : 0.0;
        return new Pose(d3 * 0.4 * Math.sin(d2), 0.0, pose2.rightArm() + (pose.rightArm() - pose2.rightArm()) * d3 * 0.65, pose2.leftArm() + (pose.leftArm() - pose2.leftArm()) * d3 * 0.65, pose2.roll());
    }

    public static double clamp(double d, double d2, double d3) {
        return Math.max(d2, Math.min(d3, d));
    }

    private RunCycle() {
    }

    public record Pose(double leg, double lean, double rightArm, double leftArm, double roll) {
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

        public void update(int n, double d, double d2, boolean bl, double d3) {
            double d4;
            if (n == this.tick) {
                return;
            }
            int n2 = n - this.tick;
            if (this.tick == Integer.MIN_VALUE || n2 < 1 || n2 > 5 || !Double.isFinite(d) || !Double.isFinite(d2)) {
                this.reset(n, d, d2);
                return;
            }
            double d5 = Math.hypot(d - this.lastX, d2 - this.lastZ);
            this.previousPhase = this.phase;
            this.previousWeight = this.weight;
            if (d5 > 1.5 * (double)n2) {
                this.reset(n, d, d2);
                return;
            }
            this.speed += (d5 / (double)n2 - this.speed) * (1.0 - Math.pow(0.55, n2));
            if (bl) {
                this.idleTicks = 0;
                this.walkTicks = 0;
                this.exitTicks = 0;
                this.enterTicks = 0;
                this.mode = 0;
                this.previousWeight = 0.0;
                this.weight = 0.0;
            } else {
                this.enterTicks = this.speed >= 0.18 ? this.enterTicks + n2 : 0;
                this.exitTicks = this.speed <= 0.145 ? this.exitTicks + n2 : 0;
                this.walkTicks = this.speed >= 0.018 ? this.walkTicks + n2 : 0;
                int n3 = this.idleTicks = this.speed <= 0.009 ? this.idleTicks + n2 : 0;
                if (this.mode != 2 && this.enterTicks >= 3) {
                    this.mode = 2;
                } else if (this.mode == 2 && this.exitTicks >= 4) {
                    this.mode = this.speed >= 0.018 ? 1 : 0;
                } else if (this.mode == 0 && this.walkTicks >= 3) {
                    this.mode = 1;
                } else if (this.mode == 1 && this.idleTicks >= 4) {
                    this.mode = 0;
                }
                d4 = this.mode == 2 ? 1.0 : 0.0;
                this.weight += (d4 - this.weight) * (1.0 - Math.pow(d4 > this.weight ? 0.72 : 0.76, n2));
            }
            if (d3 < -d5 * 0.35) {
                this.travelSign = -1.0;
            } else if (d3 > d5 * 0.35) {
                this.travelSign = 1.0;
            }
            this.phase += this.travelSign * Math.min(d5 * (Math.PI * 2) / 2.8, 0.68 * (double)n2);
            if (Math.abs(this.phase) > 10000.0) {
                d4 = Math.floor(this.phase / (Math.PI * 2)) * (Math.PI * 2);
                this.phase -= d4;
                this.previousPhase -= d4;
            }
            this.tick = n;
            this.lastX = d;
            this.lastZ = d2;
        }

        private void reset(int n, double d, double d2) {
            this.tick = n;
            this.lastX = Double.isFinite(d) ? d : 0.0;
            this.lastZ = Double.isFinite(d2) ? d2 : 0.0;
            this.previousPhase = 0.0;
            this.phase = 0.0;
            this.previousWeight = 0.0;
            this.weight = 0.0;
            this.speed = 0.0;
            this.idleTicks = 0;
            this.walkTicks = 0;
            this.exitTicks = 0;
            this.enterTicks = 0;
            this.mode = 0;
            this.travelSign = 1.0;
        }
    }
}

