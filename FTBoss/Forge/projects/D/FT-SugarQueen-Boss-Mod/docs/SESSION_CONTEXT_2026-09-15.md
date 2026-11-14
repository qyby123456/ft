# 会话上下文压缩稿（截至 2026-09-15 15:3x）

> 用途：下一条对话/新会话直接读这一份即可接上进度。更细的历史在 `docs/CHANGELOG.md`（v128→v19 各条），
> 参考资料的量化结论在 `docs/MODEL_REFERENCE_NOTES.md`，工程硬规则在 `AGENTS.md`。

## 一、项目与铁律
- 工程：`D:\ft工程文件\FT-SugarQueen-Boss-Mod`（Forge 1.20.1-47.4.10，`modId=ftboss`，产物 `qys-bosses.jar`）。
- **只改 ftboss 自己的 `src/main/java/net/farmingtales/ftboss/**` 与 `assets|data/ftboss/**`**；别的模组只读、只按 id/反射引用。
- **两个会话同时改这棵树**：`BUILD_TAG` 归隔壁会话（现为 `easter-v155-…`），**不要动**；编辑前重读文件；
  `:reobfJar` 报 `zip END header not found` = 并发写 jar，重跑即可。
- 实例游戏目录 = `.minecraft\versions\FarmingTales Forge`（日志 `logs\latest.log` / `debug.log`，**不是** `.minecraft\logs`）。

## 二、环境与流程
- 构建：`JAVA_HOME=.minecraft\runtime\java-runtime-gamma-snapshot`，gradle 8.8 全路径 + **`--rerun-tasks`**。
- 部署：`powershell -NoProfile -ExecutionPolicy Bypass -File tools\deploy_jar.ps1 -NoBuild` → 4 路（实例 mods + 桌面 + 两个成品目录），清 `lightspeed-cache`；游戏在跑时会拒绝部署。
- 校验：`tar -xf` 抽 jar 内文件比 sha256 + 四路 jar 哈希；jar 里的 `.class` 是**重混淆**过的，不能与 `build/classes` 比哈希。
- PowerShell 5.1 坑：`[ ]` 当通配符（用 `-LiteralPath`）；无三元 `?:`；中文文本禁止 `Get-Content/Set-Content` 往返（双重编码）；
  `tar -tvf` 列宽会变（读别人 jar 用 .NET `ZipFile::OpenRead`）。读别人 mod jar 也别用 tar。
- 音频（更早轮次）：ffmpeg = `C:\Users\YOUR_USERNAME\AppData\Local\oopz\ffmpeg.exe`；导出目录 `C:\Users\YOUR_USERNAME\OneDrive\桌面\FT-Boss-音频_2026-09-14\`（真实桌面是 OneDrive 那个）。

## 三、模型工具链（可复用）
- `tools/model3d/lib_itemtex.cjs`：画布（**全 clamp**）/ 侧盖面**装箱器**（尺寸要乘 k）/ 1px 描边 /
  **四项自检**（暖色杂点、正反镜像、浮点噪声、饱和金色杂点）/ `writeSafe`+`safeCopy`（自动清只读）。
- `tools/model3d/gen_weapons3d.cjs`：数据驱动武器生成器（`SPECS` 一条 = 一把武器）。**12 把武器**已升级：
  黑色切割者、河豚棒、屠夫剁骨刀、茶刃、文玄蓝的飞轮、绽放的荆棘、酒瓶锤、奶酪轮锤、糖霜权杖、打狗棒、苹果果梗（+ 暗影剑单独脚本）。
  **糖拐 `candy_cane`、酒桶弩 `barrel_crossbow`(含装填态)、肉腿 `turkey_legs` 按用户要求未动。**
- `tools/model3d/regen_shadow_sword.cjs`：暗影剑专用（部件表 + 10 条剑身 + 逐面 UV + 画法）。
- `tools/model3d/java_to_geo_rig.cjs`：静态 Java 物品模型 → 带骨骼 Bedrock geo + 动画；
  用法 `node java_to_geo_rig.cjs <id> <geo名> <动画名> [输入模型路径]`。
- `tools/model3d/add_attack_anims.cjs`：补 `attack/attack_double/claw_snap`（**转换器会覆写动画文件，之后必须跑它**）。
- 其他：`import_item_model.cjs`、`lint_java_model.cjs`、`check_uv_coverage.cjs`、`preview_spin.cjs`
  （**只画每面平均色**：看不出贴图细节/半透明/UV 左右方向）。

## 四、暗影剑「噬影之刃 · 裂瞳」当前状态
- 贴图 **256×256** 逐面 UV；配色用用户给的 7 色：`#100E18 #221D2C #504759 #563374 #A96CDC #EAD5F5 #55424E`；
  另有发光层 `textures/item/shadow_sword_e.png`（**ETF 认 `_e` 后缀**，日志已确认）。
- 几何 **62 cube**：剑身每段 **10 条**（4 级刃口楔形 + 剑面 + 圆背三级 + **血槽**），4 段错位（微弯），
  三级收薄 + 22.5° 旋转真斜尖；护手 = 钩爪 + **三件套竖瞳魔眼**（厚 3.0~3.5，正反都凸）；柄 = 深色柄芯 + 斜缠带 + 上下金属箍；
  柄首 = 两段黑底座 + 双面凸出紫晶核。
- **GeckoLib 骨骼版**：`geo/shadow_sword.geo.json`（**11 骨骼** root→weapon→{pommel→crystal, handle, guard,
  claw_left, claw_right, eye→pupil, blade}）+ `animations/shadow_sword.animation.json`（**5 段**：
  idle 4s / awaken 0.7s / attack 0.55s / attack_double 0.95s / claw_snap 0.4s）。
