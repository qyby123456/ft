# 甜苹果庄园（隐藏 Boss 彩蛋十人）共享机制 + 0~5 号变体

11 位共用 `entity/EasterBossEntity.java`，靠 `variant` 区分。`COUNT=11`；`MANOR_VARIANTS={0,1,2,3,4,5,6,7,8,10}`（`MANOR_COUNT=10`）；`SPARROW_VARIANT=9` 是支援位、不进轮换；`XUELI_VARIANT=10`。名字取 `NAMES[]` 字面量。

## 变体总表

- 麻雀适应性护盾：按上一位 Boss 承受的物理/法术总量选择较高的一类，在满足原有同场支援、存活和范围条件时，对当前 Boss 提供该类额外 5% 减伤，不叠层；非对应类型和绕过伤害不受此减伤。

| variant | 中文名 | 主手武器 | 招牌技能一句话 |
|---|---|---|---|
| 0 | 柚子 | ftboss:statue_youzi | 满 3 次计数召 2 个迷你分身 |
| 1 | 蛋蛋 | minecraft:end_rod | 与玩家互换位置 |
| 2 | 朵拉 | 鸣脉冲锋枪 / 聚能枪 + 鸣脉长枪 | 双枪轮换：连射 / 蓄力狙击；副手突刺与装弹冲刺，连续命中获得致命节奏 |
| 3 | 功底 | ftboss:black_cleaver | 断罪斩蓄力横扫 + 破甲 + 永久血怒 |
| 4 | 柠檬猫 | farmersdelight:skillet | 猛火翻锅 + 喊 2 位食物系 Boss 助战 |
| 5 | 青衣 | tridents_n_stuff 四件 | 投戟+激流冲刺 与 恒星三叉戟雨 交替 |
| 6 | 文玄蓝 | create:flywheel | 飞轮绞杀 + 蒸汽泄压 + 光束连招 + 超能光束 |
| 7 | 折耳根 | ftboss:shadow_sword | 绕到背后一刀 |
| 8 | 紫菜 | ftboss:puffer_bat | 毒雾爆发 + 河豚减益池 |
| 9 | 麻雀 | minecraft:feather | 治疗/上增益/净化/分摊 50% 伤害，不攻击玩家 |
| 10 | 雪黎 | farmersdelight:skillet | 汤锅/平底锅/压路冲锋/禁疗飞刀/女仆护法/自爆分身 |

- `RANGED=true` 仅 2、6 号；`CASTER=true` 仅 7 号；`STYLES` 0=劈砍 1=突刺 2=横扫；`EXTRA` 全 false。

## 共享机制

### 属性与伤害系数

- `MAX_HEALTH=1000.0`、`ARMOR=12.0`、`ATTACK_DAMAGE=9.0×5.0×(4/3)=60.0`、`MOVEMENT_SPEED=0.33`、`KNOCKBACK_RESISTANCE=0.8`、`FOLLOW_RANGE=64.0`、`STEP_HEIGHT_ADDITION=2.4`、`xpReward=180`（`createAttributes`）。
- `WEAPON_DAMAGE_MULT=5.0`、`SKILL_MULT=3.0`（技能伤害与减益时长 ×3）、`SKILL_RADIUS_MULT=1.5`（只放半径）、`BOSS_DAMAGE_MULT=4.0/3.0`；后者已含「Boss→玩家全局减半 ×0.5」与「彩蛋 +1/3 ×4/3」（类注释），故 `× BOSS_DAMAGE_MULT` 即玩家实吃伤害。
- `BossScaling` 每 20 tick 按人数缩放：半径 `RADIUS=48.0`，创造/旁观不计，血量 ×`1.25^(n-1)`、攻击 ×`1+0.15×(n-1)`；只改基础值，故血怒/青衣层数用 AttributeModifier。
- 魔法减免 `MAGIC_DAMAGE_REDUCTION=0.40`；原“单次 >500 点整下无效”规则已取消。
- 装备：腾炎甲 `ARMOR_IDS`（3 号用亚当甲 `ARMOR_IDS_ADAMANTINE`），满附魔 `ARMOR_ENCH_LEVEL=8`/`WEAPON_ENCH_LEVEL=8` + Unbreakable、掉落率 0、每 200 tick 重穿。
- 近战 `getAttackReachSqr()=9.0`（3 格）；`doHurtTarget` 先清玩家 `invulnerableTime`，命中后击退 1.0/上抛 0.42 + 自身速度 III 1.5 秒追击；远程位 `KiteGoal` 进 6 格后撤到 8 格外。

