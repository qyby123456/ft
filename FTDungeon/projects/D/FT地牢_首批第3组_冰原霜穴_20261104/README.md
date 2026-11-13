# 第一批第 3 组：冰原霜穴结构候选

本线程 `01a1a5df-7c46-7fc0-a33d-cfc5b059d4d3` 独立制作整组，不拆房间线程。交付为 Minecraft Java 1.20.1 vanilla structure NBT，不以概念图片替代结构。仅写本目录，未部署或修改 mods、源码、配置、玩家存档、统一 incoming。

## 交付范围

首批 S3-01～S3-09 对应 9 个可放置模块；S3-10 为整体布局参考，不能把它当第十个单体房间。目录中后来新增的 S3-11～S3-20 不属于本次首批，未混入或声称完成。

| 候选 ID | 内容 | X×Y×Z | 连接方向 | 原件权威尺寸来源 |
| --- | --- | --- | --- | --- |
| FTN3-01 | 冰晶主厅 | 19×16×19 | 北、南、东 | betterstrongholds:rooms/junction_md |
| FTN3-02 | 冻石休憩室 | 13×8×19 | 北、南 | betterstrongholds:rooms/mess |
| FTN3-03 | 霜封储藏室 | 7×8×13 | 北 | betterstrongholds:rooms/storeroom |
| FTN3-04 | 双列冰柱大厅 | 25×14×19 | 北、南 | betterstrongholds:portal_rooms/portal_room |
| FTN3-05 | 冰脊直廊 | 5×8×25 | 北、南 | hallway_2 的 5×8×5 沿 Z 重复五段 |
| FTN3-06 | 冰壁折角回廊 | 15×8×15 | 北、东 | hallway_turn 的 5×8×5 包围盒 X/Z 各扩三倍 |
| FTN3-07 | 霜门前厅 | 13×8×13 | 北、南 | betterstrongholds:rooms/armoury_md |
| FTN3-08 | 雪松冰瀑天井 | 19×16×19 | 北、南、东 | betterstrongholds:rooms/junction_md |
| FTN3-09 | 霜晶宝库祭台 | 19×16×19 | 北 | betterstrongholds:rooms/treasure_room_lg |

每个 `incoming/FTN3-xx/` 含 `room.nbt`、`room.json`、`preview.png`、`qa.json`、`blocks.json`、完整 Lodestone 外观 `lodestone-preview.png`、Lodestone 内部剖切 `lodestone-interior.png`，以及 `views/` 下北南西东内部/外壳共八张检查图。`room.nbt` 保留完整屋顶；只有天井有明确 7×7 天窗。`views/preview-only-cutaway.nbt` 只用于预览，严禁接入生成池。

## 先读的解包证据与沿用/改造点

- 完整结构库：`D:/ft工程文件/结构参考_20261103/README.md`；已读同目录 `LEARNING_NOTES.md`、`SAMPLE_CATALOG.md`、`EVIDENCE.md`，不使用 C 盘中断解包的残留库作为权威。
- 解析原件目录：`D:/ft工程文件/结构参考_20261103/unpacked/better_strongholds/data/betterstrongholds/structures/`。
- 逐模块原件绝对路径、真实 SHA-256、原尺寸、原始材料计数、全部 Jigsaw 坐标/状态/name/target/pool/final_state 和 details JSON 路径见 `evidence/reference-evidence.json`。脚本实际解码这些 NBT，再用其尺寸驱动 recipe；不只引用文件名。
- 沿用真实模块的尺寸层级、轴向走廊、转角模块、功能房/储藏/军械/宝库分工，以及石砖、裂石砖、錾制石砖的材料语言。原件的实际连接元数据保留为证据，但没有把所有 Jigsaw 都当作可走门洞。
- 为 FT 契约重新设计北侧中心入口，脚部 Y=1，三格宽四格高；额外出口均在元数据列明，所有出口可从入口步行到达。原 Jigsaw 的位置、池与数量没有照搬，也不声称兼容 YUNG 扩展运行时。
- 冰晶中心、冰瀑、云杉支撑与雪松为本次程序化改造；冰雪材质来自图片主题，不声称原件自带这些造型。雪块、浮冰、蓝冰替代会融化的冰/真实水；无岩浆、粉雪、刷怪笼、命令或自动奖励。原传送门和玩法触发器移除。
- 宝库是空箱装饰，不包含原版末地传送门或原模组机关。模板功能与游戏玩法接入严格分开。
- 接到新增证据约束前生成过未交付初稿；现有正式候选已经依据解析后的权威尺寸重建。未调用任何生图接口，未创建图像生成半写入，也未清理或覆盖任何原始 PNG。

