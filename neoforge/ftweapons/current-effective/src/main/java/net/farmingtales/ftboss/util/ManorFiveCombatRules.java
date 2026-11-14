package net.farmingtales.ftboss.util;

import java.util.Locale;

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

    public static boolean supports(int var0) {
        return var0 == 0 || var0 == 1 || var0 == 4 || var0 == 6 || var0 == 7;
    }

    public static boolean hasExpandedSkills(int var0) {
        return supports(var0) || var0 == 5;
    }

    public static int rotationLength(int var0) {
        return var0 == 5 ? 6 : 4;
    }

    public static double adaptiveDamageBonus(double var0, int var2) {
        double var3 = Double.isNaN(var0) ? 0.0 : Math.max(0.0, Math.min(1.0, (var0 - 20.0) / 80.0));
        double var5 = (double)Math.max(0, Math.min(10, var2)) / 10.0;
        return Math.max(0.1, Math.min(0.5, 0.1 + var3 * 0.2 + var5 * 0.2));
    }

    public static ManorFiveCombatRules.Skill select(int var0, int var1) {
        if (var0 == 5) {
            return switch (Math.floorMod(var1, rotationLength(var0))) {
                case 0 -> ManorFiveCombatRules.Skill.QINGYI_TIDE;
                default -> null;
                case 2 -> ManorFiveCombatRules.Skill.QINGYI_THUNDER;
                case 4 -> ManorFiveCombatRules.Skill.QINGYI_STAR;
            };
        } else {
            int var2 = Math.floorMod(var1 + (var0 == 4 ? 1 : 0), 4);
            if ((var2 & 1) != 0) {
                return null;
            } else {
                return switch (var0) {
                    case 0 -> var2 == 0 ? ManorFiveCombatRules.Skill.YOUZI_SEAL : ManorFiveCombatRules.Skill.YOUZI_ECHO;
                    case 1 -> var2 == 0 ? ManorFiveCombatRules.Skill.DANDAN_REAP : ManorFiveCombatRules.Skill.DANDAN_STAR;
                    default -> null;
                    case 4 -> var2 == 0 ? ManorFiveCombatRules.Skill.LEMON_ACID : ManorFiveCombatRules.Skill.LEMON_FLAMBE;
                    case 6 -> var2 == 0 ? ManorFiveCombatRules.Skill.XUANLAN_RAIL : ManorFiveCombatRules.Skill.XUANLAN_RING;
                    case 7 -> var2 == 0 ? ManorFiveCombatRules.Skill.SHADOW_CROSS : ManorFiveCombatRules.Skill.SHADOW_CAGE;
                };
            }
        }
    }

    public static int pulseAt(ManorFiveCombatRules.Skill var0, int var1) {
        int var2 = var1 - var0.windup;
        if (var2 >= 0 && var2 % var0.interval == 0) {
            int var3 = var2 / var0.interval;
            return var3 < var0.pulses ? var3 : -1;
        } else {
            return -1;
        }
    }

    public static double waveRadius(ManorFiveCombatRules.Skill var0, int var1) {
        double var2 = var0 == ManorFiveCombatRules.Skill.XUANLAN_RING ? 1.0 : 0.0;
        return (var0.radius - var2) * ((double)var1 + 1.0) / (double)var0.pulses + var2;
    }

    public static double beamHalfWidth(ManorFiveCombatRules.Skill var0) {
        return var0 == ManorFiveCombatRules.Skill.XUANLAN_RAIL ? 2.2 : 1.2;
    }

    public static boolean contains(ManorFiveCombatRules.Skill var0, int var1, double var2, double var4, double var6, double var8) {
        if (Math.abs(var6) > 3.0) {
            return false;
        } else {
            double var10 = Math.hypot(var2, var4);

            return switch (var0.shape) {
                case CIRCLE, ECHO -> var10 <= var0.radius + var8;
                case WAVE -> Math.abs(var10 - waveRadius(var0, var1)) <= 1.15 + var8;
                case CROSS -> Math.abs(var2) <= var0.radius + var8 && Math.abs(var4) <= 1.15 + var8
                || Math.abs(var4) <= var0.radius + var8 && Math.abs(var2) <= 1.15 + var8;
                case BEAM -> var2 >= -var8 && var2 <= var0.radius + var8 && Math.abs(var4) <= beamHalfWidth(var0) + var8;
                case CONE -> var10 <= var0.radius + var8 && var2 >= 0.0 && (var10 <= var8 || var2 / var10 >= Math.cos(Math.toRadians(50.0)));
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

    public static enum Skill {
        YOUZI_SEAL(0, "庄园震印", ManorFiveCombatRules.Shape.CIRCLE, 5.5, 22, 1, 1, 100.0F, 1.65, 0.55),
        YOUZI_ECHO(0, "叠影合击", ManorFiveCombatRules.Shape.ECHO, 2.8, 18, 3, 12, 38.0F, 1.25, 0.35),
        DANDAN_REAP(1, "末影六重镰", ManorFiveCombatRules.Shape.WAVE, 13.5, 18, 6, 10, 44.0F, 1.4, 0.42),
        DANDAN_STAR(1, "烛光坠星", ManorFiveCombatRules.Shape.CIRCLE, 6.75, 22, 3, 12, 42.0F, 1.65, 0.62),
        LEMON_ACID(4, "酸雾爆锅", ManorFiveCombatRules.Shape.CIRCLE, 5.0, 18, 4, 16, 24.0F, 0.55, 0.18),
        LEMON_FLAMBE(4, "烈火颠勺", ManorFiveCombatRules.Shape.CONE, 9.0, 18, 2, 14, 54.0F, 1.6, 0.95),
        XUANLAN_RAIL(6, "磁轨连射", ManorFiveCombatRules.Shape.BEAM, 18.0, 24, 3, 12, 96.0F, 1.2, 0.28),
        XUANLAN_RING(6, "超压震环", ManorFiveCombatRules.Shape.WAVE, 10.0, 20, 3, 10, 92.0F, 1.8, 0.48),
        SHADOW_CROSS(7, "影刃十字", ManorFiveCombatRules.Shape.CROSS, 9.0, 20, 2, 14, 56.0F, 1.4, 0.3),
        SHADOW_CAGE(7, "噬影牢笼", ManorFiveCombatRules.Shape.CIRCLE, 6.0, 22, 3, 14, 32.0F, 1.55, 0.4),
        QINGYI_TIDE(5, "雷潮裂阵", ManorFiveCombatRules.Shape.CONE, 15.0, 20, 2, 14, 140.0F, 1.85, 0.65),
        QINGYI_THUNDER(5, "苍雷领域", ManorFiveCombatRules.Shape.CIRCLE, 8.25, 24, 3, 16, 125.0F, 1.35, 0.42),
        QINGYI_STAR(5, "星戟天裁", ManorFiveCombatRules.Shape.CROSS, 15.0, 26, 2, 16, 180.0F, 1.9, 0.75);

        public final int variant;
        public final String title;
        public final ManorFiveCombatRules.Shape shape;
        public final double radius;
        public final int windup;
        public final int pulses;
        public final int interval;
        public final float damage;
        public final double knockback;
        public final double lift;

        private Skill(
            int nullxx,
            String nullxxx,
            ManorFiveCombatRules.Shape nullxxxx,
            double nullxxxxx,
            int nullxxxxxx,
            int nullxxxxxxx,
            int nullxxxxxxxx,
            float nullxxxxxxxxx,
            double nullxxxxxxxxxx,
            double nullxxxxxxxxxxx
        ) {
            this.variant = nullxx;
            this.title = nullxxx;
            this.shape = nullxxxx;
            this.radius = nullxxxxx;
            this.windup = HiddenBossCastRules.quicker(nullxxxxxx);
            this.pulses = nullxxxxxxx;
            this.interval = nullxxxxxxxx;
            this.damage = nullxxxxxxxxx;
            this.knockback = nullxxxxxxxxxx;
            this.lift = nullxxxxxxxxxxx;
        }

        public int duration() {
            return this.windup + (this.pulses - 1) * this.interval + 1;
        }

        public String id() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public static ManorFiveCombatRules.Skill byId(String id) {
            if (id == null) {
                return null;
            } else {
                for (ManorFiveCombatRules.Skill skill : values()) {
                    if (skill.id().equals(id)) {
                        return skill;
                    }
                }

                return null;
            }
        }

        public double meleeMultiplier() {
            return switch (this) {
                case YOUZI_SEAL, QINGYI_STAR -> 2.0;
                default -> 1.5;
                case DANDAN_STAR, LEMON_FLAMBE, QINGYI_THUNDER -> 1.75;
            };
        }
    }
}
