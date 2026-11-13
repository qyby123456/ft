package cn.farmingtales.sdmshopaudit;

import com.google.gson.JsonArray;

public final class SecurityRules {
    private SecurityRules() {
    }

    public static long quote(long price, int batches, int units) {
        if (batches < 1 || batches > 1024) {
            throw new IllegalArgumentException("INVALID_BATCH_COUNT");
        }
        if (price < 0) {
            throw new IllegalArgumentException("NEGATIVE_PRICE");
        }
        if (units < 1 || (long) units * batches > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("INVALID_OR_OVERFLOWING_ITEM_COUNT");
        }
        try {
            return Math.multiplyExact(price, batches);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("PRICE_OVERFLOW", exception);
        }
    }

    public static void balance(long balance, long amount, boolean sell, boolean itemCurrency) {
        if (balance < 0 || amount < 0) {
            throw new IllegalArgumentException("NEGATIVE_BALANCE_OR_PAYMENT");
        }
        if (itemCurrency && amount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("ITEM_CURRENCY_AMOUNT_OVERFLOW");
        }
        if (!sell && balance < amount) {
            throw new IllegalArgumentException("INSUFFICIENT_BALANCE");
        }
        if (sell && (amount > Long.MAX_VALUE - balance
                || itemCurrency && amount > Integer.MAX_VALUE - balance)) {
            throw new IllegalArgumentException("BALANCE_OVERFLOW");
        }
    }

    public static void limit(int used, int limit, int batches) {
        if (used < 0 || limit < 0 || (long) used + batches > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("INVALID_OR_OVERFLOWING_LIMIT");
        }
        if (limit != 0 && (long) used + batches > limit) {
            throw new IllegalArgumentException("LIMIT_EXCEEDED");
        }
    }

    public static String commandFingerprint(String command, boolean elevated, boolean silent) {
        JsonArray definition = new JsonArray();
        definition.add(command);
        definition.add(elevated);
        definition.add(silent);
        return ShopSnapshot.digest(definition.toString());
    }
}
