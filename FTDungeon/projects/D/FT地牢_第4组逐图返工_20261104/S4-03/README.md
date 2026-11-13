# S4-03 盘根水窟藏宝小室

## 2026-11-05 盘根构图修订1

旧哈希`e10200d3e4e15cb2ced1f8b51f5581cb5215c73da3fac060e4ee18c99f57bf0f`已按独立要求封存为`history/rejected-e10200d3/`（NBT、37项原清单、配方、QA及截图），不计入当前候选清单。独立原生/路线已通过不等于构图通过；本修订仍待独立复验，不放行04。

实际修订是几何而非藤蔓换色：东门北侧主干加粗后向水线分成内斜、贴墙、长短错落的多支根脚，连续连接至(16,4,5)、(19,4,6)、(16,4,9)、(19,4,10)、(16,4,11)，中段高低由y8降到y4；西角与前角另有非对称扇根连接矮墙/水边。木根延伸不进入x15边路或东门z13至15净空，不改箱岛、盆栽、藤蔓、灯或护栏。水线根底仅在原有流体位置含水，干燥墙基不用含水根凭空造水。

`revision-diff.json`记录逐坐标差异、受保护家具与箱岛保持不变、东根与主干六面连通的支端点。`roots-closeup.png`为完整实际NBT放大近景，`roots-east-view.png`为另一侧完整结构，均非生图或替代候选裁切NBT。当前manifest不含history旧版文件。

本张独立制作，自检后提交独立验收。前置放行真值为 `D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S4-02-ACCEPTANCE.json`，不修改S4-01/S4-02文件或NOT_RUN状态；S4-04及以后未授权、未开始。

## 参考与比例

参考PNG：`D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S4-swamp-wetland/out/S4-03-small-room.png`。

按画面可见块面独立估算21×17×21，而非提示词名义9×9。与29×18×27书房相比缩小占地；本图没有书架墙、长桌、发光嵌面、四向桥或古树。地标是中央单箱小岛，北偏西门对应画面左侧高墙门，东偏南窄门对应右侧门；两个壁龛、盘根、三盏暖灯和前侧低护墙/栅栏构成识别点。

岛基5×5，顶面y=4；箱位(11,5,11)，朝南，空装饰储物箱。有真实箱子方块实体，没有Items/LootTable、奖励、刷怪或实体。箱上方没有屋顶/灯链阻挡。岛东侧只有一级台阶，玩家由单格石条上岛，到南侧箱前(11,5,12)操作，无需跳跃或涉水。

北/西墙约11至15格顶高，随段落升降，木梁、苔石、扎根泥土和长藤构成层次；南/东墙约5至8格低墙、缺口与东门框。实际交付就是露天残墙，不是仅供预览的削壳副本。

## 路线与水边界

中心以水为主：15×15池区，普通水深2格；单格含水上半砖绕池边行走，水可在石条下连通。只从东侧接中央岛，没有干地十字或多桥大台。西墙壁龛前局部2至3格宽操作位不是贯穿池子的堤道。

根系采用原木与含水红树根，只放靠墙侧，不堵门或箱前；它们没有叶片衰减，也不是会自动长大的树苗。池底、侧边和门槛封闭，不依赖外部世界挡水。生物群系水色/插画雾/沼泽背景树林不在本NBT范围，原版预览色彩不同不伪称完全一致。

单格石条、空宝箱和入口连通是明确可玩性偏差；不要求玩家穿深水取箱。随机刻植物蔓延、真实液体刻与游戏物理未运行，不把静态邻居形状检查称为世界实测。

## 原点与接口

交付 `incoming/S4-03/room.nbt` 与builder产物 `nbt/s4-03-rootbound-cache.nbt` 逐字节一致。Java1.20.1，DataVersion3465，gzip vanilla structure。单件，无拼装顺序。

原点为西北底角(0,0,0)；+X东、+Y上、+Z南。完整占用边界21×17×21。

| 接口 | 脚部中心 | 地面块Y | 保证支持/净空 |
| --- | --- | --- | --- |
| 北入口 | (6,4,0) | 3 | 宽3×高4×向内深3 |
| 东侧出口 | (20,4,14) | 3 | 宽3×高4×向内深3 |

本图两门是3宽，而不是沿用书房5宽口。连接北入口到世界脚部(A,B,C)时，放置原点(A−6,B−4,C)。连接法线必须相反、脚部高度对齐。运行时需完整体积占用拒绝；稀疏NBT不清除未知地形。无正式存档/模组/配置修改、无放置命令执行、无部署。

## 自检证据与动态边界

- `qa/native-review.json`：真实1.20.1注册表/属性、1个空箱子方块实体、StructureTemplate加载保存回读，两口3×4×3支持净空，逐块楼梯兼容路线。
- `qa/body-routes.json`：0.6×1.8体积、半格网格、最大0.5步高，原版碰撞形状；水平每半格5点采样，所有12目标可达，无跳跃。
- `qa/vine-stability.json`：绑定170个原版方块标签，藤蔓/睡莲/盆栽静态存活；含水根/上半砖、栏杆、锁链、藤蔓、睡莲及盆栽六向updateShape迭代固定点。调度的流体tick仅计数并抑制，不执行，不声称动态流体已通过。
- `geometry-check.json`：睡莲/灯/灯链支撑、水底/侧直接空气泄漏0；所有水及含水格六邻接为单组件，可见水面占比与尺寸可回读。
- `materials.json`：方块数量，禁止方块、实体与非空战利品检查。
- `preview-default.png`、`preview-reverse.png`、`preview-night.png`：实际完整NBT的Lodestone内外方向/夜景；`preview-check.json`回读HTTP提供的NBT并逐字节绑定最终文件。
- `repetition-check.json`：包括S4-01/S4-02的本地可用结构快照，材质无关几何/归一化占用旋转粗筛；非普遍独特性证明，仍待独立视觉验收。
- `acceptance.json`及`SHA256SUMS.txt`：自检待独立验收、当前文件回读指纹，绝不自动放行第4张。

12目标：北(6,4,0)、东(20,4,14)、北内(6,4,3)、东内(17,4,14)、岛前石条(15,4,11)、台面东(12,5,11)、箱前(11,5,12)、西边路(5,4,9)、西壁龛操作位(3,4,9)、东南角(15,4,17)、西南角(5,4,17)、北壁龛前(13,4,3)。实际以room.json为检查输入。

## 重现

```powershell
$root = 'D:/ft工程文件/FT地牢_第4组逐图返工_20261104/S4-03'
$python = 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe'
node 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs' "$root/recipe.mjs" --out-dir "$root/nbt" --data-version 3465 --json
& $python "$root/review.py"
& $python "$root/compare.py"
node 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs' --nbt "$root/incoming/S4-03/room.nbt" --port 5417 --no-open
```

保持预览服务运行，在另一终端：

```powershell
node 'D:/ft工程文件/FT地牢_第4组逐图返工_20261104/S4-03/capture.cjs'
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' 'D:/ft工程文件/FT地牢_第4组逐图返工_20261104/S4-03/revision_diff.py'
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' 'D:/ft工程文件/FT地牢_第4组逐图返工_20261104/S4-03/finalize.py'
```

QA复用已有验证器，不复用旧结构几何。映射原版JAR仅在本目录生成移除陈旧签名的隔离副本，类字节不变、原依赖未动；来源与哈希见fixture-provenance.json。