### 常驻增益与 buffAmp

- `INNATE_EFFECTS` 六项每 `INNATE_BUFF_TICKS=100` tick 补一次、时长 `INNATE_BUFF_DURATION=200`、等级 `buffAmp(1)`；6 号跳过速度。
- 3 号额外挂 `armamentarium:adamantine_effect`（等级 0），缺 mod 时退回抗性 `buffAmp(2)`+吸收 `buffAmp(1)`，`KNOCKBACK_RESISTANCE` 基础值 ≥0.9。
- `buffAmp(base)=base+(48 格内有麻雀 ? 1 : 0)`。

### 连锁增益与同步玩家增益

- 连锁：`EasterBuffChain` 监听 `MobEffectEvent.Added` → 48 格内有麻雀且距上次 ≥ `CHAIN_BUFF_CD=40` tick → 自己再得 2 个随机增益（`CHAIN_BUFF_POOL` 16 项，15 秒，`buffAmp(1)`）+ 给 32 格内最近玩家 1 个随机减益（`RANDOM_DEBUFF_POOL` 9 项，10 秒，等级 1；无失明/黑暗/漂浮）；麻雀喂的增益走 `runQuietBuffs`，不触发连锁、不甩玩家减益。
- `syncPlayerBuffs` 每 `ManorBuffCopy.COOLDOWN_TICKS=300` tick（15 秒）：复制 48 格内最近一名玩家的可复制正面效果，等级照抄、时长统一 `ManorBuffCopy.DURATION_TICKS=600` tick（30 秒），不看玩家剩余时长。跳过伤害吸收、生命提升、焕新之触，以及包含最大生命属性修饰符的模组效果；开局额外生命不复制，玩家原有效果不移除。
- 仅 9 号麻雀在同步成功后按 `SPARROW_NOHEAL_CHANCE=0.10` 对该玩家 `HealBan.ban`。

### 净化 / 削甲 / 禁疗 / 血怒

- `purgeTick`：自身有减益 → `PURGE_DELAY_TICKS=1200`（**60 秒**；v118 按用户要求「玩家的净化效果改成 1min 一次」，原来是 2 秒 —— 等于任何减益在它身上都留不住）后清全部减益、**增益保留**；无减益则计时归零。
- `ArmorShred`：`MAX_STACKS=5`、每层 `PER_STACK=-5.0` 护甲、`DURATION_TICKS=10×20=200`；ADDITION 修饰符，过期由 `ManorFight.pumpAll` 每 tick 调 `ArmorShred.tick` 清。「ArmorShield」在代码里未找到（代码里未找到 ?）。
- `HealBan`：`BAN_TICKS=30×20=600`；优先挂 `relics:anti_heal`，缺 mod 只用计时表；靠拦 `LivingHealEvent` 置 0 生效。
- 血怒：`BLOOD_RAGE_MAX=5` 层，每层 `BLOOD_RAGE_PER_STACK=0.50` 攻击（满层 +250%），满层再 +`BLOOD_RAGE_MAX_SPEED_BONUS=0.40` 移速；实伤吸血 `BLOOD_RAGE_LIFESTEAL=0.15` + 真实伤害 `BLOOD_RAGE_TRUE_DAMAGE=0.10`（10%，`BossDamageTuning.trueDamage`）；`BLOOD_RAGE_TICKS=160` 用于破甲满时自身速度 II 8 秒。

### 抓回身边与砸方块

- `NO_TARGET_GRAB_TICKS=10×20=200`（10 秒；日志文案仍写「15 秒」），只认玩家目标；庄园内此条交给 `ManorFight.recallStrayPlayers`。
- `grabLaggard`：`NO_TARGET_GRAB_RANGE=256.0` 内最近玩家传送到自己身边 3 格 + 「哼，想逃？」（`NO_TARGET_TEXT`）+ 64 格内他人播报；同时 `breakAround(sl,3,3)` = 7×7、脚下不砸、往上 3 层。
- `digTick`：锁着目标时每 `BREAK_INTERVAL=3×20=60` tick 啃 `breakBlocks(sl,2,1)` = 5×5×1；只砸硬度 0~5 且无方块实体的方块。

### 免疫与无敌帧

