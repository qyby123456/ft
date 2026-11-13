# C04 焦毁塌角 · 独立交付

Minecraft Builder / Lodestone 0.6.0 构建。Minecraft Java 1.20.1、DataVersion 3465、gzip 单调色板结构；尺寸 **25 × 13 × 25**，8125 个显式坐标记录，其中 3744 个非空气方块。成品包含完整地面、双层外墙和屋顶。

设计源为 `D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/corners/C04-corners-burnt-corner-collapse.png`，并与同批 README、manifest 和 verify 核对。

- 原图 SHA-256：`2c960f11d1cb074939998fd525e3059bd3c21adae2c7c005c75de3317d96ce0c`
- room.nbt SHA-256：`aca307fde4fc5fba665124a9afff3772a3c29d1308ed249084423cc2dcb64bc5`

## 布局与接口

北入口脚位 `[12,1,0]`，内部安全点 `[12,1,4]`，东出口脚位 `[24,1,12]`。两开口均三格宽、四格高。保护路线为 `x=11..13,z=0..13` 与 `x=11..24,z=11..13` 的并集，合计 75 个地面脚位；其上三格全空气，可平层往返。

焦毁塌损集中西南角，以深板岩碎石、持续相接并落地的倒梁、黑红嵌地毯、红旗和烟熏墙体表现；四根带深色石箍的石柱、木架、空桶、蜡烛桌和四盏链吊灯保留原图主题。余烬使用菌光体与橙色玻璃，结构没有真实火、熔岩或其它路线危险方块。所有桶均为空且没有 LootTable；无预置实体、刷怪笼、命令或拼图方块。

## 预览与证据

- `room.nbt` / `room.json`：最终模板、接口与原图指纹。
- `preview.png`：实际最终 NBT 的软件渲染。
- `preview/interior-sheet.png`：北、东、南、西内部剖切视图。
- `preview/exterior-sheet.png`：完整外壳四向视图。
- `preview/eye-sheet.png`：北口、东口完整结构眼平视角。
- `qa.json`：汇总结果；`qa/hashes.json`：交付文件指纹清单。
- `qa/static.json`：尺寸、坐标、实体、全封顶、完整地面、保护路线、结构连通、危险方块与参考图核对。
- `qa/views.json` / `qa/visual-review.json`：10 张单视图的来源哈希、相机与逐图视觉检查。
- `qa/forge-native.json` / `qa/forge-run.log`：Forge 47.4.10 隔离 GameTest 的四次旋转放置、方块属性与实体原生加载、碰撞可达、支撑、生存性、全状态保留、强制邻居更新与 240 tick 稳定性。
- `qa/native-roundtrip.nbt`：Minecraft 原生模板读写结果，仅为 QA 证据。

预览剖切只发生在渲染器内存中，最终 NBT 不开顶。预览是 Lodestone WebGL 软件渲染；Forge 证据来自独立测试服务端，不声称为整合包客户端实游截图或多人性能测试。

首轮 GameTest 使用默认地下地形，外部天然熔岩在邻居更新后从设计开口流入，日志保留在 `qa/forge-attempt1-natural-terrain-fluid.log`。最终四个测试夹具位于测试世界绝对 Y=200，隔离自然地形液体后重测；此修正没有改变 room.nbt。

## 复现

在 Minecraft Builder 技能目录运行 `node scripts/run-build.mjs "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-C04/source/recipe.mjs" --out-dir "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-C04" --name room --json`。

在本交付目录运行 `node source/static-qa.mjs`；运行 `source/run-forge.ps1` 进行隔离 Forge 测试。预览服务使用技能自带 `scripts/preview.mjs --nbt <本目录/room.nbt> --no-open --port 5244`，随后在本目录运行 `node source/render-views.mjs 5244`、`node source/contact-sheets.mjs` 和 `node source/finalize.mjs`。

本次仅写入 zone-C04 独立交付目录，没有部署，也没有修改正式存档、JAR、资源包、配置或其它房间。接入方须按契约检查整个目标体积的占用和两个接口外侧的地形、液体、实体，不能将模板显式空气当作无条件清空授权。
