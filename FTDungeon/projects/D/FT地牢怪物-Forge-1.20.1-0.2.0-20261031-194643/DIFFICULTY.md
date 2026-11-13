# 自定义怪物属性与梯度接口 · 0.2.0

## 三种控制入口

1. **配置文件**：游戏或独立服务端目录下的 `config/ftdungeonmobs-common.toml`。Forge 首次加载模组时创建；本机部署同时提供初始配置。只影响 FT 地牢怪物，不修改玩家、原版怪物或其他模组实体。
2. **管理员命令**：`/ftdungeon difficulty`，权限等级 2，可用选择器批量操作指定怪物。
3. **Java API**：`net.farmingtales.ftdungeonmobs.DungeonDifficultyApi`，供地牢、任务、层数与刷怪器代码分配难度和单体加成。

这版提供配置与命令入口，没有新增客户端属性编辑 GUI。配置采用 COMMON 类型，独立服务端以服务端配置为准，单人游戏以本机集成服务端配置为准；客户端本地修改不会覆盖远程服务端的怪物属性。

## 默认五档梯度

| 档位 | 最大生命倍率 | 攻击倍率 | 物理护甲加值 | 铁魔法魔抗加值 | 法强倍率 |
| --- | --- | --- | --- | --- | --- |
| 1 | 1.00 | 1.00 | 0 | 0.00 | 1.00 |
| 2 | 1.50 | 1.20 | 2 | 0.10 | 1.20 |
| 3 | 2.00 | 1.50 | 4 | 0.20 | 1.50 |
| 4 | 3.00 | 1.85 | 7 | 0.35 | 1.85 |
| 5 | 4.00 | 2.25 | 10 | 0.50 | 2.25 |

默认 `general.useWorldDifficulty = true`：简单 → 一档，普通 → 二档，困难 → 三档。四、五档留给更高层地牢和指定精英，也可以把世界难度映射改成四、五档。和平模式仍受原版敌对生物消失规则约束，不因本配置强制保留怪物。

若不想跟随原版难度，将 `useWorldDifficulty` 设为 `false`，然后配置 `defaultTier`。通过命令或 API 显式分配的固定档位优先，不被世界难度和默认档位覆盖。

## 配置结构

以下为完整配置中的部分段落；同一个 TOML 表只写一次，请直接修改现有对应字段，不要在文件末尾重复粘贴同名表。

```toml
[general]
useWorldDifficulty = true
defaultTier = 1
peacefulTier = 1
easyTier = 1
normalTier = 2
hardTier = 3

[tiers.tier4]
healthMultiplier = 3.0
attackMultiplier = 1.85
armorBonus = 7.0
magicArmorBonus = 0.35
spellPowerMultiplier = 1.85

[mobs.skeleton_king]
healthMultiplier = 1.25
attackMultiplier = 1.1
armorBonus = 3.0
magicArmorBonus = 0.1
spellPowerMultiplier = 1.2
```

可修改的怪物段为 `mobs.skeleton_warrior`、`mobs.skeleton_archer`、`mobs.skeleton_mage`、`mobs.skeleton_king`。默认按类型的调整均为倍率 1、加值 0。

### 五项属性的准确含义

| 字段 / 命令属性名 | 类型 | 接入属性与规则 |
| --- | --- | --- |
| `healthMultiplier` / `health` | 倍率 | `minecraft:generic.max_health`，不是直接写死基础血量 |
| `attackMultiplier` / `attack` | 倍率 | `minecraft:generic.attack_damage`，包含剑装备伤害；弓另按当前攻击属性相对弓手基础值的比例缩放原生箭基础伤害一次 |
| `armorBonus` / `armor` | 加值 | `minecraft:generic.armor`，护甲点数，不是固定减伤百分比；仍遵守原版护甲、穿甲和伤害类型规则 |
| `magicArmorBonus` / `magic_armor` | 加值 | `irons_spellbooks:spell_resist`，原生基础值 1.0；这是铁魔法抗性属性，不是第二套物理护甲 |
| `spellPowerMultiplier` / `spell_power` | 倍率 | `irons_spellbooks:spell_power`，基础值 1.0；魔法飞弹直接通过铁魔法 `getSpellPower` 消费此属性，不额外乘第二次 |

