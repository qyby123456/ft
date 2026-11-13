# qys_event 每日世界事件 + 预言机 —— 代码行为研究报告

- 工程根：`_work/qys-event`（Mod 版本 `1.0.3`：`src/main/resources/META-INF/mods.toml:7`、`build.gradle:8`；modId `qys_event`）
- 研究方式：只读源码 + 只读实例存档；**本文所有结论均对应当前代码，不含历史修改记录**
- 源码快照（本次研究读取到的版本，SHA-256 前 12 位）：

| 文件（相对工程根） | mtime | SHA-256(12) |
| --- | --- | --- |
| `src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEvent.java` | 2026-10-05 10:26:07 | `F548A01EB350` |
| `src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEventRules.java` | 2026-10-05 11:08:55 | `7E16AAFB9D09` |
| `src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEventData.java` | 2026-10-05 10:48:21 | `B73CB67A96C1` |
| `src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEventHooks.java` | 2026-10-05 11:21:22 | `7860B41E0B0A` |
| `src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEventManager.java` | 2026-10-05 12:58:32 | `35EF9945817D` |
| `src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEventCommand.java` | 2026-10-05 12:58:32 | `198AAA7C9A8B` |
| `src/main/java/cn/farmingtales/qysevent/OracleBlock.java` | 2026-10-05 10:42:31 | `2CE4F9937FF9` |
| `src/main/java/cn/farmingtales/qysevent/WeatherManager.java` | 2026-10-05 12:58:32 | `1A528FEB6D40` |
| `src/main/java/cn/farmingtales/qysevent/WeatherSavedData.java` | 2026-10-05 10:24:12 | `DB2751B94233` |

> 读取期间该工作树正在被并发编辑（`DailyWorldEventManager.java`、`DailyWorldEventCommand.java`、`WishingWellBlock.java`、`WeatherManager.java`、`README.md` 在 2026-10-05 12:58 前后被改动）。本文引用的是上表哈希对应的版本；若后续再改动，行号可能偏移。

---

## 0. 关键数字速览

| 项目 | 数值 | 位置 |
| --- | --- | --- |
| 事件总数 | 19 | `worldevent/DailyWorldEvent.java:9-27`；测试断言 `src/test/java/cn/farmingtales/qysevent/QysEventVerifier.java:11` |
| 每日触发概率 | 10%（`DAILY_TRIGGER_CHANCE = 0.10D`，硬编码，**不受配置/数据包影响**） | `worldevent/DailyWorldEventRules.java:4,48-50` |
| 单事件每日无条件概率 | 10% × 1/19 ≈ **0.5263%**（候选为 19 天固定排列） | `DailyWorldEventRules.java:35-46`、`DailyWorldEventManager.java:43-46` |
| 一天的长度 | 24000 tick = 20 现实分钟（`day = floorDiv(overworld().getDayTime(), 24000)`） | `DailyWorldEventManager.java:39-41` |
| 同一天可叠加事件数 | 1（只存一个 `ActiveEvent` 字符串） | `DailyWorldEventData.java:19-23,70-82` |
| 事件轮询/判定频率 | 每 20 tick（1 秒）一次 | `DailyWorldEventHooks.java:78-81` |
| 替代生物远去判定半径 | 48 格 | `DailyWorldEventHooks.java:371` |
| 替代生物远去后消失 | 宽限 600 tick（30 秒）起、每次检查 35% 概率消失；硬上限 1200 tick（60 秒）必消失；检查间隔 100 tick（5 秒） | `DailyWorldEventHooks.java:70-71,83,358-383` |
| 预言机查看天数 | 10 天（D+1…D+10）；`forecast()` 内部上限 30 | `OracleBlock.java:51`、`DailyWorldEventManager.java:71-81` |
| 预言机准确率 | 45% 真话 / 28% 假话 / 27% 错乱（8 种文本均分） | `DailyWorldEventManager.java:31-33,97-118` |
| 预言机消耗 | 1 颗绿宝石（创造模式不消耗） | `OracleBlock.java:39-47` |

---

## 1. 每日事件总表（19 种，逐一）

### 1.1 所有事件共用的判定框架

| 机制 | 说明 | 位置 |
| --- | --- | --- |
| 生效判定 | `DailyWorldEventManager.active(server)` 直接返回存档中的 `activeEvent()` | `DailyWorldEventManager.java:48-51` |
| 每日抽取 | 每 20 tick：`tick(server)` → `synchronize(server, true)`；跨天时按 `triggers()` 判定，触发则取 `candidate()`，否则置空 | `DailyWorldEventHooks.java:78-81`、`DailyWorldEventManager.java:54-64,144-178` |
| 候选事件 | 由世界种子决定的 19 元固定洗牌，取 `bag[floorMod(day,19)]`：**19 天为一周期，19 天内 19 个事件各出现一次、不重复，条件概率均等 1/19** | `DailyWorldEventRules.java:35-46` |
| 纯函数特性 | `triggers()` 与 `candidate()` 只依赖（世界种子, 天数），无随机数状态；同存档同天结果固定 | `DailyWorldEventRules.java:35-50` |
| 可被影响的新生成生物 | `MobSpawnEvent.FinalizeSpawn`（LOWEST 优先级）标记 `qys_event_daily_spawn=true`；限定生成类型：`NATURAL / CHUNK_GENERATION / PATROL / REINFORCEMENT`（4 种野外类型）或 `SPAWNER`（刷怪笼）；其中只有 4 种野外类型额外标记 `qys_event_wild_spawn=true` | `DailyWorldEventHooks.java:103-111,169-176` |
| 不受影响的生成 | 刷怪蛋、命令召唤、结构生成、繁殖、桶装、EVENT 召唤（即事件自身的替代生物）等都不在标记范围 | `DailyWorldEventHooks.java:169-176` |
| 生物替换执行点 | `EntityJoinLevelEvent`（LOW 优先级）；已有 `qys_event_replacement` 标记的生物只做“登记跟踪”，不再二次处理 | `DailyWorldEventHooks.java:113-167` |
| 保护名单（永不被替换/改动） | `MobCategory.MISC`、`isPersistenceRequired()`、有自定义名、已驯服的 `TamableAnimal`、`EnderDragon`、`WitherBoss` | `DailyWorldEventHooks.java:190-195`、调用点 `:123` |
| 替代生物创建方式 | 在源生物坐标复制朝向创建，标记 `qys_event_replacement`，以 `MobSpawnType.EVENT` 执行 `finalizeSpawn`，`noCollision` 通过才加入世界，随后取消原 `EntityJoinLevelEvent`（即原生物不入世界） | `DailyWorldEventHooks.java:292-303` |
| 生效范围 | 每日事件为**服务器全局单一事件**，对所有维度生效；除“大雪”外没有维度限制 | `DailyWorldEventManager.java:144-178`；维度限制见 `DailyWorldEventHooks.java:225-228` |
| 只影响“事件生效后新加入世界的生物” | 所有生物侧改动都在 join/finalize 阶段执行，已存在的生物不会被追溯修改 | `DailyWorldEventHooks.java:114-167` |

### 1.2 总览表

