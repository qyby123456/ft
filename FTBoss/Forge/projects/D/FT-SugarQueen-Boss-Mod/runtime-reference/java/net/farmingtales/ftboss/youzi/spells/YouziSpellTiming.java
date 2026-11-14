/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.youzi.spells;

import net.farmingtales.ftboss.youzi.spells.YouziVisibility;

public final class YouziSpellTiming {
    public static boolean eventFresh(long l, long l2, String string) {
        long l3 = l - l2;
        return l3 >= -2L && l3 <= (long)(string.equals("BEAM") ? 4 : (string.equals("CHARGE") ? 8 : 100));
    }

    public static boolean ordered(long l, String string, long l2, String string2) {
        if (l != l2) {
            return l > l2;
        }
        return YouziSpellTiming.order(string) > YouziSpellTiming.order(string2);
    }

    private static int order(String string) {
        return string.equals("CHARGE") ? 0 : (string.equals("RELEASE") ? 1 : (string.equals("PULSE") ? 2 : 3));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static boolean expired(String string, String string2, long l, long l2, long l3, boolean bl, boolean bl2, double d) {
        if (!bl) return true;
        if (l < l2) {
            return true;
        }
        if (string2.equals("CHARGE")) {
            if (l - l3 <= 8L) return false;
            return true;
        }
        if (string.equals("effigy_ward")) {
            if (l - l2 > 370L) return true;
            if (l - l2 <= 4L) return false;
            if (bl2) return false;
            return true;
        }
        if (l - l2 >= (long)(string.equals("effigy_volley") ? 40 : 60)) return true;
        if (!(d > 48.0)) return false;
        return true;
    }

    public static Sample sample(String string, boolean bl, double d, double d2, double d3, double d4) {
        if (bl) {
            return new Sample("charge", Math.max(0.0, Math.min(1.0, d2)) * d3, 1.0);
        }
        double d5 = string.equals("effigy_volley") ? 1.0417 : 0.0;
        String string2 = d + d5 < d4 ? "release" : "sustain";
        double d6 = string2.equals("release") ? d + d5 : d - d4 + d5;
        double d7 = ((double)(string.equals("effigy_volley") ? 40 : 60) - d * 20.0) / 8.0;
        return new Sample(string2, Math.max(0.0, d6), string.equals("effigy_ward") ? 1.0 : YouziVisibility.smooth(d7));
    }

    public record Sample(String channel, double seconds, double opacity) {
    }
}

