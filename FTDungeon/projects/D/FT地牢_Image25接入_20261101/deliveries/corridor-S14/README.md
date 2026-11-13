# S14「末地蔓生短廊」独立交付

仅落实已存在的 image2.5 参考图，使用 Minecraft Builder / Lodestone 0.6.0 生成结构与实际 NBT 预览；没有重新生图、部署、打开玩家存档、改核心或修改其他交付。

## 领取文件

本目录根部的 room.nbt、room.json、preview.png、qa.json 为四件主交付。delivery-manifest.json 记录文件指纹；evidence/ 与 qa/ 保留复验资料。native-test/ 仅为隔离验证工程，任何测试模组/JAR 均不可部署。

- 候选 ID：CORRIDOR_S14；图像批次源 ID：S14；交付 ID：corridor-S14。避免与已有房间 S14 混淆，最终生产 ID/槽位由唯一接入负责人分配。
- Minecraft Java 1.20.1 / DataVersion 3465 / gzip / 单一 palette。
- XYZ：13 × 10 × 16；长度恰为 1 区块，轴线为 Z。
- 北入口脚位 [6,1,0]，内部安全脚位 [6,1,2]，南出口脚位 [6,1,15]。地板 Y=0，完整屋顶 Y=9。
- 中轴 X=5..7、Z=0..15 连续三格宽、三格高净空；平地一字贯通，无转弯、分岔或行走楼梯。
- 2080 条显式记录，其中 1538 非空气方块。无容器、战利品、方块实体、模板实体、刷怪笼或命令方块，无真水/熔岩/火。

最终 NBT SHA-256：bb75401b9f8f5bbab4f1186d910bc3908dc631546008272e0076d534a27229b4。

## 设计指纹与对应

参考：D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S14-end-overgrown.png。

原图 SHA-256：55d41c576a5d4061b182f784bc256c2ae56c7aee06df2d5b3020c825a442a1f2；1536×1024。

依据批次 SPEC.md、该图同名 prompts/S14-end-overgrown.txt，以及 FT地牢_Image25接入_20261101/CONTRACT.md 建造。保留奶油色末地石砖、紫珀柱拱、黑曜底座与壁龛、六支暖火把、十二根末地烛、苔藓、贴墙藤蔓和有屋顶挂点的垂根。Z=0/5/10/15 四道拱架，内侧两处拱饰残损但不切断上梁。所有非空气方块形成连接到底板的单一面相邻组件。

明确偏差：原图远端似封闭紫珀门，为服从「一字贯通」交付为南侧敞开拱口。原版末地烛实际白光，不伪造概念图青色后期辉光；火把不引入独立火焰方块或可燃构件。

## 真实结构预览

preview.png 为完整模板内部一点透视。evidence/north-cutaway.png、south-cutaway.png、west-cutaway.png、east-cutaway.png 与 four-view-contact-sheet.png 为北/南/西/东内部剖视；full-envelope.png 保留完整外壳。

软件预览基于最终 room.nbt、Lodestone 默认原版资源与 WebGL。只在显示副本中切掉屋顶及近侧墙，不修改 NBT；每个保留方块都复核名称及属性与源 NBT 完全一致。不是 AI 概念图替代，也不是 Minecraft 客户端截图。剖视中被裁去挂点的显示植物不代表成品悬空，挂点由完整模板与原生 canSurvive 检查确认。

## QA 证据

- qa/static-qa.json：15 项通过，包括参考指纹、完整显式体积、完整地板/屋顶、三格净空、平行封闭侧墙、两个端口、植物/火把挂点和全结构连通。
- qa/native-registry-qa.json：真实 Minecraft 1.20.1 注册表、19 个最终 palette 状态与原生形状；StructureTemplate 加载和保存往返。
- qa/native-world-qa.json：Forge 47.4.10 隔离 GameTest 四种旋转实际放置；每快照合计 8320 记录；逐非空气块支撑/存活、48 个脚位的 0.6×1.8 扫掠碰撞与三格净空、完整地板/屋顶、无实体/液体/库存均通过。
- 正常随机刻速度 3、四轮显式藤蔓原生随机 tick，加 240 tick 稳定性复查通过；四方向合法藤蔓变化分别为 7/2/4/4。只允许合法藤蔓生长/状态变更，建筑结构状态必须精确不变，不承诺活植物永远字节不变。
- 四套夹具先验证完整体积为空再施工，结束后回滚至已确认的空体积；仅测试工程 native-test/run/s14-isolated-gametest/world 参与验证，没有玩家世界。
- evidence/forge-gametest-rerun-1.log：S14_NATIVE_QA_PASS、All 1 required tests passed、BUILD SUCCESSFUL。初次测试的编译兼容失败日志保留，不将其冒充通过；修复测试常量后 NBT 指纹没有改变。
- qa/four-view-review.json 是制作方四视图自查，不冒充独立接入人的终审。qa/preview-provenance.json 与 evidence/render-session.json 记录预览来源及完整属性一致性。

四夹具同步放置曾产生 28.715 秒追帧警告。因此仅证明有界功能/结构稳定性，不证明完整整合包性能、长时间植被生长、客户端画面、多人体验、正式目录与大厅线路。

## 重跑

在本机 PowerShell 执行本目录 Run-QA.ps1；加 -Rebuild 可从 Minecraft Builder 配方重建。脚本使用本机已缓存的 Java17、ForgeGradle6.0.54、Forge47.4.10、Gradle8.8，以及配置的 Python/Pillow 和 Edge/Playwright；仅在本交付子目录写入。预览辅助进程隐藏启动，结束即关闭，过程变量在 finally 恢复。

单看预览：从已安装 Minecraft Builder skill 目录运行 node 加本目录 tools/preview-server.mjs，访问其打印的 localhost/viewer.html；可在 URL 加 ?view=north、south、west、east 或 envelope。默认端口 5314，可设置进程 S14_PREVIEW_PORT 改变。

## 接入边界

本交付未注册正式槽位、未接大厅道路、未改返程/难度/战斗/奖励、未部署。接入方须另行核对唯一 ID，按完整 13×10×16 体积检查占用、液体、实体、世界边界与入口安全；不允许因显式空气而无条件清空。保持每实例独立持久化标记和幂等，已有建筑/玩家修改不得重建覆盖。外部南北连接落点与护栏由接入方设计。

