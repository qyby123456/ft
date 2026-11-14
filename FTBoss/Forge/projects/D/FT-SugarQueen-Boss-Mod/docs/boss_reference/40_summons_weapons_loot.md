# 召唤物 / 武器 / 掉落 / 音乐 / 其他

口径：数字取自本仓库源码与数据包，构建戳 `BossHandler.BUILD_TAG = "easter-v109-spawn-eggs"`；中文名取自 `assets/ftboss/lang/zh_cn.json`。

## 隐藏 Boss 刷怪蛋（v109 新增，共 10 个）

| 物品 id | 中文名 | variant | 说明 |
|---|---|---|---|
| ftboss:easter_egg_youzi | 刷怪蛋·柚子 | 0 | 庄园轮换第 1 位 |
| ftboss:easter_egg_dandan | 刷怪蛋·蛋蛋 | 1 | 末影换位 / 抽玩家增益 |
| ftboss:easter_egg_duola | 刷怪蛋·朵拉 | 2 | 冲锋枪连射 / 聚能枪蓄力狙击 / 长枪突刺与冲刺 |
| ftboss:easter_egg_gongdi | 刷怪蛋·功底 | 3 | 黑色切割者 / 血怒 |
| ftboss:easter_egg_ningmengmao | 刷怪蛋·柠檬猫 | 4 | 食物增益 / 喊食物系 Boss 助战 |
| ftboss:easter_egg_qingyi | 刷怪蛋·青衣 | 5 | 三叉戟 / 恒星三叉戟雨 |
| ftboss:easter_egg_wenxuanlan | 刷怪蛋·文玄蓝 | 6 | 飞轮绞杀 / 蒸汽泄压 / 超能光束 |
| ftboss:easter_egg_zheergen | 刷怪蛋·折耳根 | 7 | 暗影突袭 |
| ftboss:easter_egg_zicai | 刷怪蛋·紫菜 | 8 | 毒雾 / 河豚减益池 |
| ftboss:easter_egg_xueli | 刷怪蛋·雪黎 | 10 | 五招轮换（首发禁疗飞刀）/ 女仆护法 / 自爆分身 |

- 机制：`item/EasterSpawnEggItem.java` —— 隐藏 Boss 十人共用**同一个实体** `ftboss:easter_boss`，只是 `variant` 不同，所以原版 `SpawnEggItem` 不够用；这个物品自己实现 `useOn`，生成时写入 `variant`（走 `EasterBossEntity.setVariant`），并 `setPersistenceRequired()`（测试时跑远也不会被刷掉）。
- 外观：模型 `assets/ftboss/models/item/easter_egg_*.json` 全部指向原版 `minecraft:item/template_spawn_egg`（蛋壳 + 斑点两层），颜色由客户端 `FTClientSetup.onClientSetup` 注册的 `ItemColors` 提供（tintIndex 0 = 蛋壳色，1 = 斑点色）。**零新增贴图**。
- 麻雀（variant 9）是支援位，**不发刷怪蛋**；10 个蛋的 variant 顺序 = `EasterBossEntity.MANOR_VARIANTS = {0,1,2,3,4,5,6,7,8,10}`。
- 全部已加入本模组创造模式物品栏（创造页最末），也可以用 `/give @s ftboss:easter_egg_xueli` 之类的指令拿。
- 注意：刷怪蛋是**脱开庄园流程**直接生成单只 Boss；想要完整的十人轮换/开场演出/终章，仍然走庄园触发（见 32 号文件）。

## 召唤物物品

| 物品 id | 中文名 | 堆叠/稀有度 | 右键效果 |
|---|---|---|---|
| ftboss:sugar_queen_crystal | 糖霜水晶 | 16 / RARE | 召唤糖霜女皇，消耗 1 |
| ftboss:meat_heart | 屠夫之心 | 16 / RARE | 召唤屠夫，消耗 1 |
| ftboss:tea_seed | 茶树种子 | 16 / EPIC | 视线 3 格外长出沙耶 |
| ftboss:wine_charm | 酒引 | 16 / EPIC | 视线 2.5 格外召唤酒魔·酒桶王 |
| ftboss:rat_bait | 奶酪诱惑 | 16 / UNCOMMON | 视线 2.5 格外召唤鼠群 |
| ftboss:defect_core | 故障机器人 | 16 / EPIC | 视线 4 格、抬高 2.4 格召唤 |
| ftboss:compost_summon | 炫彩堆肥桶 | 1 / 默认 | 面朝 4 格召唤炫彩答辩超人 |
| ftboss:forbidden_apple | 禁果 | 1 / 默认 | 面朝 4 格召唤苹果爵士·蜜脆 |

