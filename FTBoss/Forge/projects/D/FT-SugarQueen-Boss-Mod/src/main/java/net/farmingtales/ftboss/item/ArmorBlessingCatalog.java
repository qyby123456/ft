package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static net.farmingtales.ftboss.item.ArmorBlessingCatalog.Condition.*;
import static net.farmingtales.ftboss.item.ArmorBlessingCatalog.Mechanic.*;
import static net.farmingtales.ftboss.item.ArmorBlessingCatalog.Phase.*;

public final class ArmorBlessingCatalog {
    public enum Action { ARMOR, TOUGHNESS, HEALTH, KNOCKBACK, GUARD, HAZARD, ABSORPTION, REGENERATION, SPEED, CLEANSE }
    public enum Phase { ATTRIBUTE, HURT, DAMAGE, TICK }
    public enum Mechanic { STAT, REDUCE, POTION, COUNTER, CLEANSE, EXTINGUISH, FEED, AIR, REPAIR, REPEL, RESCUE, IMMUNE }
    public enum Condition {
        ALWAYS, FED, HUNGRY, LOW_HEALTH, CROUCHING, BURNING, PROJECTILE, GROUNDED, WOUNDED, WET, SPRINTING,
        AIRBORNE, NIGHT, DAY, UNDERWATER, MELEE, MAGIC, FALL, FIRE, EXPLOSION, POISONED,
        CROUCH_PROJECTILE, CROUCH_FALL, FED_MAGIC, FED_EXPLOSION, WET_FIRE, SPRINT_PROJECTILE, FREEZING, POISON
    }
    public record Context(double healthRatio, int food, boolean crouching, boolean burning, boolean projectile,
                          boolean grounded, boolean wet, boolean sprinting, boolean underwater, boolean night,
                          boolean melee, boolean magic, boolean fall, boolean fire, boolean explosion, boolean poisoned,
                          boolean freezing, boolean poisonDamage) {}
    public record Definition(String id, String profile, String nameZh, String nameEn, Action action, Phase phase,
                             Mechanic mechanic, Condition condition, String payload, double amount, int duration, int cooldown) {
        public double value(int level) { return amount * clampLevel(level); }
        public int ticks(int level) { return duration + 20 * clampLevel(level); }
        public String signature() { return phase + ":" + mechanic + ":" + condition + ":" + payload; }
        public String inspiration() {
            return switch (mechanic) {
                case AIR, COUNTER, REPEL -> "https://www.mcmod.cn/class/12224.html";
                case REPAIR, RESCUE, STAT -> "https://www.mcmod.cn/class/8194.html";
                case POTION, CLEANSE -> "https://www.mcmod.cn/class/493.html";
                default -> "https://www.mcmod.cn/class/21154.html";
            };
        }
        public String descriptionZh() {
            String timing = switch (phase) { case ATTRIBUTE -> "正确穿戴时"; case HURT -> "受伤减免阶段";
                case DAMAGE -> "实际扣血前（条件按扣血后预计生命判断，除保命外需存活）"; case TICK -> "穿戴期间每20 tick检查"; };
            String effect = switch (mechanic) {
                case STAT -> label(payload) + " +" + amount + "L" + (payload.equals("speed") ? "（基础值比例）" : "");
                case REDUCE -> "本次伤害减少" + Math.round(amount * 100) + "L%";
                case IMMUNE -> condition == Condition.POISON ? "免疫中毒效果与毒素伤害，穿戴时清除已有中毒"
                        : "完全免疫此类伤害" + (condition == Condition.FIRE ? "并熄灭自身火焰"
                        : condition == Condition.FREEZING ? "并清除冰冻积累" : "");
                case POTION -> "获得" + label(payload) + (payload.equals("absorption") ? " L级" : " I级")
                        + "，持续" + duration + "+20L tick";
                case COUNTER -> "给予非友方存活攻击者" + label(payload) + " I，持续" + duration + "+20L tick";
                case CLEANSE -> "移除" + label(payload);
                case EXTINGUISH -> "熄灭自身火焰";
                case FEED -> "饥饿未满时恢复" + amount + "L点饥饿，饱和系数0.1";
                case AIR -> "空气不足时补充" + amount + "L tick空气，不超过上限";
                case REPAIR -> "修复最破损的一件穿戴护甲" + amount + "L耐久，不修复已损坏护甲";
                case REPEL -> "击退非友方存活攻击者，强度" + amount + "L";
                case RESCUE -> "致死伤害时消耗max(1,4-L)级经验，保留1点生命（生命需大于1）";
            };
            return timing + "；条件：" + conditionZh(condition) + "；" + effect + "。冷却" + cooldown
                    + " tick。L=1..3；同ID触发取最高级，静态属性按件叠加；同药水不叠等级；持续状态离装后自然到期；受伤减免阶段词条减伤合计上限60%（不含原版护甲/抗性）。"
                    + (mechanic == Mechanic.IMMUNE ? "分类免疫各级均完全生效，脱装即失效，不阻止虚空或指令伤害。" : "");
        }
        public String descriptionEn() {
            String formula = switch (mechanic) {
                case STAT -> "+" + amount + "L " + payload + (payload.equals("speed") ? " (MULTIPLY_BASE)" : " (ADDITION)");
                case REDUCE -> "reduce incoming damage by " + Math.round(amount * 100) + "L%";
                case IMMUNE -> condition == Condition.POISON ? "prevent poison effects and poison damage; remove existing poison while worn"
                        : "fully prevent matching damage" + (condition == Condition.FIRE ? " and extinguish yourself"
                        : condition == Condition.FREEZING ? " and clear freezing buildup" : "");
                case POTION, COUNTER -> (mechanic == COUNTER ? "apply to living non-allied attacker: " : "gain ") + payload
                        + (payload.equals("absorption") ? " rank L" : " rank I") + " for " + duration + "+20L ticks";
                case CLEANSE -> "remove " + payload;
                case EXTINGUISH -> "extinguish yourself";
                case FEED -> "restore " + amount + "L hunger if hungry (saturation modifier 0.1)";
                case AIR -> "restore " + amount + "L air ticks, capped at maximum";
                case REPAIR -> "repair the most damaged worn unbroken armor by " + amount + "L durability";
                case REPEL -> "knock back living non-allied attacker with strength " + amount + "L";
                case RESCUE -> "on lethal damage, spend max(1,4-L) XP levels and keep 1 health; requires health >1";
            };
            return phase + "; condition=" + condition + "; " + formula + ". Cooldown " + cooldown
                    + " ticks. L=1..3. TICK checks every 20 ticks. DAMAGE uses projected remaining health and requires survival except RESCUE."
                    + " Same-ID triggers use highest rank; static stats stack per piece. Potion ranks do not add; existing effects expire naturally after removal. Direct HURT-rule reduction cap: 60%, excluding vanilla armor/resistance."
                    + (mechanic == Mechanic.IMMUNE ? " Typed immunities are complete at every rank, end on removal, and never block void or command damage." : "");
        }
    }
    private static final Map<String, List<Definition>> POOLS = new LinkedHashMap<>();
    private static final Map<String, Definition> BY_ID = new LinkedHashMap<>();
    private record Rule(String zh, String en, Phase phase, Mechanic mechanic, Condition condition, String payload, double amount, int duration, int cooldown) {}
    private static Rule rule(String zh, String en, Phase phase, Mechanic mechanic, Condition condition, String payload, double amount, int duration, int cooldown) {
        return new Rule(zh, en, phase, mechanic, condition, payload, amount, duration, cooldown);
    }
    static {
        add("sugar",
            rule("糖晶甲壳", "Sugar Plating", ATTRIBUTE, STAT, ALWAYS, "armor", .5, 0, 0),
            rule("糖霜韧层", "Frost Resilience", ATTRIBUTE, STAT, ALWAYS, "toughness", .3, 0, 0),
            rule("甜心生机", "Sweet Vitality", ATTRIBUTE, STAT, ALWAYS, "health", 1, 0, 0),
            rule("糖锚稳身", "Sugar Anchor", ATTRIBUTE, STAT, ALWAYS, "knockback", .02, 0, 0),
            rule("饱糖守护", "Sated Guard", HURT, REDUCE, FED, "", .02, 0, 0),
            rule("糖镜御咒", "Sugar Spell Mirror", HURT, REDUCE, MAGIC, "", .04, 0, 0),
            rule("糖衣缓冲", "Sugar Cushion", DAMAGE, POTION, LOW_HEALTH, "absorption", 1, 80, 240),
            rule("蜜糖回春", "Honey Recovery", DAMAGE, POTION, ALWAYS, "regeneration", 1, 40, 200),
            rule("糖风脱险", "Sugar Escape", DAMAGE, POTION, ALWAYS, "speed", 1, 20, 120),
            rule("霜雪不侵", "Snowproof", HURT, IMMUNE, FREEZING, "", 1, 0, 0));
        add("butcher",
            rule("血刃反攻", "Bloodblade Retort", ATTRIBUTE, STAT, ALWAYS, "attack_damage", .2, 0, 0),
            rule("绝境硬皮", "Last Stand Hide", TICK, POTION, LOW_HEALTH, "resistance", 1, 60, 0),
            rule("血肉壁垒", "Flesh Bulwark", TICK, POTION, LOW_HEALTH, "absorption", 1, 100, 600),
            rule("铁钩拒敌", "Hook Repulsion", DAMAGE, REPEL, MELEE, "", .3, 0, 100),
            rule("绝境护肉", "Desperate Guard", HURT, REDUCE, LOW_HEALTH, "", .02, 0, 0),
            rule("铁钩挡箭", "Hook Arrowguard", HURT, REDUCE, PROJECTILE, "", .04, 0, 0),
            rule("血怒还击", "Bloodrage", DAMAGE, POTION, WOUNDED, "strength", 1, 40, 240),
            rule("伤痕复苏", "Scar Recovery", DAMAGE, POTION, MELEE, "regeneration", 1, 40, 200),
            rule("绝境撤步", "Desperate Retreat", DAMAGE, POTION, LOW_HEALTH, "speed", 1, 40, 160),
            rule("血气祛弱", "Blood Purge", DAMAGE, CLEANSE, ALWAYS, "weakness", 1, 0, 160));
        add("saya",
            rule("茶签佑运", "Tea Fortune", ATTRIBUTE, STAT, ALWAYS, "luck", .25, 0, 0),
            rule("静叶凝神", "Still Leaf", TICK, POTION, CROUCHING, "resistance", 1, 60, 0),
            rule("坐忘回春", "Quiet Renewal", TICK, POTION, CROUCHING, "regeneration", 1, 60, 160),
            rule("盘根跃起", "Rooted Leap", TICK, POTION, CROUCHING, "jump", 1, 60, 0),
            rule("静叶守护", "Quiet Guard", HURT, REDUCE, CROUCHING, "", .02, 0, 0),
            rule("落叶无痕", "Feather Landing", HURT, IMMUNE, FALL, "", 1, 0, 0),
            rule("茶叶滑翔", "Tea Glide", TICK, POTION, AIRBORNE, "slow_falling", 1, 60, 0),
            rule("茶汤辟毒", "Tea Antitoxin", HURT, IMMUNE, POISON, "", 1, 0, 0),
            rule("饱茶巧手", "Sated Tea Hands", TICK, POTION, FED, "haste", 1, 60, 0),
            rule("夜茗明目", "Night Tea Sight", TICK, POTION, NIGHT, "night_vision", 1, 220, 0));
        add("wine",
            rule("燃酿隔膜", "Blazing Brew", DAMAGE, POTION, BURNING, "fire_resistance", 1, 60, 240),
            rule("醉火硬身", "Burning Resolve", DAMAGE, POTION, BURNING, "resistance", 1, 40, 240),
            rule("燃酿回春", "Ember Renewal", TICK, POTION, BURNING, "regeneration", 1, 60, 160),
            rule("酒沫灭火", "Foam Extinguisher", DAMAGE, EXTINGUISH, FIRE, "", 1, 0, 200),
            rule("燃酿守护", "Burning Guard", HURT, REDUCE, BURNING, "", .02, 0, 0),
            rule("酒窖隔火", "Cellar Fireward", HURT, IMMUNE, FIRE, "", 1, 0, 0),
            rule("火沫缓冲", "Ember Cushion", DAMAGE, POTION, FIRE, "absorption", 1, 80, 240),
            rule("烈酒醒神", "Spirited Resolve", TICK, CLEANSE, BURNING, "weakness", 1, 0, 160),
            rule("火场撤离", "Fire Escape", DAMAGE, POTION, FIRE, "speed", 1, 40, 160),
            rule("醉拳反制", "Drunken Retort", DAMAGE, COUNTER, MELEE, "nausea", 1, 60, 160));
        add("rat",
            rule("鼠径轻足", "Rat Trail", ATTRIBUTE, STAT, ALWAYS, "speed", .01, 0, 0),
            rule("鼠影潜踪", "Rat Veil", TICK, POTION, CROUCHING, "invisibility", 1, 60, 0),
            rule("饥鼠求生", "Hungry Survival", TICK, POTION, HUNGRY, "regeneration", 1, 60, 200),
            rule("夜鼠寻宝", "Night Scavenger", TICK, POTION, NIGHT, "luck", 1, 60, 0),
            rule("绝境藏粮", "Emergency Rations", TICK, FEED, LOW_HEALTH, "", 1, 0, 400),
            rule("饱腹御咒", "Sated Spellward", HURT, REDUCE, FED_MAGIC, "", .04, 0, 0),
            rule("箭雨藏身", "Arrow Shelter", DAMAGE, POTION, PROJECTILE, "absorption", 1, 80, 240),
            rule("箭毒净化", "Arrow Antidote", DAMAGE, CLEANSE, PROJECTILE, "poison", 1, 0, 160),
            rule("伏身疾行", "Crouched Escape", TICK, POTION, CROUCHING, "speed", 1, 60, 0),
            rule("疫刺反制", "Plague Retort", DAMAGE, COUNTER, MELEE, "poison", 1, 40, 200));
        add("defect",
            rule("法能重构", "Arcane Reconstruction", DAMAGE, REPAIR, MAGIC, "", 1, 0, 240),
            rule("接地维护", "Grounded Maintenance", TICK, REPAIR, GROUNDED, "", 1, 0, 400),
            rule("满电护盾", "Charged Shield", TICK, POTION, FED, "absorption", 1, 100, 600),
            rule("弹道装甲", "Ballistic Shell", DAMAGE, POTION, PROJECTILE, "resistance", 1, 40, 240),
            rule("伏姿屏障", "Covered Barrier", HURT, REDUCE, CROUCH_PROJECTILE, "", .04, 0, 0),
            rule("防爆隔舱", "Blast Compartment", HURT, REDUCE, EXPLOSION, "", .04, 0, 0),
            rule("弹道标定", "Ballistic Tracking", DAMAGE, COUNTER, PROJECTILE, "glowing", 1, 100, 120),
            rule("电磁阻滞", "Magnetic Snare", DAMAGE, COUNTER, PROJECTILE, "slowness", 1, 40, 160),
            rule("爆破脱离", "Blast Evasion", DAMAGE, POTION, EXPLOSION, "speed", 1, 40, 160),
            rule("接地解锁", "Grounded Unlock", TICK, CLEANSE, GROUNDED, "slowness", 1, 0, 200));
        add("compost",
            rule("腐殖补给", "Compost Rations", DAMAGE, FEED, MELEE, "", 1, 0, 300),
            rule("根网固守", "Rooted Resolve", TICK, POTION, GROUNDED, "resistance", 1, 60, 0),
            rule("向阳生机", "Sunlit Renewal", TICK, POTION, DAY, "regeneration", 1, 60, 200),
            rule("腐败净化", "Decay Purge", DAMAGE, CLEANSE, ALWAYS, "wither", 1, 0, 160),
            rule("接地守护", "Grounded Guard", HURT, REDUCE, GROUNDED, "", .02, 0, 0),
            rule("伏身软着陆", "Crouched Landing", HURT, REDUCE, CROUCH_FALL, "", .04, 0, 0),
            rule("泥膜缓落", "Mud Landing", DAMAGE, POTION, FALL, "absorption", 1, 80, 240),
            rule("毒中回春", "Toxic Renewal", DAMAGE, POTION, POISONED, "regeneration", 1, 40, 200),
            rule("疫土脱险", "Toxic Escape", TICK, POTION, POISONED, "speed", 1, 60, 0),
            rule("沃土祛凋", "Soil Purification", TICK, CLEANSE, ALWAYS, "wither", 1, 0, 200));
        add("apple",
            rule("果园口粮", "Orchard Rations", TICK, FEED, HUNGRY, "", 1, 0, 400),
            rule("饱果回春", "Sated Renewal", TICK, POTION, FED, "regeneration", 1, 60, 200),
            rule("金苹果契约", "Golden Apple Pact", DAMAGE, RESCUE, ALWAYS, "", 1, 0, 1200),
            rule("爆风浮叶", "Blast Leaf", DAMAGE, POTION, EXPLOSION, "slow_falling", 1, 60, 240),
            rule("伤果守护", "Wounded Guard", HURT, REDUCE, WOUNDED, "", .02, 0, 0),
            rule("饱果防爆", "Sated Blastward", HURT, REDUCE, FED_EXPLOSION, "", .04, 0, 0),
            rule("果胶防爆", "Pectin Shield", DAMAGE, POTION, EXPLOSION, "absorption", 1, 80, 240),
            rule("飞矢补给", "Arrow Rations", DAMAGE, FEED, PROJECTILE, "", 1, 0, 300),
            rule("跃枝疾跑", "Branch Sprint", TICK, POTION, SPRINTING, "jump", 1, 60, 0),
            rule("果香止饥", "Apple Purge", DAMAGE, CLEANSE, ALWAYS, "hunger", 1, 0, 160));
        add("tide",
            rule("潮汐呼吸", "Tidal Breath", TICK, POTION, UNDERWATER, "water_breathing", 1, 60, 0),
            rule("海豚护航", "Dolphin Escort", TICK, POTION, WET, "dolphins_grace", 1, 60, 0),
            rule("深潜复苏", "Diving Renewal", TICK, POTION, UNDERWATER, "regeneration", 1, 60, 160),
            rule("深海储气", "Deep Air Reserve", TICK, AIR, UNDERWATER, "", 20, 0, 100),
            rule("湿鳞守护", "Wet Scale Guard", HURT, REDUCE, WET, "", .02, 0, 0),
            rule("湿鳞隔火", "Wet Fireward", HURT, REDUCE, WET_FIRE, "", .04, 0, 0),
            rule("水幕缓冲", "Water Cushion", DAMAGE, POTION, WET, "absorption", 1, 80, 240),
            rule("海流祛疲", "Current Purge", TICK, CLEANSE, WET, "mining_fatigue", 1, 0, 200),
            rule("浪涌脱险", "Wave Escape", DAMAGE, POTION, WET, "speed", 1, 40, 160),
            rule("深渊反制", "Abyssal Retort", DAMAGE, COUNTER, MELEE, "mining_fatigue", 1, 60, 160));
        add("benger",
            rule("群猎轻足", "Pack Pace", TICK, POTION, SPRINTING, "speed", 1, 60, 0),
            rule("奔猎硬身", "Running Resolve", TICK, POTION, SPRINTING, "resistance", 1, 60, 0),
            rule("牧野回春", "Running Renewal", TICK, POTION, SPRINTING, "regeneration", 1, 60, 200),
            rule("越矢腾跃", "Arrow Leap", DAMAGE, POTION, PROJECTILE, "jump", 1, 40, 160),
            rule("奔猎守护", "Running Guard", HURT, REDUCE, SPRINTING, "", .02, 0, 0),
            rule("奔猎挡箭", "Running Arrowguard", HURT, REDUCE, SPRINT_PROJECTILE, "", .04, 0, 0),
            rule("绒盾奔护", "Running Fleece", DAMAGE, POTION, SPRINTING, "absorption", 1, 80, 240),
            rule("绝境牧粮", "Last Stand Rations", DAMAGE, FEED, LOW_HEALTH, "", 1, 0, 300),
            rule("脱钩疾行", "Melee Escape", DAMAGE, POTION, MELEE, "speed", 1, 40, 160),
            rule("猎途净步", "Running Purge", DAMAGE, CLEANSE, SPRINTING, "slowness", 1, 0, 160));
        if (BY_ID.size() != 100 || BY_ID.values().stream().map(Definition::signature).distinct().count() != 100)
            throw new IllegalStateException("Expected 100 structurally distinct armor rules");
    }
    private ArmorBlessingCatalog() {}
    private static void add(String profile, Rule... rules) {
        if (rules.length != 10) throw new IllegalStateException(profile);
        List<Definition> pool = new ArrayList<>();
        for (int index = 0; index < rules.length; index++) {
            Rule rule = rules[index];
            Action action = Action.values()[index];
            String id = "armor_" + profile + "_" + action.name().toLowerCase(java.util.Locale.ROOT);
            Definition definition = new Definition(id, profile, rule.zh(), rule.en(), action, rule.phase(), rule.mechanic(),
                    rule.condition(), rule.payload(), rule.amount(), rule.duration(), rule.cooldown());
            if (BY_ID.put(id, definition) != null) throw new IllegalStateException(id);
            pool.add(definition);
        }
        POOLS.put(profile, List.copyOf(pool));
    }
    public static int clampLevel(int level) { return Math.max(1, Math.min(3, level)); }
    public static List<Definition> pool(String profile) { return profile == null ? List.of() : POOLS.getOrDefault(profile, List.of()); }
    public static Definition byId(String id) { return BY_ID.get(id); }
    public static List<Definition> all() { return List.copyOf(BY_ID.values()); }
    public static double multiplier(double reduction) { return Math.max(.4, 1 - Math.max(0, reduction)); }
    public static boolean ready(boolean present, long previous, long now, int cooldown) {
        return !present || now < previous || now - previous >= cooldown;
    }
    public static boolean matches(Condition condition, Context context) {
        return switch (condition) {
            case ALWAYS -> true; case FED -> context.food() >= 16; case HUNGRY -> context.food() < 16;
            case LOW_HEALTH -> context.healthRatio() <= .5; case WOUNDED -> context.healthRatio() <= .75;
            case CROUCHING -> context.crouching(); case BURNING -> context.burning(); case PROJECTILE -> context.projectile();
            case GROUNDED -> context.grounded(); case AIRBORNE -> !context.grounded(); case WET -> context.wet();
            case SPRINTING -> context.sprinting(); case UNDERWATER -> context.underwater(); case NIGHT -> context.night(); case DAY -> !context.night();
            case MELEE -> context.melee(); case MAGIC -> context.magic(); case FALL -> context.fall(); case FIRE -> context.fire();
            case EXPLOSION -> context.explosion(); case POISONED -> context.poisoned();
            case FREEZING -> context.freezing(); case POISON -> context.poisonDamage();
            case CROUCH_PROJECTILE -> context.crouching() && context.projectile(); case CROUCH_FALL -> context.crouching() && context.fall();
            case FED_MAGIC -> context.food() >= 16 && context.magic(); case FED_EXPLOSION -> context.food() >= 16 && context.explosion();
            case WET_FIRE -> context.wet() && context.fire(); case SPRINT_PROJECTILE -> context.sprinting() && context.projectile();
        };
    }
    private static String conditionZh(Condition condition) {
        return switch (condition) {
            case ALWAYS -> "无额外条件"; case FED -> "饥饿值≥16"; case HUNGRY -> "饥饿值<16";
            case LOW_HEALTH -> "生命≤50%"; case WOUNDED -> "生命≤75%"; case CROUCHING -> "潜行"; case BURNING -> "着火";
            case PROJECTILE -> "弹射物伤害"; case GROUNDED -> "着地"; case AIRBORNE -> "腾空"; case WET -> "水中、雨中或气泡柱";
            case SPRINTING -> "疾跑"; case UNDERWATER -> "眼部没入水中"; case NIGHT -> "夜晚"; case DAY -> "白天";
            case MELEE -> "存活生物的直接接触伤害"; case MAGIC -> "原版魔法、铁魔法及带魔法标签的法术伤害"; case FALL -> "摔落伤害";
            case FIRE -> "火焰伤害"; case EXPLOSION -> "爆炸伤害"; case POISONED -> "处于中毒";
            case FREEZING -> "细雪等冰冻伤害"; case POISON -> "中毒效果或毒素伤害（不免疫其他魔法）";
            case CROUCH_PROJECTILE -> "潜行且受到弹射物伤害"; case CROUCH_FALL -> "潜行且受到摔落伤害";
            case FED_MAGIC -> "饥饿值≥16且受到魔法伤害"; case FED_EXPLOSION -> "饥饿值≥16且受到爆炸伤害";
            case WET_FIRE -> "湿身且受到火焰伤害"; case SPRINT_PROJECTILE -> "疾跑且受到弹射物伤害";
        };
    }
    private static String label(String key) {
        return switch (key) {
            case "armor" -> "护甲"; case "toughness" -> "韧性"; case "health" -> "最大生命"; case "knockback" -> "击退抗性";
            case "attack_damage" -> "近战攻击力"; case "luck" -> "幸运"; case "speed" -> "移动速度"; case "resistance" -> "抗性提升";
            case "absorption" -> "伤害吸收"; case "regeneration" -> "生命恢复"; case "slowness" -> "缓慢"; case "weakness" -> "虚弱";
            case "jump" -> "跳跃提升"; case "slow_falling" -> "缓降"; case "poison" -> "中毒"; case "haste" -> "急迫";
            case "night_vision" -> "夜视"; case "fire_resistance" -> "防火"; case "nausea" -> "反胃"; case "invisibility" -> "隐身";
            case "glowing" -> "发光"; case "wither" -> "凋零"; case "hunger" -> "饥饿"; case "strength" -> "力量";
            case "water_breathing" -> "水下呼吸"; case "dolphins_grace" -> "海豚的恩惠"; case "mining_fatigue" -> "挖掘疲劳";
            default -> throw new IllegalArgumentException(key);
        };
    }
}
