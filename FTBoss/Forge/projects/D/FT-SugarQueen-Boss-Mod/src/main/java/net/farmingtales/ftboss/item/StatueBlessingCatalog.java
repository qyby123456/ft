package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 十座食物石像各自的专属附魔目录（共 100 项，每座恰 10 项）。
 *
 * <p>结构约定：每个 profile 的池子前 3 项是既有 {@code FoodWeaponEvolution.FoodBlessing} 枚举中
 * 连续的那 3 个（原 30 项 ID 与效果保持不变），后 7 项是本次新增的专属附魔；所有池互不重叠，
 * 100 个 ID 全局唯一。目录只承载数据，不依赖任何 Minecraft 类，供石像交互、语言文件与
 * {@link StatueBlessingCombat} 共用。</p>
 *
 * <p>关键参数：{@code descriptionZh}/{@code descriptionEn} 必须给出真实等级公式（等级 L 为 1..3）、
 * 触发条件与冷却；{@code inspiration} 只记录机制参考来源，注明“独立实现”表示未复制参考模组源码。</p>
 *
 * <p>修改历史：2026-10-25 新建，按 sugar,butcher,saya,wine,rat,defect,compost,apple,tide,benger
 * 顺序建立 10 池 × (原 3 + 新 7) 的 100 项目录。</p>
 */
public final class StatueBlessingCatalog {
    /** 池顺序即石像顺序；每座池子恰好 10 项且互不重叠。 */
    public record Definition(String id, String profile, String nameZh, String nameEn,
                             String descriptionZh, String descriptionEn, String inspiration) {}

    private static final String ORIGINAL_NOTE = "既有食物附魔（保持原 ID 与机制不变）";

    private static final List<Definition> SUGAR = List.of(
            d("candied_edge", "sugar", "晶糖斩杀", "Candied Execution",
                    "等级L：目标生命 ≤(12+5L)% 时，本次命中伤害 ×(1.20+0.12L)。",
                    "Rank L: when the target is at or below (12+5L)% health, this hit deals x(1.20+0.12L) damage.",
                    ORIGINAL_NOTE),
            d("syrup_step", "sugar", "糖浆疾步", "Syrup Step",
                    "等级L：近战攻击速度 +0.04L；弩装填 +4%L；法杖施法时间 -0.04L。",
                    "Rank L: +0.04L melee attack speed; +4%L crossbow reload; -0.04L staff cast time.",
                    ORIGINAL_NOTE),
            d("frosted_shell", "sugar", "霜糖护壳", "Frosted Shell",
                    "等级L：命中时 (12+5L)% 概率获得伤害吸收（60+20L tick，等级L-1）。",
                    "Rank L: (12+5L)% chance on hit to gain Absorption for 60+20L ticks at level L-1.",
                    ORIGINAL_NOTE),
            d("sugar_spun_guard", "sugar", "糖丝缠甲", "Spun-Sugar Aegis",
                    "等级L：命中时若生命 ≤50% 且 140 tick 冷却就绪，回复 0.5+0.25L 生命并获得吸收（60+20L tick）。",
                    "Rank L: on hit, if health <=50% and the 140-tick cooldown is ready, heal 0.5+0.25L and gain Absorption for 60+20L ticks.",
                    "参考 Ensorcellation 防御向附魔与吸收护盾概念；独立实现。"),
            d("sugar_glass_edge", "sugar", "琉璃糖刃", "Glass-Candy Edge",
                    "等级L：命中生命 ≥90% 的目标时额外造成 1.0+0.5L 伤害。",
                    "Rank L: hits against targets at >=90% health deal 1.0+0.5L bonus damage.",
                    "参考 Apotheosis 满血目标增伤（Instigating 类）概念；独立实现。"),
            d("sugar_caramel_snare", "sugar", "焦糖缚足", "Caramel Snare",
                    "等级L：命中距离 ≤3 格的目标时施加缓慢 II（40+10L tick）；60 tick 冷却。",
                    "Rank L: hits on targets within 3 blocks apply Slowness II for 40+10L ticks; 60-tick cooldown.",
                    "参考 Quark 冰霜/黏滞减速类附魔概念；独立实现（近距条件）。"),
            d("sugar_frost_lattice", "sugar", "霜纹格挡", "Frost Lattice",
                    "等级L：受到弹射物伤害时减伤 (10+4L)%，100 tick 冷却。",
                    "Rank L: incoming projectile damage is reduced by (10+4L)%, 100-tick cooldown.",
                    "参考 Ensorcellation 投射物防护类附魔概念；独立实现。"),
            d("sugar_syrup_surge", "sugar", "糖浆涌动", "Syrup Surge",
                    "等级L：击杀回复 1.0+0.5L 生命并获得速度 I（40+15L tick）。",
                    "Rank L: kills heal 1.0+0.5L and grant Speed I for 40+15L ticks.",
                    "参考 CoFH/Ensorcellation Leech 击杀回复概念；独立实现。"),
            d("sugar_peppermint_veil", "sugar", "薄荷薄幕", "Peppermint Veil",
                    "等级L：生命 ≤40% 时受到伤害减伤 (7+3L)% 并获得吸收（60 tick，等级L-1），150 tick 冷却。",
                    "Rank L: at <=40% health, incoming damage is reduced by (7+3L)% and grants Absorption for 60 ticks at level L-1, 150-tick cooldown.",
                    "参考 Ensorcellation/Apotheosis 低血量防御向附魔概念；独立实现。"),
            d("sugar_amber_heart", "sugar", "琥珀蜜心", "Amber Honey Heart",
                    "等级L：持握时最大生命 +2.0L。",
                    "Rank L: +2.0L maximum health while held.",
                    "参考 Ensorcellation Vitality（最大生命）概念；独立实现。")
    );

