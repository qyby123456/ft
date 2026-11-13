# 交付验证 · 0.20.1（Boss 房单向集结门）· 2026-11-02

## 0.20.1 当前验证

- `runGameTestServer`：隔离 Forge 47.4.10，10 项必需测试全部通过，未使用正式玩家世界。
- `BOSS_GATE_VERIFIED assertions=93`：四向过门、靠近/右键不触发、前进/反向碰撞、远端队友集结、其他维度隔离、落点不足不拆队、危险地面/液体、可取消接口、模拟拒绝传送的回退、独立落点、旧返回 NBT、轮次锁、保存重载、迟到/死亡等待与下轮重启。使用真实 ServerPlayer 和原生传送/碰撞，仅网络发包由测试监听器丢弃。
- `BOSS_GATE_PLAYER_TICK_PASS`：通过 Forge EVENT_BUS 上注册的 PlayerTick 入口触发过门，不仅调用内部方法。
- `DIMENSION_DIFFICULTY_TEST`：147 条断言通过，覆盖远端实体五属性、血量比例、手动覆盖、跨维度隔离、门口控制、持久选择、旧火炬和本轮锁档。
- 房间、画廊、刷怪点、普通/精英池和冒险保护原有测试继续通过；小房安全空间不足时保留原有点数降额。
- `runData`：原生注册、维度 codec、三种门共 3456 状态及唯一锚点实体、门动画/碰撞、建筑/配方、房间与 Lootr 校验通过。完整 `build` 和 JAR 审核见 `output/boss-gate-20261102/`。
- 没有启动完整整合包客户端、没有真人客户端接入或多人网络游玩验收；自动化服务器测试不等于实机美术验收。候选未安装，正式实例仍为 0.20.0。

## 历史 0.20.0 验证记录

## 0.20.0 验证结果

- 完整离线 `build` 成功，最终 34s；全部既有模型、门、火盆、血池、房间与建筑资源验证通过。证据在 `output/room-tiers-adventure-20261102/`，最终构建日志 `build-final.log`。
- 未安装灾变的可选依赖分支最终六项 Forge GameTest 通过（`gametest-vanilla-repeat.log`，57s）；加入实际 Cataclysm 3.31 与 LionfishAPI 3.0 后，含祭坛交互的六项原生测试通过（`gametest-cataclysm-altar.log`，1m 9s）。在隔离真实 FT 维度执行，不直接打开玩家存档，不把单元测试冒充完整整合包游玩。
- 全部 11 房生成安全点并逐一断言房间等级：S1/S2/S3/30_1 普通，M1/M2/L1/L2/L3/L4/31 精英。五档 2/4/6/8/10、空间/视线限制、精确固定组、取消/阻挡重试、保存、新轮清理等原功能回归通过；小房仍允许少于目标，不破墙凑数。
- 逐个原生创建并实际生成 22 种灾变与五种原版代表怪（僵尸、卫道士、女巫、凋灵骷髅、猪灵蛮兵），检查注册 ID、持久性、目标、轮次档位、安全碰撞及死亡不补刷；额外检查 FinalizeSpawn 拦截不消耗名额。猪灵蛮兵使用原生 Brain 目标记忆并禁止僵尸化。排除完整 Boss/宠物，缺模组不会把精英房降成普通房。
- 旧无命名空间组与未分级保存迁移通过：仅替换未生成且等级错误的剩余名额，保留已消耗数量、已生成组前缀与 activated 状态，迁移标记持久保存，不重发已刷怪物。
- 生存、创造、冒险、旁观四种原模式均验证入场转冒险、地牢内命令不得切模式、重连不改原模式、死亡/克隆、外部重生和离开恢复原模式。无客户端连接的 FakePlayer 登入/克隆/重生直接调用本模组事件处理器，避免其他模组发送联网包；维度/模式和保护事件通过真实 Forge 总线，不声称测试了真人断线网络。
- 验证带 CanDestroy 工具不可破坏、放置/桶被拦、交互事件保留方块用途、食物与怪物攻击允许、装饰攻击被拒、爆炸只阻止方块伤害、怪物破坏禁止、其他维度不受影响。另搭完整原生血池：冒险玩家手持两份头颅献满 16 点、空手触发祭坛成功，修复原 `mayBuild` 导致交互被误拒的问题。
- 发布包 654463 字节，SHA-256 `9bee4bb411ef404ef34163375115e5b5f5dc21512019e9e398d76c0c5914920f`。相对 0.19.0，390 个旧条目逐字节不变，12 个预定条目更新，新增三类；326 份源资源完全匹配，旧语言键全部保留，模板/贴图/几何不变，未捆绑测试代码或灾变依赖。
- 已安装至当前 Forge 实例并备份 0.19.0，活动同 ID 包唯一，原配置与其余 385 个模组文件元数据不变，禁用挑战扩展未启用。部署报告 `deployment.json`；没有启动当前整合包、修改其它实例/远端服或打开玩家世界。
- 初次回归发现猪灵蛮兵 Brain 目标未设置，已修复并原生重测；另一次 FakePlayer 人为广播登入导致 Forge 尝试向空网络通道发包，测试改为直接调用本模组相应处理器，生产事件仍正常订阅。失败日志保留，不掩盖过程。原有 Iron's Spellbooks 战利品表告警未修改。
- 在同一开发测试世界重复运行时，祭坛 fixture 曾沿用上次已激活的方块实体而导致断言失败；已在测试开始清除旧祭坛后重新搭建，不修改生产献祭/冷却语义，重跑通过。安装后独立审核 `post-deployment.json` 确认包/备份/原配置哈希、活动包唯一和无 pending/锁残留。
- 尚待真人完整整合包联机、美术观感、战斗平衡与所有第三方法术/道具交互复核；绕开 Forge 事件直接改世界的其他模组、管理员世界编辑命令不在事件保护保证内。具体名单和边界见 `ROOM_RULES.md`。

## 0.20.0 地牢方块防爆补验

- 当前实例安装的 0.20.0 已默认订阅地牢专属爆炸和生物破坏事件；本次核对安装包字节码与原 SHA-256 一致，仅补充测试和说明，不修改生产代码、不重复换包。安装包仍为 654463 字节，SHA-256 `9bee4bb411ef404ef34163375115e5b5f5dc21512019e9e398d76c0c5914920f`。
- 新增真实 `Level.explode` 回归：分别以苦力怕、TNT、大火球及无实体方块来源，在 FT 地牢与主世界各执行一次爆炸。地牢 225 格玻璃地板、箱子及其中三颗钻石完整，方块破坏列表为空、无对应掉落和爆炸点火；受击原生生物实际扣血，主世界仍正常炸坏方块。独立断言共享 `mobGriefing` 保持开启，测试结束恢复原测试世界规则和难度。
- 不加载可选灾变依赖的七项 Forge GameTest 全部通过（`output/explosion-protection-20261102/gametest-await-tracking.log`，1m 51s），包含原有六项和新增爆炸回归；不是只手工发送爆炸事件。
- 加入实际 Cataclysm 3.31 / LionfishAPI 3.0 后，同样七项测试全部通过（同目录 `gametest-cataclysm.log`，1m 32s），含真实爆炸保护及 22 种灾变原生实体生成回归；没有把可选依赖捆进发布包。
- 初始测试夹具在实体加入后立即或固定延迟查询，实体尚未进入原生空间查询索引而失败；改成原生 GameTest 序列等待实际查询可见、显式使用正常难度并保留测试生物后通过。失败日志保留，此过程不修改生产防爆语义，也不将夹具问题描述为玩家存档故障。
- 所有爆炸均在 `run/rooms-gametest` 隔离开发世界执行，未启动完整客户端或打开玩家世界。验证的是原生/Forge 爆炸管线，并非逐种复演全部灾变特殊攻击；绕过 Forge 事件直接改方块的模组或世界编辑命令仍在保证范围之外。规则边界见 `ROOM_RULES.md`。

## 0.19.0 验证结果

- `BUILD.ps1 -Task runGameTestServer -Offline`：`All 4 required tests passed`，完整执行 2m 12s；`BUILD.ps1 -Task build -Offline`：成功，37s。日志与交付报告保留在 `output/spawn-points-20261102/`。
- 原两间场景：实际地牢维度中的 25335 条记录、17 个 Lootr 方块实体、方块/实体预检、幂等改建、序列化与 40 tick 挡水回归通过；九间主题房的 49575 条记录、入口/内部步行、共享道路、独立标记和延迟液体检查全部通过。
- 新原生刷怪测试：五档目标 2/4/6/8/10；只在可达地面布点、入口留空、点间距、无碰撞符印、默认组 1～3 且不混入王；创造/旁观/过远/隔墙不触发；实体加入事件取消和空间阻挡不消耗数量；和平暂停；固定战士/弓手/法师精确生成、属性固定 3 档；连续检查不重复、序列化后不重刷、移除怪物不重新武装、新轮清理和旧轮实体回流拒绝通过。
- 实际 11 房五档布点：31=10、30_1=10、L1=10、L2=10、M1=10、M2=10、S1=2、S2=8、L3=10、L4=10、S3=3。S1/S2/S3 因净空、入口留白及三格间距少于目标；这是本次随机布点实测，不将安全上限冒充每轮必定刷满十点。
- 房间实测暴露原始场景入口为浅水环绕的起伏地面，已按原始自然场景允许有底浅水过路/一格高差，实际落点和生成实体仍要求干燥净空。主题房继续使用楼梯连通规则。
- 既有房间 GameTest 的无玩家维度实体阻挡夹具曾因区块未进入实体追踪状态失败；仅在隔离测试中保持对应夹具区块加载并等待十 tick，断言不放宽，完成后释放。生产 `DungeonRooms` / `DungeonRoomGallery` 建筑预检及实体保护代码没有修改。早期失败日志保留在同一输出目录。
- 新符印资源校验：8 个整数长方体、35 面、2 个方块状态、中英文键均有效；复用既有纹理，无新纹理、方块实体或独立渲染器。既有 GeckoLib 四模型 218 骨骼/17 动画、火盆、门、血池和 24 建筑方块验证全部通过。
- 相对已安装 0.18.0：385 个旧 JAR 条目逐字节不变，只允许 manifest、模组描述/版本、两种语言、主注册类、门轮次数据类及命令类共七个旧条目变化；新增十个运行条目，不包含 GameTest 代码。全部原模板、几何、动画与既有贴图不变。
- 最终 JAR 644057 字节，SHA-256 `679cae94edb0ac63d5e572a2bfcdc44d56f165b1603410705ad93f0acd79db95`。已安装包与固定交付副本一致，旧版备份与配置验证通过，同 ID 活动包唯一；其他 385 个模组文件元数据未变，未打开玩家存档。
- 验证边界：运行的是隔离开发服和真实 Minecraft/Forge API，不是完整整合包客户端人工游玩。旧版 Iron's Spellbooks 开发依赖仍输出其原有战利品表解析告警，未修改该依赖。美术观感、多人实机节奏和整合包全模组交互需进游戏复核，不声称已验收。

