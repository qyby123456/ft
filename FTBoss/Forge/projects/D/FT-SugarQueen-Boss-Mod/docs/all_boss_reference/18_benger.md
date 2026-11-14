# 边牧犬王

常规 Boss 第 9 只 `ftboss:benger`。所有伤害均取代码字面常量；`boss/ai/NonManorDamageX2.java:43` 会把**它造成的所有伤害 ×2**，故下表同时给出结算值。时间单位 tick（1 秒 = 20 tick），距离单位格。查不到的数字写 `?`。

## 一、实体与判定箱

| 项目 | 值 | 出处 |
|---|---|---|
| 实体 id | `ftboss:benger` | `entity/FTEntities.java:172` |
| 分类与判定箱 | `MobCategory.MONSTER`，宽 1.7 × 高 3.5 格 | `entity/FTEntities.java:173` |
| 客户端追踪范围 | 10 | `entity/FTEntities.java:190` |
| 客户端渲染 | `geo/benger.geo.json` + `animations/benger.animation.json` + `textures/entity/benger.png`，阴影半径 1.0（`shadowRadius` 构造参数）；虚影期间渲染管线换成 `entityTranslucent`（半透明） | `client/FTClientSetup.java:141`、`client/render/PathGeoRenderer.java:14`、`client/render/BengerRenderer.java:30` |
| 召唤物 | `ftboss:benger_whistle`（牧羊哨，`stacksTo(1)`）：右键在主世界玩家朝向 4 格外生成，非创造模式消耗 1 个 | `item/FTBossItems.java:124`、`item/BengerWhistleItem.java:42` |
| 刷怪蛋 | `ftboss:benger_spawn_egg`（主色 0x2A2A2E、副色 0xEDEAE2），同样只允许主世界使用 | `item/FTBossItems.java:128`、`item/OverworldBossSpawnEggItem.java:30` |
| 召唤维度限制 | `SummonGate.OVERWORLD_ONLY = true`；非主世界召唤失败且不消耗召唤物 | `boss/SummonGate.java:25` |
| 专属 BGM | `ftboss:boss_music_benger`《牧野犬王》，`BENGER_TICKS` = 3708 tick | `sound/BossMusic.java:108` |
| 音效 | 环境音借原版 `WOLF_GROWL`，受伤 `WOLF_HURT`，死亡 `WOLF_DEATH`；音量 1.3、音高 0.72 | `entity/BengerEntity.java:233` |
| 持久化 | `setPersistenceRequired()`，且 `removeWhenFarAway` 恒为 false（不因远离消失） | `entity/BengerEntity.java:189`、`:240` |

## 二、基础属性

| 属性 | 值 | 出处 |
|---|---|---|
| `MAX_HP`（最大生命） | 330.0 | `entity/BengerEntity.java:74` |
| `ATTACK_DAMAGE` | 30.0（×2 后 60.0） | `entity/BengerEntity.java:201` |
| `MOVEMENT_SPEED` | 属性基准 0.1（`BossWalkingSpeed.BASE_SPEED`）；普通陆地寻路结果再 ×2 | `entity/BengerEntity.java:202`、`boss/ai/BossWalkingSpeed.java:24`、`:74` |
| `ARMOR` | 4.0 | `entity/BengerEntity.java:203` |
| `ARMOR_TOUGHNESS` | 4.0 | `entity/BengerEntity.java:204` |
| `KNOCKBACK_RESISTANCE` | 0.85（`BOSS_RESISTANCE`，只吃 15% 击退） | `entity/BengerEntity.java:205`、`util/BossKnockback.java:31` |
| `FOLLOW_RANGE` | 48.0 | `entity/BengerEntity.java:206` |
| 经验 `xpReward` | 0（战利品由 `BossMercy` 统一发） | `entity/BengerEntity.java:188` |
| 目标 AI | `NearestAttackableTargetGoal(Player, mustSee=true)` | `entity/BengerEntity.java:228` |
| 移动 AI | `FloatGoal` + `MeleeAttackGoal` 速度修正 1.25 + `WaterAvoidingRandomStrollGoal` 1.0 + `LookAtPlayerGoal` 24 格 + `RandomLookAroundGoal` | `entity/BengerEntity.java:223` |
| 动作时长常量 | `T_BITE` = 10、`T_POUNCE` = 14、`T_ROAR` = 20、`T_HURT` = 8、`T_DEATH` = 24 | `entity/BengerEntity.java:77` |

