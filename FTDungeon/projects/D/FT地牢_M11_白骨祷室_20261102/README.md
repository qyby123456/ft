# M11 · 白骨祷室（Bone Prayer Chamber）

独立制作工程，2026-11-02（Asia/Shanghai）。只交付 M11，不部署、不改核心、不分配世界槽位、不写正式存档，也不覆盖其他房间或统一 incoming/catalog。

## 交付

- 最小交付：`delivery/M11/room.nbt`、`room.json`、`preview.png`、`qa.json`。
- 接入摘要和完整文件 SHA-256：`handoff.json`。压缩交付：`M11-bone-prayer-chamber-handoff.zip`，压缩包 SHA-256：`package-receipt.json`。
- 建议资源 ID：`ftdungeonmobs:image25/m11`。这只是建议，不代表已经登记或部署。
- 实际尺寸 X×Y×Z：**15×12×17**。北入口脚部 **[7,2,0]**；内部安全点 **[7,2,4]**；朝北 `north`。
- 北开口三格宽、三格高，脚下完整地板，北口向内七格保持三格净空；祭坛左右各有干燥步行通路，后侧连通，六个主题路点可达。

## 原图与转译

参考：`D:/ft工程文件/image2.5/dungeon-rooms-medium-random-20261102/out/M11-bone-prayer-chamber-hero.png`（1536×1024）。

原图 SHA-256：`1660fa43b88cd9bd661a580af3bdaa937c860d0f7ac280d64679860b56616045`。已核验，没有调用生图 API、没有付费重生图，也没有改图像连接设置。

图中“骨饰祭祀室”落实为：灰石砖/裂石砖墙与三道拱肋、骨块与14颗骷髅头、双层壁龛、五面带白色骷髅纹的原生红旗、红陶背景挂毯意象、红布祭坛、空炼药锅仪式盆、15组蜡烛、四臂暖光吊灯与一盏蓝色魂灯。地面暗红砖是无伤害的色斑，不是红石线路或血液机制。

概念图是约13×15×13的设计口径，不是精确坐标图。落地调整为15×12×17，为抬高祭坛、双侧环路、合法支撑、完整屋顶和三格接口留足空间。原版骨块、骷髅头和炼药锅转译概念图里的散骨与圣杯，不引入模型模组。蜡烛不等于开放火方块；本结构没有火、魂火、熔岩或水。

## 证据

- `qa/static.json`：NBT 往返一致、3060个显式坐标、1501个非空气方块、入口/路点、头骨/蜡烛/灯具/旗帜/吊链支撑、255个完整屋顶格，无实体、库存或战利品表。
- `qa/native.json`、`qa/gametest.log`：Forge **47.4.10 / Minecraft 1.20.1** 隔离 GameTestServer，真实 `ServerLevel` 调用 `StructureTemplate.placeInWorld`，四个旋转逐格对比；显式邻居更新后400 tick再次检查。检查原生方块状态、19个方块实体、旗帜图案、支撑、灯烛、屋顶、边界、入口与可往返通路，没有掉落物或新实体。
- `qa/render.json`、`qa/four-views.png`、`qa/exterior.png`、`qa/floorplan.png`：从最终 NBT 读取并使用本机原版1.20.1方块模型/贴图软件渲染，旗帜图案读取实际方块实体数据。**不是游戏截图，不是整合包多人实机游玩证据**。骷髅头和静态旗布采用简单实体网格，未模拟实时光照、旗帜摆动、粒子。剖切只影响显示，NBT屋顶没有删除。
- `qa/gametest-attempt1-network-probe-stopped.log`：首次执行被 ForgeGradle 的公网 SSL 环境探测卡住；核验工程归属后只停止本工程的 daemon，保留真实失败日志。最终采用离线缓存，`-Dnet.minecraftforge.gradle.check.certs=false`仅跳过该前置联网探测；没有修改系统证书或下载配置。

## 接入边界

继承 `D:/ft工程文件/FT地牢_Image25接入_20261101/CONTRACT.md`。模板是 gzip、Java 1.20.1 DataVersion 3465、单一调色板，整个体积包含空气显式记录；**显式空气不是清空许可**。唯一核心接入负责人应先验证完整体积、边界和实体，占用即拒绝；分配槽位和可步行大厅连接，保留每房持久生成标记、幂等行为、个人返程记录、五档难度，不重建玩家已改房间。不得把测试用 verification mod 部署到整合包。

只使用原版方块；无箱、桶或其他库存容器，无命令/结构/拼图方块，无刷怪笼，无模板实体；空炼药锅只是仪式盆，不会发奖励。两处蛛网在后侧壁龛上方，不在主路或安全点。

## 复现

Python：`C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe`（只用其 Pillow 和标准库，不调用 imagegen CLI）。在本工程目录依次执行：

```powershell
& 'C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe' tools/build_m11.py
& 'C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe' tools/render_m11.py
$env:JAVA_HOME='C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot'
& 'C:/Users/YOUR_USERNAME/.gradle/wrapper/dists/gradle-8.8-bin/dl7vupf4psengwqhwktix4v1/gradle-8.8/bin/gradle.bat' --offline --no-daemon --console=plain '-Dnet.minecraftforge.gradle.check.certs=false' runGameTestServer
& 'C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe' tools/finalize_m11.py
```

测试世界仅在本工程 `qa/forge-run/`。再次运行原生放置验收须使用空的独立测试世界，不能改指向正式 `saves/`。生成/渲染工具中的 `nbtio.py`、`mc_models.py`基于只读复用M6独立工程辅助模块，源目录未修改；本房间生成器、渲染适配和验收器保留于 `tools/`。
