package net.farmingtales.ftboss.client.manorrun;

import java.util.Arrays;

public final class RunGait {
    public static final double START_SPEED = 0.24;
    public static final double FULL_SPEED = 0.38;
    public static final double MAX_STEP = 2.5;
    public static final double STANCE = 0.44;
    private static final RunGait.Profile[] PROFILES = new RunGait.Profile[]{
        new RunGait.Profile("柚子", 0.29, 1.04, 1.8, 0.38, 0.58, -0.48, 0.96),
        new RunGait.Profile("蛋蛋", 0.26, 1.08, 2.1, 0.43, 0.67, -0.58, 1.0),
        new RunGait.Profile("朵拉", 0.12, 0.96, 1.5, 0.23, 0.36, -0.9, 1.03),
        new RunGait.Profile("功底", 0.34, 1.13, 1.8, 0.34, 0.52, -0.62, 0.92),
        new RunGait.Profile("柠檬猫", 0.22, 0.95, 2.4, 0.63, 0.76, -0.46, 1.08),
        new RunGait.Profile("青衣", 0.28, 1.1, 2.0, 0.4, 0.7, -0.56, 1.0),
        new RunGait.Profile("文玄蓝", 0.17, 1.01, 1.8, 0.29, 0.45, -0.62, 1.02),
        new RunGait.Profile("折耳根", 0.31, 1.09, 2.1, 0.31, 0.6, -0.5, 1.06),
        new RunGait.Profile("紫菜", 0.23, 0.99, 2.3, 0.51, 0.71, -0.49, 1.04),
        new RunGait.Profile("麻雀", 0.18, 0.93, 2.1, 0.5, 0.6, -0.55, 1.07),
        new RunGait.Profile("雪黎", 0.25, 1.03, 1.9, 0.35, 0.62, -0.51, 1.01),
        new RunGait.Profile("祁崽", 0.15, 0.96, 1.6, 0.24, 0.42, -0.92, 0.98)
    };

    private RunGait() {
    }

    public static RunGait.Profile profile(int var0) {
        return PROFILES[Math.max(0, Math.min(PROFILES.length - 1, var0))];
    }

    public static int count() {
        return PROFILES.length;
    }

