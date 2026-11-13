# S5-01 世界更新补证（R4）

**交付状态：测试执行完成，待独立复验；不自放行，S5-02仍锁定。** 冻结NBT保持 `9450884a348eda61ea55f0f11a8dae87a0f7d5183312b1528d9a519449f6b9a2`。没有修改候选几何、配方或正式实例。

## 审计与复用边界

只读检查旧通用启动器 `FT地牢_Image25接入_20261101/work/nature-redo-b01-s1-01-20261104/source/validate.py`、GameTest类与Gradle配置。复用了已缓存的Java17、Gradle8.8、ForgeGradle6.0.54、Forge1.20.1-47.4.10及通用空GameTest模板；没有复制旧建筑、旧候选或任何旧世界。新的WaterTests.java仅针对本冻结NBT编写。每轮均独立新目录、新世界。`--offline`完整命令、工作目录和参数在invocation.json；任务中download字样是Gradle任务名，不是下载未知JAR的证据。

## 必须保留的失败/不足证据

- `../S5-01-worldtest-20261104`：四向均跑1200tick、randomTickSpeed=3，液体/邻居更新未见后续差异；但没有证明自然随机刻被激活，不能作为植物随机刻PASS。
- `../S5-01-worldtest-R2-20261104`：加入原版小麦随机刻探针，四向均为0生长，覆盖检查故意失败。只有强制区块加载不足以激活本场景自然随机刻，不能把gamerule=3冒充已执行随机刻。
- `../S5-01-worldtest-R3-20261104`：尝试原版GameTest模拟玩家，Forge环境触发空Netty channel异常；tests.xml保留具体失败。不是结构失败，不计作1200tick测试。
- 本R4使用Forge FakePlayerFactory，将非旁观创造玩家加入ServerLevel并固定在各水槽上方40格；正常ServerChunkCache随机刻路径被激活。没有直接调用randomTick、没有手动催熟或骨粉。上下两个结构高度区段均有小麦生长，证据在四份report.json。

## 夹具与执行参数

检查体积为51×30×51（相对原点X/Z=-8..42，Y0..29），外壳位于X/Z=-9/43及Y30，额外底壳Y-1。Y0..2为连续海晶石砖；Y3..29预填源水。原点Y160，四水槽X=1024/1184/1344/1504，Z1024，互不重叠。四向旋转均由原版StructureTemplate.placeInWorld实际放置，旋转枢轴按先前归一化契约偏移。

`knownShape=false`，flags=`Block.UPDATE_ALL`即3；不是照抄旧工具knownShape放置。对全部26950源结构位置显式执行Block.updateFromNeighbourShapes、需要时setBlock，以及updateNeighborsAt；检查体积内所有流体位置显式scheduleTick(delay=1)。保留before-explicit-updates.nbt、after-explicit-updates.nbt、after-1200-ticks.nbt三份51×30×51世界回读。

randomTickSpeed=3、doMobSpawning=false；每向1200个实际ServerLevel.gameTime增量，GameTest会加速运行，不用墙钟秒数冒充tick数。128株原版age0小麦探针分布在水槽外、同高度两个16格区段（世界Y170/186），每层64株，海晶灯照明；探针不在51×30×51差异统计区域中。它们证明自然随机刻路径在该高度区段有执行，不证明每一株候选植物在本次有限采样内都被随机选中。

## R4 结果

|方向|实际tick|随机刻速率|下/上区段探针生长株数|放置后邻居更新差异|1200tick水槽差异|
|---|---|---|---|---|---|
|NONE|1200|3|11 / 12|0|0|
|CLOCKWISE_90|1200|3|7 / 4|0|0|
|CLOCKWISE_180|1200|3|10 / 3|0|0|
|COUNTERCLOCKWISE_90|1200|3|10 / 8|0|0|

四向settled：死亡珊瑚=0、原珊瑚方块身份变化=0、水生相关位置canSurvive失败=0、原水方块不再是源水=0、最终水槽及邻近范围ItemEntity掉落物=0。完整51×30×51前后状态差异为0，覆盖外围水环带与水盖，不只是候选内部。未声称有逐tick掉落事件记录；本测试只直接统计开始与结束实体，1200tick小于原版6000tick物品自然消失周期，模拟玩家固定在水槽上方以避免拾取。

### 不得隐藏的放置差异

每向**14处海晶石砖楼梯从waterlogged=false自动变为true**，发生在实际放置阶段，而非1200tick后的损坏。全部差异逐坐标记录在placement_expected_vs_actual；没有改方块身份/朝向/形状。这符合完整水下放置的含水适配，但不是“世界与源NBT逐状态完全一致”，是否接受由独立复验决定。后续0差异的baseline是放置完成后的世界，不是原始NBT。

## 复核入口与限制

`verify-evidence.ps1`可只读核对冻结NBT、4个JUnit通过用例、Gradle退出码、自然随机刻探针、全部差异限定和水生存活结果，并重新生成summary.json。`run.ps1`记录完整启动流程，若世界目录存在会拒绝再次运行；需要复跑时只复制src、Gradle配置、frozen.nbt、run.ps1和初始server.properties到另一个新目录，绝不复制server世界/build/.gradle或改写本证据。

本R4是封闭人工水槽与仅Forge+QA Mod隔离服务器结果，不是正式整合包混合Mod运行验证，不是干走廊接入许可，不是无限期自然海洋稳定性保证。未更改正式saves/mods/config、共享测试目录或NeoForge。旧补证中的NOT_RUN是当时历史状态，由本R4新增执行证据补充，未回写独立REVIEW.md或覆盖旧失败回执。
