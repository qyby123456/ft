# 调度线程交付复核与继续记录

2026-11-06用户要求继续后，已通知integration使用新到齐的三个组件交付，不再停在旧runtime WAITING。

## 本线程实际检查

- audit_handoff.py：core20份、catalog56份、runtime41份交付文件SHA-256一致，ready文件读取期间未变化。
- runtime候选JAR：`2648e54f72169f7722852469f3aeaec5861676fe41e61de0f59eb04e90ec200d`；ZIP CRC通过，包含真实core类、两个真实房间资源及相符模板哈希，不包含GameTest测试类。
- 重新运行Java17 core测试：94932断言通过，200种子产生200种布局，200种布局均有分支。这里包含人工单测模板，不能等同200个真实存档已通过。
- 重新运行catalog测试：7项通过，确认真实NBT、原独立验收证据、实际接口、四旋转坐标与排除策略。
- 只读核对runtime生产者日志：实际Forge世界2项通过；两个不同进程的干净中断与显式恢复通过。这是生产者证据复核，不冒称调度线程独立世界测试。

命令输出分别保存在handoff-audit.json、core-tests-recheck.log及catalog-tests-recheck.log。原组件目录未改动。

## 独立集成尚未完成的事项

integration首次冻结快照20261106_131217_940590已构建候选并运行5项实际世界测试，其中两项独立测试失败，具体为Planner and runtime instance identity differs。其余生产者测试通过不能抵消独立失败；非零测试进程也不意味着Forge没有启动。已将明确错误反馈给integration，要求核对实际请求/版本/种子与fixture并保留失败证据。

后续状态以integration/progress.json、test-matrix.json及保留日志为准。本记录是当次检查快照，不随线程进展伪造更新。尚未确认独立全部通过或发布JAR安装后实测，不作正式部署许可。

## 仍然存在的功能限制

目前模板池只有S8-05直廊和S8-06转角；可以按种子组合多房，但不是九房完整版或八群系生成器。只允许独立隔离世界高空纯空气区域，拒绝额外模组，拒绝方块实体/实体载荷以及未支持的液体/动态方块；没有战斗奖励、入口返程完整玩法，不保证强杀/断电恢复或ACID回滚。此版本不能直接安装到当前FarmingTales整合包使用。

本次没有修改正式实例mods、配置或saves，也没有打开玩家世界。
