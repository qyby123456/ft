# qys_event 天气系统代码研究报告

- 工程根：`_work/qys-event`（Mod ID `qys_event`，包名 `cn.farmingtales.qysevent`，版本 `1.0.2`，`src/main/resources/META-INF/mods.toml:6-7`）
- 报告范围：节气系统、天气类型全表、天气调度、实际效果、客户端表现与性能、配置项
- 全部结论基于当前代码；中文名全部来自 `src/main/resources/assets/qys_event/lang/zh_cn.json`，并已与 `WeatherType.java` / `OrdinaryWeatherType.java` 内的硬编码中文名交叉核对（两者一致）
- 路径约定：下文所有 `文件:行号` 均相对于工程根 `_work/qys-event/`

---

## 0. 关键常量与总览

| 常量 | 值 | 来源 |
| --- | --- | --- |
| 一个游戏日 | 24000 tick（20 分钟） | `src/main/java/cn/farmingtales/qysevent/WeatherManager.java:49` |
| 一个节气（无 Serene Seasons） | 4 游戏日 = 96000 tick = 80 分钟 | `src/main/java/cn/farmingtales/qysevent/SolarTerms.java:70-71` |
| 一个节气（有 Serene Seasons） | `cycleTicks / 24`，`cycle` = `getCycleDuration()`，`dayDuration` = `getDayDuration()` | `src/main/java/cn/farmingtales/qysevent/SolarTerms.java:62,75-82` |
| 特殊天气枚举总数 | 33 = 32 种特殊天气 + `NONE` | `src/main/java/cn/farmingtales/qysevent/WeatherType.java:6-44`，`src/test/java/cn/farmingtales/qysevent/QysEventVerifier.java:58` |
| 普通天气枚举总数 | 8 | `src/main/java/cn/farmingtales/qysevent/OrdinaryWeatherType.java:10-17` |
| 昼夜相位分界 | `t < 12500` 为白天，`t >= 12500` 为夜晚（`t = dayTime % 24000`） | `src/main/java/cn/farmingtales/qysevent/WeatherType.java:86-89` |
| 天气主循环频率 | 每 20 tick（1 秒）一次 | `src/main/java/cn/farmingtales/qysevent/WeatherManager.java:63` |
| 有益/破坏地形脉冲 | 每 100 tick（5 秒）一次 | `src/main/java/cn/farmingtales/qysevent/WeatherManager.java:70` |
| 史莱姆雨生成脉冲 | 每 200 tick（10 秒）一次 | `src/main/java/cn/farmingtales/qysevent/WeatherManager.java:78-81` |
| actionbar 提醒 | 每 200 tick（10 秒）一次 | `src/main/java/cn/farmingtales/qysevent/WeatherManager.java:68` |
| 网络包协议版本 | `"2"`，仅注册 1 个包（id 0，PLAY_TO_CLIENT） | `src/main/java/cn/farmingtales/qysevent/WeatherNetwork.java:14,23-30` |

---

## 1. 节气系统

### 1.1 二十四节气名称表

`SolarTerms.NAMES` 按顺序硬编码（`src/main/java/cn/farmingtales/qysevent/SolarTerms.java:7-12`）：

| 索引 | 名称 | 季段（`seasonForTerm`） | 索引 | 名称 | 季段 |
| --- | --- | --- | --- | --- | --- |
| 0 | 立春 | 春季 | 12 | 立秋 | 秋季 |
| 1 | 雨水 | 春季 | 13 | 处暑 | 秋季 |
| 2 | 惊蛰 | 春季 | 14 | 白露 | 秋季 |
| 3 | 春分 | 春季 | 15 | 秋分 | 秋季 |
| 4 | 清明 | 春季 | 16 | 寒露 | 秋季 |
| 5 | 谷雨 | 春季 | 17 | 霜降 | 秋季 |
| 6 | 立夏 | 夏季 | 18 | 立冬 | 冬季 |
| 7 | 小满 | 夏季 | 19 | 小雪 | 冬季 |
| 8 | 芒种 | 夏季 | 20 | 大雪 | 冬季 |
| 9 | 夏至 | 夏季 | 21 | 冬至 | 冬季 |
| 10 | 小暑 | 夏季 | 22 | 小寒 | 冬季 |
| 11 | 大暑 | 夏季 | 23 | 大寒 | 冬季 |

季段划分：`normalized < 6` 春季，`< 12` 夏季，`< 18` 秋季，否则冬季（`src/main/java/cn/farmingtales/qysevent/SolarTerms.java:44-50`）。季段中文名为「春季/夏季/秋季/冬季」（`src/main/java/cn/farmingtales/qysevent/SolarTerms.java:29`）。

### 1.2 驱动方式：游戏内天数，不是现实日期

- 判定入口：`SolarTerms.current(level)` → `atDayOffset(level, 0)`（`src/main/java/cn/farmingtales/qysevent/SolarTerms.java:52-54`）。
- **回落路径（无 Serene Seasons 或反射失败）**：完全由游戏内 `level.getDayTime()` 决定，与现实日期、系统时钟无关：
  - `absoluteDay = floorDiv(dayTime, 24000) + offsetDays`（`src/main/java/cn/farmingtales/qysevent/SolarTerms.java:69`）
  - `termIndex = floorMod(absoluteDay / 4, 24)`，即**每 4 个游戏日推进一个节气**，24 个节气 = 96 游戏日 = 2,304,000 tick = 32 小时现实时间（`src/main/java/cn/farmingtales/qysevent/SolarTerms.java:70`）
  - `dayInTerm = absoluteDay % 4 + 1`（第 1~4 天），`termLengthDays = 4`（`src/main/java/cn/farmingtales/qysevent/SolarTerms.java:71`）
  - 注意：该路径直接使用 `getDayTime()`（含当天时间模 24000 后的整日推进），不使用 `getDayTime()/24000` 之外的任何日历/月相/现实时间来源。

### 1.3 Serene Seasons 兼容逻辑

采用**纯反射、软依赖**（`src/main/resources/META-INF/mods.toml:29-33` 声明 `sereneseasons` 为 `mandatory=false`、`versionRange="[9.1.0.3,10)"`、`ordering="AFTER"`）：

```
1) Class.forName("sereneseasons.api.season.SeasonHelper")
2) SeasonHelper.getSeasonState(Level)  -> ISeasonState
3) ISeasonState.getCycleDuration()     -> cycle      （下限保护 max(24, ...)）
4) ISeasonState.getDayDuration()       -> dayDuration（下限保护 max(1, ...)）
5) ISeasonState.getSeasonCycleTicks()  -> 与 offsetDays * dayDuration 相加后 floorMod cycle
6) fromCycle(cycleTicks, cycle, dayDuration) -> TermInfo
```
（`src/main/java/cn/farmingtales/qysevent/SolarTerms.java:57-67`）

映射公式（`src/main/java/cn/farmingtales/qysevent/SolarTerms.java:75-82`）：

| 字段 | 公式 |
| --- | --- |
| `index` | `min(23, cycleTicks * 24 / cycle)` |
| `termStart` / `termEnd` | `index * cycle / 24` / `(index+1) * cycle / 24` |
| `dayInTerm` | `max(1, (cycleTicks - termStart) / dayDuration + 1)` |
| `termLengthDays` | `max(1, (termEnd - termStart + dayDuration - 1) / dayDuration)` |

即装了 Serene Seasons 后，节气的长度和起算点由该 Mod 的季节周期决定（sub-season 区间等分 24 份），不再是固定 4 天。任何 `Throwable`（类不存在、方法签名变化、`InvocationTargetException` 等）都会静默回落到 1.2 的 4 天/节气公式（`src/main/java/cn/farmingtales/qysevent/SolarTerms.java:68`）。

### 1.4 「今天是什么节气」的判定与使用点

| 使用点 | 行为 | 来源 |
| --- | --- | --- |
| `tickDay` | 每天首次 tick 时取 `SolarTerms.current(level)`，与 `data.lastTerm` 比较得出 `termChanged` | `src/main/java/cn/farmingtales/qysevent/WeatherManager.java:140-141` |
| 节气播报 | `termChanged && ANNOUNCE_SOLAR_TERMS` 时广播 `§6[二十四节气] §e今日进入：§f§l<名称>` | `src/main/java/cn/farmingtales/qysevent/WeatherManager.java:165-167` |
| 普通天气抽取 | `chooseOrdinaryWeather(random, term.index())` 使用当天的节气索引 | `src/main/java/cn/farmingtales/qysevent/WeatherManager.java:151,172-201` |
| 特殊天气抽取 | `rollSpecialEvent(random, term.index())`，用 `seasonForTerm(term)` 取季段；冬季额外用 `term == 21`（冬至）解锁圣诞夜 | `src/main/java/cn/farmingtales/qysevent/WeatherManager.java:152-154,213,270` |
| 预报 | `SolarTerms.atDayOffset(level, 1)` 取「明天」的节气并存入 `forecastTerm` | `src/main/java/cn/farmingtales/qysevent/WeatherManager.java:426,430` |

