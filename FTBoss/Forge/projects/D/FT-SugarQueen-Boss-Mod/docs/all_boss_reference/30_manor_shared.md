# 隐藏 Boss（甜苹果庄园）共享机制

- 麻雀适应性护盾：按上一位 Boss 承受的物理/法术总量选择较高的一类，在满足原有同场支援、存活和范围条件时，对当前 Boss 提供该类额外 5% 减伤，不叠层；非对应类型和绕过伤害不受此减伤。

2026-09-20 追击更新：基础移速保持普通玩家，允许速度增益加速；普通近战追击珍珠等待为 20 tick、蛋蛋为 10 tick，技能前摇期间累计但不强行打断动作。部分远程法术可交给同场空闲麻雀，缺席或忙碌时由原 Boss 施放。最新规则和代施法术表见 `../MANOR_CHASE_SPARROW_20260920.md`，覆盖本文旧珍珠频率描述。

**★ 2026-09-24 珍珠追击重做（覆盖上面那句"20 tick / 10 tick"）**：判据从"离目标 >4 格累计 N tick"改成**按"多久没打到玩家"计时**（`LivingHurtEvent` 里伤害能回溯到本 Boss 才算打到）——
连续 `PEARL_IDLE_TICKS` = **15 秒**没打到玩家才投珍珠（蛋蛋 `DANDAN_PEARL_IDLE_TICKS` = 10 秒），每次追完冷却 `PEARL_COOLDOWN_TICKS` = 10 秒（蛋蛋 6 秒）；连续 `FLY_BAN_TRIGGER_TICKS` = **120 秒**没打到玩家则开 `FLY_BAN_DURATION_TICKS` = **60 秒**的**场地禁飞**（战场 96 格内玩家不能飞/不能鞘翅，只掐飞行 + 提示，不劈雷不断筋），窗口结束恢复飞行并**重新开始** 120 秒倒计时。常量在 `util/ManorChaseRules.java`，窗口在 `boss/ai/ManorFlyBan.java`，计时器在 `boss/ai/ManorChaseTracker.java`。

**★ 2026-09-24 击退再降一档**：`BossKnockback.BOSS_RESISTANCE` 0.5 → **0.85**（Boss 只吃 15% 击退）、`TO_PLAYER_MULT` 0.25 → **0.10**、`VERTICAL_MULT` 0.5 → **0.25**。

**★ 2026-09-24 爱意滑翔**：Boss 身上有盛节精酿的 `brewery:haley`（爱意滑翔）时**允许飞行**（解除重力 + `FlyingMoveControl` 三维追击），效果结束立刻落回地面；`PursueGoal` 在飞行期间让位。

庄园十一人共用**同一个实体类型** `ftboss:easter_boss`（`entity/FTEntities.java` 第 154-155 行），靠 `variant` 字段区分（`entity/EasterBossEntity.java` 第 92-111 行）。本文只写十一人**共用**的属性、法术、减益、支援与表现规则；各角色专属技能见下一节。全文时间单位 tick（1 秒 = 20 tick），距离单位「格」。

召回边界表现：以当前上场 Boss 为圆心、`STRAY_FROM_BOSS` = 76.0 格为半径画随动红金断续光幕（`util/ManorRecallBoundaryGeometry.java` 第 5 行，含内向箭头与本地粉尘）；玩家进入最后 6 格（`urgency`，第 21-25 行）或 Boss 接近 10 秒未索敌时逐渐增亮。玄蓝高空施法、祁崽不可打断施法期间光幕隐藏，Boss 轮换或战斗结束时一并隐藏（`boss/ManorFight.java` 第 1000-1019 行）。越界判定阈值同为 76.0 格：超过 `RECALL_WARNING_DISTANCE` = 68.0 格先收到动作栏警告，越界（或 10 秒未索敌）后进入 `RECALL_COUNTDOWN_TICKS` = 100 tick（5 秒）倒计时，倒计时结束才传送（`util/ManorTeleportRules.java` 第 6-10 行、`boss/ManorFight.java` 第 1088-1141 行）。

## 一、十一人名单与实体对照

- 实体 id 一律是 `ftboss:easter_boss`；普通判定箱 0.6 格宽 × 1.8 格高（`entity/FTEntities.java` 第 154-155 行）。
- 轮换名单 `MANOR_VARIANTS` = {0, 1, 2, 3, 4, 5, 6, 7, 8, 10, 11}，人数 `MANOR_COUNT` = 11（= 名单长度，`entity/EasterBossEntity.java:108-110`）；`COUNT` = 12（第 99 行）。
- `SPARROW_VARIANT` = 9 是场外支援位、**不进轮换**（第 114 行）；`XUELI_VARIANT` = 10（第 116 行）；`QIZAI_VARIANT` = 11 祁崽（第 100 行，2026-09-20 加入轮换，专属机制见 `33_qizai.md`）。
- 名字取 `NAMES`（第 224-226 行）、贴图取 `SKINS`（第 228-231 行）、主手取 `WEAPONS`（第 238-250 行）、近战风格取 `STYLES`（第 272-274 行：0 = 劈砍、1 = 突刺、2 = 横扫）。

| `variant` | 角色 | 皮肤（`SKINS`） | 主手武器（`WEAPONS`） | 近战风格（`STYLES`） | 定位 |
|---|---|---|---|---|---|
| 0 | 柚子 | youzi | `ftboss:statue_youzi` | 1 突刺 | 近战，带庄园五技能 |
| 1 | 蛋蛋 | dandan | `minecraft:end_rod` | 0 劈砍 | 近战，带庄园五技能 |
| 2 | 朵拉 | duola | `bren:machine_gun` | 1 突刺 | 远程（`RANGED` = true） |
| 3 | 功底 | gongdi | `ftboss:black_cleaver` | 0 劈砍 | 近战 |
| 4 | 柠檬猫 | ningmengmao | `farmersdelight:skillet` | 2 横扫 | 近战，带庄园五技能 |
| 5 | 青衣 | qingyi | `ftboss:qingyi_confluence_trident` | 2 横扫 | 近战，唯一允许并行近战 |
| 6 | 文玄蓝 | wenxuanlan | `create:flywheel` | 2 横扫 | 远程（`RANGED` = true），带庄园五技能 |
| 7 | 折耳根 | zheergeng | `ftboss:shadow_sword` | 2 横扫 | 近战 + 施法型（`CASTER` = true），带庄园五技能 |
| 8 | 紫菜 | zicai | `ftboss:puffer_bat` | 1 突刺 | 近战 |
| 9 | 麻雀 | maque | `ftboss:maque_blessing_feather` | 2 横扫 | 场外支援位，不攻击玩家、血条常隐 |
| 10 | 雪黎 | xueli | `farmersdelight:skillet` | 2 横扫 | 近战，副手 `bakery:rolling_pin`，带女仆护法 |
| 11 | 祁崽 | qizai | `ftboss:qizai_prayer_staff` | 1 突刺 | 远程（`RANGED` = true）+ 施法型（`CASTER` = true），星术法师，详见 `33_qizai.md` |

## 二、共享属性、判定箱与攻击力修正

