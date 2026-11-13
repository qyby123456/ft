# 许愿井研究报告（qys-event 自带方块 + KubeJS 多方块）

> 本报告只描述"当前代码行为"，不含历史修改记录。
> 所有路径均相对实例根 `C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2`。

## 0. 快照与来源

调研快照时间：**2026-10-05 13:01（本地时间）**。以下文件均按该时刻内容描述，附 SHA-256 前 16 位以便复核：

| 文件 | 字节 | mtime | SHA-256(前16) |
| --- | --- | --- | --- |
| `_work/qys-event/src/main/java/cn/farmingtales/qysevent/WishingWellBlock.java` | 5938 | 12:58:32 | `D73B17AC3EF44FC3` |
| `_work/qys-event/src/main/java/cn/farmingtales/qysevent/WishingWellItem.java` | 1059 | 11:11:42 | `93FBE3047B477CED` |
| `_work/qys-event/src/main/java/cn/farmingtales/qysevent/ModBlocks.java` | 2167 | 11:11:42 | `8FCAA9624B18C67C` |
| `_work/qys-event/src/main/resources/assets/qys_event/lang/zh_cn.json` | 9177 | 11:23:05 | `17F1F67CF486F2D7` |
| `_work/qys-event/src/main/resources/data/qys_event/recipes/wishing_well.json` | 247 | 10:38:02 | `5ECC68509DFC69B3` |
| `_work/qys-event/src/main/resources/data/qys_event/loot_tables/blocks/wishing_well.json` | 180 | 10:38:02 | `EC9ABBE0A579EBD9` |
| `kubejs/server_scripts/block_events/wishing_well.js` | 35869 | 10:40:42 | `69DB9904CCC69E2D` |
| `kubejs/startup_scripts/content/wishing_well.js` | 626 | 2026-10-04 21:36:47 | `6CCB87A18BFB4327` |
| `kubejs/client_scripts/tooltips/wishing_well.js` | 948 | 10:41:31 | `A9F93ED6C9998B4B` |
| `kubejs/WISHING_WELL.md` | 2739 | 10:41:31 | `FD4BE737EE071659` |
| `schematics/xyj.nbt` | 770 | 2026-10-04 22:41:00 | `15BB26FC45FFCA41` |

**重要前提（源码正在被并发修改）**：调研过程中 `_work/qys-event/src` 于 12:58:32 被整体重写（`WishingWellBlock.java` 由 76 行变为 99 行、`DailyWorldEventManager.java` 由 236 行变为 207 行）。本报告第 3 节描述的是 **12:58:32 之后的当前源码**；已安装的 `mods/qys-event-forge-1.20.1-1.0.2.jar`（构建于 11:43:57）**仍是旧版**——已核对该 jar 内 `WishingWellBlock.class` 常量池中不存在 `wishing_well_stones` / `wishing_well_woods`，即线上运行时行为与当前源码不一致（详见 3.6、6.1）。

---

## 1. 两套许愿井的区别（对照表）

| 维度 | **A：`qys_event:wishing_well`（Mod 方块）** | **B：`farmingtales:wishing_well_core`（KubeJS 多方块）** |
| --- | --- | --- |
| 实现位置 | Java 方块类 `WishingWellBlock.java:29`，注册于 `ModBlocks.java:29-33` | 方块注册 `kubejs/startup_scripts/content/wishing_well.js:4`；玩法逻辑 `kubejs/server_scripts/block_events/wishing_well.js` |
| 代价 | 1 颗绿宝石（`WishingWellBlock.java:32,48`）；创造模式不扣（`:48`） | 1 颗绿宝石（`wishing_well.js:4`）；右键时创造模式不扣（`:600`），**丢弃投入时无创造判定，照扣**（`:396-412`） |
| 交互方式 | 手持绿宝石右键方块；手持其它物品右键返回 `PASS`（不消耗、无提示、无反应），`WishingWellBlock.java:32` | ① 把绿宝石**丢**进 8 格内圈水 ② 手持绿宝石右键核心 ③ **潜行右键**看搭建指南 ④ **空手右键**状态栏提示（`wishing_well.js:564-576`） |
| 结构要求 | 无，单方块放在哪里都能用 | 必须完整匹配 `schematics/xyj.nbt` 蓝图，共校验 77 个方块位（`wishing_well.js:160-227`），不完整则不消耗且报错 |
| 冷却 | **40 tick（2 秒）**，用原版绿宝石物品冷却（`WishingWellBlock.java:39,47`） | 右键 **12 tick（0.6 秒）**，自建 per-player 冷却 + `addItemCooldown`（`wishing_well.js:13,599-601`）；丢弃路径**无冷却**，仅受"实体生成后 200 tick（10 秒）追踪窗口"限制（`:6,539-540`） |
| 每日次数限制 | 无 | 无 |
| 产出类别 | 45% 物品（石料/木材/金粒/紫水晶/铁粒/绿宝石/钻石）＋25% 幸运 I＋20% 明日预报＋10% 空（`WishingWellBlock.java:51-82`） | 39.9% 空＋22% 杂物＋10% 木材＋10% 食物＋7% 屎＋5% 金币＋3% 种子袋＋2.5% 矿物＋0.5% 绿宝石＋0.1% 晶球（`wishing_well.js:265-277`） |
| 是否影响世界事件 | 不触发任何事件；每日世界事件只做**只读**查询（`DailyWorldEventManager.java:71-81`）。但 20% 分支会**生成并写入**明日天气预报（`WeatherManager.java:429-438`），该预报次日会成为实际天气（`WeatherManager.java:146-148`） | 完全不涉及天气/世界事件系统，脚本内无任何相关调用 |
| 随机源 | `level.random`（世界随机源，`WishingWellBlock.java:41,43`） | `Math.random()`（JS 全局随机，`wishing_well.js:251,266`），**不受世界种子/幸运效果影响** |
| 管理员命令 | 无 | `/wishingwelltest …`、`/ft wishingwell …`，权限等级 2（`wishing_well.js:759-791`） |
| 合成 | 有序 3×3：`SGS/GEG/SSS`（S=石头，G=金锭，E=绿宝石）→ 5 石头＋3 金锭＋1 绿宝石（`data/qys_event/recipes/wishing_well.json`） | 有序 3×3：`SES/CWC/SSS`（S=石砖，E=绿宝石，C=錾制石砖，W=水桶）→ 5 石砖＋2 錾制石砖＋1 绿宝石＋1 水桶（`wishing_well.js:793-803`） |
| 掉落 | 自身（`loot_tables/blocks/wishing_well.json`，仅 `survives_explosion` 条件） | 默认掉自身；开采需石镐及以上（`startup_scripts/content/wishing_well.js:9-11` 打了 `mineable/pickaxe` + `needs_stone_tool`） |

