# 烛影·末镰：充能骨骼与手持光效

2026-09-18，作用于玩家掉落武器 `ftboss:dandan_scythe`。保留原设计 111 个几何块、纹理、基础属性、技能伤害和冷却；不替换蛋蛋 Boss 的 `minecraft:end_rod{CustomModelData:91601}` 纯外观装备。

## 操作与外观

| 状态 | 模型 | 光效 |
| --- | --- | --- |
| 静止持握 | 原始弯月轮廓 | 烛芯与锋口低亮，不堆积尾迹 |
| 普通挥砍 | 柄部带动镰首，刃片小幅展开 | 暖白主残影 + 淡紫副残影，四层淡出 |
| 按住右键 0.6 秒 | 三级刃片联动张开，前后烛芯向外分离并对向转动，反刃抬起，尾座伸出 | 柄部光点向镰首汇聚，烛芯双环增亮、旋转 |
| 蓄力完成 | 自动横斩，潜行时为安全跃迁；仍按原规则消耗耐久和冷却 | 释放光环 + 原有世界横斩 / 跃迁效果 |
| 释放后 | 反冲，前 8 tick 保持展开，至第 28 tick 完全回收 | 残余光芒减弱，刃片逐段归位 |
| 提前松开 | 约 6 tick 平滑收拢 | 无成功释放闪光，不扣耐久 / 冷却、不造成技能伤害 |

潜行状态在实际释放时决定技能类型。技能蓄力期间的移动减速沿用 Minecraft 持续使用物品行为。
原有攻击伤害、冷却、范围、烛痕归属、队友 / 驯服宠物 / 创造旁观保护、传送碰撞和取消钩子均保留。

## 实现

- `DandanScytheMotionRules` 是姿态时序与骨骼参数的唯一真源；所有动画基于游戏 tick 和插值，不按渲染调用累计时间。
- Java 静态模型按原几何划分为九组，由 `ModelPart` 执行父子矩阵链，不转换 UV，不引入 GeckoLib 坐标换算误差。
- `DandanScytheModel` 使用实际持有者解析物品模型。仅手持视角返回自定义渲染标记，`DandanScytheRenderer` 绘制分组；GUI / 掉落 / 展示框仍返回原模型。
- 这条链路显式覆写 `isCustomRenderer()`，因此不需要把原模型改成 `builtin/entity`；不只注册一个永远不会被调用的 BEWLR。
- 渲染前状态按 stack 匹配并立即清理 `ThreadLocal`；释放恢复按玩家 UUID、槽位及除耐久外的物品数据匹配，避免背包同步、他人武器、不同槽位串动画。
- 追加特效 kind 4/5 表示横斩 / 跃迁成功，仅携带既有包格式的施法者实体编号；只在成功施放时发包，观察者和施法者都会收到。包字段未改变，客户端 / 服务端仍应安装同版模组。
- 发光层使用深度测试、颜色写入且不写深度，避免透明残影遮挡实体；最多四层残影、八个聚光点，持有者状态上限 64。
- 暖白与淡紫不依赖光影，自发光不等于动态照亮世界方块。

## 生成与检查

```text
node tools/model3d/gen_dandan_scythe.cjs <临时 assets 目录>
node tools/verification/check_dandan_motion.cjs
node tools/verification/check_dandan_motion_state.cjs
node tools/verification/dandan_scythe_wiring.cjs
gradle build checkDandanMotion -I tools/verification/dandan_motion_checks.gradle --offline
```

`gen_dandan_scythe.cjs` 同时调用 `gen_dandan_rig.cjs`，原模型或贴图更新后必须重生成骨骼分组；旧六份资源的内容不因本次重做改变。
状态机回归用简化客户端世界替身执行真实 `DandanScytheMotion`，验证多人隔离、取消、槽位切换、耐久同步、死亡、离线和恢复超时。

`DandanScytheMotionCheck` 可传出参 JSON 路径，导出真实 Java 的九骨骼变换；交给 `preview_dandan_motion.cjs <poses.json> <输出目录>` 生成贴图姿态联系图。
该图为**离线姿态验证**，不是 Minecraft 截图，未模拟玩家手臂、光影或全部整合包渲染兼容；实际持握仍需实机检查。

验收建议：普通挥砍、完整蓄力、松开取消、潜行跃迁失败 / 成功、左右手、换槽、两名玩家同时持握、第一 / 第三人称、背包 / 展示框以及 N 键检视。
