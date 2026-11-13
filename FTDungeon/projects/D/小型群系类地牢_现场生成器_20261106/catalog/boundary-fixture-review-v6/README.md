# 边界夹具首次失败的只读复核

**根因是null bounds，非xAtom嵌套。** failed-run verification中实际BoundaryTests.unchanged把scene origin/turn包装成new DungeonApi.PlacedRoom(...,turn,null)。核心record构造器Objects.requireNonNull(bounds,"bounds")立即抛出，日志的failed! bounds与之吻合。没有证据说352条台阶运动已失败；本线程不启动JVM，仅静态复核失败执行快照和日志。

最小修复应由integration在自己的测试代码实施：让本地逆变换直接接收scene origin/quarterTurns（或NativeShapeCollection.inverse的测试本地重载）；无需构造PlacedRoom，不改core非空bounds契约。不要用一个伪造无关盒掩盖测试建模问题。

## 输入与覆盖

inputs/保存失败run的实际verification源码、日志、核心构造器、真实finite-certificate及只读当前collector源码，各自原路径/SHA在inputs-lock.json。失败run源码优先于可变当前world-tests。boundary-requirements.json把352项逐一赋予stripId:index，保留192个singleton和160个open_interval；双向共704项。xAtom均是扁平[low,high]，单点即low==high，不能解成仅一个端点、过滤零宽或合并删除。

01四转及派生07北口四转各44项；07南口四转无脚面跃迁不产生边界项，但仍属于12strip、4416原生采集及原完整宽度/3高验证。所有源/派生资源SHA、quarterTurns、最终JAR bab12080…、contract、certificate必须绑定。不能以0个执行/空列表passed==size算通过。

## 坐标与后续夹具要求

- 连续世界点：原点旋转q1(-z,x)、q2(-x,-z)、q3(z,-x)，再平移；现physical正确。世界完整AABB逆变换应八角回原坐标再取min/max，**不加离散格-1**。
- 离散原方块min坐标：q1(-z-1,x)、q2(-x-1,-z-1)、q3(z,-x-1)；该-1不能套在连续脚点/AABB顶点。
- 必须assert352项/704方向/全PASS；当前report后无条件helper.succeed存在误报风险。失败必须真实GameTest fail，不只写JSON状态。
- 用contract真实min/maxFeet代替hardcoded top<=4；保留实际楼梯步高，不用setPos在步内抬升。每次move前/后和精确边界核当前玩家CollisionContext、nativeState==sourceRotatedState、无fluid和完整nativeShape，不能只比较起终。
- unchanged当前按距玩家XZ<=2过滤格；如保留此优化，需要从已冻结有界shape包络证明所有可能侵入身体/3高区的来源都包含，并记录检查格，不能只凭“附近”推断完整。完整4416集合依旧保留。
- sweep使用deflate(0.00001)缩了身体与三高。应保留精确原AABB原生结果，不借epsilon豁免实际正体积侵入。浮点误差若出现必须独立量化，不能把验收宽/高变小。

**中点native replay不等于整open_interval原生证明。** 有限原子可由冻结原生箱静态覆盖；运动层还须解释原生step分支/上下文为何在原子内等价，尤其singleton边界。192单点必须逐一保持。更密采样不能代替未说明的等价条件；没有完成此层时保留条件性证书，不把704个代表回放称全部实数坐标原生证明。

findings.json给出B1—B7具体修复建议，未修改integration文件或脚本、未启动JVM。协调者已明确授权直接通知integration执行者，首次根因已发送到“群系地牢生成器 · 隔离集成与故障测试”；此目录供其实施与独立复验，不是本线程代改。
