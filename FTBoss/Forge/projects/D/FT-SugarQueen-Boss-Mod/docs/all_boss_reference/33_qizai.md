# 祁崽（甜苹果庄园第 11 位 · 远程星术法师）

祁崽是 2026-09-20 加入的**庄园正式轮换位**：变体 id `11`（`EasterBossEntity.QIZAI_VARIANT = 11`，`entity/EasterBossEntity.java:100`），参战名单由 10 位扩到 **11 位**（`MANOR_VARIANTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11}`，`entity/EasterBossEntity.java:108`；麻雀仍是独立支援位、不进这份名单）。她的机制分三块：**十二步主动出招循环**、**一次读条六门铁魔法**、以及**三颗完全独立的常驻浮游法晶**。

## 一、基础属性

| 项 | 值 | 出处 |
|---|---|---|
| 变体 id | `QIZAI_VARIANT = 11` | `entity/EasterBossEntity.java:100` |
| 参战名单 | 11 位（0–8、10、11），麻雀仍为支援位 | `entity/EasterBossEntity.java:108` |
| 最大生命 | 1500.0（`util/ManorSupportRules.java` 的 `BASE_HEALTH`，与其余庄园位同一套基础属性，再吃人数缩放） | `entity/EasterBossEntity.java:1033` |
| 护甲 | `ARMOR = 6.0` | `entity/EasterBossEntity.java:919` |
| 近战攻击力 | `ATTACK_DAMAGE = 60`（9 × 武器 5 × 全局 4/3） | `entity/EasterBossEntity.java:921` |
| 基础移速 | `BossWalkingSpeed.BASE_SPEED = 0.1` | `boss/ai/BossWalkingSpeed.java:21` |
| 祁崽移速倍率 | `QIZAI_MOVEMENT_MULTIPLIER = 1.3F`（单独给 variant 11，实际基础移速 0.13） | `boss/ManorDifficulty.java:22`、`:47` |
| 抗击退 | `BossKnockback.BOSS_RESISTANCE = 0.85`（2026-09-24 由 0.5 再降一档） | `util/BossKnockback.java:28` |
| 额外抬腿高度 | `STEP_HEIGHT_ADDITION = 2.4`（合计可上 3 格台阶） | `entity/EasterBossEntity.java:930` |
| 追击距离 | `FOLLOW_RANGE = 64.0` | `entity/EasterBossEntity.java:932` |
| 法术伤害倍率 | `SPELL_DAMAGE_MULTIPLIER = 2`（下表「原始」一列已含此倍率） | `util/QizaiRules.java:18` |
| 技能半径倍率 | `radiusScale(11) = 2.0`（她在庄园侧的判定半径整体 ×2） | `util/ManorBossCombatRules.java:19-22` |
| 索敌 / 起手范围 | `RANGE = 32`（目标需在 32 格内且有视线） | `util/QizaiRules.java:49` |
| 对玩家伤害倍率 | 普通难度 ×`NORMAL_DAMAGE_MULTIPLIER = 5/6`；终结难度 ×`LEGACY_DAMAGE_MULTIPLIER × IMPOSSIBLE_ATTACK_MULTIPLIER` = 2.0 × 2.0 = **4.0** | `boss/ManorImpossibleRules.java:66-68`、`boss/ManorDifficulty.java:14-17` |

只有存活、非创造、非旁观的玩家会成为目标；施法期间停止寻路。范围伤害检查高度与方块遮挡，不能隔墙命中。

## 二、出招循环与通用节奏

出招顺序（按代码 `PATTERN`，`util/QizaiRules.java:72`）：星矢 → 落星阵 → 星十字 → 星矢 → 落星阵 → 星矢 → 玫瑰星陨 → 绯星祈愿 → 绯星指针 → 天陨星裁 → 绯星锁裁 → 万棱星狱，走完一轮才回到开头。