## 三、通用战斗机制与减益

| 机制 | 数值与行为 | 出处 |
|---|---|---|
| 全局伤害倍率 | 它造成的**一切**伤害 ×2.0（`ftboss` 命名空间且非庄园彩蛋 Boss） | `boss/ai/NonManorDamageX2.java:43` |
| 全局最终伤害 300 规则 | 结算后的实际扣血伤害 > `MAX_FINAL_DAMAGE` = 300.0 时本次血量伤害归零（不是截断为 300；恰好 300 或更低照常放行；不按原始伤害判断）；`/kill` 这类管理员伤害保留，NaN / 无穷最终伤害直接屏蔽；原始 500 点规则与旧 `LIMIT_ENABLED` 限伤继续停用；本 Boss 适用，玩家、外部 Boss、鼠群小怪、迷你 Boss、召唤物不适用 | `boss/ai/FinalBossDamageGuard.java:19`、`mixin/FinalBossDamageMixin.java:14` |
| 逐 Boss 限伤体系 | `LIMIT_ENABLED = false`，故单次伤害上限、连击递减、反逃课距离全部不生效 | `boss/ai/BossDamageGate.java:46` |
| 流血与真实伤害免疫 | 伤害类型路径或提示含 hemorrhage、blood_loss、bleed、doom、true_damage 等关键字时完全免伤并提示 | `boss/ai/BossDamageGate.java:120` |
| 负面效果（不再免疫） | 事件层全局 `DENY` 已停用（`IMMUNE_TO_DEBUFFS` = false），本实体没有周期性清减益逻辑，外来减益会正常生效并持续；只有喝奶时先移除旧的 `armamentarium:damon_effect` 再刷新 | `boss/BossHandler.java:409`、`entity/BengerEntity.java:696-705` |
| 虚影（不可选中） | 每 `INTANG_CD` = 500 tick（25 秒）自动触发一次，持续 `INTANG_TICKS` = 60 tick（3 秒）；进入时 `setInvulnerable(true)`、清空自身目标，并解除 64 格内把它当目标的生物索敌；期间 `isInvulnerableTo` 对一切非穿透伤害恒真（`/kill`、虚空这类绕过无敌的伤害照常生效）；结束时解除无敌并显形 | `entity/BengerEntity.java:171`、`:724`、`:741`、`:787` |
| 虚影的客户端同步 | 服务端把虚影标记写进 `DATA_ACTION` 的 `bengerIntangible`，客户端 `isIntangible()` 读该标记；渲染管线切到 `entityTranslucent`，颜色染成青蓝（0.55, 0.85, 1.0），透明度按 sin((tickCount + partialTick) × 0.45) 在 0.20 至 0.36 之间脉动 | `entity/BengerEntity.java:763`、`:758`；`client/render/BengerRenderer.java:31`、`:39` |
| 虚影的粒子与音效 | 虚影期间每 tick 12 点魂火（`SOUL_FIRE_FLAME`，半径 1.35 ± 0.12 格、高度 0.3 至 1.6 格）绕身旋转，另加 6 点逆向传送粒子；进入时 40 点逆向传送粒子 + 传送音效，期间每 10 tick 一次传送音效，结束播 FLASH、24 点魂火与狼嗥 | `entity/BengerEntity.java:729`、`:735`、`:751`、`:779` |
| 通用狂暴档 | 血量 ≤ 50% → 狂暴 I（速度 I + 抗性 I）；≤ 25% → 追加力量 I + 再生 I；每 20 tick 刷新，单次时长 80 tick | `boss/ai/BossRage.java:40`、`:85` |
| 防卡瞬移 | 每 80 tick 检查一次，水平位移 < 2.0 格且距目标 > 4.0 格 → 传送到目标周围 2.0 至 5.5 格的安全点 | `boss/ai/BossUnstuck.java:27` |
| 破困 | 每 10 tick 清掉卡在身上的矿车、船、坐垫/座椅类方块与蜘蛛网、细雪 | `boss/ai/TrapBreaker.java:60` |
| 受击动作 | 受击结算成功且当前无动作时，60% 概率播 `T_HURT` = 8 tick 受击动画 | `entity/BengerEntity.java:960` |
| 血条 | `ServerBossEvent`，颜色 WHITE、样式 PROGRESS，每 tick 更新为 health / maxHealth；玩家首次看见时加入并登记 `BossBars` | `entity/BengerEntity.java:144`、`:249` |
| 流血 `ftboss:bleed` | 每层每秒 3.0 点魔法伤害（穿甲，不吃护甲值），每 20 tick 结算一次；默认时长 8 秒 | `effect/BleedEffect.java:23`、`:41` |
| 流血叠层 | 本体期望上限：一阶段等级 ≤ 4（5 层）、二阶段等级 ≤ 9（10 层）；但 `ExtraDebuffs.bleed` 会把等级夹到 ≤ 4，故实际最高 5 层 = 15.0 点每秒 | `entity/BengerEntity.java:645`、`boss/ai/ExtraDebuffs.java:41` |
| 流血回流 | 玩家身上流血每次结算后，把该次伤害的 50% 回给登记源（登记有效期 5 秒） | `effect/BleedEffect.java:52`、`boss/ai/BleedLink.java:31` |
| 回流归因 | 本体挂的流血回流给自己；`DogPack` 的狗挂的流血登记源是那只狗本身（回流给狗）；只有 `benger_pup` 会就近归到 64 格内的 benger，而它当前不生成 | `boss/ai/BleedLink.java:44`、`boss/ai/DogPack.java:297` |
| 震颤 `ftboss:tremor` | 连续下砸每命中一次叠 1 层，每层 200 tick；叠到 `STUN_AT` = 10 层立刻眩晕 20 tick（缓慢等级 250 + 跳跃等级 128 + 反胃 40 tick）并清零 | `effect/TremorEffect.java:23`、`:51` |
| 眩晕（锚点硬定身） | 记下触发瞬间的锚点，每 tick 把玩家传送回锚点（每 5 tick 出粒子）；大狗叫 50 tick、冲撞 40 tick | `entity/BengerEntity.java:925`、`:930` |