- 节气变化**不改变**已抽到的当天天气，只影响下一次抽取（每天只在跨日时抽一次）。
- `TermInfo.dayInTerm` / `termLengthDays` 被记录但当前代码中除预报结构体外没有任何展示或判定使用（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:53-54,445-451` 的 `forecastLine` 只输出普通/特殊天气）。
- 节气的雨/雷概率表：`RAIN_CHANCE` 24 项 `.28 .43 .48 .46 .40 .52 / .38 .42 .46 .34 .39 .43 / .34 .31 .28 .25 .23 .21 / .20 .25 .32 .30 .23 .18`（`src/main/java/cn/farmingtales/qysevent/SolarTerms.java:14-19`）；`THUNDER_CHANCE` 24 项 `.01 .02 .04 .05 .04 .06 / .08 .11 .14 .16 .18 .20 / .13 .10 .07 .04 .03 .02 / .01 .01 .01 .01 .01 .01`（`src/main/java/cn/farmingtales/qysevent/SolarTerms.java:21-26`）。

---

## 2. 天气类型全表

### 2.1 普通天气（`OrdinaryWeatherType`，8 种）

构造参数顺序：`id, 中文名, rain, thunder, neutral, description`（`src/main/java/cn/farmingtales/qysevent/OrdinaryWeatherType.java:26-33`）。`neutral=true` 表示该普通天气不参与产量倍率计算（倍率固定 1.0）。

| # | ID | 中文名（lang） | lang 键 | 原版降水 | 雷 | neutral | 产量倍率（连击 < `diminishingStreakStart`，默认 3） | 降水着色 RGB | 来源 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | `clear` | 晴天 | `qys_event.ordinary.clear` | 否 | 否 | 否 | **1.15** | `0xFFFFFF`（无色偏） | `OrdinaryWeatherType.java:10,48`；`WeatherManager.java:413` |
| 2 | `rain` | 雨天 | `qys_event.ordinary.rain` | 是 | 否 | 否 | **1.25** | `0xA9C7E8` | `OrdinaryWeatherType.java:11,44`；`WeatherManager.java:411` |
| 3 | `thunder` | 原版雷雨 | `qys_event.ordinary.thunder` | 是 | 是 | 否 | **1.18** | `0x7085A8` | `OrdinaryWeatherType.java:12,45`；`WeatherManager.java:412` |
| 4 | `cloudy` | 多云 | `qys_event.ordinary.cloudy` | 否 | 否 | 是 | 1.00 | `0xFFFFFF` | `OrdinaryWeatherType.java:13,48` |
| 5 | `overcast` | 阴天 | `qys_event.ordinary.overcast` | 否 | 否 | 是 | 1.00 | `0xFFFFFF` | `OrdinaryWeatherType.java:14,48` |
| 6 | `drizzle` | 毛毛雨 | `qys_event.ordinary.drizzle` | 是 | 否 | 是 | 1.00 | `0xC6DDEA` | `OrdinaryWeatherType.java:15,46` |
| 7 | `breeze` | 微风 | `qys_event.ordinary.breeze` | 否 | 否 | 是 | 1.00 | `0xFFFFFF` | `OrdinaryWeatherType.java:16,48` |
| 8 | `sunshower` | 太阳雨 | `qys_event.ordinary.sunshower` | 是 | 否 | 是 | 1.00 | `0xE2CF86` | `OrdinaryWeatherType.java:17,47` |

- 中文名同时硬编码在枚举中，与 lang 文案完全一致；`byId` 只接受 `id` 或枚举名小写，**不接受中文名**，未知值回落为 `clear`（`src/main/java/cn/farmingtales/qysevent/OrdinaryWeatherType.java:52-64`）。
- "无额外效果"的四项（cloudy/overcast/breeze）以及 drizzle/sunshower 之所以 `neutral`，是因为它们无独立玩法修改，只做不同的降水状态映射（原版 `setWeatherParameters` 只有晴/雨/雷三种状态）。
- 普通天气**没有持续时间上限/下限字段**：它每天由节气抽一次并持续到下一个跨日；唯一的"时长"是原版雨计时器被每 20 tick 覆写为 24000 tick（见 §3.4）。

### 2.2 特殊天气（`WeatherType`，32 种）

枚举构造参数：`id, 中文名, Phase, yieldMultiplier`（`src/main/java/cn/farmingtales/qysevent/WeatherType.java:53-58`）。

- `Phase`：`DAY`（当天时间 < 12500 tick 才生效）、`NIGHT`（≥ 12500 tick 才生效）、`ALL`（全天）（`src/main/java/cn/farmingtales/qysevent/WeatherType.java:46,86-89`）。
- 「生效窗口」=`activeEventDay == 当天` **且** 相位命中；相位不命中时该天气当天完全"沉默"（不生效、不同步客户端、不播报，`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:387-390`）。
- 「出现权重」来自 `rollSpecialEvent` 的加权池，四个季节权重总和分别为 **春 115 / 夏 123 / 秋 95 / 冬 95**（含 6 项全年项）（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:211-288`）。概率列 = 权重/当季总和。多季天气的列内为「季节(权重/概率)」。

**全年共通项（4 个季节都会加入权重池）**

| ID | 中文名 | 相位 | 首日晚间窗口 | 产量倍率 | 降水着色 | 全年权重 | 春夏秋冬概率 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `blood_moon` | 血月 | NIGHT | 12500→24000 | 0.72（−28%） | 默认白（无覆盖） | 3 | 2.609% / 2.439% / 3.158% / 3.158% |
| `solar_eclipse` | 日食 | DAY | 0→12500 | 0.78（−22%） | 默认白 | 夏季 4，其他 2 | 1.739% / 3.252% / 2.105% / 2.105% |
| `meteor_shower` | 流星雨 | NIGHT | 12500→24000 | 1.20（+20%） | 默认白 | 4 | 3.478% / 3.252% / 4.211% / 4.211% |
| `comet_night` | 彗星夜 | NIGHT | 12500→24000 | 1.25（+25%） | 默认白 | 2 | 1.739% / 1.626% / 2.105% / 2.105% |
| `star_dew` | 星露夜 | NIGHT | 12500→24000 | 1.50（+50%） | 默认白 | 3 | 2.609% / 2.439% / 3.158% / 3.158% |
| `firefly_night` | 萤火之夜 | NIGHT | 12500→24000 | 1.18（+18%） | 默认白 | 4 | 3.478% / 3.252% / 4.211% / 4.211% |

> 上表首行 `blood_moon` 的说明：全年项共 6 项，已在表中逐行列出（`WeatherManager.java:217-222`）。HARVEST_MOON 在 `DailyWorldEvent` 中也存在同名日常事件，但天气枚举里的 `HARVEST_MOON` 只属于秋季权重池（`WeatherManager.java:257`）。

**季节限定项与权重/概率**

| ID | 中文名 | 相位 | 产量倍率 | 降水着色 | 春季 | 夏季 | 秋季 | 冬季 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `harvest_moon` | 丰收月 | NIGHT | 2.00（+100%） | 默认白 | — | — | 14 / **14.737%** | — |
| `christmas_eve` | 圣诞夜 | NIGHT | 1.50（+50%） | `0xDCEEFF` | — | — | — | 20 / **21.053%**（仅 `term == 21` 冬至） |
| `slime_rain` | 史莱姆从天而降 | ALL | 0.92（−8%） | `0x79C95A` | 6 / 5.217% | 5 / 4.065% | — | — |
| `blizzard` | 暴风雪 | ALL | 0.52（−48%） | `0xFFFFFF` | — | — | — | 13 / 13.684% |
| `hurricane` | 飓风天 | ALL | 0.42（−58%） | `0x718A9B` | — | 9 / 7.317% | 5 / 5.263% | — |
| `sandstorm` | 沙尘暴 | ALL | 0.55（−45%） | 默认白 | 5 / 4.348% | 4 / 3.252% | — | — |
| `thunderstorm` | 雷雨 | ALL | 0.68（−32%） | `0x687DA8` | 6 / 5.217% | 13 / **10.569%** | 4 / 4.211% | — |
| `redstone_storm` | 红石雷暴 | ALL | 0.78（−22%） | `0xCF3B35` | 4 / 3.478% | 8 / 6.504% | 5 / 5.263% | — |
| `green_rain` | 绿雨 | ALL | 1.75（+75%） | `0x58B957` | 8 / 6.957% | 6 / 4.878% | — | — |
| `aurora` | 极光夜 | NIGHT | 1.35（+35%） | 默认白 | — | — | 4 / 4.211% | 12 / 12.632% |
| `heatwave` | 灼热热浪 | ALL | 0.50（−50%） | 默认白 | — | 12 / 9.756% | — | — |
| `dense_fog` | 迷途浓雾 | ALL | 0.88（−12%） | 默认白 | 6 / 5.217% | — | 8 / 8.421% | 5 / 5.263% |
| `rainbow_rain` | 彩虹雨 | ALL | 1.60（+60%） | `0xC56DDB` | 7 / 6.087% | 5 / 4.065% | — | — |
| `pollen_storm` | 花粉风暴 | ALL | 1.32（+32%） | 默认白 | 12 / **10.435%** | — | — | — |
| `spring_bloom` | 春日花潮 | ALL | 1.45（+45%） | 默认白 | 10 / 8.696% | — | — | — |
| `hailstorm` | 冰雹 | ALL | 0.72（−28%） | `0xC4E8F2` | 7 / 6.087% | 5 / 4.065% | — | — |
| `monsoon` | 季风暴雨 | ALL | 1.12（+12%） | `0x477AB8` | — | 10 / 8.130% | — | — |
| `golden_wind` | 金风送爽 | ALL | 1.30（+30%） | 默认白 | — | — | 11 / 11.579% | — |
| `frost_night` | 霜夜 | NIGHT | 0.82（−18%） | 默认白 | — | — | 7 / 7.368% | 6 / 6.316% |
| `silent_snow` | 静雪 | ALL | 1.10（+10%） | `0xD4E7F0` | — | — | — | 11 / 11.579% |
| `acid_rain` | 酸雨 | ALL | 0.60（−40%） | `0xA0D33C` | 5 / 4.348% | 7 / 5.691% | — | — |
| `ash_fall` | 灰烬飘落 | ALL | 0.70（−30%） | 默认白 | — | 4 / 3.252% | 6 / 6.316% | — |
| `thundersnow` | 雷雪 | ALL | 0.58（−42%） | `0x83C7DE` | — | — | — | 10 / 10.526% |
| `misty_drizzle` | 烟雨 | ALL | 1.28（+28%） | `0x9CB8C2` | 11 / 9.565% | — | 8 / 8.421% | — |
| `dry_lightning` | 干雷 | ALL | 0.55（−45%） | 默认白 | — | 8 / 6.504% | 5 / 5.263% | — |
| `bee_flight` | 蜂舞晴日 | DAY | 1.42（+42%） | 默认白 | 10 / 8.696% | 7 / 5.691% | — | — |
| `none` | 平稳天气 | ALL | 1.00 | `0xFFFFFF` | — | — | — | — |

- **`christmas_eve` 的特殊门槛**：仅当 `seasonForTerm(term) == WINTER` **且** `term == 21`（冬至）时才被加入池（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:270`）。因此每个节气年只有 4 天（冬至那 4 天）可能抽到圣诞夜。
- `WeatherManager.seasonDescription` 与上表季节池**不完全一致**（例如它把 `sandstorm` 写作"春夏"、`dense_fog` 写作"春秋冬"、`hurricane` 写作"夏秋"），该方法是给展示/文档用的描述文本（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:325-343`）；**实际抽取规则以上表的 `switch (season)` 为准**。
- `WeatherType.byId` 支持三种写法：`id`、枚举名小写、**中文名精确匹配**（`src/main/java/cn/farmingtales/qysevent/WeatherType.java:91-100`）。
- `NONE` 的相位为 `ALL`，永远 `isActiveAt == true`（`WeatherType.java:6,86-89`），但在 `isEventActiveNow` 中被 `!= NONE` 短路排除。

### 2.3 每种特殊天气的实际效果（逐条）

