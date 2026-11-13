# 1.1.0 验证记录与边界

## 验证对象

Minecraft 1.20.1、Forge 47.4.10、Java 17，mod ID `ftdungeontrading`。独立重做工程与 1.0.0 分开保存；本轮不改现有整合包的 mods、配置或存档，不自动启动 Minecraft 客户端。

模型参照本轮提供的交易桌概念图重新制作。Handcrafted、Another Furniture、Let's Do Furniture 的本地桌子 JSON 与真实纹理仅用于研究承重划分、父模型和 UV 方式，未复制其几何或分发其贴图。`reference-study.json` 记录技术文件事实，不代表第三方美术资产可再分发。

## 结果

| 层次 | 结果 | 证据 |
| --- | --- | --- |
| 编辑工程 | 57 个立方体/片组件，单图集，保存后重开正常 | `roundtrip.json`、可编辑 bbmodel |
| 运行几何 | 57 组件、302 声明面，14 个合法 Y 轴旋转组件，无反尺寸 | `model-report.json`、`model-audit.json` |
| 包围范围 | 包含旋转后的全部顶点，最小 `[0.42,0,0.42]`、最大 `[15.58,15.85,15.58]`，在一格内 | `roundtrip-render-check.json` |
| 图集与透明 | 128×128、16,384 像素；298 个不透明面 UV 全为实心；两片蛛网共四面使用二值 cutout，无半透明壳 | `model-audit.json`、`roundtrip-render-check.json` |
| 隐藏面 | 只在轴对齐、不透明且完整遮挡条件下删除 32 面；旋转件和蛛网不当作实心遮挡体 | `hidden-face-proofs.json` |
| 成本预算 | ≤64 组件、≤352 面、≤16,384 纹理像素，全部通过 | `budget.json`、`cost-audit.json` |
| 原生导出 | Blockbench Java Block 原生导出的几何、UV、Display 与运行 JSON 对照一致 | `blockbench-native-export.json`、`resource-check.json` |
| 资源 | 12 个 JSON/元数据、25 个语言键，模型/贴图/交易定义引用通过 | `resource-check.json` |
| 实际解析器 | Minecraft 1.20.1 `BlockModel.fromString` 解析 57 元素 / 302 面 / 七种 Display 成功 | `minecraft-parser.json` |
| 可视检查 | 正、背、左右、上下、前斜、后斜八视角和七种 Display 已查看 | `all-views.png`、`all-displays.png` |
| 保存重开 | 同相机、同画布重开渲染 1300×1100 RGBA 像素完全一致 | `roundtrip-render-check.json` |
| 交易回归 | 一个 Forge GameTest 内通过 134 项断言 | `runtime-release.log` |
| 核心保留 | 15 个未修改交易核心文件 SHA-256 与 1.0.0 一致 | `transaction-core-baseline.json`、`resource-check.json` |
| 正式构建 | `build` 和重映射通过，JAR 只含 Java 17 class 与正式资源 | `build-release.log`、`jar-audit.json` |

原生导出比较按组件名匹配，允许分组导致的顺序变化；坐标和 UV 统一到六位小数，归一化默认值和等价角度，不是文件字节相同。保存重开渲染则直接比较解码后 RGBA 像素，不用空背景平均误差放宽标准。

## 交易测试范围

覆盖报价预览、不提前扣款、6 tick 防连点、确认超时、原料不足、背包放不下、组合配方与标签/NBT、主手/副手及旁观状态限制、库存与持久化、换价与重载失效、重复交互、前置取消及后置事件、接口防御性复制、管理员命令权限、无效配置拒绝。额外四项验证重做桌子的默认朝向、碰撞高度、落地和一格占地。

GameTest 使用独立测试世界和模拟玩家连接；成功不等于真实客户端、多人联机和第三方经济/领地集成全部验证。正式 JAR 不含 `TradingRuntimeTest`、测试标签或空测试结构。API 示例在实际 Forge 映射类路径下编译通过；目标版本中的 ResourceLocation 弃用警告不影响 Java 17 构建。

## 像素与体块调整

从未交付的 84 组件 / 452 面 / 256² 图集候选改为 57 组件 / 302 面 / 128² 图集。27 个组件的精简明细见 `REMODEL-DESIGN.md`。材质重新画大色块，不是对旧 PNG 模糊或缩放；钥匙改为桌面贴图，少量瓶口、卷轴、硬币等细节的剪影主动改变。`像素与体块优化对照.png` 展示同相机前后对照，不声称与细像素候选外观完全不变。

## 未验证项目

- 未启动 Minecraft 客户端；模型预览均为 Blockbench 渲染，不是游戏截图。
- 未测实际 draw calls、显存总量、多人网络、着色器效果或 FPS；组件/面数降低不直接推导游戏帧率提升。
- 静态模型成本不包含运行时最多六个动态商品 ItemStack 的第三方模型成本；无额外 shader、动画轨道或发光前置。
- 当前没有接通 FT 难度商品规则、真实卖家容器或外部货币事务，只保留既有公开 API 和事件扩展入口。

## 重建与复查

在解压后的源码目录执行：

```powershell
python tools/build-model.py
node tools/verify-resources.mjs
.\BUILD.ps1 -Task build -Offline
.\BUILD.ps1 -Task runGameTestServer -Offline
```

Python 需要 Pillow。`build-model.py` 重写生成资源；手改模型后不要直接运行覆盖。若改变模型几何，需重新导入 Blockbench、保存重开、导出 `model_sources/qa/blockbench-native-export.json` 并更新多视角记录；不是仅改统计文件使检查通过。生成的原生工程和 PNG 验证记录随包附带。GameTest 不以客户端渲染为前提。
