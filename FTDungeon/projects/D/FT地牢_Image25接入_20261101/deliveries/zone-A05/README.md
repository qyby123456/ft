# A05 废弃储藏室 · 独立交付

2026-11-03，Asia/Shanghai。Minecraft Builder 配方生成；仅写本交付目录，未部署、未打开正式玩家世界、未修改 FT 核心或并行房间，未调用付费生图。

## 四件套与结构接口

- `room.nbt`、`room.json`、`preview.png`、`qa.json`，附可复现配方、参考副本与验证证据。
- ID `zone-A05`，来源图 `A05`；Java 1.20.1 / DataVersion 3465，gzip 单调色板，21×11×21（XYZ）。
- NBT SHA-256：`24d4a8e622bc191797b3d62af7478c074192eb16a6bd13454d6faa534cd96d40`。
- 4,851 条显式记录，2,422 个非空气方块；441 格连续基础、441 格完整屋顶。
- 北入口脚位 `[10,2,0]`，内部安全脚位 `[10,2,4]`，`facing=north`；入口三格宽、四格高。房间为单入口储藏室，经原入口返回。
- 主通路 `x=9..11,z=0..19,y=2..4`：三格宽、三格高、连续20格；原生碰撞检查确认277格脚位彼此连通。
- 四座单箱、十九只木桶，23个原生容器共621槽全部为空，无战利品表/种子、无锁、无实体奖励。

## 参考与落地偏差

参考图：`D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/abandoned/A05-abandoned-abandoned-storage-room.png`。
图 SHA-256：`a04d973577416ee3eb03731a510975c77f62a64240227ba5ce614167e2eb70be`。
已核对来源 `README.md`、`docs/manifest.json`、`docs/verify.json` 与原提示词，并将只读副本保存在 `reference/`。

保留石砖/苔砖、木质绑带石柱、两侧及后墙木货架、空箱桶、空陶罐、上部蛛网、链悬灯与地面废木料。货架贴墙并有完整落地底座，石柱位于主通路两侧，不与货架相穿。

1. 概念图为剖视；实际NBT补齐四壁和屋顶。软件预览仅隐藏近墙和顶层，原件保持完整。
2. 图中谷物、倒置破箱、陶罐使用原版木板/活板门、空花盆和空箱桶近似，没有添加可领取物资或掉落实体。
3. 浅水反光以完全氧化铜地砖表现水渍，没有活水；蛛网仅在远离主路的上部角落。
4. 石柱按成图的柱列观感落为六根独立落地柱；原提示词要求四柱，因此此处记录为数量偏差。北墙新增便于接入的三格宽门洞。
5. 21×11×21为本次落地尺度，概念图未给出尺寸；预览使用较亮检查光，未复现图中的整体暗光、破灯和反射。

## 验证证据

- `qa/static.json`：87项检查通过；实际重读NBT，核验指纹/尺寸/坐标、材料、完整底顶、柱架支撑、入口/安全点/主通路、23个空容器、箱盖净空。
- `qa/render.json`、`preview/`：Lodestone 0.6.0 从最终 `room.nbt` 渲染北/南/西/东完整外观4张、剖视内部4张，另有正反向眼平视图2张。`preview.png`为北向剖视，八向总览在 `preview/four-direction-interior-exterior.png`。
- `qa/visual-review.json`：目视核对四向内外图及正反向眼平图；视图为结构软件渲染，不是实机截图或AI图。
- `qa/native.json`、`qa/forge-gametest.log`：隔离Forge47.4.10 / Java17，实际GameTestServer / ServerLevel / StructureTemplate.placeInWorld四旋转放置，总19,404条记录；相邻形状更新后继续400 tick，再次逐格对照。四向均为零状态漂移、零掉落实体、零液体外溢。
- 每方向初始及400 tick后均检查2,422个非空气方块的原生 `canSurvive`、441格完整屋顶、主路往返、23个容器621槽为空、四箱箱盖及菜单可用。对每个容器，从可达脚位作4.5格内原生视线射线，确认其可触及。
- `qa/native-roundtrip.nbt`和四份 `qa/native-placed-*.nbt`保留原生往返及400 tick后的世界导出；`qa/native-snapshots.json`用独立Lodestone重读，比对24,255条旋转记录和115次空容器内容。
- 最终 `qa.json`绑定交付、源代码、参考图及上述证据SHA-256。Forge首次完整运行即通过；日志保留原始完整输出。

## 复现与边界

`./Run-QA.ps1 -Rebuild`使用本地已安装 Minecraft Builder、Gradle8.8/Forge缓存与Java17，在本目录创建新的独立测试世界；不启动正式整合包或写入正式 `saves`。

在本目录运行 `node tools/preview-server.mjs 5655`，另一个终端运行 `node tools/capture-preview.mjs http://127.0.0.1:5655/` 可重拍固定视图；预览只绑定本机回环地址。使用技能默认交互预览也可在技能目录执行 `node scripts/preview.mjs --nbt "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-A05/room.nbt" --port 5656 --no-open`。

核对视图后执行 `node tools/finalize.mjs --visual-reviewed`更新最终证据指纹。完整整合包客户端、多人、着色器和FPS未验收；独立测试通过不代表已经部署。正式接入仍需验证目标占用、实体/液体、边界、持久标记与返程，显式空气记录不构成覆盖已有建筑的授权。
