package net.farmingtales.ftboss.util;

/** 图鉴宽屏与缩字布局，翻页箭头、页码和正文分别占位。 */
public final class BossPreviewLayout {
    public static final float TEXT_SCALE = 0.9F;
    public static final int LINE_HEIGHT = 11;

    private BossPreviewLayout() {}

    public static int modelWidth(int screenWidth) {
        return Math.min(380, Math.max(320, screenWidth - 12));
    }

    public static int projectionRight(int modelWidth) {
        return modelWidth / 2;
    }

    public static int summaryX(int modelWidth) {
        return projectionRight(modelWidth) + 12;
    }

    public static int summaryWidth(int modelWidth) {
        return modelWidth - summaryX(modelWidth) - 12;
    }

    public static int guideWidth(int screenWidth) {
        return Math.min(720, Math.max(300, screenWidth - 12));
    }

    public static int guideHeight(int screenHeight) {
        return Math.min(460, Math.max(200, screenHeight - 12));
    }

    public static int previousX(int modelWidth) {
        return modelWidth - 102;
    }

    public static int nextX(int modelWidth) {
        return modelWidth - 30;
    }

    public static int pageCenterX(int modelWidth) {
        return modelWidth - 56;
    }

    public static int unscaledWidth(int width) {
        return Math.max(1, (int) Math.floor(width / TEXT_SCALE));
    }

    public static int footerTextWidth(int contentWidth) {
        return Math.max(1, contentWidth - 70);
    }
}
