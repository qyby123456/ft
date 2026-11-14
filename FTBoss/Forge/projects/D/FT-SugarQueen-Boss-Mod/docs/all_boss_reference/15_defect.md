# 故障机器人

常规 Boss 第 6 只：`ftboss:defect_core`。核心是悬浮机器人，靠**胸口充能球**（可被玩家拆掉的召唤物）累积并激发技能，另有一套二阶段后循环施放的召唤、爆发与光束机制。

## 实体与判定箱

| 项目 | 数值 | 出处 |
|---|---|---|
| 实体 id | `ftboss:defect_core` | `entity/FTEntities.java` 第 61 行 |
| 中文名 | 故障机器人 | `assets/ftboss/lang/zh_cn.json` 第 130 行 |
| 判定箱宽 × 高 | 2.4 格 × 3.8 格 | `entity/FTEntities.java` 第 62 行 |
| 充能球 id / 判定箱 | `ftboss:charged_orb` / 0.5 格 × 0.5 格 | `entity/FTEntities.java` 第 64 至 65 行 |
| 蛆 id / 判定箱 | `ftboss:maggot_minion` / 0.7 格 × 0.5 格 | `entity/FTEntities.java` 第 67 至 68 行 |
| 召唤方式 | 右键使用召唤物 `ftboss:defect_core`，在视线前方 4 格、玩家高度 + 2.4 处生成 | `item/DefectCoreItem.java` 第 43、46 行 |
| 多只共存 | 允许，物品里没有任何单例闸门 | `item/DefectCoreItem.java` 第 25 行注释 |
| 维度限制 | 只在主世界可召唤 | `boss/SummonGate.java` 第 25、32 行 |
| 重力 / 自然消失 | 无重力纯传送式悬浮；`removeWhenFarAway` 恒为 `false` | `entity/DefectCoreEntity.java` 第 208、217 行 |
| 着火动画 | 不显示火焰图层（火焰伤害照常结算） | `entity/DefectCoreEntity.java` 第 47 行 |

## 基础属性表

| 属性 | 数值 | 出处 |
|---|---|---|
| `MAX_HEALTH` | 404 | `entity/DefectCoreEntity.java` 第 255 行 |
| `ARMOR` | 2.0 | `entity/DefectCoreEntity.java` 第 256 行 |
| `ATTACK_DAMAGE` | 9.0（近战挥击用，未再乘技能半系数） | `entity/DefectCoreEntity.java` 第 257 行 |
| `ATTACK_SPEED` | 1.0 | `entity/DefectCoreEntity.java` 第 258 行 |
| `MOVEMENT_SPEED` | 0.1（`BossWalkingSpeed.BASE_SPEED`；普通陆地寻路结果再 ×2，实际位移走 `teleportTo`，此项基本不生效） | `entity/DefectCoreEntity.java` 第 259 行；`boss/ai/BossWalkingSpeed.java` 第 24、74 行 |
| `FOLLOW_RANGE` | 40.0 | `entity/DefectCoreEntity.java` 第 260 行 |
| `KNOCKBACK_RESISTANCE` | 0.85（`BOSS_RESISTANCE`，只吃 15% 击退） | `entity/DefectCoreEntity.java` 第 261 行；`util/BossKnockback.java` 第 31 行 |
| 经验奖励 | `xpReward` = 120 | `entity/DefectCoreEntity.java` 第 224 行 |
| 索敌与朝向 | 目标为玩家；48 格内最近的非创造非旁观玩家；每 tick 显式写 `yRot`、`yBodyRot`、`yHeadRot` | `entity/DefectCoreEntity.java` 第 251、472 至 496 行 |
| 负面效果清除 | 每 20 tick 清一次全部非增益效果；事件层全局 `DENY` 已停用（`IMMUNE_TO_DEBUFFS` = false），减益实际只能短暂存在 | `entity/DefectCoreEntity.java` 第 406 至 410 行、`boss/BossHandler.java` 第 409 行 |
| 阵营免伤 | 免疫 `ChargedOrbEntity` 直接造成的伤害，也免疫本模组 Boss 阵营成员的伤害 | `entity/DefectCoreEntity.java` 第 189 至 194 行 |
| 最终伤害 300 阈值 | 结算后的实际扣血伤害 > `MAX_FINAL_DAMAGE` = 300.0 时本次血量伤害归零（不是截断；恰好 300 或更低放行；不按原始伤害判断；`/kill` 保留）；原始 500 点规则与旧 `LIMIT_ENABLED` 限伤继续停用；本 Boss 适用，玩家、外部 Boss、鼠群小怪、迷你 Boss、召唤物不适用 | `boss/ai/FinalBossDamageGuard.java` 第 19 行、`mixin/FinalBossDamageMixin.java` 第 14 行 |
| 限伤体系 | `LIMIT_ENABLED` = `false`；登记的反逃课距离 36 格同样未启用 | `boss/ai/BossDamageGate.java` 第 46、82 行 |
| 完全免疫的伤害 | 流血与真实伤害类关键字（`hemorrhage`、`doom` 等）整下无效 | `boss/ai/BossDamageGate.java` 第 120 至 129 行 |
| 伤害写入口径 | 技能字面量已烘焙全局倍率，运行时不再有倍率钩子 | `boss/ai/BossDamageTuning.java` 第 11 至 24 行 |

