# 第6组：火山熔岩地牢结构模块

本线程 `01a1a5df-bd6b-7403-ae9c-35c85144f0c5` 独立制作。仅处理首批 S6-01～S6-10：9 个实际 Java 1.20.1 gzip vanilla structure NBT，整体概念图只作为布局参考。S6-11～20 不属于本次交付。

当前修订：**S6-R2-distinct-topology**。详见 `DISTINCTIVE_DESIGN.md` 和 `distinctiveness-audit.json`：熔核隔脊双侧回路、三挡墙横向换位大厅、2→5→2跨炉高廊、天井双高检修沿台已经写入 NBT；小室/前厅各有一个实际拉杆红石灯。机关 tick、战斗节奏均未实机验证。不是仅换材质；最终跨组重复度矩阵仍待统一审计。

没有调用图片 API、没有新生成环境图片、没有需要清理的图片半写入。初版为图片驱动草稿；收到追加约束后，读取并核对解包 NBT 与摘要，改版重生成全部九个结构。旧版 QA 不作为新版证据。

## 权威结构输入与改造边界

权威库：`D:/ft工程文件/结构参考_20261103/`。已阅读 `README.md`、`LEARNING_NOTES.md`、`SAMPLE_CATALOG.md`、`EVIDENCE.md`；读取 YUNG 下界要塞原始 NBT、对应详情、`worldgen/template_pool/halls.json`、`worldgen/processor_list/main.json`。每个原件完整路径、SHA-256、真实尺寸、真实 palette、原始接口及逐模块改造公式见 `reference-evidence.json`，每间 `room.json` 也内嵌该房间的来源。

沿用：keep 的 37×25 占地；halls 的 7 格模块尺度、直线和转角语义；storeroom 单入口容器功能；battle_bridge_gate 双端门庭；keep_well 中心井庭功能；worship 单终点祭坛功能；原始 nether_bricks、red_nether_bricks、polished_blackstone 材料。

改造：单层压缩、边框和安全余量、北向标准入口、显式 JSON 连接点；黑石玄武岩火山饰面，岩浆渠六面封闭、表面橙玻璃；屋顶补全（天井例外）；不复制战利品、刷怪笼、实体、处理器或函数。

这是参考真实模块尺度/拓扑/功能后的原创参数化重构，不是逐方块复制，也不声称保留全部原建筑布局。原图仅提供环境造型；不能拿原图或渲染图代替 NBT。来源仅用于本地研究，不推断公开再分发授权。

原 YUNG jigsaw 的 name/target/pool/orientation 只在来源证据中保留。本交付不包含活动 jigsaw；使用 FT JSON 接口，不能声称与 YUNG 生成器直接互通，也没有复制其权重、条件和运行时保证。

## 模块表

| 临时 ID | 功能 | X×Y×Z | 出口（另有北入口） |
|---|---|---|---|
| NAT6-01 | 熔核大殿 | 37×12×25 | 东、西 |
| NAT6-02 | 玄武岩议事室 | 15×10×15 | 东 |
| NAT6-03 | 余烬密室 | 9×8×9 | 无，终点 |
| NAT6-04 | 黑曜石柱廊大厅 | 29×12×15 | 南 |
| NAT6-05 | 熔脉直廊 | 9×9×21 | 南 |
| NAT6-06 | 玄武岩折廊 | 15×9×15 | 东 |
| NAT6-07 | 熔岩门庭 | 13×12×19 | 南 |
| NAT6-08 | 火山天井 | 15×12×15 | 南 |
| NAT6-09 | 烬金祭坛宝库 | 15×10×17 | 无，终点 |

所有 ID 都是本组临时标识，未占用全局槽位。房间原点为西北底部；+X 东、+Y 上、+Z 南。入口/出口坐标是玩家脚部位置，Y=2、地板 Y=1；入口为 `(floor(width/2),2,0)`，安全点为 `(floor(width/2),2,2)`。逐间出口坐标在 `room.json` 的 `connectors`，每个为 3×3 净空。小房间、宝库是终点，原路返回，不虚构额外出口。

连接计算：先旋转连接点和朝向，父连接点世界坐标加外向单位向量，再减去旋转后的子连接点，得到子模板原点。两端外向须相反，脚部高度相等；整个包围盒和路线都须检查占用，不能只检查 NBT 非空气坐标。未连接出口由接入方封口，未在此处假装已接入。南向连南向需旋转，不可直接对接。

## 文件与验证

每间 `incoming/NAT6-XX/` 包含 `room.nbt`、`room.json`、`blocks.json`、`qa.json`、`preview.png`，以及 `views/` 内北南西东的内部剖视、完整外壳视图和两张四视图拼图。预览从最终 NBT 离线渲染，非 AI 图、非游戏截图；剖视不影响 NBT 完整屋顶。

- `manifest.json`：图片原件/结构哈希、组指纹、模块元信息。
- `reference-evidence.json`：实际解包输入及沿用/改造点。
- `qa/native-review.json`：实际 Minecraft 1.20.1 注册表、状态、容器、全部接口及特色高位路径点、StructureTemplate 往返校验。
- 每间 `qa.json`：边界、全体积显式空气、接口净空、连通、岩浆密封、容器支撑和顶盖检查。
- `preview-sheet.png`：九模块实际 NBT 预览总览。
- `tools/build.mjs`：minecraft-builder-skill 配方；`tools/deliver.py`：构建/原件复核/静态验收/原生验收/四视图脚本。

Lodestone 交互预览：运行 `./preview.ps1 -Room 1`（1～9），打开终端报告的本地 URL。仅启动本机查看器，不放置到世界。

## 尚未执行

Forge 世界原生放置、旋转后碰撞与占用拒绝、液体 tick 稳定性和实际游玩均未执行；本地注册表解码通过不等于 Forge 实机验收。未修改正式 JAR、存档、NeoForge、配置或其他组目录，未部署，也未自动向其他聊天发送消息。