---

## 2. KubeJS 多方块许愿井（B）

### 2.1 蓝图实测（直接解析 `schematics/xyj.nbt`）

- 文件为 gzip 压缩 NBT（magic `1F 8B`），770 字节，SHA-256 = `15BB26FC45FFCA41AC59D208EB747EE6B69A4CD2FACE3E5FEBDED78B4030157F`，与脚本内常量 `wishing_well.js:18` 完全一致。
- `size = [7, 5, 7]`，`blocks` 共 77 项，无实体。
- 核心 `farmingtales:wishing_well_core` 位于 NBT 坐标 **(3, 1, 3)**，与脚本注释 `wishing_well.js:17` 一致；后文所有"相对坐标"均以核心为 (0,0,0)。
- 调色板实际内容（20 项）：石砖、石砖台阶（`type=double`）、绿宝石块、4 种朝向的石砖楼梯、9 种连接状态的石砖墙、石砖台阶（`type=bottom`）、水源、`farmingtales:wishing_well_core`、`torchmaster:invisible_light`。
- 逐层实测（数字为调色板索引，`.` = 空气）：

```text
Y-1（地基，NBT y=0）           Y0（井盆，NBT y=1）
. . .  4 . . .                 .  .  .  .  .  .  .
.  0  0  0  0  0 .             .  7 12  9 15 16  .
.  0  0  0  0  0 .             .  8 13 13 13  8  .
 3  0  0  0  0  0  6             .  9 13 14 13  9  .
.  0  0  0  0  0 .             . 10 13 13 13 10  .
.  0  0  0  0  0 .             . 11 12  9 15 17  .
. . .  5 . . .                 .  .  .  .  .  .  .

Y+1 / Y+2（立柱，NBT y=2,3）    Y+3（屋顶，NBT y=4）
. . . . . . .                  .  .  .  .  .  .  .
. 18 . . . 18 .                .  9  1  2  1  9  .
. . . . 19 . .                 .  1  .  .  .  1  .
. . . . . . .                  .  2  .  .  .  2  .
. . . . . . .                  .  1  .  .  .  1  .
. 18 . . . 18 .                .  9  1  2  1  9  .
. . . . . . .                  .  .  .  .  .  .  .
```

索引对照：`0`=石砖，`1`=石砖台阶(double)，`2`=绿宝石块，`3/4/5/6`=朝东/南/北/西的石砖楼梯，`7~12、15~18`=石砖墙，`9`=石砖台阶(bottom)，`13`=水源，`14`=许愿井核心，`19`=**`torchmaster:invisible_light`**（仅此 1 个，位于相对 (+1, **+1**, -1)）。

结论：`WISHING_WELL.md` 的结构表与 NBT **完全一致**，包括"屋顶边段是双层台阶、四角是下半台阶"这一细节。

### 2.2 结构校验规则（`ftwwCheckStructure`，`wishing_well.js:160-227`）

校验不是"范围扫描找匹配"，而是**以核心为原点的 77 个硬编码绝对偏移逐格比对**，每格只比较 **方块 id**（`ftwwBlockId` = `level.getBlock(x,y,z).id`，`wishing_well.js:146-148`）。校验半径：水平 ±3 格，下方 1 格、上方 3 格。

| 顺序 | 相对坐标 (dx, dy, dz) | 期望方块 id | 失败时标签 | 代码行 |
| --- | --- | --- | --- | --- |
| 1 | dx,dz ∈ [-2,2]，dy=-1（25 格） | `minecraft:stone_bricks` | `石砖地基` | 167-172 |
| 2 | (-3,0,0) (0,0,-3) (0,0,3) (3,0,0) at dy=-1（4 格） | `minecraft:stone_brick_stairs` | `朝向井心的石砖楼梯` | 175-181 |
| 3 | dy=0，井盆外圈 16 格：边中点 (±2,0)/(0,±2) → `minecraft:stone_brick_slab`；其余 12 格 → `minecraft:stone_brick_wall` | 同上 | `下半石砖台阶井沿` / `石砖墙井沿` | 185-192 |
| 4 | dy=0，内圈 8 格（max(\|dx\|,\|dz\|)=1 且非中心） | `minecraft:water` | `水源` | 193-195 |
| 5 | dy=0 中心 (0,0,0) | **跳过不校验**（核心自身） | — | 187 |
| 6 | 四角 (±2,±2)，dy=+1 与 dy=+2（8 格） | `minecraft:stone_brick_wall` | `石砖墙立柱` | 201-209 |
| 7 | dy=+3，屋顶外圈 16 格：边中点 (±2,0)/(0,±2) → `minecraft:emerald_block`；其余 12 格 → `minecraft:stone_brick_slab` | 同上 | `绿宝石块屋顶节点` / `石砖台阶屋顶` | 213-222 |

**容差 / 允许的替换**：

- **只校验方块 id，不校验方块状态**：楼梯朝向（蓝图是"朝向井心"）、台阶 `half`/`type`（蓝图屋顶 8 格是 `type=double`、4 角是 `type=bottom`，两者 id 都是 `minecraft:stone_brick_slab`）、石砖墙的连接状态、水的 `level` 全部**不校验**（`wishing_well.js:154-158` 只比 `found === expectedId`）。
- **多余的方块一律容忍**：不检查"多出来的方块"，因此蓝图里的 `torchmaster:invisible_light`（相对 +1,+1,-1）以及任何额外装饰都不会导致失败；反过来，缺失的额外方块也不影响（脚本明确注释不校验它，`wishing_well.js:224-225`）。
- 任一格失败立即 `return`（只报**第一个**不匹配的位置，按上表顺序 + dx/dz 递增顺序）。

### 2.3 结构不完整时的表现

| 触发方式 | 是否消耗 | 表现 | 代码行 |
| --- | --- | --- | --- |
| 丢入绿宝石 | **不消耗**（消耗发生在校验之后） | 在第一个错误位置发 `minecraft:angry_villager` 粒子（`0.22` 扩散、`14` 个、`force @a`，位置 = 方块中心 +0.5/+0.6/+0.5）＋ `minecraft:block.note_block.bass` 音效（音量 0.75、音高 0.65、`@a`）；私聊 `§c投入未生效：许愿井结构不完整。相对坐标 §f(dx, dy, dz) §c需要§f<标签>§c，当前为§8<实际id>`；控制台 warn | `wishing_well.js:501-507`、粒子 `:240-246`、文案 `:504` |
| 手持绿宝石右键 | **不消耗** | 同样的错误粒子与音效；私聊 `§c许愿井结构不完整：相对坐标 …` ＋ `§7潜行右键核心可再次查看完整搭建方法。` | `:589-597`、粒子 `:240-246`、文案 `:593-594` |
| 管理员命令 | 不消耗（本来就不消耗） | 同样的错误粒子与音效；私聊 `§c测试被结构校验拦截：相对坐标 …` | `:734-741`、文案 `:738` |

