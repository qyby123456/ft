# 附录：召唤物 / Boss 掉落 / 音乐物品 / 交易

本节只收录与 Boss 直接相关的**召唤物、掉落、遗物、音乐唱片、交易入口**；Boss 本体技能与血量见各 Boss 章节。时间统一 tick（1 秒 = 20 tick），距离统一「格」。下列数值全部取自当前源码，出处写在行内。

## 一、Boss 召唤物总表

所有玩家可见的召唤入口都要先过 `SummonGate`：`OVERWORLD_ONLY` = true（`boss/SummonGate.java:25`），白名单只有 `minecraft:overworld`，其它维度（下界 / 末地 / 模组维度）**中止召唤且不消耗召唤物**（`boss/SummonGate.java:30-60`）。

| 召唤物 id | 召唤的 Boss | 生成位置（仅主世界） | 消耗 | 出处与备注 |
|---|---|---|---|---|
| `ftboss:sugar_queen_crystal`（16 个一组, RARE） | 糖霜女皇（姜饼王）`ftboss:sugar_queen` | 视线前方 3 格，再抬高 2 格（`setNoGravity(true)`） | 消耗 1，创造模式也消耗 | `item/SugarQueenCrystalItem.java:22,29`；本体在战时改为召唤**克隆体**（`boss/SugarQueenBoss.java:121,126-136`） |
| `ftboss:meat_heart`（16, RARE） | 屠夫 `ftboss:meat_mountain` | 视线前方 2.5 格 | 消耗 1，创造模式也消耗 | `item/MeatHeartItem.java:22,30`；本体在战时改为克隆体（`boss/MeatBoss.java:131-135,148-150`） |
| `ftboss:tea_seed`（16, EPIC） | 沙耶（茶树古树）`ftboss:tea_tree` | 视线前方 3 格 | 消耗 1，创造模式也消耗 | `item/TeaSeedItem.java:25,36-49` |
| `ftboss:wine_charm`（16, EPIC） | 酒魔·酒桶王 `ftboss:wine_barrel` | 视线前方 2.5 格 | 消耗 1，创造模式也消耗 | `item/WineCharmItem.java:25,36-49` |
| `ftboss:rat_bait`（16, UNCOMMON） | 鼠群 `ftboss:rat_boss` | 视线前方 2.5 格 | 消耗 1，创造模式也消耗 | `item/RatSwarmBaitItem.java:25,36-49` |
| `ftboss:defect_core`（16, EPIC） | 故障机器人 `ftboss:defect_core` | 视线前方 4 格，抬高 2.4 格 | 消耗 1，创造模式也消耗 | `item/DefectCoreItem.java:30,42-48`；**无单例闸门**，可连续召唤多只 |
| `ftboss:compost_summon`（1 个一组） | 炫彩答辩超人 `ftboss:compost_boss` | 面朝方向 4 格 | 生存消耗 1，创造不消耗 | `item/CompostSummonItem.java:29-41` |
| `ftboss:forbidden_apple`（1） | 苹果爵士·蜜脆 `ftboss:apple_boss` | 面朝方向 4 格 | 生存消耗 1，创造不消耗 | `item/ForbiddenAppleItem.java:29-48` |
| 【禁果配方】8 种不同苹果食品 + 金币锅 | 3×3 有序：上排蜜饯苹果 / 农夫乐事苹果派切片 / 苹果纸杯蛋糕；中排蜜渍苹果 / 金币锅 / 整合包苹果果酱；下排整合包苹果果汁 / 葡园酒香苹果泥 / 丰饶食记苹果炖；每格 1 份，产出 1 个禁果 | 允许整体左右镜像，不可任意换位或重复替代 | 使用整合包保留的材料，避免 KubeJS 清理整条配方 | `data/ftboss/recipes/forbidden_apple.json`；`docs/FORBIDDEN_APPLE_RECIPE.md` |
| `ftboss:benger_whistle`（1） | 边牧犬王 benger `ftboss:benger` | 面朝方向 4 格 | 生存消耗 1，创造不消耗 | `item/BengerWhistleItem.java:39-52` |
| `ftboss:tide_banquet`（1, EPIC） | 潮宴鱼王 `ftboss:tide_fish_boss` | 视线前方 5 格，Y 依次试 0 / +1 / -1 / +2 / -2 | 生存消耗 1，创造不消耗；使用后冷却 40 tick | `item/TideBanquetItem.java:29,36-49`；5 个高度都放不下则失败且不消耗 |
| `ftboss:easter_egg_*`（11 个刷怪蛋） | 隐藏 Boss（庄园十一人）`ftboss:easter_boss` | 点击的方块处或其点击面外侧 | 消耗 1，创造模式也消耗 | `item/EasterSpawnEggItem.java:57-99`；本维度已有庄园挑战或入场演出时拒绝召唤 |
| `ftboss:benger_spawn_egg` | 边牧犬王 benger | 原版刷怪蛋行为 | 原版规则 | `item/OverworldBossSpawnEggItem.java:28-38`，同样仅主世界可用 |

