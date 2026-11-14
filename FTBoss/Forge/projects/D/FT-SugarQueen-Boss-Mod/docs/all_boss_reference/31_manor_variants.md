# 甜苹果庄园 · 十一位变体专属技能与数值

本节点名十一位轮换变体（0 柚子 / 1 蛋蛋 / 2 朵拉 / 3 功底 / 4 柠檬猫 / 5 青衣 / 6 文玄蓝 / 7 折耳根 / 8 紫菜 / 10 雪黎 / 11 祁崽）各自**专属**的技能、召唤物与特殊机制，末节附支援位 9 麻雀。共用的属性、判定箱、全局倍率、庄园五技能表、铁魔法法术表、共享减益与召唤限制一律见 `30_manor_shared.md`，本节不重复；整场流程见 `32_manor_fight.md`。全文时间单位 tick（1 秒 = 20 tick），距离单位「格」。

- 源码口径：`COUNT` = 12（下标 0~11，含支援位 9），`MANOR_VARIANTS` = {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11} 共 11 项，`MANOR_COUNT` = 11，`SPARROW_VARIANT` = 9 不进轮换（`entity/EasterBossEntity.java:99-114`）。

- 祁崽（`variant` 11，2026-09-20 加入轮换）的技能量级与前面十位不同 —— 十二步出招循环、一次读条六门铁魔法、三颗常驻浮游法晶 —— 为了不把本节撑成两个文档，它的专属数值单列在 `33_qizai.md`，本节只保留上面名单里那一行。

- 数值一律取自代码常量：表里先写 `常量名`，再写「= 值」（标「下限」的是**基础伤害常量**，不是玩家实吃值），出处列给 `文件.java:行号`。
- 走 `manorSkillDamage` 的技能，基础常量只是**下限**：实伤 = max(基础常量, (攻击力 + 附魔加成) × max(1.50, 倍率))，见 `util/ManorSkillDamageRules.java:4-13`。
- 前摇若来自 `HiddenBossCastRules.quicker(原值)`，本节直接写换算后的 tick（= ceil(原值 × 35%)，下限 3），换算规则见 `util/HiddenBossCastRules.java:4-14`。
- 技能出手冷却的算法是「基准 160 + random(120) tick，按变体修正，近战位（0 / 1 / 3 / 4 / 7 / 8 / 10）最后在 `ManorBossCombatRules.skillCooldown` 里再 ×2」；各变体的最终区间写在该角色表里，入口见 `entity/EasterBossEntity.java:2857-2872` 与 `util/ManorBossCombatRules.java:20-28`。

## 一、柚子（`variant` 0，近战，带庄园五技能）

| 技能 / 机制 | 触发与冷却 | 前摇 | 判定半径 / 线路 | 伤害与数值 | 附带减益 / 召唤物 | 出处 |
|---|---|---|---|---|---|---|
| 五技能 `YOUZI_SEAL` 庄园震印 | 五技能轮换偶数槽（轮换长度 4），冷却 = (80 + random(40)) × 2 = 160~240 tick | 8 tick | CIRCLE 半径 5.5 格，加目标半宽 | `damage` = 100.0（下限），普攻基准倍率 2.00，击退 1.65 / 上抛 0.55 | 缓慢 II 70 tick + 发光 I 100 tick / 无 | `util/ManorFiveCombatRules.java:25`、`entity/EasterBossEntity.java:2860` |
| 五技能 `YOUZI_ECHO` 叠影合击 | 同一轮换的另一槽，冷却 = (80 + random(40)) × 2 = 160~240 tick | 7 tick | ECHO 半径 2.8 格，3 段 × 12 tick 间隔，三段圆心各自绕原点 3 格、彼此相隔 120° | `damage` = 38.0（下限），倍率 1.50，击退 1.25 / 上抛 0.35 | 缓慢 II 70 tick + 发光 I 100 tick / 无 | `util/ManorFiveCombatRules.java:26`、`boss/ai/ManorFiveSkills.java:274-278` |
| 自我叠影 · 迷你分身 | 命中玩家或自己被打都累计，满 `YOUZI_HITS_PER_SUMMON` = 3 次且距上次 ≥ `MINI_SUMMON_CD` = 40 tick | 无 | 场上同变体分身上限 `MINI_CAP` = 6，判定箱与本体相同 | 计数满那一下对目标补 `YOUZI_BURST_DAMAGE` = 72.0；已达上限时改为自身速度 II 200 tick | 无 / 一次召 2 个迷你柚子 | `entity/EasterBossEntity.java:286-293`、`entity/EasterBossEntity.java:2585-2605` |
| 迷你分身属性 | 生成即开始计时 | 无 | 血量 `MINI_HEALTH` = 150.0，渲染缩放 `MINI_SCALE` = 0.55 | 寿命 `MINI_LIFE_TICKS` = 300 tick；**武器从 `MINI_WEAPONS` 三把随机**（斩龙剑 / 冰斧 / 荣耀金斧，满附魔 8 级 + Unbreakable）——★ 2026-09-24 按用户要求「禁止柚子分身使用血王子焰型剑」，`armamentarium:blood_prince_flamberge` 已移除 | 无 / 召唤物本体 | `entity/EasterBossEntity.java`、`entity/EasterBossEntity.java:285-291` |
| 分身分摊 | 本体每次受伤时结算 | 无 | 64 格内同变体分身平分 | `YOUZI_MINION_SHARE` = 0.10（本体少挨 10%），分身血不够就当场阵亡 | 无 / 本体 | `entity/EasterBossEntity.java:290`、`entity/EasterBossEntity.java:2505-2534` |
| 分身施法 | 分身独立冷却：首轮 40 tick，间隔 `CAST_INTERVAL` = 200 tick | 与该法术本身一致 | 射程 16 格 | 威力 `POWER_SCALE` = 0.35，冷却 ×2 | 见共享法术表 / 无 | `integ/spells/YouziCloneSpellProfile.java:7-22` |
| 武器命中附带 | 每次近战命中 | 无 | 单体 | 近战伤害与击退走共享口径（`MELEE_PUSH` = 1.50 / `MELEE_LIFT` = 0.55） | 缓慢 II 200 tick + 虚弱 II 200 tick，并累计一次「自我叠影」计数 / 无 | `entity/EasterBossEntity.java:1994-2000`、`util/ManorFiveCombatRules.java:5-6` |

- 机制说明：柚子的分身既是输出也是护盾 —— 分身替本体吃掉 10% 受伤，满 6 个后不再分裂而是给自己速度 II，所以先清分身还是先打本体，会直接改变这场战斗的硬度。
  ★ 2026-09-24：分身武器从四把减到三把（去掉血王子焰型剑，见上表「迷你分身属性」行）。

## 二、蛋蛋（`variant` 1，近战，带庄园五技能）

| 技能 / 机制 | 触发与冷却 | 前摇 | 判定半径 / 线路 | 伤害与数值 | 附带减益 / 召唤物 | 出处 |
|---|---|---|---|---|---|---|
| 五技能 `DANDAN_REAP` 末影六重镰 | 五技能轮换偶数槽（轮换长度 4），冷却 = ((160 + random(120)) ÷ 4) × 2 = 80~140 tick | 7 tick | WAVE 半径 13.5 格，半径按段数递增，6 段 × 10 tick 间隔 | `damage` = 44.0（下限），倍率 1.50，击退 1.40 / 上抛 0.42 | 虚弱 II 80 tick，末段另加缓慢 II 50 tick / 无 | `util/ManorFiveCombatRules.java:27`、`boss/ai/ManorFiveSkills.java:207-210` |
| 五技能 `DANDAN_STAR` 烛光坠星 | 同一轮换的另一槽，冷却 = ((160 + random(120)) ÷ 4) × 2 = 80~140 tick | 8 tick | CIRCLE 半径 6.75 格，3 段 × 12 tick 间隔 | `damage` = 42.0（下限），倍率 1.75，击退 1.65 / 上抛 0.62 | 虚弱 II 80 tick，末段另加缓慢 II 50 tick / 无 | `util/ManorFiveCombatRules.java:28`、`boss/ai/ManorFiveSkills.java:207-210` |
| 烛影置换 `skillEnderSwap` | 技能轮换命中即放，冷却 = ((160 + random(120)) ÷ 4) × 2 = 80~140 tick | `playCast(30, 2)` = 30 tick | 与目标**随机换位**：本体到玩家处、玩家被丢到本体原位，两侧各找安全落点 | 换位后接「连瞬」`DANDAN_RUSH_HITS` = 6 次，每次 `DANDAN_RUSH_DAMAGE` = 28.0 魔法伤害，间隔 `DANDAN_RUSH_INTERVAL` = 5 tick，每次瞬移到目标身侧 2.5 格；自身速度 II 360 tick + 抗性 I 180 tick | 反胃 300 tick + 饥饿 II 300 tick / 无 | `entity/EasterBossEntity.java:3272-3309`、`entity/EasterBossEntity.java:675-677` |
| 末影珍珠封锁 `DandanPearlBan` | 常驻判定，同一玩家提示限流 40 tick | 无 | 半径 `RANGE` = 48.0 格内有处于战斗态的蛋蛋 | 玩家**扔不出**末影珍珠（起手拦截，珍珠不消耗），已在飞的珍珠落地也不传送、不算摔落伤害；模组自带珍珠位移走 `blockThrow` / `blockLanding` | 无 / 无 | `boss/ai/DandanPearlBan.java:54`、`boss/ai/DandanPearlBan.java:101-170` |
| 虚空漩涡 `dandanVortexTick` | 冷却 `DANDAN_VORTEX_CD` = 400 tick，上场 6 × 20 tick 后放第一次 | 无，起手即结算 | 吸引半径 `DANDAN_VORTEX_RADIUS` = 21.0 格 | 持续 `DANDAN_VORTEX_TICKS` = 80 tick，每 tick 往中心拽 `DANDAN_VORTEX_PULL` = 0.26 格、上提 `DANDAN_VORTEX_LIFT` = 0.12 格 | 无 / 召唤 `cataclysm:void_vortex` 实体做演出，取不到时只有自绘粒子而吸引力照常 | `entity/EasterBossEntity.java:6080-6090`、`entity/EasterBossEntity.java:6143-6244`、`boss/ai/EnderVortex.java:35` |
| 信标压制 `dandanBeaconBan` | 每 `DANDAN_BEACON_PERIOD` = 10 tick | 无 | `DANDAN_BEACON_RANGE` = 96.0 格，庄园战内无视距离 | 摘掉玩家身上全部 ambient（信标 / 潮涌）效果，提示每 100 tick 最多一次 | 无 / 无 | `entity/EasterBossEntity.java:6058-6060`、`entity/EasterBossEntity.java:6114-6140` |
| 攻击力成长 `dandanRampAttack` | 每次打中玩家 | 无 | 本体自身 | 每次 +`DANDAN_ATK_PER_HIT` = 5.0 攻击力（加法叠加），直到玩家实吃伤害达 `DANDAN_ATK_MAX` = 200.0 | 无 / 无 | `entity/EasterBossEntity.java:6064-6065`、`entity/EasterBossEntity.java:6098-6109` |
| 末影珍珠追击 `pearlTick` | **★ 2026-09-24 重做**：距目标 > `PEARL_NEAR` = 4.0 格 **且** 连续 `DANDAN_PEARL_IDLE_TICKS` = 200 tick（10 秒）**没打到玩家**才触发；每次追完冷却 `DANDAN_PEARL_COOLDOWN_TICKS` = 120 tick（6 秒）。旧口径（"离目标 >4 格累计 13 tick"）实战几乎不触发，已废弃 | 无 | 珍珠落点半径 `LAND_RADIUS` = 4.0 格 | 落点玩家吃 `LAND_DAMAGE` = 30.0 魔法伤害（走 manorSkillDamage 倍率 1.5），随后本体瞬移到目标旁 2.2 格 | 无 / 无 | `entity/EasterBossEntity.java`、`util/ManorChaseRules.java`、`entity/EasterPearlEntity.java:29-31` |
| 掉落武器 · 镰刀瞬移 | 镰刀右键，冷却 `BLINK_COOLDOWN` = 200 tick | 无 | 水平位移 1.25 ~ `BLINK_DISTANCE` = 6.0 格，落点判定 `BLINK_RADIUS` = 2.5 格 | `blinkDamage()` = `BLINK_DAMAGE` 16.0 × 技能倍率 1.5 = 24.0；横扫 `SWEEP_DAMAGE` = 20.0 × 1.5 = 30.0，冷却 `SWEEP_COOLDOWN` = 120 tick | 无 / 无 | `item/weapons/DandanScytheItem.java:43-50`、`util/DandanScytheRules.java:18-23` |
| 武器命中附带 | 每次近战命中 | 无 | 单体 | 近战伤害与击退走共享口径，并同时叠加一次「攻击力成长」 | 反胃 80 tick + 饥饿 II 160 tick，并 `stripOneBuff` 抽掉玩家一个随机正面效果 / 无 | `entity/EasterBossEntity.java:1982-1992`、`entity/EasterBossEntity.java:1790-1791` |