## 0.18.0 验证结果

- 三路真实并行导出，每路八款，PID 分别为墙地材 36124、柱拱件 30816、装饰件 16724；工作区间确有重叠。先做柱座小样，检查整数网格与预算，再批量制作；语言、标签、Java 目录串行汇总，避免并发覆盖。证据为 `preview/worker-*.json` 和 `geometry-budget.json`。
- 原生 Minecraft `BlockModel.fromStream` 验证 24 款资源，其中 22 款自定义几何 95 个整数长方体/500 面，两款运行时复用原版楼梯/台阶。每款不超过 16 体块/96 面，实际最多 13 体块/66 面；UV 在 128×64 图集上对齐整像素，全部不透明，无平面卡、楔面、曲面、体块旋转、逐像素几何或逐帧副本。物品、唯一配方、掉落存在性、语言与镐标签通过。
- 完整离线构建首次 `BUILD SUCCESSFUL in 55s`，补齐元数据/来源后最终 `BUILD SUCCESSFUL in 32s`；编译、重混淆及全部 `check` 通过。原有四套骷髅、火盆、血池、门与房间原文件验证均回归通过；常规 `test NO-SOURCE`，不将自定义资源验证器冒充 JUnit 或实机测试。
- Forge `runData` 最终 `BUILD SUCCESSFUL in 44s`：24 个方块和物品注册、174 个状态、24 份原生配方、22 款自定义几何与四向碰撞精确并集、整数边界、固定亮度和无新增方块实体均通过；原维度、门轮次/动画、血池、11 份房间与 Lootr 兼容验证回归通过。首次运行以本地原生进程退出码 `-1073741819` 终止，无 Java 异常栈；未改代码/运行环境再次执行后通过，保留失败日志，不推定该原生异常的原因或声称已修复。
- 原生 Blockbench 加载全部 24 份分组编辑源，各含根组与主体子组、单图集、整数 Pivot/几何、零旋转；保存拱券、笼灯、锁环前后代表截图，另有软件四向预览。原先未保存工程的完整编译快照与 saved 标记逐字一致，恢复原选中，不关闭或修改其它原项目。
- 最终 JAR 615710 字节，SHA-256 `89c40826b66ca76bc08e5254e334e57e60053fcf1b6d25a7d44fcb91fe8a5758`。ZIP CRC、唯一条目、模组 ID/版本、依赖未变、来源文档打包、323 份源码资源完全一致、无编辑源/工具/验证器混入检查通过。
- 以安装前 0.17.0 为基线：旧 133 份美术/绑定、59 份其它数据和 57 个运行类逐字节不变；原房间 NBT、目录、地牢维度、血池/门逻辑没有变化。235 个旧 src 文件仅六个预定接入文件修改；旧语言键值保留，镐标签只追加新套件标签引用，既有可编辑文件与七份解包模型/贴图参考哈希均不变。现有类仅主注册、客户端创造栏接入及相关内部类行号变化；新增类仅三种建筑类及两种 record 内部类。
- 已在确认目标游戏未运行后备份 0.17.0 并安装 0.18.0；仅一份活动同 ID JAR、没有 pending/锁残留，原配置及其余 385 个模组文件哈希不变，FTBoss 未改，FT 地牢挑战仍禁用。独立安装复核为 `POST_DEPLOYMENT_PASS`。
- 本批只有建筑与固定光源，不增加铁刺伤害、棺材库存、自动开门、血槽液体传输、波次或房间替换；无新增 BE/ticker/自定义渲染器。没有启动客户端、打开玩家世界或测量 FPS；放置、楼梯连接/含水、挖掘掉落、实际光照与库存 Display 留待备份世界验收。既有返程门占用 P1 未混入此次修复。
- 完整证据：`output/dungeon-architecture-20261101-231800/`，包括 `build-final.log`、`runtime.log`（失败原样保留）、`runtime-final.log`、`reference-audit.json`、`blockbench-verification.json`、`jar-verification.json` 和安装后收据。使用与清单见 `DUNGEON_ARCHITECTURE.md`。

## 0.17.0 验证结果

- 完整离线构建 `BUILD SUCCESSFUL in 1m 9s`：既有骷髅、美术、火盆、门、血池校验与新增原 NBT 校验通过；Java 17，未把测试工具、测试空模板或 Lootr 类内嵌入发行包。`test NO-SOURCE` 不冒充 JUnit 测试。
- Forge `runData` `BUILD SUCCESSFUL in 1m 10s`：真实注册表/属性、维度 Codec、既有门/轮次/血池断言、原两间 StructureTemplate 往返、挡水/道路/地基、非法多调色板/缺方块/重复坐标拒绝及原/逐房 SavedData 序列化通过。
- 隔离 `runGameTestServer` `BUILD SUCCESSFUL in 1m 44s`：实际 `ftdungeonmobs:dungeon` 放置原两间 25,335 条记录、17 个 Lootr BE，逐房新增九间 49,575 条记录，总 74,910 条。九间均通过原生逐格状态与 `canSurvive` 支撑、3×3 北入口/内部干燥步行连通、实际从大厅南口到北入口的九条路、三格净高和楼梯升高。
- 安全负例：完整预留体积、外沿留白液体、道路净空、实体阻挡均拒绝且不标记成功；原两间及逐房生成标记互不覆盖，序列化重载和幂等钻石改建保留通过，延迟 40 tick 无边缘外溢。清理只限 `GameTestServer` 保护的隔离测试区域，未打开玩家存档。
- 早期原生检查真实拒绝 L3 无支撑吊灯与 S3 非空瓶子库存。制作方交付修正版后保持原有检查重测，最终明确 `GALLERY_GAME_TEST_PASS: rooms=9/9, records=49575`，无拒绝；早期失败日志保留，不将失败当成成功。
- 最终 JAR 550520 字节，SHA-256 `a806046dadc63f2b264313a94b490deb0f01fec5126c4a58e17decae04d4b009`。相对 0.16.0 的 221 个既有条目逐字节不变，仅 manifest、mods 描述/版本、两种语言及命令/成功入场类共六个旧条目有允许的改动；九间模板/元数据逐字节等于各制作方最终交付，31/30_1 仍等于原用户文件。原语言键值、维度和其它美术未变。
- 22:48:02 安装到 FarmingTales 实例；22:49:16 独立 `POST_DEPLOYMENT_PASS`：已安装包=构建包哈希，0.16.0 备份与配置验证通过，活动同 ID 包唯一，其余 385 个模组哈希不变，挑战包继续禁用，无 pending/锁残留。运行中的「服务器」实例与远端服未修改。
- 证据：`output/room-import-20261101-211745/` 的 `build-final.log`、`runtime-final-gallery.log`、`gametest-final.log`、`jar-verification.json`、`deployment-receipt.json`、`post-deployment-verification.json`；共享总清单 `D:/ft工程文件/FT地牢_Image25接入_20261101/integration/integration-manifest.json`，固定位置/使用方法见 `ROOM_IMPORT.md`。
- **实机边界**：只证明隔离原生建筑接入，不证明完整整合包多人、保护插件、网络、FPS 或真实玩家返程。既有返程门占用误报 P1 保持未修复；没有新增波次、奖励或改入场/难度逻辑。

## 0.16.0 验证结果

