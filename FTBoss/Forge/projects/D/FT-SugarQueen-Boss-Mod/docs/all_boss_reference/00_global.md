# 全局规则与公共机制（所有 Boss 通用）

本节收录 `ftboss` 全模组 Boss 共用的伤害、缩放、强化、狂暴、限伤、怜悯、仇恨、脱困、支援、血条与 BGM 规则。时间单位一律 tick（1 秒 = 20 tick），距离单位一律「格」。表中出处省略前缀 `src/main/java/net/farmingtales/ftboss/`。

## 一、公共系统与生效开关

| 系统 | 关键常量或入口 | 数值 | 出处 | 状态 |
|---|---|---|---|---|
| 全局 500 点整下无效规则 | 原规则已取消 | — | — | 关闭 |
| 最终伤害 300 屏蔽 | 结算后实际扣血伤害 > `MAX_FINAL_DAMAGE` = 300 时本次血量伤害归零（恰好 300 或更低照常结算，详见第三节） | 300 | `boss/ai/FinalBossDamageGuard.java:19` | 生效 |
| 非庄园 Boss 陆地行走速度 ×2 | 普通陆地寻路结果在原限速之后 ×2（通常上限 0.1 → 0.2）；水中、无重力飞行、庄园 Boss、召唤物不加速 | ×2.0 | `boss/ai/BossWalkingSpeed.java:66`、`:74` | 生效 |
| 传奇怪物式限伤 | `LIMIT_ENABLED` | false | `boss/ai/BossDamageGate.java:46` | 关闭 |
| 流血与真实伤害完全免疫 | `IMMUNE_IDS` | hemorrhage、blood_loss、bloodloss、bleed、bleeding、doom、impending_doom、executioner、assassination、desecration、true_damage；`IMMUNE_NAMESPACES` 为空集，命名空间不作为免伤依据 | `boss/ai/BossDamageGate.java:120` | 生效 |
| 主世界限定召唤 | `SummonGate.OVERWORLD_ONLY` | true，白名单仅 `minecraft:overworld` | `boss/SummonGate.java:25` | 生效 |
| Boss 战禁飞 | `FLY_BAN_ENABLED` | false | `boss/BossHandler.java:563` | 关闭 |
| 运行时全局伤害钩子 | `BossDamageTuning` | 无运行时钩子，倍率直接写在各技能数值里 | `boss/ai/BossDamageTuning.java:11` | 不生效 |
| 石像交易 | `TradeHandler.load` | 读 `config/ftboss_trades.json`，无触发点 | `trade/TradeHandler.java:47` | 无触发点 |

## 二、全局伤害倍率

| 倍率 | 数值 | 作用对象 | 出处 |
|---|---|---|---|
| `BOSS_DAMAGE_MULT` | 4.0 / 3.0（≈ 1.333） | 庄园彩蛋 Boss（`EasterBossEntity`）全部「× BOSS_DAMAGE_MULT」技能 | `entity/EasterBossEntity.java:126` |
| 无运行时钩子 | — | 全局倍率已直接写进各技能数值与近战 `ATTACK_DAMAGE`，受伤事件里不再逐次相乘 | `boss/ai/BossDamageTuning.java:11`、`:21` |
| 真实伤害工具 | — | `BossDamageTuning.trueDamage` 只提供「无视护甲的真实伤害来源」，不参与倍率计算 | `boss/ai/BossDamageTuning.java:46` |
| `WEAPON_DAMAGE_MULT` | 5.0 | 彩蛋 Boss 近战：`ATTACK_DAMAGE` 基础 9 × 5 = 45，再叠 `BOSS_DAMAGE_MULT` 得 60 | `entity/EasterBossEntity.java:123`、`:584` |
| `SKILL_MULT` | 3.0 | 彩蛋 Boss 技能伤害与技能减益时长 | `entity/EasterBossEntity.java:124` |
| `SKILL_RADIUS_MULT` | 1.5 | 彩蛋 Boss 技能半径（只放大半径） | `entity/EasterBossEntity.java:125` |
| 非庄园伤害翻倍 | ×2.0 | 除 `EasterBossEntity` 外全部本模组 Boss 造成的所有伤害（含投射物与爆炸，按 `getOwner` 回溯弹体主人） | `boss/ai/NonManorDamageX2.java:43` |
| 屠夫最终出伤 | ×0.25 | 屠夫本体、复制体及带主人标记的小怪造成的全部可归因伤害；在既有非庄园 ×2 之后结算，因此相对技能字面量净倍率为 ×0.5 | `boss/ai/BossOutgoingDamageNerfs.java` |
| 折耳根最终出伤 | ×0.25 | 折耳根本体造成的全部可归因伤害，含近战、技能、投射物、铁魔法、无实体包装伤害、满连击额外虚空伤害与虚空侵蚀 buff。2026-09-21 当天先减半到 ×0.5，用户看实机后要求「虚空伤害 / 虚空buff / 伤害」三条**再减半**（累计保留 1/4） | `boss/ai/BossOutgoingDamageNerfs.java`、`effect/ShadowErosionEffect.java` |
| 青衣伤害减半 | ×0.5 | 庄园 5 号青衣造成的**全部**伤害：近战 / 庄园五技能那三招 / 铁魔法闪电 / 激流冲刺 / 投戟两招 / 三叉戟落雷与本体命中补伤；按 `getOwner` 回溯弹体主人。与非庄园倍率互不干扰 | `boss/ai/QingyiDamageHalved.java`；常量 `util/QingyiCombatRules.java` |
| 朵拉 / 雪黎出伤提高 | ×1.5 | 朵拉（variant 2）与雪黎（variant 10）**本体及其召唤物**造成的全部可归因伤害（雪黎女仆靠 `ManorFightMembers.owner`/`MinionBlessing` 追溯回本体；**不排除迷你分身** —— 雪黎的自爆分身也是她的招式）。口径："提高 1/2" = ×(1 + 1/2) = ×1.5 | `boss/ai/BossOutgoingDamageNerfs.java` |
| 庄园自适应加伤 | ×(1.0 + bonus)，bonus 落在 0.10 至 0.50 | 庄园五位（variant 0、1、4、6、7）非迷你分身打玩家 | `boss/ai/ManorAdaptiveDamage.java:50` |
| 按人数伤害倍率 | 1 + 0.15 × (n - 1) | 所有已登记血条的 Boss | `boss/ai/BossScaling.java:55` |

