package net.farmingtales.ftboss.util;

import java.util.List;

/** 空白石像索引规则；2026-10-12：只认精确召唤物与契约 ID。 */
public final class BossPreviewRules {
    public static final int EMPTY = -1;
    public static final int TITLE_REQUIRED = -2;
    public static final int MANOR = 10;
    public static final String MANOR_TITLE = "farmingtales:sweet_apple_manor";
    public static final String CONTRACT = "tridents_n_stuff:sanguine_bond";
    public static final List<String> SUMMONS = List.of(
            "ftboss:sugar_queen_crystal", "ftboss:meat_heart", "ftboss:tea_seed", "ftboss:wine_charm",
            "ftboss:rat_bait", "ftboss:defect_core", "ftboss:compost_summon", "ftboss:forbidden_apple",
            "ftboss:benger_whistle", "ftboss:tide_banquet");

    private BossPreviewRules() {}

    public static boolean accepts(String itemId) {
        return CONTRACT.equals(itemId) || SUMMONS.contains(itemId);
    }

    public static int resolve(String itemId, boolean wearingManorTitle) {
        if (CONTRACT.equals(itemId)) return wearingManorTitle ? MANOR : TITLE_REQUIRED;
        return SUMMONS.indexOf(itemId);
    }
}
