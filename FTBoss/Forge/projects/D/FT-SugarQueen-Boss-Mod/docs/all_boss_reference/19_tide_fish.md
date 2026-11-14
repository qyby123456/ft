# 潮宴鱼王·沧鳞

常规 Boss 第 10 只，实体 id 为 `ftboss:tide_fish_boss`（`entity/FTEntities.java` 第 25~26 行）。技能节奏与全部数值集中在 `util/TideFishCombatRules.java`，本体行为在 `entity/TideFishBossEntity.java`。下文时间一律 tick（1 秒 = 20 tick），距离一律「格」；技能表的伤害列给的是代码字面常量，实际结算还要乘全局倍率（见第二节末尾的伤害结算说明）。

## 一、实体与召唤

| 项目 | 值 | 出处 |
|---|---|---|
| 实体 id | `ftboss:tide_fish_boss` | `entity/FTEntities.java` 第 25~26 行 |
| 判定箱 | 宽 2.4 格、高 3.5 格，`MobCategory.MONSTER` | `entity/FTEntities.java` 第 26、181 行 |
| 基类 | `Monster`，GeckoLib 动画实体 | `entity/TideFishBossEntity.java` 第 48 行 |
| 召唤物 | `ftboss:tide_banquet`（深海御膳）右键使用 | `item/TideBanquetItem.java` 第 26~31 行；`assets/ftboss/lang/zh_cn.json` 第 334 行 |
| 召唤落点 | 朝向正前方 5 格；竖直偏移依次尝试 0、1、-1、2、-2；要求下方有碰撞或流体、无碰撞、区块已载、在世界边界内 | `item/TideBanquetItem.java` 第 32~43 行 |
| 维度限制 | 仅主世界（`SummonGate.OVERWORLD_ONLY` = true），其它维度中止召唤 | `boss/SummonGate.java` 第 25、30~33 行 |
| 消耗与冷却 | 非创造模式消耗 1 个，物品进入 40 tick 冷却；落点失败提示 `message.ftboss.tide_no_room` 且不消耗 | `item/TideBanquetItem.java` 第 44~49 行 |
| 出场音效 | `CONDUIT_ACTIVATE`，音量 1.5、音调 0.6 | `item/TideBanquetItem.java` 第 50 行 |

## 二、基础属性

| 属性 / 参数 | 值 | 出处 |
|---|---|---|
| `MAX_HEALTH` | 480.0 | `entity/TideFishBossEntity.java` 第 51 行 |
| `ATTACK_DAMAGE` | 10.0 | 同文件第 93 行 |
| `ARMOR` | 4.0 | 同文件第 93 行 |
| `MOVEMENT_SPEED` | 0.1（`BossWalkingSpeed.BASE_SPEED`）；普通陆地寻路结果再 ×2 | 同文件第 94 行；`boss/ai/BossWalkingSpeed.java` 第 24、74 行 |
| `FOLLOW_RANGE` | 40.0 格 | 同文件第 94 行 |
| `ATTACK_SPEED` | 5.6 | 同文件第 95 行 |
| `KNOCKBACK_RESISTANCE` | 0.85（`BOSS_RESISTANCE`，只吃 15% 击退） | 同文件第 96 行；`util/BossKnockback.java` 第 31 行 |
| 近战 | 不使用：`registerGoals` 只注册 `FloatGoal`，没有任何近战 goal，全部伤害来自技能 | 同文件第 87~89 行 |
| 索敌 | 目标无效或距离超过 40 格（`distanceToSqr` > 1600）时，重新取 40 格内最近的有效玩家；创造与旁观玩家不算目标 | 同文件第 141~144 行；`boss/ai/BossTargets.java` 第 31~33 行 |
| 追击倍率 | 与目标距离大于约 3.16 格（`distanceToSqr` > 10）时每 10 tick 追击一次，移速修饰符 1.3125（一阶段）、1.5625（二阶段）；否则停下 | 同文件第 162~164 行 |
| 出手条件 | 冷却归零且目标在视线内（`hasLineOfSight`） | 同文件第 165 行 |
| 经验 | `xpReward` = 160 | 同文件第 73 行 |
| 免疫与状态 | 免疫同类 Boss 与自家小怪造成的伤害；`canBeAffected` 只接受有益效果（免疫全部负面效果）；可在水下呼吸；不因远离玩家消失并强制持久化 | 同文件第 74、101、104~106、109、113 行 |

