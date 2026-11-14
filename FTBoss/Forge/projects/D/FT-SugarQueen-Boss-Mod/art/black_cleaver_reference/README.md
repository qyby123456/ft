# 黑切 · 掌心与刃面校准版 07

以 `C:\Users\YOUR_USERNAME\OneDrive\桌面\自绘黑切.png` 为原型制作的可编辑立体模型。本版继续保留上一版的粗柄厚刃，并完善金属层次、刃背结构和血怒动画。

本版已接入正式资源与客户端血怒渲染逻辑。部署状态、Jar 哈希及安装位置以同目录 `deployment.json` 为准。尚未在游戏或 Blockbench 图形界面中验收；动画视频仍是离线预览。原版 DS 几何未复用。

## 本次掌心与刃面校准（07）

- 12:38/12:39 的截图说明，06 版只验证“手部坐标内柄轴竖直”不够。原版持物手臂还会前抬 18°，手部层的物品原点也不等于掌心。
- 旧参数在实际原版矩阵链里偏离掌心约 2.83 模型单位，主刃朝向身体侧面。本版第三人称右手改为旋转 `[72,90,0]`、平移 `[1.4,-2,2]`，让握柄落在手掌内部、距中心向外 1.4 单位的抓握区，为厚斧头留出静止姿势的头部间距；主刃朝前，静止柄轴端正。
- 第一人称旋转改为 `[0,125,0]`，保持原位置和 0.55 显示比例，不再把整片宽斧面正对镜头；第三人称比例仍为 0.85。
- 使用原版粗臂/细臂模型、ITEM 持物姿态、待机摆臂、`translateToHand` 及物品变换检查 36 组握持状态，另保留 44 组第一人称视野检查。几何、贴图和血怒动画不变。
- `native-hand-preview.png` 四栏依次为右手正视、右手侧视、血怒展开斜视、左手斜视。直接绘制原版矩阵变换后的顶点，没有人为把武器挪到手上；仍是离线参考，不是实机验收。
- 游戏安装状态以 `deployment.json` 为准；运行中的游戏绝不能覆盖模组包。

## 上版端正持握（06，已被实机否定）

- 11:16 和 11:29 的实机截图显示，上版主动加入的斜持仍不符合要求；这次取消第一人称 12°、第三人称 15° 的侧倾，以及第三人称额外 20° 前倾。
- 第一人称右手旋转 `[0,160,0]`，第三人称右手旋转 `[90,0,0]`；这里的 90° 用于抵消手部坐标基，并不是再让武器歪 90°。
- 第一人称仅平移到 `[-0.7,-0.5,-4.5]` 保持斧头入画；两种视角的显示比例仍为 0.55 / 0.85，握点、厚度和粗柄不变。
- 左右手都增加“最终柄轴必须竖直”的原生矩阵回归检查；旧斜持参数明确测试失败，新参数通过，另保留 44 组视野检查。
- 几何、UV、贴图、血怒动作及物品栏/地面/展示框参数不改。实体自身抬臂、行走和挥击仍会带动武器，实际画面需游戏验收。

## 上版视角校准（05）

- 根据 2026-09-17 09:35 的实机截图，修正第一人称斧头出框、第三人称斧头贴肩的问题。
- 第一人称后移、降低并转到能展示完整斧面的方向；只将该视角的显示比例由 0.65 改为 0.55，不改变模型厚度和握柄粗细。
- 第三人称比例仍为 0.85，握点不动，斧柄向前约 20°、向外约 15° 倾斜，避免沿手臂竖着顶肩。
- 针对非对称斧头处理左右手的朝向，保证两手都能把大斧刃放进视野；物品栏、地面、展示框参数不变。
- `view-validation.json` 检查原版持物投影下 44 个视角/姿态组合；并不替代人物皮肤、行走和挥击时的实机验收。
- 本次仅更新正式资源中的物品 display JSON，几何、贴图、血怒动画保持上一修正版。部署状态见 `deployment.json`。

## 上版重面修复

