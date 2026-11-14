/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.HashSet;
import java.util.Set;

public final class ManorDamageRules {
    public static final float MAX_FINAL_HIT = 300.0f;
    public static final int ARENA_RADIUS = 5;

    private ManorDamageRules() {
    }

    public static boolean ignored(float f) {
        return !Float.isFinite(f) || f > 300.0f;
    }

    public static float remainingDamage(float f, boolean bl, boolean bl2) {
        if (bl) {
            f -= f * 0.5f;
        }
        if (bl2) {
            f -= f * 0.1f;
        }
        return f;
    }

    public static long chunkKey(int n, int n2) {
        return (long)n & 0xFFFFFFFFL | ((long)n2 & 0xFFFFFFFFL) << 32;
    }

    public static Set<Long> arenaChunks(Set<Long> set) {
        HashSet<Long> hashSet = new HashSet<Long>();
        for (long l : set) {
            int n = (int)l;
            int n2 = (int)(l >> 32);
            for (int i = -5; i <= 5; ++i) {
                for (int j = -5; j <= 5; ++j) {
                    hashSet.add(ManorDamageRules.chunkKey(n + i, n2 + j));
                }
            }
        }
        return hashSet;
    }
}

