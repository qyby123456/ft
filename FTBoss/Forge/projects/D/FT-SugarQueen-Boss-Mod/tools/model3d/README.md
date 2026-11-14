# tools/model3d — 模型/动画工具链（Node，无外部依赖）

一份**规格脚本**同时产出模型与贴图，再用校验器验一遍，最后用渲染器"看"结果。
原则：**不要手写数字去对齐**，也不要在没有预览的情况下声称模型没问题。

## 目录约定（2026-09-12 整理）
- **本目录只留"以后还会用"的东西**：生成器 `gen_*.cjs`、校验器、渲染/预览器、共享库 `png.cjs`。
- **几何变换的唯一真源是 `cube_space.cjs`**（`cubeCorners()` / `getBoneChain()` /
  `transformByBoneChain()` / 隐藏骨骼 / 物品 `display`）。它由 `hand_check.cjs` 与
  `check_shadow_struct.cjs` 共用；**不要再往别的脚本里抄第三份变换函数**
  （历史上 `check_shadow_struct.cjs` / `sword_preview.cjs` / `hand_check.cjs` 各有一份，
  且遍历顺序不一致，见下面"已踩过的坑"）。
- **历史一次性脚本**（`fix_*` / `wire_*` / `patch_*` / `*_debug` / `check_*` / 旧版 `preview` / 旧输出目录…）
  已移到 `../_archive/model3d/`；归档目录里放了一份 `png.cjs`，所以那边的脚本仍可独立运行。
- 脚本依赖：活跃目录里 `gen_butcher.cjs` 依赖 `butcher_spec.cjs`（屠夫规格唯一真源，必须留在本目录）。
- 输出目录（`out_*`）统一定到 `../_archive/out/`；生成器默认仍写在当前工作目录，跑之前先 `cd` 到想要的位置。

## 生成器
| 脚本 | 产出 |
|---|---|
| `summons_icons_v1/generate.cjs [资源根目录] [--group=summons\|discs\|effects]` | 20 件召唤物 3D/GUI、15 张唱片、12 个状态图标；仅此工具依赖 pngjs，见 `docs/summons-icons-v1.md` |
| `gen_camellia.cjs <outDir>` | 茶芽小妖 geo + 贴图（+ 调试贴图） |
| `gen_benger.cjs <outDir>` | 黑白边牧 Boss geo、逐面贴图、9 段原动画及 Blockbench 工程；见 `docs/BENGER_MODEL.md` |
| `gen_barrel.cjs <outDir>` | 酒桶王 geo + 贴图 |
| `gen_butcher.cjs <outDir>` | 屠夫 geo + 贴图 |
| `gen_ginger.cjs <outDir>` | 姜饼王 geo + 贴图 |
| `gen_ginger_minion.cjs <outDir>` | 小姜饼人 geo + 贴图 + **5 段动画**(idle/walk/attack/hurt/death, 一份规格全出) |
| `gen_statues.cjs <outDir>` | 五座白膜石像的方块模型 + 物品图标（**几何**生成器；重跑有冲掉手工几何的风险，见下） |
| `gen_statue_skins.cjs <outDir> [assetsDir]` | （v2，已被 v3 取代）石像的**手绘花纹皮肤**：每 Boss 一张 64×64 花纹贴图 + 彩色图标 |
| `gen_statue_real.cjs <outDir> [assetsDir] [geoDir]` | 旧 v3 转换器；当前 OBJ 石像会拒绝覆盖，避免丢失旋转与白石细节 |
| `gen_boss_statues.cjs <outputAssets> [id...]` | 当前九座旧 Boss 石像的唯一生成入口：实际模型、完整骨骼与立方体旋转、分层底座、徽记、白石、双半格 OBJ、自动居中缩放的物品模型 |
| `preview_boss_statue.cjs <out.png> [assets] [id]` | 从生成的实际 OBJ 渲染原色斜视、正视及白石预览，无额外依赖 |
| `preview_boss_statues.cjs <outDir> [assets]` | 九座原色/白石对照总览；通过 `FT_NODE_MODULES` 指定含 `@napi-rs/canvas` 的依赖目录 |
| `gen_weapons.cjs <outDir>` | 五把 Boss 主题武器物品图标 |
| `gen_cast_anims.cjs <outDir>` | 酒桶王/姜饼王的铁魔法兼容动画 |

