# S8-01 R2：制作方补证，等待独立验收

本目录为制作方可复跑补证，未覆盖独立复核主报告，未登记ACCEPTED。第2张仍锁定。

最终NBT及原140项清单只读，运行前后哈希一致。checkpoint-original与旧9模块没有参与本次路线模型；只加载最终room.nbt。

NBT SHA256：ee50ce32006d15b24ae2b2932bbf4ebbef6683ae8b9ee33ca4781348ac0fa798

## 精确复跑

`C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe run.py`

工作目录为本文件所在目录。run.py自动编译并运行；commands.json保存完整javac/java argv和工作目录，classpath.txt是实际完整分号类路径；provenance.json含Java源、mapped jar、metadata和类路径源哈希。compile.log/run.log保留原始输出。

运行时：Minecraft Java 1.20.1 mapped jar + 原runData类路径排除minecraft_user_repo；实际Java位于C:/Program Files/Common Files/Oracle/Java/javapath。没有运行原制作包review.py，避免覆盖原报告。

## 锚点变换

局部原点保持[0,0,0]，顺时针变换[x,y,z]→[34-z,y,x]，原版BlockState.rotate同步变换状态；脚位Y不变。body center = [anchor.x+0.5,anchor.y,anchor.z+0.5]。世界坐标列只是假设原点[100,64,200]的计算示例，不是实际世界放置。

|旋转|原接口|旋转后朝向|局部脚位|示例世界脚位|3宽支撑/3高净空|
|---|---|---|---|---|---|
|0|north|north|[17, 1, 0]|[117, 65, 200]|True/True|
|0|south|south|[17, 3, 34]|[117, 67, 234]|True/True|
|0|east|east|[34, 3, 18]|[134, 67, 218]|True/True|
|0|west|west|[0, 3, 18]|[100, 67, 218]|True/True|
|90|north|east|[34, 1, 17]|[134, 65, 217]|True/True|
|90|south|west|[0, 3, 17]|[100, 67, 217]|True/True|
|90|east|south|[16, 3, 34]|[116, 67, 234]|True/True|
|90|west|north|[16, 3, 0]|[116, 67, 200]|True/True|
|180|north|south|[17, 1, 34]|[117, 65, 234]|True/True|
|180|south|north|[17, 3, 0]|[117, 67, 200]|True/True|
|180|east|west|[0, 3, 16]|[100, 67, 216]|True/True|
|180|west|east|[34, 3, 16]|[134, 67, 216]|True/True|
|270|north|west|[0, 1, 17]|[100, 65, 217]|True/True|
|270|south|east|[34, 3, 17]|[134, 67, 217]|True/True|
|270|east|north|[18, 3, 0]|[118, 67, 200]|True/True|
|270|west|south|[18, 3, 34]|[118, 67, 234]|True/True|

北口Y1、其它口Y3是结构真实高差，旋转不抹平高差；不能只按世界原点同Y接入。

## 全部9目标

|旋转|目标|局部整数脚位|无跳跃模型可达|路径采样数|
|---|---|---|---|---|
|0|interior|[17, 3, 4]|True|9|
|0|left-ring|[8, 3, 17]|True|53|
|0|right-ring|[26, 3, 17]|True|53|
|0|rear-ring|[17, 3, 26]|True|85|
|0|dais-landing|[17, 6, 14]|True|29|
|0|north|[17, 1, 0]|True|1|
|0|south|[17, 3, 34]|True|101|
|0|east|[34, 3, 18]|True|71|
|0|west|[0, 3, 18]|True|71|
|90|interior|[30, 3, 17]|True|9|
|90|left-ring|[17, 3, 8]|True|53|
|90|right-ring|[17, 3, 26]|True|53|
|90|rear-ring|[8, 3, 17]|True|85|
|90|dais-landing|[20, 6, 17]|True|29|
|90|north|[34, 1, 17]|True|1|
|90|south|[0, 3, 17]|True|101|
|90|east|[16, 3, 34]|True|71|
|90|west|[16, 3, 0]|True|71|
|180|interior|[17, 3, 30]|True|9|
|180|left-ring|[26, 3, 17]|True|53|
|180|right-ring|[8, 3, 17]|True|53|
|180|rear-ring|[17, 3, 8]|True|85|
|180|dais-landing|[17, 6, 20]|True|29|
|180|north|[17, 1, 34]|True|1|
|180|south|[17, 3, 0]|True|101|
|180|east|[0, 3, 16]|True|71|
|180|west|[34, 3, 16]|True|71|
|270|interior|[4, 3, 17]|True|9|
|270|left-ring|[17, 3, 26]|True|53|
|270|right-ring|[17, 3, 8]|True|53|
|270|rear-ring|[26, 3, 17]|True|85|
|270|dais-landing|[14, 6, 17]|True|29|
|270|north|[0, 1, 17]|True|1|
|270|south|[34, 3, 17]|True|101|
|270|east|[18, 3, 0]|True|71|
|270|west|[18, 3, 34]|True|71|

完整正向路径中心点见collision-routes.json；边条件对称，所以同一路径可反向通行。north目标就是起点，1个采样并非遗漏。

## 无跳跃判据与净空

