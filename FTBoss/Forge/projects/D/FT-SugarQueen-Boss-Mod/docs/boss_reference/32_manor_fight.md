# 甜苹果庄园 · 整场流程与规则手册

## 触发与召唤

- 触发条件（`AppleBossEntity.tick`）：附近玩家**同时**满足「称号中含【甜苹果庄园】」（`BossHandler.hasTitle`，对应 `farmingtales:sweet_apple_manor`）且「**主手或副手**持有【血腥契约】」（`BossHandler.matchedContract`，只查主手/副手、不扫背包；id 精确等于 `tridents_n_stuff:sanguine_bond`，或显示名去空白后精确等于"血腥契约"）。
- 满足后苹果爵士停止战斗，走 `AppleBossEntity.ascendToNextBoss` 做头衔共鸣特效（`WITHER_SPAWN` 音 + 200 颗粒子）并 `discard` 自己（**不是被击败**，不触发石像/武器掉落），然后调 `HiddenBossChallenge.ask(sl, 触发者, 玩家位置, 爵士位置, 5 句台词)`（**v113 起第一个参数是触发者本人**）。
- 维度闸：`Challenge.ask` 先过 `SummonGate.isAllowedDim` —— 非主世界直接不弹确认；点【挑战】时再过一次 `SummonGate.allow`，不通过则原地作废。
- 确认弹窗（`HiddenBossChallenge`）：向 `WARN_RADIUS = 96.0` 格内所有玩家发「难度极高」警告，但 **v113 起 [挑战]/[放弃] 两个可点按钮只发给"触发彩蛋的那位玩家"**（按钮本质是发命令 `/ftboss challenge` 与 `/ftboss giveup`）—— 其他人只收到「本场彩蛋由 X 触发 —— 由他决定是否挑战，请稍候」，**没有按钮**；非触发者就算自己敲这两条命令，也只会被提示「只能由他来选择」，并且 **不消耗掉这次确认**（`accept` / `decline` 开头做 UUID 校验后 `return 0`）。
- 触发者身份来源：`AppleBossEntity.tick` 里那个**同时**满足「称号含甜苹果庄园」+「主副手持有血腥契约」的玩家（`ascendToNextBoss(sl, p)` 的 `p`），`Offer` 里存下他的 UUID 与名字快照。
- 超时：`TIMEOUT_TICKS = 60×20`（60 秒）无人选择 → 作废并发一条提示（`onServerTick`）；**触发者中途离线则立刻作废**（按钮只在他手里，别人点不了，没必要让全场干等满 60 秒），播报「触发者 X 已离线 —— 本次挑战确认作废」。
- 点【挑战】的补给（`giftToPlayers`）：96 格内每名玩家 `GIFT_APPLES = 64` 个金苹果（常量名 `GIFT_APPLES`）+ 伤害吸收 `ABSORPTION_POINTS = 400` 点，时长 `ABSORPTION_TICKS = 10×60×20`（10 分钟），用原版吸收效果等级 `ABSORPTION_POINTS / 4 - 1 = 99` 实现。
- 点【放弃】或超时：停掉已起播的庄园序曲，Boss 不会出现。

## 开场演出（`HiddenBossIntro`）