| # | 中文名（lang 查证） | 内部 id | enum 序号 | 分类 | 候选权重/概率 | 持续时间 | 作用范围 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 大鱼洄游 | `big_fish_migration` | 0 | 生物替换（鱼类） | 条件 1/19；无条件 0.526% | 1 游戏日（24000 tick / 20 分钟） | 全维度，所有日常生成的 `AbstractFish` |
| 2 | 胡闹厨房 | `kitchen_chaos` | 1 | 生物替换 + 农业 | 同上 | 1 游戏日 | 全维度，`MONSTER` 类日常生成；任何维度成熟作物收获 |
| 3 | 根本没有这样的生物！ | `no_such_creature` | 2 | 生物骑乘 + 音效 | 同上 | 1 游戏日 | 全维度，日常生成的苦力怕；幻翼坐骑受 48 格清理 |
| 4 | 死灭洄游 | `undead_migration` | 3 | 生物替换（亡灵） | 同上 | 1 游戏日 | 全维度，所有非亡灵日常生成 |
| 5 | 死猪回游 | `pig_migration` | 4 | 生物替换（猪） | 同上 | 1 游戏日 | 全维度，所有非猪日常生成 |
| 6 | 大雪 | `heavy_snow` | 5 | 天气 + 生物替换 + 玩家 debuff | 同上 | 1 游戏日（雨每 10 秒续期；每次 24000 tick） | **仅主世界**；敌怪替换要求露天降雨 |
| 7 | 牢大受难日 | `kobe_memorial` | 6 | 物品发放 | 同上 | 1 游戏日（每人每天 1 次） | 全维度所有玩家 |
| 8 | 丰收月 | `harvest_moon` | 7 | 农业 | 同上 | 1 游戏日 | 全维度作物生长/收获 |
| 9 | 史莱姆雨 | `slime_rain` | 8 | 生物替换 | 同上 | 1 游戏日 | 全维度，`MONSTER` 类日常生成（下界为岩浆怪） |
| 10 | 星落夜 | `starfall` | 9 | 生物发光 + 掉落 + 客户端粒子 | 同上 | 1 游戏日 | 全维度生物发光/掉落；粒子仅主世界客户端 |
| 11 | 猩红月潮 | `blood_moon` | 10 | 生物强化 + 掉落 + 客户端粒子 | 同上 | 1 游戏日（怪物 buff 24000 tick） | 全维度，`MONSTER` 类日常生成 |
| 12 | 镀金矿脉 | `gold_rush` | 11 | 方块破坏掉落 | 同上 | 1 游戏日 | 全维度，玩家（非创造）破坏 Forge 矿石标签方块 |
| 13 | 兔潮失控 | `rabbit_tide` | 12 | 生物替换（兔） | 同上 | 1 游戏日 | 全维度，`CREATURE` 类日常生成 |
| 14 | 女巫茶会 | `witch_convention` | 13 | 生物替换（女巫） | 同上 | 1 游戏日 | 全维度，`MONSTER` 类日常生成 |
| 15 | 失重故障 | `gravity_glitch` | 14 | 生物浮空 + 玩家增益 | 同上 | 1 游戏日（玩家效果每 5 秒刷新，持续 7 秒） | 全维度所有非 MISC 日常生成 + 所有玩家 |
| 16 | 蘑菇泛滥 | `mushroom_bloom` | 15 | 生物替换 + 方块破坏掉落 | 同上 | 1 游戏日 | 全维度，`CREATURE` 类日常生成；树叶/花破坏 |
| 17 | 末影蚀 | `ender_eclipse` | 16 | 生物替换 + 客户端粒子 | 同上 | 1 游戏日 | 全维度，`MONSTER` 类日常生成；粒子仅主世界客户端 |
| 18 | 萤火之夜 | `firefly_night` | 17 | 掉落 + 客户端粒子 | 同上 | 1 游戏日 | 全维度掉落；粒子仅主世界客户端 |
| 19 | 好运日 | `lucky_day` | 18 | 玩家增益 + 掉落 | 同上 | 1 游戏日（玩家幸运每 10 秒刷新，持续 12 秒） | 全维度所有玩家 + 玩家击杀 |

> 中文名来源：`src/main/resources/assets/qys_event/lang/zh_cn.json` 第 55–92 行的 `event.qys_event.<id>.name` 键（逐个对应：55/57/59/61/63/65/67/69/71/73/75/77/79/81/83/85/87/89/91 行）。
> 注意：特殊天气的名字用的是另一套键 `qys_event.event.<id>`（`zh_cn.json:6-45`），两套键前缀恰好相反，写文档时勿混用。

### 1.3 逐个事件明细

#### 1.3.1 大鱼洄游 `big_fish_migration`（enum 0）

| 项目 | 数值/行为 | 位置 |
| --- | --- | --- |
| 作用对象 | 日常生成标记的 `AbstractFish`（鳕鱼/鲑鱼/河豚/热带鱼等） | `DailyWorldEventHooks.java:135,197-198` |
| 抽取 | 每只鱼生成时 `level.random.nextDouble()` 落入下表 | `DailyWorldEventHooks.java:199`、`DailyWorldEventRules.java:52-59` |

| 掷点区间 | 概率 | 结果实体 | 无 Mod 后备 |
| --- | --- | --- | --- |
| `[0, 0.01)` | 1.0% | `cataclysm:amethyst_crab` | `minecraft:guardian` |
| `[0.01, 0.035)` | 2.5% | `minecraft:elder_guardian` | 原版 |
| `[0.035, 0.085)` | 5.0% | `minecraft:guardian` | 原版 |
| `[0.085, 0.185)` | 10.0% | `minecraft:drowned` | 原版 |
| `[0.185, 0.535)` | 35.0% | 50/50 → `minecraft:cod` 或 `minecraft:salmon`；**若源生物是热带鱼或河豚则不替换** | 原版 |
| `[0.535, 1.0)` | 46.5% | 不变 | — |

- 数量上限：除“替代生物 48 格清理”外**无数量上限**（`DailyWorldEventHooks.java:358-383`）。
- 维度：无限制（`replaceAquatic` 不检查维度，`DailyWorldEventHooks.java:197-210`）。

#### 1.3.2 胡闹厨房 `kitchen_chaos`（enum 1）

| 项目 | 数值/行为 | 位置 |
| --- | --- | --- |
| 作用对象 | `MobCategory.MONSTER` 的日常生成 | `DailyWorldEventHooks.java:136,212-213` |
| 若目标类型与源类型相同则不替换 | 是 | `DailyWorldEventHooks.java:221` |

| 掷点区间 | 概率 | 结果实体（外部 id） | 无对应 Mod 后备 |
| --- | --- | --- | --- |
| `[0, 0.12)` | 12% | `ftboss:gingerbread_minion` 小姜饼人 | `minecraft:slime` |
| `[0.12, 0.16)` | 4% | `brewery:beer_elemental` 啤酒元素 | `minecraft:witch` |
| `[0.16, 0.19)` | 3% | `ftboss:barrel_minion` 小酒桶 | `minecraft:creeper` |
| `[0.19, 0.24)` | 5% | `ftboss:tea_minion` 茶芽小妖 | `minecraft:bee` |
| `[0.24, 1.0)` | 76% | 不变 | — |

（掷点表：`DailyWorldEventRules.java:61-67`；实体与后备：`DailyWorldEventHooks.java:214-221`。）

| 农业效果 | 数值 | 位置 |
| --- | --- | --- |
| 成熟作物收获倍率 | 1×/2×/3× 各 1/3（`1 + min(2, floor(roll*3))`，**绝不为 0**） | `DailyWorldEventRules.java:103-105`、`DailyWorldEventHooks.java:322-323` |
| 倍率生效条件 | 非创造玩家破坏成熟作物（`CropBlock` 最大龄 / `NetherWartBlock.AGE>=3` / `CocoaBlock.AGE>=2` / `StemGrownBlock`），掉落实体在同坐标、3 game tick 内生成 | `DailyWorldEventHooks.java:316-347` |
| 数量上限 | 每个掉落实体 `min(该物品最大堆叠, 原数量×倍率)` | `DailyWorldEventHooks.java:337` |

#### 1.3.3 根本没有这样的生物！ `no_such_creature`（enum 2）

| 项目 | 数值/行为 | 位置 |
| --- | --- | --- |
| 作用对象 | 日常生成（含刷怪笼）的 `minecraft:creeper`（受保护名单除外） | `DailyWorldEventHooks.java:137,273` |
| 效果 | 在苦力怕上方 +1.0 格生成 `minecraft:phantom`（`setPhantomSize(1)`），标记 `qys_event_replacement` 并登记，随后 `creeper.startRiding(phantom, true)` 强制骑乘 | `DailyWorldEventHooks.java:274-285` |
| 原苦力怕 | **保留**（不取消 join 事件）；苦力怕自身也被登记进替代生物清理表 | `DailyWorldEventHooks.java:281-283` |
| 幻翼生成方式 | `MobSpawnType.EVENT`，`addFreshEntity` 失败则不骑乘 | `DailyWorldEventHooks.java:279-280` |
| 音效氛围 | 每 80 tick（4 秒）对每名玩家 20% 概率播放 `CREEPER_PRIMED`（音量 0.12，音高 0.65–0.85） | `DailyWorldEventHooks.java:85,427-433` |
| 数量上限 | 无（除 48 格清理规则） | `DailyWorldEventHooks.java:358-383` |

#### 1.3.4 死灭洄游 `undead_migration`（enum 3）

| 条件 | 结果实体 | 位置 |
| --- | --- | --- |
| 源生物已是 `MobType.UNDEAD` | 不变 | `DailyWorldEventHooks.java:259` |
| `WaterAnimal` 或处于水/气泡柱中 | `minecraft:drowned` | `DailyWorldEventHooks.java:260-261` |
| 下界维度 | `minecraft:wither_skeleton` | `DailyWorldEventHooks.java:262` |
| 其余 | 50/50 → `minecraft:zombie` 或 `minecraft:skeleton` | `DailyWorldEventHooks.java:263` |

- 触发概率：**100%**（无掷点，除保护名单与日常生成标记外全部替换）——`DailyWorldEventHooks.java:138,258-265`。
- 作用对象不分类别：动物、鱼类、怪物都会被替换（`MobCategory.CREATURE` 也在内）。
- 维度：全维度，下界走凋灵骷髅分支。

#### 1.3.5 死猪回游 `pig_migration`（enum 4）

| 项目 | 数值/行为 | 位置 |
| --- | --- | --- |
| 概率 | 100%（非猪即换） | `DailyWorldEventHooks.java:139` |
| 结果实体 | `minecraft:pig` | 同上 |
| 作用对象 | 所有日常生成（含刷怪笼）且不在保护名单中的生物（怪物同样变猪） | `DailyWorldEventHooks.java:123,139` |

#### 1.3.6 大雪 `heavy_snow`（enum 5）——最复杂的事件

**A. 天气层**

