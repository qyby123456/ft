package net.farmingtales.ftboss.util;

public final class XuanlanRules {
    /** 绿枪蓄力满所需刻数。 */
    public static final int CHARGE_TICKS = 20;
    public static final int MIN_CHARGE_TICKS = 4;
    public static final int MARK_TICKS = 120;
    public static final int ENERGY_TICKS = 160;
    public static final int OUTBOUND_TICKS = 10;
    public static final int MAX_FLIGHT_TICKS = 60;
    public static final int MAX_ACTIVE_BLADES = 18;
    public static final int BLADES_PER_VOLLEY = 3;
    public static final int BLADE_COOLDOWN_TICKS = 3;
    public static final int BLADE_SHOT_INTERVAL_TICKS = 1;
    public static final float BLADE_DAMAGE = 10.0F;
    public static final float BLADE_SPREAD_RADIANS = 0.0F;
    public static final double RANGE = 32.0;

    /**
     * 文玄蓝绿枪沿用现行总伤害倍率；白刃使用独立的固定伤害，不经过此倍率。
     *
     * <p>它与 {@link ManorWeaponRules#SKILL_DAMAGE_MULTIPLIER}（1.5）是<b>二选一</b>的替换关系，
     * 不是叠乘：这里直接给出绿枪相对最初版本的现行总倍率，不再额外调用 {@code skillDamage}。
     */
    public static final float WENXUANLAN_DAMAGE_MULTIPLIER = 4.0F;

    private XuanlanRules() {}

    /** 绿枪倍率入口；白刃固定伤害不经过此处。 */
    public static float weaponDamage(float base) {
        return base * WENXUANLAN_DAMAGE_MULTIPLIER;
    }

    public static boolean shouldDrop(int variant, boolean mini, boolean giant, boolean recentlyHit) {
        return variant == 6 && !mini && !giant && recentlyHit;
    }

    public static float shotDamage(int chargeTicks, int energy) {
        return weaponDamage(16.0F + 20.0F * Math.min(CHARGE_TICKS, Math.max(0, chargeTicks)) / CHARGE_TICKS
                + 4.0F * Math.min(3, Math.max(0, energy)));
    }

    public static float bladeDamage(boolean returning, boolean marked) {
        return BLADE_DAMAGE;
    }

    public static boolean active(long now, long until, int maximumDuration) {
        return until > now && until - now <= maximumDuration;
    }
}
