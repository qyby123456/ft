# corridor-M11 / 蔓生白骨墓廊

## 交付与边界

- 独立中型直廊，Minecraft Java **1.20.1 / DataVersion 3465**，gzip 单一调色板结构 NBT；只使用原版方块。
- 尺寸 **15 × 12 × 40（XYZ）**，长轴 Z 方向 40 格，约 2.5 区块；主路五格宽、平地贯通，无转弯、无分岔、无楼梯。
- **四件套**：`room.nbt`、`room.json`、`preview.png`、`qa.json`。额外提供 `four-views.png`、`views/north.png`、`views/south.png`、`views/west.png`、`views/east.png`、`views/exterior.png` 和 `reference-comparison.png`。
- 制作与 QA 仅在本目录。**未部署 JAR、未修改 mods、未读取或写入玩家存档、未连接正式大厅**。不把独立模板验收冒充整合包实机游玩验收。
- `corridor-M11` 是本次走道的唯一 ID，不复用 2026-11-02 房间 M11 白骨祷室的 ID 或模板。

## 参考核验

参考：`D:\ft工程文件\image2.5\dungeon-corridors-20261103\out\M11-bone-crypt-overgrown.png`

参考图 SHA-256：`78c7104c420851a657b80c0e4194028b8d57f07a42721f6c7989925d0dd25e00`。

已核对同批 README、manifest、SPEC 与 `out/corridor_index.json`：M11 = medium / bone-crypt / overgrown / lava-glow / broken-arches。直接使用既有图片，本次没有调用付费生图接口。

实现保留：深色木棺龛、白骨肋拱、侵蚀苔砖、成熟洞穴藤蔓、骷髅与燃烛、两侧熔岩辉光。12 个棺龛沿两侧分布；z=14 与 z=28 的两处肋拱饰边改用裂砖与苔圆石，表现破损而不破坏最终屋顶。AI 图中精细骨雕与棺盖图案以原版骨块、砂岩和深色木活板门转译，不声称逐像素复刻。

## 对接坐标

所有坐标为模板原点相对整数 XYZ，入口、安全点和出口均为**玩家脚部位置**，不是地板位置。

| 属性 | 相对坐标 / 规格 |
| --- | --- |
| 北入口 | `[7, 2, 0]`，facing north |
| 内部安全点 | `[7, 2, 5]` |
| 南出口 | `[7, 2, 39]` |
| 主路 | `x=5..9, feet_y=2, z=0..39`，200 个安全脚位 |
| 保证净空 | 全长主路 `y=2..4` 三格净空，北南均有至少三宽三高接口 |
| 地板 | 主路 y=1 的完整实心方块 |
| 屋顶 | y=11 覆盖整个 15 × 40 平面，600 个完整方块 |

模板包含 **7200 个显式坐标**，其中 4581 个非空气、2619 个空气；不是稀疏 NBT 清空命令。对接核心必须在任何写入前验证**整个 15 × 12 × 40 体积**、液体、实体、世界边界与高度；有占用就拒绝。不得用模板中的 air 无条件清场。应由核心添加独立持久生成标记，防止重进或新增模板时覆盖玩家修改。

## 安全设计

- 36 格真实熔岩全部位于边带 y=1，具有实心底、侧界和橙色染色玻璃顶盖；每个源格上方有 3 × 3 不可燃实心封护层，阻断流动和起火逃逸通道。主路不接触熔岩。
- 24 簇点燃蜡烛、22 个有底座骷髅及对应 skull 方块实体；木质棺饰远离熔岩火焰出口。无明火、无水、无蛛网阻路。
- 40 格洞穴藤蔓有顶部支撑，20 个末梢均设 `age=25`，不会继续向下生长占路；只在高位边带。
- 无容器、无 LootTable、无 Items、无奖励、无刷怪笼、无模板实体，无命令/结构/拼图方块。

## QA 证据

### 1. 静态 NBT：PASS

`qa/static.json`：原图指纹、gzip、单一调色板、3465 数据版本、7200 唯一有界坐标、原版方块、禁用内容、完整屋顶、封闭侧墙、五宽直路、双端净空、入口—内部—出口双向连通、熔岩封护、蜡烛/骷髅支撑、成熟藤蔓与 block entity 负载。

