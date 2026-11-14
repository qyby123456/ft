# 黑切重建版接续记录

日期：2026-09-16。接续基线：DS 21:33 的 v4 预览，不是正式资源，也不是游戏目录里的旧副本。

## 工作边界

- 维护源：`../../tools/model3d/gen_black_cleaver_rebuild.cjs`。
- 本轮仅调整重建预览生成器和两项配套工具，输出仍在 `art/rebuild/`。
- 不构建、不部署，不修改 `src/main/resources`、Java、物品 display 或已部署 jar。
- 保留 DS 的主副刃造型、四片活动翼、骨骼名称和父子关系；不擅自改轮廓参数。
- 游戏目录 `.dsh-analysis/swordcheck/proj` 是旧快照，不可回写正式工程。

## 当前产物

- `black_cleaver_rebuild.geo.json`：17 骨骼、90 cube、540 个逐面 UV，256×256 图集。
- `black_cleaver_rebuild.animation.json`：idle、blood_pose，两段预览动画。
- `black_cleaver_rebuild_open0.geo.json`、`black_cleaver_rebuild_open6.geo.json`、`black_cleaver_rebuild_open11.geo.json`：三个静态骨骼姿态样本。
- 主、副弧当前 sweep 是 120° / 110°；生成器头部关于 150° / 140° 的旧说明不是当前参数。
- 尺寸约 16.318 × 37.011 × 6.320 模型单位。正式版仍是独立的 17 骨骼 / 41 cube，不能混用两版报告。

## 本轮修复

1. `radialStrip` 为 76 个旋转 cube 显式输出中心 pivot。
   - GeckoLib 4.8.4 实测：`Cube.deserializer` 读取缺省 pivot，`JsonUtil.jsonArrayToDoubleArray(null)` 返回 `[0,0,0]`，`BakedModelFactory.Builtin.constructCube` 使用该 pivot。
   - 所以省略 cube pivot 并不代表绕 cube 中心转。旧 `render_tex.cjs` 却默认用中心，导致旧预览与实际加载语义不同。
   - 此修复将旧预览想表达的中心枢轴写入模型；所有 origin、size、rotation、UV、骨骼参数不变，四张模型贴图 SHA256 不变。
2. `wing_pose_bake4.cjs` 改为给四根翼骨骼写 rotation，不再改 cube 原点、覆盖其斜切角。拒绝非法角度和覆盖输入模型。
3. `verify_rebuild_claims.cjs` 通过 `cube.label` 识别背板，兼容旧 `cube.name`。旧版找不到背板，检查结果不能作为通过证据。

通用 `render_tex.cjs` 最终没有改动。新对照图使用支持显式 cube pivot 的 `render_geo.cjs`，避免顺带改变其它模型的预览行为。

## 已验证

- `validate.cjs`：结构 error / warning / review 均为 0。
- `anim_key_order_scan.cjs`：5 条时间轨道均升序。
- 接续专项验证：除新增 76 个 pivot 外，与 DS v4 几何逐字段相同；四张模型贴图哈希相同。
- 共用 `cube_space.cjs` 验证 0°、6°、11°：每个姿态 90 个 cube、720 个角点与骨骼姿态变换完全一致，最大角点误差 0。
- 4 个非法角度与覆盖输入请求均被拒绝，源文件未改变。
- 正式模型、动画、display、贴图、两项 Java 渲染代码及部署 jar 共 12 个文件 SHA256 与开工前相同。
- 本轮没有启动游戏验收，没有把离线检查当成最终外观通过。

## 尚未收敛

- 当前正视/斜视预览中，中央体较大，副刃仍有明显遮挡；双头斧辨识度需要后续美术确认。
- 修复背板标签识别后，旧的“背板包络必须完全包含于单片翼包络”检查报出三处超出。背板本来跨翼与静止刃的接缝，仅这个判据不足以证明可见穿帮；下一步应检查整个静止刃、斧眼、轴盖参与遮挡后的可见表面，不能把它忽略成全通过。
- `rebuild_joint_gap.cjs` 在 0/6/8/11° 报最大新增缝 0，但它比较全局最大空隙，且每次只转一片翼；不能证明四翼同时展开后的每个局部接缝无孔洞。旧 `joint_gap.json`、`quality.json` 仍是早期版本，不应当作当前结论。
- 像素密度复核：主体中位 2.5 px/单位，略高于项目 1–2 目标；126 个面有提示，高密度面积占比 3.2%，最大 5.556。检查器打印“在目标范围内”不代表完全达标。不自动缩图、不改已确认 UV。
- 重建动画只有两段，标识符带 `_rebuild`。它尚不具备直接替换正式模型所需的完整技能动画与接线；不要直接拷入正式资源目录。

## 证据与备份

接续验证目录：`C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales Forge/.dsh-analysis/cleaver-handoff-20260916/`

- `tools/`：本轮三个修改工具的开工前备份。`render_tex.cjs` 是临时修改时的快照，不是开工前版本，勿用于整批还原。
- `preview-before/`：DS v4 开工前全部预览产物。
- `verify-handoff.cjs`、`verification.json`：可复跑的专项验证及结果。
- `runtime-before.csv`：正式文件开工前 SHA256。
- `pose-0.png`、`pose-6.png`、`pose-11.png`、`comparison.png`：修复后的结构对照图，非游戏截图。
- `pixel-style.json`：本轮纹理密度复核。

## 复现

在工程根目录运行：

```text
node tools/model3d/gen_black_cleaver_rebuild.cjs
node tools/model3d/wing_pose_bake4.cjs art/rebuild/black_cleaver_rebuild.geo.json art/rebuild/black_cleaver_rebuild_open11.geo.json 11
node tools/model3d/validate.cjs art/rebuild/black_cleaver_rebuild.geo.json
node tools/model3d/anim_key_order_scan.cjs art/rebuild/black_cleaver_rebuild.animation.json
node tools/model3d/render_geo.cjs art/rebuild/black_cleaver_rebuild_open11.geo.json art/rebuild/black_cleaver_rebuild_geo.png art/rebuild/current_open11.png 0,35 10
```

如需继续验证 0°、6°，先分别运行 `wing_pose_bake4.cjs` 输出对应样本，再运行接续验证目录中的 `verify-handoff.cjs`。
