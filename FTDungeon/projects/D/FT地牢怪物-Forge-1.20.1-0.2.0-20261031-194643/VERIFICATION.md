# 交付验证 · 0.2.0 · 2026-10-31

## 已通过

- JDK 17、Forge 1.20.1-47.4.10 下编译完成，并通过 ForgeGradle `reobfJar` 生成可部署 JAR。
- 四个模型的 655 个方块、218 根骨骼、17 段动画完整接入；几何版本为 GeckoLib 4 兼容的 `1.12.0`。
- 四张运行贴图与原始 bbmodel 内嵌 PNG 的字节完全一致，四份 bbmodel 副本与用户原文件的 SHA-256 完全一致。
- GeckoLib 4.8.4 实际模型烘焙器与动画解析器成功读取全部运行资源。
- 携带 Iron's Spells 'n Spellbooks 3.16.3、GeckoLib 4.8.4、Curios、Player Animator、Iron's Lib 的独立 Forge 开发环境成功执行 `runData`，模组和依赖加载完成。
- 弓走 `ProjectileUtil.getMobArrow` 与 `BowItem.customArrow`，再对原生箭基础伤害应用攻击属性比例一次；法师走铁魔法原生 `MagicMissileProjectile` 和法术强度接口；剑走原版近战接口。战斗不使用 `setHealth` 直接扣血，也不使用通用 `indirectMagic` 冒充铁魔法；改最大生命时的 `setHealth` 只用于保留剩余血量比例。
- 模型提供者署名为“一颗大豆，b站uid355533780”；没有将其登记为模组代码作者。
- 成品不内置 Minecraft、Forge、GeckoLib、铁魔法的类文件；所有前置作为独立依赖使用。
- 四种原版形状、不同副色的刷怪蛋保留完整模型和中英文名称；独立“FT地牢怪物”创造栏按战士、弓手、法师、王排列，并以骷髅王刷怪蛋为图标。四种蛋也加入原版刷怪蛋分类。
- 已部署到用户指定的 FarmingTales Forge 1.20.1 整合包；部署文件与构建文件的 SHA-256 一致。旧版 0.1.1 已移至整合包内独立备份目录，活动 `mods/` 只保留 0.2.0；其他模组文件列表、大小与修改时间前后相同。
- 成品 JAR 包含难度配置、公开 API、profile 与管理员命令类，不包含临时 GameTest 诊断类、诊断弓、测试结构或第三方类。

## 难度系统运行验证

在工程 `run/difficulty-verification/` 内使用真实 Forge 47.4.10 GameTest 服务端、真实前置与实体执行独立验证，最终日志为 `All 1 required tests passed`。没有启动用户整合包，也没有接触用户存档或修改服务器 EULA。

- 默认普通难度自动选择二档；战士生命 60、装备铁剑后的攻击属性 7.2、护甲 4，王生命 300、装备钻石剑后的攻击属性 12，法师原生法强 1.2、魔抗 1.1。
- 测得弓的 `customArrow` 基础伤害经过配置倍率只缩放一次，生成的仍是真实原版箭实体。
- 法师一档与五档的铁魔法 `getSpellPower` 比例为 2.25；五档魔抗由铁魔法原生 `DamageSources.getResist` 得到 0.5 的中性学派伤害系数，没有第二个减伤钩子。
- 半血战士切四档仍为半血；连续刷新 30 次不叠加修饰符、不回血，其他模组风格的外来护甲修饰符没有被移除。
- 单体倍率和加值正确组合；保存并读取 NBT 后保留固定四档、单体调整，以及 150 最大生命 / 75 剩余生命，不错误回满或截断。
- 实际执行 `set`、`bonus`、`clear_bonus`、`get`、`auto`、`reload` 管理员命令。世界难度改为困难时自动实体升三档，固定四档实体不被覆盖。
- 从磁盘重载档位与骷髅王类型配置后，已加载实体立即变化；测试中带类型加成的二档王得到生命 437.5、攻击 13.2、护甲 11、魔抗 1.2、法强 1.44。
- 无效档位、NaN 和超范围单体输入被拒绝；无效 TOML 重载报错并保留原已应用属性，修复文件后能再次成功重载。已死亡实体改档不复活。

诊断辅助源码与运行配置仅放在忽略的 `build/diagnostics/`，不添加或发布到生产模组中。导出目录的验证报告附有诊断 ZIP 以便复现；它不是游戏模组。配置文件监控取决于系统与 Forge，明确验证的是 `difficulty reload` 的磁盘重载路径，不承诺文件保存后监控一定立即触发。

## 验证边界

以上验证包含编译、资源解析、模型烘焙、原始文件完整性、依赖加载、独立服务端实体属性/NBT/命令验证，不等于在完整整合包中完成客户端战斗与联机验收。

尚未在真实游戏客户端逐项实测伤害数值、盾牌、武器附魔、学派抗性、多人同步和所有其他模组的交互。实现复用原版/铁魔法的相应处理链，不绕过这些规则；最终效果仍需在游戏中验收。

本次仅更新本模组 JAR，并新增自己的初始配置 `config/ftdungeonmobs-common.toml`。没有修改玩家存档、其他模组或其他配置。部署路径、摘要与回滚位置见 `DEPLOYMENT.md`。

独立前置环境仍有铁魔法自带的战利品表解析与版本检查网络警告，未改动这些无关资源；GameTest 最终通过，不把这些警告描述为本模组的难度功能失败，也不宣称整个整合包没有任何警告。

## 日志与资源报告

- `build/reports/build-verification-0.2.0.log`
- `build/reports/forge-load-verification-0.2.0.log`
- `build/reports/difficulty-runtime-verification.log`
- `build/reports/imported-assets.json`
- `build/reports/deployment-0.2.0.json`

导出交付目录另带以上日志和报告的副本。
