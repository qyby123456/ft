# P01 跳坑跑酷 · 石柱试步厅

交付范围：独立 Minecraft Java 1.20.1 结构，Forge 47.4.10 隔离验证。全部成果只写入本 zone-P01 目录，未部署，未修改正式实例、玩家存档、共享核心或其它房间。

## 文件

- room.nbt：Minecraft Builder 生成的 gzip 单调色板模板，DataVersion 3465，19×14×29，共 7,714 个显式体素（包括空气）。
- room.json：脚部坐标、入口出口、四段跳距、净空、安全回收路线、危险边界和接入要求。
- preview.png：最终 NBT 的 Lodestone 北向剖切预览，非概念图。
- qa.json：汇总验收与限制；qa/native.json、qa/static.json 为详细机器证据。
- views/interior-{north,east,south,west}.png、views/exterior-{north,east,south,west}.png：四向内外视图；eye-north.png、eye-south.png 为双端眼平视角。
- views/interior-sheet.png、views/exterior-sheet.png：四向总览。剖切仅在渲染内存发生，最终 NBT 保有完整屋顶。
- SHA256SUMS.txt：主文件、证据、脚本、视图和原生世界回采模板的指纹。

## 设计对应

原图：D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/parkour/P01-parkour-gap-jump-pit.png

原图 SHA-256：d8809777350e5a681cd94c59af4d328631e02ef5bc2226f1ebe859a6e8de7603

NBT SHA-256：6d7f1cac5922f0c4a4702bd78ec0677f46655c0ccdfd347a85842cda103b29c3

已核对批次 README、docs/manifest.json 与 docs/verify.json；qa/reference.json 留存匹配条目和索引文件哈希。保留原图中央干坑、三个连续石跳台、侧廊承重柱、链吊灯、石砖与木箱堆主题。为可玩性增加完整屋顶、两端通口、两侧铁栏、阻断侧廊绕行的石隔墙，以及浅坑回收带。木箱用实木装饰块表达，没有库存、随机战利品或方块实体。

## 入口、路线与危险边界

北入口脚位 [9,5,0]，安全内点 [9,5,3]，南出口脚位 [9,5,28]。脚下铺装 y=4；两个门洞宽 3、高 4，无门。南北路线可逆。白色地面箭头指向跳坑，坑两侧三格高铁栏和侧廊隔墙隔开观景通道，步行无法绕过跳跃到达出口。

中线 x=9；三个落脚台均为 x=8..10、顶面 y=4，z 分别为 9..11、14..16、19..21。跳跃路线如下：

| 跳跃 | 起跳脚位 | 落脚脚位 | 空格跨度 | 中心距离 | 平台宽 | 保守净空 |
| --- | --- | --- | --- | --- | --- | --- |
| J1 | [9,5,6] | [9,5,9] | 2 | 3 | 3 | 6 |
| J2 | [9,5,11] | [9,5,14] | 2 | 3 | 3 | 6 |
| J3 | [9,5,16] | [9,5,19] | 2 | 3 | 3 | 6 |
| J4 | [9,5,21] | [9,5,24] | 2 | 3 | 3 | 6 |

使用普通生存移动速度短助跑跨跳，无药水、飞行或特殊装备要求。每块中间跳台纵深 3 格，可停步和重新助跑。原生跳跃模型每段约 12 tick 落地，脚部最高上升约 1.252 格。

危险边界为 x=6..12、z=7..23 的干坑，行走层脚位 y=5、坑底脚位 y=2，地板有完整实体承托，没有岩浆、水、火、粉雪、蛛网或伤害方块。直接踏空落差 3 格，原版预计摔伤 0；从正常跳跃最高点失败落下约 4.252 格，原版 calculateFallDamage 预计最多 2 HP（一颗心）。因此“安全落点”指非致命的干燥回收区，不声明任何低血量状态均无风险，也不声明跳跃失败零伤害。

坑底共有 84 个可站立脚位，全部连回入口。沿 x=6..7 或 x=11..12 的两格宽浅色回收带向北，经过 z=9→8→7→6，脚位依次 y=2→3→4→5；最后两个一格高台阶用普通跳跃即可回起点。恢复过程不穿越其它坑或伤害区域，不需要命令、死亡、传送或一次性物品。

## 验收证据

qa/static.json 包含 26 类静态检测：原图哈希、尺寸与版本、全体积唯一坐标、完整顶底、双端开口、四跳净空与地板、链灯承托、墙灯附着、无战利品/实体/流体/伤害块、步行无旁路，以及全部 84 个坑底脚位返程可达。

qa/native.json 与 qa/forge-gametest.log 来自独立 Forge GameTestServer。NONE / CLOCKWISE_90 / CLOCKWISE_180 / COUNTERCLOCKWISE_90 四向原生 placeInWorld，强制区块 tick 和邻居更新，稳定 200 tick 后再次逐格核对全部 30,856 格。每向均有 14 个有支撑灯具、84 个可恢复坑底脚位和 244 个与入口连通的脚位，外部流体、世界实体与掉落为 0。

原生路线验证组合五段真实碰撞检查通过的行走段与四段 native FakePlayer.travel/jumpFromGround 跨跳，另测反向四跳。每向 8 次跨跳，共 32 次；另测每个断口直接落下和跳跃后失败落下，每向 16 次，共 64 次。摔伤值来自原版 calculateFallDamage；FakePlayer 的健康实现不等同真实客户端玩家，因此不把 FakePlayer 健康值作为零伤害证据。

qa/native-placed-*.nbt 是四向实际世界回采模板，均逐格匹配旋转源模板，且原生重新加载通过。占用显式空气格、拆链灯支撑、拆坑底回收地板、阻塞跳跃通道的负向探针每类四次均成功检测。

qa/views.json 记录源 NBT 哈希、相机、截图 SHA-256 和内存剖切规则；qa/image-decode.json 确认十张原图可解码；qa/visual-review.json 记录对四向内外总览及眼平视角的实际视觉复核。

首次运行因夹具缺少 pack.mcmeta 导致 empty 模板不可见；补齐后通过。增强摔伤测试时重用了旧隔离测试世界，占用预检按设计拒绝。现脚本每次使用新的 native-qa/runs/run-<时间> 目录，保留旧世界与失败日志，不清空任何存档。attempt-* 为历史尝试证据，不作为最终 PASS 的替代。

## 复现与接入

1. 在 C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill 下运行 node scripts/run-build.mjs <本目录>/tools/recipe.mjs --out-dir <本目录> --name room --data-version 3465 --json。
2. 运行 node <本目录>/tools/qa-static.mjs。
3. PowerShell 运行 tools/run-native.ps1；使用本机 Java 17、缓存 Gradle 8.8、Forge 47.4.10，每次创建全新隔离世界。夹具严禁放入玩家 mods。
4. 用技能 scripts/preview.mjs --nbt <本目录>/room.nbt --no-open --port 5321 启动本地预览，运行 node tools/render-views.mjs，再用配置的 Python 运行 tools/make-sheet.py。无头 Edge 只负责 Lodestone 视图。
5. 复核截图后运行 node tools/finalize.mjs 生成报告和指纹。

接入方放置前应检查整个 19×14×29 体积（含显式空气）、世界边界、实体和流体，遇占用拒绝；模板旋转时同变换入口出口和状态；保留独立持久生成标记及玩家修改，禁止重复覆盖。未完成主干连接、返程门、整合包客户端人工通关、多人或 FPS 验收；此次交付不涉及部署。

