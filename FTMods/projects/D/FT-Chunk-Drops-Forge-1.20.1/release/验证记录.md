# 验证记录

日期：2026-10-25（Asia/Shanghai）。

## 构建

- Java 17.0.8、Gradle 8.8、ForgeGradle 6.0.54、Minecraft 1.20.1、Forge 47.4.10。
- `gradlew.bat --offline -Dnet.minecraftforge.gradle.check.certs=false build` 成功，自动执行 `reobfJar`。
- 复用本机依赖缓存；关闭的是 ForgeGradle 启动连通性自检，不修改系统证书或全局 TLS 设置。
- 正式 JAR 仅含生产类、模组元数据和资源包说明，不包含 GameTest 类或测试结构。

## 实际 Forge GameTest

`gradlew.bat --offline -Dnet.minecraftforge.gradle.check.certs=false runGameTestServer` 成功，**4/4 必需测试通过**。
完整日志：`logs/gametest-verification.log`；服务端日志：`gametest-run/logs/latest.log`。

1. **数量和只读性**：64+7+5 个物品统计为 3 个实体 / 76 个物品，排除经验球与已删除实体，查询不改变数量、坐标或存活状态。
2. **负坐标和边界**：实际生成 X=-16.001、-16、-0.001、0、15.999、16 的实体，验证区块归属、`here` 和负坐标方块范围。
3. **指令、权限和分页**：12 个实体按 10+2 分页，验证排行条数、默认指令、帮助、非法参数、OP 权限、空区块；远处未加载区块查询不会增加已加载区块数。
4. **跨维度和报告**：在主世界和下界实际生成实体，验证全服扫描、原版 `execute in`、CSV 完整行数、物品 ID、UUID 和完整 double 坐标，以及空报告和不覆盖旧报告。

跨维度测试等待下界区块完成实体加载后才放入测试实体；正式查询本身不加载区块。
测试使用本工程内独立的 `gametest-run` 世界，未读写正式游戏存档，也未向现有整合包 `mods` 自动安装 JAR。