## 四、主技能

技能轮转一次只做一件事，判定顺序即下表自上而下：追猎突击最先检查，其余依次为大狗叫、野生狗奶、狼群扑击、连续下砸、撕咬钳制、打狗棒横扫、单独长嗥、低吼（`entity/BengerEntity.java:336`、`:342`、`:349`、`:355`、`:360`、`:365`、`:370`、`:378`、`:384`）。

| 技能 | 触发与冷却 | 前摇 | 范围 | 伤害 | 附带 | 出处 |
|---|---|---|---|---|---|---|
| 追猎突击 | 优先级最高；`chargeCd` ≤ 0 且 `CHARGE_MIN` = 3.5 格 ≤ 距离 ≤ `CHARGE_MAX` = 22.0 格且有视线；冷却 `CHARGE_CD` = 380 tick（初始 200） | 先 8 tick 方向预警（沿锁定方向每 2 tick 铺 12 点橙色 Dust 直线 + CRIT 粒子，期间定身），随后冲刺 16 tick；冲刺姿态借 `ACT_LEAP` 飞扑动画（动作计时 24 tick） | 冲刺途中沿位移方向外扩后膨胀 0.65 / 0.3 / 0.65 格的扫掠判定箱内的玩家，每次冲撞只结算 1 次；创造与旁观玩家跳过 | `CHARGE_DAMAGE` = 34.0（×2 → 68.0） | 推（方向 × 1.4，0.45）+ 眩晕 `CHARGE_STUN` = 40 tick；冲刺速度 1.6 格每 tick；撞到方块立即停止并结束本次冲撞 | `entity/BengerEntity.java:109`、`:336`、`:834`、`:853` |
| 大狗叫！（`ACT_BARK`） | `barkCd` ≤ 0 且距目标 ≤ 14 格；冷却 400 tick（初始 200） | 20 tick 动作，第 8 tick 结算 | 判定箱外扩 14 格内全部生物（排除自己与 `benger_pup`，含它自己召来的狗） | 玩家 8.0（×2 → 16.0）；非玩家生物无伤害 | 玩家锚点硬定身 50 tick；非玩家生物缓慢 50 tick 等级 9 + 虚弱 50 tick 等级 2 + 水平速度清零；同时召 2 只狗（二阶段 4 只） | `entity/BengerEntity.java:342`、`:667` |
| 野生狗奶（`ACT_MILK`） | `milkCd` ≤ 0 且（血量 < 70% 或每 tick 25% 概率）；冷却 560 tick（初始 320） | 20 tick 动作，第 8 tick 结算 | 自身 | 无 | 吸收 IV 400 tick、生命恢复 II 600 tick、抗火 1200 tick、抗性 II 400 tick、`armamentarium:damon_effect`（血王子 IV）400 tick 等级 3；同时立刻进入虚影（`INTANG_TICKS` = 60 tick，含无敌、清空目标与解除 64 格内索敌） | `entity/BengerEntity.java:349`、`:691` |
| 狼群扑击（起手 `ACT_ROAR`、扑击 `ACT_LEAP`） | `packCd` ≤ 0 且距目标 ≤ 20 格且有视线；冷却 280 tick（初始 120） | 起手长嗥 20 tick；自起手起 `PACK_LEAD` = 30 tick 后起飞（即长嗥结束后再等 10 tick），起飞后 6 tick 落地结算 | 落点半径 3.5 格 | 36.0（×2 → 72.0） | 流血 8 秒 + `knockback(1.0, jumpX, jumpZ)`；起跳速度（`jumpX` × 1.05，0.55，`jumpZ` × 1.05）。方向量 `jumpX` / `jumpZ` 只在不可达的 `lockPack` 里赋值，故实际恒为 0.0：这一扑只有竖直位移，也没有水平击退 | `entity/BengerEntity.java:339`、`:457`、`:468`、`:473` |
| 连续下砸（`ACT_SLAM`） | `slamCd` ≤ 0 且距目标 ≤ 18 格且有视线；冷却 340 tick（初始 240） | 每次腾空滞空 `SLAM_HANG` = 8 tick，砸落即时结算，两次之间间隔 `SLAM_GAP` = 22 tick | 落点半径 4.0 格，共 `SLAM_COUNT` = 3 次 | 39.0 每次（×2 → 78.0） | 缓慢 80 tick 等级 1 + 震颤 1 层 + 向上推 0.55；滞空期间每 tick 用玩家当前位置刷新落点并画 24 点地面预警圈 | `entity/BengerEntity.java:344`、`:504` |
| 撕咬钳制 | `clampCd` ≤ 0 且距目标 ≤ 5.5 格且目标是玩家；近战咬中有 35% 概率直接转入；冷却 320 tick（初始 280） | 无，立即咬住（同时播 10 tick 咬击动作，本招不再走横扫结算） | 咬住后玩家距本体 > 6.0 格或死亡、旁观即中断 | 每秒 20.0（×2 → 40.0），持续 `CLAMP_TICKS` = 60 tick | 每 tick 把玩家硬传送回咬住瞬间的锚点；每次结算叠流血 6 秒；本体未满血时每秒回 `CLAMP_HEAL` = 16.0 | `entity/BengerEntity.java:349`、`:771` |
| 打狗棒横扫（`ACT_SWEEP`） | `sweepCd` ≤ 0 且距目标 ≤ 5.5 格；冷却 220 tick（初始 120） | 10 tick 动作，第 6 tick 结算 | 半径 4.2 格 | 33.0（×2 → 66.0） | 缓慢 100 tick 等级 0 + 击退强度 1.3 | `entity/BengerEntity.java:359`、`:563` |
| 单独长嗥（`ACT_ROAR`） | `roarCd` ≤ 0 且距目标 ≤ 14 格；冷却 320 tick（初始 200） | 20 tick 动作，第 8 tick 结算 | 半径 14 格 | 无 | 本体获速度 II 160 tick + 抗性 I 160 tick；范围内玩家缓慢 II 120 tick + 虚弱 I 120 tick + 击退 0.7 | `entity/BengerEntity.java:367`、`:578` |
| 近战撕咬（`MeleeAttackGoal`） | 普通追击近战，无独立冷却 | 命中即结算，之后播 10 tick 咬击动作 | 本体攻击距离 | `ATTACK_DAMAGE` = 30.0（×2 → 60.0） | 流血 8 秒（按层累加）+ 缓慢 60 tick 等级 0 + 击退 0.8；35% 概率改为触发撕咬钳制（需其冷却已好） | `entity/BengerEntity.java:880` |
| 低吼（氛围） | `growlCd` ≤ 0 且每 tick 2% 概率；触发后冷却 160 tick | 无 | 自身 | 无 | 播放 `WOLF_GROWL`（音量 1.2、音高 0.7） | `entity/BengerEntity.java:373` |