- 离线完整 `BUILD.ps1 -Offline`：`BUILD SUCCESSFUL in 44s`，Java 编译、重混淆 JAR、完整 `check` 通过。四套骷髅 GeckoLib 解析/烘焙、火盆六状态、旧 DSH 资源退役、大门模型/动作均回归通过；保留现有 Gradle 弃用/终端警告。没有新增常规 JUnit 测试，`test NO-SOURCE`；独立 Java 验证器随 `check` 执行。
- `verifyBloodPool` 使用真实 Minecraft `BlockModel.fromStream`：九种模块、七种自定义几何合计 27 个整数长方体/137 面，原版楼梯/台阶父模型、双态材质继承、四向状态、物品、配方唯一性与掉落存在性通过。两张 64×64 图集，16×256 静态血液和 32×512 流动血液各 16 个不重复、不插值的 4 tick 帧；没有曲面、楔形、旋转体块或逐帧几何副本。
- 独立纯 Java 状态机验证：16 点封顶、空手确认对应的显式 activate、重复启动拒绝、200 tick 激活和其后 600 tick 冷却边界、结构中断后的冷却、非法保存值、充能/截止时间重载及新一轮充能通过。
- Forge `runData`：`BUILD SUCCESSFUL in 48s`，真实注册检查血液 FluidType/源/流/桶/液体方块，九种建筑 143 个原生状态及碰撞，44 血液源、24 边界格和四向安全示例蓝图、原生十份配方解析、祭台 NBT 和激活红石 15/平时 0 状态通过。同次原有维度 Codec、2304 门状态、动画/轮次/火盆回归通过。未创建玩家世界。
- 九份新 bbmodel 均由原生 Blockbench 加载检查，根组与主体子组、单贴图、整数 Pivot/体块得到核验；保存祭台、祭像和转角截图。原编辑器未保存工程的完整编译快照及 saved 标记逐字一致，并恢复原选中项目，不关闭其它已有项目。软件整体预览不是 Minecraft 截图，不声称 FPS 或着色器发光测试。
- 0.15.0 → 0.16.0 包比对：176 份源码资源与 JAR 字节一致，旧 86 份美术/绑定及 14 份既有配方/世界生成/掉落等数据不变；唯一旧数据变化是镐标签追加九个条目。旧语言键值全保留，旧 11 份可编辑文件不变，三张参考图哈希不变。129 个旧 src 文件仅六个接入文件有变化；其它旧运行类除主入口/客户端接入类及其行号关联内部类外不变，原门落点逻辑逐字节不变。
- JAR：401104 字节，SHA-256 `a871fc6fe5caa011b3914836b1f404aad17f10b82b36b8f8bb70a3076563b43a`，ZIP CRC、模组 ID/版本、无编辑源/验证器泄露均通过。升级已部署到当前整合包，仅一份同 ID JAR；旧 0.15.0 完整备份，配置及其余 385 个模组文件哈希不变，FTBoss 未改、FT 地牢挑战仍禁用，独立安装复核 `POST_DEPLOYMENT_PASS`。
- 本轮不自动启用波次、奖励、任务或光束着色器；事件触发与红石用于后续显式接入。实际游戏中的液体渲染/流动/装桶、掉落物收集、权限插件兼容、多人交互和红石电路仍待备份世界实测；没有主动启动 Minecraft、修改玩家世界或测量 FPS。
- 证据：`output/sacrifice-blood-pool-20261101-192317/` 的 `build-final.log`、`runtime-final.log`、`blockbench-verification.json`、`jar-verification.json`、`post-deployment-verification.json`；实际规则、配方、搭建图、事件与验收清单见 `BLOOD_POOL.md`，安装/备份路径见 `DEPLOYMENT.md`。
- **既有 P1 未修复**：以下 0.15.0 返程门占用问题在此次血池实现中保持原样，不能把本次资源/注册验证解读为五分钟多人入场已验收。

## 2026-11-01 占用复核发现（尚未修复）

- P1：`DungeonArrival.ensureReturnGate` 将现有返程门完整状态与默认关闭、未运动、tier=0 状态比较；首人进入后的开门/染色会使合法返程门被判为落点占用，阻止后来玩家进入。已核对当前源码及与安装包同 SHA 的字节码，未在真实玩家世界复现；前述资源与数据验证未覆盖该真实世界路径，不能将五分钟多人入场视为已验收。
- 另发现绑定后未锁定阶段最多三次/秒重复控制器扫描，以及运动逐面渲染的临时数组分配。复核未修改运行源码、模型或安装 JAR；详细成本、问题位置和修复建议见 `output/dungeon-gate-occupancy-audit-20261101-181710/REPORT.md`。

## 0.15.0 验证结果

- 离线完整构建 `BUILD SUCCESSFUL in 42s`：编译、重混淆、四套骷髅 GeckoLib 解析/烘焙（4 模型、218 骨骼、17 动画）、两种火盆六状态、DSH 旧资源退役和大门资源检查通过。`test NO-SOURCE`；资源/数据断言不冒充实际联机行为测试。
- Forge `runData` `BUILD SUCCESSFUL in 33s`：真实注册与世界生成 Codec、2304 个门状态、96 次部件/锚点 BE 检查、碰撞与通道、40 tick 动作/反向/保存及 UV 门槛裁剪通过。新增 6000 tick 边界、首人创建与后入不延时、冻结档位、锁定半径/来源、空成员仍需等待截止、离线成员保持、截止时间与成员 UUID 保存重载、新一轮重开断言通过。
- 入口使用 6 格内最近已加载火盆（含熄灭）；普通和潜行交互均循环六档。0 档关闭、1～5 档保持开放，首人成功传送后才冻结难度并计时；到期拒绝新入场，返程不受入场计时限制。正常改档、破坏/替换、爆炸和活塞锁定保护已接入事件；这些实际世界事件仍待联机验收。
- 最终 JAR CRC、105 份源码资源一致性通过；15 份世界生成/配方/掉落/标签逐字节不变，58 份其它美术与绑定逐字节不变。24 固定模型和 2 物品模型除传送面 `tintindex=0` 外 JSON 完全相同，12 共用运动模型逐字节不变；只更改原有两张 16×256 传送动画为 16 帧不透明灰度图。全部火盆（含熄灭）和骷髅美术、5 张参考图未改。
- 入口 182 体块/779 面、返程 181 体块/775 面、共用运动部分 52 体块/221 面及 8 组层级不变；两份可编辑源的几何、组树、Pivot、UV 与 0.14.0 严格比对一致，仅替换内嵌传送首帧。复用前版原生 Blockbench 几何证据，本轮未开启编辑器做颜色截图；软件五色预览不是 Minecraft 截图或 FPS 证据，不声称发光着色器效果。
- 20 份既有运行 class 逐字节不变，仅修改门、火盆交互/难度 API、注册和客户端颜色绑定相关类，新增一个 `DungeonGatePalette` 类；没有打包验证工具、编辑工程或旧 DSH 素材。候选 322800 字节，SHA-256：`08dd26642400f78edf7b76a5384e3106d24ee1f95908ec6abc9b91700b9bba18`。
- 本轮证据目录：`output/dungeon-gate-rounds-20261101-172859/`，包括改前副本、完整构建/最终 Forge 日志、五色软件示意、可复跑 JAR/安装后核验脚本与收据。`build/reports/dungeon-gates-runtime.txt` 记录无世界启动的数据验证；部署状态另见 `DEPLOYMENT.md`。
- 未启动 Minecraft 客户端或服务器世界，未修改玩家世界/配置。两名玩家的真实传送、截止关门与防夹、在线/离线/死亡返程、保护事件和网络染色仍须实机验收；具体清单见 `DUNGEON_GATES.md`。6000 游戏 tick 在 20 TPS 下为五分钟，停服或暂停不消耗计时，低 TPS 下实际时间会增长。

## 历史：0.14.0（已被 0.15.0 替代）

## 0.14.0 验证结果

- 离线完整构建 `BUILD SUCCESSFUL in 24s`，包括重混淆、全部四套骷髅 GeckoLib 解析/烘焙（4 模型、218 骨骼、17 动画）、火盆六状态/五档焰色、旧 DSH 素材退役及大门资源检查；`test NO-SOURCE`，不把资源断言冒充完整游戏行为测试。
- 原生 `BlockModel` 检查 24 个固定部件、12 个共用运动部件、2 个整数微缩物品。入口 182 体块/779 面，返程 181 体块/775 面；共用铁闸 52 体块/221 面，每门不超过 220 体块/900 面。固定与运动几何分开剔除隐藏面，开门不会露出原来被铁闸挡住的缺面。384 个朝向/部件/open/moving 状态逐一验证静态闸仅在关门静止时存在。
- 纯运动逻辑验证 40 tick 完整开/关、partial tick 连续进度、端点钳制、中途反向、保存进度续播与门槛 Y=3/16 裁剪；验证 UV 插值、不可修改共用烘焙顶点、完全沉没的面不再输出。没有地下机井或世界地面编辑，也没有压力板功能。
- Forge `runData` `BUILD SUCCESSFUL in 34s`：真实模组/方块实体注册、384 原生状态、96 次部件/锚点实体创建检查、仅锚点创建 BE、两种门共用注册类型、中途 NBT 与原生更新包、四朝向闭门阻挡/开门通道/点击坐标及自动传送范围变换通过；保留真实世界生成 Codec、SavedData 1～5 档及非法返回 NBT 回归。仅隔离数据环境，无客户端或服务器世界启动，无 EULA 操作。
- 整合独立火盆熄灭态：默认 tier=0、六状态、0/14 级光照、32 体块/161 面无火焰静态模型、旧 half/tier 十二状态与熄灭不参与难度来源选择均通过 Forge 校验；原五档点燃几何、材质和动画不变。普通右键门旁火盆控制门、空手潜行右键调档，避免开门误改档。
- Blockbench 打开两份最终源：182 / 181 个整数 Cuboid、8 组、3 张内嵌首帧纹理，可动组各 52 体块，导轨留在固定组；保存关闭与全开可见性姿态原生截图并恢复源姿态。原选中的未保存熄灭火盆工程编译快照和标记保持严格一致并恢复选中。软件 GIF 仅为纹理/轨迹示意，不是游戏渲染或 FPS 证据。
- 最终 JAR ZIP CRC 通过，105 个源码运行资源与打包内容一致；15 个 data 资源、37 个既有美术资源（含骷髅、点燃火盆、门纹理及门物品）与 0.13.1 逐字节一致，18 个既有 class 不变，变更 class 限于门、注册/客户端及火盆交互/熄灭来源；无原文件删除。原五张参考图哈希未改。JAR 不包含 bbmodel、验证类或旧 DSH 几何/贴图/渲染器。
- 版本 `0.14.0`，313313 字节，SHA-256 `0a893f7cfd5daa2739598a54164bf18ce035267eb60238c49d767aacf1222d05`。部署前基线与配置校验、模组 ID 扫描及干跑通过；旧包备份后安装，活动本模组一份，385 个其它模组文件与配置哈希不变。证据在 `output/dungeon-gate-animation-20261101-163724/`；安装细节见 `DEPLOYMENT.md`。
- 待完整整合包实机：一次交互开门/再次关闭、四向真实渲染、门旁调档/点燃、关门防夹与中途反开、步入/右键往返、区块卸载/重启续播、多客户端一致性及原有死亡/重登/取消事件/多人返回点规则；见 `DUNGEON_GATES.md`。本轮没有自行启动游戏，因此不宣称以上实机操作或 FPS 已验证。

## 历史：0.13.1 验证结果