十一个隐藏 Boss 刷怪蛋与 variant 的对应（`item/FTBossItems.java:202-216`、`entity/EasterBossEntity.java:99-116,244-256`）：

- `ftboss:easter_egg_youzi` = variant 0 柚子；`ftboss:easter_egg_dandan` = 1 蛋蛋；`ftboss:easter_egg_duola` = 2 朵拉；`ftboss:easter_egg_gongdi` = 3 功底；`ftboss:easter_egg_ningmengmao` = 4 柠檬猫。
- `ftboss:easter_egg_qingyi` = 5 青衣；`ftboss:easter_egg_wenxuanlan` = 6 文玄蓝；`ftboss:easter_egg_zheergen` = 7 折耳根；`ftboss:easter_egg_zicai` = 8 紫菜；`ftboss:easter_egg_xueli` = 10 雪黎；`ftboss:easter_egg_qizai` = 11 祁崽（2026-09-20 新增）。
- 9 号麻雀 `SPARROW_VARIANT` 是支援位，不发刷怪蛋。

## 二、召唤限制与连死怜悯（离场）

| 规则 | 数值 | 出处 |
|---|---|---|
| 允许召唤的维度 | 仅 `minecraft:overworld` | `boss/SummonGate.java:25-33` |
| 隐藏 Boss 正式入口 | `/ftboss challenge`（先由苹果爵士「头衔共鸣」蜕变弹出询问） | `boss/HiddenBossChallenge.java:184-194`、`entity/AppleBossEntity.java:708-714` |
| 挑战确认流程 | 两次确认、只有触发者能选、每一步 60 秒（1200 tick）不选即放弃、触发者中途离线则作废 | `boss/HiddenBossChallenge.java:59,156-163,207-228` |
| 挑战播报半径 | 96 格 | `boss/HiddenBossChallenge.java:57` |
| 连死怜悯上限 `LIMIT` | 同一 Boss **实例**连续死亡 5 次 | `boss/ai/BossMercy.java:44` |
| 连死计数过期 `STREAK_TIMEOUT` | 12000 tick（`20 × 60 × 10`，即 10 分钟未再死亡即归零） | `boss/ai/BossMercyData.java:34` |
| 威胁记忆 `THREAT_MEMORY` | 200 tick（10 秒） | `boss/ai/BossMercy.java:49` |
| 归因半径 | 威胁记忆命中 48 格；兜底取 32 格内最近的 Boss；召唤物击杀时取玩家 64 格内最近的 Boss | `boss/ai/BossMercy.java:46,83,197,208` |
| 怜悯离场清理范围 | 本体 + 其召唤物（故障机器人的充能球按归属清；小老鼠 64 格内；小酒桶 / 姜饼小怪 / 茶芽 / 蛆 48 格内） | `boss/ai/BossMercy.java:288-302` |
| 怜悯离场代价 | 直接 `discard()`，**不给任何掉落与经验** | `boss/ai/BossMercy.java:24-26,281-323` |
| 隐藏 Boss 豁免 | 庄园十一人（`EasterBossEntity`）不参与连死怜悯 | `boss/ai/BossMercy.java:106-115` |
| 击败清零 | Boss 真死时清零 96 格内玩家的该实例计数 | `boss/ai/BossMercy.java:150-155` |

## 三、Boss 石像掉落

石像由中央钩子 `BossMercy.giveStatue` 发放，**不要求玩家击杀**，Boss 真死即掉（`boss/ai/BossMercy.java:140-143,329-360`）。糖霜女皇与屠夫的死亡流程里还会各自再放 1 座同款石像（`boss/SugarQueenBoss.java:552-553`、`boss/MeatBoss.java:433-434`），所以这两只实际掉 2 座。石像是 2 格高双半格方块，放下时按「上色档位」显示对应 Boss 皮肤；手持**对应 Boss 的召唤物**左键可在「上色 / 白膜」间切换（`boss/BossHandler.java:141-196`）。

