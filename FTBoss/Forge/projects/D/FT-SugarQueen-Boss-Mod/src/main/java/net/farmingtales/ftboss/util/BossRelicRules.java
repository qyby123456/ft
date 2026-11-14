package net.farmingtales.ftboss.util;

import java.util.List;

public final class BossRelicRules {
    /** ★ 2026-09-19 按用户要求：遗物改成**必掉**（原 0.35F 概率）。
        石像 / 专属武器 / 法术卷轴本来就是必掉，只有遗物在抽概率，用户反馈「机器人的吊坠未掉落」后确认要必掉。 */
    public static final float DROP_CHANCE = 1.0F;
    public static final String AFFIX_TAG = "ftboss:relic_affix";
    public static final String SLOT = "boss_relic";
    public static final List<String> AFFIXES = List.of("vitality", "guarding", "swift", "fortunate");

    public enum Kind {
        FROST("sugar_queen", "frost_crown_charm"),
        BUTCHER("meat_mountain", "butcher_fang_charm"),
        TEA("tea_tree", "tea_heart_pendant"),
        WINE("wine_barrel", "aged_wine_seal"),
        RAT("rat_boss", "cheese_moon_charm"),
        DEFECT("defect_core", "charged_core_pendant"),
        COMPOST("compost_boss", "rainbow_spore_charm"),
        APPLE("apple_boss", "golden_apple_brooch"),
        BENGER("benger", "shepherd_whistle_charm"),
        TIDE("tide_fish_boss", "tide_pearl_pendant");

        public final String entity;
        public final String item;

        Kind(String entity, String item) {
            this.entity = entity;
            this.item = item;
        }
    }

    private BossRelicRules() {}

    public static Kind forEntity(String entity, boolean minion) {
        if (minion) return null;
        for (Kind kind : Kind.values()) {
            if (("ftboss:" + kind.entity).equals(entity)) return kind;
        }
        return null;
    }

    public static int validAffix(int value) {
        return value >= 0 && value < AFFIXES.size() ? value : -1;
    }

    public static boolean drops(float roll) {
        return roll >= 0 && roll < DROP_CHANCE;
    }
}
