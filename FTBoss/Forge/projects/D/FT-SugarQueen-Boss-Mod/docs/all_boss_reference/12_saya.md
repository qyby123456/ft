# 沙耶（茶树古树）

本节覆盖常规 Boss 第 3 只：实体 id `ftboss:tea_tree`。她扎根原地不移动，战斗全部由 `tickServer()` 驱动：根刺爆发、茶毒飞沫、贴身轻推、铁魔法连发、7 个招牌法术，加上「茶心汲取」与茶芽小妖编队。半血进入二阶段「茶心觉醒」并开启「茶芽庇佑」免伤，首次死亡还会触发一次「茶根再生」（二命）。时间单位统一 tick（1 秒 = 20 tick），距离单位统一格。

## 实体与判定箱

| 项目 | 数值 | 出处 |
|---|---|---|
| 实体 id | `ftboss:tea_tree` | `entity/FTEntities.java:167` |
| 判定箱 | 宽 1.3 格 × 高 2.1 格 | `entity/FTEntities.java:168` |
| 生物分类 | `MobCategory.MONSTER` | `entity/FTEntities.java:181` |
| 名称键 | `boss.ftboss.saya`（中文「沙耶」） | `entity/TeaTreeBossEntity.java:189` |
| 血条 | `ServerBossEvent`，颜色 `GREEN`、叠加 `PROGRESS` | `entity/TeaTreeBossEntity.java:188` |
| AI | `registerGoals()` 为空，无原版寻路与近战 AI | `entity/TeaTreeBossEntity.java:205` |

### 血条

- 显示范围 `BOSS_BAR_RANGE` = 48.0 格，范围内的玩家加入、范围外的移除；进度 = 当前生命 / 最大生命，每 tick 刷新；受击时强制可见，死亡后 `BossBars.untrack`（`entity/TeaTreeBossEntity.java:138`、`:564`、`:562`、`:219`、`:554`）。
- 被标记为茶小妖的实体（`isTeaMinion()`）隐藏血条（`entity/TeaTreeBossEntity.java:558`）。
- BGM：开战后每 tick 向全局 `BossMusic` 登记；`FTSounds.MUSIC_ENABLED` 为假时不播（`entity/TeaTreeBossEntity.java:244`）。

## 基础属性

| 属性 | 数值 | 出处 |
|---|---|---|
| 最大生命 `MAX_HP` | 340 | `entity/TeaTreeBossEntity.java:156` |
| 护甲 `ARMOR` | 5.0 | `entity/TeaTreeBossEntity.java:235` |
| 攻击伤害 `ATTACK_DAMAGE` | 3.5（贴身轻推使用） | `entity/TeaTreeBossEntity.java:236` |
| 移动速度 `MOVEMENT_SPEED` | 0.0（扎根） | `entity/TeaTreeBossEntity.java:237` |
| 击退抗性 `KNOCKBACK_RESISTANCE` | 0.85（`BOSS_RESISTANCE`，只吃 15% 击退） | `entity/TeaTreeBossEntity.java:238`、`util/BossKnockback.java:31` |
| 索敌距离 `FOLLOW_RANGE` | 40.0 格 | `entity/TeaTreeBossEntity.java:239` |
| 实际索敌 | 仇恨目标优先，否则 20 格内最近的有效玩家（排除创造与旁观） | `entity/TeaTreeBossEntity.java:272`、`boss/ai/BossTargets.java:40` |

## 常驻机制与成长

