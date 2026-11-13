# corridor-S10 · 轻损锈铜机械短廊

独立制作交付，未部署，未修改正式 Mod、核心源码、配置或玩家存档。以 Minecraft Builder 的 Lodestone 0.6.0 生成，所有预览来自最终 `room.nbt`，不重新调用生图服务。

## 结构

- 唯一 ID：`corridor-S10`；候选资源路径 `ftdungeonmobs:image25/corridor_s10`，本地排队槽 70 仅为接收约定，不代表已集成。
- 尺寸 **13 × 11 × 16**（XYZ），北南向 16 格一字直通，无转弯、分岔或路线台阶。
- 玩家脚位：北入口 `(6,2,0)`，内部安全点 `(6,2,3)`，南出口 `(6,2,15)`；脚下完整方块 y=1。
- 全长保留 x=5..7、y=2..4 的 **3 格宽 × 3 格高**净空。地面排水格栅位于主路两侧。
- 完整顶盖 y=10、完整底座 y=0；2288 个显式坐标记录，其中 933 个空气、1355 个非空气。显式空气不授权在占用体积中清场；核心仍须先检查整卷占用。
- 铜橙、氧化青绿、黄铜金配色；四组石拱、六组铜管仪表、三组末地烛灯、裂砖与侧边碎石。

## 原图与兼容性

参考 `D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S10-copper-machinery-damaged.png`，完整 SHA-256 为 `c6a7c3600395f94a6af3160b9fed188cc08906428831ecfb684844e5e6db6907`。已完整解码 PNG，核对 README、SPEC、manifest、index、corridors_meta；证据在 `qa/reference.json`。

Java 1.20.1 没有铜格栅，以主路外侧的封闭上半铁活板门和氧化铜衬底替代；避雷针代表铜管，金块、标靶和墙面石按钮代表无实体仪表。原图尽头的封闭门改为开放南拱门，满足双端接通。所有铜均用涂蜡版本，破损只落在装饰托拱和侧边非重力石半砖，不打穿最终屋顶。不含容器、战利品、刷怪笼、执行方块、预置实体、液体或危险地面。

## 已完成的制作端 QA

| 层级 | 真实结果 | 证据 |
| --- | --- | --- |
| 静态 | PASS：gzip、DataVersion 3465、单调色板、范围/重复/显式体积、两端通路、地板与顶盖、原图指纹 | `qa/static.json` |
| 四视图 | PASS：北南西东内部剖视及完整外壳共 8 张逐张审阅 | `views/`、`qa/render.json`、`qa/four-view-review.json` |
| Forge 原生 | PASS：Forge 47.4.10 / MC 1.20.1 四旋转实际放置，240 tick；9152 次最终方块状态比对，无漂移、掉落或实体 | `qa/native.json`、`qa/forge-gametest.log` |
| 负例 | PASS：四方向分别识别堵路、缺地板、缺灯座，共 12 次 | `qa/native.json` |

原生测试只创建并访问本交付目录 `qa/forge-run/world` 的隔离测试世界；未打开游戏客户端或玩家世界。常规随机 tick、强制 ticking 区块和方块掉落均开启。初始模板放置保留已知形状，之后对全部记录触发邻居更新，并验证每格 `canSurvive`、实际碰撞通路及最终状态。日志中的并行负载警告不改变实际通过的 240 个游戏 tick。

内部图仅在渲染时移除 y=10 顶盖与观察侧边界，保留原始 NBT；灯座在剖图中可能与被隐藏的屋顶视觉分离，最终外壳及原生检查均保留屋顶支撑。所有图是软件预览，不是整合包客户端截图。

## 复现

1. 在 `C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill` 运行 `node scripts/run-build.mjs "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S10/tools/recipe.mjs" --out-dir "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S10" --name room --data-version 3465 --json`。
2. 运行 `node tools/audit.mjs` 更新本地 metadata/静态报告；NBT 更新后必须重做原生和图像验证，旧证据不可沿用。
3. `powershell -NoProfile -File tools/run-native.ps1` 运行隔离 Forge GameTest，Java 17、Gradle 8.8 与已缓存 Forge 依赖由脚本中的绝对路径提供。
4. `node tools/preview-server.mjs` 启动只读本机 Lodestone 预览；另一个终端运行 `node tools/render.mjs`，无窗口 Edge / SwiftShader 输出 9 张真实 NBT 图。
5. 用配置好的 Pillow 环境运行 `tools/contact_sheet.py`。重新逐图审阅后，运行 `tools/finalize.py` 封装证据。

## 接收边界

本交付的 PASS 只代表制作端验证完成，`ready_for_receiver_review=true`。接收者仍须冻结最终 NBT/JSON/QA/图片 bundle，在自己的隔离夹具中重做八图及至少 240 tick 原生复验，才可标为可统一接入。大厅连接、两端落点、整卷占用拒绝、幂等与旧世界不覆盖尚未测试；没有部署授权。

NBT SHA-256：`f0657f0745b8acc902a9bd65969663d3265bc9d31a4f46f4f3a827fcd421fb16`。
