# corridor-S16 · 冰封牢狱·蔓生短廊

独立候选，Minecraft Java 1.20.1 / Forge 47.4.10；不注册正式槽位、不部署、不打开或修改玩家存档。本目录内 `qa/forge-run/world` 仅为本任务新建的隔离测试世界。

## 规格

- 模板 **11 × 10 × 16**；南北直通，16 格长；无转弯、分岔、通路楼梯。
- 玩家脚部坐标：北入口 `[5,1,0]`，内部安全点 `[5,1,2]`，南出口 `[5,1,15]`；北向为 `north`。
- 净通路 `x=3..7, y=1..3, z=0..15`；中线 `x=4..6` 为干燥、不打滑的完整石砖地板。
- 四组拱肋、六扇冰封铁栏牢窗、六串顶板锚定铁链、十二根末地烛、壁面与屋顶直接支撑的藤蔓；完整屋顶和左右封闭外墙。
- 使用永久浮冰/蓝冰，不使用普通冰、霜冰、积雪、流体或会掉落的冰锥。没有实体、刷怪笼、容器、库存、战利品或命令负载。
- 概念图远端封门改为开放拱口，保证“一字贯通”；牢窗为封闭装饰，不作为支路。

## 交付与验收

`room.nbt` 为 Builder/Lodestone 实际生成的 gzip、单一调色板、DataVersion 3465 模板，完整记录包括空气的 1760 个坐标。`room.json` 保存尺寸、入口、出口、原图和结构完整 SHA-256。`preview.png` 从实际 NBT 渲染，不是 AI 图或客户端截图。

`qa.json` 是最终状态入口；只有它、`qa/static.json`、`qa/four-view-review.json` 和 `qa/forge-native.json` 全部为 PASS 且绑定同一最终指纹，才算独立验收通过。`qa/forge-process.json` 还记录 Forge 进程退出码，避免仅依靠日志片段判断成功。

- 静态：完整体积、原图指纹、1 区块长度、原版调色板、无实体/库存/流体、五格净路、三格干燥中线、屋顶、左右外壳、藤蔓与铁链支撑。
- 四视图：`preview/north.png`、`south.png`、`west.png`、`east.png`；补充 `exterior.png`、`roof.png`、`perspective.png`、`reverse.png` 与 `four-view-sheet.png`。剖切只修改渲染副本。
- 原生：隔离 Forge GameTest 四向实际放置，原生注册表/状态/碰撞/支撑/通路检查，全体邻居与形状更新，200 tick 稳定性，以及四向世界回采与最终模板逐格比对。包含各四次堵路与缺失藤蔓支撑的反例探针。
- 藤蔓自然蔓延在隔离 fixture 中关闭，随机刻速度仍是默认值；未来正式世界若允许藤蔓生长，它可能正常蔓延。

测试过程中修正了空模板/资源包元数据与测试模板命名空间；另为隔离 fixture 增加一格空气外圈，避免边界铁栏连接测试地形，并把不足的 512 MiB 测试堆提高至 1 GiB。历史日志保留为 `qa/previous-*-forge-run.log`。内存不足时 Gradle 曾返回 0 但没有原生 PASS，因此运行脚本与最终封装都要求当前结构指纹一致的真实原生 PASS，不能把退出码或 BUILD SUCCESSFUL 单独当作成功。

## 原图

`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S16-frozen-prison-overgrown.png`

SHA-256：`253f2d0d39dd3a642e97a1022ad95c9f14e3fc7a321e18769430ae8c16f39fc4`。遵循该批次 `README.md`、`manifest.json`、`SPEC.md`、提示词及接入目录 `CONTRACT.md`；未调用付费生图。

## 重现

在本目录运行；使用已安装的 Minecraft Builder，不改动其脚本：

```powershell
node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs ./recipe.mjs --out-dir . --name room --data-version 3465 --json
node ./audit.mjs
./run-native.ps1
node ./preview-server.mjs
```

预览服务使用 `http://127.0.0.1:53116/`。另一个终端运行 `node ./capture-preview.mjs`，再用配置的 Python 与 Pillow 运行 `make-sheet.py`。人工查看全部最终视图后执行 `node ./finalize.mjs`，它要求全部真实证据通过且指纹一致，否则拒绝生成最终 PASS。

## 接入边界

接入负责人应分配唯一 `corridor-S16` 槽位，不与旧房间 S16 或其他批次共用标记。首次施工前检查**包括空气的整个体积**、边界、流体与实体，遇占用必须拒绝，不能因为模板显式记录空气就清除玩家建筑。正式道路、占用拒绝、幂等、返程门、多人和整合包客户端性能不在本次独立 QA 范围内。
