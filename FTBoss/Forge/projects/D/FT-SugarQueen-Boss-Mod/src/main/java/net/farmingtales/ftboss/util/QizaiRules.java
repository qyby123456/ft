package net.farmingtales.ftboss.util;

public final class QizaiRules {
    public static final int FAN = 1;
    public static final int CIRCLE = 2;
    public static final int PRAYER = 3;
    public static final int STEP = 4;
    public static final double STEP_TRIGGER_RANGE = 5;
    public static final float STEP_CHANCE = 0.4F;
    public static final int STEP_RETRY_TICKS = 20;
    public static final int STEP_COOLDOWN = 80;
    public static final double STEP_MIN_DISTANCE = 4;
    public static final double STEP_MAX_DISTANCE = 8;
    public static final int STAR_CROSS = 5;
    public static final int ROSE_STARFALL = 6;
    public static final int RELEASES = 3;
    public static final int REPEAT_INTERVAL = 12;
    public static final int SPELL_DAMAGE_MULTIPLIER = 2;
    public static final double CROSS_LENGTH = 12;
    public static final double CROSS_WIDTH = 1.4;
    public static final double STARFALL_RADIUS = 3.2;
    public static final double STARFALL_OFFSET = 7.6;
    public static final int STARFALL_INTERVAL = 8;
    public static final float CROSS_DAMAGE = 104.0F;
    public static final float STARFALL_DAMAGE = 64.0F;
    public static final int RECOVERY = 24;
    public static final int REST = 32;

    /**
     * ★ v190 按用户要求「祁崽法术冷却减到 1/3，攻击欲望提升」：
     * 两招之间的**休整时间倍率**。3 = 冷却缩到原来的三分之一。
     *
     * <p>为什么用**倍率**而不是把这几个常数逐个除以 3：休整时间散在
     * {@code QizaiMagic} 的好几处（普通招 / 绯星指针 / 天陨星裁各自不同），
     * 逐个改数字迟早会漏一处，而且下次要调就没有单一旋钮了。
     *
     * <p>注意"攻击欲望"在本招里就是**它隔多久出下一次手**：
     * 缩短冷却 = 施法更密 = 压迫感更强。近战挥击本身已经是
     * {@code QingyiCombatRules.MELEE_INTERVAL}(10 tick = 0.5 秒)，
     * 属于原版节奏，本次**没有**再加快（再快会变成一秒四刀，属于另一个量级的改动）。
     */
    public static final int COOLDOWN_MULTIPLIER = 3;

    /** 把一处休整时间按 {@link #COOLDOWN_MULTIPLIER} 缩短（至少 1 tick）。 */
    public static int cooldown(int ticks) {
        return Math.max(1, (ticks + COOLDOWN_MULTIPLIER - 1) / COOLDOWN_MULTIPLIER);
    }
    public static final int STAR_LIFETIME = 60;
    public static final double RANGE = 32;
    public static final double STAR_SPEED = 0.8;
    /* ★ v188 按用户要求「法术半径翻倍」: 以下判定半径全部 ×2（原 2.8 / 3.2 / 6×0.7 / 1.6 / 3.8） */
    public static final double CIRCLE_RADIUS = 5.6;
    public static final double PRAYER_RADIUS = 6.4;
    public static final float STAR_DAMAGE = 48.0F;
    public static final float CIRCLE_DAMAGE = 80.0F;
    public static final float PRAYER_DAMAGE = 128.0F;
    /**
     * 第七招「绯星指针」（2026-09-20 用户要求：参考 astra 的铁魔法特效 + 炼狱游者二阶段的弹幕演出）。
     * 数值与演出细节都在 {@link QizaiSweepRules} 里，这里只登记它的 kind 与出招循环位置。
     */
    public static final int SWEEP = 7;
    /**
     * 第八招「天陨星裁」（2026-09-20 v189 按用户要求：巨型铁魔法 —— 飞上天空、无法选中、
     * 蓄力 5 秒、对半径 48 格玩家造成 500 点固定魔法伤害）。
     * 数值与演出时序全部在 {@link QizaiSkyNovaRules}，这里只登记 kind 与出招循环位置。
     */
    public static final int SKY_NOVA = 8;
    /** Grounded multi-angle tracking and lock-on beam attack. */
    public static final int PRISM_LOCK = 9;
    /** Grounded arena-wide random-direction laser barrage. */
    public static final int LASER_BARRAGE = 10;
    private static final int[] PATTERN = {FAN, CIRCLE, STAR_CROSS, FAN, CIRCLE, FAN, ROSE_STARFALL, PRAYER, SWEEP, SKY_NOVA, PRISM_LOCK, LASER_BARRAGE};

