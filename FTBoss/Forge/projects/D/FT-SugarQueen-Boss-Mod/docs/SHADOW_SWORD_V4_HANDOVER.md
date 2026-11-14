# 暗影剑（噬影之刃·裂瞳）接力档案 — v4

> 2026-09-29：正式包已移除旧 `/ftshadow` 与 `/ftcleaver` 动画/视觉验收指令；以下命令段仅保留为历史记录，不再可用。

> **2026-09-17 V22 重建已接替此档案的模型数据**：按原七项设计重做为 22 骨骼 / 72 cube / 9 段原动画，图集 128×64。源码与独立资源包已更新，未覆盖运行中的 jar；实机仍待验收。以 `docs/SHADOW_SWORD_V22_REBUILD.md` 为准，旧生成器及历史 patch 不能重跑覆盖成品。

> **21:51 截图后的 v21 勘误**：第五节的轴向解释、`translation × scale / 16` 换算及“只改源码后直接 F3+T”不正确。第三人称 translation 已改为 `[0,-0.73,0.6]`，模型精修后为 **22 骨骼 / 65 cube / 9 段原动画**。本轮只更新源码与独立验收资源包，未部署 jar，实机仍待复验。以 `docs/SHADOW_SWORD_GRIP_20260916.md` 为准；不要套用旧调参预设。

> **写给下一个接手的人（人或 AI）**。上一版是 `docs/SHADOW_SWORD_V3_HANDOVER.md`（2026-09-15，写的是 v3 静态→骨骼化那一轮），
> 那份**已经过期**（骨骼数、动画段数、贴图、位置、法阵全都是旧数据）。**以本文件为准，不要以 v3 那份为准。**
>
> 生成时间：2026-09-16 20:4x ｜ 工程根目录：`D:\ft工程文件\FT-SugarQueen-Boss-Mod`
> 游戏实例：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales Forge`
> 部署产物：`mods\qys-bosses.jar`（33.04 MB）

## 〇、先读这一段：本工程的三条铁律

1. **不要用 PowerShell 编辑源文件。** `Set-Content` / 正则替换会插 BOM、把 UTF-8 中文全弄成乱码 —— 本工程已经被这个坑过三次。
   要写文件就用能控制编码的编辑器/工具；已经乱码的文件要整份重写，不要试图局部修。
2. **不要凭空猜 API。** 1.20.1 的 Forge/MC 和"记忆里的版本"差很多。本工程的标准做法是：
   用 `javap` 反汇编 `joined-1.20.1-*.jar` / 读 Forge 的 `-sources.jar` / 读 Mojang `client_mappings.txt`
   **核对签名之后再写代码**（第 6.5 节列了三个已经用编译报错钉死的坑）。
3. **数值要能被代码追溯。** 不确定就写"未验证"，不要写"已完成"。本工程的历史里，
   因为"看起来接好了"而误判的次数远多于因为算错数的次数。

---

## 一、这个物品到底是什么

| 项 | 值 |
|---|---|
| 物品 ID | `ftboss:shadow_sword` |
| 中文名 | 噬影之刃 · 裂瞳 |
| 类 | `net.farmingtales.ftboss.item.weapons.ShadowSwordItem extends BlackCleaverAndShadow.ShadowSword implements GeoItem` |
| 基础数值 | 下界合金剑系：`Tiers.NETHERITE, 10, -2.4F` → 实际 15 点（1+4+10，彩蛋武器 ×1.5） |
| 渲染 | **GeckoLib 骨骼物品**（`GeoItemRenderer`），**不是** Java 静态模型 |
| 模型文件 | `assets/ftboss/geo/shadow_sword.geo.json` |
| 动画文件 | `assets/ftboss/animations/shadow_sword.animation.json` |
| 贴图 | `shadow_sword.png`（常态）/ `shadow_sword_ready.png`（就绪态）/ `shadow_sword_e.png`（发光层贴图，目前**未接线**） |
| 物品模型 JSON | `assets/ftboss/models/item/shadow_sword.json` —— **只有 `display` 块，没有 `elements`**，`parent = builtin/entity` |

**技能两段**（都在这个物品上）：

1. **【裂瞳投珠】** 长按右键蓄力 → 服务器状态机第 7 tick 发射【暗影珍珠】→ 珍珠命中后把**使用者传送到落点**并结算范围魔法伤害。
2. **【暗影镇印】** 传送落点画出地面魔法阵 + 暗影能量柱（2026-09-16 新增，见第四节）。

---

## 二、模型（几何）—— 实测数据，不是文档里的旧数

### 2.1 总量

```
format_version = 1.12.0
identifier     = geometry.ftboss.shadow_sword
texture        = 256 × 256
骨骼 22 ｜ cube 63 ｜ 带 rotation 的 cube 5 ｜ 带 rotation 的骨骼 4 ｜ 有 inflate 的 cube 若干
```

> v3 交接文档写的是 "61 cube / 19 骨骼 / 4 处静态旋转 / 10 处 inflate"。
> 现在多出来的 3 骨骼 / 2 cube 是 2026-09-15 晚加的**光效种子**（2.3 节）。

### 2.2 骨骼树（含 pivot，单位 = 模型单位，16 单位 = 1 格）

```
root                                   pivot [0,0,0]        cube 0
└─ weapon                              pivot [0,0,0]        cube 0     ← 原点 = 握点（GeckoLib 武器约定：手抓处 = (0,0,0)）
   ├─ pommel                           pivot [0,-3.5,0]     cube 2     柄尾底座 + 宝石环
   │  └─ crystal_link                  pivot [0,-3.5,0]     cube 1     短轴
   │     ├─ crystal                    pivot [0,-4,0]       cube 1
   │     └─ crystal_glow               pivot [0,-3.1,0]     cube 1     ★ 光效种子
   ├─ handle                           pivot [0,-2.5,0]     cube 5     柄芯 + 两道缠绳
   ├─ guard                            pivot [0,4.1,0]      cube 1     吞口（**没有护手**，用户明确要求"要刀不要剑"）
   │  ├─ claw_left                     pivot [-2,4.1,0]     rot [0,0,-22.5]
   │  │  └─ claw_left_tip              pivot [-2.8,4.4,0]   rot [0,0,-4]
   │  ├─ claw_right                    pivot [2,4.1,0]      rot [0,0,22.5]
   │  │  └─ claw_right_tip             pivot [2.8,4.4,0]    rot [0,0,4]
   │  ├─ eye_mount                     pivot [0,4.2,0]      cube 4     眼眶 4 条边条（**不随虹膜缩放**）
   │  │  ├─ eye                        pivot [0,4.2,0]      cube 1     虹膜（3.15 深）
   │  │  │  ├─ pupil                   pivot [0,4.2,0]      cube 2     瞳孔前后两片，各厚 0.50
   │  │  │  └─ cast_socket             pivot [0,4.2,-2.4]   cube 0     空骨骼（挂点）
   │  │  │     └─ charge_orb           pivot [0,4.2,-2.4]   cube 1     聚能球 1.3³，cube 自转 45°
   │  │  │        └─ charge_core       pivot [0,4.2,-2.4]   cube 1     亮核 0.4³
   │  │  └─ eye_glow                   pivot [0,4.2,0]      cube 1     ★ 光效种子
   ├─ blade                            pivot [0,5.1,0]      cube 37    ← 主体，38 里 1 个被拆到别处
   └─ blade_glow                       pivot [0,12,0]       cube 1     ★ 光效种子
