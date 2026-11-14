# 433 新建缓冲区真实专服验证

使用432累计候选。实际NeoForge 21.1.233 / Minecraft 1.21.1隔离专服进程78672退出码0，探针失败数0，通过stdin stop正常保存退出。全部1381产品类与绑定逐字节一致；148份物理归档、121种内容的冻结哈希与ZIP CRC通过。

正向原生箭矢命中样本确认：玩家初始缺失FTBossRangedProcTimers；Book预规划原根身份正确，初始缓冲区null；本次真实getCompound新对象通过构造来源验证并绑定，最终persistent data中保存的正是这个对象。非空计时器Group acquired=true、rooted=true、done=true，Session.next=4，四项原始操作已关闭。玩家生命10到11，远程计时器仍LastSkillProc.blood_feast:80L。第二个未知投射物加入修改样本仍prepared_unsupported=true、carrier_retired=true、无coverage，且没有新对象绑定。

tools/seal_ranged433.py逐项断言上述正反例，输出reports/actual-ranged433-sealed-experimental.json。完整signed Session仍未通过：traceRevoked=true，firstObservationFailure和firstFailedStep均为空，说明下一步需取得具体首次撤销调用位置；不能将Group完成等同完整Session完成。

74覆盖、原生8/9/60、43项整父方法克隆和四项调用者反射拒绝、六类武器准入、十二种非保留物品旧数据拒绝、176次旧附魔无消费拒绝继续通过。6104条配方结果、160项插槽指纹、五项基础规则指纹和192项原生组件物品编码对照继续通过。测试玩家不代表真人认证联机；十项缺失外部配方没有因此解决。客户端GL、完整修复/支付等要求仍未完成，NOT-INSTALLABLE，不部署。