> `WISHING_WELL.md` 第 71 行写"首个错误位置会产生**红色**粒子"：代码实际是 `minecraft:angry_villager`（原版为灰黑色怒气云，并非红色），并附带一个低音音符盒音效。**以代码为准**。

### 2.4 交互方式与消耗规则

**(a) 丢弃投入（主玩法）**

1. `EntityEvents.spawned` 只把 **`minecraft:item` 且物品 id 为 `minecraft:emerald`** 的实体登记进 `global.ftwwPendingEmeraldDrops`（`wishing_well.js:520-528`）。
2. `ServerEvents.tick` 每 **2 tick（0.1 秒）** 扫描一次队列；每个条目的 `age` 每次 +2，**`age > 200`（即 10 秒）后停止追踪**（`:530-544`、常量 `:5-6`）。
3. 命中判定（`ftwwFindCoreForDroppedEmerald`，`:421-439`）：物品实体所在方块 **或** 其正下方 1 格必须是 `minecraft:water`；再在该水格的 **同 Y 层 3×3 邻域**（跳过自身）中寻找 `farmingtales:wishing_well_core`。→ 对应蓝图的 8 格内圈水；**丢在核心方块正上方不算**（那里不是水）。
4. 找触发者：以物品实体为中心找最近玩家，**欧氏距离 ≤ 12 格**（`:441-479`，先查 12 格范围实体，再回退遍历同维度玩家，仍受 12 格上限约束）。
5. 结构校验 → 通过后消耗：
   - `count > 1`：把实体物品堆改成 `count - 1`（**一次丢一整组只扣 1 颗、只抽 1 次**），`:401`
   - `count == 1`：`discard()`/`kill()` 移除实体，`:403-410`
   - 消耗失败会打 error 日志并放弃本次（`:509-512`）
6. 成功后 `player.swing()`（`:515`），并以 `trigger=drop` 结算（`:516`）。
7. 注意：该路径**没有创造模式判定**，创造玩家丢出的绿宝石同样会被扣。

**(b) 手持绿宝石右键核心**（`BlockEvents.rightClicked`，`:557-607`）

- 顺序：潜行判定 → 手持物判定 → 本地冷却判定 → 结构校验 → 扣物品 → 结算。
- 潜行右键（任意手持物）：只显示搭建指南并 `cancel()`（`:564-568`）。
- 空手右键：状态栏提示 `§7把§a绿宝石§7丢入内圈水中，或手持右键核心；潜行右键查看结构。` 并 `cancel()`（`:571-575`）。
- 手持非绿宝石的其它物品：**直接 `return`，不 cancel、无任何提示**（`:570-576`）。
- 冷却：`global.ftwwRightClickCooldowns[uuid] = now + 12`（`:13,599`），冷却中静默 `cancel()` 并打 info 日志（`:583-587`）。脚本注释说明：故意不用原版绿宝石冷却，避免被其它 Mod 施加的冷却误伤（`:578-579`）。同时仍调用 `player.addItemCooldown('minecraft:emerald', 12)`（`:601`）。
- 扣物品：`if (!player.isCreative()) item.count--`（`:600`）→ **创造模式右键不消耗**。
- 每次结算前都会先播放一次通用前置特效：`minecraft:enchant` 粒子（0.55/0.35/0.55 扩散、`0.18` 速度、`36` 个、`force @a`）＋ `minecraft:entity.item.pickup` 音效（音量 0.8、音高 0.55）（`:345-350`）。

**(c) 潜行右键指南文案**（逐行硬编码，`:229-238`）

```text
§6§l许愿井蓝图结构 §7（xyj.nbt，核心为 0,0,0）
§eY-1：§f5×5 石砖地基，四边正中向外各接 1 个石砖楼梯
§eY 0：§f5×5 井盆；四边中点用下半石砖台阶，其余外圈用石砖墙
§eY 0：§f内圈 8 格水源，正中放许愿井核心
§eY+1～Y+2：§f四角各叠 2 格石砖墙立柱
§eY+3：§f5×5 外圈屋顶；四边中点为绿宝石块，其余为石砖台阶
§a结构完成后，把 1 颗绿宝石丢入内圈水中即可许愿。
§7也可以手持绿宝石右键核心；两种方式都只消耗 1 颗。
```

### 2.5 概率表与产出池

**主概率表**（`ftwwRollOutcome`，`wishing_well.js:265-277`，用 `Math.random()*100` 单次 roll）：

| 顺序 | 事件 | 概率 | 判定区间 | 代码行 |
| --- | --- | --- | --- | --- |
| 1 | 无事发生 | **39.9%** | roll < 39.9 | 267 |
| 2 | 无用材料（杂物池） | **22.0%** | 39.9 ≤ roll < 61.9 | 268 |
| 3 | 木材 | **10.0%** | 61.9 ≤ roll < 71.9 | 269 |
| 4 | 食物 | **10.0%** | 71.9 ≤ roll < 81.9 | 270 |
| 5 | 屎 | **7.0%** | 81.9 ≤ roll < 88.9 | 271 |
| 6 | 金币 | **5.0%** | 88.9 ≤ roll < 93.9 | 272 |
| 7 | 随机种子袋 | **3.0%** | 93.9 ≤ roll < 96.9 | 273 |
| 8 | 矿物 | **2.5%** | 96.9 ≤ roll < 99.4 | 274 |
| 9 | 更多绿宝石 | **0.5%** | 99.4 ≤ roll < 99.9 | 275 |
| 10 | 晶球 | **0.1%** | roll ≥ 99.9 | 276 |

池内二级抽取为**加权随机**（`ftwwWeightedChoice`，`:248-257`）；数量为 `min + floor(rand*(max-min+1))`（`ftwwRandomCount`，`:259-263`）。物品一律 `Item.of(id, count)`（`:359`）→ **不带附魔、不带 NBT、不带自定义名称**。

**产出池（逐项，weight 为该池内权重）**

*② 杂物池 `FTWW_JUNK_REWARDS`（`:26-35`，权重合计 100）*

