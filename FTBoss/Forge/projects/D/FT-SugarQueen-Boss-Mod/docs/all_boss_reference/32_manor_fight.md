# 庄园战整体流程（演出 / 轮换 / 终章 / 奖励）

本节覆盖「甜苹果庄园」隐藏 Boss 战的整场流程与全局规则：开启条件与两次确认、入场演出时间轴、参战者名单与防逃课拴绳、轮换与进度血条、终章现状、胜利结算与奖励箱、中途退出与重登恢复、怜悯询问边界。单只 Boss 的技能数值见各自小节。

## 一、开启条件与两次确认

| 环节 | 规则与数值 | 出处 |
|---|---|---|
| 触发源 | 苹果爵士「头衔共鸣」蜕变为隐藏 Boss | `entity/AppleBossEntity.java:687` |
| 检测周期 | 每 20 tick 检查一次 | `entity/AppleBossEntity.java:344` |
| 检测距离 | 爵士 48 格内的存活玩家 | `entity/AppleBossEntity.java:346` |
| 必要条件一 | 玩家头衔命中 `farmingtales:sweet_apple_manor` 或 `sweet_apple_manor` | `entity/AppleBossEntity.java:140` |
| 必要条件二 | 主手或副手精确持有 `tridents_n_stuff:sanguine_bond`（或显示名精确等于「血腥契约」），不扫背包 | `boss/BossHandler.java:907` |
| 两条件关系 | 头衔与血腥契约必须同时满足，缺一不可 | `entity/AppleBossEntity.java:351` |
| 谁能选择 | 只有触发彩蛋的那位玩家拿到 `[挑战]` 与 `[放弃]` 按钮，别人点了不生效也不消耗这次确认 | `boss/HiddenBossChallenge.java:126` |
| 警告范围 | 锚点 `WARN_RADIUS` = 96 格内的玩家收到警告，其中非触发者只收到「由触发者决定」的通知；更远的玩家收不到任何提示 | `boss/HiddenBossChallenge.java:57`、`boss/HiddenBossChallenge.java:125` |
| 确认次数 | 两次确认，第一次只进第二步、不发补给；第二次才开战 | `boss/HiddenBossChallenge.java:273` |
| 触发进度的发放 | 第二次确认通过、入场演出刚开始的那一刻，**发起者本人**解锁隐藏进度「farbic的记忆……」（`ftboss:easter_egg_memory`，challenge 边框 + hidden 隐藏，中英文案在 `assets/ftboss/lang/*.json`）；在场其他玩家不重复获得 | `boss/HiddenBossChallenge.java`（`grantAdvancement`）、`data/ftboss/advancements/easter_egg_memory.json` |
| 每步超时 | `TIMEOUT_TICKS` = 60 秒（1200 tick），超时本次不开始 | `boss/HiddenBossChallenge.java:59` |
| 触发者离线 | 本次确认立即作废，全场不再干等 | `boss/HiddenBossChallenge.java:209` |
| 维度限制 | 只允许主世界 `minecraft:overworld`（`OVERWORLD_ONLY` = true） | `boss/SummonGate.java:25` |
| 开战补给 | 96 格内每名玩家 64 个金苹果 + 400 点伤害吸收 | `boss/HiddenBossChallenge.java:61`、`boss/HiddenBossChallenge.java:63` |
| 吸收时长 | `ABSORPTION_TICKS` = 12000 tick（10 分钟），到期护盾一并收回 | `boss/HiddenBossChallenge.java:65` |
| 放弃结果 | 停掉已在「选择阶段」起播的庄园 BGM，Boss 不会出现 | `boss/HiddenBossChallenge.java:308` |
| 演出前置 | 同一维度同时只能有一场庄园战（`ManorFight.hasActive` 为真则不启动演出） | `boss/HiddenBossIntro.java:82` |
| 开场清场 | 接管/开局时把属于已结束（`Retired`）场次的残留实体全部清除 | `boss/ManorFight.java:488`、`boss/ManorFight.java:693` |

## 二、入场演出时间轴

演出实体固定是**麻雀**（`DEFAULT_VARIANT` = 9），出生在锚点朝苹果爵士方向 `DISTANCE` = 15.0 格处，锚点即触发者位置（`boss/HiddenBossIntro.java:38`、`boss/HiddenBossIntro.java:86`）。演出按**真实毫秒**计时（`PresentationClock`），不是按 tick。

