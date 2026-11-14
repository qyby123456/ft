# 鼠群（主王与副鼠群）

常规 Boss 第 5 只：实体 id `ftboss:rat_boss`（`entity/FTEntities.java:68-69`）。主王与副鼠群是同一实体类型，副王只是本体的 `isMinion` 变体（`entity/RatBossEntity.java:117-127`），不新增实体、模型或网络协议。时间单位 tick（20 tick = 1 秒），距离单位格；本节数值全部取自当前源码。

## 一、实体、模型与召唤

| 项目 | 数值 | 出处 |
|---|---|---|
| 实体 id | `ftboss:rat_boss`（主王与副王共用） | `entity/FTEntities.java:68-69` |
| 注册判定箱与分类 | 0.6 × 0.975 格，`MobCategory.MONSTER` | `entity/FTEntities.java:68-69,208-220` |
| 客户端追踪范围 | 10 格 | `entity/FTEntities.java:219` |
| 主王判定箱 | 0.6 × 0.975 格 | `entity/RatBossEntity.java:139-144` |
| 副王判定箱 | 0.4 × 0.6 格 | `entity/RatBossEntity.java:139-144` |
| 模型与贴图 | 主王 `geo/big_rat.geo.json` 与 `textures/entity/big_rat.png`；副王 `geo/small_rat.geo.json` 与 `textures/entity/small_rat.png` | `client/model/RatGeoModel.java:11-25` |
| 共用动画 | `animations/rat.animation.json`：死亡或受击 `startle`、近战 `bite`、召唤 `call`、按水平速度分 `run` 与 `walk` 与 `idle`、离地 `jump` | `client/model/RatGeoModel.java:27-30`、`entity/RatBossEntity.java:152-184` |
| 显示名 | 主王「鼠群」加粗常显；副王「小老鼠」常显 | `item/RatSwarmBaitItem.java:50-51`、`boss/ai/RatSwarmEncounter.java:257-258`、`assets/ftboss/lang/zh_cn.json:169,174,193` |
| 召唤入口 | 物品 `ftboss:rat_bait`「奶酪诱惑」，堆叠 16，右键消耗 1 个 | `item/RatSwarmBaitItem.java:25,59`、`assets/ftboss/lang/zh_cn.json:119` |
| 召唤位置 | 视线水平方向 2.5 格、Y 取玩家 Y，贴地放置后满血并 `setPersistenceRequired()` | `item/RatSwarmBaitItem.java:36-54` |
| 维度限制 | 仅主世界（`OVERWORLD_ONLY` 为 true），非主世界中止且不消耗 | `boss/SummonGate.java:25,30-33,51-60`、`item/RatSwarmBaitItem.java:33-35` |
| 火表现 | 白天不燃烧、着火不叠火焰图层（火焰伤害照旧） | `entity/RatBossEntity.java:81-84,446-449` |

## 二、主王：数值与护盾循环

| 项目 | 数值 | 出处 |
|---|---|---|
| 基础最大生命 | `MASTER_HEALTH = 20000`，召唤时由 `initialize` 写入属性并补满 | `boss/ai/RatSwarmRules.java:5`、`boss/ai/RatSwarmEncounter.java:178-184` |
| 属性表默认生命 | 300.0（注册默认值，进入循环后即被 20000 覆盖） | `entity/RatBossEntity.java:208-217`、`boss/ai/RatSwarmEncounter.java:180-181` |
| 攻击与护甲 | `ATTACK_DAMAGE` 与 `ARMOR` 都写 0，主王全程不攻击 | `boss/ai/RatSwarmEncounter.java:182-183` |
| 行为 | 每 tick 强制无 AI、清目标、停寻路 | `boss/ai/RatSwarmEncounter.java:146-148,199-200` |
| 护盾阶段 | `GUARD_PHASE = 0`：除穿盾与绕过无敌的伤害外全部免伤 | `boss/ai/RatSwarmRules.java:19`、`boss/ai/RatSwarmEncounter.java:116-133,149-150` |
| 装置阶段 | `DEVICE_PHASE = 1`：解除无敌，允许武器伤害 | `boss/ai/RatSwarmRules.java:20`、`boss/ai/RatSwarmEncounter.java:124,150,344` |
| 阶段切换 | 共享池归零或 `DEFEATED` 置位进装置阶段；装置全部结算后回护盾 | `boss/ai/RatSwarmEncounter.java:165,337-370,372-387` |

