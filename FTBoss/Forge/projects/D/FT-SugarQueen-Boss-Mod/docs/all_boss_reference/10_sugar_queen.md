# 糖霜女皇（姜饼王）

常规 Boss 第 1 只：`ftboss:sugar_queen`。下文全部数值取自当前源码，时间单位 tick（1 秒 = 20 tick），距离单位「格」。

## 一、实体与召唤

| 项目 | 数值 / 内容 | 出处 |
|---|---|---|
| 实体 id | `ftboss:sugar_queen` | `entity/FTEntities.java:40-41` |
| 生物分类 / 客户端追踪 | `MONSTER` / `clientTrackingRange(10)` | `entity/FTEntities.java:179-192` |
| 判定箱 | 宽 1.2 格、高 2.7 格 | `entity/FTEntities.java:41` |
| 召唤入口 | `ftboss:sugar_queen_crystal`（糖霜水晶，堆叠 16）右键，成功则消耗 1 个 | `item/SugarQueenCrystalItem.java:22,29-30` |
| 维度限制 | 仅主世界可召唤（`OVERWORLD_ONLY` = true） | `boss/SummonGate.java:25,32` |
| 本体落点 | 玩家视线方向 3 格外，Y = 玩家 Y + 2.0 | `boss/SugarQueenBoss.java:126-136` |
| 本体通用设定 | `setNoGravity(true)`、`setSilent(true)`、名称「糖霜女皇」（淡紫 + 粗体）、强制持久化 | `boss/SugarQueenBoss.java:129-136` |
| 重复召唤 | 已有一只本体在战斗时，再次使用水晶改为生成「复制体」 | `boss/SugarQueenBoss.java:88-121` |
| 复制体 | 名称后缀「·复制体」、血量 360（`MAX_HP`）、无重力、落点玩家视线 3 格外且 Y + 2.0，拥有独立血条 | `boss/SugarQueenBoss.java:95-104` |
| 血条 | `ServerBossEvent`，粉色 + PROGRESS；本体与复制体各一条；本体血条可见半径 `BAR_RANGE` = 48.0 格 | `entity/SugarQueenEntity.java:97,172-196,205-215` |
| 血量提示 | 每 10 tick 向 50 格内玩家发动作栏「糖霜女皇 当前血量 / 360」（`DESPAWN_RANGE` = 50） | `boss/SugarQueenBoss.java:48,276-284` |
| 战斗 BGM | 40 格内出现玩家后才向 `BossMusic` 登记（`BossMusic.request`），退场时释放（`BossMusic.release`） | `entity/SugarQueenEntity.java:247-272`；`sound/BossMusic.java:544,667` |

## 二、基础属性与全局修正

