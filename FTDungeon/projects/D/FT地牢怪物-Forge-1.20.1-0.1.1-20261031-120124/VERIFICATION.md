# 交付验证 · 0.1.1 · 2026-10-31

## 已通过

- JDK 17、Forge 1.20.1-47.4.10 下编译完成，并通过 ForgeGradle `reobfJar` 生成可部署 JAR。
- 四个模型的 655 个方块、218 根骨骼、17 段动画完整接入；几何版本为 GeckoLib 4 兼容的 `1.12.0`。
- 四张运行贴图与原始 bbmodel 内嵌 PNG 的字节完全一致，四份 bbmodel 副本与用户原文件的 SHA-256 完全一致。
- GeckoLib 4.8.4 实际模型烘焙器与动画解析器成功读取全部运行资源。
- 携带 Iron's Spells 'n Spellbooks 3.16.3、GeckoLib 4.8.4、Curios、Player Animator、Iron's Lib 的独立 Forge 开发环境成功执行 `runData`，模组和依赖加载完成。
- 成品字节码已核对：弓走 `ProjectileUtil.getMobArrow` 与 `BowItem.customArrow`，法师走铁魔法原生 `MagicMissileProjectile` 和原生法术强度接口；剑走原版近战接口。没有使用 `setHealth` 直接扣血、通用 `indirectMagic` 冒充铁魔法，或覆盖箭矢原生伤害。
- 模型提供者署名为“一颗大豆，b站uid355533780”；没有将其登记为模组代码作者。
- 成品不内置 Minecraft、Forge、GeckoLib、铁魔法的类文件；所有前置作为独立依赖使用。
- 四种原版形状、不同副色的刷怪蛋保留完整模型和中英文名称；独立“FT地牢怪物”创造栏按战士、弓手、法师、王排列，并以骷髅王刷怪蛋为图标。四种蛋也加入原版刷怪蛋分类。
- 已部署到用户指定的 FarmingTales Forge 1.20.1 整合包；部署文件与构建文件的 SHA-256 一致。部署前扫描了现有 360 个活动 JAR 的模组元数据，未发现旧版 `ftdungeonmobs`，未覆盖任何既有模组。

## 验证边界

以上验证包含编译、资源解析、模型烘焙、原始文件完整性、依赖加载和原生接口接入检查，不等于在完整整合包中完成客户端战斗与联机验收。

尚未在真实游戏客户端逐项实测伤害数值、盾牌、武器附魔、学派抗性、多人同步和所有其他模组的交互。实现复用原版/铁魔法的相应处理链，不绕过这些规则；最终效果仍需在游戏中验收。

本次仅向目标整合包的 `mods/` 新增本模组 JAR，没有修改玩家存档、其他模组或整合包配置。部署路径与摘要见 `DEPLOYMENT.md`；没有启动完整整合包客户端，创造栏显示、实际刷怪与战斗效果仍需启动游戏后验收。

## 日志与资源报告

- `build/reports/build-verification.log`
- `build/reports/forge-load-verification.log`
- `build/reports/imported-assets.json`
- `build/reports/deployment.json`

导出交付目录另带以上日志和报告的副本。
