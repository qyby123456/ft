# 酒魔·酒桶王
`实体 id`: ftboss:wine_barrel ｜ 源文件: WineBarrelBossEntity.java + boss/ai/BarrelTactics.java + entity/SmallBarrelEntity.java + entity/BarrelCannonballEntity.java

## 一句话定位
固定在原地的巨型酒桶型铁魔法 Boss（移速 0）：近身靠滚动冲撞与开塞喷射，远程靠火系法术、火球与酒桶炮弹齐射；火焰伤害反而治疗它。

## 基础数值
| 项目 | 数值 | 说明 |
|---|---|---|
| 血量 | 400 | createAttributes MAX_HEALTH 400.0 |
| 护甲 | 6.0 | Attributes.ARMOR |
| 攻击力 | 5.5 | ATTACK_DAMAGE；近战几乎不出手，MeleeAttackGoal 速度 0.0 |
| 移动速度 | 0.0 | MOVEMENT_SPEED；位移只来自冲撞的 setDeltaMovement |
| 跟随范围 | 40.0 | FOLLOW_RANGE |
| 击退抗性 | 1.0 | KNOCKBACK_RESISTANCE，完全免疫击退 |
| 经验 | xpReward 80 | 无额外经验球 |
| 碰撞箱尺寸 | 3.0 × 3.0 格 | FTEntities.WINE_BARREL 注册尺寸 |
| Boss 条颜色 | PURPLE，PROGRESS，半径 48 | 构造器 new ServerBossEvent 并登记 |
| 是否定身不移动 | 是（移速 0），但会滚动冲撞 | ST_CHARGE 以 0.42 格/tick 位移 |
| 召唤物与掉落物 | 小酒桶（自爆）+ 啤酒人（需 brewery）+ 分身（1 血，上限 2） | 掉落 石像·酒桶王；无其它掉落（代码里未找到 ?） |

## 阶段机制
- 阶段划分: applyStage 按血量比例，<0.67 进阶段 1、<0.34 进阶段 2，只前进不回退；各技能 CD 缩短见各技能节。
- 进阶段效果: 已有 4 种酒 buff 全部 +1 级（amp 上限 4）；阶段 1 补 vinery:resistance_effect amp0、阶段 2 补 vinery:luck_effect amp1，均 600t。
- 火焰伤害转化: hurt() 里伤害来源 msgId 含 fire、lava、burn 或 blaze 时 heal(amount × 0.75)。
- 通用狂暴 BossRage: ≤50% 狂暴 I（迅捷 I、抗性 I），≤25% 狂暴 II（追加 力量 I、再生 I），各 80t、每 20t 刷新；层级同时抬高火球伤害与法术等级。
- BossEmpower（每 20t）: 常驻 +25% 移速、+30% 攻击、+4 护甲、+0.3 击退抗性；每 400t 续 520t 的 迅捷 I、抗性 I、抗火 I、力量 II。
- 分身诱饵: 血量 <50% 且 tickCount % 600 < 20 且场上少于 2 个时生成 WineBarrelDecoyEntity（同模型、setNoAi(true)、生命 1.0、名字 §7酒桶王的分身、不攻击），本体移除时由 SummonSweep 清除。

