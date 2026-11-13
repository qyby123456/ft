# 九原房外围预算与规划可用性：审查及独立 v3 修复

## 真实发现

已完整核对冻结 core ready（20项）、next-stairs（17项）、next-planner-y（171项）、route-coordinate-review（28项）及 integration/v4-unified ready（1484项）清单，连同ready自身哈希。原组件没有修改，坐标审查结论保持。具体哈希在 `inputs/integrity-and-evidence.json` 与 `inputs/external-lock.json`。

普通安装第一次q3请求：worldSeed=267611119455246637、ordinal=3、anchor=(1000,200,1000)、九原房、maxAttempts=50000。**不是核心搜索EXHAUSTED，也不是房间损坏**：旧规划器成功返回图，runtime在任何build前安全拒绝外围预算。精确重放得到内部包络span=(271,25,91)，加3格外边后的整体277×31×97=832939格；虽然小于1500000操作上限，但271>256的X宽超限。完整旧图rooms/links与封存 `budget-rejected-plan.json`逐项完全一致。

integration随后成功的q3 retry采用worldSeed=0、ordinal=3、同九房，操作751440格。它是不同请求，不能把它写成第一次请求恢复成功，也不能固定换seed掩盖可用性。证据及原失败日志仍保留。旧版图几何正确，但没有把runtime外围写入范围/预算纳入搜索条件，是明确的跨组件可用性缺口。

本次32个不同worldSeed（含原失败seed、0、负数和long极值）×至少4个ordinal/q，另外8个边界seed扩展到32个ordinal，合计**352请求**。冻结v2观测：9份图返回后会遭外围预算拒绝、4份搜索EXHAUSTED。修复前的新包络搜索也曾遇到4份全预算耗尽，证据保留 `logs/initial-v3-failures.log`、`results/pre-pruning-matrix.json`；不是只选成功样例。最终v3把外围条件与共享总预算的多轮搜索一起实施，**352/352请求成功，九原房均各一次，未缩范围**。这只是在该有限矩阵上的结果，不保证所有种子都成功。

## 新规划器 v3 实际实现

独立类：`cn.ft.biomedungeon.core.next.DungeonEnvelopePlanner`，算法ID `ft-core-envelope-splitmix64-dfs-v3`，实例前缀 `ftde-`。生产源 `src/DungeonEnvelopePlanner.java` 由冻结v2源码派生到新文件，不覆盖旧类；保留原DungeonApi记录和DungeonGeometry。

- 保留真实Y对齐、不同本地脚高、所有四旋转、原宽3/5精确兼容、height/medium相等、端口唯一占用、三轴房间/完整走廊地板侧墙顶板碰撞、全图高度、精确九模板覆盖和末节点终局。没有改房间/门/楼梯/原目标。
- 在每个候选房间和候选连接处，重算**整个已选图**的外围操作包络，与冻结runtime Placement.java:60—78同公式：全部房间bounds；每个waypoint按min=(x-8,y-2,z-8)、maxExclusive=(x+9,y+10,z+9)扩张；求union；最外再加3格。
- 限制的是未加外3格的spanX≤256、spanY≤96、spanZ≤256，以及加外3格后整体体积≤1500000。不能错误地把“带外边宽≤256”替代runtime原规则，也不能只看走廊净空盒或原房总块数。当前每格一个Placement.Operation，体积正是操作数。
- 包络随加入候选只会单调变大；超限分支可提前安全剪枝，继续在**同一输入**中回溯找完整合法布局，不返一个超限Plan让runtime重试换seed。成功返回图再次可用公开 `envelope(plan)`核对。
- 因runtime把整个外围矩形内部写为原块/走廊/空气，连所谓“空白间隙”也是本次写入。新规划器要求整个外围盒与传入occupied不交；只预留房间和走廊会漏掉间隙内已有构筑。调用者应把其他作业**完整外围写入盒**当成occupied，不能只传其他房间盒。
- 围墙/外边也受Limits绝对Y限制；全图room/shell高度原上限仍独立保留。所有包络加减、维度和体积运算检查整数溢出。
- 为避免一次失败局部深搜耗光全部预算，每轮最多5000次尝试，共享request.maxAttempts总预算；轮间清理本轮未成功的节点、连接、used及外廊预留，用**同一嵌入RNG流继续**抽样，无worldSeed/ordinal/roomCount修改。maxAttempts50000最多十个满额轮次；如果候选集提前穷尽可提前开始下一轮，总尝试仍不超过50000。无根候选、零进展的轮次立即失败，不空转。EXHAUSTED仍只抛原因/计数，绝不返回部分图。
- 精确覆盖模式另作剩余端口兼容图的宽高/medium可达性必要条件剪枝。它不当成真实几何可达证明；所有真正加入的图仍逐盒检查。终局不会作为后续连接中继。
- 稳定ID排序、无偏洗牌、确定性SplitMix64和不可变输出不变。新算法版本改变内部种子材料是正常版本隔离；原回归仍传入**原请求的全部字段**，不是换世界种子。实例SHA包括Limits、EnvelopeBudget、模板/端口/原NBT哈希、occupied和预算，不能复用旧版持久日志的instanceId。

## 给 runtime 的准确接入接口

新增JAR：`build/ft-biomedungeon-envelope-planner-v3-experimental.jar`。**只包含新增规划器**，生产时依赖冻结基线 `core/build/ft-biomedungeon-core-v1.jar`；不需要v2 JAR（v2仅为对照测试）。可只读快照新源并保留基线API/Geometry源码编译。新class不覆盖 `DungeonYPlanner`；本线程没有修改runtime任何文件或部署JAR。

