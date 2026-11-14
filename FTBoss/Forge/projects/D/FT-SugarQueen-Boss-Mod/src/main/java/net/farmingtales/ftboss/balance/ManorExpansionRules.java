package net.farmingtales.ftboss.balance;

/** 庄园追加技能的纯规则：时间、叠层、计数与范围边界。 */
public final class ManorExpansionRules {
    public static final int TURRET_LIFETIME = 30 * 20;
    public static final int TURRET_DEPLOY = 20;
    public static final int TURRET_INTERVAL = 80;
    public static final int TURRET_COOLDOWN = 100;
    public static final double TURRET_RANGE = 32.0;

    private ManorExpansionRules() {}

    public static int nextAmplifier(int previous) { return previous < 0 ? 0 : (int) Math.min(254L, previous + 1L); }

    public static boolean drainDue(long now, long previous) {
        return previous != Long.MIN_VALUE && now >= previous && now - previous >= 20;
    }

    public static float drainedMana(float mana) {
        return Float.isFinite(mana) ? Math.max(0.0F, mana - 5.0F) : 0.0F;
    }

    public static boolean warningPhase(int phase) { return phase == 1 || phase == 2; }

    public static boolean tenthBullet(int bullets) { return bullets > 0 && bullets % 10 == 0; }

    public static boolean turretFires(int age) {
        return age >= TURRET_DEPLOY && age < TURRET_LIFETIME && (age - TURRET_DEPLOY) % TURRET_INTERVAL == 0;
    }
}