`BOSS_DAMAGE_MULT` 已包含「Boss→玩家 ×0.5」与「彩蛋系 ×4/3」两道修正，即 `2.0 × (0.5 × 4/3) = 4/3`，所有「× BOSS_DAMAGE_MULT」算出的数字就是玩家实吃伤害（`entity/EasterBossEntity.java:119`、`:126`）。

庄园自适应加伤公式：`bonus = clamp(0.10 + 0.20 × 生命进度 + 0.20 × 增益进度, 0.10, 0.50)`，其中生命进度 = `clamp((最大生命 - 20) / (100 - 20), 0, 1)`、增益进度 = `clamp(增益数量, 0, 10) / 10`（`util/ManorFiveCombatRules.java:8`、`:91`）；仅对存活、非创造、非旁观的玩家生效（`boss/ai/ManorAdaptiveDamage.java:37`）。

## 三、单次伤害上限与免伤

| 规则 | 数值或内容 | 出处 | 状态 |
|---|---|---|---|
| 最终伤害阈值 | 结算后实际扣血伤害 > 300 时本次血量伤害归零；恰好 300 或更低照常结算；不是把伤害截断成 300 | `boss/ai/FinalBossDamageGuard.java:19` | 生效 |
| 判定时机 | 护甲/韧性/附魔/抗性/伤害吸收/最终伤害事件全部结算完之后、真正扣血之前；不按原始攻击数值判断，不累计多次攻击 | `mixin/FinalBossDamageMixin.java` | 生效 |
| 适用范围 | 糖霜女皇、屠夫、沙耶、酒桶王、鼠群首领、故障机器人、答辩超人、苹果爵士、benger、潮宴鱼王与非迷你庄园 Boss（含麻雀支援与不可能挑战） | `boss/ai/FinalBossDamageGuard.java:23-31` | 生效 |
| 不适用对象 | 玩家、外部模组 Boss、鼠群小怪、迷你 Boss 与其它召唤物；鼠群主从循环整体不套用该规则 | `boss/ai/FinalBossDamageGuard.java:24-25` | — |
| 例外 | 管理员 `/kill`（`DamageTypes.GENERIC_KILL`）放行；NaN/无穷最终伤害被屏蔽；原始 500 点规则与旧 `LIMIT_ENABLED` 限伤继续停用 | `boss/ai/FinalBossDamageGuard.java:34`、`:39` | — |
| 提示节流 | 免疫与限伤提示均为动作栏消息，同一 Boss 40t（2 秒）最多提示一次，日志同样限频 | `boss/ai/BossDamageGate.java:269` | 生效 |
| 逐 Boss 单次上限 | sugar_queen 58、meat_mountain 58、tea_tree 56、wine_barrel 58、rat_boss 46、未登记兜底 48 | `boss/ai/BossDamageGate.java:65`、`:73` | 关闭 |
| 连击递减 | 窗口 24t、每次受击因子 -6、因子下限 20、因子满值 100、减伤下限 0.7 | `boss/ai/BossDamageGate.java:87` | 关闭 |
| 受击无敌帧 | `HURT_CD` = 30t | `boss/ai/BossDamageGate.java:97` | 关闭 |
| 反逃课距离 | sugar_queen 32、meat_mountain 32、tea_tree 40、wine_barrel 32、rat_boss 32、defect_core 36、兜底 32 | `boss/ai/BossDamageGate.java:76`、`:84` | 关闭 |

## 四、按玩家人数缩放

| 项 | 常量或公式 | 数值 | 出处 |
|---|---|---|---|
| 统计半径 | `RADIUS` | 48.0 格 | `boss/ai/BossScaling.java:22` |
| 参战人数 | `playersInFight` | 包围盒外扩 48 格内、存活且非创造非旁观玩家，至少按 1 计 | `boss/ai/BossScaling.java:35` |
| 血量倍率 | `HP_PER_PLAYER` | 1.25 的 (n - 1) 次幂 | `boss/ai/BossScaling.java:24` |
| 伤害倍率 | `DMG_PER_PLAYER` | 1 + 0.15 × (n - 1) | `boss/ai/BossScaling.java:26` |
| 重算周期 | — | 每 20t 一次 | `boss/ai/BossScaling.java:44` |
| 驱动范围 | `BossBars.forEach` | 仅登记过血条的 Boss | `boss/BossHandler.java:48` |
| 基准值与改写 | `ftboss_base_hp`、`ftboss_base_atk` | 基准值存实体持久数据，反复缩放不叠加；改最大生命时按当前血量比例缩放，不会突然回满 | `boss/ai/BossScaling.java:28`、`:63` |
| 自定义技能伤害 | `BossScaling.dmg(boss, base)` | base × 当前伤害倍率，倍率存 `ftboss_dmg_mul` | `boss/ai/BossScaling.java:77` |

## 五、Boss 强化包

