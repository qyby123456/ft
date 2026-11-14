# 炫彩答辩超人

常规 Boss 第 7 只，实体 id `ftboss:compost_boss`，堆肥/肥料主题 Boss，手持 Create 土豆加农炮（`entity/FTEntities.java:163-165`）。下文数值全部取自当前源码，时间单位 tick（1 秒 = 20 tick），距离单位格。

## 一、实体与判定箱

| 项目 | 数值 | 出处 |
|---|---|---|
| 实体 id | `ftboss:compost_boss` | `entity/FTEntities.java:164` |
| 中文名 | 炫彩答辩超人 | `assets/ftboss/lang/zh_cn.json:201` |
| 生物分类 | `MobCategory.MONSTER` | `entity/FTEntities.java:181` |
| 判定箱（宽 × 高） | 1.6 格 × 3.4 格 | `entity/FTEntities.java:165` |
| 客户端追踪范围 | 10（`clientTrackingRange`，单位为区块） | `entity/FTEntities.java:190` |
| 召唤物品 | `ftboss:compost_summon`（炫彩堆肥桶） | `assets/ftboss/lang/zh_cn.json:202` |
| 召唤配方 | 4 × `create:precision_mechanism` + 4 × `minecraft:bone_meal` + 1 × `minecraft:composter` | `data/ftboss/recipes/compost_summon.json:3-18` |
| 召唤维度限制 | 仅主世界（`OVERWORLD_ONLY` = true），不满足时不消耗召唤物 | `boss/SummonGate.java:25`、`item/CompostSummonItem.java:29` |
| 召唤落点 | 玩家面向方向 4 格处，朝向 = 玩家朝向 + 180° | `item/CompostSummonItem.java:32`、`item/CompostSummonItem.java:38` |
| 召唤音效 | `minecraft:entity.wither.spawn`，音量 1.6，音调 0.6 | `item/CompostSummonItem.java:40` |
| 消失规则 | `setPersistenceRequired()`，且 `removeWhenFarAway` 返回 false（不因距离消失） | `entity/CompostBossEntity.java:136`、`entity/CompostBossEntity.java:764` |

## 二、基础属性与机制

| 项目 | 数值 | 出处 |
|---|---|---|
| `MAX_HEALTH` | 340.0 | `entity/CompostBossEntity.java:160` |
| `ARMOR` | 2.0 | `entity/CompostBossEntity.java:161` |
| `ATTACK_DAMAGE` | 4.0 | `entity/CompostBossEntity.java:162` |
| `MOVEMENT_SPEED` | 0.1（`BossWalkingSpeed.BASE_SPEED`）；普通陆地寻路结果再 ×2 | `entity/CompostBossEntity.java:163`、`boss/ai/BossWalkingSpeed.java:24`、`:74` |
| `FOLLOW_RANGE` | 32.0 | `entity/CompostBossEntity.java:164` |
| `KNOCKBACK_RESISTANCE` | 0.4 | `entity/CompostBossEntity.java:165` |
| 经验奖励 | `xpReward` = 120 | `entity/CompostBossEntity.java:144` |
| AI 行为 | `FloatGoal`（优先级 0）、`MeleeAttackGoal`（速度倍率 1.0，优先级 2）、`LookAtPlayerGoal`（16 格，优先级 8） | `entity/CompostBossEntity.java:155-157` |
| 目标选择 | `NearestAttackableTargetGoal`（`Player`，随机目标 true，优先级 1） | `entity/CompostBossEntity.java:158` |
| 主手装备 | 污秽虹冠 `ftboss:compost_crown`，命中给目标中毒 I 80t（4 秒）；`AttributeModifiers` 被清空，故不加攻击力；主手掉落概率 0 | `entity/CompostBossEntity.java:218-228`、`item/weapons/CompostCrownItem.java:19`、`item/weapons/CompostCrownItem.java:29` |
| 负面效果（不再免疫） | 事件层全局 `DENY` 已停用（`IMMUNE_TO_DEBUFFS` = false），本实体没有周期性清减益逻辑，会正常吃减益；每 40 tick 把身上每个负面效果转换为力量/速度/抗性强化（最多 4 级） | `boss/BossHandler.java:409`、`entity/CompostBossEntity.java:431-450` |
| 单次伤害限伤 | 结算后的实际扣血伤害 > `MAX_FINAL_DAMAGE` = 300.0 时本次血量伤害归零（不是截断；恰好 300 或更低放行；不按原始伤害判断；`/kill` 保留）；原始 500 点规则与旧 `LIMIT_ENABLED` 限伤继续停用；本 Boss 适用，玩家、外部 Boss、鼠群小怪、迷你 Boss、召唤物不适用 | `boss/ai/FinalBossDamageGuard.java:19`、`mixin/FinalBossDamageMixin.java:14` |
| 燃烧动画 | 不显示火焰燃烧动画，伤害照常结算 | `entity/CompostBossEntity.java:759` |
| 攻速叠层 | 每次投掷物命中玩家 +1 层（上限 12），60t（3 秒）未命中清零，层数降低连射冷却与连射间隔 | `entity/CompostBossEntity.java:650`、`entity/CompostBossEntity.java:574` |
| 阶段阈值 | `P2_AT` = 0.70、`P3_AT` = 0.35（按当前生命 ÷ 最大生命判定） | `entity/CompostBossEntity.java:62` |
| 飞行 | 阶段 ≥ 2 时 `setNoGravity(true)`，悬浮高度 = 地面高 + 5.0 格（±0.3 格正弦波动） | `entity/CompostBossEntity.java:270-271`、`entity/CompostBossEntity.java:285` |

