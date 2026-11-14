# 非隐藏 Boss 机制报告（10 只常规 Boss）

本报告只覆盖**常规 Boss**：糖霜女皇 / 屠夫 / 沙耶 / 酒桶王 / 鼠群 / 故障机器人 / 炫彩答辩超人 / 苹果爵士 / 边牧犬王 / 潮宴鱼王。
**不含**甜苹果庄园的隐藏 Boss（彩蛋十人 · 麻雀 · 终章），那一套有独立的两份文档。

数字一律取自当前源码里的字面常量，并在括号里给出**常量名 + 文件:行**。
「实吃」表示该数字已经是玩家实际掉的血（已含全局系数）；没写「实吃」的是代码里的原始输入值。

## 0. 看数字前先读这几条口径

- 原“单次原始伤害超过 500 点整下无效”规则已取消；/kill、掉出世界等无视无敌的伤害仍按其它规则处理。
- 上述 500 点全局阈值已取消；它不再作用于任何 Boss。
- 传奇怪物式的「单次上限 + 连击递减 + 反逃课距离」**整套关闭**（`LIMIT_ENABLED = false`，`BossDamageGate.java:46`）。文档里能看到的女皇 58 / 屠夫 58 / 沙耶 56 / 酒桶王 58 / 鼠群 46 / 未登记 48 这些上限**当前都不生效**。
- **流血与真实伤害完全免疫**：命名空间集合已清空（`IMMUNE_NAMESPACES = {}`，`BossDamageGate.java:111`），只按伤害类型路径或 `getMsgId()` 的关键字表拦（`IMMUNE_IDS`，`BossDamageGate.java:120`）：hemorrhage / blood_loss / bloodloss / bleed / bleeding / doom / impending_doom / executioner / assassination / desecration / true_damage。
- **常规 Boss 全部拒绝负面效果**：`MobEffectEvent.Applicable` 对非 `isBeneficial()` 的效果返回 `Result.DENY`（`BossHandler.java:316`）。所以中毒、缓慢、虚弱这类减益在它们身上挂不住。
- 伤害数字不等于最终掉血：还要过护甲、抗性、附魔与上述规则。

## 1. 公共伤害与限制规则

| 规则 | 当前值 | 出处 |
|---|---|---|
| 单次伤害无效门槛 | 已取消（不再按 500 点阈值拦截） | — |
| 限伤总开关 | false（关闭） | `LIMIT_ENABLED`，BossDamageGate.java:46 |
| 关闭时的单次上限表 | 女皇/屠夫/酒桶王 58.0、沙耶 56.0、鼠群 46.0、未登记 48.0 | `DAMAGE_CAP` 与 `DEFAULT_CAP`，BossDamageGate.java:66,73 |
| 关闭时的反逃课距离 | 女皇/屠夫/酒桶王/鼠群 32.0、沙耶 40.0、故障机器人 36.0、兜底 32.0 | `ANTI_CHEESE` 与 `DEFAULT_ANTI_CHEESE`，BossDamageGate.java:77,84 |
| 连击递减参数 | `ADAPT_TICKS 24` / `FACTOR_STEP 6` / `FACTOR_MIN 20` / `FACTOR_MAX 100` / `COMBO_FLOOR 0.7` / `HURT_CD 30` | BossDamageGate.java:87-97 |
| 完全免疫命名空间 | 空集（不再按命名空间免伤） | `IMMUNE_NAMESPACES`，BossDamageGate.java:111 |

故障机器人的充能球有一条独立规则：**任何伤害来源都能打爆它**（玩家近战 / 弓箭 / 法术 / 范围伤害 / 环境伤害都算），
命中即消散、无视它自己的 20 血，并触发 40 tick 的补球暂停。

> 注意：旧文档里「只有玩家能拆球、环境伤害一律免疫」的说法**与实现相反** ——
> 源码注释写明那段判定是个空块，已在 v112 作为死代码删除（`ChargedOrbEntity.java:310-325`）。
> 环境伤害算数是有意的：球会高速飞进地形，被墙里窒息或岩浆"白掉"也算玩家的收益。

## 2. 人数缩放（每 20 tick 重算）

| 项 | 公式 | 出处 |
|---|---|---|
| 统计半径 | 48.0 格内**存活、非创造、非旁观**玩家 | `RADIUS`，BossScaling.java:22 |
| 生命 | ×`1.25^(n-1)` | `HP_PER_PLAYER`，BossScaling.java:24 |
| 攻击 | ×`1 + 0.15×(n-1)` | `DMG_PER_PLAYER`，BossScaling.java:26 |

改最大生命时**按当前血量比例缩放**，基准值存在实体持久数据里；自定义伤害走 `BossScaling.dmg`（BossScaling.java:44-71）。

## 3. Boss 强化包（每 20 tick）

