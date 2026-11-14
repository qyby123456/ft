# 酒魔·酒桶王

## 一、实体与判定箱

| 项目 | 值 | 说明 | 出处 |
|---|---|---|---|
| 实体 id | `ftboss:wine_barrel` | 常规 Boss 第 4 只，基类 `AbstractSpellCastingMob`（铁魔法施法者） | `entity/FTEntities.java:49-50`、`entity/WineBarrelBossEntity.java:48` |
| 分类与判定箱 | `MobCategory.MONSTER`，宽 3.0 格 × 高 3.0 格 | 巨型酒桶模型（GeckoLib），固定原地不动（移速 0；只有冲撞位移与防卡瞬移会挪位） | `entity/FTEntities.java:49-50,179-191` |
| 客户端追踪 | `clientTrackingRange` = 10 | 与判定箱同处注册 | `entity/FTEntities.java:190` |
| 召唤物实体 | `ftboss:barrel_minion` 0.8×1.2；`ftboss:barrel_cannonball` 0.5×0.5（MISC）；`ftboss:wine_barrel_decoy` 3.0×3.0 | 小酒桶 / 酒桶炮弹 / 分身 | `entity/FTEntities.java:46-47,70-75` |
| 手中武器 | 主手 `ftboss:barrel_crossbow`（空弦）与 `ftboss:barrel_crossbow_ready`（装填版） | 纯外观件，掉落率 0；只在状态变化时换手 | `entity/WineBarrelBossEntity.java:311-338` |
| 召唤方式 | 物品 `ftboss:wine_charm` 右键，在身前 2.5 格生成 | 满血 + `setPersistenceRequired()`；仅主世界（`OVERWORLD_ONLY` = true） | `item/WineCharmItem.java:33-48`、`boss/SummonGate.java:25,32` |

## 二、基础属性、阶段与全局规则