| 项目 | 数值 | 出处 |
|---|---|---|
| 基础生命 `BASE_HEALTH` | 1500.0（11 位轮换 Boss 通用；支援身份的麻雀改用 `SUPPORT_HEALTH` = 500.0） | `util/ManorSupportRules.java:7-8`、`entity/EasterBossEntity.java:1033`、`boss/ai/ManorHealthBalance.java:55` |
| `ARMOR` | 6.0（v176 起由 12.0 减半） | `entity/EasterBossEntity.java:1036` |
| `ATTACK_DAMAGE` | 60.0（基础值，人数缩放与属性修饰符会再乘上去） | `entity/EasterBossEntity.java:1038` |
| `MOVEMENT_SPEED` | 普通轮换 Boss 基础 0.13（`BossWalkingSpeed.BASE_SPEED` = 0.1 × `ManorBaseMovementSpeed.MULTIPLIER` = 1.30）；终结难度 0.429（`LEGACY_SPEED` = 0.33 同样 ×1.30）；迷你分身 0.165 / 0.33（不再乘 1.30；柚子迷你再 ×`YouziMiniRules.SPEED_MULTIPLIER`） | `entity/EasterBossEntity.java:1043-1044`、`:1172-1178`、`:223`、`boss/ManorDifficulty.java:13`、`util/ManorBaseMovementSpeed.java:4`、`util/YouziMiniRules.java:13-14` |
| `KNOCKBACK_RESISTANCE` | 0.85（`BossKnockback.BOSS_RESISTANCE`，只吃 15% 击退；功底穿亚当甲时另被抬到 0.9） | `entity/EasterBossEntity.java:1045`、`util/BossKnockback.java:31`、`:2739` |
| `STEP_HEIGHT_ADDITION` | 2.4（叠加原版 0.6 后合计 3.0 格） | `entity/EasterBossEntity.java:1048` |
| `FOLLOW_RANGE` | 64.0 | `entity/EasterBossEntity.java:1050` |
| `xpReward` | 180 | `entity/EasterBossEntity.java:1013` |
| 普通判定箱 | 0.6 格宽 × 1.8 格高 | `entity/FTEntities.java` 第 154-155 行 |
| 近战攻击距离 | 3.0 格（`getAttackReachSqr` = 9.0）；青衣允许并行近战时 3.5 格（12.25） | 第 6579-6580 行 |
| 迷你分身 | 血量 `MINI_HEALTH` = 150.0、渲染缩放 `MINI_SCALE` = 0.55、寿命 `MINI_LIFE_TICKS` = 300 tick，判定箱不变 | `entity/EasterBossEntity.java:314`、`:317`、`:320` |
| 攻击力修正（十一人共通） | 除功底（3 号）外全部 +25%（`MANOR_ATK_BONUS` = 0.25，`MULTIPLY_TOTAL`） | 第 589 行、第 1006-1015 行 |
| 攻击力修正（庄园五技能位） | 0 / 1 / 4 / 6 / 7 号再 +40%（`ManorFiveCombatRules.ATTACK_BONUS` = 0.40），每 20 tick 复查一次 | `util/ManorFiveCombatRules.java` 第 4 行、`boss/ai/ManorFiveSkills.java` 第 72-81 行、第 116 行 |
| 常驻增益 | `INNATE_EFFECTS` 六项（速度 / 抗性提升 / 生命恢复 / 急迫 / 跳跃提升 / 力量）每 `INNATE_BUFF_TICKS` = 100 tick 补一次，时长 `INNATE_BUFF_DURATION` = 200 tick，等级 `buffAmp(1)`（II 级）；6 号文玄蓝跳过速度 | 第 155-165 行、第 2712-2720 行 |
| 麻雀在场加成 | 48 格内有麻雀时 `buffAmp(base)` = base + 1（常驻增益因此变 III 级） | 第 1734-1736 行 |

## 三、全局伤害结算

| 环节 | 规则与数值 | 出处 |
|---|---|---|
| 全局倍率 | `WEAPON_DAMAGE_MULT` = 5.0、`SKILL_MULT` = 3.0、`SKILL_RADIUS_MULT` = 1.5、`BOSS_DAMAGE_MULT` = 4.0 / 3.0；后者已含「Boss→玩家 ×0.5」与「彩蛋 +4/3」两道修正，凡 `× BOSS_DAMAGE_MULT` 出来的数字就是玩家实吃伤害。`WEAPON_DAMAGE_MULT` 与 `SKILL_RADIUS_MULT` 只作常量保留，**没有调用点**（近战威力由 `ATTACK_DAMAGE` 常数 60 直接给出，技能半径由各技能表原值给出），实际参与运算的只有 `SKILL_MULT` 与 `BOSS_DAMAGE_MULT` | `entity/EasterBossEntity.java` 第 114-126 行、第 5536 行、第 5543 行 |
| 人数缩放 | `BossScaling`：48 格内存活玩家 n（创造 / 旁观不计），最大生命 ×`1.25^(n-1)`、攻击力 ×`1 + 0.15 × (n-1)`；每 20 tick 重算，只改基础值并按当前血量比例缩放 | `boss/ai/BossScaling.java` 第 22-74 行 |
| 单次伤害上下限 | 庄园主阶段自算伤害（`manorHurt`）：单次 clamp 到最大生命的 `MIN_DAMAGE_PERCENT` = 1.5% ~ `MAX_DAMAGE_PERCENT` = 10%（1500 血即 22.5 ~ 150 点；终结难度只卡上限）；先扣伤害吸收再扣血 | `entity/EasterBossEntity.java` 第 1570-1572 行、第 2068-2069 行、第 2582-2645 行 |
| 附魔金苹果防御阶段 | 血量比每跌破一档（`APPLE_STAGES` = {0.75, 0.50, 0.25}）吃一次附魔金苹果：伤害吸收 IV 2400 tick + 生命恢复 II 100 tick + 常驻六项增益各 II 级 `APPLE_BUFF_TICKS` = 1200 tick；迷你分身与麻雀豁免；上一行「先扣伤害吸收」扣的就是这里给的吸收 | `entity/EasterBossEntity.java` 第 1321-1371 行、第 2217 行 |
| 无敌帧 | 自身 `invulnerableTime` > 0 时直接免伤；每次结算后写 `invulnerableTime` = `hurtTime` = `hurtDuration` = 10 tick | 第 2194 行、第 2213-2215 行 |
| 物理限伤 | 轮换 Boss 任意连续 20 tick 内物理最终扣血合计 ≤ 200 点（每只 Boss 独立额度、多人共用）；护甲与吸收先结算，只限真正扣血的部分，详见「十、物理限伤与半血护甲」 | `boss/ai/ManorPhysicalDefense.java` 第 47-60 行、`entity/EasterBossEntity.java` 第 2627 行 |
| 技能伤害公式 | `manorSkillDamage` = max(基础常量, (攻击力 + 附魔加成) × max(1.50, 倍率))；`STANDARD_MULTIPLIER` = 1.50、`HEAVY_MULTIPLIER` = 2.00 | `util/ManorSkillDamageRules.java` 第 4-13 行、`entity/EasterBossEntity.java` 第 1501-1505 行 |
| 自适应增伤 | 对 0 / 1 / 4 / 6 / 7 号非迷你分身：玩家承伤 ×(1 + bonus)，bonus = clamp(0.10 + 血量进度 × 0.20 + 增益进度 × 0.20, 0.10, 0.50)；血量进度 = clamp((玩家最大生命 − 20) / (100 − 20))，增益进度 = min(10, 正面效果数) / 10 | `boss/ai/ManorAdaptiveDamage.java` 第 36-51 行、`util/ManorFiveCombatRules.java` 第 8-14 行、第 91-100 行 |
| 魔法伤害减免 | 十一人自身承受的魔法伤害 ×(1 − `MAGIC_DAMAGE_REDUCTION` = 0.40) = ×0.60 | `entity/EasterBossEntity.java` 第 2050 行、第 2135-2137 行 |
| 青衣承伤修正 | 5 号青衣（非迷你分身）在 `manorHurt` 里再 ×`QINGYI_DAMAGE_TAKEN_MULT` = 0.70（庄园主阶段绕过原版护甲，减伤只能在这一层做） | `entity/EasterBossEntity.java` 第 502-504 行、第 2203-2205 行 |
| 玩家无敌帧清除 | 庄园归属的攻击（沿 owner 向上追溯 8 层）命中玩家前，把玩家 `invulnerableTime` 归零 | `boss/ai/ManorAttackFrames.java` 第 19-48 行 |
| 无效伤害来源 | 虚空伤害无效；非真实玩家（假人 / 机械手）无效；`GENERIC_KILL` 等无视无敌的伤害跳过全部上限直接秒杀并立即移除 | `util/ManorCombatRules.java` 第 8-10 行、`entity/EasterBossEntity.java` 第 2110-2117 行、第 2183-2190 行 |
| 荆棘反伤 | 一律无效（伤害 id 含 thorns 即拒伤） | 第 2123 行、第 2171-2175 行 |
| 伤害分摊 | 48 格内有存活麻雀时替被支援 Boss 吃 `SPARROW_DAMAGE_SHARE` = 50%；柚子本体被 64 格内其迷你分身分摊 `YOUZI_MINION_SHARE` = 10%（平分给所有存活分身） | 第 2335 行、第 2439-2449 行、第 2460-2531 行 |

## 四、庄园五技能（六人共用的特效技能表）

