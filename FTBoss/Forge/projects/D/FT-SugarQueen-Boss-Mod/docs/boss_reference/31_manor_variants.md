# 甜苹果庄园 · 后半变体逐个手册

2026-09-20 新增祁崽：变体下标11，远程星术法师，绯星祈愿杖；2026-09-21 新增地面六向追踪/锁点光束「绯星锁裁」（24 tick 追踪、12 tick 锁定警告、每 2 tick 一束、每束原始魔法伤害 72），以及范围多角度弹幕「万棱星狱」（6 秒 30 束、每 4 tick 一束、每秒至少一束锁定玩家、其余随机锁向、每束原始魔法伤害 48；使用专属星棱炮镜与三层立体光束模型）。两招中本体都不升空且保持可选中。当前三颗浮游法晶为独立常驻被动，可与任意主动技能并行；第八招「天陨星裁」（天罚）与第十招「万棱星狱」成功起手后不再被普通取消、目标丢失、通用软清理或临时 NoAI 打断。两招期间庄园普通召回暂停，不传送玩家；万棱星狱仍可正常受伤，并在失去目标后使用最近锁点完成。只有死亡、换变体、真正的庄园硬转场、实体移除及迷你 / 巨型 / 终章等硬状态边界会清理。完整外观、技能、伤害与验证说明见 `../QIZAI_BOSS.md`。当前 `COUNT = 12`，正式轮换 `MANOR_VARIANTS = {0,1,2,3,4,5,6,7,8,10,11}` 共11位，9号麻雀仍为支援位。以下旧版数值条目保留历史口径，当前共享伤害以代码和最新难度说明为准。

覆盖范围：本文件写 `NAMES[]` 中**下标 6~10** 的五位 —— **文玄蓝、折耳根、紫菜、麻雀、雪黎**；下标 0~5（柚子、蛋蛋、朵拉、功底、柠檬猫、青衣）由前半篇负责，本文件不重复。`EasterBossEntity.NAMES` 共 11 项（`COUNT = 11`），庄园轮换名单 `MANOR_VARIANTS = {0,1,2,3,4,5,6,7,8,10}`（共 `MANOR_COUNT = 10` 位），麻雀（`SPARROW_VARIANT = 9`）是支援位、不进轮换。

## 全族通用底子（写各人机制前先立基线）

- 属性：`MAX_HEALTH 1000`、`ARMOR 12`、`ATTACK_DAMAGE = 9.0 × WEAPON_DAMAGE_MULT(5.0) × BOSS_DAMAGE_MULT(4/3) = 60`、`MOVEMENT_SPEED 0.33`、`KNOCKBACK_RESISTANCE 0.8`、步高额外 `2.4`、`FOLLOW_RANGE 64`（`createAttributes`）。
- 全局倍率：`WEAPON_DAMAGE_MULT = 5.0`、`SKILL_MULT = 3.0`、`SKILL_RADIUS_MULT = 1.5`、`BOSS_DAMAGE_MULT = 4/3`。下文凡写"已含全局倍率"即已乘过 `BOSS_DAMAGE_MULT`。
- 常驻增益：每 `INNATE_BUFF_TICKS = 100` tick 补一次 `INNATE_EFFECTS` 六项（速度/抗性/再生/急迫/跳跃/力量，等级 `buffAmp(1)`），时长 `INNATE_BUFF_DURATION = 200`。文玄蓝（变体 6）**跳过速度 II**（`tick()`）。
- 自身中减益 → `PURGE_DELAY_TICKS = 60×20`（**60 秒**；v118 按用户要求「玩家的净化效果改成 1min 一次」，原来只有 2 秒）后清掉**自身全部负面效果**，增益保留（`purgeTick`）。
- 技能冷却基准 `cd = 160 + random(120)` tick；变体 6 与变体 2 减半，雪黎 ×0.6（`tick()`）。
- 近战 Boss 距玩家超过 `PEARL_NEAR = 4.0` 格**且连续 15 秒没打到玩家**（蛋蛋 10 秒；`util/ManorChaseRules.java`）→ 投 `EasterPearlEntity` 并瞬移到玩家身边 2.2 格（`pearlTick` / `pearlRush`），每次追完有 10 秒（蛋蛋 6 秒）冷却。`RANGED[]` 为 true 的（朵拉 2、文玄蓝 6）与麻雀不跑这条。**★ 2026-09-24 重做**：旧的 `PEARL_TRIGGER_TICKS = 2×20` 累计口径已废弃；同时 120 秒没打到玩家会开 60 秒场地禁飞。
- 追击时每 `BREAK_INTERVAL = 3×20` tick 啃掉身边一层 5×5 方块（`digTick`）。
- 限伤：500 点全局整下无效规则已取消（`BossDamageGate.nullified`）；流血/真实伤害类一律免疫（`BossDamageGate.isImmune`、`canBeAffected` 中 path 含 bleed/hemorrhage/blood_loss 直接 false）。
- 魔法伤害进 `hurt()` 先 ×`(1 - MAGIC_DAMAGE_REDUCTION = 0.40)`（`hurt`）。
- 庄园主阶段伤害走 `manorHurt`：单次伤害被 clamp 到最大血量的 `MIN_DAMAGE_PERCENT = 0.015` ~ `MAX_DAMAGE_PERCENT = 0.10`（即 **15 ~ 100 点**），先扣伤害吸收再扣血，无敌帧 10 tick；`BYPASSES_INVULNERABILITY`（如 `/kill`）直接秒杀。