| 节奏项 | 值 | 出处 |
|---|---|---|
| 每招前摇 | 见第三节表内「前摇」列，由 `QizaiRules.windup(kind)` 决定 | `util/QizaiRules.java:75-81` |
| 通用收招 | `RECOVERY = 24` tick（固定不压缩的收招窗口） | `util/QizaiRules.java:26` |
| 通用休整 | `REST = 32` tick | `util/QizaiRules.java:27` |
| 冷却旋钮 | `cooldown(tick) = ceil(tick / COOLDOWN_MULTIPLIER)`，`COOLDOWN_MULTIPLIER = 3` | `util/QizaiRules.java:33-42` |
| 大招休整倍数 | 绯星指针 / 天陨星裁 / 绯星锁裁 / 万棱星狱各自 `REST_MULTIPLIER = 2` | `util/QizaiSweepRules.java:17`、`util/QizaiPrismRules.java:16`、`util/QizaiLaserBarrageRules.java:19` |
| 星矢飞行 | `STAR_SPEED = 0.8` 格每 tick，寿命 `STAR_LIFETIME = 60` tick | `util/QizaiRules.java:48-50` |
| 三段式结算 | `RELEASES = 3` 次，间隔 `REPEAT_INTERVAL = 12` tick（天陨与玫瑰星陨共用这两个旋钮） | `util/QizaiRules.java:16-17` |

## 三、十二招数值表

「原始」= 代码常量（已含 `SPELL_DAMAGE_MULTIPLIER = 2`）；「普通 / 终结」= 乘上第一节的对玩家伤害倍率（5/6 与 4.0），不含护甲、抗性与保护附魔。

| 技能 | 前摇 tick | 判定 | 原始伤害 | 普通 / 终结 | 要点与出处 |
|---|---|---|---|---|---|
| 星矢 | 12 | 三枚扇形，撞方块消失 | `STAR_DAMAGE = 48` | 40 / 192 | 不追踪，每 tick 0.8 格，最多 60 tick；`util/QizaiRules.java:54` |
| 落星阵 | 16 | 半径 `CIRCLE_RADIUS = 5.6` | `CIRCLE_DAMAGE = 80` | 66.7 / 320 | 锁定玩家当时脚下，结算前不再跟随；`util/QizaiRules.java:52-55` |
| 星十字 | 18 | 十字 `CROSS_LENGTH = 12` × `CROSS_WIDTH = 1.4` | `CROSS_DAMAGE = 104` | 86.7 / 416 | 铁魔法侧的主招之一，见第四节；`util/QizaiRules.java:19-24` |
| 玫瑰星陨 | 20 | 半径 `STARFALL_RADIUS = 3.2`，偏移 `STARFALL_OFFSET = 7.6` | `STARFALL_DAMAGE = 64` | 53.3 / 256 | 中心加四个周边落点，间隔 `STARFALL_INTERVAL = 8` tick；`util/QizaiRules.java:21-25` |
| 绯星祈愿 | 30 | 半径 `PRAYER_RADIUS = 6.4` | `PRAYER_DAMAGE = 128` | 106.7 / 512 | 中心与左右共三个星阵，相邻中心距 7 格留缝隙；每名玩家每次最多命中一次；`util/QizaiRules.java:53-56` |
| 星步 | 无（独立冷却） | 玩家进入 `STEP_TRIGGER_RANGE = 5` 格内尝试侧退 | 无伤害 | 无 | 触发概率 `STEP_CHANCE = 0.4`，距离 `STEP_MIN_DISTANCE = 4` 至 `STEP_MAX_DISTANCE = 8` 格，冷却 `STEP_COOLDOWN = 80`、失败重试 `STEP_RETRY_TICKS = 20`；`util/QizaiRules.java:8-13` |
| 绯星指针（第 7 招） | 30 | 扫射半径 `RADIUS = 32.0`，束半宽 `BEAM_HALF_WIDTH = 1.3` | `BEAM_DAMAGE = 48`（与星矢同档） | 40 / 192 | 低位四向 `LOW_HEIGHT = 0.9` 与高位四向 `HIGH_HEIGHT = 2.1` 双层 `ARMS = 4`，转速 `DEGREES_PER_TICK = -1.325` 与 `HIGH_DEGREES_PER_TICK = -0.75`；每轮 `SWEEP_TICKS = 100` tick，间隔 `VOLLEY_INTERVAL = 15` 发射 `VOLLEY_STARS = 8` 枚轻微追踪星矢（每 tick 最多转 `VOLLEY_TURN_DEGREES = 4` 度，速度 `VOLLEY_SPEED = 0.55`，寿命 `VOLLEY_LIFETIME = 40`）；接 `RAIN_WAVES = 5` 波星雨，每波 `RAIN_COUNT = 24` 枚、间隔 `RAIN_GAP = 10` tick、整体旋转 `RAIN_ROTATION_PER_WAVE = 7.5` 度、速度 `RAIN_SPEED = 0.7`；同一玩家被束扫到的免疫间隔 `BEAM_HIT_COOLDOWN = 20` tick；`util/QizaiSweepRules.java:11-33` |
| 天陨星裁（第 8 招） | 升空 `RISE_TICKS = 20` + 蓄力 `CHARGE_TICKS = 100` | 水平半径 `RADIUS = 48.0`，垂直容差 `VERTICAL_TOLERANCE = 32.0` | 1000（`DAMAGE` 500 × 2，固定值不吃任何倍率） | 833.3 / 4000 | 升空 `RISE_HEIGHT = 24.0` 格并进入无法选中；固定 1000 点魔法伤害，不随难度或 Boss 加成浮动；结算前解除无敌并落回地面起跳点；`util/QizaiSkyNovaRules.java:23-53` |
| 绯星锁裁（第 9 招） | 追踪 `TRACK_TICKS = 24` + 锁定 `LOCK_TICKS = 12` | 锁定半径 `MIN_ANCHOR_RADIUS = 7.0` 至 `MAX_ANCHOR_RADIUS = 14.0`，高度 `MIN_ANCHOR_HEIGHT = 3.0` 至 `MAX_ANCHOR_HEIGHT = 9.0`，束半径 `BEAM_RADIUS = 0.55` | `DAMAGE = 72` | 60 / 288 | `BEAMS = 6` 束，间隔 `FIRE_GAP = 2` tick，淡出 `BEAM_FADE = 8`；她不升空、不隐身、不免伤；`RANGE = 40.0`；`util/QizaiPrismRules.java:10-24` |
| 万棱星狱（第 10 招） | 每束 `WARNING_TICKS = 14` | 发生场半径 `FIELD_RADIUS = 24.0`，锚点高度 `MIN_ANCHOR_HEIGHT = 3.0` 至 `MAX_ANCHOR_HEIGHT = 10.0`，束长 `BEAM_LENGTH = 36.0`、半径 `BEAM_RADIUS = 0.45` | `DAMAGE = 48` | 40 / 192 | `ROUNDS = 3` 轮 30 / 60 / 120 束（单轮上限 `MAX_BEAMS_PER_ROUND = 120`，合计 210 束），每轮生成期 `ACTIVE_TICKS = 120` tick、每 `SPAWN_GAP = 4` tick 生成一束，每 `TARGET_INTERVAL = 20` tick 至少一束锁定玩家；`RANGE = 40.0`；`util/QizaiLaserBarrageRules.java:10-28` |