    private static final List<Definition> BUTCHER = List.of(
            d("cleaver_might", "butcher", "屠刀蛮力", "Cleaver Might",
                    "等级L：近战攻击伤害 +0.75L；弓弩/三叉戟按同值折算为远程增伤。",
                    "Rank L: +0.75L melee damage; bows/crossbows/tridents convert the same value into ranged damage.",
                    ORIGINAL_NOTE),
            d("butcher_hide", "butcher", "屠夫厚皮", "Butcher Hide",
                    "等级L：生命 ≤50% 时受到伤害 ×(1-0.09L)，减伤下限 55%。",
                    "Rank L: at <=50% health, incoming damage x(1-0.09L) with a 55% reduction floor.",
                    ORIGINAL_NOTE),
            d("blood_feast", "butcher", "血肉盛宴", "Blood Feast",
                    "等级L：命中回复 0.35L 生命，每 3 秒最多一次。",
                    "Rank L: hits heal 0.35L, at most once every 3 seconds.",
                    ORIGINAL_NOTE),
            d("butcher_gore_cleave", "butcher", "裂肉重劈", "Gore Cleave",
                    "等级L：命中后叠 1 层裂伤（上限 L+1 层，60 tick 未命中清零），已有每层使后续命中 +0.6 伤害。",
                    "Rank L: hits add a Gash stack (cap L+1, expires after 60 ticks); each existing stack adds +0.6 damage to subsequent hits.",
                    "参考 Apotheosis 流血/裂伤叠层增伤概念；独立实现（层数上限与过期）。"),
            d("butcher_hook_pull", "butcher", "屠钩拖拽", "Butcher Hook",
                    "等级L：80 tick 冷却；命中时把目标拉近（强度 0.28）并使其缓慢 40 tick。",
                    "Rank L: 80-tick cooldown; hits pull the target closer (strength 0.28) and apply Slowness for 40 ticks.",
                    "参考 Alex's Mobs 拖拽/钩爪类能力概念；独立实现。"),
            d("butcher_blood_armor", "butcher", "血纹护体", "Bloodwrought Armor",
                    "等级L：受到近战伤害时回复 min(2.0+1.0L, 伤害×10%) 生命，120 tick 冷却。",
                    "Rank L: melee damage taken heals min(2.0+1.0L, 10% of damage), 120-tick cooldown.",
                    "参考 CoFH Leech 生命偷取方向；独立实现（受伤转化治疗）。"),
            d("butcher_rend", "butcher", "撕裂残躯", "Rend",
                    "等级L：目标已带虚弱/缓慢/中毒时，施加虚弱 II 与缓慢 I（60+15L tick）；80 tick 冷却。",
                    "Rank L: if the target already has Weakness, Slowness or Poison, inflict Weakness II and Slowness I for 60+15L ticks; 80-tick cooldown.",
                    "参考 Ensorcellation Frost Aspect（减速+虚弱）概念；独立实现（负面状态条件）。"),
            d("butcher_offal_feast", "butcher", "内脏盛宴", "Offal Feast",
                    "等级L：击杀时若生命 ≤60% 回复 1.5+0.75L 生命，否则获得抗性 I（60 tick）。",
                    "Rank L: on kill, heal 1.5+0.75L if at <=60% health, otherwise gain Resistance I for 60 ticks.",
                    "参考 Ensorcellation Leech（击杀回复）概念；独立实现（低血分支）。"),
            d("butcher_bone_breaker", "butcher", "碎骨践踏", "Bone Breaker",
                    "等级L：单次受到 ≥8 点伤害时减伤 (8+3L)%，120 tick 冷却。",
                    "Rank L: damage taken of 8 or more is reduced by (8+3L)%, 120-tick cooldown.",
                    "参考 Apotheosis 重击减伤/护甲向附魔概念；独立实现（大额伤害条件）。"),
            d("butcher_carving_armor", "butcher", "刻肉甲胄", "Carving Armor",
                    "等级L：持握时护甲 +1.0L。",
                    "Rank L: +1.0L armor while held.",
                    "参考 Ensorcellation 护甲强化方向；独立实现。")
    );