触发频率说明：玩家增益/减益每 **20 tick（1 秒）** 施加一次（`WeatherManager.java:63`）；括号内为时长与等级（`MobEffectInstance(effect, durationTick, amplifier, ambient=true, visible=false)`，即**不显示药水粒子图标**）。所有玩家效果**仅对露天玩家生效**，例外是 `BLOOD_MOON` / `SOLAR_ECLIPSE` / `DENSE_FOG` 三项对室内玩家也生效（`WeatherManager.java:482-483`）。

| # | 天气 | 玩家效果（每秒刷新） | 作物/方块效果 | 经验/其他 | 粒子（客户端） | 银幕滤镜 |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 丰收月 harvest_moon | 幸运 I（60t） | 收获倍率 ×2.00（`WeatherType.java:7`） | — | GLOW 环境，1 个/4t，半径 8，高 4，速度 .006（`ClientWeatherParticles.java:79`） | 无 |
| 2 | 圣诞夜 christmas_eve | 幸运 **II**（60t）+ 再生 I（60t）+ 抗性提升 I（60t） | 收获倍率 ×1.50 | 原版雨/雪着色 `0xDCEEFF` | SNOWFLAKE 下落，2 个/4t，半径 10，高 5，速度 .025（`:67`） | 无 |
| 3 | 血月 blood_moon | 虚弱 I（60t）；每 40 tick 给 **28 格**内所有 `Monster` 力量 I + 速度 I（各 100t） | 作物破坏率 8%/脉冲，收获倍率 ×0.72 | — | CRIMSON_SPORE 下落，1 个/4t，半径 9，高 4，速度 .012（`:80`） | 有：`0x24A81824`(上)→`0x30C0252F`(下)，脉动 |
| 4 | 日食 solar_eclipse | 挖掘疲劳 I（60t）（**室内也生效**） | 作物破坏率 8%/脉冲，收获倍率 ×0.78 | — | ASH 下落，1 个/4t，半径 10，高 5，速度 .012（`:81`） | 有：`0x3A000000`→`0x32000000`，不脉动 |
| 5 | 流星雨 meteor_shower | 幸运 I（60t） | 收获倍率 ×1.20 | XP ×1.50（向上取整，至少 1）；每 100 tick 每露天玩家 3.5% 触发陨石爆炸（威力 1.35，`destructiveWeather=false` 时为 `NONE` 交互），其中再 6% 概率坠落含矿方块 | END_ROD 下落，38%/4t，1 个，半径 14，高 14，速度 .22（`:82-84`） | 无 |
| 6 | 史莱姆从天而降 slime_rain | 跳跃提升 I（60t） | 作物破坏率 8%/脉冲，收获倍率 ×0.92 | 每 200 tick 每露天玩家 68% 概率在头顶 18–25 格生成 1–2 只史莱姆（尺寸 1–3），28×24×28 内数量 ≥ `slimeCapPerPlayer`(16) 则跳过（`WeatherManager.java:811-831`） | **无**（用真实坠落史莱姆代替） | 无 |
| 7 | 暴风雪 blizzard | 缓慢 **II**（60t）+ 虚弱 I（60t）+ 每 tick `setTicksFrozen += 8`（上限 `需冻结tick+20`） | 玻璃破坏 6%、作物破坏 30%、土地变泥土 8%/脉冲；收获倍率 ×0.52 | 原版雪着色保持纯白（`Verifier:60` 断言） | SNOWFLAKE 下落，4 个/4t，半径 12，高 7，速度 .055（`:68`） | 无 |
| 8 | 飓风天 hurricane | 缓慢 I（40t）；每 20 tick 推动 18×8×18 内所有露天生物（`push(cos(t*.045)*.16, 落地?0.03:0.01, sin(t*.045)*.16)`，`hurtMarked=true`） | 玻璃破坏 **24%**、作物破坏 **38%**、农田变泥土 16%、草方块/泥土变砂土 16%/脉冲；收获倍率 ×0.42 | — | 默认（无专属粒子） | 无 |
| 9 | 沙尘暴 sandstorm | 缓慢 I（60t） | 玻璃破坏 6%、作物破坏 **34%**、农田变泥土 22%、草/泥土变砂土 22%/脉冲；收获倍率 ×0.55 | — | `FALLING_DUST`+沙子方块粒子，2 个/4t，半径 12，高 5，速度 .045（`:29-30,85`） | 有：`0x1EC99B63`→`0x2CB87338`，脉动 |
| 10 | 雷雨 thunderstorm | 无玩家 debuff | 玻璃破坏 6%、作物破坏 18%、农田变泥土 6%/脉冲；收获倍率 ×0.68 | 每 100 tick 每露天玩家 **5.5%** 概率在 33×33 内落雷（`destructiveWeather=false` 时 `setVisualOnly(true)`）（`WeatherManager.java:734,744-754`） | 默认（无色偏为白；有降水但无自定义粒子） | 无 |
| 11 | 红石雷暴 redstone_storm | 无玩家 debuff | 玻璃破坏 4%、作物破坏 14%、农田变泥土 5%/脉冲；收获倍率 ×0.78 | 落雷 6.5%/脉冲；另有 **20%/脉冲** 概率把 20×10×20 内一只露天未充能苦力怕充能（每次最多 1 只，`WeatherManager.java:741-743,770-782`） | 默认 | 无 |
| 12 | 绿雨 green_rain | 再生 I（60t） | 有益脉冲催熟概率 **22%**；把范围内农田 `moisture` 直接设为 7；收获倍率 ×1.75 | — | 绿色降水着色 `0x58B957` | 有：`0x1C38A850`→`0x2A309048`，脉动 |
| 13 | 极光夜 aurora | 夜视（260t）+ 幸运 I（60t）+ 缓降 I（60t） | 收获倍率 ×1.35 | **XP ×2.00** | WAX_ON 环境 1 个/4t（半径 11，高 6，速度 .008），另有 50% 概率 ENCHANT 1 个（半径 9，高 5，速度 .012）（`:86-89`） | 有：`0x16245FC0`→`0x2028C89A`，脉动 |
| 14 | 灼热热浪 heatwave | 饥饿 I（60t）+ 虚弱 I（60t） | 作物破坏 **30%**、农田变泥土 **30%**、草/泥土变砂土 30%/脉冲；收获倍率 ×0.50 | — | SMOKE 环境 1 个/4t（半径 8，高 2.5，速度 .012），另 22% 概率 FLAME 1 个（半径 7，高 2，速度 .006）（`:90-93`） | 有：`0x12F06A20`→`0x24E83B18`，脉动 |
| 15 | 迷途浓雾 dense_fog | 缓慢 I（60t）（**室内也生效**） | 收获倍率 ×0.88 | — | **无**（仅靠滤镜） | 有：`0x46C3CCCC`→`0x3A879797`，不脉动 |
| 16 | 彩虹雨 rainbow_rain | 幸运 **II**（60t）+ 再生 I（60t）+ 速度 I（60t） | 有益脉冲催熟 **16%**、湿润农田；收获倍率 ×1.60 | — | 紫色降水着色 `0xC56DDB` | 无 |
| 17 | 花粉风暴 pollen_storm | 反胃 I（**100t**，唯一超过 60t 的负面效果）（`WeatherManager.java:526`） | 有益脉冲催熟 **13%**、湿润农田；收获倍率 ×1.32 | — | SPORE_BLOSSOM_AIR 下落 2 个/4t，半径 11，高 5，速度 .018（`:94`） | 无 |
| 18 | 春日花潮 spring_bloom | 再生 I（60t）+ 幸运 I（60t） | 有益脉冲催熟 **18%**、湿润农田；收获倍率 ×1.45 | — | SPORE_BLOSSOM_AIR 环境 1 个（半径 10，高 5，速度 .012）+ HAPPY_VILLAGER 环境 1 个（半径 8，高 4，速度 .01）（`:95-98`） | 无 |
| 19 | 冰雹 hailstorm | 缓慢 I（50t）+ 每 tick `setTicksFrozen += 4`（上限 `需冻结tick`） | 玻璃破坏 10%、作物破坏 26%、农田变泥土 4%/脉冲；收获倍率 ×0.72 | 客户端冰屑落地：45%/4t 抽一次落点（17×17 内），每次 2 个 `BLOCK`+ICE 粒子，10% 概率播 `GLASS_BREAK` 音效（音量 .12，音高 1.35–1.70，`SoundSource.WEATHER`）（`:74-76,147-165`） | 有：`0x18DDEFFF`→`0x209CC9E8`，脉动 |
| 20 | 季风暴雨 monsoon | 每 20 tick 推动 18×8×18 内露天实体（强度 **0.06**，比飓风弱），无药水效果 | 作物破坏 4%、农田变泥土 4%/脉冲；有益脉冲催熟 4%、湿润农田；收获倍率 ×1.12 | — | 蓝色降水着色 `0x477AB8` | 无 |
| 21 | 金风送爽 golden_wind | 速度 I（60t）+ 幸运 I（60t） | 有益脉冲催熟 **10%**、湿润农田；收获倍率 ×1.30 | — | WAX_ON 下落 1 个/4t，半径 10，高 5，速度 .02（`:99`） | 无 |
| 22 | 霜夜 frost_night | 缓慢 I（60t）+ 每 tick `setTicksFrozen += 5`（上限 `需冻结tick`） | 作物破坏 16%/脉冲；收获倍率 ×0.82 | — | SNOWFLAKE 下落 1 个/4t，半径 9，高 5，速度 .015（`:100`） | 有：`0x1498D8E8`→`0x2070AFC8`，不脉动 |
| 23 | 静雪 silent_snow | 抗性提升 I（60t）+ 缓降 I（60t） | 收获倍率 ×1.10 | — | SNOWFLAKE 下落 2 个/4t，半径 11，高 6，速度 .018（`:69`） | 无 |
| 24 | 彗星夜 comet_night | 夜视（260t）+ 幸运 **II**（60t） | 收获倍率 ×1.25 | **XP ×1.75** | 50% 概率 END_ROD 下落 1 个（半径 13，高 12，速度 .16）+ ENCHANT 环境 1 个（半径 9，高 5，速度 .012）（`:101-104`） | 有：`0x126D55B8`→`0x1A356FC8`，脉动 |
| 25 | 酸雨 acid_rain | **头盔槽为空**的露天玩家中毒 I（60t）（`WeatherManager.java:552-556`） | 玻璃破坏 5%、作物破坏 **30%**、农田变泥土 5%/脉冲；收获倍率 ×0.60 | — | 绿色降水着色 `0xA0D33C` | 有：`0x163D9B36`→`0x245C7D24`，脉动 |
| 26 | 灰烬飘落 ash_fall | 挖掘疲劳 I（60t）+ 虚弱 I（60t） | 作物破坏 24%、农田变泥土 14%、草/泥土变砂土 14%/脉冲；收获倍率 ×0.70 | — | ASH 下落 3 个/4t，半径 12，高 7，速度 .022（`:105`） | 有：`0x1C514B49`→`0x28635A54`，不脉动 |
| 27 | 星露夜 star_dew | 夜视（260t）+ 幸运 **II**（60t）+ 再生 I（60t） | 有益脉冲催熟 **17%**、湿润农田；收获倍率 ×1.50 | XP ×1.50 | GLOW 环境 2 个/4t，半径 10，高 5，速度 .008（`:106`） | 有：`0x102E4C9C`→`0x1C7058B0`，脉动 |
| 28 | 雷雪 thundersnow | 缓慢 **II**（60t）+ 虚弱 I（60t）+ 每 tick `setTicksFrozen += 7`（上限 `需冻结tick+20`） | 玻璃破坏 8%、作物破坏 25%、农田变泥土 6%/脉冲；收获倍率 ×0.58 | 落雷 4.0%/脉冲 | SNOWFLAKE 下落 3 个/4t（半径 12，高 7，速度 .045），另 16% 概率 ELECTRIC_SPARK 环境 1 个（半径 8，高 4，速度 .035）（`:70-73`） | 有：`0x16A8C7DF`→`0x247887A8`，脉动 |
| 29 | 烟雨 misty_drizzle | 再生 I（60t） | 有益脉冲催熟 10%、湿润农田；收获倍率 ×1.28 | — | 灰蓝降水着色 `0x9CB8C2` | 有：`0x14AEBFC5`→`0x20899FA5`，不脉动 |
| 30 | 干雷 dry_lightning | 饥饿 I（60t）+ 虚弱 I（60t） | 玻璃破坏 4%、作物破坏 **32%**、农田变泥土 **30%**、草/泥土变砂土 30%/脉冲；收获倍率 ×0.55 | 落雷 **8.0%/脉冲**（四项落雷天气中最高） | 34% 概率 ELECTRIC_SPARK 环境 1 个（半径 9，高 5，速度 .04）（`:107-109`） | 有：`0x14D67520`→`0x248C4218`，脉动 |
| 31 | 萤火之夜 firefly_night | 夜视（260t）+ 幸运 I（60t） | 有益脉冲催熟 6%、湿润农田；收获倍率 ×1.18 | **XP ×1.15** | GLOW 环境 2 个/4t，半径 10，高 4，速度 .006（`:110`） | 有：`0x10275C38`→`0x1C8A7A28`，脉动 |
| 32 | 蜂舞晴日 bee_flight | 速度 I（60t）+ 幸运 I（60t） | 有益脉冲催熟 **21%**（最高）；收获倍率 ×1.42 | — | WAX_ON 环境 1 个（半径 9，高 4，速度 .018）+ HAPPY_VILLAGER 环境 1 个（半径 8，高 3.5，速度 .012）（`:111-114`） | 有：`0x10E3A91E`→`0x1CCA7A18`，脉动 |