- 召唤即护盾待机：`prepare` 置 `setNoAi(true)` 与 `setInvulnerable(true)`，主王原地不动、不索敌（`boss/ai/RatSwarmEncounter.java:109-114`）。
- 破盾：副鼠群共享池归零时 `beginDevices` 清空副鼠群、把计时器置 400、解除无敌并在周围 4 格生成 3 个装置，同时向 48 格内玩家播报「副鼠群倒下」（`boss/ai/RatSwarmEncounter.java:337-370`）。
- 回盾：本轮装置全部喂满或全部爆炸后 `beginGuard` 恢复护盾、换新波次 UUID、共享池回满并重新生成 12 只副王；主王血量不回复（`boss/ai/RatSwarmEncounter.java:372-387`）。
- 主王血条只反映主王自身血量：护盾期默认不掉血，只有穿盾与装置阶段的伤害会推动它（`boss/ai/RatSwarmEncounter.java:389-397`）。
- 不参与的全局系统：主王与副王都跳过 `BossEmpower` 的属性强化与常驻增益；主王也不跑狂暴、破困、防卡瞬移与召唤物增益分享（`boss/ai/BossEmpower.java:56-58`、`boss/ai/RatSwarmEncounter.java:83-85`、`entity/RatBossEntity.java:242-256`）。

## 三、副鼠群与共享血池

| 项目 | 数值 | 出处 |
|---|---|---|
| 基础共享生命 | `DEPUTY_HEALTH = 400`，整群共用一份，不是每只独立 400 | `boss/ai/RatSwarmRules.java:6` |
| 池上限 | 400 × 主王最大生命 ÷ 20000 | `boss/ai/RatSwarmEncounter.java:275-277` |
| 数量 | `SWARM_SIZE = 12`；共享生命低于池上限一半时 `WOUNDED_SWARM_SIZE = 16` | `boss/ai/RatSwarmRules.java:8-9,24-26` |
| 每只副王的血量 | 最大生命 = 池上限，当前生命 = 池值，逐 tick 同步 | `boss/ai/RatSwarmEncounter.java:249,253,332-335` |
| 承伤折算 | 结算后伤害 × `SWARM_DAMAGE_TAKEN = 0.6` 扣池，下限 0 | `boss/ai/RatSwarmRules.java:10,28-31` |
| 共享受击冷却 | 10 tick（字面量判定，没有单独常量） | `boss/ai/RatSwarmRules.java:33-36`、`boss/ai/RatSwarmEncounter.java:571-576` |
| 副王基础攻击 | `DEPUTY_DAMAGE = 2.0` | `boss/ai/RatSwarmRules.java:7`、`boss/ai/RatSwarmEncounter.java:247,250` |
| 副王护甲与移速 | 护甲 0、移速 0.24 | `boss/ai/RatSwarmEncounter.java:251-252` |
| 副王近战 | 前摇 5 tick、动作 10 tick、触及 1.8 格 | `entity/RatBossEntity.java:45-46,72-74` |
| 补员宽限 | `MISSING_GRACE = 200`，丢失条目清掉后按池比例补召 | `boss/ai/RatSwarmEncounter.java:71,307-318` |

