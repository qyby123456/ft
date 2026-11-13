# corridor-S04 · 陈旧菌洞短廊

独立制作成品，11 宽 × 9 高 × 16 长。Minecraft Java 1.20.1 原版方块、DataVersion 3465、gzip 单调色板结构。统一登记 ID 为 `corridor-S04`，制作局部 ID 为 `S04`，接收账本预留 64；预留不代表运行时接入。

`room.nbt`、`room.json`、`preview.png`、`qa.json` 是四件套。`preview/four-directions.png` 为四向内外八图总览；每张原尺寸 1536×1024 图独立保留。全部成品视图来自最终 NBT。

## 建筑与接口

短直廊沿 Z 轴一字贯通，保持同高地面，无分岔、转弯、楼梯。北入口脚位 `[5,1,0]`，安全点 `[5,1,3]`；南出口脚位 `[5,1,15]`，南内侧安全点 `[5,1,12]`。全长 `x=4..6, y=1..3, z=0..15` 留空，脚下 `y=0` 有完整干燥地板；屋顶 `y=8` 完整。

土壁与菌丝台地承载四组巨型红菇；五道陈旧苔石拱，中拱刻意缺损冠饰但上方顶盖完整。青色玻璃、海晶灯和竖直末地烛组成青白灯带，角落蛛网、垂根、裂砖与地面苔藓表达陈旧菌洞。装饰退出三格中央通路。

参考图为 `D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S04-fungal-cave-aged.png`，完整 SHA-256 为 `6160f7b8403ec6ee56e01a0e909423f918176c24b4a23981fa547597931c12e7`。已交叉核对原批 README、SPEC、manifest 和 corridor_index 的 S04：fungal-cave / aged / end-rods / broken-arches / short。

原图尽头像封闭板面，成品改为开放南拱满足真贯通。原版诡异菌本身不发光；青白效果来自实体灯方块。没有重新生图，也没有使用自定义贴图或着色器伪装实机光效。

## 验证证据

- `qa/static.json`：1584 个唯一完整体积记录，901 个非空气方块，33 个状态；无越界、实体、方块实体、容器战利品、执行方块或液体；双端 3×3、全长通路、侧壁、顶盖通过。
- `qa/forge-native.json`：Forge 47.4.10 无界面独立 GameTest，实际 `StructureTemplate.placeInWorld` 四向放置，共核对 6336 状态；强制邻接更新后运行 240 tick，四向各有 48 个连续三格通路单元、26 个受支持附着件，零状态漂移、零掉落物。
- `qa/forge-run.log`：含 `S04_FORGE_PLACEMENT_PASS`、`All 1 required tests passed` 和 `BUILD SUCCESSFUL`。`qa/native-roundtrip.nbt` 是 Minecraft 原生读取回写证据。
- `qa/render.json`、`qa/visual-review.json`：八张图的源 NBT 和 PNG 哈希、相机与逐张制作审阅。南北内部图为完整结构内的视线，东西图仅在内存中剖掉近侧墙台与屋顶；最终 NBT 不剖切。
- `qa/delivery-manifest.json`：交付文件哈希。`qa.json` 的 PASS 是制作完成证据，独立接收者的冻结、复验和 acceptance 仍待执行。

## 复现

依赖已安装的 `C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill`，不修改技能脚本。生成：

```powershell
node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs ./recipe.mjs --out-dir . --name room --data-version 3465 --json
node ./tools/inspect.mjs
```

本机 Java 17 为 `C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot`。临时设置进程 `JAVA_HOME` 后，在 `forge/` 使用现有 Gradle 8.8 运行 `--offline --no-daemon --console=plain runGameTestServer`，日志保存至 `../qa/forge-run.log`；之后恢复进程环境。测试只写 `forge/run/s04-isolated-gametest`，必须使用 GameTestServer，禁止指向正式世界。

预览和截图：

```powershell
node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs --nbt ./room.nbt --port 5184 --no-open
node ./tools/capture.mjs
node ./tools/finalize.mjs
```

捕获工具使用本机 bundled Playwright / sharp，默认连接 5184，可由 `S04_PREVIEW_SERVER` 改为该次技能预览实际端口。默认技能预览页支持鼠标旋转缩放。

## 接入边界

未部署、未改正式核心、JAR、游戏配置或玩家存档。测试世界与玩家存档隔离。此处软件视图和 Forge 服务端验证不等同整合包客户端游玩、多人或 FPS 验收。

模板显式包含空气；接入前必须检查整个体积占用、液体和实体，拒绝覆盖已有内容。大厅连接、外部端口、占用拒绝、持久幂等与旧世界迁移由核心接入者验证。接收者应遵守 `handover-20261102-01a19c8f/corridors-20261103/CONTRACT.md`，独立冻结并复验后才能宣布可交统一接入。
