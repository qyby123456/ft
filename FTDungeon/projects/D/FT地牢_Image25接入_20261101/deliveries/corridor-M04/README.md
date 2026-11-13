# M04 · 末地淹水吊笼长廊

独立制作交付，**未部署、未改正式模组、未读取或修改玩家存档**。仅在本目录 `qa/forge-run/` 创建一次性隔离 Forge GameTest 世界，以完成实际放置检查；验证 Mod 不可安装到玩家实例。

## 成品

- `room.nbt`：Minecraft Java 1.20.1、DataVersion 3465、gzip、单一 palette；**13×12×41**，6396 条显式方块记录（包括空气）。41 格约 2.56 区块，属于中型。
- `room.json`：尺寸、来源指纹、材料、端口、安全改动与禁止自动部署标记。
- `preview.png`：直接由最终 NBT 经 Minecraft Builder / Lodestone 渲染。`review.html` 为本地图片审阅页。
- `qa.json`、`STATIC_QA.md`、`qa/`：静态检查、真实 Forge 四向放置与 200 tick 延迟复查；`delivery-manifest.json` 为交付文件完整 SHA-256 清单。
- `views/interior-{north,south,west,east}.png`、`views/exterior-{north,south,west,east}.png`：四向内外视图。内部仅显示时移除屋面、近墙；成品结构没有剖开。
- `views/axial-north.png`、`views/axial-south.png`：完整结构内的南北贯通视线；两个 `*-four-views.png` 为四视总览。

## 参考与保留特征

参考图：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M04-end-flooded.png`。

SHA-256：`537f369f2598d7c3f603011fca9b64e69f2793fe458285bc44da1afdeee10eee`。

已核对批次 README、manifest、SPEC、corridor_index 及 M04 提示词。末地石砖奶油色、紫珀柱梁、黑曜石带、末地烛、暖光吊灯、8 组悬吊空笼、墙面藤蔓与浅积水均保留；只用 1.20.1 原版方块，无实体、刷怪笼、命令方块、自动战利品或活跃传送门。

安全改动：原图全幅水面改为两条 3×37×1 浅水池，中间留 **3 格宽、至少 3 格净高**的干燥平直通道；南北门洞均为 **3×4**。地板同一高度，无转弯、分岔、楼梯或强制跳跃。水池与主路底面齐平，不需要上下台阶。

## 坐标与接入边界

所有坐标相对模板原点 XYZ，玩家坐标是脚部，不是脚下地板。

| 用途 | 坐标 |
| --- | --- |
| 北入口 / facing north | `[6,2,0]` |
| 内部安全点 | `[6,2,2]` |
| 南出口 | `[6,2,40]` |
| 三格宽主路 | `x=5..7, y脚=2, z=0..40` |
| 脚下完整地板 | `y=1` |
| 北端口 / 南端口 | `x=5..7, y=2..5, z=0 / 40` |

接入方需提供两端同高地板、先验证**整个 13×12×41 体积**及外接路的占用、实体、液体与世界边界，满足独立持久生成标记和幂等要求后才能放置。显式空气不是授权清空世界；不得直接覆盖已有建筑或玩家修改。旋转端口时与 NBT 同用 Minecraft `StructureTemplate` 原点旋转规则，不能只旋转外框尺寸。

## 验证级别

1. **静态**：参考图和 NBT 指纹、完整体积、连续平直干路、仅南北双端口、完整屋顶、无禁用方块、222 格封闭源水。
2. **Forge 47.4.10 原生隔离**：四种 `Rotation` 实际 `StructureTemplate.placeInWorld`，每向重复放置，共 8 次。比较 6396 条模板记录，触发邻居和水 tick，保留默认 randomTickSpeed=3，强制加载验证区块；放置后延迟至少 200 世界 tick，再验支撑、玩家 0.6×1.8×0.6 AABB 的 123 个主路脚位和双端 18 个外接脚位、扩大一格流体边界。藤蔓自然生长若发生单独记数，不冒充完全一致。通过轮放置原点 Y=200，事先验证扩大场地缓冲区完全无环境液体，没有额外挡水块掩盖模板漏洞。
3. **软件渲染**：最终同 SHA-256 NBT 的 4 张完整外壳、4 张内部剖切及 2 张轴向图；不能称为整合包游戏截图。

**未验证**：完整整合包客户端、帧率、多人、FT 大厅连通、真实存档接入、生成标记、占用拒绝和持久幂等。上述内容由接入负责人后续独立实施，不属于本交付的通过声明。

首轮失败没有删除：`qa/forge-console-initial-aquifer-failure.log`。该轮误选 Y=-56 的洞穴地形测试，下方清出的空隙受到外部天然水流干扰；`qa/fixture-diagnosis.json` 从本次临时世界区域文件读取失败位置 level=7 的水、其上完整末地石砖底板及场地外的天然水源。未改 NBT、未放宽泄漏标准，改用无环境水的高空场地后重新完整执行四向与 200 tick 验证通过。

## 复现

在已安装 Minecraft Builder skill 的目录执行：

```powershell
node scripts/run-build.mjs 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-M04/tools/build-m04.mjs' --out-dir 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-M04/qa/rebuild' --name room --data-version 3465 --json
```

在本交付目录运行 `node tools/verify-m04.mjs`。Forge 使用 JDK17、Gradle8.8 和本目录构建配置运行 `gradle runGameTestServer --offline --console=plain '-Dnet.minecraftforge.gradle.check.certs=false'`，完整日志存入 `qa/forge-console.log`。不要并行运行同一个交付目录的两个 GameTest 进程。

预览使用 skill 的 `scripts/preview.mjs --nbt <本目录 room.nbt> --no-open`；将其本机 URL 传给 `node tools/capture-views.cjs <URL>` 重新生成视图。最后用配有 Pillow 的 Python 运行 `tools/finalize-m04.py` 汇总证据和指纹。
