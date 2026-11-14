package net.farmingtales.ftboss.util;

import java.util.HashSet;
import java.util.Set;

public final class ManorDamageRules {
    public static final float MAX_FINAL_HIT = Float.MAX_VALUE;
    public static final int ARENA_RADIUS = 5;

    private ManorDamageRules() {}

    public static boolean ignored(float damage) {
        return !Float.isFinite(damage);
    }

    public static float remainingDamage(float damage, boolean sparrow, boolean youzi) {
        if (sparrow) damage -= damage * 0.5F;
        if (youzi) damage -= damage * 0.1F;
        return damage;
    }

    public static long chunkKey(int chunkX, int chunkZ) {
        return (long) chunkX & 0xFFFFFFFFL | ((long) chunkZ & 0xFFFFFFFFL) << 32;
    }

    public static Set<Long> arenaChunks(Set<Long> centers) {
        Set<Long> chunks = new HashSet<>();
        for (long center : centers) {
            int centerX = (int) center;
            int centerZ = (int) (center >> 32);
            for (int offsetX = -ARENA_RADIUS; offsetX <= ARENA_RADIUS; offsetX++) {
                for (int offsetZ = -ARENA_RADIUS; offsetZ <= ARENA_RADIUS; offsetZ++) {
                    chunks.add(chunkKey(centerX + offsetX, centerZ + offsetZ));
                }
            }
        }
        return chunks;
    }
}
