/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.dandanironmotion;

public final class IronActorRules {
    private IronActorRules() {
    }

    public static boolean autonomous(String string) {
        return string.equals("PULSE") || string.equals("BEAM");
    }

    public static boolean stale(String string, long l, long l2, String string2, long l3, long l4) {
        return l > l3 || l == l3 && (l2 > l4 || string.equals("RELEASE") && string2.equals("CHARGE"));
    }

    public static boolean expires(boolean bl, long l, long l2, boolean bl2, long l3, int n, long l4) {
        return bl ? l - l2 >= 7L : l >= l3 + (long)n || bl2 && l - l4 > 8L;
    }

    public static boolean nativeHasPriority(long l, int n, long l2, double d) {
        return l >= l2 && n > 0 && d >= (double)l && d < (double)(l + (long)n);
    }
}

