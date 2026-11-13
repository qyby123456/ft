# M14 · 陈旧菌洞吊笼直廊

独立制作交付，设计编号 **M14**，本批唯一模板 ID **corridor-M14**。按走道批次账本只读核对，本地预留排队号为 **54**，不是世界位置或部署状态。仅在本目录写入，不部署、不改正式存档、JAR、核心源码或其它房间；生产位置及接入由统一交接负责人确认。

## 规格与入口

- Minecraft Java 1.20.1，DataVersion 3465，gzip 单调色板结构；由 Minecraft Builder Skill / Lodestone 0.6.0 的 `recipe.mjs` 生成。
- 尺寸 XYZ **15×12×41**，纵向 41 方块＝2.5625 区块，属于 32–48 格中型档位。无转弯、无岔路、无楼梯。
- 北入口脚部 `[7,2,0]`，内部安全脚部 `[7,2,4]`，南出口脚部 `[7,2,40]`。两端都是 3 宽×5 高开放拱口；脚下完整地板。
- 中段主路 x=5..9，脚部 y=2，保证 5 格宽、4 格高净空；两端接口收为 3 格宽。主路与两端均干燥、平坦、可往返。
- 7,380 条唯一体积记录，包括显式空气；4,283 个非空气方块。完整顶层 y=11 共 615 格，下方另有 y=10 洞顶与嵌入梁。

NBT SHA-256：

`186bb72489d05fdf57c68ac68b84320a43af61d6b7842702b2b8585e2288d1ca`

## 设计来源与取舍

原图：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M14-fungal-cave-aged.png`

原图 SHA-256：`aeea1468c5e12912daa7b14bded792897778cd3331cca76999c302646e11ea31`

保留中央一点透视直廊、泥石与苔斑菌洞、裂纹铺地、6 组红色巨菇、6 组青色发光菌、8 只铁链吊笼、垂根、孢子花、墙角蛛网和橙色墙缝/地缝光。

- 为避免真实熔岩、火焰与生长物隐患，橙色玻璃后使用菌光体，地缝也由完整橙色玻璃覆盖。**没有液体、开放火焰、岩浆块或暗藏伤害。**
- 青菌由诡异菌柄/菌块与海晶灯实体方块组合，不声称自定义荧光小蘑菇或新粒子。小蘑菇为盆栽，另有稳定基底上的诡异菌；菌床采用灰化土而不是会传播的菌丝块。
- 铁笼是建筑装饰，内有落在完整方块底座上的灯笼，无笼中实体、战利品或生成机制。顶板经链条接入玄武岩嵌顶梁，梁端连入墙柱。
- 12 处蛛网均位于墙边高处 y=7，主路不穿蛛网。垂根与 6 处孢子花都有实际完整洞顶支撑。
- 原版软件材质与正常预览照明比概念图更明亮，不冒充概念图着色器效果。

## 交付文件

- `room.nbt`、`room.json`、`preview.png`、`qa.json` 为直接交接文件。
- `preview.png` 是最终 NBT 的北侧眼平 Lodestone 渲染，`preview/lodestone-south.png` 为反向眼平视图，`preview/lodestone-exterior.png` 为完整外壳。
- `preview/four-views/m14-final/corridor-M14/` 含北、南、西、东四向内部剖视及四向完整外壳，共 8 张单图与 2 张四宫格；旁边 `room.nbt` 是同哈希复核快照。
- `qa/static.json`、`qa/native.json`、`qa/visual-review.json`、`qa/lodestone-render.json` 含独立证据；`delivery-manifest.json` 给出可交接文件完整哈希。
- `native-test/` 是隔离 Forge 验证源码/缓存/测试世界，不是生产模组，不得部署其测试 JAR 或世界。

## 实际验收结果

1. 静态 13 项 PASS：设计指纹、NBT/版本/单调色板、尺寸、全体积唯一坐标、无实体/库存、原版材料与危险物禁用、完整双层地板/顶层、无侧洞、两端接口、5×4 净空、南北/内部连通、吊饰支撑与主题数量。另有 `qa/reference.json` 核对 manifest/index、文件字节数与完整 PNG 解码，1536×1024、原图指纹均通过。
2. **真实 Forge 47.4.10 / Minecraft 1.20.1 / Java 17.0.8**，独立 `GameTestServer` 与 `ServerLevel`，实际调用 `StructureTemplate.placeInWorld`，不是只解码或模拟放置。
3. 原件按 0°、90°、180°、270°各放一次，立即与 400 tick 后分别逐格精确核对 29,520 条记录；每方向 4,283 个非空气状态通过原生 `canSurvive`，316 个干燥脚位可往返两端与安全点，8 只吊笼/18 处吊植物支撑稳定。
4. 实际执行 29,520 次邻居形状检查和邻居通知，开放火 tick、`randomTickSpeed=3`；本调色板稳定方块未报告随机 tick 状态，因此**强制随机 tick 计数为 0**，不虚报菌菇随机生长压力测试。完整运行 400 tick 后所有状态不变、外围 2,854 格空气/方向不变，无掉落物、实体、流体或方块实体。
5. 成功日志：`evidence/forge-gametest-m14-attempt2-20261103.log`。含 `M14_NATIVE_GAME_TEST_PASS: rotations=4/4; records=29520; ticks=400`、`All 1 required tests passed`、`BUILD SUCCESSFUL`，退出码 0。
6. 第一轮为夹具缺少空模板而未进入房间测试，已补 `empty.nbt`/`pack.mcmeta` 后在全新测试世界复跑通过；失败日志与 crash report 保留，不把首轮失败隐藏成成功。
7. 已目视核对内部/外部四视图及南北眼平视图。剖切仅在渲染中移除顶盖和近侧墙，保留梁及植物支点，最终 NBT 不挖顶。

这是结构软件渲染与无界面原生 Forge 验证，**不是整合包客户端/多人/FPS/着色器验收**。Lodestone 的 builtin entity 模型提示及源码 sourcemap 提示不等于原生状态失败；本房间不依赖容器实体模型。

## 本机复现

先切换到本交付目录，再按顺序执行；使用本机已经安装的 Minecraft Builder Skill，不重新安装 skill 或全局依赖：

```powershell
node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs "$PWD/recipe.mjs" --out-dir "$PWD" --name room --data-version 3465
node tools/static-qa.mjs
& C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe tools/verify-reference.py
node tools/prepare-fixture.mjs
./Run-NativeQA.ps1
& C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe tools/render-four-views.py
node tools/render-lodestone.cjs
node tools/finalize.mjs
```

以上示例先将工作目录切到本交付目录。脚本恢复 JAVA_HOME/PATH，Forge 每次使用本目录内新测试世界。四视图脚本只读调用共享渲染工具；Lodestone 渲染使用已安装 Edge 的独立 headless 实例并在结束时关停本进程服务器，不操作用户浏览器。

交互查看可运行 skill 原有 `scripts/preview.mjs --nbt <本目录绝对路径>/room.nbt --no-open`，再打开输出的本地 URL。不要将剖视图作实际屋顶缺失，也不要把气氛灯冒充自定义游戏机制。

## 统一接入边界

尚未写入 `incoming`，未打入生产 JAR，未分配世界位置，未自动部署。接入方仍须对**整个 15×12×41 体积（包括显式空气）**执行占用、世界边界、液体和实体拒绝检查；本 NBT 不是在任意现有建筑内强制清空的授权。保持既有大厅连接、幂等标记、返程、难度与个人记录逻辑，由统一交接线程审定后接入。
