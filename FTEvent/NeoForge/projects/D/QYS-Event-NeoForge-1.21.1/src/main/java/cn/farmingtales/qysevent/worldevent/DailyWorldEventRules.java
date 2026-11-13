package cn.farmingtales.qysevent.worldevent;

public final class DailyWorldEventRules {
    public static final double DAILY_TRIGGER_CHANCE = 0.10D;
    public static final int FORECAST_DAYS = 10;
    public static final int AQUATIC_AMETHYST_CRAB = 0;
    public static final int AQUATIC_ELDER_GUARDIAN = 1;
    public static final int AQUATIC_GUARDIAN = 2;
    public static final int AQUATIC_DROWNED = 3;
    public static final int AQUATIC_VALUABLE_FISH = 4;
    public static final int AQUATIC_UNCHANGED = 5;
    public static final int KITCHEN_GINGERBREAD = 0;
    public static final int KITCHEN_BEER_ELEMENTAL = 1;
    public static final int KITCHEN_EXPLOSIVE_BARREL = 2;
    public static final int KITCHEN_SAYA_FLOWER = 3;
    public static final int KITCHEN_UNCHANGED = 4;

    public static final int HEAVY_SNOW_UNCHANGED = 0;
    public static final int HEAVY_SNOW_FLY = 1;
    public static final int HEAVY_SNOW_MAGGOT = 2;
    public static final int HEAVY_SNOW_POOP = 3;
    public static final int HEAVY_SNOW_COMPOST_SLIME = 4;
    public static final int HEAVY_SNOW_SHEEP_DUNG = 5;
    public static final int HEAVY_SNOW_APPLE_WORM = 6;
    public static final int HEAVY_SNOW_APPLE_MOTH = 7;
    public static final int HEAVY_SNOW_ROTTEN_APPLE = 8;
    public static final int HEAVY_SNOW_GIANT_GRUB = 9;
    public static final int HEAVY_SNOW_PRECIPITATION_COLOR = 0x6B3E23;
    /** Faster than other event pulses so Heavy Snow remains observable even when vanilla spawning is capped. */
    public static final int HEAVY_SNOW_PULSE_INTERVAL_TICKS = 200;
    /** A forced test should immediately show a small group instead of a single easy-to-miss mob. */
    public static final int HEAVY_SNOW_PREVIEW_SPAWNS_PER_PLAYER = 3;
    public static final double HEAVY_SNOW_PREVIEW_MIN_RADIUS = 4.0D;
    public static final double HEAVY_SNOW_PREVIEW_MAX_RADIUS = 12.0D;
    public static final double HEAVY_SNOW_PULSE_MIN_RADIUS = 8.0D;
    public static final double HEAVY_SNOW_PULSE_MAX_RADIUS = 20.0D;
    /** Supplemental event mobs must remain visible in caves, buildings, and manor test arenas. */
    public static final boolean HEAVY_SNOW_PULSE_REQUIRES_SKY = false;
    private static final long ORDER_SALT = 5715702634384809777L;
    private static final long TRIGGER_SALT = 4996257039339434288L;
    private static final long DAY_GAMMA = -7046029254386353131L;

    private DailyWorldEventRules() {}

    public static int eventIndex(long worldSeed, long day, int eventCount) {
        if (eventCount <= 0) throw new IllegalArgumentException("eventCount must be positive");
        int[] bag = new int[eventCount];
        for (int i = 0; i < eventCount; i++) bag[i] = i;
        long random = mix64(worldSeed ^ ORDER_SALT);
        for (int i = eventCount - 1; i > 0; i--) {
            random = mix64(random + DAY_GAMMA + i);
            int swap = (int)Math.floorMod(random, (long)i + 1L);
            int value = bag[i]; bag[i] = bag[swap]; bag[swap] = value;
        }
        return bag[Math.floorMod(day, eventCount)];
    }

    public static boolean triggers(long worldSeed, long day) {
        return unit(mix64(worldSeed ^ TRIGGER_SALT ^ day * DAY_GAMMA)) < DAILY_TRIGGER_CHANCE;
    }