| Boss | 实体 id | 石像物品 id | 上色档位 |
|---|---|---|---|
| 糖霜女皇 | `ftboss:sugar_queen` | `ftboss:statue_sugar_queen` | 1 |
| 屠夫 | `ftboss:meat_mountain` | `ftboss:statue_butcher` | 2 |
| 沙耶（茶树古树） | `ftboss:tea_tree` | `ftboss:statue_saya` | 3 |
| 酒魔·酒桶王 | `ftboss:wine_barrel` | `ftboss:statue_wine_barrel` | 4 |
| 鼠群 | `ftboss:rat_boss` | `ftboss:statue_rat_swarm` | 5 |
| 故障机器人 | `ftboss:defect_core` | `ftboss:statue_defect` | 6 |
| 炫彩答辩超人 | `ftboss:compost_boss` | `ftboss:statue_compost` | 7 |
| 苹果爵士·蜜脆 | `ftboss:apple_boss` | `ftboss:statue_apple` | 8 |
| 潮宴鱼王 | `ftboss:tide_fish_boss` | `ftboss:statue_tide_fish` | 9 |
| 边牧犬王 benger | `ftboss:benger` | — | — |
| 隐藏 Boss（庄园十一人） | `ftboss:easter_boss` | — | — |

档位与方块注册见 `block/FTBlocks.java:40-61`。

## 四、主 Boss 掉落：专属武器、材料与战利品表

柠檬猫直接召来的食物系助战带 `ftboss:lemon_summon_no_rewards` 持久化标记，以下掉落表对它们全部失效：战利品表、装备、石像、武器、遗物、法术卷轴、手写物品与经验均为 0；普通方式召唤的同类 Boss 仍按表结算。献祭后的直接助战另带 `ftboss:lemon_post_sacrifice_helper`：真正死亡累计 3 位后柠檬永久停召，取消死亡、超时与 `discard()` 不计。

武器掉落走中央钩子 `BossMercy.giveWeaponDrop`，同样**不要求玩家击杀**（`boss/ai/BossMercy.java:362-410`）；沙耶走自己的 `dropRewards`（`entity/TeaTreeBossEntity.java:532-551`）；潮宴鱼王与炫彩答辩超人另用原版战利品表。10 只主 Boss 的专属武器一件都不缺（回归检查 `tools/verification/check_boss_weapon_drops.cjs` 会模拟"有没有掉落路径"）。