- 追猎突击与撕咬钳制进行中以 `chargeTicks` / `clampTicks` 直接挡住技能轮转，其它招式不会插入；冲撞的命中判定只在 `chargeFired` 归零时结算一次，撞到方块后立即 `finishCharge()` 收招（`entity/BengerEntity.java:321`、`:883`、`:894`、`:911`）。
- 狼群扑击的起手与单独长嗥共用 `ACT_ROAR` 结算分支，因此只要 `roarFired` 归零，起手也会附带长嗥的自身增益与玩家减益（`entity/BengerEntity.java:417`）。
- 狼群扑击中「长嗥召狗 + 全体一起扑」那一段写在 `case ACT_PACK` 分支里，但代码中从未给 `act` 赋过 `ACT_PACK`；该分支及其调用的 `lockPack`、`DogPack.launchAll` 当前不会执行，狗只来自大狗叫、二阶段与自身狂暴三处（`entity/BengerEntity.java:382`、`:136`）。同一原因使 `jumpX` / `jumpZ` 永不赋值（`lockSelf` 只被 `lockPack` 调用）：起跳只剩竖直的 0.55 分量，落地 `knockback` 也没有水平方向（`entity/BengerEntity.java:444`、`:457`、`:483`）。
- 连续下砸与狼群扑击用 `teleportTo` 直接搬到落点，砸落时把 `fallDistance` 清零，因此落地本身没有摔落伤害（`entity/BengerEntity.java:524`）。

