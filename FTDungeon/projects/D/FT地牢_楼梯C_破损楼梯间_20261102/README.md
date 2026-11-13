# FT 地牢 · 楼梯 C「破损楼梯间」

**独立候选已制作；静态、Forge 原生四向实际放置与 100 tick 稳定性通过。未部署、未修改 FT 核心、未打开玩家存档、未写共享 incoming 或其他房间。**

本目录为 `D:/ft工程文件/FT地牢_楼梯C_破损楼梯间_20261102`。暂定 ID `STAIR_C_RUINED`，`assigned_slot=null`，不是已经注册、已经连路或已经部署的正式房间。不要把本目录内的验证模组装入游戏实例。

## 接收文件

| 文件 | 用途 |
| --- | --- |
| `out/room.nbt` | gzip / 单一调色板 / Java 1.20.1 / DataVersion 3465，完整 23×18×25 模板 |
| `out/room.json` | 暂定 ID、三个接口、安全内点、原图与结构哈希、未部署声明 |
| `out/preview.png` | 最终 NBT 与本机原版材质的软件剖视，不是原概念图 |
| `out/qa.json` | 静态、原生、稳定性、复建、回采对照和视觉总报告 |
| `out/blueprint.json` | 指定三格通路、残阶、碎块、断梁、栏杆、光源等精确坐标 |
| `out/materials.json` / `out/manifest.json` | 材料数量、文件字节和 SHA-256 |
| `preview/four-views.png` | 四方向剖视，只隐藏近墙及屋顶 |
| `preview/lower-cutaway.png` | 下层细节，明确只在预览中额外隐藏 Y≥10 方块 |
| `preview/upper-break-detail.png` / `preview/lower-gap-detail.png` | 上部断口和下行半堵梯的局部裁取，非修改交付几何 |
| `preview/interfaces.png` / `preview/plan.png` | 实际北南墙立面和三层通路平面，清楚标示三个开口 |
| `preview/exterior.png` | 完整封顶外壳；无阴影软件透视可能把门洞后墙看作外墙，应结合接口立面图 |
| `preview/native-world-capture.png` | Forge 100 tick 后真实回采 NBT 的软件预览，不是游戏截图 |
| `qa/native-placed-*.nbt` | 四向真实世界回采，已原生重新载入并独立逐格旋转比较 |
| `release/STAIR_C_RUINED-20261102.zip` | 白名单交付包，不含缓存、测试世界或模组 JAR |

## 来源与指纹

- 原图：`D:/ft工程文件/image2.5/dungeon-stair-rooms-20261102/out/stair-ruined.png`，1536×1024。
- 原图 SHA-256：`88c6aa37c0987bac1df530c263e96a0424528e3f51b3797b1c18ab5fb491fab5`。
- NBT SHA-256：`8dca52e4cb25f9ab8b7dec5f0450ac9d82b6b34391643b761f7f5dff18b3150e`。
- 只读采用 FT 接入契约 `D:/ft工程文件/FT地牢_Image25接入_20261101/CONTRACT.md`；来源 README、manifest、原图及契约副本在 `reference/`。
- 本次落实已有图，没有调用付费 image2.5 接口，没有访问或修改密钥/渠道配置。
- `tools/nbtio.py`、`tools/run_native.py` 与软件材质渲染器基础取自既有独立工程 `D:/ft工程文件/FT地牢_A_破损无人商店_独立交付_20261102` 的副本，仅在当前目录适配。没有复用其成品 NBT、没有修改其文件。

## 尺寸与接口

全部坐标是 NBT 原点相对整数 XYZ，表示**玩家脚位**，不是脚下地板。尺寸 23×18×25，共 **10,350 条完整且唯一的记录，包括空气**。

| 接口 | 脚位 | 内部安全点 | 朝向 | 净空 |
| --- | --- | --- | --- | --- |
| 主入口 | `(11,6,0)` | `(11,6,4)` | 北 | 3 宽×3 高 |
| 上层出口 | `(11,12,24)` | `(11,12,20)` | 南 | 3 宽×3 高 |
| 下层出口 | `(11,1,24)` | `(11,1,20)` | 南 | 3 宽×3 高 |

**主入口不是旧模板常见的局部 Y=1，而是 Y=6。** 接收方需按三个脚位高度分别设计道路。若现有道路脚位世界高度为 `H`，则模板原点应为 `H-6`；上层脚位为 `H+6`，下层脚位为 `H-5`。不得沿用“原点统一为 65 即可”的旧房间假设，也不能只把 NBT 放入 JAR 就声称完成连路。

## 设计落地与安全