| 项目 | 值 | 说明 | 出处 |
|---|---|---|---|
| `MAX_HEALTH` | 400.0 | 基础值；实战按 48 格内参战人数再乘 `1.25^(n-1)` | `entity/WineBarrelBossEntity.java:154`、`boss/ai/BossScaling.java:22,24,54` |
| `MOVEMENT_SPEED` | 0.0 | 固定不移动；滚动冲撞用 `setDeltaMovement` 强行位移 | `entity/WineBarrelBossEntity.java:155,608` |
| `ATTACK_DAMAGE` | 5.5 | 近战基础值；再乘人数伤害倍率 `1+0.15×(n-1)` | `entity/WineBarrelBossEntity.java:156`、`boss/ai/BossScaling.java:26,71-72` |
| `ARMOR` | 3.0 | — | `entity/WineBarrelBossEntity.java:157` |
| `KNOCKBACK_RESISTANCE` | 0.85（`BOSS_RESISTANCE`，只吃 15% 击退） | 原始属性值 | `entity/WineBarrelBossEntity.java:158`、`util/BossKnockback.java:31` |
| `FOLLOW_RANGE` | 40.0 | 属性表跟随距离 | `entity/WineBarrelBossEntity.java:159` |
| `xpReward` | 80 | 击杀经验 | `entity/WineBarrelBossEntity.java:146` |
| 索敌 | 仇恨表首选目标，其次 24 格内最近的有效玩家 | 创造 / 旁观模式玩家不算目标 | `entity/WineBarrelBossEntity.java:343`、`boss/ai/BossTargets.java:31-44` |
| 血条 | `ServerBossEvent`，紫色 + `PROGRESS` 分段 | `BOSS_BAR_RANGE` = 48.0 格内可见，进度 = 当前/最大血量 | `entity/WineBarrelBossEntity.java:65,134-136,233-244` |
| BGM | 酒桶王专属曲（`BossMusic.Group.TAVERN`，曲名「酒桶王·醉乡酒馆」） | 首次请求时 40 格内有玩家才登记（之后不再判距离）；`MUSIC_ENABLED` 关闭时不播 | `entity/WineBarrelBossEntity.java:672-691`、`sound/BossMusic.java:364,527` |
| 受击反馈 | 播 `WOOD_BREAK`（音量 0.9 / 音调 0.6） | 着火时不渲染火焰图层（`displayFireAnimation` = false） | `entity/WineBarrelBossEntity.java:198-205,61-64` |
| 火焰吸收 | 伤害来源含 fire / lava / burn / blaze 时回复该次伤害的 75% | 需未满血 | `entity/WineBarrelBossEntity.java:186-195` |
| 负面效果清除 | 每 20t 移除自身全部非增益效果 | 事件层全局 `DENY` 已停用（`IMMUNE_TO_DEBUFFS` = false），本体靠自清，减益实际只能短暂存在 | `entity/WineBarrelBossEntity.java:265-272`、`boss/BossHandler.java:409` |
| 全局强化 | 移速 +25%、攻击 +30%、护甲 +4.0、击退抗性 +0.0（已取消） | 每 20t 由 `BossHandler` 挂一次 | `boss/ai/BossEmpower.java:27-30,55-60` |
| 常驻增益 | 每 400t 刷新 速度 I / 抗性 I / 抗火 I / 力量 II，各持续 520t | 同样每 20t 采样触发 | `boss/ai/BossEmpower.java:31,114-120` |
| 最终伤害 300 与免疫 | 结算后的实际扣血伤害 > `MAX_FINAL_DAMAGE` = 300.0 时本次血量伤害归零（不是截断；恰好 300 或更低放行；不按原始伤害判断）；流血与真实伤害类关键字完全免伤 | `/kill` 等绕过无敌的伤害保留；原始 500 点规则与旧 `LIMIT_ENABLED` 限伤继续停用；本 Boss 适用，玩家、外部 Boss、鼠群小怪、迷你 Boss、召唤物不适用 | `boss/ai/FinalBossDamageGuard.java:19`、`mixin/FinalBossDamageMixin.java:14`、`boss/ai/BossDamageGate.java:120-129` |
| 阶段 0（常驻） | 血量 ≥ 67% | 挂 `vinery:armor_effect` 等级 0 与 `vinery:health_effect` 等级 I；每 10t 检查，剩余 < 120t 时续到 600t | `entity/WineBarrelBossEntity.java:255-272` |
| 阶段 1 | 血量 < 67% | 已有酒 buff 各 +1 级（上限 IV），并新增 `vinery:resistance_effect` 等级 0 | `entity/WineBarrelBossEntity.java:274-297` |
| 阶段 2 | 血量 < 34% | 已有酒 buff 各 +1 级（上限 IV），并新增 `vinery:luck_effect` 等级 I | `entity/WineBarrelBossEntity.java:274-297` |
| 狂暴 I（全局） | 血量 ≤ 50% | 迅捷 I + 抗性 I（每 20t 续 80t）；火焰弹伤害 +4 | `boss/ai/BossRage.java:40,85-89`、`entity/WineBarrelBossEntity.java:446` |
| 狂暴 II（全局） | 血量 ≤ 25% | 追加 力量 I + 再生 I | `boss/ai/BossRage.java:90-95` |
| 分身门槛 | 血量 < 50% | 每 600t 判定一次，24 格内分身不足 2 个才补 | `boss/ai/BossEmpower.java:91-97` |
| 卡位脱困（全局） | 每 10t 清掉压在自己判定箱上的载具与坐垫 / 蜘蛛网类方块 | 载具判定箱外扩 0.35 格，方块外扩 0.12 / 0.05 / 0.12 格；方块不掉落 | `boss/ai/TrapBreaker.java:59-60,65-97`、`entity/WineBarrelBossEntity.java:216` |
| 防卡瞬移（全局） | 每 80t 自查水平位移：< 2.0 格且目标 > 4 格、且不在咏唱中，就瞬移到目标周围 2.0~5.5 格的安全点 | 本体移速为 0，除冲撞外不会自行位移，站着不动 80t 即触发（瞬移失败则落到目标脚边） | `boss/ai/BossUnstuck.java:27-31,74-93`、`entity/WineBarrelBossEntity.java:217` |

- 注意：技能冷却表里的「阶段」用 `stage`（< 67% / < 34%），火焰弹伤害与法术等级用 `BossRage` 层级（≤ 50% / ≤ 25%），两套阈值不重合（`entity/WineBarrelBossEntity.java:276-277,353,358,363,488`）。
- 酒桶动作类技能（炮弹 / 召唤 / 火焰弹、滚动冲撞、开塞喷射）只在动作状态机处于 `ST_IDLE` 时才能起手，动作进行中冷却照常倒数但不会插入新动作（`entity/WineBarrelBossEntity.java:351-366,585-598`）。
- 铁魔法施法不受动作状态机限制：`tickIronMagic` 在 `ST_IDLE` 判定之前调用，所以滚动冲撞 / 开塞喷射的 50t 动作期间照样排队与施法（`entity/WineBarrelBossEntity.java:346-382`）。