| 项目 | 数值 | 位置 |
| --- | --- | --- |
| 事件开始时 | `overworld().setWeatherParameters(0, 24000, true, false)` → 主世界下雨 24000 tick、无雷 | `DailyWorldEventManager.java:182-183` |
| 持续期续期 | 每 200 tick（10 秒）若主世界未下雨，则 `setWeatherParameters(0, 12000, true, false)` | `DailyWorldEventManager.java:60-63` |
| 与原版天气控制的耦合 | 天气系统每秒 `applyVanillaWeather` 时若每日事件为 `HEAVY_SNOW`，强制 `rain=true, thunder=false`（需 `controlVanillaWeather=true`，默认 true） | `WeatherManager.java:294-295,309-314`；`WeatherConfig.java:24` |
| 事件结束时 | 若前一日事件是 `HEAVY_SNOW`，调用 `WeatherManager.reapplyVanillaWeather` 恢复 | `DailyWorldEventManager.java:184-186` → `WeatherManager.java:317-320` |
| 降水颜色 | `0x6B3E23`（棕褐 RGB 107,62,35），客户端降水统一染成该色，并且**不再叠加雪花粒子** | `DailyWorldEventRules.java:28`、`client/ClientWeatherState.java:25-32`、`client/ClientWeatherParticles.java:48` |

**B. 露天淋雨的玩家 debuff（每 20 tick 对每名玩家判定一次）**

| 条件 | 数值 | 位置 |
| --- | --- | --- |
| 前置 | 非旁观者、维度=主世界、`level.isRainingAt(pos.above())` | `DailyWorldEventHooks.java:178-181` |
| 反胃 | `CONFUSION` 80 tick（4 秒）等级 I（amplifier 0），ambient=false/visible=true/icon=true | `DailyWorldEventHooks.java:184` |
| “屎？！” | 注册表存在 `ftboss:poop_confusion` 时给该效果 80 tick（4 秒）等级 I | `DailyWorldEventHooks.java:66,185-186` |
| 后备 | 效果不存在时改为 `minecraft:hunger`（饥饿）**II 级**（amplifier 1）80 tick（4 秒） | `DailyWorldEventHooks.java:187` |

**C. 野外敌怪替换**（前置：主世界 + 必须是野外生成 `WILD_SPAWN`（刷怪笼不算）+ `MONSTER` 类 + 该位置正在降雨）

| 掷点区间 | 概率 | 结果实体（`ftboss:` 前缀） | 无 Boss Mod 后备 |
| --- | --- | --- | --- |
| `[0, 0.55)` | 55% | 不变（提前 return） | — |
| `[0.55, 0.71)` | 16% | `fly_minion` 苍蝇 | `minecraft:spider` |
| `[0.71, 0.84)` | 13% | `maggot_minion` 蛆 | `minecraft:silverfish` |
| `[0.84, 0.90)` | 6% | `poop_minion` 答辩小怪 | `minecraft:slime` |
| `[0.90, 0.94)` | 4% | `compost_slime` 棕黑史莱姆 | `minecraft:slime` |
| `[0.94, 0.96)` | 2% | `sheep_dung` 羊屎蛋 | `minecraft:slime` |
| `[0.96, 0.975)` | 1.5% | `apple_worm` 果虫 | `minecraft:silverfish` |
| `[0.975, 0.985)` | 1.0% | `apple_moth` 果蛾 | `minecraft:phantom` |
| `[0.985, 0.995)` | 1.0% | `rotten_apple` 烂苹果堆 | `minecraft:creeper` |
| `[0.995, 1.0)` | 0.5% | `giant_grub` 巨型果蛆 | `minecraft:ravager` |

（掷点表：`DailyWorldEventRules.java:74-85`；实体路径：`DailyWorldEventRules.java:87-100`；后备与执行：`DailyWorldEventHooks.java:224-251`。）

| 附加限制 | 数值 | 位置 |
| --- | --- | --- |
| 巨型果蛆数量上限 | 目标实体（含无 Mod 时的 `ravager`）**96 格包围盒内最多 1 只**，超过则本次不替换 | `DailyWorldEventHooks.java:248-256` |
| 替换总比例 | 45%（=16+13+6+4+2+1.5+1+1+0.5） | `DailyWorldEventRules.java:69-85` |
| 刷怪笼 | 不参与（因为要求 `WILD_SPAWN`；`SPAWNER` 只标记 `DAILY_SPAWN`） | `DailyWorldEventHooks.java:107-110,169-176,226` |
| 与源类型相同的目标 | **不检查**（与胡闹厨房不同），会新建同类型生物并取消原事件 | `DailyWorldEventHooks.java:250` 对比 `:221` |

#### 1.3.7 牢大受难日 `kobe_memorial`（enum 6）

| 项目 | 数值/行为 | 位置 |
| --- | --- | --- |
| 发放频率 | 每 20 tick 检查一次所有在线玩家 + 玩家登录时检查一次 | `DailyWorldEventManager.java:57-59,66-69` |
| 去重 | 存档 `ClaimedGifts` 记 `"<day>|<uuid>"`，每天每人 1 次 | `DailyWorldEventData.java:21,84-88` |
| 物品 | `smc:ice_tea`（[森罗酒馆：世界名酒] kaleidoscope_world_liquor 提供） | `DailyWorldEventManager.java:30,192-193` |
| 后备 | 物品不存在（或为 AIR）时给 `minecraft:honey_bottle` 并重命名为“冰红茶”（`item.qys_event.fallback_ice_tea`） | `DailyWorldEventManager.java:193-194`、`lang/zh_cn.json:96` |
| 背包满 | `player.drop(gift, false)` 掉在原地 | `DailyWorldEventManager.java:195` |
| 提示与表现 | 聊天“牢大受难日：你收到了一瓶冰红茶。”+ 8 个 `HAPPY_VILLAGER` 粒子 + `PLAYER_LEVELUP`（音量 0.6，音高 1.25） | `DailyWorldEventManager.java:196-202`、`lang/zh_cn.json:95` |
| 存档清理 | `ClaimedGifts` 超过 4096 条时才清理 370 天前的记录 | `DailyWorldEventData.java:73-80` |

#### 1.3.8 丰收月 `harvest_moon`（enum 7）

| 项目 | 数值 | 位置 |
| --- | --- | --- |
| 作物生长加速 | `BlockEvent.CropGrowEvent.Pre`（LOW 优先级）中 35% 概率 `Event.Result.ALLOW`（强制推进一次生长） | `DailyWorldEventHooks.java:309-314` |
| 成熟作物收获 | 所有成熟作物掉落 **×2**（总量按最大堆叠截断） | `DailyWorldEventHooks.java:321,337` |
| 生效条件 | 玩家非创造破坏 + 成熟判定（见 1.3.2） | `DailyWorldEventHooks.java:316-326,341-347` |
| 维度/范围 | 任意维度、任意距离（无半径限制） | `DailyWorldEventHooks.java:310-312` |

#### 1.3.9 史莱姆雨 `slime_rain`（enum 8）

| 项目 | 数值 | 位置 |
| --- | --- | --- |
| 作用对象 | `MONSTER` 类日常生成，且本身不是 `Slime` | `DailyWorldEventHooks.java:141,267-269` |
| 概率 | 25%（`.25F`） | `DailyWorldEventHooks.java:269` |
| 结果 | 主世界/末地等 → `minecraft:slime`；下界 → `minecraft:magma_cube` | `DailyWorldEventHooks.java:270` |
| 数量上限 | 无（除 48 格清理） | `DailyWorldEventHooks.java:358-383` |
| 与同名特殊天气的区别 | 本事件**不生成天降史莱姆**；天降史莱姆属于特殊天气 `WeatherType.SLIME_RAIN`（中文名同为“史莱姆从天而降”），走 `WeatherManager` 的独立逻辑并有 `slimeCapPerPlayer`（默认 16）上限 | `WeatherType.java:12`、`WeatherManager.java:79,805-825`、`WeatherConfig.java:41` |

#### 1.3.10 星落夜 `starfall`（enum 9）

| 项目 | 数值 | 位置 |
| --- | --- | --- |
| 生物发光 | 任何非保护日常生成生物 45% 概率 `setGlowingTag(true)` | `DailyWorldEventHooks.java:142` |
| 掉落（玩家击杀 `Enemy`） | 掷点 <2% → `minecraft:amethyst_shard`×1；2%–10% → `minecraft:emerald`×1；其余无额外掉落 | `DailyWorldEventHooks.java:399-403` |
| 客户端粒子 | 每 5 Hz tick 65% 概率生成 1 个 `END_ROD` 下落粒子（半径 13、高度 12、速度 0.15），仅主世界且能看到天空 | `client/ClientWeatherParticles.java:40,44,55-57,122-132` |
| 昼夜限制 | 代码中**没有**昼夜判定（lang 描述称“夜空”，但实现只要求能看到天空） | `client/ClientWeatherParticles.java:44,55-57` |

#### 1.3.11 猩红月潮 `blood_moon`（enum 10）