- 玩家包围盒宽0.6、高1.8；水平/高度采样均0.5；每步高度差≤0.5，小于原版0.6步高。禁止整格升降，不以StairBlock类别或邻居存在楼梯豁免。
- 每个站点使用实际BlockState.getCollisionShape(EmptyBlockGetter,position)的原版AABB：玩家身体不碰撞，脚底薄片与支撑碰撞相交。允许原版阶梯常见的部分脚底支撑，不要求整个0.6×0.6脚面都落在台阶上。
- 每条边检查抬升后整段水平扫掠盒及起终两端完整竖向扫掠盒，均不能碰撞；因此头部净空检查覆盖整条边，不是只检查脚位上两格是否为空。上下边对称且高差≤0.5，不存在以自由落体代替下阶。
- 这是更严格的离散几何可行性模型，不是Entity.move逐tick输入、世界物理或游戏实测；EmptyBlockGetter也不是完整Level。当前结构使用显式直阶状态，未依赖环境动态重塑碰撞；世界更新仍需另验。

## 前入口和祭台阶梯

未旋转入口中心线：z0脚位Y1→z1南向底部深板岩砖阶（表面Y1.5/2）→z2南向底部深板岩砖阶（表面Y2.5/3）→z3+主层脚位Y3。

未旋转祭台中心线：z8主层脚位Y3→z9南向底部白石阶（Y3.5/4）→z10台面Y4→z11南向底部白石阶（Y4.5/5）→z12台面Y5→z13南向底部白石阶（Y5.5/6）→z14落脚Y6。

对应原版阶梯状态、每块局部碰撞盒及全部路线中间半格高度均在collision-routes.json；不再沿用旧验证器“当前或下方有楼梯即可整格升降”的判据。

### 正反例控制

- south_stair_forward：期望True，实际True，PASS。
- north_stair_reverse：期望False，实际False，PASS。
- full_block_no_jump：期望False，实际False，PASS。
- south_stair_low_headroom：期望False，实际False，PASS。

反例在独立内存模型中构造，不修改NBT。正向南阶通过、反向北阶/整格障碍/低顶失败，证明测试能区分朝向与头部障碍，不是仅检查阶梯存在。

## 稳定性边界和结论

- 四向真实世界放置：NOT_RUN。
- 400tick/邻居更新/区块重载/Forge实机：NOT_RUN。
- 本次执行仅为Bootstrap原版碰撞形状计算，无ServerLevel、无实际玩家运动输入、无世界文件生成；不能把四向内存状态映射写成四向实际世界放置。
- 保持原R2最终包不变；本补证不构成独立验收，不自行解除HOLD，不启动S8-02。

独立复核方可使用精确命令及本目录源文件重新执行，并据其自身锚点/路线/稳定性审核记录决定后续登记。

## 四向分别剖切的Lodestone补图（修正旧证据遮挡）

旧制作包中的东/南检查相机主要看到未剖去实墙，不能作对应方向内部验收。本补证在独立目录从最终NBT字节直接读取并派生四个不同检查NBT，每个仅去掉屋顶Y20、该方向近侧两格墙及其顶梁。未重新执行建筑配方，未改变正式候选任何方块。

directional-provenance.json逐项记录源哈希、派生哈希、含端点裁切盒和改变坐标数；逐坐标比较证明裁切范围外所有状态完全不变，范围内仅变为空气。directional-browser.json校验浏览器实际读取字节与该向派生NBT一致，并记录相机和PNG哈希。

|方向|近侧裁切与梁裁切（含端点；所有方向另去Y20顶板）|派生NBT SHA256|
|---|---|---|
|north|[[[0, 3, 0], [34, 19, 1]], [[4, 19, 4], [30, 19, 6]]]|8992e008119b10151178a74a935bdc1be51e9cd619272a858979775034a619fd|
|east|[[[33, 3, 0], [34, 19, 34]], [[28, 19, 4], [30, 19, 30]]]|d031eb087a9fc0236b5db4cc4b834bda1a23bd6de4f691c6918927afa9832f4c|
|south|[[[0, 3, 33], [34, 19, 34]], [[4, 19, 28], [30, 19, 30]]]|a7c207b0273f09ddaff1a0a8198187b4d7ab494af6e6b10fcb6bacd4138230b8|
|west|[[[0, 3, 0], [1, 19, 34]], [[4, 19, 4], [6, 19, 30]]]|812c6f0cd6743c0bf8658ab85ff9fa08ad34f2f76f36025490eee4f255a688eb|

已实际查看directional-four-view-contact.png：北向见入口白阶、中央晶核和对面南拱；东向见祭台侧面、白阶位于画面右；南向见祭台背面和对面北拱；西向见祭台侧面、白阶位于画面左。四向均可看到内部，不是旋转同一个剖面。各原尺寸图在directional/<方向>/lodestone-<方向>.png。

完整外壳仍为原最终room.nbt；所有inspection-only-*.nbt仅供检查，绝不能进候选或部署。

补图复跑：先在minecraft-builder-skill目录启动：

`node scripts/preview.mjs --nbt "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-01/incoming/S8R_IMG01/room.nbt" --port 5588 --no-open`

若该端口已有同一bundled Lodestone预览服务，可复用。再在本补证目录使用上述完整Python路径运行directional_verify.py；四次构建均显式--no-block-state-updates，保留裁切外原状态。浏览器仅在当前页面拦截NBT响应为对应方向检查件，并临时调整相机，未改服务磁盘文件或正式NBT。directional-commands.json为全部构建/截图精确命令及方向环境变量。

最后运行summarize.py更新说明与evidence-checksums.json。此补图仍不是实际世界放置/游戏截图，HOLD是否解除仅由独立复核方决定。
