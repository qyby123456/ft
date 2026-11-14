# 苹果爵士·蜜脆

常规 Boss 第 8 只：`ftboss:apple_boss`。核心机制是**冲向玩家自爆**（同一爆心连续三段、伤害与半径逐段递增）。天光光束、巨型火球、酸蚀喷吐、蜡护膜、召唤与金苹果自愈两个阶段都会用；一阶段「果皮形态」另有烂苹果投掷与果籽霰弹，血量过半进入二阶段「虫蛀形态」，追加**汁液喷洒**与**钻地突袭**（代码里只有这两招带阶段判定），并额外每 45 秒吃一次附魔金苹果。玩家最终承受的伤害经「技能内 ×0.5」与「运行时全局 ×2.0」两段处理，净结果等于技能字面量。

## 实体与判定箱

| 项目 | 数值与规则 | 出处 |
|---|---|---|
| 实体 id | `ftboss:apple_boss` | `entity/FTEntities.java` 第 161 至 162 行 |
| 中文名 | 苹果爵士·蜜脆 | `assets/ftboss/lang/zh_cn.json` 第 206 行 |
| 判定箱宽 × 高 | 1.7 格 × 2.7 格 | `entity/FTEntities.java` 第 162 行 |
| 实体分类 | `MobCategory.MONSTER`（召唤物同） | `entity/FTEntities.java` 第 179 至 182 行 |
| 召唤方式 | 右键【禁果】`ftboss:forbidden_apple`，在水平朝向（`player.getDirection()`）前方 4 格生成，非创造模式消耗 1 个 | `item/ForbiddenAppleItem.java` 第 32 至 48 行 |
| 维度限制 | `SummonGate.OVERWORLD_ONLY` = true：只在主世界可召唤，其它维度中止召唤且不消耗禁果 | `boss/SummonGate.java` 第 25、30 至 33 行 |
| 自然消失 | `removeWhenFarAway` 恒为 `false`，不因远离玩家消失 | `entity/AppleBossEntity.java` 第 1221 行 |
| 分身 | `ftboss:apple_clone`，判定箱 1.5 格 × 2.4 格 | `entity/FTEntities.java` 第 150 至 151 行 |
| 果虫 | `ftboss:apple_worm`，判定箱 0.7 格 × 0.5 格 | `entity/FTEntities.java` 第 132 至 133 行 |
| 果蛾 | `ftboss:apple_moth`，判定箱 0.7 格 × 0.6 格 | `entity/FTEntities.java` 第 135 至 136 行 |
| 烂苹果堆 | `ftboss:rotten_apple`，判定箱 0.8 格 × 0.9 格 | `entity/FTEntities.java` 第 138 至 139 行 |
| 青苹果 | `ftboss:green_apple`，判定箱 0.8 格 × 0.9 格 | `entity/FTEntities.java` 第 142 至 143 行 |
| 金币人 | `ftboss:coin_man`，判定箱 0.7 格 × 1.5 格 | `entity/FTEntities.java` 第 146 至 147 行 |
| 巨型果蛆 | `ftboss:giant_grub`，判定箱 3.0 格 × 2.0 格 | `entity/FTEntities.java` 第 158 至 159 行 |

## 基础属性表

