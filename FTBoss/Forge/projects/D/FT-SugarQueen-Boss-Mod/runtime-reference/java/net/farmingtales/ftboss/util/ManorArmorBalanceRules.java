/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class ManorArmorBalanceRules {
    private ManorArmorBalanceRules() {
    }

    public static int armor(int n, boolean bl, boolean bl2, int n2) {
        if (bl || bl2 || n < 0 || n > 11 || n == 9) {
            return n2;
        }
        return Math.max(0, n2) / 2;
    }
}