| 项目 | 数值 | 位置 |
| --- | --- | --- |
| 作用对象 | `MONSTER` 类日常生成 | `DailyWorldEventHooks.java:128-129` |
| 力量 | `DAMAGE_BOOST` 24000 tick（20 分钟）等级 I（amplifier 0） | `DailyWorldEventHooks.java:130` |
| 速度 | `MOVEMENT_SPEED` 24000 tick（20 分钟）等级 I（amplifier 0） | `DailyWorldEventHooks.java:131` |
| 发光 | `mob.setGlowingTag(true)` | `DailyWorldEventHooks.java:132` |
| 掉落（玩家击杀 `Enemy`） | 30% 概率额外掉落 `minecraft:redstone` 1–3 个（`1+rand(3)`） | `DailyWorldEventHooks.java:391-392` |
| 客户端粒子 | 每 5 Hz tick 1 个 `CRIMSON_SPORE` 下落粒子（半径 9、高度 4、速度 0.01），若当前特殊天气不是 `BLOOD_MOON` 才生成 | `client/ClientWeatherParticles.java:58-60` |

#### 1.3.12 镀金矿脉 `gold_rush`（enum 11）

| 项目 | 数值 | 位置 |
| --- | --- | --- |
| 前置 | 玩家非创造、`BlockEvent.BreakEvent` 未取消、方块属于 Forge 方块标签 `forge:ores`（`Tags.Blocks.ORES`） | `DailyWorldEventHooks.java:412-415` |
| 掷点 | <1.2% → `minecraft:raw_gold`×1；1.2%–13% → `minecraft:gold_nugget` 1–3 个（`1+rand(3)`）；≥13% 无 | `DailyWorldEventHooks.java:416-418` |
| 总计 | 13% 概率额外掉落；掉落物生成在被破坏方块坐标 | `DailyWorldEventHooks.java:417-418` |
| 维度 | 任意维度 | `DailyWorldEventHooks.java:413` |

#### 1.3.13 兔潮失控 `rabbit_tide`（enum 12）

| 项目 | 数值 | 位置 |
| --- | --- | --- |
| 作用对象 | `MobCategory.CREATURE` 且本身不是 `minecraft:rabbit` | `DailyWorldEventHooks.java:143-144` |
| 概率 | 65% | `DailyWorldEventHooks.java:145` |
| 结果 | `minecraft:rabbit` | 同上 |
| 数量上限 | 无（除 48 格清理） | `DailyWorldEventHooks.java:358-383` |

#### 1.3.14 女巫茶会 `witch_convention`（enum 13）

| 项目 | 数值 | 位置 |
| --- | --- | --- |
| 作用对象 | `MobCategory.MONSTER` 且本身不是 `minecraft:witch` | `DailyWorldEventHooks.java:147-148` |
| 概率 | 40% | `DailyWorldEventHooks.java:149` |
| 结果 | `minecraft:witch` | 同上 |
| 数量上限 | 无（除 48 格清理） | `DailyWorldEventHooks.java:358-383` |

#### 1.3.15 失重故障 `gravity_glitch`（enum 14）

| 对象 | 效果 | 位置 |
| --- | --- | --- |
| 生物（非 `MISC`） | 70% 概率：`LEVITATION` 50+rand(50) tick（**2.5–4.95 秒**）等级 I + `SLOW_FALLING` 500 tick（25 秒）等级 I（默认 ambient=false、visible=true） | `DailyWorldEventHooks.java:151-156` |
| 玩家（所有维度） | 每 100 tick（5 秒）：`SLOW_FALLING` 140 tick（7 秒）等级 I + `JUMP` 140 tick（7 秒）**II 级**（amplifier 1）；ambient=true, visible=false, icon=true | `DailyWorldEventHooks.java:88-90` |
| 覆盖 | 玩家效果无维度限制、无距离限制 | `DailyWorldEventHooks.java:86-90` |

#### 1.3.16 蘑菇泛滥 `mushroom_bloom`（enum 15）

| 项目 | 数值 | 位置 |
| --- | --- | --- |
| 生物替换对象 | `MobCategory.CREATURE` 且不是 `minecraft:mooshroom`，概率 35%，结果 `minecraft:mooshroom` | `DailyWorldEventHooks.java:157-160` |
| 方块效果 | 玩家非创造破坏 `BlockTags.LEAVES`（树叶）或 `BlockTags.FLOWERS`（花）时 30% 概率掉落 1–2 个蘑菇（红/褐各 50%） | `DailyWorldEventHooks.java:419-424` |
| 掉落位置 | 被破坏方块坐标（`Block.popResource`） | `DailyWorldEventHooks.java:422` |

#### 1.3.17 末影蚀 `ender_eclipse`（enum 16）

| 项目 | 数值 | 位置 |
| --- | --- | --- |
| 生物替换对象 | `MobCategory.MONSTER` 且本身不是 `minecraft:enderman`，概率 28%，结果 `minecraft:enderman` | `DailyWorldEventHooks.java:161-164` |
| 客户端粒子 | 每 5 Hz tick 1 个 `PORTAL` 环境粒子（半径 9、高度 4、速度 0.025） | `client/ClientWeatherParticles.java:51` |
| 昼夜限制 | 代码中**没有**昼夜判定（lang 描述称“夜间”，实现只要求能看到天空且在主世界） | `client/ClientWeatherParticles.java:44,51`、`WeatherManager.java:89` |

#### 1.3.18 萤火之夜 `firefly_night`（enum 17）

| 项目 | 数值 | 位置 |
| --- | --- | --- |
| 掉落（玩家击杀 `Enemy`） | 24% 概率 `minecraft:glowstone_dust` 1–2 个（`1+rand(2)`） | `DailyWorldEventHooks.java:393-394` |
| 客户端粒子 | 每 5 Hz tick 1 个 `GLOW` 环境粒子（半径 10、高度 4、速度 0.006）；若当前特殊天气已经是 `FIREFLY_NIGHT` 则不额外生成 | `client/ClientWeatherParticles.java:52-54` |
| 生物替换 | 无 | `DailyWorldEventHooks.java:127-166`（无该 case） |
| 昼夜限制 | 代码中**没有**昼夜判定 | `client/ClientWeatherParticles.java:44,52-54` |

#### 1.3.19 好运日 `lucky_day`（enum 18）

| 项目 | 数值 | 位置 |
| --- | --- | --- |
| 玩家增益 | 每 200 tick（10 秒）给所有玩家 `LUCK` 240 tick（12 秒）等级 I（ambient=true, visible=false, icon=true） | `DailyWorldEventHooks.java:91-92` |
| 击杀掉落 | 单次掷点：<2.5% → `minecraft:emerald`×1；2.5%–12% → `minecraft:iron_nugget` 1–4 个（`1+rand(4)`）；其余无 | `DailyWorldEventHooks.java:395-398` |
| 维度 | 玩家增益任意维度；掉落取决于死亡生物所在维度 | `DailyWorldEventHooks.java:86-93,387-390` |

### 1.4 掉落类事件的公共前置（血月/萤火/好运/星落）

| 条件 | 说明 | 位置 |
| --- | --- | --- |
| 死亡生物必须是 `net.minecraft.world.entity.monster.Enemy` | 动物、Boss（非 Enemy 实现）等不触发 | `DailyWorldEventHooks.java:388` |
| 击杀者必须是 `ServerPlayer` | 间接死亡（摔落/岩浆）不触发 | `DailyWorldEventHooks.java:389` |
| 额外掉落位置 | 死亡生物坐标 Y+0.4 | `DailyWorldEventHooks.java:406-409` |

---

## 2. 触发规则

### 2.1 每日触发概率（README 所述 10% 已核实）

| 项目 | 代码事实 | 位置 |
| --- | --- | --- |
| 常量 | `DAILY_TRIGGER_CHANCE = 0.10D` | `worldevent/DailyWorldEventRules.java:4` |
| 判定式 | `unit(mix64(worldSeed ^ TRIGGER_SALT ^ day * DAY_GAMMA)) < 0.10` | `DailyWorldEventRules.java:29-31,48-50,107-112` |
| 是否配置项 | **不是**：`worldevent` 包内没有任何 `WeatherConfig` / `ForgeConfigSpec` 引用（每日事件无配置文件、无数据包参数） | 全包搜索结果为空；`WeatherConfig` 仅含天气项（`WeatherConfig.java:9-52`） |
| 是否真随机 | 否。是基于（世界种子, 天数）的确定性哈希，同存档同一天结果固定；长期比例约 10%，但并非每次现掷随机数 | `DailyWorldEventRules.java:48-50` |
| 与候选事件的关系 | 两条独立哈希流（`TRIGGER_SALT` 判是否触发；`ORDER_SALT` 决定是哪个事件），所以“某事件在某天发生”的无条件概率 ≈ 0.10 × 1/19 ≈ 0.5263% | `DailyWorldEventRules.java:29-30,35-50` |
| 候选排列 | `eventIndex` 先按 `worldSeed ^ ORDER_SALT` 洗出 19 元排列，再取 `bag[floorMod(day,19)]`：19 天一个循环，一个循环内 19 个事件各一次 | `DailyWorldEventRules.java:35-46`；测试断言 `QysEventVerifier.java:18-25` |
| 判定时机 | 服务器 tick 每 20 tick（END 阶段）调用 `DailyWorldEventManager.tick` → `synchronize(server, true)`；跨天后 1 秒内完成判定与广播 | `DailyWorldEventHooks.java:77-81`、`DailyWorldEventManager.java:54-64,144-178` |

