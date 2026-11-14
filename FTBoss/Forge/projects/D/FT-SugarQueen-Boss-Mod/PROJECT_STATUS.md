# PROJECT_STATUS.md — FT-SugarQueen-Boss-Mod（当前状态速查）

> 2026-09-12 重写（旧版 173 KB 的流水账已归档到 `docs/archive/PROJECT_STATUS_2026-09-08_09-12.md`）。
> 这份只留**现在是什么样**：结构、Boss 清单、关键数值、构建/部署、工具。
> 历史细节看归档，最近的版本变更看 `docs/CHANGELOG.md`。

## 1. 工程基本信息

| 项 | 值 |
|---|---|
| 类型 | Minecraft **Forge 1.20.1-47.4.10** mod |
| modId | `ftboss` |
| 产物 | `qys-bosses.jar`（固定名，不带版本号） |
| JDK | `C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot` |
| 映射 | `official` 1.20.1（注意：`reobfJar` 会把原版字段/方法名重映射成 SRG，别用原版名做字节码关键字校验） |
| 依赖 | `libs/*.jar` 全部 `compileOnly`（铁魔法、GeckoLib）；运行时由整合包提供，缺失时自动降级 |

## 2. 目录结构

```
src/main/java/net/farmingtales/ftboss/
  FTBossMod.java          入口
  boss/                   事件与流程大脑: BossHandler(全局tick/禁飞/信标增益) ManorFight(庄园战场)
                          HiddenBossIntro(入场演出) SparrowSupport(麻雀) EasterBuffChain
                          ArmorShred ManorSpellBan BossMercy IronSpellBridge
  boss/ai/                机制模块: DefectOrbs SayaBuds TeaTactics BossEmpower BossDamageGate
                          BossRage MinionBlessing TrapBreaker
  entity/                 实体: 各 Boss + 召唤物 + 投射物(含 EasterPearlEntity QingyiTridentEntity)
  client/                 model / render / FTClientSetup
  item/                   物品与创造模式标签
src/main/resources/assets/ftboss/
  textures/entity/...     实体皮肤(九人在 textures/entity/easter/，含 maque)
  textures/item/...       物品贴图（**物品模型只能用 item/ 或 block/ 目录**，见 §6）
  models/item|block/...   模型 JSON      lang/zh_cn.json en_us.json
tools/                    离线工具(§5)；`tools/_archive/` = 历史一次性脚本与旧输出
docs/                     文档；`docs/archive/` = 归档
_backup/ _ref/ art/ libs/ 备份 / 参考素材 / 美术源文件 / 编译期依赖（都不参与构建）
```

## 3. Boss 一览

| # | 名称 | 实体 id | 血量 | 备注 |
|---|---|---|---|---|
| 1 | 糖霜女皇 / 姜饼王 | `ftboss:sugar_queen` | 480 | 4 种召唤物；复制体走简化 AI |
| 2 | 屠夫 | `ftboss:meat_mountain` | 480 | 双持冰斧 + 荣耀巨斧；复制体走克隆 AI |
| 3 | 沙耶 / 茶树 | `ftboss:tea_tree` | 280 | 扎根；茶芽庇佑二阶段免伤；**防御模板参考对象** |
| 4 | 酒桶王 | `ftboss:wine_barrel` | 480 | 分身诱饵 |
| 5 | 鼠群 | `ftboss:rat_boss` | 380 | |
| 6 | 故障机器人 | `ftboss:defect_core` | 404 | 充能球、悬浮、三阶段、死亡核心爆破 |
| 7 | 炫彩答辩超人 | `ftboss:compost_boss` | 340 | |
| 8 | 苹果爵士 | `ftboss:apple_boss` | 320 | |
| 9 | **甜苹果庄园·彩蛋九人** | `ftboss:easter_boss` | 各 1000 | 见 §4 |
| 10 | 麻雀（支援位） | 同 `easter_boss` variant 9 | 500 | 不进轮换；回血 / 上增益 / 净化 |
| 11 | **雪黎** | 同 `easter_boss` variant 10 | 1000 | 庄园第 10 位（v76）；平底锅 + 擀面杖（粉碎轮）/ 汤锅投掷 |

