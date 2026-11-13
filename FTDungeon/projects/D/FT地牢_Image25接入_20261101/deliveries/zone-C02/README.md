# C02 淹水拐弯 · 独立候选交付

交付目录仅服务本房间，未部署。制作使用 Minecraft Builder Skill / Lodestone 0.6.0，目标 Minecraft Java 1.20.1，DataVersion 3465。

- 正式模板：room.nbt，19×11×19，gzip 单一调色板，3971 条完整体积记录（含空气）。
- 接口与来源：room.json。北中央入口脚位 [9,2,0]，东中央出口脚位 [18,2,9]，内部安全点 [9,2,9]。两口均宽 3、高 4；整条 L 通路最少宽 3、净高 3，脚部 Y=2，转弯 90°，同高且可双向通行。
- 成品预览：preview.png，由真实 room.nbt 渲染；views/ 含四向内部、四向完整外壳、俯视、剖切总览及两个接口的内部相机，共 12 张。
- 验收入口：qa.json。逐项证据位于 qa/static.json、qa/render-manifest.json、qa/visual-review.json、qa/forge-native.json、qa/capture-comparison.json 与 Forge 原始日志。

最终 room.nbt SHA-256：3efd164b2a7ee8e8cbf328dd6a066d65877d77dd8435db633cf88c16da8c98c8。

设计图：D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/corners/C02-corners-flooded-corner-bend.png。

设计图 SHA-256：4b61ed5b869a18e1ca6c8ccd51de3972b17b7a4e2998d20b2a8429f5420f12d8，已与该批 docs/manifest.json、docs/verify.json 双重核对。未请求任何新图像生成。

## 安全构造与原图转译

保留苔裂石墙、四根落地石柱、铁栅壁龛、四盏有屋顶链条支撑的吊灯、一盏落地石台灯、浅水与两只空桶。水池只有一格深，131 格静态水源位于 Y=1，底板与四侧均为完整实体方块；通路的脚部位于 Y=2，两侧共 34 段木栏杆落在实体池沿上。铁栅后为完整实体背墙，不向外漏水。南、西两面不设连接口。

为了满足连接与安全要求，原图近乎全淹水的地面增加三格宽的石质 L 通路；图中剖面和开放边缘补齐完整墙体与屋顶。省略非原版绳圈与箱子贴图，未引入额外模型或模组方块。软件水色与照明由 Lodestone 默认资源决定，与概念图的反射、暗青色调存在差别。

两只桶没有物品、战利品表或奖励。结构没有实体、刷怪笼或命令方块。原版桶依旧是普通可交互容器；本交付没有添加奖励或战斗机制。

## 验证范围

隔离 Forge 47.4.10 GameTestServer 对 0°、90°、180°、270° 四向分别进行 StructureTemplate.placeInWorld，检查全部 3971 条记录、原生方块注册表与属性、所有非空气方块 canSurvive、玩家 0.6×1.8 碰撞体、通路净高、正反向连通、完整屋顶和外壳、水源五面密封、无外漏边界、空桶与无实体。

显式触发邻居更新和水 tick，randomTickSpeed=3、doFireTick=true；400 tick 后重复全部检查，并保存四向真实世界快照，由独立 Node 程序从原 NBT 逐方块对照旋转后的坐标及全部状态属性，共 15884 条。

故障注入要求拒绝：转弯处缺地板、转弯处堵路、水池坝体缺口、吊灯缺链条。所有探针在检查后恢复。第一版通过测试后补实侧台灯基座；原始版模板与报告保存在 qa/revisions/initial-18e6cf4d/，最终证据统一绑定上述 3efd164b… 哈希。

软件剖切只影响渲染副本，NBT 保留完整屋顶。隔离 Forge 验证不等于完整整合包客户端、多人或 FPS 游玩验收。大厅连接路由、占用拒绝、持久化与幂等由统一接入方后续验证。本次未访问玩家存档，未修改正式模组、核心源码、正式模板目录或其他房间。

## 复现

在 Minecraft Builder Skill 目录执行：

    node scripts/run-build.mjs "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-C02/source/recipe.mjs" --out-dir "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-C02" --name room --data-version 3465 --json

在此交付目录执行：

    node source/static-qa.mjs
    powershell -NoProfile -ExecutionPolicy Bypass -File source/run-forge.ps1
    node source/check-capture.mjs

交互预览：运行 node source/preview-server.mjs，使用其输出的本地 URL。截图执行 node source/render.mjs <本地服务 URL>。服务使用动态端口且不监视 QA/Forge 文件，避免并行测试的 Windows 文件锁冲突。

模板包含显式空气记录。未来接入时必须先验证完整体积为空且没有液体或实体，再放置；禁止据此无条件清空任何正式世界区域。