## 三、主技能总表

| 技能 | 触发与冷却 | 前摇 | 范围 | 伤害 | 附带 | 出处 |
|---|---|---|---|---|---|---|
| 酒桶炮弹齐射 | 待机且 `cannonCd` ≤ 0；首次 140t，之后 170t（阶段 2 为 110t） | 10t（起手 `bTimer` = `SPRAY_TICKS` 50，结算于 `bTimer` = `SPRAY_TICKS` − `SPRAY_HIT_TICK` = 40） | 落点 ±3.5 格散布；目标超过 32 格则空放 | 爆炸威力 3.0；直击 4.0 | 不破坏方块、不炸掉落物 | `entity/WineBarrelBossEntity.java:352-355,409,654-668`、`entity/BarrelCannonballEntity.java:40-44` |
| 火焰弹 | 待机且 `fireballCd` ≤ 0；首次 60t，之后 70t（阶段 2 为 45t） | 10t | 直线射向目标（ISS `MagicFireball`，出膛朝目标 1.8 格、高度 `Y + 1.6`） | `11 + 4×狂暴层级`（层级 0/1/2 → 11 / 15 / 19） | 命中效果由铁魔法火球结算 | `entity/WineBarrelBossEntity.java:362-366,437-452` |
| 召唤小酒桶 | 待机且 `summonCd` ≤ 0；首次 150t，之后 210t（阶段 ≥1 为 150t） | 10t | 环绕本体 2.5~6.0 格放置 | — | 2~3 只小酒桶，另有最多 1 只啤酒人 | `entity/WineBarrelBossEntity.java:357-361,508-554` |
| 滚动冲撞 | 待机且 `chargeCd` ≤ 0 且 22 格内有有效玩家；首次 110t，砸地后 130t | 10t | 冲撞 22t、每 t 0.42 格（最大约 9.24 格） | 每 8t 判定一次，判定半径 2.9 格内 8.0 × 0.5 = 4.0 | 击退 1.1 | `entity/WineBarrelBossEntity.java:585-618`、`boss/ai/BarrelTactics.java:138-145` |
| 砸地 | 冲撞 22t 到点或撞上方块 | 10t | 半径 6.5 格 | 4.75 | 击退 1.5 + 反胃 120t | `entity/WineBarrelBossEntity.java:613-622`、`boss/ai/BarrelTactics.java:69-87` |
| 开塞喷射 | 待机且 `popCd` ≤ 0 且本轮没排入齐射（`pendingVolley` 为空）；首次 80t，之后 100t | 10t | 11 格内、正前方向量点积 ≥ 0.35（约 70° 锥）、高度差 ≤ 3 | 3.0 | 缓慢 100t + 反胃 160t | `entity/WineBarrelBossEntity.java:593-598,624-631,654-657`、`boss/ai/BarrelTactics.java:90-116` |
| 高级火系法术连射 | 待机且 `castCd` ≤ 0；首次 40t，之后 58t（阶段 2 为 34t） | 长施法时长 × 2/3（`bossCast`） | 24 格内目标 | 法术自身伤害；等级 = 4 + 狂暴层级（夹在法术 min/max 之间） | 每次排 3~5 个火系法术；狂暴时 70% 改抽史诗 / 传说 | `entity/WineBarrelBossEntity.java:369-382,459-506`、`integ/spells/BossSpellTuning.java:27,63-70` |
| 招牌法术 | 战斗开始 60t 后首次，之后每 60t 尝试一次 | 22t（`CAST_TICKS` 32 × 2/3，向上取整） | 24 格内且需视线 | 见招牌法术表 | 见招牌法术表 | `integ/spells/BossSpellPacing.java:13-14`、`integ/spells/BossSpellCombat.java:76-95` |
| 酒桶弩（外观预告） | 距炮弹齐射 ≤ 20t（`CROSSBOW_READY_LEAD` = 20）时换成装填版 | 拉弦进度 = `clamp(45 − cannonCd, 0, 25) / 25` | 仅本体演出，不发射 | — | 状态变化才 `setItemSlot`，不掉落 | `entity/WineBarrelBossEntity.java:311,322-338` |

