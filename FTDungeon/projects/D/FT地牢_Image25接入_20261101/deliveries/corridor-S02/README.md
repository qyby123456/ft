# S02 · 陈旧石砖短直廊

独立走道 ID 为 **corridor-S02**，不是旧房间 S2。以本目录的最终 room.nbt 为唯一施工源。已用 Minecraft Builder / Lodestone 0.6.0 生成，不重新调用生图接口。

## 结构与接口

- Minecraft Java 1.20.1；DataVersion 3465；gzip 单一调色板 NBT。
- 尺寸 XYZ = **11 × 9 × 16**，长度沿 Z 轴，正好 16 格 / 1 区块。脚下地板 Y=0。
- 北入口脚位 **[5,1,0]**；内部安全点 **[5,1,3]**；南出口脚位 **[5,1,15]**。
- 主路 **X=4…6、Y=1…3、Z=0…15**，三宽三高、完整平地；无转弯、分岔或行走楼梯。石砖楼梯方块仅用于头顶拱下装饰。
- 176 格完整地板、176 格完整最顶层屋顶，平行外墙无侧门。全体积 1584 条显式记录，其中 886 非空气方块、698 空气记录。即使包含空气记录，接入方也必须先检查整个施工体积，拒绝占用，不能无条件覆盖世界。
- 四道石砖拱券、裂纹砖、少量苔砖与安山岩、六支原生壁火把和六组贴墙铁托架、高角六处蛛网、两处贴边碎石。两处拱下齿刻意破损，承重墙和屋顶保持完整。
- 无水或熔岩、无容器、无库存、无预置实体、无技术方块、无刷怪或奖励逻辑。

## 原图与取舍

原图：D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S02-stone-brick-aged.png。

原图 SHA-256：e068ca984a82719da731f79474db5a7d3cd0bbdade57e5bfaca420e46e665239。

保留灰石砖、破损拱券、暖壁火把、铁托架、角落蛛网和裂纹石地面。为服从批次“一字贯通”及通路契约，图中尽头关闭木门改为南侧永久敞开的拱口；不放可关闭的门。蛛网位于 Y=5，碎石位于 X=2 或 X=8，不进入中央三格主路。所有火把实际附着原生实体外墙，铁栏杆是装饰托架，不作为火把的虚假支撑。

## 交付与证据

- room.nbt、room.json、preview.png、qa.json 为交付四件套。
- materials.json、recipe.mjs 和 tools/build-and-audit.mjs 可复现建造及静态审计。
- preview.png 为完整最终 NBT 北口内景；preview/north.png、south.png、west.png、east.png 为四向内部剖切，preview/four-view-sheet.png 为拼板。
- preview/shell.png 为完整外壳，preview/roof.png 为完整屋顶。剖切只在浏览器中操作副本，不修改交付 NBT。
- qa/static.json 为静态结果，qa/native.json 为真实 Forge GameTest 结果，qa/render.json 绑定渲染输入与 PNG 指纹，qa/four-view-review.json 为逐项视检记录。
- qa/native-roundtrip.nbt 为 Minecraft 原生模板往返；qa/native-placed-*.nbt 为四种旋转实际放置 200 tick 后的世界回捕，preview/native-*.png 和 native-placement-sheet.png 为其真实 NBT 渲染。
- qa/forge-20261103-s02-final.log 保留真实 Gradle / Forge 日志。首次隔离世界缺少 server.properties 会产生初始化日志，最终以所需 GameTest 全部通过和 BUILD SUCCESSFUL 为准。

## 验证范围

静态校验检查指纹、版本、单一调色板、全体积、坐标去重、地板屋顶、平行侧墙、三格通路、贴墙火把和装饰、无游离液体、无容器与实体、无悬离连通体。

真实 Java 17 / Minecraft 1.20.1 / Forge 47.4.10 隔离 GameTestServer 验证 NONE、CLOCKWISE_90、CLOCKWISE_180、COUNTERCLOCKWISE_90 四个方向。每方向逐一比较 1584 个位置的准确方块状态，并执行原生 canSurvive、碰撞、全地板屋顶、三宽三高主路和安全点至出口可达性。显式触发邻居更新，在 100 和 200 tick 复查，检查实体与体积外一格液体边界，导出世界回捕。负向探针检查封堵入口、屋顶缺洞、拆除壁火把支撑时确实拒绝异常。

只操作本交付目录及其 qa/forge-runs 下隔离测试世界。**未部署 JAR，未修改正式源码、玩家存档、mods、NeoForge、KubeJS、资源包或正式配置；未分配大厅槽位，未改连接与返程逻辑。** 本结果不是完整整合包客户端、多人游玩、FPS 或统一接入验收。

## 复跑

在本目录运行：

    node tools/build-and-audit.mjs
    powershell -ExecutionPolicy Bypass -File tools/run-forge.ps1 -RunId replay-001
    node tools/preview-server.mjs

预览服务仅绑定本地回环，端口记录在 qa/preview-server.json；另一终端运行：

    node tools/capture-previews.mjs --native
    node tools/finalize-delivery.mjs

独立 QA 模组只用于测试，不得安装到玩家实例。重建或改动 NBT 后必须重新执行 Forge、截图及人工视检，不能沿用旧 PASS。