└─ grip_anchor                         pivot [0,0,0]        cube 0     空骨骼（历史遗留的握点锚，未使用）
```

**整体包围盒**（cube 原始 origin/size 的并集，未叠加骨骼变换）：
`x ∈ [-5.1, 5.1]`、`y ∈ [-4.5, 20.15]`、`z ∈ [-3.45, 1.75]`
→ 剑总高 **24.65 模型单位 ≈ 1.54 格**。

### 2.3 三个"光效种子"（★ 这是最容易看不懂的部分）

Bedrock 骨骼**没有默认缩放**：新骨骼如果不在每段动画里被缩放，就永远 `scale=1` 可见
（比如晶核外壳会永远包住晶核）。所以做法是：

- 每个光效骨骼挂一个 **0.05³ 的种子 cube** —— 静止时小到看不见；
- 光效片段 `glow_flash` 把它放大 **30~44 倍** → 一瞬间的亮球；片段结束自动缩回不可见。

| 骨骼 | parent | 种子位置 | 放大倍数 | 含义 |
|---|---|---|---|---|
| `blade_glow` | `weapon` | (0, 12.00, 0) | ×40 | 剑身中部 |
| `eye_glow` | `eye_mount` | (0, 4.20, 0) | ×30 | 虹膜中心 |
| `crystal_glow` | `crystal_link` | (0, -3.10, 0) | ×44 | 晶核中心 |

三个种子**共用同一个 UV 岛** `(144,176) 24×24`，画的是亮紫渐变。
生成器：`tools/model3d/v3_glow_bones.cjs`（幂等；**必须在贴图重新生成之后跑**）。

### 2.4 UV 岛（5 个 + 光效岛）

| 部件 | 岛位（px） | 画法 |
|---|---|---|
| `charge_orb` | 160,80 24×24 | 径向渐变：中心最亮 → 外圈最暗 + 描边 |
| `charge_core` | 186,80 16×16 | 中心亮 → 边缘次亮 |
| `eye_mount` 眼眶 | 206,80 40×24 | 近黑 + 上下受光边 + 两侧过渡 + 凹槽暗紫 |
| `barb_lo/hi` 倒钩 | 200,104 32×24 | 上亮下暗 + 外侧**冷灰**（不要用近白，22.5° 斜视会成白点）+ 内侧压暗 + 暗紫裂纹 |
| `tip_bevel` 斜切 | 208,176 32×24 | 冷灰受光斜面 + 上沿细亮边 + 下沿接缝暗 + 中间暗紫 |
| **光效种子 ×3** | 144,176 24×24 | 亮紫渐变（三处共用） |

> ⚠️ **新开 UV 岛必须同时写画法**，否则采到空白/串色 = 游戏里出现"怪色块"。
> 这是 v3 踩坑清单第 9 条。

### 2.5 几何里几个"刻意为之"，不要当成 bug 改掉

- **刀身三级厚度阶梯**：脊侧最外 x = 2.00 / 2.28 / 2.13 / 2.42 —— 这是**刻意保留的锯齿刻口**，用户明确要求。
- **相邻方块互相重叠 0.05**，不是严丝合缝 —— 避免共面 z-fighting（YSM 专业模型大量用 inflate 也是同一目的）。
- **倒钩静态后掠 22.5°**、深 1.3、geo x 1.55~2.55。
- **45° 斜尖**：枢轴取剑尖 `tip_b` 的脊角 (2.20, 19.35)，薄板 1.10×0.40×**0.95（与相邻台阶同厚 ⇒ 面齐平）**，`rotation [0,0,-45]`。
- **中缝血槽 1.45（凹）**。

---

## 三、动画 —— 9 段

全部在 `animation.ftboss.shadow_sword.*` 命名空间下。

| 片段 | 时长 | loop | 涉及骨骼数 | 作用 |
|---|---|---|---|---|
| `idle` | 4.0 s | **true** | 9 | 待机：瞳孔/晶核/聚能球呼吸 |
| `charge` | 0.8 s | false | 7 | 蓄力（聚能球渐显） |
| `release` | 0.5 s | false | 7 | 发射 |
| `recover` | 0.6 s | false | 5 | 收束 |
| `cast` | 0.9 s | false | 9 | 起手（聚能球最大 @0.349 s） |
| `cancel` | 0.2 s | false | 8 | 取消（聚能球立刻归零） |
| `cooldown_ready` | 0.65 s | false | 5 | 冷却完成那一下（回响） |
| `ready_idle` | 2.4 s | **true** | 3 | 就绪待机（魔眼/竖瞳/晶核低幅呼吸） |
| `glow_flash` | 0.35 s | false | 3 | 三处种子爆亮（0→40/30/44→0） |

**只有 `pupil` / `crystal` / `charge_orb` / `charge_core` 有 position 轨道；
`root` / `weapon` / `grip_anchor` / `handle` 都没有位移轨道**（这点很重要：剑的"位置"全部由 `display` 决定，不是动画）。

### 3.1 控制器（`ShadowSwordItem#registerControllers`）

```java
controllers.add(new AnimationController<>(this, "weapon_controller", 5, state -> {
    if (echoPulse) { echoPulse = false; return state.setAndContinue(COOLDOWN_READY); }
    return state.setAndContinue(echoReady ? READY_IDLE : IDLE);
}));
controllers.add(new AnimationController<>(this, CTRL /* "skill" */, 0, state -> PlayState.STOP)
        .triggerableAnim("cast",    CAST)
        .triggerableAnim("charge",  CHARGE)
        .triggerableAnim("release", RELEASE)
        .triggerableAnim("recover", RECOVER)
        .triggerableAnim("cancel",  CANCEL)
        .triggerableAnim("glow",    GLOW_FLASH));
```

**"技能已就绪"状态写在 ItemStack 的 NBT 里**（键 `ftboss_ready`，常量 `ShadowSwordItem.TAG_READY`）：
服务端在 `inventoryTick` 里读持有者冷却 → 写 NBT → 随背包同步下发 → 渲染器与控制器都读这个标记。
⇒ 每把剑 / 展示框里的剑 / 别人手里的剑**各自独立**，不会跟随本地玩家的冷却。