## 文玄蓝（下标 6，`create:flywheel` + 阀门，远程 `RANGED[6] = true`）

- 庄园阶段由竹鸟先登场，参战玩家攻击竹鸟后唤出玄蓝。竹鸟自身保持 1 血，不单独死亡；攻击竹鸟的原始伤害交由玄蓝正常受伤管线处理，两者复用同一“竹鸟 · 玄蓝”血条及玄蓝生命池。
- 玄蓝登场 5 秒后竹鸟开始投炮台；多人时每位有效参战玩家周围各一座，每人独立 5 秒周期，失败仅对该玩家每秒重试；单人围绕玄蓝投一座。炮台 150 生命、50 护甲，光束及随后三枚一次性飞轮均为玄蓝对应伤害的三分之一。
- 每座炮台从生成起存续 600 tick（正常 30 秒）后自动移除；玄蓝暂停行动时仍计时，被摧毁或战斗结束时可提前清理。

- 招牌「**飞轮绞杀**」（被动，`flywheelGrind`，每 20 tick 一次）：`GRIND_RADIUS = 3.5 × 1.5 = 5.25` 格内玩家各吃 `GRIND_DAMAGE = 6 × 3 × 4/3 = 24`，再 ×`XUANLAN_DAMAGE_MULT = 3.64` → **87.36 点**，附带缓慢 II 2 秒；命中任意玩家则自身获速度 II 2 秒。
- 招牌「**蒸汽泄压**」（`skillSteamVent`）：正面锥形，射程 `VENT_RANGE = 6.5 × 1.5 = 9.75` 格，锥半角判定 `dot ≥ 0.82`（≈ ±35°）；伤害 `VENT_DAMAGE = 20 × 3 × 4/3 = 80`，×3.64 → **291.2 点**；击退 `VENT_KNOCKBACK = 0.85`（水平）+ 0.22（垂直）；缓慢 II 与虚弱 II 各 `6 × 20 × SKILL_MULT = 360` tick（18 秒）。
- 招牌「**飞轮回旋**」（`skillFlywheelVolley` + `throwFlywheel`）：一次投 `FLYWHEEL_SHOTS = 6` 个追踪飞轮，间隔 `FLYWHEEL_INTERVAL = 8` tick；飞轮本体 `DAMAGE = 10.67`（16 原始 → ×2/3 后已含全局修正），无视 50% 护甲（`ARMOR_PIERCE_RATIO = 0.5`，一半物理一半魔法），另各附 `EXTRA_MAGIC = 1.33`、`EXTRA_VOID = 1.0`、`EXTRA_FIRE = 1.0` 并点燃 2 秒；玩家血量 > `HEALTH_BONUS_THRESHOLD = 100.0` 时 ×`HEALTH_BONUS_MULT = 1.25`；只上缓慢 II 与虚弱 II 各 5 秒、**不造成击退**（`FlywheelProjectileEntity.onHitEntity`）。
- 飞轮上限 `FlywheelProjectileEntity.MAX_FLYWHEELS = 25`；达到上限后 `castSkill` 改走蒸汽泄压，飞轮攻速由 `HIT_COOLDOWN = 5` tick/次 → `HIT_COOLDOWN_MAXED = 2` tick/次（`tick()` 中 `flywheelMaxed`）。
- 每 `FLYWHEEL_VOLLEY_TICKS = 20×20` tick 让所有环绕飞轮**一起再扑一轮**（`flywheelVolley`，`launchAgain`），引导光束期间暂停。
- 招牌连招「**光束打击**」（`skillBeamCombo` / `beamStrike`）：触发距离 `BEAM_COMBO_MIN = 8.0` ~ `BEAM_COMBO_MAX = 16.0` 格。① 沿直线结算 `BEAM_STRIKE_DAMAGE = 60`（2026-09-17 按用户要求「光束打击连招伤害减半」由 120 下调），再 ×`XUANLAN_DAMAGE_MULT = 3.64` → 基础下限 **218.4 点**（旧值 436.8），判定半径 `BEAM_STRIKE_RADIUS = 1.6`（再 + 玩家半宽），击退 `BEAM_KNOCKBACK = 0.55`；② 全部飞轮齐射；③ 飞轮攻击力 `FLYWHEEL_DAMAGE_STEP = 0.10` 加法叠加，上限 `FLYWHEEL_DAMAGE_CAP = 5.0`。
- 招牌「**超能光束**」（`xuanlanBeamTick`）：冷却 `XUANLAN_BEAM_CD = 16×20`（上场后 `8×20` 放第一次），蓄能 `XUANLAN_BEAM_CHARGE = 15` tick（红色收缩环 + 预警线，方向**锁定后不再追踪**），引导 `XUANLAN_BEAM_FIRE = 40` tick，每 `XUANLAN_BEAM_PULSE = 10` tick 结算一次 `XUANLAN_BEAM_DMG = 400`（**物理/魔法/虚空/真实四系各 100 点**；2026-09-17 按用户要求「减少到 400」，此前为 504），合计 4 次脉冲 = 1600 点。长度 `XUANLAN_BEAM_LEN = 40.0`，判定半径 `XUANLAN_BEAM_RADIUS = 3.0`。放完**不进入落地衰弱**（无虚弱）。
- 数值口径：`XUANLAN_DAMAGE_MULT = 3.64` 同时乘在绞杀 / 泄压 / 光束打击上，飞轮本体则读 `flywheelDamageMul()`（初始值即 `3.64`，每次连招 +0.10）。近战 `ATTACK_DAMAGE` 另有 `XUANLAN_ATK_BONUS = 0.30`（`MULTIPLY_TOTAL`，`setVariant` 中重挂）。
- 走位：`KiteGoal` —— 玩家进 6 格内就后撤 8 格，导航速度 `0.9`（其它远程 1.35）。
- 召唤物：无。
- 「**天穹锁击**」（`XuanlanSkyStrike`）：生命值不高于 70% / 35% 时分别获得一次施放机会，同一实体最多使用两次，次数存档保留。两次之间至少间隔 200 tick；不打断已经承诺的其他技能。
- 起飞 20 tick，上升 24 格后隐藏本体、武器与飞轮。随后连续 60 次自机狙光束（原 20 次 ×3），每次锁定玩家当时脚下的位置，半径 3 格，预警 10 tick（原 20 tick 减半）。打击间隔按 8、9、9 tick 循环，平均 26÷3 tick（攻速为原来的 3 倍）；预警一旦出现就不再追踪，玩家可走出圆圈躲避。
- 相邻预警可以重叠，各自保留锁定位置和完整 10 tick 前摇；新预警和前一击不会清除下一击的圆圈。每道光束独立结算，避免原版受伤冷却吞掉提速后的连续命中。
- 光束命中使用 `manorSkillDamage(player, 230, 2.0)` 的魔法伤害，单次伤害不变，按圆柱与玩家碰撞箱判定，不破坏方块。第 60 次攻击后用 20 tick 返回安全地面，完整流程 562 tick（28.1 秒）。
- 从起飞到落地全程禁止回血，普通治疗及直接提高生命值均无效；期间暂停原技能与飞轮，无法被普通伤害命中，管理员清除仍有效。目标全部离场、冻结或转场会安全取消；存档重载会落地并保留已经消耗的次数。
- 特效：起飞/降落的三层旋转青白环、橙红范围边界、收缩倒计时环、准星、细引导线、青白光柱、柱壁流光及扩散冲击环。客户端几何特效不依赖粒子数量设置。

