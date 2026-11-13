# zone-M04 · 熔岩矿脉

独立地牢分区房间，ID 固定为 **zone-M04**；对应分区概念图的 M04 矿井，不是 `corridor-M04`。本目录为候选交付，未部署、未接入正式工程、未修改正式配置或玩家存档。Forge 只使用本目录下新建的隔离测试世界。

四件套：`room.nbt`、`room.json`、`preview.png`、`qa.json`。模板为 Minecraft Java 1.20.1、DataVersion 3465、gzip 单调色板结构，**25×14×25**，完整记录 **8750** 个坐标（包括空气）。北入口为玩家脚部坐标 **(12,3,0)**，3 格宽、4 格高；安全点 **(12,3,4)**，入口内有 5×5 落点。入口外部接路的地板高度应为模板相对 Y=2。

主路 X=11…13、脚部 Y=3、Z=0…23，3 格宽、至少 3 格净高，同高穿过中央石桥。四根支架位于 (X,Z)=(4,5)、(20,5)、(4,20)、(20,20)，从地面连续连接梁与屋顶；六盏吊灯均由链条连接完整锚点。保留深板岩/玄武岩、铁箍木构、矿脉、矿轨、煤车、矿箱、铁砧和锻炉。煤车为落地静态方块装饰，没有矿车实体。

原图 SHA-256：`8f286ff92140350e6a640892d251f870954b4e1ead0117ae5cce2f50950d5ac2`。

最终 NBT SHA-256：`a52413917ad6cd7f9db2627b2a98fb198472e122a1aa9b7434666449808b2e67`。

原图来自 `D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/mines/M04-mines-lava-seam-mine.png`；已与该批 README、manifest、verify 及提示词核对，来源证据和原图副本见 `reference/`。本次没有调用付费生图 API。

## 危险隔离

原图开放熔岩转译为 **57 格真实源熔岩**的全封闭地槽：X=3…21、Y=1、Z=13…15；底为完整石块，四周玄武岩，顶为完整橙色玻璃，桥下改为完整安山岩。所有六个面均为源熔岩或完整封闭块，没有可接触熔岩的开口。石桥两侧及槽周围增设铁栏。木构和木箱远离熔岩可能的点火搜索范围，锻炉用封闭的固体光源与玻璃，没有真实火、燃料或奖励库存。玻璃和护栏可由玩家破坏，隔离结论适用于模板完好放置状态。

全屋顶 625 格完整顶盖，只有北入口一个开口。内部剖切只改变渲染时的内存副本，不修改最终 NBT。NBT 显式空气也不能作为正式世界无条件清空授权；接收方仍需实现体积占用拒绝与安全接路。

## 验证证据

- `qa/static.json`：原图指纹、8750 个唯一有界坐标、禁用方块、完整屋顶、四柱、六灯、落点、72 格主路、六面熔岩封闭和可燃物间距共 10 项通过。
- `views/interior-{north,south,west,east}.png`、`views/exterior-{north,south,west,east}.png`：最终 NBT 四向内外图；两个 `*-four-views.png` 为总览。`entrance-north.png`、`bridge-south.png` 是保留屋顶与墙体的内部观察。均为 Minecraft Builder / Lodestone 软件渲染，非游戏截图。
- `qa/visual-review.json`：逐图审阅屋顶、入口、动线、支架、吊链、设备和护栏，保留修订前横梁被链条替换的基线，修订后梁连续。
- `qa/native.json` 与 `qa/forge-20261103-124920-534.log`：实际 Forge 47.4.10 GameTest，四个旋转各放置两次，邻居更新与显式液体 tick；每次 8750 状态完整比对、`canSurvive`、玩家 0.6×1.8 碰撞体、81 格含接路主路、25 格落点、屋顶、柱、空库存、扩展液体/火边界、实体/掉落检查通过。400 个服务器 tick 后四向复查通过；开启火焰更新，并额外调用 **4332 次原生熔岩 randomTick**，57 格源熔岩保持，无泄漏、起火或掉落实体。
- `qa/native-placed-*.nbt`：四旋转 400 tick 后从实际世界回采；`qa/capture-comparison.json` 独立重新解码并逐坐标、逐声明状态比对 **35000** 条记录，零差异。
- `delivery-manifest.json`：四件套、源配方、验证代码、原图来源、视图和最终报告的完整 SHA-256。`handoff.json` 为接收摘要。

原生测试使用干燥高空夹具，周围没有额外挡液体方块，只在每个北入口外加九格地板以验收接路；测试区块强制加载。空桶与两台空炉由原生放置自动建立方块实体，检查空库存和无 LootTable。首次启动器被 javac 警告中止的日志、初版 NBT/视图保留于 qa，不作为最终通过证据。最终 Gradle 构建成功且 1/1 必需 GameTest 通过。

## 复现与接收边界

生成：在 Minecraft Builder skill 目录运行 `node scripts/run-build.mjs "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-M04/tools/build-room.mjs" --out-dir "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-M04" --name room --data-version 3465 --json`，然后运行本目录 `node tools/verify-room.mjs`。

原生验证：运行 `powershell -NoProfile -ExecutionPolicy Bypass -File tools/run-forge.ps1`，需要本机 JDK17、缓存 Gradle8.8 与 Forge47.4.10；脚本每次在本目录新建测试世界，不启动玩家实例。成功后 `node tools/compare-captures.mjs` 独立核对回采。

预览：skill 的 `scripts/preview.mjs --nbt <本目录 room.nbt> --port 5394 --no-open`；将返回的本地地址传给 `node tools/capture-views.cjs <URL>`。配有 Pillow 的 Python 运行 `tools/make-sheets.py`。最终 `node tools/finalize.mjs` 汇总证据与文件哈希；视图人工审阅结论仍需由审阅者核对。

正式接入、与大厅的路线连接、占用拒绝、持久化和幂等、整合包客户端与多人游玩未在本次执行；不能据隔离 GameTest 宣称它们已验收。交付标记 `ready_for_unified_integration=false`，待统一接收方独立复验。
