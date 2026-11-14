# 蛋蛋手持镰刀 — 烛影·末镰

2026-09-16：按用户确认，将末地烛配色的镰刀模型内置到模组，并接入蛋蛋主手装备。

## 生效链路

- `EasterBossEntity.equipWeapon()` 仅在 variant 1 且主手为 `Items.END_ROD` 时写入 `CustomModelData:91601`。
- 原有附魔、无法破坏标签、掉落概率和战斗技能保持原样；其他变体装备逻辑不变。
- `assets/minecraft/models/item/end_rod.json` 将 91601 路由到 `ftboss:item/dandan_end_rod_scythe`。
- 91602 及更高值回退原版模型；没有标记的普通末地烛与放置后的方块仍为原版造型。
- 新生成、重新装备或存档重载的蛋蛋自动获得镰刀外观，不需要独立资源包。

## 模型维护

正式编辑源为 `art/dandan_scythe/Dandan_End_Rod_Scythe.bbmodel`，包含贴图、111 个几何块和 8 个编辑分组。
正式导出工具为 `tools/model3d/gen_dandan_scythe.cjs`，只导出本武器资源，不运行全武器生成器。
原有六份资源保持不变：四份服务于蛋蛋的末地烛外观，两份为独立玩家武器模型及技能发光贴图。
2026-09-18 追加九份 `models/item/dandan_rig/` 骨骼分组资源；每块原几何恰好归入一组，UV / 贴图不重做，动态维护见 `docs/DANDAN_SCYTHE_MOTION.md`。
粒子贴图源为 `art/dandan_scythe/dandan_end_rod_scythe_particle.png`。
导出工具无外部依赖，并拒绝静默覆盖未知的末地烛 overrides。

```text
node tools/model3d/gen_dandan_scythe.cjs
node tools/model3d/gen_dandan_scythe.cjs <临时assets目录>
node tools/model3d/check_dandan_scythe.cjs
node tools/model3d/lint_java_model.cjs src/main/resources/assets/ftboss/models/item/dandan_end_rod_scythe.json src/main/resources/assets/minecraft/models/item/end_rod.json
node tools/model3d/check_uv_coverage.cjs src/main/resources/assets/ftboss/models/item/dandan_end_rod_scythe.json src/main/resources/assets/ftboss/textures/item/dandan_end_rod_scythe_v2.png
```

从已确认的 v2 模型原样导出几何、UV、贴图和八个显示变换，仅省略 Java 不使用的 credit/texture_size 元数据。
原设计工程 `_work/dandan-end-rod-scythe-v2/` 保留独立资源包生成器和初稿记录，不再作为模组正式维护入口。

## 使用与验收

完整退出游戏后，通过 `tools/deploy_jar.ps1 -NoBuild` 安全部署，再重新启动客户端。
停用旧的 `FT-Dandan-Void-Staff-v1` 与 `FT-Dandan-End-Rod-Scythe-v2` 独立外观包，避免覆盖内置资源。
纯外观测试：`/give @s minecraft:end_rod{CustomModelData:91601}`。
该物品仍为原版末地烛；真正的玩家武器是 `ftboss:dandan_scythe`，两者不可混淆。
独立武器的掉落、技能、光效与验收步骤见 `docs/DANDAN_SCYTHE_GAMEPLAY.md`。

需在游戏中检查蛋蛋主手、第一/第三人称左右手、背包、掉落及展示框。
离线预览不是游戏截图；蛋蛋使用的纯外观模型保持原样，独立玩家武器另有选择性自发光与技能特效。
初次外观部署记录见 `art/dandan_scythe/deployment-check.json`；玩家武器部署另记于
`art/dandan_scythe/gameplay-deployment-check.json`，以免把旧的外观安装记录误作技能已安装。

## 部署记录

2026-09-16 已完成部署。首次构建遇到其他会话未完成的庄园演出/黑切代码及共享编译目录竞争，
未修改或回退这些并行代码。待相关文件由其他会话补齐后，使用独立源码快照构建，
部署前逐文件核验快照与桌面工程 `src/main`、Gradle 配置完全一致。

Gradle 离线构建成功；666 个面通过 UV 覆盖检查；模型、图集、粒子贴图和 CMD 路由在 JAR 中与源码一致。
通过 `javap -c -p` 验证最终 JAR 中存在 variant 1、末地烛类型检查与 `91601` 写入逻辑。
运行安全部署脚本的原样副本，确认无客户端运行，完成主实例及三处桌面成品同步；四份 SHA256 一致。

SHA256：`665376823436A64B5B7D01B496A5493E064ADBF63C40149CF4AD011248FFFCA7`。

2026-09-17 最终复核时，其他会话已同步更新的完整构建；四处文件仍一致，新 SHA256 为
`99E57CDAD0EB5EC4C07A1F5051475803319701A5FF248924241F3ADC5DB29678`。
再次核验当前安装 JAR 的四份镰刀资源与 CMD 装备字节码均保留，未用旧快照覆盖这次更新。

本次构建产物位于当前实例 `_work/dandan-end-rod-scythe-v2/mod-build-snapshot/build/libs/qys-bosses.jar`；
没有覆盖其他会话使用的桌面工程共享 `build/`，避免再次争用。正式源码已接入，后续正常构建仍会包含本武器。
原实例 JAR 和本次改动前的实体源码备份在实例 `_work/dandan-end-rod-scythe-v2/deployment-backup-20260916-234450/`。
游戏内持握与渲染仍待实机验收，未将离线/编译校验写成游戏内实测。
