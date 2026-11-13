# FT 地牢商店变体 C · 末地虚空商店

**独立候选：静态 QA、四向 Forge 原生放置与 100 tick 稳定性通过；未注册、未分配槽位、未部署。**

只制作变体 C。没有修改其他房间、共享 incoming、FT 核心、正式玩家世界或已部署 JAR；没有启动 Minecraft 客户端，没有调用生图接口。原生检查仅在本工程 `qa/forge-run` 的独立无界面 Forge GameTestServer 中进行。

## 交付

- `out/room.nbt`：Java 1.20.1 gzip 单调色板模板，DataVersion 3465。
- `out/room.json`：入口、尺寸、参考指纹、候选接入状态与安全转译说明。
- `out/preview.png`：读取实际最终 NBT，以原版客户端模型/材质软件渲染；不是设计图或游戏截图。
- `out/qa.json`：最终汇总与可核验的 QA 证据路径/指纹。
- `out/blueprint.json`：逐件陈设、支撑、步行目标、净空坐标；`out/manifest.json`：交付文件指纹。
- `delivery-C-end-void.zip`：上述候选及证据/四视图，不包含运行 JAR、Minecraft 资源或测试世界。

工程内 `tools/`、`src/` 与 Gradle 配置完整保留，可重建重测。NBT 小工具只读复制自既有独立补给商店工程；未修改其源文件。

## 指纹与接口

| 项目 | 值 |
| --- | --- |
| 暂定 ID | `SHOP_C_END_VOID`（必须由统一接入线程确认，不私占正式槽） |
| 尺寸 XYZ | **13 × 9 × 13** |
| 北入口脚位 | **(6,1,0)**，X=5…7、Y=1…3 三格宽/高，完整脚下地板 |
| 内部安全脚位 | **(6,1,3)** |
| 最终结构 SHA-256 | `8bbd995643205032b1185f5edc2f33c3520e3efb6e5209d78d5abf85b94a65be` |
| C 设计图 SHA-256 | `e43021de1da6f121f343912490520ac611b8a46d6d586f9bc9404f4832111a3b` |
| 方块记录 | 1521，完整显式体积；762 非空气方块 |

设计源：`D:/ft工程文件/image2.5/dungeon-shop-variants-20261102/out/shop-variant-C-end-void.png`，本工程 `reference/` 有同指纹副本。已阅读源 README、manifest、DISPATCH 与 FT 接入 CONTRACT。源 README 引用 `STYLES.md`，但实际目录及 image2.5 子树未找到此文件；没有虚构读取或生成缺失规范，以 C 原图及现有 FT 契约落实。

## 布局与安全转译

- 末地石砖、紫珀、黑曜石、末地烛、末影箱、紫水晶与紫色地面纹样保留 C 主题。
- 将“漂浮台”转为连续全方块底板，完整外墙和遮光玻璃星窗封护，没有虚空洞、坑、液体或可跌落边缘。完整屋顶保留，剖切只发生在渲染副本。
- 独立柜台 X=3…9、Y=1、Z=7，四侧最少一格宽、两格高；北门至柜前保持三宽三高主道。柱嵌墙线，不占中央、不穿家具。
- 后墙展架的上下两层上半砖直接连实墙，左右立柱落地；陈列物直接落在合法完整顶面。10 根末地烛全部逐一检查墙/梁/台/架的 sturdy 支撑面，不依赖末地烛本身宽松的 `canSurvive`。
- 无预置实体、掉落物、刷怪、命令/结构/jigsaw 方块、战利品表或库存；两个桶、酿造台均为空。末影箱不是预装奖励，但玩家打开时会连接**已有个人末影箱库存**，不能声称玩家个人库存为空。
- 本候选是建筑/陈列空间；不虚构店主、交易、波次、奖励或传送门功能。

## 证据

1. `qa/static.json`：20 项静态检查，包括完整格式/显式体积、空库存、无危险物、完整底板屋顶、全封护墙、北侧三格接口、主道/柜台环路、98 个可达脚位、25 处逐件实支撑、10 根末地烛覆盖、墙连货架、家具不交叠、结构连通及缺支撑负例。
2. `qa/native.json`、`qa/forge-gametest.log`：Minecraft 1.20.1 / Forge 47.4.10 / Java 17，原生注册表/属性/方块实体/模板 roundtrip、`StructureTemplate.placeInWorld` 四向放置、邻居更新、100 tick 后精确状态/碰撞/生存/库存检查；每向 1521 记录、6 方块实体、98 可达脚位、25 实支撑、0 非预期实体。四个移除支撑和四个阻塞主道的负例全部拒绝。
3. 日志同时包含 `SHOP_C_NATIVE_GAME_TEST_PASS: rotations=4/4, records=6084, ticks=100`、`All 1 required tests passed` 与 `BUILD SUCCESSFUL`。
4. 四份 `qa/native-placed-*.nbt` 是 100 tick 后真实原生回采并重新加载；`qa/capture-comparison.json` 用独立坐标/状态旋转算法逐格比较四向共 6084 状态，同时检查方块实体类型、空库存与告示牌内容。
5. `qa/reproducibility.json`：重复构建 NBT/元数据/蓝图的 SHA-256 完全相同；`qa/render-manifest.json` 绑定预览源指纹；`qa/visual-review.json` 明确记录软件四视图、平面、完整屋顶外壳和原生回采预览的复核范围。
6. `qa/finalize-comparison-note.json`：最初回采验收器把告示牌 JSON 的无意义空白压缩误判为文字改变；改为解析后语义比较，同时保持文字、颜色、发光、蜡封和全部方块状态的严格检查。结构与原生测试未改动。

`preview/` 保留北/南/西/东、四视图、平面、完整屋顶外壳、原生世界回采与交付总览。软件渲染不模拟光照、末影粒子、光影包，末影箱/告示牌外观近似，不显示告示牌文字及附魔台书；没有客户端实机视觉、性能、多人或功能性交易验收。

## 重建

在本工程目录执行：

```powershell
$python = 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe'
& $python tools/build_room.py
& $python tools/qa_static.py
& $python tools/run_native.py
& $python tools/render_room.py
& $python tools/finalize_delivery.py
```

`run_native.py` 默认离线使用缓存 Gradle 8.8 与 Minecraft Java 17，仅测试本工程。重跑保留上一轮原生日志和报告；绝不将测试服清空/施工代码用于正式世界。工具依赖本机 Pillow、既有 ForgeGradle 缓存及 vanilla 1.20.1 `client.jar`（只读，不分发），不是脱离缓存可运行的完整服务端包。

## 接入与回滚边界

没有把候选发送进统一队列或自动安装。统一接入线程接收前需确认 ID/槽位、完整体积及缓冲区的占用/实体/液体拒绝、FT 大厅真实可走连接、持久生成标记与幂等、返程、难度和核心回归。显式空气不授权无条件清空任何玩家建筑；不得将本测试夹具直接用于正式施工。

本次回滚只需撤回独立候选，无需修改玩家世界。未来若部署，必须通过唯一接入负责人协调，停服并备份旧 JAR、配置、世界及地牢 SavedData；卸载模板不会拆除已生成建筑，不得盲删生成标记。
