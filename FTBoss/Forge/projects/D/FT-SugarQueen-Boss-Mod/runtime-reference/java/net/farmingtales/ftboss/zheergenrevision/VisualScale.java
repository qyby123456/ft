/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.zheergenrevision;

public final class VisualScale {
    private VisualScale() {
    }

    public static double scale(double d) {
        return Double.isFinite(d) && d > 0.0 ? Math.min(4.0, d) : 1.0;
    }
}