## 四、一次读条六门法术（铁魔法侧）

| 项 | 值 | 出处 |
|---|---|---|
| 每次读条齐发门数 | `SPELLS_PER_CAST = 6` | `skill/QizaiMulticast.java:28` |
| 其中庄园辅助门数 | `SUPPORT_PER_CAST = 2`（按游戏时间轮换，不会每次同一对） | `skill/QizaiMulticast.java:30` |
| 组合方式 | 主招 + 另一门自己的招 + 2 门庄园辅助 + 2 门额外星术（追踪飞弹与激光水晶一线） | `skill/QizaiMulticast.java` |
| 辅助池 | 庄园 `Kind.support()` 为真（`damage == 0`）且不属于她自己变体的法术，共 **7 门**：人偶守护 / 猩红誓言 / 灶火盛宴 / 光环圣所 / 振翅 / 潮汐莲花 / 羽翼庇护 | `util/ManorSpellRules.java:14-71`、`skill/QizaiMulticast.java:35-40` |
| 自己的两门攻击法术 | 星十字 `star_cross`（伤害 26、魔力 95、冷却 24 秒、读条 36 tick）与玫瑰星陨 `rose_starfall`（伤害 16、魔力 110、冷却 30 秒、读条 40 tick）—— 庄园签名法术表里唯二属于变体 11 的两门，也就是「主招 / 另一门自己的招」的候选 | `util/ManorSpellRules.java:48-49` |
| 读条时长 | `ManorSignatureSpell` 里对祁崽**再减半一次**，之后仍过难度 | `integ/spells/ManorSignatureSpell.java:49-54` |
| 日志口径 | 每次读条打出**真实放出去的法术 id**，哪门没生效一眼可见 | `skill/QizaiMulticast.java` |