- 原型依据为用户指定目录中的五张图片；记录图片大小和 SHA-256，安装后再次确认原文件未改。只提取石砖、骷髅、绞盘、铁链、铁闸、暖焰与冷雾等视觉特征，没有裁用参考图像素，不声称完整复原隐藏机构或下沉动画。
- 独立候选先完成软件正背左右与斜视核对，再安装到运行资源；Blockbench 分别重新打开最终两个 bbmodel，实际读得入口 182 体块/8 组/3 纹理、返程 181 体块/8 组/3 纹理。实际编辑器截图及拼图在 `preview/blockbench-entry.png`、`blockbench-return.png`、`blockbench-gates-comparison.png`，不是 Minecraft 游戏截图。原未保存火盆工程编译快照严格相同并恢复选中，未关闭或覆盖其它已有项目。
- 原生 Minecraft 1.20.1 `BlockModel` 解析全部 24 个部件和 2 件整数微缩物品；入口 182 体块/759 面，返程 181 体块/755 面，物品 106 / 105 体块；均在既定每门 220 切分体块/900 面预算内。所有坐标、尺寸、Pivot 为整数，只有正体积长方体、没有元素旋转，96 个朝向/部件状态与 0.13.0 语义完全一致。
- 共同石砖/锈铁/骨饰图集 128×128；两种冷雾及共用暖焰均为 16×256、16 个不同且不透明的帧、3 tick/帧、无插值。逐面纹理引用均存在，UV 在原版 0～16 范围内，图集 UV 对齐源像素。编辑源可见面数与导出一致；完整遮蔽面共剔除入口 333 / 返程 331 个，保留真实链孔。
- 离线完整构建 `BUILD SUCCESSFUL in 57s`：重混淆、旧 DSH 素材退役校验、血棘祭炉五档颜色/43 体块/217 面回归、真实 GeckoLib 4 模型/218 骨骼/17 动画解析烘焙均通过。`test NO-SOURCE`，不将资源验证冒充实际游戏行为测试。
- Forge `runData` 回归 `BUILD SUCCESSFUL in 1m 5s`：模组与大门注册、实际 Biome/DimensionType/LevelStem Codec、64 层地层/256 高度、48 种部件坐标逆变换、1～5 档 SavedData 往返及非法返回 NBT 拒绝检查全部通过；未启动服务器世界、玩家客户端或接受 EULA。
- 源码保留性：所有既有 Java 源文件、15 个 data 资源、39 个非大门 assets 均保持不变；新旧最终包 28 个运行 class 逐字节一致，证明本轮未改传送、返回和火盆难度代码。无原有源文件删除，未更动骷髅和火盆美术。
- 最终包 ZIP CRC 通过，91 个源码运行资源与 JAR 一致；无 bbmodel、工具验证类或退役 DSH 几何/贴图/渲染器。版本 `0.13.1`，285569 字节，SHA-256 `40aea6b139a516deb02cd45b8eb4e627d269a811ff76148b3f1a59493f5cdfa8`。
- 部署及独立复核通过：旧包完整备份、活动本模组仅一份、配置与 385 个其它模组文件不变，禁用挑战扩展保持禁用，无待安装副本和锁残留。证据在 `output/dungeon-gate-reference-20261101-152422/`；完整整合包客户端观感、光照、交互和联机仍待实机验收，无 FPS 或显存实测。

## 历史：0.13.0 验证结果

- 离线 `BUILD.ps1 -Offline`：`BUILD SUCCESSFUL in 19s`；Java 编译、重混淆 JAR 与全部资源检查通过。工程仍为 `test NO-SOURCE`，不把资源校验当作完整行为测试。
- 原生 Minecraft `BlockModel`：入口 68 个整数长方体/267 面，返程 66 体块/261 面；24 个部件模型、每门 12 部件×4 朝向，共 96 状态。两件物品分别为 33 体块整数微缩模型，均符合 Java 原版坐标范围；没有曲面、斜切或零厚度面片。
- 两扇门共用 64×64 本体贴图，各自能量贴图 16×256、16 帧、每帧 3 tick、全不透明无插值；对应方块、物品、配方、掉落、中英文名称均存在。可编辑 bbmodel 有 5 组整数 Pivot，不打包到运行 JAR；软件示意图不冒充游戏截图或性能测量。
- `BUILD.ps1 -Task runData -Offline`：Forge 47.4.10 实际模组加载和方块注册通过；真实 `Biome.DIRECT_CODEC`、`DimensionType.DIRECT_CODEC`、`LevelStem.CODEC` 在完整原版/自定义注册查找下成功读取三份维度数据。确认 FlatLevelSource 64 层、维度高 256、无天空光、床禁用。
- 同一 Forge 数据校验中检查了 48 种部件/朝向的坐标逆变换、难度 1～5 与来源入口/大厅标记的 SavedData 序列化往返、越界档位降为 1，以及缺失、NaN、超界返回坐标的拒绝。验证入口只在独立开发 SourceSet 中加载，JAR 不包含验证类；没有客户端或服务器世界启动，没有接受 EULA。
- 骷髅回归：实际 GeckoLib 4.8.4 解析/烘焙 `4 models, 218 bones, 17 animations`；祭炉回归 `43 integer cuboids, 217 faces`、五档焰色/16 帧循环均通过；旧 DSH 资源退役检查保持通过。
- 源码保持性：改前文件无删除，只变更计划内 9 个既有 Java/元数据/语言/镐标签文件；新旧包 37 个既有非语言美术资源逐字节一致，包含原血棘祭炉和全部骷髅美术。
- 最终 JAR：ZIP CRC 通过，89 个源码运行资源全部与打包内容一致，24 个新部件模型及三份维度数据均存在；没有旧 DSH 模型/渲染器、bbmodel 或工具验证类。模组 ID `ftdungeonmobs`、版本 `0.13.0`，260860 字节，SHA-256 `0817a621281e3b3f81250c2784d444f3fd5629817c06fd2daf1a35a914511fef`。
- 本机部署及独立复核 `POST_DEPLOYMENT_PASS`：旧包备份完整、活动本模组仅一份、配置及其余 385 个模组文件不变，FT 地牢挑战仍禁用，无待安装副本或锁残留。
- 待实机验收：四向放置/整门清理掉落、跨维度传送、死亡/重登返回点、安全落点、多人入口锁定、火盆调档后的实体属性刷新，以及完整整合包资源加载与光照。源码路径审查和以上数据校验不等于这些行为已经实测。
- 证据目录 `output/dungeon-gates-20261101-144504/`；操作与检查清单见 `DUNGEON_GATES.md`，安装和降级风险见 `DEPLOYMENT.md` 当前版本章节。

## 历史：0.12.0 验证结果

- 清理已核实路径的工程 `build/` 后重新完整构建，防止已删除类/资源残留：`clean` 成功，离线 `build` 显示 `BUILD SUCCESSFUL in 22s`，`check` 与 `reobfJar` 通过。无单元测试源码；不把资源/源码断言冒充游戏行为测试。
- `BLOODTHORN_ASSETS_VERIFIED`：真实 Minecraft 1.20.1 `BlockModel` 解析基模与四个继承模型，解析父模型与材质后均为 43 个长方体、217 面，11 个不受逐面方向阴影影响的火焰体块；五档只覆盖火焰材质，盆体不染色，几何无复制。
- 五档焰色为苍蓝、翠绿、鎏金、紫焰、猩红；每档 16×256、16 帧、2 tick/帧、无插值，逐像素 HSL 明度与图案布局保持一致。四张新贴图从祭炉自有贴图调色，第五档原猩红图逐字节保留；原 64×64 本体、几何、UV、Display 亦逐字节不变。
- `ARENA_RETIREMENT_VERIFIED`：旧 DSH geo、动画、六张贴图、三个渲染类、活动 bbmodel 源、生成工具已移除活动工程；旧配方与创造栏入口取消。旧方块十种 half/tier 状态仅绑定新祭炉或无几何上半部；旧物品摆放/旧下半部掉落转为新祭炉，BE 仅为惰性兼容壳。
- 骷髅回归：真实 GeckoLib 4.8.4 解析/烘焙 `4 models, 218 bones, 17 animations` 通过；16 个既有骷髅美术资源与安装前 JAR 字节一致，23 个骷髅相关源码/资源文件与改前基线一致。
- 对最终 JAR 检查：ZIP CRC 通过、模组 ID `ftdungeonmobs` / 版本 `0.12.0`，47 个运行资源全部与源码一致；不存在旧 `arena_brazier`、`arena_torch_body` 或三个旧渲染 class，也不含 bbmodel 编辑源。占位 `arena_torch` 文件仅为旧存档兼容，不含旧模型几何。
- JAR 大小 207785 字节，SHA-256 `fac3e79adc1cefbfecc77b5ca54fbfacc6c326fa65cc1a79e0d43a6ff88568a2`；备份与安装均核对哈希，活动本模组仅一份，385 个其它模组文件与原配置未变。
- 静态焰色预览 `output/bloodthorn-tier-colors-20261101-120156/preview/tier-flame-palette.png` 已目视检查；同目录 GIF 展示五组完整纹理循环。这些是纹理预览，不宣称游戏截图或 FPS 测试。
- 未启动 Minecraft 客户端或服务器；档位同步、旧存档加载、交互和最终光照仍须按 `BLOODTHORN_BRAZIER.md` 实机检查。构建保留既有 API 弃用警告，不修改无关代码。
- 完整证据目录：`output/bloodthorn-tier-colors-20261101-120156/`；部署详情与旧包路径见 `DEPLOYMENT.md` 当前版本章节。

## 历史：0.11.0 验证结果

