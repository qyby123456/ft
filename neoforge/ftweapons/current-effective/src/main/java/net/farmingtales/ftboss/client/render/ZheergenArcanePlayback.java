package net.farmingtales.ftboss.client.render;

public final class ZheergenArcanePlayback {
    private ZheergenArcanePlayback() {
    }

    public static double visualScale(double var0) {
        if (!Double.isFinite(var0) || var0 <= 0.0) {
            var0 = 1.0;
        }

        return Math.max(0.85, Math.min(1.35, Math.sqrt(var0)));
    }

    public static double elevation(double var0, double var2, double var4, double var6) {
        return Math.max(0.1 - var2 * var6, var0 - (var2 + var4) * 0.5 * var6);
    }

    public static boolean alive(double var0, int var2) {
        return Double.isFinite(var0) && var0 >= 0.0 && var2 > 0 && var0 < (double)var2;
    }

    public static float envelope(double var0, int var2) {
        return !alive(var0, var2) ? 0.0F : (float)(1.0 - Math.max(0.0, Math.min(1.0, (var0 - (double)var2 * 0.55) / ((double)var2 * 0.45))));
    }
}
