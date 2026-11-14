package net.farmingtales.ftboss.util;

import java.util.Arrays;

public final class ManorDamageLimitRules {
    public static final int WINDOW_TICKS = 20;
    public static final int OTHER_DAMAGE = 0;
    public static final int PHYSICAL_DAMAGE = 1;
    public static final int MAGIC_DAMAGE = 2;
    public static final float MAX_HIT_DAMAGE = 100.0F;
    public static final float MAX_KIND_DAMAGE = Float.MAX_VALUE;
    public static final float MAX_TOTAL_DAMAGE = 400.0F;
    private static final int DAMAGE_KINDS = 3;

    private ManorDamageLimitRules() {}

    public static final class Window {
        private final long[] ticks;
        private final long[] amounts;
        private long lastTick;

        public Window(long[] ticks, long[] amounts, long lastTick) {
            this.ticks = ticks.length == WINDOW_TICKS ? ticks.clone() : new long[WINDOW_TICKS];
            this.amounts = amounts.length == WINDOW_TICKS * DAMAGE_KINDS
                    ? amounts.clone() : new long[WINDOW_TICKS * DAMAGE_KINDS];
            this.lastTick = lastTick;
        }

        public float take(long now, int kind, float requested) {
            return take(now, kind, requested, MAX_HIT_DAMAGE, MAX_KIND_DAMAGE, MAX_TOTAL_DAMAGE);
        }

        public float take(long now, int kind, float requested, float hitLimit, float kindLimit, float totalLimit) {
            if (!Float.isFinite(requested) || requested <= 0.0F) return 0.0F;
            if (kind < OTHER_DAMAGE || kind > MAGIC_DAMAGE) kind = OTHER_DAMAGE;
            if (now < lastTick) {
                Arrays.fill(ticks, 0L);
                Arrays.fill(amounts, 0L);
            }
            lastTick = now;
            double totalSpent = 0.0;
            double kindSpent = 0.0;
            for (int slot = 0; slot < WINDOW_TICKS; slot++) {
                long elapsed = now - ticks[slot];
                for (int damageKind = 0; damageKind < DAMAGE_KINDS; damageKind++) {
                    int index = slot * DAMAGE_KINDS + damageKind;
                    double damage = Double.longBitsToDouble(amounts[index]);
                    if (!Double.isFinite(damage) || damage < 0.0 || ticks[slot] > now
                            || elapsed < 0L || elapsed >= WINDOW_TICKS) {
                        amounts[index] = 0L;
                    } else {
                        totalSpent += damage;
                        if (damageKind == kind) kindSpent += damage;
                    }
                }
            }
            double remaining = Math.min(hitLimit, Math.max(0.0, totalLimit - totalSpent));
            if (kind != OTHER_DAMAGE) {
                remaining = Math.min(remaining, Math.max(0.0, kindLimit - kindSpent));
            }
            float accepted = (float) Math.min(requested, remaining);
            if (accepted > remaining) accepted = Math.nextDown(accepted);
            if (accepted <= 0.0F) return 0.0F;
            int slot = Math.floorMod(now, WINDOW_TICKS);
            if (ticks[slot] != now) {
                Arrays.fill(amounts, slot * DAMAGE_KINDS, (slot + 1) * DAMAGE_KINDS, 0L);
                ticks[slot] = now;
            }
            int index = slot * DAMAGE_KINDS + kind;
            amounts[index] = Double.doubleToRawLongBits(Double.longBitsToDouble(amounts[index]) + accepted);
            return accepted;
        }

        public long[] ticks() { return ticks.clone(); }
        public long[] amounts() { return amounts.clone(); }
        public long lastTick() { return lastTick; }
    }
}