## 折耳根（下标 7，`ftboss:shadow_sword` + 铁魔法，`CASTER[7] = true`）

- 招牌「**暗影突袭**」（`skillShadowStrike`）：瞬移到目标**背后** 2.2 格，命中判定 `distanceToSqr ≤ 16.0`（4 格）时结算：
- 　先挂**失明 II** 与**黑暗**（两个一起上：v115 曾把黑暗换成失明，v116 按用户要求**并存**）各 `SHADOW_DARKNESS_TICKS = 5×20×3 = 300` tick（15 秒）；
- 　总伤害 `SHADOW_DAMAGE = 20 × 3 × 4/3 = 80`，整体 ×`SHADOW_DAMAGE_CUT = 0.70` → 56；
- 　其中 `SHADOW_VOID_HALF = 0.5` 那半走**虚空伤害**（`fellOutOfWorld`，自带穿甲），且按 `×1.5` 补回旧钩子折扣 → 虚空 42 / 物理 14；
- 　另附凋零 III `6×20×3 = 360` tick（18 秒）。
- 武器命中（`onWeaponHit` case 7）：凋零 II 8 秒 + **失明 II**（v115 起时长补上 ×3）+ **黑暗**（v116 起并存，`SHADOW_DARKNESS_ON_HIT = 5×20×3 = 300` tick）；音效 `SCULK_SHRIEKER_SHRIEK` + 墨影粒子。
- **虚空侵蚀 · 可叠加 debuff**（v116 加、v118 按用户要求「允许虚空持续伤害叠加，可以做成 debuff」重做）：现在是**真正的状态效果** `ftboss:shadow_erosion`（`effect/ShadowErosionEffect`，判断类 `MobEffectCategory.HARMFUL`，图标 `textures/mob_effect/shadow_erosion.png`，文案 `effect.ftboss.shadow_erosion`）。**每次打中玩家叠 1 层**（`applyShadowVoid`，上限 `MAX_STACKS = 5`、时长刷新 `TICKS = 10×20`），每秒（20t）按层数结算基础 `DAMAGE_PER_STACK = 4.0` × 层数 点**虚空伤害**，再吃折耳根最终 ×0.5（`fellOutOfWorld`：自带穿甲、不吃护甲与魔法减免）—— 实际 1 层 2/秒、满 5 层 **10/秒**。玩家的 HUD 上能直接看到层数与剩余时间；叠层/刷新走原版"同名效果取更高等级"的规则。创造/旁观者不挂。
- **连击转虚空**：每次归因于折耳根且命中玩家的伤害累计 1 次，满 `SHADOW_STREAK_HITS = 25` 次后，后续命中额外追加原伤害 50% 的 `fellOutOfWorld` 穿甲虚空伤害；30 秒未再命中玩家会断连。折耳根本体的原伤害与这段额外虚空伤害都再走最终 ×0.5，因此满连后的总输出为该次削减前原伤害的 75%。
- `SHADOW_VOID_DAMAGE = 10.0f` 是 v72 的固定虚空追加常量，当前实现改走 `SHADOW_DAMAGE_CUT` + `SHADOW_VOID_HALF` 折算路径（该常量仍保留在代码里）。
- 召唤物：无。
- 阶段变化：无。

