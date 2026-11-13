# 团队卡顿纯查询版 1.0.0 验证记录

日期：2026-10-18（Asia/Shanghai）。

## 成品

- 文件：qys-team-lag-query.jar，50821 字节。
- SHA-256：0691AD0C890D65F9BBB3CFEE02843A270A79F537F715B6433A5B9949F1FF77FA
- 新模组 ID：ftteamlag；Minecraft 1.20.1 / Forge 47.4.10 / Java 17。
- 工程：D:/ft工程文件/FT-Team-Lag-Query-Mod。
- 交付目录：D:/ft工程文件/FT团队卡顿查询mod成品。
- FTB Teams 2001.3.2、FTB Chunks 2001.3.8；Refined Storage 1.12.4 / Observable 4.4.2 为可选适配。

## 构建及实际运行

| 环境 | 结果 | 计时注入自检 | 日志 |
| --- | --- | --- | --- |
| 完整环境，含 RS + OBS | build/reobfJar 成功；8/8 必需 GameTest 通过 | 9/9 | logs/full-verification-final.log |
| 不安装 OBS | 8/8 必需 GameTest 通过；OBS 专属用例跳过，RS 本地更新仍实际测量 | 8/8 | logs/without-observable-final.log |
| 不安装 RS | 3/3 必需 GameTest 通过；RS 测试源排除 | 7/7 | logs/without-refinedstorage-final.log |

构建工具为 Gradle 8.8、Java 17.0.8。本次在本地缓存下使用 --offline，并以 -Dnet.minecraftforge.gradle.check.certs=false 跳过 ForgeGradle 启动时独立的联网连通性自检，避免自检网络超时；未修改全局 TLS 或证书信任设置。前期失败的模板资源/测试断言已修正，最终日志以上表为准。测试世界与 KubeJS 测试目录位于本工程 gametest-run，不是正式游戏世界。

## 验证内容

- 使用真实 FTB API 创建团队与跨主世界/下界认领：450 + 450 = 900 μs/tick；未采样领地不稀释总值，其他团队的 200 μs/tick 和未认领区块不混入。
- 实际执行 UUID 查询、自己团队查询、脚下团队查询、团队名称查询、列表查询；检查输出总值、人均、覆盖率、无人在线时不适用、无效 UUID 与管理员权限。
- 验证没有注册 lagcontrol 或 freeze/freezeteam/release/releaseteam/limit/economy 等限制命令。
- 真实方块实体 ticker、动物 tick 只计时；熔炉输入未被没收，动物仍存活，玩家不计入本地负载。
- RS 真实 NetworkListener 更新控制器与 Importer：原更新只执行一次；控制器和节点分别归属自己位置；OBS 原耗时表出现真实 RS 方块名称和坐标；本地及 OBS 使用同一实测纳秒值；库存过滤物品保持 64。
- RS 原更新异常不吞掉、不重试；finally 恢复 OBS 先前计时上下文。
- 真实 OBS ProfilingData.create 报告经 Mixin 回调到达查询统计，包含两个 RS 位置；没有 OBS 时 RS 本地计时仍生效。
- 远端认领查询和计时不强制加载区块；工作线程不写本地计时表。
- tools/verify-artifact.ps1 通过：成品仅一个 ftteamlag 标识、所有 class 为 Java 17；不含旧处罚/经济包、测试、测试结构、Boss 内容或依赖 JAR。生产源码没有取消 tick、冻结返回值、删除实体、SavedData 或旧经济处理器。

## 原版与部署状态

原 FT-Merged-Economy-Mod 的 src、build.gradle、README.md 与原交付 qys-economy-merged.jar 均已对比任务开始时备份，哈希未变。新成品独立交付，未安装到任何 Minecraft mods 目录；未修改正式世界、配置或脚本。

安装纯查询版前需停服、备份并移走旧经济/处罚 JAR，否则旧版限制仍然生效；此版不提供旧 ftecon API，不能替其他脚本自动解除经济依赖。旧 ftlagcontrol.dat 不读写、不删除。详细安装步骤和数据口径见 README.md。

这些是独立 GameTest 与静态成品检查，不是正式多人服务器长期 TPS/内存压测。计时仅覆盖受测同步更新路径，不承诺所有第三方异步开销均可归属到区块；此处“RS 定位修复”不代表冻结/阻止 RS 运行。
