# Catalog native boundary result

判定：FULL_DECLARED_WIDTH_CLEARANCE 在本批 12 条有限声明条带内通过。4416 条完整原生碰撞记录的精确有理数 4620 tile 覆盖，与本轮 352/352 边界、704/704 双向原生 step/context 执行共同支撑这一结论。192 个 singleton 与 160 个 open interval 均保留，24 次条带初末全量回读通过；23784 次 context 检查、370364 次来源状态/形状检查通过。实际 Forge GameTest 显示 All 1 required tests passed，Gradle exit 0，并正常保存退出。

有限归约理由：每个开区间内没有新增声明宽度 footprint/box 临界边界，参与碰撞的集合与支撑高度固定；固定横坐标、沿旋转后基数方向移动时，垂直及行进轴的固定 box 裁剪约束在实数模型内相同。几何覆盖检查整个区间，原生代表执行验证各实际 step 分支及双向落差，singleton 独立执行，途中逐步核对 context/state/shape/fluid。当前没有具体可复现的声明条带几何阻断，不再把无穷坐标 JVM 遍历设为额外门槛。

completeContinuousMotionProof=false 仍如实保留：没有声称所有浮点输入的原生分支完全等价。实际原生宽度/stepHeight 为 0.6000000238418579，声明宽度为精确 0.6；原生记录保留这一差异。context 不变性是执行轨迹逐步检查加固定状态/形状证据，非任意实体状态或未来世界修改的保证。移动采用 <=0.005 水平细分、原生 move 与重力；没有内部传送，也没有缩小碰撞体通过。源码里的 PENDING 是输入要求原文，执行结果以各 forward/returned.pass 和顶层状态为准。

运行身份：标准 Gradle runGameTestServer 从最终快照未改动的 main/core 源码编译运行；最终发布 JAR 的哈希用于锁定输入身份，不宣称开发映射 class 与重混淆 JAR 是同一字节码。529 个最终快照文件以及复制后的不可变输入再次校验。executed-classes.json 锁定实际编译 class，world-final-hashes.json 锁定测试后世界。

单独保留的范围：侧双门不封闭中央拱道，交互、全生成、重启持久化与保护检查由 integration 独立合并。本证据不修改任何原房间、其他组件、integration 世界或已安装游戏。派生房间 accepted=false，productionPromotion=false。

证据入口：ready.json、verdict.json、native-boundary-report.json、build.log、input-lock.json、final-snapshot-lock.json、verification/、inputs/。build-attempt1/2/3 与 launch-incomplete-attempt2 是实际启动之前的失败/捕获尝试，不能算原生通过；launch 文件只保存 MOD_CLASSES 环境白名单。
