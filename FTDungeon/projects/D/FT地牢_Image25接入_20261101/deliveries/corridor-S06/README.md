# corridor-S06 · 浅淹苔石短拱廊

本目录是独立走道交付，不是旧房间 S6。只在此 ID 目录内制作；不部署 JAR、不改正式源码或玩家存档。

## 结构

- 尺寸 X×Y×Z：**13×10×16**；南北直通、长度正好 16 格，无转弯、分岔或行走楼梯。
- 入口脚位 `[6,2,0]`，安全脚位 `[6,2,2]`，出口脚位 `[6,2,15]`，正面朝北。
- 干燥主路 `x=5..7,y=2..4,z=0..15`，全程 3 格宽、3 格净高；完整实体地板在 `y=1`，没有台阶。
- 五道完整石砖拱肋，苔石墙与有墙面支撑的藤蔓；8 盏灵魂灯通过双段竖直锁链连接实体屋顶。
- 两侧浅水位于 `y=1,x∈{3,4,8,9},z=2..13`，合计 42 个水源；柱脚占去另外 6 格。完整底板、外墙、中央实体路基、两端各两格止水坎围合。
- `y=8,9` 保留完整双层屋顶。最终 NBT 含全部 2080 个体积坐标，包括显式空气；这不是对现有世界的无条件清空授权。
- 仅使用 Java 1.20.1 原版方块；无容器、库存、奖励、实体、刷怪笼、命令、结构或拼图方块。

## 对照图与安全取舍

原图：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S06-flooded-intact.png`。

SHA-256：`72daa317fc6e42fb149d4fc138f88c73beea0b01177fb7d4eeef58868c014200`。

保留湿灰石砖、苔绿、青蓝浅水、蓝色灵魂灯和连续拱廊。原概念图的满地浅水收束为两侧密封浅池，中间保留与水面近齐的无台阶石质通路；尽头画面似为封墙，交付改为可穿行南拱口，以满足一字贯通。灵魂灯原版实际照明不等同于概念图的有色动态光，预览不宣称安装了光影。

## 交付与证据

- `room.nbt`：Minecraft Builder + Lodestone 导出的 gzip、DataVersion 3465、单调色板结构。
- `room.json`：入口、安全点、出口、来源指纹、NBT 指纹、液体范围与独立边界。
- `preview.png`：读取最终 `room.nbt` 的走道内部透视，不是设计原图。
- `qa.json`：只有静态、渲染、人工四向复核、最终 Forge 日志及四份放置快照全部与最终指纹一致才会生成 PASS。
- `views/{north,south,west,east}-{interior,exterior}.png`：四向内部剖视及完整外壳，共 8 张。
- `views/four-direction-review.png`：四向对照总览；`views/plan.png`：水体与通路俯视。
- `qa/static.json`：静态拓扑、液体密封、拱肋连接、吊灯/藤蔓支撑、屋顶与主路。
- `qa/forge-native.json` 与 `qa/forge-run.log`：隔离 Forge 1.20.1 / 47.4.10 原生四向放置、注册表/状态、碰撞通路、canSurvive、液体及 200/240 tick 稳定快照。
- `qa/native-placed-*.nbt`：四向真实放置后逆旋转归一化的快照；最终汇总独立比对每一个原始显式状态。
- `qa/forge-native-initial.json`、`qa/forge-run-initial.log`：补齐拱肩前的首轮通过证据，仅供历史追踪，不代替最终版验收。

所有内部剖视只在渲染层隐藏遮挡面；完整外壳及最终 NBT 不做剖切。

## 重现

生成时在 `C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill` 运行：

```powershell
node scripts/run-build.mjs 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S06/recipe.mjs' --out-dir 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-S06' --name room --data-version 3465 --json
```

随后在本目录运行 `node tools/static-qa.mjs`。使用技能的 `scripts/preview.mjs --nbt <本目录/room.nbt> --no-open` 启动预览，再把它返回的实际本地 URL 传给 `node tools/capture-views.mjs <URL>`。截图脚本会首先核对服务端 NBT 的完整 SHA-256，避免读到其它并行走道。

原生测试位于本目录 `forge/`，运行 `powershell -ExecutionPolicy Bypass -File tools/run-forge.ps1`，使用已有 Java 17 与 Gradle 8.8 离线缓存执行 `runGameTestServer --offline --no-daemon --console=plain -Dnet.minecraftforge.gradle.check.certs=false`。该测试模组仅用于本目录新建的隔离 GameTest 世界，禁止安装到玩家实例。四向 fixture 放置前清理各自测试区域，调度真实液体/邻居更新，并执行堵路、穿孔池底、移除吊灯支撑三类负向探针。

静态、原生、渲染及人工复核均完成后，运行 `node tools/finalize.mjs`，重新核对最终指纹、日志与四份实际放置快照，生成汇总 `qa.json`。

## 验收界限

软件渲染不等于整合包截图。Forge 原生四向放置不等于完整整合包客户端、多人或 FPS 验收；藤蔓随机生长关闭，但支撑/流体调度开启。未测试或执行 FT 大厅接路、唯一世界槽位、占用拒绝、幂等或正式部署；由统一接入线程另行确认。