### 2.2 “一天”如何划分

| 项目 | 代码事实 | 位置 |
| --- | --- | --- |
| 天数定义 | `Math.floorDiv(server.overworld().getDayTime(), 24000L)` —— **主世界 DayTime**，不是现实日、不是 `gameTime` | `DailyWorldEventManager.java:39-41` |
| 边界时刻 | `dayTime % 24000 == 0`，即游戏内 06:00（一天开始）；每到边界换事件（最长延迟 1 秒） | `DailyWorldEventManager.java:55,146,153-160` |
| 1 天时长 | 24000 tick = 1200 秒 = 20 现实分钟 | 常量 24000：`DailyWorldEventManager.java:40`、`WeatherManager.java:49` |
| 现实日 | 完全不参与（代码中没有系统时间/日期读取） | `DailyWorldEventManager.java:39-41` |
| 副作用 | `getDayTime()` 受 `/time set` 影响，也只在原版 `doDaylightCycle=true` 时自行前进（Mod 只读取该值） | `DailyWorldEventManager.java:40` |

### 2.3 同一天能否叠加多个事件

**不能。** 存档只保存一个 `ActiveEvent` 字符串（`DailyWorldEventData.java:20`），`beginDay(day, active)` 每次整体覆盖（`DailyWorldEventData.java:70-82`）；`synchronize` 一天只选一个事件（`DailyWorldEventManager.java:162-167`）。玩家在一个游戏日内最多看到 1 个每日事件（可能为“未触发”）。

### 2.4 事件如何结束与清理（含 README“48 格 / 30～60 秒消失”核实）

| 清理对象 | 机制与精确数值 | 位置 |
| --- | --- | --- |
| 事件本身 | 跨天时 `synchronize` 重新判定并调用 `beginDay` 覆盖 `activeId`；无“结束回调”，只做天气回滚 | `DailyWorldEventManager.java:144-178,180-187` |
| 大雪天气 | 新事件不是 `HEAVY_SNOW` 且上一事件是 `HEAVY_SNOW` → `WeatherManager.reapplyVanillaWeather(overworld)` | `DailyWorldEventManager.java:180-187`、`WeatherManager.java:317-320` |
| 替代生物（核心） | `despawnFarReplacements`：每 100 tick（5 秒）执行一次（外层 20 tick 门 + `%100==0` 判断） | `DailyWorldEventHooks.java:83,358-383` |
| 判定半径 | `level.getNearestPlayer(mob, 48.0D) == null` 视为“远离玩家”，即 **48 格**（原版该方法按选择器排除旁观者，Mod 未额外过滤） | `DailyWorldEventHooks.java:371` |
| 宽限 | 首次判定为远时写入 `qys_event_far_since = 当前 gameTime`，本 tick 不消失 | `DailyWorldEventHooks.java:374-376` |
| 概率消失 | 距记录 ≥ **600 tick（30 秒）** 后每次检查（每 5 秒）**35%** 概率 `discard()` | `DailyWorldEventHooks.java:70,378` |
| 硬上限 | 距记录 ≥ **1200 tick（60 秒）** 必定 `discard()`（与随机无关） | `DailyWorldEventHooks.java:71,378` |
| 豁免 | 有持久化要求、有自定义名、被拴绳、已驯服的生物：清除计时并永不按此规则消失 | `DailyWorldEventHooks.java:367-370` |
| 玩家回到 48 格内 | 清除 `qys_event_far_since`，计时从 0 重新开始 | `DailyWorldEventHooks.java:371-373` |
| 索引结构 | 内存态 `Map<维度, Set<UUID>> REPLACEMENTS`（**不写存档**）；实体死亡或非 Mob 时从集合移除 | `DailyWorldEventHooks.java:73,305-307,366` |
| 重启后的影响 | 索引丢失，但实体 NBT 里的 `qys_event_replacement` 标记仍在；当实体再次加入世界（区块重载等）时会被 `EntityJoinLevelEvent` 重新登记（“旧标记接管”分支） | `DailyWorldEventHooks.java:117-122,296` |
| 收获倍率缓存 | `HARVESTS`（维度+坐标）在掉落实体生成后 3 game tick 过期；每秒 `cleanupHarvests` 清理 | `DailyWorldEventHooks.java:72,82,324-325,333-334,349-356` |
| 怪物 buff | 血月 buff 时长 24000 tick，自然到期 | `DailyWorldEventHooks.java:130-131` |
| 玩家 buff | 仅在事件生效期间周期刷新；跨天后不再刷新，最长 12 秒（幸运）/7 秒（缓降、跳跃）内自然消失 | `DailyWorldEventHooks.java:88-93` |
| 近处替代生物 | 事件结束后**不会**立刻消失：只要 48 格内有玩家就一直保留（这是当前实现的既有行为） | `DailyWorldEventHooks.java:371-373` |

---

## 3. 事件管理器与持久化

### 3.1 `DailyWorldEventData` 存了什么

| NBT 键 | 类型 | 含义 | 位置 |
| --- | --- | --- | --- |
| `CurrentDay` | long | 上次结算的游戏内天数（初始 `Long.MIN_VALUE`） | `DailyWorldEventData.java:19,32,43` |
| `ActiveEvent` | string | 当前事件 id；空串 = 未触发/无事件 | `DailyWorldEventData.java:20,33,44,68,72` |
| `ClaimedGifts` | string 列表 | `牢大受难日` 领奖记录，格式 `"<day>|<uuid>"`，按字典序输出 | `DailyWorldEventData.java:21,34-35,45-47,84-88` |
| `OracleConsultedDays` | compound（uuid→long） | 每名玩家最后一次查看预言机的天数（写入侧） | `DailyWorldEventData.java:22,36,48,90-93` |
| `OracleAlertedDays` | compound（uuid→long） | “次日预言应验提醒”已发放的天数（**当前无调用方，见 4.7**） | `DailyWorldEventData.java:23,37,49,95-102` |
| 存储 ID / 位置 | `qys_event_daily_world_events`，挂在**主世界** `getDataStorage()` 上 → `saves/<世界>/data/qys_event_daily_world_events.dat` | `DailyWorldEventData.java:18,25-28` |
| 落盘时机 | `setDirty()`：`beginDay` / `claimGift` / `recordOracleConsultation` / `claimOracleAlert` | `DailyWorldEventData.java:81,86,92,100` |

实例佐证（只读解压 `saves/新的世界 (2)/data/qys_event_daily_world_events.dat`，174 字节 gzip，解压后 207 字节 NBT）：
`data → OracleAlertedDays={} / ActiveEvent="gravity_glitch" / CurrentDay=4 / ClaimedGifts=[] / OracleConsultedDays={}`，并带 `DataVersion=3465`、`brewinandchewin:data_version`、`Railways_DataVersion` 三个外部 mod 写入的同文件附加键。与该存档当天处于 `失重故障` 事件一致。

### 3.2 `WeatherSavedData` 是否参与

| 问题 | 结论 | 位置 |
| --- | --- | --- |
| 是否保存每日事件 | **不保存**。它只保存天气状态：`LastProcessedDay/LastTerm/ActiveEvent(WeatherType)/ActiveEventDay/OrdinaryWeather/OrdinaryWeatherStreak/CooldownUntilDay/ForecastDay/ForecastTerm/ForecastOrdinaryWeather/ForecastEvent`，存储 ID `solarweather_state`，同样挂在主世界 | `WeatherSavedData.java:8,10-22,28-60` |
| 与每日事件的耦合点 1 | 天气每秒 `applyVanillaWeather` 时读取 `DailyWorldEventData.activeEvent()`，若为 `HEAVY_SNOW` 则强制 `rain=true, thunder=false` | `WeatherManager.java:309-313` |
| 与每日事件的耦合点 2 | 大雪结束/开始时的天气切换走每日事件侧的 `applyWeatherTransition` → `reapplyVanillaWeather` | `DailyWorldEventManager.java:180-187`、`WeatherManager.java:317-320` |
| 与每日事件的耦合点 3 | 客户端同步：每日事件随天气包一起下发，非主世界玩家下发 `daily=null`（所以事件的客户端粒子只在主世界出现） | `WeatherManager.java:83-99`、`WeatherSyncPacket.java:12-31`、`client/ClientWeatherState.java:34-38` |

### 3.3 重启后如何恢复

| 步骤 | 行为 | 位置 |
| --- | --- | --- |
| 读档 | `SavedData.load` 还原 `CurrentDay/ActiveEvent/ClaimedGifts/OracleConsultedDays/OracleAlertedDays` | `DailyWorldEventData.java:30-39` |
| 首次 tick | `tick` → `synchronize(server, true)`：若 `data.currentDay() == 今天` 则**保留存档中的事件继续生效**（不会重抽、不广播）；否则按规则重新判定并广播“今日世界事件…” | `DailyWorldEventManager.java:54-55,144-178` |
| 强制测试态 | `TestOverride` 只在内存 `WeakHashMap<MinecraftServer, TestOverride>`（**不持久化**）；重启后丢失，但当天的 `ActiveEvent` 仍留在存档里继续生效，直到当天结束 | `DailyWorldEventManager.java:34-35,132-142,147-152` |
| 替代生物索引 | 不持久化；重启后靠实体自带的 `qys_event_replacement` 标记在再次入世界时重新登记 | `DailyWorldEventHooks.java:73,117-122` |
| 收获倍率缓存 | 不持久化（3 tick 内瞬态） | `DailyWorldEventHooks.java:72` |

