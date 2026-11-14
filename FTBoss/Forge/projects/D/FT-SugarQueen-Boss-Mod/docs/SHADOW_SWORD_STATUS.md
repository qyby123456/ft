# 噬影之刃 · 裂瞳 —— 当前状态与剩余项（v26）

> 更新于 2026-09-15。承接 `docs/SESSION_CONTEXT_2026-09-15.md`（那是压缩稿），本文只写**这把剑**。
> 已部署哈希：**`30CB669D318215A6`**（v26 = 美化第一轮）。

## 一、当前事实（可核对）

| 项 | 值 |
|---|---|
| 几何 | **56 cube / 11 骨骼 / 旋转 4 / inflate 10** |
| 面数 | 导出面 **336**、估三角形 **672**（未扣内部面；**不假设引擎会剔除内部面**） |
| 骨骼树 | `root → weapon → {pommel → crystal, handle(5), guard, claw_left, claw_right, eye → pupil, blade(43)}` |
| 贴图 | `textures/item/shadow_sword.png` 256×256 逐面 UV；发光层 `shadow_sword_e.png`（ETF 认 `_e`） |
| 动画 | `idle 4s`(爪/瞳/晶核) · `awaken 0.7s` · `attack 0.55s` · `attack_double 0.95s` · `claw_snap 0.4s` |
| 文件 | `geo/shadow_sword.geo.json`、`animations/shadow_sword.animation.json`、`models/item/shadow_sword.json`(=`builtin/entity`+display) |
| Java | `item/weapons/ShadowSwordItem`(继承原 ShadowSword，玩法不变) + `client/render/ShadowSwordGeoRenderer` |

## 二、已通过的结构体检（数值，非目测）

- **悬浮/脱离：5 段动画全部为 0** ✓（做法：柄尾"通芯化"，底座↔基座直连，宝石环只作装饰）
- **UV：没有任何两个部件共用同一 UV 矩形** ✓（逐面 UV 全部独立）
- **握柄稳定性：没有任何动画触碰 `handle`/`root`** ✓（idle 只动爪/瞳/晶核 → 不会脱手）
- **刀身 10 条细分之间是刻意 0.05 重叠**（内部顶/底面同平面重合，不可见）
- 剩余"运动穿模"计数（去重后仅少数几对，全部为**内部隐藏嵌入**）：`guard×eye`（魔眼在护手里放大）、
  `pommel×crystal`/`crystal×handle`（环浮动、柄穿环）、`eye×blade`（已收敛到小量）
- 贴图四项自检全绿：侧面比例一一对应 / 正反镜像 / 零浮点噪声 / 零金色杂点

## 三、⚠️ 验证边界（重要，别搞错）

1. **自研预览工具对本模型已失效**：物品模型切成 `builtin/entity` 后**没有 `elements`**，而
   `tools/model3d/preview_spin.cjs` 靠 elements 渲染 → **预览图是空白的**。
   ⇒ 从 v19 起，**模型外观/穿模/动画一律只能靠 Blockbench 或实机验证**；贴图仍可用放大 PNG 自检。
2. **未做实机验证的都必须标"待验证"**：手持贴合、视野遮挡、刀身内部共面在远近/斜视下是否闪烁、
   动画过渡与循环接缝、`eye×blade` 残量是否可见。
3. `ItemDisplayContext` 分场景**尚未做**：GeckoLib `GeoItem#isPerspectiveAware()` 的确切语义**未核实**，
   按"不瞎改"原则搁置（物品栏目前在播 idle）。

## 四、复现链（改任何一处后按此跑）

```
node tools/model3d/regen_shadow_sword.cjs                       # 生成静态模型(61/56 cube 那个)
node %TEMP%\switch_shadow_entity.cjs                            # 备份静态版 + 换成 builtin/entity 存根
node tools/model3d/java_to_geo_rig.cjs shadow_sword shadow_sword shadow_sword ref/shadow_sword_static_v18.json.bak
node tools/model3d/add_attack_anims.cjs                         # 转换器会覆写动画文件, 必须补回攻击段
node tools/model3d/check_shadow_struct.cjs                      # 结构体检(悬浮/共面/相交/UV/握柄)
# 然后 gradle build --rerun-tasks + tools\deploy_jar.ps1 -NoBuild
```

**回退**：把 `ref/shadow_sword_static_v18.json.bak` 拷回 `models/item/shadow_sword.json` 再 build+deploy
（物品回到静态版；GeckoLib 那些类留着无害）。

## 五、剩余项（按建议顺序）

1. **魔眼/宝石环像素细节**（虹膜纹路、C6 眼神光、宝石切面高光）—— 纯贴图；
2. **剑尖收束的贴图过渡**（加长后从刃口到尖的渐变）；
3. **`ItemDisplayContext` 分场景**（需要先核实 GeckoLib API，可以从 javap 读 `GeoItem.isPerspectiveAware` 与
   `GeoItemRenderer.renderPerspective` 的用法）；
4. **手持位置最终微调**（当前 `OFF=[8,4.5,8]` 让握点落原点，实机若偏高/低，只动这一个数字或对应槽位 `translation`）；
5. **文档**：`CHANGELOG.md` 的 v19~v26 条目、`AGENTS.md` 的 GeckoLib 物品接入流程（含下面第六节的教训）。

## 六、这轮新增的硬教训（务必带到 AGENTS.md）

1. **Bedrock geo 的 cube 旋转是数组 `[x,y,z]`**，不是 Java 的 `{origin,axis,angle}` 对象 → 抄错 =
   `GeckoLibException: Error loading model file`，表现为**资源重载阶段卡死**。
2. **原点用"握点"**：GeckoLib 武器约定手抓处 = (0,0,0)；用"方块中心"(y−8)会让第三人称整体抬高 3.1 格。
3. **检测悬浮必须带 cube 自身旋转**：只用未旋转 AABB 会把"旋转后脱离"的零件漏掉（那次漏掉了 `tip_point`）。
4. **相邻块"刻意 0.05 重叠"会让顶/底面同平面重合** —— 内部不可见，但检测器要能区分"内部重合"与"外露重复面"。
5. **任何"斜插半埋的薄片"从侧面都会露出小块** —— 用户点名过两次，已全部删除（刃口切面、四片倒角、旋转刀尖）。
6. **写 Java 前必须 javap 核对本地 GeckoLib jar 的签名**（`GeoItemRenderer` 本身即 BEWLR；`GeoItem` 父接口在
   `software.bernie.geckolib.core.animatable.GeoAnimatable`）。