    private static final List<Definition> SAYA = List.of(
            d("tea_thorns", "saya", "茶棘毒锋", "Tea Thorns",
                    "等级L：(12+5L)% 概率使目标中毒（40+20L tick，等级L-1）。",
                    "Rank L: (12+5L)% chance to poison the target for 40+20L ticks at level L-1.",
                    ORIGINAL_NOTE),
            d("camellia_grace", "saya", "山茶回生", "Camellia Grace",
                    "等级L：(12+5L)% 概率获得再生（40+20L tick，等级L-1）。",
                    "Rank L: (12+5L)% chance to gain Regeneration for 40+20L ticks at level L-1.",
                    ORIGINAL_NOTE),
            d("rooted_focus", "saya", "根系凝神", "Rooted Focus",
                    "等级L：(12+5L)% 概率使目标缓慢（60+25L tick，等级 min(3,L)）。",
                    "Rank L: (12+5L)% chance to slow the target for 60+25L ticks at amplifier min(3,L).",
                    ORIGINAL_NOTE),
            d("saya_root_snare", "saya", "根须缚杀", "Root Snare",
                    "等级L：目标处于缓慢/中毒/虚弱之一时，本次命中额外 +1.2+0.6L 伤害。",
                    "Rank L: if the target has Slowness, Poison or Weakness, this hit deals +1.2+0.6L bonus damage.",
                    "参考 Ensorcellation Frost Aspect 的受控目标增伤概念；独立实现。"),
            d("saya_bud_shield", "saya", "茶芽壁垒", "Bud Bulwark",
                    "等级L：8 格内至少 2 个敌对生物时受到伤害减伤 (9+3L)%，140 tick 冷却。",
                    "Rank L: with at least 2 hostile mobs within 8 blocks, incoming damage is reduced by (9+3L)%, 140-tick cooldown.",
                    "参考 Alex's Mobs 召唤物护航/围攻减伤概念；独立实现。"),
            d("saya_thorn_repay", "saya", "棘刺回敬", "Thorn Repay",
                    "等级L：受到近战伤害时使攻击者中毒（40+10L tick），60 tick 冷却。",
                    "Rank L: melee damage taken poisons the attacker for 40+10L ticks, 60-tick cooldown.",
                    "参考原版荆棘反制模型（改为施毒而非反伤）；独立实现。"),
            d("saya_camellia_bloom", "saya", "山茶怒放", "Camellia Bloom",
                    "等级L：击杀获得再生 I（60+20L tick），并使 5 格内敌对生物中毒（40 tick）。",
                    "Rank L: kills grant Regeneration I for 60+20L ticks and poison hostiles within 5 blocks for 40 ticks.",
                    "参考 CoFH 击杀回馈与山茶题材；独立实现（再生+有限范围中毒）。"),
            d("saya_tea_mist", "saya", "茶雾弥散", "Tea Mist",
                    "等级L：100 tick 冷却；命中生命 ≤50% 的目标时施加中毒 II（60+20L tick）。",
                    "Rank L: 100-tick cooldown; hits on targets at <=50% health inflict Poison II for 60+20L ticks.",
                    "参考 Ensorcellation Frost Aspect 的受控追击概念；独立实现（低血条件施毒）。"),
            d("saya_verdant_balance", "saya", "青翠平衡", "Verdant Balance",
                    "等级L：单次受到 ≥12% 最大生命伤害时减伤 (10+4L)% 并回复伤害的 (5+2L)%，180 tick 冷却。",
                    "Rank L: damage of at least 12% max health is reduced by (10+4L)% and heals (5+2L)% of it, 180-tick cooldown.",
                    "参考 Apotheosis 大额伤害减伤与自愈概念；独立实现。"),
            d("saya_seed_of_fortune", "saya", "茶籽之幸", "Seed of Fortune",
                    "等级L：持握时幸运 +0.5L。",
                    "Rank L: +0.5L luck while held.",
                    "参考 Ensorcellation Insight（收益提升）与幸运属性概念；独立实现。")
    );

    private static final List<Definition> WINE = List.of(
            d("vintage_crit", "wine", "陈酿暴击", "Vintage Critical",
                    "等级L：(12+5L)% 概率本次伤害 ×(1.30+0.12L)。",
                    "Rank L: (12+5L)% chance for this hit to deal x(1.30+0.12L) damage.",
                    ORIGINAL_NOTE),
            d("barrel_plate", "wine", "酒桶铁壁", "Barrel Plate",
                    "等级L：受到弹射物伤害 ×(1-0.10L)，减伤下限 55%。",
                    "Rank L: incoming projectile damage x(1-0.10L) with a 55% reduction floor.",
                    ORIGINAL_NOTE),
            d("cork_rush", "wine", "开瓶疾行", "Cork Rush",
                    "等级L：(12+5L)% 概率获得速度与急迫（80+20L tick，等级L-1）。",
                    "Rank L: (12+5L)% chance to gain Speed and Haste for 80+20L ticks at level L-1.",
                    ORIGINAL_NOTE),
            d("wine_barrel_bash", "wine", "酒桶重击", "Barrel Bash",
                    "等级L：距上次命中 ≥60 tick 时本次额外 +2.0+1.0L 伤害；每次命中都会重置蓄力计时。",
                    "Rank L: if 60+ ticks passed since the last hit, this hit deals +2.0+1.0L bonus damage; every hit resets the charge timer.",
                    "参考原版蓄力重击模型；独立实现（3 秒未命中蓄力）。"),
            d("wine_tipsy_weave", "wine", "醉步闪避", "Tipsy Weave",
                    "等级L：自身带有速度或急迫效果时受到伤害减伤 (8+3L)%，80 tick 冷却。",
                    "Rank L: while you have Speed or Haste, incoming damage is reduced by (8+3L)%, 80-tick cooldown.",
                    "参考 Ensorcellation Bulwark/酒醉闪避概念；独立实现。"),
            d("wine_aging_strike", "wine", "陈酿连击", "Aging Strike",
                    "等级L：60 tick 内连续命中叠 1 层陈酿（上限 3 层），每层本次命中 +0.3L 伤害。",
                    "Rank L: consecutive hits within 60 ticks add one Vintage stack (cap 3); each stack adds +0.3L damage to this hit.",
                    "参考 Apotheosis 连续命中叠层概念；独立实现（上限 3 层）。"),
            d("wine_cork_echo", "wine", "软木回响", "Cork Echo",
                    "等级L：命中未着火目标时点燃 2+L 秒；90 tick 冷却。",
                    "Rank L: hits on non-burning targets ignite them for 2+L seconds; 90-tick cooldown.",
                    "参考 Ensorcellation 附魔触发模型（改为灼烧）；独立实现（目标状态条件）。"),
            d("wine_vintage_revive", "wine", "年份复苏", "Vintage Revival",
                    "等级L：击杀后获得力量 I（80+30L tick）与吸收（60 tick，等级L-1），200 tick 冷却。",
                    "Rank L: kills grant Strength I for 80+30L ticks and Absorption for 60 ticks at level L-1, 200-tick cooldown.",
                    "参考 CoFH 击杀充能与力量药剂回馈概念；独立实现。"),
            d("wine_spill_guard", "wine", "泼洒防护", "Spill Guard",
                    "等级L：受到火焰或爆炸伤害时减伤 (15+5L)% 并获得抗火（40+20L tick），160 tick 冷却。",
                    "Rank L: fire or explosion damage is reduced by (15+5L)% and grants Fire Resistance for 40+20L ticks, 160-tick cooldown.",
                    "参考 Ensorcellation 火焰防护方向；独立实现。"),
            d("wine_barrel_reinforce", "wine", "酒桶加固", "Barrel Reinforcement",
                    "等级L：持握时护甲韧性 +0.75L。",
                    "Rank L: +0.75L armor toughness while held.",
                    "参考 Ensorcellation 护甲强化方向；独立实现。")
    );