| 机制 | 规则与数值 | 出处 |
|---|---|---|
| 茶心焕新 | 每 10 tick 检查；无该效果或剩余 < 120 tick 时补 `herbalbrews:renewal` 600 tick、等级 II（amplifier 1） | `entity/TeaTreeBossEntity.java:488`、`:495` |
| 负面效果清除 | 每 20 tick 移除自身全部非增益效果 | `entity/TeaTreeBossEntity.java:302-306` |
| 负面效果免疫（事件层已关闭） | Forge 事件层全局 `DENY` 已停用（`IMMUNE_TO_DEBUFFS` = false）；沙耶本体仍每 20 tick 自清非增益效果，实际只能短暂存在；茶芽小妖没有自清逻辑，会正常吃减益 | `boss/BossHandler.java:409`、`entity/TeaTreeBossEntity.java:302-306`、`entity/TeaMinionEntity.java` |
| 阳光与火焰 | `isSunBurnTick()` 返回 false（白天不燃烧）；`displayFireAnimation()` 返回 false（不着火焰图层） | `entity/TeaTreeBossEntity.java:600`、`:135` |
| 增益轮盘 | 每 110 + `random(60)` tick 随机 2 个正面效果，时长 100 + `random(120)` tick、等级 0~2 | `entity/TeaTreeBossEntity.java:435`、`:443` |
| 生命提升成长 | 每 1200 tick（60 秒）+1 级 `HEALTH_BOOST`（每级 +4 最大生命），上限 10 级（+40）；需 48 格内有目标；升级同时 `heal(4)` | `entity/TeaTreeBossEntity.java:148`、`:150`、`:462` |
| 全局限伤 | 结算后的实际扣血伤害 > `MAX_FINAL_DAMAGE` = 300.0 时本次血量伤害归零（不是截断；恰好 300 或更低放行；不按原始伤害判断；`/kill` 保留）；原始 500 点规则与旧 `LIMIT_ENABLED` 限伤继续停用；本 Boss 适用，玩家、外部 Boss、鼠群小怪、迷你 Boss、召唤物不适用；沙耶登记的 `DAMAGE_CAP` 56.0 单次上限与 40 格反逃课距离仍未启用 | `boss/ai/FinalBossDamageGuard.java:19`、`mixin/FinalBossDamageMixin.java:14`、`boss/ai/BossDamageGate.java` |
| 免疫伤害类型 | 伤害类型路径或 msgId 命中 `hemorrhage`、`bleed`、`doom`、`true_damage` 等关键字时直接 0 伤害 | `boss/ai/BossDamageGate.java:120` |

## 主技能