    private QizaiRules() {}

    public static int next(int sequence) { return PATTERN[Math.floorMod(sequence, PATTERN.length)]; }
    public static int repeatTicks() { return (RELEASES - 1) * REPEAT_INTERVAL; }
    public static boolean constellation(int kind) { return kind == STAR_CROSS || kind == ROSE_STARFALL; }
    public static boolean sweep(int kind) { return kind == SWEEP; }
    /** 这一招由自己的专用状态机接管（不走下面通用的 marks/radius 那套）。 */
    public static boolean skyNova(int kind) { return kind == SKY_NOVA; }
    public static boolean prismLock(int kind) { return kind == PRISM_LOCK; }
    public static boolean laserBarrage(int kind) { return kind == LASER_BARRAGE; }
    public static boolean spell(int kind) {
        return kind >= FAN && kind <= PRAYER || constellation(kind) || sweep(kind) || skyNova(kind)
                || prismLock(kind) || laserBarrage(kind);
    }
    public static int windup(int kind) {
        return laserBarrage(kind) ? QizaiLaserBarrageRules.WARNING_TICKS
                : prismLock(kind) ? QizaiPrismRules.TRACK_TICKS + QizaiPrismRules.LOCK_TICKS
                : skyNova(kind) ? QizaiSkyNovaRules.RISE_TICKS + QizaiSkyNovaRules.CHARGE_TICKS
                : sweep(kind) ? QizaiSweepRules.WINDUP
                // ★ v188 按用户要求「祁崽施法速度翻倍」: 全部前摇减半(24/32/36/40/60 -> 12/16/18/20/30)
                : kind == STAR_CROSS ? 18 : kind == ROSE_STARFALL ? 20 : kind == PRAYER ? 30 : kind == CIRCLE ? 16 : 12;
    }
    public static int recovery(int kind) {
        return laserBarrage(kind) ? QizaiLaserBarrageRules.RECOVERY
                : prismLock(kind) ? QizaiPrismRules.RECOVERY
                : skyNova(kind) ? QizaiSkyNovaRules.RECOVERY_TICKS
                : sweep(kind) ? QizaiSweepRules.RECOVERY : kind == ROSE_STARFALL ? 40 : RECOVERY;
    }
    public static double radius(int kind) {
        return laserBarrage(kind) ? QizaiLaserBarrageRules.RANGE
                : prismLock(kind) ? QizaiPrismRules.RANGE
                : skyNova(kind) ? QizaiSkyNovaRules.RADIUS
                : sweep(kind) ? QizaiSweepRules.RADIUS
                : kind == STAR_CROSS ? CROSS_LENGTH : kind == ROSE_STARFALL ? STARFALL_RADIUS
                : kind == PRAYER ? PRAYER_RADIUS : CIRCLE_RADIUS;
    }
    public static String id(int kind) {
        return laserBarrage(kind) ? QizaiLaserBarrageRules.ID
                : prismLock(kind) ? QizaiPrismRules.ID
                : skyNova(kind) ? "sky_nova"
                : sweep(kind) ? QizaiSweepRules.ID : kind == STAR_CROSS ? "star_cross" : "rose_starfall";
    }

    public static boolean cross(double offsetX, double height, double offsetZ) {
        return Double.isFinite(offsetX + height + offsetZ) && Math.abs(height) <= 2.5
                && (Math.abs(offsetX) <= CROSS_LENGTH && Math.abs(offsetZ) <= CROSS_WIDTH
                || Math.abs(offsetZ) <= CROSS_LENGTH && Math.abs(offsetX) <= CROSS_WIDTH);
    }

    public static boolean inside(double horizontalSquared, double height, int kind) {
        double radius = radius(kind);
        return Double.isFinite(horizontalSquared) && Double.isFinite(height) && horizontalSquared >= 0
                && horizontalSquared <= radius * radius && Math.abs(height) <= 2.5;
    }
}