    private static final List<Definition> RAT = List.of(
            d("cheese_frenzy", "rat", "奶酪狂热", "Cheese Frenzy",
                    "等级L：60 tick 内连续命中叠加层数（上限 L+2），每层伤害 +(2.5+1.0L)%。",
                    "Rank L: consecutive hits within 60 ticks stack (cap L+2); each stack adds (2.5+1.0L)% damage.",
                    ORIGINAL_NOTE),
            d("plague_tooth", "rat", "鼠疫尖牙", "Plague Tooth",
                    "等级L：(12+5L)% 概率施加凋零（30+15L tick，等级 min(1,L-1)）。",
                    "Rank L: (12+5L)% chance to inflict Wither for 30+15L ticks at amplifier min(1,L-1).",
                    ORIGINAL_NOTE),
            d("scavenger_luck", "rat", "拾荒跳电", "Scavenger Arc",
                    "等级L：(12+5L)% 概率向最近的其他敌人弹射电弧，造成 2.0+1.5L 伤害。",
                    "Rank L: (12+5L)% chance to arc to the nearest other enemy for 2.0+1.5L damage.",
                    ORIGINAL_NOTE),
            d("rat_plague_bite", "rat", "疫牙撕咬", "Plague Fang",
                    "等级L：命中生命 ≤40% 的目标时施加中毒 II（60+20L tick）并额外 +0.8+0.4L 伤害；90 tick 冷却。",
                    "Rank L: hits on targets at <=40% health inflict Poison II for 60+20L ticks and add +0.8+0.4L damage; 90-tick cooldown.",
                    "参考 Ensorcellation Vorpal 的附加减益模型与鼠疫题材；独立实现（斩杀线条件）。"),
            d("rat_swarm_mark", "rat", "鼠群围猎", "Swarm Hunt",
                    "等级L：命中标记目标 100 tick（等级L）；对已标记目标命中额外 +1.5+0.75L 伤害并消耗标记。",
                    "Rank L: hits mark the target for 100 ticks at level L; hits on a marked target deal +1.5+0.75L bonus damage and consume the mark.",
                    "参考 Apotheosis 标记/引爆类附魔概念；独立实现。"),
            d("rat_cheese_armor", "rat", "奶酪护壳", "Cheese Shell",
                    "等级L：饱食度 ≥15 时受到伤害获得吸收（80 tick，等级L-1），90 tick 冷却。",
                    "Rank L: with food level >=15, damage taken grants Absorption for 80 ticks at level L-1, 90-tick cooldown.",
                    "参考 Ensorcellation 吸收护盾概念；独立实现（饱食度条件）。"),
            d("rat_scavenge_hide", "rat", "拾荒潜行", "Scavenger Hide",
                    "等级L：生命 ≤40% 时受到伤害减伤 (9+3L)% 并获得速度 I（60 tick），140 tick 冷却。",
                    "Rank L: at <=40% health, incoming damage is reduced by (9+3L)% and grants Speed I for 60 ticks, 140-tick cooldown.",
                    "参考 Ensorcellation 低血量防御概念；独立实现。"),
            d("rat_gnaw_heal", "rat", "啮咬饱食", "Gnawing Meal",
                    "等级L：击杀补充 (1+L) 点饱食与饱和（20 tick），并回复 0.25L 生命。",
                    "Rank L: kills restore (1+L) hunger and Saturation for 20 ticks, and heal 0.25L.",
                    "参考 Ensorcellation Gourmand（食物恢复）概念；独立实现。"),
            d("rat_plague_cloud", "rat", "疫云扩散", "Plague Cloud",
                    "等级L：击杀时使 3 格内敌对生物中毒（40+10L tick）。",
                    "Rank L: kills poison hostile mobs within 3 blocks for 40+10L ticks.",
                    "参考 CoFH 范围诅咒/瘟疫云概念；独立实现（3 格内敌对生物）。"),
            d("rat_whisker_luck", "rat", "鼠须探触", "Whisker Reach",
                    "等级L：主手持握时实体交互及近战触及距离 +0.15L 格；不增加方块触及或弹射物射程。",
                    "Rank L: +0.15L blocks of entity interaction and melee reach in the main hand; no block reach or projectile range bonus.",
                    "参考 YuoEnchants 距离提升概念；独立实现，保留原ID。")
    );

