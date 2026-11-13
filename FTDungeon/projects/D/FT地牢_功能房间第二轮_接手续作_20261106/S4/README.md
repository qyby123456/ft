# S4 沼泽湿地 11—20：R3 接入与自然重力补证

当前候选以 ready-for-review.json 为唯一索引；其 ready 标志为 true 前仍在补证。作者检查不等于独立放行，accepted 始终为 false，等待独立复核。R2 冻结目录 S4-submission-20261107-074242-062606 已独立确认源载荷、分片、屏障、静态路线、刻后路线、19 栏外景深及全部 29 份 NBT 从冻结配方重建字节一致，本轮不重复重建建筑。

## R3 针对性补证

新增 access/INSTALL.md 和 build-access.py：在明确预留的外部候台安装原版数据包，普通玩家以权限 0 的 `/trigger fts4_enter set 1` 进入干燥起点，`/trigger fts4_exit set 1` 从内部返回候台。计分按玩家独立，限制候台、维度与结构范围；离开范围/维度后重置进入状态以便重入。两侧屏障保留，受控传送提供外部接入，不把屏障封口冒充可行走入口。安装器绑定同一候选哈希和实际原点，写入候台范围明确；正式世界未安装。

qa/access-gravity-r3 是补证隔离世界：数据包启动前实际安装，接入测试使用带标准 ServerGamePacketListenerImpl 的 ServerPlayer，以真实权限 0 的 trigger 和自然数据包 tick 执行进入/返回，拒绝未授权返回。Forge FakePlayer 的传送为空实现，因此没有用它伪证接入。

11 书廊与 19 实际楼梯的上下往返使用独立重力开启的 FakePlayer：只初始化一次脚点，逐实际服务器 tick 调用 Player.travel，输入 Y 始终 0，不跳跃，不直接注入竖向 move 或校正脚高。楼梯由原版碰撞自动跨半格，下降由自然重力；逐帧保存 game_time、脚高、输入、delta movement、onGround、noGravity。最终只以 exit.json/tests.xml/access-gravity-report.json 和 qa/r3-summary.json 一致通过为准。该证据补足自然重力跨台阶，仍不等于网络客户端按键或生存/战斗。

R2 原始变化继续保留：8 个 air→vine，620/915/933 条同类型原始属性变化，21 个原版新增默认 BE（12 skull、6 空 furnace、3 lectern），另有独立复核观察到的自然怪；不写成原始零变化，不为默认补全重建候选。

本轮只改 19 号几何，其余九房保持 R1 已核验字节。19 号为 29×21×49，栏外新增随 NBT 交付的湿地景深：浅水、四棵带根红树、近远残石柱和木桩，降低观察位前栏沿，保留干燥平台和原目标。player-outlook.png 从局部脚点 [20.5,7,21.5]、眼高 1.62 朝栏外拍摄完整源 NBT；玻璃围护和顶棚均为实际方块。该景深仍是有限封闭环境，不以概念图雾效代替交付。

## 当前证据

