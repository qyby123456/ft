# H05 穹顶大殿 · 独立交付

唯一模板 ID：zone-H05。Minecraft Builder / Lodestone 0.6.0 生成，Java 1.20.1，DataVersion 3465，尺寸 XYZ 31×27×39，32643 条含显式空气的唯一记录，11298 个非空气方块，32 项实际调色板状态。

NBT SHA-256：8ac1fcb78cd2dfe439254ddcf669fb6501ab4bf8e95a929e7dbbaebe56a4d68c。

## 四件套与证据

- room.nbt：完整封闭的三跨拱顶大殿；仅北面声明入口开口。
- room.json：入口、安全点、通道、平台、尺寸、设计来源、完整指纹和偏差。
- preview.png：最终 NBT 的 Lodestone 北侧室内渲染，非游戏截图。
- qa.json：交付验收汇总；delivery-manifest.json 是文件级完整 SHA-256 清单。
- qa/static.json、qa/native.json、qa/reference.json、qa/visual-review.json 分别保存静态、真实 Forge、图源、目视检查证据。
- views/8ac1fcb78cd2dfe4/zone-H05/ 包含北南西东四向内部剖视、四向完整外壳、两张四宫格及同哈希 NBT 快照；views/lodestone-*.png 是完整 NBT 的眼平/外观渲染。
- source/ 保留可复现配方、渲染与审查程序。native-test/ 仅为隔离 Forge 验收夹具和测试世界，禁止将测试模组或世界作为正式交付部署。

## 坐标与安全路径

本地坐标，NBT 原点为西北底角。北入口脚部 [15,2,0]，入口净宽 9 格，中央净高 9 格；安全点 [15,2,5]，周围 x=13..17、z=3..7 是 5×5 干燥净空区。主通道 x=13..17、z=0..30，脚部 y=2，净宽 5 格、净高至少 4 格，红色羊毛嵌平地面避免地毯高度变化。

两组朝南石砖楼梯位于 z=31、33，分别通往 y=3、4 的平台。平台红毯表面脚高 4.0625；room.json 给出准确落点，不应将整数 y=4 误写为地毯表面。原生测试使用 0.6×1.8 玩家碰撞箱检查台阶、平台及 1/16 地毯边缘的实际碰撞和踏面支撑。

六根主柱位于 x=8/22、z=9/20/31，柱芯从地板至柱头连续，拱脚向柱体连续承托。三道横向拱肋与纵向石梁连接，屋顶在全部 1209 个水平位置保留至少双层实心拱壳，所有屋面台阶相邻面相接。静态外部空气洪泛将唯一北入口临时封闭后未发现内部泄漏；此封闭仅用于分析，不改变 NBT。

四组吊灯有完整石质承盘、落在承盘上的蜡烛、承盘下方悬挂灯笼以及一直连接到屋面的链条。所有非空气方块属于与基础相连的单一六邻接组件；原生 canSurvive 另行核验真实支撑。没有流体、开放火焰、掉落方块、实体、方块实体、库存、战利品或刷怪机制。

安全点表示通行净空、干燥和支撑安全，不代表修改正式世界的刷怪规则或提供无敌区域。自然刷怪仅在隔离验收夹具内关闭。

## 图源与艺术化取舍

源图 D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/halls/H05-halls-vaulted-grand-hall.png。

原图 SHA-256：2985f763563c014582ded69a7fcd8b950098934ddf5452a5c5b7ade2ed6b805c。reference/ 保留图、批次 README、manifest、verify。原图完整解码为 1536×1024 RGB，字节数 2322958，两个批次清单均匹配。

保留灰石高拱、独立落地柱、蓝金彩窗、四组蜡烛吊灯、红金中轴、后方两级平台、侧边书架与桌案。原图是艺术化剖视，本结构补全前墙及完整三跨穹顶，尺度落实为 31×27×39；柱上青金纹章改用陶瓦镶嵌，木箱为实心木材装饰，侧边书架无库存。红毯主路使用嵌平红色羊毛，平台才使用真实地毯。未声称自定义纹理、FT 交互火盆、战斗波次、奖励或与概念图一致的着色器效果。

剖视图仅移除屋面面板和两面近侧外墙，保留六柱、三道拱肋、悬挂链条和锚点；最终 room.nbt 屋顶始终完整。完整外壳四向图负责核对实际轮廓。Lodestone 内景与原版材质数据图均为软件渲染，不能当作整合包实机截图。

## 原生验收与复现

隔离 Minecraft 1.20.1 / Forge 47.4.10 / Java 17 GameTestServer，实际 StructureTemplate.placeInWorld 四向放置。每向 32643 条精确状态记录，合计 130572 条；立即及 400 tick 后复查，执行邻居形状与通知更新、每个非空气方块的 canSurvive、玩家通路与安全点、台阶碰撞箱、完整地板屋壳、柱梁吊灯、外部空气边界和无掉落/实体。完整成功状态以 qa/native.json 和 qa.json 为准；源码或中间 RUNNING 标记不能替代通过证据。

重跑：先用 Minecraft Builder 的 scripts/run-build.mjs 执行 source/recipe.mjs，指定本目录为输出目录且 --name room；接着 node source/static-qa.mjs、node source/prepare-fixture.mjs；用配置 Python 执行 source/run-forge.py --run 新唯一名称。渲染使用 source/render-four-views.py 与 node source/render-lodestone.cjs。修改结构后必须重跑全部验收并刷新清单，禁止复用旧哈希的视图快照。

历史拒绝证据保留：静态首轮 12 块斜接拱脚已补实；Forge attempt1 在 y=100 受测试世界地形占位拒绝，改用 y=240 全新世界；attempt2 碰撞采样遗漏地毯 1/16 高度，修正为连续采样；attempt3 停在 ForgeGradle 远端证书探测，保留线程栈并仅终止已核实属于 H05 的两进程，离线依赖完整时关闭该次构建的远端环境探测后复跑。未修改正式安全设置。

此交付只写 zone-H05，本任务未部署 JAR、未改正式源码/结构、未打开玩家世界、未覆盖并行房间。隔离原生检查不等于完整整合包客户端、多人或 FPS 验收。