- 离线 `BUILD.ps1 -Offline`：`BUILD SUCCESSFUL in 30s`，Java 编译、重混淆 JAR、完整 `check` 均通过。保留现有 API 弃用警告，未修改无关代码；`test NO-SOURCE`，不将资源验证冒充游戏行为测试。
- `verifyBloodthornAssets` 使用真实 Minecraft 1.20.1 `BlockModel.fromStream`：43 个长方体、217 面、11 个 `shade:false` 血焰体块，全部几何坐标/尺寸/Pivot 为 0～16 内整数、旋转为 0，逐面 UV 对齐像素；5 个 tier 状态均绑定新模型，物品父模型解析通过。
- 新贴图：本体 64×64、火焰 16×256，全不透明；16 帧连续编号、每帧 2 tick、无插值；来源与接入副本逐字节哈希一致。新模型没有 `ftboss` 依赖或编辑器私有 `groups/texture_size` 字段。
- 新配方、单格掉落表、两种火盆的镐挖掘标签、中英文名称/提示绑定检查通过；8 个新运行资源均打包且与源码一致，可编辑 bbmodel 未打入运行 JAR。
- 旧火炬回归：`ARENA_ASSETS_V8_VERIFIED`，真实 GeckoLib 4.8.4 解析/烘焙 16 bones / 59 cubes / 354 faces、2 秒循环动画、5 档贴图、10 个状态与 BE/渲染器注册均通过。
- 骷髅回归：`VERIFIED: 4 models, 218 bones, 17 animations`，真实 GeckoLib 解析与烘焙通过。
- 保留性：改前全部 `src` 文件建立哈希基线，只变更计划内的 7 个既有 Java/语言/元数据/标签文件，未删除旧文件；新旧 JAR 中 27 个旧非语言美术资源逐字节一致，第一种火盆及骷髅模型/贴图/动画均未覆盖。
- JAR：ZIP CRC 全部通过；模组 ID `ftdungeonmobs`、版本 `0.11.0`，219353 字节，SHA-256 `556c304bb9d3ec891582db1d320c3febaa4f9034d096027e32fe419dcb76f7d6`。
- 部署：旧包完整备份、安装包与候选哈希一致；活动同 ID 包仅 1 个，配置不变，385 个其它模组文件哈希不变；独立安装后复核为 `POST_DEPLOYMENT_PASS`。部署脚本本身成功，外层命令误用 PowerShell 的 `LASTEXITCODE` 曾返回非零；独立复核确认文件安装与备份正确，无二次重复部署。
- 本次没有启动完整整合包客户端或修改世界；资源解析、构建和部署核验不能证明实机画面、网络同步或右键/存档交互已验收。实机检查清单见 `BLOODTHORN_BRAZIER.md`。
- 证据：`output/bloodthorn-integration-20261101-111539/build.log`、`source-preservation.json`、`jar-verification.json`、`deployment-receipt.json`、`post-deployment-verification.json`；部署与回退见 `DEPLOYMENT.md` 当前版本章节。

## 历史验证记录

## 0.10.0 火炬 v8（GeckoLib 骨骼动画 + 2 格 BaseEntityBlock + BE）验证

- **构建**：离线 Gradle 8.8 + JDK17 `BUILD SUCCESSFUL in 21s`；完整 `check`（`verifyAssets` + `verifyArenaAssets`）+ `reobfJar` 通过。日志：`build/reports/build-verification-0.10.0.log`。
- **真实 GeckoLib 4.8.4 解析/烘焙（v8 验证器）**：
  `ARENA_ASSETS_V8_VERIFIED: GeckoLib 4.8.4 parsed/baked geo (bones=16, cubes=59, faces=354, flameBones=2, integerViolations=0); animation=animation.arena_brazier.idle (loop=true, length=2.0, bones=10); textures body 64x64 + tierBodies t1..t5; blockstates=10 variants (placeholder elements=0); item 59 elements with display; legacy v7 static resources cleaned; java=17 files, block/be/renderer/gecko/registered=true`
  —— geo(format 1.12.0/整数网格/火焰 UV (48,48))、animation(idle loop 2.0s)、body_t1..t5(64²)、blockstates 10 variants→占位、静态 item(59 元素/display 7/无 texture_size)、旧静态 17 项清理、Java BE/Renderer/注册 均 PASS。
- **方块/渲染形态**：2 格 BaseEntityBlock + BE（lower）+ GeckoLib 渲染；item 静态模型；GeckoLib 4.8.4（依赖既有）。
- **Java diff 白名单**：改 `ArenaTorchBlock.java`/`FTDungeonMobs.java`/`client/DungeonClient.java`；新 `ArenaTorchBlockEntity.java`/`client/ArenaTorchModel.java`/`client/ArenaTorchRenderer.java`/`client/ArenaTorchEmissiveLayer.java`（7 文件；其余 12 Java 未动）。
- **验证器接线**：`build.gradle` 的 `verifyArenaAssets` 升级为 GeckoLib 验证器（`compileAssetVerifier + extractVerifierLibraries + asset-verifier-libs`，需 GeckoLib jarjar 库 mclib 等）。
- **骷髅线回归**：`verifyAssets` → `VERIFIED: 4 models, 218 bones, 17 animations`（字节不变）。
- **staging 预演**：v8 资源+Java+v8 验证器 `BUILD SUCCESSFUL in 19s`。
- **JAR**：`ft-dungeon-mobs-forge-1.20.1-0.10.0.jar` **205283 字节**，SHA-256 `42c688bc…`，`Implementation-Version: 0.10.0`；arena 资源 11 项（geo/anim/blockstates/占位/item/6 body 纹理）；无 v7 旧静态 17 项残留；Java BE/Renderer/Model/EmissiveLayer 类存在。
- **部署**：已安装 0.10.0（SHA 与构建一致）；本模组仅一版；**0.9.0 jar `4bfb43d0…`+配置备份于 `ft-mod-backups/ftdungeonmobs/20261101013203`**；0.8.0/0.7.0/0.6.0/0.5.0/0.4.0 备份仍在；mods 可见 373→373 不变；配置 `46da1c10…` 前后一致；存档未动。回执：`build/reports/deployment-0.10.0.json`。
- **已知限制/P2**：未启动完整整合包客户端（idle 动画/渲染观感、火焰 tile 换色、EmissiveLayer 发光层、BE 性能、item GUI/手持、旧存档首次加载均未实机验证）；P2 候选：渲染/动画观感、BE 性能、细节可再增。
- **详细证据**：`work/v8/integration/INTEGRATION_REPORT.md`、`DEPLOY_PLAN.md`、`logs/build-0.10.0.log`、`work/v8/be/verify/`（geckolib 独立验证器）、`previews/v8/`（如已生成）。

# 历史验证 · 0.9.0（火炬 v7 新火盆 / 两格高静态方块模型，已被 0.10.0 取代）· 2026-11-01

## 0.9.0 火炬 v7（静态方块模型 + 贴图动画火焰）验证（历史）

- **构建**：离线 Gradle 8.8 + JDK17 `BUILD SUCCESSFUL in 26s`；完整 `check`（`verifyAssets` + `verifyArenaAssets`）+ `reobfJar` 通过。日志：`build/reports/build-verification-0.9.0.log`。
- **真实 Minecraft 1.20.1 解析（v7 静态验证器）**：
  `V7_BUDGET: flameFaces=8/8/8/8/8; T1..T5 edit=87el/432f world=91el/448f`
  `ARENA_ASSETS_VERIFIED (v7 static)` —— 8 个 Java 模型全部 `BlockModel.fromStream` 解析；10 variants；lower 0 火焰面；upper 每档 8/8/8/8/8；**两格高校验通过（upper 局部 y≤16/世界 32，实际 localYmax 12.60→15.80）**；**编辑口径 87 元素/432 面**（≤96/≤600）；world 91/448（≤112）；64×64 body（sha `e188fe0b…`）；5×16×128 8 帧+mcmeta；Forge 全亮；item 87/432/display 7/ymax 28.60；UV 0–16；旋转白名单含 ±22.5。
- **结构统计**：lower 40/190；upper 51/258；item 87/432；跨 y=16 元素 4；遮挡剔除 0 面。
- **Java 小改**：仅 `ArenaTorchBlock.animateTick` 粒子高度（`position.getY()+0.3..1.0`，仅 UPPER 半部 = lower +1.3..2.0 格 = 世界 +20.8..+31.8 像素），与 0.8.0 diff 1 行；HALF/放置/破坏/右键/难度/配方/逻辑不变（QA 可 diff）。
- **骷髅线回归**：`verifyAssets` → `VERIFIED: 4 models, 218 bones, 17 animations`（GeckoLib 字节不变）。
- **staging 预演**：最终 runtime 完整 `build` → `BUILD SUCCESSFUL in 17s`，两项验证 PASS（`work/v7/integration/logs-staging-build-v7-0856.log`）。
- **JAR**：`ft-dungeon-mobs-forge-1.20.1-0.9.0.jar` **204787 字节**，SHA-256 `4bfb43d0…`，`Implementation-Version: 0.9.0`；arena 恰 20 条目；无 v6/v5/v4/v3/v2 残留与第三方参考素材；body sha `e188fe0b…`；item 87/432 ymax28.6。
- **参考来源/原创声明**：造型参考 Cataclysm Altar of Fire / 用户 gpt 解包模型风格（风格/构图参考）；几何与贴图原创、未复制第三方文件；**参考作品许可未核实**（内部/个人使用，公开分发前需确认）。
- **部署**：已安装 0.9.0（SHA 与构建一致）；本模组仅一版；**0.8.0 jar `1e33e8a2…`+配置备份于 `ft-mod-backups/ftdungeonmobs/20261101010040`**；0.7.0/0.6.0/0.5.0/0.4.0 备份仍在；mods 可见 373→373 不变（含外部工作线独立模组）；配置 `46da1c10…` 前后一致；存档未动。回执：`build/reports/deployment-0.9.0.json`。
- **已知限制/P2**：未启动完整整合包客户端（新火盆观感/比例/粒子高度/性能/多人联机未实机验证）；P2 候选：细节可再增、火焰 core 偏白、top/bottom 渲染覆盖率、新造型比例、许可核实（critic 正式评审 PASS，无 P0/P1）。
- **详细证据**：`work/v7/integration/INTEGRATION_REPORT.md`、`DEPLOY_PLAN.md`、`logs/build-0.9.0.log`、`work/v7/review/design-review-v7.md`（critic PASS）、`previews/v7/`（如已生成）。

# 历史验证 · 0.8.0（火炬 v6 视觉三格高 / 粗矛对角托举，已被 0.9.0 取代）· 2026-11-01

## 0.8.0 火炬 v6（静态方块模型 + 贴图动画火焰）验证（历史）