| 属性 / 项目 | 基础值 | 全局修正 | 出处 |
|---|---|---|---|
| `MAX_HEALTH` | 360.0（`MAX_HP` = 360） | 场上人数 n 时 × 1.25^(n-1)（`HP_PER_PLAYER`） | `entity/SugarQueenEntity.java:344`；`boss/SugarQueenBoss.java:47`；`boss/ai/BossScaling.java:24,54` |
| `ARMOR` | 2.5 | + 4.0（`ARMOR_ADD`） | `entity/SugarQueenEntity.java:345`；`boss/ai/BossEmpower.java:29,59` |
| `ATTACK_DAMAGE` | 1.5 | × (1 + 0.15 × (n-1))（`DMG_PER_PLAYER`），再 × (1 + `ATTACK_MUL` = 0.30) | `entity/SugarQueenEntity.java:346`；`boss/ai/BossScaling.java:26,55,72`；`boss/ai/BossEmpower.java:28,58` |
| `MOVEMENT_SPEED` | 0.1（`BossWalkingSpeed.BASE_SPEED`）；普通陆地寻路结果再 ×2 | × (1 + `SPEED_MUL` = 0.25) | `entity/SugarQueenEntity.java:347`；`boss/ai/BossWalkingSpeed.java:24,74`；`boss/ai/BossEmpower.java:27,57` |
| `KNOCKBACK_RESISTANCE` | 0.85（`BOSS_RESISTANCE`，只吃 15% 击退） | 强化包不再叠加（`KB_ADD` = 0.0） | `entity/SugarQueenEntity.java:348`；`util/BossKnockback.java:31`；`boss/ai/BossEmpower.java:30,60` |
| `FOLLOW_RANGE` | 40.0 | — | `entity/SugarQueenEntity.java:349` |
| 人数统计口径 | `RADIUS` = 48 格内、存活且非创造 / 非旁观，每 20 tick 重算一次 | — | `boss/ai/BossScaling.java:22,38-40,45` |
| 常驻增益 | 每 400 tick 续一次、持续 `EFFECT_TICKS` = 520 tick：速度 I、抗性 I、火抗 I、力量 II | — | `boss/ai/BossEmpower.java:31,114-120` |
| 索敌 | 仇恨表最高者优先，否则 30 格内最近的有效玩家 | — | `boss/SugarQueenBoss.java:176`；`boss/ai/BossTargets.java:40-44` |
| 负面效果免疫（事件层已关闭） | Forge 事件层全局 `DENY` 已停用（`IMMUNE_TO_DEBUFFS` = false，v176 起）；本体仍每 20 tick 自清全部非增益效果，实际负面状态只能短暂存在 | — | `boss/BossHandler.java:409`、`entity/SugarQueenEntity.java:327-335` |
| 单次最终伤害限伤 | 结算后的实际扣血伤害 > `MAX_FINAL_DAMAGE` = 300.0 时本次血量伤害归零（不是截断；恰好 300 或更低放行；不按原始伤害判断） | `/kill` 等绕过无敌的伤害保留；原始 500 点规则与旧 `LIMIT_ENABLED` 限伤继续停用；本 Boss 适用，玩家、外部 Boss、鼠群小怪、迷你 Boss、召唤物不适用 | `boss/ai/FinalBossDamageGuard.java:19`、`mixin/FinalBossDamageMixin.java:14` |
| 传奇怪物式限伤 | 已关闭（`LIMIT_ENABLED` = false）；已登记的 `DAMAGE_CAP` 58.0 单次上限与 `ANTI_CHEESE` 32.0 反逃课距离当前不生效 | — | `boss/ai/BossDamageGate.java:46,66,77,167,249-256` |
| 完全免疫的伤害类型 | 11 个流血 / 末日类 id：`hemorrhage`、`blood_loss`、`bloodloss`、`bleed`、`bleeding`、`doom`、`impending_doom`、`executioner`、`assassination`、`desecration`、`true_damage` | — | `boss/ai/BossDamageGate.java:120-129,158-161` |

## 三、悬浮与位移

- 本体全程 `setNoGravity(true)`，移动不由寻路 AI 驱动，而是每 tick 直接 `teleportTo` 逐帧修正（`boss/SugarQueenBoss.java:135,196-197,223-224`）。
- 环绕悬停：以当前目标为圆心、半径 6.5 格，高度 = 目标 Y + 2.0 + sin(tick × 0.05) × 0.35；每 tick 最多朝悬停点移动 `0.28` 格，距悬停点 < 0.8 格时不动；环绕相位 `flyA` 每 tick + 0.02（`boss/SugarQueenBoss.java:75,215-225`）。
- 朝向：每 tick 用 `facePlayer` 把身体与头部朝向目标（含俯仰角），保证铁魔法法术方向正确（`boss/SugarQueenBoss.java:187,375-383`）。
- 目标丢失（`BossTargets.combatTarget` 返回 null）时本体整个 tick 直接返回：不位移、不施法、不召唤、不结算光环（`boss/SugarQueenBoss.java:176-177`）。
- 冲刺期间**不走环绕**：整段冲刺接管位移（每 tick 朝目标直线挪 0.95 格），环绕相位与悬停点当帧不更新（`boss/SugarQueenBoss.java:189-204,215-225`）。
- 复制体走另一套悬停：贴近 32 格内最近的有效玩家，悬停点为目标上方 2.2 格，每 tick 最多 0.25 格，距离 ≤ 2.0 格时停止（`entity/SugarQueenEntity.java:217-229`）。

## 四、主动技能

