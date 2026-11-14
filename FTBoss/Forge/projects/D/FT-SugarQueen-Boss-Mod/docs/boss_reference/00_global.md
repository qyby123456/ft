# 全局公共规则

所有 Boss 共用的数值与机制。口径：`BossHandler.BUILD_TAG = "easter-v108-barrel-crossbow"`。

## 全局伤害体系

- 彩蛋系（庄园十人及其投射物/召唤物）总倍率 `EasterBossEntity.BOSS_DAMAGE_MULT = 4.0/3.0`（≈1.3333）。
- 含义：基础倍率原为 ×2，已**烘焙**「Boss→玩家 全局减半 ×0.5」+「彩蛋系 +1/3 ×4/3」= 2.0 × (0.5 × 4/3) = 4/3；所有「× BOSS_DAMAGE_MULT」的数字**就是玩家实际吃到的伤害**。运行时伤害钩子已删除（v77，`BossDamageTuning` 类注释）。
- 非彩蛋系（食物系 Boss/召唤物/炮弹）已逐个乘 0.5（近战 `ATTACK_DAMAGE` 同）；无攻击者实体的伤害（虚空/真实/火焰/粉碎轮）保持原样，爆炸伤害按威力对半。
- 其它乘子：`WEAPON_DAMAGE_MULT = 5.0`（近战 9 → 45）、`SKILL_MULT = 3.0`、`SKILL_RADIUS_MULT = 1.5`（只放大半径），出处 `EasterBossEntity.java:93-96`。
- 真实伤害/穿甲取法 `BossDamageTuning.trueDamage(...)`：按 `electrocution → assassination → hemorrhage → frenzied_burn → venom → cacophony → doom` 在 armamentarium 命名空间取**第一个存在**的类型，全取不到退回 `sl.damageSources().magic()`。仅两处使用：功底血怒 2% 附伤（`:3000`）、「被击中」四分之一真伤（`:3443`）。
- 彩蛋系魔法减伤 `MAGIC_DAMAGE_REDUCTION = 0.40f`（判定含 `WITCH_RESISTANT_TO` 标签、path 含 magic/spell/sonic、或 msgId 含 magic/spell）。

## 限伤与无敌帧

- 鼠群主从循环跳过全局“最终伤害超过 300 点归零”守卫；其他 Boss 不变。护盾只放行绕过无敌的清理伤害或达到主王最大生命的玩家单次高额伤害。

- 原“单次伤害 >500 点整下无效”规则已取消；高额单次伤害按正常伤害管线结算。
- 伤害入口仍统一经过 `gate(...)`；彩蛋十人、堆肥与苹果保留的 `nullified(...)` 只是兼容入口，当前永远放行。
- `LIMIT_ENABLED = false`：传奇怪物式的「单次上限 + 连击递减 + 反逃课距离」**当前整套关闭**，以下数字仅在改回 true 后生效。
- 单次上限 `DAMAGE_CAP`：女皇 58.0、屠夫 58.0、沙耶 56.0、酒桶王 58.0、鼠群 46.0、未登记 `DEFAULT_CAP = 48.0`。反逃课距离 `ANTI_CHEESE`：女皇/屠夫/酒桶王/鼠群 32.0、沙耶 40.0、故障机器人 36.0、兜底 `DEFAULT_ANTI_CHEESE = 32.0`（超距该次完全免伤）。
- 连击常量：`ADAPT_TICKS = 24`、`FACTOR_STEP = 6`、`FACTOR_MIN = 20`、`FACTOR_MAX = 100`、`COMBO_FLOOR = 0.7F`、受击无敌帧 `HURT_CD = 30`。
- 完全免疫：`IMMUNE_NAMESPACES` 为**空集**（v83 起命名空间不再作为免伤依据）；`IMMUNE_IDS = hemorrhage, blood_loss, bloodloss, bleed, bleeding, doom, impending_doom, executioner, assassination, desecration, true_damage`，命中 path 或 `getMsgId()` 子串即完全免伤。免疫/限伤/反逃课提示同一 Boss 40 tick 最多一条。

## BossHandler 事件