只强化**糖霜女皇 / 屠夫 / 鼠群 / 酒桶王 / 炫彩答辩超人**；**沙耶与故障机器人不吃这一套**（`isEmpowered`，BossEmpower.java:35）。

| 项 | 值 | 出处 |
|---|---|---|
| 移速 | +0.25（MULTIPLY_TOTAL） | `SPEED_MUL`，BossEmpower.java:27 |
| 攻击 | +0.30（MULTIPLY_TOTAL） | `ATTACK_MUL`，BossEmpower.java:28 |
| 护甲 | +4.0（ADDITION） | `ARMOR_ADD`，BossEmpower.java:29 |
| 击退抗性 | +0.30（ADDITION） | `KB_ADD`，BossEmpower.java:30 |
| 常驻药水 | 速度 I + 抗性提升 I + 抗火 I + 力量 II | BossEmpower.java:115-120 |
| 药水时长 / 刷新 | 持续 520 tick，每 400 tick 续一次 | `EFFECT_TICKS`，BossEmpower.java:31,115 |
| 施法等级 | 该 Boss 的铁魔法等级 **+1** | `spellBonusFor`，BossEmpower.java:50 |

屠夫额外：狂暴后移速再 +50%、攻击再 +200%，每 40 tick 给速度 II 100 tick，索敌改为 32 格内最近的非 Boss 侧活体（BossEmpower.java:63-88）。
酒桶王额外：每 600 tick 且血量低于 50% 时，24 格内分身不足 2 个就再召 1 个（BossEmpower.java:92-112）。

## 4. 狂暴（BossRage）

| 项 | 值 | 出处 |
|---|---|---|
| 触发阈值 | 血量比例 ≤0.50 → 狂暴 I；≤0.25 → 狂暴 II；只升不降 | BossRage.java:40-46 |
| 刷新 | 每 20 tick 一次，效果时长 80 tick | BossRage.java:49,85 |
| 狂暴 I | 迅捷 I + 抗性提升 I | BossRage.java:85-89 |
| 狂暴 II | 再加 力量 I + 生命恢复 I | BossRage.java:90-95 |
| 故障机器人例外 | 纯进攻档：迅捷（II 级时为 II 级）+ 力量（II 级时 I 级），**不给抗性、不给再生** | BossRage.java:84-94 |
| 层级存储 | 持久数据键 `ftboss_rage_tier`，跨区块重载不重复触发 | BossRage.java:29 |

狂暴触发有演出（`RAVAGER_ROAR` 1.7 音量 + `WITHER_SPAWN`、35 个红尘粒子、26 个 `ANGRY_VILLAGER` 环），**不再发聊天播报**（BossRage.java:67-76）。

## 5. 召唤限制

| 项 | 值 | 出处 |
|---|---|---|
| 同种召唤物上限 | 10 只 | `MAX_PER_TYPE`，SummonLimit.java:38 |
| 统计半径 | 64.0 格 | `RADIUS`，SummonLimit.java:40 |
| 到上限的替代 | 改为给同类全体 速度 II + 力量 I + 抗性 I，持续 400 tick | `BUFF_TICKS`，SummonLimit.java:42 |
| 主世界限定 | 只在 `minecraft:overworld` 能召唤，未通过则中止且**不消耗召唤物** | `SummonGate.OVERWORLD_ONLY` |

## 6. 玩家侧：信标增益（2026-09-18 收窄）

| 项 | 值 | 出处 |
|---|---|---|
| 触发条件 | 玩家 64 格内有**隐藏 Boss（甜苹果庄园彩蛋十人）**才发 | `hiddenBossNear`，BossHandler.java |
| 检查节奏 | 每 10 tick | BossHandler.onPlayerTick |
| 效果 | 速度 II + 急迫 II + 抗性提升 II + 跳跃提升 II + 力量 II + 生命恢复 II | `BEACON_EFFECTS`，BossHandler.java:515 |
| 时长 / 补发条件 | 240 tick；无该效果、或 amplifier < 1、或剩余 < 60 tick 时补 | `applyBeaconBuffs`，BossHandler.java:540 |

**常规 Boss 战不再发这六项。** 早期版本对「任意本模组 Boss」都发，等于常规战白送六项 II 级增益（其中生命恢复 II 最显眼），已收窄到只认隐藏 Boss。

## 7. 玩家侧：禁疗与削甲

| 项 | 值 | 出处 |
|---|---|---|
| 禁疗时长 | 600 tick（30 秒） | `BAN_TICKS`，HealBan.java:43 |
| 禁疗实现 | 优先挂 `relics:anti_heal` 作可见标记，没装则退回内部计时表；真正生效靠监听 `LivingHealEvent` 并 `setAmount(0)` | HealBan.java |
| 禁疗触发点（常规 Boss 相关） | 仅**麻雀**（隐藏 Boss 支援）会以 10% 概率施放；常规 Boss 自身不施放 | `SPARROW_NOHEAL_CHANCE` |
| 削甲 | 每层 −5 护甲、最多 5 层（合计 −25）、每层 200 tick、再命中刷新 | `ArmorShred.PER_STACK/MAX_STACKS/DURATION_TICKS` |