| 技能 | 触发与冷却 | 前摇 / 持续 | 范围 | 伤害 | 附带效果 | 出处 |
|---|---|---|---|---|---|---|
| 权杖砸击 | 任意近战行为调用 `doHurtTarget` 时启动，无独立冷却字段；上一段动作未结束时不再起手（`BossActionState.active()` 闸） | 前摇 `impactTick` = 5 tick，动作总长 14 tick | 3.2 格，且需视线 | 取 `ATTACK_DAMAGE` 属性值（基础 1.5，叠加上表的全局修正），不是字面常量 | `PLAYER_ATTACK_SWEEP` 音效 + 命中处 6 个 CRIT 粒子；动作期播放动画 `animation.king.scepter_slam`；本体近战打中玩家另给对方饥饿 II 80 tick + 缓慢 I 60 tick | `entity/SugarQueenEntity.java:33-34,43-46,128-129,157`；`boss/ai/BossMeleeWindup.java:20-50`；`boss/BossHandler.java:347-349` |
| 冲刺撞击 | `dashCd` ≤ 0 且目标距离在 6.0 ~ 26.0 格之间；开战时 `dashCd` = 120 | 触发即开始（无前摇），持续 16 tick | 结算半径 4.5 格；非玩家目标超出 4.5 格时按半伤补一次 | `dmg` = 8f 字面常量（已含全局 0.5 修正，不再乘 `BossScaling.dmg`） | 每 tick 朝目标（眼睛高度 - 0.6）位移 0.95 格；4.5 格内玩家被击退 1.8（Y 分量 0.6）并挂缓慢 II 80 tick；`GENERIC_EXPLODE` + `ANVIL_LAND` + 40 CRIT / 30 CLOUD 粒子；冲刺结束时把冷却置 90（二阶段或终局）/ 140 | `boss/SugarQueenBoss.java:77-79,151-152,188-214,342-372` |
| 铁魔法·普通法术 | 每 tick `castCd--`，≤ 0 时重置冷却并尝试施放（正在咏唱则本次跳过） | 冷却 85（一阶段）/ 62（二阶段）/ 45（终局）tick，字段初值 `castCd` = 100 tick | 由所选法术决定 | 由所选铁魔法法术决定 | 施法等级 `4 + 狂暴层级`（4 / 5 / 6，受法术自身 min / max 级别限制）；狂暴 ≥ I 时 70% 概率只从 EPIC+ 池挑（`spellPoolRare`）；先反射绑定目标再面朝玩家，LONG 类法术咏唱时间 × 2/3（`BossSpellTuning.BOSS_CAST_MULT` = 2.0 / 3.0，向上取整、下限 1 tick） | `boss/SugarQueenBoss.java:74,239-248,296-336`；`integ/spells/BossSpellTuning.java:27,63-70` |
| 施法闸（普通铁魔法共用） | 每次施放前调用 `BossSpellPacing.tryLegacyCast` | 25% 概率放行（`LEGACY_CHANCE` = 0.25） | — | — | 放行后 `LEGACY_INTERVAL_TICKS` = 240 tick 内不再放行，未放行则 `LEGACY_RETRY_TICKS` = 60 tick 后再试；正在咏唱直接拒绝；该 boss 的签名法术全部被禁用时整道闸直接放行；小姜饼人的法术走同一道闸 | `integ/spells/BossSpellPacing.java:15-17,26-38` |
| 召唤波次 | `summonCd` ≤ 0 时召唤，间隔 `SUMMON_CD` = 170（一阶段）/ `SUMMON_CD_P2` = 120（二阶段与终局）tick | 详见「八、召唤波次与召唤物」 | — | — | 每次只写一行日志（`broadcast`），不再向玩家播报 | `boss/SugarQueenBoss.java:53-54,229-236,541-544` |

## 五、常驻机制