| 常量 | 数值 | 说明 | 出处 |
|---|---|---|---|
| `DURATION_SECONDS` | 46 秒 | 演出总时长 | `util/ManorIntroTimeline.java:4` |
| `DURATION_MILLIS` | 46000 | 服务端判定演出结束的时间 | `util/ManorIntroTimeline.java:6` |
| `DURATION_TICKS` | 920 | 46 秒换算成 tick | `util/ManorIntroTimeline.java:5` |
| `INTRO_MIN_TICKS` | 920 | 演出最短 tick 数 | `boss/HiddenBossIntro.java:40` |
| `DISTANCE` | 15.0 格 | 演出位置与锚点的水平距离 | `boss/HiddenBossIntro.java:38` |
| `DEFAULT_VARIANT` | 9 | 演出实体固定为麻雀变体 | `boss/HiddenBossIntro.java:39` |
| `VIEW_RADIUS_SQUARED` | 96 格（`96×96` 平方距离） | 只有这个范围内的玩家看得见演出与演出血条 | `boss/HiddenBossIntro.java:41` |
| `GROUND_SEARCH_UP` | 4 格 | 找站立地面时向上搜索上限 | `boss/HiddenBossIntro.java:42` |
| `GROUND_SEARCH_DOWN` | 32 格 | 找站立地面时向下搜索上限 | `boss/HiddenBossIntro.java:43` |
| `SYNC_MILLIS` | 2000（100 tick） | 演出状态对客户端重同步的间隔 | `util/ManorIntroTimeline.java:17` |
| `DIALOGUE_END_MILLIS` | 40000（2000 tick） | 5 句台词铺开的区间上限 | `util/ManorIntroTimeline.java:16` |
| `SEAL_MILLIS` | 8000（400 tick） | 封印阶段终点 | `util/ManorIntroTimeline.java:7` |
| `ORBIT_FULL_MILLIS` | 12000（600 tick） | 环绕阶段的第一个里程碑 | `util/ManorIntroTimeline.java:8` |
| `ORBIT_MILLIS` | 24000（1200 tick） | 环绕阶段终点 | `util/ManorIntroTimeline.java:9` |
| `ASCENT_MILLIS` | 36000（1800 tick） | 升空阶段终点 | `util/ManorIntroTimeline.java:10` |
| `RELEASE_MILLIS` | 43000（2150 tick） | 释放阶段起点 | `util/ManorIntroTimeline.java:11` |
| `FADE_IN_MILLIS` | 2000（100 tick） | 演出淡入 | `util/ManorIntroTimeline.java:12` |
| `FADE_OUT_MILLIS` | 1000（50 tick） | 演出结尾淡出 | `util/ManorIntroTimeline.java:13` |
| `RUNE_STEP_MILLIS` | 500（25 tick） | 符文逐个出现的间隔 | `util/ManorIntroTimeline.java:14` |
| `RUNE_FADE_MILLIS` | 3000（150 tick） | 单个符文淡入时长 | `util/ManorIntroTimeline.java:15` |

- 演出血条：红色 `PROGRESS` 条，标题「§c§l甜苹果庄园」，进度 = 已播毫秒 ÷ 46000（`boss/HiddenBossIntro.java:117`、`boss/HiddenBossIntro.java:210`）。
- 台词共 5 句，按 0 / 10000 / 20000 / 30000 / 40000 毫秒依次发给 96 格内玩家；文本为「§6哦哦，此乃吾辈之戟也。」「§6则汝当是勇士也？」「§6剿灭邪龙之事可当是真？」「§e若如此，则吾用以狩猎邪龙之剑技」「§e汝当身以受之，加以习得即可…」（`util/ManorIntroTimeline.java:21`、`entity/AppleBossEntity.java:710`）。
- 演出期间实体 `setInvulnerable(true)` + `setNoAi(true)`，只有演出正常走完才复位；中途取消走清理分支（清血条 + 停音乐 + 发复位包），不单独复位（`boss/HiddenBossIntro.java:111`、`boss/HiddenBossIntro.java:223`、`boss/HiddenBossIntro.java:258`）。
- 找不到站立地面：提示「§c【甜苹果庄园】附近没有可站立的演出地面，请在平坦地面重新触发。」并中止（`boss/HiddenBossIntro.java:101`）。
- 演出结束分支：演出实体是麻雀 → 转为场地支援位并随机抽一位开战，战场锚点取麻雀落脚点；否则由 `adopt` 直接接管该实体作为首发（`boss/HiddenBossIntro.java:225`、`boss/HiddenBossIntro.java:228`、`boss/HiddenBossIntro.java:237`）。
- 麻雀支援位血量 `SPARROW_HEALTH` = 500.0（`boss/SparrowSupport.java:53`）；支援身份由 `ManorHealthBalance.supportHealth` 统一压到 500.0，本体基础生命是 `ManorSupportRules.BASE_HEALTH` = 1500.0（`boss/ai/ManorHealthBalance.java:25-64`、`entity/EasterBossEntity.java:2078-2095`）。
- 进场计划：正式庄园在 11 位轮换里随机安排 4~10 位出场（`util/SparrowAppearanceRules.java:10-31`），单人每位 Boss 1 次、每多 1 名参战玩家额度 +1（`util/SparrowMultiplayerRules.java:9-11`）；独立彩蛋 Boss 每 `CHECK_INTERVAL` = 200 tick 判定一次，1 / 2 / 3 人概率 50% / 75% / 87.5%，全场最多 1 只（`boss/SparrowSupport.java:36-53`、`util/SparrowMultiplayerRules.java:13-15`）。
- 麻雀分摊：48 格内有活着的麻雀时，她替庄园 Boss 吃掉 `SPARROW_DAMAGE_SHARE` = 0.50（50%）的受伤，分摊量直接从她自己那 500 血里扣（不走她的受伤管线）；血不够就当场阵亡，演出 / 无敌期则留 1 血不死（`entity/EasterBossEntity.java:2751`、`entity/EasterBossEntity.java:2890-2910`）。
- 麻雀三项干扰（完整数值见 `30_manor_shared.md` 第八节）：支援有效时每 200 tick 对 48 格内每位有效玩家随机沉默一格物品并移除一个非 ambient 正面效果（`boss/ai/SparrowDisruption.java:42-95`）；每 200 tick 随机借一位庄园 Boss 的招牌法术（`integ/spells/SparrowBorrowedSpells.java:78-116`）；招牌「千羽散华」冷却 160 tick、5 枚飞羽各最多 3 次命中并附 100 tick 禁疗（`integ/spells/MaqueFeatherCombat.java:36-143`）。
- 演出被中断（实体被移除、服务器关闭）→ 清血条、停庄园音乐、把已播状态发给客户端复位（`boss/HiddenBossIntro.java:203`）。
- 开场复制增益：演出实体先设好身份 / 难度 / 召唤者 / 坐标与演出状态（无敌 + 停 AI），调 `prepareManorEntryBuffs` 复制 48 格内最近一名玩家的可用正面效果，再 `addFreshEntity` 加入世界；没有合格玩家或没有可复制增益就正常进场（`boss/HiddenBossIntro.java:99-125`、`entity/EasterBossEntity.java:3573-3601`）。
- 开场净化与场地增益：`syncViewers` 首次把 96 格内玩家接入演出时调 `ManorIntroPlayerBuffs.begin`，先移除该玩家全部正面状态（负面保留），再立即发放六项场地增益（速度 / 急迫 / 抗性提升 / 跳跃提升 / 力量 / 生命恢复，II 级、`12 × 20` = 240 tick、ambient）；每场演出对每位玩家只处理一次，重复同步与断线重连都不重复清除（`boss/ai/ManorIntroPlayerBuffs.java:14-29`、`boss/HiddenBossIntro.java:173-182`、`boss/BossHandler.java:626-633`）。
- 演出结束再复制：46 秒走完后先复制一次最新玩家增益（补回演出期间过期的 30 秒增益），随后才解除无敌与 AI 冻结并接入战斗（`boss/HiddenBossIntro.java:235-257`）。
- 后续换人：`spawnVariant` 的顺序固定为生成实体 → 设变体与召唤者 → 绑定战场 → 复制增益 → 启用 AI → 加入世界 → 公告与红圈（`boss/ManorFight.java:782-818`）；战斗中仍每 15 秒复制一次（`entity/EasterBossEntity.java:3209`）。