铁魔法未安装时主招回退到纯代码版本，辅助法术直接跳过；六门法术各用独立 `MagicData`，互不覆盖。

## 五、三颗浮游法晶（独立常驻被动）

三颗法晶使用自己的被动状态与目标缓存，不进入 `QizaiMagic`、不调用 `playCast`、也不用 `setTarget` 反写本体目标，因此**可与任意主动技能并行**。

| 项 | 值 | 出处 |
|---|---|---|
| 数量与种类 | `COUNT = 3`（`LASER = 0`、`MISSILE = 1`、`GUARD = 2`） | `util/QizaiCrystalRules.java:4-7` |
| 法晶生命 | `HEALTH = 120` | `util/QizaiCrystalRules.java:8` |
| 作用范围 | `RANGE = 36` | `util/QizaiCrystalRules.java:9` |
| 重生与闲置 | 被击落后 `RESUMMON_TICKS = 240` tick 重生；无目标时闲置 `IDLE_TICKS = 100` | `util/QizaiCrystalRules.java:10-11` |
| 激光晶 | 前摇 `LASER_WINDUP = 16`、持续 `LASER_DURATION = 9`、冷却 `LASER_COOLDOWN = 80`，伤害 32（`LASER_DAMAGE` 16 × 2） | `util/QizaiCrystalRules.java:12-15` |
| 飞弹晶 | 冷却 `MISSILE_COOLDOWN = 100`，一轮 `MISSILE_COUNT = 9` 枚，寿命 `MISSILE_LIFETIME = 70`，伤害 `MISSILE_DAMAGE = 24` | `util/QizaiCrystalRules.java:16-20` |
| 护盾晶 | 护盾量 `WARD_CAPACITY = 240`（= 80 × `SHIELD_MULTIPLIER = 3`），减伤 `WARD_REDUCTION = 0.35`，持续 `WARD_DURATION = 120`、冷却 `WARD_COOLDOWN = 200` | `util/QizaiCrystalRules.java:18-24` |

## 六、星棱炮镜（绯星连奏的专属演出）

| 项 | 值 | 出处 |
|---|---|---|
| 一轮晶棱 / 飞弹 | `CRYSTALS = 6` 枚晶棱、`MISSILES = 12` 枚追踪飞弹 | `util/QizaiArsenalRules.java:4-5` |
| 蓄能与节奏 | 蓄能 `CHARGE = 24` tick，晶棱间隔 `LASER_GAP = 2` tick，淡出 `LASER_FADE = 8` tick，单轮总长 `SALVO_DURATION = 42` tick | `util/QizaiArsenalRules.java:6-9` |
| 轮次与冷却 | 最多 `MAX_SALVOS = 4` 轮，冷却 `COOLDOWN = 180` tick | `util/QizaiArsenalRules.java:10-11` |
| 光束 | 射程 `RANGE = 32`，宽度 `LASER_WIDTH = 0.45`，伤害 `LASER_DAMAGE = 48` | `util/QizaiArsenalRules.java:12-14` |
| 追踪飞弹 | 寿命 `MISSILE_LIFE = 60`，速度 `MISSILE_SPEED = 0.58`，每 tick 最多转 6 度 | `util/QizaiArsenalRules.java:15-17` |
| 近身震退 | 触发距离 `PUSH_TRIGGER = 5` 格，半径 `PUSH_RADIUS = 7`，前摇 `PUSH_WINDUP = 16`、淡出 `PUSH_FADE = 12`、冷却 `PUSH_COOLDOWN = 100` | `util/QizaiArsenalRules.java:18-22` |

## 七、不可打断与硬清理边界