## 4. 甜苹果庄园（当前设计：独立血条）

- 十位变体（`EasterBossEntity.NAMES`）：0 柚子 / 1 蛋蛋 / 2 朵拉 / 3 功底 / 4 柠檬猫 / 5 青衣 / 6 文玄蓝 / 7 折耳根 / 8 紫菜 / **10 雪黎**；**第 10 位 9 = 麻雀**（支援，不进轮换）。
  `COUNT = 11`、`SPARROW_VARIANT = 9`、`XUELI_VARIANT = 10`；
  **轮换名单是显式的 `MANOR_VARIANTS = {0..8, 10}`，`MANOR_COUNT = 10`**（不再是连续区间 —— 用 `i < MANOR_COUNT` 取变体就会把麻雀算进去）。
- 流程：入场演出 → 抽一位上场 → **打死当前这位**才上下一位（随机、不重复）→ 十位全灭 → **最后的 Boss**（十人升空组成火柴人，雪黎当头顶，单独 5000 血池）→ 结束清场。
- 血条：顶上大血条 = 进度条「已击败 n/9」；每位 Boss 显示自己的血条。
- 玩家离场 > `RECALL_RANGE`(128 格) 会被拉回；当前 Boss 死活**按实体引用判定**（区块卸载不会误判成击杀）。
- 防御：主阶段伤害自管 `manorHurt()` = `clamp(原始伤害, 1.5% ~ 10% 最大血量)`，先扣伤害吸收；每掉 25%（75/50/25%）吃**附魔金苹果**（吸收 IV 2 分钟 + 再生 II 5 秒）+ **6 个二级增益**（60 秒）。
- 装备：腾炎套 4 件（功底穿亚当甲）每件 **8 级满附魔 + 无法破坏**，掉落率 0；武器同样 8 级满附魔。
- 麻雀：每 2 秒奶 1% 最大生命 + 3 个随机增益（**安静模式，不触发连锁增益提示**）、每 5 秒净化一位同伴；在场时**全体 Boss 增益等级 +1**（`buffAmp`）；地面跟随不飞。
  v72：**离友军 >10 格就把自己传送过去**（不再抓玩家/追玩家）；**伤害走 `manorHurt` 保底管线**（单次 1.5%~10% 最大血），否则腾炎套满附魔让她根本打不死；她**不吃附魔金苹果**。
- 柚子：每 3 次命中召 2 个迷你分身（上限 4，超限改自身速度 II）；本体阵亡后分身延长到 **5 分钟**后消散；分身随机拿 斩龙剑 / 冰斧 / 荣耀金斧（**★ 2026-09-24 按用户要求去掉血王子焰型剑，`MINI_WEAPONS` 从四把减到三把**）。
- 追击（**★ 2026-09-24 重做**）：珍珠追击改成"连续 **15 秒**没打到玩家才投"（蛋蛋 10 秒），投完冷却 10 秒（蛋蛋 6 秒）；连续 **120 秒**没打到玩家 → **场地禁飞 60 秒**（战场 96 格内玩家不能飞，只掐飞行+提示），窗口结束恢复飞行并重新倒计时。见 `util/ManorChaseRules.java`、`boss/ai/ManorFlyBan.java`、`boss/ai/ManorChaseTracker.java`。
- 击退（**★ 2026-09-24**）：Boss 抗击退 `BOSS_RESISTANCE` 0.5 → **0.85**；Boss 打玩家 `TO_PLAYER_MULT` 0.25 → **0.10**、竖直 0.5 → **0.25**。
- 爱意滑翔（**★ 2026-09-24**）：Boss 身上有 `brewery:haley`（盛节精酿）时**允许飞行**（解除重力 + `FlyingMoveControl` 三维追击），效果结束落回地面。
- 末影珍珠：落点 4 格内玩家吃 **45 点魔法伤害**（`EasterPearlEntity`）。
- 抓人砸方块：**7×7、从脚底往上 3 层，脚下那层不砸**。
- 对玩家的减益**不含**失明/黑暗/漂浮（例外：折耳根已获准使用失明/黑暗）。
- 雪黎（v76）：平底锅(主手) + 擀面杖(副手, 近战挂"粉碎轮" 3 点/tick × 60 tick, 伤害类型 `create:crush`)；
  招牌技能扔**汤锅**(`ftboss:stockpot` 实体, 落点 3 格一半物理一半魔法)。粉碎轮**不打全局折**。
  v78：三招轮换（汤锅投掷 / 平底锅横扫 30+点燃 / 擀面杖压路冲锋 12+粉碎轮），冷却约 2.4~4.7 秒。