## 坐标与安全放置

`room.json` 的 entrance/interior/connections 均为模板原点相对的玩家脚部 XYZ；+X 东、+Y 上、+Z 南，北为 Z=0。旋转后的非负包围盒坐标见 `rotated-connectors.json`，实际引擎若绕其它 pivot 旋转须同步平移，不能直接忽略偏移。

所有 ID 是独立候选名称，`runtime_slot=null`；没有抢占共享房间槽位。最终槽位与从现有大厅连通的世界布局仍由统一接入负责，未伪造完整地牢接入。

结构包含整个包围盒的显式空气，L 形外侧也有显式空气。**接入器必须先检查整个体积的占用、玩家建筑、世界边界和实体，任何占用即拒绝；不得直接覆盖或先清空。** 仅预览不会放置。没有提供面向玩家存档的自动施工命令。

## 验证的真实等级

本组拓扑、垂直层次、手动机关、战斗节奏、地标与调色板的设计卡，以及已读其他组快照与未完成审计边界，见 `DISTINCTIVENESS.md`。最新修订加入主厅/中室错位冰脊遮挡、长厅高低双线，以及入口侧龛拉杆/栅门，不能用修订前快照验收。

- PASS：9 个模板经 bundled `minecraft-builder-skill/scripts/run-build.mjs` 创建，Lodestone 写入/读回一致，DataVersion=3465。
- PASS：静态边界、密集坐标唯一性、原版命名空间、空容器、禁止方块、无实体/流体、完整屋顶/天井例外、所有出口可走与三宽四高开口。
- PASS：真正 Minecraft 1.20.1 注册表/属性解析、BlockEntity 加载、原生碰撞形状寻路、StructureTemplate load/save 往返；报告在 `qa/native-review.json`。这不是世界放置测试。
- PASS：9 个原始 NBT 的 Lodestone 完整外壳渲染和 9 个只读派生剖切渲染；运行在 headless Edge，无页面异常。`qa/lodestone*.json` 绑定源 NBT SHA-256。另有基于最终 NBT 的原版材质软件四向检查图，不能称为游戏截图。
- NOT_RUN：Forge 世界四旋转实际放置、邻居更新/tick 稳定性、随机组装碰撞测试、从 FT 大厅真实连通与游玩测试。没有将这些未执行项写成通过；本批状态是待接收复验的结构候选，不是已安装成品。

## 预览与复跑

PowerShell 执行 `& './预览结构.ps1' -Id FTN3-01` 会启动 bundled Lodestone 预览并打印 URL，不打开游戏、不修改世界；Ctrl+C 停止。内部总览见 `lodestone-overview.png`；软件四向总览见 `overview.png`。

`tools/reference_evidence.py` 重新解析权威来源；设置进程变量 `FT_FROST_ROOM=01` 至 `09`，调用 bundled `run-build.mjs recipes/frost-group.mjs --out-dir incoming/FTN3-xx --name room --data-version 3465` 可重建。随后运行 `tools/verify_group.py --render`、`node tools/capture-lodestone.mjs`、`node tools/capture-lodestone.mjs --cutaway`、`tools/finalize_delivery.py`。Python 使用本机已有 Pillow 环境；工具中的本机绝对路径是可复现依赖，不包含密钥。

本次为原创程序化结构改造，不复制原始 NBT/贴图进入交付；原始素材授权边界仍按参考库说明处理。