    private static final List<Definition> DEFECT = List.of(
            d("overload_damage", "defect", "超载链爆", "Overload Chain",
                    "等级L：(12+5L)% 概率对 3 格内敌人造成 1.5+1.25L 范围爆发。",
                    "Rank L: (12+5L)% chance to release a 1.5+1.25L area burst against enemies within 3 blocks.",
                    ORIGINAL_NOTE),
            d("volt_step", "defect", "电涌步", "Volt Step",
                    "等级L：持握时移动速度 +1.2%L。",
                    "Rank L: +1.2%L movement speed while held.",
                    ORIGINAL_NOTE),
            d("magnetic_plate", "defect", "磁锁装甲", "Magnetic Plate",
                    "等级L：持握时击退抗性 +0.08L。",
                    "Rank L: +0.08L knockback resistance while held.",
                    ORIGINAL_NOTE),
            d("defect_static_charge", "defect", "静电蓄能", "Static Charge",
                    "等级L：命中叠电荷（上限 L+1 层，300 tick 未命中清零）；满层命中额外 +1.5+0.5L 伤害并清零。",
                    "Rank L: hits build Charge (cap L+1, resets after 300 ticks without a hit); a hit at full charge deals +1.5+0.5L bonus damage and consumes it.",
                    "参考 Apotheosis 充能叠层与 CoFH 电弧类附魔概念；独立实现（层数上限）。"),
            d("defect_arc_brand", "defect", "电弧烙印", "Arc Brand",
                    "等级L：命中未发光目标时使其发光（100+20L tick）；命中已发光目标额外 +1.2+0.6L 伤害；70 tick 冷却。",
                    "Rank L: hits brand non-Glowing targets with Glowing for 100+20L ticks; hits on Glowing targets add +1.2+0.6L damage; 70-tick cooldown.",
                    "参考 Ensorcellation 电弧/发光标记概念；独立实现（发光标记条件）。"),
            d("defect_deflect_field", "defect", "偏转磁场", "Deflection Field",
                    "等级L：每第 3 次受到的弹射物伤害被完全抵消，并获得吸收（40+10L tick）；触发后 200 tick 冷却。",
                    "Rank L: every third incoming projectile hit is fully negated and grants Absorption for 40+10L ticks; 200-tick cooldown after a trigger.",
                    "参考 Ensorcellation Bulwark（弹射物防护）概念；独立实现（计数完全抵消）。"),
            d("defect_grounding", "defect", "接地回路", "Grounding Circuit",
                    "等级L：受到爆炸或雷击伤害时减伤 (15+5L)% 并获得抗性 I（40 tick），160 tick 冷却。",
                    "Rank L: explosion or lightning damage is reduced by (15+5L)% and grants Resistance I for 40 ticks, 160-tick cooldown.",
                    "参考 Ensorcellation Magic Protection 概念；独立实现（爆炸/雷击条件）。"),
            d("defect_surge_kill", "defect", "过载击杀", "Overload Kill",
                    "等级L：击杀获得力量 I（60+20L tick）与速度 I（30 tick）。",
                    "Rank L: kills grant Strength I for 60+20L ticks and Speed I for 30 ticks.",
                    "参考 CoFH 击杀充能（Leech 变体）概念；独立实现。"),
            d("defect_feedback", "defect", "反馈回路", "Feedback Loop",
                    "等级L：受到近战伤害时使攻击者缓慢（40+10L tick），60 tick 冷却。",
                    "Rank L: melee damage taken slows the attacker for 40+10L ticks, 60-tick cooldown.",
                    "参考 Ensorcellation Chilling Rebuke（受击减速攻击者）概念；独立实现。"),
            d("defect_overclock", "defect", "超频核心", "Overclock Core",
                    "等级L：主手近战武器攻击速度 +0.03L；弓弩、法杖及三叉戟命中伤害 +3%L。",
                    "Rank L: +0.03L mainhand melee attack speed; +3%L hit damage for bows, crossbows, staffs and tridents.",
                    "参考其他模组的超频与条件强化能力；独立实现，按武器类型兑现收益。")
    );

    private static final List<Definition> COMPOST = List.of(
            d("prismatic_venom", "compost", "虹彩剧毒", "Prismatic Venom",
                    "等级L：(12+5L)% 概率施加虚弱（60+20L tick）与反胃（80+20L tick）。",
                    "Rank L: (12+5L)% chance to inflict Weakness (60+20L ticks) and Nausea (80+20L ticks).",
                    ORIGINAL_NOTE),
            d("recycle_heart", "compost", "回收心脏", "Recycled Heart",
                    "等级L：持握时最大生命 +1.5L。",
                    "Rank L: +1.5L maximum health while held.",
                    ORIGINAL_NOTE),
            d("fertile_haste", "compost", "沃土疾行", "Fertile Haste",
                    "等级L：每 max(20, 70-15L) tick 修复武器 1 点耐久。",
                    "Rank L: repairs 1 durability every max(20, 70-15L) ticks.",
                    ORIGINAL_NOTE),
            d("compost_spore_burst", "compost", "孢子喷发", "Spore Burst",
                    "等级L：自身生命 ≤70% 时命中回复 0.5+0.25L 并使目标中毒 I（40+10L tick）；100 tick 冷却。",
                    "Rank L: hits while at <=70% health heal 0.5+0.25L and poison the target for 40+10L ticks; 100-tick cooldown.",
                    "参考 CoFH 瘟疫/孢子范围减益概念；独立实现（自身血量条件）。"),
            d("compost_recycle_guard", "compost", "循环护育", "Recycle Ward",
                    "等级L：生命 ≤50% 时受到伤害减伤 (6+2L)% 并获得再生 I（40+10L tick），140 tick 冷却。",
                    "Rank L: at <=50% health, incoming damage is reduced by (6+2L)% and grants Regeneration I for 40+10L ticks, 140-tick cooldown.",
                    "参考 Ensorcellation 低血量防御与再生概念；独立实现。"),
            d("compost_mulch_armor", "compost", "腐殖甲被", "Mulch Mantle",
                    "等级L：受到伤害获得吸收（80 tick，等级L-1），100 tick 冷却。",
                    "Rank L: damage taken grants Absorption for 80 ticks at level L-1, 100-tick cooldown.",
                    "参考 Ensorcellation 吸收护盾概念；独立实现。"),
            d("compost_vital_rot", "compost", "腐化回春", "Vital Rot",
                    "等级L：击杀回复 (1.0+0.5L)×(1+已损生命比例) 生命。",
                    "Rank L: kills heal (1.0+0.5L) x (1 + missing health ratio).",
                    "参考 CoFH Leech 击杀回复概念；独立实现（按已损生命放大）。"),
            d("compost_fertile_wrath", "compost", "沃土之怒", "Fertile Wrath",
                    "等级L：目标中毒或缓慢时本次命中额外 +1.5+0.75L 伤害。",
                    "Rank L: if the target is poisoned or slowed, this hit deals +1.5+0.75L bonus damage.",
                    "参考 Quark 冰霜/受控目标增伤概念；独立实现。"),
            d("compost_growth_spurt", "compost", "生长突进", "Growth Spurt",
                    "等级L：受到伤害后获得速度 I（60+20L tick），120 tick 冷却。",
                    "Rank L: damage taken grants Speed I for 60+20L ticks, 120-tick cooldown.",
                    "参考 Ensorcellation 移动向附魔概念；独立实现。"),
            d("compost_harvest_mark", "compost", "收割印记", "Harvest Mark",
                    "等级L：命中标记目标 120 tick；击杀被标记目标回复 1.0+0.5L 生命并补充 1 点饱食。",
                    "Rank L: hits mark the target for 120 ticks; killing a marked target heals 1.0+0.5L and restores 1 hunger.",
                    "参考 Apotheosis 标记/收割类附魔概念；独立实现（命中标记、击杀结算）。")
    );