| 物品 id | 数量区间 | 权重 | 池内占比 | 绝对概率(×22%) |
| --- | --- | --- | --- | --- |
| `minecraft:cobblestone` | 1–5 | 22 | 22% | 4.84% |
| `minecraft:stone` | 1–4 | 16 | 16% | 3.52% |
| `minecraft:gravel` | 1–4 | 16 | 16% | 3.52% |
| `minecraft:dirt` | 1–5 | 14 | 14% | 3.08% |
| `minecraft:stick` | 1–6 | 12 | 12% | 2.64% |
| `minecraft:rotten_flesh` | 1–3 | 8 | 8% | 1.76% |
| `minecraft:flint` | 1–2 | 6 | 6% | 1.32% |
| `minecraft:string` | 1–3 | 6 | 6% | 1.32% |

*③ 木材池 `FTWW_WOOD_REWARDS`（`:37-44`，权重合计 100）*

| 物品 id | 数量区间 | 权重 | 池内占比 | 绝对概率(×10%) |
| --- | --- | --- | --- | --- |
| `minecraft:oak_log` | 1–4 | 28 | 28% | 2.80% |
| `minecraft:spruce_log` | 1–4 | 24 | 24% | 2.40% |
| `minecraft:birch_log` | 1–4 | 20 | 20% | 2.00% |
| `minecraft:acacia_log` | 1–3 | 12 | 12% | 1.20% |
| `minecraft:dark_oak_log` | 1–3 | 10 | 10% | 1.00% |
| `minecraft:mangrove_log` | 1–3 | 6 | 6% | 0.60% |

*④ 食物池 `FTWW_FOOD_REWARDS`（`:46-55`，权重合计 100）*

| 物品 id | 数量区间 | 权重 | 池内占比 | 绝对概率(×10%) |
| --- | --- | --- | --- | --- |
| `minecraft:bread` | 1–2 | 20 | 20% | 2.00% |
| `minecraft:baked_potato` | 1–3 | 20 | 20% | 2.00% |
| `minecraft:apple` | 1–3 | 18 | 18% | 1.80% |
| `minecraft:carrot` | 1–4 | 16 | 16% | 1.60% |
| `minecraft:cooked_chicken` | 1–2 | 12 | 12% | 1.20% |
| `minecraft:pumpkin_pie` | 1–2 | 8 | 8% | 0.80% |
| `minecraft:cookie` | 2–6 | 5 | 5% | 0.50% |
| `minecraft:golden_carrot` | 1（固定） | 1 | 1% | 0.10% |

*⑤ 屎（`wishing_well.js:271`）*：`kubejs:poop` × **1–3**（`:271`；测试分支同参 `:628`）。物品注册于 `kubejs/startup_scripts/jiqi/animal_waste.js:4`，中文名"屎"（`kubejs/assets/kubejs/lang/zh_cn.json:59`）。

*⑥ 金币（`wishing_well.js:272`）*：`kubejs:coin` × **3–16**。注册于 `kubejs/startup_scripts/content/misc_items_and_food_balance.js:16`，中文名"金币"（`kubejs/assets/kubejs/lang/zh_cn.json:10`）。

*⑦ 随机种子袋（`FTWW_SEED_BAG_REWARDS`，`wishing_well.js:70-127`）*：**内嵌 56 项，每项 weight 均为 1**（等概率，池内各 1/56 ≈ 1.7857%；绝对概率 3% × 1/56 ≈ **0.0536%**），数量一律为 **1**。56 个 id 与 `kubejs/startup_scripts/farming/seed_packets.js` 中注册的 56 个 `farmingtales:*_packet` **一一对应、无缺无多**（已实测核对）。完整列表：

```text
wheat / beetroot / carrot / potato / flax / kernels / oat / barley / lettuce / tomato /
onion / strawberry / cabbage / hops / ancient_fruit / tea / yerba_mate_leaf / rooibos_leaf /
f_tomatoe / melon / pumpkin / rice / nether_wart / sugar_cane / cocoa_beans / glow_berries /
sweet_berries / foul_berries / apple / coffee / jalapeno / lemon / orange / starfruit /
young_coconut / grass / hoary / leek / spongekin / plum / walnut / blueberries / grape /
redbean / soybean / cucumber / canola / pepper / sweet_potato / houttuynia / pomegranate /
dragon_fruit / lime / lucuma / date / eggplant
（全部前缀 farmingtales:，后缀 _packet）
```

*⑧ 矿物池 `FTWW_MINERAL_REWARDS`（`:57-65`，权重合计 100）*

| 物品 id | 数量区间 | 权重 | 池内占比 | 绝对概率(×2.5%) |
| --- | --- | --- | --- | --- |
| `minecraft:coal` | 2–6 | 30 | 30% | 0.750% |
| `minecraft:raw_copper` | 2–5 | 25 | 25% | 0.625% |
| `minecraft:raw_iron` | 1–3 | 20 | 20% | 0.500% |
| `minecraft:redstone` | 2–6 | 12 | 12% | 0.300% |
| `minecraft:lapis_lazuli` | 2–5 | 8 | 8% | 0.200% |
| `minecraft:raw_gold` | 1–2 | 4 | 4% | 0.100% |
| `minecraft:diamond` | 1（固定） | 1 | 1% | 0.025% |

*⑨ 更多绿宝石（`:275`）*：`minecraft:emerald` × **2–8**。
*⑩ 晶球（`:276`）*：`farmingtales:geode` × **1**（注册于 `kubejs/startup_scripts/jiqi/machine_items.js:93`，中文名"晶球"）。

### 2.6 表现：粒子 / 音效 / 文案

结算通用（所有分支）：`minecraft:enchant` 粒子 ×36 ＋ `minecraft:entity.item.pickup`（0.8/0.55）（`:345-350`）。

