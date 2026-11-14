package net.farmingtales.ftboss.util;

import java.util.HashSet;
import java.util.Set;

public final class ManorDamageRules {
    public static final float MAX_FINAL_HIT = 300.0F;
    public static final int ARENA_RADIUS = 5;

    private ManorDamageRules() {
    }

    public static boolean ignored(float var0) {
        return !Float.isFinite(var0) || var0 > 300.0F;
    }

    public static float remainingDamage(float var0, boolean var1, boolean var2) {
        if (var1) {
            var0 -= var0 * 0.5F;
        }

        if (var2) {
            var0 -= var0 * 0.1F;
        }

        return var0;
    }

    public static long chunkKey(int var0, int var1) {
        return (long)var0 & 4294967295L | ((long)var1 & 4294967295L) << 32;
    }

    public static Set<Long> arenaChunks(Set<Long> var0) {
        Set<Long> var1 = new HashSet<>();

        for (long var3 : var0) {
            int var5 = (int)var3;
            int var6 = (int)(var3 >> 32);

            for (int var7 = -5; var7 <= 5; var7++) {
                for (int var8 = -5; var8 <= 5; var8++) {
                    var1.add(chunkKey(var5 + var7, var6 + var8));
                }
            }
        }

        return var1;
    }
}