| 技能 | 触发与冷却 | 前摇 | 范围 | 伤害 | 附带 | 出处 |
|---|---|---|---|---|---|---|
| 根刺爆发（`roots`） | 距离 1.5~16 格；每 100 tick 尝试一次（`FANG_CD`，二阶段 `FANG_CD_P2` = 62）；动作进行中只继续铁魔法，不起新的根刺 / 茶毒 / 轻推 | 总时长 26 tick（茶毒飞沫为 20 tick，`BossActionState.start`），第 12 tick 结算（`release`）；前摇每 3 tick 沿目标方向 2~16 格撒 `COMPOSTER` 粒子 | 沿施法瞬间记录的水平方向，每 1.6 格（`step`）生成 1 根，共 9~11 根（`9 + random.nextInt(3)`） | ?（由原版 `EvokerFangs` 结算，本模组未覆写） | 每根延迟 (i-1)×2 tick 破土；音效 `ROOTED_DIRT_PLACE` | `entity/TeaTreeBossEntity.java:142`、`:144`、`:86`、`:94`、`:296`、`:404` |
| 茶毒飞沫（`poison`） | 距离 < 16 格；每 160 tick 尝试一次（`SPIT_CD`，二阶段 `SPIT_CD_P2` = 100） | 总时长 20 tick，第 8 tick 结算；前摇每 3 tick 在头顶 1.4 格撒 `SNEEZE` | 需目标碰撞箱 `inflate(0.85)` 含施法瞬间记录的眼位，且距离 ≤ 16 格并有视线 | 2.5 点 `indirectMagic`（`spitTeaPoison` 内字面常量，直接 `hurt`，不乘任何倍率） | 中毒 220 tick（11 秒）等级 III（amplifier 2）；音效 `CAVE_VINES_PICK_BERRIES` | `entity/TeaTreeBossEntity.java:143`、`:145`、`:305`、`:422` |
| 贴身轻推（`melee`） | 距离 < 3.0 格；每 30 tick 一次（`contactCd` = 30）；`doHurtTarget` 只启动前摇 | 命中判定第 6 tick，动作总时长 14 tick，判定距离 3.0 格（`BossMeleeWindup(6, 14, 3.0)`） | 3.0 格内 + 有视线 + `BossTargets.valid` | 3.5（`ATTACK_DAMAGE` 字面常量，走原版 `Mob.doHurtTarget`） | 音效 `PLAYER_ATTACK_SWEEP` + `CRIT` 粒子（6 颗） | `entity/TeaTreeBossEntity.java:58`、`:197`、`:313`、`:315`、`boss/ai/BossMeleeWindup.java:20` |
| 铁魔法连发 | 不在施法中时每 62 tick 施放 1 个（`castCd`，二阶段 44 tick）；每次施放前还要过 `BossSpellPacing.tryLegacyCast` 施法闸：25% 概率放行（`LEGACY_CHANCE`），放行后 240 tick 内不放行（`LEGACY_INTERVAL_TICKS`），未放行则 60 tick 后再试（`LEGACY_RETRY_TICKS`），正在咏唱直接拒绝 | 由所选铁魔法法术自身决定 | 队列每次填 2 个不同法术（`n` = 2）；狂暴等级 ≥1 时 80% 概率改抽史诗 + 传说池（`spellPoolRare(8, 3)`）；35% 概率追加 1 个沙耶专属自施法法术 | ?（由所选铁魔法法术决定） | 法术等级 = `max(最低级, min(6 + 狂暴等级, 最高级))`；`CastType.LONG` 施法时间 × 2/3（`bossCast`，向上取整） | `entity/TeaTreeBossEntity.java:321`、`:326`、`:345`、`:349`、`:352`、`:357`、`:373`、`integ/spells/BossSpellPacing.java:15`、`:17`、`integ/spells/BossSpellTuning.java:27` |
| 招牌法术（7 个 `tea_tree` 系法术） | 目标距离 ≤ 32 格且有视线（超出即取消本次蓄力）；开局延迟 60 tick + `floorMod(实体 id, 20)`，之后每 60 tick 尝试一次（`SIGNATURE_INTERVAL_TICKS`） | 基础 32 tick（`CAST_TICKS`），常规 Boss 经 `BossSpellTuning.bossCast` 缩到 × 2/3 → 22 tick | 起手需目标在 24 格内（`BossSpellRules.RANGE`）；瞄准距离上限同为 24 格；各法术范围见下表 | 运行时计算 = 基础威力（`signaturePower`）× `SPELL_POWER` × 学派强度 × 配置 `POWER_MULTIPLIER`（`scaledPower`） | 单法术节流 `max(160, ceil(法术冷却 × 0.75))` tick（`signatureCooldown`，法术冷却 = 配置秒数 × 20）；蓄力超 60 tick 取消 | `integ/spells/BossSpellCombat.java:57`、`:77`、`:80`、`:94`、`BossSpellPacing.java:13`、`:14`、`:22`、`BossSpellTuning.java:33`、`:55` |
| 茶心汲取 | 每 240 tick（12 秒，`absorbCd`）尝试一次；需未满血（< 最大生命 − 0.5） | 即时结算，无前摇 | 24 格内（`ABSORB_RANGE`）、最多 3 株茶芽小妖（`ABSORB_MAX_PER_CAST`） | 非伤害：每株回复最大生命 × 7.5% | 每株 +1 层「茶心共鸣」（上限 4 层 `ABSORB_STACK_CAP`），刷新 400 tick 增益 | `boss/ai/TeaTactics.java:96`、`:99`、`:100`、`:117` |

## 招牌法术

`ftboss:tea_tree` 在 `BossSpellRules` 中登记了 7 个招牌法术（`util/BossSpellRules.java:74`）。通用规则：