- **隐藏 Boss 挑战确认**（v78）：入场演出前先弹警告 + `[挑战]/[放弃]` 按钮（`/ftboss challenge|giveup`，60 秒过期）；
  点挑战才开演，并发 **金苹果 ×64 + 伤害吸收 400 点(10 分钟)**。见 `boss/HiddenBossChallenge.java`。
- **索敌**（v78）：Boss 除玩家外**还会主动索敌其它生物**（铁傀儡/中立怪/别的 mod 的怪），
  但排除本模组自己的实体与彩蛋同伴（按命名空间判断）。
- 文玄蓝（v79）：多了**超能光束**（红色蓄能预警 0.75s → 引导 2s，每 0.5s 四系各 6 点；蓄能后方向锁定不再追踪；
  **放完不虚弱**，技能冷却 16 秒）。蛋蛋（v79）：末影珍珠瞬移 ≈0.65 秒一次、换位冷却 ÷4，且换位失败时兜底硬传。
- **主动追击**（v81）：Boss 离玩家超过 20 格就强制锁定 + 加速追（`FOLLOW_RANGE` 42→64），
  超过 72 格直接闪现到玩家身边（6 秒冷却）。麻雀与终章站位者不参与。
- **柠檬猫召唤**（v81）：一次召唤**必是两位不同**的食物系 Boss（以前会抽到同一个，日志实证过两只糖霜女皇）。
- 玄蓝/朵拉击退（v81）：长矛 1.2/0.35 → **0.45/0.12**；蒸汽 0.5 → **0.18**；光束 0.2 → **0.05**。
- **"是否在交战"的唯一判据**（v84）：`boss/ai/FightState.inFight()` ——
  ① 玩家在庄园战场 96 格内；② 64 格内有本模组 Boss 正把该玩家当目标。
  **祝福 / 麻雀支援都走这一条**（不再只看"附近有没有 Boss 实体"，遗留或自己召唤的 Boss 不算战斗）。
- **免疫判定**（v83 修）：`BossDamageGate` 以前把**整个 `armamentarium` 命名空间**的伤害一律免疫，
  结果连该 mod 武器的普通伤害也吃不到 → Boss"卡血条、打不掉、kill 不死"。
  现在只按精确 id 免疫流血/真伤；**免疫会打日志**。`/kill` 这类无视无敌的伤害现在**直接秒杀**庄园 Boss。
- **青衣闪电**（v82）：三叉戟落雷伤害 5.33 → **16**、脉冲 4s → **3s**、全局冷却 6s → **3.5s**；
  新增**铁魔法闪电**（每 6 秒，`chain_lightning` 等 4 选 1，**5~7 级**；无铁魔法时退回真闪电 + 24 魔法伤害）。
- **庄园专属 BGM《史诗战斗曲》**（v82）：`music/manor_epic.ogg`，**20 分钟 / 10.1 MB**，
  由 `tools/audio_tools/compose_epic.cjs` 生成（纯 Node 合成 + WASM Ogg 编码，**机器上没 ffmpeg**）。
  彩蛋十人整场不切（`BossMusic.Group.MANOR_EPIC`）。