- 通用限制：全部过 `SummonGate.allow(...)`（`OVERWORLD_ONLY = true`，只允许主世界），不通过则**中止且不消耗**；女皇与屠夫另有「已在战斗中」检查（`SugarQueenBoss.isActive` / `MeatBoss.isActive`）。
- 五件食物系召唤物共用配方形状 `ABC / D#E / FGH`，中心 `#` = `farmingtales:gold_bucket`，外圈 8 格各为 8 种主题食材，产出 1 个（`data/ftboss/recipes/*.json`）：水晶 = 8 种甜点（`bakery:chocolate_gateau`、`farmingtales:fruit_waffle`/`blueberry_pie`/`blueberry_cake`/`pineapple_pie`、`farm_and_charm:grandmothers_strawberry_cake`、`bakery:strawberry_cake`、`farmersdelight:chocolate_pie`）。
- 肉心 = 8 种肉料理（`brewery:pork_knuckle`、`farm_and_charm:baked_lamb_ham`、`kaleidoscope_chinesefood:sichuan_boiled_pork_slices`/`sauerkraut_beef_noodles`/`stir_fried_yellow_beef_rice`、`kaleidoscope_cookery:cold_cut_ham_slices`/`braised_beef_rice_bowl`、`meadow:sausage_with_cheese`）。
- 茶种 = 8 种茶（`herbalbrews:oolong_tea`/`dried_oolong_tea`/`black_tea`/`green_tea`、`smc:ice_tea`、`kaleidoscope_world_liquor:cool_tea`、`farm_and_charm:strawberry_tea`、`crockpot:iced_tea`）。酒引 = 8 种葡萄酒（`vinery:jellie_wine`、`aegis_wine`、`cristel_wine`、`lilitu_wine`、`solaris_wine`、`clark_wine`、`chorus_wine`、`glowing_wine`）。奶酪诱惑 = 8 种奶酪（`brewinandchewin:fiery_fondue_pot`/`flaxen_cheese_wheel`/`scarlet_cheese_wheel`、`meadow:grain_cheese_block`/`amethyst_cheese_block`/`warped_cheese_block`/`goat_cheese_block`/`buffalo_cheese_block`）。
- 故障机器人：同样 `ABC / D#E / FGH` + 中心 `farmingtales:gold_bucket`，外圈 `create:precision_mechanism`、`mechanical_arm`、`electron_tube`、`blaze_burner`、`rose_quartz_block`、`zinc_block`、`brass_block`、`createaddition:capacitor`；**无单例门槛，可同时召唤多只。**
- 炫彩堆肥桶：`PBP / BCB / PBP`，P = `create:precision_mechanism`、B = `minecraft:bone_meal`、C = `minecraft:composter`。禁果：`GRG / RAR / GRG`，G = `minecraft:gold_block`、R = `minecraft:redstone_block`、A = `minecraft:apple`；生存模式必定消耗（创造不消耗）。
- 冷却：五件食物系召唤物**没有冷却**，限制来自「已在战斗中」与主世界判定；故障机器人与炫彩堆肥桶无数量上限。统一冷却常量（代码里未找到 ?）。

## 五把主题武器（FTWeapons.java）

