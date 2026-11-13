# FT地牢怪物 · Forge 1.20.1 准备版

模型提供者：**一颗大豆，b站uid355533780**，仅标注四套骷髅模型提供身份。模组 ID：`ftdungeonmobs`，版本：`0.19.1`。

## 0.19.1 火炬难度仅作用于地牢维度（2026-11-02）

- 取消附近火盆的怪物半径加成；仅整个 `ftdungeonmobs:dungeon` 使用统一难度，主世界、下界及其它维度的周边怪物不受影响。
- 地牢内未锁定时可直接调节全维度的自动 FT 怪物；入口控制、五分钟入场与本轮锁定保持，手动固定档位仍优先。
- 保留血量比例、单体加成、存档和旧 API；旧 `arenaTorch.radius` 仅兼容读取，不再控制难度范围。细则见 `DIMENSION_DIFFICULTY.md`。

## 0.19.0 地面靠近刷怪点（2026-11-02）

- 地牢原两间场景和九间主题房接入“唤骨符印”：每轮在房间入口可达、干燥且有足够净高的地面随机布点，不覆盖建筑、容器和玩家改建，不放到隔墙密室或不可达屋顶。
- 生存/冒险玩家进入房间、接近 6 格并有视线时触发；每点预先保存固定的 1～3 只战士、弓手或法师，每轮只生成这一组。触发后变为熄灭符印，重进、读档、杀怪不重新刷；阻挡/事件取消只重试未生成的剩余部分，不重抽怪物组。
- 复用入口火炬锁定难度：1～5 档每房目标分别为 2/4/6/8/10 点。小房间或安全位置不足时少放，不强行挖地。怪物采用本轮固定属性档位；下一轮才清理旧点位和本系统上一轮怪物，重新随机布点，房间建筑不重置。
- 配置：`config/ftdungeonmobs-spawns.toml`；管理员 `/ftdungeon spawns status` 查看本轮。无新增方块实体或逐点 ticker；详细规则、配置和边界见 `SPAWN_POINTS.md`。

## 0.18.0 第一批地牢建筑方块（2026-11-01）

- 三条独立进程制作线并行完成 24 款：八种墙地材、八种柱拱构件、八种装饰。含深砌砖、裂纹/腐苔砖、血槽、骨纹/符印、楼梯台阶、柱座柱身柱冠、左右拱券/楔石、扶壁城垛、囚栏锁环、铁刺残骨、石棺与两种灯和封印座。
- 22 种自定义几何共 95 个整数长方体/500 面，另两种复用原版楼梯/台阶；单件最多 13 体块，不新增方块实体、ticker、自定义渲染器或动画副本。全部有合成、掉落、中英文、创造栏及分组 bbmodel。
- 只读研究 Goety/Ars Nouveau 解包结构，几何与 128×64 像素图集重新制作；不搬用原素材，不自动替换既有房间或改变血池/门的规则。拼装、获取、成本和边界见 `DUNGEON_ARCHITECTURE.md`。

## 0.17.0 新地牢房间接入（2026-11-01）

- 原样内置用户提供的 `31.nbt` 与 `30_1.nbt`，不修改原文件；成功从既有入口进入 FT 地牢后，安全检查并一次性接出南侧大型场景与东侧紫晶侧区。已有大厅、入口/返程坐标、门动画、五分钟窗口与五档难度规则保持不变。
- 两份完整包围盒及连接区均须为空，已有建筑、液体或实体会阻止本次扩建，不清空旧地形，也不阻止已经成功的入场；原大厅仍可使用。增加外置挡水边界和连接阶梯，不改模板内部方块或空容器。
- 在其东侧新增九间主题房 L1/L2/M1/M2/S1/S2/L3/L4/S3，三列三行固定槽位、三格宽共享道路、逐房持久指纹；支持追加房间而不重建旧房，逐房安全失败不影响已生成区域。原生验证北入口/内部可走、全部状态、灯与植物支撑及无外溢液体。
- OP2 可执行 `/ftdungeon rooms status` 与 `/ftdungeon rooms build`；生成记录独立持久化，不按每轮重建，不恢复或覆盖玩家后续修改。31 号要求目标运行时有 Lootr；缺依赖拒绝施工，不将箱子静默替成空气。
- 本次只接建筑，不添加刷怪、波次、奖励或启用 FT 地牢挑战扩展；已知返程门占用误报未在本轮修改。位置、空气遮罩、隔离测试与回退注意见 `ROOM_IMPORT.md`。

