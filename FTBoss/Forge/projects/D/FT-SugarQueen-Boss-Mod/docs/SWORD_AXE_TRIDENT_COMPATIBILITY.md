# 原版剑、斧、三叉戟与灾变近战构筑兼容

本轮仅是独立可审查增量。正式 Forge 1.5.2 JAR 不由任何子线程写入；NeoForge 不变。

## 范围

- 原版六剑：木、石、铁、金、钻石、下界合金。
- 原版六斧：木、石、铁、金、钻石、下界合金。
- 原版三叉戟：近战构筑、编辑、五功能门控已接入独立候选；投掷新增羁绊/小被动必须等统一来源桥闭环。
- 灾变 3.31 共 21 项精确 ID：black_steel_sword、black_steel_axe、athame、khopesh、coral_spear、coral_bardiche、ancient_spear、the_annihilator、soul_render、the_incinerator、the_immolator、astrape、ceraunus、meat_shredder、gauntlet_of_guard、gauntlet_of_bulwark、gauntlet_of_maelstrom、infernal_forge、void_forge、brontes、tidal_claws。
- 不以整个 tools tag、所有 Item 或所有 PickaxeItem 放行。弓杖另属独立分支，不混进这份候选。

## 构筑规则

五项逻辑仍为 HANDLE / GUARD / CORE / STABILITY / BLADE，沿用已有协议和制作者接口。每个格子一件，不堆叠。初始组成只使用真实配方成本及有限、不可退款的环境参考；材料拆除会减少功能，不保留必定材料。

原铁剑保持既有六件原生组成、九格起步、十七格封顶及原五功能阈值。其他原生默认阈值由各自组成与位置权重生成，通常只有 0.5% 余量。位置基础值复用既有七类五维基值，远小于主体材料；空格/锁格不贡献功能。

| 原版族 | 初始格数 | 进化四次后 |
| --- | ---: | ---: |
| 木剑 / 木斧 | 7 / 8 | 15 / 16 |
| 石剑 / 石斧 | 8 / 9 | 16 / 17 |
| 铁剑 / 铁斧 | 9 / 10 | 17 / 18 |
| 金剑 / 金斧 | 9 / 10 | 17 / 18 |
| 钻石剑 / 钻石斧 | 11 / 12 | 19 / 20 |
| 下界合金剑 / 斧 | 15 / 17 | 23 / 25 |
| 三叉戟 | 13 | 21 |

每次进化解锁两格，最多 27 格。灾变按武器分别使用 9–19 初格与 17–27 上限；既有 paid 材料和拆除记录不被强制重写。

## 配方与环境事实

以当前实例 Minecraft 1.20.1、灾变 3.31 和 Integrated Cataclysm 安装 JAR 为来源，不用其他版本百科替代本机配方。

- 木材保持实际原版 planks 成员和灾变 chorus_planks，石材保持圆石、黑石、深板岩圆石的标签等价；不会凭任意后缀判断材料。原生账本保存稳定的配方候选选材，不宣称可以恢复玩家过去实际消耗的木种。
- 下界合金剑/斧结构展开为原钻石武器成本加四碎片、四金锭；升级模板仍是真实锻造耗材，但明确不做可拆换或可退款结构件。
- 融合台只适配实际 WeaponfusionRecipe 的 getbaseIngredient / getAdditionIngredient，不冒充普通 CraftingRecipe，不修改灾变 JAR。
- 绞肉锯和炎葬分别识别原配方与机械合成覆盖，依据运行时 RecipeManager 与保存的预算选择对应阈值，绝不把两套成本相加。
- 无合成配方的武器只提供 THEME 参考，三叉戟不虚构合成配方；原生参考拆出永不产出真实材料，也不激活 paid 材料羁绊。
- 旧铁粒/金粒预算按等价成本读取；非等价自定义配方不强制替换为默认，制作者应同时注册对应 functions profile。
- 金属、食物、Boss 材料、环境材料共用既有扩展接口；新增材料带明确亲和属性，不捏造未知价格或加工复杂度。

## 原生行为保留

不把非剑武器强制转成 SwordItem。灾变十二把零最大耐久和三把原生 isDamageable=false 的战锤保持原生不可耗损语义；零耐久不等于损坏，也不伪造最大耐久。

正常武器的原伤害、攻速 UUID、工具动作、破盾、附魔和原生技能入口不被替换。新可选 @Pseudo Mixin 只在五功能不足时取消 Ancient_Spear / Tidal_Claws 的 onLeftClick；不抢网络包、不重复调用技能。

三叉戟原生只剩一点耐久时不能起投，剩两点是无干预条件下最后合法普通投掷；不移除原门控。忠诚、激流、引雷、创造拾取及原实体携带/回收完整 NBT 保持原逻辑。新增投掷伤害/羁绊不能从命中时当前手补来源。

## 制作者接口

SwordCavityExtensions.registerLayout / registerNativeRecipe / registerFunctions / registerBond 优先于默认规则。布局仍是 0..26 的合法排列并通过原服务器容器同步。SwordMaterialExtensions.register / registerBond 保留营养、饱和、namespace、价格、复杂度、五功能和亲和属性组合接口；未知信息不满足显式要求。右侧独立羁绊栏沿用原接口，主面板不新增大段提示。

## 放行门槛

本轮源冻结、来源校验、JVM严格类/成员/Handle链接、原生目标签名、ZIP、无关方法/条目保持和纯规则/NBT fixture 验证仅为离线门槛，不等于实机或实际 Mixin 应用。

最终必须等所有食武分支方法级统合、统一 v2 AttackId/WeaponId/来源快照、secondary-first、狼繁殖与全部 UI/语言/网络入口闭环，完成 GPT 独立复审。部署当刻双共享锁内确认游戏已关闭、最新基线未变、备份并串行安装；基线变化立即停止重基。第三方只读调研和编译声明不能进入 payload。