| 属性 | 数值 | 出处 |
|---|---|---|
| `MAX_HEALTH` | 320.0 | `entity/AppleBossEntity.java` 第 167 行 |
| `ARMOR` | 2.0 | `entity/AppleBossEntity.java` 第 168 行 |
| `ATTACK_DAMAGE` | 3.5（仅近战保留路径使用，常规攻击不走它；全局 ×2 后实收 7.0） | `entity/AppleBossEntity.java` 第 169 行 |
| `MOVEMENT_SPEED` | 属性基准 0.1（`BossWalkingSpeed.BASE_SPEED`），普通陆地寻路结果再 ×2；走位倍率 1.5，二阶段变身把基准重置为同一值，不额外提速 | `entity/AppleBossEntity.java` 第 170、1013 行；`boss/ai/BossWalkingSpeed.java` 第 24、74 行 |
| `KNOCKBACK_RESISTANCE` | 0.85（`BOSS_RESISTANCE`，只吃 15% 击退） | `entity/AppleBossEntity.java` 第 171 行；`util/BossKnockback.java` 第 31 行 |
| `FOLLOW_RANGE` | 42.0 | `entity/AppleBossEntity.java` 第 172 行 |
| 经验奖励 | `xpReward` = 140 | `entity/AppleBossEntity.java` 第 157 行 |
| 火焰 | 火焰免疫，且不显示着火动画 | `entity/AppleBossEntity.java` 第 214 至 218 行 |
| 负面效果 | 每 20 tick 清一次自身全部非增益效果 | `entity/AppleBossEntity.java` 第 285 至 289 行 |
| 负面效果（事件层已关闭） | 事件层全局 `DENY` 已停用（`IMMUNE_TO_DEBUFFS` = false）；本体与分身仍每 20 tick 自清全部非增益效果，外来减益实际只能短暂存在；果虫、果蛾、烂苹果堆、青苹果、金币人、巨型果蛆本来就不在旧 DENY 名单内 | `boss/BossHandler.java` 第 409 行、`entity/AppleBossEntity.java` 第 302 至 306 行 |
| 单次伤害完全无效阈值 | 结算后的实际扣血伤害 > `MAX_FINAL_DAMAGE` = 300.0 时本次血量伤害归零（不是截断；恰好 300 或更低放行；不按原始伤害判断；`/kill` 保留）；原始 500 点规则与旧 `LIMIT_ENABLED` 限伤继续停用；本 Boss 适用，玩家、外部 Boss、鼠群小怪、迷你 Boss、召唤物不适用 | `boss/ai/FinalBossDamageGuard.java` 第 19 行、`mixin/FinalBossDamageMixin.java` 第 14 行 |
| 限伤体系 | `LIMIT_ENABLED` = `false`，不启用单次上限与连击递减 | `boss/ai/BossDamageGate.java` 第 46 行 |
| 阵营免伤 | 本体、分身与全部召唤物免疫闪电与爆炸伤害 | `entity/AppleBossEntity.java` 第 758 至 764 行、`boss/BossHandler.java` 第 781 至 789 行 |
| 伤害写入口径 | 技能字面量已把全局 ×0.5 烘焙进数据，随后 `NonManorDamageX2` 对「本模组非庄园 Boss 造成的全部伤害」再乘 ×2.0（`e.setAmount(amt * 2.0f)`），两段相抵 —— 玩家实收 = 技能字面量 | `boss/ai/BossDamageTuning.java` 第 11 至 24 行；`boss/ai/NonManorDamageX2.java` 第 39 至 43 行；`FTBossMod.java` 第 53 行 |
| 走位 | 目标玩家；追击速度倍率 1.5，距离 2.5 格内停下 | `entity/AppleBossEntity.java` 第 167、172 至 200 行 |

## 阶段与转换

| 项目 | 数值与规则 | 出处 |
|---|---|---|
| 初始阶段 | 阶段 1「果皮形态」，`DATA_PHASE` 默认 1 | `entity/AppleBossEntity.java` 第 206 行 |
| 进入二阶段 | 血量 ≤ 最大生命 × 0.5 且当前无动作，立即置为阶段 2 并播放 56 tick 变身 | `entity/AppleBossEntity.java` 第 366 至 379 行 |
| 变身爆发帧 | 变身动作播放到第 24 tick 时结算 | `entity/AppleBossEntity.java` 第 993 至 995 行 |
| 变身增益 | 力量 II 600 tick；再生 II 600 tick；吸收 IV 2400 tick；防火 6000 tick；抗性 I 6000 tick；回复 40.0 生命；金苹果与附魔金苹果计时同时重置为 300 / 900 tick | `entity/AppleBossEntity.java` 第 1014 至 1022 行 |
| 变身强制技能 | 同时开始天光光束（锁定目标位置，60 tick 预警）与巨型火球（30 tick 预警） | `entity/AppleBossEntity.java` 第 1013 至 1019 行 |
| 二阶段常驻姿势 | 额外叠加播放 `animation.apple_boss.lid_open`（蜡盖缩没、虫子探出） | `entity/AppleBossEntity.java` 第 1231 至 1237 行 |
| 金苹果（两个阶段都有） | 每 300 tick（15 秒）：再生 II 100 tick、吸收 I 2400 tick、回复 4.0 生命 | `entity/AppleBossEntity.java` 第 312 至 324 行 |
| 附魔金苹果（仅二阶段） | 每 900 tick（45 秒）：再生 II 600 tick、吸收 IV 2400 tick、防火 6000 tick、抗性 I 6000 tick、回复 40.0 生命 | `entity/AppleBossEntity.java` 第 326 至 340 行 |
| 阶段持久化 | 阶段与蜕变标记写入 NBT（`phase`、`transformed`） | `entity/AppleBossEntity.java` 第 223 至 236 行 |
| 血条换曲 | 变身时先停一阶段 BGM，再播二阶段 BGM | `entity/AppleBossEntity.java` 第 1028 至 1037 行 |