- 五把共用材质 `FTWeaponTier.INSTANCE`：耐久 1500、挖掘速度 6.0、附加伤害 3.0、等级 = 铁级、附魔值 18、修复材料 `minecraft:iron_ingot`。显示攻击力 = 1（玩家基础）+ 参数 + 3（材质）。
- 酒瓶锤 `ftboss:wine_bottle_mace`：重装瓶身模型与酒压技能重做；普攻仍为伤害 9、攻速 0.8。有效近战实伤每秒至多攒 1 层酒压，上限 3、15 秒失效；J「开瓶重砸」锁定 7 格内位置、0.6 秒蓄势，半径 3～4.5 格，伤害 `6+1.5A+酒压×(4+0.35A)`（单目标上限 128）；冷却 12 秒，消耗 3 耐久与全部酒压。取消原强制醉酒与随机增益，满压命中才获得抗性 I 4 秒。详见 `../wine-bottle-mace.md`。
- 奶酪轮锤 `ftboss:cheese_wheel_mace` 保持原机制：伤害参数 8（合计 12），攻速 −3.4；手持醉酒 V，每 8 秒从原 6 项池轮替 2 个 II 级效果，命中 35% 反胃与迟缓；不使用酒瓶锤的新酒压机制。
- 这两把是本模组自制品，`data/ftboss/recipes/` 下没有它们的配方，创造栏 `FTCreativeTabs` 有条目；掉落途径（代码里未找到 ?）。

## 其余 Boss 武器与掉落物

- 荆棘头冠 `ftboss:thorn_crown`：沙耶最终死亡必掉 1 顶的史诗头盔。护甲 4、韧性 3、击退抗性 10%、耐久 528、附魔值 18、藤蔓修复。佩戴后近战反击实受伤害的 25%，最低 2、最高 6 点，冷却 0.5 秒；荆棘伤害不递归反击，不反击友方、弹射物、爆炸或环境伤害。仅头盔栏生效；佩戴时视野染血红，摘下立即恢复。

| 物品 id | 中文名 | 数值 | 来源 |
|---|---|---|---|
| ftboss:dora_pulse_gun | 朵拉的地脉脉冲枪 | 原蓄力脉冲/新星：满蓄 32/28 伤害，冷却 2.5/6 秒；与 Boss 三段狙击表独立 | 保留在创造栏武器区，不替换冲锋枪掉落 |
| ftboss:black_cleaver | 黑色切割者 | 下界合金剑，参数 20（合计 25），攻速 −2.6 | 彩蛋「功底」(variant 3) 主手 |
| ftboss:shadow_sword | 暗影剑 | 下界合金剑，参数 4（合计 9），攻速 −2.4 | 彩蛋「折耳根」(7) |
| ftboss:puffer_bat | 河豚棒 | 钻石剑，参数 3（合计 8），攻速 −2.0 | 彩蛋「紫菜」(8)，魔法攻击 |
| ftboss:blooming_thorns | 绽放的荆棘 | 下界合金剑，参数 4（合计 9），攻速 −2.4 | 沙耶死亡掉落，`dropRewards` |
| ftboss:candy_cane | 糖果拐杖 | 钻石剑，参数 3（合计 8），攻速 −2.2 | 糖霜女皇死亡掉落 |
| ftboss:apple_stem | 苹果爵士的果梗 | 下界合金剑，参数 3（合计 8），攻速 −2.2 | 苹果爵士死亡掉落 |
| ftboss:statue_youzi | 柚子小像 | 铁级剑；玩家基础攻击 6、攻速 1.8、耐久 512；史诗、防火 | 柚子本体死亡必掉 1 件，迷你/巨型分身不掉；创造栏武器区 |
| ftboss:wenxuanlan_flywheel | 文玄蓝的飞轮 | 普通物品 | 「文玄蓝」飞轮投射物外观 |
| ftboss:barrel_crossbow | 酒桶弩 | 普通物品，纯外观 | 酒桶王主手（空弦） |
| ftboss:barrel_crossbow_ready | 酒桶弩·装填 | 普通物品，纯外观 | 酒桶王齐射前 1 秒换上 |