- 全局血量与攻击力缩放：Boss 周围 `RADIUS` = 48.0 格内每多 1 名有效玩家（非创造、非旁观），最大生命 × `HP_PER_PLAYER`（1.25）的 (n-1) 次方、攻击力 × (1 + `DMG_PER_PLAYER` 0.15 × (n - 1))，每 20 tick 重算一次（`boss/ai/BossScaling.java` 第 22~26、44~74 行；由 `boss/BossHandler.java` 第 47~52 行对已登记血条的 Boss 驱动）。
- 技能伤害统一在命中时过 `BossScaling.dmg(this, damage)`（`entity/TideFishBossEntity.java` 第 234、347 行），即：实际伤害 = 表中字面值 × 2.0 × `BossScaling` 伤害倍率（单人时该倍率为 1.0；本节表格里的伤害都是单人、未乘 2 的字面值）。
- 其中的固定 ×2 来自 `boss/ai/NonManorDamageX2.java` 第 38~43 行：本模组非庄园 Boss（含沧鳞本体、其潮骨水刃与小怪）造成的所有伤害在 `LivingHurtEvent` 里再乘 2.0。
- 小怪与玩家侧的攻击不走 `BossScaling.dmg`，但仍吃上面这条 ×2：例如盐渍河豚爆刺的字面值 14 点实际结算为 28 点（本文表格里的小怪伤害均为字面值）。
- 全局最终伤害 300 规则：结算后的实际扣血伤害 > `MAX_FINAL_DAMAGE` = 300.0 时本次血量伤害归零（不是截断为 300；恰好 300 或更低放行；不按原始伤害判断；`/kill` 这类管理员伤害保留）；原始 500 点规则与旧 `LIMIT_ENABLED` 限伤继续停用；本 Boss 适用，玩家、外部 Boss、鼠群小怪、迷你 Boss、召唤物不适用（`boss/ai/FinalBossDamageGuard.java` 第 19 行、`mixin/FinalBossDamageMixin.java` 第 14 行）。

## 三、技能总表

7 个技能按 `Skill.values()` 顺序循环：`sweep`、`volley`、`dash`、`summon`、`wave`、`maelstrom`、`geyser`（`util/TideFishCombatRules.java` 第 13~19、41 行）。中文名取自 `assets/ftboss/lang/zh_cn.json`：摆尾分潮 / 鱼骨齐射 / 破浪冲盘 / 御膳开席 / 翻海浪环 / 沸海涡流 / 潮柱裁决（第 342~348 行，`geyser` 在第 383 行）。前摇即 `windup`，代码保证它等于该技能第一段伤害帧。

### 技能数值

| 技能 | 前摇 tick | 持续 tick 一阶段 | 持续 tick 二阶段 | 伤害 一阶段 | 伤害 二阶段 |
|---|---|---|---|---|---|
| `sweep` 摆尾分潮 | 4 | 19 | 29 | 24 | 32 |
| `volley` 鱼骨齐射 | 5 | 22 | 31 | 16 | 20 |
| `dash` 破浪冲盘 | 5 | 25 | 44 | 32 | 40 |
| `summon` 御膳开席 | 6 | 25 | 25 | 0 | 0 |
| `wave` 翻海浪环 | 6 | 31 | 47 | 28 | 36 |
| `maelstrom` 沸海涡流 | 8 | 31 | 41 | 36 | 44 |
| `geyser` 潮柱裁决 | 7 | 30 | 41 | 32 | 40 |

出处：`util/TideFishCombatRules.java` 第 13~19 行（前摇、两套时长、两套伤害），第 37~38 行（二阶段取第二套值）。

### 技能判定与附带