- 流血类效果：`canBeAffected` 中 id 含 `bleed`/`hemorrhage`/`blood_loss` → false；流血/真伤类伤害：`BossDamageGate.isImmune`（`bleed`/`doom`/`true_damage` 等；`IMMUNE_NAMESPACES` 已清空）。
- 负面效果免疫名单（`BossHandler.onEffectApplicable` 的 DENY）**不含彩蛋十人**，他们靠 2 秒净化而非免疫。
- Boss 之间互相免疫：`isValidMobTarget` 排除玩家、其它 `EasterBossEntity` 与 `ftboss` 命名空间实体；技能 AOE 只遍历 `sl.players()`，故十位不索敌也不互伤。
- `BossMercy` 连死怜悯：同一 **Boss 实例**下连续死亡 `LIMIT=5` 次 → 该只离场且不给掉落与经验；彩蛋十人**不参与**（`onPlayerDeath` 里 `instanceof EasterBossEntity` 直接返回、不计数、不提示），但归因日志照旧。
- `SummonLimit`：同种召唤物在 `RADIUS=64.0` 内上限 `MAX_PER_TYPE=10`，到顶改为给场上同类全体上速度 II/力量 I/抗性 I `BUFF_TICKS=400`。本类未调用它 —— 柚子用 `MINI_CAP=6`、柠檬猫一次 2 位助战、雪黎 2~6 位女仆，各自另有上限。
- 所有技能打玩家前先 `p.invulnerableTime=0`；`manorHurt` 自身 `invulnerableTime>0` 免伤、结算后写 10；单次伤害 clamp 到最大血量 `MIN_DAMAGE_PERCENT=0.015`~`MAX_DAMAGE_PERCENT=0.10`（1000 血即 15~100），先扣吸收再扣血，扣到 0 立即 `discard()`；`BYPASSES_INVULNERABILITY` 直接秒杀。5 号在庄园主阶段承伤 ×`QINGYI_DAMAGE_TAKEN_MULT=0.70`。

### 追逃与防卡

- `huntStrayPlayer`：`HUNT_RANGE=160.0` 内找最近玩家，> `HUNT_START=20.0` 格加速追击，> `HUNT_TELEPORT=72.0` 格且冷却 `HUNT_TELEPORT_CD=120` tick 后闪现到其身边 3 格（庄园内不闪）。
- 近战位 `pearlTick`（**2026-09-24 重做**）：距目标 > `PEARL_NEAR=4.0` 格 **且连续 `PEARL_IDLE_TICKS=15×20`（15 秒）没打到玩家** → 投末影珍珠（落点 4 格内 45 点魔法）+ 瞬移到玩家身边 2.2 格；蛋蛋门槛 `DANDAN_PEARL_IDLE_TICKS=10×20`、冷却 `DANDAN_PEARL_COOLDOWN_TICKS=6×20`，其余变体冷却 `PEARL_COOLDOWN_TICKS=10×20`。连续 `FLY_BAN_TRIGGER_TICKS=120×20` 没打到玩家 → 开 `FLY_BAN_DURATION_TICKS=60×20` 的场地禁飞（96 格内玩家不能飞，只掐飞行+提示），窗口结束恢复飞行并重新倒计时。旧口径「累计 40 tick 没进 4 格」已废弃（实战几乎不触发）。
- 卡死看门狗：`isNoAi()` 连续 >200 tick 且不在演出/庄园托管 → 自动解除无敌与站定。`BossUnstuck`（80 tick 查一次、位移 <2 格、离目标 >4 格即瞬移）**未被本类调用**（调用点只有糖霜女皇/屠夫/酒桶王/鼠群）。

### 麻雀支援位（9 号）

- 出场：`SparrowSupport.tick` 每 `CHECK_INTERVAL=200` tick（**10 秒**，v118 由 600 缩短）判定，48 格内有彩蛋 Boss 且确实交战 → `CHANCE=0.5`（v118 由 0.22 上调；配合缩短的间隔, 期望约 20 秒进场）；全场只允许 1 只。
- `SPARROW_HEALTH=500.0`、`SPARROW_LIFE_TICKS=90×20=1800` 后退场；不攻击玩家、不吃金苹果。
- 每 `SPARROW_HEAL_TICKS=60` tick 给被支援 Boss 回 `SPARROW_HEAL_PERCENT=0.01` 最大生命 + `SPARROW_BUFFS=3` 个随机增益（15 秒，`buffAmp(1)`，安静模式）；每 `SPARROW_CLEANSE_TICKS=100` tick 清另一位 Boss 全部减益；每 `SPARROW_SHARE_TICKS=20` tick 双向共享正面效果（15 秒）。
- 分摊 `SPARROW_DAMAGE_SHARE=0.50`：直接扣她的血、不走她的 hurt 管线；离友军 > `SPARROW_TELEPORT_RANGE=10.0` 自我传送，冷却 `SPARROW_TELEPORT_CD=60`。