    private static final List<Definition> APPLE = List.of(
            d("honeycrisp_heart", "apple", "蜜脆心核", "Honeycrisp Heart",
                    "等级L：生命 ≤50% 且冷却就绪时命中回复 0.75L 并获得吸收（60 tick）。",
                    "Rank L: hits at <=50% health with the cooldown ready heal 0.75L and grant Absorption for 60 ticks.",
                    ORIGINAL_NOTE),
            d("golden_fortune", "apple", "金穗标记", "Golden Mark",
                    "等级L：(12+5L)% 概率标记目标（100+40L tick）；对已标记目标命中伤害 ×(1.12+0.06L) 并消耗标记。",
                    "Rank L: (12+5L)% chance to mark the target for 100+40L ticks; hits on a marked target deal x(1.12+0.06L) damage and consume the mark.",
                    ORIGINAL_NOTE),
            d("orchard_edge", "apple", "果园灼痕", "Orchard Scorch",
                    "等级L：(12+5L)% 概率点燃目标 2+2L 秒。",
                    "Rank L: (12+5L)% chance to ignite the target for 2+2L seconds.",
                    ORIGINAL_NOTE),
            d("apple_cider_haste", "apple", "苹果酒疾行", "Cider Rush",
                    "等级L：命中后获得急迫 I（60+20L tick），90 tick 冷却。",
                    "Rank L: hits grant Haste I for 60+20L ticks, 90-tick cooldown.",
                    "参考 Ensorcellation 急迫/工具强化概念；独立实现。"),
            d("apple_blossom_ward", "apple", "花萼守护", "Blossom Ward",
                    "等级L：受到伤害时减伤 (5+2L)% 并回复 0.5+0.25L 生命，100 tick 冷却。",
                    "Rank L: damage taken is reduced by (5+2L)% and heals 0.5+0.25L, 100-tick cooldown.",
                    "参考 Ensorcellation 低血量防御+再生概念；独立实现。"),
            d("apple_fruit_skin", "apple", "果皮韧性", "Fruit Skin",
                    "等级L：生命 ≥95% 时受到伤害减伤 (15+5L)%。",
                    "Rank L: at >=95% health, incoming damage is reduced by (15+5L)%.",
                    "参考 Ensorcellation 满血减伤（Bulwark 变体）概念；独立实现。"),
            d("apple_core_blast", "apple", "果核爆裂", "Core Blast",
                    "等级L：命中燃烧中的目标额外 +1.5+0.75L 伤害。",
                    "Rank L: hits on burning targets deal +1.5+0.75L bonus damage.",
                    "参考原版火焰增伤与元素连携概念；独立实现。"),
            d("apple_harvest_feast", "apple", "丰收盛宴", "Harvest Feast",
                    "等级L：击杀获得饱和 I（60+20L tick）与吸收（80 tick，等级L-1）。",
                    "Rank L: kills grant Saturation I for 60+20L ticks and Absorption for 80 ticks at level L-1.",
                    "参考 Ensorcellation Gourmand 概念；独立实现（击杀饱和/吸收）。"),
            d("apple_orchard_step", "apple", "果园轻步", "Orchard Step",
                    "等级L：命中后获得速度 I（40+10L tick），80 tick 冷却。",
                    "Rank L: hits grant Speed I for 40+10L ticks, 80-tick cooldown.",
                    "参考 Ensorcellation 移动向附魔概念；独立实现。"),
            d("apple_golden_shell", "apple", "金苹果残响", "Golden Afterglow",
                    "等级L：受到致命伤害时保留 1 点生命并获得吸收（100 tick，等级1）与再生 I（60 tick），1200 tick 冷却。",
                    "Rank L: lethal damage leaves you at 1 health and grants Absorption (100 ticks, amplifier 0) and Regeneration I (60 ticks), 1200-tick cooldown.",
                    "参考原版不死图腾/Ensorcellation 免死保命概念；独立实现（长冷却）。")
    );