- `onServerTick`（END）：40t 跑 `BossBars.sweep` + `BossMusic.sweep` + `ManorFight.recoverNear`；20t 对每个登记 Boss 跑 `BossScaling.tick` 与 `BossEmpower.tick`；10t 跑 `BossAggro.tick`；200t 跑 `MeatBoss.purgeStrays`。
- 信标增益 `BEACON_EFFECTS`（六项均 II 级 = amplifier 1）：速度、急迫、抗性提升、跳跃提升、力量、生命恢复。触发：玩家 64 格内有本模组 Boss，每 10 tick 检查；补发条件「无该效果 或 amplifier < 1 或剩余 < 60 tick」，时长 12×20 = 240 tick（`applyBeaconBuffs`）。
- `ForgeBlessing`：每 `PERIOD_TICKS = 1200`（60 秒）一次；`BUFF_TICKS = 500`（25 秒）；`BUFF_AMPLIFIER = 3` 即 **IV 级**；只清 `MobEffectCategory.HARMFUL` 减益（套装效果是 NEUTRAL，不会被自己删）；`ONLY_WHEN_BOSS_NEARBY = true`，只在 `FightState.inFight` 为真时发。
- 三件套装效果 `EFFECT_IDS`：`armamentarium:adamantine_effect`（金刚不坏）、`damon_effect`（死亡王子）、`lone_graveseeker_effect`（寻墓孤人/幽匿共生体）；取不到记 warn 跳过。
- Boss 互相免疫 `onAllyFriendlyFire`：攻击者与受害者同属 Boss 阵营且攻击者不是玩家时取消事件（投射物先取 `getOwner()`）；阵营名单见 `isBossSide`，共 16 类实体。苹果阵营另有 `onAppleFriendlyFire`，对 `isAppleSide` 实体取消全部 `IS_LIGHTNING` 与 `IS_EXPLOSION` 伤害。
- Boss 免疫一切负面效果 `onEffectApplicable`：女皇/屠夫/茶树/酒桶王/鼠群/茶芽小妖/故障机器人/答辩超人/苹果爵士/苹果分身，非 `isBeneficial()` 的效果设为 `Result.DENY`（1.20.1 该事件不能 setCanceled）。
- 鼠群主从循环：副 Boss 为原小鼠群，12 只、残血补至 16 只，整群共用 400 基础生命；小鼠承伤 ×0.6 扣入本场副鼠群血池，不转给主王。血池归零才破盾；20 秒内喂满奶酪装置造成主王最大生命比例伤害，未完成装置爆炸，之后整群满血复活、主王恢复护盾。
- 鼠群副鼠和旧一般小鼠都使用原随机减益池：每次命中一种，I～II 级，140～259 tick；不再为副王另设固定减益组合。
- 感染 `onPlayerAttackGrubCurse`：玩家有 `ftboss:infection` 且攻击对象不是自己时，先清零 `invulnerableTime` 再吃 `amplifier + 1` 点魔法伤害。

## 血量缩放与强化

- `BossScaling`（20t）：`RADIUS = 48.0`（范围内存活、非创造、非旁观玩家）；血量 `1.25^(n-1)`（`HP_PER_PLAYER = 1.25`）、攻击 `1 + 0.15*(n-1)`（`DMG_PER_PLAYER = 0.15`）；改最大生命按当前比例缩放，基准值存持久数据；自定义伤害走 `BossScaling.dmg`。
- 最终出伤修正：折耳根全部可归因伤害 ×0.5；屠夫本体、复制体与带主人标记的小怪全部可归因伤害 ×0.25。屠夫仍先吃非庄园 Boss ×2，所以相对技能字面量净倍率为 ×0.5（`BossOutgoingDamageNerfs`）。
- `BossEmpower`（20t）只强化女皇/屠夫/鼠群/酒桶王/答辩超人，**不含沙耶与故障机器人**。属性：移速 `SPEED_MUL = 0.25`、攻击 `ATTACK_MUL = 0.30`（MULTIPLY_TOTAL）、护甲 `ARMOR_ADD = 4.0`、击退抗性 `KB_ADD = 0.30`（ADDITION）；常驻药水 400t 一续、`EFFECT_TICKS = 520`：速度 I、抗性提升 I、抗火 I、力量 II；`spellBonusFor` 施法等级 +1。
- 屠夫：`BossRage.tierOf >= 1` 时额外移速 +50%、攻击 +200%，每 40t 给速度 II 100t，索敌改为 32 格内最近的非 Boss 侧活体。酒桶王：600t 一次且血量 < 50% 时，24 格内分身不足 2 个则召 1 个 1 血无 AI 分身。

## 狂暴系统