### 附魔金苹果与战场规则

- 金苹果 `APPLE_STAGES={0.75,0.50,0.25}`（麻雀豁免）：伤害吸收 IV `2400` tick + 生命恢复 II `100` tick + 六项 II 级 `APPLE_BUFF_TICKS=60×20=1200` tick。
- 召回：玩家离在场上那位 > `STRAY_FROM_BOSS=30.0` 格或该 Boss 连续 `NO_TARGET_RECALL_TICKS=200` tick 没锁定玩家 → 谁离锚点更远挪谁（同玩家 `RECALL_CD=100` tick 一次，跨维度也算）。30 格阈值由随当前 Boss 移动的红金断续光幕标记，最后 6 格及脱战倒计时会逐渐增亮；普通召回暂停、轮换或结束时隐藏。
- 禁用：庄园内禁术 `black_hole`/`heartstop`/`oakskin`，禁食 `kaleidoscope_cookery:satiated_shield`/`farmersdelight:nourishment`/`farm_and_charm:satiation`（每 10 tick 清一次）。

> ⚠ **2026-09-24 更新**：`ManorBan.BANNED_SPELLS` 现为 `black_hole` + `heartstop`（**两档难度都拦**），`oakskin` 仍只在终结难度拦；禁食方面「饱腹代偿」`kaleidoscope_cookery:satiated_shield` 改成**两档难度都清**（`ManorImpossibleRules.SHARED_BANNED_FOOD_BUFFS`），「滋养」「饱腹」仍只在终结难度清。
- v106 之后：净化只清减益；同步跳过 `ABSORPTION`/`HEALTH_BOOST`；新增 `digTick`（`BREAK_INTERVAL=60`）；`HealBan` 改在 `FTBossMod` 显式注册；紫菜减益走 `safeAddDebuff` + `ZICAI_BLOCKED` 黑名单，排除 `irons_spellbooks`/`rajswordmod` 与 `rajswordmod:holy_fire`。

## 前六位变体

### 变体 0 柚子

- 主手 `ftboss:statue_youzi`；`STYLES[0]=1`；非远程非施法 → 看家技能走通用近战横扫：`playAction(26)`、半径 5 格内 `12.0×(4/3)=16.0` 点 + `knockback(0.8,-dx,-dz)`。
- 命中（`onWeaponHit` case 0）：缓慢 II `200` tick + 虚弱 II `200` tick，并调用 `youziAddHit`。
- 招牌「自我叠影」：`YOUZI_HITS_PER_SUMMON=3` 次计数（打中玩家**与被玩家打中都算**）→ 距上次 ≥ `MINI_SUMMON_CD=40` tick 时先补 `YOUZI_BURST_DAMAGE=18.0×3×(4/3)=72.0` 点，再召 2 个分身。
- 分身：`MINI_HEALTH=150.0`、`MINI_SCALE=0.55`、`MINI_LIFE_TICKS=300`、`xpReward=0`；同款 ≥ `MINI_CAP=6` 时改为自身速度 II 10 秒；武器取 `MINI_WEAPONS` **三把**随机 —— 斩龙剑 `armamentarium:dragonslayer` / 冰斧 `icebreaker` / 荣耀金斧 `greataxe_of_glory`（满附魔 8 级 + Unbreakable）；**★ 2026-09-24 按用户要求「禁止柚子分身使用血王子焰型剑」，`armamentarium:blood_prince_flamberge` 已从表里移除**；本体死亡或 `remove()` 时 `clearMinis` 散掉 128 格内分身（`MINI_GRACE_TICKS=6000` 无引用 —— 代码里未找到 ?）。
- **分身分摊 10% 受伤**（v117 按用户要求「柚子的召唤物会分摊她 10% 的受伤」，`youziMinionShare`）：在 `manorHurt` 里接在麻雀分摊之后，把这一下伤害的 `YOUZI_MINION_SHARE=0.10` **平分给 64 格内所有活着的迷你分身**（直接扣它们的血、不走它们的受伤管线，免得再吃一次 10% 缩放）；谁血不够谁就地阵亡。一个分身都没有时不生效，**清掉分身她就少这 10% 减免**。
- 冷却 `160+rand(120)` → 160~279 tick（8~13.95 秒）。召唤物：迷你分身。

