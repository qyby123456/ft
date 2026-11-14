package net.farmingtales.ftboss.util;

public final class MaqueFeatherRules {
    public static final int COOLDOWN_TICKS = 20 * 20;
    public static final int PASSIVE_INTERVAL_TICKS = 4 * 20;
    public static final int BLESSING_TICKS = 6 * 20;
    public static final int WIND_TICKS = 6 * 20;
    public static final float PASSIVE_HEAL = 2.0F;
    public static final float BURST_HEAL = 16.0F;
    public static final double RADIUS = 6.0;
    public static final int MAX_TARGETS = 16;

    private MaqueFeatherRules() {}

    /** 连续手持的周期治疗（庄园技能倍率已计入）。 */
    public static float passiveHeal() {
        return 3.0F;
    }

    /** 右键群体回春的单体治疗量（庄园技能倍率已计入）。 */
    public static float burstHeal() {
        return 24.0F;
    }

    public static boolean shouldDrop(int variant, boolean mini, boolean giant, boolean recentlyHit) {
        return variant == 9 && !mini && !giant && recentlyHit;
    }

    public static int remainingCooldown(long now, long until) {
        if (until <= now) return 0;
        return (int) Math.min(COOLDOWN_TICKS, until - now);
    }

    public static boolean continuousHold(long now, long previous) {
        return previous == now - 1;
    }

    public static boolean canSupport(boolean self, boolean alive, boolean spectator, boolean alliedPlayer,
                                     boolean ownedPet, double distanceSquared, boolean visible) {
        return alive && !spectator && (self || ((alliedPlayer || ownedPet)
                && distanceSquared <= RADIUS * RADIUS && visible));
    }
}