| 分支 | 粒子（`force @a`，核心上方 y+1.0） | 音效（`@a`） | 玩家私聊文案 |
| --- | --- | --- | --- |
| 无事发生 | `minecraft:smoke` ×18，扩散 .35/.25/.35 | `minecraft:block.fire.extinguish` 0.7/0.7 | 三选一随机（`:295-300`）：`§7水面恢复了平静……这次什么也没有。` / `§8绿宝石沉入井底，愿望没有回应。` / `§7井中传来一声回响，仅此而已。` |
| 屎 | `minecraft:poof` ×24，扩散 .35/.35/.35 | `minecraft:entity.slime.squish` 0.9/0.65 | `§6愿望实现了？§r 获得 §f<名称> ×<数量>`（`:312`） |
| 金币 | `farmersdelight:star` ×18，扩散 .45/.45/.45 | `minecraft:entity.experience_orb.pickup` 0.9/1.2 | `§6井底闪过金光：获得 §e<名称> ×<数量>`（`:316`） |
| 种子袋 | `minecraft:happy_villager` ×28，扩散 .5/.35/.5 | `minecraft:item.crop.plant` 1/1.05 | `§a井水托起一份生机：获得 §f<名称> ×<数量>`（`:320`） |
| 矿物 | `minecraft:glow` ×26，扩散 .45/.45/.45 | `minecraft:block.amethyst_block.chime` 1/1.15 | `§b罕见愿望：获得 §f<名称> ×<数量>`（`:324`） |
| 更多绿宝石 | `minecraft:firework` ×40 ＋ `minecraft:happy_villager` ×24 | `minecraft:ui.toast.challenge_complete` 0.8/1.1 | `§a§l愿望成真！§r§a 获得 <名称> ×<数量>`（`:329`） |
| 晶球 | `minecraft:reverse_portal` ×55 ＋ `minecraft:glow` ×32 | `minecraft:block.amethyst_block.chime` 1/0.75 | `§d§l井水凝成晶球！§r§d 获得 <名称> ×<数量>`（`:334`） |
| 其它（杂物/木材/食物，fallback 分支） | `minecraft:splash` ×22，扩散 .45/.25/.45 | `minecraft:entity.item.pickup` 0.8/0.9 | `§7许愿结果：获得 §f<名称> ×<数量>`（`:338`） |

- 名称取 `stack.displayName`（`:305`）。物品发放用 `event.player.give(stack)`（`:307`）。
- 另有客户端表现：结算成功时向玩家发 `IAdisplay` 网络包（`:341`），由 `kubejs/client_scripts/ui/item_activation.js:1-5` 接收 → `Client.gameRenderer.displayItemActivation(item)` ＋ 播放 `minecraft:item.trident.return`。
- **没有全服公告**（无 `tellraw @a`）；粒子/音效因使用 `force @a` 而全服可见可闻，但文字提示只发给触发者。
- 控制台日志：`[FarmingTales][WishingWell] trigger=… player=… outcome=… reward=…`（`:286`）、`trigger=drop queued/accepted/rejected`（`:527,505,510,514`）、`trigger=right_click detected/accepted/rejected`（`:563,584,591,605`）、`trigger=command requested/accepted/rejected`（`:683,713,710,722,729,739,753`）。这些日志名属于"触发方式"而非事件名。

### 2.7 冷却与次数限制

- 右键：**12 tick（0.6 秒）** 本地 per-UUID 冷却（`global.ftwwRightClickCooldowns`，`:13,580-587,599`）＋ 原版物品冷却 12 tick（`:601`）。冷却期间静默取消，无提示。
- 丢弃：**无冷却**，但同一物品实体在 10 秒追踪窗口内只会触发一次（成功后立刻从队列移除，`:517`），且被扣到 0 后实体消失。
- **没有每日/周期性次数上限**，脚本中不存在按天计数逻辑。

### 2.8 管理员命令全表

命令树由 `ftwwBuildTestCommand` 统一构造（`wishing_well.js:759-783`），注册两次（`:785-791`）：

| 命令 | 权限 | 参数 | 行为 | 代码行 |
| --- | --- | --- | --- | --- |
| `/wishingwelltest` | 等级 2（`.requires(hasPermission(2))`） | 无 | 打开事件测试菜单 | `:761-767` |
| `/wishingwelltest list` | 等级 2 | 无 | 同菜单 | `:769-774` |
| `/ft wishingwell` | 等级 2（`ft` 根命令由多个脚本共用） | 无 | 同菜单 | `:790`、`:759-767` |
| `/ft wishingwell list` | 等级 2 | 无 | 同菜单 | `:769-774` |
| `/wishingwelltest trigger random` | 等级 2 | 无 | 按正式概率做 1 次随机抽取 | `:777` |
| `/ft wishingwell trigger random` | 等级 2 | 无 | 同上 | `:790,777` |
| `… trigger <key>`（两个入口相同） | 等级 2 | `key` ∈ `nothing` `junk` `wood` `food` `poop` `coin` `seed_bag` `mineral` `emerald` `geode` | **强制**触发指定事件，产出按该事件正式池抽取 | 事件表 `:610-621`、强制映射 `:623-635`、注册 `:778-781` |

执行细节（`ftwwRunTestCommand`，`:707-757`）：

- 必须由**玩家**执行；控制台/命令方块执行时 `ftwwSourcePlayer` 返回 null → 直接返回 0，无任何反馈（`:708-712`）。
- **不消耗绿宝石**（不涉及任何扣物品代码），但**会真实发放抽到的物品**（走同一个 `ftwwResolveWish` → `ftwwPlayResult` → `player.give`，`:755,307`）。菜单第 3 行也是这么写的：`§c测试不消耗绿宝石，但物品奖励会真实发放。`（`:686`）。
- **范围限制**：以玩家为中心的有界方块扫描找最近的核心——dx/dz ∈ [-12,12]、dy ∈ [-6,6]（共 25×13×25 格），取平方距离最小者（`ftwwFindNearestCore`，`:656-680`）。找不到时提示 `§c附近 12 格内没有找到许愿井核心。请站到许愿井旁再测试。`（`:730`）——注意文案写"12 格"，实际垂直方向只有 ±6 格。
- 找到核心后**仍要跑完整结构校验**，不通过则拦截并显示同一套错误粒子＋文案（`:734-741`）。
- 通过后先私聊 `§d[测试] §f正在触发许愿井事件：§e<key>`（`:754`），再以 `trigger=command:<key>` 结算（`:755`）。
- 菜单里"**[触发正常随机抽取]**"按钮实际执行的是 `/wishingwelltest trigger random`（`:690`），其余每个事件按钮执行 `/wishingwelltest trigger <key>`（`:699`）；菜单末尾提示 `§8文字用法：/ft wishingwell trigger <事件英文名>`（`:704`）。
- **没有** `clear` / `reset` / `reload` 等子命令；`/ft` 根节点由其它脚本共同注册（`kubejs/server_scripts/block_events/economy/selling_market.js:717`、`kubejs/server_scripts/seasons/*.js`、`kubejs/server_scripts/permissions/ftb_team_commands.js:697`），脚本注释说明保留独立入口 `/wishingwelltest` 正是为了不被共享树影响（`:787-788`）。

### 2.9 方块与配方

