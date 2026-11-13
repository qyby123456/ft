# P03 · 熔岩泡跳台

独立房间交付，Minecraft Java 1.20.1，DataVersion 3465，Forge 47.4.10。房间尺寸 **27×13×29**，完整屋顶、四座错位 3×3 跳台、五次等高跳跃。基于指定 image2.5 原图制作，没有再次调用生图 API。

## 入口、出口与玩法

坐标均为模板局部 XYZ **玩家脚部坐标**：

- 北入口 `[13,5,0]`，内部安全点 `[13,5,3]`，南出口 `[13,5,28]`。
- 两端中央开口均为 3 格宽、4 格净高，完整方块地板位于 y=4；接入走廊脚位必须对齐 y=5。
- 从北平台沿四座跳台抵达南平台，台面脚位统一 y=5。五段起落点距离依次为 2、√5、√5、√5、2 格，最大 2.236 格；实际方块边缘空隙主要为 1 格。
- 跳台范围依次为 x=10..12/z=7..9、x=13..15/z=11..13、x=10..12/z=15..17、x=13..15/z=19..21。每座表面镶嵌镶金黑石作为方向标记，底座为玄武岩并带菌光体。
- 起跳与落点、逐 tick 轨迹、玩家包围盒参数见 `qa/route.json`。保守轨迹水平速度最高约 0.2033 格/tick，起跳竖直速度 0.42，重力 0.08，竖直阻力 0.98；无需疾跑、药水或跳跃强化。

## 熔岩密封与失败恢复

17×17 共 **289 格真实熔岩源**位于 x=5..21/y=1/z=6..22；底部 y=0 为完整黑石，侧边完整黑石，上方 y=2 为完整玻璃方块。熔岩六向封闭，玩家不会接触液体，没有可燃材料或明火。

失足从台面脚位 y=5 降至玻璃面脚位 y=3，最大下落 2 格。池面及侧边共有 **336 个可站立安全格**，全部连通西北恢复阶梯。恢复阶梯位于 x=5..7/z=5..6，朝北，每半格台阶上升 0.5，通往北平台；可步行返回入口重新尝试。南平台比安全面高 2 格，没有南端恢复梯，恢复路线回到起点。

原图中的红地狱砖、黑石、玄武岩独立落地柱、铁链吊灯、熔岩与分散跳台均已落实。为保证重复尝试，将原图开放熔岩改为透明密封池；木箱意象改为不可燃的雕纹黑石陈设。成品保留双层完整屋顶，所有剖视只在渲染内存中移除屋顶与近侧外墙，不改 NBT。

## 文件和证据

- `room.nbt`：gzip 单调色板结构，含完整 10,179 格显式坐标（包括 air）。
- `room.json`：入口、出口、尺寸、原图指纹、恢复机制和接入要求。
- `preview.png`：实际 NBT 北向室内剖视；`views/` 包含四向室内、四向完整外观、南北视线、恢复阶梯和路线图。
- `qa.json`：汇总结果；`qa/static.json`：静态检查；`qa/route.json`：逐 tick 跳跃轨迹。
- `qa/native.json`、`qa/forge-gametest.log`：隔离 Forge GameTestServer 的真实放置、原生碰撞和稳定性证据。
- `qa/native-placed-*.nbt`：四旋转世界回采，与旋转后的源结构逐格一致，并由原生模板重新读取。
- `qa/visual-review.json`、`qa/image-decode.json`、`qa/views.json`：软件视图审查、图片解码与渲染源哈希。
- `qa/provenance.json`：原图、批次 README/manifest/verify 的指纹及来源关系。
- `SHA256SUMS.txt`：交付文件指纹，排除构建缓存、临时测试世界和校验清单本身。
- `tools/`：配方、静态检查、视图捕获、拼图和打包汇总脚本；`native-qa/`：仅用于复核的隔离测试工程。

## 验证范围

静态检查覆盖 NBT 尺寸/版本、显式体积、原版方块、禁用方块/实体、完整地板屋顶、入口出口、灯具支撑、六向熔岩密封、跳跃净空和全部恢复落点连通性。

Forge QA 对 NONE、CLOCKWISE_90、CLOCKWISE_180、COUNTERCLOCKWISE_90 执行原生 `StructureTemplate.placeInWorld`。强制区块 tick，并显式调度熔岩更新，等待 200 tick 后再次逐格对比，总计检查 40,716 格；检查流体外溢、掉落实体、灯具存活及模板回采一致性。五段跳跃、台上步行、落点下落和恢复阶梯使用真实原版碰撞形状扫过 0.6×1.8 玩家包围盒，每段子采样位移不超过 0.04 格。每个旋转还执行占用 air、移除熔岩底、移除玻璃盖、断开吊灯链、阻挡跳跃的负向探针。

测试世界只位于 `native-qa/run-isolated/world`，未加载玩家世界。首次验证覆盖中央 284 个落点，随后扩大至包含侧边的全部 336 个落点并将水平速度降至 0.2033 格/tick；旧证据保存在 `qa/first-run-284-central-catch.*`，最终结果以 `qa/native.json` 为准。

这是软件渲染、原生碰撞和隔离服务器证据；**没有执行完整 FarmingTales 客户端人工游玩、多人联机、帧率测试或 FT 核心接入测试**。

## 原图和固定指纹

原图：`D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/parkour/P03-parkour-lava-bubble-jumps.png`

原图 SHA-256：`2c844d5d29371dfc8e529c47aa77f3b61c48d64d3364aca57e365532e75dd6d3`

room.nbt SHA-256：`c95e063e7461496ab421f6c5e8d0642f9db3819330105db02fbcdd614927a161`

## 复核命令

从本目录执行：

```powershell
node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs ./tools/recipe.mjs --out-dir . --name room --data-version 3465
node ./tools/qa-static.mjs
powershell -NoProfile -File ./tools/run-native.ps1
node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs --nbt ./room.nbt --port 5213 --no-open
node ./tools/render-views.mjs
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' ./tools/make-sheets.py
node ./tools/finalize.mjs
```

重新构建会重写本目录文件；复核时应先保留原交付。浏览器捕获依赖本机 Edge 和已安装 Playwright。`tools/finalize.mjs` 要求静态、原生、图片和视觉报告均通过且绑定同一 NBT 指纹。

## 接入边界

未部署、未修改 mods、正式 JAR、玩家存档或并行房间。接入负责人须先检查整个 27×13×29 体积（包括 air）、液体、实体和世界边界，任何占用都拒绝施工；旋转时同时变换脚位、开口和原版方块状态。使用持久生成标记，不得重建已生成房间或覆盖玩家修改。测试夹具模组及隔离世界不得打入正式整合包。