| 项 | 口径 | 出处 |
|---|---|---|
| 保护型施法（免伤） | `isQizaiProtectedCasting()`：**只剩第八招「天陨星裁」**——它一旦成功起手就拒绝普通取消、目标丢失、通用读条清理与临时 NoAI。**★ 2026-09-24 按用户要求去掉第七招「绯星指针」的免伤**（那一招的绯色星束激光整段演出期间她现在是可受伤的，只剩"流程不可软打断"） | `boss/ai/QizaiMagic.java`、`entity/EasterBossEntity.java:1589` |
| 不可打断施法 | `isQizaiUninterruptibleCasting()`：庄园普通防逃课召回在这段期间暂停，四处召回 / 传送入口也会二次拒绝 | `entity/EasterBossEntity.java:1590`、`boss/ManorFight.java:1216`、`:1244`、`:1322`、`:1336` |
| 万棱星狱的例外 | 「不可打断」只保护状态机，不提供免伤；她仍保持地面、碰撞、可选中与可受伤；失去玩家后按最近锁点与随机方向完成三轮 | `skill/QizaiLaserBarrage.java` |
| 硬清理边界 | `forceCancel()` 只留给死亡、换变体、真正的庄园硬转场、实体移除，以及迷你 / 巨型 / 终章等硬身份切换 | `entity/EasterBossEntity.java:1084-1085` |

## 八、终结难度规则（2026-09-21）

终结难度（`ManorDifficulty.IMPOSSIBLE`，枚举标题就是「终结」）通过 `acceleratedTicks(ticks) = ticks / 2 + ticks % 2` **只在该难度**把前摇与冷却折半（`boss/ManorDifficulty.java:38-40`）。

| 项目 | 普通难度 | 终结难度 | 出处 |
|---|---|---|---|
| 自招前摇 | 星矢 12、落星阵 16、星十字 18、玫瑰星陨 20、绯星祈愿 30 | 各减半：6 / 8 / 9 / 10 / 15 | `boss/ai/QizaiMagic.java` |
| 五处冷却 | 普通招 11、指针与天陨 22 等 | 各减半 | `boss/ai/QizaiMagic.java` |
| 铁魔法读条 | 祁崽在 `ManorSignatureSpell` 里已再减半一次 | 之后再减半 | `integ/spells/ManorSignatureSpell.java:49-54` |
| 天陨星裁升空与蓄力 | 20 + 100 tick（5 秒） | **10 + 50 tick（2.5 秒）** | `skill/QizaiSkyNova.java` |
| 星步冷却与重试 | 80 / 20 tick | 各减半 | `boss/ai/QizaiMagic.java` |
| 绯星指针演出 | 扫射 100、星雨间隔 10、星矢轮次 15 tick | **各减半（50 / 5 / 8）**；总转角与波数不变，整场快一倍 | `skill/QizaiSweep.java` |
| 万棱星狱四阶段 | 预警 14、生成 120、淡出 6、收招 20 tick | 各减半 | `skill/QizaiLaserBarrage.java` |
| 绯星锁裁追踪与锁定 | 24 + 12 tick | 各减半 | `skill/QizaiPrismLock.java` |
| 不跟着减的 | 收招窗口（`RECOVERY = 24`）与演出规模（星雨波数、弹幕数量、光束伤害） | 同左 | `util/QizaiRules.java:26` |

终结难度通关会发放进度 `ftboss:manor_impossible_victory`（「卧槽，外挂！」）；普通难度通关发放的是 `ftboss:memories_end`（「回忆的终结」），两条互不替代。

## 九、玩家侧：祁崽的四门星术

她的四门星术做成可选抄录的法术卷轴（属于铁魔法体系，需先取得卷轴并抄录，不会仅持杖就自动获得 Boss 范围技能）。

| 法术 id | 对应技能 | 伤害 | 魔力 | 冷却（秒） | 读条（tick） | 出处 |
|---|---|---|---|---|---|---|
| `qizai_star_volley` | 星矢 | 24 | 45 | 6 | 24 | `integ/spells/QizaiPlayerSpells.java:19` |
| `qizai_starfall` | 落星阵 | 40 | 75 | 12 | 32 | `integ/spells/QizaiPlayerSpells.java:20` |
| `qizai_crimson_prayer` | 绯星祈愿 | 64 | 130 | 28 | 60 | `integ/spells/QizaiPlayerSpells.java:21` |
| `qizai_starstep` | 星步 | 0 | 35 | 8 | 8 | `integ/spells/QizaiPlayerSpells.java:22` |

冷却的单位是**秒**（`setCooldownSeconds(kind.cooldown)`，`integ/spells/QizaiSpell.java:44`），读条的单位是 tick（`castTime = kind.castTicks`，`:41`）；四门都是 `CastType.LONG`、传说稀有度、魔力与伤害按上面的表取值，等级上限 1。

