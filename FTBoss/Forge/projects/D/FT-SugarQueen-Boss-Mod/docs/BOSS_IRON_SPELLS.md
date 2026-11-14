# 非庄园 Boss 独立铁魔法法术：首发七门

适配 Iron's Spells 'n Spellbooks `1.20.1-3.16.3`、Forge `47.4.10`。用户提供的 jar、工程编译依赖和 beta3 实例的铁魔法 jar SHA256 均为 `54B5AAA52887C38F570FBD820288171234D8541C17A5E953F9271189FDD480FB`。

## 范围与法术

只为下列七种非庄园 Boss 增加各自独立的注册法术。不为庄园成员、benger、屠夫、鼠群或其他类型的小怪添加本套技能。

本文列出七门主法术。当前非庄园共有37门法术、37种单一满阶卷轴：沙耶7门，其余六位各5门。完整扩展见 `BOSS_EXPANSION.md`，当前满阶规则见 `FULL_POWER_BOSS_SPELLS.md`。

| Boss | 独立法术 ID | 名称 / 学派 | 满阶命中伤害 | 满阶魔力 / 冷却 | 独立效果 |
|---|---|---|---:|---|---|
| 糖霜女皇 | `ftboss:crystal_coronation` | 糖霜加冕 / 冰 | 34 | 120 / 18 秒 | 4 格晶冠爆发，缓慢 III 4 秒，积累冰冻 |
| 沙耶 | `ftboss:camellia_blood_bloom` | 血茶绽放 / 自然 | 30 | 125 / 20 秒 | 4 格花心，缓慢 IV 3 秒；实际伤害的 15% 转为治疗，每次上限 6 生命 |
| 酒桶王 | `ftboss:vintage_vortex` | 陈酿回旋 / 唤魔 | 32 | 120 / 18 秒 | 4 格琥珀酒涡，向内牵引；虚弱、反胃各 5 秒 |
| 故障机器人 | `ftboss:capacitor_overload` | 电容过载 / 雷电 | 34 | 130 / 20 秒 | 连锁电弧，最多 5 个不同敌人；每跳 6 格，伤害乘 0.8 |
| 答辩超人 | `ftboss:prismatic_blight` | 彩秽孢潮 / 自然 | 每次 24 | 140 / 24 秒 | 4 格孢域，第 0、1、2 秒三次脉冲，各附中毒 4 秒 |
| 苹果爵士 | `ftboss:gilded_verdict` | 金果裁决 / 神圣 | 36 | 135 / 20 秒 | 三束金光穿透，每敌只命中一次；发光 5 秒，自身伤害吸收 I 4 秒 |
| 潮宴鱼王·沧鳞 | `ftboss:tidal_edict` | 潮庭敕令 / 冰 | 32 | 125 / 18 秒 | 前方 12 格扇形潮波，击退、缓慢 II 3 秒、灭火，不放置水方块 |

每门法术只保留单一满阶，固定传奇品质；下表伤害和魔力均已提升至满阶，取消逐级成长。蓄力32 tick，除潮波外瞄准距离24格；电弧最多命中5个目标。

表中为基础伤害，不是最终扣血，仍经过学派/装备法强、抗性及既有伤害事件。Boss 同样使用满阶法术，不重复乘伤害倍率。原生配置仍可控制启用、冷却、倍率和制作许可，但不能重新开启等级成长。

## 卷轴、学习与制作

新增的不是改名纸张：全部使用原生 `irons_spellbooks:scroll` 与 `ISpellContainer`，每张携带不同的 `ftboss:` 法术 ID 和等级。

1. **Boss 掉落**：有效击杀对应本体后，其全部启用法术各掉落一张满阶原生卷轴。保留原战利品、玩家归属、复活取消、分身排除和防重复规则。
2. **学习**：在铁魔法抄写台放入法术书和卷轴，选择空法术槽抄写；卷轴消耗，法术写入原生法术书。装备书后使用铁魔法原有施法操作。
3. **制作**：原生卷轴锻造台使用纸、传奇墨水和对应学派媒介。产物即为满阶，奥术铁砧不再提供升级结果。
4. **直接用卷轴**：沿用铁魔法原生一次性卷轴施法逻辑。
5. **创造获取**：非庄园37门法术各保留一张满阶卷轴，不再列出五档等级。

