# 糖霜女皇
`实体 id`: ftboss:sugar_queen ｜ 源文件: SugarQueenEntity.java + boss/SugarQueenBoss.java

## 一句话定位
铁魔法施法系飞行法师 Boss（姜饼国王外观），行为全部由 SugarQueenBoss 静态大脑驱动：悬停环绕、手动施法、冲刺撞击、波次召唤糖系小怪，首次死亡自动进入终局形态。

## 基础数值
| 项目 | 数值 | 说明 |
|---|---|---|
| 血量 | 360 | SugarQueenEntity.MAX_HEALTH 360.0；SugarQueenBoss.MAX_HP 360 同值 |
| 护甲 | 5.0 | Attributes.ARMOR |
| 攻击力 | 1.5 | ATTACK_DAMAGE，只影响撞人与复制体挥杖，主输出是法术 |
| 移动速度 | 0.22 | MOVEMENT_SPEED，实际位移由 teleportTo 逐帧驱动 |
| 跟随范围 | 40.0 | FOLLOW_RANGE |
| 击退抗性 | 0.6 | KNOCKBACK_RESISTANCE |
| 经验 | 未设 xpReward（代码里未找到 ?） | 死亡额外生成 ExperienceOrb 200 |
| 碰撞箱尺寸 | 1.2 × 2.7 格 | FTEntities.SUGAR_QUEEN 注册尺寸 |
| Boss 条颜色 | PINK，PROGRESS（本体与复制体都有） | v110 修复：本体在 `tickBossBar()` 里建 `ServerBossEvent(Component.translatable("boss.ftboss.sugar_queen"), PINK)` 并 `BossBars.track`，48 格内可见；复制体的条在 `tickCloneBrain` 里单独建 |
| 是否定身不移动 | 否 | 入场 setNoGravity(true) 悬停飞行 |
| 召唤物与掉落物 | 小姜饼人 / 焦糖结晶 / 糖霜蜘蛛 / 软糖史莱姆 | 掉落 糖 24+rand(25)、石像·姜饼王、30% 秘制焦糖配方、糖果拐杖 |

## 阶段机制
- 二阶段: tick 里 health/max < 0.5 置 phase2，summonCd 压到 SUMMON_CD_P2 = 120，castCd 由 85 降 62，冲刺结算后 dashCd 由 140 降 90；每 20t heal(0.5f)，8 格内玩家每 20t 吃 缓慢 amp1 与 140t、虚弱 amp0 与 140t、饥饿 amp0 与 140t。
- 终局二命: 首次死亡由 BossHandler 调 tryPopTotem 拦截，血设回 MAX_HP × 0.3 = 108、removeAllEffects()、cancelCast()；finalMode 下 castCd = 45、dashCd = 90、召唤 120t。只触发一次（totemUsed）。
- 通用狂暴 BossRage: ≤50% 进狂暴 I（迅捷 I、抗性 I，各 80t），≤25% 进狂暴 II（追加 力量 I、再生 I），每 20t 刷新，层级存 ftboss_rage_tier。
- 阶段播报全部只写日志，玩家侧只看到每 10t 的动作栏血量。

## 技能与机制
### 悬停环绕走位
- 触发: 每 tick，未冲刺且未咏唱
- 效果: 绕最近玩家半径 6.5 格圆周漂移（flyA += 0.02/tick），高度为目标 Y + 2.0 + sin(tick×0.05)×0.35，单 tick 位移 ≤0.28 格，并持续面向玩家
### 手动铁魔法施法
- 触发: castCd 到时且未咏唱；初值 100，循环 85 / 62 / 45（一阶段 / 二阶段 / 终局）
- 效果: 随机取 1 个法术，等级 = clamp(4 + 狂暴层级, min, max)；狂暴层级 ≥1 时 70% 概率改用 spellPoolRare(6, 3)（3 = EPIC）；施法前反射写入 TargetEntityCastData 锁定玩家
### 冲刺撞击
- 触发: dashCd 到时、距离 6.0 到 26.0 格、未咏唱
- 效果: dashTicks = 16，每 tick 以 0.95 朝目标 eyeY-0.6 直线 teleportTo；距离 <2.2 或冲完即结算
- 效果（结算）: 4.5 格内玩家吃 8 点 mobAttack（原 16 已折半）+ 击退 1.8 + 缓慢 amp1 与 80t；半径外目标只吃 4 点擦身伤害；粒子 CRIT 40 与 CLOUD 30，音效 GENERIC_EXPLODE 与 ANVIL_LAND
### 波次召唤
- 触发: summonCd 到时；170（一阶段）/ 120（二阶段与终局）
- 效果: WAVES = {4,0} {4,0} {5,1} {5,1} 循环（总数, 精英数）；落点半径 3 到 8 格，在 ±14 格内自动找安全 Y
- 效果（种类）: 非精英每 4 只中 3 只是小姜饼人，第 4 只轮换焦糖结晶（尸壳）/ 糖霜蜘蛛 / 软糖史莱姆（setSize(3)）；精英取 (waveIdx+spawned)%2，即小姜饼人或焦糖结晶
- 效果（增益与指令）: 精英 迅捷 amp2、力量 amp1、生命提升 amp1，尸壳类再穿粉色皮革（0xF0B6FF）拿带锋利 4+rand(2) 的甜点；非精英 迅捷 amp2（仅蜘蛛）其余 amp1、力量 amp1（史莱姆与小姜饼人）、小姜饼人额外抗性 amp0；每 20t 重新索敌，<2.2 格主动攻击，武器掉落率 0
### 克隆体
- 触发: 本体在场时再次使用召唤物，走 spawnClone
- 效果: 同血量同外观，由 tickCloneBrain 驱动：32 格内最近玩家为目标，悬停其上方 2.2 格（≤0.25 格/tick），cloneCastCd = 70+rand(40) 时 castRandom(3+狂暴层级)，<3.0 格时 cloneMeleeCd = 30 挥杖；自带 PINK 血条
### 甜蜜光环与负面免疫
- 触发: 每 10t 续光环，每 20t 清 debuff
- 效果: farm_and_charm:sweets amp2 与 brewinandchewin:sweet_heart amp0，600t，剩余 <120t 即续；每 20t 清空自身非增益；BossHandler.onEffectApplicable 对本体直接 DENY 一切负面效果
### 反制与通用规则
- 原“单次 >500 点整下无效”规则已取消，绕过无敌的伤害除外；完全免疫 armamentarium 的流血与真实伤害关键字（hemorrhage、blood_loss、doom 等）
- 传奇怪物式限伤（单次上限 58、连击递减、反逃课 32 格）总开关 LIMIT_ENABLED = false，未启用；Boss 阵营友伤免疫；主世界限定召唤
- 破困 TrapBreaker 每 10t 清掉身上的矿车、船与坐垫类方块；防卡瞬移 BossUnstuck 每 80t 自查，位移 <2 格且离目标 >4 格时瞬移到目标附近

