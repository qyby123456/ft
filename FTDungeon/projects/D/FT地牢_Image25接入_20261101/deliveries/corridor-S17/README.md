# corridor-S17 / 灾变暗铁·绯纹悬笼短廊

独立候选结构，依据已存在的 S17 设计图制作；不重新生图、不部署、不修改玩家存档、FT 核心、正式 JAR 或其他走道交付。所有本任务产物位于本目录。

## 四件套

- `room.nbt`：Minecraft Java 1.20.1，DataVersion 3465，gzip、单调色板，15×13×16，3120 个显式坐标（包括空气）。
- `room.json`：接口、设计指纹、尺寸、保护路、已知改编与放置约束。
- `preview.png`：实际最终 NBT 软件剖视，不是概念图或游戏截图。
- `qa.json`：静态、四视图、Forge 原生四向放置与稳定性证据汇总。

最终 NBT SHA-256：`5cfb4333360e3426c16c23b6a934218383f430c71d5455205697383651cf1913`。

## 几何与接口

尺寸顺序为 XYZ：宽 15、高 13、长 16，严格短型、水平直通，无转弯、支路、台阶。

| 接口 | 相对脚部坐标 | 朝向 |
| --- | --- | --- |
| 北入口 | `(7,1,0)` | north |
| 安全点 | `(7,1,3)` | north |
| 南出口 | `(7,1,15)` | south |

北、南门洞净宽 7、净高 6；中央 `x=6..8,y=1..3,z=0..15` 全程净空，脚下 `y=0` 为完整干燥实体地板。静态可达脚位 164。四笼底部位于 y=6，与玩家通行分离；六个壁火把贴附实墙，笼内为有链支撑的暖色灯笼。屋面 y=12 全覆盖、两侧墙双层，剖面仅在渲染阶段隐藏屋面和近侧墙，NBT 不剖切。

主题保留黑曜地毯、暗石柱肋、金边、绯红壁纹与地面菱纹、悬空铁笼和橙色光源。仅用 1.20.1 原版方块；零实体、零方块实体、零容器/库存/战利品，无液体、刷怪笼、命令方块或隐形光源。图中尽头封闭符文门改为开放南拱，避免做成死胡同；暗铁由深板岩、黑石、黑曜石表达，笼顶使用黑石实盖以保证链条连续。

## 输入指纹

- 原图：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S17-cataclysm-dark-iron-intact.png`。
- 原图 SHA-256：`18a1c608777d9f99e6e4faaaa95f556bbf3017db07944b47868dd329ebcd8ddb`。
- 本地只读规范快照：`reference/SPEC.md`、`reference/README.md`、`reference/manifest.json`；核验结果 `reference/fingerprint.json`。
- 使用已安装 Minecraft Builder Skill 的 `run-build.mjs` 与 Lodestone 生成结构，配方 `recipe.mjs`。无图像 API 请求、无费用调用。

## QA 证据

- 静态：`qa/static.json`，33 项通过，完整体积、两端开口、三格直通、地板屋面、支撑、禁用方块、零实体与库存；材质数量见 `materials.json`。
- 重建：`qa/reproducibility.json`，Minecraft Builder 从配方重新生成后与最终 NBT 逐字节一致，SHA-256 相同。
- 四视图：`qa/four-views/interior-four-views.png`、`qa/four-views/exterior-four-views.png`，各含北/南/西/东；目录还含八张单图与 `floorplan.png`。来自最终 NBT 的 1.20.1 原版方块模型/材质软件渲染；目视检查 `qa/visual-review.json`，源绑定 `qa/render-manifest.json`。
- 原生：`qa/forge.json`，Forge 47.4.10 / Minecraft 1.20.1 无界面独立 GameTestServer 实际加载、属性解析、模板往返、四向放置、邻居更新、200 tick 稳定性。末态累计检查 12480 方块；每方向 48 主路脚位与 2352 外圈液体/火探针。四次堵路和四次移除内链负对照均检出。
- 原生四向捕获：`qa/placed-none.nbt`、`qa/placed-clockwise_90.nbt`、`qa/placed-clockwise_180.nbt`、`qa/placed-counterclockwise_90.nbt`，均在 200 tick 后从测试世界重新采集并由 Minecraft 重载；哈希见 Forge 报告。
- 命令、全新测试世界路径和终态：`qa/forge-command.json`、`qa/forge-exit.json`、`qa/forge-run-<timestamp>/gradle.log`。早期内存不足与墙连接差异的失败日志保留，不能当作成功证据；初版四件产物保留在 `qa/revisions/initial/`，不用于最终接入。
- 这不是整合包客户端截图、多人/FPS/实际游玩验收，也未证明 FT 接入路线、占用拒绝或持久幂等。

## 复现

在本目录使用 PowerShell；依赖已安装 Minecraft Builder Skill、专用 Python（Pillow）和本机缓存 ForgeGradle/Java 17。不会打开玩家存档。

```powershell
$skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
$python = 'C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe'
node "$skill/scripts/run-build.mjs" ./recipe.mjs --out-dir . --name room --data-version 3465 --json
node ./tools/prepare.mjs
& $python ./tools/qa_static.py
& $python ./tools/render_views.py
& $python ./tools/run_forge.py
```

配方内容不变时重建 NBT 指纹应不变。配方修订后必须重做静态、所有视图、目视审查和 Forge，更新 `qa/visual-review.json` 的源/图像指纹，然后执行 `tools/finalize.py`；不能沿用旧 PASS。

交互预览（读取完整最终 NBT，不改模板）：

```powershell
node "$skill/scripts/preview.mjs" --nbt ./room.nbt --no-open --port 5317
```

## 接收边界

仅可进入接收者独立复验，不能视为已经统一接入。`corridor-S17` 与房间旧 ID `S17` 隔离命名；最终全局槽位由统一交接负责人决定。接收方必须逐个核对 `delivery-manifest.json` 和最终 NBT 哈希，复核四视图，验证旋转后的接口与场地兼容性。

NBT 中显式空气不是自动清空许可。正式核心需预检整个 15×13×16 体积（包括空气记录）、世界边界、液体及实体；遇占用拒绝，不无条件清场；每次生成需要独立持久标记，重复进入不覆盖玩家改动。这里的夹具只在 `qa/forge-run-*` 新建临时测试世界中清理测试区域，禁止用于任何正式世界。不安装 QA mod，不替换任何正式 JAR。
