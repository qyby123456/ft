package cn.farmingtales.qysevent.wishingwell;

import java.util.EnumMap;
import java.util.Map;

public final class GoldPotWishVerifier {
    public static void verify() {
        Map<GoldPotWishRules.Category, Integer> counts = new EnumMap<>(GoldPotWishRules.Category.class);
        for (int ticket = 0; ticket < GoldPotWishRules.TOTAL_WEIGHT; ticket++) {
            counts.merge(GoldPotWishRules.categoryForRoll(ticket), 1, Integer::sum);
        }
        for (GoldPotWishRules.Category category : GoldPotWishRules.Category.values()) {
            require(counts.get(category) == category.weight(), "Category weight mismatch: " + category);
        }
        require(counts.get(GoldPotWishRules.Category.MANOR_REWARD) == 1, "Manor rewards must be exactly 0.01%");
        require(GoldPotWishRules.expectedMoney() == 25.0D, "Expected money must be 25 per pot");
        require(GoldPotWishRules.eligiblePots(0, 64) == 0, "Early donations must not draw rewards");
        require(GoldPotWishRules.eligiblePots(99, 1) == 0, "The level-three upgrade pot is not a draw");
        require(GoldPotWishRules.eligiblePots(99, 3) == 2, "Crossing threshold must draw only excess pots");
        require(GoldPotWishRules.eligiblePots(100, 1) == 1, "Level-three pots must draw once each");
        require(GoldPotWishRules.eligiblePots(100, 64) == 64, "Bulk pots must draw independently");
        require(GoldPotWishRules.eligiblePots(Long.MAX_VALUE, 1) == 1, "Eligibility must not overflow");
        expectInvalid(() -> GoldPotWishRules.categoryForRoll(-1));
        expectInvalid(() -> GoldPotWishRules.categoryForRoll(10000));
        expectInvalid(() -> GoldPotWishRules.eligiblePots(-1, 1));
        expectInvalid(() -> GoldPotWishRules.eligiblePots(100, 0));
        expectInvalid(() -> GoldPotWishRules.eligiblePots(100, 65));
        System.out.println("Gold-pot wishes verified: all 10000 tickets, exact 0.01% manor odds, 25 expected coins, upgrade/bulk boundaries.");
    }

    private static void expectInvalid(Runnable operation) {
        try { operation.run(); } catch (IllegalArgumentException expected) { return; }
        throw new IllegalStateException("Invalid wish input was accepted");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