- `BossRage` 层级存持久数据键 `ftboss_rage_tier`；血量比例 ≤ 0.50 进**狂暴 I**，≤ 0.25 进**狂暴 II**，只升不降，跨区块重载不重复触发。
- 狂暴 I（20t 刷新，80 tick）：迅捷 I + 抗性提升 I；狂暴 II 额外：力量 I + 生命恢复 I。例外：故障机器人纯进攻档，只给迅捷（II 级时为 II 级）与力量（II 级时 I 级），**不给抗性、不给再生**。
- 演出：`RAVAGER_ROAR` 1.7 音量 + `WITHER_SPAWN`、红尘粒子 35 个（II 级 60）、`ANGRY_VILLAGER` 环 26 个（II 级 40）；**不再发聊天播报**；每 5t 一次狂暴尘粒子。

## 召唤上限与主世界限定

- `SummonLimit`：同种召唤物上限 `MAX_PER_TYPE = 10`，统计半径 `RADIUS = 64.0`。到上限不召唤，改为给同类全体上速度 II + 力量 I + 抗性 I，时长 `BUFF_TICKS = 400`（20 秒），并播 36 个 `HAPPY_VILLAGER` 与 `BEACON_ACTIVATE`。
- `SummonGate`：`OVERWORLD_ONLY = true`，白名单只有 `minecraft:overworld`；所有玩家可见召唤入口（含 `/ftboss challenge`）都要过 `allow(...)`，不通过则中止且**不消耗召唤物**。

## 削甲 / 禁疗 / 庄园禁用

- `ArmorShred`（功底用黑色切割者命中玩家）：每层 −5 护甲（`PER_STACK = -5.0`），最多 `MAX_STACKS = 5` 层（合计 −25），每层 `DURATION_TICKS = 200`（10 秒），再命中刷新；用固定 UUID 的 `Attributes.ARMOR` 修饰符实现；过期清理由 `ManorFight` 每秒调 `ArmorShred.tick`。
- `HealBan`：`BAN_TICKS = 600`（30 秒）。优先挂 `relics:anti_heal` 作可见标记，没装 Relics 则退回内部计时表 `UNTIL`；真正生效靠监听 `LivingHealEvent` 并 `setAmount(0)`（不用 cancel）；判定与标记同源；拦下回血每名玩家每 2 秒最多一条日志。
- HealBan 触发点：麻雀「获取玩家增益」时以 `SPARROW_NOHEAL_CHANCE = 0.10f` 施放；雪黎的飞刀命中时以署名「雪黎的飞刀」施放。
- 庄园禁术 `ManorBan.BANNED_SPELLS = black_hole, heartstop, oakskin`（只比 path），由 `ManorSpellBan` 监听 Iron's Spells 的 `SpellPreCastEvent` 取消，仅在 `ManorFight.isPlayerFighting` 为真时生效。
- 庄园禁食 `ManorBan.BANNED_FOOD_BUFFS = kaleidoscope_cookery:satiated_shield`（饱腹代偿）、`farmersdelight:nourishment`（滋养）、`farm_and_charm:satiation`（饱腹），按完整注册名比对；禁术/禁食共用 `LINES` 台词（随机二选一）与同一反馈（屏幕中上方 + 聊天栏 + `ANVIL_LAND` 0.8/0.6）。

> ⚠ **2026-09-24 更新**：`ManorBan.BANNED_SPELLS` 现为 `black_hole` + `heartstop`（**两档难度都拦**），`oakskin` 仍只在终结难度拦；禁食方面「饱腹代偿」`kaleidoscope_cookery:satiated_shield` 改成**两档难度都清**（`ManorImpossibleRules.SHARED_BANNED_FOOD_BUFFS`），「滋养」「饱腹」仍只在终结难度清。

## 麻雀支援

- 判定节奏 `CHECK_INTERVAL = 200` tick（**10 秒**；v118 由 600=30 秒缩短），每次判定概率 `CHANCE = 0.5`（v118 由 0.22 上调；期望约 20 秒进场 —— 以前基本见不到她）。
- 条件：玩家 48 格内（`FIGHT_RANGE`）有**真・彩蛋 Boss（EasterBossEntity）**且 `MIN_BOSSES = 1`，同时确实在交战（在庄园战场内，或该 Boss 正把该玩家当目标）；非隐藏 Boss 的战斗**一律不刷**。全局唯一：直接扫世界数活着的麻雀，`alive > 0` 不再刷。
- 麻雀血量 `SPARROW_HEALTH = 500.0f`，变体号 `SPARROW_VARIANT = 9`，刷在 Boss 上方 6 格、周围 6 格，名字常显「麻雀」。
- 支援行为：每 `SPARROW_HEAL_TICKS = 60`（3 秒）治疗被支援 Boss `SPARROW_HEAL_PERCENT = 0.01`（1% 最大生命）+ 3 个随机增益（15 秒）；每 `SPARROW_CLEANSE_TICKS = 100`（5 秒）净化另一位的减益；每 `SPARROW_SHARE_TICKS = 20` 双向共享正面效果（跳过伤害吸收与生命提升）；寿命 `SPARROW_LIFE_TICKS = 1800`（90 秒）后飞走。
- 分摊 `SPARROW_DAMAGE_SHARE = 0.50f`：麻雀替被支援 Boss 吃 50% 伤害（可能因此阵亡）。麻雀不攻击玩家、不抓人、不投末影珍珠；离友军超过 `SPARROW_TELEPORT_RANGE = 10.0` 格时传送过去，冷却 `SPARROW_TELEPORT_CD = 60`。麻雀在场时全体彩蛋 Boss 增益等级 +1（`buffAmp(base) = base + 1`）。

