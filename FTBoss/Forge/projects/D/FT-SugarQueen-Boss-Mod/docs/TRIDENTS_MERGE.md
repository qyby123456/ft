# 三叉戟模组合并（2026-09-21）

`qys-bosses.jar` 通过 Forge Jar-in-Jar 内嵌 Tridents 'n' Stuff 的原生 Forge 流血削弱版，安装时只放 Boss 包，不再单独放三叉戟包。内部保留 `tridents_n_stuff` 模组 ID、物品/实体/效果 ID 和原有贴图模型，以兼容旧存档、青衣武器、血腥契约和四相归一配方。不是把原有物品重注册到 `ftboss`。

仍需整合包已有的 Kotlin for Forge 4.x 前置；不要删除该前置。模组列表里仍有两个逻辑模组，这是正常的。被整合包沿用的流血削弱（最多五层、最高 16 倍、20 秒持续、10 秒后衰减）原样保留。

## 五个圣像

风暴、邪恶、深海、恒星、鲜血：`icon_of_storms`、`icon_of_evil`、`icon_of_depths`、`icon_of_stars`、`icon_of_blood`。

- 关闭全部创造物品栏及其搜索目录中的圣像领取入口。
- 全局战利品过滤器移除战利品表生成的五个圣像，不影响其他战利品。
- 当前原生 Forge 版没有生成圣像的配方、战利品表或事件掉落；构建会检查内嵌包不能出现生成圣像的配方。
- 保留物品注册、模型、已有背包/箱子里的圣像；不清除玩家旧物品，也不拦截转移旧物品。
- 消耗旧圣像的五条三叉戟升级配方仍保留。管理员 `/give` 不受限制；这不是防作弊或任意外部脚本的物品封禁系统。

## 内嵌来源

使用测试实例的 `tridents_n_stuff-0.2.0+1.20.1-forge-bleed-nerf.jar`，SHA256：
`9C39717DEDE7E31FE7176627126063BD9F4706C217D92781E7D2CD6B2BC5747A`。

原作者 fzzyhmstrs，内嵌包声明 MIT；原始作者及许可元数据保持不变，不受外层 Boss 包的版权声明覆盖。流血补丁源码及说明见 `_ref/tns_src/tridents_n_stuff-流血削弱-源码与说明/`。

部署脚本仅在确认新 Boss 包带内嵌依赖并完成哈希验证后，将目标目录的独立三叉戟包备份到 `ftboss-merged-mod-backups`（实例目录下，而非 mods 下），再撤出独立包，避免重复模组 ID。游戏运行时禁止部署。

## 回归

隔离工程 `smoke-libs` 放既有 GameTest 前置、内嵌原包的副本 `tridents_n_stuff-0.2.0.jar` 及 `kotlinforforge-4.12.0-all.jar`。运行 `gradle -I tools/verification/tridents_gametest.gradle clean runGameTestServer`：检查五种武器、五种旧圣像存档、投射物、创造/搜索入口、真实战利品过滤和保留升级配方。

开发环境需把内嵌依赖映射到 Mojang 开发名称。`tridents_dev_runtime.gradle` 只替换开发输出目录的内嵌 jar，仍走真实 Jar-in-Jar 加载，不更改源资源。其他既有 GameTest 可追加 `-I tools/verification/tridents_dev_runtime.gradle`。测试结束后必须不带测试 init 脚本运行 `gradle clean build`，恢复正式 SRG 内嵌包；`verifyMergedTridents` 会拒绝打包开发映射版。

## 2026-09-24 追加：附赠条目只进创造栏

用户先问「为什么没把三叉戟 mod 的全部内容合并完」，弄清内嵌机制后要求把这些东西**放回创造栏**，
并明确「**不进宝箱**」。落地如下：

- 内嵌本身没变：仍是 `META-INF/jarjar/tridents_n_stuff-0.2.0-forge-bleed-nerf.jar` 整包内嵌
  （220 条目、SHA256 `9C39717DEDE7E31F…` 不变），26 件物品一件不少。
- **新增** `item/ManorWeaponCatalog.java` 的第二份名单 `CREATIVE_ONLY`（13 条）：
  血腥契约 `sanguine_bond`、其余 6 把三叉戟（沉睡 / 狂暴 / 远古 / 海洋 / 神圣 / 远射）、
  6 把长矛（木 → 下界合金）。`addTo()` 在 `ENTRIES` 之后一并输出到创造栏。
- **战利品箱不受影响**：`ManorVictoryLoot.contents()` 只遍历 `ENTRIES`，仍是 20 条 + 四象归一戟 + 法术书。
  这正是要分成两份名单的原因 —— 并进 `ENTRIES` 就会连带进宝箱。
- 这 13 件不是庄园武器：不进 `ManorWeaponEnhancements.WEAPONS`，行为与内嵌包原版一致。
- 四个鱼叉（`harpoon_launcher` / 骨制 / 海晶石 / 钻石）用户没点名，**本次没动**。
- 回归：`tools/verification/check_tns_creative_extras.cjs`。
