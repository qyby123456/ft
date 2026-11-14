# 工程整理记录（project-cleanup）— 2026-09-16

> 本轮做的事：**工程清单 → 资源整理 → 模型骨骼排序 → 关键帧键序修复 → 检查工具修复 → 构建验证**。
> 修改原则：保留玩法/伤害/冷却/网络行为/视觉时序；不升级依赖；不改数值与外观比例；不删除未确认资源。

---

## 一、改了什么、为什么

### 1. geo 骨骼排序（3 个文件）— 可维护性

| 文件 | 问题 | 处理 |
|---|---|---|
| `geo/shadow_sword.geo.json` | `crystal`(#3) 排在父级 `crystal_link`(#14) 之前；`eye`(#8) 排在父级 `eye_mount`(#13) 之前 | 排成稳定父级优先（根→父→子，同级保持原相对顺序） |
| `geo/compost_superman.geo.json` | `crossbar`(#4) 排在父级 `torso`(#5) 之前 | 同上 |
| `geo/compost_superman_p2.geo.json` | 同上 | 同上 |

**语义零差异已逐维度证明**（`geo_semantic_diff.cjs`）：骨骼集合/parent/pivot/rotation/自定义字段、
每个 cube 的顺序与内容、UV 写法计数、包围盒数值、父子层级集合 **全部逐项相同**；cube 数组一律未动。
**为什么渲染不受影响**：Bedrock geo 的 `bones` 是扁平列表，层级由 `parent` 决定；GeckoLib 4.8.4
只持有 `topLevelBones` + 按名字查骨骼（javap 核对 `BakedModelFactory`/`BakedGeoModel`）。

### 2. 动画关键帧时间键序（5 个文件 / 109 条轨道）— **真实缺陷修复**

**这是本轮最重要的发现。** 详见 `docs/model-assets.md` §"为什么关键帧时间键必须升序"与
`tools/model3d/README.md`。要点：

- GeckoLib `AnimationController.getCurrentKeyFrameLocation()` 用 `total += keyframe.length()` 的
  **前缀和**定位当前段；只有键按数值升序时前缀和才等于真正的时间轴。
- 整条加载链**无任何排序**（反汇编搜 `sort` 为空），键序 = JSON 书写顺序。

修正明细：

| 文件 | 乱序轨道 | 例子 |
|---|---|---|
| `apple_boss.animation.json` | 88 | `idle / root.position`：`[0,2,0.7,1.4]` → `[0,0.7,1.4,2]` |
| `compost_superman.animation.json` | 10 | `cannon_fire / cape3.rotation` 等 |
| `benger.animation.json` | 8 | `walk / spine.rotation`：`[0,1,0.125,…]` → `[0,0.125,…,1]` |
| `shadow_sword.animation.json` | 2 | `idle / claw_left_tip.rotation`、`claw_right_tip.rotation`：`[0,4,1.4,2.6]` → `[0,1.4,2.6,4]` |
| `black_cleaver.animation.json` | 1 | `idle / ember_core.scale`：`[0,3,1.5]` → `[0,1.5,3]` |

**只重排键、不动值**：键集合完全一致、每个关键帧的取值**逐字符不变**、动画段集合/长度/loop/骨骼集合未变
（由不依赖该工具的独立验证器 `anim_key_order_verify.cjs` 验证，21/21 通过）。

原件：`_backup/anim-keyorder-before-20260916/`（21 个文件）。

> ⚠ **这条修复会改变这些动画的实际观感**（此前插值段是错的）。属于实机确认项，见 §六。

### 3. 开发残留清出运行时资源 — 构建产物洁净

- 上一版 `build/libs/qys-bosses.jar` 里**确实带着 8 个开发备份**（61,724 B）——直接开包证实。
  根因：`.gitignore` 的 `*.bak` 只挡 git，挡不住 Gradle `processResources`。
- 处理：16 个备份（566,444 B）搬到 `_dev_backup/resources-2026-09-16/`（**只搬运不删除**）。
- `build.gradle` 加 `processResources { exclude ... }`（按文件名后缀，不动目录结构）。
- `.gitignore` 补 `_dev_backup/`、`*.reorder.bak`、`*.keyorder.bak`、`*.tilted-bak`。
- **结果**：新 jar 643 条目，开发残留 **0**；`shadow_sword_e.png` / `black_cleaver_blood_emissive.png`
  等正式发光资源**全部在包内**（未误伤）。

### 4. 检查工具修复（原来 3 个工具是坏的）

| 工具 | 原缺陷 | 修复 |
|---|---|---|
| `validate.cjs` | ① 无参数直接崩（`readFileSync(undefined)`）；② 用 `walk(b.children)` 递归遍历，与工程自己的约定（**扁平列表 + parent**）矛盾，且**完全没查** parent 是否存在/重复名/循环父级/父级优先 | 加 `--all` 批量；改扁平列表 + 显式父级图；新增重复名/循环父级/父级优先/inflate 检查；统一 `[error]/[warning]/[review]` 分类；负 `uv_size` 判为合法镜像 |
| `validate_anim.cjs` | ① 无参数直接崩；② **不支持** `"rotation": [0,0,0]` 这种"整段静态取值"轨道；③ 把 `hold_on_last_frame` 也算进循环闭合；④ 关键帧乱序只报 warning；⑤ `scale` 为 0 误报 error | 加 `--all`；支持静态取值轨道；修 `hold_on_last_frame`；**乱序升级为 error**（附字节码依据与"当前/应为"顺序）；`scale=0` 改 review（本工程用于隐藏部件）；非正/负缩放分开判；新增时间键重复检查；geo 里从未被动画触碰的骨骼列 review |
| `audit.cjs` | ① 无参数崩（需给工程根目录，但报错信息没提示）；② `--all` 批量缺失；③ 把 `lang/*.json`、`sounds.json`、全部 `_e`/`_glowmask` 发光层、`mob_effect` 图标、代码拼接路径的球体贴图都误报成"无引用资源"（64 个）；④ 从字符串拼接里抽出假语言键（`enchantment.level.`、`potion.withAmplifier`），后者其实是**原版键** | 加 `--json` 与参数校验；把"按命名约定被引用"单列 `[12]`（不再混入无引用）；开发残留单列 `[11]`；**原版键从 Gradle 缓存的 client jar 读 `assets/minecraft/lang/en_us.json` 对照**，只有既不在 mod lang 也不在原版 lang 的才报缺失；假键片段单列 `[13]` 供人工确认 |

**结果**：`validate --all` → error 0 / warning 0 / review 0；
`validate_anim --all` → error 0；`audit` → 缺键 0 / 冗余键 0 / data 问题 0 / 非法模型 0 / 缺战利品表 0 / entity 贴图误用 0。

### 5. 新增工具（6 个）

`reorder_bones.cjs`（geo 骨排序）、`geo_semantic_diff.cjs`（语义差异）、
`anim_key_order.cjs`（键序检查/整理）、`anim_key_order_scan.cjs`（只读原始文本扫描）、
`anim_key_order_verify.cjs`（独立验证）、`dev_resource_cleanup.cjs`（开发残留搬运）。

统一约定：**默认只读**，必须显式 `--write` / `--move` 才动文件，且先留备份、写后校验。

---

## 二、文件迁移映射（旧路径 → 新路径）

**没有任何源码/资源文件被移动或改名**（包结构、注册 ID、资源路径、骨骼名、动画名全部保持不变）。
唯一的位置变化是"开发残留搬出运行时资源目录"：

| 旧路径（相对工程根） | 新路径 |
|---|---|
| `src/main/resources/assets/ftboss/models/item/shadow_sword.json.before_grip_fix.bak` | `_dev_backup/resources-2026-09-16/assets/ftboss/models/item/shadow_sword.json.before_grip_fix.bak` |
| `src/main/resources/assets/ftboss/models/item/shadow_sword.json.grip.bak` | `_dev_backup/resources-2026-09-16/assets/ftboss/models/item/shadow_sword.json.grip.bak` |
| `src/main/resources/assets/ftboss/models/item/shadow_sword.json.tune.bak` | `_dev_backup/resources-2026-09-16/assets/ftboss/models/item/shadow_sword.json.tune.bak` |
| `src/main/resources/assets/ftboss/models/item/turkey_legs.json.tilted-bak` | `_dev_backup/resources-2026-09-16/assets/ftboss/models/item/turkey_legs.json.tilted-bak` |
| `src/main/resources/assets/ftboss/textures/item/shadow_sword.png.dedens.bak` | `_dev_backup/resources-2026-09-16/assets/ftboss/textures/item/shadow_sword.png.dedens.bak` |
| `src/main/resources/assets/ftboss/textures/item/shadow_sword.png.lumfix.bak` | `_dev_backup/resources-2026-09-16/assets/ftboss/textures/item/shadow_sword.png.lumfix.bak` |
| `src/main/resources/assets/ftboss/textures/item/shadow_sword_ready.png.dedens.bak` | `_dev_backup/resources-2026-09-16/assets/ftboss/textures/item/shadow_sword_ready.png.dedens.bak` |
| `src/main/resources/assets/ftboss/textures/item/shadow_sword_ready.png.lumfix.bak` | `_dev_backup/resources-2026-09-16/assets/ftboss/textures/item/shadow_sword_ready.png.lumfix.bak` |
| `…/animations/{apple_boss,benger,black_cleaver,compost_superman,shadow_sword}.animation.json.keyorder.bak`（5 个） | `_dev_backup/resources-2026-09-16/assets/ftboss/animations/…` |
| `…/geo/{compost_superman,compost_superman_p2,shadow_sword}.geo.json.reorder.bak`（3 个） | `_dev_backup/resources-2026-09-16/assets/ftboss/geo/…` |

> ⚠ **注意 `_dev_backup/` 里那 5 个 `*.keyorder.bak` 的内容**：我在收尾时又跑了一次 `--write --all`
> （当时工具已被修好、文件也已正确，属幂等的空操作），但该工具"已存在 `.bak` 就不覆盖"，
> 所以这 5 个 `.bak` 实际是**第二次写入时**的副本（内容=已排序版），**不是**改动前的原件。
> 改动前的原件在下面这张表里，是**唯一**可信的回退源。

**唯一可信的回退源**（`_backup/` 下，均非 `src` 内容）：

| 内容 | 路径 | 已验证 |
|---|---|---|
| 键序整理前 21 个动画原件（含全部 109 条乱序轨道） | `_backup/anim-keyorder-before-20260916/*.animation.json`（21 文件 / 1,690,923 B） | ✓ 用只读扫描器复测：仍能检出 109 条未升序，证明是真正的改动前状态 |
| 骨排序前 3 个 geo 原件 | `_dev_backup/resources-2026-09-16/assets/ftboss/geo/*.reorder.bak`（121,580 / 43,337 / 43,337 B） | ✓ 与改动前快照 SHA256 逐字节一致 |
| 本轮新增工具产生的 8 个备份的工程内副本 | `_backup/cleanup-20260916/` | — |

回退方式：
- **键序回退** → 用 `_backup/anim-keyorder-before-20260916/*.animation.json` 覆盖回 `animations/`
  （**不要**用 `_dev_backup` 里那 5 个 `.keyorder.bak`）
- 骨排序回退 → 用 `_dev_backup/resources-2026-09-16/assets/ftboss/geo/*.reorder.bak` 覆盖回 `geo/`
- 开发残留回退 → 从 `_dev_backup/resources-2026-09-16/` 按原相对路径搬回
- 一键检查当前是否干净：`node tools/model3d/dev_resource_cleanup.cjs --check`
  与 `node tools/model3d/anim_key_order_scan.cjs src/main/resources/assets/ftboss/animations`

---

## 三、代码优化

### 3.1 已做的（有明确收益、零行为风险）

| 项 | 说明 |
|---|---|
| `processResources` 排除规则 | 少打 8 个备份文件（61,724 B）进 jar，且以后不会再混入 |
| 检查工具 3 个修复 + 6 个新增 | 见 §一.4/§一.5；把"静态扫描看不到"与"真的没引用"分开，消除 40+ 条假问题 |
| `tools/model3d/README.md` | 表格里 7 个脚本（`check_clip` / `fit_check` / `preview` / `inspect` / `sheet` / `zoom_tex` / `apply_cleanup`）实际已归档到 `tools/_archive/model3d/`，照旧路径调用会失败 —— 已全部标注并给出正确路径；补上本轮新增的 6 个工具与"为什么键序是 error"的字节码依据 |

### 3.2 已定位但**本轮未改**（需你确认或需实机验证；详见底下"待确认问题"）

这些都属于"改了会改变玩法"，与规格"不在本轮顺带修改数值/外观"冲突，因此**只记录不改**：

1. **`BlackCleaverItem.triggerCleaverAnim()` 零调用者** ⇒ `black_cleaver` 的 `charge/slam/recover/ready`
   四段动画**永远播不到**，只有 `idle` 会播。血怒的"斩击"音画链路（`cleaver_slam_*.png` 等）也没接线。
2. **`ShadowSwordItem` 把玩家/物品栈状态放在 static**（`echoPulse`/`echoReady`/`WAS_READY`），
   被共享的控制器谓词读取 ⇒ 理论上存在跨栈/跨玩家的姿态泄漏，且 `echoReady` 跨世界不清理。
3. **`BloodRageFakeEffect`**：`onRemoved` 处理器**从未注册**（类没有 `@Mod.EventBusSubscriber`，也没人注册）；
   且 10 秒衰减在 15 秒过期前最多触发一次，触发时也**无法真正降低层数**（原版 `MobEffectInstance.update`
   只提升 amplifier，较弱的实例被塞进 `hiddenEffect`）。
4. **`BlackCleaverAndShadow:55` 的 `if (false && ...)`** —— 一段被永久关闭的血怒叠层/破甲/真伤逻辑。
5. **渲染热路径分配**：`BlackCleaverGeoRenderer` 每帧 `String.format` 与 `new ResourceLocation`（模型/贴图/动画资源
   可以提到常量）、`BloodGlowLayer` 每帧 `RenderType.eyes(...)`、`WeaponHitEffects` 每次命中 `String.format`。
6. **若干集合无上限**：`ShadowSwordItem.WAS_READY`、`WeaponHitEffects.HIT_DIAG`、
   `BossHandler.armamentariumBlockLogged`、`EasterBossEntity.ZICAI_BLOCKED`。
   `BlackCleaverGeoRenderer` 的两个诊断集合**是有界的**（16 / 4）。
7. **`audit.cjs` 报的 5 个无用 import 与 8 个疑似只用一次的 private 成员** —— 属低风险清理，但需要逐个确认
   （可能是反射/序列化调用），留待你确认后再动。
8. **`docs/` 与 `tools/` 里的历史一次性脚本**（大量 `patch_*` / `fix_*` / `recover_*`）已按工程惯例在
   `tools/_archive/`；`tools/` 顶层仍有约 40 个一次性 `.cjs` 与 `_bgm*.txt` 等大体积日志，
   迁移会影响既有 `AGENTS.md` 记录，**未动**。

---

## 四、验证结果

| 验证项 | 命令 | 结果 |
|---|---|---|
| Java 编译 + 资源处理 + 打包 | `gradle clean build --console=plain --no-daemon` | **BUILD SUCCESSFUL**（exit 0）；100 条 warning 全部是 `ResourceLocation(String,String)` 的 `[removal]` 弃用提示，无 error |
| 产物洁净 | 开包枚举 `build/libs/qys-bosses.jar` | 643 条目，**开发残留 0**；两把武器的 geo/动画/发光贴图均在包内 |
| geo 结构 | `validate.cjs --all` | 23 个文件 → **error 0 / warning 0 / review 0** |
| 动画↔geo 一致性 | `validate_anim.cjs --all` | 23 组 → **error 0**；3 组"无同名动画"已确认为**共用**（big_rat/small_rat 用 `rat.animation.json`，compost_superman_p2 与 p1 同源），重合度均 100% |
| 关键帧键序 | `anim_key_order_scan.cjs`（只读原始文本） | 21 文件 / 1443 轨道块，**未升序 0**；对改动前原件复测仍能检出 **109** 条（证明检查有效） |
| 键序整理无损性 | `anim_key_order_verify.cjs`（独立验证器） | **21/21 PASS**：键序已升序、键集合一致、取值文本逐字符一致、结构未变 |
| 骨排序语义 | `geo_semantic_diff.cjs` ×3 | **语义零差异** |
| 黑切刃翼三方对齐 | `cleaver_bones_check.cjs` | ✓ 四方刃翼在 Java 常量 / geo / blood_pose 完全一致 |
| 暗影剑骨架兼容 | `check_sword_compat.cjs` | ✓ 通过（19→22 骨骼、62→63 cube，新增 3 个 glow 骨骼） |
| 全工程体检 | `audit.cjs .` | 无引用资源 21（全部为保留待确认或按约定引用）、缺键 0 / 冗余键 0 / data 问题 0 / 非法模型 0 |
| 开发残留 | `dev_resource_cleanup.cjs --check` | 0 个 |
| 工具幂等性 | `anim_key_order.cjs --write --all` 二次执行 | 0 条需整理、不产生新备份（幂等） |

## 五、未验证项（**不要当成通过**）

1. **游戏内运行验证：全部未做。** 本轮未启动游戏、未做任何实机测试。
   本项目自己的规矩是"部署前检查游戏是否在跑"—— 检查时**游戏正在运行**（进程 PID 10300，Forge client），
   因此**没有**替换已部署的 `mods/qys-bosses.jar`，也没跑 `tools/deploy_jar.ps1`。
2. **关键帧键序修复的视觉结果未实测。** 我只证明了"键序改成升序 + 取值一字未改 + 结构未变"，
   **没有**在游戏里确认观感变好还是变差。受影响的是 apple_boss（88 条）、compost_superman（10）、
   benger（8）、shadow_sword（2）、black_cleaver（1）。
3. **骨排序后的实机渲染未验证**（理论依据是 GeckoLib 按名字查骨骼 + 父子链渲染，但未实机核对）。
4. **模型外观评价未做**：静态几何检查**不能**宣称动画全过程无穿模。`check_clip.cjs` / `fit_check.cjs`
   已归档在 `tools/_archive/model3d/`，本轮**未**对全部 23 个模型逐个跑穿模检查 ⇒ 该口径为"未检查"。
5. **握持参考图不是完整游戏持物矩阵**：`hand_check.cjs` 输出只能作**比例参考**，
   不能据此自动改正式 `display`（工具自己的说明也这么写）。
6. **Blockbench 源对应关系**：全工程标 `未确认`（证据见 `docs/model-assets.md` §4）。
7. **`compost_superman_p2.geo.json` 等 21 个无引用资源**只做了"识别 + 保留"，未删除、未确认归属。

## 六、尚需人工查看的外观问题

1. **先看这 5 个动画**（改动最直接）：`apple_boss` 的 `idle`（躯干/披风/腰带/帽子的抖动时序）、
   `death`（蠕虫/眼镜/领扣）；`benger` 的 `walk`/`roar`（脊柱/腰/尾巴）；`shadow_sword` 的 `idle`
   （爪尖 `claw_left_tip`/`claw_right_tip` 的收张节奏）；`black_cleaver` 的 `idle`
   （`ember_core` 余烬核心的呼吸缩放）。进游戏后按 **F3+T** 重载资源即可看到，不必重启。
2. 暗影剑刀身 10 条细分之间**刻意 0.05 重叠**（内部顶/底面同平面重合，不可见），
   远近与斜视下是否闪烁 —— 只有实机能确认。
3. `eye×blade` 的残余穿模量是否可见。
4. 暗影剑 `ItemDisplayContext` 分场景**尚未做**（物品栏目前在播 idle），
   需要先核实 GeckoLib `GeoItem#isPerspectiveAware()` 的确切语义。
5. 石像贴图/`statue_*` 物品图标（已无引用）是否还要保留为素材。

---

## 七、本轮的操作失误与纠正（如实记录）

1. **`anim_key_order.cjs --write --all` 一度误改正式资源**：当时该工具我还没给你确认，属越界。
   发现后立即用备份回退，并确认回到基线；随后经你明确选择后才重新应用。
2. **我自己的第一版验证脚本把已修好的文件写坏过一次**：它用 `JSON.parse → 排序 → JSON.stringify`
   往返，而 JS 对象无法表示 `["0","1.4","2.6","4"]` 这种键序，于是把 88 条已修好的轨道写回了乱序。
   该脚本已删除；现在的判据一律**直接读原始文本字节**，并新增了独立的只读扫描器
   `anim_key_order_scan.cjs` 交叉验证。
3. **新增的 `*.bak` / `*.reorder.bak` / `*.keyorder.bak` 一度落进 `src/main/resources`**（会让下次构建多打 208 KB）。
   已加 Gradle 排除规则 + `.gitignore` 兜底，并把已有备份搬到 `_dev_backup/`；新 jar 已验证 0 残留。
4. **`_dev_backup/` 里的 5 个 `*.keyorder.bak` 已被第二次写入覆盖**，内容是"已排序版"而非改动前原件。
   （工具按"已存在就不覆盖"保护备份，我收尾时又跑了一次幂等的 `--write --all`，于是旧备份没被刷新。）
   **不影响回退能力** —— 改动前的 21 个原件完整保存在 `_backup/anim-keyorder-before-20260916/`
   且已用只读扫描器复测（仍检出 109 条乱序）。但这是工具设计上的一个坑：
   "备份只写一次"在**重复执行**时会让人误以为备份是当前改动前的状态。已在 `docs/model-assets.md`
   与本文件明确标注哪一份才是可信回退源。
