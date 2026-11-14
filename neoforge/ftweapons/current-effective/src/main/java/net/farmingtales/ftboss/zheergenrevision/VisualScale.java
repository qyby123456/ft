package net.farmingtales.ftboss.zheergenrevision;

public final class VisualScale {
    private VisualScale() {
    }

    public static double scale(double var0) {
        return Double.isFinite(var0) && var0 > 0.0 ? Math.min(4.0, var0) : 1.0;
    }
}
