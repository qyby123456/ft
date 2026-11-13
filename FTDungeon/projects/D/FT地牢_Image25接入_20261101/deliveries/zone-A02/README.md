# A02 塌毁卧室 · 独立结构交付

本交付位于 zone-A02 专属目录，四件套为 room.nbt、room.json、preview.png、qa.json；附建造配方、八张四向内外预览、静态与 Forge 原生验收证据。没有部署、修改正式模组或打开玩家存档。

## 尺寸与布局

- Java 1.20.1，DataVersion 3465；21 × 12 × 21，5292 格显式记录，其中 2152 格非空气。
- 北入口相对坐标 (10,2,0)，南出口 (10,2,20)，安全点 (10,2,10)。坐标代表玩家脚部，地板位于 Y=1。
- 主路 X=9..11、Z=0..20，三格宽、四格净高。红灰破毯嵌入地板，主路没有地毯高度差。床、床头柜、衣物、柜体和落石均位于侧区。
- 四柱位于 X/Z=(3,3)、(17,3)、(3,17)、(17,17)，柱周留空，柱脚落地、柱顶接梁和顶板。东北角虽有塌顶，保留该柱完整顶帽。
- 东北角设计性塌损 48 格顶面，其余 393 格顶面保留；边墙呈阶梯式残口，落石与断木落在同侧完整地板上。四盏灯均通过链条挂在承重梁下。
- 参考中的裂镜以灰色玻璃嵌墙表现；倒柜为贴地深色木柜残骸，衣物用侧区地毯表示，残帘用贴墙纹样旗帜表示。原版床仍具有原生交互；本交付没有添加游戏机制、刷怪或奖励。

## 参考与指纹

参考图：D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/abandoned/A02-abandoned-collapsed-bedroom.png。

参考图 SHA-256：946dadb0601ffabfb9257ad4ef5455225a146fa6be1abcb324ddac169ee833b4。

已核对批次 README、manifest.json、verify.json 和 A02 提示词；来源为概念图，不是实机截图。

最终 room.nbt SHA-256：0c9c677cc9e2fd9c15e2dc3341680a490e9920ff90a769375413a68c901eca33。

## 验收

- qa/static.json：全部体积显式编码、边界、原版方块、床配对、家具支撑、落地连通性、四柱留空、灯链锚点、屋顶塌口、三格主路与六个地标通路均通过。
- qa/visual.json：Minecraft Builder / Lodestone 对最终 NBT 进行 SHA-256 核验后渲染北东南西四向，各含内剖与完整外观，共八张 1536×1024 PNG；已人工视觉复核。内剖只在浏览器中隐藏近侧墙和顶板，交付 NBT 保持完整设计。
- views/eight-view-sheet.png：八视图总览。preview.png：南向内部剖视。预览灯光和特殊方块属于渲染近似，不是客户端截图。
- qa/native.json：隔离 Minecraft 1.20.1 / Forge 47.4.10 GameTestServer，实际 ServerLevel 调用 StructureTemplate.placeInWorld，四旋转各 5292 格，初始及显式邻居更新后 400 tick 两阶段逐格一致，每阶段合计 21168 格。真实注册表、状态参数、NBT 往返、canSurvive、家具与吊灯支撑、床/旗帜方块实体、0.6×1.8 玩家体积碰撞、双向步行和六地标、边界及无掉落实体均通过。
- 成功日志：qa/gametest-20261103-135843.log，含 A02_NATIVE_GAME_TEST_PASS rotations=4/4 records=21168 ticks=400。
- 失败证据保留：qa/static-attempt1-roof-mask.json（首次静态屋顶掩码漏计边墙塌口，已修正）；qa/gametest-20261103-124624.log（首次测试资源包缺少 pack.mcmeta，空夹具未加载，补齐后通过）。未绕过原生验收。

## 复现

在已安装 Minecraft Builder 的同一环境中运行：

~~~powershell
node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs ./tools/recipe.mjs --out-dir . --name room --data-version 3465
node ./tools/static-qa.mjs
& ./tools/run-native.ps1
node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs --nbt ./room.nbt --port 5232 --no-open
node ./tools/capture-views.mjs 'http://127.0.0.1:5232/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=A02'
~~~

原生运行器只设置进程级 JAVA_HOME，完成后恢复；复用本机 Gradle/Forge 缓存，每次创建 A02 目录内的新隔离世界。测试世界、Gradle 缓存、编译产物不进入交付 ZIP。该验收不代表完整整合包客户端、多人游玩或性能验收。

NBT 包含显式空气，应由未来接入流程先检查完整槽位为空，禁止直接覆盖已有建筑。当前只交付候选结构，没有接入任何正式目录。