- 表体是 `ManorFiveCombatRules.Skill`（`util/ManorFiveCombatRules.java` 第 24-75 行）。可用变体：`supports` = 0 / 1 / 4 / 6 / 7（第 79-81 行），`hasExpandedSkills` 再加 5 号青衣（第 83-85 行）。
- 前摇 = `HiddenBossCastRules.quicker(原始值)` = ceil(原始值 × 35%)，下限 `MIN_WINDUP_TICKS` = 3 tick（`util/HiddenBossCastRules.java` 第 4-14 行）；段数、段间隔与伤害不随前摇缩短而变。
- 总时长 = 前摇 + (段数 − 1) × 段间隔 + 1（`Skill.duration()`，第 64-66 行）。
- 伤害一律走 `manorSkillDamage`（见上一节），表里的「基础伤害」是**下限**；「普攻基准倍率」是 `Skill.meleeMultiplier()`（第 68-74 行）。
- 轮换长度 `rotationLength`：5 号青衣 = 6，其余 = 4（第 87-89 行）；`select` 只在偶数槽出招，奇数槽返回 null（即「一招一停」）；柠檬猫（4 号）整体错开一格，先出烈火颠勺再出酸雾爆锅（第 102-121 行）。青衣三招顺序为 rotation 0 / 2 / 4 = 雷潮裂阵 / 苍雷领域 / 星戟天裁。

| 技能 | 归属 | 形状 | 半径（格） | 前摇（tick） | 段数 × 间隔 = 总时长 | 基础伤害（击退 / 上抛） | 普攻基准倍率 | 附带减益 |
|---|---|---|---|---|---|---|---|---|
| `YOUZI_SEAL` 庄园震印 | 0 柚子 | CIRCLE | 5.5 | 8 | 1 段 = 9 | 100.0（1.65 / 0.55） | 2.00 | 缓慢 II 70 + 发光 I 100 |
| `YOUZI_ECHO` 叠影合击 | 0 柚子 | ECHO | 2.8 | 7 | 3 × 12 = 32 | 38.0（1.25 / 0.35） | 1.50 | 缓慢 II 70 + 发光 I 100 |
| `DANDAN_REAP` 末影六重镰 | 1 蛋蛋 | WAVE | 13.5 | 7 | 6 × 10 = 58 | 44.0（1.40 / 0.42） | 1.50 | 虚弱 II 80 + 末段缓慢 II 50 |
| `DANDAN_STAR` 烛光坠星 | 1 蛋蛋 | CIRCLE | 6.75 | 8 | 3 × 12 = 33 | 42.0（1.65 / 0.62） | 1.75 | 虚弱 II 80 + 末段缓慢 II 50 |
| `LEMON_ACID` 酸雾爆锅 | 4 柠檬猫 | CIRCLE | 5.0 | 7 | 4 × 16 = 56 | 24.0（0.55 / 0.18） | 1.50 | 中毒 I 80 + 虚弱 II 80 + 饥饿 II 100 |
| `LEMON_FLAMBE` 烈火颠勺 | 4 柠檬猫 | CONE | 9.0 | 7 | 2 × 14 = 22 | 54.0（1.60 / 0.95） | 1.75 | 点燃 80 + 饥饿 II 100 |
| `XUANLAN_RAIL` 磁轨连射 | 6 文玄蓝 | BEAM | 18.0 | 9 | 3 × 12 = 34 | 96.0（1.20 / 0.28） | 1.50 | 缓慢 II 50 + 挖掘疲劳 I 80 |
| `XUANLAN_RING` 超压震环 | 6 文玄蓝 | WAVE | 10.0 | 7 | 3 × 10 = 28 | 92.0（1.80 / 0.48） | 1.50 | 缓慢 II 50 + 挖掘疲劳 I 80 |
| `SHADOW_CROSS` 影刃十字 | 7 折耳根 | CROSS | 9.0 | 7 | 2 × 14 = 22 | 56.0（1.40 / 0.30） | 1.50 | 凋零 I 80 + 缓慢 II 60 |
| `SHADOW_CAGE` 噬影牢笼 | 7 折耳根 | CIRCLE | 6.0 | 8 | 3 × 14 = 37 | 32.0（1.55 / 0.40） | 1.50 | 凋零 I 80 + 缓慢 II 60 |
| `QINGYI_TIDE` 雷潮裂阵 | 5 青衣 | CONE | 15.0 | 7 | 2 × 14 = 22 | 140.0（1.85 / 0.65） | 1.50 | 缓慢 II 80 + 虚弱 II 80 + 发光 I 100 |
| `QINGYI_THUNDER` 苍雷领域 | 5 青衣 | CIRCLE | 8.25 | 9 | 3 × 16 = 42 | 125.0（1.35 / 0.42） | 1.75 | 缓慢 II 80 + 虚弱 II 80 + 发光 I 100 |
| `QINGYI_STAR` 星戟天裁 | 5 青衣 | CROSS | 15.0 | 10 | 2 × 16 = 27 | 180.0（1.90 / 0.75） | 2.00 | 缓慢 II 80 + 虚弱 II 80 + 发光 I 100 |

- 减益等级写法：代码里 amplifier 0 = I 级、amplifier 1 = II 级（`boss/ai/ManorFiveSkills.java` 第 202-233 行）。
- 触发条件：目标必须是真实玩家（非创造 / 旁观）、有视线、距离 ≤ `MAX_CAST_DISTANCE` = 24 格，且非迷你 / 非 `isNoAi`（第 83-88 行）。
- 命中判定：高度 |Δy| ≤ 3.0 格；CIRCLE / ECHO 用半径 + 目标半宽；WAVE 取环带 ±(1.15 + 半宽)（半径按段数递增，`waveRadius`；`XUANLAN_RING` 另有 +1.0 格偏移）；CROSS 十字条宽 ±1.15；BEAM 半宽 `beamHalfWidth` = 2.20（文玄蓝）/ 1.20（其余）；CONE 为 50° 半角锥（`util/ManorFiveCombatRules.java` 第 130-153 行）。ECHO 三段的圆心各自绕原点 3 格、彼此相隔 120°（`boss/ai/ManorFiveSkills.java` 第 274-278 行）；每段命中还要过 `clearLine` 视线判定（同文件第 150 行、第 339-342 行）。
- 击退：末段 = 技能 `knockback`，中间段 = `knockback × 0.55`；噬影牢笼的非末段方向反向（吸附）。上抛 = max(当前竖直速度, `lift` × (1 − 击退抗性))（`boss/ai/ManorFiveSkills.java` 第 195-201 行）。
- 中途取消：变成迷你、`isNoAi`、死亡、变体不符、目标失效、目标距离 > 40 格、或本人成为终章成员（第 119-126 行）；其中「成为终章成员」当前不可能发生（没有终章/巨人阶段）。
- 施法期间若 `allowsParallelMelee()` 为假（即非青衣）则停导航并清掉水平速度（第 127-130 行）；预警地面特效每 4 tick 重画一次（第 132 行）。
- 五技能位的普攻也在同一套加成里：普通近战命中把玩家水平推开 `MELEE_PUSH` = 1.50 格、上抛 `MELEE_LIFT` = 0.55；非五技能位为 1.0 / 0.42（`util/ManorFiveCombatRules.java` 第 5-6 行、`entity/EasterBossEntity.java` 第 1799-1813 行）。

## 五、铁魔法体系（每个角色 3 个法术）

- 每位角色固定 **3 个法术** = 2 个常规 + 1 个进阶（`Kind.additional()` 定义为 `ordinal() >= EFFIGY_PROCESSION`），取自 `ManorSpellRules.Kind`（`util/ManorSpellRules.java` 第 14-72 行）。
- 学派分布：0 号 唤魔 + 神圣；1 号 血 + 末影；2 号 唤魔 + 火焰 + 雷电；3 号 血；4 号 火焰 + 神圣；5 号 冰霜 + 自然 + 神圣；6 号 唤魔 + 雷电；7 号 末影；8 号 神圣；9 号 自然；10 号 冰霜。
- 法术等级：`MAX_LEVEL` = 1（唯一等级，`FullPowerBossSpell.getMaxLevel` 固定返回 1）；实际施法等级 `rank` = max(minLevel, min(2, maxLevel)) = 1，效果结算用的满功率等级 `FULL_POWER_LEVEL` = 5（`util/ManorSpellRules.java` 第 7-8 行、第 80 行；`integ/spells/ManorSpellCombat.java` 第 77 行）。
- 「基础威力」列取 `ManorBossSpellProfile.power(kind)`，是庄园 Boss 实际使用的威力常量（`integ/spells/ManorBossSpellProfile.java` 第 22-52 行）；`Kind.damage` 只在非庄园路径使用。威力 0 = 支援型（`Kind.support()`）。
- 实际伤害 = 基础威力 × 施法者 `SPELL_POWER` 属性 × 学派加成 × 法术配置 `POWER_MULTIPLIER`（`BossSpellTuning.scaledPower`，`integ/spells/BossSpellTuning.java` 第 55-61 行）。