- 注册（`kubejs/startup_scripts/content/wishing_well.js:4-18`）：显示名"许愿井核心"，`soundType('stone')`，硬度 3.0，抗性 6.0，`requiresTool(true)`，`mineable/pickaxe` + `needs_stone_tool`，`lightLevel(4)`，碰撞箱两段（0,0,0→16,4,16 与 3,4,3→13,8,13），模型 `farmingtales:block/wishing_well_core`，物品稀有度 `uncommon`。
- 模型/贴图（`kubejs/assets/farmingtales/models/block/wishing_well_core.json`）：石砖底座 + 錾制石砖台座 + 金块四边沿 + 绿宝石献祭板，贴图引用原版 `stone_bricks` / `chiseled_stone_bricks` / `gold_block` / `emerald_block`。
- 配方（`wishing_well.js:793-803`）：`ServerEvents.recipes` 注册有序配方 `SES / CWC / SSS`（S=`minecraft:stone_bricks`，E=`minecraft:emerald`，C=`minecraft:chiseled_stone_bricks`，W=`minecraft:water_bucket`），输出 `farmingtales:wishing_well_core` ×1，配方 id 固定为 `farmingtales:wishing_well_core`。脚本外没有发现该方块的其他配方或战利品表（`kubejs/data/**` 内无相关文件）。
- 物品 tooltip（`kubejs/client_scripts/tooltips/wishing_well.js:4-13`）：标题 `§6§lxyj.nbt 多方块结构核心` ＋ 4 条操作/结构提示 ＋ 2 条提示语（"大多数愿望只会换来沉默或普通杂物。""种子袋属于低概率奖励，晶球仅有极小概率出现。"）。

---

## 3. Mod 自带许愿井（A：`qys_event:wishing_well`）

> 本节以 **12:58:32 的当前源码** 为准（`WishingWellBlock.java` 99 行）。与已安装 jar 的差异见 3.6。

### 3.1 方块与交互

- 注册：`ModBlocks.java:29-33`，`BlockBehaviour.Properties.copy(Blocks.STONE)` ＋ `.strength(2.5F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops()`，物品为 `WishingWellItem`（`WishingWellItem.java:12-25`）。
- 方块类：`WishingWellBlock extends Block`，无自定义方块状态、无 GUI、无 BlockEntity（`WishingWellBlock.java:29,35`）。
- 交互（`WishingWellBlock.java:37-87`）：
  1. `held.is(Items.EMERALD)` 为假 → 返回 `InteractionResult.PASS`（**不消耗、不提示、不做任何事**），`:42`
  2. 客户端 → `InteractionResult.SUCCESS`（`:43`）
  3. 非 ServerLevel/ServerPlayer → `CONSUME`（`:44-46`）
  4. 玩家身上有 `Items.EMERALD` 的物品冷却 → `CONSUME`（**无任何提示**），`:47`
  5. 非创造模式扣 1 颗绿宝石（`:48`）
  6. 施加 **40 tick（2 秒）** 绿宝石物品冷却（`:49`）
- 其它：`WishingWellItem.place()` 在放置失败时显示 `message.qys_event.well.placement_error`（`WishingWellItem.java:17-25`）；物品本身没有 tooltip。

### 3.2 随机分支与精确概率/产出（`WishingWellBlock.java:51-82`）

单次 `level.random.nextFloat()`，四个互斥区间：

| 分支 | 概率 | 内容 | 代码行 |
| --- | --- | --- | --- |
| 物品奖励 | **45%**（roll < 0.45） | `level.random.nextInt(7)` 七选一（每项 45%÷7 ≈ **6.4286%**） | 52-65 |
| 幸运 | **25%**（0.45 ≤ roll < 0.70） | 幸运 I，**1200 tick = 60 秒**（amplifier 0、ambient=false、visible=true、showIcon=true） | 66-68 |
| 预报 | **20%**（0.70 ≤ roll < 0.90） | 私聊明日预报（特殊天气 + 每日世界事件），不发物品 | 69-79 |
| 空 | **10%**（roll ≥ 0.90） | 仅提示被拒绝 | 80-82 |

**45% 物品分支的 7 个精确结果**（`WishingWellBlock.java:53-61`）：

| case | 内容 | 数量 | 绝对概率 |
| --- | --- | --- | --- |
| 0 | 石材池：`taggedBlockReward(serverLevel, STONE_REWARDS, Items.COBBLESTONE, 4, 12)` | 4–12 | 6.4286% |
| 1 | 木材池：`taggedBlockReward(serverLevel, WOOD_REWARDS, Items.OAK_PLANKS, 3, 8)` | 3–8 | 6.4286% |
| 2 | `minecraft:gold_nugget` | 2 + rand(5) = **2–6** | 6.4286% |
| 3 | `minecraft:amethyst_shard` | 1 + rand(3) = **1–3** | 6.4286% |
| 4 | `minecraft:iron_nugget` | 3 + rand(6) = **3–8** | 6.4286% |
| 5 | `minecraft:emerald` | **1** | 6.4286% |
| 6（default） | `minecraft:diamond` | **1** | 6.4286% |

- 物品一律 `new ItemStack(item, count)` → **不带附魔、不带 NBT**。
- 发放：`serverPlayer.getInventory().add(reward)`，失败则 `drop`（`:64`）；随后私聊 `message.qys_event.well.reward`（名称、数量）（`:65`）。
- **石材/木材池的实现**（`taggedBlockReward`，`:89-98`）：
  - 标签 `qys_event:wishing_well_stones`（`:30-31`）与 `qys_event:wishing_well_woods`（`:32-33`）；
  - 从标签中**随机取一个元素**，且**必须是 `BlockItem`**（`:94`），否则回退；
  - 回退物品：石材 = `minecraft:cobblestone`，木材 = `minecraft:oak_planks`；
  - 数量 = `min + rand(max-min+1)`，并 **`Math.min(请求数量, 该物品默认最大堆叠数)`**（`:96-97`）。
  - **当前实例中这两个标签没有任何数据文件定义**（全实例搜索 `wishing_well_stones` / `wishing_well_woods` 只命中源码与 `README.md`；mod 自身的 `src/main/resources/data/qys_event/` 下也没有 `tags/` 目录）→ 运行时会走 `orElse` 回退，即实际产出**圆石 4–12 / 橡木木板 3–8**。见 6.2。

### 3.3 固定附加表现（所有分支，包括"空"）

