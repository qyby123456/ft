/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class WeaponBondGuideLayout {
    public static final int TAB_COUNT = 5;
    public static final int LINE_HEIGHT = 12;
    public static final int DIAGRAM_HEIGHT = 64;

    private WeaponBondGuideLayout() {
    }

    public static int panelWidth(int n) {
        return Math.max(1, Math.min(620, n - 12));
    }

    public static int panelHeight(int n) {
        return Math.max(1, Math.min(420, n - 12));
    }

    public static int page(int n, int n2) {
        return Math.max(0, Math.min(n, Math.max(0, n2 - 1)));
    }

    public static int viewportHeight(int n) {
        return Math.max(1, n - 139);
    }

    public static int scroll(int n, int n2, int n3) {
        return Math.max(0, Math.min(n, Math.max(0, n2 - n3)));
    }

    public static int groupWidth(int n, int n2) {
        return Math.max(1, (n - Math.max(0, n2 - 1) * 16) / Math.max(1, n2));
    }

    public static boolean compactEntry(int n) {
        int n2 = (n - 232) / 2;
        return n2 + 254 > n && n2 < 22;
    }

    public static int entryY(int n) {
        int n2 = (n - 232) / 2;
        int n3 = n2 + 238;
        int n4 = n2 - 22;
        return n3 + 16 <= n ? n3 : (n4 >= 0 ? n4 : n2 + 211);
    }
}

