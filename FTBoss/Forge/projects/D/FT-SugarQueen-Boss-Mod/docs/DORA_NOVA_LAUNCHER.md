# 朵拉手持武器 — 内置地脉新星

## 当前接线

朵拉（`EasterBossEntity` 变体 2）持有 `bren:machine_gun` 时，客户端改用地脉新星模型。
`DoraWeaponModel` 在 `ModelEvent.RegisterAdditional` 注册模型，在 `ModifyBakingResult` 包装
Bren 机枪的 inventory 模型；`ItemOverrides.resolve` 根据持有实体选择外观。

只读持有实体，不替换 ItemStack、不写 NBT、不注册新物品，不修改伤害、射速、弹药、
附魔、掉落或朵拉 AI。装弹时若朵拉换成其他物品，按原来逻辑显示，不强行画炮。
普通玩家、其他 Boss、背包、掉落与展示框继续走 Bren 原模型及原始弹匣判定。
缺少 Bren 时跳过包装，没有新增编译期 Bren 依赖。

这是 Boss 专属外观，不是可单独 `/give` 的新物品。它保留原版静态 `elements`，
绿色区域只是亮色贴图，没有自发光、光束或新的地震技能。

## 美术文件与维护

唯一编辑源：`art/dora_nova_launcher/Dora_Nova_Launcher.bbmodel`，包含嵌入贴图。
独立粒子图源：`art/dora_nova_launcher/dora_nova_particle_v1.png`。
133 个方块、9 个分组、256×256 图集；八棱炮口、能量轨、下置能量匣、双握把和肩托。
本次保留确认稿的几何、UV、贴图与显示参数，不重新设计外形。

生成器仅导出这三份资源，不调用全武器生成器：

- `src/main/resources/assets/ftboss/models/item/dora_nova_launcher_v1.json`
- `src/main/resources/assets/ftboss/textures/item/dora_nova_launcher_v1.png`
- `src/main/resources/assets/ftboss/textures/item/dora_nova_particle_v1.png`

```text
node tools/model3d/gen_dora_nova_launcher.cjs
node tools/model3d/verify_dora_nova_launcher.cjs
node tools/model3d/lint_java_model.cjs src/main/resources/assets/ftboss/models/item/dora_nova_launcher_v1.json
node tools/model3d/check_uv_coverage.cjs src/main/resources/assets/ftboss/models/item/dora_nova_launcher_v1.json src/main/resources/assets/ftboss/textures/item/dora_nova_launcher_v1.png
```

生成器也接受 `<assets/ftboss 目标目录> [美术源目录]`，可先生成到暂存目录核对。
无第三方 Node 依赖，使用工程现有 PNG 校验及安全写入工具。
`art/dora_nova_launcher/` 中预览图来自相同几何和 UV 的离线渲染，不是游戏截图。

## 部署与验收

使用 `tools/deploy_jar.ps1` 部署，绝不覆盖运行中的客户端 JAR。
成功部署后停用 `FT-Dora-Nova-Launcher-v1` 预览包，保留其他资源包。
旧预览包会替换所有 Bren 机枪，启用它就无法观察内置版对普通机枪的隔离效果。

启动客户端后查看朵拉持枪、开火和装弹过程；同时拿普通 Bren 机枪检查空弹匣、
普通弹匣与染色弹匣仍保持原外观。现存与新生成的朵拉都通过渲染时实体类型判定，
不依赖重新生成装备或给存档补 NBT。左右手持握和遮挡仍需实机确认。

## 本次交付记录

2026-09-16：Gradle 离线构建通过（工程没有测试源，`test NO-SOURCE`）；
798 个面 UV 检查通过，模型、嵌入贴图与确认稿一致。
直接检查成品 JAR 的重混淆字节码，确认实体变体 2 分支与原模型回退调用均已入包。
三份美术资源与源码 SHA-256 一致，没有加入全局 `assets/bren` 模型覆盖。
通过安全部署脚本同步实例与三个桌面交付位置，四处 SHA-256 一致。
旧预览包已从当前资源包选择中移除，其目录和 ZIP 保留；原配置已备份。
部署记录见 `art/dora_nova_launcher/jar-deployment.json`；实机渲染仍待用户启动客户端验收。