## 三、弹幕、抛射与其它技能

| 技能 | 触发与冷却 | 前摇 | 范围 | 伤害 | 附带 |
|---|---|---|---|---|---|
| 答辩连射（4~5 发普通弹） | 首次 120t；之后 `max(16, 55 − 攻速层数 × 4) + random(12)` t（层数 0 时 55~66t，层数 12 时 16~27t） | 16t 抬炮（`cannon_fire` 动作，动画时长 `T_FIRE` = 32t） | 射向目标并做速度预判，初速 1.35、散布 0.6 | 设计 6.0 + 阶段 × 2.0，弹体内部 ×0.5 → 实际 4.0 / 5.0 / 6.0（阶段 1 / 2 / 3） | 单发间隔 `max(1, 3 − 层数 ÷ 4)` t；命中附缓慢 I 100t、反胃 I 140t、饥饿 I 160t |
| 抛射答辩（3 发抛物弧） | 首次 160t；之后 140 + random(80) t | 17t 抬手，随后每 4t 一发，共 3 发 | 出膛点 y + 2.1，上抛分量 0.55 加垂直修正，初速 1.05、散布 1.2 | 设计 7.0 + 阶段 × 2.5，弹体内部 ×0.5 → 实际 4.75 / 6.0 / 7.25 | 命中附缓慢 I 100t、反胃 I 140t、饥饿 I 160t；每发都有炮口粒子 |
| 毒气蓄力爆炸 | 首次 200t；之后 200 + random(100) t | 蓄力动画 `gas_charge` 52t，引信 41t 后爆炸 | 8 格（`distanceToSqr <= 64.0`） | 半径 1.6 的爆炸（不破坏地形）+ 直接伤害设计 8.0 + 阶段 × 1.5，直接伤害内部 ×0.5 → 实际 4.75 / 5.5 / 6.25 | 点燃 4 秒；蓄力期间每 4t 一圈 24 点堆肥桶粒子 |
| 空中弹幕（阶段 ≥ 2） | 首次 90t；之后 170 + random(60) t | 17t 抬炮 | 5 发扇形，第 i 发水平偏移 i × 1.9 格（i 取 −2 到 2），初速 1.3、散布 0.8、垂直分量 ≤ −0.15（向下压角） | 设计 9.0 + 阶段 × 2.5，弹体内部 ×0.5 → 实际 7.0（阶段 2）/ 8.25（阶段 3） | 每发炮口粒子；播放 `SNOWBALL_THROW` |
| 俯冲撞击（阶段 ≥ 2） | 首次 200t；之后 180 + random(60) t | 14t 悬停蓄势（`phase_shift` 动作） | 命中判定 1.8 格（`distanceToSqr < 3.24`），持续 40t，速度 0.55 格/t | 设计 9.0 + 阶段 × 2.0，内部 ×0.5 → 实际 6.5（阶段 2）/ 7.5（阶段 3） | 击退 1.2、点燃 3 秒；撞到即结束俯冲，播爆炸音效与 24 点史莱姆粒子 |
| 空中投弹（阶段 ≥ 2，环绕移动中） | 每 30t（首次 20t） | 17t 抬手 | 落点 = 玩家位置 ±1.0 格随机散布，上抛 1.05、水平 0.95、初速 1.15、散布 0.45 | 设计 10.0 + 阶段 × 3.0，弹体内部 ×0.5 → 实际 8.0（阶段 2）/ 9.5（阶段 3） | 播放 `SNOWBALL_THROW`（音量 0.8、音调 0.7） |
| 环绕冲刺（阶段 ≥ 2 机动） | 每 26 + random(14) t | 无（瞬时） | 环绕半径 6.5 ± 2.0 格（4.5~8.5 格），每 40t 换向 | 无（纯位移，不结算伤害） | 冲刺持续 14t、0.95 格/t；非冲刺时基础移动 0.55 格/t |

