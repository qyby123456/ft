/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Locale;

public final class NonManorWeaponRules {
    public static final int DURABILITY_COST = 3;
    public static final int REQUEST_INTERVAL = 4;
    public static final String COOLDOWN_TAG = "FTBossNonManorWeaponSkills";

    private NonManorWeaponRules() {
    }

    public static Skill forItem(String identifier) {
        for (Skill skill : Skill.values()) {
            if (!skill.item.equals(identifier)) continue;
            return skill;
        }
        return null;
    }

    public static float damage(Skill skill, double attackDamage, int chainIndex) {
        if (skill == null || !Double.isFinite(attackDamage) || attackDamage < 0.0 || chainIndex < 0) {
            return 0.0f;
        }
        double attenuation = skill == Skill.ARC ? Math.max(0.55, 1.0 - (double)chainIndex * 0.15) : 1.0;
        return (float)Math.min(128.0, (skill.flatDamage + attackDamage * skill.attackMultiplier) * attenuation);
    }

    public static boolean within(double distanceSquared, double range) {
        return Double.isFinite(distanceSquared) && Double.isFinite(range) && distanceSquared >= 0.0 && range >= 0.0 && distanceSquared <= range * range;
    }

    public static boolean inCone(double distanceSquared, double facingDot, double range) {
        return NonManorWeaponRules.within(distanceSquared, range) && Double.isFinite(facingDot) && facingDot >= 0.35;
    }

    public static int remaining(Skill skill, long now, long deadline) {
        if (deadline <= now) {
            return 0;
        }
        long difference = deadline - now;
        return difference < 0L ? skill.cooldownTicks : (int)Math.min((long)skill.cooldownTicks, difference);
    }

    public static float meatHealing(int hits) {
        return Math.min(6.0f, (float)Math.max(0, hits) * 2.0f);
    }

    public static enum Skill {
        FROST("candy_cane", Shape.CONE, 10, 6.0, 0.0, 2.0, 1.5, 24),
        MEAT("turkey_legs", Shape.RING, 12, 4.0, 0.0, 4.0, 1.6, 24),
        THORNS("blooming_thorns", Shape.BURST, 12, 8.0, 2.75, 2.0, 1.2, 24),
        WINE("wine_bottle_mace", Shape.BURST, 12, 7.0, 3.0, 6.0, 1.5, 24),
        CHEESE("cheese_wheel_mace", Shape.CONE, 10, 4.5, 0.0, 2.0, 1.5, 24),
        ARC("defect_claw", Shape.CHAIN, 10, 10.0, 4.0, 4.0, 1.2, 4),
        COMPOST("compost_crown", Shape.BURST, 14, 7.0, 3.5, 2.0, 1.0, 24),
        APPLE("apple_stem", Shape.SELF, 18, 0.0, 0.0, 0.0, 0.0, 0),
        HOWL("dog_beating_stick", Shape.CONE, 10, 6.0, 0.0, 2.0, 1.5, 24),
        BARREL("barrel_crossbow", Shape.BURST, 14, 12.0, 2.5, 16.0, 1.0, 24),
        TIDE("tide_trident", Shape.RING, 16, 4.0, 0.0, 2.0, 1.0, 24);

        public final String item;
        public final Shape shape;
        public final int cooldownTicks;
        public final double range;
        public final double radius;
        public final double flatDamage;
        public final double attackMultiplier;
        public final int maxTargets;

        private Skill(String item, Shape shape, int seconds, double range, double radius, double flatDamage, double attackMultiplier, int maxTargets) {
            this.item = "ftboss:" + item;
            this.shape = shape;
            this.cooldownTicks = seconds * 20;
            this.range = range;
            this.radius = radius;
            this.flatDamage = flatDamage;
            this.attackMultiplier = attackMultiplier;
            this.maxTargets = maxTargets;
        }

        public String translationKey() {
            return "skill.ftboss.non_manor." + this.name().toLowerCase(Locale.ROOT);
        }
    }

    public static enum Shape {
        CONE,
        RING,
        BURST,
        CHAIN,
        SELF;

    }
}