| 项 | 数值或内容 | 出处 |
|---|---|---|
| 覆盖 Boss | `SUGAR_QUEEN`、`MEAT_MOUNTAIN`、`RAT_BOSS`、`WINE_BARREL`、`COMPOST_BOSS` | `boss/ai/BossEmpower.java:39` |
| 鼠群循环期跳过 | 鼠群主从循环激活时 `RatSwarmEncounter.skipEmpower` 直接跳过旧强化包（主王/副鼠群都不吃强化） | `boss/ai/BossEmpower.java:57`、`boss/ai/RatSwarmEncounter.java` |
| 不参与 | 沙耶（`TEA_TREE`）、故障机器人（`DEFECT_CORE`）等未列入白名单的实体 | `boss/ai/BossEmpower.java:35` |
| 移速加成 | 乘法 +0.25（即 ×1.25） | `boss/ai/BossEmpower.java:27` |
| 攻击加成 | 乘法 +0.30（即 ×1.30） | `boss/ai/BossEmpower.java:28` |
| 护甲加成 | 加算 +4.0 | `boss/ai/BossEmpower.java:29` |
| 击退抗性 | 加算 +0.0（2026-09-19 按用户要求取消，见第十八节） | `boss/ai/BossEmpower.java:30` |
| 调用周期 | 每 20t 一次，修饰符按固定 UUID 去重只加一次 | `boss/BossHandler.java:47`、`boss/ai/BossEmpower.java:126` |
| 常驻增益刷新 | 每 400t 续一次，持续 520t | `boss/ai/BossEmpower.java:31` |
| 常驻增益内容 | 速度 I、抗性提升 I、抗火 I、力量 II | `boss/ai/BossEmpower.java:116` |
| 施法等级 | 强化 Boss 作为施法者时法术等级 +1 | `boss/ai/BossEmpower.java:50` |
| 屠夫狂暴加成（当前不触发） | 屠夫三阶段循环接管后不再调用通用狂暴，`BossRage.tierOf` 恒为 0，因此该 +50% 移速 / +200% 伤害加成不生效；三阶段数值见屠夫页 | `boss/ai/BossEmpower.java:65`、`boss/ai/ButcherPhases.java` |
| 屠夫扩展索敌 | 无目标时锁定 32 格内最近的非本模组实体 | `boss/ai/BossEmpower.java:79` |
| 酒桶王分身 | 血量 < 50% 后每 600t 判定一次，Boss 24 格内最多 2 个，分身 1 血且 NoAI | `boss/ai/BossEmpower.java:92` |

## 六、狂暴阶段

| 阶段 | 触发 | 增益 | 出处 |
|---|---|---|---|
| 狂暴 I | 血量 ≤ 50% | 迅捷 I + 抗性提升 I | `boss/ai/BossRage.java:40` |
| 狂暴 II | 血量 ≤ 25% | 狂暴 I 全部 + 力量 I + 生命恢复 I | `boss/ai/BossRage.java:90` |
| 故障机器人特例 | 同上阈值 | 只给迅捷（狂暴 II 时再 +1 级）与力量（狂暴 II 时 II 级），不给抗性与再生 | `boss/ai/BossRage.java:84` |
| 当前调用者 | 糖霜女皇、沙耶、酒桶王、故障机器人、鼠群首领（仅鼠群主从循环之外）、benger、潮宴鱼王；屠夫（三阶段循环）、答辩超人、苹果爵士不调用 | — | 各实体 `tick()` |
| 增益刷新 | 每 20t | 持续 80t | `boss/ai/BossRage.java:49` |
| 粒子 | 每 5t | 狂暴 II 每次 6 个，狂暴 I 每次 3 个 | `boss/ai/BossRage.java:99` |
| 进入演出 | 一次性 | 劫掠兽咆哮（音量 1.7，狂暴 I 音调 0.9、狂暴 II 0.7）+ 凋灵生成音，红尘粒子 35 或 60 个、怒气环半径 2.6 或 3.6 格；阶段切换不发聊天播报 | `boss/ai/BossRage.java:67` |
| 记账方式 | NBT `ftboss_rage_tier` | 持久化，区块重载不重复触发；由 `reset` 清零 | `boss/ai/BossRage.java:29` |

## 七、禁疗

| 项 | 数值或内容 | 出处 |
|---|---|---|
| 时长 | `BAN_TICKS` = 600t（30 秒） | `boss/ai/HealBan.java:45` |
| 标记效果 | `ftboss:heal_ban`，取不到时退化为内部计时表 | `boss/ai/HealBan.java:57` |
| 拦截 1：回血事件 | `LivingHealEvent` 把回血量设为 0 | `boss/ai/HealBan.java:107` |
| 拦截 2：血量回滚 | 每 tick 末按快照压回上涨的血量，覆盖不死图腾与焰形剑的 `setHealth` 类回复 | `boss/ai/HealBan.java:172` |
| 拦截 3：拒绝回复类效果 | 拒绝 `minecraft:regeneration`、`absorption`、`saturation`、`instant_health` | `boss/ai/HealBan.java:208` |
| 触发：麻雀 | `SPARROW_NOHEAL_CHANCE` = 0.10，麻雀同步玩家增益成功时按此概率禁疗 | `entity/EasterBossEntity.java:3085`、`:2337` |
| 触发：雪黎飞刀 | 命中即禁疗，署名「雪黎的飞刀」 | `entity/XueliKnifeEntity.java:148` |
| 来源限定 | 只有麻雀走这条判定，其它彩蛋 Boss 拿到玩家增益不附带禁疗 | `entity/EasterBossEntity.java:3085` |
| 提示节流 | 每名玩家 40t（2 秒）最多一条日志与动作栏提示 | `boss/ai/HealBan.java:248` |
| 附带禁用 | 禁疗期间不死图腾的复活同样被血量回滚抹掉 | `boss/ai/HealBan.java:164` |

