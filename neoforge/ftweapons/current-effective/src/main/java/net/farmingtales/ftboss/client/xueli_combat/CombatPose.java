package net.farmingtales.ftboss.client.xueli_combat;

public final class CombatPose {
    private CombatPose() {
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    private static double smooth(double var0) {
        double var2 = clamp(var0);
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static CombatPose.State sample(String var0, double var1) {
        String[] var3 = var0.split(":");
        String var4 = var3[0];
        int var5 = var3.length > 1 ? Integer.parseInt(var3[1]) : 0;
        double[] var6 = new double[]{0.0, 0.0, 0.0};
        double[] var7 = new double[]{0.0, 0.0, 0.0};
        double var8 = 1.0;
        double var10 = 1.0;
        switch (var4) {
            case "guard":
                var10 = 0.68 + 0.1 * Math.sin(var1 * 2.6 + (double)var5 * 1.7);
                var6[1] = 0.016 * Math.sin(var1 * 2.0 + (double)var5);
                break;
            case "guard_mote":
                var6[1] = 0.12 * Math.sin(var1 * 2.0 + (double)var5);
                var10 = 0.5 + 0.25 * Math.sin(var1 * 3.0 + (double)var5);
                break;
            case "thread":
                var10 = 0.62 + 0.17 * Math.sin(var1 * 4.0 + (double)var5 * 0.4);
                break;
            case "thread_flow":
                var6[2] = (var1 * 0.6 + (double)var5 * 0.23) % 0.88;
                var10 = 0.6 * Math.sin(Math.PI * var6[2] / 0.88);
                break;
            case "break":
                double var22 = clamp(var1 / 0.8);
                var8 = 1.0 + var22 * 0.52;
                var6[1] = -0.25 * var22 * var22;
                var7[1] = (double)var5 * var22 * 5.0;
                var10 = 1.0 - smooth(var1 / 0.8);
                break;
            case "hit":
                var8 = 0.62 + 0.38 * smooth(var1 / 0.12);
                var10 = (1.0 - smooth(var1 / 0.42)) * smooth(var1 / 0.045);
                break;
            case "charge":
                double var21 = clamp(var1);
                var10 = 0.35 + 0.55 * var21;
                var8 = var5 == 0 ? 1.0 : 0.23 + 0.73 * smooth(var21);
                break;
            case "charge_mote":
                var6[1] = 0.035 * (1.0 - clamp(var1));
                var10 = 0.3 + 0.55 * clamp(var1);
                break;
            case "sweep":
                var7[1] = -58.0 + 136.0 * smooth(var1 / 0.27);
                var10 = smooth(var1 / 0.045) * (1.0 - smooth((var1 - 0.22) / 0.23));
                break;
            case "wave":
                var8 = 0.08 + 0.92 * smooth(var1 / 0.25);
                var10 = 1.0 - smooth((var1 - 0.12) / 0.43);
                break;
            case "wake":
                double var20 = (var1 * 3.0 + (double)var5 * 0.17) % 1.0;
                var6[2] = -var20 * 1.8;
                var6[1] = var20 * 0.045;
                var10 = 0.8 * Math.sin(var20 * Math.PI);
                break;
            case "crush":
                var6[0] = (double)(var5 % 2 != 0 ? -1 : 1) * 0.07 * Math.sin(var1 * 14.0);
                var10 = 0.55 + 0.25 * Math.sin(var1 * 14.0 + (double)var5);
                break;
            case "impact":
                var8 = 0.06 + 0.94 * smooth(var1 / 0.24);
                var10 = 1.0 - smooth((var1 - 0.12) / 0.55);
                break;
            case "spray":
                double var19 = clamp(var1 / 0.68);
                double var23 = (double)var5 * 2.399963;
                var6 = new double[]{Math.sin(var23) * var19 * 0.64, Math.max(-0.02, 0.72 * var19 - 0.8 * var19 * var19), Math.cos(var23) * var19 * 0.64};
                var10 = 1.0 - smooth(var19);
                break;
            case "fuse":
                double var14 = clamp(var1 / 5.0);
                double var16 = Math.pow(0.5 + 0.5 * Math.sin(var1 * (5.0 + var14 * 11.0) + (double)var5), 3.0);
                var8 = 1.0 + 0.035 * var16;
                var10 = 0.35 + 0.35 * var16 + 0.22 * var14;
                break;
            case "stun":
                var7[1] = var1 * 100.0 + (double)(var5 * 120);
                var10 = 0.6 + 0.15 * Math.sin(var1 * 5.0 + (double)var5);
                break;
            default:
                throw new IllegalArgumentException("Unknown combat track: " + var0);
        }

        return new CombatPose.State(var6, var7, var8, var10);
    }

    public static double[] place(double[] var0, double[] var1, CombatPose.State var2) {
        double var3 = (var0[0] - var1[0]) * var2.size();
        double var5 = (var0[1] - var1[1]) * var2.size();
        double var7 = (var0[2] - var1[2]) * var2.size();
        double[] var9 = new double[3];

        for (int var10 = 0; var10 < 3; var10++) {
            var9[var10] = Math.toRadians(var2.turn()[var10]);
        }

        double var17 = var5 * Math.cos(var9[0]) - var7 * Math.sin(var9[0]);
        var7 = var5 * Math.sin(var9[0]) + var7 * Math.cos(var9[0]);
        var5 = var17;
        var17 = var3 * Math.cos(var9[1]) + var7 * Math.sin(var9[1]);
        var7 = -var3 * Math.sin(var9[1]) + var7 * Math.cos(var9[1]);
        var3 = var17;
        var17 = var3 * Math.cos(var9[2]) - var5 * Math.sin(var9[2]);
        var5 = var3 * Math.sin(var9[2]) + var5 * Math.cos(var9[2]);
        var3 = var17;
        return new double[]{var3 + var1[0] + var2.move()[0], var5 + var1[1] + var2.move()[1], var7 + var1[2] + var2.move()[2]};
    }

    public static record State(double[] move, double[] turn, double size, double alpha) {
    }
}
