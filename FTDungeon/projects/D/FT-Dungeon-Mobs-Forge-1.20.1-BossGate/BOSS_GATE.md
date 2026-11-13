# Boss 房单向集结门 · 0.20.1

Minecraft Java 1.20.1 / Forge 47.4.10 / Java 17。日期：2026-11-02（Asia/Shanghai）。

## 玩家行为

1. 建图者将“Boss 房集结门”放在走廊与 Boss 房之间，并绑定房间可站立空间及集结落点。
2. 只有已开始的地牢轮次中，配置完整的门才打开。玩家从走廊一侧真正走过门的中心平面后开始集结；靠近、站在门旁或右键均不会触发。
3. 将当前 `ftdungeonmobs:dungeon` 内全部在线且存活的玩家传送进同一 Boss 房，不按门的距离筛人；已在房间中的玩家也参与这次集结。主世界、下界及其他维度的玩家不受影响。
4. 每个人使用独立安全落点，清除速度与坠落距离。全队预检失败时不开始传送，并将触发者挡回门前；如果传送调用被运行扩展拒绝，则尝试将已移动成员退回原位，不把本轮标为已集结。
5. 门只能正向通行，背面有实体碰撞和反向越线纠正。首次集结成功后，所有 Boss 门关闭，本轮只激活这一间 Boss 房。门沿用原有 40 tick 铁栅升降和防夹逻辑。
6. 后加入、在地牢断线后重连、或在地牢重生的玩家，会在下一次每秒检查中补传至本轮 Boss 房。死亡界面的玩家等重生后处理；离线玩家本身不能立即传送。若此时缺安全空间，保留待传状态，等有空间时重试。
7. 保留入场时的原维度、返回坐标、冷却记录与冒险模式保护。`/ftdungeonreturn` 应急返程仍可用；本次不增加“击杀 Boss 才允许离开”的限制。
8. 沿用本轮锁定的 1～5 档地牢维度难度，不重新抽档、不重置普通房间刷怪点。轮次结束清除本轮集结标记，下一轮重新启用；地图的门与房间绑定保留。

## 获取与建图

- 方块及物品 ID：`ftdungeonmobs:dungeon_boss_gate`。出现在“FT地牢怪物”和原版“功能方块”创造栏。
- 门宽 3 格、高 4 格，坐标指底部正中格。复用已有地牢入口门的模型、贴图和动画，不新增贴图或叠加几何。
- 既有地牢没有指定的独立 Boss 房，因此不会擅自把 L1～L4 改成 Boss 房，也不会清空或自动重建旧房间。需要管理员指定目标房间；下面的例子只绑定现有建筑，不创建 Boss 房本体。
- 地牢冒险保护会阻止普通放置，建图时使用 OP2 命令创建门；服务器控制台也可使用 `execute in`。不需关闭保护。

### 示例

假设已经有一间房：地板为 Y=63，室内可站立空间是 X=4088..4104、Y=64..69、Z=4100..4114，走廊在北侧。以下数字是示例，必须换成自己建筑的坐标。

`/execute in ftdungeonmobs:dungeon run ftdungeon bossgate create 4096 64 4096 north`

`/execute in ftdungeonmobs:dungeon run ftdungeon bossgate bind 4096 64 4096 4088 64 4100 4104 69 4114 4096 64 4104`

`/execute in ftdungeonmobs:dungeon run ftdungeon bossgate status`

`/execute in ftdungeonmobs:dungeon run ftdungeon bossgate remove 4096 64 4096`

- `create <门底中坐标> <north|south|east|west>`：朝向是门正面、即走廊方向；Boss 房在相反方向。检查 3×4 空、干燥、无生物空间与底部三格支撑后才放置，绝不覆盖方块。
- `bind <门底中坐标> <房间最小 XYZ> <房间最大 XYZ> <集结中心 XYZ>`：最小/最大坐标均为包含端点的**室内空域**，不是墙体或地板坐标。房间必须全部在门背面，离中心平面至少 1 格；落点在范围内。
- 尺寸限制：X/Z 3～128 格、Y 3～64 格。安全落点只在给定范围内、以中心为基准水平 16 格和上下 2 格搜索。站立需干燥坚固支撑、头部空间、世界边界内，无岩浆/火/仙人掌/粉雪/营火等危险与其他生物阻挡；人数过多或局部太窄就拒绝集结。
- `remove` 只移除绑定，不删除门和房间。集结已开始时不能改绑或移除，需让本轮正常结束。
- 可保存最多 64 个门绑定；一轮首次通过者确定该轮唯一 Boss 房，其余门随之关闭。入口五分钟准入窗口的现有规则不变。

## 开发接口

所有读写与操作必须在服务器主线程上，作用维度固定为 `DungeonGateTravel.DUNGEON`。

| 接口 | 用途 |
| --- | --- |
| `DungeonBossGateApi.Binding(gate, facing, minimum, maximum, arrival)` | 不可变房间坐标；构造时验证朝向、尺寸、落点与门背面 |
| `DungeonBossGateApi.create(level, anchor, facing)` | 安全放置现有 3×4 门；不覆盖场景 |
| `DungeonBossGateApi.bind(level, binding)` / `unbind(level, anchor)` | 注册、修改或移除绑定；活动 Boss 阶段拒绝改动 |
| `DungeonBossGateApi.state(level)` | 当前轮状态入口，自动对齐轮次；查询 `engaged`、`activeBinding`、`arrived(UUID)` |
| `DungeonBossGateApi.canOpen(level, anchor)` | 门的开闭条件；不触发传送 |
| `DungeonBossGatherEvent` | Forge EVENT_BUS 上的可取消事件，在全队落点预检后、首次移动前发出 |

事件的 `level()`、`binding()`、`players()`、`landings()`、`roundId()`、`tier()` 提供本次维度、房间、玩家快照、落点快照、本轮 UUID 与难度。集合不可修改。监听器可调用 `setCanceled(true)` 拒绝整次集结，不应在事件内递归传送或直接修改轮次；门和落点会在事件后再次检查。补传迟到玩家同样经过该事件，但玩家列表仅含本次待补传成员。

本次不新增自动生成 Boss、奖励发放、战斗波次或第三方 Boss 依赖；这些系统可独立监听事件并关联既有轮次 UUID，不把完整 Boss 塞进普通房间刷怪池。

## 存档与兼容

- 新数据在地牢维度的 `data/ftdungeonmobs_boss_gates.dat`，保存门/房间绑定、活动门、轮次 UUID 与已到达玩家 UUID。
- 原入口/返回门、旧房间、已消耗刷怪组、实体模型和动画 ID 保持不变。旧存档无新数据时默认没有 Boss 门，不会自行施工。
- 地牢维度难度补丁一并保留；火盆不再给周边半径内怪物加成。完整行为见 `DIMENSION_DIFFICULTY.md`。
- 模型提供者“一颗大豆，b站uid355533780”只对应原始骷髅模型，不作为整个 Mod 作者署名。
- 源码 ZIP 不打包第三方依赖、测试世界或 Gradle 缓存。重新构建时将原 0.20.0 工程的 `libs/` 依赖复制到解压后的同名目录，并配置 JDK 17；执行 `./BUILD.ps1 -Task build -Offline`。若机器没有依赖缓存，先联网构建一次。
- 本版是独立开发副本和交付候选；未替换游戏实例的 0.20.0，未启动完整整合包客户端，未打开玩家存档。安装需双方同版并完整重启，先备份存档；使用新门后的世界不宜直接移除 Mod 或降级。