### 3.4 命令完整列表与参数

注册入口：`DailyWorldEventCommand.register`（`worldevent/DailyWorldEventCommand.java:24-33`），日志行 `:32`；类注册在 `QysEventMod.java:45`。

| 命令 | 子命令/参数 | 权限 | 行为 | 位置 |
| --- | --- | --- | --- | --- |
| `/qysevent event` | 无参数 | 等级 2 | 等同 `list` | `DailyWorldEventCommand.java:26-28,35-39` |
| `/qysevent event list` | — | 等级 2 | 打印测试头 `【世界事件测试】共 19 项｜当前：…`，随后 19 行 `[触发] 名称 [id]`（点击执行 `/ftevent trigger <id>`），最后 `[清除今日事件]`（点击执行 `/ftevent clear`）；返回 19 | `:39,63-92` |
| `/qysevent event trigger <event>` | `event`：word 参数，建议列表 = 19 个 id | 等级 2 | 命中 id 则强制今日事件并全服广播 `【事件测试】已触发：<名>（<id>）`；未命中则失败提示 `未知世界事件：…` 并返回 0 | `:40-44,94-107` |
| `/qysevent event clear` | — | 等级 2 | 清除今日强制事件（`setActiveForTesting(server, null)`），广播 `【事件测试】已清除今日事件；下一游戏日恢复正常判定。` | `:45,109-114` |
| `/qysevent status` | — | **无 requires，任何人可用** | 输出 `[世界事件] …` + `WeatherManager.statusLine`（当前普通/特殊天气） | `:28,49-57` |
| `/ftevent …` | 同 `event` 节点 | 等级 2 | 旧命令兼容入口（同一 builder） | `:29` |
| `/ftboss event …` | 同 `event` 节点 | 等级 2 | 更旧的兼容入口 | `:31` |
| 权限判定 | `canUse = source.hasPermission(2)`（**当前版本没有单人房主兜底**） | — | — | `:59-61` |
| 天气命令（另一文件） | `/qysevent weather status\|forecast\|start <type>\|stop\|reroll`，另有 `/qysweather`、`/solarweather`、`/weather <天气ID>` | `status` 无限制；`forecast/start/stop/reroll` 需等级 2 | 与每日事件无关，供文档区分 | `WeatherCommands.java:32-69,76-95` |

### 3.5 测试用覆盖（TestOverride）入口

| 入口 | 说明 | 位置 |
| --- | --- | --- |
| 设置 | `DailyWorldEventManager.setActiveForTesting(server, event)`：记录 `TestOverride(day(server), event)`，立即 `beginDay(today, event)` 并做天气切换与日志 `[QysEvent][DailyEventTest] …` | `DailyWorldEventManager.java:132-142` |
| 清除 | `setActiveForTesting(server, null)`：移除覆盖并 `beginDay(today, null)` | `DailyWorldEventManager.java:136-138` |
| 调用者 | 命令 `trigger` / `clear` | `DailyWorldEventCommand.java:102,110` |
| 生效方式 | `synchronize` 中若覆盖天数 == 今天，则强制使用该事件；**天数一变立即失效并移除** | `DailyWorldEventManager.java:147-158` |
| 持久化 | 无（内存 `WeakHashMap`，随服务器实例回收） | `DailyWorldEventManager.java:34-35` |

---

## 4. 预言机（`qys_event:oracle`）

### 4.1 方块本身与获取方式

| 项目 | 内容 | 位置 |
| --- | --- | --- |
| 注册 | 方块 `qys_event:oracle`（`OracleBlock extends HorizontalDirectionalBlock`，有 `facing` 属性）与同名 BlockItem | `ModBlocks.java:23-27`、`OracleBlock.java:27-33,70-73` |
| 方块属性 | `Properties.copy(AMETHYST_BLOCK)`：硬度 2.0、抗爆 6.0、音效 AMETHYST、`requiresCorrectToolForDrops()` | `ModBlocks.java:24-25` |
| 工作台配方 | 有形状合成 `"AGA"/"OCO"/" E "`：A=`minecraft:amethyst_shard`×4、G=`minecraft:gold_ingot`×1、O=`minecraft:crying_obsidian`×2、C=`minecraft:clock`×1、E=`minecraft:emerald_block`×1 → 产出 1 个 `qys_event:oracle` | `data/qys_event/recipes/oracle.json:1` |
| 挖掘掉落 | 掉落自身（`survives_explosion` 条件） | `data/qys_event/loot_tables/blocks/oracle.json:1` |
| 旧 id 迁移 | `ftboss:oracle`（方块与物品）在缺失映射事件中重映射到 `qys_event:oracle` | `QysEventMod.java:58-65` |
| 创造栏 | 出现在 `FUNCTIONAL_BLOCKS` 与 `REDSTONE_BLOCKS` 标签页 | `QysEventMod.java:68-74` |
| 标签 | 本 Mod 的 `mineable/pickaxe` 与 `needs_stone_tool` 只登记了 `qys_event:weather_forecaster`，**没有登记预言机** | `data/minecraft/tags/blocks/mineable/pickaxe.json:3`、`data/minecraft/tags/blocks/needs_stone_tool.json:3` |
| 物品 tooltip | `tooltip.qys_event.oracle` / `.cost` 两个键存在，但代码中**没有任何 Tooltip 注册/引用**，因此不会显示 | `lang/zh_cn.json:48-49`；`src/main/java` 内无 tooltip 相关代码 |

### 4.2 交互代价

| 项目 | 数值/行为 | 位置 |
| --- | --- | --- |
| 前置 | 交互手必须手持 `minecraft:emerald` | `OracleBlock.java:38-39` |
| 未持有时 | 服务端发 action bar 提示“预言机需要 1 颗绿宝石。”（`displayClientMessage(..., true)`），交互仍返回 SUCCESS | `OracleBlock.java:40-41`、`lang/zh_cn.json:50` |
| 消耗 | 非创造模式消耗 1 颗绿宝石（`held.shrink(1)`）；创造模式（`instabuild`）不消耗 | `OracleBlock.java:47` |
| 无冷却 | 代码中没有冷却检查（与许愿井的 40 tick 冷却不同） | `OracleBlock.java:36-53` 对比 `WishingWellBlock.java:47-49` |
| 表现 | 16 个 `PORTAL` 粒子（偏移 0.45/0.55/0.45，速度 0.06）+ `AMETHYST_BLOCK_CHIME`（方块音源，音量 0.8，音高 0.85） | `OracleBlock.java:48-50` |
| 记录 | 调用 `showForecast` 时会写入 `OracleConsultedDays[uuid] = data.currentDay()` | `DailyWorldEventManager.java:85`、`DailyWorldEventData.java:90-93` |

### 4.3 显示哪些天、如何抽样

| 项目 | 数值/行为 | 位置 |
| --- | --- | --- |
| 天数 | 方块写死 10 → 显示 **D+1 … D+10**（`forecast` 的 offset 从 1 开始；`forecast` 内部把天数夹到 1–30） | `OracleBlock.java:51`、`DailyWorldEventManager.java:71-81` |
| 未使用常量 | `DailyWorldEventRules.FORECAST_DAYS = 10` 定义了但**无任何引用**（只有方块里的字面量 10 生效） | `DailyWorldEventRules.java:5` |
| 输出顺序 | 先输出标题行“预言提醒：未来 10 天”（LIGHT_PURPLE+BOLD），再输出今日状态行（有事件=金色“今日事件已触发：<名>”，无事件=深灰“今日事件未触发”），然后 D+1…D+10 逐行 | `DailyWorldEventManager.java:83-95`、`lang/zh_cn.json:106-108` |
| 每行抽样 | 每行独立 `mode = player.getRandom().nextInt(100)`（`RandomSource` 为玩家自身随机源），按下面三段判定 | `DailyWorldEventManager.java:93,97-118` |
| 真值来源 | 该行对应天数的 `candidate(seed, day)` 与 `triggers(seed, day)`（确定性函数），因此“真话行”必然正确 | `DailyWorldEventManager.java:71-81`、`DailyWorldEventRules.java:35-50` |

### 4.4 三类文本的占比（README 的“错误事件/错误日期/错误概率/错乱文本”核实结果）