- 出场位：优先保留离玩家 `DISTANCE = 15.0` 格的出场方向，脚底对齐最近的可站立地面，不额外抬高；识别半砖与台阶碰撞面并检查身体净空。原落点不可站立时检查玩家锚点，仍无地面则取消并提示。面朝玩家；默认出场者 `DEFAULT_VARIANT = 9`（麻雀）。
- 演出状态：`setInvulnerable(true)` + `setNoAi(true)`，**演出期间无敌且站定**；自带血条隐藏，改用演出专用血条（标题「甜苹果庄园」，`BossBarOverlay.PROGRESS`，进度在 46 秒内从 0 填满）。
- 时长：`ManorIntroTimeline.DURATION_SECONDS = 46`，覆盖当前《双轨坦克曲》约 46 秒的前奏。单调时钟计时，不随帧率/TPS变慢；单人暂停时同步冻结。五句台词在 0、10、20、30、40 秒发送给演出 96 格内玩家。
- 独立客户端演出：0–8 秒金红苹果阵纹与翠绿叶片、渐变符文点亮；8–24 秒红/金/翠绿/紫晶四晶体环绕，带渐隐拖尾与少量光屑；24–36 秒渐变光柱、双螺旋和冠纹光环上升；36–43 秒转暖金收束；43–46 秒金红双环扩散与淡出。光柱最高 26 格。
- 性能：专用网格绘制，不生成粒子或末影水晶实体。单材质批次，普通/精简档峰值 833/386 个四边形；精简档保留四色晶体、短拖尾和单螺旋。服务端每两秒同步一次状态，离场、中止、结束与断线清理。详细参数与验证见 `docs/MANOR_INTRO_FX.md`。
- 演出期间只显示皮肤：渲染层 `EasterBossRenderer` **只注册了 `ItemInHandLayer`（武器照样渲染）**，`HumanoidArmorLayer` 整段被注释掉 —— 腾炎套照穿照生效，只是不画出来，64×64 自定义皮肤完整可见。
- 演出期间自动隐藏自带血条：`EasterBossEntity.tick` 中 `bossBar.setVisible(isAlive() && !(isInvulnerable() && isNoAi()))`。
- 演出结束（经过 46 秒）：解除无敌与站定、清理演出并播一次信标收尾音；若出场者是麻雀则 `supportSparrow` 把她登记为支援位留在场地，再从 `MANOR_VARIANTS` 里**随机**抽首发并 `ManorFight.start`；否则由 `ManorFight.adopt` 直接接管。随后 `BossMusic.startManorBattle` 切战斗曲。
- 卡死看门狗：实体处于 `NoAi` 且既不在演出、也不在战场托管时，满 `200` tick 自动解除无敌与站定（`EasterBossEntity.tick`）。
- 音乐：弹确认时 `BossMusic.beginManorPhase` 接管附近玩家的音乐状态并保持安静；点【挑战】`playManorCue` 播放《双轨坦克曲》，演出结束后 `startManorBattle` 先停演出曲，再顺序播放《鏖战》与《双轨·战斗高潮》。放弃或超时由 `stopManorCue` 清理演出状态。

## 战斗曲与进度条

- 战斗播放列表固定为《鏖战》→《双轨·战斗高潮》，每首单次播放。前曲实际结束后才接后曲，不依赖预估时长强制截断，也不循环回第一首。
- 每名玩家同时最多一个音乐实例；进入半径 96 格，保留半径 128 格。同一演出或战斗重复调用播放入口不会重新起播。
- 庄园音乐会话绑定战场身份，战场仍存在时不因成员轮换而重新开始；没有战场身份的单独召唤保留 100 tick 空场宽限。
- 庄园成员死亡不播放落幕钟声。柠檬猫召来的食物系 Boss 标记 `ftboss_no_bgm`，不会抢占或追加音乐。
- 进度条（`ManorFight.Fight.bar`）：`BossBarColor.RED` + `NOTCHED_10`，标题格式为「§c§l甜苹果庄园 §7已击败 §cN§7/§cMANOR_COUNT」，进度 = `已击败数 / MANOR_COUNT`（`progressTitle` / `updateBar`）；`BAR_NAME = "甜苹果庄园"`。
- 上场播报（`spawnVariant`）：聊天栏「【甜苹果庄园】第 N 位上场：<名字>（自己 <血量> 血 · 已击败 n/10）」，剧情文本见 `ManorFight` 第 438~441 行。
- 击败播报（`onCurrentDefeated`）：「<名字> 已被击败！（进度 n/10）」。

## 十人轮换与战场规则