- 投掷物实体：`ftboss:rainbow_poop`（判定箱 0.5 × 0.5，`MobCategory.MISC`，`entity/FTEntities.java:77-78`）；阶段 1 用棕色外观、阶段 ≥ 2 用彩色外观（`entity/CompostBossEntity.java:716`、`entity/RainbowPoopEntity.java:53`）；命中或落地即 `discard()`，不再生成肥水坑（`entity/RainbowPoopEntity.java:93`）。
- 伤害倍率：调用方传设计值，`setDamage` 内部统一 ×0.5（`entity/RainbowPoopEntity.java:38`），故上表「实际」列 = 设计值 × 0.5；表内伤害均为代码字面常量。
- 命中附带：投掷物命中玩家固定附缓慢 I 100t、反胃 I 140t、饥饿 I 160t（`entity/RainbowPoopEntity.java:68-70`）。
- 施法前置：所有技能都要通过 `canStartSkill()`（无进行中的动作、无俯冲、无连射/抛射余量、目标存活且有效，`entity/CompostBossEntity.java:630-634`）。
- 停用技能：悬停激光射线（条件恒假 `if (false && …)`，`entity/CompostBossEntity.java:357`），数值为激光每 2t 一次、伤害 4.5 + 阶段 × 1.0（`entity/CompostBossEntity.java:386`），当前不生效。
- 打断：升阶或目标失效时调用 `cancelPendingSkills()`，清空连射/抛射余量与各技能前摇（`entity/CompostBossEntity.java:636-645`）。

## 四、召唤物

| 召唤物 | 实体 id 与判定箱 | 触发与冷却 | 单次数量 | 属性 | 附带与寿命 |
|---|---|---|---|---|---|
| 答辩小怪 | `ftboss:poop_minion`，0.8 × 0.9 | 阶段 ≥ 2；首次 200t，之后固定 240t | 3 + random(2)（3~4 只） | 生命 16.0、攻击 2.0、移速 0.34、跟随 28.0 | 命中附缓慢 I 80t + 饥饿 I 100t；600t（30 秒）后消散 |
| 苍蝇 | `ftboss:fly_minion`，0.7 × 0.6 | 阶段 ≥ 2；首次 180t，之后 200 + random(80) t | 4 + random(3)（4~6 只） | 生命 6.0、攻击 1.5、移速与飞行速度 0.40、跟随 32.0 | 无重力飞行，绕目标 1.6 格半径盘旋；600t（30 秒）后消散；咬中不再触发「屎？！！」 |
| 蛆 | `ftboss:maggot_minion`，0.7 × 0.5 | 无阶段限制；首次 140t，之后 170 + random(80) t | 3 + random(3)（3~6 只） | 生命 12.0、攻击 4.0、移速 0.32、跟随 32.0 | 命中叠 1 层【感染】（上限 10 层、每层 600t；感染期间玩家每次攻击敌人都受「层数」点魔法伤害，`entity/MaggotMinionEntity.java:52-54`、`boss/BossHandler.java:80`）；只吃玩家伤害；600t 后消散；生成时打主人标记 |
| 棕黑史莱姆 | `ftboss:compost_slime`，1.0 × 1.0 | 无阶段限制；首次 160t，之后 220 + random(90) t | 3 + random(2)（3~4 只） | 继承原版史莱姆，属性表用 `Monster.createMonsterAttributes()` | 生成体型 2~4 号（血量/移速按原版史莱姆体型公式）；死亡分裂成小史莱姆；900t（45 秒）后消散 |
| 羊屎蛋 | `ftboss:sheep_dung`，0.8 × 0.8 | 无阶段限制；首次 120t，之后 200 + random(80) t | 3 + random(3)（3~6 只） | 继承原版岩浆怪，属性表用 `Monster.createMonsterAttributes()` | 生成体型 1~2 号；死亡爆炸半径 0.95（不破坏地形）+ 3 格内 2.5 伤害 + 反胃 I 100t；寿命到点消散不爆炸；900t（45 秒）后消散 |