### 3.2 ★★ 关键帧时间键必须升序（2026-09-16 反汇编 GeckoLib 4.8.4 实证）

GeckoLib 4.8.4 解析关键帧时**假设时间键是升序的**。乱序的键会导致：
- 插值取错区间 → 动画在中途跳到错误姿态；
- 某些情况下直接抛异常。

本工程为此写了三个工具，改完动画**必须跑**：

```bash
node tools/model3d/anim_key_order_scan.cjs     # 只扫描报告，不改
node tools/model3d/anim_key_order.cjs          # 修复（幂等，写盘前备份）
node tools/model3d/anim_key_order_verify.cjs   # 验证
```

---

## 四、Java 侧：技能链路

### 4.1 文件地图

| 文件 | 职责 |
|---|---|
| `item/weapons/ShadowSwordItem.java` | 物品本体：GeoItem 注册、控制器、ready NBT、`use`/`releaseUsing`、**`teleportStrike`（传送结算）**、`flashGlow` |
| `client/render/ShadowSwordGeoRenderer.java` | GeckoLib 渲染器：模型/贴图/动画三个 ResourceLocation + ready 贴图切换 + 挂 `SwordAxisDebugLayer` |
| `skill/ShadowCastManager.java` | **服务器状态机**：长按蓄力 → 第 7 tick 发射 → 第 18 tick 结束；提前松手/换物/死亡/登出/换维度 → 取消。尾部还有 `ShadowCastEvents`（事件）与 `ShadowCastHooks`（触发动画 + **在 `fired()` 里真的生成珍珠**） |
| `entity/ShadowPearlEntity.java` | 暗影珍珠投射物：无重力直线、扫掠线段判定、命中即 `impact()` |
| `skill/FTNetwork.java` | ★ 本模组**第一个自定义网络通道**（`ftboss:main`，协议版本 `"1"`） |
| `skill/ShadowSigilPacket.java` | S2C 包：落点坐标 + 是否有能量柱 |
| `client/render/ShadowSigilVisual.java` | ★ 客户端：地面魔法阵 + 能量柱的世界渲染 + 生命周期 |
| `item/weapons/ShadowDebugCommand.java` | `/ftshadow` 调试指令 |
| `client/render/SwordAxisDebugLayer.java` | 实机标定层（画坐标轴 + 刻度），默认关闭 |

### 4.2 状态机参数（`ShadowCastManager`）

```java
FIRE_TICK     = 7      // 第 7 tick 发射
END_TICK      = 18     // 第 18 tick 结束
FIRE_COOLDOWN = 40     // 发射时起算
CANCEL_COOLDOWN = 4
PEARL_SPEED   = 1.9F   // 格/tick
enum Phase { CHARGING, RELEASED, RECOVERING }
```

**取消的触发点**：玩家换手/换物、死亡、登出、换维度、服务器停止（都在 `ShadowCastEvents` 里）。
`CastSession` 保存的是**ItemStack 引用**（不是 item 类型）—— 换成另一把同级剑也必须取消旧施法。

### 4.3 `teleportStrike`（v2，2026-09-16 定稿）

```java
public static void teleportStrike(ServerPlayer sp, double x, double y, double z)
```

1. `findLandingY()`：从落点往下找 ≤3 格能站人的位置（脚下实心、身位两格空）；找不到返回 `NaN`。
2. 找到 → `teleportTo` + `resetFallDistance()` + `hurtMarked = true` + 末影传送音 + 行动栏提示。
3. **发法阵包**：`FTNetwork.sendSigilNear(sp, x, landY(或 y), z, true)` —— 见 4.4。
4. 范围伤害：**圆柱判定**（横向 `SKY_RADIUS = 3.0`，竖向 `SKY_HALF_HEIGHT = 2.5`），
   `SKY_DAMAGE = 5.0F` 走 `damageSources().indirectMagic(sp, sp)`（无视护甲，受魔法抗性减免）。
   PvP/友军保护：`canHarmPlayer` 不通过的目标**不吃伤害，但照样吃致盲与击退**。
5. 击退 `SKY_KNOCKBACK = 1.4` 向外 + 抬高 0.35；贴脸时用随机方向保底。
6. 特效：三层错开半径的地面环 + 上冲暗影柱 + 尘埃 + 云 + CRIT + 落点闪光 + 音效三层。
   **全部走 `util/Fx`**（`ring` / `particles` / `dust` / `sound`），不要手写粒子循环。
7. 日志：`[FTBoss][暗影传送] <玩家> 落点结算: 命中 N 个目标, 半径 3.0 格 / 5.0 点魔法伤害`

> **历史**：v1 是"传送到落点上方 14 格再砸下来"，有坠落伤害先于触地结算的致命缺陷（技能变自杀键），
> 以及卡住/登出/换维度的残留状态。v2 把整段下坠删掉，传送与结算是同一瞬间。
>
> **v158 的教训还留着**：`ShadowPearlEntity.impact()` 原本自己算一遍 AOE、又调 `teleportStrike`
> （后者内部还会再算一遍）→ **落点内生物吃 2 倍伤害**，而且两套半径不同（3.0 vs `SKY_RADIUS`）。
> 现在：**使用者是玩家时以 `teleportStrike` 为唯一结算点**；非玩家发射才退回 `impact()` 自己那套。

### 4.4 ★★ 法阵（暗影镇印）—— 这一节是新加的，也是最容易只做一半的地方

**背景**：`textures/fx/shadow_sigil.png`（256×256 魔法阵）与 `shadow_pillar.png`（32×128 能量柱）
早在 2026-09-15 就由 `tools/model3d/gen_sigil_fx.cjs` 生成好了，但**全工程零 Java 引用**
（`docs/model-assets.md` 里那句"无 Java 引用，保留待确认"就是这个缺口）。
用户报「暗影剑的传送法阵未生效」时，**不是参数错，是根本没实现**。

**为什么必须新增一条管线**：粒子与音效服务端会自动同步（`ServerLevel#sendParticles` / `playSound`），
**世界贴花绝对不会** —— 魔法阵既不是实体也不是粒子（粒子是点精灵，画不出阵列贴图）。
所以本工程 2026-09-16 才有了第一个 `SimpleChannel`。

**链路**：`teleportStrike` → `FTNetwork.sendSigilNear` → `ShadowSigilPacket`（S2C）
→ `ShadowSigilVisual.spawn` → `ClientTickEvent` 推进寿命 → `RenderLevelStageEvent.Stage.AFTER_PARTICLES` 出画。

**表现参数**（都在 `ShadowSigilVisual` 里，独立常量，改一行即可）：

