# qys-economy-merged 2.0.23 团队总卡顿修改记录

日期：2026-10-17，Asia/Shanghai。

## 基线与交付

- 用户指定文件：D:/ft工程文件/FT经济-波动mod成品/qys-economy-merged.jar。
- 原版 2.0.22 的 SHA-256：7F2FA5A8DA1F901D4480761F0DA5D9CCABC2612364790166E602AE1697E760B2；与现有工程原构建输出完全相同。
- 新版 2.0.23 的 SHA-256：E5CF3F516DBA1E54807A46CD0CA6F16DB99A47FEABFBB93203D17E67195A7699，149,813 字节。
- 已更新上述用户指定成品及同目录 Merged-Economy-README.md；工程 release/2.0.23-team-total/ 中保留相同成品。
- 原成品和说明备份：D:/ft工程文件/FT经济-波动mod成品/backups/before-2.0.23-team-total-20261017/。
- 原源码与成品备份：D:/ft工程文件/FT-Merged-Economy-Mod/release-backups/before-2.0.23-team-total-20261017-163817/。
- 没有向任何 Minecraft mods 目录安装此经济模组，也没有修改正式世界、现用配置、KubeJS 磁盘脚本或 ftboss。

## 关键行为

团队当前所有维度认领区块的有效实测 μs/tick 求和，作为团队生产限制预算，不除人数或区块数。一次评估一个团队算一个连续样本，总量作用于它的所有认领区块；未认领区块及 FTB 不可用时回退原单区块判定。默认阈值 500/800 μs/tick，原自定义阈值与评估周期不变。已有红色冻结不自动解除。

新增管理员命令 /lagcontrol releaseteam [团队UUID]；无参数时取玩家脚下区块的认领团队，一次解除团队当前全部领地并进入原有保护期。旧逐区块解除保留。每日团队人均收入扣减、财富与出货箱逻辑不变。

原配置中的冻结清场规则保留：如果启用了清除掉落物/非 boss 生物，团队总量冻结后该团队其余受限领地的已加载区块也执行同样清场；这是范围扩大的直接影响，不是仅暂停 tick。安装前应阅读 README 并备份服务器世界。

## 验证

- Java 17、Gradle 8.8、Forge 47.4.10，正式构建和 reobfJar 成功。
- LAG CONTROL CHECKS PASSED: 30198。
- All 31 required tests passed；11/11 原 Mixin 注入自检生效。
- 实际加载 FTB Teams 2001.3.2、FTB Chunks 2001.3.8、Observable、Create、KubeJS 等依赖，使用独立 GameTest 世界。
- 新增真实认领用例验证：跨主世界/下界 450+450=900、不同队伍隔离、无采样领地一并受限、团队连续样本数、冻结存档往返、同团队升级仅一次与共享冷却、局部/整队解除保护期、释放区块仍计入总量、转移领地按新所有者判定、黄色限制恢复。
- 远端认领区块在评估与冻结清场前后保持未加载；新逻辑不强制加载区块。边界粒子每玩家每 40 tick 最多查询 49 个邻近区块，不再遍历全服所有限制。
- 发布 JAR 检查：两个兼容 mod ID 均为 2.0.23；包含新增生产类，未包含测试类、脚本快照、FTBoss 内容或依赖 JAR；成品副本 SHA-256 一致。
- 相对备份运行 git diff --no-index --check，无空白格式错误。

完整构建/测试日志：verification-2.0.23-team-total-final.log。未做真实多人服务器长期 TPS/内存压测，不承诺所有第三方私有/异步耗时均可测量。FTB 所有权变化在下一个既定评估窗口生效，不额外注册高频领地扫描。