## 八、削甲与附加减益

| 机制 | 数值 | 出处 |
|---|---|---|
| 护甲侵蚀（Boss 打玩家） | 每层 -5.0 护甲，最多 5 层（合计 -25），每层 200t（10 秒），再命中刷新时长 | `boss/ArmorShred.java:25` |
| 护甲侵蚀施加者 | 庄园彩蛋 Boss 功底以黑色切割者命中时 | `entity/EasterBossEntity.java:1482` |
| 护甲侵蚀清理 | 每 tick 过期清理；玩家死亡或整场结束立即清除 | `boss/ArmorShred.java:75` |
| 护甲侵蚀效果（玩家武器侧） | `ftboss:armor_shred` 每层 -10.0 护甲、最多 2 层、持续 160t | `effect/ArmorShredEffect.java:21` |
| 流血 | `ftboss:bleed` 每秒 3.0 点 × min(10, 层数)，层数 = 等级 + 1，等级上限 4 | `effect/BleedEffect.java:23` |
| 蓝火 | `ftboss:blue_fire` 每秒 10.0 点虚空伤害 | `effect/BlueFireEffect.java:18` |
| 感染反噬 | 带 `ftboss:infection` 的玩家攻击任何非自身目标时受（等级 + 1）点魔法伤害 | `boss/BossHandler.java:74` |
| 鼠群撕咬 | 命中玩家即挂随机减益：等级 0~1、时长 140 + 0~119 tick | `boss/BossHandler.java:329` |
| 鼠群减益池 | 8 种原版效果 + armamentarium 的 15 个候选 id（缺失自动跳过），不含失明与黑暗 | `boss/BossHandler.java:301` |
| 糖霜女皇命中 | 饥饿 II 80t + 缓慢 I 60t | `boss/BossHandler.java:347` |

## 九、召唤上限与清扫

| 规则 | 数值或内容 | 出处 |
|---|---|---|
| 同种召唤物上限 | `MAX_PER_TYPE` = 10 只 | `boss/ai/SummonLimit.java:38` |
| 统计半径 | `RADIUS` = 64.0 格 | `boss/ai/SummonLimit.java:40` |
| 超限处理 | 不再召唤，改为给场上同类全体上速度 II、力量 I、抗性 I，持续 `BUFF_TICKS` = 400t（20 秒） | `boss/ai/SummonLimit.java:42` |
| 随主消散默认半径 | `RANGE` = 80.0 格 | `boss/ai/SummonSweep.java:29` |
| 按类型清扫 | `clear`：类型白名单 + 认主人 | `boss/ai/SummonSweep.java:48` |
| 按标记清扫 | `clearMarked`：必须带主人标记且主人一致 | `boss/ai/SummonSweep.java:77` |
| 按归属清扫 | `clearOwned`：按召唤者 UUID 清，避免共用类型互相误杀 | `boss/ai/SummonSweep.java:97` |
| 怜悯离场清扫 | 充能球 64 格、鼠群小老鼠 64 格，小酒桶、姜饼小兵、茶芽、蛆 48 格 | `boss/ai/BossMercy.java:291` |
| 召唤物继承增益 | `PERIOD` = 60t 一次，最多 `MAX_MINIONS` = 8 只，每只最多 `MAX_EFFECTS` = 3 个效果，`DURATION` = 140t，等级为 Boss 等级 -1 且最高 I 级 | `boss/ai/MinionBlessing.java:33`、`:98` |
| 小老鼠例外 | 不继承生命恢复、伤害吸收、生命提升、饱和 | `boss/ai/MinionBlessing.java:96` |
| 召唤物 BGM 豁免 | 带 `ftboss_no_bgm` 标记的实体不参与 BGM 调度 | `sound/BossMusic.java:281` |

## 十、连死怜悯

### 极限模式死亡护栏

| 项 | 数值或内容 | 出处 |
|---|---|---|
| 生效条件 | 服务器 `isHardcore()` 为真、服务端玩家非旁观且 `LivingDeathEvent` 未取消 | `boss/HardcoreBossSafety.java` |
| 触发时机 | 真正死亡事件开始后、普通死亡记账前；原版不死图腾成功时不会进入该路径 | `boss/BossHandler.java`、`boss/HardcoreBossSafety.java` |
| 庄园归因 | 伤害源实体/直接实体、庄园包装伤害主人、投射物或召唤物主人；环境伤害另按本场参战名单兜底 | `boss/HardcoreBossSafety.java`、`boss/ManorFight.java` |
| 致命救援 | 只有 `ManorFight.abortForHardcore` 确认中止庄园战后，才把生命设为 1 并取消本次死亡事件 | `boss/HardcoreBossSafety.java` |
| 短暂无敌 | `MANOR_GRACE_TICKS` = 60t（3 秒）；抗性提升 V + 延长原版受伤冷却，并由服务端取消保护期内全部 `LivingHurtEvent` | `boss/HardcoreBossSafety.java` |
| 旁路兜底 | 保护期内若虚空、命令或外部模组直接再次触发死亡事件，恢复至少 1 点生命并继续取消死亡 | `boss/HardcoreBossSafety.java` |
| 生命周期 | 每个服务端 tick 清理过期保护，`ServerStoppedEvent` 清空静态表，不会留下永久无敌 | `boss/BossHandler.java`、`boss/HardcoreBossSafety.java` |
| 庄园中止结果 | 本体、所属召唤物、血条、入场演出和 BGM 无奖励退场；无掉落、无经验、无击败或通关记录 | `boss/ManorFight.java`、`boss/HiddenBossIntro.java` |
| 非庄园 Boss | 仍在死亡前无奖励中止归属 Boss，但不取消玩家死亡，也不给庄园无敌 | `boss/HardcoreBossSafety.java` |

