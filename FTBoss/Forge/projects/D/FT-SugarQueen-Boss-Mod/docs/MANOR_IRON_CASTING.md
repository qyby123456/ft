# 庄园独立铁魔法与技能替换

庄园11位角色每位3个独立法术，共33个；本表列出前两招，第三招见 `MANOR_THIRD_SPELLS.md`。与非庄园37个共计70个法术，均为单一满阶，独立图标、学派、魔力、咏唱与冷却保持不变。

## 法术目录

| 角色 | 法术一 | 法术二 |
|---|---|---|
| 柚子 | 偶像连星 `ftboss:effigy_volley` | 灵偶守御 `ftboss:effigy_ward` |
| 蛋蛋 | 暮烛收割 `ftboss:candle_reap` | 暮烛囚笼 `ftboss:candle_prison` |
| 朵拉 | 秘银齐射 `ftboss:arcane_fusillade` | 破阵榴弹 `ftboss:breach_shell` |
| 功底 | 黑刃裂甲 `ftboss:rending_cleave` | 赤誓战意 `ftboss:crimson_oath` |
| 柠檬猫 | 炽锅翻焰 `ftboss:searing_pan` | 炉心暖宴 `ftboss:hearth_feast` |
| 青衣 | 四海奔流 `ftboss:four_tides` | 百药甘霖 `ftboss:elixir_rain` |
| 文玄蓝 | 机轮卫星 `ftboss:orbital_gears` | 高压贯星 `ftboss:pressure_lance` |
| 折耳根 | 影缚镇印 `ftboss:shadow_brand` | 虚空回响 `ftboss:void_echo` |
| 紫菜 | 耀环斥退 `ftboss:luminous_rebuke` | 圣环庇所 `ftboss:halo_sanctuary` |
| 麻雀 | 千羽散华 `ftboss:feather_fan` | 乘羽轻身 `ftboss:wingstep` |
| 雪黎 | 凝霜飞针 `ftboss:frost_needles` | 白夜禁疗 `ftboss:whiteout_edict` |

## 卷轴

- 使用铁魔法原生 `irons_spellbooks:scroll` 和 `ISpellContainer`，不是只改名称的普通物品。
- FTBoss 创造页每门启用法术只保留一张满阶卷轴，使用原版铁魔法卷轴外观。
- 玩家或其宠物击败有效庄园 Boss 时，获得该角色三种满阶卷轴；同一死亡不重复发放。
- 迷你分身、巨型分身、无 AI 演出和终幕复制体不产出卷轴；遵守 `doMobLoot`。
- 卷轴保存法术 ID 与等级，使用原生法术容器，支持铁魔法法术书及其原生工作台流程。

## Boss 施法

- 柠檬猫献祭继承期间统一调度自身、继承专属及继承原生铁魔法，单轮上限为 `min(5, 2 + 被献祭的 Boss 个数)`；同种 Boss 分别计数，小怪不增加槽位。每招独立冷却，继承到期恢复原有双施法，详见 `LEMON_MULTICAST.md`。
- 柚子迷你分身例外开放本角色三门专属魔法：伤害为本体对应法术的 35%，每轮单发，正常读条、16 格选敌、施法后间隔 10 秒、同招冷却翻倍；守御改为 4 秒吸收 I 和 3 秒抗性 I。其他分身仍不施法，所有分身仍不掉卷轴；详见 `YOUZI_CLONE_SPELLS.md`。
- 2026-09-19：`ManorSpellCombat` 每轮并行准备两种就绪法术，按两招中较长咏唱的一半同步释放；2026-09-20（v177）起该换算改为 × 2/3（比减半长 1/3），其余不变；不占用近战、寻路或武器技能状态。仅剩一招启用时允许单招回退，其他情况等待第二招就绪。
- 功底的大杀四方、致残打击、无情铁手、诺克萨斯断头台保留独立武器轮换，与铁魔法并行；大杀四方不再被黑刃裂甲替换，也不会在铁魔法忙碌或冷却时被吞掉。武器与法术各自保留原有伤害、前摇、冷却和取消条件。
- 柠檬猫猛火翻锅入口使用炽锅翻焰；玄蓝蒸汽泄压入口使用高压贯星；折耳根暗影突袭入口使用影缚镇印。
- 上述三个替换入口在新法术启用时直接返回，不再启动对应旧伤害计时器；冷却中的技能不会回落旧伤害制造第二次攻击。
- 注册法术未启用或未安装铁魔法时，依赖隔离桥返回原技能路径。
- 两招分别使用独立 `MagicData`，分别预检查、预施法、咏唱 tick、释放和结束回调；完成/取消只清理本次铁魔法提示，不取消正在进行的近战或武器技能。
- Boss 伤害读取独立 `ManorBossSpellProfile`，不再读取玩家卷轴威力。黑刃裂甲及三个替换法术保留原 Boss 数值与关键效果，八位指定角色半径翻倍，详见 `BOSS_COMBAT_BALANCE_20260919.md`。
- 各个法术分别冷却。目标消失、死亡、换目标、距离过远、被墙阻挡、Boss 无 AI 或变体改变会取消尚未释放的施法，并清除动作锁。
- 麻雀保留支援定位，Boss 自动施法不会选取攻击型千羽散华；玩家仍可使用该法术卷轴。
- 朵拉枪械和新长枪、青衣实体三叉戟、玄蓝飞轮、其它现有招式继续保留。

## 安全与验证

- 伤害使用铁魔法 `DamageSources.applyDamage` 和学派伤害源，尊重无敌保护，不强制清除伤害无敌帧。
- 射线和区域效果检查墙体、友军、宠物、创造/旁观玩家及 PvP 设置；不破坏方块。
- 持续区域按世界管理、有数量及寿命上限；施法者移除或失去资格后清理。世界卸载和服务器停止时清空缓存。
- `tools/verification/check_manor_iron_spells.cjs` 检查33个ID、图标、双语说明、卷轴、生命周期及替换入口。
- `tools/verification/manor_integration_gametest.gradle` 运行70法术共存、满阶数值、旧卷轴兼容、原生工作台、玩家效果、技能替换、冷却、取消和全套掉落测试。
- `GongdiParallelCastingTests` 验证功底 AI 在铁魔法中起手大杀四方、四种武器技能与铁魔法双向并行、法术冷却不吞斧技，以及无 AI 时两条通道安全取消。
- 此测试不等于完整整合包内的客户端画面验收。