- 受击入口：`onDeputyHurt` 在 `LivingHurtEvent` 的 `LOWEST` 优先级判定；主人不存在、不是本波成员或主王不在护盾阶段时整下取消，否则把副王自身无敌帧清零，保证每一下都进入结算（`boss/ai/RatSwarmEncounter.java:551-559,119-121`）。
- 共享冷却：距最近一次全额结算不足 10 tick 时只扣「本次伤害 − 上次记录」的差额（等额或更低记 0），满 10 tick 按全额；带 `BYPASSES_COOLDOWN` 标签的伤害视为冷却已过（`boss/ai/RatSwarmRules.java:33-36`、`boss/ai/RatSwarmEncounter.java:570-576`）。
- 扣池：`onDeputyDamage` 取消副王自身的掉血事件，再用 `damageSwarm` 扣 `0.6 × 伤害`，护甲与减伤已在事件里结算完毕；同一发范围伤害打到多只副王时，后续几次只有打出更高伤害才补差额，不会按只数重复扣（`boss/ai/RatSwarmEncounter.java:561-585`）。
- 归零：池值到 0 时置 `DEFEATED` 并 `clearSwarm`，下一次 tick 进入装置阶段（`boss/ai/RatSwarmEncounter.java:165,579-582`）。
- 补召不回血：护盾阶段每 20 tick 按池比例补员，新副王直接继承当前池值（`setHealth` 用池值），血池本身不动（`boss/ai/RatSwarmEncounter.java:230-236,249-253,318`）。
- 副王在正常战斗中不会死亡：伤害在 `LivingDamageEvent` 被取消，只有换阶段、清场或主人失效时才 `discard()`（`boss/ai/RatSwarmEncounter.java:565,212-217,454-462`）。

## 四、限时奶酪装置

| 项目 | 数值 | 出处 |
|---|---|---|
| 每轮数量与摆放 | `DEVICE_COUNT = 3`，主王周围 4 格按 0/120/240 度贴地摆放 | `boss/ai/RatSwarmRules.java:11`、`boss/ai/RatSwarmEncounter.java:345-350` |
| 期限 | `DEVICE_TICKS = 400`（20 秒），只在主王 tick 时递减 | `boss/ai/RatSwarmRules.java:12`、`boss/ai/RatSwarmEncounter.java:151-158` |
| 充能 | 上限 `DEVICE_FULL = 100`，每份 `FOOD_HEAL = 20`，共 5 份 | `boss/ai/RatSwarmRules.java:13-14`、`boss/ai/RatSwarmEncounter.java:42-44` |
| 完成伤害 | 主王喂食瞬间最大生命的 `DEVICE_DAMAGE_FRACTION = 0.125`，魔法伤害 | `boss/ai/RatSwarmRules.java:16`、`boss/ai/RatSwarmEncounter.java:523-528` |
| 超时爆炸 | `EXPLOSION_DAMAGE = 8.0` 点，半径 `EXPLOSION_RADIUS = 4.0` 格 | `boss/ai/RatSwarmRules.java:17-18`、`boss/ai/RatSwarmEncounter.java:538-549` |
| 交互冷却 | `FEED_COOLDOWN = 4` tick，按装置各自计时 | `boss/ai/RatSwarmRules.java:15`、`boss/ai/RatSwarmEncounter.java:507-509` |
| 装置外形 | 隐形盔甲架，头上戴原版木桶，无敌、无重力 | `boss/ai/RatSwarmEncounter.java:351-356` |
| 可喂材料 | 标签 `ftboss:rat_swarm_healing_food`、牛奶桶、注册名含 `cheese` 的可食用物品 | `boss/ai/RatSwarmEncounter.java:68-69,476-481` |
| 喂食条件 | 玩家存活、非旁观、距装置不超过 6 格且有视线，本轮装置未超时 | `boss/ai/RatSwarmEncounter.java:499-501` |
| 消耗 | 创造模式不消耗；有容器残留的物品返还容器（牛奶桶返还桶） | `boss/ai/RatSwarmEncounter.java:513-520` |
| 名称显示 | 「奶酪装置 充能/100 剩余秒数」，每 20 tick 与每次喂食后刷新 | `boss/ai/RatSwarmEncounter.java:160-162,522,533-536` |

