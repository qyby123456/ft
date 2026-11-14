package net.farmingtales.ftboss.util;

public final class ManorFiveCombatRules {
    public static final double ATTACK_BONUS = 0.40;
    public static final double MELEE_PUSH = 1.50;
    public static final double MELEE_LIFT = 0.55;
    public static final double MAX_CAST_DISTANCE = 24.0;
    public static final double ADAPTIVE_MIN_BONUS = 0.10;
    public static final double ADAPTIVE_MAX_BONUS = 0.50;
    public static final double ADAPTIVE_HEALTH_BASE = 20.0;
    public static final double ADAPTIVE_HEALTH_CAP = 100.0;
    public static final double ADAPTIVE_HEALTH_BONUS = 0.20;
    public static final int ADAPTIVE_BUFF_CAP = 10;
    public static final double ADAPTIVE_BUFF_BONUS = 0.20;

    public enum Shape { CIRCLE, WAVE, CROSS, BEAM, ECHO, CONE }

    /**
     * 庄园 Boss 的「特效技能」表（每个技能都有独立的地面预警/粒子演出）。
     *
     * <p>表内 windup 为 2026-09-17 的前摇基准；2026-09-18 经 HiddenBossCastRules 缩短约 30%。
     * 预警圈、动画和首段结算共用缩短后的 windup；脉数、段间隔与伤害不变。
     */
    public enum Skill {
        YOUZI_SEAL(0, "庄园震印", Shape.CIRCLE, 5.5, 22, 1, 1, 100.0f, 1.65, 0.55),
        YOUZI_ECHO(0, "叠影合击", Shape.ECHO, 2.8, 18, 3, 12, 38.0f, 1.25, 0.35),
        DANDAN_REAP(1, "末影六重镰", Shape.WAVE, 13.5, 18, 6, 10, 44.0f, 1.40, 0.42),
        DANDAN_STAR(1, "烛光坠星", Shape.CIRCLE, 6.75, 22, 3, 12, 42.0f, 1.65, 0.62),
        LEMON_ACID(4, "酸雾爆锅", Shape.CIRCLE, 5.0, 18, 4, 16, 24.0f, 0.55, 0.18),
        LEMON_FLAMBE(4, "烈火颠勺", Shape.CONE, 9.0, 18, 2, 14, 54.0f, 1.60, 0.95),
        XUANLAN_RAIL(6, "磁轨连射", Shape.BEAM, 18.0, 24, 3, 12, 96.0f, 1.20, 0.28),
        XUANLAN_RING(6, "超压震环", Shape.WAVE, 10.0, 20, 3, 10, 92.0f, 1.80, 0.48),
        SHADOW_CROSS(7, "影刃十字", Shape.CROSS, 9.0, 20, 2, 14, 56.0f, 1.40, 0.30),
        SHADOW_CAGE(7, "噬影牢笼", Shape.CIRCLE, 6.0, 22, 3, 14, 32.0f, 1.55, 0.40),
        QINGYI_TIDE(5, "雷潮裂阵", Shape.CONE, 15.0, 20, 2, 14, 140.0f, 1.85, 0.65),
        QINGYI_THUNDER(5, "苍雷领域", Shape.CIRCLE, 8.25, 24, 3, 16, 125.0f, 1.35, 0.42),
        QINGYI_STAR(5, "星戟天裁", Shape.CROSS, 15.0, 26, 2, 16, 180.0f, 1.90, 0.75);

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

        Skill(int variant, String title, Shape shape, double radius, int windup, int pulses,
              int interval, float damage, double knockback, double lift) {
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

        /**
         * ★ 2026-09-24：技能 id（如 {@code shadow_cross} / {@code shadow_cage}）。
         *
         * <p>给折耳根「紫色残影」用的：残影要**记录当时触发它的那一招**，
         * 0~5 秒后再把同一招重放一次（伤害压成原伤害的 25% 真伤）。
         * 直接用枚举名的小写形式，不另维护一张表 —— 加减技能时不会两边不同步。
         */
        public String id() {
            return this.name().toLowerCase(java.util.Locale.ROOT);
        }

        /** 按 {@link #id()} 反查技能；找不到返回 null（残影那边据此安全跳过）。 */
        public static Skill byId(String id) {
            if (id == null) return null;
            for (Skill skill : values()) {
                if (skill.id().equals(id)) return skill;
            }
            return null;
        }

        public double meleeMultiplier() {
            return switch (this) {
                case YOUZI_SEAL, QINGYI_STAR -> ManorSkillDamageRules.HEAVY_MULTIPLIER;
                case DANDAN_STAR, LEMON_FLAMBE, QINGYI_THUNDER -> 1.75;
                default -> ManorSkillDamageRules.STANDARD_MULTIPLIER;
            };
        }
    }