- 机制说明：蛋蛋是庄园里唯一能**封锁位移**的变体 —— 48 格内玩家用不了末影珍珠（连模组自带的珍珠位移一起封），她自己却同时有珍珠追击、随机换位、6 连瞬与 21 格虚空漩涡四套位移，把玩家反复拽回近战距离，再靠每命中 +5 的攻击力成长把平砍堆到上限。

## 三、朵拉（`variant` 2，远程，`RANGED` = true）

| 技能 / 机制 | 触发与冷却 | 前摇 | 判定半径 / 线路 | 伤害与数值 | 附带减益 / 召唤物 | 出处 |
|---|---|---|---|---|---|---|
| 压制射击 `skillSuppress` | 技能轮换命中即放，冷却 = (160 + random(120)) ÷ 2 = 80~140 tick，并按致命节奏加速推进 | 无预警圈，直接开火 | 射程 `BURST_RANGE` = 18.0 格 | 一次 `BURST_SHOTS` = 32 发（每层节奏 +4 发），基准间隔 `BURST_INTERVAL` = 3 tick、射速倍率 `BURST_RATE_MULT` = 2.0（实际 1.5 tick 一发）；单发 `BURST_DAMAGE_PHYS` = 8.0 × 4/3 ≈ 10.67 物理 + `BURST_DAMAGE_MAGIC` ≈ 10.67 魔法 | 击退 `BURST_KNOCKBACK` = 0.55 + 上抛 0.10，打空装弹 `RELOAD_TICKS` = 60 tick / 无 | `entity/EasterBossEntity.java:302-322`、`entity/EasterBossEntity.java:636-642`、`entity/EasterBossEntity.java:2859` |
| 连射散布 | 连射期间 | 无 | 近距离（≤4 格）`BURST_SPREAD_NEAR` = 0.42，射程上限处 `BURST_SPREAD_FAR` = 0.78 | 见上行 | 无 / 无 | `entity/EasterBossEntity.java:321-322` |
| 聚能枪狙击 `DoraSniperCycle` | 与压制射击交替（`doraSniperNext`），一轮 = 追踪 10 tick + 蓄力到 14 tick + 收招 `RECOVERY_TICKS` = 12 tick | `TRACK_TICKS` = 10 tick 追踪，`CHARGE_TICKS` = 14 tick 到点开火 | 射程 `RANGE` = 36.0 格，判定半径 `RADIUS` = 0.72 格 | `DoraSniperDamageRules.TABLE` 三系各 240.0 点（`dora_sniper_void` / `dora_sniper_physical` / `dora_sniper_magic`，合计 720.0） | 命中击退 1.1 + 上抛 0.16，打空断连 / 无 | `util/DoraSniperCycle.java:6-10`、`skill/DoraSniperDamageRules.java:8-12`、`entity/EasterBossEntity.java:3623-3627` |
| 致命节奏 `DoraCombatTempo` | 连中 `TRIGGER_HITS` = 3 次起 1 层，连击窗口 `COMBO_WINDOW` = 60 tick，单层时长 `DURATION` = 120 tick，上限 `MAX_STACKS` = 6 层 | 无 | 本体自身 | 每层攻速 ×(1 + 0.125)、冷却 ×(1 − 0.05)、连射 +4 发；血条名字显示 `[致命节奏 n/6]` | 无 / 无 | `util/DoraCombatTempo.java:6-9`、`util/DoraCombatTempo.java:47-57`、`entity/EasterBossEntity.java:3679` |
| 长枪横扫 `tickDoraSpear` | 副手为 `ftboss:dora_lance` 且目标 10 格内有视线；一次 3 连后冷却 `DORA_SPEAR_COOLDOWN` = 30 tick（再乘节奏冷却系数） | 无 | 目标距离 `distanceToSqr` ≤ 100.0 | 每下 `SPEAR_CHARGE_DAMAGE` = 10.0 × 4/3 ≈ 13.33；`DORA_SPEAR_HITS` = 3 连、间隔 `DORA_SPEAR_INTERVAL` = 4 tick | 击退 `SPEAR_KNOCKBACK` = 1.25 + 上抛 `SPEAR_KNOCKBACK_UP` = 0.30 / 无 | `util/ManorBossCombatRules.java:5-8`、`entity/EasterBossEntity.java:638-642`、`entity/EasterBossEntity.java:3725-3757` |
| 后撤走位 `KiteGoal` | 目标进到 2.5 格内即触发（文玄蓝为 6.0 格），装弹与长枪连段期间不后撤 | 无 | 后撤目标点 = 当前位向反方向 8.0 格 | 导航速度 1.35（文玄蓝 0.9） | 无 / 无 | `entity/EasterBossEntity.java:6630-6662` |
| **最终出伤提高 1/2** | 常驻（2026-09-21 用户要求「朵拉伤害提高1/2」） | 无 | 朵拉本体**及其召唤物 / 带主人标记单位**造成的全部可归因伤害 | 上表全部数值最终 ×**1.5**（口径："提高 1/2" = ×(1 + 1/2)）；挂在 `LivingHurtEvent`(LOW) 上统一乘，狙击那三段自带 owner 的伤害源也一起吃 | 无 / 无 | `boss/ai/BossOutgoingDamageNerfs.java` |
| 武器命中附带 | 每次长枪或近战命中 | 无 | 单体 | 近战伤害走共享口径，并同时累计一次「致命节奏」 | 虚弱 II 200 tick + 挖掘疲劳 II 200 tick / 无 | `entity/EasterBossEntity.java:1975-1980` |

- 机制说明：朵拉是纯远程双节奏位 —— 狙击每轮打三系各 240 点（合计 720），压制射击一次 32 发双系小伤害，致命节奏叠满后射速 +75%、冷却 −30%、连射 56 发，逼玩家在「贴脸吃长枪三连」和「远处吃狙击」之间选。★ 2026-09-21 按用户要求「朵拉伤害提高1/2」后，上表所有数值统一 ×1.5（狙击三系实际各 **360**、合计 **1080**；压制射击单发约 **16**）；倍率挂在 `LivingHurtEvent`(LOW) 上，所以她本体、狙击与召唤物一视同仁。

## 四、功底（`variant` 3，近战，不带庄园五技能）