- 修正 GeckoLib 的 X 镜像与旋转符号组合：斧面长条不再转反 90°，消除截图里的错层、破洞与大片交叉重面。
- 按实际 GeckoLib 顶点顺序修正正反面的 U 方向，避免斧面符文在每个条块中局部镜像。
- 活动翼面相对固定板内收 0.2 单位，柄顶包铁和木柄错开 0.3，防止血怒展开与柄顶的共面闪烁。
- 整体几何上移 5.5 单位，让粗黑握柄的中点成为原点，攻击绕该点转动；抵消 GeoItemRenderer 的额外 0.01 格偏移。
- 第三人称按实际手部坐标基旋转 90°，不再使用旧 DS 的斜向参数；左右手对称，第一人称、物品栏图标同步调整。
- `gecko-validation.json` 直接使用本机 GeckoLib 4.8.4 烘焙器、矩阵辅助函数与 Minecraft ItemTransform 检查，不再仅让离线预览互相证明。
- `surface-validation.json` 在 419 个姿态下检查斧面正反两侧的可见重面。当前角色的手掌贴合仍需重启后的截图验收。

## 先看什么

- `blood-rage-preview.mp4`：9.75 秒、24 fps、1100×900 的双视角动画，可直接播放，无音轨。
- `blood-rage-preview.gif`：同一段动作的循环预览。
- `animation-sheet.png`：从闭合、启怒到重劈、退怒的八个关键姿态。
- `black_cleaver.bbmodel`：可编辑模型，已嵌入主贴图与全部 12 段骨骼动画。建议使用带 GeckoLib 插件的 Blockbench，选择 `animation.black_cleaver.blood_showcase` 查看完整流程。
- `ANIMATION_GUIDE.md`：逐段时长、发光层说明和游戏接入边界。

## 造型优化

- 主斧面厚 4.8，副斧面厚 4.0；保留 2.30×2.20 的黑色握柄、2.10×2.05 的棕色柄身，柄芯长度不变。尺寸均为模型单位。
- 将原来的单道银色台阶改为分级刃带：主刃 4.8 → 暗银肩 3.65 → 亮银刃 2.2 → 红色刃口 0.8，既保持份量，也读得出开刃。
- 刃背新增厚 5.4 的暗铁加强条及少量铆钉；侧面增加压暗与边缘高光，不铺满细碎噪点。
- 四翼接缝的内衬厚度由 0.22 增至 0.7，维持展开时的连接；核心枢轴校正到宝石中心，脉动不再偏心。
- 17 个骨骼、357 个方块、2142 个面，256×256 图集；合拢外包围约 24.40×38.27×6.60。

## 动画设计

保留厚重武器的稳定感：握点不平移，柄身不做橡胶式缩放。四翼在 0°～11° 内运动，维持阶段只做约 0.48° 的短促回压，不持续大幅扇动。

- 启怒：1.4 秒，核心先收紧发亮，接缝先亮，四翼错峰展开，符文和刃口随后亮起。
- 维持：2.4 秒循环，以一次强拍、一次弱拍形成心跳；核心、刻痕与刃缘按不同强度响应。
- 血怒重劈：0.65 秒，蓄势时刃翼略回压，0.38 秒释放并达到刃光峰值，随后回震；另有 0.3 秒回收。
- 退怒：1.4 秒，刃光先熄，接缝后暗，四翼缓慢合拢，不瞬间跳回闭合。

完整演示是独立 `blood_showcase` 动画，默认循环；五段血怒动作也分别可编辑。另保留普通 `idle / charge / slam / recover / ready` 和校准用 `blood_pose`。

## 文件清单

| 文件 | 用途 |
| --- | --- |
| `black_cleaver.geo.json` | GeckoLib / Bedrock 几何，标识为 `geometry.black_cleaver` |
| `black_cleaver.animation.json` | 标准 GeckoLib 骨骼动画，12 段、6952 个通道关键帧 |
| `black_cleaver.effects.json` | 自定义四层发光强度曲线，由本版客户端渲染层采样，不是 GeckoLib 标准动画 |
| `black_cleaver_geo.png` | 主贴图 |
| `black_cleaver_core_emissive.png` | 核心独立发光遮罩 |
| `black_cleaver_rune_emissive.png` | 刻痕、柄部红色嵌件发光遮罩 |
| `black_cleaver_edge_emissive.png` | 刃口独立发光遮罩 |
| `black_cleaver_blood_seam_emissive.png` | 展开接缝发光遮罩 |
| `black_cleaver_blood_emissive.png` / `black_cleaver_emissive.png` | 保留的旧接口合并发光贴图与常规核心贴图 |
| `hero.png` / `side.png` | 立体视角与侧厚预览 |
| `comparison.png` | 与原始手绘同角度对照 |
| `mass-comparison.png` | 与最初细柄版的厚重感对比 |
| `model-sheet.png` | 正背侧、灰模与展开总览 |
| `reference.png` | 原图原样副本 |
| `report.json` / `validation.json` / `film-validation.json` | 规格、模型与动画检查、动态预览校验及资源哈希 |