本版本学派媒介：冰系 `irons_spellbooks:frozen_bone`；自然系 `minecraft:poisonous_potato`；唤魔系 `minecraft:emerald`；雷电系 `irons_spellbooks:lightning_bottle`；神圣系 `irons_spellbooks:divine_pearl`。实际接受的材料由铁魔法的学派标签控制，兼容其他数据包扩展。

每门法术有独立 64×64 图标、中英文名字、效果说明及指南文本。图标通过原生 `textures/gui/spell_icons/<法术名>.png` 路径显示，不修改铁魔法原文件。

## Boss 使用及保护

- 有合法目标后开始计时，使用独立蓄力和冷却；当前并行调整后的 Boss 节奏以 `NON_MANOR_SPELL_PACING.md` 为准，玩家法术冷却仍按本表。原有 Boss 技能保留。
- 已有铁魔法施法实体的女皇、沙耶和酒桶王调用原生 `IMagicEntity.initiateCastSpell`；其余 Boss 使用单体独立蓄力状态，再调用注册法术的 `onCast`。
- 地点法术在蓄力开始时锁定落点并显示学派色预警，可走出范围躲避；玩家版同样锁定开始瞄准位置。金光与潮波沿施法者朝锁定点的方向释放。
- 故障机器人不在已有动作锁期间启动新施法；非魔法 Boss 的目标丢失、换目标、无 AI、死亡、离开射程或被墙遮挡会取消待释放技能。
- 不伤害施法者、同队成员、自己的宠物及受保护玩家；Boss 不用本套伤害攻击其他 FTBoss 实体。尊重 PvP 设置，排除创造、旁观和盔甲架。
- 不破坏方块、物品，不改变地形。视线检测阻止穿墙伤害。孢域每位施法者最多一片、每维度最多 64 片；施法者失效、区块不可用、维度卸载或停服时清理。
- 本套法术不混入普通随机战利品池；Boss 掉落与原生卷轴制作是主要生存获取途径。

## 开发与验证

- 实现：`integ/spells/BossIronSpells`、`BossSignatureSpell`、`BossSpellEffects`、`BossSpellCombat`、`BossSpellDrops` 和 `util/BossSpellRules`。
- 图标生成：`node tools/model3d/gen_boss_spell_icons.cjs <assets/ftboss 输出目录> [预览图.png]`。
- 规则检查：编译并运行 `tools/verification/BossSpellRulesCheck.java`，62,687 项断言通过。
- 资源接线：`node tools/verification/check_boss_iron_spells.cjs <临时输出目录>`，158 项断言通过。
- 实际 Forge GameTest：隔离工程使用 `tools/verification/boss_spell_gametest.gradle`，配套五个真实前置 jar 放入 `smoke-libs`，运行 `gradle -I tools/verification/boss_spell_gametest.gradle runGameTestServer`。测试世界仅在隔离工程的 `smoke-run`，不打开玩家存档。
- 验证入口见 `FULL_POWER_BOSS_SPELLS.md`：注册、旧卷轴兼容、满阶掉落、原生锻造/抄录及拒绝继续升级。旧版本验收记录保留在工作区备份，不代表当前分级规则。
- 测试服务器使用官方映射的真实模组依赖，开启 Mixin 引用映射重映射；以数据包同步事件完成铁魔法原生配置初始化，用 Forge FakePlayer 避免 GameTest 原版无网络通道模拟登录的问题。没有跳过或替换法术与菜单实现。
- 最小测试环境没有整合包的全部外部合成材料，因此旧召唤物配方出现缺少材料的解析提示，与本次法术无关，未修改那些配方。
- 游戏客户端视觉、实际完整 Boss 战斗节奏及多人客户端同步仍需在 beta3 实测；服务器 GameTest 不代替这些验收。

更新后完全退出并重启 beta3。仅更新 `qys-bosses.jar`，不替换用户提供的铁魔法 jar，也不更改其配置或按键。