- 酒桶弩（v108）：`WineBarrelBossEntity.CROSSBOW_READY_LEAD = 20` tick；`bState == ST_IDLE` 且 `cannonCd <= 20` 时换 `barrel_crossbow_ready`，否则 `barrel_crossbow`；主手 `setDropChance(..., 0.0f)`，即**弩不掉落**。
- 酒桶弩渲染 `client/render/WineBarrelGeoRenderer`：挂骨骼 `CROSSBOW_BONE = "spout"`，`OFF_X/Y/Z = 0.0`、`SCALE = 1.0`、`YAW_DEG = 0.0`，用 `ItemDisplayContext.NONE`（不套手持变换）；模型 128×128 贴图，模型文件 `models/item/barrel_crossbow[_ready].json`。
- 彩蛋 Boss 掉落自己的武器：`EasterBossEntity.dropWeapon()` 读 `WEAPONS[variant]` 表按注册 id 掉；表中含 `ftboss:statue_youzi`(0)、`minecraft:end_rod`(1)、`bren:machine_gun`(2)、`ftboss:black_cleaver`(3)、`farmersdelight:skillet`(4)、`tridents_n_stuff:stormseeker`(5)、`create:flywheel`(6)、`ftboss:shadow_sword`(7)、`ftboss:puffer_bat`(8)、`minecraft:feather`(9 麻雀)、`farmersdelight:skillet`(10 雪黎)。
- 苹果爵士：`ftboss:apple_core` × (2 + nextInt(3))（2~4 个）+ `minecraft:enchanted_golden_apple` × (1 + nextInt(2))（1~2 个）；武器与石像走 `BossMercy`（`AppleBossEntity.java:1162-1165`）。

## 柚子小像的玩家技能

- 右键「三重叠影」：起手后第 4、16、28 tick 各释放一道粉白光束，按当时准星瞄准；每道 8 点魔法伤害，射程 18 格，冷却 8 秒，消耗 2 耐久。光束被方块和沿途第一个生物挡住，不穿透墙壁或友方去伤害后排。
- 潜行右键「柚影守护」：持续 6 秒，受到伤害降低 25%，冷却 16 秒，消耗 4 耐久；绕过无敌的伤害（如虚空、管理员处决）不减免。仅需手持，不额外叠加抗性或吸收药水。
- 两招共享物品冷却，多件同名物品不能轮换连续释放。须保持原手原物品栈；换手、切换物品、死亡、退出或换维度结束当前技能，冷却不退款。耐久不足时拒绝施法，创造模式不扣耐久。
- 光束不伤自己、同队、已认主宠物与驯服的马；玩家目标另遵守服务端 PvP 开关、队伍友伤规则，不攻击创造或旁观玩家。不破坏方块或地面掉落物。
- 视觉：平时手边稀疏白色微光；起手召出三枚全亮小像、粉白底环和星芒；光束为白芯粉晕，命中处散星；护盾期间小像绕身，受击出现扩散亮环。装饰环和光束自发光，不要求光影包，不改变世界照明亮度。
- 掉落通过 Forge `LivingDropsEvent` 发放：同次掉落去重到一件满耐久小像；遵守 `doMobLoot` 以及事件取消；非死亡清场不掉。本技能是玩家专用，不给柚子 Boss 额外加招。

## 掉落表与材料

- 柠檬猫直接召来的食物系助战带持久化无奖励标记，死亡时原版/战利品表掉落、石像、武器、遗物、法术卷轴、手写奖励与经验全部为 0；普通同类 Boss 不受影响。献祭后直接助战真死累计 3 位会永久关闭柠檬召唤，取消死亡、超时与 `discard()` 不计（`LemonSummonRewards`）。