- 破盾提示：向 48 格内玩家发「副鼠群倒下！20秒内用奶酪或牛奶右键喂满装置，每个装置需要5份」（`boss/ai/RatSwarmEncounter.java:366-368`）。
- 喂满：装置立刻消失、飘 4 颗爱心粒子并播 `GENERIC_EAT`，把主王无敌帧清零后立刻结算 12.5% 魔法伤害；三个装置都喂完即回护盾（`boss/ai/RatSwarmEncounter.java:521-530`）。
- 超时：计时器到 0 的那一 tick 把剩余装置逐个引爆，4 格内存活、非创造、非旁观玩家各吃 8 点（走 `mobAttack`，护甲与减伤正常生效）；不破坏方块、不伤害主王与其他实体，随后回护盾（`boss/ai/RatSwarmEncounter.java:155-159,541-547`）。
- 8 个装置正好打掉 100% 最大生命，因此不计其它伤害时固定 8 个装置击杀主王，按每轮 3 个算约 3 轮、40 份材料（`boss/ai/RatSwarmRules.java:16`、`boss/ai/RatSwarmEncounter.java:526`）。

## 五、伤害口径与高配路线

| 规则 | 当前口径 | 出处 |
|---|---|---|
| 装置阶段 | 主王解除无敌，武器伤害按正常管线结算，护甲为 0 | `boss/ai/RatSwarmEncounter.java:124,150,183` |
| 护盾穿盾 | 来自玩家的单次输入伤害达到主王当前最大生命即可穿盾，随后照常结算；非有限数值不能过门槛 | `boss/ai/RatSwarmRules.java:46-48`、`boss/ai/RatSwarmEncounter.java:125-131` |
| 绕过无敌 | 带 `BYPASSES_INVULNERABILITY` 的伤害（`/kill`、虚空等）不受护盾限制 | `boss/ai/RatSwarmEncounter.java:123`、`boss/ai/BossDamageGate.java:151-152` |
| 全局最终伤害规则 | `MAX_FINAL_DAMAGE = 300.0`：结算后实际扣血大于 300 时整次归零（不是截断），恰好 300 放行，`GENERIC_KILL` 不参与 | `boss/ai/FinalBossDamageGuard.java:19,33-41` |
| 鼠群是否适用 | 不适用：`FinalBossDamageGuard.applies` 对 `RatBossEntity` 一律返回 false，主王与所有小鼠都不受限 | `boss/ai/FinalBossDamageGuard.java:23-31` |
| 限伤总开关 | `LIMIT_ENABLED` 为 false，单次上限、连击递减、反逃课距离都不生效 | `boss/ai/BossDamageGate.java:46,147-149` |
| 真实伤害免疫 | 主王走 `BossDamageGate`，流血与末日类伤害 id 仍完全免伤；副王承伤直接扣池，没有这道免伤 | `boss/ai/BossDamageGate.java:120-145`、`entity/RatBossEntity.java:227-236`、`boss/ai/RatSwarmEncounter.java:561-585` |

- 最终伤害规则的判定点是 `ForgeHooks.onLivingDamage` 的返回值，即护甲、抗性、吸收全部结算之后真正会扣掉的血量；判定用 `> 300`，命中时这次血量伤害整体按 0 处理，不会截成 300（`mixin/FinalBossDamageMixin.java:12-22`、`boss/ai/FinalBossDamageGuard.java:33-41`）。
- 鼠群的所有实体（主王、副王、旧版小鼠）都不吃这条规则，破盾期一次高额武器伤害可以完整结算；其它 Boss 仍按 300 归零（`boss/ai/FinalBossDamageGuard.java:23-31`）。

## 六、多人规模与终末难度