    private ManorFiveCombatRules() {}

    public static boolean supports(int variant) {
        return variant == 0 || variant == 1 || variant == 4 || variant == 6 || variant == 7;
    }

    public static boolean hasExpandedSkills(int variant) {
        return supports(variant) || variant == 5;
    }

    public static int rotationLength(int variant) {
        return variant == 5 ? 6 : 4;
    }

    public static double adaptiveDamageBonus(double maxHealth, int beneficialEffects) {
        double healthProgress = Double.isNaN(maxHealth) ? 0.0
                : Math.max(0.0, Math.min(1.0, (maxHealth - ADAPTIVE_HEALTH_BASE)
                / (ADAPTIVE_HEALTH_CAP - ADAPTIVE_HEALTH_BASE)));
        double buffProgress = Math.max(0, Math.min(ADAPTIVE_BUFF_CAP, beneficialEffects))
                / (double) ADAPTIVE_BUFF_CAP;
        return Math.max(ADAPTIVE_MIN_BONUS, Math.min(ADAPTIVE_MAX_BONUS,
                ADAPTIVE_MIN_BONUS + healthProgress * ADAPTIVE_HEALTH_BONUS
                        + buffProgress * ADAPTIVE_BUFF_BONUS));
    }

    public static Skill select(int variant, int rotation) {
        if (variant == 5) {
            return switch (Math.floorMod(rotation, rotationLength(variant))) {
                case 0 -> Skill.QINGYI_TIDE;
                case 2 -> Skill.QINGYI_THUNDER;
                case 4 -> Skill.QINGYI_STAR;
                default -> null;
            };
        }
        int slot = Math.floorMod(rotation + (variant == 4 ? 1 : 0), 4);
        if ((slot & 1) != 0) return null;
        return switch (variant) {
            case 0 -> slot == 0 ? Skill.YOUZI_SEAL : Skill.YOUZI_ECHO;
            case 1 -> slot == 0 ? Skill.DANDAN_REAP : Skill.DANDAN_STAR;
            case 4 -> slot == 0 ? Skill.LEMON_ACID : Skill.LEMON_FLAMBE;
            case 6 -> slot == 0 ? Skill.XUANLAN_RAIL : Skill.XUANLAN_RING;
            case 7 -> slot == 0 ? Skill.SHADOW_CROSS : Skill.SHADOW_CAGE;
            default -> null;
        };
    }

    public static int pulseAt(Skill skill, int age) {
        int elapsed = age - skill.windup;
        if (elapsed < 0 || elapsed % skill.interval != 0) return -1;
        int pulse = elapsed / skill.interval;
        return pulse < skill.pulses ? pulse : -1;
    }

    public static double waveRadius(Skill skill, int pulse) {
        double bonus = skill == Skill.XUANLAN_RING ? 1.0 : 0.0;
        return (skill.radius - bonus) * (pulse + 1.0) / skill.pulses + bonus;
    }

    public static double beamHalfWidth(Skill skill) {
        return skill == Skill.XUANLAN_RAIL ? 2.20 : 1.20;
    }

    public static boolean contains(Skill skill, int pulse, double forward, double side,
                                   double height, double halfWidth) {
        if (Math.abs(height) > 3.0) return false;
        double distance = Math.hypot(forward, side);
        return switch (skill.shape) {
            case CIRCLE, ECHO -> distance <= skill.radius + halfWidth;
            case WAVE -> Math.abs(distance - waveRadius(skill, pulse)) <= 1.15 + halfWidth;
            case CROSS -> Math.abs(forward) <= skill.radius + halfWidth && Math.abs(side) <= 1.15 + halfWidth
                    || Math.abs(side) <= skill.radius + halfWidth && Math.abs(forward) <= 1.15 + halfWidth;
            case BEAM -> forward >= -halfWidth && forward <= skill.radius + halfWidth
                    && Math.abs(side) <= beamHalfWidth(skill) + halfWidth;
            case CONE -> distance <= skill.radius + halfWidth && forward >= 0.0
                    && (distance <= halfWidth || forward / distance >= Math.cos(Math.toRadians(50.0)));
        };
    }
}