## 三、参战者名单与防逃课拴绳

### 3.1 名单（participants）

| 项目 | 规则 | 出处 |
|---|---|---|
| 初始成员 | 召唤者（触发彩蛋并完成两次确认的玩家） | `boss/ManorFight.java:205` |
| 主动加入 | 攻击庄园 Boss 的玩家自动入列；若本场还没有召唤者，他同时成为召唤者 | `entity/EasterBossEntity.java:2147`、`boss/ManorFight.java:370` |
| 被动加入 | 被庄园系实体（含召唤物与投射物）打到的玩家自动入列 | `boss/ManorMercy.java:63` |
| 持久化 | 名单写入 `Participants` 并随存档保存，重启时读回 | `boss/ManorFight.java:309`、`boss/ManorFight.java:277` |
| 实体归属标记 | 成员实体持久数据写 `ftboss_manor_fight` = 战场 UUID，召唤物与投射物自动继承 | `boss/ManorFightMembers.java:17`、`boss/ManorFightMembers.java:34` |
| 血条可见范围 | 距锚点 96 格内自动挂进度条，出范围自动摘 | `boss/ManorFight.java:1068` |
| 名单用途 | 拴绳只对名单内玩家生效，不做「最近玩家」补位 | `util/ManorTeleportRules.java:23` |
| 场地判定半径 | `isPlayerFighting` 取锚点 96 格，用于禁术与禁食拦截 | `boss/ManorFight.java:461` |

### 3.2 拴绳：把谁拉回来、不拉谁

