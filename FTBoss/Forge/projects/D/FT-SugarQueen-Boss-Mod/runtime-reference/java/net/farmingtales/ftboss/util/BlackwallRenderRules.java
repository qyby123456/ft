/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class BlackwallRenderRules {
    public static final int MAX_WIDTH = 1280;
    public static final int MAX_HEIGHT = 720;

    private BlackwallRenderRules() {
    }

    private static double scale(int n, int n2) {
        return Math.min(1.0, Math.min(1280.0 / (double)Math.max(1, n), 720.0 / (double)Math.max(1, n2)));
    }

    public static int width(int n, int n2) {
        return Math.max(1, (int)Math.floor((double)Math.max(1, n) * BlackwallRenderRules.scale(n, n2)));
    }

    public static int height(int n, int n2) {
        return Math.max(1, (int)Math.floor((double)Math.max(1, n2) * BlackwallRenderRules.scale(n, n2)));
    }
}