## 悬浮、移动与基础攻击

| 机制 | 数值与规则 | 出处 |
|---|---|---|
| 悬浮轨道 | 8 字漂移，半径 3.5 格，`hoverPhase` 每 tick +0.03 | `entity/DefectCoreEntity.java` 第 636 至 639 行 |
| 悬浮高度 | 目标脚下 + 1.3 格，再叠正弦 ±0.2 格 | `entity/DefectCoreEntity.java` 第 640 行 |
| 悬浮速度 | 每 tick 最多 0.11 格，距离小于 0.6 格时不动 | `entity/DefectCoreEntity.java` 第 643 至 646 行 |
| 推力特效 | 悬停每 5 tick 一次喷流，位移时每 3 tick；每 20 tick 一圈 8 点下洗气流 | `entity/DefectCoreEntity.java` 第 659 至 675 行 |
| 转身动作 | 每 tick 偏航变化超过 1.2° 判定为转身，持续刷新 6 tick 余量并循环播 `turn_l`（左）/ `turn_r`（右），停下后回 idle | `entity/DefectCoreEntity.java` 第 503 至 512 行 |
| 基础攻击冷却 | 阶段 1 = 60 tick，阶段 2 = 48 tick，阶段 3 = 36 tick | `entity/DefectCoreEntity.java` 第 541 行 |
| 贴脸挥击 | 距离小于 3.4 格，6 tick 后结算 `doHurtTarget`（= 9.0 攻击力） | `entity/DefectCoreEntity.java` 第 544 至 548 行 |
| 远程低环铁魔法 | 距离小于 26 格，低环法术 7 选 1，等级 = `3 + 阶段`（回想形态再 +2），上限为该法术 `maxLevel` | `boss/ai/DefectOrbs.java` 第 211 至 236 行 |
| 远程回退伤害 | 没装铁魔法时改为直接魔法伤害 `4.5 + 阶段 × 1.5`（该字面量已含烘焙倍率，阶段 1/2/3 实伤 = 6.0 / 7.5 / 9.0），26 格内且需视线 | `entity/DefectCoreEntity.java` 第 526 至 558 行 |
| 施法动作锁定 | `castTicks`、`summonTicks`、`phaseAnimTicks`、`echoAnimTicks`、`beamAnimTicks`、`exhaustedTicks` 任一大于 0 时不再起手新动作 | `entity/DefectCoreEntity.java` 第 157 至 160 行 |

## 充能球系统

### 球种、权重、存活时间与激发前摇

| 球种 | `id` | 定位 | 权重 阶段 1 | 权重 阶段 2 | 权重 阶段 3 | 基础存活 tick | 激发前摇 tick |
|---|---|---|---|---|---|---|---|
| 玻璃球 | `glass` | AOE | 25 | 20 | 15 | 400 | 11 |
| 电球 | `lightning` | 单体 | 25 | 22 | 20 | 400 | 9 |
| 冰球 | `frost` | 保护 | 20 | 20 | 18 | 400 | 6 |
| 离子球 | `ion` | 强化 | 18 | 20 | 22 | 400 | 6 |
| 黑球 | `dark` | 蓄力 | 12 | 18 | 25 | 640 | 16 |

