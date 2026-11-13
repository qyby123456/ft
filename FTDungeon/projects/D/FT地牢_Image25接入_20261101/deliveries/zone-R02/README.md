# zone-R02 · 废弃前哨

独立 Minecraft Java 1.20.1 结构交付；使用 Minecraft Builder / Lodestone 生成。参考图已存在，本次未调用图像生成接口。

## 四件套

| 文件 | 内容 |
| --- | --- |
| `room.nbt` | gzip 单一调色板结构，DataVersion 3465，21×11×21 |
| `room.json` | 北入口、安全点、通路、原图来源与完整 SHA-256 |
| `preview.png` | 从最终实际 NBT 渲染的东南剖视；剖切仅作用于渲染副本 |
| `qa.json` | 静态、视觉、隔离 Forge 四旋转与 200 tick 汇总 |

北入口脚位 `[10,1,0]`，安全点脚位 `[10,1,6]`，朝北。入口为三宽三高；入口通路与指挥桌两侧环路保持三格净高。东侧木封门是封闭陈设，南侧窗有铁栏与完整窗台，不作为第二入口。完整地板 y=0，完整双层屋顶 y=9/10；预览中的无顶和开放墙面不改变最终 NBT。

保留四根带红纹的落地石柱、苔石裂石、红旗、双椅指挥桌、军械架、磨石、空补给箱、四只空木桶、封窗、熄灭火盆、边缘碎石和高处蛛网。桌面浅木板与红白标记代替可领取地图，军械架使用铁条与木构件，无武器实体；火盆不点燃。五盏灯均有底座或连顶链条。没有水、熔岩、掉落物、战利品、刷怪或命令方块。

## 原图与指纹

原图：`D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/ruins/R02-ruins-abandoned-outpost-room.png`

原图 SHA-256：`750661cfb67285196ab621178b07f857da3df6e804593cad03bd9cf301604512`

`docs/manifest.json` 与 `docs/verify.json` 均命中同一原图，1536×1024、RGB、1,972,870 字节。原图为设计概念图，软件预览与隔离 Forge 证据另列。

room.nbt SHA-256：`50a5eaeaba0a791056625fd164bad013a737022d9fff5e5395573305bae00793`

## 验收

- `qa/static.json`：16 项通过；4,851 条显式坐标、2,800 个非空气方块；完整地板与双层屋顶，所有非空气陈设处于落地支撑连通分量；195 个可达脚位。
- `preview/`：四向内部剖视、四向完整外壳、完整屋顶、入口室内视角、东南总览，共 11 张实际 NBT 软件渲染。`four-view-sheet.png` 和 `overview-roof-entrance.png` 供整组审阅。`qa/render-evidence.json` 记录原始结构哈希、剖切状态、记录数和零浏览器错误。
- `qa/four-view-review.json`：逐项视觉审阅通过；家具、入口、安全点、通路、柱梁、灯链、墙体与屋顶均与静态及原生验证交叉核对。
- `qa/forge-native.json`：Forge 47.4.10 / Minecraft 1.20.1 隔离 GameTest，通过原生注册表、全部方块属性、NBT 往返、原生碰撞、方块生存条件、8 个空或装饰方块实体、旗帜纹样、地板屋顶与家具支撑检查。
- 四种旋转实际放置后逐块触发邻居/形状更新；各结构含一格空气外圈的全部相交区块持续强制加载，在正常 randomTickSpeed=3 下确认世界实际推进至少 200 tick，逐旋转复核 4,851 条记录，合计 19,404 条；四次世界回采 NBT 均与源结构旋转后的全部方块状态一致。回采文件及其独立哈希保留在 `qa/`。
- 四次通路阻塞、四次移除旗帜背墙、四次移除台灯桌面的负向探针均被检测；恢复后进行稳定性验收。`forge-process.json` 的退出码为 0，日志记录 `All 1 required tests passed` / `BUILD SUCCESSFUL`。

最终加强验收在全新隔离世界的绝对 Y=280 空中区域进行，放置前确认体积无实体，30 个区块持续强制加载并确认实际推进 200 tick。中间复测曾因测试场地清理产生环境掉落物（火把、种子）失败，未过滤这些实体或关闭掉落掩盖问题；改为全新世界、固定空中坐标后通过，房间 NBT 未改动。此前 PASS/FAIL、日志、进程结果与隔离世界均保留，详见 `qa/fixture-revisions.json`。

隔离测试世界仅位于本交付 `qa/forge-run/`。本次没有部署 JAR、修改正式实例、打开玩家存档、分配 FT 接入槽位或覆盖其它房间。接入主路、占用拒绝、持久幂等、完整整合包客户端与多人性能不属于本房间独立交付的已验证结论。软件渲染器的旗帜纹样显示能力有限，原生 QA 已确认 Java 1.20.1 `Patterns` 数据保留。

## 复现

在 `C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill` 运行：

```powershell
node scripts/run-build.mjs 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-R02/recipe.mjs' --out-dir 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-R02' --name room --data-version 3465 --compression gzip --json
```

在本交付目录依次运行 `node audit.mjs`、`./run-native.ps1`；运行 `node preview-server.mjs` 后，另一个终端运行 `node capture-preview.mjs` 和 Python `make-sheet.py`。确认视觉后运行 `node finalize.mjs`，生成最终汇总与 `manifest.json`。原生测试只在专属隔离工程执行；脚本保留前一次原生结果和日志，并将上次隔离世界移至本目录 `qa/fixture-history/`，再创建全新测试世界。`node verify.mjs` 可在交付完成后只读复核 manifest 全部文件哈希。

可直接打开 `review.html` 离线浏览所有成品视图；也可访问仅绑定本机的 `http://127.0.0.1:53202/` 操作 Lodestone 审阅器。
