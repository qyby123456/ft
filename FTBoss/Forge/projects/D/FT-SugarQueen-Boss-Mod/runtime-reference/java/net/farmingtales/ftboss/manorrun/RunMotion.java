/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.manorrun;

public final class RunMotion {
    public static final Profile[] PROFILES = new Profile[]{new Profile("youzi", "\u67da\u5b50", "\u7a33\u5065\u538b\u8eab\u8ffd\u51fb", 0.96, 0.25, 0.32, 0.64, -0.3, 0.065, 1.65, 2.7), new Profile("dandan", "\u86cb\u86cb", "\u524d\u503e\u730e\u624b\u5954\u88ad", 1.08, 0.34, 0.28, 0.82, -0.4, 0.085, 2.25, 2.85), new Profile("duola", "\u6735\u62c9", "\u6536\u81c2\u6218\u672f\u75be\u8dd1", 0.98, 0.22, 0.16, 0.22, -0.92, 0.035, 1.9, 2.55), new Profile("gongdi", "\u529f\u5e95", "\u6c89\u80a9\u91cd\u6b65\u51b2\u950b", 0.91, 0.3, 0.22, 0.58, -0.28, 0.05, 1.5, 2.95), new Profile("ningmengmao", "\u67e0\u6aac\u732b", "\u8f7b\u76c8\u9519\u80a9\u5feb\u8dd1", 1.0, 0.24, 0.46, 0.78, -0.34, 0.1, 2.45, 2.55), new Profile("qingyi", "\u9752\u8863", "\u957f\u5175\u4fa7\u6301\u75be\u884c", 1.04, 0.24, 0.2, 0.68, -0.38, 0.075, 2.0, 2.8), new Profile("wenxuanlan", "\u6587\u7384\u84dd", "\u7a33\u4f4f\u5668\u68b0\u6025\u8fdb", 0.96, 0.23, 0.2, 0.36, -0.65, 0.045, 1.75, 2.7), new Profile("zheergeng", "\u6298\u8033\u6839", "\u4f4e\u8eab\u6301\u5203\u63a0\u884c", 1.12, 0.38, 0.24, 0.84, -0.32, 0.105, 2.3, 2.9), new Profile("zicai", "\u7d2b\u83dc", "\u7d27\u51d1\u8fc5\u6b65\u7a81\u8fdb", 0.99, 0.25, 0.38, 0.67, -0.36, 0.08, 2.1, 2.6), new Profile("maque", "\u9ebb\u96c0", "\u8f7b\u6b65\u6536\u7ffc\u5954\u8dd1", 0.88, 0.18, 0.4, 0.57, -0.35, 0.07, 2.55, 2.35), new Profile("xueli", "\u96ea\u9ece", "\u5e73\u7a33\u6301\u68b0\u5feb\u8dd1", 0.95, 0.22, 0.28, 0.61, -0.38, 0.06, 1.9, 2.65), new Profile("qizai", "\u7941\u5d3d", "\u6301\u6756\u5c0f\u5e45\u6025\u884c", 0.84, 0.17, 0.18, 0.49, -0.48, 0.045, 1.6, 2.45)};

    private RunMotion() {
    }

    public static double clamp(double d, double d2, double d3) {
        return Double.isFinite(d) ? Math.max(d2, Math.min(d3, d)) : d2;
    }