## 技能与机制
### 滚动冲撞与砸地
- 触发: chargeCd 到时、22 格内有玩家且未咏唱；chargeCd 初值 110，砸地后置 130
- 效果: 先蓄力 10t（WOOD_STEP 1.1/0.55 与 8 点 POOF），再冲 22t：每 tick setDeltaMovement(方向 × 0.42) 且 hasImpulse 置真；每 3t 酒渍木屑粒子、每 6t WOOD_STEP 1.5/0.45
- 效果（接触）: 每 8t 调 hitPlayers，2.9 格内玩家吃 8.0 × 0.5 = 4.0 点 mobAttack 并被击退 1.1
- 效果（砸地）: 冲撞结束自动进入，6.5 格内玩家吃 4.75 点 mobAttack、被 knockback 1.5 并挂 反胃 amp0 与 120t；音效 WOOD_BREAK 1.7/0.5 与 GENERIC_EXPLODE 0.5/1.6
### 开塞喷射
- 触发: popCd 到时且 22 格内有玩家；popCd 初值 80、结束置 100；动作 18t，第 14t 结算
- 效果: 正前方锥形（点积 ≥0.35、≤11 格）内玩家吃 3.0 点 mobAttack 并挂 缓慢 amp0 100t、反胃 amp0 160t；音效 BOTTLE_EMPTY 1.5/0.8
### 火系远程（火焰弹与法术连射）
- 效果（火焰弹）: 触发 CD 70 或 45（阶段 2）；生成铁魔法 MagicFireball，setDamage(11 + 狂暴层级 × 4) 即 11、15、19；出膛在朝目标 1.8 格、高度 +1.6 处；音效 BLAZE_SHOOT 1.2/0.85
- 效果（法术连射）: 触发 CD 58 或 34，队列非空且未咏唱时逐个施放；从 spellPool() 筛 isFire，每次入队 3+rand(3) 即 3 到 5 个互不相同法术，等级 = clamp(4 + 狂暴层级, min, max) 即 4、5、6；狂暴层级 ≥1 时 70% 概率换成 rarityOf ≥3（EPIC）的法术
### 酒桶炮弹齐射与酒桶弩预警
- 效果（齐射）: 触发 CD 170（阶段 0 与 1）或 110（阶段 2）；一次抛 3+rand(3) 即 3 到 5 颗 BarrelCannonballEntity，落点在目标周围 ±3.5 格随机偏移；水平速度 1.05、按距离补偿抛物线（重力 0.045/tick），出膛在朝目标 2.2 格、高度 +1.9 处；发射音 FIREWORK_ROCKET_LAUNCH 1.6/0.7
- 效果（炮弹本体）: EXPLOSION_POWER = 3.0（注释口径闪电苦力怕 6，已折半）且不毁方块；直击额外 DIRECT_DAMAGE = 4.0；寿命 MAX_LIFE = 80t 或落地即炸
- 酒桶弩预警: tickCrossbow 在 bState == ST_IDLE 且 cannonCd ≤ CROSSBOW_READY_LEAD = 20 时把主手换成装填完成版 ftboss:barrel_crossbow_ready（平时 ftboss:barrel_crossbow），只在状态变化或手里没弩时 setItemSlot、掉落率 0.0；该弩不发射箭矢也不造成伤害
### 小酒桶与啤酒人召唤
- 触发: summonCd 210（阶段 0）或 150（阶段 1 起）
- 效果: 生成 2+rand(2) 即 2 到 3 只小酒桶（SmallBarrelEntity），装有 brewery 时额外 1 只 brewery:beer_elemental；落点半径 2.5 到 6.0 格；全部 setGlowingTag、同步目标并复制本体酒 buff
- 效果（小酒桶自爆）: 目标进入 5.0 格点燃，FUSE = 30t（1.5 秒）后引爆，跑出 9.0 格熄灭；BLAST = 1.2（原 2.4 折半）且 ExplosionInteraction.NONE 只伤实体；音效 TNT_PRIMED 与 GENERIC_EXPLODE
### 醉酒光环与酒buff
- 触发: 每 30t 对 7 格内玩家；每 10t 检查自身酒 buff
- 效果: brewery:drunk 200t amp0、原版 反胃 amp0 160t、brewinandchewin:tipsy amp4（微醺 V）240t（后两者需对应 mod）；自身常驻 vinery:armor_effect amp0 与 vinery:health_effect amp1，600t、剩余 <120t 即续
### 反制与通用规则
- 原“单次 >500 点整下无效”规则已取消；免疫 armamentarium 流血与真实伤害（hemorrhage、blood_loss、doom）；传奇怪物式限伤总开关 LIMIT_ENABLED = false；Boss 阵营友伤免疫，炮弹 canHitEntity 排除其它炮弹与本体
- 主世界限定召唤；每 20t 清空自身非增益且免疫一切负面效果；白天不烧；受击音 WOOD_BREAK 0.9/0.6

## 召唤物 / 掉落 / 音乐
- 入口: 酒引 ftboss:wine_charm 右键消耗 1 个，落在视线前 2.5 格，音效 BREWING_STAND_BREW 1.0/0.9；非主世界被 SummonGate 拦下
- 退场清理: remove() 把 minions 名单全部 discard()，并用 SummonSweep.clear 清掉半径 80 内的 WineBarrelDecoyEntity 与 SmallBarrelEntity
- 掉落: 石像·酒桶王（BossMercy.giveStatue）+ xpReward 80 经验；无其它掉落（代码里未找到 ?）
- BGM: 醉乡酒馆，music/tavern.ogg（Group.TAVERN）整场不切
- 自爆与炮弹爆炸均 ExplosionInteraction.NONE 不破坏地形；连死怜悯 5 次离场且无掉落无经验

## 应对要点
- 主手换成装填版酒桶弩后的 20t（1 秒）内正是齐射窗口，立刻离开落点（落点另有 ±3.5 格偏移）
- 冲撞是速度 0.42 的直线位移、总长 22t，横向走位 2 到 3 格即可躲开
- 开塞喷射只覆盖正前方约 70° 锥形，绕到侧后方反而安全，但 7 格内醉酒光环持续工作
- 不要用火焰伤害（75% 转成它的治疗）；拖到阶段 2 会让法术 CD 降到 34、炮弹 CD 降到 110 且酒 buff 再 +1 级

## 关键常量（代码位置）
- `WineBarrelBossEntity.java: MAX_HEALTH 400.0 / ARMOR 6.0 / ATTACK_DAMAGE 5.5 / MOVEMENT_SPEED 0.0 / KNOCKBACK_RESISTANCE 1.0 / FOLLOW_RANGE 40.0 / BOSS_BAR_RANGE 48.0 / 阶段阈值 0.67 与 0.34`
- `WineBarrelBossEntity.java: castCd 58 或 34 / fireballCd 70 或 45 / cannonCd 170 或 110 / summonCd 210 或 150 / 火球 11 + 狂暴层级 × 4 / CROSSBOW_READY_LEAD 20 / 火焰伤害 heal(amount × 0.75)`
- `WineBarrelBossEntity.java: chargeCd 110 或 130 / popCd 80 或 100 / ST_WINDUP 10t / ST_CHARGE 22t / ST_POP 18t / 冲撞 0.42 格每 tick 与 2.9 格每 8t 判定 / 炮弹 3+rand(3) 颗偏移 ±3.5`
- `BarrelTactics.java: slam 4.75 伤害 6.5 格 knockback 1.5 / corkSpray 3.0 伤害 11 格 点积 0.35 / hitPlayers 伤害 ×0.5；BarrelCannonballEntity.java: EXPLOSION_POWER 3.0F / DIRECT_DAMAGE 4.0F / MAX_LIFE 80；SmallBarrelEntity.java: MAX_HEALTH 30.0 / FUSE 30 / BLAST 1.2f`