## 技能表

- 冷却只在「空闲选招」阶段递减；每 tick 按固定顺序自上而下检查，命中一招就 `return`，顺序为：冲撞自爆 → 烂苹果投掷 → 果籽霰弹 → 汁液喷洒 → 酸蚀喷吐 → 钻地突袭 → 分身召唤 → 巨型果蛆 → 头顶三苹果 → 天光光束 → 巨型火球 → 蜡护膜 → 召唤虫群，出处 `entity/AppleBossEntity.java` 第 451 至 569 行。
- 距离条件全部用 `distanceToSqr` 判定；表中的「设计值」是代码里作为设计值传入的字面量，「实收」是玩家最终承受的最大值。伤害走两段：技能内先把字面量折半（标注「字面量已含折半」的表示代码里已是折半后的值），随后 `NonManorDamageX2` 再全局 ×2.0，净结果是**实收 = 技能字面量**，出处 `boss/ai/NonManorDamageX2.java` 第 39 至 43 行。

| 技能 | 触发与冷却 | 前摇与爆发帧 | 范围 | 伤害 | 附带 |
|---|---|---|---|---|---|
| 冲撞自爆（常规攻击） | 冷却初始 10 tick；每次引爆后重置 22 + rand(16) tick | 蓄力 10 tick，随后冲刺 40 tick，每 tick 前进 1.2 格 | 撞到 2.5 格内玩家、或距锁定点小于 1.2 格、或冲刺计时结束时引爆 | 三段设计值 45 / 52 / 60，半径 6 / 6.5 / 7 格，按距离衰减到 25%；中心实收 45.0 / 52.0 / 60.0 | 后续两段各隔 8 tick，共用同一爆心；击退 1.3 / 1.7 / 2.1；纯视觉爆炸，不毁掉落物、不引爆自家分身 |
| 烂苹果投掷 | 冷却初始 140 tick；一阶段重置 230 + rand(90) tick，二阶段 150 + rand(70) tick；需目标 7 至 26 格 | 动作 28 tick，第 12 tick 出手 | 投射物飞行，带提前量预判 | 设计 12 + 阶段 × 3（实收 15.0 / 18.0） | 命中附带缓慢 100 tick、反胃 140 tick、饥饿 160 tick；投射物外观为原版苹果，不爆炸 |
| 果籽霰弹 | 冷却初始 90 tick；一阶段重置 200 + rand(90) tick，二阶段 130 + rand(60) tick；需目标小于 20 格 | 动作 32 tick，第 16 tick 结算 | 前方 7 格、半角 70 度扇形 | 设计 9 + 阶段 × 2（实收 11.0 / 13.0） | 击退 0.7 |
| 酸蚀喷吐 | 冷却初始 200 tick；一阶段重置 300 + rand(120) tick，二阶段 200 + rand(90) tick；需目标小于 12 格 | 动作 36 tick，第 15 tick 结算 | 前方 6.5 格、半角 60 度扇形 | 设计 12 + 阶段 × 3（实收 15.0 / 18.0） | 中毒 140 tick、缓慢 II 100 tick、击退 0.7 |
| 汁液喷洒（二阶段） | 冷却初始 240 tick；重置 220 + rand(90) tick；需目标小于 14 格 | 动作 36 tick，第 15 tick 结算 | 前方 8 格、半角 55 度扇形 | 3.0（字面量已含折半，全局 ×2 后实收 6.0） | 缓慢 III 140 tick、挖掘疲劳 II 120 tick、反胃 100 tick |
| 蜡护膜 | 冷却初始 320 tick；重置 460 + rand(160) tick | 动作 40 tick，第 20 tick 生效 | 自身 | 无直接伤害，回复 20.0 生命 | 抗性提升 III 140 tick、吸收 II 200 tick |
| 钻地突袭（二阶段） | 冷却初始 320 tick；重置 400 + rand(160) tick；需目标小于 30 格 | 潜入 26 tick，期间隐形且无敌 | 现身于目标脚下，约 5.5 格内结算 | 字面量已含折半：10 + 阶段 × 1.5（二阶段实收 26.0） | 向上弹起 0.85；现身时纯视觉爆炸 |
| 分身召唤 | 冷却初始 120 tick；一阶段重置 200 + rand(100) tick，二阶段 140 + rand(80) tick | 动作 44 tick，第 19 tick 生成 | 本体周围 4.5 至 6.0 格环形均匀分布 | 无直接伤害 | 单次 5 + rand(3) 只，二阶段再 +2；受同类 10 只上限约束，到顶改为群体强化 |
| 巨型果蛆召唤 | 冷却初始 200 tick；一阶段重置 600 + rand(240) tick，二阶段 400 + rand(160) tick | 动作 44 tick，第 19 tick 生成 | 本体周围 4 格 | 无直接伤害 | 场上已有存活果蛆则跳过，同时只允许 1 条 |
| 头顶三苹果抛射 | 冷却初始 160 tick；一阶段重置 240 + rand(90) tick，二阶段 150 + rand(70) tick | 动作 32 tick，第 21 tick 三发齐射 | 三发 ±12 度扇形，抛物线下坠 | 设计 14 + 阶段 × 3（实收 17.0 / 20.0） | 命中附带与烂苹果投掷相同：缓慢 100 tick、反胃 140 tick、饥饿 160 tick |
| 天光光束 | 冷却初始 220 tick；一阶段重置 560 + rand(200) tick，二阶段 380 + rand(140) tick | 施法动作 44 tick，锁定后 60 tick（3 秒）光柱预警 | 以锁定点为中心 15 格；共 10 波，每波 16 处，波间隔 10 tick | 每处 7.0（字面量已含折半）按 3.5 格距离衰减，最低 2.45；全局 ×2 后实收 14.0、最低 4.9 | 每波前 5 处为天火并点燃 4 秒，后 11 道为真实闪电（原版闪电实体）；每次命中向上弹起 0.4；60% 落点压在目标 6 格内，每波第 6 处必劈目标 |
| 巨型火球砸地 | 冷却初始 300 tick；一阶段重置 460 + rand(200) tick，二阶段 320 + rand(140) tick | 施法动作 44 tick，坠落预警 30 tick（1.5 秒） | 落点半径 8 格 | 30.0（字面量已含折半）按距离衰减到 40%，最低 12.0；全局 ×2 后实收 60.0、最低 24.0 | 点燃 6 秒、向上弹起 1.0 |
| 召唤虫群 | 冷却初始 160 tick；一阶段重置 260 + rand(100) tick，二阶段 180 + rand(80) tick | 动作 44 tick，第 19 tick 生成 | 本体周围约 1.5 至 1.75 格随机散布 | 无直接伤害 | 五选一等概率：果虫、果蛾、烂苹果堆、青苹果、金币人 |
| 近战挥击（保留路径） | 没有注册近战 AI 目标，仅被强制近战时触发 | 动作 18 tick | 近战距离 | `ATTACK_DAMAGE` = 3.5（全局 ×2 后实收 7.0） | 饥饿 100 tick、缓慢 60 tick |