- 球种与中文名出处：`entity/OrbType.java` 第 16 至 20 行；权重出处：`boss/ai/DefectOrbs.java` 第 699 至 704 行。
- 基础存活时间出处：`entity/ChargedOrbEntity.java` 第 64 行（`LIFE` = 400）；黑球额外 240 tick 出处：`boss/ai/DefectOrbs.java` 第 677 至 678 行。
- 实际存活 tick = `LIFE` + （阶段 − 1）× 80 + 黑球 240：阶段 1 为 400 / 黑球 640，阶段 2 为 480 / 720，阶段 3 为 560 / 800。
- 激发前摇出处：`util/OrbLinkMath.java` 第 21 至 29 行；调用点 `boss/ai/DefectOrbs.java` 第 753 至 755 行。

### 球槽与补球

| 项目 | 数值与规则 | 出处 |
|---|---|---|
| 球槽 | `SLOTS_BASE` = 5、每次突破 `SLOTS_PER_PHASE` = 3、上限 `SLOTS_MAX` = 11，即阶段 1/2/3 = 5 / 8 / 11 | `boss/ai/DefectOrbs.java` 第 40 至 44、454 行 |
| 补球周期 | `CYCLE` = 48 tick；回想形态 `ECHO_CYCLE` = 80 tick | `boss/ai/DefectOrbs.java` 第 46 至 48 行 |
| 拆球惩罚 | 被拆后 `PUNISH_CD` = 40 tick 内不补球（只挡补球，不挡激活） | `boss/ai/DefectOrbs.java` 第 50、634 行 |
| 单周期行为 | 先补齐全部空槽，再吸收并激活一颗已有球；按成熟度比例 `chargeTicks / lifeTicks` 最大者优先 | `boss/ai/DefectOrbs.java` 第 626 至 653 行 |
| 生成位置 | Boss 脚下 + 1.6 格，水平半径 1.4 格，按槽位错开角度 | `boss/ai/DefectOrbs.java` 第 673 至 674 行 |
| 施法排队上限 | 待结算队列最多 11 条 | `boss/ai/DefectOrbs.java` 第 488 行 |
| 拆球后果 | 不激发技能，球直接消散并触发停补 | `entity/ChargedOrbEntity.java` 第 330 至 336 行 |
| Boss 死亡 | 一次性清掉 64 格内自己名下的所有球 | `boss/ai/DefectOrbs.java` 第 875 至 888 行 |

### 环绕、链接与确定性曲线

- 每颗球独立参数：环绕半径 2.3 至 3.5 格、上下摆幅 0.2 至 0.7 格、旋向随机，出处 `entity/ChargedOrbEntity.java` 第 270 至 276 行。
- 出生动画 10 tick：从 `orbAnchor(true)`（掌心，前方 1.25 格、高 2.05 格）弧线飞到编队点，出处 `entity/ChargedOrbEntity.java` 第 74、494 至 503 行。
- 光环锚点 `orbAnchor`：掌心位前方 1.25 格 / 高 2.05 格，默认位前方 0.34 格 / 高 2.1 格，出处 `entity/DefectCoreEntity.java` 第 147 至 151 行。
- 环绕：角速度 `+= 0.045 × 速度倍率 × 旋向`，高度 = Boss 脚下 + 2.2 格 + 正弦摆幅，限速 0.38 格/tick，出处 `entity/ChargedOrbEntity.java` 第 575 至 584 行。
- 螺旋收束：速度倍率 1.35、`pulse` = 0.8，半径收束到 20% 至 100%，出处 `entity/ChargedOrbEntity.java` 第 556 行。
- 后撤蓄力：退到 Boss 背离目标一侧 3.2 格、高度 Boss 脚下 + 2.3 格，限速 0.85 格/tick；回旋归位飞向脚下 + 1.6 格，限速 0.55 格/tick，出处 `entity/ChargedOrbEntity.java` 第 564 至 594 行。
- 光束期间链接成环：角度 = 槽位角 + `tickCount × 0.018`，引导中半径 2.7 格、开火中半径 1.9 格，高度脚下 + 2.65 格 ± 0.25 格，限速 0.45 格/tick，出处 `entity/ChargedOrbEntity.java` 第 540 至 545 行。
- 消散吸收阶段每 tick 按 `position().lerp(orbAnchor(false), 0.45)` 插值飞向胸前默认锚点；聚焦阶段按 `OrbLinkMath.arc`（y 段抬升 0.4）沿弧线飞到掌心锚点，出处 `entity/ChargedOrbEntity.java` 第 465 至 478 行。
- `OrbLinkMath`：`progress` = `clamp(elapsed / max(1, duration), 0, 1)`；`smooth` = `p² × (3 − 2p)`；`arc` = `start + (end − start) × smooth(p) + sin(p × π) × lift`，出处 `util/OrbLinkMath.java` 第 7 至 19 行。

