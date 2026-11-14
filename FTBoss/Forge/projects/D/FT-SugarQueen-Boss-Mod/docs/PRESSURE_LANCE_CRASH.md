# 高压贯星客户端崩溃

## 已定位的原因

beta3 的 `crash-2026-09-18_22.59.03-client.txt` 首次留下完整原始异常：`Rendering Particle` / `IllegalStateException: Not filled all elements of the vertex`，直接出错点为 Iron 3.16.3 的 `SoulfireRayParticle.drawQuad:111`。

崩溃粒子为 SoulfireRayParticle，Age=0，颜色约为(0.47451, 0.73725, 0.98039)，与高压贯星的0x79BCFA一致。其归属 `PARTICLE_SHEET_TRANSLUCENT`，但 drawQuad 使用实体式顶点写入顺序与字段，不能完整填写粒子格式。该粒子由本模组的光效桥接创建，因此修复本模组的选择路径，不归咎于用户操作。

之前两次 `Already building!` 是 Not Enough Crashes 的崩溃清理界面再次报错，遮住了原始异常；不能据此认定 FancyMenu/EaseGUI 是本次施法崩溃的根因。

## 修复范围

- 高压贯星 `pressure_lance`、共用同一问题粒子的离子轨道炮 `ion_rail` 改用 Trace 光痕，客户端不再引用或构造 SoulfireRayParticleOptions。
- 保留法术自身的加速环、轨道线圈与末端环；预算不足、粒子最少模式、远坐标精度不足或粒子创建失败时，使用原有完整几何光束。
- 不修改法术伤害、命中、穿透、墙体阻挡、耗蓝、冷却、满阶掉落及原版卷轴贴图。
- 不修改 Iron 原始 jar，不禁用 FancyMenu/EaseGUI 或光影，不删除法术。

## 回归入口

- `node tools/verification/check_native_spell_lights.cjs`：校验两门法术走 Trace、客户端没有危险粒子引用且不修改玩法。
- `gradle checkIronSpellLights`：遍历70种法术，禁止任何法术重新映射到 Soulfire Ray，并扫描客户端源码防止重新引入。
- `gradle -I tools/verification/iron_spell_vertex_check.gradle checkIronParticleVertices --offline`：直接调用实际 Iron 粒子方法与 Minecraft BufferBuilder，复现原错误并验证 Trace 顶点；不使用玩家世界，不打开游戏窗口。测试通过反射分配未连接世界的粒子，仅验证顶点写入，不模拟粒子完整生命周期或光影画面。
- `gradle -I tools/verification/manor_integration_gametest.gradle runGameTestServer --offline`：原生 Forge/Iron 施法、效果、工作台、满阶卷轴和掉落回归。
- 客户端复测：正常粒子设置下，对假人施放高压贯星和离子轨道炮，分别测试命中、空放和被墙截断；使用原来的光影设置。离线顶点验证不代替此项完整整合包画面验收。

本次日志和旧 jar 备份位于旧实例工作目录 `_work/pressure-lance-crash-20260918/backup/`。为继续保留原始崩溃报告，上一轮设置的 `catchGameloop=false` 暂不恢复。

## 验证与交付结果

2026-09-18：已合并并保留共享工程中的 Trace 修补与其他会话更新。独立工程干净构建通过；实际 Iron 3.16.3 + Minecraft BufferBuilder 顶点测试11项断言通过，成功复现原异常，并验证两门替换光痕在0.5/12/24/64格长度的顶点完整性。此复现不加载光影，因此原异常并不需要光影才能触发。

97项真实 Forge/Iron GameTest 全部通过；原生光效规则3026项、光效接线31项、非庄园规则62921项、素材接线828项、庄园素材/双语检查均通过。98个客户端 Java 源文件扫描未发现实际代码引用危险粒子；发布 class 常量池也不包含 SoulfireRayParticleOptions。

1251个源码/资源/构建输入的哈希与共享工程一致。jar 为42,151,428字节，SHA256：`598E18634CF7C0BD233EBED25CAF728B037D67A664357847CC2494AF8E97187E`。经 `tools/deploy_jar.ps1` 同步桌面两处及152beta3，三处哈希一致；旧 FarmingTales Forge 实例未覆盖。包内没有自定义卷轴包装器或旧 boss_scrolls 美术，单一满阶法术继续保留。

日志：`vertex-build.log`、`gametest.log`，快照审计：`verified-source-manifest.json`，均在本次工作目录。尚未以完整整合包客户端人工复测画面；请重启152beta3后用原有光影测试高压贯星。