- 甜蜜光环：每 10 tick 检查一次；本体常驻 `farm_and_charm:sweets` III 级（`amp` = 2）与 `brewinandchewin:sweet_heart` I 级（`amp` = 0），每次续期给 `600` tick，剩余 < 120 tick 即续期；缺任一 mod 时整条光环不生效（只打一次 WARN）（`entity/SugarQueenEntity.java:274-301`）。
- 负面清扫：每 20 tick 遍历并移除身上所有非增益效果（事件层的全局 `DENY` 已停用，实际只靠这一条自清，`entity/SugarQueenEntity.java:327-335`）。
- 反向甜蜜光环：二阶段或终局时每 20 tick，对 8 格内玩家挂缓慢 II 140 tick（`amp` = 1）、虚弱 I 140 tick（`amp` = 0）、饥饿 I 140 tick（`amp` = 0）（`boss/SugarQueenBoss.java:262-274`）。
- 自愈：二阶段或终局时每 20 tick 且血量未满时 `heal(0.5f)`，即 0.5 点 / 秒；狂暴 II 另有再生 I（80 tick，每 20 tick 续）（`boss/SugarQueenBoss.java:263-266`；`boss/ai/BossRage.java:79-96`）。
- 破困：每 10 tick（`PERIOD`）自查；清掉与判定箱相交（膨胀 0.35）的矿车与船，并破坏判定箱膨胀 0.12 / 0.05 / 0.12 范围内的蜘蛛网、细雪、`ftboss:trap_breakable` 标签方块，以及注册名含 `cushion` / `seat` / `chair` / `sofa` 等关键词的坐具方块（不掉落，仅放 `ITEM_BREAK` 音效）（`boss/ai/TrapBreaker.java:59-60,73-97,106-117`）。
- 防卡瞬移：每 `CHECK` = 80 tick 检查水平位移；位移 < `MIN_MOVE` = 2.0 格、离目标 > `MIN_TARGET_DIST` = 4.0 格且未在咏唱时，随机瞬移到目标周围 2.0 ~ 5.5 格的安全点（最多 10 次尝试），全失败则落到目标脚边（`boss/ai/BossUnstuck.java:26-31,55-93`）。
- 复制体 AI：仅在 `isCloneQueen()` 时接管；施法冷却 70 + rand(40)（70 ~ 109 tick）、法术等级 `3 + 狂暴层级`；贴身（< 3.0 格）每 30 tick 挥杖一次；波次召唤、二命护符与冲刺撞击只属于本体（`entity/SugarQueenEntity.java:198-243`）。

## 六、铁魔法签名法术与施法节奏

5 个签名法术同属冰系，由 `BossSpellCombat` 对**所有** `ftboss:sugar_queen` 实体（含复制体）统一驱动。

| 法术 id | 强度基数 | 配置冷却（秒） | 咏唱（tick） | 效果要点 | 出处 |
|---|---|---|---|---|---|
| `ftboss:crystal_coronation` | 68 | 80 | 16 | 落点 4 格（`RADIUS`）环爆，命中者缓慢 III 80 tick 并把冻结值抬到 100 tick | `util/BossSpellRules.java:11,15`；`integ/spells/BossSpellTuning.java:35,52`；`integ/spells/BossSpellEffects.java:145-155` |
| `ftboss:frostglass_lance` | 80 | 65 | 10 | 自眼中沿瞄准方向贯通 24 格（`RANGE`）、撞墙终止；最多命中 4 个，对已冻结目标伤害 × 1.5 并解冻；命中者缓慢 II 40 tick | `util/BossSpellRules.java:10,22,142`；`integ/spells/BossSpellTuning.java:40,52`；`integ/spells/BossSecondSpellEffects.java:64-89` |
| `ftboss:sugar_hail` | 48 | 85 | 16 | 落点 4 格范围共 3 段脉冲（第 0 / 20 / 40 tick），每段把冻结值抬到 100 tick 并挂缓慢 II 50 tick | `util/BossSpellRules.java:29,113-128`；`integ/spells/BossSpellTuning.java:38,52`；`integ/spells/BossExpandedSpellEffects.java:202-205` |
| `ftboss:glacial_prism` | 12 | 80 | 16 | 自身吸收护盾 160 tick（护盾量由法术强度折算 `shield()`，范围 4 ~ 40）+ 抗性 I 80 tick；3 格内敌人缓慢 III 60 tick 并冻结 100 tick | `util/BossSpellRules.java:30,144`；`integ/spells/BossSpellTuning.java:49`；`integ/spells/BossExpandedSpellEffects.java:114-121` |
| `ftboss:royal_shatter` | 84 | 100 | 16 | 起手 20 tick（`firstPulse`）后单段 4 格爆发；对已冻结目标伤害 × 1.5 并解冻，命中者被推开（水平 0.35、垂直 0.15） | `util/BossSpellRules.java:31,106,138`；`integ/spells/BossSpellTuning.java:41,52`；`integ/spells/BossExpandedSpellEffects.java:206-209` |

