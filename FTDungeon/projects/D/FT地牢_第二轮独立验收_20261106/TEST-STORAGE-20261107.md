# 已完成独立测试的存储迁移

2026-11-07约10:11，D盘无剩余空间，S5最终结论首次写入报ENOSPC；此时两测试已正常结束，所有要求断言已通过，结论写入不完整，因此未在该次失败后宣称接受。自动审批拒绝缓存递归删除后，改用保留数据的跨盘迁移，无文件删除清理。

本线程当日新建且已停止的五套独立测试：S3-supplement-20261107-094835-560734下independent-worldqa-r3-r2、independent-ladderqa-r2-r2；S4-submission-20261107-095341-476526下independent-access-gravity；S5-submission-20261107-100256-517444下independent-natural-movement/qa/natural-movement、independent-natural-detours/qa/natural-detours。

这些目录的native-test/build构建输出完整保存在C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603/preserved-test-builds-20261107-1012，子目录保留相对本验收根的原路径。需要再次构建时Gradle可从保留的源码重建，无须改动源或证据。

上述五目录的server原始世界/配置/日志完整迁至同一C盘工作根下preserved-test-worlds-20261107-1012，保留相同相对路径；各D盘原server位置建立Windows目录联接，旧证据链接及脚本仍直接访问原文件。迁移仅涉及已完成的父线程独立测试，不触及制作方目录、原冻结候选、正式Minecraft实例或其他线程测试。每个源和目的绝对路径均在迁移前确认限定于指定根，目标未存在。

约10:12迁移后D盘剩余约325MB。S5最终审计随后完整重新执行，生成可解析ACCEPTANCE.json，1016当前及974既有冻结文件复核通过，未保留部分写入为正式结论。后续大体积隔离世界应优先在本线程C盘工作根内创建，避免继续挤占D盘。
