# FTBoss 召唤物 / 唱片 / 状态图标内置资源

2026-09-17 从 `FTBoss-Summons-Icons-v1` 导入，保持已验收的外观，不重做武器或 Boss 实体。

## 资源范围

- 20 件召唤物的立体物品模型和 32×32 GUI 图标：9 件召唤道具、benger 刷怪蛋、10 枚彩蛋 Boss 刷怪蛋。
- 15 张唱片的 32×32 图标及模型引用。
- 12 个 ftboss 自定义状态的 18×18 图标。
- 123 个运行时文件，仅在 `src/main/resources/assets/ftboss/models/item` 和 `textures/item|mob_effect`。
- 20 件模型共 935 个 cube；GUI 使用 Forge `separate_transforms` 的独立平面图标分支。刷怪蛋模型的所有面均无 tintindex，避免原版颜色处理器二次染色。

## 唯一生成源

`tools/model3d/summons_icons_v1/` 中 `spec.cjs`、`models.cjs`、`art.cjs`、`icons.cjs`、`effects.cjs` 维护本批资源。

```powershell
node tools/model3d/summons_icons_v1/generate.cjs
node tools/model3d/summons_icons_v1/generate.cjs --group=discs
node tools/model3d/summons_icons_v1/generate.cjs --group=effects
node tools/model3d/summons_icons_v1/generate.cjs --group=summons
```

可提供一个资源根目录位置参数来生成到临时目录做逐字节对照。生成器只写本批明确列出的资源，绝不重出其他武器、掉落物、实体、动画或音频。

像素 PNG 使用 `pngjs`，默认引用当前机器的既有 Codex Node 依赖目录，可通过 `FT_NODE_MODULES` 指向其他 `node_modules`；不影响模组运行时依赖。旧 `gen_discs.cjs` 转交新生成器，旧 `gen_summon_icons.cjs` 跳过已归本批维护的物品，防止再次覆盖成 16×16 旧图。

`art/summons_icons_v1/` 保留 Blockbench 工程、实际模型/图标预览和检查结果。初始导入资源与已展示资源包逐字节一致。模型结构、UV、引用、透明度、独立图标、ZIP CRC 检查已通过；没有宣称做过游戏内手持实测。

## 打包与部署

这些资源现在内置于 JAR；正常 Gradle 构建会自动纳入。不需要外置资源包。确认部署的 JAR 内资源完整后，只从 `options.txt` 移除 `file/FTBoss-Summons-Icons-v1`，不改变其他资源包或游戏设置；文件夹和 ZIP 留作备份。

首次导入期间发现 Java 源码仍有并发修改，因此不编译或发布那些未完成改动：从当前已安装的 `qys-bosses.jar` 生成资源增量包，验证除上述 123 个资源外所有原条目的内容完全不变，然后使用工程原有 `tools/deploy_jar.ps1 -NoBuild` 安全部署。游戏正在运行时不覆盖 JAR。后续正常构建会从源码资源目录得到同一套图标和模型。

本次资源导入不修改 Java 逻辑、物品/效果注册、配方、掉落、声音、音频时长、Boss 数值或 ftecon。并发工作产生的源码改动保持原样，不回滚也不混入本次资源增量包。
