# S11 · 烬狱吊笼短廊

独立短直廊候选，13×12×16 方块，Minecraft Java 1.20.1 / DataVersion 3465。16 格一字贯通，无转弯、无分岔、无地面台阶。红地狱砖与焦黑石砖、四组屋顶锚定吊笼、六支暖色壁火把，保留参考图的地狱焚毁主题。

四件套为 `room.nbt`、`room.json`、`preview.png`、`qa.json`。额外提供 `interior-four-views.png`、`exterior-four-views.png`、`previews/`、复现工具 `tools/` 和独立 Forge GameTest 工程 `forge/`。

- 参考图：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S11-nether-burnt.png`。
- 参考 SHA-256：`2bdda4f75bf87f19ae80540a5de09af4b720dde05e515cdcd2631fba795d65a6`，与 manifest / corridor_index 的 S11 条目一致。
- 最终 NBT SHA-256：`3e4d7506ba5dcec155bda6ed2260e6825e930495bcccffd2640ef6a7c5c1f387`。
- 北入口脚位 `[6,3,0]`，内部安全点 `[6,3,3]`，南出口脚位 `[6,3,15]`，南侧内部点 `[6,3,12]`。两端朝向 north / south，开口五格宽、四格高；中央五格宽主路三格净空，80 个主路脚位连通。
- 真熔岩只在侧边 24 个封闭源单元内：黑曜石底、四面完整边界、橙色玻璃齐平盖板。完整永久屋顶在 y=11。四视图剖切只作用于内存渲染，不改 NBT。

## 验收证据

`qa/static.json` 18 项通过：gzip、单调色板、原版命名空间、完整 2496 坐标、双端开口、平直安全主路、完整屋顶、无侧分岔、熔岩封闭、火把和吊笼支撑、无实体/战利品/禁用块等。

`qa/forge-native.json` 使用 Forge 47.4.10 的隔离 GameTest 服务端，真实 `StructureTemplate.load/save/placeInWorld`，四个旋转分别放置 2496 坐标，强制邻块形状/事件更新和熔岩源 tick，再观察 240 tick。每向保持 2496 状态、80 个实际碰撞可走脚位、24 个熔岩源、6 支火把、8 条顶锚链、38 个铁栏和32块关闭铁活板门；零泄漏、零掉落。日志 `qa/forge-run.log` 为 BUILD SUCCESSFUL。完整原生往返 NBT 保存在 `qa/native-roundtrip.nbt`。

首轮测试夹具使用普通随机地形，附近天然岩浆导致夹具边界扫描失败；该次日志保存在 `qa/forge-attempt1.log`。最终测试把四个夹具统一移至 y=290，先确认无液体再放置；最终 NBT 未改动，四向通过。

`preview.png` 主图来自 Lodestone 0.6.0 ThreeStructureRenderer 对最终 NBT 的眼平一点透视，右侧为同一 NBT 的材质剖视；`qa/lodestone-preview.json` 记录相机、灯光、输入 SHA。内外各四视图使用 1.20.1 原版材质软件渲染并人工视检，详见 `qa/renders.json`。这些不是整合包游戏客户端截图，也不代表 FPS、多人或客户端游玩测试。

## 本机复现

Builder 位于 `C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill`，Python 位于 `C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe`。在 Builder 目录运行 `node scripts/run-build.mjs "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S11/tools/corridor-s11.mjs" --out-dir "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S11" --data-version 3465 --json`，再运行本目录 `tools/check_delivery.py` 和 `tools/render_views.py`。

Builder 本地预览：`node scripts/preview.mjs --nbt "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S11/room.nbt" --no-open --port 8131`。预览服务启动后运行本目录 `node tools/capture_preview.mjs`，再运行 `tools/make_preview.py`。

Forge 独立测试在 `forge/` 目录，用 Java 17 启动已安装 Gradle 8.8：`runGameTestServer --offline --no-daemon --console=plain -Dnet.minecraftforge.gradle.check.certs=false`。该工程的运行目录固定在交付内 `forge/run/s11-only-gametest`；不会访问玩家存档。最终运行 `tools/finalize_delivery.py --visual-reviewed`，此参数须在确实检查四视图与预览后使用。

本交付未部署、未修改地牢核心目录或连接路线、未修改玩家世界。未来接入须先校验整个体积的空闲条件，不能向占用区无条件覆盖本模板。
