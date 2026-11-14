# 柠檬猫手持武器

## 玩家掉落武器

- `ftboss:lemoncat_skillet`（柠爪煎锅）：史诗品质，面板基础攻击 10、攻速 1.4、耐久 1500。
- `ftboss:lemoncat_spatula`（喵厨锅铲）：史诗品质，面板基础攻击 7、攻速 2.0、耐久 1500。
- 两件可用铁锭修复，支持原版剑类附魔；属于独立战斗物品，不承担原厨具的烹饪、铲油功能。
- 玩家击败柠檬猫本体时必掉其中一件，两件各 50%。掉落为全新、未附魔、有正常耐久的武器，不复制 Boss 的八级附魔/无法破坏。
- 掉落接在真正的 `dropCustomDeathLoot`，要求近期玩家参与、开启 `doMobLoot`，排除迷你分身与庄园终章实体。轮换退场、卸载与怜悯清场不掉落；抢夺不增加本专属武器数量。

## 玩家技能

- 煎锅对空右键「猛火翻锅」：前方 110 度扇形，半径 4.5 格、上下高差不超过 2.5 格，造成 12 点可被护甲减免的玩家攻击伤害，点燃 3 秒、击退并轻微抛起。冷却 8 秒，消耗 4 耐久；不破坏地形或掉落物。
- 锅铲对空右键「清香调味」：立即回复 4 点生命，清除中毒与缓慢，获得再生 I 5 秒和速度 I 6 秒。冷却 14 秒，消耗 3 耐久；遵守现有禁疗规则。
- 双持组合（左右手可互换）：右键始终施放翻锅，潜行＋右键施放调味。翻锅变为 16 点伤害、5.5 格、附带缓慢 II 3 秒；调味变为回复 6 点生命、熄火并获得抗火 6 秒。
- 两件武器共享冷却，切手/切换同种副本不能跳过冷却；攻击键仍是普通近战。主动技能只给玩家，不增强 Boss。
- 翻锅需要视线通畅，不穿墙，不攻击队友、已驯服宠物、旁观者、创造玩家或盔甲架；玩家对战遵守服务器 PvP 开关与队伍规则。伤害尊重原有无敌帧，不强制清零。

## 光效

- 玩家实际手持且技能就绪时，猫爪、猫耳内衬与柠檬徽章显示金黄/薄荷绿全亮纹。冷却时恢复普通材质；背包、展示框与地上物品不会借用本地玩家的状态。
- 翻锅：向前展开的金黄弧光、火星、扫击声；双持增加第二道薄荷绿弧光。
- 调味：薄荷绿扩散光环、猫爪印、清香粒子和风铃音；双持增加外圈金环。
- 光效使用原版/Forge 渲染与粒子，不要求 OptiFine 或光影；全亮不等于动态照亮周围方块。
- 独立网络通道 `ftboss:lemoncat_weapons`，协议 1；仅服务端发送，64 格内含施法者本人可见，携带维度，换维度/退出世界清理，最多 48 组、每组 22 tick。

```mcfunction
/give @s ftboss:lemoncat_skillet
/give @s ftboss:lemoncat_spatula
```

## 当前接入

- 柠檬猫 `variant=4` 主手为 `farmersdelight:skillet`，副手为 `kaleidoscope_cookery:kitchen_shovel`，两件写入 `CustomModelData:160416`。
- 两件沿用八级武器附魔、无法破坏、装备不掉落。伤害、攻速、技能、召唤与配方不变。
- `setVariant()` 负责装备，旧存档的 `readAdditionalSaveData()` 也调用此入口，无需修改存档或重新召唤。
- 离开柠檬猫变体时清理带本模型标记的锅铲副手；雪黎仍使用原有煎锅与擀面杖。
- 客户端 `LemonCatWeaponModel` 注册两个附加模型，在烘焙后仅包装这两件厨具的模型选择器。只有 CMD 精确等于 160416 才选择新外观，其余完整委托原模型的 overrides，包括烹饪/沾油状态。
- 不在模组内覆盖其他模组的 JSON，不依赖模组资源加载优先级；Boss 外观继续保留原 CMD 厨具，玩家掉落使用上述独立物品。无需新增厨具模组编译依赖，服务器端不加载客户端渲染类。
- 模型与贴图内置于 JAR。安装内置版后不需要启用 `FT-LemonCat-Weapons-v1` 独立资源包。

## 维护

权威美术源为 `art/lemoncat_weapons/LemonCat_Citrus_Skillet.bbmodel` 与 `LemonCat_Paw_Spatula.bbmodel`，均内嵌 128×128 贴图。
煎锅 101 个方块、8 组；锅铲 40 个方块、7 组。预览和四视图在同一目录。

```powershell
node tools/model3d/gen_lemoncat_weapons.cjs
node tools/model3d/gen_lemoncat_player_assets.cjs
node tools/model3d/verify_lemoncat_weapons.cjs
node tools/model3d/lint_java_model.cjs src/main/resources/assets/ftboss/models/item/lemoncat/skillet.json src/main/resources/assets/ftboss/models/item/lemoncat/kitchen_shovel.json
```

生成器仅生成这两件武器的模型与 PNG，支持可选输出目录。不要运行全武器生成器来更新本套模型。

自动检查（JDK 17、项目既有 Gradle 8.8）：

```powershell
gradle -I tools/verification/lemoncat_checks.gradle build LemonCatWeaponCheck LemonCatPacketCheck LemonCatVisualCheck --offline --console=plain --no-daemon
```

检查覆盖掉落条件、双持控制、扇形边界、网络序列化，以及实际光环网格的向上绕序、加法混合渐隐和全亮顶点。自动检查不替代实机交互验收。

## 实机验收

重启客户端后查看柠檬猫主副手，检查闲置、攻击、施法及换场；查看雪黎和普通厨具未变。
日志 `[FTBoss][LemonCat] weapon models CMD=160416 main=... off=...` 用于确认实际装备。

玩家武器验收：分别单持释放技能，再互换左右手双持，验证右键翻锅、潜行右键调味和共享冷却；在暗处确认亮纹随冷却熄灭/恢复。让第二名玩家在 64 格内观察技能光效。测试击败本体掉一件、迷你分身/终章不掉、`doMobLoot=false` 不掉，并检查队友/宠物/墙后目标不被翻锅命中。随机两件各 50% 是概率规则，不保证每两次各掉一件。

```mcfunction
/give @s farmersdelight:skillet{CustomModelData:160416}
/give @s kaleidoscope_cookery:kitchen_shovel{CustomModelData:160416}
```

本套是静态武器外观，特制 CMD 不展示烹饪或沾油叠层。离线预览不是游戏截图，玩家持握/Boss 动画仍需实机确认。
