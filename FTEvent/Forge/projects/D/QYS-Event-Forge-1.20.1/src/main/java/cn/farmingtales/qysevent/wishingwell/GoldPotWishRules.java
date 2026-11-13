package cn.farmingtales.qysevent.wishingwell;

public final class GoldPotWishRules {
    public static final int LEVEL_THREE_POTS = 100;
    public static final int TOTAL_WEIGHT = 100000;
    public static final int MINERAL_TOTAL_WEIGHT = 10000;

    public enum Category {
        SPELL("spell", 15000), FOOD("food", 30000), MONEY("coin", 5000), GEODE("geode", 15000),
        MINERAL_BLOCK("mineral", 25000), SKILL_EXPERIENCE("skill_experience", 9965), MANOR_REWARD("manor_reward", 35);

        private final String id;
        private final int weight;

        Category(String id, int weight) {
            this.id = id;
            this.weight = weight;
        }

        public String id() { return id; }
        public int weight() { return weight; }
    }

    public enum Mineral {
        COAL("minecraft:coal_block", 2500), COPPER("minecraft:copper_block", 2500),
        IRON("minecraft:iron_block", 2000), REDSTONE("minecraft:redstone_block", 1200),
        LAPIS("minecraft:lapis_block", 800), GOLD("minecraft:gold_block", 600),
        DIAMOND("minecraft:diamond_block", 200), EMERALD("minecraft:emerald_block", 100),
        ANCIENT_METAL("cataclysm:ancient_metal_block", 50), NETHERITE("minecraft:netherite_block", 30),
        IGNITIUM("cataclysm:ignitium_block", 10), CURSIUM("cataclysm:cursium_block", 10);

        private final String id;
        private final int weight;

        Mineral(String id, int weight) {
            this.id = id;
            this.weight = weight;
        }

        public String id() { return id; }
        public int weight() { return weight; }
    }

    private GoldPotWishRules() {}

    public static Category categoryForRoll(int ticket) {
        if (ticket < 0 || ticket >= TOTAL_WEIGHT) throw new IllegalArgumentException("Invalid wish ticket");
        for (Category category : Category.values()) {
            ticket -= category.weight();
            if (ticket < 0) return category;
        }
        throw new IllegalStateException("Gold-pot wish weights do not total " + TOTAL_WEIGHT);
    }

    public static Mineral mineralForRoll(int ticket) {
        if (ticket < 0 || ticket >= MINERAL_TOTAL_WEIGHT) throw new IllegalArgumentException("Invalid mineral ticket");
        for (Mineral mineral : Mineral.values()) {
            ticket -= mineral.weight();
            if (ticket < 0) return mineral;
        }
        throw new IllegalStateException("Mineral weights do not total " + MINERAL_TOTAL_WEIGHT);
    }

    public static int eligiblePots(long previousPots, int donatedPots) {
        if (previousPots < 0 || donatedPots < 1 || donatedPots > 64) {
            throw new IllegalArgumentException("Invalid gold-pot donation");
        }
        if (previousPots >= LEVEL_THREE_POTS) return donatedPots;
        return Math.max(0, donatedPots - (LEVEL_THREE_POTS - (int) previousPots));
    }

    public static int unlockedRolls(int donatedPots) {
        if (donatedPots < 1 || donatedPots > 64) throw new IllegalArgumentException("Invalid unlocked gold-pot donation");
        return donatedPots;
    }

    public static double expectedMoney() {
        return (double) Category.MONEY.weight() / TOTAL_WEIGHT
                * GoldPotCoinRewards.defaults().expectedCoins();
    }
}