武器与道具：`ftboss:qizai_prayer_staff`（绯星祈愿杖，使用铁魔法 `CastingItem` 施法行为）、召唤蛋 `ftboss:easter_egg_qizai`；法杖进入创造武器栏与庄园胜利宝箱，不在挑战途中额外掉落。 法杖掉落态有低频绯红/金色星尘，拾取时在拾取点展开绯红五芒星与金色星尖并播放紫水晶清响。 法杖另有 4.05 秒独立第一人称视检动作，可通过当前绑定的视检键完整展示星环、浮晶与环绕咒页。

## 十、可躲避性与反制要点

- 星矢扇面靠横移躲；星十字沿十字条躲；玫瑰星陨与绯星祈愿的星阵之间有固定缝隙，站在缝隙里安全。
- 绯星指针是双层旋转束：低位束（0.9 格）跳起来躲、高位束（2.1 格）站着别跳，节奏是「跳、不跳、跳、不跳」，同时要横移甩掉追踪星矢。
- 天陨星裁有 5 秒（终结 2.5 秒）蓄力与半径 48 格的地面预警圈，内圈从 48 格收到 `WARNING_INNER_MIN = 8.0` 格；跑出 48 格即可完全规避。
- 绯星锁裁锁定后落点冻结、每 2 tick 一束，锁定提示出现后横移即可躲避；方块能截断射线。
- 万棱星狱在 24 格发生场内连放三轮，每束 14 tick 预警且终点冻结，离开发生场或借方块遮挡都能规避。
- 三颗浮游法晶可被普通攻击击落（每颗 120 生命，240 tick 后重生）；护盾晶给的 240 点护盾与 35% 减伤需要先打掉。

## 十一、验证入口

已接入 `node tools/verify_project.cjs` 的四道门：

| 门名 | 脚本 | 覆盖 |
|---|---|---|
| `qizai-sweep` | `tools/verification/check_qizai_sweep.cjs` | 绯星指针在出招循环里的位置、数值与可躲避性 |
| `qizai-buffs` | `tools/verification/check_qizai_buffs.cjs` | v188 五条强化（前摇、半径、六门齐发、移速、辅助池） |
| `qizai-liveness` | `tools/verification/check_qizai_spell_liveness.cjs` | 从 `ManorSpellRules` 枚举反推辅助法术承接路径；天陨星裁不会被自己的看门狗打断 |
| `qizai-final` | `tools/verification/check_qizai_final_difficulty.cjs` | 终结难度施法与冷却减半全覆盖（含三条例外） |

另有同类脚本与 Java 检查（由 Gradle 或手动调用）：`check_qizai_sky_nova.cjs`、`check_qizai_prism.cjs`、`check_qizai_laser_barrage.cjs`、`check_qizai_assets.cjs`、`check_qizai_scrolls.cjs`、`check_qizai_preview.cjs`；`QizaiGeometryCheck`、`QizaiAdvancedGeometryCheck`、`QizaiCrystalGeometryCheck`、`QizaiPrismRulesCheck`、`QizaiLaserBarrageRulesCheck`、`QizaiSkyNovaModelCheck`、`QizaiCrystalModelCheck`、`QizaiMissileModelCheck`、`QizaiRoseStarfallModelCheck`、`QizaiVisibilityCheck`（同类均在 `tools/verification/` 下），以及 `tools/verification/gametest/Qizai*GameTests.java` 一整套 Gametest。

详细开发记录见 `docs/QIZAI_BOSS.md`、`docs/QIZAI_ARSENAL_20260921.md`、`docs/QIZAI_LASER_BARRAGE_20260921.md`、`docs/QIZAI_PRISM_LOCK_20260921.md`、`docs/QIZAI_INFERNO_SWEEP.md`、`docs/QIZAI_SCROLLS.md`、`docs/qizai-floating-crystals.md`。
- 详细开发记录与参考文献见仓库文档 `docs/QIZAI_BOSS.md`、`docs/QIZAI_LASER_BARRAGE_20260921.md`、`docs/QIZAI_PRISM_LOCK_20260921.md`、`docs/qizai-floating-crystals.md`。