- 粒子：`ParticleTypes.HAPPY_VILLAGER` × **10**，位置 = 方块中心上方 1.0 格（`pos.x+0.5, pos.y+1.0, pos.z+0.5`），扩散 `0.35/0.35/0.35`，速度 `0.02`（`WishingWellBlock.java:83-84`）。
- 音效：`SoundEvents.AMETHYST_BLOCK_CHIME`，`SoundSource.BLOCKS`，音量 **0.8**，音高 **1.15**（`:85`）。
- 返回 `InteractionResult.CONSUME`（`:86`）。
- 分支文案：`message.qys_event.well.reward`（`:65`）、`message.qys_event.well.luck`（`:68`）、`message.qys_event.well.forecast`（`:78-79`）、`message.qys_event.well.refused`（`:81`）。

### 3.4 冷却 / 次数限制

- 仅 **40 tick（2 秒）** 的绿宝石物品冷却（`:39,47`）。**没有任何每日次数限制**。

### 3.5 是否强制世界事件

- **不强制**（类注释也写明 never force-starts an event，`:28`）。
- 每日世界事件：`DailyWorldEventManager.forecast(server, 1)` 是**纯查询**（`DailyWorldEventManager.java:71-81` → `candidate`（`:43-46`）与 `DailyWorldEventRules.triggers`），不写状态。
- 天气：`WeatherManager.forecastTomorrow()` 有**副作用**——若 `data.forecastDay != targetDay`，会写入 `forecastDay / forecastTerm / forecastOrdinaryWeather / forecastEvent` 并 `setDirty()`，即**锁定明日天气（含是否发生特殊天气，概率 `specialEventChance`，本实例 = 0.15，冷却 `specialEventCooldownDays` = 2）**（`WeatherManager.java:422-437`）。次日 `WeatherManager` 直接用这份预报作为实际天气与事件（`WeatherManager.java:146-148`）。
  → 结论：许愿井的 20% 分支不会"指定"某个事件，但会**把明日的天气/特殊天气结果提前定死**（与天气预报机共用同一份锁）。配置项来自 `config/qys_event-common.toml`（本实例取值与默认一致）。
- 幸运分支：`MobEffects.LUCK` 1200 tick、amplifier 0（`:67`）。

### 3.6 与已安装 jar 的差异（提醒）

- 已安装 `mods/qys-event-forge-1.20.1-1.0.2.jar` 构建时间 **11:43:57**，早于源码最后修改（12:58:32）。
- 实测该 jar 内 `cn/farmingtales/qysevent/WishingWellBlock.class` 的常量池**不含** `wishing_well_stones`、`wishing_well_woods`、`getTag`，说明线上运行的仍是**旧的 45% 物品分支（5 选 1，无标签池）**版本。
- 因此：**文档若描述"石料/木材标签池"，请以"源码已实现但尚未重新构建进 jar"为前提**。

### 3.7 配方与掉落

- 配方（`data/qys_event/recipes/wishing_well.json`，单行 JSON）：`type=minecraft:crafting_shaped`，`category=misc`，pattern `["SGS","GEG","SSS"]`，key：`S`=`minecraft:stone`、`G`=`minecraft:gold_ingot`、`E`=`minecraft:emerald`，结果 `qys_event:wishing_well` ×1。→ 实际用料：**5 石头 + 3 金锭 + 1 绿宝石**。
- 掉落（`data/qys_event/loot_tables/blocks/wishing_well.json`，单行 JSON）：`type=minecraft:block`，1 次抽取，掉落 `qys_event:wishing_well` 自身，条件仅 `minecraft:survives_explosion`。
- 注：方块属性调用了 `requiresCorrectToolForDrops()`，但 `data/minecraft/tags/blocks/mineable/pickaxe.json` 与 `needs_stone_tool.json` 的 values **只包含 `qys_event:weather_forecaster`**，未包含 `qys_event:wishing_well`（两个文件各 1 行）。其挖掘掉落后果未在本次调研中实测，见 6.7。

---

## 4. 玩家可见文案（`assets/qys_event/lang/zh_cn.json`）

许愿井直接相关（键 → 值，行号取自该文件）：

| 行 | 键 | 中文值 | 使用处 |
| --- | --- | --- | --- |
| 47 | `block.qys_event.wishing_well` | `许愿井` | 方块名 |
| 51 | `message.qys_event.well.reward` | `许愿井回应了你：获得 %2$s 个 %1$s。` | `WishingWellBlock.java:65`（%1=物品名，%2=数量） |
| 52 | `message.qys_event.well.luck` | `许愿井赐予你幸运 I。` | `WishingWellBlock.java:68` |
| 53 | `message.qys_event.well.forecast` | `井中预言：特殊天气 %1$s；世界事件 %2$s。` | `WishingWellBlock.java:78` |
| 54 | `message.qys_event.well.refused` | `井水沉默了。也许今天不该许愿。` | `WishingWellBlock.java:81` |
| 119 | `message.qys_event.well.placement_error` | `§c许愿井放置失败。` | `WishingWellItem.java:22`（动作栏） |
| 120 | `message.qys_event.state.triggered` | `已触发：%s` | 被第 53 行的预报文案复用（`:74,:76`） |
| 121 | `message.qys_event.state.not_triggered` | `未触发` | 同上（`:73,:77`） |
| 5 | `modmenu.descriptionTranslation.qys_event` | `独立的节气天气、每日世界事件、预言机与许愿井系统` | Mod 列表描述（提及许愿井） |

（`message.qys_event.forecaster.*`、`message.qys_event.oracle.*` 等虽然也被同一预报组件使用，但属于天气预报机/预言机，不属于许愿井专属文案。）

**对比：KubeJS 许愿井没有任何 lang 键**——它的全部提示都是脚本内硬编码字符串（见 2.3/2.4/2.6/2.8），只在 `kubejs/assets/kubejs/lang/zh_cn.json` 中有奖励物 `kubejs:coin`="金币"（第 10 行）与 `kubejs:poop`="屎"（第 59 行）。

---

## 5. `kubejs/WISHING_WELL.md` 与代码的一致性核对

**结论：主体一致，1 处描述不符，2 处不完整（未写而非写错）。**