### 球的撞击（DASH）攻击

| 项目 | 数值 | 出处 |
|---|---|---|
| 冲刺速度 `DASH_SPEED` | 1.35 格/tick | `entity/ChargedOrbEntity.java` 第 201 行 |
| 撞击伤害基数 / 每阶段追加 | `SLAM_DMG_BASE` = 12，`SLAM_DMG_PER_PHASE` = 6 | `entity/ChargedOrbEntity.java` 第 203 至 204 行 |
| 撞击伤害公式 | `SLAM_DMG_BASE + 阶段 × 6 + slamBonus`，再 × 球伤害倍率，再 × 0.5 全局烘焙 | `entity/ChargedOrbEntity.java` 第 705 行 |
| 撞击实测值（普通球，无黑球加成） | 阶段 1 = 9.0，阶段 2 = 12.0，阶段 3 = 15.0 | 由第 705 行公式推算 |
| 伤害类型拆法 | 物理 1/4 + 魔法 1/4（两段各 0.25 系数，合计 0.5） | `entity/ChargedOrbEntity.java` 第 709 至 711 行 |
| 击退 / 玻璃球溅射 | `knockback(1.5, 沿冲刺方向)`；目标 4 格内其它玩家吃 0.15 系数两段并击退 0.9 | `entity/ChargedOrbEntity.java` 第 712 至 722 行 |
| 撞后冷却 `SLAM_CD` | 100 tick；出场首次撞击延迟 `slamCd` = 20 至 79 tick 随机 | `entity/ChargedOrbEntity.java` 第 206、274、747 行 |
| 触发概率 / 距离 | 玻璃球、电球、黑球 = 0.5；冰球、离子球 = 0.16；距离 26 格内 | `entity/ChargedOrbEntity.java` 第 598、616 至 621 行 |
| 蓄力预警 `WINDUP` | 12 tick（回想形态第二撞为 8 tick） | `entity/ChargedOrbEntity.java` 第 601、688 行 |
| 冲刺距离与命中判定 | 飞 `min(28, 到目标距离 + 6)` tick；途中每 tick 检测，目标判定箱膨胀 1.0 格（电球 1.6 格） | `entity/ChargedOrbEntity.java` 第 638、680 至 683 行 |
| 电球追踪 / 撞墙 | 电球每 tick 向目标修正 0.5 方向，其它球种直线可躲；撞到方块则弹回环绕 | `entity/ChargedOrbEntity.java` 第 650 至 674 行 |
| 回想形态双撞 | 冲刺结束后再补一次蓄力 8 tick 的撞击 | `entity/ChargedOrbEntity.java` 第 603、684 至 692 行 |
| 黑球撞击黑洞 | 满充能黑球命中时 5% 概率释放 `irons_spellbooks:black_hole`（等级 4） | `entity/ChargedOrbEntity.java` 第 701 至 704 行 |
| 充能球自身属性 | `MAX_HEALTH` = 20.0、`ATTACK_DAMAGE` = 1.0（仅补表防崩）、`MOVEMENT_SPEED` = 0.0、`KNOCKBACK_RESISTANCE` = 1.0、`FOLLOW_RANGE` = 48.0、`xpReward` = 0 | `entity/ChargedOrbEntity.java` 第 229、245 至 252 行 |
| 充能球被拆 / 消散 | 任何伤害来源都能一击打掉（含环境伤害）；淡出 6 tick，吸收用 `drain`、被打掉用 `burst` | `entity/ChargedOrbEntity.java` 第 75、330 至 336、379 至 403 行 |

## 技能表（充能球激发）

- 通用规则：吸收时从 Boss 位置（脚下 + 1.6 格）结算，并播该球种专属施法动画（玻璃球 23 tick、黑球 26 tick、其余 20 tick），出处 `entity/DefectCoreEntity.java` 第 279 至 284 行。
- 伤害列括号内为**已含全局 0.5 烘焙系数后的玩家实际承受值**；设计值再乘技能半系数 0.5。

