# S01 · 蔓生白骨吊笼短廊

2026-11-03（Asia/Shanghai）独立交付。只在本目录制作、渲染和运行隔离验证；没有部署到整合包、修改核心源码、其它房间或任何玩家存档，没有调用付费生图 API。

## 最小交付

- `room.nbt`：Minecraft Java 1.20.1 / DataVersion 3465 / gzip / 单调色板 / 2288 个显式坐标。
- `room.json`：尺寸、双向接口、图像和模板 SHA-256、旋转与接入约束。
- `preview.png`：从最终 NBT 软件渲染的室内剖切，不是 AI 概念图或游戏截图。
- `qa.json`：静态、原生 Forge 放置、400 tick 稳定性及四向视图的汇总。
- `handoff.json`、`S01-bone-crypt-overgrown-handoff.zip`、`package-receipt.json`：文件指纹、完整可复现源码和压缩包校验。

## 尺寸与接口

模板 X×Y×Z = **13×11×16**，沿 Z 轴直线贯通，长度 16 格，即短型 1 区块。整段主路 X=5..7、脚部 Y=2、Z=0..15，地板 Y=1，三格宽、至少三格高，平层无台阶、无转弯、无分支。

| 接口 | 玩家脚部相对坐标 | 向外方向 |
| --- | --- | --- |
| 北入口 | `[6,2,0]` | north / -Z |
| 内部安全点 | `[6,2,2]` | — |
| 南出口 | `[6,2,15]` | south / +Z |

原点是模板最小角，不是入口脚部；入口地板在 Y=1，所以接口脚部 Y=2。四种旋转均经原生 Minecraft 验证。旋转后接入方仍应调用 `StructureTemplate.transform` 转换接口坐标；不要直接把原相对点当世界坐标。

## 参考和转译

原图：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S01-bone-crypt-overgrown.png`，1536×1024。核对同批 `README.md`、`manifest.json`、`SPEC.md` 和 `out/corridor_index.json`。原图 SHA-256：`31b3e9c04d8f1e6c562edea2bbd7c9b99c02d1c7280fd89e922f5ec214cfded0`。

保留灰石砖/裂石砖/苔石、骨色拱肋、深橡木梁、四个铁链吊笼、四组双层骨龛、十颗原版骷髅头、八簇点燃蜡烛、四组双末地烛灯。地面骨块/錾制石砖形成中轴骨纹。侧木板是封闭墓龛面饰，使用原版活板门贴墙表现，不是可开启的支路。

原图远端的关闭木门改为**敞开骨拱**，优先满足一字贯通和安全接口。蔓生状态用苔石、苔藓地毯、合法依附的发光地衣及 `persistent=true` 的杜鹃树叶转译；不用会持续扩散、改变模板状态的藤蔓。吊笼骨饰采用有底支撑的骷髅头/骨块，不使用实体。无真实火、魂火、水、熔岩、蛛网、容器或奖励。

## 验证证据

- `summary.json`：Minecraft Builder / Lodestone 0.6.0 生成；回读尺寸和记录数一致，连接状态自动更新 32 格。
- `qa/static.json`、`qa/materials.json`：2288 格坐标唯一且在界内、1624 个非空气方块、42 个调色板状态、208 格完整屋顶；连续 3×3×16 干燥通路、双口开放、侧墙封闭、附着支撑、十个头骨方块实体，无模板实体和库存/战利品数据。
- `qa/native.json`、`qa/gametest.log`：Forge **47.4.10 / Minecraft 1.20.1** 隔离 GameTestServer；真实 `ServerLevel` 调用 `StructureTemplate.placeInWorld`，四种旋转共 **9152 格**初始逐格对比，显式邻居更新后 **400 tick** 再逐格对比全部一致。每个非空气方块通过 `canSurvive`；通路用真实碰撞形状检测；灯、蜡烛、头骨、链条、屋顶、边界无掉落或新实体。
- `qa/interior-four-views.png`、`qa/exterior-four-views.png`：西北/东北/东南/西南各四张；单图在同目录。剖切只影响显示，NBT 保持完整屋顶；另有 `qa/floorplan.png`。
- 软件渲染读取本地原版 1.20.1 方块模型/贴图。头骨使用简化的原版贴图实体网格，未模拟游戏内动态光照、粒子和环境遮蔽；**不是整合包实机游玩证据**。
- 保留两次真实失败：`qa/gametest-attempt1-java25.log`（默认 Java 25 不兼容 Gradle 8.8，改用进程级 Java 17）；`qa/gametest-attempt2-terrain-refused.log`（Y=100 测试体积碰到高地，拒绝覆盖，最终在验证为空的 Y=300 放置通过）。没有为通过测试移除占用拒绝。

## 接入边界

继承 `D:/ft工程文件/FT地牢_Image25接入_20261101/CONTRACT.md`。本目录不是已部署资源，也不分配大厅槽位或改核心注册表。唯一接入负责人应检查整个 13×11×16 体积及附近实体、世界边界、液体，占用即拒绝；显式空气不是清空许可。为房间保存持久生成标记，不覆盖玩家修改；保留大厅可步行连接、返程、个人记录和五档难度逻辑。

`qa/forge-run/world` 仅为本工程 GameTest 产生的独立测试世界，不是玩家世界；测试模组构建产物和测试世界均不进入交付 ZIP，也不得部署到整合包。

## 复现

使用现有 Minecraft Builder（无需修改技能本体）：

```powershell
node 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs' tools/recipe.mjs --out-dir . --name room --data-version 3465 --json
& 'C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe' tools/static_qa.py
& 'C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe' tools/render_s01.py
& ./tools/run_native.ps1
& 'C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe' tools/finalize.py
```

`run_native.ps1` 只临时设置进程级 `JAVA_HOME` 为本机 Java 17，结束时恢复；使用已缓存 ForgeGradle 6.0.54、Gradle 8.8 和 Forge 47.4.10。每次新建本交付目录内的独立 `qa/forge-run-时间戳` 测试目录，不接触原版游戏目录，不覆盖旧测试世界。`--offline` 复用已有缓存；`-Dnet.minecraftforge.gradle.check.certs=false` 仅跳过 ForgeGradle 前置公网证书探测，不修改系统证书或下载配置。

交互预览：

```powershell
node 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs' --nbt room.nbt --no-open --port 4317
```

浏览器打开脚本返回的本地地址。软件图像工具在本目录 `tools/nbtio.py`、`tools/mc_models.py`，由既有独立房间工程的只读工具复用后纳入交付；不依赖修改该房间。
