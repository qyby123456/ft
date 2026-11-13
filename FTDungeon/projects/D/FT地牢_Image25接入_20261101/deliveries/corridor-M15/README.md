# Corridor M15 · 苔蚀黑石断拱烛廊

2026-11-03（Asia/Shanghai）独立制作；**未部署、不接入核心、不读取或修改玩家存档**。仅在本交付目录运行专用 Forge GameTest 世界。

## 交付四件套

本目录根部的 `room.nbt`、`room.json`、`preview.png`、`qa.json` 为交付四件套。`preview.png` 从最终 NBT 经 Minecraft Builder / Lodestone 实际渲染，不是概念图或游戏客户端截图。

| 字段 | 结果 |
| --- | --- |
| ID | `corridor-M15`（原图编号 `M15`） |
| 尺寸 | `15 × 12 × 40`；Z 轴直廊，长 40 格 = 2.5 区块 |
| 北入口脚坐标 | `[7,1,0]`，朝向 `north` |
| 内部安全点 | `[7,1,2]` |
| 南出口脚坐标 | `[7,1,39]` |
| 全程净空 | `x=5..9, y=1..5, z=0..39`；5 格宽、5 格高，无转弯、分岔或地面楼梯 |
| 结构格式 | gzip vanilla structure NBT，单调色板，Java 1.20.1 / DataVersion 3465 |
| 显式记录 | 7200，含 3615 空气；初始非空气 3585 |
| 调色板 | 24 种状态；Minecraft 原版方块，Forge 原生逐属性校验 |
| NBT SHA-256 | `3bde7a0d8d39e57c359a861364be2899722d3c1607e64fd5341e7cfe21ad7620` |

## 参考与转译

依据 `D:/ft工程文件/image2.5/dungeon-corridors-20261103/README.md`、`SPEC.md`、`manifest.json`、`out/corridor_index.json` 及本编号提示词：`blackstone-deepslate × overgrown × candles × broken-arches`。

- 原图：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M15-blackstone-deepslate-overgrown.png`。
- 已核验原图 SHA-256：`501484bf3ef3bc469ec605adcadc0170fda464876eb3dcb94641d26a115b250b`。
- 黑石砖双层墙、深板岩砖/瓦地面、雕纹柱座与 7 道拱肋；两道局部断拱只破坏装饰肋，连续屋顶完整保留。
- 六组对称壁龛共 12 簇点燃蜡烛；可见烛台前移到侧缘，不侵入主路。藤蔓、固定树叶、红树根和边缘苔毯形成蔓生层；少量坠石仅位于两侧。
- 暗铁意象以有顶锚定的链条表现；没有新增怪物、战利品、容器、命令方块或刷怪笼。
- 使用已存在的参考图，本任务不调用付费图像 API。

## QA 与真实范围

`qa.json` 汇总以下独立证据，均绑定当前 NBT 完整指纹：

1. `qa/static-qa.json`：11 项静态检查，包含原图指纹、完整体积、封闭侧墙、连续屋顶/地板、5×5 直通净空、双端步行连通、危险物排除、植物/烛台/链条支撑。
2. `qa/native-registry-qa.json`：Minecraft 1.20.1 / Forge 47.4.10 原生 NBT 解码、24 状态注册表及精确属性检查，保留原生形状数据。
3. `qa/native-world-qa.json`：隔离 GameTest 世界对四旋转 `NONE / CLOCKWISE_90 / CLOCKWISE_180 / COUNTERCLOCKWISE_90` 实际 `StructureTemplate.placeInWorld` 放置；每方向检查 7200 记录（每轮合计 28800），实际 `canSurvive`、完整顶/底、12 簇蜡烛、200 个 0.6×1.8 玩家碰撞体脚位。全体邻居更新、4 轮显式植物原生 randomTick、200 游戏 tick 后复检，最后回滚到预先验证的空测试场地。
4. `evidence/forge-gametest.log`：实际成功日志，包含 `M15_NATIVE_QA_PASS` 与 `BUILD SUCCESSFUL`。
5. `qa/view-render-qa.json`：8 张 1600×1000 Lodestone 内外图、相机参数、实际渲染记录数与浏览器错误检查。

**植物自然变化不隐瞒**：200 tick 后四方向记录分别有 31 / 29 / 31 / 25 个自然植物状态变化，包括固定树叶 distance 重算和藤蔓生长；允许这一原生行为，但不允许蜡烛/附件脱落、结构块变化或主路受阻。四方向 5×5 净空、12 簇蜡烛、顶底和玩家通行均再次通过。不是“所有方块状态完全不变”的宣称。

**启动修复留痕**：`evidence/forge-gametest-attempt1-missing-fixture.log` 保留缺失数据包元信息导致的空模板不可用记录；`evidence/forge-gametest-attempt2-namespace.log` 保留错误手写模板命名空间的失败。最终补入 `pack.mcmeta`、恢复 Forge 自动命名空间，并仅在专用测试目录限制并发和关闭 Forge 更新查询后成功。不把失败日志当通过证据。

**未覆盖**：完整整合包客户端帧率/光影/粒子、多人游玩、FT 大厅连接、核心导入幂等/占用拒绝业务逻辑、实际玩家世界部署。`native-test` 是仅用于验证的工程，不是待安装模组。

## 四向内外图

- `four-view-exterior.png`：北、南、东、西完整外壳，未剖切。
- `four-view-interior.png`：北/南为实际封顶内部透视；东/西仅展示时移除屋顶与近侧墙，便于查看侧龛、苔毯及拱肋。剖切未写回 NBT。
- `views/`：8 张独立图；`preview.png` 为北侧居中一字贯通透视。
- 渲染使用 Lodestone 默认材质包和展示光照；蜡烛无火焰粒子、植物色与真实 Forge 光照不完全等价，不冒充实机截图。

## 接入约束

此交付不提供部署操作。未来由核心负责人先检查完整旋转体积（**包括显式空气**）、世界边界、流体和实体；遇占用应拒绝，禁止为放置而清空现有世界。以独立 ID 持久化首次生成标记，失败须可诊断且不写成功标记，已生成不得覆盖玩家改动。旋转枢轴为模板 `[0,0,0]`，入口/出口及方块状态必须同旋转；三个坐标均是玩家脚部而非地板。

## 复现

在本机 PowerShell 执行 `./Run-QA.ps1 -Rebuild` 可依次重新运行：Minecraft Builder recipe → 静态 QA → 隔离 Forge GameTest → Lodestone 八向截图 → 四向汇总图与指纹清单。依赖已安装 Builder、Node、运行时 Python/Pillow、Edge 与本机已缓存 ForgeGradle/JDK17；离线构建，不写 `mods` 或玩家 `saves`。所有生成结果仅在本目录。

生成源码在 `tools/build-m15.mjs`，测试源码在 `native-test/src/main/java/net/farmingtales/m15qa/`。NBT 工具 `tools/nbtio.py` 来自本地既有 M08 独立工程；结构生成本身仍由 Minecraft Builder / Lodestone 执行。

`manifest.json` 列出四件套、证据、图像及主要复现文件的 SHA-256；改变 NBT 后必须重新生成匹配的全套证据。
