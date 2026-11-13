# S4-02 浅水遗迹书房与发光石台

## 构图修订1（待独立复验）

独立首检已认可旧版注册/回读、静态存活、接口与路线，但要求积水构图返修。旧候选SHA256 `9a101d563824cca090337068809235d56c1ec9ab22b4067d9956d5148138592b` 的全部文件、NBT、截图、检查源码和日志已封存在 `history/rejected-9a101d56/`，不属于当前候选。历史目录不参与当前版本SHA256SUMS。

修订只调整必要通路和绿色嵌面：两格或三格宽的实心堤道缩为单格宽上半砖石条，石条为waterlogged top slab且下方y=2保留水，池水在通路下连为同一体；两口向内4格仍保持5宽干燥实心支撑，家具平台及两箱前操作位不减。绿色中心由孤立一格变为5格十字形嵌面。西/北墙、书架壁龛、木桌、两空箱、三悬灯和开放屋顶保持不变。

`water-composition.json`登记内区21×19（399列）中的可见水面占比和流体单连通分量，需大于一半可见水面；水格与含水半砖六邻接只是静态连通证据，不代替流体tick。原生逐块检查器现在允许上半砖地板含水：水在脚面下方，顶面有实际支撑；同时更严格的0.6×1.8体积检查仍独立检查脚部/身体不碰撞、不涉水，不将任意含水地板一律视为安全。

最终14个目标含两箱操作位(4,4,10)、(25,4,8)，其它目标的单格石条中心改为(19,4,14)、(7,4,18)、(13,4,19)；真值由当前room.json读取。S4-03仍未授权、未开始，世界tick/整合/部署仍NOT_RUN。

仅本张待独立验收；S4-03及以后未授权、未制作。前置放行为独立复核目录 `S4-01-revision1-independent-final/ACCEPTANCE.json`，不把本张自检当成下一张放行。

## 读图与独立几何

参考：`D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-02-medium-room.png`。

本图不是大树主室：主地标是低矮矩形石台与绿色发光中心，西墙嵌入书架/箱子壁龛，北墙偏东有格栅门洞，东北有木桌、花盆和箱子，三个暖色悬灯，高墙有原木横梁与附壁藤蔓，大面积地面浸水。没有树、树冠、树根或四向桥。配方从零编写，不读取S4-01或旧失败结构；复用仅限检查器和截图脚本。

依据可见方块比例估为29×18×27，不套提示词名义15×15。北/西墙约13至16格高；南/东破损低墙约5至8格。实际为露天遗迹，预览与交付使用同一完整NBT，没有另一套隐形屋顶。

中央基础9×7、石台7×5；顶面y=5，池水顶部y=3。东西各两级真楼梯，可从干燥步道走上顶台。石台绿色光源为verdant_froglight，周围3个ochre_froglight由原木梁和锁链支撑。

## 路线与偏差

水深2格，有底、侧壁与睡莲。为可游玩性加入单格干燥石条路径：北门偏东入场，接东侧台阶；南门偏西入场，接西侧台阶和书架壁龛。两路经石台相通；东北木桌另有干燥台面前通道。含水上半砖石条下面水体相通；不再使用宽干地十字。

参考图的北格栅疑似封闭门，本作将格栅保留在门框两边，中央开放以提供5宽×4高接口。此为明确通路偏差，不宣称复制封闭门。两口而非四口，绝不沿用S4-01四桥连接模板。

箱子是空装饰箱：有真实方块实体但没有Items、LootTable或自动奖励。盆栽和家具保留；无命令方块、拼图、刷怪笼或实体。预览不模拟插画雾、沼泽生物群系水色或萤火虫。

## 交付、原点与连接

`incoming/S4-02/room.nbt`为交付，`nbt/s4-02-drowned-study.nbt`为同字节builder产物。Java1.20.1，DataVersion3465，gzip vanilla structure。

原点(0,0,0)为西北底角；+X东、+Y上、+Z南，完整包围盒29×18×27。无需拆件和拼装顺序。

| 接口 | 玩家脚部中心 | 桥面/地面块Y | 支持净空 |
| --- | --- | --- | --- |
| 北 | (19,4,0) | 3 | 宽5×高4×向内深4 |
| 南 | (13,4,26) | 3 | 宽5×高4×向内深4 |

连接北脚部世界坐标(A,B,C)时，放置原点为(A−19,B−4,C)。接口法线要相反，门槛标高要对齐。不同于S4-01的脚部Y5，整合方不可套同一高度偏移。

稀疏NBT不等于清空地形；整合时必须先拒绝完整29×18×27体积中的占用、实体、世界边界问题。未执行任何世界放置命令，未改正式存档、mods、配置或部署。

## 检查证据

- `qa/native-review.json`：原版方块注册表/状态、两箱子方块实体加载、模板load/save回读，及逐块干燥楼梯路径。
- `qa/body-routes.json`：原版碰撞形状0.6×1.8体积、半格网格、最大0.5台阶、14个目标（含两箱前），水平每步5点采样，无跳跃。不是游戏物理tick实测。
- `qa/vine-stability.json`：绑定原版方块标签，所有藤蔓/睡莲/花盆canSurvive，藤蔓六方向updateShape迭代到固定点，无脱落；不宣称随机刻下永不生长。
- `geometry-check.json`：水池底侧边界、睡莲、桌腿、悬灯链支撑、两空箱子。
- `preview-default.png`、`preview-reverse.png`、`preview-night.png`：完整实际NBT的Lodestone内外方向/夜间截图；`preview-check.json`回读HTTP提供的NBT并与交付逐字节绑定。
- `repetition-check.json`：可用本地自然主题结构快照，加入S4-01，材质无关实体占用/四向旋转粗筛；预览裁切也参与，分数偏高并非自动近似结论。需独立视觉复核，不声称普遍唯一性。
- `acceptance.json`：仅SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE，NOT_RUN保持真实。

## 可复跑命令

```powershell
$root = 'D:/ft工程文件/FT地牢_第4组逐图返工_20261104/S4-02'
$python = 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe'
node 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs' "$root/recipe.mjs" --out-dir "$root/nbt" --data-version 3465 --json
& $python "$root/review.py"
& $python "$root/compare.py"
node 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs' --nbt "$root/incoming/S4-02/room.nbt" --port 5416 --no-open
```

预览服务器运行后，另一个终端运行：

```powershell
node 'D:/ft工程文件/FT地牢_第4组逐图返工_20261104/S4-02/capture.cjs'
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' 'D:/ft工程文件/FT地牢_第4组逐图返工_20261104/S4-02/revision_diff.py'
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' 'D:/ft工程文件/FT地牢_第4组逐图返工_20261104/S4-02/finalize.py'
```

14个路线目标由`room.json`读取并逐个原版检查：北(19,4,0)、南(13,4,26)、北内(19,4,4)、南内(13,4,22)、西路(7,4,9)、东路(19,4,14)、台面西(12,6,14)、台面东(16,6,14)、书架前(4,4,8)、木桌前(22,4,8)、西南折角(7,4,18)、南路折角(13,4,19)、书架箱操作位(4,4,10)、木桌箱操作位(25,4,8)。

原版映射JAR仅在本目录qa生成移除陈旧签名/清单的隔离副本，类字节不变；不修改原依赖。来源与SHA256见fixture-provenance.json。标签快照来自前置独立复核的原版标签文件并在本目录保留副本。