## 校验器（改完模型/动画**必须**跑）
| 脚本 | 检查 |
|---|---|
| `validate.cjs <geo> [必需骨骼]` / `validate.cjs --all` | 骨骼/父子引用/重复名/**循环父级**/**父级优先**/尺寸/cube rotation/UV（box-UV 必须整数、逐面 UV 允许小数、负 uv_size 合法=镜像）/贴图越界 |
| `validate_anim.cjs <geo> <anim> [java源码目录]` / `--all` | 动画引用的骨骼是否存在、**关键帧时间键必须数值升序**（见下）、时间键重复、关键帧越界、数值是否合法、循环是否闭合、**动画名在代码里有没有被引用（抓"死动画"）**；geo 里从未被动画触碰的骨骼也列出 |
| `anim_key_order.cjs --check\|--write <anim...>\|--all` | **关键帧时间键顺序**检查/整理。默认只读；`--write` 才写，且写前留 `.keyorder.bak`、写后做无损校验。 |
| `anim_key_order_scan.cjs <dir\|file...>` | 只读**原始文本**扫描键序（不经过 `JSON.parse`，因为 JS 对象无法表示 `["0","1.4","2.6","4"]` 这种顺序） |
| `anim_key_order_verify.cjs <beforeDir> <afterDir> [文件名]` | 整理前后的**独立验证**：键序已升序 / 键集合一致 / 每个关键帧取值文本逐字符一致 / 结构未变 |
| `reorder_bones.cjs --check\|--write <geo...>\|--all` | geo `bones` 排成**稳定父级优先**顺序（根→父→子，同级保持原相对顺序）。默认只读；`--write` 才写并留 `.reorder.bak`。cube 顺序一律不动 |
| `geo_semantic_diff.cjs [--anim] <before> <after>` | **语义差异**（不是文件哈希）：骨骼集合/字段/cube 顺序与内容/UV/包围盒/父子层级，逐维度比对并给结论；`--anim` 比动画 |
| `dev_resource_cleanup.cjs --check\|--move` | 把 `src/main/resources` 里的开发备份（`.bak`/`.tilted-bak`/`~`…）搬到 `_dev_backup/resources-<日期>/`。**只搬运不删除** |
| `check_clip.cjs <geo> [阈值]` | 骨骼 AABB 两两求交，报告穿模/重叠体积 → **已归档到 `tools/_archive/model3d/`** |
| `fit_check.cjs [姿势名] [导出目录]` | **按动画姿势**的穿模检查（屠夫用）：复刻 `setupAnim` 的旋转/位移写进矩阵链，逐 **cube** 求交（比 check_clip 的逐骨骼精确），并可选导出各姿势的 geo 供渲染 → **已归档** |
| `inspect.cjs <geo> [箱宽] [箱高]` | 骨骼树 + 每个 cube 的 origin/size/uv + 包围盒 + **模型/碰撞箱比值** → **已归档** |
| `audit.cjs <工程根目录>` | 全工程体检：无引用资源、缺失/冗余语言键（**会区分原版键**）、data 悬空引用、无用 import、疑似死成员、**开发残留是否混在运行时资源里**、按命名约定被引用的资源单列 |
| `apply_cleanup.cjs <工程根目录>` | 一次性补丁：精确删除无用 import、删除确认无引用的死贴图（跑完可留档） → **已归档** |

> ⚠ 上表里标"已归档"的脚本**不在本目录**，它们被移到了 `tools/_archive/model3d/`（那边另有一份 `png.cjs`，可独立运行）。
> 需要用就写全 `tools/_archive/model3d/xxx.cjs`，别照旧路径调用。
> 另外 `gen_statues.cjs` / `preview.cjs` / `sheet.cjs` / `zoom_tex.cjs` 同样在 `_archive/` 里。