| 项目 | 规则与数值 | 出处 |
|---|---|---|
| 触发一 | 参战者离**当前在场上那位** Boss 超过 `STRAY_FROM_BOSS` = 76.0 格（同值也是召回光幕与庄园领域半径） | `util/ManorTeleportRules.java:6`、`boss/ManorFight.java:1093` |
| 触发二 | 在场上那位 Boss 连续 `NO_TARGET_RECALL_TICKS` = 200 tick（10 秒）没把参战者设为目标（打小怪不算） | `boss/ManorFight.java:110`、`boss/ManorFight.java:1124` |
| 越界警告 | 距 Boss 进入 `RECALL_WARNING_DISTANCE` = 68.0 格（= 76 − 8）后，每 20 tick 发一次动作栏越界提示 | `util/ManorTeleportRules.java:8`、`boss/ManorFight.java:1094-1097` |
| 传送倒计时 | 越界或 10 秒未索敌后先进入 `RECALL_COUNTDOWN_TICKS` = 100 tick（5 秒）倒计时，倒计时结束才传送；期间回到范围内会提示「传送倒计时取消」 | `util/ManorTeleportRules.java:10`、`boss/ManorFight.java:1108-1124` |
| 边界光幕 | 半径 = `STRAY_FROM_BOSS` 的随动红金断续光幕 + 内向箭头，最后 6 格增亮；玄蓝高空施法 / 祁崽不可打断施法期间、Boss 轮换与战斗结束时隐藏 | `util/ManorRecallBoundaryGeometry.java:5`、`boss/ManorFight.java:1005-1019` |
| 挪谁 | 比较玩家与 Boss 各自到锚点的距离，**离锚点更远的那一个**挪到另一个身边 | `util/ManorTeleportRules.java:31` |
| 跨维度 | 参战者不在战场维度 → 直接把玩家拉回 Boss 身边 | `util/ManorTeleportRules.java:29` |
| 玩家落点 | Boss 位置沿 X 轴 +3 格处，8 格半径内搜索安全落点 | `boss/ManorFight.java:1182` |
| Boss 落点 | 参战者位置沿 X 轴 +3 格处，8 格半径内搜索安全落点 | `boss/ManorFight.java:1196` |
| Boss 可被传送的条件 | 与目标同维度、目标存活且非旁观 | `entity/EasterBossEntity.java:916` |
| 冷却 | 同一玩家 `RECALL_CD` = 100 tick（5 秒）最多被处理一次 | `boss/ManorFight.java:89`、`boss/ManorFight.java:1165` |
| 召回台词 | 「§c§l哼，想逃？」（仅玩家被拉走时显示） | `boss/ManorFight.java:1186` |
| 不拉谁 | 不在名单里的玩家（旁观、路人、旧存档无身份者）、已死亡、已离线、旁观模式一律不动 | `boss/ManorFight.java:1115` |
| 卡住脱困 | Boss 在同一位置附近被阻挡满 200 tick → 把 Boss 送到离它最近的参战者身边 | `boss/ManorFight.java:1147` |
| 召回记忆上限 | `RECALL_CAP` = 64 人；超过 64 且条目早于 6000 tick 就淘汰 | `boss/ManorFight.java:117`、`boss/ManorFight.java:121` |
| 坠空救援 | 连续 2 次坠空（间隔不超过 600 tick）且 200 tick 冷却就绪 → 整场安全搬迁 | `boss/ManorFight.java:553`、`boss/ManorFight.java:556` |
| 搬迁落点 | 随机方向 52 到 72 格，且新锚点与旧锚点水平距离至少 48 格，最多尝试 24 次 | `boss/ManorFight.java:585`、`boss/ManorFight.java:588` |
| 搬迁文案 | 「§6【庄园】§7战场已安全搬迁，继续挑战。」 | `boss/ManorFight.java:613` |
| 无安全落点 | 直接结束本场（原因「附近没有安全落点」，不计击败、无掉落） | `boss/ManorFight.java:570` |

## 四、轮换顺序与进度血条

