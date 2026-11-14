/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.saya;

import java.util.Arrays;
import net.farmingtales.ftboss.saya.SayaCastData;

public final class SayaCastMotion {
    public static final int RECOVERY_TICKS = 8;
    private static final String PREFIX = "saya_cast/";

    private SayaCastMotion() {
    }

    public static int index(String string) {
        for (int i = 0; i < SayaCastData.SPELLS.length; ++i) {
            if (!SayaCastData.SPELLS[i].equals(string)) continue;
            return i;
        }
        return -1;
    }

    public static String encode(String string, int n) {
        if (SayaCastMotion.index(string) < 0 || n < 1 || n > 1200) {
            throw new IllegalArgumentException("Invalid Saya casting action");
        }
        return PREFIX + string + "/" + n;
    }

    public static String[] decode(String string) {
        if (string == null || !string.startsWith(PREFIX)) {
            return null;
        }
        String[] stringArray = string.substring(PREFIX.length()).split("/");
        if (stringArray.length != 2 || SayaCastMotion.index(stringArray[0]) < 0) {
            return null;
        }
        try {
            int n = Integer.parseInt(stringArray[1]);
            return n > 0 && n <= 1200 ? stringArray : null;
        }
        catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    public static double smooth(double d) {
        d = Math.max(0.0, Math.min(1.0, d));
        return d * d * (3.0 - 2.0 * d);
    }

    public static double[] sample(String string, double d, int n) {
        double[] dArray = new double[18];
        SayaCastMotion.sample(SayaCastMotion.index(string), d, n, dArray);
        return dArray;
    }

    public static void sample(int n, double d, int n2, double[] dArray) {
        Arrays.fill(dArray, 0.0);
        if (n < 0 || !Double.isFinite(d) || d <= 0.0 || n2 <= 0 || d >= (double)(n2 + 8)) {
            return;
        }
        double[][] dArray2 = SayaCastData.TRACKS[n][d <= (double)n2 ? 0 : 1];
        double d2 = d <= (double)n2 ? d / (double)n2 : (d - (double)n2) / 8.0;
        for (int i = 1; i < dArray2.length; ++i) {
            if (d2 > dArray2[i][0]) continue;
            double[] dArray3 = dArray2[i - 1];
            double[] dArray4 = dArray2[i];
            double d3 = SayaCastMotion.smooth((d2 - dArray3[0]) / (dArray4[0] - dArray3[0]));
            for (int j = 0; j < dArray.length; ++j) {
                dArray[j] = dArray3[j + 1] + (dArray4[j + 1] - dArray3[j + 1]) * d3;
            }
            break;
        }
    }

    public static double blend(double d, int n) {
        return Math.min(SayaCastMotion.smooth(d / Math.min(4.0, (double)n * 0.2)), 1.0 - SayaCastMotion.smooth((d - (double)n - 2.0) / 6.0));
    }
}

