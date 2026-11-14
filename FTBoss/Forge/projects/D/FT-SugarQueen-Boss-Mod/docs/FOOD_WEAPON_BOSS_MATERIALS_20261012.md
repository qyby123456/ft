# 食武 Boss 掉落材料与原版/灾变兼容（2026-10-12）

## 本轮范围

- 仅 ForgeⅡ 1.5.2。主工程改动位于 D:/ft工程文件/FT-SugarQueen-Boss-Mod；不修改 NeoForge、灾变自身 JAR、ftecon、旧 Boss 的攻击模型/法术/奖励语义。
- 活动材质共 35 类（含原装）；原有三部件配比、单次替换消耗 1 件原料、按耐久损耗比例保留且拒绝换材修复将断裂武器不变。
- 删除锌、黄铜、琥珀金、炽金的活动材质与供品映射；旧字符串 NBT 由未知材质回退到原装，不迁移或销毁物品。仍保留 Iron 的法术与魔法书秘银。
- 新增原版煤炭、青金石、红石、石英、紫水晶碎片，均按明确物品 ID 接入。矿石/粗矿/金粒/矿物块不替代锭或宝石；原来的石材、木材允许列表保持。

## 独立 Boss 材料

| 对应 Boss | 新原料 ID | 构筑材质 |
| --- | --- | --- |
| 糖霜女皇 | ftboss:frost_material | boss_frost |
| 肉山 | ftboss:butcher_material | boss_butcher |
| 茶树 | ftboss:tea_material | boss_tea |
| 酒桶 | ftboss:wine_material | boss_wine |
| 鼠群本体 | ftboss:cheese_material | boss_cheese |
| 缺陷核心 | ftboss:capacitor_material | boss_capacitor |
| 堆肥 | ftboss:spore_material | boss_spore |
| 金苹果 | ftboss:apple_material | boss_apple |
| 牧羊人 | ftboss:shepherd_material | boss_shepherd |
| 潮汐鱼 | ftboss:tide_material | boss_tide |

通过现有 BossRelicDrops.reward 的同一最终死亡结算新增固定 1 件材料，而不是新建独立死亡监听器。客户端、mobLoot 关闭、无玩家伤害来源且无玩家击杀归属、鼠群小兵、柠檬召唤，以及已结算实体均不得发放。LOWEST 事件且不接收取消事件，保留复活/假死的现有取消边界与 REWARDED 防重。女皇和肉山沿现有脚本死亡路径，其他 Boss 沿现有 LivingDropsEvent 路径；两个路径都保留原掉落/奖励箱路由。

材料为可堆叠 64、耐火、不可佩戴的独立物品。原遗物、随机词缀、女皇法杖和所有旧掉落不消耗也不替换。10 种 Boss 遗物不再属于构筑供品，潮汐的既有 ftboss:tide_pearl 仍可使用。BossRelicItems 的 init/addTo 与 BossRelicDrops 的 reward 是本轮注册/掉落需要的仅三个原有方法改动；FTBossItems 与 FTBossMod 不改。

## 外部兼容白名单

| 材料 | 物品 ID | 已核实的灾变 3.31 来源 |
| --- | --- | --- |
| 铁魔法秘银 | irons_spellbooks:mithril_ingot | 保留现有 Iron 物品；不使用泛用秘银标签 |
| 凋灵合金锭 | cataclysm:witherite_ingot | 先驱者掉凋灵合金块，可拆锭 |
| 末影合金锭 | cataclysm:enderite_ingot | 已实际注册并有模型；当前版本未找到生存配方/上述 Boss 掉落，不虚构来源 |
| 腾炎锭 | cataclysm:ignitium_ingot | 焰魔掉落 |
| 咒魂锭 | cataclysm:cursium_ingot | 咒翼灵骸掉落 |
| 远古金属锭 | cataclysm:ancient_metal_ingot | 远古遗魂掉远古金属块，可拆锭 |
| 黑钢锭 | cataclysm:black_steel_ingot | 亡灵材料、冶炼/合成；不是专属 Boss 锭 |

以已安装 L_Enders_Cataclysm-3.31.jar 的 ModItems 字节码注册、物品模型、配方、实体战利品表为证据。不 import 灾变或 Iron 的物品类，不重写外部模组战利品。精确 ID 兼容不导致缺少可选模组时强制链接外国类。排除的合金与非 Iron 秘银即使附上合法材质标签也不得重新进入目录。

## 被动与联动

新增矿物和锭的命中类被动只从既有真实玩家近战/箭矢来源进入，只针对非友方、非玩家拥有、非柠檬召唤的怪物。防御类不绕过伤害的 bypass 标签。煤与腾炎联动燃烧，紫水晶与石英联动发光；其余有短时急迫、速度、虚弱、夜视、缓慢、亡灵/护甲目标增伤、摔伤或弹射物减免。

各触发型保持玩家共享冷却和箭矢发射食物快照；同系 2 个插槽使持续时间 ×1.5、下一轮冷却 ×0.8，不重置已记录冷却。没有额外 hurt、额外击杀、移除效果或净化入口；禁疗、虚空侵蚀、庄园伤害来源守卫保持。

## 资源与发布验收

十种 item/generated 模型与 16×16 像素贴图由仅针对这十件物品的生成器生成，侧面为原版生成模型的 1 模型单位厚度。最终素材正面/背面/左/右预览放在 _work/food-weapon-boss-materials-20261012/art，必须真实查看并与候选内模型/贴图 SHA256 比对。

发布前必须冻结所有生产源码/UI/语言、取得双共享锁、读取当刻正式 Forge 基线并只读完成 EasterBossEntity StackMap 与六保护类严格链接，再生成全新最小方法增量。新模型/贴图仅允许显式白名单新增且最新基线中不存在。运行基线有变化立即停止；旧候选绝不可发布。

当前状态及最终 SHA256、实际条目/方法范围以本轮 FINAL_HANDOFF.md 与锁内验证报告为准；不得把离线编译/类链接当成实机通过。部署合并按用户交给 dsh；只读环境不能冒充已部署。实机待测包括 Forge 注册、Boss 二命/最终真死、召唤/小兵/重复事件拒绝、玩家法术击杀归属、奖励箱归集、材料消耗和界面、耐久 Mixin、箭矢快照/共享 CD、禁疗与侵蚀不可净化。