| 项目 | 规则与数值 | 出处 |
|---|---|---|
| 轮换名单 | `MANOR_VARIANTS` = {0,1,2,3,4,5,6,7,8,10,11}，共 `MANOR_COUNT` = 11 位（= 名单长度） | `entity/EasterBossEntity.java:108-110` |
| 名单外 | 麻雀 `SPARROW_VARIANT` = 9 是支援位，不进轮换 | `entity/EasterBossEntity.java:104` |
| 首发出场 | 演出结束后从名单随机抽一位：`MANOR_VARIANTS[random.nextInt(MANOR_COUNT)]` | `boss/HiddenBossIntro.java:227` |
| 顺序 | 剩余未击败者洗牌进队列逐个取出，随机顺序且不重复 | `boss/ManorFight.java:227` |
| 换人条件 | 只认「当前这位真死」：`isDeadOrDying()` 或血量小于等于 0 | `boss/ManorFight.java:1018` |
| 记击败时机 | 死亡的当 tick 立刻计入已击败、刷进度条并播报 | `boss/ManorFight.java:1039` |
| 下一位延迟 | `NEXT_DELAY` = 60 tick（3 秒）后才生成下一位 | `boss/ManorFight.java:69` |
| 换人前清场 | 生成下一位之前撤掉本场不在编的庄园 Boss（麻雀与迷你分身除外） | `boss/ManorFight.java:1044`、`boss/ManorFight.java:1215` |
| 全部击败后 | 已击败数达到 `MANOR_COUNT` = 11 位 → 直接 `finish` 结算，不再生成下一位 | `boss/ManorFight.java:1043`、`boss/ManorFight.java:1157` |
| 卸载宽限 | `UNLOAD_GRACE_TICKS` = 600 tick（30 秒）查不到实体仍不判死 | `boss/ManorFight.java:78` |
| 超出宽限 | 宽限过后仍不在世界 → 取消本场（原因「Boss 卸载后无法恢复」，不计击败） | `boss/ManorFight.java:1032` |
| 区块重载 | 每 tick 按当前 Boss 的 UUID 重新绑定世界里的新实例 | `boss/ManorFight.java:1010` |
| 保区块 | 查不到当前 Boss 时每 100 tick 主动加载 `lastPosition` 所在区块 | `boss/ManorFight.java:1007` |
| 主阶段伤害 | `onDamage` 在主阶段直接返回 false（不接管 5000 血池），改由实体自算口径 `manorHurt` 结算：单次伤害 clamp 到最大血量的 `MIN_DAMAGE_PERCENT` = 0.015 ~ `MAX_DAMAGE_PERCENT` = 0.10（1500 血即每次 22.5 ~ 150 点；终结难度只卡上限），绕过原版护甲管线、先扣伤害吸收再扣血 | `boss/ManorFight.java:710`、`entity/EasterBossEntity.java:1570-1572`、`entity/EasterBossEntity.java:2582-2645` |
| 进度条外观 | 红色 + `NOTCHED_10`，标题「§c§l甜苹果庄园 §7已击败 §cn§7/§c11」（分母直接取 `MANOR_COUNT`） | `boss/ManorFight.java:212-213`、`boss/ManorFight.java:242-243` |
| 进度条数值 | 进度 = 已击败数 ÷ `MANOR_COUNT`（11） | `boss/ManorFight.java:251` |
| 上场播报 | 「§c【甜苹果庄园】§7第 §cN§7 位上场：§c名字 §8(已击败 n/11)」 | `boss/ManorFight.java:856-858` |
| 击败播报 | 「§6【甜苹果庄园】§7名字 §7已被击败！§8(进度 n/11)」 | `boss/ManorFight.java:875-877` |
| 每位基础血量 | `ManorSupportRules.BASE_HEALTH` = 1500.0（`createAttributes` 写入；`ManorHealthBalance.normalize` 每 tick 检查、按需校正并保持血量百分比），每位各自一条血条 | `entity/EasterBossEntity.java:1031-1033`、`boss/ai/ManorHealthBalance.java:46-64` |
| 人数缩放 | 48 格内每多 1 名存活非创造非旁观玩家：血量 ×1.25、伤害 +15%，每 20 tick 重算 | `boss/ai/BossScaling.java:22`、`boss/ai/BossScaling.java:54` |
| 庄园攻击力上调 | 名单内基础攻击力再叠 +25% 修饰符，功底（variant 3）除外 | `entity/EasterBossEntity.java:589` |
| 在场提示粒子 | 每 10 tick 在当前 Boss 脚下冒 `CRIT` 粒子 | `boss/ManorFight.java:1080` |

## 五、结束条件、迷你分身与额外规则

- **庄园战没有巨人阶段，也没有火柴人终章**：已击败数达到 `MANOR_COUNT` = 11 时直接 `finish` 结算胜利，十一位打完就结束。
- 战场不包含巨人生成、共享终章血池、拼装阵形、重击或特殊缩放。`isFinaleMember` / `onDamage` 只保留返回 false 的旧调用兼容入口，不扫描活动战斗，也不改变伤害。
- `setGiant` 只维护旧同步标记，不增加血量、体型或攻击。实体类型与现行主战 Boss、迷你分身、胜利演出和奖励箱均保留。

- 迷你分身（仍在生效的召唤物）：柚子（variant 0）命中 3 次召 2 个迷你分身，每个 `MINI_HEALTH` = 150.0 血、`MINI_SCALE` = 0.55 倍体型、`MINI_LIFE_TICKS` = 300 tick（15 秒）后消散，同款在场上限 `MINI_CAP` = 6，满了改为给自己速度 II 10 秒，两次分裂最小间隔 `MINI_SUMMON_CD` = 40 tick；只要场上还有活着的分身，柚子本体就少挨 `YOUZI_MINION_SHARE` = 0.10（10%）的伤害，这 10% 由所有活着的分身平摊（`entity/EasterBossEntity.java:285`、`entity/EasterBossEntity.java:290`、`entity/EasterBossEntity.java:2009`、`entity/EasterBossEntity.java:2511`）。
- 雪黎（variant 10）自爆分身：一次召 `XUELI_CLONE_COUNT` = 2 个，每个 `XUELI_CLONE_HEALTH` = 80.0 血，引信 `XUELI_CLONE_FUSE` = 100 tick（5 秒）后自爆，且不占 Boss 血条（`entity/EasterBossEntity.java:4423`、`entity/EasterBossEntity.java:4516`）。
- 分身不参与轮换：换人清场与「收编无托管 Boss」都跳过 `isMini()`；主人阵亡或退场时分身按 owner 标记立刻随主消散，兜底常量 `MINI_GRACE_TICKS` = 6000 tick（5 分钟）在当前代码里没有任何引用点（`boss/ManorFight.java:1215`、`boss/ManorFight.java:663`、`entity/EasterBossEntity.java:6456`、`entity/EasterBossEntity.java:6459`）。
- 战场额外禁制（全程生效）：黑洞 / 止心术 / 橡肤三种法术在战场内被拦截并训话（`boss/ManorBan.java:38`）。
- 战场额外禁制（全程生效）：每 10 tick 清一次玩家身上的 `kaleidoscope_cookery:satiated_shield`、`farmersdelight:nourishment`、`farm_and_charm:satiation` 三种食物系增益（`boss/ManorFight.java:1074`、`boss/ManorBan.java:41`）。
- 文玄蓝（`variant` 6）阶段有独立的竹鸟 / 月轮炮台流程与飞轮上限，见「九、玄蓝阶段：竹鸟登场、唤出玄蓝与月轮炮台」。