- 释放前需有合法目标、目标距离 ≤ 32 格且有视线；瞄准点离沙耶超过 32 格则整段取消（`integ/spells/BossSpellCombat.java:41`、`BossSpellEffects.java:120`）。
- 节奏：首次开火为开局 60 tick + `floorMod(实体 id, 20)`；之后每 60 tick 轮询一次；同一法术节流 `max(160, ceil(法术冷却 × 0.75))` tick，其中「法术冷却」取 `AbstractSpell.getSpellCooldown()`（= 配置秒数 × 20，即换算成 tick 后的值；20 秒的法术门禁因此是 300 tick）（`BossSpellCombat.java:77`、`:94`、`BossSpellPacing.java:14`、`:22`）。
- 前摇：`CastType.LONG`，基础 32 tick（`BossSpellRules.CAST_TICKS`），常规 Boss 经 `BossSpellTuning.bossCast` 缩到 × 2/3 → 22 tick；目标数上限 16（`MAX_TARGETS`）。下表「配置冷却」取 `Kind.cooldown`（秒，写入 `DefaultConfig.setCooldownSeconds`，括号里给出按 `signatureCooldown` 折算的门禁 tick 数），不是同一个构造参数表里第 5 个字段 `Kind.mana`（基础蓝耗，实际蓝耗 = `mana + 40`）（`BossSpellRules.java:9`、`:12`、`:58`、`:61`、`BossSpellTuning.java:27`）。
- 基础威力取 `BossSpellTuning.signaturePower`：非辅助法术为基线 ×2，辅助法术（`TEA_SANCTUARY`、`TEA_RENEWAL`）不乘（`BossSpellTuning.java:33`）。实际伤害还要乘施法者 `SPELL_POWER`、学派强度与配置 `POWER_MULTIPLIER`，故最终值不固定（`BossSpellTuning.java:55`）。
- 表中「基础威力」的基线来自 `signaturePower` 的 `case` 分支（`BossSpellTuning.java:36`、`:38`、`:40`、`:43`、`:44`、`:47`、`:48`），「配置冷却」来自 `util/BossSpellRules.Kind` 里 7 条 `tea_tree` 记录的 `cooldown` 字段（`BossSpellRules.java:16`、`:23`、`:32`、`:33`、`:34`、`:35`、`:36`）。

| 法术 | 法术 id | 基础威力 | 配置冷却 | 脉冲节奏 | 范围与附带 |
|---|---|---|---|---|---|
| `TEA` | `camellia_blood_bloom` | 基线 30，×2 → 60 | `Kind.TEA.cooldown` = 20 秒 → 门禁 300 tick | 单段（0 tick） | 半径 4 格、最多 16 目标；命中后缓慢 IV 60 tick，并按实伤 15% 回血（单次上限 6） |
| `TEA_SANCTUARY` | `camellia_sanctuary` | 基线 10，辅助不乘 → 10 | `Kind.TEA_SANCTUARY.cooldown` = 24 秒 → 门禁 360 tick | 单段（0 tick） | 自身 + 4 格内友方；治疗量 = 威力，移除中毒，抗性提升 I 100 tick |
| `TEA_THORNS` | `briar_procession` | 基线 26，×2 → 52 | `Kind.TEA_THORNS.cooldown` = 18 秒 → 门禁 270 tick | 0 / 10 / 20 | 3 段沿朝向推进 4 / 8 / 12 格，半径 2.2 格，每目标只结算一次；缓慢 IV 45 tick |
| `TEA_POLLEN` | `scarlet_pollen` | 基线 21，×2 → 42 | `Kind.TEA_POLLEN.cooldown` = 26 秒 → 门禁 390 tick | 0 / 20 / 40 / 60 | 半径 4 格；中毒 I 60 tick + 虚弱 I 60 tick |
| `TEA_RENEWAL` | `camellia_rebirth` | 基线 8，辅助不乘 → 8 | `Kind.TEA_RENEWAL.cooldown` = 32 秒 → 门禁 480 tick | 0 / 20 / 40 | 5 格内友方（含自身）；每段治疗 = 威力，移除中毒 |
| `TEA_FLESH` | `flesh_bloom` | 基线 40，×2 → 80 | `Kind.TEA_FLESH.cooldown` = 26 秒 → 门禁 390 tick | 20（单段） | 半径 4 格；按实伤 20% 回血（单次上限 6）；缓慢 III 60 tick |
| `TEA_SUTURE` | `sanguine_suture` | 基线 24，×2 → 48 | `Kind.TEA_SUTURE.cooldown` = 24 秒 → 门禁 360 tick | 0 / 20 / 40 | 锁定 3 格内单体（且 18 格内、有视线）；按实伤 30% 回血（单次上限 4）；虚弱 I 45 tick |