| `variant` | 角色 | 法术 id（`ftboss:` 前缀） | 学派 | 基础威力 | 魔力 | 冷却（秒） | 施法（原始 → 庄园 tick） |
|---|---|---|---|---|---|---|---|
| 0 | 柚子 | `effigy_volley` | evocation | 16 | 65 | 16 | 24 → 16 |
| 0 | 柚子 | `effigy_ward` | holy | 0 | 75 | 26 | 28 → 19 |
| 0 | 柚子 | `effigy_procession` | evocation | 17 | 100 | 28 | 32 → 22 |
| 1 | 蛋蛋 | `candle_reap` | blood | 26 | 70 | 16 | 24 → 16 |
| 1 | 蛋蛋 | `candle_prison` | ender | 14 | 90 | 24 | 32 → 22 |
| 1 | 蛋蛋 | `candle_eclipse` | blood | 36 | 105 | 30 | 36 → 24 |
| 2 | 朵拉 | `arcane_fusillade` | evocation | 15 | 65 | 14 | 24 → 16 |
| 2 | 朵拉 | `breach_shell` | fire | 32 | 90 | 24 | 32 → 22 |
| 2 | 朵拉 | `prism_battery` | lightning | 20 | 105 | 26 | 32 → 22 |
| 3 | 功底 | `rending_cleave` | blood | 28 | 70 | 18 | 28 → 19 |
| 3 | 功底 | `crimson_oath` | blood | 0 | 80 | 30 | 28 → 19 |
| 3 | 功底 | `blood_verdict` | blood | 38 | 110 | 30 | 36 → 24 |
| 4 | 柠檬猫 | `searing_pan` | fire | 100.8 | 65 | 16 | 24 → 16 |
| 4 | 柠檬猫 | `hearth_feast` | holy | 0 | 95 | 30 | 32 → 22 |
| 4 | 柠檬猫 | `citrus_nova` | fire | 32 | 100 | 26 | 32 → 22 |
| 5 | 青衣 | `four_tides` | ice | 26 | 75 | 18 | 28 → 19 |
| 5 | 青衣 | `elixir_rain` | nature | 13 | 90 | 24 | 32 → 22 |
| 5 | 青衣 | `tidal_lotus` | holy | 0 | 110 | 32 | 32 → 22 |
| 6 | 文玄蓝 | `orbital_gears` | evocation | 17 | 80 | 20 | 28 → 19 |
| 6 | 文玄蓝 | `pressure_lance` | lightning | 335.2 | 90 | 22 | 32 → 22 |
| 6 | 文玄蓝 | `magnetic_storm` | lightning | 16 | 110 | 30 | 36 → 24 |
| 7 | 折耳根 | `shadow_brand` | ender | 78.4 | 85 | 22 | 32 → 22 |
| 7 | 折耳根 | `void_echo` | ender | 24 | 95 | 26 | 32 → 22 |
| 7 | 折耳根 | `nightfall_blades` | ender | 22 | 100 | 28 | 32 → 22 |
| 8 | 紫菜 | `luminous_rebuke` | holy | 26 | 70 | 18 | 24 → 16 |
| 8 | 紫菜 | `halo_sanctuary` | holy | 0 | 90 | 30 | 32 → 22 |
| 8 | 紫菜 | `seraph_descent` | holy | 34 | 105 | 28 | 36 → 24 |
| 9 | 麻雀 | `feather_fan` | nature | 25 | 65 | 16 | 24 → 16 |
| 9 | 麻雀 | `wingstep` | nature | 0 | 60 | 24 | 20 → 14 |
| 9 | 麻雀 | `feather_refuge` | nature | 0 | 100 | 32 | 28 → 19 |
| 10 | 雪黎 | `frost_needles` | ice | 24 | 75 | 18 | 28 → 19 |
| 10 | 雪黎 | `whiteout_edict` | ice | 14 | 100 | 28 | 36 → 24 |
| 10 | 雪黎 | `winter_clock` | ice | 16 | 110 | 30 | 36 → 24 |

- 魔力实耗 = 表值 + 32（`FullPowerBossSpell` 构造里 `mana + 4 × manaStep`，庄园的 `manaStep` = 8，`integ/spells/ManorSignatureSpell.java` 第 27 行、`integ/spells/FullPowerBossSpell.java` 第 7-12 行）。
- 施法 tick 的「庄园」值 = 原始值 × 2/3（`BossSpellTuning.bossCast` = ceil(t × 2/3)，下限 1 tick，`integ/spells/BossSpellTuning.java` 第 27 行），实际施法时长再被 clamp 到 1 ~ 200 tick（`integ/spells/ManorSpellCombat.java` 第 78 行）。
- 四类法术走三条特例分支，不走「基础威力 × 加成」那条路：`searing_pan` 与 `shadow_brand` 走 `manorSkillDamage(基础威力, 2.0)`；`pressure_lance` 走 `manorSkillDamage(基础威力, 1.5)`；`rending_cleave` 走 `GongdiCombatRules.cleaveDamage(近战伤害, 是否外圈)` = 近战 × 1.2（内圈：`INNER_RADIUS` 4.5 格 × 半径缩放 2.0 = 9.0 格内）或 × 2.0（外圈，锥长 6 格 × 半径缩放 2.0 = 12 格）（`integ/spells/ManorBossSpellProfile.java` 第 54-63 行、`util/GongdiCombatRules.java` 第 8-10 行、第 35-37 行、`integ/spells/ManorSpellEffects.java` 第 164-186 行）。
- `pressure_lance` 的 335.2 = `VENT_DAMAGE` 80 × `XUANLAN_DAMAGE_MULT` 4.19；`shadow_brand` 的 78.4 = `SHADOW_DAMAGE` 112 × `SHADOW_DAMAGE_CUT` 0.70；`searing_pan` 的 100.8 = `PAN_DAMAGE`（`entity/EasterBossEntity.java` 第 336 行、第 598 行、第 600 行、第 636 行、第 740 行、第 745 行；`integ/spells/ManorBossSpellProfile.java` 第 30-35 行）。

## 六、铁魔法施法节奏与判定常量