| 技能 | 判定帧 elapsed | 范围或线路 | 附带效果 |
|---|---|---|---|
| `sweep` | 一阶段 4；二阶段 4 与 15 | 以自身为心，半径 4.5 格（一阶段）、5.5 格（二阶段），竖向容差 2.5 格；第 1 段只打锁定方向正面约正负 96 度（`dot` 小于 -0.1 的目标被排除），第 2 段无方向限制 | 缓慢 I 60 tick；水平推出 0.45 格、竖直 0.15 格 |
| `volley` | 一阶段 5；二阶段 5、12、19 | 每波 5 发（二阶段 7 发）潮骨水刃 `ftboss:tide_bolt`，从眼睛高度射出、瞄准起手锁定的目标位置上方 1 格，左右各偏 0.14 弧度（5 发覆盖 ±0.28 弧度），速度 1.3 格每 tick、无重力、存在 60 tick 后消失，弹体判定箱 0.25 × 0.25 格 | 命中施加缓慢 I 50 tick；撞到方块或实体即消失并溅水花 |
| `dash` | 一阶段 5~17；二阶段 5~17 与 31~43 | 每 tick 沿锁定方向位移 0.92 格（二阶段 1.05 格），路径上半径 2.6 格内判定；二阶段第 19 tick 掉头（方向取反、清空去重、解除阻挡）；前方不可通行则当次冲撞立即停住 | 冲撞判定不做去重（同一次冲撞可重复结算，受原版受击无敌帧限制）；缓慢与击退同 `sweep`；每次成功位移都画一圈半径 1.4 格水花 |
| `summon` | 6 | 无伤害判定 | 见第六节 |
| `wave` | 一阶段 6~23；二阶段 6~23 与 25~42（连放两轮） | 从起手位置向外扩的水环：半径 = 1 + age × 0.6 格（age 0~17，最大 11.2 格），只命中与当前环半径差值小于 0.8 格、且与本体高度差小于 0.75 格的玩家；每 2 tick 画一圈水花（`elapsed % 2 == 0`） | 每名玩家每轮只结算一次；缓慢与击退同 `sweep` |
| `maelstrom` | 一阶段 8；二阶段 8、15、22 | 以起手锁定的目标位置为中心：一阶段 1 个、二阶段 3 个漩涡，落点沿侧向相隔 4.5 格（0、+4.5、-4.5）；每处判定半径 3.5 格 | 每段结算一次（去重）；缓慢与击退同 `sweep`；每个落点从第 8 + 7 × 序号 tick 起 12 tick 内收缩水花（半径 3.5 → 1.3 格，每 3 tick 画一圈） |
| `geyser` | 一阶段 7、12、17；二阶段再 22、27 | 一阶段 3 个、二阶段 5 个泉眼，落点沿侧向相隔 4.5 格（0、+4.5、-4.5、+9、-9）；每处判定半径 1.8 格 | 每处结算一次（去重）；缓慢与击退同 `sweep`；命中处喷 6 段 SPLASH 水柱 |

- 判定帧与段数出处：`util/TideFishCombatRules.java` 第 53~76 行；范围、去重标记与附带见 `entity/TideFishBossEntity.java` 第 218~310、333~353 行。
- 范围判定与 `wave` 在结算前还会再检查一次「目标是有效玩家」与「有视线」，不满足则整下不生效（`entity/TideFishBossEntity.java` 第 344~346 行）；潮骨水刃走自己的命中回调，只校验目标是有效玩家（`entity/TideBoltEntity.java` 第 33~39、45 行；飞行与自毁第 59~66 行）。
- 当前没有任何技能预警：起手不播 actionbar 提示，地面也没有预警圈，只有前摇动画；命中特效与阶段特效（水花环）保留（`entity/TideFishBossEntity.java` 第 204~209、378~393 行；相关的 `message.ftboss.tide_*` 文案仍留在语言文件里但已无代码引用）。
- 冷却不是每技能独立，而是每次连发结束后统一进入：一阶段 15 tick、二阶段 10 tick；开场首次出手前 18 tick（`util/TideFishCombatRules.java` 第 50~51 行；扣减点 `entity/TideFishBossEntity.java` 第 165 行）。
- 技能演出期间本体被完全锁住：每 tick 水平速度归零并 `getNavigation().stop()`，只保留竖直分量（坠落照常）（`entity/TideFishBossEntity.java` 第 155~158、215 行）。
- `summon` 的伤害列是 0，它的判定帧 6 只用来生成小怪，不产生任何伤害（`entity/TideFishBossEntity.java` 第 264~266 行）。
- 冷却走完的第一次出手是「两招连着放」，第二招结束才真正开始倒计时，所以一阶段两轮技能之间至少空 15 tick、二阶段至少 10 tick（同文件第 317~329 行）。