## 阶段与转换

| 阶段 / 事件 | 触发条件 | 变化 | 出处 |
|---|---|---|---|
| 茶心觉醒（二阶段） | 生命 < 最大生命 50%，只触发一次 | 根刺冷却 100 → 62 tick；茶毒冷却 160 → 100 tick；铁魔法间隔 62 → 44 tick；茶芽满编 3 → 5、召唤间隔 150 → 90 tick；音效 `BEACON_ACTIVATE` | `entity/TeaTreeBossEntity.java:279`、`:289`、`:326`、`boss/ai/TeaTactics.java:64` |
| 茶芽庇佑（护盾） | 进入二阶段时 `buds.trigger()`；区块重载后若场上仍有存活守护茶芽则自动重新接管护盾 | 召唤 5 株守护茶芽（`SayaBuds.BUD_COUNT`）；只要 1 株存活，沙耶 `hurt()` 直接返回 false（完全免伤）；护盾期间每 5 tick 冒一次护盾能量粒子 | `boss/ai/SayaBuds.java:71`、`:31`、`:109`、`:130`、`entity/TeaTreeBossEntity.java:211` |
| 护盾破除 | 64 格判定范围（`SayaBuds.SCAN`）内守护茶芽全部死亡 | 音效 `BEACON_DEACTIVATE` + `GLASS_BREAK` 并广播，沙耶恢复可受伤 | `boss/ai/SayaBuds.java:33`、`:117`、`:120` |
| 全 Boss 狂暴 | 生命 ≤ 50% 进入狂暴 I，≤ 25% 进入狂暴 II | I：迅捷 I + 抗性提升 I；II：追加力量 I + 再生 I；每次 80 tick、每 20 tick 刷新 | `boss/ai/BossRage.java:40`、`:79` |
| 连死怜悯 | 同一玩家在同一只沙耶下连续死亡 5 次（`BossMercy.LIMIT`） | 该只沙耶离场（无掉落、无经验），48 格内召唤物一并清理 | `boss/ai/BossMercy.java:44`、`:295` |

### 二命：茶根再生

- 触发：首次死亡时由 `BossHandler.onBossDeath` 调用 `tryRevive()`；`secondLifeUsed` 已置位或实体已非存活则返回 false（`entity/TeaTreeBossEntity.java:515`、`boss/BossHandler.java:416`）。
- 效果：取消本次死亡，生命设为 `getMaxHealth() * 0.3`，清空全部效果，根刺冷却置 40 tick、茶毒冷却置 70 tick（`entity/TeaTreeBossEntity.java:518`）。
- 召唤物：`buds.reset()` 先清场，再 `buds.trigger()` 重新召唤 5 株守护茶芽并重新开启免伤，因此必须再次清空茶芽才能击杀（`entity/TeaTreeBossEntity.java:522`）。
- 演出：`TOTEM_USE` 音效；整场只触发一次（`entity/TeaTreeBossEntity.java:525`）。

## 召唤物：茶芽小妖与守护茶芽

| 项目 | 茶芽小妖 | 守护茶芽 | 出处 |
|---|---|---|---|
| 实体 id | `ftboss:tea_minion` | 同一实体，标记 `ftboss_guard_bud` | `entity/FTEntities.java:55`、`entity/TeaMinionEntity.java:50` |
| 判定箱 | 宽 0.8 格 × 高 1.3 格 | 宽 0.8 格 × 高 1.3 格 | `entity/FTEntities.java:56` |
| 最大生命 | 20.0 | 20.0 + 100%（`AttributeModifier` 的 `MULTIPLY_TOTAL 1.0`） | `entity/TeaMinionEntity.java:62`、`:81` |
| 攻击伤害 | 1.5 | 1.5 | `entity/TeaMinionEntity.java:63` |
| 攻击速度 | 1.5 | 1.5 | `entity/TeaMinionEntity.java:64` |
| 移动速度 | 0.28 | 0.28 | `entity/TeaMinionEntity.java:65` |
| 索敌距离 | 24.0 格 | 24.0 格 | `entity/TeaMinionEntity.java:66` |
| 击退抗性 | 0.3 | 0.3 | `entity/TeaMinionEntity.java:67` |
| 经验 `xpReward` | 4 | 12 | `entity/TeaMinionEntity.java:57`、`:87` |
| 行为 | 近战 `MeleeAttackGoal(1.2, true)`、看玩家 8 格、随机游走 0.8；目标选择只锁玩家 | 同左，并强制持久化、显示名称 | `entity/TeaMinionEntity.java:91`、`:85` |
| 名称键 | `entity.ftboss.tea_minion` | `entity.ftboss.guard_bud` | `boss/ai/TeaTactics.java:85`、`boss/ai/SayaBuds.java:87` |