- 保留苔石、开裂石砖、上行断阶、下行倒柱碎块、破红毯、残栏、木桶、壁火把、灯笼与贴墙蛛网。四个木桶均空，无奖励库存、战利品表、刷怪笼、命令方块或实体。
- 上行共六级：原五列梯的西侧两列在顶端两级缺失，四格断口真实存在；东侧 **X=5…7** 保留连续三格宽通路。断口沿边以砌体及顶平台栏杆防跌落，不要求跳跃或临时垫方块。
- 下行共五级：外侧 **X=18…19** 被落柱堆块占据，内侧 **X=15…17** 保留三格宽可通行缝隙；不是把障碍放在与梯道无关的角落。所有残阶下方均为完整砌体。
- **24 处**碎块与堆叠残块接触地面、阶面或下层残块；不用砂砾等重力方块。侧墙、护颊、**30 处**栏杆和桥端封护避开指定路线。
- 断梁保留东墙锚固端，另一端落在接地的砌体墩上，非凭空漂浮。概念斜断梁以原版整格梁、楼梯端面及废墟支墩转译。
- 破洞冷蓝光转译为封闭浅蓝玻璃及海晶灯上沿，避免为复刻透光而破坏完整外壳。顶部 575 格与底板 575 格均完整，侧墙仅三处声明接口为空气。
- 三盏壁火把、两盏落地灯、两盏完整链条吊灯与六处贴墙蛛网均检查合法支撑。无明火、熔岩、水、粉雪或通路蛛网。

## 验收证据

1. `qa/static.json`：**36/36 PASS**，含原图指纹/尺寸、gzip 单调色板、合法体积、完整地板/屋顶、声明开口、空容器、三格路线、残阶支撑、碎块接地、断梁锚固与支墩、灯链和蛛网。
2. `qa/native.json`、`qa/forge-gametest.log`：**Java 17.0.8 / Minecraft 1.20.1 / Forge 47.4.10** 隔离 GameTestServer，真实 `StructureTemplate.load/placeInWorld/fillFromWorld`，不是仅解码或 DataGen。
3. **0°/90°/180°/270°** 各实际放置一次。先用与 FT 规则一致的 `UPDATE_KNOWN_SHAPE` 避免放置中间态拆件，再对每条记录执行正常邻居更新；所有测试单元区块强制 ticking，物品掉落保持启用，等待 **100 tick**。每向逐格检查 10,350 条，合计 **41,400 条**；每向四个合法空木桶、零意外实体，全部状态和原生 `canSurvive` 通过。
4. 每向使用原生 **0.6×1.8 玩家 AABB**、每格 1/8 水平采样、每次最多半格抬降，图边必须能反向通过，不使用跳跃或大落差。**173 个**指定三格路线脚位与三个接口/安全点全部可往返。此为保守采样碰撞证明，不冒充真人键鼠实测。
5. 六类负例各向一次，共 **24 个**：缺台阶支撑、石块堵主路、碰撞形状为空的蛛网堵主路、断灯链、缺断梁支墩、堵下行保留缝隙。均被识别，随后恢复；不会在正常验收时忽略掉落物。
6. 四份 100 tick 回采均原生重新载入成功；`qa/reproducibility.json` 再由独立 Python 旋转映射核对四向所有方块状态，共 41,400 条，并复建 NBT/元数据/布局/材料得到字节相同哈希。
7. 最终日志明确包含 `STAIR_C_NATIVE_GAME_TEST_PASS: rotations=4/4, records=41400, ticks=100, negative_probes=24`、`All 1 required tests passed`、`BUILD SUCCESSFUL`。视觉核对记录在 `qa/visual-review.json`。

早期低位测试单元碰到测试世界自然矿井/地形的种子、浆果、铁轨等无关掉落物，不能当成房间通过。失败报告/日志保留在当前 `qa/previous-*` 供追溯，随后把单元移到空中、在放置后照常执行完整邻居更新与 100 tick；最终日志及全部回采均来自通过的独立空中单元。交付 ZIP 仅包含明确命名的最终证据，不混入失败轮次或测试世界。

## 重建与复测

本机使用既有 Python/Pillow、Java 17、Gradle 8.8 和 Forge 缓存。在当前工程目录执行：

```powershell
$python = 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe'
& $python tools/build_room.py
& $python tools/qa_static.py
& $python tools/run_native.py
& $python tools/render_delivery.py
```

`run_native.py` 默认 Gradle 离线模式，仅启动本目录 `qa/forge-run` 内的无界面测试服务器；复测前归档前次最终报告和日志。不会启动 Minecraft 客户端或访问玩家存档。异机可传 `--java-home` / `--gradle`，须具备匹配依赖；Forge 自身的版本检查/认证端点仍可能尝试网络访问，不把 Gradle 离线称为整个 JVM 断网。

实际看图并更新本轮 `qa/visual-review.json` 后，再运行 `tools/finalize_delivery.py` 与 `tools/package_delivery.py`。视觉记录不能由未来脚本自动伪装成人工复核；任一几何或 PNG 改动后都需重看。软件渲染只读本机原版 `client.jar` 方块模型/材质，不分发客户端 JAR。

## 接入和回滚边界

本候选不改核心 catalog、固定槽位、连接路、门、奖励、难度或生成标记；没有部署 JAR，没有写玩家世界，没有覆盖其他制作线程。由唯一接入负责人确认正式 ID/槽位/上下层连接路后，再做完整体积、液体、实体、世界高度/边界占用预检及 FT 原生接入验收。NBT 的显式空气不是许可清空命令，测试夹具清单元代码严禁搬进正式施工逻辑。

本次不证明大厅连通、核心幂等/占用拒绝、返程、旧存档迁移、五档难度、完整整合包实机截图、真人联机或 FPS。当前回滚只需撤回本独立候选；未来正式部署须停服、备份和保留原 JAR，不能盲删 SavedData 或生成标记，也不能把卸载模板当作自动拆建筑。