- 强度基数为 `BossSpellTuning.signaturePower(kind)`（非辅助型法术在此基数上再 × 2.0）；实际伤害 = 强度基数 × 施法者法术强度属性 × 学派强度 × 法术配置倍率，代码里没有固定伤害常量（`integ/spells/BossSpellTuning.java:33-61`）。
- 节奏：首次起手等待 `SIGNATURE_OPENING_TICKS` = 60 tick + `floorMod(实体 id, 20)`（0 ~ 19 tick）；此后每 `SIGNATURE_INTERVAL_TICKS` = 60 tick 尝试一次；每个法术另有独立冷却 `signatureCooldown` = `max(160, ceil(法术冷却 × 0.75))` tick（`integ/spells/BossSpellCombat.java:76-94`；`integ/spells/BossSpellPacing.java:13-24`）。
- 起手条件：必须有有效目标、同维度、距离 ≤ 24 格（`BossSpellRules.RANGE`）且有视线；距离 > 32 格、目标丢失或易主都会取消蓄力；蓄力期每 4 tick 出一次预警（`integ/spells/BossSpellCombat.java:41-66,80`）。
- 签名法术走的是**铁魔法自己的吟唱槽**，不由本模组直接 `onCast`：`MagicData` 里存在 `ftboss:signature_cast` 时，必须由该实体的常规施法流程把它放出来（`BossSignatureSpell.onCast`）。因此 `ftboss:sugar_queen` 若整场都不发起普通铁魔法法术，签名法术也不会落地（`integ/spells/BossSpellCombat.java:48-72`；`integ/spells/BossSignatureSpell.java:100`）。
- 释放等级：`Math.min(2, spell.getMaxLevel())`，而该类 `getMaxLevel()` 恒为 1（`BossSpellRules.MAX_LEVEL`），故实际按 1 级释放；伤害公式内部再按 `FULL_POWER_LEVEL` = 5 的满功率口径计算（`integ/spells/BossSpellCombat.java:72`；`integ/spells/FullPowerBossSpell.java:15`；`util/BossSpellRules.java:7-8,148`）。

## 七、阶段与阈值

| 阶段 | 触发条件 | 变化 | 出处 |
|---|---|---|---|
| 一阶段 | 生命比例 ≥ 0.5 | 召唤间隔 170 tick；手动施法冷却 85 tick；冲刺结束时冷却置 140 tick | `boss/SugarQueenBoss.java:53,203-210,240-242` |
| 二阶段（半血） | 生命 / 生命上限 < 0.5，只判定一次并置 `phase2` | 召唤间隔降到 120 tick；手动施法冷却 62 tick；冲刺冷却 90 tick；开启自愈与反向甜蜜光环 | `boss/SugarQueenBoss.java:180-184,229-236,242,262-274` |
| 狂暴层级 I | 生命比例 ≤ 0.50，独立于二阶段单独计数 | 迅捷 I + 抗性 I（各 80 tick，每 20 tick 续）；普通法术等级 +1 → 5 | `boss/ai/BossRage.java:38-50,85-89`；`boss/SugarQueenBoss.java:310` |
| 狂暴层级 II | 生命比例 ≤ 0.25 | 追加力量 I + 再生 I（80 tick，每 20 tick 续）；普通法术等级 +2 → 6 | `boss/ai/BossRage.java:38-50,90-95`；`boss/SugarQueenBoss.java:310` |
| 终局（`finalMode`） | 二命护符触发，每场限一次 | 血量固定回到 `MAX_HP` × 0.3 = 108（多人局 `MAX_HP` 已按人数放大，实际回血量随之提高）；清空全部效果；打断当前咏唱；召唤间隔 120、手动施法冷却 45、冲刺冷却 90 | `boss/SugarQueenBoss.java:71,170,515-535` |
| 二命护符判定 | 本体血量归零触发 `LivingDeathEvent` → `SugarQueenBoss.tryPopTotem` | 死亡事件被取消；`totemUsed` 置位后再死才走真死流程；终局下血量再归零时每 tick 拉回 108 | `boss/BossHandler.java:426-428`；`boss/SugarQueenBoss.java:168-174,515-522` |
| 阶段播报 | 阶段切换不发聊天或技能文字，只写日志（`broadcast`）+ 音效粒子 | — | `boss/SugarQueenBoss.java:183,527,541-544` |

