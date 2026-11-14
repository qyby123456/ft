/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.saya;

import net.farmingtales.ftboss.saya.SayaCastMotion;

public final class SayaCastLifecycle {
    private SayaCastLifecycle() {
    }

    public static boolean cancelWindup(String string, double d, boolean bl) {
        String[] stringArray = SayaCastMotion.decode(string);
        return stringArray != null && d < (double)Integer.parseInt(stringArray[1]) && !bl;
    }

    public static boolean clearCharge(String string, String string2, double d, double d2) {
        if (d2 < 3.0) {
            return false;
        }
        String[] stringArray = SayaCastMotion.decode(string2);
        return stringArray == null || !stringArray[0].equals(string) || d >= (double)Integer.parseInt(stringArray[1]);
    }

    public static double chargeProgress(String string, String string2, double d, double d2) {
        String[] stringArray = SayaCastMotion.decode(string2);
        return stringArray != null && stringArray[0].equals(string) ? Math.max(0.0, Math.min(1.0, d / (double)Integer.parseInt(stringArray[1]))) : d2;
    }

    public static int chargeDuration(String string, String string2, int n) {
        String[] stringArray = SayaCastMotion.decode(string2);
        return stringArray != null && stringArray[0].equals(string) ? Integer.parseInt(stringArray[1]) : n;
    }
}

