package cn.farmingtales.qysevent.wishingwell;

public final class GoldPotWishRules {
    public static final int LEVEL_THREE_POTS = 100;
    public static final int TOTAL_WEIGHT = 10000;
    public static final int MONEY_BUNDLE_VALUE = 100;
    public static final int MONEY_MIN_BUNDLES = 1;
    public static final int MONEY_MAX_BUNDLES = 9;

    public enum Category {
        SPELL("spell", 1500), FOOD("food", 3000), MONEY("coin", 500), GEODE("geode", 1500),
        MINERAL_BLOCK("mineral", 2500), SKILL_EXPERIENCE("skill_experience", 999), MANOR_REWARD("manor_reward", 1);

        private final String id;
        private final int weight;

        Category(String id, int weight) {
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
        throw new IllegalStateException("Gold-pot wish weights do not total 10000");
    }

    public static int eligiblePots(long previousPots, int donatedPots) {
        if (previousPots < 0 || donatedPots < 1 || donatedPots > 64) {
            throw new IllegalArgumentException("Invalid gold-pot donation");
        }
        if (previousPots >= LEVEL_THREE_POTS) return donatedPots;
        return Math.max(0, donatedPots - (LEVEL_THREE_POTS - (int) previousPots));
    }

    public static double expectedMoney() {
        return (double) Category.MONEY.weight() / TOTAL_WEIGHT
                * MONEY_BUNDLE_VALUE * (MONEY_MIN_BUNDLES + MONEY_MAX_BUNDLES) / 2;
    }
}