| 常量 | 值 | 含义 |
|---|---|---|
| `SIGIL_SIZE` | 2.8 | 地面阵外接正方形边长（格）= 生成器里外环的视觉直径 |
| `LIFE` | 32 | 生命周期 tick（1.6 秒） |
| `FADE_IN` / `FADE_OUT` | 5 / 10 | 淡入 / 淡出 tick |
| `SPIN_DEG` | 100 | 整个生命周期的自转角 |
| `PILLAR_SIZE` / `PILLAR_HEIGHT` | 1.05 / 2.8 | 能量柱单面宽 × 高（格） |
| `PILLAR_SLICES` | 4 | 绕 Y 轴均布的面片数 |
| `LIFT` | 0.03 | 抬离地面，防共面 z-fighting |
| `DEDUPE_TICKS` | 6 | 同点去重窗口 |
| `MAX_ACTIVE` | 24 | 活动表上限 |

**渲染类型**：`RenderType.eyes(tex)`（血怒发光层同款）= NEW_ENTITY + QUADS +
**ADDITIVE_TRANSPARENCY** + NO_CULL + LIGHTMAP。
对本图正合适：阵列主体是低 alpha 暗紫（叠加后是很淡一层雾），只有轮廓/符文/尖角是亮紫 ——
"只有亮的会发光、暗的几乎看不见"，正好对上生成器注释里那句"别做成发亮的饼"。
顶点 lightmap 必须传 `LightTexture.FULL_BRIGHT`，否则进洞里法阵跟着环境光变暗。

**几何朝向**：绕 X 轴 **−90°**（不是 +90°）放平。绕 X 轴 +90° 会把贴图的 v 正方向翻到世界 −Z，
圆环看不出来，但四个尖角与 8 个符文是四向的，翻过来会南北镜像。

**调试**：`/ftshadow sigil | flat | here <x y z> | strike`（需 OP）；
`-Dftboss.debugSigil=true` 打客户端「登记 / 过期移除」日志。

### 4.5 六个已确认的 API 坑（都别再试）

| # | 坑 | 正确写法 |
|---|---|---|
| 1 | `PoseStack` **没有** `rotate(float,float,float,float)` | 用 `pose.mulPose(Axis.YP.rotationDegrees(deg))` |
| 2 | `PoseStack` **也没有** `rotate(Quaternionf)` | 同上（`com.mojang.math.Axis`） |
| 3 | `NetworkRegistry` 没有 `acceptVanillaOrMissing` | 1.20.1 叫 **`acceptMissingOr(String)`** |
| 4 | `PacketDistributor.TargetPoint` 第 1 个参数是 **`excluded`**（要排除的玩家），不是"发起者" | 要发给本人就传 **`null`** |
| 5 | `PacketDistributor.NEAR` 第 4 个参数是**半径的平方**（字段名就叫 `r2`） | 传 `64*64` 表示 64 格 |
| 6 | `GeoRenderer` 的 `modelRenderTranslations` / `itemRenderTranslations` 是 `protected`，跨类读不到 | 用 `poseStack.last().pose()`（公开 API） |

> 第 4 条是**实际造成过故障**的：我第一次写成 `new TargetPoint(source, ...)`，
> 结果把施法者本人排除掉了 —— 单人/局域网里视野内只有他一个，**一个包都发不出去**。
> 症状是"服务端结算日志正常、客户端一条日志都没有"，极易误判成渲染问题。

---

## 五、拿到手里怎么摆 —— display 定位

### 5.1 当前值（2026-09-16 20:36 定稿）

`assets/ftboss/models/item/shadow_sword.json`：

```json
"thirdperson_righthand": { "rotation": [0, -90, -20], "translation": [9.4, -0.73, 0.6], "scale": [0.85, 0.85, 0.85] },
"thirdperson_lefthand":  { "rotation": [0,  90,  20], "translation": [9.4, -0.73, 0.6], "scale": [0.85, 0.85, 0.85] },
"firstperson_righthand": { "rotation": [0, -90,  25], "translation": [1.13, 3.2, 1.13], "scale": [0.68, 0.68, 0.68] }
```

**换算**：`位移量(格) = 单位数 × scale(0.85) ÷ 16` ⇒ **半格 = 9.4 单位**。

### 5.2 三个平移分量的分工（`rotation = [0,-90,-20]` 时）

| 分量 | 管什么 |
|---|---|
| `trans.x` | **纯前后**（沿角色朝向） |
| `trans.y` | **上下**（＝柄穿进拳里的深度） |
| `trans.z` | **横向**（靠近 / 远离身体） |

> ⚠️ **只有"哪个轴管哪件事"是确定的；符号方向受手部局部空间的基影响，必须实机确认。**
> 那个基由渲染管线决定，离线算式里没有它 —— 所以离线算出来的握持高度与游戏里的必然差一个常数。
> 详见 `tools/model3d/shadow_sword_pos_audit.cjs` 的 §C。

### 5.3 改位置的正确流程（**不用重建、不用重启**）

```bash
node tools/model3d/sword_display_tune.cjs show                     # 看当前 4 个槽位
node tools/model3d/sword_display_tune.cjs set thirdperson_righthand trans=9.4,-0.73,0.6
node tools/model3d/sword_display_tune.cjs apply <预设>              # body-in / body-out / back-half / fwd-half / blade-up / grip-deeper ...
node tools/model3d/sword_display_tune.cjs reset                    # 回到 v19 冻结值
```

改完**进游戏按 F3+T 重载资源**即可看到 —— `display` 是资源，不需要 gradle、不需要重启客户端。
（对比：改了 Java 或新增包就必须重建 + 重启。）

备份：`ref/shadow_sword.v19.display.bak`（v19 冻结值）、`_dev_backup/.../*.tune.bak` / `*.grip.bak` / `*.before_grip_fix.bak`。

### 5.4 实机标定工具（当上面那套试不出来时用）

`client/render/SwordAxisDebugLayer.java` —— 加 JVM 参数 `-Dftboss.debugSwordAxes=true` 开启：

- 原点处的**白十字** = `display` 的原点 = 握点基准；
- 三根轴各长 1 格：**红=+X、绿=+Y（指向剑尖）、蓝=+Z**；
- 沿 +Y（刃侧）每 8 模型单位一道**白**刻度；沿 −Y（柄侧）每 8 单位一道**黄**刻度 —— 黄刻度那串才是用来对"手心落在哪"的；
- 日志同时打出当帧原点坐标（`[FTBoss][剑坐标轴] ...`）。

> 刻度臂长是 12 单位、轴长 1 格 —— **不要调小**。第一版臂长只有 3 单位，用户实机截图里只有几个白点露出来，三根轴全被剑身埋住。

---

## 六、贴图与像素风格标准

### 6.1 现状实测

