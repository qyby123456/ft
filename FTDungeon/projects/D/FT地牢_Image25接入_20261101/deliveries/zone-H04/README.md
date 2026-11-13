# H04 柱列中庭 · 独立交付

尺寸 **33 × 23 × 37**，Minecraft Java 1.20.1，DataVersion 3465，gzip 压缩单调色板结构。实际模板完整封顶；预览剖切仅发生在渲染器内。状态以根目录 qa.json 为准。

## 文件

- room.nbt：完整 28083 格显式记录，含空气；空场景放置用，接入负责人须预检完整包围盒，不得拿此模板覆盖玩家建筑。
- room.json：入口、安全点、四个端口、尺寸、原图及模板完整 SHA-256。
- preview.png：实际 NBT 制作的交付总览；preview/index.html 为离线图集。
- preview/four-exterior.png：北、东、南、西完整外视。
- preview/four-interior.png：北、东、南、西展示剖切内视。
- preview/four-eye.png：四入口的原模板内视，不隐藏方块。
- qa/static.json、qa/forge-native.json、qa/views.json、qa/composites.json、qa/visual-review.json：独立证据。
- source/：可复现 Minecraft Builder recipe、检查与渲染脚本；forge/ 为独立测试模组工程和一次性测试世界，禁止装到正式实例。
- SHA256SUMS.txt：核心成品、源码、视图、证据和日志指纹。

## 建筑与接口

四根主柱中心为 (9,10)、(23,10)、(9,28)、(23,28)，柱体独立于周墙，5×5 柱基完整落地，3×3 柱身连续，柱头与横纵梁接到 y21 屋顶。金色环带与蓝色挂旗保留参考图配色。两侧 3 格宽、8 级实心支承楼梯接 U 形二层回廊，上层临空边有铁栏和灯柱护栏，背墙保留石像、拱托、盆栽和木箱造型。

中央水池有 40 格静态水源，水底和四侧均封闭；高处的溢流轮廓使用青色玻璃，避免开放高位水源持续冲刷环行道路。天窗采用完整青色玻璃，整层 1221 格屋顶无洞。26 盏灯均有柱台或从屋顶连续连接的铁链，16 面旗帜保留两层金色图案数据。

坐标为模板相对坐标，玩家脚部坐标：

| 用途 | XYZ | 净空 |
| --- | --- | --- |
| 北主入口 | [16,2,0] | 3 格宽 × 4 格高 |
| 内部安全点 | [16,2,7] | 稳固干燥地面 |
| 东端口 | [32,2,19] | 3 × 4 |
| 南端口 | [16,2,36] | 3 × 4 |
| 西端口 | [0,2,19] | 3 × 4 |
| 西上层安全点 | [4,10,17] | 与双梯、回廊连通 |
| 东上层安全点 | [28,10,17] | 同上 |
| 南上层安全点 | [16,10,32] | 同上 |

四个地面入口均可经干燥环路抵达北入口与安全点；池前和池后保留至少两格环行间隙。楼梯按原生碰撞体检查，0.6×1.8 玩家沿每级半格高度可上下通行。无生成实体、刷怪笼、命令方块、预放战利品或附加游戏机制；木箱使用木方块而非有物品容器。

## 参考证据与差异

参考图：D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/halls/H04-halls-pillared-atrium.png

参考 SHA-256：7f2d1443078ad7b3ae6611ce5af55ef41682bb80b2dbbcbde497c2bf50554e84

已与同批 README.md、docs/manifest.json 和 docs/verify.json 核对。概念图是剖视设计，不作为施工屋顶缺失依据。成品补齐四周墙与封闭天窗屋顶；主柱前后间距为可行走的喷泉环路调整；喷泉高处用玻璃造型表达水幕。只落实已有图片，未调用付费生图。

## 验证层级

静态检查覆盖完整体积和指纹、四柱连续落地、梁柱连接、地板与屋顶、入口净空与往返路径、双梯、上层护栏、灯具支承和水池密封。Lodestone 0.6.0 按实际 room.nbt 渲染 14 张独立视图，无浏览器错误。

Forge **1.20.1 / 47.4.10** 在本目录独立 GameTestServer / ServerLevel 中调用原生 StructureTemplate.placeInWorld，按 NONE / CLOCKWISE_90 / CLOCKWISE_180 / COUNTERCLOCKWISE_90 四种旋转实际放置。逐条核对 112332 条记录，派发邻居更新并主动安排水更新，400 tick 后再次核对：水源、旗帜数据、灯具、碰撞通路、柱梁和屋顶保持不变，周界无漏水或掉落实体。两梯每旋转合计 82 次玩家碰撞采样。

六项缺陷注入均被检查器拒绝：缺入口地板、堵入口、破水坝、断柱、断吊灯链、屋顶缺洞。完整原始日志保存于 qa/forge-*.log。

这属于静态、实际 NBT 软件图和隔离 Forge 放置验收。**尚未部署或接入核心，未修改正式实例或玩家存档，未进行完整整合包客户端/多人游玩验收。**

## 复现

所有写入均限定本 H04 目录。Minecraft Builder 依赖已安装，生成命令应从技能目录执行：

    node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-H04/source/recipe.mjs --out-dir D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-H04 --name room --data-version 3465 --json
    node D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-H04/source/static-qa.mjs
    powershell -File D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-H04/source/run-forge.ps1

每次 Forge 验证新建 forge/run-isolated-时间戳，不复用玩家存档。预览服务只绑定 127.0.0.1:5404：

    node D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-H04/source/preview-server.mjs

随后访问 http://127.0.0.1:5404/preview/interactive.html；source/render.mjs 可重新渲染全部视图。source/compose.py 生成汇总图与离线图集，source/finalize.mjs 校验一致哈希并汇总 qa.json 和 SHA256SUMS.txt。
