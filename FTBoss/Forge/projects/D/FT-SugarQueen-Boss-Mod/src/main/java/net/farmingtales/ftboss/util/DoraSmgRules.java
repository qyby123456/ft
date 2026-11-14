package net.farmingtales.ftboss.util;

public final class DoraSmgRules {
    public static final int MAGAZINE_SIZE = 30;
    public static final int SHOT_INTERVAL = 2;
    public static final int RELOAD_TICKS = 36;
    public static final double RANGE = 28;
    public static final double EFFECTIVE_RANGE = 14;
    /** 单发技能基础伤害（最初版本的原始值）；实际结算走 {@link #damage(double)}。 */
    public static final float DAMAGE = 5;

    /**
     * 鸣脉冲锋枪（dora_nova_launcher）单发伤害倍率 —— <b>2026-09-18 用户口径：「翻倍」</b>。
     *
     * <p>与 {@link ManorWeaponRules#SKILL_DAMAGE_MULTIPLIER}（1.5）是<b>替换</b>关系不是叠乘：
     * <b>2.0</b> 就是相对最初版本的总倍率，原来那轮 ×1.5 已被它覆盖。
     */
    public static final float DAMAGE_MULTIPLIER = 4.0F;

    private DoraSmgRules() {}

    public static boolean shouldDrop(int variant, boolean mini, boolean giant, boolean noAi, boolean finale, boolean playerKill) {
        return variant == 2 && !mini && !giant && !noAi && !finale && playerKill;
    }

    public static int rounds(int stored) {
        return Math.max(0, Math.min(MAGAZINE_SIZE, stored));
    }

    public static int reloadTicks(int stored) {
        return Math.max(0, Math.min(RELOAD_TICKS, stored));
    }

    public static float damage(double distance) {
        if (!Double.isFinite(distance) || distance < 0 || distance > RANGE) return 0;
        double falloff = Math.max(0, (distance - EFFECTIVE_RANGE) / (RANGE - EFFECTIVE_RANGE));
        return (float) (20.0F * (1 - 0.5 * falloff));
    }
}
