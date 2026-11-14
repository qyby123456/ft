package net.farmingtales.ftboss.util;

public final class BlackwallRenderRules {
    public static final int MAX_WIDTH = 1280;
    public static final int MAX_HEIGHT = 720;

    private BlackwallRenderRules() {
    }

    private static double scale(int var0, int var1) {
        return Math.min(1.0, Math.min(1280.0 / (double)Math.max(1, var0), 720.0 / (double)Math.max(1, var1)));
    }

    public static int width(int var0, int var1) {
        return Math.max(1, (int)Math.floor((double)Math.max(1, var0) * scale(var0, var1)));
    }

    public static int height(int var0, int var1) {
        return Math.max(1, (int)Math.floor((double)Math.max(1, var1) * scale(var0, var1)));
    }
}
