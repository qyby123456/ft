package net.farmingtales.ftboss.util;

public final class ManorRewardBalanceRules {
    public static final float PLAYER_DAMAGE_SCALE = 0.5F;

    private ManorRewardBalanceRules() {}

    public static int mana(String identifier, int original) {
        if (original == 400 || identifier.equals("qizai_star_inferno") || identifier.equals("qizai_laser_barrage")) return original;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, (long) original * 2));
    }

    public static float damage(boolean player, boolean rewardSpell, float original) {
        return player && rewardSpell && original > 0.0F ? original * PLAYER_DAMAGE_SCALE : original;
    }
}