## 紫菜（下标 8，`ftboss:puffer_bat`）

- 招牌「**毒雾爆发**」（`skillPoisonBurst`）：半径 `POISON_RADIUS = 4.5 × 1.5 = 6.75` 格，伤害 `POISON_DAMAGE = 14 × 3 × 4/3 = 56`，附**中毒 III** `10×20×3 = 600` tick（30 秒）与缓慢 II `6×20×3 = 360` tick（18 秒），并触发一次河豚减益池。
- **已移除铁魔法**：v89 起招牌技能只剩毒雾爆发（原「毒雾 / 铁魔法高级法术各 50%」已删，`skillZicaiMagic` 与常量一并移除）。
- 武器命中（`onWeaponHit` case 8）：中毒 II 8 秒 + 缓慢 II 6 秒，并调用 `applyPufferDebuffs`。
- **河豚减益池**（`zicaiPool()`）构造顺序：① 收 `armamentarium` 命名空间全部负面效果；② 收手工点名 id `ZICAI_DEBUFF_IDS`（`armamentarium:pure_venom` 纯净毒液、`minecraft:wither`、`minecraft:poison`、`tridents_n_stuff:bleeding`、`legendary_monsters:bleeding`）；③ 收其余全部通过过滤的负面效果。
- 过滤规则（`zicaiAllowed`）：必须是 `MobEffectCategory.HARMFUL`（剔掉发光/不祥之兆这类中性）、非 `isInstantenous()`、非 `isBeneficial()`；path 或显示名命中 `ZICAI_BLOCK_KEYWORDS` 的一律排除 —— `blind / darkness / levitation / gravity / minimap / world_map / worldmap / waypoint / radar / cave_maps / wm_ / 地图 / 雷达 / 路径点`。
- 命名空间黑名单 `ZICAI_BLOCK_NAMESPACES = {"irons_spellbooks", "rajswordmod"}`：前者按用户要求去掉铁魔法，后者因为 `rajswordmod:holy_fire` 在自身 `applyEffectTick` 里无条件发包、客户端无 server 会 NPE 崩客户端。精确 id 黑名单 `ZICAI_BLOCK_IDS = {"rajswordmod:holy_fire"}`。
- 每次施加抽 `ZICAI_PER_APPLY = 2` 种**不同**减益（最多重抽 128 次），时长 `ZICAI_DEBUFF_TICKS = 15×20`（15 秒）；同种再中 → 等级 +1，上限 `ZICAI_DEBUFF_MAX_AMP = 4`（即 V 级），基础等级凋零/中毒为 III、其余为 II（`zicaiAmpFor`）。
- **★ 2026-09-24 按用户要求「减少紫菜单次攻击给予的 debuff，改成一次攻击 2 个」**：这套池子**只有招牌技能「毒雾爆发」触发**了 —— 平砍（`onWeaponHit` case 8）不再调 `applyPufferDebuffs`，一次命中只给**中毒 II 8 秒 + 缓慢 II 6 秒**两条。同时**近战出手间隔 20 → `ZICAI_MELEE_INTERVAL = 30` tick**（`util/ManorBossCombatRules.meleeInterval(8)`，只作用于紫菜）。
- 崩溃双保险（`safeAddDebuff`）：施加前先 `removeEffect` 同种效果（摘掉固定 UUID 属性修饰符），整个施加包 try/catch，失败则把该效果加进 `ZICAI_BLOCKED` 本场拉黑并打日志。
- 每次成功施加还会 `stripOneBuff`（复用蛋蛋那套）：**抽掉玩家一个随机正面效果**。
- 召唤物：无。阶段变化：无。