### 双技能连发规则

每次出手一次取两招：`activeSkill = next(rotation)`、`pendingSkill = next(rotation + 1)`，随后 `rotation` 前进 2（`entity/TideFishBossEntity.java` 第 166~170 行）。第一招演完的同一 tick 无缝开始第二招，此时冷却被置 0、不再等待；第二招演完才进入 15 / 10 tick 冷却（同文件第 317~329 行）。开局 `rotation` = 0，转阶段时归零（第 64、149 行）；步长 2 与技能数 7 互质，因此 7 次出手（共 14 招）把 7 个技能各放两遍。转阶段插入的 `phase` 演出不占轮转位（第 152 行直接 `begin`），所以二阶段第一次出手仍从 `sweep` / `volley` 开始。

| 出手次序 | 第一招（立即起手） | 第二招（同一 tick 接上） |
|---|---|---|
| 第 1 次 | `sweep` | `volley` |
| 第 2 次 | `dash` | `summon` |
| 第 3 次 | `wave` | `maelstrom` |
| 第 4 次 | `geyser` | `sweep` |
| 第 5 次 | `volley` | `dash` |
| 第 6 次 | `summon` | `wave` |
| 第 7 次 | `maelstrom` | `geyser` |

出处：`util/TideFishCombatRules.java` 第 41、45 行（顺序与 `next`），`entity/TideFishBossEntity.java` 第 167~169 行（一次前进 2）。

## 四、阶段转换与血条

| 项目 | 值 | 出处 |
|---|---|---|
| 转阶段条件 | 每 tick 检查，血量 ≤ 当前最大生命 × 0.5 即转（`phase == 1` 时只判一次；多人局最大生命被 `BossScaling` 抬高后阈值同步抬高） | `entity/TideFishBossEntity.java` 第 147 行 |
| 转阶段动作 | `PHASE` 置 2、`rotation` 归零、清空 `activeSkill` 与 `pendingSkill`、强制起手 `phase` 技能、血条换 PURPLE | 同文件第 148~153 行 |
| `phase` 演出 | 时长 `PHASE_TICKS` = 40 tick；每 5 tick 在头顶 1.2 格画半径 3 格水花环；第 20 tick 播 `CONDUIT_ACTIVATE`（音调 0.75） | 同文件第 152、311~314 行 |
| 二阶段数值 | 持续与伤害改用 `secondDuration` / `secondDamage`（见技能表） | `util/TideFishCombatRules.java` 第 37~38 行 |
| 二阶段动画 | 技能动画名追加 `_p2` 后缀（`phase` 除外） | `entity/TideFishBossEntity.java` 第 192 行 |
| 狂暴（全 Boss 通用） | 血量 ≤ 50% 进入狂暴 I：迅捷 I + 抗性 I；≤ 25% 进入狂暴 II：追加力量 I + 再生 I；每 20 tick 刷新，单项持续 80 tick；触发时只在原地播咆哮与红色粒子，不发聊天或 actionbar 播报 | `boss/ai/BossRage.java` 第 40、49、85~94、98~101 行 |
| 血条 | `ServerBossEvent`，颜色 BLUE、样式 NOTCHED_10；每 tick 刷新名字与进度；每 10 tick 重扫 48 格内玩家（`2304` = 48²），超距或死亡的立即摘除 | `entity/TideFishBossEntity.java` 第 75、130~139 行 |
| 阶段 BGM | 一阶段 `boss_music_tide_fish`（1980 tick），二阶段 `boss_music_tide_fish_p2`（1927 tick）；有目标才请求播放 | `sound/BossMusic.java` 第 127~128、358 行；`entity/TideFishBossEntity.java` 第 145~146 行 |
| 存档 | `TidePhase` 与 `TideRotation` 写入 NBT，重载后恢复阶段与轮转进度 | `entity/TideFishBossEntity.java` 第 423~424、430~431 行 |

