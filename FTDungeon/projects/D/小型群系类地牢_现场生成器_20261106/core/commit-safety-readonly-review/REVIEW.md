# runtime next-commit 持久化与显式提交：只读代码审查

审查 VERSION：`runtime-v5-explicit-commit1-envelope-v3-derived07`。源码逐文件 SHA256 与原文保存在 source-lock.json、snapshot/；结束时再次比对当前活动目录，结果见 source-drift.json。只在本 core 新目录写入证据，没有修改 runtime、旧 core、正式 mods/saves，也没有发送消息或启动世界。

## 具体发现：P2，命令路径的 journal 异常未撤销活动状态与释放票据

范围：UnifiedEngine.java:208–217（pause）与 :592–603（commit），以及 DungeonMod.java:40–49 的命令异常捕获。两处属于同一类异常清理缺口。

1. 对正在运行的任务执行 pause 时，:211–213 清理提交标志并先调用 persist；只有 persist 正常返回后 :214–215 才 release 并清空 running。JournalFile.write/AtomicFiles.write 都显式传播 IOException，因此日志写失败会跳过这两步。DungeonMod.execute 只报告 Rejected，未停机或隔离；下一次 LevelTickEvent 仍会调用同一个 running 的 tick。结果是暂停命令因 I/O 失败返回后，任务仍活跃、票据仍保留，后续 tick 可以继续执行。这是暂停失败路径的清理缺口，不是声称日常成功暂停会继续写入。
2. commit 在 :599 先调用 build，成功后该任务已 acquire 票据并设为 running；随后 :600–602 设置 receiptHash、commitRequested=true，再 persist。若这次 persist 抛 IOException，命令同样返回 Rejected，但任务仍运行、提交意图仍为 true。后续 tick 不会因为命令异常自动撤销提交，若其余读回与存储恢复成功，仍可到达 :318–329 的 COMMITTED 分支。这里没有绕过显式操作者授权或 receipt 校验；问题是失败返回后的内存状态与错误恢复不一致。

证据：probe_commands.py 从已锁定 UnifiedEngine 原文直接提取 pause/commit 方法（不改方法内容），以 stub 替代依赖，仅在 persist 注入 IOException。Java17 编译并执行，probe/result.log 显示两条异常均 releaseCalls=0、running 保留、ticket 保留；commit 另保留 commitRequested=true。方法文本哈希见 probe/extracted-methods.json。该探针验证异常分支控制流，不模拟 Forge 区块、票据实现、世界存储或真实 COMMITTED。

建议修复方向：命令级异常退出时显式释放票据并清空 running；commit 的最终持久化失败时撤销内存提交意图/receipt。将持久化失败的错误报告与活动状态清理分开，避免清理依赖下一次 persist 成功。此处仅提出审查意见，未修改 runtime。

## 已核对的其余边界与有限结论

| 检查点 | 原文证据 | 本次结论 |
|---|---|---|
| chunk save → completed → journal → checkpoint log | UnifiedEngine:267–272 依次执行 save(true)、提升 completedThrough、persist、LOGGER.info | 调用顺序正确；日志只在 journal 正常返回后输出。源码调用顺序不能代替实际强杀/磁盘持久性结果。 |
| BUILDING 的 cursor | UnifiedEngine:253–265 提前持久化 ownedThrough；:418 在 BUILDING 写 completedThrough 而非当前 cursor | stage 切换等中间 persist 不会把尚未过 save 检查点的 cursor 记成已完成。owned 表示授权重放边界，不宣称 durable。 |
| pending BE 的重放边界 | PayloadWorld:17–27；UnifiedEngine:345–351 | completed 范围先严格/语义读回，不修补已完成 BE；pending 允许空位置，或 owned 内相同 state 且目标 payload 匹配，或相同 state 的原生默认 BE。非默认外部 payload 不被覆盖。此结论不把状态相同的外部写入解释成可区分的 provenance。 |
| 写入与操作身份 | Placement:95–106；UnifiedEngine:168–179 | 操作按类别/Y/Z/X 排序，摘要包含 state 与 payload；恢复 build 核对当前 manifest、operation digest、bounds 和 cursor 区间。未发现该路径新增具体缺陷。 |
| COMMITTED 的实际入口 | UnifiedEngine:314–329、:592–603 | 普通构建只进入 pending；显式 commit 要求 idle pending、确认词与 receipt。最终读回完成、TransitionVerifier 结束并再次 save 后，重验 receipt 才写 COMMITTED。没有把 synthetic receipt 测试当作实际世界提交。 |
| receipt 与挑战绑定 | UnifiedEngine:571–589；CommitEvidence:17–39 | receipt 绑定世界路径/seed/dimension、instance、VERSION、安装 JAR、manifest、operation、plan 与 challenge；十个 gates、报告文件哈希及零 blocker 均检查。报告内容的真实性依赖显式操作者选定的独立执行者，这与已有协议一致，不另加签名基础设施要求。 |
| 重启绑定 | UnifiedEngine:80–108、:325–327、:399–424 | COMMITTED 加载时验证 stage、非空 receipt 和当前 artifact/receipt/report 绑定；中断 precommit 的 commitRequested 没有序列化，重启默认 false，重新 build 完成后生成新 challenge。restartValidations 计数本身不证明实际重启验收。 |
| 一般异常释放 | UnifiedEngine:181–190、:334–341、:373–391 | acquire/build、tick 与 close 的常规异常路径有 release；tick 的 persist 再失败会置 loadFailure，之后仍执行 release。命令 pause/commit 的缺口见唯一发现。未把未证实的 removeRegionTicket 自身抛错另列为问题。 |
| 外部冲突隔离 | UnifiedEngine:606–615、:345–351、:354–370；DungeonMod 破坏/放置/爆炸/活塞/桶事件 | 已观察的相关外部事件清 receipt/提交意图、换 challenge、标记 FAILED/ROLLBACK_REQUIRED，活动任务释放票据；恢复前检查全部操作和实体。流体事件不取消，并由计数/后续 tick 隔离。未声称覆盖任意绕过 Forge 事件的第三方写入。 |
| journal framing | JournalFile:15–43；AtomicFiles:25–39 | 帧长度、SHA256 和 pending→atomic move 有校验；先 force 临时文件。此审查不将它升级为掉电保证。 |

除上面一个命令异常清理问题外，在所列边界未确认额外具体缺陷。最终强杀、真实区块保存与实际 COMMITTED 后重启由 integration 对精确最终工件执行；本目录的 worldTests=NOT_RUN，实际 COMMITTED=NOT_EXECUTED。若活动源码改动，仅需按 source-drift.json 的受影响文件及其调用关系更新该审查，无需将未受影响文件重新列成门槛。
