# Benger 打狗棒 — 牧王·断獠

2026-09-16：将确认过的犬首战棍造型作为 `ftboss:dog_beating_stick` 的默认模型。
保留游戏内名称“打狗棒”、原物品 ID、掉落和全部技能数值，不新增武器注册或 Java 渲染器。

## 美术与资源

- 85 个方块、9 个编辑分组：犬首锤冠、立耳、白斑长吻、琥珀眼、断獠打击翼、项圈铭牌、缠革握柄、尾锤短穗。
- 黑铁、骨白为主，黄铜和暗红革点缀；保持已确认的轮廓和尺寸。
- 128×128 分部件图集，255 块 UV 画区；宽面、侧面、顶底按部件比例分配，正反宽面镜像。
- 极薄小面保证至少 0.2 UV 单位以维持像素可见性，其余画区按几何比例校验。
- 16×16 独立不透明粒子贴图，避免粒子采样到 UV 图集留白。
- 原版 `elements` 模型，无生成式父模型、无自发光或骨骼动画；八个显示槽位显式配置。

正式资源：

- `src/main/resources/assets/ftboss/models/item/dog_beating_stick.json`
- `src/main/resources/assets/ftboss/textures/item/benger_shepherd_fang.png`
- `src/main/resources/assets/ftboss/textures/item/benger_shepherd_fang_particle.png`

## 维护

唯一几何规格：`tools/model3d/benger_weapon_spec.json`。
生成器：`tools/model3d/gen_benger_weapon.cjs`，共用 `lib_itemtex.cjs` 的画布、装箱器和自检。
`gen_weapons3d.cjs dog_beating_stick` 已委托新生成器，避免后续批量生成回退到旧木棍。
生成器无第三方依赖，默认写入资源与 `art/benger_weapon/`；也可传入暂存目录：

```text
node tools/model3d/gen_benger_weapon.cjs <assets/ftboss目录> <美术输出目录>
node tools/model3d/lint_java_model.cjs <模型JSON>
node tools/model3d/check_uv_coverage.cjs <模型JSON> <贴图PNG>
```

`art/benger_weapon/Benger_Shepherd_Fang.bbmodel` 嵌入贴图；模型预览是实际几何与 UV 的离线渲染，不是游戏截图。
`art/benger_weapon/uv-report.json` 记录画区数量与极小面情况。

## 游戏验收

第一人称正面朝向已同步修正，左右手镜像、握点保持与回归检查见 `WEAPON_FACING.md`。

完整退出客户端后通过 `tools/deploy_jar.ps1` 部署并清理资源缓存，再启动游戏。
默认外观已经内置，不再要求启用外观资源包。若启用了旧预览包，请停用或使用同步后的版本，避免覆盖内置模型。
测试世界执行 `/give @s ftboss:dog_beating_stick`，或使用已有打狗棒。
检查背包、掉落、展示框以及第一/第三人称左右手；持握参数仍需实机确认，不把离线渲染视为最终验收。

## 本次交付

Gradle 离线构建成功；模型、图集和粒子贴图在 JAR 中与源码逐字节一致。
首次尝试部署因游戏运行而安全跳过；客户端退出后，通过安全部署脚本完成四处同步与 Lightspeed 缓存清理。
桌面交付位于 `C:/Users/YOUR_USERNAME/OneDrive/桌面/FT-Benger-武器模型_20260916/`，含完整模组构建快照、Blockbench 文件与预览。
