package net.farmingtales.ftboss.util;

public final class GongdiCombatRules {
    public static final int MAX_BLEED_STACKS = 5;
    public static final int BLEED_TICKS = 100;
    public static final int BLEED_INTERVAL = 20;
    public static final float BLEED_DAMAGE_PER_STACK = 1.5f;
    public static final double INNER_RADIUS = 6.75;
    public static final float INNER_DAMAGE_MULTIPLIER = 1.2f;
    public static final float OUTER_DAMAGE_MULTIPLIER = 2.0f;
    public static final float OUTER_HEAL_FRACTION = 0.12f;
    public static final int MAX_HEAL_TARGETS = 3;
    public static final int STRIKE_WINDUP = HiddenBossCastRules.quicker(16);
    public static final int PULL_WINDUP = HiddenBossCastRules.quicker(22);
    public static final int EXECUTE_WINDUP = HiddenBossCastRules.quicker(24);
    public static final int STRIKE_COOLDOWN = 100;
    public static final int PULL_COOLDOWN = 160;
    public static final int EXECUTE_COOLDOWN = 360;
    public static final int EXECUTE_RESET = 30;
    public static final double STRIKE_RANGE = 7.65;
    public static final double PULL_RANGE = 15.75;
    public static final double EXECUTE_RANGE = 10.125;
    public static final double PULL_HALF_ANGLE = Math.toRadians(50.0);
    public static final double STRIKE_HALF_ANGLE = Math.toRadians(60.0);
    public static final double HEIGHT_TOLERANCE = 2.5;
    public static final float STRIKE_DAMAGE_MULTIPLIER = 1.75f;
    public static final float EXECUTE_DAMAGE_MULTIPLIER = 2.5f;

    private GongdiCombatRules() {}

    public static float meleeDamage(double attackDamage, float enchantmentBonus) {
        return Math.max(0.0f, (float) attackDamage + enchantmentBonus);
    }

    public static float cleaveDamage(float meleeDamage, boolean outer) {
        return Math.max(0.0f, meleeDamage) * (outer ? OUTER_DAMAGE_MULTIPLIER : INNER_DAMAGE_MULTIPLIER);
    }

    public static float strikeDamage(float meleeDamage) {
        return Math.max(0.0f, meleeDamage) * STRIKE_DAMAGE_MULTIPLIER;
    }

    public static float executeDamage(float meleeDamage, int stacks) {
        return Math.max(0.0f, meleeDamage) * EXECUTE_DAMAGE_MULTIPLIER
                * (1.0f + Math.max(0, Math.min(MAX_BLEED_STACKS, stacks)) * 0.2f);
    }

    public static float outerHeal(float missingHealth, int targets) {
        return Math.max(0.0f, missingHealth) * OUTER_HEAL_FRACTION * Math.max(0, Math.min(MAX_HEAL_TARGETS, targets));
    }

    public static boolean inRange(double offsetX, double offsetY, double offsetZ, double radius) {
        return Math.abs(offsetY) <= HEIGHT_TOLERANCE && offsetX * offsetX + offsetZ * offsetZ <= radius * radius;
    }

    public static boolean inCone(double offsetX, double offsetZ, double directionX, double directionZ, double halfAngle) {
        double distance = Math.hypot(offsetX, offsetZ);
        double directionLength = Math.hypot(directionX, directionZ);
        return distance < 0.001 || (directionLength > 0.001
                && (offsetX * directionX + offsetZ * directionZ) / (distance * directionLength) >= Math.cos(halfAngle));
    }

    public static final class Bleed {
        private int stacks;
        private long expiresAt;
        private long nextPulse;

        public int addHit(long now, boolean bloodRage) {
            if (now >= this.expiresAt) {
                this.stacks = 0;
                this.nextPulse = now + BLEED_INTERVAL;
            }
            this.stacks = bloodRage ? MAX_BLEED_STACKS : Math.min(MAX_BLEED_STACKS, this.stacks + 1);
            this.expiresAt = now + BLEED_TICKS;
            return this.stacks;
        }

        public int stacks(long now) {
            return now < this.expiresAt ? this.stacks : 0;
        }

        public boolean expired(long now) {
            return now >= this.expiresAt;
        }

        public float pulse(long now) {
            if (this.stacks == 0 || now > this.expiresAt || now < this.nextPulse) return 0.0f;
            this.nextPulse = now + BLEED_INTERVAL;
            return this.stacks * BLEED_DAMAGE_PER_STACK;
        }
    }
}
