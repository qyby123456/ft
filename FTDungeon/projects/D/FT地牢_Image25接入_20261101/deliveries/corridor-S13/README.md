# corridor-S13 陈旧血誓祭仪短廊

制作日期：2026-11-03（Asia/Shanghai）。独立交付，仅制作线程证据；未接入核心、未部署，未打开或写入玩家存档。所有原生放置仅发生在本目录 qa/forge-runs 下的新建临时世界。

## 成品与接口

- 四件套：room.nbt、room.json、preview.png、qa.json。
- 模板：Java 1.20.1、DataVersion 3465、gzip 单调色板；Minecraft Builder Skill / Lodestone 0.6.0 生成。
- 尺寸 XYZ：15×11×16，Z 为16格短型长轴，完整地板和屋顶各240格。2640条记录包含1384空气；非空气1256。
- 北入口脚位 (7,1,0)，内部安全点 (7,1,3)，南出口 (7,1,15)，南侧安全点 (7,1,12)。
- 主路 X=6…8、Z=0…15、Y=1…3 全为空气，同高无台阶；脚下均为实心安全地板，无转弯或分岔。
- 主题：灰黑裂石、血红地砖、8面红底黑纹旗、4座木梁铁链吊笼、8支末地烛、12组仪式蜡烛及10格侧角蛛网。
- 原图：D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S13-blood-ritual-aged.png。
- 原图 SHA-256：91a5ac1e968b27eaab5641524e3923b5dff855fd885d1cf84237b5c46bc0e8c4。
- 最终 NBT SHA-256：e0dea56d7299795c44c5717160ba1dc1cd07db5799d04e6a4c48a898ad0dddd8。

## 参考图转译

概念图封闭铁栅门改成升起的门栅，保持北南贯通；血槽以同高红色实心砖表现，没有血液流体、伤害方块、红石或可交互祭坛机制。吊笼为空装饰，无实体、容器或奖励。末地烛使用原版白光，不把概念图青色辉光伪称实机效果。

吊笼中心铁块承重点连接铁链，铁链接在连续木梁下，木梁由两端完整立柱支承。旗帜在四视图初审后从 Y=7 下移到 Y=5，保留旧版证据于 qa/revisions/，最终版本重新生成静态报告、八张视图并执行原生验证。

## 验证证据

- qa/static.json：21项静态检查通过，包括图源哈希、坐标范围、唯一记录、单调色板、屋顶、双端安全点、三宽三高全长动线和主题构件锚点。
- views/：北、南、西、东各一张内部剖视和一张完整外壳，共8张；另有俯视 plan.png 与 four-view-sheet.png。qa/four-view-review.json 绑定逐图审阅结果和图片哈希。
- preview.png 来自最终 NBT 的玩家眼高透视。视图使用实际1.20.1方块模型和纹理软件渲染，不是游戏截图；旗帜采用原生 skull/border 贴图的静态平面，未模拟布料动画或动态光照。
- qa/native.json：Minecraft 1.20.1 / Forge 47.4.10 / Java 17.0.8 隔离 GameTest 通过；一个测试方法执行0/90/180/270度四次放置，共10560条最终状态核对，强制区块加载并等待300 tick。
- 原生检查包括每格状态及 canSurvive、8个旗帜方块实体与花纹、两端及48个主路脚位、完整屋顶、柱梁与吊笼锚点、无流体、无实体掉落和无容器库存。17次故意移除链/梁/屋顶、封堵入口及注入战利品的负例均被检查器拒绝。
- qa/native-placed-*.nbt：四次原生世界回采。未旋转回采的全部2640条状态和8面旗帜花纹已与源模板比对。
- qa/native-result.json 绑定本次报告、日志、四向回采哈希与真实退出码。原生日志存在 qa/forge-20261103-105704-487649.log，含 CORRIDOR_S13_NATIVE_PASS 和 BUILD SUCCESSFUL。

制作线程 QA 不替代接收者对冻结包的独立八视图与原生复验。ready_for_unified_integration 保持 false；本次不测试大厅路线、核心槽位、整卷占用拒绝、幂等、持久标记、整合包客户端、多人或FPS。模板候选路径仅为 ftdungeonmobs:image25/corridor_s13。

## 重建与查看

从 C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill 运行：

	node scripts/run-build.mjs "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S13/source/corridor-s13.mjs" --out-dir "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S13" --name room --data-version 3465 --compression gzip

使用同目录 scripts/preview.mjs --nbt 指向 room.nbt 可打开 Lodestone 交互预览。

使用带 Pillow、NumPy 的 Python，按顺序运行 tools/prepare.py、tools/render.py、tools/run_native.py、tools/finalize.py。Forge 需要本机现有 Java17、Gradle8.8 和47.4.10缓存。首次准备或资源缓存可能耗时，原生运行自动新建 qa/forge-runs/<时间戳>，不加载正式世界。

HANDOFF.json 记录四件套和 bundle 指纹；sha256sums.txt 校验包内文件；corridor-S13-delivery.zip 包含成品、脚本、视图及证据，不含 Gradle 缓存、测试世界或旧版本。