    private static final List<Definition> TIDE = List.of(
            d("undertow_step", "tide", "暗流步", "Undertow Step",
                    "等级L：目标潮湿时伤害 ×(1.10+0.05L) 并获得海豚恩惠（60+20L tick）。",
                    "Rank L: against wet targets, damage x(1.10+0.05L) and gain Dolphin's Grace for 60+20L ticks.",
                    ORIGINAL_NOTE),
            d("brine_slow", "tide", "盐潮迟滞", "Brine Drag",
                    "等级L：(12+5L)% 概率施加挖掘疲劳（60+25L tick，等级 min(2,L-1)）。",
                    "Rank L: (12+5L)% chance to inflict Mining Fatigue for 60+25L ticks at amplifier min(2,L-1).",
                    ORIGINAL_NOTE),
            d("tidal_bulwark", "tide", "潮汐壁垒", "Tidal Bulwark",
                    "等级L：身处水中/雨中受到伤害 ×(1-0.08L)，减伤下限 55%。",
                    "Rank L: while wet or in rain, incoming damage x(1-0.08L) with a 55% reduction floor.",
                    ORIGINAL_NOTE),
            d("tide_undertow_pull", "tide", "底流拖拽", "Undertow Drag",
                    "等级L：80 tick 冷却；命中潮湿目标时将其拉近（强度 0.30）并使其缓慢 40 tick。",
                    "Rank L: 80-tick cooldown; hits on wet targets pull them closer (strength 0.30) and apply Slowness for 40 ticks.",
                    "参考 Alex's Mobs 潮涌/拖拽类能力概念；独立实现（潮湿目标）。"),
            d("tide_brine_bite", "tide", "盐卤撕咬", "Brine Bite",
                    "等级L：身处水中/雨中时命中使目标缓慢 II 与挖掘疲劳 I（60+15L tick）；60 tick 冷却。",
                    "Rank L: while wet or in rain, hits inflict Slowness II and Mining Fatigue I for 60+15L ticks; 60-tick cooldown.",
                    "参考 Ensorcellation Frost Aspect（减速+疲劳）概念；独立实现（自身潮湿条件）。"),
            d("tide_current_guard", "tide", "潮流护体", "Current Guard",
                    "等级L：身处水中/雨中时受到伤害减伤 (10+4L)%，80 tick 冷却。",
                    "Rank L: while wet or in rain, incoming damage is reduced by (10+4L)%, 80-tick cooldown.",
                    "参考 Ensorcellation 潮湿减伤（Tidal Bulwark 方向）概念；独立实现。"),
            d("tide_foam_veil", "tide", "泡沫薄纱", "Foam Veil",
                    "等级L：受到伤害后获得海豚恩惠（60+20L tick）与水下呼吸（30+10L tick），120 tick 冷却。",
                    "Rank L: damage taken grants Dolphin's Grace for 60+20L ticks and Water Breathing for 30+10L ticks, 120-tick cooldown.",
                    "参考原版海豚恩惠/水下呼吸概念；独立实现。"),
            d("tide_tidal_reward", "tide", "潮汐回馈", "Tidal Reward",
                    "等级L：击杀回复 (0.75+0.5L) 生命；身处水中/雨中时治疗翻倍。",
                    "Rank L: kills heal (0.75+0.5L); healing doubles while wet or in rain.",
                    "参考 CoFH Leech 击杀回复概念；独立实现（水中翻倍）。"),
            d("tide_crushing_depth", "tide", "深渊压强", "Crushing Depth",
                    "等级L：目标生命比例高于自身时本次命中额外 +1.2+0.6L 伤害。",
                    "Rank L: if the target's health ratio is higher than yours, this hit deals +1.2+0.6L bonus damage.",
                    "参考 Apotheosis 压制/处决条件增伤概念；独立实现（生命比例比较）。"),
            d("tide_abyssal_anchor", "tide", "海渊之锚", "Abyssal Anchor",
                    "等级L：持握时击退抗性 +0.06L。",
                    "Rank L: +0.06L knockback resistance while held.",
                    "参考 Ensorcellation Bulwark（击退抗性）概念；独立实现。")
    );

