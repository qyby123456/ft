# 苔藓古林生成器

基于正式 FT 0.24.2，独立工作目录，不编辑三生成器合并源项目。基线安装 JAR 哈希见 baseline.json。

- A：moss/generator/Planner、Catalog、PrisonGridGenerator 及 data/ftmossgen/structures、catalog、reference-test 和 design。制作四层细胞递归布局与苔藓古林实际模板。
- B：moss/generator 其余运行时及新增 MossDungeonGenerator。复用 FT 门，Boss 真实玩家击败后开启折返门。
- C：src/mossTest、verification/moss、tests-contract、使用说明。独立核验和真实 Forge GameTests。
- 主线程：build.gradle、模组元信息、维度与符文显示资源、菜单注册、FT 门最小路由修改、合并打包和部署。

最低主楼层排除独立 Boss 场馆；最远按出生点至房间门口图的最短可行走距离计算，稳定处理并列。原房砖与装饰不能被刷怪点或连接覆盖。FT 门预留三宽四高、三块支撑和两侧通行空间。不同实例不得互串。

gateTest sourceSet 已加入 src/mossTest/java 与 resources，运行 namespace 加入 ftmossgen。测试辅助 Mod 元数据仍以 -PgateTestMetadata=true 临时加入 ftgateintegration。

每次验收区分静态、原生 Forge 测试、安装版测试、客户端实测。不得从旧版本继承新风格已通过的结论。正式存档不用于生成器测试。
