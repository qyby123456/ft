# M12 菌菇地窖 · 独立建筑交付

2026-11-02，Asia/Shanghai。只制作本目录；未部署、未打开正式玩家存档、未修改 FT 核心、共享目录或其他线程的房间。未重新生图、未调用收费 API、未接触图像密钥或配置。

## 交付接口

- 根目录四件套：`room.nbt`、`room.json`、`preview.png`、`qa.json`。
- Minecraft Java 1.20.1，DataVersion 3465，gzip 单调色板结构模板，**17×11×17（XYZ）**。
- 实际记录 3,179 格，包含明确空气；非空气 1,589 格，38 个状态，只用原版方块。
- 北向入口 `entrance=[8,2,0]`，内部安全脚位 `interior=[8,2,4]`。入口三格宽、三格高，向内六格直进净空，下方完整地板。
- 坐标是模板相对玩家脚位，不是脚下地板；`facing=north`。拟用模板名 `ftdungeonmobs:image25/m12`，仅为建议，ID 唯一性、固定槽位和大厅连线由接入负责人确认。
- 本交付包含显式空气，但不是授权清空旧建筑。生产接入必须对完整体积、世界边界、液体及实体预检，占用就拒绝，并保持持久生成标记与幂等，不覆盖玩家修改。

## 原图与落地

原图：`D:/ft工程文件/image2.5/dungeon-rooms-medium-random-20261102/out/M12-fungal-cellar-hero.png`，1536×1024，1,943,153 字节；SHA-256：

`827226cc6b2d2a0ad450bde5342825cb197898601d83a05854b313cb6e8df96f`

成品 NBT SHA-256：

`10173ff303a3726e5cf43ca9dc98596e7201b784751f73ae2929667facee3970`

保留潮湿苔石墙与斑驳地面、双侧巨型红蘑菇与菌床、后墙三层储物架和梯子、采集工作台、培养盆、木桶、双链条吊灯和左前浅水洼。将概念尺寸合理放宽至 17×17，为巨菇和工作台两侧干燥绕行留出空间。家具全部落地或放在架板/桌面；立柱嵌墙，不穿家具。

原版盆栽蘑菇代替参考图自绘玻璃菌种罐，八只木桶均为空；两处原版孢子花提供原生孢子效果，另有四处垂根、六处贴墙发光地衣。固定巨菇使用原版菌柄和红蘑菇方块，不借用模组方块或实体。12 朵小蘑菇全部种在灰化土，单组密度也限制随机蔓延。

六格真实水源位于 y=1，底板与四侧完整封闭。主路不经过水，不使用裸露熔岩、火焰、蛛网、粉雪或带伤害植物。完整石砖顶盖位于 y=10，共 289 格；剖切仅存在于预览，NBT 不删顶。无奖励、战利品表、刷怪笼或结构实体。

## 验收与证据

1. `qa/static.json` 独立重读实际 NBT，检查指纹、尺寸、唯一坐标、单调色板、全底/全顶/四墙、3×3 北接口、六格入口净空、水密封、支撑、库存和十处地标干燥连通。静态材料类通路计数 204；它不等于原生碰撞结果。
2. `qa/native.json`、`evidence/forge-gametest-attempt1.log`：Java 17.0.8、Minecraft 1.20.1、Forge 47.4.10，真实 `GameTestServer`、`ServerLevel` 和 `StructureTemplate.placeInWorld`，不是仅解码 NBT。
3. 同一原件按 0°、90°、180°、270°独立放置，共逐格核对 **12,716 条记录**；每方向 1,589 个非空气方块通过原生 `canSurvive`，8 个空桶方块实体合法，10 处地标都可往返北入口，原生碰撞口径为 **203 个干燥可达脚位**。静态多算的一个贴墙梯子脚位在原生碰撞检查中被保守排除，安全点与地标均不受影响。
4. 邻居更新后，显式调度水 tick，并调用小蘑菇原生随机 tick **1,536 次**；独立夹具运行 **400 tick** 再逐格复查。四向所有记录保持不变：无菌菇脱落/扩散、无吊饰掉落、无水源外溢、无掉落实体；完整顶盖、空库存、路径和外围 1,514 格留白均保持。随机 tick 速度 3、火蔓延开启；隔离夹具关闭自然刷怪以避免测试场景外生物干扰。
5. 日志实证：`M12_NATIVE_GAME_TEST_PASS: rotations=4/4; records=12716; ticks=400`、`All 1 required tests passed`、`BUILD SUCCESSFUL`。首次正式原生验收通过；初次空目录启动的 `server.properties`/Forge 默认配置初始化提示和构建 API 弃用警告不属于房间失败。
6. `preview.png`、`preview/four-views.png`、`preview/roof-exterior.png`、`preview/floorplan.png` 从成品 NBT 与本机 1.20.1 原版模型/纹理生成；水纹理使用首帧和青蓝色预览色调。这是**结构软件渲染，不是游戏截图**，不包含客户端光照、粒子、着色器或 FPS 测量。四视图和完整屋顶已目视核对。

各文件完整哈希见 `delivery-manifest.json`，交付 ZIP 指纹见 `package-receipt.json`；只读来源指纹和边界观察在 `evidence/`，不把并行源文件自然变化误报为本线程修改。NBT 工具和模型渲染工具从 M6 工程只读复制，来源指纹已保留。

## 复现

运行 `./Run-QA.ps1 -Rebuild`。使用本机已配置 Python/Pillow、Java17、Gradle8.8/Forge缓存；每次原生测试创建本目录下独立的新夹具世界，不使用任何正式 `saves`。脚本会恢复过程环境变量，不部署测试模组。

手工流程：`tools/build_m12.py` → `tools/static_qa.py` → `native-test` 中 `gradle --offline --console plain -Dnet.minecraftforge.gradle.check.certs=false -PqaRun=<唯一值> runGameTestServer` → `tools/render_m12.py` → `tools/finalize_delivery.py`。手工原生运行还须保存 stdout 日志及 `evidence/native-run.json`；建议直接使用入口脚本，以免遗漏证据关联。

## 接入边界

未运行完整整合包客户端/多人实机，不宣称该层验收。接入负责人仍需确认 M12 编号、固定槽位、大厅可步行连接、占用拒绝、幂等、五档难度和个人返程兼容、生产打包及部署回滚。测试模组源码和夹具世界不能放入生产 `mods`。ZIP 排除 Gradle 构建缓存及夹具世界，仅保留交付、可复现源码和验收证据。