| 项 | 数值或内容 | 出处 |
|---|---|---|
| 触发次数 | `LIMIT` = 5 次 | `boss/ai/BossMercy.java:44` |
| 记账粒度 | 单只 Boss 的实例 UUID | `boss/ai/BossMercy.java:235` |
| 过期时间 | `STREAK_TIMEOUT` = 12000t（10 分钟）未再死亡即归零 | `boss/ai/BossMercyData.java:34` |
| 不参与者 | 庄园彩蛋十一人（`EasterBossEntity`）不计数也不离场 | `boss/ai/BossMercy.java:111` |
| 离场效果 | 直接 `discard()`，不走死亡流程，无掉落、无经验，并清掉自己的召唤物 | `boss/ai/BossMercy.java:281`、`:301` |
| 归因 1 | 直接击杀者（投射物取主人） | `boss/ai/BossMercy.java:179` |
| 归因 2 | 击杀者为召唤物，取 64 格内最近 Boss | `boss/ai/BossMercy.java:184`、`:190` |
| 归因 3 | 200t（10 秒）内打中过玩家、仍活着且在 48 格内的 Boss | `boss/ai/BossMercy.java:196` |
| 归因 4 | 32 格内最近的 Boss | `boss/ai/BossMercy.java:208` |
| 胜利清零 | 真死时清零 96 格内所有玩家的该实例计数 | `boss/ai/BossMercy.java:150` |
| 威胁记忆 | 玩家被打中即记录，记忆 200t；表上限 128 名玩家，超限清过期项 | `boss/ai/BossMercy.java:49`、`:56` |

## 十一、仇恨表与索敌规则

| 项 | 数值或内容 | 出处 |
|---|---|---|
| 仇恨距离上限 | `MAX_DIST` = 64.0 格 | `boss/ai/BossAggro.java:22` |
| 仇恨值来源 | 伤害量，且不超过目标当前血量 | `boss/ai/BossAggro.java:30` |
| 食物仇恨 | `FOOD_THREAT` = 5.0（吃食物时给正在交战的 Boss 加仇） | `boss/ai/BossThreatTable.java:11` |
| 药水仇恨 | `POTION_THREAT` = 8.0（喝有效果药水时加仇） | `boss/ai/BossThreatTable.java:12` |
| 半衰期 | `HALF_LIFE_TICKS` = 600t（30 秒） | `boss/ai/BossThreatTable.java:9` |
| 最小保留仇恨 | `MIN_THREAT` = 0.1，低于即剔除 | `boss/ai/BossThreatTable.java:10` |
| 无效目标 | 创造、旁观、已死、已移除、跨维度、本模组阵营实体 | `boss/ai/BossTargets.java:31` |
| 兜底清扫 | 每 10t 把所有登记 Boss 的创造或旁观目标清空 | `boss/BossHandler.java:54` |
| 反击规则 | 本模组 Boss 被任何生物攻击都记仇并回头打它 | `boss/BossHandler.java:86` |
| 麻雀不参与 | 麻雀变体不算仇恨方，麻雀自身也清空目标 | `boss/ai/BossAggro.java:91`、`entity/EasterBossEntity.java:2359` |
| 阵营判定 | 按 `ftboss` 命名空间判定「自己人」 | `boss/ai/BossAggro.java:123` |
| 跨维度 | 目标必须与 Boss 同维度，否则不算有效目标 | `boss/ai/BossTargets.java:73`、`boss/ai/BossAggro.java:96` |
| 并列与切目标 | 仇恨并列时保留当前目标，只有严格超过才换人 | `boss/ai/BossThreatTable.java:26` |

## 十二、卡位脱困与瞬移

| 项 | 数值或内容 | 出处 |
|---|---|---|
| 检查周期 | `CHECK` = 80t | `boss/ai/BossUnstuck.java:27` |
| 判卡阈值 | 一个周期内水平位移 < 2.0 格 | `boss/ai/BossUnstuck.java:29` |
| 贴身豁免 | 离目标 ≤ 4.0 格不判卡 | `boss/ai/BossUnstuck.java:31` |
| 咏唱豁免 | 铁魔法 `isCasting` 为真时跳过，反射探测、缺 mod 安全 | `boss/ai/BossUnstuck.java:103` |
| 落点 | 目标周围半径 2.0 至 5.5 格，最多试 10 次 | `boss/ai/BossUnstuck.java:74` |
| 特效 | 反向传送门 24 个 + 传送门 16 个 + 末影人传送音 | `boss/ai/BossUnstuck.java:82` |
| 兜底 | 10 次全失败则直接传送到目标脚边 | `boss/ai/BossUnstuck.java:92` |
| 适用范围 | 仅会移动的 Boss，沙耶扎根不调用 | `boss/ai/BossUnstuck.java:21` |
| 近战前摇 | `BossMeleeWindup`：挥击音 + 命中点暴击粒子 6 个，需视线与距离校验 | `boss/ai/BossMeleeWindup.java:46` |

## 十三、麻雀支援

