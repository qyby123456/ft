# qys 团队卡顿纯查询版 1.0.0

适用 Minecraft 1.20.1 / Forge 47.4.10，独立成品 `qys-team-lag-query.jar`，模组 ID `ftteamlag`。

## 仅保留统计，不带任何处罚

- 查询 FTB 团队所有维度**当前认领区块**的实测总耗时（μs/tick）及人均值、在线/总人数、采样覆盖率。
- 本地自动测量方块实体、非玩家实体、农场物语原生机器回调与收割机；不取消 tick，不更改机器时钟、物品、流体、库存或产出。
- 动物仅计时：没有实体上限、清场、删除、冻结或交互限制。玩家实体不计入团队耗时。
- 完全没有财富、价格、税收、收入扣减、商店修改、KubeJS 脚本改写、限产、冻结、踢人、罚款或限制解除功能。
- 不包含 `ftecon`/`ftlagcontrol` 兼容 ID/API，不读取/写入旧配置或 `data/ftlagcontrol.dat`。

## 指令（管理员，权限等级 2）

| 指令 | 功能 |
| --- | --- |
| `/teamlag` | 查询执行玩家自己的团队 |
| `/teamlag here` | 查询脚下区块的认领团队 |
| `/teamlag <团队UUID>` | 按完整团队 UUID 查询，控制台可用 |
| `/teamlag <在线玩家名>` | 查询该在线玩家所属团队 |
| `/teamlag <团队名>` | 查询有认领区块的团队，含空格名称加双引号；同名用 UUID |
| `/teamlag list` | 按团队总值列出最多 50 队，显示完整 UUID 和人均 |
| `/teamlag status` | 查看计时窗口、RS 与 OBS 接入状态 |
| `/teamlag observable 10` | 触发一次真实 OBS 采样（可选，5–60 秒，间隔至少 30 秒） |

没有 `freeze`、`freezeteam`、`release`、`releaseteam` 等处罚指令。之前提出的限制/解除需求不带入此纯查询版本。

## 数据口径

- 优先用当前窗口累计纳秒 ÷ 1000 ÷ 已经过的服务器 tick；当前窗口还无该团队采样时，显示上一完整窗口，并明确标注来源。默认窗口 1200 tick（正常 TPS 约 1 分钟）。
- 未采样团队显示“暂无实测数据”，不把未知当作 0。只统计已有实测的认领区块；未加载领地不强制加载，已取消/转让的认领按查询时实际所有权统计。
- 人均默认除以**当前在线成员数**；无人在线显示“不适用”，不会用假人数，也不跳过整队总值。配置 `config/ftteamlag.toml` 的 `perCapitaOnlineOnly=false` 可改为全部成员数。
- OBS 报告在采样结束后接入，排除玩家实体；与本地数据**同区块取较大值**，不是相加。它是不同窗口的保守估计，不是全服 CPU 百分比或完整异步开销。OBS 数据最多保留两倍配置窗口的现实时间。
- 不永久保存统计，重启重新采样。没有新采样时上一窗口不是当前瞬时负载，需留意输出来源。

## RS 无法定位/锁定耗时的修复

兼容 Refined Storage 1.12.4 / Observable 4.4.2。RS 的实际网络与节点更新在 `NetworkListener.onLevelTick` 中，普通方块实体 ticker 的计时覆盖不到。

此版包裹原始 `INetwork.update()` 与 `INetworkNode.update()`，每次只调用原方法一次：控制器维护开销归控制器位置，Importer、Exporter、Interface、Crafter 等归节点自己的位置和区块。OBS 采样时额外写入原来的方块耗时表，保留真实 RS 方块名称与坐标，让原报告/叠加界面能定位；未开启/未安装 OBS 时仍有本地 RS 实测。异常在 finally 中结束计时并恢复 OBS 上下文，原异常继续抛出，不重试、不吞掉。

这里修复的是**耗时检测和 OBS 位置定位**，不是冻结/锁定 RS 网络。纯查询版不会阻止 RS 运行。

## 安装与替换

1. 停服并备份世界与 mods。把成品放入 Forge 1.20.1 服务器的 mods；FTB Teams 与 FTB Chunks 必需，RS / OBS / KubeJS / 农场物语均为可选计时适配。
2. 要真正移除旧处罚，必须移走旧的 `qys-economy-merged.jar` / `qys-lag-economy.jar` / `qys-progressive-taxation` 等相关处罚模组。**本查询版不会停用仍安装着的旧模组**，也不建议与旧经济版并存（RS 计时挂钩可能冲突）。
3. 若其他模组/脚本依赖旧 `ftecon` API，需要另行解除依赖；此版刻意不提供经济 API。旧存档文件可保留但此版不读写；不自动清除历史状态。
4. 本次只交付新成品，没有修改任何 Minecraft 实例的 mods、世界或脚本，也不修改原合并经济工程。

## 构建与验证

Java 17、Gradle 8.8；libs 使用现有整合包依赖。运行 `gradle --offline build runGameTestServer`；另有 `-PwithoutObservable`、`-PwithoutRefinedStorage` 可选依赖启动验证。测试代码与依赖不进入成品。
