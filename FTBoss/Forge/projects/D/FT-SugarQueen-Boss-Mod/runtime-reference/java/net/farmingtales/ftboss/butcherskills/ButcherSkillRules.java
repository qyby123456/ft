/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.butcherskills;

public final class ButcherSkillRules {
    public static final int FIRST_CAST_DELAY = 80;
    public static final int GLOBAL_COOLDOWN = 80;
    public static final int SUMMON_DURATION = 32;
    public static final int MAX_SUMMON_PULSES = 24;
    public static final int MAX_PARTICLES_PER_WORLD_TICK = 320;
    public static final int MAX_SUMMON_PARTICLES_PER_WORLD_TICK = 160;

    private ButcherSkillRules() {
    }

    public static boolean inCircle(double d, double d2, double d3) {
        return d <= d3 * d3 && Math.abs(d2) <= 2.5;
    }

    public static boolean inCone(double d, double d2, double d3, double d4, double d5, double d6) {
        double d7 = d * d + d2 * d2;
        if (d7 > d5 * d5) {
            return false;
        }
        if (d7 < 1.0E-4) {
            return true;
        }
        return (d * d3 + d2 * d4) / Math.sqrt(d7) >= d6;
    }

    public static float damage(Skill skill, double d, boolean bl) {
        if (!Double.isFinite(d) || d <= 0.0) {
            return 0.0f;
        }
        return (float)Math.min(3.4028234663852886E38, (double)skill.baseDamage * (d / 5.5) * (bl ? 1.15 : 1.0));
    }

    public static float lifesteal(float f, float f2) {
        if (!Float.isFinite(f) || !Float.isFinite(f2)) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(f2, f * 0.15f));
    }

    public static double progress(long l, int n) {
        return Math.max(0.0, Math.min(1.0, (double)l / (double)n));
    }

    public static enum Skill {
        BLOOD_BRAND("butcher_blood_brand", 28, 12, 240, 18.0, 3.0, 16.0f),
        BLOOD_TIDE("butcher_blood_tide", 22, 12, 220, 8.0, 8.0, 14.0f),
        CLEAVER_SWEEP("butcher_cleaver_sweep", 14, 10, 160, 4.5, 4.5, 20.0f),
        BONE_CRUSH("butcher_bone_crush", 20, 14, 200, 5.0, 3.0, 28.0f);

        public final String action;
        public final int windup;
        public final int recovery;
        public final int cooldown;
        public final double castRange;
        public final double radius;
        public final float baseDamage;

        private Skill(String string2, int n2, int n3, int n4, double d, double d2, float f) {
            this.action = string2;
            this.windup = n2;
            this.recovery = n3;
            this.cooldown = n4;
            this.castRange = d;
            this.radius = d2;
            this.baseDamage = f;
        }

        public int duration() {
            return this.windup + this.recovery;
        }

        public boolean melee() {
            return this == CLEAVER_SWEEP || this == BONE_CRUSH;
        }
    }

    public static final class ParticleBudget {
        private long tick = Long.MIN_VALUE;
        private int used;
        private int summons;

        public boolean take(long l, boolean bl) {
            if (l != this.tick) {
                this.tick = l;
                this.used = 0;
                this.summons = 0;
            }
            if (this.used >= 320 || bl && this.summons >= 160) {
                return false;
            }
            ++this.used;
            if (bl) {
                ++this.summons;
            }
            return true;
        }
    }
}

