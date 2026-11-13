# Event 1.0.7：金币锅奖池调整

此目录是 2026-10-16 的独立候选源码与构建交付，基线为 FT-Event-独立最终-src-Forge-20261014-01a11be8。不修改或覆盖该封存基线。

## 调整

- 庄园战奖励（武器、唱片、法术合计）从 0.01% 提高到 0.035%，技能经验书从 9.99% 调整为 9.965%，总概率仍为 100%。
- 矿物块类别保持 25%，新增下界合金块和灾变腾炎块、远古金属块、咒魂块，按稀有度配置 12 种矿物子池。
- 保留真实 ManorVictoryLoot.contents() 目录和奖励 NBT；绿宝石奖池、商店价格、四向放置、天气与世界事件不改。
- 金币锅跨级奖励在自动打开商店前发放，避免关闭商店后被旧背包覆盖；对应回归检查真实库存与显式掉落，不再仅检查 give() 调用。

完整概率表、实际物品 ID、相对与绝对概率见 project/docs/gold-pot-wishes.md。

## 部署边界

本批交付是离线候选，未部署。实例原有 qys-event-forge-1.20.1-1.0.6.jar.disabled 保持禁用，原实例脚本、Boss、NeoForge、存档不变。

以后单独授权部署时，必须同时使用 deliverables 下的 Event 1.0.7 JAR 和 integration/kubejs/server_scripts/block_events/wishing_well.js；不能只替换脚本提示或只启用旧 JAR。不要同时启用两个 Event 版本，不在未经备份与授权时打开原存档。

## 验证与重建

project/build.ps1 使用 Java 17、Gradle 8.8、固定 ForgeGradle 6.0.54，运行 build、reobfJar、verifyQysEvent。构建依赖与中间文件留在本目录的 _work；分发源码包不包含这些缓存。

行为脚本：node project/docs/wishing_well.behavior.test.js

金币锅桥接：node project/docs/test-gold-pot-bridge.cjs integration/kubejs/server_scripts/block_events/wishing_well.js

audit 记录本轮构建、回归、物品目录和候选 JAR 的核查结果。尚未启动 Minecraft、实际应用 Mixin 或进行游戏内发奖验收。

本轮结果：两次 clean build 的 JAR SHA256 相同；直接从交付 JAR 加载实际规则类，穷举 100000 个类别签与 10000 个矿物签通过；行为模拟 731 项通过。负对照使用旧脚本时，三个跨级发奖场景均因实际库存/掉落不足而失败，证明新增断言能抓住旧问题。

交付 JAR 与上一轮可复现基线相比，80 个实际文件字节不变；只变更 wishingwell 包和版本元数据，新增 Mineral 枚举。Boss 类没有打入 Event。