**降水状态覆盖总表**（`applyVanillaWeather`，`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:293-314`）：

| 分组 | 天气 | 结果 |
| --- | --- | --- |
| 强制下雨（不雷） | `CHRISTMAS_EVE, SLIME_RAIN, BLIZZARD, GREEN_RAIN, RAINBOW_RAIN, HAILSTORM, MONSOON, SILENT_SNOW, ACID_RAIN, MISTY_DRIZZLE` | `rain=true, thunder=false` |
| 强制雨+雷 | `HURRICANE, THUNDERSTORM, REDSTONE_STORM, THUNDERSNOW` | `rain=true, thunder=true` |
| 强制晴朗（覆盖普通天气的降水） | `HARVEST_MOON, BLOOD_MOON, SOLAR_ECLIPSE, METEOR_SHOWER, SANDSTORM, AURORA, HEATWAVE, DENSE_FOG, POLLEN_STORM, SPRING_BLOOM, GOLDEN_WIND, FROST_NIGHT, COMET_NIGHT, ASH_FALL, STAR_DEW, DRY_LIGHTNING, FIREFLY_NIGHT, BEE_FLIGHT` | `rain=false, thunder=false` |
| 不干预 | `NONE`（走 `default`） | 沿用普通天气的 `rains()/thunders()` |

> `SANDSTORM` / `HEATWAVE` / `DRY_LIGHTNING` / `ASH_FALL` 属于"晴天但有破坏性脉冲"的天气：它们不下雨，但会破坏作物/农田/草方块并可能落雷（`WeatherManager.java:302-305` 与 `666-727`）。
> **日常事件「大雪 HEAVY_SNOW」是叠加层，优先级最高**：无论当前特殊天气是什么，都会强制 `rain=true, thunder=false`（`WeatherManager.java:308-312`），其降水着色为棕色 `0x6B3E23`（`src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEventRules.java:28`，`src/main/java/cn/farmingtales/qysevent/client/ClientWeatherState.java:25-32`）。

**效果强度换算（对写正式文档有用）**

| 分组 | 天气 | 单次收益/损失比例 |
| --- | --- | --- |
| 最强增产 | 丰收月 | +100% |
| 强增产 | 绿雨 +75%、彩虹雨 +60%、圣诞夜/星露夜 +50%、春日花潮 +45%、蜂舞晴日 +42% |
| 中增产 | 极光夜 +35%、花粉风暴 +32%、金风送爽 +30%、烟雨 +28%、彗星夜 +25%、流星雨 +20%、萤火之夜 +18%、季风暴雨 +12%、静雪 +10% |
| 中性 | 平稳天气（NONE）1.00 |
| 中减产 | 史莱姆雨 −8%、迷途浓雾 −12%、霜夜 −18%、红石雷暴 −22%、日食 −22%、血月 −28%、冰雹 −28%、雷雨 −32%、灰烬飘落 −30% |
| 强减产 | 酸雨 −40%、雷雪 −42%、沙尘暴 −45%、干雷 −45%、暴风雪 −48%、灼热热浪 −50%、飓风天 −58% |

---

## 3. 天气调度

### 3.1 状态机（跨日抽签 → 锁定 → 预报）

```
每 20 tick (gameTime % 20 == 0)
 └─ tickDay(overworld)                         WeatherManager.java:63-64,135-170
     ├─ day = floorDiv(dayTime, 24000)；若 day == data.lastProcessedDay 直接 return（每天只跑一次）  :136-138
     ├─ term = SolarTerms.current(level)；termChanged = (lastTerm != term.index)                 :140-141
     ├─ 写入 lastProcessedDay / lastTerm；activeEvent 清为 NONE；activeEventDay = day            :142-145
     ├─ 分支 A：data.forecastDay == day  （前一天已被预报机锁定）
     │    ├─ ordinaryWeather = forecastOrdinaryWeather                                            :148
     │    └─ activeEvent     = forecastEvent                                                     :149
     └─ 分支 B：未锁定
          ├─ ordinaryWeather = chooseOrdinaryWeather(random, term)                               :151
          └─ if (day >= cooldownUntilDay && random < SPECIAL_EVENT_CHANCE(0.15))
                 activeEvent = rollSpecialEvent(random, term)                                     :152-154
     ├─ 若 activeEvent != NONE：activeEventDay = day；cooldownUntilDay = day + cooldownDays + 1   :157-160
     ├─ applyVanillaWeather(level, data)（写原版雨/雷状态）                                        :162
     ├─ data.setDirty()                                                                          :163
     ├─ 节气播报（termChanged && announceSolarTerms）                                              :165-167
     └─ 特殊天气播报（"已触发：xxx" 或 "§d[特殊天气] §7未触发"）                                    :168-169
```

关键点：

1. **天气是"按游戏日"抽签，不是"按时间段"**。一个特殊天气绑定到具体的一天（`activeEventDay`），只在当天生效（`isEventActiveNow`，`WeatherManager.java:387-390`）。
2. **相位（DAY/NIGHT）只决定当天内哪一半时间生效**，不改变持续天数（`WeatherType.java:86-89`）。
3. **相位不命中时**：`syncClientWeather` 下发 `NONE`、`applyPlayerAtmosphere` 直接 return、`showActionBar` 直接 return、`applyVanillaWeather` 也不为它改降水 —— 即"白天抽到夜行天气"意味着该天气当天完全看不到任何效果，但跨日播报仍会宣告"已触发"（`WeatherManager.java:85,168,467,478,298`）。这是当前代码的确定行为。
4. **冷却**：`cooldownUntilDay = day + SPECIAL_EVENT_COOLDOWN_DAYS + 1`。默认 `SPECIAL_EVENT_COOLDOWN_DAYS = 2`，即触发特殊天气后，**接下来 3 个游戏日**（`day+1, day+2, day+3` 均 `< cooldownUntilDay`）不会再抽特殊天气（`WeatherManager.java:159`）。注意预报路径使用同样的判定（`WeatherManager.java:433`）。
5. **预报锁定**：`forecastTomorrow` 一旦被调用（天气预报机/许愿井/`/qysweather forecast`），当天的 `forecastDay = 明天`，并固定 `forecastOrdinaryWeather` 与 `forecastEvent`；第二天 `tickDay` 走分支 A 直接采用该结果，**所以付费预报 100% 准确**（`WeatherManager.java:421-443,147-149`）。
6. `rerollToday` 通过把 `lastProcessedDay` 与 `cooldownUntilDay` 重置为 `Long.MIN_VALUE` 并立即再跑一次 `tickDay` 来重抽当天（`WeatherManager.java:872-879`）。

### 3.2 普通天气抽取（`chooseOrdinaryWeather`）

