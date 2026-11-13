# 自动候选 Y 对齐规划器 v2（实验组件，不晋级生产）

## 实际完成与边界

已实现可执行的新规划器，不是仅有两房对齐公式或文档。真实输入取自只读 catalog/expansion-v3 七间原房候选与 catalog 基线05/06原走廊，逐字节核对九份NBT哈希并解析实际尺寸；所有端口、宽高、脚高、角色保持原manifest声明。七个 `accepted=false` 原样保留，**没有新增验收或资源晋级**。

Java17实跑：32个worldSeed × 4种强制入口旋转 = **128/128份完整九房图成功，0失败**。每图01—09各恰好一次，07入口根、09终局叶，8条连接；包含01、04、07真实异高端口与05/06原廊道。128份机器可读完整Plan和全部走廊安全盒见 `results/plans/`，不是用重复同一模板凑九房。每种固定根旋转内都有32种不同布局；忽略纯几何位置/旋转，模板放置顺序与真实端口树也有32种，不能把根旋转数当成独立种子数。

所有128份图实际含非anchorY房间；100份图的04用到了两个不同本地feetY层的真实接口。全图垂直包络（含所有房间和完整走廊外壳）实测高度为23、24、25、26或28，均在本批策略上限64之内。纯几何树连通不代表原生楼梯、门、房内路线、流体或头部净空已验收。

## Java17 可消费接口

新类 `cn.ft.biomedungeon.core.next.DungeonYPlanner`。保留冻结 `DungeonApi` 的所有记录，不在基线包中覆盖类。生产源在 `src/main/java/cn/ft/biomedungeon/core/next/DungeonYPlanner.java`；产物 `build/ft-biomedungeon-planner-y-v2-experimental.jar` **只包含新增规划器及其嵌套类**，需同时链接冻结 `../build/ft-biomedungeon-core-v1.jar`（SHA-256见ready）。也可由 integration 只读快照新源和已锁定基线源后编译。不得用新JAR覆盖旧JAR。

```java
import cn.ft.biomedungeon.core.DungeonApi.Plan;
import cn.ft.biomedungeon.core.next.DungeonYPlanner;

var limits = new DungeonYPlanner.Limits(-64, 320, 64, true, -1);
Plan plan = DungeonYPlanner.plan(request, templates, occupied, limits);
```

参数顺序冻结为 `Limits(int minY,int maxYExclusive,int maxGraphHeight,boolean requireEveryTemplateOnce,int rootQuarterTurns)`：

- `[minY,maxYExclusive)` 为调用者指定的绝对世界高度带。必须 `minY<maxYExclusive`，**没有隐式读取MC维度属性**；runtime应传入真实维度限界及更严格的测试区域政策。
- `maxGraphHeight>0`，限制**整个图**已放置房间、走廊地板/侧墙/顶板的全局 `maxYExclusive-minY`，不是每个局部房间或每条边单独限高。
- `requireEveryTemplateOnce=true` 要求 `request.roomCount == 匹配biome的模板数量`，每个模板恰好一次，不重复抽样；多入口直接INVALID_POLICY；多个终局无法同时作为末节点，按有界搜索拒绝。九房验证使用true。false保留基线的可重复抽样行为，不能用其九节点数量宣称九原房全覆盖。
- `rootQuarterTurns=-1` 随机根旋转，0..3固定根旋转供可重放验收；后续房间仍随机试旋转。该策略是约束，不是放置后对整图进行旋转。

另保留三参数 `plan(request,templates,occupied)`，默认 `Limits(-64,320,128,false,-1)`。这只是明确的默认实验策略，**不表示所有维度都能使用-64..320**。`Request` 原限制1..64房间、1..100000全局尝试预算继续有效。公开 `deriveSeed(Request)`，异常 `PlanningException.reason()/attempts()`；原拒绝类型外加 `INVALID_POLICY`。无部分Plan泄漏；有限预算不保证搜索完备。

## 核心修改：不只删高度检查

以冻结规划器为只读来源形成新命名空间版本。候选局部口先在origin=(0,0,0)旋转，再解：

`child.origin.y = parent.origin.y + parentPort.feet.y - childPort.feet.y`

等价于 `sourceWorldPort.feet.y - targetAtZero.feet.y`，使用检查溢出的整数减法。Y递归来自实际parent，不在下一层重置到anchor；保留所有原房内楼梯、平台、其他口的相对脚高。仅第一间根房的origin为request.anchor，之后origin可以整体上下移动。

随后用**实际解出的origin**重算全部八角点半开bounds；候选房间与所有现有房间/occupied/已预留走廊做三轴碰撞。两口的真实世界高度对齐后才生成外部水平直廊；用实际世界feet重算包含地板、两侧墙和顶板的reservation，检查其与新房、已有房、occupied及所有旧走廊的完整碰撞。房间候选和走廊候选均执行绝对高度与全图包络约束，失败回溯，不写世界。