- 名单与人数：`EasterBossEntity.MANOR_VARIANTS = {0,1,2,3,4,5,6,7,8,10}`，共 `MANOR_COUNT = 10` 位；麻雀（`SPARROW_VARIANT = 9`）**不进轮换**。
- 血量：每人**独立 1000 血、各有各的血条**（`createAttributes` 的 `MAX_HEALTH = 1000.0`）；`ManorFight.onDamage` 在主阶段**直接返回 false，不接管伤害**（早期"共享血条"已废弃）。
- 上场顺序：`Fight.pool` 是 `MANOR_VARIANTS` 去掉已击败者后的洗牌队列（`refillPool`），`nextVariant()` 逐个取出 → **顺序随机、不重复**；首发由演出结束时随机抽（`MANOR_FIRST_VARIANT_ALLOWED = true`）。
- 换人规则：**必须打死当前这位**才上下一位。`pumpAll` 每 tick 判定：真假死 = `cur == null || isDeadOrDying() || getHealth() <= 0`；区块卸载导致查不到实体时只累计 `unloadedTicks`，超过 `UNLOAD_GRACE_TICKS = 30×20` 才按已击败处理并告警。死亡后先等 `NEXT_DELAY = 60` tick（3 秒，留出播报与拾取），再 `clearStrayManorBosses` 清场并 `spawnVariant` 下一位。
- 死亡结算：`manorHurt`（主阶段绕开原版护甲/抗性管线）—— 单次伤害 clamp 到最大血量的 `MIN_DAMAGE_PERCENT = 0.015` ~ `MAX_DAMAGE_PERCENT = 0.10`（**15 ~ 100 点**），先扣伤害吸收再扣血，无敌帧 10 tick，血量 ≤ 0 立即 `die` + `discard`（不给被奶回 >0 的假死窗口）；`BYPASSES_INVULNERABILITY`（`/kill`、掉出世界）直接秒杀。
- 玩家离场召回（`recallStrayPlayers`，v94）：触发条件为「玩家离**在场那位** > `STRAY_FROM_BOSS = 30.0` 格」或「在场那位连续 `NO_TARGET_RECALL_TICKS = 10×20` tick 没锁定到玩家（打小怪不算）」；动作是拿两人的"离场地距离"比较，**离场地更远的那个挪到另一个身边**（各 3 格、带传送门粒子，玩家侧提示「哼，想逃？」）。同一玩家 `RECALL_CD = 5×20` 最多被拉一次；跨维度的登记玩家也会被拽回战场。参与判定半径 `ATTACH_RANGE = 96.0`。
- 清场：`purgeStale`（开局/adopt/结束）与 `clearStrayManorBosses`（每次换人前）撤掉所有不在本场名单里的彩蛋 Boss；麻雀与迷你分身不在清理范围（结算 `finish` 时连麻雀一起撤并 `SparrowSupport.forgetSparrow()`）。
- 进度持久化与恢复：`reattach`（玩家重进存档时重挂进度血条并重推标题）、`recoverNear`（每 40 tick 扫玩家 160 格内"没有战场托管"的庄园 Boss，就地按进度 0 重建战场，覆盖"重开游戏丢静态内存态"）。
- 血条可视：在场者脚下持续冒 `CRIT`（每 10 tick）；主阶段**不隐藏**各人自带血条，终章成员则统一隐藏（`eb.getBossBar().setVisible(!f.finale)`）。

## 终章（最后的 Boss）

- 进入条件：`f.defeated.size() >= MANOR_COUNT`（十位全部被击败）→ `startFinale`。
- 形态：**不再单独生成放大模型**，由 `FORMATION` 里十位（`0~8` + `10` 雪黎）升空拼成**巨型火柴人** —— 柚子=头、蛋蛋=上胸、朵拉=下胸、功底=左臂、柠檬猫=右臂、青衣=左手、文玄蓝=右手、折耳根=左腿、紫菜=右腿、**雪黎=头顶（站在柚子头上当帽子）**。人形中心离地 `FORMATION_HEIGHT = 5.5` 格，整体高约 8 格、宽约 8 格。
- 部件性质：这十位是最终 Boss 的**身体部件** —— `setInvulnerable(true)` + `setNoAi(true)` + `setNoGravity(true)`，血条隐藏、名字不显示；打它们**不掉它们的血**，伤害统一进 5000 血池（`onDamage` 在 `finale` 阶段接管）。
- 血池与减伤：`FINALE_HP = 5000.0`，进池伤害 ×`FINALE_DEFENSE_MULT = 0.60`，血条进度 = `finaleHp / FINALE_HP`，打空即 `finish`。
- 骨架与轮廓：`SKELETON` 十条连线用 `SOUL_FIRE_FLAME` 粒子勾出骨头，另在头/双手/双脚画 4 个小环（半径 1.3 / 0.7）；每 tick 由 `finaleUpkeep` 重画。
- 移动：整个火柴人朝 64 格内最近玩家移动，速度 `FIGURE_STEP = 0.09` 格/tick（≈1.8 格/秒），离玩家 `FIGURE_STOP = 4.0` 格内停步。
- 巨人重击（`giantSlam`）：每 `SLAM_INTERVAL = 60` tick 一次，以人形中心为圆心、8 格内玩家吃 `SLAM_DAMAGE = 25 × 4/3 ≈ 33.3` 点（若无攻击者实体则按旧有效值 25 点的 magic 结算）、击飞 0.8、缓慢 II 100 tick（5 秒）。
- 另有 `GIANT_*` 常量组（`GIANT_SCALE = 3.5`、`GIANT_HEALTH = 5000.0`、`GIANT_STOMP_RADIUS = 9.0`、`GIANT_STOMP_DAMAGE = 30 × 4/3 = 40`）属于 `setGiant` / `giantStomp` 那条"单只 3.5 倍体型大巨人（碰撞箱 2.4×6.0、每 3 秒践踏）"的旁路实现；当前庄园终章走的是 `ManorFight` 的十人火柴人 + 5000 血池，不走 `setGiant`。
- 终章副作用：终章成员在 `tick()` 里被 `ManorFight.isFinaleMember` 直接 `return`（只站位，不同步玩家增益、不净化、不放技能、不抓人）；`recallStrayPlayers` 在终章直接让路（巨人自己会走向玩家）。