- 出处：冲撞状态机 `entity/AppleBossEntity.java` 第 574 至 612 行；三段爆炸第 619 至 651 行；投掷第 866 至 893 行；三苹果第 900 至 933 行；扇形结算第 1085 至 1113 行；汁液第 936 至 960 行；钻地第 964 至 988 行；天光轰炸第 771 至 837 行；火球第 840 至 861 行；分身第 656 至 676 行；果蛆第 730 至 750 行；虫群第 1116 至 1188 行；爆发帧对齐第 1045 至 1082 行。
- 冲撞自爆三段：第 1 段立即结算，第 2、3 段分别在第 8、16 tick 于同一爆心结算，半径与伤害逐段放大，出处 `entity/AppleBossEntity.java` 第 619 至 651 行。
- 钻地期间本体隐形且无敌，结束时直接传送到目标坐标，出处 `entity/AppleBossEntity.java` 第 485 至 499、964 至 970 行。
- 冲撞自爆光效：判定与伤害不受光效影响，画面上改由 `network/AppleBlastNetwork.java` 同步的短寿命细光环表现 —— 光环寿命 10 tick（0.5 秒）、中心闪光 2 tick（0.1 秒）、32 段（`SEGMENTS`），半径从 0.35 格缓出扩到本次爆炸半径，亮度按剩余寿命二次方淡出；同屏最多 24 个（`MAX_ACTIVE`）、64 格内可见（`VIEW_DISTANCE`）；本体与分身都改走这套，不再生成密集爆炸烟幕；蓄力只在脚下留 4 点提示粒子，冲刺每 tick 只留 1 个白色光点，出处 `util/AppleBlastLightRules.java` 第 5 至 11 行、`entity/AppleBossEntity.java` 第 600、621、653 行、`entity/AppleCloneEntity.java` 第 143 行。

