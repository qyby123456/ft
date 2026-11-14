package net.farmingtales.ftboss.client.maque;

public final class MaqueHumanoidTiming {
    private MaqueHumanoidTiming() {
    }

    public static boolean merge(boolean var0, boolean var1, boolean var2, long var3, long var5, long var7, int var9) {
        return var0 && var1 == var2 && var3 >= var7 && var3 - var7 < 4L && var3 >= var5 && var3 - var5 < (long)(var9 - 6);
    }

    public static MaqueHumanoidTiming.Sample sample(String var0, boolean var1, double var2, int var4, double var5) {
        if (Double.isFinite(var2) && !(var2 < 0.0) && !(var2 >= (double)var4)) {
            double var7 = var1 ? Math.min(0.4, var2 / 12.0 * 0.4) : var5 + var2 / (double)var4 * (1.0 - var5);
            double var9 = var5 > 0.0 && !var1 ? 1.0 : MaqueHumanoidPose.smooth(var2 / (double)(var0.equals("arrival") ? 6 : (var1 ? 4 : 3)));
            double var11 = MaqueHumanoidPose.smooth(((double)var4 - var2) / 6.0);
            return new MaqueHumanoidTiming.Sample(var7, var9 * var11, true);
        } else {
            return new MaqueHumanoidTiming.Sample(1.0, 0.0, false);
        }
    }

    public static double transition(double var0) {
        return MaqueHumanoidPose.smooth(var0 / 4.0);
    }

    public static record Sample(double progress, double weight, boolean active) {
    }
}
