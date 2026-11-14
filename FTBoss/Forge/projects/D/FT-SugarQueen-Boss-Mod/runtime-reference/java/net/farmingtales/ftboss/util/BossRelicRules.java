/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.List;

public final class BossRelicRules {
    public static final float DROP_CHANCE = 1.0f;
    public static final String AFFIX_TAG = "ftboss:relic_affix";
    public static final String SLOT = "boss_relic";
    public static final List<String> AFFIXES = List.of("vitality", "guarding", "swift", "fortunate");

    private BossRelicRules() {
    }

    public static Kind forEntity(String entity, boolean minion) {
        if (minion) {
            return null;
        }
        for (Kind kind : Kind.values()) {
            if (!("ftboss:" + kind.entity).equals(entity)) continue;
            return kind;
        }
        return null;
    }

    public static int validAffix(int value) {
        return value >= 0 && value < AFFIXES.size() ? value : -1;
    }

    public static boolean drops(float roll) {
        return roll >= 0.0f && roll < 1.0f;
    }

    public static enum Kind {
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

        private Kind(String entity, String item) {
            this.entity = entity;
            this.item = item;
        }
    }
}