三层配置依次组合：**难度档位 × 怪物类型 × 单体加成**。生命、攻击、法强的倍率相乘；物理护甲与魔抗的加值相加，再由原生属性系统与装备、药水及其他模组修正共同结算。

攻击倍率调整的是原生攻击属性，不是把每个最终伤害事件强制乘一个系数；原版近战附魔额外伤害等后续环节仍按原版规则处理，弓的药箭效果也不会被当作箭的基础伤害一起重写。

例如原始基础最大生命为 200 的骷髅王，四档倍率 3、类型倍率 1.25、单体倍率 1 时，未触及属性上限且没有其他修正的最大生命为 750。其他加成不会被清除或改写基础值。

**魔抗不是全局固定减伤率。** 铁魔法还会结合具体学派抗性与软上限；在其他抗性均为默认值时，原生魔抗 1.1 对应约 10% 减伤、1.5 对应约 50% 减伤，超过 1.5 后继续使用铁魔法自己的软上限公式。不会对铁魔法伤害再次手动减伤，也不宣称能抵抗所有其他模组的任意“魔法”伤害源。

配置和命令输入范围：生命倍率 `0.05~100`，攻击与法强倍率 `0~100`，物理护甲加值 `-30~30`，魔抗加值 `-0.9~99`。最终属性仍受 Minecraft、Forge、铁魔法及属性上限类模组的实际限制；超出上限时以引擎读到的值为准。负护甲或魔抗加值表示削弱，叠加负魔抗可能增加受到的铁魔法伤害。

## 管理员命令

`<targets>` 使用实体选择器、UUID 等原版实体参数。只操作其中的 FT 地牢怪物，选中玩家或其他模组怪物不会改变它们；未选中任何 FT 怪物时报错。下面命令中的尖括号是参数占位，不要照抄。

```text
/ftdungeon difficulty get <targets>
/ftdungeon difficulty set <targets> <1-5>
/ftdungeon difficulty auto <targets>
/ftdungeon difficulty bonus <targets> <health|attack|armor|magic_armor|spell_power> <value>
/ftdungeon difficulty clear_bonus <targets>
/ftdungeon difficulty reload
/ftdungeon difficulty refresh [<targets>]
```

- `get` 显示实际档位、自动/固定模式、剩余与最大生命、攻击、物理护甲、原生魔抗和法强。魔抗显示的是属性值，不伪装成最终减伤百分比。
- `set` 固定档位，保留该怪物已有单体加成。
- `auto` 恢复根据配置选择档位，仍保留单体加成。
- `bonus` **设置**指定单体倍率或加值，其他四项不变；重复设置同一数值不会再次累加。`health 1.25` 表示单体额外乘 1.25，不是把生命设为 1.25。
- `clear_bonus` 清除五项单体调整，保留当前自动/固定模式和档位，不清除配置中的梯度或类型加成。
- `reload` 明确从此模组的配置文件重新读取、校验，再立即刷新所有已加载怪物；文件语法或数值范围无效时报错，保留之前已应用的属性。推荐编辑文件后使用此命令，不必依赖系统文件监控是否及时触发。
- `refresh` 按当前已加载配置重算属性。不带选择器时处理所有已加载维度中的本模组怪物，不强制加载区块。它不是原版 `/reload` 的别名，也不会重读其他模组的配置。

可直接使用的例子：

```mcfunction
/ftdungeon difficulty set @e[type=ftdungeonmobs:skeleton_king,limit=1,sort=nearest] 5
/ftdungeon difficulty bonus @e[type=ftdungeonmobs:skeleton_king,limit=1,sort=nearest] health 1.25
/ftdungeon difficulty bonus @e[type=ftdungeonmobs:skeleton_king,limit=1,sort=nearest] magic_armor 0.1
/ftdungeon difficulty get @e[type=ftdungeonmobs:skeleton_king,limit=1,sort=nearest]
/summon ftdungeonmobs:skeleton_king ~ ~ ~ {FTDungeonDifficulty:{Tier:5,Automatic:0b}}
```

实体自己的档位和单体调整保存在 `FTDungeonDifficulty` NBT 中。亦可在召唤 NBT 的 `Overrides` 中提供 `HealthMultiplier`、`AttackMultiplier`、`ArmorBonus`、`MagicArmorBonus`、`SpellPowerMultiplier`；省略项按倍率 1、加值 0 处理。