    public static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    public static double smooth(double var0) {
        double var2 = clamp(var0);
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static double fraction(double var0) {
        return var0 - Math.floor(var0);
    }

    public static double weight(double var0) {
        return Double.isFinite(var0) && var0 <= 2.5 ? smooth((var0 - 0.24) / 0.14) : 0.0;
    }

    public static double cadence(RunGait.Profile var0, double var1) {
        return var0.cadence() * (2.25 + 1.15 * smooth((var1 - 0.28) / 0.55));
    }

    public static double sole(double var0, double var2, double var4) {
        double var6 = Math.sin(var4) * Math.cos(var2);
        double var8 = Math.sin(var4) * Math.sin(var2) * Math.sin(var0) + Math.cos(var4) * Math.cos(var0);
        double var10 = Math.sin(var4) * Math.sin(var2) * Math.cos(var0) - Math.cos(var4) * Math.sin(var0);
        return 12.0 * Math.max(0.0, var8) + 2.0 * Math.abs(var6) + 2.0 * Math.abs(var10);
    }

    public static double channelAllowance(double var0, double var2) {
        return 1.0 - smooth(Math.max(Math.abs(var0) / 0.3, Math.abs(var2) / 1.25));
    }

    public static void orient(double[] var0, double var1, double var3) {
        double var5 = var0[9] * var1;
        double var7 = 12.0 * (1.0 - Math.cos(var0[9])) - var0[7];
        double var9 = -0.1 * var3;
        double var11 = 12.0 * (1.0 - Math.cos(var5) * Math.cos(var9)) - var7;
        double var13 = -12.0 * Math.sin(var5);
        var0[0] = var0[6] = 12.0 * Math.sin(var9);
        var0[7] = var11;
        var0[1] = var11 + 2.0 * Math.abs(Math.sin(var5)) + 2.0 * Math.abs(Math.sin(var0[11] + var9));
        var0[2] = var0[8] = var13;
        var0[5] = 0.0;
        var0[9] = var5;
        var0[11] += var9;

        for (int var15 = 0; var15 < 2; var15++) {
            int var16 = (4 + var15) * 6;
            int var17 = (2 + var15) * 6;
            double var18 = 12.0 - sole(var0[var16 + 3], var0[var16 + 4], var0[var16 + 5]) - var0[var16 + 1];
            double var20 = var0[var16 + 3];
            var0[var16 + 3] = var0[var16 + 3] * var1;
            var0[var16 + 5] = var0[var16 + 5] + var20 * var3 * 0.52;
            var0[var16 + 1] = 12.0 - sole(var0[var16 + 3], var0[var16 + 4], var0[var16 + 5]) - var18;
            var0[var17] = 9.5 * Math.sin(var9);
            var0[var17 + 1] = 9.5 * (1.0 - Math.cos(var5) * Math.cos(var9)) - var7;
            var0[var17 + 2] = -9.5 * Math.sin(var5);
            if (Math.abs(var0[var17 + 4]) < 0.001) {
                var0[var17 + 3] = -0.18 + (var0[var17 + 3] + 0.18) * var1;
            }
        }
    }

    public static void sample(int var0, double var1, double var3, boolean var5, boolean var6, double[] var7) {
        Arrays.fill(var7, 0.0);
        RunGait.Profile var8 = profile(var0);
        double var9 = fraction(var1);
        double var11 = var9 * Math.PI * 2.0;
        double var13 = var8.lean() * (0.88 + 0.12 * smooth((var3 - 0.3) / 0.5));
        double var15 = var8.bob() * (0.5 - 0.5 * Math.cos(var11 * 2.0));
        double var17 = 12.0 * (1.0 - Math.cos(var13)) - var15;
        double var19 = -12.0 * Math.sin(var13);
        var7[1] = var17 + 2.0 * Math.abs(Math.sin(var13)) + 2.0 * Math.abs(Math.sin(0.018 * Math.sin(var11)));
        var7[2] = var19;
        var7[3] = 0.018 * Math.sin(var11 * 2.0);
        var7[7] = var17;
        var7[8] = var19;
        var7[9] = var13;
        var7[10] = 0.028 * Math.sin(var11);
        var7[11] = 0.018 * Math.sin(var11);

        for (int var21 = 0; var21 < 2; var21++) {
            int var22 = (4 + var21) * 6;
            int var23 = (2 + var21) * 6;
            double var24 = fraction(var9 + (double)var21 * 0.5);
            boolean var26 = var24 < 0.44;
            double var27 = var26 ? var24 / 0.44 : (var24 - 0.44) / 0.56;
            double var29 = var8.stride() * (0.89 + 0.11 * smooth((var3 - 0.3) / 0.5));
            double var31 = var26 ? -0.7 + 1.57 * smooth(var27) : 0.87 - 1.57 * smooth(var27);
            var31 *= var29;
            double var33 = var26 ? 0.0 : var8.clearance() * Math.pow(Math.sin(var27 * Math.PI), 2.0);
            double var35 = (double)(var21 == 0 ? 1 : -1) * 0.024 * Math.sin(var11 * 2.0);
            var7[var22 + 1] = 12.0 - sole(var31, 0.0, var35) - var33;
            var7[var22 + 3] = var31;
            var7[var22 + 5] = var35;
            var7[var23 + 1] = 9.5 * (1.0 - Math.cos(var13)) - var15;
            var7[var23 + 2] = -9.5 * Math.sin(var13);
            boolean var37 = var6 && var5 == (var21 == 1);
            var7[var23 + 3] = var37 ? var8.grip() + 0.035 * Math.sin(var11 * 2.0) : -0.18 - var8.arm() * var31;
            var7[var23 + 4] = var37 ? (var21 == 0 ? -0.12 : 0.12) : 0.0;
            var7[var23 + 5] = (double)(var21 == 0 ? 1 : -1) * (var37 ? 0.15 : 0.075);
        }
    }

    public static final class Clock {
        public double phase;
        public double speed;
        public double blend;
        private double previousAge = Double.NaN;

        public void update(double var1, double var3, boolean var5, double var6, int var8) {
            if (Double.isFinite(var1) && Double.isFinite(var3) && !(var3 < 0.0) && !(var3 > 2.5) && var5) {
                if (Double.isFinite(this.previousAge) && !(var1 < this.previousAge) && !(var1 - this.previousAge > 20.0)) {
                    double var9 = Math.min(4.0, var1 - this.previousAge);
                    this.previousAge = var1;
                    this.speed = this.speed + (var3 - this.speed) * (1.0 - Math.exp(-var9 / 1.8));
                    double var11 = var3 < 0.01 ? 0.0 : RunGait.weight(this.speed);
                    this.blend = this.blend + (var11 - this.blend) * (1.0 - Math.exp(-var9 / (var11 > this.blend ? 2.3 : 1.6)));
                    if (this.blend < 0.001 && var11 == 0.0) {
                        this.blend = 0.0;
                        this.phase = RunGait.fraction(var6 / (Math.PI * 2));
                    } else {
                        this.phase = RunGait.fraction(
                            this.phase + var9 * RunGait.cadence(RunGait.profile(var8), this.speed) * RunGait.smooth(this.speed / 0.12) / 20.0
                        );
                    }
                } else {
                    this.previousAge = var1;
                    this.speed = var3;
                    this.phase = RunGait.fraction(var6 / (Math.PI * 2));
                    this.blend = 0.0;
                }
            } else {
                this.blend = this.speed = 0.0;
                this.previousAge = var1;
            }
        }
    }

    public static record Profile(String name, double lean, double stride, double clearance, double bob, double arm, double grip, double cadence) {
    }
}