### ★★ 为什么"关键帧时间键必须升序"（2026-09-16 反汇编 GeckoLib 4.8.4 实证）

`AnimationController.getCurrentKeyFrameLocation(List, tick)` 的字节码是：

```
double total = 0.0;
for (Keyframe kf : list) {            // 严格按 List 顺序遍历
    total += kf.length();             // 累加的是 keyframe 的**间隔(duration)**
    if (total > tick) return new KeyframeLocation(kf, tick - (total - kf.length()));
}
return new KeyframeLocation(list.get(list.size() - 1), tick);   // 兜底取**最后一个元素**
```

⇒ 段定位完全依赖"总时长前缀和"，**只有键按数值时间升序时前缀和才等于真正的时间轴**。
键序错了就落到错误的段，插值起点/斜率全错，连兜底都取错元素。

而整条加载链**没有任何排序**（对 `BakedAnimationsAdapter` / `AnimationController` / `AnimationProcessor`
反汇编全文搜 `sort` 均为空；`BakedAnimationsAdapter.getKeyframes` 用 `ObjectArrayList` 按 `entrySet()`
顺序建表 = JSON 书写顺序；`KeyframeStack.getLastKeyframeTime()` 直接取末尾元素）。
**⇒ JSON 里关键帧键的书写顺序会直接改变动画播放结果，不是风格问题。**

**本工程 2026-09-16 已修正 109 条**：apple_boss 88、compost_superman 10、benger 8、
shadow_sword 2（`idle` 的 `claw_left_tip`/`claw_right_tip`：`[0,4,1.4,2.6]` → `[0,1.4,2.6,4]`）、
black_cleaver 1（`idle` 的 `ember_core.scale`：`[0,3,1.5]` → `[0,1.5,3]`）。
改前原件留在 `_backup/anim-keyorder-before-20260916/`。

> ⚠ **改这类文件不能用 `JSON.parse` → 改 → `JSON.stringify` 往返**：JS 对象把类数组下标的字符串键
> （`"0"`/`"4"`）提前并按数值升序，而 `"1.4"`/`"2.6"` 属普通字符串键按插入顺序 —— 目标顺序
> `["0","1.4","2.6","4"]` 在 JS 对象里**根本表示不出来**，往返必然写出错的键序。
> 本工程 1581 条轨道里有 **461 条**正是这种"整数键与小数键混写"。
> 所以 `anim_key_order.cjs` 自带一个按**显式键序**输出的序列化器，校验也直接扫**原始文本**。
> **检查判据同样必须读原始文本**：`Object.keys(JSON.parse(raw))` 会把已经正确的文件误判成"需要整理"。