- 技能优先级：炮弹 > 召唤 > 火焰弹（三者同时到点时先放炮弹），`entity/WineBarrelBossEntity.java:352-366`。
- 动作状态机内部：滚动冲撞蓄力优先于开塞喷射（同一分支用 `else if`），`entity/WineBarrelBossEntity.java:587-598`。
- 齐射类技能结算时会再校验目标：目标死亡、失效或距离超过 32 格则该次齐射空放（`entity/WineBarrelBossEntity.java:659-667`）。
- 铁魔法原版法术（`irons_spellbooks:` 前缀）另有放行门：每次仅 25% 概率放行（`LEGACY_CHANCE`），放行后 240t 内不再放，未放行则 60t 后重试（`integ/spells/BossSpellPacing.java:15-17,26-38`）。

## 四、酒桶炮弹与酒桶弩参数

| 项目 | 值 | 出处 |
|---|---|---|
| 齐射颗数 | `3 + rand(3)` → 3~5 颗 | `entity/WineBarrelBossEntity.java:409` |
| 落点散布 | `(rand − 0.5) × 7.0` → ±3.5 格 | `entity/WineBarrelBossEntity.java:411-412` |
| 出膛位置 | 桶口朝目标方向 2.2 格，高度 `Y + 1.9` | `entity/WineBarrelBossEntity.java:421-424` |
| 水平速度 | 1.05 格/t | `entity/WineBarrelBossEntity.java:417` |
| 重力 | 0.045 格/t² | `entity/BarrelCannonballEntity.java:120` |
| 爆炸威力 | `EXPLOSION_POWER` = 3.0，用 `ExplosionInteraction.NONE` | `entity/BarrelCannonballEntity.java:40,158-159` |
| 直击附加伤害 | `DIRECT_DAMAGE` = 4.0 | `entity/BarrelCannonballEntity.java:42,135` |
| 最长存活 | `MAX_LIFE` = 80t（4 秒）；撞方块、命中实体或落地立即爆炸 | `entity/BarrelCannonballEntity.java:44,127-146` |
| 弹体友伤规则 | 不打其它炮弹与发射者；玩家用弩射出的炮弹能命中酒桶王本体 | `entity/BarrelCannonballEntity.java:103-111` |
| 弩装填 | `CHARGE_TICKS` = 25t（快速装填每级 −5t，最低 5t），背包需有啤酒 | `item/BarrelCrossbowItem.java:51,95-98,139-145` |
| 弩发射 | 装填后右键打出 1 颗爆炸酒桶；多重射击 3 颗、左右各偏 9°；出膛速度 1.65，散布 0.35（多重 1.0），射击冷却 8t | `item/BarrelCrossbowItem.java:55,57,188-201` |
| 弩弹药判定 | 物品 id 词元命中 17 个关键词（beer / ale / wine / rum 等）之一，或位于标签 `ftboss:beer_ammo` | `item/BeerAmmo.java:33-41,46-59` |
| 弩耐久与附魔 | 耐久 384，每次发射 −1；可附魔 快速装填 / 多重射击 / 耐久 / 经验修补 / 消失诅咒 | `item/FTBossItems.java:186-188`、`item/BarrelCrossbowItem.java:121-125` |

## 五、招牌法术（`BossSpellCombat` 自动施放）

| 法术 id | 学派 | 威力基数（`signaturePower`） | 范围 | 附带 | 配置冷却 → 实际门禁 |
|---|---|---|---|---|---|
| `ftboss:vintage_vortex` | evocation | 64（`WINE` 基线 32 ×2） | 半径 4 格圆形 | 拉向中心 0.6 格 + 虚弱 100t + 反胃 100t | 18 秒 → 270t（13.5 秒） |
| `ftboss:cask_rupture` | fire | 84（`WINE_BOMB` 基线 42 ×2） | 半径 5 格，延时 16t 引爆 | 伤害按距离衰减到 0.5~1.0 倍 + 点燃 3 秒 + 向爆心击退 0.6 | 22 秒 → 330t（16.5 秒） |
| `ftboss:cinder_breath` | fire | 46（`WINE_BREATH` 基线 23 ×2） | 长 10 格、半宽 `0.8 + 0.35×距离` 的扇形 | 0 / 12 / 24t 三次脉冲，各点燃 3 秒 | 20 秒 → 300t（15 秒） |
| `ftboss:intoxicating_toast` | evocation | 10（`WINE_TOAST`；辅助法术不 ×2） | 自身（护盾 + 双增益） | 吸收护盾（4~40，取 4 的倍数，持续 160t）+ 力量 I 120t + 速度 I 120t | 30 秒 → 450t（22.5 秒） |
| `ftboss:vintage_minefield` | fire | 68（`WINE_FUSE` 基线 34 ×2） | 中心 2.5 格圆上 3 点，每点半径 2.2 格 | 20 / 32 / 44t 三次脉冲，各点燃 3 秒 + 推开 0.35 | 28 秒 → 420t（21 秒） |