| MD 论述 | 代码事实 | 判定 |
| --- | --- | --- |
| 蓝图 `schematics/xyj.nbt`，7×5×7，核心 `farmingtales:wishing_well_core`，SHA-256 `15BB…0157F` | 全部实测吻合（见 2.1） | ✅ 一致 |
| Y-1 地基 5×5 石砖 + 四边中点外接楼梯朝向井心 | 脚本要求 25 格石砖 + 4 个楼梯 **id**；朝向不校验（MD 是建造指引，非校验规则） | ✅ 一致（校验更宽松） |
| Y0 井盆：四边中点下半石砖台阶、其余石砖墙、内圈 8 水、中心核心 | 完全一致（`wishing_well.js:185-198`） | ✅ 一致 |
| Y+1/Y+2 两层四角石砖墙 | 一致（`:201-209`） | ✅ 一致 |
| Y+3 屋顶外圈；边中点绿宝石块；边段双层台阶、四角下半台阶 | 与 NBT 完全一致（`:213-222`） | ✅ 一致 |
| "蓝图中的一个 `torchmaster:invisible_light` 仅用于照明，不参与结构校验" | 一致，且它的位置在相对 (+1,+1,-1)（NBT 4,2,2） | ✅ 一致 |
| 潜行右键显示搭建方法 | 一致（`:564-568`） | ✅ 一致 |
| 丢 1 颗绿宝石进内圈 8 格水 → 消耗 1 颗并许愿 | 一致（`:421-439,509-516`） | ✅ 一致 |
| 手持绿宝石右键核心，同样消耗 1 颗 | 一致（`:599-606`），但补充：**创造模式右键不扣**（`:600`） | ✅ 一致（MD 未写创造差异） |
| 一次丢一整组只消耗 1 颗、只抽 1 次 | 一致（`:401`） | ✅ 一致 |
| 结构不完整：不消耗，首个错误位置产生**红色**粒子 | 不消耗 ✅；粒子实为 `minecraft:angry_villager`（灰黑怒气云）＋ 低音音符盒音效，**不是红色** | ⚠️ **不符，以代码为准** |
| 管理员指令需权限等级 2、不消耗绿宝石但真实发物品 | 一致（`:761,686,755`） | ✅ 一致 |
| 玩家需站在完整许愿井核心 **12 格**范围内 | 水平 ±12 但**垂直只有 ±6**（`:665-678`）；且必须结构完整 | ⚠️ 不精确（文案本身也这么写，`:730`） |
| `/ft wishingwell`、`list`、`trigger random`、`trigger <10 键>`、`/wishingwelltest` 同功能 | 全部一致（`:759-791`） | ✅ 一致 |
| 概率表 39.9 / 22 / 10 / 10 / 7 / 5 / 3 / 2.5 / 0.5 / 0.1 | 与 `:267-276` 完全一致 | ✅ 一致 |
| —（MD 未提） | 各池明细与权重、屎 1–3、金币 3–16、绿宝石 2–8、种子袋 56 项等权重、右键 12 tick 冷却、丢弃 10 秒追踪窗口、12 格找玩家、`Math.random()` 随机源 | 📝 MD 未记载 |
| MD 标题写"（xyj.nbt）" | 一致；附带说明：脚本中 `FTWW_BLUEPRINT_SHA256`（`:18`）**声明后从未被使用**，仅作文档用途 | 📝 备注 |

---

## 6. 不确定项 / 未固定 / 来自配置

1. **源码正在被并发修改（最高优先级提醒）**：`_work/qys-event/src` 于 12:58:32 整体重写，本报告第 3 节基于该时刻之后的源码。已安装 jar（11:43:57 构建）不含新标签逻辑（3.6），若以"实际开服看到的 A 侧行为"为准，应重跑本节数据。
2. **`qys_event:wishing_well_stones` / `wishing_well_woods` 两个物品标签在实例内没有任何定义**（mod 资源、kubejs、datapack、世界存档全部搜索无果）。因此 45% 分支的 case 0/1 实际只会产出回退物 `minecraft:cobblestone` 4–12 与 `minecraft:oak_planks` 3–8。**未实测** 1.20.1 注册表对"完全未定义标签"返回空 `Optional` 还是抛错（源码按 `Optional.empty()` + `orElse` 回退处理）。
3. **`global.ftText` 未定义**：`wishing_well.js:129-134` 会优先调用 `global.ftText(message)`，但全 kubejs 搜索只有这一处引用，没有任何脚本定义它 → 实际总是回退到 `Text.of(message)`（纯文本，无整合包统一翻译包装）。
4. **`particle farmersdelight:star`**（金币分支，`:314`）依赖 FarmersDelight 注册的粒子 id；若该 id 不存在，`runCommandSilent` 只会静默失败（不影响奖励发放），本次**未实机验证**该粒子是否存在。其余粒子/音效 id 均为原版。
5. **随机源与幸运**：B 侧全部用 `Math.random()`（`:251,266`），**不受世界种子、`Luck` 效果、`randomTickSpeed` 影响**；A 侧用 `level.random`。若正式文档需要"幸运影响许愿"的说明，目前 **两套都没有**。
6. **`/ft` 根命令是共享节点**：由 `selling_market.js:717`、`seasons/warm_current_day.js:473`、`seasons/harvest_day.js:649`、`seasons/fish_tide_day.js:327`、`permissions/ftb_team_commands.js:697` 与本脚本共同注册；Brigadier 会合并同名 literal，但**具体合并结果与脚本加载顺序有关，未实测**。
7. **A 侧挖掘掉落**：`requiresCorrectToolForDrops()` 生效，但方块未列入 `minecraft:mineable/pickaxe` 与 `minecraft:needs_stone_tool`（标签文件只含 `weather_forecaster`）；实际能否掉落物品**未实测**，正式文档若写"用镐挖可掉落"建议先游戏内验证。
8. **蓝图里的 `torchmaster:invisible_light`**（相对 +1,+1,-1）是否可在游戏内放置、是否会被 Torchmaster 清理，取决于该 Mod 行为，本报告只确认"校验逻辑不看它"。
9. **A 侧预报的副作用范围**：`WeatherManager.forecastTomorrow` 会锁定明日预报并落盘（`:429-437`），因此"许愿井抽到预报"会**改变次日实际天气的生成路径**（不再重新随机）。这一点在类注释与 README 的"不会擅自强制世界事件"表述下属**未明说的副作用**，建议在正式文档中单独说明。
10. **未验证项**：56 个种子袋物品 id 与 `kubejs/startup_scripts/farming/seed_packets.js` 的注册列表已逐项核对一致，但**未验证**这些物品是否被其它脚本改写显示名/贴图；`kubejs:coin` 另有用途（`kubejs/server_scripts/economy/shop/coin.js:4` 潜行右键商店），奖励给玩家的金币可继续用于该商店，本报告未展开。
11. **数值来源标注**：A 侧唯一"来自配置"的数字是特殊天气概率（`specialEventChance`，`config/qys_event-common.toml:9` = **0.15**）与冷却天数（`specialEventCooldownDays`，`:12` = **2**），其余概率/数量/时长全部硬编码在 Java 或 JS 中，无配置项。

---

*报告结束。所有行号基于第 0 节所列哈希对应的文件版本。*