- 合并 assembly/s4-wetland-connected.nbt：415×24×67，SHA256 b01fdaf300987e299d141f1963530ad2b1861be8fe4c17631cdc879f7c7415d8。十源模块逐格保持 123439 格；18 分片反拼 667320 格、32 个源方块实体、2 具装备架，分片每轴≤48。见 assembly/qa/assembly-check.json。
- 十房在独立输出重跑配方，压缩 NBT 字节完全一致：qa/reproducibility.json。合并及 18 分片也在 qa/reproduced-assembly-r2 独立输出重建，19 份压缩 NBT 字节一致：qa/assembly-reproducibility-r2.json。这些均为作者复现，供独立审查者另跑。
- 当前整体世界证据位于 qa/merged-world-r2：隔离 Forge 1.20.1 ServerLevel 放置、邻居更新、自然 800 tick、随机刻速率 3、火焰刻开启、小麦探针生长；最终结果须同时核 world-report.json、tests.xml、exit.json。测试平台按实测地形最高点上移，放置前清理测试基线实体，放置后不删除实体或掉落物。失败和基线污染日志保留在 history-*。
- merged-interaction.json：全部原 56 个目标从统一起点逐一往返，实际 FakePlayer.move(MoverType.SELF) 碰撞位移；六个拉杆经 ServerPlayerGameMode.useItemOn 各打开再关闭邻门；32 个空容器实际打开菜单，接近路线、眼距≤4.5 和命中射线有记录。每次交互后重新求图并返回起点。脚本运动不等同真人客户端按键或生存通关。
- 接口全宽全高检查逐一覆盖 20 个模块接口及 9 条连接廊的每个 X 列，使用实时世界 AABB 碰撞形状。两端对外终端属于有意封闭的联合包络屏障，单独记录为 capped；未宣称接通外部地牢/正式世界。不能把 56 个功能目标替代全截面覆盖。
- after-neighbor-updates、after-800-ticks、after-player-interactions 三阶段 NBT 均保留；qa/r2-all 对十房、合并及三份快照共 14 份结构重跑注册/模板、静态支撑和路线（280 个目标）。这是复用独立工具的作者运行，不是本次独立批准。
- classified-differences.json 按捕获偏移 [-2,0,-2] 精确对齐，逐项列出类型/显式属性/默认属性变化、全部源与原版生成 BE、两具装备架。实际有限藤蔓扩展保留，不宣称没有类型变化；刻后全部原路线另验。装备 Damage:0、默认字段、运行时 UUID/Motion 等列出；ArmorStand 不保存源中 Mob 专用 PersistenceRequired 标志，不声称原始实体载荷逐字不变。
- 新完整/剖切总览和 segment-1—5 为真实 Lodestone 渲染并已目检；19 号额外提供实际观察位。渲染不显示装备架实体，须结合 NBT/世界证据。
- DELIVERY-SHA256.json 绑定当前配方、工具、设计、参考、预览、QA、全部分片；清楚列出外部只读依赖与缓存/历史排除项。旧 S4 的 577 份继承文件再次核哈希，保持不变。

## 复现入口

使用本机安装的 minecraft-builder-skill、Node、指定 Python 和 Java 17；外部依赖路径/哈希见完整清单。只读依赖不得回写。

1. Python qa/reproduce.py：重建十房到 qa/reproduced，14 号自动补两具原生装备架。
2. PowerShell assembly/reproduce.ps1 -OutputDirectory <全新输出绝对路径>：使用当前已冻结房间生成合并、layout、18 分片并逐格反拼；不覆盖 assembly 正式候选。qa/check-reproduced-assembly.py 对约定复现输出比压缩字节。
3. Python qa/make-assembly-input.py 后 PowerShell qa/run-assembly-world.ps1：独立新世界运行整组，工作目录为本组 QA 下带时间戳的 server-merged-*。不得改正式存档/模组/配置/KubeJS。
4. Python qa/classify-merged-r2.py，再 qa/prepare-r2-audit.py，再 PowerShell qa/audit-current.ps1 r2-all：分类三阶段载荷并重跑全部原生静态检查。
5. R3 补证用 PowerShell qa/run-access-gravity.ps1 在全新隔离世界运行；Python qa/finalize-r3.py 核验补证、冻结建筑哈希及实际已安装数据包，复制可安装包到 access/datapack-tested；最后 qa/delivery-index.py 封存完整清单。qa/finalize-r2.py 只对应旧 R2 状态；qa/finalize.py、qa/current、qa/after-world 及 qa/worldtest 的单房结果是 R1 历史入口，不能覆盖 R3。

## 认证边界

最终 R2 实际世界报告记录 doMobSpawning=true、doWeatherCycle=true；火焰刻开启、随机刻速率 3，本测试未修改规则。规则开启和有限小麦生长不等于完成刷怪、天气或生存认证。未测试旋转/镜像、长期生长、战斗/精英/Boss；未部署正式世界或接管共享生成器。20 号拉杆为有背衬装饰，容器无战利品表。独立 R3 补证审查仍是下一步。
