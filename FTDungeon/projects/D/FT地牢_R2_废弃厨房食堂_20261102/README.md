# R2 · 废弃厨房食堂（独立制作，未部署）

## 交付与规格

四个接入文件位于 `out/`：`room.nbt`、`room.json`、`preview.png`、`qa.json`。完整指纹和证据索引在 `handoff.json`。

- Minecraft Java 1.20.1 / DataVersion 3465，gzip 压缩，单一调色板，仅用原版方块，无预放实体。
- 模板尺寸按 XYZ 为 **15×9×15**，共 2025 个唯一坐标（含空气）。原设计是近似中型概念图；适度扩展到 15×15 占地以容纳双餐桌和安全通路。
- 北入口玩家脚位 `[7,1,0]`；内部安全脚位 `[7,1,3]`。北接口三格宽、三格高，中央 X=6…8、Z=0…11 保持同样净空。
- 两张五格长餐桌、四条长凳、墙边餐具架、草药悬架、洗涤盆、四只空木桶、两台空烟熏炉、石罩炉灶与吊锅。裂纹/苔石地板、四处高位蛛网、角落断凳和倚墙板体现废弃凌乱，不堵主路。
- 桌端用倒置楼梯形成八处落地支撑，桌面上半砖连接两端；长凳直接落在完整地板上。吊灯、草药、吊锅链条有真实顶锚，架子贴实墙。
- 炉灶使用不传播火势的原版营火，石底座、两侧烟熏炉、背墙及正面铁栏围护。主路不经过火源；无普通火/魂火方块、熔岩、可流动水或水浸方块。三组蜡烛同样不传播火势。水仅存在于原版水炼药锅中。
- 只有七个方块实体：4 木桶、2 烟熏炉、1 营火，库存为空、无 LootTable、无奖励、无刷怪笼或命令装置。
- 最终 NBT 保留完整地板、四面外壳（仅北入口开口）及 **225 格完整屋顶**；预览剖切只发生在内存副本。

## 来源

只读使用既有 R2 图片，没有调用付费生图 API：

`D:\ft工程文件\image2.5\dungeon-rooms-medium-random-thread3-20261102\out\R2-abandoned-kitchen-mess.png`

1536×1024；源图 SHA-256：`8dab754ca881b61b789b78e3cfe09f12f26f6be4bbc56ea583f298cdb244865d`。

原图的陶盘、食物、布袋和悬挂食材改用原版花盆、盆栽、空桶和草药外观表达，不塞入可领取物品或用浮空掉落物充当装饰。没有声称精确复刻 AI 图中原版不存在的模型。

## 验证与证据

- `qa/static.json`：读取实际 NBT，核对源图/结构指纹、完整体积、地板和屋顶、入口、通道、家具支撑、悬挂件、火源及空库存；`qa/reachable.json` 和 `preview/plan.png` 给出干燥脚位。
- `qa/forge-native.json`：Minecraft 1.20.1 / Forge 47.4.10 / Java 17 的隔离 GameTest；注册表和全部属性解析、`StructureTemplate` 往返、四个方向 `placeInWorld` 原生放置、真实支撑与 0.6×1.8 玩家碰撞检查。
- 稳定性阶段保持 `doFireTick=true`，强制邻居通知及原生方块形状重算，观察 240 tick，复查所有状态、空库存、支撑、屋顶、无掉落物、无火势及流体外溢。结果以最终原生日志及 JSON 为准。
- `qa/native-placed-*.nbt`：四向在 240 tick 后从真实测试世界回采。`qa/native-capture-comparison.json` 独立比较旋转后的全部 8100 个世界状态；输入省略的四处悬根默认属性 `waterlogged=false` 按原生解码结果显式归一后比较，报告记录此语义归一，不声称序列化字节相同。`qa/native-roundtrip.nbt` 是原生往返证据，不替换交付 NBT。
- `qa/render.json`：实际 NBT + 原版方块模型/材质的软件预览来源和资源清单；`preview/four-views.png`、`preview/exterior.png`、`preview/native-world.png` 及 `qa/visual-review.json` 为目视核查证据。
- 失败过程完整保留：`qa/forge-attempt-1-missing-empty.log`（缺 pack.mcmeta，夹具未加载）、`qa/forge-attempt-2-template-namespace.log`（重复模板命名空间）、`qa/forge-attempt-3-exterior-environment.log`（首次真正放置时发现测试模板外的既有环境流体，非房间内状态变化）。最终夹具仅在已经校验类型的 GameTest 世界内清理每次放置范围及两格缓冲，不对正式存档做清理；240 tick 后外圈依然无流体/火。最终证据为修正后 `qa/forge-run.log`，包含 `R2_FORGE_PLACEMENT_PASS`、`All 1 required tests passed` 和 `BUILD SUCCESSFUL`，前三次失败不算通过。

软件渲染**不是客户端截图**，不模拟整合包资源包、动态烟火粒子、实际光照或 FPS；原生隔离测试不等于整合包多人实机游玩验收。

## 边界与后续接入

仅在本独立工程内编写源文件与交付。测试世界只在 `forge/run/r2-only-gametest/`，且测试入口校验 `GameTestServer` 类型。没有打开、清空、施工或修改正式玩家存档，没有修改 FT 核心、其他房间、已部署 JAR、正式配置、连接密钥或 image2.5 配置。

供接入负责人后续处理：登记唯一 R2 ID、从大厅可步行到北接口的连接、独立持久生成标记和完整目的地体积（包括空气坐标）占用/液体/实体拒绝。**NBT 内有空气不授权清空世界；未协调前不得部署或覆盖已生成房间。**

## 可复现

使用本机已配置 Python/Pillow，依次运行 `tools/build_r2.py`、`tools/qa_static.py`、`tools/run_forge.ps1`、`tools/render_r2.py`，目视确认图片并记录 `qa/visual-review.json` 后，运行 `tools/finalize.py`。最终器会要求四向原生通过、世界回采精确匹配、预览与结构指纹一致，缺证据就失败而非生成伪成功。

`run_forge.ps1` 使用本机 Minecraft Java 17、Gradle 8.8 和现有 Forge 依赖离线缓存，不启动游戏客户端；工具缓存/Gradle 日志可能按工具正常行为更新，不属于并行房间或生产文件修改。NBT 工具和渲染引擎分别从既有 S6、H 独立工程复制，只改本工程副本；原来源工程未改。
