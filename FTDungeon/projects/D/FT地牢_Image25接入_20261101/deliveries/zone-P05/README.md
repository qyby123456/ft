# P05 石柱跃厅 · 独立候选交付

本目录包含 Minecraft Java 1.20.1 原版结构 `room.nbt`、接口元数据 `room.json`、实际 NBT 渲染的 `preview.png` 和验收汇总 `qa.json`。房间为 17 × 14 × 29，DataVersion 3465，gzip、单调色板、6902 条完整体积记录。未部署，未写玩家存档，未修改正式模组、目录或连接路线。

参考同批 `README.md`、`docs/manifest.json`、`docs/verify.json`，原图为 `D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/parkour/P05-parkour-pillar-hop-hall.png`。原图 SHA-256 为 `671abf0d3795ca83ff0554600bbee74ef6a1f3894325624155a06dcfc6f193df`，本次只读取既有图片。

成品 NBT SHA-256：`a752b63daf9f83cacd648be941a3bcb8dee63d2d6e704f9b0f1abde5b250e4d3`。四件交付与证据文件的完整哈希见 `qa/SHA256SUMS.txt`。

## 玩法与接口

- 北侧黄色入口脚位 `[8,5,0]`，安全内部点 `[8,5,2]`；南侧绿色出口脚位 `[8,5,28]`。两口均中央 3 格宽、3 格高，脚下为完整方块，入口到安全点可步行往返。
- 五根 2 × 2 跳柱依次位于 X=7..8、Z=6..7 / 10..11 / 14..15 / 18..19 / 22..23；柱顶方块 Y=4、玩家脚位 Y=5，柱体连续连接 Y=0 池底，没有悬浮柱。六次跳跃全部跨两格空隙，无高差，可逆向返回。
- 一格深浅水在 Y=1，共 304 格含水单元，其中两格为含水梯底。水区底部和侧边封闭，头部干燥，全部连通。北岸 X=3 与 X=13、Z=4 的双梯从 Y=1 连续到 Y=4，水中可回北岸重新挑战；南岸无恢复梯，避免从池底直接绕到出口。
- 主题保留苔石、裂砖、雕纹柱、青蓝池底、吊灯和壁火把。最终 NBT 有完整屋顶 Y=12。无容器战利品、命令方块、结构方块、拼图、刷怪笼或预放实体。

## 验收与证据

`qa/static.json` 验证参考哈希与批次记录、体积坐标、禁用方块、柱脚、灯与梯支撑、水池封闭、304 格恢复区连通、双口和完整地板屋顶。

`qa/views.json` 记录来自实际 `room.nbt` 的 11 个视图及每张 SHA-256。`preview/four-interior.png`、`preview/four-exterior.png` 是四向图组；八张内外单图都使用未剖切的完整 NBT。轴测和俯视明确标记展示剖切；剖切隐藏屋顶、部分近侧墙以及随梁隐藏的链灯，不更改交付 NBT。全部为 Lodestone 0.6.0 软件预览，不是游戏客户端截图。`preview/index.html` 可离线浏览图集。

`qa/forge-native.json` 和 `qa/forge-run.log` 来自独立 Forge 47.4.10 GameTestServer。四个 Rotation 均经原生 StructureTemplate 读取、状态注册校验、放置与 NBT 回写检查；前后各核对 27608 条状态记录，并强制邻块和流体 tick，400 tick 后结构与周围空气不变。全部非空气方块通过原生 canSurvive，所有水源及含水梯底稳定。

路线证据在每个方向放置后及 400 tick 后分别验证：0.6 × 1.8 FakePlayer 经 Entity.move 原生扫掠碰撞完成六段正向和六段逆向跳跃，共 96 段，轨迹逐 tick 保存。初始竖直冲量 0.42、重力 0.08、竖直阻力 0.98，水平受控速度 0.27 格/tick；每次空中 12 tick、水平 3.24 格、最高上升约 1.2522 格，落到预期台面并步行穿过出口。恢复区原生碰撞与 BFS 覆盖 304 格，两套梯井均以 0.2 格/tick 向上扫掠返回北岸。

六项故障注入均正确拒绝：缺柱脚、破水坝、缺梯支撑、缺吊灯锚点、入口堵塞、跳跃弧线堵塞。每次注入恢复原状后继续验收。原生扫掠使用实际玩家尺寸；无碰撞复核仅收缩 AABB 的 1e-7 格，消除旋转后浮点贴面误报，Entity.move 的碰撞体保持原尺寸。

先前两次夹具失败日志完整保留：第一次 Y=100 测试体积边界碰到随机地形，被占用检查拒绝，最终改在隔离世界 Y=300 放置；第二次旋转方向的贴地浮点复核误报，修正上述容差后四向全部通过。两次均未修改房间 NBT。

## 复现

生成使用已安装的 Minecraft Builder Skill：`node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs source/recipe.mjs --out-dir . --name room --data-version 3465 --json`。随后运行 `node source/static-qa.mjs`。

原生验收运行 `powershell -ExecutionPolicy Bypass -File source/run-forge.ps1`，会在此目录的 `forge/run-时间戳/` 创建独立测试世界，只使用本目录测试模组；不要把该测试模组安装进正式整合包。脚本使用已有 Java 17 与离线 Gradle 缓存，并在结束时恢复进程环境变量。

交互预览运行 `node source/preview-server.mjs` 后访问 `http://127.0.0.1:5355/preview/interactive.html`。预览程序依赖本机技能目录；可用 `node source/render.mjs` 重建单图，用已配置 Python 运行 `source/compose.py` 重建图组，再运行 `node source/finalize.mjs` 刷新证据索引。

## 证据边界与后续接入

确定性路线验证直接控制水平移动与上升速度，验证的是实际模板上的原生扫掠碰撞、落点、通路和恢复净空；尚未验证键盘操作手感、完整加速过程、水中移动引擎、完整整合包、多人或 FPS。不能据此声称已在正式整合包实机通关。

后续集成者必须对整个 17 × 14 × 29 体积（含明确记录的空气）做世界边界、实体、液体和占用拒绝检查，并保持每房间持久化一次性生成标记；本交付没有实施正式放置或任何部署。
