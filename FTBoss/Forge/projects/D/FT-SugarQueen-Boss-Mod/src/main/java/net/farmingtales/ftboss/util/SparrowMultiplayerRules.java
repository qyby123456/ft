package net.farmingtales.ftboss.util;

import java.util.Random;
import java.util.UUID;

public final class SparrowMultiplayerRules {
    private SparrowMultiplayerRules() {}

    public static int quota(int players) {
        return Math.max(1, players);
    }

    public static double standaloneChance(int players) {
        return 1.0 - Math.pow(0.5, quota(players));
    }

    public static double additionalStageChance(int players) {
        return 1.0 - Math.pow(0.75, (double) quota(players) - 1);
    }

    public static boolean planned(UUID fight, int variant, boolean original, int players) {
        if (fight == null || SparrowAppearanceRules.bit(variant) == 0) return false;
        if (original) return true;
        if (players <= 1) return false;
        long seed = fight.getMostSignificantBits() ^ Long.rotateLeft(fight.getLeastSignificantBits(), 17)
                ^ (0x9e3779b97f4a7c15L * (variant + 1));
        return new Random(seed).nextDouble() < additionalStageChance(players);
    }

    public static boolean canAppear(int used, int players) {
        return Math.max(0, used) < quota(players);
    }
}