    public static double smooth(double d) {
        double d2 = RunMotion.clamp(d, 0.0, 1.0);
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double target(double d) {
        return RunMotion.smooth((d - 0.105) / 0.115);
    }

    public static double cadence(Profile profile, double d) {
        return RunMotion.clamp(d * 20.0 / profile.cycle, 1.15, 3.3);
    }

    public static double[] rotate(double[] dArray, double[] dArray2) {
        double d = Math.cos(dArray2[0]);
        double d2 = Math.sin(dArray2[0]);
        double d3 = Math.cos(dArray2[1]);
        double d4 = Math.sin(dArray2[1]);
        double d5 = Math.cos(dArray2[2]);
        double d6 = Math.sin(dArray2[2]);
        double d7 = dArray[1] * d - dArray[2] * d2;
        double d8 = dArray[1] * d2 + dArray[2] * d;
        double d9 = dArray[0] * d3 + d8 * d4;
        double d10 = -dArray[0] * d4 + d8 * d3;
        return new double[]{d9 * d5 - d7 * d6, d9 * d6 + d7 * d5, d10};
    }

    public static double footHeight(double[] dArray) {
        double d = -1.7976931348623157E308;
        for (double d2 : new double[]{-2.0, 2.0}) {
            for (double d3 : new double[]{-2.0, 2.0}) {
                d = Math.max(d, RunMotion.rotate(new double[]{d2, 12.0, d3}, dArray)[1]);
            }
        }
        return d;
    }

    public static Pose sample(int n, double d, double d2, boolean bl, double d3, double d4) {
        int n2;
        Profile profile = PROFILES[Math.max(0, Math.min(PROFILES.length - 1, n))];
        d = Double.isFinite(d) ? d % (Math.PI * 2) : 0.0;
        d2 = RunMotion.clamp(d2, 0.0, 0.8);
        double d5 = RunMotion.clamp((d2 - 0.1) / 0.35, 0.0, 1.0);
        double d6 = Math.cos(d);
        double d7 = Math.sin(d * 2.0);
        double[][] dArray = new double[6][3];
        double[][] dArray2 = new double[6][3];
        double d8 = bl ? -1.0 : 1.0;
        dArray2[1] = new double[]{profile.lean * (0.78 + 0.22 * d5) + 0.015 * d7, profile.twist * Math.sin(d) * d8, 0.018 * d6 * d8};
        for (int i = 4; i <= 5; ++i) {
            double d9 = d + (i == 4 ? 0.0 : Math.PI);
            double d10 = i == 4 ? -1.0 : 1.0;
            dArray2[i] = new double[]{-profile.stride * (0.84 + 0.16 * d5) * Math.cos(d9) + 0.1 * Math.sin(d9 * 2.0), 0.025 * Math.sin(d9), -d10 * 0.035};
        }
        double d11 = Math.pow(Math.abs(d6), 8.0) * (0.42 + 0.4 * d5);
        double d12 = 12.0 - Math.max(RunMotion.footHeight(dArray2[4]), RunMotion.footHeight(dArray2[5])) - d11;
        double[] dArray3 = RunMotion.rotate(new double[]{0.0, -12.0, 0.0}, dArray2[1]);
        dArray[1] = new double[]{dArray3[0], 12.0 + d12 + dArray3[1], dArray3[2]};
        dArray[0] = (double[])dArray[1].clone();
        dArray2[0] = new double[]{Math.toRadians(RunMotion.clamp(d4, -70.0, 70.0)) - 0.42 * dArray2[1][0], Math.toRadians(RunMotion.clamp(d3, -80.0, 80.0)) - 0.25 * dArray2[1][1], -0.45 * dArray2[1][2]};
        for (n2 = 2; n2 <= 3; ++n2) {
            boolean bl2 = n2 == (bl ? 3 : 2);
            double d13 = n2 == 2 ? -1.0 : 1.0;
            double d14 = d + (n2 == 2 ? Math.PI : 0.0);
            double d15 = bl2 ? profile.mainSwing : profile.freeSwing;
            double[] dArray4 = RunMotion.rotate(new double[]{d13 * 5.0, 2.5, 0.0}, dArray2[1]);
            dArray[n2] = new double[]{dArray[1][0] + dArray4[0], dArray[1][1] + dArray4[1], dArray[1][2] + dArray4[2]};
            double d16 = bl2 ? profile.carry : (n == 2 ? -0.98 : (n == 6 ? -0.5 : -0.22));
            dArray2[n2] = new double[]{dArray2[1][0] + d16 - d15 * Math.cos(d14), dArray2[1][1] + d13 * (bl2 ? 0.07 : 0.04), dArray2[1][2] - d13 * (bl2 ? 0.16 : 0.12)};
        }
        for (n2 = 4; n2 <= 5; ++n2) {
            double d17 = n2 == 4 ? -1.0 : 1.0;
            double d18 = d + (n2 == 4 ? 0.0 : Math.PI);
            double d19 = profile.lift * Math.pow(Math.max(0.0, -Math.sin(d18)), 1.4);
            dArray[n2] = new double[]{d17 * 1.98, 12.0 + d12 - d19, -0.12 * d5};
        }
        return new Pose(dArray, dArray2, d, d2, 0.18 + 0.24 * d5 + 0.065 * d7);
    }

    public record Profile(String id, String title, String character, double stride, double lean, double mainSwing, double freeSwing, double carry, double twist, double lift, double cycle) {
    }

    public record Pose(double[][] position, double[][] rotation, double phase, double speed, double cape) {
    }
}