## 禁术拦截与禁用增益

- `ManorSpellBan`（全局事件监听）：玩家在庄园战场内（`ManorFight.isPlayerFighting`，锚点 96 格内）施放被禁法术时取消，并调 `ManorBan.scold`。
- 禁用清单（`ManorBan.BANNED_SPELLS`，只比对命名空间后的 path）：`black_hole`（黑洞）、`heartstop`（止心术）、`oakskin`（橡肤）。
- 禁用食物系增益（`ManorBan.BANNED_FOOD_BUFFS`，按完整注册名）：`kaleidoscope_cookery:satiated_shield`（饱腹代偿）、`farmersdelight:nourishment`（滋养）、`farm_and_charm:satiation`（饱腹）。

> ⚠ **2026-09-24 更新**：`ManorBan.BANNED_SPELLS` 现为 `black_hole` + `heartstop`（**两档难度都拦**），`oakskin` 仍只在终结难度拦；禁食方面「饱腹代偿」`kaleidoscope_cookery:satiated_shield` 改成**两档难度都清**（`ManorImpossibleRules.SHARED_BANNED_FOOD_BUFFS`），「滋养」「饱腹」仍只在终结难度清。
- 清除节奏：`pumpAll` 里每 10 tick 对战场内玩家调 `ManorBan.purgeFoodBuffs`，清到至少一个就训话一次。
- 训话文案（`ManorBan.LINES`，法术与增益共用）：二选一随机 —— 「雕虫小技还敢班门弄斧？」「呵，旁门左道」；屏幕中上方 + 聊天栏留痕 + `ANVIL_LAND` 音效。

## 麻雀支援（`SparrowSupport`）

- 只服务隐藏 Boss：v106 起 `bossesNear` 的结果会 `removeIf` 掉所有非 `EasterBossEntity`，**非隐藏 Boss 的战斗一律不刷**。
- 判定节奏与概率：每 `CHECK_INTERVAL = 20×10` tick（10 秒；v118 由 30 秒缩短）扫一次；玩家 `FIGHT_RANGE = 48.0` 格内需有彩蛋 Boss（`MIN_BOSSES = 1`）且确实在交战（在庄园战场内，或该 Boss 正索敌该玩家），再通过 `CHANCE = 0.5`（v118 由 0.22 上调）概率判定。
- 唯一性：`countAliveSparrows` 全维度扫描，场上已有任何活着的麻雀就不再刷（`DUPLICATE_RANGE = 96.0`）；`spawnSparrow` 前再兜底查一次。
- 生成与属性：生成在 Boss 上方环绕 6 格处，血量 `SPARROW_HEALTH = 500.0`，名字常显「§e麻雀」，`setPersistenceRequired`。
- 支援内容：治疗 / 上增益 / 净化 / 双向共享增益 / 分摊 50% 伤害 / 10% 概率禁疗 30 秒（数值见 `31_manor_variants.md` 的麻雀小节）。
- 退场：寿命 `SPARROW_LIFE_TICKS = 90×20`；庄园 `finish` 时 `purgeStale(sl, null, false)` 连她一起撤并 `forgetSparrow()`。

## 通关奖励与掉落

