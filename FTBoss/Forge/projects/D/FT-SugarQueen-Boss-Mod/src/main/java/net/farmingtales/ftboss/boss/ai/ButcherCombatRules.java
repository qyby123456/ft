package net.farmingtales.ftboss.boss.ai;

public final class ButcherCombatRules {
    public static final int MELEE_IMPACT = 1;
    public static final int MELEE_DURATION = 4;
    public static final int NORMAL_INTERVAL = 6;
    public static final int ENRAGED_INTERVAL = 4;
    public static final int REPATH_INTERVAL = 3;
    public static final double TARGET_RANGE = 48;
    public static final int FIRST_SUMMON_DELAY = 160;
    public static final int SUMMON_INTERVAL = 360;
    public static final int ENRAGED_SUMMON_INTERVAL = 240;
    public static final int MAX_MINIONS = 6;
    public static final double BASE_SPEED = 0.1;
    public static final double MAX_SPEED = 0.42;

    private ButcherCombatRules() {}

    public static int interval(boolean enraged) {
        return enraged ? ENRAGED_INTERVAL : NORMAL_INTERVAL;
    }

    public static double chaseSpeed(boolean enraged) {
        return enraged ? 1.15 : 1.05;
    }

    public static boolean withinReach(double distanceSquared, double verticalGap, double ownerWidth, double targetWidth) {
        double reach = ownerWidth * 2;
        return distanceSquared <= reach * reach + targetWidth && verticalGap < 2.0;
    }

    public static boolean empowered(int successfulHits) {
        return successfulHits % 2 == 1;
    }

    public static float damage(float attackDamage, int successfulHits) {
        return attackDamage > 0 ? Math.max(24.0F, attackDamage * 2.0F) * (empowered(successfulHits) ? 1.25F : 1.0F) : 0;
    }

    public static double minionHealth(int kind) {
        return kind == 2 ? 160 : kind == 1 ? 120 : 80;
    }

    public static double minionDamage(int kind) {
        return kind == 2 ? 12 : kind == 1 ? 14 : 8;
    }

    public static double minionArmor(int kind) {
        return kind == 2 ? 16 : kind == 1 ? 12 : 8;
    }

    public static double minionSpeed(int kind) {
        return kind == 2 ? 0.38 : kind == 1 ? 0.32 : 0.36;
    }

    public static int minionInterval(int kind) {
        return kind == 2 ? 10 : 12;
    }

    public static int summonCount(int requested, int current) {
        return Math.max(0, Math.min(requested, MAX_MINIONS - current));
    }

    /**
     * 屠夫命中后的吸血比例（2026-09-19 按用户要求：「屠夫的吸血应该只有 15% 的吸血」）。
     *
     * <p>旧口径是 {@code 1.0 + 实际伤害 × 0.45}（45% 再送 1 点），现在**只剩 15%**，
     * 那 1 点固定回血也一并去掉 —— 一句话：回血 = 实际造成的伤害 × 0.15，且不超过缺失血量。
     * 柠檬猫献祭后继承的屠夫近战走的是同一个函数，所以它也一起变成 15%。
     */
    public static final float LIFESTEAL_RATIO = 0.15F;

    public static float lifesteal(float dealt, float missingHealth) {
        return dealt > 0 ? Math.max(0, Math.min(missingHealth, dealt * LIFESTEAL_RATIO)) : 0;
    }
}
