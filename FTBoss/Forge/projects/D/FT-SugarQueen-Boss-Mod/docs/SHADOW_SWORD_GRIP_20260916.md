# 暗影剑 v21：握持与模型精修

## 握持诊断

- 用户提供 2026-09-16 21:50:52、21:51:00、21:51:03 三张截图，刀柄明显离开手掌；不能仅凭外观确定持有者的实体类型或精确高度。
- 本机 Minecraft 1.20.1 `ItemTransform.apply` 字节码先调用 `PoseStack.translate`，再旋转、缩放。JSON translation 单位为 1/16 格，不乘本物品 display 的 rotation 或 scale；之后仍受外层实体、手臂变换影响。
- `ItemInHandLayer` 在物品变换前接入手臂，并应用 X -90°、Y 180°。display 的 X 是手部横向，不是旧注释里的前后轴；左手会翻转 translation.x。
- 原 `translation.x=9.4` 产生额外 0.5875 格手部横向位移。两处第三人称现改为 `[0,-0.73,0.6]`，只移除横向偏移。
- `ItemRenderer` 的 `(-0.5,-0.5,-0.5)` 与 GeckoLib 4.8.4 `GeoItemRenderer.preRender` 的 `(0.5,0.51,0.5)` 基本相抵，不能再补半格中心偏移。
- rotation、scale、其余 translation、第一人称、GUI、掉落物和展示框槽位不变；玩家和 Boss 手臂姿态不变。最终握点仍需实机复验。

## 模型精修

- 两条握柄缠带的 cube pivot 从柄底移到各自中心，保留 ±22.5°，减少侧向甩出；没有改动骨骼 pivot。
- 收窄刀尖三层端帽，形成更集中的阶梯尖端，相邻层保持 0.05 模型单位的嵌接。
- 柄尾底座拆为三层，收窄上下端，保留中段厚度，改善原方盒轮廓。
- 22 个骨骼及层级、骨骼 pivot/rotation 全部保留；63 → 65 cube；9 段动画逐字节不变。
- 主刀身、锯齿刻口、魔眼、爪、聚能球和光效种子不变。两张贴图逐字节不变，未重排 UV 图集；只在原 UV 岛内按几何裁剪端帽和柄尾侧面取样。

## 复现和回滚

- 生成器：`tools/model3d/refine_shadow_sword_v21.cjs <v4输入.geo.json> <独立输出.geo.json>`。拒绝同路径输入输出；仅接受本次 v4 基线，不要对已经精修的结果再次运行。
- 本机原始快照：游戏实例 `_work/shadow-sword-v21/before-shadow_sword.geo.json`、`before-shadow_sword.json`、`before-shadow_sword.animation.json`、`before-shadow_sword.png`、`before-shadow_sword_ready.png`。
- 修改过的资源已同步进桌面源码，但本轮未构建、未覆盖游戏 jar。工作期间检测到另一构建已更新 jar，核实其中暗影剑资源仍与本轮原始快照一致，因此没有覆盖整包。
- 源码回滚只需恢复本轮的两个 JSON；游戏回滚只需禁用验收资源包。

## 游戏验收

- 资源包目录：游戏实例 `resourcepacks/FTBoss-ShadowSword-V21`，仅覆盖暗影剑物品 JSON 和 geo JSON，贴图与动画沿用模组。
- 在“选项 → 资源包”启用该包，置于其他包上方，点击“完成”等待资源重载。首次安装只按 F3+T 不会自动启用新包。
- 后续调整必须同步到已启用的覆盖包，再按 F3+T；只改桌面源码不等于部署。
- 复查右手、左手、正面、侧面、待机及施法。纯离线预览不证明握持正确；截图没有覆盖法阵验收。
- 正式构建部署后禁用临时覆盖包，避免它遮盖未来版本的资源更新。

## 检查结果和限制

- 结构校验：0 error / 0 warning；骨骼、动画兼容检查通过，全部动画握柄稳定性通过，采样姿态无疑似脱离。
- AABB 仍有刻意嵌接和光效重叠，不能把重叠计数当作真实穿模数；不声称全部穿模已经消除。
- 主体像素密度仍为 12.54 px/单位，检查器返回需要复查；本轮没有获得重排 UV 的实机依据，没有缩图或重绘纹理。
- 原 `sword_preview.cjs` 当前只有一段 Markdown，不能作为可运行检查器。本轮改用 `render_geo.cjs` 四视图；该渲染器不含实机控制器混合或完整透明排序。
- `sword_display_tune.cjs` 的旧预设及轴向注释仍保留错误假设，不应用于此次标定；`set` 只写源码，不等于热部署。