## 0.16.0 血祭血池建筑套件（2026-11-01）

- 按指定血池参考图新制九种整数长方体建筑部件：血痕黑石砖、符文石、直沿、阶梯转角、方颅浮雕石、方颅祭像、楼梯、台阶、献祭台。配方、掉落、创造栏、中英文与九份分组 bbmodel 已接入；不覆盖既有门、火盆和骷髅美术。
- 新增可流动、可装桶的真实 Forge 祭祀血液，16 帧像素动画，无无限源复制。管理员可执行 `/ftbloodpool build ~ ~1 ~` 安全生成面向自己的 9×9 阶梯八边血池及入口；全量占用预检，不挖掘旧建筑，不改写已有世界。
- 44 血液源与池沿/中央祭像构成触发结构；献祭累计 16 点后必须空手右键确认，输出 200 tick 的 15 级红石并发布可取消 Forge 事件，随后冷却 600 tick。支持供品掉落收集、比较器与存档，不自动启用波次/奖励扩展。
- 完整搭建图、配方、标签、触发接口、成本与待实机清单见 `BLOOD_POOL.md`。既有返程门占用 P1 此次未改动，见 `VERIFICATION.md`，不把五分钟多人入场视为已修复。

## 历史：0.15.0 火盆联动、门色与五分钟入场（2026-11-01）

- 右键火盆循环熄灭→1～5→熄灭；入口以 6 格内最近火盆为控制器（包括熄灭态），熄灭关门、点燃保持开门，切换 1～5 档不再反复关门。传送雾色同步苍蓝、翠绿、鎏金、紫焰、猩红，门框、铁闸和暖焰不染色。
- 第一名玩家成功传送后锁定本轮入口火盆和难度，开启 6000 服务端游戏 tick（正常 20 TPS 下 5 分钟）的队友入场窗口；后续入场不延时。到期立即拒绝新传送并启动关门，返程门保持开启。窗口结束且本轮所有参与者离开地牢后解锁，离线但仍在地牢的玩家不算已离开。
- 锁定覆盖门口 6 格内火盆的普通/潜行右键和服务端改档 API，并保护火盆及底座免受玩家挖掘、爆炸和活塞移动；不拦截有管理权限的任意世界编辑命令。入口壁灯改为代操作最近火盆，不能绕过锁定；返程壁灯不关闭出口。
- 原 2 秒动画、防夹、往返位置、模型几何及前置依赖不变；只为中性传送雾增加原生 tint，不增加五套模型或五套纹理。完整规则、存档/停机计时与待实机清单见 `DUNGEON_GATES.md`。

## 历史：0.14.0 火炬交互开关门（2026-11-01）

- 入口门和返程门均有 40 tick（2 秒）铁闸下沉开启、升起关闭动画；右键门上自带壁灯或 6 格内最近门旁的血棘祭炉即可开关，无需压力板。门槛符牌只作装饰。
- 火盆默认熄灭；门旁普通右键只开关门，首次开门会把未点燃火盆设为 1 档，已有档位不改变。空手潜行右键独立调档：熄灭→1→2→3→4→5→熄灭。附近无门时保留普通右键调档。
- 铁闸完全打开后走入门洞或右键非壁灯门框才传送；关闭或运动中不传送。关闭时门洞有生物则拒绝关门，关闭途中有人进入则反向打开。支持中途反向、保存和联机状态同步；不挖地下机井、不修改门下方块。
- 运行只增加每门一个锚点方块实体和一套共享运动几何，不复制逐帧模型；固定框与运动铁闸分别剔除隐藏面，保持纯整数 Cuboid 源模型。包含并行完成的火盆未开启外观，未恢复旧 DSH 火炬素材。操作、成本与待实机清单见 `DUNGEON_GATES.md`。