- **构建**：离线 Gradle 8.8 + JDK17 `BUILD SUCCESSFUL in 16s`；完整 `check`（`verifyAssets` + `verifyArenaAssets`）+ `reobfJar` 通过。日志：`build/reports/build-verification-0.8.0.log`。
- **真实 Minecraft 1.20.1 解析（v6 静态验证器）**：
  `V6_BUDGET: flameFaces=8/8/8/8/8; T1..T5 edit=53el/275f world=59el/299f`
  `ARENA_ASSETS_VERIFIED (v6 static)` —— 8 个 Java 模型全部 `BlockModel.fromStream` 解析；10 variants；lower 0 火焰面；upper 每档火焰面 8/8/8/8/8；**upper 局部 y≤32（世界 16..48）通过**；**编辑口径 53 元素/275 面**（≤56/≤380 ✓）；world 59/299（≤80 ✓）；64×64 body（sha `34d15233…`）；5×16×128 8 帧+mcmeta；Forge 全亮；item 53/275/display 7；UV 0–16；旋转白名单含 ±22.5。
- **三格高口径**：视觉世界高度 y0..48（3 格）；lower y0..16、upper 局部 y0..32 = 世界 16..48；跨 y=16 拆分元素 6 个；**方块碰撞/占用仍为 lower+upper 两格（第 3 格仅视觉）**。
- **Java 小改**：仅 `ArenaTorchBlock.animateTick` 火焰粒子高度（世界 +34..48；UPPER 半部调用），与 0.7.0 diff 1 行；HALF/放置/破坏/右键/难度/配方/掉率/数据不变（QA 可 diff）。
- **item 模型修正**：原版 BlockElement 坐标上限 [-16,32]，item 几何 y0..44.6 无法解析；等比缩放 0.715247（x/y/z 均匀，x/z 绕中心、y 绕底部，y≤31.9）+ display.scale ×1.398119（7 槽），元素/面数不变 53/275；原始备份 `work/v6/integration/fixes/arena_torch.item.original.json`。
- **骷髅线回归**：`verifyAssets` → `VERIFIED: 4 models, 218 bones, 17 animations`（GeckoLib 字节不变）。
- **staging 预演**：最终 runtime + 修正 item 完整 `build` → `BUILD SUCCESSFUL in 15s`，两项验证 PASS（`work/v6/integration/logs-staging-build-v6final2.log`；初次失败日志 `logs-staging-build-v6.log` 留档）。
- **JAR**：`ft-dungeon-mobs-forge-1.20.1-0.8.0.jar` **201959 字节**，SHA-256 `1e33e8a2…`，`Implementation-Version: 0.8.0`；arena 20 条目；无 v5/v4/v3/v2 残留；item 53/275（y0..31.9）。
- **部署**：已安装 0.8.0（SHA 与构建一致）；本模组仅一版；**0.7.0 jar `4b4fdbd3…`+配置备份于 `ft-mod-backups/ftdungeonmobs/20261101003038`**，0.6.0/0.5.0/0.4.0 备份仍在；mods 可见 373→373 不变（含外部工作线独立模组）；配置 `46da1c10…` 前后一致；存档未动。回执：`build/reports/deployment-0.8.0.json`。
- **已知限制/ P2**：未启动完整整合包客户端（三格高观感/粒子高度/性能/多人联机未实机验证）；视觉 3 格但占 2 格的遮挡需实机评估；P2 候选：面数 275 略低、火焰 core 偏白、top/bottom 渲染覆盖率、骨堆偏小（critic R2 PASS，无 P0/P1）。
- **详细证据**：`work/v6/integration/INTEGRATION_REPORT.md`、`DEPLOY_PLAN.md`、`logs/build-0.8.0.log`、`work/v6/review/design-review-v6.md`（critic R2 PASS）、`previews/v6/`（如已生成）。

# 历史验证 · 0.7.0（火炬 v5 细长矛束支撑 / 血腥基座，已被 0.8.0 取代）· 2026-11-01

## 0.7.0 火炬 v5（静态方块模型 + 贴图动画火焰）验证（历史）

- **构建**：离线 Gradle 8.8 + JDK17 `BUILD SUCCESSFUL in 22s`；完整 `check`（`verifyAssets` + `verifyArenaAssets`）+ `reobfJar` 通过。日志：`build/reports/build-verification-0.7.0.log`。
- **真实 Minecraft 1.20.1 解析（v5 静态验证器）**：
  `V5_BUDGET: flameFaces=8/8/8/8/8; T1..T5 edit=48el/249f world=58el/289f`
  `ARENA_ASSETS_VERIFIED (v5 static)` —— 8 个 Java 模型全部 `BlockModel.fromStream` 解析；blockstates=10 variants；**lower 0 火焰面**；**每档 upper 火焰面 8/8/8/8/8**（4 平面 0/90 正交）；**编辑口径预算 48 元素/249 面**（v5 预算 ≤56/≤340 ✓）；`arena_torch_body.png` **64×64**（sha `34d15233…`，TEX-5 血腥基座）；五档 `arena_flame_1..5.png` **16×128（8 帧×16×16）+ mcmeta frametime=2, interpolate=false**（sha 沿用 v4 最终版）；火焰面 Forge 全亮；item 模型 **48 元素/249 面/display 7**；UV 0–16。
- **结构统计**：lower 24 元素/124 面；upper 34 元素/165 面；item 48 元素/249 面；world 口径 58 元素/289 面（≤68 ✓）。
- **结构/穿模自检（Lead/critic 工具）**：`support` PASS（细杆 6、粗柱 0）、`clip` PASS（0 非有意穿透）、`spec` PASS、`blood` PASS；`check_overlap.py` structural 0 对（flame-flame 层叠 6 对/档为有意叠加）。
- **骷髅线回归**：`verifyAssets` → `VERIFIED: 4 models, 218 bones, 17 animations`（GeckoLib 资源字节未变）。
- **staging 预演**：同最终 runtime + 同验证器完整 `build` → `BUILD SUCCESSFUL in 18s`，两项验证 PASS（`work/v5/integration/logs-staging-build-v5final-0807.log`）。
- **JAR**：`ft-dungeon-mobs-forge-1.20.1-0.7.0.jar` **201317 字节**，SHA-256 `4b4fdbd3…`，`Implementation-Version: 0.7.0`；arena 资源 20 条目；**无 v4/v3/v2 残留**（geo/animations/128²/glowmask/particle/BE 类/`_glowing` 均为 0）。
- **部署**：已安装 `FarmingTales_ForgeⅡ v1.5.2/mods/FT地牢怪物-Forge-1.20.1-0.7.0.jar`（SHA 与构建一致）；本模组仅一版；**0.6.0 jar `9e9b01ed…`+配置备份于 `ft-mod-backups/ftdungeonmobs/20261101000936`**，0.5.0（`.../20261031234809`）、0.4.0（`.../20261031162146`）备份仍在；mods 可见文件 373→373 不变（含外部工作线独立模组，非本模组）；配置 `46da1c10…` 前后一致；存档未动。回执：`build/reports/deployment-0.7.0.json`。
- **已知 P2（0.7.1 候选，不影响本版验收）**：面数 249 略低、火焰 core 偏白、top/bottom 视角渲染覆盖率偏低、底部骨堆偏小（critic 终评 PASS，无 P0/P1）。
- **已知限制（如实）**：未启动完整整合包客户端（细矛支撑/血腥基座/大银矛头观感、性能、多人联机未实机验证）。
- **详细证据**：`work/v5/integration/INTEGRATION_REPORT.md`、`work/v5/integration/DEPLOY_PLAN.md`、`work/v5/integration/logs/build-0.7.0.log`、`work/v5/review/design-review-v5.md`（critic PASS）、交付包 `previews/v5/`（如已生成）。

# 历史验证 · 0.6.0（火炬 v4 第一版精度重置版，已被 0.7.0 取代）· 2026-11-01

## 0.6.0 火炬 v4（第一版精度重置 · 静态方块模型 + 贴图动画火焰）验证（历史）

- **构建**：离线 Gradle 8.8 + JDK17 `BUILD SUCCESSFUL in 22s`；完整 `check`（`verifyAssets` + `verifyArenaAssets`）+ `reobfJar` 通过。日志：`build/reports/build-verification-0.6.0.log`。
- **真实 Minecraft 1.20.1 解析（v4 静态验证器）**：
  `V4_BUDGET: flameFaces=8/8/8/8/8; T1..T5 edit=52el/269f world=62el/307f`
  `ARENA_ASSETS_VERIFIED (v4 static)` —— 8 个 Java 模型全部 `BlockModel.fromStream` 解析；blockstates=10 variants；**lower 0 火焰面**；**每档 upper 火焰面 8/8/8/8/8**（4 平面 0/90 正交交叉）；**编辑口径预算 52 元素/269 面**（v4 预算 ≤52/≤280 ✓）；`arena_torch_body.png` **64×64**（sha `c2c14dc1…`）；五档 `arena_flame_1..5.png` **16×128（8 帧×16×16）+ mcmeta frametime=2, interpolate=false**；火焰面 Forge 全亮；item 模型 **52 元素/269 面/display 7**；UV 0–16。
