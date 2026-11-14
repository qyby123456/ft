/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.manorrun;

import java.util.Arrays;

public final class RunGait {
    public static final double START_SPEED = 0.24;
    public static final double FULL_SPEED = 0.38;
    public static final double MAX_STEP = 2.5;
    public static final double STANCE = 0.44;
    private static final Profile[] PROFILES = new Profile[]{new Profile("\u67da\u5b50", 0.29, 1.04, 1.8, 0.38, 0.58, -0.48, 0.96), new Profile("\u86cb\u86cb", 0.26, 1.08, 2.1, 0.43, 0.67, -0.58, 1.0), new Profile("\u6735\u62c9", 0.12, 0.96, 1.5, 0.23, 0.36, -0.9, 1.03), new Profile("\u529f\u5e95", 0.34, 1.13, 1.8, 0.34, 0.52, -0.62, 0.92), new Profile("\u67e0\u6aac\u732b", 0.22, 0.95, 2.4, 0.63, 0.76, -0.46, 1.08), new Profile("\u9752\u8863", 0.28, 1.1, 2.0, 0.4, 0.7, -0.56, 1.0), new Profile("\u6587\u7384\u84dd", 0.17, 1.01, 1.8, 0.29, 0.45, -0.62, 1.02), new Profile("\u6298\u8033\u6839", 0.31, 1.09, 2.1, 0.31, 0.6, -0.5, 1.06), new Profile("\u7d2b\u83dc", 0.23, 0.99, 2.3, 0.51, 0.71, -0.49, 1.04), new Profile("\u9ebb\u96c0", 0.18, 0.93, 2.1, 0.5, 0.6, -0.55, 1.07), new Profile("\u96ea\u9ece", 0.25, 1.03, 1.9, 0.35, 0.62, -0.51, 1.01), new Profile("\u7941\u5d3d", 0.15, 0.96, 1.6, 0.24, 0.42, -0.92, 0.98)};

    private RunGait() {
    }

    public static Profile profile(int n) {
        return PROFILES[Math.max(0, Math.min(PROFILES.length - 1, n))];
    }

    public static int count() {
        return PROFILES.length;
    }

