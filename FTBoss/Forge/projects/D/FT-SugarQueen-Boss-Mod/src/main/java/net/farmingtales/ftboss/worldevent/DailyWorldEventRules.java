package net.farmingtales.ftboss.worldevent;

/** Pure deterministic rules, kept independent from Minecraft so calendar invariants are executable in tests. */
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

    private static final long ORDER_SALT = 0x4F5241434C455F31L;
    private static final long TRIGGER_SALT = 0x4556454E545F3130L;
    private static final long DAY_GAMMA = 0x9E3779B97F4A7C15L;

    private DailyWorldEventRules() {}

    /**
     * Returns one entry from a seed-shuffled bag. Every aligned block of {@code eventCount} days contains
     * each candidate exactly once, so candidate events cannot repeat until the bag is exhausted.
     */
    public static int eventIndex(long worldSeed, long day, int eventCount) {
        if (eventCount <= 0) throw new IllegalArgumentException("eventCount must be positive");
        int[] bag = new int[eventCount];
        for (int i = 0; i < eventCount; i++) bag[i] = i;
        long random = mix64(worldSeed ^ ORDER_SALT);
        for (int i = eventCount - 1; i > 0; i--) {
            random = mix64(random + DAY_GAMMA + i);
            int swap = (int) Math.floorMod(random, (long) i + 1L);
            int value = bag[i];
            bag[i] = bag[swap];
            bag[swap] = value;
        }
        return bag[Math.floorMod(day, eventCount)];
    }

    /** Independent deterministic 10% roll; the oracle intentionally reveals the candidate, not this result. */
    public static boolean triggers(long worldSeed, long day) {
        return unit(mix64(worldSeed ^ TRIGGER_SALT ^ day * DAY_GAMMA)) < DAILY_TRIGGER_CHANCE;
    }

    /** Exact mutually-exclusive rates requested for aquatic replacements, plus a valuable-fish boost. */
    public static int aquaticOutcome(double roll) {
        if (roll < 0.010D) return AQUATIC_AMETHYST_CRAB;
        if (roll < 0.035D) return AQUATIC_ELDER_GUARDIAN;
        if (roll < 0.085D) return AQUATIC_GUARDIAN;
        if (roll < 0.185D) return AQUATIC_DROWNED;
        if (roll < 0.535D) return AQUATIC_VALUABLE_FISH;
        return AQUATIC_UNCHANGED;
    }

    public static int kitchenOutcome(double roll) {
        if (roll < 0.12D) return KITCHEN_GINGERBREAD;
        if (roll < 0.16D) return KITCHEN_BEER_ELEMENTAL;
        if (roll < 0.19D) return KITCHEN_EXPLOSIVE_BARREL;
        if (roll < 0.24D) return KITCHEN_SAYA_FLOWER;
        return KITCHEN_UNCHANGED;
    }

    /** 0x, 1x, 2x, or 3x, selected once per mature crop harvest. */
    public static int kitchenHarvestMultiplier(double roll) {
        return Math.min(3, Math.max(0, (int) Math.floor(roll * 4.0D)));
    }

    /** A paid forecast matures only on the immediately following day and only if an event really triggered. */
    public static boolean oracleAlertDue(long consultationDay, long currentDay, boolean eventTriggered) {
        return eventTriggered && consultationDay == currentDay - 1L;
    }

    private static double unit(long value) {
        return (value >>> 11) * 0x1.0p-53;
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}