| 技能 / 机制 | 触发与冷却 | 前摇 | 判定半径 / 线路 | 伤害与数值 | 附带减益 / 召唤物 | 出处 |
|---|---|---|---|---|---|---|
| 大杀四方 `skillCleave` | 轮换第 0 步让位给别的招，冷却 = `spellInterval(3) ÷ SPELLS_PER_CAST` = 80 tick | `CLEAVE_WINDUP` = 11 tick | 半径 `CLEAVE_RADIUS` = 11.25 格；内圈 = `INNER_RADIUS` 4.5 × 半径缩放 2.0 = 9.0 格 | 近战伤害 × `INNER_DAMAGE_MULTIPLIER` = 1.2（内圈）/ × `OUTER_DAMAGE_MULTIPLIER` = 2.0（外圈）；外圈命中给自己回血 `OUTER_HEAL_FRACTION` = 0.12 × 已损生命 × 命中数，命中数上限 `MAX_HEAL_TARGETS` = 3 | 外圈命中 ≥ 2 人再给自己力量 II 480 tick / 无 | `entity/EasterBossEntity.java:2846-2848`、`util/GongdiCombatRules.java:8-12`、`entity/EasterBossEntity.java:3334-3385` |
| 致残打击 `crippling_strike` | 三步轮换里的重击招，冷却 `STRIKE_COOLDOWN` = 100 tick | `STRIKE_WINDUP` = 6 tick | 射程 `STRIKE_RANGE` = 5.1 格，锥半角 `STRIKE_HALF_ANGLE` = 60°，高差 `HEIGHT_TOLERANCE` = 2.5 格 | 近战伤害 × `STRIKE_DAMAGE_MULTIPLIER` = 1.75 | 缓慢 IV 30 tick / 无 | `util/GongdiCombatRules.java:13-26`、`boss/ai/GongdiSkills.java:200-212` |
| 无情铁手 `apprehend` | 三步轮换里的拉人招，冷却 `PULL_COOLDOWN` = 160 tick | `PULL_WINDUP` = 8 tick | 射程 `PULL_RANGE` = 10.5 格，锥半角 `PULL_HALF_ANGLE` = 50° | 把玩家朝本体拉，速度上限 1.15，距本体 1.8 格内不再拉 | 缓慢 II 24 tick / 无 | `util/GongdiCombatRules.java:14-23`、`boss/ai/GongdiSkills.java:185-198` |
| 诺克萨斯断头台 `guillotine` | 出血 ≥ 3 层或「当前生命 + 吸收 ≤ 断头台伤害」时优先触发；冷却 `EXECUTE_COOLDOWN` = 360 tick，击杀后 `EXECUTE_RESET` = 30 tick 重置连招冷却 | `EXECUTE_WINDUP` = 9 tick | 射程 `EXECUTE_RANGE` = 6.75 格 | 近战伤害 × `EXECUTE_DAMAGE_MULTIPLIER` = 2.5 × (1 + 0.2 × 出血层数)，伤害类型 `ftboss:gongdi_guillotine` | 无 / 无 | `boss/ai/GongdiSkills.java:89-95`、`util/GongdiCombatRules.java:19`、`boss/ai/GongdiSkills.java:214-230` |
| 出血 `Bleed` | 每次斧刃命中叠 1 层，重复命中刷新时长 | 无 | 每 `BLEED_INTERVAL` = 20 tick 结算一次 | 每层 `BLEED_DAMAGE_PER_STACK` = 1.5 点，上限 `MAX_BLEED_STACKS` = 5 层（满层 7.5 点/秒），时长 `BLEED_TICKS` = 100 tick | 无 / 无 | `util/GongdiCombatRules.java:4-7`、`util/GongdiCombatRules.java:63-91` |
| 破甲 `ArmorShred` | 黑色切割者命中玩家 | 无 | 每层 −5.0 护甲（`PER_STACK`），上限 `MAX_STACKS` = 5 层（−25.0），时长 `DURATION_TICKS` = 200 tick | 叠满 5 层时功底额外获得速度 II 160 tick | 无 / 无 | `boss/ArmorShred.java:25-28`、`entity/EasterBossEntity.java:1965` |
| 血怒 `bloodRage` | 打中玩家 +1 层，上限 `BLOOD_RAGE_MAX` = 5 层，保留到重上场 | 无 | 本体自身 | 每层 +`BLOOD_RAGE_PER_STACK` = 0.50 攻击力（乘法叠加，满层 +250%）；满层再 +`BLOOD_RAGE_MAX_SPEED_BONUS` = 0.40 移速；血怒期间造成伤害的 `BLOOD_RAGE_LIFESTEAL` = 0.15 回血、`BLOOD_RAGE_TRUE_DAMAGE` = 0.10 追加为真实伤害 | 无 / 无 | `entity/EasterBossEntity.java:203-220`、`entity/EasterBossEntity.java:4212-4280` |
| 武器命中附带 | 每次黑色切割者命中 | 无 | 单体 | 近战伤害走 `gongdiMeleeDamage` | 凋零 II 160 tick + 虚弱 II 160 tick，并同时叠 1 层破甲与 1 层血怒 / 无 | `entity/EasterBossEntity.java:1950-1972` |

- 机制说明：功底是十一人里唯一不吃庄园五技能的纯近战位，输出全部挂在「出血层数」这条链上（层数既当断头台倍率、又当触发条件），再叠永久保留、满层 +250% 攻击与 15% 吸血的「血怒」，所以战斗拖长只会让他更硬更疼。

## 五、柠檬猫（`variant` 4，近战，带庄园五技能）

| 技能 / 机制 | 触发与冷却 | 前摇 | 判定半径 / 线路 | 伤害与数值 | 附带减益 / 召唤物 | 出处 |
|---|---|---|---|---|---|---|
| 五技能 `LEMON_ACID` 酸雾爆锅 | 五技能轮换（整体错开一格，先出烈火颠勺），冷却 = (80 + random(40)) × 2 = 160~240 tick | 7 tick | CIRCLE 半径 5.0 格，4 段 × 16 tick 间隔 | `damage` = 24.0（下限），倍率 1.50，击退 0.55 / 上抛 0.18 | 中毒 I 80 tick + 虚弱 II 80 tick + 饥饿 II 100 tick / 无 | `util/ManorFiveCombatRules.java:29`、`util/ManorFiveCombatRules.java:111` |
| 五技能 `LEMON_FLAMBE` 烈火颠勺 | 同一轮换的另一槽，冷却同上 | 7 tick | CONE 半径 9.0 格，50° 半角锥，2 段 × 14 tick 间隔 | `damage` = 54.0（下限），倍率 1.75，击退 1.60 / 上抛 0.95 | 点燃 4 秒 + 饥饿 II 100 tick / 无 | `util/ManorFiveCombatRules.java:30`、`boss/ai/ManorFiveSkills.java:229-232` |
| 助战召唤 `skillSummonFoodBosses` | 场上无帮手时**必定**召唤，否则与猛火翻锅、借招三招轮换（`lemonStep % 3`）；献祭后助战累计真死 3 位即永久停召，召唤槽改为翻锅 / 借招 | 无 | 献祭前随机召 2 位**不同**的食物系 Boss，献祭后一次 1 位；候选为糖霜女皇 / 屠夫 / 茶树 / 酒桶王 / 鼠群，不含苹果爵士；旧批次先回收。只计真实死亡，超时 / 换批 / 转场 `discard()` 不计 | 助战持续 `SUMMON_LIFE_TICKS` = 1800 tick，额外 `SUMMON_EXTRA_HEALTH` = 100.0 生命 + `SUMMON_EXTRA_BUFFS` = 5 个随机增益；带持久化无奖励标记，死亡不掉物品与经验；击败进度持久化为 0~3 | 无 / 召唤物本体 | `entity/EasterBossEntity.java`、`boss/ai/LemonSacrifice.java`、`boss/ai/LemonSummonRewards.java` |
| 献祭 `trySacrifice` | 场上至少有 2 只已登记、存活且归属自己的助战 Boss 时，下一次原生技能判定优先触发；若降到 50% 生命仍未献祭，则立即召齐两位并在同一 tick 保底献祭；每实体仅一次 | 短献祭动作 | 只收符合归属的助战 Boss；麻雀与小怪既不计数也不献祭，死亡/失效/重复 UUID 不凑数；冻结/转场无敌时延后到恢复战斗 | 只继承 Boss 技能档案，不复制祭品身上的正面效果；持续**直到柠檬猫死亡**（`INFINITE_UNTIL_DEATH` = true）；献祭后每次只召 1 位 | 无 / 无 | `boss/ai/LemonSacrifice.java`、`entity/EasterBossEntity.java`、`docs/LEMON_SACRIFICE.md` |
| 能力继承 `LemonInheritedAbilities` | 献祭生效期间每 tick 运行 | 无 | 需要 32 格内、有视线的当前目标 | 继承屠夫近战与吸血（吸血同屠夫，只有 `LIFESTEAL_RATIO` = 0.15）、茶树毒刺与树根、酒桶王光环 / 炮弹 / 火球 / 翻滚 / 召唤、鼠群、女皇甜品爆裂与瞬移；继承技能伤害 ×`LemonInheritedAbilityRules.MULTIPLIER` = 5.0（例：屠夫近战 24~30 → 120~150）；**不继承沙耶五株守护茶芽的完全免伤护盾** | 柠檬在献祭后始终可受伤；继承来的减益照原样（毒、醉酒、反胃等）/ 继承期内召唤物带 `ftboss:lemon_inherited_until` 标记 | `boss/ai/LemonInheritedAbilities.java`、`boss/ai/LemonSacrifice.java`、`boss/ai/LemonInheritedAbilityRules.java` |
| 借招 `skillLemonBorrowed` | 三招轮换的第 3 招 | 无 | 甜品爆裂半径 5 格，茶雾半径 6 格，剁砍与酒桶为单体 | `LEMON_BORROW_DAMAGE` = 78.4（剁砍 ×1.5 = 117.6），走 manorSkillDamage 倍率 1.5 或 2.0 | 甜品爆裂缓慢 II 100 tick；剁砍流血 II 160 tick；茶雾缓慢 III 160 tick + 中毒 II 120 tick；酒桶反胃 II 160 tick + 击飞 | `entity/EasterBossEntity.java:3392-3442` |
| 猛火翻锅 `skillPanFlip` | 三招轮换的第 2 招，或铁魔法走 `searing_pan` | `PAN_WINDUP` = 6 tick | 半径 `PAN_RADIUS` = 6.0 格 | `PAN_DAMAGE` = 100.8（走 manorSkillDamage 倍率 2.0） | 向外推 0.9 + 上抛 1.65、点燃 18 秒、蓝火 360 tick、饥饿 II 480 tick / 无 | `entity/EasterBossEntity.java:334-336`、`integ/spells/ManorBossSpellProfile.java:81-88` |
| 食物增益 `foodBuffTick` | 每 `FOOD_BUFF_INTERVAL` = 100 tick | 无 | 本体自身 | 随机 2 个食物系增益，时长 200 tick、等级 II | 无 / 无 | `entity/EasterBossEntity.java:187`、`entity/EasterBossEntity.java:5330-5385` |
| 回复倍率 | 任意 `heal(amount)` 来源 | 无 | 本体自身 | 最终治疗量 ×`LEMON_HEAL_MULTIPLIER` = **2/3**；覆盖再生、吸血、麻雀支援、继承技能与火焰回复 | 无 / 无 | `entity/EasterBossEntity.java` |
| 法术齐射 `LemonSpellBarrage` | 献祭生效期间，首轮 100 tick + (实体 id % 40)，一轮结束后再等「时长 + 160 tick」 | 每个法术各自的施法时长 | 施法距离 `ManorSpellRules.RANGE` = 24 格 | 每次施放 = 2 + min(3, 继承 Boss 数) 个法术；同时存在的齐射组上限 `MAX_ACTIVE` = 64 | 见共享法术表 / 无 | `integ/spells/LemonSpellBarrage.java:28`、`integ/spells/LemonSpellBarrage.java:107-158`、`boss/ai/LemonSacrifice.java:73-75` |
| 武器命中附带 | 每次锅铲命中 | 无 | 单体 | 近战伤害走共享口径 | 点燃 5 秒 + 蓝火 100 tick + 缓慢 II 160 tick / 无 | `entity/EasterBossEntity.java:1937-1948` |

- 机制说明：柠檬猫先召 2 位不同食物系 Boss 助战，两只存活达标后优先一次性献祭；若到半血仍未完成，则立刻召齐两位并同 tick 献祭。只继承技能、不复制祭品 buff，**直到它死亡**（继承伤害 ×5），且沙耶继承不会再生成完全免伤护盾，柠檬始终可受伤；自身全部回复只结算 2/3。继承两只时「法术齐射」上限为 4 个法术；后续一次召一位助战，真死累计 3 位后停召；所有直接助战死亡均无物品、无经验。

## 六、青衣（`variant` 5，近战，唯一允许并行近战，带扩充五技能）

