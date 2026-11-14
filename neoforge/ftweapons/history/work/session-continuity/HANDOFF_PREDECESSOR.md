# 必须换线：功能移植交接，2026-11-10

旧线程01a1ab89-ceb8-7ea0-ae8e-a1e4c83f400f已撤销写入权。当前没有实现写入者，必须由新线程先只读核验本检查点，再按AGENTS.md和WRITE_OWNER.json取得唯一写入权。旧线程不得因自动目标续跑恢复写入。

本次因上下文已承载大量历史与实现细节而主动安全换线；没有反复尝试上下文压缩。收到换线规则后停止新增实现，仅等待既有定向命令结束、只读检查结果，最后写本交接与所有权记录。

## 当前目录与硬约束

- 唯一续接工程为C:/ft工程文件C/FTBoss-NeoForge-1.21.1-移植-20261022；D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022为只读依赖和历史证据，不要删除或迁回D写入。
- 正式Forge/NF实例均只读；NOT-INSTALLABLE，不部署，不采用实时漂移，不改玩法数值或Independent Event。
- 保留剑、弓、弩、斧、三叉戟、法杖六类；删除其他旧武器适配和FTBoss食物附魔，保留原版全局附魔。旧协议、目录、翻译残留裁剪尚未全部完成。
- 最新用户要求：以功能缺口推进，冻结输入哈希未变的已有结论直接引用。每次只做必要编译、定向行为检查、相关类链接/ASM和失败半写入检查。完整ZIP/CRC、全量NF、客户端/专服/多人和四向GL集中在里程碑/最终候选。不要再用观察器、守卫层或批次编号代替功能实现。

## 最新累计候选与功能改动

当前候选不是旧README所述435，而是work/session-chain/reports/actual-current.json；1381类、716源码路径、208守卫，没有增加守卫。

候选构建目录：work/session-chain/build/current-a1b294d2。完整有效源码参数为该目录complete-arguments316.json，产品类为complete-classes。以参数清单为实际有效源码依据，不要修改D输入或误改旧批次。

功能源码：

- work/session-chain/proposal/net/farmingtales/ftboss/item/OwnerFullRepairGuard.java
- work/session-chain/proposal/net/farmingtales/ftboss/item/NativeExpectedFieldObservation.java

436已明确首次撤销来自FoodWeaponFinalDamage.publishListedProducer第2106行selectPublishingWriter失败；发布后玩家字段域retired。旧字段变更钩子把合法原生远程计时器putLong/put也视为外部改写，破坏下一个生产者的连续性。

本次实际修复：在既有NativeExpectedFieldObservation作用域中识别当前独立预规划、原根/缓冲区、步骤、参数、字段前像、同线程、原生注入入口均一致的远程写入；只对这两个具体原生入口保留玩家字段域，其他字段变更与武器/别名/逃逸撤销通知继续执行，原有写后验证保留。只改变一个旧方法OwnerFullRepairGuard.changed，新增一个功能判断方法plannedRangedFieldMutation，没有新增观察器或证明层。

工具位于work/session-chain/tools：implement.py、check_functional.py、prepare_current.py、build_current.py、prepare_milestone.py。后续不要盲目重跑生成器覆盖手工修复；先读源码和收据。

必要检查已经完成：编译通过，十二项直接/反射/MethodHandle越界调用不获准，十六个相关类ASM与-Xverify:all通过；其余旧方法体保留。旧输入的全量守卫、ZIP/CRC、195/196/197证据不重复执行。

## 已结束的真实定向验证

work/session-chain/runtime/tools/run.py已结束，exec session 13007已消费终端结果。Java PID8368已退出，没有本工作目录相关Java/Python残留进程；所有本次构建和只读等待命令也已结束。

运行目录：work/session-chain/runtime/build/focused-storage308-bb64e30c。

终端：exitCode=0、stopSent=true、completeSave=true，探针failures=0。自动通过stdin stop保存退出，没有强杀或部署。实际NF21.1.233/MC1.21.1，三个正向与一个负向场景均为测试玩家，不是真人认证多人。

注意路径：work/session-chain/runtime/reports/actual-coverage436-read.json只是复用旧工具的文件名，不是历史batches/436-native-revocation-origin-runtime报告。必须使用此完整新路径。

结果：