| 项 | 数值或内容 | 出处 |
|---|---|---|
| 判定间隔 | `CHECK_INTERVAL` = 200t（10 秒） | `boss/SparrowSupport.java:32` |
| 出现概率 | `CHANCE` = 0.5（每次判定） | `boss/SparrowSupport.java:34` |
| 交战判据 | 玩家 48 格内有真・彩蛋 Boss，且玩家在庄园战场内或该 Boss 正锁定该玩家 | `boss/SparrowSupport.java:36` |
| 去重 | 直接数世界里活着的麻雀，数量大于 0 就不刷（跨维度全局只允许 1 只）；常量 `DUPLICATE_RANGE` = 96.0 当前未被调用 | `boss/SparrowSupport.java:69`、`:38` |
| 生成位置 | 被支援 Boss 周围 6.0 格、上方 6.0 格 | `boss/SparrowSupport.java:153` |
| 血量 | `SPARROW_HEALTH` = 500.0 | `boss/SparrowSupport.java:53` |
| 本体 / 支援身份生命 | `BASE_HEALTH` = 1500；进入支援身份后为 `SUPPORT_HEALTH` = 500（普通与终结一致，普通与终结共用同一生命值；旧实体按当前生命比例迁移） | `util/ManorSupportRules.java:7-8`、`boss/ai/ManorHealthBalance.java:28` |
| 多人额度与概率 | 每位有效参战玩家 1 次额度（`quota` = max(1, 人数)）；独立 Boss 判定概率 = 1 − 0.5^人数；庄园未选中场次额外机会 = 1 − 0.75^(人数−1) | `util/SparrowMultiplayerRules.java` |
| 随机借招 | 每 `INTERVAL` = 200t 等概率抽 11 位庄园 Boss 之一，再抽其已启用招牌法术（共 32 种，不含麻雀自身 3 种）；沿用既有施法管线，不切换变体 | `util/ManorSupportRules.java:9`、`integ/spells/SparrowBorrowedSpells.java` |
| 沉默 / 驱散 | 有效支援时每 `INTERVAL_TICKS` = 200t，对支援 Boss 48 格内每位存活生存/冒险玩家随机沉默一格物品 20t（阻止使用/蓄力/交互/近战/挖掘/本模组武器技能）并移除一个非 ambient 正面效果；创造/旁观/死亡/假人排除 | `util/SparrowDisruptionRules.java:5-7`、`boss/ai/SparrowDisruption.java` |
| 千羽散华 | 支援麻雀独立施法入口：首次约 2~3 秒、之后两次起手至少 8 秒；命中存活敌人附 100t 禁疗，刷新短禁疗但不缩短更长的禁疗 | `integ/spells/MaqueFeatherCombat.java`、`util/ManorSpellRules.java:33` |
| 治疗 | `SPARROW_HEAL_TICKS` = 60t（3 秒）治疗被支援 Boss `SPARROW_HEAL_PERCENT` = 0.5% 最大生命，并给 `SPARROW_BUFFS` = 3 个随机增益（15 秒） | `entity/EasterBossEntity.java:2745`、`:2763`、`:2807` |
| 净化 | `SPARROW_CLEANSE_TICKS` = 100t（5 秒）清掉另一位 Boss 的减益 | `entity/EasterBossEntity.java:2401`、`:2330` |
| 共享增益 | `SPARROW_SHARE_TICKS` = 20t 与被支援 Boss 双向同步正面效果（对方层数更高或自己剩余不足 3 秒才补，幂等） | `entity/EasterBossEntity.java:2403`、`:2419` |
| 伤害分摊 | `SPARROW_DAMAGE_SHARE` = 0.50，替被支援 Boss 承伤 50% | `entity/EasterBossEntity.java:2465`、`:2335` |
| 禁疗 | 同步玩家增益成功时 `SPARROW_NOHEAL_CHANCE` = 0.10 概率禁疗该玩家 30 秒 | `entity/EasterBossEntity.java:3085`、`:2337` |
| 存续时间 | `SPARROW_LIFE_TICKS` = 1800t（90 秒）后飞走 | `entity/EasterBossEntity.java:2342` |
| 位移 | 离被支援者超过 `SPARROW_TELEPORT_RANGE` = 10 格时自我传送，冷却 `SPARROW_TELEPORT_CD` = 60t；入场时 96 格内玩家收到系统消息 | `entity/EasterBossEntity.java:2347`、`:2348`、`boss/SparrowSupport.java:174` |

## 十四、血条显示范围

| Boss | 显示半径 | 出处 |
|---|---|---|
| 糖霜女皇 | 48.0 格 | `entity/SugarQueenEntity.java:97` |
| 屠夫 | 48.0 格 | `entity/MeatMountainEntity.java:82` |
| 沙耶（茶树） | 48.0 格 | `entity/TeaTreeBossEntity.java:138` |
| 酒桶王 | 48.0 格 | `entity/WineBarrelBossEntity.java:65` |
| 鼠群 | 48.0 格 | `entity/RatBossEntity.java:79` |
| 炫彩答辩超人 | 48.0 格 | `entity/CompostBossEntity.java:621` |
| 苹果爵士 | 48.0 格 | `entity/AppleBossEntity.java:302` |
| 故障机器人 | 48.0 格 | `entity/DefectCoreEntity.java:576` |
| 潮宴鱼王 | 48 格（2304 = 48 的平方） | `entity/TideFishBossEntity.java:134` |
| 边牧犬王 benger | 原版实体追踪范围，进出视野时挂载或摘除 | `entity/BengerEntity.java:903` |
| 血条登记与清扫 | 仅服务端登记，客户端不持有登记表；每 40t 一次：主人已移除或已死则隐藏、发 REMOVE 并注销 | `boss/BossBars.java:77`、`boss/BossHandler.java:39` |
| 玩家死亡与重生 | 死亡、重生、下线时从所有血条摘除 | `boss/BossBars.java:55` |

## 十五、BGM 规则