- 召唤物生成时都会把目标设为 Boss 当前目标（`entity/CompostBossEntity.java:449`、`entity/CompostBossEntity.java:496`）。
- 同类召唤物上限 10 只（以 Boss 为中心 64 格内统计，`boss/ai/SummonLimit.java:38`、`boss/ai/SummonLimit.java:40`）；到上限时本次不召唤，改为场上同类全体获得 速度 II / 力量 I / 抗性 I，持续 400t（20 秒）（`boss/ai/SummonLimit.java:42`、`boss/ai/SummonLimit.java:72-74`）。
- Boss 死亡清场：按类型清掉 80 格内的答辩小怪、苍蝇、棕黑史莱姆、羊屎蛋，并按主人标记（`MinionBlessing`）清掉自己召唤的全部召唤物（蛆与故障机器人共用同一实体类型 `ftboss:maggot_minion`，按类型清会互相误杀，故只能认主人）（`entity/CompostBossEntity.java:778-784`、`boss/ai/SummonSweep.java:29`、`boss/ai/SummonSweep.java:97`）。

## 五、阶段与转换

| 阶段 | 进入阈值 | 移动方式 | 可用技能 | 背景音乐 |
|---|---|---|---|---|
| 阶段 1 | 初始（生命 > 70%） | 地面步行，`MeleeAttackGoal` 速度倍率 1.0 | 连射、抛射、毒气爆炸、蛆、棕黑史莱姆、羊屎蛋、「屎？！！」 | 《堆肥·强袭》`ftboss:boss_music_compost` |
| 阶段 2 | 生命 ≤ 70%（`P2_AT`） | 无重力悬浮（地面 + 5.0 格），环绕 + 冲刺机动，机体始终转向玩家 | 阶段 1 全部技能，另加答辩小怪、苍蝇、空中弹幕、俯冲撞击、空中投弹 | 切换为《堆肥·熔毁》`ftboss:boss_music_compost_p2` |
| 阶段 3 | 生命 ≤ 35%（`P3_AT`） | 同阶段 2 | 同阶段 2（弹幕、投弹、俯冲伤害随阶段系数提高） | 继续《堆肥·熔毁》 |

- 阶段只升不降（`want > getPhase()` 才切换），进入阶段 2 起 `empowered` 置 true（`entity/CompostBossEntity.java:242-258`）。
- 阶段 2 转换播 `transform` 动画 56t，阶段 3 播 `phase_shift` 动画 44t；同时播 `WITHER_SPAWN`（音量 1.4、音调 0.7）与 24 点 `HAPPY_VILLAGER` 粒子（`entity/CompostBossEntity.java:81`、`entity/CompostBossEntity.java:260-264`）。
- 每次升阶先 `cancelPendingSkills()`（`entity/CompostBossEntity.java:243`）。
- BGM：进入阶段 2 时向 64 格内玩家停掉一阶段曲，再以音量 4.0 播放二阶段曲（`entity/CompostBossEntity.java:246-257`）；战斗中每 tick 调 `BossMusic.request` 登记曲目，本 Boss 曲目组 `COMPOST` 对应一阶段曲，且该组被排除在「曲尾重播」与「每 600t 补发播放包」之外，避免与实体自播的二阶段曲叠响（`entity/CompostBossEntity.java:626`、`sound/BossMusic.java:367`、`sound/BossMusic.java:636`、`sound/BossMusic.java:659`）；中文曲名见 `assets/ftboss/lang/zh_cn.json:197`、`assets/ftboss/lang/zh_cn.json:205`。

## 六、「屎？！！」诅咒与混乱