## 五、阶段与转换

| 阶段 / 事件 | 触发条件 | 效果 | 出处 |
|---|---|---|---|
| 二阶段 | 血量 < 最大生命 50% 且当前为阶段 1，只触发一次 | 立即召 4 只狗；移速 +0.05（ADDITION 修饰符 `ftboss.benger_phase2`）；ActionBar 播报 + 狼嗥音效 | `entity/BengerEntity.java:284` |
| 自身狂暴 | 血量 < 最大生命 40% 且 NBT `bengerRaged` 未置位，只触发一次 | 移速 +0.07（`ftboss.benger_rage`）；召 3 只狗（已是二阶段则 6 只）；铃声 + 狼嗥；标记写档，读档不重复触发 | `entity/BengerEntity.java:299`、`:1028` |
| 狂暴 I（通用档） | 血量 ≤ 50% | 速度 I + 抗性 I（每 20 tick 刷新，单次 80 tick）+ 咆哮音效与红色粒子，无聊天播报 | `boss/ai/BossRage.java:40` |
| 狂暴 II（通用档） | 血量 ≤ 25% | 在狂暴 I 基础上追加力量 I + 再生 I | `boss/ai/BossRage.java:90` |
| 致死 | 生命归零 | 动作切 `ACT_DEATH`（24 tick 死亡动画）、注销血条、释放 BGM、交由 `BossMercy` 掉落 | `entity/BengerEntity.java:1006` |
| 连死怜悯 | 同一玩家在同一只 benger 下连续死亡 5 次；计数按 Boss 实例记，且带时间戳（超过 `STREAK_TIMEOUT` = 12000 tick 即 10 分钟未再死亡就过期归零） | 该 benger 直接 `discard()` 离场，无掉落、无经验 | `boss/ai/BossMercy.java:44`、`boss/ai/BossMercyData.java:34` |