| 文件 | 大小 | 说明 |
|---|---|---|
| `shadow_sword.png` | 9,862 B | 256×256，逐面 UV |
| `shadow_sword_ready.png` | 9,031 B | 就绪态，**只提亮 `b-r>20` 的偏紫像素**，中性暗色剑身一个像素没动 |
| `shadow_sword_e.png` | 1,769 B | 发光层贴图，**目前无引用** |
| `textures/fx/shadow_sigil.png` | 13,243 B | 256×256 地面魔法阵 |
| `textures/fx/shadow_pillar.png` | 2,099 B | 32×128 暗影能量柱 |

### 6.2 ★★ 「游戏里看不清」的真正成因（别再往"降分辨率"方向改）

用户原话：「优化一下暗影剑的贴图，太高分辨率反而游戏里不清晰」。实测推翻了"分辨率太高"这个假设：

- 剑长 **1.5 格**，视距 5 格时屏幕上约 **232 px**，贴图 **256 列** → 压缩比仅 **1.1** ——
  **分辨率根本没浪费**，降分辨率只会更糊。
- alpha **已经是二值的**（0–15 占 39.7% / 240–255 占 52.1% / 16–191 **全为 0**）——
  "半透明被 mipmap 掺灰"的猜测**不成立**（`mipmapLevels:2` 是玩家客户端设置，不是元凶）。
- **真凶是明度**：优化前 `shadow_sword.png` 亮度 `<40` 的实体像素占 **66.2%**、
  `<20` 占 **35.1%**、亮部只有 **0.7%**、动态范围仅 **55** —— 在暗环境/夜晚/洞里就是一团看不清的黑块。
- 优化后：`<20` **0.0%**、`<40` **33.3%**、亮部 **3.3%**、动态范围 **67**；
  alpha 逐像素比对 **65536/65536 完全相同** → 形状/镂空一点没动。

工具：`tools/model3d/shadow_sword_tex_fix.cjs`（只重排明度，色相与 alpha 一位不动；
两张贴图用**同一组曲线**处理，否则切换时轮廓会跳变）。备份 `*.lumfix.bak`，`--restore` 还原。

> ⚠️ **重跑 `regen_shadow_sword.cjs` 会覆盖这次优化**（它按旧调色板重画贴图）。
> 已在生成器写盘处加了醒目提示 —— 重跑后必须再跑一次 `shadow_sword_tex_fix.cjs`。

### 6.3 像素密度标准（2026-09-16 定）

工程标准：**主体 1–2 px/模型单位**，刃口/魔法眼可以更细，**>3 px/单位 = 复查标记**。
检查器：

```bash
node tools/model3d/check_pixel_style.cjs <geo.json> <texture.png>
```

暗影剑当前的实测报告（**结论：明显偏精细，但用户尚未决定是否重做 UV**）：

```
已检查面 378  跳过 0
整模密度分位: p50 12.667  p75 19.862  p90 69.44  max 480

★ 去掉光效种子块后的主体：
    排除骨骼 blade_glow, eye_glow, crystal_glow, charge_core
    面数 354   中位密度 12.54 px/单位   高密度面积占比 100.0%
    主体中位密度 12.54 px/单位，超过目标 2 的 1.5 倍 → 纹理明显偏精细

按骨骼（平均密度降序前几名）：
    blade_glow 480 / eye_glow 480 / crystal_glow 480   ← 0.05³ 种子块 + 24×24 UV，必然爆表，属预期
    eye_mount 70.47（面积 58.45）  crystal_link 66.05  charge_core 40  pupil 37.28  crystal 21.68
```

> **这 12.54 不是 bug，是设计选择的后果**：贴图是"整把刀按 k=0.68 UV/单位画成一张 2D 侧视图"，
> 主体中位密度自然落在 12.5 左右。要真降到 1–2 必须**重排 UV 并重画图集**，
> 那会动到全部 378 个面的贴图对齐 —— 属于未决事项，**不要在没确认的情况下自动缩图或改 UV**
> （检查器自己也只出提示，不写盘）。

### 6.4 相关工具

| 工具 | 作用 |
|---|---|
| `check_pixel_style.cjs` | 像素密度检查（本文件 6.3 的那张报告） |
| `shadow_sword_tex_fix.cjs` | 明度重排（`--dry` 预览 / `--restore` 还原） |
| `shadow_sword_de_densify.cjs` | 颜色步进量化 + 去杂点（**不改变 px/单位**，只压色数）；备份 `.dedens.bak` |
| `check_uv_coverage.cjs` | 判断"这张贴图到底配不配这个模型"（采样每个面的 UV 区域，落在一半以上透明像素的面会列出来） |
| `uv_atlas_check.cjs` | UV 图集重叠/越界检查 |
| `check_model_quality.cjs` | 综合质量检查 |
| `check_shadow_struct.cjs` | 暗影剑专用结构检查（疑似脱离 / 稳定性 / UV 复用组） |
| `check_sword_compat.cjs` | **门禁**：已有动画逐段不变、骨骼/pivot/rotation 不变（允许新增） |

### 6.5 复现链（**顺序不能换**）

```
node tools/model3d/regen_shadow_sword.cjs        # 静态 Java 模型 → geo（8 条剑刃/节带/断续刃口/三级剑尖）
node %TEMP%\switch_shadow_entity.cjs             # 备份静态版 + 换 builtin/entity 存根
node tools/model3d/java_to_geo_rig.cjs shadow_sword shadow_sword shadow_sword ref/shadow_sword_static_v18.json.bak
node tools/model3d/add_cast_skill.cjs            # 聚能骨骼 + cast 动画（会覆写动画文件！）
node tools/model3d/upgrade_cast_v2.cjs           # 施法 v2（渐入/复位/cancel）
node tools/model3d/add_ready_feedback.cjs        # 裂瞳回响：两段动画 + ready 贴图
node tools/model3d/v3_glow_bones.cjs             # ★ 光效种子（必须在贴图重生成之后跑）
node tools/model3d/v3_uv_islands.cjs             # 画岛：聚能球/亮核/眼眶
node tools/model3d/v3_barbs.cjs                  # 倒钩（geo 后处理 + 画岛，幂等）
node tools/model3d/v3_tip_bevel2.cjs             # 45° 斜切（geo 后处理 + 自检 + 画岛，幂等）
node tools/model3d/anim_key_order.cjs            # ★ 关键帧时间键升序修复
node tools/model3d/shadow_sword_tex_fix.cjs      # ★ 明度优化（必须在贴图重生成之后！）
```

**改完必跑**：
`check_sword_compat.cjs`（门禁）→ `check_shadow_struct.cjs` → `check_pixel_style.cjs`
→ `sword_preview.cjs render`（出图）→ `export_bbmodel.cjs`（导出 Blockbench 工程）。

