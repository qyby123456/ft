# routeaudit 坐标语义审查（只读冻结候选，不放行世界验收）

## 结论

针对 runtime/next-unified 冻结JAR `e6ca2da6399ce75ca3dca73236557d002d3f3584c93a0772602279855894dbdd`，**当前S8原目标矩阵没有证明出的额外半格偏移错误**。`R(q)*(0.5,0,0.5)`与原 `IndependentRouteAudit.java` 的真实节点转换一致，也与blockPosition的旋转原点补偿一致；不能因为原JSON标注“保留坐标、不暗加0.5”就认定玩家中心不能加半格。JSON记录原始目标，玩家中心是原审计早已采用的解释层，二者不能混淆。

但这个结论有明确域：选中S8的36case/2200检查含4400端点出现，**全部是整数坐标**。真实文件并未发现分数端点；不能把“支持保留分数”的描述当成本批确有分数的证据。一般半格分数也等价；任意非半格分数存在量化差异反例，详见下文。**不因此声明1100/2200以外未跑的路线PASS，更不放行交互、整图返回路线或生产。**

## 原审计与运行时到底在做什么

原文件绝对路径：`D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685\IndependentRouteAudit.java`。行94—96 `node`：

`eastHalf=round(2*x)+1; upHalf=round(2*y); southHalf=round(2*z)+1`

行68—75 `stand`将各half整数除以2作为玩家脚部身体中心。故原始p的真实玩家中心为：

`C_old(p)=(round(2*x)/2+0.5, round(2*y)/2, round(2*z)/2+0.5)`

整数/半格p时，简化为 `p+(0.5,0,0.5)`。原`body`围绕这个中心作对称AABB，绝不是直接以JSON的整数x/z作为玩家中心。原程序的方块形状保持未旋转、无补偿的本地原索引，二者在源空间一致。

catalog-v4把原目标p变换为 `f=(0,96,0)+R_q(p)`，仍是原目标坐标表示，没有中心化。runtime `NativeRouteAudit.java:73`的center使用已旋转f，计算：

`W=room.origin+(f-(0,96,0))+R_q(0.5,0,0.5)`

因此在本批真实输入上 `W=room.origin+R_q(C_old(p))`。逆变换 `p=R_-q(W-room.origin)-(0.5,0,0.5)`恢复完全相同的原目标，无另选脚点、删目标或移动端点。**不能再旋转f一次**，也不能给Y加0.5。96是这批fixture的固定Y锚点，不是房间最终originY；实际origin来自已规划房间，已在报告中核对。

## 与blockPosition及q1/2/3边界一致性

`Placement.java:42/44`用`transformCell`放原块并native rotate其BlockState；`transformCell:202`调用冻结 `DungeonGeometry.blockPosition`。对原索引b：

`B=origin+R_q(b)+c_q`

其中q0..3的c分别为 `(0,0,0),(-1,0,0),(-1,0,-1),(0,0,-1)`。它们是**旋转unit cube的最小格点补偿**，不是连续物理坐标额外平移。恒等式：

`R_q(0.5,0,0.5)=c_q+(0.5,0,0.5)`

所以对整数原目标p，runtime玩家中心正好 `blockPosition(p,origin,q)+(0.5,0,0.5)`；玩家处于实际已摆放的那格中心，而非旋转bounds外的原始索引点。q1/2/3并没有漏掉c，也不能再加一次c（否则错一整格）。对连续几何和原碰撞形状，正确物理变换是 `T(s)=origin+R_q(s)`；块的c已使旋转块占据该体积的正确unit cell。

07北口p=(12,2,0)的四转相对玩家中心分别 `(12.5,2,0.5),(-0.5,2,12.5),(-12.5,2,-0.5),(0.5,2,-12.5)`；这些点均在对应旋转半开房间边界内半格，身体横截面仍有足够边界余量。不加中心偏移或对q1/2/3统一加未旋转(+0.5,+0.5)才会改变原支承/边缘位置。全4400端点均核对其身体横截面位于旋转房间范围；这不是头部与家具净空认证。

## 真实楼梯、门边与支承证据

冻结q1报告中07原点(1000,200,1000)，原北口的nativeStart=(999.5,202,1012.5)。去掉origin再逆旋转得到(12.5,2,0.5)，再减半格恰是原目标(12,2,0)。同一条真实报告路径逆变换依次为：

`(12.5,2,0.5) -> (12.5,2,1.5) -> (12.5,3,2.5) -> (12.5,4,3.5) -> (12.5,4,4.5) -> … -> (12.5,4,7.5)`

其脚下原NBT分别为deepslate_tiles、两级原deepslate_brick_stairs、polished_deepslate平台，近z4.5位置保留原dark_oak_door的open=true上下半状态（门仍有形状，未视为空气）。q2报告逆变换得到相同源路径。01报告q0/q1逆变换得到原入口 `(17.5,1,0.5)`、两级原楼梯 `(17.5,2,1.5)/(17.5,3,2.5)`、内平台 `(17.5,3,4.5)`。完整浮点数未改写，正文只为可读性四舍五入，误差在1e-7内。