| Boss | 掉落物 | 数量 | 条件与出处 |
|---|---|---|---|
| 糖霜女皇 | `ftboss:candy_cane` | 1 | 真死即掉（`boss/ai/BossMercy.java:372-375`） |
| 糖霜女皇 | `ftboss:frost_queen_staff` | 1 | 与遗物同一钩子（需玩家击杀 + `mobLoot`），每次击杀**必掉**（`skill/BossRelicDrops.java:63`）；右键射线 24 格 / 14 点魔法伤害 / 冷却 30 tick，Shift 右键新星半径 5 格 / 10 点魔法伤害 / 冷却 160 tick（`item/FrostQueenStaffItem.java:33-38`） |
| 糖霜女皇 | `minecraft:sugar` | 24 ~ 48 | 真死掉落 `24 + random.nextInt(25)`（`boss/SugarQueenBoss.java:550-551`） |
| 糖霜女皇 | `minecraft:paper`（命名「秘制焦糖配方」） | 1（30% 概率） | 真死掉落，`random.nextFloat() < 0.3f`（`boss/SugarQueenBoss.java:554-558`） |
| 糖霜女皇 | 经验球 | 200 点 | 真死掉落（`boss/SugarQueenBoss.java:559`） |
| 屠夫 | `ftboss:turkey_legs` | 1 | 真死即掉（`boss/ai/BossMercy.java:380-384`） |
| 屠夫 | `minecraft:cooked_porkchop` | 12 ~ 27 | 真死掉落 `12 + random.nextInt(16)`（`boss/MeatBoss.java:428,430`） |
| 屠夫 | `minecraft:cooked_beef` | 8 ~ 15 | 真死掉落 `8 + random.nextInt(8)`（`boss/MeatBoss.java:429,431`） |
| 屠夫 | `minecraft:paper`（命名「秘制烤肉配方」） | 1（30% 概率） | 真死掉落，`random.nextFloat() < 0.3f`（`boss/MeatBoss.java:435-439`） |
| 屠夫 | 经验球 | 150 点 | 真死掉落（`boss/MeatBoss.java:440`） |
| 故障机器人 | `ftboss:defect_claw` | 1 | 真死即掉（`boss/ai/BossMercy.java:385-388`） |
| 边牧犬王 benger | `ftboss:dog_beating_stick` | 1 | 真死即掉（`boss/ai/BossMercy.java:389-393`） |
| 苹果爵士·蜜脆 | `ftboss:apple_stem` | 1 | 真死即掉（`boss/ai/BossMercy.java:376-379`） |
| 苹果爵士·蜜脆 | `minecraft:enchanted_golden_apple` | 1 ~ 2 | `1 + random.nextInt(2)`（`entity/AppleBossEntity.java:1204-1210`） |
| 沙耶（茶树古树） | `ftboss:blooming_thorns` | 1 | 真死掉落（`entity/TeaTreeBossEntity.java:543-545`） |
| 沙耶（茶树古树） | `ftboss:thorn_crown` | 1 | 真死掉落（`entity/TeaTreeBossEntity.java:546-547`） |
| 沙耶（茶树古树） | `minecraft:oak_leaves` | 24 | 真死掉落（`entity/TeaTreeBossEntity.java:541`） |
| 沙耶（茶树古树） | `minecraft:oak_sapling` | 8 | 真死掉落（`entity/TeaTreeBossEntity.java:542`） |
| 沙耶（茶树古树） | 经验球 | 150 点 | 真死掉落（`entity/TeaTreeBossEntity.java:548`） |
| 潮宴鱼王 | `ftboss:tide_trident` | 1 | 真死即掉（`boss/ai/BossMercy.java:368-370`） |
| 潮宴鱼王（战利品表） | `ftboss:tide_pearl` | 4 ~ 8（另加抢夺 0 ~ 1） | `data/ftboss/loot_tables/entities/tide_fish_boss.json` |
| 潮宴鱼王（战利品表） | `minecraft:cooked_salmon` | 8 ~ 12 | 同上 |
| 潮宴鱼王（战利品表） | `minecraft:prismarine_crystals` | 2 ~ 5（另加抢夺 0 ~ 1） | 同上 |
| 潮宴鱼王（战利品表） | `ftboss:music_disc_tide_fish` 或 `ftboss:music_disc_tide_fish_p2` | 1 | 同上，两个条目权重各 1 |
| 潮宴鱼王（战利品表） | `minecraft:heart_of_the_sea` | 1 | 同上，20% 概率，每级抢夺 +5% |
| 炫彩答辩超人（战利品表） | `ftboss:compost_crown` | 1 | `data/ftboss/loot_tables/entities/compost_boss.json` |
| 鼠群 | `ftboss:cheese_wheel_mace` | 1 | 真死即掉（`boss/ai/BossMercy.java:394-400`）；小老鼠是 `ftboss:rat_minion`，类型不同不会跟着掉 |
| 酒魔·酒桶王 | `ftboss:wine_bottle_mace` | 1 | 真死即掉（`boss/ai/BossMercy.java:401-410`） |
| 酒魔·酒桶王 | `ftboss:barrel_crossbow` | 1 | 与上一行同一钩子；它**手上**那把仍是纯外观（`setDropChance` 0，`entity/WineBarrelBossEntity.java:329-337`），掉落由中央钩子统一发一把 |

战利品表掉落遵循原版规则：只要 `mobLoot` 游戏规则开启，Boss 真死就结算，**不要求玩家击杀**；只有带 `looting_enchant` / `random_chance_with_looting` 的条目（潮宴鱼王的 `ftboss:tide_pearl`、`minecraft:prismarine_crystals`、`minecraft:heart_of_the_sea`）会再按击杀者的抢夺等级加成。

## 五、隐藏 Boss（庄园十一人）掉落