### 变体 1 蛋蛋

- 本次范围增强：末影三重镰最大半径 13.5 格，烛光坠星半径 6.75 格，虚空漩涡吸引半径 21 格；前摇、伤害与脉冲数不变，详见 `../manor-range-opening.md`。
- 主手 `minecraft:end_rod`；非远程非施法。
- 招牌「烛影置换」`skillEnderSwap`（`playCast(30,2)`）：Boss 随机传送到玩家坐标（失败硬传），玩家被丢到 Boss 原位。
- 效果：目标反胃 `5×20×3=300` tick + 饥饿 II `300` tick + `ENDER_SWAP_DAMAGE=16.0×3×(4/3)=64.0` 点；自身速度 II `360` tick + 抗性 I `180` tick。
- 命中（case 1）：反胃 `80` tick + 饥饿 II `160` tick；还会 `stripOneBuff` 抽掉玩家一个正面效果（屏幕「末影扰动」），十位里唯一（8 号复用同方法）。
- **信标压制**（v117 按用户要求「蛋蛋现在会禁用玩家的信标加成，同时自身保留加成」，`dandanBeaconBan`）：每 `DANDAN_BEACON_PERIOD=10` tick，把 `DANDAN_BEACON_RANGE=96.0` 格内玩家身上**所有 `isAmbient()` 的效果全摘掉**（v118 按用户要求「应该开局使用」：半径由 48 提到 **96**，且**正在打庄园战的玩家无视距离**，所以它一上场就全区生效）。原版信标/潮涌核心给的都是 ambient 环境效果 —— 信标补一次、它摘一次，等于"禁用"；**只动玩家**，蛋蛋自己与它同步来的增益照留。每名玩家 5 秒最多提示一次「蛋蛋压制了你的信标加成」。
- **末影封锁**（2026-09-17 按用户要求「给蛋蛋加一个禁止玩家用末影珍珠传送的效果」，`boss/ai/DandanPearlBan`）：`DandanPearlBan.RANGE=48.0` 格内没有"真在战斗"的蛋蛋（同维度、variant 1、活着、非迷你、非 NoAI —— 所以终章那九位不触发），玩家就**扔不出末影珍珠、也传不过去**。两条拦截：① `PlayerInteractEvent.RightClickItem` 取消右键（`ServerPlayerGameMode.useItem` 拿到非空结果即 return，珍珠**不被消耗**）；② `EntityTeleportEvent.EnderPearl` 取消落地传送（原版 `ThrownEnderpearl.onHit` 会整段跳过传送与摔落伤害）。提示每名玩家 2 秒最多一次。
- 冷却 `(160+rand(120))/4` → 40~69 tick（2~3.45 秒，最频繁）；`pearlTick` 阈值 13 tick。召唤物：无。

### 变体 2 朵拉