- **结构统计**：lower 24 元素/124 面；upper 38 元素/183 面；item 52 元素/269 面；编辑口径 52/269（长矛交错、基座细节、底部骷髅头/骨堆/断箭回归）。
- **骷髅线回归**：`verifyAssets` → `VERIFIED: 4 models, 218 bones, 17 animations`（GeckoLib 资源字节未变）。
- **staging 预演**：同 runtime + 同验证器完整 `build` → `BUILD SUCCESSFUL in 20s`，两项验证 PASS（`work/v4/integration/logs-staging-build-v4flamefinal.log`）。
- **JAR**：`ft-dungeon-mobs-forge-1.20.1-0.6.0.jar` **203393 字节**，SHA-256 `9e9b01ed…`，`Implementation-Version: 0.6.0`；arena 资源 20 条目；**无 v2/v3 残留**（geo/animations/128²/glowmask/particle/BE 类/`_glowing` 均为 0）。
- **部署**：已安装 `FarmingTales_ForgeⅡ v1.5.2/mods/FT地牢怪物-Forge-1.20.1-0.6.0.jar`（SHA 与构建一致）；本模组仅一版；**0.5.0 jar `877d795f…`+配置备份于 `ft-mod-backups/ftdungeonmobs/20261031234809`**，0.4.0 jar `f8f5c52a…`+配置备份于 `.../20261031162146`；mods 可见文件 373→373 不变（含外部工作线新增的其他模组，非本模组）；配置 `46da1c10…` 前后一致；存档未动。回执：`build/reports/deployment-0.6.0.json`。
- **已知限制（如实）**：未启动完整整合包客户端（长矛交错/骷髅头/贴图 8 帧火焰观感、性能、多人联机未实机验证）；未实机 FPS/画面验收。
- **详细证据**：`work/v4/integration/INTEGRATION_REPORT.md`、`work/v4/integration/DEPLOY_PLAN.md`、`work/v4/integration/logs/build-0.6.0.log`、`work/v4/review/design-review-v4.md`（critic PASS）、交付包 `previews/v4/`（如已生成）。

# 历史验证 · 0.5.0（火炬 v3 原版风简化版，已被 0.6.0 取代）· 2026-11-01

## 0.5.0 火炬 v3（静态方块模型 + 贴图动画火焰）验证（历史）

- **构建**：离线 Gradle 8.8 + JDK17 `BUILD SUCCESSFUL in 21s`；完整 `check`（`verifyAssets` + `verifyArenaAssets`）+ `reobfJar` 通过。日志：`build/reports/build-verification-0.5.0.log`。
- **真实 Minecraft 1.20.1 解析（v3 静态验证器，`tools/VerifyArenaAssets.java`，无 GeckoLib 依赖）**：
  `ARENA_ASSETS_VERIFIED (v3 static)` —— 8 个 Java 模型全部 `BlockModel.fromStream` 解析；blockstates=10 variants；**lower 0 火焰面**；**每档 upper 火焰面 8/8/8/8/8**（4 平面 ×2 面，0/90 正交交叉）；火焰最高点逐档严格递增；`arena_torch_body.png` **64×64**；五档 `arena_flame_1..5.png` **16×128（8 帧×16×16）+ mcmeta frametime=2, interpolate=false**；火焰面 Forge 全亮（block_light=15/sky_light=15/ambient_occlusion=false）；item 模型 display；UV 0–16。
- **结构统计**：lower 13 元素/56 面；upper 33 元素/157 面；item 40 元素/196 面；lower+upper 最大 **213 面**；每档火焰面统一 8（0/90 正交交叉）。
- **骷髅线回归**：`verifyAssets` → `VERIFIED: 4 models, 218 bones, 17 animations`（GeckoLib 渲染仍服务四套骷髅，未受影响）。
- **staging 预演**：同 runtime 在工程副本 staging 完整 `build` → `BUILD SUCCESSFUL in 17s`，两项验证 PASS（`work/v3/integration/logs-staging-build-final.log`）。
- **JAR**：`ft-dungeon-mobs-forge-1.20.1-0.5.0.jar` **198646 字节**，`Implementation-Version: 0.5.0`；arena 资源 20 条目（blockstate/7 模型/item/64² body/5×16×128 火焰+5 mcmeta）；**无 v2 geo/animations/128²/glowmask/particle/BE 类残留**。
- **部署**：已安装 `FarmingTales_ForgeⅡ v1.5.2/mods/FT地牢怪物-Forge-1.20.1-0.5.0.jar`（SHA `877d795f…` 与构建一致）；本模组仅一版；其他模组/配置/存档未变；interim 0.5.0 jar（`d568b081…`）+配置备份于 `ft-mod-backups/ftdungeonmobs/20261031162604`，0.4.0 jar（`f8f5c52a…`）+配置备份于 `.../20261031162146`。回执：`build/reports/deployment-0.5.0.json`。
- **interim → final**：最终 runtime（design-critic P1 修复 + 火焰条提亮）到位前曾部署 interim 0.5.0（旧 v3 runtime，jar `d568b081…` 234732B，00:21:46）；最终版已覆盖，两版 jar 均有备份。记录详见 `work/v3/integration/INTEGRATION_REPORT.md`。
- **外部 1 行修复（Lead 授权）**：gptt 在途 `ArenaChallengeRun.java:168` `SoundEvents.RAID_HORN` → `.get()`（1.20.1 Holder→SoundEvent，修复编译，不改玩法；原 SHA `6c163394…`→`b82fc60b…`，副本/diff 存档 `work/v3/integration/external-fix/`）。该在途文件其后被外部工作线整体移除，最终源/jar 不含该特性。
- **已知限制（如实）**：未启动完整整合包客户端（贴图动画火焰观感/性能/多人联机未实机验证）；v3 为原版风简化版（低分辨率 64²、少元素、8 帧贴图火焰），视觉密度按用户要求明显低于 v2；调级无 v2 的 flare 爆燃与发光层。
- **详细证据**：`work/v3/integration/INTEGRATION_REPORT.md`、`work/v3/integration/DEPLOY_PLAN.md`、`work/v3/integration/logs/build-0.5.0.log`、交付包 `previews/v3/`（如已生成）。

# 历史验证 · 0.4.0（火炬 v2 GeckoLib 动画方块实体，已被 0.5.0 回退取代）· 2026-10-31

## 0.4.0 火炬 v2（GeckoLib 动画方块实体）验证

- **构建**：`BUILD.ps1 -Task build -Offline`（离线 Gradle 8.8 + JDK17）`BUILD SUCCESSFUL in 23s`；完整 `check`（`verifyAssets` + `verifyArenaAssets`）+ `reobfJar` 通过。日志：`build/reports/build-verification-0.4.0.log`。
- **真实 Minecraft 1.20.1 + GeckoLib 4.8.4 解析**（`verifyArenaAssets` v2）：`ARENA_ASSETS_V2_VERIFIED` —— GeoLib 烘焙 geo（bones=50、cubes=233、flameBones=19、显式 UV 面=1398）；animations=2（`animation.arena_torch.idle` loop 2.0s / 24 骨骼，`animation.arena_torch.flare` once 0.8s / 12 骨骼）；5 档 128×128 主纹理 + 5 张 `_glowmask` 发光贴图；blockstates=10 variants；item 模型 165 元素 + display；无旧静态资源残留；4 个 ArenaTorch Java 类存在。
- **骷髅线回归**：`verifyAssets` → `VERIFIED: 4 models, 218 bones, 17 animations`（4 套骷髅 GeckoLib 模型/动画未受影响）。
- **独立复核**：qa-auditor 用自建工程副本 + 独立 GeckoLib harness 复跑 `97 checks / 0 fails / QA_V2_VERIFIED`；独立离线构建重建 JAR 与部署 JAR 逐字节一致（240858B，SHA-256 `f8f5c52ad481947e...6516`）。
- **JAR**：`ft-dungeon-mobs-forge-1.20.1-0.4.0.jar` 240,858 字节，`Implementation-Version: 0.4.0`；含 geo/animations/10 张 arena 纹理/particle/新 item 模型/占位 blockstate + 10 variants；无 0.3.2 静态资源或旧命名残留。
- **部署**：已安装 `FarmingTales_ForgeⅡ v1.5.2/mods/FT地牢怪物-Forge-1.20.1-0.4.0.jar`（SHA 与构建一致）；本模组仅一版；其他模组/配置/存档未变；0.3.2 JAR+配置备份于 `ft-mod-backups/ftdungeonmobs/20261031151957`。回执：`build/reports/deployment-0.4.0.json`。
- **已知限制（如实）**：未启动完整整合包客户端（动画观感/发光亮度/性能/多人联机未实机验证）；静态渲染下侧视/顶视火焰偏薄（2D billboard，游戏内由动画+glow+粒子部分缓解）；本机 Blockbench 动画帧渲染管线不稳定，预览 GIF 为火焰示意并已标注，整模 idle/flare 由 GeckoLib 在游戏内播放；链环为几何近似、黄铜口沿偏木质、UV 为整格映射（P1/P2 偏差已记录）。GeckoLib 4.8.4+ 前置需已安装。
- **详细证据**：`D:t工程文件\竞技场长矛火炬模型_20261031\work2\V2_DELIVERY_NOTES.md`、`work/v2/integration/INTEGRATION_REPORT.md`、`work/v2/review/design-review-v2.md`、`reports/qa-v2.md`（交付包内）。

# 历史验证 · 0.3.2（2026-10-31，gptt 参考图重制静态模型线，已被 0.4.0 取代）

## 0.3.2 参考图重制验证（历史）

- Java17/Forge47.4.10离线构建和重混淆通过；真实Minecraft1.20.1解析8个Java模型、10个双格档位状态、四个Forge全亮火焰面、五色各12个不同的32×32帧、七种Display；GeckoLib4.8.4实烘焙4套模型、218骨骼、17动画通过。
- `tools/verify-arena-torch.mjs` 验证14个跨界组件、549个侧面UV和旋转原点连续性、合法Java1.20.1角度、零重复几何。世界两半143组件/775面，编辑及物品129组件/723面，25个已证明隐藏面移除；火焰不复制逐帧几何。
- 本版独立Forge GameTest两组均通过，覆盖原生剑/箭/铁魔法属性、五项属性无重复叠加、配置重载、受伤血量比例、固定/自动档位、NBT往返、真实双格放置与右键循环、冒险/副手、范围最近选择、进出刷新、拆除与单次掉落、无效空间/水下拒绝。
- Blockbench七向、五色各12帧和GUI/左右第一人称/左右第三人称/地面/展示框已检查。专用Display WebGL上下文已丢失，改用健康的主渲染器读取同一Display相机与场景；没有将空白截图当成通过。实际截图及GIF位于 `output/`。
- 内嵌六张PNG与运行资源逐字节相同；主文件与原生Java导出对应129组件，UV无误差，导出坐标最大舍入误差约0.000005模型单位，Display角度按360°取模一致。编辑器原生导出带有新格式版本，因此仅作为对照，不用于1.20.1运行资源。
- 八个运行模型的局部静态审计均无错误、警告或未解析依赖；母版有六项预期提示（五张内嵌长条不能单独证明动画、编辑器不等于导出），由真实Minecraft的mcmeta检查和实际运行JSON另行覆盖。此局部预检不是完整版本认证。
- 生产JAR CRC/重复条目/版本/署名及45个资源逐字节核对通过；不含诊断类或第三方模组类。与接管时0.3.1相比，非火炬资源和其他Java类保持一致；唯一行为类变化是火星颜色常量。原四份骷髅模型与伤害系统未改。
- 报告：`build/reports/reference-0.3.2-build.log`、`reference-0.3.2-gametest.log`、`reference-0.3.2-package.json`、`arena-torch-continuity.json`、`arena-torch-master-export.json`、`audit-arena*.json`。诊断源和环境位于忽略的 `build/diagnostics/`、`run/difficulty-verification/`，交付诊断包仅用于复测，不能当模组安装。