| variant | Boss 名 | 掉落物 | 条件与出处 |
|---|---|---|---|
| 0 | 柚子 | `ftboss:statue_youzi` | variant 0 且非迷你、非巨型，`mobLoot` 开启；每次先清掉同名掉落再补 1 个（`skill/YouziStatueSkills.java:166-177`、`util/YouziStatueRules.java:32-34`） |
| 1 | 蛋蛋 | `ftboss:dandan_scythe` | variant 1 且非迷你 / 非巨型 / 非 `noAi` / 非终局成员，玩家击杀且 `mobLoot` 开启（`util/DandanScytheRules.java:25-28`） |
| 2 | 朵拉 | `ftboss:dora_nova_launcher` | variant 2 且非迷你 / 非巨型 / 非 `noAi` / 非终局成员，`recentlyHit` 为真；同时移除 `bren:machine_gun` 掉落（`skill/DoraSmgDrops.java:20-32`、`util/DoraSmgRules.java:22-24`） |
| 3 | 功底 | — | 无掉落规则，武器 `ftboss:black_cleaver` 仅持握与创造栏 |
| 4 | 柠檬猫 | `ftboss:lemoncat_skillet` 或 `ftboss:lemoncat_spatula` | variant 4 且非迷你、非终局成员，`recentlyHit` 为真且 `mobLoot` 开启；两件随机掉 1 件（`entity/EasterBossEntity.java:6408-6416`、`util/LemonCatWeaponRules.java:8-10`） |
| 5 | 青衣 | — | 无掉落规则，武器 `ftboss:qingyi_confluence_trident` 走合成配方与庄园胜利宝箱 |
| 6 | 文玄蓝 | `ftboss:wenxuanlan_flywheel` | variant 6 且非迷你 / 非巨型 / 非 `noAi` / 非终局成员，`recentlyHit` 为真且 `mobLoot` 开启（`skill/XuanlanDrops.java:19-29`、`util/XuanlanRules.java:31-33`） |
| 7 | 折耳根 | — | 无掉落规则，武器 `ftboss:shadow_sword` 仅持握与创造栏 |
| 8 | 紫菜 | — | 无掉落规则，武器 `ftboss:puffer_bat` 仅持握与创造栏 |
| 9 | 麻雀（支援位） | `ftboss:maque_blessing_feather` | variant 9 且非迷你、非巨型，`recentlyHit` 为真且 `mobLoot` 开启；非终局成员（`entity/EasterBossEntity.java:6417-6421`、`util/MaqueFeatherRules.java:25-27`） |
| 10 | 雪黎 | `ftboss:xueli_skillet` + `ftboss:xueli_rolling_pin` | variant 10 且非迷你 / 非 `noAi` / 非终局成员，玩家击杀且 `mobLoot` 开启；两件各 1（`item/weapons/XueliWeaponCombat.java:190-202`、`item/weapons/XueliWeaponRules.java:50-52`） |
| 11 | 祁崽 | — | 无掉落规则，武器 `ftboss:qizai_prayer_staff` 仅持握、创造栏与庄园胜利宝箱（铁魔法 `CastingItem` 法杖） |

庄园十一人全部击败后生成胜利宝箱 `ftboss:manor_loot_chest`（容量 54 格，`block/ManorLootChestBlockEntity.java:17`）：内容是 `ManorWeaponCatalog.ENTRIES` 的 20 项（含 `ftboss:qizai_prayer_staff`；缺外部模组时跳过）+ `ftboss:qingyi_confluence_trident`，再追加所有已启用的庄园法术书（`boss/ManorVictoryLoot.java:19-49`、`item/ManorWeaponCatalog.java:21-41`）。

## 六、遗物（Boss 遗物 · Curios）

遗物掉落在 `skill/BossRelicDrops.java` 结算，规则如下：

- 掉落率 `DROP_CHANCE` = 1.0（必掉），即 `random.nextFloat() < 1.0F` 恒成立（2026-09-19 按用户要求由 35% 改成必掉）（`util/BossRelicRules.java:6,46-48`）。
- 必须由玩家击杀（直接击杀者或 `killCredit` 是 `ServerPlayer`），且 `mobLoot` 开启；每只 Boss 只结算一次（`skill/BossRelicDrops.java:52-62`）。
- 召唤物不算：小老鼠等小怪传 `minion = true` 时 `forEntity` 返回 null（`util/BossRelicRules.java:34-40`、`skill/BossRelicDrops.java:56-57`）。
- 糖霜女皇与屠夫走 `LivingDeathEvent`（它们不用普通掉落表），其余 Boss 走 `LivingDropsEvent`（`skill/BossRelicDrops.java:29-50`）。
- 遗物只能放进 Curios 的 `boss_relic` 槽（槽位 `size` = 1）；物品 `stacksTo(1)`、EPIC、防火（`item/BossRelicItem.java:30,82-84`、`data/ftboss/curios/slots/boss_relic.json`）。