> ⚠ **2026-09-24 更新**：`ManorBan.BANNED_SPELLS` 现为 `black_hole` + `heartstop`（**两档难度都拦**），`oakskin` 仍只在终结难度拦；禁食方面「饱腹代偿」`kaleidoscope_cookery:satiated_shield` 改成**两档难度都清**（`ManorImpossibleRules.SHARED_BANNED_FOOD_BUFFS`），「滋养」「饱腹」仍只在终结难度清。
- 拦截反馈：屏幕中上方与聊天栏各甩一句「雕虫小技还敢班门弄斧？」或「呵，旁门左道」，并播放铁砧音效（`boss/ManorBan.java:47`）。

## 六、胜利结算与奖励箱

| 项目 | 规则与数值 | 出处 |
|---|---|---|
| 胜利条件 | 全部 11 位主战 Boss 被击败（原因记为「全部主战 Boss 已击败」） | `boss/ManorFight.java:1043` |
| 结算入口 | `endFight(victory=true)` → `ManorVictory.begin`，同时把本场 id 记入 `Retired` | `boss/ManorFight.java:408` |
| 幂等 | `ManorVictoryData.known` 已记录的场次不再发奖 | `boss/ManorVictory.java:42` |
| 演出时长 | `ManorVictoryGeometry.DURATION` = 120 tick（6 秒） | `util/ManorVictoryGeometry.java:4` |
| 标题提示 | `message.ftboss.manor_victory.title`，淡入 10 / 停留 80 / 淡出 20 tick | `boss/ManorVictory.java:67`、`assets/ftboss/lang/zh_cn.json:4` |
| 预播报 | `message.ftboss.manor_victory.soon`（「十一道纹章正在归位……」） | `assets/ftboss/lang/zh_cn.json:5` |
| 广播范围 | 距落点 128 格内，每 20 tick 广播一次 | `boss/ManorVictory.java:81`、`boss/ManorVictory.java:168` |
| 落点搜索 | 以锚点为中心，水平半径 0 到 8 格、上下 0 到 8 格找「两格空 + 脚下坚实」 | `boss/ManorVictory.java:86` |
| 宝箱放置 | 放下 `ftboss:manor_loot_chest` 并写入内容，播放 `BEACON_ACTIVATE` | `boss/ManorVictory.java:119` |
| 宝箱播报 | `message.ftboss.manor_victory.chest`（播报宝箱坐标供右键领取） | `boss/ManorVictory.java:125` |
| 宝箱容量 | `CAPACITY` = 54 格（6 行箱子），破坏方块时掉落全部内容 | `block/ManorLootChestBlockEntity.java:17`、`block/ManorLootChestBlock.java:58` |
| 发放名单 | 结算时把本场 participants 写入待发记录，兜底发放按该名单顺序取第一位存活非旁观者 | `boss/ManorVictory.java:53`、`boss/ManorVictory.java:129` |
| 无落点兜底 | 装满的宝箱打包成物品交给第一位存活非旁观参战者，背包满则掉脚边 | `boss/ManorVictory.java:129`、`boss/ManorVictory.java:142` |
| 掉落物保护 | 兜底掉落的物品无限寿命、无敌、无重力、速度为 0 | `boss/ManorVictory.java:148` |
| 兜底提示 | `message.ftboss.manor_victory.portable` | `boss/ManorVictory.java:135` |
| 奖励上限 | 奖励条目超过 54 格直接抛异常 | `boss/ManorVictoryLoot.java:29` |
| 非胜利结束 | 播报「挑战已结束（原因），不计击败、无掉落」，隐藏血条、收回 BGM、丢弃本场实体 | `boss/ManorFight.java:426`、`boss/ManorFight.java:411` |
| 重开防护 | 已结束场次的任何实体加入世界时被取消 | `boss/ManorFightMembers.java:73` |

奖励箱内容（`boss/ManorVictoryLoot.java:21`）：

- 固定武器取自 `ManorWeaponCatalog.ENTRIES`，共 20 条目（另有 13 件 TnS 附赠条目只进创造栏、不进宝箱，见 `ManorWeaponCatalog.CREATIVE_ONLY`）；取不到的条目（未装对应模组）跳过并记警告日志。
- `ftboss:statue_youzi`、`ftboss:dandan_scythe`、`ftboss:dora_nova_launcher`、`ftboss:dora_pulse_gun`、`ftboss:dora_lance`、`ftboss:black_cleaver`。
- `ftboss:lemoncat_skillet`、`ftboss:lemoncat_spatula`、`ftboss:wenxuanlan_flywheel`（绿、白各一把）、`ftboss:shadow_sword`、`ftboss:puffer_bat`、`ftboss:maque_blessing_feather`、`ftboss:xueli_skillet`、`ftboss:xueli_rolling_pin`。
- `tridents_n_stuff:stormseeker`、`tridents_n_stuff:the_desecrator`、`tridents_n_stuff:echo_of_the_deep`、`tridents_n_stuff:stellaris`。
- 额外固定 1 件 `ftboss:qingyi_confluence_trident`（`boss/ManorVictoryLoot.java:26`）。
- 装了 `irons_spellbooks` 时，为 `ManorSpellRules.Kind` 里每种已启用法术发 1 张满阶卷轴；`Kind` 全量枚举共 35 种（`boss/ManorVictoryLoot.java:75-82`、`util/ManorSpellRules.java:14-49`）。

