# zone-M03 · 塌方矿道

独立地牢分区房间，尺寸 **23×12×25**，Minecraft Java **1.20.1 / DataVersion 3465**。本 ID 与 `corridor-M03` 完全独立。

四件套：`room.nbt`、`room.json`、`preview.png`、`qa.json`。

- 北入口脚位 `[11,1,0]`，内部安全点 `[11,1,4]`，南出口 `[11,1,24]`；入口五格宽、四格高。
- 北南主路 `x=9..13, z=0..24` 连通，净宽五格、净高四格。塌方仅占西侧 `x=2..7, z=13..22` 支巷。
- 四组落地木架、额外支巷护架、八盏梁链吊灯、短残轨、空矿斗、煤铁矿面、空木桶及工作台保留采矿主题。无液体、重力方块、命令方块、刷怪或预置实体。
- `y=10..11` 为完整双层屋顶；内部图隐藏屋顶与近侧外墙仅发生在渲染阶段。

## 参考和适配

参考 PNG：`D:\ft工程文件\image2.5\dungeon-zone-batch-20261103\out\mines\M03-mines-collapsed-mine-tunnel.png`。

参考 SHA-256：`14d4e7e36cd29469c254e7a28b6e2f8dca7a94546d434be8abd9c5ec99b48241`。

本次使用已给定图片，无付费生图调用。概念图的斜断梁改成有石堆承托的阶梯原木；矿车用空炼药锅表现报废矿斗；塌方安排在侧巷，增加南北通路以满足接入契约。完整变化记录见 `room.json/reference_adaptations`。

## 验证证据

- 静态 `qa/static.json`：6900 坐标、完整地板/双层屋顶、五格主路、入口/安全点/出口连通、支撑和空库存均通过，非空气方块全部连至基础。
- `qa/forge-native.json`：隔离 Forge 47.4.10 GameTest 实际放置 0°/90°/180°/270° 全通过；强制区块更新 200 tick 后逐条验证 27600 条记录；无掉落实体、灯轨脱落、方块变化或库存变化。
- `qa/captures/*.nbt` 为真实世界回读，逆旋转回原始坐标；独立 Python 解码逐方块及方块实体比对为零差异，见 `qa/capture-review.json`。
- 8 次缺支撑及 4 次堵路负向探针成功被检测。Forge 结束标记及 Gradle `BUILD SUCCESSFUL` 见 `qa/forge-gametest.log`；原始日志单独保留。
- 最终 NBT 生成北南西东内部剖视与完整外壳八图，见 `views/` 和 `qa/render-manifest.json`。人工图检见 `qa/visual-review.json`。`preview.png` 为真实 NBT 原版材质软件渲染，非 AI 参考图、非实机截图。
- Lodestone Viewer 已加载原始最终 NBT，截图 `qa/lodestone-exterior.png`，加载状态与指纹记录 `qa/lodestone-preview.json`。

## 重现

1. PowerShell 运行 `tools/build.ps1`；Minecraft Builder 技能在其安装目录运行，原版 gzip NBT 往返校验。
2. 使用带 Pillow 的 Python 运行 `tools/inspect_static.py` 和 `tools/render_views.py`。
3. 运行 `tools/run_native.ps1`；仅使用 `forge/run` 内的一次性 GameTest 世界。
4. 检查八张图后更新图检记录，再运行 `tools/finalize.py`。

最终 `room.nbt` SHA-256：`f318cf2ca68ee38e98cdb7d35018bbc860da705de46d32030c0a8f85638b4aac`。

状态：**PASS，可交统一接入负责人审阅；未部署、未分配正式槽位。** 未修改正式 JAR、源码、玩家存档、NeoForge、KubeJS、资源包或其它房间。没有执行整合包客户端、多人、性能或正式接入道路/持久化测试。正式施工前须验证整个体积占用、液体及实体；NBT 中显式空气不是清场授权。