| Kind | Boss（实体 id） | 遗物物品 id | 基础属性加成 |
|---|---|---|---|
| FROST | 糖霜女皇 `ftboss:sugar_queen` | `ftboss:frost_crown_charm` | `MAX_HEALTH` +6 |
| BUTCHER | 屠夫 `ftboss:meat_mountain` | `ftboss:butcher_fang_charm` | `ATTACK_DAMAGE` +3 |
| TEA | 沙耶 `ftboss:tea_tree` | `ftboss:tea_heart_pendant` | `ARMOR` +4 |
| WINE | 酒桶王 `ftboss:wine_barrel` | `ftboss:aged_wine_seal` | `KNOCKBACK_RESISTANCE` +0.0（2026-09-19 取消） |
| RAT | 鼠群 `ftboss:rat_boss` | `ftboss:cheese_moon_charm` | `MOVEMENT_SPEED` +12% |
| DEFECT | 故障机器人 `ftboss:defect_core` | `ftboss:charged_core_pendant` | `ATTACK_SPEED` +15% |
| COMPOST | 炫彩答辩超人 `ftboss:compost_boss` | `ftboss:rainbow_spore_charm` | `ARMOR_TOUGHNESS` +3 |
| APPLE | 苹果爵士·蜜脆 `ftboss:apple_boss` | `ftboss:golden_apple_brooch` | `LUCK` +2 |
| BENGER | 边牧犬王 benger `ftboss:benger` | `ftboss:shepherd_whistle_charm` | `MOVEMENT_SPEED` +8% 与 `ATTACK_DAMAGE` +1 |
| TIDE | 潮宴鱼王 `ftboss:tide_fish_boss` | `ftboss:tide_pearl_pendant` | `SWIM_SPEED` +35% |

表中 `MOVEMENT_SPEED` / `ATTACK_SPEED` / `SWIM_SPEED` 三条是 `MULTIPLY_BASE`（百分比），其余为 `ADDITION`（`item/BossRelicItem.java:49-63`）。

掉落时随机 roll 0 ~ 3 决定词缀（`item/BossRelicItem.java:34-38`、`util/BossRelicRules.java:9`）：

- 词缀 `vitality`：`MAX_HEALTH` +4（`ADDITION`）。
- 词缀 `guarding`：`ARMOR` +2（`ADDITION`）。
- 词缀 `swift`：`MOVEMENT_SPEED` +5%（`MULTIPLY_BASE`）。
- 词缀 `fortunate`：`LUCK` +1（`ADDITION`）。

## 七、法术书掉落（Iron's Spells 集成）

只有 `util/BossSpellRules.PAIRS` 登记过的 Boss 才掉法术书；掉落物是该 Boss 全部**已启用**法术的最高等级卷轴，每法术 1 张（`integ/spells/BossSpellDrops.java:45-55`）。

| Boss | 法术书张数 | 条件与出处 |
|---|---|---|
| 糖霜女皇 | 5（冰系一整套） | 本体（非克隆体）真死 + 玩家击杀 + `mobLoot` 开启 + 铁魔法已加载且法术启用（`integ/spells/BossSpellDrops.java:25-30`、`util/BossSpellRules.java:73`） |
| 沙耶（茶树古树） | 7（自然 / 血液系一整套） | 同上（`util/BossSpellRules.java:74`） |
| 酒魔·酒桶王 | 5 | 同上（`util/BossSpellRules.java:75`） |
| 故障机器人 | 5 | 同上（`util/BossSpellRules.java:76`） |
| 炫彩答辩超人 | 5 | 同上（`util/BossSpellRules.java:77`） |
| 苹果爵士·蜜脆 | 5 | 同上（`util/BossSpellRules.java:78`） |
| 潮宴鱼王 | 5 | 同上（`util/BossSpellRules.java:79`） |
| 屠夫 / 鼠群 / 边牧犬王 benger | — | `PAIRS` 未登记，不掉法术书 |
| 隐藏 Boss（庄园十一人各变体） | 0~8 与 10 各 3 张、祁崽（变体 11）2 张 | **当前 `PER_BOSS_DROPS` = false 时一张都不掉**，卷轴改由通关宝箱发放；这条开关开回 true 时按 `ManorSpellRules.pair(variant)` 掉（`entity/EasterBossEntity.java:6814`、`integ/spells/ManorSpellDrops.java:26-40`、`util/ManorSpellRules.java:73`） |