- 击败每人：`xpReward = 180`（构造函数设置；迷你分身与大巨人被 `setMini`/`setGiant` 置为 0）。十位各给一次。
- 武器掉落：`EasterBossEntity.dropWeapon()` 会掉一个自己的武器（`WEAPONS[变体]`），但**整个工程没有任何地方调用它**（全仓只有定义处）—— 即实际不掉武器；且 `equipWeapon` 把主手掉落率设为 0（`setDropChance(MAINHAND, 0.0f)`），护手同理。终章十位身体部件在 `finish` 中被 `discard`，无掉落。
- 通关结算（`finish`）：撤掉场上全部成员（带 `TOTEM_OF_UNDYING` 粒子）、进度条清零并 `removeAllPlayers`、清场、`SparrowSupport.forgetSparrow()`、播 `WITHER_DEATH`，向 96 格内玩家播报「【甜苹果庄园】已被平定。」。代码里**没有额外通关奖励/纪念品发放**（代码里未找到 ?）。
- 结算前玩家能白拿的只有挑战补给（金苹果 ×64 + 400 点伤害吸收 10 分钟）。

## 重开规则

- 打完一场后 `ManorFight.ACTIVE` 里的该场被移除、10 位进度丢弃；**再打一次就是从 0/10 重新开始**，名单重新洗牌（`Fight` 每次构造/`refillPool` 都重洗）。
- 重开入口与第一次完全相同：苹果爵士再次被"甜苹果庄园称号 + 血腥契约"玩家打到触发头衔共鸣 → 再弹一次挑战确认。爵士的蜕变没有写入任何"已完成"标记，`ascendToNextBoss` 每次都照走。
- 中途放弃（点【放弃】或 60 秒超时）＝ 什么都没发生：不会生成演出实体、不会创建战场，可立刻重来。
- 战斗中途服务器重启：静态内存态丢失，`recoverNear` 会把场上残留的庄园 Boss 就当地重建战场，**进度从 0 起算**（日志会打 warn）。
- 残留清理：`purgeStale` 会把不在本场名单里的彩蛋 Boss 撤掉，所以上一场没打完就重开不会出现"两位同时在场"。

## 应对要点

- 开场确认阶段先把 64 个金苹果和 400 点吸收吃满再点【挑战】—— 补给在点【挑战】瞬间才发，演出 30 秒内不会再发第二次。
- 庄园主阶段单次伤害被 clamp 到 15~100 点：堆爆发不如堆**频率与持续输出**；500 点全局限伤已取消。
- 每一位都自带 `ARMOR 12` + 满附魔 8 级护甲 + 抗性 II + 再生 II + 力量 II，且中减益 2 秒后自净（增益保留）：靠减益磨血基本无效，靠物理/魔法混合输出更稳。
- 麻雀在场时她会替 Boss 吃 50% 伤害、每 3 秒奶 1% 最大生命并上 3 个增益，还每 5 秒净化队友 —— **优先清麻雀**（500 血）。清掉后 `finish` 前不会再刷第二只。
- 麻雀的禁疗（10% 概率 / 30 秒）与雪黎飞刀的禁疗（必中 / 30 秒）都会封住金苹果、生命恢复与一切 `heal()`；被禁疗时改吃伤害吸收而不是回血，或先撤离等 30 秒。
- 文玄蓝远距离靠"超能光束"（蓄能 15 tick 后**方向锁定**）：看到红色收缩环 + 预警线就横向侧移，引导期不再追踪。同时贴到 5.25 格内会被飞轮绞杀持续磨，8~16 格又正好触发高伤连招 —— 距离管理是他的核心解法。
- 雪黎上场第一招固定是禁疗飞刀；她的护法（2~6 位带标签的车万女仆）活着时**完全免伤**，必须先把女仆全清才能推进；自爆分身别贴着打（打死也会炸，只是半径 3.375、基准 14）。
- 紫菜的河豚池是**每 2 种、15 秒、可叠到 V 级**，并且每次都会抽掉你一个正面效果：进场前别把关键增益压在少数几个效果上；已排除失明/黑暗/漂浮/反胃类与铁魔法、`rajswordmod` 全命名空间。
- 玩家离在场那位超过 30 格、或那位连续 10 秒锁不到玩家：谁离场地更远谁被挪到对方身边（同一人 5 秒最多一次）。别指望跑远拖时间。
- 终章十位是"身体部件"：打他们只掉 5000 血池（进池伤害 ×0.6），火柴人会以 0.09 格/tick 走向你、每 3 秒 8 格内 33.3 点重击 —— 保持移动、别站桩。