| 常量 / 规则 | 数值 | 出处 |
|---|---|---|
| 每次施法法术数 | 近战位 1 个、其余位 2 个（主手 + 副手同放，时长取两者较大；`SPELLS_PER_CAST` = 2 是远程位上限） | `util/ManorBossCombatRules.java` 第 6 行、第 87 行、`integ/spells/ManorSpellCombat.java` 第 83-96 行 |
| 施法间隔 `spellInterval` | 320 tick（近战位 0 / 1 / 3 / 4 / 7 / 8 / 10）或 80 tick（2 / 5 / 6 / 9 / 11）；功底再乘 `spellIntervalMultiplier` = 1.8（约 576 tick） | `util/ManorBossCombatRules.java` 第 79 行、第 102-104 行、`integ/spells/ManorSpellCombat.java` 第 40-42 行 |
| 首轮施法延迟 | 100 tick + (实体 id % 40) | `integ/spells/ManorSpellCombat.java` 第 160-164 行 |
| 单法术冷却锁 | `max(100, 法术自身冷却)` tick（柚子分身再 ×2，见「柚子分身施法」）；法术自身冷却 = `Kind.cooldown` 秒（14 ~ 32 秒） | 第 102-104 行、`util/ManorSpellRules.java` 第 31 行、`integ/spells/YouziCloneSpellProfile.java` 第 20-22 行 |
| 副法术冷却下限 | `max(100, 副法术自身冷却)` tick | 第 103-104 行 |
| 施法等级 `rank` | 1 | 第 77 行 |
| 施法距离 `RANGE` | 24 格（施法前置判定另要求目标 ≤ 32 格且有视线） | `util/ManorSpellRules.java` 第 11 行、`integ/spells/ManorSpellCombat.java` 第 41-43 行、第 166-167 行 |
| 命中目标上限 `MAX_TARGETS` | 12 | `util/ManorSpellRules.java` 第 9 行 |
| 场地上限与寿命 | `MAX_FIELDS` = 64；单个场地存在 100 tick 后清除 | 第 10 行、`integ/spells/ManorSpellEffects.java` 第 350-360 行 |
| 额外脉半径 `extraRadius` | 3.5 格（蚀月 / 血裁 / 夜刃 / 冬钟）或 4 格（其余） | `util/ManorSpellRules.java` 第 82-87 行 |
| 额外脉延迟 `extraDelay` | 12 tick（默认）/ 20 tick（血裁、炽天使）/ 24 tick（蚀月） | 第 89-95 行 |
| 额外脉脉数 | 1（默认）/ 2（夜刃）/ 3（游行、棱镜、冬钟）/ 4（磁暴），间隔 20 tick | 第 97-106 行 |
| 法术半径缩放 | `ManorBossCombatRules.radiusScale`：1 号蛋蛋 = 1.5，2 ~ 8 号与 10 号 = 2.0，其余（0 / 9）= 1.0；`spellRadius` 再给 6 号文玄蓝 +1.0 格。Boss 铁魔法判定与特效读取同一缩放，玩家卷轴范围不变 | `util/ManorBossCombatRules.java`、`integ/spells/BossSpellTuning.java`、`integ/spells/ManorSpellEffects.java`、`skill/SpellVisuals.java` |
| 锥形判定 `cone` | 前向 ∈ [0, length] 且横向 ≤ 0.8 + 前向 × 0.7；`length` = 6 格（`four_tides` 为 10 格），判定前偏移先按半径缩放归一，等价于锥长与锥宽同乘缩放 | `util/ManorSpellRules.java` 第 112-115 行、`integ/spells/ManorSpellEffects.java` 第 164-180 行 |
| 柚子分身施法 | 威力 `POWER_SCALE` = 0.35、首轮 40 tick、间隔 `CAST_INTERVAL` = 200 tick、射程 16 格、冷却 ×2 | `integ/spells/YouziCloneSpellProfile.java` 第 7-22 行 |
| 法术预警 | 施法期间每 6 tick 播一次预警特效；场地脉动期间每次脉动都重播 | `integ/spells/ManorSignatureSpell.java` 第 82-87 行、`integ/spells/ManorSpellEffects.java` 第 52-57 行 |
| 施法资格 | 迷你分身（柚子分身例外）、大巨人、`isNoAi`、终章成员一律不施法（其中「大巨人」与「终章成员」**当前不可能出现**，属防御性判断 —— 庄园战没有巨人/终章阶段） | `integ/spells/ManorSpellCombat.java` 第 33-35 行、`util/ManorSpellRules.java` 第 121-123 行 |
| 麻雀的施法白名单 | 9 号走通用铁魔法管线时只施放两个支援型法术（`wingstep` / `feather_refuge`，威力 0）；输出技「千羽散华」`feather_fan` 走 `MaqueFeatherCombat` 独立管线（见「八、支援、分摊与召唤限制」） | `integ/spells/ManorSpellCombat.java` 第 81-83 行、`integ/spells/MaqueFeatherCombat.java` 第 36-52 行 |

## 七、共享减益、禁疗、削甲与黑暗

| 机制 | 效果与数值 | 出处 |
|---|---|---|
| 自身净化 | 中到任意减益后 `PURGE_DELAY_TICKS` = 1200 tick（60 秒）清空自身全部减益，增益保留；没有减益则计时归零 | `entity/EasterBossEntity.java` 第 182-184 行、第 5297-5310 行 |
| 连锁增益 | 48 格内有麻雀且获得正面效果、距上次 ≥ `CHAIN_BUFF_CD` = 40 tick 时，自己再得 2 个随机增益（`CHAIN_BUFF_POOL` 16 项，15 秒，等级 `buffAmp(1)`），并给 32 格内最近玩家挂 1 个随机减益 | 第 128-145 行、第 2255-2299 行 |
| 同步玩家增益 | 每 `ManorBuffCopy.COOLDOWN_TICKS` = 300 tick（15 秒）复制 48 格内最近一名玩家的可复制正面效果：等级照抄、时长统一改写为 `ManorBuffCopy.DURATION_TICKS` = 600 tick（30 秒）；跳过伤害吸收、生命提升、任何带最大生命修饰符的效果与 `herbalbrews:renewal`；只加到自己身上，不动玩家原有效果；只有 9 号麻雀在复制成功后按 `SPARROW_NOHEAL_CHANCE` = 0.10 顺带禁疗。进场演出开场、演出结束与后续每位主战 Boss 出生前各额外复制一次（`prepareManorEntryBuffs`） | `boss/ai/ManorBuffCopy.java` 第 11-30 行、`entity/EasterBossEntity.java` 第 3209 行、第 3573-3601 行、`boss/HiddenBossIntro.java` 第 121 行、第 238 行、`boss/ManorFight.java` 第 790 行 |
| 随机减益池 | `RANDOM_DEBUFF_POOL` 共 9 项：虚弱 / 缓慢 / 挖掘疲劳 / 中毒 / 凋零 / 饥饿 / 反胃 / 霉运 / 发光；时长 10 秒、amplifier 1（II 级）；池中**不含**失明、黑暗、漂浮 | 第 140-145 行、第 2280-2281 行 |
| 禁疗 `ftboss:heal_ban` | 时长 `BAN_TICKS` = 600 tick（30 秒）；`LivingHealEvent` 把回血量改成 0；每 tick 末把血量涨回去（因此图腾的 `setHealth(1.0)` 与直接改血量的模组武器同样被抹掉）；期间拒绝生命恢复 / 伤害吸收 / 饱和 / 瞬间治疗四条效果 | `boss/ai/HealBan.java` 第 44-45 行、第 105-197 行、第 208-213 行 |
| 禁疗施加者 | 麻雀同步玩家增益成功时按 `SPARROW_NOHEAL_CHANCE` = 0.10 触发（署名「麻雀」）；雪黎飞刀命中（署名「雪黎的飞刀」）；雪黎的冰霜法术「白夜禁疗」`whiteout_edict`（挂 `ftboss:heal_ban` 60 tick） | `entity/EasterBossEntity.java` 第 3085-3088 行、`entity/XueliKnifeEntity.java` 第 148 行、`integ/spells/ManorSpellEffects.java` 第 304-305 行、`util/ManorSpellRules.java` 第 36 行、`boss/ai/HealBan.java` 第 64-71 行 |
| 削甲 A（功底黑色切割者） | `ArmorShred`：每层 `PER_STACK` = −5.0 护甲、上限 `MAX_STACKS` = 5（满层 −25 护甲）、时长 `DURATION_TICKS` = 200 tick；用 `ADDITION` 修饰符，过期由每 tick 的 `ArmorShred.tick` 清理 | `boss/ArmorShred.java` 第 25-28 行、第 62-92 行 |
| 削甲 B（`ftboss:armor_shred`） | `ArmorShredEffect`：每层 `SHRED_PER_STACK` = −10.0 护甲（最多 2 层）、上限 `MAX_STACKS` = 2（满层 −20 护甲）、时长 `TICKS` = 160 tick | `effect/ArmorShredEffect.java` 第 20-25 行 |
| 削甲 B 施加者 | 断罪斩（`rending_cleave`）命中挂 60 tick；血之裁决（`blood_verdict`）命中挂 80 tick | `integ/spells/ManorSpellEffects.java` 第 189 行、`integ/spells/ManorThirdSpellEffects.java` 第 52 行 |
| 暗影侵蚀 `ftboss:shadow_erosion` | 每层每秒 `DAMAGE_PER_STACK` = 4.0 点虚空伤害、上限 `MAX_STACKS` = 5 层、时长 `TICKS` = 200 tick；暗影烙印每次命中 +1 层 | `effect/ShadowErosionEffect.java` 第 19-24 行、`integ/spells/ManorBossSpellProfile.java` 第 101-103 行 |
| 黑暗 / 失明 | 折耳根武器命中挂黑暗 `SHADOW_DARKNESS_ON_HIT` = 300 tick + 失明 II 300 tick（15 秒；失明在代码里是字面量 300）；暗影烙印挂黑暗 `SHADOW_DARKNESS_TICKS` = 300 tick + 失明 II 300 tick + 凋零 III 360 tick | `entity/EasterBossEntity.java` 第 647-665 行、第 1868-1874 行、`integ/spells/ManorBossSpellProfile.java` 第 98-100 行 |
| 专属黑暗特效 `ftboss:shadow_blind` | 时长 `TICKS` = 60 tick、等级 `AMPLIFIER` = 1（II 级）；配套屏幕遮罩淡入淡出 `FADE_TICKS` = 8 tick、呼吸周期 `PERIOD_TICKS` = 100 tick | `effect/ShadowBlindEffect.java` 第 30-33 行、`util/ShadowDarknessRules.java` 第 6-7 行 |
| 流血 `ftboss:bleed` | 每层每秒 `DAMAGE_PER_SECOND` = 3.0 点魔法（穿甲）伤害、上限 `MAX_STACKS` = 10 层、默认时长 `TICKS` = 160 tick；`ExtraDebuffs.bleed` 把传入的 amplifier clamp 到 0 ~ 4（即 I ~ V 级） | `effect/BleedEffect.java` 第 22-27 行、`boss/ai/ExtraDebuffs.java` 第 39-41 行 |
| 蓝火 `ftboss:blue_fire` | 每秒 `DAMAGE_PER_SECOND` = 10.0 点虚空伤害，时长 `TICKS` = 360 tick | `effect/BlueFireEffect.java` 第 17-20 行、`boss/ai/ExtraDebuffs.java` 第 82-87 行 |
| 禁术拦截 | 庄园战场内禁止施放黑洞 / 止心术 / 橡肤（按法术 id 的 path `black_hole` / `heartstop` / `oakskin` 比对），事件 `SpellPreCastEvent` 直接取消 | `boss/ManorBan.java` 第 37-38 行、`boss/ManorSpellBan.java` 第 27-41 行 |
| 禁食清除 | 持续清除「饱腹代偿」`kaleidoscope_cookery:satiated_shield`、「滋养」`farmersdelight:nourishment`、「饱腹」`farm_and_charm:satiation` 三种增益；战场锚点 96 格内的玩家每 10 tick 查一次，清到至少一个就训话一次 | `boss/ManorBan.java` 第 40-45 行、第 80-94 行、`boss/ManorFight.java` 第 1068-1074 行 |