| 技能 | 触发与冷却 | 前摇 | 范围 | 伤害 | 附带 |
|---|---|---|---|---|---|
| 玻璃球·范围法术 | 玻璃球成熟被激活 | 11 tick | 以 Boss 为中心 8 格内全部玩家 | 铁魔法 AOE 池；回退 20 点设计值（实伤 10，物理 / 魔法各半） | 缓慢 I 持续 60 tick；音效 `GLASS_BREAK` |
| 电球·单体雷击 | 电球成熟被激活，优先挑【雷击】【雷暴】 | 9 tick | 28 格内最近玩家 | 铁魔法单体雷系；回退 30 点设计值（实伤 15，物理 / 魔法各半） | 缓慢 III 持续 50 tick；音效 `LIGHTNING_BOLT_IMPACT` |
| 黑球·蓄力撞击 | 黑球成熟被激活，不走铁魔法 | 16 tick | 发射后直线撞击，方向取 28 格内最近玩家 | 撞击公式同球撞击，额外 + 蓄力秒数 × 2 | 满充能命中 5% 放黑洞；音效 `WITHER_SHOOT` |
| 冰球·保护 | 冰球成熟被激活 | 6 tick | 自身 | 无直接伤害；放冰系铁魔法（可打向玩家） | 保底吸收 I 持续 200 tick |
| 离子球·强化 | 离子球成熟被激活 | 6 tick | 自身 | 无直接伤害；放增益类铁魔法 | 充能 +12；速度 II 持续 260 tick |

- 出处：玻璃球回退 `boss/ai/DefectOrbs.java` 第 800 至 817 行；电球回退第 818 至 837 行；黑球改撞击第 762 至 796 行；冰球保底霜甲第 860 至 864 行；离子球充能与速度第 865 至 870 行。
- 铁魔法等级：玻璃 / 电 / 冰 / 离子 = `4 + 阶段 + 回想加成 2 + min(充能 / 4, 4)`；黑球再加 `2 + min(蓄力 tick / 80, 5)`；均以该法术 `maxLevel` 封顶，出处 `boss/ai/DefectOrbs.java` 第 169 至 170 行。
- 黑球完全不走铁魔法：`releaseCast` 进入 switch 前就对 `DARK` 提前 `return`，改走 `launchDarkOrb`（新建一颗球、`setSlamBonus` = 蓄力秒数 × 2、`setBlackHoleChance(true)`、`lifeTicks` = 200），出处 `boss/ai/DefectOrbs.java` 第 792 至 796 行；switch 内那段黑球回退（`16 + min(蓄力 tick / 14, 30)` 设计值、附反胃 120 tick，第 839 至 852 行）因此不会被执行。

## 技能表（核心自身技能）

| 技能 | 触发与冷却 | 前摇 | 范围 | 伤害 | 附带 |
|---|---|---|---|---|---|
| 超能光束 | 冷却 240 tick 起步；结束后 阶段 1 = 360、阶段 2 = 280、阶段 3 = 200 tick，回想形态再 × 0.7 | 蓄力 160 tick，期间站立不动并出红色预警线与收缩红环 | 长 40 格、判定半径 3.5 格 | 开火瞬间立即结算 1 次，之后引导期内每 10 tick 一次（`BEAM_PULSE` = 10，60 tick 内再 6 次），实际共 7 次脉冲；设计 22 / 脉冲（实伤 11 / 脉冲，约 22 / 秒、3 秒合计约 77） | 四段伤害：物理 / 魔法 / 虚空 / 真实，每段 = `BEAM_DMG` / 8（即总伤害减半后均分四份）；沿射线推 0.32 格；结束时进入落地衰弱 |
| 闪电打击（充能满） | 充能 ≥ 6 即触发，可连续结算多段 | 0 tick | 32 格内最近玩家脚下 | `12 + 阶段 × 6` 设计值，实伤 = 阶段 1 = 9、阶段 2 = 12、阶段 3 = 15 | 纯视觉闪电不点火；场上所有电球立刻进入扑击；播电球施法手势 |
| 电流相生 | 阶段 ≥ 2，冷却 600 tick；需已激发电球数 ≥ 2 | 落雷前播手势 | 落点散布在 Boss 周围 ±3 格，命中半径 `sqrt(12)` 约 3.46 格 | `12 + 阶段 × 6`（闪电伤害，不再乘 0.5 烘焙）→ 阶段 2 = 24、阶段 3 = 30 | 落雷道数 = `min(已用电球数, 12)`；施放后自身进入 100 tick 虚弱并回复 10% 最大生命 |
| 集中碎片 | 阶段 ≥ 2 时整场只放一次 | 播离子球施法动作 | 全场生效 | 无直接伤害 | 全部球伤害 × 1.5、蛆召唤基数升到 5、电流相生冷却压到 200 tick |
| 召唤【！?区区？！】 | 阶段 ≥ 2，每 240 tick 一次 | 无前摇 | 以 Boss 为中心半径 2 格散布 | 无直接伤害 | 召唤 2 至 3 只蛆，集中碎片后为 5 至 6 只 |
| 常驻基础攻击 | 冷却 阶段 1 = 60、阶段 2 = 48、阶段 3 = 36 tick | 6 tick（挥击）/ 9 tick（远程回退） | 3.4 格内挥击；26 格内远程 | 挥击 9.0；远程回退 4.5 + 阶段 × 1.5 | 远程优先走铁魔法低环池，没装铁魔法才回退直伤 |