    private static final List<Definition> BENGER = List.of(
            d("shepherd_might", "benger", "牧王之力", "Shepherd Might",
                    "等级L：目标 4 格内无其他可攻击目标时伤害 ×(1.10+0.05L)。",
                    "Rank L: when no other attackable mob is within 4 blocks of the target, damage x(1.10+0.05L).",
                    ORIGINAL_NOTE),
            d("hunting_step", "benger", "猎群印记", "Hunting Mark",
                    "等级L：首次命中标记猎物（160+40L tick）并加速；标记期间伤害 ×(1.05+0.035L)。",
                    "Rank L: the first hit marks the prey for 160+40L ticks and grants Speed; marked targets take x(1.05+0.035L) damage.",
                    ORIGINAL_NOTE),
            d("pack_feast", "benger", "群猎盛宴", "Pack Feast",
                    "等级L：击杀回复 0.75+0.5L 生命并补充饱食（1+L）。",
                    "Rank L: kills heal 0.75+0.5L and restore (1+L) hunger.",
                    ORIGINAL_NOTE),
            d("benger_pack_call", "benger", "唤群嗥叫", "Pack Call",
                    "等级L：击杀获得速度 I（60+20L tick），8 格内已驯服宠物获得力量 I（60 tick）。",
                    "Rank L: kills grant Speed I for 60+20L ticks; tamed pets within 8 blocks gain Strength I for 60 ticks.",
                    "参考 Alex's Mobs 群狼/群体增益概念；独立实现。"),
            d("benger_hamstring", "benger", "断筋撕咬", "Hamstring Bite",
                    "等级L：60 tick 冷却；命中使目标缓慢（30+10L tick）并额外 +0.5L 伤害。",
                    "Rank L: 60-tick cooldown; hits slow the target for 30+10L ticks and add +0.5L damage.",
                    "参考 Ensorcellation Frost Aspect（断筋减速）概念；独立实现。"),
            d("benger_scent_trail", "benger", "循迹追猎", "Scent Trail",
                    "等级L：命中发光目标时额外 +1.5+0.75L 伤害。",
                    "Rank L: hits on Glowing targets deal +1.5+0.75L bonus damage.",
                    "参考原版发光标记与追击概念；独立实现。"),
            d("benger_guard_dog", "benger", "护卫之犬", "Guard Dog",
                    "等级L：8 格内存在已驯服宠物时受到伤害减伤 (10+3L)%，并使宠物获得抗性 I（40 tick），100 tick 冷却。",
                    "Rank L: with a tamed pet within 8 blocks, incoming damage is reduced by (10+3L)% and pets gain Resistance I for 40 ticks, 100-tick cooldown.",
                    "参考 Alex's Mobs 护卫/宠物协防概念；独立实现。"),
            d("benger_blood_hound", "benger", "血猎亢奋", "Blood Hound",
                    "等级L：击杀获得力量 I（60+20L tick）。",
                    "Rank L: kills grant Strength I for 60+20L ticks.",
                    "参考 CoFH 击杀充能概念；独立实现。"),
            d("benger_territorial_growl", "benger", "领地低吼", "Territorial Growl",
                    "等级L：受到伤害时使 3 格内敌对生物虚弱（40+10L tick），80 tick 冷却。",
                    "Rank L: damage taken inflicts Weakness on hostiles within 3 blocks for 40+10L ticks, 80-tick cooldown.",
                    "参考 Ensorcellation Chilling Rebuke（受击范围减益）概念；独立实现。"),
            d("benger_loyal_heart", "benger", "忠犬之心", "Loyal Heart",
                    "等级L：生命 ≤35% 时受到伤害减伤 (9+3L)%，120 tick 冷却。",
                    "Rank L: at <=35% health, incoming damage is reduced by (9+3L)%, 120-tick cooldown.",
                    "参考 Ensorcellation 低血量防御概念；独立实现。")
    );

    private static final List<List<Definition>> POOLS = List.of(SUGAR, BUTCHER, SAYA, WINE, RAT, DEFECT, COMPOST, APPLE, TIDE, BENGER);
    private static final List<String> PROFILE_ORDER = List.of("sugar", "butcher", "saya", "wine", "rat",
            "defect", "compost", "apple", "tide", "benger");
    private static final Map<String, Definition> BY_ID;
    private static final Set<String> ADDED = new HashSet<>();

    static {
        Map<String, Definition> byId = new LinkedHashMap<>();
        for (int poolIndex = 0; poolIndex < POOLS.size(); poolIndex++) {
            List<Definition> pool = POOLS.get(poolIndex);
            if (pool.size() != 10) throw new IllegalStateException("statue blessing pool must contain exactly 10 entries");
            String profile = PROFILE_ORDER.get(poolIndex);
            for (int index = 0; index < pool.size(); index++) {
                Definition definition = pool.get(index);
                if (!profile.equals(definition.profile())) throw new IllegalStateException("statue blessing profile mismatch: " + definition.id());
                if (byId.put(definition.id(), definition) != null) throw new IllegalStateException("duplicate statue blessing id: " + definition.id());
                if (index >= 3) ADDED.add(definition.id());
            }
        }
        if (byId.size() != POOLS.size() * 10) throw new IllegalStateException("statue blessing catalog must contain 100 unique ids");
        BY_ID = Map.copyOf(byId);
    }

    private StatueBlessingCatalog() {}

    /** 返回某座石像的 10 项专属附魔（前 3 项为原 30 项中连续对应的那 3 项）；未知 profile 返回空表。 */
    public static List<Definition> pool(String profile) {
        if (profile == null) return List.of();
        return switch (profile) {
            case "sugar" -> SUGAR;
            case "butcher" -> BUTCHER;
            case "saya" -> SAYA;
            case "wine" -> WINE;
            case "rat" -> RAT;
            case "defect" -> DEFECT;
            case "compost" -> COMPOST;
            case "apple" -> APPLE;
            case "tide" -> TIDE;
            case "benger" -> BENGER;
            default -> List.of();
        };
    }

    /** 全局按 ID 查定义；未知 ID 返回 null。 */
    public static Definition byId(String id) {
        return id == null ? null : BY_ID.get(id);
    }

    /** 100 项定义，按石像顺序再按池内顺序展开。 */
    public static List<Definition> all() {
        List<Definition> result = new ArrayList<>(BY_ID.size());
        for (List<Definition> pool : POOLS) result.addAll(pool);
        return List.copyOf(result);
    }

    /** 本次新增的 70 项专用判定；战斗热路径只处理这些 ID，原 30 项继续走既有实现。 */
    static boolean isAdded(String id) {
        return id != null && ADDED.contains(id);
    }

    private static Definition d(String id, String profile, String nameZh, String nameEn,
                                String descriptionZh, String descriptionEn, String inspiration) {
        return new Definition(id, profile, nameZh, nameEn, descriptionZh, descriptionEn, inspiration);
    }
}
