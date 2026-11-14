# 模型与开发资源对应清单（model-assets）

> 建立于 2026-09-16。本文回答一个问题：**每个模型从哪来、谁在用它、怎么改、怎么验**。
> 凡是无法用证据确立的关联，一律写 **未确认**，不猜、不靠"看起来像"。

## 0. 结论速查

| 项 | 数量 |
|---|---|
| geo 模型 | 23 |
| 动画文件 | 21 |
| 逐面 UV 模型 | 3（shadow_sword、small_rat、big_rat 系列同源） |
| 有 Blockbench 源工程的模型 | **0 个已确认**（见 §4） |
| 本次做骨排序的 geo | 3（shadow_sword、compost_superman、compost_superman_p2） |
| 本次修正关键帧键序的动画 | 5 个文件 / 109 条轨道 |
| 运行时资源里的开发残留 | 0（已搬到 `_dev_backup/`） |

## 1. 目录约定（本工程现状 + 本次整理）

```
src/main/resources/assets/ftboss/       ← 运行时资源, 只有这里的东西会进 jar
  geo/            23 个 *.geo.json      GeckoLib 模型
  animations/     21 个 *.animation.json
  models/item/    物品模型 JSON (display + builtin/entity 或 generated)
  models/block/   石像方块模型 (Java elements 格式)
  blockstates/    石像 blockstate
  textures/item|entity|block|effect|fx|mob_effect/
  lang/ sounds.json sounds/

tools/model3d/                          ← 活跃工具链 (生成器/校验器/渲染器)
tools/_archive/model3d/                 ← 历史一次性脚本 (fix_*/wire_*/patch_* 等)
art/                                    ← 预览图与检查报告
ref/                                    ← 单个资源的"上一版"备份 + 接线说明
_backup/                                ← 历史快照 (zip / 旧源码 / 本轮改动前原件)
_dev_backup/resources-<日期>/           ← 本次新增: 从 src/main/resources 搬出来的开发备份
docs/                                   ← 本目录
```

**规则**：开发脚本、预览图、历史版本**一律不进** `src/main/resources`。
`.gitignore` 已补 `_dev_backup/`、`*.reorder.bak`、`*.keyorder.bak`、`*.tilted-bak`；
`build.gradle` 已加 `processResources { exclude ... }` 双保险（`.gitignore` 挡不住 Gradle）。

## 2. 武器资源对应清单（重点）

### 2.1 噬影之刃 · 裂瞳（shadow_sword）