## 召唤物 / 掉落 / 音乐
- 入口: 糖霜水晶 ftboss:sugar_queen_crystal 右键消耗 1 个；非主世界被 SummonGate 拦下
- 小怪无硬性上限（未接 SummonLimit），清场按 ftboss_summon 标记走 SummonSweep.clearMarked，半径 80
- 掉落: 糖 24+rand(25)、石像·姜饼王、30% 秘制焦糖配方（§6 命名纸）、糖果拐杖（BossMercy 统一发放）、ExperienceOrb 200
- BGM: 木偶剧场·加洛普，music/gingerbread.ogg（Group.GINGERBREAD），整场一首不切
- 脱战与规则: 距离 >50 格清场；主人下线即消散；连死怜悯 5 次离场且无掉落无经验
- 人数缩放: BossScaling 与 BossEmpower 由 BossBars.forEach 遍历**登记血条的实体**施加。v110 之前本体不建血条 → 本体既没血条、也不吃 ×1.25^(人数-1) 血量与 +25% 移速 / +30% 攻击；**v110 起本体也登记血条，缩放与强化同样生效**（想退回旧行为：注释掉 `tickBossBar()` 里的 `BossBars.track` 一行）

## 应对要点
- 冲刺前有 RAVAGER_ROAR 音效播报，听到就侧移：撞击只覆盖 4.5 格，且它用 teleportTo 直线接近
- 本体免疫一切负面效果且每 20t 自动清 debuff，磨血无效，只能走直伤
- 第一次打到 0 血只是终局：回 108/360、清空自身效果、castCd 缩到 45t
- 二阶段起 8 格内每秒被叠缓慢、虚弱、饥饿，请在 8 格外输出
- 单次 >500 的爆发不再被全局阈值拦截，高倍率单发武器无需因该规则拆分

## 关键常量（代码位置）
- `SugarQueenEntity.java: MAX_HEALTH 360.0 / ARMOR 5.0 / ATTACK_DAMAGE 1.5 / MOVEMENT_SPEED 0.22 / KNOCKBACK_RESISTANCE 0.6 / FOLLOW_RANGE 40.0`
- `SugarQueenEntity.java: tickBossBar() 本体 PINK 条 + BAR_RANGE 48.0（v110 新增）`
- `SugarQueenBoss.java: MAX_HP 360 / DESPAWN_RANGE 50 / SUMMON_CD 170 / SUMMON_CD_P2 120`
- `SugarQueenBoss.java: WAVES {4,0}{4,0}{5,1}{5,1} / 落点半径 3~8 / 安全 Y 扫描 ±14`
- `SugarQueenBoss.java: dashTicks 16 / speed 0.95 / dashCd 140 或 90 / dashSlam range 4.5 与 dmg 8f`
- `SugarQueenBoss.java: castCd 85 / 62 / 45，法术等级 4 + 狂暴层级 / tryPopTotem 回血 MAX_HP × 0.3`
- `SugarQueenEntity.java: cloneCastCd 70+rand(40) / cloneMeleeCd 30 / 悬停 2.2 格 / sweets amp2 与 sweet_heart amp0 均为 600t`
- `GingerbreadMinionEntity.java: MAX_HEALTH 36.0 / ATTACK_DAMAGE 3.0 / MOVEMENT_SPEED 0.34 / KNOCKBACK_RESISTANCE 0.4 / xpReward 5 / spellCd 160+rand(120) 距离 24`
- `SugarQueenCrystalItem.java: stacksTo(16) / Rarity.RARE`