## 玩家侧通用规则

- 极限模式死亡护栏：服务器 `isHardcore()` 为真且庄园参战玩家进入真正的 `LivingDeathEvent` 时，先无奖励结束庄园战/入场演出，再取消本次死亡、保留 1 点生命并给予 60 tick（3 秒）无敌；期间服务端直接取消后续伤害，并对虚空/命令等再次死亡做兜底。原版不死图腾成功时不会触发，保护会自动过期且停服清空。普通生存模式不启用；非庄园 Boss 仍只中止战斗、不阻止玩家死亡。
- 禁止飞行 `BossHandler.FLY_BAN_ENABLED = false`（**已关闭**，整套不执行）。若改回 true：判定半径 64 格，每玩家冷却 80 tick；先掐断创造飞行与鞘翅，再落一次 `LIGHTNING_BOLT`，并按候选 id 与关键词（tendon/rupture/cripple/broken/lame/snap）找「筋箭断裂」，找不到退回虚弱 II + 缓慢 II 各 200 tick。
- `FightState`：判定半径 `ENGAGE_RANGE = 64.0`；成立条件为玩家在庄园战场内，或 64 格内有本模组 Boss 且 `Mob.getTarget() == 玩家`；`isBoss` 按 `EntityType` 命名空间判断，麻雀与柚子分身不算。
- 玩家退出：`BossBars.forgetPlayer`；若退出者是女皇/屠夫的召唤者则 `cleanup()` 让 Boss 消散；女皇另有 `DESPAWN_RANGE = 50` 格超距自动清场。死亡立即从**所有**血条摘除并结算连死怜悯；重生（`PlayerEvent.Clone`）摘除旧实例；重连（`PlayerLoggedInEvent`）调 `ManorFight.reattach` 重挂庄园进度条。
- 实体离场（`EntityLeaveLevelEvent`）：屠夫/鼠群/茶树/酒桶王/答辩超人/苹果爵士/故障机器人的血条立即 `untrack`。

## Boss 死亡掉落与连死怜悯

- 统一掉落走 `BossMercy.onBossDefeated`（由 `BossHandler.onBossDeath` 在确认真死后调用）：每个 Boss 掉自己的石像；糖霜女皇额外掉糖果拐杖，苹果爵士额外掉苹果果梗。
- 柠檬猫直接召来的食物系 Boss 带持久化无奖励标记，死亡时物品与经验全部归零；战利品表、石像、武器、遗物、法术卷轴及手写奖励都被拦截，普通方式召唤的同类 Boss 不受影响（`LemonSummonRewards`）。
- 沙耶是二命：`tryRevive()` 为真时取消死亡且**不**清零连死计数，真死才 `onBossDefeated` + `dropRewards()`；焦糖护符与屠夫回魂命中也取消死亡，同样不清零。
- `BossMercy.LIMIT = 5`：同一玩家在**同一只 Boss 实例**（记账键 `i:<实体UUID>`）下连续死亡 5 次，该只 Boss 及其召唤物直接 `discard()` 离场，**无掉落、无经验**（不走死亡流程）。
- 计数存 `SavedData`（overworld 的 `ftboss_mercy`），死亡重生/退出重进/重启都保留；`STREAK_TIMEOUT = 12000` tick（10 分钟）未再死亡即过期归零；Boss 被击杀时清零其 96 格内所有玩家的该实例计数。
- 隐藏 Boss（彩蛋十人）**不参与**连死怜悯：归因照做，但不累计、不提示、不移出场地。
- 归因顺序：① 击杀者（投射物取主人）是 Boss → ② 击杀者是召唤物则取 64 格内最近 Boss → ③ 最近 `THREAT_MEMORY = 200` tick 内打中过该玩家、仍在 48 格内的 Boss → ④ `NEAR_RADIUS = 32.0` 格内最近 Boss；全不命中不计数。