> ⚠ **2026-09-24 更新**：`ManorBan.BANNED_SPELLS` 现为 `black_hole` + `heartstop`（**两档难度都拦**），`oakskin` 仍只在终结难度拦；禁食方面「饱腹代偿」`kaleidoscope_cookery:satiated_shield` 改成**两档难度都清**（`ManorImpossibleRules.SHARED_BANNED_FOOD_BUFFS`），「滋养」「饱腹」仍只在终结难度清。
| 禁术 / 禁食共用台词 | 两句随机（`雕虫小技还敢班门弄斧？` / `呵，旁门左道`），屏幕中上方 + 聊天栏各发一次，并播铁砧音效 `ANVIL_LAND`（音量 0.8、音高 0.6） | `boss/ManorBan.java` 第 47-65 行 |

## 八、支援、分摊与召唤限制

| 系统 | 数值与规则 | 出处 |
|---|---|---|
| 女仆护法（10 号雪黎） | 一次召唤 `BATCH_MIN` = 1 ~ `BATCH_MAX` = 3 批次，每批 `PER_BATCH` = 2 位，合计 `MIN_TOTAL` = 2 ~ `MAX_TOTAL` = 6 位 `touhou_little_maid:maid`；同一时间只留一批；两次之间需过 `TRIGGER_CD` = 800 tick | `boss/ai/XueliMaids.java` 第 57-63 行、第 141-166 行 |
| 女仆护法免伤 | 只要还有一位女仆活着，雪黎**完全免伤**（`hurt` 直接返回 false）；例外是无视无敌的伤害（`/kill`、掉出世界）与女仆本身受到的「真实玩家伤害」 | `entity/EasterBossEntity.java` 第 2124-2133 行、`boss/ai/XueliMaids.java` 第 249-267 行 |
| 女仆护法回血 | 每位女仆每秒回 `HEAL_PERCENT_PER_MAID` = 0.001（0.1%）最大生命，即满 6 位时为每秒 0.6% | `boss/ai/XueliMaids.java` 第 64-65 行、第 309-319 行 |
| 女仆护法辅助规则 | 存活扫描缓存 10 tick、全量重扫 100 tick、刚召唤后 `graceTicks` = 20 tick 宽限不判全灭；离雪黎 > `FOLLOW_DISTANCE` = 16 格或卡墙 / 窒息时传送到身边；动作栏提醒每 `REMIND_CD` = 100 tick、被挡伤害提示每 `BLOCK_NOTIFY_CD` = 40 tick；广播半径 `PLAYER_RANGE` = 48 格 | 第 66-71 行、第 124-138 行、第 269-325 行、第 337-361 行 |
| 女仆标记 | 女仆打计分板标签 `ftboss_xueli_guard` + 持久化标记 `ftboss_xueli_guard` + 主人 UUID 记录；带标记者不被本模组 Boss 索敌 | 第 51-55 行、第 114-122 行、`entity/EasterBossEntity.java` 第 860-864 行 |
| 麻雀进场（9 号） | 完整庄园 11 位轮换里随机安排 **4～10 位**各来一次（`MIN_APPEARANCES` = 4、`MAX_APPEARANCES` = 10、`VALID_MASK` = 0xdff 覆盖 0–8 / 10 / 11）；单人每位 Boss 1 次，每多 1 名参战玩家额度 +1（`quota` = max(1, 参战人数)），两次出场至少隔 40 tick；波次结束离场（终结难度保留），换位可再出现；计划与出场记录存档，演出麻雀计入首场；全局同时最多 1 只；血量 `SPARROW_HEALTH` = 500.0（支援身份 500.0，本体基础生命 1500.0）；独立彩蛋 Boss 每 `CHECK_INTERVAL` = 200 tick 判定，概率 `standaloneChance` = 1 − 0.5^n → 1 / 2 / 3 人分别 50% / 75% / 87.5%；终结难度每场由 `ImpossibleSparrowSupport.ensure` 直接备好一只 | `boss/SparrowSupport.java` 第 36-53 行、`util/SparrowAppearanceRules.java` 第 10-31 行、`util/SparrowMultiplayerRules.java` 第 9-32 行、`boss/ai/ManorHealthBalance.java` 第 25-64 行 |
| 麻雀支援效果 | 每 `SPARROW_HEAL_TICKS` = 60 tick 给被支援 Boss 回 `SPARROW_HEAL_PERCENT` = 0.5%（即 0.005）最大生命 + `SPARROW_BUFFS` = 3 个随机增益（15 秒）；每 `SPARROW_CLEANSE_TICKS` = 100 tick 清掉另一位 Boss 的全部减益；每 `SPARROW_SHARE_TICKS` = 20 tick 双向共享正面效果；寿命 `SPARROW_LIFE_TICKS` = 1800 tick 后飞走（终结难度不飞走） | `entity/EasterBossEntity.java` 第 2745-2765 行、第 2807-2833 行 |
| 麻雀沉默 / 驱散 | 支援有效时每 `INTERVAL_TICKS` = 200 tick，对支援 Boss 48 格内每位存活、非创造 / 旁观 / 假人的玩家：从主物品栏 36 格（含快捷栏）与副手非空格中随机抽一格 → 该物品类型进入 20 tick 物品冷却，并拦下使用 / 蓄力 / 交互 / 近战 / 挖掘 / 本模组武器技能；同时随机移除一个非 ambient 正面效果（ambient 与负面保留）；两项都空则不操作 | `boss/ai/SparrowDisruption.java` 第 42-95 行、`util/SparrowDisruptionRules.java` 第 7-23 行、`boss/ai/SparrowDisruptionTargets.java` 第 10-12 行 |
| 麻雀随机借招 | 每 `INTERVAL` = 200 tick 等概率抽 11 位庄园 Boss 之一，再抽其已启用的招牌法术（合计 32 种，不含麻雀自身 3 种），沿用既有铁魔法施法管线，不切换麻雀变体；目标先取支援对象当前有效敌对玩家，失效时取 24 格内最近的有效玩家；友伤由 `LivingAttackEvent` 与友方判定双重拦截 | `integ/spells/SparrowBorrowedSpells.java` 第 78-116 行、第 150-179 行、`util/ManorSupportRules.java` 第 7-19 行、`util/ManorSpellRules.java` 第 9-11 行 |
| 千羽散华（麻雀输出技） | 冷却 `COOLDOWN_TICKS` = 160 tick（8 秒），首轮延迟 `INITIAL_DELAY` = 40 tick；一次放出 `FEATHERS` = 5 枚飞羽，各自在服务端扫掠（`AREA_RADIUS` = 8 格、`SPEED` = 1.38、寿命 `LIFETIME` = 160 tick、单枚最多 `HITS` = 3 次命中且每次都换目标）；命中挂 `HEAL_BAN_TICKS` = 100 tick 禁疗 | `integ/spells/MaqueFeatherFlights.java` 第 44-66 行、`util/MaqueRicochetRules.java` 第 4-10 行、`util/MaqueFlightRules.java` 第 4-10 行、`integ/spells/MaqueFeatherCombat.java` 第 134-143 行 |
| 麻雀位置规则 | 离被支援 Boss > `SPARROW_TELEPORT_RANGE` = 10.0 格时把自己传送过去，冷却 `SPARROW_TELEPORT_CD` = 60 tick；不攻击玩家、血条常隐 | 第 2343-2359 行 |
| 伤害分摊 | 48 格内有存活麻雀时替被支援 Boss 吃 `SPARROW_DAMAGE_SHARE` = 50%（直接扣她的血、不走受伤管线；她处于无敌时留 1 点不死）；柚子本体被 64 格内其迷你分身分摊 `YOUZI_MINION_SHARE` = 10%（平分给所有存活分身，谁血不够谁就地阵亡） | 第 2334-2335 行、第 2439-2449 行、第 2460-2531 行 |
| 柚子迷你分身 | 命中计数满 `YOUZI_HITS_PER_SUMMON` = 3 且距上次分裂 ≥ `MINI_SUMMON_CD` = 40 tick 时召 2 个；场上同变体分身达 `MINI_CAP` = 6 后不再召，改为给自己速度 II 10 秒；单个 150 血、`MINI_LIFE_TICKS` = 300 tick；本体每 60 tick（3 秒）把自身增益复制给 48 格内同变体分身；**★ 2026-09-24 起分身武器只剩三把**（`MINI_WEAPONS` = 斩龙剑 / 冰斧 / 荣耀金斧，按用户要求禁用`armamentarium:blood_prince_flamberge`血王子焰型剑） | 第 285-293 行、第 1116-1122 行、第 2003-2043 行、第 2745 行 |
| 柠檬猫助战 | 随机召 2 位**不同**的食物系 Boss（献祭机制用掉后一次 1 位）；若半血时仍未献祭会立即召齐两位并同 tick 保底献祭；献祭后直接助战真正死亡累计 `POST_SACRIFICE_HELPER_DEFEAT_LIMIT` = 3 位即永久停召，超时 / 换批 / 转场收回不计；持续 `SUMMON_LIFE_TICKS` = 1800 tick；额外血量 `SUMMON_EXTRA_HEALTH` = 100.0、额外 `SUMMON_EXTRA_BUFFS` = 5 个随机增益；名单为糖霜女皇 / 屠夫 / 茶树 / 酒桶王 / 鼠群（**不含**苹果爵士）；带持久化无奖励标记，死亡无物品、无经验 | `entity/EasterBossEntity.java`、`boss/ai/LemonSacrifice.java`、`boss/ai/LemonSummonRewards.java` |
| 助战不参与音乐 | 被柠檬猫召来的助战 Boss 打 `ftboss_no_bgm` 标记，被 BGM 调度统一拒绝 | 第 3898-3901 行、`sound/BossMusic.java` 第 278-288 行 |
| 召唤物归属继承 | 召唤时调 `ManorFightMembers.inherit`，写入 `ftboss_manor_owner`（根主人）与 `ftboss_manor_fight`（场次）；战场退役后 `EntityJoinLevelEvent` 会直接取消该场次实体的入场 | `boss/ManorFightMembers.java` 第 30-45 行、第 66-74 行 |
| 同类召唤上限工具 | `SummonLimit`：`RADIUS` = 64 格内同类上限 `MAX_PER_TYPE` = 10，到顶改为给同类全体上速度 II / 力量 I / 抗性 I，时长 `BUFF_TICKS` = 400 tick；庄园十一人**没有调用点** | `boss/ai/SummonLimit.java` 第 37-42 行、第 61-90 行 |
| 召唤地点限制 | `SummonGate.OVERWORLD_ONLY` = true，所有 Boss 只能在主世界召唤，其它维度直接中止召唤并提示 | `boss/SummonGate.java` 第 24-33 行、第 51-60 行 |