| 技能 / 机制 | 触发与冷却 | 前摇 | 判定半径 / 线路 | 伤害与数值 | 附带减益 / 召唤物 | 出处 |
|---|---|---|---|---|---|---|
| 五技能 `QINGYI_TIDE` 雷潮裂阵 | 轮换长度 6，只在 0 / 2 / 4 槽出招，奇数槽空过；冷却 = (160 + random(120)) × `QINGYI_CD_MULT` 0.30 ≈ 48~84 tick | 7 tick | CONE 半径 15.0 格，50° 半角锥，2 段 × 14 tick 间隔 | `damage` = 140.0（下限），倍率 1.50，击退 1.85 / 上抛 0.65 | 缓慢 II 80 tick + 虚弱 II 80 tick + 发光 I 100 tick / 无 | `util/ManorFiveCombatRules.java:35`、`util/ManorFiveCombatRules.java:87-89`、`entity/EasterBossEntity.java:2864` |
| 五技能 `QINGYI_THUNDER` 苍雷领域 | 同上 | 9 tick | CIRCLE 半径 8.25 格，3 段 × 16 tick 间隔 | `damage` = 125.0（下限），倍率 1.75，击退 1.35 / 上抛 0.42 | 缓慢 II 80 tick + 虚弱 II 80 tick + 发光 I 100 tick / 无 | `util/ManorFiveCombatRules.java:36` |
| 五技能 `QINGYI_STAR` 星戟天裁 | 同上 | 10 tick | CROSS 半径 15.0 格，十字条宽 ±1.15，2 段 × 16 tick 间隔 | `damage` = 180.0（下限），倍率 2.00，击退 1.90 / 上抛 0.75 | 缓慢 II 80 tick + 虚弱 II 80 tick + 发光 I 100 tick / 无 | `util/ManorFiveCombatRules.java:37` |
| 四象归墟 `QingyiConfluenceSkill` | 与投戟、星戟雨交替（`qingyiConfluenceNext`），需主手为 `ftboss:qingyi_confluence_trident` | 蓄力 `CHARGE_TICKS` = 32 tick，命中 `IMPACT_TICKS` = 4 tick，收招 `RELEASE_TICKS` = 16 tick | 施放距离 `RANGE` = 24.0 格，判定半径 `RADIUS` = 3.5 格、竖向 `HALF_HEIGHT` = 3.0 格 | Boss 版基础 = `BOSS_DAMAGE` 12.0 × `SKILL_MULT` 3.0 × `BOSS_DAMAGE_MULT` 4/3 × `QINGYI_DAMAGE_MULT` 2.25 = 108.0（再加层数加成，走 manorSkillDamage 倍率 2.0）；玩家版 `PLAYER_DAMAGE` = 48.0 × 1.5 = 72.0 | 缓慢 II + 凋零 I + 挖掘疲劳 I + 发光 I 各 `DEBUFF_TICKS` = 80 tick，并把玩家往中心拉 0.45 | `util/QingyiConfluenceRules.java:4-25`、`skill/QingyiConfluenceSkill.java:139-170`、`entity/EasterBossEntity.java:498` |
| 投戟 + 激流 + 混毒药剂 `skillQingyi` | 与星戟雨交替 | `playImpactAction(20)` | 投戟命中即触发对应武器效果；激流冲刺 12 tick（水平 1.15 格/tick，每 6 tick 瞬移到玩家身边）；收尾扔一瓶溅射药水 | 药水挂 `QINGYI_POTION_DEBUFFS` = 6 种随机减益，各自带时长 | 虚弱 / 缓慢 / 挖掘疲劳 / 中毒 / 凋零 / 饥饿 / 反胃 / 霉运 / 发光九选六 / 无 | `entity/EasterBossEntity.java:5417-5432`、`entity/EasterBossEntity.java:5695-5712` |
| 恒星三叉戟雨 `skillStarTridentRain` | 与投戟交替 | `playCast(36, 2)` = 36 tick | 从目标头顶 12 格落下，落点随机半径 1.0~5.5 格 | 一次 `QINGYI_RAIN_COUNT` = 12 把 `tridents_n_stuff:stellaris`（取不到退原版三叉戟） | 每把命中把玩家挑起 1.05 / 无 | `entity/EasterBossEntity.java:5637-5676` |
| 坠戟 `tickQingyiFallingTrident` | 每 `FALLING_TRIDENT_INTERVAL` = **40** tick 丢一把（2026-09-21 用户要求「三叉戟召唤速度减半」：20 → 40，每秒一把 → 每两秒一把） | 从 12 格高空落下 | 目标需在 `FALLING_TRIDENT_RANGE` = 32.0 格内有视线；场上同主人上限 `MAX_TRIDENTS` = 48，单把寿命 `TRIDENT_LIFETIME` = 600 tick，离主人 > `TRIDENT_OWNER_RANGE` = 64.0 格自动消散 | 命中结算同三叉戟 | 命中把玩家挑起 1.05 / 无 | `util/QingyiCombatRules.java:7-11` |
| 充能球式三叉戟 `QingyiTridentEntity` | 每 `ORB_PULSE_TICKS` = 60 tick 脉冲；同主人闪电全局冷却 `STRIKE_CD` = 70 tick、增益全局冷却 `BUFF_CD` = 60 tick | 无 | 落雷范围 `ORB_RANGE` = 24.0 格 | 落雷 = 16.0 × `QINGYI_DAMAGE_MULT` 2.25 = 36.0（再加层数加成，走 manorSkillDamage 倍率 1.5）；给本体随机 1 个增益（六选一，10 秒 II 级） | 无 / 无 | `entity/QingyiTridentEntity.java:34-42`、`entity/QingyiTridentEntity.java:108-138` |
| 三叉戟层数 `onQingyiTridentSummoned` | 每召唤一把三叉戟，星戟雨每落一把也算 | 无 | 本体自身 | 每把 +`QINGYI_STACK_DAMAGE` = 1.0 攻击（`QINGYI_MAX_STACKS` = 200 层封顶）；生命上限每把 +`QINGYI_STACK_HEALTH` = 1.0（`QINGYI_MAX_HEALTH_STACKS` = 20 层封顶，基础 1500 → 1520）；护甲按 `ManorRefinementRules.armorSteps` 每 5 把 +2（`QINGYI_STACK_ARMOR` = 1.0 × 2 步），`QINGYI_MAX_ARMOR_STACKS` = 20 封顶（基础 6 → 26）。同时获得【充能】（等级 = 层数 − 1，封顶 `QINGYI_CHARGE_MAX_AMP` = 4）+ `QINGYI_ONSUMMON_BUFFS` = 6 个不重复随机增益，全部 `QINGYI_ONSUMMON_TICKS` = 600 tick | 无 / 无 | `entity/EasterBossEntity.java:556-578`、`entity/EasterBossEntity.java:5762-5800`、`util/ManorRefinementRules.java:30-32` |
| 四象轮转 `rotateQingyiWeapon` | 每 `QINGYI_SWAP_TICKS` = 160 tick 换一件 TnS 独特武器 | 无 | 本体自身 | `QINGYI_STELLARIS_INDEX` = 3 为恒星（星戟雨使用的那件） | 无 / 无 | `entity/EasterBossEntity.java:474`、`entity/EasterBossEntity.java:2886-2889` |
| 铁魔法闪电 `qingyiLightningTick` | 每 `QINGYI_SPELL_CD` = 80 tick | 施法姿势 14 tick | 目标需在 32 格内有视线 | 等级 `QINGYI_SPELL_LVL_MIN` = 5 ~ `QINGYI_SPELL_LVL_MAX` = 7；没装铁魔法时退回真闪电 + `QINGYI_SPELL_FALLBACK_DAMAGE` = 64.8（加层数加成，倍率 1.75） | 缓慢 III 60 tick + 虚弱 II 60 tick / 无 | `entity/EasterBossEntity.java:468-472`、`entity/EasterBossEntity.java:5201-5250` |
| 星戟连击 `onTridentHit` | 三叉戟命中玩家累计，满 `QINGYI_TRIDENT_STREAK` = 5 次触发 | 无 | 单体 | 第 1 件武器（风暴追寻者）命中必定召真闪电并追加 32.0 魔法伤害；满 5 次挂 4 个随机减益，每个 15 秒 II 级 | 从 `RANDOM_DEBUFF_POOL`（虚弱 / 缓慢 / 挖掘疲劳 / 中毒 / 凋零 / 饥饿 / 反胃 / 霉运 / 发光）随机 4 项 / 无 | `entity/EasterBossEntity.java:476`、`entity/EasterBossEntity.java:5596-5631`、`entity/EasterBossEntity.java:140-145` |
| **全局出伤减半** | 常驻（2026-09-21 用户要求「青衣的伤害减半」） | 无 | 她造成的**全部**伤害：原版近战 / 庄园五技能那三招 / 铁魔法闪电 / 激流冲刺 / 投戟两招 / 三叉戟落雷 / 三叉戟本体命中补伤（含「风暴追寻者」那记写死的 32） | ×`QingyiCombatRules.OUTGOING_DAMAGE_MULT` = **0.5**，挂在 `LivingHurtEvent` 上统一乘（与「非庄园伤害翻倍」同一套写法；那份显式排除 `EasterBossEntity`，两者不会二次相乘） | 无 / 无 | `boss/ai/QingyiDamageHalved.java`、`util/QingyiCombatRules.java` |
| 攻防强化 | 常驻，随变体重挂 | 无 | 本体自身 | `QINGYI_ATK_BONUS` = 1.00（乘法叠加）+ `QINGYI_ARMOR_BONUS` = 8.0（加法）；庄园主体承伤 ×`QINGYI_DAMAGE_TAKEN_MULT` = 0.70 | 无 / 无 | `entity/EasterBossEntity.java:498-504` |
| 近战规则 | 常驻 | 无 | 攻击距离 `MELEE_REACH_SQUARED` = 3.5² = 12.25，攻击间隔 `MELEE_INTERVAL` = 10 tick | 追击速度 `CHASE_SPEED` = 1.5 | 无 / 无 | `util/QingyiCombatRules.java:4-6` |
| 武器命中附带 | 每次三叉戟命中，并按当前四象武器追加 | 无 | 单体 | 先统一挂 `mark`（缓慢 II + 凋零 I + 挖掘疲劳 I + 发光 I 各 160 tick） | 0 号武器：缓慢 II 160 tick + 虚弱 II 160 tick；1 号：凋零 II 160 tick；2 号：挖掘疲劳 II 160 tick + 反胃 I 160 tick + 流血 II 160 tick；3 号：发光 I 160 tick + 缓慢 III 160 tick / 无 | `entity/EasterBossEntity.java:1904-1935`、`skill/QingyiConfluenceSkill.java:139-144` |