## 8. 连死怜悯（BossMercy）

| 项 | 值 | 出处 |
|---|---|---|
| 触发 | 同一玩家、**同一只 Boss 实例**（记账键 `i:<实体UUID>`）连续死亡 5 次 | `LIMIT`，BossMercy.java:44 |
| 后果 | 该只 Boss 及其召唤物直接离场，**无掉落、无经验** | BossMercy.java:118-124 |
| 计数存储 | `SavedData`，死亡重生、退出重进、重启都保留 | `BossMercyData` |
| 过期 | 12000 tick（10 分钟）未再死亡即归零 | `STREAK_TIMEOUT`，BossMercyData.java:34 |
| 击杀清零 | Boss 被击杀时清零其 96 格内所有玩家的该实例计数 | BossMercy.java:150-153 |
| 归因顺序 | 击杀者（投射物取主人）→ 召唤物则取 64 格内最近 Boss → 200 tick 内打中过且仍在 48 格内 → 32 格内最近 Boss | BossMercy.java:172-210 |
| 隐藏 Boss | **不参与**连死怜悯（不累计、不提示、不移出场地） | BossMercy.java:111-115 |

## 9. 血条、脱战与清理

| 项 | 值 | 出处 |
|---|---|---|
| 血条可见半径 | 各实体自带均为 48.0 格 | 各 Boss 的 `BAR_RANGE` |
| 兜底清扫 | 每 40 tick 扫一次，登记实体不再存活或已移除则隐藏并注销 | `BossBars.sweep`，BossHandler.java:31-33 |
| 退场清理 | `EntityLeaveLevelEvent` 时立即注销血条 | BossHandler.java |
| 玩家死亡 | 立即从**所有**血条摘除并结算连死怜悯 | `BossBars.forgetPlayer`，BossHandler.java:354 |
| 玩家重生 / 重连 | 重生摘除旧实例；重连重挂 | `PlayerEvent.Clone` / `PlayerLoggedInEvent` |

## 10. 音乐调度（常规 Boss 部分）

| 项 | 值 | 出处 |
|---|---|---|
| 总开关 | `MUSIC_ENABLED = true` | FTSounds |
| 播放半径 / 停止广播半径 | 96.0 / 128.0 格 | `HEAR_RANGE` / `STOP_RANGE`，BossMusic.java:43,45 |
| 定曲规则 | **第一个开战**的 Boss 决定曲目组，后续只登记；新 Boss 的组与当前不同**且当前组已无活着成员**时才换曲 | `BossMusic.request`，BossMusic.java:583-599 |
| 空场宽限 | 100 tick（5 秒）内不停；超时才停；若最后一位是**真死**再播一次落幕钟声 | `GRACE_TICKS`，BossMusic.java:62 |
| 三幕剧（未登记 Boss 兜底） | 按全场最低血量比例推进：>0.66 序幕 / >0.33 假面圆舞 / 其余 断线狂宴；每幕最短 200 tick | `ACT2_AT` / `ACT3_AT` / `MIN_DWELL` |
| 专属曲 | 十只常规 Boss 各有自己的曲子（详见各篇） | `groupOf`，BossMusic.java:356 |
| 双播防护 | 开演与换曲前 `stopAllBgmNearby()` 停掉附近全部 BGM 事件 | BossMusic.java:755 |
| 召唤物不参与 | 持久标记 `ftboss_no_bgm` | `markNoBgm` / `isNoBgm` |

客户端（`BossMusicClient`）负责压掉原版音乐，并在附近 128 格内没有 BGM Boss 时兜底停止；
起播后 40 tick 是宽限期，期间不判「没在播」。

## 11. 掉落与交易（常规 Boss 部分）

| 项 | 值 | 出处 |
|---|---|---|
| 统一掉落 | 每个 Boss 掉**自己的石像** | `BossMercy.onBossDefeated`，BossMercy.java:140-143 |
| 额外武器掉落 | 糖霜女皇掉糖果拐杖、苹果爵士掉苹果果梗，其余见各篇 | BossMercy.java:372 |
| 石像方块 | 8 座：女皇 / 屠夫 / 沙耶 / 酒桶王 / 鼠群 / 故障机器人 / 答辩超人 / 苹果爵士 | `block/FTBlocks.java` |
| 石像交易 | `trade/TradeHandler` 读 `config/ftboss_trades.json`；**当前没有触发点**（右键钩子已随通用石像一并移除） | TradeHandler |

潮宴鱼王另有独立掉落（潮骨三叉戟 / 潮汐珍珠 / 唱片），见该篇。