## 八、召唤波次与召唤物

| 项目 | 数值 / 内容 | 出处 |
|---|---|---|
| 波次间隔 | `SUMMON_CD` = 170 tick（一阶段）；`SUMMON_CD_P2` = 120 tick（二阶段与终局）；开战时 `summonCd` 置 170 | `boss/SugarQueenBoss.java:53-54,73,149,229-231` |
| 波次编队（循环） | `WAVES` = `{4,0}` → `{4,0}` → `{5,1}` → `{5,1}`，即「总数 4 / 精英 0」两波后接「总数 5 / 精英 1」两波，取模循环 | `boss/SugarQueenBoss.java:57,232-234` |
| 落点 | 以本体为中心、随机角度、半径 3 + rand × 5（3 ~ 8 格）；`findSafeY` 取「下方非空、自身与上方为空」的 y，先向下探 14 格再向上探 14 格，落在该 y + 0.6；每波最多尝试 数量 × 5 次 | `boss/SugarQueenBoss.java:392-400,496-512` |
| 小姜饼人（kind 0） | `ftboss:gingerbread_minion`；生命 36、攻击 3.0、攻速 1.2、移速 0.34、抗击退 0.4、跟随 28；判定箱 0.8 × 1.1；经验 5 | `boss/SugarQueenBoss.java:386,463`；`entity/GingerbreadMinionEntity.java:65,68-77`；`entity/FTEntities.java:58-59` |
| 焦糖结晶（kind 1） | `minecraft:husk`，原版尸壳，本模组未改属性（数值 `?`，见原版属性表） | `boss/SugarQueenBoss.java:387,456` |
| 糖霜蜘蛛（kind 2） | `minecraft:spider`，原版属性未改（数值 `?`，见原版属性表） | `boss/SugarQueenBoss.java:387,457` |
| 软糖史莱姆（kind 3） | `minecraft:slime` 并 `setSize(3, false)` 取大号，原版属性未改（数值 `?`，见原版属性表） | `boss/SugarQueenBoss.java:388,458-461` |
| 种类分配 | 非精英：每 4 只中前 3 只是小姜饼人，第 4 只按 `1 + ((waveIdx + spawned) % 3)` 从焦糖结晶 / 糖霜蜘蛛 / 软糖史莱姆中取；精英按 `(waveIdx + spawned) % 2`，只出小姜饼人或焦糖结晶 | `boss/SugarQueenBoss.java:401-413` |
| 精英强化 | 迅捷 III、力量 II、生命提升 II（均 999999 tick）；若为僵尸（尸壳）另穿粉色皮革四件套（`PINK` = 0xF0B6FF）、持锋利 4 ~ 5 的甜点武器并补满血；装备掉落率 0 | `boss/SugarQueenBoss.java:420-427,471-479` |
| 小怪强化 | 迅捷 II（蜘蛛为 III）；力量 II（小姜饼人 / 史莱姆）或 I（焦糖结晶 / 糖霜蜘蛛）；小姜饼人额外抗性 I；僵尸与尸壳持锋利 2 ~ 3 的甜点武器，掉落率 0 | `boss/SugarQueenBoss.java:428-441` |
| 继承增益 | 每 `PERIOD` = 60 tick，把本体身上最多 `MAX_EFFECTS` = 3 种有益效果各降 1 级（`amplifier` 再降 1、上限 0，即最高 I 级）分享给 `range` = 40 格内最多 `MAX_MINIONS` = 8 只召唤物，持续 `DURATION` = 140 tick | `entity/SugarQueenEntity.java:162`；`boss/ai/MinionBlessing.java:33-36,76-100` |
| 行为 | 名牌可见 + 发光；每 20 tick 强制把目标改为本体当前目标，距目标 < 2.2 格时直接代打一次（`doHurtTarget`，不走自身攻击冷却） | `boss/SugarQueenBoss.java:250-260,415-418` |
| 小姜饼人自身法术 | 低级铁魔法：冷却 160 + rand(120)（160 ~ 279 tick）；目标须是玩家且 ≤ 24 格；随机 `irons_spellbooks:fireball` / `ice_block` / `lightning_bolt` / `magic_missile`，等级 1 | `entity/GingerbreadMinionEntity.java:31,42-49` |
| 离场 | 本体真死、实体被移除或主人下线时：先 `discard()` 登记在案的召唤物，再按主人标记清掉 80 格内（`SummonSweep.RANGE`）带本体主人 UUID 标记的召唤物，最后本体 `discard()` | `boss/SugarQueenBoss.java:570-604`；`boss/ai/SummonSweep.java:29,77-92` |
| 连死怜悯离场 | 同一名玩家在同一只本体（按实体 UUID 记账）下连续死亡 `LIMIT` = 5 次 → 本体与召唤物一起离场（无掉落、无经验） | `boss/ai/BossMercy.java:44,118-124,281-323` |