- 主手在 `ftboss:dora_nova_launcher` 鸣脉冲锋枪与 `bren:machine_gun` 聚能枪外观之间切换，副手固定 `ftboss:dora_lance` 鸣脉长枪。两把枪保留 Boss 附魔、无法破坏与零装备掉落，独立玩家战利品仍由掉落事件生成。先冲锋枪连射，再聚能枪蓄力狙击；18～36 格距离优先狙击。装弹期间不换物品，3 格内可用副手突刺；狙击蓄力与后坐期间不插入突刺。
- 「聚能狙击」：蓄力 40 tick（2 秒），前 28 tick 黄线追踪目标，最后 12 tick 红线锁定方向，随后单发释放并后坐 12 tick。射程 36 格，碰撞盒扩宽 0.12 格，遇墙或首个生物停止，不破坏方块。独立伤害表为 120 虚空 + 120 物理 + 120 魔法（基础合计 360；2026-09-17 晚按用户口径由 240/240/240 减半），不随连射、全局、难度或庄园自适应倍率变化；三段独立类型均绕过受击冷却，只计一次致命节奏命中。物理遵守护甲与盾牌，魔法绕过护甲与盾牌，虚空额外绕过抗性与保护附魔；其他模组伤害事件仍可生效。蓄力/锁定窗口不受致命节奏缩短；无效玩家、目标改变、超距、演出、死亡或场地转移取消狙击。
- 「压制射击」：基础 `BURST_SHOTS=32` 发，致命节奏每层为下一轮加 4 发，满层 56 发；基础每 1.5 tick 一发，满层平均每秒约 23.33 发。每发约 10.67 物理 + 10.67 魔法伤害，两段之间清无敌帧，但一发只计一次命中。
- 射程 `BURST_RANGE=18.0`，超距停火；水平偏差 `BURST_SPREAD_NEAR=0.42` 至 `BURST_SPREAD_FAR=0.78`。判定同时检查墙体和碰撞箱。实际伤害成功后才计命中与击退；枪击水平推力 `BURST_KNOCKBACK=0.55`，上抛 `BURST_KNOCKBACK_UP=0.10`。一轮命中至少 5 发附缓慢 III 12 秒与流血 8 秒。
- 「长枪突刺」：只攻击 3 格内、可见且存活的非创造/旁观目标玩家，每次 `SPEAR_CHARGE_DAMAGE≈13.33`，附水平推力 `SPEAR_KNOCKBACK=1.25`、上抛 `SPEAR_KNOCKBACK_UP=0.30` 和缓慢 II 5 秒；基础间隔 5 tick，满层 3 tick。击退按被击中玩家相对朵拉的水平方向归一化，并同步位移。**射击期**由 `doraCombatTick` 结算（要求副手仍是长枪、视线通畅），**装弹冲刺期**由 `reloadTick` → `spearChargeHit` 结算（2026-09-17 修「装弹时刺击没生效」：那一段原本没有任何伤害代码，两次冲刺之间用同一个 `spearHitCd` 节流，互斥不重复结算）。
- 「致命节奏」：对同一玩家连续命中 3 次触发 1 层，未触发时每两次命中间隔须少于 3 秒，射偏、打墙或伤害被拒绝会中断积累；触发后每次命中加 1 层、刷新 6 秒，最多 6 层。激活后射偏不立刻清除，但不刷新时长；改为命中另一玩家重新积累。Boss 获得 `ftboss:lethal_tempo`，血条显示层数；超时、失去有效玩家目标、进入演出、死亡、切换变体或效果被移除时清除强化。
- 每层射速倍率增加 0.125、技能/装弹冷却倍率减少 0.05。满层攻速 +75%、技能/装弹冷却 -30%、下一轮额外 24 发；技能基础冷却 80~139 tick，满层约 56~98 tick。实际起手还需等待当前连射、装弹结束，不能重入覆盖弹匣。
- 装弹冲刺基础 `RELOAD_TICKS=60` tick，满层持续保持时 42 tick；水平推进速度 1.05。技能与装弹剩余冷却均按当前层数动态推进，层数变化立即影响剩余进度。无召唤物。

### 变体 3 功底

- 范围增强：大杀四方外圈 11.25 / 内圈 4.5 格；致残打击 5.1 格、无情铁手 10.5 格、断头台 6.75 格。命中与预警共用参数，角度与前摇不变。
- 主手 `ftboss:black_cleaver`；唯一穿亚当甲 `ARMOR_IDS_ADAMANTINE`。
- 招牌「断罪斩」：`skillCleave` 蓄力 `CLEAVE_WINDUP=30` tick → `executeCleave` 半径 `CLEAVE_RADIUS=11.25` 格、`CLEAVE_DAMAGE=22.0×3×(4/3)=88.0` 点 + 上抛 0.9 + 凋零 III `360` tick + 缓慢 II `300` tick；砍中 ≥2 人 → 自身力量 II `480` tick。
- 命中（case 3）：凋零 II `160` tick + 虚弱 II `160` tick + 1 层 `ArmorShred`；破甲满 `MAX_STACKS=5`（-25 护甲）→ 自身速度 II `BLOOD_RAGE_TICKS=160` tick。
- 招牌「血怒」：每命中 +1 层（上限 `BLOOD_RAGE_MAX=5`），每层 `BLOOD_RAGE_PER_STACK=0.50` 攻击（满层 +250%，永久），满层 +`BLOOD_RAGE_MAX_SPEED_BONUS=0.40` 移速；实伤吸血 `BLOOD_RAGE_LIFESTEAL=0.15` + 真伤 `BLOOD_RAGE_TRUE_DAMAGE=0.10`。
- 常驻每 5 秒补 `armamentarium:adamantine_effect`（等级 0），`KNOCKBACK_RESISTANCE` ≥0.9。冷却 160~279 tick。召唤物：无。