---

## 七、"模型学习"—— 从 YSM 专业模型学到的东西

这一节记录的是**方法论**，来源是用户提供的参考模型
`C:\Users\YOUR_USERNAME\OneDrive\桌面\yes_steve_model`（YSM）里的专业模型，以及本工程一路踩出来的结论。
下一轮继续优化模型时，**先读这一节**。

### 7.1 参考对象

| 参考文件 | 规格 | 学到什么 |
|---|---|---|
| `alex/models/main.json` | 64×64 / 61 骨骼 / 47 cube | 逐面 UV 100%；**62% 的 cube 带 rotation**；13 个用 inflate |
| `custom/default_controllers/models/main.json` | 256×256 / 133 骨骼 / 241 cube | 逐面 UV 100%；极端精细的骨骼粒度 |

### 7.2 五条可直接套用的做法

1. **逐面 UV，不要六面共用一块被拉伸的图。**
   本工程 v9 定版时把 21 个部件里的 19 个改成逐面：宽面取整刀侧视图、
   薄侧面取"刃口截面/刀背截面/柄/吞口"材质条、顶底取盖面。
2. **用旋转件做形状，不要只用轴对齐方块。**
   45° 斜尖、22.5° 后掠倒钩、45° 自转的聚能球 —— 都是 rotation。
   **但 rotation 一定要走 geo 后处理脚本**（`v3_barbs.cjs` / `v3_tip_bevel2.cjs` 那套），
   ✗ **不要走生成器的 `put()`+rotation** —— 枢轴空间不一致会把整块甩到 7 单位外
   （用户当时看到的是"侧边悬浮方块"）。
3. **相邻件互相重叠 0.05，不要严丝合缝。** 避免共面 z-fighting。YSM 大量用 inflate 也是同一目的。
4. **相邻件保持同厚**，否则薄片斜插进去会从侧面露出小块（倒角/背侧切面/旋转刀尖都会）。
   需要斜构件时用**与相邻件同厚的整块**。
5. **写完先自检再落盘。** 新加的旋转件必须满足"与主体最大重叠体积 ≥ 0.2、不凸出轮廓、不超高"，
   不满足就 `exit(1)` 拒绝写入。这条已经两次救场。

### 7.3 十二个踩过的坑（**别重犯**）

1. **Bedrock cube 旋转是数组 `[x,y,z]`**（Java 是 `{origin,axis,angle}`）→ 抄错 =
   `GeckoLibException` + 资源重载卡死。
2. **原点用"握点"**（GeckoLib 武器约定：手抓处 = (0,0,0)）；用方块中心（y−8）→ 第三人称抬高 3.1 格。
3. **父子变换必须"子先作用 → 逐级回根"**（写反 = "护爪看着断开"的假象）。
4. **悬浮检测**：只测未旋转 AABB 会漏"旋转后脱离"；只按 AABB 又会误报（瞳孔/晶核/聚能球）→
   用完整父级变换 + 跳过零缩放。
5. **斜插半埋薄片从侧面必露小块** → 见 7.2 第 4 条。
6. **`rotation` 走 geo 后处理**，不走生成器（见 7.2 第 2 条）。
7. **写完先自检再落盘**（见 7.2 第 5 条）。
8. **cube 不要写 `name` 等额外字段**；严格校验的渲染器会直接报错。
9. **新开 UV 岛必须同时写画法**，否则采到空白/串色 = 怪色块。
10. **近白 `#EAD5F5` 要克制**：连续刃口线会变成"白色灯带" → 要断续（每 0.36 画 0.16）；
    倒钩外侧用冷灰。
11. **`preview_spin.cjs` 对 `builtin/entity` 模型无效** → 用 `sword_preview.cjs` / `render_geo.cjs`。
12. **环境坑**：
    - PowerShell 的 `cd` **不影响 .NET 文件 API 的相对路径**（用绝对路径）；
    - `node -e` 的双引号会被 PowerShell 吃掉（改成 here-string 喂 stdin）；
    - `[ ]` 文件名要 `-LiteralPath`（`mods\[前置]geckolib-*.jar` 就是这种）；
    - `getCurrentItemStack()` 是**实例方法**，不能在静态嵌套类里调用（贴图切换必须写在渲染器外层）。

### 7.4 工具可信度（**哪些能信、哪些不能**）

| 工具 | 能信 | 不能信 |
|---|---|---|
| `geo_transform.cjs` | 变换/UV 解析（A~F 校准 PASS） | 不含渲染 |
| `sword_preview.cjs` | Z-buffer 渲染、逐像素采样、报告 JSON | 无控制器混合 / 无 Molang / 无 Box UV |
| `check_shadow_struct.cjs` | 疑似脱离、稳定性、UV 复用组 | AABB 重叠**只是嫌疑**，不要据此删几何 |
| `check_sword_compat.cjs` | 骨骼/pivot/rotation 不变、动画逐段不变 | 不判视觉 |
| `render_geo.cjs` | 快速多视角（画家算法） | 无 z-buffer |
| `hand_check.cjs` | 第三人称比例参考（共享 Z-buffer / 抠图采样 / UV 表 8 项查表） | 后背面剔除默认关（镜像几何会误判） |
| `check_pixel_style.cjs` | 逐面 px/单位密度分位 | 只出提示，**不写盘** |

---

## 八、MCP / 工具接入（给 AI 用的那部分）

### 8.1 Blockbench MCP —— 让 AI 直接驱动 Blockbench

工程里有一套**已经跑通**的 Blockbench MCP 接入，位置在**游戏实例目录**（不在 mod 源码仓库里）：

```
C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales Forge\.dsh-analysis\mcp\
├── mcp.js     ← Blockbench 插件本体（Blockbench MCP Server，v1.6.1，作者 Jason J. Gardner）
└── bb.cjs     ← 零依赖 Node 客户端（自己写的，直接 fetch，不需要装任何包）
```

**服务端**：`mcp.js` 是 Blockbench 插件，装进 Blockbench 后在 `localhost:3000` 起一个
**streamable HTTP MCP 端点 `/bb-mcp`**。它暴露的是 **Blockbench 的实时状态**：
`ModelProject` 列表、每个工程的骨骼/元素/贴图/动画数量、以及一大票可调用的工具与 prompts
（内置工具里能看到 `hytale_*` 相关的 prompt：角色/道具建模、动画工作流、附件系统等）。

**客户端**：`bb.cjs` 用法

```bash
node bb.cjs list                        # 列出当前可用的所有工具
node bb.cjs call <tool> '<json参数>'    # 调用工具
node bb.cjs raw <method> '<json参数>'   # 直接发任意 MCP 方法
node bb.cjs ping                        # 探测服务是否在线
```