## 麻雀（下标 9，`SPARROW_VARIANT`，支援位、不进轮换）

- 血量 `SparrowSupport.SPARROW_HEALTH = 500.0`；自己不主动攻击（`sparrowTick` 里 `setTarget(null)`，`PlayerReachMeleeGoal` 对麻雀 `canUse()` 返回 false），不参与末影珍珠追击。
- 支援节奏（`sparrowTick`）：每 `SPARROW_HEAL_TICKS = 3×20` tick 给被支援 Boss 治疗 `getMaxHealth() × SPARROW_HEAL_PERCENT(0.01)` 并上 `SPARROW_BUFFS = 3` 个随机增益（走 `runQuietBuffs`，不触发连锁增益、不给玩家挂减益，增益时长 15 秒、等级 `buffAmp(1)`）；每 `SPARROW_CLEANSE_TICKS = 5×20` tick 净化另一位 Boss 的全部负面效果；每 `SPARROW_SHARE_TICKS = 20` tick 与被支援 Boss **双向共享正面效果**（`shareBuffsWith`）。
- 分摊：`SPARROW_DAMAGE_SHARE = 0.50` —— 48 格内有她时，Boss 吃到的伤害一半直接扣她的血（不走她的 `hurt` 管线以免再吃庄园 10% 缩放），她被打死后此效果消失（`sparrowShare` / `nearbySparrow`）。
- 禁疗：`SPARROW_NOHEAL_CHANCE = 0.10` —— 每次"获取玩家增益"时 10% 概率让那名玩家 **30 秒**无法回血（`HealBan.ban`，`BAN_TICKS = 30×20`）。
- 走位：地面寻路跟随被支援 Boss（32 格内最近的其它非迷你彩蛋 Boss，`supportedBoss`）；距友军超过 `SPARROW_TELEPORT_RANGE = 10.0` 格时把自己传送过去，冷却 `SPARROW_TELEPORT_CD = 3×20`（`teleportToAlly`）。
- 寿命：`SPARROW_LIFE_TICKS = 90×20`（90 秒后自动飞走 `discard`）。
- 出场判定（`SparrowSupport.tick`）：每 `CHECK_INTERVAL = 20×10` tick（10 秒；v118 由 30 秒缩短）扫一次，玩家 `FIGHT_RANGE = 48.0` 格内必须有**彩蛋 Boss**（v106 起非隐藏 Boss 的战斗一律不刷），且玩家确实在交战（在庄园战场内或该 Boss 正索敌该玩家），再通过 `CHANCE = 0.5`（v118 由 0.22 上调）的概率判定；全场只允许一只（`countAliveSparrows` 全维度扫描，跨维度唯一）。**v118 之前 30 秒才掷一次 22%，庄园里一位 Boss 往往几十秒就被打死 → 几乎见不到她**。
- 入场演出那位默认就是麻雀（`HiddenBossIntro.DEFAULT_VARIANT = 9`），演出结束后她会留在场地当支援位并另抽首发。