```
roll = random.nextDouble()
if (roll < thunderChance(term))                       -> thunder（原版雷雨）
else if (roll < thunderChance + rainChance)           -> 按季段细分（雨类）
else                                                  -> 按季段细分（干类）
```
（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:172-201`；雨/雷概率表见 §1.4）

| 季段 | 雨类细分（`variation = random.nextDouble()`） | 干类细分 |
| --- | --- | --- |
| 春季 | `<0.45` 毛毛雨，否则 雨天 | `<0.22` 微风，`<0.40` 多云，`<0.48` 阴天，否则 晴天 |
| 夏季 | `<0.30` 太阳雨，`<0.42` 毛毛雨，否则 雨天 | `<0.16` 微风，`<0.28` 多云，否则 晴天（**夏季没有阴天**） |
| 秋季 | `<0.25` 毛毛雨，否则 雨天 | `<0.28` 阴天，`<0.45` 多云，`<0.58` 微风，否则 晴天 |
| 冬季 | 恒为 雨天 | `<0.38` 阴天，`<0.58` 多云，否则 晴天（**冬季没有微风**） |

来源：`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:179-200`。

### 3.3 `WeatherSavedData` 存了什么

`SavedData` 名：`solarweather_state`（`src/main/java/cn/farmingtales/qysevent/WeatherSavedData.java:8`），挂在**主世界** `ServerLevel` 的 `DataStorage` 上（`WeatherSavedData.java:24-26`，调用点始终传 `server.overworld()`，如 `WeatherManager.java:60,137,422`）。

| 字段 | 类型 | 默认 | NBT 键 | 含义 | 来源 |
| --- | --- | --- | --- | --- | --- |
| `lastProcessedDay` | long | `Long.MIN_VALUE` | `LastProcessedDay` | 已处理到的游戏日，防止同一天重复抽签 | `WeatherSavedData.java:10,30,48` |
| `lastTerm` | int | `-1` | `LastTerm` | 上次节气索引，用于播报与 `termChanged` | `:11,31,49` |
| `activeEvent` | `WeatherType` | `NONE` | `ActiveEvent`（存 `id()`） | 今天抽到/强制的特殊天气 | `:12,32,50` |
| `activeEventDay` | long | `Long.MIN_VALUE` | `ActiveEventDay` | 该特殊天气绑定的游戏日 | `:13,33,51` |
| `ordinaryWeather` | String | `"clear"` | `OrdinaryWeather` | 当前普通天气 id | `:14,34-35,52` |
| `ordinaryWeatherStreak` | int | 0 | `OrdinaryWeatherStreak` | 连续同一天气天数（用于递减惩罚） | `:15,36,53` |
| `cooldownUntilDay` | long | `Long.MIN_VALUE` | `CooldownUntilDay` | 该日之前不抽特殊天气 | `:16,37,54` |
| `forecastDay` | long | `Long.MIN_VALUE` | `ForecastDay` | 已被锁定的预报目标日 | `:19,38,55` |
| `forecastTerm` | int | `-1` | `ForecastTerm` | 预报目标日的节气索引（仅记录） | `:20,39,56` |
| `forecastOrdinaryWeather` | String | `"clear"` | `ForecastOrdinaryWeather` | 锁定的普通天气 | `:21,40-41,57` |
| `forecastEvent` | `WeatherType` | `NONE` | `ForecastEvent`（存 `id()`） | 锁定的特殊天气 | `:22,42,58` |

`lastTerm` 之外的所有字段都在 `setDirty()` 后写入（`WeatherSavedData.java:46-60`；调用点 `WeatherManager.java:163,436,846,857,867,877`）。

### 3.4 原版降水控制

`applyVanillaWeather(level, data)`（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:293-314`）：

- 若 `CONTROL_VANILLA_WEATHER = false`，**直接 return**，不做任何原版降水控制（`:294`）。
- 最终始终调用一次 `level.setWeatherParameters(rain ? 0 : 24000, rain ? 24000 : 0, rain, thunder)`（`:313`）：晴天时"晴天剩余 24000 tick"，雨天时"雨天剩余 24000 tick"，把原版随机天气计时器**每分钟覆写为整天**，从而长期压住原版随机天气。
- 该方法在每个 `tickDay`（跨日）被调用一次；日常事件「大雪」结束时也会调用 `reapplyVanillaWeather` 做一次恢复（`WeatherManager.java:316-319`，调用点 `src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEventManager.java:199`）。

### 3.5 预报机怎么读

| 读取入口 | 行为 | 来源 |
| --- | --- | --- |
| 天气预报机方块 | 手持**绿宝石**右击：非创造模式扣 1 颗，输出 `§6【天气预报】` + `forecastLine`，播 `AMETHYST_BLOCK_CHIME` 音效与 12 个 HAPPY_VILLAGER 粒子 | `src/main/java/cn/farmingtales/qysevent/WeatherForecasterBlock.java:27-47` |
| 许愿井 | 45% 给物品 / 25% 幸运 I(1200t) / 20% 同时播报明日特殊天气与明日世界事件 / 10% 拒绝；消耗 1 绿宝石，40 tick 冷却 | `src/main/java/cn/farmingtales/qysevent/WishingWellBlock.java:37-70` |
| `/qysweather forecast` | 权限 2，输出 `forecastLine` | `src/main/java/cn/farmingtales/qysevent/WeatherCommands.java:43-49` |
| `/qysevent status` | 同时输出今日世界事件与 `statusLine` | `src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEventCommand.java:50-58` |

`forecastLine` 只报**普通天气 + 特殊天气**两项（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:445-451`）；预报对象固定为"明天"（`targetDay = today + 1`，`WeatherManager.java:423-424`），**不分页、不提供多日预报**。工具提示里的"未来 10 天"属于预言机（OracleBlock）文案，不是天气预报机（`assets/qys_event/lang/zh_cn.json:48`）。

### 3.6 命令一览

**天气根命令**：`/qysweather`、`/solarweather`（旧名兼容别名）以及 `/qysevent weather`（三者为同一套节点）（`src/main/java/cn/farmingtales/qysevent/WeatherCommands.java:32-37`）。

| 命令 | 权限 | 参数 | 行为 | 来源 |
| --- | --- | --- | --- | --- |
| `<根> status` | 无 | — | 输出 `§6[当前天气] 普通天气：… \| 特殊天气：…` | `WeatherCommands.java:41-42,171-174` |
| `<根> forecast` | 2 | — | 锁定并输出明日预报 | `:43-49` |
| `<根> start <type>` | 2 | `greedyString`，建议列表 = 32 个特殊天气 id | `forceEvent`，立即生效并广播"已强制启动：xxx" | `:50-55,123-133,176-185` |
| `<根> stop` | 2 | — | `stopEvent`：清空特殊天气、重算原版降水、广播"特殊天气已停止" | `:56-61`；`WeatherManager.java:862-870` |
| `<根> reroll` | 2 | — | 重抽当天天气 | `:62-68` |

**原版 `/weather` 扩展**（重新注册了原版字面量，需要权限 2）（`src/main/java/cn/farmingtales/qysevent/WeatherCommands.java:76-121`）：

| 命令 | 默认时长 | 行为 |
| --- | --- | --- |
| `/weather clear` | `ServerLevel.RAIN_DELAY` | 强制普通天气 `clear`，同时清空特殊天气 |
| `/weather rain` | `ServerLevel.RAIN_DURATION` | 强制普通天气 `rain` |
| `/weather thunder` | `ServerLevel.THUNDER_DURATION` | 强制普通天气 `thunder` |
| `/weather <clear\|rain\|thunder> <duration>` | 同上 | `TimeArgument.time(1)`，时长单位为 tick，直接透传 |
| `/weather <solar_weather>` | 固定 24000 | greedyString；`none`/`normal`/`auto` → 停止特殊天气恢复正常循环；8 个普通天气 id → 强制该普通天气 24000 tick；其余按特殊天气 id/中文名强制启动 |
| 建议列表 | — | 8 个普通天气 + `none` + 32 个特殊天气，共 41 项（每项带灰色/金色 tooltip） |

- `forceOrdinaryWeather` 会把 `activeEvent` 清为 `NONE`、`ordinaryWeatherStreak` 重置为 1，并把原版降水设为 `durationTicks`（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:833-848`）。
- `forceEvent` 会把 `activeEventDay` 设为当天（不校验相位），随后 `applyVanillaWeather` 生效（`WeatherManager.java:850-860`）。
- 世界事件另有 `/qysevent event`、`/ftevent`、`/ftboss event`（权限 2 或单人房主），含 `list` / `trigger <id>` / `clear`（`src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEventCommand.java:26-47,60-64`）。

---

## 4. 天气的实际效果（数值与作用域）

### 4.1 产量倍率总公式