**★ 玩家用这些卷轴施法时有加强（2026-09-19 按用户要求「boss掉落的法术卷轴数值翻倍，buff持续时间翻倍」）**：

| 项 | 玩家施法 | Boss 施法 |
|---|---|---|
| 数值（伤害 / 护盾 / 治疗） | `BossSpellTuning.playerPower` → ×`PLAYER_POWER_MULT` 2.0 | 原样（`signaturePower` / `ManorBossSpellProfile` 那套不变） |
| 正面效果（buff）持续时间 | ×`PLAYER_BUFF_MULT` 2 | 原样 |
| 减益（缓慢 / 虚弱 / 中毒 / 反胃 …）持续时间 | 不变 | 原样 |

判定口径是"施法者是不是玩家"（`caster instanceof Player`），所以把卷轴抄进法术书后照样享受；五个法术效果类里的每一次 `MobEffectInstance` 都过 `BossSpellTuning.buffTicks(caster, effect, ticks)`，只对 `isBeneficial()` 的效果翻倍（`integ/spells/BossSpellTuning.java`）。

## 八、音乐物品与唱片

唱片全部是原版 `RecordItem`，比较器输出统一 15；长度参数必须等于音频真实时长，且必须登记进 `data/minecraft/tags/items/music_discs.json`（17 张全部已登记），否则唱片机会静默拒收（`item/FTBossItems.java:219-222`）。每张唱片单独注册 sound event（`music_disc_*`），与战斗 BGM 事件（`boss_music_*`）分开，避免战斗切曲时把唱片机正在放的曲子一起掐掉（`sound/FTSounds.java:163-184,229-232`）。音乐总开关 `MUSIC_ENABLED` = true（`sound/FTSounds.java:32`）。

| 唱片 id | 对应 Boss / 曲目 | 稀有度 | 时长（tick） | 约合秒 |
|---|---|---|---|---|
| `ftboss:music_disc_tide_fish` | 潮宴鱼王 一阶段 | RARE | 1980 | 99.0 |
| `ftboss:music_disc_tide_fish_p2` | 潮宴鱼王 二阶段 | EPIC | 1927 | 96.4 |
| `ftboss:music_disc_apple` | 苹果爵士《果园教父》 | RARE | 2000 | 100.0 |
| `ftboss:music_disc_apple_p2` | 苹果爵士《果园教父·虫蛀暴走》 | RARE | 1644 | 82.2 |
| `ftboss:music_disc_gingerbread` | 糖霜女皇《木偶剧场·加洛普》 | RARE | 2308 | 115.4 |
| `ftboss:music_disc_butcher` | 屠夫《肉山祭坛》 | RARE | 4568 | 228.4 |
| `ftboss:music_disc_saya` | 沙耶《茶の唄》 | RARE | 2633 | 131.7 |
| `ftboss:music_disc_tavern` | 酒桶王《醉乡酒馆》 | RARE | 2478 | 123.9 |
| `ftboss:music_disc_rat` | 鼠群《鼠潮夜行》 | RARE | 2236 | 111.8 |
| `ftboss:music_disc_defect` | 故障机器人 | RARE | 3696 | 184.8 |
| `ftboss:music_disc_compost` | 炫彩答辩超人《堆肥·强袭》 | RARE | 1782 | 89.1 |
| `ftboss:music_disc_compost_p2` | 炫彩答辩超人《堆肥·熔毁》 | RARE | 1714 | 85.7 |
| `ftboss:music_disc_manor_cue` | 庄园《庄园·序》演出曲 | RARE | 916 | 45.8 |
| `ftboss:music_disc_manor_battle` | 庄园《庄园·战》 | EPIC | 5170 | 258.5 |
| `ftboss:music_disc_manor_dual` | 庄园《庄园·双轨》 | EPIC | 5130 | 256.5 |
| `ftboss:music_disc_manor_guignol` | 庄园《庄园·鏖战》 | EPIC | 4173 | 208.7 |
| `ftboss:music_disc_benger` | 边牧犬王 benger《牧野犬王》 | EPIC | 3708 | 185.4 |