没有启动完整整合包客户端或多人联机，没有彩色环境光或FPS实测结论。GameTest日志中的故意无效配置错误属于恢复测试；原有前置战利品/版本检查警告与本次模型变更无关。

0.3.2已部署，SHA-256与构建一致；备份旧0.3.1，配置原样保留，部署当刻其他383个mods文件没有变化。本次没有改玩家存档。回执见 `build/reports/deployment-0.3.2.json`。

## 0.3.0 历史验证

## 本次新增验证

- `BUILD.ps1 -Task build -Offline` 成功，包括 Java17 编译、Forge 重混淆、原四套 GeckoLib 模型/动画解析、新长矛火炬的 Minecraft 1.20.1/Forge 模型解析。
- Minecraft 原生解析器读取 8 个 Java 模型，解析全部10个“半部×档位”状态，确认五种火焰各8帧、每帧2刻，以及四个火焰面的 Forge 全亮元数据。
- 独立 Forge GameTest 使用真实 Forge 47.4.10 与铁魔法3.16.3前置：原难度系统回归和新火炬测试合计 **2 项全部通过**。
- 新测试实际执行 BlockItem 双格放置、消耗一个物品、上下任一半部右键循环1→2→3→4→5→1、上下状态同步、副手防重复、冒险玩家交互、现存与后续怪物跟随、固定档位优先、剩余血量比例、最近火炬选择、拆除回退、范围进出与20刻更新。
- 验证了方块状态与维度索引的序列化往返；破坏上半部、下半部或支撑均只掉一个物品，缺少双格空间与水下放置被拒绝，无效档位输入被拒绝。没有强行修改原版世界难度。
- 可编辑主文件与实际 Java 模型逐组件比对了坐标、旋转、面集合、UV 与 Display；角度按360度取模比较，225度与-135度视为相同变换。两张纹理均内嵌于主文件。
- Blockbench 人工核对正、背、侧、顶、底、斜视，五种颜色、8个动画相位以及 GUI/第一人称/第三人称/掉落展示；修正了第一人称底座裁切。截图、五色预览和动画GIF位于 `output/`。
- 静态结构审计没有发现错误、警告或未解析依赖；它不是完整游戏客户端认证。22个编辑组件，世界两半合计25个组件/124面，8帧纹理不复制几何，无客户端FPS结论。
- 生产JAR不含诊断类、诊断物品、验证器或第三方模组类；四套骷髅源模型/纹理、655个方块、218骨骼、17动画仍保持核对通过。署名只记录原模型提供身份。
- 已部署0.3.0并校验SHA-256；仅补充自己配置中的半径字段，备份旧版和原配置，其他360个模组JAR保持原样，未改玩家存档。

诊断环境位于忽略的 `build/diagnostics/` 和独立 `run/difficulty-verification/`。没有启动完整整合包客户端，没有宣称彩色环境光、多人/其他模组交互或FPS实测通过。Forge前置自带的战利品表与版本检查警告不属于本次改动。

## 本次报告

- `build/reports/build-verification-0.3.0.log`
- `build/reports/torch-gametest.log`，包含 `All 2 required tests passed`。
- `build/reports/arena-runtime.txt`
- `build/reports/arena-parser.log`
- `build/reports/arena-torch-assets.json`
- `build/reports/arena-project-equivalence.json`
- `build/reports/arena-model-audit.json`
- `build/reports/deployment-0.3.0.json`

## 0.2.0 历史验证记录

以下为上一次交付记录；其中的部署路径、版本和配置创建描述均为当时状态。

## 已通过

- JDK 17、Forge 1.20.1-47.4.10 下编译完成，并通过 ForgeGradle `reobfJar` 生成可部署 JAR。
- 四个模型的 655 个方块、218 根骨骼、17 段动画完整接入；几何版本为 GeckoLib 4 兼容的 `1.12.0`。
- 四张运行贴图与原始 bbmodel 内嵌 PNG 的字节完全一致，四份 bbmodel 副本与用户原文件的 SHA-256 完全一致。
- GeckoLib 4.8.4 实际模型烘焙器与动画解析器成功读取全部运行资源。
- 携带 Iron's Spells 'n Spellbooks 3.16.3、GeckoLib 4.8.4、Curios、Player Animator、Iron's Lib 的独立 Forge 开发环境成功执行 `runData`，模组和依赖加载完成。
- 弓走 `ProjectileUtil.getMobArrow` 与 `BowItem.customArrow`，再对原生箭基础伤害应用攻击属性比例一次；法师走铁魔法原生 `MagicMissileProjectile` 和法术强度接口；剑走原版近战接口。战斗不使用 `setHealth` 直接扣血，也不使用通用 `indirectMagic` 冒充铁魔法；改最大生命时的 `setHealth` 只用于保留剩余血量比例。
- 模型提供者署名为“一颗大豆，b站uid355533780”；没有将其登记为模组代码作者。
- 成品不内置 Minecraft、Forge、GeckoLib、铁魔法的类文件；所有前置作为独立依赖使用。
- 四种原版形状、不同副色的刷怪蛋保留完整模型和中英文名称；独立“FT地牢怪物”创造栏按战士、弓手、法师、王排列，并以骷髅王刷怪蛋为图标。四种蛋也加入原版刷怪蛋分类。
- 已部署到用户指定的 FarmingTales Forge 1.20.1 整合包；部署文件与构建文件的 SHA-256 一致。旧版 0.1.1 已移至整合包内独立备份目录，活动 `mods/` 只保留 0.2.0；其他模组文件列表、大小与修改时间前后相同。
- 成品 JAR 包含难度配置、公开 API、profile 与管理员命令类，不包含临时 GameTest 诊断类、诊断弓、测试结构或第三方类。

## 难度系统运行验证

在工程 `run/difficulty-verification/` 内使用真实 Forge 47.4.10 GameTest 服务端、真实前置与实体执行独立验证，最终日志为 `All 1 required tests passed`。没有启动用户整合包，也没有接触用户存档或修改服务器 EULA。

- 默认普通难度自动选择二档；战士生命 60、装备铁剑后的攻击属性 7.2、护甲 4，王生命 300、装备钻石剑后的攻击属性 12，法师原生法强 1.2、魔抗 1.1。
- 测得弓的 `customArrow` 基础伤害经过配置倍率只缩放一次，生成的仍是真实原版箭实体。
- 法师一档与五档的铁魔法 `getSpellPower` 比例为 2.25；五档魔抗由铁魔法原生 `DamageSources.getResist` 得到 0.5 的中性学派伤害系数，没有第二个减伤钩子。
- 半血战士切四档仍为半血；连续刷新 30 次不叠加修饰符、不回血，其他模组风格的外来护甲修饰符没有被移除。
- 单体倍率和加值正确组合；保存并读取 NBT 后保留固定四档、单体调整，以及 150 最大生命 / 75 剩余生命，不错误回满或截断。
- 实际执行 `set`、`bonus`、`clear_bonus`、`get`、`auto`、`reload` 管理员命令。世界难度改为困难时自动实体升三档，固定四档实体不被覆盖。
- 从磁盘重载档位与骷髅王类型配置后，已加载实体立即变化；测试中带类型加成的二档王得到生命 437.5、攻击 13.2、护甲 11、魔抗 1.2、法强 1.44。
- 无效档位、NaN 和超范围单体输入被拒绝；无效 TOML 重载报错并保留原已应用属性，修复文件后能再次成功重载。已死亡实体改档不复活。

诊断辅助源码与运行配置仅放在忽略的 `build/diagnostics/`，不添加或发布到生产模组中。导出目录的验证报告附有诊断 ZIP 以便复现；它不是游戏模组。配置文件监控取决于系统与 Forge，明确验证的是 `difficulty reload` 的磁盘重载路径，不承诺文件保存后监控一定立即触发。

## 验证边界

以上验证包含编译、资源解析、模型烘焙、原始文件完整性、依赖加载、独立服务端实体属性/NBT/命令验证，不等于在完整整合包中完成客户端战斗与联机验收。

尚未在真实游戏客户端逐项实测伤害数值、盾牌、武器附魔、学派抗性、多人同步和所有其他模组的交互。实现复用原版/铁魔法的相应处理链，不绕过这些规则；最终效果仍需在游戏中验收。

本次仅更新本模组 JAR，并新增自己的初始配置 `config/ftdungeonmobs-common.toml`。没有修改玩家存档、其他模组或其他配置。部署路径、摘要与回滚位置见 `DEPLOYMENT.md`。

独立前置环境仍有铁魔法自带的战利品表解析与版本检查网络警告，未改动这些无关资源；GameTest 最终通过，不把这些警告描述为本模组的难度功能失败，也不宣称整个整合包没有任何警告。

## 日志与资源报告

- `build/reports/build-verification-0.2.0.log`
- `build/reports/forge-load-verification-0.2.0.log`
- `build/reports/difficulty-runtime-verification.log`
- `build/reports/imported-assets.json`
- `build/reports/deployment-0.2.0.json`

导出交付目录另带以上日志和报告的副本。
