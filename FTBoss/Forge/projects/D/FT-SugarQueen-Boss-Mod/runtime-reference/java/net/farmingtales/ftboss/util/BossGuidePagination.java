/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class BossGuidePagination {
    private BossGuidePagination() {
    }

    public static int pages(int n, int n2) {
        int n3 = Math.max(1, n2);
        return n <= 0 ? 1 : (n - 1) / n3 + 1;
    }

    public static int clamp(int n, int n2, int n3) {
        return Math.max(0, Math.min(n, BossGuidePagination.pages(n2, n3) - 1));
    }

    public static int start(int n, int n2, int n3) {
        return BossGuidePagination.clamp(n, n2, n3) * Math.max(1, n3);
    }
}

