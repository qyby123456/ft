# 苔藓古林独立验收合同（C）

测试只属于 `src/mossTest/java`、`src/mossTest/resources` 与 `verification/moss`。运行命名空间 `ftmossgen`，Java 17、原生 Forge GameTest，`@PrefixGameTestTemplate(false)`、`template="empty"`。宿主独占 sourceSets、构建、主代码与串行运行；C 不部署、不修改玩家世界、不运行 Gradle。

1. 多种子与正负区域：重复计划逐字段确定、四个主层、门口邻接图实际连通、无包围盒重叠。独立使用最终模板原生碰撞形状、完整支撑与流体状态建立可行走脚点，在四方向平移及上下一级（需额外头部净空）上做块步距 BFS，计算从入口到每个 eligible 非走廊房 `portal_trigger` 的最短距离。最低 eligible 房层按实际 trigger 世界 Y 选择最大距离，距离相同时按模块 index。模块跳数仅验证拓扑连通，不能用于选最远房。独立重算并核对 `graphEdges`、全部 `roomDistances` 与 `portalDistance`，不得直接信字段或包含无门龛走廊。
2. A 最终 catalog：27 个变体逐一核对打包 NBT SHA256，实际解码非空、尺寸完整。不能因 clone 暂时存在就宣称苔藓资源已验收。检查苔藓材料及与原月蚀资源不同。
3. 真正建造：专用维度内规划并 tick 至 COMPLETE；失败阶段立即报真实 error。12 格 FT 门实际方块、朝向、完整支撑、门前后净空，原房非门格材料保持；不得笼统跳过整间 Boss 门房。
4. 门生命周期：实际玩家在 Boss 门不打开生成选择菜单；门绑定当前实例，重复触发受冷却约束，其他实例不能被启动。Boss 消失与非玩家死亡不能获胜；实际 ServerPlayer 击杀才开放返回并保存胜利。
5. 兼容性：原三 provider（混沌、月蚀、紫晶）仍注册且唯一，新苔藓 provider 独立加入；旧月蚀维度、catalog 与计划仍可读取。不修改、不替换旧测试来掩盖回归。

API 约定：`moss.generator.Planner.Plan` 新增 `bossPortalPiece:int`、`bossPortal:int[3]`、`bossPortalFacing:int`；Catalog/Engine/Store/Gameplay 沿用克隆工程公开接口。Boss 门桥接以 B 实际落盘 `MossBossPortals` API 为准。字段暂缺必须明确失败或报告待实现，不提供假通过和生产桩。

FT 门坐标必须是底部中央原生锚点 `PART=1`，不是底部左格。测试一律使用 `DungeonGateBlock.partPosition(anchor,facing,part)` 计算 12 格、三列支撑；显式断言冻结位置的 PART、BlockEntity 位置与 `DungeonGateBlock.anchor` 一致。不自行添加向右一格的临时补偿来掩盖生产锚点错误。

已核实 A 落盘设计：水平 stride=48、垂直 stride=32；仍有 27 变体，11 个有原生门龛 metadata，常规房 48×30×48、楼梯 48×64×48，另含子布局与独立 Boss。27 个实际磁盘 NBT 的 SHA256 均与当前 catalog 相符；这只是文件检查，不是原生 GameTest 通过。保持完整哈希、解码、材料与碰撞路径断言。测试 provider 仅精确过滤 `ftgateintegration:isolated`，其余生产注册必须精确为原三项加苔藓一项；不修改过时旧 gateTest。

B 已落盘的 `MossBossPortals.handleUse` 只接管交互提示，真正传送由门完全打开后 `DungeonGateBlock.entityInside` 走 `handleTravel`。测试保留点击不弹菜单，再等待真实开门动画并穿过原生门框；Boss 入口、冷却与胜利返回都检验这一实际路径。返回门坐标读取 `MossBossPortals.gates` 的 returning 项，但传送仍调用原生方块入口，不直接调用测试捷径。

保存/加载与实际服务端构建不是断电原子性证明。ready-C.json 区分源码完成、静态检查、宿主原生执行及待验证项；没有本轮实际日志绝不写通过。由宿主在独立 workingDirectory 执行，失败存档不能混入下一轮。

最终补充：在同一集成测试开始时实际获取 Engine 临时区块票据，暂停必须归零；恢复后由真实钻石方块障碍造成预检 FAILED，票据必须归零且保留障碍；随后每个 COMPLETE 回调必须验证 `temporaryTicketCount(level)==0`，不得靠测试调用 releaseTickets 清理后伪证通过。实际生产入口菜单选苔藓项须显示图标、写入类型与种子、保持 unbound 且鼠标无可取物品。三项 GameTest 数量保持不变。原 0.24.2 archive 全部 assets/data 哈希保留归宿主独立检查，C 不改正式安装目录。

外部门完整接口链必须复用首个 COMPLETE 实例：overworld 真 FT 12 格门＋tier2 火盆；真实菜单槽位图标/标题与注册的苔藓 provider 相符，点击后 generatorId 确认为 ftmossgen:forest；choose＋匹配 seed，prepare 两次绑定原 COMPLETE且 jobs 数不增；普通玩家通过 DungeonGateGenerators.travel 进入该实例，生产 MossDungeonGenerator.leave 回原点；原门 entityInside 不得自动重入，走开后恢复触发。不得仅以 providers.all 计数替代接口链，也不为此新增完整建造。