- 端点可用环境变量覆盖：`BB_MCP_URL`（默认 `http://localhost:3000/bb-mcp`）；
- 会话 id 自动缓存到同目录 `bb-session.txt`，重启 Blockbench 后若报 404「Session not found」，
  删掉那个文件重跑即可；
- 响应同时兼容 `application/json` 与 `text/event-stream`（SSE）两种返回。

**典型用法（本工程的模型工作流）**：

```bash
node bb.cjs ping
node bb.cjs list | head -40                     # 看有哪些工具
node bb.cjs raw resources/list                  # 列 Blockbench 里打开的工程（projects://<slug>）
node bb.cjs raw resources/read '{"uri":"projects://shadow-sword"}'
```

> ⚠️ **注意分工**：MCP 这条路走的是 **Blockbench 工程**（`.bbmodel`），
> 而游戏实际吃的是 `geo/*.geo.json` + `animations/*.animation.json`。
> 两边互转用工程自带的 `export_bbmodel.cjs`（导出）/ `import_item_model.cjs`（导入），
> **不要手改 `.bbmodel` 的 JSON**。

### 8.2 另一条路：直接反汇编核对 API（不依赖任何 MCP）

这是本工程**最常用**的"事实核对"手段，比查文档可靠：

```powershell
# 1) 定位缓存里的 jar
$jar = "$env:USERPROFILE\.gradle\caches\forge_gradle\mcp_repo\net\minecraft\joined\1.20.1-20230612.114412\joined-1.20.1-20230612.114412-srg.jar"

# 2) 看签名（注意：SRG 名，不是 MCP 名）
& 'C:\Program Files\Java\jdk-25.0.2\bin\javap.exe' -p -classpath $jar net.minecraft.client.renderer.RenderType
& 'C:\Program Files\Java\jdk-25.0.2\bin\javap.exe' -p -c -classpath $jar net.minecraft.server.players.PlayerList   # -c 看字节码

# 3) 把人名映射到 SRG：Mojang 官方映射表
#    $env:USERPROFILE\.gradle\caches\forge_gradle\minecraft_repo\versions\1.20.1\client_mappings.txt
#    例：RenderType.eyes(...) -> o   （再用 joined.tsrg 找 m_110458_）
Select-String -Path "$env:USERPROFILE\.gradle\caches\forge_gradle\minecraft_repo\versions\1.20.1\client_mappings.txt" -Pattern 'RenderType eyes'

# 4) Forge 自己的源码 jar（类名/方法名与写代码时一致，最好用）
$fs = "$env:USERPROFILE\.gradle\caches\forge_gradle\maven_downloader\net\minecraftforge\forge\1.20.1-47.4.10\forge-1.20.1-47.4.10-sources.jar"
# 用 System.IO.Compression.ZipFile 枚举条目（tar -tvf 的列宽对齐会变，别拿正则抠）
```

读别人 mod 的 jar 也用同一招（.NET 而不是 tar）：

```powershell
Add-Type -AssemblyName System.IO.Compression.FileSystem
$z = [System.IO.Compression.ZipFile]::OpenRead($jar)
$z.Entries | Where-Object { $_.FullName -match 'Xxx' } | Select-Object FullName, Length
$z.Dispose()
```

### 8.3 构建 / 部署（**有游戏在跑时不要覆盖 jar**）

```powershell
# 构建 + 部署到 4 处 + 清 lightspeed 缓存；检测到 MC 在跑就只构建、跳过拷贝
powershell -NoProfile -ExecutionPolicy Bypass -File tools\deploy_jar.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File tools\deploy_jar.ps1 -NoBuild   # 只部署
```

> ★★ **为什么部署脚本要检查"游戏在跑吗"**：2026-09-13 血的教训 ——
> 游戏运行时就地覆盖 `mods\qys-bosses.jar`，**文件没被锁、复制会"成功"**，
> 但 JVM 之后**懒加载**某个类时读到被改写的 jar → `NoClassDefFoundError` → 服务端崩溃。
> 崩的是"之后某一刻第一次用到那个类"的地方，离覆盖动作可能隔好几分钟。
> 需要临时构建验证时用**另一个工作目录**，不要碰 `mods\`。

JDK：`C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot`
Gradle：`%USERPROFILE%\.gradle\wrapper\dists\gradle-8.8-bin\*\gradle-8.8\bin\gradle.bat`

**改了 Java / 新增包** → 必须重建 + **重启客户端**。
**只改 `display` / 贴图 / UV** → 进游戏按 **F3+T** 重载资源即可。

### 8.4 系统属性开关（给 JVM 参数，不必改代码）

| 参数 | 作用 |
|---|---|
| `-Dftboss.debugSwordAxes=true` | 画剑的坐标轴 + 刻度尺，标定 display 用（第 5.4 节） |
| `-Dftboss.debugSigil=true` | 客户端打「法阵登记 / 过期移除」日志 |
| `-Dftboss.debugCleaverBlood=true` | 强制黑色切割者血怒展开（隔壁武器，不是暗影剑） |
| `-Dftboss.cleaverWingDeg=NN` | 覆盖黑色切割者刃翼张角（默认 11°） |

**游戏内调试指令**：

| 指令 | 作用 |
|---|---|
| `/ftshadow sigil` | 脚下画法阵 + 能量柱 |
| `/ftshadow flat` | 只画地面阵 |
| `/ftshadow here <x y z>` | 指定坐标画阵 |
| `/ftshadow strike` | 原地走一次完整传送结算（**会真的打到周围生物**） |
| `/ftcleaver give / blood / noblood` | 黑色切割者相关（隔壁） |

---

## 九、日志速查（排查时先看这些）

| 关键字 | 来源 | 含义 |
|---|---|---|
| `载入构建版本戳: <TAG>` | `FTBossMod` | **先确认这一行**，再判断行为是否符合预期 |
| `[FTBoss][暗影传送] <玩家> 落点结算: 命中 N 个目标` | `teleportStrike` | 服务端结算跑到了 |
| `[FTBoss][网络] 已发暗影镇印包: (...)` | `FTNetwork` | 包**发出去了**（诊断用，可删） |
| `[FTBoss][镇印] 客户端收到包 (...)` | `ShadowSigilPacket` | 包**到了客户端**（诊断用，可删） |
| `[FTBoss][镇印] 登记 / 过期移除` | `ShadowSigilVisual` | 需要 `-Dftboss.debugSigil=true` |
| `[FTBoss][暗影剑] 调用链② 构造 ShadowSwordGeoRenderer` | 渲染器构造 | 确认走的是自绘渲染器（诊断用，可删） |
| `[ShadowCast] START / FIRE / END / ABORT` | `ShadowCastHooks` | 施法状态机 |
| `[FTBoss][剑坐标轴] ... 当帧原点=[...]` | `SwordAxisDebugLayer` | 需要 `-Dftboss.debugSwordAxes=true` |

**实例日志路径**：`...\versions\FarmingTales Forge\logs\latest.log` / `debug.log`（后者有 DEBUG 级）。

---

## 十、未完成 / 未验证（**不要当成已完成**）

| # | 事项 | 状态 |
|---|---|---|
| 1 | **法阵实机验收** | 2026-09-16 修掉了 `excluded` 那个 bug（第 4.5 节第 4 条）并加了诊断日志，**但尚未经用户实机确认**。下一步：重启 → 放技能 → 看 `[FTBoss][镇印] 客户端收到包` 是否出现 |
| 2 | **暗影剑第三人称位置** | `translation` 由 `[9.4, 8.67, 10]` 改为 `[9.4, -0.73, 0.6]`（按用户"往右偏半格、往上偏半格"的反馈），**横向那一半的符号方向未实机确认**。若方向反了，把 `trans.z` 的 −9.4 换成 +9.4 即可 |
| 3 | **第一人称位置未动** | 用户只报了第三人称。`firstperson_*` 仍是 v19 值 `[1.13, 3.2, 1.13]` / scale 0.68 |
| 4 | **像素密度 12.54 px/单位** | 高于工程标准（1–2）。**要真降下来必须重排 UV + 重画图集**（会动到全部 378 个面的贴图对齐），属未决事项，等用户点头 |
| 5 | **`shadow_sword_e.png` 无引用** | 发光层贴图存在但没接。当前发光靠 `glow_flash` 片段放大三个种子 cube（用 `shadow_sword.png` 的 UV 岛），不走这张图 |
| 6 | **`ShadowPearlEntity` 仍是末影珍珠占位外观** | 等 `shadow_pearl.png` |
| 7 | **`WAS_READY` 冷却表未清理** | 登出 / 换维度时不移除表项（`ShadowSwordItem` 里的静态 `Map<UUID,Boolean>`），长期运行会缓慢增长 |
| 8 | **`cast @0.349s` 聚能球与魔眼/护手有 ≤1.14 轻微嵌入** | 要更干净就外移 `cast_socket` 0.2，属冻结项需用户点头 |
| 9 | **`handle × eye` 动画重叠最大 0.64** | 可优化 |
| 10 | **`regionOverlaps` 1768 条分类未收敛** | 要把"剑身相邻面刻意 0.05 重叠"与意外重叠分开 |
| 11 | **`zh_cn.json` 存量乱码** | 例：`"item.ftboss.shadow_sword": "鏆楀奖鍓?`。**故意没动**（改动面太大，且与暗影剑功能无关） |