召唤与汲取规则（`boss/ai/TeaTactics` 与 `boss/ai/SayaBuds` 两个组件驱动）：

| 召唤与汲取规则 | 数值与规则 | 出处 |
|---|---|---|
| 满编数量 | 常态 3 只，沙耶生命 < 50% 时 5 只 | `boss/ai/TeaTactics.java:78` |
| 召唤间隔 | 首次 150 tick，之后常态 150 tick（7.5 秒）、半血 90 tick（4.5 秒） | `boss/ai/TeaTactics.java:36`、`:64` |
| 每轮补充 | 最多补 3 只，随机方位、半径 2.5 + `random×4` 格 | `boss/ai/TeaTactics.java:79`、`:83` |
| 编队维护 | 48 格内（`MINION_RANGE`）所有非守护茶芽回满血、同步沙耶目标、`setPersistenceRequired` | `boss/ai/TeaTactics.java:27`、`:67`、`:72` |
| 守护茶芽生成 | 二阶段与二命各 5 株，半径 4.5 + `random×2.5` 格，角度均分 + 随机 0.4 弧度 | `boss/ai/SayaBuds.java:31`、`:82` |
| 茶芽庇佑供能 | 每 40 tick 给沙耶 再生 II 140 tick + 抗性提升 II 140 tick | `boss/ai/SayaBuds.java:37`、`:168` |
| 护盾反馈 | 免伤期间被攻击时动作栏提示并播 `SHIELD_BLOCK`，限频 40 tick；剩余株数每 100 tick 提醒，范围 48 格 | `boss/ai/SayaBuds.java:41`、`:39`、`:35` |
| 茶心汲取判定 | 每 240 tick 一次；需未满血（< 最大生命 − 0.5）；24 格内最多 3 株；守护茶芽不可被汲取 | `boss/ai/TeaTactics.java:99`、`:100`、`:30`、`:109` |
| 汲取收益 | 每株回复最大生命 × 7.5%，+1 层共鸣；共鸣上限 4 层；被汲取的茶小妖 `discard()` 静默消失、无掉落 | `boss/ai/TeaTactics.java:117`、`:120`、`:114` |
| 共鸣增益 | 力量 400 tick（1 层 I 级、≥2 层 II 级）；抗性提升 400 tick（≥3 层 II 级）；伤害吸收 400 tick（≥2 层 II 级） | `boss/ai/TeaTactics.java:122`、`:123`、`:124` |
| 召唤物继承增益 | 每 60 tick（`PERIOD`）把自身增益降级（`min(1, 等级 − 1)`、最高 I 级）分享给 40 格内最多 8 只召唤物（`MAX_MINIONS`），每只最多 3 种（`MAX_EFFECTS`）、持续 140 tick（`DURATION`） | `boss/ai/MinionBlessing.java:33`、`:34`、`:35`、`:36`、`:98`、`entity/TeaTreeBossEntity.java:234` |
| 死亡清场 | 死亡或实体被移除时清理 80 格内（`SummonSweep.RANGE`）茶芽小妖，并移除全部守护茶芽 | `entity/TeaTreeBossEntity.java:537`、`:581`、`boss/ai/SummonSweep.java:29` |

## 肉视（荆棘头冠）