| 项 | 数值或规则 | 出处 |
|---|---|---|
| 播放半径 | `HEAR_RANGE` = 96.0 格 | `sound/BossMusic.java:43` |
| 停止广播半径 | `STOP_RANGE` = 128.0 格 | `sound/BossMusic.java:45` |
| 空场宽限 | `GRACE_TICKS` = 100t（5 秒），期间有 Boss 回来则取消停止 | `sound/BossMusic.java:62` |
| 曲目归属 | 第一个开战且存活的 Boss 决定本场曲目组 | `sound/BossMusic.java:559` |
| 换曲条件 | 新 Boss 曲目组不同且当前组已无存活成员；庄园战不许被顶掉 | `sound/BossMusic.java:584` |
| 召唤物豁免 | `TAG_NO_BGM` = `ftboss_no_bgm`，带此标记的实体不请求也不释放 BGM | `sound/BossMusic.java:278`、`:548` |
| 专属单曲时长 | 潮宴鱼王 1980t（二阶段 1927t）、沙耶 2633t、糖霜女皇 2308t、屠夫 4568t、酒桶王 2478t、鼠群 2236t、故障机器人 3696t、答辩超人 1782t、苹果爵士 2000t、benger 3708t | `sound/BossMusic.java:129`、`:108` |
| 三幕剧兜底 | 未登记 Boss 按全场最低血量比例推进：>0.66 序幕、>0.33 假面圆舞、其余断线狂宴，每幕最短停留 200t | `sound/BossMusic.java:74`、`:696` |
| 曲尾重播 | 到曲尾即重播，先停后播分两个 tick；庄园、答辩超人、苹果爵士除外 | `sound/BossMusic.java:636`、`:645` |
| 补发机制 | 每 600t 补发一次当前曲目播放包，防玩家跑远丢包；庄园、答辩超人、苹果爵士三组不走这条 | `sound/BossMusic.java:659` |
| 开演与换曲清场 | 播放前停掉附近全部 23 条 BGM 事件，防两首叠加 | `sound/BossMusic.java:755` |
| 落幕钟声 | 最后一位真死时敲一次；庄园彩蛋 Boss 阵亡不算真死，不敲 | `sound/BossMusic.java:344` |

## 十六、通用召唤与掉落规则

| 规则 | 内容 | 出处 |
|---|---|---|
| 主世界限定 | 所有玩家可见召唤入口只在主世界允许，其它维度中止召唤且不消耗召唤物 | `boss/SummonGate.java:51` |
| 石像掉落 | 统一击杀钩子按类型发放，共 9 种：潮宴鱼王、糖霜女皇、屠夫、沙耶、酒桶王、鼠群、苹果爵士、答辩超人、故障机器人；全部只发石像不发武器 | `boss/ai/BossMercy.java:329` |
| 武器掉落 | 潮宴鱼王得潮宴三叉戟、糖霜女皇得糖果拐杖、苹果爵士得苹果梗、屠夫得大肉腿、故障机器人得充能之爪、benger 得打狗棒 | `boss/ai/BossMercy.java:366` |
| benger 石像 | benger 不在石像表内，击杀只掉打狗棒 | `boss/ai/BossMercy.java:366`、`:389` |
| 无掉落情形 | 连死怜悯离场走 `discard()`，不掉落也不给经验 | `boss/ai/BossMercy.java:281` |
| 石像交易 | 交易表从 `config/ftboss_trades.json` 载入，右键石像的交易入口不存在，当前无触发点；手持对应召唤物左键石像可在上色与白膜间切换 | `trade/TradeHandler.java:47` |
| 召唤物随主消散 | `SummonSweep.RANGE` = 80.0 格；三种口径：`clear`（类型白名单 + 认主人）、`clearMarked`（带主人标记且主人一致）、`clearOwned`（按召唤者 UUID 清，避免共用类型误杀） | `boss/ai/SummonSweep.java:29`、`:48`、`:77`、`:97` |
| BGM 豁免标记 | 带 `ftboss_no_bgm` 的实体不请求也不释放 BGM（柠檬猫喊来的助战等） | `sound/BossMusic.java:278` |
| 柠檬猫助战无奖励 | 柠檬猫直接召来的食物系 Boss 带持久化 `ftboss:lemon_summon_no_rewards` 标记；真死不产生物品或经验，包括战利品表、石像、武器、遗物、法术卷轴与手写奖励。普通同类 Boss 不受影响 | `boss/ai/LemonSummonRewards.java`、`entity/EasterBossEntity.java` |

## 十七、其它公共规则

| 规则 | 内容 | 出处 |
|---|---|---|
| Boss 负面效果免疫（事件层已关闭） | `IMMUNE_TO_DEBUFFS` = false：Forge 事件层不再一律 `DENY`；但糖霜女皇、屠夫、酒桶王、鼠群首领、故障机器人、苹果爵士仍每 20t 自清一次非增益效果（实际只能短暂挂上）；潮宴鱼王仍由 `canBeAffected` 只接受增益；庄园 Boss 另有自己的减益清除与流血类拒绝规则（见庄园章） | `boss/BossHandler.java:409`、各实体 `tick()`、`entity/TideFishBossEntity.java:124` |
| 友军伤害免疫 | 本模组命名空间实体互殴一律取消，玩家打 Boss 不受影响 | `boss/BossHandler.java:252` |
| 苹果阵营免疫 | 苹果系不吃闪电与爆炸伤害 | `boss/BossHandler.java:783` |
| 鼠群小怪承伤 | 旧一般小鼠/柠檬猫小鼠：伤害 ×0.6 转入大老鼠公共血池；2026-10-12 鼠群主从循环的副鼠群由 `RatSwarmEncounter.onDeputyDamage` 统一按 `SWARM_DAMAGE_TAKEN` = 0.6 扣副鼠群共享血池（400 基础），10 tick 内同额或更低伤害不重复扣、更高伤害只扣差额 | `boss/BossHandler.java:257`、`boss/ai/RatSwarmEncounter.java`、`boss/ai/RatSwarmRules.java:10` |
| 隐藏 Boss 信标增益 | 仅当 64 格内有非迷你彩蛋 Boss：速度、急迫、抗性、跳跃、力量、生命恢复全 II 级，持续 240t，剩余不足 60t 或等级不足时补挂，每 10t 检查一次 | `boss/BossHandler.java:596` |
| 无主屠夫清理 | 每 200t 清理未登记且非分身的屠夫实体 | `boss/BossHandler.java:62` |
| 死亡停止进食 | 玩家死亡瞬间停掉可食用物品的使用，规避外部模组崩溃 | `boss/BossHandler.java:640` |
| 公共驱动节拍 | 每 10t 仇恨与目标清扫、每 20t 缩放与强化、每 40t 血条与 BGM 兜底、每 200t 无主实体清理 | `boss/BossHandler.java:33` |
| 主世界限定调用点 | 各召唤物右键、Boss 刷怪蛋与 `/ftboss challenge` 隐藏 Boss 都先过 `SummonGate.allow`，非主世界中止且不消耗召唤物 | `boss/SummonGate.java:51`、`boss/HiddenBossChallenge.java:269` |
| 石像外观切换用召唤物 | 潮宴、糖霜女皇晶体、屠夫之心、茶种、酒符、鼠饵、故障核心、堆肥召唤物、禁忌苹果 | `boss/BossHandler.java:199` |
| 其它召唤入口 | benger 哨子、Boss 刷怪蛋各自先过 `SummonGate.allow` | `item/BengerWhistleItem.java:39`、`item/OverworldBossSpawnEggItem.java:24` |