| sample | 输入 | 实际Session快照 | 字段域 | 原生效果 |
| --- | --- | --- | --- | --- |
| 0 | 初始缺失计时器键 | next=4、coverageInstalled=true、traceRevoked=false、非空组acquired/rooted/done=true | player/carrier retired=false | 生命10→11，LastSkillProc.blood_feast=80L |
| 3 | 已存在复合键，旧计时值5L | 同上，无首次失败或撤销 | player/carrier retired=false | 生命10→11，计时值80L |
| 4 | 原键为int 19 | 同上，无首次失败或撤销 | player/carrier retired=false | 生命10→11，替换为计时器复合键80L |
| 1 | 未知投射物加入修改 | unsupported=true、coverage=false、traceRevoked=true、组未完成 | player/carrier retired=true | 原生路径仍产生生命10→11及80L，不应宣称失败后原生效果回滚 |

三个正向都有真实quotePresent/originPresent、programUnsupported为空；三名已列生产者全部执行到producerOrdinal=2，OriginalRun.terminal=true、originalFailure为空、activeProducer/activeLeaf为空，发布关闭。首次生产者交接撤销在这三个正向中已消除。

重要限制：没有实际调用Session.traceComplete作最终断言；旧探针顶层positiveSignedSessionAccepted=false是继承的固定保守字段，不能把它当作三条Session快照仍被撤销的证据，也不能直接改成true冒充验收。完整read projection/lease/repair/payment链没有因此全部通过。

## 输入/输出哈希：新线程只读核验这些检查点

| 文件（相对工程根） | SHA256 |
| --- | --- |
| work/session-chain/reports/functional-compiled.json | ee993e77ff402fc2ddb4dfcb1507aa392d55855fdaee0ddd8b165b6580458c3b |
| work/session-chain/reports/functional-checked.json | d0675b048ba4a772d4fd8b0f15f4b0e7925f141c31a34f22e3a4a274488ce2b3 |
| work/session-chain/reports/actual-current.json | ccf1096fe2503d2385f3ee34e000981b85d09e9bf649c9b40cb11cab30aae6bc |
| work/session-chain/runtime/reports/actual-coverage436-read.json | 1fa495df217e5a29b7dc7b666af016ecf40d0b1064dc07ad03103685c12fbee6 |
| work/session-chain/runtime/build/focused-storage308-bb64e30c/terminal308.json | c890c204fb54936786818b4d6a04c11fa92702fcd71543b3c70de7f082382772 |

## 失败命令与半写入检查

首次implement.py因匹配到多处旧文本而在写首个目标前中止；已收紧到含Node声明的唯一位置后成功。首次build_current.py因生成器未替换转义Windows路径而找不到原型报告，在累计构建创建前退出；已改为显式绝对路径并成功构建current-a1b294d2。没有把这些失败视作通过，没有仍运行的失败命令。

成功候选和运行输入收据完整，运行工具在退出前校验了本次输入哈希。运行四场景都已输出最终原生武器/玩家状态，负向场景的原生效果如上明确保留；没有声称事务已全局回滚。不要为“清理半写入”删除D证据或正式实例文件。失败尝试日志保留供追溯。

## 新线程优先继续的功能工作

1. 只读核验上述哈希、终端与进程状态，确认没有其他写入者，然后按所有权文件协议取得唯一写入权。冻结输入未变就引用已有结果，不重启本次已结束运行。
2. 基于当前真实三条正向Session快照，直接完善功能验收：实际traceComplete、同一玩家同一武器重复命中/跨次Session、read projection与lease延续。特别检查OwnerFieldExpectedAuthority.DomainBefore.expected为final初始值、prepare使用该expected的后续命中语义；本次没有测试同一玩家第二次命中，不能假定连续性已全部解决。
3. 对当前活动scope内错key/receiver/别名/异常注入做必要定向负例并核查半写入；十二项现有越界测试主要为scope外调用，不能替代活动scope负例。
4. 接着实现并贯通repair/payment及完整正向signed Session，随后补provider内部修改撤销、十项外部配方缺口。最终再集中做完整真实环境、客户端/专服/真人多人、四向实际GL、实例闭环与ZIP/CRC。保持原始全部要求，完成前NOT-INSTALLABLE。

195冻结基线、196/197交接证据与参考快照最近只读核验见reports/frozen-handoff436-audit.json。历史435/436报告保持原样作为旧证据。旧线程在本文件与WRITE_OWNER.json落盘后只读，不再修改源码、运行工具或任何累计候选。
