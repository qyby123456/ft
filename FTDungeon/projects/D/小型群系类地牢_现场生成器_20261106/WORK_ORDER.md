# 小型群系类地牢：现场生成器并行实现任务

2026-11-06，用户授权“分线程去尝试完成生成器”。四个实现线程与现有八个房间制作线程并行，不打断房间制作；不创建新分支、不移动原工程。目标是**实际可构建、可在隔离Forge1.20.1世界中触发的现场随机房间组装MVP**，不是继续只写方案或预先写好MCA存档假装现场生成。

## 范围与安全

根目录：D:\ft工程文件\小型群系类地牢_现场生成器_20261106。只写各自子目录core、runtime、catalog、integration。共享此工单只读。其他组件、源工程、80旧验收源和新房间工作目录只读。**不得修改当前游戏实例的mods、配置、KubeJS、datapacks或saves，不得启动玩家世界，不得部署正式JAR。** 可在本根内创建隔离测试服务端/测试世界。不得下载付费图片，不调用生图。所有子进程记录工作目录，避免日志或Gradle写入正式实例。

目标先支持一个完整群系的真实随机规划与现场放置，基础轮其余已验收群系可按清单接入；禁止以凑数为由纳入第二轮制作方自检候选。第二轮全部目前先排除正式池。第10张总览禁止入房间池。整座已合并NBT随机旋转不等于房间级随机组装。

## 共同契约v1（冻结接口；不直接改其他线程源码）

统一Java17，基础纯Java依赖只用JDK；记录类在core所有者提供的`src/main/java/cn/ft/biomedungeon/core`中实现。组件间包依赖core，runtime不得依赖catalog源码。

core公开类`DungeonApi`，包含以下嵌套record/enum（名称和字段顺序固定）：

- `Vec3i(int x,int y,int z)`。
- `Box(Vec3i min,Vec3i maxExclusive)`，半开边界；输入检查三轴min<maxExclusive。
- `Port(String id,Vec3i feet,String facing,int width,int height,String medium)`，facing为north/east/south/west，medium为dry/wet。
- `Template(String id,String biome,String resourceId,String sha256,Vec3i size,List<Port> ports,String role)`，resourceId为资源位置而非任意磁盘路径，role可入口/普通/终局但不代表战斗。
- `Request(long worldSeed,String dimension,Vec3i anchor,long instanceOrdinal,String generatorVersion,String biome,int roomCount,int maxAttempts)`。
- `PlacedRoom(String nodeId,String templateId,Vec3i origin,int quarterTurns,Box bounds)`，origin为未旋转原点在世界的位置；旋转以局部(0,0,0)为中心，Y不变，0:(x,z)，1:(-z,x)，2:(-x,-z)，3:(z,-x)。最终bounds由旋转八角点重算，不偷偷正规化为盒最小点。
- `Link(String fromNode,String fromPort,String toNode,String toPort,List<Vec3i> waypoints,int width,int height)`，waypoints为世界脚点，连接安全区必须保留；初版允许仅直线/单拐，复杂高差拒绝重试，不伪造已支持。
- `Plan(String instanceId,long seed,String generatorVersion,List<PlacedRoom> rooms,List<Link> links)`。
- `enum Phase { PLANNED, RESERVED, BUILDING, VERIFYING, COMMITTED, FAILED, ROLLBACK_REQUIRED }`。
- `DungeonPlanner.plan(Request request,List<Template> templates,List<Box> occupied)`返回Plan或抛出带原因异常。稳定排序后抽样、明确算法，禁止Object.hashCode/未定序HashMap影响结果。保留重试上限，roomCount不能超允许范围。API实现与测试均由core负责。

catalog输出manifest.json模式：`schemaVersion:1`，`templates`数组字段与Template一致，size/feet坐标对象为{x,y,z}；`accepted:true`必须有独立验收来源与哈希记录。模板复制文件相对路径为`data/biomedungeon/structures/<id>.nbt`，manifest resourceId=`biomedungeon:<id>`；结构不是客户端assets资源。坐标真实读取来源，接口缺失/证据不清就拒绝，不猜门中心。

集成时manifest只允许资源白名单；runtime按打包文件原始NBT字节校验SHA-256，严格解析尺寸/接口/枚举及上限。不依赖外部任意路径。core不实现JSON解析；runtime用MC现有Gson。catalog需输出同时可解析的示例与字段说明。

## 四个线程分工与依赖

### A：core（随机规划与可重现几何）

