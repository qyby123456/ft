# 剑体构筑扩展接口 v2 与铁剑试案

2026-10-13。主工程 `D:/ft工程文件/FT-SugarQueen-Boss-Mod`；Forge 1.20.1。仅剑，弓/法杖仍暂停，不修改 NeoForge。

## 本轮实际范围

- 默认仅 minecraft:iron_sword 启用五功能试案。其他剑继续现有结构检查，不自动套用铁剑阈值、槽数或食品平衡。
- 铁剑初始9个可编辑格，四次进化依次11、13、15、17；27是画布，不是铁剑上限。永久锁格与暂锁格都不能放入。
- 初始只显示两铁锭、一木棍及各一件圆石、燧石、线。两铁锭严格等价原配方成本，三件环境结构是有限主题参考。
- 原生参考贡献基础功能，但不能取出成为物品、不满足付费羁绊。清除参考永久降低功能，后续打开界面不会重新生成。
- 每格最多一件。玩家放入的是实物，原版式拿放、右键、Shift、拖拽与数字键逻辑由标准容器承接；正式实机操作验收仍须进行。
- “不必保留指定材料”不等于“空剑可用”：手柄、护臂、核心、稳固、剑刃均需达到阈值。不足时攻击/材料被动/羁绊停用，但仍能编辑补装。
- 格子基值只在该格解锁且被有效材料占据时提供，约比材料贡献小一个量级。总值为格子基值加材料值乘格子五维权重。
- 进化倒退后锁格不删除原实物；允许取出，不允许放入/替换，且锁格材料不贡献功能、属性与羁绊。

## 五项功能和初始格

SwordFunctionRules 是无 Minecraft 注册副作用的纯规则；Values 按 HANDLE、GUARD、CORE、STABILITY、BLADE 顺序。

| 项目 | 最低值 |
| --- | ---: |
| 手柄 | 1.25 |
| 护臂 | 1.33 |
| 核心 | 1.38 |
| 稳固 | 3.10 |
| 剑刃 | 2.13 |

这些是本模组铁剑试案值，不是胸腔模组原值。默认六件略高于各项最低值；拆掉关键材料会至少损失一项。可用其他材料组合补足，不校验“固定物品必须在固定槽”。

逻辑格索引保持既有 NBT Slot 名，不因显示位置变化迁移：

- 初始：2、3、13、14、15、16、17、6、20。
- 进化1增加7、21；进化2增加8、22；进化3增加9、23；进化4增加10、24。
- 初始实物参考：2木棍、3线、13圆石、14与15铁锭、16燧石。其余初始格为空。
- 旧18铁粒加木棍的原生预算只读等价显示为新六件，不覆写旧账本。付费旧件不被合并或删除；锁格中仍能取回。

## 编辑格子、阈值与配方

SwordCavityExtensions.API_VERSION=2，第一版的布局/配方/羁绊函数仍保留。

- registerLayout(weaponId, positions)：27个逻辑索引到0..26显示位置的唯一排列，只改变显示，不改变格子身份或功能权重。
- registerFunctions(weaponId, profile)：为其他等级/模组/Boss武器注册五维最低值、27格权重、占用基值、解锁等级及材料/物品向量。
- Profile.unlockLevels：0初始开放、1..4进化开放、-1永久锁。capacity()/maximumCells 返回最终上限；其他高级武器可设置较高阈值和更多格。
- Profile.withItemValues：返回防御复制的新配置，不修改原配置。各项值必须有限非负，格子/材料输入上限64、最低值上限1024。
- registerNativeRecipe(weaponId, recipeId)：优先选游戏 RecipeManager 中实际存在且产出该剑一件的配方；不存在或不匹配则按原规则回退。
- 不接受客户端自报配方成本、进化次数或临时开放格。旧原生快照优先于重新读取，避免数据包变化给旧剑凭空补料。
- 新武器原生构成可改 SwordNativeMaterialRules 的纯预算/布局入口；铁剑本轮新增 ironPrototype、ironRecipe、ironPrototypeLayout 三个入口。

Java 注册须在客户端/服务端共同初始化阶段完成，同一 ID 重复注册返回false而非静默覆盖；unregisterFunctions/布局/配方接口供开发期调整。未实现 JSON 热重载、自动跨端同步或全武器自动平衡。

## 材料与食品元数据

SwordMaterialDefinition 和 SwordMaterialExtensions 是公开 API。

