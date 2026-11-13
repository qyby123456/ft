# 独立扩展候选 v2：完整晶洞群系缺口与最终 core 重测

本目录与既有两模板冻结池隔离。未改 catalog 根目录 ready/progress/manifest、data 资源或旧验收哈希；未改其他组件、原72房或任何实例。所有写入位于本 expansion-v2。现有基线每个清单文件都重新校验，锁定结果在 snapshot-lock.json。

## 已交付事实

- 真正解码基础轮72个独立单房 NBT，复核原验收 SHA、原始接口元数据、显式方块实体完整载荷、容器方块但无显式载荷的情况、流体、水浸和动态材料筛查。完整逐房数据在 all-72-expansion-audit.json；各来源绝不使用10号总览、11—20或整组合并NBT。
- 按**当前 runtime 快照的实际 STATIC_BLOCKS 白名单**筛查，仍只有 S8-05 与 S8-06 无缺口。未发现可直接提升进现有运行时的第三个原字节模板；没有以删除家具、抹掉容器NBT或抽走流体来伪造兼容。
- 完整晶洞群系9房清单在 s8-complete-biome-requirements.json。无流体的7房原字节候选已实际复制在 candidate-resources，且逐个验证 SHA。candidate-manifest.json 的7项一律 accepted=false：它们仍须运行时新增能力及独立世界验收，**不可直接替换正式manifest**。其中真实入口S8-07、大厅S8-04、宝库S8-09都保留。S8-01需要入口台阶几何支持，S8-08需要熔岩/藤蔓专项支持，未混入这7个候选。
- 当前最终 core/ready.json 所列3个源码已按其SHA逐个校验后捕获；不是旧 catalog/core-snapshot。新 core-snapshot 内附准确ready与源码。实用JDK17.0.8执行32种子、每次6房5连接，逆序输入重现、两种真实房间抽样、32种布局通过；只测试现有正式两模板，不声称7个扩展候选已经世界/规划验收。

## 完整晶洞角色与精确阻塞

|真实房|用途|不改变源字节的主要支持需求|
|---|---|---|
|S8-01|祭坛厅|北口(17,1,0)内一格已有楼梯，现有严格直角净空检查不成立；不能拿合并版坐标代替原源|
|S8-02|藏书储藏|1个chest方块(7,2,21)未携显式NBT，也仍需原生创建默认箱BE；另有门/木材/玻璃白名单|
|S8-03|小宝箱房|1份minecraft:chest显式载荷，台阶/slab白名单|
|S8-04|主大厅|8份minecraft:banner显式载荷，紫色玻璃/墙旗白名单|
|S8-05|直廊|现有基线；不能冒充主大厅|
|S8-06|折角|现有基线；不能冒充宝库|
|S8-07|真实入口|6份显式载荷：barrel/banner；保留原北口Y2、南口Y4，门/木材/装饰白名单；不能套合并布局Y1/Y3|
|S8-08|庭院|9格真实lava、藤蔓；流体封装、更新顺序、传播和世界tick验证，以及墙体/藤蔓白名单|
|S8-09|宝库|8份显式载荷：chest/barrel/banner；其他木材/花盆等白名单。1.20.1花盆是方块状态，不误报为BE|

block-entity-support-request.json 提供上述7房每个载荷的**原坐标、方块名、完整NBT、精确类型、原SHA、独立验收快照及要求**，不只给泛泛能力名称。原验收是建筑/逐张制作许可，不等于扩展运行时通过。7房候选有入口与终局role，仅表达未来目标；accepted=false与未验收限制不能删除。

运行时必须保留原显式载荷、创建未显式携带NBT的原生默认BE、旋转状态和坐标、持久保存施工前/后完整BE、做重启/幂等/回滚所有权校验，以及读回原生归一化NBT。读回不能把MC补默认字段误判为源字节不一致，也不能忽略Items/Patterns/朝向等语义。箱子相邻合并、旗帜、桶/箱交互和中断恢复均需四朝向隔离世界验证。扩大白名单与BE支持是两个独立需求。

## 筛查范围说明

requirements计数是能力缺口而非源文件不合格数量。接口检查复用现MVP有限的实体全方块支撑白名单，**对其他群系的非白名单地板报告的是未被该MVP证明支持，绝不等同原接口实际堵塞**。dynamicScreen是保守的已知材料标记，不是完整原生isRandomlyTicking检测。blockEntityBlocks由显式NBT位置及列举已知容器/旗帜等方块名识别，不声称替代所有Minecraft注册表能力查询。各类问题仍需MC原生测试；不从文本推断全体世界安全。

## 测试与消费

从本目录使用 C:\Users\YOUR_USERNAME\.codex\.venvs\imagegen\Scripts\python.exe -B 分别执行 audit_expansion.py、prepare_candidates.py、test_expansion.py。它只是已有Python解释器，不调用任何生图API。audit_expansion读取源码只读，并捕获当前core/runtime新快照；因此重跑audit后，必须重新编译该快照并执行Java smoke，再跑测试。

Java命令：javac --release 17 -encoding UTF-8 -d test-classes core-snapshot/DungeonApi.java core-snapshot/DungeonGeometry.java core-snapshot/DungeonPlanner.java CatalogCoreSmoke.java；然后使用 C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot\bin\java.exe -ea -cp test-classes CatalogCoreSmoke。core-compile.log及core-smoke-java17.log记录实际结果，snapshot-lock.json和交付inventory提供源码锁。

**扩展ready仅表示清单/精确候选/需求与测试证据可消费，不表示扩展资源可发布。** 原正式两模板ready继续有效。新增候选不进入现有integration基线，需runtime支持后另行构建和独立验收；完整9房仍有入口台阶和庭院流体阻塞。没有启动游戏、修改世界或修改全局gamerule。