## 七、中途退出与重登恢复

| 场景 | 处理方式 | 出处 |
|---|---|---|
| 定期存盘 | 每 100 tick 存一次战场数据，关键节点（开局、换人、击败、结束、搬迁）额外存 | `boss/ManorFight.java:1061` |
| 存档内容 | 战场 id、维度、锚点、Boss 坐标、当前变体与 UUID、已击败集合、参战者名单 | `boss/ManorFight.java:297` |
| 存档位置 | 主世界 DataStorage 的 `ftboss_manor_fights` | `boss/ManorFightData.java:18` |
| 服务器关闭 | 先 `persist()` 存盘，再取消全部入场演出、摘掉所有进度条并清空内存态 | `boss/ManorFight.java:944` |
| 服务器重启 | 读回战场重建（跳过 `Retired` 场次），按已击败数重刷进度条 | `boss/ManorFight.java:263` |
| 玩家重登 | 距锚点 96 格内则重挂进度条并重推标题与进度 | `boss/ManorFight.java:628` |
| 重登入口 | `PlayerLoggedInEvent` 调用 `ManorFight.reattach` | `boss/BossHandler.java:485` |
| 奖励补发 | 未交付的凯旋奖励持久化在 `ftboss_manor_victories`，每 20 tick 重试放置宝箱或打包给参战者 | `boss/ManorVictoryData.java:19`、`boss/ManorVictory.java:171` |
| 整局重启丢内存态 | 每 40 tick `recoverNear` 收编玩家 160 格内无托管、非迷你、非麻雀、非演出中的庄园 Boss | `boss/BossHandler.java:44`、`boss/ManorFight.java:662` |
| 当前 Boss 区块卸载 | 只累计 `unloadedTicks`，600 tick 内不判死，并按 UUID 重新绑定实例 | `boss/ManorFight.java:1025` |
| 玩家死亡后 | 复活的参战者若离在场 Boss 超过 76 格（`STRAY_FROM_BOSS`），会被拴绳拉回 | `util/ManorTeleportRules.java:6` |
| 血条脏数据 | 每 tick 清掉血条列表里已断线的旧 `ServerPlayer` 对象 | `boss/ManorFight.java:1065` |
| 结束原因清单 | 「全部主战 Boss 已击败」「Boss 卸载后无法恢复」「附近没有安全落点」「召唤者投降」「战场重置」 | `boss/ManorFight.java:937`、`boss/ManorFight.java:1033`、`boss/ManorFight.java:570`、`boss/ManorFight.java:361`、`boss/ManorFight.java:528` |

## 八、怜悯与不怜悯的边界

| 项目 | 规则与数值 | 出处 |
|---|---|---|
| 计数键 | （玩家 UUID，战场 id，当前 Boss UUID）三元组，换 Boss 即另起一份计数 | `boss/ManorMercy.java:31` |
| 致死归因 | 致死伤害的来源实体属于本场庄园系（含召唤物与投射物）才计数 | `boss/ManorMercy.java:56` |
| 无来源兜底 | 没有攻击者时，用 200 tick（10 秒）内最后一次被庄园系命中的记录 | `boss/ManorMercy.java:79`、`boss/ManorMercy.java:84` |
| 失效条件 | 死亡时当前 Boss 已经换人 → 本次死亡不计 | `boss/ManorMercy.java:85` |
| 不在本场 | 取不到战场 id 或当前 Boss 时直接不计数（非庄园战没有怜悯询问） | `boss/ManorMercy.java:62` |
| 询问间隔 | 每 `ASK_EVERY` = 10 次死亡问一次 | `boss/ManorMercy.java:30`、`boss/ManorMercy.java:93` |
| 询问内容 | 「§6【挑战询问】§e你已在本场「Boss」下倒下 N 次。」并附 `[继续挑战]`；只有召唤者额外拿到 `[放弃挑战]` | `boss/ManorMercy.java:96` |
| 按钮有效期 | 生成后 12000 tick（10 分钟）内有效 | `boss/ManorMercy.java:94` |
| 继续（怜悯） | 清零该 Boss 的死亡计数，战斗继续，不清场也不结算 | `boss/ManorMercy.java:137` |
| 投降（不怜悯） | 只有召唤者能点，整场立即结束、不计击败、无掉落 | `boss/ManorMercy.java:129`、`boss/ManorFight.java:361` |
| 非召唤者 | 只提示「整场投降由召唤者决定。」，不给放弃按钮 | `boss/ManorMercy.java:104` |
| 按钮失效 | token 不匹配、超时或 Boss 已换人 → 提示「旧按钮已失效」且不生效 | `boss/ManorMercy.java:125` |
| 计数持久化 | 写入 `Deaths` 存档，重启时 `ManorMercy.restore` 读回 | `boss/ManorMercy.java:177`、`boss/ManorFight.java:262` |
| 清理时机 | 当前 Boss 被击败时清空该场计数；整场结束时也清空 | `boss/ManorFight.java:764`、`boss/ManorFight.java:413` |
| 入场提醒 | 挑战确认时固定提示「连续死亡 10 次后出现投降询问、投降不算通关、不发胜利宝箱」 | `boss/HiddenBossChallenge.java:166` |

