# S7 暗金囚笼 · 独立制作交付

仅制作第三批 S7。原创布局，灾变仅作为暗铁、黑曜石、金饰与红符文的视觉参考；只使用 Minecraft Java 1.20.1 原版方块。未修改其他房间、共享 incoming、核心源码、正式存档或已部署 JAR，也没有调用付费生图接口。

## 交付与坐标

- 四个交付文件：`out/S7/room.nbt`、`out/S7/room.json`、`out/S7/preview.png`、`out/S7/qa.json`。
- 模板尺寸 XYZ：**9×9×9**；保留 **7×7 净内室、五格净高**。Y=0 石底，Y=1 封液层，Y=2 完整地面，Y=3 玩家脚部，Y=8 完整屋顶。
- 北入口脚部 **[4,3,0]**，内部安全点 **[4,3,2]**，朝北。接口 X=3..5、Y=3..5、Z=0 为三宽三高纯空气，下方 X=3..5、Y=2 有完整地板，无门扇或突出的门框。
- 北入口可往返到安全点、刑台左右两侧 [2,3,4] / [6,3,4] 和囚笼前 [4,3,5]。原生碰撞检查确认 30 个同层可达脚位。
- **接入时不要把入口当成 Y=1。** 必须接到 Y=2 地板高度并保留三格高净空，坡道由接入负责人统一安排。

## 设计转译

- 南侧装饰囚笼：12 格铁栅，黑曜石立柱、錾制黑石梁、镶金黑石装饰。铁栅不是唯一通路，没有可操作的关闭铁门。
- 中央锁链刑台：六格石质台体、磨制黑石半砖顶、红石核心，两侧可绕行。18 格竖直锁链的上端连接实际屋顶锚块，下端为自由悬垂装饰。
- 地下熔岩缝：11 个真实熔岩源，完整石底、石侧壁、完整橙色玻璃顶盖；玩家只会踩到完整玻璃，不会接触熔岩。没有裸露火焰、危险地板或必经跳跃。
- 三个骷髅头均有石座 / 骨块支撑；两组点燃黑蜡烛有石座；红旗固定于西墙。旗帜花纹为装饰，不是奖励物品。
- 无容器、奖励库存、战利品表、刷怪笼、命令方块、活动传送门或预放实体。

## 指纹

- 原图：`D:\ft工程文件\image2.5\dungeon-design-batch3-20261101\out\S7-cataclysm-dark-iron-cell-hero.png`
- 原图 SHA-256：`6b5857d5b06a07ab67343b8faf1fc353802d56e14a24801681ab14ba94c8015e`
- 最终 NBT SHA-256：`69714984d9c1dde963be7239b4bc61982a05ed20fb466bd29eacd13c52830a28`

## 验收证据

- `qa/static-qa.json`：22 项静态检查通过，完整体积共 729 条记录，包含显式空气；屋顶、底板、接口、双向通路、熔岩封闭、锁链 / 头骨 / 蜡烛 / 旗帜支撑及原版资源合法性通过。
- `qa/native-north.json`、`qa/native-east.json`、`qa/native-south.json`、`qa/native-west.json`：**Minecraft 1.20.1 + Forge 47.4.10 独立 GameTest，4/4 通过**。每方向进行真实 StructureTemplate 加载、保存、世界放置，以及初始 / 100 tick 后全部 729 个状态逐一比对、原生碰撞通路、支撑、方块实体和熔岩无外泄检查。
- `qa/forge-gametest.log`：最终运行包含四条 `S7_NATIVE_PLACEMENT_PASS`、`All 4 required tests passed` 与 `BUILD SUCCESSFUL`。
- 前两次为隔离测试配置失败，而非结构验收失败；缺少 pack.mcmeta、模板命名空间重复均已修复。原日志保留为 `qa/attempt-01-resource-loading.log`、`qa/attempt-02-template-namespace.log`，没有抹除失败记录。
- `preview.png` 来自最终 NBT。`preview/four-views.png`、`preview/exterior.png`、`preview/floorplan.png` 补充四向、封顶外观与通路平面。使用原版模型 / 纹理的软件投影，剖切不写回 NBT；**不是游戏截图**，不模拟整合包光照、粒子或自定义着色器；头骨和旗帜为简化实体外观。

## 重建

使用本机 Codex bundled Python：`C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`。

1. 运行 `tools/build_s7.py` 生成结构、元数据和静态 QA。
2. 运行 `tools/run_native.ps1` 执行四向隔离 Forge GameTest。脚本只临时设置进程 JAVA_HOME / PATH 并在 finally 恢复；旧结果先移除，不能把旧 PASS 当成本次证据。
3. 运行 `tools/render_s7.py` 从最终 NBT 生成预览。
4. 运行 `tools/finalize_s7.py` 校验所有指纹与证据，生成四文件交付、接入报告及 `S7-delivery.zip`。

只读复用已存在的 `output/dungeon-expansion-20261101/tools/nbtio.py` 及主接入工程 `tools/review_four_views.py` 的模型 / 纹理读取工具；不写入其目录，禁用导入的字节码缓存。结构生成器、深度缓冲渲染器和 GameTest 源码均保存在本独立工程。Forge / Gradle 与原版客户端纹理使用本机既有缓存。

## 接入边界

`HANDOFF.md` 和 `handoff-report.json` 是给接入聊天 `01a19758-b0e5-75c2-822a-4860527a1e8b` 的落盘报告。**未向其他聊天发送消息，未部署。** 独立 GameTest 不等于 FT 大厅道路 / 持久化 / 占用拒绝的核心接入验收，也不等于整合包客户端游玩；这些须由接入负责人统一验证后再决定部署。