| 环节 | 实际值 |
|---|---|
| 物品注册 | `item/weapons/ShadowSwordItem`（`FTWeapons` 注册） |
| GeoModel / Renderer | `client/render/ShadowSwordGeoRenderer`（`GeoItemRenderer` + BEWLR） |
| geo | `geo/shadow_sword.geo.json` — identifier `geometry.ftboss.shadow_sword`，**22 骨骼 / 63 cube / 逐面 UV 63 / 负 uv_size 52** |
| 主贴图 | `textures/item/shadow_sword.png`（256×256） |
| 发光遮罩 | `textures/item/shadow_sword_e.png`（`_e` 约定，**静态扫描看不到，不是死资源**） |
| 就绪态贴图 | `textures/item/shadow_sword_ready.png` |
| animation | `animations/shadow_sword.animation.json` — 9 段：idle / charge / release / recover / cast / cancel / cooldown_ready / ready_idle / glow_flash |
| 骨骼树 | `root → weapon → {pommel → crystal_link → {crystal, crystal_glow}, handle, guard → {claw_left → claw_left_tip, claw_right → claw_right_tip, eye_mount → {eye → {pupil, cast_socket → charge_orb → charge_core}, eye_glow}}, blade → blade_glow}`，另有 `grip_anchor`(root 下) |
| 挂点/动画节点（无 cube，**禁删**） | `root`、`weapon`、`grip_anchor`、`cast_socket` |
| Blockbench 源 | **未确认**。`art/shadow_sword_v3.bbmodel` 存在，但 `tools/model3d/export_bbmodel.cjs` 是**从 geo 反向导出** bbmodel 的（第 18 行），没有任何生成器**读取** bbmodel。`art/import_shadow_sword.{geo,animation}.json`（19 骨骼 / 58 cube）是导入侧痕迹，与 `C:\Users\YOUR_USERNAME\Desktop\暗影剑V3导入\` 下同名文件成对；现行 22/63 的 geo 由它经 patch 链演化而来，但**没有 1:1 的对应证据** ⇒ 标未确认 |
| 维护源（权威顺序） | ① `tools/model3d/regen_shadow_sword.cjs`（静态模型 + 贴图）→ ② `java_to_geo_rig.cjs`（Java 模型 → geo/动画骨架）→ ③ `add_cast_skill.cjs` → ④ `add_attack_anims.cjs` → ⑤ `add_ready_feedback.cjs` → ⑥ `patch_shadow_v20…v27.cjs`（8 个都在） |
| 派生输出 | `geo/shadow_sword.geo.json`、`animations/shadow_sword.animation.json`、`models/item/shadow_sword.json` |
| 结构体检 | `tools/model3d/check_shadow_struct.cjs`（悬浮/共面/相交/UV/握柄） |
| 位置审计 | `tools/model3d/shadow_sword_pos_audit.cjs`（原点是否落在握柄区间、动画有没有碰握持链、display 落点） |
| 握持参考图 | `tools/model3d/hand_check.cjs` → `art/hand_check.png`、`art/grip_up20.png`、`art/hand_check_left.png` |
| 预览/报告 | `art/shadow_v2..v13_*.png`、`art/shadow_structure_report.json`、`art/shadow_sword_quality.json`、`art/shadow_sword_pos_audit.txt` |

### 2.2 黑色切割者 · 赤痕（black_cleaver）

| 环节 | 实际值 |
|---|---|
| 物品注册 | `item/weapons/BlackCleaverItem`（`FTWeapons` 注册） |
| GeoModel / Renderer | `client/render/BlackCleaverGeoRenderer`（含 `BloodGlowLayer` 血怒发光层） |
| 血怒视觉状态 | `client/render/CleaverBloodVisualState`（进度状态机 + 客户端 tick 推进） |
| geo | `geo/black_cleaver.geo.json` — identifier `geometry.black_cleaver`，**17 骨骼 / 31 cube** |
| 主贴图 | `textures/item/black_cleaver_geo.png`（256² 逐面 UV 图集）—— **不是** `black_cleaver.png`（那是平面物品图标） |
| 血怒发光遮罩 | `textures/item/black_cleaver_blood_emissive.png`（`RenderType.eyes` 全亮） |
| 平面图标 | `textures/item/black_cleaver.png` + `models/item/black_cleaver.json` |
| 旧发光层 | `textures/item/black_cleaver_emissive.png` —— **已被 `_blood_emissive` 取代，无引用**（见 §5） |
| animation | `animations/black_cleaver.animation.json` — 6 段：idle / charge / slam / recover / ready / **blood_pose（仅模型预览用）** |
| 四片刃翼（Java 直接写骨骼） | `blade_upper_wing`、`blade_lower_wing`、`blade_right_upper_wing`、`blade_right_lower_wing` — 由 `BlackCleaverGeoRenderer.renderRecursively` 绕枢轴加偏移 |
| 两侧主刃 | `blade`（左）+ `blade_right` |
| 固定轴盖 / 中央斧眼 / 握柄 / 兼容空骨骼 | `fittings`、`axe_head`、`handle`+`haft`、`rear_hook`(空)、`impact_socket`(空定位器) |
| Blockbench 源 | **未确认**（同 §2.1 的理由） |
| 维护源 | `tools/model3d/gen_black_cleaver.cjs`（单一生成器，头部注释即列出它产出的 6 个文件） |
| 清单 | `ref/black_cleaver_manifest.json` 声明 `cubes: 31` — 与现行 geo **一致** ✓ |
| 接线说明 | `ref/black_cleaver_wiring_plan.md` |
| 结构/翼展检查 | `cleaver_check.cjs`（结构 + 刃翼拆分守门）、`wing_joint_check.cjs`（展开会不会撕裂轮廓）、`cleaver_bones_check.cjs`（Java↔geo↔blood_pose 三方对齐）、`wing_pose_bake.cjs` + `cleaver_wing_angles.cjs` |
| 预览/报告 | `art/black_cleaver_uv_preview.png`、`art/black_cleaver_quality.json`、`art/w_black_cleaver_{gui,fp}.png`、`art/cleaver_check.json` |

**双刃造型约束（规格要求，勿回退）**：保留两侧主刃 + 四个活动刃翼 + 固定轴盖 + 中央斧眼 + 握柄 + 兼容空骨骼。
**不得**整理回"单刃＋短钩"造型。

### 2.3 其余模型（简表）

| 模型 | geo | 主贴图 | 动画 | 备注 |
|---|---|---|---|---|
| 糖霜女皇 | `sugar_queen` 相关（见 `SugarQueenGeoRenderer`） | `textures/entity/…` | — | 铁魔法 `AbstractSpellCastingMob` |
| 屠夫 | （Java 模型，非 geo） | `butcher.png` | — | `ButcherBossModel` 手改过穿模；规格真源 `butcher_spec.cjs` |
| 沙耶 | `camellia_sprout.geo.json` | `camellia_sprout.png` | `camellia_sprout.animation.json` | 9 骨骼 / 45 cube |
| 酒桶王 | `wine_barrel_boss.geo.json` | `wine_barrel_boss_v2.png` | `wine_barrel_boss.animation.json` | 17 / 66 |
| 鼠群 | `big_rat` + `small_rat` | `big_rat.png` / `small_rat.png` | **共用 `rat.animation.json`** | 9 骨骼，骨骼名 100% 重合 |
| 故障核心 | `defect_core.geo.json` | `defect_core*.png`（5 种球色 + glowmask） | `defect_core.animation.json`（1.3 MB / 17 段） | 34 / 186 |
| 堆肥超人 | `compost_superman.geo.json` | `compost_superman.png` (+`_p2`) | `compost_superman.animation.json` | 19 / 150 |
| 苹果爵士 | `apple_boss.geo.json` | `apple_boss.png` (+`_p2`) | `apple_boss.animation.json` | 37 / 116 |
| 五颗充能球 | `{dark,frost,glass,ion,lightning}_orb.geo.json` | 同名 png + glowmask | 各自 animation（共用 `animation.orb.*` 段名） | 贴图路径由代码拼接 |
| 茶芽/姜饼/蛆/便便/苍蝇等小怪 | 各自 geo | 同名 png | 各自 animation | 见 `client/model/` |

## 3. 本次骨骼排序（geo）——语义零差异

| 文件 | 骨骼 | 整理前 | 整理后 |
|---|---|---|---|
| `shadow_sword.geo.json` | 22 | `root→weapon→pommel→crystal→handle→guard→claw_left→claw_right→eye→pupil→blade→…→eye_mount→crystal_link→…` | `root→weapon→pommel→crystal_link→crystal→handle→guard→claw_left→claw_right→eye_mount→eye→pupil→blade→…` |
| `compost_superman.geo.json` | 19 | `…pole→crossbar→torso→head…` | `…pole→torso→crossbar→head…` |
| `compost_superman_p2.geo.json` | 19 | 同上 | 同上 |

原问题：`crystal`(#3) 排在父级 `crystal_link`(#14) 之前、`eye`(#8) 排在父级 `eye_mount`(#13) 之前（`crossbar`/`torso` 同理）。
同级骨骼保持原有相对顺序；**cube 数组一律未动**。

**为什么是语义零差异**（`geo_semantic_diff.cjs` 逐维度验证，全部 ✓）：
1. Bedrock geo 的 `bones` 是**扁平列表**，层级由 `parent` 字段决定，不由数组顺序决定；
2. GeckoLib 4.8.4 烘焙时（javap 核对 `BakedModelFactory`/`BakedGeoModel`）：只持有 `topLevelBones` + properties，
   找骨骼走 `getBone(String)` 按**名字**，渲染沿父子链递归 ⇒ 与数组顺序无关；
3. 本工程全部离线工具都按**名字**取骨骼（已 grep 核对），不依赖索引；
4. 每个骨骼对象的字段与取值、每个 cube 的顺序与内容、UV、包围盒、父子层级集合 **逐项相同**。

原件备份：`*.geo.json.reorder.bak`（已随开发残留一并搬到 `_dev_backup/resources-2026-09-16/`）。

## 4. Blockbench 源工程 —— 全工程标"未确认"

**证据**：`tools/model3d/export_bbmodel.cjs` 是**唯一**接触 `.bbmodel` 的工具，它**写出** `art/shadow_sword_v3.bbmodel`
（输入是 `geo/shadow_sword.geo.json`，见第 18 行）。**没有任何生成器把 `.bbmodel` 当输入读。**

⇒ 对**每一个**模型而言，"Blockbench 源 → 出厂 geo"这条链**都标 `未确认`**，包括两把武器。
现存的 `.bbmodel` 只有两个：`art/shadow_sword_v3.bbmodel`（由 geo 导出）
与 `art/butcher_standalone_v2_archived/butcher_boss.bbmodel`（屠夫 v2 归档）。

**因此**：本清单只声明"**维护源 = 生成器脚本**"，不声明"维护源 = bbmodel"。
不要创建空的 bbmodel 占位文件，也不要把 geo 改后缀冒充源工程。

## 5. 删除 / 归档 / 保留待确认

### 5.1 本次已搬走（**非删除**，保留原相对路径）

| 内容 | 去处 |
|---|---|
| 16 个开发备份（8 个旧 + 8 个本轮工具产物），566,444 B | `_dev_backup/resources-2026-09-16/` |
| 改动前 21 个动画原件 | `_backup/anim-keyorder-before-20260916/` |

**依据**：`audit.cjs` 直接打开上一版 `build/libs/qys-bosses.jar` 证实里面真的带着 8 个 `.bak`
（61,724 B）。`.gitignore` 的 `*.bak` 只挡 git，挡不住 `processResources`；本轮已加 Gradle 排除规则。

### 5.2 保留待确认（**未删除**，原位不动）

以下资源**当前无任何静态引用**，但**用途未确认或属于"尚未接线的功能"**，按规格一律保留原位：

| 资源 | 体量 | 判定 |
|---|---|---|
| `geo/compost_superman_p2.geo.json` | 66,781 B | 与 `compost_superman.geo.json` **字节完全相同**、identifier 也相同；`CompostGeoModel` 两阶段都硬编码用非 p2 的 geo（注释说明 p2 曾让 GeckoLib 加载失败）。**判为死资源但保留** |
| `sounds/effects/apple_boss_{attack,dash,death,seed}.ogg` | 81,720 B | 不在 `sounds.json`，也没有注册 SoundEvent ⇒ **确实发不出声**，保留待确认 |
| `textures/effect/cleaver_slam_{circle,column,runes,wave}.png` | 3,252 B | `gen_black_cleaver.cjs` 生成它们，manifest 自己写着"特效 PNG 需要客户端渲染代码接入"，**那段代码从没写** ⇒ 属于未接线功能，保留 |
| `textures/fx/shadow_{pillar,sigil}.png` | 15,342 B | ~~无 Java 引用~~ **2026-09-16 已接线**：`client/render/ShadowSigilVisual.java`（暗影传送落点的地面法阵 + 能量柱），走 `skill/FTNetwork` 的 S2C 包下发。不再属于未接线资源 |
| `textures/item/black_cleaver_emissive.png` | — | 被 `black_cleaver_blood_emissive.png` 取代，保留待确认 |
| `textures/item/cheese.png`、`cheese_wheel_mace.png` | — | `models/item/blue_cheese.json` 是孤儿模型，其贴图只靠 `cheese_wheel_mace.json` 存活，保留 |
| `textures/item/statue_{apple,butcher,rat_swarm,saya,sugar_queen,wine_barrel,youzi}.png` | — | 通用石像方块 2026-09-12 删除后留下的物品图标，保留待确认 |
| `models/item/{shadow_sword.json.*,turkey_legs.json.tilted-bak}` 等 | — | 已随 §5.1 搬走（不是保留） |

### 5.3 按"命名约定"被引用 —— **不是死资源**（静态扫描看不到属正常）

- `*_e.png`（7 个）—— OptiFine/Iris 发光层约定
- `*_glowmask.png`（13 个）—— GeckoLib `AutoGlowingTexture` 约定
- `textures/mob_effect/<id>.png`（12 个）—— 原版按 MobEffect 注册 id 自动取图
- `*_orb.*` / `defect_core_<tint>*` / `easter/<skin>.png` —— 代码里**字符串拼接**出的路径
- `lang/*.json`、`sounds.json`、`pack.mcmeta` —— 加载器按**固定名**读取

`audit.cjs` 现在把这类单独列在 `[12]`，不再混进"无引用资源"。

## 6. 复现链（改任何一处后按此跑）

```powershell
cd <工程根>
# 1) 模型/动画结构
node tools/model3d/validate.cjs --all
node tools/model3d/validate_anim.cjs --all
# 2) 关键帧键序（只读检查; 确认要改再加 --write）
node tools/model3d/anim_key_order.cjs --check --all
node tools/model3d/anim_key_order_scan.cjs src/main/resources/assets/ftboss/animations
# 3) geo 骨骼父级优先（只读检查; 确认要改再加 --write）
node tools/model3d/reorder_bones.cjs --check --all
# 4) 武器专项
node tools/model3d/cleaver_bones_check.cjs
node tools/model3d/wing_joint_check.cjs
node tools/model3d/check_shadow_struct.cjs
# 5) 全工程体检
node tools/model3d/audit.cjs .
# 6) 开发残留
node tools/model3d/dev_resource_cleanup.cjs --check
# 7) 构建
& $gradle -p . clean build --console=plain --no-daemon
```

**不要**在没跑预览/没做实测的情况下声称模型没问题（见 `tools/model3d/README.md` 与 `AGENTS.md`）。