| 规则 / 参数 | 数值与效果 | 出处 |
|---|---|---|
| 触发条件 | 头盔槽佩戴 `ftboss:thorn_crown`（`ThornCrownItem.isWornBy`），玩家存活、非旁观且为当前相机实体 | `client/ThornCrownOverlay.java:27`、`:28` |
| 叠加位置 | 注册在 `VanillaGuiOverlay.VIGNETTE` 之上 | `client/ThornCrownOverlay.java:25` |
| 画面底板 | 全屏填充 `BLOOD_TINT = 0x0C681626`（ARGB，alpha = `0x0C`） | `util/SayaFleshVisionRules.java:7` |
| 肉膜层 | `saya_flesh_frame.png`，alpha = `0.93 + 0.05 × heartbeat`（0.93~0.98） | `util/SayaFleshVisionRules.java:19` |
| 血管层 | `saya_flesh_veins.png`，alpha = `0.48 + 0.24 × heartbeat`（0.48~0.72） | `util/SayaFleshVisionRules.java:23` |
| 搏动周期 | `PERIOD_TICKS = 36`；相位 = (`tickCount` % 36 + partialTick) / 36 | `util/SayaFleshVisionRules.java:6`、`client/ThornCrownOverlay.java:29` |
| 搏动曲线 | 主峰在相位 0.23、宽度 0.075；次峰在 0.43、宽度 0.09、幅度 0.42；峰值截断为 1 | `util/SayaFleshVisionRules.java:11` |
| 贴图尺寸 | 1024 × 576 | `util/SayaFleshVisionRules.java:4` |
| 配套荆棘反击 | 近战受伤反弹实受伤害的 25%（`REFLECT_RATIO`，下限 2.0 `MIN_REFLECT_DAMAGE`、上限 6.0 `MAX_REFLECT_DAMAGE`），冷却 10 tick（`REFLECT_COOLDOWN_TICKS`）；爆炸与弹射物伤害不反弹 | `item/ThornCrownItem.java:30`、`:31`、`:32`、`:33`、`item/ThornCrownEffects.java:21` |

## 掉落

| 掉落物 | 数量 | 条件与说明 | 出处 |
|---|---|---|---|
| 沙耶石像 `ftboss:statue_saya` | 1 | 真死时由击杀钩子 `BossMercy.giveStatue` 发放，不要求玩家击杀 | `boss/ai/BossMercy.java:341`、`:354`、`block/FTBlocks.java:42` |
| 橡树树叶 | 24 | 死亡时必定掉落（`Items.OAK_LEAVES`） | `entity/TeaTreeBossEntity.java:541` |
| 橡树树苗 | 8 | 死亡时必定掉落（`Items.OAK_SAPLING`） | `entity/TeaTreeBossEntity.java:542` |
| 绽放的荆棘 | 1 | 剑：`Tiers.NETHERITE`、攻击伤害加成 4、攻速 −2.4 | `entity/TeaTreeBossEntity.java:544`、`item/FTBossItems.java:171` |
| 荆棘头冠 | 1 | 头盔：护甲 4（`DEFENSE`）、韧性 3.0（`TOUGHNESS`）、耐久 528（`DURABILITY`），佩戴触发肉视 | `entity/TeaTreeBossEntity.java:546`、`item/ThornCrownItem.java:27`、`:28`、`:29` |
| 经验球 | 150 | 另有 `xpReward = 150` | `entity/TeaTreeBossEntity.java:548`、`:187` |
| 茶心护坠 | 1 | 必掉；需 `doMobLoot` 开启且由玩家击杀，每只只结算一次 | `util/BossRelicRules.java:6`、`skill/BossRelicDrops.java:60` |
| 招牌法术卷轴 | 7 | 7 个 `tea_tree` 系法术各 1 张；需 `doMobLoot` 开启且由玩家击杀 | `integ/spells/BossSpellDrops.java:52` |
| 召唤物清理 | — | 死亡时清理 80 格内（`SummonSweep.RANGE`）茶芽小妖并移除守护茶芽 | `entity/TeaTreeBossEntity.java:534`、`:537`、`boss/ai/SummonSweep.java:29` |