| 项目 | 数值 | 出处 |
|---|---|---|
| 效果 id 与类别 | `ftboss:poop_confusion`，`HARMFUL`，颜色 0x8B5A2B | `effect/FTEffects.java:22`、`effect/PoopConfusionEffect.java:26` |
| 唯一来源 | 仅 Boss 概率施放一次（`PoopCurse.castOnce`）；命中触发与召唤物触发均已取消 | `boss/ai/PoopCurse.java:33` |
| 尝试间隔 | 首次 200t；之后 200 + random(120) t（10~16 秒） | `entity/CompostBossEntity.java:554` |
| 命中概率 | 35%（`random.nextFloat() < 0.35f`） | `entity/CompostBossEntity.java:558` |
| 施放条件 | 目标为 `ServerPlayer`、距离 ≤ 32 格、通过 `canStartSkill()`；结算时还需有视线且距离 ≤ 32 格 | `entity/CompostBossEntity.java:553-569` |
| 前摇 | 17t 抬手（`cannon_fire` 动作），并播 `SLIME_SQUISH`（音量 1.2、音调 0.6） | `entity/CompostBossEntity.java:559-562` |
| 层数 | 固定 I 级（`AMPLIFIER` = 0），不叠层 | `boss/ai/PoopCurse.java:28` |
| 时长 | 160t（8 秒，`DURATION`） | `boss/ai/PoopCurse.java:26` |
| 画面影响（I 级） | A/D 左右反向（`leftImpulse` 取反，`left` 与 `right` 互换） | `client/PoopConfusionClient.java:46-50` |
| 画面影响（II 级） | 前后 + 左右全反（整个摇杆翻转） | `client/PoopConfusionClient.java:51-58` |
| 画面影响（III 级及以上） | 整体旋转 90°（W→左、A→后、S→右、D→前） | `client/PoopConfusionClient.java:59-67` |
| 法力流失 | 每 20t 结算一次，扣 `3.0 + 等级 × 2.0` 点（I 级 = 3 点/秒）；用反射调用 Iron's Spells `MagicData`，未装该模组则只保留方向键混乱 | `effect/PoopConfusionEffect.java:23`、`effect/PoopConfusionEffect.java:37`、`effect/PoopConfusionEffect.java:43` |
| 反馈 | 快捷栏提示、18 点 `ITEM_SLIME` + 6 点 `LARGE_SMOKE` 粒子、`SLIME_ATTACK`（音量 1.0、音调 0.55） | `boss/ai/PoopCurse.java:37-45` |

- 当前来源固定 I 级且不叠层，实战只会走到 I 级分支（A/D 反向）；II 级与 III 级分支保留在客户端代码中（`client/PoopConfusionClient.java:51-67`）。
- 投掷物自带的缓慢/反胃/饥饿与「屎？！！」相互独立（`entity/RainbowPoopEntity.java:68-70`）。

## 七、血条与掉落

| 项目 | 数值 | 出处 |
|---|---|---|
| 血条类型 | `ServerBossEvent`，颜色 `GREEN`，样式 `PROGRESS`，名称 = 实体显示名 | `entity/CompostBossEntity.java:137-138` |
| 血条进度 | 当前生命 ÷ 最大生命（限制在 0~1）；生命为 0 时隐藏 | `entity/CompostBossEntity.java:617-618` |
| 血条可见范围 | 48 格内加入、超出移除 | `entity/CompostBossEntity.java:619-624` |
| 血条登记与清理 | 构造时 `BossBars.track`；`remove()` 与离开世界（区块卸载）时取消登记 | `entity/CompostBossEntity.java:139`、`entity/CompostBossEntity.java:789`、`boss/BossHandler.java:516-517` |
| 战利品表 | `ftboss:entities/compost_boss`：1 × `ftboss:compost_crown`（污秽虹冠） | `data/ftboss/loot_tables/entities/compost_boss.json:3-13` |
| 遗物掉落 | 必掉 `ftboss:rainbow_spore_charm`（虹彩孢子符），附带随机 1 条词缀（vitality / guarding / swift / fortunate） | `util/BossRelicRules.java:6`、`util/BossRelicRules.java:9`、`util/BossRelicRules.java:18`、`skill/BossRelicDrops.java:60-61` |
| 法术卷轴掉落 | 每个已启用的签名法术各掉 1 张满阶卷轴，最多 5 张 | `integ/spells/BossSpellDrops.java:52-53` |
| 经验 | `xpReward` = 120 | `entity/CompostBossEntity.java:135` |
| 主手武器掉落 | 概率 0，污秽虹冠不会随尸体掉落 | `entity/CompostBossEntity.java:227` |
| 掉落前提 | `doMobLoot` 游戏规则开启 + 由玩家击杀 + 未重复发放（遗物与卷轴各有一份「已发放」标记） | `skill/BossRelicDrops.java:53-55`、`integ/spells/BossSpellDrops.java:47-51` |

## 八、铁魔法签名法术（需装 Iron's Spells）

