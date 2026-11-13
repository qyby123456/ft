# corridor-S12 · 残损档案书库短廊

2026-11-03 独立制作交付。使用已存在的 S12 原图，没有重新调用付费图像生成 API。只写本交付目录；没有部署、修改核心、操作玩家存档或覆盖其他走道。

## 成品

- Minecraft Java 1.20.1，DataVersion 3465，gzip 单调色板结构；Minecraft Builder Skill / Lodestone 配方见 `builder/recipe.mjs`。
- 模板尺寸 **11×10×16（X×Y×Z）**，长度恰好 1 区块。1,760 个显式方块记录，包括 612 格空气；不是清空世界许可，接入方仍须先检查整个包围盒占用。
- 唯一 ID `corridor-S12`，源图 ID `S12`。北入口脚位 `[5,1,0]`，内部安全点 `[5,1,3]`，南出口脚位 `[5,1,15]`；入口默认朝北。
- 中央保留 X=4..6、Y=1..4、Z=0..15 的三格宽四格净高通路。无转弯、分支、可行走高差或关闭的门；屋顶完整。
- 石砖、苔裂砖、四道局部残损拱、64 格书架、深橡木台座、两处无字羊皮卷替代壁旗、六盏灵魂灯、靠墙碎石。四盏灯由铁链悬挂，两盏灯落在石台上。

## 文件与证据

| 文件 | 内容 |
| --- | --- |
| `room.nbt` / `room.json` | 最终结构、入口/出口/安全点、双端口、来源、SHA-256、设计转译 |
| `preview.png` | 从最终 NBT 渲染的技术内部剖视；不是参考图或游戏截图 |
| `qa.json` | 最终静态、四视图、Forge 验收汇总 |
| `views/interior-four-views.png` | 北、南、西、东内部剖视总览，另有四张单图 |
| `views/exterior-four-views.png` | 北、南、西、东完整外壳总览，另有四张单图 |
| `qa/static.json` | 边界、单调色板、完整记录、支撑、屋顶、通路、禁用块、库存检查 |
| `qa/visual-review.json` | 两张四视图总览逐项目视检查及参考图偏差 |
| `qa/forge-native.json` | 原生状态注册表、StructureTemplate 读写、四向实际放置和 240 ticks 稳定性 |
| `qa/forge-run.log` / `qa/forge-invocation.json` | 真实执行日志、独立临时世界位置、调用命令 |
| `qa/native-roundtrip.nbt` | Minecraft 原生模板读写回环副本；不替换最终结构 |
| `forge/` / `tools/` / `builder/` | 本走道独立验收工程、复验脚本、Minecraft Builder 配方 |

### 真实验收结果

- 静态 **PASS**；八张单图与内外四视图总览已生成、已目视核对；最终 NBT 未被剖切修改。
- 隔离的 **Forge 47.4.10 / Minecraft 1.20.1 GameTest PASS**，不是模拟摆放。0°、90°、180°、270° 每份保留 1,760 个状态；合计 7,040 个记录。
- 强制邻居形状和事件更新后，等待 **240 ticks**；全部状态一致、两端与安全点可达、每向有 89 个可站脚位、主路 48 个脚位全部可达、所有灯和壁旗有支撑、无掉落实体和液体。
- 只建立 `qa/forge-runs/` 下可丢弃测试世界。未打开正式世界，未启动整合包客户端；FPS、多人、光影与正式大厅接入尚未测试。最终由统一交接线程按同一哈希独立复验。

## 原图与转译

参考：`D:\ft工程文件\image2.5\dungeon-corridors-20261103\out\S12-archive-library-damaged.png`。

已核对该批 `README.md`、`manifest.json`、`SPEC.md`、`out/corridor_index.json` 与 S12 同名提示词。参考图中的尽端木门改成贯通石拱；卷轴用有墙体支撑的无字白旗近似。破损只出现在装饰拱圈，完整上层屋顶不破洞。无真实危险方块、容器、库存、战利品表和预置实体。

最终 NBT SHA-256：`e1ae152398f270c2cadac810abac9f162a6e501111404de0580cf16ed5ed984c`。

原图 SHA-256：`0aaf6c72391d8b31fd4926350422149041b8cbf10a206b2b833672af1bca39dd`。

## 复现

1. 从已安装 Skill 目录运行 `node scripts/run-build.mjs <本目录>/builder/recipe.mjs --out-dir <本目录> --name room --data-version 3465`。
2. 使用有 Pillow 的 Python 执行 `tools/inspect_delivery.py --render`；读取本机现有 1.20.1 原版模型材质，重新生成静态报告与八视图。
3. 执行 `tools/run_forge.py`，用本机 Java 17 和 Gradle 8.8 离线运行独立 Forge GameTest；每次新建本目录内测试世界，报告不会借用旧成功结果。
4. 重新目视核对后同步 `qa/visual-review.json` 的哈希与结果，再执行 `tools/inspect_delivery.py --finalize`；不允许以旧哈希报告覆盖新结构。

可选 Lodestone 交互预览：从 Skill 目录运行 `node scripts/preview.mjs --nbt <本目录>/room.nbt --no-open --lighting night`，使用启动器实际输出的本地 URL。技术截图与交互预览均不代表客户端实机验收。
