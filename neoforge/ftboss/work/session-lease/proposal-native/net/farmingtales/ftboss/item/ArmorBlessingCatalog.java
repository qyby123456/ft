package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ArmorBlessingCatalog {
    private static final Map<String, List<ArmorBlessingCatalog.Definition>> POOLS = Map.of();
    private static final Map<String, ArmorBlessingCatalog.Definition> BY_ID = Map.of();



    private ArmorBlessingCatalog() {
    }



    public static int clampLevel(int var0) {
        return Math.max(1, Math.min(3, var0));
    }

    public static List<ArmorBlessingCatalog.Definition> pool(String var0) {
        return var0 == null ? List.of() : POOLS.getOrDefault(var0, List.of());
    }

    public static ArmorBlessingCatalog.Definition byId(String var0) {
        return BY_ID.get(var0);
    }

    public static List<ArmorBlessingCatalog.Definition> all() {
        return List.copyOf(BY_ID.values());
    }

    public static double multiplier(double var0) {
        return Math.max(0.4, 1.0 - Math.max(0.0, var0));
    }

    public static boolean ready(boolean var0, long var1, long var3, int var5) {
        return !var0 || var3 < var1 || var3 - var1 >= (long)var5;
    }

    public static boolean matches(ArmorBlessingCatalog.Condition var0, ArmorBlessingCatalog.Context var1) {
        return switch (var0) {
            case ALWAYS -> true;
            case FED -> var1.food() >= 16;
            case HUNGRY -> var1.food() < 16;
            case LOW_HEALTH -> var1.healthRatio() <= 0.5;
            case WOUNDED -> var1.healthRatio() <= 0.75;
            case CROUCHING -> var1.crouching();
            case BURNING -> var1.burning();
            case PROJECTILE -> var1.projectile();
            case GROUNDED -> var1.grounded();
            case AIRBORNE -> !var1.grounded();
            case WET -> var1.wet();
            case SPRINTING -> var1.sprinting();
            case UNDERWATER -> var1.underwater();
            case NIGHT -> var1.night();
            case DAY -> !var1.night();
            case MELEE -> var1.melee();
            case MAGIC -> var1.magic();
            case FALL -> var1.fall();
            case FIRE -> var1.fire();
            case EXPLOSION -> var1.explosion();
            case POISONED -> var1.poisoned();
            case FREEZING -> var1.freezing();
            case POISON -> var1.poisonDamage();
            case CROUCH_PROJECTILE -> var1.crouching() && var1.projectile();
            case CROUCH_FALL -> var1.crouching() && var1.fall();
            case FED_MAGIC -> var1.food() >= 16 && var1.magic();
            case FED_EXPLOSION -> var1.food() >= 16 && var1.explosion();
            case WET_FIRE -> var1.wet() && var1.fire();
            case SPRINT_PROJECTILE -> var1.sprinting() && var1.projectile();
        };
    }

    private static String conditionZh(ArmorBlessingCatalog.Condition var0) {
        return switch (var0) {
            case ALWAYS -> "无额外条件";
            case FED -> "饥饿值≥16";
            case HUNGRY -> "饥饿值<16";
            case LOW_HEALTH -> "生命≤50%";
            case WOUNDED -> "生命≤75%";
            case CROUCHING -> "潜行";
            case BURNING -> "着火";
            case PROJECTILE -> "弹射物伤害";
            case GROUNDED -> "着地";
            case AIRBORNE -> "腾空";
            case WET -> "水中、雨中或气泡柱";
            case SPRINTING -> "疾跑";
            case UNDERWATER -> "眼部没入水中";
            case NIGHT -> "夜晚";
            case DAY -> "白天";
            case MELEE -> "存活生物的直接接触伤害";
            case MAGIC -> "原版魔法、铁魔法及带魔法标签的法术伤害";
            case FALL -> "摔落伤害";
            case FIRE -> "火焰伤害";
            case EXPLOSION -> "爆炸伤害";
            case POISONED -> "处于中毒";
            case FREEZING -> "细雪等冰冻伤害";
            case POISON -> "中毒效果或毒素伤害（不免疫其他魔法）";
            case CROUCH_PROJECTILE -> "潜行且受到弹射物伤害";
            case CROUCH_FALL -> "潜行且受到摔落伤害";
            case FED_MAGIC -> "饥饿值≥16且受到魔法伤害";
            case FED_EXPLOSION -> "饥饿值≥16且受到爆炸伤害";
            case WET_FIRE -> "湿身且受到火焰伤害";
            case SPRINT_PROJECTILE -> "疾跑且受到弹射物伤害";
        };
    }

    private static String label(String var0) {
        return switch (var0) {
            case "armor" -> "护甲";
            case "toughness" -> "韧性";
            case "health" -> "最大生命";
            case "knockback" -> "击退抗性";
            case "attack_damage" -> "近战攻击力";
            case "luck" -> "幸运";
            case "speed" -> "移动速度";
            case "resistance" -> "抗性提升";
            case "absorption" -> "伤害吸收";
            case "regeneration" -> "生命恢复";
            case "slowness" -> "缓慢";
            case "weakness" -> "虚弱";
            case "jump" -> "跳跃提升";
            case "slow_falling" -> "缓降";
            case "poison" -> "中毒";
            case "haste" -> "急迫";
            case "night_vision" -> "夜视";
            case "fire_resistance" -> "防火";
            case "nausea" -> "反胃";
            case "invisibility" -> "隐身";
            case "glowing" -> "发光";
            case "wither" -> "凋零";
            case "hunger" -> "饥饿";
            case "strength" -> "力量";
            case "water_breathing" -> "水下呼吸";
            case "dolphins_grace" -> "海豚的恩惠";
            case "mining_fatigue" -> "挖掘疲劳";
            default -> throw new IllegalArgumentException(var0);
        };
    }



    public static enum Action {
        ARMOR,
        TOUGHNESS,
        HEALTH,
        KNOCKBACK,
        GUARD,
        HAZARD,
        ABSORPTION,
        REGENERATION,
        SPEED,
        CLEANSE;
    }

    public static enum Condition {
        ALWAYS,
        FED,
        HUNGRY,
        LOW_HEALTH,
        CROUCHING,
        BURNING,
        PROJECTILE,
        GROUNDED,
        WOUNDED,
        WET,
        SPRINTING,
        AIRBORNE,
        NIGHT,
        DAY,
        UNDERWATER,
        MELEE,
        MAGIC,
        FALL,
        FIRE,
        EXPLOSION,
        POISONED,
        CROUCH_PROJECTILE,
        CROUCH_FALL,
        FED_MAGIC,
        FED_EXPLOSION,
        WET_FIRE,
        SPRINT_PROJECTILE,
        FREEZING,
        POISON;
    }

    public static record Context(
        double healthRatio,
        int food,
        boolean crouching,
        boolean burning,
        boolean projectile,
        boolean grounded,
        boolean wet,
        boolean sprinting,
        boolean underwater,
        boolean night,
        boolean melee,
        boolean magic,
        boolean fall,
        boolean fire,
        boolean explosion,
        boolean poisoned,
        boolean freezing,
        boolean poisonDamage
    ) {
    }

    public static record Definition(
        String id,
        String profile,
        String nameZh,
        String nameEn,
        ArmorBlessingCatalog.Action action,
        ArmorBlessingCatalog.Phase phase,
        ArmorBlessingCatalog.Mechanic mechanic,
        ArmorBlessingCatalog.Condition condition,
        String payload,
        double amount,
        int duration,
        int cooldown
    ) {
        public double value(int var1) {
            return this.amount * (double)ArmorBlessingCatalog.clampLevel(var1);
        }

        public int ticks(int var1) {
            return this.duration + 20 * ArmorBlessingCatalog.clampLevel(var1);
        }

        public String signature() {
            return this.phase + ":" + this.mechanic + ":" + this.condition + ":" + this.payload;
        }

        public String inspiration() {
            return switch (this.mechanic) {
                case AIR, COUNTER, REPEL -> "https://www.mcmod.cn/class/12224.html";
                case REPAIR, RESCUE, STAT -> "https://www.mcmod.cn/class/8194.html";
                case POTION, CLEANSE -> "https://www.mcmod.cn/class/493.html";
                default -> "https://www.mcmod.cn/class/21154.html";
            };
        }

        public String descriptionZh() {
            String var1 = switch (this.phase) {
                case ATTRIBUTE -> "正确穿戴时";
                case HURT -> "受伤减免阶段";
                case DAMAGE -> "实际扣血前（条件按扣血后预计生命判断，除保命外需存活）";
                case TICK -> "穿戴期间每20 tick检查";
            };

            String var2 = switch (this.mechanic) {
                case AIR -> "空气不足时补充" + this.amount + "L tick空气，不超过上限";
                case COUNTER -> "给予非友方存活攻击者" + ArmorBlessingCatalog.label(this.payload) + " I，持续" + this.duration + "+20L tick";
                case REPEL -> "击退非友方存活攻击者，强度" + this.amount + "L";
                case REPAIR -> "修复最破损的一件穿戴护甲" + this.amount + "L耐久，不修复已损坏护甲";
                case RESCUE -> "致死伤害时消耗max(1,4-L)级经验，保留1点生命（生命需大于1）";
                case STAT -> ArmorBlessingCatalog.label(this.payload) + " +" + this.amount + "L" + (this.payload.equals("speed") ? "（基础值比例）" : "");
                case POTION -> "获得"
                + ArmorBlessingCatalog.label(this.payload)
                + (this.payload.equals("absorption") ? " L级" : " I级")
                + "，持续"
                + this.duration
                + "+20L tick";
                case CLEANSE -> "移除" + ArmorBlessingCatalog.label(this.payload);
                case REDUCE -> "本次伤害减少" + Math.round(this.amount * 100.0) + "L%";
                case IMMUNE -> this.condition == ArmorBlessingCatalog.Condition.POISON
                ? "免疫中毒效果与毒素伤害，穿戴时清除已有中毒"
                : "完全免疫此类伤害"
                    + (
                        this.condition == ArmorBlessingCatalog.Condition.FIRE
                            ? "并熄灭自身火焰"
                            : (this.condition == ArmorBlessingCatalog.Condition.FREEZING ? "并清除冰冻积累" : "")
                    );
                case EXTINGUISH -> "熄灭自身火焰";
                case FEED -> "饥饿未满时恢复" + this.amount + "L点饥饿，饱和系数0.1";
            };
            return var1
                + "；条件："
                + ArmorBlessingCatalog.conditionZh(this.condition)
                + "；"
                + var2
                + "。冷却"
                + this.cooldown
                + " tick。L=1..3；同ID触发取最高级，静态属性按件叠加；同药水不叠等级；持续状态离装后自然到期；受伤减免阶段词条减伤合计上限60%（不含原版护甲/抗性）。"
                + (this.mechanic == ArmorBlessingCatalog.Mechanic.IMMUNE ? "分类免疫各级均完全生效，脱装即失效，不阻止虚空或指令伤害。" : "");
        }

        public String descriptionEn() {
            String var1 = switch (this.mechanic) {
                case AIR -> "restore " + this.amount + "L air ticks, capped at maximum";
                case COUNTER, POTION -> (this.mechanic == ArmorBlessingCatalog.Mechanic.COUNTER ? "apply to living non-allied attacker: " : "gain ")
                + this.payload
                + (this.payload.equals("absorption") ? " rank L" : " rank I")
                + " for "
                + this.duration
                + "+20L ticks";
                case REPEL -> "knock back living non-allied attacker with strength " + this.amount + "L";
                case REPAIR -> "repair the most damaged worn unbroken armor by " + this.amount + "L durability";
                case RESCUE -> "on lethal damage, spend max(1,4-L) XP levels and keep 1 health; requires health >1";
                case STAT -> "+" + this.amount + "L " + this.payload + (this.payload.equals("speed") ? " (MULTIPLY_BASE)" : " (ADDITION)");
                case CLEANSE -> "remove " + this.payload;
                case REDUCE -> "reduce incoming damage by " + Math.round(this.amount * 100.0) + "L%";
                case IMMUNE -> this.condition == ArmorBlessingCatalog.Condition.POISON
                ? "prevent poison effects and poison damage; remove existing poison while worn"
                : "fully prevent matching damage"
                    + (
                        this.condition == ArmorBlessingCatalog.Condition.FIRE
                            ? " and extinguish yourself"
                            : (this.condition == ArmorBlessingCatalog.Condition.FREEZING ? " and clear freezing buildup" : "")
                    );
                case EXTINGUISH -> "extinguish yourself";
                case FEED -> "restore " + this.amount + "L hunger if hungry (saturation modifier 0.1)";
            };
            return this.phase
                + "; condition="
                + this.condition
                + "; "
                + var1
                + ". Cooldown "
                + this.cooldown
                + " ticks. L=1..3. TICK checks every 20 ticks. DAMAGE uses projected remaining health and requires survival except RESCUE. Same-ID triggers use highest rank; static stats stack per piece. Potion ranks do not add; existing effects expire naturally after removal. Direct HURT-rule reduction cap: 60%, excluding vanilla armor/resistance."
                + (
                    this.mechanic == ArmorBlessingCatalog.Mechanic.IMMUNE
                        ? " Typed immunities are complete at every rank, end on removal, and never block void or command damage."
                        : ""
                );
        }
    }

    public static enum Mechanic {
        STAT,
        REDUCE,
        POTION,
        COUNTER,
        CLEANSE,
        EXTINGUISH,
        FEED,
        AIR,
        REPAIR,
        REPEL,
        RESCUE,
        IMMUNE;
    }

    public static enum Phase {
        ATTRIBUTE,
        HURT,
        DAMAGE,
        TICK;
    }

    private static record Rule(
        String zh,
        String en,
        ArmorBlessingCatalog.Phase phase,
        ArmorBlessingCatalog.Mechanic mechanic,
        ArmorBlessingCatalog.Condition condition,
        String payload,
        double amount,
        int duration,
        int cooldown
    ) {
    }
}
