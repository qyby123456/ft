# 1.2.0 验证记录与边界

## 验证对象

Minecraft 1.20.1、Forge 47.4.10、Java 17，mod ID 为 ftdungeontrading。本轮使用独立重做工程，不修改整合包的 mods、配置或存档，不自动启动 Minecraft 客户端。

参照交易桌概念图和新场景图制作两格宽长桌。其他模组的桌子 JSON 与真实贴图只用于研究结构、父模型和 UV，未复制其几何，也不分发其贴图。reference-study.json 是技术研究记录，不是第三方美术授权。

## 结果

| 层次 | 结果 | 证据 |
| --- | --- | --- |
| 编辑工程 / 物品模型 | 52 组件、270 声明面，单图集 | model-report.json、可编辑 bbmodel |
| 完整桌子的世界模型 | 左 34 组件 / 174 面，右 24 组件 / 120 面，合计 58 组件 / 294 面 | 两份半桌 JSON、assembly-structure-check.json |
| 结构与接缝 | 六个连续承重结构按方块边界切开；不重复渲染整张桌，不生成内部接缝面；分割 UV 对应原表面 | assembly-structure-check.json |
| 包围范围 | 含旋转顶点的完整模型范围约为 [-7.58,0,0.42] 至 [23.58,15.85,15.58]，以两格中点为中心 | roundtrip-render-check.json |
| 图集与透明 | 128×128、16,384 像素；266 个实心面和两片蛛网的四个二值 cutout 面，无半透明壳 | model-audit.json、roundtrip-render-check.json |
| 隐藏面 | 有轴对齐、实心且完全遮挡证明后删除 34 面；旋转件和蛛网不当作实心遮挡体 | hidden-face-proofs.json |
| 成本预算 | 编辑 / 物品 ≤64 组件 / 352 面；世界装配 ≤80 组件 / 432 面；单图集 ≤16,384 像素 | budget.json、cost-audit.json、resource-check.json |
| 原生导出 | Blockbench Java Block 原生导出与完整物品 JSON 的几何、UV、Display 对照通过 | blockbench-native-export.json、resource-check.json |
| 资源检查 | 14 个 JSON / 元数据、25 个语言键、八种朝向与半桌组合引用通过 | resource-check.json |
| 实际解析器 | Minecraft 1.20.1 BlockModel.fromString 解析完整模型、两个半桌和七种 Display 成功 | minecraft-parser.json |
| 可视检查 | 完整模型与实际装配各八视角、灰模四视角、七种 Display | all-views.png、all-assembled-views.png、all-displays.png、灰模预览 |
| 保存重开 | 相同相机和画布，重开前后 1300×1100 RGBA 像素完全一致 | roundtrip-render-check.json |
| 交易与双格行为 | 一个 Forge GameTest 通过 206 项断言 | runtime-release.log |
| 核心保留 | 原基线 15 个文件中 11 个未变，4 个为双格解析、权限和展示边界而修改 | transaction-core-baseline.json、resource-check.json |
| 正式构建 | build、资源检查和重映射通过；JAR 仅含 Java 17 class 与正式资源 | build-release.log、jar-audit.json |

原生导出按组件名匹配，坐标与 UV 统一至六位小数，归一化默认值和等价角度，不代表文件字节相同。世界模型的连续结构切割与 UV 采用独立解析校验，计算容差 0.000002，用于导出小数舍入。

保存重开检查严格比较解码后的 RGBA，不使用空背景平均误差。完整物品和切割后的双格装配另作比较：少量边缘像素因独立表面采样和深度排序不同，不能宣称两者逐像素完全相同。assembly-render-check.json 保留各视角实际差异，差分图与两组预览供复核；结构、材质和 UV 连续性由独立检查约束。

## 测试范围

交易回归覆盖报价预览、不提前扣款、6 tick 防连点、确认超时、原料不足、背包放不下、组合配方和标签 / NBT、主副手及旁观状态、库存持久化、换价与重载失效、前置取消及后置事件、接口防御性复制、管理员权限和无效配置拒绝。

新增双格测试覆盖四朝向、占位拒绝且不吞物品、一次消耗放下左右两半、各半碰撞不越格、不可推动、仅主半有方块实体、任意半绑定和查询、右半预览后左半确认、共享库存、拆任意半清理整桌且只掉一件、创造拆除零掉落，以及残缺桌拒绝交易。

GameTest 使用独立测试世界和模拟玩家，不等于真实客户端或第三方经济 / 领地集成均通过。正式 JAR 不含测试类、测试标签和空测试结构。API 示例已在实际 Forge 映射类路径下编译，目标 API 的 ResourceLocation 弃用警告不影响 Java 17 构建。

## 本轮造型变化

桌架由未发布的单格候选改为两格宽、一格深；仅桌架和连续表面横向延长，药瓶、卷轴、骷髅、匕首和陶器只重新排位，不横向拉胖。金币状烛底替换为两片相连的薄米白融蜡。此前删除的金币、绿宝石、钥匙及桌面 / 置物板灰斑不恢复。

本轮有主动造型和图集布局变化，不声称其他区域逐像素保持不变。前后对照均标注各图适配画幅，不冒充同尺度照片。装饰删除不限制交易配方使用绿宝石或其他物品。

## 未验证项目

- 未启动 Minecraft 客户端，预览均为 Blockbench 渲染，不是游戏截图。
- 未测实际 draw calls、显存或 FPS，不能据组件数宣称性能提升。
- 未自动部署；旧存档的单格桌不会自动扩展。应先备份、记录报价和库存，再重新摆放完整长桌并绑定。
- 未承诺第三方经济、领地保护、服务器插件和全部资源包兼容；扩展需自行接入权限。

## 复现

在项目根目录使用 Java 17 执行 BUILD.ps1 的 build 和 runGameTestServer 任务。模型由 tools/build-model.py 生成；随后使用 Blockbench 导入、保存、重开并导出固定相机预览。tools/verify-assembly.py、tools/prepare-previews.py、tools/verify-resources.mjs 分别校验装配、图像和资源。最后 tools/package-release.py 审计 JAR 并生成带 SHA-256 清单的独立交付目录，拒绝覆盖已发布版本。