- **Forge 的祝福**（v80）：玩家每 60 秒自动清掉自身 HARMFUL 减益, 并获得
  `armamentarium:adamantine_effect`(金刚不坏) / `damon_effect`(死亡王子) / `lone_graveseeker_effect`(幽匿共生体套装)
  **25 秒**。见 `boss/ForgeBlessing.java`；`ONLY_WHEN_BOSS_NEARBY=false` 表示一直都发。
  （汉化名对照来自 `kubejs/assets/汉化材质.zip`；整合包里没有"血王子"套装效果，第 2 件取的是死亡王子套。）

## 5. 构建 / 部署 / 工具

**构建**（PowerShell）：

```powershell
$env:JAVA_HOME="C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot"
& "C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat" `
  -p "D:\ft工程文件\FT-SugarQueen-Boss-Mod" build --console=plain --no-daemon `
  "-Dorg.gradle.jvmargs=-Duser.language=en -Duser.country=US -Dfile.encoding=UTF-8"
```

**部署 5 处**（每次改完必做；**Minecraft 运行时绝对不要覆盖**）：
`tools/deploy_jar.ps1 -NoBuild -ArtifactPath <jar>` 覆盖其中的 1~4（第 1 处实际已改成
`FarmingTales_ForgeⅡ v1.5.2_beta3`，见 AGENTS.md 的部署口径）。

1. `...\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2_beta3\mods\qys-bosses.jar`
2. `C:\Users\YOUR_USERNAME\OneDrive\桌面\FT经济-波动mod成品\qys-bosses.jar`
3. `C:\Users\YOUR_USERNAME\Desktop\qys-bosses.jar`
4. `C:\Users\YOUR_USERNAME\Desktop\FT经济-波动mod成品\qys-bosses.jar`
5. `build\libs\qys-bosses.jar`（源）
6. **★ 2026-09-24 追加：用户实际在玩的实例 `versions\FarmingTales_ForgeⅡ v1.5.2\mods\qys-bosses.jar`**
   （deploy_jar.ps1 **不覆盖**这一处，要单独 copy —— 2026-09-23 那版 jar 就是落在这里被测试的）

部署后**核对各处 SHA256 一致**，并删除实例目录下的 `lightspeed-cache` 与 `modernfix`。

> ★ 2026-09-24 起**从主工程重建 jar 之后必须再跑一次** `tools/merge_jar_extras.ps1`
> （把部署版里那 6 个"没有源码"的血腥契约条目 + `FTBossMod.class`/`SpellVisualRules.class`/lang 补键搬回来）。
> 详见 `docs/CHANGELOG.md` 顶部的「构建合并」一节。

> ⚠️ **部署前必须先确认没有 java 在跑**（2026-09-13 踩过一次）：
> ```powershell
> if (Get-Process java,javaw -ErrorAction SilentlyContinue) { "游戏正在运行 —— 不要覆盖 jar！"; exit 1 }
> ```
> 事故经过：jar 写入时间落在"游戏启动 09:27:25 → 崩溃 09:27:51"之间，Forge 的模块类加载器读到了被替换中的 jar，
> 抛 `NoClassDefFoundError: .../ButcherBossModel`（类其实**在** jar 里，13.4 MB / 425 条目全可读），
> 表现为 `ModLoadingException (common_setup)` 启动崩溃。**这种崩溃不是代码问题，重启游戏即可**；
> 但为保险，部署前先查进程。

**工具**（`tools/`）：

