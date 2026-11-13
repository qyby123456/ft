# S18 浸水熔铸短廊

独立结构交付，短型一字贯通走道。只使用 Minecraft Java 1.20.1 原版方块，采用 Minecraft Builder / Lodestone 生成。交付体积为 **15 × 11 × 16**；南北贯通，中央 **三格宽干燥主路**，没有转弯、分岔或必经楼梯。

## 入口与空间

- 本地交付 ID：`CORRIDOR_S18`；设计图 ID：`S18`。全局槽位由统一接入方分配，本交付不分配世界坐标。
- 北入口脚位 `[7,3,0]`；内部安全脚位 `[7,3,2]`；南出口脚位 `[7,3,15]`。
- 路面 Y=2，玩家脚部 Y=3；X=6～8、Z=0～15 全程干燥且至少三格净空。两端至少三宽三高，开放拱口无关闭门。
- Y=0～1 为完整地基；两侧三格厚围墙容纳封闭炉体；Y=9～10 为完整双层屋顶。预览中的剖切不影响 NBT。
- 四道黑石拱框、六盏有连续锁链顶锚的灵魂吊灯、六座落地铁砧、裂砖与苔石砖嵌墙。

## 液体安全与原图调整

原图的黑石锻炉、冷蓝魂灯、湿润苔墙、重复拱框和近端铁制门框均保留。为保证通行，把原图的满地积水改成两侧各两格宽、低一格的浅水槽；水槽有完整底板和石材侧界。中央三格石路不涉水、不跳跃。

四组锻炉合计 **12 个熔岩源**，藏于厚墙中，六面均由完整石块或完整玻璃封闭。**48 个水源**位于 Y=2 的下沉水槽；熔岩位于 Y=4，水与熔岩没有直接接触。无可燃植物和木构件，无明火；藻苔以完整苔石砖表现。

概念图尽头的关闭铁门改成开放门拱，两侧保留内退铁栅；端部上墙和角部封齐，确保短廊两端贯通且没有上方漏空。

## 文件

- `room.nbt`：gzip 压缩、DataVersion 3465、单调色板，2640 个全体积显式记录，含空气。放置前必须由接入方检查整个体积占用；显式空气不构成无条件清空授权。
- `room.json`：入口、内部点、出口、尺寸、参考图、结构 SHA-256、安全调整和交付边界。
- `preview.png`：最终 NBT 的软件剖视总览。近侧墙、炉壳和屋顶仅在该视图中被隐藏，以便检查封闭熔岩；实际成品保持封闭。
- `preview/interior-four-views.png`、`preview/exterior-four-views.png`：最终 NBT 的北、南、西、东剖视与完整外壳视图；单图同目录。
- `preview/lodestone-interior-north.png`：最终完整 NBT 轴线平视，保持墙顶与炉壳；`preview/lodestone-exterior.png` 为 Lodestone 全壳预览。
- `preview/floorplan.png`：水、熔岩、玻璃和干道分层叠加核查图。
- `qa.json`、`qa/static-qa.json`、`qa/native-*.json`、`qa/visual-review.json`：相同最终指纹的分层验收证据。
- `qa/forge-gametest.log`：Forge 实际四向放置日志。`qa/rejected-r1` 保留初版失败证据；`qa/history` 保存视觉修订前的已通过快照。
- `recipe.mjs`、`materials.csv`、`tools`、`src`、Gradle 配置：生成配方、材料及隔离测试源码。测试工程不用于安装到玩家实例。

## 验收范围

静态检查包括原图指纹、gzip/单调色板/边界、明确空气语义、三宽干道、完整屋顶、两端接口、液体隔离与支撑。四视图再对照原图、元数据和实际 NBT 检查柱梁、端墙、吊灯锚点、炉壳和通路。

Forge 1.20.1 / 47.4.10 隔离 GameTest 对北、东、南、西四种旋转实际调用 `StructureTemplate.placeInWorld`。显式触发全部水与熔岩流体 tick 和邻接更新；在 0、40、120、400 tick 逐一核对全部 2640 条旋转后的实际方块状态、原生支撑生存条件、完整封液面、三宽干燥双向碰撞通路和外侧两格液体空域。保持 doFireTick=true、randomTickSpeed=3。无容器、方块实体、奖励、刷怪实体或掉落物。

所有原生测试只操作本交付 `qa/forge-run` 下的临时 GameTest 世界。未部署、未修改正式 JAR、源码或玩家存档。软件预览与隔离 Forge 物理测试不等同于完整整合包客户端游玩、多人体验或正式地牢接入验收。

## 重现

在已安装的 Minecraft Builder 目录运行 `scripts/run-build.mjs`，将本交付 `recipe.mjs` 作为配方并指定当前交付目录、`--name room --data-version 3465`。随后用带 Pillow 和 NumPy 的 Python 执行 `tools/prepare.py`、`tools/render.py`；执行 `tools/run_native.ps1` 复现隔离 Forge 验证。当前脚本引用本机已有技能、原版材质和只读共享渲染工具，路径见源文件。

本地参考图：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S18-forge-lava-flooded.png`。

参考图 SHA-256：`90f93164051207db586026e4c3b910809ba98e07acfc3529de12e5436f921e9f`。

最终 NBT 指纹与终态以 `room.json`、`qa.json` 为准；更改任何配方后须重新生成全部证据。