```java
import cn.ft.biomedungeon.core.next.DungeonEnvelopePlanner;

var limits = new DungeonEnvelopePlanner.Limits(
    164, level.getMaxBuildHeight() - 4, 64, true,
    Math.floorMod(ordinal, 4));
var budget = DungeonEnvelopePlanner.EnvelopeBudget.isolatedRuntime();
var plan = DungeonEnvelopePlanner.plan(request, templates, occupied, limits, budget);
var allWrites = DungeonEnvelopePlanner.envelope(plan);
```

`Limits`字段顺序与v2一致：`minY,maxYExclusive,maxGraphHeight,requireEveryTemplateOnce,rootQuarterTurns`，只是嵌套类归属不同，不能直接传旧v2.Limits实例。`EnvelopeBudget(int maxSpanX,int maxSpanY,int maxSpanZ,long maxCells)`默认256/96/256/1500000；**只允许收紧这些数，不允许构造更宽的限制**，防止接入时放大世界范围。四参数plan默认这一预算，三参数另有明确实验Limits默认值；runtime应优先用五参数显式传真实范围。

`Envelope(Box bounds,long cells,int innerSpanX,int innerSpanY,int innerSpanZ)`包含最终完整写入范围和操作数；静态 `envelope(Plan)`、`fits(Envelope,EnvelopeBudget)`可供接入层复核。保持runtime原Placement预算/保护预检，**不可因为核心通过就删预检**。如果更改runtime包络公式，必须锁定新版本并重新对齐，不允许把当前硬绑定+8/+9/+10/+3当成通用任意runtime合同。

接入时建议更新runtime generatorVersion标记使用v3并隔离新计划/日志；旧BUILDING/VERIFYING作业继续用原冻结计划/源/版本恢复，禁止重规划替换其图。参数哈希和新`ftde-`实例身份应与原runtime实际持久化流程共同复核。本组件ready不是已接入的runtime ready。

原失败请求的新图实际包络为 `[907,196,966]..[1073,227,1096)`、内部span=(160,25,124)、668980操作；同原worldSeed、ordinal、anchor、九房、原模板与50k总预算。图和完整旧图分别见 `results/fixed-exact-request-plan.json` / `old-exact-failure-plan.json`。

## 测试证据

执行 `& .\build.ps1`。仅本目录写class/log/results，基线测试在本目录重新编译并执行，v2原测试工作目录是本目录build/previous-run，不改冻结日志。编译 `javac --release17 -encoding UTF-8 -Xlint:all -Werror`，实际Microsoft OpenJDK17.0.8运行。

- 新v3：真实352九房请求、32seed×4及8seed×32ordinal；精确原失败重放与新图预算；九房全覆盖、完整全图/安全盒碰撞与连接、排序插入不变/重复/12并发；精确预算边界、超过一格/一个操作拒绝、禁止放大runtime范围、全局attempts1及50000拒绝、无根零进展退出、绝对围护Y、occupied空白间隙覆盖拒绝/重规划、宽3/5和dry/wet显式不兼容拒绝、Limits限高、预算进入ID。
- 冻结core原测试94932断言PASS；冻结v2原测试47948断言PASS，证明没有悄改旧实现。新断言准确总数由 `results/java-summary.json` / ready记录。
- 独立Python公式逐份复核352图：原失败旧图与封存JSON完全一致；真实原端口Y、宽度、面朝向、区间；所有房间和完整走廊壳两两3D分离；按原runtime公式独立求完整包络/操作数；所有图合规。352份中最大操作数1233210，未放宽1500000上限。独立是第二份方程实现，不冒充另线程的MC世界验收。
- Java17/25都执行相同矩阵，并保留重放/统计证据；JAR固定时间打包，两次打包字节哈希一致。初始失败搜索日志保留，最终ready清单只声称最终明确测试范围。

## 尚未覆盖的风险与世界证据边界

本次读到的**较新 integration/v4-unified/final-evidence.json**已记录独立原目标矩阵2200/2200、36场景、每转148目标且零目标删除；这不同于旧route-coordinate-review当时锁定的runtime生产者1100/2200。旧审查ready保持原历史时间范围，不篡改其结论为最新世界状态。最新集成仍明确748条全宽采样线中112条失败，主因S8-07原门洞整宽/头部碰撞；completeGeneratorReady和productionPromotion均false。此新规划器**不修、不掩盖该失败**，归catalog/runtime处理。

九房包围盒和外廊连通是几何图，不等于实际房内路线、门交互、所有实数宽度位置可通行、全图入口返回或实体/流体稳定。精确源字节和脚高照旧，但不得因重排后包络更紧就继承原世界route证书：新布局必须由runtime/integration按新源/JAR/ID重跑。08熔岩/藤蔓动态政策、邻居改变楼梯/墙/门形状、封未用口/整体屏障、保护预检、回读、票据和重启/断电恢复仍在世界侧验证，core未启动任何世界。

有限352请求不证明所有seed/ordinal/occupied都成功；50k预算或真实障碍太多时仍安全拒绝，不减少房数、不增加世界边界、不换seed。原Request.roomCount1..64接口继续存在，但本批真实验收仅九房。完全任意模板池、巨大池性能、复杂高差/弯廊/循环图和原内部通道状态不是本修复承诺。

`ready=true`仅表示新Java17规划器及审查证据可只读消费。所有旧core、坐标审查、catalog/runtime/integration文件、原房间NBT、原目标及正式mods/saves未改；未宣称生产或完整世界验收。