- 出处：超能光束状态机与参数 `boss/ai/DefectOrbs.java` 第 276 至 293、910 至 1008 行；引导期间每 tick 重新对准 40 格内最近玩家，第 912 至 921 行；蓄力期剥防御并给射线内玩家加速 I（持续 `BEAM_CHARGE + 30` tick），第 976 至 989 行。
- 出处：闪电打击 `boss/ai/DefectOrbs.java` 第 389 至 416 行；电流相生第 314 至 317、1137 至 1176 行；集中碎片第 565 至 577 行；召唤蛆第 578 至 594 行。

## 被动：球的环绕增益

球存活时每 20 tick 刷新一次被动，出处 `boss/ai/DefectOrbs.java` 第 597 至 601 行。

| 球种 | 被动效果 | 数值细节 | 出处 |
|---|---|---|---|
| 玻璃球 | 给核心充能 | 每颗 +1，再 × 当前阶段 | `boss/ai/DefectOrbs.java` 第 351 行 |
| 电球 | 给核心充能 | 每颗 +2，再 × 当前阶段 | `boss/ai/DefectOrbs.java` 第 351 行 |
| 冰球 | 薄霜甲 + 抗火 | 吸收等级 `min((颗数 − 1) / 2 + (阶段 − 1) / 2, 1)`（上限吸收 II）持续 60 tick；抗火 I 持续 90 tick | `boss/ai/DefectOrbs.java` 第 364 至 369 行 |
| 离子球 | 一批进攻 / 功能增益 | 等级 `min(颗数 − 1 + (阶段 − 1), 3)`，全部持续 90 tick：速度、急迫、跳跃、幸运、饱和、抗火、夜视、水下呼吸、海豚恩惠、缓降、导管能量 | `boss/ai/DefectOrbs.java` 第 370 至 380 行 |
| 黑球 | 无被动 | 只贡献蓄力等级与高稀有度法术 | `boss/ai/DefectOrbs.java` 第 345 行 |
| 充能条（全球种） | 上限与闪电消耗 | `CHARGE_MAX` = 60，一次打击消耗 `CHARGE_PER_STRIKE` = 6，并抬高施法等级 `min(charge / 4, 4)` | `boss/ai/DefectOrbs.java` 第 123、321 至 323 行 |

## 阶段与转换

| 项目 | 阶段 1 | 阶段 2 | 阶段 3 | 出处 |
|---|---|---|---|---|
| 进入阈值 | 初始 | 血量比例 < 0.70 | 血量比例 < 0.35 | `entity/DefectCoreEntity.java` 第 52 至 53、602 至 605 行 |
| 血条后缀 | `[阶段 I]` | `[阶段 II]` | `[阶段 III]` | `entity/DefectCoreEntity.java` 第 570 至 571、590 至 596 行 |
| 球槽 | 5 | 8 | 11 | `boss/ai/DefectOrbs.java` 第 454 行 |
| 基础攻击冷却 | 60 tick | 48 tick | 36 tick | `entity/DefectCoreEntity.java` 第 541 行 |
| 超能光束冷却 | 360 tick | 280 tick | 200 tick | `boss/ai/DefectOrbs.java` 第 1006 行 |
| 蛆召唤 | 未开放 | 每 240 tick | 每 240 tick | `boss/ai/DefectOrbs.java` 第 579 行 |
| 电流相生 | 未开放 | 开放，冷却 600 tick | 开放，冷却 600 tick | `boss/ai/DefectOrbs.java` 第 1138 行 |
| 集中碎片 | 未开放 | 首次进入即触发一次 | 已用尽 | `boss/ai/DefectOrbs.java` 第 565 行 |
| 回想形态 | 关闭 | 开启 | 开启 | `boss/ai/DefectOrbs.java` 第 604 行 |
| 突破演出 | — | 播 `phase_shift` 动画 52 tick + 音效 + 48 格内提示 | 同阶段 2，音调更低 | `entity/DefectCoreEntity.java` 第 606 至 616 行 |
| 阶段只能前进 | 回血不会让阶段回退 | 同 | 同 | `entity/DefectCoreEntity.java` 第 605 行 |

