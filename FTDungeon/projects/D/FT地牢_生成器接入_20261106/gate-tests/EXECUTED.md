# 实际执行结果

隔离快照的 main + backend library sources 编译成功，generatorGateVerification 夹具编译成功。实际 Forge GameTestServer 运行 1 个必需测试，29 个断言全部通过，服务端正常退出，Gradle BUILD SUCCESSFUL。不是只读源码审查结果，也不是普通安装发布 JAR 的独立验证。

执行目录为本 gate-tests/stage；主工程和 backend 仅只读复制。只合入 backend/src/main/java、core-snapshot、data/ftbd_candidate，未合入后端 harness、build 或 mods.toml。execution-input-lock.json 对实际读取的672个源文件/资源/库记录源路径、目标及 SHA256；result.json 额外列出5个门关键源码摘要及测试后活动源变化，供最终 main 对比。

实际输出：`INDEPENDENT_GENERATOR_GATE_CONTRACT_PASS checks=29 fakePlayer=true actualNetworkUI=false` 和 `All 1 required tests passed :)`。编译日志在 compile.log，完整启动/测试/退出日志在 gametest-attempt1.log，启动期间 Java PID/父PID/命令在 processes-attempt1.json。只运行一次该 GameTest，不把无关模组资源警告归为门逻辑失败。

覆盖：旧 SavedData/BE rooms 默认；两门选择隔离与 native save/load；round generator 存取、锁定与完成解锁；未知 ID 不 fallback；第三 provider 菜单列入；非法 index/远程/跨维度/旁观/round 锁拒绝及正常选择；BUILDING、FAILED 确实进入 provider 后均无teleport/round/返程覆盖，未知 provider 无fallback/onEntered/状态改动。

局限：FakePlayer 与反射构造真实服务端 menu 不等同实际玩家网络 UI；save/load 是 NBT 序列化，未声称跨 JVM 磁盘重启；未验证 READY 成功传送或 backend 长测。首次准备文档 prepared.json/REVIEW.md 保留其当时 NOT_RUN 状态；当前执行结论以 result.json/ready.json 为准。ready.json 封存源快照、夹具、编译产物、世界和日志等文件，排除可变 Gradle 内部 .gradle 缓存。