## 五、水之祝福

- 刷新间隔 `INTERVAL_TICKS` = 40 tick（按 `tickCount % INTERVAL_TICKS == 0` 限流），单项持续 `DURATION_TICKS` = 60 tick，等级全为 I（amplifier 0），且不刷粒子、不占 HUD 图标位（`util/WaterBlessing.java` 第 18~19、46~48 行）。
- 常驻三个效果：速度 I、抗性提升 I、力量 I（第 33~35 行）。
- 下雨或雷雨（`isRaining()` 或 `isThundering()`）时额外再挂两个：生命恢复 I、水下呼吸 I（第 36~39、42~44 行）。
- 适用对象：沧鳞本体（`entity/TideFishBossEntity.java` 第 129 行）与全部水族召唤物（`entity/ReefMinionEntity.java` 第 78 行）。

## 六、召唤物与雨天加成

`summon` 技能按序号交替生成 `ftboss:reef_fish`（香煎飞鱼卫，实体名见 `assets/ftboss/lang/zh_cn.json` 第 331 行）与 `ftboss:brine_puffer`（盐渍河豚，第 332 行），围绕本体 3 格环形落位、竖直 +0.3 格（`entity/TideFishBossEntity.java` 第 360~366 行）。两者共用同一套属性与 AI（`entity/ReefMinionEntity.java`），判定箱分别是 0.8 × 0.7 格与 0.8 × 0.8 格（`entity/FTEntities.java` 第 28、30 行）。

| 项目 | `ftboss:reef_fish` 香煎飞鱼卫 | `ftboss:brine_puffer` 盐渍河豚 |
|---|---|---|
| 判定箱 | 宽 0.8 格、高 0.7 格 | 宽 0.8 格、高 0.8 格 |
| 最大生命 | 24 | 24 |
| 攻击力 | 12 | 12 |
| 移动速度 | 0.34 | 0.34 |
| 索敌 | `FOLLOW_RANGE` 40 格；无有效目标时取 36 格内最近玩家 | `FOLLOW_RANGE` 40 格；无有效目标时取 36 格内最近玩家 |
| 近战 | `MeleeAttackGoal` 倍率 1.3，命中施加缓慢 I 40 tick | `MeleeAttackGoal` 倍率 1.3，命中施加缓慢 I 40 tick |
| 特殊技能 | 无 | 目标距离小于 3 格时爆刺：冷却 100 tick（初生时为 80 tick），半径 2.5 格内造成字面值 14 点伤害（实际 28 点）并施加剧毒 I 60 tick，需有视线 |
| 机动 | 有目标且着地时每 10 tick 竖直加速 0.34 格每 tick（跳扑） | 有目标且着地时每 10 tick 竖直加速 0.34 格每 tick（跳扑） |
| 存在时间 | 600 tick（30 秒）后自动消失 | 600 tick（30 秒）后自动消失 |
| 归属 | 每 20 tick 检查主人是否存活，主人死亡即消失 | 每 20 tick 检查主人是否存活，主人死亡即消失 |
| 免伤 | 免疫沧鳞本体与其它小怪的伤害 | 免疫沧鳞本体与其它小怪的伤害 |
| 经验 | `xpReward` = 0 | `xpReward` = 0 |
| 水下与药水 | 可在水下呼吸；同样吃「水之祝福」，雨天额外获得生命恢复 I 与水下呼吸 I | 可在水下呼吸；同样吃「水之祝福」，雨天额外获得生命恢复 I 与水下呼吸 I |

