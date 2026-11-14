/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.List;

public final class BossPreviewSummaryRules {
    public static final List<String> ORDINARY_ENTITIES = List.of("ftboss:sugar_queen", "ftboss:meat_mountain", "ftboss:tea_tree", "ftboss:wine_barrel", "ftboss:rat_boss", "ftboss:defect_core", "ftboss:compost_boss", "ftboss:apple_boss", "ftboss:benger", "ftboss:tide_fish_boss");

    private BossPreviewSummaryRules() {
    }

    public static boolean showsKills(int n) {
        return n >= 0 && n < ORDINARY_ENTITIES.size();
    }

    public static String entityId(int n) {
        return BossPreviewSummaryRules.showsKills(n) ? ORDINARY_ENTITIES.get(n) : "";
    }

    public static int lowBits(int n) {
        return Math.max(0, n) & 0xFFFF;
    }

    public static int highBits(int n) {
        return Math.max(0, n) >>> 16;
    }

    public static int kills(int n, int n2) {
        return (n2 & Short.MAX_VALUE) << 16 | n & 0xFFFF;
    }
}

