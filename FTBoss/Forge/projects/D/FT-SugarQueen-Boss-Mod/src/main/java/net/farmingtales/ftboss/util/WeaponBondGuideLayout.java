package net.farmingtales.ftboss.util;

/** 羁绊图鉴布局；2026-10-21 为五类武器筛选行预留空间，正文统一滚动。 */
public final class WeaponBondGuideLayout {
    public static final int TAB_COUNT = 5;
    public static final int LINE_HEIGHT = 12;
    public static final int DIAGRAM_HEIGHT = 64;

    private WeaponBondGuideLayout() {}

    public static int panelWidth(int screenWidth) { return Math.max(1, Math.min(620, screenWidth - 12)); }

    public static int panelHeight(int screenHeight) { return Math.max(1, Math.min(420, screenHeight - 12)); }

    public static int page(int requested, int count) { return Math.max(0, Math.min(requested, Math.max(0, count - 1))); }

    public static int viewportHeight(int panelHeight) { return Math.max(1, panelHeight - 139); }

    public static int scroll(int requested, int contentHeight, int viewportHeight) {
        return Math.max(0, Math.min(requested, Math.max(0, contentHeight - viewportHeight)));
    }

    public static int groupWidth(int availableWidth, int count) {
        return Math.max(1, (availableWidth - Math.max(0, count - 1) * 16) / Math.max(1, count));
    }

    public static boolean compactEntry(int screenHeight) {
        int top = (screenHeight - 232) / 2;
        return top + 254 > screenHeight && top < 22;
    }

    public static int entryY(int screenHeight) {
        int top = (screenHeight - 232) / 2;
        int below = top + 238;
        int above = top - 22;
        return below + 16 <= screenHeight ? below : above >= 0 ? above : top + 211;
    }
}
