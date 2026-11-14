# 旧 Boss 石像统一优化

## 范围与兼容性

优化糖霜女皇、屠夫、沙耶、酒桶王、鼠群、故障机器人、大便超人、苹果、清蒸鱼九座现有石像。benger 保留既有造型并参加回归校验。`statue_youzi` 是武器，不在本次可放置石像改造范围内。

所有注册 ID、方块状态、掉落、召唤物匹配、白石切换、创造栏和交易逻辑不变。老存档无需重放石像即可获得新外观。仍占上下两格，只有下半格产生一次掉落，匹配的召唤物左键任一半格切换原色/白石。

## 外观

- 使用当前 Boss 模型，而不是旧姜饼王、旧屠夫、旧故障核心。屠夫生成时逐字节校验 Java 模型和贴图与其规格源一致；沙耶使用 Alex 三像素细手臂和六部位第二层皮肤。
- 保留源模型全部立方体、完整父子骨骼和多轴旋转。鼠群使用立起的姿态并弯回完整三段尾巴，不再删除尾巴凑尺寸。
- 统一三层石台与深色边框，正面分别使用王冠、菜刀、叶、酒桶、爪印、闪电、堆叠、苹果、三叉戟金色徽记。
- 原色贴图复制到 block 目录，避免实体贴图兼容模组拦截；白石按原贴图明暗生成，保留纹样、五官和透明度，破坏粒子也跟随当前外观。
- Forge 内置 OBJ 按高度 16 精确分割多边形并插值 UV，上下半格不重复缩放。无需新增方块实体、每帧自定义渲染或外部依赖。
- 物品独立按完整包围盒居中，最长维度收进 14 模型单位，避免矮胖石像在背包里过小或偏下。原色/白石共用几何，透明皮肤使用 cutout。

## 生成与验证

```powershell
node tools/model3d/gen_boss_statues.cjs <临时资源目录> [sugar_queen butcher saya wine_barrel rat_swarm defect compost apple tide_fish]
node tools/verification/check_boss_statues.cjs <临时资源目录>
node tools/model3d/preview_boss_statues.cjs <预览目录> <临时资源目录>
```

必须先生成到临时目录、与旧成品比对，仅复制 models、textures、blockstates 下这九座的生成文件。未提供筛选时生成九座，不生成 benger 或柚子武器。旧 `gen_statue_real` / `gen_statue_skins` 对 OBJ 资源拒绝写回；旧 item 合并器跳过 OBJ，防止回退。

`gradle build` 包含 `checkBossStatueAssets`，检查全部几何面、上下边界、分割面积、UV 范围、法线、组名唯一性、物品居中、白石透明度、资源引用及既有掉落。清蒸鱼资源校验继续验证武器及 Boss，并调用新的石像校验。

实际 Forge OBJ 解析：`gradle -I tools/verification/benger_statue_model.gradle checkBossStatueModels`。验证十座石像共 50 个原色/白石/物品模型。该测试使用独立无窗口 JVM，不是实机画面测试。

服务端回归：`gradle -I tools/verification/benger_statue_gametest.gradle runGameTestServer`。包括九座旧石像的实际物品放置、上下半格、外观切换、召唤物不消耗、错误召唤物拒绝、上半格破坏/失去支撑后仅掉一个，以及既有 benger 七项回归。

预览基于实际 OBJ 与贴图离线渲染，底座石材颜色为示意，并非整合包实机截图；整合包光影和资源包的最终表现仍需实机确认。