- `data/ftboss/loot_tables/` 下**只有** `blocks/statue_*.json` 共 8 份（苹果/屠夫/堆肥/故障/鼠群/沙耶/女皇/酒桶王），全部 `minecraft:block` 类型、1 次 roll、掉同名石像物品，条件 `block_state_property` 限定 `half = lower`。
- `data/ftboss/loot_tables/entities/` **不存在**：Boss 掉落全部由代码发放，不走战利品表。
- 糖霜女皇（`SugarQueenBoss.onBossDeath`）：`minecraft:sugar` × (24 + nextInt(25))（24~48）+ 女皇石像 ×1 + 30% 概率掉命名为「§6秘制焦糖配方」的 `minecraft:paper` + 经验球 200。
- 沙耶（`TeaTreeBossEntity.dropRewards`）：`minecraft:oak_leaves` ×24、`minecraft:oak_sapling` ×8、`ftboss:blooming_thorns` ×1、`ftboss:thorn_crown` ×1、经验球 150；死亡时清掉它的茶芽小妖。
- 材料与用途：`ftboss:apple_core`（苹果核）为苹果爵士材料。
- 每只 Boss 死亡都掉自己的石像（`BossMercy.giveStatue`）：女皇→`statue_sugar_queen`、屠夫→`statue_butcher`、沙耶→`statue_saya`、酒桶王→`statue_wine_barrel`、鼠群→`statue_rat_swarm`、苹果爵士→`statue_apple`、答辩超人→`statue_compost`、故障机器人→`statue_defect`。连死怜悯离场（连死 5 次）**不掉落、不给经验**（`BossMercy.despawnBoss`）。

## 唱片与音乐物品

| 物品 id | 中文名 | 曲目与时长 |
|---|---|---|
| ftboss:music_disc_manor_cue | 庄园唱片 · 序 | boss_music_manor_cue，916 tick（45.8 s），RARE |
| ftboss:music_disc_manor_battle | 庄园唱片 · 战 | boss_music_manor_battle，5170 tick（258.5 s，4:18），EPIC |
| ftboss:music_disc_manor_dual | 庄园唱片 · 双轨 | music_disc_manor_dual，5130 tick（256.5 s，4:16），EPIC |
| ftboss:music_disc_apple | 苹果爵士唱片 | music/apple.ogg，2000 tick（99.98 s，1:39），RARE |
| ftboss:music_disc_apple_p2 | 苹果爵士唱片 · 二阶段 | music/apple_p2.ogg，1644 tick（82.20 s，1:22），RARE |
| ftboss:music_disc_gingerbread | 糖霜女皇唱片 | music/gingerbread.ogg，2308 tick（115.38 s，1:55），RARE |
| ftboss:music_disc_butcher | 屠夫唱片 | music/butcher.ogg，4568 tick（228.38 s，3:48），RARE |
| ftboss:music_disc_saya | 沙耶唱片 | music/saya.ogg，2633 tick（131.65 s，2:11），RARE |
| ftboss:music_disc_tavern | 酒桶王唱片 | music/tavern.ogg，2478 tick（123.92 s，2:03），RARE |
| ftboss:music_disc_rat | 鼠群唱片 | music/rat.ogg，2236 tick（111.81 s，1:51），RARE |
| ftboss:music_disc_defect | 故障机器人唱片 | music/defect_robot.ogg，3696 tick（184.80 s，3:05），RARE |
| ftboss:music_disc_compost | 炫彩答辩超人唱片 | music/compost.ogg，1782 tick（89.08 s，1:29），RARE |
| ftboss:music_disc_compost_p2 | 炫彩答辩超人唱片 · 二阶段 | music/compost_p2.ogg，1714 tick（85.68 s，1:25），RARE |
| ftboss:music_disc_manor_guignol | 庄园唱片 · 鏖战 | music/manor_guignol.ogg，4173 tick（208.67 s，3:29），EPIC |
| ftboss:music_disc_benger | 边牧犬王唱片 | music/benger.ogg，3708 tick（185.41 s，3:05），EPIC |

