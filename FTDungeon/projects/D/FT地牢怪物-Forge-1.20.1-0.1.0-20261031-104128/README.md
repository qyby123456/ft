# FT地牢怪物 · Forge 1.20.1 准备版

模型提供者：**一颗大豆，b站uid355533780**，仅标注模型提供身份。模组 ID：`ftdungeonmobs`，版本：`0.1.0`。

## 安装

1. 使用 Minecraft **1.20.1**、Forge **47.4.10 或同系列更新版本**、Java **17**。
2. 客户端与服务端的 `mods/` 均安装本模组、**GeckoLib Forge 1.20.1 4.8.4**、**Iron's Spells 'n Spellbooks 1.20.1-3.16.3**，以及铁魔法的 Curios、Player Animator、Iron's Lib 前置；每个模组只保留一份。
3. 成品位于 `build/libs/ft-dungeon-mobs-forge-1.20.1-0.1.0.jar`。不要将源码 ZIP 当作模组安装。
4. 此工程与现有 `ftboss` 等模组完全独立，没有覆盖原模组，也没有自动复制到整合包 `mods/`。

## 已接入内容

| 怪物 | 实体 ID | 保留动画 | 基础行为 |
| --- | --- | --- | --- |
| 骷髅战士 | `ftdungeonmobs:skeleton_warrior` | idle、walk、attack、animation2 | 原版铁剑近战伤害 |
| 骷髅弓手 | `ftdungeonmobs:skeleton_archer` | idle、walk、attack、animation2 | 原版弓箭弹射物伤害 |
| 骷髅法师 | `ftdungeonmobs:skeleton_mage` | idle、walk、attack、animation2 | 铁魔法原生魔法飞弹 |
| 骷髅王 | `ftdungeonmobs:skeleton_king` | idle、walk、attack、dash、summon | 原版钻石剑近战伤害 |

- 共四套原始贴图、655 个方块、218 根骨骼、17 段动画。
- 创造模式物品栏有独立的“FT地牢怪物”分类和四种刷怪蛋，支持原版刷怪笼。
- 待机、移动、攻击动画自动切换；攻击动作开始后，在动作中段判定伤害或发射箭矢，不是动作一开始便直接扣血。
- 中文、英文名称与基础骨头掉落已接入。使用不死生物属性，不依赖其他 FT 模组。
- 战士 / 弓手 / 法师 / 王的生命值暂为 40 / 30 / 35 / 200，均为方便验证的占位数值，并非最终战斗平衡。

## 伤害系统

- **剑**：战士真实装备铁剑，骷髅王真实装备钻石剑，走原版 `Mob.doHurtTarget` 与 `mob_attack` 伤害源。装备属性、锋利/亡灵杀手等武器附魔、击退、火焰附加，以及目标的盔甲、防御、受伤事件均按原版/Forge 结算。默认总近战攻击值分别为 6 / 10，不将武器面板伤害重复叠加两次；可通过实体 `HandItems` 配置武器和附魔，默认不额外掉落装备。
- **弓**：弓手真实装备弓，实现 `RangedAttackMob`，使用 `ProjectileUtil.getMobArrow` 和 `BowItem.customArrow` 生成实际箭矢。支持力量、冲击、火矢以及副手持有的药箭；没有直接对目标扣血，也不使用近战攻击属性去覆盖箭矢原生伤害。
- **铁魔法**：法师发射 Iron's Spells 'n Spellbooks 原生 `MagicMissileProjectile`，伤害与原版该法术一致，使用一级魔法飞弹 `getSpellPower(1, caster) × 0.5`。命中后由铁魔法自己的 `SpellDamageSource` 和 `DamageSources.applyDamage` 处理末影学派、法术强度、抗性、击杀归属和相关事件；不再使用通用的 `indirectMagic` 瞬间伤害。飞弹是实际弹射物，能被方块挡住并可躲避，法术被铁魔法配置禁用时不会绕过禁用强制发射。
- **受到攻击**：四类怪物均保留原生 `LivingEntity.hurt` 处理，玩家剑击、箭矢与铁魔法法术能正常打到它们；没有仅允许某一种伤害的白名单，也没有绕过原版受伤无敌帧、直接修改生命值或重复扣血。
- 模型自带武器保留原样；额外装备仅参与伤害结算，不叠加第二套手持物模型。

## 生成与动画预览

需开启作弊或具有管理员权限。

```mcfunction
/summon ftdungeonmobs:skeleton_warrior ~ ~ ~
/summon ftdungeonmobs:skeleton_archer ~ ~ ~
/summon ftdungeonmobs:skeleton_mage ~ ~ ~
/summon ftdungeonmobs:skeleton_king ~ ~ ~
/give @s ftdungeonmobs:skeleton_king_spawn_egg
/ftdungeon animation @e[type=ftdungeonmobs:skeleton_king,limit=1,sort=nearest] dash
/ftdungeon animation @e[type=ftdungeonmobs:skeleton_king,limit=1,sort=nearest] summon
/ftdungeon animation @e[type=ftdungeonmobs:skeleton_warrior,limit=1,sort=nearest] animation2
```

预览命令支持 `idle`、`walk`、`attack`、`dash`、`summon`、`animation2`；只对原模型实际拥有对应动画的实体生效。播放一遍后恢复正常，预览动作本身不产生伤害、不生成召唤物。`animation2` 是原模型的未命名辅助动画，未擅自定义为死亡、受击或其他战斗行为。

## 准备版边界

- 没有增加自然生成规则，不会自行刷满世界；通过刷怪蛋、`/summon`、刷怪笼等使用。
- 没有地牢建筑、维度、战利品装备、任务或刷怪器系统。
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
