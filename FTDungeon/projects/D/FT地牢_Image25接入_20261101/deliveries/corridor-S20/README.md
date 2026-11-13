# S20 / 废弃矿道 · 浅水短廊

独立候选交付，**未部署、未改玩家存档、未改地牢目录或生产模组**。本次不调用付费生图接口。

## 结构

- `room.nbt`：Minecraft Java 1.20.1 原版结构，DataVersion 3465；11×9×16（X×Y×Z），短型一字贯通，无转弯、分岔或路线升降。
- `room.json`：坐标、双端接口、参考图指纹与结构指纹。北口 `[5,2,0]`，南口 `[5,2,15]`；接口范围 X=4..6、Y=2..4，均为3宽3高；地板表面Y=2。
- 两端保留开放木石门框，不用概念图中的闭门封死连接口。中央 X=4..6 为干燥通道，轨道 X=5 全长16格。
- 深色橡木支架、4道石拱，其中2处缺角；8支有支撑壁火把、苔石与金/铁/铜/红石/钻石矿脉。矿石是可开采原版方块，并非脚本奖励。
- 为避免水漫出接口和冲掉铁轨，将概念图的淹水地面调整为**左右双浅水槽+中央干燥轨道**：44格水源，水位Y=1、深1格，侧底密封，端头有实体挡水坎。无需外界地形封水。
- 完整地基和屋顶，屋顶Y=8；剖切仅在预览克隆结构中进行，不改变NBT。
- 1584条显式方块记录（含空气），628格空气；无方块实体、预放实体、容器、战利品表、刷怪或奖励逻辑。放置前必须检查整个11×9×16体积，空气记录会清除已有方块。

## 图与来源

批次：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/`，已核对README、SPEC、manifest与index的S20条目。

参考图：`out/S20-abandoned-mine-flooded.png`，1536×1024；SHA-256 `f29b7371cb7f60007dc799b199dd6ceca086fe3b97baaa757ec98ef13b033112`。`reference/`保留原图副本和批次证据摘要。

结构SHA-256：`341dbcfa3178fba8d8cf9501309d10fe89a4c1cd39cfcc2e51f9d3879dc4e4c1`。

- `preview.png`：交付总览。
- `preview/index.html`：离线图集，包含10张实际NBT渲染图；四向内视和四向外视另有合成图。
- `preview/interactive.html`：本地服务下可切换方向。北、南轴线内视保留原始NBT；东、西内部图隐藏相应外墙和高位顶盖，仅用于观察内部。
- 所有结构图为Minecraft Builder / Lodestone 0.6.0 / ThreeStructureRenderer的原版纹理WebGL软件预览，**不是概念图替代品，也不是实机截图**。

## 验收

- `qa/static.json`：结构往返、完整体积、批次指纹、上下封层、侧壁、火把支撑、轨道、44格水源及220个水槽侧底邻面、3宽3高直通路线通过。
- `qa/views.json`：10个相机、每张图的指纹、完整/剖切模式；`qa/composites.json`登记合成图。
- `qa/forge-native.json`：**实际隔离Forge 47.4.10 GameTestServer**，原生注册表/状态/结构NBT解码与往返、`StructureTemplate.placeInWorld`四方向放置通过；每方向1584条记录，合计6336条。
- 初始及400 tick后逐记录状态、原生`canSurvive`、0.6×1.8玩家碰撞、双端可达和返程、8火把、16轨道、44水源、220封水邻面、990格外圈空气均通过。显式派发邻居更新、水/重力tick；randomTickSpeed=3，doFireTick=true；无外泄、物品、掉落方块或其他实体。
- 4个负向探针均被拒绝：拆通道地板、堵通道、破挡水坎、拆火把支撑。
- `qa/forge-run.log`保留最终成功日志。首轮测试夹具使用相对Y落入隔离测试地形，失败日志保存在`qa/forge-run-first-fixture-overlap.log`；改为绝对高空夹具后通过，NBT未因此变更。
- 这不是完整整合包客户端、FPS或多人联机验收。只创建交付目录内隔离测试世界，未打开或修改任何玩家存档。

## 复现

本机使用已有Minecraft Builder依赖与Java17 Forge离线缓存。源码与测试均在本交付中，`forge/`仅为验收夹具，**不要把测试JAR安装到游戏**。

```powershell
$root = 'D:\ft工程文件\FT地牢_Image25接入_20261101\deliveries\corridor-S20'
$skill = 'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill'
Push-Location $skill
node scripts/run-build.mjs "$root\source\recipe.mjs" --out-dir $root --data-version 3465
Pop-Location
node "$root\source\static-qa.mjs"
& "$root\source\run-forge.ps1"
node "$root\source\preview-server.mjs"
```

预览服务启动后打开 `http://127.0.0.1:5220/preview/interactive.html`。另开终端运行 `node "$root\source\render.mjs"`重拍；使用已配置Pillow的Python运行`source/compose.py`生成总览和图集。Forge复测每次使用新的交付内`forge/run-时间戳/`目录，避免旧测试夹具占位。

若修改NBT，需重跑静态、Forge和视图检查，重新登记指纹；本交付不包含生产接入步骤。