- 威力基数取自 `BossSpellTuning.signaturePower`：非辅助法术 = 源码 switch 基线 ×2，辅助法术（`BossSpellRules.support` = 治疗或护盾，酒桶王只有 `WINE_TOAST`）不乘（`integ/spells/BossSpellTuning.java:33-53`、`util/BossSpellRules.java:90-98`）；实际伤害 = 基数 × 法术强度属性 × 学派强度 × 配置倍率（`BossSpellTuning.scaledPower`，`integ/spells/BossSpellTuning.java:55-61`，调用处 `integ/spells/BossSpellEffects.java:118-122`），故此处给基数。
- 单个招牌法术的实际冷却 = `max(160, ⌈getSpellCooldown() × 0.75⌉)` tick；`getSpellCooldown()` 返回的就是 tick（= 配置秒 × 20），代入后 160t 下限永不生效，五个法术依次为 270 / 330 / 300 / 450 / 420t（`integ/spells/BossSpellPacing.java:22-24`、`integ/spells/BossSpellCombat.java:94`）；施法间隔 60t，开场偏移 60t + `id % 20`（`integ/spells/BossSpellPacing.java:13-14`、`integ/spells/BossSpellCombat.java:77`）。
- 起手门禁与中断：5 个法术按 `NEXT` 指针轮转，必须同时满足「该法术未被配置禁用（`isEnabled`）」与「自身冷却已到」才会起手；没有可用法术时不推进 60t 间隔（等于每 tick 重试）；蓄力中换目标、目标消失或蓄力超过 60t 会取消该次施法（`integ/spells/BossSpellCombat.java:57,81-95`）。
- 施法期间每 4t 画一次半径 4 格的预警环（`integ/spells/BossSpellEffects.java:95-99`）；法术等级上限 1，威力按满级 5 结算（`util/BossSpellRules.java:7-8,148`）。

## 六、召唤物、分身与助战形态

| 单位 | 触发 | 数量 | 关键数值 | 行为与附带 | 出处 |
|---|---|---|---|---|---|
| 小酒桶 `ftboss:barrel_minion` | 召唤技能结算 | 2~3 只 | HP 30 / 攻击 3.0 / 攻速 1.0 / 移速 0.28 / 跟随 28 / 击退抗性 0.5 / 经验 5 | 近战追击；目标进入 5 格点燃 30t 引信自爆，威力 1.2 且不毁地形；跑出 9 格熄灭引信 | `entity/SmallBarrelEntity.java:46-50,54,57-65,86-113` |
| 啤酒人 `brewery:beer_elemental` | 同一次召唤结算 | 最多 1 只 | 属性由 Brewery 模组提供：? | 生成即锁定酒桶王当前目标 | `entity/WineBarrelBossEntity.java:517-529` |
| 召唤物强化 | 生成瞬间 | — | 同步酒桶王当前四系酒 buff（时长与等级一致） | 发光标记 + 自定义名 + 半空 0.5 格放置 | `entity/WineBarrelBossEntity.java:535-554` |
| 分身 `ftboss:wine_barrel_decoy` | 血量 < 50% 时每 600t 判定 | 场上最多 2 个 | HP 1.0 / 攻击 0.0 / 移速 0.0 / 经验 0 | 无 AI、不攻击不移动、不自然消失、无血条，纯诱饵（与本体同一模型）；距本体 ±2 格内随机落点，命名「酒桶王的分身」 | `boss/ai/BossEmpower.java:91-112`、`entity/WineBarrelDecoyEntity.java:31-48` |
| 随主消散 | 酒桶王死亡或被移除 | — | 名单内召唤物直接 `discard()`；再按 80 格白名单（类型 + 认主人过滤）清一次分身与小酒桶 | 不走 `kill()`，避免被限伤拦下留下幽灵召唤物 | `entity/WineBarrelBossEntity.java:694-712`、`boss/ai/SummonSweep.java:29,33-40,48-66` |
| 助战 · 酒桶王 | 柠檬猫（甜苹果庄园第 5 位，`LEMONCAT_VARIANT` = 4）技能「喊帮手」 | 从 5 位食物系 Boss 中不重复抽 2 位（`summonCount()`：献祭用过后只抽 1 位），酒桶王为其中之一 | `MAX_HEALTH` 基础值 +100（`SUMMON_EXTRA_HEALTH`）；从 `CHAIN_BUFF_POOL` 不重复抽 5 个增益（`SUMMON_EXTRA_BUFFS`），等级 `buffAmp(1)` | 寿命 1800t（`SUMMON_LIFE_TICKS` = 90 秒）后随柠檬猫收回；禁播自身 BGM、继承庄园战斗成员标记、命名「…·助战」 | `entity/EasterBossEntity.java:344,348-354,3839-3919`、`boss/ai/LemonSacrifice.java:84-86` |

