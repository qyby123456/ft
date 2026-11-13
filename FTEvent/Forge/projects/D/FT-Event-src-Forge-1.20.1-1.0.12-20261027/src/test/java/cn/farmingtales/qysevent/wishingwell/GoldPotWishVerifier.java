package cn.farmingtales.qysevent.wishingwell;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class GoldPotWishVerifier {
    public static void verify() {
        Map<GoldPotWishRules.Category, Integer> counts = new EnumMap<>(GoldPotWishRules.Category.class);
        for (int ticket = 0; ticket < GoldPotWishRules.TOTAL_WEIGHT; ticket++) {
            counts.merge(GoldPotWishRules.categoryForRoll(ticket), 1, Integer::sum);
        }
        for (GoldPotWishRules.Category category : GoldPotWishRules.Category.values()) {
            require(counts.get(category) == category.weight(), "Category weight mismatch: " + category);
        }
        Map<GoldPotWishRules.Category, Integer> expectedCategories = Map.of(
                GoldPotWishRules.Category.SPELL, 15000,
                GoldPotWishRules.Category.FOOD, 30000,
                GoldPotWishRules.Category.MONEY, 5000,
                GoldPotWishRules.Category.GEODE, 15000,
                GoldPotWishRules.Category.MINERAL_BLOCK, 25000,
                GoldPotWishRules.Category.SKILL_EXPERIENCE, 9965,
                GoldPotWishRules.Category.MANOR_REWARD, 35);
        require(GoldPotWishRules.TOTAL_WEIGHT == 100000, "The category draw must use 100000 tickets");
        require(counts.equals(expectedCategories), "Category odds changed unexpectedly: " + counts);
        Map<GoldPotWishRules.Mineral, Integer> minerals = new EnumMap<>(GoldPotWishRules.Mineral.class);
        for (int ticket = 0; ticket < GoldPotWishRules.MINERAL_TOTAL_WEIGHT; ticket++) {
            minerals.merge(GoldPotWishRules.mineralForRoll(ticket), 1, Integer::sum);
        }
        Map<GoldPotWishRules.Mineral, Integer> expectedMinerals = Map.ofEntries(
                Map.entry(GoldPotWishRules.Mineral.COAL, 2500),
                Map.entry(GoldPotWishRules.Mineral.COPPER, 2500),
                Map.entry(GoldPotWishRules.Mineral.IRON, 2000),
                Map.entry(GoldPotWishRules.Mineral.REDSTONE, 1200),
                Map.entry(GoldPotWishRules.Mineral.LAPIS, 800),
                Map.entry(GoldPotWishRules.Mineral.GOLD, 600),
                Map.entry(GoldPotWishRules.Mineral.DIAMOND, 200),
                Map.entry(GoldPotWishRules.Mineral.EMERALD, 100),
                Map.entry(GoldPotWishRules.Mineral.ANCIENT_METAL, 50),
                Map.entry(GoldPotWishRules.Mineral.NETHERITE, 30),
                Map.entry(GoldPotWishRules.Mineral.IGNITIUM, 10),
                Map.entry(GoldPotWishRules.Mineral.CURSIUM, 10));
        require(GoldPotWishRules.MINERAL_TOTAL_WEIGHT == 10000, "The mineral draw must use 10000 tickets");
        require(minerals.equals(expectedMinerals), "Mineral rarity odds changed unexpectedly: " + minerals);
        Set<String> mineralIds = new HashSet<>();
        for (GoldPotWishRules.Mineral mineral : GoldPotWishRules.Mineral.values()) {
            require(mineralIds.add(mineral.id()), "Duplicate mineral item: " + mineral.id());
            require(minerals.get(mineral) == mineral.weight(), "Mineral weight mismatch: " + mineral);
        }
        require(mineralIds.containsAll(Set.of("minecraft:netherite_block", "cataclysm:ancient_metal_block",
                "cataclysm:ignitium_block", "cataclysm:cursium_block")), "Requested alloy blocks are missing");
        verifyCoins();
        require(GoldPotWishRules.expectedMoney() == 9000.0D, "Expected money must be 9000 per pot");
        require(GoldPotWishRules.eligiblePots(0, 64) == 0, "Early donations must not draw rewards");
        require(GoldPotWishRules.eligiblePots(99, 1) == 0, "The level-three upgrade pot is not a draw");
        require(GoldPotWishRules.eligiblePots(99, 3) == 2, "Crossing threshold must draw only excess pots");
        require(GoldPotWishRules.eligiblePots(100, 1) == 1, "Level-three pots must draw once each");
        require(GoldPotWishRules.eligiblePots(100, 64) == 64, "Bulk pots must draw independently");
        require(GoldPotWishRules.eligiblePots(Long.MAX_VALUE, 1) == 1, "Eligibility must not overflow");
        for (int donatedPots = 1; donatedPots <= 64; donatedPots++) {
            require(GoldPotWishRules.unlockedRolls(donatedPots) == donatedPots,
                    "Every pot after emerald-block unlock must draw, including the first pot");
        }
        expectInvalid(() -> GoldPotWishRules.unlockedRolls(0));
        expectInvalid(() -> GoldPotWishRules.unlockedRolls(65));
        expectInvalid(() -> GoldPotWishRules.categoryForRoll(-1));
        expectInvalid(() -> GoldPotWishRules.categoryForRoll(GoldPotWishRules.TOTAL_WEIGHT));
        expectInvalid(() -> GoldPotWishRules.mineralForRoll(-1));
        expectInvalid(() -> GoldPotWishRules.mineralForRoll(GoldPotWishRules.MINERAL_TOTAL_WEIGHT));
        expectInvalid(() -> GoldPotWishRules.eligiblePots(-1, 1));
        expectInvalid(() -> GoldPotWishRules.eligiblePots(100, 0));
        expectInvalid(() -> GoldPotWishRules.eligiblePots(100, 65));
        System.out.println("Gold-pot wishes verified: category/mineral/coin tickets, exact 0.035% manor odds, 12 mineral blocks, 9000 expected coins per pot, upgrade/bulk boundaries.");
    }

    private static void verifyCoins() {
        GoldPotCoinRewards coins = GoldPotCoinRewards.defaults();
        Map<Integer, Integer> counts = new java.util.HashMap<>();
        long totalCoins = 0;
        for (int ticket = 0; ticket < coins.totalWeight(); ticket++) {
            int amount = coins.coinsForRoll(ticket);
            counts.merge(amount, 1, Integer::sum);
            totalCoins += amount;
        }
        require(coins.totalWeight() == 9000000, "Coin draw must use 9000000 tickets");
        require(counts.equals(Map.of(1, 900000, 100, 1800000, 1000, 1800000,
                10000, 1800000, 100000, 1222201, 1000000, 1477799)), "Coin payout odds changed");
        require(totalCoins == 180000L * coins.totalWeight(), "Conditional coin expectation must be exactly 180000");
        require(coins.expectedCoins() == 180000.0D, "Conditional expectation calculation is incorrect");
        expectInvalid(() -> coins.coinsForRoll(-1));
        expectInvalid(() -> coins.coinsForRoll(coins.totalWeight()));
        expectInvalid(() -> GoldPotCoinRewards.read(new java.io.StringReader("{\"rewards\":[]}")));
        try {
            java.nio.file.Path file = java.nio.file.Files.createTempDirectory("gold-pot-coins-").resolve("rewards.json");
            require(GoldPotCoinRewards.load(file).expectedCoins() == 180000.0D, "Missing config must export defaults");
            String custom = java.nio.file.Files.readString(file).replace("900000 }", "1800000 }");
            java.nio.file.Files.writeString(file, custom);
            require(GoldPotCoinRewards.load(file).totalWeight() == 9900000, "Edited config must load on the next batch");
            require(java.nio.file.Files.readString(file).equals(custom), "Loading must not overwrite edited config");
            java.nio.file.Files.delete(file);
            java.nio.file.Files.delete(file.getParent());
        } catch (java.io.IOException error) {
            throw new IllegalStateException("Coin config export/reload verification failed", error);
        }
    }

    private static void expectInvalid(Runnable operation) {
        try { operation.run(); } catch (IllegalArgumentException expected) { return; }
        throw new IllegalStateException("Invalid wish input was accepted");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
