# 原房间楼梯接口调查：独立交付，不升级冻结 core

## 结论优先

1. **S8-01 北口被 catalog 的矩形净空规则拒绝，不是 DungeonGeometry 拒绝。** 原始口是 `(17,1,0)`、north、width3、height3。现有 catalog 检查深度0和1、固定 feetY 的三层空气；深度1的 `(16,1,1)` 实际为原装 `deepslate_brick_stairs`，故拒绝。冻结 `DungeonGeometry.validatePort` 接受此元数据，四向旋转均可计算真实边界。
2. **冻结 DungeonPlanner 确实不支持房间间 Y 平移。** 它把所有候选的 origin.y 固定为 `request.anchor.y`，随后要求两端世界 feet.y 相同；不是已有隐藏能力。真实01 feet集合1/3与07 feet集合2/4在共同基准Y下无相等值。双向完整原端口池实跑均 EXHAUSTED；只保留各自真实北口（均width3）可隔离复现 `height difference unsupported`，排除宽度错误掩盖原因。
3. **几何工具与冻结 API 本身可以表达不同 origin.y；不需要在外部走廊造楼梯。** 本目录用真实原端口作手动候选对齐实验：`child.origin.y = parent.origin.y + parentPort.feet.y - childPort.feet.y`；整个原房间刚性平移，保持所有原始台阶和内部高差，外部走廊仍水平。32组双向、四旋转、真实width3配对均通过房间与完整走廊外壳三轴碰撞检查。这是几何可表达性证明，**不是冻结 planner 已生成这些图**。
4. **07 南口不能用 Y 平移消除宽度差。** 原 north `(12,2,0)` width3，south `(12,4,26)` width5，height均3，catalog 两者严格检查均PASS。07南口与01的所有width3口均应拒绝；本测试明确统计32组双向width不兼容。不得把width5改成3，或声称平移后所有口都兼容。
5. **不能据此宣称原楼梯已通过原生行走/完整头部净空验证。** 真实NBT已解析，三条原台阶横向通道与上方空气有证据；原生 VoxelShape、玩家连续扫掠、双向上下楼、整个07内部连通与世界tick后验均 NOT_RUN。供 runtime 消费的是 `transition-contract.json` 草案与真实方块数据，不是放行许可。

## 实际证据与例子

读取并逐字节核对了 catalog expansion-v2 需求表所指向的原始01/07 NBT和原metadata哈希，不复制、不重写源NBT。`source-verification.json`记录绝对来源、哈希、原口及严格catalog函数的原样复现结果，并绑定所读只读解析器文件哈希。

01北入口真实剖面（x16、17、18三条通道一致）：z0支承顶面y1；z1原南向bottom/straight台阶预期顶面y1.5/2；z2原同向台阶预期顶面y2.5/3；z3..5平台y3。台阶均waterlogged=false。这不是坏掉的入口：把深度1的台阶判为“空气被堵”只是平地合同不适用于原楼梯。不得删台阶、抬入口feet到3、修改JSON假装其原口在高处。

`s8-01-north-transition-cells.json`导出保护带 `[16,0,0]..[19,10,6)` 内180个真实方块状态，以及18列支承和上方空气检查。子方块台阶轮廓只作为标准straight/bottom/south形状的解析预期；未调用MC，不冒充原生碰撞证据。`s8-07-transition-cells.json`导出北/南各向内8行、原3/5宽、y0..9共640个状态；这并不覆盖整座房间通路。

实际例子（原口、不裁宽）：01原点 `(100,64,200)`、q0，北口世界脚高65；07原点 `(130,63,195)`、q2，原北口经旋转朝南，世界脚高也65。原01台阶由65升至67，完整保留；07的原南口平移后脚高67、width5，仍必须单独寻找真正width5匹配。水平外廊预留盒为 `[115,64,195]..[120,69,200)`，含3宽净空、两侧墙、地板和顶板，不侵入任一原房间。

## 兼容推进方案

- 基线继续完全冻结；不修改既有接口/ready/JAR。将楼梯类接口作为**独立的哈希绑定sidecar**，区分 `RECTANGULAR_FLAT` 和 `ORIGINAL_STAIR_TRANSITION`。后者保留原boundary脚点，提供内部原支承轮廓、过渡保护带和原生验证要求，不把台阶所在体积要求成全空气。
- 如需自动生成包含不同本地脚高的完整原房间图，应由父线程批准在新的版本目录实现最小候选Y对齐，而非更改冻结算法。修改点应限于候选originY求解及重新计算真实世界bounds；之后保持全图回溯预算、稳定排序、走廊预留和三轴碰撞。**不能只移除 feetY 相等检查**，也不能把源脚高统一化。
- 原01楼梯在房间内部，所以不必先增加斜坡/楼梯走廊算法。所有门口仍在同一条连接的相同世界脚高，房间之间不同baseY合法，房间内部原台阶承担高差。
- sidecar过渡带是房间自身的只读子区域；不能作为“其他房间”与自己的房间盒碰撞。它仍须随实际origin/q变换，禁止外廊/屏障/封口覆盖其中的原台阶、支承、完整头部扫掠区。任何额外外部适配体积需加入跨组件占用/恢复归属；本交付不引入该体积。
- 07南口找真正width5节点；如果确需外部渐宽接头，必须另立合同、原生碰撞/头部净空验收并单独预留空间，不能拿通用走廊或静默裁宽代替原房间。
- 不从本报告推断S8九间均可正式接入；入口/主厅/藏宝室的方块实体、原载荷、动态行为和整个原房间路线仍由各所有者按其证据独立处理。

## runtime 必须后验的内容

`transition-contract.json`列明来源哈希、实际口、分段半格台阶预期、内部落脚平台、预留/归属与拒绝政策。先保护完整原NBT，不改平原房间、不改家具。

必须使用实际放置并旋转后的 BlockState.getCollisionShape(world,pos,playerCollisionContext)，包括邻居与形状更新。按真实玩家width0.6、height1.8、实际stepHeight（vanilla目标0.6）做连续身体扫掠、每个踏步支承/最大台阶高度及完整声明height3的上方净空验证；不能只看脚点或把所有楼梯当空气。三条通道、所有旋转、入口边界到内平台和反向返回均需检查，并验证整个必经房间端口之间路线；07两条边界通过不等于其内部返回路线通过。提交前再次读回原块与头部空间，保留tick后证据，原生验证不可用则fail closed。

## 可重跑的真实测试

在本目录 PowerShell 执行 `& .\run.ps1`。仅向本目录写输出；Python用`-B`且`sys.dont_write_bytecode=True`，只读导入catalog工具，不向其目录生成pycache。Java编译 `--release 17 -Xlint:all -Werror`，运行Microsoft OpenJDK17.0.8，仅依赖冻结core JAR，所有class输出在本目录build下。

最终 **398项Java断言PASS**，32组实际口平移几何对齐通过、32组宽度不兼容拒绝、两个方向的冻结规划拒绝和两个隔离高度拒绝；真实源解析、严格catalog复现、哈希与冻结20项inventory核对也PASS。日志在 `logs/source-investigation.log`、`logs/real-port-java17.log`。源NBT静态调查不替代世界测试。

本目录 `ready.json` = 独立调查/合同可消费，**不是升级生成器ready，不是模板晋级，也不是完整现场MVP验收**。父线程按此目录清单只读快照即可；未向其他线程发消息，未改catalog/runtime/integration所有者文件、core基线或正式实例。
