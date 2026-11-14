package net.farmingtales.ftboss.butcherskills;

public final class ButcherSkillRules {
    public static final int FIRST_CAST_DELAY = 80;
    public static final int GLOBAL_COOLDOWN = 80;
    public static final int SUMMON_DURATION = 32;
    public static final int MAX_SUMMON_PULSES = 24;
    public static final int MAX_PARTICLES_PER_WORLD_TICK = 320;
    public static final int MAX_SUMMON_PARTICLES_PER_WORLD_TICK = 160;

    public static final class ParticleBudget {
        private long tick = Long.MIN_VALUE;
        private int used;
        private int summons;

        public boolean take(long now, boolean summon) {
            if (now != this.tick) { this.tick = now; this.used = 0; this.summons = 0; }
            if (this.used >= MAX_PARTICLES_PER_WORLD_TICK || summon && this.summons >= MAX_SUMMON_PARTICLES_PER_WORLD_TICK) return false;
            this.used++;
            if (summon) this.summons++;
            return true;
        }
    }

    public enum Skill {
        BLOOD_BRAND("butcher_blood_brand", 28, 12, 240, 18, 3.0, 16),
        BLOOD_TIDE("butcher_blood_tide", 22, 12, 220, 8, 8.0, 14),
        CLEAVER_SWEEP("butcher_cleaver_sweep", 14, 10, 160, 4.5, 4.5, 20),
        BONE_CRUSH("butcher_bone_crush", 20, 14, 200, 5, 3.0, 28);

        public final String action;
        public final int windup;
        public final int recovery;
        public final int cooldown;
        public final double castRange;
        public final double radius;
        public final float baseDamage;

        Skill(String action, int windup, int recovery, int cooldown, double castRange, double radius, float baseDamage) {
            this.action = action;
            this.windup = windup;
            this.recovery = recovery;
            this.cooldown = cooldown;
            this.castRange = castRange;
            this.radius = radius;
            this.baseDamage = baseDamage;
        }

        public int duration() { return this.windup + this.recovery; }
        public boolean melee() { return this == CLEAVER_SWEEP || this == BONE_CRUSH; }
    }

    private ButcherSkillRules() {}

    public static boolean inCircle(double horizontalSquared, double height, double radius) {
        return horizontalSquared <= radius * radius && Math.abs(height) <= 2.5;
    }

    public static boolean inCone(double offsetX, double offsetZ, double forwardX, double forwardZ, double radius, double minimumDot) {
        double distanceSquared = offsetX * offsetX + offsetZ * offsetZ;
        if (distanceSquared > radius * radius) return false;
        if (distanceSquared < 0.0001) return true;
        return (offsetX * forwardX + offsetZ * forwardZ) / Math.sqrt(distanceSquared) >= minimumDot;
    }

    public static float damage(Skill skill, double attackAttribute, boolean enraged) {
        if (!Double.isFinite(attackAttribute) || attackAttribute <= 0) return 0;
        return (float) Math.min(Float.MAX_VALUE, skill.baseDamage * (attackAttribute / 5.5) * (enraged ? 1.15 : 1.0));
    }

    public static float lifesteal(float actualDamage, float missingHealth) {
        if (!Float.isFinite(actualDamage) || !Float.isFinite(missingHealth)) return 0;
        return Math.max(0, Math.min(missingHealth, actualDamage * 0.15F));
    }

    public static double progress(long elapsed, int duration) {
        return Math.max(0, Math.min(1, elapsed / (double) duration));
    }
}
