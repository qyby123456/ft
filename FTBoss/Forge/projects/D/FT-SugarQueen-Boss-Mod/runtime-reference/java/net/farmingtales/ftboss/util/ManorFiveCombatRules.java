/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Locale;
import net.farmingtales.ftboss.util.HiddenBossCastRules;

public final class ManorFiveCombatRules {
    public static final double ATTACK_BONUS = 0.4;
    public static final double MELEE_PUSH = 1.5;
    public static final double MELEE_LIFT = 0.55;
    public static final double MAX_CAST_DISTANCE = 24.0;
    public static final double ADAPTIVE_MIN_BONUS = 0.1;
    public static final double ADAPTIVE_MAX_BONUS = 0.5;
    public static final double ADAPTIVE_HEALTH_BASE = 20.0;
    public static final double ADAPTIVE_HEALTH_CAP = 100.0;
    public static final double ADAPTIVE_HEALTH_BONUS = 0.2;
    public static final int ADAPTIVE_BUFF_CAP = 10;
    public static final double ADAPTIVE_BUFF_BONUS = 0.2;

    private ManorFiveCombatRules() {
    }

    public static boolean supports(int variant) {
        return variant == 0 || variant == 1 || variant == 4 || variant == 6 || variant == 7;
    }

    public static boolean hasExpandedSkills(int variant) {
        return ManorFiveCombatRules.supports(variant) || variant == 5;
    }

    public static int rotationLength(int variant) {
        return variant == 5 ? 6 : 4;
    }

    public static double adaptiveDamageBonus(double maxHealth, int beneficialEffects) {
        double healthProgress = Double.isNaN(maxHealth) ? 0.0 : Math.max(0.0, Math.min(1.0, (maxHealth - 20.0) / 80.0));
        double buffProgress = (double)Math.max(0, Math.min(10, beneficialEffects)) / 10.0;
        return Math.max(0.1, Math.min(0.5, 0.1 + healthProgress * 0.2 + buffProgress * 0.2));
    }

    public static Skill select(int variant, int rotation) {
        if (variant == 5) {
            return switch (Math.floorMod(rotation, ManorFiveCombatRules.rotationLength(variant))) {
                case 0 -> Skill.QINGYI_TIDE;
                case 2 -> Skill.QINGYI_THUNDER;
                case 4 -> Skill.QINGYI_STAR;
                default -> null;
            };
        }
        int slot = Math.floorMod(rotation + (variant == 4 ? 1 : 0), 4);
        if ((slot & 1) != 0) {
            return null;
        }
        return switch (variant) {
            case 0 -> {
                if (slot == 0) {
                    yield Skill.YOUZI_SEAL;
                }
                yield Skill.YOUZI_ECHO;
            }
            case 1 -> {
                if (slot == 0) {
                    yield Skill.DANDAN_REAP;
                }
                yield Skill.DANDAN_STAR;
            }
            case 4 -> {
                if (slot == 0) {
                    yield Skill.LEMON_ACID;
                }
                yield Skill.LEMON_FLAMBE;
            }
            case 6 -> {
                if (slot == 0) {
                    yield Skill.XUANLAN_RAIL;
                }
                yield Skill.XUANLAN_RING;
            }
            case 7 -> {
                if (slot == 0) {
                    yield Skill.SHADOW_CROSS;
                }
                yield Skill.SHADOW_CAGE;
            }
            default -> null;
        };
    }

    public static int pulseAt(Skill skill, int age) {
        int elapsed = age - skill.windup;
        if (elapsed < 0 || elapsed % skill.interval != 0) {
            return -1;
        }
        int pulse = elapsed / skill.interval;
        return pulse < skill.pulses ? pulse : -1;
    }

    public static double waveRadius(Skill skill, int pulse) {
        double bonus = skill == Skill.XUANLAN_RING ? 1.0 : 0.0;
        return (skill.radius - bonus) * ((double)pulse + 1.0) / (double)skill.pulses + bonus;
    }

    public static double beamHalfWidth(Skill skill) {
        return skill == Skill.XUANLAN_RAIL ? 2.2 : 1.2;
    }