## 十八、击退规则（2026-09-19 按用户要求）

用户口径：「允许 boss 受到击退，减少 boss 对玩家的击退」。

| 项 | 数值 | 说明与出处 |
|---|---|---|
| Boss 本体抗击退 | `BOSS_RESISTANCE` = 0.85 | 11 只 Boss 本体统一 0.85（只吃 15% 击退；**2026-09-24 由 0.5 再降一档**），不再有 1.0 的完全免疫；`util/BossKnockback.java`、各 `entity/*.java` 的属性注册行 |
| 功底特例 | 抬到 0.9 | 「功底」穿亚当甲套装时会把基础值抬到 0.9，属它自己的护甲机制，本次未动；`entity/EasterBossEntity.java:2739` |
| 答辩超人 | 0.4 | 本来就比 0.85 更吃击退，保持不动；`entity/CompostBossEntity.java:150` |
| 强化包抗击退 | `KB_ADD` = 0.0 | 原为 +0.30，会叠到 0.8 又把击退挡住，已取消；`boss/ai/BossEmpower.java:31` |
| Boss 打玩家击退倍率 | `TO_PLAYER_MULT` = 0.10 | 只削**玩家**目标的**水平**分量（**2026-09-24 由 0.25 再降一档**）；56 处推/击退统一走 `BossKnockback.push` / `BossKnockback.knockback`；`util/BossKnockback.java` |
| 竖直分量 | `SCALE_VERTICAL` = true、`VERTICAL_MULT` = 0.25 | 上挑与击飞高度一起削（2026-09-24 由 0.5 再降一档）；关掉 `SCALE_VERTICAL` 可恢复"竖直不削" |
| 技能表里的击退数值 | 字面常量 | 本文各 Boss 技能表写的是代码字面量；实际打到玩家身上时水平分量再乘 `TO_PLAYER_MULT`（0.10） |
| 非玩家目标 | 原样 | 自家小怪、投射物、Boss 本体不受倍率影响（`BossKnockback` 只在目标是 `Player` 时缩放） |
| 未覆盖 | 原版近战 0.4 基础击退 | `Mob#doHurtTarget` / `Player#attack` 那条不经过本模组；若要一起削需再加 `LivingKnockBackEvent` 钩子 |
| 小怪抗击退 | 各自原值 | 小姜饼人 0.4、茶小兵 0.3、小酒桶 0.5、巨型果蛆 0.8、充能球 1.0 等，均未改动 |

## 十九、Boss 咏唱时长（2026-09-20 按用户要求）

用户口径：「boss施法时间变长1/3倍」。

| 项 | 数值 | 说明与出处 |
|---|---|---|
| Boss 咏唱倍率 | `BOSS_CAST_MULT` = 2.0 / 3.0 | Boss 实际咏唱 = 法术原始时长 × 2/3，向上取整、下限 1 tick；旧值是 ×1/2，所以现在比原来长 1/3；`integ/spells/BossSpellTuning.java:24,27` |
| 常规 Boss 招牌法术 | 32 tick → 22 tick（特例 20 tick → 14 tick） | 特例 = `FROST_LANCE` / `DEFECT_PULSE`（`castTicks` 20）；`integ/spells/BossSignatureSpell.java:49-52`、`util/BossSpellRules.java:138` |
| 庄园 Boss 招牌法术 | 20 / 24 / 28 / 32 / 36 → 14 / 16 / 19 / 22 / 24 tick | 33 个庄园法术逐条换算，再 clamp 到 1 ~ 200 tick；`integ/spells/ManorSignatureSpell.java:45-48`、`integ/spells/ManorSpellCombat.java:79`、`util/ManorSpellRules.java:14-47` |
| 常规 Boss 的铁魔法 LONG 法术 | 同倍率 | `fasterLegacyCast` 重设咏唱时长时也乘 2/3（只处理 `CastType.LONG` 且非招牌法术）；`integ/spells/BossSpellTuning.java:63-70` |
| 玩家自己施法 | 不受影响 | 倍率只在施法者是 Boss 时生效，玩家用卷轴 / 法术书仍按法术自身时长 |
| 未改动的部分 | 冷却 / 施法间隔 / 伤害 / 蓝耗 | 本次只动咏唱时长；招牌法术的 60 tick 取消闸门、庄园的 200 tick 上限都保持原样 |