### 回想形态

| 项目 | 效果 | 出处 |
|---|---|---|
| 开启条件 | 狂暴层级 ≥ 1（血量 ≤ 50%）或 阶段 ≥ 2 | `boss/ai/DefectOrbs.java` 第 604 行 |
| 补球周期 | 周期参数取 `ECHO_CYCLE` = 80 tick | `boss/ai/DefectOrbs.java` 第 48、622 行 |
| 法术双发 | 第一发后 16 tick 原样复读同一法术（等级与目标相同） | `boss/ai/DefectOrbs.java` 第 187 至 188、620 行 |
| 等级与稀有度 | 法术等级 +2；优先挑传说（`minRarity` = 4） | `boss/ai/DefectOrbs.java` 第 122、127 至 128 行 |
| 球撞击 | 触发双撞 | `entity/ChargedOrbEntity.java` 第 603 行 |
| 开启演出 | 播 `echo` 动画 28 tick + 音效 `WARDEN_SONIC_BOOM` | `boss/ai/DefectOrbs.java` 第 607 至 611 行 |
| 狂暴增益 | 纯进攻档：每 20 tick 刷新一次迅捷（狂暴 I = 迅捷 I、狂暴 II = 迅捷 II）；狂暴 II 起追加力量 II，始终不给抗性与再生 | `boss/ai/BossRage.java` 第 84 至 95 行 |

## 落地衰弱与过载状态

| 状态 | 触发 | 时长 | 效果 | 出处 |
|---|---|---|---|---|
| 落地衰弱（光束后） | 超能光束引导结束 | `EXHAUST_TICKS` = 120 tick（6 秒） | 强制落地、不出手、不补球、不放技能 | `entity/DefectCoreEntity.java` 第 88 行；`boss/ai/DefectOrbs.java` 第 559、937 行 |
| 落地衰弱（电流相生后） | 电流相生施放完成 | `CURRENT_EXHAUST` = 100 tick（5 秒） | 同上，另回复 10% 最大生命 | `boss/ai/DefectOrbs.java` 第 316、1173 行 |
| 削弱期回血与落地 | 每 5 tick 回一次血；每 tick 下压贴地 | 覆盖整段衰弱 | 默认总回血 80 点、按比例版本为最大生命 × 10%；每 tick 最多下压 0.9 格，速度归零 | `entity/DefectCoreEntity.java` 第 315、409 至 427 行 |
| 削弱期剥防御 | 进入时 | 立即 | 移除抗性提升、伤害吸收、抗火、生命提升 | `entity/DefectCoreEntity.java` 第 316 至 322 行 |
| 回升 | 衰弱结束时 | 立即 | 音效 `BEACON_POWER_SELECT`、播 `phase_shift`、48 格内提示 | `entity/DefectCoreEntity.java` 第 428 至 436 行 |
| 蓄力期无法防御 | 超能光束蓄力 160 tick 内 | 160 tick | 剥掉全部防御增益、原地不动、不出手 | `boss/ai/DefectOrbs.java` 第 977 至 983 行 |
| 机能紊乱 | 技能计数每满 3 次本可触发 | — | 计数后直接返回，实际不会触发 | `boss/ai/DefectOrbs.java` 第 1094 至 1109 行 |
| 死亡超载爆炸 | `die()` | 一次性 | 清球、清蛆后原地爆炸：威力 `OVERLOAD_POWER` = 4.0，`ExplosionInteraction.NONE` 只伤实体不毁地形 | `entity/DefectCoreEntity.java` 第 683 至 692 行 |

## 血条与战斗提示