| 脚本 | 用途 |
|---|---|
| `cleanup_dead_code.cjs` | 死代码清理（带行断言，断言不过就整体跳过） |
| `model3d/preview_3d.cjs` | 物品模型**正交 3D 预览**（逐面仿射贴图 + 画家算法）—— 排查"贴图错位 / 手持朝向不对" |
| `model3d/preview_item.cjs` | 单面正视图预览（遵守 `texture_size`） |
| `model3d/render_tex.cjs` `render_model_tex.cjs` | 实体贴图/模型四视图渲染 |
| `model3d/gen_*.cjs` | 各 Boss / 道具 / 石像的模型与贴图生成器（**改资源要改这里再重跑，别手改产物**） |
| `model3d/make_statue_youzi_blockstyle.cjs` | 把柚子石像重建成"食物系石像写法"（`block/block` + UV 0..16 + 无 `texture_size`）；**从 PARTS 表推导，幂等** |
| `extract_bbmodel_tex.cjs` | 从 Blockbench `.bbmodel` 里导出内嵌的 base64 贴图（用户直接发 bbmodel 时用） |
| `bake_global_damage.cjs` | **全局伤害烘焙**：把"Boss→玩家"的整体系数写进各处数字（带断言，改错即报错退出）——v77 用过 |
| `model3d/validate*.cjs` `check_assets.cjs` `audit.cjs` | 资源校验 |
| `tools/_archive/` | 78 个历史一次性脚本（fix_*/wire_*/check_* 等）+ 旧输出目录 |

## 6. 已踩过的坑（改代码前先读）

1. **渲染器构造抛异常 → Lightspeed 会静默换成空渲染器 → 实体隐形**，日志里不报错。
2. `@Mod.EventBusSubscriber` 的 tick 事件**可能不触发**；关键逻辑挂在实体 `tick()` 里并用游戏刻去重。
3. **方块/物品图集只收 `textures/item/` 与 `textures/block/`**；物品模型引用 `textures/entity/` 会显示成黑紫缺贴图。
4. `reobfJar` 把原版方法/字段名重映射成 SRG（`m_xxxxx_`），对原版名做字节码校验会得到假阴性；只有自定义名字可 grep。
5. `EasterBossEntity.COUNT` 必须等于变体总数：`setVariant()` 会 `min(COUNT-1, v)` 夹位。
6. 人形/立体物品模型**不要**沿用 `item/handheld` 的 `rotation [0,-90,35]`（那是给扁平刀剑的），否则手里只能看到侧面。
7. `sl.getEntity(uuid)` 在区块卸载时返回 null，**不能**用来判定实体死亡（会误判成击杀并跳过 Boss）。
8. 共享血条那类"按伤害百分比推进"的设计容易出边界 bug（开局掉 1% 就换人、重复扣血）；现在九人改用独立血条。
9. **UV 空间与 `texture_size` 必须配套**：写 `texture_size` 时 UV 是**像素**，不写时 UV 是 **0..16 相对整张贴图**。
   两个混用（如 UV 已改成 0..16 却还留着 `texture_size:[64,64]`）会让每个面只采到 2×2 像素
   → 表现为"褪色平板 / 贴图展开图"。这就是柚子石像 v48~v67 一直"没修好"的根因（v68 修）。
10. **`north` 面才是"看得到的那面"**：`block/block` 的 display 下，GUI/手中看到的是 **north**。
    为物品模型（`item/handheld`，正面朝 +Z）把 `north↔south`、`east↔west` 对调过的模型，
    一旦改成方块写法就会**把后脑勺对着镜头**。食物系石像的正脸都在 north，照抄它。
11. 食物系石像**同时**有"可放置方块"（`FTBlocks.STATUE_*` + blockstate + 上下半模型 + 战利品表）和
    "物品模型"两份；柚子石像只有物品一份（柚子 手里的武器 `FTBossItems.STATUE_YOUZI`），别搞混。
