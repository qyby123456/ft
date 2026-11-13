# 第8组逐张重做：仅第01张待登记验收

本次只处理参考图 S8-01-large-chamber.png，独立新写 recipe.mjs 并调用已安装 minecraft-builder-skill 的 run-build.mjs 创建实际 Java 1.20.1 gzip vanilla structure。没有复制旧NBT/旧建筑配方、没有套用其它组几何或仅换材质；公共加载/渲染校验工具复用不等于复用建筑。此前三套输出继续封存，未更改其拒绝状态。

**当前状态：AWAITING_REGISTRATION_ACCEPTANCE。技术自检通过不等于登记验收通过。没有开始、预生成或领取第02张，尚未完成第8组剩余结构。**

## 参考图与还原

- 原图：`D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S8-crystal-geode/out/S8-01-large-chamber.png`，来源哈希见 room.json。
- 先行解析：`REFERENCE_ANALYSIS.md`，分别说明构图、路线、材质、地标和推断边界。
- 规模：**23×13×23**，不受单个16×16区块限制；粗柱、平台、完整环道与晶峰跨度保留。透视图没有精确坐标，23格为原创比例转译，不是量测结果。
- 核心构图：单晶核三级圆台、紫白地面环带、左右绕行、三侧紫晶拱门、四根2×2分带柱身、相连顶梁、倒挂晶簇和六盏链悬暖灯。
- 垂直层次：地面脚位Y1；石台Y1/2/3，白石阶到脚位Y4观晶点；主晶峰最高Y9，完整顶板Y12。没有擅自加入二层交通或抬升出口。
- 机关与战斗：原图未呈现明确机关，机关数量0；空间允许分流绕台和回撤，但未加入怪物、波次、战利品或自动门，未声称实测战斗节奏。
- 原版紫晶没有概念图那样的普遍自发光；实际光照由灯笼提供，不伪造材质/光效还原结论。剖视仅为检查，最终NBT保留完整屋顶。

## 实际交付

`incoming/S8R_IMG01/`：room.nbt、room.json、summary.json、materials.json、preview.png、qa.json。

`views/single-01/S8R_IMG01/`：由最终NBT生成北南西东四向内部剖视、完整外壳图，以及两张四视图拼图；含相同哈希的NBT快照。软件材质渲染不是实机截图。

`lodestone-preview.png`：bundled Lodestone WebGL 真实查看器读取最终完整NBT后的截图；与软件剖视图分开。`qa/lodestone-browser.json`记录加载结果。

`assembly.json`：单一模块，不需拆分；局部原点[0,0,0]、放置顺序1和四个接口已记录。世界原点及正式槽位尚未分配。若未来大型结构需要拆分，将提供各分块偏移、锚点和拼装顺序，不先行缩水。

## 连接点与安全

原点为西北底部，+X东、+Y上、+Z南；下列坐标均是玩家脚部，不是脚下方块：

| 用途 | 朝外方向 | 局部XYZ |
|---|---|---|
| 入口 | 北 | [11,1,0] |
| 出口 | 南 | [11,1,22] |
| 出口 | 东 | [22,1,12] |
| 出口 | 西 | [0,1,12] |
| 安全点 | — | [11,1,2] |
| 观晶台落脚点 | — | [11,4,9] |

所有接口至少3宽×3高、有完整干燥地板。旋转时同步旋转NBT状态和接口；顺时针90° `[x,y,z] -> [22-z,y,x]`，北→东→南→西。连接模板应提供门外地板，不得悬空对接。

NBT包含完整体积显式空气，接入前必须检查整个23×13×23包围体的占用、世界边界、液体、实体；有冲突拒绝，不能无条件清空。没有提供玩家存档自动施工命令。

## 校验等级与重复度

- `qa/static.json`：版本、边界、完整地板/屋顶、四门干燥3×3、晶簇方向支撑、链条到顶板与灯笼支撑、无危险流体/禁用块/实体。
- `qa/native-review.json`：实际 Minecraft 1.20.1 NBT解码、注册表与方块状态、StructureTemplate读写回环；原生碰撞检查入口到安全点、全部出口、左右/后环道以及三级台阶落脚点可达。加载结果PASS，346个原生可达脚位。
- `qa/duplicate-selfcheck.json`：只读比较73个已存在结构（含已封存旧输出），忽略材质的三维占位经过四向旋转，未发现完全相同几何；另提供31×31重采样首层通行近似相似度。最高读样本相似度0.7466并非“通过阈值”或验收标准。图论拓扑与概念设计仍需协调方最终重复度矩阵审计，**不宣称全局唯一或审核通过**。
- `qa/packaging.json`：源/预览快照/服务返回NBT哈希一致，PNG可解码，实际WebGL查看器加载与材料清单核对。
- **未运行：Forge世界四向放置、邻居更新稳定性、区块加载、完整地牢接入及实机游玩。**原生NBT加载PASS不能替代这些证据。

## Lodestone与复现

当前交互查看：<http://127.0.0.1:5588/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S8R_IMG01>。服务停止后链接失效；默认显示完整屋顶，并非剖切预览。

重启：进入 `C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill`，执行 `node scripts/preview.mjs --nbt "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-01/incoming/S8R_IMG01/room.nbt" --port 5588 --no-open`。

复现顺序：Python deliver.py build、deliver.py static、review.py native、review.py render、review.py assembly、duplicate_check.py；启动上述预览后运行node capture.cjs和Python package.py。只处理当前一张；登记后不得擅自覆盖已接收文件，须另起修订目录。
