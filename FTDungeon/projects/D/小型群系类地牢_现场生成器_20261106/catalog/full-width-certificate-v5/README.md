# v5入口整宽有限覆盖契约与可执行计算器

本目录仅为支持交付；不改现有v5 ready、41文件或任何原房/派生NBT。原01与派生07保持全部原宽与三高，侧双门仍为独立窄路线，不能关闭中央主通道。**没有原生碰撞输入时不产生原生通过证书。** collection-summary.json目前记录nativeWorldEvidence=NOT_PROVIDED_YET。

## 直接消费

integration只读本目录contract.json、native-input.schema.json与certificate.py。contract.json明确每个场景的源SHA、原局部坐标宽带、四旋转、需取回的全部原局部方块集合、原真实支撑高度提示及脚面起终高度。

1. 在integration拥有的隔离世界放置对应源SHA/旋转，原生回读状态并核对正确旋转与派生ID。按requiredBlocks逐格调用原生getCollisionShape，以真实站立玩家CollisionContext取回toAabbs；空气/空形状也必须显式记录boxes=[]，同时记录fluidEmpty和nativeState。
2. 将世界AABB所有角点逆旋转/平移回原局部坐标，重取min/max；返回boxes使用**原局部绝对坐标**，不是block-relative。localBlock是原整数格坐标。保留原生精确格点数值，不能按方块名手写形状冒称native，也不能取旋转后一个角点代替完整AABB逆变换。
3. integration生成自己的native-shapes.json，字段符合native-input.schema.json，contractSha256取本contract.json原字节SHA。collectionMode=WORLD_NATIVE_VOXELSHAPE，coverageComplete=true要求确实全量采集；12scene对应12strip不能漏/重复，scene资源SHA完全匹配。上下文必须为PLAYER_STANDING_COLLISION_CONTEXT。原生上下文/状态可能变化时分别采集验证，不把动态形状静态化。
4. 在integration工作目录执行：`python -B <本目录>/certificate.py --contract <本目录>/contract.json --native <integration>/native-shapes.json --out <integration>/finite-certificate.json`。输出和native文件只在integration所有权目录；catalog不改写或要求互写。

## 原坐标带、真实脚面与取回集合

12scene = 01北口、派生07北口、派生07南口三条带×四转。所有坐标为原局部连续坐标，**不加半格、不减宽**。

- 原01北口：中心X16.3..18.7，中心Z0.3..5.7，原脚Y1到Y3，bodyWidth0.6，三高。实际南向bottom直楼梯在Z1/Y1及Z2/Y2；原生表面预期先后为Y1、1.5、2、2.5、3。身体前缘比中心前0.3，因此支撑包络的预期前行中心抬升界限为Z0.7、1.2、1.7、2.2；仅为核对提示，不作原生输入。
- 派生07北口：中心X11.3..13.7，中心Z0.3..5.7，原脚Y2到Y4；楼梯在Z2/Y2和Z3/Y3。预期支撑Y2、2.5、3、3.5、4，前缘导致中心界限Z1.7、2.2、2.7、3.2。派生中央Z4/5三宽三高拱洞应原生回读为空；台阶没有削平。
- 派生07南口：中心X10.3..14.7，Z24.3..26.7，脚Y4恒定。计算按升序Z，仍需原生双向运动复核；保持原南口整宽要求。

每条完整中心横带的身体横区间是[x-0.3,x+0.3]，全带联合为原01 X16..19、派生07北X11..14、南X10..15。集合覆盖身体footprint加余量、Y0至最高脚面+三高上方余量。requiredBlocks逐格列全，不以只列台阶名代替真实方块集合。遇到AABB越出所属单元格时本版本fail closed，必须扩展采集/来源贡献集合，不声称已覆盖来自包围区外的高墙/栅栏形状。

## 有限解析覆盖如何计算

certificate.py读取全量原生轴对齐箱。对每个箱的X/Z水平边界，减/加0.3得到身体中心碰撞激活边界；这些边界连同原条带端点形成**有限构型空间划分**。每一轴同时包括相邻临界值间开区间、每个临界值单点，二维笛卡尔积覆盖整个闭合条带。不是0.05网格抽样：因为每个原生箱与身体水平重叠的真假在每个原子内部恒定，只需计算该原子的一个代表值就证明该原子所有点的同一箱激活集合；单点边界另外精确计算。

所有输入十进制值按Fraction转成有理数，接触边界不算正体积碰撞，不作epsilon缩小身体/条带。脚面取该原子水平footprint下、限定真实最低/最高脚Y间的原生支撑AABB最高顶面；它是身体前缘先遇到台阶时的保守支撑包络。支持实体必须来自原生输入，不能凭stairs名字加0.5。没有正面积支撑则拒绝；脚面上的三高身体包络与任何原生箱正体积重叠则拒绝。

相邻Z原子的脚高差必须不超过0.6，端点脚面必须等于原真实Y。计算输出每个有限原子的支撑来源格、脚Y、净空阻塞格、全部临界分区，并输出nativeStepReplayRequired：每个不连续抬升/下降的X原子与Z边界。01/07的半格抬升不可删除或用强制setPos抬升冒称原生运动。

## 解析覆盖不等于玩家运动全验收

无碰撞条件及有限原子覆盖可计算，但“脚面包络有路径且每次升高<=0.6”本身**不是Minecraft原生step算法已走过的证明**。因此即使静态箱输入全部通过，本脚本只输出CONDITIONAL_FINITE_COVERAGE_PASS，仍列native step forward/reverse、CollisionContext稳定、原状态旋转和端点支持等未完成条件。integration须以原生player.move/形状步高算法补齐有限不连续边界的双向运动证据，不能把条件性结果提升为全部原生PASS。真实原生算法的横向依赖若不能证明在原子内恒定，需给出算法级区间界/完整分支证据；继续加密采样不替代该证明。

这里所称脚面“最高支撑”只证明与输入箱一致的保守静态包络与正面积支撑，不证明完整足底受支撑或原生落地/摩擦/速度动态；要求更强支撑时必须新增原生规则，不偷改本证书结论。若Minecraft Native isFree/noCollision上下文包含此箱集合未表达的因素，integration需单列并使证书保持条件性。

侧门不能套中央三宽：按v5九房包追加16目标/48检查执行原双扇1.625宽通路、关门阻断侧路、开门恢复侧路、中央拱洞始终通行。所有原36case/2200check继续保留，不由本支持目录替换。

## 本地测试和冻结

prepare_contract.py实际生成输入/JSON Schema。test_certificate.py使用**明确为合成数据**的平面、薄障碍、过高台阶、支撑洞和不足三高来测试有限划分算法；不是Minecraft测试，也不是01/07真实原生碰撞数据。测试通过只证明工具约定下的正负例。ready和SHA清单锁定脚本、契约及schema，integration原生采集完成后可直接只读调用。未给其他线程发消息或改写其文件。