| 元数据 | 来源和规则 |
| --- | --- |
| itemId、namespace | 实际注册 ID；namespace 从 ID 推导 |
| baseMaterial | 既有 Material 的兼容倍率桥，不能为 ORIGINAL；独立 functions 决定五项基础值 |
| categories | METAL、FOOD、BOSS、ENVIRONMENT、WOOD、STONE、GEM，可组合 |
| functions | 非负有限五维值，显式注册可以覆盖默认物品 |
| nutrition | 实际 FoodProperties.getNutrition；非食品为0 |
| saturationModifier | 食物的倍率，非实际饱和度增加量 |
| saturationGain | nutrition × saturationModifier × 2 |
| price | OptionalDouble；未知保持缺失，不依赖/修改 ftecon |
| processingComplexity | OptionalInt；未知保持缺失，不凭物品名猜加工复杂度 |
| attributes | 自定义 ResourceLocation 到非负有限值的防御复制 Map |

- register(definition)/unregister(itemId)/resolve(stack) 用于材料注册和只读查询。重复 ID 不覆盖；损坏工具/未知非食品拒绝。
- 原白名单金属、矿物、Boss材料继续可用，未重新加入已淘汰的锌/黄铜等材料。环境新增仅线与燧石；圆石与木棍沿用原白名单。
- 普通食品自动读取实际营养/饱和度，给少量握持、核心供能和稳固值，护臂/剑刃很低；不会仅因食品存在就赠送强技能。
- 原生环境参考和实装环境物品分来源，不能重复计入同一格，不能退款主题参考。
- 自定义材料 functions 在实际 NBT 评估中按 itemId 覆盖通用向量；baseMaterial 只承担旧属性/存档兼容桥。

## 羁绊与技能联动

既有12组 SwordCavityRules.Link 及 v1 registerBond(Material数量, Build加值, 文本) 保留。新增 SwordMaterialExtensions.BondDefinition 支持多维条件。

- Requirement 可同时要求类别、namespace、单件数、最低营养/饱和度、最低价格/加工复杂度、自定义属性、五功能材料值。
- 缺价格/复杂度的物品不会满足设置了该门槛的条件，未知不等于零。
- 只数解锁格中真实付费单件；原生参考/旧无实物ghost/锁格不满足羁绊。五基础功能不足时所有新羁绊停用。
- 多个 Requirement 取 AND；各条件独立计数，同一物品可满足多个条件。必须要求不同部件时应选不相交类别，或由技能侧显式校验。
- bonuses 是五种战斗属性的加法变化，最终仍受现有上下限保护。
- effectIds/hasBond/activeEffectIds 仅提供查询桥；其他制作者可以在已有 Forge 技能/被动事件读取 ID。API 不会自行无冷却施法、召唤或触发联网动作。
- 本轮实际示范“食铁共生”：铁剑两件付费金属加一份营养≥4、饱和度增益≥1的付费食品，在功能达标后稍减磨损；原生铁锭不计。其余复杂被动与联动技能须逐条注册并实机验证，未宣称全做完。

## UI 与迁移

主面板196×208，右侧独立羁绊栏118×208，间隔6，总宽320；背包格距18、图标16，不整体缩放。下方仅五个“柄/护/核/稳/刃”加减状态与当前格/最终格；详细基础值/最低值悬停显示。主面板不放长中文说明，材料工具提示写作用，不显示伤害/攻速变化大表。

右栏单独列出已生效的内置羁绊、v1自定义羁绊及多维材料羁绊的名称和效果，说明自动换行、裁切并可滚轮查看。羁绊数只在右栏标题显示，无羁绊时显示“—”。羁绊列表仅描述实际已激活的效果，不将未实现的联动技能当作已有功能。

容器分离完整性与可用性；containerEditable 只检查可靠存档、耐久未耗尽、Revision与真实Broken标记。NBT INVALID/未知版本仍拒绝编辑。材料改变保留耐久比例，不免费满修；容器提交故障回滚完整武器NBT/Revision/Damage。耗尽与曾进化的修复/禁经验修补逻辑保持已部署版本，不把功能缺失永久写入Broken。

## 胸腔参考与验收边界

参考已缓存ChestCavity2.17.2.1实际器官JSON及javap：心提供health、肺提供恢复/容量/耐力，脊柱提供神经/防御；功能由材料累计，不绑默认槽或具体器官名。胸腔同时有硬门槛和渐进退化，剑的五项AND阈值是本模组设计，不冒充其原公式。剑槽始终单件，不抄胸腔满堆叠分母。

验证包括纯规则、真实NBT加显式物品/玩家桥接、严格类/成员/Handle链接、ZIP、方法级和无关条目保持。离线PASS不是Forge实机、GameTest、真实Mixin应用或部署成功。源码导出包含完整共享src与单独实际构建输入；完整共享src含暂停分支，不可直接整体build覆盖正式jar。