- 机制说明：青衣是唯一允许并行近战的变体（放技能期间照常平砍），强度随场上三叉戟数量线性成长 —— 每把 +1 攻击 / +1 生命上限、每 5 把 +2 护甲并给 6 个随机增益（攻击层数上限 200，护甲与生命上限各 20 层），所以放任她铺满 48 把三叉戟，会让她的平砍与落雷一起变成主威胁。

## 七、文玄蓝（`variant` 6，远程，带庄园五技能，`RANGED` = true）

- 庄园阶段由竹鸟先登场，参战玩家攻击竹鸟后唤出玄蓝。竹鸟自身保持 1 血，不单独死亡；攻击竹鸟的原始伤害交由玄蓝正常受伤管线处理，两者复用同一“竹鸟 · 玄蓝”血条及玄蓝生命池。
- 玄蓝登场 5 秒后竹鸟开始投炮台；多人时每位有效参战玩家周围各一座，每人独立 5 秒周期，失败仅对该玩家每秒重试；单人围绕玄蓝投一座。炮台 150 生命、50 护甲，光束及随后三枚一次性飞轮均为玄蓝对应伤害的三分之一。
- 每座炮台从生成起存续 600 tick（正常 30 秒）后自动移除；玄蓝暂停行动时仍计时，被摧毁或战斗结束时可提前清理。
- 玄蓝阶段禁止麻雀支援：`spawnSparrow` / `ImpossibleSparrowSupport.ensure` 在生成或接管前直接拒绝玄蓝，本场战斗或直接归属玄蓝的残留支援麻雀每 tick 复核后清掉（不移除其它战斗的独立麻雀，`boss/ai/XuanlanEncounterLimits.java:28-47`、`boss/SparrowSupport.java:252-257`、`boss/ImpossibleSparrowSupport.java:102-104`）；飞轮硬上限 15 个，见下表「飞轮回旋」行。

| 技能 / 机制 | 触发与冷却 | 前摇 | 判定半径 / 线路 | 伤害与数值 | 附带减益 / 召唤物 | 出处 |
|---|---|---|---|---|---|---|
| 五技能 `XUANLAN_RAIL` 磁轨连射 | 五技能轮换偶数槽（轮换长度 4），冷却 `cd` = 80~140 tick | 9 tick | BEAM 长度 18.0 格，半宽 2.20 格（文玄蓝专属），3 段 × 12 tick 间隔 | `damage` = 96.0（下限），倍率 1.50，击退 1.20 / 上抛 0.28 | 缓慢 II 50 tick + 挖掘疲劳 I 80 tick / 无 | `util/ManorFiveCombatRules.java:31`、`util/ManorFiveCombatRules.java:130-153`、`entity/EasterBossEntity.java:2859` |
| 五技能 `XUANLAN_RING` 超压震环 | 同一轮换的另一槽，冷却同上 | 7 tick | WAVE 半径 10.0 格，另有 +1.0 格偏移，3 段 × 10 tick 间隔 | `damage` = 92.0（下限），倍率 1.50，击退 1.80 / 上抛 0.48 | 缓慢 II 50 tick + 挖掘疲劳 I 80 tick / 无 | `util/ManorFiveCombatRules.java:32` |
| 飞轮绞杀 `flywheelGrind` | 每 20 tick 结算一次 | 无 | `GRIND_RADIUS` = 6.25 格 | `GRIND_DAMAGE` = 24.0 × `XUANLAN_DAMAGE_MULT` 4.19 = 100.56 | 缓慢 II 40 tick；命中任意玩家则自身速度 II 40 tick / 无 | `entity/EasterBossEntity.java:597-598`、`entity/EasterBossEntity.java:4562-4578` |
| 蒸汽泄压 `skillSteamVent` | 飞轮达上限且不在 8~16 格光束连招距离时，技能换挡到这一招 | 10 tick | 正面锥 `to · look` ≥ 0.82（约 ±35°），射程 `VENT_RANGE` = 10.75 格 | `VENT_DAMAGE` = 80.0 × 4.19 = 335.2（走 manorSkillDamage 倍率 1.5） | 击退 `VENT_KNOCKBACK` = 0.98 + 上抛 0.22；缓慢 II 360 tick + 虚弱 II 360 tick / 无 | `entity/EasterBossEntity.java:599-600`、`entity/EasterBossEntity.java:666`、`entity/EasterBossEntity.java:4574-4608` |
| 飞轮回旋 `skillFlywheelVolley` | 飞轮未满时出招，每 `FLYWHEEL_INTERVAL` = 8 tick 投 1 个，共 `FLYWHEEL_SHOTS` = 6 个；每 20 tick 由 `XuanlanEncounterLimits` 复核一次，超过上限按实体 id 保留最早 15 个，已达 15 个时新飞轮入场被拒 | `playAction(34)` | 追踪 `HOMING_RANGE` = 24.0 格、`HOMING_TICKS` = 40 tick 后返航并环绕；场上上限 `MAX_FLYWHEELS` = 15，只统计玄蓝名下 `FlywheelProjectileEntity`（飞行中与环绕中都算，多人追加的外围飞轮也计入同一额度；竹鸟炮台的 `TurretFlywheelEntity` 不计入） | 本体 `DAMAGE` = 10.67（16.0 原始 × 2/3），命中再 × `flywheelDamageMul()`（初始 4.19 ≈ 44.7，每次光束连招 +0.10，上限 5.0），无视 50% 护甲（`ARMOR_PIERCE_RATIO` = 0.5，一半物理一半魔法），另各附 `EXTRA_MAGIC` = 1.33、`EXTRA_VOID` = 1.0、`EXTRA_FIRE` = 1.0；玩家血量 > `HEALTH_BONUS_THRESHOLD` = 100.0 时 × `HEALTH_BONUS_MULT` = 1.25；同一玩家命中间隔 `HIT_CD_NEAR` = 2（≤1 格）到 `HIT_CD_FAR` = 6（≥6 格），叠满再 −1 | 缓慢 II + 虚弱 II 各 100 tick、点燃 2 秒、无击退 / 飞轮本体（环绕寿命 `ORBIT_MAX_LIFE` = 3600 tick） | `entity/FlywheelProjectileEntity.java:46-81`、`util/FlywheelCombatRules.java:4-8`、`boss/ai/XuanlanEncounterLimits.java:24-79`、`entity/EasterBossEntity.java:4620-4680` |
| 开局飞轮 `grantOpeningFlywheels` | 上场即结算 | 无 | 环绕本体 | 直接给 `OPENING_COUNT` = 10 个环绕飞轮 | 无 / 飞轮本体 | `util/FlywheelCombatRules.java:9`、`entity/EasterBossEntity.java:4630-4651` |
| 飞轮齐射 `flywheelVolley` | 每 `FLYWHEEL_VOLLEY_TICKS` = 400 tick 一次，引导光束期间暂停 | 无 | 48 格内自己名下全部环绕飞轮 | 全部飞轮同时再扑一轮（重新追最近玩家，40 tick 后回到环绕） | 无 / 无 | `entity/EasterBossEntity.java:605-606`、`entity/EasterBossEntity.java:4684-4700` |
| 光束连招 `skillBeamCombo` | 目标距离在 `BEAM_COMBO_MIN` = 8.0 ~ `BEAM_COMBO_MAX` = 16.0 格时优先 | `playImpactAction(20)` | 从眼前沿直线打到目标，判定半径 `BEAM_STRIKE_RADIUS` = 2.6 格加玩家半宽 | `BEAM_STRIKE_DAMAGE` = 60.0 × 4.19 = 251.4（走 manorSkillDamage 倍率 2.0）；随后全部飞轮齐射，并把飞轮攻击力 +`FLYWHEEL_DAMAGE_STEP` = 0.10（加算，上限 `FLYWHEEL_DAMAGE_CAP` = 5.0） | 击退 `BEAM_KNOCKBACK` = 0.63 + 上抛 0.18 / 无 | `entity/EasterBossEntity.java:617-623`、`entity/EasterBossEntity.java:4702-4750` |
| 超能光束 `xuanlanBeamTick` | 冷却 `XUANLAN_BEAM_CD` = 320 tick，上场后 `8 × 20` tick 放第一次；锁定 / 前摇 / 引导期间不插别的技能 | **锁定** `XUANLAN_BEAM_LOCK` = 20 tick（这段时间跟着你，预警线跟着走）→ **前摇** `XUANLAN_BEAM_CHARGE` = 20 tick（方向已冻结，红色收缩环收满即发射，这 1 秒是躲避窗口）→ **引导** `XUANLAN_BEAM_FIRE` = 40 tick | 长度 `XUANLAN_BEAM_LEN` = 40.0 格，判定半径 `XUANLAN_BEAM_RADIUS` = 4.0 格加玩家半宽；锁定结束后方向完全不动，前摇与引导期间无法跟头 | **无伤害**：每 `XUANLAN_BEAM_PULSE` = 10 tick 只做一次判定，每次施法每个玩家只结算一次 | 命中 = **眩晕 `XUANLAN_BEAM_STUN` = 60 tick（3 秒硬定身，每 tick 拉回锚点）+ 清空全部增益**（`PlayerBuffRules.clearBuffs`，减益保留）；**眩晕解除的那一 tick** 才把攒着的击退甩出去：`XUANLAN_BEAM_KNOCKBACK` = 1.5 + 上抛 `XUANLAN_BEAM_LIFT` = 0.35（走带施法者的 `BossKnockback.push(this, p, …)`，吃玄蓝 1.5 倍加成，折算到玩家约 0.56 水平 / 0.26 上抛），放完不进入虚弱 | `entity/EasterBossEntity.java:641-714`、`entity/EasterBossEntity.java:5492-5650`、`util/PlayerBuffRules.java` |
| 数值口径 | 常驻 | 无 | 本体自身 | `XUANLAN_DAMAGE_MULT` = 4.19 同时乘在绞杀、泄压与光束打击上；飞轮本体读 `flywheelDamageMul()`（初始即 4.19）；近战另有 `XUANLAN_ATK_BONUS` = 0.30（乘法叠加）。招牌「超能光束」无伤害，不走这条倍率 | 无 / 无 | `entity/EasterBossEntity.java:636-638`、`entity/EasterBossEntity.java:952`、`entity/EasterBossEntity.java:1258-1261` |
| 后撤走位 `KiteGoal` | 目标进到 6.0 格内即触发 | 无 | 后撤目标点 = 当前位向反方向 8.0 格 | 导航速度 `0.9`（其它远程 1.35） | 无 / 无 | `entity/EasterBossEntity.java:6630-6662` |
| 掉落武器 · 回旋月刃 | 绿枪按住右键蓄力，满蓄 `CHARGE_TICKS` = 20 tick、下限 `MIN_CHARGE_TICKS` = 4；白刃右键投掷，冷却 8 tick | 蓄力即前摇 | 白刃出程 `OUTBOUND_TICKS` = 10 tick 后返航，单枚寿命 `MAX_FLIGHT_TICKS` = 60 tick，同时最多 `MAX_ACTIVE_BLADES` = 2 枚；射程 `RANGE` = 32.0 格 | `bladeDamage` = 出程 16.0 或返航 12.0（命中月印再 +16.0），整体 × `WENXUANLAN_DAMAGE_MULTIPLIER` = 2.0；绿枪 `shotDamage` = (16.0 + 20.0 × 蓄力比 + 4.0 × 月能层数) × 2.0；月印 `MARK_TICKS` = 120 tick、月能 `ENERGY_TICKS` = 160 tick（最多 3 层） | 无 / 无 | `util/XuanlanRules.java:5-12`、`util/XuanlanRules.java:35-42`、`entity/PlayerMoonChakramEntity.java:63-83` |
| 武器命中附带 | 每次飞轮或近战命中 | 无 | 单体 | 近战伤害走共享口径 | 缓慢 III 100 tick + 反胃 I 100 tick / 无 | `entity/EasterBossEntity.java:1865-1871` |