    public static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    public static double smooth(double d) {
        double d2 = RunGait.clamp(d);
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double fraction(double d) {
        return d - Math.floor(d);
    }

    public static double weight(double d) {
        return Double.isFinite(d) && d <= 2.5 ? RunGait.smooth((d - 0.24) / 0.14) : 0.0;
    }

    public static double cadence(Profile profile, double d) {
        return profile.cadence() * (2.25 + 1.15 * RunGait.smooth((d - 0.28) / 0.55));
    }

    public static double sole(double d, double d2, double d3) {
        double d4 = Math.sin(d3) * Math.cos(d2);
        double d5 = Math.sin(d3) * Math.sin(d2) * Math.sin(d) + Math.cos(d3) * Math.cos(d);
        double d6 = Math.sin(d3) * Math.sin(d2) * Math.cos(d) - Math.cos(d3) * Math.sin(d);
        return 12.0 * Math.max(0.0, d5) + 2.0 * Math.abs(d4) + 2.0 * Math.abs(d6);
    }

    public static double channelAllowance(double d, double d2) {
        return 1.0 - RunGait.smooth(Math.max(Math.abs(d) / 0.3, Math.abs(d2) / 1.25));
    }

    public static void orient(double[] dArray, double d, double d2) {
        double d3 = dArray[9] * d;
        double d4 = 12.0 * (1.0 - Math.cos(dArray[9])) - dArray[7];
        double d5 = -0.1 * d2;
        double d6 = 12.0 * (1.0 - Math.cos(d3) * Math.cos(d5)) - d4;
        double d7 = -12.0 * Math.sin(d3);
        dArray[0] = dArray[6] = 12.0 * Math.sin(d5);
        dArray[7] = d6;
        dArray[1] = d6 + 2.0 * Math.abs(Math.sin(d3)) + 2.0 * Math.abs(Math.sin(dArray[11] + d5));
        dArray[2] = dArray[8] = d7;
        dArray[5] = 0.0;
        dArray[9] = d3;
        dArray[11] = dArray[11] + d5;
        for (int i = 0; i < 2; ++i) {
            int n = (4 + i) * 6;
            int n2 = (2 + i) * 6;
            double d8 = 12.0 - RunGait.sole(dArray[n + 3], dArray[n + 4], dArray[n + 5]) - dArray[n + 1];
            double d9 = dArray[n + 3];
            int n3 = n + 3;
            dArray[n3] = dArray[n3] * d;
            int n4 = n + 5;
            dArray[n4] = dArray[n4] + d9 * d2 * 0.52;
            dArray[n + 1] = 12.0 - RunGait.sole(dArray[n + 3], dArray[n + 4], dArray[n + 5]) - d8;
            dArray[n2] = 9.5 * Math.sin(d5);
            dArray[n2 + 1] = 9.5 * (1.0 - Math.cos(d3) * Math.cos(d5)) - d4;
            dArray[n2 + 2] = -9.5 * Math.sin(d3);
            if (!(Math.abs(dArray[n2 + 4]) < 0.001)) continue;
            dArray[n2 + 3] = -0.18 + (dArray[n2 + 3] + 0.18) * d;
        }
    }

    public static void sample(int n, double d, double d2, boolean bl, boolean bl2, double[] dArray) {
        Arrays.fill(dArray, 0.0);
        Profile profile = RunGait.profile(n);
        double d3 = RunGait.fraction(d);
        double d4 = d3 * Math.PI * 2.0;
        double d5 = profile.lean() * (0.88 + 0.12 * RunGait.smooth((d2 - 0.3) / 0.5));
        double d6 = profile.bob() * (0.5 - 0.5 * Math.cos(d4 * 2.0));
        double d7 = 12.0 * (1.0 - Math.cos(d5)) - d6;
        double d8 = -12.0 * Math.sin(d5);
        dArray[1] = d7 + 2.0 * Math.abs(Math.sin(d5)) + 2.0 * Math.abs(Math.sin(0.018 * Math.sin(d4)));
        dArray[2] = d8;
        dArray[3] = 0.018 * Math.sin(d4 * 2.0);
        dArray[7] = d7;
        dArray[8] = d8;
        dArray[9] = d5;
        dArray[10] = 0.028 * Math.sin(d4);
        dArray[11] = 0.018 * Math.sin(d4);
        for (int i = 0; i < 2; ++i) {
            int n2 = (4 + i) * 6;
            int n3 = (2 + i) * 6;
            double d9 = RunGait.fraction(d3 + (double)i * 0.5);
            boolean bl3 = d9 < 0.44;
            double d10 = bl3 ? d9 / 0.44 : (d9 - 0.44) / 0.56;
            double d11 = profile.stride() * (0.89 + 0.11 * RunGait.smooth((d2 - 0.3) / 0.5));
            double d12 = bl3 ? -0.7 + 1.57 * RunGait.smooth(d10) : 0.87 - 1.57 * RunGait.smooth(d10);
            double d13 = bl3 ? 0.0 : profile.clearance() * Math.pow(Math.sin(d10 * Math.PI), 2.0);
            double d14 = (double)(i == 0 ? 1 : -1) * 0.024 * Math.sin(d4 * 2.0);
            dArray[n2 + 1] = 12.0 - RunGait.sole(d12 *= d11, 0.0, d14) - d13;
            dArray[n2 + 3] = d12;
            dArray[n2 + 5] = d14;
            dArray[n3 + 1] = 9.5 * (1.0 - Math.cos(d5)) - d6;
            dArray[n3 + 2] = -9.5 * Math.sin(d5);
            boolean bl4 = bl2 && bl == (i == 1);
            double d15 = dArray[n3 + 3] = bl4 ? profile.grip() + 0.035 * Math.sin(d4 * 2.0) : -0.18 - profile.arm() * d12;
            dArray[n3 + 4] = bl4 ? (i == 0 ? -0.12 : 0.12) : 0.0;
            dArray[n3 + 5] = (double)(i == 0 ? 1 : -1) * (bl4 ? 0.15 : 0.075);
        }
    }

    public record Profile(String name, double lean, double stride, double clearance, double bob, double arm, double grip, double cadence) {
    }

    public static final class Clock {
        public double phase;
        public double speed;
        public double blend;
        private double previousAge = Double.NaN;

        public void update(double d, double d2, boolean bl, double d3, int n) {
            if (!Double.isFinite(d) || !Double.isFinite(d2) || d2 < 0.0 || d2 > 2.5 || !bl) {
                this.speed = 0.0;
                this.blend = 0.0;
                this.previousAge = d;
                return;
            }
            if (!Double.isFinite(this.previousAge) || d < this.previousAge || d - this.previousAge > 20.0) {
                this.previousAge = d;
                this.speed = d2;
                this.phase = RunGait.fraction(d3 / (Math.PI * 2));
                this.blend = 0.0;
                return;
            }
            double d4 = Math.min(4.0, d - this.previousAge);
            this.previousAge = d;
            this.speed += (d2 - this.speed) * (1.0 - Math.exp(-d4 / 1.8));
            double d5 = d2 < 0.01 ? 0.0 : RunGait.weight(this.speed);
            this.blend += (d5 - this.blend) * (1.0 - Math.exp(-d4 / (d5 > this.blend ? 2.3 : 1.6)));
            if (this.blend < 0.001 && d5 == 0.0) {
                this.blend = 0.0;
                this.phase = RunGait.fraction(d3 / (Math.PI * 2));
            } else {
                this.phase = RunGait.fraction(this.phase + d4 * RunGait.cadence(RunGait.profile(n), this.speed) * RunGait.smooth(this.speed / 0.12) / 20.0);
            }
        }
    }
}

