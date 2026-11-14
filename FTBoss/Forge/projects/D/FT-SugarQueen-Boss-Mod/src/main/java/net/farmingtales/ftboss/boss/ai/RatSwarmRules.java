package net.farmingtales.ftboss.boss.ai;

/** 鼠群主从循环的数值与结算规则，2026-10-12。 */
public final class RatSwarmRules {
    public static final double MASTER_HEALTH = 40000.0;
    public static final double DEPUTY_HEALTH = 400.0;
    public static final double DEPUTY_DAMAGE = 2.0;
    public static final int SWARM_SIZE = 12;
    public static final int WOUNDED_SWARM_SIZE = 16;
    public static final float SWARM_DAMAGE_TAKEN = 0.6F;
    public static final int DEVICE_COUNT = 3;
    public static final int DEVICE_TICKS = 400;
    public static final int DEVICE_FULL = 100;
    public static final int FOOD_HEAL = 20;
    public static final int FEED_COOLDOWN = 4;
    public static final float DEVICE_DAMAGE_FRACTION = 0.125F;
    public static final float EXPLOSION_DAMAGE = 8.0F;
    public static final double EXPLOSION_RADIUS = 4.0;
    public static final int GUARD_PHASE = 0;
    public static final int DEVICE_PHASE = 1;

    private RatSwarmRules() {}

    public static int swarmSize(float health, float maximum) {
        return health < maximum * 0.5F ? WOUNDED_SWARM_SIZE : SWARM_SIZE;
    }

    public static float damageSwarm(float health, float damage) {
        if (!Float.isFinite(damage) || damage <= 0) return health;
        return Math.max(0, health - damage * SWARM_DAMAGE_TAKEN);
    }

    public static float sharedHitDamage(float damage, float previousDamage, long elapsed) {
        if (!Float.isFinite(damage) || damage <= 0) return 0;
        return elapsed >= 0 && elapsed < 10 ? Math.max(0, damage - previousDamage) : damage;
    }

    public static float scaleSwarm(float health, float maximum, float nextMaximum) {
        return maximum > 0 ? Math.max(0, Math.min(nextMaximum, health / maximum * nextMaximum)) : nextMaximum;
    }

    public static int healDevice(int health) {
        return Math.min(DEVICE_FULL, Math.max(0, health) + FOOD_HEAL);
    }

    public static boolean lethalBurst(float damage, float maximumHealth, boolean playerAttack) {
        return playerAttack && Float.isFinite(damage) && maximumHealth > 0 && damage >= maximumHealth;
    }

    public static boolean deviceComplete(int health) {
        return health >= DEVICE_FULL;
    }

    public static boolean deviceExpired(int ticks) {
        return ticks <= 0;
    }
}