| 分支 | 占比 | 输出文本 | 颜色 | 位置 |
| --- | --- | --- | --- | --- |
| 准确 | **45%**（`ORACLE_CLEAR_PERCENT`） | 真触发 → `D+%s：预言已触发：%s`；真未触发 → `D+%s：预言未触发` | GRAY | `DailyWorldEventManager.java:31,99-103`、`lang/zh_cn.json:122-123` |
| 故意出错 | **28%**（`ORACLE_FALSE_PERCENT`） | ① 该天**确实**会触发 → 谎报“未触发”（即“错误日期/状态”）；② 该天**不会**触发 → 谎报“已触发”并给出**另一个事件**（`differentEvent`：在 enum 序上偏移 1–18 位取模，永远不会等于真事件） | DARK_AQUA | `DailyWorldEventManager.java:32,104-113,126-130`、`lang/zh_cn.json:122-123` |
| 错乱文本 | **27%**（剩余） | 从 `message.qys_event.oracle.glitch.0` … `.7` 中**均匀**取 1 条（`nextInt(8)`，各 12.5%）；偶数索引 DARK_PURPLE、奇数索引 DARK_GRAY | — | `DailyWorldEventManager.java:33,115-117`、`lang/zh_cn.json:110-117` |

8 条错乱文本（`lang/zh_cn.json:110-117`）：

| 索引 | 文本（zh_cn） |
| --- | --- |
| 0 | `D+%s：§k已触发：不存在的事件§r` |
| 1 | `D+%s：触发状态：？？？` |
| 2 | `D+%s：TRIGGER=NULL` |
| 3 | `D+%s：未触发 / 已触发` |
| 4 | `D+%s：▉▒░ 事件名丢失 ░▒▉` |
| 5 | `D+%s：已触发：并不存在的事件` |
| 6 | `D+%s：§k已触发：000000§r` |
| 7 | `D+%s：天气说已触发，猫说没有。` |

**需要更正 README 的地方**：代码里**没有**独立的“错误概率”分支——预言从不输出任何百分比/概率数字（唯一的数字文本是错乱 6 的 `000000`）。README 第 9 行所说的四类，在当前实现中实际只对应三个分支：45% 真话、28% 谎报（状态反转/换事件名，承担“错误事件 + 错误日期”）、27% 错乱文本。

### 4.5 准确率与“可否验证”

| 项目 | 结论 | 位置 |
| --- | --- | --- |
| 单行准确率 | 45%（真话分支）；被谎报或错乱的行占 55% | `DailyWorldEventManager.java:99-117` |
| 未来天数的可预测性 | 由于 `triggers`/`candidate` 是确定性函数，10 行理论上都可由种子精确算出；45% 的分支输出的就是真值 | `DailyWorldEventRules.java:35-50` |
| 今日行 | 使用存档 `ActiveEvent`（可能被 `/ftevent trigger` 强制过），与未来行规则无关 | `DailyWorldEventManager.java:86-92` |

### 4.6 与天气预报机的区别

| 维度 | 预言机 `qys_event:oracle` | 天气预报机 `qys_event:weather_forecaster` |
| --- | --- | --- |
| 预测对象 | **每日世界事件**（19 种） | **节气天气**：明日普通天气 + 明日特殊天气（32 种之一） |
| 天数 | 未来 10 天（D+1…D+10）+ 今日状态 | 仅明日 1 天 |
| 准确性 | 故意不可靠：45% 真 / 28% 假 / 27% 错乱 | **锁定并必然准确**：首次查询时把 `ForecastDay/ForecastOrdinaryWeather/ForecastEvent` 写入存档，次日 `tickDay` 直接采用该预报（`WeatherManager.java:146-148`，`tickDay` 定义见 `:134`） |
| 代价 | 1 绿宝石，创造免消耗，无冷却 | 1 绿宝石，创造免消耗，无冷却（`WeatherForecasterBlock.java:38`） |
| 提示文本 | `预言提醒：未来 10 天` + 10 行预言 | `§6【天气预报】` + `§6[天气预报] 普通天气：… | 特殊天气：…` |
| 未持宝石提示 | action bar“预言机需要 1 颗绿宝石。” | action bar“§a请手持一颗绿宝石右击天气预报机” |
| 表现 | PORTAL 粒子 16 个 + 紫水晶音（0.8/0.85） | HAPPY_VILLAGER 粒子 12 个 + 紫水晶音（1.0/1.15） |
| 代码位置 | `OracleBlock.java:36-53` | `WeatherForecasterBlock.java:24-48`、`WeatherManager.java:422-452`；配方 `data/qys_event/recipes/weather_forecaster.json` |
| 相关常量 | `FORECAST_DAYS=10`（未使用）、`ORACLE_*_PERCENT` | `SPECIAL_EVENT_CHANCE`（默认 0.15）、`SPECIAL_EVENT_COOLDOWN_DAYS`（默认 2） |
| 另一处对照 | 许愿井 30%–40% 分支显示的“井中预言”用的是 `WeatherManager.forecastTomorrow` + `DailyWorldEventManager.forecast(server, 1)`，**不经过预言机的 45/28/27 失真处理** | `WishingWellBlock.java:69-79` |

### 4.7 当前版本已失效的预言机相关代码（供文档避免误写）

| 项目 | 现状 | 位置 |
| --- | --- | --- |
| 次日“预言应验”提醒 | `claimOracleAlert(...)` 定义了但**没有任何调用方**；`DailyWorldEventManager` 中原来的 `notifyOracleRealization` 已不存在 → 不会再有“预言提醒：今日事件已触发：…”的自动提醒，也没有传送门粒子/紫水晶音 | `DailyWorldEventData.java:95-102`（无调用）；`lang/zh_cn.json:109` 该键当前无人使用 |
| 登录播报 | `onLogin` 现在只在事件是 `KOBE_MEMORIAL` 时补发冰红茶，不再发送任何“今日世界事件…”消息 → `message.qys_event.daily_event.login` 键当前无人使用 | `DailyWorldEventManager.java:66-69`；`lang/zh_cn.json:93` |
| `OracleConsultedDays` | 仍会被写入并持久化，但已无消费方（只有死代码 `claimOracleAlert` 读它） | `DailyWorldEventManager.java:85`、`DailyWorldEventData.java:22,90-93` |
| `OracleAlertedDays` | 仍会读写存档，但实际永远为空 | `DailyWorldEventData.java:23,37,49` |

---

## 5. 兼容与后备

### 5.1 兼容实现方式

| 项目 | 结论 | 位置 |
| --- | --- | --- |
| 判定方式 | **纯注册表查询**，没有任何 `ModList.get().isLoaded(...)` / 模组 id 判断：`optionalType(namespace, path, fallback)` 用 `ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(ns, path))`，取不到就用原版后备 | `DailyWorldEventHooks.java:287-290` |
| 效果兼容 | 同样按注册表取 `ftboss:poop_confusion`，取不到用 `minecraft:hunger` | `DailyWorldEventHooks.java:66,185-187` |
| 物品兼容 | 冰红茶按注册表取 `smc:ice_tea`，取不到用 `minecraft:honey_bottle` 改名 | `DailyWorldEventManager.java:30,192-194` |
| 旧标记接管 | 实体若带旧标记 `ftboss_daily_event_replacement`，会被当作替代生物接管并登记清理 | `DailyWorldEventHooks.java:67,117-122` |
| 依赖声明 | `mods.toml` 无 `ftboss`/`brewery`/`cataclysm` 依赖；只有 `forge`、`minecraft` 必选与 `sereneseasons` 可选 | `src/main/resources/META-INF/mods.toml:15-34` |

### 5.2 用到的外部实体/效果与精确后备

| 事件 | 触发条件（概率） | 外部 id（装了对应 Mod 时） | 注册表为空时的后备 | 位置 |
| --- | --- | --- | --- | --- |
| 胡闹厨房 | 12% | `ftboss:gingerbread_minion`（小姜饼人） | `minecraft:slime` | `DailyWorldEventHooks.java:215` |
| 胡闹厨房 | 4% | `brewery:beer_elemental`（[盛节精酿] letsdo-brewery 提供） | `minecraft:witch` | `DailyWorldEventHooks.java:216` |
| 胡闹厨房 | 3% | `ftboss:barrel_minion`（小酒桶） | `minecraft:creeper` | `DailyWorldEventHooks.java:217` |
| 胡闹厨房 | 5% | `ftboss:tea_minion`（茶芽小妖） | `minecraft:bee` | `DailyWorldEventHooks.java:218` |
| 大雪 | 16% | `ftboss:fly_minion`（苍蝇） | `minecraft:spider` | `DailyWorldEventHooks.java:235,245` |
| 大雪 | 13% | `ftboss:maggot_minion`（蛆） | `minecraft:silverfish` | `DailyWorldEventHooks.java:236,245` |
| 大雪 | 6% | `ftboss:poop_minion`（答辩小怪） | `minecraft:slime` | `DailyWorldEventHooks.java:237-238,245` |
| 大雪 | 4% | `ftboss:compost_slime`（棕黑史莱姆） | `minecraft:slime` | `DailyWorldEventHooks.java:237-238,245` |
| 大雪 | 2% | `ftboss:sheep_dung`（羊屎蛋） | `minecraft:slime` | `DailyWorldEventHooks.java:237-238,245` |
| 大雪 | 1.5% | `ftboss:apple_worm`（果虫） | `minecraft:silverfish` | `DailyWorldEventHooks.java:236,245` |
| 大雪 | 1.0% | `ftboss:apple_moth`（果蛾） | `minecraft:phantom` | `DailyWorldEventHooks.java:239,245` |
| 大雪 | 1.0% | `ftboss:rotten_apple`（烂苹果堆） | `minecraft:creeper` | `DailyWorldEventHooks.java:240,245` |
| 大雪 | 0.5% | `ftboss:giant_grub`（巨型果蛆，96 格内最多 1 只） | `minecraft:ravager`（同样受 96 格上限约束） | `DailyWorldEventHooks.java:241,245,248-249` |
| 大雪 · 玩家 debuff | 露天淋雨时每 20 tick | 效果 `ftboss:poop_confusion`（“屎？！！”，等级 I，80 tick） | 效果 `minecraft:hunger` **II 级**，80 tick | `DailyWorldEventHooks.java:66,185-187` |
| 大鱼洄游 | 1% | `cataclysm:amethyst_crab`（[灾变] L_Enders_Cataclysm 提供） | `minecraft:guardian` | `DailyWorldEventHooks.java:200` |
| 牢大受难日 | 每人每天 1 次 | 物品 `smc:ice_tea`（[森罗酒馆：世界名酒] kaleidoscope_world_liquor 提供） | 物品 `minecraft:honey_bottle` + 自定义名“冰红茶” | `DailyWorldEventManager.java:30,192-194` |
| 死灭洄游 / 猪 / 兔 / 女巫 / 史莱姆 / 蘑菇 / 末影蚀 | — | 不使用任何外部 id | 纯原版实体（`drowned/wither_skeleton/zombie/skeleton/pig/rabbit/witch/slime/magma_cube/mooshroom/enderman`） | `DailyWorldEventHooks.java:258-271,139,143-164` |

