package net.farmingtales.ftboss.client.render;

public final class QingyiWaveTiming {
    private QingyiWaveTiming() {
    }

    public static double scale(double var0) {
        return Double.isFinite(var0) && var0 > 0.0 ? Math.min(32.0, var0) : 1.0;
    }

    public static int slice(double var0, double var2) {
        return Math.max(1, Math.min(4, (int)Math.round((var0 * 0.5 - 0.8) / (1.75 * var2))));
    }

    public static double delay(int var0) {
        return 0.0;
    }

    public static float time(double var0, int var2) {
        return (float)Math.max(0.0, (var0 - delay(var2)) * 0.05);
    }

    public static float opacity(double var0, int var2) {
        double var3 = var0 - delay(var2);
        return (float)(smooth(var3 / 1.2) * (1.0 - smooth((var3 - 11.0) / 8.0)));
    }

    public static double travel(double var0, int var2) {
        return 0.0;
    }

    public static float cameraOpacity(double var0, double var2, double var4, double var6, double var8, double var10, boolean var12) {
        double var13 = Math.max(0.0, Math.max(var0, -var6));
        double var15 = Math.max(0.0, Math.max(var2, -var8));
        double var17 = Math.max(0.0, Math.max(var4, -var10));
        double var19 = Math.sqrt(var13 * var13 + var15 * var15 + var17 * var17);
        return (float)smooth((var19 - (var12 ? 0.3 : 0.12)) / (var12 ? 0.85 : 0.45));
    }

    public static double smooth(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, var0));
        return var2 * var2 * (3.0 - 2.0 * var2);
    }
}