- 机制说明：文玄蓝是「越打越多」的远程位 —— 飞轮环绕本体累积（硬上限 15 个，开局先给 10 个，多人共用这一额度），每次光束连招再加 0.10 倍飞轮攻击力（上限 5.0），每 20 秒全部飞轮集体齐射一次；叠满 15 个后飞轮每次命中的冷却再 −1 tick（最低 2 tick），技能也换挡成正面锥的蒸汽泄压，另有 320 tick 冷却的招牌「超能光束」—— 它**不造成伤害**，改成锁定 20 tick → 前摇 20 tick → 引导 40 tick，被照到就眩晕 3 秒并清空全部增益。

## 八、折耳根（`variant` 7，近战 + 施法型，带庄园五技能）

| 技能 / 机制 | 触发与冷却 | 前摇 | 判定半径 / 线路 | 伤害与数值 | 附带减益 / 召唤物 | 出处 |
|---|---|---|---|---|---|---|
| 五技能 `SHADOW_CROSS` 影刃十字 | 五技能轮换偶数槽（轮换长度 4），冷却 = (80 + random(40)) × 2 = 160~240 tick | 7 tick | CROSS 半径 9.0 格，十字条宽 ±1.15，2 段 × 14 tick 间隔 | `damage` = 56.0（下限），倍率 1.50，击退 1.40 / 上抛 0.30 | 凋零 I 80 tick + 缓慢 II 60 tick / 无 | `util/ManorFiveCombatRules.java:33`、`boss/ai/ManorFiveSkills.java:211-214` |
| 五技能 `SHADOW_CAGE` 噬影牢笼 | 同一轮换的另一槽，冷却 = (80 + random(40)) × 2 = 160~240 tick | 8 tick | CIRCLE 半径 6.0 格，3 段 × 14 tick 间隔，非末段方向反向（吸附） | `damage` = 32.0（下限），倍率 1.50，击退 1.55 / 上抛 0.40 | 凋零 I 80 tick + 缓慢 II 60 tick / 无 | `util/ManorFiveCombatRules.java:34`、`boss/ai/ManorFiveSkills.java:195` |
| 暗影突袭 `skillShadowStrike` | 技能轮换命中即放 | `SHADOW_STRIKE_WINDUP` = 4 tick | 瞬移到目标**背后** 2.2 格；命中判定 `distanceToSqr` ≤ 16.0（4 格）且有视线 | `SHADOW_DAMAGE` = 112.0 × `SHADOW_DAMAGE_CUT` = 0.70 → 78.4（走 manorSkillDamage 倍率 2.0），其中 `SHADOW_VOID_HALF` = 0.5 那半走虚空并按 ×1.5 补回（虚空 58.8 / 物理 19.6） | 失明 II `SHADOW_DARKNESS_TICKS` = 300 tick + 黑暗 300 tick + 凋零 III 360 tick，并开「虚空侵蚀」/ 无 | `entity/EasterBossEntity.java:647-656`、`entity/EasterBossEntity.java:5966-6029` |
| 虚空侵蚀 `ftboss:shadow_erosion` | 每次打中玩家 +1 层 | 无 | 单体 | 每层基础 `DAMAGE_PER_STACK` = 4.0 点虚空伤害，再吃折耳根最终 ×**0.25**（2026-09-21 用户要求「虚空buff伤害减半」：当天先到 0.5，再减半到 0.25）；实际每层 **1 点/秒**，上限 `MAX_STACKS` = 5 层（满层 **5 点/秒**），时长 `TICKS` = 200 tick | 虚空侵蚀 / 无 | `effect/ShadowErosionEffect.java:19-24`、`effect/ShadowErosionEffect.java` 结算入 `entity/EasterBossEntity.java:6034-6050` |
| 连击转虚空 `ShadowVoidSplit` | 挂在 `LivingHurtEvent` 上，每次「伤害归因于折耳根、挨打的是玩家」+1 连击，满 `SHADOW_STREAK_HITS` = 25 次开启；断连超时 `SHADOW_STREAK_TIMEOUT` = 600 tick | 无 | 单体玩家 | 开启后它对玩家造成的**任意**伤害都额外补一发等量 50% 的虚空伤害（净效果：对玩家总伤害 +50%，多出的那半自带穿甲） | 无 / 无 | `boss/ai/ShadowVoidSplit.java:70-76`、`entity/EasterBossEntity.java:6252-6254` |
| 失明维持 `maintainTick` | 每 5 tick 补挂一次 | 无 | 折耳根 24.0 格内，或 15 秒内被它打过的玩家 | 补挂失明 II + 黑暗 + `ftboss:shadow_blind`，每次补挂时长 `MAINTAIN_TICKS` = 60 tick | 失明 II + 黑暗 + 暗影笼罩 / 无 | `boss/ai/ShadowVoidSplit.java:141-174` |
| 武器命中 `onWeaponHit` | 每次暗影剑命中 | 无 | 单体 | 凋零 II 8 秒 | 凋零 II + 失明 II `SHADOW_DARKNESS_ON_HIT` = 300 tick + 黑暗 300 tick，并开一层「虚空侵蚀」 / 无 | `entity/EasterBossEntity.java:1872-1889`、`boss/ai/ShadowVoidSplit.java:108-113` |
| 铁魔法法术 | 与同伴一致的施法节奏 | 见共享法术表 | 见共享法术表 | `shadow_brand` 基础威力 = `SHADOW_DAMAGE` 112.0 × `SHADOW_DAMAGE_CUT` 0.70 = 78.4（走 manorSkillDamage 倍率 2.0，其中一半走虚空） | 黑暗 + 失明 II + 凋零 III + 虚空侵蚀一层 / 无 | `integ/spells/ManorBossSpellProfile.java:35`、`integ/spells/ManorBossSpellProfile.java:97-104` |
| 最终出伤保留 1/4 | 常驻 | 无 | 折耳根本体造成的全部可归因伤害 | 上述近战、技能、投射物、铁魔法、虚空侵蚀与满连击额外虚空伤害最终统一 ×**0.25**（2026-09-21：当天先减半到 ×0.5，用户要求「虚空伤害 / 虚空buff / 伤害」三条再减半后累计保留 1/4） | 无 / 无 | `boss/ai/BossOutgoingDamageNerfs.java` |

- 机制说明：折耳根是唯一把「视野剥夺」做成常驻状态的变体 —— 24 格内玩家每 5 tick 被补挂一次失明与黑暗（还有本模组自己的 `ftboss:shadow_blind`，外部饰品摘不掉），同时每次命中都在叠「虚空侵蚀」层数，连击满 25 次后追加 50% 穿甲虚空伤害；整次可归因输出（包括这段额外虚空伤害与虚空侵蚀 buff）最终统一**保留 1/4**。⚠ 日志口径：「连击转虚空」那行会同时打出**拆出的原始值**与**实际结算值**（2026-09-21 前只打原始值，看起来像"虚空伤害没被削"）。

## 九、紫菜（`variant` 8，近战）

2026-09-19 新增减益增伤：紫菜对玩家的每次有效伤害，在原伤害上追加 `玩家当前负面效果种类数 × 2` 点，再走正常护甲、抗性等减伤。同种效果等级不重复计数，正面与中性效果不计入；平砍、毒雾及能归因于紫菜的法术/投射物共用伤害入口。此次命中之后新施加的减益从后续命中开始计数，不改变无来源中毒/凋零的自然跳伤。见 `docs/DANDAN_ZICAI_BALANCE.md`。

| 技能 / 机制 | 触发与冷却 | 前摇 | 判定半径 / 线路 | 伤害与数值 | 附带减益 / 召唤物 | 出处 |
|---|---|---|---|---|---|---|
| 毒雾爆发 `skillPoisonBurst` | 技能轮换命中即放，冷却 = (160 + random(120)) × 2 = 320~560 tick | `POISON_WINDUP` = 7 tick | 半径 `POISON_RADIUS` = 6.75 格 | `POISON_DAMAGE` = 56.0（走 manorSkillDamage） | 中毒 III 600 tick（30 秒）+ 缓慢 II 360 tick（18 秒），并触发一次河豚减益池 / 无 | `entity/EasterBossEntity.java:669-670`、`entity/EasterBossEntity.java:698`、`entity/EasterBossEntity.java:6286-6310` |
| 武器命中 `onWeaponHit` | 每次河豚棒命中；**近战出手间隔 = `ZICAI_MELEE_INTERVAL` = 30 tick（1.5 秒一次，2026-09-24 按用户要求「减少紫菜的攻速」由原版 20 tick 拉长）** | 无 | 单体 | 见共享近战口径 | **★ 2026-09-24 按用户要求「一次攻击 2 个 debuff」：只剩 中毒 II 160 tick + 缓慢 II 120 tick 两条，不再调 `applyPufferDebuffs`** / 无 | `entity/EasterBossEntity.java`、`util/ManorBossCombatRules.java` |
| 河豚减益池构造 `zicaiPool` | 首次使用时构建并缓存 | 无 | 全注册表扫描 | 第 ① 步收 `armamentarium` 命名空间全部合格负面效果；第 ② 步收手工点名 id；第 ③ 步收其余通过过滤的负面效果 | 无 / 无 | `entity/EasterBossEntity.java:5739-5745`、`entity/EasterBossEntity.java:5885-5915` |
| 减益过滤 `zicaiAllowed` | 建池时与每次抽取前 | 无 | 无 | 必须是 `MobEffectCategory.HARMFUL`、非 `isBeneficial()`、非 `isInstantenous()`；命名空间黑名单 `ZICAI_BLOCK_NAMESPACES` = `irons_spellbooks`、`rajswordmod`；精确 id 黑名单 `ZICAI_BLOCK_IDS` = `rajswordmod:holy_fire`；关键字黑名单 `ZICAI_BLOCK_KEYWORDS` = blind / darkness / levitation / gravity / minimap / world_map / worldmap / waypoint / radar / cave_maps / wm_ / 地图 / 雷达 / 路径点 | 无 / 无 | `entity/EasterBossEntity.java:5817-5883` |
| 河豚施加 `applyPufferDebuffs` | **★ 2026-09-24 起只有「毒雾爆发」触发**（平砍不再触发；平砍固定给 中毒 II + 缓慢 II 两个） | 无 | 单体 | 每次抽 `ZICAI_PER_APPLY` = 2 种**不同**减益（最多重抽 128 次），时长 `ZICAI_DEBUFF_TICKS` = 300 tick；同种再中等级 +1，上限 `ZICAI_DEBUFF_MAX_AMP` = 4（V 级）；基础等级凋零与中毒为 III、其余为 II | 池内随机 2 项；每次成功施加还 `stripOneBuff` 抽掉玩家 1 个随机正面效果 / 无 | `entity/EasterBossEntity.java:5750-5758`、`entity/EasterBossEntity.java:5797-5802`、`entity/EasterBossEntity.java:5924-5959` |
| 崩溃双保险 `safeAddDebuff` | 每次施加 | 无 | 单体 | 先 `removeEffect` 同种效果再挂（摘掉固定 UUID 属性修饰符），整包 try/catch，失败就把该效果加进 `ZICAI_BLOCKED` 本场拉黑 | 无 / 无 | `entity/EasterBossEntity.java:5773-5791` |
| 铁魔法 | 不参与 | 无 | 无 | 紫菜的招牌技能只剩毒雾爆发，减益池同步排除 `irons_spellbooks` | 无 / 无 | `entity/EasterBossEntity.java:3197-3203` |

