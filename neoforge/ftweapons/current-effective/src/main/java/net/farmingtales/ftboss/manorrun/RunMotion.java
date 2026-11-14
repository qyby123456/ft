package net.farmingtales.ftboss.manorrun;

public final class RunMotion {
    public static final RunMotion.Profile[] PROFILES = new RunMotion.Profile[]{
        new RunMotion.Profile("youzi", "柚子", "稳健压身追击", 0.96, 0.25, 0.32, 0.64, -0.3, 0.065, 1.65, 2.7),
        new RunMotion.Profile("dandan", "蛋蛋", "前倾猎手奔袭", 1.08, 0.34, 0.28, 0.82, -0.4, 0.085, 2.25, 2.85),
        new RunMotion.Profile("duola", "朵拉", "收臂战术疾跑", 0.98, 0.22, 0.16, 0.22, -0.92, 0.035, 1.9, 2.55),
        new RunMotion.Profile("gongdi", "功底", "沉肩重步冲锋", 0.91, 0.3, 0.22, 0.58, -0.28, 0.05, 1.5, 2.95),
        new RunMotion.Profile("ningmengmao", "柠檬猫", "轻盈错肩快跑", 1.0, 0.24, 0.46, 0.78, -0.34, 0.1, 2.45, 2.55),
        new RunMotion.Profile("qingyi", "青衣", "长兵侧持疾行", 1.04, 0.24, 0.2, 0.68, -0.38, 0.075, 2.0, 2.8),
        new RunMotion.Profile("wenxuanlan", "文玄蓝", "稳住器械急进", 0.96, 0.23, 0.2, 0.36, -0.65, 0.045, 1.75, 2.7),
        new RunMotion.Profile("zheergeng", "折耳根", "低身持刃掠行", 1.12, 0.38, 0.24, 0.84, -0.32, 0.105, 2.3, 2.9),
        new RunMotion.Profile("zicai", "紫菜", "紧凑迅步突进", 0.99, 0.25, 0.38, 0.67, -0.36, 0.08, 2.1, 2.6),
        new RunMotion.Profile("maque", "麻雀", "轻步收翼奔跑", 0.88, 0.18, 0.4, 0.57, -0.35, 0.07, 2.55, 2.35),
        new RunMotion.Profile("xueli", "雪黎", "平稳持械快跑", 0.95, 0.22, 0.28, 0.61, -0.38, 0.06, 1.9, 2.65),
        new RunMotion.Profile("qizai", "祁崽", "持杖小幅急行", 0.84, 0.17, 0.18, 0.49, -0.48, 0.045, 1.6, 2.45)
    };

    private RunMotion() {
    }

    public static double clamp(double var0, double var2, double var4) {
        return Double.isFinite(var0) ? Math.max(var2, Math.min(var4, var0)) : var2;
    }

