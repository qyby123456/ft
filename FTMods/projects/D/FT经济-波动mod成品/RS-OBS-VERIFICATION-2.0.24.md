# 2.0.24：Refined Storage μs 检测验证

日期：2026-10-17。基于已交付的 2.0.23 团队总量版继续修改，不属于 ftboss，不更改物品 ban 独立模组。

## 基线与接口

- 项目：`D:/ft工程文件/FT-Merged-Economy-Mod`。
- 用户成品基线：`D:/ft工程文件/FT经济-波动mod成品/qys-economy-merged.jar`。
- 修改前 SHA-256：`E5CF3F516DBA1E54807A46CD0CA6F16DB99A47FEABFBB93203D17E67195A7699`。
- 修改前源码、配置与 2.0.23 JAR：`release-backups/before-2.0.24-refinedstorage-20261017-224444/`。
- 使用用户实例中的 `refinedstorage-1.12.4.jar`，以及现有 `observable-4.4.2.jar`、FTB Teams 2001.3.2、FTB Chunks 2001.3.8、Forge 47.4.10；构建/测试 Java 17.0.8。
- 通过真实 JAR 的 `javap` 验证：RS `NetworkListener.onLevelTick(LevelTickEvent)` 在服务端 END 阶段分别遍历网络、节点并调用各自 `update()`，不经过 Observable 原版 `LevelMixin` 里的方块实体 ticker。

## 实现与边界

- 新增两处可选 Redirect，原方法只执行一次，`finally` 收尾；控制器和节点分别按自身实际维度、位置记录纳秒。
- OBS 采样期间写入原生 `Profiler.blockTimingsMap`，保留真实方块名称、调用次数与时间，并恢复此前 `Props.currentTarget`；不改原 OBS JAR。
- 本地实测直接复用原 `LagControl.record`，不需要启动 OBS。和 OBS 报告同一区块取最大值、FTB 团队跨维度求和的原规则保留。
- 不新增库存快照、没收库存、RS 网络暂停/断网或强加载。不会把 RS 存储整体加入普通生产机器没收白名单；既有团队处罚与区块交互限制仍按原配置执行。
- 本地计时帧复用，热路径为两个时钟调用和 O(1) 区块累计，无 FTB 查询、全网枚举、库存扫描。OBS 的额外条目管理只在主动采样时执行。
- 仅计主线程网络/节点 update 及同步子调用；异步规划、包处理、其它调用路径不保证覆盖，不伪装成全服 MSPT 或整模组所有开销。

## 用例与运行记录

- 完整构建：`build runGameTestServer` 成功（`verification-2.0.24-rs-final.log`）；30198 项规则检查、**40/40 必需集成测试**通过（原有 31 项 + 新增 9 项）；已安装目标注入自检 **13/13**，包括网络与节点两处 RS 挂钩。
- 真实 RS 控制器/Importer 单次更新的一个观测样本为 **83.5 μs / 2.4 μs**，OBS 与本地纳秒记录完全对应；这只是测试网络的样本，不代表玩家实际网络的固定耗时。
- 真实 OBS 报告链路的另一个样本为 **124.176 μs/tick**（测试报告分母 100 tick）；两个真实位置进入报告、现有 ObservableBridge 接收器和同一 FTB 团队的预算合计。
- 复用帧连续调用 20,000 次，当前开发机平均 **234 ns/次**（约 0.234 μs，仅计时框架、不含 RS 更新）；不是大服实测、不承诺每台机器都达到该数值。
- 缺少 RS：`-PgameTestNamespaces=ftlagcontrol_rs -PwithoutRefinedStorage runGameTestServer` 成功；执行不依赖 RS 类的 1 项可选启动自检，自检 **11/11**，明确验证 RS 没有被加载，其余 8 项 RS 专用用例不参与注册。对应 `verification-2.0.24-rs-without-refinedstorage-2.log`。
- 缺少 OBS：`-PgameTestNamespaces=ftlagcontrol_rs -PwithoutObservable runGameTestServer` 成功；9/9 必需用例返回通过，其中 OBS 专用部分按明确的存在检查跳过，RS 原生实测、原样更新、非主线程保护、帧复用及启动状态检查实际执行；自检 **12/12**，明确验证 `refinedstorage=true, observable=false`。日志为 `verification-2.0.24-rs-without-observable-final.log`。
- 成品 **158147 字节**；SHA-256：`C007B7CF4D43F52626C33D6951AF7CB74D5BDF16489952618383310786115464`。
- 已检查混淆重映射、两个兼容 mod ID/版本与兼容 Mixin 配置，成品不含 RS、Observable、FTB、Minecraft 依赖类，不含 GameTest、测试脚本或 Boss 类。
- 与修改前快照逐文件 SHA-256 对比，`net/farmingtales/ftecon` 源码保持完全一致；团队总量实现与 2.0.23 一致。源码差异空白检查通过。

开发测试时曾修正测试结构的跨命名空间模板写法，以及缺少 RS 时测试类自身的提前加载；这些是测试夹具问题，已分别通过完整/缺失依赖重跑验证。Forge 在线版本检查超时及既有测试故意构造的脚本不匹配警告不是测试失败，未关闭证书检查或改动玩家配置来规避。

新增真实 RS / OBS 用例覆盖：

1. 兼容目标自检与实际可选模组加载状态一致。
2. 真实 creative controller、Importer 通过真实 NetworkListener 各更新一次，OBS 都产生非零 μs，且位置、名称、调用次数正确，与本地计时为同一份纳秒证据。
3. 没有启动 OBS 或没有安装 OBS 时，本地 RS 计时仍增加。
4. 原生管控和 OBS 都关闭时仍原样执行 RS update，不产生计时。
5. RS update 抛异常时不吞异常、不重试，计时关闭且 OBS 当前目标还原。
6. 非服务器线程跳过，不为遥远未加载位置生成 OBS 条目或加载区块。
7. 只开 OBS、关闭原生管控时，OBS 仍有真实数据，本地管控统计不增加。
8. 嵌套帧互不覆盖，正常更新复用帧；连续 20,000 次计时框架调用仅做开发机观测，不设不可靠的硬性性能保证。
9. 从真实 RS 更新生成真实 Observable 报告，经现有报告回调进入区块统计，同队两个区块的 μs 正确求和。

缺少可选模组的启动验证会明确跳过需要该模组的用例，不把跳过当作真实 RS 或 OBS 负载验证。

## 安装与回退

停服备份后替换原 `qys-economy-merged.jar`，勿同时保留新旧经济合并版。仅需替换该 JAR，不需要修改 OBS、RS、FTB 的 JAR。正式存档、客户端 mods 与 KubeJS 均未自动修改。

进入服务器让 RS 实际执行导入/导出/合成，再用 `/observable run 30` 采样；结束后查看 OBS 叠加的 RS 方块 μs。`/lagcontrol observable` 显示 RS 实测次数、OBS 写入次数和两处注入自检。

需要回退时，在停服状态恢复备份的 2.0.23 JAR。本次未修改存档格式或迁移玩家存储库存。