## Boss 血条与玩家列表

- `BossBars` 以 `UUID -> ServerBossEvent` 登记；`hide` 执行 `setVisible(false)` + `removeAllPlayers()`（客户端只在收到 REMOVE 包时销毁血条）；每 40t 兜底 `sweep`：登记实体不再存活或已被移除则隐藏 + REMOVE + 注销。
- 各实体自带血条可见半径均为 48.0 格（女皇/屠夫/鼠群/茶树/酒桶王各自的 `BOSS_BAR_RANGE = 48.0`）：范围内 `addPlayer`、范围外 `removePlayer`，无人时整体隐藏。`BossBars.forEach` 供「按场上人数缩放」遍历登记中的 Boss。

## Boss 音乐调度

- 总开关 `FTSounds.MUSIC_ENABLED = true`。服务端每 10 tick 按玩家、维度与战斗选择曲目，客户端只有一个播放器，同时最多一个正在加载或播放的 Boss 音乐实例。
- 首次选择 96 格内最近的请求者，仍在 128 格内的当前 Boss 优先保留，不因新 Boss 加入而抢曲。离场或空场保留 100 tick 宽限；死亡、旁观和跨维度显式停止。超过 80 tick 不再续报的 Boss 会从请求表清除。
- 沙耶、女皇、屠夫、酒桶王、鼠群、故障机器人、答辩超人、苹果爵士、边牧犬王与潮汐鱼使用对应专属曲目。答辩超人、苹果爵士与潮汐鱼的二阶段由同一调度器切曲，阶段音乐只前进不回退，实体不直接播放 BGM。
- 庄园确认等待保持安静，演出开始播放《双轨坦克曲》；正式开打切为《鏖战》→《双轨·战斗高潮》的有限播放列表，客户端确认前曲实际结束后才接后曲。两首各播一次，不回绕、不定时补播。
- 同一场战斗的同一首曲目只播一次。重复请求、同帧重复包、迟到的旧版本状态以及加载中重复请求都不会重启歌曲；普通单曲自然结束后不强制循环。新战斗可以重新播放对应曲目。
- 空场宽限结束后，非庄园 Boss 真死可以单独播放一次落幕钟声，新战斗会先停钟声再起曲；庄园成员不触发落幕钟声。标记 `ftboss_no_bgm` 的召唤物及迷你分身不参与选曲。
- 客户端换曲先停止旧实例，再等 2 tick 起播新实例；异步起播最多等待 100 tick，超时显式停止。断线、世界卸载及声音资源重载都会清理实例。播放期间压制原版背景音乐，唱片机使用独立事件，不受影响。
- `ftboss:boss_music` 专用 S2C 状态包包含递增版本、战斗身份、维度与最多 3 首曲目；状态不变不发包。未通过统一播放器的旧式 `boss_music_*` 播放事件会被真正取消，不只是不登记。

## 石像与交易

- 石像方块共 8 座：`statue_sugar_queen`（档位 1）、`statue_butcher`(2)、`statue_saya`(3)、`statue_wine_barrel`(4)、`statue_rat_swarm`(5)、`statue_defect`(6)、`statue_compost`(7)、`statue_apple`(8)；属性 `Blocks.STONE` 复制 + `strength(1.5F, 6.0F)` + `noOcclusion`，1×2 双半格高（`block/FTBlocks.java`）。通用「交易用石像」`ftboss:statue` 已整体移除。
- 外观切换：手持匹配档位的召唤物左键石像，在「上色 / 白膜」间切换（不匹配则提示「这个召唤物和这座石像不匹配」）；切换时取消事件以免敲掉石像，并播 `AMETHYST_BLOCK_CHIME`；档位→召唤物映射见 `BossHandler.tintFor`。
- `data/ftboss/loot_tables/blocks/statue_*.json`：均 `minecraft:block` 类型、1 次 roll、掉同名物品，条件 `block_state_property` 仅 `half = lower`。`trade/TradeHandler` 读 `config/ftboss_trades.json`（首次自动生成），默认两条 = 8 个 `minecraft:sugar` → 1 个 `ftboss:sugar_queen_crystal`、16 个 `minecraft:sugar` → 1 个 `minecraft:emerald`；**触发它的右键钩子已随通用石像一并移除，当前无触发点。**

