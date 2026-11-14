/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.dandaniron;

public final class IronLifecycle {
    private IronLifecycle() {
    }

    public static boolean stale(String string, long l, long l2, String string2, long l3, long l4) {
        return string.equals(string2) && l2 > l4 || !string.equals("CHARGE") && !string2.equals("CHARGE") && l > l3;
    }

    public static boolean replaces(String string, long l, String string2, long l2) {
        if (string.equals(string2)) {
            return true;
        }
        if (string2.equals("RELEASE")) {
            return !string.equals("BEAM") && l <= l2;
        }
        return string2.equals("PULSE") && string.equals("RELEASE") && l <= l2;
    }

    public static boolean cancels(String string, long l, long l2) {
        return string.equals("CHARGE") && l <= l2;
    }
}