### 变体 4 柠檬猫

- 主手 `farmersdelight:skillet`；非远程非施法。
- 招牌一「猛火翻锅」：`PAN_WINDUP=15` tick 起手 → 半径 `PAN_RADIUS=4.0×1.5=6.0` 格、`PAN_DAMAGE=18.0×3×(4/3)=72.0` 点 + 抛飞 `push(0,1.4,0)` + 点燃 `18` 秒 + 饥饿 II `480` tick。
- 招牌二「喊帮手」`skillSummonFoodBosses`：`FOOD_BOSS_IDS` 五选二（糖霜女皇/屠夫/茶树/酒桶王/鼠群，**不含苹果爵士**），一次抽不重复，召在自身 3.5 格外；献祭后一次只召 1 位，直接助战真死累计 3 位后永久停召（超时 / 换批 / 转场收回不计），召唤槽改为翻锅 / 借招。
- 助战强化：血量基础值 +`SUMMON_EXTRA_HEALTH=100.0` + `SUMMON_EXTRA_BUFFS=5` 个不重复增益（`buffAmp(1)`，时长 `SUMMON_LIFE_TICKS=1800`）+ 名牌「X·助战」+ `setCloneQueen`/`setCloneButcher` + `BossMusic.markNoBgm`；全部带持久化无奖励标记，死亡不产生物品或经验；到点/本体死亡/`remove()` 时 `clearSummons` 收回。献祭继承沙耶时保留其进攻与回复能力，但不继承五株守护茶芽的完全免伤，柠檬始终可受伤。
- 技能交替：场上无存活帮手或 `lemonSummonNext` → 召唤，否则翻锅。命中（case 4）：点燃 5 秒 + 缓慢 II `160` tick。
- 被动每 `FOOD_BUFF_INTERVAL=100` tick 给自己 2 个随机食物增益（10 项池，`200` tick，等级固定 1，**不经 `buffAmp`**）。全部 `heal(amount)` 来源最终只回复 2/3。冷却 160~279 tick。

### 变体 5 青衣

- 范围增强：雷潮裂阵 15 格、苍雷领域半径 8.25 格、星戟天裁十字臂长 15 格；激流碰撞半径 3.75 格，场上三叉戟脉冲选敌距离 24 格。
- 主手按 `QINGYI_WEAPONS` 四件轮换，`QINGYI_SWAP_TICKS=160`（8 秒一件，真换物品）；换装时自身 8 秒增益：0 速度 II、1 力量 II、2 抗性 II、3 生命恢复 I。
- **投出去的三叉戟现在全是恒星**（v117 按用户要求「召唤的三叉戟改成全部都是恒星三叉戟」，`qingyiStellarisStack`）：`skillQingyi` 的投戟不再跟着当前手持那件走，一律用 `QINGYI_WEAPONS[QINGYI_STELLARIS_INDEX=3]`（`tridents_n_stuff:stellaris`，取不到时退回手持那件）；星戟雨原本就固定下标 3。**手持仍四件轮换**（决定近战减益与换装增益），投戟仍**必定触发**当前那件的效果。
- 命中（case 5）按当前件给 `160` tick 减益：0 缓慢 II+虚弱 II；1 凋零 II；2 挖掘疲劳 II+反胃；3 发光+缓慢 III。
- ① `skillQingyi` 投戟（投速 3.0）必定触发当前武器效果：0 视觉落雷 + 基础 72 魔法 + 缓慢 II 15 秒；1 凋零 III 15 秒 + 基础 57.6 魔法；2 挖掘疲劳 II+反胃 15 秒+上抛 0.9；3 发光+缓慢 III 15 秒。两项魔法加层数后与普攻基准 1.5 倍取大；随后 12 tick 激流冲刺（速度 1.15、每 6 tick 瞬移到玩家身边 2.2 格），3.75 格内伤害 `max(28.8+层数, 普攻基准×1.5)`，撞人间隔 5 tick，水平推力 1.45、上抛 0.95；冲刺后 6 tick 扔随机 6 种减益药水（120 tick，等级 0）。
- ② `skillStarTridentRain`：`QINGYI_RAIN_COUNT=12` 把，每 2 tick 一把，从目标上方 12 格、半径 1.0~5.5 随机点以 1.8 速度投下（stellaris，下标 3）。
- ③ `qingyiLightningTick`：冷却 `QINGYI_SPELL_CD=100` tick，忙碌时等待，施放等级 5–7 的 `QINGYI_LIGHTNING_SPELLS` 之一；缺铁魔法时退回「真闪电 + `max(64.8+层数, 普攻基准×1.75)` 魔法 + 缓慢 III 3 秒 + 虚弱 II 3 秒」，首次冷却 80 tick。成功桥接的第三方法术保留自身伤害结算。
- ④ 层数（v95）：每投出一把（含星戟雨每把）全部伤害/护甲/血量上限各 +`QINGYI_STACK_DAMAGE`/`QINGYI_STACK_ARMOR`/`QINGYI_STACK_HEALTH`=1.0；伤害上限 `QINGYI_MAX_STACKS=200`，**★ 2026-09-24 按用户反馈「血量护甲异常上升」把护甲与血量上限各自封顶**：`QINGYI_MAX_ARMOR_STACKS=20`、`QINGYI_MAX_HEALTH_STACKS=20`（星戟雨一次 10 把，以前 3 分钟就把 200 层顶满 → 护甲 6→206、血量 1000→1200）。修饰符实现并存 NBT。
- ⑤ `onTridentHit`：原版三叉戟伤害之外追加魔法 `max(层数 + ATTACK_DAMAGE×0.8, 普攻基准×1.5)`；当前件为 0 时继续触发原有真闪电与 32 魔法；累计 5 次命中挂 4 个随机减益（15 秒，等级 1）。
- ⑥ 攻防强化：攻击总乘区 +50%、护甲 +8（基础 12→20）、庄园承伤 ×0.70、原技能基数倍率 1.80；每把三叉戟成长仍独立保留。
- ⑦ 普通技能冷却乘 `QINGYI_CD_MULT=0.45`，即 72–125 tick（3.6–6.25 秒）。青衣新增雷潮裂阵、苍雷领域、星戟天裁，与旧技能在六槽轮换中交替；三招每段分别取 `max(140, 普攻基准×1.5)`、`max(125, 普攻基准×1.75)`、`max(180, 普攻基准×2)`。范围、预警与验证见 `../QINGYI_SKILL_SCALING.md`。