    public static boolean contains(Skill skill, int pulse, double forward, double side, double height, double halfWidth) {
        if (Math.abs(height) > 3.0) {
            return false;
        }
        double distance = Math.hypot(forward, side);
        return switch (skill.shape) {
            default -> throw new IncompatibleClassChangeError();
            case Shape.CIRCLE, Shape.ECHO -> {
                if (distance <= skill.radius + halfWidth) {
                    yield true;
                }
                yield false;
            }
            case Shape.WAVE -> {
                if (Math.abs(distance - ManorFiveCombatRules.waveRadius(skill, pulse)) <= 1.15 + halfWidth) {
                    yield true;
                }
                yield false;
            }
            case Shape.CROSS -> {
                if (Math.abs(forward) <= skill.radius + halfWidth && Math.abs(side) <= 1.15 + halfWidth || Math.abs(side) <= skill.radius + halfWidth && Math.abs(forward) <= 1.15 + halfWidth) {
                    yield true;
                }
                yield false;
            }
            case Shape.BEAM -> {
                if (forward >= -halfWidth && forward <= skill.radius + halfWidth && Math.abs(side) <= ManorFiveCombatRules.beamHalfWidth(skill) + halfWidth) {
                    yield true;
                }
                yield false;
            }
            case Shape.CONE -> distance <= skill.radius + halfWidth && forward >= 0.0 && (distance <= halfWidth || forward / distance >= Math.cos(Math.toRadians(50.0)));
        };
    }

    public static enum Skill {
        YOUZI_SEAL(0, "\u5e84\u56ed\u9707\u5370", Shape.CIRCLE, 5.5, 22, 1, 1, 100.0f, 1.65, 0.55),
        YOUZI_ECHO(0, "\u53e0\u5f71\u5408\u51fb", Shape.ECHO, 2.8, 18, 3, 12, 38.0f, 1.25, 0.35),
        DANDAN_REAP(1, "\u672b\u5f71\u516d\u91cd\u9570", Shape.WAVE, 13.5, 18, 6, 10, 44.0f, 1.4, 0.42),
        DANDAN_STAR(1, "\u70db\u5149\u5760\u661f", Shape.CIRCLE, 6.75, 22, 3, 12, 42.0f, 1.65, 0.62),
        LEMON_ACID(4, "\u9178\u96fe\u7206\u9505", Shape.CIRCLE, 5.0, 18, 4, 16, 24.0f, 0.55, 0.18),
        LEMON_FLAMBE(4, "\u70c8\u706b\u98a0\u52fa", Shape.CONE, 9.0, 18, 2, 14, 54.0f, 1.6, 0.95),
        XUANLAN_RAIL(6, "\u78c1\u8f68\u8fde\u5c04", Shape.BEAM, 36.0, 24, 3, 12, 96.0f, 1.2, 0.28),
        XUANLAN_RING(6, "\u8d85\u538b\u9707\u73af", Shape.WAVE, 10.0, 20, 3, 10, 92.0f, 1.8, 0.48),
        SHADOW_CROSS(7, "\u5f71\u5203\u5341\u5b57", Shape.CROSS, 9.0, 20, 2, 14, 56.0f, 1.4, 0.3),
        SHADOW_CAGE(7, "\u566c\u5f71\u7262\u7b3c", Shape.CIRCLE, 6.0, 22, 3, 14, 32.0f, 1.55, 0.4),
        QINGYI_TIDE(5, "\u96f7\u6f6e\u88c2\u9635", Shape.CONE, 15.0, 20, 2, 14, 140.0f, 1.85, 0.65),
        QINGYI_THUNDER(5, "\u82cd\u96f7\u9886\u57df", Shape.CIRCLE, 8.25, 24, 3, 16, 125.0f, 1.35, 0.42),
        QINGYI_STAR(5, "\u661f\u621f\u5929\u88c1", Shape.CROSS, 15.0, 26, 2, 16, 180.0f, 1.9, 0.75);

        public final int variant;
        public final String title;
        public final Shape shape;
        public final double radius;
        public final int windup;
        public final int pulses;
        public final int interval;
        public final float damage;
        public final double knockback;
        public final double lift;

        private Skill(int variant, String title, Shape shape, double radius, int windup, int pulses, int interval, float damage, double knockback, double lift) {
            this.variant = variant;
            this.title = title;
            this.shape = shape;
            this.radius = radius;
            this.windup = HiddenBossCastRules.quicker(windup);
            this.pulses = pulses;
            this.interval = interval;
            this.damage = damage;
            this.knockback = knockback;
            this.lift = lift;
        }

        public int duration() {
            return this.windup + (this.pulses - 1) * this.interval + 1;
        }

        public String id() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public static Skill byId(String id) {
            if (id == null) {
                return null;
            }
            for (Skill skill : Skill.values()) {
                if (!skill.id().equals(id)) continue;
                return skill;
            }
            return null;
        }

        public double meleeMultiplier() {
            return switch (this) {
                case YOUZI_SEAL, QINGYI_STAR -> 2.0;
                case DANDAN_STAR, LEMON_FLAMBE, QINGYI_THUNDER -> 1.75;
                default -> 1.5;
            };
        }
    }

    public static enum Shape {
        CIRCLE,
        WAVE,
        CROSS,
        BEAM,
        ECHO,
        CONE;

    }
}