---

## 十一、备份清单（`ref/`）

```
shadow_sword.v19.display.bak                display 的 v19 冻结值（改位置的还原点）
shadow_sword.v3pre.geo.json                 V3 前基准（门禁对照）
shadow_sword.v3pre.animation.json
shadow_sword.v3pre.png
shadow_sword.before_cast_v2.animation.json  施法 v2 前
shadow_sword.before_ready.animation.json    裂瞳回响前
shadow_sword.before_ready.png
shadow_sword_static_v18.json.bak            静态 Java 模型（转 geo 的输入）
shadow_sword_32.png.bak                     旧 32×32 贴图
shadow_sword_prev.json.bak
check_shadow_struct.v1.cjs                  旧结构检查器
```

贴图备份在源码旁边：`shadow_sword.png.lumfix.bak` / `.dedens.bak`（`ready` 版同理）。
其它目录：`_dev_backup\resources-2026-09-16\...`（本次会话的资源快照）、`_backup\cleanup-20260916\`。

---

## 十二、给接手者的第一小时建议

1. **确认版本**：读日志第一行 `载入构建版本戳`，确认跑的是哪个构建。
2. **确认现状**：跑一遍第 6.5 节的"改完必跑"清单里前三个检查器，看当前模型是否干净。
3. **确认未决项**：第 10 节里第 1、2 条是**本次会话刚改、还没实机验收**的，先跟用户确认。
4. **要改位置** → 走第 5.3 节（F3+T，不重建）。
   **要改几何/贴图** → 走第 6.5 节（复现链顺序不能换）。
   **要改 Java** → 先读第 4.5 节的六个 API 坑，再动手。
5. **不要**在没有用户确认的情况下：改 `display` 的 `rotation`、重排 UV、重跑 `regen_shadow_sword.cjs`（会覆盖贴图明度优化）、
   或在游戏运行时覆盖 `mods\qys-bosses.jar`。

---

*文档版本：v4 ｜ 2026-09-16 ｜ 对应构建：`build/libs/qys-bosses.jar`（33.04 MB，20:36 部署）*
*相关文档：`docs/SHADOW_SWORD_V3_HANDOVER.md`（**已过期**）、`docs/MODEL_REFERENCE_NOTES.md`、`AGENTS.md`、`docs/CHANGELOG.md` v157~v160*

---

## 附：本文件里哪些是"实测"，哪些是"我还没验证"

为了让接手者知道该信任到什么程度，逐条标注来源：

| 结论 | 来源 | 可信度 |
|---|---|---|
| 22 骨骼 / 63 cube / 9 动画段 / UV 岛坐标 / pivot 值 | 本次直接解析 `geo.json` + `animation.json` 打印 | **实测** |
| 贴图文件大小、`display` 当前值 | 直接读文件 / 读 jar 内条目 | **实测** |
| 像素密度报告（12.54 等） | 直接跑 `check_pixel_style.cjs` | **实测** |
| `PoseStack` 没有 `rotate(float×4)` / `rotate(Quaternionf)` | javac 编译报错原文 | **实测** |
| `NetworkRegistry.acceptMissingOr` / `TargetPoint.excluded` / `NEAR` 第 4 参是半径平方 | 读 Forge 47.4.10 源码 jar + `javap -c` 反汇编 `PlayerList.broadcast` 字节码 | **实测** |
| `modelRenderTranslations` / `itemRenderTranslations` 是 `protected` | `javap -p` 看 GeckoLib 4.8.4 jar | **实测** |
| 法阵 bug 的成因（施法者被排除） | 字节码 + 用户日志（服务端有结算日志、客户端零日志） | **实测 + 日志互证** |
| 法阵"现在能显示了" | —— | ✗ **未验证**（改完还没跑过） |
| 暗影剑位置"现在在手里了" | 按用户"右半格 / 上半格"的反馈反推 | ✗ **未验证**；横向符号方向尤其不确定 |
| v3 文档里的 "61 cube / 19 骨骼" | 旧文档 | **已过期**，本文件已更正 |
| `shadow_sword_e.png` "无引用" | 全工程 grep | **实测**（但可能是"给未来留的"，不是死资源） |
