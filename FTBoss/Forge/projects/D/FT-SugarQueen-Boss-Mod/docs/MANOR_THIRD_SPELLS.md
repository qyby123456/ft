# 庄园第三法术：十位成员 + 麻雀

2026-09-18，Forge 1.20.1 / Iron's Spells 3.16.3。

保留此前每人两招，新增以下 11 个独立法术。庄园共 33 招；与当前非庄园 37 招共存，共 70 招。麻雀是十位庄园成员之外的支援位，本轮额外给一招，不把已有的千羽散华或乘羽轻身当作新增。

| 角色 | 新法术 ID | 实际玩法 | 专属三维表现 |
| --- | --- | --- | --- |
| 柚子 | 星偶巡礼 `effigy_procession` | 4格区域，0.6秒后开始3次伤害与虚弱脉冲 | 三尊悬浮星偶、牵引星线、顶端星核 |
| 蛋蛋 | 烛月蚀魂 `candle_eclipse` | 3.5格区域，1.2秒后收割；吸取实际生命伤害的15%，最多6点 | 双弧蚀月、六烛阵、汇聚魂线 |
| 朵拉 | 棱晶炮阵 `prism_battery` | 4格区域，0.6秒后3轮伤害与发光标记 | 三座三脚棱晶炮台、悬浮箍环、交汇光束 |
| 功底 | 赤刃裁决 `blood_verdict` | 3.5格区域，1秒后重击与4秒护甲撕裂 | 悬空巨剑、十字护手、落地放射裂纹 |
| 柠檬猫 | 柠日焰轮 `citrus_nova` | 4格区域，0.6秒后焰爆、点燃与击退 | 八瓣柠檬焰轮、叶片、火晶冲击环 |
| 青衣 | 碧潮莲华 `tidal_lotus` | 自身4格内友方灭火解毒、满阶回复8点生命，水下呼吸与抗性 | 十瓣水莲、三层涟漪、悬浮水晶 |
| 文玄蓝 | 磁暴天仪 `magnetic_storm` | 4格区域，0.6秒后4次磁暴伤害、轻微牵引与减速 | 三轴旋转天仪、六极磁晶、折线电弧 |
| 折耳根 | 永夜交锋 `nightfall_blades` | 3.5格区域，0.6秒后首次交叉斩，1秒后第二次；施加虚弱 | 四柄环形影刃、暗影圆门、中心交锋 |
| 紫菜 | 炽羽天降 `seraph_descent` | 4格区域，1秒后光枪坠落、伤害、击退与标记 | 羽翼光枪、光环冠、坠落冲击圈 |
| 雪黎 | 霜刻时钟 `winter_clock` | 3.5格区域，0.6秒后3次冰钟脉冲，冻结、伤害，减速逐次增强至III | 十二刻冰钟、逆转指针、六枚冰晶 |
| 麻雀（额外） | 归羽结界 `feather_refuge` | 自身4格内友方获得吸收盾、缓降和生命恢复 | 十根弯曲羽骨穹顶、羽枝、顶端羽冠 |

## 获取和接线

- 全部为原生铁魔法注册法术，不是武器右键替代品；固定为单一满阶，保留原生法力、冷却、学派属性、撰写台及抄写台，不再需要奥术铁砧升级。
- 创造栏提供庄园33种满阶卷轴，各法术一张。击败对应正常 Boss 掉落三张满阶卷轴，各一张；仍排除分身、巨型演出体、NoAI 和终章替身，尊重掉落规则及去重。
- Boss 原有双法术轮换自然扩展为三招，各招独立冷却，沿用现有吟唱、动作互斥与取消逻辑；麻雀仍跳过攻击型法术，只轮换两个辅助法术。
- 新攻击法术锁定释放时的位置，之后不追踪目标；释放后仍有0.6–1.2秒预警。三维攻击形态由服务端真正命中时的 PULSE 包驱动，而不是仅把模型文件放进 jar。
- 玩家、Boss 共用伤害实现；尊重无敌帧、PvP、队友和宠物保护、墙体遮挡与禁疗。施法者死亡、移除、切换变体、离开过远或世界卸载会取消持续结算。不破坏方块、不强行清空无敌帧。
- 每世界持续法术上限64，同一施法者同一法术替换旧实例，每次最多12个目标。客户端仍限制96个活动特效、每帧16000面、每个特效1800面，并按粒子设置、距离、视锥裁剪。

## 资源与维护

- 新增11张独立64×64透明背景法术图标；延用其他线程的确定性像素图标生成方式，无需外部生成服务。
- 每招使用独立128×128法阵贴图及运行时三维几何，具有吟唱、释放、脉冲、旋转和淡出状态。
- 为同时容纳70招，共享图集扩为2048×2048（16×16格，第255格为纯色几何保留格）；Java渲染器、生成器、离线预览和验证器已统一尺寸，避免超过旧64格容量。
- 网络协议升至6，客户端和服务器应同步更新同一 jar。已有法术顺序保留，新11招排在当前59种特效之后。
- 规则在 `ManorSpellRules.java`，新效果在 `ManorThirdSpellEffects.java`，三维网格在 `SpellVisualGeometry.java`，图标源在 `tools/model3d/manor_third_assets.cjs`。

```text
node tools/model3d/gen_manor_spell_assets.cjs <staging-resources> <preview.png>
node tools/model3d/gen_spell_visuals.cjs <staging-fx>
node tools/verification/check_manor_iron_spells.cjs
node tools/verification/check_spell_visuals.cjs <staging-fx>
node tools/verification/check_manor_third_assets.cjs <preview-output> <spell-meshes.json>
gradle build runGameTestServer -I tools/verification/manor_integration_gametest.gradle --offline
gradle checkSpellVisualPackets -I tools/verification/spell_visual_audit.gradle --offline
```

离线图鉴由实际Java网格渲染，不是整合包游戏截图。服务端测试与离线预览不等于完整客户端光影、第一人称遮挡及战斗手感验收。
