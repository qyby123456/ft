# next-commit-iofix：只读修复复核

结论：上一轮 P2 命令 journal 异常清理缺口，在本次锁定源码的控制流范围已修复；未发现新增具体缺陷。只写本 core 新目录，不修改 runtime 或之前任何封存证据，不发送消息，不运行世界。

VERSION 仍为 `runtime-v5-explicit-commit1-envelope-v3-derived07`。UnifiedEngine.java 的 SHA256 为 `fa6202cb49527a0c79dd4248228fb8ce417f9b1a33a3567c9ff2511c8ca1f488`。source-lock.json 记录全部 27 个 Java 快照与哈希。对旧审查快照作差异比较：只有 UnifiedEngine 的 pause 与 commit 方法有变化；除去这两段原方法文本，其余文件内容相同。结束复核检查当前源码仍匹配快照。

| 分支 | 修复源码证据 | 原文提取探针结果 |
|---|---|---|
| pause 的 IOException | UnifiedEngine:213–221：catch 设置 loadFailure 后重抛；finally 先清 running，再 release | 活动任务清空、ticket=0、intent=false、receipt 为空、release 一次、runtime 停用；不依赖再次 persist |
| commit 的 IOException | :606–622：成功 persist 后才 admitted=true；catch 清 intent/receipt 并停用；finally 在未 admitted 时清 running 并 release | 活动任务清空、ticket=0、intent=false、receipt 为空、release 一次、runtime 停用 |
| commit 的其他 Exception | 同一 catch(Exception) 与 finally | 注入 IllegalStateException，清理结果相同 |
| 正常 pause | finally 仍执行清理，成功不设置 loadFailure | 清理正确，runtime 保持可用 |
| idle pause | 未进入 running==job 分支 | 不写 journal，不调用 release |
| 正常 commit | admitted=true 时保留已排定的验证任务 | 保留 intent/receipt/票据，未过早释放，仍返回 NOT COMMITTED |
| 确认词错误 | :600 在 build 之前拒绝 | 无活动、票据、持久化或提交意图 |

探针从锁定的 UnifiedEngine 源码直接提取 pause/commit 方法，不更改方法内容；依赖使用 stub。Java 17.0.8，`javac --release 17 -encoding UTF-8 -Xlint:all -Werror`；7 个场景、41 个断言通过。probe/extracted-methods.json 保存两段原文哈希，probe/result.log 保存结果。runtime 停用字段由原方法设置；实际 requireReady 的 loadFailure==null 检查位于 :441，复核其未改动。probe 对停用后拒绝工作使用同条件 stub，不冒称执行了 Forge requireReady。

本次验证的是实际方法在异常下的活动/intent/receipt 清理及 release 调用控制流。ticket 数量由 stub 模拟，证明清理分支到达和调用次数；未验证真实 Forge 票据内部实现、区块 I/O、强杀恢复或实际 COMMITTED。

旧 42 文件审查、149 文件 v5 几何补证以及旧 runtime 95 文件清单均在结束时逐项核验（实际计数见 result.json），未改动。VERSION、九模板 manifest/NBT、规划器 core-snapshot 均保持一致，因此本次 I/O 清理修改不要求重跑 129 几何请求；最终安装 JAR 与独立提交 receipt 绑定仍必须对应新工件，不能沿用旧 JAR 的验收。

复核严格限于差异影响范围，未新增世界测试门槛。worldTests=NOT_RUN，actualCommitted=NOT_EXECUTED。活动 runtime 如再修改受影响方法，应仅更新相关源码与探针复核；若 VERSION 改变，再明确完整重跑 129 请求。