## 历史：0.13.1 参考图石砖铁闸大门（2026-11-01）

- 按 `D:\ft工程文件\image2.5\dungeon-gate-concept-20261101\out\` 的五张参考图重做两扇门：错缝暗石砖、苔痕、门楣方颅、阶梯方绞盘、矩形环链、七根尖齿闸条、铆钉横梁、暖焰壁灯与门内冷雾。入口附血痕和血祭符，返程附冷蓝路符。
- 保持 3×4 占位、原方块 ID、十二部件与四向状态，以及所有传送、个人返回记录、入口火盆调难度逻辑；不扫描或改写已保存世界。此轮只改外观，不实现参考图中的下沉闸门、地下机井或压力板开关。
- 仅使用整数长方体，绞盘和尖齿用阶梯近似，不使用圆柱、楔形、曲面或透明卡片。编辑源和运行模型按同一整数网格分割并保留分组；成本与验收见 `DUNGEON_GATES.md`。

## 历史：0.13.0 地牢往返大门与入口火盆难度（2026-11-01）

- 新增两扇 3 格宽、4 格高的整数方块门：`dungeon_entry_gate` 血祭入口、`dungeon_return_gate` 冷蓝返程，创造栏与合成均可获取。
- 新增 `ftdungeonmobs:dungeon` 独立维度及首次落地大厅，自动放置返程门；各玩家保存各自的返回维度/位置，有安全落点检查、冷却与普通玩家自救指令 `/ftdungeonreturn`。
- 入口 6 格内最近火盆的档位实时作用于整个地牢的自动分级 FT 怪物；保留手动锁定档位、已有配置和生命比例。共享维度有在线玩家时不能被其它入口抢绑，入口卸载/火盆移除则保留最近有效难度。
- 两种门只有正体积长方体，原版模型与动画纹理渲染，不新增门方块实体；旧火炬退役保持不变。维度、大门、控制规则与实机验收说明见 `DUNGEON_GATES.md`。
- 新维度首次使用需完整重启游戏/服务器；不自动启用独立的 FT 地牢挑战扩展，不承诺随机地牢或波次奖励系统。

## 历史：0.12.0 血棘祭炉五档变色，移除旧 DSH 火炬（2026-11-01）

- 血棘祭炉火焰随难度切换：1 苍蓝 / 2 翠绿 / 3 鎏金 / 4 紫焰 / 5 猩红；提示文本颜色与火焰一致。黑铁盆体、骨饰、血痕、43 个整数长方体与 UV 保持不变。
- 共用一份几何和本体贴图，四个材质继承模型加原猩红模型组成五档；所有火焰仍为 16 帧、32 tick 循环，无平面卡、无新增方块实体。
- 旧长矛火炬的几何、动画、六张贴图、三份自定义渲染代码、模型源及生成工具已移出活动工程和运行包，旧配方与创造栏入口取消；历史备份不作为运行资源。
- `arena_torch` 的方块、物品与惰性 BE 类型仅留作存档兼容：旧下半部显示新祭炉、旧上半部无模型/碰撞；旧物品摆放和旧方块掉落均为新的单格祭炉。旧摆放建议挖下后重放，清除历史双格占位；不扫描或改写玩家世界。
- 获取与实机检查见 `BLOODTHORN_BRAZIER.md`；单独的 FT 地牢挑战扩展继续保持禁用。

## 历史：0.11.0 第二种火盆：血棘祭炉（2026-11-01）

- 新增独立方块与物品 `ftdungeonmobs:bloodthorn_brazier`；原竞技场长矛火炬的 ID、模型、骨骼动画和存档格式保持不变。
- 血棘祭炉为一格高的黑铁、骨饰、血痕与阶梯血焰模型：43 个长方体、217 面；坐标与尺寸均为 0～16 内整数，无旋转、曲面或火焰平面卡。
- 加入 FT 地牢怪物和原版功能方块创造栏，支持合成、挖掘掉落、14 级光照；16 帧血焰纹理以 1.6 秒循环，不增加方块实体或自定义渲染器。
- 右键循环 1～5 档，沿用局部难度半径、最近火盆优先及存档索引；血焰不换色，档位由右键提示显示。使用方式见 `BLOODTHORN_BRAZIER.md`。
- 只接入已启用的 FT 地牢怪物；已禁用的 FT 地牢挑战扩展不修改、不启用，不新增自然生成或地牢自动替换。

## 历史：0.10.0 火炬 v8：GeckoLib 4.8.4 骨骼动画版（2026-11-01）

- 用户第八轮要求：火盆升级为 **GeckoLib 骨骼动画版**（idle 动画）。
- 形态：**2 格 BaseEntityBlock + BE（lower）+ GeckoLib 4.8.4 渲染**；blockstates 保留 HALF+tier **10 variants**（→空占位模型，渲染由 BE 提供）；旧 v7 静态 17 项（7 block 模型 + 5 火焰 + 5 mcmeta）已清理。
- 资源：`geo/arena_brazier.geo.json`（**16 bones / 59 cubes / 354 faces**，整数网格 0 违规）、`animations/arena_brazier.animation.json`（`animation.arena_brazier.idle`，loop，**2.0s**）；纹理 `arena_torch_body.png` + `arena_torch_body_t1..t5.png`（均 **64×64**，火焰烘焙在 body 图集 flame tile，5 档换色，body=t3 兼容副本）；item 静态模型（59 元素/display 7/ymax 32/无私有字段）。
- **Java 7 文件**（geckolib 实现）：改 `ArenaTorchBlock.java`（→BaseEntityBlock+HALF/TIER+ENTITYBLOCK_ANIMATED+lower-only BE+粒子高度）、`FTDungeonMobs.java`（BLOCK_ENTITY_TYPES）、`client/DungeonClient.java`（注册 BE renderer + cutout）；新 `ArenaTorchBlockEntity.java`、`client/ArenaTorchModel.java`、`client/ArenaTorchRenderer.java`、`client/ArenaTorchEmissiveLayer.java`；其余 12 Java 未动。
- 方块/物品 ID、HALF+tier blockstate、右键 1→2→3→4→5→1、难度接口、配方/loot/语言键不变；旧存档 blockstate 兼容。
- 构建、真实 GeckoLib 解析/烘焙（`ARENA_ASSETS_V8_VERIFIED`：geo/anim/纹理/blockstates/item/旧静态清理/Java BE 注册）+ 骷髅线 GeckoLib 回归（4/218/17）+ 部署核对均通过；未启动完整整合包客户端，构建/解析器/部署校验 ≠ 实机画面验收。部署、回滚（七级）与证据见 `DEPLOYMENT.md`、`VERIFICATION.md`。

## 安装

1. 使用 Minecraft **1.20.1**、Forge **47.4.10 或同系列更新版本**、Java **17**。
2. 客户端与服务端的 `mods/` 均安装本模组、**GeckoLib Forge 1.20.1 4.8.4**、**Iron's Spells 'n Spellbooks 1.20.1-3.16.3**，以及铁魔法的 Curios、Player Animator、Iron's Lib 前置；每个模组只保留一份。
3. 成品位于 `build/libs/ft-dungeon-mobs-forge-1.20.1-0.19.1.jar`。不要将源码 ZIP 当作模组安装；升级时只保留一份本模组。新增维度/液体/方块需完整重启，使用新内容的世界必须先备份再回退。
4. 此工程与现有 `ftboss` 等模组完全独立，不覆盖原模组。本机整合包的部署路径与校验记录见 `DEPLOYMENT.md`。

## 已接入内容

| 怪物 | 实体 ID | 保留动画 | 基础行为 |
| --- | --- | --- | --- |
| 骷髅战士 | `ftdungeonmobs:skeleton_warrior` | idle、walk、attack、animation2 | 原版铁剑近战伤害 |
| 骷髅弓手 | `ftdungeonmobs:skeleton_archer` | idle、walk、attack、animation2 | 原版弓箭弹射物伤害 |
| 骷髅法师 | `ftdungeonmobs:skeleton_mage` | idle、walk、attack、animation2 | 铁魔法原生魔法飞弹 |
| 骷髅王 | `ftdungeonmobs:skeleton_king` | idle、walk、attack、dash、summon | 原版钻石剑近战伤害 |

- 共四套原始贴图、655 个方块、218 根骨骼、17 段动画。
- 创造模式物品栏有独立的“FT地牢怪物”分类，图标为骷髅王刷怪蛋，四种蛋按战士、弓手、法师、王排列；同时加入原版“刷怪蛋”分类，支持物品搜索及原版刷怪笼。
- 待机、移动、攻击动画自动切换；攻击动作开始后，在动作中段判定伤害或发射箭矢，不是动作一开始便直接扣血。
- 中文、英文名称与基础骨头掉落已接入。使用不死生物属性，不依赖其他 FT 模组。
- 战士 / 弓手 / 法师 / 王的未加成基础生命值为 40 / 30 / 35 / 200；最终数值由难度档位、按怪物类型的配置和单体加成共同决定，默认普通难度使用二档、困难使用三档。这些默认梯度仍可自行调整，并非最终战斗平衡。
- 新增五档难度配置、管理员属性命令、持久化单体加成和公开 Java API，入口与完整示例见 `DIFFICULTY.md`。
- 单格“血棘祭炉”：右键按 熄灭→1→2→3→4→5→熄灭 切换地牢维度难度，火焰依次为苍蓝、翠绿、鎏金、紫焰、猩红；16 帧纹理动画、照明、创造栏和合成配方见 `BLOODTHORN_BRAZIER.md`。旧 DSH 火炬已退役。

## 血棘祭炉

在“FT地牢怪物”或原版“功能方块”创造栏找到，也可执行 `/give @s ftdungeonmobs:bloodthorn_brazier`。只需一格空间和完整底座，初始熄灭；右键调档。入口门 6 格内最近火盆决定开关、门色和下一轮难度，首人进入后锁定；不再给普通维度周边怪物施加难度；地牢内未锁定时可调整该维度所有自动 FT 怪物。不改变原版世界难度，手动锁定档位的怪物不受覆盖。多人游戏两端均需更新到 0.19.1，完整规则见 `DUNGEON_GATES.md`。旧 ID 仅用于存档兼容，不再提供旧火炬外观。

## 刷怪蛋与创造栏

进入创造模式，翻页找到“FT地牢怪物”分类，或在搜索栏输入“骷髅”。四种蛋使用原版刷怪蛋形状，以副色区分：

| 刷怪蛋 | 物品 ID | 副色 |
| --- | --- | --- |
| 骷髅战士刷怪蛋 | `ftdungeonmobs:skeleton_warrior_spawn_egg` | 钢灰 |
| 骷髅弓手刷怪蛋 | `ftdungeonmobs:skeleton_archer_spawn_egg` | 棕色 |
| 骷髅法师刷怪蛋 | `ftdungeonmobs:skeleton_mage_spawn_egg` | 紫色 |
| 骷髅王刷怪蛋 | `ftdungeonmobs:skeleton_king_spawn_egg` | 金色 |

右键地面生成对应怪物；右键原版刷怪笼可设置其实体类型。它们仍遵守原版的碰撞空间、难度和刷怪笼规则，不会强制绕过这些限制。

## 伤害系统

- **剑**：战士真实装备铁剑，骷髅王真实装备钻石剑，走原版 `Mob.doHurtTarget` 与 `mob_attack` 伤害源。装备属性、锋利/亡灵杀手等武器附魔、击退、火焰附加，以及目标的盔甲、防御、受伤事件均按原版/Forge 结算。默认总近战攻击值分别为 6 / 10，不将武器面板伤害重复叠加两次；可通过实体 `HandItems` 配置武器和附魔，默认不额外掉落装备。
- **弓**：弓手真实装备弓，实现 `RangedAttackMob`，使用 `ProjectileUtil.getMobArrow` 和 `BowItem.customArrow` 生成实际箭矢。支持力量、冲击、火矢以及副手持有的药箭；没有直接对目标扣血，也不使用近战攻击属性去覆盖箭矢原生伤害。
- **铁魔法**：法师发射 Iron's Spells 'n Spellbooks 原生 `MagicMissileProjectile`，伤害与原版该法术一致，使用一级魔法飞弹 `getSpellPower(1, caster) × 0.5`。命中后由铁魔法自己的 `SpellDamageSource` 和 `DamageSources.applyDamage` 处理末影学派、法术强度、抗性、击杀归属和相关事件；不再使用通用的 `indirectMagic` 瞬间伤害。飞弹是实际弹射物，能被方块挡住并可躲避，法术被铁魔法配置禁用时不会绕过禁用强制发射。
- **受到攻击**：四类怪物均保留原生 `LivingEntity.hurt` 处理，玩家剑击、箭矢与铁魔法法术能正常打到它们；没有仅允许某一种伤害的白名单，也没有绕过原版受伤无敌帧、直接修改生命值或重复扣血。
- 模型自带武器保留原样；额外装备仅参与伤害结算，不叠加第二套手持物模型。
- 梯度攻击倍率只在原生攻击属性上应用一次，剑不会重复计算武器伤害；箭矢在原生箭与弓扩展处理后按攻击属性比例缩放基础伤害，仍保留药箭、附魔、命中与伤害事件链。魔法护甲使用铁魔法原生 `spell_resist`，法术加成使用 `spell_power`，不额外拦截伤害进行二次减伤或二次增伤。

## 难度与属性入口

编辑游戏目录的 `config/ftdungeonmobs-common.toml`，可分别调整五档梯度和四类怪物的生命、攻击、物理护甲、铁魔法魔抗与法强。保存后执行 `/ftdungeon difficulty reload`，从文件重新加载并立即重算所有已加载的本模组怪物；也支持 Forge 配置重载事件，事件生效后实体通常在 20 个游戏刻内更新。`refresh` 只按当前已加载的配置重算，不保证从文件重读。

```mcfunction
/ftdungeon difficulty get @e[type=ftdungeonmobs:skeleton_warrior,limit=1,sort=nearest]
/ftdungeon difficulty set @e[type=ftdungeonmobs:skeleton_warrior,limit=1,sort=nearest] 4
/ftdungeon difficulty bonus @e[type=ftdungeonmobs:skeleton_warrior,limit=1,sort=nearest] health 1.25
/ftdungeon difficulty auto @e[type=ftdungeonmobs:skeleton_warrior,limit=1,sort=nearest]
```

命令需要管理员权限 2。Java 接口为 `net.farmingtales.ftdungeonmobs.DungeonDifficultyApi`，可供后续地牢层数、任务和刷怪器分配难度，调用须在逻辑服务端线程进行。改档及刷新保留当前剩余血量比例；固定档位与单体加成随实体 NBT 保存，不随世界难度变化丢失。完整配置表、参数含义、清除加成和 API 使用方式见 `DIFFICULTY.md`。

## 生成与动画预览

需开启作弊或具有管理员权限。

```mcfunction
/summon ftdungeonmobs:skeleton_warrior ~ ~ ~
/summon ftdungeonmobs:skeleton_archer ~ ~ ~
/summon ftdungeonmobs:skeleton_mage ~ ~ ~
/summon ftdungeonmobs:skeleton_king ~ ~ ~
/give @s ftdungeonmobs:skeleton_warrior_spawn_egg
/give @s ftdungeonmobs:skeleton_archer_spawn_egg
/give @s ftdungeonmobs:skeleton_mage_spawn_egg
/give @s ftdungeonmobs:skeleton_king_spawn_egg
/ftdungeon animation @e[type=ftdungeonmobs:skeleton_king,limit=1,sort=nearest] dash
/ftdungeon animation @e[type=ftdungeonmobs:skeleton_king,limit=1,sort=nearest] summon
/ftdungeon animation @e[type=ftdungeonmobs:skeleton_warrior,limit=1,sort=nearest] animation2
```

预览命令支持 `idle`、`walk`、`attack`、`dash`、`summon`、`animation2`；只对原模型实际拥有对应动画的实体生效。播放一遍后恢复正常，预览动作本身不产生伤害、不生成召唤物。`animation2` 是原模型的未命名辅助动画，未擅自定义为死亡、受击或其他战斗行为。

## 准备版边界

- 没有增加普通世界自然生成规则；除刷怪蛋、`/summon`、刷怪笼外，FT 地牢每轮会在已安装房间布置一次性靠近刷怪点。
- 已接入独立地牢维度、往返门、初始大厅与可手动搭建的血池套件；没有随机地牢、战利品装备、任务或完整波次系统，不自动启用独立挑战扩展。
- 骷髅王的 `dash` / `summon` 目前是已完整接入的动画与预览接口，不代表已实现冲刺伤害或召唤技能。
- 后续可在 `DungeonSkeletonEntity` 接入正式技能与阶段机制，在 `SkeletonVariant` 调整数值。

## 素材保留与导出

`model_sources/` 保存四份未改动的原始文件及导出报告。运行资源由 Blockbench 的 GeckoLib 模型格式、原生 Bedrock 几何与动画导出器生成，没有将动画烘焙成静态姿势。

原工程包含少量已删除骨骼的残留动画通道（战士 1 个、法师 7 个、王 1 个）。仅在运行资源中移除了这些没有对应骨骼、无法产生可见运动的通道；没有删除任何有效骨骼、方块或动画片段。清单见 `model_sources/export-report.json`。

模型提供者署名与素材原始来源声明分别保留，见 `CREDITS.md`；没有将模型提供者写成模组代码作者。

## 构建和验证

Windows PowerShell：

```powershell
node .\tools\verify-assets.mjs
.\BUILD.ps1 -Task build
.\BUILD.ps1 -Task runData
```

`BUILD.ps1` 自动使用当前电脑的 Minecraft Java 17；在其他电脑上可先设置 `$env:JAVA_HOME_17`。Gradle Wrapper、GeckoLib 及铁魔法相关的开发依赖保存在本工程内；这些前置不会被合并进模组 JAR，第一次完整构建仍需能下载 Forge/Gradle 依赖。缓存齐全后可使用 `-Offline`，避免联网探测阻塞。

`build` 中的 `verifyAssets` 会调用 **GeckoLib 4.8.4 实际解析器和模型烘焙器**读取所有几何与动画。Node 校验脚本另外核对方块、骨骼、动画名、时长、贴图原始字节、语言文件和刷怪蛋资源，报告写入 `build/reports/imported-assets.json`。`runData` 用于在独立开发目录进行 Forge 模组加载验证，不修改任何玩家存档，也不替玩家同意服务器 EULA。

完整游戏客户端内的渲染、联网和战斗效果仍需实机验收；不要把资源检查或数据生成器启动等同于已经完成整合包内联机实测。
