/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class FoodWeaponDamageAccounting {
    private FoodWeaponDamageAccounting() {
    }

    public static boolean positive(double d) {
        return Double.isFinite(d) && d > 0.0;
    }

    public static Ledger ledger(double d, double d2, double d3, double d4, double d5, double d6, double d7) {
        if (!(Double.isFinite(d) && !(d < 0.0) && FoodWeaponDamageAccounting.positive(d2) && FoodWeaponDamageAccounting.positive(d3) && FoodWeaponDamageAccounting.positive(d4) && FoodWeaponDamageAccounting.positive(d5) && FoodWeaponDamageAccounting.positive(d6) && FoodWeaponDamageAccounting.positive(d7))) {
            throw new IllegalArgumentException("Invalid damage ledger");
        }
        double d8 = d * d2;
        double d9 = d8 * d3;
        double d10 = d9 * d4;
        double d11 = d2 / d5 * (d3 / d6) * (d4 / d7);
        if (!Double.isFinite(d10) || !FoodWeaponDamageAccounting.positive(d11)) {
            throw new IllegalArgumentException("Damage ledger overflow");
        }
        return new Ledger(d, d8 - d, d9 - d8, d10 - d9, d10, d11);
    }

    public static float scale(float f, double d) {
        if (!FoodWeaponDamageAccounting.positive(f) || !FoodWeaponDamageAccounting.positive(d)) {
            return 0.0f;
        }
        return (float)Math.min(3.4028234663852886E38, (double)f * d);
    }

    public record Ledger(double nativeAmount, double materialIncrement, double evolutionIncrement, double rarityIncrement, double total, double residual) {
    }
}

