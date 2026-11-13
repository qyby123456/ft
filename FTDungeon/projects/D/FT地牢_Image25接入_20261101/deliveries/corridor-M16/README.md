# corridor-M16 · 苔石涵泉长廊

Minecraft Builder / Lodestone 制作的独立中型直廊候选。**未部署、未修改正式 JAR／配置／玩家存档；制作端 QA 通过不等于接收者已验收或已接入。** 不与旧房间 M16 或其他 M 编号共用 ID，正式标识始终为 `corridor-M16`，槽位留给统一接入负责人分配。

## 文件与接口

- `room.nbt`：Java 1.20.1、DataVersion 3465、gzip、单一 palette。15×11×40，纵向 40 格＝2.5 区块。6600 条记录完整编码体积，包含空气。
- `room.json`：北入口脚位 `[7,2,0]`，内部安全脚位 `[7,2,4]`，南出口脚位 `[7,2,39]`；北为 `z=0`，南为 `z=39`。相对模板原点坐标。
- 北南开口均宽 5、平直净高至少 4；中央 `x=5..9` 的步道全长同高，地板 `y=1`，脚位 `y=2`，全长保证三格净空，无楼梯、转弯或支路。
- `preview.png` 是最终 NBT 的眼平内景软件渲染，**不是原图、AI 成品替代图或游戏客户端截图**。
- `views/` 有北南西东四向内部剖视及完整外壳，共八张 1536×1024 图片；`contact-sheet.png` 为总览，`manifest.json` 绑定输入 NBT 与图片哈希。
- `qa.json` 为汇总；`qa/static.json`、`qa/visual-review.json`、`qa/forge-native.json` 分别为静态、制作端人工核图和真实 Forge 放置报告。源文件与日志、清单见 `delivery-manifest.json`；交接入口为 `handoff.json`。

## 参考与取舍

参考：`D:\ft工程文件\image2.5\dungeon-corridors-20261103\out\M16-flooded-intact.png`。

原图 SHA-256：`3dd8be10bea89290f99a9af69dd387ed6ed38da08d08bf034fe1d83063cc7f1a`。

遵循同批 `SPEC.md`、`README.md`、`manifest.json`、`out/corridor_index.json` 及父工程 `CONTRACT.md`。保留完整灰／苔石砖墙、木梁、暖灯、六个悬空小铁笼、青色水体与湿润质感。

安全性优先的已知偏差：原图满地浅水改为两侧浅水渠，中央五格为干燥同高石道；原图敞开侧壁泉改为四根正面青色玻璃封闭、背面／侧面石砖密封的真实水柱。共有 208 格水源，底／侧边界及端部挡水完整。小笼为静态铁栏／半砖装饰，没有生物、陷阱逻辑、刷怪笼、容器或战利品。12 盏吊灯、42 节竖链均接木梁，56 格藤蔓贴墙。

## 验证证据

- 静态：最终 NBT 反读，完整体积／唯一坐标、北南开口、200 格五宽干路、双端可达、600 格屋顶与600格基础、每格水源的四侧／底边界、灯链藤蔓支撑、无实体／方块实体／容器检查通过。
- 视觉：制作端实际打开并检查北南西东全部八张图，以及眼平预览；无明显通路遮挡、悬浮灯链、柱梁脱节、屋顶破口或水道开口。内部剖切只移除内存中的屋顶与对应观察侧外墙；剖面露出的水体并非最终 NBT 泄漏。
- 原生：独立 Forge 47.4.10 / Minecraft 1.20.1 GameTest，原生注册表及全部方块状态属性、`StructureTemplate` 反读回写检查通过；NONE／顺时针90／180／逆时针90四种旋转均实际放置，各保留6600条方块状态。
- 强制源水 tick 与邻居状态更新，持续观察 240 tick；四种旋转均验证208格水源、200格干路、212格碰撞可达脚位、完整顶底、吊件支撑、藤蔓存活；零漏水、零掉落物、零库存。执行退出码0且日志含 `BUILD SUCCESSFUL`。
- 临时世界只位于本目录 `forge/run/20261103-100815/`，仅加载本地 QA 模组；并未启动整合包客户端或加载玩家世界。无客户端画面／多人／FPS／大厅接线／占用拒绝／持久幂等接入证据。接收者仍须针对最终哈希独立验收。

## 复现

Node：`C:\Program Files\nodejs\node.exe`。Python：`C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`。Java17 与 Gradle8.8 使用当前本地已缓存运行时，Forge 离线构建，不安装到正式 `mods/`。

```powershell
$delivery = 'D:\ft工程文件\FT地牢_Image25接入_20261101\deliveries\corridor-M16'
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
node "$skill\scripts\run-build.mjs" "$delivery\tools\build.mjs" --out-dir $delivery --name room --data-version 3465 --json
node "$delivery\tools\static-qa.mjs"
& 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe' -B -X utf8 "$delivery\tools\run-forge.py"
node "$skill\scripts\preview.mjs" --nbt "$delivery\room.nbt" --port 5266 --no-open
```

另一个终端运行 `node "$delivery\tools\render-views.mjs" http://127.0.0.1:5266/`，生成预览与四向图。渲染只修改浏览器响应，不修改已安装 skill 或 NBT。重新生成图或 NBT 后，必须再次实际打开检查全部视图，再执行 `tools/finalize.py`；该脚本仅用于本次已人工核看版本，不是自动视觉验收器。它会严格核对静态、原生和图像哈希。

接入必须先检查**整个结构体积，包括空气坐标**，与世界边界、液体和实体占用；不得把完整空气记录当作无条件清空授权。保持独立持久生成标记，拒绝占用，禁止覆盖既有玩家改动。复核、分配唯一槽位与部署由统一接入负责人另行执行。