## 九、血条、提示与音乐

| 类别 | 规则与数值 | 出处 |
|---|---|---|
| 单体血条 | 每位庄园 Boss 自带 `ServerBossEvent`：名字为 `§c` + 中文名，颜色 `BossBarColor.RED`，样式 `BossBarOverlay.PROGRESS`，进度 = 当前血量比；朵拉叠「致命节奏」期间名字临时变成 `§c朵拉 §6[致命节奏 n/6]`，清空后还原 | `entity/EasterBossEntity.java` 第 817-819 行、第 980-981 行、第 2758 行、第 3654-3675 行 |
| 血条隐藏条件 | 演出期间（无敌 + 站定）隐藏自带血条；麻雀全程隐藏；迷你分身全程隐藏 | 第 2354 行、第 2646 行、第 2766-2767 行 |
| 庄园进度条 | `§c§l甜苹果庄园 §7已击败 §c n §7/§c 11`（分母 `MANOR_COUNT` = 11），样式 `BossBarOverlay.NOTCHED_10`，进度 = 已击败数 / `MANOR_COUNT`，显示半径 `ATTACH_RANGE` = 96 格 | `boss/ManorFight.java` 第 84 行、第 151-152 行、第 181-190 行 |
| 终章血条（**当前不生效**） | 当前版本没有共享终章血池与终章阶段：庄园战打完 `MANOR_COUNT` = 11 位直接结算胜利，`isFinaleMember` 只是返回 false 的兼容入口 | `boss/ManorFight.java` 第 497-501 行、第 957 行 |
| 技能预警提示 | 庄园五技能起手给 32 格内玩家发动作栏提示「角色名 【技能名】 蓄力中，离开预警区域！」；前摇期间每 4 tick 重画一次地面预警 | `boss/ai/ManorFiveSkills.java` 第 103-110 行、第 132 行、第 236-272 行 |
| 铁魔法预警 | 施法期间每 6 tick 播一次法术预警特效，并在落点画预警环 | `integ/spells/ManorSignatureSpell.java` 第 82-87 行、`integ/spells/ManorSpellEffects.java` 第 52-57 行 |
| 系统提示 | 麻雀进场给 96 格内玩家一条系统消息；女仆护法启动 / 破除 / 提醒走 `msg.ftboss.xueli_shield` 系列翻译键；禁疗提示「署名封住了你的恢复力！30 秒内无法回复血量」 | `boss/SparrowSupport.java` 第 173-178 行、`boss/ai/XueliMaids.java` 第 225 行、第 292 行、第 380 行、`boss/ai/HealBan.java` 第 79-80 行 |
| 音乐 · 演出阶段 | 开场秀开始时播《庄园·双轨坦克曲》全曲（`MANOR_DUAL_TICKS` = 5130 tick）；玩家点【放弃】或挑战确认结束时立即停 | `sound/BossMusic.java` 第 120-122 行、第 229-235 行、第 254-264 行 |
| 音乐 · 战斗阶段 | 正式开打播《庄园·鏖战》（`GUIGNOL_TICKS` = 4173 tick），放完自动换《庄园·双轨·战斗高潮》（`DUAL_BATTLE_TICKS` = 2960 tick），两首轮流播 | 第 101-114 行、第 164-172 行、第 237-252 行、第 605-614 行 |
| 音乐 · 在场判定 | 只有**非迷你分身**的 `ftboss:easter_boss` 算「在场 Boss」（分身不替主人续 BGM）；被柠檬猫召来的助战 Boss 带 `ftboss_no_bgm` 标记，不参与 BGM 调度；全场空场满 `GRACE_TICKS` = 100 tick 后停乐 | 第 61-62 行、第 278-288 行、第 292-344 行、第 429-432 行 |
| 音乐总开关 | `FTSounds.MUSIC_ENABLED` = true（关掉则整条音乐链都不启动） | `sound/FTSounds.java` 第 32 行 |