| 法术 id（中文名） | 学院 | 基础强度 | 单体冷却 | 效果要点 |
|---|---|---|---|---|
| `prismatic_blight`（彩秽孢潮） | nature | 48 | 360t | 24 格内生成半径 4 格的孢域（存续 60t），第 0 / 1 / 2 秒各结算一次伤害并施加中毒 I 80t（4 秒）（`assets/ftboss/lang/zh_cn.json:437`、`integ/spells/BossSpellEffects.java:198`、`integ/spells/BossSpellEffects.java:202`、`integ/spells/BossSpellEffects.java:263`） |
| `virulent_harvest`（毒性收割） | nature | 68 | 240t | 24 格内锁定半径 4 格的收割区域，中毒目标增伤 50%（×1.5）并消耗中毒，每个被引爆的中毒目标回 1 点生命（单次至多 4）（`assets/ftboss/lang/zh_cn.json:416`、`integ/spells/BossSecondSpellEffects.java:132`、`integ/spells/BossSecondSpellEffects.java:140`） |
| `spore_eruption`（孢囊喷发） | nature | 64 | 330t | 落点预警 0.8 秒（16t）后喷发孢子，造成伤害、向上抛起 0.5 并附中毒 I 100t（5 秒）（`assets/ftboss/lang/zh_cn.json:600`、`util/BossSpellRules.java:108`、`integ/spells/BossExpandedSpellEffects.java:230-232`） |
| `rotting_requiem`（腐朽挽歌） | nature | 72 | 390t | 腐化波由 2 格扩至 6 格（每 20t 一跳、共 3 跳），每个敌人只命中一次，消耗中毒增伤 50% 并施加凋零 I 60t（3 秒）（`assets/ftboss/lang/zh_cn.json:603`、`integ/spells/BossExpandedSpellEffects.java:183`、`integ/spells/BossExpandedSpellEffects.java:191`、`integ/spells/BossExpandedSpellEffects.java:236`） |
| `living_mulch`（活性腐殖） | nature | 9 | 450t | 治疗自身及 4 格内盟友并清除中毒、凋零，被净化者额外获得 8 点护盾（140t = 7 秒）（`assets/ftboss/lang/zh_cn.json:606`、`integ/spells/BossExpandedSpellEffects.java:142-143`） |

- 触发：`BossSpellCombat` 每 tick 检查，目标在 32 格内且有视线才进入施放流程（`integ/spells/BossSpellCombat.java:43`）；按 `BossSpellRules.forBoss("ftboss:compost_boss")` 的 5 个法术顺序轮流使用（`util/BossSpellRules.java:77`）。
- 节奏：开场后 60t + 实体 id % 20 首次可用，之后每 60t 尝试一次（`integ/spells/BossSpellCombat.java:77`、`integ/spells/BossSpellCombat.java:93`、`integ/spells/BossSpellPacing.java:13-14`）；每个法术另有独立冷却 = `max(160, ⌈getSpellCooldown() × 0.75⌉)` t，而 `getSpellCooldown()` 返回的就是 tick（= 配置秒数 × 20）（`integ/spells/BossSpellPacing.java:22-23`、`integ/spells/BossSpellCombat.java:94`），本 Boss 五个法术的 `Kind.cooldown` 为 24 / 16 / 22 / 26 / 30 秒（`util/BossSpellRules.java:19`、`util/BossSpellRules.java:26`、`util/BossSpellRules.java:43-45`），代入后 160t 下限不生效，依次为 360 / 240 / 330 / 390 / 450t。
- 前摇：施法时间 32t，常规 Boss × 2/3 后为 22t（`util/BossSpellRules.java:9`、`integ/spells/BossSpellTuning.java:27`、`integ/spells/BossSignatureSpell.java:49-52`）。
- 范围：目标 ≤ 24 格才施放，效果半径 4 格、最多 16 个目标（`util/BossSpellRules.java:10-12`）。
- 伤害：基础强度由 `BossSpellTuning.signaturePower` 给出（非辅助法术再 ×2），实际伤害 = 基础强度 × `SPELL_POWER` 属性 × 学院系数 × 配置 `POWER_MULTIPLIER`，随装备与配置浮动，代码中不写死（`integ/spells/BossSpellTuning.java:33-61`）。
- 生效前提：对应法术 `isEnabled()` 为真才参与轮换与掉落（`integ/spells/BossSpellCombat.java:85`）；未装铁魔法时整套不生效。
