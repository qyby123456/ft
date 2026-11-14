package net.farmingtales.ftboss.balance;

import java.util.Set;

public final class ManorBalanceRules {
    public static final int TRIDENT_SINGLE_TICKS = 15;
    public static final int TRIDENT_MULTI_TICKS = 10;
    public static final int SHADOW_BLINK_TICKS = 30;
    public static final int FOOD_INTERVAL_TICKS = 200;
    public static final int FOOD_DURATION_TICKS = 300;
    public static final double MOVEMENT_BONUS = 0.5;
    private static final Set<String> FOOD_MODS = Set.of(
        "farmersdelight",
        "vinery",
        "herbalbrews",
        "brewery",
        "farm_and_charm",
        "bakery",
        "candlelight",
        "beachparty",
        "meadow",
        "kaleidoscope_cookery",
        "brewinandchewin",
        "delightful",
        "expandeddelight",
        "corn_delight",
        "culturaldelights",
        "nethersdelight",
        "ends_delight",
        "oceansdelight"
    );

    private ManorBalanceRules() {
    }

    public static int tridentInterval(int players) {
        return players >= 2 ? 10 : 15;
    }

    public static int fasterCast(int ticks) {
        return ticks <= 0 ? ticks : (int)(((long)ticks * 4L + 4L) / 5L);
    }

    public static boolean foodMod(String namespace) {
        return FOOD_MODS.contains(namespace);
    }

    public static boolean blinkReady(long now, long last) {
        return last == Long.MIN_VALUE || now < last || now - last >= 30L;
    }
}