- 机制说明：紫菜是「减益抽奖」位 —— **毒雾爆发**从全整合包筛出的河豚减益池里抽 2 种不同减益（15 秒、可叠到 V 级），并顺手抽掉玩家一个正面效果。
  ★ 2026-09-24 调整：**平砍不再抽池**（一次攻击正好 2 个 debuff：中毒 II + 缓慢 II），**近战出手间隔 20 → 30 tick**（1.5 秒一次）。

## 十、雪黎（`variant` 10，近战，七招轮换 + 女仆护法）

| 技能 / 机制 | 触发与冷却 | 前摇 | 判定半径 / 线路 | 伤害与数值 | 附带减益 / 召唤物 | 出处 |
|---|---|---|---|---|---|---|
| 装备 | 上场即装备 | 无 | 主手 `farmersdelight:skillet`、副手 `bakery:rolling_pin` | 满附魔 `WEAPON_ENCH_LEVEL` = 8 级 + 无法破坏 | 无 / 无 | `entity/EasterBossEntity.java:249`、`entity/EasterBossEntity.java:369`、`entity/EasterBossEntity.java:1068` |
| 技能节奏 | 七招轮换 0 到 6 循环，`xueliNext` 上场固定从 3 起（第一招必是禁疗飞刀） | 无 | 无 | 冷却 = ((160 + random(120)) × 0.6) × 2 ≈ 192~336 tick | 无 / 无 | `entity/EasterBossEntity.java:384`、`entity/EasterBossEntity.java:3176-3194`、`entity/EasterBossEntity.java:2862` |
| ① 汤锅投掷 `skillXueli` | 轮换第 0 招 | `playImpactAction(20)` | 抛物线射向目标，落点 `SPLASH_RADIUS` = 3.0 格 | 落点玩家吃 `XUELI_POT_DAMAGE` = 40.0（一半物理 20.0 + 一半魔法 20.0）；汤锅寿命 `MAX_LIFE` = 200 tick | 细雪 / 汤锅实体 | `entity/EasterBossEntity.java:367`、`entity/StockpotEntity.java:32-36`、`entity/StockpotEntity.java:90-98` |
| ② 平底锅横扫 `skillXueliPanSlap` | 轮换第 1 招；第 4 招落点无玩家时也改放这一招 | `XUELI_PAN_WINDUP` = 5 tick | 半径 `XUELI_PAN_RADIUS` = 4.5 格 | `XUELI_PAN_DAMAGE` = 30.0，击退 0.8 + 上抛 0.3 | 点燃 4 秒 + 细雪 / 无 | `entity/EasterBossEntity.java:392-393`、`entity/EasterBossEntity.java:699`、`entity/EasterBossEntity.java:4587-4609` |
| ③ 擀面杖压路冲锋 `skillXueliRoll` | 轮换第 2 招 | `playAction(22)` | 持续 `XUELI_ROLL_TICKS` = 30 tick，水平速度 0.95 格/tick；3 格内（`distanceToSqr` ≤ 9.0）玩家挨打 | `XUELI_ROLL_DAMAGE` = 12.0，同一次冲锋的命中间隔 `XUELI_ROLL_HIT_CD` = 6 tick | 直接挂粉碎轮 + 细雪 + 击退 / 无 | `entity/EasterBossEntity.java:395-397`、`entity/EasterBossEntity.java:4626-4661` |
| 粉碎轮 `startCrush` | 擀面杖命中即挂，重复命中原地刷新计时 | 无 | 单体 | 每 tick `XUELI_CRUSH_DAMAGE` = 5.0，持续 `XUELI_CRUSH_TICKS` = 60 tick（合计 300.0）；每 tick 清无敌帧，伤害类型 `create:crush`（取不到退普通物理） | 无 / 无 | `entity/EasterBossEntity.java:363-365`、`entity/EasterBossEntity.java:4322-4364` |
| ④ 禁疗飞刀 `skillXueliKnives` | 轮换第 3 招（上场第一招）；第 5 招女仆不可用时也改掷飞刀 | `playImpactAction(16)` | 一次 `XUELI_KNIFE_COUNT` = 3 把，散射 ±0.09 弧度，初速 2.4；擦身判定 `NEAR_RADIUS` = 1.6 格 | 直接命中 `KNIFE_DAMAGE` = 12.0，擦身命中 60% = 7.2；飞刀寿命 `MAX_LIFE` = 120 tick | 禁疗 30 秒（`BAN_TICKS` = 600 tick）+ 流血 II `BLEED_TICKS` = 160 tick + 细雪 / 飞刀实体 | `entity/XueliKnifeEntity.java:36-42`、`entity/XueliKnifeEntity.java:143-152`、`entity/EasterBossEntity.java:4427` |
| ⑤ 女仆护法 `XueliMaids` | 轮换第 5 招，两次召唤间隔 `TRIGGER_CD` = 800 tick，同一时间只留一批 | 无 | 一次 `BATCH_MIN` = 1 ~ `BATCH_MAX` = 3 批 × `PER_BATCH` = 2 位，合计 `MIN_TOTAL` = 2 ~ `MAX_TOTAL` = 6 位 `touhou_little_maid:maid` | 只要有一位存活，雪黎**完全免伤**（无视无敌的伤害与女仆本身受的真实玩家伤害除外）；每位女仆每秒回 `HEAL_PERCENT_PER_MAID` = 0.001（满 6 位 0.6%/秒） | 无 / 女仆（计分板标签与持久化标记都是 `ftboss_xueli_guard`） | `boss/ai/XueliMaids.java:52-71`、`boss/ai/XueliMaids.java:141-166` |
| 女仆护法辅助规则 | 存活缓存 10 tick，全量重扫 100 tick，刚召唤 `graceTicks` = 20 tick 宽限 | 无 | 离雪黎 > `FOLLOW_DISTANCE` = 16.0 格或卡墙、窒息时传送到她身边 | 动作栏提醒每 `REMIND_CD` = 100 tick，被挡伤害提示每 `BLOCK_NOTIFY_CD` = 40 tick，广播半径 `PLAYER_RANGE` = 48.0 格 | 无 / 无 | `boss/ai/XueliMaids.java:66-71`、`boss/ai/XueliMaids.java:131-138`、`boss/ai/XueliMaids.java:433-435` |
| ⑥ 自爆分身 `skillXueliClones` | 轮换第 6 招；另有独立冷却 `XUELI_CLONE_CD` = 600 tick（上场先等 6 × 20 tick），场上没有活着的分身且冷却到点就召一批 | `quicker(30)` = 11 tick | 一次 `XUELI_CLONE_COUNT` = 2 个迷你雪黎，出生在本体 3.0 格环上 | 分身血量 `XUELI_CLONE_HEALTH` = 80.0、缩放 `MINI_SCALE` = 0.55、引信 `XUELI_CLONE_FUSE` = 100 tick；爆炸半径 `XUELI_BOMB_RADIUS` = 4.5 格、基准 `XUELI_BOMB_DAMAGE` = 26.0，按距离线性衰减到 25% 再 × `BOSS_DAMAGE_MULT`；被提前打死走弱化版（半径 ×0.75 = 3.375 格、基准 `XUELI_BOMB_DAMAGE_ON_DEATH` = 14.0） | 细雪 + 击退 1.1 / 迷你分身 | `entity/EasterBossEntity.java:4427-4438`、`entity/EasterBossEntity.java:4478-4538`、`entity/EasterBossEntity.java:4548-4576` |
| ⑦ 天降瞬砸 `skillXueliSlam` | 轮换第 4 招（上场第二招）；蓄势与间隔期 `castSkill` 直接 return，不接别的招 | 每发悬停 `XUELI_SLAM_WARN_TICKS` = 7 tick | 落点在起手瞬间定死；瞬移到落点上方 `XUELI_SLAM_HEIGHT` = 12.0 格，砸落判定半径 `XUELI_SLAM_RADIUS` = 8.0 格 | 每发 `XUELI_SLAM_DAMAGE` = 40.0；一次施放 `XUELI_SLAM_COMBO` = 5 连砸，每发之间落地停 `XUELI_SLAM_GAP_TICKS` = 20 tick | 眩晕 `XUELI_SLAM_STUN_TICKS` = 20 tick + 细雪，无击退 / 无 | `entity/EasterBossEntity.java:415-425`、`entity/EasterBossEntity.java:4672-4810` |
| 眩晕 `stunPlayer` | 跟着雪黎实例走，她死亡或离场即结束 | 无 | 单体；每 tick 把玩家硬拉回定身锚点，并清掉水平速度（保留下落） | 停疾跑、打断使用物品，刷新缓慢 amplifier 6 共 15 tick，头顶画 3 颗 `CRIT` 金星 | 眩晕 / 无 | `entity/EasterBossEntity.java:4817-4866` |
| 细雪冻结 `applyXueliFrost` | 她每一招命中都触发（近战、飞刀、汤锅溅射、平底锅、压路、分身自爆、瞬砸） | 无 | 单体 | 每次叠 `XUELI_FROST_PER_HIT` = 90 点冻结进度，上限 = `getTicksRequiredToFreeze()` + `XUELI_FROST_OVER` = 60；叠满转原版冻结伤害 | 缓慢 I 60 tick / 无 | `entity/EasterBossEntity.java:4298-4319` |
| 清理 | 雪黎死亡或被移除时 | 无 | 无 | `xueliMaids.reset()` 收掉女仆、`clearMinis` 收掉自爆分身；`reset()` 顺手清悬停状态与眩晕表 | 无 / 无 | `entity/EasterBossEntity.java:6451`、`entity/EasterBossEntity.java:6489` |
| **最终出伤提高 1/2** | 常驻（2026-09-21 用户要求「雪黎伤害提高1/2」） | 无 | 雪黎本体**及其女仆 / 自爆分身**造成的全部可归因伤害 | 上表全部数值最终 ×**1.5**（口径："提高 1/2" = ×(1 + 1/2)）；女仆靠 `ManorFightMembers.owner` / `MinionBlessing` 追溯回本体，**提高分支故意不排除迷你分身**（自爆分身是她自己的招式） | 无 / 无 | `boss/ai/BossOutgoingDamageNerfs.java` |
| 武器命中附带 | 每次平底锅或擀面杖命中 | 无 | 单体 | 擀面杖命中直接挂粉碎轮，近战伤害走共享口径 | 细雪（`applyXueliFrost`）/ 无 | `entity/EasterBossEntity.java:1793-1795` |

