# FT 地牢装饰 · 指定十二款重制 1.1.0

**Minecraft Java 1.20.1 · Forge 47.4.10 · Java 17。全包仍有24款，本次只重制原编号06–13、15、16、21、23。**

其余12款的模型、可编辑源、所引用贴图与原版1.0.0逐字节核对。保留所有方块ID和原编号，旧版交付保留不覆盖；重制范围和变化见 `REMAKE.md`、`qa/remake-scope.json`。

参考 Mine Cells 的囚狱冷石、锈铁与冷暖灯光语言，模型和像素原创，不包含其模型、贴图、徽记或代码。无需安装 Mine Cells 或 GeckoLib。06保留原生台阶形状，重做上表面、底面、围边，且上下/双层都使用新材质；其他11款重做结构与材质。

## 交付

- `FT地牢装饰-指定12款重制-Forge-1.20.1-1.1.0.jar`：包含全部24款的独立模组候选，namespace `ftdungeonprops`。
- `preview/remade-12.png`：本次十二款；`preview/before-after-12.png`：同机位重制前后；`preview/all-24.png`：全套。
- `preview/index.html`：离线WebGL三维浏览器，默认只显示重制12款，可拖动、缩放、切换灯态或显示全套。无需CDN、服务器或付费图片API。
- `preview/four-view/`：最终导出模型的96张视图及24张四视图合页；七份明暗对比另存于 `preview/`。
- `model_sources/`：24份可编辑Java方块格式 `.bbmodel`，内嵌全部32px贴图，与最终运行资源逐面核对。
- `revisions/`：本次三条制作线的原始几何、图集、像素材质表和可复现生成器；`lanes/`保留基线输入，运行时只取未重制的条目。
- `forge-project/`：Java运行源码、运行资源、Gradle wrapper、导出/渲染/验证工具；`qa/`：实际检查报告与冻结哈希。

三个独立制作聊天处理建筑3款、陈设7款、灯具2款；独立QA聊天复验；主聊天统一集成。真实聊天ID见 `qa/parallel-work.json`。重制材质使用独立 `remake-<lane>` 集，避免更换共用贴图误改未选中的模型。

## 安装与行为

先备份实例，在测试实例中将新JAR放入 `mods/`，客户端和服务端都需安装。**同一实例只保留一个 `ftdungeonprops`，不要同时安装1.0.0和1.1.0。** 无须删除独立的 `ftdungeonmobs`。

此包不改变地牢生成、难度、旧怪物/房间逻辑，不提供方块实体、库存、伤害、炼金、信标效果或持续扫描。装饰性名字不代表这些玩法机制。

- 创造栏“FT地牢装饰 · 囚狱套件”；命令如 `/give @s ftdungeonprops:prison_weapon_rack`。
- 装饰按水平方向摆放，正面朝玩家；墙饰背板到自身后边界，吊灯挂点到顶边界，但不会强制验证支撑或自动掉落。
- 七灯默认亮。具有建造权限且主手空手右键时切换 `lit`；持物不切换。明暗态几何相同，原版亮度切换，不是着色器自发光。
- 原生楼梯/台阶支持原版含水及形态；其他装饰不含水。06双层台阶使用对应重制材质，掉落两个台阶。
- 需镐正确采掘。普通件掉自身；冷蓝基材由石砖＋深板岩砖合成4个，楼梯/台阶为形状配方，其余为基材＋不同附加材料。原配方和掉落不改。

**本次没有自动安装、修改正式FT工程或存档，没有打开游戏客户端/世界服务器，没有切换其他任务的共享Blockbench。**

## 检查范围

- 新旧版哈希与指定范围：12款重制、12款模型/源/引用像素不变，24个运行ID及配方/掉落保持。
- 24份源模型、Java导出、UV、内嵌PNG与原图集逐像素核对；整数单格、正体积、无重叠、结构预算和灯态几何一致性。
- 83份 `.bbmodel`/Java模型通用结构审计；`generic`只验证共同结构，不冒充1.20.1版本依据。
- Forge 47.4.10真实数据生成启动目标验证24方块/物品、190原生状态、59份模型解析、24配方及四方向碰撞和视觉体积、56灯状态亮度。
- 实际导出JSON固定投影和光照的96视图、12款前后对比；Edge无头WebGL逐款加载、两款重制灯材质切换、交互和移动布局。
- 独立QA结论、具体哈希、发现与修复以 `qa/independent/` 最终报告为准。不隐藏修复前失败，不复用旧候选的PASS。

软件渲染不是游戏截图。可编辑源已独立解析与导出逐面比较，但不声称在共享Blockbench打开验证。客户端视觉、手持遮挡、真实放置、动态光传播、整合包互操作和FPS未实测。

成本以 `qa/performance-summary.json` 为准：默认各方块选择一个状态与所有声明模型分开统计；上下/双层、明暗态不可能全部同时渲染。sprite进入原版图集，文件数量不是draw call；RGBA字节仅为基础估计，不是显存实测。无动画帧、透明卡片、方块实体或每刻扫描。

## 重新构建

在 `forge-project/` 里使用Java17和PowerShell；`BUILD.ps1`可用 `JAVA_HOME_17` 或本机Minecraft Java17。依赖已缓存时加 `-Offline`。

```powershell
.\BUILD.ps1 -Task @('build','runData') -Offline
```

`runData`只运行Forge数据生成/验证，不启动世界。重新创作时依次运行三条 `lanes/<lane>/make_assets.py` 和三条 `revisions/<lane>/make_assets.py`，需要Python3.10+、Pillow；CPU渲染另需NumPy。

```powershell
python tools/export_kit.py --minecraft-jar "<本地Minecraft1.20.1客户端JAR>"
.\BUILD.ps1 -Task @('build','runData') -Offline
python tools/audit_delivery.py --minecraft-jar "<客户端JAR>" --jar "build/libs/ft-dungeon-prison-decor-forge-1.20.1-1.1.0.jar"
python tools/render_kit.py --minecraft-jar "<客户端JAR>"
python tools/audit_remake.py --minecraft-jar "<客户端JAR>" --baseline "<原版完整交付目录>"
python tools/render_remake.py --baseline "<原版完整交付目录>" --minecraft-jar "<客户端JAR>"
```

应等所有制作线完成后才导出并冻结，不可导出后继续改图集。技能审计另需本机建模skill脚本，浏览器验证需Playwright和Edge；路径可用工具参数/环境变量指定。最终打包还要求独立QA通过且资源/源哈希未改变。

作者：一颗大豆 / B站 UID355533780。来源与权利说明见 `CREDITS.md`。
