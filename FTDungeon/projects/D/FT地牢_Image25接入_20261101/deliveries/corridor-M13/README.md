# M13 · 苔覆遗迹断拱长廊

独立中型直廊交付，2026-11-03（Asia/Shanghai）。本目录不注册地牢、不部署 JAR、不修改正式实例或玩家存档。

## 核心四件套

| 文件 | 用途 |
| --- | --- |
| `room.nbt` | gzip 原版 Minecraft Java 1.20.1 结构模板，DataVersion 3465 |
| `room.json` | 相对坐标、双端接口、尺寸、参考图片及完整指纹 |
| `preview.png` | 从最终 NBT 渲染的北入口室内视角，不是概念图 |
| `qa.json` | 静态、四视图和隔离 Forge 放置稳定性汇总 |

NBT SHA-256：`b4f5287ca4bbe460b9217f2e23f4337814afaa23d24cf5ed5493ebe92a233709`。

## 结构与接口

- 尺寸 X×Y×Z：**13×11×40**；沿南北 Z 轴延伸 40 格，即 **2.5 区块**。无转弯、分支或通路台阶。
- 北入口脚位 `[6,1,0]`，内部安全点 `[6,1,4]`，南出口脚位 `[6,1,39]`。坐标均为模板原点相对整数 XYZ，脚下地板在 Y=0。
- 双端保证中央 **3 格宽×3 格高**净空；中央 X=5..7、Y=1..3、Z=0..39 全是空气，通路地板为连续完整方块。
- 六道石拱位于 Z=0/8/16/24/32/39；中间三处拱角缺损表现废墟状态。5 盏暖色吊灯由完整三节铁链连接到屋顶。
- 灰绿苔石、裂纹石砖、336 格贴墙藤蔓、侧边苔藓和不腐烂树叶、深色木嵌墙；全部使用 1.20.1 原版方块。
- 模板共 5720 条记录（含显式空气），非空气 3383 格，20 种实际方块状态。无实体、方块实体、战利品、刷怪、液体、火或命令设施。

## 参考来源

已核对 `D:/ft工程文件/image2.5/dungeon-corridors-20261103/README.md`、`manifest.json`、`SPEC.md`、`out/corridor_index.json` 与 M13 提示词。

- 原图：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M13-overgrown-ruins-overgrown.png`
- 原图 SHA-256：`1fdf0434a35e4261f88d3de7b228deaba30f1b0af06ad467d723c4509fba9e66`
- 矩阵：overgrown-ruins × overgrown × lanterns × broken-arches；gray-green stone / green vines / moss / dark wood。
- 采用 Minecraft Builder 本地 recipe 与 Lodestone 0.6.0 生成和渲染，没有重新请求付费生图。概念图不是逐块施工蓝图；根系以深色原木/木板嵌墙表达，拱角使用原版楼梯/台阶。

## 验收证据

| 层级 | 结果与证据 |
| --- | --- |
| 静态 | PASS；`qa/static.json`：尺寸、完整体积、坐标唯一、双通口、连续地板、中央净空、完整屋顶、封闭侧墙、藤蔓和吊灯支撑、危险方块检查 |
| 软件图像 | PASS；`qa/interior-four-views.png`、`qa/exterior-four-views.png`；9 张单图及相机/PNG/NBT 指纹在 `qa/views/`、`qa/captures.json`、`qa/visual.json` |
| Forge 原生 | PASS；Minecraft 1.20.1 / Forge 47.4.10 / Java 17；`qa/native.json`、`qa/forge-gametest-java17.log`、`qa/forge-server.log` |

Forge 在本目录 `native-test/run/m13-isolated-gametest/` 的一次性测试世界中完成：4 朝向、每朝向 2 次实际模板放置、完整模板往返、原生注册表/属性校验、邻居形状更新、20 tick 静置后再运行 200 tick（randomTickSpeed=3）；共 **68640 次方块状态观察**。每朝向还校验外接短地板及 138 个可站立脚位；中央 3×3 直通空间、所有挂灯与墙藤支撑、无流体和掉落实体均通过。中央通路最低实测方块光照为 **5**。

本次运行未观察到藤蔓数量或面朝向变化。模板保留原版藤蔓随机生长；验收允许自然生长阶段在非通路新增藤蔓/改变藤蔓面，但不允许原有藤蔓丢失、承重方块或灯具变化，且中央净空必须保留。这不是无限时间的植物扩散冻结保证。

所有剖切仅是内存中的显示过滤；`room.nbt` 保持完整屋顶和侧墙。软件灯光不等于整合包实机画面；没有宣称完整客户端 FPS、多人联机、接入路径、占用拒绝或 SavedData 幂等验收。

## 重现

当前本机依赖：Minecraft Builder 已安装依赖、Java 17 `java-runtime-gamma-snapshot`、缓存的 Gradle 8.8 / Forge 47.4.10、Bundled Playwright 与 Edge、配置的 imagegen Python 环境（仅用 Pillow，不调用图片 API）。

1. `node tools/preview-server.mjs` 启动本地 NBT 专用预览 `http://127.0.0.1:51813/`。通过 `?view=interior-north`、`interior-south`、`interior-west`、`interior-east`、`exterior-north` 等参数切换视图。
2. 另开终端运行 `./Run-QA.ps1 -Rebuild -Capture`，重新生成、静态检查、隔离 Forge 验收、截图及汇总。脚本临时设置并恢复进程 Java 环境；不修改全局 Java。
3. 原始 recipe 在 `tools/recipe.mjs`；完整测试源码在 `native-test/src/main/java/`。`qa/forge-gametest.log` 保留了首次误用系统 Java 25 导致的 Gradle 兼容性失败，正式通过证据是带 `java17` 的日志。

接入方必须先做整个体积的占用/流体/实体/边界检查再放置；显式空气不是清空既有建筑的许可。北侧与南侧外部连接路不属于模板；不能将本目录测试模组装入正式实例。
