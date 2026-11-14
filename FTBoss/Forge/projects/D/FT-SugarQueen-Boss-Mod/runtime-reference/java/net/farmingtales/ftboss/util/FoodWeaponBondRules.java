/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class FoodWeaponBondRules {
    public static final double MELEE_ECHO_RANGE = 6.0;
    public static final double RANGED_ECHO_RANGE = 24.0;

    private FoodWeaponBondRules() {
    }

    public static boolean positiveDamage(float f, boolean bl, boolean bl2, boolean bl3) {
        return Float.isFinite(f) && f > 0.0f && !bl && bl2 && !bl3;
    }

    public static Kind hitKind(String string, String string2, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        if (string == null || string2 == null) {
            return null;
        }
        if (bl) {
            return string2.equals(string) && (string.equals("MELEE") || string.equals("BOW") || string.equals("STAFF")) ? Kind.MELEE : null;
        }
        if (string2.equals("TRIDENT")) {
            return string.equals("MELEE") && bl2 && bl3 && bl4 ? Kind.TRIDENT : null;
        }
        if (bl2) {
            return null;
        }
        if (string.equals("BOW") && (string2.isEmpty() || string2.equals("BOW"))) {
            return bl3 ? Kind.BOW : null;
        }
        if (string.equals("STAFF") && (string2.isEmpty() || string2.equals("STAFF"))) {
            return Kind.STAFF;
        }
        return null;
    }

    public static double echoRange(Kind kind) {
        return kind == Kind.BOW || kind == Kind.STAFF || kind == Kind.TRIDENT ? 24.0 : 6.0;
    }

    public static enum Kind {
        MELEE,
        BOW,
        STAFF,
        TRIDENT;

    }
}

