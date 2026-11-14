# 法术普通光线审计与独立模型

## 范围

检查全部 72 门已登记法术，以及 Boss 和玩家的实际 `SpellVisuals.beam` 调用路径：24 门仍登记在铁魔法通用光线适配表中的法术，加上 6 门未登记在该表中的横向波纹/扇形斩击。27 门新增独立立体模型；已退出通用光线表的绯星连奏保留 `QizaiRoseStarfallModel`，不再叠加另一份模型；另外 3 门已有庄园独立模型，仅移除重复的普通横线。

本轮只改外观，不改魔力、冷却、技能释放次数、伤害、范围、宠物保护、卷轴、掉落、弹数、追踪和碰撞。祁崽独立飞弹、激光发射器、陨星以及现有庄园模型保留。范围法阵、预警、治疗光点和非直线主题特效不属于“普通光线”，继续保留。

## 新模型

除 27 门射线模型外，另替换斥力放电、紧急重启、耀环斥退的三种通用散射光线，共新增 30 套造型。它们分别在 RELEASE / PULSE / RELEASE 阶段使用三轴斥力陀螺、四臂修复核心、圣光钟冠；原本的电弧/放射线停止生成。

| 法术 ID | 独立造型 |
|---|---|
| capacitor_overload | 四爪电容核心、金属导电支架 |
| static_lattice | 三层方形拘束门、浮动电网晶核 |
| magnetic_storm | 双列磁极、异色极靴与中心磁核 |
| pressure_lance | 五级线圈高压炮芯、三尾翼枪尖 |
| ion_rail | 双轨加速器、四组金属齿与离子弹芯 |
| star_cross | 四刃十字星、前向星晶 |
| frostglass_lance | 长柄冰枪、四枚倒钩冰棱 |
| frost_needles | 错位三针束、六角针箍 |
| camellia_blood_bloom | 八瓣血茶花、后方荆棘 |
| sanguine_suture | 骨白缝合针、针眼与血色缝线 |
| flesh_bloom | 跳动的切面血肉心脏与血管 |
| candle_eclipse | 黑曜日蚀盘、六烛环列 |
| gilded_verdict | 金色裁决剑、刻环与四枚审判星 |
| arcane_fusillade | 青白聚能激光：全程白热芯、螺旋场轨、移动能量箍与终点聚焦环（无子弹模型） |
| effigy_volley | 有头身、四肢与面部的灵偶 |
| feather_fan | 金羽轴、十二片有厚度的羽枝 |
| camellia_sanctuary | 六瓣治疗花、金环与白色十字 |
| camellia_rebirth | 种子、四叶新芽与顶端小花 |
| virulent_harvest | 六段毒镰刀刃与暗色柄 |
| golden_harvest | 三岔金枝、叶片与金果 |
| prism_battery | 三撑架棱镜炮、双端固定环 |
| effigy_procession | 三只带冠灵偶队列 |
| nightfall_blades | 错位双暗刃、悬浮紫晶 |
| seraph_descent | 圣剑、八片银金翼羽与剑环 |
| tidal_edict | 连续曲面浪脊、翻卷的白色浪尖 |
| cinder_breath | 五重双层焰舌、金色喷口 |
| candle_reap | 带蜡烛、烛焰和握环的实体镰刀 |
| repulsor_discharge | 三轴实体陀螺、向外打开的四枚装甲翼 |
| emergency_reboot | 带修复十字的机械核心、四条维修臂 |
| luminous_rebuke | 六尖圣光钟冠、外侧金环 |

`rending_cleave`、`searing_pan`、`four_tides` 已有独立模型，删除 BEAM 阶段多余横线，但保留 RELEASE/PULSE 的完整模型和方向预警。

## 接线与限制

- `SpellBeamModels` 使用纯 Java 实体网格；每门有独立结构，不是同一根光束换颜色。
- 朵拉 `arcane_fusillade` 不再生成弹壳/弹头：共享立体批次绘制贯穿命中段的聚焦场，再叠加青白激光能量层；四次射击、伤害与命中判定不变。
- `rose_starfall` 在全量目录审计中标记为保留已有独立模型；`QizaiRoseStarfallModel` 的天穹镜、移动星核、三层坠星束与玫瑰冲击冠继续工作，不再生成简版重复模型。
- 客户端实际包入口禁止这些阶段再生成铁魔法 TRACE/ZAP/SIPHON；旧透明光线绘制也跳过。
- 两处服务端辅助方法保留视觉包发送，随后跳过旧粒子直线；它们只负责表现，不跳过伤害结算。
- 与庄园模型共用一次支持透明度、仍写深度的绘制批次，避免反复切换缓冲；顶点透明度只在收招末尾生效，光照仍为全亮切面色。
- 射线模型沿真实起点到终点旋转，支持垂直和反向射线。近距离只按真实段长约束自身结构；生命周期不再缩放整套模型或横截面，保持设计尺寸到收招，最后最多 3 tick 用平滑透明度消散。
- 每次视觉包最多 12 tick；循环脉冲独立重启，使用现有世界切换、退出、施法者死亡清理。每帧上限 24/12/6 个射线模型，随粒子质量降低；减少尾部节点而不删除主体。

## 验证

`checkSpellBeamModels` 随 `build` 执行：覆盖 27 门 × 48 帧 × 7 段长 × 2 档质量，共 18,144 帧，检查有限顶点、实际面数、面面积、命中边界、唯一几何结构和过期清理。额外检查绯星连奏仅走自己的专用模型入口。输出 `build/verification/spell-beam-models/preview.png` 和完整 72 行 `spell-visual-audit.csv`。

`SpellBeamModelGameTests` 对真实网络包编码/解码后的模型进行七种朝向变换验证，并用自然服务器 tick 检查全部模型的完整寿命与再次释放。另运行现有 Boss/庄园法术效果、墙体遮挡、友军保护和延迟技能清理回归。预览是实际网格的软件渲染，不是整合包实机截图。

散射模型另覆盖完整 312 个四分之一 tick 采样帧，总计 18,456 帧。现有绯星连奏专项检查单独保留，不将它误记为新模型。

## 基线回归说明

扩展战斗回归中的以下四项失败，在撤回本轮客户端接线与两处服务端粒子线改动的隔离对照版中同样复现，未在本轮混入战斗逻辑修复：

- `manorPlayerOffenseAndSelfBuffs`：`STAR_CROSS` 玩家伤害断言。
- `manorBossDropsBothScrollsOnlyOnce`：变体 0 的三个卷轴掉落断言。
- `realEntityTicksFinishNovaWhilePlayerAttacks`：陨星测试记录 18 次伤害，与测试期待值不符。
- `naturalServerTicksApplyAllThreeCircleHits`：圆阵三次伤害计数断言，记录值在不同运行中不一致。

新增模型、视觉包朝向、完整动画寿命与原生光线替换测试通过。完整日志保存在本轮隔离目录 `_work/spell-beam-models-20260921`。构建守卫照常执行，不使用跳过检查的构建参数；部署前再次比对测试快照、当前源码及四处产物哈希。
