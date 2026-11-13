# A03 · 荒废厨房

独立房间制作交付，未部署、未改正式模组或正式配置、未读取或修改玩家存档。仅在本目录 qa/forge-run 创建隔离 Forge GameTest 世界。

## 四件套

- room.nbt：Minecraft Java 1.20.1 / DataVersion 3465 / gzip / 单一 palette；19×11×19，3971 条完整体积记录，其中非空气 1758、显式空气 2213，32 个调色板状态。
- room.json：尺寸、北入口、安全点、端口、来源哈希、内容与接入边界。
- preview.png：由最终 room.nbt 经 Minecraft Builder / Lodestone 0.6.0 渲染，内景剖视保留灯链横梁；真实 NBT 保留完整石屋顶。
- qa.json：静态、四向内外渲染、真实 Forge 四旋转的汇总结果；全部 PASS。

可直接打开 review.html 审阅。views 有北/南/西/东四方向外观与内部剖视各四张、完整 NBT 入口视点、两张四视总览；qa 有来源、静态、材料、原生放置和视觉记录。delivery-manifest.json 给出交付文件的 SHA-256。

## 参考与保留特征

参考：D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/abandoned/A03-abandoned-derelict-kitchen.png

原图 SHA-256：413f30992df8f88ee93bdffd0c1ada33f1e33d0ccc4a47e1186f45fa5ded37b5

已核对同批 README、docs/manifest.json、docs/verify.json，记录见 qa/reference-evidence.json。本任务使用已有概念图，未调用任何付费图像生成 API。

保留灰色老化石砖、苔痕、石砌炉罩和烟囱、中央木长桌、矮凳、木柜架、储物桶、陶锅盆器、壁挂锅具和链灯主题。锅具使用原版砂轮表现，厨房点缀使用空花盆、炼药锅、工作台、旧布和枯盆栽。

## 支撑与安全处理

- 中央木桌四脚完整落地；两侧柜架立柱落地；炉罩由连续石墩支撑，空炉锅下方补完整玄武岩承台。
- 五盏灯的 y=6..7 铁链直连 y=8 木横梁，横梁两端靠墙，屋顶 y=9 完整；三个吊挂砂轮紧贴木梁，梁后为南墙。
- 石炉床和侧墩、顶罩隔开木构件。营火与熔炉均 lit=false，库存为空；当前模板无活动火源、无液体、无战利品。暖光来自灯笼。
- 三个木桶库存为空，无 LootTable；熔炉和营火的物品列表也为空，无刷怪笼、命令方块、结构方块、拼图或实体。
- 两处蛛网均在高处装饰位，主路不穿蛛网。完整地板 361 格，完整屋顶 361 格。

## 坐标与接入

所有坐标均为模板原点相对整数 XYZ，玩家坐标为脚部坐标；北为 z=0。

| 项目 | 坐标或范围 |
| --- | --- |
| 尺寸 | [19,11,19] |
| 北入口 | [9,1,0] / facing north |
| 内部安全点 | [9,1,4] |
| 北开口 | x=8..10, y=1..4, z=0，即 3×4 |
| 入口前场 | x=8..10, z=0..6，至少三格净高 |
| 地板 / 屋顶 | y=0 / y=9 |
| 干燥可达脚位 | 209 格，八个检查点均可往返 |

只有北入口；侧墙为窄铁栏窗，无额外出口。接入方须检查整个 19×11×19 体积、实体、液体、世界边界与外接路占用，设置独立持久生成标记，拒绝覆盖玩家修改。显式空气不代表授权清空已有世界。旋转时端口、安全点和结构共同使用 StructureTemplate 原点规则。

## 验证证据

1. 静态：qa/static.json，验证原图哈希、批次记录、gzip/3465/单 palette、完整体积、完整地板屋顶、3×4 入口、三格前场、通路洪泛、支撑列、灯链、锅具梁、库存及禁止内容。
2. 原生：Forge 47.4.10 / Minecraft 1.20.1，qa/forge-placement.json 与 qa/forge-gametest.log。四种 Rotation 分别重复放置，共八次 StructureTemplate.placeInWorld；触发邻居更新、强制加载测试区块，保留 randomTickSpeed=3 和火 tick，延迟200实际世界 tick 后复查。每向3971条记录精确一致、1758个非空气方块原生 canSurvive 通过；0.6×1.8×0.6玩家 AABB 的209个可达脚位、八个检查点和30格含外接石地板的入口通路通过；五个空方块实体、13个桌面点缀、10条家具支撑列、五组链灯、三件吊挂物、361格屋顶和5292格无液体边界扫描通过。
3. 渲染：最终同 SHA-256 NBT 的十张1536×1024截图，以及两张四向拼图；内景只做显示剖切，保留横梁与灯链，最终 NBT 不切屋顶。已检查四向内外视图、预览与完整 NBT 入口视点。

证据等级为软件渲染加隔离原生 Forge GameTest；未做整合包客户端实机游玩、大厅接通、持久生成或正式部署。

最终 room.nbt SHA-256：99e2c33ebf29feb673fc6677c027a73fe7f7d158102eb37a7a9abdc612666c67

## 复验

1. 从 C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill 运行：node scripts/run-build.mjs <本目录>/source/build-a03.mjs --out-dir <本目录> --name room --data-version 3465 --json
2. node <本目录>/source/verify-a03.mjs
3. PowerShell：& '<本目录>/source/run-forge.ps1'
4. 从 Minecraft Builder skill 运行：node scripts/preview.mjs --nbt <本目录>/room.nbt --no-open；再将返回的本地 URL 传入 node <本目录>/source/capture-views.mjs <URL>
5. 视觉检查后运行 node <本目录>/source/finalize-a03.mjs，重建总览与哈希清单。

native 是验证专用模组工程，不能安装进玩家实例。Gradle 缓存、build 和 qa/forge-run 一次性世界不属于四件套；保留用于复验。