| 项目 | 数值与规则 | 出处 |
|---|---|---|
| 血条颜色与样式 | 紫色 `PURPLE` + `PROGRESS` 分段 | `entity/DefectCoreEntity.java` 第 211 行 |
| 血条可见范围 | 48 格内且同维度的存活玩家 | `entity/DefectCoreEntity.java` 第 574 至 587 行 |
| 血条进度与名称 | `health / maxHealth` 钳制 0 至 1；名称为 `boss.ftboss.defect_core` 加阶段罗马数字后缀 | `entity/DefectCoreEntity.java` 第 569 至 571 行 |
| 血条注销路径 | `die()`、`remove()`、非存活分支都会注销 | `entity/DefectCoreEntity.java` 第 226 至 234、565 至 568 行 |
| 战斗 BGM | 每 tick 主动 `BossMusic.request`，音轨组 `DEFECT` | `entity/DefectCoreEntity.java` 第 441 行；`sound/BossMusic.java` 第 366 行 |
| 动作栏播报过滤 | 只显示含「光束 / 虚弱 / 过载 / 衰弱」的提示，球的产生与使用不打扰玩家 | `boss/ai/DefectOrbs.java` 第 1195 至 1203 行 |
| 连死怜悯 | 同一玩家在同一只 Boss 下连续死亡 5 次 → 该只清球后 `discard()`，无掉落无经验 | `boss/ai/BossMercy.java` 第 44、120 至 124、308 至 310 行 |

## 召唤物

| 召唤物 | 实体 id | 生命 | 攻击 | 移动速度 | 追随范围 | 存活 | 出处 |
|---|---|---|---|---|---|---|---|
| 充能球 | `ftboss:charged_orb` | 20.0 | 1.0（不走近战） | 0.0 | 48.0 | 400 至 800 tick（按阶段与球种） | `entity/ChargedOrbEntity.java` 第 245 至 252 行 |
| 【！?区区？！】蛆 | `ftboss:maggot_minion` | 12.0 | 4.0 | 0.32 | 32.0 | 600 tick（30 秒） | `entity/MaggotMinionEntity.java` 第 49、68 至 74 行 |

- 蛆只吃玩家造成的伤害，其它来源一律免疫，出处 `entity/MaggotMinionEntity.java` 第 128 至 132 行。
- 蛆每次命中叠一层【感染】：上限 `MAX_STACKS` = 10 层，每层 `STACK_TICKS` = 600 tick；效果为「攻击敌人时受到层数点伤害」，出处 `entity/MaggotMinionEntity.java` 第 51 至 54、113 至 125 行。
- Boss 死亡时清理 80 格内自己名下的全部蛆，出处 `entity/DefectCoreEntity.java` 第 684 至 687 行；`boss/ai/SummonSweep.java` 第 29 行。
- 充能球在 Boss 死亡或归属丢失时一起消散，出处 `entity/ChargedOrbEntity.java` 第 459 至 462、514 至 516 行。

## 掉落

| 掉落物 | 数量与概率 | 条件 | 出处 |
|---|---|---|---|
| 经验 | 120 点 | 正常击杀（`xpReward`） | `entity/DefectCoreEntity.java` 第 207 行 |
| 石像·故障机器人 | 1 个，必掉 | 死亡钩子统一发放 | `boss/ai/BossMercy.java` 第 351 至 354 行 |
| 充能机械爪 `ftboss:defect_claw` | 1 个，必掉 | 死亡钩子统一发放 | `boss/ai/BossMercy.java` 第 385 至 388 行 |
| 蓄能核心坠 `ftboss:charged_core_pendant` | 1 个，必掉（`DROP_CHANCE` = 1.0） | 需玩家击杀、`doMobLoot` 开启且未领取过 | `util/BossRelicRules.java` 第 6、17、46 至 48 行；`skill/BossRelicDrops.java` 第 52 至 62 行 |
| Boss 专属铁魔法卷轴 | 5 个技能各 1 张满阶卷轴 | 需玩家击杀、`doMobLoot` 开启且未领取过 | `integ/spells/BossSpellDrops.java` 第 45 至 55 行 |
| 掉落表本身 | 不存在 `loot_tables/entities/defect_core.json` | — | `src/main/resources/data/ftboss/loot_tables/entities/` 目录列举 |

- 5 张卷轴与中文名出处：`ftboss:capacitor_overload`（电容过载）、`ftboss:repulsor_discharge`（斥力放电）、`ftboss:ion_rail`（离子轨道炮）、`ftboss:static_lattice`（静电囚网）、`ftboss:emergency_reboot`（紧急重启），`util/BossSpellRules.java` 第 18、25、40 至 42、76 行；`assets/ftboss/lang/zh_cn.json` 第 433、412、590、593、596 行。
- 卷轴发放的等级取该法术的 `getMaxLevel()`，出处 `integ/spells/BossSpellDrops.java` 第 53 行。