出处：判定箱 `entity/FTEntities.java` 第 28、30 行；属性 `entity/ReefMinionEntity.java` 第 43~44 行；经验第 38 行；近战与减速第 54、70 行；爆刺第 34、90~98 行；跳扑第 87~89 行；存在时间第 33、79 行；归属第 80~83 行；免伤第 61~64 行；药水第 78 行与 `util/WaterBlessing.java` 第 36~39 行。

- 一次召唤数量：一阶段 3 只、二阶段 5 只，并受 `MINION_CAP` = 6 限制（按归属本体的存活小怪总数计算），取 `min(上限档位, 6 - 已存在数)`、下限 0（`util/TideFishCombatRules.java` 第 5、47 行）。
- 生成时刻按序号交替取两种实体，所以偶数序号是香煎飞鱼卫、奇数序号是盐渍河豚；落点上有碰撞就跳过这一只（`entity/TideFishBossEntity.java` 第 360、373 行）。
- 另有一层同类型上限：`SummonLimit.MAX_PER_TYPE` = 10，统计半径 `RADIUS` = 64 格，超限的那一只直接跳过（`boss/ai/SummonLimit.java` 第 38、40 行；`entity/TideFishBossEntity.java` 第 362~364 行）。沧鳞这里只做「跳过」，不触发 `SummonLimit` 那套「到上限就给同类全体强化」的通用分支。
- 二阶段新召唤物自带增益：速度 I 600 tick、抗性提升 I 200 tick（`entity/TideFishBossEntity.java` 第 369~372 行）；小怪本身只活 600 tick，所以抗性提升基本全程覆盖、速度 I 与存在时间同长。此外「水之祝福」也会在第一个整 40 tick 给它们补上常驻增益。
- 本体死亡或被移除时，96 格内归属自己的小怪全部清除（同文件第 411~418 行）。
- 雨天加成：本体与小怪都只走「水之祝福」这一条，雨天/雷雨时额外多生命恢复 I 与水下呼吸 I，小怪本身没有额外的雨天数值强化（`util/WaterBlessing.java` 第 36~39 行）。

## 七、掉落

掉落表为 `data/ftboss/loot_tables/entities/tide_fish_boss.json`（路径由实体 id 决定）。经验 `xpReward` = 160（`entity/TideFishBossEntity.java` 第 73 行）。

| 掉落物 | 数量 | 条件 |
|---|---|---|
| `ftboss:tide_pearl` 潮汐珍珠 | 4~8 | 必掉；条目挂了抢夺函数，但原版会把结果夹在基础上限 8 内，实际不增加数量 |
| `minecraft:cooked_salmon` 熟鲑鱼 | 8~12 | 必掉 |
| `minecraft:prismarine_crystals` 海晶砂粒 | 2~5 | 必掉；条目挂了抢夺函数，但结果同样被夹在基础上限 5 内，实际不增加数量 |
| `ftboss:music_disc_tide_fish` 或 `ftboss:music_disc_tide_fish_p2` | 1 | 二选一，权重各 1（各 50%） |
| `minecraft:heart_of_the_sea` 海洋之心 | 1 | 基础概率 0.2，抢夺每级 +0.05 |
| 铁魔法卷轴共 5 张（见第九节） | 各 1 | 需加载 Iron's Spells、`doMobLoot` 开启、由玩家击杀 |

出处：`data/ftboss/loot_tables/entities/tide_fish_boss.json` 第 4~107 行；铁魔法卷轴见 `integ/spells/BossSpellDrops.java` 第 45~54 行（直接塞进 `LivingDropsEvent` 的掉落列表，不是掉落表条目）。

## 八、相关物品

