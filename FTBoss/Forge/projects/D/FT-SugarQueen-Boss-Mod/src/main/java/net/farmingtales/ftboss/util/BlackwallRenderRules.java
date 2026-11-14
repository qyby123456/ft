package net.farmingtales.ftboss.util;

/** 黑墙客户端渲染预算；2026-10-21 特效最多 1280×720，保留原画面分辨率并避免循环创建帧缓冲。 */
public final class BlackwallRenderRules {
    public static final int MAX_WIDTH = 1280;
    public static final int MAX_HEIGHT = 720;

    private BlackwallRenderRules() {}

    private static double scale(int width, int height) {
        return Math.min(1.0, Math.min((double) MAX_WIDTH / Math.max(1, width), (double) MAX_HEIGHT / Math.max(1, height)));
    }

    public static int width(int width, int height) {
        return Math.max(1, (int) Math.floor(Math.max(1, width) * scale(width, height)));
    }

    public static int height(int width, int height) {
        return Math.max(1, (int) Math.floor(Math.max(1, height) * scale(width, height)));
    }
}