## 雪黎（下标 10，`ftboss:skillet` 平底锅 + `bakery:rolling_pin` 擀面杖，`XUELI_VARIANT = 10`）

- 装备：主手 `farmersdelight:skillet`、副手 `bakery:rolling_pin`（`XUELI_MELEE_ID`），两者都满附魔 8 级 + 无法破坏（`equipWeapon`）。
- **七招轮换**（`castSkill` 的 `switch (this.xueliNext)`，每放一招 `xueliNext = (xueliNext + 1) % 7`，即 0→1→2→3→4→5→6→0）：
  1. 汤锅投掷 `skillXueli`
  2. 平底锅横扫 `skillXueliPanSlap`
  3. 擀面杖压路冲锋 `skillXueliRoll`
  4. 禁疗飞刀 `skillXueliKnives`
  5. 天降瞬砸 `skillXueliSlam`（v111 新增；场上一个玩家都没有时本轮改平底锅横扫）
  6. 女仆护法 `xueliMaids.trigger()`（不可用则本轮改掷飞刀）
  7. 自爆分身 `skillXueliClones`（`default` 分支）
- 　起始值 `xueliNext = 3`（`setVariant` 每次上场重置）→ **上场第一招固定是禁疗飞刀，第二招就是天降瞬砸**（庄园里她常两三个技能就被打死，排太后根本看不到），七招里飞刀出现两次（4 号位 + 6 号位的兜底）。
- 　天降瞬砸**悬停蓄势的 3 秒里 `castSkill` 直接 return**（不接别的招），`huntStrayPlayer` 也一并跳过 —— 否则警告打到一半她自己会先跑掉。
- ① **汤锅投掷**（`skillXueli` + `StockpotEntity`）：抛物线射出 `kaleidoscope_cookery:stockpot`（取不到退回 `youkaisfeasts:stockpot` → 原版炼药锅），寿命 `MAX_LIFE = 200` tick；落点 `SPLASH_RADIUS = 3.0` 格内玩家吃 `XUELI_POT_DAMAGE = 10 × 3 × 4/3 = 40`，**一半物理一半魔法**（各 20），并附带细雪。
- ② **平底锅横扫**（`skillXueliPanSlap`）：半径 `XUELI_PAN_RADIUS = 4.5` 格，伤害 `XUELI_PAN_DAMAGE = 30.0`（常量注释标注"数字已含全局修正"），点燃 4 秒，向外击退 0.8/0.3，附细雪。
- ③ **擀面杖压路冲锋**（`skillXueliRoll` / `xueliRollTick`）：持续 `XUELI_ROLL_TICKS = 30` tick，每 tick 以 0.95 水平速度冲向目标；3 格内玩家吃 `XUELI_ROLL_DAMAGE = 12.0`，击退、附细雪，并直接挂上**粉碎轮**；同一次冲锋的命中间隔 `XUELI_ROLL_HIT_CD = 6` tick。
- **粉碎轮**（`startCrush` / `xueliCrushTick`）：擀面杖命中后给玩家挂 `XUELI_CRUSH_TICKS = 60` tick 的持续伤害，每 tick `XUELI_CRUSH_DAMAGE = 5.0` 点（合计 300，v116 按用户要求由 3.0 上调），走 `create:crush` 伤害类型（取不到退回普通物理 `crushDamageSource`），每次都清无敌帧；重复命中原地刷新计时。
- **细雪冻结**（`applyXueliFrost`）：雪黎的攻击自带细雪，每次命中叠 `XUELI_FROST_PER_HIT = 90` 点冻结进度，上限 = `getTicksRequiredToFreeze()`（原版 140）+ `XUELI_FROST_OVER = 60`；叠满转原版冻结伤害；另附缓慢 I 60 tick（3 秒）。触发点：武器近战、禁疗飞刀、汤锅溅射、平底锅横扫、压路冲锋、分身自爆、天降瞬砸砸落。
- ④ **禁疗飞刀**（`skillXueliKnives` + `XueliKnifeEntity`）：一次掷 `XUELI_KNIFE_COUNT = 3` 把，散射 ±0.09 弧度，初速 2.4；命中伤害 `KNIFE_DAMAGE = 12.0`（v116 按用户要求「每次伤害 12 点」，原来是 6 × 4/3 = 8；擦身命中 `NEAR_RADIUS = 1.6` 格内吃 60% = 7.2），命中即 `HealBan.ban(sp, "雪黎的飞刀")` → **禁疗 30 秒**，并追加**流血**（v116：`legendary_monsters:bleeding` → 回退 `tridents_n_stuff:bleeding`，另叠 `armamentarium:blood_loss`，各 `BLEED_TICKS = 8×20` tick、等级 II，缺 mod 自动跳过）；飞刀寿命 `MAX_LIFE = 120` tick，附细雪。
- ⑤ **女仆护法**（`boss/ai/XueliMaids`，照沙耶茶芽庇佑那套）：召 `BATCH_MIN = 1` ~ `BATCH_MAX = 3` 批次、每批 `PER_BATCH = 2` 位 → 合计 `MIN_TOTAL = 2` ~ `MAX_TOTAL = 6` 位 `touhou_little_maid:maid`，带计分板标签 `GUARD_TAG = "ftboss_xueli_guard"`（只认带标签的，玩家自己的女仆不算）；**只要还有一位活着，雪黎完全免伤**（在 `hurt()` 里拦，`BYPASSES_INVULNERABILITY` 的伤害不受此阻挡）；存活期间按"每位女仆每秒 `HEAL_PERCENT_PER_MAID = 0.001` × 最大生命"缓慢回血（每 20 tick 结算一次）；全部倒下 → 护盾破除。两次召唤间隔 `TRIGGER_CD = 40×20`，同一时间只留一批（`trigger()` 先 `clear()`）。没装车万女仆时本次跳过并记冷却。
- **女仆护法 v118 修复「女仆没死护盾就失效了」**：`aliveMaids()` 带 10 tick 缓存，而 `trigger()` 以前**没让缓存失效** → 下一 tick 读到的还是召唤前那份空列表 → 立刻判"尽数倒下"。现在：① 召唤/清理后**立刻让缓存失效**；② 召唤后给 `graceTicks = 20`（1 秒）宽限，这一秒内扫不到也不判全灭；③ 判定半径 `SCAN` 由 64 提到 **128** —— 女仆会追着玩家跑，64 格立方很容易被跑出范围而被误判成全灭。
- ⑥ **自爆分身**（`skillXueliClones` / `bombFuseTick` / `detonate`）：召 `XUELI_CLONE_COUNT = 2` 个小号雪黎（走 mini 体系，`MINI_SCALE = 0.55`，血量 `XUELI_CLONE_HEALTH = 80.0`，隐藏血条、会追人但**不再放技能**）；引信 `XUELI_CLONE_FUSE = 5×20` tick 到点自爆 —— 半径 `XUELI_BOMB_RADIUS = 4.5` 格、基准伤害 `XUELI_BOMB_DAMAGE = 26.0`，按距离线性衰减到 25%，再 ×`BOSS_DAMAGE_MULT`（即 4.5 格处约 8.7 点、脚下约 34.7 点），附带细雪与击退 1.1；被提前打死走弱化版：半径 ×0.75 = 3.375 格、基准 `XUELI_BOMB_DAMAGE_ON_DEATH = 14.0`（`die()` 在 `super.die` 之前引爆）。
- **自爆分身 v118 独立冷却（修「雪黎不会召唤小雪黎」）**：这一招原本排在**轮换最后一格**（`default` 分支），而雪黎常常两三个技能就被打死 → 永远轮不到。现在除轮换外再加一条兜底：`XUELI_CLONE_CD = 12×20`（12 秒）独立冷却，**场上没有活着的分身**且冷却到点就召一批（`xueliCloneTick` / `aliveXueliClones`，开局先等 6 秒）。
- ⑦ **天降瞬砸**（`skillXueliSlam` / `xueliSlamTick` / `xueliSlamDown`，v111 新按用户要求"瞬移到玩家上空，3 秒后下砸造成眩晕和 40 点伤害"）：**落点在她起手瞬间定死**（目标不是玩家就取最近玩家）—— 要躲就得在 1 秒内跑出雪圈，不是跟踪型 AOE。瞬移到落点正上方 `XUELI_SLAM_HEIGHT = 12.0` 格并 `setNoGravity(true)`，悬停 `XUELI_SLAM_WARN_TICKS = 20` tick（每发 1 秒）：每 tick 被"钉"在悬停点（`getNavigation().stop()` + 清速度 + 每 tick 拉回坐标），落点画**随进度旋转的雪花警戒环**（半径 `XUELI_SLAM_RADIUS = 8.0` 格）+ 中心 `END_ROD` + 她身上寒气/脚下云柱（粒子特效提醒），音调递增的提示音 + 全场 action bar 播报；到点砸落：`EXPLOSION_EMITTER` + 雪花 60/云 28/暴击 22 + 爆炸与铁砧音，半径内玩家吃 `XUELI_SLAM_DAMAGE = 40.0` 伤害 + 细雪 + **眩晕** `XUELI_SLAM_STUN_TICKS = 20` tick（1 秒 = 两发间隔），并向外不击退（只用眩晕定身）。一次施放 = **5 连砸**（`XUELI_SLAM_COMBO = 5`）：每发蓄势 1 秒 → 砸落 → 落地停 `XUELI_SLAM_GAP_TICKS = 20` tick（1 秒）→ 再窜上去砸下一发；整段连砸期间不接别的招；进坑前 2 拍收掉混响送、0.1 拍把长音通道表情归零（掉坑那套手法）。
- **眩晕**（`stunPlayer` / `xueliStunTick`）：原版没有"眩晕"效果可用，本项目用组合拳 —— 每 tick 横向速度清零（`setDeltaMovement(0, Math.min(0, vy), 0)`：**保留重力/下落，只压掉跳跃**）+ `hurtMarked = true` 让客户端接受位移（否则橡皮筋）+ 停疾跑 + `stopUsingItem()`（打断吃东西/拉弓），并刷新 `MOVEMENT_SLOWDOWN VI` 15 tick、头顶画 3 颗绕头旋转的 `CRIT` 金星。状态记在雪黎实例的 `Map<UUID,Integer>` 里、由她 `tick()` 结算 —— **她被打死/离场，眩晕立刻结束**，不会留下"动不了"的僵尸状态。
- 技能节奏：`cd = (160 + random(120)) × 0.6` → 约 96~168 tick（4.8~8.4 秒）一招。
- 清理：雪黎死亡或被移除时 `xueliMaids.reset()` 收掉女仆、`clearMinis` 收掉自爆分身（`die` / `remove`）；`reset()` 顺手清掉 ⑦ 的悬停状态与眩晕表（若在悬停中被重置还会 `setNoGravity(false)`，免得她永久停在半空）。
- 召唤物：小号雪黎（自爆分身）、车万女仆（护法）。
- 阶段变化：无独立血量阶段；只有"七招轮换 + 护法开启/破除"两条状态线。