```
getYieldMultiplier(level):
  data = WeatherSavedData.get(overworld)              // 永远读主世界
  special = 当天且相位命中的特殊天气（否则 NONE）
  if (special != NONE) return special.yieldMultiplier()          // 特殊天气完全覆盖普通天气
  t = OrdinaryWeatherType.byId(data.ordinaryWeather)
  if (t.neutral()) return 1.0
  threshold = DIMINISHING_STREAK_START (默认 3)
  if (streak < threshold) return {RAIN:1.25, THUNDER:1.18, CLEAR:1.15, 其他:1.0}
  if (streak == threshold) return 1.0
  return max(0.65, 1.0 - 0.10 * (streak - threshold))
```
（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:397-419`；`ordinaryWeatherStreak` 在普通天气变化时重置为 1，否则 +1，`WeatherManager.java:203-209`）

连续天数衰减表（默认阈值 3，下限 0.65；最大连续奖励天数 = 2）：

| 连续相同普通天气天数 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | ≥8 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 雨天倍率 | 1.25 | 1.25 | 1.00 | 0.90 | 0.80 | 0.70 | 0.65 | 0.65 | 0.65 |
| 雷雨倍率 | 1.18 | 1.18 | 1.00 | 0.90 | 0.80 | 0.70 | 0.65 | 0.65 | 0.65 |
| 晴天倍率 | 1.15 | 1.15 | 1.00 | 0.90 | 0.80 | 0.70 | 0.65 | 0.65 | 0.65 |
| 其他非 neutral 普通天气 | 1.00 | 1.00 | 1.00 | 0.90 | 0.80 | … | | | 0.65 |

（计算来源：`WeatherManager.java:408-418`，`Math.max(0.65D, 1.0D - 0.10D * (streak - threshold))`）

> 作用域注意：`getYieldMultiplier` 使用 `level.getServer().overworld()`（`WeatherManager.java:118,128,398`；`WeatherCropLootModifier.java:31` 传入的 `context.getLevel()` 再到 `getYieldMultiplier` 内部转主世界）。因此**下界/末地的作物也吃主世界的天气倍率**。

### 4.2 作物产量 —— `WeatherCropLootModifier`

| 项目 | 值 | 来源 |
| --- | --- | --- |
| 注册名 | `qys_event:crop_yield` | `src/main/resources/data/qys_event/loot_modifiers/crop_yield.json:2`；`src/main/java/cn/farmingtales/qysevent/QysEventMod.java:30-31` |
| 全局挂载 | `global_loot_modifiers.json` 中 `entries: ["qys_event:crop_yield"]`，`replace: false` | `src/main/resources/data/forge/loot_modifiers/global_loot_modifiers.json:2-4` |
| 条件 | 无战利品条件（`"conditions": []`），判定全在 Java 内 | `data/qys_event/loot_modifiers/crop_yield.json:3` |
| 触发前提 | `CROP_YIELD_EFFECTS = true`；LootContext 含 `BLOCK_STATE`；`CropUtil.isMature(state)` 为真 | `src/main/java/cn/farmingtales/qysevent/WeatherCropLootModifier.java:27-29` |
| 倍率来源 | `WeatherManager.getYieldMultiplier(context.getLevel())`（即 §4.1 的公式） | `WeatherCropLootModifier.java:31` |
| 死亡区 | `abs(multiplier - 1.0) < 0.001` 时完全不改动 | `:32` |
| 增产算法 | 对**每一份已有掉落物**按 `count * (multiplier - 1)` 计算期望追加数，向下取整 + 小数部分概率进位；超过最大堆叠时拆成多份 `ItemStack.copy()` 追加 | `:34-43,57-66` |
| 减产算法 | 对**每一份已有掉落物**重算 `count = roundFloor(stock.count * multiplier)`，结果为 0 时**直接移除该掉落物** | `:44-52` |
| 作用对象 | 只有通过 `CropUtil.isMature` 的方块掉落表 | `:29`；`CropUtil.java:36-44` |

`CropUtil` 的作物判定（`src/main/java/cn/farmingtales/qysevent/CropUtil.java:19-34`）：

1. `CropBlock` / `SweetBerryBushBlock` / `NetherWartBlock` / `CocoaBlock` 实例直接算作物；
2. 否则取方块的注册名路径（小写），包含以下**任一子串**：`crop, wheat, barley, oat, corn, rice, tomato, grape, berry, tea_plant, coffee_plant, flax, chili, cucumber, soybean, redbean, leek, lettuce, onion, potato, carrot, beetroot`；
3. **且** 该方块是 `BushBlock` 或拥有名为 `age` / `stage` 的 `IntegerProperty`。
4. 成熟判定：`CropBlock` 用 `isMaxAge`；其他用 `age/stage` 属性的当前值 ≥ 该属性最大值；找不到属性视为已成熟（`CropUtil.java:36-44,46-55`）。

> 这是**名称/类型启发式**，不是标签驱动：`src/main/resources/data/` 下没有为作物定义任何标签（该目录仅含 `forge/loot_modifiers`、`minecraft/tags/blocks/needs_stone_tool.json`、`minecraft/tags/blocks/mineable/pickaxe.json`、`qys_event/loot_modifiers`、`qys_event/loot_tables`、`qys_event/recipes`）。

### 4.3 作物生长 —— `CropGrowEvent` 拦截

| 钩子 | 逻辑 | 数值 | 来源 |
| --- | --- | --- | --- |
| `CropGrowEvent.Pre` | `CROP_GROWTH_EFFECTS` 开启、方块是作物、且倍率 < 1.0 时：`random > multiplier` 就 `setResult(DENY)` | 倍率 0.42 时**拒绝 58%** 的随机生长 tick | `WeatherManager.java:114-120` |
| `CropGrowEvent.Post` | 生长已发生、方块仍是未成熟作物、倍率 > 1.0 时：`chance = min(0.45, (multiplier - 1) * 0.35)`，命中则再执行一次 `performBonemeal` | 倍率 2.00 → 35%；1.75 → 26.25%；1.18 → 6.3% | `WeatherManager.java:122-133,637-643` |

`growOnce` 只对 `BonemealableBlock` 且 `isValidBonemealTarget` / `isBonemealSuccess` 通过时调用 `performBonemeal`（`WeatherManager.java:637-643`）。注意：该随机 tick 逻辑与 `CropGrowEvent` 由原版作物随机 tick 触发，不是每 tick 执行。

### 4.4 地形脉冲（有益 / 破坏性）

**触发时机**：`gameTime % 100 == 0`（每 5 秒），且当天特殊天气处于生效窗口（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:70-77`）。

**有益地形脉冲**（`pulseBeneficialTerrain`，`:604-635`）：

- 每个玩家每脉冲执行 `max(6, TERRAIN_SAMPLES_PER_PLAYER)` 次采样（默认 `max(6,12) = 12`）。**注意下限是 6**：即使配置为 0，有益脉冲仍会采样 6 个方块（`:619`，与破坏性脉冲的 `samples <= 0 return` 不同）。
- 采样点：以玩家为中心 `x + [-20,20]`、`z + [-20,20]`（41×41），`y = MOTION_BLOCKING_NO_LEAVES 高度 - 1`（`:622-625`）。
- 命中农田且 `moisture < 7` → 直接设为 7（`:628-629`）。
- 命中未成熟作物且 `random < growChance` → 催熟一次（`growOnce`）（`:630-632`）。

| 天气 | growChance | 天气 | growChance |
| --- | --- | --- | --- |
| 绿雨 | 0.22 | 烟雨 | 0.10 |
| 蜂舞晴日 | 0.21 | 金风送爽 | 0.10 |
| 春日花潮 | 0.18 | 萤火之夜 | 0.06 |
| 星露夜 | 0.17 | 季风暴雨 | 0.04 |
| 彩虹雨 | 0.16 | 其他天气 | 0.0（直接 return，不采样、不浇地） |
| 花粉风暴 | 0.13 | | |

（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:605-617`）

**破坏性地形脉冲**（`pulseTerrain` + `damageOneBlock`，`:645-727`）：

- 前置：`DESTRUCTIVE_WEATHER = true`；若 `REQUIRE_MOB_GRIEFING = true` 则要求 `mobGriefing` 游戏规则为真；`TERRAIN_SAMPLES_PER_PLAYER <= 0` 直接返回（`:646-649`）。
- 每玩家每脉冲采样 `TERRAIN_SAMPLES_PER_PLAYER`（默认 12）次：`x/z` 为玩家 ±24 格（49×49）；`y` 交替取「地表 -1」与「玩家 y ±4」（`:651-663`）。
- 优先级：玻璃 → 作物 → 土地（命中即 `return`，`:709-726`）。玻璃用 `Tags.Blocks.GLASS` / `Tags.Blocks.GLASS_PANES` 判定并 `destroyBlock(pos, false)`（无掉落）；作物用 `CropUtil.isCrop` 判定并 `destroyBlock(pos, false)`（`:709-718`）。
- 土地效果：`FARMLAND → DIRT`；仅当天气属于 `HURRICANE / SANDSTORM / HEATWAVE / ASH_FALL / DRY_LIGHTNING` 时，`GRASS_BLOCK / DIRT → COARSE_DIRT`（`:720-726`）。

| 天气 | 玻璃破坏率 | 作物破坏率 | 土地（农田变泥土 / 草土变砂土） |
| --- | --- | --- | --- |
| 飓风天 | **0.24** | **0.38** | 0.16 / 0.16 |
| 暴风雪 | 0.06 | 0.30 | 0.08 / — |
| 沙尘暴 | 0.06 | 0.34 | 0.22 / 0.22 |
| 雷雨 | 0.06 | 0.18 | 0.06 / — |
| 红石雷暴 | 0.04 | 0.14 | 0.05 / — |
| 灼热热浪 | — | 0.30 | **0.30** / 0.30 |
| 血月 | — | 0.08 | — |
| 日食 | — | 0.08 | — |
| 史莱姆从天而降 | — | 0.08 | — |
| 冰雹 | 0.10 | 0.26 | 0.04 / — |
| 霜夜 | — | 0.16 | — |
| 季风暴雨 | — | 0.04 | 0.04 / — |
| 酸雨 | — | 0.30 | 0.05 / — |
| 灰烬飘落 | — | 0.24 | 0.14 / 0.14 |
| 雷雪 | 0.08 | 0.25 | 0.06 / — |
| 干雷 | 0.04 | **0.32** | **0.30** / 0.30 |
| 其他 16 种天气 | 0 | 0 | 0 |

（来源：`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:668-707`）

### 4.5 稀有危害（落雷 / 充能苦力怕 / 陨石）

同一 5 秒脉冲，仅对**露天玩家**（`level.canSeeSky(player.blockPosition().above())`）触发（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:729-768`）：

| 危害 | 触发概率（每玩家每 5 秒） | 作用范围与实现 |
| --- | --- | --- |
| 雷雨落雷 | 5.5% | 玩家 ±16 格（33×33），`MOTION_BLOCKING` 高度落雷；`DESTRUCTIVE_WEATHER=false` 时 `setVisualOnly(true)`（`:734,744-754`） |
| 红石雷暴落雷 | 6.5% | 同上 |
| 雷雪落雷 | 4.0% | 同上 |
| 干雷落雷 | **8.0%** | 同上 |
| 红石雷暴充能苦力怕 | 20%（每次最多检查/充能 1 只） | 20×10×20 内第一只存活、未充能、露天的苦力怕 → 生成 `visualOnly` 闪电实体并 `thunderHit`（`:741-743,770-782`） |
| 流星雨陨石 | 3.5% | 玩家 ±20 格（41×41），`MOTION_BLOCKING_NO_LEAVES` 高度：4 个 EXPLOSION 粒子 + `explode(null, x+.5, y+1, z+.5, 1.35F, TNT 或 NONE)`（`:755-766`） |
| 陨石携带矿脉 | 陨石触发后再 `METEOR_MINERAL_CHANCE`（默认 **6%**） | 在 `min(最大建筑高度-2, max(地表+24, 玩家y+20))` 处生成一个 `FallingBlockEntity`（`dropItem=true`、`setHurtsEntities(1.0F, 16)`），并给玩家发 `§b[流星雨] … 稀有陨石正在坠落！`（`:763-765,784-797`） |

**陨石矿石权重**（`chooseMeteorMineral`，`:799-809`，总量 1000）：

| 矿石 | 区间 | 概率 | 矿石 | 区间 | 概率 |
| --- | --- | --- | --- | --- | --- |
| 铜矿石 | 0–269 | 27.0% | 红石矿石 | 870–939 | 7.0% |
| 铁矿石 | 270–529 | 26.0% | 青金石矿石 | 940–979 | 4.0% |
| 煤矿石 | 530–769 | 24.0% | 钻石矿石 | 980–994 | 1.5% |
| 金矿石 | 770–869 | 10.0% | 绿宝石矿石 | 995–999 | 0.5% |

### 4.6 史莱姆雨（坠落生成）