### 5.3 实例内的外部 id 佐证（只读，非源码引用）

| 外部 id | 提供者（实例 `mods/` 目录实到文件） | 佐证 |
| --- | --- | --- |
| `ftboss` 命名空间（`gingerbread_minion`/`barrel_minion`/`tea_minion`/`fly_minion`/`maggot_minion`/`poop_minion`/`compost_slime`/`sheep_dung`/`apple_worm`/`apple_moth`/`rotten_apple`/`giant_grub`，效果 `poop_confusion`） | `mods/qys-bosses.jar`（其 `META-INF/mods.toml` 中 `modId="ftboss"`，`version="1.0.0"`） | 该 jar 内 `assets/ftboss/lang/zh_cn.json` 含 `entity.ftboss.<上述 id>` 与 `effect.ftboss.poop_confusion`（“屎？！！”） |
| `brewery:beer_elemental` | `mods/[盛节精酿] letsdo-brewery-forge-2.0.6.jar` | 存在 `assets/brewery/models/item/beer_elemental_spawn_egg.json` 等资源 |
| `cataclysm:amethyst_crab` | `mods/[灾变]L_Enders_Cataclysm-3.31.jar` | 存在 `assets/cataclysm/textures/entity/amethyst_crab.png` 等资源 |
| `smc:ice_tea` | `mods/[森罗酒馆：世界名酒]kaleidoscope_world_liquor-1.1.6-forge+mc1.20.1-all.jar` | 存在 `assets/smc/models/item/ice_tea.json`、`assets/smc/lang/zh_cn.json` |

---

## 6. 写正式文档时的注意事项（代码 vs 文案差异清单）

| # | 文案/README 说法 | 当前代码事实 | 位置 |
| --- | --- | --- | --- |
| 1 | README 第 9 行“错误事件、错误日期、错误概率和错乱文本” | 只有 3 个分支：45% 真话、28% 谎报（状态反转或换事件名）、27% 错乱；**没有任何输出概率数字的分支** | `DailyWorldEventManager.java:97-118` |
| 2 | lang 描述“夜间出现紫色空间噪点 / 夜晚漂浮微光 / 夜空洒落星屑” | 客户端粒子**没有昼夜判定**，只要求“能看到天空 + 主世界 + 玩家处在（特殊天气或每日事件）下” | `client/ClientWeatherParticles.java:40-63`、`WeatherManager.java:89` |
| 3 | lang 描述“今天的日常生物生成” | 实际只覆盖 `NATURAL/CHUNK_GENERATION/PATROL/REINFORCEMENT` 与刷怪笼；刷怪蛋、命令、结构生成、繁殖、EVENT 召唤都不受影响 | `DailyWorldEventHooks.java:169-176` |
| 4 | lang 描述“自然生成的苦力怕会骑乘幻翼” | 代码用的是 `DAILY_SPAWN` 标记，**刷怪笼产出的苦力怕也会被装上幻翼** | `DailyWorldEventHooks.java:137,169-171,273-285` |
| 5 | README 第 21 行“刷怪笼不会产生这些污雨替代怪” | 正确：大雪替换额外要求 `WILD_SPAWN`（刷怪笼只算 `DAILY_SPAWN`） | `DailyWorldEventHooks.java:107-110,226` |
| 6 | README 第 13 行“远离玩家 48 格…30～60 秒内逐渐消失” | 正确，精确数值：48 格 / 宽限 600 tick(30 s) / 每 100 tick(5 s) 检查 35% 概率 / 1200 tick(60 s) 硬消失 | `DailyWorldEventHooks.java:70-71,83,371,378` |
| 7 | README 第 8 行“10% 每日触发规则” | 正确（`0.10D`），但为**确定性哈希**且**不可配置** | `DailyWorldEventRules.java:4,48-50` |
| 8 | `DailyWorldEventRules.FORECAST_DAYS` | 定义但未使用（方块里写死 10） | `DailyWorldEventRules.java:5`、`OracleBlock.java:51` |
| 9 | 日常事件与特殊天气同名 | 每日事件 `史莱姆雨(slime_rain)` / `丰收月(harvest_moon)` / `猩红月潮(blood_moon)` / `萤火之夜(firefly_night)` 与特殊天气 `史莱姆从天而降(slime_rain)` / `丰收月(harvest_moon)` / `血月(blood_moon)` / `萤火之夜(firefly_night)` **是两套独立系统**，id 有重名；天降史莱姆属于特殊天气而非每日事件 | 每日事件：`lang/zh_cn.json:55-92`；特殊天气：`lang/zh_cn.json:6-45`、`WeatherType.java:7-12`、`WeatherManager.java:79,805-825` |
| 10 | 预言机 tooltip 文案 | 两个 tooltip 键存在但无代码引用（不会显示） | `lang/zh_cn.json:48-49` |
| 11 | “预言机/次日应验提醒” | 相关方法已被移除/无调用方：不会有登录播报，也不会有次日自动提醒 | `DailyWorldEventManager.java:66-69`、`DailyWorldEventData.java:95-102` |
| 12 | 权限 | 事件测试命令只要等级 2（无单人房主兜底）；`/qysevent status` 无权限要求 | `DailyWorldEventCommand.java:28,59-61` |
| 13 | 预言机是否在工具标签内 | 本 Mod 只把天气预报机写进 `mineable/pickaxe` 与 `needs_stone_tool`，预言机不在其中 | `data/minecraft/tags/blocks/mineable/pickaxe.json:3`、`data/minecraft/tags/blocks/needs_stone_tool.json:3` |

---

## 7. 附：全部事件 id 与 enum 序号（供文档做表）

| enum 序号 | id | 中文名 | zh_cn 行 |
| --- | --- | --- | --- |
| 0 | `big_fish_migration` | 大鱼洄游 | 55-56 |
| 1 | `kitchen_chaos` | 胡闹厨房 | 57-58 |
| 2 | `no_such_creature` | 根本没有这样的生物！ | 59-60 |
| 3 | `undead_migration` | 死灭洄游 | 61-62 |
| 4 | `pig_migration` | 死猪回游 | 63-64 |
| 5 | `heavy_snow` | 大雪 | 65-66 |
| 6 | `kobe_memorial` | 牢大受难日 | 67-68 |
| 7 | `harvest_moon` | 丰收月 | 69-70 |
| 8 | `slime_rain` | 史莱姆雨 | 71-72 |
| 9 | `starfall` | 星落夜 | 91-92 |
| 10 | `blood_moon` | 猩红月潮 | 73-74 |
| 11 | `gold_rush` | 镀金矿脉 | 75-76 |
| 12 | `rabbit_tide` | 兔潮失控 | 77-78 |
| 13 | `witch_convention` | 女巫茶会 | 79-80 |
| 14 | `gravity_glitch` | 失重故障 | 81-82 |
| 15 | `mushroom_bloom` | 蘑菇泛滥 | 83-84 |
| 16 | `ender_eclipse` | 末影蚀 | 85-86 |
| 17 | `firefly_night` | 萤火之夜 | 87-88 |
| 18 | `lucky_day` | 好运日 | 89-90 |

（enum 声明顺序：`worldevent/DailyWorldEvent.java:9-27`。该序号顺序同时决定 `differentEvent` 的“换名偏移”与预言机谎报时可出现的错误事件名范围：`DailyWorldEventManager.java:126-130`。）