## 九、玄蓝阶段：竹鸟登场、唤出玄蓝与月轮炮台

- 轮到文玄蓝（`variant` 6）时，`ZhuniStage` 先把玄蓝本体隐藏：`noAi` + 无敌 + 隐形 + 演出隐藏，血条改名「竹鸟 · 玄蓝」且只推给有效参战玩家，同时在玄蓝位置生成竹鸟；等待期间 `EasterBossEntity.tick` 开头的 `ZhuniStage.hold` 直接接管并返回，玄蓝本体的常规 tick 完全不跑（`turret/ZhuniStage.java:26-64`、`turret/ZhuniStage.java:82-112`、`entity/EasterBossEntity.java:3033-3036`）。
- 竹鸟是独立实体 `ftboss:zhuni`：生命固定 1 点、每 tick 把血量写回 1、击退抗性 1，绕在玄蓝上方跟随；只有本场有效参战玩家的攻击会被受理（`turret/ZhuniEntity.java:21-68`、`turret/XuanlanTurrets.java:28-30`）。
- 唤出玄蓝：玩家打中竹鸟后，`ZhuniEntity.hurt` 把这次攻击的原始伤害原样转给玄蓝的受伤管线（共用玄蓝的生命池），竹鸟保持 1 血不单独死亡；`ZhuniStage.reveal` 恢复玄蓝原来的 AI / 无敌 / 隐形状态、血条转正，并广播「§a竹鸟振翅！§b玄蓝登场。」（`turret/ZhuniEntity.java:70-85`、`turret/ZhuniStage.java:66-80`）。
- 炮台投放：唤出后 `TURRET_COOLDOWN` = 100 tick（5 秒）投第一座，之后每 5 秒一座；单人时围绕玄蓝 3 格投一座，多人时改为在每位有效参战玩家脚边各投一座、每人一条独立 5 秒周期，某位玩家投失败只让他 20 tick（1 秒）后重试（`turret/ZhuniTurretDrops.java:23-47`、`balance/ManorExpansionRules.java:8`、`balance/ManorExpansionHooks.java:122-149`）。
- 炮台本体：150 生命 / 50 护甲、`noSave`、不自然消失；部署 20 tick 后每 40 tick 开火，选 32 格内最近的有效玩家，光束沿直线对每名有效参战玩家各结算一次，伤害等于玄蓝「光束打击」口径的 1/3；同一次开火再射出 3 枚一次性飞轮，每枚同样按玄蓝飞轮对应伤害的 1/3 结算（`turret/XuanlanTurretEntity.java:29-48`、`turret/XuanlanTurretEntity.java:65-112`、`turret/TurretFlywheelEntity.java:45-88`、`balance/ManorExpansionRules.java:5-9`、`balance/ManorExpansionRules.java:27-29`）。
- 归属与清理：炮台与一次性飞轮都继承 `ftboss_manor_fight` 归属；玄蓝死亡、退场或变体变化时它们自行消散，炮台还额外要求仍在本场战场范围内（`turret/XuanlanTurretEntity.java:65-72`、`turret/TurretFlywheelEntity.java:49-56`、`turret/ZhuniTurretDrops.java:73-79`）。
- 飞轮硬上限：`FlywheelProjectileEntity.MAX_FLYWHEELS` = 15，本体三处内联判定（状态日志、技能换挡、投掷）都用它；`XuanlanEncounterLimits` 在本人飞轮加入世界时拒绝超额，并每秒按实体 ID 保留最早的 15 个（旧存档超量也会被裁掉）；竹鸟炮台的一次性飞轮是独立实体 `ftboss:turret_flywheel`，不计入这 15 个（`entity/FlywheelProjectileEntity.java:81`、`entity/EasterBossEntity.java:3374-3380`、`boss/ai/XuanlanEncounterLimits.java:24`、`boss/ai/XuanlanEncounterLimits.java:59-79`、`turret/XuanlanTurrets.java:31-33`）。
- 玄蓝阶段不接待麻雀：`spawnSparrow` 与 `ImpossibleSparrowSupport.ensure` 在生成 / 接管前直接拒绝玄蓝，麻雀 tick 先清掉属于本场或直接归玄蓝的残留支援麻雀（并释放终结难度的持久化占位），再过滤玄蓝支援目标（`boss/ai/XuanlanEncounterLimits.java:28-47`、`boss/SparrowSupport.java:252-253`、`boss/ImpossibleSparrowSupport.java:102-103`、`entity/EasterBossEntity.java:2775-2797`）。