证据 `evidence/actual-stair-door-path-inverses.json` 对4条实际报告路径逐点记录世界身体中心、逆变换、本地脚下源格状态及邻近原门状态；`stairs-door-and-platform-probes.json`另列01/07原端点与显式非目标台阶/门格探针四转。真实原01 `dais-landing`目标(17,6,14)亦保留，未替换为地面点。探针明确标记是否原matrix目标，**不把另加探针冒充原目标验收**。

这些证据证明没有通过半格偏移把旧目标换成新目标、或漏用旋转补偿。它们**不重新证明原生支承/门碰撞/头部扫掠**：点下格查表不是VoxelShape，踩踏位置在台阶半格分界附近尤其不能以floor格子名判成功。原IndependentRouteAudit使用EmptyBlockGetter静态shape、0.598横宽body、较窄support probe、半格采样；runtime使用FakePlayer native move、0.6宽身体、实际世界和不同步进/下落规则。坐标一致不意味着两个物理模型/支持探针或路线搜索完备性等价。

## 分数坐标边界：真实数据与反例分开

本批挑中的九间原房start/targets/ports，以及完整2200检查，实际零分数坐标；原始路由文件的其他内嵌项检查也没发现分数。不能按原描述推断本批测试已经覆盖真实分数端点。

对于未来真实半格p，例如(12.5,4,4.5)，原审计中心(13,4,5)，runtime同样如此，四转成立；分数并不代表坐标已是玩家中心。对于非半格p，例如**合成**(12.25,4,4.25)，原audit先量化，中心(13,4,5)，runtime保留小数后中心(12.75,4,4.75)；q1/2/3同样不等价。另有Y=4.1的合成反例，原量化到4而runtime仍4.1，可能改变支承判定。`fraction-domain-counterexamples.json`明确标合成，不制造“真实端点bug”。

集成应先检查源domain；本批整数无需改目标。如果以后接入非半格目标，必须明示/版本化“原半格量化语义”或“连续数值语义”，分别保留raw原坐标和derived身体中心；不得静默round或移动原目标来凑成功。不能笼统宣称当前center对所有分数与原审计一致。

## 实际执行与可复核来源

`& .\run.ps1`仅在本目录写文件。Python `-B`禁pycache，只读导入catalog解NBT；Java编译 `--release17 -Xlint:all -Werror`，Microsoft OpenJDK17.0.8实跑。**31,179项Python检查、52,808项Java断言通过**，覆盖：

- 全36case/2200输入矩阵的4400真实端点转后中心、原audit量化规则、源目标逆变换、冻结blockPosition及q1/2/3外边界横向余量；每项真实fixture值与目标均不修改。
- 冻结runtime两份报告1100项检查的2200个实际nativeStart/nativeEnd，与真实room.origin及sourceFrozenCheck逐一匹配并逆变换；4条原台阶/门边真实报告路径额外回源查证。
- exact JAR资源与runtime源resource、catalog-v4矩阵/route-input字节逐项一致；JAR哈希和class-entry哈希保留。`javap -p -c`从准确冻结JAR导出NativeRouteAudit/Placement/DungeonGeometry字节码，center中符号offset和Y减96、transformCell调用blockPosition与源一致。没有只靠源码存在便宣称验证了另一个JAR。
- 基线core、next-stairs、next-planner-y所有ready清单及ready本身，runtime统一候选整个ready清单、catalog-v4整个SHA清单和真实资源均核对；外部读入精确锁在 `input-lock.json`。

Java公式harness并非直接调用Minecraft NativeRouteAudit，不伪称这里执行了native solver；实际世界证据仅来自已冻结的生产者报告，没有升级其证据层级。本目录没有启动世界或重编译/修改被审查JAR。

## 给integration的放行边界

`coordinate-contract.json`是**当前整数/半格原目标约定与旋转放置一致性的合同**，不是route或生产放行证书。可把坐标约定项由PENDING推进为“只读独立方程/真实报告核对通过，域受限”，但不得因此覆盖runtime现有result的其他未放行字段。

冻结result明确：nativeRouteAttempted=1100、nativeRouteRoundTrips=1100，requiredV4Checks=2200；all36IndependentCasesCertified=false。nativeUIAndDoorInteractions=NOT_RUN、globalEntranceReturnRouteCertificate=NOT_RUN、fullDeclaredWidthStairClearance=NOT_CERTIFIED，forcedKillPowerLoss=NOT_RUN。独立审查没有补跑剩余1100项，没有认证全宽3/5楼梯净空、实际门UI交互、全图入口返回、断电恢复或08独立流体验收。保持completeGeneratorReady=false、productionPromotion=false。

只写 `core/route-coordinate-review`；未改runtime/catalog/integration、所有已冻结core版本、原房NBT、原目标、正式mods/saves。交付入口本目录ready.json，哈希清单可供只读快照。