- 机制说明：雪黎一个人占两条状态线 —— 七招轮换（上场第一招必是禁疗飞刀，第二招必是天降瞬砸 5 连砸）与「女仆护法」阶段的完全免伤；护法未破之前她不吃任何伤害，所以正确解法是先清完 2 到 6 位女仆再打本体，期间还要躲飞刀的 30 秒禁疗、粉碎轮的 60 tick 持续伤害与 8 格雪圈的眩晕。★ 2026-09-21 按用户要求「雪黎伤害提高1/2」后，上表所有数值统一 ×1.5（汤锅 40 → **60**、平底锅 30 → **45**、压路 12 → **18**、粉碎轮每 tick 5 → **7.5**（60 tick 合计 450）、飞刀 12/擦身 7.2 → **18 / 10.8**、自爆 26 → **39**、瞬砸 40 → **60**）；她的女仆与自爆分身打出的伤害同样跟着提高。

## 十一、麻雀（`variant` 9，场外支援位，不进轮换）

| 技能 / 机制 | 触发与冷却 | 前摇 | 判定半径 / 线路 | 伤害与数值 | 附带减益 / 召唤物 | 出处 |
|---|---|---|---|---|---|---|
| 定位与血量 | 常驻 | 无 | 不攻击玩家，近战目标判定对麻雀返回 false，也不跑末影珍珠追击 | 支援身份固定 `SPARROW_HEALTH` = 500.0；未转支援前本体基础生命 `ManorSupportRules.BASE_HEALTH` = 1500 | 无 / 无 | `boss/SparrowSupport.java:53`、`util/ManorSupportRules.java:7-8`、`boss/ai/ManorHealthBalance.java:25-29`、`entity/EasterBossEntity.java:2780` |
| 进场判定 | 每 `CHECK_INTERVAL` = 200 tick 扫一次（独立 Boss 掷概率；庄园正式战按波次计划） | 无 | 玩家 48 格内有彩蛋 Boss（`FIGHT_RANGE` = 48.0）且确实在交战，全场没有存活的麻雀 | 庄园计划从 11 位中抽 4~10 位出场（`MIN_APPEARANCES` = 4、`MAX_APPEARANCES` = 10）；独立 Boss 单人基准概率 `CHANCE` = 0.5；同时只允许 1 只（跨维度唯一） | 无 / 麻雀本尊 | `boss/SparrowSupport.java:36-46`、`boss/SparrowSupport.java:79-132`、`util/SparrowAppearanceRules.java:10-31` |
| 支援节奏 | 每 `SPARROW_HEAL_TICKS` = 60 tick 治疗；每 `SPARROW_CLEANSE_TICKS` = 100 tick 净化；每 `SPARROW_SHARE_TICKS` = 20 tick 双向共享正面效果 | 无 | 被支援 Boss = 32 格内最近的其它非迷你彩蛋 Boss；净化优先 32 格内另一位带减益的 Boss，没有则净化被支援 Boss | 治疗 `getMaxHealth() × SPARROW_HEAL_PERCENT` = 0.005（0.5%）并给 `SPARROW_BUFFS` = 3 个随机增益（各 15 秒）；净化清掉目标身上全部负面效果 | 无 / 无 | `entity/EasterBossEntity.java:2745-2764`、`entity/EasterBossEntity.java:2807-2833`、`entity/EasterBossEntity.java:2965-3000` |
| 伤害分摊 | 常驻 | 无 | 48 格内有存活麻雀 | `SPARROW_DAMAGE_SHARE` = 0.50（替被支援 Boss 吃 50%，直接从她自己血里扣，不走她的受击减伤） | 无 / 无 | `entity/EasterBossEntity.java:2751`、`entity/EasterBossEntity.java:2869-2905` |
| 禁疗 | 每次「复制玩家增益」成功时判定 | 无 | 单体 | `SPARROW_NOHEAL_CHANCE` = 0.10 使该玩家 30 秒无法回血（`BAN_TICKS` = 600 tick） | 禁疗 / 无 | `entity/EasterBossEntity.java:2753`、`entity/EasterBossEntity.java:3596-3602`、`boss/ai/HealBan.java:45` |
| 沉默与驱散 `SparrowDisruption` | 支援有效时每 `INTERVAL_TICKS` = 200 tick 一次 | 无 | 被支援 Boss 48 格内每位存活生存/冒险玩家（排除创造、旁观、死亡与 Forge FakePlayer） | 从主物品栏 36 格（含快捷栏）与副手的非空格中随机抽 1 格：该物品类型进入 `SILENCE_TICKS` = 20 tick 冷却，并拦截使用/蓄力/交互/近战/挖掘/本模组武器技能（含进化武器），已有更长冷却不缩短；另独立移除 1 个非 ambient 正面效果 | 物品沉默 20 tick + 随机清 1 个正面效果 / 无 | `util/SparrowDisruptionRules.java:7-22`、`boss/ai/SparrowDisruption.java:42-95`、`boss/ai/SparrowDisruptionTargets.java:10-12` |
| 随机借招 `SparrowBorrowedSpells` | 支援有效时每 `ManorSupportRules.INTERVAL` = 200 tick 一次，麻雀不在原技能或代施法读条中才起手 | 沿用被借法术的读条 | 先等概率抽 11 位庄园 Boss 之一，再抽该 Boss 已启用的招牌法术（共 32 种，不含麻雀自身 3 种）；目标锁支援 Boss 当前有效敌对玩家，失效时找 24 格内最近有效玩家 | 走既有铁魔法施法、预警、伤害与场地管线，不切变体；独立施法状态，与其它委托施法互斥；友伤保护覆盖麻雀、庄园 Boss、同场召唤物与队友 | 借来的法术效果 / 无 | `boss/ai/SparrowBorrowedSpells.java:78-116`、`util/ManorSupportRules.java:9-19`、`util/ManorSpellRules.java:14-49` |
| 千羽散华 `FEATHER_FAN` | 冷却 `COOLDOWN_TICKS` = 160 tick（8 秒）；首轮 `INITIAL_DELAY` = 40 tick 加实体 id 偏移 0~19 tick | 沿用铁魔法长读条 | 5 枚飞羽服务端扫掠（`FEATHERS` = 5、`AREA_RADIUS` = 8.0 格、`COLLISION_RADIUS` = 0.28 格、速度上限 `SPEED` = 1.38 格/tick），单枚寿命 `LIFETIME` = 160 tick | 单枚每次有效命中独立累计，第 3 次（`HITS` = 3）同一 tick 消失；随机选有效目标、有多人时优先换人，单人可连续命中同一人；成功命中附 `HEAL_BAN_TICKS` = 100 tick 禁疗（只刷新更短的，不缩短更长或无限禁疗） | 禁疗 100 tick / 无 | `util/MaqueFlightRules.java:4-25`、`util/MaqueRicochetRules.java:4-16`、`integ/spells/MaqueFeatherFlights.java:44-66`、`integ/spells/MaqueFeatherFlights.java:85-158`、`integ/spells/MaqueFeatherCombat.java:134-143` |
| 多人登场额度 `SparrowMultiplayerRules` | 每场战斗按有效参战人数记录峰值 | 无 | 有效参战玩家 = 在线、存活、未移除、非创造/旁观，且在被支援 Boss 48 格内（庄园战为已登记召唤者/参战者，锚点 96 格内也算；独立 Boss 取 60 秒内造成过伤害的玩家与当前目标） | 额度 `quota(players)` = max(1, 有效参战人数)：1 人 1 次、2 人 2 次、3 人 3 次，额度是上限、不保证用完。独立 Boss 每轮判定概率 `1 − 0.5^quota` = 50% / 75% / 87.5%；庄园原未选中场次的追加概率 `1 − 0.75^(quota−1)` = 2 人 25%、3 人 43.75% | 无 / 无 | `util/SparrowMultiplayerRules.java:9-31`、`boss/SparrowMultiplayerSupport.java:37-76`、`boss/SparrowMultiplayerSupport.java:116-129` |
| 走位与寿命 | 离友军 > `SPARROW_TELEPORT_RANGE` = 10.0 格时把自己传送过去，冷却 `SPARROW_TELEPORT_CD` = 60 tick | 无 | 地面寻路跟随被支援 Boss | 寿命 `SPARROW_LIFE_TICKS` = 1800 tick（90 秒后飞走）；IMPOSSIBLE 难度不设寿命 | 无 / 无 | `entity/EasterBossEntity.java:2765-2771`、`entity/EasterBossEntity.java:2784`、`entity/EasterBossEntity.java:3437-3455` |
| 演出默认位 | 开场演出结束即留在场地当支援位 | 无 | 无 | `DEFAULT_VARIANT` = 9 | 无 / 无 | `boss/HiddenBossIntro.java:39` |

- 机制说明：麻雀不进轮换，但她一在场就替被支援 Boss 吃掉 50% 伤害、每 3 秒回 0.5% 最大生命并塞 3 个增益、每 5 秒清掉一位友军身上的全部减益；每 10 秒还会抽一件玩家物品沉默 20 tick 并清一个正面效果、随机借一位庄园 Boss 的招牌法术，另用 8 秒冷却的千羽散华打出 5 枚各 3 次命中的飞羽（命中附 5 秒禁疗）。多一人参战，每位 Boss 的麻雀额度 +1，独立 Boss 的登场判定也从 50% 依次升到 75% / 87.5%，所以清场时优先点掉她往往比直接打 Boss 更划算。