独占core。实现契约API、seed派生、旋转/半开包围盒、接口对齐、房间级抽样、整图规划、碰撞与走廊预留、重试失败。纯Java17可执行测试：同输入稳定、插入顺序不变、不同种子可生成不同布局、所有旋转/接口变换、边界/走廊不相撞、请求非法、耗尽/不兼容干湿/高差拒绝。不能只摆一排固定图或简单换朝向冒充随机规划。给出编译运行命令和测试输出。先做真实算法再文档。

### B：runtime（Forge命令、世界放置与持久化）

独占runtime。创建隔离Forge1.20.1 Java17模组工程，modid=`ft_biome_dungeon_generator`，不得覆写已有FT地牢模组。只读参考现有D:\ft工程文件\FT-Dungeon-Mobs-Forge-1.20.1的Gradle包装器、映射/依赖、世界API用法；复制必要小文件并记录来源，避免拷贝大cache或正式内容。可复用本机Gradle缓存但不得改共享源码。

实现权限≥2的显式`/ftbiomedungeon plan ...`和`build ...`等可说明命令（准确语法由runtime实现后记录），限定隔离测试区域/显式确认锚点，默认dry-run。仅服务端执行，图先完整规划再写入。模板池哈希校验、占用/保护预检、所有目标区块票据、分刻预算、原模板空气/实体载荷、旋转、走廊补缝与整体外围屏障、回读验证、SavedData或等效持久日志及重启幂等必须真实实现；暂不支持的能力fail closed。

日志在写前持久化，明确本次拥有范围；无法保证恢复或原方块实体恢复时不要声明事务安全。遇到玩家冲突/未知实体/范围越界/模板缺失立即拒绝。世界随机刻/流体传播是专项限制，先从无裸露火液的已验收房间最小池验证；禁止通过全局关闭世界规则掩盖安全问题。核心依赖未交付时可用临时测试stub在runtime内部测试，但最终必须接core真实实现，不假通过。至少构建JAR并给出实际日志；隔离GameTest/服务端运行可做，无法运行则如实说明未世界实测。

### C：catalog（冻结模板与接口适配）

独占catalog。从基础轮独立索引和组装layout读取已验收01—09源，只读复制到catalog/data/biomedungeon/structures。实际解码NBT和真实接口，生成v1 manifest、单元检查、文件哈希清单及无法适配的拒绝记录。必须区分原房间开门属性/未使用口是否需要安全适配，安全适配生成新候选需重新检查，不能沿用旧验收哈希。先输出一套可用于MVP的最小合法池，明确哪些朝向/接口元数据尚需复核；不以制作方静态检查冒称独立验收。禁止纳入10号总览或11—20制作中的候选。不得把8个完整合并NBT当作8个房间。真实湿口/干口和楼梯高度不可统一猜测。提供runtime消费manifest/打包目录和测试。其余群系能适配则推进，不能则记录阻塞。

### D：integration（隔离整合、验收与故障注入）

独占integration。不直接修改前三线程文件。先建立测试矩阵与只读快照整合脚本，等各自ready.json后复制精确哈希到integration/staging，整合构建；不得因文件存在便认为已完成，快照后哈希验证。测试可重放、真实房间级随机、旋转、拒绝覆盖玩家方块、空模板/哈希错误、预检失败零写入、分刻中断/重启恢复与幂等、回读/载荷、世界边界/票据释放、返回路线、门缝/屏障不堵内部通道。隔离ForgeGameTest需在integration自己的run目录；不要启动正式客户端。测试失败保留证据和缺陷，不修其他所有者源码，用ISSUES.json描述；没有实际运行的测试标NOT_RUN。

最终能生成可构建候选JAR和隔离世界实测证据才按实际范围称MVP；不能以离线规划器通过代替现场放置通过。先输出独立构件准备与测试工具，不只写计划后结束。

## 通用交付与协调

各线程维护`progress.json`、`README.md`、测试日志。达成可消费接口时写`ready.json`，包含组件名、apiVersion=1、源码/产物哈希、实际测试命令与结果、未实现限制；它只是组件交付，不是整体验收。组件目录不可相互写入。依赖未完成可继续独立测试，接口冲突写本目录ISSUES.json。不要擅自向其他线程发消息；此调度记录为交接依据。

资料：D:\ft工程文件\地牢生成效果档案\02_小型群系类地牢\现场随机生成记录\README.md；其同目录S1—S8文件为逐房间规范。旧组验收索引：D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685\FINAL-80-SOURCE-INDEX.json。当前合并布局目录由D:\ft工程文件\FT地牢_八组合并总复核_20261106\delivery-index.json定位。只读原数据，不使用实时变化的progress自称模板已验收。