## 召唤物

| 召唤物 | 实体 id | 生命 / 攻击 / 移速 | 寿命（tick） | 单次召唤量（阶段一 / 阶段二） | 命中附带 | 出处 |
|---|---|---|---|---|---|---|
| 果虫 | `ftboss:apple_worm` | 12.0 / 2.0 / 0.30 | 600 | 3 至 5 只 / 5 至 7 只 | 缓慢 70 tick、饥饿 90 tick | `entity/AppleWormEntity.java` 第 32、44 至 47、61 至 62 行 |
| 果蛾 | `ftboss:apple_moth` | 8.0 / 1.5 / 0.36 | 600 | 5 至 7 只 / 7 至 9 只 | 虚弱 100 tick；无重力飞行，绕目标 1.5 格半径盘旋 | `entity/AppleMothEntity.java` 第 33、49 至 53、71、83 至 86 行 |
| 烂苹果堆 | `ftboss:rotten_apple` | 18.0 / 2.0 / 0.26 | 900 | 3 至 5 只 / 5 至 7 只 | 自爆附带中毒 120 tick、饥饿 140 tick | `entity/RottenAppleEntity.java` 第 35、50 至 53、88 至 90 行 |
| 青苹果 | `ftboss:green_apple` | 16.0 / 1.5 / 0.30 | 600 | 6 至 8 只 / 8 至 10 只 | 中毒 120 tick、饥饿 II 160 tick、虚弱 120 tick | `entity/GreenAppleEntity.java` 第 33、45 至 48、63 至 65 行 |
| 金币人 | `ftboss:coin_man` | 24.0 / 2.5 / 0.26，另有护甲 4.0 | 800 | 3 至 5 只 / 5 至 7 只 | 缓慢 III 140 tick、挖掘疲劳 II 160 tick、定身 15 tick | `entity/CoinManEntity.java` 第 34、49 至 53、68 至 70 行 |
| 巨型果蛆 | `ftboss:giant_grub` | 180.0 / 4.5 / 0.26，另有护甲 6.0、击退抗性 0.8 | 无寿命限制 | 1 条（场上只允许 1 条） | 反胃 220 tick、饥饿 II 300 tick、感染 +2 层（上限 5 层、每层 600 tick）；每 40 tick 给 6 格内玩家饥饿 100 tick | `entity/GiantGrubEntity.java` 第 44、57 至 62、77 至 83、95 至 100 行 |
| 分身 | `ftboss:apple_clone` | 20.0 / 0.0 / 0.28 | 280 | 5 至 7 只 / 7 至 9 只 | 贴身自爆或被打死时自爆（见下） | `entity/AppleCloneEntity.java` 第 64 至 67、102 行 |