12. **Boss→玩家 的伤害已经"烘焙"进数据**（v77 起，**运行时钩子已删除**）：
    - 彩蛋系（0~10 变体 + 其投射物）净倍率 `0.5 × 4/3 = 2/3` 折进了 `EasterBossEntity.BOSS_DAMAGE_MULT`
      （现值 **4/3**）→ 所有 `× BOSS_DAMAGE_MULT` 出来的数字**就是玩家实际吃到的伤害**；
    - 非彩蛋系（食物系 Boss/召唤物/炮弹）各自数字已 **×0.5**（含近战 `ATTACK_DAMAGE`）；
    - 想整体调"Boss 对玩家伤害"：彩蛋系改 `BOSS_DAMAGE_MULT` 一个数；非彩蛋系用
      `tools/bake_global_damage.cjs`（带断言，改错会报错退出）。
    - **不要**再引入"每次受伤都跑的全局打折钩子"（那是 v69~v76 的做法，已按用户要求移除）。
    - 新写技能时：数字直接写**最终值**；没有攻击者实体的伤害类型（虚空/真实/火焰）不受任何全局倍率影响。
13. **区块卸载不是死亡**：区块卸载时实体是 `setRemoved(RemovalReason.UNLOADED_TO_CHUNK)`，
    `isRemoved()` / `!isAlive()` **都为 true**，但它没死、区块重载后还会回来（而且是**新对象**，
    旧引用永远是"已移除"）。判断 Boss 死活只能用 `isDeadOrDying()` / `getHealth() <= 0`，
    并且要按 UUID 每 tick 重新绑定实例（`ManorFight.pumpAll` 已按这套改，见 v70）。
    玩家死亡时区块票据会失效 —— 这正是"Boss 活着却被记成已击败"的触发条件。

14. **BGM 必须由 Boss 自己"请求"**：`BossMusic.request(level, boss)` 决定曲目组
    （`groupOf` 按实体类型映射，彩蛋系 → `MANOR_EPIC`），`release` 收尾。
    **只加资源（`sounds.json` + `.ogg`）而不加调用点 = 永远静音**：
    v82 写完《甜苹果庄园·史诗战斗曲》后一直没响，根因是 `EasterBossEntity`
    **从来没调过 `request`**（只有食物系 Boss 调），战斗里听到的是别的 Boss 请求的兜底曲，
    它一退场就"兜底停止"。v85 已在 `tick()`/`die()` 补上（迷你分身不参与，免得抢音乐控制权）。
    新增 Boss 时**先确认这两个调用点**。
15. **BGM 的另外三个坑（v90 一次踩全，排查顺序照这个来）**：
    1. **`release` 不能"立刻停"**：庄园是**一位一位轮换**上场的，中间有 ~3 秒空场；
       立即停会让长曲每 3 分钟从头重播一次（听起来就是"音乐不对"）。
       现在空场后等 `GRACE_TICKS`(5 秒) 才真停，期间 `request()` 会取消（`pendingSince = 0`）。
    2. **客户端的音效 id ≠ 音频文件路径**：`SoundInstance.getLocation()` 是**事件 id**
       （`ftboss:boss_music_xxx`），`ftboss:music/xxx` 在 `getSound().getLocation()`。
       v90 之前 `BossMusicClient` 拿事件 id 去 `startsWith("music/")`，**永远为假** ——
       于是"压掉原版背景音乐"一次都没执行（打 Boss 时原版音乐照响）、兜底字段永远是 null。
    3. **客户端兜底的 Boss 名单要和服务端同源**：以前只认茶树/糖霜女皇两只。
       现统一用 `BossMusic.isBgmBoss(Entity)`；**加新 BGM Boss 时 `groupOf` 和它一起改**。
    - 验证：客户端应打 `[FTBoss] 客户端收到 BGM 播放请求: 事件=… 音频文件=ftboss:music/…`；
      整场**只应有一次 `开演`**。
    - 自写 ogg 的"到底能不能播"可以直接用游戏自带的 JOrbis 验：
      `tools/audio_tools/OggInfo.java`（`javac -encoding UTF-8 -cp <codecjorbis.jar> …`，
      跑出来 `dur=` 对得上就没问题 —— v90 就是这么排除掉"文件坏了"这条的）。
    - ④ **`PlaySoundEvent` 的时序陷阱（v98 才彻底搞清，害我返工三次）**：
      `PlaySoundEvent` 在 `SoundEngine.play` **最开头**触发，此时
      `SoundInstance.resolve()` **还没被调用** → `AbstractSoundInstance` 的 `sound` 字段**还是 null**：
      · `instance.getSound()` → **null**（**不是** `EMPTY_SOUND`！v90 把它误读成"解析成空音源"，方向就跑偏了）；
      · `instance.getVolume()` / `getPitch()` → **内部解引用那个 null → NPE**
        （`AbstractSoundInstance:78`，对应 `getVolume()`）；而这个异常是从 `ForgeHooksClient.playSound`
        里抛出去的 → `SoundEngine.play` 当场中断 → **声音被杀掉**，还伴随一条
        `Error executing task on Client` 的 FATAL（v97 就是这样把本来正常的音乐搞哑的）。
      ⇒ **在 `PlaySoundEvent` 里只能用**：`getLocation()`（事件 id）、`getSource()`、`isLooping()`、坐标。
      判断"到底在不在播"要等事件之后用 `SoundManager.isActive(instance)`。