## 六、召唤物与随从

| 对象 | 项目 | 值 | 出处 |
|---|---|---|---|
| 狗群 | 生成入口 | `summonPups` → `DogPack.summon`；大狗叫召 2 只（二阶段 4 只）、二阶段召 4 只、自身狂暴召 3 只（二阶段 6 只）；每次召唤都按 `CAP` − 场上存活数截断 | `entity/BengerEntity.java:606`、`boss/ai/DogPack.java:163` |
| 狗群 | 名单判定 | 扫实体注册表：注册名含 wolf、dog、hound、corgi、husky、akita、poodle、beagle、terrier、retriever、collie、dachshund、chihuahua、samoyed、malamute 之一；排除 shiba，排除 `ftboss` 命名空间；只收 CREATURE 或 MONSTER 且宽 ≤ 2.0、高 ≤ 2.5 格；扫不到时兜底 `minecraft:wolf` 与 `quark:foxhound` | `boss/ai/DogPack.java:78`、`:130`、`:138` |
| 狗群 | 数量上限与寿命 | `DogPack.CAP` = 12；每只存活 `DogPack.LIFE` = 1200 tick（60 秒）后自动 `discard`（出生刻写在持久数据 `ftboss_pack_born` 里，每 20 tick 检查一次，读档后继续计时） | `boss/ai/DogPack.java:69`、`:174`、`:274` |
| 狗群 | 生成方式 | 以本体为中心 2.6 格半径随机角度生成，POOF 粒子 + 紫水晶音效，打持久标记 `ftboss_pack` | `boss/ai/DogPack.java:170` |
| 狗群 | 索敌 | 生成时锁定 64 格内最近玩家（原版狼额外写怒气目标）；每 20 tick 重锁，每 40 tick 强制重锁 | `boss/ai/DogPack.java:176`、`:279` |
| 狗群 | 命中附加 | 由 `LivingHurtEvent` 统一挂钩（投射物按主人归因）：流血 5 秒（等级 0）+ `armamentarium:vulnerable` 200 tick + `clipped` 200 tick + 缓慢 30 tick | `boss/ai/DogPack.java:289`、`:293` |
| 狗群 | 近战伤害 | 取各自实体的 `ATTACK_DAMAGE`，狗种不定故为 `?`；召来的都是原版或外模组实体，不满足 `ftboss` 命名空间，故不吃 ×2 倍率 | `boss/ai/DogPack.java:38`、`boss/ai/NonManorDamageX2.java:38` |
| 狗群 | 不可驯服 | 右键交互被取消并提示「这批野狗是 benger 唤来的 —— 不能被驯服。」 | `boss/ai/DogPack.java:320` |
| 狗群 | 集体飞扑 | `DogPack.launchAll` 仅被 `BengerEntity.lockPack` 调用，而 `lockPack` 所在分支不可达，故当前不会触发；机制参数为 48 格内全体以（方向 × 1.0，0.5）抛出 | `boss/ai/DogPack.java:227`、`:253` |
| 小狗 `ftboss:benger_pup` | 判定箱与属性 | 0.9 × 2.0 格；`MAX_HP` = 30.0、`ATTACK_DAMAGE` = 6.0、`MOVEMENT_SPEED` = 0.50、`FOLLOW_RANGE` = 52.0 | `entity/FTEntities.java:176`、`entity/BengerPupEntity.java:82` |
| 小狗 | 渲染与音效 | 与本体共用 `geo/benger.geo.json` + `animations/benger.animation.json` + `textures/entity/benger.png`，整体缩到 0.55 倍、阴影半径 0.35；环境音 `WOLF_PANT`、受伤 `WOLF_HURT`、死亡 `WOLF_DEATH`，音量 0.7、音高 1.35 | `client/FTClientSetup.java:143`、`:147`、`entity/BengerPupEntity.java:100`、`:103` |
| 小狗 | 存活与掉落 | `LIFE` = 900 tick（45 秒）后 `discard`；`xpReward` = 0，无掉落；`setPersistenceRequired()` 且 `removeWhenFarAway` 恒为 false | `entity/BengerPupEntity.java:43`、`:78`、`:79`、`:107` |
| 小狗 | AI 与受击 | `FloatGoal` + `MeleeAttackGoal` 1.6 + `LookAtPlayerGoal` 16 格 + `NearestAttackableTargetGoal(Player)`；伤害只认玩家：`source.getEntity()` 是玩家以外的实体时直接免伤，无来源的环境伤害（摔落、火焰等）不在此限；受击 70% 概率播 8 tick 受击动画 | `entity/BengerPupEntity.java:93`、`:151`、`:154` |
| 小狗 | 命中附加 | 流血 5 秒（等级 0）+ 缓慢 30 tick + `vulnerable` 200 tick + `clipped` 200 tick | `entity/BengerPupEntity.java:160` |
| 小狗 | 扑击（当前不可达） | `packPounce(delay, radius, damage)`：到点以（方向 × 1.0，0.5）起飞，8 tick 后落地；落地伤害 = 传入伤害 × 0.5（本体传 36.0 时为 18.0）+ 流血 4 秒；字段初值半径 3.5、伤害 24.0，实际被调用时由传入值覆盖；唯一调用方 `lockPack` 不可达，故不会执行 | `entity/BengerPupEntity.java:58`、`:62`、`:120`、`:139` |
| 小狗 | 当前是否生成 | 不生成：`summonPups` 在 `entity/BengerEntity.java:614` 处 `if (true) return;`，其后的 pup 生成分支不可达；另有未被任何代码引用的常量 `PUP_CAP` = 8 | `entity/BengerEntity.java:614`、`:140` |