- 同类上限：Boss 周围 64 格内每种召唤物最多 10 只；到顶时不召唤，改为给场上同类全体上速度 II、力量 I、抗性 I 各 400 tick，出处 `boss/ai/SummonLimit.java` 第 38 至 42、54 至 56、72 至 74 行。
- 主人死亡或被击败时按 80 格半径清扫全部召唤物（果虫、果蛾、烂苹果堆、青苹果、分身、金币人、巨型果蛆），出处 `boss/ai/SummonSweep.java` 第 29 行；`entity/AppleBossEntity.java` 第 1198 至 1202 行。
- 上表的伤害值都是属性表数值；召唤物打玩家同样吃 `NonManorDamageX2` 的全局 ×2，出处 `boss/ai/NonManorDamageX2.java` 第 39 至 43 行。
- 分身自爆数值：贴身自爆（含寿命到期）半径 5.0 格、基准 28.0，技能内折半再全局 ×2 后中心实收 28.0；被打死时半径 3.5 格、基准 14.0，中心实收 14.0；两者都按距离衰减到 25%、击退 1.3，且为纯视觉爆炸；分身跟随距离 40 格，出处 `entity/AppleCloneEntity.java` 第 67、133 至 157、160 至 163 行。
- 分身冲撞节奏：首次冷却 15 + rand(20) tick，之后 60 + rand(40) tick；蓄力 8 tick，冲刺 30 tick、每 tick 位移 0.95 格；冲刺途中玩家进入 2.5 格（`distanceToSqr` 小于 6.25）、或冲刺结束、或距锁定点小于 1.2 格即自爆，出处 `entity/AppleCloneEntity.java` 第 49、102、108、114 至 129 行。
- 烂苹果堆自爆：生成于本体周围约 1.75 格随机位置；寿命 900 tick；目标进入 2.5 格（`distanceToSqr` 小于 6.25）时自行引爆，被击杀时在死亡流程里引爆，`blasted` 标记保证只炸一次；爆炸威力 `BLAST_RADIUS` = 1.1（`Level.ExplosionInteraction.NONE`，不破坏方块），伤害结算半径 `HURT_RANGE` = 3.4 格；伤害 3.0（字面量已含折半，全局 ×2 后实收 6.0），附带中毒 120 tick、饥饿 140 tick，出处 `entity/RottenAppleEntity.java` 第 35、37 至 38、66、68、72 至 93 行。

## 血条与战斗提示

| 项目 | 数值与规则 | 出处 |
|---|---|---|
| 血条颜色与样式 | 红色 `RED` + 10 段刻度 `NOTCHED_10` | `entity/AppleBossEntity.java` 第 146 至 147 行 |
| 血条可见范围 | 48 格内的存活玩家，进范围加入、出范围移除 | `entity/AppleBossEntity.java` 第 296 至 309 行 |
| 血条进度与名称 | `health / maxHealth` 钳制 0 至 1；名称为实体显示名「苹果爵士·蜜脆」 | `entity/AppleBossEntity.java` 第 146、296 行 |
| 血条注销路径 | `die()`、`remove()`、实体离开世界三条路径都会注销并隐藏 | `entity/AppleBossEntity.java` 第 1194 至 1195、1214 至 1218 行；`boss/BossHandler.java` 第 518 至 519 行 |
| 战斗 BGM | 一阶段《果园教父》，二阶段《果园教父·虫蛀暴走》 | `sound/BossMusic.java` 第 372、469 行；`entity/AppleBossEntity.java` 第 1030 至 1037 行；`assets/ftboss/lang/zh_cn.json` 第 212 至 213 行 |
| 连死怜悯 | 不适用：本 Boss 未登记进 `BossMercy.typeOf` 归因表，玩家连死不会被归因到它 | `boss/ai/BossMercy.java` 第 239 至 253 行 |

