package net.farmingtales.ftboss.util;

import java.util.List;

public final class BossPreviewRules {
    public static final int EMPTY = -1;
    public static final int TITLE_REQUIRED = -2;
    public static final int MANOR = 10;
    public static final String MANOR_TITLE = "farmingtales:sweet_apple_manor";
    public static final String CONTRACT = "tridents_n_stuff:sanguine_bond";
    public static final List<String> SUMMONS = List.of(
        "ftboss:sugar_queen_crystal",
        "ftboss:meat_heart",
        "ftboss:tea_seed",
        "ftboss:wine_charm",
        "ftboss:rat_bait",
        "ftboss:defect_core",
        "ftboss:compost_summon",
        "ftboss:forbidden_apple",
        "ftboss:benger_whistle",
        "ftboss:tide_banquet"
    );

    private BossPreviewRules() {
    }

    public static boolean accepts(String var0) {
        return "tridents_n_stuff:sanguine_bond".equals(var0) || SUMMONS.contains(var0);
    }

    public static int resolve(String var0, boolean var1) {
        if ("tridents_n_stuff:sanguine_bond".equals(var0)) {
            return var1 ? 10 : -2;
        } else {
            return SUMMONS.indexOf(var0);
        }
    }
}