## 七、掉落与结算

| 掉落物 | 数量与概率 | 条件 | 出处 |
|---|---|---|---|
| 打狗棒 `ftboss:dog_beating_stick` | 1 件 | Boss 被击败时由 `BossMercy.giveWeaponDrop` 直接生成，任何死亡路径都发 | `boss/ai/BossMercy.java:389` |
| 遗物 `ftboss:shepherd_whistle_charm` | 必掉（`DROP_CHANCE` = 1.0） | 需由玩家击杀（击杀者或 `killCredit` 为 `ServerPlayer`）、`doMobLoot` 开启、每只 Boss 只结算一次 | `skill/BossRelicDrops.java:52`、`util/BossRelicRules.java:6`、`:20` |
| 遗物属性与词条 | 基础：移速 +0.08（MULTIPLY_BASE）、攻击力 +1（ADDITION）；词条 4 选 1 随机：最大生命 +4、护甲 +2、移速 +0.05、幸运 +1 | 装备槽为 `boss_relic` | `item/BossRelicItem.java:58` |
| 石像 | — | 不掉：`giveStatue` 的名单里没有 benger | `boss/ai/BossMercy.java:329` |
| 经验 | 0 | `xpReward` = 0 | `entity/BengerEntity.java:186` |
| 狗群 | 按各自原版或模组战利品表 | 不属于 Boss 掉落 | `boss/ai/DogPack.java:37` |
| 小狗 `ftboss:benger_pup` | 无 | `xpReward` = 0，无掉落表 | `entity/BengerPupEntity.java:78` |
| 连死怜悯 | 无任何掉落与经验 | 同一玩家在同一只 benger 下连续死亡 5 次 → 该实体 `discard()` | `boss/ai/BossMercy.java:44` |