## 渲染器（把数字变成能看的图）
| 脚本 | 产出 |
|---|---|
| `preview.cjs <geo> <out.png> [箱宽] [箱高]` | 四视图线框 + 16 单位网格 + 碰撞箱线框，并打印包围盒与比值 |
| `render_tex.cjs <geo> <tex.png> <out.png> [箱宽] [箱高]` | 按 UV 采样的**带贴图**四视图（box-UV 与逐面 UV 都支持） |
| `zoom_tex.cjs <in.png> <out.png> [倍数]` | 贴图最近邻放大（核对 box-UV 里五官实际落在哪一面的像素） |
| `sheet.cjs <out.png> <每格px> <in...>` | 多张贴图拼成一版（一次看清整套石像/武器的外观） |
| `render_model_tex.cjs <lower.json> [upper.json] <tex.png> <out.png> [每单位px]` | **Java 方块模型**的正面视图 + 真实贴图采样（石像这类 elements 模型"放到世界里长什么样"） |
| `preview_item.cjs <model.json> <tex.png> <out.png> [每单位px]` | **物品模型**单面正视图（遵守 `texture_size`；`FT_FACE` 选面、`FT_CX` 设水平中心） |
| `preview_3d.cjs <model.json> <tex.png> <out.png> [px] [yaw] [pitch]` | **物品模型正交 3D 预览**（逐面仿射贴图 + 画家算法，远→近）。排查"贴图错位 / 手持朝向不对"用这个 —— 石像 v55 就是靠它确认几何和 UV 没问题、真凶是 `item/handheld` 的 -90° 朝向 |
| `bake_pose.cjs <in.geo> <out.geo> rotX=<deg> posY=<u> [posZ=<u>]` | 把"倒地/躺平"这类**绕 X 轴 90 的整数倍**姿势烘焙成静态 geo（轴对齐 cube 转完仍轴对齐），再用 `render_tex.cjs` 出图；并**直接报"有没有陷进地面以下"** |
| `hand_check.cjs [slot] [zoom] [--hand] [--reference-only] [--sword-bias n]` | **第三人称比例参考图**：经典比例玩家参照体 + 手持影刃 + 手心红点 + 地平面网格，共享 Z-buffer、cutout 贴图采样、超采样。同时打印 `display` 把剑放到哪里、与手心的偏差（方块/单位）。**只是比例参考**，没复现手臂姿态/左右手镜像/GeckoLib 矩阵，不能据此自动改正式 `display` |
| `shadow_sword_pos_audit.cjs [--json]` | 暗影剑**位置专项审计**：① 原点是否落在握柄区间 ② 动画有没有碰握持链 ③ display 在两种轴约定下把剑放到哪 ④ 与旧静态版(v18)同一物理点的刚性对照。输出可直接贴给人看 |
| `cleaver_check.cjs [--geo f] [--anim f] [--blade name] [--json]` | black_cleaver **结构体检 + 刃翼拆分守门**：骨骼名/父级链/cube 字段/动画引用自洽；对 `--blade` 下的子骨骼**逐对算重叠体积与逐轴错开量**，守住"相邻段重叠 ≥2 单位、每轴错开 ≥0.2"（低了会断裂或 z-fighting）。**只读**，拆完刃翼跑它 |
| `wing_joint_check.cjs [--geo f] [--angles -12,-8,4,8,12] [--json]` | **刃翼"展开会不会撕裂轮廓"的量化检查**：①侧视投影上逐列求"静态几何 ∪ 旋转后的刃翼"覆盖的 y 区间，**上下都有几何、中间却空着**的区间就是能透背景的缝，报出最大缝高与位置；②"补缝背板在合拢态是否完全被挡住"（采样点外露 = 会露馅）；③**展开方向**：上翼尖端必须升高、下翼必须降低（= 向外张开，而不是往刃身里转）。**只读**，改刃翼/枢轴/补缝件后跑它。⚠ 别喂**已经烘焙过姿态**的 geo 给它（它自己会再转一次，结果无意义） |
| `cleaver_bones_check.cjs [--geo f] [--anim f] [--java f] [--json]` | **「Java 刃翼骨骼名 ↔ geo ↔ blood_pose」三方对齐**。血怒展开是 Java 按骨骼名直接 set 旋转（不走控制器），名字打错一个字母的后果**完全静默**：那一片刃翼永不展开，而 `validate.cjs` / `validate_anim.cjs` 全绿。本脚本查：Java 常量在 geo 里存在吗、blood_pose 引用的骨骼存在吗、两边集合是否**不多不少完全一致** |
| `wing_pose_bake.cjs <in.geo> <out.geo> [upper=deg] [lower=deg]` | 把刃翼的展开角**烘焙成静态 geo** 供离线出图（`render_tex.cjs` 支持 cube `rotation`）。骨骼旋转是绕 pivot、cube rotation 是绕自身中心，轴对齐盒子可以精确等价换算（center 搬运 + `rotation=[0,0,deg]`）。用法见 `art/bc_open_8deg.png`（±8° 展开态） |
| `bg_key.cjs <in.png> <out.png> [fromHex] [toHex]` | 把预览图**和模型深色材料撞色的背景**换成洋红（默认取左上角像素为背景色）。本工程武器主体是暗铁(~42,37,43)，"轮廓破了个洞"和"实心暗铁"在图上几乎同色 —— 换背景后洞就是一眼可见的洋红块。做"有没有露背景"的判读必用 |
| `sword_display_tune.cjs show\|set\|apply\|reset` | **改 display 不重建**：物品模型 JSON 支持游戏内 **F3+T 重载资源**，所以试握持参数走这条路（40 秒构建 + 重启 → 一次 F3+T）。`set thirdperson_righthand rot=0,-90,45 trans=-1,3.5,0.2`；`apply x-flip\|diag45\|diag55\|flat20\|in-up` 是给"往外飘 + 角度不对"准备的候选；`reset` 回备份 |
| `boss_arm_axis.cjs [--png]` | **离线算"折耳根举剑"的手臂端点**：`EasterBossModel extends PlayerModel`，骨骼链就是原版那套，所以手掌位置可以**算死** —— 专治"往内/往外/往前"这种靠截图判不准的符号问题（2026-09-15 为它绕了三轮）。打印横向/前后偏移表，`--png` 出正视+俯视对比图 |
| `check_pixel_style.cjs <geo> <texture> [--target 2] [--screen-ppu n] [--out f]` | **像素风格一致性检查**：逐面算 **px/模型单位**（同样 16 单位的刃，铺 16px = 1 px/单位≈原版尺度，铺 128px = 8 px/单位就明显偏精细），提示密度偏高 / 横纵拉伸 >1.5 倍 / UV 未对齐纹素 / 高密度且相邻色差频繁（碎杂）/ 单纹素不足一个屏幕像素（易闪烁）。**只提示，不缩图不改 UV**。⚠ 整模汇总会被 `blade_glow` 那种 size=0.05 的**光效种子块**带偏（24×24 的 UV = 480 px/单位），判断请看 `body_excluding_glow` 与 `by_bone` |
| `shadow_sword_tex_fix.cjs [--dry\|--restore] [--gamma n] [--dark n] [--bright n]` | 暗影剑贴图**明度**优化（只重排明度，alpha 与色相一位不动）。治"游戏里一团黑看不清"：优化前亮度 <40 占 66%、亮部仅 0.7%、动态范围 55 |
| `shadow_sword_de_densify.cjs [--levels n] [--gate n] [--dry\|--restore]` | 把**高密度件的有效细节**降下来（颜色台阶量化 + 去孤立噪点）：**不碰几何/UV/图集**，alpha 完全不动，默认只处理 >`--gate`(20 px/单位) 的骨骼区域。注意它**不会改变 px/单位 那个数**（那是 UV 决定的），改的是"相邻色差频繁"这一项 |