- **13 张唱片**都以 `RecordItem` 注册，比较器输出参数 `15`、`stacksTo(1)`；**时长参数必须等于音频真实时长**，否则唱片机会提前判定「放完了」。
- 15 张全部写在 `data/minecraft/tags/items/music_discs.json` 里（缺了会被 `JukeboxBlockEntity.setItem()` 静默拒绝，而 `RecordItem.useOn()` 照样扣掉唱片）；`sounds.json` 里各自的 `music_disc_*` 事件均为 `stream = true`。
- **v112 新增 10 张食物系唱片**（`FTBossItems.FOOD_MUSIC_DISCS` 列表，创造页直接遍历注册）：8 位食物系 Boss 的专属 BGM **全部**做成唱片 —— 苹果爵士与炫彩答辩超人各 2 首，其余 6 位各 1 首。音频文件**零新增**（与同名 BGM 共用 ogg）。
- 每张唱片另注册**独立的 `music_disc_*` 声音事件**，**刻意不复用 `boss_music_*`**：`BossMusic` 切曲时会往客户端发 `ClientboundStopSoundPacket` 掐掉战斗曲，唱片若挂在同一个事件上，一开打/一结束就会把唱片机里正在放的同名曲一起掐了。
- 贴图与物品模型由 `tools/model3d/gen_discs.cjs` 生成（16×16 黑胶画法，每张一个标签色）。
- 时长口径：**一律用 `node tools/audio_tools/ogg_dur.cjs <ogg...>` 现场测**（纯 JS 扫 `OggS` 页取最大 granule ÷ 采样率，秒 × 20 = tick）。v112 就是用它发现庄园三张唱片的声明长度过期（836/5353/5353 → 916/5170/5130）并修正的。
- 旧试听唱片（序曲 / 第一段 / 第二段 / 第三段 / 终曲 / 样片 6 / 样片 7）已按用户要求删除，`FTSounds` 无对应注册。

## 全部 boss_music_* 与曲名

| 音效 id | 音频文件与曲名 |
|---|---|
| boss_music_saya | music/saya.ogg《沙耶唱片》131 s |
| boss_music_gingerbread | music/gingerbread.ogg《糖霜女皇唱片》115 s |
| boss_music_butcher | music/butcher.ogg《屠夫唱片》 |
| boss_music_tavern | music/tavern.ogg《酒桶王唱片》 |
| boss_music_rat | music/rat.ogg《鼠群唱片》 |
| boss_music_defect_robot | music/defect_robot.ogg《故障机器人》 |
| boss_music_compost | music/compost.ogg《堆肥·冬夜》 |
| boss_music_compost_p2 | music/compost_p2.ogg《炫彩答辩超人唱片 · 二阶段》二阶段 |
| boss_music_apple | music/apple.ogg《苹果爵士唱片》108 BPM |
| boss_music_apple_p2 | music/apple_p2.ogg《苹果爵士唱片·虫蛀暴走》132 BPM |
| boss_music_manor_epic | music/manor_epic.ogg 庄园十人史诗曲（约 20 分钟，整场不切） |
| boss_music_manor_cue | music/manor_cue.ogg《庄园·序》演出曲 |
| boss_music_manor_battle | music/manor_battle.ogg《庄园·战》战斗曲 |
| boss_music_overture | music/theatre_overture.ogg 第一幕·序幕 51.43 s |
| boss_music_masquerade | music/theatre_masquerade.ogg 第二幕·假面圆舞 51.43 s |
| boss_music_finale | music/theatre_finale.ogg 第三幕·断线狂宴 51.43 s |
| boss_music_curtain | music/theatre_curtain.ogg 落幕钟声 21.40 s，真死时一次 |

- 另有 **15 个唱片专用事件**，各自单独注册（不复用 BGM 事件）：庄园 4 张（`music_disc_manor_cue` / `manor_battle` / `manor_dual` / `manor_guignol`）+ 食物系 10 张（`music_disc_apple` / `apple_p2` / `gingerbread` / `butcher` / `saya` / `tavern` / `rat` / `defect` / `compost` / `compost_p2`）+ 边牧犬王 1 张（`music_disc_benger`），它们与同名 BGM **共用音频文件**（`manor_dual` 是坦克版与真吉他版各 0.5 叠放）。
- 只有 `boss_music_*` 前缀会被客户端 `BossMusicClient` 拦截并压制原版背景音乐。**v110 更新**：`boss_music_apple` 现在参与曲目组调度（`BossMusic.Group.APPLE`，苹果爵士一阶段），`boss_music_apple_p2` 与 `boss_music_compost_p2` 仍由实体自己直接播放，但已加入 `BossMusic.allTrackEvents()` 的"停止时全停"名单（此前漏了，Boss 死后这两首会继续响）。
- 三幕剧与落幕钟声的秒数写在 `FTSounds` 注释里；其余曲目秒数没有统一常量（部分（代码里未找到 ?））。
