package net.farmingtales.ftboss.util;

/**
 * 玩家【河豚棒】的技能数值与几何规则（2026-09-18）。
 *
 * <p>技能原型取自<b>紫菜 Boss 本体</b>的毒雾招：4.5 格半径、14 点基础伤害、
 * 中毒 III 10 秒 + 缓慢 II 6 秒。玩家版按用户口径做成<b>持续毒雾场</b>：
 * 右键在落点留下一团毒云，停留 {@link #CLOUD_TICKS} 刻，每 {@link #TICK_INTERVAL} 刻结算一次，
 * 所以一共 {@link #damageTicks()} 跳。敌人必须走出范围才停伤。
 *
 * <p>伤害走 {@link ManorWeaponRules#skillDamage(float)}，与庄园武器 2026-09-18 的
 * 「技能 ×1.5」口径一致（14 → 21）：以后调倍率这一个数字就会自动跟上。
 *
 * <p>本类只放常量与纯函数，不引用 Minecraft 类型，方便无引导 JVM 里直接跑校验。
 */
public final class PufferBatRules {
    /** 落点毒云的半径（格）——与紫菜 Boss 原招一致。 */
    public static final double CLOUD_RADIUS = 4.5;
    /** 毒云存在时长（tick）：5 秒。 */
    public static final int CLOUD_TICKS = 100;
    /** 每跳伤害的间隔（tick）：1 秒一跳。 */
    public static final int TICK_INTERVAL = 20;
    /** 施放距离（格）：瞄准点超出这个距离就贴在射线上。 */
    public static final double CAST_RANGE = 6.0;
    /** 冷却（tick）：9 秒。 */
    public static final int COOLDOWN_TICKS = 180;
    /** 耗耐久。 */
    public static final int DURABILITY_COST = 3;
    /** 同时存在的毒云上限（每个玩家）：防止反复空放堆满实体扫描。 */
    public static final int MAX_ACTIVE_CLOUDS = 4;
    /** 毒云网格刷新间隔；客户端效果会跨过这个间隔，不会闪断。 */
    public static final int CLOUD_VISUAL_REFRESH_TICKS = 20;
    /** 总伤害上限保护：单次施法的跳伤总和不会超过这个值。 */
    public static final float TOTAL_DAMAGE_CAP = 256.0F;

    /** 潜行右键「毒刺膨爆」半径。 */
    public static final double SHOCKWAVE_RADIUS = 5.5;
    /** 毒刺膨爆基础伤害，仍走庄园技能倍率。 */
    public static final float SHOCKWAVE_BASE_DAMAGE = 18.0F;
    /** 已中毒目标承受的额外倍率。 */
    public static final float SHOCKWAVE_POISONED_MULTIPLIER = 1.5F;
    /** 毒刺膨爆冷却：12 秒。 */
    public static final int SHOCKWAVE_COOLDOWN_TICKS = 240;
    /** 毒刺膨爆耐久消耗。 */
    public static final int SHOCKWAVE_DURABILITY_COST = 4;
    /** 毒刺膨爆施加中毒 II 6 秒。 */
    public static final int SHOCKWAVE_POISON_TICKS = 120;
    public static final int SHOCKWAVE_POISON_AMPLIFIER = 1;
    /** 水平击退与上抛力度。 */
    public static final double SHOCKWAVE_KNOCKBACK = 1.15;
    public static final double SHOCKWAVE_LIFT = 0.25;

    /** 每跳的基础伤害（紫菜 Boss 原招的 14 点）。 */
    public static final float TICK_BASE_DAMAGE = 14.0F;
    /** 中毒等级：II（amp 1）。 */
    public static final int POISON_AMPLIFIER = 1;
    /** 中毒时长（tick）：10 秒，与 Boss 原招一致。 */
    public static final int POISON_TICKS = 200;
    /** 缓慢等级：I（amp 0）——比 Boss 原招的 II 温和一档。 */
    public static final int SLOW_AMPLIFIER = 0;
    /** 缓慢时长（tick）：6 秒，与 Boss 原招一致。 */
    public static final int SLOW_TICKS = 120;

    private PufferBatRules() {}

    /** 单跳实际伤害（庄园技能倍率已计入）。 */
    public static float tickDamage() {
        return 42.0F;
    }

    /** 毒刺膨爆实际伤害；先判断目标原本是否中毒，再续上新毒。 */
    public static float shockwaveDamage(boolean poisoned) {
        float damage = 54.0F;
        return poisoned ? damage * SHOCKWAVE_POISONED_MULTIPLIER : damage;
    }

    /** 一团毒云最多跳几次。 */
    public static int damageTicks() {
        return Math.max(1, CLOUD_TICKS / TICK_INTERVAL);
    }

    /** 第 {@code index} 跳（从 0 起）距离施放时刻的 tick 偏移。 */
    public static int tickOffset(int index) {
        return Math.max(0, index) * TICK_INTERVAL;
    }

    /**
     * 本次施放对<b>单个目标</b>的理论总伤害（全部跳数打满时），并受
     * {@link #TOTAL_DAMAGE_CAP} 保护。用于 tooltip 与回归断言，不是保证命中值。
     */
    public static float totalDamage() {
        return Math.min(TOTAL_DAMAGE_CAP, tickDamage() * damageTicks());
    }

    /** 目标是否落在毒云内（横向圆 + 竖向柱，容忍高差）。 */
    public static boolean inside(double offsetX, double offsetY, double offsetZ, double radius) {
        return Double.isFinite(offsetX) && Double.isFinite(offsetY) && Double.isFinite(offsetZ)
                && Double.isFinite(radius) && radius > 0
                && Math.abs(offsetY) <= 3.0
                && offsetX * offsetX + offsetZ * offsetZ <= radius * radius;
    }

    /** 瞄准点是否可用（有限坐标）。 */
    public static boolean validCast(double x, double y, double z) {
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
    }

    /**
     * 把过远的瞄准点收到 {@link #CAST_RANGE} 内的落点。
     * 返回 {@code {x, y, z}}；输入非法时返回 {@code null}。
     */
    public static double[] castPoint(double originX, double originY, double originZ,
                                     double targetX, double targetY, double targetZ) {
        if (!validCast(originX, originY, originZ) || !validCast(targetX, targetY, targetZ)) return null;
        double dx = targetX - originX;
        double dy = targetY - originY;
        double dz = targetZ - originZ;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= CAST_RANGE || length <= 1.0E-6) return new double[]{targetX, targetY, targetZ};
        double scale = CAST_RANGE / length;
        return new double[]{originX + dx * scale, originY + dy * scale, originZ + dz * scale};
    }
}