### 2. 实际 NBT 四视图：PASS

Minecraft Builder / Lodestone `ThreeStructureRenderer` 读取最终 NBT，预览服务回传字节 SHA-256 与最终 `room.nbt` 一致。`qa/render.json` 记录相机、剖切规则、各图 SHA-256 和复核结果。

`preview.png` 为完整模板中轴视角；四视图只在渲染副本中移除屋面和近侧墙壳。最终 NBT 没有剖切。`views/exterior.png` 另示完整外壳。图像为软件光照，不是游戏截图；特殊方块实体、骷髅纹理和蜡烛粒子以游戏为准。曾出现的后处理大三角遮屏伪影通过关闭预览后处理消除，未修改结构。

### 3. Forge 实际放置 / 稳定性：PASS

`qa/forge-placement.json`、`qa/forge-gradle.log`、`qa/forge-run/logs/latest.log`：

- **Forge 47.4.10 / Minecraft 1.20.1** 独立 `GameTestServer` 中真实 `ServerLevel`，用 `StructureTemplate.placeInWorld` 放置。
- NONE / CLOCKWISE_90 / CLOCKWISE_180 / COUNTERCLOCKWISE_90 四种旋转，**4/4 通过**。
- 每个旋转 7200 个坐标，四份共 28800 条；放置前检查测试体积及一格外边界全空，不清场，不接触任何正式存档。
- 先检查即时状态，再对全部坐标显式触发邻居更新；**400 tick 后**重新逐格比较状态与支撑，并验证路线、完整屋顶、蜡烛燃烧、骷髅方块实体、藤蔓与封闭熔岩。
- `doFireTick=true`、`randomTickSpeed=3`；所有状态仍与模板相同，边界无漏液、无火、无掉落物或其它实体，200 个主路脚位全部安全。
- Gradle `BUILD SUCCESSFUL`，GameTest `All 1 required tests passed`。400 tick 结论是本次隔离测试时间窗，不等同于长期服务器运行或整合包实机游玩。

最终 NBT SHA-256：`4121cbca43feca21af7d0d066004a67d9187634ac5e8d0b6f0b862d0248aba39`。

`qa/rebuild.json` 另证实从同一 Minecraft Builder recipe 重建后的压缩 NBT 字节与该指纹一致。`manifest.json` 列出交付文件的完整指纹；`delivery-receipt.json` 记录交付包指纹。`corridor-M11-delivery.zip` 含四件套、全部视图、QA 报告与日志、生成器及测试源码；排除独立测试世界、Gradle 缓存和编译产物。

## 复现

在本交付目录使用 PowerShell：

```powershell
$root = (Get-Location).Path
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
$python = 'C:\Users\YOUR_USERNAME\.codex\.venvs\imagegen\Scripts\python.exe'
node "$skill\scripts\run-build.mjs" "$root\recipe.mjs" --out-dir "$root" --name room --data-version 3465 --json
& $python "$root\tools\qa_static.py"
node "$root\tools\capture_preview.cjs"
& $python "$root\tools\assemble_preview.py"
```

人工检查中轴图、四视图、完整外壳及参考对照后，再运行 `tools/assemble_preview.py --approve`。该开关记录实际视觉复核结果，不能跳过审图直接宣称通过。捕获脚本默认启动无窗口预览；如端口 5311 被其它 NBT 占用会拒绝，可设置进程变量 `M11_PREVIEW_PORT` 使用另一个端口。

原生测试只能在 `native` 子工程运行；每次自动创建全新隔离工作子目录，也可用 `-PqaRun=唯一名称` 指定，不要复用已放置过模板的测试体积：

```powershell
$env:JAVA_HOME = 'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
Push-Location "$root\native"
try {
  & 'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat' --offline --no-daemon --console=plain '-Dnet.minecraftforge.gradle.check.certs=false' runGameTestServer
} finally { Pop-Location }
& $python "$root\tools\qa_static.py" --finalize
```

Python 环境仅用于本地 NBT/Pillow 工具，不调用 imagegen。保留的 NBT 解析依赖以只读方式复用本机 `output/dungeon-expansion-20261101/tools/nbtio.py`；Lodestone 与 Forge 依赖来自本机现有 skill / Gradle 缓存。`native` 是测试夹具源码，**不能部署到整合包**。