`render_tex.cjs` 的取景可用环境变量放大（**小模型必须放大才看得清五官**）：
`FT_PANEL`（面板边长，默认 250）、`FT_SCALE`（每单位像素，默认 4.2）、`FT_OY`（脚底在面板内的 y，默认 210）。
例：小姜饼人 `$env:FT_PANEL=620; $env:FT_SCALE=26; $env:FT_OY=520`。

## 已踩过的坑（别再踩）
- ★★ **Bedrock 的 box-UV 矩形不能原样抄进 Java 方块模型**（石像 v3 实测：贴图"进去了但整个错乱"）。
  Java 每个面的顶点 0..3 依次拿贴图角 **TL, BL, BR, TR**（`FaceInfo` + `BlockFaceUV.getU/getV`），
  而 Bedrock（GeckoLib `VertexSet.quadX` + `GeoQuad.build`）的 4 个顶点依次拿 **TL, TR, BR, BL**。
  两张表对齐后规则极简：**侧面发 `[u2,v1,u1,v2]`（uv 都交换）、顶/底面发 `[u1,v2,u2,v1]`（只换 v）**；
  Java 允许 `u1>u2` 的镜像写法。实现在 `gen_statue_real.cjs` 的 `orientRect()`，两张顶点表抄在该文件注释里。
  **方法论**：跨引擎搬 UV/顶点，别"看着公式一样就照抄"，直接从字节码里挖两边的顶点表逐面对齐（javap 就行）。