## 十、物理限伤与半血护甲

| 项目 | 规则与数值 | 出处 |
|---|---|---|
| 适用范围 | 轮换变体 0–8、10、11 的**非迷你、非巨大**庄园 Boss；麻雀支援位、迷你 / 巨大形态、非庄园 Boss 与玩家都不适用 | `util/ManorPhysicalDefenseRules.java` 第 11-16 行、`boss/ai/ManorPhysicalDefense.java` 第 32-35 行 |
| 物理来源 | 由伤害标签 `ftboss:physical_damage` 分类，魔法 / 真实伤害不算物理；管理员 `GENERIC_KILL` 直接跳过限伤 | `boss/ai/ManorPhysicalDefense.java` 第 25-26 行、第 47-50 行、`boss/ai/FinalBossDamageGuard.java` 第 37-41 行 |
| 滚动窗口 | `WINDOW_TICKS` = 20 tick 内物理最终扣血合计不超过 `MAX_PHYSICAL_DAMAGE` = 200.0 点；超额截断到剩余额度，额度耗尽后本次物理扣血为 0，按滚动窗口过期 | `util/ManorPhysicalDefenseRules.java` 第 6-7 行、第 34-61 行 |
| 额度归属 | 每只 Boss 各自一份 200 点额度，多人、多武器与弹体共用同一份，不是每位攻击者各 200 | `util/ManorPhysicalDefenseRules.java` 第 42-60 行、`boss/ai/ManorPhysicalDefense.java` 第 52-59 行 |
| 结算位置 | 护甲、其他减伤、分摊、伤害吸收与最终伤害事件都处理完之后才限伤；被伤害吸收（黄心）抵掉的伤害不占额度 | `entity/EasterBossEntity.java` 第 2600-2631 行、`mixin/FinalBossDamageMixin.java` 第 14-21 行 |
| 单次最终伤害屏蔽 | 走原版最终伤害管线时，最终单次伤害大于 `MAX_FINAL_DAMAGE` = 300.0 直接归零；名单覆盖庄园 Boss（非迷你）与其它受管 Boss | `boss/ai/FinalBossDamageGuard.java` 第 19 行、第 23-35 行、`mixin/FinalBossDamageMixin.java` 第 14-21 行 |
| 半血护甲 | 首次生命降到最大生命 50% 及以下（`halfHealth`）后护甲翻倍，回血超过半血也保留；同一 UUID 的修饰符不叠加，换实体或换变体重新计算 | `util/ManorPhysicalDefenseRules.java` 第 18-21 行、`boss/ai/ManorPhysicalDefense.java` 第 62-84 行 |
| 护甲上限 | 原版 ARMOR 属性的 30 上限保留；只在这些 Boss 的 `getArmorValue` 里让有效护甲翻倍（24 变 30 而不是 48），其他实体不受影响 | `boss/ai/ManorPhysicalDefense.java` 第 92-109 行、`entity/EasterBossEntity.java` 第 2480-2482 行 |
| 状态持久化 | 窗口历史与半血阶段写在实体 Forge 持久数据 `ftboss:physical_defense`；存档 / 重载不清空窗口，护甲修饰符在服务器恢复时重建 | `boss/ai/ManorPhysicalDefense.java` 第 28 行、第 37-45 行、第 71-84 行 |
| 不改的部分 | 护甲韧性与原版护甲伤害公式不变；护甲翻倍不等于所有伤害减半，忽略护甲的伤害仍然忽略护甲 | `boss/ai/ManorPhysicalDefense.java` 第 92-109 行 |

- 标签 `ftboss:physical_damage`（`src/main/resources/data/ftboss/tags/damage_type/physical_damage.json`，`replace: false`）默认收录 `player_attack` / `mob_attack` / `mob_attack_no_aggro` / `arrow` / `trident` / `thrown` / `mob_projectile` / `sting` / `explosion` / `player_explosion` / `falling_block` / `falling_anvil` / `falling_stalactite`，可选追加 `bren:bullet_type` 与 `armamentarium:assassination`（`required: false`，缺模组不影响标签加载）。

## 十一、庄园领域（Boss 光环）

- 领域半径 `ManorDomainRules.RADIUS` = `ManorTeleportRules.STRAY_FROM_BOSS` = 76.0 格，与召回光幕同半径；判定只看水平距离（`contains` 不比较高度差），`REFRESH_TICKS` = 20 tick 刷新一次，效果脉动 `PULSE_TICKS` = 100 tick 一轮（`util/ManorDomainRules.java` 第 6-7 行、第 15-18 行、`util/ManorDomainEffectRules.java` 第 4 行）。
- 11 位轮换变体各有领域，变体 9 麻雀没有领域（`ManorDomainEffectRules.kind` 返回「无领域」）；下表数值逐项来自 `ManorDomainRules` 与 `ManorDomainEffectRules` 的当前常量。
- 吸血常量：`LEECH_FRACTION` = 0.08、`LEECH_CAP` = 2.0 点、`LEECH_COOLDOWN` = 20 tick（每秒最多触发一次），恢复量取「实际伤害」与「剩余生命」较小者的 8%，再截到 2 点（`util/ManorDomainEffectRules.java` 第 5-7 行、第 88-91 行）。
- 回血常量：生命低于最大生命 70% 时每 5 秒回 1% 最大生命；玩家单次上限 1.0 点，Boss 按其最大生命 1% 回复（`util/ManorDomainEffectRules.java` 第 93-98 行）。

| `variant` | 领域名 | 类别 | 数值与效果（当前生效值） |
|---|---|---|---|
| 0 | 琼华照阙 | Boss 受益 | Boss 护甲 +4、承受伤害 −10% |
| 1 | 长夜悬灯 | 双方共享 | 双方命中吸血 8%（每秒最多 1 次、每次最多 2 点）；Boss 远程伤害 +18%；玩家侧只对本场庄园 Boss 阵营生效 |
| 2 | 参商渡星 | 双方共享 | 双方 6 格及以上的远程命中伤害 +22%；玩家侧只对本场庄园 Boss 阵营生效 |
| 3 | 无间玄庭 | 玩家受害 | 玩家获得的治疗 −25%；Boss 对生命低于 60% 的目标近战伤害 +25% |
| 4 | 扶桑照野 | 双方共享 | 双方攻击属性 +12% |
| 5 | 沧溟归墟 | 双方共享 | 双方承受伤害 −10%、移动速度 −10%；Boss 远程伤害 +18% |
| 6 | 璇玑太虚 | 玩家受害 | 玩家移动速度 −15%；Boss 击退抗性 +0.35、远程伤害 +20% |
| 7 | 幽篁照影 | 双方共享 | 双方从目标背后近战命中伤害 +20%；Boss 移动速度 +12% |
| 8 | 溟海浮槎 | 玩家受害 | 玩家护甲 −3；Boss 护甲 +2 |
| 10 | 霜华停云 | 玩家受害 | 玩家移动速度 −20%；Boss 护甲 +3、击退抗性 +0.20 |
| 11 | 紫微梦阙 | 双方共享 | 双方生命低于 70% 时每 5 秒回最大生命 1%（玩家单次最多 1 点）；Boss 每第三次远程命中伤害 +35% |

- 数值与名称出处：`util/ManorDomainRules.java` 第 20-52 行（远程增伤、护甲、移速、击退抗性、攻击属性）、`util/ManorDomainEffectRules.java` 第 11-91 行（类别、名称、描述、玩家侧护甲 / 移速 / 攻击、承伤、治疗、背后近战、吸血）。
