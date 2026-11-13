# FT 群系后端源码交付

目标 Forge 1.20.1-47.4.10 / Java 17；provider ID 由 FT 主模组注册为 `ftdungeonmobs:amethyst_biome`。本包不注册 @Mod、事件订阅、命令或维度；`src/harness` 是独立测试宿主，严禁合入主模组。

## 合并

将 `src/main/java` 与 `core-snapshot` 的 Java 源码合入主模组编译源集，`src/main/resources/data/ftbd_candidate` 合入主模组资源。不要复制 `src/harness` 的维度、测试门、mods.toml 或 pack.mcmeta。主模组使用自己的入口及资源元数据。本包 build/libs 中 library JAR 仅用于构建审核，不是另装的独立模组；不能把测试宿主 JAR 当正式组件部署。

复用最终九房 core、锁定 CandidateCatalog、原生 BE、原流体协议和 TransitionVerifier；八原件加一授权派生入口 NBT 字节保持原样。源目录和旧冻结组件不改动。候选资源里的 accepted=false/旧隔离审计标签是保留的来源账本，不是本适配器要求世界标记或 receipt 的逻辑。本适配器没有旧 isolated 命令、world marker、receipt、commit 或其他模组白名单限制。

## API

```java
BiomeDungeonBackend.Status state = BiomeDungeonBackend.prepare(destination, new BlockPos(1000, 200, 1000), 0);
BiomeDungeonBackend.tick(server);
BiomeDungeonBackend.current(destination);
BiomeDungeonBackend.status(destination);
BiomeDungeonBackend.ready(destination);
BiomeDungeonBackend.close(server);
```

全部在服务器线程调用。由主模组每次 ServerTickEvent.END 调用 tick 一次，停止时调用 close；内部以维度 gameTime 防止重复 tick。prepare 规划并保留状态，完整预检/施工/自然更新/观察/读回分 tick 进行。状态为 IDLE、BUILDING、READY、FAILED；仅 READY 可传送到 Status.landing。current/status/ready 是缓存状态查询；实际入场通过 prepare 校验保留平台和门格。已存在的不同 anchor/ordinal 请求返回 FAILED/REQUEST_MISMATCH，不重建。

Status 还包含 entranceOutward、returnGateAnchor、returnGateFacing、platformBounds、envelopeBounds、instanceId、completed/total、observedTicks、error。所有边界为最小包含、最大排除。方向和坐标必须消费实际 Status，不要硬编码旧隔离世界或旧(0,64,3)大厅位置。FT 主适配器锁定每轮生成类型、READY 后放返程门并使用现有 returnToOrigin；本后端不实现传送，也不创建长廊到旧大厅。

## 入口侧平台与门格

完整九房图保留入口两种宽度的原端口。最初“独占预留北口”会断开精确九房门宽图，已撤回并保留失败回归。当前 policy/VERSION 为 `ft-biome-backend-v1-side-porch2`，直接调用原核心完整端口图；不改种子、维度、ordinal，不隐式重试或提高预算，仍为 50000 次有界搜索。

在入口北口外连接廊上侧向扩建短平台；只修改新外接连接廊材料，不覆盖任何原房间格或 BE。平台创建纳入 Placement 操作摘要、完整空气/流体/BE/实体/边界预检与同一包络预算。其玻璃侧墙、地面与顶面生成一次；返程门避开原三宽主廊。ordinal 0 的几何以最终状态/实测记录为准。

FT 门使用 Status.returnGateAnchor 与 returnGateFacing，partPosition 等于 anchor.relative(facing.getClockWise(), part%3-1).above(part/3)，共12格。创建完成前这12格与所有格一样要求空气并严格写入/读回；READY 后允许空气（无孤立BE，供主适配器补门）或注册ID精确为 ftdungeonmobs:dungeon_return_gate 且 part/facing 正确的门。part=1 必须有类型 ftdungeonmobs:dungeon_gate 的BE，其余part不得有BE；open、moving、tier 动态值不限制。平台其余格完整严格比对，不把整个 porch 忽略为任意可写区。

每次已完成实例 prepare 和 READY 重启加载都检查安全落点、原生三宽三高平台通道及门格规则。首次开放前完整逐格/BE读回所有源房间和连接廊，并执行真实台阶/侧门/主拱验证；READY 之后不再以空箱初始 payload 覆盖或否定玩家的正常房间改动。不会重置物品、门状态或房间建筑。平台改坏、错误门格或落点不安全则闭锁；后端不擅自修复。

## 安全施工与存储

仅允许 `ftdungeonmobs:dungeon` 高空独立空域；必须保留 randomTickSpeed=3/doFireTick=true。预检覆盖完整包络，包括显式空气、外壳和侧平台，未知占用拒绝；施工时再次逐格冲突检查。实体/玩家进入施工包络也会闭锁。每批最多1024操作，先持久化 attempted 所有权，再写原生方块/BE；chunk save(true) 后才确认完成前缀。原藤蔓支撑后放置、熔岩最后放置，检查盆体/燃料，强制 ticking 并观察2400实际世界ticks；完成读回后才 READY。

每维度 SavedData 名称 ft_biome_backend_v1，耐久性日志在世界根 ft_biome_backend_v1/<namespace>/<dimension>.ftb；日志使用长度/摘要校验与原子写入并先于 SavedData 更新，是已确认状态的权威记录，SavedData 是正常世界保存的镜像。缺日志而残留 SavedData、损坏日志、政策/种子/维度不符均拒绝。进程在 BUILDING 时中断后重启直接 FAILED，不自动重放或覆盖；正常停止施工同样闭锁。没有自动 reset/清场 API，必须管理员保留现场后人工决定迁移/恢复方案。没有断电持久性保证。

FT 在方块破坏、放置/多放、桶、工具/耕地、活塞源/目标、爆炸等事件修改世界前调用 protects(level,pos)；命中则阻止该外部操作，并用 externalChange(level,pos,reason) 闭锁。原生流体/火放置事件不得被偷偷取消；如命中施工区，同样通知 externalChange。后端自身不注册事件，避免主模组重复。未知插件直接改地形不属于此API能自动阻止的范围，逐格预检与读回仍会检测冲突。完成后保护/冒险模式规则由 FT 主模组负责。

## 编译与隔离测试

设置 JAVA_HOME_17 为 JDK17，执行 `./BUILD.ps1 -Task build -Offline`；已有 ForgeGradle 缓存可离线。`./BUILD.ps1 -Task backendPlannerTest -Offline` 验证实际 FT 维度、实际 seed267611119455246637、固定 anchor 的四个 ordinal 请求确定性。`./BUILD.ps1 -Task harnessJar -Offline` 只构建隔离测试宿主，随后用现有 Python 环境执行 `python -B installed_test.py --attempt <新唯一名字> ready interrupted occupied journal-io`。

测试脚本仅在 backend/run/<case>-<attempt> 创建普通 Forge 服务端，不访问正式实例。它安装测试宿主并创建测试用 FT同名维度，未调用旧命令或使用世界授权标记/receipt。ready 覆盖九房、原生流体2400ticks、重启复用与玩家改动保留；返程门夹具匹配真实FT registry/property/anchor-BE 合同，但不是完整FT门动画/传送实测。完整 FT 门/大厅、主mod整合与多生成器切换交由独立 integration 验收。interrupted 为真实 JVM 强杀；occupied、journal-io 验证障碍和真实文件I/O失败闭锁。所有失败世界、日志保留，最终可消费状态只看本目录 ready.json 及其精确输入/测试摘要。