`spawnFallingSlimes`，每 200 tick（10 秒）一次（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:78-81,811-831`）：

- 前置：`data.activeEvent == SLIME_RAIN` 且处于生效窗口；`SLIME_CAP_PER_PLAYER > 0`（默认 16）。
- 只对露天玩家生效；28×24×28 范围内现有 `Slime` 数量 ≥ 上限则跳过；否则 68% 概率继续。
- 每次生成 `1 + random(2)` 只（即 1–2 只），位置 `x/z = 玩家 ±14`，`y = min(最大建筑高度-3, 玩家y + 18 + random(8))`，尺寸 `1 + random(3)`（1–3），调用 `finalizeSpawn(EVENT)`。
- 与日常事件 `SLIME_RAIN` 的怪物替换逻辑相互独立（后者在 `src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEventHooks.java:141`）。

### 4.7 玩家增益/减益

- 施加上限频率：每 20 tick（1 秒）一次，效果时长 60 tick（`AURORA`/`COMET_NIGHT`/`STAR_DEW`/`FIREFLY_NIGHT` 的夜视为 260 tick；`POLLEN_STORM` 反胃为 100 tick；`HURRICANE` 缓慢为 40 tick；`HAILSTORM` 缓慢为 50 tick）（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:485-577`）。
- 全部使用 `ambient = true, visible = false`：**不显示药水粒子与 HUD 图标**（`MobEffectInstance(effect, duration, amplifier, true, false)`，含 `KA` 变体 4 与 5 参数构造）。
- **室内豁免**：只有 `BLOOD_MOON`、`SOLAR_ECLIPSE`、`DENSE_FOG` 对室内玩家也生效，其余天气在 `!outdoors` 时 `continue`（`:482-483`）。
- **酸雨特例**：只检查 `EquipmentSlot.HEAD` 是否为空，任何头盔（含皮革帽）都能完全免疫（`:552-556`）。
- **冻结机制**：`BLIZZARD` 每 tick `+8`（上限 `需冻结tick + 20`）、`THUNDERSNOW` 每 tick `+7`（同上限）、`FROST_NIGHT` 每 tick `+5`（上限 `需冻结tick`）、`HAILSTORM` 每 tick `+4`（上限 `需冻结tick`）（`:502,533,542,569`）。这是每个 tick 累加（由每秒的效果施加帧触发），原版冻结机制会随之为玩家叠加冰冻伤害。
- **血月怪物增益**：每 40 tick（2 秒）给玩家 28 格（`player.getBoundingBox().inflate(28.0D)`）内所有 `Monster` 力量 I + 速度 I（各 100 tick）（`:492-495,580-586`）。
- **推挤**：`HURRICANE` 与 `MONSOON` 每 20 tick 对玩家 18×8×18（`inflate(18,8,18)`）内的**存活且露天**实体施加 `push(cos(t*0.045)*s, 落地?0.03:0.01, sin(t*0.045)*s)`，`hurtMarked = true`；`HURRICANE` 强度 0.16，`MONSOON` 强度 0.06（`:504-507,535,588-602`）。

### 4.8 经验加成（`PlayerXpEvent.XpChange`）

判定用的是「主世界当天且相位命中的特殊天气」（`getEffectiveEvent(overworld)`，与玩家所在维度无关），只修改正值经验（`src/main/java/cn/farmingtales/qysevent/WeatherManager.java:102-112`）：

| 天气 | 倍率 | 取整方式 |
| --- | --- | --- |
| 极光夜 | ×2.00 | 直接 `amount * 2` |
| 彗星夜 | ×1.75 | `ceil`，至少 1 |
| 流星雨 / 星露夜 | ×1.50 | `ceil`，至少 1 |
| 萤火之夜 | ×1.15 | `ceil`，至少 1 |
| 其他天气 | ×1.00 | — |

### 4.9 生物生成影响

- **天气系统本身不改变生物生成**：`WeatherManager` 内没有任何 `MobSpawnEvent`/`FinalizeSpawn` 的生成替换逻辑，唯一的生物相关代码是史莱姆雨额外生成（§4.6）、血月怪物 buff（§4.7）与红石雷暴充能苦力怕（§4.5）。
- **怪物生成替换属于日常世界事件系统**（`src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEventHooks.java:128-161`），与天气类型无联动：`BLOOD_MOON / BIG_FISH_MIGRATION / KITCHEN_CHAOS / NO_SUCH_CREATURE / UNDEAD_MIGRATION / PIG_MIGRATION / HEAVY_SNOW / SLIME_RAIN / STARFALL / RABBIT_TIDE / WITCH_CONVENTION / GRAVITY_GLITCH / MUSHROOM_BLOOM / ENDER_ECLIPSE`。写天气文档时不要把这两套混为一谈。
- **维度与群系限制**：天气系统中**没有任何群系（Biome）判定**，也没有除"主世界 / 非主世界"以外的维度判定。唯一维度相关的过滤是客户端同步：非主世界玩家恒定收到 `special = NONE`、`ordinary = CLEAR`、`daily = null`（`WeatherManager.java:88-99`）。

### 4.10 「镀金矿脉」到底是什么

结论：**它是日常世界事件（`DailyWorldEvent.GOLD_RUSH`，中文名"镀金矿脉"），不是天气类型；它既不是战利品修改器也不是方块替换，而是 `BlockEvent.BreakEvent` 里的额外掉落注入。**

| 项目 | 值 | 来源 |
| --- | --- | --- |
| 中文名 | 镀金矿脉 | `assets/qys_event/lang/zh_cn.json:75` |
| 枚举 | `DailyWorldEvent.GOLD_RUSH("gold_rush")` | `src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEvent.java:20` |
| 实现 | `BlockEvent.BreakEvent` → `Block.popResource` | `src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEventHooks.java:411-418` |
| 作用标签 | 仅 `Tags.Blocks.ORES`（Forge 的 `forge:ores` 方块标签） | `DailyWorldEventHooks.java:415` |
| 前置 | 事件未取消、玩家非创造模式、`ServerLevel` | `DailyWorldEventHooks.java:413` |
| 概率 | `roll < 0.012` → 1 个粗金；`0.012 ≤ roll < 0.13` → 金粒 `1 + random(3)`（1–3 个） | `DailyWorldEventHooks.java:416-418` |
| 综合期望 | 粗金 1.2%，金粒 11.8%（合计 13% 触发额外掉落） | `DailyWorldEventHooks.java:417-418` |
| 与天气的关系 | 无。`WeatherCropLootModifier` 只处理成熟作物的产量倍率，不处理矿石 | `WeatherCropLootModifier.java:27-31` |

（该事件的触发概率由日常事件系统决定：`DAILY_TRIGGER_CHANCE = 0.10` 且候选按世界种子确定性轮转，`src/main/java/cn/farmingtales/qysevent/worldevent/DailyWorldEventRules.java:4,35-50`。完整日常事件表不在本报告范围内。）

---

## 5. 客户端表现与性能

### 5.1 降水着色（`LevelRendererMixin`）

- 目标方法：`LevelRenderer.m_109703_(Lnet/minecraft/client/renderer/LightTexture;FDDD)V`（即 `renderSnowAndRain` / 降水渲染），**SRG 名硬编码**，`remap = false`（`src/main/java/cn/farmingtales/qysevent/mixin/LevelRendererMixin.java:19-22`）。
- `HEAD` 注入：`RenderSystem.setShaderColor(r/255, g/255, b/255, 1.0F)`，RGB 取自 `ClientWeatherState.precipitationColor()`（`:22-33`）。
- `RETURN` 注入：恢复 `setShaderColor(1,1,1,1)`（`:35-40`）。
- Mixin 配置只注册客户端侧一个 mixin（`src/main/resources/qys_event.mixins.json:6-8`，`injectors.defaultRequire = 1`）。
- 这是**每帧常数开销**：一次 `ClientWeatherState` 静态读 + 两次 `setShaderColor`，不分配对象（注释见 `LevelRendererMixin.java:12-15`）。

**着色优先级**（`src/main/java/cn/farmingtales/qysevent/client/ClientWeatherState.java:25-32`）：

1. 若日常事件 == `HEAVY_SNOW` → `0x6B3E23`（棕色，最高优先级）；
2. 否则若特殊天气的 `precipitationColor() != 0xFFFFFF` → 用特殊天气颜色；
3. 否则用普通天气的颜色（`0xFFFFFF` 表示"无色偏"通常留给原版雪/白）。

即：只有 14 种特殊天气拥有降水着色（`WeatherType.java:66-84` 的 14 个 case），其余 18 种 + `NONE` 走普通天气着色。

### 5.2 全屏滤镜（`WeatherFilterOverlay`）

- 挂载点：`RenderGuiEvent.Post`，用 `GuiGraphics.fillGradient(0, 0, GUI宽, GUI高, top, bottom)` 铺满屏幕（`src/main/java/cn/farmingtales/qysevent/client/WeatherFilterOverlay.java:39-79`）。
- 淡入速度 1.8/s，淡出速度 2.6/s，`deltaSeconds` 上限 0.1 s（`:43,56-58`）；`strength <= 0.001` 时清空 `paintedWeather`（`:59-62`）。
- 脉动：`pulses = true` 的调色板透明度乘 `0.94 + 0.06 * sin(now_seconds * 1.5)`；`false` 时恒为 1.0（`:67-69`）。
- 18 种天气有滤镜（列表见 §2.3 末列），构造为 `new FilterPalette(topColor, bottomColor, pulses)`，颜色为 ARGB（`scaleAlpha` 只缩放 alpha 通道，`:112-116`）。
- 退出世界时 `ClientPlayerNetworkEvent.LoggingOut` 会重置 `ClientWeatherState` 与滤镜状态（`:81-86`）。
- 用 `paintedWeather` 缓存"最后一个有调色板的天气"，使淡出过程中颜色不会突变（`:52-54,64`）。

### 5.3 粒子：本地生成，服务端不广播

- 全部自定义天气粒子都在 `ClientWeatherParticles.onClientTick` 中通过 `level.addParticle(...)` 本地生成，`TickEvent.ClientTickEvent` 的 `END` 相位，且 `(level.getGameTime() & 3L) != 0` 时跳过 → **每 4 tick（0.2 秒，5 Hz）执行一次**（`src/main/java/cn/farmingtales/qysevent/client/ClientWeatherParticles.java:34-45`）。
- 前置：`weather == NONE && daily == null` 或玩家看不到天空 → 直接 return（`:44`）。
- 除以下服务端粒子外，**没有任何天气粒子经由网络发送**：陨石爆炸的 4 个 EXPLOSION 与 24 个 END_ROD、矿石陨石生成的 END_ROD（`WeatherManager.java:759-760,794-795`）、天气预报机 12 个 HAPPY_VILLAGER（`WeatherForecasterBlock.java:44-46`）、许愿井 10 个 HAPPY_VILLAGER（`WishingWellBlock.java:71-72`）。
- 粒子生成函数：
  - `falling(...)`：以玩家为中心球形半径 `radius`、高度 `3.5 + random*height`，速度 `((r-0.5)*0.018, -fallSpeed, (r-0.5)*0.018)`（`:122-132`）；
  - `ambient(...)`：半径 `radius`、高度 `0.5 + random*height`，速度各轴 `(r-0.5)*speed`（`:134-145`）；
  - `hailImpact(...)`：17×17 内随机取点，取 `MOTION_BLOCKING_NO_LEAVES` 高度，必须在已加载区块且能看到天空，生成 2 个 ICE 方块粒子 + 10% 概率 `GLASS_BREAK` 本地音效（音量 0.12，音高 1.35–1.70）（`:147-165`）。