## 验证结果

- 几何校验：0 error / 0 warning / 0 review；动画结构校验：0 error / 0 warning。
- 62 条动画通道时间键升序；6952 个导出关键帧与 Blockbench 工程逐项一致。
- 三段循环的首尾，以及普通/血怒共八组动作衔接点的姿态和发光值连续。
- 2142 个面共采样 26442 个 UV 像素，无空白面或越界；参考图与嵌入贴图哈希匹配。
- 五档四翼开合姿态均为完整连通结构；血怒五段动作按 60 fps 检查了 374 个内部姿态，另检查 39 个中途退怒混合姿态，未发现分离部件。全武器的刚性挥动不改变内部连接。
- 主体像素密度中位数 2 px/单位，高密度面积约 0.8%；缠带与小嵌件仍有 144 个面级 review，最高 3.57 px/单位，不等于完全没有审查提示。
- 血怒入场、循环和退场由客户端按导出的名字采样；技能使用 GeckoLib 同步控制器，自动衔接回收。`blood_pose` 与 `blood_showcase` 仍只用于校准和展示，不在游戏中触发。
- Java 专项测试对照 1338 个离线姿态，共 221037 项断言通过，覆盖快速反向、循环衔接、物品状态隔离、绘制不推进时钟与技能回收。
- 动态预览直接采样导出的动画 JSON，不使用另一套手写姿态。固定镜头检查全部 234 帧，两视角都未裁切模型。

这些检查不能替代实机持握、遮挡、特效混合与技能命中时序验收。GIF / MP4 的发光来自独立遮罩的离线模拟；Blockbench 工程主要用于查看骨骼动作，不会自动复现客户端发光层。

## 源码与再生成

工程根目录：`D:\ft工程文件\FT-SugarQueen-Boss-Mod`。

- `tools/model3d/gen_black_cleaver_reference.cjs`：模型、贴图、动画及编辑工程导出。
- `tools/model3d/cleaver_reference_motion.cjs`：动作时长、曲线、关键帧烘焙、发光曲线的共同源头。
- `tools/model3d/render_cleaver_reference.cjs`：共享几何变换的深度缓冲渲染器。
- `tools/model3d/check_cleaver_reference.cjs`：资源、层级、UV、动作衔接与密集姿态连通检查。
- `tools/model3d/film_cleaver_reference.cjs`：读取导出 JSON，渲染动态预览并用 FFmpeg 编码。
- `tools/model3d/sheet_cleaver_reference.ps1`：静态对照、结构总览和动作关键帧排版。

在工程根目录执行：

```powershell
node tools/model3d/gen_black_cleaver_reference.cjs
node tools/model3d/check_cleaver_reference.cjs
node tools/model3d/check_cleaver_runtime.cjs
node tools/model3d/check_cleaver_gecko.cjs
node tools/model3d/check_cleaver_surfaces.cjs
node tools/model3d/render_cleaver_reference.cjs
node tools/model3d/film_cleaver_reference.cjs
pwsh -File tools/model3d/sheet_cleaver_reference.ps1 -PreviousDirectory "_backup/black_cleaver_reference_mass_20260916_224916/art"
```

动态预览需要本机 FFmpeg 和微软雅黑字体；可用 `FFMPEG_PATH`、`CLEAVER_PREVIEW_FONT` 环境变量覆盖。`--stills` 只渲染关键姿态，`--encode-only` 使用已有帧重新编码。原始帧留在 `art/black_cleaver_reference/motion-frames`，桌面交付不包含这些中间帧。

本次修改前的源码、资源与桌面交付备份位于 `_backup/black_cleaver_reference_animation_20260916_230358`。

正式接入前备份：`_backup/black_cleaver_deploy_20260916_234042`，包含旧 Jar、对应 Java 和正式模型资源。

确认模型后，用 `node tools/model3d/gen_black_cleaver_reference.cjs --publish` 仅发布 11 个黑切资源到正式资源目录，并自动备份旧文件；包括持握 display 的物品模型，不触碰其他武器。随后运行 `pwsh -File tools/deploy_jar.ps1` 构建并安全部署。不得在游戏运行时使用强制部署。

本次修复前的完整资源、代码、桌面交付及已安装 Jar 备份：`_backup/black_cleaver_overlap_grip_20260917_082214`。

不要用旧 `gen_black_cleaver.cjs` 或 DS rebuild 生成器重建本版。手工编辑 `.bbmodel` 前请另存副本，重新运行生成器会覆盖生成文件。
