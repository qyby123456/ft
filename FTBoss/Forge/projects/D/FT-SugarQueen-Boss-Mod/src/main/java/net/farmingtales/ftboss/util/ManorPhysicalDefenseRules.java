package net.farmingtales.ftboss.util;

import java.util.Arrays;

public final class ManorPhysicalDefenseRules {
    public static final int WINDOW_TICKS = 20;
    public static final float MAX_PHYSICAL_DAMAGE = 200.0F;

    private ManorPhysicalDefenseRules() {}

    public static boolean supports(int variant) {
        return switch (variant) {
            case 0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11 -> true;
            default -> false;
        };
    }

    public static boolean halfHealth(float health, float maximum) {
        return Float.isFinite(health) && Float.isFinite(maximum)
                && health > 0.0F && maximum > 0.0F && health <= maximum * 0.5F;
    }

    public static final class Window {
        private final long[] ticks;
        private final long[] amounts;
        private long lastTick;

        public Window(long[] ticks, long[] amounts, long lastTick) {
            this.ticks = ticks.length == WINDOW_TICKS ? ticks.clone() : new long[WINDOW_TICKS];
            this.amounts = amounts.length == WINDOW_TICKS ? amounts.clone() : new long[WINDOW_TICKS];
            this.lastTick = lastTick;
        }

        public float take(long now, float requested) {
            if (!Float.isFinite(requested) || requested <= 0.0F) return 0.0F;
            if (now < lastTick) {
                Arrays.fill(ticks, 0L);
                Arrays.fill(amounts, 0L);
            }
            lastTick = now;
            double spent = 0.0;
            for (int index = 0; index < WINDOW_TICKS; index++) {
                double damage = Double.longBitsToDouble(amounts[index]);
                long elapsed = now - ticks[index];
                if (!Double.isFinite(damage) || damage < 0.0 || ticks[index] > now
                        || elapsed < 0L || elapsed >= WINDOW_TICKS) {
                    amounts[index] = 0L;
                } else {
                    spent += damage;
                }
            }
            double remaining = Math.max(0.0, MAX_PHYSICAL_DAMAGE - spent);
            float accepted = (float) Math.min(requested, remaining);
            if (accepted > remaining) accepted = Math.nextDown(accepted);
            if (accepted <= 0.0F) return 0.0F;
            int slot = Math.floorMod(now, WINDOW_TICKS);
            double damage = ticks[slot] == now ? Double.longBitsToDouble(amounts[slot]) : 0.0;
            ticks[slot] = now;
            amounts[slot] = Double.doubleToRawLongBits(damage + accepted);
            return accepted;
        }

        public long[] ticks() { return ticks.clone(); }
        public long[] amounts() { return amounts.clone(); }
        public long lastTick() { return lastTick; }
    }
}
