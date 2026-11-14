package net.farmingtales.ftboss.util;

public final class QingyiConfluenceRules {
    public static final int CHARGE_TICKS = 32;
    public static final int IMPACT_TICKS = 4;
    public static final int RELEASE_TICKS = 16;
    public static final int ATTACK_TICKS = 10;
    public static final int THROW_CHARGE_TICKS = 10;
    public static final int THROW_USE_TICKS = 72000;
    public static final int COOLDOWN_TICKS = 160;
    public static final int DEBUFF_TICKS = 80;
    public static final double RANGE = 24.0;
    public static final double RADIUS = 3.5;
    public static final double HALF_HEIGHT = 3.0;
    /**
     * 玩家【四象归墟】的基础伤害 —— <b>2026-09-18 按用户要求「青衣的掉落三叉戟伤害翻倍」由 24.0 翻到 48.0</b>。
     *
     * <p>玩家结算使用固定最终基础伤害 144；保留 48 作为兼容用的原始参数。
     * 所以玩家看到的最终值是 {@code 48.0 × 1.5 = 72}；tooltip 里写的也是 72。
     *
     * <p>注意区分：{@link #BOSS_DAMAGE} 是**青衣本体**放同一招时用的基准，那是 Boss 技能，
     * 属于隐藏 Boss 的伤害体系；本体结算使用固定基础伤害 54，再叠加实时成长。
     */
    public static final float PLAYER_DAMAGE = 48.0F;
    public static final float BOSS_DAMAGE = 12.0F;

    private QingyiConfluenceRules() {}

    public static boolean canThrow(int remaining) {
        return remaining >= 0 && remaining <= THROW_USE_TICKS - THROW_CHARGE_TICKS;
    }

    public static long skillCooldownRemaining(long now, long readyAt) {
        return Math.max(0L, Math.min(COOLDOWN_TICKS, readyAt - now));
    }

    public static int useTicks() {
        return CHARGE_TICKS + IMPACT_TICKS;
    }

    public static boolean impactAt(int elapsed) {
        return elapsed == useTicks();
    }

    public static boolean finished(int elapsed) {
        return elapsed >= CHARGE_TICKS + RELEASE_TICKS;
    }

    public static boolean inside(double offsetX, double offsetY, double offsetZ) {
        return Double.isFinite(offsetX) && Double.isFinite(offsetY) && Double.isFinite(offsetZ)
                && Math.abs(offsetY) <= HALF_HEIGHT
                && offsetX * offsetX + offsetZ * offsetZ <= RADIUS * RADIUS;
    }
}
