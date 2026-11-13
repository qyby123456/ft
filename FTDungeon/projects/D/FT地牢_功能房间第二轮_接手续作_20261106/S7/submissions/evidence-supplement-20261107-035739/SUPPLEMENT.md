# S7 补充证据提交

本次不改变十房NBT、合并NBT或15个分片；合并SHA-256仍为 0d1d19064a170e607c47c3278200eaf7a937fadbc65b73ee2e06ea20524829d5。

上一清单声明的渲染脚本哈希与补近景后脚本不同，旧清单原字节保存在prior-ready-for-review.json。changes_from_prior_manifest逐项记录旧声明与当前捕获哈希，不将其伪称一致。独立验收方已经冻结的03:46证据不修改。

新增12 guard-detail、13 prep-detail、15 dummy-detail、16 channel-detail，均为原候选的真实Lodestone渲染，已实际目检；无新NBT候选。该版本目录中的证据为本次捕获副本，后续整改必须另建版本目录。

20号拉杆旧缺陷已由独立复核关闭；14号三实体和装备证据保留。独立报告原路径与哈希见independent-evidence-update.json。

范围纠正：totalIssues不统计state_changes，火/流体只遍历源显式记录；独立前后NBT检查补足的是结构内部方块类型/遗漏格，没有外部传播证明。setOpen是API往返，非玩家输入/拉杆操作；世界测试未被父线程独立重跑。

仍待：独立最终视觉与合并验收、11上层玩家攀梯、16侧阶/小菌群整改、真实玩家开箱与完整往返、合并结构动态更新。group_accepted=false。