- `ftboss:tide_trident` 潮骨三叉戟右键射出 3 道潮刃：每道 12 点伤害，80 tick 冷却，损耗 1 点耐久（`item/TideTridentItem.java` 第 53~64 行）。
- `ftboss:tide_trident` 潜行右键「回潮」：拉拽身边 5 格内的敌人，16 点伤害、缓慢 II 80 tick、向自己牵引，自身获得水下呼吸 200 tick；200 tick 冷却，损耗 3 点耐久（同文件第 35~51、63~64 行）。
- 三叉戟本体是下界合金级剑：`Tiers.NETHERITE`，攻击伤害加成 5、攻击速度修正 -2.6（`item/TideTridentItem.java` 第 26 行）。
- 深海御膳为 9 格有序配方，中心是 `farmingtales:gold_bucket`，其余为熟鳕鱼、熟鲑鱼、河豚、鹦鹉螺壳、海洋之心、海晶砂粒、干海带块、金块（`data/ftboss/recipes/tide_banquet.json`）。
- 潮骨三叉戟为无序合成：3 个潮汐珍珠 + 原版三叉戟 + 海晶砂粒（`data/ftboss/recipes/tide_trident_recast.json`）。
- 两件物品的说明文案见 `assets/ftboss/lang/zh_cn.json` 第 338~340 行（深海御膳的召唤提示、三叉戟的两种用法数值）。

## 九、铁魔法联动（可选依赖）

- 仅在加载 Iron's Spells 'n Spellbooks 时注册（`FTBossMod.java` 第 65~66 行）。`util/BossSpellRules.java` 第 79 行为 `ftboss:tide_fish_boss` 登记 5 个专属法术：`tidal_edict`（潮庭敕令，第 442 行）、`abyssal_geyser`（深渊涌柱，第 421 行）、`undertow_snare`（暗流牵引，第 617 行）、`abyssal_carapace`（深海泡甲，第 620 行）、`riptide_corridor`（裂潮长廊，第 623 行），中文名出自 `assets/ftboss/lang/zh_cn.json`（类型常量按顺序为第 21、28、49、50、51 行）。
- 类型常量（字段名照抄 `BossSpellRules.Kind`）：`damage` = 16 / 28 / 6 / 8 / 14，`mana` = 85 / 90 / 90 / 85 / 90，`cooldown`（秒）= 18 / 20 / 24 / 30 / 22，五行法术的 `school` 都是 `ice`。法术实际结算伤害由铁魔法法强公式决定（走 `BossSpellTuning.signaturePower`），不是 `damage` 字面值（`util/BossSpellRules.java` 第 21、28、49~51 行；`integ/spells/BossSignatureSpell.java` 第 29~37 行；`integ/spells/BossSpellEffects.java` 第 118~123 行）。
- 施法时长：`BossSpellRules.CAST_TICKS` = 32 tick，沧鳞这类普通 Boss 走 `BossSpellTuning.bossCast` 缩到 × 2/3，实际 22 tick 起手；`tidal_edict` / `abyssal_geyser` / `undertow_snare` / `abyssal_carapace` / `riptide_corridor` 都不属于 20 tick 的那两个特例（`util/BossSpellRules.java` 第 9、138 行；`integ/spells/BossSignatureSpell.java` 第 36、48~52 行）。`abyssal_geyser` 另有 `delay` = 20 tick，`riptide_corridor` 的脉冲首帧 = 20、间隔 = 10 tick（`util/BossSpellRules.java` 第 106、124、140 行）。
- 节奏：首次在 60 tick（`SIGNATURE_OPENING_TICKS`）后再加 0~19 tick 的实体 id 偏移进入候选，之后每 60 tick（`SIGNATURE_INTERVAL_TICKS`）尝试一次；单个法术另有 `signatureCooldown` = `max(160, 法术冷却秒数 × 20 × 0.75)` tick 的间隔（`integ/spells/BossSpellPacing.java` 第 13~14、22~24 行；应用点 `integ/spells/BossSpellCombat.java` 第 76~77、93~94 行）。施法要求目标在 32 格内且有视线，且每 tick 校验目标仍是同一人、施法未超过 60 tick（`integ/spells/BossSpellCombat.java` 第 41~43、57~66、80 行；法术射程常量 `BossSpellRules.RANGE` = 24 格用于瞄准，第 10 行）。