- **生成器与"已部署的成品"会漂移 —— 这是本项目最危险的一类坑**：
  屠夫的 `ButcherBossModel.java` 被手改修过穿模（`body.y`/`head.y`/手臂枢轴/肩宽/腰带），但 `gen_butcher.cjs` 没同步，
  于是"重新生成一次"会把修好的 bug 原样带回来（实测：生成器里还写着 `body.y = 10.0F`，正是当初躯干下沉 7 单位插进腿里的那行）。
  **规矩**：任何手改生成物之后，立刻把改动回写到生成器；或者干脆只改生成器再重新生成。
  开工前先跑一次生成器并 `Compare-Object` 与仓库里的成品对比，差异应该**只有你本次想改的东西**。
  石像就是活例子：`gen_statues.cjs` 重跑会把手工修过的 `statue_rat_swarm_upper`（1 cube 占位）换回 13 cube。
  所以**皮肤层单独拆成 `gen_statue_skins.cjs`，它读已部署模型当几何真源**，改外观时不要再动几何生成器。
- **屠夫规格的唯一真源是 `butcher_spec.cjs`**（gen 与 fit_check 共用），别在 gen_butcher.cjs 里另抄一份。
- **Bedrock 的骨骼 `pivot` 是绝对模型坐标**（骨骼绕它旋转），而 Java 的 `PartPose.offset` 是相对父骨骼的。
  转换时必须把父级偏移逐级累加，否则嵌套骨骼（头/手臂）旋转会绕错点。
- **box-UV 必须是整数**：浮点 UV 打包坐标会让 `Uint8Array` 静默丢弃写入 → 整片材质空白。
  另外**画五官的 cube 其 w/h/d 要取整数**，因为"北面"在贴图里的落点是 `(u+d, v+d)`, `d` 是小数就画偏。
- **两个不同 cube 的面落在同一平面会闪烁（共面 z-fighting）**：小姜饼人第一版就是
  "头骨正面 z=-3、领口正面也 z=-3" → 头与领口交界处一条 6x0.4 的闪烁带。
  改法是让其中一个**内缩 0.5**（领口 z 从 6 宽收到 5 宽）。同理: 躯干与袖子不要"正好贴住"，
  要么重叠进体内(肩关节, 会被外层面片挡住)、要么留 0.2 缝隙。
- **逐面 UV 允许小数**（Bedrock 合法），但 `uv_size` 为负时是镜像，渲染器要处理。
- **不要多个不同尺寸的 cube 共用一个材质**（会串脸）；材质按"需要的最大 UV 矩形"货架式打包。
- **Bedrock 的 bones 必须是扁平列表 + `parent` 字段**；嵌套 `children` 会让 GeckoLib 读不到 cube，整个模型不渲染。
- **GeckoLib 动画的 `position` 单位是 1/16 格（模型单位）**：`[0,2,0]` 只有 2 像素，别当成 2 格。
- **`hold_on_last_frame` 不是循环**：校验循环闭合时不要把它算进去。
- **动画定义了不等于生效**：必须确认实体控制器里真的按名调用了（`validate_anim.cjs` 的最后一列）。
- 大模型注意：碰撞箱由 `FTEntities.register(id, factory, 宽, 高)` 决定，模型尺寸要与之一致；
  同一个实体类型里"大小两种角色"要用 `getDimensions(Pose)` 分档（鼠群就是这么修的）。
- ★★ **骨骼链必须"先子后父"**：`getBoneChain()` 用 `unshift()` 建出的是 root → … → child，
  而点位于**子骨骼**局部空间，必须先被最近的骨骼变换。写成 `for (const bone of chain)`（root 先作用）
  会让子骨骼的偏移再被祖先变换一次 —— `check_shadow_struct.cjs` 曾这样算错护爪/晶核/瞳孔的位置。
  现在统一走 `cube_space.transformByBoneChain()`。
- ★★ **`uvq` 必须展开成 8 项角点查找表**：角点索引是 `4x+2y+z`（0..7），
  而贴图只有 4 个角。`hand_check.cjs` 里曾把 4 项的 `uvq` 直接按 0..7 索引，
  结果一半角点取到 `undefined`，剑贴图整片错乱。做法：
  `uv`/`uv_size` 建四角 → 按"该面固定轴"筛出 4 个角点 → 归到 TL/TR/BR/BL 四角（见 `buildUvTable()`）。
