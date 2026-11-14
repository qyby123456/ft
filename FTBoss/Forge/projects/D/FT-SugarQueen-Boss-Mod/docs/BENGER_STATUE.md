# 边牧犬王 benger 石像

- 方块与物品 ID：`ftboss:statue_benger`。加入 Boss 创造物品栏的石像分区，由真正的 benger 死亡奖励统一掉落一座；小狗不掉落，不重复添加实体战利品表奖励。
- 沿用其他石像的两格高放置与上下半格联动。破坏上半格、下半格或移除支撑时，只返还一座石像；空间不足不消耗物品。
- 默认显示黑白边牧原色，保留嘴里的木棍、耳朵、毛簇与四爪。尾巴收拢，底座采用石材与金色爪印。手持牧羊哨左键任一半格，切换原色/白膜，不消耗牧羊哨，也不召唤 Boss。
- benger 使用独立外观编号 10，原石像编号 0–9 保持不变。其他 Boss 召唤物不能给 benger 上色，牧羊哨也不能给其他石像上色。

## 模型实现

`tools/model3d/gen_statue_benger.cjs` 从现有 `benger.geo.json` 与 `benger.png` 派生，保留 122 个原模型部件的多轴旋转和逐面 UV。使用 Forge 内置 OBJ 加载器，无需额外模型加载模组，也不新增逐帧渲染器。

放置模型沿上下半格边界裁切，插值保留纹理坐标；上下半格共 1652 个三角形。物品栏/手持使用独立完整缩小模型，共 1560 个三角形。方块贴图位于 `textures/block`，不引用可能被实体材质模组接管的 `textures/entity`。白膜贴图保留雕塑细节和原透明度。

## 验证入口

- `node tools/verification/check_benger_statue.cjs`：部件完整性、面朝向、法线、UV、半格边界、裁切面积、OBJ 唯一分组、纹理和战利品引用。
- `tools/verification/gametest/BengerStatueGameTests.java`：7 项服务器 GameTest，验证实际物品放置、掉落、牧羊哨交互、错误召唤物、空间阻挡和支撑移除。包含于 `manor_integration_gametest.gradle` 的整包回归。
- `gradle -I tools/verification/benger_statue_model.gradle checkBengerStatueModels`：独立 JVM 调用 Forge 原生 OBJ 加载器，检查上下半格、白膜和物品模型全部成功解析、部件不丢失。测试用无窗口客户端占位，不启动游戏图形界面；不替代实机画面检查。
- `gradle -I tools/verification/benger_statue_gametest.gradle build runGameTestServer`：单独运行本次 7 项服务器测试，同时保留正常 `build` 的所有检查。
- `node tools/model3d/preview_statue_benger.cjs <预览路径.png>`：从最终 OBJ 资源渲染原色斜视、正视和白膜；这是离线模型预览，不是游戏截图，底座以代表色展示。
- 获取命令：`/give @s ftboss:statue_benger`。

## 回归说明（2026-09-19）

13:23 的整包源码快照运行 213 项 GameTest，其中 211 项通过，包括本次全部 7 项石像测试；另 2 项失败是现有 `LemonSacrificeGameTests.consumesOwnWholeArmyOnly` 与 `LemonSacrificeFxGameTests.consumedSourcesRemainRenderable` 的旧“30 分钟”断言。主工程已按另一需求改成继承能力持续至死亡，本次未改动该逻辑或对应旧测试，也未跳过正常构建检查。

后续打包重新同步了另一线程新增的挑战进度及本地化，避免用较早的石像包覆盖新功能。验证日志与独立产物保存在本实例 `_work/benger-statue-0919-01a0b304/`。

最终 `final-verified.log`：正常 `build`、5 个模型的 Forge 原生 OBJ 解析和全部 7 项专项 GameTest 通过。测试准备阶段清理测试点残留物品，避免重复运行已保存的 GameTest 世界时把旧掉落重复计入。

交付时主工程又出现另一线程正在进行的禁果配方修改，因此没有覆盖更新中的源码或盲目用较早快照重新部署。核验发现四个约定交付目标已由并行构建同步到与本次最终验证包 **完全相同的 SHA-256**；另逐条核验了 13 个石像资源、5 个接线类和中英名称。因此本次石像已在 Desktop 根目录、两处桌面成品目录及 `FarmingTales_ForgeⅡ v1.5.2_beta3/mods` 就位。

交付包 SHA-256：`4F8D8445068282A00E7D879C995592BE2D8FA6871E859420736E3A746870CB07`；大小 42,965,738 字节。未覆盖主实例 `FarmingTales Forge/mods`。