出处：`item/FTBossItems.java:22-27,224-320`。其中 `music_disc_butcher` 与 `music_disc_defect` 的**代码参数**（4568 / 3696）与同行注释写的秒数（145.34 s / 156.04 s）不一致，上表按实际构造参数取值。

## 九、交易入口

- 交易配置在 `config/ftboss_trades.json`，模组初始化时读取一次，改完需重启生效（`FTBossMod.java:72`、`trade/TradeHandler.java:47-49,120`）；文件不存在时自动写出默认内容（`trade/TradeHandler.java:51-53,123-131`）。
- 默认两条交易：`minecraft:sugar` x8 → `ftboss:sugar_queen_crystal` x1；`minecraft:sugar` x16 → `minecraft:emerald` x1（`trade/TradeHandler.java:126-129`）。
- 读取失败时的兜底交易：`minecraft:sugar` x8 → `ftboss:sugar_queen_crystal` x1（`trade/TradeHandler.java:73-77`）。
- 逻辑本身：手持成本物品右键 → 扣材料、给结果，背包放不下就掉在方块旁；空手右键输出可交易清单（`trade/TradeHandler.java:85-121`）。
- **交易没有游戏内触发点**：`TradeHandler.tryTrade` 在全工程无调用者，通用交易石像 `ftboss:statue` 没有方块 / 物品注册，也没有右键交易钩子（`boss/BossHandler.java:131-133`），因此交易实际不可用，只有配置加载逻辑仍在运行。
- 本模组的 9 座 Boss 石像右键不参与交易；石像只做外观切换，且用的是**左键**（`boss/BossHandler.java:141-177`）。

## 十、一页速查表：Boss → 召唤物 → 主要掉落

| Boss | 召唤物 | 主要掉落 |
|---|---|---|
| 糖霜女皇（姜饼王） | `ftboss:sugar_queen_crystal` | 石像 2 座 + `candy_cane` + `frost_queen_staff` + 糖 24 ~ 48 + 经验 200 + 遗物必掉 + 5 张法术书 |
| 屠夫 | `ftboss:meat_heart` | 石像 2 座 + `turkey_legs` + 熟猪排 12 ~ 27 + 牛排 8 ~ 15 + 经验 150 + 遗物必掉 |
| 沙耶（茶树古树） | `ftboss:tea_seed` | 石像 + `blooming_thorns` + `thorn_crown` + 树叶 24 + 树苗 8 + 经验 150 + 遗物必掉 + 7 张法术书 |
| 酒魔·酒桶王 | `ftboss:wine_charm` | 石像 + `wine_bottle_mace` + `barrel_crossbow` + 遗物必掉 + 5 张法术书 |
| 鼠群 | `ftboss:rat_bait` | 石像 + `cheese_wheel_mace` + 遗物必掉（小老鼠不掉遗物、不掉武器） |
| 故障机器人 | `ftboss:defect_core` | 石像 + `defect_claw` + 遗物必掉 + 5 张法术书 |
| 炫彩答辩超人 | `ftboss:compost_summon` | 石像 + `compost_crown` + 遗物必掉 + 5 张法术书 |
| 苹果爵士·蜜脆 | `ftboss:forbidden_apple` | 石像 + `apple_stem` + 金苹果 1 ~ 2 + 遗物必掉 + 5 张法术书 |
| 边牧犬王 benger | `ftboss:benger_whistle` 或 `ftboss:benger_spawn_egg` | `dog_beating_stick` + 遗物必掉；无石像、无法术书 |
| 潮宴鱼王 | `ftboss:tide_banquet` | 石像 + `tide_trident` + `tide_pearl` 4 ~ 8 + 熟鲑鱼 8 ~ 12 + 海晶碎片 2 ~ 5 + 海之心 20% + 潮宴鱼王唱片 1 张 + 遗物必掉 + 5 张法术书 |
| 隐藏 Boss（庄园十一人） | `ftboss:easter_egg_*`（11 个）或 `/ftboss challenge` | 各变体专属武器（柚子 / 蛋蛋 / 朵拉 / 柠檬猫 / 文玄蓝 / 麻雀 / 雪黎 / 祁崽）+ 每个变体 3 张法术书 + 通关胜利宝箱；无石像、无遗物 |