- 每种天气/日夜事件的粒子数量、半径、高度、速度见 §2.3 表格与 `ClientWeatherParticles.java:49-119`。

**每 tick 平均粒子数（按 5 Hz 与概率折算）**

| 天气 | 平均粒子/tick | 天气 | 平均粒子/tick |
| --- | --- | --- | --- |
| 雷雪 | 0.75 + 0.04 spark | 春/秋 萤火（夜） | 1.00 |
| 冰雹 | 0.225（脉冲内 2 个） | 血月 | 0.25 |
| 暴风雪 | 1.00 | 日食 | 0.25 |
| 圣诞夜 / 静雪 | 0.50 | 沙尘暴 | 0.50 |
| 流星雨 / 彗星夜 | 0.095 + 0.25 enchant | 星露夜 | 0.50 |
| 极光夜 | 0.25 + 0.125 | 干雷 | 0.085 |
| 灼热热浪 | 0.25 + 0.055 | 蜂舞晴日 | 0.50 |
| 花粉风暴 | 0.50 | 春日花潮 | 0.50 |
| 金风送爽 | 0.25 | 灰烬飘落 | 0.75 |
| 丰收月 | 0.25 | 霜夜 | 0.25 |
| 日常事件 界蚀 / 萤火 / 星落 / 血月 | 0.25 / 0.15 / 0.11 / 0.25 | 无粒子天气（14 种） | 0 |

（数据来源：`ClientWeatherParticles.java:49-119`；"无粒子"= 史莱姆雨、飓风天、雷雨、红石雷暴、绿雨、迷途浓雾、彩虹雨、季风暴雨、烟雨、萤火之夜(有)… 等按代码 `default -> {}` 分支）

### 5.4 网络包何时发送

| 项目 | 值 | 来源 |
| --- | --- | --- |
| 包类 | `WeatherSyncPacket(WeatherType, OrdinaryWeatherType, DailyWorldEvent)`，编码为 3 个 `writeUtf` 字符串（id），解码时 `readUtf(64)` | `src/main/java/cn/farmingtales/qysevent/WeatherSyncPacket.java:12-25` |
| 通道 | `qys_event:weather`，协议版本 `"2"`，消息 id 0，方向 `PLAY_TO_CLIENT` | `src/main/java/cn/farmingtales/qysevent/WeatherNetwork.java:14-31` |
| 发送时机（周期） | 每 20 tick 的 `syncClientWeather`（`gameTime % 20 == 0`）；**仅在快照变化时发送**（`WeakHashMap<ServerPlayer, ClientWeatherSnapshot>` 比较，相等则 `continue`） | `WeatherManager.java:63-66,84-100` |
| 即时发送 | `forceOrdinaryWeather` / `forceEvent` / `stopEvent` 都会调用 `syncClientWeather` 立即推送 | `WeatherManager.java:847,858,868` |
| 接收处理 | `DistExecutor.unsafeRunWhenOn(CLIENT)` → `ClientWeatherState.setWeather(...)` | `WeatherSyncPacket.java:27-34` |
| 客户端状态清理 | `ClientPlayerNetworkEvent.LoggingOut` → `ClientWeatherState.clear()` | `WeatherFilterOverlay.java:81-86` |

- 每个玩家的快照内容：主世界玩家拿到真实 `active`/`ordinary`，非主世界玩家恒为 `NONE`/`CLEAR`/`daily=null`（`WeatherManager.java:88-95`）。因此切换维度会让你收到一次包（快照变了）。
- 玩家退出时 `WeakHashMap` 条目可被 GC 回收；`LAST_CLIENT_WEATHER` 是 `Collections.synchronizedMap`（`WeatherManager.java:50-51`）。
- 包只有在"可见状态"变化时才发，配合 5 Hz 的本地粒子/服务端脉冲，整体网络开销为**每玩家每次状态变化 1 个不足 200 字节的小包**。

---

## 6. 配置项（`config/qys_event-common.toml`，`COMMON` 类型）

声明位置 `src/main/java/cn/farmingtales/qysevent/QysEventMod.java:41`，定义 `src/main/java/cn/farmingtales/qysevent/WeatherConfig.java:20-54`。以下默认值与实例配置文件 `config/qys_event-common.toml` 完全一致。

### `[general]`

| 键 | 类型 / 范围 | 默认 | 含义与代码作用点 |
| --- | --- | --- | --- |
| `controlVanillaWeather` | boolean | `true` | 是否由节气系统接管原版降水。`false` 时 `applyVanillaWeather` 立即返回，不覆写原版天气计时器（`WeatherManager.java:294`） |
| `announceSolarTerms` | boolean | `true` | 节气变更时是否全服广播（`WeatherManager.java:165-167`） |
| `specialEventChance` | double，0.0–1.0 | `0.15` | 每天触发特殊天气的概率（在冷却检查通过后）。`random.nextDouble() < chance`（`WeatherManager.java:152,433`） |
| `specialEventCooldownDays` | int，0–30 | `2` | 特殊天气后的最小间隔天数，实际锁定 `day + N + 1` 天（`WeatherManager.java:159`） |

### `[damage]`

| 键 | 类型 / 范围 | 默认 | 含义与代码作用点 |
| --- | --- | --- | --- |
| `destructiveWeather` | boolean | `true` | 是否允许破坏性脉冲。`false` 时 `pulseTerrain` 直接返回，且落雷变 `setVisualOnly(true)`、陨石爆炸交互降为 `NONE`（`WeatherManager.java:646,751,761`） |
| `requireMobGriefing` | boolean | `true` | 破坏方块前要求 `mobGriefing` 游戏规则为真（`WeatherManager.java:647`） |
| `terrainSamplesPerPlayer` | int，0–64 | `12` | 每玩家每次脉冲采样的方块数（每 5 秒）。破坏性脉冲为 0 时完全停止；**有益脉冲实际使用 `max(6, 该值)`**（`WeatherManager.java:619,648`） |
| `slimeCapPerPlayer` | int，0–64 | `16` | 史莱姆雨时 28×24×28 区域内的史莱姆上限；为 0 时完全停止生成（`WeatherManager.java:812-817`） |
| `meteorMineralChance` | double，0.0–1.0 | `0.06` | 流星雨陨石额外坠落含矿方块的概率（`WeatherManager.java:763`） |

### `[farming]`

| 键 | 类型 / 范围 | 默认 | 含义与代码作用点 |
| --- | --- | --- | --- |
| `cropGrowthEffects` | boolean | `true` | 是否对作物生长施加加减益（控制 `CropGrowEvent.Pre/Post` 两个钩子，`WeatherManager.java:116,124`） |
| `cropYieldEffects` | boolean | `true` | 是否对成熟作物掉落施加倍率（控制 `WeatherCropLootModifier`，`WeatherCropLootModifier.java:27`） |
| `diminishingStreakStart` | int，2–10 | `3` | 连续同一天气达到该天数前为奖励，等于该天数为 1.0，超过后每天 −0.10（下限 0.65）（`WeatherManager.java:408-418`） |

> 全部键名都是**小驼峰**（例如 `controlVanillaWeather`），配置文件名硬编码为 `qys_event-common.toml`（`QysEventMod.java:41`）。
> `WeatherConfig` 中没有天气类型名单、倍率、持续时间、颜色、粒子数量的配置项——**这些全部硬编码在 `WeatherType` / `OrdinaryWeatherType` / `WeatherManager` 中，数据包也无法覆盖**（`src/main/resources/data/` 下没有与天气相关的数据文件）。

---

## 7. 写文档时需要注意的实现细节（易踩坑）

1. **特殊天气不跨维度**：只有主世界的天气被抽取与生效；非主世界玩家收到的一律是 `NONE`/`CLEAR`（`WeatherManager.java:88-99`），但**玩家 buff、怪物 buff、落雷、陨石、史莱姆雨对全服所有玩家生效**，不判断玩家所在维度（`WeatherManager.java:481,730,814`）。
2. **作物倍率跨维度**：主世界以外的作物（下界/末地农场）也吃主世界天气倍率（`WeatherManager.java:398`）。
3. **相位错配 = 当天看不到效果**：例如白天下抽到霜夜，玩家与客户端当天什么都感知不到，但聊天栏仍播报"已触发"（见 §3.1 第 3 点）。
4. **`SLIME_RAIN` 降水着色为绿色（`0x79C95A`）但默认无自定义粒子**，视觉上完全依赖着色 + 坠落史莱姆实体（`WeatherType.java:69`；`ClientWeatherParticles.java:118` 的 `default` 分支）。
5. **`SANDSTORM` / `HEATWAVE` / `DRY_LIGHTNING` / `ASH_FALL` 被强制为"晴天"**，却仍有破坏性脉冲与落雷，属于"无雨灾害"（`WeatherManager.java:302-305`）。
6. **`DENSE_FOG` 没有粒子**，只有更深的灰雾滤镜 + 缓慢（`WeatherFilterOverlay.java:23,93`）。
7. **`NONE` 不是"没有天气"**：它表示"今天没有特殊天气"，普通天气仍然生效，产量倍率由普通天气与连击天数决定（`WeatherType.java:6`；`WeatherManager.java:403-419`）。
8. **`/weather <id>` 在 id 未识别/为 `none` 时不会立即清掉特殊天气**；`start()` 对 `NONE` 直接返回失败消息"未知天气类型"（`WeatherCommands.java:176-181`），只有 `/weather none|normal|auto` 或 `<根> stop` 才会调 `stopEvent`（`WeatherCommands.java:106-112`）。
9. **预报只有 1 天**（明天），且一旦读取即被锁定，因此同一天多次读取结果一致（`WeatherManager.java:421-443`）。
10. **`showActionBar` 只在 `activeEvent != NONE` 时显示**，且即使相位不命中也会显示（它只检查 `data.activeEvent`，不检查 `isEventActiveNow`）（`WeatherManager.java:466-471`）——这条与 §3.1 第 3 点的"相位不命中"会同时出现：聊天播报 + 每 10 秒 actionbar 提示，但实际无任何效果。
