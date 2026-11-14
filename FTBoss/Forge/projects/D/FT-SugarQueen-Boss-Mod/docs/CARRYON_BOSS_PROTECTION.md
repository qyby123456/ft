# Carry On：禁止搬起 Boss

- 对当前 Carry On Forge 1.20.1-2.1.2.7 的服务端 `EntityPickupEvent` 取消 Boss 搬起，在实体写入玩家搬运数据、从世界移除之前生效。
- 覆盖 FT 的 11 种 Boss 实体类型；庄园全部变体、支援麻雀与同类型迷你分身均包含。另保护苹果分身、酒桶替身、原版末影龙与凋灵，以及其他模组标记进 `forge:bosses` / `c:bosses` 的实体。
- 生存和创造模式均不能绕过；Boss 未开战、入场冻结、读档后仍按实体类型保护，不依赖体型、当前仇恨或血量。
- 只取消 Carry On 的搬起事件，不取消普通右键、攻击、交易或方块搬运。普通生物及未列入标签的召唤物继续遵循原有 Carry On 配置。
- 无需修改玩家的 Carry On 配置，不把整个 `ftboss` 命名空间或所有敌对生物禁用。
- Carry On 为可选兼容：未安装时不加载它的 API；运行时通过真实事件类注册监听，无新增编译依赖。数据包可向 `ftboss:carryon_blacklist` 追加其他未使用通用 Boss 标签的实体。
- 验证入口：`tools/verification/carryon_gametest.gradle`，与 `manor_integration_gametest.gradle` 一起用于隔离工程，实际 Carry On jar 放在隔离工程 `compat-libs/carryon-forge-1.20.1-2.1.2.7.jar`；`-PwithoutCarryOn` 验证缺少可选模组仍正常启动。

## 自动验证

- 安装实际 Carry On 2.1.2.7 的隔离服务端通过 36 项 GameTest：6 项搬运保护、10 项天穹锁击、12 项暗影笼罩保护、8 项蛋蛋传送。
- 搬运测试覆盖全部 Boss 类型和庄园变体、迷你形态、冻结和创造模式；直接调用 Carry On 真实右键入口，并观察搬起事件确实触发，验证 Boss 不会被删除或进入搬运数据。普通动物和箱子的实际搬运成功，普通右键事件没有被全局取消。
- 测试模拟按键同步后进入下一 tick，避免 Carry On 自身的同 tick 防重复规则导致假阳性。
- 未安装 Carry On 时另行启动并验证可选监听关闭、Boss 标签及既有回归正常；5 项必须使用 Carry On API 的测试在该轮不执行。
- 构建、源码快照及日志位于旧实例 `_work/carryon-boss-block-20260920/`。未进行客户端手动实机验收；玩家原有 Carry On 配置未修改。

## 交付核对

- 2026-09-20 19:10 核实：并行任务在 19:03 更新的四处交付 jar 已包含本次防搬运类与黑名单，无需以本轮隔离构建覆盖其他正在开发的改动。
- 分别读取三个桌面交付位置与 `FarmingTales_ForgeⅡ v1.5.2_beta3/mods/qys-bosses.jar`，逐字节 SHA256 核对 `CarryOnBossProtection.class` 与 `carryon_blacklist.json`，两项均与通过实际 Carry On 测试的隔离产物一致；对应源码也未变更。
- 当时四处完整 jar SHA256 一致：`60E467926E62AAF3C1B6C1BAE2E74CA7538EA2370EB8FE89B876A98D4192AE28`。未覆盖正在并行更新的整包，未更新旧实例 mods。
- 核对记录：同一任务目录的 `existing-deployed-hashes.csv`、`verified-protection-entries.csv`；最终测试记录为 `final-build-gametest.log`，可选依赖缺失记录为 `optional-absent-build-gametest.log`。
