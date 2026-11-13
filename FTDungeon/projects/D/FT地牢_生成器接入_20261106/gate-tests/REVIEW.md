# 入口门生成器选择：独立夹具准备与只读审查

当前状态：FIXTURE_PREPARED_NOT_COMPILED_NOT_RUN。未执行编译或任何 JVM；等待主线程完成 main 编译并通知后，才在本目录新副本运行。不得在主源码工程应用 overlay；后续必须复制所需主项目到 gate-tests 下的独立 stage，避免 Gradle 的 build/.gradle/run 写入主工程。

源码基线及 SHA256 见 prepared.json；原文快照在 source-review/。没有修改主源码或正式实例。新增 GeneratorGateGameTests.java 单一同步 GameTest 限定 GameTestServer，测试后恢复全局 gate SavedData。测试 provider 只在隔离 JVM 注册，名称明确为负向回归夹具。

计划覆盖：

- 旧 SavedData 与旧 BE 缺 GeneratorType 时默认 rooms；两门各自选择，原生 BE save/load 保留；合法未知 ID 不降级为 rooms。
- 活跃 round 的 generator SavedData 持久化和锁定，每门 setter 锁定；participant 清空并过 admission 后结束，选择可改。
- 第三个 provider 注册并进入菜单；非法 index、距离过远、跨维度、旁观者和 round 锁拒绝；解锁后合法选择成功。
- FakePlayer 通过真实 DungeonGateTravel.use 进入 BUILDING/FAILED 测试 provider；断言 provider 调用计数实际增加，避免被门关闭/火盆/冷却等早期 guard 偶然截获造成假 PASS。验证玩家维度/坐标、round 与返程 NBT 均不变。未知 provider 同样无 fallback、无 onEntered、无 round/返程改动。

菜单构造仅通过反射调用现有私有构造器，以验证真实 stillValid/clickMenuButton 服务端逻辑；这不等于真实网络 UI。FakePlayer 不等于真实连接玩家传送验收；本轮主要验证非 READY/未知的零传送。BE save/load 与 SavedData 序列化测试不声称跨 JVM 磁盘重启。

当前只读审查：Travel 的 provider 查找与 READY 分支在返程记录写入、teleport、bind/startRound 之前；未知 ID 显式返回。Menu 逐次验证旁观/维度/距离/交互权限、真实完整门和 round 锁，entries 是创建时快照，新建菜单可见后续注册 provider。GateData 与 BE 对缺字段采用 rooms，对可解析未知 ID 保留。未确认具体新缺陷；这些只是源码结论，最终必须用实际 GameTest 结果判断。

Prepared 源码仍活动；运行前需记录精确最终源码/工件 SHA，若接口变化只更新受影响夹具，不将当前未编译代码误报通过。overlay.gradle 是独立 stage 配置草案，尚未验证 ForgeGradle 运行任务是否可直接继承；根据主线程最终可运行配置适配。
