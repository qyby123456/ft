/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Random;
import java.util.UUID;
import net.farmingtales.ftboss.util.SparrowAppearanceRules;

public final class SparrowMultiplayerRules {
    private SparrowMultiplayerRules() {
    }

    public static int quota(int n) {
        return Math.max(1, n);
    }

    public static double standaloneChance(int n) {
        return 1.0 - Math.pow(0.5, SparrowMultiplayerRules.quota(n));
    }

    public static double additionalStageChance(int n) {
        return 1.0 - Math.pow(0.75, (double)SparrowMultiplayerRules.quota(n) - 1.0);
    }

    public static boolean planned(UUID uUID, int n, boolean bl, int n2) {
        if (uUID == null || SparrowAppearanceRules.bit(n) == 0) {
            return false;
        }
        if (bl) {
            return true;
        }
        if (n2 <= 1) {
            return false;
        }
        long l = uUID.getMostSignificantBits() ^ Long.rotateLeft(uUID.getLeastSignificantBits(), 17) ^ -7046029254386353131L * (long)(n + 1);
        return new Random(l).nextDouble() < SparrowMultiplayerRules.additionalStageChance(n2);
    }

    public static boolean canAppear(int n, int n2) {
        return Math.max(0, n) < SparrowMultiplayerRules.quota(n2);
    }
}