## 掉落

| 掉落物 | 数量与概率 | 条件 | 出处 |
|---|---|---|---|
| 附魔金苹果 | 1 至 2 个 | 死亡时直接生成 | `entity/AppleBossEntity.java` 第 1208 至 1209 行 |
| 石像·苹果爵士 `ftboss:statue_apple` | 1 个，必掉 | 真死走中央死亡钩子 | `boss/ai/BossMercy.java` 第 140 至 143、347 至 348 行 |
| 苹果爵士的果梗 `ftboss:apple_stem` | 1 个，必掉 | 真死走中央死亡钩子；下界合金级剑（攻击 +3、攻速 -2.2） | `boss/ai/BossMercy.java` 第 376 至 379 行；`item/FTBossItems.java` 第 152 至 154 行 |
| 金苹果胸针 `ftboss:golden_apple_brooch` | 1 个，必掉（`DROP_CHANCE` = 1.0） | 需玩家击杀、`doMobLoot` 开启，且该只 Boss 未领取过 | `util/BossRelicRules.java` 第 6、19 行；`skill/BossRelicDrops.java` 第 41 至 62 行 |
| 招牌法术卷轴 | 5 张（每种法术 1 张满阶卷轴） | 需玩家击杀、`doMobLoot` 开启、未领取过，且对应法术已启用 | `integ/spells/BossSpellDrops.java` 第 32 至 54 行 |
| 经验 | 140 点 | 正常击杀（`xpReward`） | `entity/AppleBossEntity.java` 第 157 行 |

- 金苹果胸针装备后提供幸运 +2，并附带一条随机词条：生命 +4、护甲 +2、移速 +5%、幸运 +1 四选一，出处 `item/BossRelicItem.java` 第 57、64 至 70 行。
- 5 张卷轴对应法术：金果裁决 `ftboss:gilded_verdict`、金果圣佑 `ftboss:orchard_aegis`、日轮果园 `ftboss:solar_orchard`、审判种子 `ftboss:judgment_seed`、金枝收获 `ftboss:golden_harvest`，出处 `util/BossSpellRules.java` 第 20、27、46 至 48、78 行；中文名出处 `assets/ftboss/lang/zh_cn.json` 第 418、439、608、611、614 行。
- 装 Iron's Spells 时本体会按池轮流施放这 5 个招牌法术：首次在 60 + (实体 id mod 20) tick 后开始，之后每 60 tick 挑一个「已启用且不在冷却中」的法术，单法术冷却取 `max(160, ceil(冷却 × 0.75))`；`BossSpellRules` 里 5 个法术的 `damage` 字段为 20 / 4 / 8 / 24 / 18，`cooldown` 字段为 20 / 28 / 24 / 28 / 20 **秒**（即 400 / 560 / 480 / 560 / 400 tick，实际门槛 300 / 420 / 360 / 420 / 300 tick）；真正结算的基础威力取 `BossSpellTuning.signaturePower` = 36 / 8 / 24 / 40 / 34（辅助类法术不再翻倍，故金果圣佑是 8，其余 ×2）= 72 / 8 / 48 / 80 / 68，之后还要乘施法者的法术强度，出处 `integ/spells/BossSpellCombat.java` 第 76 至 94 行；`integ/spells/BossSpellPacing.java` 第 13 至 14、22 至 24 行；`util/BossSpellRules.java` 第 20、27、46 至 48、56 至 67、94 至 98 行；`integ/spells/BossSpellTuning.java` 第 33 至 53 行。
- 蜕变彩蛋：48 格内玩家同时持有头衔 `farmingtales:sweet_apple_manor` 与【血腥契约】武器时（每 20 tick 检测一次），爵士退场并交接隐藏 Boss 挑战流程，退场前清空自己 80 格内的全部召唤物；退场走 `discard()` 而非被击败，因此不触发石像、武器与遗物掉落，出处 `entity/AppleBossEntity.java` 第 140、344 至 363、687 至 739 行。
