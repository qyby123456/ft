# Observable 高耗时区块自动报告

独立 Forge 1.20.1 模组，文件名 `observable-lag-reporter.jar`。不属于 Boss 模组，也不依赖经济模组。

## 安装与使用

依赖 Forge 47.4.x、Observable 4.4.2 及 Observable 自身的 Architectury / Kotlin for Forge 前置。此版本限定 Observable `[4.4.2,4.5)`，已按用户提供的 4.4.2 JAR 接口实现；不把这些前置打进本模组。

- 单机：把新 JAR 放进当前实例 `mods`，退出游戏后安装，再启动世界。
- 联机：需要装在**运行 Observable 采样的服务器**。本模组不要求每个玩家客户端安装，不新增网络协议。只装远程玩家客户端不能分析远程服务器采样。
- OP 执行 `/observable run 30`，或者使用 Observable 界面开始一次采样。**采样结束后自动生成区块汇总，不需要再点导出。**
- 自动通知只发给权限等级 ≥2 的管理员；服务器控制台同时记录详细报告。没有在线 OP 也会保存。
- 本模组不定时启动新采样，不自动清理实体、方块，不强加载区块，也不上传报告。Observable 本身的原有上传行为不受本模组改变。

## 指令（OP / 控制台）

| 指令 | 用途 |
| --- | --- |
| `/lagchunks` | 显示使用说明 |
| `/lagchunks status` | 查看开关、阈值、队列和保存目录 |
| `/lagchunks last` | 查看本次服务器启动以来最近一次报告 |
| `/lagchunks last 10` | 最多显示前 10 项，可选 1–20 |
| `/lagchunks import 2026-09-21--19.00.00.json` | 读取服务器本机 `observable_profiles` 下的 Observable 导出文件 |

文件名含空格时加英文双引号。导入仅接受报告目录内的普通 `.json` 文件，拒绝路径穿越、链接文件、超过 32 MiB 或 250,000 条目的报告。支持 Observable 原生 JSON，以及上传 JSON 的 `data` 包装。

聊天行显示维度、区块坐标、方块坐标边界和 `ms/tick`；悬停显示前三个耗时对象及精确坐标。点击只把跨维度排查传送指令填入输入框，**不会执行**。需自行检查落点、维度权限和安全性。

## 配置

首次启动生成实例 `config/observable-lag-reporter.toml`。

| 配置项 | 默认 | 含义 |
| --- | --- | --- |
| `enabled` | `true` | 自动接收 Observable 完成的采样；关闭后仍能手动导入 |
| `chunkThresholdMsPerTick` | `2.0` | 区块达到此耗时才报告 |
| `criticalMsPerTick` | `5.0` | 红色严重级别，不低于普通阈值 |
| `minimumSampleTicks` | `100` | 短于此采样提示结果需复核，不当作确定诊断 |
| `chatTopChunks` | `5` | 聊天显示前几项，最多 20 |
| `fileTopChunks` | `200` | 文件中最多列出多少个高耗时区块，最多 2000 |
| `keepReports` | `20` | 保留最近多少组历史 JSON/TXT，最多 200 |
| `notifyAdministrators` | `true` | 是否自动发管理员聊天通知；关闭仍保存并写日志 |

生成文件位于**当前世界存档**的 `observable-lag-reports/`：`latest.json`、`latest.txt`，以及带时间戳/唯一 ID 的历史文件。超过保留数量仅删除本模组命名的旧报告，不碰原始 Observable 报告。切换世界不会把旧世界的报告发到新世界。

## 统计口径

通过反编译提供的 JAR 验证：`Profiler.TimingData.time` 使用 `System.nanoTime()`，`ProfilingData.Entry.rate = time / entry.ticks`。

每条记录对区块的贡献：

`ms/tick = rate × entry.ticks ÷ profile.ticks ÷ 1,000,000`

先按维度和 `floorDiv(方块坐标,16)` 分区，再合并实体/方块记录，按耗时降序排序。负坐标正确向下取整，例如 `(-1,-17)` 位于区块 `(-1,-2)`。短期出现的实体按实际调用次数加权；方块每 tick 多次更新不错误截断为一次。

**这不是总服务器 MSPT 或 FPS 测量。** Observable 未记录的网络、区块生成、其他线程耗时不会被凭空归到区块；占比只针对已经记录的实体/方块耗时。实体位置是采样结束时的位置，移动实体的时间无法按历史轨迹重新分配。采样过短、无效条目、超限报告、磁盘写入失败都有明确警告，不报虚假的“正常”。

## 实现与开发

- 单一 Mixin 监听 `ProfilingData.Companion.create` 返回，在 Observable 发回/上传前接收已生成的报告，不修改其计时逻辑。
- 排序、反射读取与文件保存走有界单线程后台队列（最多 2 个待处理），聊天发布回服务器线程。同一报告对象去重。
- 无客户端专用类引用；专服可用；不修改现有 Boss 或其他模组文件。
- JDK 17、Gradle 8.8，`gradle clean build runGameTestServer`。`build` 包含纯统计、导入和文件保留验证，GameTest 使用真实 Observable 类验证完成回调、权限和真实 Kotlin JSON 导入。
- `libs/` 是开发/验证用本地前置，成品仅包含本模组自己的代码与配置。