| 项目 | 规则 | 出处 |
|---|---|---|
| 主王基础生命 | `MASTER_HEALTH = 20000`；终末难度再 ×2；再乘人数倍率 `HP_PER_PLAYER = 1.25` 的 n−1 次方 | `boss/ai/RatSwarmRules.java:5`、`boss/NonManorFinaleDifficulty.java:33,86-93`、`boss/ai/BossScaling.java:24,68-84` |
| 副鼠群池上限 | `DEPUTY_HEALTH = 400` × 主王最大生命 ÷ 20000，因此随终末 ×2 与人数倍率同步放大 | `boss/ai/RatSwarmRules.java:6`、`boss/ai/RatSwarmEncounter.java:245-249,275-277` |
| 剩余比例 | 池上限变化时按「当前池值 ÷ 旧上限 × 新上限」折算，不会回满 | `boss/ai/RatSwarmRules.java:38-40`、`boss/ai/RatSwarmEncounter.java:304-306` |
| 人数统计 | `RADIUS = 48.0` 格内存活、非创造、非旁观玩家，每 20 tick 重算一次 | `boss/ai/BossScaling.java:22,37-44,67-70`、`boss/BossHandler.java:57-62` |
| 副王伤害 | 基础 `DEPUTY_DAMAGE = 2.0`，同样吃人数伤害倍率 `DMG_PER_PLAYER = 0.15` | `boss/ai/BossScaling.java:26,86-90` |
| 装置伤害 | 始终按喂食瞬间最大生命的 12.5% 计算，任何规模下都是 8 个装置打满 100% | `boss/ai/RatSwarmRules.java:16`、`boss/ai/RatSwarmEncounter.java:526` |

- 终末难度由召唤者难度决定：`NonManorFinaleDifficulty` 在实体入世界时把主王属性基础值乘 2（`ATTRIBUTE_MULTIPLIER = 2.0`），副王本身不算常规 Boss，池上限的翻倍来自「主王最大生命 ×2」这条链路（`boss/NonManorFinaleDifficulty.java:33,43-65,86-93,143-151`）。

## 七、命中减益与副王行为

| 项目 | 数值 | 出处 |
|---|---|---|
| 减益池 | 原版 7 种：中毒、虚弱、缓慢、挖掘疲劳、饥饿、霉运、凋零；再加 `armamentarium` 的 16 条候选（`vulnerable` 重复一次），取不到的自动跳过 | `boss/BossHandler.java:325-348` |
| 等级与时长 | 等级 `nextInt(2)` 即 I 至 II；时长 140 至 259 tick | `boss/BossHandler.java:352-358` |
| 触发 | 副王每次命中玩家随机挂一种，无独立冷却，不推行动栏文本 | `boss/BossHandler.java:379-380,359-363` |
| 目标选择 | 每 20 tick 锁定 48 格内最近的非创造玩家 | `boss/ai/RatSwarmEncounter.java:221,411-418` |
| 自身减益 | 副王每 tick、主王每 20 tick 清掉自身全部非增益效果 | `boss/ai/RatSwarmEncounter.java:171-173,223-225` |

## 八、血条与音乐

| 项目 | 数值 | 出处 |
|---|---|---|
| 主王血条名称 | 护盾期为「鼠群主王 · 护盾（击败副鼠群破盾）」紫色，装置期为「鼠群主王 · 装置倒计时 N秒」红色 | `boss/ai/RatSwarmEncounter.java:389-396` |
| 主王血条进度 | 主王当前生命 ÷ 最大生命 | `boss/ai/RatSwarmEncounter.java:397` |
| 副王血条 | 只显示一只「鼠群副王 · 共享血池」黄色，进度为共享池比例，其余副王每 tick 隐藏 | `boss/ai/RatSwarmEncounter.java:226-227,394-396`、`entity/RatBossEntity.java:108-111` |
| 可见范围 | `BAR_RANGE = 48.0` 格内的存活、非旁观玩家，圈内无人即隐藏 | `boss/ai/RatSwarmEncounter.java:70,398-409` |
| BGM | 主王每 20 tick 向 `BossMusic` 登记《鼠潮夜行》（`boss_music_rat`），死亡或移除时释放；副王不登记 | `boss/ai/RatSwarmEncounter.java:169-170,625`、`sound/BossMusic.java:105-114`、`sound/FTSounds.java:54-56` |
| 音乐总开关 | `MUSIC_ENABLED` 为 true 时生效 | `sound/FTSounds.java:32` |
| 血条注销 | 死亡、移除与全局兜底清扫三条路径都会摘除 | `entity/RatBossEntity.java:395-396,432-434`、`boss/BossBars.java:76-95` |

