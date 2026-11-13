package net.farmingtales.ftdungeonmobs;

public final class DungeonGatePalette {
    private static final int[] COLORS = {0x42464D, 0x55CCEB, 0x54DB75, 0xF4C64E, 0xB66CE8, 0xE34555};

    private DungeonGatePalette() {}

    public static int color(int tier) {
        if (tier < 0 || tier >= COLORS.length) {
            throw new IllegalArgumentException("Portal color tier must be 0..5.");
        }
        return COLORS[tier];
    }
}
