# 验证记录 · 1.0.0

日期：2026-11-01。目标 Minecraft 1.20.1、Forge 47.4.10、Java 17。验证位于本项目独立目录，没有写入既有整合包的 `mods` 或存档。

下文证据路径相对源码根目录；外层交付包的 `验证记录/` 也附带同名报告、预览与最终日志。原始逐角度大图和只读第三方研究缓存不打进交付包。

## 已实际执行

| 检查 | 结果 | 证据位置 |
| --- | --- | --- |
| Forge 编译、`build`、`reobfJar` | 通过，正式 JAR 已重映射 | `model_sources/qa/build-release.log` |
| 独立 Forge GameTest 服务端 | 1 个必需测试通过，内部 130 项断言 | `model_sources/qa/runtime-release.log` |
| 实际 Minecraft 模型反序列化 | `BlockModel.fromString` 解析方块和物品模型，57 组件、290 面、7 Display | `model_sources/qa/minecraft-parser.json` |
| JSON、纹理、双语键及资源引用 | 12 份 JSON/元数据、25 个语言键，检查通过 | `model_sources/qa/resource-check.json` |
| Blockbench 原生 Java 导出对照 | 几何、声明面、UV、纹理引用、Display 等价 | 同上及 `blockbench-native-export.json` |
| 结构与依赖审计 | 无结构错误/警告，依赖齐全，预算通过 | `model-audit.json`、`cost-audit.json` |
| Blockbench 重开 | Java 方块格式、57 组件、1 张 128×128 纹理 | `roundtrip.json` |
| 重开前后固定斜视像素对照 | 1300×1100 RGBA 字节完全一致 | `roundtrip-render-check.json` |
| 固定八视角、七种 Display | 已截图并人工查看 | `all-views.png`、`all-displays.png` |
| UV 不透明区域 | 290 个声明面的 UV 采样全部不透明；图集留白透明未用于可见面 | `roundtrip-render-check.json` |
| API 示例 | Java 17 编译通过，使用公开 API 而非内部类 | `examples/java/DungeonShopIntegration.java` |
| 最终 JAR | CRC、必要文件、资源字节一致、Java 17 字节码、无测试内容 | `model_sources/qa/jar-audit.json` |

原生导出对照不是 JSON 字节一致：坐标按六位小数归一化，零旋转/省略默认值及等价角度环绕按同义处理。实际 Minecraft 1.20.1 解析使用交付的运行资源，不将当前 Blockbench 导出里的新版本标记当作 1.20.1 模型规范。技能的 `java-legacy` 审计只作为局部预检，目标版本依据仍为实际 Minecraft 解析器。

## 结算回归覆盖

真实 Forge 启动、物品/方块注册、内置数据交易加载及自定义物品标签；公开报价和桌子 API；预览不扣款、同 tick 重复点击、6 tick 最小间隔、副手不重复触发；普通购买、有限库存和最后一份商品竞争；付款不足、满背包、扣款腾格、部分放得下的多项奖励整体拒绝；副手材料、盔甲栏排除、创造模式正常付款；命名材料保护、精确 NBT、明确放行任意 NBT；未知物品、空交易、小数数量、错误 SNBT/字段拒绝；标签混合材料和重叠条件最大流；130 个商品自动分组；换价/改库存/超时废除确认；库存存档、客户端显示数据包、访问器防御性复制；购买前取消、防重入和一次成功通知；远距离/旁观者/异步调用拒绝；管理员命令权限；未绑定桌子不能补货；移除代码报价后禁售。

GameTest 使用两个 `ServerPlayer` 测试对象及空网络发送器，没有真实客户端连接。库存竞争按服务器线程顺序执行，不是网络压力/多人联机测试。`build` 中普通 Gradle `test` 不等于 GameTest，130 项断言来自单独执行的 `runGameTestServer` 日志。

## 模型预算与来源

预算生成前确定为 ≤100 组件、≤600 面、单张 128×128 图集。实际为 57 编辑/导出组件、290 声明面、1 张纹理、16384 像素、0 透明壳、0 动画展开副本。52 个被另一不透明轴对齐实体完整遮住的面保守移除；旋转部件没有参与此剔除，保留逐面证据 `hidden-face-proofs.json`。

每桌运行时最多 6 个商品预览实例，沿用物品自己的模型，不包含在静态桌体 57 组件中。未计算其他模组商品模型的复杂度，不把 65536 字节基础 RGBA 估计当作实测显存或 FPS。

只读研究了 Handcrafted `desk.json` 与 Goety `template_altar.json` 的体块和真实 UV；对应参考 PNG 均为 64×64。交付纹理/几何为本项目原创，第三方 PNG、解包内容不进入 JAR 或交付包。装饰账本/封蜡不是可交易物品储存。

## 未验证与运营建议

- 未启动 Minecraft 客户端：没有实测整合包内光照、实际物品悬浮位置、第一人称及多人网络同步；Blockbench 截图不是游戏截图。
- 未实测第三方领地/经济/自定义 Ingredient，未自动接入 FT 难度、挑战等级或余额支付。
- 未做 FPS、批量商店、网络恶意流量或进程崩溃恢复压力测试。有限库存依赖常规世界保存，不是跨文件数据库事务。
- 玩家持有物品并潜行时，原版可能优先方块物品行为；使用普通右键核价。长按右键可能重复交互，不等同松开再按检测。
- 重要商店请保护方块、备份世界；先在测试世界用普通/带 NBT 材料、满背包与两名玩家做客户端验收，再投入运营。

代码 API、JSON 示例和操作文档随源码交付；验收报告只描述已经完成的检查，不代替上述未执行的游戏内测试。