## Java 接口

入口类：`net.farmingtales.ftdungeonmobs.DungeonDifficultyApi`。
参数实体类型：`net.farmingtales.ftdungeonmobs.DungeonSkeletonEntity`。
不可变加成类型：`net.farmingtales.ftdungeonmobs.DifficultyProfile`。

```java
DungeonDifficultyApi.setTier(monster, 4);
DungeonDifficultyApi.setOverrides(monster, new DifficultyProfile(1.25, 1.1, 3.0, 0.1, 1.2));
DungeonDifficultyApi.Snapshot stats = DungeonDifficultyApi.inspect(monster);
```

`DifficultyProfile` 构造参数依次为：生命倍率、攻击倍率、物理护甲加值、魔抗加值、法强倍率。`setOverrides` 替换全部五项单体调整；只改一项可用：

```java
DifficultyProfile existing = DungeonDifficultyApi.getOverrides(monster);
DungeonDifficultyApi.setOverrides(monster, existing.with("armor", 6.0));
```

其他公开方法：

| 方法 | 用途 |
| --- | --- |
| `getTier(monster)` | 读取并在必要时更新当前档位 |
| `getOverrides(monster)` | 获取单体调整，不包含配置中的档位与类型调整 |
| `inspect(monster)` | 获取档位、自动模式、组合后的 profile 与最终原生属性快照 |
| `useAutomaticTier(monster)` | 恢复自动选择档位，保留单体调整 |
| `clearOverrides(monster)` | 恢复单体倍率 1、加值 0 |
| `refresh(monster)` | 强制重算一个实体的当前配置 |
| `refreshAll(server)` | 重算所有已加载维度中的本模组实体，返回数量 |
| `reloadConfig(server)` | 从文件重新加载并校验配置，然后刷新所有已加载的本模组实体 |
| `DifficultyProfile.IDENTITY` | 不附加额外调整的 profile |
| `profile.combine(other)` | 按倍率相乘、加值相加组合两个 profile |

**调用线程与约束**：读写入口都应在逻辑服务端线程调用；不要从渲染线程、网络收包线程或异步地牢生成线程直接调用。异步任务应先通过 `server.execute(...)` 切回服务端线程。可以在实体加入世界前设置档位，只要实体属于正确的 `ServerLevel` 且调用线程正确。档位必须在 1~5；profile 拒绝非有限数值、非正生命倍率、负攻击倍率与负法强倍率；`setOverrides` 还要求单体输入与配置/命令的各项范围一致，以免极端数值在组合时溢出，原生属性上限仍生效。`onEntityJoin` 是内部生命周期监听，不应由外部模组手动触发。

其他 Forge 开发工程可将成品按标准名 `ft-dungeon-mobs-forge-1.20.1-0.2.0.jar` 放入其 `libs/`，通过 flatDir 本地 Maven 坐标配合 ForgeGradle `fg.deobf(...)` 作为依赖使用；游戏运行时仍需同时安装本模组及其前置。无需把本模组或铁魔法类复制到自己的 JAR。

## 热更新、存档与血量

- 编辑并保存 TOML 后，推荐执行 `/ftdungeon difficulty reload`，立即读取配置并更新已加载实体。若系统文件监控触发了 Forge 配置重载事件，已加载且存活的实体也会在接下来约 20 个游戏刻内更新；此时间从配置重载事件生效起算，而不是保证从文件保存时起算。`refresh` 只应用当前已经成功加载的配置。
- 未加载区块中的实体在下次加载时按最新配置重算，不强行加载整个世界。
- 切换档位、改加成与热更新均保留剩余生命比例，例如 50% 血量改成更高最大生命后仍为 50%，不免费回满，也不复活已死亡实体。
- 加成使用固定 UUID 的原生持久属性修饰符，重新计算时只替换本系统自己的修饰符，不重复叠加，也不抹掉装备、药水或其他模组修饰符。
- NBT 同时保留档位、单体调整与属性修饰符，使受伤的高血量实体在区块卸载、存档读取后不会因先被基础血量截断而错误回血。
- 现有 0.1.x 实体没有难度标签时，首次加载会采用自动模式；建议升级前备份存档。不会添加自然生成、地牢建筑或额外技能。
