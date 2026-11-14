/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class BossPreviewLayout {
    public static final float TEXT_SCALE = 0.9f;
    public static final int LINE_HEIGHT = 11;

    private BossPreviewLayout() {
    }

    public static int modelWidth(int n) {
        return Math.min(380, Math.max(320, n - 12));
    }

    public static int projectionRight(int n) {
        return n / 2;
    }

    public static int summaryX(int n) {
        return BossPreviewLayout.projectionRight(n) + 12;
    }

    public static int summaryWidth(int n) {
        return n - BossPreviewLayout.summaryX(n) - 12;
    }

    public static int guideWidth(int n) {
        return Math.min(720, Math.max(300, n - 12));
    }

    public static int guideHeight(int n) {
        return Math.min(460, Math.max(200, n - 12));
    }

    public static int previousX(int n) {
        return n - 102;
    }

    public static int nextX(int n) {
        return n - 30;
    }

    public static int pageCenterX(int n) {
        return n - 56;
    }

    public static int unscaledWidth(int n) {
        return Math.max(1, (int)Math.floor((float)n / 0.9f));
    }

    public static int footerTextWidth(int n) {
        return Math.max(1, n - 70);
    }
}