16. **缓存目录在"版本目录"，不是 `.minecraft\`**：本包是**版本隔离**安装，游戏目录 =
    `versions\FarmingTales Forge\`。所以
    - `lightspeed-cache` / `modernfix` 真正在 `versions\FarmingTales Forge\` 下
      （`.minecraft\` 下那两个根本不存在 —— v97 之前一直在删空气，缓存 16.8 MB / 782 个文件
      从来没清过，其中包括 `resourceLists\ftboss1.0.0-qys-bosses.ser` 这种"我们 jar 的资源清单"缓存）；
    - **改 jar 之后一定要清它**：Lightspeed 会缓存每个 mod jar 的资源清单（按"模组版本+jar 名"命名，
      我们的 mod 版本恒为 1.0.0），jar 内容变了而清单没刷新时，客户端会认为某个资源"不存在" ——
      v97 的"音乐放不出声"（sound 事件解析成空音源、却没有 `cannot add` 警告）高度怀疑就是它。
    - 同理，日志/存档也在 `versions\FarmingTales Forge\logs`、`...\saves`。
17. **细雪(Powder Snow)冻结只能写 `ticksFrozen`（v105 加雪黎细雪时才搞清）**：
    `Entity.isInPowderSnow` 是**每 tick 被 `Entity.baseTick` 重置**的派生状态，
    `setIsInPowderSnow(true)` 活不过一 tick（写了等于没写）；要给实体挂"细雪"必须 `setTicksFrozen(n)`：
    - 客户端按 `Entity.getPercentFrozen()` 画**结霜覆盖层** → `ticksFrozen > 0` 就看得见；
    - 叠到 `getTicksRequiredToFreeze()`（玩家 140）→ `isFullyFrozen()` → `Player.tick()` 里按
      `freezeDamage` 游戏规则结算**冻结伤害**；
    - 不在细雪里每 tick **掉 2 点**（`Entity.baseTick`）→ 挂 90 点只留约 2 秒霜，所以"连击"才有压制力；
    - 细雪的**减速**其实来自 `PowderSnowBlock` 的 `makeStuckInBlock`，光设 ticksFrozen **不会**减速，
      要减速得自己补一条 `MOVEMENT_SLOWDOWN`（v105 就是这么做的）。
18. **"眩晕/定身"怎么做（v111 雪黎 ⑦ 天降瞬砸）**：原版**没有**眩晕效果，1.20.1 也没有能直接禁用移动的 API。
    本项目用的组合拳（`EasterBossEntity.stunPlayer / xueliStunTick`）：
    - **每 tick 横向清零速度**：`p.setDeltaMovement(0, Math.min(0.0, mv.y), 0.0)` ——
      `Math.min(0, y)` 是关键：**保留重力/下落**，但把跳跃那点向上速度压掉（否则定身的人会原地起跳）；
    - 必须 `p.hurtMarked = true`（等价于发 `ClientboundSetEntityMotionPacket`），否则客户端不认服务端的清零，
      会一直"预测"着自己还在走 → 表现成橡皮筋；
    - 再补 `MOVEMENT_SLOWDOWN`（短时长**每 tick 刷新**，如 15 tick / amp 6）与 `setSprinting(false)`、
      `stopUsingItem()`（吃东西/拉弓被打断）；
    - 状态**挂在 Boss 实例的 Map<UUID,Integer>** 上、在 Boss 的 `tick()` 里结算 ——
      Boss 一死/离场眩晕就自动结束，不会留下"玩家动不了"的僵尸状态（比写事件监听器安全）。
    - 别用 `MobEffects` 里的失明/黑暗去"代替"眩晕（用户明确不要那三个给玩家）。
19. **"游戏关了"≠"进程没了"：部署判定要看 jar 占用，别只看 `Get-Process java`（v112 踩到）**：
    2026-09-14 那天游戏 23:20:53 已经打了 `Stopping!`、窗口也没了，但 **JVM 卡在关闭流程里没退**
    （残留 `java.exe` 一个多小时后还在，CPU ≈ 0、仍占 1 GB 内存），于是"等 java 退出再部署"的脚本
    **永远等不到**，自动部署静默失效。正确判据是**真正要紧的那件事**：
    `[System.IO.File]::Open($modsJar,'Open','ReadWrite','None')` 能不能打开 ——
    游戏活着时 Forge 一直握着 mods 里的 jar（打不开），释放了就说明可以安全覆盖。
    已按这个改 `tools/wait_deploy.ps1`。
    另外：**卡死的空壳 JVM 可以直接结束**（游戏内容全在，日志也已经 flush 完）——
    否则它会一直占内存、也会继续骗过"看进程"的脚本。
20. **`FlyingMoveControl` 每 tick 读 `Attributes.FLYING_SPEED`（2026-09-24 实机崩溃）**：
    它构造时不会报错，但 `tick()` 里 `getAttributeValue(FLYING_SPEED)` 一旦属性表没注册就抛
    `IllegalArgumentException: Can't find attribute minecraft:generic.flying_speed` ——
    **服务端当场崩**（崩报 Suspected Mod = ftboss）。`Mob.createMobAttributes()` **不含**飞行速度。
    那天是「爱意滑翔(brewery:haley) 允许 Boss 飞行」第一稿踩的：蛋蛋从玩家那儿复制到这个增益就崩。
    以后**任何"让某个实体飞"的改动**，先确认它的 `createAttributes()` 里有
    `.add(Attributes.FLYING_SPEED, …)`；并且切飞控/无重力这类演出**整段包 try/catch**，
    出错退回地面而不是崩 tick。回归：`tools/verification/check_haley_flight.cjs`。

## 7. 相关文档

- `AGENTS.md`：协作 / 构建 / 部署完整操作手册（历史很长，保留）。
- `CODE_STYLE.md`：编码规范（UTF-8 无 BOM、LF、Java 4 空格、常量优先…）。
- `docs/ARCHITECTURE.md`：分层与事件流。
- `docs/CHANGELOG.md`：近期版本变更（v44 → v113）。
- `docs/boss_reference/`：**全 Boss 数值与技能机制**的内容源（markdown）+ 生成 Word 的说明；
  生成器 `tools/docgen/make_boss_doc.cjs`；**生成前跑两个体检**：`tools/docgen/check_ref.cjs`（格式，挑嵌套列表等
  Word 会排坏的写法）与 `tools/docgen/check_numbers.cjs`（文档里的「常量 = 值」vs 代码真值，抓"代码改了文档忘了"）。
  音频时长一律用 `node tools/audio_tools/ogg_dur.cjs <ogg>` 实测（唱片长度参数必须等于它）。
- `docs/archive/`：旧版 PROJECT_STATUS（173 KB 流水账）、其它工程的文档。