宽3/宽5、height和medium仍须**精确相等**，朝向仍须相反，不放宽、不裁宽。07的北3宽与南5宽使其成为两套真实口宽子图间的根桥接；04为5宽多口房，09为5宽终局，因此无需伪造宽度转换器。原01北台阶没有被删除或抬高元数据，07原Y2/Y4和04原Y2/Y4均保留。

排序、嵌入SplitMix64、无偏Fisher-Yates、跨全部现有节点选择未使用口、全局预算、DFS回溯和输出不可变语义沿用基线；只增加Y求解、限界和精确覆盖策略。算法ID独立为 `ft-core-y-splitmix64-dfs-v2`，故与旧算法的seed/图不应混用。新instanceId前缀 `ftdy-`，其SHA-256覆盖请求种子材料、完整Limits、房间数/预算、排序模板和端口元数据、原NBT哈希及排序occupied盒；Limits不改变deriveSeed的基本材料，但一定进入实例身份。不要让生产world日志仅凭相同worldSeed匹配旧计划。

## 测试与独立复核

从本目录执行 `& .\build.ps1`。全部构建、class和日志仅写本目录；不运行基线build.ps1，不清理或重写基线目录。Python固定`-B`并禁用字节码写入，只读导入catalog NBT解析器。javac25.0.2使用 `--release 17 -encoding UTF-8 -Xlint:all -Werror`，实际执行Microsoft OpenJDK17.0.8，无外部Java依赖。使用jar的固定时间选项需要JDK19+打包器，运行/源码语言仍Java17。

- 冻结基线原测试直接链接其原JAR、编译到本目录独立测试目录：**94,932项断言PASS**，包括原本不同脚高拒绝，证明旧算法未被偷偷升级。
- 新规划器：**47,948项断言PASS**。真实九房128份全图、四旋转、原口局部高差、对齐、完整安全盒、排序输入不变、重复重放、12并发重放；width3/5与dry/wet继续拒绝；全局预算、非法覆盖策略、空池/重复模板、坐标溢出；完整三层累积originY=64/68/72链及全图限高；实际位移后occupied体积与走廊碰撞拒绝；同XZ但不同Y不误碰；真实九房绕开既有外廊预留。
- 独立Python方程复核：**33,060项检查PASS**，从JSON完整重建128份图的八角点、完整端口区间、面距离、外侧feet、全部地板/侧墙/顶板、全图高度和所有房间/走廊两两3D分离，不调用Java几何函数。这里“独立”指第二份公式实现，**不是另一个人员/线程的世界独立验收**。
- Java17/Java25重放向量、真实布局和统计一致；两次构建JAR字节哈希一致。源/JAR/日志/输入/128份Plan哈希在本目录ready清单。`inputs/source-lock.json`锁住基线与next-stairs所有ready清单项和ready本身、catalog-v3整个SHA清单及原NBT来源。交付前再次逐项检查无改变。

示例 `results/plans/q0-seed0.json`：07 originY64、06Y66、02Y64、01Y63、03Y64、08Y65、04Y64、05Y66、09Y66。每间原房只整体平移/旋转，未把建筑内部高差改平。`results/real-nine-results.json`包含逐seed/q实际结果，而不是仅几张精选成功图。

## 未解决的上线缺口（不得用几何通过掩盖）

1. 01原台阶、07楼梯与open=true原门需要原生VoxelShape、连续玩家身体/完整头部净空、step-up、邻居更新和双向整房路线验收。继承冻结next-stairs合同，API对齐并不消除catalog矩形检查与原台阶的语义差异。
2. 08原房有熔岩/藤蔓/墙等runtime政策外能力；本批只研究几何连通，**包含08不代表批准加载或流体安全**。七份v3候选仍accepted=false，其方块实体/载荷/动态状态及独立世界验收缺口未由本组件解决。
3. Runtime还要按实际translated origin（含基线blockPlacementOrigin旋转补偿）放置原NBT，保留原块、原实体载荷和楼梯，不让外廊/整体外围屏障/未用口封闭破坏原过渡带；所有权、区块票据、保护预检、回读/恢复与重启幂等仍属runtime/integration职责。
4. 这里只生成树和直线水平外廊，不实现新楼梯走廊、弯廊、循环图、地形适配或原生MC世界放置。源验收不能代替新增运行时验收；必须由integration锁定这些源与策略快照后复现。

没有启动任何Minecraft世界，没有修改正式mods/saves，也没有改catalog/runtime/integration、core基线或next-stairs。`ready=true`仅表示**新纯Java实验规划组件可消费**；`productionPromotion=false`、`world=NOT_RUN`，不称生产生成器或完整MVP已验收。