- ★★ **`Buffer.copy` 参数顺序是 `(target, targetStart, sourceStart, sourceEnd)`**：
  把"拼多联图"写成 `slice.copy(canvas, rowStart, srcStart, srcEnd)` 之类的顺序，
  会静默写出**全黑画面**（PNG 本身合法，只有肉眼看图才发现）。
- ★ **背面剔除要用视空间法线**：拿 geo 空间的 `normal[2]` 当判据，
  侧面与上下面的那个分量恒为 0，会被整片剔掉（表现为"人看不见、剑只剩几条"）。
  正确判据：投影后三角形的有向面积 = 视空间法线 z × scale²，两者同号即朝前。
  `hand_check.cjs` 默认**关闭**剔除 —— 右手 display 含一次镜像（`scale(-1,1,1)`），
  绕序整体反过来而法线表没跟着翻，剔除不可靠；共享 Z-buffer 已给出正确遮挡。
- **不要在模型坐标里偷偷做 Z 取反来"假装" Java 显示空间**：
  `(x, y, -z)` 等价于额外插入 180° 绕 Y 旋转，会把 `[0,-90,28]` 的刃向从
  `(0.469, 0.883, 0)` 翻成 `(-0.469, 0.883, 0)`。display 的旋转数值是在 geo 所属坐标系里
  标定过的，要换空间就在渲染器里整体换，别在取点函数里翻。
- **`display` 必须逐字段补默认值**：只写了 `rotation` 的物品模型，直接读 `disp.scale[0]`
  会 `TypeError`。另外 `display` 可能继承自父模型，**缺字段不等于游戏里就是默认值**。
- ★ **物品 `display` 的"另一个版本看起来差不多"不能当参照**：暗影剑从静态 v18（原点=**柄尾**）
  换成 GeckoLib 骨骼版（原点=**握柄中部**，比柄尾高 2.67 单位）时，两版 `trans.y` 只差 0.5，
  但原点约定差了 2.67 单位 —— 真正该比的是"**同一个物理点**（柄尾）落在哪"，
  拿 `trans` 数值互抄会凭空错 0.17 格。用 `shadow_sword_pos_audit.cjs` 的 §D 做这种对照。
- ★★ **不要把"离线算出来的握持高度"当成游戏里的握持高度**：物品 display 作用在**手部局部空间**，
  该空间的基（轴指向、Y 朝上还是朝下）由渲染管线决定，离线算式里没有它就一定差一个常数。
  判据只能是实机在同一姿态下画出"手部挂点 + 三轴 + 握柄两端"再对照。
  `hand_check.cjs` 因此把偏差**打印成数字**、把剑**显式平移**到手心，并在图上注明"红点必然重合"。
  实机标定用 `client/render/SwordAxisDebugLayer`：JVM 参数 `-Dftboss.debugSwordAxes=true` 开启，
  游戏里会画出 display 原点（白十字）+ 红+X/绿+Y/蓝+Z 三根一格长的轴 + 沿 ±Y 每 8 单位的刻度
  （白刻度在刃侧、黄刻度在柄侧），日志里同时打出当帧原点坐标。
  手持截图后照 `shadow_sword_pos_audit.cjs` 输出的 §E 步骤读数即可。
- ★ **参照体的"手心"要取拳心，不是手腕**：手臂 y 0.75~1.5、12 单位长、拳 4 单位高
  → 拳心比手腕低约 1.3 单位，在"脚底为 0"的参照体里是 **y ≈ 0.667**。
  早先 `hand_check.cjs` 取 0.8125（手腕位置），会让"剑偏低"变成假象（差 0.13 格）。
- ★ **握点通常死在拳的"内侧"，不是拳心**：拳 4 单位厚、柄贴在掌侧，
  所以"剑原点相对拳心偏内 7 单位（0.44 格）"是**正常**的；
  不能拿"原点到拳心的距离"当握持对错的判据，**垂距**（手心到握柄轴线）才有意义。
  本工具把剑显式平移到手心只是为了画图，别把图当证据 —— 数看控制台。