    public static double smooth(double var0) {
        double var2 = clamp(var0, 0.0, 1.0);
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static double target(double var0) {
        return smooth((var0 - 0.105) / 0.115);
    }

    public static double cadence(RunMotion.Profile var0, double var1) {
        return clamp(var1 * 20.0 / var0.cycle, 1.15, 3.3);
    }

    public static double[] rotate(double[] var0, double[] var1) {
        double var2 = Math.cos(var1[0]);
        double var4 = Math.sin(var1[0]);
        double var6 = Math.cos(var1[1]);
        double var8 = Math.sin(var1[1]);
        double var10 = Math.cos(var1[2]);
        double var12 = Math.sin(var1[2]);
        double var14 = var0[1] * var2 - var0[2] * var4;
        double var16 = var0[1] * var4 + var0[2] * var2;
        double var18 = var0[0] * var6 + var16 * var8;
        double var20 = -var0[0] * var8 + var16 * var6;
        return new double[]{var18 * var10 - var14 * var12, var18 * var12 + var14 * var10, var20};
    }

    public static double footHeight(double[] var0) {
        double var1 = -Double.MAX_VALUE;

        for (double var6 : new double[]{-2.0, 2.0}) {
            for (double var11 : new double[]{-2.0, 2.0}) {
                var1 = Math.max(var1, rotate(new double[]{var6, 12.0, var11}, var0)[1]);
            }
        }

        return var1;
    }

    public static RunMotion.Pose sample(int var0, double var1, double var3, boolean var5, double var6, double var8) {
        RunMotion.Profile var10 = PROFILES[Math.max(0, Math.min(PROFILES.length - 1, var0))];
        var1 = Double.isFinite(var1) ? var1 % (Math.PI * 2) : 0.0;
        var3 = clamp(var3, 0.0, 0.8);
        double var11 = clamp((var3 - 0.1) / 0.35, 0.0, 1.0);
        double var13 = Math.cos(var1);
        double var15 = Math.sin(var1 * 2.0);
        double[][] var17 = new double[6][3];
        double[][] var18 = new double[6][3];
        double var19 = var5 ? -1.0 : 1.0;
        var18[1] = new double[]{var10.lean * (0.78 + 0.22 * var11) + 0.015 * var15, var10.twist * Math.sin(var1) * var19, 0.018 * var13 * var19};

        for (int var21 = 4; var21 <= 5; var21++) {
            double var22 = var1 + (var21 == 4 ? 0.0 : Math.PI);
            double var24 = var21 == 4 ? -1.0 : 1.0;
            var18[var21] = new double[]{
                -var10.stride * (0.84 + 0.16 * var11) * Math.cos(var22) + 0.1 * Math.sin(var22 * 2.0), 0.025 * Math.sin(var22), -var24 * 0.035
            };
        }

        double var39 = Math.pow(Math.abs(var13), 8.0) * (0.42 + 0.4 * var11);
        double var23 = 12.0 - Math.max(footHeight(var18[4]), footHeight(var18[5])) - var39;
        double[] var25 = rotate(new double[]{0.0, -12.0, 0.0}, var18[1]);
        var17[1] = new double[]{var25[0], 12.0 + var23 + var25[1], var25[2]};
        var17[0] = (double[])var17[1].clone();
        var18[0] = new double[]{
            Math.toRadians(clamp(var8, -70.0, 70.0)) - 0.42 * var18[1][0], Math.toRadians(clamp(var6, -80.0, 80.0)) - 0.25 * var18[1][1], -0.45 * var18[1][2]
        };

        for (int var26 = 2; var26 <= 3; var26++) {
            boolean var27 = var26 == (var5 ? 3 : 2);
            double var28 = var26 == 2 ? -1.0 : 1.0;
            double var30 = var1 + (var26 == 2 ? Math.PI : 0.0);
            double var32 = var27 ? var10.mainSwing : var10.freeSwing;
            double[] var34 = rotate(new double[]{var28 * 5.0, 2.5, 0.0}, var18[1]);
            var17[var26] = new double[]{var17[1][0] + var34[0], var17[1][1] + var34[1], var17[1][2] + var34[2]};
            double var35 = var27 ? var10.carry : (var0 == 2 ? -0.98 : (var0 == 6 ? -0.5 : -0.22));
            var18[var26] = new double[]{
                var18[1][0] + var35 - var32 * Math.cos(var30), var18[1][1] + var28 * (var27 ? 0.07 : 0.04), var18[1][2] - var28 * (var27 ? 0.16 : 0.12)
            };
        }

        for (int var40 = 4; var40 <= 5; var40++) {
            double var41 = var40 == 4 ? -1.0 : 1.0;
            double var29 = var1 + (var40 == 4 ? 0.0 : Math.PI);
            double var31 = var10.lift * Math.pow(Math.max(0.0, -Math.sin(var29)), 1.4);
            var17[var40] = new double[]{var41 * 1.98, 12.0 + var23 - var31, -0.12 * var11};
        }

        return new RunMotion.Pose(var17, var18, var1, var3, 0.18 + 0.24 * var11 + 0.065 * var15);
    }

    public static record Pose(double[][] position, double[][] rotation, double phase, double speed, double cape) {
    }

    public static record Profile(
        String id,
        String title,
        String character,
        double stride,
        double lean,
        double mainSwing,
        double freeSwing,
        double carry,
        double twist,
        double lift,
        double cycle
    ) {
    }
}