- Java：`item/weapons/ShadowSwordItem`（`extends BlackCleaverAndShadow.ShadowSword implements GeoItem`，
  **玩法零改动**：伤害 + 右键投末影珍珠致盲 AOE）+ `client/render/ShadowSwordGeoRenderer`
  （`GeoItemRenderer` 本身即 BEWLR，`initializeClient` 直接返回）。物品模型 = `{"parent":"builtin/entity"}` + display 变换。
- 触发：砍中实体 → `attack`（每第三次 `attack_double`）；右键 → `claw_snap`；服务端 `triggerAnim` 自动同步。
- **已部署哈希 `5C73762E5974F5A2`**（62 cube / 旋转 9 / inflate 11 / 四项自检全绿）。
  静态版备份：`ref/shadow_sword_static_v18.json.bak`（内容 = 62 cube 修正版）。

## 五、参考资料结论（详见 docs/MODEL_REFERENCE_NOTES.md）
- **YSM**（`OneDrive\桌面\yes_steve_model`，明文 geo+png；`.ysm` 是加密包）：旗舰 412 cube / 75% 带旋转 /
  34% inflate / 188 骨骼；连一支箭都 27 cube / 25 旋转 / 100% 逐面 UV → **细分靠多而小、旋转块做斜构件、inflate 做分层**。
- **灾变**（`L_Enders_Cataclysm-3.31`）：模型/骨骼/动画**全编译进 class**（0 geo、0 animation json）→ 只能参考结构手感；
  贴图可读（大 Boss 512²、武器 128²、有 `_layer` 发光层与 ghost 变体）→ **细噪点、热芯发光裂纹、面板 1px 描边、板边铆钉**。
- **农场物语体系**：`[森罗物语：烹饪]` 有 **673 个 3D 食物模型**（最多 26 cube）；食物 = **小方块 + 22.5° 旋转摆盘**
  （26/26、19/20 全带旋转）、100% 逐面 UV、不写 shade/tint/inflate、贴图 32×32（PNG 常 16×16）、高度仅 5~6.5 格。
- `[农场物语-核心]farmingtales_core` **0 个模型**（只是框架包）。

## 六、硬坑清单（已进 AGENTS.md）
1. **Java 物品模型 ↔ Bedrock geo 差异**：cube 旋转必须是**数组 `[x,y,z]`**（Java 是 `{origin,axis,angle}` 对象）→
   抄错 = `GeckoLibException: Error loading model file`，表现为**资源重载卡死**；UV 是像素 `uv`+`uv_size`（负值表镜像）；
   **原点是方块中心 (8,8,8)，x/y/z 三个都要减 8**（只减 x/z → 手持悬空 8 格）。
2. **父链根是 `builtin/generated` 又没有 `layer0`** ⇒ 模型烘不出面，物品**完全不见且日志零报错**；
   `builtin/entity` 而 BEWLR 没接好同理。
3. **写像素/插值必须 clamp**：外插 >255 写进 `Uint8Array` 回绕成**金色杂点**（用 `checks.goldArtifacts()` 判错）。
4. **逐面 UV 要配比例正确的块**（共用一条 → 拉伸）；**宽面 north/south 必须互为镜像**。
5. **相邻方块重叠 0.05** 防 z-fighting；**斜插半埋的薄片（倒角/背侧切面）从侧面会露出悬浮小块**（已全删）。
6. 资源带**只读属性** → 写入 EPERM，统一走 `writeSafe/safeCopy`。
7. 切到 `builtin/entity` 后物品模型**没有 `elements`**，转换器必须用 `ref/*_static_*.json.bak` 当输入。
8. 给物品挂 GeckoLib：`GeoItemRenderer` 本身继承 `BlockEntityWithoutLevelRenderer`；`GeoItem` 的父接口在
   `software.bernie.geckolib.core.animatable.GeoAnimatable`（有 `core`）；**写 Java 前先用 javap 从
   `libs/[前置]geckolib-forge-1.20.1-4.8.4.jar` 核对签名**（jar 里还自带 `JackInTheBoxItem` 范例可抄）。

## 七、待办（按序）
1. **刀刃贴图简化**：当前刀身叠了 10 条色阶 + 逐条 AO 缝 + 折钢缝 + 噪点 + 裂纹 + 高光点，太繁 →
   砍到 **2 级刃口 + 单一剑面渐变 + 少量噪点**，折钢缝与多数 AO 缝去掉，只留裂纹点缀。
2. **刀刃加长**：分段表 / 剑尖三段 / 剑尖贴图落笔 **三处必须一起改**（改一半会贴图错位）。
3. **柄尾"两个模型穿模"收尾**：紫晶核改成嵌进底座环、只露两面（要重画那块贴图）。
4. **物品栏里也在播 idle** → 按 `ItemDisplayContext` 分场景；GeckoLib `isPerspectiveAware()` 的确切语义**未核实，别瞎改**。
5. **补文档 v19**：GeckoLib 接入全流程 + 第六节 1/2/8 三条坑 → `CHANGELOG.md` / `AGENTS.md`。
6. 标准复现链：
   `regen_shadow_sword.cjs` → `switch_shadow_entity.cjs`（备份并换存根）→
   `java_to_geo_rig.cjs shadow_sword shadow_sword shadow_sword ref/shadow_sword_static_v18.json.bak` →
   `add_attack_anims.cjs` → build + deploy。
7. 物品若在游戏里消失，回退：把 `ref/shadow_sword_static_v18.json.bak` 拷回
   `src/main/resources/assets/ftboss/models/item/shadow_sword.json`，再 build + deploy。
