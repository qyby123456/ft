package net.farmingtales.ftboss.util;

/** 图鉴介绍页的纯分页规则，按可用行数计算页数并钳位导航。 */
public final class BossGuidePagination {
    private BossGuidePagination() {}

    public static int pages(int lines, int rows) {
        int safeRows = Math.max(1, rows);
        return lines <= 0 ? 1 : (lines - 1) / safeRows + 1;
    }

    public static int clamp(int page, int lines, int rows) {
        return Math.max(0, Math.min(page, pages(lines, rows) - 1));
    }

    public static int start(int page, int lines, int rows) {
        return clamp(page, lines, rows) * Math.max(1, rows);
    }
}