## 九、掉落

| 掉落物 | 数量 / 概率 | 条件 | 出处 |
|---|---|---|---|
| `minecraft:sugar` | 24 + rand(25)，即 24 ~ 48 个 | 本体真死 | `boss/SugarQueenBoss.java:550-551` |
| `ftboss:statue_sugar_queen`（石像·姜饼王） | 本体真死掉 1 个；另外任何 `ftboss:sugar_queen` 死亡（含复制体）时 `BossMercy.onBossDefeated` 再给 1 个 | 本体 1 个：真死；另 1 个：实体死亡事件 + `spawnAtLocation` 成功 | `boss/SugarQueenBoss.java:552-553`；`boss/ai/BossMercy.java:140-142,329-360` |
| 纸（命名为「秘制焦糖配方」） | 1 个，30% 概率（`nextFloat() < 0.3f`） | 本体真死 | `boss/SugarQueenBoss.java:554-558` |
| 经验 | 200 点 | 本体真死 | `boss/SugarQueenBoss.java:559` |
| `ftboss:candy_cane`（糖果拐杖） | 1 个 | 任何 `ftboss:sugar_queen` 死亡（含复制体） | `boss/ai/BossMercy.java:143,366-375` |
| `ftboss:frost_crown_charm`（霜糖王冠坠） | 1 个，必掉（`DROP_CHANCE` = 1.0，2026-09-19 起） | 玩家击杀（或 `killCredit` 是玩家）+ 开启 `doMobLoot` + 该实体未领过 | `skill/BossRelicDrops.java:52-62`；`util/BossRelicRules.java:6,46-48` |
| `ftboss:frost_queen_staff`（糖霜女王的晶冠法杖） | 1 个，必掉 | 同上一行，FROST 类固定追加 | `skill/BossRelicDrops.java:63` |
| 签名法术卷轴 | 5 种各 1 张 | 仅非复制体本体（`!isCloneQueen()`）；玩家击杀（或 `killCredit` 是玩家）+ 开启 `doMobLoot`；只对已启用的法术发放 | `integ/spells/BossSpellDrops.java:27,45-53`；`util/BossSpellRules.java:73` |

## 十、备注

- 血条与血量提示都只显示纯血量，不附「狂暴 / 终局」后缀（`boss/SugarQueenBoss.java:280-281`）。
- 着火时不渲染火焰图层，但火焰伤害照旧结算（`entity/SugarQueenEntity.java:59-62`）。
- `ftboss:charged_orb`（充能球）不属于本节：它只由故障机器人的 `DefectOrbs` 生成与驱动（`boss/ai/DefectOrbs.java:659,765`；`entity/ChargedOrbEntity.java:301-308`）。
