# 苹果爵士的果梗 · 蜜脆青枝

2026-09-16，苹果爵士专属掉落武器的静态立体外观重设计。

## 设计
- 红苹果护心、双叶枝杈护手、斜缠带木柄、青绿色弯叶刀身、浅色刃口、双面叶脉。
- 使用现有物品 `ftboss:apple_stem`，游戏名称仍为「苹果爵士的果梗」。
- 不改物品注册、攻击伤害、攻速、耐久、掉落规则、Boss 本体或其他武器。
- 66 个立体部件，逐面 UV，128×128 图集；主体以约 2 px/模型单位为目标，不用高频噪点。
- 刃口以 22.5°/45° 旋转长条接成连续斜线，转角按斜接长度重叠；刀面共用连续 UV 和统一厚度，不在每段重置明暗。保留斜刃外轮廓、配色和全部显示变换。
- 斜刃接头、刀背与叶脉分层，避免和木脊共面；柄芯埋入箍内，苹果底座内缩，刀根埋入护心，避免表面贴在一起闪烁。
- 52 个被相邻刀片或包边遮住的内部面不导出，实际渲染 344 个面；Blockbench 工程中同样显式关闭这些面。
- 生成器自带旋转后面片检查：共面或间距小于 0.015 模型单位的平行重叠会阻止输出。验证器另对每个省略面进行 17×17 遮挡采样，检查没有暴露缺口。
- Java 静态物品模型，不使用 `item/generated`、`item/handheld` 或 `builtin/entity` 父模型；不需要新增 Java 渲染器。
- 无独立动画、无发光层；叶脉的浅色只是贴图和几何，不代表额外技能或自发光。

## 文件
- `models/item/apple_stem.json`：直接用于 Forge 1.20.1 的模型。
- `textures/item/apple_stem_micui.png`：独立新贴图路径，保留旧贴图，避免串用旧图集。
- `apple_stem_micui.bbmodel`：Blockbench Java Block/Item 工程，包含内嵌贴图和分组。
- `apple_stem_views.png`：正面、斜视、背面真实 UV 采样预览，不是游戏截图。
- `apple_stem_icon.png`、`apple_stem_side.png`：物品栏构图与厚度检查。
- `apple_stem_report.json`：部件数、UV 岛和密度明细。非常薄的装饰侧面最少分配 1 px，因此局部密度可超过 3；主体大面单独统计。
- `apple_stem_surface_audit.json`：共面/近共面重叠面检查结果。

## 重新生成
在模组源码工程根目录运行：

```powershell
node tools/model3d/gen_weapons3d.cjs apple_stem
node tools/model3d/preview_apple_stem.cjs art/apple_stem_micui tools/model3d
node tools/model3d/validate_apple_stem.cjs art/apple_stem_micui tools/model3d
node tools/model3d/lint_java_model.cjs src/main/resources/assets/ftboss/models/item/apple_stem.json
node tools/model3d/check_uv_coverage.cjs src/main/resources/assets/ftboss/models/item/apple_stem.json src/main/resources/assets/ftboss/textures/item/apple_stem_micui.png
```

专用规格在 `tools/model3d/gen_apple_stem.cjs`；总生成器对 `apple_stem` 委托给此脚本，不再用旧版 8 部件造型覆盖新版。编辑应改专用规格后重生成。

## 验收边界
- 已生成模型、贴图、Blockbench 工程与离线预览；结果以配套校验日志为准。
- 第一/第三人称左右手沿用此前果梗的变换，不将离线渲染当作握持正确的证明；物品栏与展示框单独适配。
- 尚需游戏内确认握点、左右手朝向、附魔光效与资源重载结果。
- 获取：`/give @s ftboss:apple_stem`。
- 若 Minecraft 正在运行，只构建，不覆盖 `mods/qys-bosses.jar`。完全退出游戏后通过 `tools/deploy_jar.ps1 -NoBuild` 部署，并重启客户端。