## 九、生命周期、归属与掉落

| 项目 | 规则 | 出处 |
|---|---|---|
| 场次绑定 | 主王用 `ftboss:rat_swarm_wave` 与 `ftboss:rat_deputies` 记录本轮，副王带 `ftboss:rat_cycle_owner` 与同一波次 UUID；三者对得上才算本场 | `boss/ai/RatSwarmEncounter.java:51-57,430-441` |
| 持久化 | 阶段、计时、池值与上限、波次 UUID、副王 UUID 列表、装置 UUID 列表都写在实体持久数据里；副王 `setPersistenceRequired()` | `boss/ai/RatSwarmEncounter.java:50-69,256` |
| 区块卸载 | `UNLOADED_TO_CHUNK` 与 `UNLOADED_WITH_PLAYER` 只释放音乐，不清副王、不清装置、不重置阶段 | `boss/ai/RatSwarmEncounter.java:628-636` |
| 找不到主人 | 副王与装置各自计 `MISSING_GRACE = 200` tick，超时才 `discard()` | `boss/ai/RatSwarmEncounter.java:71,203-211,599-613` |
| 副王丢失补召 | 加载中的副王少于记录数时累计丢失 tick，满 200 清掉条目后按池比例补召，保留当前池值 | `boss/ai/RatSwarmEncounter.java:307-318` |
| 异常消失不算破盾 | 副王死亡事件只在池值小于等于 0 时才置 `DEFEATED` | `boss/ai/RatSwarmEncounter.java:587-597` |
| 主王死亡或移除 | `cleanup` 清副王与装置、删归属键并释放音乐；64 格内旧版小鼠一并 `kill()` | `boss/ai/RatSwarmEncounter.java:619-636`、`entity/RatBossEntity.java:392-405,430-444` |

- 副王不是 Boss：`BossAdvancements.bossId` 与 `BossMercy.typeOf` 对 `isMinion` 都返回 null，因此不给胜利进度，也不掉石像、专属武器、遗物与 FTB 战利品箱（`boss/BossAdvancements.java:127-136`、`boss/ai/BossMercy.java:258-270,294-309`、`boss/ai/NonManorLootCrateDrops.java:28-37`）。
- 经验：主王与所有同类型实体共用 `xpReward = 120`，源码没有按副王减免；副王正常战斗不会死亡，离场一律 `discard()`，不结算经验（`entity/RatBossEntity.java:99-105`、`boss/ai/RatSwarmEncounter.java:454-462`）。
- 主王掉落：石像·鼠群、奶酪轮锤、必掉遗物奶酪月牙坠（`DROP_CHANCE = 1.0`）、FTB Quests 食物 Boss 战利品箱；仓库内没有 `rat_boss` 的原版战利品表（`boss/ai/BossMercy.java:390-423,457-463`、`util/BossRelicRules.java:8,18,36-42`、`boss/ai/NonManorLootCrateDrops.java:28-37`、`src/main/resources/data/ftboss/loot_tables/entities/`）。
- 旧存档：主王首次 tick 发现没有就绪标记时 `initialize` 按当前血量比例重设到 20000，并清掉旧的单只副王键，副鼠群按满池 12 只重建；只有「已就绪但缺列表或池」的存档才走 `ensureSwarm` 的兼容分支，用 `scaleSwarm` 按比例折算旧副王血量（`boss/ai/RatSwarmEncounter.java:178-201,279-300`）。
- 旧版小鼠（有 `isMinion` 但没有本轮 `ftboss:rat_deputy` 标记）不归本循环管，仍走 `BossHandler.onRatSwarmHurt` 的旧共享血池路径（`boss/BossHandler.java:234-258`、`boss/ai/RatSwarmEncounter.java:638-641`）。
- 连死怜悯：同一玩家在主王下连续死亡 `LIMIT = 5` 次即离场（无掉落、无经验），64 格内副王连带清理（`boss/ai/BossMercy.java:44,337-384`）。