    public static int aquaticOutcome(double roll) {
        if (roll < .01D) return AQUATIC_AMETHYST_CRAB;
        if (roll < .035D) return AQUATIC_ELDER_GUARDIAN;
        if (roll < .085D) return AQUATIC_GUARDIAN;
        if (roll < .185D) return AQUATIC_DROWNED;
        if (roll < .535D) return AQUATIC_VALUABLE_FISH;
        return AQUATIC_UNCHANGED;
    }

    public static int kitchenOutcome(double roll) {
        if (roll < .12D) return KITCHEN_GINGERBREAD;
        if (roll < .16D) return KITCHEN_BEER_ELEMENTAL;
        if (roll < .19D) return KITCHEN_EXPLOSIVE_BARREL;
        if (roll < .24D) return KITCHEN_SAYA_FLOWER;
        return KITCHEN_UNCHANGED;
    }

    /**
     * Heavy Snow corrupts 45% of natural outdoor hostile spawns. The last
     * half-percent is the Apple boss's giant grub and is additionally capped
     * to one within 96 blocks by the runtime hook.
     */
    public static int heavySnowOutcome(double roll) {
        if (roll < .55D) return HEAVY_SNOW_UNCHANGED;
        if (roll < .71D) return HEAVY_SNOW_FLY;
        if (roll < .84D) return HEAVY_SNOW_MAGGOT;
        if (roll < .90D) return HEAVY_SNOW_POOP;
        if (roll < .94D) return HEAVY_SNOW_COMPOST_SLIME;
        if (roll < .96D) return HEAVY_SNOW_SHEEP_DUNG;
        if (roll < .975D) return HEAVY_SNOW_APPLE_WORM;
        if (roll < .985D) return HEAVY_SNOW_APPLE_MOTH;
        if (roll < .995D) return HEAVY_SNOW_ROTTEN_APPLE;
        return HEAVY_SNOW_GIANT_GRUB;
    }

    public static String heavySnowEntityPath(int outcome) {
        return switch (outcome) {
            case HEAVY_SNOW_FLY -> "fly_minion";
            case HEAVY_SNOW_MAGGOT -> "maggot_minion";
            case HEAVY_SNOW_POOP -> "poop_minion";
            case HEAVY_SNOW_COMPOST_SLIME -> "compost_slime";
            case HEAVY_SNOW_SHEEP_DUNG -> "sheep_dung";
            case HEAVY_SNOW_APPLE_WORM -> "apple_worm";
            case HEAVY_SNOW_APPLE_MOTH -> "apple_moth";
            case HEAVY_SNOW_ROTTEN_APPLE -> "rotten_apple";
            case HEAVY_SNOW_GIANT_GRUB -> "giant_grub";
            default -> null;
        };
    }

    /** Events that need active pulse spawns so forced tests and quiet areas are visibly testable. */
    public static boolean hasCreatureSpawns(DailyWorldEvent event) {
        if (event == null) return false;
        return switch (event) {
            case BIG_FISH_MIGRATION, KITCHEN_CHAOS, NO_SUCH_CREATURE, UNDEAD_MIGRATION,
                    PIG_MIGRATION, HEAVY_SNOW, SLIME_RAIN, RABBIT_TIDE, WITCH_CONVENTION,
                    MUSHROOM_BLOOM, ENDER_ECLIPSE -> true;
            default -> false;
        };
    }

    /** Kitchen Chaos is random 1–3x output and never reduces a mature harvest. */
    public static int kitchenHarvestMultiplier(double roll) {
        return 1 + Math.min(2, Math.max(0, (int)Math.floor(roll * 3.0D)));
    }

    private static double unit(long value) { return (double)(value >>> 11) * 1.1102230246251565E-16D; }
    private static long mix64(long value) {
        value = (value ^ value >>> 30) * -4658895280553007687L;
        value = (value ^ value >>> 27) * -7723592293110705685L;
        return value ^ value >>> 31;
    }
}