## 应对要点

- 文玄蓝新生成本体开局自带 10 个环绕飞轮，正式可行动时一次性发放；保存进度防重领，上限仍为 25。旧存档已存在 Boss 不在加载中途追发，见 `../manor-range-opening.md`。
- 流血/真伤/末日类效果与伤害都被免疫，原“单次 >500 点整下无效”规则已取消；庄园主阶段单次伤害还被 clamp 到最大血量 1.5%~10%，只能靠多段中等伤害。
- 减益留不住（2 秒后被 `purgeTick` 清掉），输出要卡在 2 秒窗口内；也别给 Boss 送增益：有麻雀时每个增益会连锁出 2 个随机增益并给你挂 1 个随机减益，它还每 15 秒复制 48 格内最近玩家的可复制正面效果（统一持续 30 秒，排除生命上限类、伤害吸收与焕新之触）。
- 麻雀在场按约 55% 输出估（分摊 50% + 每 3 秒回 1% + 每 5 秒净化 + 双向共享增益）；她只有 500 血且不走自己的 hurt 管线，优先集火。
- 跑远无效：庄园内超 30 格被「谁离场地更远挪谁」拽回（同玩家 5 秒一次）；庄园外 20 格起被加速追击、72 格起被闪现，10 秒没锁定玩家就抓人并砸开 7×7×3；掩体也不可靠，锁着你时每 3 秒啃掉身边 5×5×1 层硬度 ≤5 的方块。庄园内还禁用黑洞/止心术/橡肤，饱腹代偿/滋养/饱腹每 10 tick 被清。
- 0 号：打它也会帮它计数（每 3 次召 2 个 150 血分身，上限 4）；1 号：换位 + 抽掉你一个增益，冷却仅 2~3.45 秒；2 号：32 发约 683 点但命中率随距离下降，打空后冲刺装弹 3 秒是近身窗口。
- 3 号：断罪斩 1.5 秒蓄力、88 点，见举刀出圈；血怒永久叠层、破甲满额外扣 25 护甲 10 秒，不能拖；4 号：翻锅 72 点并抛飞点燃 18 秒，无帮手时必召 2 位强化助战，半血前未完成献祭会立即召齐两位并献祭，优先清帮手；5 号：8 秒换武器决定减益，每投一把三叉戟它的伤害/护甲/血量 +1，不能打消耗战。