## 七、减益（醉意等）

| 减益 | 触发 | 时长与等级 | 出处 |
|---|---|---|---|
| `brewery:drunk` | 每 30t 对 7 格内、有视线、非友军 / 非创造 / 非旁观的玩家 | 200t，等级 0 | `entity/WineBarrelBossEntity.java:384-401` |
| `minecraft:confusion`（反胃） | 同上 | 160t，等级 0 | `entity/WineBarrelBossEntity.java:384-401` |
| `brewinandchewin:tipsy`（微醺） | 同上 | 240t，等级 IV（显示 V 级） | `entity/WineBarrelBossEntity.java:384-401` |
| 反胃 | 砸地命中 | 120t，等级 0 | `boss/ai/BarrelTactics.java:83` |
| 缓慢 + 反胃 | 开塞喷射命中 | 缓慢 100t / 反胃 160t，等级 0 | `boss/ai/BarrelTactics.java:111-112` |
| 虚弱 + 反胃 | `ftboss:vintage_vortex` 命中 | 各 100t，等级 0 | `integ/spells/BossSpellEffects.java:161-167` |
| 点燃 | `cask_rupture` / `cinder_breath` / `vintage_minefield` 命中 | 3 秒 | `integ/spells/BossSecondSpellEffects.java:162`、`integ/spells/BossExpandedSpellEffects.java:221,301` |
| 对自身的减益 | 每 20t 清除全部非增益效果 | 事件层全局 `DENY` 已停用，实际只能短暂存在 | `entity/WineBarrelBossEntity.java:265-272` |

## 八、掉落

| 掉落 | 条件 | 数量与概率 | 出处 |
|---|---|---|---|
| 经验 | 被击杀 | 80（`xpReward`） | `entity/WineBarrelBossEntity.java:133` |
| 遗物 `ftboss:aged_wine_seal` | `doMobLoot` 开启且击杀归因到玩家；每只实例只结算一次（必掉） | 必掉（`DROP_CHANCE` = 1.0），随机 4 种词条（`AFFIXES`）之一 | `util/BossRelicRules.java:6,9,15,46-48`、`skill/BossRelicDrops.java:52-62` |
| 招牌法术卷轴 | 同上，每只实例只结算一次 | 5 张：`vintage_vortex` / `cask_rupture` / `cinder_breath` / `intoxicating_toast` / `vintage_minefield` 各 1（`isEnabled()` 为假的法术不发） | `integ/spells/BossSpellDrops.java:45-54` |
| 石像 `ftboss:statue_wine_barrel` | Boss 死亡时（不检查 `doMobLoot`，也不要求玩家击杀；怜悯离场不掉） | 1 | `boss/ai/BossMercy.java:140-144,329-356` |
| 手中酒桶弩 | 任何情况 | 0（掉落率被设为 0） | `entity/WineBarrelBossEntity.java:337` |
| 小酒桶 | 被打死 | 经验 5，无掉落物（自爆是 `discard`，连经验也不给） | `entity/SmallBarrelEntity.java:54,104-113` |
| 连死怜悯离场 | 同一玩家在同一只酒桶王下连续死亡 5 次（`LIMIT` = 5；`STREAK_TIMEOUT` = 12000t 内计数，超时归零） | 无掉落、无经验；本体与其召唤物一起离场 | `boss/ai/BossMercy.java:44,116-124,281-323` |
